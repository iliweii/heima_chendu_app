package heima.chendu.ui.home;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import heima.chendu.R;
import heima.chendu.pojo.Chendu;
import heima.chendu.pojo.Record;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    private TextView home_date;
    private TextView home_mean;
    private TextView home_yinbiao;
    private EditText home_word;
    private TextView home_res;
    private Button home_submit;
    private Button home_look;
    private TextView home_metting;
    private TextView home_count;

    private List<Chendu> chendus;
    private String nowDate;
    private SharedPreferences mSharedPreferences;
    private Integer meeting_today = 0, meeting_total = 0;
    private Integer count_today = 0, count_total = 0;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
//        final TextView textView = root.findViewById(R.id.text_home);
//        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });

        home_date = root.findViewById(R.id.home_date);
        home_mean = root.findViewById(R.id.home_mean);
        home_yinbiao = root.findViewById(R.id.home_yinbiao);
        home_word = root.findViewById(R.id.home_word);
        home_res = root.findViewById(R.id.home_res);
        home_submit = root.findViewById(R.id.home_submit);
        home_look = root.findViewById(R.id.home_look);
        home_metting = root.findViewById(R.id.home_meeting);
        home_count = root.findViewById(R.id.home_count);

        Gson gson = new Gson();

        // 获取当前日期
        Date date = new Date();
        SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd");
        nowDate = dateFormat.format(date);

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
            if (recordList.get(i).getDate().equals(nowDate)) {
                Record record = recordList.get(i);
                meeting_today = record.getWords().size();
                count_today = record.getTime();
            }
            count_total += recordList.get(i).getTime();
        }

        // 请求 heima_chendu.json 获取数据
        StringBuilder stringBuilder = new StringBuilder();
        try {
            InputStream is = getResources().getAssets().open("heima_chendu.json");
            InputStreamReader isReader = new InputStreamReader(is, "UTF-8");
            BufferedReader bReader = new BufferedReader(isReader);
            String mimeTypeLine = null;
            while ((mimeTypeLine = bReader.readLine()) != null) {
                stringBuilder.append(mimeTypeLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 解析数据，赋值到chendus
        String chenduJson = stringBuilder.toString();
        Type chenduListType = new TypeToken<ArrayList<Chendu>>(){}.getType();
        List<Chendu> chenduList = gson.fromJson(chenduJson, chenduListType);
        chendus = chenduList;

        // 页面展示部分
        meeting_total = getChenduList(nowDate).size();
        home_metting.setText(meeting_today + " / " + meeting_total);
        home_count.setText(count_today + " / " + count_total);

        // 展示题目
        NextQuestion();

        // 确定按钮点击事件
        home_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 确定状态下：
                if (home_submit.getText().equals("确定")) {
                    // Toast.makeText(getContext(), "确认", Toast.LENGTH_SHORT).show();
                    home_submit.setText("下一题");
                    String user_input = home_word.getText().toString();
                    String user_res = home_res.getText().toString();
                    if (user_input.equals(user_res)) {
                        // 正确状态下直接跳转下一题
                        NextQuestion();
                    } else {
                        // 错误状态下提示答案并调为红色
                        home_res.setVisibility(View.VISIBLE);
                        home_word.setTextColor(getResources().getColor(R.color.red));
                    }
                    // 记录题数并更新
                    RecordRefresh(user_res);
                } else {
                    // 下一题状态下直接跳转下一题
                    NextQuestion();
                }

            }
        });

        // 看答案按钮点击事件
        home_look.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                home_res.setVisibility(View.VISIBLE);
            }
        });

        return root;
    }

    /**
     * 获取日期列表
     * @return
     */
    public List<String> getDateList() {
        List<String> datelist = new ArrayList<String>();
        for (int i = 0; i < chendus.size(); i++) {
            datelist.add(chendus.get(i).getDate());
        }
        List<String> dates = datelist.stream().distinct().collect(Collectors.toList());
        return dates;
    }

    /**
     * 获取晨读单词列表
     * @param date
     * @return
     */
    public List<Chendu> getChenduList(String date) {
        List<Chendu> chenduList = new ArrayList<Chendu>();
        for (Chendu cd:chendus) {
            if (cd.getDate().indexOf(date) > -1)
                chenduList.add(cd);
        }
        List<Chendu> chenduList1 = chenduList.stream().sorted(Comparator.comparing(Chendu::getId)).collect(Collectors.toList());
        return chenduList1;
    }

    /**
     * 获取随机晨读单词
     * @param date
     * @return
     */
    public Chendu getChendu(String date) {
        List<Chendu> chenduList = getChenduList(date);
        int index = RandomNum(0, chenduList.size() - 1);
        Chendu cd = chenduList.get(index);
        cd.setWord(cd.getWord().trim());
        cd.setYinbiao(cd.getYinbiao().replaceAll("(\\\r\\\n|\\\r|\\\n|\\\n\\\r)", ""));
        return cd;
    }

    /**
     * 获取范围内随机数
     * @param min
     * @param max
     * @return
     */
    public static Integer RandomNum(int min, int max) {
        int i = (int) (Math.random() * (max - min + 1) + min);
        return i;
    }

    /**
     * 跳转下一题
     */
    private void NextQuestion() {
        home_res.setVisibility(View.INVISIBLE);
        home_word.setTextColor(getResources().getColor(R.color.black));
        home_submit.setText("确定");
        home_word.setText("");
        home_word.requestFocus();
        Chendu chendu = getChendu(nowDate);
        home_mean.setText(chendu.getMean());
        home_yinbiao.setText(chendu.getYinbiao());
        home_res.setText(chendu.getWord());
    }

    /**
     * 记录并刷新
     * @param word
     */
    private void RecordRefresh(String word) {
        // 获取存储数据
        String recordJson = mSharedPreferences.getString("records", "");
        Type recordListType = new TypeToken<ArrayList<Record>>(){}.getType();
        Gson gson = new Gson();
        List<Record> recordList = gson.fromJson(recordJson, recordListType);
        // 处理存储数据
        if (!recordList.stream().filter(m->m.getDate().equals(nowDate)).findAny().isPresent()) {
            Record newRecord = new Record();
            newRecord.setDate(nowDate);
            newRecord.setTime(0);
            recordList.add(newRecord);
        }
        for (int i = 0; i < recordList.size(); i++) {
            if (recordList.get(i).getDate().equals(nowDate)) {
                Record record = recordList.get(i);
                count_today ++;
                count_total ++;
                record.setTime(count_today);
                if (record.getWords() == null) {
                    record.setWords(new ArrayList<String>());
                }
                if (!record.getWords().stream().filter(m->m.equals(word)).findAny().isPresent()) {
                    // 不存在该word，添加，并计数++
                    meeting_today ++;
                    List<String> l = record.getWords();
                    l.add(word);
                    record.setWords(l);
                }
            }
        }
        // 存储处理完的数据
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("records", gson.toJson(recordList));
        editor.commit();
        // 更新页面
        home_metting.setText(meeting_today + " / " + meeting_total);
        home_count.setText(count_today + " / " + count_total);
    }
}