package com.jansen.sander.carrgbapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ColorBrowsingActivity extends AppCompatActivity {

    private Snackbar mySnackbar;
    private int cid;
    private CustomColorDataAdapter mAdapter;
    private SwipeController swipeController;
    private ItemTouchHelper itemTouchHelper;
    protected List<CustomColor> allColors;
    protected RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_browsing);
//        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(myToolbar);
        setupActionBar();
        View sbView = MainActivity.mySnackbar.getView();
        sbView.setBackgroundColor(Color.parseColor("#3C4149"));

        getAllSavedColors();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        if (id == R.id.action_drop_database) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Delete all colors?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.database_options, menu);
        return true;
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    new DeleteAllColorsTask().execute((Void)null);
                    int size = mAdapter.customColors.size();
                    mAdapter.customColors.clear();
                    mAdapter.notifyItemRangeRemoved(0, size);
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };

    private void getAllSavedColors(){
        new GetAllSavedColorsTask().execute((Void)null);
    }

    private void setupRecyclerView(){
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(mAdapter);

        //Looper.prepare(); //Needs to be called, otherwise you get this error message: Can't create handler inside thread that has not called Looper.prepare()
        swipeController = new SwipeController(new SwipeControllerActions() {
            @Override
            public void onRightClicked(int position) {
                cid = mAdapter.getCid(position);
                new DeleteSavedColorsTask().execute((Void)null);

                mAdapter.customColors.remove(position);
                mAdapter.notifyItemRemoved(position);
                mAdapter.notifyItemRangeChanged(position, mAdapter.getItemCount());
            }
            @Override
            public void onLeftClicked(int position) {
                cid = mAdapter.getCid(position);
                new GetSavedColorByIdTask().execute((Void)null);
            }
        });
        itemTouchHelper = new ItemTouchHelper(swipeController);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                swipeController.onDraw(c);
            }
        });
    }

    public class GetAllSavedColorsTask extends AsyncTask<Void, Void, List<CustomColor>> {
        private List<CustomColor> allSavedColors = new ArrayList<>();

        @Override
        protected List<CustomColor> doInBackground(Void... voids) {
            allSavedColors = AppDatabase.getInstance(getApplicationContext()).color_db_api()
                    .getStoredColors();

            for (CustomColor colorX : allSavedColors){
                Log.v("Color", "RGB: " + colorX.getRed() + ", " + colorX.getGreen() + ", " +  colorX.getBlue() + "   id: " + colorX.getCid());
            }
            return allSavedColors;
        }

        @Override
        protected void onPostExecute(final List<CustomColor> allSavedColors) {
            mAdapter = new CustomColorDataAdapter(allSavedColors);
            allColors = allSavedColors;
            setupRecyclerView();
        }
    }

    public class GetSavedColorByIdTask extends AsyncTask<Void, Void, Boolean > {

        @Override
        protected Boolean doInBackground(Void... voids) {
            List<CustomColor> colorByCid = AppDatabase.getInstance(getApplicationContext()).color_db_api()
                    .colorByCid(cid);
            ArrayList<SeekBar> sliders = (ArrayList<SeekBar>) MainActivity.getSliders();
            for (CustomColor colorX : colorByCid){
                Log.v("Color", "RGB: " + colorX.getRed() + ", " + colorX.getGreen() + ", " +  colorX.getBlue() + "   id: " + colorX.getCid());
                sliders.get(0).setProgress(colorX.getRed());
                sliders.get(1).setProgress(colorX.getGreen());
                sliders.get(2).setProgress(colorX.getBlue());
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                finish();

            } else {
                mySnackbar.setText("Could not load color");
                mySnackbar.show();
            }
        }
    }

    public class DeleteSavedColorsTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            AppDatabase.getInstance(getApplicationContext()).color_db_api()
                    .deleteById(cid);
            return true;
        }
    }

    public class DeleteAllColorsTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            AppDatabase.getInstance(getApplicationContext()).color_db_api()
                    .reset();
            return true;
        }
    }

}
