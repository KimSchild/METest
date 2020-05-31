package android.kim.automatedtestapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class FirstFragment extends Fragment {

    //int[] numberOfImagesArray = new int[]{100, 200, 500, 700, 1000, 2000, 5000, 7000, 10000};
    //int[] numberOfImagesArray = new int[]{100, 200, 500, 700, 1000, 2000, 5000, 7000, 10000};
    //int[] numberOfImagesArray = new int[]{100, 500, 1000, 5000, 10000};
//    int[] numberOfImagesArray = new int[]{100, 500, 1000, 5000, 10000};
    int[] numberOfImagesArray = new int[]{Constants.numberOfImages};
    //int [] numberOfSwipesArray = new int[]{3, 6, 9, 18, 48};
    int [] numberOfSwipesArray = new int[]{3, 6, 9, 18, 27, 48};
    TestLogic mTestLogic;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        mTestLogic = new TestLogic(this.getContext());
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.button_first).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Testing started", Toast.LENGTH_SHORT).show();
                mTestLogic.runTesting(numberOfImagesArray, numberOfSwipesArray);
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
                Toast.makeText(getActivity(), "Testing finished", Toast.LENGTH_SHORT).show();

            }
        });
    }
}
