package com.byted.camp.todolist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.byted.camp.todolist.db.TodoContract;
import com.byted.camp.todolist.db.TodoDbHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NoteActivity extends AppCompatActivity {

    private EditText editText;
    private Button addBtn;
    private CheckBox lowBox;
    private CheckBox medBox;
    private CheckBox highBox;
    private String priority = "LOW";


    private TodoDbHelper dbHelper;
    private static final String TAG = "DatabaseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        setTitle(R.string.take_a_note);

        dbHelper = new TodoDbHelper(this);

        editText = findViewById(R.id.edit_text);
        editText.setFocusable(true);
        editText.requestFocus();
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.showSoftInput(editText, 0);
        }

        addBtn = findViewById(R.id.btn_add);
        lowBox = findViewById(R.id.checkBoxLow);
        medBox = findViewById(R.id.checkBoxMed);
        highBox = findViewById(R.id.checkBoxHigh);

        lowBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(lowBox.isChecked()){
                    medBox.setChecked(false);
                    highBox.setChecked(false);
                    lowBox.setClickable(false);
                    medBox.setClickable(true);
                    highBox.setClickable(true);
                    priority = "LOW";
                }
            }
        });
        medBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(medBox.isChecked()){
                    medBox.setClickable(false);
                    lowBox.setChecked(false);
                    highBox.setChecked(false);
                    lowBox.setClickable(true);
                    highBox.setClickable(true);
                    priority = "MEDIUM";
                }
            }
        });
        highBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(highBox.isChecked()){
                    highBox.setClickable(false);
                    lowBox.setChecked(false);
                    medBox.setChecked(false);
                    lowBox.setClickable(true);
                    medBox.setClickable(true);
                    priority = "HIGH";
                }
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence content = editText.getText();
                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(NoteActivity.this,
                            "No content to add", Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean succeed = saveNote2Database(content.toString().trim());
                if (succeed) {
                    Toast.makeText(NoteActivity.this,
                            "Note added", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                } else {
                    Toast.makeText(NoteActivity.this,
                            "Error", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }

    private boolean saveNote2Database(String content) {
        // TODO 插入一条新数据，返回是否插入成功

        // Gets the data repository in write mode
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Date date = new Date(System.currentTimeMillis());
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(TodoContract.TodoEntry.COLUMN_NAME_TITLE, content);
        values.put(TodoContract.TodoEntry.COLUMN_NAME_DATE, df.format(date));
        values.put(TodoContract.TodoEntry.COLUMN_NAME_PRIORITY, priority);

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(TodoContract.TodoEntry.TABLE_NAME, null, values);
        Log.i(TAG, "perform add data, result:" + newRowId);
        if (newRowId < 0)
            return false;
        else
            return true;
    }
}
