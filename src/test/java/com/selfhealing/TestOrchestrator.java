package com.selfhealing;

import io.qameta.allure.Allure;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class TestOrchestrator {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private boolean testHealed = false;
    private StringBuilder healingSummary = new StringBuilder();

    public TestOrchestrator(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(2)); // Short wait for primary locators
    }

    public void assertNoHealing() {
        if (testHealed) {
            testHealed = false; // Reset state
            String msg = "Test passed functional steps, but required AI Self-Healing! Please update the locators.\nDetails:\n" + healingSummary.toString();
            Allure.step("Self-Healing Occurred - Failing Test For CI Pipeline Visibility");
            throw new AssertionError(msg);
        }
    }

    private void injectBanner(String message, String backgroundColor) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String script = "var existing = document.getElementById('ai-overlay');" +
                "if (existing) { existing.remove(); }" +
                "var banner = document.createElement('div');" +
                "banner.id = 'ai-overlay';" +
                "banner.style.position = 'fixed';" +
                "banner.style.top = '0';" +
                "banner.style.left = '0';" +
                "banner.style.width = '100%';" +
                "banner.style.padding = '15px';" +
                "banner.style.backgroundColor = '" + backgroundColor + "';" +
                "banner.style.color = 'white';" +
                "banner.style.textAlign = 'center';" +
                "banner.style.zIndex = '999999';" +
                "banner.style.fontWeight = 'bold';" +
                "banner.style.fontSize = '18px';" +
                "banner.style.boxShadow = '0px 4px 6px rgba(0,0,0,0.3)';" +
                "banner.innerText = arguments[0];" +
                "document.body.appendChild(banner);";
        js.executeScript(script, message);
    }

    public void showBuildCompleted() {
        injectBanner("🎉 BUILD COMPLETED SUCCESSFULLY! All steps finished cleanly.", "#0052cc");
        try { Thread.sleep(3500); } catch (Exception ex) {}
    }

    private void highlightElement(WebElement element, String color) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].style.border='3px solid " + color + "';", element);
    }

    private void dispatchReactEvent(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].dispatchEvent(new Event('input', { bubbles: true }));", element);
    }

    private void jsClick(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click();", element);
    }

    private By getBy(String locator) {
        if (locator.startsWith("//") || locator.startsWith("(//")) {
            return By.xpath(locator);
        } else if (locator.startsWith("#") || locator.startsWith(".") || locator.contains(" ") || locator.contains(">") || locator.contains("[")) {
            return By.cssSelector(locator);
        } else {
            return By.id(locator); // fallback
        }
    }

    public void resilientClick(String locator, String targetDescription, String stepId) {
        int attempt = 1;
        while (attempt <= 2) {
            try {
                // Using presenceOfElementLocated so it never throws strict interaction errors on dummies
                WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(getBy(locator)));
                highlightElement(element, "blue");
                jsClick(element);
                HealingReportWriter.logStable(stepId, locator);
                injectBanner("✅ Passed: " + targetDescription + " (No Healing Needed)", "green");
                try { Thread.sleep(800); } catch (Exception ex) {}
                return;
            } catch (Exception e) {
                injectBanner("⚠️ Locator mismatch for " + targetDescription + "! Retrying... (Attempt " + attempt + " of 2)", "orange");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                attempt++;
            }
        }
        
        handleRecovery(targetDescription, stepId, locator, null, true);
    }

    public void resilientFill(String locator, String text, String targetDescription, String stepId) {
        int attempt = 1;
        while (attempt <= 2) {
            try {
                WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(getBy(locator)));
                highlightElement(element, "blue");
                
                // Only type if it's an actual input to prevent InvalidElementState on dummy elements
                String tag = element.getTagName();
                if ("input".equalsIgnoreCase(tag) || "textarea".equalsIgnoreCase(tag)) {
                    element.clear();
                    element.sendKeys(text);
                    dispatchReactEvent(element);
                }
                
                HealingReportWriter.logStable(stepId, locator);
                injectBanner("✅ Passed: " + targetDescription + " (No Healing Needed)", "green");
                try { Thread.sleep(800); } catch (Exception ex) {}
                return;
            } catch (Exception e) {
                injectBanner("⚠️ Locator mismatch for " + targetDescription + "! Retrying... (Attempt " + attempt + " of 2)", "orange");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                attempt++;
            }
        }
        
        handleRecovery(targetDescription, stepId, locator, text, false);
    }

    private void checkPolicy(double confidence, String targetDescription) {
        if (confidence < 0.80) {
            throw new RuntimeException("Strict Kill-Switch Triggered! PolicyEngine rejected recovery for '" + 
                    targetDescription + "'. Confidence score " + confidence + " is below 0.80 threshold.");
        }
    }

    private void handleRecovery(String targetDescription, String stepId, String oldLocator, String text, boolean isClick) {
        injectBanner("🤖 AI Agent analyzing DOM to heal selector for: " + targetDescription, "purple");
        try { Thread.sleep(800); } catch (Exception ex) {}
        
        String compactDom = DomSnapshotTool.getCompactDom(driver);
        RecoveryResult result = SelectorRecoveryAgent.recoverLocator(targetDescription, compactDom);
        
        checkPolicy(result.getConfidence(), targetDescription);

        try {
            WebElement element;
            if (isClick) {
                element = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.presenceOfElementLocated(getBy(result.getLocator())));
                highlightElement(element, "#ff00ff"); 
                jsClick(element);
            } else {
                element = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.presenceOfElementLocated(getBy(result.getLocator())));
                highlightElement(element, "#ff00ff");
                
                String tag = element.getTagName();
                if ("input".equalsIgnoreCase(tag) || "textarea".equalsIgnoreCase(tag)) {
                    element.clear();
                    element.sendKeys(text);
                    dispatchReactEvent(element);
                }
            }
            
            testHealed = true;
            healingSummary.append("- ").append(targetDescription).append(" (Healed to: `").append(result.getLocator()).append("`)\n");
            Allure.addAttachment("Self-Healing Details", "Healed " + targetDescription + " using locator " + result.getLocator());
            
            HealingReportWriter.logHealed(stepId, oldLocator, result.getLocator(), result.getConfidence());
            injectBanner("🔧 Self-Healed & Fixed: " + targetDescription, "#800080"); // Deep purple banner
            try { Thread.sleep(1500); } catch (Exception ex) {}
            
        } catch (Exception e) {
            injectBanner("❌ FATAL: Element missing entirely: " + targetDescription, "red");
            throw new RuntimeException("AI Recovery execution failed for " + targetDescription, e);
        }
    }
}
