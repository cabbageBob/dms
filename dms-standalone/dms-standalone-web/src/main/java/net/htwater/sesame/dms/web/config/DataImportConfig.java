package net.htwater.sesame.dms.web.config;

import net.htwater.sesame.dms.web.file.*;
import net.htwater.sesame.dms.web.service.DataImportExportService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataImportConfig {

    private final DataImportExportService dataImportExportService;

    @Autowired
    public DataImportConfig(DataImportExportService dataImportExportService) {
        this.dataImportExportService = dataImportExportService;
    }

    @Bean
    ExcleParser excleParser(){return new ExcleParser();}


    @Bean
    CsvParser csvParser(){return new CsvParser();}

    @Bean
    public BeanPostProcessor fileTypeRegister(){
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
                return bean;
            }
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof FileParser){
                    AbstractFileParser fileParser = (AbstractFileParser) bean;
                    for (FileType fileType : FileType.values()){
                        if (fileParser.isSupport(fileType)) {
                            dataImportExportService.fileTypeRegister(fileType,fileParser.get());
                        }
                    }
                }
                return bean;
            }
        };
    }
}
