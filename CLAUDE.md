# CLAUDE.md — Sistema de Prácticas Profesionales

Este repositorio es el **Sistema de Prácticas Profesionales** de la Facultad de Estadística e Informática (Universidad Veracruzana), construido en Java + JavaFX + MySQL. Todo el trabajo —lectura, edición, creación, refactor, revisión o explicación de archivos— se rige por un **estándar de codificación obligatorio** publicado como Skill del proyecto.

## Regla nº1 — Usa la Skill `sistema-practicas-profesionales` SIEMPRE

**Antes** de leer, crear, editar, refactorizar, revisar o explicar **cualquier** archivo de este repositorio (`.java`, `.fxml`, `.css`, `.sql`, `.env`, rutas de paquetes, nombres de carpetas, mensajes de commit, scripts), invoca la Skill:

```
Skill: sistema-practicas-profesionales
```

La Skill vive en [.claude/skills/sistema-practicas-profesionales/SKILL.md](.claude/skills/sistema-practicas-profesionales/SKILL.md) y es la **única fuente de verdad** para:

- Convenciones de nombrado (PascalCase / camelCase / UPPER_SNAKE_CASE / snake_case) e idioma (código en inglés, UI en español).
- Estructura `GUI/View/` (FXML con prefijo `GUI`, CSS con sufijo `Styles`, imágenes en `images/`).
- Política de **cero comentarios**, máximo **3 parámetros** por método, **único `return`** final, prohibición de números/strings mágicos, espaciado, Logger por clase.
- `if/else` con llaves K&R y `else` explícito; `for-each` vs `while` vs `do-while`; ternarios solo simples.
- Seguridad: **BCrypt** para contraseñas, prohibición de `root` en MySQL, `.env` con java-dotenv, `DataBaseConnection.connectDatabase()` dentro de `try-with-resources`.
- Controladores JavaFX: responsabilidades, prohibiciones, `ValidationUtils.setTypeAndLength`, `SessionManager` singleton, CSS dedicado por vista y selectores camelCase.
- Excepciones: jerarquía `ValidationException` / `DuplicateEntryException` / `ServiceException`; prohibición de `catch (Exception e)`.
- Git: una rama por desarrollador, `main` solo vía PR, commits en inglés imperativo.
- Formato: sin alineación visual, 120–150 caracteres por línea, salto **después** del `.` en cadenas de llamadas.
- Storage: `storage/intern_{matricula}/project_{idProyecto}/{categoria}/`.

> **Si el código existente contradice la Skill, el código está equivocado.** No infieras convenciones del repo; sigue la Skill literalmente y corrige lo que toques.

## Cómo aplicar la Skill en cada turno

1. **Apertura del turno**: invoca `Skill: sistema-practicas-profesionales` antes de cualquier `Read`, `Edit`, `Write`, `Bash` que toque código del proyecto.
2. **Durante el trabajo**: consulta las secciones de la Skill aplicables al archivo concreto (DAO, controlador, FXML, CSS, `.env`, commit, etc.).
3. **Cierre del turno**: ejecuta la **lista de verificación final** que vive al pie de `SKILL.md`. Si algún ítem falla, corrige antes de declarar la tarea terminada.

## Comunicación con el usuario

Responde en **español** (idioma del usuario y del documento normativo). Los identificadores y mensajes de Logger que aparezcan en tu respuesta van en **inglés**; los textos de UI y los mensajes de error mostrados al practicante van en **español**.

## Estructura mínima del repositorio (referencia)

```
src/
  Main.java
  DataAccess/DataBaseConnection.java
  GUI/
    Controller/        # <Accion><Entidad>Controller
    DocumentGeneration/
    SessionManager/    # singleton (gui.sessionmanager)
    Utils/             # ValidationUtils, RestrictedTextField, etc.
    View/
      GUI*.fxml        # vistas con prefijo GUI
      css/*Styles.css  # una hoja por vista
      images/
  Logic/
    DAO/
    DTOs/
    Exceptions/        # ValidationException, DuplicateEntryException, ServiceException
    Interface/         # I<NombreEntidad>DAO
    Utils/
documents/             # snake_case (partial_reports, final_reports, ...)
storage/               # intern_{matricula}/project_{idProyecto}/{categoria}/
.env                   # NO commitear
.env.example           # SÍ commitear
```

## Recordatorio operativo

- No crees comentarios en código fuente bajo ninguna circunstancia (regla 2.1 de la Skill).
- No introduzcas `catch (Exception e)`, ni `e.printStackTrace()` como única acción (regla 6.1).
- No uses `root` ni credenciales hardcodeadas (reglas 4.2 / 4.3).
- No abras conexiones JDBC fuera de `DataBaseConnection.connectDatabase()` ni fuera de `try-with-resources` (regla 4.4).
- No instancies `SessionManager` con `new` (regla 5.3).
- No agregues `style="..."` inline en FXML (regla 5.4).

Cualquier excepción a estas reglas requiere autorización explícita del usuario para esa tarea concreta. Sin autorización explícita, **la Skill manda**.
