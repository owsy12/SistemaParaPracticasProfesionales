---
title: "Auditoría Técnica Integral — Sistema SGPP"
subtitle: "Sistema para Prácticas Profesionales · UV / FEI"
author: "Claude Code — Arquitecto Senior / QA / Analista"
date: "2026-06-10"
lang: es
geometry: margin=2.5cm
fontsize: 11pt
toc: true
toc-depth: 3
numbersections: true
colorlinks: true
linkcolor: blue
urlcolor: blue
header-includes:
  - \usepackage{booktabs}
  - \usepackage{longtable}
  - \usepackage{array}
  - \usepackage{xcolor}
  - \definecolor{critical}{RGB}{192,0,0}
  - \definecolor{high}{RGB}{237,125,49}
  - \definecolor{medium}{RGB}{255,192,0}
  - \definecolor{low}{RGB}{91,155,213}
---

\newpage

# Resumen Ejecutivo

El sistema es una aplicación de escritorio **JavaFX** con arquitectura en capas
(GUI → Controller → DAO → DataAccess), patrón DTO y gestión de excepciones de dominio propio.
La base del proyecto es sólida y el estándar de codificación está bien documentado.
Sin embargo, se encontraron **5 errores críticos funcionales** que impiden el correcto
funcionamiento del sistema, **5 hallazgos altos** con impacto directo en integridad de datos,
**9 hallazgos medios** y **9 bajos**.

Los hallazgos más graves son:

- **Bug funcional confirmado en `AddInternController`**: provoca un `NumberFormatException`
  al registrar un practicante con créditos vacíos.
- **Fuga de conexiones de base de datos en `UserDAO`**: cada inicio de sesión y cada
  consulta de usuarios abre conexiones MySQL que nunca se cierran, agotando el pool del servidor.
- **Método `validateUser()` vacío**: ningún dato de usuario es validado antes de persistirse.
- **Error lógico en `ProjectDAO.update()`**: al editar cualquier campo de un proyecto su
  estado siempre se resetea a `"Disponible"`.
- **Tres rutas FXML con capitalización incorrecta**: rotas en entornos Linux/Docker.

\newpage

# Comprensión del Proyecto

## Arquitectura

```
GUI (FXML + CSS)
   └─ Controller (JavaFX @FXML)
         └─ DAO (JDBC PreparedStatement)
               └─ DataAccess.DataBaseConnection (dotenv + DriverManager)
                     └─ MySQL (schema: spp)
```

## Capas identificadas

| Capa | Paquete | Responsabilidad |
|---|---|---|
| Vista | `GUI/View/` | FXML + CSS por vista |
| Controlador | `GUI/Controller/` | Eventos, validación UI, navegación |
| Utilidades GUI | `GUI/Utils/` | ValidationUtils, AuditLog, SessionManager |
| Generación Docs | `GUI/DocumentGeneration/` | PDF/DOCX via POI/iText |
| DAO | `Logic/DAO/` | CRUD JDBC |
| DTO | `Logic/DTOs/` | Objetos de transferencia |
| Interfaces | `Logic/Interface/` | Contratos de DAOs |
| Excepciones | `Logic/Exceptions/` | ValidationException, DuplicateEntryException, ServiceException |
| Acceso a Datos | `DataAccess/` | DataBaseConnection (dotenv) |

## Tecnologías

Java 17+, JavaFX, JDBC MySQL, BCrypt (jBCrypt), java-dotenv, Apache POI, iText.

## Patrones detectados

- **DAO + DTO** — correcto
- **Singleton** — `SessionManager` (implementación con riesgo — ver Bajo-3)
- **MVC** adaptado a JavaFX
- **Builder** — `ReportGenerationContextBuilder`

\newpage

# Hallazgos Críticos

---

## CRÍTICO-1 — `UserDAO`: Fuga de Conexión en Cada Instanciación

