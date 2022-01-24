package servletpack.KNJA;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;


/**
 * HRクラス名簿
 */
public class KNJA224F {

    private static final Log log = LogFactory.getLog(KNJA224F.class);
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
     * 生徒の出力（空白行あり）
     */
    private boolean printMain(final Vrw32alp svf, final List studentList) throws Exception {
        final int PAGE_MAX_LINE = 45;
        int cutLine = 0;
        boolean hasData = false;

        svf.VrSetForm("KNJA224F.frm", 1);
        int max = -1;
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (NumberUtils.isDigits(student._attendno)) {
                max = Math.max(max, Integer.parseInt(student._attendno));
            }
        }
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            int gyo = Integer.parseInt(student._attendno) - cutLine;

            if(gyo > PAGE_MAX_LINE) {
                //45レコード以降は改ページ
                svf.VrEndPage();
                cutLine = cutLine + PAGE_MAX_LINE;
                gyo = Integer.parseInt(student._attendno) - cutLine;
            }

            for (int len = 1; len <= 3; len++) {
                final String slen = String.valueOf(len);
                svf.VrsOut("HR_NAME" + slen, student._hrName); //学級
                final String hrClass = StringUtils.stripStart(student._hrClass,"0");
                svf.VrsOutn("HR" + slen, gyo, hrClass); //組
                final String attendNo = StringUtils.stripStart(student._attendno,"0");
                svf.VrsOutn("ATTENDNO" + slen, gyo, attendNo); //出席番号
                final int kanaLen = getMS932ByteLength(student._name_kana);
                final String kanaField = kanaLen > 18 ? "_2" : "";
                svf.VrsOutn("KANA1" + kanaField, gyo, student._name_kana); //ふりがな

                // 男:空白 女:'*'
                svf.VrsOutn("MARK" + slen, gyo, student._sex);

                // 生徒漢字・規則に従って出力
                final String names = StringUtils.defaultString(student._name);
                final int z = names.indexOf("　"); // 空白文字の位置
                String strx = "";
                String stry = "";
                String field1 = null;
                String field2 = null;
                if (z != -1) {
                    strx = names.substring(0, z); // 姓
                    stry = names.substring(z + 1); // 名
                    if (strx.length() == 1) {
                        field1 = "LNAME" + slen + "_2"; // 姓１文字
                    } else {
                        field1 = "LNAME" + slen + "_1"; // 姓２文字以上
                    }
                    if (stry.length() == 1) {
                        field2 = "FNAME" + slen + "_2"; // 名１文字
                    } else {
                        field2 = "FNAME" + slen + "_1"; // 名２文字以上
                    }
                }
                if (z != -1 && strx.length() <= 4 && stry.length() <= 4) {
                    final String lNameField = getMS932ByteLength(strx) > 10 ? "_2" : getMS932ByteLength(strx) > 8 ? "" : "_3";
                    final String fNameField = getMS932ByteLength(stry) > 10 ? "_2" : getMS932ByteLength(stry) > 8 ? "" : "_3";
                    svf.VrsOutn(field1 + lNameField, gyo, strx); //性
                    svf.VrsOutn(field2 + fNameField, gyo, stry); //名
                } else {
                    svf.VrsOutn("NAME" + slen, gyo, names); //空白がない
                }
            }

            hasData = true;
        }
        svf.VrEndPage();
        return  hasData;
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
                      rs.getString("HR_CLASS"),
                      rs.getString("ATTENDNO"),
                      rs.getString("SEX"),
                      StringUtils.defaultString(rs.getString("NAME")),
                      rs.getString("NAME_KANA")
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
        stb.append(" SELECT ");
        stb.append("   T1.HR_NAME,");
        stb.append("   T2.HR_CLASS,");
        stb.append("   T2.ATTENDNO,");
        if ("1".equals(_param._nameNasi)) {
            stb.append("   CASE WHEN T3.GRD_DIV IN ('1','2','3') THEN '' ELSE CASE WHEN T3.SEX = '2' THEN '*' ELSE '' END END AS SEX, ");  // 男:空白、女:'*';
            stb.append("   CASE WHEN T3.GRD_DIV IN ('1','2','3') THEN '' ELSE T3.NAME END AS NAME,");
            stb.append("   CASE WHEN T3.GRD_DIV IN ('1','2','3') THEN '' ELSE T3.NAME_KANA END AS NAME_KANA ");
        }else {
            stb.append("   CASE WHEN T3.SEX = '2' THEN '*' ELSE '' END AS SEX, ");  // 男:空白、女:'*';
            stb.append("   T3.NAME,");
            stb.append("   T3.NAME_KANA ");
        }
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_HDAT T1");
        stb.append("   INNER JOIN SCHREG_REGD_DAT T2   ");
        stb.append("      ON T2.YEAR     = T1.YEAR     ");
        stb.append("     AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("     AND T2.GRADE    = T1.GRADE    ");
        stb.append("     AND T2.HR_CLASS = T1.HR_CLASS ");
        stb.append("   INNER JOIN SCHREG_BASE_MST T3 ");
        stb.append("      ON T3.SCHREGNO = T2.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR                 = '" + _param._year + "' and ");
        stb.append("   T1.SEMESTER             = '" + _param._gakki + "' and ");
        stb.append("   T1.GRADE || T1.HR_CLASS = '" + hrClass + "' ");
        stb.append(" ORDER BY T2.ATTENDNO");
        return stb.toString();
    }

    /** 生徒データクラス */
    private class Student {

        final String _hrName;
        final String _hrClass;
        final String _attendno;
        final String _sex;
        final String _name;
        final String _name_kana;

        Student(
                final String hrName,
                final String hrClass,
                final String attendno,
                final String sex,
                final String name,
                final String name_kana
        ) {

            _hrName = hrName;
            _hrClass = hrClass;
            _attendno = attendno;
            _sex = sex;
            _name = name;
            _name_kana = name_kana;
        }
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) {
        log.fatal("$Revision: 66930 $ $Date: 2019-04-11 16:54:59 +0900 (木, 11 4 2019) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return new Param(db2, request);
    }

    private static class Param {

        final String _year;
        final String _nameNasi;
        final String _gakki;
        final String[] _classSelected;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year      = request.getParameter("YEAR");
            _nameNasi = request.getParameter("NAME_NASI");
            _gakki  = request.getParameter("GAKKI");
            _classSelected = request.getParameterValues("CLASS_SELECTED");
        }

    }

}// クラスの括り
