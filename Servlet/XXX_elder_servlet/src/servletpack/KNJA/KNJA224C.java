package servletpack.KNJA;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.pdf.AlpPdf;
import servletpack.pdf.IPdf;
import servletpack.pdf.SvfPdf;

/**
 * ＨＲ別名票
 *
 */
public class KNJA224C implements AlpPdf.IOutputPdf {

    private static final Log log = LogFactory.getLog(KNJA224C.class);

    private boolean _hasData;
    private Param _param;

    /**
     * KNJW.classから呼ばれる処理
     * @param request リクエスト
     * @param response レスポンス
     * @throws Exception I/O例外
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);

        final Vrw32alp svf = new Vrw32alp();

        if (svf.VrInit() < 0) {
            throw new IllegalStateException("svf初期化失敗");
        }
        svf.VrSetSpoolFileStream(response.getOutputStream());
        response.setContentType("application/pdf");

        IPdf ipdf = new SvfPdf(svf);
        try {
            
            outputPdf(ipdf, request);

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
            ipdf.close();
        }
    }
    
    public void outputPdf(
            final IPdf ipdf,
            final HttpServletRequest request
    ) throws Exception {
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            // パラメータの取得
            _param = new Param(request, db2);

            printMain(ipdf, db2);

        } catch (final Exception ex) {
            log.error("error! ", ex);
            throw ex;
        } finally {
            try {
                db2.commit();
                db2.close();
            } catch (Exception ex) {
                log.error("db close error!", ex);
            }
        }
    }
    
    /**
     * 印刷処理メイン
     * @param db2   ＤＢ接続オブジェクト
     * @return      対象データ存在フラグ(有り：TRUE、無し：FALSE)
     * @throws Exception
     */
    private void printMain(final IPdf svf, final DB2UDB db2) throws Exception {

        final String form = getForm();
        svf.VrSetForm(form, 1);
        log.info(" form = " + form);

        int colMax = 0;

//        if ("chiben".equals(param._output)){
//            colMax = 1;
//        } else if ("4".equals(param._output)){
//            colMax = 8;
//        } else if ("6".equals(param._output)){
//            colMax = 1;
//        } else {
            colMax = 3;
//        }
        
        final List studentList = Student.getStudentList(db2, _param);
        printStudentList(svf, studentList, colMax, _param);
    }

    private void printStudentList(final IPdf svf, final List studentListAll, int colMax, final Param param) {

        final int PAGE_MAX_LINE = 50;
        final List pageList = getPrintPageList(studentListAll, param, colMax, PAGE_MAX_LINE);
        
        for (int pi = 0; pi < pageList.size(); pi++) {
            final List columnList = (List) pageList.get(pi);
            
            for (int ci = 0; ci < columnList.size(); ci++) {
                final List studentList = (List) columnList.get(ci);
                
                for (int gyoi = 0; gyoi < studentList.size(); gyoi++) {
                    final Student student = (Student) studentList.get(gyoi);
                    if (null == student) {
                        continue;
                    }
                    printStudent(svf, ci + 1, gyoi + 1, student);
                    _hasData = true;
                }
            }
            svf.VrEndPage(); // SVFフィールド出力

        }
    }

    private List getPrintPageList(final List studentList, final Param param, int colMax, final int lineMax) {
        final List hrList = new ArrayList();
        String gradeHrclass = null;
        List current = null;
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (null == current || null == gradeHrclass || !gradeHrclass.equals(student._grade + student._hrClass)) {
                current = new ArrayList();
                hrList.add(current);
            }
            current.add(student);
            gradeHrclass = student._grade + student._hrClass;
        }
        
