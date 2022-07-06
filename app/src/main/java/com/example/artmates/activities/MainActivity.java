package com.example.artmates.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.artmates.R;
import com.example.artmates.fragments.ChatFragment;
import com.example.artmates.fragments.ComposeFragment;
import com.example.artmates.fragments.LocationFragment;
import com.example.artmates.fragments.PostsFragment;
import com.example.artmates.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    final FragmentManager fragmentManager = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment fragment;
                switch (menuItem.getItemId()) {
                    case R.id.actionHome:
                        fragment = new PostsFragment();

                        break;
                    case R.id.actionCompose:
                        fragment = new ComposeFragment();
                        break;
                    case R.id.actionLocation:
                        fragment = new LocationFragment();
                        break;
                    case R.id.actionChat:
                        fragment = new ChatFragment();
                        break;
                    case R.id.actionProfile:
                    default:
                        fragment = new ProfileFragment();
                        break;
                }
                fragmentManager.beginTransaction()
                        .replace(R.id.flContainer, fragment)
                        .addToBackStack(null)
                        .commit();
                return true;
            }
        });
        bottomNavigationView.setSelectedItemId(R.id.actionHome);

    }
}
