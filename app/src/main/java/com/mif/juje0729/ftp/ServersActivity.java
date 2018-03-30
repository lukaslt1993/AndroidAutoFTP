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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.R;

import java.util.List;

public class ServersActivity extends Activity {
    public final static String EXTRA_MESSAGE = "com.example.NoteTaking.MESSAGE";
    String currentServer = new String();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addnote);
    }

    public void addNote(View theButton) {
        Intent intent = new Intent(this, EditServerActivity.class);
        intent.putExtra("ACTION", "addServer");
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
        add.setText("+ Prideti serveri");
        lLayout.addView(add);
        NotesDatabase db = new NotesDatabase(this);

        List<Note> servers = db.getAllNotes(this);

        for (Note server : servers) {

            TextView b = (TextView) inflater.inflate(R.layout.textviews, null);
            b.setTextColor(Color.BLACK);
            b.setText(server.getNote());
            registerForContextMenu(b);
            lLayout.addView(b);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo
            menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        currentServer = ((TextView) v).getText().toString();
        menu.add(0, v.getId(), 0, "Modifikuoti");
        menu.add(0, v.getId(), 1, "Istrinti");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        super.onContextItemSelected(item);

        if (item.getTitle().toString().equals("Istrinti")) {
            NotesDatabase db = new NotesDatabase(this);

            db.searchAndDelete(this, currentServer);
            onResume();
        } else if (item.getTitle().toString().equals("Modifikuoti")) {
            Intent intent = new Intent(this, EditServerActivity.class);
            intent.putExtra("ACTION", "oldServer");
            intent.putExtra("ACTION2", "replace");
            intent.putExtra(EXTRA_MESSAGE, currentServer);
            startActivity(intent);
        }

        return true;
    }

    public void onClickBack(View theButton) {
        finish();
    }

}
