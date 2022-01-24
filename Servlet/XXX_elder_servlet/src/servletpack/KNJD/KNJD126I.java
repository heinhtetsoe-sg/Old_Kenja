/**
 *
 *	学校教育システム 賢者 [成績管理]
 *
 *					＜ＫＮＪＤ１２６Ｊ＞  観点別成績・評価チェックリスト（中学）
 *
 *	2011/01/07 nakamoto 作成日
 */

package servletpack.KNJD;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJD126I {

    private static final Log log = LogFactory.getLog(KNJD126I.class);

    private String _ctrlY;
    private String _semester2;
    private String _ctrlD;
    private String _semester;
    private String _useCurriculumcd;
    private String _kantenHyouji;
    private String _useTestCountflg;
    private String _z009;
    private String _useJviewStatusNotCd;

    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        log.fatal(" $Revision: 61038 $ $Date: 2018-07-05 09:20:16 +0900 (木, 05 7 2018) $");
        KNJServletUtils.debugParam(request, log);

        Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        String as[] = new String[17];
        HashMap hashmap = new HashMap();
        try {
            _ctrlY = request.getParameter("CTRL_Y");//今年度
            _semester2 = request.getParameter("SEMESTER2");//学期 観点データ以外用 1:前期,2:後期
            _ctrlD = request.getParameter("CTRL_D");//生徒を抽出する日付 学籍処理日または学期終了日
            _semester = request.getParameter("SEMESTER");//学期 観点データ用 1:前期,9:学年
            as[4] = request.getParameter("CLASSCD");//教科
            as[5] = request.getParameter("CHAIRCD");//講座
            as[12] = StringUtils.split(as[5], ":")[1];//科目
            as[5] = StringUtils.split(as[5], ":")[0];//講座
            _useCurriculumcd = request.getParameter("useCurriculumcd");//プロパティ(教育課程コード)(1:教育課程対応)
            _kantenHyouji = request.getParameter("kantenHyouji");//プロパティ(6:観点の種類が6種類)
            as[15] = "1".equals(_useCurriculumcd) ? StringUtils.split(as[4], "-")[0] : as[4];//観点の頭2桁
            _useTestCountflg = request.getParameter("useTestCountflg");
            _z009 = request.getParameter("Z009");
            _useJviewStatusNotCd =  request.getParameter("useJviewStatus_NotHyoji"); //表示しないコード
        }
        catch(Exception exception) {
            log.warn("parameter error!", exception);
        }
        PrintWriter printwriter = new PrintWriter(response.getOutputStream());
        response.setContentType("application/pdf");
        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        }
        catch(Exception exception1) {
            log.error("DB2 open error!", exception1);
            return;
        }
        boolean flag = false;
        getHeaderData(db2, svf, as);
        for(int j = 0; j < as.length; j++)
            log.debug("param[" + j + "]=" + as[j]);

        if(printMain(db2, svf, as, hashmap))
            flag = true;
        log.debug("nonedata=" + flag);
        if(!flag) {
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note", "");
            svf.VrEndPage();
        }
        svf.VrQuit();
        db2.commit();
        db2.close();
        printwriter.close();
    }

    private void getHeaderData(DB2UDB db2, Vrw32alp svf, String as[]) {
        //観点の種類が6種類
        if ("6".equals(_kantenHyouji)) {
            svf.VrSetForm("KNJD126I_2.frm", 1);
        } else {
            svf.VrSetForm("KNJD126I.frm", 1);
        }
        try {
            as[7] = KNJ_EditDate.gengou(db2, Integer.parseInt(_ctrlY)) + "\u5E74\u5EA6";//平成XX年度
        }
        catch(Exception exception) {
            log.warn("nendo get error!", exception);
        }
        try {
            String s = "VALUES RTRIM(CHAR(DATE(SYSDATE()))),RTRIM(CHAR(HOUR(SYSDATE()))),RTRIM(CHAR(MINUTE(SYSDATE())))";
            db2.query(s);
            ResultSet resultset3 = db2.getResultSet();
            String as1[] = new String[3];
            for(int j = 0; resultset3.next(); j++)
                as1[j] = resultset3.getString(1);

            db2.commit();
            as[8] = KNJ_EditDate.h_format_JP(db2, as1[0]) + as1[1] + "\u6642" + as1[2] + "\u5206" + "\u3000\u73FE\u5728";//平成XX年XX月XX日XX時XX分　現在
        }
        catch(Exception exception1) {
            log.warn("ctrl_date get error!", exception1);
        }
        try {
            db2.query("SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = '" + _z009 + "' AND NAMECD2 = '" + _semester + "'");
            for(ResultSet resultset = db2.getResultSet(); resultset.next();)
                as[9] = resultset.getString("NAME1");//管理者コントロール 観点名称 1:前期,9:学年

            db2.commit();
        }
        catch(Exception exception2) {
            log.warn("name1 get error!", exception2);
        }
        try {
            final String sqlClassMst;
            if ("1".equals(_useCurriculumcd)) {
                sqlClassMst = "SELECT CLASSNAME FROM CLASS_MST WHERE CLASSCD || '-' || SCHOOL_KIND = '" + as[4] + "'";
            } else {
                sqlClassMst = "SELECT CLASSNAME FROM CLASS_MST WHERE CLASSCD = '" + as[4] + "'";
            }
            db2.query(sqlClassMst);
            for(ResultSet resultset1 = db2.getResultSet(); resultset1.next();)
                as[10] = resultset1.getString("CLASSNAME");//教科名称

            db2.commit();
        }
        catch(Exception exception3) {
            log.warn("classname get error!", exception3);
        }
        try {
            db2.query("SELECT CHAIRNAME FROM CHAIR_DAT WHERE YEAR = '" + _ctrlY + "' AND SEMESTER = '" + _semester2 + "' AND CHAIRCD = '" + as[5] + "'");
            for(ResultSet resultset2 = db2.getResultSet(); resultset2.next();)
                as[11] = resultset2.getString("CHAIRNAME");//講座名称

            db2.commit();
        }
        catch(Exception exception4) {
            log.warn("chairname get error!", exception4);
        }
        try {
            final String setNameCd1 = "9".equals(_semester) ? "D028": "D029";
            db2.query("SELECT ABBV1 FROM NAME_MST WHERE NAMECD1 = '" + setNameCd1 + "' AND NAMECD2 = '" + _useJviewStatusNotCd + "'");
            for(ResultSet resultset = db2.getResultSet(); resultset.next();)
                as[16] = resultset.getString("ABBV1");//表示しないコード

            db2.commit();
        }
        catch(Exception exception5) {
            log.warn("d028d029 get error!", exception5);
        }
    }

    private boolean printMain(DB2UDB db2, Vrw32alp svf, String as[], Map map) {
        boolean flag = false;
        try {
            getJviewname(db2, svf, as, map);
            if(printMeisai(db2, svf, as, map))
                flag = true;
        }
        catch(Exception exception) {
            log.warn("printMain read error!", exception);
        }
        return flag;
    }

    private void getJviewname(DB2UDB db2, Vrw32alp svf, String as[], Map map) {
        try {
            log.debug("Jviewname start!");
            db2.query(statementJviewname(as));
            ResultSet resultset = db2.getResultSet();
            log.debug("Jviewname end!");
            int i = 0;
            String s = "";
            int maxlen = ("6".equals(_kantenHyouji)) ? 6 : 5;
            for(; resultset.next(); map.put(resultset.getString("VIEWCD"), String.valueOf(++i))) {
                if(i == maxlen)
                    break;
                if(i == 0)
                    as[6] = "";
                as[6] = as[6] + s + resultset.getString("VIEWCD");//観点コード 観点状況
                s = "','";
            }

            if(0 < i) {
                as[6] = "('" + as[6] + s + as[15] + "99" + "')";//観点コード 評定
                map.put(as[15] + "99", "9");
            }
            log.debug("param[6]=" + as[6]);
            resultset.close();
            db2.commit();
        }
        catch(Exception exception) {
            log.warn("getJviewname read error!", exception);
        }
    }

    private boolean printMeisai(DB2UDB db2, Vrw32alp svf, String as[], Map map) {
        boolean flag = false;
        try {
            log.debug("Meisai start!");
            PreparedStatement preparedstatement = db2.prepareStatement(statementMeisai(as));
            ResultSet resultset = preparedstatement.executeQuery();
            log.debug("Meisai end!");
            int k = 0;
            String s = "0";
            while(resultset.next())  {
                if(!s.equals("0") && !s.equals(resultset.getString("SCHREGNO")))
                    k++;
                if(49 < k) {
                    printHeader(svf, as);
                    if(as[6] != null)
                        getJviewname2(db2, svf, as);
                    svf.VrEndPage();
                    k = 0;
                }
                printExam(svf, as, resultset, k, map);
                s = resultset.getString("SCHREGNO");
                flag = true;
            }
            if(flag) {
                printHeader(svf, as);
                if(as[6] != null)
                    getJviewname2(db2, svf, as);
                svf.VrEndPage();
            }
            resultset.close();
            preparedstatement.close();
            db2.commit();
        }
        catch(Exception exception) {
            log.warn("printMeisai read error!", exception);
        }
        return flag;
    }

    private void printHeader(Vrw32alp svf, String as[]) {
        try {
            svf.VrsOut("NENDO", as[7]);//年度
            svf.VrsOut("DATE", as[8]);//作成日時
            svf.VrsOut("SEMESTER", as[9]);//学期
            svf.VrsOut("CLASS", as[10]);//教科
            svf.VrsOut("CHAIRNAME", as[11]);//講座
            svf.VrsOut("CHAIRTITLE", "講座");//講座タイトル
        }
        catch(Exception exception) {
            log.warn("printHeader read error!", exception);
        }
    }

    private void printExam(Vrw32alp svf, String as[], ResultSet resultset, int i, Map map) {
        try {
            svf.VrsOutn("HR_NAME", i + 1, resultset.getString("HR_NAME") + "-" + resultset.getString("ATTENDNO"));//クラス
            svf.VrsOutn("NAME", i + 1, resultset.getString("SCHREGNO") + "\u3000" + resultset.getString("NAME_SHOW"));//氏名
            if(resultset.getString("VIEWCD") != null && as[6] != null) {
                if (null != as[16]) {
                    if (!"9".equals((String)map.get(resultset.getString("VIEWCD")))) {
                        final String setStatus = (as[16].equals(resultset.getString("STATUS"))) ? "": resultset.getString("STATUS");
                        svf.VrsOutn("STATUS" + (String)map.get(resultset.getString("VIEWCD")), i + 1, setStatus);//観点状況および評定
                    } else {
                        svf.VrsOutn("STATUS" + (String)map.get(resultset.getString("VIEWCD")), i + 1, resultset.getString("STATUS"));//観点状況および評定
                    }
                } else {
                    svf.VrsOutn("STATUS" + (String)map.get(resultset.getString("VIEWCD")), i + 1, resultset.getString("STATUS"));//観点状況および評定
                }
            }
        }
        catch(Exception exception) {
            log.warn("printExam read error!", exception);
        }
    }

    private void getJviewname2(DB2UDB db2, Vrw32alp svf, String as[]) {
        try {
            log.debug("Jviewname2 start!");
            PreparedStatement preparedstatement = db2.prepareStatement(statementJviewname(as));
            ResultSet resultset = preparedstatement.executeQuery();
            log.debug("Jviewname2 end!");
            int i1 = 0;
            int j1 = 1;
            String as1[] = {"1", "2", "3", "4", "5", "6"};
            int maxlen = ("6".equals(_kantenHyouji)) ? 6 : 5;
            while(resultset.next())  {
                if(i1 == maxlen)
                    break;
                svf.VrsOut("VIEWCD1_" + String.valueOf(i1 + 1), as1[i1]);

                String viewname = StringUtils.defaultString(resultset.getString("VIEWNAME"));
                int mojisuu = viewname.length();
                for (int k = 0; k < 7; k++) {
                    int s = 10 * k;
                    int e = 10 * (k + 1);
                    if (0 < k) {
                        j1++;
                        svf.VrsOutn("VIEWCD2", i1 + j1, "");
                    } else {
                        svf.VrsOutn("VIEWCD2", i1 + j1, as1[i1]);//観点コード
                    }
                    if (e < mojisuu) {
                        svf.VrsOutn("VIEWNAME", i1 + j1, viewname.substring(s, e));
                    } else {
                        svf.VrsOutn("VIEWNAME", i1 + j1, viewname.substring(s));//観点名称
                        break;
                    }
                }

                i1++;
            }
            resultset.close();
            preparedstatement.close();
        }
        catch(Exception exception) {
            log.warn("getJviewname2 read error!", exception);
        }
    }

    private String statementMeisai(String as[]) {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("WITH SCHNO AS ( ");
            stb.append("SELECT ");
            stb.append("    t1.hr_name, t2.grade, t2.hr_class, t2.attendno, t4.schregno, t3.name_show ");
            stb.append("FROM ");
            stb.append("    schreg_regd_hdat t1, ");
            stb.append("    schreg_regd_dat t2, ");
            stb.append("    schreg_base_mst t3, ");
            stb.append("    chair_std_dat t4 ");
            stb.append("WHERE ");
            stb.append("    t1.year      = t2.year AND ");
            stb.append("    t1.semester  = t2.semester AND ");
            stb.append("    t1.grade     = t2.grade AND ");
            stb.append("    t1.hr_class  = t2.hr_class AND ");
            stb.append("    t2.year      = '" + _ctrlY + "' AND ");
            stb.append("    t2.semester  = '" + _semester2 + "' AND ");
            stb.append("    t2.schregno  = t3.schregno AND ");
            stb.append("    t4.year      = t2.year AND ");
            stb.append("    t4.chaircd   = '" + as[5] + "' AND ");
            stb.append("    t4.schregno  = t2.schregno AND ");
            stb.append("    '" + _ctrlD + "' BETWEEN t4.appdate AND t4.appenddate ");
            stb.append(" ) ");
            stb.append(",JVIEWSTAT AS ( ");
            stb.append("SELECT ");
            stb.append("    schregno, viewcd, status ");
            stb.append("FROM ");
            stb.append("    jviewstat_record_dat ");
            stb.append("WHERE ");
            stb.append("    year = '" + _ctrlY + "' AND ");
            stb.append("    semester = '" + _semester + "' AND ");
            //教育課程対応
            if ("1".equals(_useCurriculumcd)) {
                stb.append("    CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '" + as[12] + "' AND ");
            } else {
                stb.append("    SUBCLASSCD = '" + as[12] + "' AND ");
            }
            if(as[6] != null)
                stb.append("    viewcd in " + as[6] + " AND ");
            else
                stb.append("    SUBSTR(VIEWCD,1,2) = '" + as[15] + "' AND ");
            stb.append("    viewcd not like '%99' AND ");
            stb.append("    status is not null ");
            stb.append("UNION ALL ");
            stb.append("SELECT ");
            stb.append("    SCHREGNO, ");
            stb.append("    '" + as[15] + "99' AS VIEWCD, ");
            if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(_useTestCountflg)) {
                stb.append("    RTRIM(CHAR(SCORE)) AS STATUS ");
            } else {
                stb.append("    RTRIM(CHAR(VALUE)) AS STATUS ");
            }
            stb.append("FROM ");
            stb.append("    RECORD_SCORE_DAT ");
            stb.append("WHERE ");
            stb.append("    YEAR = '" + _ctrlY + "' AND ");
            stb.append("    SEMESTER = '" + _semester + "' AND ");
            stb.append("    TESTKINDCD = '99' AND ");
            stb.append("    TESTITEMCD = '00' AND ");
            if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(_useTestCountflg)) {
                if ("9".equals(_semester)) {
                    stb.append("    SCORE_DIV  = '09' AND ");
                } else {
                    stb.append("    SCORE_DIV  = '08' AND ");
                }
            } else {
                stb.append("    SCORE_DIV  = '00' AND ");
            }
            //教育課程対応
            if ("1".equals(_useCurriculumcd)) {
                stb.append("    CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '" + as[12] + "' AND ");
            } else {
                stb.append("    SUBCLASSCD = '" + as[12] + "' AND ");
            }
            if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(_useTestCountflg)) {
                stb.append("    SCORE IS NOT NULL ");
            } else {
                stb.append("    VALUE IS NOT NULL ");
            }
            stb.append(" ) ");
            stb.append("SELECT  TBL1.hr_name,TBL1.grade,TBL1.hr_class,TBL1.attendno,TBL1.schregno,TBL1.name_show, ");
            stb.append("        TBL2.viewcd, TBL2.status ");
            stb.append("FROM    SCHNO TBL1 ");
            stb.append("        LEFT JOIN JVIEWSTAT TBL2 ON TBL2.SCHREGNO = TBL1.SCHREGNO ");
            stb.append("ORDER BY TBL1.grade, TBL1.hr_class, TBL1.attendno, TBL2.viewcd ");
        }
        catch(Exception exception) {
            log.warn("statementMeisai error!", exception);
        }
        return stb.toString();
    }

    private String statementJviewname(String as[]) {
        StringBuffer stb = new StringBuffer();
        try {
                stb.append("WITH MAX_GRADE AS ( ");
                stb.append("SELECT ");
                stb.append("    max(t2.grade) as grade ");
                stb.append("FROM ");
                stb.append("    schreg_regd_hdat t1, ");
                stb.append("    schreg_regd_dat t2, ");
                stb.append("    schreg_base_mst t3, ");
                stb.append("    chair_std_dat t4 ");
                stb.append("WHERE ");
                stb.append("    t1.year      = t2.year AND ");
                stb.append("    t1.semester  = t2.semester AND ");
                stb.append("    t1.grade     = t2.grade AND ");
                stb.append("    t1.hr_class  = t2.hr_class AND ");
                stb.append("    t2.year      = '" + _ctrlY + "' AND ");
                stb.append("    t2.semester  = '" + _semester2 + "' AND ");
                stb.append("    t2.schregno  = t3.schregno AND ");
                stb.append("    t4.year      = t2.year AND ");
                stb.append("    t4.chaircd   = '" + as[5] + "' AND ");
                stb.append("    t4.schregno  = t2.schregno AND ");
                stb.append("    '" + _ctrlD + "' BETWEEN t4.appdate AND t4.appenddate ");
                stb.append(" ) ");

                stb.append("SELECT T1.VIEWCD, T2.VIEWNAME ");
                stb.append("FROM   JVIEWNAME_GRADE_YDAT T1, JVIEWNAME_GRADE_MST T2 ");
                stb.append("WHERE  T1.YEAR = '" + _ctrlY + "' AND ");
                stb.append("       T1.GRADE = T2.GRADE AND ");
                //教育課程対応
                if ("1".equals(_useCurriculumcd)) {
                    stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '" + as[12] + "' AND ");
                    stb.append("       T1.CLASSCD = T2.CLASSCD AND ");
                    stb.append("       T1.SCHOOL_KIND = T2.SCHOOL_KIND AND ");
                    stb.append("       T1.CURRICULUM_CD = T2.CURRICULUM_CD AND ");
                    stb.append("       T1.SUBCLASSCD = T2.SUBCLASSCD AND ");
                } else {
                    stb.append("       T1.SUBCLASSCD = '" + as[12] + "' AND ");
                    stb.append("       T1.SUBCLASSCD = T2.SUBCLASSCD AND ");
                }
                stb.append("       T1.VIEWCD = T2.VIEWCD AND ");
                stb.append("       SUBSTR(T1.VIEWCD,1,2) = '" + as[15] + "' ");
                stb.append("       AND T1.GRADE IN (SELECT W1.GRADE FROM MAX_GRADE W1) ");
                stb.append("ORDER BY T1.VIEWCD ");
        }
        catch(Exception exception) {
            log.warn("statementJviewname error!", exception);
        }
        return stb.toString();
    }
}
