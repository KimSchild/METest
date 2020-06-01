package android.kim.automatedtestapp;

import android.content.Context;
import android.database.Cursor;
import android.kim.automatedtestapp.LocalSVM.svm_model;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class TestLogic {

    private static String TAG = "testLogic";
    private String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA};
    private Cursor cursor ;
    private static List<Float[]> trainingDataValues;
    private static List<Integer[]> trainingDataLabels;

    private static Map<Integer, List<Float>> unseenProbs;
    private  static Map<Integer,List<Integer>> unseenLabels;
    private static int numberOfRepetitions = 100;
    //private List<int[]> resultsList = new ArrayList<>();
    private List<int[]> resultsList = new ArrayList<>();

    private VectorLab mVectorLab;

    private static List<Integer> ratings;
    private Context mContext;
    /**
     * (a) images on phone: 100, 500, 1000, 5000, 10000
     * (b) swipes: 1, 3, 5, 10, 20, 50
     * * (b) swipes: 3, 6, 9, 18, 54
     * (c) suggestions retrieved: only 6 best images
     */

    public TestLogic(Context c){
        mContext = c;
    }

    public void runTesting(int[] numberOfImagesArray, int[] numberOfSwipesArray){
        long testStart = System.nanoTime();
        for(int numberOfImages : numberOfImagesArray){
            String dbName = "vectorDB_" + numberOfImages + ".db";
            Log.i(TAG, "vector db we work with " + dbName);
            //Log.i(TAG, "number of Image to analyse in this round" + numberOfImages);
            for(int numberOfSwipes : numberOfSwipesArray) {
                for(int rep = 0; rep < numberOfRepetitions; rep++) {
                    //Log.i(TAG, "we are at images " + numberOfImages + " swipes " + numberOfSwipes + " repetition " + rep);
                    long beforeDataPrep = prepareModelData(numberOfImages, numberOfSwipes, dbName);
                    //long beforeDataPrep = prepareModelData(numberOfImages, numberOfSwipes, "vectorDB_10000.db");
                    long beforeModel = System.nanoTime();
                    svm_model model = SVMModel.buildModel(ratings, trainingDataValues, trainingDataLabels);
                    long afterModel = System.nanoTime();
                    getBestCandidateDistanceBased(model);
                    long afterDistance = System.nanoTime();
                    int[] result = {numberOfImages, numberOfSwipes, rep, (int) (beforeModel - beforeDataPrep), (int) (afterModel - beforeModel), (int) (afterDistance-afterModel), (int) (afterDistance - beforeDataPrep)};
                    resultsList.add(result);
                }
            }
        }
        long testEnd = System.nanoTime();
        writeToFile(resultsList);
        Log.i(TAG, "1000 repetitions, 10000 image vectors,  with 3, 6, 9 and 18. it took " + ((testEnd - testStart)/1000000000) + " sec");
    }


    /*public void runTesting(int[] numberOfImagesArray, int[] numberOfSwipesArray){
        long testStart = System.nanoTime();
        for(int numberOfImages : numberOfImagesArray){
            //Log.i(TAG, "number of Image to analyse in this round" + numberOfImages);
            for(int numberOfSwipes : numberOfSwipesArray) {
                long modelTime = 0;
                long predTime = 0;
                for(int rep = 0; rep < numberOfRepetitions; rep++) {
//                    Log.i(TAG, "we are at images " + numberOfImages + " swipes " + numberOfSwipes + " repetition " + rep);
                    prepareModelData(numberOfImages, numberOfSwipes);
                    long beforeModel = System.nanoTime();
                    svm_model model = SVMModel.buildModel(ratings, trainingDataValues, trainingDataLabels);
                    long afterModel = System.nanoTime();
                    getBestCandidateDistanceBased(model);
                    long afterDistance = System.nanoTime();
                    //int[] result = {numberOfImages, numberOfSwipes, rep, (int) (afterModel - beforeModel), (int) (afterDistance-afterModel) };
                    //resultsList.add(result);
                    modelTime = modelTime + (afterModel - beforeModel);
                    predTime = predTime + (afterDistance - afterModel);
                }
                String[] result = {String.valueOf(numberOfImages), String.valueOf(numberOfSwipes), (String.format("%.6f", (double) ((modelTime/numberOfRepetitions)/1000000000.0))), (String.format("%.6f", (double) ((predTime/numberOfRepetitions)/1000000000.0)))};
                resultsList.add(result);
            }
        }
        long testEnd = System.nanoTime();
        writeToFile(resultsList);
        Log.i(TAG, "300 repetitions, 10000 image vectors,  with 3, 6, 9 and 48. it took " + ((testEnd - testStart)/1000000000) + " sec");
    }*/

    private void writeToFile(List<int[]> data)
    {
        Date today= new Date();
        long time = today.getTime();
        //Passed the milliseconds to constructor of Timestamp class
        Timestamp ts = new Timestamp(time);
        String fileName = ts + "_SVMTestRun";

        try {

            File resultsDir = new File(Environment.getExternalStorageDirectory(), "SVMTestResults");

            if (!resultsDir.exists()) {
                Log.i(TAG, "resultsDir dir doesn't exist");
                Log.i(TAG, "Creating resultsDir dir");
                resultsDir.mkdirs();
            } else {
                Log.i(TAG, "resultsDir dir exists");
            }

            if (Environment.getExternalStorageDirectory().canWrite()) {
                //Log.i(TAG, "backupdir " + backUpDir);
                File file = new File(resultsDir, fileName + ".csv");
                // if file doesnt exists, then create it
                if (!file.exists()) {
                    boolean created = file.createNewFile();
                    Log.i(TAG, "new file was created " + created);
                }

                FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
                BufferedWriter bw = new BufferedWriter(fw);
                StringBuilder sb = new StringBuilder();

                //loops through entire data list
                for (int i = 0; i < data.size(); i++) {
                    // Append strings from array
                    for (int element : data.get(i)) {
                        sb.append(element);
                        sb.append(",");
                    }
                    sb.append("\n");
                }

                bw.write(sb.toString());
                bw.close();
                Log.i(TAG, "csv created sucessfully");
                fw.close(); // close will automatically flush the data
            }

            else Log.i(TAG,"We cannot write into external storage");


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // get label ids and feature values for x images into hashmap
    private long prepareModelData(int imagesToAnalyse, int swipesForTestData, String dbName){

        System.out.println("Is mVectorLab null? " + (mVectorLab == null));
        mVectorLab = VectorLab.get(mContext, dbName);
        Log.i(TAG, "images to analyse and db name " + imagesToAnalyse + " " + dbName);
        if(trainingDataValues == null) {
            trainingDataValues = new ArrayList<Float[]>();
            trainingDataLabels = new ArrayList<Integer[]>();
        }
        else{
            trainingDataValues.clear();
            trainingDataLabels.clear();
            unseenProbs.clear();
            unseenLabels.clear();
        }

        long beforeDataPrep = System.nanoTime();
         unseenLabels = mVectorLab.queryXNumberOfFeatures(imagesToAnalyse);
         System.out.println("Unseen labels size: " + unseenLabels.size());
        // Map <imageID, probsList>
        unseenProbs = mVectorLab.queryXNumberOfProbs(imagesToAnalyse);
// END
        Random random = new Random();
        Integer[]labelArray;

        int iterations = swipesForTestData;

        // creates training data
        //Log.i("random", "NEW ");
        List<Integer> randomCheck = new ArrayList<>();
        while (iterations >  0) {
            int randomIdx = random.nextInt(imagesToAnalyse - 1);
            if(randomCheck.isEmpty() || !randomCheck.contains(randomIdx))
            {
                //Log.i("random", "does randomCheck contain idx? " + randomCheck.contains(randomIdx));
                randomCheck.add(randomIdx);
                //Log.i("random", "randomIDX " + randomIdx);
                //Log.i(TAG, "vector lab null? " + (mVectorLab == null));
                Log.i(TAG, "vector lab null? " + (mVectorLab.getDBName()));
                Log.i(TAG, "random idx" + (randomIdx));
                //Log.i(TAG, "unseen labels null ? " + (unseenLabels = null));
                labelArray = convertToIntegerArray(Objects.requireNonNull(unseenLabels.get(randomIdx)));
                trainingDataLabels.add(labelArray);
                unseenLabels.remove(randomIdx);

                trainingDataValues.add(convertToFloatArray(Objects.requireNonNull(unseenProbs.get(randomIdx))));
                unseenProbs.remove(randomIdx);

                imagesToAnalyse--;
                iterations--;
            }
        }

        createRatingsList(swipesForTestData);
        mVectorLab.setToNull();
        mVectorLab.deleteDB();
        return beforeDataPrep;

    }

    private Integer[] convertToIntegerArray(List<Integer> toConvert){
        Integer[] convertedArray = new Integer[toConvert.size()];
        for (int i = 0; i < toConvert.size(); i++) {
            convertedArray[i] = toConvert.get(i);
        }
        return convertedArray;
    }

    private Float[] convertToFloatArray(List<Float> toConvert){
        Float[] convertedArray = new Float[toConvert.size()];
        for (int i = 0; i < toConvert.size(); i++) {
            convertedArray[i] = toConvert.get(i);
        }
        return convertedArray;
    }

    // fill ratings list 1 to 3 with pos to neg
    //if we only have one feedback, the rating will be positive
    // since map stores vectors randomly for test data, no need to randomize here
    private static void createRatingsList(int amountTestData){
        ratings = new ArrayList<Integer>();
        int numberPos = amountTestData/3;
        int numberNeg = amountTestData - numberPos;
        //Log.i(TAG, "number of test data  " + amountTestData );
        //Log.i(TAG, "number of Negative ratings " + numberNeg );
        //Log.i(TAG, "number of positive ratings " + numberPos);
        int i = 0;
        while (i< numberPos)
        {
            ratings.add(1);
            i++;
        }
        i =0;
        while (i< numberNeg)
        {
            ratings.add(-1);
            i++;
        }
    }

    ////// HELPERS ////////

    private static void getBestCandidateDistanceBased(svm_model model) {
        //Log.i("bestCandidate", "getBestCandidateDistanceBased was called");
        // Setting up a map for the best candidates ids and distances
        Map<Integer, Double> bestCandidates = new HashMap<>(); // 6 best distances

        List<String> bestPaths;
        //Log.i(TAG, " ---- ");
        //Log.i(TAG, " test probs NEW RATINGS ");

        //ad this piont we have removed the rated indices from unseenlabels and unseenprobs
        Iterator<Map.Entry<Integer, List<Integer>>> it = unseenLabels.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, List<Integer>> entry = it.next();
            Integer key = entry.getKey();
            //Log.i(TAG, " test probs key that crashes: " + key);
            List<Float> oneProbsVectorList = unseenProbs.get(key);
            //Log.i(TAG, "one probs vector list " + oneProbsVectorList);
            //Log.i(TAG, "one probs vector list " + oneProbsVectorList.get(2));
            //Log.i(TAG, "one probs vector list size " + oneProbsVectorList.size());
            List<Integer> labelsVectorList = unseenLabels.get(key);
            //Log.i(TAG, "one label vector list" + labelsVectorList.get(1));
            //Log.i(TAG, "one label vector list size " + labelsVectorList.size());


            //convert to arrays
            Integer[] testLabels = new Integer[labelsVectorList.size()];

            Float[] testProbs = new Float[oneProbsVectorList.size()];
            for(int i = 0; i < labelsVectorList.size(); i++ ) {
                //float probability = ;
                testProbs[i] = oneProbsVectorList.get(i);
                //Integer  x= ;
                //Log.i(TAG, "do we add label to testLabel list " + x);
                testLabels[i] = labelsVectorList.get(i);
            }

            // get distance for each unseen image
            //Log.i(TAG, "test probs " + Arrays.toString(testProbs) + " testlabels " + Arrays.toString(testLabels));
            double distance = SVMModel.doPredictionDistanceBased(model, testProbs, testLabels);
//            Log.i(TAG, "distance for bestCand: " + distance);

            if(bestCandidates.size() < 6 ) {
                //Log.i(TAG, "added to first 6 " + pairUnseenImage.getKey());
                bestCandidates.put(key, distance);
//                Log.i("bestCandidate", "home: add this candidate to best 6 " + key + " distance: " + distance);

            }
            else {
                Comparator<Double> c = new Comparator<Double>() {
                    @Override
                    public int compare(Double o1, Double o2) { // -1 if o1 < o2
                        if (o1 < o2) return -1;
                        else if (o1.equals(o2)) return 0;
                        else return 1; // o1 > o2
                    }
                };
                Map.Entry<Integer, Double> min = null;
                // Getting the minimum distance value in bestCandidates
                for(Map.Entry<Integer, Double> bestEntry : bestCandidates.entrySet()) {
                    if(min == null || (c.compare(min.getValue(), bestEntry.getValue()) > 0)) {
                        min = bestEntry;
                    }
                }

                if(distance > min.getValue()) {
                    bestCandidates.remove(min.getKey());
                    bestCandidates.put(key, distance);
                }
            }
        }
        //Log.i(TAG, "prediction has finished.");
        //Log.i(TAG, "best Candidates " + bestCandidates);
    }

}
