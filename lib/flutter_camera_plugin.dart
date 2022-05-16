import 'dart:async';
import 'package:flutter/services.dart';

class FlutterCameraPlugin {
  static const MethodChannel _channel = MethodChannel('flutter_camera_plugin');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<List> takePhoto([int? maxSelectNum = 1]) async {
    List path = await _channel.invokeMethod('takePhoto', maxSelectNum);
    return path;
  }

  static Future<String> get takeCamera async {
    String path = await _channel.invokeMethod('takeCamera');
    return path;
  }
}
