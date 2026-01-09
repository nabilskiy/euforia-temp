package digital.euforia.app.ui.subscription;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import coil.Coil;
import coil.ImageLoader;
import coil.request.ImageRequest;
import digital.euforia.app.R;
import digital.euforia.app.domain.model.subscription.PremiumBenefit;

public class PremiumBenefitsAdapter extends RecyclerView.Adapter<PremiumBenefitsAdapter.VH> {

    private final LayoutInflater inflater;
    private List<PremiumBenefit> items = Collections.emptyList();
    private boolean infinite = true;

    public PremiumBenefitsAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
    }

    public void submitList(List<PremiumBenefit> list) {
        this.items = list == null ? Collections.emptyList() : list;
        notifyDataSetChanged();
    }

    public void setInfinite(boolean infinite) {
        this.infinite = infinite;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_premium_benefit, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        if (items.isEmpty()) return;
        int idx = position % items.size();
        PremiumBenefit item = items.get(idx);
        holder.title.setText(item.getTitle());

        // Resolve custom "img://<drawable_name>" scheme to a drawable resource id
        Object data = item.getImageUrl();
        if (data instanceof String) {
            String s = (String) data;
            if (s.startsWith("img://")) {
                String name = s.substring(6);
                int resId = holder.image.getContext().getResources().getIdentifier(
                        name,
                        "drawable",
                        holder.image.getContext().getPackageName()
                );
                if (resId != 0) {
                    data = resId; // Coil supports drawable resource ids directly
                }
            }
        }

        ImageLoader imageLoader = Coil.imageLoader(holder.image.getContext());
        ImageRequest request = new ImageRequest.Builder(holder.image.getContext())
                .data(data)
                .crossfade(true)
                .placeholder(android.R.drawable.ic_menu_report_image)
                .error(android.R.drawable.ic_menu_report_image)
                .target(holder.image)
                .build();
        imageLoader.enqueue(request);
    }

    @Override
    public int getItemCount() {
        if (items == null || items.isEmpty()) return 0;
        return infinite ? Integer.MAX_VALUE : items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        VH(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            title = itemView.findViewById(R.id.title);
        }
    }
}