package servletpack.KNJL;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

/**
 *
 *    学校教育システム 賢者 [入試管理]
 *
 **/

public class KNJL329Q {

    private static final Log log = LogFactory.getLog(KNJL329Q.class);

    private boolean _hasData;
    private Param _param;

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        Vrw32alp svf     = new Vrw32alp();   //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2       = null;             //Databaseクラスを継承したクラス

        log.fatal("$Revision: 57087 $ $Date: 2017-11-14 14:40:57 +0900 (火, 14 11 2017) $"); // CVSキーワードの取り扱いに注意

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final List applicantList = Applicant.getApplicantList(db2, _param);

        final String form = "KNJL329Q.frm";

        for (int i = 0; i < applicantList.size(); i++) {

            final Applicant appl = (Applicant) applicantList.get(i);
            svf.VrSetForm(form, 1);

            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._noticeDate)); // 日付
            svf.VrsOut("EXAM_NO", appl._examno); // 受験番号
            svf.VrsOut("NAME", appl._name); // 氏名
            svf.VrsOut("SCHOOL_NAME", _param._schoolName); // 学校名
            svf.VrsOut("STAFF_NAME", _param._jobName + _param._principalName); // 職員名
            if (null != _param._schoollogoStampFilePath) {
                svf.VrsOut("SCHOOLSTAMP", _param._schoollogoStampFilePath);//校長印
            }
            svf.VrsOut("COURSE_NAME", StringUtils.defaultString(appl._majorName)); // 学科名
            svf.VrsOut("EXAM_DATE", KNJ_EditDate.h_format_S(appl._testDate1, "M月d日") + "(" + KNJ_EditDate.h_format_W(appl._testDate1) + ")"); // 入試日

            svf.VrEndPage();
            _hasData = true;
        }
    }

    private static class Applicant {
        String _examno;
        String _name;
        String _testDate1;
        String _majorName;
        String _examcourseName;

        public static List getApplicantList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Applicant applicant = new Applicant();
                    applicant._examno = rs.getString("EXAMNO");
                    applicant._name = rs.getString("NAME");
                    applicant._testDate1 = rs.getString("TESTDATE1");
                    applicant._majorName = rs.getString("MAJORNAME");
                    applicant._examcourseName = rs.getString("EXAMCOURSE_NAME");
                    list.add(applicant);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     B1.EXAMNO, ");
            stb.append("     B1.NAME, ");
            stb.append("     L004.NAMESPARE1 AS TESTDATE1, ");
            stb.append("     M1.MAJORNAME, ");
            stb.append("     C1.EXAMCOURSE_NAME ");
            stb.append(" FROM ");
            stb.append("     V_ENTEXAM_APPLICANTBASE_DAT B1 ");
            stb.append("     LEFT JOIN MAJOR_MST M1 ");
            stb.append("           ON M1.COURSECD = B1.DAI1_COURSECD ");
            stb.append("          AND M1.MAJORCD = B1.DAI1_MAJORCD ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST C1 ON C1.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
            stb.append("            AND C1.APPLICANTDIV = B1.APPLICANTDIV ");
            stb.append("            AND C1.TESTDIV = B1.TESTDIV ");
            stb.append("            AND C1.COURSECD = B1.DAI1_COURSECD ");
            stb.append("            AND C1.MAJORCD = B1.DAI1_MAJORCD ");
            stb.append("            AND C1.EXAMCOURSECD = B1.DAI1_COURSECODE ");
            stb.append("     LEFT JOIN NAME_MST L004 ON L004.NAMECD1 = 'L004' ");
            stb.append("          AND B1.TESTDIV = L004.NAMECD2 ");
            stb.append(" WHERE ");
            stb.append("     B1.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND B1.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("     AND B1.TESTDIV0 = '" + param._testDiv + "' ");
            stb.append("     AND B1.JUDGEMENT = '4' "); // 欠席
            stb.append(" ORDER BY ");
            stb.append("     B1.EXAMNO ");
            return stb.toString();
        }
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

    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testDiv;
        final String _loginDate;
        final String _noticeDate;
        final String _testDateString;

        private String _principalName;
        private String _jobName;
        private String _schoolName;
        private String _printPrincpalName;

        final String _documentroot;
        final String _imagepath;
        final String _schoollogoStampFilePath;
        Param(
                final DB2UDB db2,
                final HttpServletRequest request
        ) {
            _entexamyear  = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _loginDate    = request.getParameter("LOGIN_DATE");
            _noticeDate    = request.getParameter("NOTICEDATE");
            _testDateString = getTestdate(db2);
            _documentroot = request.getParameter("DOCUMENTROOT");
            _imagepath = request.getParameter("IMAGEPATH");
            _schoollogoStampFilePath = getImageFilePath("SCHOOLSTAMP_H.bmp");
            setCertifSchoolDat(db2);
        }

        /* 入試区分 */
        private String getTestdate(final DB2UDB db2) {
            String sql = "";
            sql += " SELECT T1.*, DAYOFWEEK(DATE(REPLACE(T1.NAMESPARE1, '/', '-'))) AS YOUBI_ID FROM V_NAME_MST T1 ";
            sql += " WHERE YEAR = '" + _entexamyear + "' ";
            sql += "   AND NAMECD1 = 'L004' ";
            sql += "   AND NAMECD2 = '5' "; // 一般・基準試験
            PreparedStatement ps = null;
            ResultSet rs = null;
            String dateString = null;
            try {
                log.debug(" testdiv sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (null != rs.getString("NAMESPARE1")) {
                        try {
                            dateString = KNJ_EditDate.h_format_JP_MD(rs.getString("NAMESPARE1"));
                        } catch (Exception e) {
                            log.fatal("exception!", e);
                        }
                    }
                    final String youbiId = rs.getString("YOUBI_ID");
                    if (NumberUtils.isDigits(youbiId)) {
                        final String youbi = new String[] {"", "日", "月", "火", "水", "木", "金", "土"}[Integer.parseInt(youbiId)];
                        dateString = StringUtils.defaultString(dateString) + "(" + youbi + ")";
                    }
                }
            } catch (Exception e) {
                log.error("exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return dateString;
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
            _schoolName = StringUtils.defaultString(_schoolName);
            _jobName = StringUtils.defaultString(_jobName, "校長");
            _principalName = StringUtils.defaultString(_principalName);
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
