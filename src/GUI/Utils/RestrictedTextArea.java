package GUI.Utils;

import javafx.scene.control.IndexRange;
import javafx.scene.control.TextArea;

import java.util.regex.Pattern;

public class RestrictedTextArea extends TextArea {

    private int maxLength = Integer.MAX_VALUE;
    private Pattern pattern = null;

    public void setRestriction(int maxLength, Pattern pattern) {
        this.maxLength = maxLength;
        this.pattern = pattern;
    }

    @Override
    public void replaceText(int start, int end, String text) {
        String currentText = getText();
        if (currentText == null) {
            currentText = "";
        }
        String resultingText = currentText.substring(0, start) + text + currentText.substring(end);
        boolean isAccepted = ValidationUtils.isAcceptedInput(resultingText, maxLength, pattern);
        if (isAccepted) {
            super.replaceText(start, end, text);
        }
    }

    @Override
    public void replaceSelection(String replacement) {
        String currentText = getText();
        if (currentText == null) {
            currentText = "";
        }
        IndexRange selection = getSelection();
        String resultingText = currentText.substring(0, selection.getStart())
                + replacement + currentText.substring(selection.getEnd());
        boolean isAccepted = ValidationUtils.isAcceptedInput(resultingText, maxLength, pattern);
        if (isAccepted) {
            super.replaceSelection(replacement);
        }
    }
}
