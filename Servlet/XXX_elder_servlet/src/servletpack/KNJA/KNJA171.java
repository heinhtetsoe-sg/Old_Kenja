package servletpack.KNJA;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Semester;
import servletpack.KNJZ.detail.StaffInfo;
import servletpack.KNJZ.detail.SvfField;

/**
 * 学校教育システム 賢者 [学籍管理] ＜ＫＮＪＡ１７１＞ 生徒基本データ(生徒名簿)
 * 2005/01/28 nakamoto 作成（東京都）
 * 2005/12/18 m-yama NO001 SCHREG_BASE_DAT、SCHREG_ADDRESS_DAT修正に伴う修正
 * 2008/03/19 nakasone リファクタリング。
 *                     性別欄・備考欄の追加。フォーム(性別無し・性別有り)の選択機能追加。
 *                     電話番号・急用電話番号・性別の表示・非表示機能の追加。
 *                     タイトル年度・作成日・生年月日の西暦・和暦表示の選択機能追加。
 */

public class KNJA171 {

    private boolean _hasData;
    private Form _form;
    private Vrw32alp _svf;
    private Param _param;
    private Map _formFieldInfoMap = new HashMap();

    private static final Log log = LogFactory.getLog(KNJA171.class);

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws Exception {

        log.fatal("$Revision: 76489 $ $Date: 2020-09-07 14:28:17 +0900 (月, 07 9 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        DB2UDB db2 = null;

        // パラメータの取得
        try {
            final String dbName = request.getParameter("DBNAME");
            db2 = new DB2UDB(dbName, "db2inst1", "db2inst1", DB2UDB.TYPE2);
            if (openDb(db2)) {
                return;
            }

            _param = new Param(db2, request);
            final String form;
            if (_param._form.equals("1")) {
                // 性別なし
                if (_param._dormistudentflg) {
                    form = "KNJA171_1_2.frm";
                } else {
                    form = "KNJA171_1.frm";
                }
            } else if (_param._form.equals("2")) {
                // 性別あり
                form = "KNJA171_2.frm";
//            } else {
//                // 性別なし
//                form = "KNJA171_3.frm";
            } else if (_param._form.equals("4")) {
                // 札幌開成で追加
                form = "KNJA171_5.frm";
            } else if (_param._form.equals("5")) {
                // 埼玉栄で追加
                form = "KNJA171_6.frm";
            } else {
                // 性別あり
                if ("1".equals(_param._form3ClubCheck) || "1".equals(_param._form3GrdCheck)) {
                    form = "KNJA171_4_2.frm";
                } else {
                    form = "KNJA171_4.frm";
                }
            }
            _form = new Form(form, response);

            _param.load(db2);
            _hasData = printMain(db2);

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            _form.closeSvf();
            closeDb(db2);
        }
    }

    /** 印刷処理メイン
     * @param db2   ＤＢ接続オブジェクト
     * @return
     * @throws Exception
     */
    private boolean printMain(final DB2UDB db2) throws Exception {

        boolean retflg = false;

        log.fatal(" choice = " + _param._choice);

        final String[][] arg;
        if ("1".equals(_param._choice) || "3".equals(_param._choice)) {
            if (_param._isGhr) {
                arg = new String[_param._categorySelected.length][];
                for (int i = 0; i < _param._categorySelected.length; i++) {
                    arg[i] = new String[] {_param._categorySelected[i], null, null};  //GHRはGHR_CDのみ。
                }
            } else if (_param._isGakunenKongou) {
                arg = new String[_param._categorySelected.length][];
                for (int i = 0; i < _param._categorySelected.length; i++) {
                    arg[i] = new String[] {_param._categorySelected[i].substring(0,1), _param._categorySelected[i].substring(2), null};  //学年混合では校種とクラス(例："J-002")
                }
            } else {
                // 指示画面にて選択された年組を繰り返す
                arg = new String[_param._categorySelected.length][];
                for (int i = 0; i < _param._categorySelected.length; i++) {
                    arg[i] = new String[] {_param._categorySelected[i].substring(0,2), _param._categorySelected[i].substring(2), null};
                }
            }
        } else {
            arg = new String[][] {{_param._gradeHrClass.substring(0,2), _param._gradeHrClass.substring(2), _param.schregNo}};
        }
        for (int i = 0; i < arg.length; i++) {
            final String grade = arg[i][0];
            final String hrClass = arg[i][1];
            final String schregno = arg[i][2];
            final String schoolKind = getSchoolKind(db2, grade);
            // 生徒データ取得
            final List studentList = createSchregNo(db2, grade, hrClass, schregno);

            // 組名称及び担任名の取得
            //KNJ_Grade_Hrclass hrclass_staff = new KNJ_Grade_Hrclass(); // クラスのインスタンス作成
            ReturnVal returnvalStaff = Hrclass_Staff(db2, _param._year, _param._output, grade, hrClass);

            // 帳票出力のメソッド
            if (outPutPrint(db2, studentList, returnvalStaff, schoolKind)) {
                retflg = true;
            }
        }

        return retflg;
    }

    /** ＤＢより組名称及び担任名を取得するメソッド **/
    public ReturnVal Hrclass_Staff(DB2UDB db2,String year,String semester,String grade,String hr_class) {

        String hrclass_name = new String();     //組名称
        String hrclass_abbv = new String();     //組略称
        String staff_name = new String();       //担任名
        String classweeks = new String();       //授業週数
        String classdays = new String();        //授業日数

        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT ");
        if (_param._isGhr) {
            sql.append("   W2.GHR_NAME AS HR_NAME, ");
            sql.append("   W2.GHR_NAMEABBV AS HR_NAMEABBV, ");
        } else {
            sql.append("   W2.HR_NAME, ");
            sql.append("   W2.HR_NAMEABBV, ");
            sql.append("   W2.CLASSWEEKS, ");
            sql.append("   W2.CLASSDAYS, ");
        }
        sql.append("   W1.STAFFNAME ");

        sql.append(" FROM ");
        if (_param._isGhr) {
            sql.append(" SCHREG_REGD_GHR_DAT TF1 ");
            sql.append("     INNER JOIN SCHREG_REGD_GHR_HDAT W2 ON W2.YEAR = TF1.YEAR ");
            sql.append("     AND W2.SEMESTER = TF1.SEMESTER ");
            sql.append("     AND W2.GHR_CD = TF1.GHR_CD ");
        } else if (_param._isGakunenKongou) {
            sql.append(" V_STAFF_HR_DAT TG1 ");
            sql.append("     INNER JOIN SCHREG_REGD_HDAT W2 ON W2.YEAR = TG1.YEAR ");
            sql.append("     AND W2.SEMESTER = TG1.SEMESTER ");
            sql.append("     AND W2.GRADE = TG1.GRADE ");
            sql.append("     AND W2.HR_CLASS = TG1.HR_CLASS ");
        } else {
            sql.append("   SCHREG_REGD_HDAT W2 ");
        }
        sql.append("LEFT JOIN STAFF_MST W1 ON W1.STAFFCD=W2.TR_CD1 ");
        sql.append(" WHERE ");
        final String setSemesterStr;
        if (_param._isGhr) {
            sql.append("  TF1.YEAR = '" + year + "' ");
            sql.append("  AND TF1.GHR_CD = '" + grade + "' ");
            setSemesterStr = "TF1.SEMESTER";
        } else if (_param._isGakunenKongou) {
            sql.append("  TG1.YEAR = '" + year + "' ");
            sql.append("  AND TG1.SCHOOL_KIND || '-' || TG1.HR_CLASS IN '" + grade + '-' + hr_class + "' ");
            setSemesterStr = "TG1.SEMESTER";
        } else {
            sql.append("  W2.YEAR = '" + year + "' ");
            sql.append("  AND W2.GRADE || W2.HR_CLASS = '" + grade + hr_class + "' ");
            setSemesterStr = "W2.SEMESTER";
        }
        if ( !semester.equals("9") ) { //学期指定の場合
            sql.append("  AND " + setSemesterStr + " = '" + semester + "' ");
        } else {  //学年指定の場合
            sql.append("  AND " + setSemesterStr + " = (SELECT ");
            sql.append("                       MAX(W3.SEMESTER) ");
            sql.append("                     FROM ");
            sql.append("                       SCHREG_REGD_HDAT W3 ");
            sql.append("                     WHERE ");
            sql.append("                       W2.YEAR = W3.YEAR ");
            sql.append("                       AND W2.GRADE || W2.HR_CLASS = W3.GRADE || W3.HR_CLASS ");
            sql.append("                    ) ");
        }

        try{
            db2.query(sql.toString());
            ResultSet rs = db2.getResultSet();

            if ( rs.next() ) {
                hrclass_name = rs.getString("HR_NAME");
                hrclass_abbv = rs.getString("HR_NAMEABBV");
                staff_name = rs.getString("STAFFNAME");
                if (!_param._isGhr) {
                    classweeks = rs.getString("CLASSWEEKS");
                    classdays = rs.getString("CLASSDAYS");
                }
            }

            rs.close();
            System.out.println("[KNJ_Grade_Hrclass]Hrclass_Staff ok!");
        } catch( Exception ex ) {
            System.out.println("[KNJ_Grade_Hrclass]Hrclass_Staff error!");
            System.out.println( ex );
        }

        return (new ReturnVal(hrclass_name,hrclass_abbv,staff_name,classweeks,classdays));
    }

    /** <<< return値を返す内部クラス >>> **/
    private class ReturnVal{

        public final String val1,val2,val3,val4,val5;

        public ReturnVal(String val1,String val2,String val3,String val4,String val5) {
            this.val1 = val1;
            this.val2 = val2;
            this.val3 = val3;
            this.val4 = val4;
            this.val5 = val5;
        }
    }

    private String getSchoolKind(final DB2UDB db2, String serchGrade) {
        final String sql;
        if (_param._isGhr) {
            //校種を拾ってくる
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("   T3.SCHOOL_KIND ");
            stb.append(" FROM ");
            stb.append("   SCHREG_REGD_GHR_DAT T1 ");
            stb.append("   LEFT JOIN SCHREG_REGD_DAT T2 ");
            stb.append("     ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("    AND T2.YEAR = T1.YEAR ");
            stb.append("    AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("   INNER JOIN SCHREG_REGD_GDAT T3 ");
            stb.append("     ON T3.YEAR = T2.YEAR ");
            stb.append("    AND T3.GRADE = T2.GRADE ");
            stb.append(" WHERE ");
            stb.append("   T1.GHR_CD = '" + serchGrade + "' ");
            sql = stb.toString();
        } else if (_param._isGakunenKongou) {
            //校種が渡ってくるので、それをそのまま返す。
            return serchGrade;
        } else {
            sql = "SELECT * FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _param._year + "' AND GRADE = '" + serchGrade + "' ";
        }
        PreparedStatement ps = null;
        ResultSet rs = null;
        String schoolKind = "";
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                schoolKind = rs.getString("SCHOOL_KIND");
            }
        } catch (SQLException e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return schoolKind;
    }

    private List getPageList(final List list, final int size) {
        String keyGrade = "";
        String keyHrClass = "";
        final List pageList = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Student o = (Student) it.next();
            final String nowGrdStr;
            final String nowHrClsStr;
            ReturnVal rObj = setKeyStr(o);
            nowGrdStr = rObj.val1;
            nowHrClsStr = rObj.val2;
            if (null == current || current.size() >= size || keyGrade != null && keyHrClass != null && (!keyGrade.equals(nowGrdStr) || !keyHrClass.equals(nowHrClsStr))) {
                current = new ArrayList();
                pageList.add(current);
            }
            current.add(o);
            keyGrade = nowGrdStr;
            keyHrClass = nowHrClsStr;
        }
        return pageList;
    }

    private ReturnVal setKeyStr(final Student student) {
        String grd;
        String hrCls;
        if (_param._isGhr) {
            grd = student._ghrCd;
            hrCls = "1";
        } else if (_param._isGakunenKongou) {
            grd = student._schoolKind;
            hrCls = student._gkHrClsCd;
        } else {
            grd = student._grade;
            hrCls = student._hrClass;
        }
        ReturnVal retObj = new ReturnVal(grd, hrCls, null, null, null);
        return retObj;
    }


    /**
     * 帳票出力処理
     * @param allStudentList           帳票出力生徒データ
     * @return
     * @throws Exception
     */
    private boolean outPutPrint(final DB2UDB db2, final List allStudentList, ReturnVal returnvalStaff, final String schoolKind)   throws Exception {

        boolean dataflg = false;       // 対象データ存在フラグ

        final int setLine = "5".equals(_param._form) ? 50 : "3".equals(_param._form) || "4".equals(_param._form) ? 45 : 15;
        final List pageList = getPageList(allStudentList, setLine);

        String keyGrade = null;
        String keyHrClass = null;
        String defSchregno = "";
        int schCnt = 1;
        for (final Iterator pit = pageList.iterator(); pit.hasNext();) {
            final List studentList = (List) pit.next();

            for (int line = 0; line < studentList.size(); line++) {
                final Student student = (Student) studentList.get(line);

                // ヘッダ設定
                printHead(db2, _form._svf, returnvalStaff, schoolKind, student);
                String nowGrdStr = "";
                String nowHrClsStr = "";
                ReturnVal rObj = setKeyStr(student);
                nowGrdStr = rObj.val1;
                nowHrClsStr = rObj.val2;
                if (keyGrade != null &&  keyHrClass != null && (!keyGrade.equals(nowGrdStr) || !keyHrClass.equals(nowHrClsStr))) {
                    schCnt = 1;
                }

                if (!"3".equals(_param._form) || ("3".equals(_param._form) && "1".equals(_param._form3ClubCheck))) {
                    if (defSchregno.equals(student._schregno)) {
                        // 同一生徒の場合、部活動のみを印字
                        final int clubNameLen = getMS932ByteCount(student._clubName);
                        final String clubField = clubNameLen > 20 ? "3" : clubNameLen > 10 ? "2" : "1";
                        _form._svf.VrsOutn("CLUB" + clubField, line + 1, student._clubName);
                    } else {
                        // 明細設定
                        printMeisai(db2, _form._svf, student, line + 1, schCnt);
                    }
                } else {
                    // 明細設定
                    printMeisai(db2, _form._svf, student, line + 1, schCnt);
                }

                defSchregno = student._schregno;
                keyGrade = nowGrdStr;
                keyHrClass = nowHrClsStr;
                schCnt++;
                dataflg = true;
            }
            _form._svf.VrEndPage();
        }

        return dataflg;
    }

    /** 帳票ヘッダ設定処理
     * @throws Exception
     */
    private void printHead(final DB2UDB db2, final Vrw32alp svf, final ReturnVal returnvalStaff, final String schoolKind, final Student student)   throws Exception {

        final String hiduke;
        final String nendo;
        if (_param.seirekiOutHantei) {
            // 西暦出力
            hiduke = fomatSakuseiDate(_param.ctrlDate, "yyyy-MM-dd", "yyyy'年'M'月'd'日'");
            nendo = _param._year + "年度";
        } else {
            // 和暦出力
            hiduke = KNJ_EditDate.h_format_JP(db2, _param.ctrlDate);
            nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度";
        }
        svf.VrsOut("YMD", hiduke);

        // タイトル年度
        final String seito = "K".equals(schoolKind) ? "園児" : "P".equals(schoolKind) ? "児童" : "生徒";
        final String setTitle   = "　" + ("K".equals(schoolKind) ? "園　児" : "P".equals(schoolKind) ? "児　童" : "生　徒") + "　名　簿";
        final String setBikou   = "K".equals(schoolKind) ? "備考" : "P".equals(schoolKind) ? "出身園" : "出身学校";
        if (_param._form.equals("4")) {
            svf.VrsOut("HEADER2", setBikou);
            svf.VrsOut("HEADER1", (_param._isSundaikoufu || "1".equals(StringUtils.defaultString(_param._fixedTitle, ""))) ? "学籍番号" : (seito + "番号"));
            svf.VrsOut("nendo", nendo + "　" + (_param._isSundaikoufu ? seito + "名簿" : seito + "名簿‐在校生"));

            if (_param._isSundaikoufu) {
                svf.VrAttribute("HEADER4", "Edit=");
                svf.VrsOut("HEADER4", "読　　み");
            }
        } else if (_param._form.equals("3")) {
            if ("1".equals(_param._form3ClubCheck)) {
                svf.VrsOut("HEADER1", _param._isSundaikoufu ? "学　　籍" : "部活動");
            }
            if ("1".equals(_param._form3GrdCheck)) {
                svf.VrsOut("HEADER2", setBikou);
            }
            svf.VrsOut("nendo", nendo + setTitle);

            if (_param._isSundaikoufu) {
                svf.VrAttribute("HEADER3", "Edit=");
                svf.VrsOut("HEADER3", "出席");
            }
        } else {
            svf.VrsOut("HEADER2", setBikou);
            if ("1".equals(StringUtils.defaultString(_param._fixedTitle, "")) && (_param._form.equals("1") || _param._form.equals("2"))) {
                svf.VrsOut("HEADER1", "学　　籍"); //固定出力
            } else {
                svf.VrsOut("HEADER1", _param._isSundaikoufu ? "学　　籍" : (seito + "手帳"));
            }
            svf.VrsOut("nendo", nendo + setTitle);

            if (_param._isSundaikoufu) {
                svf.VrAttribute("HEADER3", "Edit=");
                svf.VrsOut("HEADER3", "出席");
            }
        }
        if (!_param._isGakunenKongou) {
            svf.VrsOut("HR_NAME", returnvalStaff.val1);      // 組名称
            svf.VrsOut("tannin_mei", returnvalStaff.val3);   // 担任名
        } else {
            svf.VrsOut("HR_NAME", StringUtils.defaultString(student._courseName) + " " + StringUtils.defaultString(student._gkHrClsName));      // 組名称
        }
    }

    private void svfVrsOutWithCheckField(final Vrw32alp svf, final String[] fields, final int line, final String data) {
        String field = fields[0];
        for (int i = 1; i < fields.length; i++) {
            final int fieldLength = getFieldLength(field);
            if (fieldLength > 0 && fieldLength < getMS932ByteCount(data) && getFieldLength(fields[i]) > fieldLength) {
                field = fields[i];
            } else {
                break;
            }
        }
        svf.VrsOutn(field, line, data);
    }

    /** 帳票明細設定処理
     * @throws Exception
     */
    private void printMeisai(final DB2UDB db2, final Vrw32alp svf, final Student student, final int line, final int gkAttendCnt)   throws Exception {

        //英語・日本語切替処理
        String seitoShowName = null;
        try {
            seitoShowName = _param._staffInfo.getStrEngOrJp(student._seitoKana, student._seitoEng);
        } catch (Throwable t) {
            seitoShowName = student._seitoKana;
        }

        if (_param._isGakunenKongou) {
            svf.VrlOutn("gakkyuubango", line, gkAttendCnt);
        } else {
            svf.VrlOutn("gakkyuubango", line, student._attendno);
        }
        svf.VrsOutn("gakusekibango", line, student._schregno);
        if ("4".equals(_param._form)) {
            final String kanaField = getMS932ByteCount(seitoShowName) > 24 ? "2" : "";
            svf.VrsOutn("seito_kana" + kanaField, line, seitoShowName);
            final String nameField = getMS932ByteCount(student._seitoKanji) > 20 ? "2" : "";
            svf.VrsOutn("NAME" + nameField, line, student._seitoKanji);
        } else if ("5".equals(_param._form)) {
            svfVrsOutWithCheckField(svf, new String[] {"seito_kana", "seito_kana_2", "seito_kana_3"}, line, seitoShowName);
            svfVrsOutWithCheckField(svf, new String[] {"NAME", "NAME_2", "NAME_3"}, line, student._seitoKanji);
        } else {
            svfVrsOutWithCheckField(svf, new String[] {"seito_kana", "seito_kana_2"}, line, seitoShowName);
            svfVrsOutWithCheckField(svf, new String[] {"NAME", "NAME_2"}, line, student._seitoKanji);
        }
        // 誕生日出力フラグ
        if (_param._birthday != null) {
            if (!StringUtils.isEmpty(student._birthday)) {
                if ("3".equals(_param._form)) {
                    svf.VrsOutn("birthday1", line, student._birthday.replace('-', '/'));
                } else if ("4".equals(_param._form) || "5".equals(_param._form)) {
                    svf.VrsOutn("birthday1", line, fomatSakuseiDate2(db2, student._birthday));
                } else if (_param.seirekiOutHantei) {
                    // 西暦出力
                    svf.VrsOutn("birthday1", line, fomatSakuseiDate(student._birthday, "yyyy-MM-dd", "yyyy'年'"));
                    svf.VrsOutn("birthday2", line, fomatSakuseiDate(student._birthday, "yyyy-MM-dd", "M'月'd'日'"));
                } else {
                    // 和暦出力
                    svf.VrsOutn("birthday1", line, KNJ_EditDate.h_format_JP_N(db2, student._birthday));
                    svf.VrsOutn("birthday2", line, KNJ_EditDate.h_format_JP_MD(student._birthday));
                }
            }
        }
        // 性別出力フラグ
        if (_param._sex != null) {
            svf.VrsOutn("sex", line, student._sex);
        }

        svf.VrsOutn("ZIPCD", line, student._zipcd1);
        if ("3".equals(_param._form)) {
            final String setAddr = student._address1 + student._address2;
            final int addrLen = getMS932ByteCount(setAddr);
            if ("1".equals(_param._form3ClubCheck) && "1".equals(_param._form3GrdCheck)) {
                if (addrLen > 100 && getFieldLength("seito_jyusho5") > addrLen) {
                    svf.VrsOutn("seito_jyusho5", line, setAddr);
                } else if (addrLen > 80) {
                    svf.VrsOutn("seito_jyusho4", line, setAddr);
                } else if (addrLen > 60) {
                    svf.VrsOutn("seito_jyusho3", line, setAddr);
                } else if (addrLen > 40) {
                    svf.VrsOutn("seito_jyusho2", line, setAddr);
                } else {
                    svf.VrsOutn("seito_jyusho", line, setAddr);
                }
            } else {
                if (addrLen > 100 && getFieldLength("seito_jyusho4") > addrLen) {
                    svf.VrsOutn("seito_jyusho4", line, setAddr);
                } else if (addrLen > 80) {
                    svf.VrsOutn("seito_jyusho3", line, setAddr);
                } else if (addrLen > 50) {
                    svf.VrsOutn("seito_jyusho2", line, setAddr);
                } else {
                    svf.VrsOutn("seito_jyusho", line, setAddr);
                }
            }
        } else if ("5".equals(_param._form)) {
            final String setAddr = student._address1 + student._address2;
            final int addrLen = getMS932ByteCount(setAddr);
            if (addrLen > 70 && getFieldLength("seito_jyusho_4") > addrLen) {
                svf.VrsOutn("seito_jyusho_4", line, setAddr);
            } else if (addrLen > 60) {
                svf.VrsOutn("seito_jyusho_3", line, setAddr);
            } else if (addrLen > 50) {
                svf.VrsOutn("seito_jyusho_2", line, setAddr);
            } else {
                svf.VrsOutn("seito_jyusho", line, setAddr);
            }
        } else {
            String field1 = "seito_jyusho";
            String field2 = "seito_jyusho2";
            final int field1len = getFieldLength(field1);
            final int field2len = getFieldLength(field2);
            if (field1len > 0 && (getMS932ByteCount(student._address1) > field1len || getMS932ByteCount(student._address2) > field2len) && getFieldLength("seito_jyusho_2") > field1len) {
                field1 = "seito_jyusho_2";
                field2 = "seito_jyusho_22";
            }
            svf.VrsOutn(field1, line, student._address1);
            svf.VrsOutn(field2, line, student._address2);
        }
        // 電話番号出力フラグ
        if (_param._tel != null) {
            svf.VrsOutn("PHONE1", line, student._telno1);
        }
        // 急用電話番号出力フラグ
        if (_param._eTel != null) {
            svf.VrsOutn("PHONE2", line, student._telno2);
        }

        if ("5".equals(_param._form)) {
            svfVrsOutWithCheckField(svf, new String[] {"hogosha_kanji", "hogosha_kanji_2", "hogosha_kanji_3"}, line, student._guardName);
        } else {
            svfVrsOutWithCheckField(svf, new String[] {"hogosha_kanji", "hogosha_kanji_2"}, line, student._guardName);
        }
        final String setJname = StringUtils.replace(StringUtils.replace(StringUtils.replace(StringUtils.defaultString((_param._use_finSchool_teNyuryoku_P && "P".equals(student._schoolKind)) ? student._det002BaseRemark1 : null, student._jName), "幼稚園", ""), "小学校", ""), "中学校", "");
        svf.VrsOutn("syussinko", line, setJname);
        if (_param._dormistudentflg) {
            svf.VrsOutn("DORMITORY", line, student._dormitoryflg);
        }
        svf.VrsOutn("ENT_DIV", line, student._ent_div_name);

        if (!"3".equals(_param._form) || ("3".equals(_param._form) && "1".equals(_param._form3GrdCheck))) {
            final int jNameLen = getMS932ByteCount(setJname);
            final String jField = getFieldLength("FINSCHOOL_NAME2") < jNameLen && getFieldLength("FINSCHOOL_NAME3") > getFieldLength("FINSCHOOL_NAME2") ? "3" : jNameLen > 14 ? "2" : "1";
            svf.VrsOutn("FINSCHOOL_NAME" + jField, line, setJname);
        }
        if (!"3".equals(_param._form) || ("3".equals(_param._form) && "1".equals(_param._form3ClubCheck))) {
            final int clubNameLen = getMS932ByteCount(student._clubName);
            final String clubField = clubNameLen > 20 ? "3" : clubNameLen > 10 ? "2" : "1";
            svf.VrsOutn("CLUB" + clubField, line, student._clubName);
        }

    }

    private int getMS932ByteCount(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;
            } catch (Exception e) {
            }
        }
        return ret;
    }

    // ======================================================================
    /** 生徒データ取得処理
     * @param db2           ＤＢ接続オブジェクト
     * @param grade    学年
     * @param hrClass  組
     * @param schregNo 学籍番号
     * @throws SQLException
     */
    private List createSchregNo(final DB2UDB db2,
                                 String grade,
                                 String hrClass,
                                 String schregNo) throws SQLException {

        final List rtnList = new ArrayList();
        final String sql = Student.sql(_param, grade, hrClass, schregNo);
        log.debug("regdSql :" + sql);

        PreparedStatement ps = db2.prepareStatement(sql);
        final ResultSet rs = ps.executeQuery();
        try {
            while (rs.next()) {
                final Student student = Student.createStudent(_param, rs);
                rtnList.add(student);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnList;
    }

    /** 生徒クラス */
    private static class Student {
        final String _year;
        final String _grade;
        final String _hrClass;
        final String _semester;
        final String _schregno;
        final int _attendno;
        final String _schoolKind;
        final String _seitoKanji;
        final String _seitoKana;
        final String _seitoEng;
        final String _birthday;
        final String _zipcd1;
        final String _address1;
        final String _address2;
        final String _telno1;
        final String _guardName;
        final String _telno2;
        final String _jName;
        final String _clubName;
        final String _sex;
        final String _ent_div_name;
        final String _det002BaseRemark1;
        final String _dormitoryflg;
        final String _ghrCd;
        final String _gkHrClsCd;
        final String _gkHrClsName;
        final String _courseName;

        Student(
                final String year,
                final String grade,
                final String hrClass,
                final String semester,
                final String schregno,
                final int attendno,
                final String schoolKind,
                final String seitoKanji,
                final String seitoKana,
                final String seitoEng,
                final String birthday,
                final String zipcd1,
                final String address1,
                final String address2,
                final String telno1,
                final String guardName,
                final String telno2,
                final String jName,
                final String clubName,
                final String sex,
                final String ent_div_name,
                final String det002BaseRemark1,
                final String dormitoryflg,
                final String ghrCd,
                final String gkHrClsCd,
                final String gkHrClsName,
                final String courseName

        ) {
            _year = year;
            _grade = grade;
            _hrClass = hrClass;
            _semester = semester;
            _schregno = schregno;
            _attendno = attendno;
            _schoolKind = schoolKind;
            _seitoKanji = seitoKanji;
            _seitoKana = seitoKana;
            _seitoEng = seitoEng;
            _birthday = birthday;
            _zipcd1 = zipcd1;
            _address1 = address1;
            _address2 = address2;
            _telno1 = telno1;
            _guardName = guardName;
            _telno2 = telno2;
            _jName = jName;
            _clubName = clubName;
            _sex = sex;
            _ent_div_name = ent_div_name;
            _det002BaseRemark1 = det002BaseRemark1;
            _dormitoryflg = (dormitoryflg != null && !dormitoryflg.equals("")) ? dormitoryflg : "";
            _ghrCd = ghrCd;
            _gkHrClsCd = gkHrClsCd;
            _gkHrClsName = gkHrClsName;
            _courseName = courseName;
        }

        public static Student createStudent(final Param param, final ResultSet rs) throws SQLException {
            return new Student(
                    rs.getString("YEAR"),
                    rs.getString("GRADE"),
                    rs.getString("HR_CLASS"),
                    rs.getString("SEMESTER"),
                    rs.getString("SCHREGNO"),
                    rs.getInt("ATTENDNO"),
                    rs.getString("SCHOOL_KIND"),
                    rs.getString("SEITO_KANJI"),
                    rs.getString("SEITO_KANA"),
                    rs.getString("SEITO_ENG"),
                    rs.getString("BIRTHDAY"),
                    rs.getString("ZIPCD1"),
                    rs.getString("ADDRESS1"),
                    rs.getString("ADDRESS2"),
                    rs.getString("TELNO1"),
                    rs.getString("GUARD_NAME"),
                    rs.getString("TELNO2"),
                    rs.getString("J_NAME"),
                    rs.getString("CLUBNAME"),
                    rs.getString("SEX"),
                    rs.getString("ENT_DIV_NAME"),
                    rs.getString("DET002_BASE_REMARK1"),
                    rs.getString("DORMITORYFLG"),
                    param._isGhr ? rs.getString("GHR_CD") : "",
                    param._isGakunenKongou ? rs.getString("GK_HR_CLASS") : "",
                    param._isGakunenKongou ? rs.getString("GK_CLASSNAME") : "",
                    param._isGakunenKongou ? rs.getString("COURSENAME") : ""
            );
        }

        private static String sql(final Param param, String grade, String hrClass, String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append("  SELECT DISTINCT");
            stb.append("   DB1.YEAR,");
            if (param._isGhr) {
                stb.append("  TF1.GHR_CD, ");
            } else if (param._isGakunenKongou) {
                stb.append("  TG1.SCHOOL_KIND AS GK_SCHKIND, ");
                stb.append("  TG1.HR_CLASS AS GK_HR_CLASS, ");
                stb.append("  TG1.HR_CLASS_NAME1 AS GK_CLASSNAME, ");
                stb.append("  DB20.COURSENAME, ");
            }
            stb.append("   DB1.GRADE,");
            stb.append("   DB1.HR_CLASS,");
            stb.append("   DB1.SEMESTER,");
            stb.append("   DB1.SCHREGNO,");
            if (param._isGhr) {
                stb.append("   TF1.GHR_ATTENDNO AS ATTENDNO,");
            } else {
                stb.append("   DB1.ATTENDNO,");
            }
            stb.append("   GDAT.SCHOOL_KIND,");
            stb.append("   DB2.NAME AS SEITO_KANJI,");
            stb.append("   VALUE(DB2.NAME_KANA,'') AS SEITO_KANA, ");
            stb.append("   DB2.NAME_ENG AS SEITO_ENG, ");
            stb.append("   VALUE(CHAR(DB2.BIRTHDAY),'') AS BIRTHDAY,");
            stb.append("   VALUE(DB3.ZIPCD,'') AS ZIPCD1,");
            stb.append("   VALUE(DB3.ADDR1,'') AS ADDRESS1,");
            stb.append("   value(DB3.ADDR2,'') AS ADDRESS2,");
            stb.append("   VALUE(DB3.TELNO,'') AS TELNO1,");
            stb.append("   VALUE(DB4.GUARD_NAME,'') AS GUARD_NAME,");
            stb.append("   VALUE(DB2.EMERGENCYTELNO,'') AS TELNO2,");
            stb.append("   VALUE(DB6.FINSCHOOL_NAME,'') AS J_NAME,");
            if ("3".equals(param._form) && "1".equals(param._form3ClubCheck)) {
                stb.append("   VALUE(CLUB_T.CLUBNAME,'') AS CLUBNAME,");
            } else {
                stb.append("   '' AS CLUBNAME,");
            }
            stb.append("   VALUE(DB7.ABBV1,'') AS SEX,");
            stb.append("   VALUE(DB8.NAME1,'') AS ENT_DIV_NAME,");
            stb.append("   CASE WHEN DB9.DOMI_ENTDAY IS NOT NULL THEN '〇' ELSE '' END AS DORMITORYFLG, ");
            stb.append("   DET002.BASE_REMARK1 AS DET002_BASE_REMARK1 ");
            stb.append(" FROM");
            if (param._isFi) {
                stb.append(" SCHREG_REGD_FI_DAT TF1 ");
                stb.append("     LEFT JOIN SCHREG_REGD_DAT DB1 ON DB1.SCHREGNO = TF1.SCHREGNO ");
                stb.append("     AND DB1.YEAR = TF1.YEAR ");
                stb.append("     AND DB1.SEMESTER = TF1.SEMESTER ");
                stb.append("     INNER JOIN SCHREG_REGD_HDAT DB7 ON DB1.YEAR = DB7.YEAR ");
                stb.append("     AND DB1.SEMESTER = DB7.SEMESTER ");
                stb.append("     AND DB1.GRADE = DB7.GRADE ");
                stb.append("     AND DB1.HR_CLASS = DB7.HR_CLASS ");
            } else if (param._isGhr) {
                stb.append(" SCHREG_REGD_GHR_DAT TF1 ");
                stb.append("     LEFT JOIN SCHREG_REGD_DAT DB1 ON DB1.SCHREGNO = TF1.SCHREGNO ");
                stb.append("     AND DB1.YEAR = TF1.YEAR ");
                stb.append("     AND DB1.SEMESTER = TF1.SEMESTER ");
                stb.append("     INNER JOIN SCHREG_REGD_HDAT DB7 ON DB1.YEAR = DB7.YEAR ");
                stb.append("     AND DB1.SEMESTER = DB7.SEMESTER ");
                stb.append("     AND DB1.GRADE = DB7.GRADE ");
                stb.append("     AND DB1.HR_CLASS = DB7.HR_CLASS ");
            } else if (param._isGakunenKongou) {
                stb.append(" V_STAFF_HR_DAT TG1 ");
                stb.append("     INNER JOIN SCHREG_REGD_HDAT DB7 ON DB7.YEAR = TG1.YEAR ");
                stb.append("     AND DB7.SEMESTER = TG1.SEMESTER");
                stb.append("     AND DB7.GRADE = TG1.GRADE");
                stb.append("     AND DB7.HR_CLASS = TG1.HR_CLASS");
                stb.append("     INNER JOIN SCHREG_REGD_DAT DB1 ON DB1.YEAR = TG1.YEAR ");
                stb.append("     AND DB1.SEMESTER = TG1.SEMESTER ");
                stb.append("     AND DB1.GRADE = DB7.GRADE ");
                stb.append("     AND DB1.HR_CLASS = DB7.HR_CLASS ");
            } else {
                stb.append("   SCHREG_REGD_DAT DB1");
                stb.append(" INNER JOIN SCHREG_REGD_HDAT DB7 ON");
                stb.append("   DB1.YEAR = DB7.YEAR");
                stb.append("   AND DB1.SEMESTER = DB7.SEMESTER");
                stb.append("   AND DB1.GRADE = DB7.GRADE");
                stb.append("   AND DB1.HR_CLASS = DB7.HR_CLASS");
            }
            stb.append(" INNER JOIN SCHREG_BASE_MST DB2 ON");
            stb.append("   DB1.SCHREGNO = DB2.SCHREGNO");
            stb.append(" LEFT JOIN SCHREG_REGD_GDAT GDAT ON");
            stb.append("   GDAT.YEAR = DB1.YEAR");
            stb.append("   AND GDAT.GRADE = DB1.GRADE");
            stb.append(" LEFT  JOIN GUARDIAN_DAT DB4 ON");
            stb.append("   DB2.SCHREGNO = DB4.SCHREGNO");
            stb.append(" LEFT  JOIN FINSCHOOL_MST DB6 ON");
            stb.append("   DB2.FINSCHOOLCD = DB6.FINSCHOOLCD");
            stb.append(" LEFT JOIN NAME_MST DB7 ON");
            stb.append("   DB7.NAMECD1='Z002' AND DB7.NAMECD2=DB2.SEX");
            stb.append(" LEFT JOIN NAME_MST DB8 ON");
            stb.append("   DB8.NAMECD1='A002' AND DB8.NAMECD2=DB2.ENT_DIV");
            stb.append(" LEFT JOIN (");
            stb.append("   SELECT");
            stb.append("     SCHREGNO,");
            stb.append("     ZIPCD,");
            stb.append("     TELNO,");
            stb.append("     ADDR1,");
            stb.append("     ADDR2");
            stb.append("   FROM");
            stb.append("     SCHREG_ADDRESS_DAT W1");
            stb.append("   WHERE");
            stb.append("     (W1.SCHREGNO,W1.ISSUEDATE) IN (");
            stb.append("       SELECT SCHREGNO,");
            stb.append("              MAX(ISSUEDATE)");
            stb.append("       FROM   SCHREG_ADDRESS_DAT W2");
            stb.append("       WHERE  W2.ISSUEDATE <= '" + param.serchdate2 + "'");
            stb.append("         AND (W2.EXPIREDATE IS NULL " + " OR W2.EXPIREDATE >= '" + param.serchdate1 + "')");
            stb.append("       GROUP BY SCHREGNO )");
            stb.append(" )DB3 ON DB3.SCHREGNO = DB1.SCHREGNO");
            stb.append(" LEFT JOIN SCHREG_BASE_DETAIL_MST DET002 ON");
            stb.append("   DET002.SCHREGNO = DB1.SCHREGNO ");
            stb.append("   AND DET002.BASE_SEQ ='002' ");
            if ("3".equals(param._form) && "1".equals(param._form3ClubCheck)) {
                stb.append("   LEFT JOIN ( SELECT ");
                stb.append("                   SCHW.SCHREGNO, ");
                //校種利用、または校種対応プロパティが立っている場合は校種を取得する。
                if ("1".equals(param._useClubMultiSchoolKind)) {
                    //校種対応プロパティでは校種をLOGIN校種で絞る。
                    stb.append(" SCHW.SCHOOL_KIND, ");
                } else if ("1".equals(param._use_prg_schoolkind) ||"1".equals(param._useSchool_KindField)) {
                    //校種を利用するプロパティ設定であれば、校種を取得する。
                    stb.append(" SCHW.SCHOOL_KIND, ");
                }
                stb.append("                   SCHW.CLUBCD, ");
                stb.append("                   CLUB_M.CLUBNAME ");
                stb.append("               FROM ");
                stb.append("                   SCHREG_CLUB_HIST_DAT SCHW ");
                stb.append("                   INNER JOIN CLUB_MST CLUB_M ");
                stb.append("                      ON SCHW.CLUBCD = CLUB_M.CLUBCD ");
                if ("1".equals(param._useClubMultiSchoolKind)) {
                    //校種対応プロパティでは校種をLOGIN校種で絞る。
                    stb.append("                 AND SCHW.SCHOOL_KIND = CLUB_M.SCHOOL_KIND ");
                } else if ("1".equals(param._use_prg_schoolkind) ||"1".equals(param._useSchool_KindField)) {
                    //校種を利用するプロパティ設定であれば、学年指定しているのでGDATの校種で絞る。
                    stb.append("                 AND SCHW.SCHOOL_KIND = CLUB_M.SCHOOL_KIND ");
                }
                stb.append("               WHERE ");
                stb.append("                   '" + param._loginDate + "' BETWEEN SCHW.SDATE AND VALUE(SCHW.EDATE, '9999-12-31') ");
                stb.append("               ORDER BY ");
                stb.append("                   SCHW.SCHREGNO, ");
                stb.append("                   SCHW.CLUBCD ");
                stb.append("               ) CLUB_T ON CLUB_T.SCHREGNO = DB1.SCHREGNO ");
                //校種利用、または校種対応プロパティが立っている場合は校種を絞る。
                if ("1".equals(param._useClubMultiSchoolKind)) {
                    //校種対応プロパティでは校種をLOGIN校種で絞る。
                    stb.append(" AND CLUB_T.SCHOOL_KIND = '" + param._schoolkind + "' ");
                } else if ("1".equals(param._use_prg_schoolkind) ||"1".equals(param._useSchool_KindField)) {
                    //校種を利用するプロパティ設定であれば、学年指定しているのでGDATの校種で絞る。
                    stb.append(" AND CLUB_T.SCHOOL_KIND = GDAT.SCHOOL_KIND ");
                }
            }
            stb.append("   LEFT JOIN SCHREG_DOMITORY_HIST_DAT DB9 ON ");
            stb.append("     DB9.SCHREGNO = DB1.SCHREGNO ");
            stb.append("     AND DB9.DOMI_ENTDAY <= '" + param._loginDate + "' ");
            stb.append("     AND ( DB9.DOMI_OUTDAY IS NULL OR '" + param._loginDate + "' < DB9.DOMI_OUTDAY) ");
            stb.append("   LEFT JOIN COURSE_MST DB20 ON DB20.COURSECD = DB1.COURSECD ");
            stb.append(" WHERE");
            stb.append("       DB1.YEAR = '" + param._year + "'");
            stb.append("   AND DB1.SEMESTER = '" + param._output + "'");
            if (schregno != null) {
                stb.append("   AND DB1.SCHREGNO IN " + schregno + " ");
            }
            if (param._isGhr) {
                stb.append("   AND TF1.GHR_CD IN '" + grade + "'");
                stb.append(" ORDER BY DB1.GRADE, DB1.HR_CLASS, TF1.GHR_ATTENDNO");
            } else if (param._isGakunenKongou) {
                stb.append("   AND TG1.SCHOOL_KIND = '" + grade + "'");
                stb.append("   AND TG1.HR_CLASS IN '" + hrClass + "'");
                stb.append(" ORDER BY TG1.SCHOOL_KIND, TG1.HR_CLASS, DB1.GRADE,DB1.HR_CLASS,DB1.ATTENDNO ");
            } else {
                stb.append("   AND DB1.GRADE = '" + grade + "'");
                stb.append("   AND DB1.HR_CLASS = '" + hrClass + "'");
                stb.append(" ORDER BY DB1.GRADE, DB1.HR_CLASS, DB1.ATTENDNO");
            }
            return stb.toString();
        }
    }

    // ======================================================================

    private String fomatSakuseiDate2(final DB2UDB db2, final String date) {
        String rtn = null;
        try {
            final String[] tate_format = KNJ_EditDate.tate_format(KNJ_EditDate.h_format_JP(db2, date));
            final DecimalFormat d2 = new DecimalFormat("00");
            final String kigo = KNJ_EditDate.gengouAlphabetMarkOfDate(db2, date.replace('/', '-'));
            if (tate_format != null) {
                final String y = NumberUtils.isNumber(tate_format[1]) ? d2.format(Integer.parseInt(tate_format[1])) : tate_format[1];
                final String m = d2.format(Integer.parseInt(tate_format[2]));
                final String d = d2.format(Integer.parseInt(tate_format[3]));
                rtn = kigo + y + "." + m + "." + d;
            }
        } catch( Exception e ) {
            log.error("setHeader set error!", e);
        }
        return rtn;
    }
    /**
     * 日付を指定されたフォーマットに設定し文字列にして返す
     * @param s
     * @return
     */
    private String fomatSakuseiDate(String cnvDate, String sfmt, String chgfmt) {

        String retDate = "";
        try {
            DateFormat foramt = new SimpleDateFormat(sfmt);
            //文字列よりDate型へ変換
            Date date1 = foramt.parse(cnvDate);
            // 年月日のフォーマットを指定
            SimpleDateFormat sdf1 = new SimpleDateFormat(chgfmt);
            // Date型より文字列へ変換
            retDate = sdf1.format(date1);
        } catch (Exception e) {
            log.error("setHeader set error!", e);
        }
        return retDate;
    }

    /** パラメータクラス */
    private class Param {
        private final String _programid;
        private final String _year;
        private final String _output;
        private final String _tel;
        private final String _eTel;
        private final String _sex;
        private final String _form;
        private final String _birthday;
        private final String _choice;
        private final String _gradeHrClass;
        private final String _form3ClubCheck;
        private final String _form3GrdCheck;
        private final String _loginDate;
        private final boolean _dormistudentflg;

        private final String _schoolkind;
        private final String _useClubMultiSchoolKind;
        private final String _use_prg_schoolkind;
        private final String _useSchool_KindField;

        private final String _staffCd;
        private StaffInfo _staffInfo;

        private String[] _categorySelected;
        private String schregNo = "";
        private int pagecnt = 0;    // 現在ページ数
        private boolean seirekiOutHantei = false;
        String serchdate1; // 学期開始日
        String serchdate2; // 学期終了日
        String ctrlDate;
        String _z010Name1;
        boolean _isSundaikoufu;

        private String _fixedTitle;  // 見出し名固定

        final boolean _isTokubetsuShien;
        final String _gakunenKongou;
        final String _dispMTokuHouJituGrdMixChkRad;
        boolean _isFi = false;
        boolean _isGhr = false;
        boolean _isGakunenKongou = false;
        boolean _isHoutei = false;
        boolean _use_finSchool_teNyuryoku_P = false;

        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _programid = request.getParameter("PRGID");
            _year = request.getParameter("YEAR");        // 年度
            _output = request.getParameter("OUTPUT");    // 学期
            _tel = request.getParameter("TEL");          // 電話番号出力フラグ
            _eTel = request.getParameter("E_TEL");       // 急用電話番号出力フラグ
            _sex = request.getParameter("SEX");          // 性別
            _form = request.getParameter("FORM");        // 1:性別なし、2:性別あり
            _birthday = request.getParameter("BIRTHDAY");// 生年月日出力フラグ
            _choice = request.getParameter("CHOICE");    // 1:クラス指定、2:個人指定 3:実クラス
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS"); // 学年＋組
            _form3ClubCheck = request.getParameter("FORM3_CLUB_CHECK");
            _form3GrdCheck = request.getParameter("FORM3_GRD_CHECK");
            _loginDate = request.getParameter("LOGIN_DATE");
            _dormistudentflg = StringUtils.defaultString(request.getParameter("FORM3_DORMITORY_CHECK")).equals("1") ? true : false;
            _fixedTitle = StringUtils.defaultString(request.getParameter("KNJA171_UseFixedTitile"), "");

            _schoolkind = request.getParameter("SCHOOLKIND");
            _useClubMultiSchoolKind = request.getParameter("useClubMultiSchoolKind");
            _use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            _useSchool_KindField = request.getParameter("useSchool_KindField");

            _staffCd = request.getParameter("PRINT_LOG_STAFFCD");
            try {
                _staffInfo = new StaffInfo(db2, _staffCd);
            } catch (Throwable t) {
                log.error("exception!", t);
            }

            _isTokubetsuShien = "1".equals(request.getParameter("useSpecial_Support_Hrclass"));
            _gakunenKongou = request.getParameter("GAKUNEN_KONGOU");
            _dispMTokuHouJituGrdMixChkRad = request.getParameter("dispMTokuHouJituGrdMixChkRad");
            _use_finSchool_teNyuryoku_P = "1".equals(request.getParameter("use_finSchool_teNyuryoku_P"));

            if ("1".equals(_dispMTokuHouJituGrdMixChkRad)) {
                if ("3".equals(_choice) && "1".equals(request.getParameter("useFi_Hrclass"))) {
                    _isFi = true;
                } else if ("3".equals(_choice) && _isTokubetsuShien) {
                    _isGhr = true;
                } else if ("1".equals(_choice) && "1".equals(_gakunenKongou) && _isTokubetsuShien) {
                    _isGakunenKongou = true;
                } else {
                    _isHoutei = true;
                }
            } else {
                _isHoutei = true;
            }

            // 学籍番号
            if (_choice.equals("1") || _choice.equals("3")) {
                _categorySelected = request.getParameterValues("CATEGORY_SELECTED"); // 学年＋組または学籍番号
                if (_isGakunenKongou) {
                    for (int ia = 0; ia < _categorySelected.length; ia++) {
                        if (_categorySelected[ia] == null) {
                            break;
                        }
                        //1文字目はソート用のコードなので、除外
                        _categorySelected[ia] = ("".equals(_categorySelected[ia]) ? "" : StringUtils.substring(_categorySelected[ia], 1));
                    }
                }
            } else {
                String c_select[] = request.getParameterValues("CATEGORY_SELECTED"); // 学籍番号SQL抽出用文字列
                schregNo = "(";
                for (int ia = 0; ia < c_select.length; ia++) {
                    if (c_select[ia] == null) {
                        break;
                    }
                    if (ia > 0) {
                        schregNo = schregNo + ",";
                    }
                    schregNo = schregNo + "'" + c_select[ia] + "'";
                }
                schregNo = schregNo + ")";
            }
        }
        public void load(DB2UDB db2) throws SQLException {
            String seirekiHantei = getNameMstInfo(db2);
            if (seirekiHantei.equals("2")) {
                seirekiOutHantei = true;
            }

            KNJ_Semester semester = new KNJ_Semester(); // クラスのインスタンス作成
            KNJ_Semester.ReturnVal returnval = semester.Semester(db2, _year, _output);
            serchdate1 = returnval.val2; // 学期開始日
            serchdate2 = returnval.val3; // 学期終了日

            // 作成日(現在処理日)の取得
            KNJ_Control control = new KNJ_Control(); // クラスのインスタンス作成
            KNJ_Control.ReturnVal controlReturnval = control.Control(db2);
            ctrlDate = controlReturnval.val3;

            _z010Name1 = getZ010(db2);
            _isSundaikoufu = "sundaikoufu".equals(_z010Name1);
        }

        /**
         * 名称マスタ。
         */
        private String getNameMstInfo(final DB2UDB db2)
            throws SQLException {

            String retSeirekiFlg = "";

            final String sql = sqlNameMst();
            final PreparedStatement ps = db2.prepareStatement(sql);
            final ResultSet rs = ps.executeQuery();
            try {
                while (rs.next()) {
                    retSeirekiFlg = rs.getString("seirekiFlg");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retSeirekiFlg;
        }

        private String sqlNameMst() {
            String ret ="select"
                    + "  VALUE(NAME1, '') as seirekiFlg"
                    + " from"
                    + "   NAME_MST"
                    + " where"
                    + "   NAMECD1 = 'Z012' and"
                    + "   NAMECD2 = '00' "
                    ;
            return ret;
        }

        /**
         * Z010名称1
         */
        private String getZ010(final DB2UDB db2) throws SQLException {
            String name1 = null;
            final String sql = " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ";
            final PreparedStatement ps = db2.prepareStatement(sql);
            final ResultSet rs = ps.executeQuery();
            try {
                while (rs.next()) {
                    name1 = rs.getString("NAME1");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name1;
        }
    }

    // ======================================================================
    private class Form {
        private Vrw32alp _svf;
        private String _file;

        public Form(final String file,final HttpServletResponse response) throws IOException {
            _svf = new Vrw32alp();

            if (_svf.VrInit() < 0) {
                throw new IllegalStateException("svf初期化失敗");
            }
            _svf.VrSetSpoolFileStream(response.getOutputStream());
            response.setContentType("application/pdf");

            _file = file;
            _svf.VrSetForm(file, 1);

            if (!_formFieldInfoMap.containsKey(_file)) {
                _formFieldInfoMap.put(_file, null);
                try {
                    _formFieldInfoMap.put(_file, SvfField.getSvfFormFieldInfoMapGroupByName(_svf));
                } catch (Throwable t) {
                    log.warn(" no class SvfField.");
                }
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

    private int getFieldLength(final String fieldname) {
        int length = -1;
        try {
            length = Integer.parseInt((String) getFieldStatusMap(fieldname).get("Keta"));
        } catch (Throwable t) {
        }
        return length;
    }

    private Map getFieldStatusMap(final String fieldname) {
        final Map m = new HashMap();
        try {
            SvfField f = (SvfField) getMappedMap(_formFieldInfoMap, _form._file).get(fieldname);
            m.put("X", String.valueOf(f.x()));
            m.put("Y", String.valueOf(f.y()));
            m.put("Size", String.valueOf(f.size()));
            m.put("Keta", String.valueOf(f._fieldLength));
        } catch (Throwable t) {
            final String key = _form._file + "." + fieldname;
            if (null == getMappedMap(_formFieldInfoMap, "ERROR").get(key)) {
                log.warn(" svf field not found:" + key);
                if (null == _form._file) {
                    log.error(" form not set!");
                }
                getMappedMap(_formFieldInfoMap, "ERROR").put(key, "ERROR");
            }

        }
        return m;
    }

    private static Map getMappedMap(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return (Map) map.get(key1);
    }
}
