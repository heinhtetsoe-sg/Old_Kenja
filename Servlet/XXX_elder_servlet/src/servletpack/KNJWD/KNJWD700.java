// kanji=漢字
/*
 * $Id: 46cfe61645d1cf7ec2405e364987b6a66f60b88b $
 *
 * 作成日: 2007/11/20 17:20:00 - JST
 * 作成者: nakada
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWD;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * 生徒別履修科目一覧
 * @author nakada
 * @version $Id: 46cfe61645d1cf7ec2405e364987b6a66f60b88b $
 */
public class KNJWD700 {
    /* pkg */static final Log log = LogFactory.getLog(KNJWD700.class);

    private static final String FORM_FILE = "KNJWD700.frm";

    /* 
     * 文字数による出力項目切り分け基準 
     */
    /** 氏名 */
    private static final int NAME1_LENG = 20;
    /** 担当名 */
    private static final int STAFFNAME1_LENG = 20;

    /* 
     * 項目見出し
     */
    /** 単位 */
    private static final String UNIT_NAME = "単位";
    /** 計 */
    private static final String TOTAL_NAME = "計";

    /*
     * 所属指定
     */
    // 全件
    private static final String GRADE_ALL = "0";

    /*
     * 伝票明細件数ＭＡＸ
     */
    /** 伝票横件数ＭＡＸ */
    private static final int COLUMN_MAX = 60;
    /** 伝票明細件数ＭＡＸ */
    private static final int DETAILS_MAX = 40;

    private Form _form;
    private Vrw32alp _svf;

    private DB2UDB db2;

    private boolean _hasData;

    Param _param;

    private int _page;
    private int _totalPage;
    private int _cntPerGrade;
    private int _effectivelyCnt;
    private int _saidePage;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        dumpParam(request);
        _param = createParam(request);
        _form = new Form(FORM_FILE, response, _svf);
        db2 = null;

        try {
            final String dbName = request.getParameter("DBNAME");
            db2 = new DB2UDB(dbName, "db2inst1", "db2inst1", DB2UDB.TYPE2);
            if (openDb(db2)) {
                return;
            }

            _param.load(db2);

            // 対象データ全件取り込み
            final List schregRegdDats = createSchregRegdDats(db2);
            // 所属別対象データ件数取得用
            final List schregRegdDats2 = schregRegdDats;

            // 所属ブレーク判定用※初期化
            String oldGrade = "";
            String oldAnnual = "";

            // 所属別データ格納用配列作成
            List lst = new ArrayList();

            boolean initCompletFlg = false; // 初回処理済フラグ

            int i = 0;
            _cntPerGrade = 0;    // 所属別対象データ件数格納用
            for (Iterator it = schregRegdDats.iterator(); it.hasNext();) {
                final SchregRegdDat applicant = (SchregRegdDat) it.next();

                // ※初回はかならず入る
                if ((!applicant._grade.equals(oldGrade)) ||
                        (!applicant._annual.equals(oldAnnual))) {
                    if (initCompletFlg) {
                        printMain(lst, _cntPerGrade, oldGrade, oldAnnual);
                    } else {
                        // 初回は、印字しない
                        initCompletFlg = true;
                    }

                    // 所属+年次 別対象データ(学生数)件数取得
                    getCntPerGrade(schregRegdDats2, applicant);

                    // 所属別データ格納用配列初期化
                    lst = getLst();

                    i = 0;
                    _page = 0;
                    _totalPage = 0;
                    
                    oldGrade = applicant._grade;
                    oldAnnual = applicant._annual;
                    
                }

                // 所属別対象データを配列に格納※lstを使いまわし、更新していく。
                lst = setArrayCompCredit(lst, applicant, i);

                i++;
            }

            // 未印字データがあれば、印字する。
            if (i > 0) {
                printMain(lst, _cntPerGrade, oldGrade, oldAnnual);
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            _form.closeSvf();
            closeDb(db2);
        }
    }

    private List getLst() {
        List lst;
        String[] staffname = new String[_cntPerGrade];
        String[] schregno = new String[_cntPerGrade];
        String[] name = new String[_cntPerGrade];
        int[][][][] compCredit = new int[_cntPerGrade][_param._classcdArrayNum][_param._subclassArrayNum][_param._curriculumArrayNum];
        boolean[][][] compCreditSet = new boolean[_param._classcdArrayNum][_param._subclassArrayNum][_param._curriculumArrayNum];

        lst = new ArrayList();
        lst.add(staffname);
        lst.add(schregno);
        lst.add(name);
        lst.add(compCredit);
        lst.add(compCreditSet);

        return lst;
    }

