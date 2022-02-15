package com.example.filmreview.activity;

import android.content.Intent;
import android.os.Bundle;

import com.example.filmreview.R;
import com.example.filmreview.adapter.MainAdapter;
import com.example.filmreview.model.MovieModel;
import com.example.filmreview.retrofit.Constant;
import com.example.filmreview.retrofit.MovieService;
import com.example.filmreview.retrofit.RetrofitInstance;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModel;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filmreview.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private String TAG = "MainActivity";
    private MovieService service = RetrofitInstance.getUrl().create(MovieService.class);
    private RecyclerView recyclerView;
    private ProgressBar progressBar, progressBarNextPage;
    private NestedScrollView scrollView;


    private RecyclerView.LayoutManager layoutManager;
    private MainAdapter adapter;
    private List<MovieModel.Results> movies = new ArrayList<>();

    private String movieCategory;

    private Boolean isScrolling = false;
    private int currentPage = 1;
    private int totalPages = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupView();
        setupListener();
        setupRecyclerView();

    }


    @Override
    protected void onStart() {
        super.onStart();
        if (movieCategory == null) movieCategory = Constant.POPULAR;
        getMovie();
        showLoadingNextPage(false);
    }

    private void setupView() {
        recyclerView = findViewById(R.id.list_movie);
        progressBar = findViewById(R.id.progress_loading);
        scrollView = findViewById(R.id.scroll_view);
        progressBarNextPage = findViewById(R.id.progress_loading_next_page);
    }

    private void setupListener() {
        scrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                    if (!isScrolling){
                        if (currentPage <= totalPages){
                            getMovieNextPage();
                        }
                    }
                }
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new MainAdapter(movies, this, new MainAdapter.AdapterListener() {
            @Override
            public void onClick()
            {
                showMessage(Constant.MOVIE_TITLE);
                startActivity(new Intent(MainActivity.this, DetailActivity.class));
            }
        });

        layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void getMovie() {

        scrollView.scrollTo(0,0);
        currentPage = 1;
        showLoading(true);

        Call<MovieModel> call = null;

        switch (movieCategory) {
            case Constant.POPULAR:
                call = service.getPopular(Constant.API_KEY,
                        Constant.LANGUAGE, String.valueOf(currentPage));
                break;
            case Constant.NOW_PLAYING:
                call = service.getNowPlaying(Constant.API_KEY,
                        Constant.LANGUAGE, String.valueOf(currentPage));
                break;
        }

        call.enqueue(new Callback<MovieModel>() {
            @Override
            public void onResponse(Call<MovieModel> call, Response<MovieModel> response) {
                if (response.isSuccessful()) {

                    showLoading(false);

                    MovieModel movie = response.body();
//                    List<MovieModel.Results> results = movieModel.getResults();
//                    Log.d(TAG, results.toString());
                    showMovie(movie);
                }
            }

            @Override
            public void onFailure(Call<MovieModel> call, Throwable t) {

                    showLoading(false);

                    Log.d(TAG, t.toString());
            }
        });

    }

    private void getMovieNextPage() {

        currentPage += 1;
        showLoadingNextPage(true);

        Call<MovieModel> call = null;

        switch (movieCategory) {
            case Constant.POPULAR:
                call = service.getPopular(Constant.API_KEY, Constant.LANGUAGE, String.valueOf(currentPage));
                break;
            case Constant.NOW_PLAYING:
                call = service.getNowPlaying(Constant.API_KEY, Constant.LANGUAGE, String.valueOf(currentPage));
                break;
        }

        call.enqueue(new Callback<MovieModel>() {
            @Override
            public void onResponse(Call<MovieModel> call, Response<MovieModel> response) {
                if (response.isSuccessful()) {

                    showLoadingNextPage(false);

                    MovieModel movie = response.body();
//                    List<MovieModel.Results> results = movieModel.getResults();
//                    Log.d(TAG, results.toString());
                    showMovieNextPage(movie);
                }
            }

            @Override
            public void onFailure(Call<MovieModel> call, Throwable t) {

                showLoadingNextPage(false);

                Log.d(TAG, t.toString());
            }
        });

    }

    private void showLoading(Boolean loading) {
        if (loading) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void showLoadingNextPage(Boolean loading) {
        if (loading) {
            isScrolling = true;
            progressBarNextPage.setVisibility(View.VISIBLE);
        } else {
            isScrolling = false;
            progressBarNextPage.setVisibility(View.GONE);
        }
    }

    private void showMovie(MovieModel movie) {
        totalPages = movie.getTotal_pages();
        movies = movie.getResults();
        adapter.setData(movies);
    }

    private void showMovieNextPage(MovieModel movie) {
        totalPages = movie.getTotal_pages();
        movies = movie.getResults();
        adapter.setDataNextPage(movies);
        showMessage("Page " + currentPage + " loaded");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menuPopular) {
            getSupportActionBar().setTitle("POPULAR");
            showMessage("Popular");
            movieCategory = Constant.POPULAR;
            getMovie();
            return true;
        } else if (id == R.id.menuNowPlaying) {
            getSupportActionBar().setTitle("NOW PLAYING");
            showMessage("Now Playing");
            movieCategory = Constant.NOW_PLAYING;
            getMovie();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

}