**Archivos:** `src/Logic/DAO/UserDAO.java` líneas 22–24 ·
`src/Logic/Utils/Connection.java` líneas 9–10

### Evidencia

```java
// Connection.java — campo público estático mutable
public class Connection {
    public static java.sql.Connection connection;   // NUNCA SE CIERRA
    public static java.sql.Connection createdConnection() throws ServiceException {
        connection = DataBaseConnection.connectDatabase();
        return connection;
    }
}

// UserDAO.java — conexión almacenada como campo de instancia
public UserDAO() throws ServiceException {
    connection = createdConnection();  // Creada en constructor, nunca cerrada
}
```

### Flujo que lo provoca

1. `LoginController.initialize()` → `hasNoRegisteredUsers()` → `new UserDAO()` → conexión creada, nunca cerrada.
2. `LoginController.clickLogin()` → `loginProcess()` → `new UserDAO()` → otra conexión, nunca cerrada.
3. En cada login: **mínimo 2 conexiones MySQL abiertas y nunca liberadas**.

### Causa raíz

`UserDAO` almacena la conexión como campo de instancia. Los métodos solo hacen
`try-with-resources` sobre el `PreparedStatement`, no sobre la conexión.
Adicionalmente `Logic.Utils.Connection.connection` es un campo `public static`
que sobreescribe la referencia en cada llamada, haciendo irrecuperable la
conexión anterior.

**Agravante:** `InternDAO extends UserDAO` y llama a `super.saveUser(intern)`
usando la conexión persistente de `UserDAO`, mientras sus propios métodos
abren conexiones correctas. Resultado: **2 conexiones abiertas simultáneamente
en cada registro de practicante**.

### Impacto

Agotamiento del pool de conexiones MySQL → el sistema deja de funcionar
completamente en producción.

---

## CRÍTICO-2 — `UserDAO.validateUser()`: Método de Validación Completamente Vacío

**Archivo:** `src/Logic/DAO/UserDAO.java` líneas 254–255

### Evidencia

```java
private void validateUser(User user) throws ValidationException {
    // VACÍO — no valida nada
}
```

Llamado desde `saveUser()` (línea 28) y `update()` (línea 135) antes de persistir.

### Causa raíz

El método fue declarado con la firma correcta pero su cuerpo nunca fue
implementado.

### Impacto

Posible inserción de datos nulos/inválidos en la tabla `usuario`, violación de
constraints de BD (errores no controlados), datos corruptos.

---

## CRÍTICO-3 — `AddInternController`: `NumberFormatException` al Registrar Practicante

**Archivo:** `src/GUI/Controller/AddInternController.java` líneas 136–163 y 108

### Evidencia

```java
// hasEmptyFields() — computa isCreditEmpty pero NO lo incluye en el resultado
private boolean hasEmptyFields() {
    boolean isCreditEmpty = creditTextField.getText().isEmpty(); // calculado
    // ...
    boolean hasEmptyFields = isIdEmpty || isLastNameEmpty || isSecondLastNameEmpty
            || isEmailEmpty || isFirstNameEmpty || isPasswordEmpty
            || isConfirmPasswordEmpty;
    // isCreditEmpty AUSENTE de la expresión final
    return hasEmptyFields;
}

// registrationProcess() — crash si créditos está vacío
intern.setCredits(Integer.parseInt(creditTextField.getText())); // CRASH
```

### Flujo que lo provoca

1. Usuario llena todos los campos excepto "Créditos".
2. `hasEmptyFields()` retorna `false` (créditos vacíos no se verifica).
3. `registrationProcess()` ejecuta `Integer.parseInt("")` →
   **`NumberFormatException` no capturada** → crash silencioso.

### Causa raíz

`isCreditEmpty` fue calculado correctamente pero olvidado en la expresión
booleana de retorno. El bloque `catch` solo captura
`DuplicateEntryException`, `ValidationException` y `ServiceException`,
no `NumberFormatException`.

### Impacto

