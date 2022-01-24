/*
 * $Id: a9538100684184b1ca047679addb40e97329372a $
 *
 * 作成日: 2016/12/01
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJE370J {

    private static final Log log = LogFactory.getLog(KNJE370J.class);

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

            printMain(svf, db2);
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

    public void printMain(final Vrw32alp svf, final DB2UDB db2) throws SQLException {
        final List printSingakus = getPrintSingaku(db2);
        svf.VrSetForm("KNJE370J.frm", 1);
        String befHrClass = "";
        int fieldCnt = 1;
        for (final Iterator it = printSingakus.iterator(); it.hasNext();) {
            final Singaku singaku = (Singaku) it.next();
            if ("1".equals(_param._hrKaipage) && !befHrClass.equals(singaku._grade + singaku._hrClass) && _hasData) {
                svf.VrEndPage();
                fieldCnt = 1;
            } else if (fieldCnt > 25) {
                svf.VrEndPage();
                fieldCnt = 1;
            }
            //ヘッダ
            svf.VrsOut("TITLE", nao_package.KenjaProperties.gengou(Integer.parseInt(_param._ctrlYear)) + "年度　進路状況一覧");
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._ctrlDate));
            //明細
            svf.VrsOutn("SCHREGNO", fieldCnt, singaku._schregno);
            svf.VrsOutn("HR_NAME", fieldCnt, singaku._hrName + singaku.getAttendNo());
            svf.VrsOutn("NAME_SHOW", fieldCnt, singaku._name);
            svf.VrsOutn("PUBPRIV_KIND", fieldCnt, singaku._gakkouRitsu);
            svf.VrsOutn("LOCATION", fieldCnt, singaku._prefName);
            svf.VrsOutn(getFieldName(singaku._statName, "SCHOOL_NAME1", "SCHOOL_NAME2", 15), fieldCnt, singaku._statName);
            svf.VrsOutn("MAJORCD", fieldCnt, singaku._buName);
            svf.VrsOutn("EXAM_METHOD", fieldCnt, singaku._howtoexamName);
            svf.VrsOutn("RESULT2", fieldCnt, singaku._decisionName);
            svf.VrsOutn("EXAM_NO", fieldCnt, singaku._examNo);
            svf.VrsOutn("COURSE_AHEAD", fieldCnt, singaku._planstatName);
            _hasData = true;
            befHrClass = singaku._grade + singaku._hrClass;
            fieldCnt++;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
        return;
    }

    /**
     * 文字数によるフォームフィールド名を取得
     * @param str：データ
     * @param field1：フィールド１（小さい方）
     * @param field2：フィールド２（大きい方）
     * @param len：フィールド１の文字数
     */
    private String getFieldName(final String str, final String field1, final String field2, final int len) {
        if (null == str) return field1;
        return len < str.length() ? field2 : field1;
    }

    private List getPrintSingaku(final DB2UDB db2) throws SQLException {
        final List rtnList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String singakuSql = getSingakuSql();
        log.debug(singakuSql);
        try {
            ps = db2.prepareStatement(singakuSql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String attendno = rs.getString("ATTENDNO");
                final String hrName = rs.getString("HR_NAME");
                final String name = rs.getString("NAME");
                final String senkouKind = rs.getString("SENKOU_KIND");
                final String statCd = rs.getString("STAT_CD");
                final String gakkouRitsu = rs.getString("GAKKOU_RITSU");
                final String prefCd = rs.getString("PREF_CD");
                final String howtoexam = rs.getString("HOWTOEXAM");
                final String decision = rs.getString("DECISION");
                final String planstat = rs.getString("PLANSTAT");
                final String statName = rs.getString("STAT_NAME");
                final String buName = rs.getString("BUNAME");
                final String prefName = rs.getString("PREF_NAME");
                final String howtoexamName = rs.getString("HOWTOEXAM_NAME");
                final String decisionName = rs.getString("DECISION_NAME");
                final String planstatName = rs.getString("PLANSTAT_NAME");
                final String examNo = rs.getString("EXAMNO");
                final Singaku singaku = new Singaku(
                        schregno,
                        grade,
                        hrClass,
                        attendno,
                        hrName,
                        name,
                        senkouKind,
                        statCd,
                        gakkouRitsu,
                        prefCd,
                        howtoexam,
                        decision,
                        planstat,
                        statName,
                        buName,
                        prefName,
                        howtoexamName,
                        decisionName,
                        planstatName,
                        examNo);
                rtnList.add(singaku);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnList;
    }

    private String getSingakuSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     AFT.SEQ, ");
        stb.append("     AFT.SCHREGNO, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     REGD_H.HR_NAME, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.NAME_KANA, ");
        stb.append("     Z002.ABBV1 AS SEX, ");
        stb.append("     AFT.SENKOU_KIND, ");
        stb.append("     AFT.STAT_CD, ");
        stb.append("     FINSCHOOL.FINSCHOOL_NAME AS STAT_NAME, ");
        stb.append("     L015.NAME1 as GAKKOU_RITSU, ");
        stb.append("     AFT.BUNAME, ");
        stb.append("     AFT.PREF_CD, ");
        stb.append("     PREF.PREF_NAME, ");
        stb.append("     AFT.HOWTOEXAM, ");
        stb.append("     E002.NAME1 as HOWTOEXAM_NAME, ");
        stb.append("     AFT.DECISION, ");
        stb.append("     E005.NAME1 as DECISION_NAME, ");
        stb.append("     AFT.PLANSTAT, ");
        stb.append("     AFT_GRAD_D.REMARK9 AS EXAMNO, ");
        stb.append("     E006.NAME1 as PLANSTAT_NAME ");
        stb.append(" FROM ");
        stb.append("     AFT_GRAD_COURSE_DAT AFT ");
        stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = AFT.SCHREGNO ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = AFT.SCHREGNO ");
        stb.append("                                  AND REGD.YEAR     = '" + _param._ctrlYear + "' ");
        stb.append("                                  AND REGD.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT REGD_H ON REGD_H.YEAR     = REGD.YEAR ");
        stb.append("                                   AND REGD_H.SEMESTER = REGD.SEMESTER ");
        stb.append("                                   AND REGD_H.GRADE    = REGD.GRADE ");
        stb.append("                                   AND REGD_H.HR_CLASS = REGD.HR_CLASS ");
        stb.append("     INNER JOIN FINSCHOOL_MST FINSCHOOL ON AFT.STAT_CD = FINSCHOOL.FINSCHOOLCD ");
        stb.append("     LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT AFT_GRAD_D ON AFT.YEAR = AFT_GRAD_D.YEAR ");
        stb.append("                                        AND AFT.SEQ = AFT_GRAD_D.SEQ ");
        stb.append("                                        AND AFT_GRAD_D.DETAIL_SEQ = 1 ");
        stb.append("     LEFT JOIN PREF_MST PREF ON AFT.PREF_CD = PREF.PREF_CD ");
        stb.append("     LEFT JOIN NAME_MST E002 ON E002.NAMECD1 = 'E002' AND E002.NAMECD2 = AFT.HOWTOEXAM ");
        stb.append("     LEFT JOIN NAME_MST E005 ON E005.NAMECD1 = 'E005' AND E005.NAMECD2 = AFT.DECISION ");
        stb.append("     LEFT JOIN NAME_MST E006 ON E006.NAMECD1 = 'E006' AND E006.NAMECD2 = AFT.PLANSTAT ");
        stb.append("     LEFT JOIN NAME_MST L015 ON L015.NAMECD1 = 'L015' AND L015.NAMECD2 = FINSCHOOL.FINSCHOOL_DIV ");
        stb.append("     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' AND Z002.NAMECD2 = BASE.SEX ");
        stb.append(" WHERE ");
        stb.append("         AFT.YEAR         = '" + _param._ctrlYear + "' ");
        stb.append("     AND AFT.SENKOU_KIND  = '0' ");
        stb.append("     AND REGD.GRADE || REGD.HR_CLASS IN " + _param._classSelectedIn + " ");
        if (null != _param._notPrintCourseMajor && !"".equals(_param._notPrintCourseMajor)) {
            stb.append("     AND AFT.STAT_CD != '" + _param._notPrintCourseMajor + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     AFT.SEQ ");

        return stb.toString();
    }

    private class Singaku {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _attendno;
        final String _hrName;
        final String _name;
        final String _senkouKind;
        final String _statCd;
        final String _gakkouRitsu;
        final String _prefCd;
        final String _howtoexam;
        final String _decision;
        final String _planstat;
        final String _statName;
        final String _buName;
        final String _prefName;
        final String _howtoexamName;
        final String _decisionName;
        final String _planstatName;
        final String _examNo;

        Singaku(final String schregno,
                final String grade,
                final String hrClass,
                final String attendno,
                final String hrName,
                final String name,
                final String senkouKind,
                final String statCd,
                final String gakkouRitsu,
                final String prefCd,
                final String howtoexam,
                final String decision,
                final String planstat,
                final String statName,
                final String buName,
                final String prefName,
                final String howtoexamName,
                final String decisionName,
                final String planstatName,
                final String examNo
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _attendno = attendno;
            _hrName = hrName;
            _name = name;
            _senkouKind = senkouKind;
            _statCd = statCd;
            _gakkouRitsu = gakkouRitsu;
            _prefCd = prefCd;
            _howtoexam = howtoexam;
            _decision = decision;
            _planstat = planstat;
            _statName = statName;
            _buName = buName;
            _prefName = prefName;
            _howtoexamName = howtoexamName;
            _decisionName = decisionName;
            _planstatName = planstatName;
            _examNo = examNo;
        }

        /**
         * {@inheritDoc}
         */
        public String getAttendNo() {
            if (null == _attendno) return "  番";
            return String.valueOf(Integer.parseInt(_attendno)) + "番";
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return _schregno + " = " + _name;
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
    private class Param {
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _classSelectedIn;

        final String _notPrintCourseMajor;   //課程学科除く
        final String _hrKaipage;  //クラス毎に改ページ

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");

            final String[] classSelected = request.getParameterValues("CLASS_SELECTED");
            _classSelectedIn = getClassSelectedIn(classSelected);

            _notPrintCourseMajor = request.getParameter("NOT_PRINT");
            _hrKaipage = request.getParameter("HR_KAIPAGE");
        }

        private String getClassSelectedIn(final String[] classSelected) {
            StringBuffer stb = new StringBuffer();
            stb.append("(");
            for (int i = 0; i < classSelected.length; i++) {
                if (0 < i) stb.append(",");
                stb.append("'" + classSelected[i] + "'");
            }
            stb.append(")");
            return stb.toString();
        }

    }
}

// eof

