package com.bofa;

import org.openqa.selenium.*;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

import static org.testng.Assert.assertEquals;

public class BofATest {
//Environment variable for user and Sauce_accesskey
    public String sauce_username = System.getenv("SAUCE_USERNAME");
    public String sauce_accesskey = System.getenv("SAUCE_ACCESS_KEY");

    /**
     * ThreadLocal variable which contains the  {@link WebDriver} instance which is used to perform browser interactions with.
     */
    private ThreadLocal<WebDriver> webDriver = new ThreadLocal<WebDriver>();
    /**
     * ThreadLocal variable which contains the Sauce Job Id.
     */
    private ThreadLocal<String> sessionId = new ThreadLocal<String>();

    @DataProvider(name = "hardCodedBrowsers", parallel = true)
    public static Object[][] sauceBrowserDataProvider(Method testMethod) {
        return new Object[][]{

//                // Windows
                new Object[]{"browser","chrome", "latest", "Windows 10",""},
                new Object[]{"browser","firefox", "latest-2", "Windows 10",""},
        };
    }

    /**
     * Constructs a new {@link RemoteWebDriver} instance which is configured to use the capabilities defined by the browser,
     * version and os parameters, and which is configured to run against ondemand.saucelabs.com, using
     * the username and access key populated by the  instance.
     *
     * @param browser Represents the browser to be used as part of the test run.
     * @param version Represents the version of the browser to be used as part of the test run.
     * @param os Represents the operating system to be used as part of the test run.
     * @return
     * @throws MalformedURLException if an error occurs parsing the url
     */
    private WebDriver createDriver(String environment, String browser, String version, String os, String device, String methodName) throws MalformedURLException {

        DesiredCapabilities capabilities = new DesiredCapabilities();

        capabilities.setCapability("username", sauce_username);
        capabilities.setCapability("accesskey", sauce_accesskey);
        String jobName = methodName;
        capabilities.setCapability("name", jobName);

        if (environment == "browser") {
            capabilities.setCapability("browserName", browser);
            capabilities.setCapability("version", version);
            capabilities.setCapability("platform", os);
            capabilities.setCapability("extendedDebugging", true);
            capabilities.setCapability("capturePerformance", true);
        } else {
            capabilities.setCapability("platformName", os);
            capabilities.setCapability("deviceName", device);
        }

        //US
        //Creates Selenium Driver
        webDriver.set(new RemoteWebDriver(
                new URL("https://ondemand.us-west-1.saucelabs.com:443/wd/hub"),
                capabilities));
        // EU
        // webDriver.set(new RemoteWebDriver(
        //         new URL("https://ondemand.eu-central-1.saucelabs.com/wd/hub"),
        //         capabilities));

        //Keeps track of the unique Selenium session ID used to identify jobs on Sauce Labs
       String id = ((RemoteWebDriver) getWebDriver()).getSessionId().toString();
        sessionId.set(id);

        //For CI plugins
        String message = String.format("SauceOnDemandSessionID=%1$s job-name=%2$s", id, jobName);
        System.out.println(message);

        return webDriver.get();
    }

    @AfterMethod
    public void tearDown(ITestResult result) throws Exception {
        boolean status = result.isSuccess();
        ((JavascriptExecutor)webDriver.get()).executeScript("sauce:job-result="+ status);
        webDriver.get().quit();
    }

    /**
     * Runs a simple test verifying the title of the wikipedia.org home page.
     *
     * @param browser Represents the browser to be used as part of the test run.
     * @param version Represents the version of the browser to be used as part of the test run.
     * @param os Represents the operating system to be used as part of the test run.
     * @throws Exception if an error occurs during the running of the test
     */

    @Test(dataProvider = "hardCodedBrowsers")
    public void BofAFileUpload(String type, String browser, String version, String os, String device, Method method) throws Exception {

        WebDriver driver = createDriver(type, browser, version, os, device, method.getName());
        driver.get("https://cgi-lib.berkeley.edu/ex/fup.html");
        driver.findElement(By.name("upfile")).click();
        driver.findElement(By.name("upfile")).clear();
        driver.findElement(By.name("upfile")).sendKeys("“$HOME/downloads/T04c82511-5f2b-4c4e-b8fc-dab675b779c2.pdf");
        driver.findElement(By.name("note")).click();
        driver.findElement(By.name("note")).clear();
        driver.findElement(By.name("note")).sendKeys("This is my itinerary");
        driver.findElement(By.xpath("//input[@value='Press']")).click();
    }
    /**
     * @return the {@link WebDriver} for the current thread
     */
    public WebDriver getWebDriver() {
        System.out.println("WebDriver" + webDriver.get());
        return webDriver.get();
    }

    /**
     *
     * @return the Sauce Job id for the current thread
     */
    public String getSessionId() {
        return sessionId.get();
    }
}