---
name: sistema-practicas-profesionales
description: Estándar de codificación obligatorio del Sistema de Prácticas Profesionales (UV, FEI, Principios de Construcción de Software). DEBE invocarse antes de leer, crear, editar, revisar, refactorizar o explicar CUALQUIER archivo del repositorio — incluye .java, .fxml, .css, .sql, .env, rutas de paquetes, mensajes de commit y nombres de carpetas. Cubre: idioma del código (inglés) e idioma de UI (español), nombrado (PascalCase, camelCase, UPPER_SNAKE_CASE, snake_case para datos), GUI/View y prefijo GUI en FXML, política de cero comentarios, máximo 3 parámetros por método, único return final, prohibición de números/strings mágicos, espaciado, Logger por clase, if/else con llaves K&R y else explícito, for-each vs while vs do-while, ternarios simples, BCrypt para contraseñas, prohibición de root en MySQL, .env con dotenv, DataBaseConnection y try-with-resources, controladores JavaFX (responsabilidades y prohibiciones), ValidationUtils.setTypeAndLength, SessionManager singleton, CSS dedicado por vista y selectores camelCase, jerarquía ValidationException/DuplicateEntryException/ServiceException, prohibición de catch(Exception), ramas por desarrollador y commits en inglés imperativo, formato (alineación, longitud de línea 120–150, saltos de línea), ruta storage/intern_{matricula}/project_{idProyecto}/{categoria}/. Activar también ante frases como "agrega", "modifica", "refactoriza", "crea controlador", "nuevo DAO", "nueva vista FXML", "guardar contraseña", "consultar BD", "subir documento", "hacer commit".
---

# Estándar de Codificación — Sistema de Prácticas Profesionales

Fuente normativa: `Estandar_Codificacion.docx` (UV, Facultad de Estadística e Informática, Principios de Construcción de Software). Esta Skill es la **única fuente de verdad** para el estilo y arquitectura de este repositorio. **No** infieras convenciones del código existente: si el código contradice esta Skill, el código está equivocado y debe corregirse.

## Cómo usar esta Skill

1. **Antes de tocar cualquier archivo**: identifica la sección o secciones aplicables (nombrado, controlador, DAO, vista, CSS, .env, commit, etc.) y léelas íntegras. No improvises desde memoria.
2. **Mientras escribes/editas**: aplica cada regla literalmente. Si una regla entra en conflicto con código preexistente, prefiere ajustar el código nuevo a la regla y, si el cambio cabe en el alcance, corrige también el código viejo afectado.
3. **Antes de cerrar el turno**: ejecuta la **lista de verificación final** al pie de este archivo. Si algún ítem falla, vuelve y corrige antes de declarar la tarea terminada.
4. **Lenguaje de comunicación con el usuario**: responde en español (es la lengua del usuario y del documento normativo).

---

## 1. Convenciones de Nombrado

### 1.1 Idioma

- **Lógica interna (clases, interfaces, métodos, variables, constantes, parámetros, paquetes, selectores CSS, archivos de vista) → inglés**, sin excepción.
- **Cadenas visibles en la UI (títulos de alertas, mensajes, etiquetas) → español**.

| Correcto | Incorrecto |
|---|---|
| `int accumulatedHours = 0;` | `int horasAcumuladas = 0;` |
| `boolean isValidInput = false;` | `boolean entradaValida = false;` |
| `List<Activity> projectActivities;` | `List<Actividad> actividadesProyecto;` |
| `showAlert("Error de validación", "El campo no puede estar vacío.", AlertType.WARNING);` | `showAlert("Validation Error", "Field cannot be empty.", AlertType.WARNING);` |
| `.loginCardVBox { ... }` | `.tarjetaLoginVBox { ... }` |
| `GUIAddCoordinator.fxml` | `GUIAddCoordinador.fxml` |

### 1.2 Tabla maestra de convenciones

