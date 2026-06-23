package GUI.DocumentGeneration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DocumentManagement {

    private static final Logger LOGGER = Logger.getLogger(DocumentManagement.class.getName());

    private DocumentManagement() {
    }

    public static void saveFile(File sourceFile, String relativeFolder, String newFileName) {

        try {
            Path folderPath = Paths.get(relativeFolder);

            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }

            Path destinationPath = folderPath.resolve(newFileName + ".pdf");
            Files.copy(sourceFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException ioException) {
            LOGGER.log(Level.SEVERE, "Error al guardar el documento {0} en {1}: {2}",
                    new Object[]{newFileName, relativeFolder, ioException.getMessage()});
        }
    }
}
