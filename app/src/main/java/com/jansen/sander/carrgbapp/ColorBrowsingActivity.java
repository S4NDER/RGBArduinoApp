package com.jansen.sander.carrgbapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


public class ColorBrowsingActivity extends AppCompatActivity {

    private CustomColorDataAdapter mAdapter;
    private SwipeController swipeController;
    private ItemTouchHelper itemTouchHelper;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_browsing);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        getAllSavedColors();

    }

    private void getAllSavedColors(){
        new GetAllSavedColorsTask().execute((Void)null);
    }

    private void setupRecyclerView(){
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(mAdapter);

        Looper.prepare(); //Needs to be called, otherwise you get this error message: Can't create handler inside thread that has not called Looper.prepare()
        swipeController = new SwipeController();
        itemTouchHelper = new ItemTouchHelper(swipeController);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public class GetAllSavedColorsTask extends AsyncTask<Void, Void, List<CustomColor>> {
        private List<CustomColor> allSavedColors = new ArrayList<CustomColor>();

        @Override
        protected List<CustomColor> doInBackground(Void... voids) {
            allSavedColors = AppDatabase.getInstance(getApplicationContext()).color_db_api()
                    .getStoredColors();

            for (CustomColor colorX : allSavedColors){
                Log.v("Color", "RGB: " + colorX.getRed() + ", " + colorX.getGreen() + ", " +  colorX.getBlue() + "   id: " + colorX.getCid());
            }
            mAdapter = new CustomColorDataAdapter(allSavedColors);
            setupRecyclerView();

            return allSavedColors;
        }
    }

}