| Elemento | Convención | Ejemplo |
|---|---|---|
| Clase concreta | PascalCase — sustantivo | `ActivityDAO`, `GenerateReportController` |
| Interfaz | `I` + PascalCase — sustantivo | `IActivityDAO`, `IReportDAO`, `IProjectDAO` |
| Método | camelCase — verbo en infinitivo | `findByProject()`, `buildReport()`, `save()` |
| Variable de instancia | camelCase — sustantivo descriptivo | `currentIntern`, `projectActivities` |
| Parámetro | camelCase — sustantivo descriptivo | `idProject`, `reportType`, `targetActivity` |
| Constante | UPPER_SNAKE_CASE | `PARTIAL_REPORT_MIN_HOURS`, `MAX_RETRIES` |
| Campo `@FXML` | propósito + TipoWidget (camelCase) | `nameTextField`, `saveButton`, `statusLabel` |
| ID de entidad (DAO) | `id` + NombreEntidad (camelCase) | `idProject`, `idIntern`, `idOrganization` |
| Paquete (general) | minúsculas sin separadores | `logic.dao`, `gui.controller`, `dataaccess` |
| Paquete (datos) | snake_case obligatorio | `user_reports`, `user_documents` |
| Controller | `<Accion><Entidad>Controller` | `AddActivityController`, `LoginController` |
| DTO | NombreEntidad (PascalCase, sin sufijo) | `Activity`, `PartialReport`, `Intern` |
| Selector CSS | propósito + TipoNodo (camelCase) | `loginCardVBox`, `saveButton`, `statusLabel` |
| Archivo FXML | `GUI` + PascalCase | `GUILogin.fxml`, `GUIAddCoordinator.fxml` |
| Archivo CSS | camelCase + `Styles` | `loginStyles.css`, `addActivityStyles.css` |

### 1.3 Paquetes de datos de usuario — snake_case

Todo directorio destinado al almacenamiento o exportación de datos generados por el usuario en tiempo de ejecución va en **snake_case**.

Correcto: `documents/partial_reports/`, `documents/final_reports/`, `documents/self_evaluations/`, `documents/initial_formats/`, `documents/signed_documents/`.
Incorrecto: `documents/partialReports/`, `documents/FinalReports/`, `documents/signedDocuments/`.

### 1.4 Estructura `GUI/View`

- FXML en `GUI/View/` con prefijo `GUI` (`GUILogin.fxml`, `GUIAddActivity.fxml`).
- CSS en `GUI/View/css/` con sufijo `Styles` (`loginStyles.css`, `addActivityStyles.css`).
- Imágenes en `GUI/View/images/` (`logo_uv.png`).

---

## 2. Directrices Mandatorias de Diseño de Código

### 2.1 Política de Cero Comentarios

**Prohibido** `//`, `/* */` y Javadoc en el cuerpo de métodos. Si el código necesita explicación, renombra o extrae método. La legibilidad es responsabilidad del nombrado.

### 2.2 Máximo 3 parámetros por método

Si un método requiere más de tres argumentos, **extrae un DTO o un objeto contexto**. No flexibilizar la regla con `varargs` ni con builders ad-hoc.

```java
// Correcto
public int save(Activity activity) throws ServiceException, ValidationException { ... }

// Incorrecto
public void registerUser(String firstName, String lastName, String email,
                         String password, String role, int departmentId) { ... }
```

### 2.3 Un solo punto de salida

Cada método tiene **exactamente un `return`**, ubicado como última instrucción. **Prohibidos los early returns**. La lógica condicional se estructura con una variable de resultado y bloques `if/else` que convergen.

```java
// Correcto
public boolean isValidEmail(String email) {
    boolean isValid = false;
    if (email != null && !email.isBlank()) {
        isValid = EMAIL_PATTERN.matcher(email).matches();
    }
    return isValid;
}
```

En handlers `@FXML` (que son `void`), la regla se traduce en **no usar `return;` para abortar**; se estructura con `if/else` y el flujo cae al final del método de forma natural.

### 2.4 Prohibición de números y strings mágicos

Todo valor literal con significado de negocio se declara como `private static final` con nombre revelador.

```java
private static final int PARTIAL_REPORT_MIN_HOURS = 210;
private static final int FINAL_REPORT_EXACT_HOURS = 420;
private static final int MAX_LOGIN_ATTEMPTS = 3;
private static final String STATUS_GENERATED = "Generado";
private static final String STATUS_SUBMITTED = "Entregado";
```

