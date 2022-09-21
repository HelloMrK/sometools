package io.github.hellomrk.sometools;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelTools {



    private final static String excel2003L = ".xls"; // 2003- 版本的excel
    private final static String excel2007U = ".xlsx"; // 2007+ 版本的excel

    /**
     * 将流中的Excel数据转成List<Map>
     *
     * @param in
     *            输入流
     * @param fileName
     *            文件名（判断Excel版本）
     * @param mapping
     *            字段名称映射
     * @return
     * @throws Exception
     */
    private static List<Map<String, Object>> parseExcel(InputStream in, String fileName,
                                                        Map<String, String> mapping) throws Exception {
        // 根据文件名来创建Excel工作薄
        Workbook work = getWorkbook(in, fileName);
        if (null == work) {
            throw new Exception("创建Excel工作薄为空！");
        }
        Sheet sheet = null;
        Row row = null;
        Cell cell = null;
        // 返回数据
        List<Map<String, Object>> ls = new ArrayList<Map<String, Object>>();

        // 遍历Excel中所有的sheet
        for (int i = 0; i < work.getNumberOfSheets(); i++) {
            sheet = work.getSheetAt(i);
            if (sheet == null)
                continue;

            // 取第一行标题
            row = sheet.getRow(0);
            String title[] = null;
            if (row != null) {
                title = new String[row.getLastCellNum()];

                for (int y = row.getFirstCellNum(); y < row.getLastCellNum(); y++) {
                    cell = row.getCell(y);
                    if (cell==null){
                        continue;
                    }
                    title[y] = (String) getCellValue(cell);
                }

            } else
                continue;

            // 遍历当前sheet中的所有行
            for (int j = 1; j < sheet.getLastRowNum() + 1; j++) {
                row = sheet.getRow(j);
                Map<String, Object> m = new HashMap<String, Object>();
                // 遍历所有的列
                if (row==null){
                    continue;
                }
                for (int y = row.getFirstCellNum(); y < row.getLastCellNum(); y++) {
                    cell = row.getCell(y);
                    if (title==null||title[y]==null){
                        continue;
                    }
                    String key = title[y];
                    // log.info(JSON.toJSONString(key));
                    if (cell==null){
                        continue;
                    }
                    m.put(mapping.get(key), getCellValue(cell));
                }
                ls.add(m);
            }

        }
        work.close();
        return ls;
    }

    /**
     * 描述：根据文件后缀，自适应上传文件的版本
     *
     * @param inStr
     *            ,fileName
     * @return
     * @throws Exception
     */
    private static Workbook getWorkbook(InputStream inStr, String fileName) throws Exception {
        Workbook wb = null;
        String fileType = fileName.substring(fileName.lastIndexOf("."));
        if (excel2003L.equals(fileType)) {
            wb = new HSSFWorkbook(inStr); // 2003-
        } else if (excel2007U.equals(fileType)) {
            wb = new XSSFWorkbook(inStr); // 2007+
        } else {
            throw new Exception("解析的文件格式有误！");
        }
        return wb;
    }

    /**
     * 描述：对表格中数值进行格式化
     *
     * @param cell
     * @return
     */
    private static Object getCellValue(Cell cell) {
        Object value = null;
        DecimalFormat df = new DecimalFormat("0.00000000"); // 格式化number String字符
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd"); // 日期格式化
        DecimalFormat df2 = new DecimalFormat("0.00000000"); // 格式化数字

        switch (cell.getCellType()) {
            case STRING:
                value = cell.getRichStringCellValue().getString().trim();
                break;
            case NUMERIC:
                if ("General".equals(cell.getCellStyle().getDataFormatString())) {
                    value = new BigDecimal(df.format(cell.getNumericCellValue())).stripTrailingZeros().toPlainString() ;
                } else if ("m/d/yy".equals(cell.getCellStyle().getDataFormatString())) {
                    value = sdf.format(cell.getDateCellValue());
                } else {
                    value = new BigDecimal(df2.format(cell.getNumericCellValue())).stripTrailingZeros().toPlainString() ;
                }
                break;
            case BOOLEAN:
                value = cell.getBooleanCellValue();
                break;
            case BLANK:
                value = "";
                break;
            default:
                break;
        }
        if(value==null){
            value="";
        }
        return value;
    }


    /**
     * 将url流中的Excel数据转成List<Map<String,Object>> ,配合JSON.parseArray方法直接反射成List<Bean>
     * @param url
     * @param m  m.put("excel表头","字段名");
     * @return
     */
    public static List<Map<String, Object>> parseExcel(String url,Map<String, String> m){
        URL u = null;
        try {
            u = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        InputStream is = null;
        try {
            is = u.openStream();
        } catch (IOException e) {
            e.printStackTrace();
        }


        List<Map<String, Object>> ls = null;
        try {
            ls = ExcelTools.parseExcel(is, url, m);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ls;
    }
}
