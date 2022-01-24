// kanji=漢字
/*
 * $Id: 92c775037c827d5dc83ffdf2041593ce5ab70a1c $
 *
 * 作成日: 2011/03/07 16:13:53 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJB;

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
 * @version $Id: 92c775037c827d5dc83ffdf2041593ce5ab70a1c $
 */
public class KNJB170 extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJB170.class");

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
        final String sql = getSql();
        PreparedStatement psXls = null;
        ResultSet rsXls = null;
        final List dataList = new ArrayList();
        try {
            psXls = _db2.prepareStatement(sql);
            rsXls = psXls.executeQuery();
            while (rsXls.next()) {
                List xlsData = null;
                if ("1".equals(_param._radio)) {
                    xlsData = getTaniList(rsXls);
                } else {
                    xlsData = getSchList(rsXls);
                }
                dataList.add(xlsData);
            }
        } finally {
            DbUtils.closeQuietly(null, psXls, rsXls);
            _db2.commit();
        }
        return dataList;
    }

    private List getTaniList(final ResultSet rsXls) throws SQLException {
        final List xlsData = new ArrayList();
        xlsData.add(rsXls.getString("GRADE"));
        xlsData.add(rsXls.getString("HR_CLASS"));
        xlsData.add(rsXls.getString("ATTENDNO"));
        xlsData.add(rsXls.getString("NAME_SHOW"));
        xlsData.add(_param._schoolCd);
        xlsData.add(_param._majorCd);
        final String nendo = null == rsXls.getString("NENDO") ? "0000" : rsXls.getString("NENDO");
        xlsData.add(nendo);
        xlsData.add(rsXls.getString("SCHREGNO"));
        xlsData.add(_param._saiken);
        final String cntSub = null == rsXls.getString("CNT_SUB") ? "00" : rsXls.getString("CNT_SUB").length() <= 1 ? "0" + rsXls.getString("CNT_SUB") : rsXls.getString("CNT_SUB");
        xlsData.add(cntSub);
        final String cntCre = null == rsXls.getString("CNT_CRE") ? "00" : rsXls.getString("CNT_CRE").length() <= 1 ? "0" + rsXls.getString("CNT_CRE") : rsXls.getString("CNT_CRE");
        xlsData.add(cntCre);
        xlsData.add(rsXls.getString("FLG"));
        final String densanSub = _param._schoolCd + _param._majorCd + nendo + rsXls.getString("SCHREGNO") + _param._saiken + cntSub
                + rsXls.getString("FLG");
        xlsData.add(densanSub);
        final String densanCre = _param._schoolCd + _param._majorCd + nendo + rsXls.getString("SCHREGNO") + _param._saiken + cntCre
                + rsXls.getString("FLG");
        xlsData.add(densanCre);
        xlsData.add("DUMMY");
        return xlsData;
    }

    private List getSchList(final ResultSet rsXls) throws SQLException {
        final List xlsData = new ArrayList();
        String nameKana = null == rsXls.getString("NAME_KANA") ? "" : rsXls.getString("NAME_KANA");
        nameKana = getHankakuKana(nameKana, 20, ' ');

        String name = null == rsXls.getString("NAME") ? "" : rsXls.getString("NAME");
        name = StringUtils.rightPad(name, (40 - name.length()), ' ');

        String gNameKana = null == rsXls.getString("GUARD_KANA") ? "" : rsXls.getString("GUARD_KANA");
        gNameKana = getHankakuKana(gNameKana, 20, ' ');

        String gName = null == rsXls.getString("GUARD_NAME") ? "" : rsXls.getString("GUARD_NAME");
        gName = StringUtils.rightPad(gName, (40 - gName.length()), ' ');

        xlsData.add(_param._schoolCd);
        xlsData.add(_param._majorCd);
        xlsData.add(rsXls.getString("NENDO"));
        xlsData.add(rsXls.getString("SCHREGNO"));
        xlsData.add(nameKana);
        xlsData.add(name);
        xlsData.add(gNameKana);
        xlsData.add(gName);

        final int wareki = Integer.parseInt(_param._year) - 1988;
        final String densan = _param._schoolCd + _param._majorCd + String.valueOf(wareki) + rsXls.getString("SCHREGNO") +
                              nameKana + name + gNameKana + gName + "                                   ";
        xlsData.add(densan);

        xlsData.add("DUMMY");
        return xlsData;
    }

    private String getHankakuKana(final String nameKana, final int padLen, final char padChar) throws SQLException {
        String retStr = nameKana;
        retStr = retStr.replace('　', padChar);
        final String nameKanaSql = "VALUES(TRANSLATE_K_HK('" + retStr + "'))";
        PreparedStatement psNameKana = null;
        ResultSet rsNameKana = null;
        try {
            psNameKana = _db2.prepareStatement(nameKanaSql);
            rsNameKana = psNameKana.executeQuery();
            rsNameKana.next();
            retStr = rsNameKana.getString("1");
        } finally {
            DbUtils.closeQuietly(null, psNameKana, rsNameKana);
        }
        retStr = StringUtils.rightPad(retStr, padLen, padChar);
        return retStr;
    }

    protected List getHeadData() {
        final List retList = new ArrayList();
        if ("1".equals(_param._radio)) {
            retList.add("学年");
            retList.add("組");
            retList.add("出席番号");
            retList.add("氏名");
            retList.add("学校コード");
            retList.add("課程コード");
            retList.add("授業料年度");
            retList.add("生徒番号");
            retList.add("債権種別");
            retList.add("科目数");
            retList.add("合計単位数");
            retList.add("併修フラグ");
            retList.add("授業料電算に渡すデータ(科目数)");
            retList.add("授業料電算に渡すデータ(単位数)");
        } else {
            retList.add("学校コード");
            retList.add("課程コード");
            retList.add("授業料年度");
            retList.add("学籍番号");
            retList.add("生徒氏名(カナ・半角)");
            retList.add("生徒氏名(漢字)");
            retList.add("保護者氏名(カナ・半角)");
            retList.add("保護者氏名(漢字)");
            retList.add("授業料電算に渡すデータ(生徒情報)");
        }
        return retList;
    }

    protected String[] getCols() {
        final String[] cols = {"SCHREGNO",
                "YEAR",
                "SEMESTER",
                "CHAIRCD",
                "TEXTBOOKCD",};
        return cols;
    }

    protected String getSql() {
        final StringBuffer stb = new StringBuffer();
        if ("1".equals(_param._radio)) {
            //対象生徒
            stb.append(" WITH SCHNO AS ( ");
            stb.append("     SELECT ");
            stb.append("         YEAR, ");
            stb.append("         SCHREGNO, ");
            stb.append("         HR_CLASS, ");
            stb.append("         ATTENDNO, ");
            stb.append("         COURSECD, ");
            stb.append("         MAJORCD, ");
            stb.append("         GRADE, ");
            stb.append("         COURSECODE ");
            stb.append("     FROM ");
            stb.append("         SCHREG_REGD_DAT ");
            stb.append("     WHERE ");
            stb.append("         YEAR     = '" + _param._year + "' ");
            stb.append("     AND SEMESTER = '1' ");
            stb.append("  ) ");

            //各生徒が履修している科目に単位加算・固定、親科目、子科目どちらなのかの目印をつける
            stb.append(" ,SUBMAIN AS ( ");
            stb.append("     SELECT DISTINCT ");
            stb.append("         T1.SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("         L1.CLASSCD, ");
                stb.append("         L1.SCHOOL_KIND, ");
                stb.append("         L1.CURRICULUM_CD, ");
            }
            stb.append("         L1.SUBCLASSCD, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("         L2.COMBINED_CLASSCD, ");
                stb.append("         L2.COMBINED_SCHOOL_KIND, ");
                stb.append("         L2.COMBINED_CURRICULUM_CD, ");
            }
            stb.append("         L2.COMBINED_SUBCLASSCD, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("         L2.ATTEND_CLASSCD, ");
                stb.append("         L2.ATTEND_SCHOOL_KIND, ");
                stb.append("         L2.ATTEND_CURRICULUM_CD, ");
            }
            stb.append("         L2.ATTEND_SUBCLASSCD, ");
            stb.append("         L2.CALCULATE_CREDIT_FLG, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("         CASE WHEN L1.CLASSCD || L1.SCHOOL_KIND || L1.CURRICULUM_CD || L1.SUBCLASSCD = L2.COMBINED_CLASSCD || L2.COMBINED_SCHOOL_KIND || L2.COMBINED_CURRICULUM_CD || L2.COMBINED_SUBCLASSCD ");
            } else {
                stb.append("         CASE WHEN L1.SUBCLASSCD = L2.COMBINED_SUBCLASSCD ");
            }
            stb.append("             THEN 'COMB' ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("             WHEN L1.CLASSCD || L1.SCHOOL_KIND || L1.CURRICULUM_CD || L1.SUBCLASSCD = L2.ATTEND_CLASSCD || L2.ATTEND_SCHOOL_KIND || L2.ATTEND_CURRICULUM_CD || L2.ATTEND_SUBCLASSCD ");
            } else {
                stb.append("             WHEN L1.SUBCLASSCD = L2.ATTEND_SUBCLASSCD ");
            }
            stb.append("             THEN 'ATTE' ");
            stb.append("             ELSE NULL ");
            stb.append("         END AS COMB_ATTE_FLG ");
            stb.append("     FROM ");
            stb.append("         CHAIR_STD_DAT T1 ");
            stb.append("     INNER JOIN ");
            stb.append("         CHAIR_DAT L1 ON  L1.YEAR = T1.YEAR ");
            stb.append("                      AND L1.SEMESTER = T1.SEMESTER ");
            stb.append("                      AND L1.CHAIRCD = T1.CHAIRCD ");
            stb.append("                      AND SUBSTR(L1.SUBCLASSCD,1,2) <= '90' ");
            stb.append("     LEFT JOIN ");
            stb.append("         SUBCLASS_REPLACE_COMBINED_DAT L2 ON   L2.YEAR = T1.YEAR ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("                                          AND (L2.COMBINED_CLASSCD || L2.COMBINED_SCHOOL_KIND || L2.COMBINED_CURRICULUM_CD || L2.COMBINED_SUBCLASSCD = L1.CLASSCD || L1.SCHOOL_KIND || L1.CURRICULUM_CD || L1.SUBCLASSCD ");
                stb.append("                                          OR   L2.ATTEND_CLASSCD || L2.ATTEND_SCHOOL_KIND || L2.ATTEND_CURRICULUM_CD || L2.ATTEND_SUBCLASSCD   = L1.CLASSCD || L1.SCHOOL_KIND || L1.CURRICULUM_CD || L1.SUBCLASSCD) ");
            } else {
                stb.append("                                          AND (L2.COMBINED_SUBCLASSCD = L1.SUBCLASSCD ");
                stb.append("                                          OR   L2.ATTEND_SUBCLASSCD   = L1.SUBCLASSCD) ");
            }
            stb.append("      ");
            stb.append("     WHERE ");
            stb.append("         T1.YEAR = '" + _param._year + "' ");
            stb.append(" ) ");

            //各生徒が履修している科目
            //対象外のレコードを取除く(固定で合併元、加算で合併先は対象外)
            stb.append(" ,CHAIR AS ( ");
            stb.append("     SELECT ");
            stb.append("         SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("         CLASSCD, ");
                stb.append("         SCHOOL_KIND, ");
                stb.append("         CURRICULUM_CD, ");
            }
            stb.append("         SUBCLASSCD ");
            stb.append("     FROM ");
            stb.append("         SUBMAIN ");
            stb.append("     WHERE ");
            stb.append("             CALCULATE_CREDIT_FLG IS NULL ");
            stb.append("         OR (CALCULATE_CREDIT_FLG = '1' AND COMB_ATTE_FLG = 'COMB') ");
            stb.append("         OR (CALCULATE_CREDIT_FLG = '2' AND COMB_ATTE_FLG = 'ATTE') ");
            stb.append("     GROUP BY SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("         CLASSCD, ");
                stb.append("         SCHOOL_KIND, ");
                stb.append("         CURRICULUM_CD, ");
            }
            stb.append("         SUBCLASSCD ");
            stb.append("     ) ");

            //各科目の単位数
            stb.append(" ,CREDIT AS ( ");
            stb.append("     SELECT ");
            stb.append("         K2.SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("         K1.CLASSCD, ");
                stb.append("         K1.SCHOOL_KIND, ");
                stb.append("         K1.CURRICULUM_CD, ");
            }
            stb.append("         K1.SUBCLASSCD, ");
            stb.append("         K1.CREDITS ");
            stb.append("     FROM ");
            stb.append("         SCHNO K2 ");
            stb.append("     LEFT JOIN ");
            stb.append("         CREDIT_MST K1 ON  K1.YEAR       = K2.YEAR ");
            stb.append("                       AND K1.COURSECD   = K2.COURSECD ");
            stb.append("                       AND K1.MAJORCD    = K2.MAJORCD ");
            stb.append("                       AND K1.GRADE      = K2.GRADE ");
            stb.append("                       AND K1.COURSECODE = K2.COURSECODE ");
            stb.append("     ) ");

            //各生徒毎の科目と単位数
            //対象生徒が履修している科目数と合計単位数
            stb.append(" ,CNT_SUB AS ( ");
            stb.append("     SELECT ");
            stb.append("         W3.SCHREGNO, ");
            stb.append("         COUNT(W3.SUBCLASSCD) AS CNT_SUB, ");
            stb.append("         SUM(W2.CREDITS)      AS CNT_CRE ");
            stb.append("     FROM ");
            stb.append("         CHAIR W3 ");
            stb.append("     LEFT JOIN ");
            stb.append("         CREDIT W2 ON  W2.SCHREGNO   = W3.SCHREGNO ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("                   AND W2.CLASSCD = W3.CLASSCD ");
                stb.append("                   AND W2.SCHOOL_KIND = W3.SCHOOL_KIND ");
                stb.append("                   AND W2.CURRICULUM_CD = W3.CURRICULUM_CD ");
            }
            stb.append("                   AND W2.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("     GROUP BY ");
            stb.append("         W3.SCHREGNO ");
            stb.append("     ) ");

            //メイン
            stb.append(" SELECT ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     T3.NAME_SHOW, ");
            stb.append("     Fiscalyear(T3.ENT_DATE)  AS NENDO, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T2.CNT_SUB, ");
            stb.append("     T2.CNT_CRE, ");
            stb.append("     CASE WHEN T3.INOUTCD = '9' ");
            stb.append("          THEN '1' ");
            stb.append("          ELSE '0' ");
            stb.append("     END AS FLG ");
            stb.append(" FROM ");
            stb.append("     SCHNO T1 ");
            stb.append(" LEFT JOIN ");
            stb.append("     CNT_SUB T2         ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN ");
            stb.append("     SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ");
            stb.append(" ORDER BY ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.ATTENDNO ");
        } else {
            stb.append(" WITH SCHNO AS ( ");
            stb.append("     SELECT ");
            stb.append("         YEAR, ");
            stb.append("         SCHREGNO ");
            stb.append("     FROM ");
            stb.append("         SCHREG_REGD_DAT  ");
            stb.append("     WHERE ");
            stb.append("         YEAR     = '" + _param._year + "' AND  ");
            stb.append("         SEMESTER = '1' ) ");
            stb.append(" SELECT ");
            stb.append("     Fiscalyear(T2.ENT_DATE)  AS NENDO, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     TRANSLATE_H_HK(T2.NAME_KANA) AS NAME_KANA, ");
            stb.append("     T2.NAME, ");
            stb.append("     TRANSLATE_H_HK(T3.GUARD_KANA) AS GUARD_KANA, ");
            stb.append("     T3.GUARD_NAME ");
            stb.append(" FROM   SCHNO T1 ");
            stb.append(" LEFT JOIN ");
            stb.append("     SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN ");
            stb.append("     GUARDIAN_DAT    T3 ON T3.SCHREGNO = T1.SCHREGNO ");
            stb.append(" ORDER BY T1.SCHREGNO ");
        }

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
        private final String _radio;
        private final String _year;
        private final String _schoolCd;
        private final String _majorCd;
        private final String _saiken;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final boolean _header;
        private final String _templatePath;
        private final String _useCurriculumcd;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _radio = request.getParameter("RADIO");
            _year = request.getParameter("YEAR");
            _schoolCd = request.getParameter("SCHOOLCD");
            _majorCd = request.getParameter("MAJORCD");
            _saiken = request.getParameter("SAIKEN");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _header = true;
            _templatePath = request.getParameter("TEMPLATE_PATH");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
        }

    }
}

// eof
