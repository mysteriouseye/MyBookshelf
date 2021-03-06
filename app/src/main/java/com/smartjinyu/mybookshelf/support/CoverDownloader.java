package com.smartjinyu.mybookshelf.support;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.smartjinyu.mybookshelf.model.bean.Book;
import com.smartjinyu.mybookshelf.ui.book.BookEditActivity;
import com.smartjinyu.mybookshelf.util.AppUtil;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by smartjinyu on 2017/1/23.
 */

public class CoverDownloader {
    private static final String TAG = "CoverDownloader";

    private Handler mHandler;
    private Context mContext;
    private Book mBook;
    private int mode;

    /**
     * @param context
     * @param book
     * @param mode    = 0 Call from BookEditActivity, mode = 1 Call from BatchAddActivity
     */
    public CoverDownloader(Context context, Book book, int mode) {
        mBook = book;
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());
        this.mode = mode;
    }


    public void downloadAndSaveImg(String url, final String path) {
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://smartjinyu.com/")//no use here, will use dynamic url for request
                .build();
        downloadImgApi downImgApi = retrofit.create(downloadImgApi.class);
        Call<ResponseBody> call = downImgApi.downloadFileWithDynamicUrlSync(url);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.i(TAG, "Get download image response, code = " + response.code());
                if (AppUtil.saveImgToDisk(response.body(), path)) {
                    mBook.setHasCover(true);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mode == 0) {
                                ((BookEditActivity) mContext).setBookCover();
                            }
                            // nothing except setHasCover(true) need to do if mode == 1
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Fail to download image response," + t.toString());
                // Toast.makeText(mContext,"",Toast.LENGTH_LONG).show();
                //todo
            }
        });


    }

    private interface downloadImgApi {
        @GET
        Call<ResponseBody> downloadFileWithDynamicUrlSync(@Url String fileUrl);
    }
}