### 2.5 Espaciado

- **Horizontal**: un espacio alrededor de `=`, `+`, `-`, `*`, `==`, `>`, `<`, `&&`, `||`. Un espacio después de cada coma.
- **Vertical**: **una sola** línea en blanco entre métodos. Dentro de un método, una línea en blanco puede separar bloques lógicos. **Prohibidas** las líneas en blanco múltiples consecutivas.

### 2.6 Logger

Toda clase que toque la capa de datos o gestione errores de infraestructura declara, **como primera constante**:

```java
private static final Logger LOGGER = Logger.getLogger(ActivityDAO.class.getName());
```

- Una instancia **por clase**, `private static final`.
- Mensajes del Logger **en inglés** (a diferencia de los mensajes de UI).
- `Level.SEVERE` → errores de infraestructura que lanzan `ServiceException`.
- `Level.WARNING` → violaciones de negocio (`DuplicateEntryException`, `ValidationException`).
- **Prohibido** loggear contraseñas, tokens, ni datos sensibles.

```java
LOGGER.log(Level.SEVERE,
    "Error saving activity with ID {0}: {1}",
    new Object[]{ activity.getIdActivity(), sqlException.getMessage() });
```

---

## 3. Estructuras de Control

### 3.1 `if` / `else`

- `{}` **obligatorias** en todo `if`/`else`, sin excepción.
- Estilo K&R: llave de apertura en la misma línea que la declaración.
- `else` y `else if` en la misma línea que la `}` de cierre del bloque previo.
- Todo condicional con camino alternativo significativo incluye su `else` **explícito**.

### 3.2 Bucles

- **`for-each`** para colecciones de tamaño conocido al inicio:
  ```java
  for (Activity activity : projectActivities) {
      activityDAO.linkToReport(activity.getIdActivity(), idReport);
  }
  ```
  No usar `for` indexado con `i++` cuando un for-each basta.

- **`while`** cuando la condición depende de un estado dinámico (`ResultSet`, stream, retry):
  ```java
  while (resultSet.next()) {
      activities.add(mapResultSet(resultSet));
  }
  ```
  **Prohibido** `while (true)` con `break` para simular esto.

- **`do-while`** solo cuando la primera ejecución del cuerpo está **garantizada** con independencia de la condición (típicamente reintentos). Fuera de ese caso, está prohibido.

### 3.3 Ternarios

Permitido **solo** para asignación de valor simple donde condición, valor verdadero y valor falso caben legiblemente en una sola declaración. **Prohibidos** los ternarios anidados y los ternarios dentro de argumentos múltiples.

```java
// Correcto
String displayStatus = isActive ? STATUS_ACTIVE_LABEL : STATUS_INACTIVE_LABEL;

// Incorrecto
String label = (age >= ADULT_AGE)
    ? (hasIdDocument ? "Adulto verificado" : "Adulto sin ID")
    : "Menor de edad";
```

---

## 4. Seguridad

### 4.1 Contraseñas

**Bajo ninguna circunstancia** una contraseña se manipula, transporta, almacena, loggea o compara en texto plano. Toda contraseña pasa por **BCrypt con sal aleatoria** antes de cualquier operación.

```java
String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(BCRYPT_WORK_FACTOR));
```

### 4.2 Acceso a BD — menor privilegio

**Prohibido** usar `root` de MySQL en el código fuente. Crear y usar `spp_user` con permisos limitados (`GRANT SELECT, INSERT, UPDATE, DELETE ON spp.* TO 'spp_user'@'localhost';`).

### 4.3 Variables de entorno — `.env`

Todas las credenciales, URLs de conexión y parámetros sensibles residen en un `.env` en la raíz, leído con **java-dotenv**. Nombres de variables en **UPPER_SNAKE_CASE**.

```
DB_URL=jdbc:mysql://localhost:3306/spp
DB_USER=spp_user
DB_PASSWORD=<variable_de_entorno_segura>
DB_DRIVER=com.mysql.cj.jdbc.Driver
```

- `.env` **debe** estar en `.gitignore`.
- Debe existir `.env.example` versionado con placeholders.
- **Prohibidos** valores hardcodeados (`"root"`, `"admin123"`, etc.) en código.

