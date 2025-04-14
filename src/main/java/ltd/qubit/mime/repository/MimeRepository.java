////////////////////////////////////////////////////////////////////////////////
//
//    Copyright (c) 2022 - 2024.
//    Haixing Hu, Qubit Co. Ltd.
//
//    All rights reserved.
//
////////////////////////////////////////////////////////////////////////////////
package ltd.qubit.mime.repository;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import ltd.qubit.commons.config.Config;
import ltd.qubit.commons.config.error.XmlConfigurationError;
import ltd.qubit.commons.io.FileUtils;
import ltd.qubit.commons.io.FilenameUtils;
import ltd.qubit.commons.io.IoUtils;
import ltd.qubit.commons.text.tostring.ToStringBuilder;
import ltd.qubit.commons.text.xml.DomUtils;
import ltd.qubit.commons.text.xml.XmlException;
import ltd.qubit.commons.text.xml.XmlUtils;
import ltd.qubit.commons.util.Version;
import ltd.qubit.commons.util.VersionSignature;
import ltd.qubit.commons.util.pair.Pair;

import static ltd.qubit.commons.io.InputUtils.readList;
import static ltd.qubit.commons.io.OutputUtils.writeCollection;
import static ltd.qubit.commons.lang.Argument.requireNonNull;
import static ltd.qubit.commons.lang.Argument.requirePositive;

/**
 * MIME类型存储库是所有MIME类型的单例注册表。
 * <p>
 * TODO: 此实现无法处理目录的MIME类型，该目录具有目录树的glob模式。
 *
 * @see <a href='http://standards.freedesktop.org/shared-mime-info-spec/shared-mime-info-spec-0.19.html'>Shared MIME-info Database</a>
 * @see <a href='http://www.freedesktop.org/wiki/Software/shared-mime-info'>shared-mime-info</a>
 * @author 胡海星
 * @repository
 */
@NotThreadSafe
public class MimeRepository implements Serializable {

  private static final long serialVersionUID = - 6880339145050773602L;

  /**
   * 此属性的值决定是否重建MIME注册表并忽略现有的序列化注册表。
   * <p>
   * <table border="1">
   * <caption></caption>
   * <tr>
   * <th>类型</th>
   * <th>数量</th>
   * <th>值</th>
   * <th>必需</th>
   * <th>默认值</th>
   * <th>范围</th>
   * </tr>
   * <tr>
   * <td>boolean</td>
   * <td>1</td>
   * <td>是否重建MIME注册表并忽略现有的序列化注册表。</td>
   * <td>否</td>
   * <td>{@link #DEFAULT_REBUILD}</td>
   * <td></td>
   * </tr>
   * </table>
   *
   * @see #DEFAULT_REBUILD
   */
  public static final String PROPERTY_REBUILD = "ltd.qubit.mime.repository.MimeRepository.rebuild";

  /**
   * 属性{@link #PROPERTY_REBUILD}的默认值。
   *
   * @see #PROPERTY_REBUILD
   */
  public static final boolean DEFAULT_REBUILD = false;

  /**
   * 此属性的值决定是否在重建存储库后保存MIME类型存储库。
   * <p>
   * <table border="1">
   * <caption></caption>
   * <tr>
   * <th>类型</th>
   * <th>数量</th>
   * <th>值</th>
   * <th>必需</th>
   * <th>默认值</th>
   * <th>范围</th>
   * </tr>
   * <tr>
   * <td>boolean</td>
   * <td>1</td>
   * <td>是否在重建存储库后保存MIME类型存储库。</td>
   * <td>否</td>
   * <td>{@link #DEFAULT_SAVE}</td>
   * <td></td>
   * </tr>
   * </table>
   *
   * @see #DEFAULT_SAVE
   */
  public static final String PROPERTY_SAVE = "ltd.qubit.mime.repository.MimeRepository.save";

  /**
   * 属性{@link #PROPERTY_SAVE}的默认值。
   *
   * @see #PROPERTY_SAVE
   */
  public static final boolean DEFAULT_SAVE = false;

