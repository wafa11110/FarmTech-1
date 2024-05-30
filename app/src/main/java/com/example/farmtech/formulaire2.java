package com.example.farmtech;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class formulaire2 extends AppCompatActivity {
    EditText emaill, passwordd;
    Button start, signup;
    FrameLayout btngoogle;
    ProgressDialog progressDialog;
    GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulaire2);
        getWindow().setStatusBarColor(ContextCompat.getColor(formulaire2.this, R.color.white));
        changeStatusBarTextColor(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        emaill = findViewById(R.id.editText_email);
        passwordd = findViewById(R.id.editText_password);
        start = findViewById(R.id.starte2);
        signup = findViewById(R.id.button2);
        btngoogle = findViewById(R.id.google);
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

        signup.setOnClickListener(v -> {
            Intent i = new Intent(formulaire2.this, formulaire1.class);
            startActivity(i);
            finish();
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateEmail() && validatePassword()) {
                    checkUser();
                }
            }
        });

        setupPasswordVisibilityToggle(passwordd);
    }

    private void setupPasswordVisibilityToggle(final EditText passwordField) {
        passwordField.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (passwordField.getRight() - passwordField.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        int selection = passwordField.getSelectionEnd();
                        if (passwordField.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                            passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            passwordField.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_visibility_24, 0);
                        } else {
                            passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
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
        mAuth.signOut();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public boolean validateEmail() {
        String val = emaill.getText().toString();
        if (val.isEmpty()) {
            emaill.setError("Email cannot be empty");
            return false;
        } else {
            emaill.setError(null);
        }
        return true;
    }

    public boolean validatePassword() {
        String val = passwordd.getText().toString();
        if (val.isEmpty()) {
            passwordd.setError("Password cannot be empty");
            return false;
        } else {
            passwordd.setError(null);
        }
        return true;
    }

    public void checkUser() {
        String userEmail = emaill.getText().toString().trim();
        String userPassword = passwordd.getText().toString().trim();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUserDatabase = reference.orderByChild("email").equalTo(userEmail);
        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String username = dataSnapshot.child("username").getValue(String.class);
                        String email = dataSnapshot.child("email").getValue(String.class);
                        String password = dataSnapshot.child("password").getValue(String.class);
                        if (password != null && password.equals(userPassword)) {
                            Intent intent = new Intent(formulaire2.this, homepage.class);
                            intent.putExtra("username", username);
                            intent.putExtra("email", email);
                            intent.putExtra("password", password);
                            startActivity(intent);
                            finish();
                        } else {
                            passwordd.setError("Invalid password");
                            passwordd.requestFocus();
                        }
                    }
                } else {
                    emaill.setError("Email does not exist");
                    emaill.requestFocus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled event
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
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
                            Toast.makeText(formulaire2.this, "Google sign in successful", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent intent = new Intent(formulaire2.this, homepage.class);
                            intent.putExtra("username", username);
                            intent.putExtra("email", email);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(formulaire2.this, "Google sign in failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } catch (ApiException e) {
                Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        }
    }

    private void changeStatusBarTextColor(int flags) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(flags);
        }
    }
}