### 4.4 `DataBaseConnection`

La **única vía** para obtener conexión es `DataBaseConnection.connectDatabase()`. Toda conexión se abre dentro de **`try-with-resources`**. Las clases de utilidad exponen solo métodos públicos, **nunca campos públicos** de conexión.

```java
try (Connection connection = DataBaseConnection.connectDatabase();
     PreparedStatement stmt = connection.prepareStatement(SQL_INSERT)) {
    stmt.setString(1, intern.getMatricula());
    stmt.executeUpdate();
} catch (SQLException sqlException) {
    throw new ServiceException("Error saving intern.", sqlException);
}
```

---

## 5. Controladores JavaFX

### 5.1 Responsabilidades vs prohibiciones

**El Controller debe:**
- Inicializar componentes en `initialize()`.
- Aplicar validación de campos de texto en la UI (vía `ValidationUtils`).
- Responder eventos del usuario (`@FXML`).
- Delegar operaciones de datos a los DAOs.
- Construir DTOs.
- Coordinar navegación entre vistas.
- Capturar excepciones y presentarlas al usuario con `showAlert`.

**El Controller NO debe (prohibiciones absolutas):**
- Escribir SQL.
- Abrir/cerrar/gestionar conexiones JDBC.
- Usar `PreparedStatement` directamente.
- Contener lógica de negocio compleja.
- Usar `catch (Exception e)` genérico.
- Hacer retornos anticipados.
- Embeber números o strings mágicos.

### 5.2 `ValidationUtils.setTypeAndLength`

La validación de entradas se hace **exclusivamente** mediante `ValidationUtils.setTypeAndLength(RestrictedTextField, String)`. **Prohibido** usar `TextFormatter`, `UnaryOperator` o listeners para validar caracteres/longitud.

| Tipo | Caracteres permitidos / Longitud máxima |
|---|---|
| `"ID"` | Alfanumérico (a-z, A-Z, 0-9). Máx 10. |
| `"Name"` | Letras con acentos, ñ y espacios. Máx 30. |
| `"Email"` | Caracteres válidos de correo (`@ . _ % + -`). Máx 50. |
| `"Text"` | Alfanumérico con acentos, ñ y espacios. Máx 255. |
| `"Number"` | Solo dígitos 0-9. Máx 8. |
| `"Password"` | Letras, dígitos y símbolos `@#$%^&*!?_-`. Máx 20. |

```java
@FXML private void initialize() {
    ValidationUtils.setTypeAndLength(matriculaTextField, "ID");
    ValidationUtils.setTypeAndLength(firstNameTextField, "Name");
    ValidationUtils.setTypeAndLength(emailTextField, "Email");
    ValidationUtils.setTypeAndLength(creditsTextField, "Number");
    ValidationUtils.setTypeAndLength(passwordField, "Password");
}
```

### 5.3 `SessionManager`

Singleton, ubicado en `gui.sessionmanager`. Único acceso: `SessionManager.getInstance()`. **Prohibido** `new SessionManager()`. **Prohibido** mantener `private User user;` en controladores para esquivar el singleton.

```java
SessionManager.getInstance().login(user);
User currentUser = SessionManager.getInstance().getUser();
SessionManager.getInstance().logout();
boolean hasSession = SessionManager.getInstance().getUser() != null;
```

### 5.4 CSS para JavaFX

- Una hoja **dedicada por vista FXML** en `GUI/View/css/`, con patrón `<nombreVista>Styles.css`.
- Enlace desde FXML por `stylesheets="@css/loginStyles.css"` en el nodo raíz.
- Selectores camelCase + sufijo de tipo de nodo JavaFX, **en inglés**: `.mainBackgroundPane`, `.loginCardVBox`, `.systemBannerHBox`, `.welcomeTitleLabel`, `.fieldContainerHBox`, `.inputTextField`, `.passwordInputField`, `.loginButton`, `.saveButton`, `.pageFooterVBox`.
- **Prohibido** `style="..."` inline en FXML.
- `setStyle()` en el Controller se reserva **exclusivamente** para estilos dinámicos que dependen de valores calculados en tiempo de ejecución.

