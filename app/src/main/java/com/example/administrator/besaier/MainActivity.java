package com.example.administrator.besaier;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private ControlView myView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myView = findViewById(R.id.myView);
    }

    public void start(View view) {
        myView.start();
    }

    public void add(View view) {
        myView.setEditState(ControlView.ADD_BEZIER_STATE);
    }

    public void move(View view) {
        myView.setEditState(ControlView.MOVE_BEZIER_STATE);
    }

    public void remove(View view) {
        myView.setEditState(ControlView.REMOVE_BEZIER_STATE);
    }

    public void edit(View view) {
        myView.setShowState(ControlView.EDIT_MODE);
    }

    public void show(View view) {
        myView.setShowState(ControlView.SHOW_MODE);
    }
}
