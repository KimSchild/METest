package android.kim.automatedtestapp;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;


public class TestFragment extends Fragment {

    private VectorLab vectorLab;
    private Cursor cursor ;
    private String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA};

    private static String TAG = "testImages";



    private Button startButton;
    private Button resultsButton;
    private ImageView imageView;
    private TextView results;

    private List<String> allPaths;

    private long sumTime = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vectorLab = VectorLab.get(getActivity(), "vectorDB_100.db");
        allPaths = new ArrayList<>();
        allPaths = vectorLab.queryAllPaths();
        System.out.println("All paths size: " + allPaths.size());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("lifecycle", "onCreateView called");
        cursor = getActivity().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.Media._ID);
        View v = inflater.inflate(R.layout.fragment_test, container, false);
        //createPagerDialog();
        Random random = new Random();
        imageView = v.findViewById(R.id.imageView);

        results = v.findViewById(R.id.resultsText);

        resultsButton = v.findViewById(R.id.resultsButton);
        resultsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Clicked!!!!!!!!");
                System.out.println("Elapsed time for 100 images in a db of 100 images (ns): " + sumTime);
                double seconds = sumTime / 1000000000.0;
                results.setText(String.valueOf(sumTime) + "ns = " + String.valueOf(seconds) + " s");
                sumTime = 0;
            }
        });

        startButton = v.findViewById(R.id.startButton);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testLoop();
            }
        });

        return v;
    }

    private void testLoop() {
        Random random = new Random();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable(){
            int i = 0;
            public void run(){
                if(i<200) { // warm up
                    String path = allPaths.get(random.nextInt(allPaths.size()));
//                    System.out.println("Path: " + path);
                    path = getFullPath(path);
//                    long startTime = System.nanoTime();
//                    long endTime = 0;
                    updateImageView(path);
//                    endTime = System.nanoTime();
//                    long elapsedTime = endTime - startTime;
//                    sumTime = sumTime + elapsedTime;
                    i++;
                } else if (i > 199 && i < 300){
                    String path = allPaths.get(random.nextInt(allPaths.size()));
//                    System.out.println("Path: " + path);
                    path = getFullPath(path);
                    long startTime = System.nanoTime();
                    long endTime = 0;
                    updateImageView(path);
                    endTime = System.nanoTime();
                    long elapsedTime = endTime - startTime;
                    sumTime = sumTime + elapsedTime;
                    i++;
                }
                handler.postDelayed(this, 10);
            }
        }, 5);

    }

    private void updateImageView(String path) {
//        long startTime = System.nanoTime();
//        long endTime = 0;
        File imgFile = new File(path);
        if (!imgFile.exists()) {
            System.out.println("Img file doesn't exist");
            imageView.setImageDrawable(null);
        } else {
            Bitmap myBitmap = PictureUtils.getThunbnail(imgFile.getPath());
            imageView.setImageBitmap(myBitmap);
//            endTime = System.nanoTime();
        }
//        long elapsedTime = endTime - startTime;
//        sumTime = sumTime + elapsedTime;
    }

    ////// HELPERS ////////
    private String getFullPath(String shortPath) {
        String fullPath = "";
        if (!shortPath.startsWith(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString())) {
            fullPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + "/" + shortPath;
            return fullPath;
        } else return shortPath;
    }
}