El flujo de registro de practicantes falla con crash silencioso para el usuario.

---

## CRÍTICO-4 — `UserDAO.findByEmail()`: Retorna `new User()` en Vez de `null`

**Archivo:** `src/Logic/DAO/UserDAO.java` líneas 231–251

### Evidencia

```java
public User findByEmail(String email) throws ServiceException, ValidationException {
    User user = new User();  // objeto vacío con id=0
    // ...
    if (resultSet.next()) {
        user = mapUser(resultSet);  // solo se asigna si existe
    }
    return user;  // retorna User vacío si no se encontró
}
```

Contraste: `findById()` retorna `null` si no encuentra nada.

### Causa raíz

Inconsistencia de contrato de retorno. Cuando un llamador hace
`if (user != null)` la verificación siempre pasa aunque no haya usuario real.

### Impacto

Cualquier flujo que use `findByEmail()` puede producir comportamientos
incorrectos silenciosos.

---

## CRÍTICO-5 — `UserRoleDAO.findRolesByUserId()`: Retorna Strings Concatenados con Coma

**Archivo:** `src/Logic/DAO/UserRoleDAO.java` líneas 87–90

### Evidencia

```java
roleList.add(resultSet.getString("rol") + ","
        + resultSet.getString("estado"));
// Ejemplo: ["Coordinador,Activo", "Profesor,Inactivo"]
```

### Causa raíz

El método mezcla dos datos en un solo `String` en vez de usar un DTO o `Map`.
Cualquier llamador debe hacer `split(",")` para parsear el estado, lo cual es
frágil.

### Impacto

Cualquier código que use `findRolesByUserId()` interpretará los datos
incorrectamente.

\newpage

# Hallazgos Altos

---

## ALTO-1 — `LoginController`: Captura `NullPointerException` Como Control de Flujo

**Archivo:** `src/GUI/Controller/LoginController.java` líneas 99–135

### Evidencia

```java
currentUser = userDao.findByIdentifier(userTextField.getText()); // puede retornar null
currentUser.setRoles(...);  // NullPointerException si currentUser == null
// ...
} catch (NullPointerException e) {
    showAlert("Error de autenticación", "Usuario no encontrado...", AlertType.ERROR);
}
```

### Causa raíz

`findByIdentifier()` retorna `null` si el usuario no existe. En vez de verificar
`if (currentUser == null)`, el código deja explotar el NPE y lo captura.
Viola el estándar ("Prohibido `catch (Exception e)` genérico").

---

## ALTO-2 — `LoginController`: Uso de `catch (Exception e)` Genérico (2 Instancias)

**Archivo:** `src/GUI/Controller/LoginController.java` líneas 79 y 165

```java
} catch (Exception e) {  // openAdministratorRegistration() y openWindow()
    showAlert("Error", "Error al abrir, intente más tarde.", AlertType.ERROR);
}
```

Debería capturar `IOException ioException` específicamente.

---

## ALTO-3 — `ProjectDAO.update()`: Siempre Resetea Estado a `"Disponible"`

**Archivo:** `src/Logic/DAO/ProjectDAO.java` líneas 278–312

### Evidencia

```java
// UPDATE_PROJECT_SQL incluye columna estado
preparedStatement.setString(10, STATUS_AVAILABLE);  // SIEMPRE "Disponible"
// Debería ser: project.getStatus()
```

### Impacto

Un proyecto "Lleno" recupera cupos al ser editado.
Un proyecto "Cancelado" vuelve a estar disponible. Corrupción de datos de estado.

---

## ALTO-4 — `MainMenuController`: Paths FXML con Capitalización Incorrecta (Crash en Linux)

**Archivo:** `src/GUI/Controller/MainMenuController.java` líneas 207, 219, 220

