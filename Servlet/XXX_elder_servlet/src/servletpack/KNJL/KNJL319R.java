/*
 * $Id: 68de3e3735ed2895bc8849536b41d914c8c68c50 $
 *
 * 作成日: 2019/01/10
 * 作成者: matsushima
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３１９Ｒ＞  特待生一覧
 **/
public class KNJL319R {

    private static final Log log = LogFactory.getLog(KNJL319R.class);

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

        final Map map = BaseDat.load(db2, _param);

        svf.VrSetForm("KNJL319R.frm", 1);
        setPrint(db2, svf, map);
    }

    private void setPrint(final DB2UDB db2, final Vrw32alp svf, final Map map) {
        final int maxLine = 50;

        String[] nendo = KNJ_EditDate.tate_format4(db2,_param._entexamyear + "-04-01");
        int renban = 1;

        final List pageList = getPageList((List)map.get(_param._shdiv), maxLine);

        for (int idx = 0; idx < pageList.size(); idx++) {
            final List baseDatList = (List) pageList.get(idx);
            svf.VrsOut("TITLE", nendo[0] + nendo[1] + "年度　" + _param._testdivName + "　特待生一覧"); // タイトル
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._date)); //作成日
            if (!"ALL".equals(_param._shdiv)) {
            	svf.VrsOut("SUBTITLE", _param._hopeMap.get(_param._shdiv).toString()); // サブタイトル
            }
            if("1".equals(_param._testdiv)) {
                setSubClass(svf, "1", "SUBCLASS_NAME1"); //科目名1
                setSubClass(svf, "2", "SUBCLASS_NAME2"); //科目名2
                setSubClass(svf, "4", "SUBCLASS_NAME3"); //科目名3
                setSubClass(svf, "5", "SUBCLASS_NAME4"); //科目名4
            }else {
                setSubClass(svf, "6", "SUBCLASS_NAME1"); //科目名1
                setSubClass(svf, "7", "SUBCLASS_NAME2"); //科目名2
                //科目名3は空白
                //科目名4は空白
            }
            svf.VrsOut("SUBCLASS_NAME5_2", "加点1"); //科目名4
            svf.VrsOut("SUBCLASS_NAME6_2", "加点2"); //科目名5

            for (int j = 0; j < baseDatList.size(); j++) {

                final BaseDat base = (BaseDat) baseDatList.get(j);
                final int line = j + 1;
                final String[] birthday = KNJ_EditDate.tate_format4(db2,base._birthday);

                svf.VrsOutn("NO", line, String.valueOf(renban++)); // NO.
                svf.VrsOutn("EXAM_NO", line, base._receptno); // 受験番号
                svf.VrsOutn("NAME", line, base._name); // 氏名
                svf.VrsOutn("KANA", line, base._nameKana); // かな
                svf.VrsOutn("BIRTHDAY", line, birthday[0] + birthday[1] + "." + birthday[2] + "." + birthday[3]); // 生年月日
                svf.VrsOutn("SCHOOL_NAME", line, base._finschoolName); // 出身学校名
                svf.VrsOutn("SCORE1", line, base._score1); // 得点1
                svf.VrsOutn("SCORE2", line, base._score2); // 得点2
                svf.VrsOutn("SCORE3", line, base._score3); // 得点3
                svf.VrsOutn("SCORE4", line, base._score4); // 得点4
                svf.VrsOutn("SCORE5", line, base._score5); // 得点5
                svf.VrsOutn("SCORE6", line, base._score6); // 得点6
                svf.VrsOutn("TOTAL_SCORE1", line, base._totalScore1); // 合計
                svf.VrsOutn("JUDGE", line, base._judge); // 合否
                svf.VrsOutn("PROCEDURE", line, base._procedure); // 手続
                svf.VrsOutn("ENT", line, base._ent); // 入学
                svf.VrsOutn("EXAM_NO2", line, base._receptno2); // 受験番号
                svf.VrsOutn("CONSENT2", line, base._consent2); // 内諾
                svf.VrsOutn("REMARK", line, base._remark); // 備考
            }
            _hasData = true;
            svf.VrEndPage();
        }
    }

    private static int getMS932Bytecount(String str) {
        int count = 0;
        if (null != str) {
            try {
                count = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error(e);
            }
        }
        return count;
    }

    private void setSubClass(final Vrw32alp svf, final String srchMapStr, final String fieldBaseStr) {
        final String classNameStr = (String)_param._subclsMap.get(srchMapStr);
        final int classNameLen = KNJ_EditEdit.getMS932ByteLength(classNameStr);
        if (classNameLen > 4) {
            if (classNameLen > 6) {
                svf.VrsOut(fieldBaseStr + "_3", classNameStr); //科目名
            }else {
                svf.VrsOut(fieldBaseStr + "_2", classNameStr);//科目名
            }
        } else {
            svf.VrsOut(fieldBaseStr, classNameStr);//科目名
        }
    }

    private static class BaseDat {
        final String _hope;
        final String _hopeName;
        final String _receptno;
        final String _name;
        final String _nameKana;
        final String _birthday;
        final String _finschoolName;
        final String _score1;
        final String _score2;
        final String _score3;
        final String _score4;
        final String _score5;
        final String _score6;
        final String _totalScore1;
        final String _judge;
        final String _procedure;
        final String _ent;
        final String _receptno2;
        final String _consent2;
        final String _remark;

        BaseDat(
                final String hope,
                final String hopeName,
                final String receptno,
                final String name,
                final String nameKana,
                final String birthday,
                final String finschoolName,
                final String score1,
                final String score2,
                final String score3,
                final String score4,
                final String score5,
                final String score6,
                final String totalScore1,
                final String judge,
                final String procedure,
                final String ent,
                final String receptno2,
                final String consent2,
                final String remark
        ) {
            _hope = hope;
            _hopeName = hopeName;
            _receptno = receptno;
            _name = name;
            _nameKana = nameKana;
            _birthday = birthday;
            _finschoolName = finschoolName;
            _score1 = score1;
            _score2 = score2;
            _score3 = score3;;
            _score4 = score4;
            _score5 = score5;
            _score6 = score6;
            _totalScore1 = totalScore1;
            _judge = judge;
            _procedure = procedure;
            _ent = ent;
            _receptno2 = receptno2;
            _consent2 = consent2;
            _remark = remark;
        }

        public static Map load(final DB2UDB db2, final Param param) {
            Map map = new HashMap();
            final List hopeListAll = new ArrayList();
            final List hopeList1 = new ArrayList();
            final List hopeList2 = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String hope = StringUtils.defaultString(rs.getString("HOPE"));
                    final String hopeName = rs.getString("HOPENAME");
                    final String receptno = rs.getString("RECEPTNO");
                    final String name = rs.getString("NAME");
                    final String nameKana = rs.getString("NAME_KANA");
                    final String birthday = StringUtils.defaultString(rs.getString("BIRTHDAY"));
                    final String finschoolName = rs.getString("FINSCHOOL_NAME");
                    final String score1 = rs.getString("SCORE1");
                    final String score2 = rs.getString("SCORE2");
                    final String score3 = rs.getString("SCORE3");
                    final String score4 = rs.getString("SCORE4");
                    final String score5 = rs.getString("SCORE5");
                    final String score6 = rs.getString("SCORE6");
                    final String totalScore1 = rs.getString("TOTAL_SCORE1");
                    final String judge = rs.getString("JUDGE");
                    final String procedure = rs.getString("PROCEDURE");
                    final String ent = rs.getString("ENT");
                    final String receptno2 = rs.getString("RECEPTNO2");
                    final String consent2 = rs.getString("CONSENT2");
                    final String remark = rs.getString("REMARK");

                    final BaseDat basedat = new BaseDat(hope, hopeName, receptno, name, nameKana, birthday, finschoolName, score1, score2, score3, score4, score5, score6, totalScore1, judge, procedure, ent, receptno2, consent2, remark);
                    if ("ALL".equals(param._shdiv)) {
                    	hopeListAll.add(basedat);//すべて
                    } else if("1".equals(hope)) {
                        hopeList1.add(basedat);//専願
                    }else {
                        hopeList2.add(basedat);//併願
                    }
                }
                map.put("ALL", hopeListAll);
                map.put("1", hopeList1);
                map.put("2", hopeList2);
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return map;
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            if("1".equals(param._testdiv)) {
                stb.append("     BD2_013.REMARK1 AS HOPE, ");
            }else {
                stb.append("     BD2_013.REMARK2 AS HOPE, ");
            }
            stb.append("     NML006_1.NAME1 AS HOPENAME, ");
            stb.append("     R1.RECEPTNO, ");
            stb.append("     B1.NAME, ");
            stb.append("     B1.NAME_KANA, ");
            stb.append("     B1.BIRTHDAY, ");
            stb.append("     FINSCHOOL.FINSCHOOL_NAME, ");
            if("1".equals(param._testdiv)) {
                stb.append("     SCORE1.SCORE AS SCORE1, ");
                stb.append("     SCORE2.SCORE AS SCORE2, ");
                stb.append("     SCORE4.SCORE AS SCORE3, ");
                stb.append("     SCORE5.SCORE AS SCORE4, ");
            }else {
                stb.append("     SCORE6.SCORE AS SCORE1, ");
                stb.append("     SCORE7.SCORE AS SCORE2, ");
                stb.append("     ''           AS SCORE3, ");
                stb.append("     ''           AS SCORE4, ");
            }
            stb.append("     RD1_008.REMARK1 AS SCORE5, ");
            stb.append("     RD1_008.REMARK2 AS SCORE6, ");
            stb.append("     R1.TOTAL1 AS TOTAL_SCORE1, ");
            stb.append("     NML013.ABBV1 AS JUDGE, ");
            stb.append("     NML011.NAME1 AS PROCEDURE, ");
            stb.append("     NML012.NAME1 AS ENT, ");
            if("1".equals(param._testdiv)) {
                stb.append("     BD2_012.REMARK2 AS RECEPTNO2, ");
            }else{
                stb.append("     BD2_012.REMARK1 AS RECEPTNO2, ");
            }
            stb.append("     NML006_2.NAME2 || NML064.NAME1 AS CONSENT2, ");
            stb.append("     '' AS REMARK ");
            stb.append(" FROM ");
            stb.append("     V_ENTEXAM_APPLICANTBASE_TESTDIV_DAT V1");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT B1 ");
            stb.append("          ON B1.ENTEXAMYEAR  = V1.ENTEXAMYEAR ");
            stb.append("         AND B1.APPLICANTDIV = V1.APPLICANTDIV ");
            stb.append("         AND B1.EXAMNO       = V1.EXAMNO ");
            stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT R1  ");
            stb.append("          ON R1.ENTEXAMYEAR  = V1.ENTEXAMYEAR ");
            stb.append("         AND R1.APPLICANTDIV = V1.APPLICANTDIV ");
            stb.append("         AND R1.EXAMNO       = V1.EXAMNO ");
            stb.append("         AND R1.TESTDIV      = V1.TESTDIV ");
            stb.append("         AND R1.HONORDIV     = '1' ");
            stb.append("         AND R1.JUDGEDIV     <> '3' ");
            stb.append("         AND R1.JUDGEDIV     <> '4' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR1 ");
            stb.append("          ON ADDR1.ENTEXAMYEAR  = B1.ENTEXAMYEAR ");
            stb.append("         AND ADDR1.APPLICANTDIV = B1.APPLICANTDIV ");
            stb.append("         AND ADDR1.EXAMNO       = B1.EXAMNO ");
            stb.append("     LEFT JOIN NAME_MST NMZ002 ");
            stb.append("          ON NMZ002.NAMECD2 = B1.SEX ");
            stb.append("         AND NMZ002.NAMECD1 = 'Z002' ");
            stb.append("     LEFT JOIN FINSCHOOL_MST FINSCHOOL  ");
            stb.append("          ON FINSCHOOL.FINSCHOOLCD = B1.FS_CD ");
            stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT SCORE1  ");
            stb.append("          ON SCORE1.ENTEXAMYEAR    = R1.ENTEXAMYEAR ");
            stb.append("         AND SCORE1.APPLICANTDIV   = R1.APPLICANTDIV ");
            stb.append("         AND SCORE1.TESTDIV        = R1.TESTDIV ");
            stb.append("         AND SCORE1.EXAM_TYPE      = R1.EXAM_TYPE ");
            stb.append("         AND SCORE1.RECEPTNO       = R1.RECEPTNO ");
            stb.append("         AND SCORE1.TESTSUBCLASSCD = '1' ");
            stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT SCORE2 ");
            stb.append("          ON SCORE2.ENTEXAMYEAR    = R1.ENTEXAMYEAR ");
            stb.append("         AND SCORE2.APPLICANTDIV   = R1.APPLICANTDIV ");
            stb.append("         AND SCORE2.TESTDIV        = R1.TESTDIV ");
            stb.append("         AND SCORE2.EXAM_TYPE      = R1.EXAM_TYPE ");
            stb.append("         AND SCORE2.RECEPTNO       = R1.RECEPTNO ");
            stb.append("         AND SCORE2.TESTSUBCLASSCD = '2' ");
            stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT SCORE4 ");
            stb.append("          ON SCORE4.ENTEXAMYEAR    = R1.ENTEXAMYEAR ");
            stb.append("         AND SCORE4.APPLICANTDIV   = R1.APPLICANTDIV ");
            stb.append("         AND SCORE4.TESTDIV        = R1.TESTDIV ");
            stb.append("         AND SCORE4.EXAM_TYPE      = R1.EXAM_TYPE ");
            stb.append("         AND SCORE4.RECEPTNO       = R1.RECEPTNO ");
            stb.append("         AND SCORE4.TESTSUBCLASSCD = '4' ");
            stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT SCORE5 ");
            stb.append("          ON SCORE5.ENTEXAMYEAR    = R1.ENTEXAMYEAR ");
            stb.append("         AND SCORE5.APPLICANTDIV   = R1.APPLICANTDIV ");
            stb.append("         AND SCORE5.TESTDIV        = R1.TESTDIV ");
            stb.append("         AND SCORE5.EXAM_TYPE      = R1.EXAM_TYPE ");
            stb.append("         AND SCORE5.RECEPTNO       = R1.RECEPTNO ");
            stb.append("         AND SCORE5.TESTSUBCLASSCD = '5' ");
            stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT SCORE6 ");
            stb.append("          ON SCORE6.ENTEXAMYEAR    = R1.ENTEXAMYEAR ");
            stb.append("         AND SCORE6.APPLICANTDIV   = R1.APPLICANTDIV ");
            stb.append("         AND SCORE6.TESTDIV        = R1.TESTDIV ");
            stb.append("         AND SCORE6.EXAM_TYPE      = R1.EXAM_TYPE ");
            stb.append("         AND SCORE6.RECEPTNO       = R1.RECEPTNO ");
            stb.append("         AND SCORE6.TESTSUBCLASSCD = '6' ");
            stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT SCORE7 ");
            stb.append("          ON SCORE7.ENTEXAMYEAR    = R1.ENTEXAMYEAR ");
            stb.append("         AND SCORE7.APPLICANTDIV   = R1.APPLICANTDIV ");
            stb.append("         AND SCORE7.TESTDIV        = R1.TESTDIV ");
            stb.append("         AND SCORE7.EXAM_TYPE      = R1.EXAM_TYPE ");
            stb.append("         AND SCORE7.RECEPTNO       = R1.RECEPTNO ");
            stb.append("         AND SCORE7.TESTSUBCLASSCD = '7' ");
            stb.append("    LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD1_008 ");
            stb.append("          ON RD1_008.ENTEXAMYEAR  = R1.ENTEXAMYEAR ");
            stb.append("         AND RD1_008.APPLICANTDIV = R1.APPLICANTDIV ");
            stb.append("         AND RD1_008.TESTDIV      = R1.TESTDIV ");
            stb.append("         AND RD1_008.EXAM_TYPE    = R1.EXAM_TYPE ");
            stb.append("         AND RD1_008.RECEPTNO     = R1.RECEPTNO ");
            stb.append("         AND RD1_008.SEQ          = '008' ");
            stb.append("     LEFT JOIN NAME_MST NML013 ");
            stb.append("          ON NML013.NAMECD2 = R1.JUDGEDIV ");
            stb.append("         AND NML013.NAMECD1 = 'L013' ");
            stb.append("     LEFT JOIN NAME_MST NML011 ");
            stb.append("          ON NML011.NAMECD2 = B1.PROCEDUREDIV ");
            stb.append("         AND NML011.NAMECD1 = 'L011' ");
            stb.append("     LEFT JOIN NAME_MST NML012 ");
            stb.append("          ON NML012.NAMECD2 = B1.ENTDIV ");
            stb.append("         AND NML012.NAMECD1 = 'L012' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BD2_012 ");
            stb.append("          ON BD2_012.ENTEXAMYEAR  = B1.ENTEXAMYEAR ");
            stb.append("         AND BD2_012.APPLICANTDIV = B1.APPLICANTDIV ");
            stb.append("         AND BD2_012.EXAMNO       = B1.EXAMNO  ");
            stb.append("         AND BD2_012.SEQ          = '012'  ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BD2_013 ");
            stb.append("          ON BD2_013.ENTEXAMYEAR  = B1.ENTEXAMYEAR ");
            stb.append("         AND BD2_013.APPLICANTDIV = B1.APPLICANTDIV ");
            stb.append("         AND BD2_013.EXAMNO       = B1.EXAMNO ");
            stb.append("         AND BD2_013.SEQ          = '013' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BD2_014 ");
            stb.append("          ON BD2_014.ENTEXAMYEAR  = B1.ENTEXAMYEAR ");
            stb.append("         AND BD2_014.APPLICANTDIV = B1.APPLICANTDIV ");
            stb.append("         AND BD2_014.EXAMNO       = B1.EXAMNO ");
            stb.append("         AND BD2_014.SEQ          = '014' ");
            if("1".equals(param._testdiv)) {
                stb.append("     LEFT JOIN NAME_MST NML006_1 ");
                stb.append("          ON NML006_1.NAMECD2 = BD2_013.REMARK1 ");
                stb.append("         AND NML006_1.NAMECD1 = 'L006' ");
                stb.append("     LEFT JOIN NAME_MST NML006_2 ");
                stb.append("          ON NML006_2.NAMECD2 = BD2_013.REMARK2 ");
                stb.append("         AND NML006_2.NAMECD1 = 'L006' ");
                stb.append("     LEFT JOIN NAME_MST NML064 ");
                stb.append("          ON NML064.NAMECD2 = BD2_014.REMARK2 ");
                stb.append("         AND NML064.NAMECD1 = 'L064' ");
            }else {
                stb.append("     LEFT JOIN NAME_MST NML006_1 ");
                stb.append("          ON NML006_1.NAMECD2 = BD2_013.REMARK2 ");
                stb.append("         AND NML006_1.NAMECD1 = 'L006' ");
                stb.append("     LEFT JOIN NAME_MST NML006_2 ");
                stb.append("          ON NML006_2.NAMECD2 = BD2_013.REMARK1 ");
                stb.append("         AND NML006_2.NAMECD1 = 'L006' ");
                stb.append("     LEFT JOIN NAME_MST NML064 ");
                stb.append("          ON NML064.NAMECD2 = BD2_014.REMARK1 ");
                stb.append("         AND NML064.NAMECD1 = 'L064' ");
            }
            stb.append(" WHERE   ");
            stb.append("    V1.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("    AND V1.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("    AND V1.TESTDIV = '" + param._testdiv + "' ");
            if ("ALL".equals(param._judgediv)) {
                stb.append("    AND NML013.ABBV2 = '1' ");
            } else {
                stb.append("    AND R1.JUDGEDIV = '" + param._judgediv + "' ");
            }
            stb.append(" ORDER BY  ");
            stb.append("     R1.RECEPTNO ");

            return stb.toString();

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

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 65340 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _shdiv;
        final String _judgediv;
        final String _date;
        final String _prgid;
        final String _testdivName;
        final Map _subclsMap;
        final Map _hopeMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _shdiv = request.getParameter("SHDIV");
            _judgediv = request.getParameter("JUDGEDIV");
            _date = request.getParameter("CTRL_DATE");
            _prgid = request.getParameter("PRGID");

            if("2".equals(_applicantdiv)) {
                _testdivName = getNameMst(db2, "NAME1", "L024", _testdiv);
            }else {
            	_testdivName = "";
            }
            _subclsMap = getMapNameMst(db2, "NAME2", "L009");
            _hopeMap = getMapNameMst(db2, "NAME1", "L006");
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

        private Map getMapNameMst(final DB2UDB db2, final String field, final String namecd1) {
            Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT NAMECD2, " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn.put(rs.getString("NAMECD2"), rs.getString(field));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }
    }
}

// eof
