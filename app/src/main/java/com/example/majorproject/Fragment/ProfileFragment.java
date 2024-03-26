package com.example.majorproject.Fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.amulyakhare.textdrawable.TextDrawable;
import com.bumptech.glide.Glide;
import com.example.majorproject.EditProfile;
import com.example.majorproject.History;
import com.example.majorproject.R;
import com.example.majorproject.SingIn;
import com.example.majorproject.VerifyEmailPassword;
import com.example.majorproject.Wallet;
import com.example.majorproject.databinding.FragmentProfileBinding;

import org.bson.Document;

import java.util.Objects;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;

public class ProfileFragment extends Fragment {

   FragmentProfileBinding binding;
   String email;
   String userObjId;
    App app;
    MongoClient client;
    MongoDatabase database;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        Realm.init(getContext());
        app = new App(new AppConfiguration.Builder(getContext().getString(R.string.MONGO_APP_ID)).build());
        client = app.currentUser().getMongoClient(getContext().getString(R.string.MONGO_DB_SERVICE_NAME));
        database = client.getDatabase(getContext().getString(R.string.MONGO_DATABASE_NAME));

        SharedPreferences preferences = getActivity().getSharedPreferences("MajorProject", Context.MODE_PRIVATE);
        email = preferences.getString("email",null);

        setProfile(getContext());

        //set Edit Profile Button
        binding.btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), EditProfile.class);
                intent.putExtra("_id", userObjId);
                startActivity(intent);
            }
        });

        //set Logout button
        binding.btnProfileLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setCancelable(true)
                        .setIcon(R.drawable.icon_logout)
                        .setTitle("Logout")
                        .setMessage("Are you confirm to logout?")
                        .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //logout of mongoauth auth
                                new LogoutTask().execute();

                                SharedPreferences preferences = getContext().getSharedPreferences("MajorProject", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.clear(); // Clear all data from this SharedPreferences
                                editor.apply();

                                SharedPreferences preferencesWatchlist = getContext().getSharedPreferences("Watchlist", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editorWatchlist = preferencesWatchlist.edit();
                                editorWatchlist.clear(); // Clear all data from this SharedPreferences
                                editorWatchlist.apply();

                                //passintent on mainScreen
                                Intent iSignIn = new Intent(getContext(), SingIn.class);
                                startActivity(iSignIn);
                                requireActivity().finishAffinity();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        //History Button
        binding.btnProfileHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(),History.class);
                intent.putExtra("_id", userObjId);
                startActivity(intent);
            }
        });

        return binding.getRoot();
    }

    private void setProfile(Context context) {
        MongoCollection<Document> collection = database.getCollection(context.getString(R.string.MONGO_DB_USER_COLLECTION));

        Document filter = new Document("user_id", app.currentUser().getId()).append("email", email);
        collection.findOne(filter).getAsync(new App.Callback<Document>() {
            @Override
            public void onResult(App.Result<Document> result) {
                if(result.isSuccess()){
                    if(result.get()!=null){
                        String img = result.get().getString("img_url");
                        String user_name = result.get().getString("name");
                        userObjId = result.get().getObjectId("_id").toString();
                        if (img!=null){
                            Glide.with(context)
                                    .load(img)
                                    .into(binding.imgUserProfile);
                        }else{
                            TextDrawable firstLetterImage = TextDrawable.builder()
                                    .beginConfig()
                                    .width(60)
                                    .height(60)
                                    .endConfig()
                                    .buildRect(String.valueOf(user_name.toUpperCase().charAt(0)), context.getColor(R.color.light_green));
                            Objects.requireNonNull(binding.imgUserProfile).setImageDrawable(firstLetterImage);
                        }
                        binding.txtUserProfileEmail.setText(email);
                        binding.txtUserProfileName.setText(user_name);
                    }
                }else{
                    Log.e("ErrGetProfileData", result.getError().toString());
                }
            }
        });

        //Wallet button
        binding.btnProfileWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), Wallet.class);
                intent.putExtra("_id", userObjId);
                startActivity(intent);
            }
        });

        //Delete account button
        binding.btnProfileDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        //Contect Support
        binding.btnProfileContectSupport.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("QueryPermissionsNeeded")
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // Use this line to set the mailto: data scheme
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"developerpandav16@gmail.com"});

                Intent chooserIntent = Intent.createChooser(intent, "Share email");

                startActivity(chooserIntent);

            }
        });

        //Change Password button
        binding.btnProfileChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), VerifyEmailPassword.class).putExtra("_id", userObjId));
            }
        });
    }

    //Log out on background task
    private class LogoutTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                // Move your logout logic here
                app.currentUser().logOut();
                return true; // Indicates success
            } catch (Exception e) {
                e.printStackTrace();
                return false; // Indicates failure
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            // This method is called on the UI thread after doInBackground finishes
            if (success) {
                // Handle UI updates or post-logout actions here
                Toast.makeText(requireContext(), "Logout successful", Toast.LENGTH_SHORT).show();
            } else {
                // Handle failure or notify the user
                Toast.makeText(requireContext(), "Logout failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}