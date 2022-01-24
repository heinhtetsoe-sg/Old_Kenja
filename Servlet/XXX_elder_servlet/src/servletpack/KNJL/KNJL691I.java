// kanji=漢字
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJL691I {

    private static final Log log = LogFactory.getLog("KNJL691I.class");

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

            _hasData = printMain(db2, svf);
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

    private boolean printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final Map applicantMap = getApplicantMap(db2); //願書から募集データ検索
        final Map recruitMap   = getRecruitMap(db2);   //願書に紐づかない募集データ
        boolean printFlg = false;

        if (applicantMap.isEmpty() && recruitMap.isEmpty()) {
            return printFlg;
        }

        final int maxLine = 24; //最大行
        final String maxPage = getMaxPage(maxLine, applicantMap.size(), recruitMap.size()); //最大ページ数
        int page = 1; //ページ数
        int line = 1; //印字行

        svf.VrSetForm("KNJL691I.frm" , 1);
        setTitle(svf, String.valueOf(page), maxPage);

        //願書のループ
        for (Iterator ite = applicantMap.values().iterator(); ite.hasNext();) {
            final PrintData printData = (PrintData) ite.next();

            if (line > maxLine) {
                svf.VrEndPage();
                setTitle(svf, String.valueOf(++page), maxPage);
                line = 1;
            }

            //上段　募集データ
            svf.VrsOutn("EXAM_DIV", line, "募");
            if (printData._bosyu_Examno != null) {
                final int nameKeta = KNJ_EditEdit.getMS932ByteLength(printData._bosyu_Name);
                final String nameField = nameKeta <= 20 ? "1" : nameKeta <= 30 ? "2" : "3";
                svf.VrsOutn("NAME" + nameField, line, printData._bosyu_Name); //氏名

                svf.VrsOutn("CONSUL_COURSE", line, printData._soudan); //相談コース
                svf.VrsOutn("FINSCHOOL_CD", line, printData._bosyu_Fs_Cd); //学校コード
                svf.VrsOutn("FINSCHOOL_NAME", line, printData._bosyu_School); //学校名
                svf.VrsOutn("COMMON_TEST", line, printData._common_Test); //共通テスト
                svf.VrsOutn("ABSENT", line, printData._kesseki); //欠席日数
                svf.VrsOutn("ABSENT_REASON", line, printData._kesseki_Riyuu); //欠席理由書
                svf.VrsOutn("SP_MARK", line, printData._tokutai); //特待記号
                svf.VrsOutn("CLUB_CD", line, printData._bukatsuno); //部活記号

                //備考
                final int bikouKeta = KNJ_EditEdit.getMS932ByteLength(printData._bikou);
                if (bikouKeta <= 24) {
                    svf.VrsOutn("REMARK1", line, printData._bikou);
                } else if (bikouKeta <= 30) {
                    svf.VrsOutn("REMARK2", line, printData._bikou);
                } else if (bikouKeta <= 50) {
                    svf.VrsOutn("REMARK3_2", line, printData._bikou);
                } else {
                    final List<String> bikou = KNJ_EditKinsoku.getTokenList(printData._bikou, 50);
                    for (int cnt = 0; cnt < bikou.size(); cnt++) {
                        svf.VrsOutn("REMARK3_" + (cnt + 1), line, bikou.get(cnt));
                    }
                }
            }
            line++;

            //下段　願書データ
            svf.VrsOutn("EXAM_NO", line, printData._shigan_Examno);
            svf.VrsOutn("EXAM_DIV", line, "願");
            final int nameKeta = KNJ_EditEdit.getMS932ByteLength(printData._shigan_Name);
            final String nameField = nameKeta <= 20 ? "1" : nameKeta <= 30 ? "2" : "3";
            svf.VrsOutn("NAME" + nameField, line, printData._shigan_Name); //氏名

            final int kanaKeta = KNJ_EditEdit.getMS932ByteLength(printData._shigan_Kana);
            final String kanaField = kanaKeta <= 20 ? "1" : kanaKeta <= 30 ? "2" : "3";
            svf.VrsOutn("KANA" + kanaField, line, printData._shigan_Kana); //カナ

            svf.VrsOutn("SEX", line, printData._shigan_Sex); //性別
            svf.VrsOutn("FINSCHOOL_CD", line, printData._shigan_Fs_Cd); //学校コード
            svf.VrsOutn("FINSCHOOL_NAME", line, printData._shigan_School); //学校名

            printFlg = true;
            line++;
        }

        //募集のループ
        for (Iterator ite = recruitMap.values().iterator(); ite.hasNext();) {
            final PrintData printData = (PrintData) ite.next();

            if (line > maxLine) {
                svf.VrEndPage();
                setTitle(svf, String.valueOf(++page), maxPage);
                line = 1;
            }

            //上段　募集データ
            svf.VrsOutn("EXAM_DIV", line, "募");
            final int nameKeta = KNJ_EditEdit.getMS932ByteLength(printData._bosyu_Name);
            final String nameField = nameKeta <= 20 ? "1" : nameKeta <= 30 ? "2" : "3";
            svf.VrsOutn("NAME" + nameField, line, printData._bosyu_Name); //氏名

            svf.VrsOutn("CONSUL_COURSE", line, printData._soudan); //相談コース
            svf.VrsOutn("FINSCHOOL_CD", line, printData._bosyu_Fs_Cd); //学校コード
            svf.VrsOutn("FINSCHOOL_NAME", line, printData._bosyu_School); //学校名
            svf.VrsOutn("COMMON_TEST", line, printData._common_Test); //共通テスト
            svf.VrsOutn("ABSENT", line, printData._kesseki); //欠席日数
            svf.VrsOutn("ABSENT_REASON", line, printData._kesseki_Riyuu); //欠席理由書
            svf.VrsOutn("SP_MARK", line, printData._tokutai); //特待記号
            svf.VrsOutn("CLUB_CD", line, printData._bukatsuno); //部活記号

            //備考
            final int bikouKeta = KNJ_EditEdit.getMS932ByteLength(printData._bikou);
            if (bikouKeta <= 24) {
                svf.VrsOutn("REMARK1", line, printData._bikou);
            } else if (bikouKeta <= 30) {
                svf.VrsOutn("REMARK2", line, printData._bikou);
            } else if (bikouKeta <= 50) {
                svf.VrsOutn("REMARK3_2", line, printData._bikou);
            } else {
                final List<String> bikou = KNJ_EditKinsoku.getTokenList(printData._bikou, 50);
                for (int cnt = 0; cnt < bikou.size(); cnt++) {
                    svf.VrsOutn("REMARK3_" + (cnt + 1), line, bikou.get(cnt));
                }
            }

            line++;

            //下段　願書データ
            svf.VrsOutn("EXAM_DIV", line, "願");

            printFlg = true;
            line++;
        }

        svf.VrEndPage();
        return printFlg;
    }

    private void setTitle(final Vrw32alp svf, final String page, final String maxPage) {
        svf.VrsOut("TITLE", _param._testName + "　募集・願書チェックリスト");
        svf.VrsOut("DATE", _param._date + " " + _param._time);
        svf.VrsOut("PAGE", "PAGE: " + page + "/" + maxPage);
    }

    private String getMaxPage(final int maxLine, final int size1, final int size2) {
        final int maxCnt = maxLine / 2;
        final int totalSize = size1 + size2;

        int page = totalSize % maxCnt;

        if (page == 0) {
            return String.valueOf(totalSize / maxCnt) ;
        } else {
            return String.valueOf(totalSize / maxCnt + 1);
        }
    }
    //願書から紐づく募集データ
    private String getApplicantSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.EXAMNO AS SHIGAN_EXAMNO, ");
        stb.append("     T1.NAME AS SHIGAN_NAME, ");
        stb.append("     T1.NAME_KANA AS SHIGAN_KANA, ");
        stb.append("     T1.FS_CD AS SHIGAN_FS_CD, ");
        stb.append("     SHIGAN_SCHOOL.FINSCHOOL_NAME_ABBV AS SHIGAN_SCHOOL, ");
        stb.append("     CASE WHEN T1.SEX = '1' THEN '男' ELSE '女' END AS SHIGAN_SEX, ");
        stb.append("     T2.EXAMNO AS BOSYU_EXAMNO, ");
        stb.append("     GENE04.GENERAL_NAME AS TOKUTAI, ");
        stb.append("     T2.NAME AS BOSYU_NAME, ");
        stb.append("     SEQ002.REMARK1 AS SOUDAN, ");
        stb.append("     T2.FS_CD AS BOSYU_FS_CD, ");
        stb.append("     BOSYU_SCHOOL.FINSCHOOL_NAME_ABBV AS BOSYU_SCHOOL, ");
        stb.append("     SEQ003.REMARK1 AS COMMON_TEST, ");
        stb.append("     SEQ008.REMARK1 AS KESSEKI, ");
        stb.append("     SEQ008.REMARK2 AS KESSEKI_RIYUU, ");
        stb.append("     SEQ005.REMARK1 AS BUKATSUNO, ");
        stb.append("     SEQ006.REMARK1 AS BIKOU ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     LEFT JOIN ");
        stb.append("         FINSCHOOL_MST SHIGAN_SCHOOL ");
        stb.append("              ON SHIGAN_SCHOOL.FINSCHOOLCD = T1.FS_CD ");
        stb.append("     LEFT JOIN ");
        stb.append("         ENTEXAM_RECRUIT_ADVICE_DAT T2 ");
        stb.append("              ON T2.FS_CD = T1.FS_CD ");
        stb.append("             AND T2.NAME  = T1.NAME ");
        stb.append("     LEFT JOIN ");
        stb.append("         ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT SEQ004 ");
        stb.append("              ON SEQ004.ENTEXAMYEAR = T2.ENTEXAMYEAR ");
        stb.append("             AND SEQ004.EXAMNO      = T2.EXAMNO ");
        stb.append("             AND SEQ004.SEQ         = '004' ");
        stb.append("     LEFT JOIN ");
        stb.append("         ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT SEQ002 ");
        stb.append("              ON SEQ002.ENTEXAMYEAR = T2.ENTEXAMYEAR ");
        stb.append("             AND SEQ002.EXAMNO      = T2.EXAMNO ");
        stb.append("             AND SEQ002.SEQ         = '002' ");
        stb.append("     LEFT JOIN ");
        stb.append("         ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT SEQ003 ");
        stb.append("              ON SEQ003.ENTEXAMYEAR = T2.ENTEXAMYEAR ");
        stb.append("             AND SEQ003.EXAMNO      = T2.EXAMNO ");
        stb.append("             AND SEQ003.SEQ         = '003' ");
        stb.append("     LEFT JOIN ");
        stb.append("         ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT SEQ008 ");
        stb.append("              ON SEQ008.ENTEXAMYEAR = T2.ENTEXAMYEAR ");
        stb.append("             AND SEQ008.EXAMNO      = T2.EXAMNO ");
        stb.append("             AND SEQ008.SEQ         = '008' ");
        stb.append("     LEFT JOIN ");
        stb.append("         ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT SEQ005 ");
        stb.append("              ON SEQ005.ENTEXAMYEAR = T2.ENTEXAMYEAR ");
        stb.append("             AND SEQ005.EXAMNO      = T2.EXAMNO ");
        stb.append("             AND SEQ005.SEQ         = '005' ");
        stb.append("     LEFT JOIN ");
        stb.append("         ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT SEQ006 ");
        stb.append("              ON SEQ006.ENTEXAMYEAR = T2.ENTEXAMYEAR ");
        stb.append("             AND SEQ006.EXAMNO      = T2.EXAMNO ");
        stb.append("             AND SEQ006.SEQ         = '006' ");
        stb.append("     LEFT JOIN ");
        stb.append("         ENTEXAM_GENERAL_MST GENE02 ");
        stb.append("              ON GENE02.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("             AND GENE02.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("             AND GENE02.TESTDIV      = '0' ");
        stb.append("             AND GENE02.GENERAL_DIV  = '02' ");
        stb.append("             AND GENE02.GENERAL_CD   = SEQ002.REMARK1 ");
        stb.append("     LEFT JOIN ");
        stb.append("         ENTEXAM_GENERAL_MST GENE04 ");
        stb.append("              ON GENE04.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("             AND GENE04.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("             AND GENE04.TESTDIV      = '0' ");
        stb.append("             AND GENE04.GENERAL_DIV  = '04' ");
        stb.append("             AND GENE04.GENERAL_CD   = SEQ004.REMARK2 ");
        stb.append("     LEFT JOIN ");
        stb.append("         ENTEXAM_GENERAL_MST GENE05 ");
        stb.append("              ON GENE05.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("             AND GENE05.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("             AND GENE05.TESTDIV      = '0' ");
        stb.append("             AND GENE05.GENERAL_DIV  = '05' ");
        stb.append("             AND GENE05.GENERAL_CD   = SEQ004.REMARK1 ");
        stb.append("     LEFT JOIN ");
        stb.append("         FINSCHOOL_MST BOSYU_SCHOOL ");
        stb.append("              ON BOSYU_SCHOOL.FINSCHOOLCD = T2.FS_CD ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR  = '" + _param._examYear + "' AND ");
        stb.append("     T1.APPLICANTDIV = '" + _param._applicantDiv + "' AND ");
        stb.append("     T1.TESTDIV0     = '" + _param._testDiv0 + "' AND ");
        stb.append("     T1.TESTDIV      = '" + _param._testDiv + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.EXAMNO ");

        return stb.toString();
    }

    //願書に紐づかない募集データ
    private String getRecruitSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.FS_CD AS FS_CD, ");
        stb.append("     SCHOOL.FINSCHOOL_NAME_ABBV AS SCHOOL, ");
        stb.append("     GENE04.GENERAL_NAME AS TOKUTAI, ");
        stb.append("     SEQ002.REMARK1 AS SOUDAN, ");
        stb.append("     SEQ003.REMARK1 AS BOSYU_TEST, ");
        stb.append("     SEQ008.REMARK1 AS BOSYU_KESSEKI, ");
        stb.append("     SEQ008.REMARK2 AS BOSYU_KESSEKI_RIYUU, ");
        stb.append("     SEQ005.REMARK1 AS BOSYU_BUKATSUNO, ");
        stb.append("     SEQ006.REMARK1 AS BOSYU_BIKOU ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECRUIT_ADVICE_DAT T1 ");
        stb.append("         INNER JOIN ");
        stb.append("             ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT SEQ001 ");
        stb.append("                  ON SEQ001.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("                 AND SEQ001.EXAMNO = T1.EXAMNO ");
        stb.append("                 AND SEQ001.SEQ = '001' ");
        stb.append("                 AND SEQ001.REMARK1 = '" + _param._testDiv + "' ");
        stb.append("         INNER JOIN ");
        stb.append("             ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT SEQ002 ");
        stb.append("                  ON SEQ002.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("                 AND SEQ002.EXAMNO = T1.EXAMNO ");
        stb.append("                 AND SEQ002.SEQ = '002' ");
        stb.append("         INNER JOIN ");
        stb.append("             ENTEXAM_GENERAL_MST GENE02 ");
        stb.append("                  ON GENE02.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("                 AND GENE02.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("                 AND GENE02.TESTDIV = '0' ");
        stb.append("                 AND GENE02.GENERAL_DIV = '02' ");
        stb.append("                 AND GENE02.GENERAL_CD = SEQ002.REMARK1 ");
        stb.append("                 AND GENE02.REMARK1 = '" + _param._testDiv0 + "' ");
        stb.append("         LEFT JOIN ");
        stb.append("             FINSCHOOL_MST SCHOOL ");
        stb.append("                  ON SCHOOL.FINSCHOOLCD = T1.FS_CD ");
        stb.append("         LEFT JOIN ");
        stb.append("             ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT SEQ004 ");
        stb.append("                  ON SEQ004.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("                 AND SEQ004.EXAMNO = T1.EXAMNO ");
        stb.append("                 AND SEQ004.SEQ = '004' ");
        stb.append("         LEFT JOIN ");
        stb.append("             ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT SEQ003 ");
        stb.append("                  ON SEQ003.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("                 AND SEQ003.EXAMNO = T1.EXAMNO ");
        stb.append("                 AND SEQ003.SEQ = '003' ");
        stb.append("         LEFT JOIN ");
        stb.append("             ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT SEQ008 ");
        stb.append("                  ON SEQ008.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("                 AND SEQ008.EXAMNO = T1.EXAMNO ");
        stb.append("                 AND SEQ008.SEQ = '008' ");
        stb.append("         LEFT JOIN ");
        stb.append("             ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT SEQ005 ");
        stb.append("                  ON SEQ005.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("                 AND SEQ005.EXAMNO = T1.EXAMNO ");
        stb.append("                 AND SEQ005.SEQ = '005' ");
        stb.append("         LEFT JOIN ");
        stb.append("             ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT SEQ006 ");
        stb.append("                  ON SEQ006.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("                 AND SEQ006.EXAMNO = T1.EXAMNO ");
        stb.append("                 AND SEQ006.SEQ = '006' ");
        stb.append("         LEFT JOIN ");
        stb.append("             ENTEXAM_GENERAL_MST GENE04 ");
        stb.append("                  ON GENE04.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("                 AND GENE04.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("                 AND GENE04.TESTDIV = '0' ");
        stb.append("                 AND GENE04.GENERAL_DIV = '04' ");
        stb.append("                 AND GENE04.GENERAL_CD = SEQ004.REMARK2 ");
        stb.append("         LEFT JOIN ");
        stb.append("             ENTEXAM_GENERAL_MST GENE05 ");
        stb.append("                  ON GENE05.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("                 AND GENE05.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("                 AND GENE05.TESTDIV = '0' ");
        stb.append("                 AND GENE05.GENERAL_DIV = '05' ");
        stb.append("                 AND GENE05.GENERAL_CD = SEQ004.REMARK1 ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._examYear + "' AND ");
        stb.append("     T1.EXAMNO NOT IN (SELECT ");
        stb.append("                             T2.EXAMNO ");
        stb.append("                         FROM ");
        stb.append("                             ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("                             INNER JOIN ");
        stb.append("                                 ENTEXAM_RECRUIT_ADVICE_DAT T2 ");
        stb.append("                                      ON T2.FS_CD = T1.FS_CD ");
        stb.append("                                     AND T2.NAME = T1.NAME ");
        stb.append("                         WHERE ");
        stb.append("                             T1.ENTEXAMYEAR = '" + _param._examYear + "' AND ");
        stb.append("                             T1.APPLICANTDIV = '" + _param._applicantDiv + "' AND ");
        stb.append("                             T1.TESTDIV = '" + _param._testDiv + "' AND ");
        stb.append("                             T1.TESTDIV0 = '" + _param._testDiv0 + "' ");
        stb.append("                         ) ");
        stb.append(" ORDER BY ");
        stb.append("     T1.EXAMNO ");

        return stb.toString();
    }


    private Map getApplicantMap(final DB2UDB db2) throws SQLException {
        Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;

        final String sql = getApplicantSql();

        try{
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String shigan_Examno = rs.getString("SHIGAN_EXAMNO");
                final String shigan_Name = rs.getString("SHIGAN_NAME");
                final String shigan_Kana = rs.getString("SHIGAN_KANA");
                final String shigan_Fs_Cd = rs.getString("SHIGAN_FS_CD");
                final String shigan_School = rs.getString("SHIGAN_SCHOOL");
                final String shigan_Sex = rs.getString("SHIGAN_SEX");
                final String bosyu_Examno = rs.getString("BOSYU_EXAMNO");
                final String tokutai = rs.getString("TOKUTAI");
                final String bosyu_Name = rs.getString("BOSYU_NAME");
                final String soudan = rs.getString("SOUDAN");
                final String bosyu_Fs_Cd = rs.getString("BOSYU_FS_CD");
                final String bosyu_School = rs.getString("BOSYU_SCHOOL");
                final String common_Test = rs.getString("COMMON_TEST");
                final String kesseki = rs.getString("KESSEKI");
                final String kesseki_Riyuu = rs.getString("KESSEKI_RIYUU");
                final String bukatsuno = rs.getString("BUKATSUNO");
                final String bikou = rs.getString("BIKOU");

                if (!retMap.containsKey(shigan_Examno)) {
                    final PrintData wk = new PrintData(shigan_Examno, shigan_Name, shigan_Kana, shigan_Fs_Cd,
                            shigan_School, shigan_Sex, bosyu_Examno, tokutai, bosyu_Name, soudan, bosyu_Fs_Cd,
                            bosyu_School, common_Test, kesseki, kesseki_Riyuu, bukatsuno, bikou);
                    retMap.put(shigan_Examno, wk);
                }
            }
        } catch (final SQLException e) {
            log.error("志願者の基本情報取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        return retMap;

    }

    private Map getRecruitMap(final DB2UDB db2) throws SQLException {
        Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;

        final String sql = getRecruitSql();

        try{
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String fs_Cd = rs.getString("FS_CD");
                final String school = rs.getString("SCHOOL");
                final String tokutai = rs.getString("TOKUTAI");
                final String soudan = rs.getString("SOUDAN");
                final String bosyu_Test = rs.getString("BOSYU_TEST");
                final String bosyu_Kesseki = rs.getString("BOSYU_KESSEKI");
                final String bosyu_Kesseki_Riyuu = rs.getString("BOSYU_KESSEKI_RIYUU");
                final String bosyu_Bukatsuno = rs.getString("BOSYU_BUKATSUNO");
                final String bosyu_Bikou = rs.getString("BOSYU_BIKOU");

                if (!retMap.containsKey(examno)) {
                    final PrintData wk = new PrintData("", "", "", "", "", "", examno, tokutai, name, soudan, fs_Cd,
                            school, bosyu_Test, bosyu_Kesseki, bosyu_Kesseki_Riyuu, bosyu_Bukatsuno, bosyu_Bikou);
                    retMap.put(examno, wk);
                }
            }
        } catch (final SQLException e) {
            log.error("志願者の基本情報取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        return retMap;

    }

    private class PrintData {
        final String _shigan_Examno;
        final String _shigan_Name;
        final String _shigan_Kana;
        final String _shigan_Fs_Cd;
        final String _shigan_School;
        final String _shigan_Sex;
        final String _bosyu_Examno;
        final String _tokutai;
        final String _bosyu_Name;
        final String _soudan;
        final String _bosyu_Fs_Cd;
        final String _bosyu_School;
        final String _common_Test;
        final String _kesseki;
        final String _kesseki_Riyuu;
        final String _bukatsuno;
        final String _bikou;

        public PrintData(final String shigan_Examno, final String shigan_Name, final String shigan_Kana,
                final String shigan_Fs_Cd, final String shigan_School, final String shigan_Sex,
                final String bosyu_Examno, final String tokutai, final String bosyu_Name, final String soudan,
                final String bosyu_Fs_Cd, final String bosyu_School, final String common_Test, final String kesseki,
                final String kesseki_Riyuu, final String bukatsuno, final String bikou) {
            _shigan_Examno = shigan_Examno;
            _shigan_Name = shigan_Name;
            _shigan_Kana = shigan_Kana;
            _shigan_Fs_Cd = shigan_Fs_Cd;
            _shigan_School = shigan_School;
            _shigan_Sex = shigan_Sex;
            _bosyu_Examno = bosyu_Examno;
            _tokutai = tokutai;
            _bosyu_Name = bosyu_Name;
            _soudan = soudan;
            _bosyu_Fs_Cd = bosyu_Fs_Cd;
            _bosyu_School = bosyu_School;
            _common_Test = common_Test;
            _kesseki = kesseki;
            _kesseki_Riyuu = kesseki_Riyuu;
            _bukatsuno = bukatsuno;
            _bikou = bikou;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _examYear; //入試年度
        final String _applicantDiv; // 2:固定
        final String _testDiv0; //学科
        final String _testDiv; //入試区分
        final String _date; //日付
        final String _time; //時間
        final String _testName; //試験区分名称

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _examYear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = "2";
            _testDiv0 = request.getParameter("TESTDIV0");
            _testDiv = request.getParameter("TESTDIV");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            _date = sdf.format(new Date());

            sdf = new SimpleDateFormat("HH:mm");
            _time = sdf.format(new Date());

            _testName = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT TESTDIV_NAME FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR = '" + _examYear + "' AND APPLICANTDIV = '" + _applicantDiv + "' AND TESTDIV = '" + _testDiv + "' "));
        }
    }
}

// eof
