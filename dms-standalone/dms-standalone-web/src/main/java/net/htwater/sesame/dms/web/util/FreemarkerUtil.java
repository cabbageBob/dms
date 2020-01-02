package net.htwater.sesame.dms.web.util;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.Map;
@Slf4j
public class FreemarkerUtil {
    public static File createDoc(String templateName, Map dataMap,String outFileName,String outFileDirectory){
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
        configuration.setDefaultEncoding("utf-8");
        configuration.setClassForTemplateLoading(FreemarkerUtil.class,"/templates/");
        Template template = null;
        try {
            template = configuration.getTemplate(templateName);
        } catch (IOException e) {
            log.error(e.toString(),e);
        }
        String outFilePath = outFileDirectory + outFileName;
        File file = new File(outFilePath);
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), "utf-8")) {
            if (template == null) {
                return null;
            }
            template.process(dataMap, writer);
        } catch (TemplateException | IOException e) {
            log.error(e.toString(), e);
        }
        return file;
    }
}
