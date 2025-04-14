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
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ltd.qubit.commons.concurrent.Lazy;
import ltd.qubit.commons.datastructure.CollectionUtils;
import ltd.qubit.commons.util.CommandExecutor;
import ltd.qubit.mime.repository.MimeRepository;
import ltd.qubit.mime.repository.MimeType;

/**
 * 基于系统'file'命令的MIME类型检测器。
 *
 * @author 胡海星
 */
public class FileCommandMimeDetector extends FileBasedMimeDetector {

  public static final String COMMAND = "file --mime-type --brief ${file}";

  private final CommandExecutor executor = new CommandExecutor();

  /**
   * 设置命令执行超时时间。
   *
   * @param timeout
   *     命令执行的超时时间。
   */
  public void setExecutionTimeout(final Duration timeout) {
    executor.setTimeout(timeout);
  }

  /**
   * 设置命令执行的工作目录。
   *
   * @param workingDirectory
   *     命令执行的工作目录。
   */
  public void setWorkingDirectory(final String workingDirectory) {
    executor.setWorkingDirectory(workingDirectory);
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  protected List<String> guessFromFilename(@Nonnull final String filename) {
    final MimeRepository repository = MimeRepository.getInstance();
    final List<MimeType> candidates = repository.detectByFilename(filename);
    if (CollectionUtils.isEmpty(candidates)) {
      return Collections.emptyList();
    } else {
      return candidates
          .stream()
          .map(MimeType::getName)
          .collect(Collectors.toList());
    }
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  protected List<String> guessFromContent(@Nonnull final File file)
      throws IOException {
    final String cmd = COMMAND.replace("${file}", file.getAbsolutePath());
    final String output = executor.execute(cmd, true);
    if (output != null) {
      final String result = output.strip();
      if (result.isEmpty()) {
        return Collections.emptyList();
      } else {
        return Collections.singletonList(result);
      }
    } else {
      return Collections.emptyList();
    }
  }

  /**
   * 检查'file'命令是否可用。
   *
   * @return
   *     如果'file'命令可用，则返回{@code true}，否则返回{@code false}。
   */
  public static boolean isAvailable() {
    return AVAILABLE.get();
  }

  private static final Lazy<Boolean> AVAILABLE = Lazy.of(FileCommandMimeDetector::checkAvailable);

  /**
   * 检查'file'命令是否可用。
   *
   * @return
   *     如果'file'命令可用，则返回{@code true}，否则返回{@code false}。
   */
  private static boolean checkAvailable() {
    final String cmd = COMMAND.replace("${file}", ".");
    final CommandExecutor executor = new CommandExecutor();
    executor.setDisableLogging(true);
    boolean result;
    try {
      final String output = executor.execute(cmd, true);
      result = (output != null);
    } catch (final IOException e) {
      result = false;
    }
    final Logger logger = LoggerFactory.getLogger(FileCommandMimeDetector.class);
    if (result) {
      logger.info("The 'file' command is available.");
    } else {
      logger.info("The 'file' command is not available.");
    }
    return result;
  }
}