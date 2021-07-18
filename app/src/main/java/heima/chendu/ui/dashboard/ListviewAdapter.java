package heima.chendu.ui.dashboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

import heima.chendu.R;
import heima.chendu.pojo.Chendu;

public class ListviewAdapter extends ArrayAdapter<Chendu> {
    private int resourceId;
    private Context context;

    public ListviewAdapter(@NonNull Context context, int textViewResourceId, @NonNull List<Chendu> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
        this.context = context;
    }

    public View getView(int position, View convertview, ViewGroup parent) {
        Chendu chendu = getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertview == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.item_id = view.findViewById(R.id.item_id);
            viewHolder.item_word = view.findViewById(R.id.item_word);
            viewHolder.item_yinbiao = view.findViewById(R.id.item_yinbiao);
            viewHolder.item_mean = view.findViewById(R.id.item_mean);
            view.setTag(viewHolder);
        } else {
            view = convertview;
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.item_id.setText(String.valueOf(position + 1));
        viewHolder.item_word.setText(chendu.getWord());
        viewHolder.item_yinbiao.setText(chendu.getYinbiao());
        viewHolder.item_mean.setText(chendu.getMean());

        return view;
    }

    class ViewHolder {
        TextView item_id;
        TextView item_word;
        TextView item_yinbiao;
        TextView item_mean;
    }

}
