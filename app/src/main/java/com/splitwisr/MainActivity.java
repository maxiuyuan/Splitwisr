package com.splitwisr;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    NavController navController;
    private boolean showLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        setSupportActionBar(findViewById(R.id.toolbar));

        this.initNav();
        this.initFirebase();
    }

    private void initNav() {
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(
                (BottomNavigationView) findViewById(R.id.bottom_navigation),
                navController
        );
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // User is signed in
                showLogout = true;
                invalidateOptionsMenu();
                findViewById(R.id.bottom_navigation).setVisibility(View.VISIBLE);
                NavHostFragment
                        .findNavController(getSupportFragmentManager().getPrimaryNavigationFragment())
                        .navigate(R.id.destination_balance_fragment);
            } else {
                // User is signed out
                showLogout = false;
                invalidateOptionsMenu();
                findViewById(R.id.bottom_navigation).setVisibility(View.GONE);
                NavHostFragment
                        .findNavController(getSupportFragmentManager().getPrimaryNavigationFragment())
                        .navigate(R.id.destination_login_fragment);
            }
        };
    }

    // Add Auth state listener in onStart method.
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_bar, menu);
        menu.findItem(R.id.log_out).setVisible(showLogout);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.log_out && mAuth.getCurrentUser() != null) {
            mAuth.signOut();
            showLogout = false;
            invalidateOptionsMenu();
        }
        return super.onOptionsItemSelected(item);
    }
}