package android.kim.automatedtestapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class MainActivity extends AppCompatActivity {

    public static String PACKAGE_NAME;
    private static String TAG = "home";
    // PERMISSIONS
    private static final int REQUEST_CODE = 404;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PACKAGE_NAME = getApplicationContext().getPackageName();
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            //showFragments();
        } else { // if we don't have the permissions, request them
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);}



        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            fragment = new TestFragment();
            fm.beginTransaction().add(R.id.fragment_container, fragment).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        // inspired by https://stackoverflow.com/questions/50067149/start-a-fragment-from-upon-getting-permission
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Log.i("tflite", "onRequestPermission was claled");
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                /**
                 * case 1 - we open the app for the first time, no file in internal storage, no file in external storage
                 * connect to server
                 * case 2 - we reinstalled the app, no file in internal storage, DB file in external storage
                 * importDB
                 */

                // check if we have a backup in the external storage
                // external DB file
                File backUpDir = new File(Environment.getExternalStorageDirectory(), "DBBackup2");
                //Log.i(TAG, "backupdir " + backUpDir);
                File backupDB2 = new File(backUpDir, "vectorDB_100.db");
                //Log.i(TAG, "backup file name " + backupDB2);
                //Log.i(TAG, "does backup exist? " + backupDB2.exists());
                if (backupDB2.exists()) {
                    //Log.i(TAG, "BackupDB exists, copying it from external storage");
                    // Creating internal storage databases folder
                    String internalPath = "";
                    if(android.os.Build.VERSION.SDK_INT >= 17) {
                        internalPath = this.getApplicationInfo().dataDir + "/databases/";
                       // Log.i(TAG, "this is internalPath " + internalPath);
                        File internalDir = new File(internalPath);
                        //Log.i(TAG, "this is internal dir " + internalDir);
                        internalDir.mkdir();
                    } else {
                        internalPath = "/data/data/" + this.getPackageName() + "/databases/";
                        Log.i(TAG, "this is internalPath " + internalPath);

                    }
                    importDB(backupDB2, internalPath, this);
                } else {

                    Log.i(TAG, "BackupDB doesn't exist, calling classify images");
                    //server.connectServer(mContext, getAllImages());
                }

            }
            else {
                // TODO: Permission was not granted
            }
        }
    }

    public static void importDB(final File backupDB, final String internalPath, final Context context) {
        Log.i(TAG, "importDB was called.");
        String [] dbs = new String[]{"vectorDB_100.db"};
        for(int i = 0 ; i < dbs.length; i++){
            int index = i;
            Log.i(TAG, "copy one db " + dbs[index]);
            Thread thread = new Thread() {
            public void run() {

                try {
                    File newImportedDB = new File(internalPath, dbs[index]);
                    boolean fileCreation = newImportedDB.createNewFile();
                    Log.i(TAG, "FileCreation: " + fileCreation);
//                    Log.i(TAG, "Does newImportedDB exist? " + newImportedDB.exists());
                    FileChannel src = new FileInputStream(backupDB).getChannel();
                    Log.i(TAG, "src.size: " + src.size());
                    FileChannel dst = new FileOutputStream(newImportedDB).getChannel();
//                    Log.i(TAG, "dst.size 1: " + dst.size());
                    dst.transferFrom(src, 0, src.size());
//                    Log.i(TAG, "dst.size 2: " + dst.size());
                    src.close();
                    dst.close();
                    Log.i(TAG, "BackupDB writing finished");
                } catch (Exception e) {
                    Log.w("Settings Backup", e);
                }
            }
        };
        thread.start();
    }

    }
}
