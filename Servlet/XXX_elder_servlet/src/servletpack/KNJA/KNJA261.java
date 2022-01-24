// kanji=漢字
/*
 * $Id: c496d43ec9f4e2174b08d721ddda1ed406c72955 $
 *
 * 作成日: 2011/03/13 23:09:52 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.util.ArrayList;
import java.util.List;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.AbstractXls;
import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: c496d43ec9f4e2174b08d721ddda1ed406c72955 $
 */
public class KNJA261 extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJA261.class");

    private boolean _hasData;

    Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void xls_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        _param = createParam(_db2, request);

        //テンプレートの読込&初期化
        setIniTmpBook(_param._templatePath);

        //ヘッダデータ取得
        _headList = getHeadData();

        //出力データ取得
        _dataList = getXlsDataList();

        outPutXls(response, _param._header);
    }
    protected List getXlsDataList() throws SQLException {
        final String[] cols = getCols();
        List dataList = new ArrayList();
        final String sql = getSql();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String chairSql = getChairSql();
        PreparedStatement psChar = null;
        ResultSet rsChar = null;
        final String chairNameSql = getChairNameSql();
        PreparedStatement psCharName = null;
        ResultSet rsCharName = null;
        try {
            ps = _db2.prepareStatement(sql);
            rs = ps.executeQuery();
            psChar = _db2.prepareStatement(chairSql);
            psCharName = _db2.prepareStatement(chairNameSql);
            while (rs.next()) {
                final List xlsData = new ArrayList();
                for (int i = 0; i < cols.length; i++) {
                    if ("CHAIRCD".equals(cols[i])) {
                        int chairset = 1;
                        psChar.setString(chairset++, rs.getString("YEAR"));
                        psChar.setString(chairset++, rs.getString("SEMESTER"));
                        psChar.setString(chairset++, rs.getString("SUBCLASSCD"));
                        psChar.setString(chairset++, rs.getString("SCHREGNO"));
                        rsChar = psChar.executeQuery();
                        rsChar.next();
                        final String chairCd  = (rsChar.getInt("CNT") > 1) ? '*' + rsChar.getString("CHAIRCD") : rsChar.getString("CHAIRCD");
                        xlsData.add(chairCd);
                    } else if ("CHAIRNAME".equals(cols[i])) {
                        int chairset2 = 1;
                        psCharName.setString(chairset2++, rs.getString("YEAR"));
                        psCharName.setString(chairset2++, rs.getString("SEMESTER"));
                        psCharName.setString(chairset2++, rsChar.getString("CHAIRCD"));
                        rsCharName = psCharName.executeQuery();
                        rsCharName.next();
                        final String chairName = rsCharName.getString("CHAIRNAME");
                        xlsData.add(chairName);
                    } else {
                        xlsData.add(rs.getString(cols[i]));
                    }
                }
                xlsData.add("DUMMY");
                dataList.add(xlsData);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            DbUtils.closeQuietly(null, psChar, rsChar);
            DbUtils.closeQuietly(null, psCharName, rsCharName);
            _db2.commit();
        }

        return dataList;
    }

    private final String getChairSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     COUNT(T1.CHAIRCD) AS CNT, ");
        stb.append("     VALUE(MIN(T1.CHAIRCD), '') AS CHAIRCD ");
        stb.append(" FROM ");
        stb.append("     CHAIR_DAT T1, ");
        stb.append("     CHAIR_STD_DAT T2 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = T2.YEAR AND ");
        stb.append("     T1.YEAR = ? AND ");
        stb.append("     T1.SEMESTER = T2.SEMESTER AND ");
        stb.append("     T1.SEMESTER = ? AND ");
        stb.append("     T1.CHAIRCD = T2.CHAIRCD AND ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(                  " T1.CLASSCD  || '-' || T1.SCHOOL_KIND || '-' ||  T1.CURRICULUM_CD  || '-' || ");
        }
        stb.append("     T1.SUBCLASSCD = ? AND ");
        stb.append("     T2.SCHREGNO = ? ");
        return stb.toString();
    }

    private final String getChairNameSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     CHAIRNAME ");
        stb.append(" FROM ");
        stb.append("     CHAIR_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = ? AND ");
        stb.append("     SEMESTER = ? AND ");
        stb.append("     CHAIRCD = ? ");
        return stb.toString();
    }

    protected List getHeadData() {
        final List retList = new ArrayList();
        retList.add("年度");
        retList.add("学期");
        retList.add("学籍番号");
        retList.add("学年");
        retList.add("組");
        retList.add("出席番号");
        retList.add("生徒氏名");
        retList.add("科目コード");
        retList.add("科目名称");
        retList.add("単位数");
        retList.add("受講講座コード");
        retList.add("受講講座名称");
        retList.add("半期認定フラグ");
        retList.add("無条件履修修得フラグ");
        return retList;
    }

    protected String[] getCols() {
        final String[] cols = {"YEAR",
                "SEMESTER",
                "SCHREGNO",
                "GRADE",
                "HR_CLASS",
                "ATTENDNO",
                "NAME_SHOW",
                "SUBCLASSCD",
                "SUBCLASSNAME",
                "CREDITS",
                "CHAIRCD",
                "CHAIRNAME",
                "AUTHORIZE_FLG",
                "COMP_UNCONDITION_FLG",};
        return cols;
    }

    protected String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHNO AS ( ");
        stb.append("     SELECT SCHREGNO,GRADE,HR_CLASS,ATTENDNO,COURSECD,MAJORCD,COURSECODE ");
        stb.append("       FROM SCHREG_REGD_DAT ");
        stb.append("      WHERE YEAR = '" + _param._year + "' ");
        stb.append("        AND SEMESTER = '" + _param._semester + "' ");
        if ("2".equals(_param._kubun)) {
            stb.append("    AND SCHREGNO IN (" + _param.getInState() + ") ");
        } else {
            stb.append("    AND GRADE || HR_CLASS IN (" + _param.getInState() + ") ");
        }
        stb.append("     ) ");
        stb.append(" ,CHAIR_SCHNO AS ( ");
        stb.append("     SELECT DISTINCT ");
        stb.append("            W1.SCHREGNO,W1.GRADE,W1.HR_CLASS,W1.ATTENDNO, ");
        stb.append("            W2.YEAR,W2.SEMESTER,");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(                  " W2.CLASSCD  || '-' || W2.SCHOOL_KIND || '-' ||  W2.CURRICULUM_CD  || '-' || ");
        }
        stb.append("            W2.SUBCLASSCD AS SUBCLASSCD ");
        stb.append("       FROM SCHNO W1, ");
        stb.append("            CHAIR_DAT W2, ");
        stb.append("            CHAIR_STD_DAT W3 ");
        stb.append("      WHERE W2.YEAR = '" + _param._year + "' ");
        stb.append("        AND W2.SEMESTER = '" + _param._semester + "' ");
        stb.append("        AND W3.YEAR = W2.YEAR ");
        stb.append("        AND W3.SEMESTER = W2.SEMESTER ");
        stb.append("        AND W3.CHAIRCD = W2.CHAIRCD ");
        stb.append("        AND W3.SCHREGNO = W1.SCHREGNO ");
        stb.append("        AND '" + _param._date.replace('/', '-') + "' BETWEEN W3.APPDATE AND W3.APPENDDATE ");
        stb.append("     ) ");
        stb.append(" ,CREDIT AS ( ");
        stb.append("     SELECT DISTINCT ");
        stb.append("            W1.SCHREGNO,W1.GRADE,W1.HR_CLASS,W1.ATTENDNO, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(                  " W2.CLASSCD  || '-' || W2.SCHOOL_KIND || '-' ||  W2.CURRICULUM_CD  || '-' || ");
        }
        stb.append("            W2.SUBCLASSCD AS SUBCLASSCD,W2.CREDITS,W2.AUTHORIZE_FLG,W2.COMP_UNCONDITION_FLG ");
        stb.append("       FROM SCHNO W1, ");
        stb.append("            CREDIT_MST W2 ");
        stb.append("      WHERE W2.YEAR = '" + _param._year + "' ");
        stb.append("        AND W2.COURSECD = W1.COURSECD ");
        stb.append("        AND W2.MAJORCD = W1.MAJORCD ");
        stb.append("        AND W2.GRADE = W1.GRADE ");
        stb.append("        AND W2.COURSECODE = W1.COURSECODE ");
        stb.append("     ) ");
        stb.append("  ");
        stb.append(" SELECT T1.YEAR, T1.SEMESTER, T1.SCHREGNO, ");
        stb.append("        T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T3.NAME_SHOW, ");
        stb.append("        T1.SUBCLASSCD, T4.SUBCLASSNAME, T2.CREDITS, ");
        stb.append("        '' AS CHAIRCD, '' AS CHAIRNAME, ");
        stb.append("        T2.AUTHORIZE_FLG, T2.COMP_UNCONDITION_FLG ");
        stb.append("   FROM CHAIR_SCHNO T1 ");
        stb.append("        LEFT JOIN CREDIT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("                           AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("        LEFT JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("        LEFT JOIN SUBCLASS_MST T4 ON ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(                  " T4.CLASSCD  || '-' || T4.SCHOOL_KIND || '-' ||  T4.CURRICULUM_CD  || '-' || ");
        }
        stb.append("                          T4.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append(" ORDER BY T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.SUBCLASSCD ");
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
        private final String _year;
        private final String _semester;
        private final String _kubun;
        private final String _date;
        private final String _selectData;
        private final String[] _selectDatas;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final boolean _header;
        private final String _templatePath;
        private final String _useCurriculumcd;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("GAKKI");
            _kubun = request.getParameter("KUBUN");
            _date = request.getParameter("DATE");
            _selectData = request.getParameter("selectleft");
            _selectDatas = getSelectDatas();
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _header = true;
            _templatePath = request.getParameter("TEMPLATE_PATH");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
        }

        private String[] getSelectDatas() {
            String[] retStrs = StringUtils.split(_selectData, ",");
            if ("2".equals(_kubun)) {
                final String[] datas = StringUtils.split(_selectData, ",");
                for (int i = 0; i < datas.length; i++) {
                    final String[] setData = StringUtils.split(datas[i], '-');
                    retStrs[i] = setData[0];
                }
            }
            return retStrs;
        }

        public String getInState() {
            final StringBuffer stb = new StringBuffer();
            String sep = "";
            for (int i = 0; i < _selectDatas.length; i++) {
                stb.append(sep + "'" + _selectDatas[i] + "'");
                sep = ",";
            }
            return stb.toString();
        }

    }
}

// eof
