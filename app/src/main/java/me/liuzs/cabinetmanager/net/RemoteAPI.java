package me.liuzs.cabinetmanager.net;

import android.util.Log;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpDelete;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.client.methods.HttpRequestBase;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClients;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.util.EntityUtils;
import me.liuzs.cabinetmanager.CabinetApplication;
import me.liuzs.cabinetmanager.CabinetCore;
import me.liuzs.cabinetmanager.model.Cabinet;
import me.liuzs.cabinetmanager.model.Chemical;
import me.liuzs.cabinetmanager.model.DepositItem;
import me.liuzs.cabinetmanager.model.DeviceInfo;
import me.liuzs.cabinetmanager.model.DictType;
import me.liuzs.cabinetmanager.model.InventoryDetail;
import me.liuzs.cabinetmanager.model.InventoryItem;
import me.liuzs.cabinetmanager.model.StandingBookItem;
import me.liuzs.cabinetmanager.model.StorageLaboratoryDetail;
import me.liuzs.cabinetmanager.model.SurveillanceCamera;
import me.liuzs.cabinetmanager.model.TakeOutInfo;
import me.liuzs.cabinetmanager.model.TakeOutItemInfo;
import me.liuzs.cabinetmanager.model.UsageInfo;
import me.liuzs.cabinetmanager.model.UsageItemInfo;
import me.liuzs.cabinetmanager.model.User;

public class RemoteAPI {

    public static final String TAG = "RemoteAPI";

    /**
     * HTTP接口Root地址
     */
    public static final String API_ROOT = "http://mockapi.eolinker.com/RvgW5arf86a8060ba89c248670915e44a97647837a7dade";
    //http://47.104.235.225:1080/collage
    //http://idburgsafe.com:1080/collage_pro
    private static final Random _Random = new Random();

    private static void generalBaseHeader(HttpRequestBase httpRequest) {
//        Header contentType = new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
//        Header authorization = new BasicHeader("Token", CabinetApplication.getInstance().getAdminUser().token);
//        httpRequest.addHeader(contentType);
//        httpRequest.addHeader(authorization);
    }

    /**
     * MQTT相关设定
     */
    public static class MQTT {

        /**
         * MQTT Root地址
         */
        public static final String MQTT_ROOT = "tcp://47.104.235.225:1883";
        public static final String MQTT_USER = "admin";
        public static final String MQTT_PASSWORD = "public";
        public static final String MQTT_HARDWARE_PUBLISH_TOPIC = "LAB/UP/JSONDATA";
    }

    /**
     * 基础信息，相关接口
     */
    public static class BaseInfo {

        /**
         * 搜索化学品信息
         */
        public static final String API_SEARCH_CHEMICAL = API_ROOT + "/drug/v1/chemicalInfo/chemicalInfoPageList";
        /**
         * 获取单位字典
         */
        public static final String API_GET_UNIT_DICT_CODE = API_ROOT + "/drug/v1/sysDict/dictCode/unitTypes?dictCode=unitTypes";
        /**
         * 获取纯度字典
         */
        public static final String API_GET_PURITY_DICT_CODE = API_ROOT + "/drug/v1/sysDict/dictCode/purityTypes?dictCode=purityTypes";
        /**
         * 获取计量规格字典
         */
        public static final String API_GET_MEASURE_SPEC_DICT_CODE = API_ROOT + "/drug/v1/sysDict/dictCode/measureSpec?dictCode=measureSpec";
        /**
         * 获取化学品用途字典
         */
        public static final String API_GET_PURPOSE_Types_DICT_CODE = API_ROOT + "/drug/v1/sysDict/dictCode/purposeLTypes?dictCode=purposeLTypes";

