/*
 * 作成日: 2020/12/23
 * 作成者: ishimine
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJL630I {

    private static final Log log = LogFactory.getLog(KNJL630I.class);

    private boolean _hasData;
    private static final String HUTSUU = "普通科";
    private static final String KOUGYOU = "工業科";
    private Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf);
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        if ("1".equals(_param._output)) {
            printPassList(db2, svf);
        } else if ("2".equals(_param._output)) {
            printFailList(db2, svf);
        } else if ("3".equals(_param._output)) {
            printPassNotice(db2, svf);
        } else if ("5".equals(_param._output)) {
            printExamStatus(db2, svf);
        } else if ("6".equals(_param._output)) {
            printNgList(db2, svf);
        } else if ("7".equals(_param._output)) {
            printResult(db2, svf);
        } else if ("8".equals(_param._output)) {
            printResult2(db2, svf);
        } else if ("9".equals(_param._output)) {
            printFurikomi(db2, svf);
        }

        if (_hasData) {
            svf.VrEndPage();
        }
    }

    /**
     * 1:合格者一覧表を出力する。
     *
     * @param db2
     * @param svf
     */
    private void printPassList(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL630I_1.frm", 1);

        final int maxLine = 35;
        int lineCnt = 1; // 書き込み行数
        String shikenGakka = ""; //改行用 入試区分＋学科
        final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamyear)) + "年度 ";

        List<PrintData1> printData1List = getPassList(db2);

        for(PrintData1 printData1 : printData1List) {
            // 改ページの制御
            if (lineCnt > maxLine || !shikenGakka.equals(printData1._testdiv_Name + printData1._gakka)) {
                if(!"".equals(shikenGakka)) {
                    svf.VrEndPage();
                }
                lineCnt = 1;
                shikenGakka = printData1._testdiv_Name + printData1._gakka;
                svf.VrsOut("TITLE", nendo + " " + printData1._testdiv_Name + " 合格者リスト");
            }

            //連番
            svf.VrsOutn("NO", lineCnt, printData1._no);
            //入試区分
            svf.VrsOutn("EXAM_DIV", lineCnt, printData1._testdiv_Name);
            //学科
            svf.VrsOutn("DEPARTMENT_NAME", lineCnt, printData1._gakka);
            //受験番号
            String receptno = printData1._examno;
            if ("1".equals(printData1._duplicateFlg)) {
                receptno = "*" + receptno;
            }
            svf.VrsOutn("EXAM_NO", lineCnt, receptno);
            //合格コース
            svf.VrsOutn("COURSE_NAME", lineCnt, printData1._passcourse);

            //漢字氏名
            final int nameByte = KNJ_EditEdit.getMS932ByteLength(printData1._name);
            final String nameFieldStr = nameByte <= 20 ? "1" : nameByte <= 30 ? "2" : "3";
            svf.VrsOutn("NAME" + nameFieldStr, lineCnt, printData1._name);
            //氏名カナ
            final int kanaByte = KNJ_EditEdit.getMS932ByteLength(printData1._name_Kana);
            final String kanaFieldStr = kanaByte <= 20 ? "1" : kanaByte <= 30 ? "2" : "3";
            svf.VrsOutn("KANA1_" + kanaFieldStr, lineCnt, printData1._name_Kana);

            //性別
            svf.VrsOutn("SEX", lineCnt, printData1._sex);
            //中学校コード
            svf.VrsOutn("FINSCHOOL_CD", lineCnt, printData1._fs_Cd);

            //中学校名
            final int schoolByte = KNJ_EditEdit.getMS932ByteLength(printData1._finschool_Name_Abbv);
            final String schoolFieldStr = schoolByte <= 20 ? "1" : schoolByte <= 30 ? "2" : "3";
            svf.VrsOutn("FINSCHOOL_NAME" + schoolFieldStr, lineCnt, printData1._finschool_Name_Abbv);

            //特待コード
            svf.VrsOutn("SP_CD", lineCnt, printData1._honorDiv);
            //特待
            svf.VrsOutn("SP_NAME", lineCnt, printData1._spName);
            //特待理由
            svf.VrsOutn("SP_REASON", lineCnt, printData1._spReasonRemark);
            //寮生
            svf.VrsOutn("DORMITORY_NAME", lineCnt, printData1._ryo);

            //保護者氏名
            final int gnameByte = KNJ_EditEdit.getMS932ByteLength(printData1._gname);
            final String gnameFieldStr = gnameByte <= 16 ? "1" : gnameByte <= 20 ? "2" : gnameByte <= 30 ? "3" : "4";
            svf.VrsOutn("GUARD_NAME" + gnameFieldStr, lineCnt, printData1._gname);

            //郵便番号
            svf.VrsOutn("ZIP_NO", lineCnt, printData1._zipcd);

            //住所１
            final int addr1Byte = KNJ_EditEdit.getMS932ByteLength(printData1._address1);
            final String addr1FieldStr = addr1Byte <= 40 ? "1" : addr1Byte <= 50 ? "3" : "4";
            svf.VrsOutn("ADDR1_" + addr1FieldStr, lineCnt, printData1._address1);
            //住所２
            final int addr2Byte = KNJ_EditEdit.getMS932ByteLength(printData1._address2);
            final String addr2FieldStr = addr2Byte <= 40 ? "1" : addr2Byte <= 50 ? "3" : "4";
            svf.VrsOutn("ADDR2_" + addr2FieldStr, lineCnt, printData1._address2);

            //電話番号
            svf.VrsOutn("TEL_NO", lineCnt, printData1._telno);
            lineCnt++;
            _hasData = true;
        }
    }

    /**
     * 2:不合格者一覧表を出力する。
     *
     * @param db2
     * @param svf
     */
    private void printFailList(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL630I_2.frm", 1);

        int lineCnt = 1; // 書き込み行数
        final int maxLine = 40;
        final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamyear)) + "年度 ";
        String shikenGakka = ""; //改行用 入試区分＋学科

        List<PrintData2> printData2List = getFailList(db2);

        for(PrintData2 printData2 : printData2List) {
            // 改ページの制御
            if (lineCnt > maxLine || !shikenGakka.equals(printData2._testdiv_Name + printData2._gakka)) {
                if(!"".equals(shikenGakka)) {
                    svf.VrEndPage();
                }
                lineCnt = 1;
                shikenGakka = printData2._testdiv_Name + printData2._gakka;
                svf.VrsOut("TITLE", nendo + " " + printData2._testdiv_Name + " 不合格者リスト");
            }

            //連番
            svf.VrsOutn("NO", lineCnt, printData2._no);
            //入試区分
            svf.VrsOutn("EXAM_DIV", lineCnt, _param._testDivName);
            //学科
            svf.VrsOutn("DEPARTMENT_NAME", lineCnt, printData2._gakka);
            //受験番号
            String receptno = printData2._examno;
            if ("1".equals(printData2._duplicateFlg)) {
                receptno = "*" + receptno;
            }
            svf.VrsOutn("EXAM_NO", lineCnt, receptno);
            //合格コース
            svf.VrsOutn("HOPE_RANK1", lineCnt, printData2._aspiring1);
            svf.VrsOutn("HOPE_RANK2", lineCnt, printData2._aspiring2);
            svf.VrsOutn("HOPE_RANK3", lineCnt, printData2._aspiring3);
            svf.VrsOutn("HOPE_RANK4", lineCnt, printData2._aspiring4);

            //漢字氏名
            final int nameByte = KNJ_EditEdit.getMS932ByteLength(printData2._name);
            final String nameFieldStr = nameByte <= 20 ? "1" : nameByte <= 30 ? "2" : "3";
            svf.VrsOutn("NAME" + nameFieldStr, lineCnt, printData2._name);
            //氏名カナ
            final int kanaByte = KNJ_EditEdit.getMS932ByteLength(printData2._name_Kana);
            final String kanaFieldStr = kanaByte <= 20 ? "1" : kanaByte <= 30 ? "2" : "3";
            svf.VrsOutn("KANA1_" + kanaFieldStr, lineCnt, printData2._name_Kana);

            //性別
            svf.VrsOutn("SEX", lineCnt, printData2._sex);
            //中学校コード
            svf.VrsOutn("FINSCHOOL_CD", lineCnt, printData2._fs_Cd);

            //中学校名
            final int schoolByte = KNJ_EditEdit.getMS932ByteLength(printData2._finschool_Name_Abbv);
            final String schoolFieldStr = schoolByte <= 20 ? "1" : schoolByte <= 30 ? "2" : "3";
            svf.VrsOutn("FINSCHOOL_NAME" + schoolFieldStr, lineCnt, printData2._finschool_Name_Abbv);

            //特待コード
            svf.VrsOutn("SP_CD", lineCnt, printData2._honorDiv);
            //特待
            svf.VrsOutn("SP_NAME", lineCnt, printData2._general_Mark);
            //特待理由
            svf.VrsOutn("SP_REASON", lineCnt, printData2._general_Name);
            //寮生
            svf.VrsOutn("DORMITORY_NAME", lineCnt, printData2._ryo);

            //保護者氏名
            final int gnameByte = KNJ_EditEdit.getMS932ByteLength(printData2._gname);
            final String gnameFieldStr = gnameByte <= 16 ? "1" : gnameByte <= 20 ? "2" : gnameByte <= 30 ? "3" : "4";
            svf.VrsOutn("GUARD_NAME" + gnameFieldStr, lineCnt, printData2._gname);

            //判定
            svf.VrsOutn("JUDGE", lineCnt, printData2._hantei);
            lineCnt++;
            _hasData = true;
        }
    }

    /**
     * 3:合格通知書を出力する。
     *
     * @param db2
     * @param svf
     */
    private void printPassNotice(final DB2UDB db2, final Vrw32alp svf) {
        List<PrintData3> printData3List = getPassNotice(db2);

        final String date[] = KNJ_EditDate.tate_format4(db2, _param._logindate.replace("/", "-")) ;
        final String wareki = date[0] + date[1] + "年" + date[2] + "月" + date[3] + "日";

        for(PrintData3 printData3 : printData3List) {
            svf.VrSetForm("KNJL630I_3.frm", 1);
            svf.VrsOut("DATE", wareki);
            svf.VrsOut("EXAM_NO", printData3._examno);
            svf.VrsOut("NAME", printData3._name + "様");
            final String rui = "1".equals(printData3._gakka) ? "・類" : "";
            svf.VrsOut("FINSCHOOL_NAME", printData3._finschool_Name);
            svf.VrsOut("TEXT", "あなたは入学試験の結果、下記の学科" + rui + "に合格されました");
            svf.VrsOut("PASS_COURSE_TITLE", "＜合格学科" + rui + "＞");
            final int courseByte = KNJ_EditEdit.getMS932ByteLength(printData3._passCourse);
            final String courseFieldStr = courseByte <= 30 ? "1" : "2";
            svf.VrsOut("PASS_COURSE" + courseFieldStr, printData3._passCourse);
            svf.VrEndPage();
            _hasData = true;
        }
    }

    /**
     * 4:
     *
     * @param db2
     * @param svf
     */
    private void print(final DB2UDB db2, final Vrw32alp svf) {
    }

    /**
     * 5:入試状況を出力する。
     *
     * @param db2
     * @param svf
     */
    private void printExamStatus(final DB2UDB db2, final Vrw32alp svf) {
        final String form = "1".equals(_param._exam) ? "KNJL630I_5_1.frm" : "KNJL630I_5_2.frm";
        final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamyear)) + "年度 ";
        final String date[] = KNJ_EditDate.tate_format4(db2, _param._logindate.replace("/", "-")) ;
        final String wareki = date[0] + date[1] + "年" + date[2] + "月" + date[3] + "日";

        Map<String, PrintData5> printData5Map = getExamStatus(db2);

        if(printData5Map.isEmpty()) return;


        //入試区分毎に繰り返し 1:専願、2:前期、3:後期、99:計
        final String examList[] = {"01","02","03","99"};
        //性別毎に繰り返し 1:男、2:女、3:計
        final String sexList[] = {"1","2","3"};

        //1:学科別入試状況
        if("1".equals(_param._exam)) {
            //学科毎に繰り返し 1:普通科、2:工業科、99:合計
            final String gakkaList[] = {"1","2","99"};
            for(String gakka : gakkaList) {
                svf.VrSetForm(form, 1);
                final String title = "1".equals(gakka) ? HUTSUU : "2".equals(gakka) ? KOUGYOU : "合計";
                svf.VrsOut("TITLE", nendo + " 入試状況(学科別)");
                svf.VrsOut("DATE", wareki);
                svf.VrsOut("COURSE_NAME", title);
                int line = 1;

                for(String exam : examList) {
                    for(String sex : sexList) {
                        final String key = gakka + exam + sex;

                        final PrintData5 printData5 = (PrintData5)printData5Map.get(key);
                        if(printData5 != null) {
                            svf.VrsOutn("HOPE_NUM", line, printData5._shigansya);
                            svf.VrsOutn("EXAM_NUM", line, printData5._jukensya);
                            svf.VrsOutn("PASS_NUM", line, printData5._pass);
                            svf.VrsOutn("FEE_NUM", line, printData5._nyugakukin);
                            svf.VrsOutn("FACILITY_NUM", line, printData5._setsubihi);
                            svf.VrsOutn("ENT_NUM", line, printData5._ent);
                        }
                        line++;
                    }
                }
                svf.VrEndPage();
            }
        } else if("2".equals(_param._exam)) { //2:類別入試状況
            //学科毎に繰り返し 1:普通科、2:工業科
            final String gakkaList[] = {"1","2"};
            for(String gakka : gakkaList) {
                svf.VrSetForm(form, 1);
                final Map ruibetsuMap = getRuibetsu(db2, gakka);
                svf.VrsOut("TITLE", nendo + " 入試状況(類別)");
                svf.VrsOut("DATE", wareki);
                int lineDiv = 1;
                for(Iterator iteRui = ruibetsuMap.keySet().iterator(); iteRui.hasNext();) {
                    final String ruibetsuKey = (String)iteRui.next();
                    final String ruibetsuName = (String)ruibetsuMap.get(ruibetsuKey);
                    int len = ruibetsuName.length();
                    if(len <= 2) {
                        svf.VrsOut("COURSE_NAME" + lineDiv + "_1_2", ruibetsuName);
                    } else if(len <= 4){
                        final String ruibetsu[] = KNJ_EditEdit.get_token(ruibetsuName, 4, 2);
                        svf.VrsOut("COURSE_NAME" + lineDiv + "_2_1", ruibetsu[0]);
                        svf.VrsOut("COURSE_NAME" + lineDiv + "_2_2", ruibetsu[1]);
                    } else {
                        final String ruibetsu[] = KNJ_EditEdit.get_token(ruibetsuName, 4, 3);
                        svf.VrsOut("COURSE_NAME" + lineDiv + "_1_1", ruibetsu[0]);
                        svf.VrsOut("COURSE_NAME" + lineDiv + "_1_2", ruibetsu[1]);
                        svf.VrsOut("COURSE_NAME" + lineDiv + "_1_3", ruibetsu[2]);
                    }

                    int line = 1;
                    for(String exam : examList) {
                        for(String sex : sexList) {
                            final String key = gakka + ruibetsuKey + exam + sex;
                            final PrintData5 printData5 = (PrintData5)printData5Map.get(key);
                            if(printData5 != null) {
                                svf.VrsOutn("HOPE_NUM" + lineDiv, line, printData5._shigansya);
                                svf.VrsOutn("EXAM_NUM" + lineDiv, line, printData5._jukensya);
                                svf.VrsOutn("PASS_NUM" + lineDiv, line, printData5._pass);
                                svf.VrsOutn("FEE_NUM" + lineDiv, line, printData5._nyugakukin);
                                svf.VrsOutn("FACILITY_NUM" + lineDiv, line, printData5._setsubihi);
                                svf.VrsOutn("ENT_NUM" + lineDiv, line, printData5._ent);
                            }
                            line++;
                        }
                    }
                    lineDiv++;
                }
                svf.VrEndPage();
            }
        } else { //3:志望コース別
            final Map courseMap = getCourse(db2);
            int lineDiv = 1;
            for(Iterator iteCourse = courseMap.keySet().iterator(); iteCourse.hasNext();) {

                if(lineDiv == 1) {
                    svf.VrSetForm(form, 1);
                    svf.VrsOut("TITLE", nendo + " 入試状況(コース別)");
                    svf.VrsOut("DATE", wareki);
                }

                final String courseKey = (String)iteCourse.next();
                final String courseName = (String)courseMap.get(courseKey);

                int len = courseName.length();
                if(len <= 2) {
                    svf.VrsOut("COURSE_NAME" + lineDiv + "_1_2", courseName);
                } else if(len <= 4){
                    final String course[] = KNJ_EditEdit.get_token(courseName, 4, 2);
                    svf.VrsOut("COURSE_NAME" + lineDiv + "_2_1", course[0]);
                    svf.VrsOut("COURSE_NAME" + lineDiv + "_2_2", course[1]);
                } else {
                    final String course[] = KNJ_EditEdit.get_token(courseName, 4, 3);
                    svf.VrsOut("COURSE_NAME" + lineDiv + "_1_1", course[0]);
                    svf.VrsOut("COURSE_NAME" + lineDiv + "_1_2", course[1]);
                    svf.VrsOut("COURSE_NAME" + lineDiv + "_1_3", course[2]);
                }

                int line = 1;

                for(String exam : examList) {
                    for(String sex : sexList) {
                        final String key = courseKey + exam + sex;
                        final PrintData5 printData5 = (PrintData5)printData5Map.get(key);
                        if(printData5 != null) {
                            svf.VrsOutn("HOPE_NUM" + lineDiv, line, printData5._shigansya);
                            svf.VrsOutn("EXAM_NUM" + lineDiv, line, printData5._jukensya);
                            svf.VrsOutn("PASS_NUM" + lineDiv, line, printData5._pass);
                            svf.VrsOutn("FEE_NUM" + lineDiv, line, printData5._nyugakukin);
                            svf.VrsOutn("FACILITY_NUM" + lineDiv, line, printData5._setsubihi);
                            svf.VrsOutn("ENT_NUM" + lineDiv, line, printData5._ent);
                        }
                        line++;
                    }
                }
                lineDiv++;
                if(lineDiv > 2) {
                    svf.VrEndPage();
                    lineDiv = 1;
                }
            }
        }
        _hasData = true;
    }

    private Map getRuibetsu(final DB2UDB db2, final String gakka) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final Map rtnMap = new LinkedMap();

        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     GENERAL_CD, ");
        stb.append("     GENERAL_NAME, ");
        stb.append("     REMARK1 ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_GENERAL_MST ");
        stb.append(" WHERE ");
        stb.append("     ENTEXAMYEAR = '" + _param._entexamyear + "' AND ");
        stb.append("     APPLICANTDIV = '2' AND ");
        stb.append("     GENERAL_DIV = '01' AND ");
        stb.append("     TESTDIV = '0' AND ");
        stb.append("     REMARK1 = '" + gakka + "'");
        stb.append(" ORDER BY ");
        stb.append("     GENERAL_CD ");
        stb.append(" LIMIT 2 ");

        try {
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                rtnMap.put(rs.getString("GENERAL_CD"), rs.getString("GENERAL_NAME")) ;
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtnMap;
    }

    private Map getCourse(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final Map rtnMap = new LinkedMap();

        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     GENERAL_CD, ");
        stb.append("     GENERAL_ABBV ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_GENERAL_MST ");
        stb.append(" WHERE ");
        stb.append("     ENTEXAMYEAR = '" + _param._entexamyear + "' AND ");
        stb.append("     APPLICANTDIV = '2' AND ");
        stb.append("     GENERAL_DIV = '02' AND ");
        stb.append("     TESTDIV = '0' ");
        stb.append(" ORDER BY ");
        stb.append("     GENERAL_CD ");

        try {
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                rtnMap.put(rs.getString("GENERAL_CD"), rs.getString("GENERAL_ABBV")) ;
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtnMap;
    }

    /**
     * 6:合格者なし中学校リストを出力する。
     *
     * @param db2
     * @param svf
     */
    private void printNgList(final DB2UDB db2, final Vrw32alp svf) {
        List<PrintData6> printData6List = getNgList(db2);
        final int maxLine = 50;
        final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamyear)) + "年度 ";
        int line = 0;
        if(printData6List.isEmpty()) return;

        for(PrintData6 printData6 : printData6List) {
            if(line > maxLine || line == 0) {
                if(line > maxLine) svf.VrEndPage();
                svf.VrSetForm("KNJL630I_6.frm", 1);
                svf.VrsOut("TITLE",nendo + _param._testDivName + "合格者なし中学校リスト" );
                line = 1;
            }
            svf.VrsOutn("NO", line, String.valueOf(line));
            svf.VrsOutn("FINSCHOOL_CD", line, printData6._fs_Cd);
            svf.VrsOutn("FINSCHOOL_NAME", line, printData6._finschool_Name);
            svf.VrsOutn("EXAM_NUM", line, printData6._jukensya);
            svf.VrsOutn("NOT_EXAM_NUM", line, printData6._kesseki);
            line++;
            _hasData = true;
        }
        svf.VrEndPage();
    }

    /**
     * 7:選考結果のお知らせを出力する。
     *
     * @param db2
     * @param svf
     */
    private void printResult(final DB2UDB db2, final Vrw32alp svf) {
        List<PrintData7> printData7List = getResult(db2);
        if(printData7List.isEmpty()) return;


        final String principalName = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT PRINCIPAL_NAME FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _param._entexamyear + "' AND CERTIF_KINDCD = '106'"));
        final String schoolName = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SCHOOL_NAME FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _param._entexamyear + "' AND CERTIF_KINDCD = '106'"));
        String key = ""; //繰り返し用変数
        for(PrintData7 printData7 : printData7List) {

            if(!key.equals(printData7._fs_Cd + printData7._testdiv)) {
                if(!"".equals(key)) {
                    svf.VrEndPage();
                }
                svf.VrSetForm("KNJL630I_7.frm", 4);
                if(printData7._remark2 != null) {
                    final String date[] = KNJ_EditDate.tate_format4(db2, printData7._remark2.replace("/", "-")) ;
                    final String wareki = date[0] + date[1] + "年" + date[2] + "月" + date[3] + "日";
                    svf.VrsOut("DATE", wareki);
                }
                svf.VrsOut("FINSCHOOL_NAME", printData7._finschool_Name + " 校長 殿" );
                svf.VrsOut("SCHOOL_NAME", schoolName);
                svf.VrsOut("PRINCIPAL_NAME", "校長 " + principalName);
                svf.VrsOut("TITLE", printData7._testdiv_Name + "入学試験選考結果のお知らせ" );
                key = printData7._fs_Cd + printData7._testdiv;
            }

            svf.VrsOut("NO", printData7._no);
            svf.VrsOut("EXAM_NO", "[" + printData7._examno + "]");
            final int nameByte = KNJ_EditEdit.getMS932ByteLength(printData7._name);
            final String nameFieldStr = nameByte <= 20 ? "1" : nameByte <= 30 ? "2" : "3";
            svf.VrsOut("NAME" + nameFieldStr, printData7._name);
            svf.VrsOut("HOPE", printData7._shibou);
            svf.VrsOut("JUDGE", printData7._gouhi);
            svf.VrsOut("PASS_COURSE", printData7._course);
            svf.VrsOut("SCHOLAR＿DIV", printData7._special);
            svf.VrEndRecord();
            _hasData = true;
        }
        svf.VrEndPage();
    }

    /**
     * 8:選考結果のお知らせ送付についてを出力する。
     *
     * @param db2
     * @param svf
     */
    private void printResult2(final DB2UDB db2, final Vrw32alp svf) {
        List<PrintData8> printData8List = getResult2(db2);
        if(printData8List == null) return;

        final String principalName = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT PRINCIPAL_NAME FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _param._entexamyear + "' AND CERTIF_KINDCD = '106'"));
        final String schoolName = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SCHOOL_NAME FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _param._entexamyear + "' AND CERTIF_KINDCD = '106'"));
        final String telNo = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT REMARK1 FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _param._entexamyear + "' AND CERTIF_KINDCD = '106'"));
        for(PrintData8 printData8 : printData8List) {
            svf.VrSetForm("KNJL630I_8.frm", 4);
            if(printData8._remark2 != null) {
                final String date[] = KNJ_EditDate.tate_format4(db2, printData8._remark2.replace("/", "-")) ;
                final String wareki = date[0] + date[1] + "年" + date[2] + "月" + date[3] + "日";
                svf.VrsOut("DATE", wareki);
            }
            svf.VrsOut("FINSCHOOL_NAME", printData8._finschool_Name + " 校長 殿" );
            svf.VrsOut("SCHOOL_NAME", schoolName);
            svf.VrsOut("PRINCIPAL_NAME", "校長 " + principalName);

            final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamyear)) + "年度 ";
            svf.VrsOut("METHOD", nendo + "城東特待生の入学手続きについて");
            svf.VrsOut("SUM1", "1"); //１：固定
            svf.VrsOut("SUM2", printData8._special); //特定性入学手続き
            svf.VrsOut("SUM3", printData8._setsubi); //諸手続き及び学費
            svf.VrsOut("SUM4", printData8._pass); //合格者数
            svf.VrsOut("EXAM_DIV", printData8._testdiv_Name);
            svf.VrsOut("TEL_NO", "本校（" + telNo + "）までご連絡ください。");

            //入学までのスケジュール
            for(Iterator ite = printData8._scheduleMap.keySet().iterator(); ite.hasNext();) {
                final String key = (String)ite.next();
                final String schedule[] = (String[])printData8._scheduleMap.get(key);
                if(schedule[0] != null) {
                    svf.VrsOut("EVENT_DATE", schedule[1]); //開催日
                    svf.VrsOut("TIME1", schedule[2] + "～"); //時間1
                    svf.VrsOut("TIME2", schedule[3] + "頃"); //時間2
                    svf.VrsOut("TARGET", schedule[5]); //対象者
                    //内容
                    final int scheByte1 = KNJ_EditEdit.getMS932ByteLength(schedule[0]);
                    if(scheByte1 <= 36) {
                        svf.VrsOut("INFO1", schedule[0]);
                    } else {
                        String info[] = KNJ_EditEdit.get_token(schedule[0], 36, 2);
                        svf.VrsOut("INFO2_1", info[0]);
                        svf.VrsOut("INFO2_2", info[1]);
                    }
                    svf.VrEndRecord();
                }
            }

            //印字されないため
            if(printData8._scheduleMap.isEmpty()) {
                svf.VrsOut("INFO1", "スケジュールが設定されていません");
                svf.VrEndRecord();
            }
            svf.VrEndPage();
            _hasData = true;
        }
    }

    private void printInfo(final DB2UDB db2, final Vrw32alp svf, final String str) {

    }

    /**
     * 9:振込依頼書を出力する。
     *
     * @param db2
     * @param svf
     */
    private void printFurikomi(final DB2UDB db2, final Vrw32alp svf) {
        List<PrintData9> printData9List = getFurikomi(db2);

        for(PrintData9 printData9 : printData9List) {
            svf.VrSetForm("KNJL630I_9.frm", 1);

            //振込依頼書出力(1行目：入学申込金)
            if(!"0".equals(printData9._nyugaku)) {
                printCommonFurikomi(db2, svf, 1, "入学金(入学申込金)", printData9);
            }

            //振込依頼書出力(2行目：施設設備)
            if(!"0".equals(printData9._setsubi)) {
                printCommonFurikomi(db2, svf, 2, "入学金(施設設備費)", printData9);
            }

            svf.VrEndPage();
            _hasData = true;
        }
    }

    //入試区分マスタに設定された納入締切日・時を整形して返す
    private String getLimitDateTimeStr(final DB2UDB db2, final String limitDate, final String limitTime) {
        if (limitDate == null || limitTime == null) {
            return "";
        }
        final String limit[] = KNJ_EditDate.tate_format4(db2, limitDate.replace("/", "-")) ;
        final String time = limitTime.substring(0,2) + "時";

        return limit[0] + limit[1] + "年" + limit[2] + "月" + limit[3] + "日" + time;
    }

    //一行分の振込書印刷
    private void printCommonFurikomi(final DB2UDB db2, final Vrw32alp svf, final int line, final String title, final PrintData9 printData9) {
        //年度
        final String date[] = KNJ_EditDate.tate_format4(db2, _param._logindate.replace("/", "-")) ;
        svf.VrsOutn("NENDO1", line, date[1]);
        svf.VrsOutn("NENDO2", line, date[1]);
        svf.VrsOutn("NENDO3", line, date[1]);

        //タイトル
        svf.VrsOutn("ITEM1", line, title);
        svf.VrsOutn("ITEM2", line, title);
        svf.VrsOutn("ITEM3", line, title);

        //締切日付
        String limitDateTimeStr = "";
        if (line == 1) {
            //入学申込金納入締切日・時
            limitDateTimeStr = getLimitDateTimeStr(db2, printData9._remark4, printData9._remark5);
        } else {
            //施設設備費納入締切日・時
            limitDateTimeStr = getLimitDateTimeStr(db2, printData9._remark7, printData9._remark8);
        }
        svf.VrsOutn("LIMIT_DATE2", line, limitDateTimeStr);
        svf.VrsOutn("LIMIT_DATE3", line, limitDateTimeStr);

        //学校名
        svf.VrsOutn("SCHOOL_NAME1", line, _param._schoolName);
        svf.VrsOutn("SCHOOL_NAME2", line, _param._schoolName);
        svf.VrsOutn("SCHOOL_NAME3", line, _param._schoolName);

        //3列目注釈
        svf.VrsOutn("NOTICE", line, "※期限後受付厳禁");

        //納入金額 (1行目：入学申込金, 2行目:施設設備費)
        final String money = (line == 1) ? printData9._nyugaku: printData9._setsubi;
        svf.VrsOutn("MONEY1", line, money);
        svf.VrsOutn("MONEY2", line, money);
        svf.VrsOutn("MONEY3", line, money);

        //受験番号
        svf.VrsOutn("EXAM_NO1", line, printData9._examno);
        svf.VrsOutn("EXAM_NO2", line, printData9._examno);
        svf.VrsOutn("EXAM_NO3", line, printData9._examno);

        //銀行名
        final String bankname = KnjDbUtils.getString(_param._schoolBankMst, "BANKNAME");
        svf.VrsOutn("BANK_NAME1", line, bankname);
        svf.VrsOutn("BANK_NAME2", line, bankname);
        svf.VrsOutn("BANK_NAME3", line, bankname);

        //講座種別
        final String depositTypeName = KnjDbUtils.getString(_param._schoolBankMst, "DEPOSIT_ITEM_NAME");
        svf.VrsOutn("DEPOSIT_TYPE", line, depositTypeName);

        //口座番号
        final String acNumber = KnjDbUtils.getString(_param._schoolBankMst, "ACCOUNTNO");
        svf.VrsOutn("AC_NUMBER", line, acNumber);

        //受験番号項目文字列
        svf.VrsOutn("EXAM_NO_NAME1", line, "受験番号");
        svf.VrsOutn("EXAM_NO_NAME2", line, "受験番号");

        //志願者氏名(1,2列目)
        final int nameByte = KNJ_EditEdit.getMS932ByteLength(printData9._name);
        final String nameFieldStr = nameByte <= 22 ? "1" : nameByte <= 30 ? "2" : "3";
        svf.VrsOutn("NAME1_" + nameFieldStr, line, printData9._name);
        svf.VrsOutn("NAME2_" + nameFieldStr, line, printData9._name);

        //志願者氏名(3列目)
        final int nameByte2 = KNJ_EditEdit.getMS932ByteLength(printData9._name);
        final String nameFieldStr2 = nameByte2 <= 18 ? "1" : nameByte2 <= 30 ? "2" : "3";
        svf.VrsOutn("NAME3_" + nameFieldStr2, line, printData9._name);

        //志願者氏名フリガナ(3列目)
        final int kanaByte = KNJ_EditEdit.getMS932ByteLength(printData9._name_Kana);
        final String kanaFieldStr = kanaByte <= 18 ? "1" : kanaByte <= 30 ? "2" : "3";
        svf.VrsOutn("KANA3_" + kanaFieldStr, line, printData9._name_Kana);

        svf.VrsOutn("EXAM_DIV", line, _param._testDiv);
    }

    //カタカナ⇒ひらがな
    private String getHiraFrom(String str) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
          char code = str.charAt(i);
          if ((code >= 0x30a1) && (code <= 0x30f3)) {
            sb.append((char) (code - 0x60));
          } else {
            sb.append(code);
          }
        }
        return sb.toString();
    }

    private List<PrintData1> getPassList(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<PrintData1> printData1List = new ArrayList<PrintData1>();
        PrintData1 printData1 = null;

        try {
            final String sql = getPassListSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String no = rs.getString("NO");
                final String testdiv_Name = rs.getString("TESTDIV_NAME");
                final String gakka = rs.getString("GAKKA");
                final String examno = rs.getString("EXAMNO");
                final String passcourse = rs.getString("PASSCOURSE");
                final String name = rs.getString("NAME");
                final String name_Kana = rs.getString("NAME_KANA");
                final String sex = rs.getString("SEX");
                final String fs_Cd = rs.getString("FS_CD");
                final String finschool_Name_Abbv = rs.getString("FINSCHOOL_NAME_ABBV");
                final String honorDiv = rs.getString("HONORDIV");
                final String spName = rs.getString("SP_NAME");
                final String spReasonRemark = rs.getString("SP_REASON_MARK");
                final String ryo = rs.getString("RYO");
                final String gname = rs.getString("GNAME");
                final String zipcd = rs.getString("ZIPCD");
                final String address1 = rs.getString("ADDRESS1");
                final String address2 = rs.getString("ADDRESS2");
                final String telno = rs.getString("TELNO");
                final String duplicateFlg = rs.getString("DUPLICATE_FLG");

                printData1 = new PrintData1(no, testdiv_Name, gakka, examno, passcourse, name, name_Kana, sex, fs_Cd,
                        finschool_Name_Abbv, honorDiv, spName, spReasonRemark, ryo, gname, zipcd, address1, address2, telno, duplicateFlg);
                printData1List.add(printData1);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return printData1List;
    }

    private List<PrintData2> getFailList(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<PrintData2> printData2List = new ArrayList<PrintData2>();
        PrintData2 printData2 = null;

        try {
            final String sql = getfailListSql();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String no = rs.getString("NO");
                final String testdiv_Name = rs.getString("TESTDIV_NAME");
                final String gakka = rs.getString("GAKKA");
                final String examno = rs.getString("EXAMNO");
                final String aspiring1 = rs.getString("ASPIRING1");
                final String aspiring2 = rs.getString("ASPIRING2");
                final String aspiring3 = rs.getString("ASPIRING3");
                final String aspiring4 = rs.getString("ASPIRING4");
                final String name = rs.getString("NAME");
                final String name_Kana = rs.getString("NAME_KANA");
                final String sex = rs.getString("SEX");
                final String fs_Cd = rs.getString("FS_CD");
                final String finschool_Name_Abbv = rs.getString("FINSCHOOL_NAME_ABBV");
                final String honorDiv = rs.getString("HONORDIV");
                final String spName = rs.getString("SP_NAME");
                final String spReasonRemark = rs.getString("SP_REASON_MARK");
                final String ryo = rs.getString("RYO");
                final String gname = rs.getString("GNAME");
                final String hantei = rs.getString("HANTEI");
                final String duplicateFlg = rs.getString("DUPLICATE_FLG");

                printData2 = new PrintData2(no, testdiv_Name, gakka, examno, aspiring1, aspiring2, aspiring3, aspiring4, name, name_Kana, sex, fs_Cd, finschool_Name_Abbv, honorDiv, spName, spReasonRemark, ryo, gname, hantei, duplicateFlg);
                printData2List.add(printData2);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return printData2List;
    }

    private List<PrintData3> getPassNotice(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<PrintData3> printData3List = new ArrayList<PrintData3>();
        PrintData3 printData3 = null;

        try {
            final String sql = getPassNoticeSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String finschool_Name = rs.getString("FINSCHOOL_NAME");
                final String passCourse = rs.getString("PASSCOURSE");
                final String gakka = rs.getString("GAKKA");

                printData3 = new PrintData3(examno, name, finschool_Name, passCourse, gakka);
                printData3List.add(printData3);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return printData3List;
    }

    private Map<String, PrintData5> getExamStatus(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        Map<String, PrintData5> printData5Map = new LinkedMap();
        PrintData5 printData5 = null;

        try {
            final String sql = getExamStatusSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String testdiv = rs.getString("TESTDIV");
                final String sex = rs.getString("SEX");
                final String shigansya = rs.getString("SHIGANSYA");
                final String jukensya = rs.getString("JUKENSYA");
                final String pass = rs.getString("PASS");
                final String nyugakukin = rs.getString("NYUGAKUKIN");
                final String setsubihi = rs.getString("SETSUBIHI");
                final String ent = rs.getString("ENT");

                //入試状況１:学科別
                if("1".equals(_param._exam)) {
                    final String testdiv0 = rs.getString("TESTDIV0");
                    final String key = testdiv0 + testdiv + sex;
                    printData5 = new PrintData5(shigansya, jukensya, pass, nyugakukin, setsubihi, ent);
                    printData5Map.put(key, printData5);
                } else if("2".equals(_param._exam)) {
                    final String testdiv0 = rs.getString("TESTDIV0");
                    final String remark7 = rs.getString("REMARK7");
                    final String key = testdiv0 + remark7 + testdiv + sex;
                    printData5 = new PrintData5(shigansya, jukensya, pass, nyugakukin, setsubihi, ent);
                    printData5Map.put(key, printData5);
                } else {
                    final String remark1 = rs.getString("REMARK1");
                    final String key = remark1 + testdiv + sex;
                    printData5 = new PrintData5(shigansya, jukensya, pass, nyugakukin, setsubihi, ent);
                    printData5Map.put(key, printData5);
                }

            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return printData5Map;
    }

    private List<PrintData6> getNgList(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<PrintData6> printData6List = new ArrayList<PrintData6>();
        PrintData6 printData6 = null;

        try {
            final String sql = getNgListSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String testdiv = rs.getString("TESTDIV");
                final String testdiv_Name = rs.getString("TESTDIV_NAME");
                final String fs_Cd = rs.getString("FS_CD");
                final String finschool_Name = rs.getString("FINSCHOOL_NAME");
                final String jukensya = rs.getString("JUKENSYA");
                final String kesseki = rs.getString("KESSEKI");
                final String pass = rs.getString("PASS");


                printData6 = new PrintData6(testdiv, testdiv_Name, fs_Cd, finschool_Name, jukensya, kesseki, pass);
                printData6List.add(printData6);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return printData6List;
    }

    private List<PrintData7> getResult(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<PrintData7> printData7List = new ArrayList<PrintData7>();
        PrintData7 printData7 = null;

        try {
            final String sql = getResultSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String fs_Cd = rs.getString("FS_CD");
                final String finschool_Name = rs.getString("FINSCHOOL_NAME");
                final String testdiv = rs.getString("TESTDIV");
                final String testdiv_Name = rs.getString("TESTDIV_NAME");
                final String remark2 = rs.getString("REMARK2");
                final String princname = rs.getString("PRINCNAME");
                final String no = rs.getString("NO");
                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String judgement = rs.getString("JUDGEMENT");
                final String shibou = rs.getString("SHIBOU");
                final String gouhi = rs.getString("GOUHI");
                final String course = rs.getString("COURSE");
                final String special = rs.getString("SPECIAL");

                printData7 = new PrintData7(fs_Cd, finschool_Name, remark2, testdiv, testdiv_Name, princname, no, examno, name, judgement, shibou, gouhi, course, special);
                printData7List.add(printData7);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return printData7List;
    }

    private List<PrintData8> getResult2(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<PrintData8> printData8List = new ArrayList<PrintData8>();
        PrintData8 printData8 = null;
        try {
            final String sql = getResult2Sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String fs_Cd = rs.getString("FS_CD");
                final String finschool_Name = rs.getString("FINSCHOOL_NAME");
                final String remark2 = rs.getString("REMARK2");
                final String testdiv = rs.getString("TESTDIV");
                final String testdiv_Name = rs.getString("TESTDIV_NAME");
                final String setsubi = rs.getString("SETSUBI");
                final String special = rs.getString("SPECIAL");
                final String pass = rs.getString("PASS");
                final String schedule1_1 = rs.getString("SCHEDULE1_1");
                final String schedule1_2 = rs.getString("SCHEDULE1_2");
                final String schedule1_3 = rs.getString("SCHEDULE1_3");
                final String schedule1_4 = rs.getString("SCHEDULE1_4");
                final String schedule1_5 = rs.getString("SCHEDULE1_5");
                final String schedule1_6 = rs.getString("SCHEDULE1_6");
                final String schedule2_1 = rs.getString("SCHEDULE2_1");
                final String schedule2_2 = rs.getString("SCHEDULE2_2");
                final String schedule2_3 = rs.getString("SCHEDULE2_3");
                final String schedule2_4 = rs.getString("SCHEDULE2_4");
                final String schedule2_5 = rs.getString("SCHEDULE2_5");
                final String schedule2_6 = rs.getString("SCHEDULE2_6");
                final String schedule3_1 = rs.getString("SCHEDULE3_1");
                final String schedule3_2 = rs.getString("SCHEDULE3_2");
                final String schedule3_3 = rs.getString("SCHEDULE3_3");
                final String schedule3_4 = rs.getString("SCHEDULE3_4");
                final String schedule3_5 = rs.getString("SCHEDULE3_5");
                final String schedule3_6 = rs.getString("SCHEDULE3_6");
                final String schedule4_1 = rs.getString("SCHEDULE4_1");
                final String schedule4_2 = rs.getString("SCHEDULE4_2");
                final String schedule4_3 = rs.getString("SCHEDULE4_3");
                final String schedule4_4 = rs.getString("SCHEDULE4_4");
                final String schedule4_5 = rs.getString("SCHEDULE4_5");
                final String schedule4_6 = rs.getString("SCHEDULE4_6");

                final Map scheduleMap = new LinkedMap();
                if(schedule1_1 != null) {
                    final String schedule[] = {schedule1_1, schedule1_2, schedule1_3, schedule1_4, schedule1_5, schedule1_6};
                    scheduleMap.put("1", schedule);
                }
                if(schedule2_1 != null) {
                    final String schedule[] = {schedule2_1, schedule2_2, schedule2_3, schedule2_4, schedule2_5, schedule2_6};
                    scheduleMap.put("2", schedule);
                }
                if(schedule3_1 != null) {
                    final String schedule[] = {schedule3_1, schedule3_2, schedule3_3, schedule3_4, schedule3_5, schedule3_6};
                    scheduleMap.put("3", schedule);
                }
                if(schedule4_1 != null) {
                    final String schedule[] = {schedule4_1, schedule4_2, schedule4_3, schedule4_4, schedule4_5, schedule4_6};
                    scheduleMap.put("4", schedule);
                }

                printData8 = new PrintData8(fs_Cd, finschool_Name, remark2, testdiv, testdiv_Name, setsubi, special, pass, scheduleMap);
                printData8List.add(printData8);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return printData8List;
    }

    private List<PrintData9> getFurikomi(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<PrintData9> printData9List = new ArrayList<PrintData9>();
        PrintData9 printData9 = null;

        try {
            final String sql = getFurikomiSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String testdiv = rs.getString("TESTDIV");
                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String name_Kana = rs.getString("NAME_KANA");
                final String remark4 = rs.getString("REMARK4");
                final String remark5 = rs.getString("REMARK5");
                final String remark7 = rs.getString("REMARK7");
                final String remark8 = rs.getString("REMARK8");
                final String finschool_Name = rs.getString("FINSCHOOL_NAME");
                final String nyugaku = rs.getString("NYUGAKU");
                final String setsubi = rs.getString("SETSUBI");


                printData9 = new PrintData9(testdiv, examno, name, name_Kana, remark4, remark5, remark7, remark8, finschool_Name, nyugaku, setsubi);
                printData9List.add(printData9);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return printData9List;
    }

    private String getPassListSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        if("1".equals(_param._sort)) {
            stb.append("   ROW_NUMBER() OVER(PARTITION BY BASE.TESTDIV0, BASE.TESTDIV ORDER BY BASE.TESTDIV0, BASE.TESTDIV, BASE.EXAMNO, BASE.NAME_KANA) AS NO, ");
        } else {
            stb.append("   ROW_NUMBER() OVER(PARTITION BY BASE.TESTDIV0, BASE.TESTDIV ORDER BY BASE.TESTDIV0, BASE.TESTDIV, BASE.NAME_KANA, BASE.EXAMNO) AS NO, ");
        }
        stb.append("     TEST.TESTDIV_NAME, ");
        stb.append("     CASE WHEN BASE.TESTDIV0 = '1' THEN '" + HUTSUU + "' ELSE '" + KOUGYOU + "' END AS GAKKA, ");
        stb.append("     BASE.EXAMNO, ");
        stb.append("     GENE02.GENERAL_NAME AS PASSCOURSE, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.NAME_KANA, ");
        stb.append("     NAME.NAME2 AS SEX, ");
        stb.append("     BASE.FS_CD, ");
        stb.append("     SCHOOL.FINSCHOOL_NAME_ABBV, ");
        stb.append("     GENE04.GENERAL_CD AS HONORDIV, ");
        stb.append("     GENE04.GENERAL_NAME AS SP_NAME, ");
        stb.append("     GENE05.GENERAL_MARK AS SP_REASON_MARK, ");
        stb.append("     CASE WHEN BASE.DORMITORY_FLG = '1' THEN SET042.NAME1 END AS RYO, ");
        stb.append("     ADDR.GNAME, ");
        stb.append("     ADDR.ZIPCD, ");
        stb.append("     ADDR.ADDRESS1, ");
        stb.append("     ADDR.ADDRESS2, ");
        stb.append("     ADDR.TELNO, ");
        stb.append("     CASE WHEN BASE_D012.REMARK1 IS NOT NULL AND BASE.EXAMNO <> BASE_D012.REMARK1 THEN '1' ");
        stb.append("          WHEN BASE_D012.REMARK2 IS NOT NULL AND BASE.EXAMNO <> BASE_D012.REMARK2 THEN '1' ");
        stb.append("          WHEN BASE_D012.REMARK3 IS NOT NULL AND BASE.EXAMNO <> BASE_D012.REMARK3 THEN '1' ");
        stb.append("          ELSE NULL ");
        stb.append("     END AS DUPLICATE_FLG ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_APPLICANTBASE_DETAIL_DAT BD031 ON BD031.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND BD031.APPLICANTDIV = BASE.APPLICANTDIV AND BD031.EXAMNO = BASE.EXAMNO AND BD031.SEQ = '031' ");
        stb.append(" LEFT JOIN ");
        stb.append("     V_NAME_MST NAME ON NAME.YEAR = BASE.ENTEXAMYEAR AND NAMECD1 = 'Z002' AND NAMECD2 = BASE.SEX ");
        stb.append(" LEFT JOIN ");
        stb.append("     FINSCHOOL_MST SCHOOL ON SCHOOL.FINSCHOOLCD = BASE.FS_CD ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_TESTDIV_MST TEST ON TEST.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND TEST.APPLICANTDIV = BASE.APPLICANTDIV AND TEST.TESTDIV = BASE.TESTDIV ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_APPLICANTADDR_DAT ADDR ON ADDR.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND ADDR.APPLICANTDIV = BASE.APPLICANTDIV AND ADDR.EXAMNO = BASE.EXAMNO ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_SETTING_MST SET042 ON SET042.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND SET042.APPLICANTDIV = BASE.APPLICANTDIV AND SET042.SETTING_CD = 'L042' AND SET042.SEQ = BASE.SEX ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_RECEPT_DAT RPT ON RPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND RPT.APPLICANTDIV = BASE.APPLICANTDIV AND RPT.TESTDIV = BASE.TESTDIV AND RPT.EXAMNO = BASE.EXAMNO ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_RECEPT_DETAIL_DAT RD015 ON RD015.ENTEXAMYEAR = RPT.ENTEXAMYEAR AND RD015.APPLICANTDIV = RPT.APPLICANTDIV AND RD015.TESTDIV = RPT.TESTDIV AND RD015.EXAM_TYPE = RPT.EXAM_TYPE AND RD015.RECEPTNO = RPT.RECEPTNO AND RD015.SEQ = '015' ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_GENERAL_MST GENE01 ON GENE01.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND GENE01.APPLICANTDIV = BASE.APPLICANTDIV AND GENE01.TESTDIV = '0' AND GENE01.GENERAL_DIV = '01' AND GENE01.GENERAL_CD = RD015.REMARK3 ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_GENERAL_MST GENE03 ON GENE03.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND GENE03.APPLICANTDIV = BASE.APPLICANTDIV AND GENE03.TESTDIV = '0' AND GENE03.GENERAL_DIV = '03' AND GENE03.GENERAL_CD = RD015.REMARK3 ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_GENERAL_MST GENE02 ON GENE02.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND GENE02.APPLICANTDIV = BASE.APPLICANTDIV AND GENE02.TESTDIV = '0' AND GENE02.GENERAL_DIV = '02' AND GENE02.GENERAL_CD = GENE03.REMARK1 ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_GENERAL_MST GENE04 ON GENE04.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND GENE04.APPLICANTDIV = BASE.APPLICANTDIV AND GENE04.TESTDIV = '0' AND GENE04.GENERAL_DIV = '04' AND GENE04.GENERAL_CD = RD015.REMARK4 ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_GENERAL_MST GENE05 ON GENE05.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND GENE05.APPLICANTDIV = BASE.APPLICANTDIV AND GENE05.TESTDIV = '0' AND GENE05.GENERAL_DIV = '05' AND GENE05.GENERAL_CD = RD015.REMARK5 ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D012 ON BASE_D012.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND BASE_D012.APPLICANTDIV = BASE.APPLICANTDIV AND BASE_D012.EXAMNO = BASE.EXAMNO AND BASE_D012.SEQ = '012' ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' AND ");
        stb.append("     BASE.APPLICANTDIV = '2' AND ");
        stb.append("     BASE.TESTDIV = '" + _param._testDiv + "' ");
        if(!"ALL".equals(_param._gakka)) {
            stb.append("   AND BASE.TESTDIV0 = '" + _param._gakka + "' ");
        }
        if(!"".equals(_param._ruibetsu)) {
            stb.append("   AND BD031.REMARK7 = '" + _param._ruibetsu + "' ");
        }
        if(!"".equals(_param._course)) {
            stb.append("   AND GENE02.GENERAL_CD = '" + _param._course + "' ");
        }
        if(!"3".equals(_param._sex)) {
            stb.append("   AND BASE.SEX = '" + _param._sex + "' ");
        }
        if(!"".equals(_param._s_examNo) && !"".equals(_param._e_examNo)) {
            stb.append("   AND BASE.EXAMNO BETWEEN " + _param._s_examNo + " AND " + _param._e_examNo );
        } else if(!"".equals(_param._s_examNo)) {
            stb.append("   AND BASE.EXAMNO >= '" + _param._s_examNo + "' ");
        } else if(!"".equals(_param._e_examNo)) {
            stb.append("   AND BASE.EXAMNO <= '" + _param._e_examNo + "' ");
        }
        stb.append("     AND RPT.JUDGEDIV = '1' "); //合格者のみ
        return stb.toString();
    }

    private String getfailListSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        if("1".equals(_param._sort)) {
            stb.append("   ROW_NUMBER() OVER(ORDER BY BASE.TESTDIV0, BASE.TESTDIV, BASE.EXAMNO, BASE.NAME_KANA) AS NO, ");
        } else {
            stb.append("   ROW_NUMBER() OVER(ORDER BY BASE.TESTDIV0, BASE.TESTDIV, BASE.NAME_KANA, BASE.EXAMNO) AS NO, ");
        }
        stb.append("     TEST.TESTDIV_NAME, ");
        stb.append("     BASE.EXAMNO, ");
        stb.append("     CASE WHEN BASE.TESTDIV0 = '1' THEN '" + HUTSUU + "' ELSE '" + KOUGYOU + "' END AS GAKKA, ");
        stb.append("     BD031.REMARK1 AS ASPIRING1, ");
        stb.append("     VALUE(BD031.REMARK2, '0') AS ASPIRING2, ");
        stb.append("     VALUE(BD031.REMARK3, '0') AS ASPIRING3, ");
        stb.append("     VALUE(BD031.REMARK4, '0') AS ASPIRING4, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.NAME_KANA, ");
        stb.append("     NAME.NAME2 AS SEX, ");
        stb.append("     BASE.FS_CD, ");
        stb.append("     SCHOOL.FINSCHOOL_NAME_ABBV, ");
        stb.append("     GENE04.GENERAL_CD AS HONORDIV, ");
        stb.append("     GENE04.GENERAL_NAME AS SP_NAME, ");
        stb.append("     GENE05.GENERAL_MARK AS SP_REASON_MARK, ");
        stb.append("     CASE WHEN BASE.DORMITORY_FLG = '1' THEN SET042.NAME1 END AS RYO, ");
        stb.append("     ADDR.GNAME, ");
        stb.append("     GENE03.GENERAL_MARK AS HANTEI, ");
        stb.append("     CASE WHEN BASE_D012.REMARK1 IS NOT NULL AND BASE.EXAMNO <> BASE_D012.REMARK1 THEN '1' ");
        stb.append("          WHEN BASE_D012.REMARK2 IS NOT NULL AND BASE.EXAMNO <> BASE_D012.REMARK2 THEN '1' ");
        stb.append("          WHEN BASE_D012.REMARK3 IS NOT NULL AND BASE.EXAMNO <> BASE_D012.REMARK3 THEN '1' ");
        stb.append("          ELSE NULL ");
        stb.append("     END AS DUPLICATE_FLG ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_APPLICANTBASE_DETAIL_DAT BD031 ON BD031.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND BD031.APPLICANTDIV = BASE.APPLICANTDIV AND BD031.EXAMNO = BASE.EXAMNO AND BD031.SEQ = '031' ");
        stb.append(" LEFT JOIN ");
        stb.append("     V_NAME_MST NAME ON NAME.YEAR = BASE.ENTEXAMYEAR AND NAMECD1 = 'Z002' AND NAMECD2 = BASE.SEX LEFT JOIN FINSCHOOL_MST SCHOOL ON SCHOOL.FINSCHOOLCD = BASE.FS_CD ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_TESTDIV_MST TEST ON TEST.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND TEST.APPLICANTDIV = BASE.APPLICANTDIV AND TEST.TESTDIV = BASE.TESTDIV ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_APPLICANTADDR_DAT ADDR ON ADDR.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND ADDR.APPLICANTDIV = BASE.APPLICANTDIV AND ADDR.EXAMNO = BASE.EXAMNO ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_SETTING_MST SET042 ON SET042.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND SET042.APPLICANTDIV = BASE.APPLICANTDIV AND SET042.SETTING_CD = 'L042' AND SET042.SEQ = BASE.SEX ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_RECEPT_DAT RPT ON RPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND RPT.APPLICANTDIV = BASE.APPLICANTDIV AND RPT.TESTDIV = BASE.TESTDIV AND RPT.EXAMNO = BASE.EXAMNO ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_RECEPT_DETAIL_DAT RD015 ON RD015.ENTEXAMYEAR = RPT.ENTEXAMYEAR AND RD015.APPLICANTDIV = RPT.APPLICANTDIV AND RD015.TESTDIV = RPT.TESTDIV AND RD015.EXAM_TYPE = RPT.EXAM_TYPE AND RD015.RECEPTNO = RPT.RECEPTNO AND RD015.SEQ = '015' ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_GENERAL_MST GENE03 ON GENE03.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND GENE03.APPLICANTDIV = BASE.APPLICANTDIV AND GENE03.TESTDIV = '0' AND GENE03.GENERAL_DIV = '03' AND GENE03.GENERAL_CD = RD015.REMARK3 ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_GENERAL_MST GENE04 ON GENE04.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND GENE04.APPLICANTDIV = BASE.APPLICANTDIV AND GENE04.TESTDIV = '0' AND GENE04.GENERAL_DIV = '04' AND GENE04.GENERAL_CD = RD015.REMARK4 ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_GENERAL_MST GENE05 ON GENE05.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND GENE05.APPLICANTDIV = BASE.APPLICANTDIV AND GENE05.TESTDIV = '0' AND GENE05.GENERAL_DIV = '05' AND GENE05.GENERAL_CD = RD015.REMARK5 ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D012 ON BASE_D012.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND BASE_D012.APPLICANTDIV = BASE.APPLICANTDIV AND BASE_D012.EXAMNO = BASE.EXAMNO AND BASE_D012.SEQ = '012' ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' AND ");
        stb.append("     BASE.APPLICANTDIV = '2' AND ");
        stb.append("     BASE.TESTDIV = '" + _param._testDiv + "' ");
        if(!"ALL".equals(_param._gakka)) {
            stb.append("   AND BASE.TESTDIV0 = '" + _param._gakka + "' ");
        }
        if(!"".equals(_param._ruibetsu)) {
            stb.append("   AND BD031.REMARK7 = '" + _param._ruibetsu + "' ");
        }
        if(!"".equals(_param._course)) {
            stb.append("   AND BD031.REMARK1 = '" + _param._course + "' ");
        }
        if(!"3".equals(_param._sex)) {
            stb.append("   AND BASE.SEX = '" + _param._sex + "' ");
        }
        if(!"".equals(_param._s_examNo) && !"".equals(_param._e_examNo)) {
            stb.append("   AND BASE.EXAMNO BETWEEN " + _param._s_examNo + " AND " + _param._e_examNo );
        } else if(!"".equals(_param._s_examNo)) {
            stb.append("   AND BASE.EXAMNO >= '" + _param._s_examNo + "' ");
        } else if(!"".equals(_param._e_examNo)) {
            stb.append("   AND BASE.EXAMNO <= '" + _param._e_examNo + "' ");
        }
        stb.append("    AND (BASE.JUDGEMENT = '2' OR BASE.JUDGEMENT = '4') "); //不合格者、未受験含む

        return stb.toString();
    }

    private String getPassNoticeSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   BASE.EXAMNO, ");
        stb.append("   GENE01.REMARK1 AS GAKKA, ");
        stb.append("   GENE02.REMARK3 AS PASSCOURSE, ");
        stb.append("   BASE.NAME, ");
        stb.append("   SCHOOL.FINSCHOOL_NAME ");
        stb.append(" FROM ");
        stb.append("   ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append(" LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD031  ");
        stb.append("   ON BD031.ENTEXAMYEAR = BASE.ENTEXAMYEAR  ");
        stb.append("   AND BD031.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("   AND BD031.EXAMNO = BASE.EXAMNO AND BD031.SEQ = '031' ");
        stb.append(" LEFT JOIN FINSCHOOL_MST SCHOOL ");
        stb.append("   ON SCHOOL.FINSCHOOLCD = BASE.FS_CD ");
        stb.append(" LEFT JOIN ENTEXAM_RECEPT_DAT RPT ");
        stb.append("   ON RPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("   AND RPT.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("   AND RPT.TESTDIV = BASE.TESTDIV ");
        stb.append("   AND RPT.EXAMNO = BASE.EXAMNO ");
        stb.append(" LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD015 ");
        stb.append("   ON RD015.ENTEXAMYEAR = RPT.ENTEXAMYEAR ");
        stb.append("   AND RD015.APPLICANTDIV = RPT.APPLICANTDIV ");
        stb.append("   AND RD015.TESTDIV = RPT.TESTDIV ");
        stb.append("   AND RD015.EXAM_TYPE = RPT.EXAM_TYPE ");
        stb.append("   AND RD015.RECEPTNO = RPT.RECEPTNO ");
        stb.append("   AND RD015.SEQ = '015' ");
        stb.append(" LEFT JOIN ENTEXAM_GENERAL_MST GENE03 ");
        stb.append("   ON GENE03.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("   AND GENE03.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("   AND GENE03.TESTDIV = '0' ");
        stb.append("   AND GENE03.GENERAL_DIV = '03' ");
        stb.append("   AND GENE03.GENERAL_CD = RD015.REMARK3 ");
        stb.append(" LEFT JOIN ENTEXAM_GENERAL_MST GENE02 ");
        stb.append("   ON GENE02.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("   AND GENE02.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("   AND GENE02.TESTDIV = '0' ");
        stb.append("   AND GENE02.GENERAL_DIV = '02' ");
        stb.append("   AND GENE02.GENERAL_CD = GENE03.REMARK1 ");
        stb.append(" LEFT JOIN ENTEXAM_GENERAL_MST GENE01 ");
        stb.append("   ON GENE01.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("   AND GENE01.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("   AND GENE01.TESTDIV = '0' ");
        stb.append("   AND GENE01.GENERAL_DIV = '01' ");
        stb.append("   AND GENE01.GENERAL_CD = GENE02.REMARK1 ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' AND ");
        stb.append("     BASE.APPLICANTDIV = '2' AND ");
        stb.append("     BASE.TESTDIV = '" + _param._testDiv + "' AND ");
        stb.append("     RPT.JUDGEDIV = '1' ");
        if(!"ALL".equals(_param._gakka)) {
            stb.append("   AND BASE.TESTDIV0 = '" + _param._gakka + "' ");
        }
        if(!"".equals(_param._ruibetsu)) {
            stb.append("   AND BD031.REMARK7 = '" + _param._ruibetsu + "' ");
        }
        if(!"".equals(_param._course)) {
            stb.append("   AND BD031.REMARK1 = '" + _param._course + "' ");
        }
        if(!"3".equals(_param._sex)) {
            stb.append("   AND BASE.SEX = '" + _param._sex + "' ");
        }
        if(!"".equals(_param._s_examNo) && !"".equals(_param._e_examNo)) {
            stb.append("   AND BASE.EXAMNO BETWEEN " + _param._s_examNo + " AND " + _param._e_examNo );
        } else if(!"".equals(_param._s_examNo)) {
            stb.append("   AND BASE.EXAMNO >= '" + _param._s_examNo + "' ");
        } else if(!"".equals(_param._e_examNo)) {
            stb.append("   AND BASE.EXAMNO <= '" + _param._e_examNo + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("   BASE.EXAMNO, ");
        stb.append("   BASE.NAME_KANA ");


        return stb.toString();
    }

    private String getExamStatusSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH BASE AS (SELECT ");
        stb.append("     BASE.EXAMNO, ");
        stb.append("     BASE.TESTDIV, ");
        stb.append("     BASE.TESTDIV0, ");
        stb.append("     BD031.REMARK7, ");
        stb.append("     BD031.REMARK1, ");
        stb.append("     BASE.SEX, ");
        stb.append("     BASE.JUDGEMENT, ");
        stb.append("     RDT020.REMARK1 AS NYUGAKUKIN, ");
        stb.append("     RDT020.REMARK3 AS SETSUBIHI, ");
        stb.append("     BASE.PROCEDUREDIV, ");
        stb.append("     BASE.ENTDIV ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_APPLICANTBASE_DETAIL_DAT BD031 ");
        stb.append("     ON BD031.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("     AND BD031.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("     AND BD031.EXAMNO = BASE.EXAMNO AND BD031.SEQ = '031' ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_RECEPT_DAT RPT ");
        stb.append("     ON RPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("     AND RPT.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("     AND RPT.TESTDIV = BASE.TESTDIV ");
        stb.append("     AND RPT.RECEPTNO = BASE.EXAMNO ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_RECEPT_DETAIL_DAT RDT020 ");
        stb.append("     ON RDT020.ENTEXAMYEAR = RPT.ENTEXAMYEAR ");
        stb.append("     AND RDT020.APPLICANTDIV = RPT.APPLICANTDIV ");
        stb.append("     AND RDT020.TESTDIV = RPT.TESTDIV ");
        stb.append("     AND RDT020.RECEPTNO = RPT.RECEPTNO ");
        stb.append("     AND RDT020.SEQ = '020' ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND BASE.APPLICANTDIV = '2' ");
        stb.append("     AND BASE.TESTDIV0 IS NOT NULL ");
        stb.append("     AND BD031.REMARK7 IS NOT NULL ");
        stb.append(" ORDER BY ");
        stb.append("     BASE.EXAMNO ");
        stb.append(" ), KEKKA AS (SELECT ");
        if("1".equals(_param._exam)) {
            stb.append("     VALUE(TESTDIV0, '99') AS TESTDIV0, ");
            stb.append("     VALUE(TESTDIV, '99') AS TESTDIV, ");
            stb.append("     VALUE(SEX, '3') AS SEX, ");
            stb.append("     COUNT(EXAMNO) AS SHIGANSYA, ");
            stb.append("     COUNT(JUDGEMENT <> '4' OR JUDGEMENT IS NULL OR NULL) AS JUKENSYA, ");
            stb.append("     COUNT(JUDGEMENT = '1' OR NULL) AS PASS, ");
            stb.append("     COUNT(NYUGAKUKIN = '1' OR NULL) AS NYUGAKUKIN, ");
            stb.append("     COUNT(SETSUBIHI = '1' OR NULL) AS SETSUBIHI, ");
            stb.append("     COUNT(ENTDIV = '1' AND PROCEDUREDIV = '1' OR NULL) AS ENT ");
            stb.append(" FROM ");
            stb.append("     BASE ");
            stb.append(" GROUP BY ");
            stb.append("     GROUPING SETS ((TESTDIV0, TESTDIV, SEX),(TESTDIV0, TESTDIV), (TESTDIV, SEX), (TESTDIV0), (TESTDIV0, SEX), (TESTDIV), (SEX),()) ");
            stb.append(" ORDER BY ");
            stb.append("     TESTDIV0, ");
            stb.append("     TESTDIV, ");
            stb.append("     SEX ");
            stb.append(" ) SELECT ");
            stb.append("     KEKKA.*, ");
            stb.append("     CASE WHEN KEKKA.TESTDIV0 = '1' THEN '" + HUTSUU + "' WHEN KEKKA.TESTDIV0 = '2' THEN '" + KOUGYOU + "' ELSE '合計' END AS GAKKA, ");
            stb.append("     VALUE(TEST.TESTDIV_NAME, '計') AS TESTDIV_NAME, ");
            stb.append("     VALUE(NAME.NAME1, '計') AS SEX_NAME ");
            stb.append(" FROM ");
            stb.append("     KEKKA ");
            stb.append(" LEFT JOIN ");
            stb.append("     V_NAME_MST NAME ");
            stb.append("     ON NAME.YEAR = '" + _param._entexamyear + "' ");
            stb.append("     AND NAMECD1 = 'Z002' ");
            stb.append("     AND NAMECD2 = KEKKA.SEX ");
            stb.append(" LEFT JOIN ");
            stb.append("     ENTEXAM_TESTDIV_MST TEST ");
            stb.append("     ON TEST.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
            stb.append("     AND TEST.APPLICANTDIV = '2' ");
            stb.append("     AND TEST.TESTDIV = KEKKA.TESTDIV ");
        } else if("2".equals(_param._exam)) {
            stb.append("     VALUE(TESTDIV0, '99') AS TESTDIV0, ");
            stb.append("     VALUE(REMARK7, '99') AS REMARK7, ");
            stb.append("     VALUE(TESTDIV, '99') AS TESTDIV, ");
            stb.append("     VALUE(SEX, '3') AS SEX, ");
            stb.append("     COUNT(EXAMNO) AS SHIGANSYA, ");
            stb.append("     COUNT(JUDGEMENT <> '4' OR JUDGEMENT IS NULL OR NULL) AS JUKENSYA, ");
            stb.append("     COUNT(JUDGEMENT = '1' OR NULL) AS PASS, ");
            stb.append("     COUNT(NYUGAKUKIN = '1' OR NULL) AS NYUGAKUKIN, ");
            stb.append("     COUNT(SETSUBIHI = '1' OR NULL) AS SETSUBIHI, ");
            stb.append("     COUNT(ENTDIV = '1' AND PROCEDUREDIV = '1' OR NULL) AS ENT ");
            stb.append(" FROM ");
            stb.append("     BASE ");
            stb.append(" GROUP BY ");
            stb.append("     GROUPING SETS ((TESTDIV0,REMARK7, TESTDIV, SEX),(TESTDIV0,REMARK7, TESTDIV),(TESTDIV0,REMARK7),(TESTDIV0,REMARK7, SEX)) ");
            stb.append(" ORDER BY ");
            stb.append("     TESTDIV0, ");
            stb.append("     REMARK7, ");
            stb.append("     TESTDIV, ");
            stb.append("     SEX ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     KEKKA.*, ");
            stb.append("     CASE WHEN KEKKA.TESTDIV0 = '1' THEN '" + HUTSUU + "' WHEN KEKKA.TESTDIV0 = '2' THEN '" + KOUGYOU + "' ELSE '合計' END AS GAKKA, ");
            stb.append("     GENE01.GENERAL_NAME, ");
            stb.append("     VALUE(TEST.TESTDIV_NAME, '計') AS TESTDIV_NAME, ");
            stb.append("     VALUE(NAME.NAME1, '計') AS SEX_NAME ");
            stb.append(" FROM ");
            stb.append("     KEKKA ");
            stb.append(" LEFT JOIN ");
            stb.append("     V_NAME_MST NAME ");
            stb.append("     ON NAME.YEAR = '" + _param._entexamyear + "' ");
            stb.append("     AND NAMECD1 = 'Z002' ");
            stb.append("     AND NAMECD2 = KEKKA.SEX ");
            stb.append(" LEFT JOIN ");
            stb.append("     ENTEXAM_GENERAL_MST GENE01 ");
            stb.append("     ON GENE01.ENTEXAMYEAR= '" + _param._entexamyear + "' ");
            stb.append("     AND GENE01.APPLICANTDIV = '2' ");
            stb.append("     AND GENE01.TESTDIV = '0' ");
            stb.append("     AND GENE01.GENERAL_DIV = '01' ");
            stb.append("     AND GENE01.GENERAL_CD = KEKKA.REMARK7 ");
            stb.append(" LEFT JOIN ");
            stb.append("     ENTEXAM_TESTDIV_MST TEST ");
            stb.append("     ON TEST.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
            stb.append("     AND TEST.APPLICANTDIV = '2' ");
            stb.append("     AND TEST.TESTDIV = KEKKA.TESTDIV ");
        } else if("3".equals(_param._exam)) {
            stb.append("     VALUE(REMARK1, '99') AS REMARK1, ");
            stb.append("     VALUE(TESTDIV, '99') AS TESTDIV, ");
            stb.append("     VALUE(SEX, '3') AS SEX, ");
            stb.append("     COUNT(EXAMNO) AS SHIGANSYA, ");
            stb.append("     COUNT(JUDGEMENT <> '4' OR JUDGEMENT IS NULL OR NULL) AS JUKENSYA, ");
            stb.append("     COUNT(JUDGEMENT = '1' OR NULL) AS PASS, ");
            stb.append("     COUNT(NYUGAKUKIN = '1' OR NULL) AS NYUGAKUKIN, ");
            stb.append("     COUNT(SETSUBIHI = '1' OR NULL) AS SETSUBIHI, ");
            stb.append("     COUNT(ENTDIV = '1' AND PROCEDUREDIV = '1' OR NULL) AS ENT ");
            stb.append(" FROM ");
            stb.append("     BASE ");
            stb.append(" GROUP BY ");
            stb.append("     GROUPING SETS ((REMARK1, TESTDIV, SEX),(REMARK1, TESTDIV),(REMARK1),(REMARK1, SEX)) ");
            stb.append(" ORDER BY ");
            stb.append("     REMARK1, ");
            stb.append("     TESTDIV, ");
            stb.append("     SEX ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     KEKKA.*, ");
            stb.append("     GENE02.GENERAL_NAME, ");
            stb.append("     VALUE(TEST.TESTDIV_NAME, '計') AS TESTDIV_NAME, ");
            stb.append("     VALUE(NAME.NAME1, '計') AS SEX_NAME ");
            stb.append(" FROM ");
            stb.append("     KEKKA ");
            stb.append(" LEFT JOIN V_NAME_MST NAME");
            stb.append("     ON NAME.YEAR = '" + _param._entexamyear + "' ");
            stb.append("     AND NAMECD1 = 'Z002' ");
            stb.append("     AND NAMECD2 = KEKKA.SEX ");
            stb.append(" LEFT JOIN ENTEXAM_GENERAL_MST GENE02 ");
            stb.append("     ON GENE02.ENTEXAMYEAR= '" + _param._entexamyear + "' ");
            stb.append("     AND GENE02.APPLICANTDIV = '2' ");
            stb.append("     AND GENE02.TESTDIV = '0' ");
            stb.append("     AND GENE02.GENERAL_DIV = '02' ");
            stb.append("     AND GENE02.GENERAL_CD = KEKKA.REMARK1 ");
            stb.append(" LEFT JOIN ENTEXAM_TESTDIV_MST TEST ");
            stb.append("     ON TEST.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
            stb.append("     AND TEST.APPLICANTDIV = '2' ");
            stb.append("     AND TEST.TESTDIV = KEKKA.TESTDIV ");
        }
        return stb.toString();
    }

    private String getNgListSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     BASE.TESTDIV, ");
        stb.append("     TEST.TESTDIV_NAME, ");
        stb.append("     BASE.FS_CD, ");
        stb.append("     SCHOOL.FINSCHOOL_NAME, ");
        stb.append("     COUNT(CASE WHEN BASE.JUDGEMENT <> '4' THEN 1 ELSE NULL END) AS JUKENSYA, ");
        stb.append("     COUNT(CASE WHEN BASE.JUDGEMENT = '4' THEN 1 ELSE NULL END) AS KESSEKI, ");
        stb.append("     COUNT(BASE.JUDGEMENT = '1' OR NULL) AS PASS ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append(" LEFT JOIN ");
        stb.append("     FINSCHOOL_MST SCHOOL ");
        stb.append("     ON SCHOOL.FINSCHOOLCD = BASE.FS_CD ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_TESTDIV_MST TEST ");
        stb.append("     ON TEST.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("     AND TEST.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("     AND TEST.TESTDIV = BASE.TESTDIV ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' AND ");
        stb.append("     BASE.APPLICANTDIV = '2' AND ");
        stb.append("     BASE.TESTDIV = '" + _param._testDiv + "' ");
        stb.append(" GROUP BY ");
        stb.append("     BASE.TESTDIV, ");
        stb.append("     TEST.TESTDIV_NAME, ");
        stb.append("     BASE.FS_CD, ");
        stb.append("     SCHOOL.FINSCHOOL_NAME ");
        stb.append(" HAVING ");
        stb.append("         COUNT(CASE WHEN BASE.JUDGEMENT = '1' THEN 1 ELSE NULL END) = 0 ");
        stb.append("     AND COUNT(CASE WHEN BASE.JUDGEMENT IS NULL THEN 1 ELSE NULL END) = 0 ");

        return stb.toString();
    }


    private String getResultSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     BASE.FS_CD, ");
        stb.append("     SCHOOL.FINSCHOOL_NAME, ");
        stb.append("     BASE.TESTDIV, ");
        stb.append("     TEST.TESTDIV_NAME, ");
        stb.append("     TESTDT.REMARK2, ");
        stb.append("     SCHOOL.PRINCNAME, ");
        if("1".equals(_param._sort)) {
            stb.append("     ROW_NUMBER() OVER(PARTITION BY BASE.FS_CD, BASE.TESTDIV ORDER BY BASE.FS_CD, BASE.TESTDIV, BASE.EXAMNO) AS NO, ");
        } else {
            stb.append("     ROW_NUMBER() OVER(PARTITION BY BASE.FS_CD, BASE.TESTDIV ORDER BY BASE.FS_CD, BASE.TESTDIV, BASE.NAME_KANA) AS NO, ");
        }
        stb.append("     BASE.EXAMNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.JUDGEMENT, ");
        stb.append("     CASE WHEN BD031.REMARK1 = GEN03.REMARK1 THEN '第１志望' WHEN BD031.REMARK2 = GEN03.REMARK1 THEN '第２志望' WHEN BD031.REMARK3 = GEN03.REMARK1 THEN '第３志望' WHEN BD031.REMARK4 = GEN03.REMARK1 THEN '第４志望' ELSE '' END AS SHIBOU, ");
        stb.append("     CASE WHEN RPT.JUDGEDIV = '1' THEN '合格' ELSE '不合格' END AS GOUHI, ");
        stb.append("     CASE WHEN GEN01.REMARK1 = '1' THEN '" + HUTSUU + "／' || GEN02.GENERAL_NAME ELSE '" + KOUGYOU + "／' || GEN02.GENERAL_NAME END AS COURSE, ");
        stb.append("     GEN05.GENERAL_NAME || GEN04.GENERAL_MARK AS SPECIAL ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append(" LEFT JOIN ");
        stb.append("     FINSCHOOL_MST SCHOOL ");
        stb.append("     ON SCHOOL.FINSCHOOLCD = BASE.FS_CD ");
        stb.append(" LEFT JOIN ENTEXAM_RECEPT_DAT RPT ");
        stb.append("     ON RPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("     AND RPT.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("     AND RPT.TESTDIV = BASE.TESTDIV ");
        stb.append("     AND RPT.EXAMNO = BASE.EXAMNO ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_RECEPT_DETAIL_DAT RD015 ");
        stb.append("     ON RD015.ENTEXAMYEAR = RPT.ENTEXAMYEAR ");
        stb.append("     AND RD015.APPLICANTDIV = RPT.APPLICANTDIV ");
        stb.append("     AND RD015.TESTDIV = RPT.TESTDIV ");
        stb.append("     AND RD015.RECEPTNO = RPT.RECEPTNO ");
        stb.append("     AND RD015.SEQ = '015' ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_APPLICANTBASE_DETAIL_DAT BD031 ");
        stb.append("     ON BD031.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("     AND BD031.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("     AND BD031.EXAMNO = BASE.EXAMNO ");
        stb.append("     AND BD031.SEQ = '031' ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_GENERAL_MST GEN03 ");
        stb.append("     ON GEN03.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("     AND GEN03.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("     AND GEN03.TESTDIV = '0' ");
        stb.append("     AND GEN03.GENERAL_DIV = '03' ");
        stb.append("     AND GEN03.GENERAL_CD = RD015.REMARK3 ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_GENERAL_MST GEN02 ");
        stb.append("     ON GEN02.ENTEXAMYEAR = GEN03.ENTEXAMYEAR ");
        stb.append("     AND GEN02.APPLICANTDIV = GEN03.APPLICANTDIV ");
        stb.append("     AND GEN02.TESTDIV = '0' ");
        stb.append("     AND GEN02.GENERAL_DIV = '02' ");
        stb.append("     AND GEN02.GENERAL_CD = GEN03.REMARK1 ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_GENERAL_MST GEN01 ");
        stb.append("     ON GEN01.ENTEXAMYEAR = GEN02.ENTEXAMYEAR ");
        stb.append("     AND GEN01.APPLICANTDIV = GEN02.APPLICANTDIV ");
        stb.append("     AND GEN01.TESTDIV = '0' ");
        stb.append("     AND GEN01.GENERAL_DIV = '01' ");
        stb.append("     AND GEN01.GENERAL_CD = GEN02.REMARK1 ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_GENERAL_MST GEN04 ");
        stb.append("     ON GEN04.ENTEXAMYEAR = RD015.ENTEXAMYEAR ");
        stb.append("     AND GEN04.APPLICANTDIV = RD015.APPLICANTDIV ");
        stb.append("     AND GEN04.TESTDIV = '0' ");
        stb.append("     AND GEN04.GENERAL_DIV = '04' ");
        stb.append("     AND GEN04.GENERAL_CD = RD015.REMARK4 ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_GENERAL_MST GEN05 ");
        stb.append("     ON GEN05.ENTEXAMYEAR = RD015.ENTEXAMYEAR ");
        stb.append("     AND GEN05.APPLICANTDIV = RD015.APPLICANTDIV ");
        stb.append("     AND GEN05.TESTDIV = '0' ");
        stb.append("     AND GEN05.GENERAL_DIV = '05' ");
        stb.append("     AND GEN05.GENERAL_CD = RD015.REMARK5 ");
        stb.append(" LEFT JOIN ENTEXAM_TESTDIV_MST TEST ");
        stb.append("     ON TEST.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("     AND TEST.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("     AND TEST.TESTDIV = BASE.TESTDIV ");
        stb.append(" LEFT JOIN ENTEXAM_TESTDIV_DETAIL_SEQ_MST TESTDT ");
        stb.append("     ON TESTDT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("     AND TESTDT.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("     AND TESTDT.TESTDIV = BASE.TESTDIV ");
        stb.append("     AND TESTDT.SEQ = '001' ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' AND ");
        stb.append("     BASE.APPLICANTDIV = '2' AND ");
        stb.append("     BASE.TESTDIV = '" + _param._testDiv + "' ");

        return stb.toString();
    }

    private String getResult2Sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH BASE AS (SELECT ");
        stb.append("     BASE.FS_CD, ");
        stb.append("     SCHOOL.FINSCHOOL_NAME, ");
        stb.append("     BASE.TESTDIV, ");
        stb.append("     TEST.TESTDIV_NAME, ");
        stb.append("     COUNT((BASE.JUDGEMENT = '1' AND RD015.REMARK4 IS NOT NULL AND RD015.REMARK5 IS NOT NULL) OR NULL) AS SPECIAL, ");
        stb.append("     COUNT(BASE.JUDGEMENT = '1' OR NULL) AS PASS ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append(" LEFT JOIN ");
        stb.append("     FINSCHOOL_MST SCHOOL ");
        stb.append("     ON SCHOOL.FINSCHOOLCD = BASE.FS_CD ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_RECEPT_DETAIL_DAT RD015 ");
        stb.append("     ON RD015.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("     AND RD015.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("     AND RD015.TESTDIV = BASE.TESTDIV ");
        stb.append("     AND RD015.EXAM_TYPE = '1' ");
        stb.append("     AND RD015.RECEPTNO = BASE.EXAMNO ");
        stb.append("     AND RD015.SEQ = '015' ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_APPLICANTBASE_DETAIL_DAT BD031 ");
        stb.append("     ON BD031.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("     AND BD031.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("     AND BD031.EXAMNO = BASE.EXAMNO ");
        stb.append("     AND BD031.SEQ = '031' ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_GENERAL_MST GEN01 ");
        stb.append("     ON GEN01.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("     AND GEN01.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("     AND GEN01.TESTDIV = '0' ");
        stb.append("     AND GEN01.GENERAL_DIV = '01' ");
        stb.append("     AND GEN01.GENERAL_CD = RD015.REMARK3 ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_GENERAL_MST GEN04 ");
        stb.append("     ON GEN04.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("     AND GEN04.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("     AND GEN04.TESTDIV = '0' ");
        stb.append("     AND GEN04.GENERAL_DIV = '04' ");
        stb.append("     AND GEN04.GENERAL_CD = RD015.REMARK4 ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_TESTDIV_MST TEST ");
        stb.append("     ON TEST.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("     AND TEST.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("     AND TEST.TESTDIV = BASE.TESTDIV ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' AND ");
        stb.append("     BASE.APPLICANTDIV = '2' AND ");
        stb.append("     BASE.TESTDIV = '" + _param._testDiv + "' ");
        stb.append(" GROUP BY ");
        stb.append("     BASE.FS_CD, ");
        stb.append("     SCHOOL.FINSCHOOL_NAME, ");
        stb.append("     BASE.TESTDIV, ");
        stb.append("     TEST.TESTDIV_NAME ");
        stb.append(" ORDER BY ");
        stb.append("     BASE.FS_CD, ");
        stb.append("     BASE.TESTDIV ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     BASE.FS_CD, ");
        stb.append("     BASE.FINSCHOOL_NAME, ");
        stb.append("     TDL001.REMARK2, ");
        stb.append("     BASE.TESTDIV, ");
        stb.append("     BASE.TESTDIV_NAME, ");
        stb.append("     CASE WHEN BASE.PASS > 0 THEN '1' ELSE '0' END AS SETSUBI, ");
        stb.append("     CASE WHEN BASE.SPECIAL > 0 THEN '1' ELSE '0' END AS SPECIAL, ");
        stb.append("     BASE.PASS, ");
        stb.append("     TDL003.REMARK1 AS SCHEDULE1_1, ");
        stb.append("     TDL003.REMARK2 AS SCHEDULE1_2, ");
        stb.append("     TDL003.REMARK3 AS SCHEDULE1_3, ");
        stb.append("     TDL003.REMARK4 AS SCHEDULE1_4, ");
        stb.append("     TDL003.REMARK5 AS SCHEDULE1_5, ");
        stb.append("     TDL003.REMARK6 AS SCHEDULE1_6, ");
        stb.append("     TDL004.REMARK1 AS SCHEDULE2_1, ");
        stb.append("     TDL004.REMARK2 AS SCHEDULE2_2, ");
        stb.append("     TDL004.REMARK3 AS SCHEDULE2_3, ");
        stb.append("     TDL004.REMARK4 AS SCHEDULE2_4, ");
        stb.append("     TDL004.REMARK5 AS SCHEDULE2_5, ");
        stb.append("     TDL004.REMARK6 AS SCHEDULE2_6, ");
        stb.append("     TDL005.REMARK1 AS SCHEDULE3_1, ");
        stb.append("     TDL005.REMARK2 AS SCHEDULE3_2, ");
        stb.append("     TDL005.REMARK3 AS SCHEDULE3_3, ");
        stb.append("     TDL005.REMARK4 AS SCHEDULE3_4, ");
        stb.append("     TDL005.REMARK5 AS SCHEDULE3_5, ");
        stb.append("     TDL005.REMARK6 AS SCHEDULE3_6, ");
        stb.append("     TDL006.REMARK1 AS SCHEDULE4_1, ");
        stb.append("     TDL006.REMARK2 AS SCHEDULE4_2, ");
        stb.append("     TDL006.REMARK3 AS SCHEDULE4_3, ");
        stb.append("     TDL006.REMARK4 AS SCHEDULE4_4, ");
        stb.append("     TDL006.REMARK5 AS SCHEDULE4_5, ");
        stb.append("     TDL006.REMARK6 AS SCHEDULE4_6 ");
        stb.append(" FROM ");
        stb.append("     BASE ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_TESTDIV_DETAIL_SEQ_MST TDL001 ");
        stb.append("     ON TDL001.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND TDL001.APPLICANTDIV = '2' ");
        stb.append("     AND TDL001.TESTDIV = BASE.TESTDIV ");
        stb.append("     AND TDL001.SEQ = '001' ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_TESTDIV_DETAIL_SEQ_MST TDL003 ");
        stb.append("     ON TDL003.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND TDL003.APPLICANTDIV = '2' ");
        stb.append("     AND TDL003.TESTDIV = BASE.TESTDIV ");
        stb.append("     AND TDL003.SEQ = '003' ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_TESTDIV_DETAIL_SEQ_MST TDL004 ");
        stb.append("     ON TDL004.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND TDL004.APPLICANTDIV = '2' ");
        stb.append("     AND TDL004.TESTDIV = BASE.TESTDIV ");
        stb.append("     AND TDL004.SEQ = '004' ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_TESTDIV_DETAIL_SEQ_MST TDL005 ");
        stb.append("     ON TDL005.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND TDL005.APPLICANTDIV = '2' ");
        stb.append("     AND TDL005.TESTDIV = BASE.TESTDIV ");
        stb.append("     AND TDL005.SEQ = '005' ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_TESTDIV_DETAIL_SEQ_MST TDL006 ");
        stb.append("     ON TDL006.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND TDL006.APPLICANTDIV = '2' ");
        stb.append("     AND TDL006.TESTDIV = BASE.TESTDIV ");
        stb.append("     AND TDL006.SEQ = '006' ");
        stb.append(" WHERE ");
        stb.append("     BASE.PASS > 0 "); //合格者のいる学校のみ出力

        return stb.toString();

    }

    private String getFurikomiSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     BASE.TESTDIV, ");
        stb.append("     BASE.EXAMNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.NAME_KANA, ");
        stb.append("     TDL001.REMARK4, ");
        stb.append("     TDL001.REMARK5, ");
        stb.append("     TDL001.REMARK7, ");
        stb.append("     TDL001.REMARK8, ");
        stb.append("     SCHOOL.FINSCHOOL_NAME, ");
        stb.append("     CASE WHEN RD015.REMARK4 IS NULL THEN TDL001.REMARK3 ELSE GEN04.REMARK1 END AS NYUGAKU, ");
        stb.append("     CASE WHEN RD015.REMARK4 IS NULL THEN TDL001.REMARK6 ELSE GEN04.REMARK2 END AS SETSUBI ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_TESTDIV_DETAIL_SEQ_MST TDL001 ");
        stb.append("     ON TDL001.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("     AND TDL001.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("     AND TDL001.TESTDIV = BASE.TESTDIV ");
        stb.append("     AND TDL001.SEQ = '001' ");
        stb.append(" LEFT JOIN ");
        stb.append("     FINSCHOOL_MST SCHOOL ");
        stb.append("     ON SCHOOL.FINSCHOOLCD = BASE.FS_CD ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_RECEPT_DAT RPT ");
        stb.append("     ON RPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("     AND RPT.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("     AND RPT.TESTDIV = BASE.TESTDIV ");
        stb.append("     AND RPT.EXAMNO = BASE.EXAMNO ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_RECEPT_DETAIL_DAT RD015 ");
        stb.append("     ON RD015.ENTEXAMYEAR = RPT.ENTEXAMYEAR ");
        stb.append("     AND RD015.APPLICANTDIV = RPT.APPLICANTDIV ");
        stb.append("     AND RD015.TESTDIV = RPT.TESTDIV ");
        stb.append("     AND RD015.RECEPTNO = RPT.RECEPTNO ");
        stb.append("     AND RD015.SEQ = '015' ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_GENERAL_MST GEN04 ");
        stb.append("     ON GEN04.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("     AND GEN04.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("     AND GEN04.TESTDIV = '0' ");
        stb.append("     AND GEN04.GENERAL_DIV = '04' ");
        stb.append("     AND GEN04.GENERAL_CD = RD015.REMARK4 ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' AND ");
        stb.append("     BASE.APPLICANTDIV = '2' AND ");
        stb.append("     RPT.JUDGEDIV = '1' AND ");
        stb.append("     BASE.TESTDIV = '" + _param._testDiv + "' ");
        if(!"ALL".equals(_param._gakka)) {
            stb.append("   AND BASE.TESTDIV0 = '" + _param._gakka + "' ");
        }
        if(!"".equals(_param._s_examNo) && !"".equals(_param._e_examNo)) {
            stb.append("   AND BASE.EXAMNO BETWEEN " + _param._s_examNo + " AND " + _param._e_examNo );
        } else if(!"".equals(_param._s_examNo)) {
            stb.append("   AND BASE.EXAMNO >= '" + _param._s_examNo + "' ");
        } else if(!"".equals(_param._e_examNo)) {
            stb.append("   AND BASE.EXAMNO <= '" + _param._e_examNo + "' ");
        }
        stb.append(" ORDER BY ");
        if("1".equals(_param._sort)) {
            stb.append("     BASE.EXAMNO, ");
            stb.append("     BASE.NAME_KANA ");
        } else {
            stb.append("     BASE.NAME_KANA, ");
            stb.append("     BASE.EXAMNO ");
        }


        return stb.toString();
    }


    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private class PrintData1 {
        final String _no;
        final String _testdiv_Name;
        final String _gakka;
        final String _examno;
        final String _passcourse;
        final String _name;
        final String _name_Kana;
        final String _sex;
        final String _fs_Cd;
        final String _finschool_Name_Abbv;
        final String _honorDiv;
        final String _spName;
        final String _spReasonRemark;
        final String _ryo;
        final String _gname;
        final String _zipcd;
        final String _address1;
        final String _address2;
        final String _telno;
        final String _duplicateFlg;

        PrintData1(final String no, final String testdiv_Name, final String gakka, final String examno,
                final String passcourse, final String name, final String name_Kana, final String sex,
                final String fs_Cd, final String finschool_Name, final String honorDiv, final String spName,
                final String spReasonRemark, final String ryo, final String gname, final String zipcd,
                final String address1, final String address2, final String telno, final String duplicateFlg) {
            _no = no;
            _testdiv_Name = testdiv_Name;
            _gakka = gakka;
            _examno = examno;
            _passcourse = passcourse;
            _name = name;
            _name_Kana = name_Kana;
            _sex = sex;
            _fs_Cd = fs_Cd;
            _finschool_Name_Abbv = finschool_Name;
            _honorDiv = honorDiv;
            _spName = spName;
            _spReasonRemark = spReasonRemark;
            _ryo = ryo;
            _gname = gname;
            _zipcd = zipcd;
            _address1 = address1;
            _address2 = address2;
            _telno = telno;
            _duplicateFlg = duplicateFlg;
        }
    }

    private class PrintData2 {
        final String _no;
        final String _testdiv_Name;
        final String _gakka;
        final String _examno;
        final String _aspiring1;
        final String _aspiring2;
        final String _aspiring3;
        final String _aspiring4;
        final String _name;
        final String _name_Kana;
        final String _sex;
        final String _fs_Cd;
        final String _finschool_Name_Abbv;
        final String _honorDiv;
        final String _general_Mark;
        final String _general_Name;
        final String _ryo;
        final String _gname;
        final String _hantei;
        final String _duplicateFlg;

        PrintData2(final String no, final String testdiv_Name, final String gakka, final String examno,
                final String aspiring1, final String aspiring2, final String aspiring3, final String aspiring4,
                final String name, final String name_Kana, final String sex, final String fs_Cd,
                final String finschool_Name, final String honorDiv, final String general_Mark,
                final String general_Name, final String ryo, final String gname, final String hantei, final String duplicateFlg) {
            _no = no;
            _testdiv_Name = testdiv_Name;
            _gakka = gakka;
            _examno = examno;
            _aspiring1 = aspiring1;
            _aspiring2 = aspiring2;
            _aspiring3 = aspiring3;
            _aspiring4 = aspiring4;
            _name = name;
            _name_Kana = name_Kana;
            _sex = sex;
            _fs_Cd = fs_Cd;
            _finschool_Name_Abbv = finschool_Name;
            _honorDiv = honorDiv;
            _general_Mark = general_Mark;
            _general_Name = general_Name;
            _ryo = ryo;
            _gname = gname;
            _hantei = hantei;
            _duplicateFlg = duplicateFlg;
        }
    }

    private class PrintData3 {
        final String _examno;
        final String _name;
        final String _finschool_Name;
        final String _passCourse;
        final String _gakka;

        PrintData3(final String examno, final String name, final String finschool_Name, final String passCourse, final String gakka) {
            _examno = examno;
            _name = name;
            _finschool_Name = finschool_Name;
            _passCourse = passCourse;
            _gakka = gakka;
        }
    }

    private class PrintData4 {
        final String _examhallCd;
        final String _examhallName;
        final String _subclassCd;
        final String _subclassName;
        final String _no;
        final String _examno;
        final String _s_receptno;
        final String _e_receptno;

        PrintData4(final String examhallCd, final String examhallName, final String subclassCd,
                final String subclassName, final String no, final String examno, final String s_receptno,
                final String e_receptno) {
            _examhallCd = examhallCd;
            _examhallName = examhallName;
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _no = no;
            _examno = examno;
            _s_receptno = s_receptno;
            _e_receptno = e_receptno;
        }
    }

    private class PrintData5 {
        final String _shigansya;
        final String _jukensya;
        final String _pass;
        final String _nyugakukin;;
        final String _setsubihi;
        final String _ent;

        PrintData5(final String shigansya, final String jukensya, final String pass, final String nyugakukin,
                final String setsubihi, final String ent) {
            _shigansya = shigansya;
            _jukensya = jukensya;
            _pass = pass;
            _nyugakukin = nyugakukin;
            _setsubihi = setsubihi;
            _ent = ent;
        }
    }

    private class PrintData6 {
        final String _testdiv;
        final String _testdiv_Name;
        final String _fs_Cd;
        final String _finschool_Name;
        final String _jukensya;
        final String _kesseki;
        final String _pass;

        PrintData6(final String testdiv, final String testdiv_Name, final String fs_Cd, final String finschool_Name,
                final String jukensya, final String kesseki, final String pass) {
            _testdiv = testdiv;
            _testdiv_Name = testdiv_Name;
            _fs_Cd = fs_Cd;
            _finschool_Name = finschool_Name;
            _jukensya = jukensya;
            _kesseki = kesseki;
            _pass = pass;

        }
    }

    private class PrintData7 {
        final String _fs_Cd;
        final String _finschool_Name;
        final String _remark2;
        final String _testdiv;
        final String _testdiv_Name;
        final String _princname;
        final String _no;
        final String _examno;
        final String _name;
        final String _judgement;
        final String _shibou;
        final String _gouhi;
        final String _course;
        final String _special;

        PrintData7(final String fs_Cd, final String finschool_Name, final String remark2, final String testdiv, final String testdiv_Name,
                final String princname, final String no, final String examno, final String name, final String judgement,
                final String shibou, final String gouhi, final String course, final String special) {
            _fs_Cd = fs_Cd;
            _finschool_Name = finschool_Name;
            _remark2 = remark2;
            _testdiv = testdiv;
            _testdiv_Name = testdiv_Name;
            _princname = princname;
            _no = no;
            _examno = examno;
            _name = name;
            _judgement = judgement;
            _shibou = shibou;
            _gouhi = gouhi;
            _course = course;
            _special = special;
        }
    }

    private class PrintData8 {
        final String _fs_Cd;
        final String _finschool_Name;
        final String _remark2;
        final String _testdiv;
        final String _testdiv_Name;
        final String _setsubi;
        final String _special;
        final String _pass;
        final Map _scheduleMap;

        PrintData8(final String fs_Cd, final String finschool_Name, final String remark2, final String testdiv,
                final String testdiv_Name, final String setsubi, final String special,
                final String pass, final Map scheduleMap) {
            _fs_Cd = fs_Cd;
            _finschool_Name = finschool_Name;
            _remark2 = remark2;
            _testdiv = testdiv;
            _testdiv_Name = testdiv_Name;
            _setsubi = setsubi;
            _special = special;
            _pass = pass;
            _scheduleMap = scheduleMap;
        }
    }

    private class PrintData9 {
        final String _testdiv;
        final String _examno;
        final String _name;
        final String _name_Kana;
        final String _remark4;
        final String _remark5;
        final String _remark7;
        final String _remark8;
        final String _finschool_Name;
        final String _nyugaku;
        final String _setsubi;

        PrintData9(final String testdiv, final String examno, final String name, final String name_Kana,
                final String remark4, final String remark5, final String remark7, final String remark8,
                final String finschool_Name, final String nyugaku, final String setsubi) {
            _testdiv = testdiv;
            _examno = examno;
            _name = name;
            _name_Kana = name_Kana;
            _remark4 = remark4;
            _remark5 = remark5;
            _remark7 = remark7;
            _remark8 = remark8;
            _finschool_Name = finschool_Name;
            _nyugaku = nyugaku;
            _setsubi = setsubi;
        }
    }

    /** パラメータクラス */
    private static class Param {
        private final String _entexamyear;
        private final String _logindate;
        private final String _loginYear;
        private final String _testDiv;
        private final String _sex;
        private final String _order;
        private final String _sort;
        private final String _output;
        private final String _gakka;
        private final String _course;
        private final String _ruibetsu;
        private final String _testDivName;
        private final String _s_examNo;
        private final String _e_examNo;
        private final String _exam;
        private final String _schoolName;

        final Map _schoolBankMst;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _logindate = request.getParameter("LOGIN_DATE");
            _loginYear = request.getParameter("LOGIN_YEAR");
            _testDiv = request.getParameter("TESTDIV");
            _sex = request.getParameter("SEX");
            _order = request.getParameter("ORDER");
            _sort = request.getParameter("SORT");
            _output = request.getParameter("OUTPUT");
            _gakka = request.getParameter("GAKKA");
            _exam = request.getParameter("EXAM");
            _course = request.getParameter("COURSE");
            _ruibetsu = request.getParameter("RUIBETSU");
            _testDivName = getTestDivName(db2);
            _s_examNo = StringUtils.defaultString(request.getParameter("S_EXAMNO"), "");
            _e_examNo = StringUtils.defaultString(request.getParameter("E_EXAMNO"), "");
            _schoolName = getSchoolName(db2);
            _schoolBankMst = getSchoolBankMst(db2);
        }

        private String getTestDivName(final DB2UDB db2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            String sql = " SELECT TESTDIV_NAME FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR='" + _entexamyear + "' AND APPLICANTDIV = '2' AND TESTDIV = '" + _testDiv + "' ";
            log.debug(" sql =" + sql);

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("TESTDIV_NAME");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private String getSchoolName(final DB2UDB db2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            String sql = " SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + _loginYear + "' AND SCHOOLCD = '000000000000' AND SCHOOL_KIND = 'H' ";
            log.debug(" sql =" + sql);

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("SCHOOLNAME1");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }

        private Map getSchoolBankMst(final DB2UDB db2) {
            final String cd = "0001";
            Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT NMG203.NAME1 AS DEPOSIT_ITEM_NAME, T1.* "
                                 + " FROM SCHOOL_BANK_MST T1 "
                                 + " LEFT JOIN NAME_MST NMG203 ON NMG203.NAMECD1 = 'G203' AND NMG203.NAMECD2 = T1.DEPOSIT_ITEM "
                                 + " WHERE BANKTRANSFERCD = '" + cd + "' ";

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                final ResultSetMetaData meta = rs.getMetaData();
                if (rs.next()) {
                    for (int i = 1; i < meta.getColumnCount(); i++) {
                        rtn.put(meta.getColumnLabel(i), rs.getString(meta.getColumnLabel(i)));
                    }
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }


    }
}

// eof

