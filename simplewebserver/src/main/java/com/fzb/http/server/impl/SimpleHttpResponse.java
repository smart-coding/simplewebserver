package com.fzb.http.server.impl;

import com.fzb.http.kit.HexConversionUtil;
import com.fzb.http.kit.IOUtil;
import com.fzb.http.kit.*;
import com.fzb.http.mimetype.MimeTypeUtil;
import com.fzb.http.server.ChunkedOutputStream;
import com.fzb.http.server.HttpRequest;
import com.fzb.http.server.HttpResponse;
import com.fzb.http.server.cookie.Cookie;
import flexjson.JSONSerializer;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

public class SimpleHttpResponse implements HttpResponse {


    private static String serverName = "SIMPLEWEBSERVER/" + StringsUtil.VERSIONSTR;
    private static Logger LOGGER = LoggerUtil.getLogger(SimpleHttpResponse.class);
    private SocketChannel channel;
    private Map<String, String> header = new HashMap<String, String>();
    private HttpRequest request;
    private List<Cookie> cookieList = new ArrayList<Cookie>();


    public SimpleHttpResponse(SocketChannel channel, HttpRequest request) {
        this.channel = channel;
        this.request = request;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.isCreate()) {
                    cookieList.add(cookie);
                }
            }
        }
    }

    @Override
    public OutputStream getWriter() {
        return null;
    }

    @Override
    public void setHeader(String key, String value) {
        header.put(key, value);
    }

    @Override
    public void writeFile(File file) {
        if (file.exists()) {
            try {
                // getMimeType
                ByteArrayOutputStream fout = new ByteArrayOutputStream();
                if (file.isDirectory()) {
                    renderByStatusCode(302);
                    return;
                }
                String ext = file.getName().substring(file.getName().lastIndexOf(".") + 1);
                if (header.get("Content-Type") == null) {
                    header.put("Content-Type", MimeTypeUtil.getMimeStrByExt(ext));
                }
                if (file.length() < 1024 * 1024) {
                    fout.write(wrapperData(200, IOUtil.getByteByInputStream(new FileInputStream(file))));
                    send(fout);
                } else {
                    fout.write(wrapperResponseHeader(200, file.length()));
                    send(fout, false);
                    //处理大文件
                    FileInputStream fileInputStream = new FileInputStream(file);
                    int length;
                    byte tempByte[] = new byte[512 * 1204];
                    while ((length = fileInputStream.read(tempByte)) != -1) {
                        ByteArrayOutputStream bout = new ByteArrayOutputStream();
                        bout.write(tempByte, 0, length);
                        send(bout, false);
                    }
                    fileInputStream.close();
                    channel.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            renderByStatusCode(404);
        }
    }

    private void send(ByteArrayOutputStream fout, boolean close) {
        ByteBuffer buffer = ByteBuffer.allocate(fout.toByteArray().length);
        try {
            buffer.put(fout.toByteArray());
            buffer.flip();

            while (buffer.hasRemaining()) {
                int len = channel.write(buffer);
                if (len < 0) {
                    throw new EOFException();
                }
            }
            if (close) {
                channel.close();
            }
        } catch (Exception e) {
            //e.printStackTrace();

        } finally {
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void send(ByteArrayOutputStream fout) {
        send(fout, true);
    }

    @Override
    public void renderJson(Object json) {
        try {
            String body = new JSONSerializer().deepSerialize(json);
            ByteArrayOutputStream fout = new ByteArrayOutputStream();
            header.put("Content-Type", MimeTypeUtil.getMimeStrByExt("json") + ";charset=UTF-8");
            fout.write(wrapperData(200, body.getBytes("UTF-8")));
            send(fout);
        } catch (FileNotFoundException e) {
            renderByStatusCode(404);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return
     * @throws IOException
     */
    private byte[] wrapperData(Integer statusCode, byte[] data) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        bout.write(wrapperResponseHeader(statusCode, data.length));
        if (data.length > 0) {
            bout.write(data);
        }
        return bout.toByteArray();
    }

    private byte[] wrapperResponseHeader(Integer statusCode, long length) {
        header.put("Content-Length", length + "");
        return wrapperBaseResponseHeader(statusCode);
    }

    private byte[] wrapperBaseResponseHeader(int statusCode) {
        header.put("server", serverName);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            bout.write(("HTTP/1.1 " + statusCode + " " + StatusCodeKit.getStatusCode(statusCode) + "\r\n").getBytes());
            for (Entry<String, String> he : header.entrySet()) {
                bout.write((he.getKey() + ": " + he.getValue() + "\r\n").getBytes());
            }
            //deal cookie
            for (Cookie cookie : cookieList) {
                bout.write(("Set-Cookie: " + cookie + "\r\n").getBytes());
            }
            bout.write("\r\n".getBytes());
            return bout.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[]{};
    }

    private byte[] wrapperResponseHeader(Integer statusCode) {
        header.put("Transfer-Encoding", "chunked");
        return wrapperBaseResponseHeader(statusCode);
    }


    private void renderByStatusCode(Integer errorCode) {
        if (errorCode > 399) {
            ByteArrayOutputStream fout = new ByteArrayOutputStream();
            try {
                header.put("Content-Type", "text/html");
                fout.write(wrapperData(errorCode, StringsUtil.getHtmlStrByStatusCode(errorCode).getBytes()));
                send(fout);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } else if (errorCode >= 300 && errorCode < 400) {
            ByteArrayOutputStream fout = new ByteArrayOutputStream();
            try {
                if (!header.containsKey("Location")) {
                    header.put("Location", "http://" + request.getHeader("Host") + "/" + request.getUri() + "index.html");
                }
                fout.write(wrapperData(errorCode, new byte[]{}));
                send(fout);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void renderCode(int code) {
        renderByStatusCode(code);
    }

    @Override
    public void renderHtml(String urlPath) {
        writeFile(new File(PathKit.getStaticPath() + urlPath));
    }

    @Override
    public void addCookie(Cookie cookie) {
        cookieList.add(cookie);
    }

    @Override
    public void renderHtmlStr(String htmlContent) {
        try {
            ByteArrayOutputStream fout = new ByteArrayOutputStream();
            header.put("Content-Type", MimeTypeUtil.getMimeStrByExt("html") + ";charset=UTF-8");
            fout.write(wrapperData(200, htmlContent.getBytes("UTF-8")));
            send(fout);
        } catch (FileNotFoundException e) {
            renderByStatusCode(404);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private byte[] warpperToChunkedByte(byte[] data) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ChunkedOutputStream out = new ChunkedOutputStream(bout);
        try {
            int block = 128;
            int blockCount = data.length / block;
            for (int i = 0; i < blockCount; i++) {
                out.write(HexConversionUtil.subBytes(data, i * block, block));
            }
            int last = data.length % block;
            if (last != 0) {
                out.write(HexConversionUtil.subBytes(data, blockCount * block, last));
            }
            out.close();
            return bout.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    @Override
    public void addHeader(String name, String value) {
        header.put(name, value);
    }

    @Override
    public void redirect(String url) {
        ByteArrayOutputStream fout = new ByteArrayOutputStream();
        header.put("Location", url);
        try {
            fout.write(wrapperData(302, new byte[0]));
            send(fout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void forward(String url) {
        redirect(request.getScheme() + "://" + request.getHeader("Host") + "/" + url);
    }

    @Override
    public void renderFile(File file) {
        header.put("Content-Disposition", "attachment;filename=" + file.getName());
        writeFile(file);
    }

    @Override
    public void renderFreeMarker(String name) {
        renderHtmlStr(FreeMarkerKit.renderToFM(name, request));
    }

    @Override
    public void write(InputStream inputStream) {
        write(inputStream, 200);
    }

    @Override
    public void write(InputStream inputStream, int code) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(wrapperResponseHeader(code));
            send(byteArrayOutputStream, false);
            if (inputStream != null) {
                byte[] bytes = new byte[1024];
                int length;
                while ((length = inputStream.read(bytes)) != -1) {
                    ByteArrayOutputStream tmpOut = new ByteArrayOutputStream();
                    ChunkedOutputStream chunkedOutputStream = new ChunkedOutputStream(tmpOut);
                    chunkedOutputStream.write(HexConversionUtil.subBytes(bytes, 0, length));
                    send(tmpOut, false);
                }
                ByteArrayOutputStream tmpOut = new ByteArrayOutputStream();
                ChunkedOutputStream chunkedOutputStream = new ChunkedOutputStream(tmpOut);
                chunkedOutputStream.close();
                send(tmpOut);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