```java
addButton("Manejar Actividades",
    "/GUI/view/GUISelectProjectForActivity.fxml");     // "view" minúscula
addButton("Consultar Organizaciones Vinculadas",
    "/GUI/view/GUIManageLinkedOrganization.fxml");     // "view" minúscula
addButton("Consultar técnicos responsables",
    "/GUI/view/GUIManageTechnicalResponsible.fxml");   // "view" minúscula
```

La carpeta correcta es `GUI/View/` (V mayúscula). En macOS funciona porque el
sistema de archivos es insensible a mayúsculas; en Linux/Docker estas rutas
lanzan `NullPointerException`.

---

## ALTO-5 — `ProjectDAO`: Falta `id_coordinador` en `INSERT_PROJECT_SQL`

**Archivo:** `src/Logic/DAO/ProjectDAO.java` líneas 23–27

```java
"(id_organizacion, id_tecnico, id_profesor, nombre, descripcion, objetivo, " +
"fecha_inicio, fecha_fin, cupo_maximo, cupo_disponible, estado, nrc) "
// id_coordinador AUSENTE
```

Si la columna `id_coordinador` es `NOT NULL` en el esquema real, todos los
intentos de guardar un proyecto nuevo fallarán con `SQLException`.

\newpage

# Hallazgos Medios

## MEDIO-1 — `ValidationUtils`: Longitudes Máximas Inconsistentes con el Estándar

**Archivo:** `src/GUI/Utils/ValidationUtils.java` líneas 175–200

| Tipo | SKILL.md (estándar) | Implementación actual | Estado |
|---|---|---|---|
| `"Name"` | 30 | **100** | INCORRECTO |
| `"Text"` | 255 | **150** | INCORRECTO |
| `"ID"` | 10 | 10 | Correcto |
| `"Email"` | 50 | 50 | Correcto |
| `"Number"` | 8 | 8 | Correcto |
| `"Password"` | 20 | 20 | Correcto |

## MEDIO-2 — `ReportDAO` y `SelfEvaluationDAO`: `deleteBy*` Siempre Retorna `true`

**Archivos:** `src/Logic/DAO/ReportDAO.java` línea 760 ·
`src/Logic/DAO/SelfEvaluationDAO.java` línea 308

```java
int rowsAffected = 0;
// ... executeUpdate() ...
return rowsAffected >= 0;  // 0 >= 0 siempre es TRUE
// Correcto: return rowsAffected > 0;
```

Los llamadores no pueden distinguir si se eliminaron registros o no.

## MEDIO-3 — `ProjectDAO.mapProject()`: Excepciones SQL Silenciadas

**Archivo:** `src/Logic/DAO/ProjectDAO.java` líneas 523–534

```java
try {
    project.setObjetivo(resultSet.getString("objetivo"));
} catch (SQLException ignored) { }
```

Silenciar `SQLException` en columnas opcionales enmascara errores reales de
esquema y produce objetos `Project` con campos `null` sin aviso.

## MEDIO-4 — `Main.java`: Título Incorrecto en la Ventana de Login

**Archivo:** `src/Main.java` línea 27

```java
primaryStage.setTitle("Sistema para Prácticas Profesionales - Registrar Coordinador");
// La pantalla es el Login, no registro de coordinador
```

## MEDIO-5 — `Main.java`: Uso de `exception.printStackTrace()`

**Archivo:** `src/Main.java` línea 32

```java
exception.printStackTrace();  // Prohibido; usar Logger
```

## MEDIO-6 — `InternDAO.findAllCoordinators()`: Nombre Semánticamente Incorrecto

**Archivo:** `src/Logic/DAO/InternDAO.java` líneas 116–138

El método `findAllCoordinators()` en `InternDAO` ejecuta `SELECT_ALL_INTERNS_SQL`
y retorna `List<Intern>`. El nombre sugiere coordinadores pero recupera practicantes.

## MEDIO-7 — `UserRoleDAO.saveUserRole()`: Mensaje de ValidationException Truncado

**Archivo:** `src/Logic/DAO/UserRoleDAO.java` línea 44

