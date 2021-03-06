package com.example.appsimulator;

import android.os.Handler;
import android.util.Log;
import android.util.Patterns;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ModelLogin implements Contracts.ModelLogin {
    boolean match = false;
    private FirebaseDatabase f_db;
    private DatabaseReference ref;
    private Contracts.PresenterLogin listener;

    public ModelLogin(Contracts.PresenterLogin listener){
        this.f_db = FirebaseDatabase.getInstance();
        this.ref = FirebaseDatabase.getInstance().getReference();
        this.listener = listener;
    }

    @Override
    public boolean loginError(String email, String password) {
        //Consider the type to compare corresponding regex with
        if (email.isEmpty()) {
            listener.loginFailed("Empty Email. Try Again");
            return true;
        }
        if (!(Patterns.EMAIL_ADDRESS.matcher(email).matches())){
            listener.loginFailed("Invalid Email. Try Again");
            return true;
        }
        if (password.isEmpty()) {
            listener.loginFailed("Empty Password. Try Again");
            return true;
        }
        return false;
    }

    public void match(String email, String password, String userType) {
        if(loginError(email,password)){
            return;
        }
        //Consider the type to compare corresponding regex with
        //checks if the email is the database and if credentials are correct
        final boolean[] match = {false};
        ref = FirebaseDatabase.getInstance().getReference("Users").child(userType);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {//changed
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = null;
                //for each loop checks if user is in the database
                for (DataSnapshot ds : snapshot.getChildren()){
                    if (ds.getKey().equals(Integer.toString(email.hashCode()))) {
                        match[0] = true;
                        user = ds.getValue(User.class); // gets user object
                        break;
                    }
                }

                 //checks if password is correct if it finds a match
                if (match[0]) {
                    if (user == null || !user.pwd.equals(password)) {
                        listener.loginFailed("Incorrect password");
                    } else {
                        listener.loginSuccess(Integer.toString(user.hashCode()));
                    }
                }
                else
                    //Log.i("login failed","user not found");
                    listener.loginFailed("Not a registered user, please register.");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void login(String email, String password, String userType) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                match(email,password, userType);
            }
        });
    }

    public void userType(User user){

    }
}
