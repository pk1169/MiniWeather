package com.example.pk1169;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pk1169.db.City;
import com.example.pk1169.db.County;
import com.example.pk1169.db.Province;
import com.example.pk1169.util.HttpUtil;
import com.example.pk1169.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static java.lang.Integer.parseInt;

/**
 * Created by xiaozhang on 2017/11/14.
 */

public class ChooseAreaFragment extends Fragment {
    private static final String TAG = "ChooseAreaFragment";

    // 省级
    public static final int LEVEL_PROVINCE = 0;
    // 市级
    public static final int LEVEL_CITY = 1;
    // 县级
    public static final int LEVEL_COUNTY = 2;
    // 进程框
    private ProgressDialog progressDialog;

    private TextView titleText;

    private Button backButton;
    // 地区列表
    private ListView listView;
    // 适配器
    private ArrayAdapter<String> adapter;
    // 城市数据列表
    private List<String> dataList = new ArrayList<>();

    /**
     * 省列表
     */
    private List<Province> provinceList;

    /**
     * 市列表
     */
    private List<City> cityList;

    /**
     * 县列表
     */
    private List<County> countyList;

    /**
     * 选中的省份
     */
    private Province selectedProvince;

    /**
     * 选中的城市
     */
    private City selectedCity;

    /**
     * 当前选中的级别
     */
    private int currentLevel;

    // 搜索相关组件

    private EditText searchEt;
    private Button searchBth;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 使用inflater 动态加载布局文件
        View view = inflater.inflate(R.layout.choose_area, container, false);
        titleText = (TextView) view.findViewById(R.id.title_text);
        backButton = (Button) view.findViewById(R.id.back_button);
        listView = (ListView) view.findViewById(R.id.list_view);
        //
        searchEt = (EditText)view.findViewById(R.id.selectcity_search);
        searchBth = (Button)view.findViewById(R.id.selectcity_search_button);
        // 新建一个数组适配器ArrayAdapter绑定数据，参数(当前的Activity，布局文件，数据源)
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        // 视图 listView 加载适配器
        listView.setAdapter(adapter);
        return view;
    }

    // 给ListView和Button设置了点击事件
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCities("");
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    queryCounties();
                } else if (currentLevel == LEVEL_COUNTY) {
                    String weatherId = countyList.get(position).getWeatherId();
                    if (getActivity() instanceof MainActivity) {
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    } else if (getActivity() instanceof WeatherActivity) {
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefresh.setRefreshing(true);
                        activity.requestWeather(weatherId);
                    }
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.d("search","is null");

                if (currentLevel == LEVEL_COUNTY) {
                    queryCities("");
                } else if (currentLevel == LEVEL_CITY) {
                    queryProvinces("");
                }
            }
        });
        searchBth.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String searchName =searchEt.getText().toString();
                if(currentLevel == LEVEL_PROVINCE){
                    queryProvinces(searchName);
                }else if(currentLevel == LEVEL_CITY){
                    queryCities(searchName);
                }else if(currentLevel == LEVEL_COUNTY){
                    queryCounties();
                }
            }
        });
        queryProvinces(""); // 先执行查询省份
    }

    /**
     * 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryProvinces(String search) {
        titleText.setText("中国");
        // 返回键不显示，且不占用空间
        backButton.setVisibility(View.GONE);
        // 获取省份列表
        if(search.equals("")){
            provinceList = DataSupport.findAll(Province.class);
        }else {
            provinceList = DataSupport.where("provincename = ? ",search).find(Province.class);
        }
        if (provinceList.size() > 0) {
            dataList.clear(); // 清空列表
            for (Province province : provinceList) {
                // 将省份名字添加到数据列表中
                dataList.add(province.getProvinceName());
            }
            // 实现动态刷新数据：当datalist发生变化的时候
            adapter.notifyDataSetChanged();
            // 从第一条开始显示
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            // 本地数据库没有，就到服务器上查询数据
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }

    /**
     * 查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryCities(String search) {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        if(search.equals("")){
            cityList = DataSupport.where("provinceid = ?", String.valueOf(selectedProvince.getId())).find(City.class);
        }
        else{
            cityList = DataSupport.where("provinceid = ? and cityname = ?", String.valueOf(selectedProvince.getId()),search).find(City.class);
        }

        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");
        }
    }

    /**
     * 查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid = ?", String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address, "county");
        }
    }

    /**
     * 根据传入的地址和类型从服务器上查询省市县数据。
     */
    private void queryFromServer(String address, final String type) {
        // 显示进度条
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // 服务器返回的数据
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)) {
                    // 处理返回的省份数据
                    result = Utility.handleProvinceResponse(responseText);
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(responseText, selectedProvince.getId());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                }
                if (result) {
                    // 处理完之后
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // 关闭进度条
                            closeProgressDialog();
                            // 查询数据
                            if ("province".equals(type)) {
                                queryProvinces("");
                            } else if ("city".equals(type)) {
                                queryCities("");
                            } else if ("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                // 通过runOnUiThread()方法回到主线程处理逻辑
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
