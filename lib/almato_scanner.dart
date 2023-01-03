import 'dart:async';

import 'package:flutter/services.dart';

class AlmatoScanner {
  static const MethodChannel _channel = const MethodChannel('almato_scanner');

  /// Call this method to scan the object edge in live camera.
  static Future<String?> detectEdge(
      {String androidScanTitle: "Scanning",
      String androidCropTitle: "Crop",
      String androidCropBlackWhiteTitle: "Black White",
      String androidCropReset: "Reset"}) async {
    return await _channel.invokeMethod('edge_detect', {
      'scan_title': androidScanTitle,
      'crop_title': androidCropTitle,
      'crop_black_white_title': androidCropBlackWhiteTitle,
      'crop_reset_title': androidCropReset,
    });
  }

  /// Call this method to scan the object edge from a gallery image.
  static Future<String?> detectEdgeFromGallery(
      {String androidCropTitle: "Crop",
      String androidCropBlackWhiteTitle: "Black White",
      String androidCropReset: "Reset"}) async {
    return await _channel.invokeMethod('edge_detect_gallery', {
      'crop_title': androidCropTitle,
      'crop_black_white_title': androidCropBlackWhiteTitle,
      'crop_reset_title': androidCropReset,
    });
  }
}