```java
throw new ValidationException(
    "El ID del usuario debe ser mayor a cero. ID recibido: ");
    // Falta: + user.getId()
```

## MEDIO-8 — SQL con Nombre de Esquema Hardcodeado `spp.`

**Archivo:** `src/Logic/DAO/ProjectDAO.java` múltiples líneas (35, 43, 44, 51, 77)

```java
"JOIN spp.organizacion_vinculada ov ON ov.id_organizacion = p.id_organizacion"
```

Si el esquema cambia de nombre el sistema falla completamente.

## MEDIO-9 — `MainMenuController`: "Registrar profesor" Duplicado en Menú Coordinador

**Archivo:** `src/GUI/Controller/MainMenuController.java` líneas 213 y 221

El botón "Registrar profesor" aparece dos veces en el menú del coordinador.

\newpage

# Hallazgos Bajos

## BAJO-1 — Typos en Nombres de Archivos FXML

| Archivo actual | Nombre correcto |
|---|---|
| `GUIAddRerport.fxml` | `GUIAddReport.fxml` |
| `GUIAddSerlEvaluation.fxml` | `GUIAddSelfEvaluation.fxml` |

## BAJO-2 — `MainMenuController` Almacena `private User currentUser` (Viola SessionManager)

**Archivo:** `src/GUI/Controller/MainMenuController.java` línea 79

El estándar prohíbe `private User user` en controladores. El usuario ya está en
`SessionManager.getInstance().getUser()`.

## BAJO-3 — `SessionManager`: Lazy Init Sin Sincronización

**Archivo:** `src/GUI/SessionManager/SessionManager.java` líneas 13–17

```java
public static SessionManager getInstance() {
    if(instance == null){         // no sincronizado
        instance = new SessionManager();
    }
    return instance;
}
```

Patrón correcto: holder estático o `synchronized`.

## BAJO-4 — `ValidationUtils.isValidPassword()`: Constante Local Duplicada

**Archivo:** `src/GUI/Utils/ValidationUtils.java` líneas 107, 112

```java
private static final int PASSWORD_MIN_LENGTH = 8;  // constante de clase
// ...
final int MIN_LENGTH = 8;  // constante local duplicada — debería usar PASSWORD_MIN_LENGTH
```

## BAJO-5 — BCrypt Invocado Sin Constante `BCRYPT_WORK_FACTOR`

Múltiples controladores usan `BCrypt.gensalt()` sin el factor de trabajo explícito.
El estándar requiere `BCrypt.gensalt(BCRYPT_WORK_FACTOR)` donde la constante
esté declarada como `private static final`.

## BAJO-6 — Falta Archivo `.env.example`

El estándar requiere un `.env.example` versionado con placeholders.
No existe en el repositorio. Sin él los nuevos desarrolladores no saben qué
variables configurar.

## BAJO-7 — `EvaluationPrerequisiteChecker`: Constantes con Valor Idéntico Duplicadas

**Archivo:** `src/GUI/Utils/EvaluationPrerequisiteChecker.java` líneas 23–24

```java
private static final String STATUS_SUBMITTED = "Entregada";
private static final String STATUS_SELF_EVALUATION_DELIVERED = "Entregada";
// Mismo valor, dos constantes — una sobra
```

## BAJO-8 — `EvaluateReportController`: `STATUS_APPROVED` Declarado Pero No Usado

**Archivo:** `src/GUI/Controller/EvaluateReportController.java` línea 55

```java
private static final String STATUS_APPROVED = "Aprobado";  // código muerto
```

## BAJO-9 — Contraseña de BD Débil en `.env`

El archivo `.env` contiene `DB_PASSWORD=828356`: contraseña numérica de 6 dígitos,
extremadamente débil para un entorno de producción.

\newpage

# Errores Funcionales Confirmados