        /**
         * 化学品查询
         *
         * @param chineseName 中文名
         * @return 返回化学品信息列表
         */
        public static APIJSON<List<Chemical>> queryChemical(String chineseName) {

            try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                List<NameValuePair> valuePairs = new ArrayList<>();
                valuePairs.add(new BasicNameValuePair("chineseName", chineseName));
                valuePairs.add(new BasicNameValuePair("pageSize", "1000"));
                String params = EntityUtils.toString(new UrlEncodedFormEntity(valuePairs, "UTF-8"));
                HttpGet httpGet = new HttpGet(API_SEARCH_CHEMICAL + "?" + params);
                Log.d(TAG, httpGet.getURI().toString());
                generalBaseHeader(httpGet);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == 200) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, "utf-8");
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<List<Chemical>>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    try {
                        HttpEntity entity = httpResponse.getEntity();
                        String content = EntityUtils.toString(entity, "utf-8");
                        Log.d(TAG, content);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    APIJSON<List<Chemical>> result = new APIJSON<>();
                    result.code = code;
                    result.message = "服务器返回错误";
                    return result;
                }
            } catch (IOException e) {
                APIJSON<List<Chemical>> result = new APIJSON<>();
                result.code = -1;
                result.message = "网络请求异常";
                return result;
            }
        }

        /**
         * 获取单位字典
         *
         * @return 返回单位字典
         */
        public static APIJSON<List<DictType>> getUnitDictCode() {

            try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                HttpGet httpGet = new HttpGet(API_GET_UNIT_DICT_CODE);
                Log.d(TAG, httpGet.getURI().toString());
                generalBaseHeader(httpGet);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == 200) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, "utf-8");
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<List<DictType>>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    try {
                        HttpEntity entity = httpResponse.getEntity();
                        String content = EntityUtils.toString(entity, "utf-8");
                        Log.d(TAG, content);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    APIJSON<List<DictType>> result = new APIJSON<>();
                    result.code = code;
                    result.message = "服务器返回错误";
                    return result;
                }
            } catch (IOException e) {
                APIJSON<List<DictType>> result = new APIJSON<>();
                result.code = -1;
                result.message = "网络请求异常";
                return result;
            }
        }

        /**
         * 获取纯度字典
         *
         * @return 返回纯度字典
         */
        public static APIJSON<List<DictType>> getPurityDictCode() {

            try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                HttpGet httpGet = new HttpGet(API_GET_PURITY_DICT_CODE);
                Log.d(TAG, httpGet.getURI().toString());
                generalBaseHeader(httpGet);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == 200) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, "utf-8");
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<List<DictType>>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    try {
                        HttpEntity entity = httpResponse.getEntity();
                        String content = EntityUtils.toString(entity, "utf-8");
                        Log.d(TAG, content);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    APIJSON<List<DictType>> result = new APIJSON<>();
                    result.code = code;
                    result.message = "服务器返回错误";
                    return result;
                }
            } catch (IOException e) {
                APIJSON<List<DictType>> result = new APIJSON<>();
                result.code = -1;
                result.message = "网络请求异常";
                return result;
            }
        }

        /**
         * 获取计量规格字典
         *
         * @return 返回计量规格字典
         */
        public static APIJSON<List<DictType>> getMeasureSpecDictCode() {

            try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                HttpGet httpGet = new HttpGet(API_GET_MEASURE_SPEC_DICT_CODE);
                Log.d(TAG, httpGet.getURI().toString());
                generalBaseHeader(httpGet);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == 200) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, "utf-8");
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<List<DictType>>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    try {
                        HttpEntity entity = httpResponse.getEntity();
                        String content = EntityUtils.toString(entity, "utf-8");
                        Log.d(TAG, content);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    APIJSON<List<DictType>> result = new APIJSON<>();
                    result.code = code;
                    result.message = "服务器返回错误";
                    return result;
                }
            } catch (IOException e) {
                APIJSON<List<DictType>> result = new APIJSON<>();
                result.code = -1;
                result.message = "网络请求异常";
                return result;
            }
        }

        /**
         * 获取用途字典
         *
         * @return 返回用户字典
         */
        public static APIJSON<List<DictType>> getPurposeDictCode() {

            try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                HttpGet httpGet = new HttpGet(API_GET_PURPOSE_Types_DICT_CODE);
                Log.d(TAG, httpGet.getURI().toString());
                generalBaseHeader(httpGet);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == 200) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, "utf-8");
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<List<DictType>>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    try {
                        HttpEntity entity = httpResponse.getEntity();
                        String content = EntityUtils.toString(entity, "utf-8");
                        Log.d(TAG, content);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    APIJSON<List<DictType>> result = new APIJSON<>();
                    result.code = code;
                    result.message = "服务器返回错误";
                    return result;
                }
            } catch (IOException e) {
                APIJSON<List<DictType>> result = new APIJSON<>();
                result.code = -1;
                result.message = "网络请求异常";
                return result;
            }
        }
    }

    /**
     * 出库模块，相关接口
     */
    public static class TakeOut {

        /**
         * 获取容器详情
         */
        public static final String API_GET_CONTAINER_DETAIL = API_ROOT + "/drug/v1/labInventoryDetail/getInfoByConNo";

        /**
         * 获取所有化学品容器号列表
         */
        public static final String API_GET_CONTAINER_LIST = API_ROOT + "/drug/v1/useOutDetails/getConInfoList/";
        /**
         * 创建出柜任务
         */
        public static final String API_CREATE_TAKE_OUT_TASK = API_ROOT + "/drug/v1/useOutInfos";
        /**
         * 获取系统中当前所有出柜任务列表
         */
        public static final String API_GET_TAKE_OUT_TASK_LIST = API_ROOT + "/drug/v1/useOutInfos/pageList";
        /**
         * 获取出柜药品记录列表
         */
        public static final String API_GET_TAKE_OUT_ITEM_LIST = API_ROOT + "/drug/v1/useOutDetails/pageList";
        /**
         * 获取使用者详细信息
         */
        public static final String API_GET_STORAGE_LABORATORY_DETAIL = API_ROOT + "/drug/v1/storageLaboratory/storageLaboratoryDetail/";
        /**
         * 保存试剂出柜记录
         */
        public static final String API_SAVE_TAKE_OUT_ITEM_DETAIL = API_ROOT + "/drug/v1/useOutDetails";

        /**
         * 获取某个容器的详情
         *
         * @return 返回详情
         */
        public static APIJSON<List<TakeOutItemInfo>> getContainerDetail(String conNo) {

            try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                List<NameValuePair> valuePairs = new ArrayList<>();
                valuePairs.add(new BasicNameValuePair("conNo", conNo));
                String params = EntityUtils.toString(new UrlEncodedFormEntity(valuePairs, "UTF-8"));
                HttpGet httpGet = new HttpGet(API_GET_CONTAINER_DETAIL + "?" + params);
                Log.d(TAG, httpGet.getURI().toString());
                generalBaseHeader(httpGet);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == 200) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, "utf-8");
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<List<TakeOutItemInfo>>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    try {
                        HttpEntity entity = httpResponse.getEntity();
                        String content = EntityUtils.toString(entity, "utf-8");
                        Log.d(TAG, content);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    APIJSON<List<TakeOutItemInfo>> result = new APIJSON<>();
                    result.code = code;
                    result.message = "服务器返回错误";
                    return result;
                }
            } catch (IOException e) {
                APIJSON<List<TakeOutItemInfo>> result = new APIJSON<>();
                result.code = -1;
                result.message = "网络请求异常";
                return result;
            }
        }

        /**
         * 获得所有库存化学品列表
         *
         * @return 返回库存信息列表
         */
        public static APIJSON<List<TakeOutItemInfo>> getContainerNoList(String devId) {

            try {
                Cabinet cabinetInfo = CabinetCore.getCabinetInfo();
                CloseableHttpClient httpClient = HttpClients.createDefault();
                List<NameValuePair> valuePairs = new ArrayList<>();
                valuePairs.add(new BasicNameValuePair("devId", devId));
                String params = EntityUtils.toString(new UrlEncodedFormEntity(valuePairs, "UTF-8"));
                HttpGet httpGet = new HttpGet(API_GET_CONTAINER_LIST + devId + "?" + params);
                Log.d(TAG, httpGet.getURI().toString());
                generalBaseHeader(httpGet);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == 200) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, "utf-8");
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<List<TakeOutItemInfo>>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    try {
                        HttpEntity entity = httpResponse.getEntity();
                        String content = EntityUtils.toString(entity, "utf-8");
                        Log.d(TAG, content);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    APIJSON<List<TakeOutItemInfo>> result = new APIJSON<>();
                    result.code = code;
                    result.message = "服务器返回错误";
                    return result;
                }
            } catch (IOException e) {
                APIJSON<List<TakeOutItemInfo>> result = new APIJSON<>();
                result.code = -1;
                result.message = "网络请求异常";
                return result;
            }
        }

        /**
         * 创建一个出柜单任务
         *
         * @param address   出柜地址
         * @param devId     出柜存储区域
         * @param isControl 是否危险品 1，是，需要双人操作，0，否
         * @return 创建是否成功
         */
        public static APIJSON<String> createTakeOutTask(String address, String devId, int isControl) {

            try {
                Cabinet info = CabinetCore.getCabinetInfo();
                User user = CabinetCore.getCabinetUser(CabinetCore.RoleType.Operator);
                CloseableHttpClient httpClient = HttpClients.createDefault();
                List<NameValuePair> valuePairs = new ArrayList<>();
                valuePairs.add(new BasicNameValuePair("useAddress", address));
                valuePairs.add(new BasicNameValuePair("devId", devId));
                valuePairs.add(new BasicNameValuePair("isControl", String.valueOf(isControl)));
                valuePairs.add(new BasicNameValuePair("user1Id", user.id));
                HttpPost httpPost = new HttpPost(API_CREATE_TAKE_OUT_TASK);
                generalBaseHeader(httpPost);
                httpPost.setEntity(new UrlEncodedFormEntity(valuePairs));
                HttpResponse httpResponse = httpClient.execute(httpPost);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == 200) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, "utf-8");
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<String>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    try {
                        HttpEntity entity = httpResponse.getEntity();
                        String content = EntityUtils.toString(entity, "utf-8");
                        Log.d(TAG, content);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    APIJSON<String> result = new APIJSON<>();
                    result.code = code;
                    result.message = "服务器返回错误";
                    return result;
                }
            } catch (IOException e) {
                APIJSON<String> result = new APIJSON<>();
                result.code = -1;
                result.message = "网络请求异常";
                return result;
            }
        }

        /**
         * 获取使用详情列表
         *
         * @return 如果条目详情列表
         */
        public static APIJSON<List<TakeOutInfo>> getLastTakeOutTaskList() {

            try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                List<NameValuePair> valuePairs = new ArrayList<>();
                valuePairs.add(new BasicNameValuePair("pageSize", "100"));
                valuePairs.add(new BasicNameValuePair("currentPage", "1"));
                String params = EntityUtils.toString(new UrlEncodedFormEntity(valuePairs, "UTF-8"));
                HttpGet httpGet = new HttpGet(API_GET_TAKE_OUT_TASK_LIST + "?" + params);
                generalBaseHeader(httpGet);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == 200) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, "utf-8");
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<List<TakeOutInfo>>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    APIJSON<List<TakeOutInfo>> result = new APIJSON<>();
                    result.code = code;
                    result.message = "服务器返回错误";
                    return result;
                }
            } catch (IOException e) {
                APIJSON<List<TakeOutInfo>> result = new APIJSON<>();
                result.code = -1;
                result.message = "网络请求异常";
                return result;
            }
        }

        /**
         * 获取使用详情列表
         *
         * @return 如果条目详情列表
         */
        public static APIJSON<List<TakeOutItemInfo>> getTakeOutItemList(String outId) {

            try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                List<NameValuePair> valuePairs = new ArrayList<>();
                valuePairs.add(new BasicNameValuePair("outId", outId));
                valuePairs.add(new BasicNameValuePair("pageSize", "1024"));
                valuePairs.add(new BasicNameValuePair("currentPage", "1"));
                String params = EntityUtils.toString(new UrlEncodedFormEntity(valuePairs, "UTF-8"));
                HttpGet httpGet = new HttpGet(API_GET_TAKE_OUT_ITEM_LIST + "?" + params);
                generalBaseHeader(httpGet);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == 200) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, "utf-8");
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<List<TakeOutItemInfo>>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    APIJSON<List<TakeOutItemInfo>> result = new APIJSON<>();
                    result.code = code;
                    result.message = "服务器返回错误";
                    return result;
                }
            } catch (IOException e) {
                APIJSON<List<TakeOutItemInfo>> result = new APIJSON<>();
                result.code = -1;
                result.message = "网络请求异常";
                return result;
            }
        }

        /**
         * 获取使用者详细信息
         *
         * @return 使用者详细信息
         */
        public static APIJSON<StorageLaboratoryDetail> getStorageLaboratoryDetail() {

            try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                List<NameValuePair> valuePairs = new ArrayList<>();

                String params = EntityUtils.toString(new UrlEncodedFormEntity(valuePairs, "UTF-8"));
                HttpGet httpGet = new HttpGet(API_GET_STORAGE_LABORATORY_DETAIL + "?" + params);
                generalBaseHeader(httpGet);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == 200) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, "utf-8");
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<StorageLaboratoryDetail>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    APIJSON<StorageLaboratoryDetail> result = new APIJSON<>();
                    result.code = code;
                    result.message = "服务器返回错误";
                    return result;
                }
            } catch (IOException e) {
                APIJSON<StorageLaboratoryDetail> result = new APIJSON<>();
                result.code = -1;
                result.message = "网络请求异常";
                return result;
            }
        }

        /**
         * 保存使用条目详情
         *
         * @return 条目详情id
         */
        public static APIJSON<String> saveTakeOutItemDetail(TakeOutItemInfo item) {

            try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                List<NameValuePair> valuePairs = new ArrayList<>();
                valuePairs.add(new BasicNameValuePair("outId", String.valueOf(item.outId)));
                valuePairs.add(new BasicNameValuePair("conNo", item.conNo));
                valuePairs.add(new BasicNameValuePair("outWeight", item.outWeight));
                valuePairs.add(new BasicNameValuePair("purpose", item.purpose));
                HttpPost httpPost = new HttpPost(API_SAVE_TAKE_OUT_ITEM_DETAIL);
                generalBaseHeader(httpPost);
                httpPost.setEntity(new UrlEncodedFormEntity(valuePairs));
                HttpResponse httpResponse = httpClient.execute(httpPost);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == 200) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, "utf-8");
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<String>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    try {
                        HttpEntity entity = httpResponse.getEntity();
                        String content = EntityUtils.toString(entity, "utf-8");
                        Log.d(TAG, content);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    APIJSON<String> result = new APIJSON<>();
                    result.code = code;
                    result.message = "服务器返回错误";
                    return result;
                }
            } catch (IOException e) {
                APIJSON<String> result = new APIJSON<>();
                result.code = -1;
                result.message = "网络请求异常";
                return result;
            }
        }
    }

    /**
     * 使用归还模块，相关接口
     */
    public static class ReturnAfterUse {

        /**
         * 创建入柜任务
         */
        public static final String API_CREATE_USAGE_TASK = API_ROOT + "/drug/v1/usePutInfos";
        /**
         * 获取使用归还记录列表
         */
        public static final String API_GET_USAGE_DETAIL_LIST = API_ROOT + "/drug/v1/usePutDetails/pageList";
        /**
         * 获取使用者详细信息
         */
        public static final String API_GET_STORAGE_LABORATORY_DETAIL = API_ROOT + "/drug/v1/storageLaboratory/storageLaboratoryDetail/";
        /**
         * 保存试剂使用记录
         */
        public static final String API_SAVE_USAGE_DETAIL = API_ROOT + "/drug/v1/usePutDetails";
        /**
         * 获取系统中当前所有入柜任务列表
         */
        public static final String API_GET_USAGE_TASK_LIST = API_ROOT + "/drug/v1/usePutInfos/pageList";
        /**
         * 获取所有化学品容器号列表
         */
        public static final String API_GET_CONTAINER_LIST = API_ROOT + "/drug/v1/usePutInfos/getConList/";
        /**
         * 获取容器详情
         */
        public static final String API_GET_CONTAINER_DETAIL = API_ROOT + "/drug/v1/labInventoryDetail/getInfoByConNo";


        /**
         * 创建一个入柜任务
         *
         * @return 创建是否成功
         */
        public static APIJSON<String> createUsageTask() {

            try {
                Cabinet info = CabinetCore.getCabinetInfo();
                CloseableHttpClient httpClient = HttpClients.createDefault();
                List<NameValuePair> valuePairs = new ArrayList<>();
                HttpPost httpPost = new HttpPost(API_CREATE_USAGE_TASK);
                generalBaseHeader(httpPost);
                httpPost.setEntity(new UrlEncodedFormEntity(valuePairs));
                HttpResponse httpResponse = httpClient.execute(httpPost);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == 200) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, "utf-8");
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<String>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    try {
                        HttpEntity entity = httpResponse.getEntity();
                        String content = EntityUtils.toString(entity, "utf-8");
                        Log.d(TAG, content);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    APIJSON<String> result = new APIJSON<>();
                    result.code = code;
                    result.message = "服务器返回错误";
                    return result;
                }
            } catch (IOException e) {
                APIJSON<String> result = new APIJSON<>();
                result.code = -1;
                result.message = "网络请求异常";
                return result;
            }
        }

        /**
         * 获取某个容器的详情
         *
         * @return 返回详情
         */
        public static APIJSON<List<UsageItemInfo>> getContainerDetail(String conNo) {

            try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                List<NameValuePair> valuePairs = new ArrayList<>();
                valuePairs.add(new BasicNameValuePair("conNo", conNo));
                String params = EntityUtils.toString(new UrlEncodedFormEntity(valuePairs, "UTF-8"));
                HttpGet httpGet = new HttpGet(API_GET_CONTAINER_DETAIL + "?" + params);
                Log.d(TAG, httpGet.getURI().toString());
                generalBaseHeader(httpGet);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == 200) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, "utf-8");
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<List<UsageItemInfo>>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    try {
                        HttpEntity entity = httpResponse.getEntity();
                        String content = EntityUtils.toString(entity, "utf-8");
                        Log.d(TAG, content);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    APIJSON<List<UsageItemInfo>> result = new APIJSON<>();
                    result.code = code;
                    result.message = "服务器返回错误";
                    return result;
                }
            } catch (IOException e) {
                APIJSON<List<UsageItemInfo>> result = new APIJSON<>();
                result.code = -1;
                result.message = "网络请求异常";
                return result;
            }
        }

        /**
         * 获取入柜任务列表
         *
         * @return 入柜任务列表
         */
        public static APIJSON<List<UsageInfo>> getLastUsageTaskList() {

            try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                List<NameValuePair> valuePairs = new ArrayList<>();
                valuePairs.add(new BasicNameValuePair("putNo", ""));
                valuePairs.add(new BasicNameValuePair("pageSize", "100"));
                valuePairs.add(new BasicNameValuePair("currentPage", "1"));
                String params = EntityUtils.toString(new UrlEncodedFormEntity(valuePairs, "UTF-8"));
                HttpGet httpGet = new HttpGet(API_GET_USAGE_TASK_LIST + "?" + params);
                generalBaseHeader(httpGet);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == 200) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, "utf-8");
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<List<UsageInfo>>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    APIJSON<List<UsageInfo>> result = new APIJSON<>();
                    result.code = code;
                    result.message = "服务器返回错误";
                    return result;
                }
            } catch (IOException e) {
                APIJSON<List<UsageInfo>> result = new APIJSON<>();
                result.code = -1;
                result.message = "网络请求异常";
                return result;
            }
        }

        /**
         * 获得所有库存化学品列表
         *
         * @return 返回库存信息列表
         */
        public static APIJSON<List<UsageItemInfo>> getContainerNoList() {

            try {
                Cabinet cabinetInfo = CabinetCore.getCabinetInfo();
                CloseableHttpClient httpClient = HttpClients.createDefault();
                List<NameValuePair> valuePairs = new ArrayList<>();
                String params = EntityUtils.toString(new UrlEncodedFormEntity(valuePairs, "UTF-8"));
                HttpGet httpGet = new HttpGet(API_GET_CONTAINER_LIST + "?" + params);
                Log.d(TAG, httpGet.getURI().toString());
                generalBaseHeader(httpGet);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == 200) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, "utf-8");
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<List<UsageItemInfo>>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    try {
                        HttpEntity entity = httpResponse.getEntity();
                        String content = EntityUtils.toString(entity, "utf-8");
                        Log.d(TAG, content);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    APIJSON<List<UsageItemInfo>> result = new APIJSON<>();
                    result.code = code;
                    result.message = "服务器返回错误";
                    return result;
                }
            } catch (IOException e) {
                APIJSON<List<UsageItemInfo>> result = new APIJSON<>();
                result.code = -1;
                result.message = "网络请求异常";
                return result;
            }
        }

        /**
         * 获取使用详情列表
         *
         * @return 如果条目详情列表
         */
        public static APIJSON<List<UsageItemInfo>> getUsageItemList(String putId) {

            try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                List<NameValuePair> valuePairs = new ArrayList<>();
                valuePairs.add(new BasicNameValuePair("putId", putId));
                valuePairs.add(new BasicNameValuePair("pageSize", "1024"));
                valuePairs.add(new BasicNameValuePair("currentPage", "1"));
                String params = EntityUtils.toString(new UrlEncodedFormEntity(valuePairs, "UTF-8"));
                HttpGet httpGet = new HttpGet(API_GET_USAGE_DETAIL_LIST + "?" + params);
                generalBaseHeader(httpGet);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == 200) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, "utf-8");
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<List<UsageItemInfo>>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    APIJSON<List<UsageItemInfo>> result = new APIJSON<>();
                    result.code = code;
                    result.message = "服务器返回错误";
                    return result;
                }
            } catch (IOException e) {
                APIJSON<List<UsageItemInfo>> result = new APIJSON<>();
                result.code = -1;
                result.message = "网络请求异常";
                return result;
            }
        }

        /**
         * 获取出柜者详细信息
         *
         * @return 出柜者详细信息
         */
        public static APIJSON<StorageLaboratoryDetail> getStorageLaboratoryDetail() {

            try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                List<NameValuePair> valuePairs = new ArrayList<>();
                String params = EntityUtils.toString(new UrlEncodedFormEntity(valuePairs, "UTF-8"));
                HttpGet httpGet = new HttpGet(API_GET_STORAGE_LABORATORY_DETAIL + "?" + params);
                generalBaseHeader(httpGet);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == 200) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, "utf-8");
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<StorageLaboratoryDetail>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    APIJSON<StorageLaboratoryDetail> result = new APIJSON<>();
                    result.code = code;
                    result.message = "服务器返回错误";
                    return result;
                }
            } catch (IOException e) {
                APIJSON<StorageLaboratoryDetail> result = new APIJSON<>();
                result.code = -1;
                result.message = "网络请求异常";
                return result;
            }
        }

        /**
         * 保存使用条目详情
         *
         * @return 条目详情id
         */
        public static APIJSON<String> saveUsageItemDetail(UsageItemInfo item) {

            try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                List<NameValuePair> valuePairs = new ArrayList<>();
                valuePairs.add(new BasicNameValuePair("conNo", item.conNo));
                valuePairs.add(new BasicNameValuePair("putId", item.putId));
                valuePairs.add(new BasicNameValuePair("devId", String.valueOf(item.devId)));
                valuePairs.add(new BasicNameValuePair("putWeight", item.putWeight));
                valuePairs.add(new BasicNameValuePair("tankId", item.tankId));
                HttpPost httpPost = new HttpPost(API_SAVE_USAGE_DETAIL);
                generalBaseHeader(httpPost);
                httpPost.setEntity(new UrlEncodedFormEntity(valuePairs));
                HttpResponse httpResponse = httpClient.execute(httpPost);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == 200) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, "utf-8");
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<String>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    try {
                        HttpEntity entity = httpResponse.getEntity();
                        String content = EntityUtils.toString(entity, "utf-8");
                        Log.d(TAG, content);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    APIJSON<String> result = new APIJSON<>();
                    result.code = code;
                    result.message = "服务器返回错误";
                    return result;
                }
            } catch (IOException e) {
                APIJSON<String> result = new APIJSON<>();
                result.code = -1;
                result.message = "网络请求异常";
                return result;
            }
        }
    }

    /**
     * 库存查询模块，相关接口
     */
    public static class InventoryQuery {

        /**
         * 当前库存列表
         */
        public static final String API_INVENTORY_QUERY_LIST = API_ROOT + "/drug/v1/labInventory/getInventoryForApp";
        /**
         * 库存查询模块，库存药品详情
         */
        public static final String API_INVENTORY_QUERY_DETAIL = API_ROOT + "/drug/v1/labInventory/getInventoryDetailForApp";

        /**
         * 库存列表
         *
         * @param chemicalName 中文名，可通过化学品名模糊查询
         * @return 返回库存列表
         */
        public static APIJSON<List<InventoryItem>> queryInventoryList(String chemicalName, String currentPage) {

            try {
                Cabinet cabinetInfo = CabinetCore.getCabinetInfo();
                CloseableHttpClient httpClient = HttpClients.createDefault();
                List<NameValuePair> valuePairs = new ArrayList<>();
                valuePairs.add(new BasicNameValuePair("chemicalName", chemicalName));
                valuePairs.add(new BasicNameValuePair("pageSize", "1024"));
                valuePairs.add(new BasicNameValuePair("currentPage", currentPage));
                String params = EntityUtils.toString(new UrlEncodedFormEntity(valuePairs, "UTF-8"));
                HttpGet httpGet = new HttpGet(API_INVENTORY_QUERY_LIST + "?" + params);
                Log.d(TAG, httpGet.getURI().toString());
                generalBaseHeader(httpGet);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == 200) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, "utf-8");
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<List<InventoryItem>>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    try {
                        HttpEntity entity = httpResponse.getEntity();
                        String content = EntityUtils.toString(entity, "utf-8");
                        Log.d(TAG, content);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    APIJSON<List<InventoryItem>> result = new APIJSON<>();
                    result.code = code;
                    result.message = "服务器返回错误";
                    return result;
                }
            } catch (IOException e) {
                APIJSON<List<InventoryItem>> result = new APIJSON<>();
                result.code = -1;
                result.message = "网络请求异常";
                return result;
            }
        }

        /**
         * 库存详情
         *
         * @param chemicalId 化学平Id
         * @return 返回库存信息列表
         */
        public static APIJSON<List<InventoryDetail>> queryInventoryDetail(String chemicalId, String currentPage) {

            try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                List<NameValuePair> valuePairs = new ArrayList<>();
                valuePairs.add(new BasicNameValuePair("chemicalId", chemicalId));
                valuePairs.add(new BasicNameValuePair("pageSize", "1024"));
                valuePairs.add(new BasicNameValuePair("currentPage", currentPage));
                String params = EntityUtils.toString(new UrlEncodedFormEntity(valuePairs, "UTF-8"));
                HttpGet httpGet = new HttpGet(API_INVENTORY_QUERY_DETAIL + "?" + params);
                Log.d(TAG, httpGet.getURI().toString());
                generalBaseHeader(httpGet);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == 200) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, "utf-8");
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<List<InventoryDetail>>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    try {
                        HttpEntity entity = httpResponse.getEntity();
                        String content = EntityUtils.toString(entity, "utf-8");
                        Log.d(TAG, content);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    APIJSON<List<InventoryDetail>> result = new APIJSON<>();
                    result.code = code;
                    result.message = "服务器返回错误";
                    return result;
                }
            } catch (IOException e) {
                APIJSON<List<InventoryDetail>> result = new APIJSON<>();
                result.code = -1;
                result.message = "网络请求异常";
                return result;
            }
        }
    }

    /**
     * 初次存入模块，相关接口
     */
    public static class Storage {
        /**
         * 获取入柜单号
         */
        public static final String API_GET_DEPOSIT_NO = API_ROOT + "/drug/v1/putLabInfo/getPutNo/";
        /**
         * 保存入柜信息
         */
        public static final String API_SAVE_DEPOSIT = API_ROOT + "/drug/v1/putLabInfo/savePutLabInfo";
        /**
         * 获取入柜药品列表
         */
        public static final String API_GET_DEPOSIT_ITEM_LIST = API_ROOT + "/drug/v1/putLabDetail/putLabDetailPageList";
        /**
         * 提交入柜信息
         */
        public static final String API_COMMIT_DEPOSIT = API_ROOT + "/drug/v1/putLabInfo/putLabConfirm/";
        /**
         * 保存药品入库记录
         */
        public static final String API_SAVE_DEPOSIT_ITEM_DETAIL = API_ROOT + "/drug/v1/putLabDetail/savePutLabDetail";
        /**
         * 获得容器编号
         */
        public static final String API_GET_CONTAINER_NO = API_ROOT + "/drug/v1/putLabInfo/getNum/";
        /**
         * 移除试剂入库记录
         */
        public static final String API_REMOVE_PUT_LAB_DETAIL = API_ROOT + "/drug/v1/putLabDetail/removePutLabDetail/";

        /**
         * 获取入柜药品列表
         *
         * @return 当前入库单下药品列表
         */
        public static APIJSON<List<DepositItem>> getDepositItemList(String putId) {

            try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                List<NameValuePair> valuePairs = new ArrayList<>();
                valuePairs.add(new BasicNameValuePair("putId", putId));
                String params = EntityUtils.toString(new UrlEncodedFormEntity(valuePairs, "UTF-8"));
                HttpGet httpGet = new HttpGet(API_GET_DEPOSIT_ITEM_LIST + "?" + params);
                generalBaseHeader(httpGet);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == 200) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, "utf-8");
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<List<DepositItem>>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    APIJSON<List<DepositItem>> result = new APIJSON<>();
                    result.code = code;
                    result.message = "服务器返回错误";
                    return result;
                }
            } catch (IOException e) {
                APIJSON<List<DepositItem>> result = new APIJSON<>();
                result.code = -1;
                result.message = "网络请求异常";
                return result;
            }
        }

        /**
         * 获取入柜编号
         *
         * @return 入柜编号
         */
        public static APIJSON<String> getDepositNo() {

            try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                List<NameValuePair> valuePairs = new ArrayList<>();
                String params = EntityUtils.toString(new UrlEncodedFormEntity(valuePairs, "UTF-8"));
                HttpGet httpGet = new HttpGet(API_GET_DEPOSIT_NO + "?" + params);
                generalBaseHeader(httpGet);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == 200) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, "utf-8");
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<String>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    APIJSON<String> result = new APIJSON<>();
                    result.code = code;
                    result.message = "服务器返回错误";
                    return result;
                }
            } catch (IOException e) {
                APIJSON<String> result = new APIJSON<>();
                result.code = -1;
                result.message = "网络请求异常";
                return result;
            }
        }

        /**
         * 创建入柜单号后，立刻保存入柜单号，获取入柜ID
         *
         * @return 入柜ID
         */
        public static APIJSON<String> saveDeposit(String putNo, String putNum, String totalAmount) {

            try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                List<NameValuePair> valuePairs = new ArrayList<>();
                valuePairs.add(new BasicNameValuePair("putNo", putNo));
                valuePairs.add(new BasicNameValuePair("putNum", putNum));
                valuePairs.add(new BasicNameValuePair("totalAmount", totalAmount));
                HttpPost httpPost = new HttpPost(API_SAVE_DEPOSIT);
                generalBaseHeader(httpPost);
                httpPost.setEntity(new UrlEncodedFormEntity(valuePairs));
                HttpResponse httpResponse = httpClient.execute(httpPost);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == 200) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, "utf-8");
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<String>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    try {
                        HttpEntity entity = httpResponse.getEntity();
                        String content = EntityUtils.toString(entity, "utf-8");
                        Log.d(TAG, content);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    APIJSON<String> result = new APIJSON<>();
                    result.code = code;
                    result.message = "服务器返回错误";
                    return result;
                }
            } catch (IOException e) {
                APIJSON<String> result = new APIJSON<>();
                result.code = -1;
                result.message = "网络请求异常";
                return result;
            }
        }

        /**
         * 提交初次入柜
         *
         * @return 提交结果
         */
        public static APIJSON<String> commitDeposit(String putId) {

            try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                List<NameValuePair> valuePairs = new ArrayList<>();
                valuePairs.add(new BasicNameValuePair("putId", putId));
                String params = EntityUtils.toString(new UrlEncodedFormEntity(valuePairs, "UTF-8"));
                HttpGet httpGet = new HttpGet(API_COMMIT_DEPOSIT + putId + "?" + params);
                generalBaseHeader(httpGet);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == 200) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, "utf-8");
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<String>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    try {
                        HttpEntity entity = httpResponse.getEntity();
                        String content = EntityUtils.toString(entity, "utf-8");
                        Log.d(TAG, content);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    APIJSON<String> result = new APIJSON<>();
                    result.code = code;
                    result.message = "服务器返回错误";
                    return result;
                }
            } catch (IOException e) {
                APIJSON<String> result = new APIJSON<>();
                result.code = -1;
                result.message = "网络请求异常";
                return result;
            }
        }

        /**
         * 保存入柜条目详情
         *
         * @return 条目详情id
         */
        public static APIJSON<String> saveDepositItemDetail(DepositItem item) {

            try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                List<NameValuePair> valuePairs = new ArrayList<>();
                valuePairs.add(new BasicNameValuePair("putId", String.valueOf(item.putId)));
                valuePairs.add(new BasicNameValuePair("conNo", item.conNo));
                valuePairs.add(new BasicNameValuePair("chemicalId", item.chemicalId));
                valuePairs.add(new BasicNameValuePair("dateProduction", item.dateProduction));
                valuePairs.add(new BasicNameValuePair("specification", item.specification));
                valuePairs.add(new BasicNameValuePair("purity", item.purity));
                valuePairs.add(new BasicNameValuePair("cisNo", item.casNo));
                valuePairs.add(new BasicNameValuePair("supplier", item.supplier));
                valuePairs.add(new BasicNameValuePair("periodValidity", item.periodValidity));
                valuePairs.add(new BasicNameValuePair("weight", item.weight));
                valuePairs.add(new BasicNameValuePair("unit", item.unit));
                valuePairs.add(new BasicNameValuePair("devId", item.devId));
                HttpPost httpPost = new HttpPost(API_SAVE_DEPOSIT_ITEM_DETAIL);
                generalBaseHeader(httpPost);
                httpPost.setEntity(new UrlEncodedFormEntity(valuePairs));
                HttpResponse httpResponse = httpClient.execute(httpPost);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == 200) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, "utf-8");
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<String>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    try {
                        HttpEntity entity = httpResponse.getEntity();
                        String content = EntityUtils.toString(entity, "utf-8");
                        Log.d(TAG, content);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    APIJSON<String> result = new APIJSON<>();
                    result.code = code;
                    result.message = "服务器返回错误";
                    return result;
                }
            } catch (IOException e) {
                APIJSON<String> result = new APIJSON<>();
                result.code = -1;
                result.message = "网络请求异常";
                return result;
            }
        }

        /**
         * 获取化学品容器编号
         *
         * @param putId 入柜编号
         * @return 容器编号
         */
        public static APIJSON<String> getContainerNo(String putId) {

            try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                List<NameValuePair> valuePairs = new ArrayList<>();
                valuePairs.add(new BasicNameValuePair("putId", putId));
                String params = EntityUtils.toString(new UrlEncodedFormEntity(valuePairs, "UTF-8"));
                HttpGet httpGet = new HttpGet(API_GET_CONTAINER_NO + putId + "?" + params);
                Log.d(TAG, httpGet.getURI().toString());
                generalBaseHeader(httpGet);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == 200) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, "utf-8");
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<String>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    try {
                        HttpEntity entity = httpResponse.getEntity();
                        String content = EntityUtils.toString(entity, "utf-8");
                        Log.d(TAG, content);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    APIJSON<String> result = new APIJSON<>();
                    result.code = code;
                    result.message = "服务器返回错误";
                    return result;
                }
            } catch (IOException e) {
                APIJSON<String> result = new APIJSON<>();
                result.code = -1;
                result.message = "网络请求异常";
                return result;
            }
        }

        /**
         * 删除一条入柜详情
         *
         * @param detailId 详情编号
         * @return 返回结果
         */
        public static APIJSON<String> removeDepositItem(String detailId) {

            try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                List<NameValuePair> valuePairs = new ArrayList<>();
                valuePairs.add(new BasicNameValuePair("detailId", detailId));
                String params = EntityUtils.toString(new UrlEncodedFormEntity(valuePairs, "UTF-8"));
                HttpDelete httpDelete = new HttpDelete(API_REMOVE_PUT_LAB_DETAIL + detailId + "?" + params);
                Log.d(TAG, httpDelete.getURI().toString());
                generalBaseHeader(httpDelete);
                HttpResponse httpResponse = httpClient.execute(httpDelete);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == 200) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, "utf-8");
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<String>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    try {
                        HttpEntity entity = httpResponse.getEntity();
                        String content = EntityUtils.toString(entity, "utf-8");
                        Log.d(TAG, content);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    APIJSON<String> result = new APIJSON<>();
                    result.code = code;
                    result.message = "服务器返回错误";
                    return result;
                }
            } catch (IOException e) {
                APIJSON<String> result = new APIJSON<>();
                result.code = -1;
                result.message = "网络请求异常";
                return result;
            }
        }
    }

    /**
     * 出入库查询(台账查询)模块，相关接口
     */
    public static class StandingBook {

        /**
         * 台账-入柜
         */
        public static final String API_STANDING_BOOK_DEPOSIT = API_ROOT + "/drug/v1/statistic/getLabPutDevStatisticByTankId";
        /**
         * 台账-使用
         */
        public static final String API_STANDING_BOOK_USAGE = API_ROOT + "/drug/v1/statistic/getLabUseStatisticById";
        /**
         * 台账-出柜
         */
        public static final String API_STANDING_BOOK_TAKE_OUT = API_ROOT + "/drug/v1/statistic/getUseOutDetailDatas";
        /**
         * 台账-入柜
         */
        public static final String API_STANDING_BOOK_TAKE_IN = API_ROOT + "/drug/v1/statistic/getUsePutDetailDatas";

        /**
         * 入柜台账查询
         *
         * @return 返回入柜台账
         */
        public static APIJSON<List<StandingBookItem>> queryStandingBookDeposit(String currentPage) {
            try {
                Cabinet cabinet = CabinetCore.getCabinetInfo();
                CloseableHttpClient httpClient = HttpClients.createDefault();
                List<NameValuePair> valuePairs = new ArrayList<>();
                valuePairs.add(new BasicNameValuePair("pageSize", "20"));
                valuePairs.add(new BasicNameValuePair("currentPage", currentPage));
//                valuePairs.add(new BasicNameValuePair("devId", cabinetInfo.devId));
                String params = EntityUtils.toString(new UrlEncodedFormEntity(valuePairs, "UTF-8"));
                HttpGet httpGet = new HttpGet(API_STANDING_BOOK_DEPOSIT + "?" + params);
                Log.d(TAG, httpGet.getURI().toString());
                generalBaseHeader(httpGet);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == 200) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, "utf-8");
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<List<StandingBookItem>>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    try {
                        HttpEntity entity = httpResponse.getEntity();
                        String content = EntityUtils.toString(entity, "utf-8");
                        Log.d(TAG, content);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    APIJSON<List<StandingBookItem>> result = new APIJSON<>();
                    result.code = code;
                    result.message = "服务器返回错误";
                    return result;
                }
            } catch (IOException e) {
                APIJSON<List<StandingBookItem>> result = new APIJSON<>();
                result.code = -1;
                result.message = "网络请求异常";
                return result;
            }
        }

        /**
         * 使用台账查询
         *
         * @return 返回入柜台账
         */
        public static APIJSON<List<StandingBookItem>> queryStandingBookUsage(String currentPage) {
            try {
                Cabinet cabinet = CabinetCore.getCabinetInfo();
                CloseableHttpClient httpClient = HttpClients.createDefault();
                List<NameValuePair> valuePairs = new ArrayList<>();
                valuePairs.add(new BasicNameValuePair("pageSize", "20"));
                valuePairs.add(new BasicNameValuePair("currentPage", currentPage));
//                valuePairs.add(new BasicNameValuePair("devId", cabinetInfo.devId));
                String params = EntityUtils.toString(new UrlEncodedFormEntity(valuePairs, "UTF-8"));
                HttpGet httpGet = new HttpGet(API_STANDING_BOOK_USAGE + "?" + params);
                Log.d(TAG, httpGet.getURI().toString());
                generalBaseHeader(httpGet);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == 200) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, "utf-8");
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<List<StandingBookItem>>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    try {
                        HttpEntity entity = httpResponse.getEntity();
                        String content = EntityUtils.toString(entity, "utf-8");
                        Log.d(TAG, content);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    APIJSON<List<StandingBookItem>> result = new APIJSON<>();
                    result.code = code;
                    result.message = "服务器返回错误";
                    return result;
                }
            } catch (IOException e) {
                APIJSON<List<StandingBookItem>> result = new APIJSON<>();
                result.code = -1;
                result.message = "网络请求异常";
                return result;
            }
        }

        /**
         * 台账-出柜
         *
         * @return 返回出柜台账
         */
        public static APIJSON<List<StandingBookItem>> queryStandingBookTakeOut(String currentPage) {
            try {
                Cabinet cabinet = CabinetCore.getCabinetInfo();
                CloseableHttpClient httpClient = HttpClients.createDefault();
                List<NameValuePair> valuePairs = new ArrayList<>();
                valuePairs.add(new BasicNameValuePair("pageSize", "20"));
                valuePairs.add(new BasicNameValuePair("currentPage", currentPage));
//                valuePairs.add(new BasicNameValuePair("devId", cabinetInfo.devId));
                String params = EntityUtils.toString(new UrlEncodedFormEntity(valuePairs, "UTF-8"));
                HttpGet httpGet = new HttpGet(API_STANDING_BOOK_TAKE_OUT + "?" + params);
                Log.d(TAG, httpGet.getURI().toString());
                generalBaseHeader(httpGet);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == 200) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, "utf-8");
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<List<StandingBookItem>>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    try {
                        HttpEntity entity = httpResponse.getEntity();
                        String content = EntityUtils.toString(entity, "utf-8");
                        Log.d(TAG, content);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    APIJSON<List<StandingBookItem>> result = new APIJSON<>();
                    result.code = code;
                    result.message = "服务器返回错误";
                    return result;
                }
            } catch (IOException e) {
                APIJSON<List<StandingBookItem>> result = new APIJSON<>();
                result.code = -1;
                result.message = "网络请求异常";
                return result;
            }
        }

        /**
         * 台账-入柜
         *
         * @return 返回入柜台账
         */
        public static APIJSON<List<StandingBookItem>> queryStandingBookTakeIn(String currentPage) {
            try {
                Cabinet cabinet = CabinetCore.getCabinetInfo();
                CloseableHttpClient httpClient = HttpClients.createDefault();
                List<NameValuePair> valuePairs = new ArrayList<>();
                valuePairs.add(new BasicNameValuePair("pageSize", "20"));
                valuePairs.add(new BasicNameValuePair("currentPage", currentPage));
//                valuePairs.add(new BasicNameValuePair("devId", cabinetInfo.devId));
                String params = EntityUtils.toString(new UrlEncodedFormEntity(valuePairs, "UTF-8"));
                HttpGet httpGet = new HttpGet(API_STANDING_BOOK_TAKE_IN + "?" + params);
                Log.d(TAG, httpGet.getURI().toString());
                generalBaseHeader(httpGet);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == 200) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, "utf-8");
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<List<StandingBookItem>>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    try {
                        HttpEntity entity = httpResponse.getEntity();
                        String content = EntityUtils.toString(entity, "utf-8");
                        Log.d(TAG, content);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    APIJSON<List<StandingBookItem>> result = new APIJSON<>();
                    result.code = code;
                    result.message = "服务器返回错误";
                    return result;
                }
            } catch (IOException e) {
                APIJSON<List<StandingBookItem>> result = new APIJSON<>();
                result.code = -1;
                result.message = "网络请求异常";
                return result;
            }
        }
    }

    public static class System {

        /**
         * 登录
         */
        public static final String API_LOGIN = API_ROOT + "/login";
        /**
         * 用户名下一体机设备搜索
         */
        public static final String API_CABINET_LIST = API_ROOT + "/findCabinetByAdmin";

        /**
         * 获取柜子绑定摄像头列表
         */
        public static final String API_GET_CAMERA_LIST = API_ROOT + "/drug/v1/YSYVoder/getCameraStreamForApp/";

        /**
         * 查询管理员名下所有暂存柜列表
         *
         * @param userId 管理员Id
         * @param token  token
         * @return 暂存柜列表
         */
        public static APIJSON<List<Cabinet>> getCabinetList(String userId, String token) {
            try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                HttpPost httpPost = new HttpPost(API_CABINET_LIST);
                List<NameValuePair> valuePairs = new ArrayList<>();
                valuePairs.add(new BasicNameValuePair("userId", userId));
                valuePairs.add(new BasicNameValuePair("token", token));
                httpPost.setEntity(new UrlEncodedFormEntity(valuePairs, "utf-8"));
                Log.d(TAG, httpPost.getURI().toString());
                Log.d(TAG, httpPost.getEntity().toString());
                generalBaseHeader(httpPost);
                HttpResponse httpResponse = httpClient.execute(httpPost);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == 200) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, "utf-8");
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<List<Cabinet>>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    try {
                        HttpEntity entity = httpResponse.getEntity();
                        String content = EntityUtils.toString(entity, "utf-8");
                        Log.d(TAG, content);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    APIJSON<List<Cabinet>> result = new APIJSON<>();
                    result.code = code;
                    result.message = "服务器返回错误";
                    return result;
                }
            } catch (Exception e) {
                APIJSON<List<Cabinet>> result = new APIJSON<>();
                result.code = -1;
                result.message = e.getMessage();
                return result;
            }
        }

        /**
         * 查询柜子下所有摄像头列表
         *
         * @param tankId TankId
         * @return 监控摄像头列表
         */
        public static APIJSON<List<SurveillanceCamera>> getCameraList(String tankId) {
            try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                List<NameValuePair> valuePairs = new ArrayList<>();
                valuePairs.add(new BasicNameValuePair("tankId", tankId));
                String params = EntityUtils.toString(new UrlEncodedFormEntity(valuePairs, "UTF-8"));
                HttpGet httpGet = new HttpGet(API_GET_CAMERA_LIST + tankId + "?" + params);
                Log.d(TAG, httpGet.getURI().toString());
                generalBaseHeader(httpGet);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == 200) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, "utf-8");
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<List<SurveillanceCamera>>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    try {
                        HttpEntity entity = httpResponse.getEntity();
                        String content = EntityUtils.toString(entity, "utf-8");
                        Log.d(TAG, content);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    APIJSON<List<SurveillanceCamera>> result = new APIJSON<>();
                    result.code = code;
                    result.message = "服务器返回错误";
                    return result;
                }
            } catch (Exception e) {
                APIJSON<List<SurveillanceCamera>> result = new APIJSON<>();
                result.code = -1;
                result.message = e.getMessage();
                return result;
            }
        }

        /**
         * 登录
         *
         * @param account     账号名
         * @param passwordMD5 账号密码MD5
         * @return 登录结果信息
         */
        public static APIJSON<User> login(String account, String passwordMD5) {

            try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                HttpPost httpPost = new HttpPost(API_LOGIN);
                List<NameValuePair> valuePairs = new ArrayList<>();
                valuePairs.add(new BasicNameValuePair("account", account));
                valuePairs.add(new BasicNameValuePair("password", passwordMD5));
                valuePairs.add(new BasicNameValuePair("platformFlag", "saas"));
                valuePairs.add(new BasicNameValuePair("randomCode", String.valueOf(_Random.nextInt())));
                valuePairs.add(new BasicNameValuePair("appFlag", "app"));
                httpPost.setEntity(new UrlEncodedFormEntity(valuePairs));
                Log.d(TAG, httpPost.getURI().toString());
                Log.d(TAG, httpPost.getEntity().toString());
                HttpResponse httpResponse = httpClient.execute(httpPost);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == 200) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, "utf-8");
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<User>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    APIJSON<User> result = new APIJSON<>();
                    result.code = code;
                    result.message = "服务器返回错误";
                    return result;
                }
            } catch (Exception e) {
                APIJSON<User> result = new APIJSON<>();
                result.code = -1;
                result.message = e.getMessage();
                return result;
            }
        }
    }
}
