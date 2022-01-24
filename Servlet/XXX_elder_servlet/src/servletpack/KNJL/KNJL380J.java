// kanji=漢字
/*
 * $Id: 458a6e3f72a844254713fe0fa1451b54689779ae $
 *
 * 作成日: 2007/12/27 16:49:00 - JST
 * 作成者: nakada
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

/**
 *  新入生名札出力
 * @author nakada
 * @version $Id: 458a6e3f72a844254713fe0fa1451b54689779ae $
 */
public class KNJL380J {
    /* pkg */static final Log log = LogFactory.getLog(KNJL380J.class);

    private static final String FORM_FILE = "KNJL380J.frm";

    /*
     * タイトル
     */
    /** 仮クラス */
    private static final String TITLE_1_CD = "1";
    private static final String TITLE_1 = "仮クラス";
    /** クラス */
    private static final String TITLE_2_CD = "2";
    private static final String TITLE_2 = "クラス";

    /*
     * 文字数による出力項目切り分け基準
     */
    /** 名前 */
    private static final int NAME1_LENG = 6;
    private static final int NAME2_LENG = 20;

    /** かな名前 */
    private static final int KANA_NAME1_LENG = 10;
    private static final int KANA_NAME2_LENG = 16;

    private Form _form;
    private Vrw32alp _svf;

    private DB2UDB db2;

    private boolean _hasData;

    Param _param;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        dumpParam(request);
        _param = createParam(request);

        _form = new Form(FORM_FILE, response);

        db2 = null;

