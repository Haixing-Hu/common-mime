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
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import ltd.qubit.commons.io.FilenameUtils;

/**
 * MIME类型检测器的接口。
 *
 * @author 胡海星
 */
@Immutable
public interface MimeDetector {

  /**
   * 获取默认的MIME类型检测器。
   * <p>
   * 此方法首先检查系统中是否可以使用'file'命令。如果可以使用，则返回一个基于file命令的MIME类型检测器。
   * 否则，返回一个基于仓库的MIME类型检测器。
   * <p>
   * 该方法确保在不同的系统环境中都能提供一个合适的MIME类型检测器。
   *
   * @return
   *     默认的MIME类型检测器实例。
   */
  static MimeDetector getDefault() {
    if (FileCommandMimeDetector.isAvailable()) {
      return new FileCommandMimeDetector();
    } else {
      return new RepositoryMimeDetector();
    }
  }

  /**
   * 获取此检测器是否默认总是检查文件的魔术数字。
   * <p>
   * 魔术数字是文件内容的特征标识符，用于更准确地检测文件的MIME类型。
   * 此方法返回一个布尔值，指示检测器是否在默认情况下总是检查文件的魔术数字。
   *
   * @return
   *     如果检测器默认总是检查文件的魔术数字，则返回 {@code true}；否则返回 {@code false}。
   */
  boolean isAlwaysCheckMagicByDefault();

  /**
   * 设置此检测器是否默认总是检查文件的魔术数字。
   * <p>
   * 通过此方法，可以配置检测器在默认情况下是否总是检查文件的魔术数字，以提高MIME类型检测的准确性。
   *
   * @param alwaysCheckMagicByDefault
   *     如果为 {@code true}，则检测器将默认总是检查文件的魔术数字；如果为 {@code false}，则不检查。
   */
  void setAlwaysCheckMagicByDefault(boolean alwaysCheckMagicByDefault);

  /**
   * 仅通过文件名扩展名检测文件的MIME类型。
   * <p>
   * 此方法通过分析文件的扩展名来推断其MIME类型，不检查文件内容。
   *
   * @param file
   *     要检测的文件。
   * @return
   *     检测到的文件MIME类型的规范名称，如果无法检测到MIME类型，则返回{@code null}。
   * @see #detectByFilename(Path)
   * @see #detectByFilename(String)
   * @see #detectByFilename(URL)
   * @see #detectByFilename(URI)
   */
  @Nullable
  String detectByFilename(@Nonnull final File file);

  /**
   * 仅通过文件名扩展名检测文件的MIME类型。
   * <p>
   * 此方法通过分析文件路径的扩展名来推断其MIME类型，不检查文件内容。
   *
   * @param path
   *     要检测的文件路径。
   * @return
   *     检测到的文件MIME类型的规范名称，如果无法检测到MIME类型，则返回{@code null}。
   * @see #detectByFilename(File)
   * @see #detectByFilename(String)
   * @see #detectByFilename(URL)
   * @see #detectByFilename(URI)
   */
  @Nullable
  String detectByFilename(@Nonnull final Path path);

  /**
   * 仅通过文件名扩展名检测文件的MIME类型。
   * <p>
   * 此方法通过分析文件路径字符串的扩展名来推断其MIME类型，不检查文件内容。
   *
   * @param path
   *     要检测的文件路径。
   * @return
   *     检测到的文件MIME类型的规范名称，如果无法检测到MIME类型，则返回{@code null}。
   * @see #detectByFilename(File)
   * @see #detectByFilename(Path)
   * @see #detectByFilename(URL)
   * @see #detectByFilename(URI)
   */
  @Nullable
  String detectByFilename(@Nonnull final String path);

  /**
   * 仅通过文件名扩展名检测文件的MIME类型。
   * <p>
   * 此方法通过分析文件URL的扩展名来推断其MIME类型，不检查文件内容。
   *
   * @param url
   *     要检测的文件URL。
   * @return
   *     检测到的文件MIME类型的规范名称，如果无法检测到MIME类型，则返回{@code null}。
   * @see #detectByFilename(File)
   * @see #detectByFilename(Path)
   * @see #detectByFilename(String)
   * @see #detectByFilename(URI)
   */
  @Nullable
  String detectByFilename(@Nonnull final URL url);

