package demo.flutter.flutter_camera_plugin;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

import demo.flutter.flutter_camera_plugin.camera.util.LocalFile;
import demo.flutter.flutter_camera_plugin.camera.util.Pic;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

import com.zhihu.matisse.Matisse;
import com.google.gson.Gson;

import static android.app.Activity.RESULT_OK;

/**
 * FlutterCameraPlugin
 */
public class FlutterCameraPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;
    private ActivityPluginBinding activityPluginBinding = null;
    private final int TAKE_CAMERA = 100;
    private final int TAKE_PICTURES = 200;
    private  int MAX_IMG_COUNT = 1;
    private List<String> checkedItems = new ArrayList<>();
    private Gson gson = new Gson();
    private File mCameraFile;
    private Result result;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_camera_plugin");
        channel.setMethodCallHandler(this);
    }

    PluginRegistry.ActivityResultListener resultListener = new PluginRegistry.ActivityResultListener() {
        @Override
        public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == TAKE_PICTURES) {
                if (resultCode != RESULT_OK) {
                    checkedItems.clear();
                    result.success(checkedItems);
                    return false;
                }
                if (data != null) {
                    checkedItems.clear();
                    List<Uri> uriList = Matisse.obtainResult(data);
                    for (int i = 0; i < uriList.size(); i++) {
                        Uri selectedImage = uriList.get(i);
                        String path = Pic.getPicAddress(selectedImage, activityPluginBinding.getActivity());
                        checkedItems.add(path);
                    }
                    result.success(checkedItems);
                   // channel.invokeMethod("takePhotoResult", gson.toJson(checkedItems));
                }
            } else if (requestCode == TAKE_CAMERA) {
                if (resultCode != RESULT_OK) {
                    result.success("");
                    return false;
                }
                if (mCameraFile != null) {
                    String imagePath = mCameraFile.getAbsolutePath();
                    result.success(imagePath);
                    //channel.invokeMethod("takeCameraResult", imagePath);
                }


            }


            return false;
        }
    };

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        this.result=result;
        if (call.method.equals("getPlatformVersion")) {
            result.success("Android " + android.os.Build.VERSION.RELEASE);
        } else if (call.method.equals("takePhoto")) {
            if( call.arguments instanceof Integer){
                MAX_IMG_COUNT= (int) call.arguments;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Pic.from(activityPluginBinding.getActivity()).getMatisseLocal(TAKE_PICTURES, MAX_IMG_COUNT);

                }
            });
        } else if (call.method.equals("takeCamera")) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mCameraFile = Pic.from(activityPluginBinding.getActivity()).getCamerResult(TAKE_CAMERA);

                }
            });
        } else {
            result.notImplemented();
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        activityPluginBinding = binding;
        activityPluginBinding.addActivityResultListener(resultListener);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {

    }

    @Override
    public void onDetachedFromActivity() {
        resultListener = null;
        activityPluginBinding = null;
    }

    public void runOnUiThread(Runnable runnable) {
        activityPluginBinding.getActivity().runOnUiThread(runnable);
    }
}
