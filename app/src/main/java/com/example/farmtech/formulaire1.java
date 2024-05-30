package com.example.farmtech;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class formulaire1 extends AppCompatActivity {
    EditText username, email, password, confirmpassword;
    Button start, signin;
    FrameLayout btngoogle;
    CheckBox termsCheckBox;
    FirebaseDatabase database;
    DatabaseReference reference;
    ProgressDialog progressDialog;
    GoogleSignInClient mGoogleSignInClient;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulaire1);
        getWindow().setStatusBarColor(ContextCompat.getColor(formulaire1.this, R.color.white));
        changeStatusBarTextColor(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        username = findViewById(R.id.editText_username);
        email = findViewById(R.id.editText_email);
        password = findViewById(R.id.editText_password);
        confirmpassword = findViewById(R.id.editText_Confirmepassword);
        start = findViewById(R.id.starte1);
        signin = findViewById(R.id.button1);
        btngoogle = findViewById(R.id.google);
        termsCheckBox = findViewById(R.id.checkBox_terms);
        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Google Sign In....");

        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, signInOptions);

        btngoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });

        signin.setOnClickListener(v -> {
            Intent i = new Intent(formulaire1.this, formulaire2.class);
            startActivity(i);
            finish();
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    registerUser();
                }
            }
        });

        setupPasswordVisibilityToggle(password);
        setupPasswordVisibilityToggle(confirmpassword);
    }

    private void setupPasswordVisibilityToggle(final EditText passwordField) {
        passwordField.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (passwordField.getRight() - passwordField.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        int selection = passwordField.getSelectionEnd();
                        if (passwordField.getInputType() == (android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                            passwordField.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            passwordField.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_visibility_24, 0);
                        } else {
                            passwordField.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            passwordField.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_visibility_off_24, 0);
                        }
                        passwordField.setSelection(selection);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void signInWithGoogle() {
        progressDialog.show();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 124);
    }

    private boolean validateInput() {
        String username1 = username.getText().toString().trim();
        String email1 = email.getText().toString().trim();
        String password1 = password.getText().toString().trim();
        String confirmpassword1 = confirmpassword.getText().toString().trim();

        if (TextUtils.isEmpty(username1)) {
            username.setError("Username cannot be empty");
            return false;
        }

        if (TextUtils.isEmpty(email1) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email1).matches()) {
            email.setError("Enter a valid email address");
            return false;
        }

        if (TextUtils.isEmpty(password1)) {
            password.setError("Password cannot be empty");
            return false;
        }

        if (TextUtils.isEmpty(confirmpassword1) || !confirmpassword1.equals(password1)) {
            confirmpassword.setError("Passwords do not match");
            return false;
        }

        if (!termsCheckBox.isChecked()) {
            Toast.makeText(this, "You must agree to the terms and conditions to proceed", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void registerUser() {
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users");
        String username1 = username.getText().toString().trim();
        String email1 = email.getText().toString().trim();
        String password1 = password.getText().toString().trim();

        // Check if username already exists in database
        reference.child(username1).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful() || task.getResult().getValue() != null) {
                    // Username already exists
                    Toast.makeText(formulaire1.this, "Username already exists", Toast.LENGTH_SHORT).show();
                } else {
                    // Username doesn't exist, proceed with registration
                    HelperClass helperClass = new HelperClass(username1, email1, password1);
                    reference.child(username1).setValue(helperClass);
                    Toast.makeText(formulaire1.this, "You have signed up successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(formulaire1.this, homepage.class);
                    intent.putExtra("username", username1);
                    intent.putExtra("email", email1);
                    intent.putExtra("password", password1);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 124) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                String username = account.getDisplayName();
                String email = account.getEmail();
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                mAuth.signInWithCredential(credential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            Toast.makeText(formulaire1.this, "Google sign in succes" + task.getException(), Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent intent = new Intent(formulaire1.this, homepage.class);
                            intent.putExtra("username", username);
                            intent.putExtra("email", email);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(formulaire1.this, "Google sign in failed" + task.getException(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            } catch (ApiException e) {
                Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        }
    }
    private void changeStatusBarTextColor(int flags){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            View decor =getWindow().getDecorView();
            decor.setSystemUiVisibility(flags);
        }
    }
}


