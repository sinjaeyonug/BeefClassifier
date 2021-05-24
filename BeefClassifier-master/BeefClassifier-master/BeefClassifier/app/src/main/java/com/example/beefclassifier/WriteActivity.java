package com.example.beefclassifier;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.auth.User;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import io.perfmark.Tag;

import static com.example.beefclassifier.ImageUtils.BitmapToString;
import static com.example.beefclassifier.ImageUtils.StringToBitmap;
import static com.example.beefclassifier.ImageUtils.binaryStringToByteArray;
import static com.example.beefclassifier.ImageUtils.bitmapToByteArray;
import static com.example.beefclassifier.ImageUtils.byteArrayToBinaryString;
import static com.example.beefclassifier.ImageUtils.byteArrayToBitmap;


public class WriteActivity extends AppCompatActivity {
    private Button ConfirmBtn,CancleBtn,AttachBtn;
    private EditText titleedit,contentedit;
    private static final int PICTURE_REQUEST_CODE = 100;
    private ImageView attachimage;
    private TextView attachtext;
    private ArrayList<Uri> Uris;
    private String Username;
    SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICTURE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                //ClipData 또는 Uri를 가져온다
                Uri uri = data.getData();
                ClipData clipData = data.getClipData();
                //이미지 URI 를 이용하여 이미지뷰에 순서대로 세팅한다.
                if (clipData != null) {
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        Uri urione = clipData.getItemAt(i).getUri();
                        Uris.add(urione);
//                        if(i==0) {
//                            Glide.with(this)
//                                    .load(bitmap)
//                                    .into(attachimage);
//
//                        }
                    }
                } else if (uri != null) {
                    Uris.add(uri);

                    attachimage.setImageURI(uri);
//                    Glide.with(this)
//                            .load(bitmap)
//                            .into(attachimage);
                }
                if(Uris.size()>1)
                    attachtext.setText("외 "+(Uris.size()-1)+"장");

            }
        }
    }



    private Bitmap UriToBitmap(Uri selectedFileUri) {
        Bitmap bitmap=null;
        try {
            ParcelFileDescriptor parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(selectedFileUri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);

            parcelFileDescriptor.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);


        Uris = new ArrayList<Uri>();
        ConfirmBtn = findViewById(R.id.ConfirmButton);
        CancleBtn = findViewById(R.id.CancleButton);
        AttachBtn = findViewById(R.id.attachButton);
        titleedit = findViewById(R.id.EditTitle);
        contentedit = findViewById(R.id.EditContent);
        attachimage = findViewById(R.id.AttachImage);
        attachtext = findViewById(R.id.AttachText);

        Intent intent = getIntent();
        Username = intent.getStringExtra("Username");

        AttachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                //사진을 여러개 선택할수 있도록 한다
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"),  PICTURE_REQUEST_CODE);
            }
        });

        ConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                FirebaseUser user = auth.getCurrentUser();
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference ref = database.getReference("Board");
                HashMap<Object,String> hashMap = new HashMap<>();
                Post post = new Post();
                long now = System.currentTimeMillis();
                Date date = new Date(now);
                //hashMap.put("Date", mFormat.format(date));
                post.setDate(mFormat.format(date));
                post.setUid(user.getUid());
                post.setWriterName(Username);
                post.setTitle(titleedit.getText().toString());
                post.setContent(contentedit.getText().toString());
                if(Uris.size() >= 1) {
                    String a = "";
                    for (int i = 0; i<Uris.size(); i++) {

                        if(i ==Uris.size()-1)
                            a += BitmapToString(UriToBitmap(Uris.get(i)));
                        else
                            a +=  BitmapToString(UriToBitmap(Uris.get(i))) + ",";
                    }
                    post.setImages(a);
                }
                else {

                }
                String key=ref.push().getKey();
                ref.child(key).setValue(post);
                finish();
            }

        });

        CancleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}