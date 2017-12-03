package com.example.android.saathsaath;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UploadActivity extends AppCompatActivity {

    public static final String TAG = UploadActivity.class.getSimpleName();
    public static final String ANNONYMOUS = "annonymous";
    private static final String FILE_PROVIDER_AUTHORITY = "com.example.android.fileprovider";
    public static final int DEFAULT_DESCRIPTION_LENGTH = 250;
    public static final int PHOTO_PICK = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int REQUEST_STORAGE_PERMISSION = 3;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;

    private TextView descriptionTextView;
    private Button uploadImage_btn;
    private Button post;
    private String[] dialogOptions;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageReference;
    private Uri selectedImageUri;
    private String descriptionText;
    private String mTempPhotoPath;
    private String imagePath = null;
    private boolean uploadViaCam = false;
    private ImageView gpsImageView;

    private String mUsername = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        getSupportActionBar().setTitle(getString(R.string.report_problem));

        String selectedCase = getIntent().getStringExtra(getString(R.string.tag));
        mUsername = getIntent().getStringExtra(getString(R.string.username));

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference().child("harrasmant");
        mFirebaseStorage = FirebaseStorage.getInstance();
        mStorageReference = mFirebaseStorage.getReference();
        descriptionTextView = findViewById(R.id.description_textView);
        uploadImage_btn = findViewById(R.id.upload_img_btn);
        post = findViewById(R.id.post_button);

        descriptionText = descriptionTextView.getText().toString();

        dialogOptions = new String[]{"From Camera", "From Gallery", "Cancel"};

        descriptionTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() < 1) {
                    post.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        descriptionTextView.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_DESCRIPTION_LENGTH)});

        uploadImage_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImageDialog();
            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (post.isEnabled())
                    upload();
                else
                    Toast.makeText(UploadActivity.this, "Please check all the details are properly filled",
                            Toast.LENGTH_LONG).show();
            }
        });

    }


    private void uploadImageDialog() {
        final AlertDialog.Builder imageChoseDialog = new AlertDialog.Builder(UploadActivity.this);
        imageChoseDialog.setTitle("Add Image");
        imageChoseDialog.setItems(new String[]{"From Camera", "From Gallery", "Cancel"},
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (dialogOptions[i].equals("From Camera")) {
                            // Check for the external storage permission
                            if (ContextCompat.checkSelfPermission(UploadActivity.this,
                                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    != PackageManager.PERMISSION_GRANTED) {

                                // If you do not have permission, request it
                                ActivityCompat.requestPermissions(UploadActivity.this,
                                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        REQUEST_STORAGE_PERMISSION);
                            } else {
                                cameraIntent();
                            }
                        } else if (dialogOptions[i].equals("From Gallery"))
                            galleryIntent();
                        else
                            dialogInterface.cancel();
                    }
                });
        imageChoseDialog.show();
    }

    private void upload() {
        // Get a reference to store file at chat_photos/<FILENAME>

        if (uploadViaCam) {
            StorageReference photoRef = mStorageReference.child(mTempPhotoPath);

            photoRef.putFile(Uri.parse(mTempPhotoPath))
                    .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();

                            // Set the download URL to the message box, so that the user can send it to the database
                            Issue issue = new Issue(descriptionText, mUsername, downloadUrl.toString());
                            mDatabaseReference.push().setValue(issue);
                            uploadImage_btn.setText(getString(R.string.upload_img));
                        }
                    });
        } else {

            StorageReference photoRef = mStorageReference.child(selectedImageUri.toString());

            // Upload file to Firebase Storage
            photoRef.putFile(selectedImageUri)
                    .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // When the image has successfully uploaded, we get its download URL
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();

                            // Set the download URL to the message box, so that the user can send it to the database
                            Issue issue = new Issue(descriptionText, mUsername, downloadUrl.toString());
                            mDatabaseReference.push().setValue(issue);
                            Intent intent = new Intent(UploadActivity.this, TaF.class);
                            startActivity(intent);
                        }
                    });
        }
    }


    private void galleryIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/* video/*");
        startActivityForResult(intent, PHOTO_PICK);
    }

    private void cameraIntent() {

        Intent capturePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (capturePhotoIntent.resolveActivity(getPackageManager()) != null) {
            // Create the temporary File where the photo should go
            File photoFile = null;
            try {
                photoFile = createTempImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {

                mTempPhotoPath = photoFile.getAbsolutePath();

                Uri photoURI = FileProvider.getUriForFile(this,
                        FILE_PROVIDER_AUTHORITY,
                        photoFile);
                capturePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(capturePhotoIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // Called when you request permission to read and write to external storage
        switch (requestCode) {
            case REQUEST_STORAGE_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // If you get permission, launch the camera
                    cameraIntent();
                } else {
                    // If you do not get permission, show a Toast
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == PHOTO_PICK && resultCode == RESULT_OK) {
            selectedImageUri = null;
            uploadViaCam = false;
            selectedImageUri = data.getData();
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bitmap resultBitmap = resamplePic(mTempPhotoPath);
            uploadViaCam = true;
            imagePath = saveImage(resultBitmap);
            uploadImage_btn.setText(imagePath);
            uploadImage_btn.setClickable(false);
        }

    }

    File createTempImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalCacheDir();

        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    private Bitmap resamplePic(String imagePath) {

        // Get device screen size information
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager manager = (WindowManager) this.getSystemService(this.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(metrics);

        int targetH = metrics.heightPixels;
        int targetW = metrics.widthPixels;

        // Get the dimensions of the original bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        return BitmapFactory.decodeFile(imagePath);
    }

    private String saveImage(Bitmap image) {

        String savedImagePath = null;

        // Create the new file in the external storage
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";
        File storageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        + "/Emojify");
        boolean success = true;
        if (!storageDir.exists()) {
            success = storageDir.mkdirs();
        }

        // Save the new Bitmap
        if (success) {
            File imageFile = new File(storageDir, imageFileName);
            savedImagePath = imageFile.getAbsolutePath();
            try {
                OutputStream fOut = new FileOutputStream(imageFile);
                image.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                fOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return savedImagePath;
    }

}


