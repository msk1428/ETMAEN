package com.group7.etmaen.networking.generator;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.group7.etmaen.BuildConfig;

import java.io.IOException;
import java.text.DateFormat;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.group7.etmaen.utils.Constants.HEADER_NAME;

/**
 * Created by delaroy on 9/13/18.
 */

public class DataGenerator {
    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .readTimeout(90, TimeUnit.SECONDS)
                .connectTimeout(90, TimeUnit.SECONDS)
                .writeTimeout(90, TimeUnit.SECONDS)
                .cache(null);

    private static Gson gson = new GsonBuilder()
            .enableComplexMapKeySerialization()
            .serializeNulls()
            .setDateFormat(DateFormat.LONG)
            .setPrettyPrinting()
            .setVersion(1.0)
            .create();

    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create(gson));

    public static <S> S createService(Class<S> serviceClass, String baseUrl) {
        baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor()
                    .setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClient.addInterceptor(logging);
        }
        builder.client(httpClient.build());
        builder.baseUrl(baseUrl);
        Retrofit retrofit = builder.build();
        return  retrofit.create(serviceClass);
    }

    public static <S> S createService(Class<S> serviceClass, String apikey, String baseUrl) {
        baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";

        httpClient.addInterceptor(chain -> {
            Request original = chain.request();
            Request request = original.newBuilder()
                    .header(HEADER_NAME, apikey)
                    .method(original.method(), original.body())
                    .build();

            return chain.proceed(request);
        });

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor()
                    .setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClient.addInterceptor(logging);
        }
        builder.client(httpClient.build());
        builder.baseUrl(baseUrl);
        Retrofit retrofit = builder.build();
        return  retrofit.create(serviceClass);
    }
}
