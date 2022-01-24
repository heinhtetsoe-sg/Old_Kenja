/*
 * $Id: 31e51dded3142cb061fc8f591a355491a7b46fbe $
 *
 * 作成日: 2013/10/10
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３１２Ｂ＞  素点入力確認表（科目別）
 **/
public class KNJL312B {

    private static final Log log = LogFactory.getLog(KNJL312B.class);

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
	        response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf);
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }
    }
    
    public void printMain(final DB2UDB db2, final Vrw32alp svf) {
        
        final String form = "KNJL312B.frm";
        final int maxLine = 100;

        final List halllist = Examhall.load(db2, _param);
        
        for (int i = 0; i < halllist.size(); i++) {
            final Examhall hall = (Examhall) halllist.get(i);
            
            final List pageList = getPageList(hall._scoreList, maxLine);
            int boyCount = 0;
            int girlCount = 0;
            
            for (int pi = 0; pi < pageList.size(); pi++) {
                final List scoreList = (List) pageList.get(pi);
                
                svf.VrSetForm(form, 1);
                svf.VrsOut("TITLE", "素点入力確認表"); // タイトル
                svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度"); // 年度
                svf.VrsOut("KIND", _param._testdivAbbv1); // 入試制度
                // svf.VrsOut("HOPE_COURSE", null); // 志望所属
                svf.VrsOut("EXAMHALL_NAME", hall._examhallName); // 会場名
                svf.VrsOut("SUBJECT", _param._subclassName); // 科目
                svf.VrsOut("DATE", _param._dateStr); // 作成日
                
                for (int j = 0; j < scoreList.size(); j++) {
                    final ApplicantScore applScore = (ApplicantScore) scoreList.get(j);
                    final int line = j + 1;
                    final String k = (j < 25) ? "1" : (j < 50) ? "2" : (j < 75) ? "3" : "4";
                    final int pline = line - ((j < 25) ? 0 : (j < 50) ? 25 : (j < 75) ? 50 : 75);
                    svf.VrsOutn("NO" + k, pline, String.valueOf(line)); // NO
                    svf.VrsOutn("EXAM_NO" + k, pline, applScore._examno); // 受験番号
                    svf.VrsOutn("SCORE" + k, pline, applScore._score); // 素点
                    if ("1".equals(applScore._sex)) {
                        boyCount++;
                    } else if ("2".equals(applScore._sex)) {
                        girlCount++;
                    }
                }
                if (pi == pageList.size() - 1) {
                    svf.VrsOut("TOTAL", _param._sexName1 + boyCount + "名、" + _param._sexName2 + girlCount + "名、合計" + String.valueOf(boyCount + girlCount) + "名"); // 合計
                }
                svf.VrEndPage();
                _hasData = true;
            }
        }
    }

    private static List getPageList(final List list, final int count) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= count) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }
    
    private static class Examhall {
        final String _examhallcd;
        final String _examhallName;
        final List _scoreList;

        Examhall(
            final String examhallcd,
            final String examhallName
        ) {
            _examhallcd = examhallcd;
            _examhallName = examhallName;
            _scoreList = new ArrayList();
        }
        
        public static Examhall getExamhall(final List list, final String examhallcd) {
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final Examhall hall = (Examhall) it.next();
                if (hall._examhallcd.equals(examhallcd)) {
                    return hall;
                }
            }
            return null;
        }

        public static List load(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String examhallcd = rs.getString("EXAMHALLCD");

                    if (null == getExamhall(list, examhallcd)) {
                        final String examhallName = rs.getString("EXAMHALL_NAME");
                        final Examhall examhall = new Examhall(examhallcd, examhallName);
                        list.add(examhall);
                    }

                    final Examhall examhall = (Examhall) getExamhall(list, examhallcd);
                    final String examno = rs.getString("EXAMNO");
                    final String sex = rs.getString("SEX");
                    final String score = rs.getString("SCORE");
                    examhall._scoreList.add(new ApplicantScore(examno, sex, score));
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     HALL.EXAMHALLCD, ");
            stb.append("     HALL.EXAMHALL_NAME, ");
            stb.append("     RCPT.EXAMNO, ");
            stb.append("     BASE.SEX, ");
            stb.append("     TSCORE.SCORE ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_RECEPT_DAT RCPT ");
            stb.append("     INNER JOIN ENTEXAM_HALL_YDAT HALL ON HALL.ENTEXAMYEAR = RCPT.ENTEXAMYEAR ");
            stb.append("         AND HALL.APPLICANTDIV = RCPT.APPLICANTDIV ");
            stb.append("         AND HALL.TESTDIV = RCPT.TESTDIV ");
            stb.append("         AND HALL.EXAM_TYPE = RCPT.EXAM_TYPE ");
            stb.append("         AND HALL.EXAMHALLCD IN " + SQLUtils.whereIn(true, param._categoryName)  + " ");
            stb.append("         AND RCPT.RECEPTNO BETWEEN HALL.S_RECEPTNO AND HALL.E_RECEPTNO ");
            stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT TSCORE ON TSCORE.ENTEXAMYEAR = RCPT.ENTEXAMYEAR ");
            stb.append("         AND TSCORE.APPLICANTDIV = RCPT.APPLICANTDIV ");
            stb.append("         AND TSCORE.TESTDIV = RCPT.TESTDIV ");
            stb.append("         AND TSCORE.EXAM_TYPE = RCPT.EXAM_TYPE ");
            stb.append("         AND TSCORE.RECEPTNO = RCPT.RECEPTNO ");
            stb.append("         AND TSCORE.TESTSUBCLASSCD = '" + param._testsubclasscd + "' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = RCPT.ENTEXAMYEAR ");
            stb.append("         AND BASE.APPLICANTDIV = RCPT.APPLICANTDIV ");
            stb.append("         AND BASE.TESTDIV = RCPT.TESTDIV ");
            stb.append("         AND BASE.EXAMNO = RCPT.EXAMNO ");
            stb.append(" WHERE ");
            stb.append("     RCPT.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND RCPT.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("     AND RCPT.TESTDIV = '" + param._testdiv + "' ");
            stb.append("     AND RCPT.EXAM_TYPE = '1' ");
            if ("1".equals(param._specialReasonDiv)) {
                stb.append("     AND BASE.SPECIAL_REASON_DIV IS NOT NULL ");
            }
            stb.append(" ORDER BY ");
            stb.append("     HALL.EXAMHALLCD, ");
            stb.append("     RCPT.EXAMNO ");
            return stb.toString();
        }
    }
    
    private static class ApplicantScore {
        
        final String _examno;
        final String _sex;
        final String _score;

        ApplicantScore(
            final String examno,
            final String sex,
            final String score
        ) {
            _examno = examno;
            _sex = sex;
            _score = score;
        }
    }
    
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _date;
        final String _testsubclasscd;
        final String[] _categoryName;
        final String _specialReasonDiv;
        
        final String _subclassName;
        final String _sexName1;
        final String _sexName2;
        final String _testdivAbbv1;
        final String _dateStr;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _date = request.getParameter("CTRL_DATE").replace('/', '-');
            _dateStr = getDateStr(_date);
            _testsubclasscd = request.getParameter("TESTSUBCLASSCD");
            _specialReasonDiv = request.getParameter("SPECIAL_REASON_DIV");
            _categoryName = request.getParameterValues("category_name");
            if (null != _categoryName) {
                for (int i = 0; i < _categoryName.length; i++) {
                    _categoryName[i] = StringUtils.split(_categoryName[i], "-")[0];        
                }
            }
            _sexName1 = StringUtils.defaultString(getNameMst(db2, "NAME2", "Z002", "1"));
            _sexName2 = StringUtils.defaultString(getNameMst(db2, "NAME2", "Z002", "2"));
            _subclassName = getNameMst(db2, "NAME1", "L009", _testsubclasscd);
            _testdivAbbv1 = getNameMst(db2, "ABBV1", "L004", _testdiv);
        }
        
        private String getDateStr(final String date) {
            if (null == date) {
                return null;
            }
            final Calendar cal = Calendar.getInstance();
            final DecimalFormat df = new DecimalFormat("00");
            final String hour = df.format(cal.get(Calendar.HOUR_OF_DAY));
            final String min = df.format(cal.get(Calendar.MINUTE));
            cal.setTime(Date.valueOf(date));
            final String youbi = StringUtils.defaultString(new String[] {null, "日", "月", "火", "水", "木", "金", "土"} [cal.get(Calendar.DAY_OF_WEEK)]);
            return KNJ_EditDate.h_format_JP(date) + "（" + youbi + "） " + hour + ":" + min;
        }

        private static String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }
    }
}

// eof

