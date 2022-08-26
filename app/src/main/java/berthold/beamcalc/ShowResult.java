package berthold.beamcalc;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import org.berthold.beamCalc.Beam;
import org.berthold.beamCalc.BeamCalcError;
import org.berthold.beamCalc.BeamResult;
import org.berthold.beamCalc.Load;
import org.berthold.beamCalc.MSolver;
import org.berthold.beamCalc.QSolver;
import org.berthold.beamCalc.StressResultant;
import org.berthold.beamCalc.StressResultantTable;

import java.util.ArrayList;
import java.util.List;

import gfxNonOverlapping.RectArea;
import gfxNonOverlapping.Rectangles;


/**
 * Show Result
 * <p>
 * Draws result and returns a bitmap of it.
 * <p>
 * Created by Berthold on 1/21/19.
 */
public class ShowResult {

    // That is the size of the bitmap we use to draw our beam and the result
    private static final int CANVAS_SIZE_X = 1200;
    private static final int CANVAS_SIZE_Y = 600;
    private static final int MIN_PADDING_FROM_ANY_BORDER = 20;

    // Extra space for dimensions of beam for canvas hight
    private static final int CANVAS_SIZE_ADDED_FOR_DIMENSIONS = 300;
    private static final int LENGTH_OF_MARKER = 15;
    private static final int POSITION_OF_HORIZ_DIMENSION_BAR_LINE_LOADS = CANVAS_SIZE_Y + 50;
    private static final int POSITION_OF_HORIZ_DIMENSION_BAR_SINGLE_LOADS = CANVAS_SIZE_Y + 110;
    private static final int POSITION_OF_HORIZ_DIMENSION_BAR_BEAM = CANVAS_SIZE_Y + 170;

    // Padding
    // This determines the actual length of the beam in pixel and it's
    // horizontal and vertical pos. on canvas.
    private static final int PADDING_X = 200;
    private static final int PADDING_Y = 80;

    // Number format
    // ToDO: Get this from library... Beam- Object has this stored (passed when solve is started.....)
    private static String floatNumberFormatPreset = "%.2f";

    // Drawing parameters
    private static final int RESULTING_FORCE_TEXT_X_OFFSET = 20;
    private static final int RESULTING_FORCE_TEXT_Y_OFFSET = 100;

    private static final int ACTING_FORCE_TEXT_X_OFFSET = 5;
    private static final int ACTING_FORCE_TEXT_Y_OFFSET = -40;

    private static final int DIMENSION_TEXT_XOFFSET = 20; // If dimension text is wider than space between marks...
    private static final int DIMENSION_TEXT_YOFFSET = -20;

    // Parameters for text output
    private static final float TEXT_SIZE = 35F;
    private static final int TEXT_VERTICAL_SPACING = 10;

    // parameters for layout of beam drawing and drawing of math term with solution
    private static final int DRAW_LEFT_OF_DRWAING = 0;
    private static final int DRAW_BELOW_DRAWING = 1;

    // Bitmap parameters
    private static final int SUPPORT_BITMAP_XSIZE = 30;
    private static final int SUPPORT_BITMAP_YSIZE = 30;
    private static final int SUPPORT_VERTICAL_DIST_FROM_BEAM = 0;
    private static final int SUPPORT_CENTER_X = 15; // Alligns support center with load center....

    private static final int ARROW_XSIZE = 21;
    private static final int ARROW_YSIZE = 21;
    private static final int ARROW_X_OFFSET = -11;
    private static final int DOWN_ARROW_Y_OFFSET = -25;
    private static final int UP_ARROW_Y_OFFSET = 0;

    /**
     * Draws beam, loads and result....
     * <p>
     * Principle of operation:
     * Length of beam in [m] is the base scale. Left end of beamm is x=0,y=0
     * All loads are drawn relative to the beams left end.
     * Beam is drawn centered on canvas.
     *
     * @param result,beam,displayResult,resources
     * @return Bitmap containing either an error- message or the result.
     * @see BeamResult,Beam
     */
    public static Bitmap draw(BeamResult result, Beam beam, boolean displayResult, Resources resources) {

        // Init
        Bitmap bitmap = Bitmap.createBitmap(CANVAS_SIZE_X, CANVAS_SIZE_Y, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.WHITE);

        Bitmap bitmapWithResult;
        if (result.getErrorCount() == 0) {
            bitmapWithResult = drawBeamAndResult(bitmap, beam, result, displayResult, resources);
            bitmapWithResult = addDimensionsOfBeam(bitmapWithResult, beam, result, resources);

            if (displayResult)
                bitmapWithResult = addMathTermWithSolution(bitmapWithResult, beam, result, DRAW_BELOW_DRAWING, resources);

        } else
            bitmapWithResult = drawError(bitmap, result, resources);

        return bitmapWithResult;
    }