| # | Error | Archivo:Línea | Flujo que lo provoca | Obtenido | Esperado |
|---|---|---|---|---|---|
| F-1 | `NumberFormatException` al registrar practicante | `AddInternController.java:108` | Campo créditos vacío + click Registrar | Crash silencioso | Mensaje de validación |
| F-2 | Conexión MySQL nunca cerrada | `UserDAO.java:22` | Cualquier login o listado de usuarios | Pool de conexiones agotado | Conexión cerrada al terminar |
| F-3 | 3 opciones de menú no cargan en Linux | `MainMenuController.java:207` | Click en Manejar Actividades / Consultar Orgs / Técnicos | NullPointerException | Vista carga correctamente |
| F-4 | `findByEmail()` retorna usuario vacío | `UserDAO.java:231` | Búsqueda con email inexistente | `User` con `id=0` | `null` |
| F-5 | Actualizar proyecto resetea estado | `ProjectDAO.java:295` | Coordinador edita cualquier campo | Estado cambia a "Disponible" | Estado conservado |

\newpage

# Problemas de Arquitectura

1. **Patrón de conexión inconsistente.** `UserDAO` usa conexión persistente en instancia; todos los demás DAOs usan `try-with-resources` por método. Un solo patrón debe aplicarse en todo el proyecto.

2. **`Logic.Utils.Connection` es obsoleta y peligrosa.** El campo `public static java.sql.Connection connection` es accesible desde cualquier clase del proyecto y puede ser manipulado accidentalmente.

3. **`InternDAO extends UserDAO`.** La herencia para reutilizar `saveUser()` viola el Principio de Responsabilidad Única. `InternDAO` hereda la infraestructura problemática de conexión de `UserDAO`. Preferir composición.

4. **`EvaluationPrerequisiteChecker` en `GUI/Utils/`.** Esta clase hace 5 llamadas a DAOs y contiene lógica de negocio compleja. Pertenece a la capa lógica (`Logic/`), no a utilidades de GUI.

# Problemas de Base de Datos

1. **`proyecto` sin `id_coordinador` en INSERT.** Si la columna es `NOT NULL` en el esquema real, todos los inserts de proyectos fallan.

2. **Nombre de esquema `spp.` hardcodeado en 6+ queries.** Las queries deben funcionar en cualquier esquema configurado.

3. **`reporte_mensual` — columna de unión con nombre diferente.** `SQL_SUM_APPROVED_HOURS` hace `JOIN reporte_mensual rm ON rm.id_reporte_mensual = r.id_reporte`. Solo funciona si los IDs numéricos coinciden por casualidad.

4. **`test_data.sql` usa hashes BCrypt inválidos.** `'$2b$10$hash1'` no es un hash BCrypt real; los tests de login con esos datos fallarán con BCrypt.

# Problemas de Seguridad

1. **Contraseña débil en `.env`:** `DB_PASSWORD=828356` es numérica de 6 dígitos.
2. **`.env.example` faltante:** los nuevos colaboradores no tienen referencia de las variables requeridas.
3. **`Logic.Utils.Connection.connection` campo público:** expone la conexión a toda la aplicación.
4. **Logs de auditoría solo en Logger:** `AuditLog.java` escribe solo en `java.util.logging`. No hay persistencia de auditoría en BD; si los logs rotan, no hay traza.

\newpage

# Plan de Corrección Priorizado

