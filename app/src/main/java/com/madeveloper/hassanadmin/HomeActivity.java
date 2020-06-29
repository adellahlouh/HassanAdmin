package com.madeveloper.hassanadmin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

public class HomeActivity extends AppCompatActivity {

    ImageButton notification_img;
    TextInputEditText title_et, body_et;
    private static int RESULT_LOAD_IMG = 1;
    String imgExt;
    Bitmap imageBitmap;
    ProgressDialog dialog;
    Notification notificationModel;
    DatabaseReference refNotification;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        notification_img = findViewById(R.id.notifi_img);
        title_et = findViewById(R.id.title_et);
        body_et = findViewById(R.id.body_et);
        notificationModel = new Notification();

        refNotification = FirebaseDatabase.getInstance().getReference("Notification");

        dialog = new ProgressDialog(HomeActivity.this);

        notification_img.setOnClickListener(view -> {

            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);

        });

        findViewById(R.id.send_bt).setOnClickListener(v -> {
            if (isValid()) {
                dialog.setTitle("Upload Data");
                dialog.setMessage("Send Notification to user" + " ...");
                dialog.show();


                if (imageBitmap != null)
                    uploadImage();
                else
                    uploadNotification(null);

            }

        });

    }

    private void uploadImage() {

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference("Notification").child(UUID.randomUUID().toString());

        storageReference.putBytes(getFileDataFromBitmap(imageBitmap, imgExt)).addOnSuccessListener(taskSnapshot ->
                storageReference.getDownloadUrl().addOnCompleteListener(task -> {

                    String urlImage = Objects.requireNonNull(task.getResult()).toString();

                    uploadNotification(urlImage);
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Upload failed please check your connection", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });


    }


    void uploadNotification(String urlImage) {

        notificationModel.setImageUrl(urlImage);
        notificationModel.setTitle(Objects.requireNonNull(title_et.getText()).toString());
        notificationModel.setBody(Objects.requireNonNull(body_et.getText()).toString());
        notificationModel.setTime(ServerValue.TIMESTAMP);
        String id = refNotification.push().getKey();
        notificationModel.setId(id);


        assert id != null;
        refNotification.child(id).setValue(notificationModel).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Upload data successful.", Toast.LENGTH_SHORT).show();

            dialog.dismiss();


            sendNotificationForUser(notificationModel);

        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Check your internet", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });


    }


    private void sendNotificationForUser(Notification notificationModel) {

        JSONObject jsonObject = FCM.createNotificationObject(notificationModel);


        FCM.sendNotificationToTopic(this,jsonObject,null,"news");

        title_et.setText("");
        body_et.setText("");

        imageBitmap =null;
        notification_img.setImageBitmap(null);
        notification_img.setBackground(getDrawable(R.drawable.ic_add));

    }

    boolean isValid() {
        if (Objects.requireNonNull(title_et.getText()).toString().isEmpty()) {
            title_et.setError("Enter title");
        } else if (Objects.requireNonNull(body_et.getText()).toString().isEmpty()) {
            body_et.setError("Enter title");
        } else {
            return true;
        }
        return false;
    }


    @NotNull
    public static byte[] getFileDataFromBitmap(@NotNull Bitmap bitmap, @NotNull String ImageExt) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Bitmap.CompressFormat format = ImageExt.equals("jpg") ? Bitmap.CompressFormat.JPEG : Bitmap.CompressFormat.PNG;

        bitmap.compress(format, 100, byteArrayOutputStream);

        return byteArrayOutputStream.toByteArray();
    }

    public String getMimeType(Context context, @NonNull Uri uri) {
        String extension;

        //Check uri format to avoid null
        if (Objects.requireNonNull(uri.getScheme()).equals(ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(Objects.requireNonNull(uri.getPath()))).toString());

        }

        return extension;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == RESULT_LOAD_IMG) {
            try {
                notification_img.setImageResource(0);
                assert data != null;
                final Uri imageUri = data.getData();
                assert imageUri != null;
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                notification_img.setImageBitmap(selectedImage);
                imgExt = getMimeType(this, imageUri);
                imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            Toast.makeText(this, " You haven't picked image", Toast.LENGTH_LONG).show();
        }
    }
}