    private void getCntPerGrade(final List schregRegdDats2, final SchregRegdDat applicant) {
        _cntPerGrade = 0;
        for (Iterator it2 = schregRegdDats2.iterator(); it2.hasNext();) {
            final SchregRegdDat applicant2 = (SchregRegdDat) it2.next();

            if (applicant2._grade.equals(applicant._grade) &&
                    applicant2._annual.equals(applicant._annual)) {
                _cntPerGrade++;
            }
        }
    }

    private List setArrayCompCredit(List lst, SchregRegdDat applicant, final int i) {
        String[] staffname = (String[]) lst.get(0);
        String[] schregno = (String[]) lst.get(1);
        String[] name = (String[]) lst.get(2);   
        int[][][][] compCredit = (int[][][][]) lst.get(3);
        boolean[][][] compCreditSet = (boolean[][][]) lst.get(4);

        staffname[i] = _param._schregRegdHdatMapString(_param._year
                + _param._semester
                + applicant._grade
                + applicant._hrClass);

        schregno[i] = applicant._schregno;
        name[i] = applicant._name;

        // 該当する教科、科目、教育課程に履修単位を格納
        for (Iterator it = applicant._compRegistDats.iterator(); it.hasNext();) {
            final CompRegistDat compRegistDat = (CompRegistDat) it.next();

            for (int j = 0; j < _param._classcdArrayNum; j++) {
                if (compRegistDat._classcd.equals(_param._classcdArray[j])) {
                    for (int k = 0; k < _param._subclassArrayNum; k++) {
                        if (compRegistDat._subclasscd.equals(_param._subclassArray[j][k])) {
                            for (int m = 0; m < _param._curriculumArrayNum; m++) {
                                if (compRegistDat._curriculumCd.equals(_param._curriculumArray[j][k][m])) {

                                    compCredit[i][j][k][m] = compRegistDat._compCredit;
                                    compCreditSet[j][k][m] = true;

                                    break;
                                }
                            }

                            break;
                        }
                    }

                    break;
                }
            }
        }

        // 追加済み配列を返却
        lst = new ArrayList();
        lst.add(staffname);
        lst.add(schregno);
        lst.add(name);
        lst.add(compCredit);
        lst.add(compCreditSet);

        return lst;
    }

    private void printMain(
            final List lst,
            final int cntPerGrade,
            final String grade,
            final String annual
    ) {

        boolean[][][] compCreditSet = (boolean[][][]) lst.get(4);
        
        // 一人でも履修単位が設定されていたら有効データとしてカウント
        setEffectivelyCnt(compCreditSet);

        // 有効データ件数＋１(単位計 分)と最大列数より、横ページ数を取得。
        _saidePage = (_effectivelyCnt + 1) / COLUMN_MAX;

        if (((_effectivelyCnt + 1) % COLUMN_MAX) != 0) {
            _saidePage++;
        }

        int cnt = cntPerGrade / DETAILS_MAX;

        if ((cntPerGrade % DETAILS_MAX) != 0) {
            cnt++;
        }

        // 総ページ数算出
        _totalPage = cnt * _saidePage;

        printHeader(grade, annual);
        printApplicant(lst, grade, annual);
    }

    private void setEffectivelyCnt(boolean[][][] compCreditSet) {
        _effectivelyCnt = 0;
        for (int j = 0; j < _param._classcdArrayNum; j++) {
            for (int k = 0; k < _param._subclassArrayNum; k++) {
                for (int m = 0; m < _param._curriculumArrayNum; m++) {
                    if (compCreditSet[j][k][m]) {
                        _effectivelyCnt++;
                    }
                }
            }
        }
    }

    private void printHeader(final String Grade, final String Annual) {
        // 年度 
        int year = Integer.valueOf(_param._year).intValue();
        String date = nao_package.KenjaProperties.gengou(year);
        _form._svf.VrsOut("NENDO", date.toString() + "年度");

        // ページ
        _form._svf.VrsOut("PAGE", Integer.toString(++_page));
        // 総ページ
        _form._svf.VrsOut("TOTALPAGE", Integer.toString(_totalPage));
        // 所属
        _form._svf.VrsOut("SCHOOLNAME", _param._belongingMapString(Grade));
        // 年次
        if ((Annual != null) && !Annual.equals("")) {
            int int1 = Integer.parseInt(Annual);
            _form._svf.VrsOut("ANNUAL", Integer.toString(int1));
        }
        /* 作成日 */
        _form._svf.VrsOut("DATE", getJDate(_param._loginDate));
    }

