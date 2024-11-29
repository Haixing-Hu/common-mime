////////////////////////////////////////////////////////////////////////////////
//
//    Copyright (c) 2022 - 2024.
//    Haixing Hu, Qubit Co. Ltd.
//
//    All rights reserved.
//
////////////////////////////////////////////////////////////////////////////////
package ltd.qubit.mime.repository;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ltd.qubit.commons.config.impl.XmlConfig;
import ltd.qubit.commons.lang.ArrayUtils;
import ltd.qubit.commons.text.xml.XmlException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import static ltd.qubit.mime.repository.MimeConfig.PROPERTY_RESOURCE;

/**
 * Unit test of the {@link MimeRepository} class.
 *
 * @author Haixing Hu
 */
public class MimeRepositoryTest {

  public static class TestData {
    String   fileName;
    String   mimeName;
    String[] detectByName;
    String[] detectByMagic;
    String[] detectByBoth;

    TestData(final String fileName, final String mimeName, final String[] detectByName,
        final String[] detectByMagic, final String[] detectByBoth) {
      this.fileName = fileName;
      this.mimeName = mimeName;
      this.detectByName = detectByName;
      this.detectByMagic = detectByMagic;
      this.detectByBoth = detectByBoth;
    }

    static boolean matches(final MimeRepository repository,
        final List<MimeType> mimeList, final String[] mimeNameList) {
      if (mimeNameList == null) {
        if (mimeList == null) {
          return true;
        } else {
          System.err.println("Do not match: "
              + mimeList.stream()
                        .map(MimeType::getName)
                        .collect(Collectors.toList())
              + "\n" + ArrayUtils.toString(mimeNameList));
          return false;
        }
      } else if (mimeList == null) {
        System.err.println("Do not match: "
            + mimeList.stream()
                      .map(MimeType::getName)
                      .collect(Collectors.toList())
            + "\n" + ArrayUtils.toString(mimeNameList));
        return false;
      }
      if (mimeList.size() != mimeNameList.length) {
        System.err.println("Do not match: "
            + mimeList.stream()
                      .map(MimeType::getName)
                      .collect(Collectors.toList())
            + "\n" + ArrayUtils.toString(mimeNameList));
        return false;
      }
      for (int i = 0; i < mimeNameList.length; ++i) {
        final MimeType mime = repository.get(mimeNameList[i]);
        if (! mimeList.contains(mime)) {
          System.err.println("Do not match: "
              + mimeList.stream()
                        .map(MimeType::getName)
                        .collect(Collectors.toList())
              + "\n" + ArrayUtils.toString(mimeNameList));
          return false;
        }
      }
      return true;
    }
  }

  public static final String CONFIGURATION = "MimeRepositoryTest.xml";

  public static final String TEST_DATA_DIR = "testdata";