    /**
     * Draw description of error.
     *
     * @param bitmap
     * @param result
     * @param resources
     * @return Bitpmap with discription of error.
     */
    private static Bitmap drawError(Bitmap bitmap, BeamResult result, Resources resources) {
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setTextSize(TEXT_SIZE);
        paint.setColor(Color.RED);

        float textX = 20;
        float textY = PADDING_Y;
        float textHeight = 0;

        // @rem:Get string via resources 'getString()'@@
        String errorTextHeading = resources.getString(R.string.error_message);
        canvas.drawText(errorTextHeading, textX, textY, paint);

        int errorCount = result.getErrorCount();
        for (int i = 0; i <= errorCount - 1; i++) {
            BeamCalcError error = result.getError(i);
            String errorDescription = error.getErrorDescription();
            textHeight = getTextHeightInPixels(errorDescription, paint);
            textY = textY + textHeight;
            canvas.drawText(errorDescription, textX, textY, paint);
        }
        return bitmap;
    }

    /**
     * Draws the solution.
     *
     * @param bitmap
     * @param beam
     * @param result
     * @param displayResult
     * @param resources
     * @return Bitmap containing an image of the solution.
     */
    private static Bitmap drawBeamAndResult(Bitmap bitmap, Beam beam, BeamResult result, boolean displayResult, Resources resources) {
        Canvas canvas = new Canvas(bitmap);

        // This takes care that text drawn does not overlap.
        // Each rectangular, bounding box added to this instance
        // will be shifted if it overlaps any other object in it's list.
        Rectangles rectangles=new Rectangles(CANVAS_SIZE_X,CANVAS_SIZE_Y);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(TEXT_SIZE);
        paint.setAlpha(Color.BLACK);

        // Init
        paint.setColor(Color.BLACK);
        int beamLengthInPixels = getBeamLengthInPixels(CANVAS_SIZE_X, PADDING_X);
        int x0 = getBeamStartXCoordinate(beamLengthInPixels, CANVAS_SIZE_X);
        int y0 = getBeamStartYCoordinate(CANVAS_SIZE_Y);

        int maxLoadLengthInPixels = getMaxLoadLengthInPixels(CANVAS_SIZE_Y, PADDING_Y);
        double maxLoadIn_N = beam.getMaxLoadIn_N();

        // Draw loads
        paint.setStrokeWidth(1F);

        Load load;
        Double x;

        // Line load
        for (int i = 0; i <= beam.getNumberOfLoads() - 1; i++) {
            paint.setColor(Color.LTGRAY);
            paint.setAlpha(50);
            load = beam.getLoad(i);

            float loadLengthInPixels = getLoadLengthInPixels(load.getForce_N(), maxLoadIn_N, maxLoadLengthInPixels);
            x = getXcoordinateFromBeamStartInPixels(load.getDistanceFromLeftEndOfBeam_m(), beamLengthInPixels, beam.getLength());

            if (load.getLengthOfLineLoad_m() > 0) {
                Double lengthOfLineLoadInPixels = convertLength_m_toLengthPixels(load.getLengthOfLineLoad_m(), beamLengthInPixels, beam.getLength());

                // Draw transparent rect with black border
                paint.setColor(Color.LTGRAY);
                paint.setAlpha(100);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawRect((float) (x0 + x), y0 + loadLengthInPixels, (float) (x0 + x + lengthOfLineLoadInPixels), y0, paint);

                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(Color.BLACK);
                canvas.drawRect((float) (x0 + x), y0 + loadLengthInPixels, (float) (x0 + x + lengthOfLineLoadInPixels), y0, paint);

                paint.setStyle(Paint.Style.FILL);

                String textLoadAndLenght = load.getName() + "=" + String.format(floatNumberFormatPreset, load.getForce_N()) + "N/m  l=" + String.format(floatNumberFormatPreset, load.getLengthOfLineLoad_m()) + " m";

                float textHeight = getTextHeightInPixels(textLoadAndLenght, paint);
                float textWidth=getTextWidthInpixels(textLoadAndLenght,paint);
                float xPosOftext=(float)(x0 + x + ACTING_FORCE_TEXT_X_OFFSET);
                float yPosOfText=   y0 + loadLengthInPixels + ACTING_FORCE_TEXT_Y_OFFSET + textHeight;

                // This prevents overlapping with each other element added to the list of rectangular areas.
                // tOdo: Add this behaviour to support names and other elements...
                RectArea a=new RectArea((int)xPosOftext,(int)yPosOfText,(int)textWidth,(int)textHeight);
                a=rectangles.add(a);

                canvas.drawText(textLoadAndLenght, a.getX(), a.getY(), paint);
            }
        }

        // Single loads...
        paint.setColor(Color.BLACK);
        for (int i = 0; i <= beam.getNumberOfLoads() - 1; i++) {
            load = beam.getLoad(i);
            float loadLengthInPixels = getLoadLengthInPixels(load.getForce_N(), maxLoadIn_N, maxLoadLengthInPixels);
            x = getXcoordinateFromBeamStartInPixels(load.getDistanceFromLeftEndOfBeam_m(), beamLengthInPixels, beam.getLength());

            if (load.getLengthOfLineLoad_m() == 0) {
                // Direction of load is given by the sign of load length in pixels....
                canvas.drawLine((float) (x0 + x), (y0 + loadLengthInPixels), (float) (x0 + x), y0, paint);

                if (load.getForce_N() < 0) {
                    Bitmap arrowdown = BitmapFactory.decodeResource(resources, R.drawable.arrowdown);
                    Bitmap marrowdown = Bitmap.createScaledBitmap(arrowdown, ARROW_XSIZE, ARROW_YSIZE, true);
                    bitmap = MyBitmapTools.drawTransparentBitmap(bitmap, marrowdown, Color.WHITE, (float) (x0 + x + ARROW_X_OFFSET), y0 + DOWN_ARROW_Y_OFFSET);
                    canvas.drawBitmap(bitmap, 0, 0, paint);
                } else {
                    Bitmap arrowup = BitmapFactory.decodeResource(resources, R.drawable.arrowup);
                    Bitmap marrowup = Bitmap.createScaledBitmap(arrowup, ARROW_XSIZE, ARROW_YSIZE, true);
                    bitmap = MyBitmapTools.drawTransparentBitmap(bitmap, marrowup, Color.WHITE, (float) (x0 + x + ARROW_X_OFFSET), y0 + UP_ARROW_Y_OFFSET);
                    canvas.drawBitmap(bitmap, 0, 0, paint);
                }
                String loadDescription = load.getName() + "=" + String.format(floatNumberFormatPreset, load.getForce_N()) + " N";

                // This prevents from load description written over the right border of the canvas
                x = getSaveXpos(loadDescription, x, x0, paint);

                float textHeight = getTextHeightInPixels(loadDescription, paint);
                float textWidth=getTextWidthInpixels(loadDescription,paint);
                float xPosOftext=(float)(x0 + x + ACTING_FORCE_TEXT_X_OFFSET);
                float yPosOfText=   y0 + loadLengthInPixels + ACTING_FORCE_TEXT_Y_OFFSET ;

                RectArea a=new RectArea((int)xPosOftext,(int)yPosOfText,(int)textWidth,(int)textHeight);
                a=rectangles.add(a);
                canvas.drawText(loadDescription, a.getX(), a.getY(), paint);
            }
        }

        // Marks maximal and minimal of bending moments
        if(displayResult) {
            StressResultantTable qTable = QSolver.solve(beam, "N");
            StressResultantTable mTable = MSolver.solve(qTable, beam, "Nm");
            List<StressResultant> maxima = new ArrayList<>();
            maxima = mTable.getMaxima();

            for (StressResultant r : maxima) {

                x = getXcoordinateFromBeamStartInPixels(r.getX_m(), beamLengthInPixels, beam.getLength());

                paint.setAlpha(50);
                if (r.getShearingForce()>0)
                    paint.setColor(Color.GREEN);
                else
                    paint.setColor(Color.RED);

                canvas.drawCircle((float) (x0 + x), y0, 10, paint);

                String bendingMoment =String.format(floatNumberFormatPreset,r.getShearingForce()) + " " + r.getUnit();

                String pos="x="+String.format(floatNumberFormatPreset,r.getX_m())+" m";

                x = getSaveXpos(bendingMoment, x, x0, paint);
                paint.setAlpha(255);
                paint.setColor(Color.BLACK);
                if (r.getShearingForce() > 0) {
                    canvas.drawText(bendingMoment, (float) (x0 + x + ACTING_FORCE_TEXT_X_OFFSET), y0 - ACTING_FORCE_TEXT_Y_OFFSET, paint);
                    canvas.drawText(pos,(float) (x0 + x + ACTING_FORCE_TEXT_X_OFFSET), y0 - 2*ACTING_FORCE_TEXT_Y_OFFSET, paint);
                } else {
                    canvas.drawText(bendingMoment, (float) (x0 + x + ACTING_FORCE_TEXT_X_OFFSET), y0 + ACTING_FORCE_TEXT_Y_OFFSET, paint);
                    canvas.drawText(pos,(float) (x0 + x + ACTING_FORCE_TEXT_X_OFFSET), y0 + 2*ACTING_FORCE_TEXT_Y_OFFSET, paint);
                }
            }
        }
        paint.setAlpha(255);
        paint.setColor(Color.BLACK);

        //
        // Draw beam and supports.....
        //
        // Draw left support
        double l = beam.getBearing(0).getDistanceFromLeftEndOfBeam_m();
        int x1 = (int) getXcoordinateFromBeamStartInPixels(l, beamLengthInPixels, beam.getLength());
        Bitmap support = BitmapFactory.decodeResource(resources, R.drawable.support);
        Bitmap msupport = Bitmap.createScaledBitmap(support, SUPPORT_BITMAP_XSIZE, SUPPORT_BITMAP_YSIZE, true);
        bitmap = MyBitmapTools.drawTransparentBitmap(bitmap, msupport, Color.WHITE, x0 + x1 - SUPPORT_CENTER_X, y0 + SUPPORT_VERTICAL_DIST_FROM_BEAM);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        paint.setColor(Color.BLUE);

        String nameOfSupport = beam.getBearing(0).getNameOfSupport();
        if (displayResult)
            canvas.drawText(nameOfSupport + "=" + String.format(floatNumberFormatPreset, result.getResultingForceAtLeftBearing_N()) + " N", x0 + x1, y0 + RESULTING_FORCE_TEXT_Y_OFFSET, paint);

        // Draw right support
        l = beam.getBearing(1).getDistanceFromLeftEndOfBeam_m();
        int x2 = (int) getXcoordinateFromBeamStartInPixels(l, beamLengthInPixels, beam.getLength());
        support = BitmapFactory.decodeResource(resources, R.drawable.fixedsupport);
        msupport = Bitmap.createScaledBitmap(support, SUPPORT_BITMAP_XSIZE, SUPPORT_BITMAP_YSIZE, true);

        bitmap = MyBitmapTools.drawTransparentBitmap(bitmap, msupport, Color.WHITE, x0 + x2 - SUPPORT_CENTER_X, y0 + SUPPORT_VERTICAL_DIST_FROM_BEAM);
        canvas.drawBitmap(bitmap, 0, 0, paint);

        nameOfSupport = beam.getBearing(1).getNameOfSupport();
        if (displayResult)
            canvas.drawText(nameOfSupport + "=" + String.format(floatNumberFormatPreset, result.getResultingForceAtRightBearing_N()) + " N", x0 + x2 - RESULTING_FORCE_TEXT_X_OFFSET, y0 + RESULTING_FORCE_TEXT_Y_OFFSET, paint);

        // Draw beam
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(4F);
        canvas.drawLine(x0, y0, x0 + beamLengthInPixels, y0, paint);

        // Return drawing of beam with loads, supports and the resulting forces....
        return bitmap;
    }

