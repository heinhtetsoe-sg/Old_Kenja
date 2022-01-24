package servletpack.KNJB;

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
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;


/**
 * クラス別希望状況一覧
 */
public class KNJB1308 {

    private static final Log log = LogFactory.getLog(KNJB1308.class);
    private boolean _hasData;
    private Param _param;
    private final String JUGYOURYOU = "1";

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        DB2UDB db2 = null;
        try {
            final String dbName = request.getParameter("DBNAME");
            db2 = new DB2UDB(dbName, "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

        } catch (final Exception e) {
            log.error("Exception:", e);
            return;
        }

        _param = createParam(db2, request);

        Vrw32alp svf = null;
        try {
            svf = new Vrw32alp();

            if (svf.VrInit() < 0) {
                throw new IllegalStateException("svf初期化失敗");
            }
            svf.VrSetSpoolFileStream(response.getOutputStream());
            response.setContentType("application/pdf");

            for (int i = 0; i < _param._classSelected.length; i++) {

                // 生徒データを取得
                final List studentList = createStudentInfoData(db2, _param._classSelected[i]);
                if (printMain(svf, studentList)) { // 生徒出力のメソッド
                    _hasData = true;
                }
            }

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            final int ret = svf.VrQuit();
            log.info("===> VrQuit():" + ret);

            if (null != db2) {
                db2.commit();
                db2.close();
            }
        }
    }

