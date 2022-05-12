package demo.flutter.flutter_camera_plugin.camera.util;


import android.os.Parcel;
import android.os.Parcelable;



import java.io.IOException;

/**
 * Created by MacBook on 17/11/21.
 */

public class LocalFile implements Parcelable {
    private boolean ishttp = false;
    private String size;
    private String originalUri;//原图URI
    private String thumbnailUri;//缩略图URI
    private int orientation;//图片旋转角度

    public LocalFile() {

    }

    public LocalFile(Parcel in) {
        ishttp = in.readByte() != 0;
        size = in.readString();
        originalUri = in.readString();
        thumbnailUri = in.readString();
        orientation = in.readInt();
    }

    public static final Creator<LocalFile> CREATOR = new Creator<LocalFile>() {
        @Override
        public LocalFile createFromParcel(Parcel in) {
            return new LocalFile(in);
        }

        @Override
        public LocalFile[] newArray(int size) {
            return new LocalFile[size];
        }
    };



    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public boolean ishttp() {
        return ishttp;
    }

    public void setIshttp(boolean ishttp) {
        this.ishttp = ishttp;
    }

    public String getThumbnailUri() {
        return thumbnailUri;
    }

    public void setThumbnailUri(String thumbnailUri) {
        this.thumbnailUri = thumbnailUri;
    }

    public String getOriginalUri() {
        return originalUri;
    }

    public void setOriginalUri(String originalUri) {
        this.originalUri = originalUri;
    }


    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int exifOrientation) {
        orientation = exifOrientation;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (ishttp ? 1 : 0));
        dest.writeString(size);
        dest.writeString(originalUri);
        dest.writeString(thumbnailUri);
        dest.writeInt(orientation);
    }
}
