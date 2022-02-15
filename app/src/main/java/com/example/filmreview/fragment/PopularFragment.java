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
import com.example.filmreview.activity.MainActivity;
import com.example.filmreview.adapter.MainAdapter;
import com.example.filmreview.databinding.FragmentPopularBinding;
import com.example.filmreview.model.MovieModel;
import com.example.filmreview.retrofit.Constant;
import com.example.filmreview.retrofit.MovieService;
import com.example.filmreview.retrofit.RetrofitInstance;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PopularFragment extends Fragment {

    private String TAG = "PopularFragment";

    private FragmentPopularBinding binding;

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
        binding = FragmentPopularBinding.inflate(inflater, container, false);
        setupListener();
        setupRecyclerView();
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        getMovie();
        showLoadingNextPage(false);
    }


    private void setupListener(){
        binding.scrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
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
        binding.listMovie.setLayoutManager(layoutManager);
        binding.listMovie.setAdapter(adapter);
    }

    private void getMovie() {

        binding.scrollView.scrollTo(0,0);
        currentPage = 1;
        showLoading(true);

        Call<MovieModel> call = call = service.getPopular(Constant.API_KEY, Constant.LANGUAGE, String.valueOf(currentPage));
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

        Call<MovieModel> call = call = service.getPopular(Constant.API_KEY, Constant.LANGUAGE, String.valueOf(currentPage));
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
            binding.progressLoading.setVisibility(View.VISIBLE);
        } else {
            binding.progressLoading.setVisibility(View.GONE);
        }
    }

    private void showLoadingNextPage(Boolean loading) {
        if (loading) {
            isScrolling = true;
            binding.progressLoadingNextPage.setVisibility(View.VISIBLE);
        } else {
            isScrolling = false;
            binding.progressLoadingNextPage.setVisibility(View.GONE);
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