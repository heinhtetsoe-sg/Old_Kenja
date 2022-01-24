package servletpack.KNJA;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 *
 *	学校教育システム 賢者 [学籍管理]
 *
 *					＜ＫＮＪＡ２３３Ｅ＞  講座別名列
 *
 *	2018/05/09 yogi 作成日
 */

public class KNJA233E {

    private static final Log log = LogFactory.getLog(KNJA233E.class);

    private final String OUTPUT_MUSASHI = "musashi";
    private final String OUTPUT1 = "1";
    private final String OUTPUT2 = "2";
    private final String OUTPUT3 = "3";
    private final String OUTPUT4 = "4";

    /**
     * HTTP Get リクエストの処理
     */
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        log.fatal("$Revision: 62603 $ $Date: 2018-10-02 11:44:28 +0900 (火, 02 10 2018) $"); // CVSキーワードの取り扱いに注意

        Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;						//Databaseクラスを継承したクラス

        //	print設定
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();

        //	svf設定
        svf.VrInit();						   	//クラスの初期化
        svf.VrSetSpoolFileStream(outstrm);   		//PDFファイル名の設定

        //	ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch (Exception ex) {
            log.error("DB2 open error!", ex);
        }

        Param param = null;
        //  ＳＶＦ作成処理
        boolean nonedata = false;                               //該当データなしフラグ
        try {
            param = new Param(request, db2);
            KNJServletUtils.debugParam(request, log);

            String formName = null;
            formName = "KNJA233E.frm";
            svf.VrSetForm(formName, 1);
            //log.debug("form = " + formName);

            //SVF出力
            nonedata = printMain(db2, svf, param);
            if (nonedata) {
                svf.VrEndPage();					//SVFフィールド出力
            }

        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            //	該当データ無し
            if (!nonedata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }

            // 	終了処理
            svf.VrQuit();
            db2.close();				//DBを閉じる
            outstrm.close();			//ストリームを閉じる
        }

    }//doGetの括り

    private boolean printMain(final DB2UDB db2, final Vrw32alp svf, final Param param)
    {
        boolean nonedata = false;
        final StringTokenizer stz1 = new StringTokenizer(param._attendclasscd, ",", false);  //講座コード
        final StringTokenizer stz2 = new StringTokenizer(param._nameShow, ",", false);	       //職員コード
        //final StringTokenizer stz3 = new StringTokenizer(param._chargeDiv, ",", false);      //担任区分
        final StringTokenizer stz4 = new StringTokenizer(param._appdate, ",", false);        //適用開始日付
        while (stz1.hasMoreTokens()){
            param._attendclasscd = stz1.nextToken();    //講座コード
            param._nameShow = stz2.nextToken();	        //職員コード
            //param._chargeDiv = stz3.nextToken();      //担任区分
            param._appdate = stz4.nextToken();	        //適用開始日付
            setChairname(db2, param);                   //講座出力のメソッド
            setStaffname(db2, param);                   //担任出力のメソッド
            boolean outputdatflg = false;
            if (Set_Detail_1(db2, svf, param)) {
                nonedata = true;
                outputdatflg = true;
            }
            if (outputdatflg) {
            	svf.VrEndPage();
            }
        }
        return nonedata;
    }

    /**SVF-FORM**/
    private boolean Set_Detail_1(final DB2UDB db2, final Vrw32alp svf, final Param param)
    {
        final int maxGyo = 50;
        boolean nonedata = false;

        setTitle(db2, svf, param);

        PreparedStatement ps1 = null;
        ResultSet rs = null;
        try {
            ps1 = db2.prepareStatement(Pre_Stat1(param));
            int pp = 0;
            ps1.setString(++pp, param._attendclasscd);	//講座コード
            //log.debug("p1:"+param._attendclasscd);
            ps1.setDate(++pp, Date.valueOf(param._appdate));	//適用開始日付
            //log.debug("p2:"+Date.valueOf(param._appdate));
            rs = ps1.executeQuery();

            int gyo = 1;			//行数カウント用
            int ban = 1;			//連番

            while (rs.next()) {
                if (gyo > maxGyo) {
                    svf.VrEndPage();					//SVFフィールド出力
                    setTitle(db2, svf, param);
                    gyo = 1;
                }
                svf.VrsOutn("NO", gyo, String.valueOf(ban));
                svf.VrsOutn("SCHREGNO", gyo, rs.getString("SCHREGNO"));
                svf.VrsOutn("HR_NAME", gyo, rs.getString("HR_NAME"));
                final int slen = KNJ_EditEdit.getMS932ByteLength(rs.getString("NAME"));
                String nameidx = "";
                if (slen <= 20) {
                	nameidx = "1";
                } else if (slen <= 30) {
                	nameidx = "2";
                } else {
                	nameidx = "3";
                }
                svf.VrsOutn("NAME" + nameidx, gyo, rs.getString("NAME"));
                final int klen = KNJ_EditEdit.getMS932ByteLength(rs.getString("NAME_KANA"));
                String kanaidx = klen <= 30 ? "1" : "2";
                svf.VrsOutn("KANA"+kanaidx, gyo, rs.getString("NAME_KANA"));

                nonedata = true;
                gyo++;			//行数カウント用
                ban++;			//連番
            }
        } catch (Exception ex) {
            log.error("Set_Detail_1 read error!", ex);
        } finally {
            if (ps1 != null) {
                DbUtils.closeQuietly(null, ps1, rs);
            }
            db2.commit();
        }
        return nonedata;

    }//Set_Detail_1()の括り

    private void setTitle(final DB2UDB db2, final Vrw32alp svf, final Param param) {
        final String prttransferyear = KNJ_EditDate.h_format_JP_N(param._year+"/04/01");
        svf.VrsOut("TITLE", prttransferyear + "度 座席表");
        svf.VrsOut("CHAIR_NAME", param._attendclasscd + "　" + param._chairname);
    }

