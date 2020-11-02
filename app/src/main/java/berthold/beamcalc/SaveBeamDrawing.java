package berthold.beamcalc;
/**
 * Saves the drawn result of an calculation to a file.
 *
 */

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.provider.ContactsContract;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.berthold.beamCalc.*;

import berthold.filedialogtool.*;

public class SaveBeamDrawing extends AppCompatActivity implements FragmentCustomDialogYesNo.getDataFromFragment{

    // Data
    Beam beam;
    BeamResult result;

    // UI
    ImageButton saveDrawingOfBitmap;
    ImageView drawingOfBitmap;
    Bitmap drawingOfResult;

    // File chooser
    private static final int GET_PATH=1;        // Request code for file chooser.
    private static File workingDir;
    private static String appDir="Beam_Calc";   // App's working dir..
    private String savePath;

    // Confirm dialog fragment
    private static final int NOT_REQUIERED=0;
    private static final int REQUEST_YES_NO_DIALOG_SAVE_NEW_FILE=2;

    private static final int REQUEST_YES_NO_DIALOG_OVERWRITE_EXISTING_FILE=3;
    private String pathAndFilenameOfFileToBeOverwritten;

    private boolean SHOW_CONFIRM_OVERWRITE;
    private boolean SHOW_CONFIRM_SAVE_AT;

    /*
     * App starts here.....
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_beam_drawing);

        // File system
        //
        // @rem:Filesystem, creates public folder in the devices externalStorage dir...@@
        //
        // This seems to be the best practice. It creates a public folder.
        // This folder will not be deleted when the app is de- installed
        workingDir= Environment.getExternalStoragePublicDirectory(appDir);
        workingDir.mkdirs(); // Create dir, if it does not already exist
    }

    /**
     * On Resume
     */

