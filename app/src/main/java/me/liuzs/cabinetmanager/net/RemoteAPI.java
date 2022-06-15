package me.liuzs.cabinetmanager.net;

import android.util.Log;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpHeaders;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.client.methods.HttpPut;
import cz.msebera.android.httpclient.client.methods.HttpRequestBase;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClients;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.util.EntityUtils;
import cz.msebera.android.httpclient.util.TextUtils;
import me.liuzs.cabinetmanager.CabinetCore;
import me.liuzs.cabinetmanager.StandingBookActivity;
import me.liuzs.cabinetmanager.model.Agency;
import me.liuzs.cabinetmanager.model.Chemical;
import me.liuzs.cabinetmanager.model.ContainerNoBatchInfo;
import me.liuzs.cabinetmanager.model.DepositRecord;
import me.liuzs.cabinetmanager.model.FilterTime;
import me.liuzs.cabinetmanager.model.User;

public class RemoteAPI {

    public static final String TAG = "RemoteAPI";

    public static final int HTTP_OK = 200;

    public static final String UTF_8 = "UTF-8";

    /**
     * HTTP接口Root地址
     */
    public static final String API_ROOT = "https://idburg.cn:8090";

    public static final String APP_DOWNLOAD = API_ROOT + "/app_download/hazardous_waste_cabinet_manage.apk";

    private static void generalOptBaseHeader(HttpRequestBase httpRequest) {
        Header accept = new BasicHeader(HttpHeaders.ACCEPT, "application/json");
        httpRequest.addHeader(accept);
        User user = CabinetCore.getCabinetUser(CabinetCore.RoleType.Operator);
        if (user != null) {
            Header authorization = new BasicHeader("token", user.token);
            httpRequest.addHeader(authorization);
        }
    }

    private static void generalAdminBaseHeader(HttpRequestBase httpRequest) {
        Header accept = new BasicHeader(HttpHeaders.ACCEPT, "application/json");
        httpRequest.addHeader(accept);
        User user = CabinetCore.getCabinetUser(CabinetCore.RoleType.Admin);
        if (user != null) {
            Header authorization = new BasicHeader("token", user.token);
            httpRequest.addHeader(authorization);
        }
    }

    /**
     * MQTT相关设定
     */
    public static class MQTT {