  /**
   * 如果此属性的值决定是否默认通过文件名和内容检测MIME类型时检查魔数。
   * <p>
   * <table border="1">
   * <caption></caption>
   * <tr>
   * <th>类型</th>
   * <th>数量</th>
   * <th>值</th>
   * <th>必需</th>
   * <th>默认值</th>
   * <th>范围</th>
   * </tr>
   * <tr>
   * <td>boolean</td>
   * <td>1</td>
   * <td>是否默认通过文件名和内容检测MIME类型时检查魔数。</td>
   * <td>否</td>
   * <td>{@link #DEFAULT_CHECK_MAGIC}</td>
   * <td></td>
   * </tr>
   * </table>
   *
   * @see #DEFAULT_CHECK_MAGIC
   */
  public static final String PROPERTY_CHECK_MAGIC = "ltd.qubit.mime.repository.MimeRepository.checkMagic";

  /**
   * 属性{@link #PROPERTY_CHECK_MAGIC}的默认值。
   *
   * @see #PROPERTY_CHECK_MAGIC
   */
  public static final boolean DEFAULT_CHECK_MAGIC = false;

  /**
   * 此属性的值指定存储序列化MIME类型注册表的文件的绝对路径名。
   * <p>
   * <table border="1">
   * <caption></caption>
   * <tr>
   * <th>类型</th>
   * <th>数量</th>
   * <th>值</th>
   * <th>必需</th>
   * <th>默认值</th>
   * <th>范围</th>
   * </tr>
   * <tr>
   * <td>string</td>
   * <td>1</td>
   * <td>存储序列化MIME类型注册表的文件的绝对路径名。</td>
   * <td>否</td>
   * <td>{@link #DEFAULT_SERIALIZATION}</td>
   * <td></td>
   * </tr>
   * </table>
   *
   * @see #DEFAULT_SERIALIZATION
   */
  public static final String PROPERTY_SERIALIZATION = "ltd.qubit.mime.repository.MimeRepository.serialization";

  /**
   * 属性{@link #PROPERTY_SERIALIZATION}的默认值。
   *
   * @see #PROPERTY_SERIALIZATION
   */
  public static final String DEFAULT_SERIALIZATION = "${user.home}/.qubit/MimeRepository.ser";

  /**
   * 此属性的值指定包含MIME类型信息的数据库文件的绝对路径名。
   * <p>
   * <table border="1">
   * <caption></caption>
   * <tr>
   * <th>类型</th>
   * <th>数量</th>
   * <th>值</th>
   * <th>必需</th>
   * <th>默认值</th>
   * <th>范围</th>
   * </tr>
   * <tr>
   * <td>string</td>
   * <td>1</td>
   * <td>包含MIME类型信息的数据库文件的绝对路径名。</td>
   * <td>否</td>
   * <td>未定义</td>
   * <td></td>
   * </tr>
   * </table>
   */
  public static final String PROPERTY_DATABASE = "ltd.qubit.mime.repository.MimeRepository.database";

  /**
   * 此属性的值指定默认的二进制MIME类型名称。
   * <p>
   * <table border="1">
   * <caption></caption>
   * <tr>
   * <th>类型</th>
   * <th>数量</th>
   * <th>值</th>
   * <th>必需</th>
   * <th>默认值</th>
   * <th>范围</th>
   * </tr>
   * <tr>
   * <td>string</td>
   * <td>1</td>
   * <td>默认的二进制MIME类型名称。</td>
   * <td>否</td>
   * <td>"application/octet-stream"</td>
   * <td></td>
   * </tr>
   * </table>
   */
  public static final String PROPERTY_DEFAULT_BINARY = "ltd.qubit.mime.repository.MimeRepository.defaultBinary";

  /**
   * 此属性的值指定默认的文本MIME类型名称。
   * <p>
   * <table border="1">
   * <caption></caption>
   * <tr>
   * <th>类型</th>
   * <th>数量</th>
   * <th>值</th>
   * <th>必需</th>
   * <th>默认值</th>
   * <th>范围</th>
   * </tr>
   * <tr>
   * <td>string</td>
   * <td>1</td>
   * <td>默认的文本MIME类型名称。</td>
   * <td>否</td>
   * <td>"text/plain"</td>
   * <td></td>
   * </tr>
   * </table>
   */
  public static final String PROPERTY_DEFAULT_TEXT = "ltd.qubit.mime.repository.MimeRepository.defaultText";

