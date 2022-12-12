package com.example.preferencesdatastorejava;

import static com.example.preferencesdatastorejava.Object.EXAMPLE_COUNTER;

import androidx.appcompat.app.AppCompatActivity;
import androidx.datastore.core.DataStore;
import androidx.datastore.core.MultiProcessDataStoreFactory;
import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava2.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava2.RxDataStore;

import android.os.Bundle;
import android.util.Log;

import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Flowable;
import io.reactivex.FlowableSubscriber;
import io.reactivex.Single;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import rx.android.schedulers.AndroidSchedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    //creating preference data store
    RxDataStore<Preferences> dataStore =
            new RxPreferenceDataStoreBuilder(this, "settings").build();

//    DataStore<Integer> settings = MultiProcessDataStoreFactory.INSTANCE.create()

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        writePrefDataStore();
        readPrefDataStore();
    }

    private void writePrefDataStore() {

        Single<Preferences> updateResult = dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
            Integer currentInt = prefsIn.get(EXAMPLE_COUNTER);
            mutablePreferences.set(EXAMPLE_COUNTER, currentInt != null ? 200 : -1);
            Log.i(TAG, "stored 200");
            return Single.just(mutablePreferences);
        });
    }


    public void readPrefDataStore() {
        Flowable<Integer> exampleCounterFlow =
                dataStore.data().map(prefs -> prefs.get(EXAMPLE_COUNTER));

        Log.i(TAG, "readValue " + EXAMPLE_COUNTER + " -> " + exampleCounterFlow.subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                Log.i(TAG, integer.toString());
            }
        }));

        int collected = -100;

        Log.i(TAG, "readValue " + EXAMPLE_COUNTER + " -> " + exampleCounterFlow);
    }


    /**
     * Subscribing an observer to an int value in the DataStore which is associated to some key,
     * The subscription submits any change to the value
     *
     * @param key:      The key associated to the value need to be stored
     * @param listener: The value is returned in a worker thread, and returned to the
     *                  caller using a listener pattern
     */
    public void observeInt(Preferences.Key<Integer> key, IntListener listener) {
        Flowable<Integer> flowable =
                dataStore.data().map(prefs -> prefs.get(key));

        //https://proandroiddev.com/understanding-rxjava-subscribeon-and-observeon-744b0c6a41ea
        flowable.subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation()) // AndroidSchedulers requires ` implementation "io.reactivex.rxjava3:rxandroid:3.0.0" `
                .subscribe(new FlowableSubscriber<Integer>() {

                    @Override
                    public void onSubscribe(@NonNull Subscription s) {
                        s.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(Integer value) {
                        listener.intValue(value);
                    }

                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    interface IntListener {
        void intValue(int value);
    }

}

class Object {

    public static Preferences.Key<Integer> EXAMPLE_COUNTER = PreferencesKeys.intKey("example_counter");
}

