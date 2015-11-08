package com.fzb.http.server.codec.impl;

import com.fzb.http.kit.HexConversionUtil;
import com.fzb.http.kit.IOUtil;
import com.fzb.http.kit.ConfigKit;
import com.fzb.http.kit.LoggerUtil;
import com.fzb.http.kit.PathKit;
import com.fzb.http.server.HttpMethod;
import com.fzb.http.server.codec.IHttpDeCoder;
import com.fzb.http.server.cookie.Cookie;
import com.fzb.http.server.execption.ContentToBigException;
import com.fzb.http.server.impl.SimpleHttpRequest;
import com.fzb.http.server.session.HttpSession;
import com.fzb.http.server.session.SessionUtil;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Logger;

public class HttpDecoder extends SimpleHttpRequest implements IHttpDeCoder {

    private static final Logger LOGGER = LoggerUtil.getLogger(HttpDecoder.class);


    private static final String split = "\r\n\r\n";
    private long createTime;
    private boolean disableCookie;

    public HttpDecoder() {
        this(false);
    }

    public HttpDecoder(boolean disableCookie) {
        createTime = System.currentTimeMillis();
        this.disableCookie = disableCookie;
    }

    @Override
    public boolean doDecode(SocketChannel channel) throws Exception {
        this.ipAddr = channel.socket().getRemoteSocketAddress();
        // parse HttpHeader
        ByteBuffer buffer1 = ByteBuffer.allocate(1024 * 16);
        int length = channel.read(buffer1);
        if (length <= 0) {
            channel.socket().close();
            return true;
        }
        byte[] date = HexConversionUtil.subBytes(buffer1.array(), 0, length);
        //InputStream in=channel.socket().getInputStream();
        String fullStr = new String(date);
        boolean flag = false;
        if (dataBuffer == null && new String(date).contains(split)) {
            String tstr = new String(date).substring(0, new String(date).indexOf(split));
            String httpStr[] = new String(tstr).split("\r\n");
            String pHeader = httpStr[0];
            if (!"".equals(pHeader.split(" ")[0])) {
                try {
                    method = HttpMethod.valueOf(pHeader.split(" ")[0]);
                } catch (IllegalArgumentException e) {
                    LOGGER.warning("unSupport method " + pHeader.split(" ")[0]);
                    return false;
                }
                // 先得到请求头信息
                for (int i = 1; i < httpStr.length; i++) {
                    header.put(httpStr[i].split(":")[0], httpStr[i].substring(httpStr[i].indexOf(":") + 2));
                }
                String paramStr = null;
                String turl = uri = pHeader.split(" ")[1];
                if (turl.indexOf("?") != -1) {
                    uri = turl.substring(0, turl.indexOf("?"));
                    paramStr = turl.substring(turl.indexOf("?") + 1);
                    queryStr = paramStr;
                }
                if (method == HttpMethod.GET) {
                    wrapperParamStrToMap(paramStr);
                    flag = true;
                }
                // 存在2种情况
                // 1,POST 提交的数据一次性读取完成。
                // 2,POST 提交的数据一次性读取不完。
                else if (method == HttpMethod.POST) {
                    wrapperParamStrToMap(paramStr);
                    Integer dateLength = Integer.parseInt(header.get("Content-Length"));
                    if (dateLength > ConfigKit.getMaxUploadSize()) {
                        throw new ContentToBigException("Content-Length outSide the max uploadSize "
                                + ConfigKit.getMaxUploadSize());
                    }
                    dataBuffer = ByteBuffer.allocate(dateLength);
                    Integer remainLen = fullStr.indexOf(split) + split.getBytes().length;
                    byte[] remain = HexConversionUtil.subBytes(date, remainLen, date.length - remainLen);
                    dataBuffer.put(remain);
                    flag = !dataBuffer.hasRemaining();
                    if (flag) {
                        dealPostData();
                    }
                }
                if (!disableCookie) {
                    // deal with cookie
                    dealWithCookie();
                }
            }
        } else {
            if (dataBuffer != null) {
                dataBuffer.put(date);
                flag = !dataBuffer.hasRemaining();
                if (flag) {
                    dealPostData();
                }
            }
        }
        return flag;
    }