    private void drawSingleLoads(Bitmap bitmap, Beam beam, BeamResult result, boolean displayResult, Resources resources){

    }

    /**
     * Adds dimension to the image containing the solution.
     *
     * @param bitmapOfResult
     * @param beam
     * @param result
     * @param resources
     * @return Bitmap passed, with dimensions added.
     */
    public static Bitmap addDimensionsOfBeam(Bitmap bitmapOfResult, Beam beam, BeamResult result, Resources resources) {

        // Init
        Bitmap bitmapOfResultWithDimensionsOfBeam = Bitmap.createBitmap(CANVAS_SIZE_X, CANVAS_SIZE_Y + CANVAS_SIZE_ADDED_FOR_DIMENSIONS, Bitmap.Config.ARGB_8888);
        bitmapOfResultWithDimensionsOfBeam.eraseColor(Color.WHITE);
        Canvas canvas = new Canvas(bitmapOfResultWithDimensionsOfBeam);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(TEXT_SIZE);
        paint.setAlpha(Color.WHITE);

        // Sort loads by distance from left end in asc. order....
        List<Load> loadsSortedByDistanceFromLeftSupportIn_m;
        loadsSortedByDistanceFromLeftSupportIn_m = beam.getLoadsSortedByDistanceFromLeftSupportDesc();

        int beamLengthInPixels = getBeamLengthInPixels(CANVAS_SIZE_X, PADDING_X);
        double beamLengthIn_m = beam.getLength();
        int x0 = getBeamStartXCoordinate(beamLengthInPixels, CANVAS_SIZE_X);

        // Add result to canvas
        canvas.drawBitmap(bitmapOfResult, 0, 0, null);

        // Draw horizontal dimension bar's
        paint.setColor(Color.DKGRAY);
        canvas.drawLine(x0, +POSITION_OF_HORIZ_DIMENSION_BAR_BEAM, x0 + beamLengthInPixels, +POSITION_OF_HORIZ_DIMENSION_BAR_BEAM, paint);

        // Mark start and end of beam
        canvas.drawLine(x0, POSITION_OF_HORIZ_DIMENSION_BAR_BEAM - LENGTH_OF_MARKER, x0, POSITION_OF_HORIZ_DIMENSION_BAR_BEAM + LENGTH_OF_MARKER, paint);
        canvas.drawLine(x0 + beamLengthInPixels, POSITION_OF_HORIZ_DIMENSION_BAR_BEAM - LENGTH_OF_MARKER, x0 + beamLengthInPixels, POSITION_OF_HORIZ_DIMENSION_BAR_BEAM + LENGTH_OF_MARKER, paint);

        // Draw Mark all loads and add dimensions
        double sumOfL = 0;
        int x = 0;
        String dimensionsIn_m;

        int xOffset;  // If dimensions text is wider than space between marks....
        int yOffset;

        //
        // Mark supports and add dimensions
        //
        double distanceOfLastSupportFromLeftEndIn_m = 0;
        double distanceOfCurrentSupportFromLeftEndIn_m;

        for (int i = 0; i <= beam.getNumberOfBearings() - 1; i++) {

            xOffset = 0;
            yOffset = 0;

            double distanceOfCurrentSupportFromLeftEndInPixels = convertLength_m_toLengthPixels(beam.getBearing(i).getDistanceFromLeftEndOfBeam_m(), beamLengthInPixels, beam.getLength());
            double distanceOfLastSupportFromLeftEndInPixels = convertLength_m_toLengthPixels(distanceOfLastSupportFromLeftEndIn_m, beamLengthInPixels, beam.getLength());
            distanceOfCurrentSupportFromLeftEndIn_m = beam.getBearing(i).getDistanceFromLeftEndOfBeam_m();
            double spaceBetweenSuportsInPixels = distanceOfCurrentSupportFromLeftEndInPixels - distanceOfLastSupportFromLeftEndInPixels;
            x = (int) getXcoordinateFromBeamStartInPixels(distanceOfCurrentSupportFromLeftEndIn_m, beamLengthInPixels, beam.getLength());
            canvas.drawLine(x0 + x, POSITION_OF_HORIZ_DIMENSION_BAR_BEAM - LENGTH_OF_MARKER, x0 + x, POSITION_OF_HORIZ_DIMENSION_BAR_BEAM + LENGTH_OF_MARKER, paint);

            double space = distanceOfCurrentSupportFromLeftEndIn_m - distanceOfLastSupportFromLeftEndIn_m;
            dimensionsIn_m = String.format(floatNumberFormatPreset, space) + " m";

            if (distanceOfCurrentSupportFromLeftEndIn_m > 0) {
                float textWidth = getTextWidthInpixels(dimensionsIn_m, paint);
                distanceOfLastSupportFromLeftEndIn_m = distanceOfCurrentSupportFromLeftEndIn_m;

                double of = getHorizontalCenterCoordinatesOfTextBetweenMarkers(textWidth, spaceBetweenSuportsInPixels);

                if (spaceBetweenSuportsInPixels <= textWidth) {
                    xOffset = DIMENSION_TEXT_XOFFSET;
                    yOffset = DIMENSION_TEXT_YOFFSET;
                }
                canvas.drawText(dimensionsIn_m, x0 + x - (float) of + xOffset, POSITION_OF_HORIZ_DIMENSION_BAR_BEAM + yOffset, paint);
            }
            sumOfL = sumOfL + distanceOfCurrentSupportFromLeftEndIn_m;
        }
        double restLengthIn_m = beam.getLength() - distanceOfLastSupportFromLeftEndIn_m;

        // Space between last support and beam end, if any......
        xOffset = 0;
        yOffset = 0;

        if (restLengthIn_m > 0) {
            dimensionsIn_m = String.format(floatNumberFormatPreset, restLengthIn_m) + " m";

            float textWidth = getTextWidthInpixels(dimensionsIn_m, paint);
            double spaceBetweenLastSupportAndBeamEndInPixels = convertLength_m_toLengthPixels(restLengthIn_m, beamLengthInPixels, beam.getLength());
            double of = getHorizontalCenterCoordinatesOfTextBetweenMarkers(textWidth, spaceBetweenLastSupportAndBeamEndInPixels);

            if (spaceBetweenLastSupportAndBeamEndInPixels <= textWidth) {
                xOffset = DIMENSION_TEXT_XOFFSET;
                yOffset = DIMENSION_TEXT_YOFFSET;
            }
            canvas.drawText(dimensionsIn_m, x0 + x + (float) spaceBetweenLastSupportAndBeamEndInPixels - (float) of + xOffset, POSITION_OF_HORIZ_DIMENSION_BAR_BEAM + yOffset, paint);
        }

        //
        // Mark all single loads and add dimensions
        //
        double distanceOfLastLoadFromLeftEndIn_m = 0;
        double distanceOfCurrentLoadFromLeftEndIn_m = 0;

        if (beam.getNumberOfSingleLoads() > 0) {
            paint.setColor(Color.DKGRAY);
            canvas.drawLine(x0, +POSITION_OF_HORIZ_DIMENSION_BAR_SINGLE_LOADS, x0 + beamLengthInPixels, +POSITION_OF_HORIZ_DIMENSION_BAR_SINGLE_LOADS, paint);

            // Mark start and end of beam
            canvas.drawLine(x0, POSITION_OF_HORIZ_DIMENSION_BAR_SINGLE_LOADS - LENGTH_OF_MARKER, x0, POSITION_OF_HORIZ_DIMENSION_BAR_SINGLE_LOADS + LENGTH_OF_MARKER, paint);
            canvas.drawLine(x0 + beamLengthInPixels, POSITION_OF_HORIZ_DIMENSION_BAR_SINGLE_LOADS - LENGTH_OF_MARKER, x0 + beamLengthInPixels, POSITION_OF_HORIZ_DIMENSION_BAR_SINGLE_LOADS + LENGTH_OF_MARKER, paint);

            for (int i = 0; i <= beam.getNumberOfLoads() - 1; i++) {

                xOffset = 0;
                yOffset = 0;

                Load load = loadsSortedByDistanceFromLeftSupportIn_m.get(i);

                if (load.getIncludeThisLoadIntoCalculation()) {
                    if (load.getLengthOfLineLoad_m() == 0) {

                        double distanceOfCurrentLoadFromLeftEndInPixels = convertLength_m_toLengthPixels(load.getDistanceFromLeftEndOfBeam_m(), beamLengthInPixels, beam.getLength());
                        double distanceOfLastLoadFromLeftEndInPixels = convertLength_m_toLengthPixels(distanceOfLastLoadFromLeftEndIn_m, beamLengthInPixels, beam.getLength());
                        distanceOfCurrentLoadFromLeftEndIn_m = load.getDistanceFromLeftEndOfBeam_m();
                        double spaceBetweenLoadsInPixels = distanceOfCurrentLoadFromLeftEndInPixels - distanceOfLastLoadFromLeftEndInPixels;
                        x = (int) getXcoordinateFromBeamStartInPixels(distanceOfCurrentLoadFromLeftEndIn_m, beamLengthInPixels, beam.getLength());
                        canvas.drawLine(x0 + x, POSITION_OF_HORIZ_DIMENSION_BAR_SINGLE_LOADS - LENGTH_OF_MARKER, x0 + x, POSITION_OF_HORIZ_DIMENSION_BAR_SINGLE_LOADS + LENGTH_OF_MARKER, paint);

                        double space = distanceOfCurrentLoadFromLeftEndIn_m - distanceOfLastLoadFromLeftEndIn_m;

                        dimensionsIn_m = String.format(floatNumberFormatPreset, space) + " m";
                        canvas.drawLine(x0 + x, POSITION_OF_HORIZ_DIMENSION_BAR_SINGLE_LOADS - LENGTH_OF_MARKER, x0 + x, POSITION_OF_HORIZ_DIMENSION_BAR_SINGLE_LOADS + LENGTH_OF_MARKER, paint);

                        // If load's pos is 0 or the same as the last load's, do not draw or draw again....
                        if (distanceOfCurrentLoadFromLeftEndIn_m > 0 && distanceOfCurrentLoadFromLeftEndIn_m != distanceOfLastLoadFromLeftEndIn_m) {
                            float textWidth = getTextWidthInpixels(dimensionsIn_m, paint);
                            distanceOfLastLoadFromLeftEndIn_m = distanceOfCurrentLoadFromLeftEndIn_m;

                            double of = getHorizontalCenterCoordinatesOfTextBetweenMarkers(textWidth, spaceBetweenLoadsInPixels);

                            if (spaceBetweenLoadsInPixels <= textWidth) {
                                xOffset = DIMENSION_TEXT_XOFFSET;
                                yOffset = DIMENSION_TEXT_YOFFSET;
                            }
                            canvas.drawText(dimensionsIn_m, x0 + x - (float) of + xOffset, POSITION_OF_HORIZ_DIMENSION_BAR_SINGLE_LOADS + yOffset, paint);
                        }
                        Log.v("LOAD", "sumOfLength:" + sumOfL + "Current distanceFromleft:" + distanceOfCurrentLoadFromLeftEndIn_m + " Last DistanceFromLeft:" + distanceOfLastLoadFromLeftEndIn_m);
                    }
                }
            }
        }

        //
        // Mark all line loads
        //
        if (beam.getNumberOfLineLoads() > 0) {
            distanceOfLastLoadFromLeftEndIn_m = 0;

            paint.setColor(Color.DKGRAY);
            canvas.drawLine(x0, +POSITION_OF_HORIZ_DIMENSION_BAR_LINE_LOADS, x0 + beamLengthInPixels, +POSITION_OF_HORIZ_DIMENSION_BAR_LINE_LOADS, paint);

            // Mark start and end of beam
            canvas.drawLine(x0, POSITION_OF_HORIZ_DIMENSION_BAR_LINE_LOADS - LENGTH_OF_MARKER, x0, POSITION_OF_HORIZ_DIMENSION_BAR_LINE_LOADS + LENGTH_OF_MARKER, paint);
            canvas.drawLine(x0 + beamLengthInPixels, POSITION_OF_HORIZ_DIMENSION_BAR_LINE_LOADS - LENGTH_OF_MARKER, x0 + beamLengthInPixels, POSITION_OF_HORIZ_DIMENSION_BAR_LINE_LOADS + LENGTH_OF_MARKER, paint);

            for (int i = 0; i <= beam.getNumberOfLoads() - 1; i++) {

                Load load = loadsSortedByDistanceFromLeftSupportIn_m.get(i);

                // todo DRY! => Same code as in "mark all single loads..."
                xOffset = 0;
                yOffset = 0;

                if (load.getLengthOfLineLoad_m() > 0) {
                    double distanceOfCurrentLoadFromLeftEndInPixels = convertLength_m_toLengthPixels(load.getDistanceFromLeftEndOfBeam_m(), beamLengthInPixels, beam.getLength());
                    double distanceOfLastLoadFromLeftEndInPixels = convertLength_m_toLengthPixels(distanceOfLastLoadFromLeftEndIn_m, beamLengthInPixels, beam.getLength());
                    distanceOfCurrentLoadFromLeftEndIn_m = load.getDistanceFromLeftEndOfBeam_m();
                    double spaceBetweenLoadsInPixels = distanceOfCurrentLoadFromLeftEndInPixels - distanceOfLastLoadFromLeftEndInPixels;

                    x = (int) getXcoordinateFromBeamStartInPixels(load.getDistanceFromLeftEndOfBeam_m(), beamLengthInPixels, beam.getLength());

                    canvas.drawLine(x0 + x, POSITION_OF_HORIZ_DIMENSION_BAR_LINE_LOADS - LENGTH_OF_MARKER, x0 + x, POSITION_OF_HORIZ_DIMENSION_BAR_LINE_LOADS + LENGTH_OF_MARKER, paint);

                    double space = distanceOfCurrentLoadFromLeftEndIn_m - distanceOfLastLoadFromLeftEndIn_m;
                    dimensionsIn_m = String.format(floatNumberFormatPreset, space) + " m";

                    canvas.drawLine(x0 + x, POSITION_OF_HORIZ_DIMENSION_BAR_LINE_LOADS - LENGTH_OF_MARKER, x0 + x, POSITION_OF_HORIZ_DIMENSION_BAR_LINE_LOADS + LENGTH_OF_MARKER, paint);

                    if (distanceOfCurrentLoadFromLeftEndIn_m > 0 && distanceOfCurrentLoadFromLeftEndIn_m != distanceOfLastLoadFromLeftEndIn_m) {
                        float textWidth = getTextWidthInpixels(dimensionsIn_m, paint);
                        distanceOfLastLoadFromLeftEndIn_m = distanceOfCurrentLoadFromLeftEndIn_m;

                        if (spaceBetweenLoadsInPixels <= textWidth) {
                            xOffset = DIMENSION_TEXT_XOFFSET;
                            yOffset = DIMENSION_TEXT_YOFFSET;
                        }

                        double of = getHorizontalCenterCoordinatesOfTextBetweenMarkers(textWidth, spaceBetweenLoadsInPixels);

                        canvas.drawText(dimensionsIn_m, x0 + x - (float) of + xOffset, POSITION_OF_HORIZ_DIMENSION_BAR_LINE_LOADS + yOffset, paint);
                    }
                    //sumOfL = sumOfL + distanceOfCurrentLoadFromLeftEndIn_m;
                }
            }
        }
        // That's it....
        return bitmapOfResultWithDimensionsOfBeam;
    }

