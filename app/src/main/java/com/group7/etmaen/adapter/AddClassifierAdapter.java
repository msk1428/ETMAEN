package com.group7.etmaen.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.group7.etmaen.R;
import com.group7.etmaen.database.AddEntry;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddClassifierAdapter extends RecyclerView.Adapter<AddClassifierAdapter.ClassifierViewHolder> {

    // Member variable to handle item clicks
    final private ItemClickListener mItemClickListener;
    private List<AddEntry> mImageEntries;
    private Context mContext;

    public AddClassifierAdapter(Context context, ItemClickListener listener) {
        mContext = context;
        mItemClickListener = listener;
    }

    @Override
    public ClassifierViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.add_classification_item, parent, false);

        return new ClassifierViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ClassifierViewHolder holder, int position) {
        // Determine the values of the wanted data
        AddEntry imageEntry = mImageEntries.get(position);
        String name = imageEntry.getName();
        String phonenumber = imageEntry.getPhonenumber();
        String images = imageEntry.getImage();

        //Set values
        holder.name.setText(name);
        holder.particulars.setText(phonenumber + "");

        Glide.with(mContext)
                .load(images)
                .into(holder.imageView);
    }

    /**
     * Returns the number of items to display.
     */
    @Override
    public int getItemCount() {
        if (mImageEntries == null) {
            return 0;
        }
        return mImageEntries.size();
    }

    public List<AddEntry> getClassifier() {
        return mImageEntries;
    }


    public void setTasks(List<AddEntry> imageEntries) {
        mImageEntries = imageEntries;
        notifyDataSetChanged();
    }

    public interface ItemClickListener {
        void onItemClickListener(int itemId);
    }

    // Inner class for creating ViewHolders
    public class ClassifierViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView name;
        TextView particulars;
        TextView status;
        CircleImageView imageView;
        public RelativeLayout viewForeground;

        public ClassifierViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            particulars = itemView.findViewById(R.id.particulars);
            status = itemView.findViewById(R.id.status);
            imageView = itemView.findViewById(R.id.image);
            viewForeground = itemView.findViewById(R.id.view_foreground);
            itemView.setOnClickListener(this);
            imageView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int elementId = mImageEntries.get(getAdapterPosition()).getId();
            mItemClickListener.onItemClickListener(elementId);
        }
    }
}
