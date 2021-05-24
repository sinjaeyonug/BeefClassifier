package com.example.beefclassifier;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayInputStream;

import static com.example.beefclassifier.ImageUtils.StringToBitmap;
import static com.example.beefclassifier.ImageUtils.binaryStringToByteArray;
import static com.example.beefclassifier.ImageUtils.byteArrayToBitmap;

public class PostDetailActivity extends AppCompatActivity {
    private String Postkey;
    private DatabaseReference ref;
    private TextView Title,WriterName,Content;
    private ValueEventListener mPostListener;
    private ImageButton backbtn;
    private ImageView img1,img2,img3;
    private ImageView[] Images =new ImageView[3];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        Intent intent = getIntent();
        Postkey = intent.getStringExtra("postkey");
        if (Postkey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_POST_KEY");
        }



        ref = FirebaseDatabase.getInstance().getReference("Board").child(Postkey);
        img1 = findViewById( R.id.Detail_img1);
        img2 = findViewById( R.id.Detail_img2);
        img3 = findViewById( R.id.Detail_img3);
        Images[0]=img1;
        Images[1]=img2;
        Images[2]=img3;
        Title = findViewById(R.id.Detail_Title);
        WriterName  = findViewById(R.id.Detail_writername);
        Content = findViewById(R.id.Detail_content);
        backbtn = findViewById(R.id.BackButton);
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    @Override
    public void onStart() {
        super.onStart();

        // Add value event listener to the post
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Post post = dataSnapshot.getValue(Post.class);
                Title.setText(post.getTitle());
                WriterName.setText(post.getWriterName());
                Content.setText(post.getContent());
                if(post.getImages() != null)
                {
                    String[] imguris = post.getImages().split(",");
                    for(int i =0; i< imguris.length;i++)
                    {
                        System.out.println(imguris[i]);

                       //ByteArrayInputStream is = new ByteArrayInputStream(b);
                        //Drawable drwImage = Drawable.createFromStream(is, "image");
                        Images[i].setImageBitmap(StringToBitmap(imguris[i]));
                    }
                }



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("d", "loadPost:onCancelled", databaseError.toException());
                Toast.makeText(PostDetailActivity.this, "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        ref.addValueEventListener(postListener);

        // Keep copy of post listener so we can remove it when app stops
        mPostListener = postListener;



    }
    @Override
    public void onStop() {
        super.onStop();

        // Remove post value event listener
        if (mPostListener != null) {
            ref.removeEventListener(mPostListener);
        }

    }
}