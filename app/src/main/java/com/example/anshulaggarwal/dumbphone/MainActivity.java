package com.example.anshulaggarwal.dumbphone;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    TextView tv_currentDate, tv_currentTime;
    public static final String Tag = "------------------->";
    public static String buttonPackageName = null;
    public static String buttonLabel = "Select Custom Application";

    SharedPreferences sharedPreferences;
    public static String DARK_THEME = "Dark Mode";
    public static String APPLICATION_THEME = DARK_THEME;

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


    @TargetApi(Build.VERSION_CODES.M)
    @SuppressLint({"WrongConstant", "NewApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*-------------------------HIDE THE STATUS BAR -------------------------------------------*/
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        /*----------------------------------------------------------------------------------------*/

        /*---------------------- HIDING THE ACTION BAR -------------------------------------------*/
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        /*-------------------- END OF ACTION BAR HIDING CODE -------------------------------------*/

        setContentView(R.layout.activity_main);

        /*------------------------ Getting theme shared preference --------------------------*/
        ConstraintLayout constraintLayout = (ConstraintLayout) findViewById(R.id.constraintLayout);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        APPLICATION_THEME = sharedPreferences.getString("colorMode", "");
        if (APPLICATION_THEME.equals(DARK_THEME)) {
            constraintLayout.setBackgroundColor(Color.BLACK);

        } else {
            constraintLayout.setBackgroundColor(Color.WHITE);
        }
        /*-------------------------End of shared preference retrieval ------------------------------*/

        /*------------------------  Date & Time Utility --------------------------------------*/
        Button btn_settings = (Button) findViewById(R.id.btn_settings);
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

        if (APPLICATION_THEME.equals(DARK_THEME)) {
            btn_settings.setBackgroundColor(Color.TRANSPARENT);
            tv_currentDate.setBackgroundColor(Color.BLACK);
            tv_currentTime.setBackgroundColor(Color.BLACK);
            tv_currentTime.setTextColor(Color.WHITE);
            tv_currentDate.setTextColor(Color.WHITE);
            btn_settings.setTextColor(Color.WHITE);
        } else {
            btn_settings.setBackgroundColor(Color.TRANSPARENT);
            tv_currentDate.setBackgroundColor(Color.WHITE);
            tv_currentTime.setBackgroundColor(Color.WHITE);
            tv_currentTime.setTextColor(Color.BLACK);
            tv_currentDate.setTextColor(Color.BLACK);
            btn_settings.setTextColor(Color.BLACK);
        }
        settingsSharedPreferences();
        /*-------------------- End of Date & Time Utility -----------------------------------*/


        /*-------------------- NAVIGATION TO SETTINGS INTERFACE ----------------------------*/
        btn_settings = (Button) findViewById(R.id.btn_settings);
        btn_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, com.example.anshulaggarwal.dumbphone.Settings.class));
            }
        });
        /*--------------------  END OF SETTING INTERFACE NAVIGATION------------------------*/


        /*-------------------- For Application Launching Buttons ----------------------------*/
        createTable();
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearLayout);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        applicationCount = Integer.parseInt(sharedPreferences.getString("application_count", ""));
        //Toast.makeText(this, "The value of application Count is: " + applicationCount, Toast.LENGTH_SHORT).show();
        sqLiteDatabase = openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
        for (int button_count = 0; button_count < applicationCount; button_count += 1) {

            //Log.d(Tag, "button_count value is: " + button_count);
            final Button newButton = new Button(this);
            Cursor cursor = sqLiteDatabase.rawQuery("select * from " + BUTTON_DATA_TABLE_Name + " where id=" + Integer.toString(button_count) + ";", null);
            cursor.moveToFirst();
            
            /*---------------- Code excerpt for naming the button based on previous 
                                user applications selection -----------------------*/
            if (cursor.getCount() > 0) {
                String dbRetrievedApplicationLabel = cursor.getString(1);
                newButton.setText(dbRetrievedApplicationLabel);
            } else {
                newButton.setText("Set Application " + button_count);
            }
            /*---------------------------------------------------------------------*/

            /*--------- Button properties -----------*/
            newButton.setBackgroundResource(R.drawable.btn_style);
            if (APPLICATION_THEME.equals(DARK_THEME)) {
                newButton.setTextColor(Color.WHITE);
                newButton.setBackgroundColor(Color.TRANSPARENT);
            } else {
                newButton.setTextColor(Color.BLACK);
                newButton.setBackgroundColor(Color.TRANSPARENT);
            }

            newButton.setTextAppearance(Typeface.BOLD);
            newButton.setId(button_count);
            newButton.setPadding(0, 0, 185, 35);
            newButton.setTextAlignment(Gravity.RIGHT);
            newButton.setTextSize(30);
            newButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            linearLayout.addView(newButton);
            Log.d(Tag, "Button: " + button_count + " added to the layout");
            /*-----------------------------------------*/


            /*--------------- Button's Application Launching action -----------   */
            final int finalButton_count = button_count;
            newButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    sqLiteDatabase = openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
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
                                //    Log.d(Tag, "PACKAGE: " + packageName + " LABEL: " + applicationLabel);
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
            newButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    /*the application corresponding to this newButton has  not been selected yet*/
                    sqLiteDatabase = openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Choose another application");
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
                            String query = "update " + BUTTON_DATA_TABLE_Name + " set " + BUTTON_DATA_TABLE_COLUMN_2
                                    + " = " + " '" + buttonLabel + "' " + ", " + BUTTON_DATA_TABLE_COLUMN_3
                                    + " = " + " '" + buttonPackageName + "' " + " where " + BUTTON_DATA_TABLE_COLUMN_1
                                    + " = " + finalButton_count + ";";
                            sqLiteDatabase.execSQL(query);
                            Log.d(Tag, "Query: " + query);
                            Log.d(Tag, "Query updated for storing the package name for the button: " + finalButton_count + " PACKAGE: " + buttonPackageName + " LABEL: " + buttonLabel + "\n");
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
                    return true;
                }
            });
            /*------------------------------------------------------------------- */
        }
        sqLiteDatabase.close();


        /*-------------------- END OF APPLICATION LAUNCHING BUTTONS -------------------------*/

    }

    /*------------------------ FOR WORKING WITH SETTINGS ------------------------------------*/
    private void settingsSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }
    /*------------------------ END OF SETTINGS ACCESSION -----------------------------------*/
    /*-------------------- FOR NAVIGATION DOTS TO SETTINGS ACTIVITY ---------------------*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, com.example.anshulaggarwal.dumbphone.Settings.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*-------------------- END OF NAVIGATION DOTS TO SETTINGS ACTIVITY ------------------*/


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
                //  Log.d(Tag, "PACKAGE: " + packageName + " LABEL: " + applicationLabel);
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("application_count")) {
            changeApplicationCount(sharedPreferences.getString("application_count", "3"));
        }
    }

    private void changeApplicationCount(String application_count) {
        applicationCount = Integer.parseInt(application_count);
        Toast.makeText(this, "Changed application count registered to: " + application_count, Toast.LENGTH_SHORT).show();
    }
}
