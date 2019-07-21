package com.example.gpstracker.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gpstracker.Model.User;
import com.example.gpstracker.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
/**
 * Created by Uri Robinov on 20/7/2019.
 */
public class CircleAdapter extends RecyclerView.Adapter<CircleAdapter.CircleViewHolder> {
    ArrayList<User> userArrayList;
    OnCardClickedListener cardClickedListener;

    public CircleAdapter(ArrayList<User> usersList, OnCardClickedListener onCardClickedListener) {
        this.userArrayList = usersList;
        this.cardClickedListener = onCardClickedListener;
    }

    @NonNull
    @Override
    public CircleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout,parent,false);
       CircleViewHolder circleViewHolder = new CircleViewHolder(v,cardClickedListener);
       return circleViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CircleViewHolder holder, final int position) {

        final User curUser = userArrayList.get(position);
        holder.title.setText(curUser.getName());
        Picasso.get()
                .load(curUser.getImage())
                .resize(100, 100)
                .centerCrop()
                .into(holder.userImage);
        holder.batteryLevel.setText(curUser.getBattery()+"%");
        if (curUser.getIsSharing().equals("false")){
            Picasso
                    .get()
                    .load(R.drawable.not_sharing)
                    .into(holder.sharingIcon);

        }else {
            Picasso
                    .get()
                    .load(R.drawable.sharing)
                    .into(holder.sharingIcon);
        }

    }

    @Override
    public int getItemCount() {
        return userArrayList.size();
    }

    public class CircleViewHolder  extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView title, batteryLevel;
        ImageView userImage, sharingIcon;
        FirebaseAuth auth;
        FirebaseUser user;
        OnCardClickedListener listener;

        public CircleViewHolder(@NonNull final View itemView, OnCardClickedListener listener) {
            super(itemView);

            auth = FirebaseAuth.getInstance();
            user= auth.getCurrentUser();

            batteryLevel = itemView.findViewById(R.id.battery_text);
            sharingIcon = itemView.findViewById(R.id.sharing_image);
            title = itemView.findViewById(R.id.text_title);
            userImage = itemView.findViewById(R.id.userImage);
            this.listener = listener;
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            listener.onCardClick(getAdapterPosition());
        }
    }
    public interface  OnCardClickedListener{
        void onCardClick(int Pos);
    }
}
