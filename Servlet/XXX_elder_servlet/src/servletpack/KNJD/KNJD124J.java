/**
 *
 *  学校教育システム 賢者 [成績管理]
 *
 *                  ＜ＫＮＪＤ１２４Ｊ＞  観点別成績・評価チェックリスト（千代田区版）
 *
 *  2005/12/27 nakamoto 作成日
 *  2006/04/07 nakamoto 再作成
 */

package servletpack.KNJD;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
//import jp.co.fit.vfreport.Vrw32;
import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
//import nao_package.db.Database;
import nao_package.svf.Vrw32alp;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJD124J {

    private static final Log log = LogFactory.getLog(KNJD124J.class);
    
    private String _classcd;
    private String _useCurriculumcd;

    public void svf_out(HttpServletRequest httpservletrequest, HttpServletResponse httpservletresponse) throws ServletException, IOException {
        
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(httpservletrequest, log);
        
        Vrw32alp vrw32alp = new Vrw32alp();
        DB2UDB db2udb = null;
        String as[] = new String[12];
        HashMap hashmap = new HashMap();
        try {
            as[0] = httpservletrequest.getParameter("CTRL_Y");// 今年度
            as[1] = httpservletrequest.getParameter("CTRL_S");// 学期 観点データ以外用
                                                              // 1:前期,2:後期
            as[2] = httpservletrequest.getParameter("CTRL_D");// 生徒を抽出する日付
                                                              // 学籍処理日または学期終了日
            as[3] = httpservletrequest.getParameter("SEMESTER");// 学期 観点データ用
                                                                // 1:前期,9:学年
            as[4] = httpservletrequest.getParameter("CLASSCD");// 教科
            as[5] = httpservletrequest.getParameter("CHAIRCD");// 講座
            _useCurriculumcd = httpservletrequest.getParameter("useCurriculumcd");
            if ("1".equals(_useCurriculumcd)) {
                _classcd = StringUtils.split(as[4], "-")[0];
            } else {
                _classcd = as[4];
            }
        } catch (Exception exception) {
            log.warn("parameter error!", exception);
        }
        PrintWriter printwriter = new PrintWriter(httpservletresponse.getOutputStream());
        httpservletresponse.setContentType("application/pdf");
        vrw32alp.VrInit();
        vrw32alp.VrSetSpoolFileStream(httpservletresponse.getOutputStream());
        try {
            db2udb = new DB2UDB(httpservletrequest.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2udb.open();
        } catch (Exception exception1) {
            log.error("DB2 open error!", exception1);
            return;
        }
        boolean flag = false;
        getHeaderData(db2udb, vrw32alp, as);
        for (int j = 0; j < as.length; j++)
            log.debug("param[" + j + "]=" + as[j]);

        if (printMain(db2udb, vrw32alp, as, hashmap))
            flag = true;
        log.debug("nonedata=" + flag);
        if (!flag) {
            vrw32alp.VrSetForm("MES001.frm", 0);
            vrw32alp.VrsOut("note", "");
            vrw32alp.VrEndPage();
        }
        vrw32alp.VrQuit();
        db2udb.commit();
        db2udb.close();
        printwriter.close();
    }

    private void getHeaderData(DB2UDB db2udb, Vrw32alp vrw32alp, String as[]) {
        vrw32alp.VrSetForm("KNJD124J.frm", 1);
        try {
            as[7] = KenjaProperties.gengou(Integer.parseInt(as[0])) + "\u5E74\u5EA6";// 平成XX年度
        } catch (Exception exception) {
            log.warn("nendo get error!", exception);
        }
        try {
            String s = "VALUES RTRIM(CHAR(DATE(SYSDATE()))),RTRIM(CHAR(HOUR(SYSDATE()))),RTRIM(CHAR(MINUTE(SYSDATE())))";
            db2udb.query(s);
            ResultSet resultset3 = db2udb.getResultSet();
            String as1[] = new String[3];
            for (int j = 0; resultset3.next(); j++)
                as1[j] = resultset3.getString(1);

            db2udb.commit();
            as[8] = KNJ_EditDate.h_format_JP(as1[0]) + as1[1] + "\u6642" + as1[2] + "\u5206" + "\u3000\u73FE\u5728";// 平成XX年XX月XX日XX時XX分　現在
        } catch (Exception exception1) {
            log.warn("ctrl_date get error!", exception1);
        }
        try {
            db2udb.query("SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z009' AND NAMECD2 = '" + as[3] + "'");
            for (ResultSet resultset = db2udb.getResultSet(); resultset.next();)
                as[9] = resultset.getString("NAME1");// 管理者コントロール 観点名称 1:前期,9:学年

            db2udb.commit();
        } catch (Exception exception2) {
            log.warn("name1 get error!", exception2);
        }
        try {
            final String sql;
            if ("1".equals(_useCurriculumcd)) {
                sql = "SELECT CLASSNAME FROM CLASS_MST WHERE CLASSCD || '-' || SCHOOL_KIND = '" + as[4] + "' ";
            } else {
                sql = "SELECT CLASSNAME FROM CLASS_MST WHERE CLASSCD = '" + as[4] + "'";
            }
            db2udb.query(sql);
            for (ResultSet resultset1 = db2udb.getResultSet(); resultset1.next();)
                as[10] = resultset1.getString("CLASSNAME");// 教科名称

            db2udb.commit();
        } catch (Exception exception3) {
            log.warn("classname get error!", exception3);
        }
        try {
            db2udb.query("SELECT CHAIRNAME FROM CHAIR_DAT WHERE YEAR = '" + as[0] + "' AND SEMESTER = '" + as[1] + "' AND CHAIRCD = '" + as[5] + "'");
            for (ResultSet resultset2 = db2udb.getResultSet(); resultset2.next();)
                as[11] = resultset2.getString("CHAIRNAME");// 講座名称

            db2udb.commit();
        } catch (Exception exception4) {
            log.warn("chairname get error!", exception4);
        }
    }

    private boolean printMain(DB2UDB db2udb, Vrw32alp vrw32alp, String as[], Map map) {
        boolean flag = false;
        try {
            getJviewname(db2udb, vrw32alp, as, map);
            if (printMeisai(db2udb, vrw32alp, as, map))
                flag = true;
        } catch (Exception exception) {
            log.warn("printMain read error!", exception);
        }
        return flag;
    }

    private void getJviewname(DB2UDB db2udb, Vrw32alp vrw32alp, String as[], Map map) {
        try {
            log.debug("Jviewname start!");
            db2udb.query(statementJviewname(as));
            ResultSet resultset = db2udb.getResultSet();
            log.debug("Jviewname end!");
            int i = 0;
            String s = "";
            for (; resultset.next(); map.put(resultset.getString("VIEWCD"), String.valueOf(++i))) {
                if (i == 5)
                    break;
                if (i == 0)
                    as[6] = "";
                as[6] = as[6] + s + resultset.getString("VIEWCD");// 観点コード 観点状況
                s = "','";
            }

            if (0 < i) {
                as[6] = "('" + as[6] + s + _classcd + "99" + "')";// 観点コード 評定
                map.put(_classcd + "99", "9");
            }
            log.debug("param[6]=" + as[6]);
            resultset.close();
            db2udb.commit();
        } catch (Exception exception) {
            log.warn("getJviewname read error!", exception);
        }
    }

    private boolean printMeisai(DB2UDB db2udb, Vrw32alp vrw32alp, String as[], Map map) {
        boolean flag = false;
        try {
            log.debug("Meisai start!");
            PreparedStatement preparedstatement = db2udb.prepareStatement(statementMeisai(as));
            ResultSet resultset = preparedstatement.executeQuery();
            log.debug("Meisai end!");
            int k = 0;
            String s = "0";
            while (resultset.next()) {
                if (!s.equals("0") && !s.equals(resultset.getString("SCHREGNO")))
                    k++;
                if (49 < k) {
                    printHeader(vrw32alp, as);
                    if (as[6] != null)
                        getJviewname2(db2udb, vrw32alp, as);
                    vrw32alp.VrEndPage();
                    k = 0;
                }
                printExam(vrw32alp, as, resultset, k, map);
                s = resultset.getString("SCHREGNO");
                flag = true;
            }
            if (flag) {
                printHeader(vrw32alp, as);
                if (as[6] != null)
                    getJviewname2(db2udb, vrw32alp, as);
                vrw32alp.VrEndPage();
            }
            resultset.close();
            preparedstatement.close();
            db2udb.commit();
        } catch (Exception exception) {
            log.warn("printMeisai read error!", exception);
        }
        return flag;
    }

    private void printHeader(Vrw32alp vrw32alp, String as[]) {
        try {
            vrw32alp.VrsOut("NENDO", as[7]);// 年度
            vrw32alp.VrsOut("DATE", as[8]);// 作成日時
            vrw32alp.VrsOut("SEMESTER", as[9]);// 学期
            vrw32alp.VrsOut("CLASS", as[10]);// 教科
            vrw32alp.VrsOut("CHAIRNAME", as[11]);// 講座
        } catch (Exception exception) {
            log.warn("printHeader read error!", exception);
        }
    }

    private void printExam(Vrw32alp vrw32alp, String as[], ResultSet resultset, int i, Map map) {
        try {
            vrw32alp.VrsOutn("HR_NAME", i + 1, resultset.getString("HR_NAME") + "-" + resultset.getString("ATTENDNO"));// クラス
            vrw32alp.VrsOutn("NAME", i + 1, resultset.getString("SCHREGNO") + "\u3000" + resultset.getString("NAME_SHOW"));// 氏名
            if (resultset.getString("VIEWCD") != null && as[6] != null)
                vrw32alp.VrsOutn("STATUS" + (String) map.get(resultset.getString("VIEWCD")), i + 1, resultset.getString("STATUS"));// 観点状況および評定
        } catch (Exception exception) {
            log.warn("printExam read error!", exception);
        }
    }

    private void getJviewname2(DB2UDB db2udb, Vrw32alp vrw32alp, String as[]) {
        try {
            log.debug("Jviewname2 start!");
            PreparedStatement preparedstatement = db2udb.prepareStatement(statementJviewname(as));
            ResultSet resultset = preparedstatement.executeQuery();
            log.debug("Jviewname2 end!");
            int i1 = 0;
            int j1 = 1;
            String as1[] = {
                    "1", "2", "3", "4", "5"
            };
            while (resultset.next()) {
                if (i1 == 5)
                    break;
                vrw32alp.VrsOut("VIEWCD1_" + String.valueOf(i1 + 1), as1[i1]);
                if (20 < resultset.getString("VIEWNAME").length()) {
                    vrw32alp.VrsOutn("VIEWCD2", i1 + j1, as1[i1]);
                    vrw32alp.VrsOutn("VIEWNAME", i1 + j1, resultset.getString("VIEWNAME").substring(0, 10));
                    j1++;
                    vrw32alp.VrsOutn("VIEWCD2", i1 + j1, "");
                    vrw32alp.VrsOutn("VIEWNAME", i1 + j1, resultset.getString("VIEWNAME").substring(10, 20));
                    j1++;
                    vrw32alp.VrsOutn("VIEWCD2", i1 + j1, "");
                    vrw32alp.VrsOutn("VIEWNAME", i1 + j1, resultset.getString("VIEWNAME").substring(20));
                } else if (10 < resultset.getString("VIEWNAME").length()) {
                    vrw32alp.VrsOutn("VIEWCD2", i1 + j1, as1[i1]);
                    vrw32alp.VrsOutn("VIEWNAME", i1 + j1, resultset.getString("VIEWNAME").substring(0, 10));
                    j1++;
                    vrw32alp.VrsOutn("VIEWCD2", i1 + j1, "");
                    vrw32alp.VrsOutn("VIEWNAME", i1 + j1, resultset.getString("VIEWNAME").substring(10));
                } else {
                    vrw32alp.VrsOutn("VIEWCD2", i1 + j1, as1[i1]);// 観点コード
                    vrw32alp.VrsOutn("VIEWNAME", i1 + j1, resultset.getString("VIEWNAME"));// 観点名称
                }
                i1++;
            }
            resultset.close();
            preparedstatement.close();
        } catch (Exception exception) {
            log.warn("getJviewname2 read error!", exception);
        }
    }

    private String statementMeisai(String as[]) {
        StringBuffer sb = new StringBuffer();
        try {
            sb.append("WITH SCHNO AS ( ");
            sb.append("SELECT ");
            sb.append("    t1.hr_name, t2.grade, t2.hr_class, t2.attendno, t4.schregno, t3.name_show ");
            sb.append("FROM ");
            sb.append("    schreg_regd_hdat t1, ");
            sb.append("    schreg_regd_dat t2, ");
            sb.append("    schreg_base_mst t3, ");
            sb.append("    chair_std_dat t4 ");
            sb.append("WHERE ");
            sb.append("    t1.year      = t2.year AND ");
            sb.append("    t1.semester  = t2.semester AND ");
            sb.append("    t1.grade     = t2.grade AND ");
            sb.append("    t1.hr_class  = t2.hr_class AND ");
            sb.append("    t2.year      = '" + as[0] + "' AND ");
            sb.append("    t2.semester  = '" + as[1] + "' AND ");
            sb.append("    t2.schregno  = t3.schregno AND ");
            sb.append("    t4.year      = t2.year AND ");
            sb.append("    t4.chaircd   = '" + as[5] + "' AND ");
            sb.append("    t4.schregno  = t2.schregno AND ");
            sb.append("    '" + as[2] + "' BETWEEN t4.appdate AND t4.appenddate ");
            sb.append(" ) ");
            sb.append(",JVIEWSTAT AS ( ");
            sb.append("SELECT ");
            sb.append("    schregno, viewcd, status ");
            sb.append("FROM ");
            sb.append("    jviewstat_dat ");
            sb.append("WHERE ");
            sb.append("    year = '" + as[0] + "' AND ");
            sb.append("    semester = '" + as[3] + "' AND ");
            if (as[6] != null)
                sb.append("    viewcd in " + as[6] + " AND ");
            else
                sb.append("    SUBSTR(VIEWCD,1,2) = '" + _classcd + "' AND ");
            sb.append("    status is not null ");
            sb.append(" ) ");
            sb.append("SELECT  TBL1.hr_name,TBL1.grade,TBL1.hr_class,TBL1.attendno,TBL1.schregno,TBL1.name_show, ");
            sb.append("        TBL2.viewcd, TBL2.status ");
            sb.append("FROM    SCHNO TBL1 ");
            sb.append("        LEFT JOIN JVIEWSTAT TBL2 ON TBL2.SCHREGNO = TBL1.SCHREGNO ");
            sb.append("ORDER BY TBL1.grade, TBL1.hr_class, TBL1.attendno, TBL2.viewcd ");
        } catch (Exception exception) {
            log.warn("statementMeisai error!", exception);
        }
        return sb.toString();
    }

    private String statementJviewname(String as[]) {
        StringBuffer sb = new StringBuffer();
        try {
            sb.append("SELECT T1.VIEWCD, T2.VIEWNAME ");
            sb.append("FROM   JVIEWNAME_YDAT T1, JVIEWNAME_MST T2 ");
            sb.append("WHERE  T1.YEAR = '" + as[0] + "' AND ");
            sb.append("       T1.VIEWCD = T2.VIEWCD AND ");
            sb.append("       SUBSTR(T1.VIEWCD,1,2) = '" + _classcd + "' ");
            sb.append("ORDER BY T1.VIEWCD ");
        } catch (Exception exception) {
            log.warn("statementJviewname error!", exception);
        }
        return sb.toString();
    }
}
