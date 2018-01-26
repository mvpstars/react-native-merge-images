package com.mvpstars.reactnative.mergeimages;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class RNMergeImagesModule extends ReactContextBaseJavaModule {

  private static final String TAG = "RNMergeImages";

  public static final int RN_MERGE_SIZE_SMALLEST = 1;
  public static final int RN_MERGE_SIZE_LARGEST = 2;

  public static final int RN_MERGE_TARGET_TEMP = 1;
  public static final int RN_MERGE_TARGET_DISK = 2;

  public static final int DEFAULT_JPEG_QUALITY = 80;

  private final ReactApplicationContext reactContext;

  public RNMergeImagesModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return TAG;
  }

  @Nullable
  @Override
  public Map<String, Object> getConstants() {
    return Collections.unmodifiableMap(new HashMap<String, Object>() {
      {
        put("Size", getSizeConstants());
        put("Target", getTargetConstants());
      }

      private Map<String, Object> getSizeConstants() {
        return Collections.unmodifiableMap(new HashMap<String, Object>() {
          {
            put("smallest", RN_MERGE_SIZE_SMALLEST);
            put("largest", RN_MERGE_SIZE_LARGEST);
          }
        });
      }

      private Map<String, Object> getTargetConstants() {
        return Collections.unmodifiableMap(new HashMap<String, Object>() {
          {
            put("temp", RN_MERGE_TARGET_TEMP);
            put("disk", RN_MERGE_TARGET_DISK);
          }
        });
      }
    });
  }

  @ReactMethod
  public void merge(final ReadableArray images, final ReadableMap options, final Promise promise) {
    new MergeAsyncTask(images, options, promise).execute();
  }

  private class MergeAsyncTask extends AsyncTask<Void, Void, Void> {
    private final ReadableArray images;
    private final ReadableMap options;
    private final Promise promise;

    public MergeAsyncTask(final ReadableArray images, final ReadableMap options, final Promise promise) {
      this.images = images;
      this.options = options;
      this.promise = promise;
    }

    @Override
    protected Void doInBackground(Void... voids) {
      final int size = options.hasKey("size") ? options.getInt("size") : RN_MERGE_SIZE_SMALLEST;
      final int target = options.hasKey("target") ? options.getInt("target") : RN_MERGE_TARGET_TEMP;
      final int jpegQuality = options.hasKey("jpegQuality") ? options.getInt("jpegQuality") : DEFAULT_JPEG_QUALITY;

      final ArrayList<BitmapMetadata> bitmaps = new ArrayList<>(images.size());
      int targetWidth, targetHeight;

      switch (size) {
        case RN_MERGE_SIZE_SMALLEST:
          targetWidth = Integer.MAX_VALUE;
          targetHeight = Integer.MAX_VALUE;
          break;
        default:
          targetWidth = 0;
          targetHeight = 0;
      }

      for (int i = 0, n = images.size(); i < n; i++) {
        BitmapMetadata bitmapMetadata = BitmapMetadata.load(getFilePath(images.getString(i)));
        if (bitmapMetadata != null) {
          bitmaps.add(bitmapMetadata);
          if (size == RN_MERGE_SIZE_LARGEST && (bitmapMetadata.width > targetWidth || bitmapMetadata.height > targetHeight)) {
            targetWidth = bitmapMetadata.width;
            targetHeight = bitmapMetadata.height;
          } else if (size == RN_MERGE_SIZE_SMALLEST && (bitmapMetadata.width < targetWidth || bitmapMetadata.height < targetHeight)) {
            targetWidth = bitmapMetadata.width;
            targetHeight = bitmapMetadata.height;
          }
        }
      }

      final Bitmap mergedBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
      final Canvas canvas = new Canvas(mergedBitmap);

      for (BitmapMetadata bitmapMetadata: bitmaps) {
        Bitmap bitmap = BitmapFactory.decodeFile(bitmapMetadata.fileName);
        Matrix matrix = bitmapMetadata.getMatrix(targetWidth, targetHeight);
        if (matrix == null) {
          canvas.drawBitmap(bitmap, null, new RectF(0, 0, targetWidth, targetHeight), null);
        } else {
          canvas.drawBitmap(bitmap, matrix, null);
        }
        bitmap.recycle();
      }

      saveBitmap(mergedBitmap, target, jpegQuality, promise);
      return null;
    }
  }

  private static String getFilePath(String file) {
    try {
      final String uriPath = Uri.parse(file).getPath();
      return (uriPath != null ? uriPath : file);
    } catch (RuntimeException e) {
      return file;
    }
  }

  private void saveBitmap(Bitmap bitmap, int target, int jpegQuality, Promise promise) {
    try {
      File file;
      switch (target) {
        case RN_MERGE_TARGET_DISK:
          file = getDiskFile();
          break;
        default:
          file = getTempFile();
      }
      final FileOutputStream out = new FileOutputStream(file);
      bitmap.compress(Bitmap.CompressFormat.JPEG, jpegQuality, out);
      WritableMap response = new WritableNativeMap();
      response.putString("path", Uri.fromFile(file).toString());
      response.putInt("width", bitmap.getWidth());
      response.putInt("height", bitmap.getHeight());
      promise.resolve(response);
      out.flush();
      out.close();
    } catch (Exception e) {
      promise.reject("Failed to save image file", e);
    }
  }

  private File getDiskFile() throws IOException {
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    File outputDir = reactContext.getFilesDir();
    outputDir.mkdirs();
    return new File(outputDir, "IMG_" + timeStamp + ".jpg");
  }

  private File getTempFile() throws IOException {
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    File outputDir = reactContext.getCacheDir();
    File outputFile = File.createTempFile("IMG_" + timeStamp, ".jpg", outputDir);
    return outputFile;
  }
}