        if (null == param._kara) {
            // 出席番号の行に表示
            for (final Iterator hrit = hrList.iterator(); hrit.hasNext();) {
                final List hrStudentList = (List) hrit.next();
                for (int i = hrStudentList.size() - 1; i >= 0; i--) {
                    final Student student = (Student) hrStudentList.get(i);
                    if (NumberUtils.isDigits(student._attendno)) {
                        final int attendnoIdx = Integer.parseInt(student._attendno) - 1;
                        if (i == attendnoIdx) {
                            continue;
                        }
                        for (int j = hrStudentList.size(); j <= attendnoIdx; j++) {
                            hrStudentList.add(null);
                        }
                        if (0 <= attendnoIdx) {
                            hrStudentList.set(i, null);
                            hrStudentList.set(attendnoIdx, student);
                        }
                    }
                }
            }
        }
        
        final List columnList = new ArrayList();
        for (final Iterator it = hrList.iterator(); it.hasNext();) {
            final List hrStudentList = (List) it.next();
            
            for (int j = 0; j < Integer.parseInt(param._kensuu); j++){
                columnList.addAll(getPageList(hrStudentList, lineMax));
            }
        }
        final List pageList = getPageList(columnList, colMax);
        return pageList;
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

    private void printStudent(final IPdf svf, final int column, final int gyo, final Student student) {
        // 組略称・担任名出力
        svf.VrsOut("HR_NAME" + column, "chiben".equals(_param._output) ? student._hrName : student._hrNameabbv);
//        if (param._isSapporo && !StringUtils.isBlank(student._staffname) && !StringUtils.isBlank(student._staffname2)) {
//            svf.VrsOut("STAFFNAME" + slen + "_2", student._staffname);
//            svf.VrsOut("STAFFNAME" + slen + "_3", student._staffname2);
//        } else {
            svf.VrsOut("STAFFNAME" + column + ("chiben".equals(_param._output) ? "" : "_1"), student._staffname);
//        }

        // 出席番号
        if ("5".equals(_param._output)) {
            svf.VrsOutn("CLASS" + column, gyo, student._hrClass);
        }
        svf.VrsOutn("ATTENDNO" + column, gyo, NumberUtils.isDigits(student._attendno) ? String.valueOf(Integer.parseInt(student._attendno)) : student._attendno);

        if ("chiben".equals(_param._output)){
//            svf.VrsOut("NENDO" + slen, param._nendo); // 年度
//            svf.VrsOut("MAJOR" + slen, student._majorname); // 学科
//        } else if ("3".equals(param._output)){
//            svf.VrsOutn("FIN_SCHOOL" + slen, gyo, (String) param._finschoolMstMap.get(student._finschoolcd)); // 出身校
        } else if ("1".equals(_param._output)){
            svf.VrsOutn("KANA" + column + (getMS932ByteLength(student._name_kana) > 18 ? "_2" : ""), gyo, student._name_kana); // 氏名かな
        }

//        if ("6".equals(param._output)) {
//            svf.VrsOut("HR_NAME", student._hrName);
//        }
        // 男:空白、女:'*'
        svf.VrsOutn("MARK" + column, gyo, student._sex);

        // 生徒漢字・規則に従って出力
        if ("6".equals(_param._output)) {
//            final String strz = null == student._name ? "" : student._name;
//            final int z = strz.indexOf("　"); // 空白文字の位置
//            String strx = "";
//            String stry = "";
//            String field1 = null;
//            String field2 = null;
//            if (z != -1) {
//                strx = strz.substring(0, z); // 姓
//                stry = strz.substring(z + 1); // 名
//                if (strx.length() == 1) {
//                    field1 = "LNAME" + slen + "_2"; // 姓１文字
//                } else {
//                    field1 = "LNAME" + slen + "_1"; // 姓２文字以上
//                }
//                if (stry.length() == 1) {
//                    field2 = "FNAME" + slen + "_2"; // 名１文字
//                } else {
//                    field2 = "FNAME" + slen + "_1"; // 名２文字以上
//                }
//            }
//            final int nameLen = getMS932ByteLength(student._name);
//            if (nameLen <= 18 && strx.length() <= 4 && stry.length() <= 4 && null != field1 && null != field2) {
//                svf.VrsOutn(field1, gyo, strx);
//                svf.VrsOutn(field2, gyo, stry);
//            } else if (nameLen <= 20) {
//                svf.VrsOutn("NAME4", gyo, student._name);                   //空白がない
//            } else if (nameLen <= 30) {
//                svf.VrsOutn("NAME5", gyo, student._name);                   //空白がない
//            } else {
//                svf.VrsOutn("NAME6", gyo, student._name);                   //空白がない
//            }
//            final int kanaLen = getMS932ByteLength(student._name_kana);
//            if (kanaLen <= 26) {
//                svf.VrsOutn("KANA1", gyo, student._name_kana);                   //空白がない
//            } else if (kanaLen <= 30) {
//                svf.VrsOutn("KANA2", gyo, student._name_kana);                   //空白がない
//            } else {
//                svf.VrsOutn("KANA3", gyo, student._name_kana);                   //空白がない
//            }
        } else {

            final int idx = null == student._name ? -1 : student._name.indexOf("　"); // 空白文字の位置
            if (idx < 0) {
                svf.VrsOutn("NAME" + column, gyo, student._name); // 空白がない
            } else {
                final String sei = student._name.substring(0, idx); // 姓
                final String mei = student._name.substring(idx + 1); // 名
                String slenf1;
                if (sei.length() == 1) {
                    slenf1 = "_2"; // 姓１文字
                } else {
                    slenf1 = "_1"; // 姓２文字以上
                }
                String sfx = "";
                if ("1".equals(_param._output)) {
                    if (sei.length() <= 3 && mei.length() <= 3) {
                        sfx = "_3";
                    } else if (sei.length() > 4 || mei.length() > 4) {
                        sfx = "_2";
                    }
                }
                svf.VrsOutn("LNAME" + column + slenf1 + sfx, gyo, sei);
                final String slenf2;
                if (mei.length() == 1) {
                    slenf2 = "_2"; // 名１文字
                } else {
                    slenf2 = "_1"; // 名２文字以上
                }
                svf.VrsOutn("FNAME" + column + slenf2 + sfx, gyo, mei);
            }
        }
    }

