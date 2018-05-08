package com.jwl.mymapdemo;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;

public class MainActivity extends AppCompatActivity {
    private MapView myMapView = null;//地图控件
    private BaiduMap myBaiduMap;//百度地图对象
    private LocationClient mylocationClient;//定位服务客户对象
    private MylocationListener mylistener;//重写的监听类
    private Context context;

    private RoutePlanSearch mSearch;
    private PlanNode startNode;
    private PlanNode endNode;
    private LatLng fromLl;
    private LatLng endLl;
    private DrivingRouteOverlay mRouteOverlay;

    private double myLatitude;//纬度，用于存储自己所在位置的纬度
    private double myLongitude;//经度，用于存储自己所在位置的经度
    private float myCurrentX;//方向

    private BitmapDescriptor myIconLocation1;//图标1，当前位置的箭头图标
    private BitmapDescriptor myIconLocation2;//图表2,前往位置的中心图标

    private MyOrientationListener myOrientationListener;//方向感应器类对象

    private MyLocationConfiguration.LocationMode locationMode;//定位图层显示方式
//    private MyLocationConfiguration.LocationMode locationMode2;//定位图层显示方式

    private LinearLayout myLinearLayout1; //经纬度搜索区域1
    private LinearLayout myLinearLayout2; //地址搜索区域2
    private LinearLayout myLinearLayout3; //路径搜索区域3
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        this.context = this;
        initView();
        initLocation();
        initPlan();
    }

    private void initView() {
        //获取地图控件的引用
        myMapView = (MapView) findViewById(R.id.baiduMapView);

        //获取地图对象
        myBaiduMap = myMapView.getMap();

        //默认显示普通地图
//        myBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        //开启交通图
        //myBaiduMap.setTrafficEnabled(true);
        //开启热力图
        //myBaiduMap.setBaiduHeatMapEnabled(true);
        // 开启定位图层
//        myBaiduMap.setMyLocationEnabled(true);
//        mylocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        //配置定位SDK参数
        initLocation();
//        mylocationClient.registerLocationListener(myListener);    //注册监听函数
        //开启定位
//        mylocationClient.start();
        //图片点击事件，回到定位点
//        mylocationClient.requestLocation();

        //根据给定增量缩放地图级别
        MapStatusUpdate msu= MapStatusUpdateFactory.zoomTo(18.0f);
        myBaiduMap.setMapStatus(msu);
    }

    private void initLocation() {
        locationMode = MyLocationConfiguration.LocationMode.NORMAL;

        //初始化图标,BitmapDescriptorFactory是bitmap 描述信息工厂类.
        myIconLocation1 = BitmapDescriptorFactory.fromResource(R.drawable.location_marker);
        myIconLocation2 = BitmapDescriptorFactory.fromResource(R.drawable.ditu_mark2);

        //定义Maker坐标点
        LatLng point = new LatLng(21.1575819196, 110.3079074662);

        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(point)
                .icon(myIconLocation1);
        //在地图上添加Marker，并显示
        myBaiduMap.addOverlay(option);


        //定位服务的客户端。宿主程序在客户端声明此类，并调用，目前只支持在主线程中启动
        mylocationClient = new LocationClient(this);
        mylistener = new MylocationListener();

        //注册监听器
        mylocationClient.registerLocationListener(mylistener);
        //配置定位SDK各配置参数，比如定位模式、定位时间间隔、坐标系类型等
        LocationClientOption mOption = new LocationClientOption();
        //设置坐标类型
        mOption.setCoorType("bd09ll");
        //设置是否需要地址信息，默认为无地址
        mOption.setIsNeedAddress(true);
        //设置是否打开gps进行定位
        mOption.setOpenGps(true);
        //设置扫描间隔，单位是毫秒 当<1000(1s)时，定时定位无效
        int span = 1000;
        mOption.setScanSpan(span);
        //设置 LocationClientOption
        mylocationClient.setLocOption(mOption);

        // 开启定位图层
        myBaiduMap.setMyLocationEnabled(true);
        //mylocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类，在上面已调用
        //配置定位SDK参数
        //initLocation();
        //mylocationClient.registerLocationListener(mylistener);    //注册监听函数
        //开启定位
        mylocationClient.start();
        //图片点击事件，回到定位点
        mylocationClient.requestLocation();

        //配置定位图层显示方式,三个参数的构造器
//        MyLocationConfiguration configuration
//                = new MyLocationConfiguration(locationMode, true, myIconLocation1);
        //设置定位图层配置信息，只有先允许定位图层后设置定位图层配置信息才会生效，参见 setMyLocationEnabled(boolean)
//        myBaiduMap.setMyLocationConfigeration(configuration);

        myOrientationListener = new MyOrientationListener(context);
        //通过接口回调来实现实时方向的改变
        myOrientationListener.setOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                myCurrentX = x;
            }
        });

    }

    //设置路径位置
    private void setLocation(LatLng fromLl,LatLng endLl) {
        startNode = PlanNode.withLocation(fromLl);
        endNode = PlanNode.withLocation(endLl);
    }

    private void initPlan() {
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(new OnGetRoutePlanResultListener() {
            @Override
            public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {

            }

            @Override
            public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

            }

            @Override
            public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

            }

            @Override
            public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
                if (drivingRouteResult == null || drivingRouteResult.error !=   SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(MainActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
                }
                if (drivingRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                    // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                    drivingRouteResult.getSuggestAddrInfo();
                    return;
                }
                if (drivingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
                    if (drivingRouteResult.getRouteLines().size() >= 1) {
//                        DrivingRouteLine route = drivingRouteResult.getRouteLines().get(0);
                        DrivingRouteOverlay overlay = new DrivingRouteOverlay(myBaiduMap);
                        mRouteOverlay = overlay;
                        myBaiduMap.setOnMarkerClickListener(overlay);
                        overlay.setData(drivingRouteResult.getRouteLines().get(0));
                        overlay.addToMap();
                        overlay.zoomToSpan();
                    } else {
                        Log.d("route result", "结果数<0");
                        return;
                    }
                }
            }

            @Override
            public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

            }

            @Override
            public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

            }
        });
    }

    /*
     *创建菜单操作
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            /*
             *第一个功能，返回自己所在的位置，箭头表示
             */
            case R.id.menu_item_mylocation://返回当前位置
                myLatitude = 21.1575819196;
                myLongitude = 110.3079074662;
                getLocationByLL(myLatitude, myLongitude);
                break;

            /*
             *第二个功能，根据经度和纬度前往位置
             */
            case R.id.menu_item_llsearch://根据经纬度搜索地点
                myLinearLayout1 = (LinearLayout) findViewById(R.id.linearLayout1);
                //经纬度输入区域1可见
                myLinearLayout1.setVisibility(View.VISIBLE);
                final EditText myEditText_lg = (EditText) findViewById(R.id.editText_lg);
                final EditText myEditText_la = (EditText) findViewById(R.id.editText_la);
                Button button_ll = (Button) findViewById(R.id.button_llsearch);

                button_ll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final double mylg = Double.parseDouble(myEditText_lg.getText().toString());
                        final double myla = Double.parseDouble(myEditText_la.getText().toString());

                        getLocationByLL(myla, mylg);

                        //隐藏前面经纬度输入区域
                        myLinearLayout1.setVisibility(View.GONE);
