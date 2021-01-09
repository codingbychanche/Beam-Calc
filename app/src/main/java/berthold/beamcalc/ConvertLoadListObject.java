package berthold.beamcalc;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import org.berthold.beamCalc.*;

/**
 * Converts {@link Load}- list into a String object and/ or back to
 * a Load- list Object.<p>
 * <p>
 * The method used here works only for the {@link Load}- list objects
 * used in this package. For abitrary objects Java- Serializable's are the way to go.
 * <p>
 * Created by Berthold on 2/9/19.
 */
public class ConvertLoadListObject {

    private static final int NAME_OF_LOAD = 0;
    private static final int FORCE_N = 1;
    private static final int DISTANCE_FROM_LEFT_END_m = 2;
    private static final int LENGTH_OF_LINE_LOAD_m = 3;
    private static final int INCLUDE_THIS_LOAD_INTO_CALCULATION = 4;
    private static final int HAS_LOAD_AN_ERROR = 5;

    /**
     * Convert to String- Object.
     *
     * @param beamLoadList
     * @return beamLoadListStringObject
     * @see Load
     */
    public static String toStringObject(List<Load> beamLoadList) {
        if (beamLoadList == null)
            return null;

        StringBuilder beamLoadListStringObject = new StringBuilder();

        for (int i = 0; i <= beamLoadList.size() - 1; i++) {
            Load load = beamLoadList.get(i);
            beamLoadListStringObject.append(load.getName() + ";" +
                    load.getForce_N() + ";" +
                    load.getDistanceFromLeftEndOfBeam_m() + ";" +
                    load.getLengthOfLineLoad_m() + ";" +
                    load.getIncludeThisLoadIntoCalculation() + ";" +
                    load.getError() + "#"
            );
        }
        return beamLoadListStringObject.toString();
    }

    /**
     * Convert from String- Object.
     *
     * @param loadListStringObject
     * @return beamLoadList
     * @see Load
     */
    public static List fromStringObject(String loadListStringObject) {
        if (loadListStringObject == null)
            return null;

        List<Load> beamLoadListObject = new ArrayList<>();
        String[] rows = loadListStringObject.split("#");
        int numberOfRows = rows.length;
        boolean includeThisLoadIntoCalculation, loadHasAnError;

        for (int i = 0; i <= numberOfRows - 1; i++) {

            Log.v("ROW", rows[i]);

            String[] columns = rows[i].split(";");

            if (columns[INCLUDE_THIS_LOAD_INTO_CALCULATION].equals("true"))
                includeThisLoadIntoCalculation = true;
            else
                includeThisLoadIntoCalculation = false;

            if (columns[HAS_LOAD_AN_ERROR].equals("true"))
                loadHasAnError = true;
            else
                loadHasAnError = false;

            Load load = new Load(columns[NAME_OF_LOAD],
                    Double.parseDouble(columns[FORCE_N]),
                    Double.parseDouble(columns[DISTANCE_FROM_LEFT_END_m]),
                    Double.parseDouble(columns[LENGTH_OF_LINE_LOAD_m]),
                    includeThisLoadIntoCalculation,
                    loadHasAnError, 0);

            beamLoadListObject.add(load);

            Log.v("ROW", "BOOLEAN" + includeThisLoadIntoCalculation);
        }
        return beamLoadListObject;
    }
}