    @Override
    public void onResume() {
        super.onResume();

        // UI
        saveDrawingOfBitmap=(ImageButton)findViewById(R.id.save_drawing_as_bitmap);
        drawingOfBitmap=(ImageView)findViewById(R.id.drawing_of_bitmap);

        // Init
        drawingOfResult=MainActivity.getDrawingOfResult();
        if (drawingOfResult!=null)
            drawingOfBitmap.setImageBitmap(drawingOfResult);

        // Buttons

        // Save drawing as.....
        saveDrawingOfBitmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(SaveBeamDrawing.this, FileDialog.class);
                i.putExtra(FileDialog.MY_TASK_FOR_TODAY_IS,FileDialog.SAVE_FILE);
                i.putExtra(FileDialog.OVERRIDE_LAST_PATH_VISITED,false);
                startActivityForResult(i,GET_PATH);
            }
        });

        // Show dialog
        if (SHOW_CONFIRM_SAVE_AT){
            showConfirmDialog(REQUEST_YES_NO_DIALOG_SAVE_NEW_FILE,
                    FragmentCustomDialogYesNo.SHOW_WITH_EDIT_TEXT,
                    getResources().getString(R.string.confirm_dialog_save_at)+"\n"+savePath,
                    getResources().getString(R.string.confirm_save_at),
                    getResources().getString(R.string.cancel_save));
        }

        if (SHOW_CONFIRM_OVERWRITE){
            showConfirmDialog(REQUEST_YES_NO_DIALOG_SAVE_NEW_FILE,
                    FragmentCustomDialogYesNo.SHOW_AS_YES_NO_DIALOG,
                    (getResources().getString(R.string.confirm_dialog_overwrite)+" \n"+savePath),
                    getResources().getString(R.string.confirm_save_at),
                    getResources().getString(R.string.cancel_save));
        }
    }

    /**
     * {@link FileDialog} returns to this method when left.
     *
     * Handle result's accordingly (e.g. ask if a picked file can be overwritten etc.)
     *
     */

    @Override
    protected void onActivityResult(int reqCode,int resCode,Intent data)
    {
        if (resCode==RESULT_OK && reqCode==GET_PATH ){
            if (data.hasExtra("path")) {

                // When saving a file:
                // Check if just the folder was picked, in that case you have to take care
                // that a filename will be assigned or:
                // if a folder and an existing file was selected, in that case you may want to
                // check if the user wants the selected file to be overwritten...
                String returnStatus = data.getExtras().getString(FileDialog.RETURN_STATUS);

                if (data.hasExtra("path"))
                   savePath=data.getExtras().getString("path");

                if (returnStatus!=null) {
                    // File was picked, ask user if he want's to overwrite it
                    if (returnStatus.equals(FileDialog.FOLDER_AND_FILE_PICKED)) {
                        SHOW_CONFIRM_OVERWRITE=true;
                        SHOW_CONFIRM_SAVE_AT=false;
                    }

                    // Just the folder, ask user for filename
                    if (returnStatus.equals(FileDialog.JUST_THE_FOLDER_PICKED)) {
                        SHOW_CONFIRM_OVERWRITE=false;
                        SHOW_CONFIRM_SAVE_AT=true;
                    }
                }
            }
        }
    }

    /**
     * Callback for yesNoDialog
     *
     * @see FragmentCustomDialogYesNo
     *
     */
    @Override
    public void getDialogInput(int reqCode,int data,String filename,String buttonPressed)
    {
        // Callback from confirm dialog
        // Existing file will be overwritten or a new file will be created
        // depending on the users choice in previously shown confirm dialog.
        if(reqCode==REQUEST_YES_NO_DIALOG_SAVE_NEW_FILE){
            File test=new File (savePath + "/"+filename);

            if (buttonPressed.equals(FragmentCustomDialogYesNo.BUTTON_OK_PRESSED)) {

                // Create a new file if it does not already exist
                if (SHOW_CONFIRM_SAVE_AT) {
                    if (!test.exists())
                        saveFile(savePath + "/" + filename);
                    else {
                        // File already exists
                        // Ask user if he want's to overwrite it
                        showConfirmDialog(REQUEST_YES_NO_DIALOG_OVERWRITE_EXISTING_FILE,
                                FragmentCustomDialogYesNo.SHOW_AS_YES_NO_DIALOG,
                                (getResources().getString(R.string.confirm_dialog_name_already_exists) + " \n" + savePath + "/" + filename),
                                getResources().getString(R.string.confirm_save_at),
                                getResources().getString(R.string.cancel_save));
                        pathAndFilenameOfFileToBeOverwritten=savePath+"/"+filename;
                        Log.v("SECOND:",pathAndFilenameOfFileToBeOverwritten);
                    }
                }

                // Overwrite existing file
                if (SHOW_CONFIRM_OVERWRITE)
                    saveFile(savePath);

            } else {
                Log.v("CANCEL", " Button:" + buttonPressed);
            }
        }
        // This is executed when in an prvious dialog filename of an existing file
        // was entered...
        if(reqCode==REQUEST_YES_NO_DIALOG_OVERWRITE_EXISTING_FILE){
            if(buttonPressed.equals(FragmentCustomDialogYesNo.BUTTON_OK_PRESSED)){
                saveFile(pathAndFilenameOfFileToBeOverwritten);
                Log.v("FILENAME",":"+pathAndFilenameOfFileToBeOverwritten);
            }
        }
        SHOW_CONFIRM_SAVE_AT=false;
        SHOW_CONFIRM_OVERWRITE=false;
    }

    /**
     * Save file
     *
     */

    private void saveFile(String savePath)
    {
        try {
            FileOutputStream file = new FileOutputStream(savePath);
            drawingOfResult.compress(Bitmap.CompressFormat.PNG, 1, file);
            file.close();
        }  catch (IOException e) {
            Log.v("ERROR", e.toString());
        }
        Toast.makeText(getApplicationContext(),getResources().getString(R.string.drawing_was_saved),Toast.LENGTH_LONG).show();
    }

    /**
     * Show confirm at dialog.
     *
     * @see FragmentCustomDialogYesNo
     */

    private void showConfirmDialog(int reqCode,int kindOfDialog,String dialogText,String confirmButton,String cancelButton)
    {
        FragmentManager fm = getSupportFragmentManager();
        FragmentCustomDialogYesNo fragmentDeleteRegex =
                FragmentCustomDialogYesNo.newInstance(reqCode,kindOfDialog,NOT_REQUIERED,null,dialogText,confirmButton,cancelButton);
        fragmentDeleteRegex.show(fm, "fragment_dialog");
    }

}
