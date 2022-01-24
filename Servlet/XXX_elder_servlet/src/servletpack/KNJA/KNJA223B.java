package servletpack.KNJA;

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
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.StaffInfo;


/**
 * ＨＲ別名票
 */
public class KNJA223B {

    private static final Log log = LogFactory.getLog(KNJA223B.class);
    private boolean _hasData;
    private Param _param;

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
                for (int j = 0; j < Integer.parseInt(_param._kensuu); j++){
                    if (printMain(svf, studentList)) { // 生徒出力のメソッド
                        _hasData = true;
                    }
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
     * 生徒の出力（空白行あり）
     */
    private boolean printMain(final Vrw32alp svf, final List studentList) throws Exception {
//        final int PAGE_MAX_LINE = 45;
        boolean hasData = false;
        String form1 = null;
        String form2 = null;
        if (StringUtils.isNotBlank(_param._useFormNameA223B)) {
            form1 = _param._useFormNameA223B + ".frm";

            if (printStudentList(svf, studentList, form1)) {
                hasData = true;
            }
        } else {
            if ("1".equals(_param._komaFlg)) {
                form1 = "KNJA223B_5.frm";
            } else {
                if ("2".equals(_param._formDiv)) {
                    if ("1".equals(_param._ryomen)) {
                        form1 = "KNJA223B_4.frm"; // 表と同じ
                    } else {
                        form1 = "KNJA223B_3.frm";
                    }
                    form2 = "KNJA223B_4.frm";
                } else { // if ("1".equals(_param._formDiv)) {
                    form1 = "KNJA223B_1.frm";
                    form2 = "KNJA223B_2.frm";
                }
            }

            if (printStudentList(svf, studentList, form1)) {
                hasData = true;
            }

            if (!"1".equals(_param._komaFlg)) {
                if (printStudentList(svf, studentList, form2)) {
                    hasData = true;
                }
            }
        }
        return  hasData;
    }

    public boolean printStudentList(final Vrw32alp svf, final List studentList, final String form) {
        boolean refflg = false;
        svf.VrSetForm(form, 1);
        int max = -1;
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (NumberUtils.isDigits(student._attendno)) {
                max = Math.max(max, Integer.parseInt(student._attendno));
            }
            refflg = true;
        }
        for (int gyo = 1; gyo <= max; gyo++) {
            svf.VrsOutn("ATTENDNO", gyo, String.valueOf(gyo));
        }
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (NumberUtils.isDigits(student._attendno)) {
                printStudent(svf, Integer.parseInt(student._attendno), student);
            }
            refflg = true;
        }
        svf.VrEndPage();
        return refflg;
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

