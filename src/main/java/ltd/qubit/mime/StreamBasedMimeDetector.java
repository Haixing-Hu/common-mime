////////////////////////////////////////////////////////////////////////////////
//
//    Copyright (c) 2022 - 2024.
//    Haixing Hu, Qubit Co. Ltd.
//
//    All rights reserved.
//
////////////////////////////////////////////////////////////////////////////////
package ltd.qubit.mime;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * The abstract base class of stream based MIME-type detectors.
 * <p>
 * A stream based MIME-type detector detects the MIME-type of a file by reading
 * the file content from an input stream.
 *
 * @author Haixing Hu
 */
public abstract class StreamBasedMimeDetector extends AbstractMimeDetector {

  @Nonnull
  @Override
  protected List<String> guessFromContent(@Nonnull final File file)
      throws IOException {
    try (final InputStream input = new FileInputStream(file)) {
      return guessFromContent(input);
    }
  }

  @Nonnull
  @Override
  protected List<String> guessFromContent(@Nonnull final Path path)
      throws IOException {
    try (final InputStream input = Files.newInputStream(path)) {
      return guessFromContent(input);
    }
  }
}