  /**
   * 仅通过文件名扩展名检测文件的MIME类型。
   * <p>
   * 此方法通过分析文件URI的扩展名来推断其MIME类型，不检查文件内容。
   *
   * @param uri
   *     要检测的文件URI。
   * @return
   *     检测到的文件MIME类型的规范名称，如果无法检测到MIME类型，则返回{@code null}。
   * @see #detectByFilename(File)
   * @see #detectByFilename(Path)
   * @see #detectByFilename(String)
   * @see #detectByFilename(URL)
   */
  @Nullable
  String detectByFilename(@Nonnull final URI uri);

  /**
   * 仅通过文件内容检测文件的MIME类型。
   * <p>
   * 此方法通过分析文件的实际内容来推断其MIME类型，而不是依赖于文件名或扩展名。
   *
   * @param file
   *     要检测的文件。
   * @return
   *     检测到的文件MIME类型的规范名称，如果无法检测到MIME类型，则返回{@code null}。
   * @throws IOException
   *     如果发生任何I/O错误。
   * @see #detectByContent(Path)
   * @see #detectByContent(InputStream)
   * @see #detectByContent(byte[])
   * @see #detectByContent(URL)
   * @see #detectByContent(URI)
   */
  @Nullable
  String detectByContent(@Nonnull final File file) throws IOException;

  /**
   * 仅通过文件内容检测文件的MIME类型。
   * <p>
   * 此方法通过分析文件路径指向的文件的实际内容来推断其MIME类型，而不是依赖于文件名或扩展名。
   *
   * @param path
   *     要检测的文件路径。
   * @return
   *     检测到的文件MIME类型的规范名称，如果无法检测到MIME类型，则返回{@code null}。
   * @throws IOException
   *     如果发生任何I/O错误。
   * @see #detectByContent(File)
   * @see #detectByContent(InputStream)
   * @see #detectByContent(byte[])
   * @see #detectByContent(URL)
   * @see #detectByContent(URI)
   */
  @Nullable
  String detectByContent(@Nonnull final Path path) throws IOException;

  /**
   * 仅通过文件内容检测文件的MIME类型。
   * <p>
   * <b>注意：</b>此函数不会关闭输入流。
   * <p>
   * 此方法通过分析输入流中的数据来推断其MIME类型，而不是依赖于文件名或扩展名。
   *
   * @param input
   *     要检测的文件的输入流。注意，此函数在使用后不会关闭输入流。
   * @return
   *     检测到的文件MIME类型的规范名称，如果无法检测到MIME类型，则返回{@code null}。
   * @throws IOException
   *     如果发生任何I/O错误。
   * @see #detectByContent(File)
   * @see #detectByContent(Path)
   * @see #detectByContent(byte[])
   * @see #detectByContent(URL)
   * @see #detectByContent(URI)
   */
  @Nullable
  String detectByContent(@Nonnull final InputStream input) throws IOException;

  /**
   * 仅通过文件内容检测文件的MIME类型。
   * <p>
   * 此方法通过分析文件URL指向的内容来推断其MIME类型，而不是依赖于文件名或扩展名。
   *
   * @param url
   *     要检测的文件URL。
   * @return
   *     检测到的文件MIME类型的规范名称，如果无法检测到MIME类型，则返回{@code null}。
   * @throws IOException
   *     如果发生任何I/O错误。
   * @see #detectByContent(File)
   * @see #detectByContent(Path)
   * @see #detectByContent(InputStream)
   * @see #detectByContent(byte[])
   * @see #detectByContent(URI)
   */
  @Nullable
  String detectByContent(@Nonnull final URL url) throws IOException;

  /**
   * 仅通过文件内容检测文件的MIME类型。
   *
   * @param uri
   *     要检测的文件URI。
   * @return
   *     检测到的文件MIME类型的规范名称，如果无法检测到MIME类型，则返回{@code null}。
   * @throws IOException
   *     如果发生任何I/O错误。
   * @see #detectByContent(File)
   * @see #detectByContent(Path)
   * @see #detectByContent(InputStream)
   * @see #detectByContent(byte[])
   * @see #detectByContent(URL)
   */
  @Nullable
  String detectByContent(@Nonnull final URI uri) throws IOException;