    private void printApplicant(List lst, final String grade, final String annual) {
        String[] staffname = (String[]) lst.get(0);
        String[] schregno = (String[]) lst.get(1);
        String[] name = (String[]) lst.get(2);   

        _form._svf.VrAttribute( "RECORD1", "Print=1");
        _form._svf.VrAttribute( "RECORD2", "Print=0");  // 空行
        _form._svf.VrAttribute( "SCHOOLNAME", "FF=1");  // 自動改ページ
        _form._svf.VrAttribute( "ANNUAL", "FF=1");        // 自動改ページ

        lineClear();

        prtDetail(lst, grade, annual, staffname, schregno, name);
    }

    private void prtDetail(List lst, final String grade, final String annual, String[] staffname, String[] schregno, String[] name) {
        TotalArea totalArea = new TotalArea(
                new int[_param._classcdArrayNum][_param._subclassArrayNum][_param._curriculumArrayNum],
                new int[_cntPerGrade]); 
        int line = 0;   // 印字行を示す
        int start = 0;  // 印字対象データの配列位置を示す。
        String svStaffname = "";    // 同一担当者名を印字しない為の名称退避用
        for (int i = 0; i < _cntPerGrade; i++) {
            line++;

            if (line > DETAILS_MAX) {
                totalArea = printUnit(lst, start, i - 1, totalArea, grade, annual);

                line = 1;
                start = i;

                lineClear();

                printHeader(grade, annual);
            }

            // 1行目の印字時、または、名称が異なる場合に担当者名を印字する。
            if (line == 1 || !staffname[i].equals(svStaffname)) {
                /* 担当 */
                svStaffname = staffname[i];

                _form.printStaffName(line, svStaffname);
            }

            /* 学籍番号 */
            _form._svf.VrsOutn("SCHREGNO", line, schregno[i]);
            /* 氏名 */
            _form.printName(line, name[i]);

            _hasData = true;
        }

        if (line > 0) {
            totalArea = printUnit(lst, start, _cntPerGrade - 1, totalArea, grade, annual);
        }
    }

    private void lineClear() {
        // クリア
        for (int j = 0; j < DETAILS_MAX; j++) {
            /* 担当 */
            _form._svf.VrsOutn("STAFFNAME1", (j + 1), "");
            /* 学籍番号 */
            _form._svf.VrsOutn("SCHREGNO", (j + 1), "");
            /* 氏名 */
            _form._svf.VrsOutn("NAME1", (j + 1), "");
        }
    }

    private TotalArea printUnit(List lst, int start, int end, TotalArea pTotalArea, final String grade, final String annual) {
        int[][][][] compCredit = (int[][][][]) lst.get(3);
        boolean[][][] compCreditSet = (boolean[][][]) lst.get(4);
        TotalArea totalArea = pTotalArea;

        int colnum = 0; // 印字行を示す。
        for (int j = 0; j < _param._classcdArrayNum; j++) {
            for (int k = 0; k < _param._subclassArrayNum; k++) {
                for (int m = 0; m < _param._curriculumArrayNum; m++) {
                    // 同一所属、年次全体の中で、履修単位があるものだけを印字する。
                    colnum = prtUnit(start, end, grade, annual, compCredit, compCreditSet, totalArea, colnum, j, k, m);
                }
            }
        }

        _form._svf.VrEndRecord();

        _form._svf.VrAttribute( "RECORD1", "Print=0");
        _form._svf.VrAttribute( "RECORD2", "Print=1");

        // 空打行数の算出。
        int dummyLineNum = getDummyLineNum(grade, annual, colnum);

        // 次ページの頭から空打ちする場合は、ヘッダーを印字する。
        if (colnum >= COLUMN_MAX) {
            printHeader(grade, annual);
        }

        prtDummyLine(dummyLineNum);

        _form._svf.VrEndRecord();
        _form._svf.VrAttribute( "RECORD1", "Print=1");
        _form._svf.VrAttribute( "RECORD2", "Print=0");

        // 単位計列の印字
        prtCreditTotal(start, end, totalArea);

        _form._svf.VrEndRecord();

        return totalArea;
    }

    private void prtDummyLine(int dummyLineNum) {
        for (int j = dummyLineNum; j > 0; j--) {
            _form._svf.VrAttribute( "KARA", "Print=1");
            _form._svf.VrsOut("KARA", "空行");
            _form._svf.VrEndRecord();
        }
    }

