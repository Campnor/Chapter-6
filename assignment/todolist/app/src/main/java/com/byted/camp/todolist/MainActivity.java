package com.byted.camp.todolist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.byted.camp.todolist.beans.Note;
import com.byted.camp.todolist.db.TodoContract;
import com.byted.camp.todolist.db.TodoDbHelper;
import com.byted.camp.todolist.operation.activity.DatabaseActivity;
import com.byted.camp.todolist.operation.activity.DebugActivity;
import com.byted.camp.todolist.operation.activity.SettingActivity;
import com.byted.camp.todolist.ui.NoteListAdapter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADD = 1002;
    private static final String TAG = "DatabaseActivity";

    private RecyclerView recyclerView;
    private NoteListAdapter notesAdapter;

    private TodoDbHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = new TodoDbHelper(this);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                        new Intent(MainActivity.this, NoteActivity.class),
                        REQUEST_CODE_ADD);
            }
        });

        recyclerView = findViewById(R.id.list_todo);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        notesAdapter = new NoteListAdapter(new NoteOperator() {
            @Override
            public void deleteNote(Note note) {
                MainActivity.this.deleteNote(note);
                try {
                    notesAdapter.refresh(loadNotesFromDatabase());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void updateNote(Note note) {
                MainActivity.this.updateNode(note);
                try {
                    notesAdapter.refresh(loadNotesFromDatabase());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
        recyclerView.setAdapter(notesAdapter);

        try {
            notesAdapter.refresh(loadNotesFromDatabase());
        } catch (ParseException e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingActivity.class));
                return true;
            case R.id.action_debug:
                startActivity(new Intent(this, DebugActivity.class));
                return true;
            case R.id.action_database:
                startActivity(new Intent(this, DatabaseActivity.class));
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD
                && resultCode == Activity.RESULT_OK) {
            try {
                notesAdapter.refresh(loadNotesFromDatabase());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private List<Note> loadNotesFromDatabase() throws ParseException {
        // TODO 从数据库中查询数据，并转换成 JavaBeans

       // dbHelper = new TodoDbHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();


        List<Note> ret = new ArrayList<>();
        String[] projection = {
                BaseColumns._ID,
                TodoContract.TodoEntry.COLUMN_NAME_TITLE,
                TodoContract.TodoEntry.COLUMN_NAME_DATE,
                TodoContract.TodoEntry.COLUMN_NAME_PRIORITY
        };
        String selection = TodoContract.TodoEntry.COLUMN_NAME_PRIORITY + " = ?";


        String sortOrder =
                TodoContract.TodoEntry.COLUMN_NAME_DATE + " DESC";
        String[] Priorities = new String[]{"HIGH","MEDIUM","LOW"};

        for (int i=0;i<Priorities.length;i++) {
            String[] selectionArgs = new String[]{Priorities[i]};
            Cursor cursor = db.query(
                TodoContract.TodoEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
            );

            while (cursor.moveToNext()) {
                long itemId = cursor.getLong(cursor.getColumnIndexOrThrow(TodoContract.TodoEntry._ID));
                String title = cursor.getString(cursor.getColumnIndex(TodoContract.TodoEntry.COLUMN_NAME_TITLE));
                String strData = cursor.getString(cursor.getColumnIndex(TodoContract.TodoEntry.COLUMN_NAME_DATE));
                String priority = cursor.getString(cursor.getColumnIndex(TodoContract.TodoEntry.COLUMN_NAME_PRIORITY));

                DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                Note record = new Note(itemId);
                record.setContent(title);
                Date date = df.parse(strData);
                record.setDate(date);
                record.setPriority(priority);
                ret.add(record);
                Log.i(TAG, "itemId:" + itemId + ", title:" + title + ", date:" + strData + ", priority:" + priority);
            }
            cursor.close();
        }


        return ret;
    }

    private void deleteNote(Note note) {
        // TODO 删除数据
        // Gets the data repository in write mode
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Define 'where' part of query.
        String selection = TodoContract.TodoEntry.COLUMN_NAME_TITLE + " LIKE ?";
        // Specify arguments in placeholder order.
        String[] selectionArgs = {note.getContent()};
        // Issue SQL statement.
        int deletedRows = db.delete(TodoContract.TodoEntry.TABLE_NAME, selection, selectionArgs);
        Log.i(TAG, "perform delete data, result:" + deletedRows);

    }


    private void updateNode(Note note) {
        // 更新数据

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // New value for one column
        String title = "title_1";
        ContentValues values = new ContentValues();
        values.put(TodoContract.TodoEntry.COLUMN_NAME_TITLE, title);

        // Which row to update, based on the title
        String selection = TodoContract.TodoEntry.COLUMN_NAME_TITLE + " LIKE ?";
        String[] selectionArgs = {note.getContent()};

        int count = db.update(
                TodoContract.TodoEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
        Log.i(TAG, "perform update data, result:" + count);
    }

}
