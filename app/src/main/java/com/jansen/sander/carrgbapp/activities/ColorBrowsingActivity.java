package com.jansen.sander.carrgbapp.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;

import com.jansen.sander.carrgbapp.AppDatabase;
import com.jansen.sander.carrgbapp.R;
import com.jansen.sander.carrgbapp.classes.CustomColor;
import com.jansen.sander.carrgbapp.classes.CustomColorDataAdapter;
import com.jansen.sander.carrgbapp.classes.SwipeController;
import com.jansen.sander.carrgbapp.classes.SwipeControllerActions;

import java.util.ArrayList;
import java.util.List;


public class ColorBrowsingActivity extends AppCompatActivity {

    private Snackbar mySnackbar;
    private int cid;
    private CustomColorDataAdapter mAdapter;
    private SwipeController swipeController;
    ArrayList<SeekBar> sliders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_browsing);
        setupActionBar();
        mySnackbar = MainActivity.mySnackbar;
        View sbView = mySnackbar.getView();
        sbView.setBackgroundColor(Color.parseColor("#3C4149"));
        sliders = (ArrayList<SeekBar>) MainActivity.getSliders();

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
            builder.setMessage(R.string.deleteAll).setPositiveButton("Yes", dialogClickListener)
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
                    int size = mAdapter.getCustomColors().size();
                    mAdapter.getCustomColors().clear();
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
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(mAdapter);

        swipeController = new SwipeController(new SwipeControllerActions() {
            @Override
            public void onRightClicked(int position) {
                cid = mAdapter.getCid(position);
                new DeleteSavedColorsTask().execute((Void)null);

                mAdapter.getCustomColors().remove(position);
                mAdapter.notifyItemRemoved(position);
                mAdapter.notifyItemRangeChanged(position, mAdapter.getItemCount());
            }
            @Override
            public void onLeftClicked(int position) {
                cid = mAdapter.getCid(position);
                new GetSavedColorByIdTask().execute((Void)null);
            }
        });
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeController);
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
            allSavedColors = AppDatabase.getInstance(getApplicationContext()).color_db_api().getStoredColors();
            for (CustomColor colorX : allSavedColors){
                Log.v("Color", "RGB: " + colorX.getRed() + ", " + colorX.getGreen() + ", " +  colorX.getBlue() + "   id: " + colorX.getCid());
            }
            return allSavedColors;
        }

        @Override
        protected void onPostExecute(final List<CustomColor> allSavedColors) {
            mAdapter = new CustomColorDataAdapter(allSavedColors);
            setupRecyclerView();
        }
    }

    public class GetSavedColorByIdTask extends AsyncTask<Void, Void, Boolean > {
        @Override
        protected Boolean doInBackground(Void... voids) {
            List<CustomColor> colorByCid = AppDatabase.getInstance(getApplicationContext()).color_db_api().colorByCid(cid);
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
                mySnackbar.setText(R.string.errorLoad).show();
            }
        }
    }

    public class DeleteSavedColorsTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            AppDatabase.getInstance(getApplicationContext()).color_db_api().deleteById(cid);
            return true;
        }
    }

    public class DeleteAllColorsTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            AppDatabase.getInstance(getApplicationContext()).color_db_api().reset();
            return true;
        }
    }
}