        /**
         * MQTT Root地址
         */
        public static final String MQTT_ROOT = "tcp://47.104.235.225:1883";
        public static final String Topic_Hardware_Value_Publish = "storage-env";
        public static final String Topic_Control_Subscribe = "storage-ctr-";
        public static final String Topic_SetupValue_Subscribe = "storage-set-";
        public static final String PublishTopicPre = "/one2one/device/";
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
                generalOptBaseHeader(httpGet);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == HTTP_OK) {
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
                    result.status = APIJSON.Status.ok;
                    result.error = "服务器返回错误";
                    return result;
                }
            } catch (IOException e) {
                APIJSON<List<Chemical>> result = new APIJSON<>();
                result.status = APIJSON.Status.other;
                result.error = "网络请求异常";
                return result;
            }
        }

    }

    /**
     * 单号管理
     */
    public static class ContainerNoManager {

        /**
         * 创建单号批次
         */
        public static final String API_CREATE_CONTAINER_NO_BATCH = API_ROOT + "/api/storage_no_batches";
        /**
         * 获取单号批次列表
         */
        public static final String API_GET_CONTAINER_NO_BATCH_LIST = API_ROOT + "/api/storage_no_batches";
        /**
         * 获取单号批次下单号列表
         */
        public static final String API_GET_CONTAINER_NO_BATCH_NO_LIST = API_ROOT + "/api/storage_no_batches/";


        /**
         * 创建一个入柜任务
         *
         * @return 创建是否成功
         */
        public static APIJSON<ContainerNoBatchInfo> createContainerNoBatch(String name, String amount) {

            try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                HttpPost method = new HttpPost(API_CREATE_CONTAINER_NO_BATCH);
                List<NameValuePair> valuePairs = new ArrayList<>();
                valuePairs.add(new BasicNameValuePair("storage_no_batch[batch_name]", name));
                valuePairs.add(new BasicNameValuePair("storage_no_batch[amount]", amount));
                method.setEntity(new UrlEncodedFormEntity(valuePairs, UTF_8));
                Log.d(TAG, method.getURI().toString());
                Log.d(TAG, valuePairs.toString());
                generalOptBaseHeader(method);
                HttpResponse httpResponse = httpClient.execute(method);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == HTTP_OK) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, UTF_8);
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<ContainerNoBatchInfo>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    //noinspection unchecked
                    return APIJSON.buildServerErrorJSON(code);
                }
            } catch (Exception e) {
                //noinspection unchecked
                return APIJSON.buildOtherErrorJSON(e);
            }
        }

        /**
         * 创建一个入柜任务
         *
         * @return 创建是否成功
         */
        public static APIJSON<ContainerNoBatchListJSON> getContainerNoBatchList(String batchName, String operator, int pageSize, int pageIndex) {

            try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                List<NameValuePair> valuePairs = new ArrayList<>();
                valuePairs.add(new BasicNameValuePair("batch_name", batchName));
                valuePairs.add(new BasicNameValuePair("creator", operator));
                valuePairs.add(new BasicNameValuePair("page_size", String.valueOf(pageSize)));
                valuePairs.add(new BasicNameValuePair("page", String.valueOf(pageIndex)));
                String params = EntityUtils.toString(new UrlEncodedFormEntity(valuePairs, "UTF-8"));
                HttpGet method = new HttpGet(API_GET_CONTAINER_NO_BATCH_LIST + "?" + params);
                Log.d(TAG, method.getURI().toString());
                Log.d(TAG, valuePairs.toString());
                generalOptBaseHeader(method);
                HttpResponse httpResponse = httpClient.execute(method);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == HTTP_OK) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, UTF_8);
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<ContainerNoBatchListJSON>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    //noinspection unchecked
                    return APIJSON.buildServerErrorJSON(code);
                }
            } catch (Exception e) {
                //noinspection unchecked
                return APIJSON.buildOtherErrorJSON(e);
            }
        }

        public static APIJSON<ContainerNoBatchInfo> getContainerNoList(String batchId) {

            try {
                User user = CabinetCore.getCabinetUser(CabinetCore.RoleType.Operator);
                CloseableHttpClient httpClient = HttpClients.createDefault();

                List<NameValuePair> valuePairs = new ArrayList<>();
                assert user != null;
                valuePairs.add(new BasicNameValuePair("userId", user.id));
                valuePairs.add(new BasicNameValuePair("batch_id", batchId));
                String params = EntityUtils.toString(new UrlEncodedFormEntity(valuePairs, UTF_8));
                HttpGet method = new HttpGet(API_GET_CONTAINER_NO_BATCH_NO_LIST + batchId + "?" + params);
                Log.d(TAG, method.getURI().toString());
                Log.d(TAG, valuePairs.toString());
                generalOptBaseHeader(method);
                HttpResponse httpResponse = httpClient.execute(method);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == HTTP_OK) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, UTF_8);
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<ContainerNoBatchInfo>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    //noinspection unchecked
                    return APIJSON.buildServerErrorJSON(code);
                }
            } catch (Exception e) {
                //noinspection unchecked
                return APIJSON.buildOtherErrorJSON(e);
            }
        }
    }

    /**
     * 记录查询
     */
    public static class DepositRecordQuery {

        /**
         * 库存列表
         */
        //public static final String API_INVENTORY_QUERY_LIST = API_ROOT + "/api/storage_record_book";
        public static final String API_INVENTORY_QUERY_LIST = API_ROOT + "/api/storages/%s/storage_records/%s";

        /**
         * 库存列表
         *
         * @return 返回库存列表
         */
        public static APIJSON<DepositRecordListJSON> queryDepositList(StandingBookActivity.DetailType detailType, int pageSize, int currentPage) {

            try {
                String type = null;
                switch (detailType) {
                    case Inventories:
                        type = "stocks";
                        break;
                    case Deposit:
                        type = "inputs";
                        break;
                    case TakeOut:
                        type = "outputs";
                        break;
                }
                CloseableHttpClient httpClient = HttpClients.createDefault();
                List<NameValuePair> valuePairs = new ArrayList<>();
                valuePairs.add(new BasicNameValuePair("page_size", String.valueOf(pageSize)));
                valuePairs.add(new BasicNameValuePair("page", String.valueOf(currentPage)));
                String params = EntityUtils.toString(new UrlEncodedFormEntity(valuePairs, "UTF-8"));
                String api_url = String.format(API_INVENTORY_QUERY_LIST, Objects.requireNonNull(CabinetCore.getCabinetInfo()).id, type);
                HttpGet method = new HttpGet(api_url + "?" + params);
                Log.d(TAG, method.getURI().toString());
                generalOptBaseHeader(method);
                HttpResponse httpResponse = httpClient.execute(method);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == HTTP_OK) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, UTF_8);
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<DepositRecordListJSON>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    //noinspection unchecked
                    return APIJSON.buildServerErrorJSON(code);
                }
            } catch (Exception e) {
                //noinspection unchecked
                return APIJSON.buildOtherErrorJSON(e);
            }

        }
    }

    /**
     * 出入柜模块
     */
    public static class Deposit {

        /**
         * 获取暂存柜记录列表
         */
        public static final String API_DEPOSIT_LIST = API_ROOT + "/api/storage_nos/%s/storage_record";

        /**
         * 提交入柜信息
         */
        public static final String API_TAKE_IN = API_ROOT + "/api/storages/%s/storage_records/opt_input";

        /**
         * 提交出柜信息
         */
        public static final String API_TAKE_OUT = API_ROOT + "/api/storages/%s/storage_records/opt_output";


        /**
         * 提交出柜
         *
         * @return 提交结果
         */
        public static APIJSON<DepositRecord> takeOutDeposit(DepositRecord depositRecord) {

            try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                String api_url = String.format(API_TAKE_OUT, Objects.requireNonNull(CabinetCore.getCabinetInfo()).id);
                HttpPost method = new HttpPost(api_url);
                List<NameValuePair> valuePairs = new ArrayList<>();
                valuePairs.add(new BasicNameValuePair("storage_record[storage_no]", depositRecord.storage_no));
                valuePairs.add(new BasicNameValuePair("storage_record[output_weight]", depositRecord.output_weight));
                method.setEntity(new UrlEncodedFormEntity(valuePairs, UTF_8));
                Log.d(TAG, method.getURI().toString());
                Log.d(TAG, valuePairs.toString());
                generalOptBaseHeader(method);
                HttpResponse httpResponse = httpClient.execute(method);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == HTTP_OK) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, UTF_8);
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<DepositRecord>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    //noinspection unchecked
                    return APIJSON.buildServerErrorJSON(code);
                }
            } catch (Exception e) {
                //noinspection unchecked
                return APIJSON.buildOtherErrorJSON(e);
            }
        }

        /**
         * 提交入柜
         *
         * @return 提交结果
         */
        public static APIJSON<DepositRecord> takeInDeposit(DepositRecord depositRecord) {

            try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                String api_url = String.format(API_TAKE_IN, Objects.requireNonNull(CabinetCore.getCabinetInfo()).id);
                HttpPost method = new HttpPost(api_url);
                List<NameValuePair> valuePairs = new ArrayList<>();
                valuePairs.add(new BasicNameValuePair("storage_record[storage_no]", depositRecord.storage_no));
//              valuePairs.add(new BasicNameValuePair("storage_record[storage_id]", depositRecord.storage_id));
                valuePairs.add(new BasicNameValuePair("storage_record[container_size]", depositRecord.container_size));
                valuePairs.add(new BasicNameValuePair("storage_record[storage_rack]", depositRecord.storage_rack));
                valuePairs.add(new BasicNameValuePair("storage_record[input_weight]", depositRecord.input_weight));
                if (!TextUtils.isEmpty(depositRecord.harmful_infos)) {
                    String[] hi = depositRecord.harmful_infos.split(",");
                    for (String s : hi) {
                        valuePairs.add(new BasicNameValuePair("storage_record[harmful_info][]", s));
                    }
                }
                valuePairs.add(new BasicNameValuePair("storage_record[remark]", depositRecord.remark));
                valuePairs.add(new BasicNameValuePair("storage_record[laboratory_id]", depositRecord.laboratory_id));
                method.setEntity(new UrlEncodedFormEntity(valuePairs, UTF_8));
                Log.d(TAG, method.getURI().toString());
                Log.d(TAG, valuePairs.toString());
                generalOptBaseHeader(method);
                HttpResponse httpResponse = httpClient.execute(method);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == HTTP_OK) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, UTF_8);
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<DepositRecord>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    //noinspection unchecked
                    return APIJSON.buildServerErrorJSON(code);
                }
            } catch (Exception e) {
                //noinspection unchecked
                return APIJSON.buildOtherErrorJSON(e);
            }
        }


        /**
         * 获取暂存记录
         *
         * @param no 暂存编号
         * @return 暂存记录
         */
        public static APIJSON<DepositRecord> getDeposit(String no) {

            try {
                String api_url = String.format(API_DEPOSIT_LIST, no);
                CloseableHttpClient httpClient = HttpClients.createDefault();
                HttpGet method = new HttpGet(api_url);
                Log.d(TAG, method.getURI().toString());
                generalOptBaseHeader(method);
                HttpResponse httpResponse = httpClient.execute(method);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == HTTP_OK) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, UTF_8);
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<DepositRecord>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    //noinspection unchecked
                    return APIJSON.buildServerErrorJSON(code);
                }
            } catch (Exception e) {
                //noinspection unchecked
                return APIJSON.buildOtherErrorJSON(e);
            }
        }
    }

    public static class System {

        /**
         * 登录
         */
        public static final String API_LOGIN = API_ROOT + "/api/sessions";
        /**
         * 用户名下一体机设备搜索
         */
        public static final String API_CABINET_LIST = API_ROOT + "/api/storages";

        /**
         * 获取柜子绑定摄像头列表
         */
        public static final String API_CAMERA_LIST = API_ROOT + "/api/storages/%s/cameras";

        /**
         * 获取机构下实验室列表
         */
        public static final String API_LABORATORY_LIST = API_ROOT + "/api/laboratories";

        /**
         * 滤芯重置
         */
        public static final String API_FILTER_RESET = API_ROOT + "/api/storages/%s/reset_filter_time";

        /**
         * 获取滤芯重置时间
         */
        public static final String API_FILTER_RESET_TIME = API_ROOT + "/api/storages/%s/filter_time";

        /**
         * 查询管理员名下所有暂存柜列表
         *
         * @param userId 管理员Id
         * @return 暂存柜列表
         */
        public static APIJSON<CabinetListJSON> getCabinetList(String userId) {
            try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                List<NameValuePair> valuePairs = new ArrayList<>();
                valuePairs.add(new BasicNameValuePair("userId", userId));
                String params = EntityUtils.toString(new UrlEncodedFormEntity(valuePairs, UTF_8));
                HttpGet method = new HttpGet(API_CABINET_LIST + "?" + params);
                Log.d(TAG, method.getURI().toString());
                generalAdminBaseHeader(method);
                HttpResponse httpResponse = httpClient.execute(method);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == HTTP_OK) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, UTF_8);
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<CabinetListJSON>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    //noinspection unchecked
                    return APIJSON.buildServerErrorJSON(code);
                }
            } catch (Exception e) {
                //noinspection unchecked
                return APIJSON.buildOtherErrorJSON(e);
            }
        }

        /**
         * 滤芯重置
         *
         * @return 结果
         */
        public static APIJSON<FilterTime> filterReset() {
            try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                String api_url = String.format(API_FILTER_RESET, Objects.requireNonNull(CabinetCore.getCabinetInfo()).id);
                HttpPut method = new HttpPut(api_url);
                Log.d(TAG, method.getURI().toString());
                generalOptBaseHeader(method);
                HttpResponse httpResponse = httpClient.execute(method);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == HTTP_OK) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, "utf-8");
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<FilterTime>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    //noinspection unchecked
                    return APIJSON.buildServerErrorJSON(code);
                }
            } catch (Exception e) {
                //noinspection unchecked
                return APIJSON.buildOtherErrorJSON(e);
            }
        }

        /**
         * 获取滤芯重置时间
         *
         * @return 结果
         */
        public static APIJSON<FilterTime> filterResetTime() {
            try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                String api_url = String.format(API_FILTER_RESET_TIME, Objects.requireNonNull(CabinetCore.getCabinetInfo()).id);
                HttpGet method = new HttpGet(api_url);
                Log.d(TAG, method.getURI().toString());
                generalOptBaseHeader(method);
                HttpResponse httpResponse = httpClient.execute(method);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == HTTP_OK) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, "utf-8");
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<FilterTime>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    //noinspection unchecked
                    return APIJSON.buildServerErrorJSON(code);
                }
            } catch (Exception e) {
                //noinspection unchecked
                return APIJSON.buildOtherErrorJSON(e);
            }
        }

        /**
         * 获取实验列表
         *
         * @return 实验列表，放在Agency对象中
         */
        public static APIJSON<Agency> getLaboratoryList() {
            try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                HttpGet method = new HttpGet(API_LABORATORY_LIST);
                Log.d(TAG, method.getURI().toString());
                generalOptBaseHeader(method);
                HttpResponse httpResponse = httpClient.execute(method);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == HTTP_OK) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, UTF_8);
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<Agency>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    //noinspection unchecked
                    return APIJSON.buildServerErrorJSON(code);
                }
            } catch (Exception e) {
                //noinspection unchecked
                return APIJSON.buildOtherErrorJSON(e);
            }
        }

        /**
         * 查询柜子下所有摄像头列表
         *
         * @return 监控摄像头列表
         */
        public static APIJSON<CameraListJSON> getCameraList(String cabinetId) {
            try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                String api_url = String.format(API_CAMERA_LIST, cabinetId);
                HttpGet method = new HttpGet(api_url);
                Log.d(TAG, method.getURI().toString());
                generalOptBaseHeader(method);
                HttpResponse httpResponse = httpClient.execute(method);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == HTTP_OK) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, "utf-8");
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<CameraListJSON>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    //noinspection unchecked
                    return APIJSON.buildServerErrorJSON(code);
                }
            } catch (Exception e) {
                //noinspection unchecked
                return APIJSON.buildOtherErrorJSON(e);
            }
        }

        /**
         * 登录
         *
         * @param account  账号名
         * @param password 账号密码MD5
         * @return 登录结果信息
         */
        public static APIJSON<User> login(String account, String password) {
            try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                HttpPost method = new HttpPost(API_LOGIN);
                List<NameValuePair> valuePairs = new ArrayList<>();
                valuePairs.add(new BasicNameValuePair("session[username]", account));
                valuePairs.add(new BasicNameValuePair("session[password]", password));
                method.setEntity(new UrlEncodedFormEntity(valuePairs, UTF_8));
                Log.d(TAG, method.getURI().toString());
                Log.d(TAG, valuePairs.toString());
                generalAdminBaseHeader(method);
                HttpResponse httpResponse = httpClient.execute(method);
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == HTTP_OK) {
                    HttpEntity entity = httpResponse.getEntity();
                    String content = EntityUtils.toString(entity, UTF_8);
                    Log.d(TAG, content);
                    Type jsonType = new TypeToken<APIJSON<User>>() {
                    }.getType();
                    return CabinetCore.GSON.fromJson(content, jsonType);
                } else {
                    //noinspection unchecked
                    return APIJSON.buildServerErrorJSON(code);
                }
            } catch (Exception e) {
                //noinspection unchecked
                return APIJSON.buildOtherErrorJSON(e);
            }
        }
    }
}
