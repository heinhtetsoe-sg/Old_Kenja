package servletpack.KNJL;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３４４Ｃ＞  入試結果の報告（送付）
 **/

public class KNJL344C {

    private static final Log log = LogFactory.getLog(KNJL344C.class);
    private Param _param ;
    private boolean nonedata = false;                               //該当データなしフラグ

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {
        log.debug("$Id: 2fb54eb301c9f450485267790b283a4090a12efc $");

        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス

        //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!");
            return;
        }
        _param = new Param(db2, request);

        //  print設定
        response.setContentType("application/pdf");

        //  svf設定
        svf.VrInit();                             //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定

        //SQL作成
        try {
            //SVF出力
            printSvf(db2, svf);  //帳票出力のメソッド
        } catch (Exception ex) {
            log.error("DB2 prepareStatement set error!", ex);
        } finally {
            //  該当データ無し
            if( !nonedata ){
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }

            //  終了処理
            svf.VrQuit();
            db2.commit();
            db2.close();                //DBを閉じる
        }

    }//doGetの括り

    private String getDateString(final String dateFormat) {
        if (null == dateFormat) {
            return null;
        }
        return _param._seirekiFlg ? dateFormat.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(dateFormat) : KNJ_EditDate.h_format_JP(dateFormat);
    }

    /**帳票出力**/
    private void printSvf(
        final DB2UDB db2,
        final Vrw32alp svf
    ) {
        final List finschoolList = getFinschoolList(db2);

        for (final Iterator it = finschoolList.iterator(); it.hasNext();) {

            final Finschool finschool = (Finschool) it.next();

            if ("1".equals(_param._output)) {
                printSvfStudent1(svf, "KNJL344C_1G.frm", finschool);
            } else if ("2".equals(_param._output)) {
                printSvfStudent2(svf, "KNJL344C_2G.frm", finschool);
            }
        }
    }

    private void printHeader(final Vrw32alp svf, final Finschool finschool) {
        svf.VrsOut("DATE", getDateString(_param._printDate));
        svf.VrsOut("SCHOOLNAME", _param._schoolName);
        svf.VrsOut("JOBNAME", _param._jobName);
        svf.VrsOut("STAFFNAME", _param._principalName);
        svf.VrsOut("ADDRESS", _param._address);
        svf.VrsOut("ZIPCD", _param._zipcd);
        svf.VrsOut("NENDO", _param.getNendo());

        svf.VrsOut("F_ZIPCD", finschool._finschoolZipcd); //出身学校郵便番号
        svf.VrsOut("F_ADDRESS", finschool._finschoolAddr); //出身学校住所
        svf.VrsOut("DISTRICT", finschool._finschoolDistname);  //出身学校地区名
        svf.VrsOut("F_SCHOOLNAME", finschool._finschoolName); //出身学校名
    }

    private void printSvfStudent2(final Vrw32alp svf, final String form, final Finschool finschool) {
        final int size1 = 12;
        final int size2 = 8;
        final int totalpage1 = finschool._list1.size() / size1 + (finschool._list1.size() % size1 == 0 ? 0 : 1);
        final int totalpage2 = finschool._list2.size() / size2 + (finschool._list2.size() % size2 == 0 ? 0 : 1);
        for (int page = 1; page <= Math.max(totalpage1, totalpage2); page++) {

            svf.VrSetForm(form, 1);

            printHeader(svf, finschool);

            // 編入
            final List sub1 = getStudentSubList(finschool._list1, page, size1);
            for (int i = 0; i < sub1.size(); i++) {
                final Student s = (Student) sub1.get(i);

                printStudent2(svf, "1", i, s);
            }
            // スポーツ
            final List sub2 = getStudentSubList(finschool._list2, page, size2);
            for (int i = 0; i < sub2.size(); i++) {
                final Student s = (Student) sub2.get(i);

                printStudent2(svf, "2", i, s);
            }
            svf.VrEndPage();
            nonedata = true;
        }
    }

    private void printStudent2(final Vrw32alp svf, final String sf, final int i, final Student s) {
        final int j = i + 1;
        svf.VrsOutn("EXAMNO" + sf, j, s._examno);       //受験番号

        final String fName = "NAME" + sf + (s._name.length() > 20 ? "_3" : s._name.length() > 10 ? "_2" : "_1");
        svf.VrsOutn(fName, j, s._name);
        svf.VrsOutn("SEX" + sf, j, s._sexname);     //性別

        if (!"2".equals(s._shdiv)) {
            // 専願
            svf.VrsOutn("DIV" + sf + "_1", j, "○");
        } else if ("2".equals(s._shdiv)) {
            // 併願
            svf.VrsOutn("DIV" + sf + "_2", j, "○");
        }

        // 合否
        svf.VrsOutn("HOPE" + sf + "_1", j, s._judgementabbv1);
    }

    private void printSvfStudent1(final Vrw32alp svf, final String form, final Finschool finschool) {
        final int size1 = 10;
        final int totalpage1 = finschool._list1.size() / size1 + (finschool._list1.size() % size1 == 0 ? 0 : 1);
        for (int page = 1; page <= totalpage1; page++) {

            svf.VrSetForm(form, 1);

            printHeader(svf, finschool);

            final List sub1 = getStudentSubList(finschool._list1, page, size1);
            for (int i = 0; i < sub1.size(); i++) {
                final Student s = (Student) sub1.get(i);

                svf.VrsOutn("EXAMNO", i + 1, s._examno);       //受験番号

                final String fName = "NAME" + (s._name.length() > 20 ? "3" : s._name.length() > 10 ? "2" : "1");
                svf.VrsOutn(fName, i + 1, s._name);
                svf.VrsOutn("SEX", i + 1, s._judgementabbv1);
            }
            svf.VrEndPage();
            nonedata = true;
        }
    }

    private List getStudentSubList(final List list, final int page, final int size) {
        final int from = (page - 1) * size;
        final int to = Math.min(list.size(), page * size);
        return list.size() <= from ? Collections.EMPTY_LIST : list.subList(from, to);
    }

    private List getFinschoolList(final DB2UDB db2) {

        final String sql = getEntSql();
        log.debug("sql=" + sql);
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List finschoolList = new ArrayList();
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                Finschool finschool = null;
                for (final Iterator it = finschoolList.iterator(); it.hasNext();) {
                    final Finschool f = (Finschool) it.next();
                    if (f._fsCd != null && f._fsCd.equals(rs.getString("FS_CD"))) {
                        finschool = f;
                        break;
                    }
                }

                if (null == finschool) {
                    finschool = new Finschool(rs.getString("FS_CD"));
                    final String addr1 = null == rs.getString("FINSCHOOL_ADDR1") ? "" : rs.getString("FINSCHOOL_ADDR1");
                    final String addr2 = null == rs.getString("FINSCHOOL_ADDR2") ? "" : " " + rs.getString("FINSCHOOL_ADDR2");
                    finschool._finschoolZipcd = rs.getString("FINSCHOOL_ZIPCD");
                    finschool._finschoolAddr = addr1 + addr2;
                    finschool._finschoolDistname = rs.getString("FINSCHOOL_DISTNAME");
                    finschool._finschoolName = rs.getString("FINSCHOOL_NAME");
                    finschoolList.add(finschool);
                }

                //明細
                final Student student = new Student(rs.getString("EXAMNO"));
                student._name = rs.getString("NAME");
                student._sexname = rs.getString("SEX_NAME");
                student._testdiv = rs.getString("TESTDIV");
                student._judgediv = rs.getString("JUDGEDIV");
                student._judgedivabbv1 = rs.getString("JUDGEDIV_ABBV1");
                student._judgementabbv1 = rs.getString("JUDGEMENT_ABBV1");
                student._shdiv = rs.getString("SHDIV");
                student._slideflg = rs.getString("SLIDE_FLG");
                student._fugoukakuabbv1 = rs.getString("FUGOUKAKU_ABBV1");
                if ("2".equals(_param._output)) {
                    if ("8".equals(rs.getString("TESTDIV"))) { // スポーツ
                        finschool._list2.add(student);
                    } else if ("3".equals(rs.getString("TESTDIV"))){ // 編入
                        finschool._list1.add(student);
                    }
                } else if ("1".equals(_param._output)) {
                    finschool._list1.add(student);
                }
            }
        } catch (Exception ex) {
            log.error("setSvfout set error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return finschoolList;
    }

    /**入学予定者を取得**/
    private String getEntSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" select ");
        stb.append("     t1.FS_CD, ");
        stb.append("     t2.FINSCHOOL_ZIPCD, ");
        stb.append("     t2.FINSCHOOL_ADDR1, ");
        stb.append("     t2.FINSCHOOL_ADDR2, ");
        stb.append("     t2.FINSCHOOL_NAME, ");
        stb.append("     t2.FINSCHOOL_DISTCD, ");
        stb.append("     t4.NAME1 AS FINSCHOOL_DISTNAME, ");
        stb.append("     t1.EXAMNO, ");
        stb.append("     t1.NAME, ");
        stb.append("     t1.SEX, ");
        stb.append("     t1.SHDIV, ");
        stb.append("     t1.SLIDE_FLG, ");
        stb.append("     t3.ABBV1 AS SEX_NAME, ");
        stb.append("     t1.TESTDIV, ");
        stb.append("     t1.JUDGEMENT, ");
        stb.append("     t5.ABBV1 AS JUDGEMENT_ABBV1, ");
        stb.append("     t6.JUDGEDIV, ");
        stb.append("     t7.ABBV1 AS JUDGEDIV_ABBV1, ");
        stb.append("     t9.ABBV1 AS FUGOUKAKU_ABBV1 ");
        stb.append(" from ENTEXAM_APPLICANTBASE_DAT t1 ");
        stb.append("     left join FINSCHOOL_MST t2 on ");
        stb.append("         t1.FS_CD = t2.FINSCHOOLCD ");
        stb.append("     left join NAME_MST t3 on ");
        stb.append("         t3.NAMECD1 = 'Z002' and ");
        stb.append("         t3.NAMECD2 = t1.SEX ");
        stb.append("     left join NAME_MST t4 on ");
        stb.append("         t4.NAMECD1 = 'L001' and ");
        stb.append("         t4.NAMECD2 = t2.FINSCHOOL_DISTCD ");
        stb.append("     left join NAME_MST t5 on ");
        stb.append("         t5.NAMECD1 = 'L013' and ");
        stb.append("         t5.NAMECD2 = t1.JUDGEMENT ");
        stb.append("     left join ENTEXAM_RECEPT_DAT t6 on ");
        stb.append("         t6.ENTEXAMYEAR = t1.ENTEXAMYEAR and ");
        stb.append("         t6.APPLICANTDIV = t1.APPLICANTDIV and ");
        stb.append("         t6.TESTDIV = t1.TESTDIV and ");
        stb.append("         t6.EXAMNO = t1.EXAMNO ");
        stb.append("     left join NAME_MST t7 on ");
        stb.append("         t7.NAMECD1 = 'L013' and ");
        stb.append("         t7.NAMECD2 = t6.JUDGEDIV ");
        stb.append("     left join V_NAME_MST t9 on ");
        stb.append("         t9.YEAR = t1.ENTEXAMYEAR and ");
        stb.append("         t9.NAMECD1 = 'L013' and ");
        stb.append("         t9.NAMECD2 = '2' ");//不合
        stb.append(" where ");
        stb.append("     t1.ENTEXAMYEAR = '" + _param._year + "' and ");
        stb.append("     t1.APPLICANTDIV = '" + _param._applicantDiv + "' and ");
        stb.append("     t1.TESTDIV in ('3', '8') ");
        if ("1".equals(_param._output)) {
            stb.append("     and (t1.TESTDIV = '3' and t1.SHDIV = '1') ");
        } else if ("2".equals(_param._output)) {
//            stb.append("     and not ");
//            stb.append("     (t1.TESTDIV = '3' and t1.SHDIV = '1') ");
        }

        stb.append(" order by ");
        stb.append("     t1.FS_CD, t1.EXAMNO ");
        return stb.toString();
    }

    static class Finschool {
        final String _fsCd;
        String _finschoolZipcd;
        String _finschoolAddr;
        String _finschoolDistname;
        String _finschoolName;
        final List _list1;
        final List _list2;
        Finschool(final String fsCd) {
            _fsCd = fsCd;
            _list1 = new ArrayList();
            _list2 = new ArrayList();
        }
    }

    static class Student {
        final String _examno;
        String _name;
        String _sexname;
        String _testdiv;
        String _judgediv;
        String _judgedivabbv1;
        String _judgementabbv1;
        String _shdiv;
        String _slideflg;
        String _fugoukakuabbv1;
        Student(final String examno) {
            _examno = examno;
        }
    }

    static class Param {
        final String _year;
        final String _applicantDiv; // '2': 高校のみ
        final String _loginDate;
        final String _output;
        final String _printDate;

        final boolean _seirekiFlg;
        private String _schoolName;
        private String _jobName;
        private String _principalName;
        private String _address;
        private String _zipcd;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");                        //年度
            _applicantDiv = request.getParameter("APPLICANTDIV");                //入試制度
            _loginDate = request.getParameter("LOGIN_DATE");                  // ログイン日付
            _output = request.getParameter("OUTPUT");
            _printDate = request.getParameter("PRINT_DATE");
            // 西暦使用フラグ
            _seirekiFlg = getSeirekiFlg(db2);
            setSchoolStatus(db2);
        }

        String getNendo() {
            return _seirekiFlg ? _year + "年度" : KNJ_EditDate.h_format_JP_N(_year+"-01-01") + "度";
        }

        private boolean getSeirekiFlg(DB2UDB db2) {
            boolean seirekiFlg = false;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z012' AND NAMECD2 = '00' AND NAME1 IS NOT NULL ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if ("2".equals(rs.getString("NAME1"))) {
                        seirekiFlg = true; //西暦
                    }
                }
            } catch(SQLException ex) {
                log.error("exception!", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return seirekiFlg;
        }

        private String getNameMst(final DB2UDB db2, final String namecd1, final String namecd2) {
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT NAME1 ");
            sql.append(" FROM NAME_MST ");
            sql.append(" WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
            String name = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try{
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                   name = rs.getString("NAME1");
                }
            } catch(SQLException ex) {
                log.error("exception!", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return name;
        }

        private void setSchoolStatus(final DB2UDB db2) {
            String certifKindCd = null;
            if ("1".equals(_applicantDiv)) certifKindCd = "111";
            if ("2".equals(_applicantDiv)) certifKindCd = "112";
            if (null == certifKindCd) {
                return ;
            }
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK1 AS ADDRESS, REMARK2 AS ZIPCD ");
            sql.append(" FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '" + certifKindCd + "' ");
            PreparedStatement ps = null;
            ResultSet rs = null;
            try{
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    _schoolName = rs.getString("SCHOOL_NAME");
                    _jobName = rs.getString("JOB_NAME");
                    _principalName = rs.getString("PRINCIPAL_NAME");
                    _address = rs.getString("ADDRESS");
                    _zipcd = rs.getString("ZIPCD");
                }
            } catch(SQLException ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }
}//クラスの括り
