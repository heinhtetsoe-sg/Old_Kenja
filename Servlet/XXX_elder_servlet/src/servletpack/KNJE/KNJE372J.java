// kanji=漢字
/*
 * $Id: 01a502431f9c9de7dca1b96de4aad49f0d2ea851 $
 *
 * 作成日: 2009/10/21 8:52:18 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: 01a502431f9c9de7dca1b96de4aad49f0d2ea851 $
 */
public class KNJE372J {

    private static final Log log = LogFactory.getLog("KNJE372J.class");

    private boolean _hasData;

    Param _param;

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
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf);

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            closeDb(db2);
            svf.VrQuit();
        }

    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final List printData = getPrintData(db2);
        svf.VrSetForm("KNJE372J.frm", 4);
        final int MAX_LINE = 25;
        int line = 0;
        for (final Iterator it = printData.iterator(); it.hasNext();) {
            final PrintData data = (PrintData)it.next();

            svf.VrsOut("TITLE", _param._nendo + "　附属高等学校推薦入学試験"); //タイトル
            svf.VrsOut("SCHOOL_NAME", _param._schoolName); //学校名

            line++;
            final int page1 = printData.size() % MAX_LINE == 0 ? printData.size() / MAX_LINE : printData.size() / MAX_LINE + 1;
            final int page2 = line % MAX_LINE == 0 ? line / MAX_LINE : line / MAX_LINE + 1;
            svf.VrsOut("PAGE1", String.valueOf(page1)); //nn枚中の
            svf.VrsOut("PAGE2", String.valueOf(page2)); //n枚目

            svf.VrsOut("FACULTY_NAME", data._facultyListName); //学部
            svf.VrsOut("DEPARTMENT_NAME", data._departmentListName); //学科（専攻）
            svf.VrsOut("DEPARTMENT_CD", data._departmentListCd); //学科コード
            //氏名（カタカナ）
            final String[] nameKana = data._nameKana.split("　");
            svf.VrsOut("NAME1", nameKana.length > 0 ? nameKana[0] : ""); //（姓）
            svf.VrsOut("NAME2", nameKana.length > 1 ? nameKana[1] : ""); //（名）
            //推薦基準の区分
            svf.VrsOut("DIV1", "ア".equals(data._baseDiv) ? "〇" : "");
            svf.VrsOut("DIV2", "イ".equals(data._baseDiv) ? "〇" : "");
            svf.VrsOut("DIV3", "ウ".equals(data._baseDiv) ? "〇" : "");

            svf.VrEndRecord();
            _hasData = true;
        }
    }

    private List getPrintData(final DB2UDB db2) throws SQLException {
        final List rtnList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getPrintSql();
            log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String facultyListName = StringUtils.defaultString(rs.getString("FACULTY_LIST_NAME"));
                final String departmentListName = StringUtils.defaultString(rs.getString("DEPARTMENT_LIST_NAME"));
                final String departmentListCd = StringUtils.defaultString(rs.getString("DEPARTMENT_LIST_CD"));
                final String schregno = StringUtils.defaultString(rs.getString("SCHREGNO"));
                final String nameKana = StringUtils.defaultString(rs.getString("NAME_KANA"));
                final String baseDiv = StringUtils.defaultString(rs.getString("BASE_DIV"));

                final PrintData printData = new PrintData(facultyListName, departmentListName, departmentListCd, schregno, nameKana, baseDiv);
                rtnList.add(printData);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnList;
    }

    private String getPrintSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     RECOM.FACULTY_LIST_NAME, ");
        stb.append("     RECOM.DEPARTMENT_LIST_NAME, ");
        stb.append("     RECOM.DEPARTMENT_LIST_CD, ");
        stb.append("     BASE.SCHREGNO, ");
        stb.append("     TRANSLATE_H_K(BASE.NAME_KANA) AS NAME_KANA, ");
        stb.append("     HOPE.RECOMMENDATION_BASE_DIV AS BASE_DIV ");
        stb.append(" FROM ");
        stb.append("     AFT_SCHREG_CONVERT_SCORE_DAT CONV ");
        stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = CONV.SCHREGNO ");
        stb.append("     INNER JOIN AFT_RECOMMENDATION_LIMIT_MST RECOM ON RECOM.YEAR = CONV.YEAR ");
        stb.append("         AND (RECOM.DEPARTMENT_S = CONV.RECOMMENDATION_DEPARTMENT_CD OR RECOM.DEPARTMENT_H = CONV.RECOMMENDATION_DEPARTMENT_CD) ");
        stb.append("     LEFT JOIN AFT_SCHREG_HOPE_DEPARTMENT HOPE ON HOPE.YEAR = CONV.YEAR ");
        stb.append("         AND HOPE.SCHREGNO = CONV.SCHREGNO ");
        stb.append("         AND HOPE.DEPARTMENT_CD = CONV.RECOMMENDATION_DEPARTMENT_CD ");
        stb.append(" WHERE ");
        stb.append("     CONV.YEAR = '" + _param._year + "' ");
        stb.append(" ORDER BY ");
        stb.append("     RECOM.DEPARTMENT_LIST_ORDER, ");   //推薦枠マスタ.推薦名簿出力順
        stb.append("     HOPE.RECOMMENDATION_BASE_DIV, ");  //志望学科データ.推薦基準区分
        stb.append("     CONV.CONVERT_SCORE DESC ");        //換算値データ.換算値の降順

        return stb.toString();
    }

    private class PrintData {
        final String _facultyListName;
        final String _departmentListName;
        final String _departmentListCd;
        final String _schregno;
        final String _nameKana;
        final String _baseDiv;

        PrintData(final String facultyListName,
                final String departmentListName,
                final String departmentListCd,
                final String schregno,
                final String nameKana,
                final String baseDiv
        ) {
            _facultyListName = facultyListName;
            _departmentListName = departmentListName;
            _departmentListCd = departmentListCd;
            _schregno = schregno;
            _nameKana = nameKana;
            _baseDiv = baseDiv;
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return _schregno + " = " + _nameKana;
        }
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 76002 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _ctrlSemester;
        final String _ctrlDate;

        final String _nendo;
        final String _schoolName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlSemester = request.getParameter("LOGIN_SEMESTER");
            _ctrlDate = request.getParameter("LOGIN_DATE");

            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "年度";
            _schoolName = getSchoolName(db2);
        }
        private String getSchoolName(final DB2UDB db2) throws SQLException {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + _year + "' AND SCHOOL_KIND = 'H'"));
        }
    }
}

// eof