    private void dealWithCookie() {
        boolean createCookie = true;
        if (header.get("Cookie") != null) {
            cookies = Cookie.saxToCookie(header.get("Cookie").toString());
            String jsessionid = Cookie.getJSessionId(header.get("Cookie").toString());
            if (jsessionid == null) {
                Cookie[] tcookies = new Cookie[cookies.length + 1];
                // copy cookie
                for (int i = 0; i < cookies.length; i++) {
                    tcookies[i] = cookies[i];
                }
                cookies = tcookies;
            } else {
                session = SessionUtil.getSessionById(jsessionid);
                if (session != null) {
                    createCookie = false;
                }
            }
        }
        if (createCookie) {
            if (cookies == null) {
                cookies = new Cookie[1];
            }
            Cookie cookie = new Cookie(true);
            String jsessionid = UUID.randomUUID().toString();
            cookie.setName(Cookie.JSESSIONID);
            cookie.setPath("/");
            cookie.setValue(jsessionid);
            cookies[cookies.length - 1] = cookie;
            session = new HttpSession(jsessionid);
            SessionUtil.sessionMap.put(jsessionid, session);
            LOGGER.info("create a Cookie " + cookie.toString());
        }
    }

    public void wrapperParamStrToMap(String paramStr) {
        paramMap = new HashMap<String, String[]>();
        if (paramStr != null) {
            Map<String, Set<String>> tempParam = new HashMap<>();
            String args[] = new String(paramStr).split("&");
            for (String string : args) {
                int idx = string.indexOf("=");
                if (idx != -1) {
                    String key = string.substring(0, idx);
                    String value = string.substring(idx + 1);
                    if (tempParam.containsKey(key)) {
                        tempParam.get(key).add(value);
                    } else {
                        Set<String> tmpSet = new TreeSet<>();
                        tmpSet.add(value);
                        tempParam.put(key, tmpSet);
                    }
                }
            }
            for (Entry<String, Set<String>> ent : tempParam.entrySet()) {
                paramMap.put(ent.getKey(), ent.getValue().toArray(new String[ent.getValue().size()]));
            }
        }
    }

    private void dealPostData() {
        String paramStr;
        if (header.get("Content-Type") != null && header.get("Content-Type").toString().split(";")[0] != null) {
            if ("multipart/form-data".equals(header.get("Content-Type").toString().split(";")[0])) {
                //TODO 使用合理算法提高对网卡的利用率
                //FIXME 不支持多文件上传，不支持这里有其他属性字段
                if (!dataBuffer.hasRemaining()) {
                    BufferedReader bin = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(dataBuffer.array())));
                    //ByteArrayOutputStream bout=new ByteArrayOutputStream(d);
                    String tstr = null;
                    StringBuffer sb2 = new StringBuffer();
                    try {
                        while ((tstr = bin.readLine()) != null && !"".equals(tstr)) {
                            sb2.append(tstr + "\r\n");
                            if (tstr.indexOf(":") != -1) {
                                header.put(tstr.split(":")[0], tstr.substring(tstr.indexOf(":") + 2));
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            bin.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }


                    String inputName = header.get("Content-Disposition").split(";")[1].split("=")[1].replace("\"", "");
                    String fileName = header.get("Content-Disposition").split(";")[2].split("=")[1].replace("\"", "");
                    File file = new File(PathKit.getRootPath() + "/temp/" + fileName);
                    files.put(inputName, file);
                    int length1 = sb2.toString().split("\r\n")[0].getBytes().length + "\r\n".getBytes().length;
                    int length2 = sb2.toString().getBytes().length + 2;
                    int dataLength = Integer.parseInt(header.get("Content-Length")) - length1 - length2 - split.getBytes().length;
                    IOUtil.writeBytesToFile(HexConversionUtil.subBytes(dataBuffer.array(), length2, dataLength), file);
                    paramMap = new HashMap<>();

                }
            } else {
                paramStr = new String(dataBuffer.array());
                wrapperParamStrToMap(paramStr);
            }
        } else {
            paramStr = new String(dataBuffer.array());
            wrapperParamStrToMap(paramStr);
        }
    }

    public long getCreateTime() {
        return createTime;
    }
}
