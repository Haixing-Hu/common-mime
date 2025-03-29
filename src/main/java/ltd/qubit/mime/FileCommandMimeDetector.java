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

import ltd.qubit.commons.datastructure.CollectionUtils;
import ltd.qubit.commons.util.CommandExecutor;
import ltd.qubit.mime.repository.MimeRepository;
import ltd.qubit.mime.repository.MimeType;

public class FileCommandMimeDetector extends FileBasedMimeDetector {

  public static final String COMMAND = "file --mime-type --brief ${file}";

  private final CommandExecutor executor = new CommandExecutor();

  public void setExecutionTimeout(final Duration timeout) {
    executor.setTimeout(timeout);
  }

  public void setWorkingDirectory(final String workingDirectory) {
    executor.setWorkingDirectory(workingDirectory);
  }

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
   * Checks if the 'file' command is available.
   *
   * @return
   *     {@code true} if the 'file' command is available, {@code false} otherwise.
   */
  public static boolean isAvailable() {
    final String cmd = COMMAND.replace("${file}", ".");
    final CommandExecutor executor = new CommandExecutor();
    final String output;
    try {
      output = executor.execute(cmd, true);
      return output != null;
    } catch (final IOException e) {
      return false;
    }
  }
}