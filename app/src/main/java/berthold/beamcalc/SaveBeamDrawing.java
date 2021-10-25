package berthold.beamcalc;
/**
 * Saves the drawn result of an calculation to a file.
 */

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

public class SaveBeamDrawing extends AppCompatActivity implements FragmentYesNoDialog.getDataFromFragment {

    // Data
    Beam beam;
    BeamResult result;

    // UI
    ImageButton saveDrawingOfBitmap;
    ImageView drawingOfBitmap;
    Bitmap drawingOfResult;

    //----------------------------For Android <11 -----------------------------------
    // File chooser
    private static final int GET_PATH = 1;        // Request code for file chooser.
    private static File workingDir;
    private static String appDir = "Beam_Calc";   // App's working dir..
    private String savePath;

    // Confirm dialog fragment
    private static final int REQUEST_YES_NO_DIALOG_SAVE_NEW_FILE = 2;

    private static final int REQUEST_YES_NO_DIALOG_OVERWRITE_EXISTING_FILE = 3;
    private String pathAndFilenameOfFileToBeOverwritten;

    private boolean SHOW_CONFIRM_OVERWRITE;
    private boolean SHOW_CONFIRM_SAVE_AT;
    //----------------------------For Android >= 11 ----------------------------------
   ActivityResultLauncher loadFileActivityResult;
    //--------------------------------------------------------------------------------

    /*
     * App starts here.....
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_beam_drawing);

        //----------------------------For Android <11 -----------------------------------
        // Create a working dir fpr this app, but only when not
        // compiled for Android 11 (SDK 30)....
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            // File system
            //
            // @rem:Filesystem, creates public folder in the devices externalStorage dir...@@
            //
            // This seems to be the best practice. It creates a public folder.
            // This folder will not be deleted when the app is de- installed
            workingDir = Environment.getExternalStoragePublicDirectory(appDir);
            workingDir.mkdirs(); // Create dir, if it does not already exist
        }

        //----------------------------For Android >= 11 ----------------------------------
        //
        // Callback for file Picker if using Android 11 or higher....
        //
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            loadFileActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Log.v("RETURN_DATA_", result.getData().toString());

                        // The result contains an uri which can be used to perform operations on the
                        // document the user selected.
                        //
                        Uri uri;
                        uri = result.getData().getData();

                        // todo add saver.....
                    }
                }
            });
        }
    }

    /**
     * On Resume
     */

