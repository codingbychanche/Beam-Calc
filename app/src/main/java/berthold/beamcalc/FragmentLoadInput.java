package berthold.beamcalc;

import android.app.Activity;
<<<<<<< HEAD
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
=======
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
>>>>>>> 23600157fc9e14b8764bfdedd7ec045702c933f0

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

<<<<<<< HEAD
=======
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static android.content.Context.BLUETOOTH_SERVICE;

>>>>>>> 23600157fc9e14b8764bfdedd7ec045702c933f0
/**
 * Add load to beam
 */
public class FragmentLoadInput extends DialogFragment {

    getDataFromFragment gf;
    public static final String BUTTON_OK_WAS_PRESSED = "OK";
    public static final String BUTTON_CANCEL_WAS_PRESSED = "CANCEL";

    public FragmentLoadInput() {
        // Constructor must be empty....
    }

    public static FragmentLoadInput newInstance(String titel) {
        FragmentLoadInput frag = new FragmentLoadInput();
        Bundle args = new Bundle();
        args.putString("titel", titel);
        frag.setArguments(args);
        return frag;
    }

    /**
     * THE INTERFACE
     * <p>
     * This is the interface used to pass data from the
     * this fragment to it's activity
     */
    public interface getDataFromFragment {
<<<<<<< HEAD
        void getLoadInputDialogData(String buttonPressed, String loadMagnitute, String loadPosition, String loadLength);
=======
        void getDialogInput(String buttonPressed, String loadMagnitute, String loadPosition, String loadLength);
>>>>>>> 23600157fc9e14b8764bfdedd7ec045702c933f0
    }

    /**
     * get interface Object...
     * You may use any method defined in the interface through the
     * object 'gf'
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        gf = (getDataFromFragment) activity;
    }

    /**
     * Inflate fragment layout.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_load_input, container);

    }

    /**
     * This fills the layout with data.
     *
     * @param view
     * @param savedInstanceState
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button okButtonView = view.findViewById(R.id.add_load_button_ok);
        Button cancelButtonView = view.findViewById(R.id.add_load_button_cancel);

        final EditText forceMagnituteView = view.findViewById(R.id.load_magnitute);
        final EditText forcePositionView = view.findViewById(R.id.load_position);
        final EditText forceLengthView = view.findViewById(R.id.load_length);

        okButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String forceMagintute=forceMagnituteView.getText().toString();
                String forcePosition=forcePositionView.getText().toString();
                String forceLength=forceLengthView.getText().toString();

<<<<<<< HEAD
                gf.getLoadInputDialogData(BUTTON_OK_WAS_PRESSED, forceMagintute,forcePosition,forceLength);
=======
                gf.getDialogInput(BUTTON_OK_WAS_PRESSED, forceMagintute,forceLength,forcePosition);
>>>>>>> 23600157fc9e14b8764bfdedd7ec045702c933f0
                dismiss();
            }
        });

        cancelButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
<<<<<<< HEAD
                gf.getLoadInputDialogData(BUTTON_CANCEL_WAS_PRESSED, "", "", "");
=======
                gf.getDialogInput(BUTTON_CANCEL_WAS_PRESSED, "", "", "");
>>>>>>> 23600157fc9e14b8764bfdedd7ec045702c933f0
                dismiss();
            }
        });
    }
}
