package com.splitwisr.data.balances;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.splitwisr.data.ApiService;
import com.splitwisr.data.AppDatabase;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BalanceRepository {

    private BalanceDao balanceDao;
    private LiveData<List<Balance>> allBalances;
    private final ApiService apiService;
    private String currentUserEmail;

    // TODO: Not good practice to be coupled to application like this
    public BalanceRepository(Application application, String currentUserEmail) {
        AppDatabase db = AppDatabase.getDatabase(application);
        balanceDao = db.balanceDao();
        allBalances = balanceDao.getAll();
        this.currentUserEmail = currentUserEmail;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://ece452project.herokuapp.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    public LiveData<List<Balance>> getAllBalances() {
        getLatestBalances();
        return allBalances;
    }

    public void insert(Balance balance) {
        StringBuilder email_a = new StringBuilder();
        StringBuilder email_b = new StringBuilder();

        if (!(balance.aEmail.compareTo(balance.bEmail) < 0)) {
            String t = balance.aEmail;
            balance.aEmail = balance.bEmail;
            balance.bEmail = t;
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            balanceDao.insertAll(balance);
        });
        sendBalance(balance.aEmail, balance.bEmail, balance.totalOwing);
    }

    public void upsert(final double totalOwing, final String aEmail, final String bEmail) {
        StringBuilder email_a = new StringBuilder();
        StringBuilder email_b = new StringBuilder();

        if (aEmail.compareTo(bEmail) < 0) {
            email_a.append(aEmail);
            email_b.append(bEmail);
        } else {
            email_a.append(bEmail);
            email_b.append(aEmail);
        }
        AppDatabase.databaseWriteExecutor.execute(() -> {
            balanceDao.upsert(totalOwing, email_a.toString(), email_b.toString());
        });
        sendBalance(email_a.toString(), email_b.toString(), totalOwing);
    }

    public Balance get(final String s1, final String s2) {
        StringBuilder email_a = new StringBuilder();
        StringBuilder email_b = new StringBuilder();

        if (s1.compareTo(s2) < 0) {
            email_a.append(s1);
            email_b.append(s2);
        } else {
            email_a.append(s2);
            email_b.append(s1);
        }
        return balanceDao.get(email_a.toString(), email_b.toString());
    }

    /** API CALLS **/
    public void getLatestBalances() {
        List<Balance> balances = balanceDao.getAllBlocking();
        if (balances != null) {
            apiService.readBalance(currentUserEmail).enqueue(new Callback<JsonArray>() {
                @Override
                public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                    if (response.isSuccessful()) {
                        for (JsonElement obj : response.body()) {
                            String aEmail = ((JsonObject) obj).get("payer").getAsString();
                            String bEmail = ((JsonObject) obj).get("payee").getAsString();
                            Double totalOwing = ((JsonObject) obj).get("balance").getAsDouble();
                            if (balanceDao.get(aEmail, bEmail) == null) {
                                balanceDao.insertAll(new Balance(aEmail, bEmail, totalOwing));
                            } else {
                                balanceDao.update(totalOwing, aEmail, bEmail);
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<JsonArray> call, Throwable t) {
                    Log.e("ApiService", t.toString());
                }
            });

        }
    }

    private void sendBalance(String aEmail, String bEmail, Double totalOwing) {
        apiService.writeBalance(aEmail, bEmail, totalOwing).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d("ApiService", "Submit Successful");
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.d("ApiService", "Submit Unsuccessful");
            }
        });
    }
}
