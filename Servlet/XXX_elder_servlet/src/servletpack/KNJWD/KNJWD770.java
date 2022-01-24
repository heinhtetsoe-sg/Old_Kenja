package servletpack.KNJWD;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.WithusUtils;


/**
 * 成績結果通知書
 * @version $Id: 3860136aa4e25ed5b9b0396551783c54d906a046 $
 * @author nakasone
 */
public class KNJWD770 {
    /**
     * 全角のハイフン
     */
    private static final String ZENKAKU_HAIFUN = "\uFF0D";

    private static final String FORM_FILE = "KNJWD770.frm";
    private static final String SOUGOU_KYOUKA = "11";
    private static final int PAGE_MAX_LINE = 20;

    private boolean _hasData;
    private Form _form;
    private Vrw32alp _svf;
    private DB2UDB db2;
    Param _param;

    private static final Log log = LogFactory.getLog(KNJWD770.class);

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        _param = new Param(db2, request);

        _form = new Form(FORM_FILE, response);

        db2 = null;
        try {
            final String dbName = request.getParameter("DBNAME");
            db2 = new DB2UDB(dbName, "db2inst1", "db2inst1", DB2UDB.TYPE2);
            if (openDb(db2)) {
                return;
            }

            _param.loadW029Map(db2);
            _param.loadSchoolInfo(db2);

            _param.loadClass(db2);
            log.debug("教科マスタの数=" + _param._classMst.size());

            _param.loadSubClass(db2);
            log.debug("科目マスタの数=" + _param._subClassMst.size());

            final List students = getStudents();
            _hasData = print(db2, students);
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            _form.closeSvf();
            closeDb(db2);
        }
    }

    private List getStudents() throws SQLException {
        // TODO: ロジックをまとめろ!
        final List students;
        if (_param._is生徒指定) {
            students = createSchregStudents(db2); // 学籍番号毎に生徒データ取得
        } else {
            students = createGradeStudents(db2);  // 所属毎に生徒データ取得
        }
        return students;
    }

    private boolean print(final DB2UDB db2, final List students)    throws Exception {
        boolean hasData = false;
        int gyo = 1;    // 現在ページ数の判断用（行）
        int recCnt = 0;    // 合計レコード数
        int getCreditCnt = 0;  // 合計レコード数
        String schregNo = "";

        _form._svf.VrAttribute("SCHREGNO", "FF=1"); // 自動改ページ

        for (Iterator it = students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();

            if (recCnt > 0){
                if (!schregNo.equals(student._schregno)) {
                    _form._svf.VrsOut("TOTAL_GET_CREDIT", String.valueOf(getCreditCnt));
                    getCreditCnt = 0;               
                } else {
                    _form._svf.VrsOut("TOTAL_GET_CREDIT", "");
                }
                _form._svf.VrEndRecord();
            } else {
                printHeadFoot(db2, student);
            }

            //ページMAX行(20行) 又は 学籍番号ブレイクの場合
            if ((gyo > PAGE_MAX_LINE) || !schregNo.equals(student._schregno)) {
                printHeadFoot(db2, student);
                gyo = 1;
            }

            printMain(student);

            final int intGetCredit = null == student._getCredit || student._getCredit.equals(ZENKAKU_HAIFUN) ? 0 : student._getCredit.intValue();
            getCreditCnt += intGetCredit;

            ++recCnt;
            ++gyo;
            schregNo = student._schregno;
            hasData = true;
        }

        // 最終レコードを出力
        if (hasData) {
            _form._svf.VrsOut("TOTAL_GET_CREDIT", String.valueOf(getCreditCnt));
            _form._svf.VrEndRecord();
        }
        
        return hasData;
    }

    private void printMain(final Student student) {
        // 教科名      
        final String className = _param.getClassName(student._classCd);
        if (null != className) {
            _form._svf.VrsOut("CLASS_NAME", className);
        }

        // 科目名
        String subclassKey = student._classCd + student._curriculumCd + student._subClassCd;
        final String subClassName = _param.getSubClassName(subclassKey);
        if (null != subClassName) {        
            _form._svf.VrsOut("SUBCLASS_NAME", subClassName);
        }
        
        // 評定
        final String gradValue = student.getGradValue();
        if (null != gradValue) {
            _form._svf.VrsOut("GRAD_VALUE", gradValue);
        }

        // 修得単位
        final String getCredit = student.getCredit();
        if (null != getCredit) {
            _form._svf.VrsOut("GET_CREDIT", getCredit);
        }
    }

    private void printHeadFoot(final DB2UDB db2, Student student)   throws SQLException {
        String zipcd = "";
        String prefName = "";
        String addr1 = "";
        String addr2 = "";
        String addr3 = "";

        // 生徒指定時
        if (_param._print.equals(PRINT_SEITO)) {
            // 生徒送り先住所情報取得
            final SchregAddresInfo info = createSchregAddresInfo(db2, student._schregno);

            zipcd = info._zipcd;  // 郵便番号
            addr1 = info._addr1;  // 住所1
            addr2 = info._addr2;  // 住所2
            addr3 = info._addr3;  // 住所3
            prefName = info._prefName;    // 都道府県
        }

        String name1 = "";
        String name2 = "";
        // 保護者又は負担者情報取得
        final GuardianInfo info = createGuardianInfo(db2, student._schregno);

        // 保護者指定時
        if (_param._print.equals(PRINT_GUARD)) {
            zipcd = info._guardZipcd;    // 保護者：郵便番号
            addr1 = info._guardAddr1;    // 保護者：住所1
            addr2 = info._guardAddr2;    // 保護者：住所2
            addr3 = info._guardAddr3;    // 保護者：住所3
            prefName = info._guardPrefName;  // 保護者：都道府県
        // 負担者指定時
        } else if (_param._print.equals(PRINT_GUARANTOR)) {
            zipcd = info._guarantorZipcd;    // 負担者：郵便番号
            addr1 = info._guarantorAddr1;    // 負担者：住所1
            addr2 = info._guarantorAddr2;    // 負担者：住所2
            addr3 = info._guarantorAddr3;    // 負担者：住所3
            prefName = info._guarantorPrefName;  // 負担者：都道府県
        }

        // 生徒または保護者の場合
        if (_param._print.equals(PRINT_SEITO) || _param._print.equals(PRINT_GUARD)) {
            name1 = info._guardName;    // 保護者氏名
        } else {
            name1 = info._guarantorName;    // 負担者氏名
        }
        
        // 生徒氏名
        name2 = info._name;
        
        
        // ヘッダ
        printHeader(student, zipcd, prefName, addr1, addr2, addr3, name1, name2, _param._schoolInfo._schoolName);
        
        // フッタ情報編集
        printFooter();
    }

    private void printHeader(
            final Student student,
            final String zipcd,
            final String prefname,
            final String addr1,
            final String addr2,
            final String addr3,
            final String name1,
            final String name2,
            final String schoolName
    ) {
        // 学校名
        _form._svf.VrsOut("SCHOOLNAME1" ,   schoolName.trim());

        // 対象年度
        _form._svf.VrsOut("NENDO"   ,   _param._nendo);

        // 郵便番号
        _form._svf.VrsOut("GUARD_ZIPCD", zipcd);

        // 住所(1行目)
        _form._svf.VrsOut("GUARD_ADDRESS1_1", prefname + addr1 + addr2);

        // 住所(2行目)
        _form._svf.VrsOut("GUARD_ADDRESS1_2", addr3);

        // 氏名(保護者または負担者の氏名)
        _form._svf.VrsOut("GUARD_NAME", name1);

        // 氏名(生徒)
        _form._svf.VrsOut("NAME1", name2);

        // 学籍番号
        _form._svf.VrsOut("SCHREGNO", student._schregno);

        // 生徒氏名
        _form._svf.VrsOut("NAME2", student._name);

        // 所属
        _form._svf.VrsOut("SCHOOLNAME2", student._schoolName1);
    }

    private void printFooter() {
        // 作成日
        _form._svf.VrsOut("DATE"        , _param._sakuseiDate);

        // 学校名 
        _form._svf.VrsOut("SCHOOLNAME3" , _param._schoolInfo._schoolName);

        // 校長名
        _form._svf.VrsOut("STAFFNAME"   , _param._schoolInfo._principalName);

        // 役職名
        _form._svf.VrsOut("JOBNAME"     , _param._schoolInfo._jobName);
    }

    private List createGradeStudents(final DB2UDB db2)  throws SQLException {
        final List rtn = new ArrayList();
        final String sql = getGradeStudentSql();
        log.debug("SQL =======>>  " + sql);
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final String classCd = rs.getString("CLASSCD");
                final String subClassCd = rs.getString("SUBCLASSCD");
                final String curriculumCd = rs.getString("CURRICULUM_CD");
                final Integer gradValue = KNJServletUtils.getInteger(rs, "GRAD_VALUE");
                final Integer getCregit = KNJServletUtils.getInteger(rs, "GET_CREDIT");
                final Student student = new Student(
                        classCd,
                        curriculumCd,
                        subClassCd,
                        rs.getString("SCHREGNO"),
                        gradValue,
                        getCregit,
                        nvlT(rs.getString("SCHOOLNAME1")),
                        nvlT(rs.getString("NAME"))
                );
                rtn.add(student);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtn;
    }

    private String getGradeStudentSql() {
        final String sql;
        sql = "select"
            + "     W1.SCHREGNO,"
            + "     W2.SCHOOLNAME1,"
            + "     W3.NAME,"
            + "     W5.CLASSCD,"
            + "     W5.CURRICULUM_CD,"
            + "     W5.GRAD_VALUE,"
            + "     W5.GET_CREDIT,"
            + "     W5.SUBCLASSCD"
            + " from  SCHREG_REGD_DAT W1"
            + " left join BELONGING_MST W2 on"
            + "     W1.GRADE  = W2.BELONGING_DIV"
            + " inner join SCHREG_BASE_MST W3 on"
            + "     W1.SCHREGNO      = W3.SCHREGNO"
            + " inner join REC_CREDIT_ADMITS W5 on"
            + "     W1.YEAR          = W5.YEAR and"
            + "     W5.CLASSCD || W5.CURRICULUM_CD || W5.SUBCLASSCD NOT " + _param._inStateSubClassAttend + " and"
            + "     W1.SCHREGNO      = W5.SCHREGNO and"
            + "     '1'              = W5.ADMITS_FLG"
            + " where"
            + "     W1.GRADE in " + SQLUtils.whereIn(true, _param._categorySelected) + " and "
            + "     W1.YEAR     = '" + _param._year +"' and"
            + "     W1.SEMESTER = '" + _param._semester +"'"
            + " order by"
            + "     W1.GRADE, W1.HR_CLASS, W1.SCHREGNO, W5.CLASSCD, W5.CURRICULUM_CD, W5.SUBCLASSCD"
            ;
        return sql;
    }

    private String getSchregStudentSql() {
        final String sql;
        sql = "select"
            + "     W1.SCHREGNO,"
            + "     W1.CLASSCD,"
            + "     W1.CURRICULUM_CD,"
            + "     W1.GRAD_VALUE,"
            + "     W1.GET_CREDIT,"
            + "     W4.SCHOOLNAME1,"
            + "     W5.NAME,"
            + "     W1.SUBCLASSCD"
            + " from  REC_CREDIT_ADMITS W1 "
            + " inner join SCHREG_REGD_DAT W3 on"
            + "     W1.YEAR          = W3.YEAR and"
            + "     W1.SCHREGNO      = W3.SCHREGNO"
            + " left join BELONGING_MST W4 on"
            + "     W3.GRADE  = W4.BELONGING_DIV"
            + " inner join SCHREG_BASE_MST W5 on"
            + "     W1.SCHREGNO      = W5.SCHREGNO"
            + " where"
            + "     W1.YEAR     = '" + _param._year +"' and "
            + "     W1.SCHREGNO in " + SQLUtils.whereIn(true, _param._categorySelected) + " and "
            + "     W1.CLASSCD || W1.CURRICULUM_CD || W1.SUBCLASSCD NOT " + _param._inStateSubClassAttend + " and"
            + "     '1'              = W1.ADMITS_FLG"
            + " order by"
            + "     W3.GRADE, W3.HR_CLASS, W3.SCHREGNO, W1.CLASSCD, W1.CURRICULUM_CD, W1.SUBCLASSCD"
            ;
            return sql;
    }

    private List createSchregStudents(final DB2UDB db2) throws SQLException {
        final List rtn = new ArrayList();
        final String sql = getSchregStudentSql();
        
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final String classCd = rs.getString("CLASSCD");
                final String curriculumCd = rs.getString("CURRICULUM_CD");
                final String subClassCd = rs.getString("SUBCLASSCD");
                final Integer gradValue = KNJServletUtils.getInteger(rs, "GRAD_VALUE");
                final Integer getCregit = KNJServletUtils.getInteger(rs, "GET_CREDIT");
                final Student student = new Student(
                        classCd,
                        curriculumCd,
                        subClassCd,
                        rs.getString("SCHREGNO"),
                        gradValue,
                        getCregit,
                        nvlT(rs.getString("SCHOOLNAME1")),
                        nvlT(rs.getString("NAME"))
                );
                rtn.add(student);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtn;
    }

    /** 生徒クラス */
    private class Student {
        /** 学籍番号. */
        final String _schregno;

        /** 生徒名. */
        final String _name;

        final String _classCd;
        final String _curriculumCd;
        final String _subClassCd;

        /** 評定. */
        final Integer _gradValue;

        /** 修得単位. */
        final Integer _getCredit;

        final String _schoolName1;

        Student(
                final String classCd,
                final String curriculumCd,
                final String subClassCd,
                final String schregno,
                final Integer gradValue,
                final Integer getCredit,
                final String schoolname1,
                final String name
        ) {
            _classCd = classCd;
            _curriculumCd = curriculumCd;
            _subClassCd = subClassCd;
            _schregno = schregno;
            _gradValue = gradValue;
            _getCredit = getCredit;
            _schoolName1 = schoolname1;
            _name = name;
        }

        public String getGradValue() {
            if (_classCd.equals(SOUGOU_KYOUKA) || _param.isW029(_param._year, _subClassCd)) {
                final int gradValue = _gradValue.intValue();
                if (2 <= gradValue) {
                    return "修得";
                } else {
                    return ZENKAKU_HAIFUN;
                }
            }

            return null == _gradValue ? null : _gradValue.toString();
        }

        public String getCredit() {
            final int gradValue = null == _gradValue ? 1 : _gradValue.intValue();
            if (_classCd.equals(SOUGOU_KYOUKA) && 1 == gradValue) {
                return ZENKAKU_HAIFUN;
            }

            return null == _getCredit ? "" : _getCredit.toString();
        }
    }

    /** 
     * 生徒指定時の送り先情報取得
     * @param db2               ＤＢ接続オブジェクト
     * @param serchSchregNo 学籍番号
     * @return
     * @throws SQLException
     */
    private SchregAddresInfo createSchregAddresInfo(final DB2UDB db2, String serchSchregNo) throws SQLException {
        final String sql;
        sql = "select"
            + "   W1.ZIPCD,"
            + "   W1.ADDR1,"
            + "   W1.ADDR2,"
            + "   W1.ADDR3,"
            + "   W2.PREF_NAME"
            + " from"
            + "   SCHREG_ADDRESS_DAT W1"
            + " left join PREF_MST W2 on"
            + "   W1.PREF_CD = W2.PREF_CD"
            + " where"
            + "   W1.SCHREGNO = '" + serchSchregNo + "'"
            ;
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            if (rs.next()) {
                final SchregAddresInfo info;
                info = new SchregAddresInfo(
                        nvlT(rs.getString("ZIPCD")),
                        nvlT(rs.getString("ADDR1")),
                        nvlT(rs.getString("ADDR2")),
                        nvlT(rs.getString("ADDR3")),
                        nvlT(rs.getString("PREF_NAME"))
                );
                return info;
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return null;
    }

    /** 生徒指定時の送り先情報クラス */
    private class SchregAddresInfo {
        final String _zipcd;
        final String _addr1;
        final String _addr2;
        final String _addr3;
        final String _prefName;
        
        SchregAddresInfo(
                final String zipcd,
                final String addr1,
                final String addr2,
                final String addr3,
                final String prefName
        ) {
            _zipcd = zipcd;
            _addr1 = addr1;
            _addr2 = addr2;
            _addr3 = addr3;
            _prefName = prefName;
        }
    }
    
    /**
     * 保護者または学資負担者指定時の送り先情報取得
     * @param db2               ＤＢ接続オブジェクト
     * @param serchSchregNo 学籍番号
     * @return
     * @throws SQLException
     */
    private GuardianInfo createGuardianInfo(final DB2UDB db2, String serchSchregNo) throws SQLException {
        final String sql = getGuardianInfoSql(serchSchregNo);
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            if (rs.next()) {
                final GuardianInfo info;
                info = new GuardianInfo(
                        nvlT(rs.getString("GUARD_NAME")),
                        nvlT(rs.getString("GUARD_ZIPCD")),
                        nvlT(rs.getString("GUARD_PREF_NAME")),
                        nvlT(rs.getString("GUARD_ADDR1")),
                        nvlT(rs.getString("GUARD_ADDR2")),
                        nvlT(rs.getString("GUARD_ADDR3")),
                        nvlT(rs.getString("GUARANTOR_NAME")),
                        nvlT(rs.getString("GUARANTOR_ZIPCD")),
                        nvlT(rs.getString("GUARANTOR_PREF_NAME")),
                        nvlT(rs.getString("GUARANTOR_ADDR1")),
                        nvlT(rs.getString("GUARANTOR_ADDR2")),
                        nvlT(rs.getString("GUARANTOR_ADDR3")),
                        nvlT(rs.getString("NAME"))
                );
                return info;
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return null;
    }
    
    /**
     * 保護者または学資負担者指定時の送り先情報抽出ＳＱＬ生成処理
     * @param serchSchregNo 学籍番号
     * @return
     */
    private String getGuardianInfoSql(String serchSchregNo){
        StringBuffer stb = new StringBuffer();
        
        stb.append(" select");
        stb.append("     W1.GUARD_NAME,");
        stb.append("     W1.GUARD_ZIPCD,");
        stb.append("     W2.PREF_NAME as GUARD_PREF_NAME,");
        stb.append("     W1.GUARD_ADDR1,");
        stb.append("     W1.GUARD_ADDR2,");
        stb.append("     W1.GUARD_ADDR3,");
        stb.append("     W1.GUARANTOR_NAME,");
        stb.append("     W1.GUARANTOR_ZIPCD,");
        stb.append("     W3.PREF_NAME as GUARANTOR_PREF_NAME,");
        stb.append("     W1.GUARANTOR_ADDR1,");
        stb.append("     W1.GUARANTOR_ADDR2,");
        stb.append("     W1.GUARANTOR_ADDR3,");
        stb.append("     W4.NAME");
        stb.append("  from  GUARDIAN_DAT W1");
        stb.append("  left join PREF_MST W2 on");
        stb.append("     W1.GUARD_PREF_CD = W2.PREF_CD");
        stb.append("  left join PREF_MST W3 on");
        stb.append("     W1.GUARANTOR_PREF_CD = W3.PREF_CD");
        stb.append("  inner join SCHREG_BASE_MST W4 on");
        stb.append("     W1.SCHREGNO = W4.SCHREGNO");
        stb.append("  where");
        stb.append("     W1.SCHREGNO = '" + serchSchregNo + "'");

        return stb.toString();
    }

    /** 保護者または学資負担者指定時の送り先情報クラス */
    private class GuardianInfo {
        /** 保護者氏名. */
        final String _guardName;
        final String _guardZipcd;
        final String _guardPrefName;
        final String _guardAddr1;
        final String _guardAddr2;
        final String _guardAddr3;

        /** 負担者氏名. */
        final String _guarantorName;
        final String _guarantorZipcd;
        final String _guarantorPrefName;
        final String _guarantorAddr1;
        final String _guarantorAddr2;
        final String _guarantorAddr3;

        final String _name;
        
        GuardianInfo(
                final String guardName,
                final String guardZipcd,
                final String guardPrefName,
                final String guardAddr1,
                final String guardAddr2,
                final String guardAddr3,
                final String guarantorName,
                final String guarantorZipcd,
                final String guarantorPrefName,
                final String guarantorAddr1,
                final String guarantorAddr2,
                final String guarantorAddr3,
                final String name
        ) {
            _guardName = guardName;
            _guardZipcd = guardZipcd;
            _guardPrefName = guardPrefName;
            _guardAddr1 = guardAddr1;
            _guardAddr2 = guardAddr2;
            _guardAddr3 = guardAddr3;
            _guarantorName = guarantorName;
            _guarantorZipcd = guarantorZipcd;
            _guarantorPrefName = guarantorPrefName;
            _guarantorAddr1 = guarantorAddr1;
            _guarantorAddr2 = guarantorAddr2;
            _guarantorAddr3 = guarantorAddr3;
            _name = name;
        }
    }

    /** 学校情報クラス */
    private class SchoolInfo {
        final String _schoolName;
        final String _principalName;
        final String _jobName;

        SchoolInfo(
                final String schoolName,
                final String principalName,
                final String jobName
        ) {
            _schoolName = schoolName;
            _principalName = principalName;
            _jobName = jobName;
        }
    }

    private static final String PRINT_SEITO = "1";
    private static final String PRINT_GUARD = "2";
    private static final String PRINT_GUARANTOR = "3";

    private class Param {
        private final String _year;
        private final String _semester;

        /** 個人設定か?. falseなら所属設定. */
        private final boolean _is生徒指定;

        private final String[] _categorySelected;

        /** 1=生徒, 2=保護者, 3=負担者 */
        private final String _print;

        private final String _sakuseiDate;
        private final String _nendo;

        private SchoolInfo _schoolInfo;

        /** 教科マスタ。<String:教科コード, String:教科名> */
        private final Map _classMst = new HashMap();

        /** 科目マスタ。<String:科目コード, String:科目名> */
        private final Map _subClassMst = new HashMap();
        
        /** 体育読替元IN文 */
        private final String _inStateSubClassAttend;

        final Map _w029Map;

        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");

            final String loginDate = request.getParameter("LOGIN_DATE");
            _sakuseiDate = KNJ_EditDate.h_format_JP(loginDate);

            final String div = request.getParameter("DIV");
            _is生徒指定 = ("1".equals(div)) ? true : false;

            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _print = request.getParameter("PRINT");

            _nendo = KenjaProperties.gengou(Integer.parseInt(_year)) + "年度";

            final StringBuffer inState = new StringBuffer();
            String sep = "";
            inState.append(" IN (");
            for (final Iterator iter = WithusUtils.PHYSICAL_EDUCATIONS_NEW_LIST.iterator(); iter.hasNext();) {
                final String subClassCd = (String) iter.next();
                inState.append(sep + "'" + WithusUtils.PHYSICAL_EDUCATION_CLASS_CD + WithusUtils.PHYSICAL_EDUCATION_NEW_CURRICULUM + subClassCd + "'");
                sep = ",";
            }
            for (final Iterator iter = WithusUtils.PHYSICAL_EDUCATIONS_LIST.iterator(); iter.hasNext();) {
                final String subClassCd = (String) iter.next();
                inState.append(sep + "'" + WithusUtils.PHYSICAL_EDUCATION_CLASS_CD + WithusUtils.PHYSICAL_EDUCATION_CURRICULUM + subClassCd + "'");
                sep = ",";
            }
            for (final Iterator iter = WithusUtils.PHYSICAL_EDUCATIONS_OLD_LIST.iterator(); iter.hasNext();) {
                final String subClassCd = (String) iter.next();
                inState.append(sep + "'" + WithusUtils.PHYSICAL_EDUCATION_CLASS_CD + WithusUtils.PHYSICAL_EDUCATION_OLD_CURRICULUM + subClassCd + "'");
            }
            inState.append(" ) ");
            _inStateSubClassAttend = inState.toString();
            _w029Map = new TreeMap();
        }

        private Map loadW029Map(final DB2UDB db2) throws SQLException {
            ResultSet rs = null;
            String sql = "SELECT * FROM V_NAME_MST WHERE NAMECD1 = 'W029' ORDER BY YEAR, NAMECD1 ";
            try {
                db2.query(sql);
                rs = db2.getResultSet();
                Map setNamecd2Map = new HashMap();
                String befYear = "";
                while (rs.next()) {
                    if (!befYear.equals(rs.getString("YEAR"))) {
                        setNamecd2Map = new HashMap();
                    }
                    setNamecd2Map.put(rs.getString("NAME1"), rs.getString("NAMECD2"));
                    _w029Map.put(rs.getString("YEAR"), setNamecd2Map);
                    befYear = rs.getString("YEAR");
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return _w029Map;
        }

        private boolean isW029(final String year, final String subclassCd) {
            if (!_w029Map.containsKey(year)) {
                return false;
            }
            final Map setNamecd2Map = (Map) _w029Map.get(year);
            return setNamecd2Map.containsKey(subclassCd);
        }

        public String getClassName(final String classCd) {
            return (String) _classMst.get(classCd);
        }

        public String getSubClassName(final String subClassCd) {
            return (String) _subClassMst.get(subClassCd);
        }

        /**
         * 科目を読み込む。
         * @param db2 DB
         */
        public void loadSubClass(DB2UDB db2) throws SQLException {
            final String sql;
            sql = "select"
                + "   CLASSCD,"
                + "   CURRICULUM_CD,"
                + "   SUBCLASSCD,"
                + "   SUBCLASSNAME"
                + " from SUBCLASS_MST"
                ;
            db2.query(sql);
            final ResultSet rs = db2.getResultSet();
            try {
                while (rs.next()) {
                    _subClassMst.put(
                            rs.getString("CLASSCD") + rs.getString("CURRICULUM_CD") + rs.getString("SUBCLASSCD"),
                            rs.getString("SUBCLASSNAME")
                    );                  
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }             
        }

        /**
         * 教科を読み込む。
         * @param db2 DB
         */
        public void loadClass(DB2UDB db2) throws SQLException {
            final String sql;
            sql = "select"
                + "   CLASSCD,"
                + "   CLASSNAME"
                + " from CLASS_MST"
                ;
            db2.query(sql);
            final ResultSet rs = db2.getResultSet();
            try {
                while (rs.next()) {
                    _classMst.put(
                            rs.getString("CLASSCD"),
                            rs.getString("CLASSNAME")
                    );
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }           
        }

        private void loadSchoolInfo(final DB2UDB db2)   throws SQLException {
            final String sql;
            sql = "select"
                + "   SCHOOL_NAME,"
                + "   PRINCIPAL_NAME,"
                + "   JOB_NAME"
                + " from CERTIF_SCHOOL_DAT"
                + " where"
                + "   YEAR = '" + _param._year + "' and"
                + "   CERTIF_KINDCD = '309'"
                ;
            db2.query(sql);
            final ResultSet rs = db2.getResultSet();
            try {
                while (rs.next()) {
                    final SchoolInfo schoolInfo = new SchoolInfo(
                            nvlT(rs.getString("SCHOOL_NAME")),
                            nvlT(rs.getString("PRINCIPAL_NAME")),
                            nvlT(rs.getString("JOB_NAME"))
                    );
                    _schoolInfo = schoolInfo;
                    break;
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
        }
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
    private class Form {
        private Vrw32alp _svf;

        public Form(final String file,final HttpServletResponse response) throws IOException {
            _svf = new Vrw32alp();

            if (_svf.VrInit() < 0) {
                throw new IllegalStateException("svf初期化失敗");
            }
            _svf.VrSetSpoolFileStream(response.getOutputStream());
            response.setContentType("application/pdf");

            _svf.VrSetForm(FORM_FILE, 4);
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
