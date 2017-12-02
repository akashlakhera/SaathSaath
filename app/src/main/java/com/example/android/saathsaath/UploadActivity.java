package com.example.android.saathsaath;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;

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
    private String cameraIntentPic = null;
    private String galleryIntentPic = null;
    private Uri selectedImageUri;
    private String descriptionText;

    private String mUsername;

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
                if (charSequence.toString().trim().length() > 0) {
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
                            } else
                                cameraIntent();
                        } else if (dialogOptions[i].equals("From Gallery"))
                            galleryIntent();
                        else
                            dialogInterface.cancel();
                    }
                });
        imageChoseDialog.show();
    }

    private boolean upload() {
        // Get a reference to store file at chat_photos/<FILENAME>

        StorageReference photoRef = mStorageReference.child(selectedImageUri.getLastPathSegment());

        // Upload file to Firebase Storage
        photoRef.putFile(selectedImageUri)
                .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // When the image has successfully uploaded, we get its download URL
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();

                        // Set the download URL to the message box, so that the user can send it to the database
                        Issue issue = new Issue(descriptionText, mUsername, downloadUrl.toString());
                        mDatabaseReference.push().setValue(issue);
                        //Inte)
                    }
                });

        return true;
    }


    private void galleryIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/* video/*");
        startActivityForResult(intent, PHOTO_PICK);
    }

    private void cameraIntent() {


        // Create the capture image intent
        Intent capturePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (capturePhotoIntent.resolveActivity(getPackageManager()) != null) {
            // Create the temporary File where the photo should go
            File photoFile = null;
            try {
                photoFile = File.createTempFile("Issue", ".jpg", getExternalCacheDir());
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {

                // Get the path of the temporary file
                //mTempPhotoPath = photoFile.getAbsolutePath();

                // Get the content URI for the image file
                Uri photoURI = FileProvider.getUriForFile(this,
                        FILE_PROVIDER_AUTHORITY,
                        photoFile);

                // Add the URI so the camera can store the image
                capturePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                // Launch the camera activity
                startActivityForResult(capturePhotoIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PHOTO_PICK && resultCode == RESULT_OK) {
            selectedImageUri = null;
            selectedImageUri = data.getData();
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            selectedImageUri = null;
            selectedImageUri = data.getData();
            uploadImage_btn.setText(selectedImageUri.getLastPathSegment());
        }


        // Get a reference to store file at chat_photos/<FILENAME>
        StorageReference photoRef = mStorageReference.child(selectedImageUri.getLastPathSegment());
        if (selectedImageUri != null) {
            photoRef = mStorageReference.child(selectedImageUri.getLastPathSegment());
        }

        // Upload file to Firebase Storage
        photoRef.putFile(selectedImageUri)
                .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // When the image has successfully uploaded, we get its download URL
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();

                        // Set the download URL to the message box, so that the user can send it to the database
                        Issue issue = new Issue(null, mUsername, downloadUrl.toString());
                        mDatabaseReference.push().setValue(issue);
                        Toast.makeText(UploadActivity.this, "Success!", Toast.LENGTH_LONG).show();
                    }
                });
    }
}


