package GUI.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimplePdfWriter {

    private static final Logger LOGGER = Logger.getLogger(SimplePdfWriter.class.getName());

    private static final float PAGE_W = 612f;
    private static final float PAGE_H = 792f;
    private static final float MARGIN = 54f;
    private static final float CONTENT_W = PAGE_W - 2 * MARGIN;
    private static final float LINE_H = 14f;
    private static final float FONT_BODY = 10f;
    private static final float FONT_HEAD = 13f;
    private static final float FONT_SECT = 11f;

    private static final Charset WIN1252 = Charset.forName("Cp1252");

    private final List<String> pageContents = new ArrayList<>();
    private StringBuilder curStream;
    private float cursorY;

    public SimplePdfWriter() {
        startNewPage();
    }

    public void title(String text) {
        checkBreak(FONT_HEAD + LINE_H);
        appendText(MARGIN, cursorY, text, "F2", FONT_HEAD);
        cursorY -= (FONT_HEAD + LINE_H);
        hLine(MARGIN, cursorY + LINE_H / 2, MARGIN + CONTENT_W);
        cursorY -= 4;
    }

    public void section(String text) {
        checkBreak(FONT_SECT + LINE_H + 4);
        cursorY -= 4;
        appendText(MARGIN, cursorY, text, "F2", FONT_SECT);
        cursorY -= (FONT_SECT + 4);
    }

    public void field(String label, String value) {
        checkBreak(FONT_BODY + LINE_H);
        String safe = value != null ? value : "";
        appendText(MARGIN,        cursorY, label + ": ", "F2", FONT_BODY);
        appendText(MARGIN + 130f, cursorY, safe,         "F1", FONT_BODY);
        cursorY -= (FONT_BODY + LINE_H);
    }

    public void paragraph(String text) {
        if (text != null && !text.isBlank()) {
            checkBreak(FONT_BODY + LINE_H);
            appendText(MARGIN, cursorY, text, "F1", FONT_BODY);
            cursorY -= (FONT_BODY + LINE_H);
        }
    }

    public void spacer() {
        cursorY -= LINE_H;
    }

    public void table(String[] headers, String[][] rows) {
        if (headers != null && headers.length > 0) {
            renderTable(headers, rows);
        }
    }

    private void renderTable(String[] headers, String[][] rows) {

        float colW = CONTENT_W / headers.length;
        float rowH = FONT_BODY + 8f;

        checkBreak(rowH + (rows != null ? rows.length * rowH : 0));
        float x = MARGIN;

        for (String header : headers) {
            appendText(x + 3, cursorY - 3, header, "F2", FONT_BODY);
            x += colW;
        }
        cursorY -= rowH;
        hLine(MARGIN, cursorY + rowH - 2, MARGIN + CONTENT_W);

        if (rows != null) {
            for (String[] row : rows) {
                checkBreak(rowH);
                x = MARGIN;
                for (int col = 0; col < headers.length; col++) {
                    String cell = (row != null && col < row.length && row[col] != null)
                            ? row[col] : "";
                    appendText(x + 3, cursorY - 3, cell, "F1", FONT_BODY);
                    x += colW;
                }
                cursorY -= rowH;
                hLine(MARGIN, cursorY + rowH - 2, MARGIN + CONTENT_W);
            }
        }
    }

    public byte[] toBytes() {
        finalizePage();
        return assemblePdf();
    }

    private void startNewPage() {
        curStream = new StringBuilder();
        cursorY = PAGE_H - MARGIN - FONT_HEAD;
        pageContents.add(null);
    }

    private void finalizePage() {
        pageContents.set(pageContents.size() - 1, curStream.toString());
    }

    private void checkBreak(float needed) {
        if (cursorY - needed < MARGIN + 20) {
            finalizePage();
            startNewPage();
        }
    }

    private void appendText(float x, float y, String text, String font, float size) {
        if (text != null && !text.isEmpty()) {
            curStream.append("BT /").append(font).append(" ").append(fmt(size))
                     .append(" Tf ").append(fmt(x)).append(" ").append(fmt(y))
                     .append(" Td (").append(escape(text)).append(") Tj ET\n");
        }
    }

    private void hLine(float x1, float y, float x2) {
        curStream.append("0.5 w ")
                 .append(fmt(x1)).append(" ").append(fmt(y)).append(" m ")
                 .append(fmt(x2)).append(" ").append(fmt(y)).append(" l S\n");
    }

    private String fmt(float v) {
        return String.format("%.2f", v);
    }

    private String escape(String text) {
        byte[] bytes;
        try {
            bytes = text.getBytes(WIN1252);
        } catch (Exception exception) {
            bytes = text.getBytes();
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            int v = b & 0xFF;
            if      (v == '(')  sb.append("\\(");
            else if (v == ')')  sb.append("\\)");
            else if (v == '\\') sb.append("\\\\");
            else if (v < 32 || (v > 126 && v < 160))
                sb.append(String.format("\\%03o", v));
            else if (v >= 160)
                sb.append(String.format("\\%03o", v));
            else
                sb.append((char) v);
        }
        return sb.toString();
    }

    private byte[] assemblePdf() {
        int pageCount = pageContents.size();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Integer> offsets = new ArrayList<>();

        try {
            write(out, "%PDF-1.4\n");

            offsets.add(out.size());
            write(out, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

            StringBuilder kids = new StringBuilder("[");
            for (int i = 0; i < pageCount; i++) {
                kids.append(3 + i * 2).append(" 0 R ");
            }
            kids.append("]");

            int fontObj1 = 3 + pageCount * 2;
            int fontObj2 = fontObj1 + 1;
            int totalObjs = fontObj2 + 1;

            offsets.add(out.size());
            write(out, "2 0 obj\n<< /Type /Pages /Kids " + kids
                    + " /Count " + pageCount + " >>\nendobj\n");

            for (int i = 0; i < pageCount; i++) {
                int pageObj = 3 + i * 2;
                int streamObj = pageObj + 1;
                String content = pageContents.get(i);
                if (content == null) content = "";

                offsets.add(out.size());
                write(out, pageObj + " 0 obj\n"
                        + "<< /Type /Page /Parent 2 0 R\n"
                        + "   /MediaBox [0 0 612 792]\n"
                        + "   /Contents " + streamObj + " 0 R\n"
                        + "   /Resources << /Font << /F1 " + fontObj1 + " 0 R"
                        + " /F2 " + fontObj2 + " 0 R >> >> >>\nendobj\n");

                byte[] streamBytes = content.getBytes(WIN1252);
                offsets.add(out.size());
                write(out, streamObj + " 0 obj\n<< /Length " + streamBytes.length
                        + " >>\nstream\n");
                out.write(streamBytes);
                write(out, "\nendstream\nendobj\n");
            }

            offsets.add(out.size());
            write(out, fontObj1 + " 0 obj\n"
                    + "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica"
                    + " /Encoding /WinAnsiEncoding >>\nendobj\n");

            offsets.add(out.size());
            write(out, fontObj2 + " 0 obj\n"
                    + "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold"
                    + " /Encoding /WinAnsiEncoding >>\nendobj\n");

            int xrefOffset = out.size();
            write(out, "xref\n0 " + totalObjs + "\n");
            write(out, "0000000000 65535 f \n");
            for (int offset : offsets) {
                write(out, String.format("%010d 00000 n \n", offset));
            }

            write(out, "trailer\n<< /Size " + totalObjs + " /Root 1 0 R >>\n"
                    + "startxref\n" + xrefOffset + "\n%%EOF\n");

        } catch (IOException ioException) {
            LOGGER.log(Level.SEVERE, "Error al ensamblar PDF: {0}", ioException.getMessage());
        }

        return out.toByteArray();
    }

    private void write(OutputStream out, String text) throws IOException {
        out.write(text.getBytes("ISO-8859-1"));
    }
}