  /**
   * 仅通过文件内容检测文件的MIME类型。
   * <p>
   * 此方法通过分析字节数组中的数据来推断其MIME类型，而不是依赖于文件名或扩展名。
   *
   * @param content
   *     要检测的文件内容的字节数组。
   * @return
   *     检测到的文件MIME类型的规范名称，如果无法检测到MIME类型，则返回{@code null}。
   * @see #detectByContent(File)
   * @see #detectByContent(Path)
   * @see #detectByContent(InputStream)
   * @see #detectByContent(URL)
   * @see #detectByContent(URI)
   */
  @Nullable
  String detectByContent(@Nonnull final byte[] content);

  /**
   * 检测文件的MIME类型。
   * <p>
   * 此方法使用以下智能策略来检测MIME类型：
   * <ul>
   * <li>首先尝试从文件名扩展名检测可能的MIME类型。</li>
   * <li>如果从文件名扩展名中检测到一个且仅有一个候选MIME类型，且参数{@code alwaysCheckMagic}为
   * {@code false}，则返回该MIME类型。</li>
   * <li>否则，将尝试从文件内容中检测可能的MIME类型。</li>
   * <li>如果从文件名扩展名和文件内容中都没有检测到候选MIME类型，则返回{@code null}。</li>
   * <li>如果从文件名扩展名中没有检测到候选MIME类型，但从文件内容中检测到一个或多个候选MIME类型，
   * 则返回从文件内容中检测到的第一个候选MIME类型。</li>
   * <li>如果从文件名扩展名和文件内容<b>同时</b>检测到任何通用MIME类型，则返回第一个通用MIME类型。</li>
   * <li>否则，返回从文件内容中检测到的第一个候选MIME类型。</li>
   * </ul>
   *
   * @param file
   *     要检测的文件。
   * @param filename
   *     要检测的文件的可选文件名；如果文件名应从{@code file}参数中提取，则为{@code null}。
   * @param alwaysCheckMagic
   *     是否始终检查文件的魔术数字，即使已识别文件名扩展名。
   * @return
   *     检测到的文件MIME类型的规范名称，如果无法检测到MIME类型，则返回{@code null}。
   * @throws IOException
   *     如果发生任何I/O错误。
   * @see #detect(Path, String, boolean)
   * @see #detect(URL, String, boolean)
   * @see #detect(URI, String, boolean)
   * @see #detect(InputStream, String, boolean)
   * @see #detect(byte[], String, boolean)
   */
  @Nullable
  String detect(@Nonnull File file, @Nullable String filename, boolean alwaysCheckMagic)
      throws IOException;

  /**
   * 检测文件的MIME类型。
   * <p>
   * 此函数与调用<code>detect(file, null, isAlwaysCheckMagicByDefault())</code>具有相同效果。
   *
   * @param file
   *     要检测的文件。
   * @return
   *     检测到的文件MIME类型的规范名称，如果无法检测到MIME类型，则返回{@code null}。
   * @throws IOException
   *     如果发生任何I/O错误。
   * @see #detect(File, String, boolean)
   */
  @Nullable
  default String detect(@Nonnull final File file) throws IOException {
    return detect(file, null, isAlwaysCheckMagicByDefault());
  }

  /**
   * 检测文件的MIME类型。
   * <p>
   * 此函数与调用<code>detect(file, filename, isAlwaysCheckMagicByDefault())</code>具有相同效果。
   *
   * @param file
   *     要检测的文件。
   * @param filename
   *     要检测的文件的可选文件名；如果文件名应从{@code file}参数中提取，则为{@code null}。
   * @return
   *     检测到的文件MIME类型的规范名称，如果无法检测到MIME类型，则返回{@code null}。
   * @throws IOException
   *     如果发生任何I/O错误。
   * @see #detect(File, String, boolean)
   */
  @Nullable
  default String detect(@Nonnull final File file, @Nullable final String filename)
      throws IOException {
    return detect(file, filename, isAlwaysCheckMagicByDefault());
  }

  /**
   * 检测文件的MIME类型。
   * <p>
   * 此函数与调用<code>detect(path, null, false)</code>具有相同效果。
   *
   * @param path
   *     要检测的文件路径。
   * @return
   *     检测到的文件MIME类型的规范名称，如果无法检测到MIME类型，则返回{@code null}。
   * @throws IOException
   *     如果发生任何I/O错误。
   * @see #detect(Path, String, boolean)
   */
  @Nullable
  default String detect(@Nonnull final Path path) throws IOException {
    return detect(path, null, false);
  }

