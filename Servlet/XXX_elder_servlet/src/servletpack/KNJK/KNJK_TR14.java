/*
 * $Id: 9d28a5d778b00379dfd26ce094269708227251a1 $
 *
 * 作成日: 2014/10/15
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJK;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;


//import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJK_TR14 {

    private static final Log log = LogFactory.getLog(KNJK_TR14.class);
    
    private static final List _threads = new ArrayList();

    private Param _param;
    
    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        
        synchronized (_threads) {
            if (_threads.size() > 0) {
                log.fatal("他スレッドが動作中です。 size = " + _threads.size());
                if ("1".equals(request.getParameter("KILL"))) {
                    for (final Iterator it = _threads.iterator(); it.hasNext();) {
                        final Thread t = (Thread) it.next();
                        log.fatal(" スレッドをインタラプトします。:" + t);
                        t.interrupt();
                        it.remove();
                    }
                }
                return;
            }
        }
        _threads.add(Thread.currentThread());

        DB2UDB db2 = null;
        final OutputWrapper ow = new OutputWrapper();
        final StopWatch sw = new StopWatch();
        sw.start();
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            final Collection toriIkouDatList = QueryUtil.getRowList(db2, "select cd from tori_ikou_dat where target = '2' ");
            ow._outputToFile = toriIkouDatList.size() > 0 ? true : false;
            log.fatal(" outputToFile = " + ow._outputToFile);

            if (ow._outputToFile) {
                response.setContentType("text/html");
            } else {
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setHeader("Accept-Ranges", "none");
                response.setHeader("Content-Transfer-Encoding", "binary");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + new String((_param._filename + ow._ext).getBytes("MS932"), "ISO8859-1") + "\"");
                ow._os = new BufferedOutputStream(response.getOutputStream());
                ow.setNewBook();
                ow.setNewSheet(ow.getDefaultSheetName());
            }
            
//            os.write(new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}); // BOM

            final Collection cds = _param.getTargetCodes(db2);

            for (final Iterator it = cds.iterator(); it.hasNext();) {
                final String cd = (String) it.next();
                if (Thread.interrupted()) {
                    throw new InterruptedException("interrupt in " + cd);
                }
                final OutputFmt outputFmt = getOutputFmt(cd);
                if (null == outputFmt) {
                    log.warn("対象なし cd = " + cd);
                } else {
                    outputFmt.preprocess(db2, _param);
                    try {
                        outputFmt.output(db2, _param, ow);
                    } catch (final Throwable t) {
                        log.fatal("throwed : ", t);
                    } finally {
                        Param.printMemoryInfo("");
                    }
                    outputFmt.closePsQuietly();
                }
            }
            sw.stop();
            
            if (ow._outputToFile) {
                final PrintWriter pw = new PrintWriter(response.getOutputStream());
                pw.println("<html>");
                pw.println("<head>");
                pw.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
                pw.println("</head>");
                pw.println("<body>");
                pw.println("<div>出力完了." + Param.getOutputTime() + "（経過時間: " + sw.toString() + "） 出力ディレクトリ = " +  _param._outputDir.getAbsolutePath() + "</div>");
                pw.println("</body>");
                pw.println("</html>");
                pw.close();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
            _threads.remove(Thread.currentThread());
        } finally {
            log.info(" KNJK_TR14 finally block.");

            ow.closeQuietly();

            if ("1".equals(_param._noDelt)) {
                log.info("移行用テーブルを削除しません");
            } else {
                IkouUtil.postProcess(db2);
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
            _threads.remove(Thread.currentThread());
        }

    }

    private static final String _01_学校 = "01";
    private static final String _02_課程 = "02";
    private static final String _03_学科 = "03";
    private static final String _04_系列 = "04";
    private static final String _05_役職 = "05";
    private static final String _06_分掌 = "06";
    private static final String _07_職員 = "07";
    private static final String _08_教室 = "08";
    private static final String _09_駅 = "09";
    private static final String _10_教科書 = "10";
    private static final String _11_学期期間D = "11";
    private static final String _12_部活 = "12";
    private static final String _13_委員 = "13";
    private static final String _14_学校区 = "14";
    private static final String _15_入学区分D = "15";
    private static final String _16_通学 = "16";
    private static final String _17_続柄 = "17";
    private static final String _18_中学校 = "18";
    private static final String _19_前籍校 = "19";
    private static final String _20_年度クラス = "20";
    private static final String _21_主催者 = "21";
    private static final String _22_教科 = "22";
    private static final String _23_正式科目名 = "23";
    private static final String _24_校内科目 = "24";
    private static final String _25_講座 = "25";
    private static final String _26_講座使用教科書D = "26";
    private static final String _27_生徒 = "27";
    private static final String _28_生徒変更履歴D = "28";
    private static final String _29_生徒所見D = "29";
    private static final String _30_生徒転入用備考D = "30";
    private static final String _31_生徒部活委員 = "31";
    private static final String _32_クラス編成 = "32";
    private static final String _33_異動情報D = "33";
    private static final String _34_休学履歴D = "34";
    private static final String _35_留学履歴D = "35";
    private static final String _36_証明書発行台帳D = "36";
    private static final String _37_評価基準 = "37";
    private static final String _38_評定基準 = "38";
    private static final String _39_考査 = "39";
    private static final String _40_成績点数 = "40";
    private static final String _41_成績評定 = "41";
    private static final String _42_通知表所見 = "42";
    private static final String _43_年間成績情報 = "43";
    private static final String _44_増加単位 = "44";
    private static final String _45_欠課種類 = "45";
    private static final String _46_欠席種類 = "46";
    private static final String _47_集計出欠期間D = "47";
    private static final String _48_集計値講座 = "48";
    private static final String _49_集計値クラス = "49";
    private static final String _50_日々講座出欠 = "50";
    private static final String _51_日々クラス出欠 = "51";
    private static final String _52_特活時間数D = "52";
    private static final String _53_特活認定数D = "53";
    private static final String _54_年間出欠情報 = "54";
    private static final String _55_学期別時間割名D = "55";
    private static final String _56_時間割 = "56";
    private static final String _57_年度休校日 = "57";
    private static final String _58_授業実施日予定日 = "58";
    private static final String _59_受講生 = "59";
    private static final String _60_文例情報 = "60";
    private static final String _61_指導要録所見 = "61";
    private static final String _62_進学用調査書所見 = "62";
    private static final String _63_就職用調査書所見 = "63";
    private static final String _64_概評段階人数D = "64";
    private static final String _65_保健室発生場所 = "65";
    private static final String _66_保健室部位 = "66";
    private static final String _67_保健室原因 = "67";
    private static final String _68_保健室時間 = "68";
    private static final String _69_保健室症状 = "69";
    private static final String _70_保健室処置 = "70";
    private static final String _71_保健室来室情報D = "71";
    private static final String _72_保健日報D = "72";
    private static final String _73_健康診断検査区分 = "73";
    private static final String _74_健康診断情報 = "74";
    private static final String _75_健康診断歯式情報 = "75";
    private static final String _76_既往症 = "76";
    private static final String _77_進路区分 = "77";
    private static final String _78_進路推薦区分 = "78";
    private static final String _79_進路先m = "79";
    private static final String _80_進路希望 = "80";
    
    private static String ZEN = "ZENKAKU"; // 全角
    private static String HAN = "HANKAKU"; // 半角
    
    private OutputFmt getOutputFmt(final String cd) {
        log.info(" ikou cd = " + cd);
        OutputFmt outputFmt = null;
        String[][] titleLines = null; // 必要ないならnull
        List createTableList = new ArrayList();
        String sql = null;
        final Fld[] flds;
        if (_01_学校.equals(cd)) {
            flds = new Fld[] { f("学校名", ZEN, 40), f("郵便番号"), f("都道府県", ZEN, 12), f("市郡区", ZEN, 16), f("住所", ZEN, 100), f("電話番号", ZEN, 16), f("FAX番号", ZEN, 16), f("英字校名", ZEN, 40), f("英字住所", HAN, 200)};

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("  SCHOOLNAME1,  ");
            stb.append("  SCHOOLZIPCD, ");
            stb.append("  T2.PREF_NAME, ");
            stb.append("  '', ");
            stb.append("  VALUE(SCHOOLADDR1, '') || VALUE(SCHOOLADDR2, ''), ");
            stb.append("  SCHOOLTELNO, ");
            stb.append("  SCHOOLFAXNO, ");
            stb.append("  SCHOOLNAME_ENG, ");
            stb.append("  VALUE(SCHOOLADDR1_ENG, '') || VALUE(SCHOOLADDR2_ENG, '') ");
            stb.append(" FROM V_SCHOOL_MST T1 ");
            stb.append(" LEFT JOIN PREF_MST T2 ON T2.PREF_CD = T1.PREF_CD ");
            stb.append(" WHERE ");
            stb.append(" YEAR = (SELECT MAX(YEAR) FROM SCHOOL_MST) ");
            stb.append(" order by ");
            stb.append(" YEAR ");
            sql = stb.toString();
            
        } else if (_02_課程.equals(cd)) {
            flds = new Fld[] { f("コード"), f("課程名")};
            
            sql = " SELECT COURSECD, COURSENAME FROM COURSE_MST order by coursecd  ";

        } else if (_03_学科.equals(cd)) {
            flds = new Fld[] { f("コード"), f("学科名"), f("調査書表示用学科名")};
            
            sql = " SELECT COURSECD || MAJORCD, MAJORNAME, MAJORNAME FROM MAJOR_MST order by coursecd, majorcd ";

        } else if (_04_系列.equals(cd)) {
            flds = new Fld[] { f("コード"), f("系列名称"), f("略称")};
        
            sql = " SELECT COURSECODE, COURSECODENAME, COURSECODENAME FROM COURSECODE_MST order by coursecode ";
            
        } else if (_05_役職.equals(cd)) {
            titleLines = new String[][] {{"役職情報一覧"}};
            flds = new Fld[] { f("役職コード", HAN, 2), f("役職名", ZEN, 8), f("備考", ZEN, 16)};
        
            sql = " SELECT JOBCD, JOBNAME, '' AS REMARK FROM JOB_MST order by jobcd ";

        } else if (_06_分掌.equals(cd)) {
            titleLines = new String[][] {{"分掌一覧"}, {}};
            flds = new Fld[] { f("分掌コード", HAN, 3), f("分掌名", ZEN, 10), f("備考", ZEN, 50)};
            
            final StringBuffer stb = new StringBuffer();
            stb.append(" select  ");
            stb.append("  DUTYSHARECD, ");
            stb.append("  SHARENAME, ");
            stb.append("  '' as biko ");
            stb.append(" from  ");
            stb.append("  DUTYSHARE_MST  ");
            stb.append(" order by  ");
            stb.append(" DUTYSHARECD  ");
            sql = stb.toString();

        } else if (_07_職員.equals(cd)) {
            titleLines = new String[][] {{"職員情報一覧"}};
            flds = new Fld[] { f("年度"), f("職員番号"), f("氏名", ZEN, 12), f("よみ", ZEN, 12), f("短縮名", ZEN, 4), f("英字氏名", HAN, 40), 
                    f("教科番号", HAN, 2), dummy("教科名"), f("役職コード", HAN, 2), dummy("役職名"), f("部活コード", HAN, 3), dummy("部活名"),
                    f("分掌コード", HAN, 3), dummy("分掌名"), f("権限"), f("ログインID"), f("パスワード")};
        
            final StringBuffer stb = new StringBuffer();
            stb.append(" with club_adviser as ( ");
            stb.append(" select year, adviser as staffcd, min(clubcd) as clubcd ");
            stb.append(" from club_adviser_dat ");
            stb.append(" group by year, adviser ");
            stb.append(" ), chair_stf as ( ");
            stb.append(" select t1.year, t1.staffcd, min(t2.classcd) as classcd ");
            stb.append(" from chair_stf_dat t1 ");
            stb.append(" inner join chair_dat t2 on t2.year = t1.year and t2.semester = t1.semester and t2.chaircd = t1.chaircd ");
            stb.append(" group by t1.year, t1.staffcd ");
            stb.append(" ) select ");
            stb.append("  t2.year, ");
            stb.append("  t1.staffcd, ");
            stb.append("  t1.staffname, ");
            stb.append("  t1.staffname_kana, ");
            stb.append("  t1.staffname_eng, ");
            stb.append("  t1.staffname, ");
            stb.append("  t5.classcd, ");
            stb.append("  '' as classname, ");
            stb.append("  t1.jobcd, ");
            stb.append("  '' as jobname, ");
            stb.append("  t3.clubcd, ");
            stb.append("  '' as clubname, ");
            stb.append("  t1.dutysharecd, ");
            stb.append("  '' as dutysharename, ");
            stb.append("  case when t6.staffcd is not null then '管理者' else '一般' end as kengen, ");
            stb.append("  t4.userid, ");
            stb.append("  '' as password ");
            stb.append(" from staff_mst t1 ");
            stb.append(" left join user_mst t4 on t4.staffcd = t1.staffcd ");
            stb.append(" inner join staff_ydat t2 on t2.staffcd = t1.staffcd ");
            stb.append(" left join club_adviser t3 on t3.year = t2.year and t3.staffcd = t2.staffcd ");
            stb.append(" left join chair_stf t5 on t5.year = t2.year and t5.staffcd = t2.staffcd ");
            stb.append(" left join usergroup_dat t6 on t6.year = t2.year and t6.groupcd = '9999' and t6.staffcd = t1.staffcd ");
            stb.append(" order by ");
            stb.append(" t1.staffcd, t2.year ");
            sql = stb.toString();

        } else if (_08_教室.equals(cd)) {
            titleLines = new String[][] {{"教室情報一覧"}};
            flds = new Fld[] { f("室番号", HAN, 4), f("教室名", ZEN, 11), f("教室として使用"), f("定員")};
            
            final StringBuffer stb = new StringBuffer();
            stb.append(" select  ");
            stb.append(" FACCD, ");
            stb.append("  FACILITYNAME, ");
            stb.append("  case when chr_capacity > 0 then '○' end, ");
            stb.append("  CAPACITY ");
            stb.append(" from  ");
            stb.append("  FACILITY_MST  ");
            stb.append(" order by  ");
            stb.append(" FACCD ");
            sql = stb.toString();

        } else if (_09_駅.equals(cd)) {
            flds = new Fld[] { f("コード"), f("駅名称"), f("区分"), f("並び順")};
            createTableList.add(IkouUtil.tori_station_mst);
            
            sql = " select * from tori_station_mst order by stationcd ";

        } else if (_10_教科書.equals(cd)) {
            titleLines = new String[][] {{}};
            flds = new Fld[] { f("教科書コード", HAN, 10), f("使用"), f("書籍名", ZEN, 30), f("価格", HAN, 7), f("備考", ZEN, 20)};
            
            final StringBuffer stb = new StringBuffer();
            stb.append(" select  ");
            stb.append("  t1.textbookcd, ");
            stb.append("  case when t2.textbookcd is not null then '○' end as use, ");
            stb.append("  t1.textbookname, ");
            stb.append("  t1.textbookunitprice, ");
            stb.append("  t1.remark ");
            stb.append(" from textbook_mst t1 ");
            stb.append(" left join (select textbookcd from chair_textbook_dat) t2 on ");
            stb.append("  t2.textbookcd = t1.textbookcd ");
            stb.append(" where ");
            stb.append("  textbookdiv = '1' ");
            stb.append(" order by  ");
            stb.append("  t1.textbookcd ");
            sql = stb.toString();

        } else if (_11_学期期間D.equals(cd)) {
            flds = new Fld[] { f("年度"), f("コード"), f("学期名称"), f("開始日"), f("終了日")};

            final StringBuffer stb = new StringBuffer();
            stb.append(" select year, semester, semestername, sdate, edate from semester_mst ");
            stb.append(" where semester <> '9' ");
            stb.append(" order by year, semester ");
            sql = stb.toString();

        } else if (_12_部活.equals(cd)) {
            flds = new Fld[] { f("コード"), f("名称")};
            
            sql = " select clubcd, clubname from club_mst order by clubcd ";

        } else if (_13_委員.equals(cd)) {
            flds = new Fld[] { f("コード"), f("委員・生徒会名称")};
            
            final StringBuffer stb = new StringBuffer();
            stb.append(" select  ");
            stb.append("  COMMITTEECD, ");
            stb.append("  COMMITTEENAME ");
            stb.append(" from  ");
            stb.append("  COMMITTEE_MST  ");
            stb.append(" order by  ");
            stb.append("  COMMITTEECD ");
            sql = stb.toString();

        } else if (_14_学校区.equals(cd)) {
            flds = new Fld[] { f("コード"), f("校区名称")};
        
            final StringBuffer stb = new StringBuffer();
            stb.append(" select namecd2, name1 ");
            stb.append(" from name_mst ");
            stb.append(" where namecd1 = 'Z003' ");
            stb.append(" order by namecd2 ");
            sql = stb.toString();

        } else if (_15_入学区分D.equals(cd)) {
            flds = new Fld[] { f("コード"), f("入学種類")};

            final StringBuffer stb = new StringBuffer();
            stb.append(" select namecd2, name1  from name_mst where namecd1 = 'A002' order by namecd2 ");
            sql = stb.toString();

        } else if (_16_通学.equals(cd)) {
            flds = new Fld[] { f("コード"), f("通学手段")};
            
            final StringBuffer stb = new StringBuffer();
            stb.append(" select namecd2, name1 ");
            stb.append(" from name_mst where namecd1 = 'H100' order by namecd2 ");
            sql = stb.toString();

        } else if (_17_続柄.equals(cd)) {
            flds = new Fld[] { f("コード"), f("続柄")};

            final StringBuffer stb = new StringBuffer();
            stb.append(" select namecd2, name1 ");
            stb.append(" from name_mst where namecd1 = 'H201' order by namecd2 ");
            sql = stb.toString();

        } else if (_18_中学校.equals(cd)) {
            titleLines = new String[][] {{}};
            flds = new Fld[] { f("中学コード", HAN, 6), f("立区分", HAN, 20), f("中学校名", HAN, 20), f("郵便番号"), f("都道府県", HAN, 6), f("市郡区", HAN, 8), f("住所", HAN, 50)};
        
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT  ");
            stb.append("      T1.FINSCHOOLCD,  ");
            stb.append("      L4.NAME1 AS FINSCHOOL_DIV, ");
            stb.append("      T1.FINSCHOOL_NAME,  ");
            stb.append("      T1.FINSCHOOL_ZIPCD,  ");
            stb.append("      value(P1.PREF_NAME, L6.NAME1) AS FINSCHOOL_PREF_CD,  ");
            stb.append("      L5.NAME1 AS DISTRICTCD,  ");
            stb.append("      value(T1.FINSCHOOL_ADDR1, '') || value(T1.FINSCHOOL_ADDR2, '')  ");
            stb.append("  FROM  ");
            stb.append("      FINSCHOOL_MST T1  ");
            stb.append("  LEFT JOIN NAME_MST L4 ON T1.FINSCHOOL_DIV      = L4.NAMECD2 AND L4.NAMECD1 = 'L015'  ");
            stb.append("  LEFT JOIN NAME_MST L5 ON T1.DISTRICTCD         = L5.NAMECD2 AND L5.NAMECD1 = 'Z003'  ");
            stb.append("  LEFT JOIN NAME_MST L6 ON T1.FINSCHOOL_DISTCD2  = L6.NAMECD2 AND L6.NAMECD1 = 'Z015'  ");
            stb.append("  LEFT JOIN PREF_MST P1 ON T1.FINSCHOOL_PREF_CD  = P1.PREF_CD  ");
            stb.append(" where ");
            stb.append(" t1.finschool_type = '3' ");
            stb.append(" order by  ");
            stb.append("      T1.FINSCHOOLCD  ");
            sql = stb.toString();

        } else if (_19_前籍校.equals(cd)) {
            titleLines = new String[][] {{}};
            flds = new Fld[] { f("前籍校コード", HAN, 6), f("立区分", HAN, 20), f("前籍校名", HAN, 20), f("郵便番号"), f("都道府県", HAN, 6), f("市郡区", HAN, 8), f("住所", HAN, 50)};
        
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT  ");
            stb.append("      T1.FINSCHOOLCD,  ");
            stb.append("      L4.NAME1 AS FINSCHOOL_DIV, ");
            stb.append("      T1.FINSCHOOL_NAME,  ");
            stb.append("      T1.FINSCHOOL_ZIPCD,  ");
            stb.append("      value(P1.PREF_NAME, L6.NAME1) AS FINSCHOOL_PREF_CD,  ");
            stb.append("      L5.NAME1 AS DISTRICTCD,  ");
            stb.append("      value(T1.FINSCHOOL_ADDR1, '') || value(T1.FINSCHOOL_ADDR2, '')  ");
            stb.append("  FROM  ");
            stb.append("      FINSCHOOL_MST T1  ");
            stb.append("  LEFT JOIN NAME_MST L4 ON T1.FINSCHOOL_DIV      = L4.NAMECD2 AND L4.NAMECD1 = 'L015'  ");
            stb.append("  LEFT JOIN NAME_MST L5 ON T1.DISTRICTCD         = L5.NAMECD2 AND L5.NAMECD1 = 'Z003'  ");
            stb.append("  LEFT JOIN NAME_MST L6 ON T1.FINSCHOOL_DISTCD2  = L6.NAMECD2 AND L6.NAMECD1 = 'Z015'  ");
            stb.append("  LEFT JOIN PREF_MST P1 ON T1.FINSCHOOL_PREF_CD  = P1.PREF_CD  ");
            stb.append(" where ");
            stb.append(" t1.finschool_type = '4' ");
            stb.append(" order by  ");
            stb.append("      T1.FINSCHOOLCD  ");
            sql = stb.toString();

        } else if (_20_年度クラス.equals(cd)) {
            titleLines = new String[][] {{"組情報一覧"}};
            flds = new Fld[] { f("年度"), f("組番号", HAN, 3), f("学年", HAN, 4), f("組", HAN, 2), f("組名", ZEN, 20), f("略称", ZEN, 4), f("教室番号", HAN, 4), dummy("教室"), f("職員番号1"), dummy("担任１"), f("職員番号2"), dummy("担任２"), f("職員番号3"), dummy("担任３")};
        
            final StringBuffer stb = new StringBuffer();
            stb.append(" with all as ( ");
            stb.append(" select ");
            stb.append("  distinct t1.year, ");
            stb.append("  t1.grade || t1.hr_class as grade_hr_class,  ");
            stb.append("  t1.grade, ");
            stb.append("  t1.hr_class, ");
            stb.append("  t1.hr_name, ");
            stb.append("  t1.hr_nameabbv, ");
            stb.append("  t1.tr_cd1, ");
            stb.append("  t1.tr_cd2, ");
            stb.append("  t1.tr_cd3 ");
            stb.append(" from schreg_regd_hdat t1 ");
            stb.append(" inner join (select distinct year, semester, grade, hr_class from schreg_regd_dat) t2 on t2.year = t1.year ");
            stb.append("     and t2.semester = t1.semester ");
            stb.append("     and t2.grade = t1.grade ");
            stb.append("     and t2.hr_class = t1.hr_class ");
            stb.append(" ), trgroup as ( ");
            stb.append(" select ");
            stb.append("  t1.year, ");
            stb.append("  t1.grade_hr_class, ");
            stb.append("  t1.grade, ");
            stb.append("  t1.hr_class, ");
            stb.append("  t1.hr_name, ");
            stb.append("  t1.hr_nameabbv, ");
            stb.append("  max(t1.tr_cd1) as tr_cd1, ");
            stb.append("  max(t1.tr_cd2) as tr_cd2, ");
            stb.append("  max(t1.tr_cd3) as tr_cd3 ");
            stb.append(" from all t1 ");
            stb.append(" group by  ");
            stb.append("  t1.year, ");
            stb.append("  t1.grade_hr_class, ");
            stb.append("  t1.grade, ");
            stb.append("  t1.hr_class, ");
            stb.append("  t1.hr_name, ");
            stb.append("  t1.hr_nameabbv ");
            stb.append(" ) ");
            stb.append(" select ");
            stb.append("  t1.year, ");
            stb.append("  t1.grade_hr_class, ");
            stb.append("  t1.grade, ");
            stb.append("  t1.hr_class, ");
            stb.append("  t1.hr_name, ");
            stb.append("  t1.hr_nameabbv, ");
            stb.append("  '' as roomno, ");
            stb.append("  '' as roomname, ");
            stb.append("  case when t2.staffcd is not null then t2.staffcd end as tr_cd1, ");
            stb.append("  t2.staffname as tr_name1, ");
            stb.append("  case when t3.staffcd is not null then t3.staffcd end as tr_cd2, ");
            stb.append("  t3.staffname as tr_name2, ");
            stb.append("  case when t4.staffcd is not null then t4.staffcd end as tr_cd3, ");
            stb.append("  t4.staffname as tr_name3 ");
            stb.append(" from trgroup t1 ");
            stb.append(" left join staff_mst t2 on t2.staffcd = t1.tr_cd1 ");
            stb.append(" left join staff_mst t3 on t3.staffcd = t1.tr_cd2 ");
            stb.append(" left join staff_mst t4 on t4.staffcd = t1.tr_cd3 ");
            stb.append(" order by t1.year, t1.grade_hr_class ");
            sql = stb.toString();

        } else if (_21_主催者.equals(cd)) {
            flds = new Fld[] { f("コード"), f("資格情報主催名")};
            createTableList.add(IkouUtil.tori_promoter_mst);

            sql = " select * from tori_promoter_mst order by promoter_cd ";

        } else if (_22_教科.equals(cd)) {
            titleLines = new String[][] {{"教科一覧"}};
            flds = new Fld[] { f("教科番号", HAN, 2), f("教科名", ZEN, 5), f("種別"), f("種別出力"), f("留学"), f("英字教科名", ZEN, 30)};
        
            final StringBuffer stb = new StringBuffer();
            stb.append(" select  ");
            stb.append("     classcd, ");
            stb.append("     classname,  ");
            stb.append("     case when specialdiv = '1' then '専門教科' else '普通教科' end as shubetsu, ");
            stb.append("     case when classcd <= '90' then '1' end as shubetsu_shuturyoku, ");
            stb.append("     '' as ryugaku, ");
            stb.append("     classname_eng ");
            stb.append(" from class_mst ");
            stb.append(" where school_kind = 'H' ");
            stb.append(" order by classcd ");
            sql = stb.toString();

        } else if (_23_正式科目名.equals(cd)) {
            titleLines = new String[][] {{"科目名一覧"}};
            flds = new Fld[] { f("教科番号"), dummy("教科名"), f("科目名番号"), f("科目名", ZEN, 30), f("英字科目名", HAN, 60)};
            createTableList.add(IkouUtil.tori_subclass_name_mst);
            
            final StringBuffer stb = new StringBuffer();
            stb.append(" select distinct t1.classcd, t1.classname, value(hnkn.tgt_subclasscd, t1.subclasscd) as subclasscd, t1.subclassname, t1.subclassname_eng ");
            stb.append(" from tori_subclass_name_mst t1 ");
            stb.append(" left join tori_subclass_henkan hnkn on hnkn.classcd = t1.classcd and hnkn.school_kind = t1.school_kind and hnkn.curriculum_cd = t1.curriculum_cd and hnkn.subclasscd = t1.subclasscd ");
            stb.append(" where t1.classcd <= '90' order by t1.classcd, value(hnkn.tgt_subclasscd, t1.subclasscd) ");
            sql = stb.toString();

        } else if (_24_校内科目.equals(cd)) {
            titleLines = new String[][] {{}};
            flds = new Fld[] { f("年度"), f("教科番号"), dummy("教科名"), f("科目番号"), f("科目名番号"), dummy("科目名"), f("校内科目名", ZEN, 20), f("短縮科目名", ZEN, 5), dummy("単位数"), f("必履修"), f("単位認定"), f("認定期"), f("調査書出力"), f("指導要録出力"), f("通知表出力"), f("証明書出力"), f("総合的な学習"), f("HR登録"), f("SHR登録"), f("成績なし"), f("定通併修"), f("履修/増単")};
            createTableList.add(IkouUtil.tori_subclass_name_mst);
            
            final StringBuffer stb = new StringBuffer();
            stb.append(" with t_credits as ( ");
            stb.append(" select ");
            stb.append("   t1.year, ");
            stb.append("   t1.classcd, ");
            stb.append("   t1.school_kind, ");
            stb.append("   t1.curriculum_cd, ");
            stb.append("   value(hnkn.tgt_subclasscd, t1.subclasscd) as subclasscd, ");
            stb.append("   max(takesemes) as takesemes, ");
            stb.append("   max(t4.credits) as credits, ");
            stb.append("   min(require_flg) as require_flg ");
            stb.append(" from chair_dat t1 ");
            stb.append(" inner join chair_std_dat t2 on t2.year = t1.year and t2.semester = t1.semester and t2.chaircd = t1.chaircd ");
            stb.append(" inner join schreg_regd_dat t3 on t3.schregno = t2.schregno and t3.year = t2.year and t3.semester = t2.semester ");
            stb.append(" left join credit_mst t4 on t4.year = t3.year and t4.coursecd = t3.coursecd and t4.majorcd = t3.majorcd and t4.grade = t3.grade and t4.coursecode = t3.coursecode and t4.classcd = t1.classcd and t4.school_kind = t1.school_kind and t4.curriculum_cd = t1.curriculum_cd and t4.subclasscd = t1.subclasscd ");
            stb.append(" left join tori_subclass_henkan hnkn on hnkn.classcd = t1.classcd and hnkn.school_kind = t1.school_kind and hnkn.curriculum_cd = t1.curriculum_cd and hnkn.subclasscd = t1.subclasscd ");
            stb.append(" group by ");
            stb.append("   t1.year, ");
            stb.append("   t1.semester, ");
            stb.append("   t1.classcd, ");
            stb.append("   t1.school_kind, ");
            stb.append("   t1.curriculum_cd, ");
            stb.append("   value(hnkn.tgt_subclasscd, t1.subclasscd) ");
            stb.append(" ) ");
            stb.append(" select distinct ");
            stb.append("   t1.year, ");
            stb.append("   t1.classcd as kyouka_bangou, ");
            stb.append("   '' as kyouka_mei, ");
            stb.append("   value(hnkn.tgt_subclasscd, t1.subclasscd) as subclasscd, ");
            stb.append("   case ");
            stb.append("      when t5.subclasscd is not null then value(hnkn5.tgt_subclasscd, t5.subclasscd) ");
            stb.append("      when t3.subclasscd is not null then value(hnkn.tgt_subclasscd, t1.subclasscd) ");
            stb.append("   end as subclassname_cd, ");
            stb.append("   '' as kamokumei, ");
            stb.append("   t1.subclassname as kounai_kamokumei, ");
            stb.append("   t1.subclassabbv as tanshuku_kamokumei, ");
            stb.append("   value(t4h.credits, t4.credits) as tanni_suu, ");
            stb.append("   case value(t4h.require_flg, t4.require_flg) when '3' then '0' ");
            stb.append("            when '1' then '1' ");
            stb.append("            when '2' then '2' ");
            stb.append("   end as hitsu_rishu, ");
            stb.append("   '' as tanni_nintei, ");
            stb.append("   value(t4h.takesemes, t4.takesemes) as ninteiki, ");
            stb.append("   case when (t5.subclasscd is null or t3.subclasscd is not null) and t1.classcd <= '90' then '○' end as tyousasho_shutsuryoku, ");
            stb.append("   case when (t5.subclasscd is null or t3.subclasscd is not null) and t1.classcd <= '90' then '○' end as sidouyouroku_shutsuryoku, ");
            stb.append("   case when t1.classcd <= '90' then '○' end as tuuchihyou_shutsuryoku, ");
            stb.append("   case when (t5.subclasscd is null or t3.subclasscd is not null) and t1.classcd <= '90' then '○' end as shoumeisho_shutsuryoku, ");
            stb.append("   case when t1.classcd = '90' then '○' end as sogaku, ");
            stb.append("   case when t1.classcd > '90' and (t1.subclassname like '%LHR%' or t1.subclassname like '%ＬＨＲ%' or t7.special_group_name like '%LHR%' or t7.special_group_name like '%ＬＨＲ%') then '○' else '' end as HR_touroku, ");
            stb.append("   case when t1.classcd > '90' and (t1.subclassname like '%SHR%' or t1.subclassname like '%ＳＨＲ%' or t7.special_group_name like '%SHR%' or t7.special_group_name like '%ＳＨＲ%') then '○' else '' end as SHR_touroku, ");
            stb.append("   '' as seiseki_nashi, ");
            stb.append("   '' as teitsu_heishu, ");
            stb.append("   0 as rishu_zoutan ");
            stb.append(" from v_subclass_mst t1 ");
            stb.append(" left join tori_subclass_henkan hnkn on hnkn.classcd = t1.classcd and hnkn.school_kind = t1.school_kind and hnkn.curriculum_cd = t1.curriculum_cd and hnkn.subclasscd = t1.subclasscd ");
            stb.append(" left join tori_subclass_name_mst t3 on t3.classcd = t1.classcd and t3.school_kind = t1.school_kind and t3.curriculum_cd = t1.curriculum_cd and t3.subclasscd = t1.subclasscd ");
            stb.append(" left join t_credits t4h on t4h.year = t1.year and t4h.classcd = t1.classcd and t4h.school_kind = t1.school_kind and t4h.curriculum_cd = t1.curriculum_cd and t4h.subclasscd = hnkn.tgt_subclasscd ");
            stb.append(" left join t_credits t4 on t4.year = t1.year and t4.classcd = t1.classcd and t4.school_kind = t1.school_kind and t4.curriculum_cd = t1.curriculum_cd and t4.subclasscd = t1.subclasscd ");
            stb.append(" left join subclass_mst t5 on t5.classcd = t1.classcd and t5.school_kind = t1.school_kind and t5.curriculum_cd = t1.curriculum_cd and t5.subclasscd = t1.subclasscd2 ");
            stb.append(" left join tori_subclass_henkan hnkn5 on hnkn5.classcd = t5.classcd and hnkn5.school_kind = t5.school_kind and hnkn5.curriculum_cd = t5.curriculum_cd and hnkn5.subclasscd = t5.subclasscd ");
            stb.append(" left join attend_subclass_special_dat t6 on t6.year = t1.year and t6.classcd = t1.classcd and t6.school_kind = t1.school_kind and t6.curriculum_cd = t1.curriculum_cd and t6.subclasscd = t1.subclasscd ");
            stb.append(" left join attend_subclass_special_mst t7 on t7.special_group_cd = t6.special_group_cd ");
            stb.append(" where (value(t4h.credits, t4.credits) is not null or t1.classcd >= '90' and t4.year is not null) ");
            stb.append(" order by t1.year, t1.classcd, value(hnkn.tgt_subclasscd, t1.subclasscd) ");
            sql = stb.toString();

        } else if (_25_講座.equals(cd)) {
            titleLines = new String[][] {{"講座情報"}};
            flds = new Fld[] { f("年度"), f("科目番号"), dummy("校内科目名"), f("講座番号"), f("講座名", ZEN, 20), f("修得期コード"), dummy("修得期"), f("同一試験コード"), f("同一試験名称"), f("初期教室番号"), dummy("初期教室"), f("担当職員番号"), dummy("担当職員名"), f("受講クラス")};
        
            final StringBuffer stb = new StringBuffer();
            stb.append(" with chr_takesemes as ( ");
            stb.append(" select year, chaircd, max(takesemes) as takesemes ");
            stb.append(" from chair_dat ");
            stb.append(" group by year, chaircd ");
            stb.append(" ), max_chr as ( ");
            stb.append(" select year, chaircd, max(semester) as semester ");
            stb.append(" from chair_dat  ");
            stb.append(" group by year, chaircd ");
            stb.append(" ), chr_fac as ( ");
            stb.append(" select year, chaircd, min(faccd) as faccd ");
            stb.append(" from chair_fac_dat  ");
            stb.append(" group by year, chaircd ");
            stb.append(" ), chr_stf as ( ");
            stb.append(" select year, chaircd, min(staffcd) as staffcd ");
            stb.append(" from chair_stf_dat  ");
            stb.append(" group by year, chaircd ");
            stb.append(" ), chr_cls0 as ( ");
            stb.append(" select  year, chaircd, trgtgrade || trgtclass as grade_hr_class ");
            stb.append(" from chair_cls_dat t1 ");
            stb.append(" where t1.groupcd = '0000' ");
            stb.append(" union all ");
            stb.append(" select  t1.year, t2.chaircd, t1.trgtgrade || t1.trgtclass as grade_hr_class ");
            stb.append(" from chair_cls_dat t1 ");
            stb.append(" inner join chair_dat t2 on t2.year = t1.year and t2.semester = t1.semester and t2.groupcd = t1.groupcd ");
            stb.append(" where t1.groupcd <> '0000' ");
            stb.append(" ), chr_cls as ( ");
            stb.append(" select year, chaircd, min(grade_hr_class) as grade_hr_class, count(*) as count ");
            stb.append(" from chr_cls0 ");
            stb.append(" group by year, chaircd ");
            stb.append(" ), regd_hdat as ( ");
            stb.append(" select year, grade || hr_class as grade_hr_class, min(hr_name) as hr_name ");
            stb.append(" from schreg_regd_hdat ");
            stb.append(" group by year, grade || hr_class ");
            stb.append(" ) ");
            stb.append(" select  ");
            stb.append("  t1.year, ");
            stb.append("  value(hnkn.tgt_subclasscd, t1.subclasscd) as subclasscd, ");
            stb.append("  '' as subclassname, ");
            stb.append("  t1.chaircd, ");
            stb.append("  t1.chairname, ");
            stb.append("  t2.takesemes, ");
            stb.append("  '' as shutoku_ki, ");
            stb.append("  '' as shiken_cd, ");
            stb.append("  '' as shiken_name, ");
            stb.append("  t3.faccd, ");
            stb.append("  '' as facname, ");
            stb.append("  t4.staffcd, ");
            stb.append("  '' as staffname, ");
            stb.append("  case when t5.count = 1 then t6.hr_name else '(クラス混在)' end as grade_hr_class ");
            stb.append(" from chair_dat t1 ");
            stb.append(" inner join max_chr t0 on t0.year = t1.year and t0.semester = t1.semester and t0.chaircd = t1.chaircd ");
            stb.append(" left join tori_subclass_henkan hnkn on hnkn.classcd = t1.classcd and hnkn.school_kind = t1.school_kind and hnkn.curriculum_cd = t1.curriculum_cd and hnkn.subclasscd = t1.subclasscd ");
            stb.append(" left join chr_takesemes t2 on t2.year = t1.year and t2.chaircd = t1.chaircd ");
            stb.append(" left join chr_fac t3 on t3.year = t1.year and t3.chaircd = t1.chaircd ");
            stb.append(" left join chr_stf t4 on t4.year = t1.year and t4.chaircd = t1.chaircd ");
            stb.append(" left join chr_cls t5 on t5.year =  t1.year and t5.chaircd = t1.chaircd ");
            stb.append(" left join regd_hdat t6 on t6.year = t1.year and t6.grade_hr_class = t5.grade_hr_class ");
            stb.append(" order by t1.year, t1.subclasscd ");
            sql = stb.toString();

        } else if (_26_講座使用教科書D.equals(cd)) {
            titleLines = new String[][] {{"講座で使用する教科書"}};
            flds = new Fld[] { f("講座番号"), f("年度"), f("教科書番号"), dummy(""), dummy("講座で使用する教科書")};
        
            final StringBuffer stb = new StringBuffer();
            stb.append(" select distinct ");
            stb.append("     year,  ");
            stb.append("     chaircd,  ");
            stb.append("     textbookcd, ");
            stb.append("     '' as dummy1, ");
            stb.append("     '' as dummy2 ");
            stb.append(" from chair_textbook_dat ");
            stb.append(" order by  ");
            stb.append("     year,  ");
            stb.append("     chaircd,  ");
            stb.append("     textbookcd ");
            sql = stb.toString();

        } else if (_27_生徒.equals(cd)) {
            createTableList.add(IkouUtil.tori_station_mst);

            titleLines = new String[][] {{}};
            flds = new Fld[] {
                    f("生徒番号"), dummy("出席番号"), f("氏名", ZEN, 12), f("ふりがな", ZEN, 12), f("性別"), 
                    f("生年月日"), f("課程"), f("部"), f("学科・科"), f("系列"), // 10

                    f("入学区分"), f("入学日"), f("中学コード"), f("中学卒業日"), f("前籍校コード"), 
                    f("前籍異動日"), f("前籍学年"), f("郵便番号"), f("都道府県", ZEN, 6), f("市郡区", ZEN, 8),  // 20

                    f("町村名・番地", ZEN, 50), f("住民票郵便番号"), f("住民票都道府県", ZEN, 6), f("住民票市郡区"), f("住民票町村名・番地"), 
                    f("電話番号１"), f("電話番号２"), f("保護者氏名", ZEN, 12), f("保護者ふりがな", ZEN, 12), f("続柄"), // 30

                    f("保護者と違う住所"), f("保護者郵便番号"), f("保護者都道府県", ZEN, 6), f("保護者市郡区", ZEN, 8), f("保護者町村名・番地", ZEN, 50),
                    f("保護者電話番号１"), f("保護者電話番号２"), f("本名", ZEN, 15), f("本名ふりがな", ZEN, 30), f("英字氏名", HAN, 40),  // 40

                    f("メールアドレス", HAN, 50), f("受検番号"), f("校区"), f("通学手段"), f("通学時間"), 
                    f("ステッカー№"), f("通学証明書の駅名"), f("通学証明書の駅経由"), f("進路", ZEN, 30), f("卒業日"), // 50

                    f("卒業学期"), f("卒業予定"), f("卒業証書番号"), f("卒業証書授与日"), f("除籍日"), // 55
                    f("除籍区分"), f("マーク", HAN, 4), f("前籍校の課程・学科・科", ZEN, 30), f("前籍校の在籍開始日"), f("前籍校の在籍終了日"),  // 60

                    f("転学先学校コード"), f("転学先学校の課程・学科・科", ZEN, 30)
            };

            final StringBuffer stb = new StringBuffer();
            stb.append(" with t_aft_remark as ( ");
            stb.append(" SELECT  ");
            stb.append("       T1.SCHREGNO, ");
            stb.append("       CASE WHEN T1.SENKOU_KIND = '0' THEN ");
            stb.append("             VALUE(L3.SCHOOL_NAME, '') || VALUE(L5.FACULTYNAME, '') || VALUE(L6.DEPARTMENTNAME, '')  ");
            stb.append("            WHEN T1.SENKOU_KIND = '1' THEN ");
            stb.append("             L4.COMPANY_NAME  ");
            stb.append("       END AS AFT_GRAD_REMARK ");
            stb.append(" FROM  ");
            stb.append("      AFT_GRAD_COURSE_DAT T1  ");
            stb.append(" LEFT JOIN COLLEGE_MST L3 ON L3.SCHOOL_CD = T1.STAT_CD  ");
            stb.append(" LEFT JOIN COLLEGE_FACULTY_MST L5 ON L5.SCHOOL_CD = L3.SCHOOL_CD AND L5.FACULTYCD = T1.FACULTYCD  ");
            stb.append(" LEFT JOIN COLLEGE_DEPARTMENT_MST L6 ON L6.SCHOOL_CD = L3.SCHOOL_CD AND L6.FACULTYCD = T1.FACULTYCD AND L6.DEPARTMENTCD = T1.DEPARTMENTCD  ");
            stb.append(" LEFT JOIN COMPANY_MST L4 ON L4.COMPANY_CD = T1.STAT_CD  ");
            stb.append(" WHERE  ");
            stb.append("      T1.PLANSTAT = '1'  ");
            stb.append(" ) ");
            stb.append(" select ");
            stb.append("   t1.schregno, ");
            stb.append("   '' as attendno, ");
            stb.append("   t1.name, ");
            stb.append("   t1.name_kana, ");
            stb.append("   t2.abbv1 as sex, ");
            stb.append("   t1.birthday, ");
            stb.append("   t5.coursecd, ");
            stb.append("   '' as bu_code, ");
            stb.append("   t5.coursecd || t5.majorcd as gakka, ");
            stb.append("   t5.coursecode as keiretsu, "); // 10
            stb.append("   t1.ent_div, ");
            stb.append("   t1.ent_date, ");
            stb.append("   t1.finschoolcd, ");
            stb.append("   t1.finish_date, ");
            stb.append("   '' as zenseki_school_cd, ");
            stb.append("   '' as zenseki_idou_bi, ");
            stb.append("   '' as zenseki_gakunen, ");
            stb.append("   t7.zipcd, ");
            stb.append("   '' as address_pref, ");
            stb.append("   '' as addresss_city, "); // 20
            stb.append("   value(t7.addr1, '') || value(t7.addr2, '') as addr, ");
            stb.append("   '' as juminhyo_zipcd, ");
            stb.append("   '' as juminhyo_pref, ");
            stb.append("   '' as juminhyo_city, ");
            stb.append("   '' as juminhyo_addr, ");
            stb.append("   t7.telno, ");
            stb.append("   '' as telno2, ");
            stb.append("   t8.guard_name, ");
            stb.append("   t8.guard_kana, ");
            stb.append("   t8.relationship, "); // 30
            stb.append("   '' as hogoshato_chigau_jusho, ");
            stb.append("   t8.guard_zipcd, ");
            stb.append("   '' as guard_address_pref, ");
            stb.append("   '' as guard_address_city, ");
            stb.append("   value(t8.guard_addr1, '') || value(t8.guard_addr2, '') as guard_addr, ");
            stb.append("   t8.guard_telno, ");
            stb.append("   '' as guard_telno2, ");
            stb.append("   t1.real_name, ");
            stb.append("   t1.real_name_kana, ");
            stb.append("   t1.name_eng, "); // 40
            stb.append("   '' as mail_address, ");
            stb.append("   '' as examno, ");
            stb.append("   '' as gakkou_ku, ");
            stb.append("   '' as school_commute_method, ");
            stb.append("   '' as school_commute_time, "); // 45
            stb.append("   '' as sticker_no, ");
            stb.append("   t16.stationcd as station_name, ");
            stb.append("   t17.stationcd as station_name2, ");
            stb.append("   t18.aft_grad_remark as shinro, ");
            stb.append("   case when t1.grd_div = '1' then t1.grd_date end as grd_date, "); // 50
            stb.append("   t9.grd_semester, ");
            stb.append("   '' as sotsugyo_yotei, ");
            stb.append("   value(t10_2.remark1, rtrim(cast(t10.certif_no as char(5)))) as sotugyo_shousho_bango, ");
            stb.append("   '' as sotugyo_shousho_juyobi, ");
            stb.append("   case when t1.grd_div <> '1' then t1.grd_date end as josekibi, "); // 55
            stb.append("   case when t1.grd_div not in ('1', '4') then t1.grd_div end as grd_div, ");
            stb.append("   '' as mark, ");
            stb.append("   '' as zenseki_katei_gakka_course, ");
            stb.append("   '' as zenseki_kaishibi, ");
            stb.append("   '' as zenseki_shuryobi, "); // 60
            stb.append("   '' as tengakusaki_school_cd, ");
            stb.append("   '' as tengakusaki_katei_gakka_course ");
            stb.append(" from schreg_base_mst t1 ");
            stb.append(" left join name_mst t2 on t2.namecd1 = 'Z002' ");
            stb.append("   and t2.namecd2 = t1.sex ");
            stb.append(" left join (select schregno, max(year) as year ");
            stb.append("               from schreg_regd_dat ");
            stb.append("               group by schregno) t3 on t3.schregno = t1.schregno ");
            stb.append(" left join (select schregno, year, max(semester) as semester ");
            stb.append("              from schreg_regd_dat ");
            stb.append("              group by schregno, year) t4 on t4.schregno = t3.schregno and t4.year = t3.year ");
            stb.append(" left join schreg_regd_dat t5 on t5.schregno = t4.schregno and t5.year = t4.year and t5.semester = t4.semester ");
            stb.append(" left join (select schregno, max(issuedate) as issuedate ");
            stb.append("               from schreg_address_dat ");
            stb.append("               group by schregno) t6 on t6.schregno = t1.schregno ");
            stb.append(" left join schreg_address_dat t7 on t7.schregno = t1.schregno and t7.issuedate = t6.issuedate ");
            stb.append(" left join guardian_dat t8 on t8.schregno = t1.schregno ");
            stb.append(" left join grd_base_mst t9 on t9.schregno = t1.schregno ");
            stb.append(" left join (select schregno, min(certif_no) as certif_no ");
            stb.append("              from certif_issue_dat ");
            stb.append("              where certif_kindcd = '001' ");
            stb.append("             group by schregno) t10 on t10.schregno = t1.schregno ");
            stb.append(" left join (select l1.schregno, min(remark1) as remark1 ");
            stb.append("              from certif_detail_eachtype_dat l1 ");
            stb.append("              inner join certif_issue_dat l2 on l2.year = l1.year and l2.certif_index = l1.certif_index and l2.certif_kindcd = '001' ");
            stb.append("             group by l1.schregno) t10_2 on t10_2.schregno = t1.schregno ");
            stb.append(" left join (select schregno, max(year) as year ");
            stb.append("               from certif_issue_dat ");
            stb.append("               where certif_kindcd = '113' ");
            stb.append("               group by schregno ");
            stb.append("              ) t11 on t11.schregno = t1.schregno ");
            stb.append(" left join (select schregno, year, max(certif_index) as certif_index ");
            stb.append("               from certif_issue_dat ");
            stb.append("              where certif_kindcd = '113' ");
            stb.append("               group by schregno, year ");
            stb.append("              ) t12 on t12.schregno = t11.schregno and t12.year = t11.year ");
            stb.append(" left join certif_issue_dat t14 on t14.year = t12.year and t14.certif_index = t12.certif_index ");
            stb.append(" left join certif_detail_eachtype_dat t15 on t15.year = t14.year and t15.certif_index = t14.certif_index ");
            stb.append(" left join tori_station_mst t16 on t16.stationname = t15.remark1 ");
            stb.append(" left join tori_station_mst t17 on t17.stationname = t15.remark2 ");
            stb.append(" left join t_aft_remark t18 on t18.schregno = t1.schregno ");
            stb.append(" order by t1.schregno ");
            sql = stb.toString();

        } else if (_28_生徒変更履歴D.equals(cd)) {
            flds = new Fld[] { f("生徒番号"), f("変更順"), f("氏名"), f("よみ"), f("本名"), f("本名よみ"), f("英字氏名"), f("生徒-〒番号"), f("生徒-都道府県"), f("生徒-市郡区"), f("生徒-住所"), f("生徒-電話番号"), f("生徒-電話番号２"), f("生徒-〒番号2"), f("生徒-都道府県2"), f("生徒-市郡区2"), f("生徒-住所2"), f("保護者氏名"), f("保護者よみ"), f("続柄コード"), f("保護者-〒番号"), f("保護者-都道府県"), f("保護者-市郡区"), f("保護者-住所"), f("保護者-電話番号"), f("保護者-電話番号２"), f("変更した職員番号")};
        
            final StringBuffer stb = new StringBuffer();
            stb.append(" with issuedates as ( ");
            stb.append(" select schregno, issuedate ");
            stb.append(" from schreg_base_hist_dat ");
            stb.append(" union ");
            stb.append(" select schregno, issuedate ");
            stb.append(" from guardian_hist_dat ");
            stb.append(" union  ");
            stb.append(" select schregno, issuedate ");
            stb.append(" from schreg_address_dat ");
            stb.append(" union ");
            stb.append(" select schregno, issuedate ");
            stb.append(" from guardian_address_dat ");
            stb.append(" ), print_issuedates as ( ");
            stb.append(" select ");
            stb.append("  t1.schregno, ");
            stb.append("  t1.issuedate, ");
            stb.append("  min(t2.issuedate) as schreg_issuedate, ");
            stb.append("  min(t3.issuedate) as guard_issuedate, ");
            stb.append("  min(t4.issuedate) as schadd_issuedate, ");
            stb.append("  min(t5.issuedate) as guardadd_issuedate ");
            stb.append(" from issuedates t1 ");
            stb.append(" left join schreg_base_hist_dat t2 on t2.schregno = t1.schregno ");
            stb.append("  and t2.issuedate <= t1.issuedate ");
            stb.append(" left join guardian_hist_dat t3 on t3.schregno = t1.schregno ");
            stb.append("  and t3.issuedate <= t1.issuedate ");
            stb.append(" left join schreg_address_dat t4 on t4.schregno = t1.schregno ");
            stb.append("  and t4.issuedate <= t1.issuedate ");
            stb.append(" left join guardian_address_dat t5 on t5.schregno = t1.schregno ");
            stb.append("  and t5.issuedate <= t1.issuedate ");
            stb.append(" group by t1.schregno, t1.issuedate ");
            stb.append(" ) ");
            stb.append(" select  ");
            stb.append("   t1.schregno, ");
            stb.append("  row_number() over(partition by t1.schregno order by t1.issuedate) as seq, ");
            stb.append("  t2.name, ");
            stb.append("  t2.name_kana, ");
            stb.append("  t2.real_name, ");
            stb.append("  t2.real_name_kana, ");
            stb.append("  t2.name_eng, ");
            stb.append("  t4.zipcd, ");
            stb.append("  '' as schpref, ");
            stb.append("  '' as schcity,  ");
            stb.append("  value(t4.addr1, '') || value(t4.addr2, '') as schaddr, ");
            stb.append("  t4.telno as schtelno, ");
            stb.append("  '' as schtelno2, ");
            stb.append("  '' as schzipcd2, ");
            stb.append("  '' as schpref2, ");
            stb.append("  '' as schschcity2, ");
            stb.append("  '' as schaddr2, ");
            stb.append("  t3.guard_name, ");
            stb.append("  t3.guard_kana, ");
            stb.append("  t3.relationship, ");
            stb.append("  t5.guard_zipcd, ");
            stb.append("  '' as guardpref, ");
            stb.append("  '' as guardcity, ");
            stb.append("  value(t5.guard_addr1, '') || value(t5.guard_addr2, '') as guardaddr, ");
            stb.append("  t5.guard_telno, ");
            stb.append("  '' as guard_telno2, ");
            stb.append("  case when t1.issuedate = t2.issuedate then t2.registercd ");
            stb.append("          when t1.issuedate =  t3.issuedate then t3.registercd  ");
            stb.append("          when t1.issuedate = t4.issuedate then t4.registercd ");
            stb.append("           else t5.registercd ");
            stb.append("   end as registercd ");
            stb.append(" from print_issuedates t1 ");
            stb.append(" left join schreg_base_hist_dat t2 on t2.schregno = t1.schregno ");
            stb.append("   and t2.issuedate = t1.schreg_issuedate ");
            stb.append(" left join guardian_hist_dat t3 on t3.schregno = t1.schregno ");
            stb.append("   and t3.issuedate = t1.guard_issuedate ");
            stb.append(" left join schreg_address_dat t4 on t4.schregno = t1.schregno ");
            stb.append("   and t4.issuedate = t1.schadd_issuedate ");
            stb.append(" left join guardian_address_dat t5 on t5.schregno = t1.schregno ");
            stb.append("  and t5.issuedate = t1.guardadd_issuedate ");
            stb.append(" order by ");
            stb.append("   t1.schregno ");
            stb.append("  , 2  ");
            sql = stb.toString();

        } else if (_29_生徒所見D.equals(cd)) {
            flds = new Fld[] { f("生徒番号"), f("所見")};
            sql = null;
        
        } else if (_30_生徒転入用備考D.equals(cd)) {
            flds = new Fld[] { f("生徒番号"), f("文章")};
            
            final StringBuffer stb = new StringBuffer();
            stb.append(" select  ");
            stb.append("  t1.schregno, ");
            stb.append("  value(concat(t1.ent_reason, '。'), '') || ");
            stb.append("  value(concat(t1.ent_school, '。'), '') || ");
            stb.append("  value(t1.ent_addr, '') as jouhou ");
            stb.append(" from schreg_base_mst t1 ");
            stb.append(" where ");
            stb.append(" (t1.ent_div = '4' or t1.ent_div = '5') ");
            stb.append(" and (t1.ent_reason is not null or  t1.ent_school is not null or t1.ent_addr is not null) ");
            stb.append(" order by ");
            stb.append("  t1.schregno ");
            sql = stb.toString();

        
        } else if (_31_生徒部活委員.equals(cd)) {
            titleLines = new String[][] {{"部活動・委員会情報一覧"}};
            flds = new Fld[] { f("年度"), f("組番号"), dummy("クラス"), dummy("マーク"), dummy("出席番号"), f("氏名"), f("生徒番号"), f("部活コード１"), dummy("部活動１"), f("部活コード２"), dummy("部活動２"), f("部活コード３"), dummy("部活動３"), f("部活コード４"), dummy("部活動４"), f("委員コード１"), dummy("委員会１"), f("委員コード２"), dummy("委員会２"), f("委員コード３"), dummy("委員会３"), f("委員コード４"), dummy("委員会４"), f("生徒会コード１"), dummy("生徒会１"), f("生徒会コード２"), dummy("生徒会２"), f("生徒会コード３"), dummy("生徒会３"), f("生徒会コード４"), dummy("生徒会４")};

            final StringBuffer stb = new StringBuffer();
            stb.append(" with seitokai as ( ");
            stb.append("   select t1.year, t1.schregno, min(t1.seq) as minseq, t2.committeecd as cd, t2.committeename as name ");
            stb.append("   from schreg_committee_hist_dat t1 ");
            stb.append("   inner join committee_mst t2 on t2.committee_flg = t1.committee_flg ");
            stb.append("     and t2.committeecd = t1.committeecd ");
            stb.append("   where t2.committee_flg = '1' ");
            stb.append("   group by t1.year, t1.schregno, t2.committeecd, t2.committeename ");
            stb.append(" ), iin as ( ");
            stb.append("   select t1.year, t1.schregno, min(t1.seq) as minseq, t2.committeecd as cd, t2.committeename as name ");
            stb.append("   from schreg_committee_hist_dat t1 ");
            stb.append("   inner join committee_mst t2 on t2.committee_flg = t1.committee_flg ");
            stb.append("     and t2.committeecd = t1.committeecd ");
            stb.append("   where t2.committee_flg = '2' ");
            stb.append("   group by t1.year, t1.schregno, t2.committeecd, t2.committeename ");
            stb.append(" ), club as ( ");
            stb.append("   select t0.year, t1.schregno, min(t1.sdate) as sdate, t1.clubcd as cd, t2.clubname as name ");
            stb.append("   from school_mst t0, ");
            stb.append("   schreg_club_hist_dat t1  ");
            stb.append("   inner join club_mst t2 on t2.clubcd = t1.clubcd ");
            stb.append("   inner join (select distinct year, schregno from schreg_regd_dat ) t3 on t3.schregno = t1.schregno ");
            stb.append("   where t3.year = t0.year and t0.year between fiscalyear(t1.sdate) and fiscalyear(value(t1.edate, '9999-12-31')) ");
            stb.append("   group by t0.year, t1.schregno, t1.clubcd, t2.clubname ");
            stb.append(" ), total as ( ");
            stb.append("   select 'SEITOKAI' as flg, ");
            stb.append("    t1.year, t1.schregno, row_number() over(partition by t1.year, t1.schregno order by t1.minseq, cd) as no, t1.cd, t1.name ");
            stb.append("   from seitokai t1 ");
            stb.append("   union all ");
            stb.append("   select 'IIN' as flg, ");
            stb.append("    t1.year, t1.schregno, row_number() over(partition by t1.year, t1.schregno order by t1.minseq, cd) as no, t1.cd, t1.name ");
            stb.append("   from iin t1 ");
            stb.append("   union all ");
            stb.append("   select 'BUKATU' as flg, ");
            stb.append("    t1.year, t1.schregno, row_number() over(partition by t1.year, t1.schregno order by sdate, cd) as  no, t1.cd, t1.name ");
            stb.append("   from club t1 ");
            stb.append(" ), schregnos as ( ");
            stb.append("   select distinct t1.year, t1.schregno ");
            stb.append("   from total t1 ");
            stb.append(" )select  ");
            stb.append("  t1.year, t1.grade || t1.hr_class, t5.hr_name, ");
            stb.append("  '', ");
            stb.append("  t1.attendno, ");
            stb.append("  t4.name, ");
            stb.append("  t1.schregno, ");
            stb.append("  b1.cd, b1.name, ");
            stb.append("  b2.cd, b2.name, ");
            stb.append("  b3.cd, b3.name, ");
            stb.append("  b4.cd, b4.name, ");
            stb.append("  i1.cd, i1.name, ");
            stb.append("  i2.cd, i2.name, ");
            stb.append("  i3.cd, i3.name, ");
            stb.append("  i4.cd, i4.name, ");
            stb.append("  s1.cd, s1.name, ");
            stb.append("  s2.cd, s2.name, ");
            stb.append("  s3.cd, s3.name, ");
            stb.append("  s4.cd, s4.name ");
            stb.append(" from schreg_regd_dat t1 ");
            stb.append(" inner join (select schregno, year, max(semester) as semester from schreg_regd_dat group by schregno, year) t2 on t2.schregno = t1.schregno ");
            stb.append("    and t2.year = t1.year and t2.semester = t1.semester ");
            stb.append(" inner join schregnos t3 on t3.schregno = t1.schregno and t3.year = t1.year ");
            stb.append(" inner join schreg_base_mst t4 on t4.schregno = t1.schregno ");
            stb.append(" left join schreg_regd_hdat t5 on t5.year = t1.year and t5.semester = t1.semester and t5.grade = t1.grade and t5.hr_class = t1.hr_class ");
            stb.append(" left join total b1 on b1.flg = 'BUKATU' and b1.schregno = t1.schregno and b1.year = t1.year and b1.no = 1 ");
            stb.append(" left join total b2 on b2.flg = 'BUKATU' and b2.schregno = t1.schregno and b2.year = t1.year and b2.no = 2 ");
            stb.append(" left join total b3 on b3.flg = 'BUKATU' and b3.schregno = t1.schregno and b3.year = t1.year and b3.no = 3 ");
            stb.append(" left join total b4 on b4.flg = 'BUKATU' and b4.schregno = t1.schregno and b4.year = t1.year and b4.no = 4 ");
            stb.append(" left join total s1 on s1.flg = 'SEITOKAI' and s1.schregno = t1.schregno and s1.year = t1.year and s1.no = 1 ");
            stb.append(" left join total s2 on s2.flg = 'SEITOKAI' and s2.schregno = t1.schregno and s2.year = t1.year and s2.no = 2 ");
            stb.append(" left join total s3 on s3.flg = 'SEITOKAI' and s3.schregno = t1.schregno and s3.year = t1.year and s3.no = 3 ");
            stb.append(" left join total s4 on s4.flg = 'SEITOKAI' and s4.schregno = t1.schregno and s4.year = t1.year and s4.no = 4 ");
            stb.append(" left join total i1 on i1.flg = 'IIN' and i1.schregno = t1.schregno and i1.year = t1.year and i1.no = 1 ");
            stb.append(" left join total i2 on i2.flg = 'IIN' and i2.schregno = t1.schregno and i2.year = t1.year and i2.no = 2 ");
            stb.append(" left join total i3 on i3.flg = 'IIN' and i3.schregno = t1.schregno and i3.year = t1.year and i3.no = 3 ");
            stb.append(" left join total i4 on i4.flg = 'IIN' and i4.schregno = t1.schregno and i4.year = t1.year and i4.no = 4 ");
            stb.append(" order by t1.year, t1.grade || t1.hr_class, t1.attendno  ");
            sql = stb.toString();

        } else if (_32_クラス編成.equals(cd)) {
            titleLines = new String[][] {{"クラス情報一覧"}};
            flds = new Fld[] { f("年度"), f("不活動"), dummy("マーク"), f("組番号"), dummy("組名"), f("出席No."), f("生徒番号"), f("氏名"), dummy("前クラス")};
        
            
            final StringBuffer stb = new StringBuffer();
            stb.append(" with regd as ( ");
            stb.append(" select t1.*  ");
            stb.append(" from schreg_regd_dat t1 ");
            stb.append(" inner join ( ");
            stb.append(" select schregno, year, max(semester) as semester from schreg_regd_dat group by schregno, year ");
            stb.append(" ) t2 on t2.schregno = t1.schregno and t2.year = t1.year and t2.semester = t1.semester ");
            stb.append(" ) ");
            stb.append(" select ");
            stb.append("  t1.year, ");
            stb.append("  '', ");
            stb.append("  '', ");
            stb.append("  t1.grade || t1.hr_class, ");
            stb.append("  t3.hr_name, ");
            stb.append("  t1.attendno, ");
            stb.append("  t1.schregno, ");
            stb.append("  t4.name, ");
            stb.append("  t6.hr_name  ");
            stb.append(" from regd t1 ");
            stb.append(" inner join schreg_regd_hdat t3 on t3.year = t1.year and t3.semester = t1.semester and t3.grade = t1.grade and t3.hr_class = t1.hr_class ");
            stb.append(" inner join schreg_base_mst t4 on t4.schregno = t1.schregno ");
            stb.append(" left join regd t5 on t5.schregno  = t1.schregno and int(t5.year) = int(t1.year) - 1 ");
            stb.append(" left join schreg_regd_hdat t6 on t6.year = t5.year and t6.semester = t5.semester and t6.grade = t5.grade and t6.hr_class = t5.hr_class ");
            stb.append(" order by ");
            stb.append(" t1.year, ");
            stb.append(" t1.grade, t1.hr_class, t1.attendno ");
            sql = stb.toString();

        } else if (_33_異動情報D.equals(cd)) {
            flds = new Fld[] { f("生徒番号"), f("年度"), f("異動種類"), f("異動日"), f("備考")};
            
            final StringBuffer stb = new StringBuffer();
            stb.append(" select schregno, fiscalyear(grd_date), t2.name1, grd_date,  ");
            stb.append("  concat(grd_reason, '。') || concat(grd_school, '。') || value(grd_addr, '') ");
            stb.append(" from schreg_base_mst t1 ");
            stb.append(" inner join name_mst t2 on t2.namecd1 = 'A003' ");
            stb.append("  and t2.namecd2 = t1.grd_div ");
            stb.append(" where grd_div = '2' or grd_div = '3' ");
            stb.append(" order by schregno ");
            sql = stb.toString();

        } else if (_34_休学履歴D.equals(cd)) {
            flds = new Fld[] { f("生徒番号"), f("休学開始"), f("休学終了"), f("備考")};
            
            final StringBuffer stb = new StringBuffer();
            stb.append(" select t1.schregno, transfer_sdate, transfer_edate,  ");
            stb.append("  value(concat(transferreason, '。'), '') || ");
            stb.append("  value(concat(transferplace, '。'), '') || ");
            stb.append("  value(concat(transferaddr, '。'), '') as remark ");
            stb.append("  from schreg_transfer_dat t1 ");
            stb.append("  inner join schreg_base_mst t2 on t2.schregno = t1.schregno ");
            stb.append("  where transfercd = '2' ");
            stb.append(" order by t1.schregno, transfer_sdate ");
            sql = stb.toString();

        } else if (_35_留学履歴D.equals(cd)) {
            flds = new Fld[] { f("生徒番号"), f("留学開始"), f("留学終了"), f("備考")};
        
            final StringBuffer stb = new StringBuffer();
            stb.append(" select t1.schregno, transfer_sdate, transfer_edate, value(transferplace, '') || value(transferaddr, '') ");
            stb.append(" from schreg_transfer_dat t1 ");
            stb.append(" inner join schreg_base_mst t2 on t2.schregno = t1.schregno ");
            stb.append(" where transfercd = '1' ");
            stb.append(" order by t1.schregno, transfer_sdate ");
            sql = stb.toString();

        } else if (_36_証明書発行台帳D.equals(cd)) {
            flds = new Fld[] { f("発行日"), f("発行番号"), f("様式名称"), f("生徒番号"), f("年度"), f("証明日"), f("発行職員番号")};
        
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH CERTIF_DATA AS( ");
            stb.append("     SELECT  T1.YEAR, T1.CERTIF_INDEX  ");
            stb.append("        ,INT(T2.REMARK1) AS CERTIF_NO  ");
            stb.append("            ,T1.CERTIF_KINDCD  ");
            stb.append("            ,T1.ISSUEDATE  ");
            stb.append("            ,T1.ISSUERNAME ");
            stb.append("            ,T1.APPLYDATE ");
            stb.append("            ,T1.SCHREGNO  ");
            stb.append("            ,T1.GRADUATE_FLG  ");
            stb.append("           ,value(t2.registercd, T1.REGISTERCD) as REGISTERCD ");
            stb.append("     FROM    CERTIF_ISSUE_DAT T1  ");
            stb.append("         INNER JOIN CERTIF_DETAIL_EACHTYPE_DAT T2  ");
            stb.append("               ON T2.YEAR = T1.YEAR  ");
            stb.append("              AND T2.CERTIF_INDEX = T1.CERTIF_INDEX  ");
            stb.append("              AND T2.TYPE = '1'  ");
            stb.append("     WHERE   T1.ISSUECD = '1'  ");
            stb.append("     union all ");
            stb.append("     SELECT  T1.YEAR, T1.CERTIF_INDEX  ");
            stb.append("        ,T1.CERTIF_NO  ");
            stb.append("            ,T1.CERTIF_KINDCD  ");
            stb.append("            ,T1.ISSUEDATE  ");
            stb.append("            ,T1.ISSUERNAME ");
            stb.append("            ,T1.APPLYDATE ");
            stb.append("            ,T1.SCHREGNO  ");
            stb.append("            ,T1.GRADUATE_FLG  ");
            stb.append("           ,T1.REGISTERCD ");
            stb.append("     FROM    CERTIF_ISSUE_DAT T1  ");
            stb.append("         left JOIN CERTIF_DETAIL_EACHTYPE_DAT T2  ");
            stb.append("               ON T2.YEAR = T1.YEAR  ");
            stb.append("              AND T2.CERTIF_INDEX = T1.CERTIF_INDEX  ");
            stb.append("     WHERE   T1.ISSUECD = '1' and t2.year is null  ");
            stb.append(" ), CERTIF_DATA2 AS( ");
            stb.append("     SELECT  T1.YEAR, T1.CERTIF_INDEX  ");
            stb.append("        ,T1.CERTIF_NO  ");
            stb.append("            ,T1.CERTIF_KINDCD  ");
            stb.append("            ,T1.ISSUEDATE  ");
            stb.append("            ,value(MIN(T2.STAFFCD), max(T1.registercd)) AS STAFFCD ");
            stb.append("            ,T1.APPLYDATE ");
            stb.append("            ,T1.SCHREGNO  ");
            stb.append("            ,T1.GRADUATE_FLG  ");
            stb.append("     FROM    CERTIF_DATA T1  ");
            stb.append("     left join staff_mst t2 on t2.staffname = t1.issuername ");
            stb.append("     group by T1.YEAR, T1.CERTIF_INDEX  ");
            stb.append("        ,T1.CERTIF_NO  ");
            stb.append("            ,T1.CERTIF_KINDCD  ");
            stb.append("            ,T1.ISSUEDATE  ");
            stb.append("            ,T1.APPLYDATE ");
            stb.append("            ,T1.SCHREGNO  ");
            stb.append("              , t1.issuername ");
            stb.append("            ,T1.GRADUATE_FLG  ");
            stb.append(" ), SCHREG_DATA AS( ");
            stb.append("        SELECT  SCHREGNO, GRD_DATE ");
            stb.append("        FROM SCHREG_BASE_MST W1  ");
            stb.append("        WHERE   EXISTS(SELECT 'X' FROM CERTIF_DATA W2 WHERE W1.SCHREGNO = W2.SCHREGNO GROUP BY W2.SCHREGNO)  ");
            stb.append("    UNION  ");
            stb.append("    SELECT  SCHREGNO, GRD_DATE ");
            stb.append("     FROM GRD_BASE_MST W1  ");
            stb.append("    WHERE   EXISTS(SELECT 'X' FROM CERTIF_DATA W2 WHERE W1.SCHREGNO = W2.SCHREGNO GROUP BY W2.SCHREGNO)  ");
            stb.append(" ), DATA AS( ");
            stb.append(" SELECT  ROW_NUMBER() OVER (ORDER BY W1.ISSUEDATE, W1.CERTIF_NO) AS NUMBER, ");
            stb.append(" w1.year, ");
            stb.append("             W1.CERTIF_INDEX            ,W1.CERTIF_NO           ,W1.CERTIF_KINDCD            ,W1.ISSUEDATE            ,W1.STAFFCD        ,W1.APPLYDATE            ,W1.SCHREGNO            ,W1.GRADUATE_FLG  ");
            stb.append("     FROM    CERTIF_DATA2 W1  ");
            stb.append("     WHERE   EXISTS(SELECT 'X' FROM SCHREG_DATA W2 WHERE W1.SCHREGNO = W2.SCHREGNO GROUP BY W2.SCHREGNO)  ");
            stb.append(" ) SELECT distinct ");
            stb.append("        T1.ISSUEDATE        , T1.CERTIF_NO            , T3.KINDNAME        ,T1.SCHREGNO        , T1.YEAR       ,T1.APPLYDATE       ,T1.STAFFCD ");
            stb.append(" FROM    DATA T1  ");
            stb.append(" INNER JOIN SCHREG_DATA T2 ON T2.SCHREGNO = T1.SCHREGNO    ");
            stb.append(" LEFT  JOIN CERTIF_KIND_MST T3 ON T3.CERTIF_KINDCD = T1.CERTIF_KINDCD  ");
            stb.append(" order by ");
            stb.append("  t1.issuedate, t1.certif_no, t1.schregno  ");
            sql = stb.toString();

        } else if (_37_評価基準.equals(cd)) {
            flds = new Fld[] { f("評価"), f("点数上限")};

        } else if (_38_評定基準.equals(cd)) {
            flds = new Fld[] { f("評定"), f("評価上限")};

            final StringBuffer stb = new StringBuffer();
            stb.append(" select ");
            stb.append("   t1.assesslevel, ");
            stb.append("   t1.assesshigh ");
            stb.append(" from assess_mst t1 ");
            stb.append(" where ");
            stb.append("   t1.assesscd = '3' ");
            stb.append(" order by t1.assesslevel ");
            sql = stb.toString();

        } else if (_39_考査.equals(cd)) {
            titleLines = new String[][] {{"考査情報一覧"}, {}};
            flds = new Fld[] { f("年度"), f("考査種類コード"), dummy("考査種類"), f("学期コード"), dummy("学期"), f("考査コード"), f("考査名称", ZEN, 10), f("略称", ZEN, 5), f("出欠集計期間（開始）"), f("出欠集計期間（終了）"), f("満点")};
            createTableList.add(IkouUtil.tori_testcd_mst);

            final StringBuffer stb = new StringBuffer();
            stb.append(" with t_perf as (  ");
            stb.append(" select ");
            stb.append("  year, semester,  testkindcd, testitemcd, max(perfect) as perfect ");
            stb.append(" from perfect_record_dat ");
            stb.append(" group by year, semester, testkindcd, testitemcd ");
            stb.append(" ), mock_perfect as ( ");
            stb.append(" select t1.year, t1.mockcd, max(value(t2.perfect, 100)) as perfect ");
            stb.append(" from mock_dat t1 ");
            stb.append(" left join mock_perfect_dat t2 on t2.year = t1.year and t2.mock_subclass_cd = t1.mock_subclass_cd ");
            stb.append(" group by t1.year, t1.mockcd ");
            stb.append(" ), main as ( ");
            stb.append(" select  ");
            stb.append("  t1.year, ");
            stb.append("  1 as div, ");
            stb.append("  case when t1.testkindcd = '99' then '0' else '1' end as kind, ");
            stb.append("  case when t1.semester = '9' and t1.testkindcd = '99' and t1.testitemcd = '00' then '学年評定' ");
            stb.append("       when t1.testkindcd = '99' then '学期成績' else '定期考査' end as itemname, ");
            stb.append("  t1.semester, ");
            stb.append("  t2.tori_testcd, ");
            stb.append("  t1.testitemname, ");
            stb.append("  t1.testitemname as testitemname2, ");
            stb.append("  t4.sdate, ");
            stb.append("  t4.edate, ");
            stb.append("  value(t5.perfect, 100) as perfect ");
            stb.append(" from testitem_mst_countflg_new t1 ");
            stb.append(" inner join tori_testcd_mst t2 on t2.year = t1.year and t2.semester = t1.semester and t2.testkindcd = t1.testkindcd and t2.testitemcd = t1.testitemcd ");
            stb.append(" left join semester_detail_mst t4 on t4.year = t1.year and t4.semester = t1.semester and t4.semester_detail = t1.semester_detail ");
            stb.append(" left join t_perf t5 on t5.year = t1.year and t5.semester = t1.semester and t5.testitemcd = t1.testitemcd and t5.testkindcd = t1.testkindcd ");
            stb.append(" union all ");
            stb.append(" select distinct ");
            stb.append("   t2.year, ");
            stb.append("   2 as div, ");
            stb.append("   '0' as kind, ");
            stb.append("   'その他考査' as itemname, ");
            stb.append("   t3.semester, ");
            stb.append("   t3.seq as tori_testcd, ");
            stb.append("   t3.name1 as testitemname, ");
            stb.append("   t3.name2 as testitemname2, ");
            stb.append("   cast(null as date) as sdate, ");
            stb.append("   cast(null as date) as edate, ");
            stb.append("   t4.perfect ");
            stb.append(" from mock_mst t1 ");
            stb.append(" inner join mock_dat t2 on t2.mockcd = t1.mockcd ");
            stb.append(" inner join (select distinct year, schregno from schreg_regd_dat) sregd on sregd.year = t2.year and sregd.schregno = t2.schregno ");
            stb.append(" left join tori_mock_mst t3 on t3.year = t2.year and t3.mockcd = t1.mockcd ");
            stb.append(" left join mock_perfect t4 on t4.year = t2.year and t4.mockcd = t1.mockcd ");
            stb.append(" ) select ");
            stb.append("  t1.year, ");
            stb.append("  t1.kind, ");
            stb.append("  t1.itemname, ");
            stb.append("  t1.semester, ");
            stb.append("  t3.semestername, ");
            stb.append("  t1.tori_testcd, ");
            stb.append("  t1.testitemname, ");
            stb.append("  t1.testitemname2, ");
            stb.append("  t1.sdate, ");
            stb.append("  t1.edate, ");
            stb.append("  t1.perfect ");
            stb.append(" from main t1 ");
            stb.append(" left join semester_mst t3 on t3.year = t1.year and t3.semester = t1.semester ");
            stb.append(" order by ");
            stb.append("  t1.year, t1.div, t1.tori_testcd ");
            sql = stb.toString();

        } else if (_40_成績点数.equals(cd)) {
            titleLines = new String[][] {{"試験", "{TESTCD}", "{TESTNAME}"}, {}};
            flds = new Fld[] { f("異動"), f("年度"), f("講座番号"), f("講座名"), f("生徒番号"), f("出席番号"), f("受講番号"), f("氏名"), f("点数"), f("実施状況"), f("評価")};
            createTableList.add(IkouUtil.tori_testcd_mst);
            outputFmt = new OutputFmt40(_param);

        } else if (_41_成績評定.equals(cd)) {
            titleLines = new String[][] {{"試験", "HYOUTEI", "評定"}, {}};
            flds = new Fld[] { f("異動"), f("年度"), f("講座番号"), f("講座名"), f("生徒番号"), f("出席番号"), f("受講番号"), f("氏名"), f("評定"), f("10段階評価")};
            createTableList.add(IkouUtil.tori_testcd_mst);

            final StringBuffer stb = new StringBuffer();
            stb.append(" with chair_std as ( ");
            stb.append(" select ");
            stb.append("   t1.year, ");
            stb.append("   t2.schregno, ");
            stb.append("   min(t1.chaircd) as chaircd, ");
            stb.append("   t1.classcd, t1.school_kind, t1.curriculum_cd, t1.subclasscd "); // 内部
            stb.append(" from chair_dat t1 ");
            stb.append(" inner join chair_std_dat t2 on t2.year = t1.year and t2.semester = t1.semester and t2.chaircd = t1.chaircd ");
            stb.append(" group by ");
            stb.append("   t1.year, ");
            stb.append("   t2.schregno, ");
            stb.append("   t1.classcd, t1.school_kind, t1.curriculum_cd, t1.subclasscd ");
            stb.append(" )select ");
            stb.append("   '' as idou, ");
            stb.append("   t1.year, ");
            stb.append("   t3.chaircd, ");
            stb.append("   '' as chairname, ");
            stb.append("   t1.schregno, ");
            stb.append("   '' as attendno, ");
            stb.append("   '' as jukou_bangou, ");
            stb.append("   '' as schreg_name, ");
            stb.append("   t1.value, ");
            stb.append("   t1.score ");
            stb.append(" from record_score_dat t1 ");
            stb.append(" inner join chair_std t3 on t3.year = t1.year and t3.schregno = t1.schregno and t3.classcd = t1.classcd and t3.school_kind = t1.school_kind and t3.curriculum_cd = t1.curriculum_cd and t3.subclasscd = t1.subclasscd ");
            stb.append(" inner join schreg_base_mst t4 on t4.schregno = t1.schregno ");
            stb.append(" where ");
            stb.append(" t1.value is not null and ");
            stb.append(" t1.semester = '9' and t1.testkindcd = '99' and t1.testitemcd = '00' ");
            stb.append(" order by ");
            stb.append(" t1.year, t3.chaircd, t1.schregno ");
            sql = stb.toString();

        } else if (_42_通知表所見.equals(cd)) {
            titleLines = new String[][] {{"{YEAR}年度"}, {}};
            flds = new Fld[] { dummy("クラス"), dummy("マーク"), dummy("出席番号"), f("氏名"), f("生徒番号"), dummy("区分"), f("通信欄")};
            outputFmt = new OutputFmtYear(" select distinct t1.year from hreportremark_dat t1 order by t1.year ");

            final StringBuffer stb = new StringBuffer();
            stb.append(" select ");
            stb.append("   '' as hr_class, ");
            stb.append("   '' as mark, ");
            stb.append("   '' as attendno, ");
            stb.append("   '' as name, ");
            stb.append("   t1.schregno, ");
            stb.append("   '' as kbn, ");
            stb.append("   t1.totalstudytime ");
            stb.append(" from hreportremark_dat t1 ");
            stb.append(" inner join schreg_base_mst t2 on t2.schregno = t1.schregno ");
            stb.append(" where ");
            stb.append(" t1.year = ? ");
            stb.append(" and t1.semester = '9' and t1.totalstudytime is not null ");
            stb.append(" order by ");
            stb.append("   t1.year, ");
            stb.append("   t1.schregno ");
            sql = stb.toString();

        } else if (_43_年間成績情報.equals(cd)) {
            titleLines = new String[][] {{"過去の履修情報"}};
            flds = new Fld[] { f("年度"), f("生徒番合"), f("氏名"), dummy("学年"), f("履修科目番号"), f("科目名"), f("評定"), f("修得単位"), f("未履修単位"), f("履修単位"), f("備考", ZEN, 100), f("入学前区分"), f("追認年度")};
            outputFmt = new OutputFmt43();

        } else if (_44_増加単位.equals(cd)) {
            titleLines = new String[][] {{}};
            flds = new Fld[] { f("年度"), f("種別"), f("科目番号"), f("科目名"), f("生徒番号"), f("出席番号"), f("氏名"), f("増加単位数"), f("認定年度"), f("認定期"), f("認定科目番号"), f("認定科目名"), f("前籍"), f("認定日"), f("主催者コード"), f("主催者"), f("取得日"), f("備考")};
            createTableList.add(IkouUtil.tori_promoter_mst);

            final StringBuffer stb = new StringBuffer();
            stb.append(" select ");
            stb.append("   t1.year as distyear, ");
            stb.append("   case t1.condition_div ");
            stb.append("   when '1' then '増加単位認定' ");
            stb.append("   when '2' then '学校外' ");
            stb.append("   when '3' then '高卒認定' ");
            stb.append("   end as shubetsu, ");
            stb.append("   value(hnkn.tgt_subclasscd, t1.subclasscd) as subclasscd, ");
            stb.append("   case t1.condition_div ");
            stb.append("   when '1' then nmh305.name1 ");
            stb.append("   when '2' then nmh306.name1 ");
            stb.append("   when '3' then t1.contents ");
            stb.append("   end as subclassname, ");
            stb.append("   t1.schregno, ");
            stb.append("   '' as attendno, ");
            stb.append("   '' as student_name, ");
            stb.append("   t1.credits, ");
            stb.append("   t1.year, ");
            stb.append("   '0' as ninteiki, ");
            stb.append("   value(hnkn.tgt_subclasscd, t1.subclasscd) as subclasscd2, ");
            stb.append("   '' as subclassname2, ");
            stb.append("   case when value((select min(md.year) from schreg_regd_dat md where md.schregno = t1.schregno), '0000') > fiscalyear(regddate) then '前籍' end as zenseki, ");
            stb.append("   cast(null as date) as nintei_date, ");
            stb.append("   '' as promoter, ");
            stb.append("   '' as promotername, ");
            stb.append("   t1.regddate, ");
            stb.append("   t1.remark ");
            stb.append(" from schreg_qualified_dat t1 ");
            stb.append(" inner join schreg_base_mst t3 on t3.schregno = t1.schregno ");
            stb.append(" left join tori_subclass_henkan hnkn on hnkn.classcd = t1.classcd and hnkn.school_kind = t1.school_kind and hnkn.curriculum_cd = t1.curriculum_cd and hnkn.subclasscd = t1.subclasscd ");
            stb.append(" left join name_mst nmh305 on nmh305.namecd1 = 'H305' and nmh305.namecd2 = t1.contents ");
            stb.append(" left join name_mst nmh306 on nmh306.namecd1 = 'H306' and nmh306.namecd2 = t1.contents ");
            stb.append(" union all ");
            stb.append(" select ");
            stb.append("   t1.year as distyear, ");
            stb.append("   '' as shubetsu, ");
            stb.append("   '' as subclasscd, ");
            stb.append("   case when t1.contents is not null then t1.contents else ");
            stb.append("   value(t2.qualified_name, '') || case when 0 < locate(value(nmh312.name1,''), value(t2.qualified_name,'')) then '' else value(nmh312.name1,'') end ");
            stb.append("   end as subclassname, ");
            stb.append("   t1.schregno, ");
            stb.append("   '' as attendno, ");
            stb.append("   '' as student_name, ");
            stb.append("   cast(null as smallint) as credits, ");
            stb.append("   t1.year, ");
            stb.append("   '0' as ninteiki, ");
            stb.append("   '' as subclasscd2, ");
            stb.append("   '' as subclassname2, ");
            stb.append("   case when value((select min(md.year) from schreg_regd_dat md where md.schregno = t1.schregno), '0000') > fiscalyear(regddate) then '前籍' end as zenseki, ");
            stb.append("   cast(null as date) as nintei_date, ");
            stb.append("   prom.promoter_cd as promoter, ");
            stb.append("   t2.promoter as promotername, ");
            stb.append("   t1.regddate, ");
            stb.append("   t1.remark ");
            stb.append(" from schreg_qualified_hobby_dat t1 ");
            stb.append(" left join qualified_mst t2 on t2.qualified_cd = t1.qualified_cd ");
            stb.append(" left join name_mst nmh312 on nmh312.namecd1 = 'H312' and nmh312.namecd2 = t1.rank "); // rank
            stb.append(" left join tori_promoter_mst prom on prom.promoter = t2.promoter ");
            stb.append(" order by");
            stb.append("   distyear, ");
            stb.append("   shubetsu, ");
            stb.append("   subclasscd, ");
            stb.append("   schregno ");
            sql = stb.toString();

        } else if (_45_欠課種類.equals(cd)) {
            flds = new Fld[] { f("コード"), f("欠課種類"), f("略称"), f("備考"), f("並び順")};

            final StringBuffer stb = new StringBuffer();
            stb.append(" select ");
            stb.append("   T1.namecd2, ");
            stb.append("   name1, ");
            stb.append("   abbv1, ");
            stb.append("   name1, ");
            stb.append("   namespare2 ");
            stb.append(" from NAME_MST T1 ");
            stb.append(" inner join (select distinct namecd2 from name_ydat where namecd1 = 'C001') T2 on t2.namecd2 = t1.namecd2 ");
            stb.append(" where T1.namecd1 = 'C001' ");
            stb.append(" and T1.namecd2 <> '27' ");
            stb.append(" and namespare1 is null ");
            stb.append(" and (name1 is not null or abbv1 is not null) ");
            stb.append(" order by namespare2, int(T1.namecd2) ");
            sql = stb.toString();

        } else if (_46_欠席種類.equals(cd)) {
            flds = new Fld[] { f("コード"), f("欠席種類"), f("略称"), f("備考"), f("並び順")};

            final StringBuffer stb = new StringBuffer();
            stb.append(" select ");
            stb.append("   T1.namecd2, ");
            stb.append("   name1, ");
            stb.append("   abbv1, ");
            stb.append("   name1, ");
            stb.append("   namespare2 ");
            stb.append(" from NAME_MST T1 ");
            stb.append(" inner join (select distinct namecd2 from name_ydat where namecd1 = 'C001') T2 on t2.namecd2 = t1.namecd2 ");
            stb.append(" where T1.namecd1 = 'C001' ");
            stb.append(" and T1.namecd2 <> '27' and T1.namecd2 <> '14' ");
            stb.append(" and namespare1 is null ");
            stb.append(" order by namespare2, int(T1.namecd2) ");
            sql = stb.toString();

        } else if (_47_集計出欠期間D.equals(cd)) {
            flds = new Fld[] { f("年度"), f("0講座1クラス"), f("コード"), f("期間名称"), f("開始日"), f("終了日")};
            createTableList.add(IkouUtil.tori_attendrange_dat);

            final StringBuffer stb = new StringBuffer();
            stb.append(" select year, div, semester_detail, semestername, sdate, edate ");
            stb.append(" from tori_attendrange_dat ");
            stb.append(" order by year, div, semester_detail ");
            sql = stb.toString();

        } else if (_48_集計値講座.equals(cd)) {
            titleLines = new String[][] {{"{YEAR}年度"}, {"講座", "{CHAIRCD}", "{CHAIRNAME}"}, {"期間", "{SEMESTER_DETAIL}", "{SEMESTERNAME}"}};
            flds = new Fld[] { f("異動"), f("マーク"), f("受講№"), f("生徒番号"), f("出席番号"), f("氏名"), f("欠課"), f("遅刻"), f("早退"), f("公欠"), f("出席停止"), f("忌引き"), f("実施時数")};
            createTableList.add(IkouUtil.tori_attendrange_dat);
            outputFmt = new OutputFmt48();

        } else if (_49_集計値クラス.equals(cd)) {
            titleLines = new String[][] {{"{YEAR}年度"}, {"クラス", "{HR_CD}", "{HR_NAME}"}, {"期間", "{SEMESTER_DETAIL}", "{SEMESTERNAME}"}, {"授業日数", "{JUGYOU_NISSU}"}};
            flds = new Fld[] { f("異動"), f("マーク"), f("出席№"), f("生徒番号"), f("出席番号"), f("氏名"), f("欠席"), f("遅刻"), f("早退"), f("公欠"), f("出席停止"), f("忌引等"), f("遅刻・早退"), f("留学"), f("授業日数")};
            createTableList.add(IkouUtil.tori_attendrange_dat);
            outputFmt = new OutputFmt49();

        } else if (_50_日々講座出欠.equals(cd)) {
            titleLines = new String[][] {{"年度", "{YEAR}"}, {"講座番号", "{CHAIRCD}", "{CHAIRNAME}"}, {"【凡例】"}, {"講座実施", "済：実施済"}, {"欠課区分"}, {}, {"欠席理由：", "欠課区分の後に「/理由」  削除：/*"}, {}};
            flds = null;
            outputFmt = new OutputFmt50();

        } else if (_51_日々クラス出欠.equals(cd)) {
            titleLines = new String[][] {{"年度", "{YEAR}"}, {"組番号", "{HR_CD}", "{HR_NAME}"}, {"【凡例】"}, {"クラス出欠", "済：登録済"}, {"欠席区分"}, {}, {}, {"欠席理由：", "欠課区分の後に「/理由」  削除：/*"}, {}};
            flds = null;
            outputFmt = new OutputFmt51();

        } else if (_52_特活時間数D.equals(cd)) {
            flds = new Fld[] { f("生徒番号"), f("年度"), f("学期"), f("期間コード"), f("特活認定時数"), f("特活時間数"), f("1:前籍時"), f("うちSHR出席時数"), f("うちSHR実施時間数"), f("SHR欠課時数"), f("うちLHR出席時数"), f("うちLHR実施時数"), f("LHR欠課時数"), f("開始"), f("終了")};
            createTableList.add(IkouUtil.tori_attendrange_dat);
            outputFmt = new OutputFmt52();

        } else if (_53_特活認定数D.equals(cd)) {
            flds = new Fld[] { f("生徒番号"), f("年度"), f("学期"), f("認定有無"), f("1：前籍時"), f("認定数")};

        } else if (_54_年間出欠情報.equals(cd)) {
            titleLines = new String[][] {{"生徒出席情報"}};
            flds = new Fld[] { f("年度"), f("生徒番合"), f("氏名"), dummy("学年"), f("授業日数"), f("出停・忌引等の日数"), f("留学中の授業日数"), f("出席すべき日数"), f("欠席日数"), f("出席日数"), f("公欠日数"), f("遅刻数"), f("早退数"), f("入学前区分")};
            
            final StringBuffer stb = new StringBuffer();
            stb.append(" with schreg_late_early as ( ");
            stb.append(" select ");
            stb.append("   year, ");
            stb.append("   schregno, ");
            stb.append("   sum(late) as late, ");
            stb.append("   sum(early) as early ");
            stb.append("  from attend_semes_dat ");
            stb.append("  group by year, schregno ");
            stb.append(" ) ");
            stb.append(" select ");
            stb.append("  t1.year, ");
            stb.append("  t1.schregno, ");
            stb.append("  t4.name, ");
            stb.append("  t1.annual, ");
            stb.append("  value(classdays, 0) - case when value(t5.sem_offdays, '0') = '1' then ");
            stb.append("         value(offdays, 0) else 0 end as attend_1, ");
            stb.append("  value(suspend, 0) + value(mourning, 0) as susp_mour, ");
            stb.append("  value(abroad, 0) as abroad, ");
            stb.append("  value(requirepresent, 0) + case when value(t5.sem_offdays, '0') = '1' then value(offdays, 0) else 0 end as requirepresent, ");
            stb.append("  value(sick, 0) + value(accidentnotice, 0) + value(noaccidentnotice, 0) + case when value(t5.sem_offdays, '0') = '1' then value(offdays, 0) else 0 end as attend_6, ");
            stb.append("  value(present, 0) as present, ");
            stb.append("  value(absent, 0) as absent, ");
            stb.append("  value(t6.late, 0) as late, ");
            stb.append("  value(t6.early, 0) as early, ");
            stb.append("  case when schoolcd = '1' then '○' end as nyugakumae ");
            stb.append(" from schreg_attendrec_dat t1 ");
            stb.append(" inner join schreg_base_mst t4 on t4.schregno = t1.schregno ");
            stb.append(" left join school_mst t5 on t5.year = t1.year ");
            stb.append(" left join schreg_late_early t6 on t6.year = t1.year and t6.schregno = t1.schregno ");
            stb.append(" order by t1.year, t1.schregno ");
            sql = stb.toString();

        } else if (_55_学期別時間割名D.equals(cd)) {
            flds = new Fld[] { f("コード"), f("時間割名称")};
            
            outputFmt = new OutputFmtYear(" select distinct year from sch_ptrn_hdat order by year ");
            outputFmt._outputExcellSplitSheet = true;
            
            final StringBuffer stb = new StringBuffer();
            stb.append(" select t1.bscseq, t1.title ");
            stb.append(" from sch_ptrn_hdat t1 ");
            stb.append(" where year = ? ");
            stb.append(" order by t1.bscseq ");
            sql = stb.toString();
            
        } else if (_56_時間割.equals(cd)) {
            titleLines = new String[][] {{"開講情報"}};
            flds = new Fld[] { f("講座番号"), dummy("講座名"), f("曜日"), f("校時"), f("担当職員"), f("使用教室")};
            
            outputFmt = new OutputFmt56();
            
        } else if (_57_年度休校日.equals(cd)) {
            titleLines = new String[][] {{"休校日情報"}, {"年度", "{NENDO}"}};
            flds = new Fld[] { f("休校日区分"), dummy("休校日区分名"), f("登録日"), dummy("曜日"), f("名称", ZEN, 30), f("年次休校(1年)"), f("年次休校(2年)"), f("年次休校(3年)"), f("年次休校(4年)")};
        
            final StringBuffer stb = new StringBuffer();
            stb.append(" select  ");
            stb.append("  '1' as kbn, ");
            stb.append("  '' as kbn_name, ");
            stb.append("  HOLIDAY, ");
            stb.append("  '' as youbi, ");
            stb.append("  REMARK, ");
            stb.append("  '○' as nen1, ");
            stb.append("  '○' as nen2, ");
            stb.append("  '○' as nen3, ");
            stb.append("  '○' as nen4 ");
            stb.append(" from  ");
            stb.append("  HOLIDAY_MST  ");
            stb.append(" order by  ");
            stb.append(" HOLIDAY  ");
            sql = stb.toString();

        } else if (_58_授業実施日予定日.equals(cd)) {
            titleLines = new String[][] {{}, {"{YEAR}年度", "{MONTH}月", "講座予実績情報"}};
            flds = new Fld[] { f("日付"), dummy("曜日"), f("校時"), f("実施区分"), f("講座番号"), dummy("講座名"), dummy("主担当"), f("教室"), f("特活実施時数")};
        
            outputFmt = new OutputFmt58();
            
        } else if (_59_受講生.equals(cd)) {
            titleLines = new String[][] {{"受講者情報"}};
            flds = new Fld[] { f("講座番号"), dummy("講座名"), dummy("マーク"), f("生徒番号"), f("氏名"), f("単位数")};
            
            outputFmt = new OutputFmtYear(" select distinct t1.year from chair_std_dat t1 order by year ");
            outputFmt._outputExcellSplitSheet = true; // エクセル出力は年度ごとにシート分割 
            
            final StringBuffer stb = new StringBuffer();
            stb.append(" with subclass_credits0 as ( ");
            stb.append(" select distinct ");
            stb.append("     t1.year, ");
            stb.append("     t1.schregno, ");
            stb.append("     t2.classcd || '-' || t2.school_kind || '-' || t2.curriculum_cd || '-' || t2.subclasscd as subclasscd, ");
            stb.append("     cre.credits ");
            stb.append(" from chair_std_dat t1 ");
            stb.append(" inner join chair_dat t2 on t2.year = t1.year and t2.semester = t1.semester and t2.chaircd = t1.chaircd ");
            stb.append(" left join schreg_regd_dat regd on regd.year = t1.year and regd.semester = t1.semester and regd.schregno = t1.schregno ");
            stb.append(" left join credit_mst cre on cre.year = regd.year and cre.coursecd = regd.coursecd and cre.grade = regd.grade ");
            stb.append("     and cre.majorcd = regd.majorcd and cre.coursecode = regd.coursecode ");
            stb.append("     and cre.classcd = t2.classcd and cre.school_kind = t2.school_kind and cre.curriculum_cd = t2.curriculum_cd and cre.subclasscd = t2.subclasscd ");
            stb.append(" ), subclass_credits as ( ");
            stb.append(" select ");
            stb.append("     t1.year, ");
            stb.append("     t1.schregno, ");
            stb.append("     t1.subclasscd, ");
            stb.append("     t1.credits ");
            stb.append(" from subclass_credits0 t1 ");
            stb.append(" left join (select distinct year, combined_classcd || '-' || combined_school_kind || '-' || combined_curriculum_cd || '-' || combined_subclasscd as subclasscd ");
            stb.append("            from subclass_replace_combined_dat ");
            stb.append("            where calculate_credit_flg = '2' ");
            stb.append("           ) l1 on l1.year = t1.year and l1.subclasscd = t1.subclasscd ");
            stb.append(" where l1.year is null ");
            stb.append(" union all ");
            stb.append(" select ");
            stb.append("     t1.year, ");
            stb.append("     t1.schregno, ");
            stb.append("     t1.subclasscd, ");
            stb.append("     sum(t2.credits) as credits ");
            stb.append(" from subclass_credits0 t1 ");
            stb.append(" inner join subclass_replace_combined_dat comb on comb.year = t1.year ");
            stb.append("     and comb.combined_classcd || '-' || comb.combined_school_kind || '-' || comb.combined_curriculum_cd || '-' || comb.combined_subclasscd = t1.subclasscd ");
            stb.append("     and calculate_credit_flg = '2' ");
            stb.append(" inner join subclass_credits0 t2 on t2.year = t1.year ");
            stb.append("     and t2.schregno = t1.schregno ");
            stb.append("     and comb.attend_classcd || '-' || comb.attend_school_kind || '-' || comb.attend_curriculum_cd || '-' || comb.attend_subclasscd = t2.subclasscd ");
            stb.append(" group by t1.year, t1.schregno, t1.subclasscd ");
            stb.append(" ), tgt as ( ");
            stb.append(" select ");
            stb.append("     t1.year, ");
            stb.append("     min(t1.chaircd) as chaircd, ");
            stb.append("     t1.schregno, ");
            stb.append("     max(cre.credits) as credits ");
            stb.append(" from chair_std_dat t1 ");
            stb.append(" inner join chair_dat t2 on t2.year = t1.year and t2.semester = t1.semester and t2.chaircd = t1.chaircd ");
            stb.append(" left join tori_subclass_henkan hnkn on hnkn.classcd = t2.classcd and hnkn.school_kind = t2.school_kind and hnkn.curriculum_cd = t2.curriculum_cd and hnkn.subclasscd = t2.subclasscd ");
            stb.append(" left join subclass_credits cre on cre.year = t1.year and cre.schregno = t1.schregno ");
            stb.append("     and t2.classcd || '-' || t2.school_kind || '-' || t2.curriculum_cd || '-' || t2.subclasscd = cre.subclasscd ");
            stb.append(" where t1.year = ? ");
            stb.append(" group by t1.year, t1.schregno, value(hnkn.tgt_subclasscd, t2.subclasscd) ");
            stb.append(" ) ");
            stb.append(" select distinct ");
            stb.append("     t1.chaircd, ");
            stb.append("     '', ");
            stb.append("     '', ");
            stb.append("     t1.schregno, ");
            stb.append("     t3.name, ");
            stb.append("     t1.credits ");
            stb.append(" from tgt t1 ");
            stb.append(" inner join schreg_base_mst t3 on t3.schregno = t1.schregno ");
            stb.append(" order by t1.chaircd, t1.schregno ");
            sql = stb.toString();

        } else if (_60_文例情報.equals(cd)) {
            titleLines = new String[][] {{"所見文章例一覧", "", "{SCHREGNO}", "{NAME}"}};
            flds = new Fld[] { f("目次コード"), f("目次", ZEN, 15), f("文章コード"), f("文章", ZEN, 100)};
        } else if (_61_指導要録所見.equals(cd)) {
            titleLines = new String[][] {{"年度", "{YEAR}"}, {"帳票区分", "1", "1：指導要録 2：進学用調査書 3：就職用調査書"}, {"項目No"}};
//            flds = new Fld[] { dummy("区分"), dummy("出席番号"), f("生徒番号"), f("氏名"), f("年度"), f("総合的な学習・学習活動", ZEN, 19*6), f("総合的な学習・評価", ZEN, 19*6), f("特別活動の記録：所見", ZEN, 17*6), f("指導上参考となる諸事項：各教科・科目の学習における特徴", ZEN, 30*6), f("指導上参考となる諸事項：行動の特徴・特技等", ZEN, 30*8), f("指導上参考となる諸事項：進路指導に関する事項", ZEN, 27*2), f("指導上参考となる諸事項：部活動・ボランティア活動・取得資格", ZEN, 27*8), f("指導上参考となる諸事項：その他", ZEN, 27*3), f("出欠の記録", ZEN, 20*2)};
            flds = new Fld[] { dummy("区分"), dummy("出席番号"), f("生徒番号"), f("氏名"), f("年度"), f("総合的な学習・学習活動", ZEN, 19*6), f("総合的な学習・評価", ZEN, 19*6), f("特別活動の記録：所見", ZEN, 17*6), f("総合所見及び指導上参考となる諸事項"), f("出欠の記録", ZEN, 20*2)};

            final StringBuffer stb = new StringBuffer();
            stb.append(" select  ");
            stb.append("  '1' as kubun, ");
            stb.append("  t3.grade || t3.hr_class || t3.attendno, ");
            stb.append("  t1.schregno, ");
            stb.append("  t4.name, ");
            stb.append("  t1.year, ");
            if (_param._isSidoShoken2) {
                stb.append("  t1.totalstudyact as totalstudyact, ");
                stb.append("  t1.totalstudyval as totalstudyval, ");
            } else {
                stb.append("  t12.totalstudyact as totalstudyact, ");
                stb.append("  t12.totalstudyval as totalstudyval, ");
            }
            stb.append("  t1.specialactremark, ");
            stb.append("  t1.totalremark, ");
            stb.append("  t1.attendrec_remark ");
            stb.append(" from htrainremark_dat t1 ");
            stb.append(" left join htrainremark_hdat t12 on t12.schregno = t1.schregno ");
            stb.append(" inner join (select schregno, year, max(semester) as semester from schreg_regd_dat  group by schregno, year) t2 on t2.schregno = t1.schregno ");
            stb.append("  and t2.year = t1.year ");
            stb.append(" inner join schreg_regd_dat t3 on t3.schregno = t2.schregno and t3.year = t2.year and t3.semester = t2.semester ");
            stb.append(" inner join schreg_base_mst t4 on t4.schregno = t1.schregno ");
            stb.append(" order by ");
            stb.append("  t1.year, t3.grade || t3.hr_class || t3.attendno ");
            sql = stb.toString();

        } else if (_62_進学用調査書所見.equals(cd)) {
            titleLines = new String[][] {{"年度", "{YEAR}"}, {"帳票区分", "2", "1：指導要録 2：進学用調査書 3：就職用調査書"}, {"項目No"}};
            flds = new Fld[] { dummy("区分"), dummy("出席番号"), f("生徒番号"), f("氏名"), f("年度"), f("出欠の記録", ZEN, 7*7), f("特別活動の記録", ZEN, 17*8), f("指導上参考となる諸事項・(1)学習における特徴等(2)行動の特徴,特技等", ZEN, 18*5), f("指導上参考となる諸事項・(3)部活動,ボランティア等(4)取得資格,検定等", ZEN, 18*5), f("指導上参考となる諸事項・(5)その他", ZEN, 14*5), f("総合的な学習の時間・活動内容", ZEN, 50*5), f("総合的な学習の時間・評価", ZEN, 50*7), f("備考", ZEN, 53*7)};

            final StringBuffer stb = new StringBuffer();
            stb.append(" select  ");
            stb.append("  '2' as kubun, ");
            stb.append("  t3.grade || t3.hr_class || t3.attendno, ");
            stb.append("  t1.schregno, ");
            stb.append("  t4.name, ");
            stb.append("  t1.year, ");
            stb.append("  t1.attendrec_remark, ");
            stb.append("  t1.specialactrec, ");
            stb.append("  value(t1.train_ref1, t1.train_ref) as train_ref1, ");
            stb.append("  t1.train_ref2, ");
            stb.append("  t1.train_ref3, ");
            stb.append("  t12.totalstudyact, ");
            stb.append("  t12.totalstudyval, ");
            stb.append("  t12.remark ");
            stb.append(" from hexam_entremark_dat t1 ");
            stb.append(" left join hexam_entremark_hdat t12 on t12.schregno = t1.schregno ");
            stb.append(" inner join (select schregno, year, max(semester) as semester from schreg_regd_dat  group by schregno, year) t2 on t2.schregno = t1.schregno ");
            stb.append("  and t2.year = t1.year ");
            stb.append(" inner join schreg_regd_dat t3 on t3.schregno = t2.schregno and t3.year = t2.year and t3.semester = t2.semester ");
            stb.append(" inner join schreg_base_mst t4 on t4.schregno = t1.schregno ");
            stb.append(" order by ");
            stb.append("  t1.year, t3.grade || t3.hr_class || t3.attendno ");
            sql = stb.toString();

        } else if (_63_就職用調査書所見.equals(cd)) {
            titleLines = new String[][] {{"年度", "{YEAR}"}, {"帳票区分", "3", "1：指導要録 2：進学用調査書 3：就職用調査書"}, {"項目No"}};
            flds = new Fld[] { dummy("区分"), dummy("出席番号"), f("生徒番号"), f("氏名"), f("年度"), f("身長"), f("体重"), f("視力（右）"), f("視力（左）"), f("視力矯正（右）"), f("視力矯正（左）"), f("聴力（右）"), f("聴力（左）"), f("検査日"), f("特別活動の記録", ZEN, 18*11), f("欠席の主な理由", ZEN, 10*7), f("身体状況・備考", ZEN, 16*4), f("本人の長所・推薦事由等", ZEN, 39*16)};
        
            final StringBuffer stb = new StringBuffer();
            stb.append(" select  ");
            stb.append("  '3' as kubun, ");
            stb.append("  t3.grade || t3.hr_class || t3.attendno, ");
            stb.append("  t1.schregno, ");
            stb.append("  t4.name, ");
            stb.append("  t3.year, ");
            stb.append("  t5.height, ");
            stb.append("  t5.weight, ");
            stb.append("  t5.r_barevision, ");
            stb.append("  t5.l_barevision, ");
            stb.append("  t5.r_vision, ");
            stb.append("  t5.l_vision, ");
            stb.append("  (select value(name2, name1) from NAME_MST where namecd1 = 'F010' and namecd2 = t5.r_ear) as r_ear, ");
            stb.append("  (select value(name2, name1) from NAME_MST where namecd1 = 'F010' and namecd2 = t5.l_ear) as l_ear, ");
            stb.append("  t6.date, ");
            stb.append("  t1.jobhunt_rec, ");
            stb.append("  t1.jobhunt_absence, ");
            stb.append("  t1.jobhunt_healthremark, ");
            stb.append("  t1.jobhunt_recommend ");
            stb.append(" from hexam_empremark_dat t1 ");
            stb.append(" inner join (select schregno, max(year) as year  from schreg_regd_dat  group by schregno) t2_0 on t2_0.schregno = t1.schregno ");
            stb.append(" inner join (select schregno, year, max(semester) as semester from schreg_regd_dat  group by schregno, year) t2 on t2.schregno = t2_0.schregno and t2.year = t2_0.year ");
            stb.append(" inner join schreg_regd_dat t3 on t3.schregno = t2.schregno and t3.year = t2.year and t3.semester = t2.semester ");
            stb.append(" inner join schreg_base_mst t4 on t4.schregno = t1.schregno ");
            stb.append(" left join medexam_det_dat t5 on t5.year = t3.year and t5.schregno = t3.schregno ");
            stb.append(" left join medexam_hdat t6 on t6.year = t3.year and t6.schregno = t3.schregno ");
            stb.append(" order by ");
            stb.append("  t3.year, t3.grade || t3.hr_class || t3.attendno ");
            sql = stb.toString();

        } else if (_64_概評段階人数D.equals(cd)) {
            flds = new Fld[] { f("年度"), f("学科コード"), f("系列コード"), f("A段階人数"), f("B段階人数"), f("C段階人数"), f("D段階人数"), f("E段階人数"), f("合計人数"), f("合計【】人数")};
        
            final StringBuffer stb = new StringBuffer();
            stb.append(" select  ");
            stb.append(" YEAR, ");
            stb.append("  COURSECD || MAJORCD, ");
            stb.append("  COURSECODE, ");
            stb.append("  A_MEMBER, ");
            stb.append("  B_MEMBER, ");
            stb.append("  C_MEMBER, ");
            stb.append("  D_MEMBER, ");
            stb.append("  E_MEMBER, ");
            stb.append("  COURSE_MEMBER, ");
            stb.append("  COURSE_MEMBER ");
            stb.append(" from  ");
            stb.append("  GENEVIEWMBR_DAT  ");
            stb.append(" where ");
            stb.append("  grade = '03' ");
            stb.append(" order by  ");
            stb.append(" YEAR, COURSECD || MAJORCD, COURSECODE ");
            sql = stb.toString();

        } else if (_65_保健室発生場所.equals(cd)) {
            flds = new Fld[] { f("コード"), f("発生場所"), f("並び順")};

            final StringBuffer stb = new StringBuffer();
            stb.append(" select namecd2, name1, row_number() over(order by namecd2) as order ");
            stb.append(" from NAME_MST ");
            stb.append(" where namecd1 = 'F206' ");
            stb.append(" order by namecd2 ");
            sql = stb.toString();

        } else if (_66_保健室部位.equals(cd)) {
            flds = new Fld[] { f("コード"), f("部位"), f("並び順")};

            final StringBuffer stb = new StringBuffer();
            stb.append(" select namecd2, name1, row_number() over(order by namecd2) as order ");
            stb.append(" from NAME_MST ");
            stb.append(" where namecd1 = 'F207' ");
            stb.append(" order by namecd2 ");
            sql = stb.toString();

        } else if (_67_保健室原因.equals(cd)) {
            flds = new Fld[] { f("コード"), f("原因"), f("並び順")};

            final StringBuffer stb = new StringBuffer();
            stb.append(" select namecd2, name1, row_number() over(order by namecd2) as order ");
            stb.append(" from NAME_MST ");
            stb.append(" where namecd1 = 'F204' ");
            stb.append(" order by namecd2 ");
            sql = stb.toString();

        } else if (_68_保健室時間.equals(cd)) {
            flds = new Fld[] { f("コード"), f("校時名称"), f("並び順")};

            final StringBuffer stb = new StringBuffer();
            stb.append(" select namecd2, name1, row_number() over(order by namecd2) as order ");
            stb.append(" from NAME_MST ");
            stb.append(" where namecd1 = 'F700' ");
            stb.append(" order by namecd2 ");
            sql = stb.toString();

        } else if (_69_保健室症状.equals(cd)) {
            flds = new Fld[] { f("内科・外科等"), f("症状コード"), f("症状名"), f("並び順")};

            final StringBuffer stb = new StringBuffer();
            stb.append(" select '01' as kind, namecd2, name1, row_number() over(order by namecd2) as order ");
            stb.append(" from NAME_MST where namecd1 = 'F200' ");
            stb.append(" union all ");
            stb.append(" select '02' as kind, namecd2, name1, row_number() over(order by namecd2) as order ");
            stb.append(" from NAME_MST where namecd1 = 'F201' ");
            stb.append(" union all ");
            stb.append(" select '03' as kind, namecd2, name1, row_number() over(order by namecd2) as order ");
            stb.append(" from NAME_MST where namecd1 = 'F219' ");
            stb.append(" union all ");
            stb.append(" select '04' as kind, namecd2, name1, row_number() over(order by namecd2) as order ");
            stb.append(" from NAME_MST where namecd1 = 'F202' ");
            stb.append(" union all ");
            stb.append(" select '05' as kind, namecd2, name1, row_number() over(order by namecd2) as order ");
            stb.append(" from NAME_MST where namecd1 = 'F203' ");
            stb.append(" order by kind, namecd2 ");
            sql = stb.toString();

        } else if (_70_保健室処置.equals(cd)) {
            flds = new Fld[] { f("内科・外科等"), f("処置コード"), f("処置名"), f("並び順")};

            final StringBuffer stb = new StringBuffer();
            stb.append(" select '01' as kind, namecd2, name1, row_number() over(order by namecd2) as order ");
            stb.append(" from NAME_MST where namecd1 = 'F208' ");
            stb.append(" union all ");
            stb.append(" select '02' as kind, namecd2, name1, row_number() over(order by namecd2) as order ");
            stb.append(" from NAME_MST where namecd1 = 'F209' ");
            stb.append(" union all ");
            stb.append(" select '03' as kind, namecd2, name1, row_number() over(order by namecd2) as order ");
            stb.append(" from NAME_MST where namecd1 = 'F220' ");
            stb.append(" union all ");
            stb.append(" select '04' as kind, namecd2, name1, row_number() over(order by namecd2) as order ");
            stb.append(" from NAME_MST where namecd1 = 'F210' ");
            stb.append(" union all ");
            stb.append(" select '05' as kind, namecd2, name1, row_number() over(order by namecd2) as order ");
            stb.append(" from NAME_MST where namecd1 = 'F210' ");
            stb.append(" order by kind, namecd2 ");
            sql = stb.toString();

        } else if (_71_保健室来室情報D.equals(cd)) {
            flds = new Fld[] { f("年度"), f("利用日"), f("生徒番号"), f("来室（時）"), f("来室（分）"), f("退室（時）"), f("退室（分）"), f("曜日"), f("内科・外科コード"), f("症状コード1"), f("症状1（手入力）"), f("症状コード2"), f("症状2（手入力）"), f("来室校時コード"), f("退室校時コード"), f("部位コード"), f("部位（手入力）"), f("原因コード"), f("原因（手入力）"), f("場所コード"), f("場所（手入力）"), f("体温"), f("処置コード"), f("処置（手入力）"), f("共済利用"), f("所見1"), f("所見２"), f("所見３"), f("備考")};

            final StringBuffer stb = new StringBuffer();
            stb.append(" select ");
            stb.append("   fiscalyear(t1.visit_date) as year, ");
            stb.append("   t1.visit_date, ");
            stb.append("   t1.schregno, ");
            stb.append("   t1.visit_hour, ");
            stb.append("   t1.visit_minute, ");
            stb.append("   t1.leave_hour, ");
            stb.append("   t1.leave_minute, ");
            stb.append("  dayofweek(t1.visit_date) as dow, ");
            stb.append("   '0' || case type when '3' then '5' ");
            stb.append("                            when '5' then '3' ");
            stb.append("                            else type end as type, ");
            stb.append("   visit_reason1, ");
            stb.append("   '' as shojo1_te, ");
            stb.append("   value(t1.visit_reason2, t1.visit_reason3) as visit_reason2, ");
            stb.append("   '' as shojo2_te, ");
            stb.append("   t1.visit_periodcd, ");
            stb.append("   t1.leave_periodcd, ");
            stb.append("  value(t1.injury_part1,t1.injury_part2,t1.injury_part3) as injury_part, ");
            stb.append("  '' as bui_te, ");
            stb.append("  t1.occur_cause, ");
            stb.append("  '' as genin_te, ");
            stb.append("  t1.occur_place, ");
            stb.append("  '' as basho_te, ");
            stb.append("  value(temperature3, temperature2, temperature1) as temparature,  ");
            stb.append("  value(treatment1,treatment2, treatment3) as treatment, ");
            stb.append("  '' as shochi_te, ");
            stb.append("  '' as kyosai, ");
            stb.append("  '' as shoken1, ");
            stb.append("  '' as shoken2, ");
            stb.append("  '' as shoken3, ");
            stb.append("  special_note as biko ");
            stb.append(" from nurseoff_visitrec_dat t1 ");
            stb.append(" inner join schreg_base_mst t2 on t2.schregno = t1.schregno ");
            stb.append(" order by ");
            stb.append("   t1.visit_date, ");
            stb.append("   t1.schregno ");
            sql = stb.toString();

        } else if (_72_保健日報D.equals(cd)) {
            flds = new Fld[] { f("登録日"), f("年度"), f("天気"), f("気温"), f("学校行事"), f("日誌"), f("記入職員番号"), f("病欠（1年）"), f("病欠（2年）"), f("病欠（3年）"), f("病欠（4年）"), f("事故欠（1年）"), f("事故欠（2年）"), f("事故欠（3年）"), f("事故欠（4年）"), f("無届（1年）"), f("無届（2年）"), f("無届（3年）"), f("無届（4年）"), f("出席停止（1年）"), f("出席停止（2年）"), f("出席停止（3年）"), f("出席停止（4年）"), f("忌引き（1年）"), f("忌引き（2年）"), f("忌引き（3年）"), f("忌引き（4年）"), f("病欠（計）"), f("事故欠（計）"), f("無届（計）"), f("出席停止（計）"), f("忌引き（計）")};
            outputFmt = new OutputFmt72();

        } else if (_73_健康診断検査区分.equals(cd)) {
            flds = new Fld[] { f("コード"), f("検査区分名称")};
        
        } else if (_74_健康診断情報.equals(cd)) {
            titleLines = new String[][] {{"定期健康診断結果一覧表"}, {}, {"出力条件:", "{YEAR}年度"}, {}};
            flds = new Fld[] { f("生徒番号"), dummy("出席番号"), f("氏名"), dummy("性別"), dummy("生年月日"), f("身長"), f("体重"), f("肥満度"), f("BMI指数"), f("座高"), f("栄養状態"), f("脊柱・胸郭"), f("眼の疾病及び異常・眼科所見"), f("眼科指示"), f("視力(右)"), f("視力(左)"), f("矯正視力(右)"), f("矯正視力(右)・めがね"), f("矯正視力(左)"), f("矯正視力(左)・めがね"), f("視力(右)数値"), f("視力(左)数値"), f("矯正視力(右)数値"), f("矯正視力(左)数値"), f("聴力・右1000Hz"), f("聴力・左1000Hz"), f("聴力・右4000Hz"), f("聴力・左4000Hz"), f("耳鼻咽喉科疾患"), f("皮膚疾患"), f("結核撮影日"), f("結核(所見)"), f("結核(その他の検査)"), f("結核(病名)"), f("結核(指導区分)"), f("心臓(心電図)"), f("心臓(疾病・異常)"), f("尿・蛋白(一次)"), f("尿・糖(一次)"), f("尿・潜血(一次)"), f("尿・蛋白(二次)"), f("尿・糖(二次)"), f("尿・潜血(二次)"), f("貧血検査"), f("その他の疾病及び異常"), f("学校医所見・内科検診", ZEN, 30), f("学校医・日付"), f("事後措置(学校医)", ZEN, 30), f("測定日(内科)"), f("歯列・咬合"), f("顎間接"), f("歯垢の状態"), f("歯肉の状態"), f("歯石の状態"), f("乳歯・現在歯数"), f("乳歯・未処置歯数"), f("乳歯・処置歯数"), f("永久歯・現在歯数"), f("永久歯・未処置歯数"), f("永久歯・処置歯数"), f("永久歯・喪失歯数"), f("要注意乳歯"), f("要観察歯(永久歯)"), f("要観察歯(乳歯)"), f("要精検歯"), f("歯科口腔の疾病及び異常", ZEN, 30), f("学校歯科医所見", ZEN, 30), f("学校歯科医・日付"), f("事後措置(学校歯科医)", ZEN, 30), f("測定日(歯科)"), f("備考", ZEN, 30)};
            outputFmt = new OutputFmtYear(" select distinct t1.year from medexam_det_dat t1 union select distinct t1.year from medexam_tooth_dat t1 order by year ");
            
            final StringBuffer stb = new StringBuffer();
            stb.append(" with t_med_schregnos as ( ");
            stb.append(" select year, schregno from medexam_det_dat ");
            stb.append(" union ");
            stb.append(" select year, schregno from medexam_tooth_dat ");
            stb.append(" ) ");
            stb.append(" select ");
            stb.append("  t1.schregno, ");
            stb.append("  '' as attendno, ");
            stb.append("  '' as name, ");
            stb.append("  '' as sex_name, ");
            stb.append("  '' as birthday, ");
            stb.append("  t2.height, ");
            stb.append("  t2.weight, ");
            stb.append("  '' as himando, ");
            stb.append("  CASE WHEN VALUE(t2.HEIGHT,0) > 0 THEN DECIMAL(ROUND(t2.WEIGHT/t2.HEIGHT/t2.HEIGHT*10000,1),4,1) END AS BMI, ");
            stb.append("  t2.sitheight, ");
            stb.append("  case when t2.nutritioncd is null then '9:空欄'  ");
            stb.append("          when t2.nutritioncd = '01' then '1:／' ");
            stb.append("          else '2:要注意' ");
            stb.append("  end as eiyou_joutai,  ");
            stb.append("  case when t2.SPINERIBCD is null then '9:空欄'  ");
            stb.append("          when t2.SPINERIBCD = '01' then '1:／' ");
            stb.append("          else (select name1 from NAME_MST where namecd1 = 'F040' and namecd2 = t2.SPINERIBCD) ");
            stb.append("  end as sekichu_kyoukaku,  ");
            stb.append("  value(eye_test_result,  ");
            stb.append("  case when t2.EYEDISEASECD is null then '9:空欄'  ");
            stb.append("          when t2.EYEDISEASECD = '01' then '1:／' ");
            stb.append("          else (select name1 from NAME_MST where namecd1 = 'F050' and namecd2 = t2.EYEDISEASECD) ");
            stb.append("  end) as menoshippei_oyobi_ijou,  ");
            stb.append("  '' as ganka_shiji, ");
            stb.append("  t2.r_barevision_mark as shiryoku_migi, ");
            stb.append("  t2.l_barevision_mark as shiryoku_hidari, ");
            stb.append("  t2.r_vision_mark as shiryoku_migi_kyousei, ");
            stb.append("  '' as megane_migi, ");
            stb.append("  t2.l_vision_mark as shiryoku_hidari_kyousei, ");
            stb.append("  '' as megane_hidari, ");
            stb.append("  t2.r_barevision as shiryoku_migi_num, ");
            stb.append("  t2.l_barevision as shiryoku_hidari_num, ");
            stb.append("  t2.r_vision as shiryoku_migi, ");
            stb.append("  t2.l_vision as shiryoku_hidari, ");
            stb.append("  case when t2.r_ear is null or t2.r_ear = '00' then '9:空欄'  ");
            stb.append("          when t2.r_ear = '02' or t2.r_ear = '04' then '2:要再検' else '1:／' ");
            stb.append("  end as chouryoku_migi_1000,  ");
            stb.append("  case when t2.l_ear is null or t2.l_ear = '00' then '9:空欄'  ");
            stb.append("          when t2.l_ear = '02' or t2.l_ear = '04' then '2:要再検' else '1:／' ");
            stb.append("  end as chouryoku_hidari_1000,  ");
            stb.append("  case when t2.r_ear is null or t2.r_ear = '00' then '9:空欄'  ");
            stb.append("          when t2.r_ear = '03' or t2.r_ear = '04' then '2:要再検' else '1:／' ");
            stb.append("  end as chouryoku_migi_4000,  ");
            stb.append("  case when t2.l_ear is null or t2.l_ear = '00' then '9:空欄'  ");
            stb.append("          when t2.l_ear = '03' or t2.l_ear = '04' then '2:要再検' else '1:／' ");
            stb.append("  end as chouryoku_hidari_4000,  ");
            stb.append("  value(NOSEDISEASECD_REMARK,  ");
            stb.append("  case when t2.NOSEDISEASECD is null or t2.NOSEDISEASECD = '00' then '9:空欄'  ");
            stb.append("          when t2.NOSEDISEASECD = '01' then '1:／' ");
            stb.append("          else (select name1 from NAME_MST where namecd1 = 'F060' and namecd2 = t2.NOSEDISEASECD) ");
            stb.append("  end) as jibiinkouka_shikkan,  ");
            stb.append(" case when t2.SKINDISEASECD is null or t2.SKINDISEASECD = '00' then '9:空欄'  ");
            stb.append("          when t2.SKINDISEASECD = '01' then '1:／' ");
            stb.append("          else (select name1 from NAME_MST where namecd1 = 'F070' and namecd2 = t2.SKINDISEASECD) ");
            stb.append("  end as hifu_shikkan, ");
            stb.append("  t2.tb_filmdate as kekkaku_satsueibi, ");
            stb.append(" case when t2.TB_REMARKCD is null or t2.TB_REMARKCD = '00' then '9:空欄'  ");
            stb.append("          when t2.TB_REMARKCD = '01' then '1:／' ");
            stb.append("          when t2.TB_REMARKCD = '03' then '2:精検不要' ");
            stb.append("          when t2.TB_REMARKCD = '04' then '3:要精密' ");
            stb.append("          else (select name1 from NAME_MST where namecd1 = 'F100' and namecd2 = t2.TB_REMARKCD) ");
            stb.append("  end as kekkaku_shoken, ");
            stb.append(" case when t2.TB_OTHERTESTCD is null or t2.TB_OTHERTESTCD = '00' then '9:空欄'  ");
            stb.append("          when t2.TB_OTHERTESTCD = '01' then '1:／' ");
            stb.append("          else (select name1 from NAME_MST where namecd1 = 'F110' and namecd2 = t2.TB_OTHERTESTCD) ");
            stb.append("  end as kekkaku_sonota_shoken, ");
            stb.append(" case when t2.TB_NAMECD is null or t2.TB_NAMECD = '00' then '9:空欄'  ");
            stb.append("          when t2.TB_NAMECD = '01' then '1:／' ");
            stb.append("          else (select name1 from NAME_MST where namecd1 = 'F120' and namecd2 = t2.TB_NAMECD) ");
            stb.append("  end as kekkaku_byoumei, ");
            stb.append("  '' as kekkaku_shidou_kbn, ");
            stb.append(" case when t2.HEART_MEDEXAM is null or t2.HEART_MEDEXAM = '00' then '9:空欄'  ");
            stb.append("          when t2.HEART_MEDEXAM = '01' then '1:／' ");
            stb.append("          when t2.HEART_MEDEXAM = '02' then '3:要精密' ");
            stb.append("          else value(HEARTDISEASECD_REMARK, (select name1 from NAME_MST where namecd1 = 'F080' and namecd2 = t2.HEART_MEDEXAM)) ");
            stb.append("  end as shinzou_shindenzu, ");
            stb.append("  value(HEARTDISEASECD_REMARK,  ");
            stb.append("  case when t2.HEARTDISEASECD is null or t2.HEARTDISEASECD = '00' then '9:空欄'  ");
            stb.append("          when t2.HEARTDISEASECD = '01' then '1:／' ");
            stb.append("          else (select name1 from NAME_MST where namecd1 = 'F090' and namecd2 = t2.HEARTDISEASECD) ");
            stb.append("  end) as shinzou_sippei_ijou,  ");
            stb.append("  case (select name1 from NAME_MST where namecd1 = 'F020' and namecd2 = t2.ALBUMINURIA1CD) ");
            stb.append("     when '-' then '-' ");
            stb.append("     when '+' then '+' ");
            stb.append("     when '++' then '2+' ");
            stb.append("     when '+++' then '3+' ");
            stb.append("     when '±' then '+-' ");
            stb.append("  end as nyou_tanpaku1, ");
            stb.append("  case (select name1 from NAME_MST where namecd1 = 'F020' and namecd2 = t2.URICSUGAR1CD) ");
            stb.append("     when '-' then '-' ");
            stb.append("     when '+' then '+' ");
            stb.append("     when '++' then '2+' ");
            stb.append("     when '+++' then '3+' ");
            stb.append("     when '±' then '+-' ");
            stb.append("  end as nyou_tou1, ");
            stb.append("  case (select name1 from NAME_MST where namecd1 = 'F020' and namecd2 = t2.URICBLEED1CD) ");
            stb.append("     when '-' then '-' ");
            stb.append("     when '+' then '+' ");
            stb.append("     when '++' then '2+' ");
            stb.append("     when '+++' then '3+' ");
            stb.append("     when '±' then '+-' ");
            stb.append("  end as nyou_senketsu1, ");
            stb.append("  case (select name1 from NAME_MST where namecd1 = 'F020' and namecd2 = t2.ALBUMINURIA2CD) ");
            stb.append("     when '-' then '-' ");
            stb.append("     when '+' then '+' ");
            stb.append("     when '++' then '2+' ");
            stb.append("     when '+++' then '3+' ");
            stb.append("     when '±' then '+-' ");
            stb.append("  end as nyou_tanpaku2, ");
            stb.append("  case (select name1 from NAME_MST where namecd1 = 'F020' and namecd2 = t2.URICSUGAR2CD) ");
            stb.append("     when '-' then '-' ");
            stb.append("     when '+' then '+' ");
            stb.append("     when '++' then '2+' ");
            stb.append("     when '+++' then '3+' ");
            stb.append("     when '±' then '+-' ");
            stb.append("  end as nyou_tou2, ");
            stb.append("  case (select name1 from NAME_MST where namecd1 = 'F020' and namecd2 = t2.URICBLEED2CD) ");
            stb.append("     when '-' then '-' ");
            stb.append("     when '+' then '+' ");
            stb.append("     when '++' then '2+' ");
            stb.append("     when '+++' then '3+' ");
            stb.append("     when '±' then '+-' ");
            stb.append("  end as nyou_senketsu2, ");
            stb.append("  hemoglobin as hinketsu_kensa, ");
            stb.append("  value(OTHER_REMARK,  ");
            stb.append("  case when t2.OTHERDISEASECD is null then '9:空欄'  ");
            stb.append("          when t2.OTHERDISEASECD = '01' then '1:／' ");
            stb.append("          else (select name1 from NAME_MST where namecd1 = 'F140' and namecd2 = t2.OTHERDISEASECD) ");
            stb.append("  end) as sonota_sppei_oyobi_ijou,  ");
            stb.append("  t2.doc_remark as gakkoui_syoken_naika_kenshin, ");
            stb.append("  t2.doc_date as gakkoui_hiduke, ");
            stb.append(" (select name1 from NAME_MST where namecd1 = 'F150' and namecd2 = t2.TREATCD) as jigosyochi_gakkoui, ");
            stb.append("  t4.date as sokuteibi, ");
            stb.append("  case  ");
            stb.append("    when t3.jaws_jointcd = '00' then '0' ");
            stb.append("    when t3.jaws_jointcd = '01' then '1' ");
            stb.append("    when t3.jaws_jointcd is not null then '2' ");
            stb.append("  end as shiretsu_kougou, ");
            stb.append("  case  ");
            stb.append("    when t3.jaws_jointcd2 = '00' then '0' ");
            stb.append("    when t3.jaws_jointcd2 = '01' then '1' ");
            stb.append("    when t3.jaws_jointcd2 is not null then '2' ");
            stb.append("  end as ago_kansetsu, ");
            stb.append("  case  ");
            stb.append("    when t3.plaquecd = '00' then '0' ");
            stb.append("    when t3.plaquecd = '01' then '1' ");
            stb.append("    when t3.plaquecd is not null then  '2' ");
            stb.append("  end as shikou_no_joutai, ");
            stb.append("  case  ");
            stb.append("    when t3.gumcd = '00' then '0' ");
            stb.append("    when t3.gumcd = '01' then '1' ");
            stb.append("    when t3.gumcd is not null then '2' ");
            stb.append("  end as shiniku, ");
            stb.append("  case t3.calculus ");
            stb.append("    when '00' then '0' ");
            stb.append("    when '01' then '1' ");
            stb.append("  end as shiseki, ");
            stb.append("  t3.babytooth as nyushi_genzai_suu, ");
            stb.append("  t3.remainbabytooth as nyushi_mishochi_suu, ");
            stb.append("  t3.treatedbabytooth as nyushi_shochi_suu, ");
            stb.append("  t3.adulttooth as eikyushi_genzai_suu, ");
            stb.append("  t3.remainadulttooth as eikyushi_mishochi_suu, ");
            stb.append("  t3.treatedadulttooth as eikyushi_shochi_suu, ");
            stb.append("  t3.lostadulttooth as eikyushi_soushitsu_suu, ");
            stb.append("  t3.brack_babytooth as you_chuui_nyuushi_suu, ");
            stb.append("  t3.brack_adulttooth as you_kansatsu_shi_eikyushi, ");
            stb.append("  0 as you_kansatsu_shi_nyushi, ");
            stb.append("  case when t3.checkadulttooth = 0 then '0:無' ");
            stb.append("       when t3.checkadulttooth > 0 then '2:有・永久歯' ");
            stb.append("  end as you_seikenshi, ");
            stb.append("  value(OTHERDISEASE, ");
            stb.append("   (select name1 from NAME_MST where namecd1 = 'F530' and namecd2 = t3.OTHERDISEASECD) ");
            stb.append("  ) as sikakoukuu_sippei_oyobi_ijou, ");
            stb.append("  value(DENTISTREMARK, ");
            stb.append("   (select name1 from NAME_MST where namecd1 = 'F540' and namecd2 = t3.DENTISTREMARKCD) ");
            stb.append("  ) as gakkou_shikai_shoken, ");
            stb.append("  t3.dentistremarkdate as gakkou_shikai_date, ");
            stb.append("  t3.dentisttreat as jigoshochi, ");
            stb.append("  t4.tooth_date, ");
            stb.append("  t2.remark ");
            stb.append(" from t_med_schregnos t1 ");
            stb.append(" left join medexam_det_dat t2 on t2.year = t1.year and t2.schregno = t1.schregno ");
            stb.append(" left join medexam_tooth_dat t3 on t3.year = t1.year and t3.schregno = t1.schregno ");
            stb.append(" left join medexam_hdat t4 on t4.year = t1.year and t4.schregno = t1.schregno ");
            stb.append(" inner join schreg_base_mst t5 on t5.schregno = t1.schregno ");
            stb.append(" where ");
            stb.append(" t1.year = ? ");
            stb.append(" order by ");
            stb.append(" t1.year, t1.schregno ");
            sql = stb.toString();

        } else if (_75_健康診断歯式情報.equals(cd)) {
            titleLines = new String[][] {{"定期健康診断結果(歯式)一覧表"}, {"", "", "", "", "", "", "1:う歯（未処置歯）、2:う歯（処置歯）、7:喪失歯（永久歯）、8:要注意乳歯、9:要観察歯"}, {"出力条件:", "{YEAR}年度"}, {}};
            flds = new Fld[] { f("生徒番号"), dummy("出席番号"), f("氏名"), dummy("性別"), dummy("生年月日"), f("永久歯・上右8"), f("永久歯・上右7"), f("永久歯・上右6"), f("永久歯・上右5"), f("永久歯・上右4"), f("永久歯・上右3"), f("永久歯・上右2"), f("永久歯・上右1"), f("永久歯・上左1"), f("永久歯・上左2"), f("永久歯・上左3"), f("永久歯・上左4"), f("永久歯・上左5"), f("永久歯・上左6"), f("永久歯・上左7"), f("永久歯・上左8"), f("乳歯・上右E"), f("乳歯・上右D"), f("乳歯・上右C"), f("乳歯・上右B"), f("乳歯・上右A"), f("乳歯・上左A"), f("乳歯・上左B"), f("乳歯・上左C"), f("乳歯・上左D"), f("乳歯・上左E"), f("乳歯・下右E"), f("乳歯・下右D"), f("乳歯・下右C"), f("乳歯・下右B"), f("乳歯・下右A"), f("乳歯・下左A"), f("乳歯・下左B"), f("乳歯・下左C"), f("乳歯・下左D"), f("乳歯・下左E"), f("永久歯・下右8"), f("永久歯・下右7"), f("永久歯・下右6"), f("永久歯・下右5"), f("永久歯・下右4"), f("永久歯・下右3"), f("永久歯・下右2"), f("永久歯・下右1"), f("永久歯・下左1"), f("永久歯・下左2"), f("永久歯・下左3"), f("永久歯・下左4"), f("永久歯・下左5"), f("永久歯・下左6"), f("永久歯・下左7"), f("永久歯・下左8")};
            outputFmt = new OutputFmtYear(" select distinct t1.year from medexam_tooth_dat t1 order by t1.year ");
            
            final StringBuffer stb = new StringBuffer();
            stb.append(" with t_F550 (KNJVAL, VAL) as ( ");
            stb.append(" values('01', '0') ");
            stb.append(" union all values('02', '1') ");
            stb.append(" union all values('03', '2') ");
            stb.append(" union all values('04', '7') ");
            stb.append(" union all values('05', '8') ");
            stb.append(" union all values('06', '9') ");
            stb.append(" ) ");
            stb.append(" select ");
            stb.append("   t1.schregno, ");
            stb.append("   '' as attendno, ");
            stb.append("   '' as name, ");
            stb.append("   '' as sex, ");
            stb.append("   '' as birthday, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.UP_R_ADULT8) as eik_ue_mg8, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.UP_R_ADULT7) as eik_ue_mg7, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.UP_R_ADULT6) as eik_ue_mg6, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.UP_R_ADULT5) as eik_ue_mg5, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.UP_R_ADULT4) as eik_ue_mg4, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.UP_R_ADULT3) as eik_ue_mg3, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.UP_R_ADULT2) as eik_ue_mg2, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.UP_R_ADULT1) as eik_ue_mg1, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.UP_L_ADULT1) as eik_ue_hdr1, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.UP_L_ADULT2) as eik_ue_hdr2, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.UP_L_ADULT3) as eik_ue_hdr3, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.UP_L_ADULT4) as eik_ue_hdr4, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.UP_L_ADULT5) as eik_ue_hdr5, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.UP_L_ADULT6) as eik_ue_hdr6, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.UP_L_ADULT7) as eik_ue_hdr7, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.UP_L_ADULT8) as eik_ue_hdr8, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.UP_R_BABY5) as nyus_ue_mg_E, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.UP_R_BABY4) as nyus_ue_mg_D, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.UP_R_BABY3) as nyus_ue_mg_C, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.UP_R_BABY2) as nyus_ue_mg_B, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.UP_R_BABY1) as nyus_ue_mg_A, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.UP_L_BABY1) as nyus_ue_hdr_A, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.UP_L_BABY2) as nyus_ue_hdr_B, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.UP_L_BABY3) as nyus_ue_hdr_C, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.UP_L_BABY4) as nyus_ue_hdr_D, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.UP_L_BABY5) as nyus_ue_hdr_E, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.LW_R_BABY5) as nyus_sita_mg_E, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.LW_R_BABY4) as nyus_sita_mg_D, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.LW_R_BABY3) as nyus_sita_mg_C, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.LW_R_BABY2) as nyus_sita_mg_B, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.LW_R_BABY1) as nyus_sita_mg_A, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.LW_L_BABY1) as nyus_sita_hdr_A, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.LW_L_BABY2) as nyus_sita_hdr_B, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.LW_L_BABY3) as nyus_sita_hdr_C, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.LW_L_BABY4) as nyus_sita_hdr_D, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.LW_L_BABY5) as nyus_sita_hdr_E, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.LW_R_ADULT8) as eik_sita_mg8, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.LW_R_ADULT7) as eik_sita_mg7, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.LW_R_ADULT6) as eik_sita_mg6, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.LW_R_ADULT5) as eik_sita_mg5, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.LW_R_ADULT4) as eik_sita_mg4, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.LW_R_ADULT3) as eik_sita_mg3, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.LW_R_ADULT2) as eik_sita_mg2, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.LW_R_ADULT1) as eik_sita_mg1, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.LW_L_ADULT1) as eik_sita_hdr1, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.LW_L_ADULT2) as eik_sita_hdr2, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.LW_L_ADULT3) as eik_sita_hdr3, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.LW_L_ADULT4) as eik_sita_hdr4, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.LW_L_ADULT5) as eik_sita_hdr5, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.LW_L_ADULT6) as eik_sita_hdr6, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.LW_L_ADULT7) as eik_sita_hdr7, ");
            stb.append("   (select val from t_F550 where KNJVAL = t1.LW_L_ADULT8) as eik_sita_hdr8 ");
            stb.append(" from medexam_tooth_dat t1 ");
            stb.append(" inner join schreg_base_mst t2 on t2.schregno = t1.schregno ");
            stb.append(" where ");
            stb.append("   t1.year = ? ");
            stb.append(" order by ");
            stb.append("   t1.year, ");
            stb.append("   t1.schregno ");
            sql = stb.toString();

        } else if (_76_既往症.equals(cd)) {
            titleLines = new String[][] {{}};
            flds = new Fld[] { dummy("学年"), dummy("クラス"), dummy("出席No"), f("生徒番号"), f("氏名"), f("治療中の持病・既往症等", ZEN, 100), f("症状・注意事項等", ZEN, 100)};
        
            final StringBuffer stb = new StringBuffer();
            stb.append(" with medhist as ( ");
            stb.append(" select distinct ");
            stb.append("   t1.schregno, ");
            stb.append("  NMF141.name1 as GUIDE_DIV_TEXT, ");
            stb.append("   t2.name1 as hist1, ");
            stb.append("   t3.name1 as hist2, ");
            stb.append("   t4.name1 as hist3 ");
            stb.append(" from medexam_det_dat t1 ");
            stb.append(" left join NAME_MST t2 on t2.namecd1 = 'F143' ");
            stb.append("  and t2.namecd2 = t1.medical_history1 ");
            stb.append(" left join NAME_MST t3 on t3.namecd1 = 'F143' ");
            stb.append("  and t3.namecd2 = t1.medical_history2 ");
            stb.append(" left join NAME_MST t4 on t4.namecd1 = 'F143' ");
            stb.append("  and t4.namecd2 = t1.medical_history3 ");
            stb.append(" left join NAME_MST NMF141 on NMF141.namecd1 = 'F141' ");
            stb.append("   and NMF141.namecd2 = t1.guide_div ");
            stb.append(" where t2.name1 is not null ");
            stb.append("  or t3.name1 is not null ");
            stb.append("  or t4.name1 is not null ");
            stb.append(" ) ");
            stb.append(" select  ");
            stb.append("  '' as grade, ");
            stb.append("  '' as hr_class, ");
            stb.append("  '' as attendno, ");
            stb.append("  t1.schregno, ");
            stb.append("  t5.name,  ");
            stb.append("  value(t1.hist1, '') ");
            stb.append(" || case when t1.hist1 is null then '' when t1.hist2 is null and t1.hist3 is null then '' else '、' end ");
            stb.append(" || value(t1.hist2, '') ");
            stb.append(" || case when t1.hist1 is null and t1.hist2 is null then '' when t1.hist3 is null then '' else '、' end ");
            stb.append(" || value(t1.hist3, '') as jibyou_kioushou_tou, ");
            stb.append("  t1.guide_div_text ");
            stb.append(" from medhist t1 ");
            stb.append(" inner join schreg_base_mst t5 on t5.schregno = t1.schregno ");
            stb.append(" order by t1.schregno ");
            sql = stb.toString();

        } else if (_77_進路区分.equals(cd)) {
            flds = new Fld[] { f("コード"), f("区分名")};

            final StringBuffer stb = new StringBuffer();
            stb.append(" select namecd2, name1 ");
            stb.append(" from NAME_MST ");
            stb.append(" where namecd1 = 'E012' ");
            stb.append(" order by namecd2 ");
            sql = stb.toString();

        } else if (_78_進路推薦区分.equals(cd)) {
            flds = new Fld[] { f("コード"), f("推薦名"), f("短縮")};
            
            final StringBuffer stb = new StringBuffer();
            stb.append(" select namecd2, name1, name1 ");
            stb.append(" from NAME_MST ");
            stb.append(" where namecd1 = 'E002' ");
            stb.append(" order by namecd2 ");
            sql = stb.toString();

        } else if (_79_進路先m.equals(cd)) {
            titleLines = new String[][] {{"進路先情報"}};
            flds = new Fld[] { f("進路先コード", HAN, 10), f("進路先区分コード", HAN, 1), dummy("進路先区分名"), f("立区分コード"), dummy("立区分名"), f("進路先名", ZEN, 25), f("学部名", ZEN, 10), f("学科名", ZEN, 15), f("日程"), f("入試方式"), f("小分類コード", HAN, 3), f("小分類名", ZEN, 5), f("推薦人数")};
        
            final StringBuffer stb = new StringBuffer();
            stb.append(" select ");
            stb.append("  right(L3.SCHOOL_CD || value(L5.facultycd, '000') || value(L6.departmentcd, '000'), 10) as cd, ");
            stb.append("  L3.SCHOOL_GROUP, ");
            stb.append("  '' as school_group_name, ");
            stb.append("  '' as setsuritsu_kbn_cd, ");
            stb.append("  '' as setsuritsu_kbn_Name, ");
            stb.append("  L3.SCHOOL_NAME, ");
            stb.append("  L5.facultyname, ");
            stb.append("  L6.departmentname, ");
            stb.append("  '' as nittei, ");
            stb.append("  '' as nyushi_houshiki, ");
            stb.append("  '' as shou_bunrui_cd, ");
            stb.append("  '' as shou_bunrui_name, ");
            stb.append("  '' as recommend_count ");
            stb.append(" from  ");
            stb.append("  COLLEGE_MST L3 ");
            stb.append("   LEFT JOIN COLLEGE_FACULTY_MST L5 ON L5.SCHOOL_CD = L3.SCHOOL_CD  ");
            stb.append("   LEFT JOIN COLLEGE_DEPARTMENT_MST L6 ON L6.SCHOOL_CD = L3.SCHOOL_CD AND L6.FACULTYCD = L5.FACULTYCD  ");
            sql = stb.toString();

        } else if (_80_進路希望.equals(cd)) {
            titleLines = new String[][] {{"進路希望・合否情報一覧"}};
            flds = new Fld[] { f("年度"), f("生徒番号"), dummy("出席番号"), f("氏名"), dummy("系列"), f("進路先コード"), dummy("進路先名"), f("推薦コード"), dummy("推薦名"), f("合否"), f("進路先決定")};
        
            final StringBuffer stb = new StringBuffer();
            stb.append(" select ");
            stb.append("   t1.year, ");
            stb.append("   t1.schregno, ");
            stb.append("   '' as attendno, ");
            stb.append("   '' as name, ");
            stb.append("   '' as keiretsu, ");
            stb.append("   right(stat_cd || value(t1.facultycd, '000') || value(t1.departmentcd, '000'), 10) as sinrosaki, ");
            stb.append("   '' as sinrosaki_name, ");
            stb.append("   juken_howto as suisen_cd, ");
            stb.append("   '' as suisen_name, ");
            stb.append("   case decision  ");
            stb.append("    when '1' then '合格' ");
            stb.append("    when '2' then '不合格' ");
            stb.append("   end as gouhi, ");
            stb.append("   case when planstat = '1' ");
            stb.append("    then '○' ");
            stb.append("   end as sinrosaki_kettei ");
            stb.append(" from aft_grad_course_dat t1 ");
            stb.append(" inner join schreg_base_mst t2 on t2.schregno = t1.schregno ");
            stb.append("   LEFT JOIN COLLEGE_FACULTY_MST L5 ON L5.SCHOOL_CD = t1.stat_cd and l5.facultycd = t1.facultycd ");
            stb.append("   LEFT JOIN COLLEGE_DEPARTMENT_MST L6 ON L6.SCHOOL_CD = t1.stat_cd AND L6.FACULTYCD = t1.FACULTYCD and l6.departmentcd = t1.departmentcd ");
            stb.append(" where ");
            stb.append("    t1.senkou_Kind = '0' ");
            stb.append(" order by ");
            stb.append("   t1.year, ");
            stb.append("   t1.schregno ");
            sql = stb.toString();

        } else {
            return null;
        }
        if (null == outputFmt) {
            outputFmt = new OutputFmt(); // デフォルトの出力
        }
        outputFmt._cd = cd;
        outputFmt._titleLines = titleLines;
        outputFmt._flds = flds;
        outputFmt._createTableList = createTableList;
        if (null != sql) {
            outputFmt._sql = sql;
        }
        return outputFmt;
    }
    
    private Fld f(final String name) {
        return new Fld(name, false, null, -1);
    }
    private Fld f(final String name, final String zenOrHan, final int maxLen) {
        return new Fld(name, false, zenOrHan, maxLen);
    }
    private Fld dummy(final String name) {
        return new Fld(name, true, null, -1);
    }

    private static class Fld {
        private String _name;
        private boolean _isDummy;
        private String _zenOrHan;
        private int _maxLen;
        private int _columnType = Types.VARCHAR; // デフォルトはVARCHARとしておく
        Fld(final String name, final boolean isDummy, final String zenOrHan, final int maxLen) {
            _name = name;
            _isDummy = isDummy;
            _zenOrHan = zenOrHan;
            _maxLen = maxLen;
        }
    }
    
    private static class IkouUtil {
        
        private static final String tori_station_mst = "tori_station_mst";
        private static final String tori_subclass_name_mst = "tori_subclass_name_mst";
        private static final String tori_promoter_mst = "tori_promoter_mst";
        private static final String tori_testcd_mst = "tori_testcd_mst";
        private static final String tori_attendrange_dat = "tori_attendrange_dat";
        private static final String[] tmpTables = {tori_station_mst, tori_subclass_name_mst, tori_promoter_mst, tori_testcd_mst, tori_attendrange_dat};
        
        public static void createTableToriStationMst(final DB2UDB db2) {
            String sql = null;
            try {
                if (QueryUtil.dbHasTable(db2, tori_station_mst)) {
                    return;
                }
                
                sql = " create table tori_station_mst (stationcd varchar(3) not null, stationname varchar(30), kbn varchar(15), order smallint) in usr1dms index in idx1dms ";
                QueryUtil.executeUpdate(db2, sql);
                
                sql = " alter table tori_station_mst add constraint t_stat_m_key primary key (stationcd) ";
                QueryUtil.executeUpdate(db2, sql);
                
                sql = 
                  "insert into tori_station_mst "
                + "with all as (select t2.* from certif_issue_dat t1 inner join certif_detail_eachtype_dat t2 on t2.year = t1.year and t2.certif_index = t1.certif_index where t1.certif_kindcd = '113' "
                + "), t_station as (select t2.remark1 as station_name from all t2 where t2.remark1 is not null union select t2.remark2 as station_name from all t2 where t2.remark2 is not null union select t2.remark3 as station_name from all t2 where t2.remark3 is not null "
                + ") "
                + "select "
                + "    right('000' || rtrim(cast(row_number() over() as char(3))), 3) as cd,"
                + "    t1.station_name,"
                + "    '駅' as kbn,"
                + "    row_number() over() as order "
                + " from t_station t1"
                ;
                QueryUtil.executeUpdate(db2, sql);
                
            } catch (SQLException e) {
                log.fatal("exception! sql = " + sql, e);
            }
        }

        public static void createTableToriTestcdMst(final DB2UDB db2) {
            String sql = null;
            try {
                if (QueryUtil.dbHasTable(db2, tori_testcd_mst)) {
                    return;
                }
                
                sql = " create table TORI_TESTCD_MST(YEAR VARCHAR(4) NOT NULL, SEMESTER VARCHAR(1) NOT NULL, TESTKINDCD VARCHAR(2) NOT NULL, TESTITEMCD VARCHAR(2) NOT NULL, TORI_TESTCD VARCHAR(2) NOT NULL ) in usr1dms index in idx1dms ";
                QueryUtil.executeUpdate(db2, sql);
                
                sql = " alter table TORI_TESTCD_MST add constraint PK_TORI_TESTCD_M primary key (YEAR,SEMESTER,TESTKINDCD,TESTITEMCD, TORI_TESTCD) ";
                QueryUtil.executeUpdate(db2, sql);
                
                sql = 
                  "insert into tori_testcd_mst "
                + "select year, semester, testkindcd, testitemcd, right('00' || rtrim(cast(row_number() over(partition by year order by year, semester, testkindcd, testitemcd) as char(2))), 2) as testcd from testitem_mst_countflg_new t1 order by year, semester, testkindcd, testitemcd "
                ;
                QueryUtil.executeUpdate(db2, sql);
                
            } catch (SQLException e) {
                log.fatal("exception! sql = " + sql, e);
            }
        }

        public static void createTableToriSubclassMst(final DB2UDB db2) {
            String sql = null;
            try {
                if (QueryUtil.dbHasTable(db2, tori_subclass_name_mst)) {
                    return;
                }
                
                sql = " create table tori_subclass_name_mst  ("
                        + " classcd varchar(2) not null,  "
                        + " school_kind varchar(2) not null,  "
                        + " curriculum_cd varchar(2) not null,  "
                        + " subclasscd varchar(6) not null,  "
                        + " classname varchar(30),  "
                        + " subclassname varchar(90),  "
                        + " subclassname_eng varchar(90) ) in usr1dms index in idx1dms ";
                QueryUtil.executeUpdate(db2, sql);
                
                sql = " alter table tori_subclass_name_mst add constraint PK_tori_SUBC_N_M primary key (classcd,school_kind,curriculum_cd,subclasscd) ";
                QueryUtil.executeUpdate(db2, sql);
                
                sql = 
                  "insert into tori_subclass_name_mst "
                + "with all as ( "
                + "select "
                + " t1.classcd, t1.school_kind, t1.curriculum_cd, t1.subclasscd, "
                + " t1.classcd || '-' || t1.school_kind || '-' || t1.curriculum_cd || '-' || t1.subclasscd as long_subclasscd, "
                + " t1.subclassname, "
                + " t1.subclassordername1, "
                + " t1.subclassname_eng, "
                + " t1.classcd || '-' || t1.school_kind || '-' || t1.curriculum_cd || '-' || t1.subclasscd2 as long_subclasscd2 "
                + "from subclass_mst t1 "
                + "inner join ( "
                + " select distinct t1.classcd, value(t2.subclasscd2, t1.subclasscd) as subclasscd from schreg_studyrec_dat t1 "
                + " left join subclass_mst t2 on t2.classcd = t1.classcd and t2.school_kind = t1.school_kind and t2.curriculum_cd = t1.curriculum_cd and t2.subclasscd = t1.subclasscd "
                + ") t2 on t2.classcd = t1.classcd and t2.subclasscd = t1.subclasscd "
                + "where t1.school_kind = 'H' "
                + "), combined as ( "
                + "select "
                + "  attend_classcd || '-' || attend_school_kind || '-' || attend_curriculum_cd || '-' || attend_subclasscd as long_subclasscd, "
                + "  max(combined_classcd || '-' || combined_school_kind || '-' || combined_curriculum_cd || '-' || combined_subclasscd) as long_subclasscd_saki "
                + "  from subclass_replace_combined_dat "
                + "  group by attend_classcd || '-' || attend_school_kind || '-' || attend_curriculum_cd || '-' || attend_subclasscd "
                + "), subclass_name_mst as ( "
                + "select distinct "
                + "  t1.classcd, "
                + "  t1.school_kind, "
                + "  t1.curriculum_cd, "
                + "  t1.subclasscd, "
                + "  value(t4.subclassordername1, t4.subclassname, t3.subclassordername1, t3.subclassname, t5.subclassordername1, t5.subclassname, t1.subclassordername1, t1.subclassname) as subclassname, "
                + "  value(t4.subclassname_eng, t3.subclassname_eng, t5.subclassname_eng, t1.subclassname_eng) as subclassname_eng "
                + "from all t1 "
                + "left join combined t2 on t2.long_subclasscd = t1.long_subclasscd "
                + "left join all t3 on t3.long_subclasscd = t2.long_subclasscd_saki "
                + "left join all t4 on t4.long_subclasscd = t3.long_subclasscd2 "
                + "left join all t5 on t5.long_subclasscd = t1.long_subclasscd2 "
                + ")"
                + "select "
                + "  t1.classcd, "
                + "  t1.school_kind, "
                + "  t1.curriculum_cd, "
                + "  t1.subclasscd, "
                + "  cast(null as varchar(1)) as classname, "
                + "  subclassname, "
                + "  subclassname_eng "
                + "from subclass_name_mst t1 "
                + "order by "
                + "t1.classcd "
                ;
                QueryUtil.executeUpdate(db2, sql);
                
            } catch (SQLException e) {
                log.fatal("exception! sql = " + sql, e);
            }
        }

        public static void createTableToriPromoterMst(final DB2UDB db2) {
            String sql = null;
            try {
                if (QueryUtil.dbHasTable(db2, tori_promoter_mst)) {
                    return;
                }
                
                sql = " create table tori_promoter_mst (promoter_cd varchar(4) not null, promoter varchar(150)) in usr1dms index in idx1dms ";
                QueryUtil.executeUpdate(db2, sql);
                
                sql = " alter table tori_promoter_mst add constraint tori_prom_m primary key (promoter_cd) ";
                QueryUtil.executeUpdate(db2, sql);
                
                sql = 
                  "insert into tori_promoter_mst "
                + "with tmp as (select min(qualified_cd) as promoter_cd, promoter from qualified_mst where promoter is not null group by promoter) "
                + "select right('000' || rtrim(cast(row_number() over(order by promoter_cd) as char(3))), 3), promoter from tmp "
                ;
                QueryUtil.executeUpdate(db2, sql);
                
            } catch (SQLException e) {
                log.fatal("exception! sql = " + sql, e);
            }
        }

        public static void createTableToriAttendRangeDat(final DB2UDB db2, final Param param) {
            String sql = null;
            try {
                if (QueryUtil.dbHasTable(db2, tori_attendrange_dat)) {
                    return;
                }
                String SEMESTER_DETAIL_MST = "SEMESTER_DETAIL_MST";
                if ("1".equals(param._altSemDet)) {
                    SEMESTER_DETAIL_MST = "SEMESTER_DETAIL_MST_DUMMY";
                    log.warn("SEMESTER_DETAIL_MSTの代わりに" + SEMESTER_DETAIL_MST + "を使用します。");
                }
                
                sql = " create table tori_attendrange_dat (year varchar(4) not null, semester varchar(1) not null, div varchar(1) not null, semester_detail varchar(1) not null, semestername varchar(60), sdate date, edate date) in usr1dms index in idx1dms ";
                QueryUtil.executeUpdate(db2, sql);
                
                sql = " alter table tori_attendrange_dat add constraint tori_attrng_d primary key (year, semester, div, semester_detail) ";
                QueryUtil.executeUpdate(db2, sql);
                
                sql = 
                  "insert into tori_attendrange_dat "
                + " select year, semester, '0', semester_detail, semestername, sdate, edate from " + SEMESTER_DETAIL_MST + " union all "
                + " select year, semester, '1', semester_detail, semestername, sdate, edate from " + SEMESTER_DETAIL_MST
                ;
                QueryUtil.executeUpdate(db2, sql);
                
            } catch (SQLException e) {
                log.fatal("exception! sql = " + sql, e);
            }
        }

        public static void postProcess(final DB2UDB db2) {
            final List list = new ArrayList();
            for (int i = 0; i < tmpTables.length; i++) {
                final String tabname = tmpTables[i];
                if (QueryUtil.dropTable(db2, tabname)) {
                    list.add(tabname);
                }
            }
            log.info(" dropped table : " + list);
        }
    }
    
    private static class QueryUtil {
        private static boolean dbHasTable(final DB2UDB db2, final String tablename) {
            boolean hasTable = false;
            try {
                final String sql = " SELECT '1' FROM SYSCAT.TABLES WHERE TABNAME = '" + tablename.toUpperCase() + "' ";
                hasTable = "1".equals(getOne(db2, sql));
                
            } catch (Exception e) {
                log.fatal("exception!", e);
            }
            log.info(" hasTable " + tablename + " = " + hasTable);
            return hasTable;
        }
        
        public static boolean dropTable(final DB2UDB db2, final String tabname) {
            boolean tableDropped = false;
            try {
                executeUpdate(db2, "drop table " + tabname);
                tableDropped = true;
            } catch (SQLException e) {
                if (e.getErrorCode() == -204) {
                    //log.info("一時テーブルを削除しようとしましたがテーブルがありませんでした。:" + tabname);
                } else {
                    log.fatal("exception! errorCode = " + e.getErrorCode(), e);
                }
            } finally {
//                if (tableDropped) {
//                    log.info(" table dropped: " + tabname);
//                }
            }
            return tableDropped;
        }

        private static Collection getRowList(final DB2UDB db2, final String sql) {
            Collection rtn = null;
            PreparedStatement ps = null;
            try {
                ps = db2.prepareStatement(sql);
                rtn = getRowList(db2, ps);
                
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
            }
            if (null == rtn) {
                rtn = new ArrayList();
            }
            return rtn;
        }

        private static Collection getRowList(final DB2UDB db2, final PreparedStatement ps) {
            return getRows(new ArrayList(), db2, ps, false);
        }

        private static Collection getRows(final Collection rows, final DB2UDB db2, final PreparedStatement ps, final boolean isOneColumn) {
            ResultSet rs = null;
            try {
                rs = ps.executeQuery();
                ResultSetMetaData meta = rs.getMetaData();
                while (rs.next()) {
                    rows.add(getRow(isOneColumn, meta, rs));
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return rows;
        }
        
        private static Object getRow(final boolean isOneColumn, final ResultSetMetaData meta, final ResultSet rs) throws SQLException {
            if (isOneColumn) {
                return rs.getString(1);
            }
            final Map m = new HashMap();
            for (int col = 1, max = meta.getColumnCount(); col <= max; col++) {
                m.put(meta.getColumnName(col), rs.getString(col));
            }
            return m;
        }

        private static String getOne(final DB2UDB db2, final String sql) throws SQLException {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn = rs.getString(1); // 1行目1列目の値
                }
            } catch (SQLException e) {
                throw e;
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
            return rtn;
        }

        private static int executeUpdate(final DB2UDB db2, final String sql) throws SQLException {
            int rtn = -1;
            PreparedStatement ps = null;
            try {
                ps = db2.prepareStatement(sql);
                rtn = ps.executeUpdate();

            } catch (SQLException e) {
                throw e;
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
            return rtn;
        }
        
        private static String getNameMst(final DB2UDB db2, final String year, final String namecd1, final String namecd2, final String field) {
            final String sql = " SELECT "
                    + field
                    + " FROM "
                    + "     V_NAME_MST "
                    + " WHERE "
                    + "     YEAR = '" + year + "' "
                    + "     AND NAMECD1 = '" + namecd1 + "' "
                    + "     AND NAMECD2 = '" + namecd2 + "'";
            String rtn = "";
            try {
                rtn = getOne(db2, sql);
            } catch (Exception e) {
                log.fatal("exception!", e);
            }
            return rtn;
        }
    }
    
    private static class OutputWrapper {
        private static final String outputEncodeName = "MS932";

        private final String _ext;
        
        private OutputStream _os;

        private Workbook _book = null;
        private Sheet _sheet = null;
        private CellStyle _style;
        private int _sheetIdx = 0;
        private final List _excellLines = new ArrayList();
        private int _excellLine = 0;

        private boolean _outputToFile = false;

        OutputWrapper() {
            _ext = ".xlsx";
        }
        
        private void closeQuietly() {
            try {
                if (null != _os) {
                    log.info(" line count = " + _excellLines + ", " + _excellLine);
                    StopWatch sw = new StopWatch();
                    sw.start();
                    _book.write(_os);
                    sw.stop();
                    log.info(" excel output done. elapsed = " + sw.toString());
                    _os.close();
                    _os = null;
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            }
        }
        
//        private static String join(final List columns, final String spl1) {
//            final StringBuffer stb = new StringBuffer();
//            String spl = "";
//            for (final Iterator it = columns.iterator(); it.hasNext();) {
//                final String s = (String) it.next();
//                stb.append(spl).append(StringUtils.defaultString(s));
//                spl = spl1;
//            }
//            return stb.toString();
//        }

        public void outputLines(final Collection lines) throws OutputExceptionWrapper {
            if (null == lines) {
                return;
            }
            try {
                for (final Iterator it = lines.iterator(); it.hasNext();) {
                    final List columnList = (List) it.next();
                    outputExcellRow(getExcellRow(_sheet, _excellLine), columnList);
                    _excellLine += 1;
                    
                    if (_excellLine > 30000 && _excellLine % 5000 == 0) {
                        Param.printMemoryInfo(" excelLine = " + _excellLine);
                    }
                }
            } catch (Throwable e) {
                if (e instanceof OutputExceptionWrapper) {
                    Param.printMemoryInfo("hoge");
                    throw (OutputExceptionWrapper) e;
                } else {
                    log.error("throwable", e);
                }
            }
        }
        
        private void outputExcellRow(final Row row, final List line) throws OutputExceptionWrapper {
            try {
                short colidx = 0;
                for (final Iterator it = line.iterator(); it.hasNext();) {
                    final String col = (String) it.next();
                    final Cell cell = getExcellCell(row, colidx);
                    cell.setCellStyle(_style);
                    cell.setCellValue(col);
                    colidx++;
                }
            } catch (Exception e) {
                throw new OutputExceptionWrapper(e);
            }
        }

        public void setNewBook() throws IOException {
            _book = new XSSFWorkbook("/root/ikou/template.xlsx");
            _sheetIdx = 0;
            _excellLines.clear();
            _excellLine = 0;
        }

        public String getDefaultSheetName() {
            return "Sheet " + String.valueOf(_sheetIdx);
        }

        public void setNewSheet(final String sheetName) {
            _sheet = OutputWrapper.getExcellSheet(_book, sheetName);
            log.info(" new sheet : " + sheetName);
            _sheetIdx += 1;
            _style = getExcellCell(getExcellRow(_sheet, 0), 0).getCellStyle();
            _style.setDataFormat(_book.createDataFormat().getFormat("text")); // text
            if (_excellLine > 0) {
                _excellLines.add(_excellLine);
            }
            _excellLine = 0;
        }
        
        private static Sheet getExcellSheet(final Workbook book, final String sheetName) {
            Sheet sheet = book.getSheet(sheetName);
            if (sheet == null) {
                sheet = book.createSheet(sheetName);
            }
            return sheet;
        }

        private static Row getExcellRow(final Sheet sheet, final int idx) {
            Row row = sheet.getRow(idx);
            if (row == null) {
                row = sheet.createRow(idx);
            }
            return row;
        }
        
        private static Cell getExcellCell(final Row row, final int idx) {
            Cell cell = row.getCell(idx);
            if (cell == null) {
                cell = row.createCell(idx);
            }
            return cell;
        }
        
        protected static class OutputExceptionWrapper extends Exception {
            final Exception _e;
            OutputExceptionWrapper(final Exception e) {
                _e = e;
            }
            public Exception get() {
                return _e;
            }
        }
    }
    
    private static class OutputFmt {
        
        String _cd;
        String[][] _titleLines;
        Fld[] _flds;
        List _createTableList;
        String _sql;
        Map _psMap;
        
        protected boolean _outputExcellSplitSheet = false;
        
        public void preprocess(final DB2UDB db2, final Param param) {
            if (_createTableList.contains(IkouUtil.tori_station_mst)) {
                IkouUtil.createTableToriStationMst(db2);
            }
            if (_createTableList.contains(IkouUtil.tori_testcd_mst)) {
                IkouUtil.createTableToriTestcdMst(db2);
            }
            if (_createTableList.contains(IkouUtil.tori_subclass_name_mst)) {
                IkouUtil.createTableToriSubclassMst(db2);
            }
            if (_createTableList.contains(IkouUtil.tori_promoter_mst)) {
                IkouUtil.createTableToriPromoterMst(db2);
            }
            if (_createTableList.contains(IkouUtil.tori_attendrange_dat)) {
                IkouUtil.createTableToriAttendRangeDat(db2, param);
            }
        }
        
        public void output(final DB2UDB db2, final Param param, final OutputWrapper ow) throws Exception {
            boolean initByCode = false;
            if (ow._outputToFile) {
                final String filename = param.getOutputFilename(db2, _cd, "", ow._ext);
                log.fatal(" create file " + filename);
                ow._os = new FileOutputStream(filename);
                ow.setNewBook();
                if (!_outputExcellSplitSheet) {
                    ow.setNewSheet(ow.getDefaultSheetName());
                }
                initByCode = true;
            }

            output0(db2, ow);

            if (initByCode) {
                ow.closeQuietly();
            }
        }

        public void output0(final DB2UDB db2, final OutputWrapper ow) throws OutputWrapper.OutputExceptionWrapper {
            PreparedStatement ps = null;
            try {
                
                ow.outputLines(getTitleLines(_titleLines, null));
                ow.outputLines(getHeaderLines(_flds));

                if (StringUtils.isEmpty(_sql)) {
                    log.info("cd = " + _cd + ":移行SQL無し");
                    return;
                }
                ps = db2.prepareStatement(_sql);
                
                ow.outputLines(getDataLines(_cd, ps, _flds));
                
            } catch (Exception e) {
                if (e instanceof OutputWrapper.OutputExceptionWrapper) {
                    throw (OutputWrapper.OutputExceptionWrapper) e;
                } else {
                    log.fatal("exception! cd = " + _cd + ", sql = " + _sql, e);
                }
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        protected static List getDataLines(final String cd, PreparedStatement ps, final Fld[] flds) throws SQLException {
            final List lines = new LinkedList();
            ResultSet rs = null;
            try {
                rs = ps.executeQuery();

                final ResultSetMetaData meta = rs.getMetaData();
                final int columnCount = meta.getColumnCount();
                if (columnCount != flds.length) {
                    log.fatal("cd = " + cd + ": SQLカラム数がヘッダカラム数と一致していません。 SQLカラム数 = " + columnCount + ", ヘッダカラム数 = " + flds.length);
                    for (int i = 0; i < Math.min(columnCount, flds.length); i++) {
                        log.warn(" column " + (i + 1) + " = " + meta.getColumnName(i + 1) + " <> " + flds[i]._name);
                    }
                }
                for (int coli = 0; coli < columnCount; coli++) {
                    flds[coli]._columnType = meta.getColumnType(coli + 1);
                }
                while (rs.next()) {
                    lines.add(getCsvDataColumns(flds, rs));
                }
                
            } catch (SQLException e) {
                throw e;
            } finally {
                DbUtils.closeQuietly(rs);
            }
            return lines;
        }
        
        private static List getCsvDataColumns(final Fld[] flds, final ResultSet rs) throws SQLException {
            final List columns = new ArrayList();
            for (int coli = 0; coli < flds.length; coli++) {
                if (flds[coli]._isDummy) {
                    columns.add("");
                    continue;
                }
                String v = rs.getString(coli + 1);
                if (null != v) {
                    final int type = flds[coli]._columnType;
                    if (type == Types.DATE) {
                        // 日付型はフォーマット
                        v = OutputFmt.formatDate(v);
                    } else if (type == Types.CHAR || type == Types.VARCHAR) {
                        // ダブルクォートを2重
                        v = OutputFmt.dupDQuote(v);
                    }
                }
                columns.add(v);
            }
            return columns;
        }

        protected List getHeaderLines(final Fld[] flds) {
            final List lines = new ArrayList();
            if (null != flds) {
                final List columns = new ArrayList();
                for (int i = 0; i < flds.length; i++) {
                    columns.add(flds[i]._name);
                }
                lines.add(columns);
            }
            return lines;
        }

        protected List getTitleLines(final String[][] titleLines, Map m) {
            final List lines = new LinkedList();
            if (null != titleLines) {
                for (int i = 0; i < titleLines.length; i++) {
                    final List columns = new ArrayList();
                    final String[] titleLine = titleLines[i];
                    for (int coli = 0; coli < titleLine.length; coli++) {
                        String title = titleLine[coli];
                        if (title.indexOf("{") != -1) {
                            m = null == m ? Collections.EMPTY_MAP : m;
                            final int oidx = title.indexOf("{");
                            final int cidx = title.indexOf("}");
                            final String os = title.substring(0, oidx);
                            final String cs = cidx == title.length() - 1 ? "" : title.substring(cidx + 1);
                            final String templItemName = title.substring(oidx + 1, cidx);
                            title = os + StringUtils.defaultString(getString(templItemName, m), "")  + cs;
                        }
                        columns.add(title);
                    }
                    lines.add(columns);
                }
            }
            return lines;
        }
        
        protected void setPs(final String name, final DB2UDB db2, final String sql) {
            try {
                final PreparedStatement ps = db2.prepareStatement(sql);
                if (null == _psMap) {
                    _psMap = new HashMap();
                }
                _psMap.put(name, ps);
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            }
        }

        protected PreparedStatement getPs(final String name) {
            if (null == _psMap) {
                return null;
            }
            return (PreparedStatement) _psMap.get(name);
        }

        protected PreparedStatement setPsArg(final PreparedStatement ps, final String[] args) {
            try {
                for (int i = 0; i < args.length; i++) {
                    ps.setString(i + 1, args[i]);
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            }
            return ps;
        }

        protected void closePsQuietly() {
            if (null == _psMap) {
                return;
            }
            for (final Iterator it = _psMap.values().iterator(); it.hasNext();) {
                final PreparedStatement ps = (PreparedStatement) it.next();
                DbUtils.closeQuietly(ps);
            }
            _psMap.clear();
            _psMap = null;
        }
        
        protected static List blankLine() {
            final List list = new ArrayList();
            list.add(Collections.EMPTY_LIST);
            return list;
        }
        
        protected static String getString(final String field, final Map m) {
            return (String) m.get(field);
        }
        

        private static final DateFormat dateformat = new SimpleDateFormat("yyyy/MM/dd");
        static String formatDate(final String date) {
            return dateformat.format(Date.valueOf(date));
        }
        private static final DateFormat dateformatShort = new SimpleDateFormat("MM/dd");
        static String formatDateShort(final String date) {
            return dateformatShort.format(Date.valueOf(date));
        }

        static String dupDQuote(final String v) {
            return StringUtils.replace(v, "\"", "\"\"");
        }
    }
    
    // _40_成績点数
    private static class OutputFmt40 extends OutputFmtDiv {
        
        String _sqlTestKind;
        String _sqlMock;
        
        private static class F40 implements SheetFormatter {
            public void setPs(final PreparedStatement ps, final Map map) throws SQLException {
                ps.setString(1, getString("YEAR", map));
                ps.setString(2, getString("TESTCD", map));
            }
            public String formatSheetName(final Map map) {
                return getString("YEAR", map) + "年度" + getString("TESTCD", map);
            }
        }
        
        private static String divYearSql(final Param param) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" select distinct t1.year ");
            sql.append(" from tori_testcd_mst t1 ");
            sql.append(" where not (t1.semester = '9' and t1.testkindcd = '99' and t1.testitemcd = '00') ");
            sql.append(" union ");
            sql.append(" select distinct t2.year ");
            sql.append(" from mock_mst t1 ");
            sql.append(" inner join mock_dat t2 on t2.mockcd = t1.mockcd ");
            sql.append(" inner join (select distinct year, schregno from schreg_regd_dat) sregd on sregd.year = t2.year and sregd.schregno = t2.schregno ");
            sql.append(" order by year ");
            return sql.toString();
        }
        
        private static String divSql(final Param param) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" select t1.year, 1 as div, t1.tori_testcd as testcd, t2.testitemname as testname ");
            sql.append(" from tori_testcd_mst t1 ");
            sql.append(" left join testitem_mst_countflg_new t2 on t2.year = t1.year and t2.semester = t1.semester and t2.testkindcd = t1.testkindcd and t2.testitemcd = t1.testitemcd ");
            sql.append(" where t1.year = ? and not (t1.semester = '9' and t1.testkindcd = '99' and t1.testitemcd = '00') ");
            sql.append(" union all ");
            sql.append(" select distinct t2.year, 2 as div, t3.seq as testcd, t3.name1 as testname ");
            sql.append(" from mock_mst t1 ");
            sql.append(" inner join mock_dat t2 on t2.mockcd = t1.mockcd ");
            sql.append(" inner join (select distinct year, schregno from schreg_regd_dat) sregd on sregd.year = t2.year and sregd.schregno = t2.schregno ");
            sql.append(" left join tori_mock_mst t3 on t3.year = t2.year and t3.mockcd = t1.mockcd ");
            sql.append(" where t2.year = ? ");
            sql.append(" order by year, div, testcd ");
            return sql.toString();
        }
        
        OutputFmt40(final Param param) {
            super(divSql(param), new F40());
            super._outputExcellSplitSheet = true;

            final StringBuffer stb1 = new StringBuffer();
            stb1.append(" with chair_std as ( ");
            stb1.append(" select ");
            stb1.append("   t1.year, ");
            stb1.append("   t2.schregno, ");
            stb1.append("   min(t1.chaircd) as chaircd, ");
            stb1.append("   t1.classcd, t1.school_kind, t1.curriculum_cd, t1.subclasscd "); // 内部
            stb1.append(" from chair_dat t1 ");
            stb1.append(" inner join chair_std_dat t2 on t2.year = t1.year and t2.semester = t1.semester and t2.chaircd = t1.chaircd ");
            stb1.append(" group by ");
            stb1.append("   t1.year, ");
            stb1.append("   t2.schregno, ");
            stb1.append("   t1.classcd, t1.school_kind, t1.curriculum_cd, t1.subclasscd ");
            stb1.append(" )select ");
            stb1.append("   '' as idou, ");
            stb1.append("   t1.year, ");
            stb1.append("   t3.chaircd, ");
            stb1.append("   '' as chairname, ");
            stb1.append("   t1.schregno, ");
            stb1.append("   '' as attendno, ");
            stb1.append("   '' as jukou_bangou, ");
            stb1.append("   '' as schreg_name, ");
            stb1.append("   t1.score, ");
            stb1.append("   '' as jisshi_joukyou, ");
            stb1.append("   t1.value ");
            stb1.append(" from record_score_dat t1 ");
            stb1.append(" inner join tori_testcd_mst t2 on t2.year = t1.year and t2.semester = t1.semester and t2.testkindcd = t1.testkindcd and t2.testitemcd = t1.testitemcd ");
            stb1.append(" inner join chair_std t3 on t3.year = t1.year and t3.schregno = t1.schregno and t3.classcd = t1.classcd and t3.school_kind = t1.school_kind and t3.curriculum_cd = t1.curriculum_cd and t3.subclasscd = t1.subclasscd ");
            stb1.append(" where ");
            stb1.append(" t1.year = ? ");
            stb1.append(" and t2.tori_testcd = ? ");
            stb1.append(" and not (t1.semester = '9' and t1.testkindcd = '99' and t1.testitemcd = '00') ");
            stb1.append(" order by ");
            stb1.append(" t2.tori_testcd, t1.year, t1.semester, t1.chaircd, t1.schregno ");
            _sqlTestKind = stb1.toString();
            
            final StringBuffer stb2 = new StringBuffer();
            stb2.append(" select ");
            stb2.append("   '' as idou, ");
            stb2.append("   t2.year, ");
            stb2.append("   t2.mock_subclass_cd as chaircd, ");
            stb2.append("   t3.subclass_name as chairname, ");
            stb2.append("   t2.schregno, ");
            stb2.append("   '' as attendno, ");
            stb2.append("   '' as jukou_bangou, ");
            stb2.append("   '' as schreg_name, ");
            stb2.append("   t2.score, ");
            stb2.append("   '' as jisshi_joukyou, ");
            stb2.append("   '' as value ");
            stb2.append(" from mock_mst t1 ");
            stb2.append(" inner join mock_dat t2 on t2.mockcd = t1.mockcd ");
            stb2.append(" inner join (select distinct year, schregno from schreg_regd_dat) sregd on sregd.year = t2.year and sregd.schregno = t2.schregno ");
            stb2.append(" left join tori_mock_mst tm on tm.year = t2.year and tm.mockcd = t1.mockcd ");
            stb2.append(" inner join mock_subclass_mst t3 on t3.mock_subclass_cd = t2.mock_subclass_cd ");
            stb2.append(" where ");
            stb2.append("   t2.year = ? ");
            stb2.append("   and tm.seq = ? ");
            stb2.append("   and (t2.score is not null or t2.score_di is not null) ");
            stb2.append(" order by ");
            stb2.append("   t2.mock_subclass_cd, t2.schregno ");
            _sqlMock = stb2.toString();
        }
        
        public void output(final DB2UDB db2, final Param param, final OutputWrapper ow) throws Exception {
            outputByDiv40(db2, param, ow);
        }
        
        public void outputByDiv40(final DB2UDB db2, final Param param, final OutputWrapper ow) throws OutputWrapper.OutputExceptionWrapper {
            
            final Collection divYearList = QueryUtil.getRowList(db2, divYearSql(param));
            for (final Iterator it = divYearList.iterator(); it.hasNext();) {
                final Map yearMap = (Map) it.next();
                final String year = getString("YEAR", yearMap);
                
                String psKey = "YEAR_SQL";
                if (null == getPs(psKey)) {
                    setPs(psKey, db2, _divSql);
                }
                
                boolean initByCode = false;
                
                try {
                    if (ow._outputToFile) {
                        final String filename = param.getOutputFilename(db2, _cd, year + "年度", ow._ext);
                        log.fatal(" create file " + filename);
                        ow._os = new FileOutputStream(filename);
                        ow.setNewBook();
                        if (ow._outputToFile && _outputExcellSplitSheet) {
                            ow._book.removeSheetAt(0);
                        } else if (!_outputExcellSplitSheet) {
                            ow.setNewSheet(ow.getDefaultSheetName());
                        }
                        initByCode = true;
                    }
                } catch (Exception e) {
                    if (e instanceof OutputWrapper.OutputExceptionWrapper) {
                        throw (OutputWrapper.OutputExceptionWrapper) e;
                    } else {
                        log.fatal("exception! cd = " + _cd + ", sql = " + _sql, e);
                    }
                }
                
                final Collection divList = QueryUtil.getRowList(db2, setPsArg(getPs(psKey), new String[] {year, year}));

                PreparedStatement ps1 = null;
                PreparedStatement ps2 = null;
                PreparedStatement ps = null;
                try {
                    ps1 = db2.prepareStatement(_sqlTestKind);
                    ps2 = db2.prepareStatement(_sqlMock);

                    boolean outputBefore = false;
                    for (final Iterator dit = divList.iterator(); dit.hasNext();) {
                        final Map divMap = (Map) dit.next();
                        
                        if ("1".equals(getString("DIV", divMap))) {
                            ps = ps1;
                        } else if ("2".equals(getString("DIV", divMap))) {
                            ps = ps2;
                        }

                        _sheetFormatter.setPs(ps, divMap);
                        
                        final List lines = getDataLines(_cd, ps, _flds);
                        if (lines.size() > 0) {
                            if (_outputExcellSplitSheet) {
                                ow.setNewSheet(_sheetFormatter.formatSheetName(divMap));
                            } else if (!_outputExcellSplitSheet && outputBefore) {
                                ow.outputLines(blankLine());
                            }
                            
                            ow.outputLines(getTitleLines(_titleLines, divMap));
                            ow.outputLines(getHeaderLines(_flds));
                            ow.outputLines(lines);
                            outputBefore = true;
                        }
                        
                    }
                    
                    if (!outputBefore) {
                        ow.setNewSheet(ow.getDefaultSheetName());
                    }
                    
                } catch (Exception e) {
                    if (e instanceof OutputWrapper.OutputExceptionWrapper) {
                        throw (OutputWrapper.OutputExceptionWrapper) e;
                    } else {
                        log.fatal("exception! cd = " + _cd + ", sql = " + _sql, e);
                    }
                } finally {
                    DbUtils.closeQuietly(ps1);
                    DbUtils.closeQuietly(ps2);
                    db2.commit();
                    
                    if (initByCode) {
                        ow.closeQuietly();
                    }
                }
            }
        }
    }
    
    // _43_年間成績情報
    private static class OutputFmt43 extends OutputFmt {
        
        OutputFmt43() {
            final StringBuffer stb = new StringBuffer();
            stb.append("  with MAX_SEMESTER AS ( ");
            stb.append(" select t1.* ");
            stb.append(" from schreg_regd_dat t1 ");
            stb.append(" inner join ( ");
            stb.append(" SELECT YEAR, SCHREGNO, MAX(SEMESTER) AS SEMESTER  ");
            stb.append("    FROM SCHREG_REGD_DAT  ");
            stb.append("    GROUP BY YEAR, SCHREGNO  ");
            stb.append(" ) t2 on t2.schregno = t1.schregno and t2.year = t1.year and t2.semester = t1.semester ");
            stb.append("  )  ");
            stb.append("  , DATA AS( ");
            stb.append("  SELECT  T1.SCHREGNO, T1.YEAR, T1.ANNUAL, T1.CLASSCD , ");
            stb.append(" t1.school_kind, t1.curriculum_cd ");
            stb.append("         ,T1.SUBCLASSCD, VALUATION AS GRADES  ");
            stb.append("         ,CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END AS CREDIT  ");
            stb.append("         ,VALUE(T1.COMP_CREDIT, 0) AS COMP_CREDIT  ");
            stb.append("         ,CLASSNAME,CLASSNAME_ENG,SUBCLASSNAME,SUBCLASSNAME_ENG  ");
            stb.append("         ,T1.SCHOOLCD  ");
            stb.append("  FROM    SCHREG_STUDYREC_DAT T1  ");
            stb.append("  WHERE    ");
            stb.append("       T1.YEAR = ? AND (T1.CLASSCD BETWEEN '01' AND '89' OR T1.CLASSCD = '89')  ");
            stb.append("  and schoolcd <> '2' ");
            stb.append("  ) ");
            stb.append(" ,DATA2 AS( ");
            stb.append("  SELECT T1.SCHREGNO,T1.YEAR,T1.ANNUAL,T1.CLASSCD, ");
            stb.append("  t1.school_kind, ");
            stb.append("  t1.curriculum_cd ");
            stb.append("        ,VALUE(T3.SUBCLASSCD2, T1.SUBCLASSCD) AS SUBCLASSCD ");
            stb.append("      , t4.remark ,T1.SCHOOLCD,T1.GRADES,T1.CREDIT,T1.COMP_CREDIT,T1.CLASSNAME,T1.CLASSNAME_ENG,T1.SUBCLASSNAME,T1.SUBCLASSNAME_ENG ");
            stb.append("  FROM DATA T1 ");
            stb.append("  LEFT JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("  and t3.classcd = t1.classcd and t3.school_kind = t1.school_kind and t3.curriculum_cd = t1.curriculum_cd  ");
            stb.append(" left join studyrecremark_dat t4 on t4.year = t1.year and t4.schregno = t1.schregno and t4.classcd = t1.classcd and t4.school_kind = t1.school_kind and t4.curriculum_cd= t1.curriculum_cd and t4.subclasscd = t1.subclasscd ");
            stb.append("  ), studyrec as ( ");
            stb.append("  SELECT T1.SCHREGNO,T1.YEAR,T1.ANNUAL,T1.CLASSCD, t1.school_kind, t1.curriculum_cd ");
            stb.append("        ,T1.SUBCLASSCD ");
            stb.append("        ,case when COUNT(*) = 1 then MAX(T1.GRADES)  ");
            stb.append("             when SC.GVAL_CALC = '0' then ROUND(AVG(FLOAT(case when 0 < T1.GRADES then GRADES end)),0) ");
            stb.append("             when SC.GVAL_CALC = '1' and 0 < SUM(case when 0 < T1.GRADES then CREDIT end) then ROUND(FLOAT(SUM((case when 0 < T1.GRADES then GRADES end) * T1.CREDIT)) / SUM(case when 0 < T1.GRADES then CREDIT end),0) ");
            stb.append("             else MAX(T1.GRADES) end AS GRADES ");
            stb.append("        ,SUM(T1.CREDIT) AS CREDIT ");
            stb.append("        ,SUM(T1.COMP_CREDIT) AS COMP_CREDIT ");
            stb.append("        ,MIN(T1.CLASSNAME) AS CLASSNAME ");
            stb.append("        ,MIN(T1.CLASSNAME_ENG) AS CLASSNAME_ENG ");
            stb.append("        ,MIN(T1.SUBCLASSNAME) AS SUBCLASSNAME ");
            stb.append("        ,MIN(T1.SUBCLASSNAME_ENG) AS SUBCLASSNAME_ENG ");
            stb.append("        ,SCHOOLCD ");
            stb.append("        ,max(remark) as remark ");
            stb.append("  FROM DATA2 T1  ");
            stb.append("         LEFT JOIN SCHOOL_MST SC ON SC.YEAR = T1.YEAR  ");
            stb.append("  GROUP BY T1.SCHREGNO,T1.YEAR,T1.ANNUAL,T1.CLASSCD, t1.school_kind, t1.curriculum_cd ");
            stb.append("           ,T1.SUBCLASSCD,SC.GVAL_CALC , schoolcd ");
            stb.append(" ) ");
            stb.append(" select  ");
            stb.append(" T1.YEAR, ");
            stb.append(" T1.SCHREGNO, ");
            stb.append(" T3.name, ");
            stb.append(" T1.ANNUAL, ");
            stb.append(" value(hnkn.subclasscd, T1.SUBCLASSCD) as subclasscd, ");
            stb.append(" value(T1.SUBCLASSNAME, t4.subclassordername1, t4.subclassname) as subclassname, ");
            stb.append(" T1.GRADES, ");
            stb.append(" T1.CREDIT, ");
            stb.append(" case when t1.grades is null or t1.grades = 0 or t1.grades = 1 or ");
            stb.append("  t1.comp_credit is null or t1.comp_credit = 0 then t5.credits ");
            stb.append("  else 0 end as mirishu_credit, ");
            stb.append(" T1.COMP_CREDIT, ");
            stb.append(" t1.remark, ");
            stb.append(" case when t1.schoolcd = '1' then '○' end, ");
            stb.append("  '' ");
            stb.append(" from studyrec t1 ");
            stb.append(" left join MAX_SEMESTER t2 on t2.year = t1.year and t2.schregno = t1.schregno ");
            stb.append(" inner join schreg_base_mst t3 on t3.schregno = t1.schregno ");
            stb.append(" left join credit_mst t5 on t5.year = t1.year and t5.grade = t2.grade and t5.coursecd = t2.coursecd and t5.majorcd = t2.majorcd and t5.coursecode = t2.coursecode and t5.classcd = t1.classcd and t5.school_kind = t1.school_kind and t5.curriculum_cd = t1.curriculum_cd and t5.subclasscd = t1.subclasscd ");
            stb.append(" left join subclass_mst t4 on t4.classcd = t1.classcd and t4.school_kind = t1.school_kind and t4.curriculum_cd = t1.curriculum_cd and t4.subclasscd = t1.subclasscd ");
            stb.append(" left join tori_subclass_henkan hnkn on hnkn.classcd = t1.classcd and hnkn.school_kind = t1.school_kind and hnkn.curriculum_cd = t1.curriculum_cd and hnkn.subclasscd = t1.subclasscd ");
            stb.append(" order by ");
            stb.append(" T1.YEAR, ");
            stb.append(" T1.SCHREGNO, ");
            stb.append(" T3.name, ");
            stb.append(" T1.ANNUAL, ");
            stb.append(" T1.SUBCLASSCD ");
            _sql = stb.toString();
        }
        
        public void output(final DB2UDB db2, final Param param, final OutputWrapper ow) throws Exception {
            outputByDiv43(db2, param, ow);
        }
        
        public void outputByDiv43(final DB2UDB db2, final Param param, final OutputWrapper ow) throws OutputWrapper.OutputExceptionWrapper {
            
            final Collection divYearList = QueryUtil.getRowList(db2, " select distinct t1.year from SCHREG_STUDYREC_DAT t1 order by t1.year ");
            for (final Iterator it = divYearList.iterator(); it.hasNext();) {
                final Map yearMap = (Map) it.next();
                final String year = getString("YEAR", yearMap);
                
                String psKey = "YEAR_SQL";
                if (null == getPs(psKey)) {
                    setPs(psKey, db2, _sql);
                }
                
                boolean initByCode = false;
                
                try {
                    if (ow._outputToFile) {
                        final String filename = param.getOutputFilename(db2, _cd, year + "年度", ow._ext);
                        log.fatal(" create file " + filename);
                        ow._os = new FileOutputStream(filename);
                        ow.setNewBook();
                        if (ow._outputToFile && _outputExcellSplitSheet) {
                            ow._book.removeSheetAt(0);
                        } else if (!_outputExcellSplitSheet) {
                            ow.setNewSheet(ow.getDefaultSheetName());
                        }
                        initByCode = true;
                    }
                    
                    final Collection lines = getDataLines(_cd, setPsArg(getPs(psKey), new String[] {year}), _flds);

                    if (lines.size() > 0) {
                        
                        ow.outputLines(getTitleLines(_titleLines, yearMap));
                        ow.outputLines(getHeaderLines(_flds));
                        ow.outputLines(lines);
                    }
                } catch (Exception e) {
                    if (e instanceof OutputWrapper.OutputExceptionWrapper) {
                        throw (OutputWrapper.OutputExceptionWrapper) e;
                    } else {
                        log.fatal("exception! cd = " + _cd + ", sql = " + _sql, e);
                    }
                } finally {
                    if (initByCode) {
                        ow.closeQuietly();
                    }
                }
            }
        }
    }
    
    // _48_集計値講座
    private static class OutputFmt48 extends OutputFmt {
        public void output(final DB2UDB db2, final Param param, final OutputWrapper ow) throws Exception { // output0ではなくoutputをオーバーライドする
            final Map paramMap = new HashMap();
            paramMap.put("useCurriculumcd", "1");
            //各学校における定数等設定
            final KNJDefineCode _definecode = new KNJDefineCode();
            //各学校における定数等設定
            final KNJDefineSchool _defineSchoolCode = new KNJDefineSchool();

            final String div = "0"; // 0:講座 1:クラス _47_集計出欠期間D
            final String attendRangeYearSql = " select distinct year from tori_attendrange_dat t1 where div = '" + div + "' and year in (select distinct year from chair_dat) order by year ";
            final Collection attendRangeYearList = QueryUtil.getRowList(db2, attendRangeYearSql);
            
            log.info(" attendRangeYear count = " + attendRangeYearList.size());
            for (final Iterator yit = attendRangeYearList.iterator(); yit.hasNext();) { // attendRangeYearList
                final Map attendRangeYear = (Map) yit.next();
                final String year = getString("YEAR", attendRangeYear);
                
                boolean initByCode = false;
                if (ow._outputToFile) {
                    final String filename = param.getOutputFilename(db2, _cd, "_" + year + "年度", ow._ext);
                    log.info(" 48 :: create file " + filename);
                    ow._os = new FileOutputStream(filename);
                    ow.setNewBook();
                    ow.setNewSheet(ow.getDefaultSheetName());
                    initByCode = true;
                }

                final String attendRangeSql = " select * from tori_attendrange_dat t1 where year = '" + year + "' and div = '" + div + "' order by year, semester, semester_detail, sdate ";
                final Collection attendRangeList = QueryUtil.getRowList(db2, attendRangeSql);
                
                try {
                    log.info(" attendRange year = " + year + ", count = " + attendRangeList.size());
                    for (final Iterator it = attendRangeList.iterator(); it.hasNext();) { // attendRangeList
                        final Map attendRange = (Map) it.next();

                        output(db2, ow, paramMap, _definecode, _defineSchoolCode, attendRange);
                    }

                } catch (Exception e) {
                    if (e instanceof OutputWrapper.OutputExceptionWrapper) {
                        throw (OutputWrapper.OutputExceptionWrapper) e;
                    } else {
                        log.fatal("exception! cd = " + _cd + ", sql = " + _sql, e);
                    }
                } finally {
                    if (initByCode) {
                        ow.closeQuietly();
                    }
                }

            } // attendRangeYearList
        }

        public void output(final DB2UDB db2,
                final OutputWrapper ow,
                final Map paramMap,
                final KNJDefineCode _definecode,
                final KNJDefineSchool _defineSchoolCode,
                final Map attendRange) throws SQLException, ParseException, servletpack.KNJK.KNJK_TR14.OutputWrapper.OutputExceptionWrapper {

            final String year = getString("YEAR", attendRange);
            final String semester = getString("SEMESTER", attendRange);
            final String sdate = getString("SDATE", attendRange);
            final String edate= getString("EDATE", attendRange);
            
            _defineSchoolCode.defineCode(db2, year);
            KNJSchoolMst _knjSchoolMst = null;
            try {
                _knjSchoolMst = new KNJSchoolMst(db2, year);
            } catch (Exception e) {
                log.fatal("exception!", e);
            }
            
            final Map attendDataMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            
            try {
                final String z010Name1 = QueryUtil.getNameMst(db2, year, "Z010", "00", "NAME1");
                final Map attendSemAllMap = AttendAccumulate.getAttendSemesMap(db2, z010Name1, year);
                final String periodInState = AttendAccumulate.getPeiodValue(db2, _definecode, year, semester, semester);
                final Map hasuuMap = AttendAccumulate.getHasuuMap(attendSemAllMap, sdate, edate);
                final boolean semesFlg = ((Boolean) hasuuMap.get("semesFlg")).booleanValue();

                final String sql = AttendAccumulate.getAttendSubclassSql(
                        semesFlg,
                        _defineSchoolCode,
                        _knjSchoolMst,
                        year,
                        semester,
                        semester,
                        (String) hasuuMap.get("attendSemesInState"),
                        periodInState,
                        (String) hasuuMap.get("befDayFrom"),
                        (String) hasuuMap.get("befDayTo"),
                        (String) hasuuMap.get("aftDayFrom"),
                        (String) hasuuMap.get("aftDayTo"),
                        null,
                        null,
                        null,
                        paramMap);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd = rs.getString("SUBCLASSCD"); // 内部
                    if (null == attendDataMap.get(subclasscd)) {
                        attendDataMap.put(subclasscd, new TreeMap());
                    }
                    final Map subclassMap = (Map) attendDataMap.get(subclasscd);
                    final Attendance attendance = new Attendance();
                    attendance.schregno = rs.getString("SCHREGNO");
                    attendance.lesson = rs.getString("LESSON");
                    attendance.sick1 = rs.getString("SICK1");
                    attendance.late = rs.getString("LATE");
                    attendance.early = rs.getString("EARLY");
                    attendance.absent = rs.getString("ABSENT");
                    attendance.suspend = String.valueOf(rs.getInt("SUSPEND"));
                    attendance.mourning = String.valueOf(rs.getInt("MOURNING"));
                    subclassMap.put(rs.getString("SCHREGNO"), attendance);
                }
            } catch (Exception e) {
                if (e instanceof OutputWrapper.OutputExceptionWrapper) {
                    throw (OutputWrapper.OutputExceptionWrapper) e;
                } else {
                    log.fatal("exception! cd = " + _cd + ", sql = " + _sql, e);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
                
            //log.info(" attendRange = " + attendRange + ", subclassMap size = " + attendDataMap.size());

            final Collection chairList = getChairList(db2, year, semester);
            for (final Iterator itc = chairList.iterator(); itc.hasNext();) {
                final Map chair = (Map) itc.next();
                
                final String subclasscd = getString("SUBCLASSCD", chair); // 内部
                final Map subclassMap = (Map) attendDataMap.get(subclasscd);
                if (null != subclassMap) {
                    final Collection chairStudentSchregnos = getChairStudentSchregnos(db2, year, semester, getString("CHAIRCD", chair));
                    if (chairStudentSchregnos.size() == 0) {
                        // 講座名簿無し
                        continue;
                    }
                    final Collection attendList = getAttendListWithChairStudent(subclassMap, chairStudentSchregnos);
                    if (attendList.size() == 0) {
                        // 出欠情報無し
                        continue;
                    }
                    
                    final List lines = new LinkedList();
                    final Map titleParam = new HashMap();
                    titleParam.putAll(attendRange);
                    titleParam.putAll(chair);
                    
                    lines.addAll(getTitleLines(_titleLines, titleParam));
                    lines.addAll(getHeaderLines(_flds));

                    for (final Iterator attit = attendList.iterator(); attit.hasNext();) {
                        final Attendance attendance = (Attendance) attit.next();
                        final List columns = new ArrayList();
                        columns.add(null);
                        columns.add(null);
                        columns.add(null);
                        columns.add(attendance.schregno);
                        columns.add(null);
                        columns.add(null);
                        columns.add(attendance.sick1);
                        columns.add(attendance.late);
                        columns.add(attendance.early);
                        columns.add(attendance.absent);
                        columns.add(attendance.suspend);
                        columns.add(attendance.mourning);
                        columns.add(attendance.lesson);
                        lines.add(columns);
                    }
                    lines.add(Collections.EMPTY_LIST); // 空行
                    ow.outputLines(lines);
                }
            }
        }

        public Collection getChairList(final DB2UDB db2, final String year, final String semester) throws SQLException {
            final String psKey = "CHAIR";
            if (null == getPs(psKey)) {
                final String chairSql = " select chaircd, chairname, classcd || '-' || school_kind || '-' || curriculum_cd || '-' || subclasscd as subclasscd from chair_dat t1 where year = ? and semester = ? order by chaircd ";
                setPs(psKey, db2, chairSql);
            }
            final PreparedStatement ps = getPs(psKey);
            ps.setString(1, year);
            ps.setString(2, semester);
            final Collection chairList = QueryUtil.getRowList(db2, ps);
            return chairList;
        }

        private Collection getChairStudentSchregnos(final DB2UDB db2, final String year, final String semester, final String chaircd) throws SQLException {
            final String psKey = "CHAIR_STUDENT";
            if (null == getPs(psKey)) {

                final StringBuffer stb = new StringBuffer();
                stb.append(" select distinct ");
                stb.append("   t1.schregno ");
                stb.append(" from chair_std_dat t1 ");
                stb.append(" inner join chair_dat t2 on t2.year = t1.year and t2.semester = t1.semester and t2.chaircd = t1.chaircd ");
                stb.append(" where ");
                stb.append("   t1.year = ? and t1.semester = ? ");
                stb.append(" group by ");
                stb.append("   t1.schregno, ");
                stb.append("   t2.classcd, t2.school_kind, t2.curriculum_cd, t2.subclasscd  ");
                stb.append(" having  ");
                stb.append("  min(t1.chaircd) = ? ");
                setPs(psKey, db2, stb.toString());
            }
            final PreparedStatement ps = getPs(psKey);
            ps.setString(1, year);
            ps.setString(2, semester);
            ps.setString(3, chaircd);
            return QueryUtil.getRows(new TreeSet(), db2, ps, true);
        }

        private static Collection getAttendListWithChairStudent(final Map subclassMap, final Collection chairStudentSchregnos) {
            final List rtn = new LinkedList();
            for (final Iterator it = chairStudentSchregnos.iterator(); it.hasNext();) {
                final String schregno = (String) it.next();
                final Object att = subclassMap.get(schregno);
                if (null == att) {
                    continue;
                }
                rtn.add(att);
            }
            return rtn;
        }
        
        private static String max(final String v1, final String v2) {
            if (null == v1) return v2;
            if (null == v2) return v1;
            return String.valueOf(Math.max(Integer.parseInt(v1), Integer.parseInt(v2)));
        }

        private static class Attendance {
            String schregno;
            String lesson;
            String sick1;
            String late;
            String early;
            String absent;
            String suspend;
            String mourning;
        }
    }
    
    // _49_集計値クラス
    private static class OutputFmt49 extends OutputFmt {
        public void output0(final DB2UDB db2, final OutputWrapper ow) throws OutputWrapper.OutputExceptionWrapper {
            final Map paramMap = new HashMap();
            paramMap.put("useCurriculumcd", "1");
            //各学校における定数等設定
            final KNJDefineCode _definecode = new KNJDefineCode();
            //各学校における定数等設定
            final KNJDefineSchool _defineSchoolCode = new KNJDefineSchool();

            final String div = "1"; // 0:講座 1:クラス _47_集計出欠期間D
            final String attendRangeSql = " select * from tori_attendrange_dat t1 where div = '" + div + "' order by year, semester, semester_detail, sdate ";
            final Collection attendRangeList = QueryUtil.getRowList(db2, attendRangeSql);

            log.info(" attendRangeList = " + attendRangeList);
        
            for (final Iterator it = attendRangeList.iterator(); it.hasNext();) {
                final Map attendRange = (Map) it.next();
                final String year = getString("YEAR", attendRange);
                final String semester = getString("SEMESTER", attendRange);
                final String sdate = getString("SDATE", attendRange);
                final String edate= getString("EDATE", attendRange);
                
                _defineSchoolCode.defineCode(db2, year);
                KNJSchoolMst _knjSchoolMst = null;
                try {
                    _knjSchoolMst = new KNJSchoolMst(db2, year);
                } catch (Exception e) {
                    log.fatal("exception!", e);
                }
                
                final Map attendDataMap = new HashMap();
                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    final String z010Name1 = QueryUtil.getNameMst(db2, year, "Z010", "00", "NAME1");
                    final Map attendSemAllMap = AttendAccumulate.getAttendSemesMap(db2, z010Name1, year);
                    final String periodInState = AttendAccumulate.getPeiodValue(db2, _definecode, year, semester, semester);
                    final Map hasuuMap = AttendAccumulate.getHasuuMap(attendSemAllMap, sdate, edate);
                    final boolean semesFlg = ((Boolean) hasuuMap.get("semesFlg")).booleanValue();

                    final String sql = AttendAccumulate.getAttendSemesSql(
                            semesFlg,
                            _defineSchoolCode,
                            _knjSchoolMst,
                            year,
                            semester,
                            semester,
                            (String) hasuuMap.get("attendSemesInState"),
                            periodInState,
                            (String) hasuuMap.get("befDayFrom"),
                            (String) hasuuMap.get("befDayTo"),
                            (String) hasuuMap.get("aftDayFrom"),
                            (String) hasuuMap.get("aftDayTo"),
                            null,
                            null,
                            null,
                            "SEMESTER",
                            paramMap);
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String schregno = rs.getString("SCHREGNO");
                        if (null == attendDataMap.get(schregno)) {
                            attendDataMap.put(schregno, new TreeMap());
                        }
                        final Attendance attendance = new Attendance();
                        attendance.schregno = rs.getString("SCHREGNO");
                        attendance.lesson = rs.getString("LESSON");
                        attendance.sick = rs.getString("SICK");
                        attendance.late = rs.getString("LATE");
                        attendance.early = rs.getString("EARLY");
                        attendance.absent = rs.getString("ABSENT");
                        attendance.suspend = rs.getString("SUSPEND");
                        attendance.mourning = rs.getString("MOURNING");
                        attendance.lateEarly = String.valueOf(rs.getInt("LATE") + rs.getInt("EARLY"));
                        attendance.abroad = rs.getString("TRANSFER_DATE");
                        attendance.mlesson = rs.getString("MLESSON");
                        attendDataMap.put(rs.getString("SCHREGNO"), attendance);
                    }
                    
                } catch (Exception e) {
                    log.fatal("exception!", e);
                }
                
                //log.info(" attendRange = " + attendRange + ", attendDataMap size = " + attendDataMap.size());

                final StringBuffer hrClassSql = new StringBuffer();
                hrClassSql.append(" select distinct t1.grade, t1.hr_class, t1.grade || t1.hr_class as hr_cd, t2.hr_name from schreg_regd_dat t1 ");
                hrClassSql.append(" inner join schreg_regd_hdat t2 on t2.year = t1.year and t2.semester = t1.semester and t2.grade = t1.grade and t2.hr_class = t1.hr_class ");
                hrClassSql.append(" where t1.year = '" + year + "' and t1.semester = '" + semester + "' ");
                hrClassSql.append(" order by t1.grade, t1.hr_class ");
                final Collection hrClassList = QueryUtil.getRowList(db2, hrClassSql.toString());
                
                for (final Iterator itc = hrClassList.iterator(); itc.hasNext();) {
                    final Map hrClassMap = (Map) itc.next();
                    final String grade = getString("GRADE", hrClassMap);
                    final String hrClass = getString("HR_CLASS", hrClassMap);
                    
                    final StringBuffer regdSql = new StringBuffer();
                    regdSql.append(" select t1.schregno from schreg_regd_dat t1 ");
                    regdSql.append(" where t1.year = '" + year + "' and t1.semester = '" + semester + "' and t1.grade = '" + grade + "' and t1.hr_class = '" + hrClass + "' ");
                    regdSql.append(" order by t1.attendno ");
                    final Collection schregnoList = QueryUtil.getRowList(db2, regdSql.toString());
                    
                    //log.info(" grade = " + grade + ", hrClass = " + hrClass + ", size = " + schregnoList.size());

                    int maxJugyoNissu = 0;
                    final List lines = new LinkedList();
                    for (final Iterator itregd = schregnoList.iterator(); itregd.hasNext();) {
                        final Map schregnoMap = (Map) itregd.next();
                        
                        final String schregno = getString("SCHREGNO", schregnoMap);
                        final Attendance attendance = (Attendance) attendDataMap.get(schregno);
                        if (null != attendance) {
                            final List columns = new ArrayList();
                            columns.add(null);
                            columns.add(null);
                            columns.add(null);
                            columns.add(attendance.schregno);
                            columns.add(null);
                            columns.add(null);
                            columns.add(attendance.sick);
                            columns.add(attendance.late);
                            columns.add(attendance.early);
                            columns.add(attendance.absent);
                            columns.add(attendance.suspend);
                            columns.add(attendance.mourning);
                            columns.add(attendance.lateEarly);
                            columns.add(attendance.abroad);
                            columns.add(attendance.mlesson);
                            lines.add(columns);
                            
                            if (NumberUtils.isDigits(attendance.lesson)) {
                                maxJugyoNissu = Math.max(maxJugyoNissu, Integer.parseInt(attendance.lesson));
                            }
                        }
                    }
                    
                    lines.add(Collections.EMPTY_LIST); // 空行

                    final List headerLines = new ArrayList();
                    final Map titleParam = new HashMap();
                    titleParam.putAll(attendRange);
                    titleParam.putAll(hrClassMap);
                    titleParam.put("JUGYOU_NISSU", String.valueOf(maxJugyoNissu));
                    
                    headerLines.addAll(getTitleLines(_titleLines, titleParam));
                    headerLines.addAll(getHeaderLines(_flds));

                    ow.outputLines(headerLines);
                    ow.outputLines(lines);
                }
            }
        }
        
        private static class Attendance {
            String schregno;
            String lesson;
            String mlesson;
            String sick;
            String late;
            String early;
            String absent;
            String suspend;
            String mourning;
            String lateEarly;
            String abroad;
        }
    }
    
    // _50_日々講座出欠
    private static class OutputFmt50 extends OutputFmt {
        public void output(final DB2UDB db2, final Param param, final OutputWrapper ow) throws OutputWrapper.OutputExceptionWrapper {
            final String yearSql = " select distinct year from semester_mst t1 order by year ";
            final Collection yearList = QueryUtil.getRowList(db2, yearSql);

            for (final Iterator it = yearList.iterator(); it.hasNext();) {
                final Map mst = (Map) it.next();
                final String year = getString("YEAR", mst);
                
                boolean initByCode = false;
                
                try {
                    if (ow._outputToFile) {
                        final String filename = param.getOutputFilename(db2, _cd, year + "年度", ow._ext);
                        log.fatal(" create file " + filename);
                        ow._os = new FileOutputStream(filename);
                        ow.setNewBook();
                        if (ow._outputToFile && _outputExcellSplitSheet) {
                            ow._book.removeSheetAt(0);
                        } else if (!_outputExcellSplitSheet) {
                            ow.setNewSheet(ow.getDefaultSheetName());
                        }
                        initByCode = true;
                    }

                    outputYear(db2, ow, year);
                } catch (Exception e) {
                    if (e instanceof OutputWrapper.OutputExceptionWrapper) {
                        throw (OutputWrapper.OutputExceptionWrapper) e;
                    } else {
                        log.fatal("exception! cd = " + _cd + ", sql = " + _sql, e);
                    }
                } finally {
                    if (initByCode) {
                        ow.closeQuietly();
                    }
                }
            }
        }

        public void outputYear(final DB2UDB db2, final OutputWrapper ow, final String year) throws OutputWrapper.OutputExceptionWrapper {

            final StringBuffer chairSql = new StringBuffer();
            chairSql.append(" select t1.year, t1.chaircd, min(chairname) as chairname from chair_dat t1 ");
            chairSql.append(" inner join (select distinct year, semester, chaircd from sch_chr_dat) t2 on t2.year = t1.year and t2.semester = t1.semester and t2.chaircd = t1.chaircd "); // 時間割のある講座のみ
            chairSql.append(" where t1.year = '" + year + "' ");
            chairSql.append(" group by t1.year, t1.chaircd order by t1.chaircd ");
            final Collection chairList = QueryUtil.getRowList(db2, chairSql.toString());

            log.info(" year = " + year + ", chair list size = " + chairList.size());
            
            int chairCount = 0;
            for (final Iterator itc = chairList.iterator(); itc.hasNext();) {
                chairCount += 1;
                final Map chair = (Map) itc.next();
                
                //log.info(" year = " + year + ", chair = " + chair);
                
                ow.outputLines(getTitleLines(_titleLines, chair));
                
                final List headers = new ArrayList();
                headers.add(new ArrayList(Arrays.asList(new String[] {"異動", "マーク", "受講番号", "生徒番号", "氏名", "日付"})));
                headers.add(new ArrayList(Arrays.asList(new String[] {   "",        "",         "",         "",     "", "校時"})));
                headers.add(new ArrayList(Arrays.asList(new String[] {   "",        "",         "",         "",     "", "講座実施"})));

                final String chaircd = getString("CHAIRCD", chair);
                
                final String psKey = "CHAIR50_1";
                if (null == getPs(psKey)) {
                    final String schedulesSql = " select executedate, periodcd, cast(executedate as varchar(10)) || ':' || periodcd as SCHEDULE_KEY, executed from sch_chr_dat where year = ? and chaircd = ? order by executedate, periodcd ";
                    setPs(psKey, db2, schedulesSql);
                }
                final Collection scheduleList = QueryUtil.getRowList(db2, setPsArg(getPs(psKey), new String[] {year, chaircd}));
                for (final Iterator schit = scheduleList.iterator(); schit.hasNext();) {
                    final Map schedule = (Map) schit.next();
                    ((List) headers.get(0)).add(formatDateShort(getString("EXECUTEDATE", schedule))); // 日付
                    ((List) headers.get(1)).add(getString("PERIODCD", schedule)); // 校時
                    ((List) headers.get(2)).add("1".equals(getString("EXECUTED", schedule)) ? "済" : ""); // 講座実施
                }
                ow.outputLines(headers);

                final List lines = new LinkedList();
                final Map studentMap = getSchregChairAttendList(db2, year, chaircd);
                for (final Iterator stit = studentMap.keySet().iterator(); stit.hasNext();) {
                    final String schregno = (String) stit.next();
                    final Map attendMap = (Map) studentMap.get(schregno);
                    
                    final List columns = new ArrayList(Arrays.asList(new String[] {"", "", "", schregno, "", ""}));
                    for (final Iterator schit = scheduleList.iterator(); schit.hasNext();) {
                        final Map schedule = (Map) schit.next();
                        final String scheduleKey = getString("SCHEDULE_KEY", schedule);
                        final String diCd = StringUtils.defaultString(getString(scheduleKey, attendMap));
                        columns.add(diCd);
                    }
                    lines.add(columns);
                }
                lines.add(Collections.EMPTY_LIST);
                ow.outputLines(lines);
                
                if (chairCount % 50 == 0) {
                    Param.printMemoryInfo(" chair count = " + chairCount);
                }
            }
            Param.printMemoryInfo(" chair count = " + chairCount);
        }
        
        private Map getSchregChairAttendList(final DB2UDB db2, final String year, final String chaircd) {
            final String psKey = "CHAIR50_2";
            PreparedStatement ps = null;
            if (null == getPs(psKey)) {
                final StringBuffer studentsql = new StringBuffer();
                studentsql.append(" select t1.schregno, cast(t2.attenddate as varchar(10)) || ':' || t2.periodcd as SCHEDULE_KEY, value(c001.namespare1, t2.di_cd) as di_cd, t2.di_remark ");
                studentsql.append(" from chair_std_dat t1 ");
                studentsql.append("  left join attend_dat t2 on t2.schregno = t1.schregno ");
                studentsql.append("      and t2.attenddate between t1.appdate and t1.appenddate ");
                studentsql.append("  inner join name_mst c001 on c001.namecd1 = 'C001' ");
                studentsql.append("      and c001.namecd2 = t2.di_cd ");
                studentsql.append("  inner join schreg_base_mst base on base.schregno = t1.schregno ");
                studentsql.append(" where t1.year = ? and t1.chaircd = ? ");
                studentsql.append(" order by t1.schregno ");

                setPs(psKey, db2, studentsql.toString());
            }

            Map rtn = new TreeMap();
            ResultSet rs = null;
            try {
                ps = setPsArg(getPs(psKey), new String[] {year, chaircd});
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    if (null == rtn.get(schregno)) {
                        rtn.put(schregno, new TreeMap());
                    }
                    if (null != rs.getString("SCHEDULE_KEY")) {
                        final Map attendMap = (Map) rtn.get(schregno);
                        attendMap.put(rs.getString("SCHEDULE_KEY"), StringUtils.defaultString(rs.getString("DI_CD")) + (StringUtils.isEmpty(rs.getString("DI_REMARK")) ? "" : "/" + rs.getString("DI_REMARK")));
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, null, rs);
                db2.commit();
            }
            return rtn;
        }
    }
    
    // _51_日々クラス出欠
    private static class OutputFmt51 extends OutputFmt {
        public void output0(final DB2UDB db2, final OutputWrapper ow) throws OutputWrapper.OutputExceptionWrapper {
            //各学校における定数等設定
            final KNJDefineCode _definecode = new KNJDefineCode();
            //各学校における定数等設定
            final KNJDefineSchool _defineSchoolCode = new KNJDefineSchool();

            final StringBuffer yearSql = new StringBuffer();
            yearSql.append(" select distinct t1.year, l1.max_regd_semester from semester_mst t1 ");
            yearSql.append(" left join (select year, max(semester) as max_regd_semester ");
            yearSql.append("            from schreg_regd_dat ");
            yearSql.append("            group by year ");
            yearSql.append("            having count(schregno) > 1 ");
            yearSql.append("           ) l1 on l1.year = t1.year ");
            yearSql.append(" order by t1.year ");
            final Collection yearList = QueryUtil.getRowList(db2, yearSql.toString());

            for (final Iterator it = yearList.iterator(); it.hasNext();) {
                final Map yearMap = (Map) it.next();
                final String year = getString("YEAR", yearMap);
                final String maxRegdSemester = getString("MAX_REGD_SEMESTER", yearMap);
                
                log.info(" yearMap = " + yearMap);
                
                final StringBuffer hrSql = new StringBuffer();
                hrSql.append(" select t1.grade, t1.hr_class, t1.grade || t1.hr_class as hr_cd, t1.hr_name from schreg_regd_hdat t1 ");
                hrSql.append(" where t1.year = '" + year + "' and t1.semester = '" + maxRegdSemester + "' ");
                hrSql.append(" order by t1.grade, t1.hr_class ");
                final Collection hrList = QueryUtil.getRowList(db2, hrSql.toString());
                
                final Map schregAttendDataMap = getSchregAttendDataMap(db2, year, _definecode, _defineSchoolCode);
                
                List headers = null;
                Map titleParam = null;
                for (final Iterator hrit = hrList.iterator(); hrit.hasNext();) {
                    final Map hr = (Map) hrit.next();
                    final String grade = getString("GRADE", hr);
                    final String hrClass = getString("HR_CLASS", hr);
                    
                    final StringBuffer regdSql = new StringBuffer();
                    regdSql.append(" select t1.schregno, t1.attendno from schreg_regd_dat t1 ");
                    regdSql.append(" where t1.year = '" + year + "' and t1.semester = '" + maxRegdSemester + "' and t1.grade = '" + grade + "' and t1.hr_class = '" + hrClass + "' ");
                    regdSql.append(" order by t1.schregno, t1.attendno ");
                    final Collection schregnoList = QueryUtil.getRowList(db2, regdSql.toString());
                    
                    final StringBuffer dateSql = new StringBuffer();
                    dateSql.append(" select t1.executedate, max(executed) as executed from sch_chr_dat t1 ");
                    dateSql.append(" inner join chair_std_dat t2 on t2.year = t1.year ");
                    dateSql.append("   and t2.semester = t1.semester ");
                    dateSql.append("   and t2.chaircd = t1.chaircd ");
                    dateSql.append("   and t1.executedate between t2.appdate and t2.appenddate ");
                    dateSql.append(" inner join schreg_regd_dat t3 on t3.schregno = t2.schregno ");
                    dateSql.append("   and t3.year = t2.year ");
                    dateSql.append("   and t3.semester = '" + maxRegdSemester + "' ");
                    dateSql.append("   and t3.grade = '" + grade + "' ");
                    dateSql.append("   and t3.hr_class = '" + hrClass + "' ");
                    dateSql.append(" inner join schreg_base_mst t4 on t4.schregno = t2.schregno ");
                    dateSql.append(" where ");
                    dateSql.append("   t1.year = '" + year + "' ");
                    dateSql.append(" group by t1.executedate ");
                    dateSql.append(" order by t1.executedate ");
                    final Collection executedateList = QueryUtil.getRowList(db2, dateSql.toString());
                    
                    if (executedateList.isEmpty()) {
                        continue;
                    }
                    //log.info("  hr = " + hr + ", date = " + executedateList.get(0) + " - " + executedateList.get(executedateList.size() - 1));

                    headers = new ArrayList();
                    headers.add(new ArrayList(Arrays.asList(new String[] {"異動", "マーク", "出席№", "生徒番号", "氏名", "日付"})));
                    headers.add(new ArrayList(Arrays.asList(new String[] {    "",       "",       "",         "",     "", "クラス出欠"})));

                    for (final Iterator schit = executedateList.iterator(); schit.hasNext();) {
                        final Map executedateMap = (Map) schit.next();
                        final String executedate = getString("EXECUTEDATE", executedateMap);
                        ((List) headers.get(0)).add(formatDateShort(executedate)); // 日付
                        ((List) headers.get(1)).add("1".equals(getString("EXECUTED", executedateMap)) ? "済" : ""); // クラス出欠
                    }
                    
                    titleParam = new HashMap();
                    titleParam.putAll(yearMap);
                    titleParam.putAll(hr);
                    
                    final List lines = new LinkedList();
                    for (final Iterator stit = schregnoList.iterator(); stit.hasNext();) {
                        final Map schregnoMap = (Map) stit.next();
                        final String schregno = getString("SCHREGNO", schregnoMap);
                        
                        if (null == schregAttendDataMap.get(schregno)) {
                            continue; // 出欠データがない
                        }
                        final Map attendDataMap = (Map) schregAttendDataMap.get(schregno);
                        
                        final List columns = new ArrayList(Arrays.asList(new String[] {"", "", "", schregno, "", ""}));
                        for (final Iterator schit = executedateList.iterator(); schit.hasNext();) {
                            final Map executedateMap = (Map) schit.next();
                            final String executedate = getString("EXECUTEDATE", executedateMap);
                            
                            final String diCd = StringUtils.defaultString(getString(executedate, attendDataMap));
                            columns.add(diCd);
                        }
                        lines.add(columns);
                    }
                    
                    if (!lines.isEmpty()) {
                        lines.add(Collections.EMPTY_LIST);

                        ow.outputLines(getTitleLines(_titleLines, titleParam));
                        ow.outputLines(headers);
                        ow.outputLines(lines);
                    }
                }
            }
        }
        
        private static Map getSchregAttendDataMap(final DB2UDB db2, final String year, final KNJDefineCode _definecode, final KNJDefineSchool _defineSchoolCode) {
            final Map schregAttendDataMap = new TreeMap();
            
            final StringBuffer dateSql = new StringBuffer();
            dateSql.append(" select distinct t1.executedate from sch_chr_dat t1 ");
            dateSql.append(" inner join chair_std_dat t2 on t2.year = t1.year ");
            dateSql.append("   and t2.semester = t1.semester ");
            dateSql.append("   and t2.chaircd = t1.chaircd ");
            dateSql.append("   and t1.executedate between t2.appdate and t2.appenddate ");
            dateSql.append(" inner join schreg_base_mst t3 on t3.schregno = t2.schregno ");
            dateSql.append(" where t1.year = '" + year + "' ");
            dateSql.append(" order by t1.executedate ");
            final List executedateList = (List) QueryUtil.getRowList(db2, dateSql.toString());

            if (executedateList.isEmpty()) {
                return schregAttendDataMap;
            }
            log.info("  date = " + executedateList.get(0) + " - " + executedateList.get(executedateList.size() - 1));

            final Map paramMap = new HashMap();
            paramMap.put("useCurriculumcd", "1");
            
            _defineSchoolCode.defineCode(db2, year);
            KNJSchoolMst _ksm = null;
            try {
                _ksm = new KNJSchoolMst(db2, year);
            } catch (Exception e) {
                log.fatal("exception!", e);
            }
            
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String periodInState = AttendAccumulate.getPeiodValue(db2, _definecode, year, "1", "3");

                final String sql = AttendAccumulate.getAttendSemesSql(
                        false,
                        _defineSchoolCode,
                        _ksm,
                        year,
                        "1",
                        "3",
                        null,
                        periodInState,
                        "?",
                        "?",
                        null,
                        null,
                        null,
                        null,
                        null,
                        "SEMESTER",
                        paramMap);
                
                ps = db2.prepareStatement(sql);
                
                for (final Iterator schit = executedateList.iterator(); schit.hasNext();) {
                    final Map executedateMap = (Map) schit.next();
                    final String executedate = getString("EXECUTEDATE", executedateMap);
                    
                    ps.setString(1, executedate);
                    ps.setString(2, executedate);
                    rs = ps.executeQuery();
                    
                    while (rs.next()) {
                        final String schregno = rs.getString("SCHREGNO");
                        if (null == schregAttendDataMap.get(schregno)) {
                            schregAttendDataMap.put(schregno, new HashMap());
                        }
                        final Map attendDataMap = (Map) schregAttendDataMap.get(schregno);
                        attendDataMap.put(executedate, getDiCd(rs));
                    }
                    
                    DbUtils.closeQuietly(rs);
                }
                
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return schregAttendDataMap;
        }

        private static String getDiCd(final ResultSet rs) throws SQLException {
            String cd = "";
            if (rs.getInt("SUSPEND") > 0) {
                cd = "2";
            } else if (rs.getInt("MOURNING") > 0) {
                cd = "3";
            } else if (rs.getInt("NOTICE_ONLY") > 0) {
                cd = "5";
            } else if (rs.getInt("NONOTICE_ONLY") > 0) {
                cd = "6";
            } else if (rs.getInt("SICK") > 0) {
                cd = "4";
            } else if (rs.getInt("LATE") > 0) {
                cd = "15";
            } else if (rs.getInt("EARLY") > 0) {
                cd = "16";
            } else if (rs.getInt("ABSENT") > 0) {
                cd = "1";
            }
            return cd;
        }
    }
    
    // _52_特活時間数D
    private static class OutputFmt52 extends OutputFmt {
        public void output0(final DB2UDB db2, final OutputWrapper ow) throws OutputWrapper.OutputExceptionWrapper {
            final Map paramMap = new HashMap();
            paramMap.put("useCurriculumcd", "1");
            //各学校における定数等設定
            final KNJDefineCode _definecode = new KNJDefineCode();
            //各学校における定数等設定
            final KNJDefineSchool _defineSchoolCode = new KNJDefineSchool();

            final String div = "1"; // 0:講座 1:クラス _47_集計出欠期間D
            final String attendRangeSql = " select * from tori_attendrange_dat t1 where div = '" + div + "' order by year, semester, semester_detail, sdate ";
            final Collection attendRangeList = QueryUtil.getRowList(db2, attendRangeSql);
            
            ow.outputLines(getTitleLines(_titleLines, null));
            ow.outputLines(getHeaderLines(_flds));

            log.info(" attend range list = " + attendRangeList);

            for (final Iterator it = attendRangeList.iterator(); it.hasNext();) {
                final Map attendRange = (Map) it.next();
                final String year = getString("YEAR", attendRange);
                final String semester = getString("SEMESTER", attendRange);
                final String semesterDetail = getString("SEMESTER_DETAIL", attendRange);
                final String sdate = getString("SDATE", attendRange);
                final String edate= getString("EDATE", attendRange);
                
                final Map subClassC005 = loadNameMstC005(db2, year);
                
                String jifunMax = null;
                _defineSchoolCode.defineCode(db2, year);
                KNJSchoolMst _ksm = null;
                try {
                    jifunMax = QueryUtil.getOne(db2, "select max(jitu_jifun_special) from v_school_mst ");
                    _ksm = new KNJSchoolMst(db2, year);
                } catch (Exception e) {
                    log.fatal("exception!", e);
                }
                
                final Map attendDataMap = new TreeMap();
                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    final String z010Name1 = QueryUtil.getNameMst(db2, year, "Z010", "00", "NAME1");
                    final Map attendSemAllMap = AttendAccumulate.getAttendSemesMap(db2, z010Name1, year);
                    final String periodInState = AttendAccumulate.getPeiodValue(db2, _definecode, year, semester, semester);
                    final Map hasuuMap = AttendAccumulate.getHasuuMap(attendSemAllMap, sdate, edate);
                    final boolean semesFlg = ((Boolean) hasuuMap.get("semesFlg")).booleanValue();

                    final String sql = AttendAccumulate.getAttendSubclassSql(
                            semesFlg,
                            _defineSchoolCode,
                            _ksm,
                            year,
                            semester,
                            semester,
                            (String) hasuuMap.get("attendSemesInState"),
                            periodInState,
                            (String) hasuuMap.get("befDayFrom"),
                            (String) hasuuMap.get("befDayTo"),
                            (String) hasuuMap.get("aftDayFrom"),
                            (String) hasuuMap.get("aftDayTo"),
                            null,
                            null,
                            null,
                            paramMap);
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        if (!"9".equals(rs.getString("SEMESTER"))) {
                            continue;
                        }
                        final String specialGroupCd = rs.getString("SPECIAL_GROUP_CD");
                        if (null != specialGroupCd) {
                            final String schregno = rs.getString("SCHREGNO");
                            if (null == attendDataMap.get(schregno)) {
                                attendDataMap.put(schregno, new Attendance());
                            }
                            final Attendance attendance = (Attendance) attendDataMap.get(schregno);
                            attendance._schregno = rs.getString("SCHREGNO");
                            
                            String field = "SPECIAL_SICK_MINUTES1";
                            final String subclassCd = StringUtils.split(rs.getString("SUBCLASSCD"), "-")[3];
                            if (subClassC005.containsKey(subclassCd)) {
                                if ("1".equals(subClassC005.get(subclassCd))) {
                                    field = "SPECIAL_SICK_MINUTES3";
                                } else if ("2".equals(subClassC005.get(subclassCd))) {
                                    field = "SPECIAL_SICK_MINUTES2";
                                }
                            }
                            
                            Attendance.addMinutes(attendance._spGroupMinutesKekka, specialGroupCd, rs.getString(field));
                            Attendance.addMinutes(attendance._spGroupMinutesLesson, specialGroupCd, rs.getString("SPECIAL_LESSON_MINUTES"));
                        }
                    }
                    
                } catch (Exception e) {
                    log.fatal("exception!", e);
                }
                
                //log.info(" attendRange = " + attendRange + ", subclassMap size = " + attendDataMap.size());

                final List lines = new LinkedList();
                for (final Iterator attit = attendDataMap.values().iterator(); attit.hasNext();) {
                    final Attendance attendance = (Attendance) attit.next();
                    final List columns = new ArrayList();
                    columns.add(attendance._schregno);
                    columns.add(year);
                    columns.add(semester);
                    columns.add(semesterDetail);
                    columns.add(null);
                    columns.add(attendance.tokkatsuJikansu(_ksm, jifunMax));
                    columns.add("0");
                    columns.add(attendance.shrSyusseki(_ksm, jifunMax));
                    columns.add(attendance.shrJisshi(_ksm));
                    columns.add(attendance.shrKekka(_ksm, jifunMax));
                    columns.add(attendance.lhrSyusseki(_ksm, jifunMax));
                    columns.add(attendance.lhrJisshi(_ksm));
                    columns.add(attendance.lhrKekka(_ksm, jifunMax));
                    columns.add(formatDate(sdate));
                    columns.add(formatDate(edate));
                    lines.add(columns);
                }
                ow.outputLines(lines);
            }

        }
        
        private static String add(final String v1, final String v2) {
            if (!NumberUtils.isDigits(v1)) {
                return v2;
            }
            if (!NumberUtils.isDigits(v2)) {
                return v1;
            }
            return String.valueOf(Integer.parseInt(v1) + Integer.parseInt(v2));
        }
        
        
        /**
         * 欠課換算法修正
         * @param db2
         */
        private Map loadNameMstC005(final DB2UDB db2, final String year) {
            final Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT NAME1 AS SUBCLASSCD, NAMESPARE1 FROM V_NAME_MST WHERE YEAR = '" + year + "' AND NAMECD1 = 'C005'";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String is = rs.getString("NAMESPARE1");
                    //log.debug("(名称マスタ C005):科目コード=" + subclassCd);
                    rtn.put(subclassCd, is);
                }
            } catch (SQLException e) {
                log.fatal("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(rs);
            }
            return rtn;
        }
    
        private static class Attendance {
            String _schregno;
            Map _spGroupMinutesKekka = new HashMap();
            Map _spGroupMinutesLesson = new HashMap();
            public static void addMinutes(final Map map, final String specialGroupCd, final String minutesNew) {
                if (!NumberUtils.isDigits(minutesNew)) {
                    return;
                }
                final Integer minutesKekkaSrc;
                if (null != map.get(specialGroupCd)) {
                    minutesKekkaSrc = (Integer) map.get(specialGroupCd);
                } else {
                    minutesKekkaSrc = new Integer("0");
                }
                map.put(specialGroupCd, new Integer(minutesKekkaSrc.intValue() + Integer.parseInt(minutesNew)));
            }
            public String jisu(final Map m, final KNJSchoolMst knjSchoolMst, final String getSpGroupCd, final String jifunMax) {
                int rtn = 0;
                for (final Iterator it = m.keySet().iterator(); it.hasNext();) {
                    final String spGroupCd = (String) it.next();
                    if (null != getSpGroupCd && !getSpGroupCd.equals(spGroupCd)) {
                        continue;
                    }
                    final Integer spTotalMinutes = (Integer) m.get(spGroupCd);
                    final int hour = getSpecialAttendExe(spTotalMinutes.intValue(), knjSchoolMst, jifunMax);
                    rtn += hour;
                }
                return String.valueOf(rtn);
            }
            public String tokkatsuJikansu(final KNJSchoolMst knjSchoolMst, final String jifunMax) {
                return jisu(_spGroupMinutesLesson, knjSchoolMst, null, jifunMax);
            }

            public String lhrKekka(final KNJSchoolMst knjSchoolMst, final String jifunMax) {
                return jisu(_spGroupMinutesKekka, knjSchoolMst, "001", jifunMax);
            }
            public String lhrJisshi(final KNJSchoolMst knjSchoolMst) {
                return null;
            }
            public String lhrSyusseki(final KNJSchoolMst knjSchoolMst, final String jifunMax) {
                return jisu(_spGroupMinutesLesson, knjSchoolMst, "001", jifunMax);
            }
            public String shrKekka(final KNJSchoolMst knjSchoolMst, final String jifunMax) {
                return jisu(_spGroupMinutesKekka, knjSchoolMst, "004", jifunMax);
            }
            public String shrJisshi(final KNJSchoolMst knjSchoolMst) {
                return null;
            }
            public String shrSyusseki(final KNJSchoolMst knjSchoolMst, final String jifunMax) {
                return jisu(_spGroupMinutesLesson, knjSchoolMst, "004", jifunMax);
            }
            
            /**
             * 欠課時分を欠課時数に換算した値を得る
             * @param kekka 欠課時分
             * @return 欠課時分を欠課時数に換算した値
             */
            private static int getSpecialAttendExe(final int kekka, final KNJSchoolMst knjSchoolMst, final String jifunMax) {
                int jituJifun = Integer.parseInt(StringUtils.isNumeric(knjSchoolMst._jituJifunSpecial) ? knjSchoolMst._jituJifunSpecial : StringUtils.isNumeric(jifunMax) ? jifunMax : "45");
                
                final BigDecimal bigKekka = new BigDecimal(kekka);
                final BigDecimal bigJitu = new BigDecimal(jituJifun);
                BigDecimal bigD = bigKekka.divide(bigJitu, 1, BigDecimal.ROUND_DOWN);
                String retSt = bigD.toString();
                final int retIndex = retSt.indexOf(".");
                int seisu = 0;
                if (retIndex > 0) {
                    seisu = Integer.parseInt(retSt.substring(0, retIndex));
                    final int hasu = Integer.parseInt(retSt.substring(retIndex + 1, retIndex + 2));
                    seisu = isKirisute(hasu, knjSchoolMst) ? seisu : seisu + 1;
                } else {
                    seisu = Integer.parseInt(retSt);
                }
                return seisu;
            }
            
            private static boolean isKirisute(final int hasu, final KNJSchoolMst knjSchoolMst) {
                if ("1".equals(knjSchoolMst._tokubetuKatudoKansan)) { // 特活換算：二捨三入 (五捨六入)
                    return hasu < 6;
                } else if ("2".equals(knjSchoolMst._tokubetuKatudoKansan)) { // 特活換算：四捨五入
                    return hasu < 5;
                } else if ("3".equals(knjSchoolMst._tokubetuKatudoKansan)) { // 特活換算：切り上げ
                    return hasu == 0;
                } else if ("4".equals(knjSchoolMst._tokubetuKatudoKansan)) { // 特活換算：切り下げ
                    return true;
                }
                return hasu < 6;
            }
        }
    }
    
    // _56_時間割
    private static class OutputFmt56 extends OutputFmtDiv {
        
        private static class F56 implements SheetFormatter {
            public void setPs(final PreparedStatement ps, final Map map) throws SQLException {
                ps.setString(1, getString("YEAR", map));
                ps.setString(2, getString("SEMESTER", map));
                ps.setInt(3, Integer.parseInt(getString("BSCSEQ", map)));
            }
            public String formatSheetName(final Map map) {
                return getString("YEAR", map) + "年度" + getString("SEMESTERNAME", map) + getString("BSCSEQ", map);
            }
        }
        
        private static String divSql() {
            final StringBuffer sql = new StringBuffer();
            sql.append(" select distinct t1.year, t1.semester, t2.semestername, t1.bscseq ");
            sql.append(" from sch_ptrn_hdat t1 ");
            sql.append(" inner join semester_mst t2 on t2.year = t1.year and t2.semester = t1.semester ");
            sql.append(" order by t1.year ");
            return sql.toString();
        }

        OutputFmt56() {
            super(divSql(), new F56());
            super._outputExcellSplitSheet = true;
            
            final StringBuffer stb = new StringBuffer();
            stb.append(" with ptrn_stf as ( ");
            stb.append(" select t1.daycd, t1.periodcd, t1.chaircd, concat(concat(t1.staffcd, ' '), t3.staffname) as staffcd, ");
            stb.append("        row_number() over(partition by t1.year, t1.semester, t1.bscseq, t1.daycd, t1.periodcd, t1.chaircd order by t1.staffcd) as no ");
            stb.append(" from sch_ptrn_stf_dat t1 ");
            stb.append(" inner join staff_mst t3 on t3.staffcd = t1.staffcd ");
            stb.append(" ) ");
            stb.append(" select ");
            stb.append("  t1.chaircd, ");
            stb.append("  '' as chairname, ");
            stb.append("  case t1.daycd ");
            stb.append("    when '1' then '日' ");
            stb.append("    when '2' then '月' ");
            stb.append("    when '3' then '火' ");
            stb.append("    when '4' then '水' ");
            stb.append("    when '5' then '木' ");
            stb.append("    when '6' then '金' ");
            stb.append("    when '7' then '土' ");
            stb.append("  end as youbi, ");
            stb.append("  t1.periodcd as kouji, ");
            stb.append("  value(concat(t3.staffcd, ','), '') || ");
            stb.append("  value(concat(t4.staffcd, ','), '') || ");
            stb.append("  value(t5.staffcd, '') as staffcd, ");
            stb.append("  '' as roomno ");
            stb.append(" from sch_ptrn_dat t1 ");
            stb.append(" left join ptrn_stf t3 on t3.daycd = t1.daycd and t3.periodcd = t1.periodcd and t3.chaircd = t1.chaircd and t3.no = 1 ");
            stb.append(" left join ptrn_stf t4 on t4.daycd = t1.daycd and t4.periodcd = t1.periodcd and t4.chaircd = t1.chaircd and t4.no = 2 ");
            stb.append(" left join ptrn_stf t5 on t5.daycd = t1.daycd and t5.periodcd = t1.periodcd and t5.chaircd = t1.chaircd and t5.no = 3 ");
            stb.append(" where year = ? and semester = ? and bscseq = ? ");
            stb.append(" order by chaircd, t1.daycd, kouji ");
            _sql = stb.toString();
        }
    }
    
    // _58_授業実施日予定日
    private static class OutputFmt58 extends OutputFmt {
        public void output(final DB2UDB db2, final Param param, final OutputWrapper ow) throws OutputWrapper.OutputExceptionWrapper {
            final String yearSql = " select distinct t1.year from sch_chr_dat t1 order by year ";
            final Collection yearList = QueryUtil.getRowList(db2, yearSql);

            for (final Iterator it = yearList.iterator(); it.hasNext();) {
                final Map yearMap = (Map) it.next();
                final String year = getString("YEAR", yearMap);
                
                boolean initByCode = false;
                
                try {
                    if (ow._outputToFile) {
                        final String filename = param.getOutputFilename(db2, _cd, year + "年度", ow._ext);
                        log.fatal(" create file " + filename);
                        ow._os = new FileOutputStream(filename);
                        ow.setNewBook();
                        if (ow._outputToFile && _outputExcellSplitSheet) {
                            ow._book.removeSheetAt(0);
                        } else if (!_outputExcellSplitSheet) {
                            ow.setNewSheet(ow.getDefaultSheetName());
                        }
                        initByCode = true;
                    }

                    outputYear(db2, ow, yearMap);
                } catch (Exception e) {
                    if (e instanceof OutputWrapper.OutputExceptionWrapper) {
                        throw (OutputWrapper.OutputExceptionWrapper) e;
                    } else {
                        log.fatal("exception! cd = " + _cd + ", sql = " + _sql, e);
                    }
                } finally {
                    if (initByCode) {
                        ow.closeQuietly();
                    }
                }
            }
        }

        public void outputYear(final DB2UDB db2, final OutputWrapper ow, final Map yearMap) throws OutputWrapper.OutputExceptionWrapper {

            final String psKey = "SCH_CHR";
            if (null == getPs(psKey)) {
                final StringBuffer stb = new StringBuffer();
                stb.append(" with tokkatsu_jisu as ( ");
                stb.append(" select t1.year, t2.classcd, t2.school_kind, t2.curriculum_cd, t2.subclasscd, ");
                stb.append("  cast(round(int(value(t2.minutes, '50')) * 1.0 / int(value(t1.jitu_jifun_special, t3.jitu_jifun_special, '50')), 3) as decimal(4,2)) as jisu ");
                stb.append(" from v_school_mst t1 ");
                stb.append(" inner join attend_subclass_special_dat t2 on t2.year = t1.year ");
                stb.append(" left join (select 1 as key, max(jitu_jifun_special) as jitu_jifun_special from v_school_mst) t3 on t3.key = 1 ");
                stb.append(" ), has_facs as ( ");
                stb.append(" select executedate as edate, periodcd as pcd, chaircd as ccd, min(faccd) as faccd ");
                stb.append(" from sch_fac_dat ");
                stb.append(" group by executedate, periodcd, chaircd ");
                stb.append(" ), sch_facs as ( ");
                stb.append(" select executedate as edate, periodcd as pcd, chaircd as ccd, faccd, row_number() over(partition by executedate, periodcd, chaircd order by faccd) as id ");
                stb.append(" from sch_fac_dat ");
                stb.append(" ), chr_facs as ( ");
                stb.append(" select year, semester, chaircd as ccd, faccd, row_number() over(partition by year, semester, chaircd order by faccd) as id ");
                stb.append(" from chair_fac_dat ");
                stb.append(" ), sch_chr as ( ");
                stb.append(" select  ");
                stb.append("  executedate as edate, periodcd as pcd, chaircd as ccd, executed, year, semester ");
                stb.append(" from sch_chr_dat ");
                stb.append(" ) ");
                stb.append(" select ");
                stb.append("  t1.edate as executedate, ");
                stb.append("  '' as youbi, ");
                stb.append("  t1.pcd as periodcd, ");
                stb.append("  case when t1.executed = '1' then '実施' end as jisshi, ");
                stb.append("  t1.ccd as chaircd, ");
                stb.append("  '' as chairname, ");
                stb.append("  '' as tantou, ");
                stb.append("  case when has.faccd is not null then  ");
                stb.append("   facs1.faccd || value(concat(',', facs2.faccd), '') || value(concat(',', facs3.faccd), '') ");
                stb.append("   else  ");
                stb.append("   cfacs1.faccd || value(concat(',', cfacs2.faccd), '') || value(concat(',', cfacs3.faccd), '')  ");
                stb.append("  end as faccd, ");
                stb.append("  tj.jisu ");
                stb.append(" from sch_chr t1 ");
                stb.append(" inner join chair_dat t2 on t2.year = t1.year and t2.semester = t1.semester and t2.chaircd = t1.ccd ");
                stb.append(" left join has_facs has on has.edate = t1.edate and has.pcd = t1.pcd and has.ccd = t1.ccd  ");
                stb.append(" left join sch_facs facs1 on facs1.edate = t1.edate and facs1.pcd = t1.pcd and facs1.ccd = t1.ccd and facs1.id = 1 ");
                stb.append(" left join sch_facs facs2 on facs2.edate = t1.edate and facs2.pcd = t1.pcd and facs2.ccd = t1.ccd and facs2.id = 2 ");
                stb.append(" left join sch_facs facs3 on facs3.edate = t1.edate and facs3.pcd = t1.pcd and facs3.ccd = t1.ccd and facs3.id = 3 ");
                stb.append(" left join chr_facs cfacs1 on cfacs1.year = t1.year and cfacs1.semester = t1.semester and cfacs1.ccd = t1.ccd and cfacs1.id = 1 ");
                stb.append(" left join chr_facs cfacs2 on cfacs2.year = t1.year and cfacs2.semester = t1.semester and cfacs2.ccd = t1.ccd and cfacs2.id = 2 ");
                stb.append(" left join chr_facs cfacs3 on cfacs3.year = t1.year and cfacs3.semester = t1.semester and cfacs3.ccd = t1.ccd and cfacs3.id = 3 ");
                stb.append(" left join tokkatsu_jisu tj on tj.year = t1.year and tj.classcd = t2.classcd and tj.school_kind = t2.school_kind and tj.curriculum_cd = t2.curriculum_cd and tj.subclasscd = t2.subclasscd ");
                stb.append(" where t1.year = ? ");
                stb.append(" order by ");
                stb.append(" t1.year, t1.semester, t1.edate, t1.pcd, t1.ccd ");

                setPs(psKey, db2, stb.toString());
            }

            try {
                final Collection lines = getDataLines(_cd, setPsArg(getPs(psKey), new String[] {getString("YEAR", yearMap)}), _flds);

                if (lines.size() > 0) {
                    
                    ow.outputLines(getTitleLines(_titleLines, yearMap));
                    ow.outputLines(getHeaderLines(_flds));
                    ow.outputLines(lines);
                }
            } catch (Exception e) {
                if (e instanceof OutputWrapper.OutputExceptionWrapper) {
                    throw (OutputWrapper.OutputExceptionWrapper) e;
                } else {
                    log.fatal("exception! cd = " + _cd + ", sql = " + _sql, e);
                }
            }
        }

    }

    // _72_保健日報D
    private static class OutputFmt72 extends OutputFmt {
        public void output0(final DB2UDB db2, final OutputWrapper ow) throws OutputWrapper.OutputExceptionWrapper {
            final KNJF175 knjf175 = new KNJF175();
            final String useVirus = null;
            final String useKoudome = null;
            //各学校における定数等設定
            final KNJDefineCode _definecode = new KNJDefineCode();
            KNJSchoolMst _ksm = null;

            ow.outputLines(getHeaderLines(_flds));
            
            final String semesterSql = " select year, semester, sdate, edate from semester_mst t1 where t1.semester <> '9' order by year, semester, sdate ";
            final Collection semesterList = QueryUtil.getRowList(db2, semesterSql);

            for (final Iterator it = semesterList.iterator(); it.hasNext();) {
                final Map semeMap = (Map) it.next();
                final String year = getString("YEAR", semeMap);
                final String semester = getString("SEMESTER", semeMap);
                final String sdate = getString("SDATE", semeMap);
                final String edate = getString("EDATE", semeMap);
                
                try {
                    _ksm = new KNJSchoolMst(db2, year);
                } catch (Exception e) {
                    log.fatal("exception!", e);
                }

                List dateInfo = null;
                try {
                    final String periodInState = AttendAccumulate.getPeiodValue(db2, _definecode, year, semester, semester);
                    knjf175.setParam(useVirus, useKoudome, year, semester, sdate, edate, periodInState);
                    dateInfo = knjf175.getDateInfo(db2, _ksm, _definecode);
                    
                } catch (Exception e) {
                    log.fatal("exception!", e);
                }
                if (null == dateInfo || dateInfo.size() == 0) {
                    continue;
                }
                log.info("  date = " + dateInfo.get(0) + " - " + dateInfo.get(dateInfo.size() - 1));

                final List lines = new ArrayList();
                for (final Iterator dit = dateInfo.iterator(); dit.hasNext();) {
                    KNJF175.Date date = (KNJF175.Date) dit.next();
                    final List columns = new ArrayList();
                    
                    columns.add(formatDate(date._date));
                    columns.add(year);
                    if (date._diaryList.size() > 0) {
                        final KNJF175.Diary diary = (KNJF175.Diary) date._diaryList.get(0);
                        columns.add(diary._weather);
                        columns.add(diary._temperature);
                        columns.add(dupDQuote(diary._event));
                        columns.add(dupDQuote(diary._diary));
                        columns.add(dupDQuote(diary._staffcd));
                    } else {
                        columns.add("");
                        columns.add("");
                        columns.add("");
                        columns.add("");
                        columns.add("");
                    }
                    final int BYOKETSU = 0;
                    final int JIKOKETSU = 1;
                    final int MUTODOKE = 2;
                    final int SYUTTEI = 3;
                    final int KIBIKI = 4;
                    final int[] kinds = { BYOKETSU, JIKOKETSU, MUTODOKE, SYUTTEI, KIBIKI };
                    for (int i = 0; i < kinds.length; i++) {
                        final int kind = kinds[i];
                        for (int g = 1; g <= 4; g++) {
                            KNJF175.AttendCnt tgt = null;
                            for (Iterator itk = date._attendCntList.iterator(); itk.hasNext();) {
                                KNJF175.AttendCnt attendCnt = (KNJF175.AttendCnt) itk.next();
                                if (NumberUtils.isDigits(attendCnt._grade) && Integer.parseInt(attendCnt._grade) == g) {
                                    tgt = attendCnt;
                                }
                            }
                            String v = null;
                            if (null != tgt) {
                                if (kind == BYOKETSU) { v = tgt._sick; }
                                else if (kind == JIKOKETSU) { v = tgt._notice; }
                                else if (kind == MUTODOKE) { v = tgt._nonotice; }
                                else if (kind == SYUTTEI) { v = tgt._suspend; }
                                else if (kind == KIBIKI) { v = tgt._mourning; }
                            }
                            columns.add(StringUtils.defaultString(v, "0"));
                        }
                    }
                    
                    columns.add(StringUtils.defaultString(date._sickT, "0"));
                    columns.add(StringUtils.defaultString(date._noticeT, "0"));
                    columns.add(StringUtils.defaultString(date._nonoticeT, "0"));
                    columns.add(StringUtils.defaultString(date._suspendT, "0"));
                    columns.add(StringUtils.defaultString(date._mourningT, "0"));

                    lines.add(columns);
                }
                
                ow.outputLines(lines);
            }
        }


        private class KNJF175 {
            
            private Param _param;
            private KNJSchoolMst _knjSchoolMst;
            private KNJDefineSchool _definecode;
            
            private class Param {
                final String _year;
                final String _semester;
                final String _diaryDateFrom;
                final String _diaryDateTo;
                final String _periodInState;
                final String _useVirus;
                final String _useKoudome;
                public Param(final String year, final String semester, final String diaryDateFrom, final String diaryDateTo, final String periodInState, final String useVirus, final String useKoudome) {
                    _year = year;
                    _semester = semester;
                    _diaryDateFrom = diaryDateFrom;
                    _diaryDateTo = diaryDateTo;
                    _periodInState = periodInState;
                    _useVirus = useVirus;
                    _useKoudome = useKoudome;
                }
            }
            
            public void setParam(final String useVirus,
                    final String useKoudome,
                    final String year,
                    final String semester,
                    final String sdate,
                    final String edate,
                    final String periodInState) {
                _param = new KNJF175.Param(year, semester, sdate, edate, periodInState, useVirus, useKoudome);
            }
            
            /**
             * 日付情報の取得
             */
            private List getDateInfo(final DB2UDB db2, final KNJSchoolMst knjSchoolMst, final KNJDefineSchool definecode) throws SQLException, ParseException {
                _knjSchoolMst = knjSchoolMst;
                _definecode = definecode;
                final List rtnList = new ArrayList();
                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    final String sql = getDateSql();
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final Date date = new Date(rs.getString("YEAR"), rs.getString("SEMESTER"), rs.getString("DATE"));
                        date.setDiary(db2);
//                        date.setVisitrec(db2);
//                        date.setVisitrecCnt(db2);
                        date.setAttendCnt(db2);
//                        if (_param._isPrintAttend) {
//                            date.setAttend(db2);
//                        }
                        rtnList.add(date);
                    }
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                    db2.commit();
                }
                return rtnList;
            }
            
            /**
             * 対象日付の取得
             * ・保健室日誌データと保健室来室データのどちらかに登録されている日付が対象。
             */
            private String getDateSql() {
                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH ALL_DATE(DATE) AS ( ");
                stb.append(" SELECT ");
                stb.append("     DATE ");
                stb.append(" FROM ");
                stb.append("     NURSEOFF_DIARY_DAT ");
                stb.append(" WHERE ");
                stb.append("     DATE BETWEEN '" + _param._diaryDateFrom + "' AND '" + _param._diaryDateTo + "' ");
                stb.append(" UNION ");
                stb.append(" SELECT ");
                stb.append("     VISIT_DATE AS DATE ");
                stb.append(" FROM ");
                stb.append("     NURSEOFF_VISITREC_DAT ");
                stb.append(" WHERE ");
                stb.append("     VISIT_DATE BETWEEN '" + _param._diaryDateFrom + "' AND '" + _param._diaryDateTo + "' ");
                stb.append(" ) ");
                stb.append(" SELECT ");
                stb.append(" T1.DATE, T2.YEAR, T2.SEMESTER ");
                stb.append(" FROM ALL_DATE T1 ");
                stb.append(" INNER JOIN SEMESTER_MST T2 ON T2.SEMESTER <> '9' AND T1.DATE BETWEEN T2.SDATE AND T2.EDATE ");
                stb.append(" ORDER BY ");
                stb.append("     T1.DATE ");
                return stb.toString();
            }
            
            private class Date {
                private final String _year;
                private final String _semester;
                private final String _date;
                private List _diaryList = new ArrayList();
//                private List _visitrecList = new ArrayList();
//                private List _visitrecCntList = new ArrayList();
                private List _attendCntList = new ArrayList();
                private String _sickT;
                private String _noticeT;
                private String _nonoticeT;
                private String _suspendT;
                private String _mourningT;
//                private List _attendList = new ArrayList();

                private Date(final String year, final String semester, final String date) {
                    _year = year;
                    _semester = semester;
                    _date = date;
                }

                /**
                 * 保健室日誌
                 */
                private void setDiary(final DB2UDB db2) throws SQLException, ParseException {
                    final String psKey = "DIARY";
                    if (null == getPs(psKey)) {
                        setPs(psKey, db2, getDiarySql());
                    }
                    PreparedStatement ps = getPs(psKey);
                    ResultSet rs = null;
                    try {
                        int i = 0;
                        ps.setString(++i, _date);
                        rs = ps.executeQuery();
                        while (rs.next()) {
                            final Diary diary = new Diary(
                                    rs.getString("WEATHER"),
                                    rs.getString("TEMPERATURE"),
                                    rs.getString("EVENT"),
                                    rs.getString("DIARY"),
                                    rs.getString("STAFFCD")
                            );
                            _diaryList.add(diary);
                        }
                    } finally {
                        DbUtils.closeQuietly(rs);
                        db2.commit();
                    }
                }

                /**
                 * 保健室日誌
                 */
                private String getDiarySql() {
                    final StringBuffer stb = new StringBuffer();
                    stb.append(" SELECT ");
                    stb.append("     A006.NAME1 AS WEATHER, ");
                    stb.append("     T1.TEMPERATURE, ");
                    stb.append("     T1.EVENT, ");
                    stb.append("     T1.DIARY, ");
                    stb.append("     T1.REGISTERCD AS STAFFCD ");
                    stb.append(" FROM ");
                    stb.append("     NURSEOFF_DIARY_DAT T1 ");
                    stb.append("     LEFT JOIN NAME_MST A006 ON A006.NAMECD1 = 'A006' ");
                    stb.append("                            AND A006.NAMECD2 = T1.WEATHER ");
                    stb.append(" WHERE ");
                    stb.append("     T1.DATE = ? ");
                    return stb.toString();
                }

                /**
                 * 出欠（件数）
                 */
                private void setAttendCnt(final DB2UDB db2) throws SQLException, ParseException {
                    ResultSet rs = null;
                    try {
                        int sickT = 0;
                        int noticeT = 0;
                        int nonoticeT = 0;
                        int suspendT = 0;
                        int mourningT = 0;

                        final String psKey = "ATTENDSEMES";
                        if (null == getPs(psKey)) {
                            final String sql = getAttendSemesSql(_definecode,
                                    _param._periodInState
                                    );
                            
                            setPs(psKey, db2, sql);
                            
                        }
                        PreparedStatement ps = getPs(psKey);
                        int i = 0;
                        ps.setString(++i, _year);
                        ps.setString(++i, _semester);
                        ps.setString(++i, _year);
                        ps.setString(++i, _semester);
                        ps.setString(++i, _date);
                        ps.setString(++i, _year);
                        rs = ps.executeQuery();
                        while (rs.next()) {
                            final String grade = rs.getString("GRADE");
                            final int sick = rs.getInt("SICK1");
                            final int notice = rs.getInt("SICK2");
                            final int nonotice = rs.getInt("SICK3");
                            final int suspend = rs.getInt("SUSPEND");
                            final int mourning = rs.getInt("MOURNING");

                            sickT += sick;
                            noticeT += notice;
                            nonoticeT += nonotice;
                            suspendT += suspend;
                            mourningT += mourning;

                            final AttendCnt attendCnt = new AttendCnt(
                                    grade,
                                    String.valueOf(sick),
                                    String.valueOf(notice),
                                    String.valueOf(nonotice),
                                    String.valueOf(suspend),
                                    String.valueOf(mourning)
                            );
                            _attendCntList.add(attendCnt);
                        }
                        //計
                        if (0 < sickT) _sickT = String.valueOf(sickT);
                        if (0 < noticeT) _noticeT = String.valueOf(noticeT);
                        if (0 < nonoticeT) _nonoticeT = String.valueOf(nonoticeT);
                        if (0 < suspendT) _suspendT = String.valueOf(suspendT);
                        if (0 < mourningT) _mourningT = String.valueOf(mourningT);
                    } finally {
                        DbUtils.closeQuietly(rs);
                        db2.commit();
                    }
                }

                /**
                 * 出欠データSQLを返す
                 * -- 開始日付の端数可
                 * -- 終了日付の端数可
                 * @param year          年度
                 * @param semester     対象学期範囲From
                 * @param periodInState 対象校時
                 * @return 出欠データSQL<code>String</code>を返す
                 */
                private String getAttendSemesSql(
                        final KNJDefineSchool definecode,
                        final String periodInState
                ) {
                    final StringBuffer stb = new StringBuffer();

                    //対象生徒
                    stb.append("WITH SCHNO AS( ");
                    stb.append(" SELECT ");
                    stb.append("    W1.SCHREGNO, ");
                    stb.append("    W1.GRADE, ");
                    stb.append("    W1.SEMESTER, ");
                    stb.append("    W1.HR_CLASS, ");
                    stb.append("    W3.HR_NAME, ");
                    stb.append("    W3.HR_NAMEABBV, ");
                    stb.append("    W1.ATTENDNO, ");
                    stb.append("    W4.NAME, ");
                    stb.append("    Z002.NAME2 AS SEX ");
                    stb.append(" FROM ");
                    stb.append("    SCHREG_REGD_DAT W1 ");
                    stb.append("    INNER JOIN SCHREG_REGD_HDAT W3 ON W3.YEAR = W1.YEAR ");
                    stb.append("                                  AND W3.SEMESTER = W1.SEMESTER ");
                    stb.append("                                  AND W3.GRADE = W1.GRADE ");
                    stb.append("                                  AND W3.HR_CLASS = W1.HR_CLASS ");
                    stb.append("    INNER JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
                    stb.append("    LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
                    stb.append("                           AND Z002.NAMECD2 = W4.SEX ");
                    stb.append(" WHERE ");
                    stb.append("    W1.YEAR = ? ");
                    stb.append("    AND W1.SEMESTER = ? ");

                    //対象生徒の時間割データ
                    stb.append(" ), SCHEDULE_SCHREG_R AS( ");
                    stb.append(" SELECT ");
                    stb.append("    T2.SCHREGNO, ");
                    stb.append("    T1.SEMESTER, ");
                    stb.append("    T1.EXECUTEDATE, ");
                    stb.append("    T1.PERIODCD ");
                    stb.append(" FROM ");
                    stb.append("    SCH_CHR_DAT T1, ");
                    stb.append("    CHAIR_STD_DAT T2 ");
                    stb.append(" WHERE ");
                    stb.append("    T1.YEAR = ? ");
                    stb.append("    AND T1.SEMESTER = ? ");
                    stb.append("    AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ");
                    stb.append("    AND T1.EXECUTEDATE = ? ");
                    stb.append("    AND T1.YEAR = T2.YEAR ");
                    stb.append("    AND T1.SEMESTER = T2.SEMESTER ");
                    stb.append("    AND T1.CHAIRCD = T2.CHAIRCD ");
                    if (definecode != null && definecode.usefromtoperiod)
                        stb.append("    AND T1.PERIODCD IN " + periodInState + " ");
                    stb.append("    AND T2.SCHREGNO IN(SELECT SCHREGNO FROM SCHNO GROUP BY SCHREGNO) ");
                    stb.append("    AND NOT EXISTS(SELECT ");
                    stb.append("                       'X' ");
                    stb.append("                   FROM ");
                    stb.append("                       SCHREG_BASE_MST T3 ");
                    stb.append("                   WHERE ");
                    stb.append("                       T3.SCHREGNO = T2.SCHREGNO ");
                    stb.append("                       AND ((ENT_DIV IN('4','5') AND EXECUTEDATE < ENT_DATE) ");
                    stb.append("                             OR (GRD_DIV IN('2','3') AND EXECUTEDATE > GRD_DATE)) ");
                    stb.append("                  ) ");
                    stb.append(" GROUP BY ");
                    stb.append("    T2.SCHREGNO, ");
                    stb.append("    T1.SEMESTER, ");
                    stb.append("    T1.EXECUTEDATE, ");
                    stb.append("    T1.PERIODCD ");

                    stb.append(" ), SCHEDULE_SCHREG AS( ");
                    stb.append(" SELECT ");
                    stb.append("    T1.SCHREGNO, ");
                    stb.append("    T1.SEMESTER, ");
                    stb.append("    T1.EXECUTEDATE, ");
                    stb.append("    T1.PERIODCD ");
                    stb.append(" FROM ");
                    stb.append("    SCHEDULE_SCHREG_R T1 ");
                    stb.append(" WHERE ");
                    stb.append("    NOT EXISTS(SELECT ");
                    stb.append("                       'X' ");
                    stb.append("                   FROM ");
                    stb.append("                       SCHREG_TRANSFER_DAT T3 ");
                    stb.append("                   WHERE ");
                    stb.append("                       T3.SCHREGNO = T1.SCHREGNO ");
                    stb.append("                       AND TRANSFERCD IN('1','2') ");
                    stb.append("                       AND EXECUTEDATE BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ");
                    stb.append("                  ) ");
                    stb.append(" GROUP BY ");
                    stb.append("    T1.SCHREGNO, ");
                    stb.append("    T1.SEMESTER, ");
                    stb.append("    T1.EXECUTEDATE, ");
                    stb.append("    T1.PERIODCD ");
                        
                    //対象生徒の出欠データ
                    stb.append(" ), T_ATTEND_DAT AS( ");
                    stb.append(" SELECT ");
                    stb.append("    T0.SCHREGNO, ");
                    stb.append("    T1.SEMESTER, ");
                    stb.append("    T0.ATTENDDATE, ");
                    stb.append("    T0.PERIODCD, ");
                    stb.append("    T0.DI_CD ");
                    stb.append(" FROM ");
                    stb.append("    ATTEND_DAT T0, ");
                    stb.append("    SCHEDULE_SCHREG T1 ");
                    stb.append(" WHERE ");
                    stb.append("    T0.YEAR = ? ");
                    stb.append("    AND T0.SCHREGNO = T1.SCHREGNO ");
                    stb.append("    AND T0.ATTENDDATE = T1.EXECUTEDATE ");
                    stb.append("    AND T0.PERIODCD = T1.PERIODCD ");
                    
                    //対象生徒の出欠データ（忌引・出停した日）
                    stb.append(" ), T_ATTEND_DAT_B AS( ");
                    stb.append(" SELECT ");
                    stb.append("    T0.SCHREGNO, ");
                    stb.append("    T0.SEMESTER, ");
                    stb.append("    T0.ATTENDDATE, ");
                    stb.append("    MIN(T0.PERIODCD) AS FIRST_PERIOD, ");
                    stb.append("    COUNT(DISTINCT T0.PERIODCD) AS PERIOD_CNT ");
                    stb.append(" FROM ");
                    stb.append("    T_ATTEND_DAT T0 ");
                    stb.append(" WHERE ");
                    stb.append("    DI_CD IN('2','3','9','10' ");
                    if ("true".equals(_param._useVirus)) {
                        stb.append("    ,'19','20'");
                    }
                    if ("true".equals(_param._useKoudome)) {
                        stb.append("    ,'25','26'");
                    }
                    stb.append("    ) ");
                    stb.append(" GROUP BY ");
                    stb.append("    T0.SCHREGNO, ");
                    stb.append("    T0.SEMESTER, ");
                    stb.append("    T0.ATTENDDATE ");
                    
                    //対象生徒の日単位の最小校時・最大校時・校時数
                    stb.append(" ), T_PERIOD_CNT AS( ");
                    stb.append(" SELECT ");
                    stb.append("    T1.SCHREGNO, ");
                    stb.append("    T1.SEMESTER, ");
                    stb.append("    T1.EXECUTEDATE, ");
                    stb.append("    MIN(T1.PERIODCD) AS FIRST_PERIOD, ");
                    stb.append("    MAX(T1.PERIODCD) AS LAST_PERIOD, ");
                    stb.append("    COUNT(DISTINCT T1.PERIODCD) AS PERIOD_CNT ");
                    stb.append(" FROM ");
                    stb.append("    SCHEDULE_SCHREG T1 ");
                    stb.append(" GROUP BY ");
                    stb.append("    T1.SCHREGNO, ");
                    stb.append("    T1.SEMESTER, ");
                    stb.append("    T1.EXECUTEDATE ");
                        
                    if ("1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
                        //対象生徒の日単位のデータ（忌引・出停した日）
                        stb.append(" ), T_PERIOD_SUSPEND_MOURNING AS( ");
                        stb.append(" SELECT ");
                        stb.append("    T0.SCHREGNO, ");
                        stb.append("    T0.EXECUTEDATE ");
                        stb.append(" FROM ");
                        stb.append("    T_PERIOD_CNT T0, ");
                        stb.append("    T_ATTEND_DAT_B T1 ");
                        stb.append(" WHERE ");
                        stb.append("        T0.SCHREGNO = T1.SCHREGNO ");
                        stb.append("    AND T0.EXECUTEDATE = T1.ATTENDDATE ");
                        stb.append("    AND T0.FIRST_PERIOD = T1.FIRST_PERIOD ");
                        stb.append("    AND T0.PERIOD_CNT = T1.PERIOD_CNT ");
                    }
                    stb.append(     ") ");
                    
                    //メイン表
                    stb.append(" SELECT ");
                    stb.append("    TT0.GRADE, ");
                    stb.append("    MAX(TT5.DINAME) AS DINAME, ");
                    stb.append("    MAX(TT5.DIREMARK) AS DIREMARK, ");
                    stb.append("    SUM(VALUE(TT5.SICK,0)) AS SICK1, ");
                    stb.append("    SUM(VALUE(TT5.NOTICE,0)) AS SICK2, ");
                    stb.append("    SUM(VALUE(TT5.NONOTICE,0)) AS SICK3, ");
                    stb.append("    SUM(VALUE(TT3.SUSPEND,0)) ");
                    if ("true".equals(_param._useVirus)) {
                        stb.append("        + SUM(VALUE(TT3_2.VIRUS,0)) ");
                    }
                    if ("true".equals(_param._useKoudome)) {
                        stb.append("        + SUM(VALUE(TT3_3.KOUDOME,0)) ");
                    }
                    stb.append("    AS SUSPEND, ");
                    stb.append("    SUM(VALUE(TT4.MOURNING,0)) AS MOURNING ");
                    stb.append(" FROM ");
                    stb.append("    SCHNO TT0 ");
                    //個人別出停日数
                    stb.append(" LEFT OUTER JOIN( ");
                    stb.append("    SELECT ");
                    stb.append("        W1.SCHREGNO, ");
                    stb.append("        VALUE(W1.SEMESTER, '9') AS SEMESTER, ");
                    stb.append("        COUNT(DISTINCT W1.ATTENDDATE) AS SUSPEND ");
                    stb.append("    FROM ");
                    stb.append("        T_ATTEND_DAT W1 ");
                    stb.append("    WHERE ");
                    stb.append("        W1.DI_CD IN ('2','9') ");
                    if ("1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
                        stb.append("    AND (W1.SCHREGNO, W1.ATTENDDATE) IN (SELECT T0.SCHREGNO, T0.EXECUTEDATE FROM T_PERIOD_SUSPEND_MOURNING T0) ");
                    }
                    stb.append("    GROUP BY ");
                    stb.append("        GROUPING SETS ((W1.SCHREGNO, W1.SEMESTER), (W1.SCHREGNO)) ");
                    stb.append("    ) TT3 ON TT0.SCHREGNO = TT3.SCHREGNO ");
                    stb.append("          AND TT0.SEMESTER = TT3.SEMESTER ");
                    //個人別伝染病日数
                    stb.append(" LEFT OUTER JOIN( ");
                    stb.append("    SELECT ");
                    stb.append("        W1.SCHREGNO, ");
                    stb.append("        VALUE(W1.SEMESTER, '9') AS SEMESTER, ");
                    stb.append("        COUNT(DISTINCT W1.ATTENDDATE) AS VIRUS ");
                    stb.append("    FROM ");
                    stb.append("        T_ATTEND_DAT W1 ");
                    stb.append("    WHERE ");
                    stb.append("        W1.DI_CD IN ('19','20') ");
                    if ("1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
                        stb.append("    AND (W1.SCHREGNO, W1.ATTENDDATE) IN (SELECT T0.SCHREGNO, T0.EXECUTEDATE FROM T_PERIOD_SUSPEND_MOURNING T0) ");
                    }
                    stb.append("    GROUP BY ");
                    stb.append("        GROUPING SETS ((W1.SCHREGNO, W1.SEMESTER), (W1.SCHREGNO)) ");
                    stb.append("    ) TT3_2 ON TT0.SCHREGNO = TT3_2.SCHREGNO ");
                    stb.append("          AND TT0.SEMESTER = TT3_2.SEMESTER ");
                    //個人別交止日数
                    stb.append(" LEFT OUTER JOIN( ");
                    stb.append("    SELECT ");
                    stb.append("        W1.SCHREGNO, ");
                    stb.append("        VALUE(W1.SEMESTER, '9') AS SEMESTER, ");
                    stb.append("        COUNT(DISTINCT W1.ATTENDDATE) AS KOUDOME ");
                    stb.append("    FROM ");
                    stb.append("        T_ATTEND_DAT W1 ");
                    stb.append("    WHERE ");
                    stb.append("        W1.DI_CD IN ('25','26') ");
                    if ("1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
                        stb.append("    AND (W1.SCHREGNO, W1.ATTENDDATE) IN (SELECT T0.SCHREGNO, T0.EXECUTEDATE FROM T_PERIOD_SUSPEND_MOURNING T0) ");
                    }
                    stb.append("    GROUP BY ");
                    stb.append("        GROUPING SETS ((W1.SCHREGNO, W1.SEMESTER), (W1.SCHREGNO)) ");
                    stb.append("    ) TT3_3 ON TT0.SCHREGNO = TT3_3.SCHREGNO ");
                    stb.append("          AND TT0.SEMESTER = TT3_3.SEMESTER ");
                    //個人別忌引日数
                    stb.append(" LEFT OUTER JOIN( ");
                    stb.append("    SELECT ");
                    stb.append("        W1.SCHREGNO, ");
                    stb.append("        VALUE(W1.SEMESTER, '9') AS SEMESTER, ");
                    stb.append("        COUNT(DISTINCT W1.ATTENDDATE) AS MOURNING ");
                    stb.append("    FROM ");
                    stb.append("        T_ATTEND_DAT W1 ");
                    stb.append("    WHERE ");
                    stb.append("        W1.DI_CD IN ('3','10') ");
                    if ("1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
                        stb.append("    AND (W1.SCHREGNO, W1.ATTENDDATE) IN (SELECT T0.SCHREGNO, T0.EXECUTEDATE FROM T_PERIOD_SUSPEND_MOURNING T0) ");
                    }
                    stb.append("    GROUP BY ");
                    stb.append("        GROUPING SETS ((W1.SCHREGNO, W1.SEMESTER), (W1.SCHREGNO)) ");
                    stb.append("    ) TT4 ON TT0.SCHREGNO = TT4.SCHREGNO ");
                    stb.append("          AND TT0.SEMESTER = TT4.SEMESTER ");
                    //個人別欠席日数
                    stb.append(" LEFT OUTER JOIN( ");
                    stb.append("    SELECT ");
                    stb.append("        W0.SCHREGNO, ");
                    stb.append("        VALUE(W1.SEMESTER, '9') AS SEMESTER, ");
                    stb.append("        MAX(CASE WHEN W0.DI_CD IN ('4','5','6','11','12','13') THEN C001.NAME1 END) AS DINAME, ");
                    stb.append("        MAX(CASE WHEN W0.DI_CD IN ('4','5','6','11','12','13') THEN W0.DI_REMARK END) AS DIREMARK, ");
                    stb.append("        SUM(CASE W0.DI_CD WHEN '4' THEN 1 WHEN '11' THEN 1 ELSE 0 END) AS SICK, ");
                    stb.append("        SUM(CASE W0.DI_CD WHEN '5' THEN 1 WHEN '12' THEN 1 ELSE 0 END) AS NOTICE, ");
                    stb.append("        SUM(CASE W0.DI_CD WHEN '6' THEN 1 WHEN '13' THEN 1 ELSE 0 END) AS NONOTICE ");
                    stb.append("    FROM ");
                    stb.append("        ATTEND_DAT W0 ");
                    stb.append("        LEFT JOIN NAME_MST C001 ON C001.NAMECD1 = 'C001' ");
                    stb.append("                               AND C001.NAMECD2 = W0.DI_CD, ");
                    stb.append("        (SELECT ");
                    stb.append("             T0.SCHREGNO, ");
                    stb.append("             T0.SEMESTER, ");
                    stb.append("             T0.EXECUTEDATE, ");
                    if ("1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
                        stb.append("         T2.FIRST_PERIOD ");
                    } else {
                        stb.append("         T0.FIRST_PERIOD ");
                    }
                    stb.append("         FROM ");
                    stb.append("             T_PERIOD_CNT T0, ");
                    stb.append("             ( ");
                    stb.append("              SELECT ");
                    stb.append("                  W1.SCHREGNO, W1.ATTENDDATE, ");
                    stb.append("                  MIN(W1.PERIODCD) AS FIRST_PERIOD, ");
                    stb.append("                  COUNT(W1.PERIODCD) AS PERIOD_CNT ");
                    stb.append("              FROM ");
                    stb.append("                  T_ATTEND_DAT W1 ");
                    stb.append("              WHERE ");
                    if ("1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
                        stb.append("              W1.DI_CD IN ('4','5','6','11','12','13','2','9','3','10' ");
                        if ("true".equals(_param._useVirus)) {
                            stb.append("    ,'19','20'");
                        }
                        if ("true".equals(_param._useKoudome)) {
                            stb.append("    ,'25','26'");
                        }
                        stb.append("              ) ");
                    } else {
                        stb.append("              W1.DI_CD IN ('4','5','6','11','12','13') ");
                    }
                    stb.append("              GROUP BY ");
                    stb.append("                  W1.SCHREGNO, ");
                    stb.append("                  W1.ATTENDDATE ");
                    stb.append("             ) T1 ");
                    if ("1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
                        stb.append("         INNER JOIN ( ");
                        stb.append("              SELECT ");
                        stb.append("                  W1.SCHREGNO, W1.ATTENDDATE, ");
                        stb.append("                  MIN(W1.PERIODCD) AS FIRST_PERIOD, ");
                        stb.append("                  COUNT(W1.PERIODCD) AS PERIOD_CNT ");
                        stb.append("              FROM ");
                        stb.append("                  T_ATTEND_DAT W1 ");
                        stb.append("              WHERE ");
                        stb.append("                  W1.DI_CD IN ('4','5','6','11','12','13') ");
                        stb.append("              GROUP BY ");
                        stb.append("                  W1.SCHREGNO, ");
                        stb.append("                  W1.ATTENDDATE ");
                        stb.append("             ) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.ATTENDDATE = T1.ATTENDDATE ");
                    }
                    stb.append("         WHERE ");
                    stb.append("             T0.SCHREGNO = T1.SCHREGNO ");
                    stb.append("             AND T0.EXECUTEDATE = T1.ATTENDDATE ");
                    stb.append("             AND T0.FIRST_PERIOD = T1.FIRST_PERIOD ");
                    stb.append("             AND T0.PERIOD_CNT = T1.PERIOD_CNT ");
                    stb.append("        ) W1 ");
                    stb.append("    WHERE ");
                    stb.append("        W0.SCHREGNO = W1.SCHREGNO ");
                    stb.append("        AND W0.ATTENDDATE = W1.EXECUTEDATE ");
                    stb.append("        AND W0.PERIODCD = W1.FIRST_PERIOD ");
                    if ("1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
                        stb.append("    AND (W1.SCHREGNO, W1.EXECUTEDATE) NOT IN (SELECT T0.SCHREGNO, T0.EXECUTEDATE FROM T_PERIOD_SUSPEND_MOURNING T0) ");
                    }
                    stb.append("    GROUP BY ");
                    stb.append("        GROUPING SETS ((W0.SCHREGNO, W1.SEMESTER), (W0.SCHREGNO)) ");
                    stb.append("    )TT5 ON TT0.SCHREGNO = TT5.SCHREGNO ");
                    stb.append("         AND TT0.SEMESTER = TT5.SEMESTER ");
                    
                    stb.append(" GROUP BY ");
                    stb.append("    TT0.GRADE ");
                    stb.append("    HAVING 0 < SUM(VALUE(TT5.SICK,0)) ");
                    stb.append("        OR 0 < SUM(VALUE(TT5.NOTICE,0)) ");
                    stb.append("        OR 0 < SUM(VALUE(TT5.NONOTICE,0)) ");
                    stb.append("        OR 0 < SUM(VALUE(TT3.SUSPEND,0)) ");
                    if ("true".equals(_param._useVirus)) {
                        stb.append("        + SUM(VALUE(TT3_2.VIRUS,0)) ");
                    }
                    if ("true".equals(_param._useKoudome)) {
                        stb.append("        + SUM(VALUE(TT3_3.KOUDOME,0)) ");
                    }
                    stb.append("        OR 0 < SUM(VALUE(TT4.MOURNING,0)) ");
                    
                    stb.append(" ORDER BY ");
                    stb.append("    TT0.GRADE ");

                    return stb.toString();
                }
                
                public String toString() {
                    return "KNJF175.Date(" + _date + ")";
                }
            }
            
            /**
             * 出欠（件数）の内部クラス
             */
            private class AttendCnt {
                private final String _grade;
                private final String _sick;
                private final String _notice;
                private final String _nonotice;
                private final String _suspend;
                private final String _mourning;

                private AttendCnt(
                        final String grade,
                        final String sick,
                        final String notice,
                        final String nonotice,
                        final String suspend,
                        final String mourning
                ) {
                    _grade = grade;
                    _sick = getVal(sick);
                    _notice = getVal(notice);
                    _nonotice = getVal(nonotice);
                    _suspend = getVal(suspend);
                    _mourning = getVal(mourning);
                }

                /**
                 * ゼロは印字しない。
                 */
                public String getVal(final String val) {
                    return "0".equals(val) ? null : val;
                }

                /**
                 * 学年列の取得
                 */
                public int getGradeInt() {
                    return Integer.parseInt(_grade);
                }

                public String toString() {
                    return "学年：" + _grade;
                }
            }
            
            /**
             * 保健室日誌の内部クラス
             */
            private class Diary {
                private final String _weather;
                private final String _temperature;
                private final String _event;
                private final String _diary;
                private final String _staffcd;

                private Diary(
                        final String weather,
                        final String temperature,
                        final String event,
                        final String diary,
                        final String staffcd
                ) {
                    _weather = weather;
                    _temperature = temperature;
                    _event = event;
                    _diary = diary;
                    _staffcd = staffcd;
                }

                public String toString() {
                    return "天気：" + _weather;
                }
            }
        }
    }
    

    // 区分ごとに出力
    private static abstract class OutputFmtDiv extends OutputFmt {
        final String _divSql;
        
        protected static interface SheetFormatter {
            void setPs(PreparedStatement ps, Map map) throws SQLException;
            String formatSheetName(Map map);
        }

        final SheetFormatter _sheetFormatter;
        
        OutputFmtDiv(final String divSql, final SheetFormatter sheetFormatter) {
            _divSql = divSql;
            _sheetFormatter = sheetFormatter;
        }
        public void output0(final DB2UDB db2, final OutputWrapper ow) throws OutputWrapper.OutputExceptionWrapper {
            outputByDiv(db2, ow);
        }
        
        public void outputByDiv(final DB2UDB db2, final OutputWrapper ow) throws OutputWrapper.OutputExceptionWrapper {
            if (ow._outputToFile && _outputExcellSplitSheet) {
                ow._book.removeSheetAt(0);
            }
            final Collection divList = QueryUtil.getRowList(db2, _divSql);

            PreparedStatement ps = null;
            try {
                ps = db2.prepareStatement(_sql);

                boolean outputBefore = false;
                for (final Iterator it = divList.iterator(); it.hasNext();) {
                    final Map yearMap = (Map) it.next();

                    _sheetFormatter.setPs(ps, yearMap);
                    
                    final List lines = getDataLines(_cd, ps, _flds);
                    if (lines.size() > 0) {
                        if (_outputExcellSplitSheet) {
                            ow.setNewSheet(_sheetFormatter.formatSheetName(yearMap));
                        } else if (!_outputExcellSplitSheet && outputBefore) {
                            ow.outputLines(blankLine());
                        }
                        
                        ow.outputLines(getTitleLines(_titleLines, yearMap));
                        ow.outputLines(getHeaderLines(_flds));
                        ow.outputLines(lines);
                        outputBefore = true;
                    }
                    
                }
            } catch (Exception e) {
                if (e instanceof OutputWrapper.OutputExceptionWrapper) {
                    throw (OutputWrapper.OutputExceptionWrapper) e;
                } else {
                    log.fatal("exception! cd = " + _cd + ", sql = " + _sql, e);
                }
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }
    }
    
    // 年度ごとに出力
    private static class OutputFmtYear extends OutputFmtDiv {
        
        private static class Year implements SheetFormatter {
            public void setPs(final PreparedStatement ps, final Map map) throws SQLException {
                ps.setString(1, getString("YEAR", map));
            }
            public String formatSheetName(final Map map) {
                return getString("YEAR", map) + "年度";
            }
        }

        OutputFmtYear(final String yearSql) {
            super(yearSql, new Year());
        }
    }
    
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.info("$Revision: 56595 $");
        final List paramlist = new ArrayList();
        final Enumeration e = request.getParameterNames();
        while (e.hasMoreElements()) {
            paramlist.add(e.nextElement());
        }
        Collections.sort(paramlist);
        for (final Iterator it = paramlist.iterator(); it.hasNext();) {
            final String name = (String) it.next();
            final String[] values = request.getParameterValues(name);
            log.info("parameter:name=" + name + ", value=[" + StringUtils.join(values, ',') + "]");
        }
        return param;
    }

    /** パラメータクラス */
    private static class Param {

        private static final DecimalFormat MEMDF = new DecimalFormat("###,### 'KB'");

        final String _year;
        final String _cd;
        final String _filename;
        final String _ikoSchPtrnDatYear = "2014";
        final String _noDelt;
        final String _altSemDet;
        final boolean _isSidoShoken2;

        File _outputDir = null;
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _cd = request.getParameter("CD");
            _noDelt = request.getParameter("NO_DELT");
            _filename = getCodeFilename(db2, _cd);
            _altSemDet = request.getParameter("ALT_SEM_DET");
            _isSidoShoken2 = 0 < Integer.parseInt(StringUtils.defaultString(QueryUtil.getOne(db2, "select count(*) from menu_mst where programid = 'KNJA130D'"), "0"));
        }

        protected String getCodeFilename(final DB2UDB db2, final String cd) throws SQLException {
            return QueryUtil.getOne(db2, "select name from tori_ikou_dat where cd = '" + cd + "' ");
        }

        public String getOutputFilename(final DB2UDB db2, final String cd, final String infix, final String ext) throws SQLException, IOException {
            if (null == _outputDir) {
                final String dir0 = "/tmp";

                final File dir = new File(dir0 + "/" + getOutputTime());
                if (dir.exists()) {
                    if (!dir.isDirectory()) {
                        log.error("not directory :" + dir.getAbsolutePath());
                    } 
                } else if (!dir.mkdir()) {
                    log.error("couldn't make directory :" + dir.getAbsolutePath());
                } else {
                    _outputDir = dir;
                }
            }
            return _outputDir.getAbsolutePath() + "/" + getCodeFilename(db2, cd) + infix + ext;
        }

        private static String getOutputTime() {
            final Calendar cal = Calendar.getInstance();
            final DecimalFormat df = new DecimalFormat("00");
            final String year = String.valueOf(cal.get(Calendar.YEAR));
            final String month = df.format(cal.get(Calendar.MONTH) + 1);
            final String day = df.format(cal.get(Calendar.DAY_OF_MONTH));
            final String hour = df.format(cal.get(Calendar.HOUR_OF_DAY));
            final String minute = df.format(cal.get(Calendar.MINUTE));
            final String second = df.format(cal.get(Calendar.SECOND));
            final String outputTime = year + month + day + "_" + hour + minute + second;
            return outputTime;
        }
        
        private Collection getTargetCodes(final DB2UDB db2) {
            final Set cds = new TreeSet();
            cds.add(_cd);

            final Collection toriIkouDatList = QueryUtil.getRowList(db2, "select cd from tori_ikou_dat where target is not null order by cd ");
            for (final Iterator it = toriIkouDatList.iterator(); it.hasNext();) {
                final Map m = (Map) it.next();
                cds.add(OutputFmt.getString("CD", m));
            }
            return cds;
        }
        
//        private void createZipfile() {
//            ZipArchiveOutputStream zos = null;
//            try {
//                final File dir = new File(_outputDir);
//                if (dir.exists() && dir.isDirectory() && dir.listFiles().length > 0) {
//                    zos = new ZipArchiveOutputStream(new FileOutputStream(_outputDir + ".zip"));
//                    zos.setEncoding("MS932");
//                    final File[] list = dir.listFiles();
//                    for (int i = 0; i < list.length; i++) {
//                        zos.putArchiveEntry(zos.createArchiveEntry(list[i], list[i].getName()));
//
//                        final InputStream is = new FileInputStream(list[i]);
//                        final byte[] data = new byte[1024];
//                        int size = 0;
//                        while ((size = is.read(data)) > 0) {
//                            zos.write(data, 0, size);
//                        }
//                        closeQuietly(is);
//                        zos.closeArchiveEntry();
//                    }
//                }
//            } catch (Exception e) {
//                log.fatal("exception!", e);
//            } finally {
//                closeQuietly(zos);
//            }
//        }
        
        private static void printMemoryInfo(final String head) {
            final Runtime rt = Runtime.getRuntime();
            final double t = rt.totalMemory() / 1024;
            final double f = rt.freeMemory() / 1024;
            final double m = rt.maxMemory() / 1024;
            final double u = t - f;
            System.out.println(head + " memory[ using=" + (MEMDF.format(u)) + " free=" + (MEMDF.format(f)) + " max=" + (MEMDF.format(m)) + " total=" + (MEMDF.format(t))+(" ]"));
        }
    }
    
}

