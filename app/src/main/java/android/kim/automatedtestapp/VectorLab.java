package android.kim.automatedtestapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import android.kim.automatedtestapp.database.IDPathSchema;
import android.kim.automatedtestapp.database.VectorBaseHelper;
import android.kim.automatedtestapp.database.VectorCursorWrapper;
import android.kim.automatedtestapp.database.VectorDBSchema;
import android.kim.automatedtestapp.database.VectorDBSchema.VectorTable;
import android.util.Log;

public class VectorLab {

    private static final String TAG = "VectorLab";
    private static VectorLab sVectorLab;

    private Context mContext;
    private static SQLiteDatabase mDatabase;

    public static VectorLab get(Context context, String dbName) {

        if (sVectorLab == null) {
            sVectorLab = new VectorLab(context, dbName);
        }
        return sVectorLab;
    }

    public void setToNull(){
        sVectorLab = null;
    }

    private VectorLab(Context context, String dbName) {
        mContext = context.getApplicationContext();
        mDatabase = new VectorBaseHelper(mContext, dbName)
                .getWritableDatabase();
    }

    public String getDBName(){
       return  mDatabase.getPath();
    }

    private VectorCursorWrapper queryItemsVectors(){
//        Log.i(TAG, "db for query" + getDBName());
        Cursor cursor = mDatabase.query(
                VectorTable.NAME,
                null, // columns - null selects all columns
                null,
                null,
                null, // groupBy
                null, // having
                null  // orderBy
        );
        return new VectorCursorWrapper(cursor);
    }

    private static VectorCursorWrapper queryItemsIDPath(String whereClause, String[] whereArgs){
        Cursor cursor = mDatabase.query(
                IDPathSchema.IDPathTable.NAME,
                null, // columns - null selects all columns
                whereClause,
                whereArgs,
                null, // groupBy
                null, // having
                null  // orderBy
        );
//        Log.i("home", "VectorLab are we looking in idpath schema db ? " +  IDPathSchema.IDPathTable.NAME);
        return new VectorCursorWrapper(cursor);
    }

    // Step one: list of paths
    // Step two: SELECT feature FROM db WHERE imagePath = path;
    // Loop iterate through list

    public  Map<Integer, List<Float>> queryXNumberOfProbs(int numberOfImages){
        Map<Integer, List<Float>> unseenProbs = new HashMap<>();
        VectorCursorWrapper cursor = queryItemsVectors();
        try {
            if (cursor.getCount() == 0) {
                return null;
            }
            List<Float> list;
//            Log.i(TAG, "number of cursor for probs " + cursor.getCount() + "numb of images to analyse " + numberOfImages);
            while (cursor.moveToNext() && unseenProbs.size()<= numberOfImages) {
//                String path = cursor.getPath();
                //Log.i(TAG, "number of unseen probs vectors " + unseenProbs.size());
                int imageID = cursor.getImageIDVectorsTable();
                if (!unseenProbs.containsKey(imageID) && imageID != numberOfImages) {
//                    Log.i(TAG, "print image id " + imageID);
                    list = new ArrayList<Float>();
                    //Log.i(TAG, "new image id added " + imageID);
                    list.add(cursor.getFloatProbs());
                    unseenProbs.put(cursor.getImageIDVectorsTable(), list);
                } else if (imageID != numberOfImages){
                    list = unseenProbs.get(imageID);
                    list.add(cursor.getFloatProbs());
                    unseenProbs.put(imageID, list);
                    //Log.i(TAG, "new image id added " + imageID + "value added " + list);
                }
                else break;
            }
            return unseenProbs;
        }
        finally {
            cursor.close();
        }
    }

     Map<Integer, List<Integer>> queryXNumberOfFeatures(int imagesToAnalyse){
//        Log.i(TAG, "number of images to analyse " + imagesToAnalyse);
//        Log.i(TAG, "db name in queryXNumberofFEatures: " + getDBName());
        Map<Integer, List<Integer>> unseenLabels = new HashMap<>();
        VectorCursorWrapper cursor = queryItemsVectors();
        try  {
            if (cursor.getCount() == 0) {
                return null;
            }
            List<Integer> list;
//            Log.i(TAG, "how many courser count " + cursor.getCount());
            while (cursor.moveToNext() && unseenLabels.size()<= imagesToAnalyse) {
                int imageID = cursor.getImageIDVectorsTable();
//                Log.i(TAG, "keyset " + unseenLabels.entrySet());
                if (!unseenLabels.containsKey(imageID) && imageID != imagesToAnalyse) {
                    list = new ArrayList<Integer>();
                    list.add(cursor.getFeatureInts());
                    unseenLabels.put(imageID, list);
                } else if (imageID != imagesToAnalyse){
                    list = unseenLabels.get(imageID);
                    list.add(cursor.getFeatureInts());
                    unseenLabels.put(imageID, list);
                }
                else break;
            }
//            Log.i(TAG, "VectorLab: number if unseen Labels array " + unseenLabels.size());
            return unseenLabels;
        }
        finally {
            cursor.close();
        }
    }

    private static ContentValues getContentValuesForLabelProb(int imageID, int topLabels, float topProbs) {
//        Log.i("DB", "getContentValuesForLabelProb was called");
        ContentValues values = new ContentValues();
        values.put("CAST("+IDPathSchema.IDPathTable.Cols.IMAGEID+" as TEXT) = ?", imageID);
        values.put("CAST("+VectorTable.Cols.SEEN+" as TEXT) = ?", 0);
        values.put(VectorTable.Cols.FEATURES, topLabels);
        values.put(VectorTable.Cols.PROBS, topProbs);
        return values;
    }

    public String getPathFromID(int imageID) {
//        Log.i("DB", "getPathFromID was called");
        String idString = String.valueOf(imageID);
        String WHERE = "CAST("+IDPathSchema.IDPathTable.Cols.IMAGEID+" as TEXT) = ?";
        VectorCursorWrapper cursor = queryItemsIDPath(
                WHERE,
                new String[]{idString}
        );
        try {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            return cursor.getImagePathIDPathTable();
        } finally {
            cursor.close();
        }
    }
}