  /**
   * 检测文件的MIME类型。
   * <p>
   * 此函数与调用<code>detect(path, filename, isAlwaysCheckMagicByDefault())</code>具有相同效果。
   *
   * @param path
   *     要检测的文件路径。
   * @param filename
   *     要检测的文件的可选文件名；如果文件名应从{@code path}参数中提取，则为{@code null}。
   * @return
   *     检测到的文件MIME类型的规范名称，如果无法检测到MIME类型，则返回{@code null}。
   * @throws IOException
   *     如果发生任何I/O错误。
   * @see #detect(Path, String, boolean)
   */
  @Nullable
  default String detect(@Nonnull final Path path, @Nullable final String filename)
      throws IOException {
    return detect(path, filename, isAlwaysCheckMagicByDefault());
  }

  /**
   * 检测文件的MIME类型。
   * <p>
   * 此方法使用以下智能策略来检测MIME类型：
   * <ul>
   * <li>首先尝试从文件名扩展名检测可能的MIME类型。</li>
   * <li>如果从文件名扩展名中检测到一个且仅有一个候选MIME类型，且参数{@code alwaysCheckMagic}为
   * {@code false}，则函数返回该MIME类型。</li>
   * <li>否则，函数将尝试从文件内容中检测可能的MIME类型。</li>
   * <li>如果从文件名扩展名和文件内容中都没有检测到候选MIME类型，则函数返回{@code null}。</li>
   * <li>如果从文件名扩展名中没有检测到候选MIME类型，但从文件内容中检测到一个或多个候选MIME类型，
   * 则函数返回从文件内容中检测到的第一个候选MIME类型。</li>
   * <li>如果从文件名扩展名和文件内容<b>同时</b>检测到任何通用MIME类型，则函数返回第一个通用MIME类型。</li>
   * <li>否则，函数返回从文件内容中检测到的第一个候选MIME类型。</li>
   * </ul>
   *
   * @param path
   *     要检测的文件路径。
   * @param filename
   *     要检测的文件的可选文件名；如果文件名应从{@code path}参数中提取，则为{@code null}。
   * @param alwaysCheckMagic
   *     是否始终检查文件的魔术数字，即使已识别文件名扩展名。
   * @return
   *     检测到的文件MIME类型的规范名称，如果无法检测到MIME类型，则返回{@code null}。
   * @throws IOException
   *     如果发生任何I/O错误。
   * @see #detect(File, String, boolean)
   * @see #detect(URL, String, boolean)
   * @see #detect(URI, String, boolean)
   * @see #detect(InputStream, String, boolean)
   * @see #detect(byte[], String, boolean)
   */
  @Nullable
  String detect(@Nonnull Path path, @Nullable String filename, boolean alwaysCheckMagic)
      throws IOException;

  /**
   * 检测文件的MIME类型。
   * <p>
   * 此函数与调用<code>detect(url, null, isAlwaysCheckMagic())</code>具有相同效果。
   *
   * @param url
   *     the URL of the file to be detected.
   * @return
   *     the canonical name of the detected MIME-type of the file, or
   *     {@code null} if the MIME-type cannot be detected.
   * @throws IOException
   *     if any I/O error occurs.
   * @see #detect(URL, String, boolean)
   */
  @Nullable
  default String detect(@Nonnull final URL url) throws IOException {
    return detect(url, null, isAlwaysCheckMagicByDefault());
  }

  /**
   * 检测文件的MIME类型。
   * <p>
   * 此函数与调用<code>detect(url, filename, isAlwaysCheckMagicByDefault())</code>具有相同效果。
   *
   * @param url
   *     要检测的文件URL。
   * @param filename
   *     要检测的文件的可选文件名；如果文件名应从{@code url}参数中提取，则为{@code null}。
   * @return
   *     检测到的文件MIME类型的规范名称，如果无法检测到MIME类型，则返回{@code null}。
   * @throws IOException
   *     如果发生任何I/O错误。
   * @see #detect(URL, String, boolean)
   */
  @Nullable
  default String detect(@Nonnull final URL url, @Nullable final String filename)
      throws IOException {
    return detect(url, filename, isAlwaysCheckMagicByDefault());
  }

