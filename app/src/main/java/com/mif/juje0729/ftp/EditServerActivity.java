package com.mif.juje0729.ftp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.example.R;


import java.util.Set;

public class EditServerActivity extends Activity {
    public final static String EXTRA_MESSAGE = "com.example.NoteTaking.MESSAGE";
    String action = new String(),action2 = new String(),oldServer = new String();

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.editfile);
        Intent intent = getIntent();
        EditText text = (EditText) findViewById(R.id.editText1);

        Bundle extras = intent.getExtras();
        Set<String> keypair = extras.keySet();

        if ( extras.getString("ACTION").equals("oldServer")) {
            action = "oldServer";
            if ( keypair.size() == 3 ) {
                if ( extras.getString("ACTION2").equals("replace")) {
                    action2 = "replace";
                }
            }
            oldServer = extras.getString(EXTRA_MESSAGE);
            text.setText(extras.getString(EXTRA_MESSAGE));
        }
    }

    public void onClickSave(View theButton) {
        EditText text = (EditText) findViewById(R.id.editText1);
        NotesDatabase db =new NotesDatabase(this);

        if ( action.equals("oldServer") && action2.equals("replace")) {
            db.searchAndDelete(this, oldServer);
        }
        db.addNote(this, new Note(text.getText().toString()));
        finish();
    }
    public void onClickBack(View theButton) {
        finish();
    }
}