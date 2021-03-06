package com.yusong.plugin_navi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.IntDef;
import android.support.annotation.StringDef;
import android.util.Log;

import com.feisher.alertview.AlertView;
import com.feisher.alertview.OnItemClickListener;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.LinkedList;
import java.util.List;
/**
* explin: 默认支持百度和高德app+腾讯 调用，默认使用骑行导航(路线规划)，暂不提供其他导航方式
 *      使用：NaviUtil.with(this,NaviUtil.GPS84).navi(………………);
* auther:feisher
* create by 2018/4/28/028 14:24
*/
public class NaviUtil {
    /**默认查找百度和高s德 + 腾讯地图*/
    private static String[] paks = new String[]{"com.autonavi.minimap","com.baidu.BaiduMap","com.tencent.map"};
    Activity activity;
    int coorType;
    String mode;
    private AlertView alertView;

    @IntDef({GPS84, GCJ02, DB09})
    @Retention(RetentionPolicy.SOURCE)
    /** 坐标系类型*/
    public @interface CoorType {}
    public static final int GPS84 = 0;
    public static final int GCJ02 = 1;
    public static final int DB09 = 2;

    @StringDef({"bus", "car", "walk","ride"})
    @Retention(RetentionPolicy.SOURCE)
    /** 坐标系类型*/
    public @interface Mode {}
    public static final String BUS = "bus";
    public static final String CAR = "car";
    public static final String WALK = "walk";
    public static final String RIDE = "ride";



    private static volatile NaviUtil instance;
    private NaviUtil(Activity activity,@CoorType int coorType) {
        this(activity,coorType,RIDE);
    }
    private NaviUtil(Activity activity,@CoorType int coorType,@Mode String mode) {
        this.activity = activity;
        this.coorType = coorType;
        this.mode = mode;
    }
    /**
     *  实际这里是没有单例的，因为涉及dialog提示，单例回导致弹窗失败
     *  ，故此处只是提供两种调起模式
     * @param activity 调用导航功能的activity
     * @param coorType 坐标系类型
     * @return
     */
    public static NaviUtil with(Activity activity,@CoorType int coorType) {
        return  with(activity,coorType,RIDE);
    }
    public static NaviUtil with(Activity activity,@CoorType int coorType,@Mode String mode) {
        return  instance = new NaviUtil(activity,coorType,mode);
    }

