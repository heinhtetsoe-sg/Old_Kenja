package servletpack.KNJA;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

/**
 *
 *	学校教育システム 賢者 [学籍管理]
 *
 *					＜ＫＮＪＡ233F＞  講座名簿
 *
 */

public class KNJA233F {

    private static final Log log = LogFactory.getLog(KNJA233F.class);
    private boolean _hasData;
    private Param _param;

    /**
     * HTTP Get リクエストの処理
     */
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
    {
        Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;						//Databaseクラスを継承したクラス

        //	ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch (Exception ex) {
            log.error("DB2 open error!", ex);
            return;
        }

        final Param param = createParam(request, db2);

        PreparedStatement ps1 = null;
        try {
            //  print設定
            response.setContentType("application/pdf");
            OutputStream outstrm = response.getOutputStream();

            //  svf設定
            svf.VrInit();                           //クラスの初期化
            svf.VrSetSpoolFileStream(outstrm);          //PDFファイル名の設定

            //SQL作成
            ps1 = db2.prepareStatement(Pre_Stat1(param, false));       //生徒preparestatement

            for (int i = 0; i < param._classSelected.length; i++) {
                if (printMain(db2, svf, param, param._classSelected[i], ps1)) {
                    _hasData = true;        //生徒出力のメソッド
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

    private Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 74137 $ $Date: 2020-05-07 14:10:50 +0900 (木, 07 5 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return new Param(request, db2);
    }

    /**SVF-FORM**/
    private boolean printMain(final DB2UDB db2, final Vrw32alp svf, final Param param, final String classSelected, final PreparedStatement ps1)
    {
        boolean hasdata = false;
        final int PAGE_MAX_LINE = 45;
        int cutLine = 0;

        svf.VrSetForm("KNJA233F.frm", 1);

        final String[] split = StringUtils.split(classSelected, ",");
        final String staffcd = split[2];
        final String appdate = split[3];
        final String chaircd = split[4];
        log.info(" sql_param:" + staffcd + "," + appdate + "," + chaircd);

        //出力クラス数を数える
        PreparedStatement ps2 = null;
        ResultSet rs2 = null;
        int clsCnt = 0;
        try {
            ps2 = db2.prepareStatement(Pre_Stat1(param, true));       //生徒preparestatement
            int pp = 0;
            ps2.setString(++pp, staffcd);
            ps2.setString(++pp, chaircd);	//講座コード
            ps2.setDate(++pp,Date.valueOf(appdate));	//適用開始日付
            rs2 = ps2.executeQuery();
            while (rs2.next()) {
                clsCnt++;
            }
        } catch (Exception ex) {
            log.error("printMain_1_1 read error!", ex);
        } finally {
            DbUtils.closeQuietly(rs2);
        }

        ResultSet rs = null;
        try {
            int pp = 0;
            ps1.setString(++pp, staffcd);
            ps1.setString(++pp, chaircd);	//講座コード
            ps1.setDate(++pp,Date.valueOf(appdate));	//適用開始日付
            rs = ps1.executeQuery();

            int putLineCnt = 1;
            while (rs.next()) {

                int gyo = (param._isEraseMargin && clsCnt > 1) ? putLineCnt : (Integer.parseInt(rs.getString("ATTENDNO")) - cutLine);

                if(gyo > PAGE_MAX_LINE) {
                    //45レコード以降は改ページ
                    svf.VrEndPage();
                    cutLine = cutLine + PAGE_MAX_LINE;
                    gyo = param._isEraseMargin ? 1 : Integer.parseInt(rs.getString("ATTENDNO")) - cutLine;
                }
                for (int len = 1; len <= 3; len++) {
                    final String slen = String.valueOf(len);
                    svf.VrsOut("CHAIR_CD" + slen, StringUtils.defaultString(rs.getString("CHAIRCD"))); //講座コード
                    svf.VrsOut("CHAIR_NAME" + slen, StringUtils.defaultString(rs.getString("CHAIRNAME"))); //講座名
                    svf.VrsOut("STAFF_NAME" + slen, StringUtils.defaultString(rs.getString("STAFFNAME"))); //講座担任

                    final String hrClass = StringUtils.stripStart(rs.getString("HR_CLASS"),"0");
                    svf.VrsOutn("HR" + slen, gyo, hrClass); //組
                    final String attendNo = StringUtils.stripStart(rs.getString("ATTENDNO"),"0");
                    svf.VrsOutn("ATTENDNO" + slen, gyo, attendNo); //出席番号
                    final String kana = rs.getString("NAME_KANA");
                    final int kanaLen = getMS932ByteLength(kana);
                    final String kanaField = kanaLen > 18 ? "_2" : "";
                    svf.VrsOutn("KANA1" + kanaField, gyo, kana); //ふりがな

                    // 男:空白 女:'*'
                    svf.VrsOutn("MARK" + slen, gyo, rs.getString("SEX"));

                    // 生徒漢字・規則に従って出力
                    final String names = StringUtils.defaultString(rs.getString("NAME"));
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
                putLineCnt++;
                hasdata = true;
            }
        } catch (Exception ex) {
            log.error("printMain_1_2 read error!", ex);
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        svf.VrEndPage();
        return hasdata;
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

    /**PrepareStatement作成**/
    private String Pre_Stat1(final Param param, final boolean distinctFlg)
    {
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT ");
        if (distinctFlg) {
            stb.append("DISTINCT ");
        }
        stb.append("    T7.CHAIRCD, ");
        stb.append("    T8.CHAIRNAME, ");
        stb.append("    T9.STAFFNAME, ");
        stb.append("    value(T2.GRADE,'') GRADE, ");
        stb.append("    value(T6.HR_NAME,'') HR_NAME, ");
        stb.append("    value(T2.HR_CLASS,'') HR_CLASS ");
        if (!distinctFlg) {
            stb.append(" , ");
            stb.append("    value(T2.ATTENDNO,'') ATTENDNO, ");
            stb.append("    CASE WHEN T1.SEX = '2' THEN '*' ELSE '' END AS SEX, ");//NO001 男:空白、女:'*'
            stb.append("    value(T1.NAME,'') NAME, ");
            stb.append("    value(T1.NAME_KANA,'') NAME_KANA ");
        }
        stb.append("FROM ");
        stb.append("    CHAIR_STD_DAT T7 ");
        stb.append("    INNER JOIN SCHREG_BASE_MST T1 ON T1.SCHREGNO = T7.SCHREGNO ");
        stb.append("    INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T7.SCHREGNO ");
        stb.append("        AND T2.YEAR = T7.YEAR ");
        stb.append("        AND T2.SEMESTER = T7.SEMESTER ");
        stb.append("    INNER JOIN SCHREG_REGD_HDAT T6 ON T6.YEAR = T2.YEAR ");
        stb.append("        AND T6.SEMESTER = T2.SEMESTER ");
        stb.append("        AND T6.GRADE = T2.GRADE ");
        stb.append("        AND T6.HR_CLASS = T2.HR_CLASS ");
        stb.append("    INNER JOIN CHAIR_DAT T8 ON T8.YEAR = T7.YEAR ");
        stb.append("        AND T8.SEMESTER = T7.SEMESTER ");
        stb.append("        AND T8.CHAIRCD = T7.CHAIRCD ");
        stb.append("    LEFT JOIN STAFF_MST T9 ON T9.STAFFCD = ? ");
        stb.append("    LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T2.YEAR ");
        stb.append("        AND GDAT.GRADE = T2.GRADE ");
        stb.append("    LEFT JOIN SCHREG_ENT_GRD_HIST_DAT ENTGRD ON ENTGRD.SCHREGNO = T7.SCHREGNO ");
        stb.append("        AND ENTGRD.SCHOOL_KIND = GDAT.SCHOOL_KIND ");
        stb.append(" WHERE ");
        stb.append("    T7.YEAR = '"+param._year+"' AND ");
        stb.append("    T7.SEMESTER = '"+param._semester+"' AND ");
        stb.append("    T7.CHAIRCD =? AND ");
        stb.append("    T7.APPDATE =? ");
        if ("1".equals(param._grdDiv)) {
        	stb.append("    AND VALUE(ENTGRD.GRD_DIV, '') NOT IN ('2', '3') ");
        }
        stb.append("ORDER BY ");
        stb.append("    GRADE,HR_CLASS ");
        if (!distinctFlg) {
            stb.append(" ,ATTENDNO ");
        }

        return stb.toString();

    }

    private static class Param {
        final String _year;
        final String _semester;
        final String[] _classSelected;
        final String _grdDiv;
        final boolean _isEraseMargin;
        public Param(final HttpServletRequest request, final DB2UDB db2) {

            _year = request.getParameter("YEAR");                                //年度
            _semester = request.getParameter("SEMESTER");                            //学期
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            _grdDiv = request.getParameter("GRD_DIV");  //転退学生を出力しない
            _isEraseMargin = true;   //余白を詰める
        }
    }

}//クラスの括り
