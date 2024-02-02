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
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

public class RepositoryMimeDetectorTest extends MimeDetectorTestBase {

  @Disabled
  @TestFactory
  public List<DynamicNode> test() throws IOException {
    final RepositoryMimeDetector detector = new RepositoryMimeDetector();
    detector.rebuildRepository();
    return testImpl(detector);
  }
}
