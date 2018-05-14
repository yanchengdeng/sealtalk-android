package cn.rongcloud.im.utils;

import android.text.TextUtils;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import cn.rongcloud.im.server.response.ModifyNameResponse;
import cn.rongcloud.im.server.response.PublishCircleResponse;
import io.rong.common.RLog;

/**
 * Created by star1209 on 2018/5/13.
 */

public class UpLoadImgManager {

    private static final String TAG = "uploadFile";
    private static final int TIME_OUT = 10*10000000; //超时时间
    private static final String CHARSET = "utf-8"; //设置编码
    private static final String BOUNDARY = "----WebKitFormBoundaryT1HoybnYeFOGFlBR" ; //UUID.randomUUID().toString(); //边界标识 随机生成 String PREFIX = "--" , LINE_END = "\r\n";
    private static final String PREFIX="--";
    private static final String LINE_END="\n\r";
    private static final String CONTENT_TYPE = "multipart/form-data"; //内容类型

    /** * android上传文件到服务器
     * @param file 需要上传的文件
     * @param requestURL 请求的rul
     * @return 返回响应的内容
     */
    public String uploadFile(File file, String requestURL) {
        try {
            URL url = new URL(requestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(TIME_OUT);
            conn.setConnectTimeout(TIME_OUT);
            conn.setDoInput(true); //允许输入流
            conn.setDoOutput(true); //允许输出流
            conn.setUseCaches(false); //不允许使用缓存
            conn.setRequestMethod("POST"); //请求方式
            conn.setRequestProperty("Charset", CHARSET);//设置编码
            //头信息
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
            if(file!=null) {
                /** * 当文件不为空，把文件包装并且上传 */
                OutputStream outputSteam=conn.getOutputStream();
                DataOutputStream dos = new DataOutputStream(outputSteam);

                String[] params = {"\"ownerId\"","\"docName\"","\"docType\"","\"sessionKey\"","\"sig\""};
                String[] values = {"1410065922",file.getName(),"jpg","dfbe0e1686656d5a0c8de11347f93bb6","e70cff74f433ded54b014e7402cf094a"};
                //添加docName,docType,sessionKey,sig参数
                for(int i=0;i<params.length;i++){
                    //添加分割边界
                    StringBuffer sb = new StringBuffer();
                    sb.append(PREFIX);
                    sb.append(BOUNDARY);
                    sb.append(LINE_END);

                    sb.append("Content-Disposition: form-data; name=" + params[i] + LINE_END);
                    sb.append(LINE_END);
                    sb.append(values[i]);
                    sb.append(LINE_END);
                    dos.write(sb.toString().getBytes());
                }

                //file内容
                StringBuffer sb = new StringBuffer();
                sb.append(PREFIX);
                sb.append(BOUNDARY);
                sb.append(LINE_END);

                sb.append("Content-Disposition: form-data; name=\"file\";filename=" + "\"" + file.getName() + "\"" + LINE_END);
                sb.append("Content-Type: image/jpg"+LINE_END);
                sb.append(LINE_END);
                dos.write(sb.toString().getBytes());
                //读取文件的内容
                InputStream is = new FileInputStream(file);
                byte[] bytes = new byte[1024];
                int len = 0;
                while((len=is.read(bytes))!=-1)
                {
                    dos.write(bytes, 0, len);
                }
                is.close();
                //写入文件二进制内容
                dos.write(LINE_END.getBytes());
                //写入end data
                byte[] end_data = (PREFIX+BOUNDARY+PREFIX+LINE_END).getBytes();
                dos.write(end_data);
                dos.flush();
                /**
                 * 获取响应码 200=成功
                 * 当响应成功，获取响应的流
                 */
                int res = conn.getResponseCode();
                RLog.e(TAG, "response code:"+res);
                if(res == 200) {
                    String oneLine;
                    StringBuffer response = new StringBuffer();
                    BufferedReader input = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((oneLine = input.readLine()) != null) {
                        response.append(oneLine);
                    }
                    return response.toString();
                }else{
                    return res+"";
                }
            }else{
                return "file not found";
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return "failed";
        } catch (IOException e) {
            e.printStackTrace();
            return "failed";
        }
    }

    /**
     * HttpUrlConnection 实现文件上传
     *
     * @param params
     *            普通参数
     * @param fileFormName
     *            文件在表单中的键
     * @param uploadFile
     *            上传的文件
     * @param newFileName
     *            文件在表单中的值（服务端获取到的文件名）
     * @param urlStr
     *            url
     * @throws IOException
     */
    public static ModifyNameResponse uploadForm(Map<String, String> params, String fileFormName, File uploadFile, String newFileName,
                                  String urlStr) throws IOException {

        if (newFileName == null || newFileName.trim().equals("")) {
            newFileName = uploadFile.getName();
        }
        StringBuilder sb = new StringBuilder();
        /**
         * 普通的表单数据
         */
        if (params != null) {
            for (String key : params.keySet()) {
                sb.append("--" + BOUNDARY + "\r\n");
                sb.append("Content-Disposition: form-data; name=\"" + key + "\"" + "\r\n");
                sb.append("\r\n");
                sb.append(params.get(key) + "\r\n");
            }
        }
        /**
         * 上传文件的头
         */
        sb.append("--" + BOUNDARY + "\r\n");
        sb.append("Content-Disposition: form-data; name=\"" + fileFormName + "\"; filename=\"" + newFileName + "\""
                + "\r\n");
        sb.append("Content-Type: image/jpeg" + "\r\n");// 如果服务器端有文件类型的校验，必须明确指定ContentType
        sb.append("\r\n");

        byte[] headerInfo = sb.toString().getBytes("UTF-8");
        byte[] endInfo = ("\r\n--" + BOUNDARY + "--\r\n").getBytes("UTF-8");

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        // 设置传输内容的格式，以及长度
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
        conn.setRequestProperty("Content-Length",
                String.valueOf(headerInfo.length + uploadFile.length() + endInfo.length));
        conn.setDoOutput(true);

        OutputStream out = conn.getOutputStream();
        InputStream in = new FileInputStream(uploadFile);
        // 写入头部 （包含了普通的参数，以及文件的标示等）
        out.write(headerInfo);
        // 写入文件
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) != -1) {
            out.write(buf, 0, len);
        }
        // 写入尾部
        out.write(endInfo);
        in.close();
        out.close();
        if (conn.getResponseCode() == 200) {
            InputStream inputStream = null;
            if (!TextUtils.isEmpty(conn.getContentEncoding())) {
                String encode = conn.getContentEncoding().toLowerCase();
                if (!TextUtils.isEmpty(encode) && encode.indexOf("gzip") >= 0) {
                    inputStream = new GZIPInputStream(conn.getInputStream());
                }
            }

            if (null == inputStream) {
                inputStream = conn.getInputStream();
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            StringBuilder builder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            String content = builder.toString();
            ModifyNameResponse response = null;
            if (!TextUtils.isEmpty(content)){
                Gson gson = new Gson();
                response = gson.fromJson(content, ModifyNameResponse.class);
            }
            RLog.v("upload", content);
            return response;
        }

        return null;
    }

    /**
     * 多文件上传
     * @param params
     * @param fileFormName
     * @param uploadFile
     * @param urlStr
     * @throws IOException
     */
    public PublishCircleResponse uploadForm(Map<String, String> params, String fileFormName, List<File> uploadFile, String urlStr)
            throws IOException {

        try {
            URL u = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) u.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("Charsert", "UTF-8");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);
            // 指定流的大小，当内容达到这个值的时候就把流输出
            conn.setChunkedStreamingMode(10240);
            OutputStream out = conn.getOutputStream();
            byte[] end_data = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();// 定义最后数据分隔线
            StringBuilder sb = new StringBuilder();
            // 添加form属性

            /**
             * 普通的表单数据
             */
            if (params != null) {
                for (String key : params.keySet()) {
                    sb.append("--" + BOUNDARY + "\r\n");
                    sb.append("Content-Disposition: form-data; name=\"" + key + "\"" + "\r\n");
                    sb.append("\r\n");
                    sb.append(params.get(key) + "\r\n");
                }
            }
            out.write(sb.toString().getBytes("utf-8"));

            int leng = uploadFile.size();
            for (int i = 0; i < leng; i++) {
                File f = uploadFile.get(i);
                sb = new StringBuilder();
                sb.append("--");
                sb.append(BOUNDARY);
                sb.append("\r\n");
                sb.append("Content-Disposition: form-data;name=\"" + fileFormName + "\";filename=\"" + f.getName()
                        + "\"\r\n");
                sb.append("Content-Type:application/octet-stream\r\n\r\n");
                byte[] data = sb.toString().getBytes();
                out.write(data);
                DataInputStream in = new DataInputStream(new FileInputStream(f));
                int bytes = 0;
                byte[] bufferOut = new byte[1024];
                while ((bytes = in.read(bufferOut)) != -1) {
                    out.write(bufferOut, 0, bytes);
                }
                out.write("\r\n".getBytes()); // 多个文件时，二个文件之间加入这个
                in.close();
            }
            out.write(end_data);
            out.flush();
            out.close();
            // 定义BufferedReader输入流来读取URL的响应
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            String content = builder.toString();
            PublishCircleResponse response = null;
            if (!TextUtils.isEmpty(content)){
                Gson gson = new Gson();
                response = gson.fromJson(content, PublishCircleResponse.class);
            }
            RLog.v("upload", content);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
