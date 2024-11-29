////////////////////////////////////////////////////////////////////////////////
//
//    Copyright (c) 2022 - 2024.
//    Haixing Hu, Qubit Co. Ltd.
//
//    All rights reserved.
//
////////////////////////////////////////////////////////////////////////////////
package ltd.qubit.mime;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import ltd.qubit.commons.datastructure.CollectionUtils;
import ltd.qubit.mime.repository.MimeRepository;
import ltd.qubit.mime.repository.MimeType;

/**
 * The MIME-type detector based on the pre-built MIME-repository.
 *
 * @author Haixing Hu
 * @see MimeRepository
 */
public class RepositoryMimeDetector extends StreamBasedMimeDetector {

  public void rebuildRepository() {
    MimeRepository.getInstance().rebuild();
  }

  @Override
  @Nonnull
  protected List<String> guessFromFilename(@Nonnull final String filename) {
    final MimeRepository repository = MimeRepository.getInstance();
    final List<MimeType> mimeTypes = repository.detectByFilename(filename);
    logger.debug("The MIME-types detected by extension from the repository are: {}", mimeTypes);
    if (CollectionUtils.isEmpty(mimeTypes)) {
      return Collections.emptyList();
    } else {
      return mimeTypes
          .stream()
          .map(MimeType::getName)
          .collect(Collectors.toList());
    }
  }

  @Nonnull
  @Override
  protected List<String> guessFromContent(@Nonnull final InputStream input)
      throws IOException {
    final MimeRepository repository = MimeRepository.getInstance();
    final InputStream markSupportedInput;
    if (input.markSupported()) {
      markSupportedInput = input;
    } else {
      markSupportedInput = new BufferedInputStream(input);
    }
    final List<MimeType> mimeTypes = repository.detectByContent(markSupportedInput);
    logger.debug("The MIME-types detected by content from the repository are: {}", mimeTypes);
    if (CollectionUtils.isEmpty(mimeTypes)) {
      return Collections.emptyList();
    } else {
      return mimeTypes
          .stream()
          .map(MimeType::getName)
          .collect(Collectors.toList());
    }
  }
}