        try {
            final String dbName = request.getParameter("DBNAME");
            db2 = new DB2UDB(dbName, "db2inst1", "db2inst1", DB2UDB.TYPE2);
            if (openDb(db2)) {
                return;
            }

            _param.load(db2);

            final List preSchoolInfoDats = createPreSchoolInfoDats(db2);

            printMain(preSchoolInfoDats);

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            _form.closeSvf();
            closeDb(db2);
        }
    }

    private void printMain(final List preSchoolInfoDats) 
        throws SQLException, Exception {

        String oldHrClass = "";

        if (!preSchoolInfoDats.isEmpty()) {
            final PreSchoolInfoDat preSchoolInfoDat = (PreSchoolInfoDat) preSchoolInfoDats.get(0);

            if (_param._title.equals(TITLE_1_CD)) {
                oldHrClass = preSchoolInfoDat._preHrClass;
            } else {
                oldHrClass = preSchoolInfoDat._hrClass;
            }
        }

        for (Iterator it = preSchoolInfoDats.iterator(); it.hasNext();) {
            final PreSchoolInfoDat classFormationDat = (PreSchoolInfoDat) it.next();

            if ((_param._title.equals(TITLE_1_CD) && (!classFormationDat._preHrClass.equals(oldHrClass))) ||
                    (_param._title.equals(TITLE_2_CD) && (!classFormationDat._hrClass.equals(oldHrClass)))) {

                _form._svf.VrPaperEject();
                
                if (_param._title.equals(TITLE_1_CD)) {
                    oldHrClass = classFormationDat._preHrClass;
                } else {
                    oldHrClass = classFormationDat._hrClass;
                }
            }

            printApplicant(classFormationDat);

            _form._svf.VrEndPage();
            _hasData = true;
        }
    }

    private void printApplicant(PreSchoolInfoDat preSchoolInfoDat) {
        if (_param._title.equals(TITLE_1_CD)) {
            /* タイトル */
            _form._svf.VrsOut("ITEM", TITLE_1);
            /* クラス */
            _form._svf.VrsOut("HR_NAME", _param._schregRegdHdatMapString(
                    _param._year + _param._semester + _param._grade + preSchoolInfoDat._preHrClass));
            /* 出席番号 */
            int int1 = Integer.parseInt(preSchoolInfoDat._preAttendno);
            _form._svf.VrsOut("ATTENDNO", Integer.toString(int1));
        } else {
            /* タイトル */
            _form._svf.VrsOut("ITEM", TITLE_2);
            /* クラス */
            _form._svf.VrsOut("HR_NAME", _param._schregRegdHdatMapString(
                    _param._year + _param._semester + _param._grade + preSchoolInfoDat._hrClass));
            /* 出席番号 */
            int int1 = Integer.parseInt(preSchoolInfoDat._attendno);
            _form._svf.VrsOut("ATTENDNO", Integer.toString(int1));
        }

        /* かな名前 */
        _form.printKanaName(preSchoolInfoDat);

        /* 名前 */
        _form.printName(preSchoolInfoDat);
        
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

    // ======================================================================
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _prgrId;
        private final String _dbName;
        private final String _title;
        private final String _grade;

        private Map _schregRegdHdatMap;

        public Param(
                final String year,
                final String semester,
                final String prgId,
                final String dbName,
                final String title,
                final String grade
        ) {
            _year = year;
            _semester = semester;
            _prgrId = prgId;
            _dbName = dbName;
            _title = title;
            _grade = grade;
        }

        public void load(DB2UDB db2) throws SQLException, Exception {
            _schregRegdHdatMap = createPreSchoolHdat(db2);

            return;
        }

        public String _schregRegdHdatMapString(String code) {
            return nvlT((String)_schregRegdHdatMap.get(code));
        }
    }

    private Param createParam(final HttpServletRequest request) {
        final String year = request.getParameter("YEAR");
        final String semester = request.getParameter("SEMESTER");
        final String programId = request.getParameter("PRGID");
        final String dbName = request.getParameter("DBNAME");
        final String title = request.getParameter("TITLE");
        final String grade = request.getParameter("GRADE");

        final Param param = new Param
        (
                year,
                semester,
                programId,
                dbName,
                title,
                grade
        );

        return param;
    }

    private void dumpParam(final HttpServletRequest request) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
    }

    // ======================================================================
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

        public void printKanaName(PreSchoolInfoDat classFormationDat) {
            String name = nvlT(classFormationDat._freshmanDat._nameKana);

            if (!name.equals("")) {
                final String label;
                if (name.length() <= KANA_NAME1_LENG) {
                    label = "NAME_KANA1_1";
                } else if (name.length() <= KANA_NAME2_LENG){
                    label = "NAME_KANA1_2";
                } else {
                    label = "NAME_KANA1_3";
                }

                _form._svf.VrsOut(label, name);
            }
        }

        public void printName(PreSchoolInfoDat classFormationDat) {
            String name = nvlT(classFormationDat._freshmanDat._name);

            if (!name.equals("")) {
                final String label;
                if (name.length() <= NAME1_LENG) {
                    label = "NAME1_1";
                } else if (name.length() <= NAME2_LENG){
                    label = "NAME1_2";
                } else {
                    label = "NAME1_3";
                }

                _form._svf.VrsOut(label, name);
            }
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

    // ======================================================================
    /**
     * プレスクールヘッダデータ
     */
    public Map createPreSchoolHdat(DB2UDB db2)
        throws SQLException {

        final Map rtn = new HashMap();

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlPreSchoolHdat());
        rs = ps.executeQuery();
        while (rs.next()) {
            final String year = rs.getString("year");
            final String semester = rs.getString("semester");
            final String grade = rs.getString("grade");
            final String hrClass = rs.getString("hrClass");
            final String hrName = rs.getString("hrName");

            rtn.put(year + semester + grade + hrClass, hrName);
        }

        return rtn;
    }

    private String sqlPreSchoolHdat() {
        return " select"
                + "    YEAR as year,"
                + "    SEMESTER as semester,"
                + "    GRADE as grade,"
                + "    HR_CLASS as hrClass,"
                + "    HR_NAMEABBV as hrName"
                + " from"
                + "    PRE_SCHOOL_HDAT"
                + " where"
                + "    YEAR = '" + _param._year + "'"
                + "    and SEMESTER = '" + _param._semester + "'"
                + "    and GRADE = '" + _param._grade + "'"
                ;
    }

    /**
     * NULL値を""として返す。
     */
    private String nvlT(String val) {

        if (val == null) {
            return "";
        } else {
            return val;
        }
    }

    // ======================================================================
    /**
     * プレスクール情報データ。
     */
    private class PreSchoolInfoDat {
        private final String _examno;
        private final String _hrClass;
        private final String _attendno;
        private final String _preHrClass;
        private final String _preAttendno;

        private EntexamApplicantbaseDat _freshmanDat;   // 志願者基礎データ

        PreSchoolInfoDat(
                final String examno,
                final String hrClass,
                final String attendno,
                final String preHrClass,
                final String preAttendno
        ) {
            _examno = examno;
            _hrClass = hrClass;
            _attendno = attendno;
            _preHrClass = preHrClass;
            _preAttendno = preAttendno;
        }

        public void load(DB2UDB db2) throws SQLException, Exception {
            _freshmanDat = createEntexamApplicantbaseDat(db2, _examno);
        }
    }

    private List createPreSchoolInfoDats(final DB2UDB db2)
        throws SQLException, Exception {

        final List rtn = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlPreSchoolInfoDats());
        rs = ps.executeQuery();

        while (rs.next()) {
            final String examno = rs.getString("examno");
            final String hrClass = rs.getString("hrClass");
            final String attendno = rs.getString("attendno");
            final String preHrClass = rs.getString("preHrClass");
            final String preAttendno = rs.getString("preAttendno");

            final PreSchoolInfoDat classFormationDat = new PreSchoolInfoDat(
                    examno,
                    hrClass,
                    attendno,
                    preHrClass,
                    preAttendno
            );

            classFormationDat.load(db2);
            rtn.add(classFormationDat);
        }

        if (rtn.isEmpty()) {
            log.debug(">>>PRE_SCHOOL_INFO_DAT に該当するものがありません。");
            throw new Exception();
        } else {
            return rtn;
        }
    }

    private String sqlPreSchoolInfoDats() {
        StringBuffer stb = new StringBuffer();

        stb.append(" select");
        stb.append("    EXAMNO as examno,");
        stb.append("    HR_CLASS as hrClass,");
        stb.append("    ATTENDNO as attendno,");
        stb.append("    PRE_HR_CLASS as preHrClass,");
        stb.append("    PRE_ATTENDNO as preAttendno");
        stb.append(" from");
        stb.append("    PRE_SCHOOL_INFO_DAT");
        stb.append(" where");
        stb.append("    YEAR = '" + _param._year + "'");
        stb.append("    and SEMESTER = '" + _param._semester + "'");
        stb.append("    and GRADE = '" + _param._grade + "'");

        if (_param._title.equals(TITLE_1_CD)) {
            stb.append("    order by PRE_HR_CLASS, PRE_ATTENDNO");
        } else {
            stb.append("    and HR_CLASS is not null");
            stb.append("    order by HR_CLASS, ATTENDNO");
        }

        return stb.toString();
     }

    // ======================================================================
    /**
     * 志願者基礎データ。
     */
    private class EntexamApplicantbaseDat {
        private final String _name;
        private final String _nameKana;

        EntexamApplicantbaseDat() {
            _name = "";
            _nameKana = "";
        }

        EntexamApplicantbaseDat(
                final String name,
                final String nameKana
        ) {
            _name = name;
            _nameKana = nameKana;
        }
    }

    private EntexamApplicantbaseDat createEntexamApplicantbaseDat(final DB2UDB db2, String examno)
        throws SQLException, Exception {

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlEntexamApplicantbaseDat(examno));
        rs = ps.executeQuery();

        while (rs.next()) {
            final String name = rs.getString("name");
            final String nameKana = rs.getString("nameKana");

            final EntexamApplicantbaseDat entexamApplicantbaseDat = new EntexamApplicantbaseDat(
                    name,
                    nameKana
            );

            return entexamApplicantbaseDat;
        }

        return new EntexamApplicantbaseDat();
    }

    private String sqlEntexamApplicantbaseDat(String examno) {
        return " select"
                + "    NAME as name,"
                + "    NAME_KANA as nameKana"
                + " from"
                + "    ENTEXAM_APPLICANTBASE_DAT"
                + " where"
                + "    ENTEXAMYEAR = '" + _param._year + "' and"
                + "    EXAMNO = '" + examno + "'"
                ;
    }
} // KNJL380J

// eof
