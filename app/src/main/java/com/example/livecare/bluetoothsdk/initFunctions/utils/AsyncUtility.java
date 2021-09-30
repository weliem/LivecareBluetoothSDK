package com.example.livecare.bluetoothsdk.initFunctions.utils;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutionException;

public class AsyncUtility {

    public interface Block {
        void execute();
    }

    public interface ObjectBlock<T> {
        T execute();
    }

    public static void doOnMainThread(final Block block) {
        if (block != null) {
            Handler mainHandler = new Handler(Looper.getMainLooper());
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    block.execute();
                } // This is your code
            };
            mainHandler.post(myRunnable);
        }
    }

    public static void doOnBackgroundThread(final Block block) {
        new BackgroundAsyncTask(block).execute();
    }

    public static Object doOnBackgroundThread(final ObjectBlock<?> block) {
        try {
            return new BackgroundAsyncTaskObject(block).execute().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static class BackgroundAsyncTask extends AsyncTask<Void, Void, Void> {

        private Block block;

        BackgroundAsyncTask(Block block) {
            this.block = block;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            block.execute();
            return null;
        }
    }

    private static class BackgroundAsyncTaskObject extends AsyncTask<Void, Void, Object> {

        private ObjectBlock block;

        BackgroundAsyncTaskObject(ObjectBlock block) {
            this.block = block;
        }

        @Override
        protected Object doInBackground(Void... voids) {
            return block.execute();
        }
    }

}
