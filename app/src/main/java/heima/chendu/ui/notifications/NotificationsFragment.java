package heima.chendu.ui.notifications;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import heima.chendu.R;
import heima.chendu.pojo.Record;

public class NotificationsFragment extends Fragment {

    private NotificationsViewModel notificationsViewModel;

    private TextView mine_number;

    private SharedPreferences mSharedPreferences;
    private Integer total = 0;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);
//        final TextView textView = root.findViewById(R.id.text_notifications);
//        notificationsViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });

        Gson gson = new Gson();
        mine_number = root.findViewById(R.id.mine_number);

        // 获取存储数据
        mSharedPreferences = getActivity().getSharedPreferences("heima_chendu", 0);
        String recordJson = mSharedPreferences.getString("records", "");
        List<Record> recordList = null;
        if ("".equals(recordJson)) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString("records", "[]");
            editor.commit();
        } else {
            Type recordListType = new TypeToken<ArrayList<Record>>(){}.getType();
            recordList = gson.fromJson(recordJson, recordListType);
        }

        // 对存储数据进行处理
        for (int i = 0; i < recordList.size(); i++) {
            total += recordList.get(i).getTime();
        }

        // 页面展示
        mine_number.setText(String.valueOf(total));

        return root;
    }
}