    /**
     * Adds the mathematical term with the solution for the left and right bearing
     * to an existing bitmap.
     *
     * @param bitmapOfResult
     * @param beam
     * @param result
     * @return Bitmap with solution, mathematical term wit detailed solution added.
     */
    private static Bitmap addMathTermWithSolution(Bitmap bitmapOfResult, Beam beam, BeamResult result, int layout, Resources resources) {

        Bitmap bitmapOfResultWithMathSolutionTerm;

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(TEXT_SIZE);
        paint.setAlpha(Color.WHITE);

        // Get mathematical term's
        String termForLeftBearing = result.getSolutionTermForLeftBearing();
        String termForRightBearing = result.getSolutionTermForRightBearing();

        // Dimensions of bitmap of the result (bram drawing, with or without the dimensions
        float bitmapOfResultWidth = bitmapOfResult.getWidth();
        float bitmapOfResultHeight = bitmapOfResult.getHeight();

        // High and width of bitmap needed for text output of term with math solution.
        float textHightInPixelsLeftAndRightBearing = getTextHeightInPixels(termForLeftBearing, paint);
        float textWidthInPixelsLeftBearing = getTextWidthInpixels(termForLeftBearing, paint);
        float textWidthInPixelsRightBearing = getTextWidthInpixels(termForRightBearing, paint);

        // Get max width of math term. This will determine the resulting bitmaps width.
        float widthForTerm;
        if (textWidthInPixelsLeftBearing > textWidthInPixelsRightBearing)
            widthForTerm = textWidthInPixelsLeftBearing + 2 * MIN_PADDING_FROM_ANY_BORDER;
        else
            widthForTerm = textWidthInPixelsRightBearing + 2 * MIN_PADDING_FROM_ANY_BORDER;

        // Create bitmap with math term
        switch (layout) {

            // ToDo Alpha, at the best :-)
            case DRAW_LEFT_OF_DRWAING: {

                // Create bitmap
                bitmapOfResultWithMathSolutionTerm = Bitmap.createBitmap(CANVAS_SIZE_X + (int) widthForTerm + MIN_PADDING_FROM_ANY_BORDER, CANVAS_SIZE_Y + CANVAS_SIZE_ADDED_FOR_DIMENSIONS, Bitmap.Config.ARGB_8888);
                bitmapOfResultWithMathSolutionTerm.eraseColor(Color.WHITE);
                Canvas canvas = new Canvas(bitmapOfResultWithMathSolutionTerm);

                // Add term
                canvas.drawText(termForLeftBearing, MIN_PADDING_FROM_ANY_BORDER, MIN_PADDING_FROM_ANY_BORDER, paint);
                float textHight = getTextHeightInPixels(termForRightBearing, paint);
                canvas.drawText(termForRightBearing, MIN_PADDING_FROM_ANY_BORDER, MIN_PADDING_FROM_ANY_BORDER + textHight + TEXT_VERTICAL_SPACING, paint);

                // Add result to canvas
                canvas.drawBitmap(bitmapOfResult, widthForTerm, 0, null);

                break;
            }
            case DRAW_BELOW_DRAWING: {

                // Calc high and width for bitmap containing the drawing of the beam, dimension lines and the math terms with solution.
                float highForTermWittMathSolution;
                float verticalPositionOfText;


                highForTermWittMathSolution = 2 * textHightInPixelsLeftAndRightBearing + TEXT_VERTICAL_SPACING + MIN_PADDING_FROM_ANY_BORDER + CANVAS_SIZE_Y + CANVAS_SIZE_ADDED_FOR_DIMENSIONS;
                verticalPositionOfText = CANVAS_SIZE_Y + TEXT_VERTICAL_SPACING + CANVAS_SIZE_ADDED_FOR_DIMENSIONS + MIN_PADDING_FROM_ANY_BORDER;


                // Check if max width of term is bigger or smaller than the drawing of the beam.
                // Set width of bitmap containing both, accordingly.
                float widthOfBitmapWithMathTerm;

                if (bitmapOfResultWidth >= widthForTerm)
                    widthOfBitmapWithMathTerm = bitmapOfResultWidth;
                else
                    widthOfBitmapWithMathTerm = widthForTerm;

                // Create bitmap
                bitmapOfResultWithMathSolutionTerm = Bitmap.createBitmap((int) widthOfBitmapWithMathTerm, (int) highForTermWittMathSolution, Bitmap.Config.ARGB_8888);
                bitmapOfResultWithMathSolutionTerm.eraseColor(Color.WHITE);
                Canvas canvas = new Canvas(bitmapOfResultWithMathSolutionTerm);

                // Add term
                canvas.drawText(termForLeftBearing, MIN_PADDING_FROM_ANY_BORDER, verticalPositionOfText, paint);
                canvas.drawText(termForRightBearing, MIN_PADDING_FROM_ANY_BORDER, verticalPositionOfText + textHightInPixelsLeftAndRightBearing + TEXT_VERTICAL_SPACING, paint);

                // Add drawing of result to canvas
                canvas.drawBitmap(bitmapOfResult, 0, 0, null);

                break;
            }
            default:
                bitmapOfResultWithMathSolutionTerm = bitmapOfResult;
        }
        return bitmapOfResultWithMathSolutionTerm;
    }