    @Override
    public void onResume() {
        super.onResume();

        // UI
        saveDrawingOfBitmap = (ImageButton) findViewById(R.id.save_drawing_as_bitmap);
        drawingOfBitmap = (ImageView) findViewById(R.id.drawing_of_bitmap);

        // Init
        drawingOfResult = MainActivity.getDrawingOfResult();
        if (drawingOfResult != null)
            drawingOfBitmap.setImageBitmap(drawingOfResult);

        // Buttons

        // Save drawing as.....
        saveDrawingOfBitmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                    Intent i = new Intent(SaveBeamDrawing.this, FileDialog.class);
                    i.putExtra(FileDialog.MY_TASK_FOR_TODAY_IS, FileDialog.SAVE_FILE);
                    i.putExtra(FileDialog.OVERRIDE_LAST_PATH_VISITED, false);
                    startActivityForResult(i, GET_PATH);
                } else {
                    //
                    // For Android versions =>11
                    //
                    // This will open the devices file picker and lets
                    // one pick a file....
                    //
                    Intent pickFileIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    pickFileIntent.addCategory(Intent.CATEGORY_OPENABLE);
                    pickFileIntent.setType("image/*");
                    loadFileActivityResult.launch(pickFileIntent);
                }
            }
        });
        //
        // Not needed when compiled for Android 11 (SDK 30).
        //
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            // Show dialog
            if (SHOW_CONFIRM_SAVE_AT) {
                showConfirmDialog(REQUEST_YES_NO_DIALOG_SAVE_NEW_FILE,
                        FragmentYesNoDialog.SHOW_WITH_EDIT_TEXT,
                        getResources().getString(R.string.confirm_dialog_save_at) + "\n" + savePath,
                        getResources().getString(R.string.confirm_save_at),
                        getResources().getString(R.string.cancel_save));
            }

            if (SHOW_CONFIRM_OVERWRITE) {
                showConfirmDialog(REQUEST_YES_NO_DIALOG_SAVE_NEW_FILE,
                        FragmentYesNoDialog.SHOW_AS_YES_NO_DIALOG,
                        (getResources().getString(R.string.confirm_dialog_overwrite) + " \n" + savePath),
                        getResources().getString(R.string.confirm_save_at),
                        getResources().getString(R.string.cancel_save));
            }
        }
    }

    /**
     * {@link FileDialog} returns to this method when left.
     * Not needed when compiled for Android 11 (SDK 30).
     *
     * Handle result's accordingly (e.g. ask if a picked file can be overwritten etc.)
     *
     */
    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            super.onActivityResult(reqCode, resCode, data);
            if (resCode == RESULT_OK && reqCode == GET_PATH) {
                if (data.hasExtra("path")) {

                    // When saving a file:
                    // Check if just the folder was picked, in that case you have to take care
                    // that a filename will be assigned or:
                    // if a folder and an existing file was selected, in that case you may want to
                    // check if the user wants the selected file to be overwritten...
                    String returnStatus = data.getExtras().getString(FileDialog.RETURN_STATUS);

                    if (data.hasExtra("path"))
                        savePath = data.getExtras().getString("path");

                    if (returnStatus != null) {
                        // File was picked, ask user if he want's to overwrite it
                        if (returnStatus.equals(FileDialog.FOLDER_AND_FILE_PICKED)) {
                            SHOW_CONFIRM_OVERWRITE = true;
                            SHOW_CONFIRM_SAVE_AT = false;
                        }

                        // Just the folder, ask user for filename
                        if (returnStatus.equals(FileDialog.JUST_THE_FOLDER_PICKED)) {
                            SHOW_CONFIRM_OVERWRITE = false;
                            SHOW_CONFIRM_SAVE_AT = true;
                        }
                    }
                }
            }
        }
    }

    /**
     * Callback for yesNoDialog
     * Not needed when compiled for Android 11 (SDK 30)
     *
     * @see FragmentYesNoDialog
     *
     */
    @Override
    public void getDialogInput(int reqCode, String filename, String buttonPressed) {
        if (Build.VERSION.SDK_INT< Build.VERSION_CODES.HONEYCOMB) {
            // Callback from confirm dialog
            // Existing file will be overwritten or a new file will be created
            // depending on the users choice in previously shown confirm dialog.
            if (reqCode == REQUEST_YES_NO_DIALOG_SAVE_NEW_FILE) {
                File test = new File(savePath + "/" + filename);

                if (buttonPressed.equals(FragmentYesNoDialog.BUTTON_OK_PRESSED)) {

                    // Create a new file if it does not already exist
                    if (SHOW_CONFIRM_SAVE_AT) {
                        if (!test.exists())
                            saveFile(savePath + "/" + filename);
                        else {
                            // File already exists
                            // Ask user if he want's to overwrite it
                            showConfirmDialog(REQUEST_YES_NO_DIALOG_OVERWRITE_EXISTING_FILE,
                                    FragmentYesNoDialog.SHOW_AS_YES_NO_DIALOG,
                                    (getResources().getString(R.string.confirm_dialog_name_already_exists) + " \n" + savePath + "/" + filename),
                                    getResources().getString(R.string.confirm_save_at),
                                    getResources().getString(R.string.cancel_save));
                            pathAndFilenameOfFileToBeOverwritten = savePath + "/" + filename;
                            Log.v("SECOND:", pathAndFilenameOfFileToBeOverwritten);
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
            if (reqCode == REQUEST_YES_NO_DIALOG_OVERWRITE_EXISTING_FILE) {
                if (buttonPressed.equals(FragmentYesNoDialog.BUTTON_OK_PRESSED)) {
                    saveFile(pathAndFilenameOfFileToBeOverwritten);
                    Log.v("FILENAME", ":" + pathAndFilenameOfFileToBeOverwritten);
                }
            }
            SHOW_CONFIRM_SAVE_AT = false;
            SHOW_CONFIRM_OVERWRITE = false;
        }
    }

    /**
     * Save file
     *
     */
    private void saveFile(String savePath) {
        try {
            FileOutputStream file = new FileOutputStream(savePath);
            drawingOfResult.compress(Bitmap.CompressFormat.PNG, 1, file);
            file.close();
        } catch (IOException e) {
            Log.v("ERROR", e.toString());
        }
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.drawing_was_saved), Toast.LENGTH_LONG).show();
    }

    /**
     * Show confirm at dialog.
     *
     * @see FragmentYesNoDialog
     */
    private void showConfirmDialog(int reqCode, int kindOfDialog, String dialogText, String confirmButton, String cancelButton) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentYesNoDialog fragmentDeleteRegex =
                FragmentYesNoDialog.newInstance(reqCode, kindOfDialog, null, dialogText, confirmButton, cancelButton);
        fragmentDeleteRegex.show(fm, "fragment_dialog");
    }

}