  public static final String ROOT_NODE = "mime-info";

  public static final Version VERSION = new Version(0, 1, 0, 0);

  private static final VersionSignature SIGNATURE = new VersionSignature(serialVersionUID, VERSION);

  private static volatile MimeRepository instance = null;

  /**
   * 获取MIME类型存储库的单例实例。
   *
   * @return MIME类型存储库的单例实例。
   */
  public static MimeRepository getInstance() {
    // use the double-checked locking trick
    if (instance == null) {
      synchronized (MimeRepository.class) {
        if (instance == null) {
          final Config config = MimeConfig.get();
          instance = new MimeRepository(config);
        }
      }
    }
    return instance;
  }

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private Config config;
  private List<MimeType> mimeList;
  private Map<String, MimeType> mimeNameMap;
  private Multimap<String, Pair<MimeGlob, MimeType>> literalGlobMap;
  private Multimap<String, Pair<MimeGlob, MimeType>> extensionGlobMap;
  private List<Pair<MimeGlob, MimeType>> otherGlobList;
  private boolean alwaysCheckMagic;
  private int maxTestBytes;

  /**
   * 构造一个默认的MIME类型存储库。
   */
  private MimeRepository() {
    mimeList = new LinkedList<>();
    mimeNameMap = new HashMap<>();
    literalGlobMap = LinkedHashMultimap.create();
    extensionGlobMap = LinkedHashMultimap.create();
    otherGlobList = new LinkedList<>();
    maxTestBytes = 0;
    alwaysCheckMagic = false;
  }

  /**
   * 构造一个具有指定配置的MIME类型存储库。
   *
   * @param config
   *     指定的配置。
   */
  private MimeRepository(final Config config) {
    this();
    this.config = config;
    alwaysCheckMagic = config.getBoolean(PROPERTY_CHECK_MAGIC, DEFAULT_CHECK_MAGIC);
    // load or rebuild the repository
    if (config.getBoolean(PROPERTY_REBUILD, DEFAULT_REBUILD)) {
      // need to rebuild the repository
      rebuild(config);
    } else { // try to load the serialized repository
      final String serialization = config.getString(PROPERTY_SERIALIZATION, DEFAULT_SERIALIZATION);
      final File file = new File(serialization);
      try {
        load(file);
      } catch (final IOException e) {
        logger.warn("Failed to load the saved MIME-type repository, rebuild it： {}",
            e.toString());
        rebuild(config);
      }
    }
  }

  /**
   * 重建MIME类型存储库。
   */
  public void rebuild() {
    rebuild(config);
  }

  /**
   * 从文件加载MIME类型存储库。
   *
   * @param file
   *     要加载的文件。
   * @throws IOException
   *     如果发生I/O错误。
   */
  protected void load(final File file) throws IOException {
    logger.info("Loading the serialized MIME-type repository from {} ...", file);
    final long start = System.currentTimeMillis();
    InputStream in = null;
    try {
      in = new FileInputStream(file);
      in = new BufferedInputStream(in);
      SIGNATURE.verify(in);
      mimeList = readList(MimeType.class, in, false, false, mimeList);
      if (mimeList != null) {
        for (final MimeType mime : mimeList) {
          this.addMimeType(mime);
        }
      }
    } finally {
      IoUtils.closeQuietly(in);
    }
    final long end = System.currentTimeMillis();
    logger.info("Successfully load the serialized MIME-type repository in {} milliseconds.", end - start);
    logStatistics();
  }

  /**
   * 记录MIME类型存储库的统计信息。
   */
  private void logStatistics() {
    logger.info("Totally {} MIME-type entries.", mimeList.size());
    logger.info("Totally {} literal globs.", literalGlobMap.size());
    logger.info("Totally {} extension globs.", extensionGlobMap.size());
    logger.info("Totally {} other globs.", otherGlobList.size());
  }