    /** 対象フォーム名を返す * */
    private String getForm() {
        String formName = "";

        if (_param._output.equals("chiben")){    // 智辯用
//            formName = "KNJA224_CHI.frm";
        } else if ("1".equals(_param._output)){    // 氏名漢字・ふりかな
            formName = "KNJA224C_1.frm";
        } else if ("2".equals(_param._output)){    // 氏名漢字
            formName = "KNJA224C_2.frm";
//        } else if ("3".equals(param._output)){    // 氏名漢字・出身校
//            formName = "KNJA224_1.frm";
//        } else if ("4".equals(param._output)){    // Ａ３ヨコ
//            formName = "KNJA224_3.frm";
//        } else if ("6".equals(param._output)){    // Ａ３ヨコ、教務手帳
//            formName = "1".equals(param._kyoumu) ? "KNJA224_5.frm" : "KNJA224_6.frm";
//            PAGE_MAX_LINE = "1".equals(param._kyoumu) ? 45 : 48;
//        } else if ("5".equals(param._output)) {
//            formName = "KNJA224_4_" + param._height + param._width + ".frm";
        }
        return formName;
    }

    /** 生徒データクラス */
    private static class Student {
        final String _schregno;
        final String _attendno;
        final String _sex;
        final String _name;
        final String _name_kana;
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _hrNameabbv;
        final String _hrClassName2;
        final String _majorname;
        final String _staffname;
        final String _staffname2;
        final String _finschoolcd;

        Student(
                final String schregno,
                final String attendno,
                final String sex,
                final String name,
                final String name_kana,
                final String grade,
                final String hrClass,
                final String hrName,
                final String hrNameabbv,
                final String hrClassName2,
                final String majorname,
                final String staffname,
                final String staffname2,
                final String finschoolcd
        ) {
            _schregno = schregno;
            _attendno = attendno;
            _sex = sex;
            _name = name;
            _name_kana = name_kana;
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrNameabbv = hrNameabbv;
            _hrClassName2 = hrClassName2;
            _majorname = majorname;
            _staffname = staffname;
            _staffname2 = staffname2;
            _finschoolcd = finschoolcd;
        }
        
