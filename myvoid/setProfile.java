package com.application.myvoid;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class setProfile extends AppCompatActivity {

    private ImageView mgetuserimageinimageview;
    private static final int PICK_IMAGE=123;
    private Uri images;

    private EditText mgetusername;

    private FirebaseAuth firebaseAuth;
    private String name;

    private StorageReference storageReference;

    private String ImageUriAccessToken;

    private FirebaseFirestore firebaseFirestore;

    ProgressBar mprogressbarofsetprofile;








    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_profile);

        firebaseAuth=FirebaseAuth.getInstance();
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        storageReference= firebaseStorage.getReference();
        firebaseFirestore=FirebaseFirestore.getInstance();


        mgetusername=findViewById(R.id.getusername);
        CardView mgetuserimage = findViewById(R.id.getuserimage);
        mgetuserimageinimageview=findViewById(R.id.getuserimageinimageview);
        android.widget.Button msaveprofile = findViewById(R.id.saveProfile);
        mprogressbarofsetprofile=findViewById(R.id.progressbarofsetProfile);


        mgetuserimage.setOnClickListener(view -> {
            Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            startActivityForResult(intent,PICK_IMAGE);
        });


        msaveprofile.setOnClickListener(view -> {
            name=mgetusername.getText().toString();
            if(name.isEmpty())
            {
                Toast.makeText(getApplicationContext(),"Name is Empty",Toast.LENGTH_SHORT).show();
            }
            else if(images ==null)
            {
                Toast.makeText(getApplicationContext(),"Image is Empty",Toast.LENGTH_SHORT).show();
            }
            else
            {

                mprogressbarofsetprofile.setVisibility(View.VISIBLE);
                sendDataForNewUser();
                mprogressbarofsetprofile.setVisibility(View.INVISIBLE);
                Intent intent=new Intent(setProfile.this,chatActivity.class);
                startActivity(intent);
                finish();


            }
        });






    }


    private void sendDataForNewUser()
    {

        sendDataToRealTimeDatabase();

    }

    private void sendDataToRealTimeDatabase()
    {


        name=mgetusername.getText().toString().trim();
        FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
        DatabaseReference databaseReference=firebaseDatabase.getReference(Objects.requireNonNull(firebaseAuth.getUid()));

        userprofile muserprofile=new userprofile(name,firebaseAuth.getUid());
        databaseReference.setValue(muserprofile);
        Toast.makeText(getApplicationContext(),"User Profile Added Sucessfully",Toast.LENGTH_SHORT).show();
        sendImagetoStorage();




    }

    private void sendImagetoStorage()
    {

        StorageReference imageref=storageReference.child("Images").child(Objects.requireNonNull(firebaseAuth.getUid())).child("Profile Pic");

        //Image compresesion

        Bitmap bitmap=null;
        try {
            bitmap= MediaStore.Images.Media.getBitmap(getContentResolver(), images);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        assert bitmap != null;
        bitmap.compress(Bitmap.CompressFormat.JPEG,25,byteArrayOutputStream);
        byte[] data=byteArrayOutputStream.toByteArray();

        ///putting image to storage

        UploadTask uploadTask=imageref.putBytes(data);

        uploadTask.addOnSuccessListener(taskSnapshot -> {

            imageref.getDownloadUrl().addOnSuccessListener(uri -> {
                ImageUriAccessToken =uri.toString();
                Toast.makeText(getApplicationContext(),"URI get sucess",Toast.LENGTH_SHORT).show();
                sendDataTocloudFirestore();
            }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(),"URI get Failed",Toast.LENGTH_SHORT).show());
            Toast.makeText(getApplicationContext(),"Image is uploaded",Toast.LENGTH_SHORT).show();

        }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(),"Image Not uploaded",Toast.LENGTH_SHORT).show());





    }

    private void sendDataTocloudFirestore() {


        DocumentReference documentReference=firebaseFirestore.collection("Users").document(Objects.requireNonNull(firebaseAuth.getUid()));
        Map<String , Object> userdata=new HashMap<>();
        userdata.put("name",name);
        userdata.put("image", ImageUriAccessToken);
        userdata.put("uid",firebaseAuth.getUid());
        userdata.put("status","Online");

        documentReference.set(userdata).addOnSuccessListener(aVoid -> Toast.makeText(getApplicationContext(),"Data on Cloud Firestore send success",Toast.LENGTH_SHORT).show());



    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode==PICK_IMAGE && resultCode==RESULT_OK)
        {
            assert data != null;
            images =data.getData();
            mgetuserimageinimageview.setImageURI(images);
        }




        super.onActivityResult(requestCode, resultCode, data);
    }




}