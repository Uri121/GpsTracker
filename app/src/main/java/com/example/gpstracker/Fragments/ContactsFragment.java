package com.example.gpstracker.Fragments;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.gpstracker.Adapters.ContactsAdapter;
import com.example.gpstracker.Model.SosPhoneContact;
import com.example.gpstracker.R;
import com.example.gpstracker.Sqlite.DbHelper;

import java.util.ArrayList;
/**
 * Created by Uri Robinov on 20/7/2019.
 */
public class ContactsFragment extends DialogFragment {


    ArrayList<SosPhoneContact> contacts;
    RecyclerView recyclerView;
    ContactsAdapter adapter;
    RecyclerView.LayoutManager layoutManager;
    ColorDrawable swipeColorBackground;
    Drawable deleteIcon;

    public ContactsFragment(ArrayList<SosPhoneContact>list){
        this.contacts = list;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        deleteIcon = ContextCompat.getDrawable(getActivity(),R.drawable.delete);
        swipeColorBackground = new ColorDrawable(Color.parseColor("#AA281D"));
        View v = inflater.inflate(R.layout.show_contact_sos,container);
        recyclerView = v.findViewById(R.id.contacts_recycler);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        adapter = new ContactsAdapter(contacts);
        recyclerView.setAdapter(adapter);
        this.getDialog().setTitle("Contacts List");

        //Swipe left to delete item from cart
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            //draw the red background after swiping
            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                View view = viewHolder.itemView;
                int iconMargin = (view.getHeight()- deleteIcon.getIntrinsicHeight()) / 2;

                if (dX > 0)
                {
                    swipeColorBackground.setBounds(view.getLeft(), view.getTop(),(int)dX, view.getBottom());
                    deleteIcon.setBounds(view.getLeft()+iconMargin,view.getTop()+iconMargin,view.getLeft()+iconMargin+deleteIcon.getIntrinsicHeight(),
                            view.getBottom()-iconMargin);
                }
                else {
                    swipeColorBackground.setBounds(view.getRight()+(int)dX, view.getTop(),view.getRight(),view.getBottom());
                    deleteIcon.setBounds(view.getRight() - iconMargin - deleteIcon.getIntrinsicHeight(),view.getTop() + iconMargin,view.getRight() - iconMargin,
                            view.getBottom() - iconMargin);
                }

                swipeColorBackground.draw(c);
                c.save();
                if (dX > 0)
                {
                    c.clipRect(view.getLeft(), view.getTop(),(int)dX, view.getBottom());
                }
                else {
                    c.clipRect(view.getRight() + (int)dX, view.getTop(), view.getRight(), view.getBottom());
                }
                deleteIcon.draw(c);
                c.restore();

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

                int deletedIndex = viewHolder.getAdapterPosition();
                String id = contacts.get(deletedIndex).getId();
                DbHelper.getInstance(getActivity()).DeleteItem(id);
                adapter.RemoveItem(deletedIndex);
            }
        }).attachToRecyclerView(recyclerView);

        return v;
    }
}