//    private String getmyouji(final String cutstaffname) {
//        String retstr;
//        final int idxwk1 = cutstaffname.indexOf("　");// < 0 ? 0 : cutstaffname.indexOf("　");
//        final int idxwk2 = cutstaffname.indexOf(" ");// < 0 ? 0 : cutstaffname.indexOf(" ");
//        final int idxSpace = (idxwk1 >= 0 && idxwk2 >= 0 && idxwk1 > idxwk2) ? idxwk2 : idxwk1 < 0 ? idxwk2 : idxwk1;                  //空白文字の位置
//        if (idxSpace >= 0) {
//            final String sei = cutstaffname.substring(0, idxSpace);
//            final String mei = cutstaffname.substring(idxSpace + 1);
//            final int chkmeiidx = mei.indexOf("　") > mei.indexOf(" ") ? mei.indexOf(" ") : mei.indexOf("　");                  //空白文字の位置
//            if (0 <= chkmeiidx) {
//                retstr = cutstaffname;
//            } else {
//                retstr = sei;
//            }
//        } else {
//            retstr = cutstaffname;
//        }
//        return retstr;
//   	}

    /**SVF-FORM**/
    private void setChairname(final DB2UDB db2, final Param param)
    {
        PreparedStatement ps2 = null;
        ResultSet rs = null;
        try {
            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT value(CHAIRNAME,'') AS CHAIRNAME FROM CHAIR_DAT ");
            stb.append("WHERE YEAR = '" + param._year + "' AND SEMESTER = '" + param._semester + "' AND CHAIRCD = ? ");
            ps2 = db2.prepareStatement(stb.toString());
            //log.debug("chair_sql:"+stb.toString());
            int pp = 0;
            ps2.setString(++pp, param._attendclasscd);	//講座コード
            rs = ps2.executeQuery();

            if (rs.next()) {
                param._chairname = rs.getString("CHAIRNAME");		//講座名称
            } else {
                param._chairname = "";
            }
        } catch (Exception ex) {
            log.error("Set_Detail_2 read error!", ex);
        } finally {
            if (ps2 != null) {
                DbUtils.closeQuietly(null, ps2, rs);
            }
            db2.commit();
        }

    }//Set_Detail_2()の括り

    /**SVF-FORM**/
    private void setStaffname(final DB2UDB db2, final Param param)
    {
    	PreparedStatement ps3 = null;
        ResultSet rs = null;
        try {
            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT value(STAFFNAME,'') AS STAFFNAME FROM STAFF_MST WHERE STAFFCD = ? ");
            ps3 = db2.prepareStatement(stb.toString());
            //log.debug("staff_sql:"+stb.toString());

            int pp = 0;
            ps3.setString(++pp, param._nameShow);	//職員コード
            rs = ps3.executeQuery();

            if (rs.next()) {
                param._staffname = rs.getString("STAFFNAME");		//職員名称
            } else {
                param._staffname = "";
            }
        } catch (Exception ex) {
            log.error("Set_Detail_3 read error!", ex);
        } finally {
        	if (ps3 != null) {
                DbUtils.closeQuietly(null, ps3, rs);
        	}
            db2.commit();
        }

    }//Set_Detail_3()の括り

    /**PrepareStatement作成**/
    private String Pre_Stat1(final Param param)
    {
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT ");
        stb.append("    T1.SCHREGNO, ");
        stb.append(" '' AS SEX, ");  // 男:空白、女:''
        stb.append("    value(T6.HR_NAME,'') HR_NAME, ");
        stb.append("    value(T6.HR_NAMEABBV,'') HR_NAMEABBV, ");
        stb.append("    value(T1.NAME,'') NAME, ");
        stb.append("    value(T1.NAME_KANA,'') NAME_KANA, ");
        stb.append("    value(TRANSLATE_KANA(T1.NAME_KANA), '') SORT_KANA, ");
//        stb.append("    value(T2.GRADE,'') GRADE, ");
//        stb.append("    value(T2.HR_CLASS,'') HR_CLASS, ");
        stb.append("    value(T2.ATTENDNO,'') ATTENDNO ");
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
        stb.append("    LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T2.YEAR ");
        stb.append("        AND GDAT.GRADE = T2.GRADE ");
        stb.append("    LEFT JOIN SCHREG_ENT_GRD_HIST_DAT ENTGRD ON ENTGRD.SCHREGNO = T7.SCHREGNO ");
        stb.append("        AND ENTGRD.SCHOOL_KIND = GDAT.SCHOOL_KIND ");
        stb.append("WHERE ");
        stb.append("    T7.YEAR = '" + param._year + "' AND ");
        stb.append("    T7.SEMESTER = '" + param._semester + "' AND ");
        stb.append("    T7.CHAIRCD = ? AND ");
        stb.append("    T7.APPDATE = ? ");
        if ("1".equals(param._grdDiv)) {
        	stb.append("    AND VALUE(ENTGRD.GRD_DIV, '') NOT IN ('2', '3') ");
        }
        stb.append("    AND '" + param._date + "' BETWEEN T7.APPDATE AND T7.APPENDDATE ");
        stb.append("ORDER BY ");
        stb.append("    CASE WHEN SORT_KANA IS NULL THEN 0 WHEN SORT_KANA = '' THEN 0 ELSE 1 END DESC, SORT_KANA ASC, ");
        stb.append("    T1.SCHREGNO ");
        //log.debug("Pre_Stat_sql:"+stb.toString());
        return stb.toString();

    }//Pre_Stat1()の括り

    private static class Param {
        final String _year;
        final String _semester;
        String _attendclasscd;
        String _nameShow;
        String _chairname;
        String _staffname;
        String _appdate;
        final String _grdDiv;
        final String _date;
        public Param(final HttpServletRequest request, final DB2UDB db2) {

            _year = request.getParameter("YEAR");                                 //年度
            _semester = request.getParameter("SEMESTER");                         //学期
            _attendclasscd = request.getParameter("ATTENDCLASSCD");               //講座コード
            _nameShow = request.getParameter("NAME_SHOW");                        //科目担任名（職員コード）
            _appdate = request.getParameter("APPDATE");                           //適用開始日付
            _grdDiv = request.getParameter("GRD_DIV");                            //転退学生を出力しない
            _date = request.getParameter("DATE").replace('/', '-');               //対象日
        }

    }

}//クラスの括り