  /**
   * 检测文件的MIME类型。
   * <p>
   * 此方法使用以下智能策略来检测MIME类型：
   * <ul>
   * <li>首先尝试从文件名扩展名检测可能的MIME类型。</li>
   * <li>如果从文件名扩展名中检测到一个且仅有一个候选MIME类型，且参数{@code alwaysCheckMagic}为
   * {@code false}，则返回该MIME类型。</li>
   * <li>否则，将尝试从文件内容中检测可能的MIME类型。</li>
   * <li>如果从文件名扩展名和文件内容中都没有检测到候选MIME类型，则返回{@code null}。</li>
   * <li>如果从文件名扩展名中没有检测到候选MIME类型，但从文件内容中检测到一个或多个候选MIME类型，
   * 则返回从文件内容中检测到的第一个候选MIME类型。</li>
   * <li>如果从文件名扩展名和文件内容<b>同时</b>检测到任何通用MIME类型，则返回第一个通用MIME类型。</li>
   * <li>否则，返回从文件内容中检测到的第一个候选MIME类型。</li>
   * </ul>
   *
   * @param url
   *     要检测的文件URL。
   * @param filename
   *     要检测的文件的可选文件名；如果文件名应从{@code url}参数中提取，则为{@code null}。
   * @param alwaysCheckMagic
   *     是否始终检查文件的魔术数字，即使已识别文件名扩展名。
   * @return
   *     检测到的文件MIME类型的规范名称，如果无法检测到MIME类型，则返回{@code null}。
   * @throws IOException
   *     如果发生任何I/O错误。
   * @see #detect(File, String, boolean)
   * @see #detect(Path, String, boolean)
   * @see #detect(URI, String, boolean)
   * @see #detect(InputStream, String, boolean)
   * @see #detect(byte[], String, boolean)
   */
  @Nullable
  String detect(@Nonnull URL url, @Nullable String filename, boolean alwaysCheckMagic)
      throws IOException;

  /**
   * 检测文件的MIME类型。
   * <p>
   * 此函数与调用<code>detect(uri, null, isAlwaysCheckMagic())</code>具有相同效果。
   *
   * @param uri
   *     the URI of the file to be detected.
   * @return
   *     the canonical name of the detected MIME-type of the file, or
   *     {@code null} if the MIME-type cannot be detected.
   * @throws IOException
   *     if any I/O error occurs.
   * @see #detect(URI, String, boolean)
   */
  @Nullable
  default String detect(@Nonnull final URI uri) throws IOException {
    return detect(uri, null, isAlwaysCheckMagicByDefault());
  }

  /**
   * 检测文件的MIME类型。
   * <p>
   * 此函数与调用<code>detect(uri, filename, isAlwaysCheckMagicByDefault())</code>具有相同效果。
   *
   * @param uri
   *     要检测的文件URI。
   * @param filename
   *     要检测的文件的可选文件名；如果文件名应从{@code uri}参数中提取，则为{@code null}。
   * @return
   *     检测到的文件MIME类型的规范名称，如果无法检测到MIME类型，则返回{@code null}。
   * @throws IOException
   *     如果发生任何I/O错误。
   * @see #detect(URI, String, boolean)
   */
  @Nullable
  default String detect(@Nonnull final URI uri, @Nullable final String filename)
      throws IOException {
    return detect(uri, filename, isAlwaysCheckMagicByDefault());
  }