// eof
/*

drop table tori_ikou_dat

create table tori_ikou_dat (
 cd varchar(2) not null,
 name varchar(60) ,
 target varchar(1)
) in usr1dms index in idx1dms

alter table tori_ikou_dat add constraint pk_tori_ikou_dat primary key (cd)

insert into tori_ikou_dat
values('01', '01-学校mD', '1') union all 
values('02', '02-課程mD', '1') union all 
values('03', '03-学科mD', '1') union all 
values('04', '04-系列mD', '1') union all 
values('05', '05-役職', '1') union all 
values('06', '06-分掌', '1') union all 
values('07', '07-職員', '1') union all 
values('08', '08-教室', '1') union all 
values('09', '09-駅mD', '1') union all 
values('10', '10-教科書', '1') union all 
values('11', '11-学期期間D', '1') union all 
values('12', '12-部活mD', '1') union all 
values('13', '13-委員mD', '1') union all 
values('14', '14-学校区mD', '1') union all 
values('15', '15-入学区分D', '1') union all 
values('16', '16-通学mD', '1') union all 
values('17', '17-続柄mD', '1') union all 
values('18', '18-中学校', '1') union all 
values('19', '19-前籍校', '1') union all 
values('20', '20-年度クラス', '1') union all 
values('21', '21-主催者mD', '1') union all 
values('22', '22-教科', '1') union all 
values('23', '23-正式科目名', '1') union all 
values('24', '24-校内科目', '1') union all 
values('25', '25-講座', '1') union all 
values('26', '26-講座使用教科書D', '1') union all 
values('27', '27-生徒', '1') union all 
values('28', '28-生徒変更履歴D', '1') union all 
values('29', '29-生徒所見D', '1') union all 
values('30', '30-生徒転入用備考D', '1') union all 
values('31', '31-生徒部活委員', '1') union all 
values('32', '32-クラス編成', '1') union all 
values('33', '33-異動情報D', '1') union all 
values('34', '34-休学履歴D', '1') union all 
values('35', '35-留学履歴D', '1') union all 
values('36', '36-証明書発行台帳D', '1') union all 
values('37', '37-評価基準mD', '1') union all 
values('38', '38-評定基準mD', '1') union all 
values('39', '39-考査', '1') union all 
values('40', '40-成績(点数)', '1') union all 
values('41', '41-成績(評定)', '1') union all 
values('42', '42-通知表所見', '1') union all 
values('43', '43-年間成績情報', '1') union all 
values('44', '44-増加単位', '1') union all 
values('45', '45-欠課種類mD', '1') union all 
values('46', '46-欠席種類mD', '1') union all 
values('47', '47-集計出欠期間D', '1') union all 
values('48', '48-集計値講座', '1') union all 
values('49', '49-集計値クラス', '1') union all 
values('50', '50-日々講座出欠', '1') union all 
values('51', '51-日々クラス出欠', '1') union all 
values('52', '52-特活時間数D', '1') union all 
values('53', '53-特活認定数D', '1') union all 
values('54', '54-年間出欠情報', '1') union all 
values('55', '55-学期別時間割名D', '1') union all 
values('56', '56-時間割', '1') union all 
values('57', '57-年度休校日', '1') union all 
values('58', '58-授業実施日予定日', '1') union all 
values('59', '59-受講生', '1') union all 
values('60', '60-文例情報', '1') union all 
values('61', '61-指導要録所見', '1') union all 
values('62', '62-進学用調査書所見', '1') union all 
values('63', '63-就職用調査書所見', '1') union all 
values('64', '64-概評段階人数D', '1') union all 
values('65', '65-保健室発生場所mD', '1') union all 
values('66', '66-保健室部位mD', '1') union all 
values('67', '67-保健室原因mD', '1') union all 
values('68', '68-保健室時間mD', '1') union all 
values('69', '69-保健室症状mD', '1') union all 
values('70', '70-保健室処置mD', '1') union all 
values('71', '71-保健室来室情報D', '1') union all 
values('72', '72-保健日報D', '1') union all 
values('73', '73-健康診断検査区分mD', '1') union all 
values('74', '74-健康診断情報', '1') union all 
values('75', '75-健康診断歯式情報', '1') union all 
values('76', '76-既往症', '1') union all 
values('77', '77-進路区分mD', '1') union all 
values('78', '78-進路推薦区分mD', '1') union all 
values('79', '79-進路先m', '1') union all 
values('80', '80-進路希望', '1')

;;;;;;;;;;;;;;

select * from menu_mst where menuid like 'K%'

insert into menu_mst
(menuid, submenuid, parentmenuid, menuname, programid, programpath, processcd)
values('K1003', 'K', 'K1000', 'データ移行2014', 'KNJK_TR14', '/K/KNJK_TR14', '1', '0')

insert into userauth_dat 
select '00999999', menuid, '0', 'alp', current timestamp from menu_mst where menuid like 'K%'


*/