    public void navi(double sLat, double sLng, final String sName, double eLat, double eLng, final String eName, final String appName){
        if (coorType == GCJ02) {
            double[] s = GPSUtil.gcj02_To_Gps84(sLat, sLng);
            double[] e = GPSUtil.gcj02_To_Gps84(eLat, eLng);
            sLat = s[0];
            sLng = s[1];
            eLat = e[0];
            eLng = e[1];
        }else if (coorType == DB09) {
            double[] s = GPSUtil.bd09_To_gps84(sLat, sLng);
            double[] e = GPSUtil.bd09_To_gps84(eLat, eLng);
            sLat = s[0];
            sLng = s[1];
            eLat = e[0];
            eLng = e[1];
        }
        List<String> mapApps = getMapApps(activity);
        final double finalSLat = sLat;
        final double finalSLng = sLng;
        final double finalELat = eLat;
        final double finalELng = eLng;
        if ( mapApps != null && !mapApps.isEmpty() ) {
            if ( mapApps.contains(paks[0]) && mapApps.contains(paks[1])&& mapApps.contains(paks[2]) ) {
                alertView = new AlertView.Builder().setContext(activity)
                        .setStyle(AlertView.Style.ActionSheet)
                        .setTitle("选择地图app")
                        .setMessage(null)
                        .setCancelText("取消")
                        .setDestructive("高德地图", "百度地图","腾讯地图")
                        .setOnItemClickListener(new OnItemClickListener() {
                            @Override
                            public void onItemClick(Object o, int i) {
                                if (i == 0) {
                                    openGaodeDirection(finalSLat, finalSLng, sName, finalELat, finalELng, eName, appName);
                                } else if (i == 1){
                                    openBaiduDirection(finalSLat, finalSLng, sName, finalELat, finalELng, eName);
                                }else if (i == 2){
                                    openTencentDirection(finalSLat, finalSLng, sName, finalELat, finalELng, eName,appName);
                                }
                                if (alertView!=null &&alertView.isShowing()) {
                                    alertView.dismiss();
                                }
                            }
                        })
                        .build().setCancelable(true);
                alertView.show();

            } else if ( mapApps.contains(paks[0]) && mapApps.contains(paks[1]) ) {
                alertView = new AlertView.Builder().setContext(activity)
                        .setStyle(AlertView.Style.ActionSheet)
                        .setTitle("选择地图app")
                        .setMessage(null)
                        .setCancelText("取消")
                        .setDestructive("高德地图", "百度地图")
                        .setOnItemClickListener(new OnItemClickListener() {
                            @Override
                            public void onItemClick(Object o, int i) {
                                if (i == 0) {
                                    openGaodeDirection(finalSLat, finalSLng, sName, finalELat, finalELng, eName, appName);
                                } else if (i == 1){
                                    openBaiduDirection(finalSLat, finalSLng, sName, finalELat, finalELng, eName);
                                }
                                if (alertView!=null &&alertView.isShowing()) {
                                    alertView.dismiss();
                                }
                            }
                        })
                        .build().setCancelable(true);
                alertView.show();

            }else if ( mapApps.contains(paks[0]) && mapApps.contains(paks[2]) ) {
                alertView = new AlertView.Builder().setContext(activity)
                        .setStyle(AlertView.Style.ActionSheet)
                        .setTitle("选择地图app")
                        .setMessage(null)
                        .setCancelText("取消")
                        .setDestructive("高德地图", "腾讯地图")
                        .setOnItemClickListener(new OnItemClickListener() {
                            @Override
                            public void onItemClick(Object o, int i) {
                                if (i == 0) {
                                    openGaodeDirection(finalSLat, finalSLng, sName, finalELat, finalELng, eName, appName);
                                }else if (i == 1){
                                    openTencentDirection(finalSLat, finalSLng, sName, finalELat, finalELng, eName,appName);
                                }
                                if (alertView!=null &&alertView.isShowing()) {
                                    alertView.dismiss();
                                }
                            }
                        })
                        .build().setCancelable(true);
                alertView.show();

            }else if ( mapApps.contains(paks[1]) && mapApps.contains(paks[2]) ) {
                alertView = new AlertView.Builder().setContext(activity)
                        .setStyle(AlertView.Style.ActionSheet)
                        .setTitle("选择地图app")
                        .setMessage(null)
                        .setCancelText("取消")
                        .setDestructive("百度地图", "腾讯地图")
                        .setOnItemClickListener(new OnItemClickListener() {
                            @Override
                            public void onItemClick(Object o, int i) {
                                 if (i == 0){
                                    openBaiduDirection(finalSLat, finalSLng, sName, finalELat, finalELng, eName);
                                }else if (i == 1){
                                    openTencentDirection(finalSLat, finalSLng, sName, finalELat, finalELng, eName,appName);
                                }
                                if (alertView!=null &&alertView.isShowing()) {
                                    alertView.dismiss();
                                }
                            }
                        })
                        .build().setCancelable(true);
                alertView.show();

            }else if ( mapApps.contains(paks[0]) ) {
                openGaodeDirection(sLat, sLng, sName, eLat, eLng, eName, appName);
            }else if ( mapApps.contains(paks[1]) ) {
                openBaiduDirection(sLat, sLng, sName, eLat, eLng, eName);
            }else {
                openTencentDirection(finalSLat, finalSLng, sName, finalELat, finalELng, eName,appName);
            }
        }else {
            if (alertView!=null &&alertView.isShowing()) {
                alertView.dismiss();
            }
            alertView = new AlertView.Builder().setContext(activity)
                    .setTitle("提示")
                    .setMessage("您没有安装百度或高德地图app,\n是否使用浏览器进行路线规划?")
                    .setStyle(AlertView.Style.Alert)
                    .setDestructive("取消")
                    .setOthers(new String[]{"确定"})
                    .setOnItemClickListener(new OnItemClickListener() {
                        @Override
                        public void onItemClick(Object o, int i) {
                            if (alertView!=null &&alertView.isShowing()) {
                                alertView.dismiss();
                            }
                            //取消对应 0，确定对应 1
                            if (i == 1) {
                                double[] s = GPSUtil.gps84_To_bd09(finalSLat, finalSLng);
                                double[] e = GPSUtil.gps84_To_bd09(finalELat, finalELng);
//                                Uri mapUri = Uri.parse("http://api.map.baidu.com/marker?location=" + e[0] + "," + e[1] + "&title=" + sName + "&content=" + eName + "&output=html&src=" + appName);
                                Uri mapUri = Uri.parse("http://api.map.baidu.com/direction?origin=latlng:" +
                                        s[0] + "," + s[1] + "|name:" + sName + "&destination=latlng:" +
                                        e[0] + "," + e[1] + "|name:" + eName + "&mode=driving&region=无"  +
                                        "&output=html&src=" + appName);
                                Intent loction = new Intent(Intent.ACTION_VIEW, mapUri);
                                activity.startActivity(loction);
                            }
                        }
                    })
                    .build().setCancelable(true);
                    alertView.show();

        }
    }

