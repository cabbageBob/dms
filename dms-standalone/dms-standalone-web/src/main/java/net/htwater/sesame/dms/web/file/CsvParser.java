package net.htwater.sesame.dms.web.file;

import com.csvreader.CsvReader;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 10415
 */
@Slf4j
public class CsvParser extends AbstractFileParser {
    public CsvParser() {
        super(FileType.CSV);
    }

    @Override
    public List<Object> getFields(InputStream inputStream,String fileName) {
        CsvReader reader = null;
        try {
            reader = new CsvReader(inputStream, Charset.defaultCharset());
            while (reader.readRecord()){
                String lineStr = reader.getRawRecord();
                if (lineStr.startsWith("\uFEFF")){
                    lineStr = lineStr.replace("\uFEFF", "");
                }
                return Arrays.stream(lineStr.split(","))
                        .map(this::removeSymbol)
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                inputStream.close();
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        throw new IllegalArgumentException("Csv文件为空");
    }


    @Override
    public List<List<Object>> parseFile(InputStream inputStream, String fileName) {
        List<List<Object>> result =new ArrayList<>();
        try {
            CsvReader reader = new CsvReader(inputStream, Charset.defaultCharset());
            while (reader.readRecord()){
                String lineStr = reader.getRawRecord();
                if (lineStr.startsWith("\uFEFF")){
                    lineStr = lineStr.replace("\uFEFF", "");
                }
                result.add(Arrays.stream(lineStr.split(","))
                        .map(this::removeSymbol)
                        .collect(Collectors.toList()));
            }
            return result;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        throw new IllegalArgumentException("Csv文件为空");
    }

    private ByteArrayOutputStream cloneInputStream(InputStream input) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = input.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            return baos;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    private String codeString(InputStream inputStream) throws IOException {
        BufferedInputStream bin = new BufferedInputStream(inputStream);
        int p = (bin.read() << 8) + bin.read();
        String code;
        switch (p) {
            case 0xefbb:
                code = "UTF-8";
                break;
            case 0xfffe:
                code = "Unicode";
                break;
            case 0xfeff:
                code = "UTF-16BE";
                break;
            default:
                code = "GBK";
        }
        return code;
    }

    private String removeSymbol(String source) {
        int index = source.indexOf('\"');
        int lastIndex = source.lastIndexOf('\"');
        if (index == 0 && lastIndex == source.length() - 1) {
            source = source.substring(index + 1, lastIndex);
        }
        return source;
    }
}