  /**
   * 检测文件的MIME类型。
   * <p>
   * 此方法使用以下智能策略来检测MIME类型：
   * <ul>
   * <li>首先尝试从文件名扩展名检测可能的MIME类型。</li>
   * <li>如果从文件名扩展名中检测到一个且仅有一个候选MIME类型，且参数{@code alwaysCheckMagic}为
   * {@code false}，则函数返回该MIME类型。</li>
   * <li>否则，函数将尝试从文件内容中检测可能的MIME类型。</li>
   * <li>如果从文件名扩展名和文件内容中都没有检测到候选MIME类型，则函数返回{@code null}。</li>
   * <li>如果从文件名扩展名中没有检测到候选MIME类型，但从文件内容中检测到一个或多个候选MIME类型，
   * 则函数返回从文件内容中检测到的第一个候选MIME类型。</li>
   * <li>如果从文件名扩展名和文件内容<b>同时</b>检测到任何通用MIME类型，则函数返回第一个通用MIME类型。</li>
   * <li>否则，函数返回从文件内容中检测到的第一个候选MIME类型。</li>
   * </ul>
   *
   * @param uri
   *     要检测的文件URI。
   * @param filename
   *     要检测的文件的可选文件名；如果文件名应从{@code uri}参数中提取，则为{@code null}。
   * @param alwaysCheckMagic
   *     是否始终检查文件的魔术数字，即使已识别文件名扩展名。
   * @return
   *     检测到的文件MIME类型的规范名称，如果无法检测到MIME类型，则返回{@code null}。
   * @throws IOException
   *     如果发生任何I/O错误。
   * @see #detect(File, String, boolean)
   * @see #detect(Path, String, boolean)
   * @see #detect(URL, String, boolean)
   * @see #detect(InputStream, String, boolean)
   * @see #detect(byte[], String, boolean)
   */
  @Nullable
  String detect(@Nonnull URI uri, @Nullable String filename, boolean alwaysCheckMagic)
      throws IOException;

  /**
   * 检测输入流的MIME类型。
   * <p>
   * <b>注意：</b>此函数不会关闭输入流。
   * <p>
   * 此函数与调用<code>detect(input, filename, isAlwaysCheckMagicByDefault())</code>具有相同效果。
   *
   * @param input
   *     要检测的输入流。
   * @param filename
   *     要检测的文件的可选文件名；如果文件名未知，则为{@code null}。如果提供了文件名，
   *     函数将尝试从文件名扩展名猜测MIME类型。注意，此参数必须是不包含任何路径组件的文件名。
   *     使用{@link FilenameUtils#getFilename(File)}或{@link FilenameUtils#getFilename(Path)}
   *     从{@link File}或{@link Path}中提取文件名。
   * @return
   *     检测到的文件MIME类型的规范名称，如果无法检测到MIME类型，则返回{@code null}。
   * @throws IOException
   *     如果发生任何I/O错误。
   * @see #detect(InputStream, String, boolean)
   * @see FilenameUtils#getFilename(File)
   * @see FilenameUtils#getFilename(Path)
   */
  @Nullable
  default String detect(@Nonnull final InputStream input, @Nullable final String filename)
      throws IOException {
    return detect(input, filename, isAlwaysCheckMagicByDefault());
  }

  /**
   * 检测输入流的MIME类型。
   * <p>
   * <b>注意：</b>此函数不会关闭输入流。
   * <p>
   * 此函数使用以下智能策略来检测MIME类型：
   * <ul>
   * <li>首先尝试从文件名扩展名检测可能的MIME类型。</li>
   * <li>如果从文件名扩展名中检测到一个且仅有一个候选MIME类型，且参数{@code alwaysCheckMagic}为
   * {@code true}，则函数返回该MIME类型。</li>
   * <li>否则，函数将尝试从文件内容中检测可能的MIME类型。</li>
   * <li>如果从文件名扩展名和文件内容中都没有检测到候选MIME类型，则函数返回{@code null}。</li>
   * <li>如果从文件名扩展名中没有检测到候选MIME类型，但从文件内容中检测到一个或多个候选MIME类型，
   * 则函数返回从文件内容中检测到的第一个候选MIME类型。</li>
   * <li>如果从文件名扩展名和文件内容<b>同时</b>检测到任何通用MIME类型，则函数返回第一个通用MIME类型。</li>
   * <li>否则，函数返回从文件内容中检测到的第一个候选MIME类型。</li>
   * </ul>
   *
   * @param input
   *     要检测的输入流。
   * @param filename
   *     要检测的文件的可选文件名；如果文件名未知，则为{@code null}。如果提供了文件名，
   *     函数将尝试从文件名扩展名猜测MIME类型。注意，此参数必须是不包含任何路径组件的文件名。
   *     使用{@link FilenameUtils#getFilename(File)}或{@link FilenameUtils#getFilename(Path)}
   *     从{@link File}或{@link Path}中提取文件名。
   * @param alwaysCheckMagic
   *     是否始终检查文件的魔术数字，即使已识别文件名扩展名。
   * @return
   *     检测到的文件MIME类型的规范名称，如果无法检测到MIME类型，则返回{@code null}。
   * @throws IOException
   *     如果发生任何I/O错误。
   * @see #detect(File, String, boolean)
   * @see #detect(Path, String, boolean)
   * @see #detect(URL, String, boolean)
   * @see #detect(URI, String, boolean)
   * @see #detect(byte[], String, boolean)
   * @see #detect(InputStream, String)
   * @see FilenameUtils#getFilename(File)
   * @see FilenameUtils#getFilename(Path)
   */
  @Nullable
  String detect(@Nonnull InputStream input, @Nullable String filename,
      boolean alwaysCheckMagic) throws IOException;

