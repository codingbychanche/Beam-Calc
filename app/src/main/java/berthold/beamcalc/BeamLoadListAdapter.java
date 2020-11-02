package berthold.beamcalc;

/*
 * BeamLoadListAdapter.java
 *
 * Created by Berthold Fritz
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License:
 * https://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 * Last modified 2/4/19 8:48 PM
 */

/**
 * Created by Berthold on 1/1/19.
 */

import android.graphics.Color;
import android.media.Image;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import org.berthold.beamCalc.*;


public class BeamLoadListAdapter extends RecyclerView.Adapter<BeamLoadListAdapter.MyViewHolder> {
    private List<Load> loadList=new ArrayList<>();
    private MainActivity main;

    // Provide a suitable constructor (depends on the kind of dataset)
    public BeamLoadListAdapter(List<Load> loadList, MainActivity main) {
        this.loadList=loadList;
        this.main=main;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View mView;
        public MyViewHolder(View v) {
            super(v);
            Log.v("Constructor","-");
            mView= v;
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public BeamLoadListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_list_row, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        Log.v("onCreateViewHolder","-");
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Log.v("onBindViewHolder",loadList.get(position).toString());

        TextView tv=(TextView)holder.mView.findViewById(R.id.my_text_View);
        final ImageButton deleteItem=(ImageButton)holder.mView.findViewById(R.id.delete_item);
        final CheckBox includeThisLoadIntoCalculation=(CheckBox)holder.mView.findViewById(R.id.include_this_load_in_calculation);
        final ImageView loadHasErrors=(ImageView)holder.mView.findViewById(R.id.load_row_with_errors);

        // Listen to click events
        // Delete row?
        deleteItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                main.itemInsideLoadListWasPressed(position, deleteItem.getId());
            }
        });

        includeThisLoadIntoCalculation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean includehisLoadIntoCalculation) {
                Log.v("Checked:"," Include checked for item "+includehisLoadIntoCalculation);
                loadList.get(position).setIncludeIntoCalculation(includehisLoadIntoCalculation);
                main.itemInsideLoadListWasPressed(position,includeThisLoadIntoCalculation.getId());
            }
        });

        // Show data
        String nameOfLoad=loadList.get(position).getName();
        double force_N=loadList.get(position).getForce_N();
        double xn_m=loadList.get(position).getDistanceFromLeftEndOfBeam_m();
        double lengthOfLineLoad_m_m=loadList.get(position).getLengthOfLineLoad_m();
        boolean includeLoad=loadList.get(position).getIncludeThisLoadIntoCalculation();
        boolean loadHasError=loadList.get(position).getError();

        String unit;
        if (lengthOfLineLoad_m_m==0)
            unit="N";
        else
            unit="N/m";

        tv.setText( nameOfLoad+"="+force_N+unit+"|"+
                    "x"+position+"="+xn_m+" m |"+
                    "l"+position+"="+lengthOfLineLoad_m_m+" m" );
        if(includeLoad)
            includeThisLoadIntoCalculation.setChecked(true);
        else
            includeThisLoadIntoCalculation.setChecked(false);

        /**
         * todo: Marking works, unmarking not
         * Full description and possible source of error see: {@link MainActivity}
         */
        if (loadHasError) {
            //holder.mView.setBackgroundColor(Color.RED);
            //loadHasErrors.setVisibility(View.VISIBLE);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if(loadList!=null)
            return loadList.size();
        return 0;
    }
}