  /**
   * 将MIME类型存储库存储到文件。
   *
   * @param file
   *     要存储到的文件。
   * @throws IOException
   *     如果发生I/O错误。
   */
  protected void store(final File file) throws IOException {
    logger.info("Storing the serialized MIME-type repository to {} ...", file);
    final long start = System.currentTimeMillis();
    OutputStream out = null;
    try {
      FileUtils.ensureParentExist(file);
      out = new FileOutputStream(file);
      out = new BufferedOutputStream(out);
      SIGNATURE.sign(out);
      writeCollection(MimeType.class, out, mimeList);
    } finally {
      IoUtils.closeQuietly(out);
    }
    final long end = System.currentTimeMillis();
    logger.info("Successfully store the serialized MIME-type " +
    		"repository in {} milliseconds.", end - start);
  }

  /**
   * 使用指定的配置重建MIME类型存储库。
   *
   * @param config
   *     指定的配置。
   */
  protected void rebuild(final Config config) {
    final String database = config.getString(PROPERTY_DATABASE);
    logger.info("Rebuilding the MIME-type repository ...");
    final long start = System.currentTimeMillis();
    try {
      final Document doc = XmlUtils.parse(database, MimeRepository.class);
      final Element root = doc.getDocumentElement();
      fromXml(root);
    } catch (final XmlException e) {
      logger.error( "Failed to parse the MIME-type database: {}", e.getMessage(), e);
      throw new XmlConfigurationError(database, e);
    }
    final long end = System.currentTimeMillis();
    logger.info("Successfully rebuild MIME-type repository in {} milliseconds.", end - start);
    logStatistics();
    if (config.getBoolean(PROPERTY_SAVE, DEFAULT_SAVE)) {
      final String serialization = config.getString(PROPERTY_SERIALIZATION);
      try {
        store(new File(serialization));
      } catch (final IOException e) {
        logger.warn( "Failed to store the MIME-type repository: {}", e.getMessage());
        // ignore the IOException
      }
    }
  }

  /**
   * 根据名称获取MIME类型。
   *
   * @param name
   *     MIME类型的名称。
   * @return 具有指定名称的MIME类型，如果没有找到，则返回null。
   */
  public MimeType get(final String name) {
    return mimeNameMap.get(normalizeName(name));
  }

  /**
   * 获取所有MIME类型的列表。
   *
   * @return 所有MIME类型的不可修改列表。
   */
  public List<MimeType> getAll() {
    return Collections.unmodifiableList(mimeList);
  }

  /**
   * 获取测试字节的最大数量。
   * <p>
   * 这是所有魔数匹配器所需的最大字节数，用于确定读取缓冲区的大小。
   *
   * @return 测试字节的最大数量。
   */
  public int getMaxTestBytes() {
    return maxTestBytes;
  }

