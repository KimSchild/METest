//package ng.com.obkm.exquisitor;
//
//import android.database.Cursor;
//import android.graphics.Bitmap;
//import android.os.Bundle;
//import android.os.Environment;
//import android.provider.MediaStore;
//import android.support.v4.app.Fragment;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.ImageView;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//
//public class TestFragment extends Fragment {
//
//    private VectorLab vectorLab;
//    private Cursor cursor ;
//    private String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA};
//
//
//
//    private Button startButton;
//    private ImageView imageView;
//
//    private List<String> allPaths;
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        Log.i("lifecycle", "onCreate called");
//        super.onCreate(savedInstanceState);
//        vectorLab = VectorLab.get(getActivity());
//        allPaths = new ArrayList<>();
//        allPaths = vectorLab.queryAllPaths();
//        System.out.println("All paths size: " + allPaths.size());
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        Log.i("lifecycle", "onCreateView called");
//        cursor = getActivity().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.Media._ID);
//        View v = inflater.inflate(R.layout.fragment_test, container, false);
//        //createPagerDialog();
//
//        startButton = v.findViewById(R.id.StartButton);
//        startButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                System.out.println("Clicked!!!!!!!!");
//                getRandomImageFromDB();
//            }
//        });
//        return v;
//    }
//
//    private void updateImageView(String path, ImageView myImage) {
//        File imgFile = new File(path);
////        Log.i(TAG, "updateImageView updated path: " + path);
////        Log.i(TAG, "updateImageView imagFile exists? " + imgFile.exists());
//        if (!imgFile.exists()) {
//            myImage.setImageDrawable(null);
//        } else {
//            Bitmap myBitmap = PictureUtils.getThunbnail(imgFile.getPath());
//            myImage.setImageBitmap(myBitmap);
//        }
//    }
//
//    private String getRandomImageFromDB() {
////        Map<Integer, List<Float>> unseenProbs = vectorLab.queryUnseenProbs();
//        String path = "";
//
//            // get random image from the phone storage
//            int numberOfImages = cursor.getCount();
//            System.out.println("Number of images: " + numberOfImages);
//            int image_path_index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
//            final Random random = new Random();
////             int rnd = random.nextInt(numberOfImageVectorsInDB);
//            //cursor.moveToPosition(random.nextInt(numberOfUnseenImages));
//            // get the path of the random image
//            path = getFullPath(cursor.getString(image_path_index));
//
////        else {
//////            List<Integer> allUnseenImages = new ArrayList<>(unseenProbs.keySet());
////            int randomID = vectorLab.queryRandomUnseen();
////            Log.i(TAG, "Random id from query " + randomID);
//////            Log.i(TAG, "how many unseen images do we have? "  + allUnseenImages.size());
////            Log.i(TAG, "how many unseen images do we have? (numberOfUnseenImages) "  + numberOfUnseenImages);
//////            Random random = new Random();
//////            int id = allUnseenImages.get(random.nextInt(allUnseenImages.size()));
//////            path = getFullPath(vectorLab.getPathFromID(id));
////            path = getFullPath(vectorLab.getPathFromID(randomID));
////            numberOfUnseenImages--;
////        }
//        return path;
//    }
//
//    ////// HELPERS ////////
//    private String getFullPath(String shortPath) {
//        String fullPath = "";
//        if (!shortPath.startsWith(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString())) {
//            fullPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + "/" + shortPath;
//            return fullPath;
//        } else return shortPath;
//    }
//
//    protected static String getShortPath(String fullPath) {
//        String shortPath = "";
//        if(fullPath.startsWith(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString())) {
//            shortPath = fullPath.replace(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + "/", "");
//            return shortPath;
//        }
//        return fullPath;
//    }
//}
