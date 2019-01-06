package com.example.anshulaggarwal.dumbphone;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    TextView tv_currentDate, tv_currentTime;
    public static final String Tag = "------------------->";
    public static String buttonPackageName = null;
    public static String buttonLabel = "Select Custom Application";
    /*---------DATABASE---------------------------------------------*/
    SQLiteDatabase sqLiteDatabase;
    public static final String DATABASE_NAME = "DumbPhoneDb";
    public static final String BUTTON_DATA_TABLE_Name = "ButtonTable";
    public static final String SETTINGS_DATA_TABLE_NAME = "SettingsTable";
    public static final String BUTTON_DATA_TABLE_COLUMN_1 = "id";
    public static final String BUTTON_DATA_TABLE_COLUMN_2 = "applicationLabel";
    public static final String BUTTON_DATA_TABLE_COLUMN_3 = "applicationPackage";
    public static final int BUTTON_DATA_TABLE_LABEL_INDEX = 1;
    public static final int BUTTON_DATA_TABLE_PACKAGE_INDEX = 2;
    public static final String SETTINGS_DATA_TABLE_COLUMN1 = "applicationCount";
    public static int applicationCount = 3; /*The Default value */
    /*---------------- END OF DATABASE -----------------------------*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*------------------------  Date & Time Utility --------------------------------------*/
        tv_currentTime = (TextView) findViewById(R.id.tv_currentTime);
        tv_currentDate = (TextView) findViewById(R.id.tv_currentDate);
        final Handler someHandler = new Handler(getMainLooper());
        someHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showDateTime();
                someHandler.postDelayed(this, 1000);
            }
        }, 10);
        /*-------------------- End of Date & Time Utility -----------------------------------*/

        Button btn_settings = (Button)findViewById(R.id.btn_settings);
        btn_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });


        /*-------------------- For Application Launching Buttons ----------------------------*/

        // this.deleteDatabase(DATABASE_NAME+".db");
        Log.d(Tag, "Database deleted...");
        createTable();
        applicationCount = getApplicationCount();
        LinearLayout flexboxLayout = (LinearLayout) findViewById(R.id.flexboxLayout);
        /*      flexboxLayout.setFlexDirection(FlexDirection.ROW);*/

        sqLiteDatabase = openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
        for (int button_count = 0; button_count < applicationCount; button_count += 1) {
            //Log.d(Tag, "button_coun value is: " + button_count);
            final Button newButton = new Button(this);
            Cursor cursor = sqLiteDatabase.rawQuery("select * from " + BUTTON_DATA_TABLE_Name + " where id=" + Integer.toString(button_count) + ";", null);
            cursor.moveToFirst();
            
            /*---------------- Code excerpt for naming the button based on previous 
                                user applications selection -----------------------*/
            if (cursor.getCount() > 0) {
                String dbRetrievedApplicationLabel = cursor.getString(1);
                newButton.setText(dbRetrievedApplicationLabel);
            } else {
                newButton.setText("Set application:" + button_count);
            }
            /*---------------------------------------------------------------------*/

            /*--------- Button properties -----------*/
            newButton.setTextColor(Color.WHITE);
            newButton.setId(button_count);
            newButton.setBackgroundColor(Color.BLUE);
            newButton.setBackgroundResource(R.drawable.btn_style);
            newButton.setPadding(185, 0, 185, 0);

            /*
            newButton.setLayoutParams(new FlexboxLayout.LayoutParams(FlexboxLayout.LayoutParams.WRAP_CONTENT, FlexboxLayout.LayoutParams.WRAP_CONTENT));
            */

            /* ---------------- Dummy button to add the desired spacing -------------*/
            Button dummyButton = new Button(this);
            dummyButton.setId(button_count * 100);
            dummyButton.setBackgroundColor(Color.TRANSPARENT);
            flexboxLayout.addView(dummyButton);
            /* ---------------------------------------------------------------------- */

            flexboxLayout.addView(newButton);
            Log.d(Tag, "Button: " + button_count + " added to the layout");
            /*-----------------------------------------*/


            /*--------------- Button's Application Launching action -----------   */
            final int finalButton_count = button_count;
            newButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Cursor cursor = sqLiteDatabase.rawQuery("select * from " + BUTTON_DATA_TABLE_Name + " where id=" + Integer.toString(finalButton_count) + ";", null);
                    cursor.moveToFirst();
                    Log.d(Tag, "Cursor count for button count: " + finalButton_count + " is: " + cursor.getCount());
                    if (cursor.getCount() != 0) {

                        /*The data for this is available*/
                        String local_packageName = cursor.getString(BUTTON_DATA_TABLE_PACKAGE_INDEX);
                        Log.d(Tag, "requesting the launch intent for package: " + local_packageName);
                        startActivity(new Intent(getPackageManager().getLaunchIntentForPackage(local_packageName)));
                    } else {

                        /*the application corresponding to this newButton has  not been selected yet*/
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Choose an application");
                        final ArrayList<String> myApplicationLabelList = new ArrayList<>();

                        final PackageManager packageManager = getPackageManager();
                        final List<ApplicationInfo> myInstalledApplicationInfoList = packageManager.getInstalledApplications(packageManager.GET_META_DATA);
                        for (ApplicationInfo application : myInstalledApplicationInfoList) {
                            String packageName = application.packageName;
                            try {
                                ApplicationInfo thisApplicationInfo = packageManager.getApplicationInfo(packageName, packageManager.GET_META_DATA);
                                String applicationLabel = (String) packageManager.getApplicationLabel(thisApplicationInfo);
                                myApplicationLabelList.add(applicationLabel);
                                Log.d(Tag, "PACKAGE: " + packageName + " LABEL: " + applicationLabel);
                            } catch (PackageManager.NameNotFoundException e) {
                                e.printStackTrace();
                            }

                        }

                        builder.setSingleChoiceItems(myApplicationLabelList.toArray(new String[myApplicationLabelList.size()]), 1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String packageName = myInstalledApplicationInfoList.get(which).packageName; /*retrieved package name using label*/
                                String applicationLabel = myApplicationLabelList.get(which);
                                Log.d(Tag, "user selected application :" + which + " " + myApplicationLabelList.get(which));
                                packageManager.getLaunchIntentForPackage(packageName);
                                buttonPackageName = packageName;
                                buttonLabel = applicationLabel;
                            }
                        });
                        builder.setPositiveButton("Select", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {


                                /*Storing the selected application data into the database*/
                                String query = "insert into " + BUTTON_DATA_TABLE_Name + " values(" + finalButton_count + ", '" + buttonLabel + "', '" + buttonPackageName + "');";
                                sqLiteDatabase.execSQL(query);
                                Log.d(Tag, "Query executed for storing the package name for the button: " + finalButton_count + " PACKAGE: " + buttonPackageName + " LABEL: " + buttonLabel + "\n");
                                Toast.makeText(MainActivity.this, "" + buttonLabel + " selected!", Toast.LENGTH_SHORT).show();
                                newButton.setText(buttonLabel);
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                }
            });
            /*------------------------------------------------------------------- */
        }

        /*-------------------- END OF APPLICATION LAUNCHING BUTTONS -------------------------*/


    }

    /*_____________________________ END OF ON CREATE _______________________________________*/

    /*Function updates the current date and time of the phone, for the time and date utility*/
    private void showDateTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        SimpleDateFormat simpleDateFormatDate = new SimpleDateFormat("MMMM dd, EEE");
        tv_currentTime.setText(simpleDateFormat.format(calendar.getTime()));
        tv_currentDate.setText(simpleDateFormatDate.format(calendar.getTime()));
    }

    /*Function that retrieves the number of application launching buttons to be displayed from db*/
    public int getApplicationCount() {
        int local_applicationCount = 3 /*The default value*/;
        sqLiteDatabase = openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
        String settingQuery = "create table if not exists " + SETTINGS_DATA_TABLE_NAME
                + "(" + SETTINGS_DATA_TABLE_COLUMN1 + "integer);";
        sqLiteDatabase.execSQL(settingQuery);
        try {
            Cursor cursor = sqLiteDatabase.rawQuery("select * from " + SETTINGS_DATA_TABLE_NAME + " ;", null);
            cursor.moveToFirst();
            local_applicationCount = cursor.getInt(0);

        } catch (Exception exception) {
            Log.d(Tag, "Exception caught while retrieving the applicationCount value from db: " + exception);
            Toast.makeText(this, "Error while retrieving number of applications ", Toast.LENGTH_SHORT).show();
        }
        sqLiteDatabase.close();
        return local_applicationCount;
    }

    private void createTable() {
        sqLiteDatabase = openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
        sqLiteDatabase.execSQL("create table if not exists " + BUTTON_DATA_TABLE_Name +
                "(" + BUTTON_DATA_TABLE_COLUMN_1 + " integer primary key," +
                BUTTON_DATA_TABLE_COLUMN_2 + " varchar, " +
                BUTTON_DATA_TABLE_COLUMN_3 + " varchar );");
        Log.d(Tag, "Table created");
        sqLiteDatabase.close();
    }


    private void selectApplication() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Choose an application");
        final ArrayList<String> myApplicationLabelList = new ArrayList<>();

        final PackageManager packageManager = getPackageManager();
        final List<ApplicationInfo> myInstalledApplicationInfoList = packageManager.getInstalledApplications(packageManager.GET_META_DATA);
        for (ApplicationInfo application : myInstalledApplicationInfoList) {
            String packageName = application.packageName;
            try {
                ApplicationInfo thisApplicationInfo = packageManager.getApplicationInfo(packageName, packageManager.GET_META_DATA);
                String applicationLabel = (String) packageManager.getApplicationLabel(thisApplicationInfo);
                myApplicationLabelList.add(applicationLabel);
                Log.d(Tag, "PACKAGE: " + packageName + " LABEL: " + applicationLabel);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

        }

        builder.setSingleChoiceItems(myApplicationLabelList.toArray(new String[myApplicationLabelList.size()]), 1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String packageName = myInstalledApplicationInfoList.get(which).packageName; /*retrieved package name using label*/
                String applicationLabel = myApplicationLabelList.get(which);
                Log.d(Tag, "user selected application :" + which + " " + myApplicationLabelList.get(which));
                packageManager.getLaunchIntentForPackage(packageName);
                buttonPackageName = packageName;
                buttonLabel = applicationLabel;
            }
        });
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}
