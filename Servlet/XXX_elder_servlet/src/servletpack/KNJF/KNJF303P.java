/*
 * $Id: 75e48b13b69b6514ab2005ffd7e52017ab3760b8 $
 *
 * 作成日: 2013/05/07
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF;

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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 * KNJF306 出席停止報告書
 * @version $Id: 75e48b13b69b6514ab2005ffd7e52017ab3760b8 $
 */
public class KNJF303P {

    private static final Log log = LogFactory.getLog(KNJF303P.class);

    private static final String PRGID_KNJF303 = "KNJF303";

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

            printMain2(db2, svf);
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

    private void printMain2(final DB2UDB db2, final Vrw32alp svf) {
		final List list = getMedexamDiseaseAddition3List(db2);
		if (list.isEmpty()) {
		    return;
		}
        if (PRGID_KNJF303.equals(_param._prgid)) {
            if (!"1".equals(_param._totalDiv)) {
                return;
            }
        }
        final MedexamDiseaseAddition3 first = (MedexamDiseaseAddition3) list.get(0);

        for (final Iterator it = getPageList(list, 10).iterator(); it.hasNext();) {
            final List pagelist = (List) it.next(); ;

            svf.VrSetForm("KNJF303P.frm", 1);
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate)); // 日付
            svf.VrsOut("CITY_NAME", _param._prefName);
            svf.VrsOut("SCHOOL_NAME1", _param._knjSchoolMst._schoolName1); // 学校名
            svf.VrsOut("PRESIDENT_NAME", _param._principalName); // 校長名
            svf.VrsOut("SCHOOL_NAME2", _param._knjSchoolMst._schoolName1); // 学校名

            for (int i = 0; i < pagelist.size(); i++) {
                final MedexamDiseaseAddition3 mda3 = (MedexamDiseaseAddition3) pagelist.get(i);
                final int line = i + 1;
                svf.VrsOutn("NO", line, String.valueOf(line)); // 番号
                svf.VrsOutn("HR_NAME", line, mda3._hrName); // 年組名称
                svf.VrsOutn("REASON", line, mda3._diseasecdName); // 理由
                svf.VrsOutn("PERIOD", line, formatDateMD(mda3._suspendSDate) + " 〜 " + formatDateMD(mda3._suspendEDate)); // 期間
                svf.VrsOutn("DIRECT_DAY", line, KNJ_EditDate.h_format_JP(mda3._suspendDirectDate)); // 指示をした年月日
            }

