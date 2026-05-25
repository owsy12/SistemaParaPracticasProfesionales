package GUI.Utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class DocxTemplateEngine {

    private static final Pattern MARKER = Pattern.compile("\\{\\{([^}]+)}}");
    private static final Pattern TEXT_IN_RUN = Pattern.compile("<w:t[^>]*>([^<]*)</w:t>");

    public static class RowExpansion {
        final String rowMarkerBase;
        final List<Map<String, String>> rowValues;
        final int rowsPerItem;

        public RowExpansion(String rowMarkerBase,
                            List<Map<String, String>> rowValues,
                            int rowsPerItem) {
            this.rowMarkerBase = rowMarkerBase;
            this.rowValues = rowValues;
            this.rowsPerItem = rowsPerItem;
        }
    }

    public static byte[] fill(String resourcePath, Map<String, String> values, List<RowExpansion> expansions) throws IOException {

        InputStream templateStream = DocxTemplateEngine.class.getResourceAsStream(resourcePath);
        if (templateStream == null) {
            throw new IOException("Template not found on classpath: " + resourcePath);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (ZipInputStream zipInput = new ZipInputStream(templateStream);
             ZipOutputStream zipOutput = new ZipOutputStream(outputStream)) {

            ZipEntry entry;
            while ((entry = zipInput.getNextEntry()) != null) {
                String entryName = entry.getName();
                byte[] entryData = readFully(zipInput);

                if ("word/document.xml".equals(entryName)) {
                    String xml = new String(entryData, StandardCharsets.UTF_8);
                    xml = normalizeSplitMarkers(xml);
                    if (expansions != null) {
                        for (RowExpansion expansion : expansions) {
                            xml = expandRows(xml, expansion);
                        }
                    }
                    xml = replaceMarkers(xml, values);
                    entryData = xml.getBytes(StandardCharsets.UTF_8);
                }

                zipOutput.putNextEntry(new ZipEntry(entryName));
                zipOutput.write(entryData);
                zipOutput.closeEntry();
            }
        }
        return outputStream.toByteArray();
    }

    public static byte[] fill(String resourcePath, Map<String, String> values) throws IOException {
        return fill(resourcePath, values, null);
    }

    private static String normalizeSplitMarkers(String xml) {
        xml = xml.replaceAll("<w:proofErr[^>]*/> *", "");

        StringBuilder result = new StringBuilder();
        int position = 0;

        while (position < xml.length()) {
            int openPos = xml.indexOf("{{", position);
            if (openPos < 0) {
                result.append(xml, position, xml.length());
                break;
            }

            int closePos = xml.indexOf("}}", openPos + 2);
            if (closePos < 0) {
                result.append(xml, position, xml.length());
                break;
            }

            String between = xml.substring(openPos + 2, closePos);

            if (!between.contains("<")) {
                result.append(xml, position, openPos);
                result.append("{{").append(between).append("}}");
            } else {
                Matcher textMatcher = TEXT_IN_RUN.matcher(between);
                StringBuilder markerName = new StringBuilder();
                while (textMatcher.find()) {
                    markerName.append(textMatcher.group(1));
                }
                result.append(xml, position, openPos);
                result.append("{{").append(markerName.toString().trim()).append("}}");
            }
            position = closePos + 2;
        }
        return result.toString();
    }

    private static String replaceMarkers(String xml, Map<String, String> values) {
        String result = xml;
        if (values != null && !values.isEmpty()) {
            StringBuffer buffer = new StringBuffer();
            Matcher matcher = MARKER.matcher(xml);
            while (matcher.find()) {
                String key = matcher.group(1).trim();
                String replacement = values.getOrDefault(key, "");
                matcher.appendReplacement(buffer,
                        Matcher.quoteReplacement(escapeXml(replacement)));
            }
            matcher.appendTail(buffer);
            result = buffer.toString();
        }
        return result;
    }

    private static String expandRows(String xml, RowExpansion expansion) {
        String expandedXml = xml;
        if (expansion.rowMarkerBase != null
                && expansion.rowValues != null
                && !expansion.rowValues.isEmpty()) {
            expandedXml = buildExpandedXml(xml, expansion);
        }
        return expandedXml;
    }

    private static String buildExpandedXml(String xml, RowExpansion expansion) {
        String markerText = "{{" + expansion.rowMarkerBase + "}}";
        int    markerPosition = xml.indexOf(markerText);
        String result = xml;

        if (markerPosition >= 0) {
            int rowStart = resolveRowStart(xml, markerPosition);
            if (rowStart >= 0) {
                int[] bounds = resolveBlockBounds(xml, rowStart, expansion.rowsPerItem);
                if (bounds != null) {
                    result = assembleExpandedXml(xml, expansion, bounds);
                }
            }
        }
        return result;
    }

    private static int resolveRowStart(String xml, int markerPosition) {
        int rowStart = xml.lastIndexOf("<w:tr ", markerPosition);
        if (rowStart < 0) {
            rowStart = xml.lastIndexOf("<w:tr>", markerPosition);
        }
        return rowStart;
    }

    private static int[] resolveBlockBounds(String xml, int rowStart, int rowsPerItem) {
        int     blockEnd = rowStart;
        boolean failed = false;
        for (int rowIndex = 0; rowIndex < rowsPerItem && !failed; rowIndex++) {
            int end = xml.indexOf("</w:tr>", blockEnd);
            if (end < 0) {
                failed = true;
            } else {
                blockEnd = end + "</w:tr>".length();
            }
        }
        int[] bounds = failed ? null : new int[]{rowStart, blockEnd};
        return bounds;
    }

    private static String assembleExpandedXml(String xml,
                                               RowExpansion expansion,
                                               int[] bounds) {
        int rowStart = bounds[0];
        int blockEnd = bounds[1];
        String templateBlock = xml.substring(rowStart, blockEnd);

        String templateSuffix = extractIndexSuffix(expansion.rowMarkerBase);
        int templateIndex = parseIndexSuffix(templateSuffix);
        String baseWithoutSuffix = expansion.rowMarkerBase.substring(
                0, expansion.rowMarkerBase.length() - templateSuffix.length());
        String activityPrefix = "a" + templateIndex + "_";
        String oldIndexFormatted = String.format("%02d", templateIndex);

        StringBuilder expanded = new StringBuilder();
        for (int rowNumber = 0; rowNumber < expansion.rowValues.size(); rowNumber++) {
            String newIndexFormatted = String.format("%02d", rowNumber + 1);
            String block = templateBlock;

            if (rowNumber > 0) {
                block = block.replace(
                        baseWithoutSuffix + templateSuffix,
                        baseWithoutSuffix + newIndexFormatted);
                block = block.replace(activityPrefix, "a" + (rowNumber + 1) + "_");
                block = renameIndexInMarkers(block, oldIndexFormatted, newIndexFormatted);
            }

            block = replaceMarkers(block, expansion.rowValues.get(rowNumber));
            expanded.append(block);
        }

        return xml.substring(0, rowStart) + expanded + xml.substring(blockEnd);
    }

    private static int parseIndexSuffix(String suffix) {
        int parsedIndex = 1;
        try {
            parsedIndex = Integer.parseInt(suffix);
        } catch (NumberFormatException ignored) {
            parsedIndex = 1;
        }
        return parsedIndex;
    }

    private static String renameIndexInMarkers(String block,
                                               String oldIndex, String newIndex) {
        block = block.replaceAll(
                "\\{\\{([^}]*)_" + Pattern.quote(oldIndex) + "(}}|_)",
                "{{$1_" + newIndex + "$2");

        block = block.replaceAll(
                "\\{\\{([^}]*)" + Pattern.quote(oldIndex) + "_",
                "{{$1" + newIndex + "_");

        return block;
    }

    private static String extractIndexSuffix(String markerBase) {
        int    underscoreIndex = markerBase.lastIndexOf('_');
        String result = "";
        if (underscoreIndex >= 0) {
            String candidate = markerBase.substring(underscoreIndex + 1);
            if (isValidInteger(candidate)) {
                result = candidate;
            }
        }
        return result;
    }

    private static boolean isValidInteger(String value) {
        boolean valid = true;
        try {
            Integer.parseInt(value);
        } catch (NumberFormatException numberFormatException) {
            valid = false;
        }
        return valid;
    }

    private static byte[] readFully(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[8192];
        int bytesRead;
        while ((bytesRead = inputStream.read(chunk)) != -1) buffer.write(chunk, 0, bytesRead);
        return buffer.toByteArray();
    }

    private static String escapeXml(String value) {
        String escaped = "";
        if (value != null) {
            escaped = value.replace("&", "&amp;")
                           .replace("<", "&lt;")
                           .replace(">", "&gt;")
                           .replace("\"", "&quot;")
                           .replace("'", "&apos;");
        }
        return escaped;
    }
}
