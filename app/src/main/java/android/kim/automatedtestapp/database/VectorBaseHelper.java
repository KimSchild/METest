package android.kim.automatedtestapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.kim.automatedtestapp.MainActivity;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class VectorBaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "VectorBaseHelper";

    private static final int VERSION = 1;
    private static  String dbname;

    public VectorBaseHelper(Context context, String databaseName) {
        super(context, databaseName, null, VERSION);
        this.dbname = databaseName;
//        if (checkExist()) {
//            Log.i(TAG, "Database exists");
//        } else {
//            try {
//                copyDataBase();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }

    public static String getDbname(){
        return dbname;
    }

    // scala code to generate the columns
    // scala> for(w <- 0 to 1000) {println( "VectorDBSchema.VectorTable.Cols.PROB" + i + " + " + """", """" + " + "); i= i+1;}

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i("BaseHelper", "start set up DB");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + IDPathSchema.IDPathTable.NAME + "(" +
                " _id integer primary key autoincrement, " +
                IDPathSchema.IDPathTable.Cols.IMAGEID + ", " +
                IDPathSchema.IDPathTable.Cols.PATH +
                ")"
        );
        db.execSQL("CREATE TABLE IF NOT EXISTS " + VectorDBSchema.VectorTable.NAME + "(" +
                " _id integer primary key autoincrement, " +
                    VectorDBSchema.VectorTable.Cols.IMAGEID + ", " +
                    VectorDBSchema.VectorTable.Cols.SEEN + ", " +
                    VectorDBSchema.VectorTable.Cols.FEATURES + ", " +
                    VectorDBSchema.VectorTable.Cols.PROBS + //", " +
                ")"
        );
    }

    @Override
    public void onOpen(SQLiteDatabase database) {
        super.onOpen(database);
        if(Build.VERSION.SDK_INT >= 28) {
            database.disableWriteAheadLogging();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    // Copying SQLite DB to external storage
    public static void exportDB() {
        //Thread thread = new Thread() {
        //public void run() {
        long startTime = System.nanoTime();
        Log.i(TAG, "exportDB was called");

        try {
            File backUpDir = new File(Environment.getExternalStorageDirectory(), "DBBackup2");

            if (!backUpDir.exists()) {
                Log.i(TAG, "Backup dir doesn't exist");
                Log.i(TAG, "Creating backup dir");
                backUpDir.mkdirs();
            } else {
                Log.i(TAG, "Backup dir exists");
            }

            if (Environment.getExternalStorageDirectory().canWrite()) {
                Log.i(TAG, "We can write into external storage");
//                        String backupDBPath = backUpDir + "\\vector_db_backup.db";
                File currentDB = new File("/data/data/" + MainActivity.PACKAGE_NAME +"/databases/" + "vectorDB.db");

                Log.i(TAG, "Creating backupDB file");
                File backupDB = new File(backUpDir, "vectorDB.db");


                if (currentDB.exists()) {
                    Log.i(TAG, "CurrentDB exists");
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    Log.i(TAG, "BackupDB writing finished");
                } else {
                    Log.i(TAG, "Current DB doesn't exist");
                }
            } else {
                Log.i(TAG,"We cannot write into external storage");
            }
        } catch (Exception e) {
            Log.w("Settings Backup", e);
        }
        // }
        //};
        long endTime = System.nanoTime();
        Log.i(TAG, "this is time to write to file " + (endTime - startTime) + " or in sec" + ((endTime - startTime)/(1e-9)));
        //thread.start();
    }

    public static void importDB(final File backupDB, final String internalPath, final Context context) {
        Log.i(TAG, "importDB was called.");
        Thread thread = new Thread() {
            public void run() {
                try {
                    File newImportedDB = new File(internalPath, "vectorDB.db");
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
