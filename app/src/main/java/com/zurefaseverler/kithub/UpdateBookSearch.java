package com.zurefaseverler.kithub;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.POST;
import retrofit2.http.FormUrlEncoded;

import static com.zurefaseverler.kithub.StartUp.HOST;

public class UpdateBookSearch extends AppCompatActivity {

    static class SearchResults {

        @SerializedName("id")
        private String id;
        @SerializedName("title")
        private String title;
        @SerializedName("first_name")
        private String first_name;
        @SerializedName("last_name")
        private String last_name;
        @SerializedName("price")
        private String price;

        public SearchResults(String id, String title, String first_name, String last_name,
                             String price) {
            this.id = id;
            this.title = title;
            this.first_name = first_name;
            this.last_name = last_name;
            this.price = price;
        }

        public String getId() {
            return id;
        }
        public String getTitle() {
            return title;
        }
        String getFirst_name() {
            return first_name;
        }
        String getLast_name() {
            return last_name;
        }
        public String getPrice() {
            return price;
        }

        @NonNull
        @Override
        public String toString() {
            return getTitle();
        }
    }

    interface MyAPIService {
        @FormUrlEncoded
        @POST("search_books.php")
        Call<List<SearchResults>> searchKitHub(@Field("query") String query);
    }

    static class RetrofitClientInstance {
        private static Retrofit retrofit;

        static Retrofit getRetrofitInstance() {
            if (retrofit == null) {
                retrofit = new Retrofit.Builder()
                        .baseUrl(HOST)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            }
            return retrofit;
        }
    }
    static class ListViewAdapter extends BaseAdapter {

        private List<SearchResults> books;
        private Context context;

        ListViewAdapter(Context context, List<SearchResults> books) {
            this.context = context;
            this.books = books;
        }

        @Override
        public int getCount() {
            return books.size();
        }

        @Override
        public Object getItem(int pos) {
            return books.get(pos);
        }

        @Override
        public long getItemId(int pos) {
            return pos;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(context).inflate(R.layout.activity_search_model,
                        viewGroup, false);
            }

            TextView nameTxt = view.findViewById(R.id.nameTextView);
            TextView txtAuthor = view.findViewById(R.id.authorTextView);

            final SearchResults thisBook = books.get(position);

            nameTxt.setText(thisBook.getTitle());
            txtAuthor.setText(thisBook.getFirst_name() + " " + thisBook.getLast_name());

            return view;
        }
    }

    private GridView mGridView;
    private ProgressBar mProgressBar;
    private SearchView mSearchView;

    private void initializeWidgets(){
        mGridView = findViewById(R.id.mGridView);
        mProgressBar = findViewById(R.id.mProgressBar);
        mProgressBar.setIndeterminate(true);
        mProgressBar.setVisibility(View.VISIBLE);
        mSearchView = findViewById(R.id.mSearchView);
        mSearchView.setIconified(true);
    }

    private void populateListView(List<SearchResults> bookList) {
        ListViewAdapter adapter = new ListViewAdapter(this, bookList);
        mGridView.setAdapter(adapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_book);

        this.initializeWidgets();
        final MyAPIService myAPIService = RetrofitClientInstance.getRetrofitInstance().create(MyAPIService.class);

        final Call<List<SearchResults>> call = myAPIService.searchKitHub("empty result");
        call.enqueue(new Callback<List<SearchResults>>() {

            @Override
            public void onResponse(@NonNull Call<List<SearchResults>> call,
                                   @NonNull Response<List<SearchResults>> response) {
                mProgressBar.setVisibility(View.GONE);
                populateListView(response.body());
            }
            @Override
            public void onFailure(@NonNull Call<List<SearchResults>> call,
                                  @NonNull Throwable throwable) {
                mProgressBar.setVisibility(View.GONE);
            }
        });
        mSearchView.setFocusable(true);
        mSearchView.requestFocusFromTouch();
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String query) {
                if (!query.equals("")){
                    final Call<List<SearchResults>> call = myAPIService.searchKitHub(query);
                    call.enqueue(new Callback<List<SearchResults>>() {

                        @Override
                        public void onResponse(@NonNull Call<List<SearchResults>> call,
                                               @NonNull Response<List<SearchResults>> response) {
                            mProgressBar.setVisibility(View.GONE);
                            populateListView(response.body());
                        }
                        @Override
                        public void onFailure(@NonNull Call<List<SearchResults>> call,
                                              @NonNull Throwable throwable) {
                            populateListView(new ArrayList<SearchResults>());
                            mProgressBar.setVisibility(View.GONE);
                        }
                    });
                    return false;
                }
                return false;
            }
        });

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final SearchResults book = (SearchResults) mGridView.getItemAtPosition(position);

                String query = book.getTitle()+ " adlı kitabin bilgilerini güncellemek istediğinizden emin misiniz?";


                AlertDialog.Builder alert = new AlertDialog.Builder(UpdateBookSearch.this);
                alert.setTitle("Kitap bilgileri güncellemesi onaylama ekranı");
                alert.setMessage(query);

                alert.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(UpdateBookSearch.this, "Gerekli sayfaya yönlendiriliyorsunuz", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(UpdateBookSearch.this, UpdateBookInfo.class);
                        intent.putExtra("book_id", book.getId());
                        startActivity(intent);
                    }
                });
                alert.setNegativeButton("Hayır", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(UpdateBookSearch.this, "İşlem iptal edildi", Toast.LENGTH_SHORT).show();
                    }
                });
                alert.create().show();
            }
        });
    }
}

