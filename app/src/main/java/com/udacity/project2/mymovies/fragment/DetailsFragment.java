package com.udacity.project2.mymovies.fragment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.udacity.project2.mymovies.BuildConfig;
import com.udacity.project2.mymovies.R;
import com.udacity.project2.mymovies.adapter.RecyclerViewReviewAdapter;
import com.udacity.project2.mymovies.adapter.RecyclerViewTrailerAdapter;
import com.udacity.project2.mymovies.database.MoviesUtil;
import com.udacity.project2.mymovies.network.NetworkUtil;
import com.udacity.project2.mymovies.network.Url;
import com.udacity.project2.mymovies.parcelable.Movie;
import com.udacity.project2.mymovies.parcelable.MovieReview;
import com.udacity.project2.mymovies.parcelable.MovieTrailerResults;
import com.udacity.project2.mymovies.retrofit.ApiClient;
import com.udacity.project2.mymovies.retrofit.ApiInterface;
import com.udacity.project2.mymovies.retrofit.MovieReviewResponse;
import com.udacity.project2.mymovies.retrofit.MovieTrailerResponse;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.ContentValues.TAG;
import static com.udacity.project2.mymovies.database.MoviesUtil.FavouriteDelete;

/**
 * Created by Dell on 12/22/2016.
 */

