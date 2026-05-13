package GUI.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class DocumentManngemt {
    public static void saveFile(File sourceFile, String relativeFolder, String newFileName) {

        try {
            Path folderPath = Paths.get(relativeFolder);

            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }

            Path destinationPath = folderPath.resolve(newFileName + ".pdf");
            Files.copy(sourceFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {

        }
    }
}