    /**
     * 调起百度客户端 路径规划(骑行模式)
     * lat,lng (先纬度，后经度)40.057406655722,116.2964407172
     * lat,lng,lat,lng (先纬度，后经度, 先左下,后右上)
     */
    public  void openBaiduDirection(double sLat, double sLng, String sName, double eLat, double eLng, String eName) {
        String bdMode = "riding";
        switch (mode){
            case BUS: bdMode = "transit";
                break;
                case CAR: bdMode = "driving";
                break;
              case WALK: bdMode = "walking";
                break;
              default: bdMode = "riding";
        }
        double[] s = GPSUtil.gps84_To_bd09(sLat, sLng);
        double[] e = GPSUtil.gps84_To_bd09(eLat, eLng);
        Intent intent = new Intent("android.intent.action.VIEW",
                Uri.parse("baidumap://map/direction?origin=name:" + sName + "|latlng:" + s[0] + "," + s[1]
                        + "&destination=name:" + eName + "|latlng:" + e[0] + "," + e[1] + "&" + "mode="+bdMode));
        activity.startActivity(intent);
    }

    /**
     * 高德路径规划
     *lat,lng (先纬度，后经度)40.057406655722,116.2964407172
     */
    public  void openGaodeDirection(double sLat, double sLng, String sName, double eLat, double eLng, String eName, String appName) {
        int tempT = 0;
        switch (mode){
            case BUS: tempT = 1;
                break;
            case CAR: tempT = 0;
                break;
            case WALK: tempT = 2;
                break;
            default: tempT = 3;
        }
        Intent intent = new Intent("android.intent.action.VIEW",
                Uri.parse("amapuri://route/plan/?sourceApplication=" + appName +
                        "&sid=&slat=" + sLat + "&slon=" +
                        sLng + "&sname=" + sName + "&did=&dlat=" +
                        eLat + "&dlon=" + eLng + "&dname=" + eName + "&dev=1&t="+tempT));
        intent.setPackage("com.autonavi.minimap");
        activity.startActivity(intent);
    }

    /**
     * 腾讯地图路径规划
     *lat,lng (先纬度，后经度)40.057406655722,116.2964407172
     */
    public  void openTencentDirection(double sLat, double sLng, String sName, double eLat, double eLng, String eName, String appName) {
        String tempMode = "drive";
        if (mode.equals(CAR)){
            tempMode = "drive";
        }else if (mode.equals(RIDE)){
            tempMode = "bike";
        }else {
            tempMode = mode;
        }

        Log.e("导航模式：", "openTencentDirection: "+tempMode );
        double[] s = GPSUtil.gps84_To_Gcj02(sLat, sLng);
        double[] e = GPSUtil.gps84_To_Gcj02(eLat, eLng);
        Intent intent = new Intent("android.intent.action.VIEW",
                Uri.parse("qqmap://map/routeplan?type="+tempMode+
                        "&from="+sName+ "&fromcoord=" + s[0] + "," + s[1]  +
                        "&tocoord=" + e[0] + "," + e[1] + "&to=" + eName +
                        "&referer="+appName));
        intent.setPackage("com.tencent.map");
        activity.startActivity(intent);
    }



    /**
     * 返回当前设备上的地图应用集合
     * @param context
     * @return
     */
    private static List<String> getMapApps(Context context) {
        LinkedList<String> apps = new LinkedList<>();
        for ( String pak : paks ) {
            String appinfo = getAppInfoByPak(context, pak);
            if ( appinfo != null ) {
                apps.add(appinfo);
            }
        }
        return apps;
    }
    /**
     * 通过包名获取应用信息
     * @param context
     * @param packageName
     * @return
     */
    private static String getAppInfoByPak(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        for ( PackageInfo packageInfo : packageInfos ) {
            if ( packageName.equals(packageInfo.packageName) ) {
                return packageName;
            }
        }
        return null;
    }


}
