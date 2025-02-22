package com.sgp.packsmart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.sgp.packsmart.Adapter.CheckListAdapter;
import com.sgp.packsmart.Data.AppData;
import com.sgp.packsmart.Database.RoomDB;
import com.sgp.packsmart.Models.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CheckList extends AppCompatActivity {

    RecyclerView recyclerView;
    CheckListAdapter checkListAdapter;
    RoomDB database;
    List<Items> itemsList = new ArrayList<>();
    String header, show;

    EditText txtAdd;
    Button btnAdd;
    LinearLayout linearLayout;
    // private String newText;

    @Override
    public boolean onCreatePanelMenu(int featureId, @NonNull Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_one, menu);

        if (MyConstants.MY_SELECTIONS.equals(header)) {
            menu.getItem(0).setVisible(false);
            menu.getItem(2).setVisible(false);
            menu.getItem(3).setVisible(false);
        } else if (MyConstants.MY_LIST_CAMEL_CASE.equals(header))
            menu.getItem(1).setVisible(false);

       // MenuItem menuItem = menu.findItem(R.id.btnSearch);
        //SearchView searchView = (SearchView) menuItem.getActionView();
        /*searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                List<Items> mFinalList = new ArrayList<>();
                for (Items items : itemsList) {
                    if (items.getItemname().toLowerCase().startsWith(newText.toLowerCase())) {
                        mFinalList.add(items);
                    }

                }
                updateRecycler(mFinalList);
                return false;
            }
        });*/

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent = new Intent(this, CheckList.class);
        AppData appData = new AppData(database, this);

        int id = item.getItemId();



            if (id == R.id.btnMySelections) {
                intent.putExtra(MyConstants.HEADER_SMALL, MyConstants.MY_SELECTIONS);
                intent.putExtra(MyConstants.SHOW_SMALL, MyConstants.FALSE_STRING);
                startActivityForResult(intent, 101);
            } else if (id == R.id.btnCustomList) {
                intent.putExtra(MyConstants.HEADER_SMALL, MyConstants.MY_LIST_CAMEL_CASE);
                intent.putExtra(MyConstants.SHOW_SMALL, MyConstants.TRUE_STRING);
                startActivity(intent);
            } else if (id == R.id.btnDeleteDefault) {
                new AlertDialog.Builder(this)
                        .setTitle("Delete default data")
                        .setMessage("Are you sure?\n\nAs this will delete the data provided by (PACKSMART) while installing")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                appData.persistDataByCategory(header, true);
                                itemsList = database.mainDao().getAll(header);
                                updateRecycler(itemsList);
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).setIcon(R.drawable.ic_alert)
                        .show();
            } else if (id == R.id.btnReset) {
                new AlertDialog.Builder(this)
                        .setTitle("Reset to default")
                        .setMessage("Are you sure?\n\nAs this will load the default data provided by (PACKSMART)" +
                                "and will delete the custom data you have added in ( " + header + " )")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                appData.persistDataByCategory(header, false);
                                itemsList = database.mainDao().getAll(header);
                                updateRecycler(itemsList);
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).setIcon(R.drawable.ic_alert)
                        .show();
            } else if (id == R.id.btnExit) {
                this.finishAffinity();
                Toast.makeText(this, "PACKSMART\nExit completed", Toast.LENGTH_SHORT).show();
            }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_list);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        header = intent.getStringExtra(MyConstants.HEADER_SMALL);
        show = intent.getStringExtra(MyConstants.SHOW_SMALL);

        Objects.requireNonNull(getSupportActionBar()).setTitle(header);

        txtAdd = findViewById(R.id.txtAdd);
        btnAdd = findViewById(R.id.btnAdd);
        recyclerView = findViewById(R.id.recyclerView);
        linearLayout = findViewById(R.id.linearLayout);

        database = RoomDB.getInstance(this);

        if (MyConstants.FALSE_STRING.equals(show)) {
            linearLayout.setVisibility(View.GONE);
            itemsList = database.mainDao().getAllSelected(true);
        } else {
            itemsList = database.mainDao().getAll(header);
        }

        updateRecycler(itemsList);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String itemname = txtAdd.getText().toString();
                if (itemname != null && !itemname.isEmpty()) {
                    addNewItem(itemname);
                    Toast.makeText(CheckList.this, "Item added", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CheckList.this, "Item cannot be added..", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void addNewItem(String itemname) {
        Items item = new Items();
        item.setChecked(false);
        item.setCategory(header);
        item.setItemname(itemname);
        item.setAddedby(MyConstants.USER_SMALL);
        database.mainDao().saveItem(item);
        itemsList = database.mainDao().getAll(header);
        updateRecycler(itemsList);
        recyclerView.scrollToPosition(checkListAdapter.getItemCount() - 1);
        txtAdd.setText("");
    }

    private void updateRecycler(List<Items> itemsList) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, LinearLayoutManager.VERTICAL));
        checkListAdapter = new CheckListAdapter(CheckList.this, itemsList, database, show);
        recyclerView.setAdapter(checkListAdapter);
    }

}