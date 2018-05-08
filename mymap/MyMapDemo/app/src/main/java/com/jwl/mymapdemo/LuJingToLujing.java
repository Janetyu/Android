package com.jwl.mymapdemo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * 根据百度地图API，根据地址得到路径的经纬度
 */

public class LuJingToLujing {

    public void test(double la1,double lg1,double la2,double lg2){

        String url = String.format("http://api.map.baidu.com/direction?origin="+la1+","+lg1+"&destination="+la2+","+lg2+
                "&ak=trasWFPVZk5BfUEBgaCy7GAf0PWjBkm3&output=json&mcode=8D:E2:B9:8D:77:1B:A0:08:A6:F5:8D:9E:46:94:69:8B:FE:D6:43:3F;com.jwl.mymapdemo&mode=driving");
        URL myURL = null;
        URLConnection httpsConn = null;
        //进行转码
        try {
            myURL = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            httpsConn = (URLConnection) myURL.openConnection();//建立连接
            if (httpsConn != null) {
                InputStreamReader insr = new InputStreamReader(//传输数据
                        httpsConn.getInputStream(), "UTF-8");
                BufferedReader br = new BufferedReader(insr);
                String data = null;
                if ((data = br.readLine()) != null) {
                    System.out.println(data);
                    //这里的data为以下的json格式字符串,因为简单，所以就不使用json解析了，直接字符串处理
                    //{"status":0,"result":{"location":{"lng":118.77807440802562,"lat":32.05723550180587},"precise":0,"confidence":12,"level":"城市"}}
                    //lat = data.substring(data.indexOf("\"lat\":")+("\"lat\":").length(), data.indexOf("},\"precise\""));
                    //lng = data.substring(data.indexOf("\"lng\":")+("\"lng\":").length(), data.indexOf(",\"lat\""));
                }
                insr.close();
                br.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //"baidumap://map/direction?region=beijing&origin=39.98871,116.43234&destination=40.057406655722,116.2964407172&mode=walking"

    public static void main(String[] args) {
        AddressToLatitudeLongitude at = new AddressToLatitudeLongitude("北京");
        at.getLatAndLngByAddress();
        System.out.println(at.getLatitude() + " " + at.getLongitude());
        //LuJingToLujing luJingToLujing = new LuJingToLujing();
        //luJingToLujing.test(at.getLatitude(),at.getLongitude());
    }
}
