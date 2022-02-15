package com.example.filmreview.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.filmreview.R;
import com.example.filmreview.activity.DetailActivity;
import com.example.filmreview.adapter.MainAdapter;
import com.example.filmreview.model.MovieModel;
import com.example.filmreview.retrofit.Constant;
import com.example.filmreview.retrofit.MovieService;
import com.example.filmreview.retrofit.RetrofitInstance;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NowPlayingFragment extends Fragment {

    private String TAG = "NowPlayingFragment";
    private RecyclerView recyclerView;
    private ProgressBar progressBar, progressBarNextPage;
    private NestedScrollView scrollView;

    private MovieService service = RetrofitInstance.getUrl().create(MovieService.class);
    private RecyclerView.LayoutManager layoutManager;
    private MainAdapter adapter;
    private List<MovieModel.Results> movies = new ArrayList<>();

    private Boolean isScrolling = false;
    private int currentPage = 1;
    private int totalPages = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_now_playing, container, false);
        setupView(view);
        setupListener();
        setupRecyclerView();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getMovie();
        showLoadingNextPage(false);
    }

    private void setupView(View view){
        recyclerView = view.findViewById(R.id.list_movie);
        progressBar = view.findViewById(R.id.progress_loading);
        scrollView = view.findViewById(R.id.scroll_view);
        progressBarNextPage = view.findViewById(R.id.progress_loading_next_page);
    }

    private void setupListener(){
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
        adapter = new MainAdapter(movies, getContext(), new MainAdapter.AdapterListener() {
            @Override
            public void onClick()
            {
                startActivity(new Intent(getContext(), DetailActivity.class));
            }
        });

        layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void getMovie() {

        scrollView.scrollTo(0,0);
        currentPage = 1;
        showLoading(true);

        Call<MovieModel> call = call = service.getNowPlaying(Constant.API_KEY, Constant.LANGUAGE, String.valueOf(currentPage));
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

        Call<MovieModel> call = call = service.getNowPlaying(Constant.API_KEY, Constant.LANGUAGE, String.valueOf(currentPage));
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

    private void showMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }


}