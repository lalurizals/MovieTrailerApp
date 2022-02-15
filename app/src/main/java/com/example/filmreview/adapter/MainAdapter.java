package com.example.filmreview.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filmreview.model.MovieModel;
import com.example.filmreview.R;
import com.example.filmreview.retrofit.Constant;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {

    private String TAG = "MainActivity";

    List<MovieModel.Results> results;
    private Context context;
    private AdapterListener listener;

    public MainAdapter(List<MovieModel.Results> results, Context context, AdapterListener listener) {
        this.results = results;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_movie,
                        parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MovieModel.Results result = results.get(position);

        holder.text_title.setText(result.getTitle());
        Picasso.get()
                .load(Constant.POSTER_PATH + result.getPoster_path())
                .placeholder(R.drawable.placeholder_portrait)
                .error(R.drawable.placeholder_portrait)
                .into(holder.image_poster);
//        Log.d(TAG, "backdrop: " + Constant.BACKDROP_PATH + result.getBackdrop_path());
//        Log.d(TAG, "poster: " + Constant.POSTER_PATH + result.getPoster_path());

        holder.image_poster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Constant.MOVIE_ID = result.getId();
                Constant.MOVIE_TITLE = result.getTitle();

                listener.onClick();
            }
        });

    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView image_poster;
        TextView text_title;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image_poster = itemView.findViewById(R.id.imgPoster);
            text_title = itemView.findViewById(R.id.textTittle);

        }

    }

    public void setData(List<MovieModel.Results> newResult) {
        results.clear();
        results.addAll( newResult );
        notifyDataSetChanged();
    }

    public void setDataNextPage(List<MovieModel.Results> newResult) {
        results.addAll( newResult );
        notifyDataSetChanged();
    }

    public interface AdapterListener {
        void onClick();
    }

}