    /*
     * Collection of formulas...
     */
    private static int getBeamLengthInPixels(int canvasSizeX, int paddingX) {
        return canvasSizeX - paddingX;
    }

    private static int getMaxLoadLengthInPixels(int canvasSizeY, int paddingY) {
        return canvasSizeY / 2 - paddingY;
    }

    private static float getLoadLengthInPixels(double loadIn_N, double maxLloadIn_N, int maxLoadLengthInPixels) {
        return (float) (loadIn_N / maxLloadIn_N) * maxLoadLengthInPixels;
    }

    private static int getBeamStartXCoordinate(int beamLength, int canvasSizeX) {
        return canvasSizeX / 2 - beamLength / 2;
    }

    private static int getBeamStartYCoordinate(int canvasYSize) {
        return canvasYSize / 2;
    }

    private static double getXcoordinateFromBeamStartInPixels(double positionFromStartOfbeam_m, int beamLengthInPixels, double beamLength_m) {
        return (beamLengthInPixels / beamLength_m) * positionFromStartOfbeam_m;
    }

    private static double convertLength_m_toLengthPixels(double lenght_m, double beamLengthInPixels, double beamLengthIn_m) {
        return (beamLengthInPixels / beamLengthIn_m) * lenght_m;
    }

    // @rem:Determines text width in pixels@@
    private static float getTextWidthInpixels(String text, Paint paint) {
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return bounds.width();
    }

    // @rem:Determines text height in pixels@@
    private static float getTextHeightInPixels(String text, Paint paint) {
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return bounds.height();
    }

    private static double getHorizontalCenterCoordinatesOfTextBetweenMarkers(double textWidth, double spaceBetweenMarkers) {
        double horizontalCenterCoordinatesOfTextBetweenMarkers = textWidth / 2 + spaceBetweenMarkers / 2;
        return horizontalCenterCoordinatesOfTextBetweenMarkers;
    }

    // Prevents text (e.g. load description) from running over right border of drawing canvas
    private static double getSaveXpos(String textLabel, double x, double x0, Paint paint) {
        double textLength = getTextWidthInpixels(textLabel, paint);
        double distOfTextEndFromRightCanvBorder = CANVAS_SIZE_X - (x0 + x + ACTING_FORCE_TEXT_X_OFFSET + textLength);
        if (distOfTextEndFromRightCanvBorder < 0)
            x = (CANVAS_SIZE_X - textLength) - x0 - MIN_PADDING_FROM_ANY_BORDER;
        return x;
    }
}