  /**
   * 检测文件的MIME类型。
   * <p>
   * 此函数与调用<code>detect(content, filename, isAlwaysCheckMagic())</code>具有相同效果。
   *
   * @param content
   *     要检测的文件内容的字节数组。
   * @param filename
   *     要检测的文件的可选文件名；如果文件名未知，则为{@code null}。如果提供了文件名，
   *     函数将尝试从文件名扩展名猜测MIME类型。注意，此参数必须是不包含任何路径组件的文件名。
   *     使用{@link FilenameUtils#getFilename(File)}或{@link FilenameUtils#getFilename(Path)}
   *     从{@link File}或{@link Path}中提取文件名。
   * @return
   *     检测到的文件MIME类型的规范名称，如果无法检测到MIME类型，则返回{@code null}。
   * @see #detect(byte[], String, boolean)
   * @see FilenameUtils#getFilename(File)
   * @see FilenameUtils#getFilename(Path)
   */
  @Nullable
  default String detect(@Nonnull final byte[] content, @Nullable final String filename) {
    return detect(content, filename, isAlwaysCheckMagicByDefault());
  }

  /**
   * 检测文件的MIME类型。
   * <p>
   * 此函数使用以下智能策略来检测MIME类型：
   * <ul>
   * <li>首先尝试从文件名扩展名检测可能的MIME类型。</li>
   * <li>如果从文件名扩展名中检测到一个且仅有一个候选MIME类型，且参数{@code alwaysCheckMagic}为
   * {@code true}，则函数返回该MIME类型。</li>
   * <li>否则，函数将尝试从文件内容中检测可能的MIME类型。</li>
   * <li>如果从文件名扩展名和文件内容中都没有检测到候选MIME类型，则函数返回{@code null}。</li>
   * <li>如果从文件名扩展名中没有检测到候选MIME类型，但从文件内容中检测到一个或多个候选MIME类型，
   * 则函数返回从文件内容中检测到的第一个候选MIME类型。</li>
   * <li>如果从文件名扩展名和文件内容<b>同时</b>检测到任何通用MIME类型，则函数返回第一个通用MIME类型。</li>
   * <li>否则，函数返回从文件内容中检测到的第一个候选MIME类型。</li>
   * </ul>
   *
   * @param content
   *     要检测的文件内容的字节数组。
   * @param filename
   *     要检测的文件的可选文件名；如果文件名未知，则为{@code null}。如果提供了文件名，
   *     函数将尝试从文件名扩展名猜测MIME类型。注意，此参数必须是不包含任何路径组件的文件名。
   *     使用{@link FilenameUtils#getFilename(File)}或{@link FilenameUtils#getFilename(Path)}
   *     从{@link File}或{@link Path}中提取文件名。
   * @param alwaysCheckMagic
   *     是否始终检查文件的魔术数字，即使已识别文件名扩展名。
   * @return
   *     检测到的文件MIME类型的规范名称，如果无法检测到MIME类型，则返回{@code null}。
   * @see #detect(File, String, boolean)
   * @see #detect(Path, String, boolean)
   * @see #detect(URL, String, boolean)
   * @see #detect(URI, String, boolean)
   * @see #detect(InputStream, String, boolean)
   * @see FilenameUtils#getFilename(File)
   * @see FilenameUtils#getFilename(Path)
   */
  @Nullable
  String detect(@Nonnull byte[] content, @Nullable String filename, boolean alwaysCheckMagic);
}