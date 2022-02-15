package com.example.filmreview.activity;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.filmreview.R;
import com.example.filmreview.databinding.ActivityDetailBinding;
import com.example.filmreview.databinding.ContentDetailBinding;
import com.example.filmreview.model.DetailModel;
import com.example.filmreview.retrofit.Constant;
import com.example.filmreview.retrofit.MovieService;
import com.example.filmreview.retrofit.RetrofitInstance;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity {

    private ActivityDetailBinding binding;
    private ContentDetailBinding bindingContent;

    private MovieService service = RetrofitInstance.getUrl().create(MovieService.class);

    public String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        bindingContent = binding.contentDetail;
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        setupView();
        setupListener();

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Constant.MOVIE_ID != 0) {
            getDetailMovie();
        } else {
            finish();
        }
    }

    private void setupView(){

        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void setupListener() {
        bindingContent.fabPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DetailActivity.this, TrailerActivity.class));
            }
        });
    }

    private void showLoading(Boolean loading) {
        if (loading) {
            bindingContent.progressLoading.setVisibility(View.VISIBLE);
        } else {
            bindingContent.progressLoading.setVisibility(View.GONE);
        }
    }

    private void getDetailMovie(){

        showLoading(true);

        Call<DetailModel> call = service.getDetail(String.valueOf(Constant.MOVIE_ID),
                Constant.API_KEY);
        call.enqueue(new Callback<DetailModel>() {
            @Override
            public void onResponse(Call<DetailModel> call, Response<DetailModel> response) {
                Log.d(TAG, response.toString());
                showLoading(false);
                if(response.isSuccessful()){
                    DetailModel detail = response.body();
                    showMoviel(detail);
                }
            }

            @Override
            public void onFailure(Call<DetailModel> call, Throwable t) {

            }
        });
    }

    private void showMoviel(DetailModel detail) {
        bindingContent.textTitle.setText(detail.getTitle());
        bindingContent.textVote.setText(detail.getVote_average().toString());
        bindingContent.textOverview.setText(detail.getOverview());

        for (DetailModel.Genres genre: detail.getGenres()) {
            bindingContent.textGenre.setText(genre.getName() + " ");
        }

        Picasso.get()
                .load(Constant.BACKDROP_PATH + detail.getBackdrop_path())
                .placeholder(R.drawable.placeholder_landscape)
                .error(R.drawable.placeholder_landscape)
                .fit().centerCrop()
                .into(binding.imgBackdrop);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}