        /**
         * 生徒データ取得処理
         * @param db2           ＤＢ接続オブジェクト
         * @return              帳票出力対象データリスト
         * @throws Exception
         */
        private static List getStudentList(final DB2UDB db2, final Param param) {

            final List rtnList = new ArrayList();
            final String sql = sql(param);
            
            //log.debug(" sql = " + sql);
            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                final Student student = new Student(
                        KnjDbUtils.getString(row, "SCHREGNO"),
                        KnjDbUtils.getString(row, "ATTENDNO"),
                        KnjDbUtils.getString(row, "SEX"),
                        KnjDbUtils.getString(row, "NAME"),
                        KnjDbUtils.getString(row, "NAME_KANA"),
                        KnjDbUtils.getString(row, "GRADE"),
                        KnjDbUtils.getString(row, "HR_CLASS"),
                        KnjDbUtils.getString(row, "HR_NAME"),
                        KnjDbUtils.getString(row, "HR_NAMEABBV"),
                        null,
                        KnjDbUtils.getString(row, "MAJORNAME"),
                        KnjDbUtils.getString(row, "STAFFNAME"),
                        KnjDbUtils.getString(row, "STAFFNAME2"),
                        KnjDbUtils.getString(row, "FINSCHOOLCD")
                    );
                    rtnList.add(student);
            }
            return rtnList;
        }

        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     BASE.SCHREGNO,");
            stb.append("     VALUE(REGD.ATTENDNO,'') AS ATTENDNO,");
            stb.append("     VALUE(REGD.GRADE,'') AS GRADE,");
            stb.append("     VALUE(REGD.HR_CLASS,'') AS HR_CLASS,");
            // 文京の場合、性別の＊を表記しない。
            if (param._isBunkyo) {
                stb.append(" '' AS SEX, ");  // 男:空白、女:''
            } else {
                stb.append(" CASE WHEN BASE.SEX = '2' THEN '*' ELSE '' END AS SEX, ");  // 男:空白、女:'*'
            }
            // 退学者・転学者・卒業生の名前は空白（チェック有りの場合）
            if ("1".equals(param._nameNasi)) {
                stb.append(" CASE WHEN BASE.GRD_DIV IN ('1','2','3') THEN '' ELSE VALUE(BASE.NAME,'') END AS NAME,");
                stb.append(" CASE WHEN BASE.GRD_DIV IN ('1','2','3') THEN '' ELSE VALUE(BASE.NAME_KANA,'') END AS NAME_KANA,");
            } else {
                stb.append(" VALUE(BASE.NAME,'') AS NAME,");
                stb.append(" VALUE(BASE.NAME_KANA,'') AS NAME_KANA,");
            }
            stb.append("     VALUE(REGDH.HR_NAME,'') AS HR_NAME,");
            stb.append("     VALUE(REGDH.HR_NAMEABBV,'') AS HR_NAMEABBV,");
            stb.append("     VALUE(MAJOR.MAJORNAME,'') AS MAJORNAME, ");
            stb.append("     VALUE(STF1.STAFFNAME,'') AS STAFFNAME, ");
            stb.append("     VALUE(STF2.STAFFNAME,'') AS STAFFNAME2, ");
            stb.append("     VALUE(BASE.FINSCHOOLCD,'') AS FINSCHOOLCD ");
            stb.append(" FROM ");
            stb.append(" SCHREG_BASE_MST BASE ");
            stb.append(" INNER JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = BASE.SCHREGNO ");
            stb.append(" LEFT JOIN MAJOR_MST MAJOR ON REGD.COURSECD = MAJOR.COURSECD AND REGD.MAJORCD = MAJOR.MAJORCD ");
            stb.append(" INNER JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
            stb.append("     AND REGDH.SEMESTER = REGD.SEMESTER ");
            stb.append("     AND REGDH.GRADE = REGD.GRADE ");
            stb.append("     AND REGDH.HR_CLASS = REGD.HR_CLASS ");
            stb.append(" LEFT JOIN STAFF_MST STF1 ON REGDH.TR_CD1 = STF1.STAFFCD ");
            stb.append(" LEFT JOIN STAFF_MST STF2 ON REGDH.TR_CD2 = STF2.STAFFCD ");
            stb.append(" WHERE ");
            stb.append("     REGDH.YEAR = '" + param._year + "' ");
            stb.append("     AND REGDH.SEMESTER = '" + param._gakki + "' ");
            stb.append("     AND REGDH.GRADE || REGDH.HR_CLASS IN " + SQLUtils.whereIn(true, param._classSelected) + " ");
            stb.append(" ORDER BY REGD.GRADE, REGD.HR_CLASS, REGD.ATTENDNO");
            return stb.toString();
        }
    }

    /** パラメータクラス */
    private static class Param {

        private final String _year;
        private final String _gakki;
        /** 名票種類 */
        private final String _output;
        /** 教務手帳パターン */
        private final String _kyoumu;
        /** 空白行判定 */
        private final String _kara;
        /** 出力件数 */
        private final String _kensuu;
        /** 学年・組 */
        private final String[] _classSelected;
        /** 列の長さ */
        private final String _width;
        /** 行の高さ */
        private final String _height;
        /** 学籍番号を表記する */
        private final String _printSchregno;
        /** 枠無しか */
        private final String _wakuNasi;
        /** 退学者・転学者・卒業生の名前は空白か */
        private final String _nameNasi;

        private boolean _seirekiFlg;
        private String _nendo;
        private boolean _isSapporo;
        private boolean _isBunkyo;
        private Map _finschoolMstMap;

        Param(final HttpServletRequest request, final DB2UDB db2) throws Exception {
            _year      = request.getParameter("YEAR");
            _gakki  = request.getParameter("GAKKI");
            _output = request.getParameter("OUTPUT");
            _kyoumu = request.getParameter("KYOUMU");
            _kensuu = request.getParameter("KENSUU");
            _kara = request.getParameter("KARA");
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            _width = request.getParameter("WIDTH");
            _height = request.getParameter("HEIGHT");
            _printSchregno = request.getParameter("PRINT_SCHREGNO");
            _wakuNasi = request.getParameter("WAKU_NASI");
            _nameNasi = request.getParameter("NAME_NASI");
            
            _seirekiFlg = getSeirekiFlg(db2);
            _nendo = _seirekiFlg ? _year + "年度": KNJ_EditDate.h_format_JP_N(_year + "-01-01") + "度";
            final String name1 = getNameMstZ010(db2);
            _isSapporo = "sapporo".equals(name1);
            _isBunkyo = "bunkyo".equals(name1);
            
            // 指示画面にて氏名漢字・出身学校を出力時の場合に出身学校マスタ情報を取得する
            if("3".equals(_output)){
                _finschoolMstMap = createFinschoolMst(db2);
            }
        }
        
        /**
         * 出身学校データ。
         */
        private Map createFinschoolMst(final DB2UDB db2) {

            final String sql = " select"
                    + "    FINSCHOOLCD,"
                    + "    value(FINSCHOOL_NAME,'') as FINSCHOOL_NAME"
                    + " from"
                    + "    FINSCHOOL_MST";
            
            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, sql), "FINSCHOOLCD", "FINSCHOOL_NAME");
        }

        /**
         * @param isJuniorHiSchool 設定する isJuniorHiSchool。
         */
        private String getNameMstZ010(final DB2UDB db2) {
            final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ";
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql));
        }

        private boolean getSeirekiFlg(DB2UDB db2) {
            final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
            final boolean seirekiFlg = "2".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, sql)));
            return seirekiFlg;
        }

    }

}// クラスの括り
