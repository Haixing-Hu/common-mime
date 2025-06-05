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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.annotation.Nonnull;

import ltd.qubit.commons.io.FileUtils;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * 基于文件的MIME类型检测器的抽象基类。
 * <p>
 * 基于文件的MIME类型检测器通过读取本地文件的内容来检测文件的MIME类型，通常通过外部程序实现。
 *
 * @author 胡海星
 */
public abstract class FileBasedMimeDetector extends AbstractMimeDetector {

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  protected List<String> guessFromContent(@Nonnull final Path path)
      throws IOException {
    return guessFromContent(path.toFile());
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  protected List<String> guessFromContent(@Nonnull final InputStream input)
      throws IOException {
    // 将输入流的内容复制到临时文件
    final File tempFile = FileUtils.getTempFile("FileBasedMimeDetector", ".tmp");
    try {
      Files.copy(input, tempFile.toPath(), REPLACE_EXISTING);
      return guessFromContent(tempFile);
    } finally {
      Files.delete(tempFile.toPath());
    }
  }
}