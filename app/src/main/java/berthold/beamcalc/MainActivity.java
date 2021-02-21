package berthold.beamcalc;
/**
 * Beam Calc.
 * <p>
 * Calculate the supporting forces of a simply supported beam.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import org.berthold.beamCalc.*;

public class MainActivity extends AppCompatActivity implements FragmentLoadInput.getDataFromFragment {

    // Debug
    private String tag;

    // UI
    private ImageButton checkShowResult, checkShowDimensions, checkShowDetailedSolution;
    private ImageButton loadAdd;
    private ImageButton foldUnfoldDataArea;
    private LinearLayout expandableDataRea;
    private EditText beamLengthIn_m, distanceOfLeftSupportFromLeftEndIn_m, distanceOfRightSupportFromLeftEndIn_m;
    private EditText forceIn_N, forceDistanceFromLeftEndIn_m, lenghtOfLineLoadIn_m;
    private ImageView resultDisplay;
    private int timesBackPressed;

    // Beam
    private RecyclerView beamLoadListRecycleView;
    private RecyclerView.Adapter beamLoadListAdapter;
    private RecyclerView.LayoutManager beamLoadListLayout;
    private List<Load> beamLoadListData = new ArrayList<>();

    private static final boolean INCLUDE_THIS_LOAD_INTO_CALCULATION = true;
    private static final boolean LOAD_HAS_NO_ERROR = false;
    private static final boolean LOAD_HAS_ERROR = true;

    private static Bitmap drawingOfResult;

    // Shared pref's
    SharedPreferences sharedPreferences;

    /**
     * On Create
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        timesBackPressed = 0;
    }

    /**
     * On Resume
     */
    @Override
    public void onResume() {
        super.onResume();

        // Debug
        tag = getClass().getSimpleName();

        // Init recycler view
        beamLoadListRecycleView = (RecyclerView) findViewById(R.id.beam_load_list);
        beamLoadListRecycleView.setHasFixedSize(true);
        beamLoadListLayout = new LinearLayoutManager(this);
        beamLoadListRecycleView.setLayoutManager(beamLoadListLayout);

        beamLoadListAdapter = new BeamLoadListAdapter(beamLoadListData, this);
        beamLoadListRecycleView.setAdapter(beamLoadListAdapter);

        // Init UI
        checkShowResult = (ImageButton) findViewById(R.id.check_show_result);
        checkShowResult.setTag(true);

        checkShowDimensions = (ImageButton) findViewById(R.id.check_show_dimension);
        checkShowDimensions.setTag(true);

        checkShowDetailedSolution = (ImageButton) findViewById(R.id.check_show_term_with_solution);
        checkShowDetailedSolution.setTag(true);

        beamLengthIn_m = (EditText) findViewById(R.id.beam_length);
        distanceOfLeftSupportFromLeftEndIn_m = (EditText) findViewById(R.id.left_support_position);
        distanceOfRightSupportFromLeftEndIn_m = (EditText) findViewById(R.id.right_support_position);

        loadAdd = (ImageButton) findViewById(R.id.add_load);
        foldUnfoldDataArea = (ImageButton) findViewById(R.id.fold_unfold_data_area);
        expandableDataRea = (LinearLayout) findViewById(R.id.expandable_data_area);

        resultDisplay = (ImageView) findViewById(R.id.result_view);

        // Restore
        restoreFromSharedPreferences();

        // Listeners

        // Display options
        // Show result?
        checkShowResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((boolean) checkShowResult.getTag()) {
                    checkShowResult.setTag(false);
                    checkShowResult.setImageResource(R.drawable.show_result_off);
                } else {
                    checkShowResult.setTag(true);
                    checkShowResult.setImageResource(R.drawable.show_result_on);
                }
                solveBeamAndShowResult(beamLoadListData);
            }
        });

        checkShowDimensions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((boolean) checkShowDimensions.getTag()) {
                    checkShowDimensions.setTag(false);
                    checkShowDimensions.setImageResource(R.drawable.show_dim_off);
                } else {
                    checkShowDimensions.setTag(true);
                    checkShowDimensions.setImageResource(R.drawable.show_dim_on);
                }
                solveBeamAndShowResult(beamLoadListData);
            }
        });

        checkShowDetailedSolution.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((boolean) checkShowDetailedSolution.getTag()) {
                    checkShowDetailedSolution.setTag(false);
                    checkShowDetailedSolution.setImageResource(R.drawable.show_detailed_solution_off);
                } else {
                    checkShowDetailedSolution.setTag(true);
                    checkShowDetailedSolution.setImageResource(R.drawable.show_detailed_solution_select);
                }
                solveBeamAndShowResult(beamLoadListData);
            }
        });

        loadAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                FragmentLoadInput fragmentDeleteRegex =
                        FragmentLoadInput.newInstance("-");
                fragmentDeleteRegex.show(fm, "fragment_dialog");
            }
        });

        // Was one of the inputs that define the beam changed?
        // If so, recalculate.....
        beamLengthIn_m.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                solveBeamAndShowResult(beamLoadListData);
            }
        });

        distanceOfLeftSupportFromLeftEndIn_m.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                solveBeamAndShowResult(beamLoadListData);
            }
        });

        distanceOfRightSupportFromLeftEndIn_m.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                solveBeamAndShowResult(beamLoadListData);
            }
        });

        // Fold/ expand data area
        foldUnfoldDataArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleView(expandableDataRea, foldUnfoldDataArea);
            }
        });
    }

    /*
     * Update buttons according to their state
     */
    private void upDateButtons() {
        if ((boolean) checkShowResult.getTag())
            checkShowResult.setImageResource(R.drawable.show_result_on);
        else
            checkShowResult.setImageResource(R.drawable.show_result_off);

        if ((boolean) checkShowDetailedSolution.getTag())
            checkShowDetailedSolution.setImageResource(R.drawable.show_detailed_solution_select);
        else
            checkShowDetailedSolution.setImageResource(R.drawable.show_detailed_solution_off);

        if ((boolean) checkShowDimensions.getTag())
            checkShowDimensions.setImageResource(R.drawable.show_dim_on);
        else
            checkShowDimensions.setImageResource(R.drawable.show_dim_off);
    }

    /**
     * Creates a name for the load (F or q) and adds the index
     * <p>
     * Result is e.g.: F1 or q3....
     */
    private static String buildNameOfLoad(double lengthOfLineLoad_m, int index) {

        String nameOfLoad;
        if (lengthOfLineLoad_m == 0)
            nameOfLoad = "F" + (index + 1);
        else
            nameOfLoad = "q" + (index + 1);
        return nameOfLoad;
    }

    /**
     * Rename load's
     * <p>
     * When a load was deleted, this method should be called in order
<<<<<<< HEAD
     * to reassign the load name indices in the right order.
=======
     * to reassign the load name indices.
>>>>>>> 23600157fc9e14b8764bfdedd7ec045702c933f0
     * <p>
     * See: {@link #beamLoadListData}
     */
    private void renameLoads() {
        Load load;
        String nameOfLoad;
        boolean includeThisLoadIntoCalculation;
        double lengthOfLineLoad_m;
        int indexOfSingleLoad = 1, indexOfLineLoad = 1;

        for (int i = 0; i <= beamLoadListData.size() - 1; i++) {
            load = beamLoadListData.get(i);

            lengthOfLineLoad_m = load.getLengthOfLineLoad_m();

            if (load.getIncludeThisLoadIntoCalculation())
                includeThisLoadIntoCalculation = true;
            else
                includeThisLoadIntoCalculation = false;

            if (lengthOfLineLoad_m == 0) {
                nameOfLoad = "F" + indexOfSingleLoad;
                indexOfSingleLoad++;
            } else {
                nameOfLoad = "q" + indexOfLineLoad;
                indexOfLineLoad++;
            }

            Load loadWithNameChanged = new Load(
                    nameOfLoad
                    , load.getForce_N()
                    , load.getDistanceFromLeftEndOfBeam_m()
                    , load.getAngleOfLoad_degrees()
                    , load.getLengthOfLineLoad_m()
                    , includeThisLoadIntoCalculation, LOAD_HAS_NO_ERROR);

            beamLoadListData.set(i, loadWithNameChanged);
        }
    }

    /**
     * Add's a new load to the beam and saves it in the load list.
     *
     * @param buttonPressed
     * @param loadMagnitute
     * @param loadPosition
     * @param loadLength
     */
    @Override