---

## 6. Gestión de Excepciones

### 6.1 Jerarquía de dominio

| Tipo | Condición de uso | Ejemplo |
|---|---|---|
| `ValidationException` | Datos de entrada inválidos en DAO | `id ≤ 0`, campo nulo, matrícula en blanco |
| `DuplicateEntryException` | Violación de restricción UNIQUE | Matrícula duplicada, NRC repetido |
| `ServiceException` | Error de infraestructura o BD | `SQLException` inesperada, timeout |

- **Prohibido** `catch (Exception e)`. Cada excepción se captura por su tipo específico.
- **Prohibido** `e.printStackTrace()` como única acción.
- Cada excepción capturada debe traducirse a un `showAlert` con título/mensaje en español y `AlertType` correcto (`ERROR` para validación e infraestructura; `WARNING` para duplicado).

### 6.2 `Exception` e `IOException` permitidos en I/O

`Exception` e `IOException` son válidos solo en escenarios genuinos de E/S o carga de recursos (`FXMLLoader.load()`, lectura/escritura de archivos, recursos del classpath). **No** envolver una operación intrínseca de E/S en una excepción de dominio.

```java
try {
    Parent view = loader.load();
    contentPane.getChildren().setAll(view);
} catch (IOException ioException) {
    showAlert("Error", "No se pudo cargar la vista.", AlertType.ERROR);
}
```

---

## 7. Control de Versiones — GitHub

### 7.1 Estrategia de ramas

- **Una rama por desarrollador** (`aocp-developer`, `jbh-developer`, etc.). Cada integrante concentra su trabajo en su rama personal.
- **`main`**: solo recibe integraciones vía **Pull Request** revisado. **Nadie** trabaja directamente sobre `main`.
- **No** se crean ramas por funcionalidad ni por corrección.

### 7.2 Mensajes de commit

En **inglés**, **tiempo imperativo**, concisos, específicos, libres de referencias a estados del proceso de desarrollo.

| Correcto | Incorrecto |
|---|---|
| `Add activity validation in ActivityDAO` | `arreglos` |
| `Fix NullPointerException in AssignmentDAO` | `subiendo codigo` |
| `Create generate report controller and view` | `update` |
| `Refactor ProjectDAO to use try-with-resources` | `ya por fin quedo la vista v2 final` |
| `Add loginStyles.css for login view` | `cambios varios en el modulo de reportes` |
| `Rename CSS selectors to English camelCase` | |

---

## 8. Formato y Estilo

### 8.1 Sin alineación visual por espacios

Cada token se separa por **un solo espacio**. No se agregan espacios para alinear visualmente asignaciones, declaraciones o llamadas.

### 8.2 Longitud de línea

Recomendado **120–150 caracteres**. La división nunca es obligatoria por la sola presencia de operadores.

### 8.3 Saltos de línea en expresiones

- No dividir una expresión solo porque contenga `||`, `&&`, `+`, `?`, `:` o `.`.
- Dividir cuando la línea supere la longitud recomendada **o** cuando la cantidad de condiciones afecte la lectura.
- Al dividir una condición booleana extensa, los operadores `&&`/`||` van **al inicio** de la línea siguiente.

```java
boolean isValidProject = project != null
    && project.getStartDate() != null
    && project.getEndDate() != null
    && project.getAvailableSlots() > 0
    && "Disponible".equals(project.getStatus());
```

### 8.4 Cadenas de llamadas (`.`)

El salto de línea va **después** del `.`, nunca antes. La primera llamada permanece en la línea inicial.

```java
// Correcto
projectTableView.getSelectionModel()
    .selectedItemProperty()
    .getValue();
```

---

## 9. Almacenamiento de Documentos de Usuario

Todo documento asociado a practicante y proyecto se guarda bajo:

```
storage/intern_{matricula}/project_{idProyecto}/{categoria}/
```

Categorías permitidas: `initial_formats`, `reports`, `self_evaluation`, `ov_evaluation`.

La ruta resultante se persiste en BD; la visualización y descarga la leen desde ahí.

