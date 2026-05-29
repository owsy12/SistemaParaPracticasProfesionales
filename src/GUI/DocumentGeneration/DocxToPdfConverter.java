package GUI.DocumentGeneration;

import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DocxToPdfConverter {

    private static final Logger LOGGER = Logger.getLogger(DocxToPdfConverter.class.getName());

    private DocxToPdfConverter() {
    }

    public static byte[] convert(byte[] docxBytes) throws IOException {
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(docxBytes));
             ByteArrayOutputStream pdfOutput = new ByteArrayOutputStream()) {

            PdfOptions pdfOptions = PdfOptions.create();
            pdfOptions.fontProvider(new CompatibleFontProvider());
            PdfConverter.getInstance().convert(document, pdfOutput, pdfOptions);
            return pdfOutput.toByteArray();

        } catch (IOException ioException) {
            LOGGER.log(Level.SEVERE, "Error al convertir DOCX a PDF: {0}",
                    ioException.getMessage());
            throw ioException;
        }
    }
}