  /**
   * 通过文件名检测MIME类型。
   * <p>
   * 检测过程应遵循以下规则：
   * <p>
   * 应用程序必须以大小写不敏感的方式匹配glob模式，除非case-sensitive属性设置为true。
   * 这是为了使main.C被识别为C++文件，而IMAGE.GIF仍然使用*.gif模式。
   * <p>
   * 如果多个相同权重的模式匹配，则应该使用最长的模式。特别是，具有多个扩展名的文件
   * （如Data.tar.gz）必须优先匹配最长的扩展名序列（例如，优先使用'*.tar.gz'而不是'*.gz'）。
   * 字面量模式（如'Makefile'）必须优先于所有其他模式匹配。建议将以`*.`开头且不包含
   * 其他特殊字符（`*?[`）的模式放入哈希表中以提高查找效率，因为这涵盖了大多数模式。
   * 因此，这种形式的模式应该在其他通配符模式之前匹配。
   * <p>
   * 如果一个匹配模式由两个或多个MIME类型提供，应用程序不应该依赖其中任何一个。
   * 它们应该使用魔数（见下文）来检测实际的MIME类型。这对于处理像Ogg或AVI这样的
   * 容器格式尤为必要，这些格式将各种视频和/或音频编码的数据映射到一个扩展名。
   * <p>
   * 可能有多个规则映射到同一类型。它们应该全部合并。如果同一模式被定义两次，
   * 则必须按照规则来源的目录进行排序，如上所述。
   *
   * @param filename
   *     要检测其MIME类型的文件名。
   * @return 与指定文件名匹配的MIME类型列表，按匹配模式的权重降序排列；
   *     如果没有找到匹配项，则返回null。
   */
  @Nullable
  public List<MimeType> detectByFilename(final String filename) {
    requireNonNull("filename", filename);
    // get the exact filename, convert it to lowercase
    final String exactFilename = FilenameUtils
        .getFilenameFromPath(filename)
        .toLowerCase();
    final GlobDetectionResult result = new GlobDetectionResult();
    // First, find in the exact glob patterns.
    if (literalGlobMap.containsKey(exactFilename)) {
      final Collection<Pair<MimeGlob, MimeType>> pairs = literalGlobMap.get(exactFilename);
      for (final Pair<MimeGlob, MimeType> pair : pairs) {
        result.compareAdd(pair.first, pair.second);
      }
    }
    // Second, find in the extension glob patterns.
    // Note that some filename may have more than one possible extension,
    // for example, "file.tar.gz", has both the extension "tar.gz" and "gz".
    int pos = exactFilename.indexOf('.');
    while (pos >= 0) {
      final String ext = exactFilename.substring(pos + 1);
      if (extensionGlobMap.containsKey(ext)) {
        final Collection<Pair<MimeGlob, MimeType>> pairs = extensionGlobMap.get(ext);
        for (final Pair<MimeGlob, MimeType> pair : pairs) {
          result.compareAdd(pair.first, pair.second);
        }
      }
      // get the next extension
      pos = exactFilename.indexOf('.', pos + 1);
    }
    // Third, find in the other glob patterns
    for (final Pair<MimeGlob, MimeType> pair : otherGlobList) {
      final MimeGlob glob = pair.first;
      if (glob.matches(exactFilename)) {
        result.compareAdd(glob, pair.second);
      }
    }
    if (result.list.isEmpty()) {
      return null;
    } else {
      return result.list;
    }
  }

  /**
   * 通过内容检测MIME类型。
   *
   * @param buffer
   *     包含要检测其MIME类型的内容的缓冲区。
   * @param n
   *     缓冲区中要检测的字节数。
   * @return 与指定内容匹配的MIME类型列表，按魔数匹配器的优先级降序排列；
   *     如果没有找到匹配项，则返回null。
   */
  @Nullable
  public List<MimeType> detectByContent(final byte[] buffer, final int n) {
    requireNonNull("buffer", buffer);
    requirePositive("n", n);
    return Utils.detectByMagic(buffer, n, mimeList);
  }

  /**
   * 通过输入流的内容检测MIME类型。
   * <p>
   * 注意：此方法将不会关闭指定的输入流。
   * 
   * @param markSupportedInput
   *     支持标记的输入流，包含要检测其MIME类型的内容。注意：此输入流必须支持标记，并且此方法
   *     将在读取内容之前标记输入流，读取内容后重置输入流。它不会关闭输入流。
   * @return
   *     与指定内容匹配的MIME类型列表，按魔数匹配器的优先级降序排列；如果没有找到匹配项，
   *     则返回null。
   */
  @Nullable
  public List<MimeType> detectByContent(final InputStream markSupportedInput)
      throws IOException {
    requireNonNull("input", markSupportedInput);
    if (!markSupportedInput.markSupported()) {
      throw new IllegalArgumentException("The input stream must support marking.");
    }
    final byte[] buffer = new byte[maxTestBytes];
    markSupportedInput.mark(maxTestBytes);
    final int nBytes = markSupportedInput.read(buffer, 0, maxTestBytes);
    markSupportedInput.reset();
    return Utils.detectByMagic(buffer, nBytes, mimeList);
  }

