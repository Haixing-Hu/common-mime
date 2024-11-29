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

public class RepositoryMimeDetectorTest extends MimeDetectorTestBase {

  @Disabled
  @TestFactory
  public List<DynamicNode> test() throws IOException {
    final RepositoryMimeDetector detector = new RepositoryMimeDetector();
    detector.rebuildRepository();
    return testImpl(detector);
  }

  @Test
  public void testWav() throws IOException {
    final RepositoryMimeDetector detector = new RepositoryMimeDetector();
    detector.setAlwaysCheckMagicByDefault(true);
    detector.rebuildRepository();
    final String resource = "/mime-detection/audio1.wav";
    final URL url = SystemUtils.getResource(resource, this.getClass());
    assertNotNull(url);
    final String result = detector.detect(url);
    assertEquals("audio/vnd.wave", result);
  }

  @Test
  public void testWebm() throws IOException {
    final RepositoryMimeDetector detector = new RepositoryMimeDetector();
    detector.setAlwaysCheckMagicByDefault(true);
    final String resource = "/files/audio5.webm";
    final URL url = SystemUtils.getResource(resource, this.getClass());
    assertNotNull(url);
    final String result = detector.detect(url);
    assertEquals("video/webm", result);
  }
}
