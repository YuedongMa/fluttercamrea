import 'dart:io';

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_camera_plugin/flutter_camera_plugin.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  @override
  void initState() {
    super.initState();
    //FlutterCameraPlugin().cameraPluginInit();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      platformVersion = await FlutterCameraPlugin.platformVersion ??
          'Unknown platform version';
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  File? file;
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            children: [
              Text('Running on: $_platformVersion\n'),
              TextButton(
                  onPressed: () {
                    takePhoto(1).then((value) {
                      if (value != null && value.length > 0) {
                        debugPrint("图片选择结果=" +
                            value.length.toString() +
                            "" +
                            value[0]);
                        setState(() {
                          file = File(value[0]);
                        });
                      }
                    });
                  },
                  child: Text("选择图片")),
              TextButton(
                  onPressed: () {
                    takeCamera().then((value) {
                      debugPrint("拍照结果=" + value);
                      setState(() {
                        file = File(value);
                      });
                    });
                  },
                  child: Text("拍照")),
              file == null
                  ? Container()
                  : Image.file(
                      file!,
                      width: double.infinity,
                      height: 200,
                    )
            ],
          ),
        ),
      ),
    );
  }

  Future<List> takePhoto(int maxSelectedNum) async {
    dynamic path = await FlutterCameraPlugin.takePhoto(maxSelectedNum);
    return path;
  }

  Future<String> takeCamera() async {
    String path = await FlutterCameraPlugin.takeCamera;
    return path;
  }
}
