package servletpack.KNJWD;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 個人別通学スクーリング明細
 * @author nakasone
 * $Id: 90800405e5c4040de42484b37b766504c0bcf64b $
 */
public class KNJWD710 {
    private static final String FORM_FILE = "KNJWD710.frm";
    private static final int PAGE_MAX_LINE = 50;

    private boolean _hasData;
    private Form _form;
    private Vrw32alp _svf;
    private DB2UDB db2;
    Param _param;

    private static final Log log = LogFactory.getLog(KNJWD710.class);

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        dumpParam(request);
        _param = new Param(db2, request);

        _form = new Form(FORM_FILE, response);

        db2 = null;
        try {
            final String dbName = request.getParameter("DBNAME");
            db2 = new DB2UDB(dbName, "db2inst1", "db2inst1", DB2UDB.TYPE2);
            if (openDb(db2)) {
                return;
            }

            printMain(db2);
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            _form.closeSvf();
            closeDb(db2);
        }
    }

    /**
     * 帳票出力処理メイン
     * @param db2
     * @throws Exception
     */
    private void printMain(final DB2UDB db2) throws Exception {

        // 指示画面にて選択された科目分の処理を行う
        for (int i = 0; i < _param._subclass.length; i++) {
            String subclass = _param._subclass[i];

            // 指示画面より取得した科目を分割する(科目とは、教科CD(2桁)+教育課程CD(1桁)+科目CD(6桁))
            String[] subclassAry = StringUtils.split(subclass,"-");
            String classCd = subclassAry[0];        // 教科CD                 
            String curriculumCd = subclassAry[1];   // 教育課程CD                       
            String subclassCd = subclassAry[2];     // 科目CD                 

            // 総ページ数取得
            getTotalPage(db2, classCd, curriculumCd, subclassCd);

            // 科目名称取得処理
            createSubClassName(db2, classCd, curriculumCd, subclassCd);

            // 帳票出力明細データ取得
            final List student = createStudents(db2, classCd, curriculumCd, subclassCd);

            // 帳票出力のメソッド
            outPutPrint(db2, student);
        }
    }

    /**
     * 帳票出力処理
     * @param db2
     * @param Student           生徒データオブジェクト
     * @throws SQLException
     */
    private void outPutPrint(final DB2UDB db2, final List student) throws SQLException {

        String breakschregNo = null;
        int line = 0;
        int page_cnt = 1;
        int schreg_cnt = 0;

        for (Iterator it = student.iterator(); it.hasNext();) {
            final Student sudent = (Student) it.next();

            // 学籍番号のブレイク
            if (breakschregNo == null  || !breakschregNo.equals(sudent._schregNo)) {
                // 実施回数を設定
                if (breakschregNo != null) {
                    _form._svf.VrsOutn("ENFORCEMENT_CNT",   line,   String.valueOf(schreg_cnt));
                    schreg_cnt = 0;
                }
                // MAX行
                if ( PAGE_MAX_LINE == line ) {
                    ++page_cnt;
                    _form._svf.VrEndPage();
                    line = 0;
                }

                if (line == 0) {
                    // 見出し出力
                    printSvfHead(page_cnt);
                }
                ++line;
                // 生徒別の出力
                printSvfSchregNo(sudent, line);
                breakschregNo = sudent._schregNo;
            }
            
            // 出席日が設定されているデータを対象に実施回数をカウント
            if (!sudent._attendDate.equals("")) {
                ++schreg_cnt;
            }

            // 生徒の出席日出力
            printSvfAttendDate(sudent, schreg_cnt, line);

            _hasData = true;
        }

        // 最終行の出力
        if (breakschregNo != null) {
            _form._svf.VrsOutn("ENFORCEMENT_CNT",   line,   String.valueOf(schreg_cnt));
            _form._svf.VrEndPage();
        }
    }

    /**
     * ヘッダ情報を帳票に設定
     * @param page_cnt      現在ページ数
     */
    private void printSvfHead(int page_cnt) {
        //* ヘッダ
        _form._svf.VrsOut("NENDO"       ,_param._nengetu);          // 対象年度 
        _form._svf.VrsOut("DATE"        ,_param._date);             // 作成日
        _form._svf.VrsOut("PAGE"        ,String.valueOf(page_cnt)); // ページ          
        _form._svf.VrsOut("TOTALPAGE"   ,String.valueOf(_param._totalPage ));           // 総ページ         
        _form._svf.VrsOut("SUBCLASSNAME"    ,String.valueOf(_param._subClassName ));    // 科目名称             
    }

    /**
     * 生徒情報を帳票に設定
     * @param sudent    生徒データオブジェクト
     * @param line      現在行数
     */
    private void printSvfSchregNo(final Student sudent, int line) {

        // 学籍番号
        _form._svf.VrsOutn("SCHREGNO",  line,   sudent._schregNo);
        // 氏名
        _form._svf.VrsOutn(setformatArea("NAME", sudent._name, 10, "1", "2"),   line, sudent._name);
        // 単位数
        _form._svf.VrsOutn("COMP_CREDIT",   line, sudent._compCredit);
        // 基準
        _form._svf.VrsOutn("SCHOOLING_SEQ", line, sudent._schoolingSeq);
    }

    /**
     * 生徒単位の出席日の設定を行う
     * @param sudent        生徒データオブジェクト
     * @param schreg_cnt    実施回数
     * @param line          現在行数
     */
    private void printSvfAttendDate(final Student sudent, int schreg_cnt,int line) {

        // 出席日
        if (!sudent._attendDate.equals("")) {
            _form._svf.VrsOutn("ENFORCEMENT_DAY"+String.valueOf(schreg_cnt),    line,
                    fomatDate(sudent._attendDate, "yyyy-MM-dd", "MM/dd") + " " + sudent._periodCd);
        }
    }

    /**
     * 総ページ数取得処理
     * @param db2
     * @param classCd           教科CD
     * @param curriculumCd      教育課程CD
     * @param subclassCd        科目CD
     * @throws SQLException
     */
    private void getTotalPage(
            final DB2UDB db2,
            final String classCd,
            final String curriculumCd,
            final String subclassCd
    ) throws SQLException {
        final String sql = getTotalPageSql(classCd, curriculumCd, subclassCd);
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                _param._totalPage = Integer.parseInt(rs.getString("COUNT"));
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
    }

    /**
     * 総ページ数取得ＳＱＬ作成処理
     * @param classCd           教科CD
     * @param curriculumCd      教育課程CD
     * @param subclassCd        科目CD
     * @return
     */
    private String getTotalPageSql(String classCd, String curriculumCd, String subclassCd) {
        StringBuffer stb = new StringBuffer();

        stb.append(" select");
        stb.append("    SUM(T1.COUNT) COUNT");
        stb.append(" from (");
        stb.append("    select CASE WHEN 0 < MOD(COUNT(DISTINCT W1.SCHREGNO),50) THEN COUNT(DISTINCT W1.SCHREGNO)/50 + 1 ELSE COUNT(DISTINCT W1.SCHREGNO)/50 END AS COUNT ");
        stb.append("    from   COMP_REGIST_DAT W1");
        stb.append("    left  join  REC_COMMUTING_DAT W2 on");
        stb.append("                W1.YEAR       = W2.YEAR  and");
        stb.append("                W1.SCHREGNO   = W2.SCHREGNO  and");
        stb.append("                W1.CLASSCD    = W2.CLASSCD  and");
        stb.append("                W1.CURRICULUM_CD = W2.CURRICULUM_CD  and");
        stb.append("                W1.SUBCLASSCD = W2.SUBCLASSCD");
        stb.append("    inner  join SCHREG_BASE_MST W3 on");
        stb.append("                W1.SCHREGNO   = W3.SCHREGNO  and");
        stb.append("                W3.GRD_DIV IS NULL");
        stb.append("    inner  join SCHREG_REGD_DAT W4 on");
        stb.append("                W4.YEAR     = '" + _param._year + "' and");
        stb.append("                W4.SEMESTER = '" + _param._semester + "' and");
        stb.append("                W3.SCHREGNO = W4.SCHREGNO");
        stb.append("    inner  join STUDENTDIV_MST W5 on");
        stb.append("                W4.STUDENT_DIV = W5.STUDENT_DIV and");
        stb.append("                W5.COMMUTING_DIV ='1'");
        stb.append(" where");
        stb.append("    W1.CLASSCD = '"+classCd+"' and");
        stb.append("    W1.CURRICULUM_CD = '"+curriculumCd+"' and");
        stb.append("    W1.SUBCLASSCD = '"+subclassCd+"' ");
        stb.append(" ) T1");

        return stb.toString();
   }

    /**
     * 生徒データ取得処理
     * @param db2
     * @param classCd           教科CD
     * @param curriculumCd      教育課程CD
     * @param subclassCd        科目CD
     * @return
     * @throws SQLException
     */
    private List createStudents(
            final DB2UDB db2,
            final String classCd,
            final String curriculumCd,
            final String subclassCd
    ) throws SQLException {

        final List rtnList = new ArrayList();
        final String sql = getStudentSql(classCd, curriculumCd, subclassCd);
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final Student student = new Student(
                        rs.getString("SCHREGNO"),
                        rs.getString("COMP_CREDIT"),
                        nvlT(rs.getString("ATTEND_DATE")),
                        rs.getString("PERIODCD"),
                        nvlT(rs.getString("SCHOOLING_SEQ")),
                        nvlT(rs.getString("NAME"))
                );
                rtnList.add(student);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }

    /**
     * 生徒データ取得ＳＱＬ作成処理
     * @param classCd           教科CD
     * @param curriculumCd      教育課程CD
     * @param subclassCd        科目CD
     * @return
     */
    private String getStudentSql(String classCd, String curriculumCd, String subclassCd) {
        final String sql;
        sql = "select"
            + " W1.SCHREGNO,"
            + " W1.COMP_CREDIT,"
            + " W2.ATTEND_DATE,"
            + " W2.PERIODCD,"
            + " W3.SCHOOLING_SEQ,"
            + " W4.NAME"
            + " from   COMP_REGIST_DAT W1"
            + " left  join  REC_COMMUTING_DAT W2 on"
            +             " W1.YEAR       = W2.YEAR  and"
            +             " W1.SCHREGNO   = W2.SCHREGNO  and"
            +             " W1.CLASSCD    = W2.CLASSCD  and"
            +             " W1.CURRICULUM_CD = W2.CURRICULUM_CD  and"
            +             " W1.SUBCLASSCD = W2.SUBCLASSCD"
            + " left  join  SUBCLASS_DETAILS_MST W3 on"
            +             " W1.YEAR       = W3.YEAR  and"
            +             " W1.CLASSCD    = W3.CLASSCD  and"
            +             " W1.CURRICULUM_CD = W3.CURRICULUM_CD  and"
            +             " W1.SUBCLASSCD = W3.SUBCLASSCD"
            + " inner  join SCHREG_BASE_MST W4 on"
            +             " W1.SCHREGNO   = W4.SCHREGNO  and"
            +             " W4.GRD_DIV IS NULL"
            + " inner  join SCHREG_REGD_DAT W5 on"
            +             " W5.YEAR     = '" + _param._year + "' and"
            +             " W5.SEMESTER = '" + _param._semester + "' and"
            +             " W4.SCHREGNO = W5.SCHREGNO"
            + " inner  join STUDENTDIV_MST W6 on"
            +             " W5.STUDENT_DIV = W6.STUDENT_DIV and"
            +             " W6.COMMUTING_DIV ='1'"
            + " where"
            +     " W1.YEAR = '"+_param._year+"' and"
            +     " W1.CLASSCD = '"+classCd+"' and"
            +     " W1.CURRICULUM_CD = '"+curriculumCd+"' and"
            +     " W1.SUBCLASSCD = '"+subclassCd+"' "
            + " order by W4.SCHREGNO, W2.ATTEND_DATE";

        log.debug("帳票出力対象データ抽出 の SQL=" + sql);
        return sql;
    }

    /**
     * 生徒データクラス
     * @author nakasone
     *
     */
    private class Student {
        final String _schregNo;

        /** 単位 */
        final String _compCredit;

        /** 出席日 */
        final String _attendDate;

        /** 校時 **/
        final String _periodCd;

        /** 基準 */
        final String _schoolingSeq;

        final String _name;

        Student(
                final String schregNo,
                final String compCredit,
                final String attendDate,
                final String periodCd,
                final String schoolingSeq,
                final String name
        ) {
            _schregNo = schregNo;
            _compCredit = compCredit;
            _attendDate = attendDate;
            _periodCd = periodCd;
            _schoolingSeq = schoolingSeq;
            _name = name;
        }
    }

    /**
     * 科目名称データ取得処理
     * @param db2
     * @param classCd
     * @param curriculumCd
     * @param subclassCd
     * @throws SQLException
     */
    private void createSubClassName(
            final DB2UDB db2,
            final String classCd,
            final String curriculumCd,
            final String subclassCd
    ) throws SQLException {
        final String sql = getSubClassNameSql(classCd, curriculumCd, subclassCd);
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                _param._subClassName = nvlT(rs.getString("SUBCLASSNAME"));
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
    }

    /**
     * 科目名称データ取得ＳＱＬ作成処理
     * @param classCd
     * @param curriculumCd
     * @param subclassCd
     * @return
     */
    private String getSubClassNameSql(String classCd, String curriculumCd, String subclassCd) {
        StringBuffer stb = new StringBuffer();

        stb.append(" select");
        stb.append(" W1.SUBCLASSNAME");
        stb.append(" from   SUBCLASS_MST W1");
        stb.append(" where");
        stb.append(" W1.CLASSCD = '"+classCd+"' and");
        stb.append(" W1.CURRICULUM_CD = '"+curriculumCd+"' and");
        stb.append(" W1.SUBCLASSCD = '"+subclassCd+"' ");

        log.debug("科目名称データ抽出 の SQL=" + stb.toString());
        return stb.toString();
    }

    private class Param {
        private final String _programid;
        private final String _year;
        private final String _semester;
        private final String _loginDate;
        private final String _nengetu;
        private final String _date;
        private final String[] _subclass;

        int _totalPage = 0;
        String _subClassName = "";

        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _programid = request.getParameter("PRGID");
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _subclass = request.getParameterValues("SUBCLASSCD");

            _nengetu = KenjaProperties.gengou(Integer.parseInt(_year))+"年度";
            _date = KNJ_EditDate.h_format_JP(_loginDate);
        }
    }

    /**
     * 帳票設定のフィールド名を設定する
     * @param area_name
     * @param sval
     * @param area_len
     * @param hokan_Name1
     * @param hokan_Name2
     * @return
     */
    private String setformatArea(String area_name, String sval, int area_len, String hokan_Name1, String hokan_Name2) {
        String retAreaName = "";
        // 値がnullの場合はnullが返される
        if (sval == null) {
            return null;
        }

        // 設定値が制限文字超の場合、帳票設定エリアの変更を行う
        if (area_len > sval.length()) {
            retAreaName = area_name + hokan_Name1;
        } else {
            retAreaName = area_name + hokan_Name2;
        }
        return retAreaName;
    }

    /**
     * NULLの場合、空文字を返す
     * @param val
     * @return
     */
    private String nvlT(String val) {
        if (val == null) {
            return "";
        } else {
            return val;
        }
    }

    /**
     * 日付を指定された形式にフォーマットを行う
     * @param cnvDate       対象日付文字列
     * @param before_sfmt   変換前の日付形式
     * @param after_sfmt    変換後の日付形式
     * @return
     */
    private String fomatDate(String cnvDate, String before_sfmt, String after_sfmt) {

        String retDate = "";
        try {
            DateFormat foramt = new SimpleDateFormat(before_sfmt);
            //文字列よりDate型へ変換
            Date date1 = foramt.parse(cnvDate);
            // 年月日のフォーマットを指定
            SimpleDateFormat sdf1 = new SimpleDateFormat(after_sfmt);
            // Date型より文字列へ変換
            retDate = sdf1.format(date1);
        } catch(Exception e) {
            log.error("fomatDate error!");
        }
        return retDate;
    }

    // =================================================================================================================
    private void dumpParam(final HttpServletRequest request) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
    }

    private class Form {
        private Vrw32alp _svf;

        public Form(final String file,final HttpServletResponse response) throws IOException {
            _svf = new Vrw32alp();

            if (_svf.VrInit() < 0) {
                throw new IllegalStateException("svf初期化失敗");
            }
            _svf.VrSetSpoolFileStream(response.getOutputStream());
            response.setContentType("application/pdf");

            _svf.VrSetForm(FORM_FILE, 1);
        }

        private void closeSvf() {
            if (!_hasData) {
                _svf.VrSetForm("MES001.frm", 0);
                _svf.VrsOut("note", "note");
                _svf.VrEndPage();
            }

            final int ret = _svf.VrQuit();
            log.info("===> VrQuit():" + ret);
        }
    }

    private boolean openDb(final DB2UDB db2) {
        try {
            db2.open();
        } catch (final Exception ex) {
            log.error("db open error!", ex);
            return true;
        }
        return false;
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }
}
