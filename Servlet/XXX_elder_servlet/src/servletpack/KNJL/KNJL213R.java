// kanji=漢字
/*
 * $Id: 905c9a7cd233b3e647ed89e117127174a60676f9 $
 *
 * 作成日: 2013/10/25 15:54:53 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2013 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 905c9a7cd233b3e647ed89e117127174a60676f9 $
 */
public class KNJL213R {

    private static final Log log = LogFactory.getLog("KNJL213R.class");

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
        final List printList = getPrintList(db2);
        Set_Head(db2, svf);
        for (final Iterator itBase = printList.iterator(); itBase.hasNext();) {
            final BaseData baseData = (BaseData) itBase.next();
            if (null == baseData) {
                continue;
            }
            if (baseData._befDataList.size() == 0) {
                svf.VrsOut("KAKUTEI", baseData._kakuteiMark);
                svf.VrsOut("EXAM_NO", baseData._examNo);
                svf.VrsOut("NAME1", baseData._name);
                svf.VrsOut("KANA1", baseData._nameKana);
                svf.VrsOut("SEX1", baseData._sex);
                svf.VrsOut("JH_CODE1", baseData._fsCd);
            }
            for (final Iterator itBef = baseData._befDataList.iterator(); itBef.hasNext();) {
                svf.VrsOut("KAKUTEI", baseData._kakuteiMark);
                svf.VrsOut("EXAM_NO", baseData._examNo);
                svf.VrsOut("NAME1", baseData._name);
                svf.VrsOut("KANA1", baseData._nameKana);
                svf.VrsOut("SEX1", baseData._sex);
                svf.VrsOut("JH_CODE1", baseData._fsCd);
                final BefData befData = (BefData) itBef.next();
                svf.VrsOut("BEFORE_NO", befData._page + "-" + befData._seq);
                svf.VrsOut("NAME2", befData._name);
                svf.VrsOut("KANA2", befData._nameKana);
                svf.VrsOut("SEX2", befData._sex);
                svf.VrsOut("JH_CODE2", befData._fsCd);
                svf.VrEndRecord();
            }
            _hasData = true;
        }
    }

    /** SVF-FORM **/
    private void Set_Head(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        svf.VrSetForm("KNJL213R.frm", 4);
        //タイトル年度
        final String setYear = nao_package.KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度";
        String setMakeDate = "";

        //  作成日(現在処理日)の取得
        ResultSet rs = null;
        try {
            String sql = "VALUES RTRIM(CHAR(DATE(SYSDATE()))),RTRIM(CHAR(HOUR(SYSDATE()))),RTRIM(CHAR(MINUTE(SYSDATE())))";
            db2.query(sql);
            rs = db2.getResultSet();
            String arr_ctrl_date[] = new String[3];
            int number = 0;
            while( rs.next() ){
                arr_ctrl_date[number] = rs.getString(1);
                number++;
            }
            setMakeDate = KNJ_EditDate.h_format_JP(arr_ctrl_date[0])+arr_ctrl_date[1]+"時"+arr_ctrl_date[2]+"分"+" 現在";
        } finally {
            DbUtils.close(rs);
            db2.commit();
        }

        svf.VrsOut("TITLE", setYear + "　高等学校" + _param._testDivName1 + "志願者/事前相談データ突合リスト");
        svf.VrsOut("SUBTITLE", (String) _param._jouken.get("SUBTITLE"));
        svf.VrsOut("DATE", setMakeDate);
    }//Set_Head()の括り

    private List getPrintList(final DB2UDB db2) throws SQLException {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String tukiawaseSql = getTukiawaseSql();
        try {
            ps = db2.prepareStatement(tukiawaseSql);
            rs = ps.executeQuery();
            BaseData baseData = null;
            String befExam = "";
            while (rs.next()) {
                final String examNo = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String kana = rs.getString("NAME_KANA");
                final String sex = rs.getString("SEX");
                final String fsCd = rs.getString("FS_CD");
                final String fsName = rs.getString("FINSCHOOL_NAME");
                final String befPage = rs.getString("BEFORE_PAGE");
                final String befSeq = rs.getString("BEFORE_SEQ");
                final String befName = rs.getString("BEF_NAME");
                final String befKana = rs.getString("BEF_KANA");
                final String befSex = rs.getString("BEF_SEX");
                final String befFsCd = rs.getString("BEF_FS_CD");
                final String befFsName = rs.getString("BEF_FINSCHOOL");
                final String kakutei = rs.getString("KAKUTEI_FLG");
                if (!befExam.equals(examNo)) {
                    if (!"".equals(befExam)) {
                        retList.add(baseData);
                    }
                    baseData = new BaseData(examNo, name, kana, sex, fsCd, fsName, kakutei);
                }
                final BefData befData = new BefData(befPage, befSeq, befName, befKana, befSex, befFsCd, befFsName);
                baseData._befDataList.add(befData);
                befExam = examNo;
            }
            retList.add(baseData);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getTukiawaseSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH KAKUTEI AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.NAME_KANA, ");
        stb.append("     Z002.NAME2 AS SEX, ");
        stb.append("     T1.FS_CD, ");
        stb.append("     FIN.FINSCHOOL_NAME, ");
        stb.append("     BEF.BEFORE_PAGE, ");
        stb.append("     BEF.BEFORE_SEQ, ");
        stb.append("     BEF.NAME AS BEF_NAME, ");
        stb.append("     BEF.NAME_KANA AS BEF_KANA, ");
        stb.append("     Z002_2.NAME2 AS BEF_SEX, ");
        stb.append("     BEF.FS_CD AS BEF_FS_CD, ");
        stb.append("     FIN2.FINSCHOOL_NAME AS BEF_FINSCHOOL ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FIN ON T1.FS_CD = FIN.FINSCHOOLCD ");
        stb.append("     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
        stb.append("          AND T1.SEX = Z002.NAMECD2 ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D ON T1.ENTEXAMYEAR = BASE_D.ENTEXAMYEAR ");
        stb.append("          AND T1.APPLICANTDIV = BASE_D.APPLICANTDIV ");
        stb.append("          AND T1.EXAMNO = BASE_D.EXAMNO ");
        stb.append("          AND BASE_D.SEQ = '002' ");
        stb.append("          AND (BASE_D.REMARK1 IS NOT NULL AND BASE_D.REMARK2 IS NOT NULL) ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANT_BEFORE_DAT BEF ON T1.ENTEXAMYEAR = BEF.ENTEXAMYEAR ");
        stb.append("          AND T1.APPLICANTDIV = BEF.APPLICANTDIV ");
        stb.append("          AND T1.TESTDIV = BEF.TESTDIV ");
        stb.append("          AND BEF.BEFORE_PAGE = BASE_D.REMARK1 ");
        stb.append("          AND BEF.BEFORE_SEQ = BASE_D.REMARK2 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FIN2 ON BEF.FS_CD = FIN2.FINSCHOOLCD ");
        stb.append("     LEFT JOIN NAME_MST Z002_2 ON Z002_2.NAMECD1 = 'Z002' ");
        stb.append("          AND BEF.SEX = Z002_2.NAMECD2 ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._year + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND T1.TESTDIV = '" + _param._testDiv + "' ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     '0' AS KAKUTEI_FLG, ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.NAME_KANA, ");
        stb.append("     Z002.NAME2 AS SEX, ");
        stb.append("     T1.FS_CD, ");
        stb.append("     FIN.FINSCHOOL_NAME, ");
        stb.append("     BEF.BEFORE_PAGE, ");
        stb.append("     BEF.BEFORE_SEQ, ");
        stb.append("     BEF.NAME AS BEF_NAME, ");
        stb.append("     BEF.NAME_KANA AS BEF_KANA, ");
        stb.append("     Z002_2.NAME2 AS BEF_SEX, ");
        stb.append("     BEF.FS_CD AS BEF_FS_CD, ");
        stb.append("     FIN2.FINSCHOOL_NAME AS BEF_FINSCHOOL ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FIN ON T1.FS_CD = FIN.FINSCHOOLCD ");
        stb.append("     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
        stb.append("          AND T1.SEX = Z002.NAMECD2 ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANT_BEFORE_DAT BEF ON T1.ENTEXAMYEAR = BEF.ENTEXAMYEAR ");
        stb.append("          AND T1.APPLICANTDIV = BEF.APPLICANTDIV ");
        stb.append("          AND T1.TESTDIV = BEF.TESTDIV ");
        stb.append("          AND T1.NAME_KANA " + (String) _param._jouken.get("KANA") + " BEF.NAME_KANA ");
        stb.append("          AND T1.FS_CD " + (String) _param._jouken.get("SCHOOL") + " BEF.FS_CD ");
        stb.append("          AND T1.SEX " + (String) _param._jouken.get("SEX") + " BEF.SEX ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FIN2 ON BEF.FS_CD = FIN2.FINSCHOOLCD ");
        stb.append("     LEFT JOIN NAME_MST Z002_2 ON Z002_2.NAMECD1 = 'Z002' ");
        stb.append("          AND BEF.SEX = Z002_2.NAMECD2 ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._year + "' ");
        stb.append("     AND NOT EXISTS ( ");
        stb.append("                     SELECT ");
        stb.append("                        'x' ");
        stb.append("                     FROM ");
        stb.append("                        KAKUTEI E1 ");
        stb.append("                     WHERE ");
        stb.append("                        T1.EXAMNO = E1.EXAMNO ");
        stb.append("                    ) ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND T1.TESTDIV = '" + _param._testDiv + "' ");
        stb.append("     AND NOT EXISTS ( ");
        stb.append("                     SELECT ");
        stb.append("                        'x' ");
        stb.append("                     FROM ");
        stb.append("                        KAKUTEI E2 ");
        stb.append("                     WHERE ");
        stb.append("                        BEF.BEFORE_PAGE = E2.BEFORE_PAGE ");
        stb.append("                        AND BEF.BEFORE_SEQ = E2.BEFORE_SEQ ");
        stb.append("                    ) ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     '1' AS KAKUTEI_FLG, ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.NAME_KANA, ");
        stb.append("     T1.SEX, ");
        stb.append("     T1.FS_CD, ");
        stb.append("     T1.FINSCHOOL_NAME, ");
        stb.append("     T1.BEFORE_PAGE, ");
        stb.append("     T1.BEFORE_SEQ, ");
        stb.append("     T1.BEF_NAME, ");
        stb.append("     T1.BEF_KANA, ");
        stb.append("     T1.BEF_SEX, ");
        stb.append("     T1.BEF_FS_CD, ");
        stb.append("     T1.BEF_FINSCHOOL ");
        stb.append(" FROM ");
        stb.append("     KAKUTEI T1 ");
        stb.append(" ORDER BY ");
        stb.append("     KAKUTEI_FLG, ");
        stb.append("     EXAMNO, ");
        stb.append("     BEFORE_PAGE, ");
        stb.append("     BEFORE_SEQ ");

        return stb.toString();
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    private class BaseData {
        private final String _examNo;
        private final String _name;
        private final String _nameKana;
        private final String _sex;
        private final String _fsCd;
        private final String _fsName;
        private final String _kakuteiMark;
        private List _befDataList;

        public BaseData (
                final String examNo,
                final String name,
                final String nameKana,
                final String sex,
                final String fsCd,
                final String fsName,
                final String kakutei
        ) {
            _examNo = examNo;
            _name = name;
            _nameKana = nameKana;
            _sex = sex;
            _fsCd = fsCd;
            _fsName = fsName;
            _kakuteiMark = "1".equals(kakutei) ? "●" : "";
            _befDataList = new ArrayList();
        }
    }

    private class BefData {
        private final String _page;
        private final String _seq;
        private final String _name;
        private final String _nameKana;
        private final String _sex;
        private final String _fsCd;
        private final String _fsName;

        /**
         * コンストラクタ。
         */
        public BefData(
                final String page,
                final String seq,
                final String name,
                final String nameKana,
                final String sex,
                final String fsCd,
                final String fsName
        ) {
            _page = page;
            _seq = seq;
            _name = name;
            _nameKana = nameKana;
            _sex = sex;
            _fsCd = fsCd;
            _fsName = fsName;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 63854 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _centerTitle;
        private final Map _jouken;
        private final String _testDivName1;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _centerTitle = request.getParameter("CENTER_TITLE");
            _jouken = getJouken(_centerTitle);
            _testDivName1 = StringUtils.defaultString(getNameMst(db2, "NAME1", "L004", _testDiv));
        }

        private Map getJouken(final String centerTitle) {
            final Map retMap = new HashMap();
            if ("1".equals(centerTitle)) {
                retMap.put("KANA", " = ");
                retMap.put("SCHOOL", " = ");
                retMap.put("SEX", " <> ");
                retMap.put("SUBTITLE", "（氏名かな〇、出身学校〇、性別×）");
            } else if ("2".equals(centerTitle)) {
                retMap.put("KANA", " = ");
                retMap.put("SCHOOL", " <> ");
                retMap.put("SEX", " = ");
                retMap.put("SUBTITLE", "（氏名かな〇、出身学校×、性別〇）");
            } else if ("3".equals(centerTitle)) {
                retMap.put("KANA", " = ");
                retMap.put("SCHOOL", " <> ");
                retMap.put("SEX", " <> ");
                retMap.put("SUBTITLE", "（氏名かな〇、出身学校×、性別×）");
            } else if ("4".equals(centerTitle)) {
                retMap.put("KANA", " <> ");
                retMap.put("SCHOOL", " = ");
                retMap.put("SEX", " = ");
                retMap.put("SUBTITLE", "（氏名かな×、出身学校〇、性別〇）");
            } else if ("5".equals(centerTitle)) {
                retMap.put("KANA", " = ");
                retMap.put("SCHOOL", " = ");
                retMap.put("SEX", " = ");
                retMap.put("SUBTITLE", "（氏名かな〇、出身学校〇、性別〇）");
            }
            return retMap;
        }

        private String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
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
