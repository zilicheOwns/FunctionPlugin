package ziliche.top.function.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;


/**
 * @author eddie
 */
public class LoadingHeaderHolder extends RecyclerView.ViewHolder {

    private ImageView loadingImage;
    private boolean isLoading = false;

    private LoadingHeaderHolder(View itemView) {
        super(itemView);
        loadingImage = itemView.findViewById(R.id.loading_image);
    }

    static LoadingHeaderHolder create(ViewGroup parent) {
        return new LoadingHeaderHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_loading_header, parent, false));
    }

    public void startLoading() {
        isLoading = true;
        if (loadingImage.getAnimation() != null && !loadingImage.getAnimation().hasEnded()) {
            loadingImage.getAnimation().cancel();
        }
        Animation animation = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.rotate_animation);
        loadingImage.startAnimation(animation);
    }

    public void stopLoading() {
        isLoading = false;
        if (loadingImage.getAnimation() != null) {
            loadingImage.getAnimation().cancel();
        }
    }

    public boolean isLoading() {
        return isLoading;
    }
}
