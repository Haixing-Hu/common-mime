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
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import ltd.qubit.commons.lang.SystemUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileCommandMimeDetectorTest extends MimeDetectorTestBase {

  @Disabled
  @TestFactory
  public List<DynamicNode> test() throws IOException {
    final MimeDetector detector = new FileCommandMimeDetector();
    return testImpl(detector);
  }

  @Test
  public void testWebm() throws IOException {
    final MimeDetector detector = new FileCommandMimeDetector();
    detector.setAlwaysCheckMagicByDefault(true);
    final String resource = "/files/audio5.webm";
    final URL url = SystemUtils.getResource(resource, this.getClass());
    assertNotNull(url);
    final String result = detector.detect(url);
    assertEquals("video/webm", result);
  }

  @Test
  public void testIsAvailable() {
    assertTrue(FileCommandMimeDetector.isAvailable());
  }
}