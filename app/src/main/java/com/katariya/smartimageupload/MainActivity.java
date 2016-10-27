package com.katariya.smartimageupload;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.katariya.smartimageupload.utility.CircleImageView;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private static String Url = "Type URL here";
    private static String user_id = "1";
    private ProgressDialog dialog;
    private Button btn_select, btn_upload;
    private CircleImageView img_Picture;
    private TextView txt_status;
    private Uri filePath;
    private int PICK_IMAGE_REQUEST = 1;
    private static final int STORAGE_PERMISSION_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt_status = (TextView) findViewById(R.id.textView_message);
        btn_select = (Button) findViewById(R.id.button_selectImage);
        btn_upload = (Button) findViewById(R.id.button_uploadImage);
        img_Picture = (CircleImageView) findViewById(R.id.imageView_Picture);

        btn_select.setOnClickListener(this);
        btn_upload.setOnClickListener(this);
        requestStoragePermission();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_selectImage:

                selectImage();
                break;
            case R.id.button_uploadImage:

                String imagePath = getPath(filePath);
                new UploadFileMulti(imagePath).execute();
                break;
        }
    }

    private void selectImage() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    //handling the image chooser activity result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                img_Picture.setImageBitmap(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //method to get the file path from uri
    public String getPath(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }

    private class UploadFileMulti extends AsyncTask<String, Void, String> {
        String imagePath;

        public UploadFileMulti(String imagePath) {
            this.imagePath = imagePath;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            txt_status.setVisibility(View.INVISIBLE);
            dialog = ProgressDialog.show(MainActivity.this, "", "Uploading Image File...", true);
            dialog.setCancelable(false);
        }

        @Override
        protected String doInBackground(String... strings) {

            try {
                //       get file from Image path
                File uploadFile1 = new File(imagePath);

                MultipartUtility multipart = new MultipartUtility(Url, "UTF-8");

                //      multipart.addHeaderField("User-Agent", "AndroidHelp");
                //       multipart.addHeaderField("HeaderParam", "Header-Value");

                multipart.addFormField("user_id", user_id);

                multipart.addFilePart("image", uploadFile1);
                List<String> response = multipart.finish();

                System.out.println("SERVER RESPONSE BELOW:");

                for (String line : response) {
                    System.out.println(line);
                }
            } catch (IOException ex) {
                System.err.println(ex);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            txt_status.setVisibility(View.VISIBLE);
            dialog.dismiss();
        }
    }

    //Requesting permission
    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return;

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }
        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if (requestCode == STORAGE_PERMISSION_CODE) {

            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Displaying a toast
                Toast.makeText(this, "Permission granted now you can read the storage", Toast.LENGTH_LONG).show();
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(this, "Oops you just denied the permission", Toast.LENGTH_LONG).show();
            }
        }
    }

}