    private void prtCreditTotal(int start, int end, TotalArea totalArea) {
        /* 教科 */
        _form._svf.VrsOut("CLASSABBV", UNIT_NAME);
        /* 科目 */
        _form._svf.VrsOut("SUBCLASSABBV", TOTAL_NAME);

        int p1 = 0;
        for (int i = start; i <= end; i++) {
            p1++;

            if (totalArea._schregUnitTotal[i] != 0) {
                /* 単位計 */
                _form._svf.VrsOut("COMP_REDIT" + p1, Integer.toString(totalArea._schregUnitTotal[i]));
            }
        }
    }

    private int getDummyLineNum(final String grade, final String annual, int colnum) {
        int dummyLineNum = 0;
        if (colnum < COLUMN_MAX) {
            dummyLineNum = COLUMN_MAX - colnum - 1;
        } else {
            dummyLineNum = COLUMN_MAX -1;
        }

        return dummyLineNum;
    }

    private int prtUnit(int start, int end, final String grade, final String annual, int[][][][] compCredit, boolean[][][] compCreditSet, TotalArea totalArea, int colnum, int j, int k, int m) {
        if (compCreditSet[j][k][m]) {
            /* 教科 */
            _form._svf.VrsOut("CLASSABBV", _param._classMstMapString(_param._classcdArray[j]));
            /* 科目 */
            _form._svf.VrsOut("SUBCLASSABBV",
                    _param._subclassMstMapString(
                    _param._classcdArray[j] + _param._curriculumArray[j][k][m] + _param._subclassArray[j][k]));

            colnum++;
            if (colnum > COLUMN_MAX) {
                printHeader(grade, annual);
                colnum = 1;
            }

            // 同一教科、科目、教育課程について、当該ページ対象の各学生の履修単位を印字する。
            printCredit(start, end, compCredit, totalArea, j, k, m);

            _form._svf.VrEndRecord();
        }
        return colnum;
    }

    private void printCredit(int start, int end, int[][][][] compCredit, TotalArea totalArea, int j, int k, int m) {
        int p = 0;  // 帳票項目「COMP_REDIT」のサフィックス※COMP_REDIT1,COMP_REDIT2.....
        for (int i = start; i <= end; i++) {
            p++;    
            if (compCredit[i][j][k][m] != 0) {
                _form._svf.VrsOut("COMP_REDIT" + Integer.toString(p), Integer.toString(compCredit[i][j][k][m]));
                totalArea._schregUnitTotal[i] += compCredit[i][j][k][m];
                totalArea._compCreditTotal[j][k][m] ++;
            }

            // 最終ページのときは、所属計も印字する。※横ページが2ページのときは、最終からの2ページ分
            if (i == _cntPerGrade - 1) {
                if (totalArea._compCreditTotal[j][k][m] != 0) {
                    // 所属計
                    _form._svf.VrsOut("COUNT", Integer.toString(totalArea._compCreditTotal[j][k][m]));
                }               
            }
        }
    }

