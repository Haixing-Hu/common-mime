////////////////////////////////////////////////////////////////////////////////
//
//    Copyright (c) 2017 - 2024.
//    Nanjing Smart Medical Investment Operation Service Co. Ltd.
//
//    All rights reserved.
//
////////////////////////////////////////////////////////////////////////////////
package ltd.qubit.mime;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.annotation.Nonnull;

import ltd.qubit.commons.io.FileUtils;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * The abstract base class of file based MIME-type detectors.
 * <p>
 * A file based MIME-type detector detects the MIME-type of a file by reading
 * the content of a local file, usually by an external program.
 *
 * @author Haixing Hu
 */
public abstract class FileBasedMimeDetector extends AbstractMimeDetector {

  @Nonnull
  @Override
  protected List<String> guessFromContent(@Nonnull final Path path)
      throws IOException {
    return guessFromContent(path.toFile());
  }

  @Nonnull
  @Override
  protected List<String> guessFromContent(@Nonnull final InputStream input)
      throws IOException {
    // copy the content of the input stream to a temporary file
    final File tempFile = FileUtils.getTempFile("FileBasedMimeDetector", ".tmp");
    try {
      Files.copy(input, tempFile.toPath(), REPLACE_EXISTING);
      return guessFromContent(tempFile);
    } finally {
      Files.delete(tempFile.toPath());
    }
  }
}