//                        Toast.makeText(context, "", Toast.LENGTH_SHORT).show();
                        //隐藏输入法键盘
                        InputMethodManager imm =(InputMethodManager)getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                });
                break;

            /*
             *第三个功能，根据地址名前往所在的位置
             */
            case R.id.menu_item_sitesearch://根据地址搜索
                myLinearLayout2 = (LinearLayout) findViewById(R.id.linearLayout2);
                //显示地址搜索区域2
                myLinearLayout2.setVisibility(View.VISIBLE);
                final EditText myEditText_site = (EditText) findViewById(R.id.editText_site);
                Button button_site = (Button) findViewById(R.id.button_sitesearch);

                button_site.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String site_str = myEditText_site.getText().toString();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                AddressToLatitudeLongitude at = new AddressToLatitudeLongitude(site_str);
                                at.getLatAndLngByAddress();
                                getLocationByLL(at.getLatitude(), at.getLongitude());

                                LatLng latLng = new LatLng(at.getLatitude(), at.getLongitude());
                                //构建MarkerOption，用于在地图上添加Marker
                                OverlayOptions option = new MarkerOptions()
                                        .position(latLng)
                                        .icon(myIconLocation2);
                                //在地图上添加Marker，并显示
                                myBaiduMap.addOverlay(option);
                            }
                        }).start();
                        //隐藏前面地址输入区域
                        myLinearLayout2.setVisibility(View.GONE);
                        //隐藏输入法键盘
                        InputMethodManager imm = (InputMethodManager) getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                });
                break;

            /*
             * 第四个功能：转换成卫星地图
             */
            case R.id.menu_item_satelite://卫星地图
                myBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                break;
            /*
             * 第五个功能：普通功能
             */
            case R.id.menu_item_normal://普通地图
                myBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                break;
            /*
             * 第六个功能；路径
             */
            case R.id.menu_item_lujing://路径显示
                myLinearLayout3 = (LinearLayout) findViewById(R.id.linearLayout3);
                //显示路径搜索区域3
                myLinearLayout3.setVisibility(View.VISIBLE);
                final EditText myEditText_primary = (EditText) findViewById(R.id.editText_primary);
                final EditText myEditText_end = (EditText) findViewById(R.id.editText_end);
                Button button_lj = (Button) findViewById(R.id.button_ljsearch);

                button_lj.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String pri_str = myEditText_primary.getText().toString();
                        final String end_str = myEditText_end.getText().toString();

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                AddressToLatitudeLongitude at1= new AddressToLatitudeLongitude(pri_str);
                                at1.getLatAndLngByAddress();
