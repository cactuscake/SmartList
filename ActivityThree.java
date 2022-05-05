package com.example.user.smartlist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityThree extends Activity implements OnClickListener {

    String[] data = {"Иностранный язык", "Литература", "Русский язык", "Физика", "Обж", "Физическая культура", "Информатика", "Математика", "География", "История", "Обществознание", "Химия", "Биология"};

    final String LOG_TAG = "myLogs";

    Button btnRead;
    TextView text;
    TextView text2;
    Spinner selected;
    DBHelper dbHelper;
    /** Called when the activity is first created. */


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_three);

        text = (TextView) findViewById(R.id.textView);
        text2 = (TextView) findViewById(R.id.textView2);

        btnRead = (Button) findViewById(R.id.btnRead);
        btnRead.setOnClickListener(this);

        selected = (Spinner) findViewById(R.id.spinner);

        // создаем объект для создания и управления версиями БД
        dbHelper = new DBHelper(this);

        // адаптер
        // Получаем экземпляр элемента Spinner
        final Spinner spinner = (Spinner)findViewById(R.id.spinner);

// Настраиваем адаптер
        ArrayAdapter<?> adapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

// Вызываем адаптер
        spinner.setAdapter(adapter);

    }
    @Override
    public void onClick(View v) {

        // создаем объект для данных
        ContentValues cv = new ContentValues();

        // получаем данные из полей ввода
        String subject = selected.getSelectedItem().toString();

        String selection3 = null;
        String[] selectionArg = null;

        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // переменные для query
        String[] columns = null;
        String selection = null;
        String[] selectionArgs = null;
        String groupBy = null;
        String having = null;
        String orderBy = null;

        // курсор
        Cursor c = null;

        // определяем нажатую кнопку
        switch (v.getId()) {
            // Все записи
            case R.id.btnRead:
                Log.d(LOG_TAG, "--- предметы ---");
                selection = "subject = ?";
                selectionArgs = new String[] { subject };
                c = db.query("mytable", null, selection, selectionArgs, null, null,
                        null);
                break;
        }
        String res = "";
        double finalMark = 0;
        double count = 0;
        if (c != null) {
            if (c.moveToFirst()) {
                String str;
                do {
                    str = "";

                    for (String cn : c.getColumnNames()) {
                        str = str.concat(cn + " = "
                                + c.getString(c.getColumnIndex(cn)) + "; ");

                        if (cn.equals("mark")) {
                            res += c.getString(c.getColumnIndex(cn))+ ", ";
                            finalMark += Integer.parseInt(c.getString(c.getColumnIndex(cn)));
                            count++;
                        }

                    }
                    Log.d(LOG_TAG, str);
                    Log.d(LOG_TAG, res);

                } while (c.moveToNext());
            }
            c.close();

        } else
            Log.d(LOG_TAG, "Cursor is null");

        dbHelper.close();

        text.setText("Все оценки: " + res);
        text2.setText("Средний бал: " + Double.toString(finalMark / count));
    }



    class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            // конструктор суперкласса
            super(context, "DB", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(LOG_TAG, "--- onCreate database ---");
            // создаем таблицу с полями
            db.execSQL("create table mytable ("
                    + "id integer primary key autoincrement,"
                    + "subject text,"
                    + "mark text" + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
