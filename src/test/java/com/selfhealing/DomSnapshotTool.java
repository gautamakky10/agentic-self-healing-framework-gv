package com.selfhealing;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

public class DomSnapshotTool {
    public static String getCompactDom(WebDriver driver) {
        String js = "var copy = document.body.cloneNode(true);" +
                    "var elementsToRemove = copy.querySelectorAll('script, style, [hidden], [style*=\"display: none\"]');" +
                    "elementsToRemove.forEach(e => e.remove());" +
                    "return copy.innerHTML;";
        return (String) ((JavascriptExecutor) driver).executeScript(js);
    }
}