  /**
   * 通过文件名和内容检测MIME类型。
   *
   * @param filename
   *     要检测其MIME类型的文件名。
   * @param buffer
   *     包含要检测其MIME类型的内容的缓冲区。
   * @param n
   *     缓冲区中要检测的字节数。
   * @return 检测结果，即与指定文件名和内容匹配的MIME类型列表，按匹配规则的权重/优先级降序排列；
   *     如果没有找到匹配项，则返回null。
   */
  @Nullable
  public List<MimeType> detect(final String filename, final byte[] buffer, final int n) {
    return detect(filename, buffer, n, alwaysCheckMagic);
  }

  /**
   * 通过文件名和内容检测MIME类型。
   *
   * @param filename
   *     要检测其MIME类型的文件名。
   * @param buffer
   *     包含要检测其MIME类型的内容的缓冲区。
   * @param nBytes
   *     缓冲区中要检测的字节数。
   * @param alwaysCheckMagic
   *     是否总是检查魔数。如果为true，即使已经通过文件名匹配到MIME类型，也会通过内容检查魔数；
   *     如果为false，只有在通过文件名无法匹配到MIME类型时，才会通过内容检查魔数。
   * @return 检测结果，即与指定文件名和内容匹配的MIME类型列表，按匹配规则的权重/优先级降序排列；
   *     如果没有找到匹配项，则返回null。
   */
  @Nullable
  public List<MimeType> detect(final String filename, final byte[] buffer,
      final int nBytes, final boolean alwaysCheckMagic) {
    final List<MimeType> list = detectByFilename(filename);
    if (list == null) {
      return Utils.detectByMagic(buffer, nBytes, mimeList);
    } else if ((!alwaysCheckMagic) && (list.size() == 1)) {
      return list;
    }
    // check each MIME-type in the mimeList returned by detection on filename,
    // test whether its or its super types' magic match the buffer.
    return Utils.checkByMagic(buffer, nBytes, list);
  }

  /**
   * 通过文件名和输入流的内容检测MIME类型。
   * <p>
   * 注意：此方法将不会关闭指定的输入流。
   *
   * @param filename
   *     要检测其MIME类型的文件名。
   * @param input
   *     包含要检测其MIME类型的内容的输入流。
   * @return 检测结果，即与指定文件名和内容匹配的MIME类型列表，按匹配规则的权重/优先级降序排列；
   *     如果没有找到匹配项，则返回null。
   * @throws IOException
   *     如果发生I/O错误。
   */
  @Nullable
  public List<MimeType> detect(final String filename, final InputStream input)
      throws IOException {
    return detect(filename, input, alwaysCheckMagic);
  }

  /**
   * 通过文件名和输入流的内容检测MIME类型。
   * <p>
   * 注意：此方法将不会关闭指定的输入流。
   *
   * @param filename
   *     要检测其MIME类型的文件名。
   * @param input
   *     包含要检测其MIME类型的内容的输入流。
   * @param alwaysCheckMagic
   *     是否总是检查魔数。如果为true，即使已经通过文件名匹配到MIME类型，也会通过内容检查魔数；
   *     如果为false，只有在通过文件名无法匹配到MIME类型时，才会通过内容检查魔数。
   * @return 检测结果，即与指定文件名和内容匹配的MIME类型列表，按匹配规则的权重/优先级降序排列；
   *     如果没有找到匹配项，则返回null。
   * @throws IOException
   *     如果发生I/O错误。
   */
  @Nullable
  public List<MimeType> detect(final String filename, final InputStream input,
      final boolean alwaysCheckMagic) throws IOException {
    if (!input.markSupported()) {
      throw new IllegalArgumentException("The input stream must support marking.");
    }
    final List<MimeType> list = detectByFilename(filename);
    if (list == null) {
      final byte[] buffer = new byte[maxTestBytes];
      input.mark(maxTestBytes);
      final int nBytes = input.read(buffer, 0, maxTestBytes);
      input.reset();
      return Utils.detectByMagic(buffer, nBytes, mimeList);
    } else if ((!alwaysCheckMagic) && (list.size() == 1)) {
      return list;
    }
    // check each MIME-type in the mimeList returned by detection on filename,
    // test whether its or its super types' magic match the buffer.
    //
    final byte[] buffer = new byte[maxTestBytes];
    input.mark(maxTestBytes);
    final int nBytes = input.read(buffer, 0, maxTestBytes);
    input.reset();
    final List<MimeType> result = Utils.checkByMagic(buffer, nBytes, list);
    // note that if the mimeList has only one candidate, but that candidate
    // has no magic rule, the checkByMagic() may also return null. So we
    // need to deal with this case.
    if (result == null) {
      if (list.size() == 1) {
        return list;
      } else {
        return null;
      }
    } else {
      return result;
    }
  }

