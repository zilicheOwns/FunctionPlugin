package ziliche.top.function.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * @author eddie
 */
public class LoadingFooterHolder extends RecyclerView.ViewHolder {
    private View loadingView;
    private View noMoreView;
    private ImageView loadingImage;

    public LoadingFooterHolder(View itemView) {
        super(itemView);
        loadingView = itemView.findViewById(R.id.footer_loading);
        loadingImage = itemView.findViewById(R.id.loading_image);
        noMoreView = itemView.findViewById(R.id.footer_no_more);
    }

    public static LoadingFooterHolder create(ViewGroup parent) {
        return new LoadingFooterHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_productlist_footer, parent, false));
    }

    public void setNoMoreViewText(String msg) {
        if (noMoreView != null) {
            if (noMoreView instanceof TextView) {
                ((TextView) noMoreView).setText(msg);
            }
        }
    }
}