    private void printStudent(final Vrw32alp svf, final int gyo, final Student student) {

        final String teachername;
        if (StringUtils.isNotBlank(_param._useFormNameA223B) && "KNJA223B_6".equals(_param._useFormNameA223B)) {
            svf.VrsOut("DATE", _param._date);
            svf.VrsOut("CLASS_NAME" + (KNJ_EditEdit.getMS932ByteLength(student._hrName) <= 20 ? "1" : "2"), student._hrName);
            teachername = StringUtils.defaultString(student._staffname) + "　　" + StringUtils.defaultString(student._staffname2);
            svf.VrsOut("HR_NAME" + (KNJ_EditEdit.getMS932ByteLength(teachername) <= 20 ? "1" : "2"), teachername);

            String hrClass = StringUtils.defaultString(student._hrClassName1);
            String attendno = StringUtils.defaultString(student._attendno);
            svf.VrsOutn("CLASSNO", gyo, hrClass + attendno.substring(attendno.length() - 2));
        } else if ("1".equals(_param._komaFlg)) {
            svf.VrsOut("NENDO", _param._nendo);
            teachername = StringUtils.defaultString(student._staffname);
            svf.VrsOut("TR_NAME2", teachername);
        } else {
            if ("2".equals(_param._formDiv) && "1".equals(_param._kana)) {
                teachername = StringUtils.defaultString(student._staffnameKana) + "　　" + StringUtils.defaultString(student._staffnameKana2);
            } else {
                teachername = StringUtils.defaultString(student._staffname) + "　　" + StringUtils.defaultString(student._staffname2);
            }
            svf.VrsOut("HR_CLASS", StringUtils.defaultString(student._hrName) + "　　" + teachername);
        }

        // 男:空白、女:'*'
        svf.VrsOutn("MARK", gyo, student._sex);

        // 生徒漢字・規則に従って出力
        final String names;
        if ("2".equals(_param._formDiv) && "1".equals(_param._kana)) {
            String studentName = null;
            try {
                studentName = _param._staffInfo.getStrEngOrJp(student._name_kana, student._name_eng);
            } catch (Throwable t) {
                studentName = student._name_kana;
            }
            names = StringUtils.defaultString(studentName);
        } else {
            String studentName = null;
            try {
                studentName = _param._staffInfo.getStrEngOrJp(student._name, student._name_eng);
            } catch (Throwable t) {
                studentName = student._name;
            }
            names = StringUtils.defaultString(studentName);
        }
        if (StringUtils.isNotBlank(_param._useFormNameA223B) && "KNJA223B_6".equals(_param._useFormNameA223B)) {
            svf.VrsOutn("NAME" + (KNJ_EditEdit.getMS932ByteLength(names) <= 14 ? "1" : "2"), gyo, names);
        } else {
            final int z = names.indexOf("　"); // 空白文字の位置
            String strx = "";
            String stry = "";
            String field1 = null;
            String field2 = null;
            if (z != -1) {
                strx = names.substring(0, z); // 姓
                stry = names.substring(z + 1); // 名
                if (strx.length() == 1) {
                    field1 = "LNAME2"; // 姓１文字
                } else {
                    field1 = "LNAME1"; // 姓２文字以上
                }
                if (stry.length() == 1) {
                    field2 = "FNAME2"; // 名１文字
                } else {
                    field2 = "FNAME1"; // 名２文字以上
                }
            }
            if (z != -1 && strx.length() <= 4 && stry.length() <= 4) {
                svf.VrsOutn(field1, gyo, strx);
                svf.VrsOutn(field2, gyo, stry);
            } else {
                svf.VrsOutn("NAME", gyo, names);                   //空白がない
            }
        }
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
                    rs.getString("attendno"),
                    rs.getString("sex"),
                    rs.getString("name"),
                    rs.getString("name_kana"),
                    rs.getString("name_eng"),
                    rs.getString("HR_CLASS"),
                    rs.getString("hr_name"),
                    rs.getString("hr_nameabbv"),
                    rs.getString("hr_class_name1"),
                    rs.getString("staffname"),
                    rs.getString("staffname2"),
                    rs.getString("staffname_kana"),
                    rs.getString("staffname_kana2")
                );
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
        stb.append(" select ");
        stb.append("   value(W3.attendno,'') attendno,");
        stb.append("   value(W3.hr_class,'') HR_CLASS,");
        stb.append("   CASE WHEN W4.sex = '2' THEN '*' ELSE '' END AS sex, ");  // 男:空白、女:'*'
        stb.append("   value(W4.name,'') name,");
        stb.append("   value(W4.name_kana,'') name_kana,");
        stb.append("   value(W4.name_eng,'') name_eng,");
        stb.append("   value(W1.hr_name,'') hr_name,");
        stb.append("   value(W1.hr_nameabbv,'') hr_nameabbv,");
        stb.append("   value(W1.hr_class_name1,'') hr_class_name1,");
        stb.append("   value(W2.staffname,'') staffname, ");
        stb.append("   value(W5.staffname,'') staffname2, ");
        stb.append("   value(W2.staffname_kana,'') staffname_kana, ");
        stb.append("   value(W5.staffname_kana,'') staffname_kana2 ");
        stb.append(" FROM ");
        if ("2".equals(_param._hrClassType)) {
            stb.append("   schreg_regd_fi_dat W3");
        } else {
            stb.append("   schreg_regd_dat W3");
        }
        stb.append("   INNER JOIN schreg_base_mst W4 on W4.SCHREGNO = W3.SCHREGNO ");
        if ("2".equals(_param._hrClassType)) {
            stb.append("   INNER JOIN schreg_regd_fi_hdat W1 on W1.YEAR = W3.YEAR ");
        } else {
            stb.append("   INNER JOIN schreg_regd_hdat W1 on W1.YEAR = W3.YEAR ");
        }
        stb.append("       and W1.semester = W3.semester ");
        stb.append("       and W1.grade = W3.grade ");
        stb.append("       and W1.hr_class = W3.hr_class ");
        stb.append("   left join staff_mst W2 on W1.tr_cd1 = W2.staffcd ");
        stb.append("   left join staff_mst W5 on W1.tr_cd2 = W5.staffcd ");
        stb.append(" WHERE ");
        stb.append("   W1.year = '" + _param._year + "' and ");
        stb.append("   W1.semester = '" + _param._gakki + "' and ");
        stb.append("   W1.grade || W1.hr_class = '" + hrClass + "' ");
        stb.append(" order by W3.attendno");
        return stb.toString();
    }

    /** 生徒データクラス */
    private class Student {
        final String _attendno;
        final String _sex;
        final String _name;
        final String _name_kana;
        final String _name_eng;
        final String _hrClass;
        final String _hrName;
        final String _hrNameabbv;
        final String _hrClassName1;
        final String _staffname;
        final String _staffname2;
        final String _staffnameKana;
        final String _staffnameKana2;

        Student(
                final String attendno,
                final String sex,
                final String name,
                final String name_kana,
                final String name_eng,
                final String hrClass,
                final String hrName,
                final String hrNameabbv,
                final String hrClassName1,
                final String staffname,
                final String staffname2,
                final String staffnameKana,
                final String staffnameKana2
        ) {
            _attendno = attendno;
            _sex = sex;
            _name = name;
            _name_kana = name_kana;
            _name_eng = name_eng;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrNameabbv = hrNameabbv;
            _hrClassName1 = hrClassName1;
            _staffname = staffname;
            _staffname2 = staffname2;
            _staffnameKana = staffnameKana;
            _staffnameKana2 = staffnameKana2;
        }
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) {
        log.fatal("$Revision: 72768 $ $Date: 2020-03-06 12:23:31 +0900 (金, 06 3 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return new Param(db2, request);
    }

    private static class Param {

        final String _year;
        final String _gakki;
        final String _hrClassType;
        /** 出力件数 */
        final String _kensuu;
        /** 学年・組 */
        final String[] _classSelected;
        /** フォーム選択 1:45行x15列 2:40行x12列 */
        final String _formDiv;
        /** かな氏名印字 */
        final String _kana;
        /** 両面印字 */
        final String _ryomen;
        private final String _staffCd;
        private StaffInfo _staffInfo;

        final String _komaFlg;
        final String _useFormNameA223B;
        final String _date;

        private boolean _seirekiFlg;
        private String _nendo;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _komaFlg = request.getParameter("KOMA_FLG");
            _year      = request.getParameter("YEAR");
            _gakki  = request.getParameter("GAKKI");
            _hrClassType = ("1".equals(_komaFlg)) ? "1" : request.getParameter("HR_CLASS_TYPE");
            _kensuu = StringUtils.isBlank(request.getParameter("KENSUU")) ? "1" : request.getParameter("KENSUU");
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            _formDiv = ("1".equals(_komaFlg)) ? "1" : request.getParameter("FORM_DIV");
            _kana = request.getParameter("KANA");
            _ryomen = request.getParameter("RYOMEN");
            if ("1".equals(_komaFlg)) {
                _seirekiFlg = getSeirekiFlg(db2);
                _nendo = changePrintYear(db2, _year, _year + "-01-01");
            }
            _staffCd = request.getParameter("PRINT_LOG_STAFFCD");
            _useFormNameA223B = request.getParameter("useFormNameA223B");
            _date = request.getParameter("DATE");

            try {
                _staffInfo = new StaffInfo(db2, _staffCd);
            } catch (Throwable t) {
                log.error("exception!", t);
            }
        }

        private boolean getSeirekiFlg(final DB2UDB db2) {
            boolean seirekiFlg = false;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if ("2".equals(rs.getString("NAME1"))) {
                        seirekiFlg = true; //西暦
                    }
                }
            } catch (Exception e) {
                log.error("getSeirekiFlg Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return seirekiFlg;
        }

        public String changePrintYear(final DB2UDB db2, final String year, final String date) {
            if (null == year) {
                return "";
            }
            if (_seirekiFlg) {
                return String.valueOf(Integer.parseInt(year)) + "年度";
            } else {
                final String[] gengou = KNJ_EditDate.tate_format4(db2, date);
                return gengou[0] + gengou[1] + "年度";
            }
        }
    }

}// クラスの括り