  // files to test and their expected MIME-types
  public static final TestData[] TEST_DATA = {
    new TestData("test-lzw.tif",
        "image/tiff",
        new String[]{"image/tiff"},
        new String[]{"image/tiff"},
        new String[]{"image/tiff"}),

    new TestData("test-lzw.tif",
        "image/tiff",
        new String[]{"image/tiff"},
        new String[]{"image/tiff"},
        new String[]{"image/tiff"}),

    new TestData("test-packbits.tif",
        "image/tiff",
        new String[]{"image/tiff"},
        new String[]{"image/tiff"},
        new String[]{"image/tiff"}),

    new TestData("test-uncomp.tif",
        "image/tiff",
        new String[]{"image/tiff"},
        new String[]{"image/tiff"},
        new String[]{"image/tiff"}),

    new TestData("test.PNG",
        "image/png",
        new String[]{"image/png"},
        new String[]{"image/png"},
        new String[]{"image/png"}),

    new TestData("test.dvi",
        "application/x-dvi",
        new String[]{"application/x-dvi"},
        new String[]{"application/x-dvi"},
        new String[]{"application/x-dvi"}),

    new TestData("test.eps",
        "image/x-eps",
        new String[]{"image/x-eps"},
        new String[]{"image/x-eps"},
        new String[]{"image/x-eps"}),

    new TestData("test.eps.bz2",
        "image/x-bzeps",
        new String[]{"image/x-bzeps"},
        new String[]{"application/x-bzip"},
        new String[]{"image/x-bzeps"}),

    new TestData("test.eps.gz",
        "image/x-gzeps",
        new String[]{"image/x-gzeps"},
        new String[]{"application/x-gzip"},
        new String[]{"image/x-gzeps"}),

    new TestData("test.gif",
        "image/gif",
        new String[]{"image/gif"},
        new String[]{"image/gif"},
        new String[]{"image/gif"}),

    new TestData("test.html",
        "text/html",
        new String[]{"text/html"},
        new String[]{"text/html"},
        new String[]{"text/html"}),

    new TestData("test.jpg",
        "image/jpeg",
        new String[]{"image/jpeg"},
        new String[]{"image/jpeg"},
        new String[]{"image/jpeg"}),

    new TestData("test.lyx",
        "application/x-lyx",
        new String[]{"application/x-lyx"},
        new String[]{"application/x-lyx"},
        new String[]{"application/x-lyx"}),

    new TestData("test.mp3",
        "audio/mpeg",
        new String[]{"audio/mpeg"},
        new String[]{"audio/mpeg"},
        new String[]{"audio/mpeg"}),

    new TestData("test.ogg",
        "audio/x-vorbis+ogg",
        new String[]{"audio/ogg", "video/ogg", "audio/x-vorbis+ogg", "audio/x-flac+ogg",
        "audio/x-speex+ogg", "video/x-theora+ogg"},
        new String[]{"audio/x-vorbis+ogg"},
        new String[]{"audio/x-vorbis+ogg"}),

    new TestData("test.pdf",
        "application/pdf",
        new String[]{"application/pdf"},
        new String[]{"application/pdf"},
        new String[]{"application/pdf"}),

    new TestData("test.ps",
        "application/postscript",
        new String[]{"application/postscript"},
        new String[]{"application/postscript"},
        new String[]{"application/postscript"}),

    new TestData("test.bmp",
        "image/bmp",
        new String[]{"image/bmp"},
        new String[]{"image/bmp"},
        new String[]{"image/bmp"}),

    new TestData("test.tex",
        "text/x-tex",
        new String[]{"text/x-tex"},
        new String[]{"text/x-tex", "text/x-matlab"},
        new String[]{"text/x-tex"}),

    new TestData("test.txt",
        "text/plain",
        new String[]{"text/plain"},
        null,
        new String[]{"text/plain"}),

    new TestData("test_Lzw_TIFF.sion",
        "image/tiff",
        null,
        new String[]{"image/tiff"},
        new String[]{"image/tiff"}),

    new TestData("test_Ogg.sion",
        "audio/x-vorbis+ogg",
        null,
        new String[]{"audio/x-vorbis+ogg"},
        new String[]{"audio/x-vorbis+ogg"}),

    new TestData("test_PNG.sion",
        "image/png",
        null,
        new String[]{"image/png"},
        new String[]{"image/png"}),

    new TestData("test.rtf",
        "application/rtf",
        new String[]{"application/rtf"},
        new String[]{"application/rtf"},
        new String[]{"application/rtf"}),

    new TestData("test.doc",
        "application/msword",
        new String[]{"application/msword"},
        new String[]{"application/msword"},
        new String[]{"application/msword"}),

    new TestData("test.xls",
        "application/vnd.ms-excel",
        new String[]{"application/vnd.ms-excel"},
        new String[]{"application/vnd.ms-excel"},
        new String[]{"application/vnd.ms-excel"}),

    new TestData("test.xps",
        "application/vnd.ms-xpsdocument",
        new String[]{"application/vnd.ms-xpsdocument"},
        new String[]{"application/zip"},
        new String[]{"application/vnd.ms-xpsdocument"}),

    new TestData("test.odt",
        "application/vnd.oasis.opendocument.text",
        new String[]{"application/vnd.oasis.opendocument.text"},
        new String[]{"application/vnd.oasis.opendocument.text"},
        new String[]{"application/vnd.oasis.opendocument.text"}),

    //  note that the MS Word template document is a sub-type of MS Word
    //  document, and its MIME-type entry has no magic rule. But this
    //  should be correctly detected by the program.
    new TestData("test.dot",
        "application/msword-template",
        new String[]{"application/msword-template", "text/vnd.graphviz"},
        new String[]{"application/msword"},
        new String[]{"application/msword-template"}),

        //  note that the newest MS Word document is in fact a ZIP file of
        //  a set of XML files.
    new TestData("test.docx",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        new String[]{"application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
        new String[]{"application/zip"},
        new String[]{"application/vnd.openxmlformats-officedocument.wordprocessingml.document"}),

    new TestData("test.docm", "application/vnd.ms-word.document.macroEnabled.12",
        new String[]{"application/vnd.ms-word.document.macroEnabled.12"},
        new String[]{"application/zip"},
        new String[]{"application/vnd.ms-word.document.macroEnabled.12"}),

    new TestData("test.dotx", "application/vnd.openxmlformats-officedocument.wordprocessingml.template",
        new String[]{"application/vnd.openxmlformats-officedocument.wordprocessingml.template"},
        new String[]{"application/zip"},
        new String[]{"application/vnd.openxmlformats-officedocument.wordprocessingml.template"}),

    new TestData("test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        new String[]{"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
        new String[]{"application/zip"},
        new String[]{"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"}),

          // FIXME: the PSD cannot be detected by magic
   new TestData("large_image.psd", "image/vnd.adobe.photoshop",
           new String[]{ "image/vnd.adobe.photoshop" },
           null,
           new String[]{ "image/vnd.adobe.photoshop" }),
  };

  private MimeRepository repository;

  @BeforeEach
  public void initialize() throws XmlException {
    System.setProperty(PROPERTY_RESOURCE, CONFIGURATION);
    repository = MimeRepository.getInstance();
  }

  void printMimeList(final List<MimeType> mimeList) {
    if (mimeList != null) {
      for (final MimeType mime : mimeList) {
        System.out.println(mime.getName());
      }
    } else {
      System.out.println("<null> list");
    }
  }

  void printList(final String[] list) {
    if (list != null) {
      for (final String str : list) {
        System.out.println(str);
      }
    }
  }

  @Test
  public final void testDetect() throws IOException {
    URL url = null;
    File file = null;
    FileInputStream fis = null;
    List<MimeType> mimeList = null;

    final int maxTestBytes = repository.getMaxTestBytes();
    final byte[] buffer = new byte[maxTestBytes];

    for (int i = 0; i < TEST_DATA.length; ++i) {
      System.out.println("Test case " + i + ": " + TEST_DATA[i].fileName);
      url = MimeRepositoryTest.class.getResource(TEST_DATA_DIR + '/' + TEST_DATA[i].fileName);
      assertNotNull(url);
      try {
        file = new File(url.toURI());
      } catch (final Exception e) {
        fail(e.getMessage());
      }
      assertNotNull(file);

      // test detection by filename only
      System.out.println("Detecting MIME by filename ...");
      mimeList = repository.detectByFilename(file.getAbsolutePath());
      printMimeList(mimeList);
      assertTrue(TestData.matches(repository, mimeList, TEST_DATA[i].detectByName));
      System.out.println("Success.");

      // test detection by file magic only
      System.out.println("Detecting MIME by magic ...");
      try {
        fis = new FileInputStream(file);
        final int nBytes = fis.read(buffer);
        if (nBytes == -1) {
          throw new EOFException();
        }
        mimeList = repository.detectByContent(buffer, nBytes);
        System.out.println("The expected MIME list is:");
        printList(TEST_DATA[i].detectByMagic);
        System.out.println("The actual MIME list is:");
        printMimeList(mimeList);

        assertTrue(TestData.matches(repository, mimeList, TEST_DATA[i].detectByMagic));

      } finally {
        if (fis != null) {
          try {
            fis.close();
          } catch (final Exception e2) {
            fail(e2.getMessage());
          }
          fis = null;
        }
      }
      System.out.println("Success.");

      // test detection by filename and file magic
      System.out.println("Detecting MIME by filename and magic ...");
      try {
        fis = new FileInputStream(file);
        final int nBytes = fis.read(buffer);
        if (nBytes == -1) {
          throw new EOFException();
        }
        mimeList = repository.detect(file.getAbsolutePath(), buffer, nBytes, false);
        printMimeList(mimeList);
        assertTrue(TestData.matches(repository, mimeList, TEST_DATA[i].detectByBoth));
      } finally {
        if (fis != null) {
          try {
            fis.close();
          } catch (final Exception e2) {
            fail(e2.getMessage());
          }
          fis = null;
        }
      }
      System.out.println("Success.");
    }
  }

  @Test
  public final void testRebuild() throws IOException {
    final XmlConfig config = new XmlConfig(CONFIGURATION, MimeRepositoryTest.class);
    repository.rebuild(config);
    testDetect();
  }

  @Test
  public final void testRebuildProduction() throws IOException {
    final XmlConfig config = new XmlConfig(MimeConfig.DEFAULT_RESOURCE, MimeRepositoryTest.class);
    repository.rebuild(config);
  }
}
