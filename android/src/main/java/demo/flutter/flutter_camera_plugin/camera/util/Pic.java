package demo.flutter.flutter_camera_plugin.camera.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import java.io.File;
import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import demo.flutter.flutter_camera_plugin.R;


/**
 * @class describe
 * @anthor
 * @time 2017/7/26 11:06
 * @change time
 * @class describe
 */
public class Pic {


    public void getMatisseLocal(int requestCode, int count) {
        Matisse matisse  =Matisse.from(getActivity());
        matisse.choose(MimeType.ofAll())
                .countable(true)
//                .capture(true)
                .captureStrategy(
                        new CaptureStrategy(true, getActivity().getApplication().getPackageName()+".fileprovider"))
                .maxSelectable(count)
                //.addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                .gridExpectedSize(mContext.get().getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .thumbnailScale(0.85f)
                .imageEngine(new GlideEngine())
                .forResult(requestCode);
    }


    private final WeakReference<Activity> mContext;


    private Pic(Activity activity) {
        mContext = new WeakReference<>(activity);

    }




    public static Pic from(Activity activity) {
        return new Pic(activity);
    }
    @NonNull
    Activity getActivity() {
        return mContext.get();
    }


    public void getLocalImgResult(int requestCode) {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
        } else {
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }

            getActivity().startActivityForResult(intent, requestCode);

    }

    //  private static final String SAVEPATH = BuildConfig.SCHEME;

    public File getCamerResult(int requestCode ) {
        if (!DeviceUtils.isSdcardExist()) {
            throw new RuntimeException("SDK????????????");
        }
        File appDir = new File(getActivity().getExternalFilesDir(null), getActivity().getPackageName());
        File cameraFile = new File(appDir, System.currentTimeMillis() + ".jpg");
        cameraFile.getParentFile().mkdirs();
        Uri imageUri;
        if (Build.VERSION.SDK_INT < 24) {
            imageUri = Uri.fromFile(cameraFile);
        } else {
            //????????????????????????mainfest????????????
            imageUri = FileProvider.getUriForFile(getActivity(), getActivity().getApplication().getPackageName() + ".fileprovider", cameraFile);
        }
        // ??????????????????
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

            getActivity().startActivityForResult(intent, requestCode);

        return cameraFile;
    }


    public static String getPicAddress(Uri uri, Context context) {
        if (null == uri) {
            throw new IllegalArgumentException("??????????????????");
        }
        if (Build.VERSION.SDK_INT >= 19) {
            // 4.4?????????????????????????????????????????????
            return handleImageOnKitKat(uri,context);
        } else {
            // 4.4??????????????????????????????????????????
            return handleImageBeforeKitKat(uri,context);
        }
    }


    /**
     * ???????????????4.4.?????????????????????
     *
     * @param selectedImage
     */
    protected static String handleImageBeforeKitKat(Uri selectedImage, Context context) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            cursor = null;
            if (picturePath == null || "null".equals(picturePath)) {
                String error = "???????????????";
                throw new RuntimeException(error);
            }
            return picturePath;
        } else {
            File file = new File(selectedImage.getPath());
            if (!file.exists()) {
                String error = "???????????????";
                throw new RuntimeException(error);
            }
            return file.getAbsolutePath();
        }

    }

    /**
     * ???????????????4.4?????????????????????
     *
     * @param selectedImage
     */
    @TargetApi(19)
    private static String handleImageOnKitKat(Uri selectedImage, Context context) {
        String imagePath = null;
        Log.d("TAG", "handleImageOnKitKat: uri is " + selectedImage);
        if (DocumentsContract.isDocumentUri(context, selectedImage)) {
            // ?????????document?????????Uri????????????document id??????
            String docId = DocumentsContract.getDocumentId(selectedImage);
            if ("com.android.providers.media.documents".equals(selectedImage.getAuthority())) {
                String id = docId.split(":")[1]; // ????????????????????????id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection,context);
            } else if ("com.android.providers.downloads.documents".equals(selectedImage.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null,context);
            }
        } else if ("content".equalsIgnoreCase(selectedImage.getScheme())) {
            // ?????????content?????????Uri??????????????????????????????
            imagePath = getImagePath(selectedImage, null,context);
        } else if ("file".equalsIgnoreCase(selectedImage.getScheme())) {
            // ?????????file?????????Uri?????????????????????????????????
            imagePath = selectedImage.getPath();
        }
        return imagePath;
    }


    /**
     * ??????????????????
     *
     * @param uri
     * @param selection
     * @return
     */
    private static String getImagePath(Uri uri, String selection, Context context) {
        String path = null;
        // ??????Uri???selection??????????????????????????????
        Cursor cursor = context.getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }


}
