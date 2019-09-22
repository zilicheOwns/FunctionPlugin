package ziliche.top.function;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import ziliche.top.function.recyclerview.SingleClick;

/**
 * @author eddie
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    String[] data = new String[]{"Text", "Android", "IOS", "Python", "Java", "go", "Swift", "Objective-C",
            "Text", "Android", "IOS", "Python", "Java", "go", "Swift", "Objective-C",
            "Text", "Android", "IOS", "Python", "Java", "go", "Swift", "Objective-C"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.set(0, 0, 0, 1);
            }
        });
        FunctionAdapter adapter = new FunctionAdapter();
        recyclerView.setAdapter(adapter);
        adapter.setData(data);

    }

    @SingleClick
    @Override
    public void onClick(View v) {

    }


    private class FunctionAdapter extends RecyclerView.Adapter<FunctionViewHolder> {

        String[] data;

        void setData(String[] data) {
            this.data = data;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public FunctionViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return FunctionViewHolder.create(viewGroup);
        }

        @Override
        public void onBindViewHolder(@NonNull FunctionViewHolder functionViewHolder, @SuppressLint("RecyclerView") final int position) {
            ((TextView) functionViewHolder.itemView).setText(data[position]);
            functionViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @SingleClick
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), ((TextView) v).getText() + " is 第" + position + "个", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.length;
        }


    }


    private static class FunctionViewHolder extends RecyclerView.ViewHolder {

        FunctionViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        static FunctionViewHolder create(ViewGroup parent) {
            return new FunctionViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder, parent, false));
        }

    }
}
