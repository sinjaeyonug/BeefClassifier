package com.example.beefclassifier;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.view.PreviewView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.beefclassifier.tflite.Classifier.Device;
import com.example.beefclassifier.tflite.Logger;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

public  class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    PreviewView mPreviewView;
    ImageView captureImage;
    private static final Logger LOGGER = new Logger();
    private Device device = Device.CPU;
    private int numThreads = -1;
    private FragmentManager fm;
    private FragmentTransaction ft;
    private BottomNavigationView BottomNavigationview;
    private CameraFragment cameraFragment;
    private RecipeFragment recipeFragment;
    private BoardFragment boardFragment;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private TextView Username,Useremail;
    private Button LogoutBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.registertoolbar);
        drawerLayout = findViewById(R.id.draw_layout);
        BottomNavigationview = findViewById(R.id.bottomNavigationView);

        cameraFragment = new CameraFragment();
        recipeFragment = new RecipeFragment();
        boardFragment = new BoardFragment();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24);

        ft.add(R.id.fragmentView, cameraFragment).commit();

        BottomNavigationview.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                ft = fm.beginTransaction();
                switch (item.getItemId()) {
                    case R.id.camera:
                        ft.replace(R.id.fragmentView, cameraFragment).commit();
                        break;
                    case R.id.board:
                        ft.replace(R.id.fragmentView, boardFragment).commit();
                        break;
                    case R.id.recipe:
                        ft.replace(R.id.fragmentView, recipeFragment).commit();
                        break;

                }
                return true;
            }
        });

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigationView);
        Username = (TextView) navigationView.getHeaderView(0).findViewById(R.id.UserName);
        Useremail = (TextView) navigationView.getHeaderView(0).findViewById(R.id.UserEmail);
        LogoutBtn = (Button) navigationView.getHeaderView(0).findViewById(R.id.Logoutbutton);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference Usersreference = database.getReference("Users");
        Usersreference.child(user.getUid()).child("name").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "오류오류", Toast.LENGTH_SHORT).show();
                } else {
                    Username.setText(String.valueOf(task.getResult().getValue()));

                }
            }
        });
        Usersreference.child(user.getUid()).child("email").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "오류오류", Toast.LENGTH_SHORT).show();
                } else {
                    Useremail.setText(String.valueOf(task.getResult().getValue()));

                }
            }
        });

        LogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                if(auth.getCurrentUser()==null)
                {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
                else
                {
                    Toast.makeText(MainActivity.this, "오류오류", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }

    }


}






