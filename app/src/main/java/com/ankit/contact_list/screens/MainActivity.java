package com.ankit.contact_list.screens;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ankit.contact_list.R;
import com.ankit.contact_list.models.PhoneContactDetails;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<PhoneContactDetails> phoneContacts = new ArrayList<>();
    List<PhoneContactDetails> filteredPhoneContacts = new ArrayList<>();
    ContentResolver cr;
    LinearLayout llProgressBar;
    RecyclerView recyclerViewDeviceContacts;
    ContactsAdapter adapter;
    ImageView imgClear;
    EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        llProgressBar = findViewById(R.id.llProgressBar);
        recyclerViewDeviceContacts = findViewById(R.id.recyclerViewDeviceContacts);
        imgClear = findViewById(R.id.imgClear);
        etSearch = findViewById(R.id.etSearch);

        recyclerViewDeviceContacts.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        recyclerViewDeviceContacts.setItemAnimator(new DefaultItemAnimator());

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            getContacts();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS}, 50);
        }

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filteredPhoneContacts.clear();
                if (etSearch.getText().toString().isEmpty()) {
                    imgClear.setVisibility(View.GONE);
                } else {
                    imgClear.setVisibility(View.VISIBLE);
                    for (int i = 0; i < phoneContacts.size(); i++) {
                        PhoneContactDetails data = phoneContacts.get(i);
                        if (!data.isHeader) {
                            if (data.name.toLowerCase().contains(etSearch.getText().toString().toLowerCase()) ||
                                    data.phone.toLowerCase().contains(etSearch.getText().toString().toLowerCase())) {
                                filteredPhoneContacts.add(data);
                            }
                        }
                    }
                    filteredPhoneContacts = addAlphabets(filteredPhoneContacts);
                }
                adapter = new ContactsAdapter(MainActivity.this, filteredPhoneContacts);
                recyclerViewDeviceContacts.setAdapter(adapter);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (etSearch.getText().toString().isEmpty()) {
                    loadPhoneContacts();
                }
            }
        });

        imgClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etSearch.setText("");
                etSearch.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
                loadPhoneContacts();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 50) {
            if (permissions[0].equals(Manifest.permission.READ_CONTACTS)
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    permissions[1].equals(Manifest.permission.WRITE_CONTACTS)
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                getContacts();
            }
        }
    }

    void getContacts() {
        cr = getContentResolver();
        llProgressBar.setVisibility(View.VISIBLE);

        Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone._ID,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER,
                        ContactsContract.CommonDataKinds.Phone.NUMBER},
                null, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER));
                if (hasPhone.equals("1")) {
                    phoneContacts.add(new PhoneContactDetails(name, phone, false));
                }
            } while (cursor.moveToNext());
            phoneContacts = addAlphabets(phoneContacts);
            loadPhoneContacts();
        }
        llProgressBar.setVisibility(View.GONE);
    }

    void loadPhoneContacts(){
        adapter = new ContactsAdapter(MainActivity.this, phoneContacts);
        recyclerViewDeviceContacts.setAdapter(adapter);
    }

    class ContactsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_VIEW = 1;
        private static final int TYPE_HEADER = 2;
        private List<PhoneContactDetails> listData;
        Activity context;

        public ContactsAdapter(Activity context, List<PhoneContactDetails> listData) {
            this.context = context;
            this.listData = listData;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_VIEW) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.phone_contacts_item_view, parent, false);
                return new VHView(itemView);
            } else {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.phone_alphabet_item_view, parent, false);
                return new VHHeader(itemView);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            PhoneContactDetails data = listData.get(position);

            if (holder instanceof VHHeader) {
                ((VHHeader) holder).tvAlphabet.setText(data.name);
            } else {
                ((VHView) holder).tvInitial.setText(getInitial(data.name));
                ((VHView) holder).tvName.setText(data.name);
                ((VHView) holder).tvPhone.setText(data.phone);

                ((VHView) holder).itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        Intent i = new Intent(MainActivity.this, EmergencyContactDetailActivity.class);
//                        i.putExtra("name", data.name);
//                        i.putExtra("phone", data.phone);
//                        i.putExtra("isEdit", false);
//                        startActivityForResult(i, 10);
                    }
                });
            }
        }

        private PhoneContactDetails getItem(int position) {
            return listData.get(position);
        }

        @Override
        public int getItemViewType(int position) {
            PhoneContactDetails dataItem = getItem(position);

            if (dataItem.isHeader) {
                return TYPE_HEADER;
            } else {
                return TYPE_VIEW;
            }

        }

        @Override
        public int getItemCount() {
            return listData.size();
        }

        public class VHView extends RecyclerView.ViewHolder {
            TextView tvInitial, tvName, tvPhone;

            public VHView(View view) {
                super(view);
                tvInitial = view.findViewById(R.id.tvInitial);
                tvName = view.findViewById(R.id.tvName);
                tvPhone = view.findViewById(R.id.tvPhone);
            }
        }

        public class VHHeader extends RecyclerView.ViewHolder {
            TextView tvAlphabet;

            public VHHeader(View view) {
                super(view);
                tvAlphabet = view.findViewById(R.id.tvAlphabet);
            }
        }
    }

    List<PhoneContactDetails> addAlphabets(List<PhoneContactDetails> list) {
        List<PhoneContactDetails> customList = new ArrayList<PhoneContactDetails>();
        if (list.size() > 0) {
            int i = 0;
            PhoneContactDetails firstMember = new PhoneContactDetails();
            firstMember.name = (String.valueOf(list.get(0).name.charAt(0)));
            firstMember.isHeader = true;
            customList.add(firstMember);
            for (i = 0; i < list.size() - 1; i++) {
                PhoneContactDetails teamMember = new PhoneContactDetails();
                char name1 = list.get(i).name.charAt(0);
                char name2 = list.get(i + 1).name.charAt(0);
                list.get(i).isHeader = false;
                customList.add(list.get(i));
                if (name1 != name2) {
                    teamMember.name = (String.valueOf(name2));
                    teamMember.isHeader = true;
                    customList.add(teamMember);
                }
            }
            list.get(i).isHeader = false;
            customList.add(list.get(i));
        }
        return customList;
    }

    public static String getInitial(String name) {
        StringBuilder initial = new StringBuilder();
        String[] names = name.split(" ");
        for (int i = 0; i < names.length; i++) {
            initial.append(names[i].charAt(0));
        }
        String i = initial.toString().toUpperCase();
        return i.length() > 3 ? i.substring(0, 2) : i;
    }
}