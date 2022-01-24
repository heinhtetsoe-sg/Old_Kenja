// kanji=漢字
/*
 * $Id: f6f3c8c923804b6b61e2dc28c3c5df8e3737a9da $
 *
 * 作成日: 2009/10/21 10:40:44 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJE.KNJE370.Param;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: f6f3c8c923804b6b61e2dc28c3c5df8e3737a9da $
 */
public class KNJE370_2 {

    private static final Log log = LogFactory.getLog("KNJE370_2.class");

    private boolean _hasData;

    private Param _param;
    private DB2UDB _db2;
    private Vrw32alp _svf;

    /**
     * コンストラクタ。
     * @param param
     * @param db2
     * @param svf
     */
    public KNJE370_2(
            final Param param,
            final DB2UDB db2,
            final Vrw32alp svf
    ) {
        _param = param;
        _db2 = db2;
        _svf = svf;
    }

    public boolean printMain(final DB2UDB db2) throws SQLException {
        boolean hasData = false;
        final List printShushokus = getPrintShushoku();
        _svf.VrSetForm("KNJE370_2.frm", 1);
        String befSchregNo = "";
        int fieldCnt = 1;
        for (final Iterator it = printShushokus.iterator(); it.hasNext();) {
            final Shushoku shushoku = (Shushoku) it.next();
            if ("1".equals(_param._kaipage) && !befSchregNo.equals(shushoku._schregno) && hasData) {
                _svf.VrEndPage();
                fieldCnt = 1;
            } else if (fieldCnt > 25) {
                _svf.VrEndPage();
                fieldCnt = 1;
            }
            //ヘッダ
            _svf.VrsOut("NENDO", _param.changePrintYear(db2, _param._year));
            if ("MIX".equals(_param._gouhiCd2)) {
                final String jobKind = (!"".equals(StringUtils.defaultString(_param._gouhiName2))) ? _param._gouhiName2 : _param._gouhiName;
                _svf.VrsOut("JOB_KIND", jobKind);
            } else {
                _svf.VrsOut("JOB_KIND", StringUtils.defaultString(_param._gouhiName2, _param._gouhiName));
            }
            _svf.VrsOut("DATE", _param.changePrintDate(db2, _param._ctrlDate));
            //明細
            _svf.VrsOutn("SCHREGNO", fieldCnt, shushoku._schregno);
            final String nameField = (KNJ_EditEdit.getMS932ByteLength(shushoku._hrName) > 15) ? "2": "";
            _svf.VrsOutn("HR_NAME" + nameField, fieldCnt, shushoku._hrName + shushoku.getAttendNo());
            _svf.VrsOutn("NAME_SHOW", fieldCnt, shushoku._name);
            _svf.VrsOutn(getFieldName(shushoku._statName, "COMPANY_NAME1", "COMPANY_NAME2", 15), fieldCnt, shushoku._statName);
            _svf.VrsOutn(getFieldName(shushoku._industryLName, "INDUST_KIND1", "INDUST_KIND2", 10), fieldCnt, shushoku._industryLName);
            _svf.VrsOutn("LOCATION", fieldCnt, shushoku._prefName);
            _svf.VrsOutn("APPLI_METHOD", fieldCnt, shushoku._howtoexamName);
            _svf.VrsOutn("RESULT2", fieldCnt, shushoku._decisionName);
            _svf.VrsOutn("COURSE_AHEAD", fieldCnt, shushoku._planstatName);
            hasData = true;
            befSchregNo = shushoku._schregno;
            fieldCnt++;
        }
        if (hasData) {
            _svf.VrEndPage();
        }
        return hasData;
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

    private List getPrintShushoku() throws SQLException {
        final List rtnList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String shushokuSql = getShushokuSql();
        try {
            ps = _db2.prepareStatement(shushokuSql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String attendno = rs.getString("ATTENDNO");
                final String gradHrName = rs.getString("G_HR_NAME");
                final String hrName = StringUtils.defaultString(rs.getString("HR_NAME"));
                final String name = rs.getString("NAME");
                final String senkouKind = rs.getString("SENKOU_KIND");
                final String statCd = rs.getString("STAT_CD");
                final String industryLCd = rs.getString("INDUSTRY_LCD");
                final String prefCd = rs.getString("PREF_CD");
                final String howtoexam = rs.getString("HOWTOEXAM");
                final String decision = rs.getString("DECISION");
                final String planstat = rs.getString("PLANSTAT");
                final String statName = rs.getString("STAT_NAME");
                final String industryLName = rs.getString("INDUSTRY_LNAME");
                final String prefName = rs.getString("PREF_NAME");
                final String howtoexamName = rs.getString("HOWTOEXAM_NAME");
                final String decisionName = rs.getString("DECISION_NAME");
                final String planstatName = rs.getString("PLANSTAT_NAME");
                final Shushoku shushoku = new Shushoku(
                        schregno,
                        grade,
                        hrClass,
                        attendno,
                        gradHrName,
                        hrName,
                        name,
                        senkouKind,
                        statCd,
                        industryLCd,
                        prefCd,
                        howtoexam,
                        decision,
                        planstat,
                        statName,
                        industryLName,
                        prefName,
                        howtoexamName,
                        decisionName,
                        planstatName);
                rtnList.add(shushoku);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            _db2.commit();
        }
        return rtnList;
    }

    private String getShushokuSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH KISOTSU AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SEQ, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     '' AS GRADE, ");
        stb.append("     '' AS HR_CLASS, ");
        stb.append("     '' AS ATTENDNO, ");
        stb.append("     '2' AS SORT_DIV, ");
        stb.append("     '既卒' AS G_HR_NAME, ");
        stb.append("     VALUE(FISCALYEAR(G_BASE.GRD_DATE),'') || '年度卒' || G_HDAT.HR_NAME AS HR_NAME, ");
        stb.append("     I1.NAME, ");
        stb.append("     T1.SENKOU_KIND, ");
        stb.append("     T1.STAT_CD, ");
        stb.append("     L1.COMPANY_NAME as STAT_NAME, ");
        stb.append("     L1.INDUSTRY_LCD, ");
        stb.append("     L2.INDUSTRY_LNAME, ");
        stb.append("     T1.PREF_CD, ");
        stb.append("     L4.PREF_NAME, ");
        stb.append("     T1.HOWTOEXAM, ");
        stb.append("     E002.NAME1 as HOWTOEXAM_NAME, ");
        stb.append("     T1.DECISION, ");
        stb.append("     E005.NAME1 as DECISION_NAME, ");
        stb.append("     T1.PLANSTAT, ");
        stb.append("     VALUE(E006.NAME2, E006.NAME1) as PLANSTAT_NAME ");
        stb.append(" FROM ");
        stb.append("     AFT_GRAD_COURSE_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_BASE_MST I1 ON I1.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN GRD_BASE_MST G_BASE ON T1.SCHREGNO = G_BASE.SCHREGNO ");
        stb.append("     LEFT JOIN GRD_REGD_HDAT G_HDAT ON T1.YEAR = G_HDAT.YEAR ");
        stb.append("                                   AND G_BASE.GRD_SEMESTER = G_HDAT.SEMESTER");
        stb.append("                                   AND G_BASE.GRD_GRADE    = G_HDAT.GRADE");
        stb.append("                                   AND G_BASE.GRD_HR_CLASS = G_HDAT.HR_CLASS");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT I2 ON I2.SCHREGNO = T1.SCHREGNO AND I2.YEAR = '" + _param._year + "' AND I2.SEMESTER = '" + _param._semester + "' ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT I3 ON I3.YEAR = I2.YEAR AND I3.SEMESTER = I2.SEMESTER AND I3.GRADE = I2.GRADE AND I3.HR_CLASS = I2.HR_CLASS ");
        stb.append("     INNER JOIN COMPANY_MST L1 ON L1.COMPANY_CD = T1.STAT_CD ");
        stb.append("     LEFT JOIN INDUSTRY_L_MST L2 ON L2.INDUSTRY_LCD = L1.INDUSTRY_LCD ");
        stb.append("     LEFT JOIN PREF_MST L4 ON L4.PREF_CD = T1.PREF_CD ");
        stb.append("     LEFT JOIN NAME_MST E002 ON E002.NAMECD1 = 'E002' AND E002.NAMECD2 = T1.HOWTOEXAM ");
        stb.append("     LEFT JOIN NAME_MST E005 ON E005.NAMECD1 = 'E005' AND E005.NAMECD2 = T1.DECISION ");
        stb.append("     LEFT JOIN NAME_MST E006 ON E006.NAMECD1 = 'E006' AND E006.NAMECD2 = T1.PLANSTAT ");
        if ("MIX".equals(_param._gouhiCd2)) {
            stb.append("     LEFT JOIN NAME_MST NM ");
            stb.append("           ON NM.NAMECD1    = '" + _param._gouhiCd1 + "' ");
            stb.append("          AND NM.NAMESPARE2 = '" + _param._gouhiCd3 + "' ");
        }
        stb.append(" WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SENKOU_KIND = '" + _param._senkouKind + "' ");
        stb.append("     AND I2.SCHREGNO IS NULL ");
        if (_param.isNamecdE005(_param._gouhiCd1)) {
            if ("MIX".equals(_param._gouhiCd2)) {
                stb.append("     AND T1.DECISION = NM.NAMECD2 ");
            } else {
                stb.append("     AND T1.DECISION = '" + _param._gouhiCd2 + "' ");
            }
        }
        if (_param.isNamecdE006(_param._gouhiCd1)) {
            stb.append("     AND T1.PLANSTAT = '" + _param._gouhiCd2 + "' ");
        }
        if ("2".equals(_param._dataDiv)) {
            stb.append("     AND T1.SCHREGNO IN " + _param._classSelectedIn + " ");
        }
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.SEQ, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     I2.GRADE, ");
        stb.append("     I2.HR_CLASS, ");
        stb.append("     I2.ATTENDNO, ");
        stb.append("     '1' AS SORT_DIV, ");
        stb.append("     '' AS G_HR_NAME, ");
        stb.append("     I3.HR_NAME, ");
        stb.append("     I1.NAME, ");
        stb.append("     T1.SENKOU_KIND, ");
        stb.append("     T1.STAT_CD, ");
        stb.append("     L1.COMPANY_NAME as STAT_NAME, ");
        stb.append("     L1.INDUSTRY_LCD, ");
        stb.append("     L2.INDUSTRY_LNAME, ");
        stb.append("     T1.PREF_CD, ");
        stb.append("     L4.PREF_NAME, ");
        stb.append("     T1.HOWTOEXAM, ");
        stb.append("     E002.NAME1 as HOWTOEXAM_NAME, ");
        stb.append("     T1.DECISION, ");
        stb.append("     E005.NAME1 as DECISION_NAME, ");
        stb.append("     T1.PLANSTAT, ");
        stb.append("     VALUE(E006.NAME2, E006.NAME1) as PLANSTAT_NAME ");
        stb.append(" FROM ");
        stb.append("     AFT_GRAD_COURSE_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_BASE_MST I1 ON I1.SCHREGNO = T1.SCHREGNO ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT I2 ON I2.SCHREGNO = T1.SCHREGNO AND I2.YEAR = '" + _param._year + "' AND I2.SEMESTER = '" + _param._semester + "' ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT I3 ON I3.YEAR = I2.YEAR AND I3.SEMESTER = I2.SEMESTER AND I3.GRADE = I2.GRADE AND I3.HR_CLASS = I2.HR_CLASS ");
        stb.append("     INNER JOIN COMPANY_MST L1 ON L1.COMPANY_CD = T1.STAT_CD ");
        stb.append("     LEFT JOIN INDUSTRY_L_MST L2 ON L2.INDUSTRY_LCD = L1.INDUSTRY_LCD ");
        stb.append("     LEFT JOIN PREF_MST L4 ON L4.PREF_CD = T1.PREF_CD ");
        stb.append("     LEFT JOIN NAME_MST E002 ON E002.NAMECD1 = 'E002' AND E002.NAMECD2 = T1.HOWTOEXAM ");
        stb.append("     LEFT JOIN NAME_MST E005 ON E005.NAMECD1 = 'E005' AND E005.NAMECD2 = T1.DECISION ");
        stb.append("     LEFT JOIN NAME_MST E006 ON E006.NAMECD1 = 'E006' AND E006.NAMECD2 = T1.PLANSTAT ");
        if ("MIX".equals(_param._gouhiCd2)) {
            stb.append("     LEFT JOIN NAME_MST NM ");
            stb.append("           ON NM.NAMECD1    = '" + _param._gouhiCd1 + "' ");
            stb.append("          AND NM.NAMESPARE2 = '" + _param._gouhiCd3 + "' ");
        }
        stb.append(" WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SENKOU_KIND = '" + _param._senkouKind + "' ");
        if (_param.isNamecdE005(_param._gouhiCd1)) {
            if ("MIX".equals(_param._gouhiCd2)) {
                stb.append("     AND T1.DECISION = NM.NAMECD2 ");
            } else {
                stb.append("     AND T1.DECISION = '" + _param._gouhiCd2 + "' ");
            }
        }
        if (_param.isNamecdE006(_param._gouhiCd1)) {
            stb.append("     AND T1.PLANSTAT = '" + _param._gouhiCd2 + "' ");
        }
        if ("1".equals(_param._dataDiv)) {
            stb.append("     AND I2.GRADE || I2.HR_CLASS IN " + _param._classSelectedIn + " ");
        } else {
            stb.append("     AND I2.SCHREGNO IN " + _param._classSelectedIn + " ");
        }
        if (_param._isKisotsu || "2".equals(_param._dataDiv)) {
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     KISOTSU ");
        }
        stb.append(" ORDER BY ");
        stb.append("     SORT_DIV, ");
        stb.append("     GRADE, ");
        stb.append("     HR_CLASS, ");
        stb.append("     ATTENDNO, ");
        stb.append("     SEQ ");

        return stb.toString();
    }

    private class Shushoku {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _attendno;
        final String _gradHrName;
        final String _hrName;
        final String _name;
        final String _senkouKind;
        final String _statCd;
        final String _industryLCd;
        final String _prefCd;
        final String _howtoexam;
        final String _decision;
        final String _planstat;
        final String _statName;
        final String _industryLName;
        final String _prefName;
        final String _howtoexamName;
        final String _decisionName;
        final String _planstatName;

        Shushoku(final String schregno,
                final String grade,
                final String hrClass,
                final String attendno,
                final String gradHrName,
                final String hrName,
                final String name,
                final String senkouKind,
                final String statCd,
                final String industryLCd,
                final String prefCd,
                final String howtoexam,
                final String decision,
                final String planstat,
                final String statName,
                final String industryLName,
                final String prefName,
                final String howtoexamName,
                final String decisionName,
                final String planstatName
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _attendno = attendno;
            _gradHrName = gradHrName;
            _hrName = hrName;
            _name = name;
            _senkouKind = senkouKind;
            _statCd = statCd;
            _industryLCd = industryLCd;
            _prefCd = prefCd;
            _howtoexam = howtoexam;
            _decision = decision;
            _planstat = planstat;
            _statName = statName;
            _industryLName = industryLName;
            _prefName = prefName;
            _howtoexamName = howtoexamName;
            _decisionName = decisionName;
            _planstatName = planstatName;
        }

        /**
         * {@inheritDoc}
         */
        public String getAttendNo() {
            if ("既卒".equals(_gradHrName)) {
                return "";
            }
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
}

// eof
