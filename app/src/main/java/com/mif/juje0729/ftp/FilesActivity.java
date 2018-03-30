package com.mif.juje0729.ftp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import com.example.R;

import java.util.List;

public class FilesActivity extends Activity {
    public final static String EXTRA_MESSAGE = "com.example.NoteTaking.MESSAGE";
    String currentFile = new String();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addnote);
    }

    public void addNote(View theButton) {
        Intent intent = new Intent(this, EditFileActivity.class);
        intent.putExtra("ACTION", "addFile");
        startActivity(intent);
    }

    public void onResume() {
        super.onResume();
        setContentView(R.layout.addnote);

        LinearLayout lLayout = (LinearLayout) findViewById(R.id.layout1);
        lLayout.removeAllViews();

        final LayoutInflater inflater = (LayoutInflater) getSystemService(Context
                .LAYOUT_INFLATER_SERVICE);

        Button add = (Button) inflater.inflate(R.layout.addnotebutton, null);
        add.setText("+ Prideti faila");
        lLayout.addView(add);
        NotesDatabase db = new NotesDatabase(this);

        List<Note> files = db.getAllNotes(this);

        for (Note file : files) {

            TextView b = (TextView) inflater.inflate(R.layout.textviews, null);
            b.setTextColor(Color.BLACK);
            b.setText(file.getNote());
            registerForContextMenu(b);
            lLayout.addView(b);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo
            menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        currentFile = ((TextView) v).getText().toString();
        menu.add(0, v.getId(), 0, "Modifikuoti");
        menu.add(0, v.getId(), 1, "Istrinti");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        super.onContextItemSelected(item);

        if (item.getTitle().toString().equals("Istrinti")) {
            NotesDatabase db = new NotesDatabase(this);

            db.searchAndDelete(this, currentFile);
            onResume();
        } else if (item.getTitle().toString().equals("Modifikuoti")) {
            Intent intent = new Intent(this, EditFileActivity.class);
            intent.putExtra("ACTION", "oldFile");
            intent.putExtra("ACTION2", "replace");
            intent.putExtra(EXTRA_MESSAGE, currentFile);
            startActivity(intent);
        }

        return true;
    }

    public void onClickBack(View theButton) {
        finish();
    }

}