/*

with subs as (
    select t1.classcd, t1.school_kind, t1.curriculum_cd, t1.subclasscd, 
    value(t3.subclassordername1, t3.subclassname, t1.subclassordername1, t1.subclassname) as subclassname
    from subclass_mst t1
    inner join (
        select subclasscd
        from subclass_mst 
        group by subclasscd
        having count(distinct curriculum_cd) > 1
    ) t2 on t2.subclasscd = t1.subclasscd
    left join subclass_mst t3 on t3.classcd = t1.classcd and t3.school_Kind = t1.school_kind and t3.curriculum_cd = t1.curriculum_cd and t3.subclasscd = t1.subclasscd2
), tgt as (
    select t1.*, row_number() over(partition by t1.subclasscd order by curriculum_cd) as rownum
    from subs t1
    inner join (
        select subclasscd
        from subs t2 
        group by subclasscd
        having count(distinct subclassname) > 1
     ) t2 on t2.subclasscd = t1.subclasscd
), withkoho as (
    select t1.classcd, t1.school_kind, t1.curriculum_cd, t1.subclasscd, t1.subclassname, t1.rownum
    , max(tr2.subclasscd) as m2, max(tr3.subclasscd) as m3, max(tr4.subclasscd) as m4, max(tr5.subclasscd) as m5, t1.subclasscd as m6
    from tgt t1
    left join subclass_mst tr2 on substr(tr2.subclasscd, 1, 2) = substr(t1.subclasscd, 1, 2)
    left join subclass_mst tr3 on substr(tr3.subclasscd, 1, 3) = substr(t1.subclasscd, 1, 3)
    left join subclass_mst tr4 on substr(tr4.subclasscd, 1, 4) = substr(t1.subclasscd, 1, 4)
    left join subclass_mst tr5 on substr(tr5.subclasscd, 1, 5) = substr(t1.subclasscd, 1, 5)
    group by t1.classcd, t1.school_kind, t1.curriculum_cd, t1.subclasscd, t1.subclassname, t1.rownum
), can1 as (
select t1.classcd, t1.school_kind, t1.curriculum_cd, t1.subclasscd,t1.subclassname,t1.rownum,
 t1.m2, 
 t1.m3, right('000' || trim(char(int(substr(t1.m3, 1, 3)) + 1)), 3) || substr(t1.m3, 3 + 1, 6 - 3) as can3,
 t1.m4, right('000' || trim(char(int(substr(t1.m4, 1, 4)) + 1)), 4) || substr(t1.m4, 4 + 1, 6 - 4) as can4,
 t1.m5, right('000' || trim(char(int(substr(t1.m5, 1, 5)) + 1)), 5) || substr(t1.m5, 5 + 1, 6 - 5) as can5,
 t1.m6, right('000' || trim(char(int(m6) + 1)), 6) as can6
from withkoho t1
), can2 as (
select t1.classcd, t1.school_kind, t1.curriculum_cd, t1.subclasscd,t1.subclassname,t1.rownum,
 t1.m2,
 t1.m3, case when (select max(subclasscd) from subclass_mst where can3 = subclasscd) is not null then cast(null as varchar(1)) else can3 end as can3,
 t1.m4, case when (select max(subclasscd) from subclass_mst where can4 = subclasscd) is not null then cast(null as varchar(1)) else can4 end as can4,
 t1.m5, case when (select max(subclasscd) from subclass_mst where can5 = subclasscd) is not null then cast(null as varchar(1)) else can5 end as can5,
 t1.m6, case when (select max(subclasscd) from subclass_mst where can6 = subclasscd) is not null then cast(null as varchar(1)) else can6 end as can6
from can1 t1
)
select t1.classcd, t1.school_kind, t1.curriculum_cd, t1.subclasscd,
 value(can5, can6, can4, can3) as tgt_subclasscd,t1.subclassname,t1.rownum,
 hen.tgt_subclasscd as set, chk.subclasscd as chk_subclasscd
from can2 t1
left join tori_subclass_henkan hen on hen.classcd = t1.classcd and hen.school_kind = t1.school_Kind and hen.curriculum_cd = t1.curriculum_cd
 and hen.subclasscd = t1.subclasscd
left join (select distinct subclasscd from subclass_mst) chk on chk.subclasscd = hen.tgt_subclasscd
where rownum > 1
order by t1.subclasscd, t1.curriculum_cd

;
78 済
79 済
80 なし
81 済
82 済

;

 create table tori_subclass_henkan (
    CLASSCD             VARCHAR(2)   NOT NULL,
    SCHOOL_KIND         VARCHAR(2)   NOT NULL,
    CURRICULUM_CD       VARCHAR(2)   NOT NULL,
    SUBCLASSCD          VARCHAR(6)   NOT NULL,
    TGT_SUBCLASSCD        VARCHAR(6)
 ) in usr1dms index in idx1dms

alter table tori_subclass_henkan add constraint PK_TORI_SUBC_H primary key (CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD)


*/

