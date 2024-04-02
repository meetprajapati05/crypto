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
import com.example.majorproject.BackgroundServices.NewsApiService;
import com.example.majorproject.EditProfile;
import com.example.majorproject.History;
import com.example.majorproject.Models.UserModel;
import com.example.majorproject.R;
import com.example.majorproject.SingIn;
import com.example.majorproject.VerifyEmailPassword;
import com.example.majorproject.Wallet;
import com.example.majorproject.databinding.FragmentProfileBinding;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Objects;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.result.DeleteResult;
import io.realm.mongodb.mongo.result.InsertOneResult;

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

                                //remove services
                                Intent serviceIntent = new Intent(getContext(), NewsApiService.class);
                                getContext().stopService(serviceIntent);
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

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Delete Account");
                builder.setMessage("Deleting your account will delete your access and all your information on Coin Galaxy application. Are you sure you want to continue?");
                builder.setIcon(R.drawable.icon_delete);
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MongoCollection<Document> collectionUser = database.getCollection(getString(R.string.MONGO_DB_USER_COLLECTION));

                        collectionUser.findOne(new Document("_id", new ObjectId(userObjId))).getAsync(new App.Callback<Document>() {
                            @Override
                            public void onResult(App.Result<Document> result) {
                                if(result.get()!=null){
                                    UserModel userData = new UserModel();
                                    userData.set_id(result.get().getString("user_id"));
                                    userData.setName(result.get().getString("name"));
                                    userData.setEmail(result.get().getString("email"));
                                    userData.setMobile(result.get().getString("phone_no"));
                                    userData.setPassword(result.get().getString("password"));
                                    userData.setProvider(result.get().getString("provider"));
                                    userData.setImage(result.get().getString("img_url"));
                                    userData.setBalance(result.get().getDouble("balance"));

                                    if(userData!=null){
                                        collectionUser.deleteOne(new Document("_id", result.get().getObjectId("_id"))).getAsync(new App.Callback<DeleteResult>() {
                                            @Override
                                            public void onResult(App.Result<DeleteResult> result) {
                                                if(result.isSuccess()){
                                                    MongoCollection<Document> collectionDeleteUser = database.getCollection(getString(R.string.MONGO_DB_DELETE_USER_COLLECTION));

                                                    Document document = new Document("user_id", userData.get_id())
                                                            .append("name", userData.getName())
                                                            .append("email", userData.getEmail())
                                                            .append("phone_no", userData.getMobile())
                                                            .append("password", userData.getPassword())
                                                            .append("provider", userData.getProvider())
                                                            .append("img_url", userData.getImage())
                                                            .append("balance", userData.getBalance());

                                                    collectionDeleteUser.insertOne(document).getAsync(new App.Callback<InsertOneResult>() {
                                                        @Override
                                                        public void onResult(App.Result<InsertOneResult> result) {
                                                            if(result.isSuccess()){

                                                                new DeleteUserTask(context, app).execute();

                                                            }else{
                                                                Log.e("ErrInsertDeleteUser", result.getError().toString());
                                                            }
                                                        }
                                                    });
                                                }else{
                                                    Log.e("ErrDeleteUser", result.getError().toString());
                                                }
                                            }
                                        });
                                    }
                                }else{
                                    Log.e("ErrFindDeleteAccount", "User not find in database user collection.");
                                }
                            }
                        });
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
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
                Toast.makeText(getContext(), "Logout successful", Toast.LENGTH_SHORT).show();
            } else {
                // Handle failure or notify the user
                Toast.makeText(getContext(), "Logout failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class DeleteUserTask extends AsyncTask<Void, Void, Boolean> {
        private Context mContext;
        private App mApp;

        public DeleteUserTask(Context context, App app) {
            mContext = context;
            mApp = app;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                mApp.removeUser(mApp.currentUser());
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                SharedPreferences preferences = mContext.getSharedPreferences("MajorProject", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.clear(); // Clear all data from this SharedPreferences
                editor.apply();

                SharedPreferences preferencesWatchlist = mContext.getSharedPreferences("Watchlist", Context.MODE_PRIVATE);
                SharedPreferences.Editor editorWatchlist = preferencesWatchlist.edit();
                editorWatchlist.clear(); // Clear all data from this SharedPreferences
                editorWatchlist.apply();

                //pass intent on mainScreen
                Toast.makeText(mContext, "Your account has been deleted.", Toast.LENGTH_SHORT).show();
                Intent iSignIn = new Intent(mContext, SingIn.class);
                mContext.startActivity(iSignIn);
                requireActivity().finishAffinity();

                //remove services
                Intent serviceIntent = new Intent(getContext(), NewsApiService.class);
                getContext().stopService(serviceIntent);
            } else {
                // Failed to remove user
                System.err.println("Failed to remove user");
            }
        }
    }
}