# 🔧 Troubleshooting - Solución de Problemas Comunes

## ❌ Problemas Encontrados y Solucionados

### 1. **Error: "No se encontró el archivo FXML"**
   
**Solución**: 
- Asegúrate de que el archivo está en: `src/GUI/View/FXMLExample.fxml`
- El Main.java busca el archivo en esta ruta relativa
- Si obtienes error, verifica la estructura del proyecto

### 2. **Error: "Módulos de JavaFX no encontrados"**

**Causas comunes**:
- No tienes Java 21+ instalado
- Las opciones `--module-path` y `--add-modules` no están configuradas

**Solución**:
1. Verifica tu versión de Java:
   ```bash
   java -version
   ```
2. Debe ser Java 21 o superior

3. En IntelliJ, asegúrate de que:
   - File → Project Structure → Project → SDK = Java 21
   - Las VM options están configuradas correctamente

### 3. **Error: "ClassNotFoundException" para el Controller**

**Solución**:
- Verifica que `GUI.Controller.ExampleController` existe
- El path debe ser correcto en el FXML: `fx:controller="GUI.Controller.ExampleController"`
- Compila el proyecto: Build → Rebuild Project

## ✅ Cambios Realizados

### 1. **Main.java** (CORREGIDO)
- ✅ Ahora es una aplicación JavaFX completa
- ✅ Extiende `Application`
- ✅ Carga el archivo FXML
- ✅ Configura la ventana

### 2. **FXMLExample.fxml** (CORREGIDO)
- ✅ Removido el import incorrecto de `java.awt.ScrollPane`
- ✅ Agregado el controller: `fx:controller="GUI.Controller.ExampleController"`
- ✅ Agregado fx:id al AnchorPane
- ✅ Namespace actualizado a JavaFX 21

### 3. **ExampleController.java** (COMPLETADO)
- ✅ Implementado correctamente
- ✅ Inicialización incluida
- ✅ Variables FXML inyectadas

## 🚀 Pasos para Ejecutar

### Opción 1: Desde IntelliJ (Recomendado)

1. **Recarga el proyecto**:
   ```
   File → Invalidate Caches → Invalidate and Restart
   ```

2. **Compila el proyecto**:
   ```
   Build → Rebuild Project
   ```

3. **Crea/Actualiza Run Configuration**:
   - `Run → Edit Configurations → + → Application`
   - Main class: `Main`
   - VM options: 
     ```
     --module-path=/Users/qp/IdeaProjects/SistemaParaPracticasProfesionales/lib/javafx-21 --add-modules=javafx.controls,javafx.fxml,javafx.graphics,javafx.base,javafx.media,javafx.swing
     ```

4. **Ejecuta**:
   - Click en ▶ (Run)

### Opción 2: Desde Terminal

```bash
cd /Users/qp/IdeaProjects/SistemaParaPracticasProfesionales
./compile.sh
./run.sh
```

## 🔍 Verificación

Después de ejecutar, deberías ver:
1. Una ventana de JavaFX con título "Sistema para Prácticas Profesionales"
2. En la consola: "ExampleController inicializado correctamente"
3. Sin errores de ClassNotFoundException

## 📋 Checklist de Verificación

- [ ] Java 21+ está instalado (`java -version`)
- [ ] Los JARs de JavaFX están en `lib/javafx-21/`
- [ ] El archivo `Main.java` fue actualizado
- [ ] El archivo `FXMLExample.fxml` no tiene imports de `java.awt`
- [ ] El `ExampleController.java` tiene la anotación `@FXML`
- [ ] Las VM options están configuradas en Run Configuration
- [ ] El proyecto fue compilado (Build → Rebuild Project)
- [ ] El caché fue invalidado (File → Invalidate Caches)

## ⚠️ Errores Específicos

### "Cannot find symbol: class Application"
- Solución: Asegúrate de que tienes JavaFX agregado a las librerías del proyecto
- File → Project Structure → Modules → Dependencies → Verifica que "JavaFX" está listado

### "FXMLLoadException: javafx.fxml.FXMLLoader$ConstructorNotFoundException"
- Solución: El nombre del controller en el FXML debe coincidir exactamente con el package y nombre de la clase
- Verifica: `fx:controller="GUI.Controller.ExampleController"`

### "Cannot read field ... in class ... (NullPointerException)"
- Solución: Los campos anotados con @FXML deben tener los mismos nombres que los fx:id en el FXML
- Verifica que el fx:id coincide con el nombre del campo

## 📞 Si aún hay problemas

1. Verifica que todos los archivos fueron actualizados:
   ```bash
   cat /Users/qp/IdeaProjects/SistemaParaPracticasProfesionales/src/Main.java
   cat /Users/qp/IdeaProjects/SistemaParaPracticasProfesionales/src/GUI/View/FXMLExample.fxml
   ```

2. Limpia el proyecto:
   ```bash
   File → Project Structure → Clean
   Build → Clean Project
   Build → Rebuild Project
   ```

3. Verifica que los paths son correctos:
   ```bash
   ls -la /Users/qp/IdeaProjects/SistemaParaPracticasProfesionales/lib/javafx-21/
   ls -la /Users/qp/IdeaProjects/SistemaParaPracticasProfesionales/src/GUI/View/
   ```

---

¡Si sigues estos pasos, tu aplicación JavaFX debería funcionar correctamente! 🎉

