package com.example.filmreview.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.example.filmreview.R;
import com.example.filmreview.adapter.TrailerAdapter;
import com.example.filmreview.model.TrailerModel;
import com.example.filmreview.retrofit.Constant;
import com.example.filmreview.retrofit.MovieService;
import com.example.filmreview.retrofit.RetrofitInstance;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrailerActivity extends AppCompatActivity {

    private String TAG = "TrailerAdapter";

    private RecyclerView list_video;
    private ProgressBar progress_loading;

    private MovieService service = RetrofitInstance.getUrl().create(MovieService.class);
    private RecyclerView.LayoutManager layoutManager;
    private TrailerAdapter adapter;
    private List<TrailerModel.Results> trailers = new ArrayList<>();

    private YouTubePlayerView youTubePlayerView;
    private YouTubePlayer player;
    private String youTubekey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trailer);

        setupView();
        setupListener();
        setupRecyclerView();

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Constant.MOVIE_ID != 0) {
            getTrailer();
        } else {
            finish();
        }
    }

    private void setupView() {

//        getSupportActionBar().setTitle(Constant.MOVIE_TITLE);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        youTubePlayerView = findViewById(R.id.youtube_player_view);
        list_video = findViewById(R.id.list_video);
        progress_loading = findViewById(R.id.progress_loading);
    }

    private void setupListener() {
        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                super.onReady(youTubePlayer);
                player = youTubePlayer;
                if (youTubekey != null) {
                    player.cueVideo(youTubekey, 0);
                }
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new TrailerAdapter(trailers, this, new TrailerAdapter.OnAdapterListener() {
            @Override
            public void OnClick(String key) {
                player.loadVideo(key, 0);
            }

            @Override
            public void OnLoadVideo(String key) {
                youTubekey = key;
            }
        });
        layoutManager = new LinearLayoutManager(this);
        list_video.setLayoutManager(layoutManager);
        list_video.setAdapter(adapter);
    }

    private void getTrailer(){
        showLoading(true);

        Call<TrailerModel> call = service.getTrailer(String.valueOf(Constant.MOVIE_ID),
                Constant.API_KEY);
        call.enqueue(new Callback<TrailerModel>() {
            @Override
            public void onResponse(Call<TrailerModel> call, Response<TrailerModel> response) {
                Log.d(TAG, response.toString());
                showLoading(false);
                if (response.isSuccessful()) {
                    TrailerModel trailer = response.body();
                    showTrailer(trailer);
                }
            }

            @Override
            public void onFailure(Call<TrailerModel> call, Throwable t) {
                Log.d(TAG, t.toString());
                showLoading(false);
            }
        });

    }

    private void showLoading(Boolean loading) {
        if (loading) {
            progress_loading.setVisibility(View.VISIBLE);
        } else {
            progress_loading.setVisibility(View.GONE);
        }
    }

    private void showTrailer(TrailerModel trailer) {
        trailers = trailer.getResults();
        adapter.setData(trailers);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

}