<<<<<<< HEAD
    public void getLoadInputDialogData(String buttonPressed, String loadMagnitute, String loadLength, String loadPosition) {
=======
    public void getDialogInput(String buttonPressed, String loadMagnitute, String loadLength, String loadPosition) {
>>>>>>> 23600157fc9e14b8764bfdedd7ec045702c933f0

        if (buttonPressed.equals(FragmentLoadInput.BUTTON_OK_WAS_PRESSED)) {
            try {
                Double force_N = Double.valueOf(loadMagnitute);
<<<<<<< HEAD
                Double positionFromLeftEnd_m = Double.valueOf(loadPosition);
                Double angleOfLoad_DEG = 0.0; // This version only supports up or downwards acting single loads...
                Double lengthOfLoad_m = Double.valueOf(loadLength);
                String nameOfLoad = buildNameOfLoad(lengthOfLoad_m, beamLoadListData.size());

                Load load = new Load(nameOfLoad, force_N, positionFromLeftEnd_m, angleOfLoad_DEG, lengthOfLoad_m, INCLUDE_THIS_LOAD_INTO_CALCULATION, LOAD_HAS_NO_ERROR);
                Log.v(tag, "Name:"+nameOfLoad+"  Force:"+force_N+" X0:"+positionFromLeftEnd_m+" Angle:"+angleOfLoad_DEG+"  length:"+lengthOfLoad_m);
=======
                Double forceEnd_N=force_N;
                Double x0_m = Double.valueOf(loadPosition);
                Double angleOfLoad_DEG = 0.0;
                Double fl_m = Double.valueOf(loadLength);
                String nameOfLoad = buildNameOfLoad(fl_m, beamLoadListData.size());


                // Load(String nameOfLoad, double forceStart_N, double distanceFromLeftEndOfBeam_m, double angleOfLoad_degrees,
                // double lengthOfLineLoad_m, boolean includeThisLoadIntoCaclulation, boolean thisLoadHasAnError)
                Load load = new Load(nameOfLoad, force_N, forceEnd_N, x0_m, angleOfLoad_DEG, fl_m, INCLUDE_THIS_LOAD_INTO_CALCULATION, LOAD_HAS_NO_ERROR);


>>>>>>> 23600157fc9e14b8764bfdedd7ec045702c933f0

                beamLoadListData.add(load);
                beamLoadListAdapter.notifyDataSetChanged();
                renameLoads();
<<<<<<< HEAD

                solveBeamAndShowResult(beamLoadListData);
=======
                solveBeamAndShowResult(beamLoadListData);

>>>>>>> 23600157fc9e14b8764bfdedd7ec045702c933f0
            } catch (NumberFormatException e) {
                Log.v(tag, "Could not convert");
            }
        }
    }

    /**
     * Callback when item in 'recyclerView' was pressed
     *
     * @param position   Position of item in recycler view's list and in array list.
     * @param resourceId Resource id, as in R.resources.....
     * @rem: Show how the name of an resource can be resolved via it's resource id@@
     * @see BeamLoadListAdapter
     */
    public void itemInsideLoadListWasPressed(int position, int resourceId) {
        String nameOfButtonPressedInres;
        nameOfButtonPressedInres = getResources().getResourceEntryName(resourceId);
        Log.v("Main", "Row:" + position + " touched" + " Button:" + nameOfButtonPressedInres);

        if (nameOfButtonPressedInres.toString().equals("delete_item")) {
            beamLoadListData.remove(position);
            renameLoads();
            beamLoadListAdapter.notifyDataSetChanged();
            solveBeamAndShowResult(beamLoadListData);
        }

        if (nameOfButtonPressedInres.toString().equals("include_this_load_in_calculation"))
            solveBeamAndShowResult(beamLoadListData);
    }

    /**
     * Solve.
     * <p>
     * Driver for {@link BeamSolver} Method. Gets all loads from {@link #beamLoadListData}
     * and adds them to a new {@link Beam}- Object. Finally it solves....
     *
     * @param beamLoadListData List containing all single or line loads acting on the beam.
     */
    private void solveBeamAndShowResult(List<Load> beamLoadListData) {

        if (allDataNeededForSolvingBeamGiven()) {

            renameLoads();

            Beam beam = createBeam();
            BeamResult result = BeamSolver.getResults(beam, "2f");
            Load load;
            boolean showResult = false;
            boolean showDimensions = false;
            boolean showMatTerm = false;
            boolean includeThisLoadIntoCalculation;

            // Clear all errors from load list
            for (int i = 0; i <= beamLoadListData.size() - 1; i++) {
                load = beamLoadListData.get(i);
                if (load.getIncludeThisLoadIntoCalculation())
                    includeThisLoadIntoCalculation = true;
                else
                    includeThisLoadIntoCalculation = false;

                Load loadWithNoError = new Load(load.getName()
                        , load.getForce_N()
                        , load.getDistanceFromLeftEndOfBeam_m()
                        , 0
                        , load.getLengthOfLineLoad_m()
                        , includeThisLoadIntoCalculation, LOAD_HAS_NO_ERROR);

                beamLoadListData.set(i, loadWithNoError);
            }

            // If result has an error, mark all loads with errors in load list.....
            // Views are updated in loadList's adapter class
            int errorCount = result.getErrorCount();
            if (errorCount > 0) {
                for (int i = 0; i <= errorCount - 1; i++) {

                    if (result.getError(i).getOriginOfError() == BeamCalcError.LOAD_ERROR) {
                        int indexOfError = result.getError(i).getIndexOfError();
                        load = beamLoadListData.get(indexOfError);

                        if (load.getIncludeThisLoadIntoCalculation())
                            includeThisLoadIntoCalculation = true;
                        else
                            includeThisLoadIntoCalculation = false;

                        // Mark old load with error!
                        Load loadWithError = new Load(load.getName()
                                , load.getForce_N()
                                , load.getDistanceFromLeftEndOfBeam_m()
                                , 0
                                , load.getLengthOfLineLoad_m()
                                , includeThisLoadIntoCalculation, LOAD_HAS_ERROR);

                        beamLoadListData.set(indexOfError, loadWithError);
                    }

                }
            }

            // Draw beam, loads and, if error: Describe errors
            if ((boolean) checkShowDimensions.getTag())
                showDimensions = true;

            if ((boolean) checkShowResult.getTag())
                showResult = true;

            if ((boolean) checkShowDetailedSolution.getTag())
                showMatTerm = true;

            drawingOfResult = ShowResult.draw(result, beam, showResult, showDimensions, showMatTerm, getResources());
            resultDisplay.setImageBitmap(drawingOfResult);

            beamLengthIn_m.setBackgroundColor(Color.TRANSPARENT);
            distanceOfLeftSupportFromLeftEndIn_m.setBackgroundColor(Color.TRANSPARENT);
            distanceOfRightSupportFromLeftEndIn_m.setBackgroundColor(Color.TRANSPARENT);

            /**
             * todo: Mark lines with errors in load list: Marking works, unmarking not!
             * So: for the time being, loads wich cuased errors are not marked (red background)
             * see also: {@link BeamLoadListAdapter}
             */

            // @rem:To avoid illegal state exception when updating recycler view:@@
            // https://github.com/kanytu/android-material-drawer-template/issues/36
            //
            //new Handler().post(new Runnable() { @Override public void run() { beamLoadListAdapter.notifyDataSetChanged(); } });
        }
    }

    /**
     * Create a beam from the data given.
     *
     * @return beam     Beam with length and two supports and all loads added, ready to solve.
     */
    private Beam createBeam() {

        Beam beam;
        double leftSupport_m = 0;
        double rightSupport_m = 0;
        double lengthIn_m = 0;
        try {
            lengthIn_m = Double.valueOf(beamLengthIn_m.getText().toString());
            leftSupport_m = Double.valueOf(distanceOfLeftSupportFromLeftEndIn_m.getText().toString());
            rightSupport_m = Double.valueOf(distanceOfRightSupportFromLeftEndIn_m.getText().toString());
        } catch (NumberFormatException e) {
            Log.v("-", "Could not convert....");
        }
        beam = new Beam(lengthIn_m);
        Support leftSupport = new Support("A", leftSupport_m);
        Support rightBearing = new Support("B", rightSupport_m);
        beam.addBearing(leftSupport);
        beam.addBearing(rightBearing);

        // Get and add load
        for (int i = 0; i <= beamLoadListData.size() - 1; i++) {
            if (beamLoadListData.get(i).getIncludeThisLoadIntoCalculation()) {
                //String nameOfLoad=buildNameOfLoad(beamLoadListData.get(i),i);
                //beamLoadListData.get(i).changeNameTo(nameOfLoad);
                String nameOfLoad = beamLoadListData.get(i).getName();
<<<<<<< HEAD
                double force_N = beamLoadListData.get(i).getForce_N();
                double distanceFromLeft_m = beamLoadListData.get(i).getDistanceFromLeftEndOfBeam_m();
                double lengthOfLineLoad_m = beamLoadListData.get(i).getLengthOfLineLoad_m();
                Load load = new Load(nameOfLoad, force_N, distanceFromLeft_m, 0, lengthOfLineLoad_m, INCLUDE_THIS_LOAD_INTO_CALCULATION, LOAD_HAS_NO_ERROR);
=======
                double force_F = beamLoadListData.get(i).getForce_N();
                double distanceFromLeft_m = beamLoadListData.get(i).getDistanceFromLeftEndOfBeam_m();
                double lengthOfLineLoad_m = beamLoadListData.get(i).getLengthOfLineLoad_m();
                Load load = new Load(nameOfLoad, force_F, distanceFromLeft_m, 0, lengthOfLineLoad_m, INCLUDE_THIS_LOAD_INTO_CALCULATION, LOAD_HAS_NO_ERROR);
>>>>>>> 23600157fc9e14b8764bfdedd7ec045702c933f0
                beam.addLoad(load);
                Log.v(tag+"CREATEBEAM", "Name:"+nameOfLoad+"  Force:"+force_N+" X0:"+distanceFromLeft_m+"  length:"+lengthOfLineLoad_m);
            }
        }
        return beam;
    }

    /**
     * Get image bitmap of beam.
     * <p>
     * Public method which is used to exchange bitmap image
     * data with other activity's.
     *
     * @return drawingOfResult  Bitmap containing result of a calculation
     */
    public static Bitmap getDrawingOfResult() {
        return drawingOfResult;
    }

    /**
     * Check if beam can be solved or if some data is missing.
     * Marks 'EditText' field which require input in order to
     * solve the beam.
     *
     * @return noDataMissing    True if beam can be solved.
     */
    private Boolean allDataNeededForSolvingBeamGiven() {

        Boolean noDataMissing = true;
        String lengthIn_m, left_m, right_m;
        lengthIn_m = beamLengthIn_m.getText().toString();
        left_m = distanceOfLeftSupportFromLeftEndIn_m.getText().toString();
        right_m = distanceOfRightSupportFromLeftEndIn_m.getText().toString();

        if (lengthIn_m.isEmpty()) {
            beamLengthIn_m.setBackgroundColor(Color.RED);
            noDataMissing = false;
        }

        if (left_m.isEmpty()) {
            distanceOfLeftSupportFromLeftEndIn_m.setBackgroundColor(Color.RED);
            noDataMissing = false;
        }
        if (right_m.isEmpty()) {
            distanceOfRightSupportFromLeftEndIn_m.setBackgroundColor(Color.RED);
            noDataMissing = false;
        }
        return noDataMissing;
    }

    /**
     * Save current state to sharedPreferences
     */
    private void saveCurrentState() {
        sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("beamLengthIn_m", beamLengthIn_m.getText().toString());
        editor.putString("distanceOfLeftSupportFromLeftEndIn_m", distanceOfLeftSupportFromLeftEndIn_m.getText().toString());
        editor.putString("distanceOfRightSupportFromLeftEndIn_m", distanceOfRightSupportFromLeftEndIn_m.getText().toString());
        editor.putBoolean("displayResult", (boolean) checkShowResult.getTag());
        editor.putBoolean("displayDimensions", (boolean) checkShowDimensions.getTag());
        editor.putBoolean("displayMathTerm", (boolean) checkShowDetailedSolution.getTag());

        String st;
        st = ConvertLoadListObject.toStringObject(beamLoadListData);
        editor.putString("loadListData", st.toString());
        editor.commit();

        timesBackPressed = 0;
    }

    /**
     * Get and set shared pref's
     */
    private void restoreFromSharedPreferences() {
        sharedPreferences = getPreferences(MODE_PRIVATE);
        String defaultValue = "";
        beamLengthIn_m.setText(sharedPreferences.getString("beamLengthIn_m", defaultValue));
        distanceOfLeftSupportFromLeftEndIn_m.setText(sharedPreferences.getString("distanceOfLeftSupportFromLeftEndIn_m", defaultValue));
        distanceOfRightSupportFromLeftEndIn_m.setText(sharedPreferences.getString("distanceOfRightSupportFromLeftEndIn_m", defaultValue));
        /*
        forceIn_N.setText(sharedPreferences.getString("forceIn_N", defaultValue));
        forceDistanceFromLeftEndIn_m.setText(sharedPreferences.getString("forceDistanceFromLeftEndIn_m", defaultValue));
        lenghtOfLineLoadIn_m.setText(sharedPreferences.getString("lengthOfLineLoadIn_m", defaultValue));
        */
        checkShowResult.setTag(sharedPreferences.getBoolean("displayResult", false));
        checkShowDimensions.setTag(sharedPreferences.getBoolean("displayDimensions", false));
        checkShowDetailedSolution.setTag(sharedPreferences.getBoolean("displayMathTerm", false));
        upDateButtons();

        String loadListDataString = sharedPreferences.getString("loadListData", null);
        if (loadListDataString != null) {
            if (!loadListDataString.isEmpty()) {
                beamLoadListData = ConvertLoadListObject.fromStringObject(loadListDataString);
                beamLoadListAdapter = new BeamLoadListAdapter(beamLoadListData, this);
                beamLoadListRecycleView.setAdapter(beamLoadListAdapter);
                solveBeamAndShowResult(beamLoadListData);
            }
        }
    }

    /*
     * Show/ hide data area below drawing of beam, change button accordingly.....
     */
    private void toggleView(LinearLayout v, ImageButton fab) {
        if (v.isShown()) {
            fab.setImageResource(R.drawable.baseline_keyboard_arrow_up_black_18dp);
            v.setVisibility(View.GONE);
        } else {
            fab.setImageResource(R.drawable.baseline_keyboard_arrow_down_black_18dp);
            v.setVisibility(View.VISIBLE);
        }
    }

    /**
     * If Back button was pressed
     *
     * @rem: Override "Back Button Pressed". Shows how to check if this button was pressed@@
     * @rem: Here it is checked to prevent the user from accidendily leave the app@@
     */
    @Override
    public void onBackPressed() {
        timesBackPressed++;
        if (timesBackPressed > 1) {
            saveCurrentState();
            finish();
        } else
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.leave_warning), Toast.LENGTH_LONG).show();
    }

    /**
     * Save instance state
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveCurrentState();
    }

    /**
     * Options menu
     */
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
        if (id == R.id.shwo_app_info) {
            Intent showAppInfo = new Intent(this, InfoActivity.class);
            this.startActivity(showAppInfo);
            return true;
        }

        if (id == R.id.save_drawing_as_bitmap) {
            Intent saveBeamDrawingActivity = new Intent(this, SaveBeamDrawing.class);
            this.startActivity(saveBeamDrawingActivity);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
