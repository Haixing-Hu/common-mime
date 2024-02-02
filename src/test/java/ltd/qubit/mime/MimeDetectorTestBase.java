////////////////////////////////////////////////////////////////////////////////
//
//    Copyright (c) 2022 - 2024.
//    Haixing Hu, Qubit Co. Ltd.
//
//    All rights reserved.
//
////////////////////////////////////////////////////////////////////////////////
package ltd.qubit.mime;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;

import ltd.qubit.commons.io.IoUtils;
import ltd.qubit.commons.lang.StringUtils;
import ltd.qubit.commons.lang.SystemUtils;

import static java.nio.charset.StandardCharsets.UTF_8;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import static ltd.qubit.commons.lang.StringUtils.formatPercent;

/**
 * The base class of all tests of the {@link MimeDetector}s.
 *
 * @author Haixing Hu
 */
public class MimeDetectorTestBase {

  public static final String TEST_DATA_DIR = "/mime-detection/";

  public static final String TEST_DATA_LIST = "/mime-detection/list";

  private static int totalTests = 0;
  private static int passedTests = 0;

  static class TestData {
    public String filename;
    public String mimeType;
  }

  private static List<TestData> loadTestData() throws IOException {
    final URL listUrl = SystemUtils.getResource(TEST_DATA_LIST, MimeDetectorTestBase.class);
    assertNotNull(listUrl, "Cannot find the test data list file.");
    final List<String> lines = IoUtils.readLines(listUrl, UTF_8);
    final List<TestData> result = new ArrayList<>();
    for (final String line : lines) {
      if (line.startsWith("#") || line.isBlank()) {
        continue;
      }
      final String[] fields = line.split("\\s+");
      if (fields.length >= 2) {
        final TestData data = new TestData();
        data.filename = fields[0];
        data.mimeType = fields[1];
        result.add(data);
      }
    }
    return result;
  }

  protected List<DynamicNode> testImpl(final MimeDetector detector) throws IOException {
    final List<DynamicNode> result = new ArrayList<>();
    final List<TestData> testData = loadTestData();
    for (final TestData datum : testData) {
      final String filename = datum.filename;
      final String mimeType = datum.mimeType;
      final String resource = TEST_DATA_DIR + filename;
      final DynamicTest test = DynamicTest.dynamicTest(resource, () -> {
        ++totalTests;
        System.out.println("Detecting mime type of " + resource + " ...");
        final URL url = SystemUtils.getResource(resource, MimeDetectorTestBase.class);
        assertNotNull(url, "Cannot find the test data file: " + resource);
        final String detectedMimeType = detector.detect(url);
        assertNotNull(detectedMimeType, "Cannot detect the mime type of the test data file: " + resource);
        assertEquals(mimeType, detectedMimeType,
            "Mime type mismatch: " + resource + " -> " + detectedMimeType + " (expected: " + mimeType + ")");
        System.out.println("PASSED: Mime type of " + resource + " is " + detectedMimeType + ".");
        ++passedTests;
      });
      result.add(test);
    }
    return result;
  }

  @BeforeAll
  public static void setUp() {
    System.out.println("Start testing...");
    System.out.println(StringUtils.repeat("=", 80));
    totalTests = 0;
    passedTests = 0;
  }

  @AfterAll
  public static void tearDown() {
    System.out.println(StringUtils.repeat("=", 80));
    System.out.println("All tests finished.");
    System.out.println("Total Tests:  " + totalTests);
    System.out.println("Passed Tests: " + passedTests);
    System.out.println("Failed Tests: " + (totalTests - passedTests));
    System.out.println("Success Rate: " + formatPercent((double)passedTests / totalTests, 2));
  }
}
