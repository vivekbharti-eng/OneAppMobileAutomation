package com.automation.reports;

import com.automation.utils.ReportPathManager;
import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportBuilder;
import net.masterthought.cucumber.presentation.PresentationMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates a Masterthought Cucumber HTML report from cucumber.json output.
 * Report is saved to: test-output/cucumber-reports/{timestamp}/
 */
public class CucumberReportGenerator {

    private static final Logger logger = LogManager.getLogger(CucumberReportGenerator.class);
    private static final String JSON_SOURCE = "target/cucumber-reports/cucumber.json";

    public static void generateReport() {
        try {
            File jsonFile = new File(JSON_SOURCE);
            if (!jsonFile.exists()) {
                logger.warn("cucumber.json not found at: " + JSON_SOURCE + " — skipping Cucumber HTML report generation");
                return;
            }

            // Output directory: test-output/cucumber-reports/{timestamp}/
            File outputDir = new File(ReportPathManager.getCucumberReportDir());
            outputDir.mkdirs();

            // JSON input files
            List<String> jsonFiles = new ArrayList<>();
            jsonFiles.add(jsonFile.getAbsolutePath());

            // Configure report
            Configuration config = new Configuration(outputDir, "EcoCash Automation");
            config.addPresentationModes(PresentationMode.RUN_WITH_JENKINS);
            config.addPresentationModes(PresentationMode.EXPAND_ALL_STEPS);
            config.setBuildNumber(ReportPathManager.getTimestamp());
            config.addClassifications("Platform", System.getProperty("platform", "android"));
            config.addClassifications("Environment", "QA Pre-Prod");
            config.addClassifications("Run", ReportPathManager.getTimestamp());

            // Generate
            ReportBuilder reportBuilder = new ReportBuilder(jsonFiles, config);
            reportBuilder.generateReports();

            logger.info("Cucumber HTML report generated: " + outputDir.getAbsolutePath());
            System.out.println("\u001B[32m  ✓ Cucumber HTML Report generated at: " + outputDir.getAbsolutePath()
                    + "/cucumber-html-reports/overview-features.html\u001B[0m");

        } catch (Exception e) {
            logger.error("Failed to generate Cucumber HTML report: " + e.getMessage(), e);
        }
    }
}
