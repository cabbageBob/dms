package net.htwater.sesame.dms.web.file;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.Format;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 解析Excle
 * @author 10415
 */
@Slf4j
public class ExcleParser extends AbstractFileParser {

    public ExcleParser() {
        super(FileType.EXCEL);
    }

    @Override
    public List<Object> getFields(InputStream is,String fileName) {
        List<List<Object>> result =new ArrayList<>();
        String suffix = fileName.substring(fileName.lastIndexOf(".")+1);
        try(FileInputStream inputStream = (FileInputStream)is){
            if ("xls".equals(suffix)){
                result = readXls(inputStream, 0, 0);
            }else if ("xlsx".equals(suffix)){
                result = readXlsx(inputStream, 0, 0);
            }else {
                throw new IllegalArgumentException("不支持的文件类型");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result.get(0);
    }

    @Override
    public List<List<Object>> parseFile(InputStream is,String fileName){
        List<List<Object>> result =new ArrayList<>();
        String suffix = fileName.substring(fileName.lastIndexOf(".")+1);
        try(FileInputStream inputStream = (FileInputStream)is){
            if ("xls".equals(suffix)){
                result = readXls(inputStream, 0, null);
            }else if ("xlsx".equals(suffix)){
                result = readXlsx(inputStream, 0, null);
            }else {
                throw new IllegalArgumentException("不支持的文件类型");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    private static List<List<Object>> readXls(InputStream inputStream, int sheetNum, Integer maxRowNum){
        List<List<Object>> result = new ArrayList<>();
        try {
            HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
            //for (int i=0;i<workbook.getNumberOfSheets();i++) {
                HSSFSheet sheet = workbook.getSheetAt(sheetNum);
                if (sheet==null){
                    throw new IllegalArgumentException("Excel为空");
                }
                HSSFRow firstHssfRow = sheet.getRow(sheet.getTopRow());
                int minColIdx = firstHssfRow.getFirstCellNum();
                int maxColIdx = firstHssfRow.getLastCellNum();
                if (maxRowNum == null || maxRowNum > sheet.getLastRowNum()) {
                    maxRowNum = sheet.getLastRowNum();
                }
                for (int rowNum=0;rowNum<=maxRowNum;rowNum++){
                    HSSFRow hssfRow = sheet.getRow(rowNum);
                    List<Object> rowList = new ArrayList<>();
                    for (int col = minColIdx; col < maxColIdx; col++) {
                        HSSFCell hssfCell = hssfRow.getCell(col);
                        if (hssfCell==null){
                            continue;
                        }
                        rowList.add(getStringVal(hssfCell));
                    }
                    result.add(rowList);
                }
            //}
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static List<List<Object>> readXlsx(InputStream inputStream, int sheetNum, Integer maxRowNum){
        List<List<Object>> result = new ArrayList<>();
        try {

            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            //for (int i=0;i<workbook.getNumberOfSheets();i++) {
                XSSFSheet sheet = workbook.getSheetAt(sheetNum);
                if (sheet==null){
                    throw new IllegalArgumentException("Excel为空");
                }
                XSSFRow firstXssfRow = sheet.getRow(sheet.getFirstRowNum());
                int minColIdx = firstXssfRow.getFirstCellNum();
                int maxColIdx = firstXssfRow.getLastCellNum();
                if (maxRowNum == null || maxRowNum > sheet.getLastRowNum()) {
                    maxRowNum = sheet.getLastRowNum();
                }
                for (int rowNum=0;rowNum<=maxRowNum;rowNum++){
                    XSSFRow xssfRow = sheet.getRow(rowNum);
                    List<Object> rowList = new ArrayList<>();
                    for (int col = minColIdx; col < maxColIdx; col++) {
                        XSSFCell xssfCell = xssfRow.getCell(col);
                        if (xssfCell==null){
                            rowList.add(null);
                            continue;
                        }
                        rowList.add(getStringVal(xssfCell));
                    }
                    result.add(rowList);
                }
           // }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static String getStringVal(Cell cell){
        switch (cell.getCellType()){
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    DataFormatter dataFormatter = new DataFormatter();
                    Format format = dataFormatter.createFormat(cell);
                    Date date = cell.getDateCellValue();
                    return format.format(date);
                }
                cell.setCellType(CellType.STRING);
                return cell.getStringCellValue();
            case FORMULA:
                return cell.getCellFormula();
            case BOOLEAN:
                return cell.getBooleanCellValue()?"true":"false";
            case BLANK:
                return "";
            case ERROR:
                return "";
            case _NONE:
                return null;
            default:
                return "";
        }
    }
}
