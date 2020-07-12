package com.splitwisr;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.os.Bundle;

import com.splitwisr.data.AppDatabase;
import com.splitwisr.ui.main.MainFragment;
import com.splitwisr.ui.main.MainViewModel;
import com.splitwisr.ui.main.ReceiptFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        MainViewModel m = new MainViewModel(this.getApplication());
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, ReceiptFragment.newInstance())
                    .commitNow();
        }
    }
}