package android.sead_systems.seads.page_management_main_menu.page_fragment_components;

import android.sead_systems.seads.R;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AdapterRecyclerViewDevices extends RecyclerView.Adapter<AdapterRecyclerViewDevices.ViewHolder> {
    private DeviceViewInfo[] mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public View mView;
        public ViewHolder(View v) {
            super(v);
            this.mView = v;
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            Log.d("Page", "onClick " + getLayoutPosition());
        }
    }

    /**
     *
     * @param dataset takes in a dataset to use to populate the reyclerview
     */
    public AdapterRecyclerViewDevices(DeviceViewInfo[] dataset) {
        mDataset = dataset;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.devices_item, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // On some viewholders we need a variable to indicate whether it's a ui element or a device
        ((TextView)holder.mView.findViewById(R.id.sample_text_view)).setText("--\n--");
        ((TextView)holder.mView.findViewById(R.id.text_view_device_name)).setText(
                mDataset[position].getDeviceName());
        ((TextView)holder.mView.findViewById(R.id.text_view_device_cost)).setText(
                mDataset[position].getCostInPastDay());
    }

    @Override
    public int getItemCount() {
        return mDataset.length;
    }


}
