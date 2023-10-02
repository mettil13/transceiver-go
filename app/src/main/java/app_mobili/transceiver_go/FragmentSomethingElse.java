package app_mobili.transceiver_go;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentSomethingElse#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentSomethingElse extends Fragment implements NoiseStrength.RecordingListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final int REQUEST_CODE_EXPORT_DB = 69;
    private static final int REQUEST_CODE_IMPORT_DB = 420;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ActivityResultLauncher<Intent> importLauncher;

    public FragmentSomethingElse() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentSomethingElse.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentSomethingElse newInstance(String param1, String param2) {
        FragmentSomethingElse fragment = new FragmentSomethingElse();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // needed for the importing process
        importLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    Intent data = result.getData();
                    DatabaseImportExportUtil.handleImportFileResult(getActivity(),resultCode,data);
                }
        );
    }

    @Override
    public void onRecordingFinished(int noise) {
        TextView noiseView = getActivity().findViewById(R.id.noiseInfo);
        noiseView.setText("noise: " + noise);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_something_else, container, false);

        Button noiseButton = rootView.findViewById(R.id.noiseButton);
        Button silenceButton = rootView.findViewById(R.id.silenceButton);
        Button clapButton = rootView.findViewById(R.id.clapButton);
        Button treesholdButton = rootView.findViewById(R.id.treesholdButton);
        Button shareDbButton = rootView.findViewById(R.id.exportDbButton);
        Button importDbButton = rootView.findViewById(R.id.importDbButton);

        NoiseStrength noiseStrength = new NoiseStrength(getContext());
        noiseStrength.setRecordingListener(this);

        treesholdButton.setOnClickListener(v -> {
            TextView noiseView = rootView.findViewById(R.id.noiseInfo);
            noiseView.setText("Silence: " + noiseStrength.getSilenceAmplitude() + "\nClap: " + noiseStrength.getClapAmplitude());
        });


        noiseButton.setOnClickListener(v -> {
            noiseStrength.startRecording();
        });

        silenceButton.setOnClickListener(v -> {
            noiseStrength.calibrateSilence();
        });

        clapButton.setOnClickListener(v -> {
            noiseStrength.calibrateClap();

        });

        shareDbButton.setOnClickListener(v -> {
            DatabaseImportExportUtil.shareDatabase(getContext(),getActivity(),"db_di_luizo");
            //exportLauncher.launch(DatabaseImportExportUtil.exportDatabaseIntent());
            //importLauncher.launch(DatabaseImportExportUtil.importFileToDatabaseDirectory());
        });

        importDbButton.setOnClickListener(v -> {
            importLauncher.launch(DatabaseImportExportUtil.importFileToDatabaseDirectory());
        });

        // Inflate the layout for this fragment
        return rootView;

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        /*
        Drawable prova = ((ImageView) getView().findViewById(R.id.imageView)).getDrawable();
        AnimatedVectorDrawable provaAnimata = (AnimatedVectorDrawable) prova;
        provaAnimata.start();
         */
    }
}