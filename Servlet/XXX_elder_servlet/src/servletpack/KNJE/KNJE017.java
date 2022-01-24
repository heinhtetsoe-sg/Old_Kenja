/*
 * $Id: 9992076ff421fe3645809e4b3f1b143bd1944dc2 $
 *
 * 作成日: 2019/02/08
 * 作成者: tawada
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
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJE017 {

    private static final Log log = LogFactory.getLog(KNJE017.class);

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        String gradeHrClass = ""; //年組
        int grpCnt = 1;

        final List printList = getList(db2);
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintData printData = (PrintData) iterator.next();

            if (!gradeHrClass.equals(printData._gradeHrClass)) {
                svf.VrSetForm("KNJE017.frm", 4);
                setTitle(db2, svf, printData); // タイトル・ヘッダー情報
            }

            String [] remark;
            remark = KNJ_EditEdit.get_token(printData._remark, _param._remarkMoji, _param._remarkGyou);

            //No.
            svf.VrsOut("NO" , printData._attendNo);

            //氏名
            final String nameIdx = 30 < KNJ_EditEdit.getMS932ByteLength(printData._name) ? "3": 20 < KNJ_EditEdit.getMS932ByteLength(printData._name) ? "2": "1";
            svf.VrsOut("NAME" + nameIdx, printData._name);

            //まなびの記録
            if(null != remark) {
                for (int i = 0; i < remark.length; i++) {
                    if(null == remark[i]) continue;

                    svf.VrsOut("GRPCD1" , String.valueOf(grpCnt)); //NO
                    svf.VrsOut("GRPCD2" , String.valueOf(grpCnt)); //氏名
                    svf.VrsOut("GRPCD3" , String.valueOf(grpCnt)); //備考

                    svf.VrsOut("CONTENT1", remark[i]);
                    svf.VrEndRecord();
                }
            } else {
                svf.VrsOut("GRPCD1" , String.valueOf(grpCnt)); //NO
                svf.VrsOut("GRPCD2" , String.valueOf(grpCnt)); //氏名
                svf.VrsOut("GRPCD3" , String.valueOf(grpCnt)); //備考
            }

            grpCnt++;
            gradeHrClass = printData._gradeHrClass;
            svf.VrEndRecord();
            _hasData = true;
        }
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf, final PrintData printData) {
        //タイトル
        final String gengou = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._ctrlYear));
        final String setRemarkName = (null == _param._remarkTitle || "".equals(_param._remarkTitle)) ? "備考": _param._remarkTitle;
        svf.VrsOut("TITLE", gengou +"年度 " + setRemarkName + " 一覧表");

        //押印名
        int nameIdx = 1;
        for (Iterator it = _param._imprintNameList.iterator(); it.hasNext();) {
            final String imprintName = (String) it.next();
            svf.VrsOut("JOB_NAME" + String.valueOf(nameIdx), imprintName);
            nameIdx++;
        }

        //担任
        svf.VrsOut("JOB_NAME6", "担任");
        String teachername = printData._staffname != null ? printData._staffname : "";
        svf.VrsOut("TEACHER_NAME", teachername);

        //年組
        svf.VrsOut("HR_NAME", printData._hr_Name);

        //備考
        svf.VrsOut("CONTENT1_NAME", setRemarkName);
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String gradeHrClass   = rs.getString("GRADE_HR_CLASS");
                final String hrName         = rs.getString("HR_NAME");
                final String attendno       = rs.getString("ATTENDNO");
                final String name           = rs.getString("NAME");
                final String remark         = rs.getString("REMARK");
                final String staffname      = rs.getString("STAFFNAME");

                final PrintData printData = new PrintData(gradeHrClass, hrName, attendno, name, remark, staffname);
                retList.add(printData);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     HDAT.GRADE || HDAT.HR_CLASS AS GRADE_HR_CLASS, ");
        stb.append("     HDAT.HR_NAME, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     BASE.NAME_SHOW AS NAME, ");
        stb.append("     value(LEAR.REMARK, '') AS REMARK, ");
        stb.append("     STAF.STAFFNAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append("     LEFT JOIN HEXAM_ENTREMARK_LEARNING_DAT LEAR ON LEAR.YEAR     = REGD.YEAR ");
        stb.append("                                                AND LEAR.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR     = REGD.YEAR ");
        stb.append("                                     AND HDAT.SEMESTER = REGD.SEMESTER ");
        stb.append("                                     AND HDAT.GRADE    = REGD.GRADE ");
        stb.append("                                     AND HDAT.HR_CLASS = REGD.HR_CLASS ");
        stb.append("     LEFT JOIN STAFF_MST STAF ON STAF.STAFFCD = HDAT.TR_CD1 ");
        stb.append(" WHERE ");
        stb.append("         REGD.YEAR     = '" + _param._ctrlYear + "' ");
        stb.append("     AND REGD.SEMESTER = '" + _param._ctrlSemester + "' ");
        if ("1".equals(_param._disp)) {
            stb.append("     AND REGD.GRADE || REGD.HR_CLASS IN (" + _param._sqlInstate + ") ");
        } else {
            stb.append("     AND REGD.SCHREGNO IN (" + _param._sqlInstate + ")             ");
        }
        stb.append(" ORDER BY ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO ");

        return stb.toString();
    }

    private class PrintData {
        final String _gradeHrClass;
        final String _hr_Name;
        final String _attendNo;
        final String _name;
        final String _remark;
        final String _staffname;
        public PrintData(
                final String gradeHrClass,
                final String hr_Name,
                final String attendno,
                final String name,
                final String remark,
                final String staffname
        ) {
            _gradeHrClass   = gradeHrClass;
            _hr_Name        = hr_Name;
            _attendNo       = attendno;
            _name           = name;
            _remark         = remark;
            _staffname      = staffname;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 70963 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _disp;
        private final String _gradeHrClass;
        private final String[] _categorySelected;
        private final String _sqlInstate;
        private final int _remarkMoji; //1行あたりの文字数(バイト)
        private final int _remarkGyou; //リマーク最大行数
        private final List _imprintNameList;
        String _remarkTitle;
        final String _useSchool_KindField;
        final String SCHOOLCD;
        final String SCHOOLKIND;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear           = request.getParameter("CTRL_YEAR");
            _ctrlSemester       = request.getParameter("CTRL_SEMESTER");
            _ctrlDate           = request.getParameter("CTRL_DATE");
            _disp               = request.getParameter("DISP");
            _gradeHrClass       = request.getParameter("GRADE_HR_CLASS");
            _categorySelected   = request.getParameterValues("CATEGORY_SELECTED");
            _remarkMoji         = Integer.parseInt(request.getParameter("REMARK_MOJI")) * 2;
            _remarkGyou         = Integer.parseInt(request.getParameter("REMARK_GYOU"));
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            SCHOOLCD            = request.getParameter("SCHOOLCD");
            SCHOOLKIND          = request.getParameter("SCHOOLKIND");
            _imprintNameList    = getPrgStampDat(db2);
            if (_imprintNameList.isEmpty()) {
            	_imprintNameList.addAll(getNmaeMst(db2, _ctrlYear, "D055"));
            }
            try {
                _remarkTitle    = new String(request.getParameter("HEXAM_ENTREMARK_LEARNING_DAT__REMARK").getBytes("ISO8859-1"));
            } catch (Exception e) {
                log.error("exception!", e);
            }

            String setInstate = "";
            String sep = "";
            for (int i = 0; i < _categorySelected.length; i++) {
                final String selectVal = _categorySelected[i];
                final String[] setVal = StringUtils.split(selectVal, "-");
                setInstate += sep + "'" + setVal[0] + "'";
                sep = ",";
            }
            _sqlInstate = setInstate;

        }

        private List getNmaeMst(final DB2UDB db2, final String year, final String namecd1) {
            final List retList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + year + "' AND NAMECD1 = '" + namecd1 + "' ORDER BY NAMECD2 ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    retList.add(rs.getString("NAME1"));
                }
            } catch (SQLException e) {
                log.error("getD055 exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            return retList;
        }
        
        private List getPrgStampDat(final DB2UDB db2) throws SQLException {

            final List list = new ArrayList();

            if (KnjDbUtils.setTableColumnCheck(db2, "PRG_STAMP_DAT", null)) {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT  ");
                stb.append("    T1.SEQ ");
                stb.append("  , T1.TITLE ");
                stb.append(" FROM PRG_STAMP_DAT T1 ");
                stb.append(" WHERE ");
                stb.append("   T1.YEAR = '" + _ctrlYear + "' ");
                stb.append("   AND T1.SEMESTER = '9' ");
                if ("1".equals(_useSchool_KindField)) {
                    stb.append("   AND T1.SCHOOLCD = '" + SCHOOLCD + "' ");
                    stb.append("   AND T1.SCHOOL_KIND = '" + SCHOOLKIND + "' ");
                }
                stb.append("   AND T1.PROGRAMID = '" + "KNJE017" + "' ");
                
                for (final Map<String, String> row : KnjDbUtils.query(db2, stb.toString())) {
                	if (NumberUtils.isDigits(KnjDbUtils.getString(row, "SEQ"))) {
                		final int idx = Integer.parseInt(KnjDbUtils.getString(row, "SEQ")) - 1;
                		for (int i = list.size(); i <= idx; i++) {
                			list.add(null);
                		}
                		list.set(idx, KnjDbUtils.getString(row, "TITLE"));
                	}
                }
            }
            return list;
        }
    }
}

// eof