  /**
   * 检测文件的MIME类型。
   *
   * @param file
   *     要检测其MIME类型的文件。
   * @return 检测结果，即与指定文件匹配的MIME类型列表，按匹配规则的权重/优先级降序排列；
   *     如果没有找到匹配项，则返回null。
   * @throws IOException
   *     如果发生I/O错误。
   */
  @Nullable
  public List<MimeType> detect(final File file) throws IOException {
    try (final InputStream in = new BufferedInputStream(new FileInputStream(file))) {
      return detect(file.getName(), in);
    }
  }

  /**
   * 检测文件的第一个MIME类型名称。
   *
   * @param file
   *     要检测其MIME类型的文件。
   * @return 与指定文件匹配的第一个MIME类型的名称；如果没有找到匹配项，则返回null。
   * @throws IOException
   *     如果发生I/O错误。
   */
  @Nullable
  public String detectFirstName(final File file) throws IOException {
    final List<MimeType> mimes = detect(file);
    if (mimes == null || mimes.isEmpty()) {
      return null;
    } else {
      return mimes.get(0).getName();
    }
  }

  /**
   * 清除此存储库的内容。
   */
  private void clear() {
    this.mimeList.clear();
    this.mimeNameMap.clear();
    this.literalGlobMap.clear();
    this.extensionGlobMap.clear();
    this.otherGlobList.clear();
    this.maxTestBytes = 0;
    this.alwaysCheckMagic = false;
  }

  /**
   * 与另一个存储库交换内容。
   *
   * @param that
   *     要交换内容的存储库。
   */
  private void swap(final MimeRepository that) {
    assert (this != that);
    final List<MimeType> tempMimeList = that.mimeList;
    that.mimeList = this.mimeList;
    this.mimeList = tempMimeList;

    final Map<String, MimeType> tempMimeNameMap = that.mimeNameMap;
    that.mimeNameMap = this.mimeNameMap;
    this.mimeNameMap = tempMimeNameMap;

    Multimap<String, Pair<MimeGlob, MimeType>> tempGlobMap = that.literalGlobMap;
    that.literalGlobMap = this.literalGlobMap;
    this.literalGlobMap = tempGlobMap;

    tempGlobMap = that.extensionGlobMap;
    that.extensionGlobMap = this.extensionGlobMap;
    this.extensionGlobMap = tempGlobMap;

    final List<Pair<MimeGlob, MimeType>> tempGlobList = that.otherGlobList;
    that.otherGlobList = this.otherGlobList;
    this.otherGlobList = tempGlobList;

    final int tempMaxTestBytes = that.maxTestBytes;
    that.maxTestBytes = this.maxTestBytes;
    this.maxTestBytes = tempMaxTestBytes;

    final boolean tempAlwaysCheckMagic = that.alwaysCheckMagic;
    that.alwaysCheckMagic = this.alwaysCheckMagic;
    this.alwaysCheckMagic = tempAlwaysCheckMagic;
  }