    private static String getJDate(String date) {
        try {
            final Calendar cal = KNJServletUtils.parseDate(date);
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1;
            int dom = cal.get(Calendar.DAY_OF_MONTH);
            
            return nao_package.KenjaProperties.gengou(year, month, dom);
        } catch (final Exception e) {
            return null;
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

    private class TotalArea {
        private final int[][][] _compCreditTotal;
        private final int[] _schregUnitTotal;

        public TotalArea(final int[][][] compCreditTotal, final int[] schregUnitTotal) {
            _compCreditTotal = compCreditTotal;
            _schregUnitTotal = schregUnitTotal;
        }
    }

    // ======================================================================
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _programId;
        private final String _dbName;
        private final String _loginDate;
        private final String _grade;

        private Map _belongingMap;
        private Map _subclassMstMap;
        private Map _classMstMap;
        private Map _schregRegdHdatMap;
        private List _CompCreditTitles;         // 当年度の教科、科目、教育課程のコード＋名称のリスト
        private String[] _classcdArray;         // 当年度の教科コードリスト
        private int _classcdArrayNum;           // _classcdArrayの格納件数
        private String[][] _subclassArray;      // 当年度の科目コードリスト（[教科][科目]）
        private int _subclassArrayNum;          // _subclassArrayの格納件数（最大件数）
        private String[][][] _curriculumArray;  // 当年度の教育課程コードリスト（[教科][科目][教育課程]）
        private int _curriculumArrayNum;        // _curriculumArrayの格納件数（最大件数）

        public Param(
                final String year,
                final String semester,
                final String programId,
                final String dbName,
                final String loginDate,
                final String grade
        ) {
            _year = year;
            _semester = semester;
            _programId = programId;
            _dbName = dbName;
            _loginDate = loginDate;
            _grade = grade;
        }

        public void load(DB2UDB db2) throws SQLException {
            _belongingMap = createBelongingDat(db2);
            _subclassMstMap = createSubclassMst(db2);
            _classMstMap = createClassMst(db2);
            _schregRegdHdatMap = createSchregRegdHdat(db2);

            // 当年度の教科、科目、教育課程のコード＋名称のリストを取得
            _CompCreditTitles = createCompCreditTitleList();

            // 上記のリストを多次元配列（基本マップ）化
            List lst = createCompCreditArray(_CompCreditTitles);
            _classcdArray = (String[]) lst.get(0);
            _classcdArrayNum = ((Integer) lst.get(1)).intValue();

            _subclassArray= (String[][]) lst.get(2);
            _subclassArrayNum = ((Integer) lst.get(3)).intValue();

            _curriculumArray = (String[][][]) lst.get(4);
            _curriculumArrayNum = ((Integer) lst.get(5)).intValue();

            return;
        }

        public String _belongingMapString(String code) {
            return (String) nvlT((String)_belongingMap.get(code));
        }

        public String _subclassMstMapString(String code) {
            return (String) nvlT((String)_subclassMstMap.get(code));
        }

        public String _classMstMapString(String code) {
            return (String) nvlT((String)_classMstMap.get(code));
        }

        public String _schregRegdHdatMapString(String code) {
            return nvlT((String)_schregRegdHdatMap.get(code));
        }

        private Map getNameMst(String nameCd1) throws SQLException {
            final String sql = sqlNameMst(nameCd1);
            final Map rtn = new HashMap();

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String code = rs.getString("code");
                    final String name = rs.getString("name");
                    rtn.put(code, name);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            return rtn;
        }

        private String sqlNameMst(String nameCd1) {
            return " select"
                    + "    NAMECD2 as code,"
                    + "    value(NAMESPARE1, '') as name"
                    + " from"
                    + "    V_NAME_MST"
                    + " where"
                    + "    year = '" + _year + "' AND"
                    + "    nameCd1 = '" + nameCd1 + "'"
                    ;
        }

        private List createCompCreditTitleList ()
            throws SQLException {

            final List rtn = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;

            ps = db2.prepareStatement(sqlCompCreditTitleList());
            rs = ps.executeQuery();
            while (rs.next()) {
                final String classcd = rs.getString("classcd");
                final String curriculumCd = rs.getString("curriculumCd");
                final String subclasscd = rs.getString("subclasscd");

                final CompCreditTitle compCreditTitle = new CompCreditTitle(
                        classcd,
                        _param._classMstMapString(classcd),
                        curriculumCd,
                        subclasscd,
                        _param._subclassMstMapString(classcd + curriculumCd + subclasscd)
                );

                rtn.add(compCreditTitle);
            }

            return rtn;
        }

        private String sqlCompCreditTitleList() {
            final String sql;

            sql = " select"
                + "    CLASSCD as classcd,"
                + "    CURRICULUM_CD as curriculumCd,"
                + "    SUBCLASSCD as subclasscd"
                + " from"
                + "    COMP_REGIST_DAT"
                + " where"
                + "    YEAR = '" + _param._year + "'"
                + " group by CLASSCD, SUBCLASSCD, CURRICULUM_CD"
                + " order by CLASSCD, SUBCLASSCD, CURRICULUM_CD"
                ;

            return sql;
        }

        private List createCompCreditArray(List compCreditTitles) {
            final List rtn = new ArrayList();

            int classNum = 0;

            int subclassNum = 0;
            int subclassMaxNum = 0;

            int curriculumNum = 0;
            int curriculumMaxNum = 0;

            String oldClasscd = "";
            String oldSubclasscd = "";
            String oldCurriculumCd = "";

            CompCreditTitle compCreditTitle = null;
            if (!compCreditTitles.isEmpty()) {
                // 最初のの教科、科目、教育課程リストを取得
                compCreditTitle = (CompCreditTitle) compCreditTitles.get(0);
            }

            int i = 0;

            // 当年度の教科、科目、教育課程の各件数を取得 ----------------------------------------->
            classNum = 0;
            while ((!compCreditTitles.isEmpty()) &&
                    (i < compCreditTitles.size())) {

                classNum++;

                oldClasscd = compCreditTitle._classcd;
                subclassNum = 0;
                while ((i < compCreditTitles.size()) &&
                        (compCreditTitle._classcd.equals(oldClasscd))
                ) {
                    // 同一教科の中に異なる科目が幾つあるか？
                    if (!compCreditTitle._subclasscd.equals(oldSubclasscd)) {
                        subclassNum++;
                    }

                    oldSubclasscd = compCreditTitle._subclasscd;
                    curriculumNum = 0;
                    while ((i < compCreditTitles.size()) &&
                            (compCreditTitle._classcd.equals(oldClasscd)) &&
                            (compCreditTitle._subclasscd.equals(oldSubclasscd))
                    ) {
                        // 同一教科、科目の中に異なる教育課程が幾つあるか？
                        if (!compCreditTitle._curriculumCd.equals(oldCurriculumCd)) {
                            curriculumNum++;
                        }

                        oldCurriculumCd = compCreditTitle._curriculumCd;
                        while ((i < compCreditTitles.size()) &&
                                (compCreditTitle._classcd.equals(oldClasscd)) &&
                                (compCreditTitle._subclasscd.equals(oldSubclasscd)) &&
                                (compCreditTitle._curriculumCd.equals(oldCurriculumCd))
                        ) {
                            i++;
                            if (i < compCreditTitles.size()) {
                                // 次の教科、科目、教育課程リストを取得
                                compCreditTitle = (CompCreditTitle) compCreditTitles.get(i);
                            }
                        }
                    }

                    // 教育課程の最大件数を取得
                    if (curriculumNum > curriculumMaxNum) {
                        curriculumMaxNum = curriculumNum;
                    }
                }            

                // 科目の最大件数を取得
                if (subclassNum > subclassMaxNum) {
                    subclassMaxNum = subclassNum;
                }
            }

            // 上記で確定された、当年度の教科、科目、教育課程の各配列数で配列を定義し、
            // 実際の当年度の教科、科目、教育課程の配列を作成。
            // ※科目、教育課程は、最大件数で、一律作成。
            // ※この配列（いわゆる3次元マップ）を基に、
            // 　所属別年次別学生別に履修単位を集計する。
            String[] classcdArray = new String[classNum];
            String[][] subclassArray = new String[classNum][subclassMaxNum];
            String[][][] curriculumArray = new String[classNum][subclassMaxNum][curriculumMaxNum];

            if (!compCreditTitles.isEmpty()) {
                compCreditTitle = (CompCreditTitle) compCreditTitles.get(0);
            }

            i = 0;
            int j = 0;  // 教科
            int k = 0;  // 科目
            int m = 0;  // 教育課程

            while ((!compCreditTitles.isEmpty()) &&
                    (i < compCreditTitles.size())) {

                // 教科コード格納
                classcdArray[j] = compCreditTitle._classcd;

                oldClasscd = compCreditTitle._classcd;
                // 同一　教科の間繰り返し処理する。
                while ((i < compCreditTitles.size()) &&
                        (compCreditTitle._classcd.equals(oldClasscd))
                ) {
                    // 科目コード格納
                    subclassArray[j][k] = compCreditTitle._subclasscd;

                    oldSubclasscd = compCreditTitle._subclasscd;
                    // 同一　教科コード＋科目コードの間繰り返し処理する。
                    while ((i < compCreditTitles.size()) &&
                            (compCreditTitle._classcd.equals(oldClasscd)) &&
                            (compCreditTitle._subclasscd.equals(oldSubclasscd))
                    ) {
                        // 教育課程コード格納
                        curriculumArray[j][k][m] = compCreditTitle._curriculumCd;

                        oldCurriculumCd = compCreditTitle._curriculumCd;
                        // 同一　教科コード＋科目コード＋教育課程コードの間処理する。
                        while ((i < compCreditTitles.size()) &&
                                (compCreditTitle._classcd.equals(oldClasscd)) &&
                                (compCreditTitle._subclasscd.equals(oldSubclasscd)) &&
                                (compCreditTitle._curriculumCd.equals(oldCurriculumCd))
                        ) {
                            // 次の教科、科目、教育課程リストを取得
                            i++;
                            if (i < compCreditTitles.size()) {
                                compCreditTitle = (CompCreditTitle) compCreditTitles.get(i);
                            }
                        }

                        // 教育課程の件数カウント
                        m++;
                    }

                    m = 0;

                    // 科目の件数カウント
                    k++;
                }            

                k = 0;

                // 教科の件数カウント
                j++;
            }

            rtn.add(classcdArray);
            rtn.add(new Integer(classNum));
            rtn.add(subclassArray);
            rtn.add(new Integer(subclassMaxNum));
            rtn.add(curriculumArray);
            rtn.add(new Integer(curriculumMaxNum));

            return rtn;
        }
    }

    private class CompCreditTitle {
        private final String _classcd;
        private final String _className;
        private final String _curriculumCd;
        private final String _subclasscd;
        private final String _subclassName;

        CompCreditTitle(
                final String classcd,
                final String className,
                final String curriculumCd,
                final String subclasscd,
                final String subclassName
        ) {
            _classcd = classcd;
            _className = className;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _subclassName = subclassName;
        }
    }

    private Param createParam(final HttpServletRequest request) {
        final String year = request.getParameter("YEAR");
        final String semester = request.getParameter("SEMESTER");
        final String programId = request.getParameter("PRGID");
        final String dbName = request.getParameter("DBNAME");
        final String loginDate = request.getParameter("LOGIN_DATE");
        final String grade = request.getParameter("GRADE");

        final Param param = new Param
        (
                year,
                semester,
                programId,
                dbName,
                loginDate,
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

        public Form(final String file,final HttpServletResponse response,
                final Vrw32alp svf) throws IOException {
            _svf = new Vrw32alp();

            if (_svf.VrInit() < 0) {
                throw new IllegalStateException("svf初期化失敗");
            }
            _svf.VrSetSpoolFileStream(response.getOutputStream());
            response.setContentType("application/pdf");

            _svf.VrSetForm(FORM_FILE, 4);
        }

        public void printName(int i, String pName) {
            String name = pName;

            if (name != null) {
                final String label;
                if (name.length() <= NAME1_LENG) {
                    label = "NAME1";
                } else {
                    label = "NAME2_1";
                }
                _form._svf.VrsOutn(label, i, name);
            }
        }

        public void printStaffName(int i, String pName) {
            String name = pName;

            if (name != null) {
                final String label;
                if (name.length() <= STAFFNAME1_LENG) {
                    label = "STAFFNAME1";
                } else {
                    label = "STAFFNAME2_1";
                }
                _form._svf.VrsOutn(label, i, name);
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
     * 生徒。学籍在籍データ。
     */
    private class SchregRegdDat {
        private final String _schregno;
        private final String _grade;
        private final String _hrClass;
        private final String _annual;
        private final String _name;

        private List _compRegistDats;

        SchregRegdDat(
                final String schregno,
                final String grade,
                final String hrClass,
                final String annual,
                final String name
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _annual = annual;
            _name = name;
        }

        public void load(DB2UDB db2) throws SQLException {
            _compRegistDats = createCompRegistDats(db2, _schregno);
        }
    }

    public List createSchregRegdDats(DB2UDB db2) throws SQLException, Exception {

        final List rtn = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sqlSchregRegdDat());
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("schregno");
                final String grade = rs.getString("grade");
                final String hrClass = rs.getString("hrClass");
                final String annual = rs.getString("annual");
                final String name = rs.getString("name");
    
                final SchregRegdDat schregRegdDat = new SchregRegdDat(
                        schregno,
                        grade,
                        hrClass,
                        annual,
                        name
                );

                schregRegdDat.load(db2);
                rtn.add(schregRegdDat);
            }

            if (rtn.isEmpty()) {
                log.debug(">>>SCHREG_REGD_DAT または SCHREG_BASE_MST に該当するものがありません。");
                throw new Exception();
            } else {
                return rtn;
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sqlSchregRegdDat() {

        StringBuffer sql = new StringBuffer();

        sql.append(" select"
            + "    T1.SCHREGNO as schregno,"
            + "    T1.GRADE as grade,"
            + "    T1.HR_CLASS as hrClass,"
            + "    T1.ANNUAL as annual,"
            + "    T2.NAME as name"
            + " from"
            + "    SCHREG_REGD_DAT T1,"
            + "    SCHREG_BASE_MST T2"
            + " where"
            + "    T1.YEAR = '" + _param._year + "'"
            + "    and T1.SEMESTER = '" + _param._semester + "'"
            + "    and T2.SCHREGNO = T1.SCHREGNO"
            + "    and T2.GRD_DIV is null");

        if (!_param._grade.equals(GRADE_ALL)) {
            sql.append("    and T1.GRADE = '" + _param._grade + "'");
        }            

        sql.append(" order by T1.GRADE, T1.ANNUAL, T1.HR_CLASS, T1.SCHREGNO");

        return sql.toString();
    }

    // ======================================================================
    /**
     * 履修登録データ
     */
    private class CompRegistDat {
        private final String _classcd;
        private final String _curriculumCd;
        private final String _subclasscd;
        private final int _compCredit;

        public CompRegistDat() {
            _classcd = "";
            _curriculumCd = "";
            _subclasscd = "";
            _compCredit = 0;
        }

        CompRegistDat(
                final String classcd,
                final String curriculumCd,
                final String subclasscd,
                final int compCredit
        ) {
            _classcd = classcd;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _compCredit = compCredit;
        }
    }

    public List createCompRegistDats(DB2UDB db2, String schregno)
        throws SQLException {

        final List rtn = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlCompRegistDat(schregno));
        rs = ps.executeQuery();
        while (rs.next()) {
            final String classcd = rs.getString("classcd");
            final String curriculumCd = rs.getString("curriculumCd");
            final String subclasscd = rs.getString("subclasscd");
            final int compCredit = Integer.parseInt(rs.getString("compCredit"));
            
            final CompRegistDat compRegistDat = new CompRegistDat(
                    classcd,
                    curriculumCd,
                    subclasscd,
                    compCredit
            );

            rtn.add(compRegistDat);
        }

        return rtn;
    }

    private String sqlCompRegistDat(String schregno) {
        final String sql;

        sql = " select"
            + "    CLASSCD as classcd,"
            + "    CURRICULUM_CD as curriculumCd,"
            + "    SUBCLASSCD as subclasscd,"
            + "    COMP_CREDIT as compCredit"
            + " from"
            + "    COMP_REGIST_DAT"
            + " where"
            + "    YEAR = '" + _param._year + "' and"
            + "    SCHREGNO = '" + schregno + "'"
            + " order by CLASSCD, SUBCLASSCD, CURRICULUM_CD"
            ;

        return sql;
    }

    // ======================================================================
    /**
     * 教科マスタ。
     */
    private Map createClassMst(final DB2UDB db2)
        throws SQLException {

        final Map rtn = new HashMap();

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlClassMst());
        rs = ps.executeQuery();

        while (rs.next()) {
            final String code = rs.getString("classcd");
            final String name = rs.getString("classname");

            rtn.put(code, name);
        }

        return rtn;
    }

    private String sqlClassMst() {
        return " select"
                + "    CLASSCD as classcd,"
                + "    CLASSABBV as classname"
                + " from"
                + "    CLASS_MST"
                ;
    }

    // ======================================================================
    /**
     * 科目マスタ。
     */
    private Map createSubclassMst(final DB2UDB db2)
        throws SQLException {

        final Map rtn = new HashMap();

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlSubclassMst());
        rs = ps.executeQuery();

        while (rs.next()) {
            final String code1 = rs.getString("classcd");
            final String code2 = rs.getString("curriculumCd");
            final String code3 = rs.getString("subclasscd");
            final String name = rs.getString("subclassname");

            rtn.put(code1 + code2 + code3, name);
        }

        return rtn;
    }

    private String sqlSubclassMst() {
        return " select"
                + "    CLASSCD as classcd,"
                + "    CURRICULUM_CD as curriculumCd,"
                + "    SUBCLASSCD as subclasscd,"
                + "    SUBCLASSABBV as subclassname"
                + " from"
                + "    SUBCLASS_MST"
                ;
    }


    // ======================================================================
    /**
     * 所属データ。
     */
    public Map createBelongingDat(DB2UDB db2)
        throws SQLException {

        final Map rtn = new HashMap();

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlBelongingDat());
        rs = ps.executeQuery();
        while (rs.next()) {
            final String code = rs.getString("belonging_div");
            final String name = rs.getString("schoolname1");

            rtn.put(code, name);
        }

        return rtn;
    }

    private String sqlBelongingDat() {
        return " select"
                + "    BELONGING_DIV as belonging_div,"
                + "    SCHOOLNAME1 as schoolname1"
                + " from"
                + "    BELONGING_MST"
                ;
    }

    // ======================================================================
    /**
     * 学籍在籍ヘッダデータ
     */
    public Map createSchregRegdHdat(DB2UDB db2)
        throws SQLException {

        final Map rtn = new HashMap();

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlSchregRegdHdat());
        rs = ps.executeQuery();
        while (rs.next()) {
            final String year = rs.getString("year");
            final String semester = rs.getString("semester");
            final String grade = rs.getString("grade");
            final String hrClass = rs.getString("hrClass");
            final String staffname = rs.getString("staffname");

            rtn.put(year + semester + grade + hrClass, staffname);
        }

        return rtn;
    }

    private String sqlSchregRegdHdat() {
        return " select"
                + "    T1.YEAR as year,"
                + "    T1.SEMESTER as semester,"
                + "    T1.GRADE as grade,"
                + "    T1.HR_CLASS as hrClass,"
                + "    T2.STAFFNAME as staffname"
                + " from"
                + "    SCHREG_REGD_HDAT T1,"
                + "    STAFF_MST T2"
                + " where"
                + "    T2.STAFFCD = T1.TR_CD1"
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
} // KNJWD700

// eof
