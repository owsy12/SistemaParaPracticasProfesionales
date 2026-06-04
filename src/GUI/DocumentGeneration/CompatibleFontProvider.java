package GUI.DocumentGeneration;

import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.BaseFont;
import fr.opensagres.xdocreport.itext.extension.font.IFontProvider;

import java.util.HashMap;
import java.util.Map;

public class CompatibleFontProvider implements IFontProvider {

    private static final String FONT_HELVETICA = FontFactory.HELVETICA;
    private static final String FONT_TIMES_ROMAN = FontFactory.TIMES_ROMAN;
    private static final String FONT_COURIER = FontFactory.COURIER;
    private static final String FONT_SYMBOL = FontFactory.SYMBOL;
    private static final String SAFE_ENCODING = BaseFont.WINANSI;

    private static final Map<String, String> FONT_MAP = buildFontMap();

    private static Map<String, String> buildFontMap() {
        Map<String, String> fontMap = new HashMap<>();
        fontMap.put("calibri", FONT_HELVETICA);
        fontMap.put("calibri light", FONT_HELVETICA);
        fontMap.put("arial", FONT_HELVETICA);
        fontMap.put("arial narrow", FONT_HELVETICA);
        fontMap.put("helvetica", FONT_HELVETICA);
        fontMap.put("tahoma", FONT_HELVETICA);
        fontMap.put("verdana", FONT_HELVETICA);
        fontMap.put("trebuchet ms",FONT_HELVETICA);
        fontMap.put("times new roman",FONT_TIMES_ROMAN);
        fontMap.put("georgia",FONT_TIMES_ROMAN);
        fontMap.put("garamond",FONT_TIMES_ROMAN);
        fontMap.put("book antiqua", FONT_TIMES_ROMAN);
        fontMap.put("courier new",FONT_COURIER);
        fontMap.put("courier", FONT_COURIER);
        fontMap.put("lucida console", FONT_COURIER);
        fontMap.put("consolas", FONT_COURIER);
        fontMap.put("symbol", FONT_SYMBOL);
        fontMap.put("wingdings", FONT_SYMBOL);
        return fontMap;
    }

    @Override
    public Font getFont(String familyName,
                        String encoding,
                        float size,
                        int style,
                        java.awt.Color color) {

        String resolvedName = resolveFontName(familyName);
        String resolvedEncoding = resolveEncoding(encoding);
        Font font = FontFactory.getFont(resolvedName, resolvedEncoding, size, style);
        if (color != null) {
            font.setColor(color.getRed(), color.getGreen(), color.getBlue());
        }
        return font;
    }

    private static String resolveFontName(String familyName) {
        String resolvedName = FONT_HELVETICA;
        if (familyName != null) {
            String normalizedName = familyName.trim().toLowerCase();
            String mappedName = FONT_MAP.get(normalizedName);
            if (mappedName != null) {
                resolvedName = mappedName;
            }
        }
        return resolvedName;
    }

    private static String resolveEncoding(String encoding) {
        String resolvedEncoding = SAFE_ENCODING;
        if (encoding != null && isSafeEncoding(encoding)) {
            resolvedEncoding = encoding;
        }
        return resolvedEncoding;
    }

    private static boolean isSafeEncoding(String encoding) {
        String lower = encoding.toLowerCase();
        boolean isCidEncoding = lower.contains("identity")
                || lower.contains("cid")
                || lower.contains("uni-")
                || lower.contains("uni_")
                || lower.equals("gbk")
                || lower.equals("big5");
        boolean isSafe = !isCidEncoding;
        return isSafe;
    }
}
