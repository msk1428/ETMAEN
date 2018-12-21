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

import com.group7.etmaen.database.VerifiedEntry;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.group7.etmaen.utils.Constants.IMAGE;

public class VerifyAdapter extends RecyclerView.Adapter<VerifyAdapter.ClassifierViewHolder> {

    // Member variable to handle item clicks
    final private ItemClickListener mItemClickListener;
    private List<VerifiedEntry> mImageEntries;
    private Context mContext;

    public VerifyAdapter(Context context, ItemClickListener listener) {
        mContext = context;
        mItemClickListener = listener;
    }

    @Override
    public ClassifierViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.verify_face_items, parent, false);

        return new ClassifierViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ClassifierViewHolder holder, int position) {
        // Determine the values of the wanted data
        VerifiedEntry imageEntry = mImageEntries.get(position);
        String name = imageEntry.getName();
        String address = imageEntry.getAddress();
        String phonenumber = imageEntry.getPhonenumber();
        String images = imageEntry.getImage();

        //Set values
        holder.name.setText(name);
        holder.phonenumber.setText(phonenumber);
        holder.address.setText(address);

        Glide.with(mContext)
                .load(IMAGE + "uploads/" + images)
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

    public List<VerifiedEntry> getClassifier() {
        return mImageEntries;
    }


    public void setTasks(List<VerifiedEntry> imageEntries) {
        mImageEntries = imageEntries;
        notifyDataSetChanged();
    }

    public interface ItemClickListener {
        void onItemClickListener(int itemId);
    }

    // Inner class for creating ViewHolders
    public class ClassifierViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView name;
        TextView phonenumber;
        TextView address;
        CircleImageView imageView;
        public RelativeLayout viewForeground;

        public ClassifierViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            phonenumber = itemView.findViewById(R.id.phonenumber);
            address = itemView.findViewById(R.id.address);
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
