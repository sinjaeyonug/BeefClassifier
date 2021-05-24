package com.example.beefclassifier;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.Base64;
import android.util.Log;

import androidx.camera.core.ImageProxy;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ImageUtils {
    static final int kMaxChannelValue = 262143;
    private static final float IMAGE_MEAN = 127.5f;

    private static final float IMAGE_STD = 127.5f;

    public static Bitmap StringToBitmap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    /*
     * Bitmap을 String형으로 변환
     * */
    public static String BitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 70, baos);
        byte[] bytes = baos.toByteArray();
        String temp = Base64.encodeToString(bytes, Base64.DEFAULT);
        return temp;
    }

    public static Bitmap byteArrayToBitmap( byte[] byteArray ) {
        Bitmap bitmap = BitmapFactory.decodeByteArray( byteArray, 0, byteArray.length ) ;
        return bitmap ;
    }

    public static String byteArrayToBinaryString(byte[] b)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length; ++i)
        {
            sb.append(byteToBinaryString(b[i]));
        }
        return sb.toString();
    } // 바이너리 바이트를 스트링으로
     public static String byteToBinaryString(byte n)
     {
         StringBuilder sb = new StringBuilder("00000000");
        for (int bit = 0; bit < 8; bit++)
        {
            if (((n >> bit) & 1) > 0)
            {
                sb.setCharAt(7 - bit, '1');
            }
        }
        return sb.toString();
     }

    public static byte[] binaryStringToByteArray(String s)
    {
        int count = s.length() / 8; byte[] b = new byte[count];
        for (int i = 1; i < count; ++i)
        {
            String t = s.substring((i - 1) * 8, i * 8);
            b[i - 1] = binaryStringToByte(t);
        }
        return b;
    } // 스트링을 바이너리 바이트로
     public static byte binaryStringToByte(String s)
     {
         byte ret = 0, total = 0;
         for (int i = 0; i < 8; ++i)
         {
             ret = (s.charAt(7 - i) == '1') ? (byte) (1 << i) : 0; total = (byte) (ret | total);
         }
         return total;
     }



    public static Bitmap imageProxyToBitmap(ImageProxy imageProxy) {

        if (imageProxy.getFormat() == ImageFormat.JPEG) {
            ByteBuffer buffer = imageProxy.getPlanes()[0].getBuffer();
            buffer.rewind();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

            return bitmap;
        } else if (imageProxy.getFormat() == ImageFormat.YUV_420_888) {
            ByteBuffer yBuffer = imageProxy.getPlanes()[0].getBuffer(); // Y
            ByteBuffer uBuffer = imageProxy.getPlanes()[1].getBuffer(); // U
            ByteBuffer vBuffer = imageProxy.getPlanes()[2].getBuffer(); // V

            int ySize = yBuffer.remaining();
            int uSize = uBuffer.remaining();
            int vSize = vBuffer.remaining();

            byte[] nv21 = new byte[ySize + uSize + vSize];

            yBuffer.get(nv21, 0, ySize);
            vBuffer.get(nv21, ySize, vSize);
            uBuffer.get(nv21, ySize + vSize, uSize);

            YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, imageProxy.getWidth(), imageProxy.getHeight(), null);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 100, out);
            byte[] imageBytes = out.toByteArray();
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

            return bitmap;
        }
        return null;
    }

    public static void convertYUV420SPToARGB8888(byte[] input, int width, int height, int[] output) {
       final int frameSize = width * height;
        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width;
           int u = 0;
            int v = 0;

            for (int i = 0; i < width; i++, yp++) {
                int y = 0xff & input[yp];
                if ((i & 1) == 0) {
                    v = 0xff & input[uvp++];
                   u = 0xff & input[uvp++];
               }

               output[yp] = YUV2RGB(y, u, v);
            }
        }
    }
public static byte[] bitmapToByteArray(Bitmap bitmap) {
    ByteArrayOutputStream stream = new ByteArrayOutputStream() ;
    bitmap.compress( Bitmap.CompressFormat.PNG, 100, stream) ;
    byte[] byteArray = stream.toByteArray() ;
    return byteArray ;
}



public static void convertYUV420ToARGB8888(
            byte[] yData,
            byte[] uData,
            byte[] vData,
            int width,
            int height,
            int yRowStride,
            int uvRowStride,
            int uvPixelStride,
            int[] out) {
        int yp = 0;
        for (int j = 0; j < height; j++) {
            int pY = yRowStride * j;
            int pUV = uvRowStride * (j >> 1);

            for (int i = 0; i < width; i++) {
                int uv_offset = pUV + (i >> 1) * uvPixelStride;

                out[yp++] = YUV2RGB(0xff & yData[pY + i], 0xff & uData[uv_offset], 0xff & vData[uv_offset]);
            }
        }
    }
    private static int YUV2RGB(int y, int u, int v) {
        // Adjust and check YUV values
        y = (y - 16) < 0 ? 0 : (y - 16);
        u -= 128;
        v -= 128;

        // This is the floating point equivalent. We do the conversion in integer
        // because some Android devices do not have floating point in hardware.
        // nR = (int)(1.164 * nY + 2.018 * nU);
        // nG = (int)(1.164 * nY - 0.813 * nV - 0.391 * nU);
        // nB = (int)(1.164 * nY + 1.596 * nV);
        int y1192 = 1192 * y;
        int r = (y1192 + 1634 * v);
        int g = (y1192 - 833 * v - 400 * u);
        int b = (y1192 + 2066 * u);

        // Clipping RGB values to be inside boundaries [ 0 , kMaxChannelValue ]
        r = r > kMaxChannelValue ? kMaxChannelValue : (r < 0 ? 0 : r);
        g = g > kMaxChannelValue ? kMaxChannelValue : (g < 0 ? 0 : g);
        b = b > kMaxChannelValue ? kMaxChannelValue : (b < 0 ? 0 : b);

        return 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
    }
}