    /**
     * 生徒の出力
     */
    private boolean printMain(final Vrw32alp svf, final List studentList) throws Exception {
        boolean hasData = false;
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (student._kouzaList.size() > 0) {
                if (printStudent(svf, student)) {
                    hasData = true;
                }
            }
        }
        return  hasData;
    }

    private boolean printStudent(final Vrw32alp svf, final Student student) {
        boolean hasData = false;
        svf.VrSetForm("KNJB1308.frm", 4);
        if ("1".equals(_param._output)) {
            svf.VrsOut("TITLE", "生徒別履修登録確認票");
        } else {
            svf.VrsOut("TITLE", "生徒別受講講座確認票");
        }
        svf.VrsOut("INVEST_YEAR", _param._year + "年度");
        svf.VrsOut("TAKE_YEAR", _param._year + "年度");
        svf.VrsOut("PRINT_DATE", _param._ctrlDate == null ? null : _param._ctrlDate.replace('-', '/'));
        svf.VrsOut("HR_NAME", student._hrName);
        svf.VrsOut("NO", (NumberUtils.isDigits(student._attendno)) ? String.valueOf(Integer.parseInt(student._attendno)) : "");
        svf.VrsOut("SCHREG_NO", student._schregno);
        svf.VrsOut("NAME" + ((30 < getMS932ByteLength(student._name)) ? "2" : "1"), student._name);

        int gyo = 0;
        for (final Iterator itk = student._kouzaList.iterator(); itk.hasNext();) {
            final Kouza kouza = (Kouza) itk.next();
            gyo++;
            final String fieldNo = (student._kouzaList.size() == gyo) ? "2" : "1";
            final String subFieldLen = (20 < getMS932ByteLength(kouza._subclassname)) ? "_2" : "_1";
            final String chairFieldLen = (30 < getMS932ByteLength(kouza._chairname)) ? "_2" : "";
            svf.VrsOut("SUBCLASS_CD" + fieldNo, kouza._subclasscd);
            svf.VrsOut("SUBCLASS_NAME" + fieldNo + subFieldLen, kouza._subclassname);
            svf.VrsOut("HOPE_CHAIR_CD" + fieldNo, kouza._chaircd);
            svf.VrsOut("HOPE_CHAIR_NAME" + fieldNo + chairFieldLen, kouza._chairname);
            svf.VrsOut("HOPE_PERIOD_NAME" + fieldNo, kouza._takesemesName);
            svf.VrsOut("MUST_GROUP_NAME" + fieldNo, kouza._hitsuri);
            svf.VrsOut("TIME" + fieldNo, (NumberUtils.isDigits(kouza._credits)) ? kouza._credits : "");
            svf.VrEndRecord();
            hasData = true;
        }
        svf.VrsOut("TOTAL_SUBCLASS", String.valueOf(student._kouzaList.size()));
        svf.VrsOut("CREDIT", String.valueOf(student._credits_sum));
        svf.VrEndRecord();

        svf.VrsOut("REMARK1", "\n");
        svf.VrEndRecord();

        svf.VrsOut("DUMMY", "a");
        svf.VrEndRecord();

        svf.VrsOut("TITLE2", "納入金額について");
        svf.VrEndRecord();

        svf.VrsOut("DUMMY", "a");
        svf.VrEndRecord();

        //校納金データ
        int totalMoney = 0;
        for (Iterator itCollect = student._collectDataList.iterator(); itCollect.hasNext();) {
            final CollectData collectData = (CollectData) itCollect.next();
            svf.VrsOut("ITEM1", collectData._mName);
            final String setCredit = JUGYOURYOU.equals(collectData._gakunoukinDiv) ? collectData._collectCnt + "単位" : String.valueOf(collectData._collectCnt);
            svf.VrsOut("COMP_TIME1", setCredit);
            svf.VrsOut("MONEY1", String.valueOf(collectData._money));
            svf.VrsOut("TOTAL_MONEY1", String.valueOf(collectData._totalMoney));
            totalMoney += collectData._totalMoney;
            svf.VrEndRecord();
        }
        svf.VrsOut("ITEM2", "計");
        svf.VrsOut("TOTAL_MONEY2", String.valueOf(totalMoney));
        svf.VrEndRecord();

        svf.VrsOut("DUMMY", "a");
        svf.VrEndRecord();

        final String[] limitDateArray = StringUtils.split(_param._limitDate, "/");
        final int setMonth = Integer.parseInt(limitDateArray[1]);
        final int setDay = Integer.parseInt(limitDateArray[2]);
        svf.VrsOut("COMMENT1", "●つり銭のいらないように、窓口に" + setMonth + "月" + setDay + "日までに納付して下さい。");
        svf.VrEndRecord();

        svf.VrsOut("COMMENT2", "問い合わせ先　" + _param._schoolMst._schoolname1);
        svf.VrEndRecord();
        svf.VrsOut("COMMENT2", _param._schoolMst._schooltelno);
        svf.VrEndRecord();

        return hasData;
    }

    private static int getMS932ByteLength(final String str) {
        int len = 0;
        if (null != str) {
            try {
                len = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return len;
    }

    /**
     * 生徒データ取得処理
     * @param db2           ＤＢ接続オブジェクト
     * @return              帳票出力対象データリスト
     * @throws Exception
     */
    private List createStudentInfoData(final DB2UDB db2, final String hrClass) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List rtnList = new ArrayList();
        try {
            final String sql = getStudentInfoSql(hrClass);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final Student studentInfo = new Student(
                        rs.getString("HR_NAME"),
                        rs.getString("ATTENDNO"),
                        rs.getString("SCHREGNO"),
                        rs.getString("NAME")
                );
                studentInfo.setKouzaInfoData(db2);
                studentInfo.setCollectDataList(db2);
                rtnList.add(studentInfo);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }

    private String getStudentInfoSql(final String hrClass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T2.HR_NAME, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T3.SCHREGNO, ");
        stb.append("     T3.NAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT T2 ");
        stb.append("             ON  T2.YEAR = T1.YEAR ");
        stb.append("             AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("             AND T2.GRADE = T1.GRADE ");
        stb.append("             AND T2.HR_CLASS = T1.HR_CLASS ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.GRADE = '" + _param._grade + "' ");
        if ("1".equals(_param._categoryIsClass)) {
            stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + hrClass + "' ");
        } else {
            stb.append("     AND T1.SCHREGNO = '" + hrClass + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO ");
        return stb.toString();
    }

    /** 生徒データクラス */
    private class Student {
        final String _hrName;
        final String _attendno;
        final String _schregno;
        final String _name;
        final List _kouzaList = new ArrayList();
        int _credits_sum = 0;
        final List _collectDataList = new ArrayList<CollectData>();

        Student(
                final String hrName,
                final String attendno,
                final String schregno,
                final String name
        ) {
            _hrName = hrName;
            _attendno = attendno;
            _schregno = schregno;
            _name = name;
        }

        private void setKouzaInfoData(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getKouzaInfoSql(_schregno);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Kouza kouzaInfo = new Kouza(
                            rs.getString("SUBCLASSCD"),
                            rs.getString("SUBCLASSNAME"),
                            rs.getString("CHAIRCD"),
                            rs.getString("CHAIRNAME"),
                            rs.getString("CREDITS"),
                            rs.getString("HITSURI"),
                            rs.getString("TAKESEMES_NAME")
                    );
                    _kouzaList.add(kouzaInfo);
                    _credits_sum += (NumberUtils.isDigits(kouzaInfo._credits)) ? Integer.parseInt(kouzaInfo._credits) : 0;
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
        }

        private void setCollectDataList(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getCollectSql(_schregno);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String lCd = rs.getString("COLLECT_L_CD");
                    final String mCd = rs.getString("COLLECT_M_CD");
                    final String gakunoukinDiv = rs.getString("GAKUNOKIN_DIV");
                    final int collectCnt = rs.getInt("COLLECT_CNT");
                    final String mName = rs.getString("COLLECT_M_NAME");
                    final int money = rs.getInt("COLLECT_M_MONEY");
                    final CollectData collectData = new CollectData(lCd, mCd, gakunoukinDiv, collectCnt, mName, money);

                    _collectDataList.add(collectData);
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
        }
    }

    private String getKouzaInfoSql(final String schregno) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH T_CHAIR AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.SCHREGNO, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T2.CLASSCD, ");
            stb.append("     T2.SCHOOL_KIND, ");
            stb.append("     T2.CURRICULUM_CD, ");
        }
        stb.append("         T2.SUBCLASSCD, ");
        stb.append("         T2.CHAIRCD, ");
        stb.append("         T2.CHAIRNAME, ");
        stb.append("         CASE WHEN T2.TAKESEMES = '0' THEN '通年' ELSE L1.SEMESTERNAME END AS TAKESEMES_NAME ");
        stb.append("     FROM ");
        stb.append("         CHAIR_STD_DAT T1 ");
        stb.append("         INNER JOIN CHAIR_DAT T2 ");
        stb.append("                 ON  T2.YEAR = T1.YEAR ");
        stb.append("                 AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("                 AND T2.CHAIRCD = T1.CHAIRCD ");
        stb.append("         LEFT JOIN SEMESTER_MST L1 ON L1.YEAR = T1.YEAR ");
        stb.append("                 AND L1.SEMESTER = T2.TAKESEMES ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append("         AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("         AND T1.SCHREGNO = '" + schregno + "' ");
        stb.append("         AND DATE('" + _param._date + "') BETWEEN T1.APPDATE AND T1.APPENDDATE ");
        stb.append(" ) ");
        stb.append(" , T_SUBCLASS_STD AS ( ");
        stb.append("     SELECT DISTINCT ");
        stb.append("         T1.SCHREGNO, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
        }
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T2.CHAIRCD, ");
        stb.append("         T2.CHAIRNAME, ");
        stb.append("         T2.TAKESEMES_NAME ");
        stb.append("     FROM ");
        stb.append("         SUBCLASS_STD_SELECT_RIREKI_DAT T1 ");
        stb.append("         LEFT JOIN T_CHAIR T2 ");
        stb.append("                 ON  T2.SCHREGNO = T1.SCHREGNO ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("             AND T2.CLASSCD = T1.CLASSCD ");
            stb.append("             AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("             AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("                 AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append("         AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("         AND T1.RIREKI_CODE = '" + _param._rirekiCode + "' ");
        stb.append("         AND T1.SCHREGNO = '" + schregno + "' ");
        stb.append(" ) ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     T2.SCHREGNO, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(" T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS SUBCLASSCD, ");
        } else {
            stb.append(" T2.SUBCLASSCD AS SUBCLASSCD, ");
        }
        stb.append("     S1.SUBCLASSNAME, ");
        stb.append("     T2.CHAIRCD, ");
        stb.append("     T2.CHAIRNAME, ");
        stb.append("     T2.TAKESEMES_NAME, ");
        stb.append("     L1.CREDITS, ");
        stb.append("     VALUE(Z011.ABBV1, '') AS HITSURI ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        //1:履修登録 2:講座
        if ("1".equals(_param._output)) {
            stb.append("     INNER JOIN T_SUBCLASS_STD T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        } else {
            stb.append("     INNER JOIN T_CHAIR T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        }
        stb.append("     LEFT JOIN SUBCLASS_MST S1 ");
        stb.append("             ON  S1.SUBCLASSCD = T2.SUBCLASSCD ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         AND S1.CLASSCD = T2.CLASSCD ");
            stb.append("         AND S1.SCHOOL_KIND = T2.SCHOOL_KIND ");
            stb.append("         AND S1.CURRICULUM_CD = T2.CURRICULUM_CD ");
        }
        stb.append("     LEFT JOIN CREDIT_MST L1  ");
        stb.append("             ON  L1.YEAR = T1.YEAR ");
        stb.append("             AND L1.GRADE = T1.GRADE ");
        stb.append("             AND L1.COURSECD = T1.COURSECD ");
        stb.append("             AND L1.MAJORCD = T1.MAJORCD ");
        stb.append("             AND L1.COURSECODE = T1.COURSECODE ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         AND L1.CLASSCD = T2.CLASSCD ");
            stb.append("         AND L1.SCHOOL_KIND = T2.SCHOOL_KIND ");
            stb.append("         AND L1.CURRICULUM_CD = T2.CURRICULUM_CD ");
        }
        stb.append("             AND L1.SUBCLASSCD = T2.SUBCLASSCD ");
        stb.append("     LEFT JOIN NAME_MST Z011 ON  Z011.NAMECD1 = 'Z011' ");
        stb.append("          AND L1.REQUIRE_FLG = Z011.NAMECD2 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
        stb.append(" ORDER BY ");
        stb.append("     SUBCLASSCD, ");
        stb.append("     T2.CHAIRCD ");
        return stb.toString();
    }

    /** 講座データクラス */
    private class Kouza {
        final String _subclasscd;
        final String _subclassname;
        final String _chaircd;
        final String _chairname;
        final String _credits;
        final String _hitsuri;
        final String _takesemesName;

        Kouza(
                final String subclasscd,
                final String subclassname,
                final String chaircd,
                final String chairname,
                final String credits,
                final String hitsuri,
                final String takesemesName
        ) {
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _chaircd = chaircd;
            _chairname = chairname;
            _credits = credits;
            _hitsuri = hitsuri;
            _takesemesName = takesemesName;
        }
    }

    private String getCollectSql(final String schregNo) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SLIP_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     SLIP_M.* ");
        stb.append(" FROM ");
        stb.append("     COLLECT_SLIP_M_DAT SLIP_M ");
        stb.append("     INNER JOIN COLLECT_M_MST COL_M ON SLIP_M.SCHOOLCD = COL_M.SCHOOLCD ");
        stb.append("          AND SLIP_M.SCHOOL_KIND = COL_M.SCHOOL_KIND ");
        stb.append("          AND SLIP_M.YEAR = COL_M.YEAR ");
        stb.append("          AND SLIP_M.COLLECT_L_CD = COL_M.COLLECT_L_CD ");
        stb.append("          AND SLIP_M.COLLECT_M_CD = COL_M.COLLECT_M_CD ");
        stb.append("          AND COL_M.GAKUNOKIN_DIV = '1' ");
        stb.append(" WHERE ");
        stb.append("     SLIP_M.SCHOOLCD = '" + _param._schoolCd + "' ");
        stb.append("     AND SLIP_M.SCHOOL_KIND = '" + _param._schoolKind + "' ");
        stb.append("     AND SLIP_M.YEAR = '" + _param._year + "' ");
        stb.append("     AND SLIP_M.SCHREGNO = '" + schregNo + "' ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     SLIP_M.COLLECT_L_CD, ");
        stb.append("     SLIP_M.COLLECT_M_CD, ");
        stb.append("     COL_M.GAKUNOKIN_DIV, ");
        stb.append("     VALUE(SLIP_M.COLLECT_CNT, 0) AS COLLECT_CNT, ");
        stb.append("     COL_M.COLLECT_M_NAME, ");
        stb.append("     VALUE(COL_M.COLLECT_M_MONEY, 0) AS COLLECT_M_MONEY ");
        stb.append(" FROM ");
        stb.append("     SLIP_T, ");
        stb.append("     COLLECT_SLIP_M_DAT SLIP_M ");
        stb.append("     LEFT JOIN COLLECT_M_MST COL_M ON SLIP_M.SCHOOLCD = COL_M.SCHOOLCD ");
        stb.append("          AND SLIP_M.SCHOOL_KIND = COL_M.SCHOOL_KIND ");
        stb.append("          AND SLIP_M.YEAR = COL_M.YEAR ");
        stb.append("          AND SLIP_M.COLLECT_L_CD = COL_M.COLLECT_L_CD ");
        stb.append("          AND SLIP_M.COLLECT_M_CD = COL_M.COLLECT_M_CD ");
        stb.append(" WHERE ");
        stb.append("     SLIP_T.SCHOOLCD = SLIP_M.SCHOOLCD ");
        stb.append("     AND SLIP_T.SCHOOL_KIND = SLIP_M.SCHOOL_KIND ");
        stb.append("     AND SLIP_T.YEAR = SLIP_M.YEAR ");
        stb.append("     AND SLIP_T.SLIP_NO = SLIP_M.SLIP_NO ");
        stb.append("     AND SLIP_T.SCHREGNO = SLIP_M.SCHREGNO ");
        stb.append(" ORDER BY ");
        stb.append("     SLIP_M.COLLECT_L_CD, ");
        stb.append("     SLIP_M.COLLECT_M_CD ");
        return stb.toString();
    }

    /** 校納金 */
    private class CollectData {
        final String _lCd;
        final String _mCd;
        final String _gakunoukinDiv;
        final int _collectCnt;
        final String _mName;
        final int _money;
        final int _totalMoney;

        CollectData(
                final String lCd,
                final String mCd,
                final String gakunoukinDiv,
                final int collectCnt,
                final String mName,
                final int money
        ) {
            _lCd = lCd;
            _mCd = mCd;
            _gakunoukinDiv = gakunoukinDiv;
            _collectCnt = collectCnt;
            _mName = mName;
            _money = money;
            _totalMoney = money * collectCnt;
        }
    }

    /** 学校マスタ */
    private class SchoolMst {
        final String _schoolname1;
        final String _schooltelno;

        SchoolMst(
                final String schoolname1,
                final String schooltelno
        ) {
            _schoolname1 = schoolname1;
            _schooltelno = schooltelno;
        }
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) {
        log.fatal("$Revision: 69630 $ $Date: 2019-09-11 17:28:19 +0900 (水, 11 9 2019) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return new Param(db2, request);
    }

    private class Param {

        final String _year;
        final String _semester;
        final String _categoryIsClass;
        final String _grade;
        final String[] _classSelected; //学年・組
        final String _output;
        final String _date;
        final String _limitDate;
        final String _ctrlDate;
        final String _schoolCd;
        final String _useCurriculumcd;
        final String _rirekiCode;
        final String _schoolKind;
        final SchoolMst _schoolMst;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year      = request.getParameter("YEAR");
            _semester  = request.getParameter("SEMESTER");
            _categoryIsClass  = request.getParameter("CATEGORY_IS_CLASS");
            _grade  = request.getParameter("GRADE");
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            _output = request.getParameter("OUTPUT");
            _date = request.getParameter("DATE") == null ? null : request.getParameter("DATE").replace('/', '-');
            _limitDate = request.getParameter("LIMIT_DATE");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _schoolCd = request.getParameter("SCHOOLCD");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _rirekiCode = request.getParameter("RIREKI_CODE");
            _schoolKind = getSchoolKind(db2);
            _schoolMst = getSchoolName(db2);
        }

        private String getSchoolKind(final DB2UDB db2) {
            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    retStr = rs.getString("SCHOOL_KIND");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retStr;
        }

        private SchoolMst getSchoolName(final DB2UDB db2) {
            SchoolMst schoolMst = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT SCHOOLNAME1, SCHOOLTELNO FROM SCHOOL_MST WHERE YEAR = '" + _year + "' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    final String schoolname1 = rs.getString("SCHOOLNAME1");
                    final String schooltelno = rs.getString("SCHOOLTELNO");
                    schoolMst = new SchoolMst(schoolname1, schooltelno);
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return schoolMst;
        }
    }

}// クラスの括り
