// kanji=漢字
package servletpack.KNJL;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.CsvUtils;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 */
public class KNJL672H {

    private static final Log log = LogFactory.getLog("KNJL672H.class");

    private boolean _hasData;

    private Param _param;


    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);
            _hasData = false;

            if(_param._csv) {
                final List<List<String>> outputLines = new ArrayList<List<String>>();
                final Map csvParam = new HashMap();
                csvParam.put("HttpServletRequest", request);

                final String title;
                if("1".equals(_param._outputDiv)) {
                    setOutputCsv1(db2, _param, outputLines); //入試成績集計表
                    title = "入試成績集計表";
                } else if("2".equals(_param._outputDiv)) {
                    setOutputCsv2(db2, _param, outputLines); //受験成績度数分布表
                    title = "受験成績度数分布表";
                } else {
                    setOutputCsv3(db2, _param, outputLines); //奨学生・奨励賞一覧
                    title = "奨学生・奨励賞一覧";
                }
                CsvUtils.outputLines(log, response, _param._examYear + "年度" + title +".csv", outputLines, csvParam);

            } else {
                response.setContentType("application/pdf");
                svf.VrInit();
                svf.VrSetSpoolFileStream(response.getOutputStream());

                if("1".equals(_param._outputDiv)) {
                    _hasData = print1Main(db2, svf); //入試成績集計表
                } else if("2".equals(_param._outputDiv)) {
                    _hasData = print2Main(db2, svf); //受験成績度数分布表
                } else {
                    _hasData = print3Main(db2, svf); //奨学生・奨励賞一覧
                }
            }

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            if(_param._csv) {
            } else {
                if (!_hasData) {
                    svf.VrSetForm("MES001.frm", 0);
                    svf.VrsOut("note", "note");
                    svf.VrEndPage();
                }
                svf.VrQuit();
            }
        }
    }

    //入試成績集計表 CSV
    private void setOutputCsv1(final DB2UDB db2, final Param param, final List<List<String>> outputList) throws SQLException {
        //試験名
        int count = 1;
        List<String> subclassList = new ArrayList<String>();
        for (final Iterator ite = _param._testSubclassMap.keySet().iterator(); ite.hasNext();) {
            final String key = (String) ite.next();
            final SubclassMst mst = (SubclassMst) _param._testSubclassMap.get(key);
            subclassList.add(mst._subclassCd);
            count++;
            if (count > 3)
                break;
        }

        final Map print1Map = getPrint1Map(db2, subclassList); //成績集計取得

        final List<List<String>> headerLineList = new ArrayList<List<String>>();
        final List<String> header1Line = newLine(headerLineList);
        header1Line.addAll(Arrays.asList(_param._examYear + "年度" + _param._testName+ "　受験者成績集計表"));
        final List<String> header2Line = newLine(headerLineList);
        final String date = _param._date != null ? _param._date.replace("-", "/") : "";
        header2Line.addAll(Arrays.asList("受験全体", "", "", "", "", "","","","","", date));
        final List<String> header3Line = newLine(headerLineList);
        List<String> list = new ArrayList<String>();
        list.add("種別");
        list.add("");
        for(final String key : subclassList) {
            final SubclassMst mst = (SubclassMst) _param._testSubclassMap.get(key);
            list.add(mst._subclassName);
            list.add("偏差値");
        }
        list.add("合計");
        list.add("偏差値");
        list.add("評定");
        list.add("偏差値");
        header3Line.addAll(list);
        for(final String key : _param._calkCd) {
            final ClassifyMst ruibetsu = (ClassifyMst)_param._classifyMstMap.get(key);
            final Print1 print1_1 = (Print1)print1Map.get(subclassList.get(0) + key); //試験1
            final Print1 print1_2 = (Print1)print1Map.get(subclassList.get(1) + key); //試験2
            final Print1 print1_3 = (Print1)print1Map.get(subclassList.get(2) + key); //試験3
            final Print1 print1_9 = (Print1)print1Map.get("99" + key); //合計
            final Print1 print1_N = (Print1)print1Map.get("NAISHIN" + key); //評定

            //人数
            final List<String> numLine = newLine(headerLineList);
            numLine.addAll(Arrays.asList(ruibetsu._classifyName, "人数", csv1PrintNum(print1_1),"",csv1PrintNum(print1_2),"",csv1PrintNum(print1_3),"",csv1PrintNum(print1_9),"",csv1PrintNum(print1_N),""));
            //最高点
            final List<String> maxLine = newLine(headerLineList);
            maxLine.addAll(Arrays.asList("", "最高点", csv1PrintMax(print1_1),csv1PrintGdevMax(print1_1), csv1PrintMax(print1_2), csv1PrintGdevMax(print1_2),
                    csv1PrintMax(print1_3), csv1PrintGdevMax(print1_3), csv1PrintMax(print1_9), csv1PrintGdevMax(print1_9), csv1PrintMax(print1_N), csv1PrintGdevMax(print1_N)));
            //最低点
            final List<String> minLine = newLine(headerLineList);
            minLine.addAll(Arrays.asList("", "最低点", csv1PrintMin(print1_1),csv1PrintGdevMin(print1_1), csv1PrintMin(print1_2), csv1PrintGdevMin(print1_2),
                    csv1PrintMin(print1_3), csv1PrintGdevMin(print1_3), csv1PrintMin(print1_9), csv1PrintGdevMin(print1_9), csv1PrintMin(print1_N), csv1PrintGdevMin(print1_N)));
            //平均点
            final List<String> avgLine = newLine(headerLineList);
            avgLine.addAll(Arrays.asList("", "平均点",  csv1PrintAvg(print1_1),csv1PrintGdevAvg(print1_1), csv1PrintAvg(print1_2), csv1PrintGdevAvg(print1_2),
                    csv1PrintAvg(print1_3), csv1PrintGdevAvg(print1_3), csv1PrintAvg(print1_9), csv1PrintGdevAvg(print1_9), csv1PrintAvg(print1_N), csv1PrintGdevAvg(print1_N)));
            //標準偏差
            final List<String> stdDevLine = newLine(headerLineList);
            stdDevLine.addAll(Arrays.asList("", "標準偏差", csv1PrintStddev(print1_1),"",csv1PrintStddev(print1_2),"",csv1PrintStddev(print1_3),"",csv1PrintStddev(print1_9),"",csv1PrintStddev(print1_N),""));
        }
        //空欄埋め
        for(int i = _param._calkCd.length; i < 9; i++ ) {
            final List<String> newLine0 = newLine(headerLineList);
            newLine0.addAll(Arrays.asList("", "人数"));
            final List<String> newLine1 = newLine(headerLineList);
            newLine1.addAll(Arrays.asList("", "最高点"));
            final List<String> newLine2 = newLine(headerLineList);
            newLine2.addAll(Arrays.asList("", "最低点"));
            final List<String> newLine3 = newLine(headerLineList);
            newLine3.addAll(Arrays.asList("", "平均点"));
            final List<String> newLine4 = newLine(headerLineList);
            newLine4.addAll(Arrays.asList("", "標準偏差"));
        }

        final Print1 print1_1 = (Print1)print1Map.get(subclassList.get(0) + "ZZ");
        final Print1 print1_2 = (Print1)print1Map.get(subclassList.get(1) + "ZZ");
        final Print1 print1_3 = (Print1)print1Map.get(subclassList.get(2) + "ZZ");
        final Print1 print1_9 = (Print1)print1Map.get("99" + "ZZ");
        final Print1 print1_N = (Print1)print1Map.get("NAISHIN" + "ZZ");

        //人数
        final List<String> numLine = newLine(headerLineList);
        numLine.addAll(Arrays.asList("全体", "人数", csv1PrintNum(print1_1),"",csv1PrintNum(print1_2),"",csv1PrintNum(print1_3),"",csv1PrintNum(print1_9),"",csv1PrintNum(print1_N),""));
        //最高点
        final List<String> maxLine = newLine(headerLineList);
        maxLine.addAll(Arrays.asList("", "最高点", csv1PrintMax(print1_1),csv1PrintGdevMax(print1_1), csv1PrintMax(print1_2), csv1PrintGdevMax(print1_2),
                csv1PrintMax(print1_3), csv1PrintGdevMax(print1_3), csv1PrintMax(print1_9), csv1PrintGdevMax(print1_9), csv1PrintMax(print1_N), csv1PrintGdevMax(print1_N)));
        //最低点
        final List<String> minLine = newLine(headerLineList);
        minLine.addAll(Arrays.asList("", "最低点", csv1PrintMin(print1_1),csv1PrintGdevMin(print1_1), csv1PrintMin(print1_2), csv1PrintGdevMin(print1_2),
                csv1PrintMin(print1_3), csv1PrintGdevMin(print1_3), csv1PrintMin(print1_9), csv1PrintGdevMin(print1_9), csv1PrintMin(print1_N), csv1PrintGdevMin(print1_N)));
        //平均点
        final List<String> avgLine = newLine(headerLineList);
        avgLine.addAll(Arrays.asList("", "平均点",  csv1PrintAvg(print1_1),csv1PrintGdevAvg(print1_1), csv1PrintAvg(print1_2), csv1PrintGdevAvg(print1_2),
                csv1PrintAvg(print1_3), csv1PrintGdevAvg(print1_3), csv1PrintAvg(print1_9), csv1PrintGdevAvg(print1_9), csv1PrintAvg(print1_N), csv1PrintGdevAvg(print1_N)));
        //標準偏差
        final List<String> stdDevLine = newLine(headerLineList);
        stdDevLine.addAll(Arrays.asList("", "標準偏差", csv1PrintStddev(print1_1),"",csv1PrintStddev(print1_2),"",csv1PrintStddev(print1_3),"",csv1PrintStddev(print1_9),"",csv1PrintStddev(print1_N),""));

        outputList.addAll(headerLineList);
    }

    private String csv1PrintNum(final Print1 print) {
        return print != null ? print._cnt : "";
    }

    private String csv1PrintMax(final Print1 print) {
        return print != null ? print._max : "";
    }

    private String csv1PrintGdevMax(final Print1 print) {
        return print != null ? getSisyaGonyu(print._gdev_Max) : "";
    }

    private String csv1PrintMin(final Print1 print) {
        return print != null ? print._min : "";
    }

    private String csv1PrintGdevMin(final Print1 print) {
        return print != null ? getSisyaGonyu(print._gdev_Min) : "";
    }

    private String csv1PrintAvg(final Print1 print) {
        return print != null ? print._avg : "";
    }

    private String csv1PrintGdevAvg(final Print1 print) {
        return print != null ? getSisyaGonyu(print._gdev_Avg) : "";
    }

    private String csv1PrintStddev(final Print1 print) {
        return print != null ? print._stddev : "";
    }

    //受験成績度数分布表 CSV
    private void setOutputCsv2(final DB2UDB db2, final Param param, final List<List<String>> outputList) throws SQLException {
        //1:入試日程毎
        final Map scoreSubclassMap = getPrint2Map(db2,false); //科目毎の成績カウント

        final List<List<String>> headerLineList = new ArrayList<List<String>>();
        final List<String> header1Line = newLine(headerLineList);
        final String date = _param._date != null ? _param._date.replace("-", "/") : "";
        header1Line.addAll(Arrays.asList( _param._examYear + "年度受験成績度数分布表　入試日程:" + _param._testName,"","","","","","","","","","","","","","","","","","","","","","","","","","","","",date));
        final List<String> header2Line = newLine(headerLineList);

        int cnt = 1;

        //科目名と集計ONの類別名称分の空欄設定
        for(Iterator ite = _param._testSubclassMap.keySet().iterator(); ite.hasNext();) {
            final String key = (String) ite.next();
            final SubclassMst mst = (SubclassMst) _param._testSubclassMap.get(key);
            List<String> headerList = new ArrayList<String>();
            headerList.add("");
            headerList.add(mst._subclassName);
            setSameSize(headerList, 10);
            header2Line.addAll(headerList);
            if(++cnt > 3) break;
        }
        //類別名称
        List<String> list = new ArrayList<String>();
        list.add("");
        for(Iterator ite = _param._classifyMstMap.keySet().iterator(); ite.hasNext();) {
            final String key = (String) ite.next();
            final ClassifyMst mst = (ClassifyMst) _param._classifyMstMap.get(key);
            if("1".equals(mst._calcFlg)) {
                list.add(mst._classifyName);
            }
        }
        for(int i = list.size(); i < 9; i++) {
            list.add("");
        }
        list.add("総計");
        List<String> list2 = new ArrayList<String>();
        list2.add("点数");
        for(int i = 0; i < list.size() - 1; i++ ) {
            list2.add("人 累計");
        }

        setSameSize(list, 10);
        setSameSize(list2, 10);
        final List<String> header3Line = newLine(headerLineList);
        header3Line.addAll(list);
        header3Line.addAll(list);
        header3Line.addAll(list);
        final List<String> header4Line = newLine(headerLineList);
        header4Line.addAll(list2);
        header4Line.addAll(list2);
        header4Line.addAll(list2);

        final List<List<String>> columnList1 = new ArrayList<List<String>>();

        // 度数分布表
        cnt = 1;
        for (Iterator ite = _param._testSubclassMap.keySet().iterator(); ite.hasNext();) {
            final String Key = (String)ite.next();
            final  SubclassMst mst = (SubclassMst)_param._testSubclassMap.get(Key);
            String testCd = mst._subclassCd;
            final Map totalMap = new TreeMap();
            final List<List<String>> testblock = new ArrayList<List<String>>();
            final List<String> pointLine = newLine(testblock);

            // 集計対象ONの類別コード + 合計設定
            final Map divMap = new LinkedMap();
            for(int i = 0; i < _param._calkCd.length; i++) {
                List<String> pointDiv = newLine(testblock);
                final String key = _param._calkCd[i];
                divMap.put(key, pointDiv);
            }

            for(int i = testblock.size(); i < 9; i++) {
                List<String> newLine = newLine(testblock);
                divMap.put(String.valueOf(i), newLine);
            }

            List<String> totalLine = newLine(testblock);
            divMap.put("99", totalLine);

            for(int point = 100; point >= 1; point--) {
                pointLine.add(String.valueOf(point));
                final  Map printMap = (Map)scoreSubclassMap.get(testCd + point);
                printCsv2(divMap, printMap, totalMap);
            }
            columnList1.addAll(testblock);
            if(++cnt > 3) break;
        }

        //2:受験全体
        final Map scoreAllMap = getPrint2Map(db2,true); //科目合算の成績カウント
        final List<List<String>> headerLineList2 = new ArrayList<List<String>>();
        final List<String> newLine1 = newLine(headerLineList2);
        final List<String> newLine2 = newLine(headerLineList2);
        final List<String> header1Line2 = newLine(headerLineList2);
        header1Line2.addAll(Arrays.asList( _param._examYear + "年度受験成績度数分布表　受験全体","","","","","","","","","","","","","","","","","","","","","","","","","","","","",date));
        final List<String> newLine3 = newLine(headerLineList2);
        final List<String> header3Line2 = newLine(headerLineList2);
        header3Line2.addAll(list);
        header3Line2.addAll(list);
        header3Line2.addAll(list);
        final List<String> header4Line2 = newLine(headerLineList2);
        header4Line2.addAll(list2);
        header4Line2.addAll(list2);
        header4Line2.addAll(list2);

        // 度数分布表
        final Map totalMap = new TreeMap();
        final List<List<String>> columnList2 = new ArrayList<List<String>>();

        for (int count = 3; count > 0; count--) {
            final List<List<String>> testblock = new ArrayList<List<String>>();
            final List<String> pointLine = newLine(testblock);

            // 集計対象ONの類別コード + 合計設定
            final Map divMap = new LinkedMap();
            for (int i = 0; i < _param._calkCd.length; i++) {
                List<String> pointDiv = newLine(testblock);
                final String key = _param._calkCd[i];
                divMap.put(key, pointDiv);
            }
            for(int i = testblock.size(); i < 9; i++) {
                List<String> newLine = newLine(testblock);
                divMap.put(String.valueOf(i), newLine);
            }

            List<String> totalLine = newLine(testblock);
            divMap.put("99", totalLine);

            int pointi = count * 100; //カウンタ変数
            for (int point = pointi; point >= pointi - 99; point--) { //1週目:300～201,2週目:200～101,3週目:100～1
                pointLine.add(String.valueOf(point));
                final Map printMap = (Map) scoreAllMap.get(String.valueOf(point));
                printCsv2(divMap, printMap, totalMap);
            }
            columnList2.addAll(testblock);
        }

        outputList.addAll(headerLineList);
        outputList.addAll(columnListToLines(columnList1));
        outputList.addAll(headerLineList2);
        outputList.addAll(columnListToLines(columnList2));
    }

    //奨学生・奨励賞一覧 CSV
    private void setOutputCsv3(final DB2UDB db2, final Param param, final List<List<String>> outputList) throws SQLException {

        final List<List<String>> headerLineList = new ArrayList<List<String>>();

        final Map scholarshipsMap = getPrint3Map(db2,true);
        printCsv3(scholarshipsMap, headerLineList, "奨学生候補者");

        final List<String> newline = newLine(headerLineList);
        final Map encouragementMap = getPrint3Map(db2,false);
        printCsv3(encouragementMap, headerLineList, "奨励賞候補者");

        outputList.addAll(headerLineList);
    }

    private void printCsv3(final Map printMap, final List headerLineList, final String title) {
        final List<String> header1Line = newLine(headerLineList);
        final String date = _param._date != null ? _param._date.replace("-", "/") : "";
        header1Line.addAll(Arrays.asList( _param._examYear + "年度", _param._testName, "","","","","","","","","","","","","","","","","","","","","","","","","1頁"));
        final List<String> header2Line = newLine(headerLineList);
        final List<String> header3Line = newLine(headerLineList);
        header2Line.addAll(Arrays.asList(title, "", "", "", "", "", "", "", "", "", "", "", "内申","","","","","","","","","","","","",date, _param._time));

        final List<String> confidentialName = new ArrayList<String>();
        for(Iterator ite2 = _param._confidentialName.keySet().iterator(); ite2.hasNext();) {
            final String key = (String)ite2.next();
            final String name = (String)_param._confidentialName.get(key);
            confidentialName.add(name);
        }
        final List<String> testSubclassName = new ArrayList<String>();
        for(Iterator ite3 = _param._testSubclassMap.keySet().iterator(); ite3.hasNext();) {
            final String key = (String)ite3.next();
            final SubclassMst mst = (SubclassMst)_param._testSubclassMap.get(key);
            testSubclassName.add(mst._subclassName);

        }
        header3Line.addAll(Arrays.asList("整理", "入学", "手続", "合格", "類別", "受験番号", "氏名", "ふりがな", "性別", "生年月日", "出身", "中学",
                confidentialName.get(0), confidentialName.get(1), confidentialName.get(2), confidentialName.get(3), confidentialName.get(4),
                "3科", "5科", "9科", testSubclassName.get(0), testSubclassName.get(1), testSubclassName.get(2), "合計", "順位", "面接", "備考"));

        int no = 1;
        for(final Iterator ite = printMap.keySet().iterator(); ite.hasNext();) {
            final String key = (String)ite.next();
            final Print3 print3 = (Print3)printMap.get(key);

            final List<String> printLine = newLine(headerLineList);
            final String seiri = String.valueOf(no); //整理
            final String nyugaku = "1".equals(print3._entdiv) ? "○" : ""; //入学
            final String tetsuduki = "1".equals(print3._procedurediv) ? "○" : ""; //手続
            final String goukaku = "1".equals(print3._judgement) ? "○" : ""; //合格
            final String ruibetsu = print3._mark; //類別
            final String jukenbangou = print3._receptno; //受験番号
            final String name = print3._name; //氏名
            final String kana = print3._name_Kana; //ふりがな
            final String sex = "1".equals(print3._sex) ? "男" : "女"; //性別
            final String birthday = print3._birthday != null ? print3._birthday.replace("-", "/") : ""; //生年月日
            final String div = print3._district; //出身
            final String school = print3._finschool_Name; //中学
            final String rpt1 = print3._confidential_Rpt01; //内申1
            final String rpt2 = print3._confidential_Rpt02; //内申2
            final String rpt3 = print3._confidential_Rpt03; //内申3
            final String rpt4 = print3._confidential_Rpt04; //内申4
            final String rpt5 = print3._confidential_Rpt05; //内申5
            final String total3 = print3._total3; //3科
            final String total5 = print3._total5; //5科
            final String total9 = print3._total_All; //9科
            final String score1 = print3._score1; //試験1
            final String score2 = print3._score2; //試験2
            final String score3 = print3._score3; //試験3
            final String goukei = print3._total4; //合計
            final String rank = print3._total_Rank4; //順位
            final String interview = print3._name1; //面接
            final String remark = StringUtils.defaultString(print3._remark8) + StringUtils.defaultString(print3._remark9) + StringUtils.defaultString(print3._remark10);

            printLine.addAll(Arrays.asList(seiri, nyugaku, tetsuduki, goukaku, ruibetsu, jukenbangou,name,
                    kana, sex, birthday, div, school, rpt1, rpt2, rpt3, rpt4, rpt5, total3, total5, total9, score1,
                    score2, score3, goukei, rank, interview, remark));
            no++;
        }
    }

    private String printDiv(final String str, final int total) {
        if(str == null) {
            return null;
        }
        return str + "   " + total;
    }

    private List<String> newLine(final List<List<String>> listList) {
        final List<String> line = line();
        listList.add(line);
        return line;
    }

    private List<String> line() {
        return line(0);
    }

    private List<String> line(final int size) {
        final List<String> line = new ArrayList<String>();
        for (int i = 0; i < size; i++) {
            line.add(null);
        }
        return line;
    }

    private <T> List<T> setSameSize(final List<T> list, final int max) {
        for (int i = list.size(); i < max; i++) {
            list.add(null);
        }
        return list;
    }

    private List<List<String>> columnListToLines(final List<List<String>> columnList) {
        final List<List<String>> lines = new ArrayList<List<String>>();
        int maxLine = 0;
        for (final List<String> column : columnList) {
            maxLine = Math.max(maxLine, column.size());
        }
        for (int li = 0; li < maxLine; li++) {
            lines.add(line(columnList.size()));
        }
        for (int ci = 0; ci < columnList.size(); ci++) {
            final List<String> column = columnList.get(ci);
            for (int li = 0; li < column.size(); li++) {
                lines.get(li).set(ci, column.get(li));
            }
        }
        return lines;
    }

    private void printCsv2(final Map divMap, final Map printMap, final Map totalMap) {
        if (printMap == null) {
            for (Iterator divite = divMap.keySet().iterator(); divite.hasNext();) {
                final String key = (String) divite.next();
                final List<String> divlist = (List) divMap.get(key);
                divlist.add("");
            }
        } else {
            for (Iterator divite = divMap.keySet().iterator(); divite.hasNext();) {
                final String key = (String) divite.next();
                final List<String> divlist = (List) divMap.get(key);
                final String divPoint = (String) printMap.get(key);
                if (divPoint == null) {
                    divlist.add("");
                } else {
                    int total = 0;
                    if (totalMap.containsKey(key)) {
                        total = Integer.parseInt((String) totalMap.get(key));
                        total += Integer.parseInt(divPoint);
                        totalMap.put(key, String.valueOf(total));
                    } else {
                        totalMap.put(key, divPoint);
                        total = Integer.parseInt(divPoint);
                    }
                    divlist.add(total == 0 ? "" : String.valueOf(total));
                }
            }
        }
    }

    //入試成績集計表
    private boolean print1Main(final DB2UDB db2, final Vrw32alp svf) throws SQLException {

        svf.VrSetForm("KNJL672H_1.frm", 1);
        svf.VrsOut("TITLE", _param._examYear + "年度" + _param._testName + "　受験者成績集計表"); //タイトル
        svf.VrsOut("SUBTITLE", "受験全体"); //サブタイトル
        final String date = _param._date != null ? _param._date.replace("-", "/") : "";
        svf.VrsOut("DATE", date); //日付

        //試験名
        int count = 1;
        List<String> subclassList = new ArrayList<String>();
        for(final Iterator ite = _param._testSubclassMap.keySet().iterator(); ite.hasNext();) {
            final String key = (String)ite.next();
            final SubclassMst mst = (SubclassMst)_param._testSubclassMap.get(key);
            svf.VrsOut("SUBCLASS_NAME" + count, mst._subclassName);
            subclassList.add(mst._subclassCd);
            count++;
            if(count > 3) break;
        }
        svf.VrsOut("SUBCLASS_NAME4", "合計");
        svf.VrsOut("SUBCLASS_NAME5", "評定");

        final Map print1Map = getPrint1Map(db2, subclassList); //成績集計取得
        log.debug(print1Map);

        //集計ON類別コード分のループ
        int line = 1;
        for(final String cdKey : _param._calkCd) {
            final ClassifyMst mst = (ClassifyMst)_param._classifyMstMap.get(cdKey);
            svf.VrsOutn("KIND", line, mst._classifyName);
            int col = 1;
            //試験1～3
            for(final String subclassCd : subclassList) {
                final String key = subclassCd + cdKey;
                final Print1 print1 = (Print1)print1Map.get(key);
                print1Record(svf, print1, col, line);
                col++;
            }
            //合計
            final String total = "99" + cdKey;
            final Print1 printTotal = (Print1)print1Map.get(total);
            print1Record(svf, printTotal, col, line);
            col++;

            //評定
            final String naishin = "NAISHIN" + cdKey;
            final Print1 printNaishin = (Print1)print1Map.get(naishin);
            print1Record(svf, printNaishin, col, line);
            line++;
        }

        //最終行　全体分
        svf.VrsOutn("KIND", 10, "全体");
        int col = 1;
        //試験1～3
        for(final String subclassCd : subclassList) {
            final String key = subclassCd + "ZZ";
            final Print1 print1 = (Print1)print1Map.get(key);
            print1Record(svf, print1, col, 10);
            col++;
        }
        //合計
        final String total = "99" + "ZZ";
        final Print1 printTotal = (Print1)print1Map.get(total);
        print1Record(svf, printTotal, col, 10);
        col++;
        //評定
        final String naishin = "NAISHIN" + "ZZ";
        final Print1 printNaishin = (Print1)print1Map.get(naishin);
        print1Record(svf, printNaishin, col, 10);


        svf.VrEndPage();
        return true;
    }

    private void print1Record(final Vrw32alp svf, final Print1 print, final int col, final int line) {
        if(print != null) {
            svf.VrsOutn("NUM" + col, line, print._cnt);
            svf.VrsOutn("MAX" + col, line, print._max);
            svf.VrsOutn("MIN" + col, line, print._min);
            svf.VrsOutn("AVE" + col, line, print._avg);
            svf.VrsOutn("ST_DEVI" + col, line, print._stddev);
            svf.VrsOutn("DEVI" + col + "_1", line, getSisyaGonyu(print._gdev_Max));
            svf.VrsOutn("DEVI" + col + "_2", line, getSisyaGonyu(print._gdev_Min));
            svf.VrsOutn("DEVI" + col + "_3", line, getSisyaGonyu(print._gdev_Avg));
        }
    }

    private String getSisyaGonyu(final String doubleValue) {
        return null == doubleValue ? null : new BigDecimal(doubleValue).setScale(0, BigDecimal.ROUND_HALF_UP).toString();
    }

    // 受験成績度数分布表 科目毎、科目合算
    private boolean print2Main(final DB2UDB db2, final Vrw32alp svf) throws SQLException {

        final Map scoreSubclassMap = getPrint2Map(db2,false); //科目毎の成績カウント

        // 集計対象の類別印字位置を設定
        final Map divFieldMap = new LinkedMap();
        int no = 1;
        for(int i = 0; i < _param._calkCd.length; i++) {
            divFieldMap.put(_param._calkCd[i], String.valueOf(no));
            no++;
        }
        divFieldMap.put("99", "9");

        svf.VrSetForm("KNJL672H_2.frm", 1);
        svf.VrsOut("TITLE",  _param._examYear + "年度受験成績度数分布表　入試日程：" + _param._testName); //タイトル
        final String date = _param._date != null ? _param._date.replace("-", "/") : "";
        svf.VrsOut("DATE", date); //日付

        int cnt = 1;
        for(Iterator ite = _param._testSubclassMap.keySet().iterator(); ite.hasNext();) {
            final String key = (String)ite.next();
            final SubclassMst mst= (SubclassMst) _param._testSubclassMap.get(key);
            svf.VrsOut("SUBCLASS_NAME" + cnt, mst._subclassName); //科目名
            if(++cnt > 3) break;
        }

        svf.VrsOut("SUBTITLE", "入試日程：" + _param._testName); //サブタイトル
        // 集計対象の類別名称分設定
        for(int i = 1; i <= 3; i++) {
            int col = 1;
            for(Iterator ite = _param._classifyMstMap.keySet().iterator(); ite.hasNext();) {
                final String key = (String)ite.next();
                final ClassifyMst mst = (ClassifyMst)_param._classifyMstMap.get(key);
                if("1".equals(mst._calcFlg)) {
                    svf.VrsOut("KIND" + i + "_" + col, mst._classifyName); //類別名
                    col++;
                }
            }
            svf.VrsOut("KIND" + i + "_9", "総計"); //総計
        }
        int fieldCol = 1;

        // 度数分布表
        for (Iterator ite = _param._testSubclassMap.keySet().iterator(); ite.hasNext();) {
            final String Key = (String)ite.next();
            final  SubclassMst mst = (SubclassMst)_param._testSubclassMap.get(Key);
            String testCd = mst._subclassCd;
            final Map totalMap = new TreeMap(); //累計人数の計算用
            int row = 1;
            for(int point = 100; point > 0; point--) {
                svf.VrsOutn("SCORE" + fieldCol, row, String.valueOf(point)); //点数
                final  Map printMap = (Map)scoreSubclassMap.get(testCd + point);
                if(printMap == null) {
                    row++;
                    continue;
                }
                for (Iterator divite = printMap.keySet().iterator(); divite.hasNext();) {
                    final String divKey = (String)divite.next();
                    final String divPoint = (String)printMap.get(divKey);
                    final String divField = (String)divFieldMap.get(divKey);

                    int total = 0;
                    if(totalMap.containsKey(divKey)) {
                        total = Integer.parseInt((String)totalMap.get(divKey));
                        total += Integer.parseInt(divPoint);
                        totalMap.put(divKey, String.valueOf(total));
                    } else {
                        totalMap.put(divKey, divPoint);
                        total = Integer.parseInt(divPoint);
                    }
                    svf.VrsOutn("NUM" + fieldCol + "_" + divField, row, String.valueOf(total)); //人 累計
                }
                row++;
            }
            fieldCol++;
        }
        svf.VrEndPage();

        // 科目合算
        final Map scoreAllMap = getPrint2Map(db2,true); //科目合算の成績カウント

        svf.VrSetForm("KNJL672H_2.frm", 1);
        svf.VrsOut("TITLE", _param._examYear + "年度受験成績度数分布表" + "　受験全体"); //タイトル
        svf.VrsOut("DATE", date); //日付

        // 集計対象の類別名称分設定
        for (int i = 1; i <= 3; i++) {
            int col = 1;
            for (Iterator ite = _param._classifyMstMap.keySet().iterator(); ite.hasNext();) {
                final String key = (String) ite.next();
                final ClassifyMst mst = (ClassifyMst) _param._classifyMstMap.get(key);
                if ("1".equals(mst._calcFlg)) {
                    svf.VrsOut("KIND" + i + "_" + col, mst._classifyName); //類別名
                    col++;
                }
            }
            svf.VrsOut("KIND" + i + "_9", "総計"); //総計
        }
        final Map totalMap = new TreeMap();
        fieldCol = 0;
        int row = 0;

        // 度数分布表
        for(int point = 300; point > 0; point--) {
            if(point % 100 == 0) {
                fieldCol++;
                row = 1;
            }
            svf.VrsOutn("SCORE" + fieldCol, row, String.valueOf(point)); // 点数
            final  Map printMap = (Map)scoreAllMap.get(String.valueOf(point));
            if(printMap == null) {
                row++;
                continue;
            }
            for (Iterator divite = printMap.keySet().iterator(); divite.hasNext();) {
                final String divKey = (String)divite.next();
                final String divPoint = (String)printMap.get(divKey);
                final String divField = (String)divFieldMap.get(divKey);

                int total = 0;
                if(totalMap.containsKey(divKey)) {
                    total = Integer.parseInt((String)totalMap.get(divKey));
                    total += Integer.parseInt(divPoint);
                    totalMap.put(divKey, String.valueOf(total));
                } else {
                    totalMap.put(divKey, divPoint);
                    total = Integer.parseInt(divPoint);
                }
                svf.VrsOutn("NUM" + fieldCol + "_" + divField, row, String.valueOf(total)); // 人 累計
            }
            row++;
        }
    svf.VrEndPage();
    return true;
    }

    //奨学生・奨励賞一覧
    private boolean print3Main(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final Map scholarshipsMap = getPrint3Map(db2,true); //奨学生候補取得
        final Map encouragementMap = getPrint3Map(db2,false); //奨励賞候補取得


        boolean hasdata1 = print3Record(svf, scholarshipsMap, "奨学生候補者");
        boolean hasdata2 = print3Record(svf, encouragementMap, "奨励賞候補者");

        return hasdata1 || hasdata2;
    }

    private boolean print3Record(final Vrw32alp svf, final Map printMap, final String title) {
        int no = 0;
        int page = 1;
        boolean hasdata = false;
        for(Iterator ite = printMap.keySet().iterator(); ite.hasNext();) {
            if(no == 0 || no > 50) {
                if(no > 50) {
                    svf.VrEndPage();
                    page++;
                }
                svf.VrSetForm("KNJL672H_3.frm", 1);
                svf.VrsOut("TITLE", _param._examYear + "年度" + _param._testName); //タイトル
                svf.VrsOut("SUBTITLE", title); //サブタイトル
                final String date = _param._date != null ? _param._date.replace("-", "/") : "";
                svf.VrsOut("DATE", date + " " + _param._time); //日付
                svf.VrsOut("PAGE", String.valueOf(page) + "頁"); //ページ
                int cnt = 1;
                for(Iterator ite2 = _param._confidentialName.keySet().iterator(); ite2.hasNext();) {
                    final String key = (String)ite2.next();
                    final String name = (String)_param._confidentialName.get(key);
                    svf.VrsOut("REP_NAME" + cnt, name); //内申教科名
                    cnt++;
                }
                cnt = 1;
                for(Iterator ite3 = _param._testSubclassMap.keySet().iterator(); ite3.hasNext();) {
                    final String key = (String)ite3.next();
                    final SubclassMst mst = (SubclassMst)_param._testSubclassMap.get(key);
                    svf.VrsOut("SUBCLASS_NAME" + cnt, mst._subclassName); //試験名
                    cnt++;
                }
                no = 1;
            }

            final String key = (String)ite.next();
            final Print3 print3 = (Print3)printMap.get(key);
            svf.VrsOutn("NO", no, String.valueOf(no)); // 連番
            svf.VrsOutn("ENT", no, "1".equals(print3._entdiv) ? "○" : ""); //入学
            svf.VrsOutn("PROCEDURE", no, "1".equals(print3._procedurediv) ? "○" : ""); //手続
            svf.VrsOutn("PASS", no, "1".equals(print3._judgement) ? "○" : ""   ); //合格
            svf.VrsOutn("KIND", no, print3._mark); //類別
            svf.VrsOutn("EXAM_NO", no, print3._receptno); //受験番号
            final String nameField = getFieldName(print3._name, false);
            svf.VrsOutn("NAME" + nameField, no, print3._name); //氏名
            final String kanaField = getFieldName(print3._name_Kana, true);
            svf.VrsOutn("KANA" + kanaField, no, print3._name_Kana); //かな
            svf.VrsOutn("SEX", no, "1".equals(print3._sex) ? "男" : "女"); //性別
            final String birthday = print3._birthday != null ? print3._birthday.replace("-", "/") : "";
            svf.VrsOutn("BIRTHDAY", no, birthday); //誕生日
            svf.VrsOutn("DISTRICT", no, print3._district); //出身
            svf.VrsOutn("FINSCHOOL_NAME", no, print3._finschool_Name); //学校名
            svf.VrsOutn("REP1", no, print3._confidential_Rpt01); //内申1
            svf.VrsOutn("REP2", no, print3._confidential_Rpt02); //内申2
            svf.VrsOutn("REP3", no, print3._confidential_Rpt03); //内申3
            svf.VrsOutn("REP4", no, print3._confidential_Rpt04); //内申4
            svf.VrsOutn("REP5", no, print3._confidential_Rpt05); //内申5
            svf.VrsOutn("TOTAL_REP1", no, print3._total3); //3科
            svf.VrsOutn("TOTAL_REP2", no, print3._total5); //5科
            svf.VrsOutn("TOTAL_REP3", no, print3._total_All); //9科
            svf.VrsOutn("SCORE1", no, print3._score1); //試験1
            svf.VrsOutn("SCORE2", no, print3._score2); //試験2
            svf.VrsOutn("SCORE3", no, print3._score3); //試験3
            svf.VrsOutn("TOTAL_SCORE", no, print3._total4); //合計
            svf.VrsOutn("RANK", no, print3._total_Rank4); //順位
            svf.VrsOutn("INTERVIEW", no, print3._name1); //順位

            final String str = StringUtils.defaultString(print3._remark8) + StringUtils.defaultString(print3._remark9) + StringUtils.defaultString(print3._remark10);
            final String remark;
                if(str.endsWith("　")){
                    remark = str.substring(0,str.length() - 1);
                } else {
                    remark = str;
                }
            final String remarkField = getFieldRemark(remark);
            svf.VrsOutn("REMARK" + remarkField, no, remark); //備考

            no++;
            hasdata = true;
        }
        if (hasdata) {
            svf.VrEndPage();
        }

        return hasdata;
    }

    private String getFieldRemark(final String str) {
        final int keta = KNJ_EditEdit.getMS932ByteLength(str);
        return keta <= 24 ? "1" : keta <= 30 ? "2" : "3";
    }

    private String getFieldName(final String str, final boolean kana) {
        final int keta = KNJ_EditEdit.getMS932ByteLength(str);
        if(kana) {
            return keta <= 30 ? "1" : "2";
        } else {
            return keta <= 20 ? "1" : keta <= 30 ? "2" : "3";
        }

    }

    // 入試成績集計表 成績取得SQL
    private Map getPrint1Map(final DB2UDB db2, final List subclassList) throws SQLException {
        Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final StringBuffer stb = new StringBuffer();
            //試験1の平均、標準偏差
            stb.append(" WITH AVG_STDDEV1 AS ( ");
            stb.append(" SELECT ");
            stb.append("   BASE.TESTDIV1, ");
            stb.append("   DECIMAL(ROUND(AVG(FLOAT(SCORE.SCORE))*10,0)/10,5,1) AS AVG1, ");
            stb.append("   DECIMAL(ROUND(STDDEV(FLOAT(SCORE.SCORE))*10,0)/10,5,1) AS STDDEV1 ");
            stb.append(" FROM ");
            stb.append("   ENTEXAM_SCORE_DAT AS SCORE ");
            stb.append(" INNER JOIN ");
            stb.append("   ENTEXAM_RECEPT_DAT RECEPT ON SCORE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR AND SCORE.APPLICANTDIV = RECEPT.APPLICANTDIV AND SCORE.RECEPTNO = RECEPT.RECEPTNO AND SCORE.TESTDIV = RECEPT.TESTDIV    ");
            stb.append(" INNER JOIN ");
            stb.append("   ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV AND BASE.EXAMNO = RECEPT.EXAMNO AND BASE.TESTDIV = RECEPT.TESTDIV    ");
            stb.append(" WHERE ");
            stb.append("   SCORE.ENTEXAMYEAR = '" + _param._examYear + "' AND ");
            stb.append("   SCORE.APPLICANTDIV = '" + _param._applicantDiv + "' AND ");
            stb.append("   SCORE.TESTDIV = '02' AND ");
            stb.append("   SCORE.EXAM_TYPE = '1' AND ");
            stb.append("   SCORE.TESTSUBCLASSCD = '" + subclassList.get(0) + "' ");
            stb.append(" GROUP BY ");
            stb.append("   TESTDIV1 ");
            //試験2の平均、標準偏差
            stb.append(" ), AVG_STDDEV2 AS ( ");
            stb.append(" SELECT ");
            stb.append("   BASE.TESTDIV1, ");
            stb.append("   DECIMAL(ROUND(AVG(FLOAT(SCORE.SCORE))*10,0)/10,5,1) AS AVG2, ");
            stb.append("   DECIMAL(ROUND(STDDEV(FLOAT(SCORE.SCORE))*10,0)/10,5,1) AS STDDEV2 ");
            stb.append(" FROM ");
            stb.append("   ENTEXAM_SCORE_DAT AS SCORE ");
            stb.append(" INNER JOIN ");
            stb.append("   ENTEXAM_RECEPT_DAT RECEPT ON SCORE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR AND SCORE.APPLICANTDIV = RECEPT.APPLICANTDIV AND SCORE.RECEPTNO = RECEPT.RECEPTNO AND SCORE.TESTDIV = RECEPT.TESTDIV    ");
            stb.append(" INNER JOIN ");
            stb.append("   ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV AND BASE.EXAMNO = RECEPT.EXAMNO AND BASE.TESTDIV = RECEPT.TESTDIV    ");
            stb.append(" WHERE ");
            stb.append("   SCORE.ENTEXAMYEAR = '" + _param._examYear + "' AND ");
            stb.append("   SCORE.APPLICANTDIV = '" + _param._applicantDiv + "' AND ");
            stb.append("   SCORE.TESTDIV = '02' AND ");
            stb.append("   SCORE.EXAM_TYPE = '1' AND ");
            stb.append("   SCORE.TESTSUBCLASSCD = '" + subclassList.get(1) + "' ");
            stb.append(" GROUP BY ");
            stb.append("   TESTDIV1 ");
            //試験3の平均、標準偏差
            stb.append(" ), AVG_STDDEV3 AS ( ");
            stb.append(" SELECT ");
            stb.append("   BASE.TESTDIV1, ");
            stb.append("   DECIMAL(ROUND(AVG(FLOAT(SCORE.SCORE))*10,0)/10,5,1) AS AVG3, ");
            stb.append("   DECIMAL(ROUND(STDDEV(FLOAT(SCORE.SCORE))*10,0)/10,5,1) AS STDDEV3 ");
            stb.append(" FROM ");
            stb.append("   ENTEXAM_SCORE_DAT AS SCORE ");
            stb.append(" INNER JOIN ");
            stb.append("   ENTEXAM_RECEPT_DAT RECEPT ON SCORE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR AND SCORE.APPLICANTDIV = RECEPT.APPLICANTDIV AND SCORE.RECEPTNO = RECEPT.RECEPTNO AND SCORE.TESTDIV = RECEPT.TESTDIV    ");
            stb.append(" INNER JOIN ");
            stb.append("   ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV AND BASE.EXAMNO = RECEPT.EXAMNO AND BASE.TESTDIV = RECEPT.TESTDIV    ");
            stb.append(" WHERE ");
            stb.append("   SCORE.ENTEXAMYEAR = '" + _param._examYear + "' AND ");
            stb.append("   SCORE.APPLICANTDIV = '" + _param._applicantDiv + "' AND ");
            stb.append("   SCORE.TESTDIV = '02' AND ");
            stb.append("   SCORE.EXAM_TYPE = '1' AND ");
            stb.append("   SCORE.TESTSUBCLASSCD = '" + subclassList.get(2) + "' ");
            stb.append(" GROUP BY ");
            stb.append("   TESTDIV1 ");
            //試験合計の平均、標準偏差
            stb.append(" ), AVG_STDDEV9 AS ( ");
            stb.append(" SELECT ");
            stb.append("   BASE.TESTDIV1, ");
            stb.append("   DECIMAL(ROUND(AVG(FLOAT(RECEPT.TOTAL4))*10,0)/10,5,1) AS AVG9, ");
            stb.append("   DECIMAL(ROUND(STDDEV(FLOAT(RECEPT.TOTAL4))*10,0)/10,5,1) AS STDDEV9 ");
            stb.append(" FROM ");
            stb.append("   ENTEXAM_RECEPT_DAT RECEPT ");
            stb.append(" INNER JOIN ");
            stb.append("   ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV AND BASE.EXAMNO = RECEPT.EXAMNO AND BASE.TESTDIV = RECEPT.TESTDIV    ");
            stb.append(" WHERE ");
            stb.append("   RECEPT.ENTEXAMYEAR = '" + _param._examYear + "' AND ");
            stb.append("   RECEPT.APPLICANTDIV = '" + _param._applicantDiv + "' AND ");
            stb.append("   RECEPT.TESTDIV = '02' AND ");
            stb.append("   RECEPT.EXAM_TYPE = '1' ");
            stb.append(" GROUP BY ");
            stb.append("   TESTDIV1 ");
            //内申の平均、標準偏差
            stb.append(" ), AVG_STDDEVZ AS ( ");
            stb.append(" SELECT ");
            stb.append("   BASE.TESTDIV1, ");
            stb.append("   DECIMAL(ROUND(AVG(FLOAT(CONFRPT.TOTAL_ALL))*10,0)/10,5,1) AS AVGZ, ");
            stb.append("   DECIMAL(ROUND(STDDEV(FLOAT(CONFRPT.TOTAL_ALL))*10,0)/10,5,1) AS STDDEVZ ");
            stb.append(" FROM ");
            stb.append("   ENTEXAM_RECEPT_DAT RECEPT ");
            stb.append(" INNER JOIN ");
            stb.append("   ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV AND BASE.EXAMNO = RECEPT.EXAMNO AND BASE.TESTDIV = RECEPT.TESTDIV  ");
            stb.append(" INNER JOIN ");
            stb.append("   ENTEXAM_APPLICANTCONFRPT_DAT CONFRPT ON BASE.ENTEXAMYEAR = CONFRPT.ENTEXAMYEAR AND BASE.APPLICANTDIV = CONFRPT.APPLICANTDIV AND BASE.EXAMNO = CONFRPT.EXAMNO ");
            stb.append(" WHERE ");
            stb.append("   RECEPT.ENTEXAMYEAR = '" + _param._examYear + "' AND ");
            stb.append("   RECEPT.APPLICANTDIV = '" + _param._applicantDiv + "' AND ");
            stb.append("   RECEPT.TESTDIV = '02' AND ");
            stb.append("   RECEPT.EXAM_TYPE = '1' ");
            stb.append(" GROUP BY ");
            stb.append("   TESTDIV1 ");
            //ベースとなる表
            stb.append(" ), SCOREBASE AS (SELECT ");
            stb.append("     RECEPT.RECEPTNO, ");
            stb.append("     SCORE1.SCORE AS SCORE1, ");
            stb.append("     SCORE2.SCORE AS SCORE2, ");
            stb.append("     SCORE3.SCORE AS SCORE3, ");
            stb.append("     RECEPT.TOTAL4 AS SCORE9, ");
            stb.append("     CONFRPT.TOTAL_ALL AS NAISHIN, ");
            stb.append("     BASE.TESTDIV1, ");
            stb.append("     T1.AVG1, ");
            stb.append("     T1.STDDEV1, ");
            stb.append("     CASE WHEN 0 < T1.STDDEV1 THEN DECIMAL(ROUND((10*(SCORE1.SCORE-T1.AVG1)/T1.STDDEV1+50)*10,0)/10,5,1) END AS GRADE_DEVIATION1, ");
            stb.append("     T2.AVG2, ");
            stb.append("     T2.STDDEV2, ");
            stb.append("     CASE WHEN 0 < T2.STDDEV2 THEN DECIMAL(ROUND((10*(SCORE2.SCORE-T2.AVG2)/T2.STDDEV2+50)*10,0)/10,5,1) END AS GRADE_DEVIATION2, ");
            stb.append("     T3.AVG3, ");
            stb.append("     T3.STDDEV3, ");
            stb.append("     CASE WHEN 0 < T3.STDDEV3 THEN DECIMAL(ROUND((10*(SCORE3.SCORE-T3.AVG3)/T3.STDDEV3+50)*10,0)/10,5,1) END AS GRADE_DEVIATION3, ");
            stb.append("     T4.AVG9, ");
            stb.append("     T4.STDDEV9, ");
            stb.append("     CASE WHEN 0 < T4.STDDEV9 THEN DECIMAL(ROUND((10*(RECEPT.TOTAL4-T4.AVG9)/T4.STDDEV9+50)*10,0)/10,5,1) END AS GRADE_DEVIATION9, ");
            stb.append("     T5.AVGZ, ");
            stb.append("     T5.STDDEVZ, ");
            stb.append("     CASE WHEN 0 < T5.STDDEVZ THEN DECIMAL(ROUND((10*(CONFRPT.TOTAL_ALL-T5.AVGZ)/T5.STDDEVZ+50)*10,0)/10,5,1) END AS GRADE_DEVIATIONZ ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_RECEPT_DAT RECEPT ");
            stb.append(" INNER JOIN  ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV AND BASE.EXAMNO = RECEPT.EXAMNO AND BASE.TESTDIV = RECEPT.TESTDIV    ");
            stb.append(" LEFT JOIN  ENTEXAM_SCORE_DAT SCORE1 ON SCORE1.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR AND SCORE1.APPLICANTDIV = RECEPT.APPLICANTDIV AND SCORE1.TESTDIV = RECEPT.TESTDIV AND SCORE1.EXAM_TYPE = RECEPT.EXAM_TYPE AND SCORE1.RECEPTNO = RECEPT.RECEPTNO AND SCORE1.TESTSUBCLASSCD = '" + subclassList.get(0) + "' ");
            stb.append(" LEFT JOIN  ENTEXAM_SCORE_DAT SCORE2 ON SCORE2.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR AND SCORE2.APPLICANTDIV = RECEPT.APPLICANTDIV AND SCORE2.TESTDIV = RECEPT.TESTDIV AND SCORE2.EXAM_TYPE = RECEPT.EXAM_TYPE AND SCORE2.RECEPTNO = RECEPT.RECEPTNO AND SCORE2.TESTSUBCLASSCD = '" + subclassList.get(1) + "' ");
            stb.append(" LEFT JOIN  ENTEXAM_SCORE_DAT SCORE3 ON SCORE3.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR AND SCORE3.APPLICANTDIV = RECEPT.APPLICANTDIV AND SCORE3.TESTDIV = RECEPT.TESTDIV AND SCORE3.EXAM_TYPE = RECEPT.EXAM_TYPE AND SCORE3.RECEPTNO = RECEPT.RECEPTNO AND SCORE3.TESTSUBCLASSCD = '" + subclassList.get(2) + "' ");
            stb.append(" LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONFRPT ON CONFRPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND CONFRPT.APPLICANTDIV = BASE.APPLICANTDIV AND CONFRPT.EXAMNO = BASE.EXAMNO ");
            stb.append(" LEFT JOIN AVG_STDDEV1 T1 ON T1.TESTDIV1 = BASE.TESTDIV1 ");
            stb.append(" LEFT JOIN AVG_STDDEV2 T2 ON T2.TESTDIV1 = BASE.TESTDIV1 ");
            stb.append(" LEFT JOIN AVG_STDDEV3 T3 ON T3.TESTDIV1 = BASE.TESTDIV1 ");
            stb.append(" LEFT JOIN AVG_STDDEV9 T4 ON T4.TESTDIV1 = BASE.TESTDIV1 ");
            stb.append(" LEFT JOIN AVG_STDDEVZ T5 ON T5.TESTDIV1 = BASE.TESTDIV1 ");
            stb.append(" WHERE ");
            stb.append("     RECEPT.ENTEXAMYEAR = '" + _param._examYear + "' AND ");
            stb.append("     RECEPT.APPLICANTDIV = '" + _param._applicantDiv + "' AND ");
            stb.append("     RECEPT.TESTDIV = '02' AND "); //筆記試験固定
            stb.append("     RECEPT.EXAM_TYPE = '1' AND ");
            stb.append("     BASE.TESTDIV1 IN " + SQLUtils.whereIn(true, _param._calkCd));
            stb.append(" ORDER BY ");
            stb.append("     RECEPT.RECEPTNO ");
            //メイン表　試験、類別毎
            stb.append(" )  SELECT ");
            stb.append("     '" + subclassList.get(0) + "' AS SUBCLASSCD, ");
            stb.append("     TESTDIV1, ");
            stb.append("     COUNT(SCORE1) AS CNT, ");
            stb.append("     MAX(SCORE1) AS MAX, ");
            stb.append("     MIN(SCORE1) AS MIN, ");
            stb.append("     DECIMAL(ROUND(AVG(FLOAT(SCORE1))*10,0)/10,5,1) AS AVG, ");
            stb.append("     SUM(SCORE1) AS TOTAL, ");
            stb.append("     DECIMAL(ROUND(STDDEV(FLOAT(SCORE1))*10,0)/10,5,1) AS STDDEV, ");
            stb.append("     MAX(GRADE_DEVIATION1) AS GDEV_MAX, ");
            stb.append("     MIN(GRADE_DEVIATION1) AS GDEV_MIN, ");
            stb.append("     DECIMAL(ROUND(AVG(FLOAT(GRADE_DEVIATION1))*10,0)/10,5,1) AS GDEV_AVG     ");
            stb.append(" FROM ");
            stb.append("     SCOREBASE ");
            stb.append(" GROUP BY ");
            stb.append("     TESTDIV1 ");
            stb.append(" UNION ALL   ");
            stb.append(" SELECT ");
            stb.append("     '" + subclassList.get(1) + "' AS SUBCLASSCD, ");
            stb.append("     TESTDIV1, ");
            stb.append("     COUNT(SCORE2) AS CNT, ");
            stb.append("     MAX(SCORE2) AS MAX, ");
            stb.append("     MIN(SCORE2) AS MIN, ");
            stb.append("     DECIMAL(ROUND(AVG(FLOAT(SCORE2))*10,0)/10,5,1) AS AVG, ");
            stb.append("     SUM(SCORE2) AS TOTAL, ");
            stb.append("     DECIMAL(ROUND(STDDEV(FLOAT(SCORE2))*10,0)/10,5,1) AS STDDEV, ");
            stb.append("     MAX(GRADE_DEVIATION2) AS GDEV_MAX, ");
            stb.append("     MIN(GRADE_DEVIATION2) AS GDEV_MIN, ");
            stb.append("     DECIMAL(ROUND(AVG(FLOAT(GRADE_DEVIATION2))*10,0)/10,5,1) AS GDEV_AVG ");
            stb.append(" FROM ");
            stb.append("     SCOREBASE ");
            stb.append(" GROUP BY ");
            stb.append("     TESTDIV1 ");
            stb.append(" UNION ALL   ");
            stb.append(" SELECT ");
            stb.append("     '" + subclassList.get(2) + "' AS SUBCLASSCD, ");
            stb.append("     TESTDIV1, ");
            stb.append("     COUNT(SCORE3) AS CNT, ");
            stb.append("     MAX(SCORE3) AS MAX, ");
            stb.append("     MIN(SCORE3) AS MIN, ");
            stb.append("     DECIMAL(ROUND(AVG(FLOAT(SCORE3))*10,0)/10,5,1) AS AVG, ");
            stb.append("     SUM(SCORE3) AS TOTAL, ");
            stb.append("     DECIMAL(ROUND(STDDEV(FLOAT(SCORE3))*10,0)/10,5,1) AS STDDEV, ");
            stb.append("     MAX(GRADE_DEVIATION3) AS GDEV_MAX, ");
            stb.append("     MIN(GRADE_DEVIATION3) AS GDEV_MIN, ");
            stb.append("     DECIMAL(ROUND(AVG(FLOAT(GRADE_DEVIATION3))*10,0)/10,5,1) AS GDEV_AVG     ");
            stb.append(" FROM ");
            stb.append("     SCOREBASE ");
            stb.append(" GROUP BY ");
            stb.append("     TESTDIV1 ");
            stb.append(" UNION ALL   ");
            stb.append(" SELECT ");
            stb.append("     '99' AS SUBCLASSCD, ");
            stb.append("     TESTDIV1, ");
            stb.append("     COUNT(SCORE9) AS CNT, ");
            stb.append("     MAX(SCORE9) AS MAX, ");
            stb.append("     MIN(SCORE9) AS MIN, ");
            stb.append("     DECIMAL(ROUND(AVG(FLOAT(SCORE9))*10,0)/10,5,1) AS AVG, ");
            stb.append("     SUM(SCORE9) AS TOTAL, ");
            stb.append("     DECIMAL(ROUND(STDDEV(FLOAT(SCORE9))*10,0)/10,5,1) AS STDDEV, ");
            stb.append("     MAX(GRADE_DEVIATION9) AS GDEV_MAX, ");
            stb.append("     MIN(GRADE_DEVIATION9) AS GDEV_MIN, ");
            stb.append("     DECIMAL(ROUND(AVG(FLOAT(GRADE_DEVIATION9))*10,0)/10,5,1) AS GDEV_AVG ");
            stb.append(" FROM ");
            stb.append("     SCOREBASE ");
            stb.append(" GROUP BY ");
            stb.append("     TESTDIV1 ");
            stb.append(" UNION ALL   ");
            stb.append(" SELECT ");
            stb.append("     'NAISHIN' AS SUBCLASSCD, ");
            stb.append("     TESTDIV1, ");
            stb.append("     COUNT(NAISHIN) AS CNT, ");
            stb.append("     MAX(NAISHIN) AS MAX, ");
            stb.append("     MIN(NAISHIN) AS MIN, ");
            stb.append("     DECIMAL(ROUND(AVG(FLOAT(NAISHIN))*10,0)/10,5,1) AS AVG, ");
            stb.append("     SUM(NAISHIN) AS TOTAL, ");
            stb.append("     DECIMAL(ROUND(STDDEV(FLOAT(NAISHIN))*10,0)/10,5,1) AS STDDEV, ");
            stb.append("     MAX(GRADE_DEVIATIONZ) AS GDEV_MAX, ");
            stb.append("     MIN(GRADE_DEVIATIONZ) AS GDEV_MIN, ");
            stb.append("     DECIMAL(ROUND(AVG(FLOAT(GRADE_DEVIATIONZ))*10,0)/10,5,1) AS GDEV_AVG ");
            stb.append(" FROM ");
            stb.append("     SCOREBASE ");
            stb.append(" GROUP BY ");
            stb.append("     TESTDIV1 ");
            stb.append(" UNION ALL   ");
            stb.append(" SELECT ");
            stb.append("     '" + subclassList.get(0) + "' AS SUBCLASSCD, ");
            stb.append("     'ZZ' AS TESTDIV1, ");
            stb.append("     COUNT(SCORE1) AS CNT, ");
            stb.append("     MAX(SCORE1) AS MAX, ");
            stb.append("     MIN(SCORE1) AS MIN, ");
            stb.append("     DECIMAL(ROUND(AVG(FLOAT(SCORE1))*10,0)/10,5,1) AS AVG, ");
            stb.append("     SUM(SCORE1) AS TOTAL, ");
            stb.append("     DECIMAL(ROUND(STDDEV(FLOAT(SCORE1))*10,0)/10,5,1) AS STDDEV, ");
            stb.append("     MAX(GRADE_DEVIATION1) AS GDEV_MAX, ");
            stb.append("     MIN(GRADE_DEVIATION1) AS GDEV_MIN, ");
            stb.append("     DECIMAL(ROUND(AVG(FLOAT(GRADE_DEVIATION1))*10,0)/10,5,1) AS GDEV_AVG ");
            stb.append(" FROM ");
            stb.append("     SCOREBASE ");
            stb.append(" UNION ALL   ");
            stb.append(" SELECT ");
            stb.append("     '" + subclassList.get(1) + "' AS SUBCLASSCD, ");
            stb.append("     'ZZ' AS TESTDIV1, ");
            stb.append("     COUNT(SCORE2) AS CNT, ");
            stb.append("     MAX(SCORE2) AS MAX, ");
            stb.append("     MIN(SCORE2) AS MIN, ");
            stb.append("     DECIMAL(ROUND(AVG(FLOAT(SCORE2))*10,0)/10,5,1) AS AVG, ");
            stb.append("     SUM(SCORE2) AS TOTAL, ");
            stb.append("     DECIMAL(ROUND(STDDEV(FLOAT(SCORE2))*10,0)/10,5,1) AS STDDEV, ");
            stb.append("     MAX(GRADE_DEVIATION2) AS GDEV_MAX, ");
            stb.append("     MIN(GRADE_DEVIATION2) AS GDEV_MIN, ");
            stb.append("     DECIMAL(ROUND(AVG(FLOAT(GRADE_DEVIATION2))*10,0)/10,5,1) AS GDEV_AVG ");
            stb.append(" FROM ");
            stb.append("     SCOREBASE ");
            stb.append(" UNION ALL   ");
            stb.append(" SELECT ");
            stb.append("     '" + subclassList.get(2) + "' AS SUBCLASSCD, ");
            stb.append("     'ZZ' AS TESTDIV1, ");
            stb.append("     COUNT(SCORE3) AS CNT, ");
            stb.append("     MAX(SCORE3) AS MAX, ");
            stb.append("     MIN(SCORE3) AS MIN, ");
            stb.append("     DECIMAL(ROUND(AVG(FLOAT(SCORE3))*10,0)/10,5,1) AS AVG, ");
            stb.append("     SUM(SCORE3) AS TOTAL, ");
            stb.append("     DECIMAL(ROUND(STDDEV(FLOAT(SCORE3))*10,0)/10,5,1) AS STDDEV, ");
            stb.append("     MAX(GRADE_DEVIATION3) AS GDEV_MAX, ");
            stb.append("     MIN(GRADE_DEVIATION3) AS GDEV_MIN, ");
            stb.append("     DECIMAL(ROUND(AVG(FLOAT(GRADE_DEVIATION3))*10,0)/10,5,1) AS GDEV_AVG ");
            stb.append(" FROM ");
            stb.append("     SCOREBASE ");
            stb.append(" UNION ALL   ");
            stb.append(" SELECT ");
            stb.append("     '99' AS SUBCLASSCD, ");
            stb.append("     'ZZ' AS TESTDIV1, ");
            stb.append("     COUNT(SCORE9) AS CNT, ");
            stb.append("     MAX(SCORE9) AS MAX, ");
            stb.append("     MIN(SCORE9) AS MIN, ");
            stb.append("     DECIMAL(ROUND(AVG(FLOAT(SCORE9))*10,0)/10,5,1) AS AVG, ");
            stb.append("     SUM(SCORE9) AS TOTAL, ");
            stb.append("     DECIMAL(ROUND(STDDEV(FLOAT(SCORE9))*10,0)/10,5,1) AS STDDEV, ");
            stb.append("     MAX(GRADE_DEVIATION9) AS GDEV_MAX, ");
            stb.append("     MIN(GRADE_DEVIATION9) AS GDEV_MIN, ");
            stb.append("     DECIMAL(ROUND(AVG(FLOAT(GRADE_DEVIATION9))*10,0)/10,5,1) AS GDEV_AVG ");
            stb.append(" FROM ");
            stb.append("     SCOREBASE ");
            stb.append(" UNION ALL   ");
            stb.append(" SELECT ");
            stb.append("     'NAISHIN' AS SUBCLASSCD, ");
            stb.append("     'ZZ' AS TESTDIV1, ");
            stb.append("     COUNT(NAISHIN) AS CNT, ");
            stb.append("     MAX(NAISHIN) AS MAX, ");
            stb.append("     MIN(NAISHIN) AS MIN, ");
            stb.append("     DECIMAL(ROUND(AVG(FLOAT(NAISHIN))*10,0)/10,5,1) AS AVG, ");
            stb.append("     SUM(NAISHIN) AS TOTAL, ");
            stb.append("     DECIMAL(ROUND(STDDEV(FLOAT(NAISHIN))*10,0)/10,5,1) AS STDDEV, ");
            stb.append("     MAX(GRADE_DEVIATIONZ) AS GDEV_MAX, ");
            stb.append("     MIN(GRADE_DEVIATIONZ) AS GDEV_MIN, ");
            stb.append("     DECIMAL(ROUND(AVG(FLOAT(GRADE_DEVIATIONZ))*10,0)/10,5,1) AS GDEV_AVG ");
            stb.append(" FROM ");
            stb.append("     SCOREBASE ");
            stb.append(" ORDER BY ");
            stb.append("     SUBCLASSCD,TESTDIV1 ");

            log.debug(" syukei sql =" + stb.toString());

            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                final String subclasscd = rs.getString("SUBCLASSCD");
                final String testdiv1 = rs.getString("TESTDIV1");
                final String cnt = rs.getString("CNT");
                final String max = rs.getString("MAX");
                final String min = rs.getString("MIN");
                final String avg = rs.getString("AVG");
                final String total = rs.getString("TOTAL");
                final String stddev = rs.getString("STDDEV");
                final String gdev_Max = rs.getString("GDEV_MAX");
                final String gdev_Min = rs.getString("GDEV_MIN");
                final String gdev_Avg = rs.getString("GDEV_AVG");

                final String key = subclasscd + testdiv1;
                if(!retMap.containsKey(key)) {
                    final Print1 print1 = new Print1(subclasscd, testdiv1, cnt, max, min, avg, total, stddev, gdev_Max, gdev_Min, gdev_Avg);
                    retMap.put(key, print1);
                }
            }
        } catch (final SQLException e) {
            log.error("志願者の基本情報取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        return retMap;
    }

    // 受験成績度数分布表 成績取得SQL
    private Map getPrint2Map(final DB2UDB db2, final boolean flg) throws SQLException {
        Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCOREBASE AS ( ");
            stb.append(" SELECT ");
            stb.append("   SCORE.ENTEXAMYEAR, ");
            stb.append("   SCORE.APPLICANTDIV, ");
            stb.append("   SCORE.TESTDIV, ");
            stb.append("   SCORE.EXAM_TYPE, ");
            stb.append("   SCORE.RECEPTNO, ");
            stb.append("   SCORE.TESTSUBCLASSCD, ");
            stb.append("   SCORE.ATTEND_FLG, ");
            stb.append("   SCORE.SCORE, ");
            stb.append("   BASE.TESTDIV1, ");
            stb.append("   SUB.TESTSUBClASS_NAME ");
            stb.append(" FROM ");
            stb.append("   ENTEXAM_SCORE_DAT SCORE ");
            stb.append(" INNER JOIN ");
            stb.append("   ENTEXAM_RECEPT_DAT RECEPT ON RECEPT.ENTEXAMYEAR = SCORE.ENTEXAMYEAR AND RECEPT.APPLICANTDIV = SCORE.APPLICANTDIV AND RECEPT.TESTDIV = SCORE.TESTDIV AND RECEPT.EXAM_TYPE = SCORE.EXAM_TYPE AND RECEPT.RECEPTNO = SCORE.RECEPTNO ");
            stb.append(" INNER JOIN ");
            stb.append("   ENTEXAM_APPLICANTBASE_DAT BASE ON RECEPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND RECEPT.APPLICANTDIV = BASE.APPLICANTDIV AND RECEPT.TESTDIV = BASE.TESTDIV AND RECEPT.EXAMNO = BASE.EXAMNO ");
            stb.append(" LEFT JOIN ");
            stb.append("   ENTEXAM_TESTSUBCLASSCD_DAT SUB ON SUB.ENTEXAMYEAR = SCORE.ENTEXAMYEAR AND SUB.APPLICANTDIV = SCORE.APPLICANTDIV AND SUB.TESTDIV = SCORE.TESTDIV AND SUB.EXAM_TYPE = SCORE.EXAM_TYPE AND SUB.TESTSUBCLASSCD = SCORE.TESTSUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("   SCORE.ENTEXAMYEAR = '" + _param._examYear + "' AND ");
            stb.append("   SCORE.APPLICANTDIV = '" + _param._applicantDiv + "' AND ");
            stb.append("   SCORE.EXAM_TYPE = '1' AND ");
            stb.append("   SCORE.SCORE IS NOT NULL AND ");
            stb.append("   SCORE.TESTDIV = '02' AND ");
            stb.append("   BASE.TESTDIV1 IN " + SQLUtils.whereIn(true, _param._calkCd )); //集計ONの類別コード
            stb.append(" ORDER BY SCORE.TESTSUBCLASSCD,BASE.TESTDIV1,SCORE.SCORE DESC,SCORE.RECEPTNO ");
            if(flg) {
                //科目合算
                stb.append(" ), SCOREALL AS ( ");
                stb.append(" SELECT ");
                stb.append("   SUM(SCORE) AS SCORE, ");
                stb.append("   TESTDIV1, ");
                stb.append("   RECEPTNO ");
                stb.append(" FROM ");
                stb.append("   SCOREBASE ");
                stb.append(" GROUP BY ");
                stb.append("   TESTDIV1,RECEPTNO ");
                stb.append(" ) ");
                stb.append(" SELECT ");
                stb.append("   SCORE, ");
                stb.append("   TESTDIV1, ");
                stb.append("   COUNT(RECEPTNO) AS COUNT ");
                stb.append(" FROM ");
                stb.append("   SCOREALL ");
                stb.append(" GROUP BY SCORE,TESTDIV1 ");
                stb.append(" UNION ALL ");
                stb.append(" SELECT ");
                stb.append("   SCORE, ");
                stb.append("   '99' AS TESTDIV1, ");
                stb.append("   COUNT(RECEPTNO) AS COUNT ");
                stb.append(" FROM ");
                stb.append("     SCOREALL ");
                stb.append(" GROUP BY ");
                stb.append("     SCORE ");
                stb.append(" ORDER BY ");
                stb.append("     SCORE DESC, ");
                stb.append("     TESTDIV1 ");
            } else {
                //科目毎
                stb.append(" ) ");
                stb.append(" SELECT ");
                stb.append("   TESTSUBCLASSCD, ");
                stb.append("   SCORE, ");
                stb.append("   TESTDIV1, ");
                stb.append("   COUNT(RECEPTNO) AS COUNT ");
                stb.append(" FROM SCOREBASE ");
                stb.append(" GROUP BY TESTSUBCLASSCD,SCORE,TESTDIV1 ");
                stb.append(" UNION ALL ");
                stb.append(" SELECT ");
                stb.append("   TESTSUBCLASSCD, ");
                stb.append("   SCORE, ");
                stb.append("   '99' AS TESTDIV1, ");
                stb.append("   COUNT(RECEPTNO) AS COUNT ");
                stb.append(" FROM ");
                stb.append("   SCOREBASE ");
                stb.append(" GROUP BY ");
                stb.append("   TESTSUBCLASSCD, ");
                stb.append("   SCORE ");
                stb.append(" ORDER BY ");
                stb.append("   TESTSUBCLASSCD, ");
                stb.append("   SCORE DESC, ");
                stb.append("   TESTDIV1 ");
            }

            log.debug(" scoreCnt sql =" + stb.toString());

            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            Map scoreMap = null;
            while (rs.next()) {
                final String score = rs.getString("SCORE");
                final String testdiv1 = rs.getString("TESTDIV1");
                final String count = rs.getString("COUNT");
                final String key;

                if (flg) {
                    key = score;
                } else {
                    final String testsubclasscd = rs.getString("TESTSUBCLASSCD");
                    key = testsubclasscd + score;
                }
                if (!retMap.containsKey(key)) {
                    scoreMap = new LinkedMap();
                    retMap.put(key, scoreMap);
                } else {
                    scoreMap = (Map) retMap.get(key);
                }
                if (!scoreMap.containsKey(testdiv1)) {
                    scoreMap.put(testdiv1, count);
                }
            }
        } catch (final SQLException e) {
            log.error("志願者の基本情報取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        return retMap;
    }

    private Map getPrint3Map(final DB2UDB db2, final boolean flg) throws SQLException {
        Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;

        int cnt = 1;
        List<String> cdList = new ArrayList<String>();
        for(Iterator ite = _param._testSubclassMap.keySet().iterator();ite.hasNext();) {
            cdList.add((String)ite.next());
            cnt++;
            if(cnt > 3) break;
        }

        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   RECEPT.ENTEXAMYEAR, ");
            stb.append("   RECEPT.APPLICANTDIV, ");
            stb.append("   RECEPT.TESTDIV, ");
            stb.append("   RECEPT.EXAM_TYPE, ");
            stb.append("   RECEPT.RECEPTNO, ");
            stb.append("   SCORE1.SCORE AS SCORE1, ");
            stb.append("   SCORE2.SCORE AS SCORE2, ");
            stb.append("   SCORE3.SCORE AS SCORE3, ");
            stb.append("   SET027.NAME1, ");
            stb.append("   RECEPT.TOTAL4, ");
            stb.append("   RECEPT.TOTAL_RANK4, ");
            stb.append("   BASE.EXAMNO, ");
            stb.append("   BASE.NAME, ");
            stb.append("   BASE.NAME_KANA, ");
            stb.append("   BASE.SEX, ");
            stb.append("   BASE.BIRTHDAY, ");
            stb.append("   BASE.FS_CD, ");
            stb.append("   BASE.ENTDIV, ");
            stb.append("   BASE.PROCEDUREDIV, ");
            stb.append("   BASE.JUDGEMENT, ");
            stb.append("   CLASSIFY.MARK, ");
            stb.append("   SCHOOL.FINSCHOOL_DIV, ");
            stb.append("   SCHOOL.FINSCHOOL_NAME, ");
            stb.append("   SET015.ABBV1, ");
            stb.append("   L001.NAME1 AS DISTRICT, ");
            stb.append("   VALUE(DETAIL.REMARK8 || '　','') AS REMARK8, ");
            stb.append("   VALUE(DETAIL.REMARK9 || '　','') AS REMARK9, ");
            stb.append("   DETAIL.REMARK10, ");
            stb.append("   CONFRPT.CONFIDENTIAL_RPT01, ");
            stb.append("   CONFRPT.CONFIDENTIAL_RPT02, ");
            stb.append("   CONFRPT.CONFIDENTIAL_RPT03, ");
            stb.append("   CONFRPT.CONFIDENTIAL_RPT04, ");
            stb.append("   CONFRPT.CONFIDENTIAL_RPT05, ");
            stb.append("   CONFRPT.TOTAL3, ");
            stb.append("   CONFRPT.TOTAL5, ");
            stb.append("   CONFRPT.TOTAL_ALL ");
            stb.append(" FROM ");
            stb.append("   ENTEXAM_RECEPT_DAT RECEPT ");
            stb.append(" INNER JOIN ");
            stb.append("   ENTEXAM_SCORE_DAT SCORE1 ON SCORE1.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR AND SCORE1.APPLICANTDIV = RECEPT.APPLICANTDIV AND SCORE1.TESTDIV = RECEPT.TESTDIV AND SCORE1.EXAM_TYPE = RECEPT.EXAM_TYPE AND SCORE1.RECEPTNO = RECEPT.RECEPTNO AND SCORE1.TESTSUBCLASSCD = '" + cdList.get(0) + "' ");
            stb.append(" INNER JOIN ");
            stb.append("   ENTEXAM_SCORE_DAT SCORE2 ON SCORE2.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR AND SCORE2.APPLICANTDIV = RECEPT.APPLICANTDIV AND SCORE2.TESTDIV = RECEPT.TESTDIV AND SCORE2.EXAM_TYPE = RECEPT.EXAM_TYPE AND SCORE2.RECEPTNO = RECEPT.RECEPTNO AND SCORE2.TESTSUBCLASSCD = '" + cdList.get(1) + "' ");
            stb.append(" INNER JOIN ");
            stb.append("   ENTEXAM_SCORE_DAT SCORE3 ON SCORE3.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR AND SCORE3.APPLICANTDIV = RECEPT.APPLICANTDIV AND SCORE3.TESTDIV = RECEPT.TESTDIV AND SCORE3.EXAM_TYPE = RECEPT.EXAM_TYPE AND SCORE3.RECEPTNO = RECEPT.RECEPTNO AND SCORE3.TESTSUBCLASSCD = '" + cdList.get(2) + "' ");
            stb.append(" INNER JOIN ");
            stb.append("   ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV AND BASE.EXAMNO = RECEPT.EXAMNO AND BASE.TESTDIV = RECEPT.TESTDIV ");
            stb.append(" LEFT JOIN ");
            stb.append("   ENTEXAM_INTERVIEW_DAT INTERVIEW ON INTERVIEW.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND INTERVIEW.APPLICANTDIV = BASE.APPLICANTDIV AND INTERVIEW.TESTDIV = BASE.TESTDIV AND INTERVIEW.EXAMNO = BASE.EXAMNO ");
            stb.append(" LEFT JOIN ");
            stb.append("   FINSCHOOL_MST SCHOOL ON SCHOOL.FINSCHOOLCD = BASE.FS_CD ");
            stb.append(" LEFT JOIN ");
            stb.append("   ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL ON DETAIL.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND DETAIL.APPLICANTDIV = BASE.APPLICANTDIV AND DETAIL.EXAMNO = BASE.EXAMNO AND DETAIL.SEQ = '031' ");
            stb.append(" LEFT JOIN ");
            stb.append("   ENTEXAM_SETTING_MST SET015 ON SET015.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR AND SET015.APPLICANTDIV = RECEPT.APPLICANTDIV AND SET015.SETTING_CD = 'L015' AND SET015.SEQ = SCHOOL.FINSCHOOL_DIV ");
            stb.append(" LEFT JOIN ");
            stb.append("   V_NAME_MST L001 ON L001.YEAR = RECEPT.ENTEXAMYEAR AND L001.NAMECD1 = 'L001' AND L001.NAMECD2 = SCHOOL.FINSCHOOL_DISTCD ");
            stb.append(" LEFT JOIN ");
            stb.append("   ENTEXAM_SETTING_MST SET027 ON SET027.ENTEXAMYEAR = INTERVIEW.ENTEXAMYEAR AND SET027.APPLICANTDIV = INTERVIEW.APPLICANTDIV AND SET027.SETTING_CD = 'L027' AND SET027.SEQ = INTERVIEW.INTERVIEW_A ");
            stb.append(" LEFT JOIN ");
            stb.append("   ENTEXAM_APPLICANTCONFRPT_DAT CONFRPT ON CONFRPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND CONFRPT.APPLICANTDIV = BASE.APPLICANTDIV AND CONFRPT.EXAMNO = BASE.EXAMNO ");
            stb.append(" LEFT JOIN ");
            stb.append("   ENTEXAM_CLASSIFY_MST CLASSIFY ON CLASSIFY.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND CLASSIFY.APPLICANTDIV = BASE.APPLICANTDIV AND CLASSIFY.CLASSIFY_CD = BASE.TESTDIV1 ");
            stb.append(" WHERE ");
            stb.append("   RECEPT.ENTEXAMYEAR = '" + _param._examYear + "' AND ");
            stb.append("   RECEPT.APPLICANTDIV = '" + _param._applicantDiv + "' AND ");
            stb.append("   RECEPT.TESTDIV = '02' AND ");
            stb.append("   RECEPT.EXAM_TYPE = '1' AND ");
            if(flg) {
                stb.append("   SCORE1.SCORE >= 70 AND ");
                stb.append("   SCORE2.SCORE >= 70 AND ");
                stb.append("   SCORE3.SCORE >= 70 AND ");
                stb.append("   RECEPT.TOTAL_RANK4 <= 10 AND ");
                stb.append("   CONFRPT.TOTAL_ALL  >= 36 ");
            } else {
                stb.append("   RECEPT.TOTAL_RANK4 <= 20 ");
            }
            stb.append(" ORDER BY RECEPT.TOTAL_RANK4,RECEPT.RECEPTNO ");

            log.debug("sql = " + stb.toString());

            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                final String entexamyear = rs.getString("ENTEXAMYEAR");
                final String applicantdiv = rs.getString("APPLICANTDIV");
                final String testdiv = rs.getString("TESTDIV");
                final String exam_Type = rs.getString("EXAM_TYPE");
                final String receptno = rs.getString("RECEPTNO");
                final String score1 = rs.getString("SCORE1");
                final String score2 = rs.getString("SCORE2");
                final String score3 = rs.getString("SCORE3");
                final String name1 = rs.getString("NAME1");
                final String total4 = rs.getString("TOTAL4");
                final String total_Rank4 = rs.getString("TOTAL_RANK4");
                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String name_Kana = rs.getString("NAME_KANA");
                final String sex = rs.getString("SEX");
                final String birthday = rs.getString("BIRTHDAY");
                final String fs_Cd = rs.getString("FS_CD");
                final String entdiv = rs.getString("ENTDIV");
                final String procedurediv = rs.getString("PROCEDUREDIV");
                final String judgement = rs.getString("JUDGEMENT");
                final String mark = rs.getString("MARK");
                final String finschool_Div = rs.getString("FINSCHOOL_DIV");
                final String finschool_Name = rs.getString("FINSCHOOL_NAME");
                final String district = rs.getString("DISTRICT");
                final String remark8 = rs.getString("REMARK8");
                final String remark9 = rs.getString("REMARK9");
                final String remark10 = rs.getString("REMARK10");
                final String confidential_Rpt01 = rs.getString("CONFIDENTIAL_RPT01");
                final String confidential_Rpt02 = rs.getString("CONFIDENTIAL_RPT02");
                final String confidential_Rpt03 = rs.getString("CONFIDENTIAL_RPT03");
                final String confidential_Rpt04 = rs.getString("CONFIDENTIAL_RPT04");
                final String confidential_Rpt05 = rs.getString("CONFIDENTIAL_RPT05");
                final String total3 = rs.getString("TOTAL3");
                final String total5 = rs.getString("TOTAL5");
                final String total_All = rs.getString("TOTAL_ALL");

                if(!retMap.containsKey(receptno)) {
                    final Print3 print3 = new Print3(entexamyear, applicantdiv, testdiv, exam_Type, receptno, score1,
                            score2, score3, name1, total4, total_Rank4, name, name_Kana, sex, birthday, fs_Cd, entdiv,
                            procedurediv, judgement, mark, finschool_Div, finschool_Name, district, remark8, remark9, remark10,
                            confidential_Rpt01, confidential_Rpt02, confidential_Rpt03, confidential_Rpt04, confidential_Rpt05,
                            total3, total5, total_All);
                    retMap.put(receptno, print3);
                }

            }
        } catch (final SQLException e) {
            log.error("志願者の基本情報取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retMap;
    }

    private class SubclassMst {
        final String _subclassCd;
        final String _subclassName;
        final String _remark2;

        public SubclassMst(final String subclassCd, final String subclassName, final String remark2) {
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _remark2 = remark2;
        }
    }

    //入試成績集計表
    private class Print1 {
        final String _subclasscd;
        final String _testdiv1;
        final String _cnt;
        final String _max;
        final String _min;
        final String _avg;
        final String _total;
        final String _stddev;
        final String _gdev_Max;
        final String _gdev_Min;
        final String _gdev_Avg;

        public Print1(final String subclasscd, final String testdiv1, final String cnt, final String max,
                final String min, final String avg, final String total, final String stddev, final String gdev_Max,
                final String gdev_Min, final String gdev_Avg) {
            _subclasscd = subclasscd;
            _testdiv1 = testdiv1;
            _cnt = cnt;
            _max = max;
            _min = min;
            _avg = avg;
            _total = total;
            _stddev = stddev;
            _gdev_Max = gdev_Max;
            _gdev_Min = gdev_Min;
            _gdev_Avg = gdev_Avg;
        }
    }

    //奨学生・奨励賞一覧
    private class Print3 {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _exam_Type;
        final String _receptno;
        final String _score1;
        final String _score2;
        final String _score3;
        final String _name1;
        final String _total4;
        final String _total_Rank4;
        final String _name;
        final String _name_Kana;
        final String _sex;
        final String _birthday;
        final String _fs_Cd;
        final String _entdiv;
        final String _procedurediv;
        final String _judgement;
        final String _mark;
        final String _finschool_Div;
        final String _finschool_Name;
        final String _district;
        final String _remark8;
        final String _remark9;
        final String _remark10;
        final String _confidential_Rpt01;
        final String _confidential_Rpt02;
        final String _confidential_Rpt03;
        final String _confidential_Rpt04;
        final String _confidential_Rpt05;
        final String _total3;
        final String _total5;
        final String _total_All;

        public Print3(final String entexamyear, final String applicantdiv, final String testdiv, final String exam_Type,
                final String receptno, final String score1, final String score2, final String score3,
                final String name1, final String total4, final String total_Rank4, final String name,
                final String name_Kana, final String sex, final String birthday, final String fs_Cd,
                final String entdiv, final String procedurediv, final String judgement, final String mark,
                final String finschool_Div, final String finschool_Name, final String district, final String remark8, final String remark9,
                final String remark10,final String confidential_Rpt01, final String confidential_Rpt02, final String confidential_Rpt03,
                final String confidential_Rpt04, final String confidential_Rpt05, final String total3, final String total5, final String total_All) {
            _entexamyear = entexamyear;
            _applicantdiv = applicantdiv;
            _testdiv = testdiv;
            _exam_Type = exam_Type;
            _receptno = receptno;
            _score1 = score1;
            _score2 = score2;
            _score3 = score3;
            _name1 = name1;
            _total4 = total4;
            _total_Rank4 = total_Rank4;
            _name = name;
            _name_Kana = name_Kana;
            _sex = sex;
            _birthday = birthday;
            _fs_Cd = fs_Cd;
            _entdiv = entdiv;
            _procedurediv = procedurediv;
            _judgement = judgement;
            _mark = mark;
            _finschool_Div = finschool_Div;
            _finschool_Name = finschool_Name;
            _district = district;
            _remark8 = remark8;
            _remark9 = remark9;
            _remark10 = remark10;
            _confidential_Rpt01 = confidential_Rpt01;
            _confidential_Rpt02 = confidential_Rpt02;
            _confidential_Rpt03 = confidential_Rpt03;
            _confidential_Rpt04 = confidential_Rpt04;
            _confidential_Rpt05 = confidential_Rpt05;
            _total3 = total3;
            _total5 = total5;
            _total_All = total_All;
        }
    }

    private class ClassifyMst {
        final String _classifyCd;
        final String _classifyName;
        final String _mark;
        final String _calcFlg;

        public ClassifyMst(final String cd, final String name, final String mark, final String flg) {
            _classifyCd = cd;
            _classifyName = name;
            _mark = mark;
            _calcFlg = flg;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Id: 053f44af8006793ec6e0b550110aeedd66629d08 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _examYear;
        final String _applicantDiv; //入試制度
        final String _outputDiv;  //帳票区分　1:入試成績集計表 2:受験成績度数分布表 3:奨学生・奨励賞一覧
        final String _cmd;
        final String _date;
        final String _testName;
        final String _time;
        final boolean _csv;
        final Map _classifyMstMap; //類別マスタ
        final Map _testSubclassMap; //試験マスタ
        final Map _confidentialName; //内申教科名
        final String[] _calkCd; // 集計ON類別コード


        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _examYear = request.getParameter("ENTEXAMYEAR");
//            _applicantDiv = request.getParameter("APPLICANTDIV");
            _applicantDiv = "2"; //固定
            _outputDiv = request.getParameter("OUTPUT");
            _cmd = request.getParameter("cmd");
            _date = request.getParameter("LOGIN_DATE");
            _time = request.getParameter("TIME");
            _csv = "csv".equals(_cmd);
            _classifyMstMap = getClassifyMst(db2);
            _testSubclassMap = getSubclassName(db2);
            _confidentialName = getConfidentialName(db2);
            _testName = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT TESTDIV_NAME FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR = '" + _examYear + "' AND APPLICANTDIV = '2' AND TESTDIV = '02' "));
            List<String> list = new ArrayList();
            for(Iterator it = _classifyMstMap.keySet().iterator(); it.hasNext();) {
                final String key = (String)it.next();
                final ClassifyMst mst = (ClassifyMst)_classifyMstMap.get(key);
                if("1".equals(mst._calcFlg)) {
                    list.add(mst._classifyCd);
                }
            }
            _calkCd = list.toArray(new String[list.size()]);
        }

        private Map getConfidentialName(final DB2UDB db2) {
            final Map retMap = new LinkedMap();
            for (final Map<String, String> row : KnjDbUtils.query(db2, "SELECT SEQ, NAME1 FROM ENTEXAM_SETTING_MST WHERE SETTING_CD = 'L008' AND ENTEXAMYEAR = '" + _examYear + "' AND APPLICANTDIV = '2' ORDER BY SEQ LIMIT 5")) {
                if(!retMap.containsKey(KnjDbUtils.getString(row, "SEQ"))) {
                    retMap.put(KnjDbUtils.getString(row, "SEQ"), KnjDbUtils.getString(row, "NAME1"));
                }
            }
            return retMap;
        }
        private Map getClassifyMst(final DB2UDB db2) {
            final Map retMap = new LinkedMap();
            for (final Map<String, String> row : KnjDbUtils.query(db2, "SELECT CLASSIFY_CD,CLASSIFY_NAME,MARK,CALC_FLG FROM ENTEXAM_CLASSIFY_MST WHERE ENTEXAMYEAR = '" + _examYear + "' AND APPLICANTDIV = '" + _applicantDiv + "' ORDER BY CLASSIFY_CD")) {
                if(!retMap.containsKey(KnjDbUtils.getString(row, "CLASSIFY_CD"))) {
                    final ClassifyMst mst = new ClassifyMst(KnjDbUtils.getString(row, "CLASSIFY_CD"),KnjDbUtils.getString(row, "CLASSIFY_NAME"),KnjDbUtils.getString(row, "MARK"),KnjDbUtils.getString(row, "CALC_FLG"));
                    retMap.put(mst._classifyCd, mst);
                }
            }
            return retMap;
        }

        private Map getL008Name(final DB2UDB db2) {
            final Map retMap = new LinkedMap();
            for (final Map<String, String> row : KnjDbUtils.query(db2, "SELECT SEQ,NAME1 FROM ENTEXAM_SETTING_MST WHERE ENTEXAMYEAR = '" + _examYear + "' AND APPLICANTDIV = '" + _applicantDiv + "' AND SETTING_CD = 'L008' ORDER BY SEQ")) {
                if(!retMap.containsKey(KnjDbUtils.getString(row, "SEQ"))) {
                    retMap.put(KnjDbUtils.getString(row, "SEQ"), KnjDbUtils.getString(row, "NAME1"));
                }
            }
            return retMap;
        }

        private Map getSubclassName(final DB2UDB db2) {
            final Map retMap = new LinkedMap();
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.TESTSUBCLASSCD, ");
            stb.append("     S1.NAME1 AS TESTSUBCLASS_NAME, ");
            stb.append("     T1.REMARK2 ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_TESTSUBCLASSCD_DAT T1 ");
            stb.append(" LEFT JOIN ");
            stb.append("     ENTEXAM_SETTING_MST S1 ON S1.ENTEXAMYEAR = T1.ENTEXAMYEAR AND S1.APPLICANTDIV = T1.APPLICANTDIV AND S1.SETTING_CD = 'L009' AND S1.SEQ = T1.TESTSUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     T1.ENTEXAMYEAR = '" + _examYear + "' ");
            stb.append("     AND T1.APPLICANTDIV = '" + _applicantDiv + "' ");
            stb.append("     AND T1.TESTDIV = '02' ");
            stb.append("     AND T1.EXAM_TYPE= '1' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.TESTSUBCLASSCD ");

            for (final Map<String, String> row : KnjDbUtils.query(db2, stb.toString())) {
                final String cd = KnjDbUtils.getString(row, "TESTSUBCLASSCD");
                final String name = KnjDbUtils.getString(row, "TESTSUBCLASS_NAME");
                final String remark2 = KnjDbUtils.getString(row, "REMARK2");
                if(!retMap.containsKey(cd)) {
                    SubclassMst subclass = new SubclassMst(cd,name,remark2);
                    retMap.put(cd, subclass);
                }
            }
            return retMap;
        }
    }

    public static String h_format_Seireki_MD(final String date) {
        if (null == date) {
            return date;
        }
        SimpleDateFormat sdf = new SimpleDateFormat();
        String retVal = "";
        sdf.applyPattern("yyyy年M月d日");
        retVal = sdf.format(java.sql.Date.valueOf(date));

        return retVal;
    }
}

// eof