//                                getLocationByLL(at1.getLatitude(), at1.getLongitude());

                                AddressToLatitudeLongitude at2 = new AddressToLatitudeLongitude(end_str);
                                at2.getLatAndLngByAddress();
//                                getLocationByLL(at2.getLatitude(), at2.getLongitude());
                                fromLl = new LatLng(at1.getLatitude(), at1.getLongitude());
                                endLl = new LatLng(at2.getLatitude(), at2.getLongitude());

                                LuJingToLujing luJingToLujing = new LuJingToLujing();
                                luJingToLujing.test(at1.getLatitude(), at1.getLongitude(),at2.getLatitude(), at2.getLongitude());

                                setLocation(fromLl,endLl);
                                mSearch.drivingSearch(new DrivingRoutePlanOption().from(startNode).to(endNode));
                            }
                        }).start();

                        //隐藏前面地址输入区域
                        myLinearLayout3.setVisibility(View.GONE);
                        //隐藏输入法键盘
                        InputMethodManager imm = (InputMethodManager) getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                });
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    /*
     *根据经纬度前往
     */
    public void getLocationByLL(double la, double lg)
    {
        //地理坐标的数据结构
        LatLng latLng = new LatLng(la, lg);

        //描述地图状态将要发生的变化,通过当前经纬度来使地图显示到该位置
        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
        myBaiduMap.setMapStatus(msu);

    }

    /*
     *定位请求回调接口
     */
    public class MylocationListener implements BDLocationListener
    {
        //定位请求回调接口
        private boolean isFirstIn=true;
        //定位请求回调函数,这里面会得到定位信息
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            //BDLocation 回调的百度坐标类，内部封装了如经纬度、半径等属性信息
            //MyLocationData 定位数据,定位数据建造器
            /*
            * 可以通过BDLocation配置如下参数
            * 1.accuracy 定位精度
            * 2.latitude 百度纬度坐标
            * 3.longitude 百度经度坐标
            * 4.satellitesNum GPS定位时卫星数目 getSatelliteNumber() gps定位结果时，获取gps锁定用的卫星数
            * 5.speed GPS定位时速度 getSpeed()获取速度，仅gps定位结果时有速度信息，单位公里/小时，默认值0.0f
            * 6.direction GPS定位时方向角度
            * */
            myLatitude = bdLocation.getLatitude();
            myLongitude = bdLocation.getLongitude();
            //构造定位数据
            MyLocationData data = new MyLocationData.Builder()
                    .direction(myCurrentX)//设定图标方向
                    .accuracy(bdLocation.getRadius())//getRadius 获取定位精度,默认值0.0f
                    .latitude(myLatitude)//百度纬度坐标
                    .longitude(myLongitude)//百度经度坐标
                    .build();
            //设置定位数据, 只有先允许定位图层后设置数据才会生效，参见 setMyLocationEnabled(boolean)
            myBaiduMap.setMyLocationData(data);

            //判断是否为第一次定位,是的话需要定位到用户当前位置
            if (isFirstIn) {
                //根据当前所在位置经纬度前往
                getLocationByLL(myLatitude, myLongitude);
                isFirstIn = false;
                //提示当前所在地址信息
//                Toast.makeText(context, bdLocation.getAddrStr(), Toast.LENGTH_SHORT).show();
            }

        }
    }

    /*
    *定位服务的生命周期，达到节省
    */
    @Override
    protected void onStart() {
        super.onStart();
        //开启定位，显示位置图标
        myBaiduMap.setMyLocationEnabled(true);
        if(!mylocationClient.isStarted())
        {
            mylocationClient.start();
        }
        myOrientationListener.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //停止定位
        myBaiduMap.setMyLocationEnabled(false);
        mylocationClient.stop();
        myOrientationListener.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        myMapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        myMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        myMapView.onPause();
    }
}
