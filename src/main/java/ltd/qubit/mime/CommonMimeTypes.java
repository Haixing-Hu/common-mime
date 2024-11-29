////////////////////////////////////////////////////////////////////////////////
//
//    Copyright (c) 2022 - 2024.
//    Haixing Hu, Qubit Co. Ltd.
//
//    All rights reserved.
//
////////////////////////////////////////////////////////////////////////////////
package ltd.qubit.mime;

/**
 * Provides constants of common MIME types.
 *
 * @author Haixing Hu
 */
public interface CommonMimeTypes {

  /**
   * The MIME types of the Microsoft Excel files.
   */
  String[] EXCEL_MIME_TYPES = {
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",  // .xlsx
      "application/vnd.ms-excel",                                           // .xls
  };

  /**
   * The default MIME type of the Microsoft Excel files.
   */
  String EXCEL_MIME_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

  /**
   * The MIME types of the Microsoft Word files.
   */
  String[] WORD_MIME_TYPES = {
      "application/vnd.openxmlformats-officedocument.wordprocessingml.document",  // .docx
      "application/msword",                                                       // .doc
  };

  /**
   * The default MIME type of the Microsoft Word files.
   */
  String WORD_MIME_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

  /**
   * The MIME types of the Microsoft PowerPoint files.
   */
  String[] POWERPOINT_MIME_TYPES = {
      "application/vnd.openxmlformats-officedocument.presentationml.presentation",  // .pptx
      "application/vnd.ms-powerpoint",                                              // .ppt
      "application/vnd.openxmlformats-officedocument.presentationml.slideshow",     // .ppsx
  };

  /**
   * The default MIME type of the Microsoft PowerPoint files.
   */
  String POWERPOINT_MIME_TYPE = "application/vnd.openxmlformats-officedocument.presentationml.presentation";

  /**
   * The default MIME type of the PDF files.
   */
  String PDF_MIME_TYPE = "application/pdf";

  /**
   * The mime types of the PDF files.
   */
  String[] PDF_MIME_TYPES = {
      PDF_MIME_TYPE,                                                        // .pdf
  };

  /**
   * The default MIME type of the JSON files.
   */
  String JSON_MIME_TYPE = "application/json";

  /**
   * The mime types of the JSON files.
   */
  String[] JSON_MIME_TYPES = {
      JSON_MIME_TYPE,                                                       // .json
  };


  /**
   * The default MIME type of the XML files.
   */
  String XML_MIME_TYPE = "application/xml";

  /**
   * The mime types of the XML files.
   */
  String[] XML_MIME_TYPES = {
      XML_MIME_TYPE,                                                          // .xml
  };

  /**
   * The default MIME type of the CSV files.
   */
  String CSV_MIME_TYPE = "text/csv";

  /**
   * The MIME types of the CSV files.
   */
  String[] CSV_MIME_TYPES = {
      CSV_MIME_TYPE,                                                           // .csv
  };

  /**
   * The default MIME type of the PNG files.
   */
  String PNG_MIME_TYPE = "image/png";

  /**
   * The default MIME type of the JPEG files.
   */
  String JPEG_MIME_TYPE = "image/jpeg";

  /**
   * The default MIME type of the GIF files.
   */
  String GIF_MIME_TYPE = "image/gif";

  /**
   * The default MIME type of the MP4 files.
   */
  String MP4_MIME_TYPE = "video/mp4";

  /**
   * The default MIME type of the MP3 files.
   */
  String MP3_MIME_TYPE = "audio/mpeg";

  /**
   * The default MIME type of the WAV files.
   */
  String WAV_MIME_TYPE = "audio/wav";

  /**
   * The default MIME type of the OGG files.
   */
  String OGG_MIME_TYPE = "audio/ogg";

  /**
   * The default MIME type of the WEBM video files.
   */
  String WEBM_MIME_TYPE = "video/webm";

  /**
   * The default MIME type of the AVI files.
   */
  String AVI_MIME_TYPE = "video/x-msvideo";

  /**
   * The default MIME type of the FLV files.
   */
  String FLV_MIME_TYPE = "video/x-flv";

  /**
   * The default MIME type of the QuickTime video files.
   */
  String MOV_MIME_TYPE = "video/quicktime";

  /**
   * The default MIME type of the WMV video files.
   */
  String WMV_MIME_TYPE = "video/x-ms-wmv";
}
