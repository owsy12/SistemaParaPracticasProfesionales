# SistemaParaPracticasProfesionales

## Configuración del Proyecto

### Requisitos
- Java 21+
- MySQL 8.0+
- IntelliJ IDEA

### Configuración de JavaFX

JavaFX se ha agregado al proyecto y está disponible en `lib/javafx-21/`. El módulo ya está configurado en el archivo `.idea/SistemaParaPracticasProfesionales.iml`.

#### Opciones de compilación:
```
--module-path=/Users/qp/IdeaProjects/SistemaParaPracticasProfesionales/lib/javafx-21
--add-modules=javafx.controls,javafx.fxml,javafx.graphics,javafx.base,javafx.media,javafx.swing
```

### Configuración de la Base de Datos

La conexión a MySQL está configurada en `src/DataAccess/BDConnection.java`:
- **Usuario**: `spp_user`
- **Contraseña**: `30dtv01015k`
- **Host**: `localhost:3306`
- **Base de datos**: `spp`

Asegúrate de que el usuario `spp_user` esté creado en MySQL con los privilegios adecuados.

### Estructura del Proyecto
- `src/` - Código fuente
  - `Main.java` - Punto de entrada
  - `DataAccess/` - Acceso a datos (DAO)
  - `GUI/` - Interfaz gráfica (JavaFX)
  - `Logic/` - Lógica de negocio y pruebas
- `lib/` - Librerías externas
  - `javafx-21/` - Librerías de JavaFX
  - JUnit 4 y 5
  - MySQL Connector

### Ejecución del Proyecto
1. Asegúrate de que MySQL esté corriendo
2. Abre el proyecto en IntelliJ IDEA
3. Compila el proyecto
4. Ejecuta `Main.java`

