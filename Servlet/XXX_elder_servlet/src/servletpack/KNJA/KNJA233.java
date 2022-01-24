package servletpack.KNJA;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
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
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.StaffInfo;

/**
 *
 * 学校教育システム 賢者 [学籍管理]
 *
 *                    ＜ＫＮＪＡ２３３＞  講座別名列
 *
 * 2005/07/04 m-yama 作成日
 * 2005/07/15 o-naka ログ(log)の記述を修正
 * 2006/05/25 o-naka NO001 氏名の前に女性は'*'を表示する(男:空白、女:'*')
 */

public class KNJA233 {

    private static final Log log = LogFactory.getLog(KNJA233.class);

    private boolean _hasdata;
    int len = 0; //列数カウント用

    private final String OUTPUT_MUSASHI = "musashi";
    private final String OUTPUT1 = "1";
    private final String OUTPUT2 = "2";
    private final String OUTPUT3 = "3";
    private final String OUTPUT4 = "4";

    int page = 1;  //useFormNameA233A:10 関西学院用 ページ数
    private final String AOYAMA_GAKUIN = "KNJA233A_7"; //青山学院プロパティ値
    private final String KANSEI_GAKUIN = "KNJA233A_10"; //関西学院プロパティ値

    /**
     * HTTP Get リクエストの処理
     */
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        log.fatal("$Revision: 72769 $ $Date: 2020-03-06 12:23:57 +0900 (金, 06 3 2020) $"); // CVSキーワードの取り扱いに注意

        Vrw32alp svf = new Vrw32alp(); //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null; //Databaseクラスを継承したクラス

        // print設定
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();

        // svf設定
        svf.VrInit(); //クラスの初期化
        svf.VrSetSpoolFileStream(outstrm); //PDFファイル名の設定

        // ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch (Exception ex) {
            log.error("DB2 open error!", ex);
        }

        Param param = null;
        //  ＳＶＦ作成処理
        try {
            KNJServletUtils.debugParam(request, log);
            param = new Param(request, db2);

            _hasdata = false;
            printMain(svf, db2, param);

        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            // 該当データ無し
            if (!_hasdata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }

            // 終了処理
            svf.VrQuit();
            if (null != param) {
                for (final Iterator it = param._psMap.values().iterator(); it.hasNext();) {
                    final PreparedStatement ps = (PreparedStatement) it.next();
                    DbUtils.closeQuietly(ps);
                }
            }
            db2.commit();
            db2.close(); //DBを閉じる
            outstrm.close(); //ストリームを閉じる
        }


    }//doGetの括り

    private void printMain(Vrw32alp svf, DB2UDB db2, Param param) {
        String formName = null;
        if (StringUtils.isNotBlank(param._useFormNameA233A)) {
            if(param._useFormNameA233A.equals(AOYAMA_GAKUIN) ) {
                if("1".equals(param._print_div) && "3".equals(param._pattern)) {
                    formName = "KNJA233_7.frm";
                } else {
                    formName = getFormName(param);
                }
            } else if(param._useFormNameA233A.equals(KANSEI_GAKUIN) ) {
                if("1".equals(param._print_div) && "3".equals(param._pattern)) {
                    formName = "KNJA233_10_1.frm";
                } else if ("1".equals(param._print_div) && "4".equals(param._pattern)) {
                    formName = "KNJA233_10_2.frm";
                } else {
                    formName = getFormName(param);
                }
            } else {
                formName = param._useFormNameA233A + ".frm";
            }
        } else {
            formName = getFormName(param);
        }
        svf.VrSetForm(formName, 1);
        log.debug("form = " + formName);

        //SVF出力
        final StringTokenizer stz1 = new StringTokenizer(param._attendclasscd, ",", false); //講座コード
        final StringTokenizer stz2 = new StringTokenizer(param._nameShow, ",", false); //職員コード
        final StringTokenizer stz3 = new StringTokenizer(param._chargeDiv, ",", false); //担任区分
        final StringTokenizer stz4 = new StringTokenizer(param._appdate, ",", false); //適用開始日付
        while (stz1.hasMoreTokens()){
            param._attendclasscd = stz1.nextToken(); //講座コード
            param._nameShow = stz2.nextToken(); //職員コード
            param._chargeDiv = stz3.nextToken(); //担任区分
            param._appdate = stz4.nextToken(); //適用開始日付
            setChairname(db2, param); //講座出力のメソッド
            setStaffname(db2, param); //担任出力のメソッド
            for (int ib = 0; ib < Integer.parseInt(param._kensuu); ib++) {
                if (Set_Detail_1(db2, svf, param)) {
                    _hasdata = true; //生徒出力のメソッド
                }
            }
        }
        if (_hasdata) {
            svf.VrEndPage(); //SVFフィールド出力
        }
    }

    private String getFormattedGrade(int grade) {
        final String str = String.valueOf(grade);
        return (grade / 10 >= 1) ? str : " " + str;
    }

    private String getFormattedHrClass(String hrClass) {
        if (!NumberUtils.isNumber(hrClass)) return hrClass;
        return getFormattedHrClass(Integer.parseInt(hrClass));
    }

    private String getFormattedHrClass(int hrClass) {
        final String str = String.valueOf(hrClass);
        return (hrClass / 100 >= 1) ? str : (hrClass / 10 >= 1) ? " " + str : "  " + str;
    }

    private String getFormattedAttendNo(int attendNo) {
        final String attendNoStr = String.valueOf(attendNo);
        return (attendNo / 100 >= 1) ? attendNoStr : (attendNo / 10 >= 1) ? " " + attendNoStr : "  " + attendNoStr;
    }

    private String getFormName(Param param) {
        String formName = null;
        if (param._output.equals(OUTPUT_MUSASHI)) {
            formName = "KNJA233_MUSA.frm";
        } else if (param._output.equals(OUTPUT1)) {
            formName = param._isTokyoto ?  "KNJA233_1_2" : "KNJA233_1";
            formName += param._isPatternB ?  "B" : "";
            formName += ".frm";
        } else if (param._output.equals(OUTPUT2)) {
            formName = param._isTokyoto ?  "KNJA233_2_2" : "KNJA233_2";
            formName += param._isPatternB ?  "B" : "";
            formName += ".frm";
        } else if (param._output.equals(OUTPUT4)) {
            if ("2".equals(param._output4AB)) {
                formName = "KNJA233_5.frm";
            } else {
                formName = "KNJA233_4.frm";
            }
        } else { // param._11.equals(OUTPUT3)
            final String width = param._width != null ? param._width : "25";
            final String height = param._height != null ? param._height : "4";
            if (param._isHirokoudatTuusin) {
                formName = "KNJA233_6.frm";
            }else if (param._isTosajoshi) {
                formName = "KNJA233_8.frm";
            }else if (param._isOsakatoin) {
                formName = "KNJA233_9.frm";
            } else {
                formName = "KNJA233_3_" + height + width+ ".frm";
            }
        }
        return formName;
    }

    /**SVF-FORM**/
    private boolean Set_Detail_1(final DB2UDB db2, final Vrw32alp svf, final Param param)
    {
        final int maxLen = param._useFormNameA233A.equals(AOYAMA_GAKUIN) && "1".equals(param._print_div) && "3".equals(param._pattern) ? 2
                : param._useFormNameA233A.equals(KANSEI_GAKUIN) && "1".equals(param._print_div) && "3".equals(param._pattern) ? 9
                        : param._useFormNameA233A.equals(KANSEI_GAKUIN) && "1".equals(param._print_div) && "4".equals(param._pattern) ? 7
                                : StringUtils.isNotBlank(param._useFormNameA233A) && "KNJA233A_6".equals(param._useFormNameA233A) ? 1
                                        : (param._output.equals(OUTPUT_MUSASHI)) ? 4
                                                : (param._output.equals(OUTPUT3) && param._isHirokoudatTuusin) ? 2
                                                        : (param._output.equals(OUTPUT3)) ? 3
                                                                        : (param._output.equals(OUTPUT4)) ? 1 : 2;
        final int maxGyo = param._useFormNameA233A.equals(AOYAMA_GAKUIN) && "1".equals(param._print_div) && "3".equals(param._pattern) ? 50
                : param._useFormNameA233A.equals(KANSEI_GAKUIN) && "1".equals(param._print_div) && "3".equals(param._pattern) || "4".equals(param._pattern) ? 50
                        : StringUtils.isNotBlank(param._useFormNameA233A) && "KNJA233A_6".equals(param._useFormNameA233A) ? 50
                                : param._output.equals(OUTPUT4) && "2".equals(param._output4AB) ? 48
                                        : (param._output.equals(OUTPUT4)) ? 45
                                                : (param._isTosajoshi) ? 50
                                                        : (param._isOsakatoin) ? 60 : 50;
        boolean nonedata = false;
        ResultSet rs = null;
        try {
            final String psKey = "ps1";
            if (null == param._psMap.get(psKey)) {
                param._psMap.put(psKey, db2.prepareStatement(Pre_Stat1(param)));       //生徒preparestatement
            }
            PreparedStatement ps1 = (PreparedStatement) param._psMap.get(psKey);

            int pp = 0;
            ps1.setString(++pp, param._attendclasscd); //講座コード
            ps1.setDate(++pp, Date.valueOf(param._appdate)); //適用開始日付
            rs = ps1.executeQuery();

            int gyo = 1; //行数カウント用
            //int len = 1; //列数カウント用
            int ban = 1; //連番
            String hrNameChk = ""; //10:関西学院用 クラス毎に列数制御
            len++;

            while (rs.next()) {
                if (gyo > maxGyo) {
                    gyo = 1;
                    len++;
                    if (param._isTosajoshi || param._isOsakatoin) {
                        svf.VrEndPage(); //SVFフィールド出力
                    }
                }
                if (param._useFormNameA233A.equals(KANSEI_GAKUIN) && "1".equals(param._print_div) && "3".equals(param._pattern) ||  "4".equals(param._pattern)) {
                    if(!hrNameChk.equals(rs.getString("HR_NAME"))) {
                        if(!hrNameChk.equals("")) {
                            len++;
                            gyo = 1;
                        }
                        hrNameChk = rs.getString("HR_NAME");
                    }

                }
                if (!param._isTosajoshi && !param._isOsakatoin) {
                    if (len > maxLen) {
                        len = 1;
                        page++;
                        svf.VrEndPage(); //SVFフィールド出力
                    }
                }
                // 講座名・担任名出力
                String slen = String.valueOf(len);
                if (StringUtils.isNotBlank(param._useFormNameA233A) && "KNJA233A_6".equals(param._useFormNameA233A)) {
                    svf.VrsOut("DATE", param._date);
                    svf.VrsOut("CLASS_NAME" + (KNJ_EditEdit.getMS932ByteLength(param._subclassname) <= 20 ? "1" : "2"), param._subclassname);
                    String hrName =  rs.getString("GRADE_NAME1");
                    svf.VrsOut("HR_NAME" + (KNJ_EditEdit.getMS932ByteLength(hrName) <= 20 ? "1" : "2"), hrName);
                    svf.VrsOut("CHAIR_NAME" + (KNJ_EditEdit.getMS932ByteLength(param._chairname) <= 20 ? "1" : "2"), param._chairname);
                } else if (param._isTosajoshi || param._isOsakatoin) {
                    svf.VrsOut("SUBCLASS1", param._chairname);
                    svf.VrsOut("SUBCLASS1_2", param._chairname);
                    svf.VrsOut("SUBCLASS1_3", param._chairname);
                    svf.VrsOut("STAFFNAME1", param._staffname);
                    svf.VrsOut("STAFFNAME1_2", param._staffname);
                    svf.VrsOut("STAFFNAME1_3", param._staffname);
                } else {
                    if (OUTPUT4.equals(param._output) && "2".equals(param._output4AB)) {
                        final int len = getMS932ByteCount(param._chairname);
                        svf.VrsOut("CHAIR_NAME" + (len < 20 ? "1" : len < 30 ? "2" : "3"), param._chairname);
                    } else {
                        svf.VrsOut("SUBCLASS" + slen, param._chairname);
                    }
                    svf.VrsOut("STAFFNAME" + slen , param._staffname);
                }


                // 連番・組略称・出席番号・かな出力
                if (param._output.equals(OUTPUT1) || param._output.equals(OUTPUT3)
                        || param._output.equals(OUTPUT_MUSASHI)
                        || (param._useFormNameA233A.equals(AOYAMA_GAKUIN) && "1".equals(param._print_div) && "3".equals(param._pattern))) {
                    if (param._isHirokoudatTuusin) {
                        svf.VrsOut("CHAIRCD" + slen, param._attendclasscd);
                        svf.VrsOutn("NUMBER" + slen     ,gyo, rs.getString("SCHREGNO"));
                    } else if(param._isTosajoshi || param._isOsakatoin) {
                        svf.VrsOut("CHAIRCD1", param._attendclasscd);
                        svf.VrsOut("CHAIRCD1_2", param._attendclasscd);
                        svf.VrsOut("CHAIRCD1_3", param._attendclasscd);
                    } else {
                        svf.VrsOutn("NUMBER" + slen ,gyo, String.valueOf(ban));
                    }
                } else {
                    svf.VrsOutn("NUMBER" + slen ,gyo, rs.getString("SCHREGNO"));

                    if (OUTPUT4.equals(param._output) && "2".equals(param._output4AB)) {
                        svf.VrsOut("CHAIR_CD", param._attendclasscd);
                    } else {
                        svf.VrsOut("CHAIRCD" + slen, param._attendclasscd);
                    }
                }
                if (param._isTosajoshi || param._isOsakatoin) {
                    final String hrClass = getFormattedHrClass(rs.getString("HR_CLASS"));
                    svf.VrsOutn("CLASS1", gyo, hrClass);
                    svf.VrsOutn("CLASS1_2", gyo, hrClass);
                    svf.VrsOutn("CLASS1_3", gyo, hrClass);
                    svf.VrsOutn("ATTENDNO1", gyo, rs.getString("ATTENDNO"));
                    svf.VrsOutn("ATTENDNO1_2", gyo, rs.getString("ATTENDNO"));
                    svf.VrsOutn("ATTENDNO1_3", gyo, rs.getString("ATTENDNO"));
                } else if (param._output.equals(OUTPUT3) || param._output.equals(OUTPUT_MUSASHI)) {
                    int attendNo = Integer.parseInt(rs.getString("ATTENDNO"));
                    if (param._isPatternB) {
                        int grade = Integer.parseInt(rs.getString("GRADE"));
                        String hrClass = rs.getString("HR_CLASS");
                        svf.VrsOutn("HR_NAME" + slen, gyo, getFormattedGrade(grade) + "-" + getFormattedHrClass(hrClass) + "-" + getFormattedAttendNo(attendNo));
                    } else {
                        svf.VrsOutn("HR_NAME" + slen, gyo, rs.getString("HR_NAMEABBV") + "-" + getFormattedAttendNo(attendNo));
                    }
                } else {
                    if (param._isPatternB) {
                        int grade = Integer.parseInt(rs.getString("GRADE"));
                        String hrClass = rs.getString("HR_CLASS");
                        svf.VrsOutn("GRADE" + slen, gyo, getFormattedGrade(grade));
                        svf.VrsOutn("CLASS" + slen, gyo, getFormattedHrClass(hrClass));
                    } else {
                        svf.VrsOutn("HR_CLASS" + slen, gyo, rs.getString("HR_NAMEABBV"));
                        svf.VrsOutn("CLASS" + slen, gyo, rs.getString("HR_CLASS"));
                    }
                        svf.VrsOutn("ATTENDNO" + slen, gyo, rs.getString("ATTENDNO"));
                }
                if (param._output.equals(OUTPUT4)) {
                    svf.VrsOut("HR_NAME", rs.getString("HR_NAME"));
                }

                if (param._output.equals(OUTPUT_MUSASHI)) {
                    // 年度
                    svf.VrsOut("NENDO" + slen    , param._year);
                }

                final String kanaOrEngName = param._staffInfo.getStrEngOrJp(rs.getString("NAME_KANA"), rs.getString("NAME_ENG"));

                if (param._output.equals(OUTPUT3) && param._isHirokoudatTuusin) {
                    svf.VrsOutn("KANA" + slen, gyo, kanaOrEngName);
                } else if (param._output.equals(OUTPUT3) && (param._isTosajoshi || param._isOsakatoin)) {
                    final String nameKana = kanaOrEngName;
                    final int kanaLen = getMS932ByteCount(nameKana);
                    final String field;
                    if (kanaLen <= 26) {
                        field = "KANA1";
                    } else if (kanaLen <= 30) {
                        field = "KANA2";
                    } else {
                        field = "KANA3";
                    }
                    svf.VrsOutn(field, gyo, nameKana);
                }

                if ("1".equals(param._huriganaOutput)) {
                    final String nameKana = kanaOrEngName;
                    final int kanaLen = getMS932ByteCount(nameKana);
                    if (OUTPUT4.equals(param._output)) {
                        final String field;
                        if (kanaLen <= 26) {
                            field = "KANA1";
                        } else if (kanaLen <= 30) {
                            field = "KANA2";
                        } else {
                            field = "KANA3";
                        }
                        svf.VrsOutn(field, gyo, nameKana);
                    } else {
                        final String field;
                        if (param._isTokyoto && kanaLen > 22) {
                            field = "KANA" + slen + "_2";
                        } else {
                            field = "KANA" + slen;
                        }
                        svf.VrsOutn(field, gyo, nameKana);
                    }
                }
                svf.VrsOutn("MARK" + slen, gyo, rs.getString("SEX")); //NO001 男:空白、女:'*'

                final String name = StringUtils.defaultString(rs.getString("NAME"));
                final int nameLen = getMS932ByteCount(name);
                //  生徒漢字・規則に従って出力
                final int idxSpace = name.indexOf("　");                  //空白文字の位置
                if (StringUtils.isNotBlank(param._useFormNameA233A) && "KNJA233A_6".equals(param._useFormNameA233A)) {
                   svf.VrsOutn("ATTENDNO", gyo, String.valueOf(gyo));
                   svf.VrsOutn("MARK", gyo, rs.getString("SEX")); //NO001 男:空白、女:'*'
                   svf.VrsOutn("NAME" + (KNJ_EditEdit.getMS932ByteLength(name) <= 14 ? "1" : "2"), gyo, name);

                   String hrClass = StringUtils.defaultString(rs.getString("HR_CLASS_NAME1"));
                   String attendno = StringUtils.defaultString(rs.getString("ATTENDNO"));
                   svf.VrsOutn("CLASSNO", gyo, hrClass + attendno.substring(attendno.length() - 2));
                } else if (param._useFormNameA233A.equals(AOYAMA_GAKUIN) && "1".equals(param._print_div) && "3".equals(param._pattern)) {
                    //7:青山学院 C
                    final String kubun = rs.getString("KUBUN");
                    final String nameKubun = kubun != null ? name + "(" + kubun + ")" : name;
                    final int keta = KNJ_EditEdit.getMS932ByteLength(nameKubun);
                    final String field = keta <= 18 ? "1" : keta <= 22 ? "2" : keta <= 26 ? "3" : "4";
                    svf.VrsOutn("NAME" + slen + "_" + field, gyo, nameKubun);
                } else if (OUTPUT4.equals(param._output)) {
                    String sei = "";
                    String mei = "";
                    String field1 = null;
                    String field2 = null;
                    if (idxSpace >= 0) {
                        sei = name.substring(0, idxSpace); // 姓
                        mei = name.substring(idxSpace + 1); // 名
                        if (sei.length() == 1) {
                            field1 = "LNAME" + slen + "_2"; // 姓１文字
                        } else {
                            field1 = "LNAME" + slen + "_1"; // 姓２文字以上
                        }
                        if (mei.length() == 1) {
                            field2 = "FNAME" + slen + "_2"; // 名１文字
                        } else {
                            field2 = "FNAME" + slen + "_1"; // 名２文字以上
                        }
                    }
                    if (nameLen <= 18 && sei.length() <= 4 && mei.length() <= 4 && null != field1 && null != field2) {
                        svf.VrsOutn(field1, gyo, sei);
                        svf.VrsOutn(field2, gyo, mei);
                    } else if (nameLen <= 20) {
                        svf.VrsOutn("NAME4", gyo, name);                   //空白がない
                    } else if (nameLen <= 30) {
                        svf.VrsOutn("NAME5", gyo, name);                   //空白がない
                    } else {
                        svf.VrsOutn("NAME6", gyo, name);                   //空白がない
                    }
                } else {

                    // 学籍番号表記
                    String slenNo = "";
                    if (OUTPUT3.equals(param._output) && "1".equals(param._printSchregno)) {
                        svf.VrsOutn("SCHREGNO" + slen, gyo, rs.getString("SCHREGNO"));
                        slenNo = "_2"; // 学籍番号表示用の氏名フィールド
                    }

                    if (param._isTokyoto && nameLen > 18) {
                        svf.VrsOutn("NAME" + slen + "_2", gyo, name);
                    } else if (param._isTosajoshi || param._isOsakatoin) {
                        final String sei = name.substring(0, idxSpace);
                        final String mei = name.substring(idxSpace + 1);
                        if (0 <= mei.indexOf("　")) {
                            final String nameField = nameLen > 30 ? "6" : nameLen > 20 ? "5" : nameLen > 18 ? "4" : nameLen > 16 ? "3" : nameLen > 14 ? "2" : "1";
                            svf.VrsOutn("NAME" + nameField, gyo, name); // 空白が２つ以上
                            svf.VrsOutn("NAME" + nameField + "_2", gyo, name);
                            svf.VrsOutn("NAME" + nameField + "_3", gyo, name);
                        } else {
                            final String field1;
                            final String field2;
                            if (sei.length() == 1) {
                                field1 = "LNAME1_2";       //姓１文字
                            } else {
                                field1 = "LNAME1_1";       //姓２文字以上
                            }
                            if (mei.length() == 1) {
                                field2 = "FNAME1_2";       //名１文字
                            } else {
                                field2 = "FNAME1_1";       //名２文字以上
                            }
                            svf.VrsOutn(field1, gyo, sei);
                            svf.VrsOutn(field1 + "_2", gyo, sei);
                            svf.VrsOutn(field1 + "_3", gyo, sei);
                            svf.VrsOutn(field2, gyo, mei);
                            svf.VrsOutn(field2 + "_2", gyo, mei);
                            svf.VrsOutn(field2 + "_3", gyo, mei);
                        }
                    } else if (idxSpace < 0) {
                        svf.VrsOutn("NAME" + slen + slenNo, gyo, name); //空白がない
                    } else {
                        final String sei = name.substring(0, idxSpace);
                        final String mei = name.substring(idxSpace + 1);
                        if (0 <= mei.indexOf("　")) {
                            svf.VrsOutn("NAME" + slen + slenNo, gyo, name); // 空白が２つ以上
                        } else {
                            final String field1;
                            final String field2;
                            if (sei.length() == 1) {
                                field1 = "LNAME" + slen + "_2";       //姓１文字
                            } else {
                                field1 = "LNAME" + slen + "_1";       //姓２文字以上
                            }
                            if (mei.length() == 1) {
                                field2 = "FNAME" + slen + "_2";       //名１文字
                            } else {
                                field2 = "FNAME" + slen + "_1";       //名２文字以上
                            }
                            svf.VrsOutn(field1 + slenNo, gyo, sei);
                            svf.VrsOutn(field2 + slenNo, gyo, mei);
                        }
                    }
                    if (param._isHirokoudatTuusin) {
                        final String teachername = StringUtils.defaultString(rs.getString("TEACHER_NAME"));
                        svf.VrsOutn("TEACHER_NAME" + slen, gyo, getmyouji(teachername));
                    }

                    //10:関西学園 C1、C2
                    if (param._useFormNameA233A.equals(KANSEI_GAKUIN) && "1".equals(param._print_div) && "3".equals(param._pattern) || "4".equals(param._pattern)) {
                        int keta;
                        String field;
                        if(gyo == 1) {
                            if(len == 1) svf.VrsOut("PAGE", String.valueOf(page));
                            final String hr_name = rs.getString("HR_NAME");
                            keta = KNJ_EditEdit.getMS932ByteLength(hr_name);
                            if("3".equals(param._pattern)) {
                                field = keta <= 26 ? "1" : keta <= 38 ? "2" : "3";
                            } else {
                                field = keta <= 30 ? "1" : keta <= 40 ? "2" : "3";
                            }
                            svf.VrsOut("HR_NAME" + slen + "_" + field, hr_name);
                            keta = KNJ_EditEdit.getMS932ByteLength(param._chairname);
                            if("3".equals(param._pattern)) {
                                field = keta <= 26 ? "1" : keta <= 38 ? "2" : "3";
                            } else {
                                field = keta <= 30 ? "1" : keta <= 40 ? "2" : "3";
                            }
                            svf.VrsOut("CHR_NAME" + slen + "_" + field, param._chairname);
                        }
                        svf.VrsOutn("ATTENDNO" + slen, gyo, rs.getString("ATTENDNO"));
                        final String nameMark = " " + name;
                        keta = KNJ_EditEdit.getMS932ByteLength(nameMark);
                        if("3".equals(param._pattern)) {
                            field = keta <= 20 ? "1" : keta <= 30 ? "2" : "3";
                        } else {
                            field = keta <= 26 ? "1" : keta <= 32 ? "2" : "3";
                        }

                        svf.VrsOutn("NAME" + slen + "_" + field, gyo, nameMark);
                    }
                }


                nonedata = true;
                gyo++; //行数カウント用
                ban++; //連番
            }

            if(param._isTosajoshi || param._isOsakatoin) {
                svf.VrEndPage(); //SVFフィールド出力
            }

        } catch (Exception ex) {
            log.error("Set_Detail_1 read error!", ex);
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return nonedata;

    }//Set_Detail_1()の括り

    private String getmyouji(final String cutstaffname) {
        String retstr;
        final int idxwk1 = cutstaffname.indexOf("　");// < 0 ? 0 : cutstaffname.indexOf("　");
        final int idxwk2 = cutstaffname.indexOf(" ");// < 0 ? 0 : cutstaffname.indexOf(" ");
        final int idxSpace = (idxwk1 >= 0 && idxwk2 >= 0 && idxwk1 > idxwk2) ? idxwk2 : idxwk1 < 0 ? idxwk2 : idxwk1;                  //空白文字の位置
        if (idxSpace >= 0) {
            final String sei = cutstaffname.substring(0, idxSpace);
            final String mei = cutstaffname.substring(idxSpace + 1);
            final int chkmeiidx = mei.indexOf("　") > mei.indexOf(" ") ? mei.indexOf(" ") : mei.indexOf("　");                  //空白文字の位置
            if (0 <= chkmeiidx) {
                retstr = cutstaffname;
            } else {
                retstr = sei;
            }
        } else {
            retstr = cutstaffname;
        }
        return retstr;
       }

    private static int getMS932ByteCount(final String str) {
        return KNJ_EditEdit.getMS932ByteLength(str);
    }

    /**SVF-FORM**/
    private void setChairname(final DB2UDB db2, final Param param) {
        ResultSet rs = null;
        try {
            final String psKey = "ps2";
            if (null == param._psMap.get(psKey)) {
                final StringBuffer stb = new StringBuffer();
                if (StringUtils.isNotBlank(param._useFormNameA233A) && "KNJA233A_6".equals(param._useFormNameA233A)) {
                    stb.append("SELECT value(T1.CHAIRNAME,'') AS CHAIRNAME, value(L1.SUBCLASSNAME,'') AS SUBCLASSNAME ");
                    stb.append(" FROM CHAIR_DAT T1 ");
                    stb.append(" LEFT JOIN SUBCLASS_MST L1 ");
                    stb.append(" ON L1.SUBCLASSCD = T1.SUBCLASSCD ");
                    //教育課程対応
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append(" AND L1.CLASSCD      = T1.CLASSCD ");
                        stb.append(" AND L1.SCHOOL_KIND  = T1.SCHOOL_KIND ");
                        stb.append(" AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ");
                    }
                    stb.append("WHERE T1.YEAR = '" + param._year + "' AND T1.SEMESTER = '" + param._semester + "' AND T1.CHAIRCD = ? ");
                } else {
                    stb.append("SELECT value(CHAIRNAME,'') AS CHAIRNAME FROM CHAIR_DAT ");
                    stb.append("WHERE YEAR = '" + param._year + "' AND SEMESTER = '" + param._semester + "' AND CHAIRCD = ? ");
                }

                param._psMap.put(psKey, db2.prepareStatement(stb.toString()));       //講座preparestatement
            }
            final PreparedStatement ps2 = (PreparedStatement) param._psMap.get(psKey);

            int pp = 0;
            ps2.setString(++pp, param._attendclasscd); //講座コード
            rs = ps2.executeQuery();

            if (rs.next()) {
                param._chairname = rs.getString("CHAIRNAME"); //講座名称
                if (StringUtils.isNotBlank(param._useFormNameA233A) && "KNJA233A_6".equals(param._useFormNameA233A)) {
                    param._subclassname = rs.getString("SUBCLASSNAME"); //科目名称
                }
            } else {
                param._chairname = "";
            }
        } catch (Exception ex) {
            log.error("Set_Detail_2 read error!", ex);
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }

    }//Set_Detail_2()の括り

    /**SVF-FORM**/
    private void setStaffname(final DB2UDB db2, final Param param) {
        ResultSet rs = null;
        try {
            final String psKey = "ps3";
            if (null == param._psMap.get(psKey)) {
                final StringBuffer stb = new StringBuffer();
                stb.append("SELECT value(STAFFNAME,'') AS STAFFNAME FROM STAFF_MST WHERE STAFFCD = ? ");

                param._psMap.put(psKey, db2.prepareStatement(stb.toString()));       //担任preparestatement
            }
            final PreparedStatement ps3 = (PreparedStatement) param._psMap.get(psKey);

            int pp = 0;
            ps3.setString(++pp, param._nameShow); //職員コード
            rs = ps3.executeQuery();

            if (rs.next()) {
                param._staffname = rs.getString("STAFFNAME"); //職員名称
            } else {
                param._staffname = "";
            }
        } catch (Exception ex) {
            log.error("Set_Detail_3 read error!", ex);
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }

    }//Set_Detail_3()の括り



    /**PrepareStatement作成**/
    private String Pre_Stat1(final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT ");
        stb.append("    T1.SCHREGNO, ");
        // 文京の場合、性別の＊を表記しない。
        if (param._isBunkyo) {
            stb.append(" '' AS SEX, ");  // 男:空白、女:''
        } else {
            stb.append("    CASE WHEN T1.SEX = '2' THEN '*' ELSE '' END AS SEX, ");//NO001 男:空白、女:'*'
        }
        stb.append("    value(T1.NAME,'') NAME, ");
        stb.append("    value(T1.NAME_KANA,'') NAME_KANA, ");
        stb.append("    value(T1.NAME_ENG,'') NAME_ENG, ");
        stb.append("    value(T6.HR_NAMEABBV,'') HR_NAMEABBV, ");
        stb.append("    value(T6.HR_NAME,'') HR_NAME, ");
        stb.append("    value(T2.GRADE,'') GRADE, ");
        stb.append("    value(T2.HR_CLASS,'') HR_CLASS, ");
        stb.append("    value(T2.ATTENDNO,'') ATTENDNO ");
        if (param._isHirokoudatTuusin) {
            stb.append("    ,value(T8.STAFFNAME, '') TEACHER_NAME ");
        }
        if (StringUtils.isNotBlank(param._useFormNameA233A) && "KNJA233A_6".equals(param._useFormNameA233A)) {
            stb.append("    ,value(T9.GRADE_NAME1, '') GRADE_NAME1 ");
            stb.append("    ,value(T6.HR_CLASS_NAME1, '') HR_CLASS_NAME1 ");
        }
        if (StringUtils.isNotBlank(param._useFormNameA233A) && "KNJA233A_7".equals(param._useFormNameA233A)) {
            //GRD_DIVとTRANSFERCDどちらも設定されていればGRD_DIVを出力
            stb.append("    ,CASE WHEN N1.NAME1 IS NOT NULL AND N2.NAME1 IS NOT NULL THEN N1.NAME1 WHEN N1.NAME1 IS NOT NULL THEN N1.NAME1 WHEN N2.NAME1 IS NOT NULL THEN N2.NAME1 END KUBUN ");
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
        if (param._isHirokoudatTuusin) {
            stb.append("    LEFT JOIN STAFF_MST T8 ON T8.STAFFCD = T6.TR_CD1 ");
        }
        if (StringUtils.isNotBlank(param._useFormNameA233A) && "KNJA233A_6".equals(param._useFormNameA233A)) {
            stb.append("    LEFT JOIN SCHREG_REGD_GDAT T9 ON T9.YEAR = T6.YEAR AND T9.GRADE = T6.GRADE ");
        }
        if (StringUtils.isNotBlank(param._useFormNameA233A) && "KNJA233A_7".equals(param._useFormNameA233A)) {
            final String date = param._date.replace("/", "-");
            stb.append("    LEFT JOIN SCHREG_TRANSFER_DAT TRANS ON TRANS.SCHREGNO = T1.SCHREGNO AND '" + date + "' BETWEEN TRANS.TRANSFER_SDATE AND TRANS.TRANSFER_EDATE ");
            stb.append("    LEFT JOIN V_NAME_MST N2 ON N2.YEAR = T7.YEAR AND N2.NAMECD1 = 'A004' AND N2.NAMECD2 = TRANS.TRANSFERCD ");
            stb.append("    LEFT JOIN V_NAME_MST N1 ON N1.YEAR = T7.YEAR AND N1.NAMECD1 = 'A003' AND N1.NAMECD2 = T1.GRD_DIV ");
        }
        stb.append("WHERE ");
        stb.append("    T7.YEAR = '" + param._year + "' AND ");
        stb.append("    T7.SEMESTER = '" + param._semester + "' AND ");
        stb.append("    T7.CHAIRCD = ? AND ");
        stb.append("    T7.APPDATE = ? ");
        stb.append("ORDER BY ");
        if (param._output.equals(OUTPUT_MUSASHI)) {
            stb.append("    T2.GRADE, T2.HR_CLASS, T2.ATTENDNO ");
        } else if (param._output.equals(OUTPUT2)) {
            stb.append("    T1.SCHREGNO ");
        } else {
            if (param._isHirokoudatTuusin) {
                stb.append("    T1.SCHREGNO ");
            } else {
                stb.append("    T2.GRADE, T2.HR_CLASS, T2.ATTENDNO ");
            }
        }
        return stb.toString();

    }//Pre_Stat1()の括り

    private static class Param {
        final String _year;
        final String _semester;
        String _attendclasscd;
        String _nameShow;
        String _chargeDiv;
        final String _semesSdate;
        final String _semesEdate;
        String _chairname;
        String _staffname;
        String _appdate;
        final String _kensuu;
        final String _print_div;
        final String _pattern;
        final boolean _isPatternB;
        final String _output;
        final String _output4AB;
        final String _width;
        final String _height;
        final String _printSchregno;
        final String _huriganaOutput;
        final boolean _isTokyoto;
        final boolean _isBunkyo;
        final boolean _isHirokoudatTuusin;
        final boolean _isTosajoshi;
        final boolean _isOsakatoin;
        final String _useCurriculumcd;
        final String _useFormNameA233A;
        final String _date;
        String _subclassname;
        final String _staffCd;
        final StaffInfo _staffInfo;
        final Map _psMap = new HashMap();
        public Param(final HttpServletRequest request, final DB2UDB db2) {

            _year = request.getParameter("YEAR");                                //年度
            _semester = request.getParameter("SEMESTER");                            //学期
            _attendclasscd = request.getParameter("ATTENDCLASSCD");                       //講座コード
            _nameShow = request.getParameter("NAME_SHOW");                           //科目担任名（職員コード）
            _chargeDiv = request.getParameter("CHARGEDIV");                           //担任区分1:正担任,0:副担任
            _appdate = request.getParameter("APPDATE");                             //適用開始日付
            _kensuu = request.getParameter("KENSUU");                             //出力件数

            _print_div = request.getParameter("PRINT_DIV");                         // 帳票種別　1:講座名簿（OUTPUT=1,2,3）、2:教務手帳（OUTPUT=4）
            _pattern = request.getParameter("PATTERN");                             // 講座名簿（OUTPUT=1,2,3）の出力パターン 1:Aパターン 2:Bパターン(年組を年と組に分ける)
            _isPatternB = "1".equals(_print_div) && "2".equals(_pattern) ?  true : false;

            _output = "2".equals(_print_div) ? "4" : request.getParameter("OUTPUT");                             //出力順
            _output4AB = request.getParameter("OUTPUT4AB");                             // 教務手帳（OUTPUT=4）の出力パターン 1:Aパターン 2:Bパターン
            _width = request.getParameter("WIDTH");                              //列の長さ
            _height = request.getParameter("HEIGHT");                             //行の高さ
            _printSchregno = request.getParameter("PRINT_SCHREGNO");
            _huriganaOutput = request.getParameter("HURIGANA_OUTPUT");                    //ふりがな出力

            //  学期開始日・学期終了日の取得
            String ssdate = null;
            String sedate = null;
            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;
            try {
                returnval = getinfo.Semester(db2, _year, _semester);
                ssdate = returnval.val2;                      //学期開始日
                sedate = returnval.val3;                      //学期終了日
            } catch (Exception e) {
                log.error("Semester date get error!", e);
            }
            getinfo = null;
            returnval = null;
            _semesSdate = ssdate;
            _semesEdate = sedate;
            final String z010Name1 = getNameMstZ010(db2);
            _isTokyoto = "tokyoto".equals(z010Name1);
            _isBunkyo = "bunkyo".equals(z010Name1);
            _isHirokoudatTuusin = "1".equals(request.getParameter("HIROKOUDAI_TUUSIN"));
            _isTosajoshi = "1".equals(request.getParameter("TOSAJOSHI"));
            _isOsakatoin = "1".equals(request.getParameter("OSAKATOIN"));

            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useFormNameA233A = request.getParameter("useFormNameA233A");
            _date = request.getParameter("DATE");

            //生徒氏名（英語・日本語）切替処理追加
            _staffCd = request.getParameter("PRINT_LOG_STAFFCD");
            _staffInfo = new StaffInfo(db2, _staffCd);
        };

        /**
         * 中高一貫か?
         * @param db2 DB2UDB
         * @return 中高一貫ならtrue
         */
        private String getNameMstZ010(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String name1 = null;
            try {
                ps = db2.prepareStatement("SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z010' AND NAMECD2 = '00'");
                rs = ps.executeQuery();
                if (rs.next()) {
                    name1 = rs.getString("NAME1");
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return name1;
        }
    }

}//クラスの括り
