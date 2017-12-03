package com.example.android.saathsaath;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import static com.example.android.saathsaath.UploadActivity.ANNONYMOUS;


public class MainActivity extends AppCompatActivity {

    public static final int RC_SIGN_IN = 1;
    private ArrayList<String> issues;
    private String username;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mFirebaseAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        username = ANNONYMOUS;


        mFirebaseAuth = FirebaseAuth.getInstance();


        issues = new ArrayList<>();
        issues.add("Harrasmant");
        issues.add("Clealiness");
        issues.add("No Light");
        issues.add("Accident");
        issues.add("Others");

        ListView listView = findViewById(R.id.lv_issues);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.issue_item, issues);
        listView.setAdapter(adapter);

        mFirebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    onSignedInInitialize(user.getDisplayName());
                } else {
                    // User is signed out
                    onSignedOutCleanup();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setProviders(
                                            AuthUI.EMAIL_PROVIDER,
                                            AuthUI.GOOGLE_PROVIDER)
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };

        mFirebaseAuth.addAuthStateListener(mFirebaseAuthStateListener);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, UploadActivity.class);
                intent.putExtra(getString(R.string.tag), issues.get(i));
                intent.putExtra(getString(R.string.username), username);
                startActivity(intent);
            }
        });

    }

    private void onSignedOutCleanup() {
        username = ANNONYMOUS;
    }

    private void onSignedInInitialize(String displayName) {
        username = displayName;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Welcome!", Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Something went wrong, Please try again, Later!", Toast.LENGTH_LONG).show();
            }
        }
    }

        @Override
        protected void onResume () {
            super.onResume();
            mFirebaseAuth.addAuthStateListener(mFirebaseAuthStateListener);
        }

        @Override
        protected void onPause () {
            super.onPause();
            if (mFirebaseAuthStateListener != null) {
                mFirebaseAuth.removeAuthStateListener(mFirebaseAuthStateListener);
            }
        }
    }
