package com.example.user.smartlist;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class ActivityTwo extends Activity implements OnClickListener {

    String[] data = {"Иностранный язык", "Литература", "Русский язык", "Физика", "Обж", "Физическая культура", "Информатика", "Математика", "География", "История", "Обществознание", "Химия", "Биология"};
    String[] data2 = {"2", "3", "4", "5"};

    final String LOG_TAG = "myLogs";

    Button btnAdd, btnRead, btnClear;
    Spinner selected;
    Spinner selected2;
    DBHelper dbHelper;
    private int id = 1;
    private NotificationCompat.Builder notification_builder;
    private NotificationManagerCompat notification_manager;

    /** Called when the activity is first created. */
    private static final int NOTIFY_ID = 101;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two);

        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(this);

        btnRead = (Button) findViewById(R.id.btnRead);
        btnRead.setOnClickListener(this);

        btnClear = (Button) findViewById(R.id.btnClear);
        btnClear.setOnClickListener(this);

        selected = (Spinner) findViewById(R.id.spinner);
        selected2 = (Spinner) findViewById(R.id.spinner2);

        // создаем объект для создания и управления версиями БД
        dbHelper = new DBHelper(this);

        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        // Настраиваем адаптер
        ArrayAdapter<?> adapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        spinner.setPrompt("Предмет");

        final Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);

        ArrayAdapter<?> adapter2 =
                new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data2);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner2.setAdapter(adapter2);
        spinner2.setPrompt("Оценка");
        spinner2.setSelection(3);

    }
    @Override
    public void onClick(View v) {

        // создаем объект для данных
        ContentValues cv = new ContentValues();

        // получаем данные из полей ввода
        String subject = selected.getSelectedItem().toString();
        String mark = selected2.getSelectedItem().toString();

        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Intent notification = new Intent(this, ActivityTwo.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, notification,
                PendingIntent.FLAG_CANCEL_CURRENT);
        Resources res = this.getResources();

        String selection = null;
        String[] selectionArgs = null;

        Cursor c = null;


        switch (v.getId()) {
            case R.id.btnAdd:
                Log.d(LOG_TAG, "--- Insert in mytable: ---");
                // подготовим данные для вставки

                cv.put("subject", subject);
                cv.put("mark", mark);
                // вставляем запись и получаем ее ID
                long rowID = db.insert("mytable", null, cv);
                Log.d(LOG_TAG, "row inserted, ID = " + rowID);

                break;

            case R.id.btnRead:
                Log.d(LOG_TAG, "--- Rows in mytable: ---");
                // делаем запрос всех данных из таблицы
                Cursor c2 = db.query("mytable", null, null, null, null, null, null);

                if (c2.moveToFirst()) {

                    int idColIndex = c2.getColumnIndex("id");
                    int subjectColIndex = c2.getColumnIndex("subject");
                    int markColIndex = c2.getColumnIndex("mark");

                    do {

                        Log.d(LOG_TAG,
                                "ID = " + c2.getInt(idColIndex) +
                                        ", subject = " + c2.getString(subjectColIndex) +
                                        ", mark = " + c2.getString(markColIndex));
                    } while (c2.moveToNext());
                } else
                    Log.d(LOG_TAG, "0 rows");
                c2.close();
                break;


            case R.id.btnClear:
                Log.d(LOG_TAG, "--- Clear mytable: ---");
                // удаляем все записи
                int clearCount = db.delete("mytable", null, null);
                Log.d(LOG_TAG, "deleted rows count = " + clearCount);
                break;

        }
        switch (v.getId()) {
            // Все записи
            case R.id.btnAdd:
                //notification_manager.notify(id, notification_builder.build());
                Log.d(LOG_TAG, "--- предметы ---");
                selection = "subject = ?";
                selectionArgs = new String[] { subject };
                c = db.query("mytable", null, selection, selectionArgs, null, null,
                        null);
                break;
        }
        String res2 = "";
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
                            res2 += c.getString(c.getColumnIndex(cn)) + ", ";
                            finalMark += Integer.parseInt(c.getString(c.getColumnIndex(cn)));
                            count++;
                        }

                    }
                    Log.d(LOG_TAG, str);
                    Log.d(LOG_TAG, res2);

                } while (c.moveToNext());
            }
        }
        sendNotification("Полученная оценка: " + mark + "; " + "Средний балл: " + Double.toString(  Math.rint(100.0 * finalMark / count) / 100.0 ), subject, "a");



        // закрываем подключение к БД
        dbHelper.close();
    }



    class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            super(context, "DB", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(LOG_TAG, "--- onCreate database ---");
            // создаем таблицу
            db.execSQL("create table mytable ("
                    + "id integer primary key autoincrement,"
                    + "subject text,"
                    + "mark text" + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
    private void sendNotification(String message, String title,String msg_id) {
        int notifyID = 0;
        try {
            notifyID = Integer.parseInt(msg_id);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        String CHANNEL_ID = "my_channel_01";            // The id of the channel.
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("message", message);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);


        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        //Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "01")
                .setContentTitle(title)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setContentText(message)
                .setAutoCancel(true)
                //.setSound(defaultSoundUri)
                .setChannelId(CHANNEL_ID)
                .setContentIntent(pendingIntent);


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {       
            CharSequence name = "My New Channel";                   
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,name, importance); 
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(notifyID /* ID of notification */, notificationBuilder.build());
    }

}