public class DetailsFragment extends Fragment implements RecyclerViewTrailerAdapter.ClickListener, View.OnClickListener {
    public static final String ID = "ID";
    private static final String MOVIE_SHARE_HASHTAG = " #MyMovies";
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.rdate)
    TextView date;
    @BindView(R.id.rate)
    TextView rate;
    @BindView(R.id.overview)
    TextView overview;
    @BindView(R.id.imageView)
    ImageView img;
    @BindView(R.id.progressbar)
    ProgressBar progressBar;
    @BindView(R.id.progressContent)
    LinearLayout progressContent;
    @BindView(R.id.contentMain)
    LinearLayout contentMain;
    @BindView(R.id.movieReview)
    RecyclerView movieReviewView;
    @BindView(R.id.movieTrailer)
    RecyclerView movieTrailerView;
    @BindView(R.id.myFavorite)
    ImageView myFavorite;
    @BindView(R.id.error)
    LinearLayout errorLayout;
    @BindView(R.id.content)
    LinearLayout contLayout;
    String mtitle;
    String mPosterPath;
    String mPosterUrl;
    String mRating;
    String mDate;
    String mOverview;
    String id;
    View rootView;
    MoviesUtil moviesUtil;
    int n = -1;
    int i = 0;
    private ShareActionProvider mShareActionProvider;
    private Unbinder unbinder;
    private ArrayList<MovieTrailerResults> movieTrailerResults;
    private ArrayList<MovieReview> movieReviewResults;
    private RecyclerViewReviewAdapter reviewAdapter;
    private RecyclerViewTrailerAdapter trailerAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private Intent intent;

    public DetailsFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initalizeInt();
        if (NetworkUtil.isNetworkConnected(getActivity())) {
            if (savedInstanceState == null || !savedInstanceState.containsKey("trailers") ||
                    savedInstanceState.getParcelableArrayList("trailers") == null ||
                    !savedInstanceState.containsKey("reviews") ||
                    savedInstanceState.getParcelableArrayList("reviews") == null) {
                getTrailers(id);

            } else {
                movieTrailerResults = savedInstanceState.getParcelableArrayList("trailers");
                movieReviewResults = savedInstanceState.getParcelableArrayList("reviews");

            }
        }

    }

    /*@Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_details, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        contentMain.setVisibility(rootView.GONE);
        setLayoutManager(movieTrailerView);
        setLayoutManager(movieReviewView);
        moviesUtil = new MoviesUtil();
        if (movieTrailerResults != null && movieReviewResults != null) {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            setData(movieTrailerResults, movieReviewResults);

        } else {

            CheckNetworkDoStuffOnView();
        }
        return rootView;

    }

    //this is on tablet call
    private void setArgs() {
        if (getArguments() != null) {
            Bundle intent = getArguments();
            id = intent.getString("id");
            if(id==null) {
               initalizeInt();
            }else {
                mtitle = intent.getString("title");
                mRating = intent.getString("vote");
                mDate = intent.getString("date");
                mOverview = intent.getString("overview");
                mPosterPath = intent.getString("poster");
            }
        }

    }

    //this is on direct call
    private void initalizeInt() {
        intent = getActivity().getIntent();
        if (intent != null) {
            id = intent.getStringExtra("id");
            if (id == null) {
                setArgs();
            } else {
                mtitle = intent.getStringExtra("title");
                mRating = intent.getStringExtra("vote");
                mDate = intent.getStringExtra("date");
                mOverview = intent.getStringExtra("overview");
                mPosterPath = intent.getStringExtra("poster");
            }
        }
    }

    void setLayoutManager(RecyclerView view) {
        view.setHasFixedSize(true);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        view.setLayoutManager(mLinearLayoutManager);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("trailers", movieTrailerResults);
        outState.putParcelableArrayList("reviews", movieReviewResults);

        super.onSaveInstanceState(outState);
    }

    public void setData(ArrayList<MovieTrailerResults> movieTrailerResults, ArrayList<MovieReview> movieReviews) {
        if (movieTrailerResults != null && movieReviews != null) {
            trailerAdapter = new RecyclerViewTrailerAdapter(getContext(), R.layout.list_item_movie_trailer, movieTrailerResults);
            reviewAdapter = new RecyclerViewReviewAdapter(getContext(), R.layout.list_item_movie_trailer, movieReviews);
            if (trailerAdapter != null && reviewAdapter != null) {
                trailerAdapter.setClickListener(this);
                movieTrailerView.setAdapter(trailerAdapter);
                movieReviewView.setAdapter(reviewAdapter);
            }
            title.setText("" + mtitle);
            n = moviesUtil.CheckisFavourite(getActivity(), mtitle);
            if (n == 1) {
                n = 0;
                myFavorite.setImageResource(android.R.drawable.btn_star_big_on);
            } else {
                n = 1;
                myFavorite.setImageResource(android.R.drawable.btn_star_big_off);
            }
            mPosterUrl = Url.POSTER_URL + mPosterPath;
            Glide.with(getContext()).load(mPosterUrl)
                    .thumbnail(0.1f)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(img);
            rate.setText("Rating: " + mRating + "/10");
            date.setText("Release Date: " + mDate);
            overview.setText("Overview: " + mOverview);
            contentMain.setVisibility(rootView.VISIBLE);
            progressBar.setVisibility(rootView.GONE);
            myFavorite.setOnClickListener(this);
        }

    }


    //Retrofit getting Trailers
    public void getTrailers(final String id) {
        if (NetworkUtil.isNetworkConnected(getActivity())) {
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }
            ApiInterface apiService =
                    ApiClient.getClient().create(ApiInterface.class);

            Call<MovieTrailerResponse> call = null;

            call = apiService.getMovieTrailers(id, BuildConfig.THE_MOVIE_DB_API_KEY);

            call.enqueue(new Callback<MovieTrailerResponse>() {
                @Override
                public void onResponse(Call<MovieTrailerResponse> call, Response<MovieTrailerResponse> response) {
                    movieTrailerResults = (ArrayList<MovieTrailerResults>) response.body().getMovieTrailerResults();
                    Log.d("id..........", "" + movieTrailerResults);
                    getReviews(id, movieTrailerResults);
                }

                @Override
                public void onFailure(Call<MovieTrailerResponse> call, Throwable t) {
                    // Log error here since request failed
                    Log.e(TAG, t.toString());
                }
            });

        }
    }

    //Retrofit getting Reviews
    public void getReviews(String id, final ArrayList<MovieTrailerResults> movieTrailers) {
        if (NetworkUtil.isNetworkConnected(getActivity())) {

            ApiInterface apiService =
                    ApiClient.getClient().create(ApiInterface.class);

            Call<MovieReviewResponse> call = null;

            call = apiService.getMovieReviews(id, BuildConfig.THE_MOVIE_DB_API_KEY);

            call.enqueue(new Callback<MovieReviewResponse>() {
                @Override
                public void onResponse(Call<MovieReviewResponse> call, Response<MovieReviewResponse> response) {
                    movieReviewResults = (ArrayList<MovieReview>) response.body().getMovieReviews();
                    setData(movieTrailers, movieReviewResults);
                }

                @Override
                public void onFailure(Call<MovieReviewResponse> call, Throwable t) {
                    // Log error here since request failed
                    Log.e(TAG, t.toString());
                }
            });

        }

    }

    //on trailer click
    @Override
    public void itemClicked(View view, int position) {
        Toast.makeText(getContext(), "Hello Check Internet Connection and Try again...", Toast.LENGTH_SHORT);
        MovieTrailerResults movieTrailersClick = movieTrailerResults.get(position);
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + movieTrailersClick.getKey()));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v=" + movieTrailersClick.getKey()));
        try {
            startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            startActivity(webIntent);
        }
    }

    //on favorite click
    @Override
    public void onClick(View view) {
        if (n == 1) {
            n = 0;
            try {
                Movie movie;
                movie = new Movie(mPosterPath, false, mOverview, mDate, null, Integer.parseInt(id), null, null, mtitle, null, 0.0, 0, false, Double.valueOf(mRating));

                ArrayList<Movie> m = new ArrayList<Movie>();
                m.add(0, movie);
                moviesUtil.insertData(getActivity(), m, "favourite");
                myFavorite.setImageResource(android.R.drawable.btn_star_big_on);
                Toast.makeText(getContext(), "Movie Inserted in Favourite", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
            }
        } else {
            n = 1;
            myFavorite.setImageResource(android.R.drawable.btn_star_big_off);
            FavouriteDelete(getActivity(), mtitle);
            Toast.makeText(getActivity(), "Movie deleted from  favourite", Toast.LENGTH_SHORT).show();
        }
    }


    private void CheckNetworkDoStuffOnView() {
        if (NetworkUtil.isNetworkConnected(getActivity())) {
            //This is because internet connection goes down between activities
            if (errorLayout != null || progressBar != null || contLayout != null) {
                errorLayout.setVisibility(View.GONE);
                contLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
            }
        } else {
            if (errorLayout != null || contLayout != null || progressBar != null) {
                progressBar.setVisibility(View.GONE);
                errorLayout.setVisibility(View.VISIBLE);
                contLayout.setVisibility(View.GONE);

            }
        }
    }
}
