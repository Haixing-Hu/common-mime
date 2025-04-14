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
 * 提供常用MIME类型的常量。
 *
 * @author 胡海星
 */
public interface CommonMimeTypes {

  /**
   * Microsoft Excel文件的MIME类型。
   */
  String[] EXCEL_MIME_TYPES = {
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",  // .xlsx
      "application/vnd.ms-excel",                                           // .xls
  };

  /**
   * Microsoft Excel文件的默认MIME类型。
   */
  String EXCEL_MIME_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

  /**
   * Microsoft Word文件的MIME类型。
   */
  String[] WORD_MIME_TYPES = {
      "application/vnd.openxmlformats-officedocument.wordprocessingml.document",  // .docx
      "application/msword",                                                       // .doc
  };

  /**
   * Microsoft Word文件的默认MIME类型。
   */
  String WORD_MIME_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

  /**
   * Microsoft PowerPoint文件的MIME类型。
   */
  String[] POWERPOINT_MIME_TYPES = {
      "application/vnd.openxmlformats-officedocument.presentationml.presentation",  // .pptx
      "application/vnd.ms-powerpoint",                                              // .ppt
      "application/vnd.openxmlformats-officedocument.presentationml.slideshow",     // .ppsx
  };

  /**
   * Microsoft PowerPoint文件的默认MIME类型。
   */
  String POWERPOINT_MIME_TYPE = "application/vnd.openxmlformats-officedocument.presentationml.presentation";

  /**
   * PDF文件的默认MIME类型。
   */
  String PDF_MIME_TYPE = "application/pdf";

  /**
   * PDF文件的MIME类型。
   */
  String[] PDF_MIME_TYPES = {
      PDF_MIME_TYPE,                                                        // .pdf
  };

  /**
   * JSON文件的默认MIME类型。
   */
  String JSON_MIME_TYPE = "application/json";

  /**
   * JSON文件的MIME类型。
   */
  String[] JSON_MIME_TYPES = {
      JSON_MIME_TYPE,                                                       // .json
  };


  /**
   * XML文件的默认MIME类型。
   */
  String XML_MIME_TYPE = "application/xml";

  /**
   * XML文件的MIME类型。
   */
  String[] XML_MIME_TYPES = {
      XML_MIME_TYPE,                                                          // .xml
  };

  /**
   * CSV文件的默认MIME类型。
   */
  String CSV_MIME_TYPE = "text/csv";

  /**
   * CSV文件的MIME类型。
   */
  String[] CSV_MIME_TYPES = {
      CSV_MIME_TYPE,                                                           // .csv
  };

  /**
   * PNG文件的默认MIME类型。
   */
  String PNG_MIME_TYPE = "image/png";

  /**
   * JPEG文件的默认MIME类型。
   */
  String JPEG_MIME_TYPE = "image/jpeg";

  /**
   * GIF文件的默认MIME类型。
   */
  String GIF_MIME_TYPE = "image/gif";

  /**
   * MP4文件的默认MIME类型。
   */
  String MP4_MIME_TYPE = "video/mp4";

  /**
   * MP3文件的默认MIME类型。
   */
  String MP3_MIME_TYPE = "audio/mpeg";

  /**
   * WAV文件的默认MIME类型。
   */
  String WAV_MIME_TYPE = "audio/wav";

  /**
   * OGG文件的默认MIME类型。
   */
  String OGG_MIME_TYPE = "audio/ogg";

  /**
   * WEBM视频文件的默认MIME类型。
   */
  String WEBM_MIME_TYPE = "video/webm";

  /**
   * AVI文件的默认MIME类型。
   */
  String AVI_MIME_TYPE = "video/x-msvideo";

  /**
   * FLV文件的默认MIME类型。
   */
  String FLV_MIME_TYPE = "video/x-flv";

  /**
   * QuickTime视频文件的默认MIME类型。
   */
  String MOV_MIME_TYPE = "video/quicktime";

  /**
   * WMV视频文件的默认MIME类型。
   */
  String WMV_MIME_TYPE = "video/x-ms-wmv";
}