```java
String folder = "storage/intern_" + internMatricula
    + "/project_" + projectId + "/ov_evaluation";
```

**Prohibido** inventar carpetas planas (`storage/evaluacion_ov`) o mezclar idiomas en la ruta.

---

## Lista de verificación final (obligatoria antes de cerrar el turno)

Recorre esta lista **explícitamente** antes de declarar terminada cualquier tarea de código en este repo. Si algo falla, corrige antes de responder al usuario.

**Nombrado e idioma**
- [ ] Identificadores Java/FXML/CSS en inglés; cadenas visibles al usuario en español.
- [ ] Clases PascalCase; interfaces con prefijo `I`; métodos camelCase verbo-infinitivo; constantes UPPER_SNAKE_CASE.
- [ ] Controllers con patrón `<Accion><Entidad>Controller`; DTOs sin sufijo.
- [ ] FXML con prefijo `GUI` en `GUI/View/`; CSS con sufijo `Styles` en `GUI/View/css/`.
- [ ] Paquetes generales en minúsculas; paquetes de **datos de usuario** en snake_case.

**Diseño de código**
- [ ] Cero comentarios (`//`, `/* */`, Javadoc en cuerpo de método).
- [ ] Ningún método con más de 3 parámetros.
- [ ] Cada método tiene un único `return` al final; ningún early return.
- [ ] Sin números ni strings mágicos — todos los literales de negocio en `private static final`.
- [ ] Una sola línea en blanco entre métodos; sin líneas en blanco múltiples.
- [ ] Logger declarado como primera constante en clases de datos/infraestructura; mensajes en inglés; niveles SEVERE/WARNING correctos; sin datos sensibles.

**Control de flujo**
- [ ] `{}` en todo `if`/`else`, estilo K&R; `else` explícito cuando aplique.
- [ ] `for-each` para colecciones; `while` para condición dinámica; `do-while` solo en retry.
- [ ] Ternarios solo para asignación simple, sin anidación.

**Seguridad**
- [ ] Contraseñas siempre vía `BCrypt.hashpw` + `BCrypt.gensalt(BCRYPT_WORK_FACTOR)`; jamás en texto plano (ni en logs).
- [ ] Ningún uso de `root`; credenciales en `.env` con `UPPER_SNAKE_CASE`.
- [ ] `.env` ignorado por Git; `.env.example` versionado.
- [ ] Conexiones solo vía `DataBaseConnection.connectDatabase()` dentro de `try-with-resources`.

**Controladores JavaFX**
- [ ] El controller no escribe SQL, no abre conexiones, no usa `PreparedStatement`, no contiene lógica de negocio compleja.
- [ ] Validación de entrada **solo** vía `ValidationUtils.setTypeAndLength(...)` con tipo correcto (`ID`/`Name`/`Email`/`Text`/`Number`/`Password`).
- [ ] Sesión solo vía `SessionManager.getInstance()`; nada de `new SessionManager()` ni cachés locales de `User`.
- [ ] FXML enlaza CSS dedicado con `stylesheets="@css/<vista>Styles.css"`; cero `style="..."` inline; `setStyle()` solo para dinámicos.

**Excepciones**
- [ ] Capturas por tipo específico (`ValidationException`, `DuplicateEntryException`, `ServiceException`). Sin `catch (Exception e)`.
- [ ] Cada captura traducida a `showAlert` en español con `AlertType` adecuado.
- [ ] `IOException`/`Exception` solo en E/S genuina (FXMLLoader, archivos, recursos).

**Git**
- [ ] Trabajo en la rama del desarrollador; `main` solo vía PR.
- [ ] Commits en inglés imperativo, específicos.

**Formato**
- [ ] Sin espacios de alineación visual.
- [ ] Líneas 120–150 cuando es razonable; división justificada solo por longitud/complejidad.
- [ ] En cadenas de llamadas, salto **después** del `.`.

**Storage**
- [ ] Ruta `storage/intern_{matricula}/project_{idProyecto}/{categoria}/` con categoría válida.

Si la tarea tocó código que **no** cumplía la regla **antes** del cambio y el alcance lo permite, corrígelo. Si el alcance no lo permite, repórtalo al usuario al cerrar el turno.