/* 
 * 境高校の出欠〆日が学期詳細と合わない件について
学期詳細の開始・終了日を出欠〆日に合わせたダミーテーブルを作成し、抽出はこのテーブルを使用する 
  
create table semester_detail_mst_dummy like semester_detail_mst

alter table semester_detail_mst_dummy add constraint semdetm_dum primary key (year, semester, semester_detail)

insert into semester_detail_mst_dummy
with t1 as (
select t1.year, t1.semester, t1.month, t1.appointed_day, count(*) as count
from attend_semes_dat t1
group by t1.year, t1.semester, t1.month, t1.appointed_day
), dets as (
select t1.year, t1.semester, row_number() over(
 partition by year order by semester, int(t1.month) + case when t1.month <= '03' then 12 else 0 end, appointed_day) as det
, t1.month, t1.appointed_day, t1.count
from t1
order by t1.year, t1.semester, int(t1.month) + case when t1.month <= '03' then 12 else 0 end, t1.appointed_day
), dets2 as (
select t1.*,
cast(t1.det as char(1)) as cdet,
date(case when t1.month <= '03' then cast(int(t1.year) + 1 as char(4)) else t1.year end || '-' || t1.month || '-' || t1.appointed_day) as simebi
from dets t1
)
select t1.year, t1.semester, t1.cdet, t4.semestername,
     value(add_days(t2.simebi, 1), t3.sdate) as sdate,
    t1.simebi as edate, 'dummy', current timestamp
from dets2 t1
left join dets2 t2 on t2.year = t1.year and t2.det = t1.det - 1
left join semester_mst t3 on t3.year = t1.year and t3.semester = '1'
left join semester_detail_mst t4 on t4.year = t1.year and t4.semester_detail = t1.cdet
order by t1.year, t1.semester, t1.det
;

select 'det', year, count(semester_detail), max(semester_detail) from semester_detail_mst group by year
union all
select 'dum', year, count(semester_detail), max(semester_detail) from semester_detail_mst_dummy group by year

select * 
from semester_detail_mst
order by year, semester, semester_detail

select * 
from semester_detail_mst_dummy
order by year, semester, semester_detail

*/

/* 実力テスト出力用マスタ

drop table tori_mock_mst

create table tori_mock_mst (year varchar(4) not null, mockcd varchar(9) not null, semester varchar(1) not null, seq varchar(2), name1 varchar(60), name2 varchar(60), name3 varchar(60)) in usr1dms index in idx1dms

alter table tori_mock_mst add constraint PK_tori_mock primary key (YEAR,MOCKCD)

select * from semester_mst where semester <> '9' order by year

select t2.year, t1.mockcd, t1.mockname1, t1.mockname2, t1.mockname3, count(*), min(t2.updated), max(t2.updated)
from mock_mst t1
inner join mock_dat t2 on t2.mockcd = t1.mockcd
group by t2.year, t1.mockcd, t1.mockname1, t1.mockname2, t1.mockname3

*/

