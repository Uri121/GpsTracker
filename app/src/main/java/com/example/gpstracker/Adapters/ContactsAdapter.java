package com.example.gpstracker.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gpstracker.Model.SosPhoneContact;
import com.example.gpstracker.R;

import java.util.ArrayList;
/**
 * Created by Uri Robinov on 20/7/2019.
 */
public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder> {

    ArrayList<SosPhoneContact> contactsList;

    public ContactsAdapter (ArrayList<SosPhoneContact> list){
        contactsList = list;

    }
    @NonNull
    @Override
    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.contacts_card,parent,false);
        ContactsAdapter.ContactsViewHolder contactsViewHolder = new ContactsViewHolder(v);
        return contactsViewHolder;
    }
    public void RemoveItem(int pos)
    {
        contactsList.remove(pos);
        notifyItemRemoved(pos);
        notifyItemRangeChanged(pos,contactsList.size());
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsViewHolder holder, int position) {
        final SosPhoneContact contact = contactsList.get(position);
        holder.name.setText(contact.getName());
        holder.phone.setText(contact.getPhone());

    }

    @Override
    public int getItemCount() {
        return contactsList.size();
    }

    public class ContactsViewHolder extends RecyclerView.ViewHolder{

        TextView name, phone;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.contact_name);
            phone = itemView.findViewById(R.id.contact_phone);

        }
    }
}
