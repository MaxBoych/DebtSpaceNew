package com.example.debtspace.utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;

import com.squareup.picasso.Transformation;

public class CircleTransform implements Transformation {



    @Override
    public Bitmap transform(Bitmap source) {
        int size = Math.min(source.getWidth(), source.getHeight());

        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;

        Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
        if (squaredBitmap != source) {
            source.recycle();
        }

        Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(squaredBitmap,
                Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);

        float r = size / 2f;
        canvas.drawCircle(r, r, r, paint);

        squaredBitmap.recycle();
        return bitmap;
    }

    @Override
    public String key() {
        return "circle";
    }

    /*private boolean downloadImageFromInternalStorage() {
        ContextWrapper wrapper = new ContextWrapper(getContext());
        File directory = wrapper.getDir("user_images", Context.MODE_PRIVATE);
        File file = new File(directory, mUsername + ".png");
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            String path = MediaStore.Images.Media.insertImage(
                    Objects.requireNonNull(getContext()).getContentResolver(), bitmap, "", null);
            Uri uri = Uri.parse(path);
            drawImage(uri);
            //mImage.setImageBitmap(bitmap);
            return false;
        } catch (FileNotFoundException e) {
            return true;
        }
    }

    private void saveImageToInternalStorage() {
        ContextWrapper wrapper = new ContextWrapper(getContext());
        File directory = wrapper.getDir("user_images", Context.MODE_PRIVATE);
        File file = new File(directory, mUsername + ".png");
        try {
            FileOutputStream stream = new FileOutputStream(file);
            Drawable drawable = mImage.getDrawable();
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.flush();
            stream.close();
        } catch (IOException e) {
            Log.d("#DS", e.toString());
        }
    }*/
}
