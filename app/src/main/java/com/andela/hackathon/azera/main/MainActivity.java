package com.andela.hackathon.azera.main;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.andela.hackathon.azera.LoginActivity;
import com.andela.hackathon.azera.Logout;
import com.andela.hackathon.azera.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

    private TabLayout tabLayout;
    private ViewPager pager;

    private static final int REQUEST_CODE = 99;

    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // check login status
        checkLoginStatus();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = findViewById(R.id.fab);

        initPager();
        initTabs();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pager.setCurrentItem(0);
            }
        });
    }


    void initPager(){
        pager = findViewById(R.id.pager_main);
        pager.setAdapter(new MainPagerAdapter(getSupportFragmentManager()));
    }

    void initTabs(){
        tabLayout = findViewById(R.id.tablayout_main);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        String tabs[] = {
                "Scan", "Pending", "Approved"
        };
        for (String name: tabs){
            tabLayout.addTab(tabLayout.newTab().setText(name));

            // use camera icon for scan tab instead of text
            tabLayout.getTabAt(0).setText(null).setIcon(R.drawable.ic_camera);
        }

        tabLayout.addOnTabSelectedListener(this);
        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        pager.setCurrentItem(1);
    }

    void checkLoginStatus(){
        if (FirebaseAuth.getInstance().getCurrentUser() == null){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            this.finish();
        }
    }


    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        pager.setCurrentItem(tab.getPosition());
        switch (tab.getPosition()){
            case 0:
                fab.hide();
                try {
                    scan();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case 1:
                fab.show();
                break;
            case 2:
                fab.show();
                break;
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getExtras().getParcelable(ScanConstants.SCANNED_RESULT);
            try {
                Statics.bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                getContentResolver().delete(uri, null, null);

                // TODO: 9/9/17 call activity to upload bitmap
                Intent intent = new Intent(this, SendRecieptActivity.class);
                startActivity(intent);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        pager.setCurrentItem(1);
    }


    void scan() throws IOException {
        File tempFile = null;
        tempFile = File.createTempFile("photo", ".jpg", this.getCacheDir());
        tempFile.setWritable(true, false);

        Intent intent = new Intent(this, ScanActivity.class);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
        intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, ScanConstants.OPEN_CAMERA);

        //request camera permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            //ask for authorisation
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 50);
        else
            startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        pager.setCurrentItem(1);
        checkLoginStatus();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout){
            new Logout().logout();
            checkLoginStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}
