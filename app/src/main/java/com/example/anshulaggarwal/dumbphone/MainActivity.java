package com.example.anshulaggarwal.dumbphone;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.KeyEventDispatcher;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.security.auth.callback.PasswordCallback;

import static com.example.anshulaggarwal.dumbphone.passcode.LOCKMODE_DATA_TABLE_COLUMN_1;
import static com.example.anshulaggarwal.dumbphone.passcode.LOCKMODE_DATA_TABLE_COLUMN_2;
import static com.example.anshulaggarwal.dumbphone.passcode.LOCKMODE_DATA_TABLE_NAME;
import static com.example.anshulaggarwal.dumbphone.passcode.PASSCODE_DATA_TABLE_COLUMN_1;
import static com.example.anshulaggarwal.dumbphone.passcode.PASSCODE_DATA_TABLE_COLUMN_2;
import static com.example.anshulaggarwal.dumbphone.passcode.PASSCODE_DATA_TABLE_NAME;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    TextView tv_currentDate, tv_currentTimeMinutes, tv_currentTimeHour, tv_currentTimeSeparator;
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
    public static boolean lockMode = Boolean.FALSE;
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


        /*---------------------EXIT & DIALLER BUTTON -------------------------------------------------------*/
        setContentView(R.layout.activity_main);
        Button btn_exit = (Button) findViewById(R.id.btn_exit);
        Button btn_dialler = (Button) findViewById(R.id.btn_dialler);
        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.finishAffinity();
            }
        });


      /*  if (isMyApplicationDefault() == Boolean.FALSE) {

            vibrate();
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(Intent.createChooser(intent, "You are just one step away from life!" +
                    "\nPlease Select DumbPhone to make it the default launcher"));
        }*/

        /*-------------------- Checking if this is the default application -----------------------*/
        if (isMyApplicationDefault() == Boolean.FALSE) {
            btn_dialler.setEnabled(false);
            btn_dialler.setVisibility(View.INVISIBLE);
            Log.d(Tag, "The Application is not Default");
        }

        btn_dialler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_DIAL));
            }
        });

        /*----------------------------------------------------------------------------------------*/

        SharedPreferences buttonSharedPreference = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences lockModeSharedPreference = PreferenceManager.getDefaultSharedPreferences(this);
        if (buttonSharedPreference.getString("colorMode", "").equals(DARK_THEME)) {
            btn_exit.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            btn_dialler.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        }
        String choice = buttonSharedPreference.getString("show_exit_button", "");
        if (choice.equals(getString(R.string.gone))) {
            btn_exit.setVisibility(View.GONE);
            btn_exit.setEnabled(false);
        } else if (choice.equals(getString(R.string.show))) {
            btn_exit.setVisibility(View.VISIBLE);

        }
        /*-------------------- END OF EXIT BUTTON ------------------------------------------------*/


        /*------------------------ Getting theme shared preference --------------------------*/
        ConstraintLayout constraintLayout = (ConstraintLayout) findViewById(R.id.constraintLayout);

        SharedPreferences sharedPreferencesHourRedColor = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean hour_color_is_red = sharedPreferencesHourRedColor.getBoolean("hour_color", Boolean.TRUE);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        APPLICATION_THEME = sharedPreferences.getString("colorMode", DARK_THEME);

        if (APPLICATION_THEME.equals(DARK_THEME)) {
            constraintLayout.setBackgroundColor(Color.BLACK);

        } else {
            constraintLayout.setBackgroundColor(Color.WHITE);
        }

        /*-------------------------End of shared preference retrieval ------------------------------*/

        /*------------------------  Date & Time Utility --------------------------------------*/
        Button btn_settings = (Button) findViewById(R.id.btn_settings);
        tv_currentTimeMinutes = (TextView) findViewById(R.id.tv_currentTime);
        tv_currentDate = (TextView) findViewById(R.id.tv_currentDate);
        tv_currentTimeHour = (TextView) findViewById(R.id.tv_currentTimeHour);
        tv_currentTimeSeparator = (TextView) findViewById(R.id.tv_currentTimeSeparator);
        tv_currentTimeHour.setTextColor(Color.RED);
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

            if (hour_color_is_red.equals(Boolean.FALSE)) {
                tv_currentTimeHour.setTextColor(Color.WHITE);
                tv_currentTimeHour.setBackgroundColor(Color.TRANSPARENT);
            }
            tv_currentTimeMinutes.setTextColor(Color.WHITE);
            tv_currentTimeSeparator.setTextColor(Color.WHITE);
            tv_currentTimeSeparator.setBackgroundColor(Color.TRANSPARENT);
            tv_currentTimeMinutes.setBackgroundColor(Color.TRANSPARENT);

            tv_currentDate.setTextColor(Color.WHITE);
            btn_settings.setTextColor(Color.WHITE);
        } else {
            btn_settings.setBackgroundColor(Color.TRANSPARENT);
            tv_currentDate.setBackgroundColor(Color.WHITE);

            if (hour_color_is_red.equals(Boolean.FALSE)) {
                tv_currentTimeHour.setTextColor(Color.BLACK);
                tv_currentTimeHour.setBackgroundColor(Color.TRANSPARENT);
            }
            tv_currentTimeMinutes.setTextColor(Color.BLACK);
            tv_currentTimeMinutes.setBackgroundColor(Color.TRANSPARENT);
            tv_currentTimeSeparator.setTextColor(Color.BLACK);
            tv_currentTimeSeparator.setBackgroundColor(Color.TRANSPARENT);
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
        createTableLockMode();
        createTablePasscode();
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearLayout);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        applicationCount = Integer.parseInt(sharedPreferences.getString("application_count", "3"));
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
                newButton.setText("Set Application " + (button_count + 1));
                Typeface typeface = ResourcesCompat.getFont(MainActivity.this, R.font.prata_regular);
                newButton.setTypeface(typeface);
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

                    vibrate();

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

                        /*Adding a security feature to lock/unlock applications*/


                        Cursor cursorLockMode = sqLiteDatabase.rawQuery("select " + LOCKMODE_DATA_TABLE_COLUMN_2
                                + " from " + LOCKMODE_DATA_TABLE_NAME + " where " + LOCKMODE_DATA_TABLE_COLUMN_1 +
                                " = 1;", null);
                        Log.d(Tag, "Printing the retrieved values" + cursorLockMode.getCount());
                        cursorLockMode.moveToFirst();
                        while (!cursorLockMode.isAfterLast()) {
                            Log.d(Tag, "status: " + cursorLockMode.getString(0));
                            cursorLockMode.moveToNext();
                        }

                        cursorLockMode.moveToFirst();
                        if (cursorLockMode.getCount() > 0 && cursorLockMode.getString(0).compareToIgnoreCase("unlocked") == 0) {
                            {
                                Log.d(Tag, "In here...:");
                                /*the application corresponding to this newButton has  not been selected yet*/
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle("Choose an application");
                                final ArrayList<String> myApplicationLabelList = new ArrayList<>();

                                final PackageManager packageManager = getPackageManager();
                                Intent intent = new Intent(Intent.ACTION_MAIN, null);
                                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                                final List<ResolveInfo> myInstalledPackagesList = packageManager.queryIntentActivities(intent, 0);

                                Collections.sort(myInstalledPackagesList, new ResolveInfo.DisplayNameComparator(packageManager));
                              /*  for (ResolveInfo el : myInstalledPackagesList) {
                                    ActivityInfo activityInfo = el.activityInfo;
                                    ComponentName componentName = new ComponentName(activityInfo.applicationInfo.packageName, activityInfo.name);
                                    Log.d(Tag, "Component Name: " + activityInfo.applicationInfo.loadLabel(packageManager).toString());
                                }*/

                      /*  final List<PackageInfo> myInstalledPackagesList = packageManager.getInstalledPackages(0);
                        List<PackageInfo> myInstalledPackagesSystemOnly = packageManager.getInstalledPackages(PackageManager.MATCH_SYSTEM_ONLY);



                       *//*Excluding system applications*//*
                        final List<PackageInfo> myInstalledPackagesListNonSystemApps = new ArrayList<>();
                        for(PackageInfo myInstalledPackagesListElement : myInstalledPackagesList){
                            if( !isSystemPackage(myInstalledPackagesListElement)){
                                myInstalledPackagesListNonSystemApps.add(myInstalledPackagesListElement);
                            }
                        }

                        *//*removing system applications*//*

                        Log.d(Tag, "&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
                        for(PackageInfo e : myInstalledPackagesListNonSystemApps)
                            Log.d(Tag,"non system application name: "+e+isSystemPackage(e)+" "+isSystemPackageSecondApproach(e));

                        Collections.sort(myInstalledPackagesListNonSystemApps, new Comparator<PackageInfo>() {
                            @Override
                            public int compare(PackageInfo o1, PackageInfo o2) {
                                String app1Label = o1.applicationInfo.loadLabel(packageManager).toString();
                                String app2Label = o2.applicationInfo.loadLabel(packageManager).toString();
                                Log.d(Tag,"Comparing: "+app1Label+" and "+app2Label+"result: "+ app1Label.compareToIgnoreCase(app2Label) );
                                return app1Label.compareToIgnoreCase(app2Label);
                            }
                        });*/

                                Log.d(Tag, "List from the experimental code");
                                for (ResolveInfo myInstalledPackagesListElement : myInstalledPackagesList) {
                                    String applicationLabel = myInstalledPackagesListElement.activityInfo.applicationInfo.loadLabel(packageManager).toString();
                                    myApplicationLabelList.add(applicationLabel);
                                }
                                //       final List<ApplicationInfo> myInstalledApplicationInfoList = packageManager.getInstalledApplications(packageManager.GET_META_DATA);

                                /*Excluding system application from the list*/
                      /*  for (int applicationIterator = 0; applicationIterator < size; applicationIterator += 1) {
                            ApplicationInfo application = myInstalledApplicationInfoList.get(applicationIterator);
                            if (isSystemPackage(application) == Boolean.TRUE) {
                                myInstalledApplicationInfoList.remove(application);
                                size -= 1;
                            }
                        }*/

                                /*To sort the list of the applications*/

                                // Collections.sort(myInstalledApplicationInfoList, new PackageItemInfo.DisplayNameComparator(packageManager));


                                builder.setSingleChoiceItems(myApplicationLabelList.toArray(new String[myApplicationLabelList.size()]), 1, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String packageName = myInstalledPackagesList.get(which).activityInfo.packageName; /*retrieved package name using label*/
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
                                        Typeface typeface = ResourcesCompat.getFont(MainActivity.this, R.font.prata_regular);
                                        newButton.setTypeface(typeface);
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
                        } else {
                            Log.d(Tag, "APplications are locked!");
                            Toast.makeText(MainActivity.this, "Please Unlock the applications first!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
            newButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {


                    vibrate();
                    sqLiteDatabase = openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
                    Cursor cursorLockMode = sqLiteDatabase.rawQuery("select " + LOCKMODE_DATA_TABLE_COLUMN_2
                            + " from " + LOCKMODE_DATA_TABLE_NAME + " where " + LOCKMODE_DATA_TABLE_COLUMN_1 +
                            " = 1;", null);
                    Log.d(Tag, "Printing the retrieved values" + cursorLockMode.getCount());
                    cursorLockMode.moveToFirst();
                    while (!cursorLockMode.isAfterLast()) {
                        Log.d(Tag, "status: " + cursorLockMode.getString(0));
                        cursorLockMode.moveToNext();
                    }

                    cursorLockMode.moveToFirst();
                    if (cursorLockMode.getCount() > 0 && cursorLockMode.getString(0).compareToIgnoreCase("unlocked") == 0) {
                        {
                            Log.d(Tag, "In here...:");


                            /*the application corresponding to this newButton has  not been selected yet*/
                            sqLiteDatabase = openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("Choose another application");
                            final ArrayList<String> myApplicationLabelList = new ArrayList<>();


                            final PackageManager packageManager = getPackageManager();
                            Intent intent = new Intent(Intent.ACTION_MAIN, null);
                            intent.addCategory(Intent.CATEGORY_LAUNCHER);
                            final List<ResolveInfo> myInstalledApplicationInfoList = packageManager.queryIntentActivities(intent, 0);

                            Collections.sort(myInstalledApplicationInfoList, new ResolveInfo.DisplayNameComparator(packageManager));
                            for (ResolveInfo el : myInstalledApplicationInfoList) {
                                ActivityInfo activityInfo = el.activityInfo;
                                ComponentName componentName = new ComponentName(activityInfo.applicationInfo.packageName, activityInfo.name);
                                Log.d(Tag, "Component Name: " + activityInfo.applicationInfo.loadLabel(packageManager).toString());
                            }


                            /*        final List<ApplicationInfo> myInstalledApplicationInfoList = packageManager.getInstalledApplications(packageManager.GET_META_DATA);
                             */

                            /*Excluding system application from the list*/
                    /*int size = myInstalledApplicationInfoList.size();
                    for (int applicationIterator = 0; applicationIterator < size; applicationIterator += 1) {
                        ApplicationInfo application = myInstalledApplicationInfoList.get(applicationIterator);
                        if (isSystemPackage(application) == Boolean.TRUE) {
                            myInstalledApplicationInfoList.remove(application);
                            size -= 1;
                        }
                    }
*/
                            /*sorting the myInstalledApplicationInfoList*/
                    /*final PackageItemInfo.DisplayNameComparator comparator = new PackageItemInfo.DisplayNameComparator(packageManager);
                    Collections.sort()
*/
                            /*--------------------------------------------------------------------------*/
                            /*   Collections.sort(myInstalledApplicationInfoList, new ApplicationInfo.DisplayNameComparator(packageManager));
                             */   /*------------------------------------------------------------------------- */


                            /*---------------------OBSOLETE APPROACH----------------------------*/
                            /*This approach led to huge time complexity and was causing a significant amount
                             * of time delay noticiable to 8.0+ seconds hence moving towards a more streamlined
                             * version of sorting algorithm*/
                   /* int tempSize = myInstalledApplicationInfoList.size();
                    for (int i = 0; i < tempSize - 1; i++) {
                        for (int j = 0; j < tempSize - 1 - i; j++) {
                            ApplicationInfo a = myInstalledApplicationInfoList.get(j);
                            ApplicationInfo b = myInstalledApplicationInfoList.get(j + 1);
                            if (returnApplicationLabel(a).compareTo(returnApplicationLabel(b)) > 0) {
                                ApplicationInfo temp = a;
                                myInstalledApplicationInfoList.set(j, b);
                                myInstalledApplicationInfoList.set(j + 1, temp);
                            }
                        }
                    }*/
                            /*--------------------------------------------------------*/

                            /*DISABLED TO CHECK WHETHER REDUCITON IN RUNNIG TIME TO FETCH APPLICATIONS ? */
                   /* Log.d(Tag, "List Sorted:");
                    for (ApplicationInfo element : myInstalledApplicationInfoList) {
                        Log.d(Tag, "\telement");
                    }
*/

                            /*Adding the Application's label from the returned list of installed applications*/
                   /* List<String> ApplicationLabel = new ArrayList<>();
                    for(ApplicationInfo element : myInstalledApplicationInfoList){
                        ApplicationLabel.add(returnApplicationLabel(element));
                    }
*/
                            for (ResolveInfo application : myInstalledApplicationInfoList) {
                                String applicationLabel = application.activityInfo.applicationInfo.loadLabel(packageManager).toString();
                                myApplicationLabelList.add(applicationLabel);
                                Log.d(Tag, "Label: " + applicationLabel);
                            }
/*
                    for (ResolveInfo application : myInstalledApplicationInfoList) {
                        String packageName = application.activityInfo.packageName;
                        try {
                            ApplicationInfo thisApplicationInfo = packageManager.getApplicationInfo(packageName, packageManager.GET_META_DATA);
                            String applicationLabel = (String) packageManager.getApplicationLabel(thisApplicationInfo);
                            String applicationLable = packageName
                            myApplicationLabelList.add(applicationLabel);
                            Log.d(Tag, "PACKAGE: " + packageName + " LABEL: " + applicationLabel);
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }

                    }*/

                            builder.setSingleChoiceItems(myApplicationLabelList.toArray(new String[myApplicationLabelList.size()]), 1, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String packageName = myInstalledApplicationInfoList.get(which).activityInfo.packageName; /*retrieved package name using label*/
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

                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Please Unlock the Applications first", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });
            /*------------------------------------------------------------------- */
        }
        sqLiteDatabase.close();


        /*-------------------- END OF APPLICATION LAUNCHING BUTTONS -------------------------*/

    }

    private String returnApplicationLabel(ApplicationInfo applicationInfo) {
        String packageName = applicationInfo.packageName;

        PackageManager packageManager = this.getPackageManager();
        ApplicationInfo thisApplicationInfo = null;
        try {
            thisApplicationInfo = packageManager.getApplicationInfo(packageName, packageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String applicationLabel = (String) packageManager.getApplicationLabel(thisApplicationInfo);
        return applicationLabel;

    }

    private void vibrate() {
        Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vib.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vib.vibrate(500);
        }
    }


    /*------------------------ CHCEKS IS THIS APPLICATION IS DEFUALT ------------------------*/
    private boolean isMyApplicationDefault() {
        final IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MAIN);
        intentFilter.addCategory(Intent.CATEGORY_HOME);
        List<IntentFilter> intentFilterList = new ArrayList<>();
        intentFilterList.add(intentFilter);
        final String myApplicationPackageName = getPackageName();
        List<ComponentName> componentNameList = new ArrayList<>();
        getPackageManager().getPreferredActivities(intentFilterList, componentNameList, null);
        for (ComponentName componentName : componentNameList) {
            Log.d(Tag, "\tCompononent name:  " + componentName);
            if (myApplicationPackageName.equals(componentName.getPackageName()))
                return true;
        }
        return false;
    }
    /*----------------------------------------------------------------------------------------*/

    /*------------------------ FOR WORKING WITH SETTINGS ------------------------------------*/
    private void settingsSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }
    /*------------------------ END OF SETTINGS ACCESSION -----------------------------------*/

    /*------------------------ FOR EXCLUDING ANY SYSTEM APPLLICATIONS ---------------------*/
    private boolean isSystemPackage(PackageInfo packageInfo) {
        ApplicationInfo applicationInfo = packageInfo.applicationInfo;
        return ((applicationInfo.flags & applicationInfo.FLAG_SYSTEM) != 0);
    }

    private boolean isSystemPackageSecondApproach(PackageInfo packageInfo) {
        ApplicationInfo applicationInfo = packageInfo.applicationInfo;
        if ((applicationInfo.flags & (ApplicationInfo.FLAG_UPDATED_SYSTEM_APP | ApplicationInfo.FLAG_SYSTEM)) > 0) {
            return true;
        }
        return false;
    }
    /*-------------------------------------------------------------------------------------*/

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

    @Override
    public void onBackPressed() {
        /*Do nothing*/
    }

    /*Function updates the current date and time of the phone, for the time and date utility*/
    private void showDateTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormatHour = new SimpleDateFormat("HH");
        SimpleDateFormat simpleDateFormatMinutes = new SimpleDateFormat("mm");
        SimpleDateFormat simpleDateFormatDate = new SimpleDateFormat("MMMM dd, EEE");
        tv_currentTimeMinutes.setText(simpleDateFormatMinutes.format(calendar.getTime()));
        tv_currentTimeHour.setText(simpleDateFormatHour.format(calendar.getTime()));
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
        if (key.equals("lock_mode")) {
            changeLockMode(sharedPreferences.getBoolean("lock_mode", false));
        }
    }

    private void changeLockMode(final Boolean lockModes) {
      //  Toast.makeText(this, "lockModes" + lockModes, Toast.LENGTH_LONG).show();
        Log.d(Tag, "lockModes: " + lockModes);
        if (lockModes == Boolean.FALSE) {
            /*creating the password gateway here*/
            startActivity(new Intent(this, passcode.class));
        } else {
            Toast.makeText(this, "Locked", Toast.LENGTH_SHORT).show();
            sqLiteDatabase = openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
            sqLiteDatabase.execSQL("update " + LOCKMODE_DATA_TABLE_NAME + " set " + LOCKMODE_DATA_TABLE_COLUMN_2
                    + " = 'locked' where " + LOCKMODE_DATA_TABLE_COLUMN_1 + " = 1;");
            sqLiteDatabase.close();
        }
    }

    private void changeApplicationCount(String application_count) {
        applicationCount = Integer.parseInt(application_count);
        //Toast.makeText(this, "Changed application count registered to: " + application_count, Toast.LENGTH_SHORT).show();
    }

    public void createTableLockMode() {
        sqLiteDatabase = openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
        sqLiteDatabase.execSQL("create table if not exists " + LOCKMODE_DATA_TABLE_NAME +
                "(" + LOCKMODE_DATA_TABLE_COLUMN_1 + " integer primary key unique," +
                LOCKMODE_DATA_TABLE_COLUMN_2 + " varchar);");
        Log.d(Tag, "LockMode Table created");
        try {
            sqLiteDatabase.execSQL("insert into " + LOCKMODE_DATA_TABLE_NAME + " values(1, 'unlocked');");
        } catch (Exception exception) {
        }
        sqLiteDatabase.close();
    }

    public final void createTablePasscode() {
        sqLiteDatabase = openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
        sqLiteDatabase.execSQL("create table if not exists " + PASSCODE_DATA_TABLE_NAME +
                "(" + PASSCODE_DATA_TABLE_COLUMN_1 + " integer primary key unique," +
                PASSCODE_DATA_TABLE_COLUMN_2 + " varchar);");
        Log.d(Tag, "Passcode Table created");
        try {
            sqLiteDatabase.execSQL("insert into " + PASSCODE_DATA_TABLE_NAME + " values(1, '1234');");
        } catch (Exception exception) {
        }
        sqLiteDatabase.close();
    }


}