| Prioridad | ID | Cambio | Archivos afectados | Esfuerzo estimado |
|---|---|---|---|---|
| 1 | C-3 | Incluir `isCreditEmpty` en la expresión booleana de `hasEmptyFields()` | `AddInternController.java` | 1 línea |
| 2 | A-1 | Reemplazar catch NPE por `if (currentUser == null)` en `loginProcess()` | `LoginController.java` | 5 líneas |
| 3 | A-3 | Usar `project.getStatus()` en vez de `STATUS_AVAILABLE` en `update()` | `ProjectDAO.java` | 1 línea |
| 4 | A-4 | Corregir paths `/GUI/view/` → `/GUI/View/` (3 ocurrencias) | `MainMenuController.java` | 3 líneas |
| 5 | C-1 | Refactorizar `UserDAO`: eliminar campo `connection`, usar `try-with-resources` en cada método | `UserDAO.java`, `Connection.java` | 30–50 líneas |
| 6 | C-2 | Implementar `validateUser()` con validaciones reales | `UserDAO.java` | 10 líneas |
| 7 | C-4 | Corregir `findByEmail()` para retornar `null` en vez de `new User()` | `UserDAO.java` | 2 líneas |
| 8 | A-2 | Reemplazar `catch(Exception e)` por `catch(IOException ioException)` | `LoginController.java` | 2 lugares |
| 9 | M-2 | Corregir `rowsAffected >= 0` a `rowsAffected > 0` en métodos delete | `ReportDAO.java`, `SelfEvaluationDAO.java` | 2 líneas |
| 10 | M-9 | Eliminar botón "Registrar profesor" duplicado en menú coordinador | `MainMenuController.java` | 1 línea |
| 11 | M-7 | Completar mensaje de ValidationException con `+ user.getId()` | `UserRoleDAO.java` | 1 línea |
| 12 | C-5 | Refactorizar `findRolesByUserId()` para retornar datos correctamente estructurados | `UserRoleDAO.java` | 10 líneas |
| 13 | M-4 | Corregir título de ventana de login en `Main.java` | `Main.java` | 1 línea |
| 14 | M-5 | Reemplazar `exception.printStackTrace()` por Logger en `Main.java` | `Main.java` | 3 líneas |
| 15 | M-8 | Eliminar prefijo `spp.` hardcodeado en queries SQL | `ProjectDAO.java` | 6 líneas |
| 16 | B-6 | Crear `.env.example` con placeholders | Nuevo archivo | 4 líneas |
| 17 | M-1 | Alinear longitudes máximas de `ValidationUtils` con SKILL.md | `ValidationUtils.java` | 2 líneas |

\newpage

# Tabla de Riesgo de Producción

| Componente | Riesgo | Consecuencia si no se corrige |
|---|---|---|
| Fuga de conexiones (C-1) | **ALTO** | Servidor MySQL deja de aceptar conexiones |
| Registro practicante (C-3) | **ALTO** | Crash sin mensaje en flujo crítico |
| Actualizar proyecto (A-3) | **ALTO** | Corrupción de estado de proyectos |
| Paths FXML (A-4) | **ALTO** | 3 opciones de menú rotas en Linux/Docker |
| validateUser vacío (C-2) | **MEDIO** | Datos inválidos persistidos en BD |
| findByEmail bug (C-4) | **MEDIO** | Lógica silenciosamente incorrecta |
| DELETE siempre true (M-2) | **MEDIO** | Fallos de borrado no detectados |
| Esquema hardcodeado (M-8) | **BAJO** | Fallo total al cambiar entorno |

# Conclusión Final

El sistema SGPP tiene una arquitectura bien definida, uso correcto de
`PreparedStatement` (sin inyección SQL), manejo de excepciones de dominio y
BCrypt para contraseñas. El estándar de codificación es riguroso y se sigue
en la mayoría de las clases nuevas.

Sin embargo, existen **5 errores críticos** que deben corregirse antes de
cualquier despliegue: una fuga de conexiones sistemática en `UserDAO`, un bug
funcional confirmado en el registro de practicantes, un método de validación
completamente vacío, un error lógico que corrompe el estado de proyectos al
actualizar, y un contrato de retorno inconsistente en `findByEmail()`.

Las correcciones de mayor impacto son de alcance pequeño (1–5 líneas cada una)
y pueden aplicarse en menos de 2 horas de trabajo.

---

*Reporte generado por Claude Code — Auditoría Técnica Integral*
*Rama analizada: `acop-developer-Rebase` · Fecha: 2026-06-10*
