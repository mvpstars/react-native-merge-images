package com.mvpstars.reactnative.mergeimages;

import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.media.ExifInterface;

import java.io.IOException;

/**
 * Created by paiou on 26/01/18.
 */
public class BitmapMetadata {
  public final String fileName;
  public final int width;
  public final int height;
  public final int exifOrientation;

  private BitmapMetadata(String fileName, int width, int height, int exifOrientation) {
    this.fileName = fileName;
    this.width = width;
    this.height = height;
    this.exifOrientation = exifOrientation;
  }

  public static BitmapMetadata load(String fileName) {
    try {
      ExifInterface exif = new ExifInterface(fileName);
      int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
      BitmapFactory.Options opts = new BitmapFactory.Options();
      opts.inJustDecodeBounds = true;
      BitmapFactory.decodeFile(fileName, opts);
      if (needsReorient(orientation))
        return new BitmapMetadata(fileName, opts.outHeight, opts.outWidth, orientation);
      else
        return new BitmapMetadata(fileName, opts.outWidth, opts.outHeight, orientation);
    } catch (IOException | RuntimeException e) {
      return null;
    }
  }

  public static boolean needsReorient(int exifOrientation) {
    return (exifOrientation >= 5 && exifOrientation <= 8);
  }

  public Matrix getMatrix(int targetWidth, int targetHeight) {
    final float scaleX = (float) targetWidth / (float) this.width;
    final float scaleY = (float) targetHeight / (float) this.height;
    final Matrix matrix = new Matrix();

    switch (exifOrientation) {
      case 1:
        return null;
      case 2:
        matrix.postScale(-1f * scaleX, scaleY);
        break;
      case 3:
        matrix.preTranslate(-this.width / 2, -this.height / 2);
        matrix.postRotate(180);
        matrix.postTranslate(this.width / 2, this.height / 2);
        matrix.postScale(scaleX, scaleY);
        break;
      case 4:
        matrix.preTranslate(-this.width / 2, -this.height / 2);
        matrix.postRotate(180);
        matrix.postTranslate(this.width / 2, this.height / 2);
        matrix.postScale(-1f * scaleX, scaleY);
        break;
      case 5:
        matrix.preTranslate(-this.height / 2, -this.width / 2);
        matrix.postRotate(90);
        matrix.postTranslate(this.width / 2, this.height / 2);
        matrix.postScale(-1f * scaleX, scaleY);
        break;
      case 6:
        matrix.preTranslate(-this.height / 2, -this.width / 2);
        matrix.postRotate(90);
        matrix.postTranslate(this.width / 2, this.height / 2);
        matrix.postScale(scaleX, scaleY);
        break;
      case 7:
        matrix.preTranslate(-this.height / 2, -this.width / 2);
        matrix.postRotate(270);
        matrix.postTranslate(this.width / 2, this.height / 2);
        matrix.postScale(-1f * scaleX, scaleY);
        break;
      case 8:
        matrix.preTranslate(-this.height / 2, -this.width / 2);
        matrix.postRotate(270);
        matrix.postTranslate(this.width / 2, this.height / 2);
        matrix.postScale(scaleX, scaleY);
        break;
      default:
        return null;
    }

    return matrix;
  }
}
