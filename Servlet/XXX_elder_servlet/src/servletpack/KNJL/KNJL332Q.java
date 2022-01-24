package servletpack.KNJL;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 *
 *    学校教育システム 賢者 [入試管理]
 *
 **/

public class KNJL332Q {

    private static final Log log = LogFactory.getLog(KNJL332Q.class);

    private boolean _hasData;
    private Param _param;

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        Vrw32alp svf     = new Vrw32alp();   //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2       = null;             //Databaseクラスを継承したクラス

        log.fatal("$Revision: 71567 $ $Date: 2020-01-07 10:28:42 +0900 (火, 07 1 2020) $"); // CVSキーワードの取り扱いに注意

        //print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

        //svf設定
        svf.VrInit();    //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream()); //PDFファイル名の設定

        //ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch (Exception ex) {
            log.error("DB2 open error!", ex);
            return;
        }

        try {
            KNJServletUtils.debugParam(request, log);
            _param = new Param(db2, request);

            printMain(db2, svf);

        } catch (Exception ex) {
            log.error("exception!", ex);
        } finally {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }

            //終了処理
            svf.VrQuit();
            db2.commit();
            db2.close();       //DBを閉じる
            outstrm.close();   //ストリームを閉じる
        }
    }

    private static String getString(final Map m, final String field) {
        if (null == m) {
            return null;
        }
        try {
            if (!m.containsKey(field)) {
                throw new IllegalArgumentException("not defined: " + field + " in " + m.keySet());
            }
        } catch (Exception e) {
            log.fatal("exception!", e);
        }
        return (String) m.get(field);
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

    /**
     * listを最大数ごとにグループ化したリストを得る
     * @param list
     * @param max 最大数
     * @return listを最大数ごとにグループ化したリスト
     */
    private static List getPageList(final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        String form = "";
        int maxLine = 999;

        if ("1".equals(_param._testdiv)) {
            maxLine = 25;
            form = "KNJL332Q_1.frm";
        } else if ("2".equals(_param._testdiv)) {
            maxLine = 20;
            form = "KNJL332Q_2.frm";
        } else if ("3".equals(_param._testdiv)) {
            maxLine = 25;
            form = "KNJL332Q_3.frm";
        } else {
            return;
        }
        final List finschoolListAll = Finschool.getFinschoolList(db2, _param);

        for (int fi = 0; fi < finschoolListAll.size(); fi++) {

            final Finschool finschool = (Finschool) finschoolListAll.get(fi);

            final List pageList = getPageList(finschool._applicantList, maxLine);

            for (int pi = 0; pi < pageList.size(); pi++) {

                final List dataList = (List) pageList.get(pi);

                svf.VrSetForm(form, 1);

                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._noticeDate)); // 日付
                svf.VrsOut("FINSCHHOL_NAME", StringUtils.defaultString(finschool._finschoolName) + "校長　殿"); // 出身学校名
                svf.VrsOut("SCHOOL_NAME", _param._schoolName); // 学校名
                svf.VrsOut("STAFF_NAME", _param._printPrincpalName); // 職員名
                if (null != _param._schoollogoStampFilePath) {
                    svf.VrsOut("SCHOOLSTAMP", _param._schoollogoStampFilePath);
                }

                svf.VrsOut("HOPE", String.valueOf(finschool._applicantList.size())); // 志願者数
                svf.VrsOut("PASS", finschool.passCount()); // 合格者数者
                svf.VrsOut("NOT_PASS", finschool.notPassCount()); // 不合格者数者

                if ("2".equals(_param._testdiv)) {
                    svf.VrsOut("EXAM_DATE", KNJ_EditDate.h_format_JP_MD(_param._ippanJisshiHiduke)); // 受験日
                    svf.VrsOut("EXAM_DATE2", KNJ_EditDate.h_format_JP_MD(_param._ippanJisshiHiduke)); // 受験日
                    svf.VrsOut("LIMIT_DATE", KNJ_EditDate.h_format_JP_MD(_param._teishutsuDate)); // 提出期限日
                } else if ("3".equals(_param._testdiv)) {
                    svf.VrsOut("NOTICE", finschool.kessekiCount()); // 欠席者者
                }

                if ("1".equals(_param._testdiv)) {
                    if ("2".equals(_param._applicantdiv) && "2".equals(_param._divoversea)) {
                        svf.VrsOut("TEXT", "早春の候、"); //テキスト本文の先頭
                    } else {
                        svf.VrsOut("TEXT", "晩秋の候、"); //テキスト本文の先頭
                    }
                }
                for (int i = 0; i < dataList.size(); i++) {
                    final int line = i + 1;
                    final Applicant appl = (Applicant) dataList.get(i);

                    svf.VrsOutn("NO", line, String.valueOf(maxLine * pi + i + 1)); // 連番
                    svf.VrsOutn("EXAM_NO", line, appl._examno); // 受験番号
                    if ("1".equals(_param._testdiv) && "2".equals(_param._applicantdiv)) {
                    	svf.VrsOutn("EXAM_DIV", line, appl._divdetailName); //入試区分(海外入試区分A/B)
                    } else {
                        svf.VrsOutn("EXAM_DIV", line, appl._testdivName1); // 入試区分
                    }
                    svf.VrsOutn("COURSE_NAME", line, appl._majorName); // 科名
                    svf.VrsOutn("GRD_DATE", line, appl.sotsugyoNentuki()); // 卒業年月
                    final int nlen = KNJ_EditEdit.getMS932ByteLength(appl._name);
                    final String nfield = nlen > 28 ? "4" : (nlen > 20 ? "3" : (nlen > 14 ? "2" : ""));
                    svf.VrsOutn("NAME" + nfield, line, appl._name); // 氏名
                    svf.VrsOutn("SEX", line, appl._sexName); // 性別
                    svf.VrsOutn("JUDGE", line, appl._judgementName); // 合否
                    svf.VrsOutn("PROCEDURE1", line, appl._scholarSaiyouName); // 手続き
                }

                svf.VrEndPage();
                _hasData = true;
            }
        }
    }

    private static class Applicant {
        String _testdiv;
        String _testdivName1;
        String _entCourse;
        String _entCouresName;
        String _majorName;
        String _examno;
        String _fsEracd;
        String _fsEracdnName1;
        String _fsY;
        String _fsM;
        String _name;
        String _sex;
        String _sexName;
        String _judgement;
        String _judgementName;
        String _scholarSaiyou;
        String _scholarSaiyouName;
        String _divdetailName;

        public String sotsugyoNentuki() {
            final String nen = NumberUtils.isDigits(_fsY) ? String.valueOf(Integer.parseInt(_fsY)) : StringUtils.defaultString(_fsY);
            final String tuki = NumberUtils.isDigits(_fsM) ? String.valueOf(Integer.parseInt(_fsM)) : StringUtils.defaultString(_fsM);
            return StringUtils.defaultString(_fsEracdnName1) + nen + "年" + tuki + "月";
        }
    }

    private static class Finschool {
        final String _fsCd;
        final String _finschoolName;
        final List _applicantList = new ArrayList();

        Finschool(
            final String fsCd,
            final String finschoolName
        ) {
            _fsCd = fsCd;
            _finschoolName = finschoolName;
        }

        public String passCount() {
            int count = 0;
            for (final Iterator it = _applicantList.iterator(); it.hasNext();) {
                final Applicant appl = (Applicant) it.next();
                if ("1".equals(appl._judgement)) {
                    count += 1;
                }
            }
            return String.valueOf(count);
        }

        public String notPassCount() {
            int count = 0;
            for (final Iterator it = _applicantList.iterator(); it.hasNext();) {
                final Applicant appl = (Applicant) it.next();
                if (null != appl._judgement && !"1".equals(appl._judgement) && !"4".equals(appl._judgement)) {
                    count += 1;
                }
            }
            return String.valueOf(count);
        }

        public String kessekiCount() {
            int count = 0;
            for (final Iterator it = _applicantList.iterator(); it.hasNext();) {
                final Applicant appl = (Applicant) it.next();
                if ("4".equals(appl._judgement)) {
                    count += 1;
                }
            }
            return String.valueOf(count);
        }

        public static List getFinschoolList(final DB2UDB db2, final Param param) {
            final List finschoollist = new ArrayList();
            final Map finschoolMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String fsCd = rs.getString("FS_CD");
                    if (null == finschoolMap.get(fsCd)) {
                        final String finschoolName = rs.getString("FINSCHOOL_NAME");
                        final Finschool finschool = new Finschool(fsCd, finschoolName);
                        finschoollist.add(finschool);
                        finschoolMap.put(fsCd, finschool);
                    }
                    final Finschool finschool = (Finschool) finschoolMap.get(fsCd);

                    final Applicant appl = new Applicant();
                    appl._testdiv = rs.getString("TESTDIV");
                    appl._testdivName1 = rs.getString("TESTDIV_NAME1");
                    appl._entCourse = rs.getString("ENT_COURSE");
                    appl._entCouresName = rs.getString("ENT_COURES_NAME");
                    appl._majorName = rs.getString("MAJORNAME");
                    appl._examno = rs.getString("EXAMNO");
                    appl._fsEracd = rs.getString("FS_ERACD");
                    appl._fsEracdnName1 = rs.getString("FS_ERACD_NAME1");
                    appl._fsY = rs.getString("FS_Y");
                    appl._fsM = rs.getString("FS_M");
                    appl._name = rs.getString("NAME");
                    appl._sex = rs.getString("SEX");
                    appl._sexName = rs.getString("SEX_NAME");
                    appl._judgement = rs.getString("JUDGEMENT");
                    appl._judgementName = rs.getString("JUDGEMENT_NAME");
                    appl._scholarSaiyou = rs.getString("SCHOLAR_SAIYOU");
                    appl._scholarSaiyouName = rs.getString("SCHOLAR_SAIYOU_NAME");
                    appl._divdetailName = rs.getString("DIVDETAIL");
                    finschool._applicantList.add(appl);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return finschoollist;
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT  ");
            stb.append("      B1.FS_CD,  ");
            stb.append("      TFIN.FINSCHOOL_NAME, ");
            stb.append("      B1.TESTDIV,  ");
            stb.append("      N1.NAME1 AS TESTDIV_NAME1,  ");
            stb.append("      B1.ENT_COURSECD || B1.ENT_MAJORCD || B1.ENT_COURSECODE AS ENT_COURSE,  ");
            stb.append("      C1.EXAMCOURSE_NAME AS ENT_COURES_NAME, ");
            stb.append("      M1.MAJORNAME, ");
            stb.append("      B1.EXAMNO, ");
            stb.append("      B1.FS_ERACD, ");
            stb.append("      B1.FS_ERACD, ");
            stb.append("      L007.NAME1 AS FS_ERACD_NAME1, ");
            stb.append("      B1.FS_Y, ");
            stb.append("      B1.FS_M, ");
            stb.append("      B1.NAME, ");
            stb.append("      B1.SEX, ");
            stb.append("      Z002.NAME2 AS SEX_NAME, ");
            stb.append("      B1.JUDGEMENT, ");
            stb.append("      L013.NAME1 AS JUDGEMENT_NAME, ");
            stb.append("      B1.SCHOLAR_SAIYOU, ");
            stb.append("      L004.NAME1 AS DIVDETAIL, ");
            stb.append("      CASE WHEN B1.SCHOLAR_SAIYOU = '1' THEN '採用'  ");
            stb.append("         WHEN B1.SCHOLAR_SAIYOU = '2' THEN '不採用' ");
            stb.append("         ELSE '-' ");
            stb.append("      END AS SCHOLAR_SAIYOU_NAME ");
            stb.append("  FROM  ");
            stb.append("      V_ENTEXAM_APPLICANTBASE_DAT B1  ");
            stb.append("      LEFT JOIN ENTEXAM_APPLICANTADDR_DAT A1 ON A1.ENTEXAMYEAR = B1.ENTEXAMYEAR  ");
            stb.append("              AND A1.APPLICANTDIV = B1.APPLICANTDIV ");
            stb.append("              AND A1.EXAMNO = B1.EXAMNO  ");
            stb.append("      LEFT JOIN V_NAME_MST L004 ON L004.YEAR = B1.ENTEXAMYEAR AND L004.NAMECD1 = 'L004' ");
            stb.append("      AND B1.TESTDIV = L004.NAMECD2 ");
            stb.append("      LEFT JOIN V_NAME_MST N1 ON N1.YEAR = B1.ENTEXAMYEAR AND N1.NAMECD1 = 'L045' AND N1.NAMECD2 = B1.TESTDIV0  ");
            stb.append("      LEFT JOIN V_NAME_MST L007 ON L007.YEAR = B1.ENTEXAMYEAR AND L007.NAMECD1 = 'L007' AND L007.NAMECD2 = B1.FS_ERACD ");
            stb.append("      LEFT JOIN V_NAME_MST L013 ON L013.YEAR = B1.ENTEXAMYEAR AND L013.NAMECD1 = 'L013' AND L013.NAMECD2 = B1.JUDGEMENT  ");
            stb.append("      LEFT JOIN V_NAME_MST Z002 ON Z002.YEAR = B1.ENTEXAMYEAR AND Z002.NAMECD1 = 'Z002' AND Z002.NAMECD2 = B1.SEX  ");
            stb.append("      LEFT JOIN MAJOR_MST M1 ON M1.COURSECD = B1.DAI1_COURSECD AND M1.MAJORCD = B1.DAI1_MAJORCD ");
            stb.append("      LEFT JOIN ENTEXAM_COURSE_MST C1 ON C1.ENTEXAMYEAR = B1.ENTEXAMYEAR  ");
            stb.append("              AND C1.APPLICANTDIV = B1.APPLICANTDIV  ");
            stb.append("              AND C1.TESTDIV = B1.TESTDIV  ");
            stb.append("              AND C1.COURSECD = B1.ENT_COURSECD  ");
            stb.append("              AND C1.MAJORCD = B1.ENT_MAJORCD  ");
            stb.append("              AND C1.EXAMCOURSECD = B1.ENT_COURSECODE  ");
            stb.append("      INNER JOIN FINSCHOOL_MST TFIN ON TFIN.FINSCHOOLCD = B1.FS_CD ");
            stb.append("  WHERE  ");
            stb.append("      B1.ENTEXAMYEAR = '" + param._entexamyear + "'  ");
            stb.append("      AND B1.APPLICANTDIV = '" + param._applicantdiv + "'  ");
            stb.append("      AND B1.TESTDIV0 = '" + param._testdiv + "'  ");
            if ("1".equals(param._testdiv) && "2".equals(param._applicantdiv)) {
                stb.append("      AND B1.TESTDIV = '" + param._divoversea + "' ");
            }
            stb.append("  ORDER BY  ");
            stb.append("      B1.FS_CD ");
            stb.append("      , B1.EXAMNO  ");
            return stb.toString();
        }
    }

    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _loginDate;
        final String _noticeDate;
        final String _teishutsuDate;
        final boolean _seirekiFlg;
        final String _ippanJisshiHiduke;

        private String _principalName;
        private String _jobName;
        private String _schoolName;
        private String _printPrincpalName;
        final String _documentroot;
        final String _imagepath;
        final String _schoollogoStampFilePath;
        final String _divoversea;

        Param(
                final DB2UDB db2,
                final HttpServletRequest request
        ) {
            _entexamyear  = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            if ("1".equals(_testdiv)) {
                _divoversea = request.getParameter("DIVOVERSEA");
            } else {
                _divoversea = "";
            }
            _loginDate    = request.getParameter("LOGIN_DATE");
            _noticeDate   = request.getParameter("NOTICEDATE");
            _teishutsuDate = request.getParameter("TEISHUTSUDATE");
            _seirekiFlg = getSeirekiFlg(db2);
            _ippanJisshiHiduke = getIppanJisshiHiduke(db2);
            _documentroot = request.getParameter("DOCUMENTROOT");
            _imagepath = request.getParameter("IMAGEPATH");
            _schoollogoStampFilePath = getImageFilePath("SCHOOLSTAMP_H.bmp");
            setCertifSchoolDat(db2);
        }

        /* 西暦表示にするのかのフラグ  */
        private boolean getSeirekiFlg(final DB2UDB db2) {
            final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z012' AND NAMECD2 = '00' AND NAME1 IS NOT NULL ";
            boolean seirekiFlg = false;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (rs.getString("NAME1").equals("2")) seirekiFlg = true; //西暦
                }
            } catch (Exception e) {
                log.error("getSeirekiFlg Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return seirekiFlg;
        }

        /* 一般の実施日付  */
        private String getIppanJisshiHiduke(final DB2UDB db2) {
            final String sql = "SELECT NAMESPARE1 FROM NAME_MST WHERE NAMECD1 = 'L004' AND NAMECD2 = '5' ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            String rtn = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn = rs.getString("NAMESPARE1");
                    if (null != rtn) {
                        rtn = StringUtils.replace(rtn, "/", "-");
                    }
                }
            } catch (Exception e) {
                log.error("exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private String gethiduke(final String inputDate) {
            // 西暦か和暦はフラグで判断
            String date;
            if (null != inputDate) {
                if (_seirekiFlg) {
                    date = inputDate.toString().substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(inputDate);
                } else {
                    date = KNJ_EditDate.h_format_JP(inputDate);
                }
                return date;
            }
            return null;
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            final String certifKindCd;
//            if (_applicantdiv.equals(APPLICANTDIV1)) {
//                certifKindCd = "105";
//            } else {
                certifKindCd = "106";
//            }

            final String sql = "SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _entexamyear + "' AND CERTIF_KINDCD = '" + certifKindCd + "' ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _principalName = rs.getString("PRINCIPAL_NAME");
                    _jobName = rs.getString("JOB_NAME");
                    _schoolName = rs.getString("SCHOOL_NAME");
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            _jobName = trim(_jobName);
            _principalName = trim(_principalName);
            _printPrincpalName = (StringUtils.isEmpty(_jobName) ? "" : StringUtils.defaultString(_jobName) + "　") + StringUtils.defaultString(_principalName);
        }

        private static String trim(final String s) {
            if (null == s) {
                return s;
            }
            int st = 0, ed = s.length();
            for (int i = 0; i < s.length(); i++) {
                final char ch = s.charAt(i);
                if (ch == ' ' || ch == '　') {
                    st = i + 1;
                } else {
                    break;
                }
            }
            for (int i = s.length() - 1; i >= 0; i--) {
                final char ch = s.charAt(i);
                if (ch == ' ' || ch == '　') {
                    ed = i;
                } else {
                    break;
                }
            }
            if (st < ed) {
                return s.substring(st, ed);
            }
            return s;
        }

        /**
         * 写真データファイルの取得
         */
        private String getImageFilePath(final String filename) {
            if (null == _documentroot || null == _imagepath || null == filename) {
                return null;
            } // DOCUMENTROOT
            final StringBuffer path = new StringBuffer();
            path.append(_documentroot).append("/").append(_imagepath).append("/").append(filename);
            final File file = new File(path.toString());
            if (!file.exists()) {
                log.warn("画像ファイル無し:" + path);
                return null;
            } // 写真データ存在チェック用
            return path.toString();
        }
    }
}//クラスの括り
