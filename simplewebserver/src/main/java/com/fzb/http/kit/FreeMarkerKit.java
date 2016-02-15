package com.fzb.http.kit;

import com.fzb.http.server.HttpRequest;
import com.fzb.http.server.session.HttpSession;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class FreeMarkerKit {

    private static Configuration cfg;

    public static String renderToFM(String name, HttpRequest httpRequest) {
        try {
            Template temp = cfg.getTemplate(name + ".ftl");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Writer writer = new OutputStreamWriter(out);
            HttpSession httpSession = httpRequest.getSession();
            if (httpSession != null) {
                httpRequest.getAttr().put("session", httpSession);
                httpRequest.getAttr().put("request", httpRequest);
            }
            temp.process(httpRequest.getAttr(), writer);
            writer.flush();
            writer.close();
            return new String(out.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return StringsUtil.getHtmlStrByStatusCode(404);
    }

    public static void init(String basePath) throws Exception {
        cfg = new Configuration();
        cfg.setDirectoryForTemplateLoading(new File(basePath));
        cfg.setObjectWrapper(new DefaultObjectWrapper());
    }
}
