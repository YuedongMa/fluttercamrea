import 'dart:async';
import 'dart:convert';
import 'package:flutter/services.dart';

class FlutterCameraPlugin {
  static const MethodChannel _channel = MethodChannel('flutter_camera_plugin');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<dynamic> takePhoto(int maxSelectNum) async {
    String path = await _channel.invokeMethod('takePhoto', maxSelectNum);
    dynamic list = json.decode(path);
    return list;
  }

  static Future<String> get takeCamera async {
    String path = await _channel.invokeMethod('takeCamera');
    return path;
  }

// cameraPluginInit() {
//   _channel.setMethodCallHandler((handler) async {
//     switch (handler.method) {
//       case "takePhotoResult":
//         debugPrint('====takePhoto--==' + handler.arguments);
//         break;
//       case "takeCameraResult":
//         debugPrint('====takeCamera--==' + handler.arguments);
//         break;
//     }
//   });
// }
}