            final List remarkList = getRemarkLine(first);
            for (int i = 0; i < remarkList.size(); i++) {
                svf.VrsOut("OTHER" + (i + 1), (String) remarkList.get(i));
            }
            svf.VrEndPage();
            _hasData = true;
        }
    }

    private String formatDateMD(final String date) {
        if (null == date) {
            return "　　　　";
        }
        return KNJ_EditDate.h_format_JP_MD(date);
    }

    private List getRemarkLine(final MedexamDiseaseAddition3 first) {
        final List rtn = new ArrayList();
        final String[] remarks = new String[] {first._remark1, first._remark2, first._remark3};
        for (int i = 0; i < remarks.length; i++) {
            final String[] token = KNJ_EditEdit.get_token(remarks[i], 80, 2);
            if (null == token) {
                continue;
            }
            for (int j = 0; j < token.length; j++) {
                if (null != token[j]) {
                    rtn.add(token[j]);
                }
            }
        }
        return rtn;
    }

    private List getPageList(final List list, final int count) {
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

    private static class MedexamDiseaseAddition3 {
        final String _suspendDirectDate;
        final String _diseasecd;
        final String _diseasecdName;
        final String _diseasecdRemark;
        final String _suspendSDate;
        final String _suspendEDate;
        final String _remark1;
        final String _remark2;
        final String _remark3;
        final String _grade;
        final String _hrClass;
        final String _count;
        final String _hrName;

        MedexamDiseaseAddition3(
                final String suspendDirectDate,
                final String diseasecd,
                final String diseasecdName,
                final String diseasecdRemark,
                final String suspendSDate,
                final String suspendEDate,
                final String remark1,
                final String remark2,
                final String remark3,
                final String grade,
                final String hrClass,
                final String count,
                final String hrName
        ) {
            _suspendDirectDate = suspendDirectDate;
            _diseasecd = diseasecd;
            _diseasecdName = diseasecdName;
            _diseasecdRemark = diseasecdRemark;
            _suspendSDate = suspendSDate;
            _suspendEDate = suspendEDate;
            _remark1 = remark1;
            _remark2 = remark2;
            _remark3 = remark3;
            _grade = grade;
            _hrClass = hrClass;
            _count = count;
            _hrName = hrName;
        }
    }

    public List getMedexamDiseaseAddition3List(final DB2UDB db2) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = sql();
            log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final MedexamDiseaseAddition3 mda3 = new MedexamDiseaseAddition3(
                        rs.getString("SUSPEND_DIRECT_DATE"),
                        rs.getString("DISEASECD"),
                        rs.getString("DISEASECD_NAME"),
                        rs.getString("DISEASECD_REMARK"),
                        rs.getString("SUSPEND_S_DATE"),
                        rs.getString("SUSPEND_E_DATE"),
                        rs.getString("REMARK1"),
                        rs.getString("REMARK2"),
                        rs.getString("REMARK3"),
                        rs.getString("GRADE"),
                        rs.getString("HR_CLASS"),
                        rs.getString("COUNT"),
                        rs.getString("HR_NAME"));
                list.add(mda3);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }

    public String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T2.SUSPEND_DIRECT_DATE, ");
        stb.append("     T2.DISEASECD, ");
        stb.append("     T5.NAME1 AS DISEASECD_NAME, ");
        stb.append("     T2.DISEASECD_REMARK, ");
        stb.append("     T2.SUSPEND_S_DATE, ");
        stb.append("     T2.SUSPEND_E_DATE, ");
        stb.append("     T2.REMARK1, ");
        stb.append("     T2.REMARK2, ");
        stb.append("     T2.REMARK3, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.COUNT, ");
        stb.append("     T4.HR_NAME ");
        stb.append(" FROM ");
        stb.append("     MEDEXAM_DISEASE_ADDITION3_DAT T2 ");
        stb.append("     LEFT JOIN MEDEXAM_DISEASE_ADDITION3_COUNT_DAT T1 ON T2.EDBOARD_SCHOOLCD = T1.EDBOARD_SCHOOLCD ");
        stb.append("         AND T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.SUSPEND_DIRECT_DATE = T1.SUSPEND_DIRECT_DATE ");
        stb.append("         AND T2.TOTAL_DIV = T1.TOTAL_DIV ");
        stb.append("         AND T2.DISEASECD = T1.DISEASECD ");
        stb.append("     LEFT JOIN SEMESTER_MST T3 ON T3.SEMESTER <> '9' ");
        stb.append("         AND T2.SUSPEND_DIRECT_DATE BETWEEN T3.SDATE AND T3.EDATE ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT T4 ON T4.YEAR = T3.YEAR ");
        stb.append("         AND T4.SEMESTER = T3.SEMESTER ");
        stb.append("         AND T4.GRADE = T1.GRADE ");
        stb.append("         AND T4.HR_CLASS = T1.HR_CLASS ");
        if ("1".equals(_param._useSchool_KindField)) {
            stb.append("     INNER JOIN SCHREG_REGD_GDAT GDAT ON T4.YEAR = GDAT.YEAR ");
            stb.append("         AND T4.GRADE = GDAT.GRADE ");
            stb.append("         AND GDAT.SCHOOL_KIND = '" + _param._schoolKind + "' ");
        }
        stb.append("     LEFT JOIN NAME_MST T5 ON T5.NAMECD1 = 'C900' ");
        stb.append("         AND T5.NAMECD2 = T2.DISEASECD ");
        stb.append(" WHERE ");
        stb.append("     T2.SUSPEND_DIRECT_DATE BETWEEN '" + _param._sdate + "' AND '" + _param._edate + "' ");
        stb.append("     AND T2.TOTAL_DIV = '1' ");
        if (PRGID_KNJF303.equals(_param._prgid)) {
            stb.append("     AND T2.DISEASECD = '" + _param._diseasecd + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T2.DISEASECD, ");
        stb.append("     T2.SUSPEND_S_DATE, ");
        stb.append("     T2.SUSPEND_E_DATE, ");
        stb.append("     T2.SUSPEND_DIRECT_DATE ");
        return stb.toString();
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
        final String _year;
        final String _loginDate;
        final String _sdate;
        final String _edate;
        final String _totalDiv;
        final String _diseasecd;
        final String _prgid;
        final KNJSchoolMst _knjSchoolMst;
        final String _prefName;
        final String _principalName;
        final String _schoolKind;
        final String _useSchool_KindField;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _prgid = request.getParameter("PRGID");
            if ("KNJF303".equals(_prgid)) {
                final String[] dataDiv = StringUtils.split(request.getParameter("DATA_DIV"), ":");
                _sdate = dataDiv[0];
                _edate = dataDiv[0];
                _totalDiv = dataDiv[1];
                _diseasecd = dataDiv[2];
                _loginDate = request.getParameter("CTRL_DATE");
            } else {
                _sdate = request.getParameter("SDATE").replace('/', '-');
                _edate = request.getParameter("EDATE").replace('/', '-');
                _totalDiv = null;
                _diseasecd = null;
                _loginDate = request.getParameter("LOGIN_DATE");
            }
            _schoolKind = request.getParameter("SCHOOLKIND");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _knjSchoolMst = new KNJSchoolMst(db2, _year);
            _prefName = getPrefName(db2, _knjSchoolMst._prefCd);
            _principalName = getPrincipalName(db2, _year);
        }

        String getPrefName(final DB2UDB db2, final String prefCd) {
            String prefName = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT PREF_NAME FROM PREF_MST WHERE PREF_CD = '" + prefCd + "'");
                rs = ps.executeQuery();
                while (rs.next()) {
                    prefName = rs.getString("PREF_NAME");
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return prefName;
        }


        String getPrincipalName(final DB2UDB db2, final String year) {
            String principalName = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = "";
                sql += " SELECT T1.STAFFNAME ";
                sql += " FROM STAFF_MST T1 ";
                sql += " INNER JOIN STAFF_YDAT T2 ON T2.STAFFCD = T1.STAFFCD ";
                sql += " WHERE T1.JOBCD = '0001' AND T2.YEAR = '" + year + "'";
                sql += " ORDER BY T1.STAFFCD ";

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    principalName = rs.getString("STAFFNAME");
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return principalName;
        }
    }
}

// eof

