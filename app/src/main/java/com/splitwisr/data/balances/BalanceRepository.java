package com.splitwisr.data.balances;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

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

    // TODO: Not good practice to be coupled to application like this
    public BalanceRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        balanceDao = db.balanceDao();
        allBalances = balanceDao.getAll();

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

    public void update(final double totalOwing, final String aEmail, final String bEmail) {
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
            balanceDao.update(totalOwing, email_a.toString(), email_b.toString());
        });
        sendBalance(email_a.toString(), email_b.toString(), totalOwing);
    }

    public List<Balance> get(final String s1, final String s2) {
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

    private void getLatestBalances() {
        List<Balance> balances = balanceDao.getAllBlocking();
        if (balances != null) {
            for (Balance balance: balances) {
                apiService.readBalance(balance.aEmail, balance.bEmail).enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        Double serverBalance = response.body().get(("balance")).getAsDouble();
                        if (serverBalance != balance.totalOwing) {
                            update(serverBalance, balance.aEmail, balance.bEmail);
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        Log.e("ApiService", t.toString());
                    }
                });
            }
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
