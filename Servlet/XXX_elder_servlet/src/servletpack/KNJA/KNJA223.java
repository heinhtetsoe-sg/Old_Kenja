package servletpack.KNJA;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * $Id: 96f9528553c84b26085a807a81abf9a8fe7e0d9f $
 * 学校教育システム 賢者 [学籍管理] ＜ＫＮＪＡ２２３＞ ＨＲ別名列（東京都） 2006/05/25 alp o-naka
 * 新規作成。旧プログラムＩＤ(KNJA220)の変更（近大と重複しているため） alp o-naka NO001
 * 氏名の前に女性は'*'を表示する(男:空白、女:'*')
 */

public class KNJA223 {

    private static final Log log = LogFactory.getLog(KNJA223.class);

    int len = 0; // 列数カウント用

    /**
     * HTTP Get リクエストの処理
     */
    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Vrw32alp svf = new Vrw32alp(); // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null; // Databaseクラスを継承したクラス
        String[] param = new String[5];

        // パラメータの取得
        String classcd[] = request.getParameterValues("CLASS_SELECTED"); // 学年・組
        try {
            param[0] = request.getParameter("YEAR"); // 年度
            param[1] = request.getParameter("GAKKI"); // 学期
            param[2] = request.getParameter("OUTPUT"); // 名票種類
            param[3] = request.getParameter("KENSUU"); // 出力件数
            param[4] = request.getParameter("OUTPUT2"); // 空白行なし
        } catch (Exception ex) {
            log.error("parameter error!", ex);
        }

        // print設定
        new PrintWriter(response.getOutputStream());
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();

        // svf設定
        svf.VrInit(); // クラスの初期化
        svf.VrSetSpoolFileStream(outstrm); // PDFファイル名の設定

        // ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch (Exception ex) {
            log.error("DB2 open error!", ex);
        }

        // ＳＶＦ作成処理
        PreparedStatement ps1 = null;
        boolean nonedata = false; // 該当データなしフラグ
        Set_Head(db2, svf, param); // 見出し出力のメソッド
        for (int ia = 0; ia < param.length; ia++)
            log.debug("param[" + ia + "]=" + param[ia]);
        // SQL作成
        try {
            ps1 = db2.prepareStatement(Pre_Stat1(param)); // 生徒preparestatement
        } catch (Exception ex) {
            log.error("DB2 open error!", ex);
        }
        // SVF出力
        for (int ia = 0; ia < classcd.length; ia++) {
            for (int ib = 0; ib < Integer.parseInt(param[3]); ib++)
                if (null == param[4]) {
                    if (Set_Detail_1(db2, svf, param, classcd[ia], ps1))
                        nonedata = true; // 生徒出力のメソッド
                } else {
                    if (Set_Detail_2(db2, svf, param, classcd[ia], ps1))
                        nonedata = true; // 生徒出力のメソッド
                }
        }
        if (nonedata)
            svf.VrEndPage(); // SVFフィールド出力

        // 該当データ無し
        if (!nonedata) {
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note", "note");
            svf.VrEndPage();
        }

        // 終了処理
        svf.VrQuit();
        DbUtils.closeQuietly(ps1);
        db2.commit();
        db2.close(); // DBを閉じる
        outstrm.close(); // ストリームを閉じる

    }// doGetの括り

    /** SVF-FORM * */
    private void Set_Head(DB2UDB db2, Vrw32alp svf, String[] param) {

        if (param[2].equals("1"))
            svf.VrSetForm("KNJA223_1.frm", 1);
        else
            svf.VrSetForm("KNJA223_2.frm", 1);

        // ＳＶＦ属性変更--->出力形式がクラス別の場合クラス毎に改ページ
        // ret = svf.VrAttribute("HR_NAME1","FF=1");

    }// Set_Head()の括り
    
    private static int getMS932ByteLength(final String s) {
        int rtn = 0;
        if (null != s) {
            try {
                rtn = s.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return rtn;
    }

    /** SVF-FORM* */
    private boolean Set_Detail_1(DB2UDB db2, Vrw32alp svf, String[] param, String classcd, PreparedStatement ps1) {
        boolean nonedata = false;
        ResultSet rs = null;
        try {
            int pp = 0;
            ps1.setString(++pp, classcd); // 学年・組
            rs = ps1.executeQuery();
            // log.debug("Set_Detail_1 sql ok!");

            len++;
            boolean len_flg = false;

            while (rs.next()) {
                int gyo = rs.getInt("ATTENDNO"); // 行数カウント用
                int atdno = gyo; // 出席番号用
                if (gyo > 50) {
                    gyo = gyo - 50;
                    if (!len_flg)
                        len++;
                    len_flg = true;
                }
                if (len > 3) {
                    len = 1;
                    svf.VrEndPage(); // SVFフィールド出力
                }
                // 組略称・担任名出力
                svf.VrsOut("HR_NAME" + String.valueOf(len), rs.getString("HR_NAMEABBV"));
                svf.VrsOut("STAFFNAME" + String.valueOf(len) + "_1", rs.getString("STAFFNAME"));

                // 出席番号・かな出力
                svf.VrsOutn("ATTENDNO" + String.valueOf(len), gyo, String.valueOf(atdno));
                final int kanalen = getMS932ByteLength(rs.getString("NAME_KANA"));
                if (kanalen > 18) {
                    svf.VrsOutn("KANA" + String.valueOf(len) + "_2", gyo, rs.getString("NAME_KANA"));
                } else {
                    svf.VrsOutn("KANA" + String.valueOf(len), gyo, rs.getString("NAME_KANA"));
                }
                svf.VrsOutn("MARK" + String.valueOf(len), gyo, rs.getString("SEX"));// NO001
                                                                                    // 男:空白、女:'*'

                // 生徒漢字・規則に従って出力
                String strz = rs.getString("NAME");
                final int namelen = getMS932ByteLength(strz);
                int z = strz.indexOf("　"); // 空白文字の位置
                String strx = ""; // 姓
                String stry = ""; // 名
                int ketax = 0;
                int ketay = 0;
                if (z >= 0) {
                    strx = strz.substring(0, z); // 姓
                    stry = strz.substring(z + 1); // 名
                    ketax = getMS932ByteLength(strx);
                    ketay = getMS932ByteLength(stry);
                }
                if (namelen > 18) {
                    svf.VrsOutn("NAME" + String.valueOf(len) + "_2", gyo, strz);
                } else if (z < 0 || (ketax > 8 || ketay > 8)) {
                    svf.VrsOutn("NAME" + String.valueOf(len), gyo, strz); // 空白がない
                } else {
                    strx = strz.substring(0, z);
                    stry = strz.substring(z + 1);
                    if (0 <= stry.indexOf("　")) {
                        svf.VrsOutn("NAME" + String.valueOf(len), gyo, strz); // 空白が２つ以上
                    } else {
                        int x = strx.length(); // 姓の文字数
                        int y = stry.length(); // 名の文字数
                        if (x == 1) {
                            svf.VrsOutn("LNAME" + String.valueOf(len) + "_2", gyo, strx); // 姓１文字
                        } else {
                            svf.VrsOutn("LNAME" + String.valueOf(len) + "_1", gyo, strx); // 姓２文字以上
                        }
                        if (y == 1) {
                            svf.VrsOutn("FNAME" + String.valueOf(len) + "_2", gyo, stry); // 名１文字
                        } else {
                            svf.VrsOutn("FNAME" + String.valueOf(len) + "_1", gyo, stry); // 名２文字以上
                        }
                    }
                }

                nonedata = true;
            }
            // log.debug("Set_Detail_1 read ok!");
        } catch (Exception ex) {
            log.error("Set_Detail_1 read error!", ex);
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return nonedata;

    }// Set_Detail_1()の括り

    /** SVF-FORM* */
    private boolean Set_Detail_2(DB2UDB db2, Vrw32alp svf, String param[], String classcd, PreparedStatement ps1) {
        boolean nonedata = false;
        ResultSet rs = null;
        try {
            int pp = 0;
            ps1.setString(++pp, classcd); // 学年・組
            rs = ps1.executeQuery();

            int gyo = 1; // 行数カウント用
            len++;
            while (rs.next()) {
                if (gyo > 50) {
                    gyo = 1;
                    len++;
                }
                if (len > 3) {
                    len = 1;
                    svf.VrEndPage(); // SVFフィールド出力
                }
                // 組略称・担任名出力
                svf.VrsOut("HR_NAME" + String.valueOf(len), rs.getString("HR_NAMEABBV"));
                svf.VrsOut("STAFFNAME" + String.valueOf(len) + "_1", rs.getString("STAFFNAME"));

                // 出席番号・かな出力
                svf.VrsOutn("ATTENDNO" + String.valueOf(len), gyo, rs.getString("ATTENDNO"));
                final int kanalen = getMS932ByteLength(rs.getString("NAME_KANA"));
                if (kanalen > 18) {
                    svf.VrsOutn("KANA" + String.valueOf(len) + "_2", gyo, rs.getString("NAME_KANA")); // 空白がない
                } else {
                    svf.VrsOutn("KANA" + String.valueOf(len), gyo, rs.getString("NAME_KANA"));
                }
                svf.VrsOutn("MARK" + String.valueOf(len), gyo, rs.getString("SEX"));// NO001
                                                                                    // 男:空白、女:'*'

                // 生徒漢字・規則に従って出力
                String strz = rs.getString("NAME");
                final int namelen = getMS932ByteLength(strz);
                int z = strz.indexOf("　"); // 空白文字の位置
                String strx = ""; // 姓
                String stry = ""; // 名
                int ketax = 0;
                int ketay = 0;
                if (z >= 0) {
                    strx = strz.substring(0, z); // 姓
                    stry = strz.substring(z + 1); // 名
                    ketax = getMS932ByteLength(strx);
                    ketay = getMS932ByteLength(stry);
                }
                if (namelen > 18) {
                    svf.VrsOutn("NAME" + String.valueOf(len) + "_2", gyo, strz);
                } else if (z < 0 || (ketax > 8 || ketay > 8)) {
                    svf.VrsOutn("NAME" + String.valueOf(len), gyo, strz); // 空白がない
                } else {
                    strx = strz.substring(0, z);
                    stry = strz.substring(z + 1);
                    if (0 <= stry.indexOf("　")) {
                        svf.VrsOutn("NAME" + String.valueOf(len), gyo, strz); // 空白が２つ以上
                    } else {
                        int x = strx.length(); // 姓の文字数
                        int y = stry.length(); // 名の文字数
                        if (x == 1) {
                            svf.VrsOutn("LNAME" + String.valueOf(len) + "_2", gyo, strx); // 姓１文字
                        } else {
                            svf.VrsOutn("LNAME" + String.valueOf(len) + "_1", gyo, strx); // 姓２文字以上
                        }
                        if (y == 1) {
                            svf.VrsOutn("FNAME" + String.valueOf(len) + "_2", gyo, stry); // 名１文字
                        } else {
                            svf.VrsOutn("FNAME" + String.valueOf(len) + "_1", gyo, stry); // 名２文字以上
                        }
                    }
                }

                nonedata = true;
                gyo++;
            }
        } catch (Exception ex) {
            log.error("Set_Detail_2 read error!", ex);
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return nonedata;

    }// Set_Detail_2()の括り

    /** PrepareStatement作成* */
    private String Pre_Stat1(final String[] param) {
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT ");
        stb.append("value(w3.attendno,'') attendno,");
        stb.append("CASE WHEN w4.SEX = '2' THEN '*' ELSE '' END AS SEX, ");// NO001
                                                                            // 男:空白、女:'*'
        stb.append("value(w4.name,'') name,");
        stb.append("value(w4.name_kana,'') name_kana,");
        stb.append("value(w1.hr_nameabbv,'') hr_nameabbv,");
        stb.append("value(w2.staffname,'') staffname ");
        stb.append("FROM ");
        stb.append("schreg_base_mst w4,");
        stb.append("schreg_regd_dat w3,");
        stb.append("schreg_regd_hdat w1 ");
        stb.append("left join staff_mst w2 on w1.tr_cd1=w2.staffcd ");
        stb.append("WHERE ");
        stb.append("w1.year='" + param[0] + "' AND ");
        stb.append("w1.semester='" + param[1] + "' AND ");
        stb.append("w1.grade || w1.hr_class=? AND ");
        stb.append("w1.year=w3.year AND ");
        stb.append("w1.semester=w3.semester AND ");
        stb.append("w1.grade=w3.grade AND ");
        stb.append("w1.hr_class=w3.hr_class AND ");
        stb.append("w3.schregno=w4.schregno ");
        stb.append("order by w3.attendno");
        return stb.toString();

    }// Pre_Stat1()の括り

}// クラスの括り
