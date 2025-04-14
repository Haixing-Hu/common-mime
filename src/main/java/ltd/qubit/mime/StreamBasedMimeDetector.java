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
 * 基于流的MIME类型检测器的抽象基类。
 * <p>
 * 基于流的MIME类型检测器通过从输入流读取文件内容来检测文件的MIME类型。
 *
 * @author 胡海星
 */
public abstract class StreamBasedMimeDetector extends AbstractMimeDetector {

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  protected List<String> guessFromContent(@Nonnull final File file)
      throws IOException {
    try (final InputStream input = new FileInputStream(file)) {
      return guessFromContent(input);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  protected List<String> guessFromContent(@Nonnull final Path path)
      throws IOException {
    try (final InputStream input = Files.newInputStream(path)) {
      return guessFromContent(input);
    }
  }
}