  /**
   * 添加MIME类型到此存储库。
   *
   * @param mime
   *     要添加的MIME类型。
   */
  private void addMimeType(final MimeType mime) {
    // put the name to the m_mimeNameMap
    final String name = mime.getName();
    final String normalizedName = normalizeName(name);
    if (mimeNameMap.containsKey(normalizedName)) {
      logger.warn("The MIME-type name {} already existed. "
          + "The newer will override the older.", name);
    }
    logger.trace("Add the MIME-Type name: {}", name);
    mimeNameMap.put(normalizedName, mime);
    // add all aliases of the mime to the newMimeNameMap
    for (final String alias : mime.getAliases()) {
      final String normalizedAlias = alias.toLowerCase();
      if (mimeNameMap.containsKey(normalizedAlias)) {
        logger.warn("The MIME-type alias {} already existed. "
            + "The newer will override the older.", alias);
      }
      logger.trace("Add the MIME-Type alias: {}", alias);
      mimeNameMap.put(normalizedAlias, mime);
    }
    // add the glob pattern to the map
    for (final MimeGlob glob : mime.getGlobs()) {
      String pattern = glob.getPattern();
      // check whether the pattern is a special pattern
      if (Utils.isExtensionPattern(pattern)) {
        // found a extension pattern in the form of "*.ext"
        pattern = pattern.substring(2);
        logger.trace("Add a extension pattern: {}", pattern);
        extensionGlobMap.put(pattern, new Pair<>(glob, mime));
      } else if (Utils.isLiteralPattern(pattern)) {
        // found a literal pattern
        logger.trace("Add a literal pattern: {}", pattern);
        literalGlobMap.put(pattern, new Pair<>(glob, mime));
      } else {
        // add to the normal glob list
        logger.trace("Add a glob pattern: {}", pattern);
        otherGlobList.add(new Pair<>(glob, mime));
      }
    }
    // calculate the max test bytes
    for (final MimeMagic magic : mime.getMagics()) {
      // update the max test bytes
      final int bytes = magic.getMaxTestBytes();
      if (maxTestBytes < bytes) {
        logger.trace("Set the max test bytes to: {}", bytes);
        maxTestBytes = bytes;
      }
    }
  }

  /**
   * 从XML元素解析MIME类型存储库。
   *
   * @param root
   *     包含MIME类型存储库信息的XML元素。
   * @throws XmlException
   *     如果发生XML解析错误。
   */
  public void fromXml(final Element root) throws XmlException {
    // first deserialize the XML and get a name map of MIME-types
    logger.info("Deserialize MimeRepository from XML ...");
    DomUtils.checkNode(root, ROOT_NODE);
    final NodeList nodeList = root.getChildNodes();
    if (nodeList.getLength() == 0) {
      clear();
      return;
    }
    final MimeRepository temp = new MimeRepository();
    final int nodeCount = nodeList.getLength();
    for (int i = 0; i < nodeCount; ++i) {
      final Node node = nodeList.item(i);
      if (node.getNodeType() != Node.ELEMENT_NODE) {
        continue; // skip non-element nodes
      }
      // deserialize a new MIME-type
      final MimeType mime = MimeTypeXmlSerializer.INSTANCE.deserialize((Element)node);
      logger.info("Parsed a MIME-Type {}", mime.getName());
      // add to the newMimeList
      temp.mimeList.add(mime);
      // collect glob information
      temp.addMimeType(mime);
    }
    // set the alwaysCheckMagic
    final Config config = MimeConfig.get();
    temp.alwaysCheckMagic = config.getBoolean(PROPERTY_CHECK_MAGIC, DEFAULT_CHECK_MAGIC);
    // then swap this with temp;
    swap(temp);
    logger.info("Successfully deserialize MimeRepository from XML.");
  }

  /**
   * 将MIME类型存储库转换为XML元素。
   *
   * @param doc
   *     用于创建XML元素的文档。
   * @return 包含MIME类型存储库信息的XML元素。
   * @throws XmlException
   *     如果发生XML生成错误。
   */
  public Element toXml(final Document doc) throws XmlException {
    logger.trace("Serializing MimeRepository into XML ...");
    final Element result = doc.createElement(ROOT_NODE);
    for (final MimeType mime : mimeList) {
      final Element node = MimeTypeXmlSerializer.INSTANCE.serialize(doc, mime);
      result.appendChild(node);
    }
    logger.trace("Successfully serialize MimeRepository into XML.");
    return result;
  }

  /**
   * 标准化MIME类型名称。
   *
   * @param name
   *     要标准化的MIME类型名称。
   * @return 标准化后的MIME类型名称。
   */
  protected String normalizeName(final String name) {
    return name.toLowerCase();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this)
               .append("mimeList", mimeList)
               .toString();
  }
}