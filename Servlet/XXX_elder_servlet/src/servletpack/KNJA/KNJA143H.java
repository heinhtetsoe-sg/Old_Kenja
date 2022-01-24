// kanji=漢字
/*
 * $Id: 281c36e08e2942a72630b2a4bcfd6faa017f26af $
 *
 * 作成日: 2005/03/25 16:07:30 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2005-2008 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 *
 *  学校教育システム 賢者 [学籍管理]
 *
 *                  ＜ＫＮＪＡ１４３H＞  生徒・職員証明書（三重県）
 **/

public class KNJA143H {

    private static final Log log = LogFactory.getLog(KNJA143H.class);

    private boolean nonedata = false;                               //該当データなしフラグ


    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response)
                     throws ServletException, IOException
    {
        final Vrw32alp svf = new Vrw32alp();            //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス

        PrintWriter outstrm = null;
        try {
            //  print設定
            response.setContentType("application/pdf");
            outstrm = new PrintWriter (response.getOutputStream());

            //  svf設定
            svf.VrInit();                               //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream());       //PDFファイル名の設定

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            final Param param = getParam(db2, request);

            if ("A143H_8".equals(param._useFormNameA143H)) {
                //広島用ルートとする。
                printSvfMainHirokoudai(db2, svf, param);
            } else if ("A143H_7".equals(param._useFormNameA143H) || param._fukuiLabelFlg) {
                //広島・福井共通ルート→福井用ルートとする。
                printSvfMainfukui(db2, svf, param);
            } else if ("A143H_6".equals(param._useFormNameA143H)) {
                printSvfMainKasiwara(db2, svf, param);
            } else {
                if ("1".equals(param._taishousha)) {
                     if ("A143H_5".equals(param._useFormNameA143H)) {
                        printSvfMainStudentKeiai(db2, svf, param);
                    } else if("KNJA143H_10".equals(param._useFormNameA143H)) {//流経柏
                    	printSvfMainStudentRyukei(db2, svf, param);
                    }else {
                        printSvfMainStudent(db2, svf, param);
                    }
                } else {
                	final boolean seireki = "A143H_5".equals(param._useFormNameA143H);
                    printSvfMainStaff(db2, svf, param, seireki);
                }
            }

        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            //  該当データ無し
            if (!nonedata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            //  終了処理
            svf.VrQuit();
            db2.commit();
            db2.close();                //DBを閉じる
            outstrm.close();            //ストリームを閉じる
        }

    }//doGetの括り

    private static int getMS932ByteLength(final String s) {
    	return KNJ_EditEdit.getMS932ByteLength(s);
    }

    public static String getString(final Map row, String field) {
        if (null == row || row.isEmpty()) {
            return null;
        }
        field = field.toUpperCase();
        if (!row.containsKey(field)) {
            throw new IllegalStateException("no such field : " + field + " / " + row);
        }
        return (String) row.get(field);
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

    private void setForm(final Vrw32alp svf, final Param param, final String form, final int data) {
    	svf.VrSetForm(form, data);
    	if (param._isOutputDebug) {
    		log.info(" form = " + form);
    	}
    }

    /** 帳票出力 **/
    private void printSvfMainStudent(
        final DB2UDB db2,
        final Vrw32alp svf,
        final Param param
    ) {
        final int maxLine = 5;

        final int remark1len = getMS932ByteLength(param._addr1);
        final String schoolAddrField = remark1len <= 40 ? "" : remark1len <= 46 ? "_2" : remark1len <= 50 ? "_3" : "_4";

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql(param);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            String schoolstampPath = null;
            if ("1".equals(param._printStamp)) {
            	final File schoolstampFile = param.getImageFile("SCHOOLSTAMP_H.bmp");
                if (null != schoolstampFile) {
                	schoolstampPath = schoolstampFile.getAbsolutePath();
                }
                log.info(" schoolstampPath = " + schoolstampPath);
            }
            final String form = param._isFukuiken ? "KNJA143H_N.frm" : "A143H_9".equals(param._useFormNameA143H) ? "KNJA143H_9.frm" : "A143H_10".equals(param._useFormNameA143H) ? "KNJA143H_10.frm" : "KNJA143H.frm";

            setForm(svf, param, form, 1);
            int line = 1;

            while (rs.next()) {

                if (line > maxLine) {
                    svf.VrEndPage();
                    line = 1;

                    setForm(svf, param, "KNJA143H_2.frm", 1);
                    svf.VrsOut("DUMMY", "　");
                    svf.VrEndPage();
                    setForm(svf, param, form, 1);
                }

                final String schregno = rs.getString("SCHREGNO");
                final String name = "1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME") : rs.getString("NAME");
                final String birthday = rs.getString("BIRTHDAY");
                final String majorname = StringUtils.defaultString(rs.getString("MAJORNAME"));
                final String hrname = StringUtils.defaultString(rs.getString("HR_NAME"));
                final String attendno2 = null == rs.getString("ATTENDNO") ? "" : rs.getString("ATTENDNO").length() <= 2 ? rs.getString("ATTENDNO") : rs.getString("ATTENDNO").substring(rs.getString("ATTENDNO").length() - 2);
                final String addr1 = rs.getString("ADDR1");
                final String addr2 = rs.getString("ADDR2");

                final File schregimg = param.getImageFile("P" + schregno + "." + param._extension); //写真データ存在チェック用
                if (null != schregimg) {
                    svf.VrsOutn("PHOTO_BMP", line, schregimg.getPath());
                }

                svf.VrsOutn("TITLE", line, "生徒証明書");
                svf.VrsOutn("SCHREGNO", line, schregno);
                svf.VrsOutn("SENTENCE", line, "生徒");

                final String syozokuSuffix = "A143H_10".equals(param._useFormNameA143H) ? "" : "(" + majorname + ")";
                final String syozoku = hrname + "　" + attendno2 + "番" + syozokuSuffix;

                final int syozokuLen = getMS932ByteLength(syozoku);
                svf.VrsOutn("ENT_SCHOOL" + (syozokuLen <= 32 ? "" : syozokuLen <= 36 ? "2" : "3"), line, syozoku);

                final int nameLen = getMS932ByteLength(name);
                svf.VrsOutn("NAME" + (nameLen <= 20 ? "" : nameLen <= 30 ? "2" : "3"), line, name);
                if (null != birthday) {
                    final String[] birthArray = KNJ_EditDate.tate_format(KNJ_EditDate.h_format_JP(db2, birthday));
                    svf.VrsOutn("BIRTHDAY1", line, birthArray[0]);
                    svf.VrsOutn("BIRTHDAY2", line, birthArray[1]);
                    svf.VrsOutn("BIRTHDAY3", line, birthArray[2]);
                    svf.VrsOutn("BIRTHDAY4", line, birthArray[3]);
                }

                final String setAddr = addr1 + addr2;
                final int addrLen = getMS932ByteLength(setAddr);
                svf.VrsOutn("ADDRESS1" + (addrLen <= 30 ? "" : addrLen <= 40 ? "_2" : addrLen <= 50 ? "_3" : "_4"), line, setAddr);

                if (null != param._termSdate) {
                    final String[] sdateArray = KNJ_EditDate.tate_format(KNJ_EditDate.h_format_JP(db2, param._termSdate));
                    svf.VrsOutn("SDATE1", line, sdateArray[0]);
                    svf.VrsOutn("SDATE2", line, sdateArray[1]);
                    svf.VrsOutn("SDATE3", line, sdateArray[2]);
                    svf.VrsOutn("SDATE4", line, sdateArray[3]);
                }

                svf.VrsOutn("SCHOOLADDRESS1" + schoolAddrField, line, param._addr1);
                svf.VrsOutn("SCHOOLNAME1", line, param._schoolname + param._principalName);

                if (null != param._termEdate) {
                    svf.VrsOutn("LIMIT", line, KNJ_EditDate.h_format_JP(db2, param._termEdate) + "まで有効");
                }

                if ("1".equals(param._printStamp) && null != schoolstampPath) {
                	svf.VrsOutn("SCHOOLSTAMP", line, schoolstampPath);
                }

                String gesyaALL = "";
                gesyaALL = getGesya(gesyaALL, rs.getString("GESYA_7"));
                gesyaALL = getGesya(gesyaALL, rs.getString("GESYA_6"));
                gesyaALL = getGesya(gesyaALL, rs.getString("GESYA_5"));
                gesyaALL = getGesya(gesyaALL, rs.getString("GESYA_4"));
                gesyaALL = getGesya(gesyaALL, rs.getString("GESYA_3"));
                gesyaALL = getGesya(gesyaALL, rs.getString("GESYA_2"));
                gesyaALL = getGesya(gesyaALL, rs.getString("GESYA_1"));
                gesyaALL = getGesya(gesyaALL, rs.getString("JOSYA_1"));
                gesyaALL += gesyaALL + '・' + (null == rs.getString("JOSYA_1") ? "" : rs.getString("JOSYA_1"));
                gesyaALL = "・".equals(gesyaALL) ? "" : gesyaALL;
                final int gesyaKeta = getMS932ByteLength(gesyaALL);
                svf.VrsOutn("SECTION" + (gesyaKeta <= 14 ? "1" : gesyaKeta <= 20 ? "2" : "3"), line, gesyaALL);

                line++;
                nonedata = true;
            }

            if (nonedata) {
                svf.VrEndPage();
                setForm(svf, param, "KNJA143H_2.frm", 1);
                svf.VrsOut("DUMMY", "　");
                svf.VrEndPage();
            }

        } catch (Exception ex) {
            log.error("setSvfout set error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        if (nonedata) {
            svf.VrEndPage();
        }
    }

    /** 帳票出力 流経柏 **/
    private void printSvfMainStudentRyukei(
        final DB2UDB db2,
        final Vrw32alp svf,
        final Param param
    ) {
        final int maxLine = 5;

        final int remark1len = getMS932ByteLength(param._addr1);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql(param);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            final String form = "KNJA143H_10.frm";
            setForm(svf, param, form, 1);
            int line = 1;
            int no = 1;

            while (rs.next()) {

                if (line > maxLine) {
                    svf.VrEndPage();
                    line  = 1;
                }

                final String schregno = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");
                final String age = rs.getString("AGE");
                final String birthday = rs.getString("BIRTHDAY");
                final String hrname = StringUtils.defaultString(rs.getString("HR_NAME"));
                final String grade = rs.getString("GRADE_CD");
                final String hrclass = rs.getString("HR_CLASS");
                final String attendno2 = null == rs.getString("ATTENDNO") ? "" : rs.getString("ATTENDNO");
                final String addr1 = rs.getString("ADDR1");
                final String addr2 = rs.getString("ADDR2");

                svf.VrsOutn("SCHREGNO" + (no % 2 == 1 ? "1" : "2"), line, schregno);
                svf.VrsOutn("SENTENCE"+ (no % 2 == 1 ? "1" : "2"), line, "生徒");

                Pattern p = Pattern.compile("^0+([0-9]+.*)");
                Matcher m = p.matcher(grade);
                String printgrade = null;
                if (m.matches()) {
                	printgrade = m.group(1);
                }
                m = p.matcher(hrclass);
                String printhrclass = null;
                if (m.matches()) {
                	printhrclass = m.group(1);
                }
                m = p.matcher(attendno2);
                String printattendo= null;
                if (m.matches()) {
                	printattendo = m.group(1);
                }


                final String syozoku = printgrade + "年 " + printhrclass + "組 " + printattendo + "番";
                svf.VrsOutn("ENT_SCHOOL"+ (no % 2 == 1 ? "1" : "2"), line, syozoku);
                svf.VrsOutn("NAME"+ (no % 2 == 1 ? "1" : "2"), line, name);
                svf.VrsOutn("AGE" + (no % 2 == 1 ? "1" : "2"), line, "(" + age + "歳)");
                if (null != birthday) {
                	final String[] birthArray = StringUtils.split(birthday, "-");
                    svf.VrsOutn("BIRTHDAY" + (no % 2 == 1 ? "1" : "2") + "_2", line, birthArray[0]);
                    svf.VrsOutn("BIRTHDAY" + (no % 2 == 1 ? "1" : "2") + "_3", line, birthArray[1]);
                    svf.VrsOutn("BIRTHDAY" + (no % 2 == 1 ? "1" : "2") + "_4", line, birthArray[2]);
                }

                final String setAddr = addr1 + addr2;
                svf.VrsOutn("ADDRESS" + (no % 2 == 1 ? "1" : "2") + "_1", line, addr1);
                svf.VrsOutn("ADDRESS" + (no % 2 == 1 ? "1" : "2") + "_2", line, addr2);

                //発行日 固定文言
                svf.VrsOutn("SDATE" + (no % 2 == 1 ? "1" : "2") + "_2", line, param._year);
                svf.VrsOutn("SDATE" + (no % 2 == 1 ? "1" : "2") + "_3", line, "4");
                svf.VrsOutn("SDATE" + (no % 2 == 1 ? "1" : "2") + "_4", line, "1");

                svf.VrsOutn("SCHOOLADDRESS" + (no % 2 == 1 ? "1" : "2") + "_1", line, param._addr1);
                svf.VrsOutn("SCHOOLNAME" + (no % 2 == 1 ? "1" : "2"), line, param._schoolname);
                svf.VrsOutn("PRINCIPAL_NAME" + (no % 2 == 1 ? "1" : "2"), line, param._principalName);


                if(no % 2 == 0) {
                	line++;
                }
                no++;

                nonedata = true;
            }

        } catch (Exception ex) {
            log.error("setSvfout set error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        if (nonedata) {
            svf.VrEndPage();
        }
    }

    /** 帳票出力（敬愛用） **/
    private void printSvfMainStudentKeiai(
        final DB2UDB db2,
        final Vrw32alp svf,
        final Param param
    ) {
        final int maxLine = 2;

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql(param);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            setForm(svf, param, "KNJA143H_5.frm", 1);
            int colNo = 1;
            int line  = 1;

            while (rs.next()) {

                if (line > maxLine) {
                    svf.VrEndPage();
                    colNo = 1;
                    line  = 1;
                }

                final String schregno  = rs.getString("SCHREGNO");
                final String name      = "1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME") : rs.getString("NAME");
                final String birthday  = rs.getString("BIRTHDAY");
                final String age       = rs.getString("AGE");
                final String grade     = StringUtils.defaultString(rs.getString("GRADE"));
                final String hrClass   = StringUtils.defaultString(rs.getString("HR_NAMEABBV"));
                final String attendno2 = null == rs.getString("ATTENDNO") ? "" : rs.getString("ATTENDNO").length() <= 2 ? rs.getString("ATTENDNO") : rs.getString("ATTENDNO").substring(rs.getString("ATTENDNO").length() - 2);
                final String addr1     = rs.getString("ADDR1");
                final String addr2     = rs.getString("ADDR2");

                svf.VrsOutn("SCHREGNO" + colNo, line, schregno);
                svf.VrsOutn("GRADE" + colNo, line, grade);
                svf.VrsOutn("CLASS" + colNo, line, hrClass);
                svf.VrsOutn("NO" + colNo, line, attendno2);

                final int nameLen = getMS932ByteLength(name);
                svf.VrsOutn("NAME" + colNo + (nameLen <= 20 ? "_1" : nameLen <= 30 ? "_2" : "_3"), line, name);
                svf.VrsOutn("AGE" + colNo, line, age);

                if (null != birthday) {
//                    final String[] birthArray = KNJ_EditDate.tate_format(KNJ_EditDate.h_format_JP(db2, birthday));
//                    svf.VrsOutn("BIRTHDAY" + colNo + "_1", line, birthArray[0]);
//                    svf.VrsOutn("BIRTHDAY" + colNo + "_2", line, birthArray[1]);
//                    svf.VrsOutn("BIRTHDAY" + colNo + "_3", line, birthArray[2]);
//                    svf.VrsOutn("BIRTHDAY" + colNo + "_4", line, birthArray[3]);

                    final String[] birthArray = StringUtils.split(birthday, "-");
                    svf.VrsOutn("BIRTHDAY" + colNo + "_2", line, String.valueOf(Integer.parseInt(birthArray[0])));
                    svf.VrsOutn("BIRTHDAY" + colNo + "_3", line, String.valueOf(Integer.parseInt(birthArray[1])));
                    svf.VrsOutn("BIRTHDAY" + colNo + "_4", line, String.valueOf(Integer.parseInt(birthArray[2])));
                }

                final String setAddr = addr1 + addr2;
                final int addrLen = getMS932ByteLength(setAddr);
                svf.VrsOutn("ADDRESS" + colNo + (addrLen <= 30 ? "" : addrLen <= 40 ? "_2" : addrLen <= 50 ? "_3" : "_4"), line, setAddr);

                if (null != param._termSdate) {
//                    final String[] sdateArray = KNJ_EditDate.tate_format(KNJ_EditDate.h_format_JP(db2, param._termSdate));
//                    svf.VrsOutn("SDATE" + colNo + "_1", line, sdateArray[0]);
//                    svf.VrsOutn("SDATE" + colNo + "_2", line, sdateArray[1]);
//                    svf.VrsOutn("SDATE" + colNo + "_3", line, sdateArray[2]);
//                    svf.VrsOutn("SDATE" + colNo + "_4", line, sdateArray[3]);

                    final String[] sdateArray = StringUtils.split(param._termSdate, "-");
                    svf.VrsOutn("SDATE" + colNo + "_2", line, String.valueOf(Integer.parseInt(sdateArray[0])));
                    svf.VrsOutn("SDATE" + colNo + "_3", line, String.valueOf(Integer.parseInt(sdateArray[1])));
                    svf.VrsOutn("SDATE" + colNo + "_4", line, String.valueOf(Integer.parseInt(sdateArray[2])));
                }

                if (null != param._termEdate) {
//                    final String[] edateArray = KNJ_EditDate.tate_format(KNJ_EditDate.h_format_JP(db2, param._termEdate));
//                    svf.VrsOutn("LDATE" + colNo + "_1", line, edateArray[0]);
//                    svf.VrsOutn("LDATE" + colNo + "_2", line, edateArray[1]);
//                    svf.VrsOutn("LDATE" + colNo + "_3", line, edateArray[2]);
//                    svf.VrsOutn("LDATE" + colNo + "_4", line, edateArray[3]);

                    final String[] edateArray = StringUtils.split(param._termEdate, "-");
                    svf.VrsOutn("LDATE" + colNo + "_2", line, String.valueOf(Integer.parseInt(edateArray[0])));
                    svf.VrsOutn("LDATE" + colNo + "_3", line, String.valueOf(Integer.parseInt(edateArray[1])));
                    svf.VrsOutn("LDATE" + colNo + "_4", line, String.valueOf(Integer.parseInt(edateArray[2])));
                }

                svf.VrsOutn("STAFFNAME" + colNo, line, param._principalName);

                if (colNo == 2) {
                    line++;
                }
                colNo = (colNo == 1) ? 2: 1;
                nonedata = true;
            }

            if (nonedata) {
                svf.VrEndPage();
            }

        } catch (Exception ex) {
            log.error("setSvfout set error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        if (nonedata) {
            svf.VrEndPage();
        }
    }

    /** 帳票出力（柏原用） **/
    private void printSvfMainKasiwara(
        final DB2UDB db2,
        final Vrw32alp svf,
        final Param param
    ) {

    	if ("1".equals(param._taishousha)) {

            final String sql = sql(param);
            final List dataList = KnjDbUtils.query(db2, sql);

            final int maxLine = 4;
            final List pageList = getPageList(dataList, maxLine);

            for (int pi = 0; pi < pageList.size(); pi++) {
            	final List studentList = (List) pageList.get(pi);

            	setForm(svf, param, "KNJA143H_6_1.frm", 1);

            	for (int sti = 0; sti < studentList.size(); sti++) {
            		final Map row = (Map) studentList.get(sti);
            		final int line = sti + 1;

                    final String schregno  = KnjDbUtils.getString(row, "SCHREGNO");
                    final String name      = "1".equals(KnjDbUtils.getString(row, "USE_REAL_NAME")) ? KnjDbUtils.getString(row, "REAL_NAME") : KnjDbUtils.getString(row, "NAME");
                    final String birthday  = KnjDbUtils.getString(row, "BIRTHDAY");
                    final String rsGrade   = KnjDbUtils.getString(row, "GRADE");
					final String grade     = NumberUtils.isDigits(rsGrade) ? String.valueOf(Integer.parseInt(rsGrade)) : StringUtils.defaultString(rsGrade);
                    final String hrClass   = StringUtils.defaultString(KnjDbUtils.getString(row, "HR_CLASS_NAME1"));
                    final String addr1     = KnjDbUtils.getString(row, "ADDR1");
                    final String addr2     = KnjDbUtils.getString(row, "ADDR2");

                    svf.VrsOutn("SCHREGNO1", line, schregno);
                    svf.VrsOutn("GRADE1", line, grade);
                    svf.VrsOutn("CLASS1", line, hrClass);

                    final int nameLen = getMS932ByteLength(name);
                    svf.VrsOutn("NAME1" + (nameLen <= 20 ? "_1" : nameLen <= 30 ? "_2" : "_3"), line, name);

                    if (null != birthday) {
//                        final String[] birthArray = KNJ_EditDate.tate_format(KNJ_EditDate.h_format_JP(db2, birthday));
//                        svf.VrsOutn("BIRTHDAY1_1", line, birthArray[0]);
//                        svf.VrsOutn("BIRTHDAY1_2", line, birthArray[1]);
//                        svf.VrsOutn("BIRTHDAY1_3", line, birthArray[2]);
//                        svf.VrsOutn("BIRTHDAY1_4", line, birthArray[3]);

                        final String[] birthArray = StringUtils.split(birthday, "-");
                        svf.VrsOutn("BIRTHDAY1_2", line, String.valueOf(Integer.parseInt(birthArray[0])));
                        svf.VrsOutn("BIRTHDAY1_3", line, String.valueOf(Integer.parseInt(birthArray[1])));
                        svf.VrsOutn("BIRTHDAY1_4", line, String.valueOf(Integer.parseInt(birthArray[2])));
                    }

                    final String setAddr = addr1 + addr2;
                    final int addrLen = getMS932ByteLength(setAddr);
                    svf.VrsOutn("ADDRESS1" + (addrLen <= 30 ? "" : addrLen <= 40 ? "_2" : addrLen <= 50 ? "_3" : "_4"), line, setAddr);

                    if (null != param._termSdate) {
//                        final String[] sdateArray = KNJ_EditDate.tate_format(KNJ_EditDate.h_format_JP(db2, param._termSdate));
//                        svf.VrsOutn("SDATE1_1", line, sdateArray[0]);
//                        svf.VrsOutn("SDATE1_2", line, sdateArray[1]);
//                        svf.VrsOutn("SDATE1_3", line, sdateArray[2]);
//                        svf.VrsOutn("SDATE1_4", line, sdateArray[3]);

                        final String[] sdateArray = StringUtils.split(param._termSdate, "-");
                        svf.VrsOutn("SDATE1_2", line, String.valueOf(Integer.parseInt(sdateArray[0])));
                        svf.VrsOutn("SDATE1_3", line, String.valueOf(Integer.parseInt(sdateArray[1])));
                        svf.VrsOutn("SDATE1_4", line, String.valueOf(Integer.parseInt(sdateArray[2])));
                    }

                    int rosenLine = 1;
                    for (int r = 1; r <= 7; r++) {
                    	final String kotsu = KnjDbUtils.getString(row, "KOTSU_" + String.valueOf(r));
                    	final String jousya = KnjDbUtils.getString(row, "JOUSYA_" + String.valueOf(r));
                    	final String gesya = KnjDbUtils.getString(row, "GESYA_" + String.valueOf(r));
                    	if (StringUtils.isBlank(kotsu) && StringUtils.isBlank(jousya) && StringUtils.isBlank(gesya)) {
                    		continue;
                    	}
                    	final int kotsuKeta = getMS932ByteLength(kotsu);
                        svf.VrsOutn("TRANSPORT" + String.valueOf(rosenLine) + "_" + (kotsuKeta <= 14 ? "1" : kotsuKeta <= 20 ? "2" : "3"), line, kotsu);
                        final String kukan = StringUtils.defaultString(jousya) + " - " + StringUtils.defaultString(gesya);
                        final int kukanKeta = getMS932ByteLength(kukan);
                        svf.VrsOutn("SECTION" + String.valueOf(rosenLine) + "_" + (kukanKeta <= 34 ? "1" : kukanKeta <= 42 ? "2" : "3"), line, kukan);
                    	rosenLine += 1;
                    }

                    nonedata = true;
            	}
            	svf.VrEndPage();

            }

        } else {
        	setForm(svf, param, "KNJA143H_6_2.frm", 1);

            final int maxCol = 3;
            final int maxLine = 4;

            final File stamp = param.getImageFile("SCHOOLSTAMP_H.bmp"); //学校長印データ存在チェック用
            final int remark1len = getMS932ByteLength(param._addr1);
            final String remark1suf = remark1len <= 30 ? "1" : remark1len <= 40 ? "2" : "3";
            final int schoolnamelen = getMS932ByteLength(param._schoolname);
            final String schoolnamesuf = schoolnamelen <= 24 ? "1" : "2";

            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = sql(param);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                int line = 1;
                int col = 0;

                for (int line1 = 1; line1 <= maxLine; line1++) {
                    for (int col1 = 1; col1 <= maxCol; col1++) {
                        final String scol = String.valueOf(col1);
                        svf.VrsOutn("TITLE" + scol, line1, "身分証明書"); // タイトル
                        if ("1".equals(param._taishousha)) {
                            svf.VrsOutn("SENTENCE" + scol, line1, "生徒"); // 証明文言
                        } else {
                            svf.VrsOutn("SENTENCE" + scol, line1, "職員"); // 証明文言
                        }
                        svf.VrsOutn("LIMIT_NAME" + scol, line1, "有効期限"); // 有効期限名
                    }
                }

                while (rs.next()) {
                    col += 1;

                    if (col > maxCol) {
                        line += 1;
                        col = 1;
                    }
                    if (line > maxLine) {
                        svf.VrEndPage();
                        for (int line1 = 1; line1 <= maxLine; line1++) {
                            for (int col1 = 1; col1 <= maxCol; col1++) {
                                final String scol = String.valueOf(col1);
                                svf.VrsOutn("TITLE" + scol, line1, "身分証明書"); // タイトル
                                if ("1".equals(param._taishousha)) {
                                    svf.VrsOutn("SENTENCE" + scol, line1, "生徒"); // 証明文言
                                } else {
                                    svf.VrsOutn("SENTENCE" + scol, line1, "職員"); // 証明文言
                                }
                                svf.VrsOutn("LIMIT_NAME" + scol, line1, "有効期限"); // 有効期限名
                            }
                        }
                        line = 1;
                    }

                    final String schregno = rs.getString("SCHREGNO");//学籍番号または職員番号
                    final String name = "1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME") : rs.getString("NAME");
                    final String birthday = rs.getString("BIRTHDAY");
                    final String majorname = StringUtils.defaultString(rs.getString("MAJORNAME"));
                    final String hrname = StringUtils.defaultString(rs.getString("HR_NAME"));
                    final String grade1 = null == rs.getString("GRADE") ? "" : rs.getString("GRADE").length() <= 1 ? rs.getString("GRADE") : rs.getString("GRADE").substring(rs.getString("GRADE").length() - 1);
                    final String hrclass1 = null == rs.getString("HR_CLASS") ? "" : rs.getString("HR_CLASS").length() <= 1 ? rs.getString("HR_CLASS") : rs.getString("HR_CLASS").substring(rs.getString("HR_CLASS").length() - 1);
                    final String attendno2 = null == rs.getString("ATTENDNO") ? "" : rs.getString("ATTENDNO").length() <= 2 ? rs.getString("ATTENDNO") : rs.getString("ATTENDNO").substring(rs.getString("ATTENDNO").length() - 2);

                    final String scol = String.valueOf(col);
                    final String scol1 = col == 1 ? "" : "_" + scol;

                    final File schregimg = param.getImageFile("P" + schregno + "." + param._extension); //写真データ存在チェック用
                    if (null != schregimg) { svf.VrsOutn("PHOTO_BMP" + scol1, line, schregimg.getPath()); } //顔写真

                    //学校印
                    if (null != stamp) { svf.VrsOutn("STAMP_BMP" + scol1, line, stamp.getPath()); } //学校印

                    //学籍番号
                    svf.VrsOutn("ENT_SCHOOL" + scol, line, majorname + "　　" + hrname); // 入学学校
                    final int namelen = getMS932ByteLength(name);
                    svf.VrsOutn("NO" + scol, line, "No." + (grade1 + hrclass1 + attendno2)); // 番号
                    svf.VrsOutn("NAME" + scol + "_" + (namelen <= 20 ? "1" : namelen <= 30 ? "2" : "3"), line, name); // 生徒氏名
                    svf.VrsOutn("SCHREGNO" + scol, line, schregno); // 学籍番号または職員番号
                    if (null != param._termSdate) {
                        svf.VrsOutn("SDATE" + scol, line, KNJ_EditDate.h_format_SeirekiJP(param._termSdate) + "発行"); // 発行日
                    }
                    if (null != param._termEdate) {
                        svf.VrsOutn("LIMIT_DATE" + scol, line, KNJ_EditDate.h_format_SeirekiJP(param._termEdate)); // 有効期限
                    }
                    if (null != birthday) {
                        svf.VrsOutn("BIRTHDAY" + scol, line, KNJ_EditDate.h_format_SeirekiJP(birthday) + "生"); // 生年月日
                    }
                    svf.VrsOutn("SCHOOLADDRESS" + remark1suf + "_" + scol, line, param._addr1); // 学校所在地
                    svf.VrsOutn("TELNO" + scol, line, param._remark3); // 電話番号
                    svf.VrsOutn("SCHOOLNAME" + scol + "_" + schoolnamesuf, line, param._schoolname); // 学校名
                    svf.VrsOutn("JOBNAME" + scol, line, param._jobname); // 役職・氏名
                    svf.VrsOutn("STAFFNAME" + scol, line, param._principalName); // 役職・氏名

                    nonedata = true;
                }

                if (nonedata) {
                    svf.VrEndPage();
                }

            } catch (Exception ex) {
                log.error("setSvfout set error!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }

    /** 帳票出力 **/
    private void printSvfMainStaff(
        final DB2UDB db2,
        final Vrw32alp svf,
        final Param param,
        final boolean seireki
    ) {
        final int maxLine = 4;
        final int maxCol = 3;

        final int remark1len = getMS932ByteLength(param._addr1);
        final String schoolAddrField = remark1len <= 46 ? "_1" : "_2";

        PreparedStatement ps = null;
        ResultSet rs = null;

        String schoolstampPath = null;
        if ("1".equals(param._printStamp)) {
        	final File schoolstampFile = param.getImageFile("SCHOOLSTAMP_H.bmp");
            if (null != schoolstampFile) {
            	schoolstampPath = schoolstampFile.getAbsolutePath();
            }
            log.info(" schoolstampPath = " + schoolstampPath);
        }

        try {
            final String sql = sql(param);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            if ("2".equals(param._taishousha)) {
            	setForm(svf, param, "KNJA143H_3.frm", 1);
            } else {
            	setForm(svf, param, "KNJA143H.frm", 1);
            }

            int line = 1;
            int col = 1;

            while (rs.next()) {

                if (col > maxCol) {
                    col = 1;
                    line++;
                }
                if (line > maxLine) {
                    svf.VrEndPage();
                    line = 1;
                    setForm(svf, param, "KNJA143H_4.frm", 1);
                    svf.VrsOut("DUMMY", "　");
                    svf.VrEndPage();
                    setForm(svf, param, "KNJA143H_3.frm", 1);
                }

                final String staffCd = rs.getString("STAFFCD");
                final String name = "1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME") : rs.getString("NAME");
                final String birthday = rs.getString("BIRTHDAY");
                final String addr1 = rs.getString("ADDR1");
                final String addr2 = rs.getString("ADDR2");

                svf.VrsOutn("TITLE" + col, line, "職員証明書");
                svf.VrsOutn("SCHREGNO" + col, line, staffCd);
                svf.VrsOutn("SENTENCE" + col, line, "職員");
                final int schoolNameLen = getMS932ByteLength(param._schoolname);
                svf.VrsOutn("SCHOOLNAME" + col + (schoolNameLen <= 36 ? "_1" : "_2"), line, param._schoolname);

                final int nameLen = getMS932ByteLength(name);
                svf.VrsOutn("NAME" + col + (nameLen <= 36 ? "_1" : nameLen <= 40 ? "_2" : "_3"), line, name);

                svf.VrsOutn("BIRTHDAY" + col, line, StringUtils.defaultString((seireki ? KNJ_EditDate.h_format_SeirekiJP(birthday) : KNJ_EditDate.h_format_JP(db2, birthday))) + "生");

                final File schregimg = param.getImageFile("P" + staffCd + "." + param._extension); //写真データ存在チェック用
                if (null != schregimg) {
                    svf.VrsOutn("PHOTO_BMP_" + col, line, schregimg.getPath());
                }

                final String setAddr = addr1 + addr2;
                final int addrLen = getMS932ByteLength(setAddr);
                svf.VrsOutn("ADDRESS" + col + (addrLen <= 36 ? "_1" : addrLen <= 40 ? "_2" : "_3"), line, setAddr);

                svf.VrsOutn("SDATE" + col, line, StringUtils.defaultString((seireki ? KNJ_EditDate.h_format_SeirekiJP(param._termSdate) : KNJ_EditDate.h_format_JP(db2, param._termSdate))) + "　発行");

                svf.VrsOutn("SCHOOLADDRESS" + col + schoolAddrField, line, param._addr1);
                svf.VrsOutn("JOBNAME" + col, line, param._schoolname + param._principalName);

                if ("1".equals(param._printStamp) && null != schoolstampPath) {
                	svf.VrsOutn("SCHOOLSTAMP" + col, line, schoolstampPath);
                }

                col++;
                nonedata = true;
            }

            if (nonedata) {
                svf.VrEndPage();
                setForm(svf, param, "KNJA143H_4.frm", 1);
                svf.VrsOut("DUMMY", "　");
                svf.VrEndPage();
            }

        } catch (Exception ex) {
            log.error("setSvfout set error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        if (nonedata) {
            svf.VrEndPage();
        }
    }

    /** 帳票出力 **/
    private void printSvfMainfukui(
        final DB2UDB db2,
        final Vrw32alp svf,
        final Param param
    ) {
        final String sql = sql(param);
        final List targetList = KnjDbUtils.query(db2, sql);

        final File bgImageFile = param.getImageFile("knja143_background.jpg");
        String bgImagePath = null;
        if (null != bgImageFile) {
            bgImagePath = bgImageFile.getAbsolutePath();
        }

        final File schoolstampFile = param.getImageFile("SCHOOLSTAMP_H.bmp");
        String schoolstampPath = null;
        if (null != schoolstampFile) {
        	schoolstampPath = schoolstampFile.getAbsolutePath();
        }
        log.info(" schoolstampPath = " + schoolstampPath);

        final String form = param._isFukuiken ? "KNJA143H_7N.frm" : "KNJ" + param._useFormNameA143H + ".frm";

        if ("1".equals(param._taishousha)) {
            final int maxCol = 2;
            final int maxLine = 5;
            final List pageList = getPageList(targetList, maxCol * maxLine);

        	for (int pi = 0; pi < pageList.size(); pi++) {

                final List studentList = (List) pageList.get(pi);
                setForm(svf, param, form, 1);

                for (int i = 0; i < maxCol * maxLine; i++) {
                    final int ip = i + 1;
                    final String scol = String.valueOf(ip % maxCol == 0 ? maxCol : ip % maxCol);
                    final int line = i / maxCol + 1;
                    svf.VrsOutn("TITLE" + scol, line, "生徒証明書");
                    svf.VrsOutn("SENTENCE" + scol, line, "生徒");
                }

                if (null != bgImagePath) {
                    svf.VrsOut("BK1_1", bgImagePath);
                }

                for (int i = 0; i < studentList.size(); i++) {
                    final Map student = (Map) studentList.get(i);
                    final int ip = i + 1;
                    final String scol = String.valueOf(ip % maxCol == 0 ? maxCol : ip % maxCol);
                    final int line = i / maxCol + 1;

                    final String schregno = getString(student, "SCHREGNO");
                    final String name = "1".equals(getString(student, "USE_REAL_NAME")) ? getString(student, "REAL_NAME") : getString(student, "NAME");

                    final File schregimg = param.getImageFile("P" + schregno + "." + param._extension); //写真データ存在チェック用
                    if (null != schregimg) {
                        svf.VrsOutn("PHOTO_BMP" + scol, line, schregimg.getPath());
                    }

                    svf.VrsOutn("SCHREGNO" + scol, line, schregno);


                    final String hrname = StringUtils.defaultString(getString(student,"HR_NAME"));
                    final String attendno1 = StringUtils.defaultString(getString(student, "ATTENDNO"));
                    final String attendno2 = null == attendno1 ? "" : attendno1.length() <= 2 ? attendno1 : attendno1.substring(attendno1.length() - 2);
                    final String majorname = StringUtils.defaultString(getString(student,"MAJORNAME"));
                    final String syozoku = hrname + "　" + attendno2 + "番(" + majorname + ")";
                    final int syozokuLen = getMS932ByteLength(syozoku);
                    svf.VrsOutn("ENT_SCHOOL" + scol + (syozokuLen <= 32 ? "_1" : syozokuLen <= 36 ? "_2" : "3"), line, syozoku);

                    final int nameLen = getMS932ByteLength(name);
                    svf.VrsOutn("NAME" + scol + (nameLen <= 24 ? "_1" : nameLen <= 30 ? "_2" : "_3"), line, name);

                    if (null != getString(student, "BIRTHDAY")) {
                    	final String[] birthday = KNJ_EditDate.tate_format4(db2, getString(student, "BIRTHDAY"));
                    	svf.VrsOutn("BIRTHDAY" + scol + "_1", line, birthday[0]);
                    	svf.VrsOutn("BIRTHDAY" + scol + "_2", line, birthday[1]);
                    	svf.VrsOutn("BIRTHDAY" + scol + "_3", line, birthday[2]);
                    	svf.VrsOutn("BIRTHDAY" + scol + "_4", line, birthday[3]);
                    }

                    final String addr1 = getString(student, "ADDR1");
                    final String addr2 = getString(student, "ADDR2");
                    final String setAddr = addr1 + addr2;
                    final int addrLen = getMS932ByteLength(setAddr);
                    svf.VrsOutn("ADDRESS" + scol + (addrLen <= 30 ? "_1" : addrLen <= 40 ? "_2" : addrLen <= 50 ? "_3" : "_4"), line, setAddr);

                    if (null != param._termSdate) {
                        final String[] sdateArray = KNJ_EditDate.tate_format4(db2, param._termSdate);
                        svf.VrsOutn("SDATE" + scol + "_1", line, sdateArray[0]);
                        svf.VrsOutn("SDATE" + scol + "_2", line, sdateArray[1]);
                        svf.VrsOutn("SDATE" + scol + "_3", line, sdateArray[2]);
                        svf.VrsOutn("SDATE" + scol + "_4", line, sdateArray[3]);
                    }

                    final int schoolAddrLen = getMS932ByteLength(param._addr1);
                    svf.VrsOutn("SCHOOLADDRESS" + scol + (schoolAddrLen <= 40 ? "_1" : schoolAddrLen <= 46 ? "_2" : schoolAddrLen <= 50 ? "_3" : "_4"), line, param._addr1);
                    svf.VrsOutn("SCHOOLNAME" + scol, line, StringUtils.defaultString(param._schoolname) + StringUtils.defaultString(param._principalName));

                    if ("1".equals(param._printStamp) && null != schoolstampPath) {
                        svf.VrsOutn("SCHOOLSTAMP" + scol, line, schoolstampPath);
                    }
                }

                nonedata = true;
                svf.VrEndPage();
        	}

        } else if ("2".equals(param._taishousha)) {
            final int maxCol = 2;
            final int maxLine = 6;
            final List pageList = getPageList(targetList, maxCol * maxLine);

        	for (int pi = 0; pi < pageList.size(); pi++) {

                final List staffList = (List) pageList.get(pi);
                setForm(svf, param, "KNJA143H_7_3.frm", 1); // 職員用

                if (null != bgImagePath) {
                    svf.VrsOut("BK1_1", bgImagePath);
                }

                for (int i = 0; i < maxCol * maxLine; i++) {
                    final int ip = i + 1;
                    final String scol = String.valueOf(ip % maxCol == 0 ? maxCol : ip % maxCol);
                    final int line = i / maxCol + 1;
                    svf.VrsOutn("TITLE" + scol, line, "身分証明書");
                }

                for (int i = 0; i < staffList.size(); i++) {
                    final Map staff = (Map) staffList.get(i);
                    final int ip = i + 1;
                    final String scol = String.valueOf(ip % maxCol == 0 ? maxCol : ip % maxCol);
                    final int line = i / maxCol + 1;

                    final String staffcd = getString(staff, "STAFFCD");
                    final String name = "1".equals(getString(staff, "USE_REAL_NAME")) ? getString(staff, "REAL_NAME") : getString(staff, "NAME");

                    final File staffimg = param.getImageFile("T" + staffcd + "." + param._extension); //写真データ存在チェック用

                    if (null != staffimg) {
                        svf.VrsOutn("PHOTO_BMP" + scol, line, staffimg.getPath());
                    }

                    final int nameLen = getMS932ByteLength(name);
                    svf.VrsOutn("NAME" + scol + (nameLen <= 24 ? "_1" : nameLen <= 30 ? "_2" : "_3"), line, name);

                    if (null != getString(staff, "BIRTHDAY")) {
                    	svf.VrsOutn("BIRTHDAY" + scol, line, KNJ_EditDate.h_format_JP(db2, getString(staff, "BIRTHDAY")));
                    }

                    svf.VrsOutn("DATE_RANGE" + scol, line, KNJ_EditDate.h_format_JP(db2, param._termSdate));
                    svf.VrsOutn("SCHOOLADDRESS" + scol, line, param._addr1);
                    svf.VrsOutn("JOBNAME" + scol, line, param._jobname);

                    if (null != schoolstampPath) {
                    	svf.VrsOutn("SCHOOLSTAMP" + scol, line, schoolstampPath);
                    }
                }

                nonedata = true;
                svf.VrEndPage();

                setForm(svf, param, "KNJA143H_7_4.frm", 1); // 裏面
                svf.VrsOut("DUMMY", "1");
                svf.VrEndPage();
            }
        }
    }

    private void printSvfMainHirokoudai(
            final DB2UDB db2,
            final Vrw32alp svf,
            final Param param
        ) {
        final String sql = sql(param);
        final List targetList = KnjDbUtils.query(db2, sql);

        final File bgImageFile = param.getImageFile("knja143_background.jpg");
        String bgImagePath = null;
        if (null != bgImageFile) {
            bgImagePath = bgImageFile.getAbsolutePath();
        }

        final File schoolstampFile = param.getImageFile("SCHOOLSTAMP_H.bmp");
        String schoolstampPath = null;
        if (null != schoolstampFile) {
        	schoolstampPath = schoolstampFile.getAbsolutePath();
        }
        log.info(" schoolstampPath = " + schoolstampPath);

        final String form = "KNJ" + param._useFormNameA143H + ".frm";
        final int maxCol = 2;
        final int maxLine = 6;
        final List pageList = getPageList(targetList, maxCol * maxLine);
        for (int pi = 0; pi < pageList.size(); pi++) {

            if ("1".equals(param._taishousha)) {
                final List studentList = (List) pageList.get(pi);
                setForm(svf, param, form, 1);

                for (int i = 0; i < maxCol * maxLine; i++) {
                    final int ip = i + 1;
                    final String scol = String.valueOf(ip % maxCol == 0 ? maxCol : ip % maxCol);
                    final int line = i / maxCol + 1;
                    svf.VrsOutn("TITLE" + scol, line, "生徒証明書");
                }

                if (null != bgImagePath) {
                    svf.VrsOut("BK1_1", bgImagePath);
                }

                for (int i = 0; i < studentList.size(); i++) {
                    final Map student = (Map) studentList.get(i);
                    final int ip = i + 1;
                    final String scol = String.valueOf(ip % maxCol == 0 ? maxCol : ip % maxCol);
                    final int line = i / maxCol + 1;

                    final String schregno = getString(student, "SCHREGNO");
                    final String name = "1".equals(getString(student, "USE_REAL_NAME")) ? getString(student, "REAL_NAME") : getString(student, "NAME");

                    final File schregimg = param.getImageFile("P" + schregno + "." + param._extension); //写真データ存在チェック用
                    if (null != schregimg) {
                        svf.VrsOutn("PHOTO_BMP" + scol, line, schregimg.getPath());
                    }

                    svf.VrsOutn("SCHREGNO" + scol, line, schregno);

                    final int nameLen = getMS932ByteLength(name);
                    svf.VrsOutn("NAME" + scol + (nameLen <= 20 ? "_1" : nameLen <= 30 ? "_2" : "_3"), line, name);

                    if (null != getString(student, "BIRTHDAY")) {
                    	svf.VrsOutn("BIRTHDAY" + scol, line, KNJ_EditDate.h_format_JP(db2, getString(student, "BIRTHDAY")));
                    }

                    final String addr1 = getString(student, "ADDR1");
                    String addr2 = getString(student, "ADDR2");
                    if (null != addr2) {
                    	addr2 = "　" + addr2;
                    }
                    final int addrLen = Math.max(getMS932ByteLength(addr1),  getMS932ByteLength(addr2));
                    svf.VrsOutn("ADDRESS" + scol + (addrLen <= 30 ? "_1" : addrLen <= 40 ? "_2" : "_3") + "_1", line, addr1);
                    svf.VrsOutn("ADDRESS" + scol + (addrLen <= 30 ? "_1" : addrLen <= 40 ? "_2" : "_3") + "_2", line, addr2);

                    svf.VrsOutn("DATE_RANGE" + scol, line, KNJ_EditDate.h_format_JP(db2, param._termSdate) + "～" + KNJ_EditDate.h_format_JP(db2, param._termEdate));
                    if (null != param._remark3) {
                    	svf.VrsOutn("TELNO" + scol, line, "(TEL " + param._remark3 + ")");
                    }
                    svf.VrsOutn("SCHOOLADDRESS" + scol, line, param._addr1);
                    svf.VrsOutn("JOBNAME" + scol, line, StringUtils.defaultString(param._schoolname) + StringUtils.defaultString(param._jobname));

                    if (null != schoolstampPath) {
                    	svf.VrsOutn("SCHOOLSTAMP" + scol, line, schoolstampPath);
                    }
                    final String schkStr = "J".equals(getString(student, "SCHOOL_KIND")) ? "中学校" : "高等学校";
                    final String courseStr = StringUtils.defaultString(getString(student, "COURSENAME"), "");
                    final String outStr = schkStr + "・" + ("".equals(courseStr) ? "" : courseStr+"課程");
                    svf.VrsOutn("COURSE_NAME" + scol, line, outStr); //校種名"・"コース名
                }

                nonedata = true;
                svf.VrEndPage();

                setForm(svf, param, "KNJ" + param._useFormNameA143H + "_2.frm", 1); // 裏面
                svf.VrsOut("DUMMY", "1");
                svf.VrEndPage();

            } else if ("2".equals(param._taishousha)) {

                final List staffList = (List) pageList.get(pi);
                setForm(svf, param, "KNJ" + param._useFormNameA143H + "_3.frm", 1); // 職員用

                if (null != bgImagePath) {
                    svf.VrsOut("BK1_1", bgImagePath);
                }

                for (int i = 0; i < maxCol * maxLine; i++) {
                    final int ip = i + 1;
                    final String scol = String.valueOf(ip % maxCol == 0 ? maxCol : ip % maxCol);
                    final int line = i / maxCol + 1;
                    svf.VrsOutn("TITLE" + scol, line, "身分証明書");
                }

                for (int i = 0; i < staffList.size(); i++) {
                    final Map staff = (Map) staffList.get(i);
                    final int ip = i + 1;
                    final String scol = String.valueOf(ip % maxCol == 0 ? maxCol : ip % maxCol);
                    final int line = i / maxCol + 1;

                    final String staffcd = getString(staff, "STAFFCD");
                    final String name = "1".equals(getString(staff, "USE_REAL_NAME")) ? getString(staff, "REAL_NAME") : getString(staff, "NAME");

                    final File staffimg = param.getImageFile("T" + staffcd + "." + param._extension); //写真データ存在チェック用

                    if (null != staffimg) {
                        svf.VrsOutn("PHOTO_BMP" + scol, line, staffimg.getPath());
                    }

                    final int nameLen = getMS932ByteLength(name);
                    svf.VrsOutn("NAME" + scol + (nameLen <= 24 ? "_1" : nameLen <= 30 ? "_2" : "_3"), line, name);

                    if (null != getString(staff, "BIRTHDAY")) {
                    	svf.VrsOutn("BIRTHDAY" + scol, line, KNJ_EditDate.h_format_JP(db2, getString(staff, "BIRTHDAY")));
                    }

                    svf.VrsOutn("DATE_RANGE" + scol, line, KNJ_EditDate.h_format_JP(db2, param._termSdate));
                    svf.VrsOutn("SCHOOLADDRESS" + scol, line, param._addr1);
                    svf.VrsOutn("JOBNAME" + scol, line, param._jobname);

                    if (null != schoolstampPath) {
                    	svf.VrsOutn("SCHOOLSTAMP" + scol, line, schoolstampPath);
                    }
                }

                nonedata = true;
                svf.VrEndPage();

                setForm(svf, param, "KNJ" + param._useFormNameA143H + "_4.frm", 1); // 裏面
                svf.VrsOut("DUMMY", "1");
                svf.VrEndPage();
            }
        }
    }

    private String getGesya(final String gesyaALL, final String gesya) {
        if (gesyaALL != "") return gesyaALL;
        if (gesya == null) return gesyaALL;
        return gesya;
    }

    /**生徒又は職員情報**/
    private String sql(final Param param)
    {
        final StringBuffer stb = new StringBuffer();
        if ("1".equals(param._taishousha)) {
            stb.append(" WITH SCHREG_ADDRESS_MAX AS ( ");
            stb.append("     SELECT ");
            stb.append("         SCHREGNO, ");
            stb.append("         MAX(ISSUEDATE) AS ISSUEDATE ");
            stb.append("     FROM ");
            stb.append("         SCHREG_ADDRESS_DAT ");
            stb.append("     GROUP BY ");
            stb.append("         SCHREGNO ");
            stb.append("     ) ");
            stb.append(" , SCHREG_ADDRESS AS ( ");
            stb.append("     SELECT DISTINCT ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.ZIPCD, ");
            stb.append("         P1.PREF_CD, ");
            stb.append("         P1.PREF_NAME, ");
            stb.append("         T1.AREACD, ");
            stb.append("         N1.NAME1 AS AREA_NAME, ");
            stb.append("         T1.ADDR1, ");
            stb.append("         T1.ADDR2, ");
            stb.append("         T1.TELNO ");
            stb.append("     FROM ");
            stb.append("         SCHREG_ADDRESS_DAT T1 ");
            stb.append("         INNER JOIN SCHREG_ADDRESS_MAX T2 ");
            stb.append("             ON  T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("             AND T2.ISSUEDATE = T1.ISSUEDATE ");
            stb.append("         LEFT JOIN ZIPCD_MST Z1 ON Z1.NEW_ZIPCD = T1.ZIPCD ");
            stb.append("         LEFT JOIN PREF_MST P1 ON P1.PREF_CD = SUBSTR(Z1.CITYCD,1,2) ");
            stb.append("         LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'A020' AND N1.NAMECD2 = T1.AREACD ");
            stb.append("     ) ");
            stb.append("SELECT T1.SCHREGNO, ");
            stb.append("       T2.NAME, ");
            stb.append("       T6.COURSENAME, ");
            stb.append("       T5.MAJORNAME, ");
            stb.append("       T4.HR_NAME, ");
            stb.append("       T4.HR_NAMEABBV, ");
            stb.append("       T4.HR_CLASS_NAME1, ");
            stb.append("       GDAT.SCHOOL_KIND, ");
            stb.append("       GDAT.GRADE_CD, ");
            stb.append("       T1.GRADE, ");
            stb.append("       T1.HR_CLASS, ");
            stb.append("       T1.ATTENDNO, ");
            stb.append("       T2.REAL_NAME, ");
            stb.append("       (CASE WHEN T6.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS USE_REAL_NAME, ");
            stb.append("       T2.BIRTHDAY, ");
            stb.append("       CASE WHEN T2.BIRTHDAY IS NOT NULL THEN YEAR('" + param._termSdate + "' - T2.BIRTHDAY) END AS AGE, ");
            stb.append("       CASE WHEN L5.FLG_1 = '1' THEN G1.STATION_NAME ELSE L5.GESYA_1 END AS JOSYA_1, ");
            stb.append("       CASE WHEN L5.FLG_1 = '1' THEN G1.STATION_NAME ELSE L5.GESYA_1 END AS GESYA_1, ");
            stb.append("       CASE WHEN L5.FLG_2 = '1' THEN G2.STATION_NAME ELSE L5.GESYA_2 END AS GESYA_2, ");
            stb.append("       CASE WHEN L5.FLG_3 = '1' THEN G3.STATION_NAME ELSE L5.GESYA_3 END AS GESYA_3, ");
            stb.append("       CASE WHEN L5.FLG_4 = '1' THEN G4.STATION_NAME ELSE L5.GESYA_4 END AS GESYA_4, ");
            stb.append("       CASE WHEN L5.FLG_5 = '1' THEN G5.STATION_NAME ELSE L5.GESYA_5 END AS GESYA_5, ");
            stb.append("       CASE WHEN L5.FLG_6 = '1' THEN G6.STATION_NAME ELSE L5.GESYA_6 END AS GESYA_6, ");
            stb.append("       CASE WHEN L5.FLG_7 = '1' THEN G7.STATION_NAME ELSE L5.GESYA_7 END AS GESYA_7, ");
            stb.append("       CASE WHEN L5.FLG_1 = '1' THEN J1.STATION_NAME ELSE L5.JOSYA_1 END AS JOUSYA_1, ");
            stb.append("       CASE WHEN L5.FLG_2 = '1' THEN J2.STATION_NAME ELSE L5.JOSYA_2 END AS JOUSYA_2, ");
            stb.append("       CASE WHEN L5.FLG_3 = '1' THEN J3.STATION_NAME ELSE L5.JOSYA_3 END AS JOUSYA_3, ");
            stb.append("       CASE WHEN L5.FLG_4 = '1' THEN J4.STATION_NAME ELSE L5.JOSYA_4 END AS JOUSYA_4, ");
            stb.append("       CASE WHEN L5.FLG_5 = '1' THEN J5.STATION_NAME ELSE L5.JOSYA_5 END AS JOUSYA_5, ");
            stb.append("       CASE WHEN L5.FLG_6 = '1' THEN J6.STATION_NAME ELSE L5.JOSYA_6 END AS JOUSYA_6, ");
            stb.append("       CASE WHEN L5.FLG_7 = '1' THEN J7.STATION_NAME ELSE L5.JOSYA_7 END AS JOUSYA_7, ");
            if ("A143H_6".equals(param._useFormNameA143H)) {
            	stb.append("       CASE WHEN L5.FLG_1 = '1' THEN J1.RR_NAME WHEN L5.FLG_1 = '2' THEN L5.ROSEN_1 END AS KOTSU_1, ");
                stb.append("       CASE WHEN L5.FLG_2 = '1' THEN J2.RR_NAME WHEN L5.FLG_2 = '2' THEN L5.ROSEN_2 END AS KOTSU_2, ");
                stb.append("       CASE WHEN L5.FLG_3 = '1' THEN J3.RR_NAME WHEN L5.FLG_3 = '2' THEN L5.ROSEN_3 END AS KOTSU_3, ");
                stb.append("       CASE WHEN L5.FLG_4 = '1' THEN J4.RR_NAME WHEN L5.FLG_4 = '2' THEN L5.ROSEN_4 END AS KOTSU_4, ");
                stb.append("       CASE WHEN L5.FLG_5 = '1' THEN J5.RR_NAME WHEN L5.FLG_5 = '2' THEN L5.ROSEN_5 END AS KOTSU_5, ");
                stb.append("       CASE WHEN L5.FLG_6 = '1' THEN J6.RR_NAME WHEN L5.FLG_6 = '2' THEN L5.ROSEN_6 END AS KOTSU_6, ");
                stb.append("       CASE WHEN L5.FLG_7 = '1' THEN J7.RR_NAME WHEN L5.FLG_7 = '2' THEN L5.ROSEN_7 END AS KOTSU_7, ");
            } else {
                stb.append("       CASE WHEN L5.FLG_1 = '1' THEN J1.RR_NAME END AS KOTSU_1, ");
                stb.append("       CASE WHEN L5.FLG_2 = '1' THEN J2.RR_NAME END AS KOTSU_2, ");
                stb.append("       CASE WHEN L5.FLG_3 = '1' THEN J3.RR_NAME END AS KOTSU_3, ");
                stb.append("       CASE WHEN L5.FLG_4 = '1' THEN J4.RR_NAME END AS KOTSU_4, ");
                stb.append("       CASE WHEN L5.FLG_5 = '1' THEN J5.RR_NAME END AS KOTSU_5, ");
                stb.append("       CASE WHEN L5.FLG_6 = '1' THEN J6.RR_NAME END AS KOTSU_6, ");
                stb.append("       CASE WHEN L5.FLG_7 = '1' THEN J7.RR_NAME END AS KOTSU_7, ");
            }
            stb.append("       VALUE(ADDR.ADDR1, '') AS ADDR1, ");
            stb.append("       VALUE(ADDR.ADDR2, '') AS ADDR2 ");
            stb.append("FROM   SCHREG_REGD_DAT T1 ");
            stb.append("       INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("       LEFT JOIN SCHREG_ADDRESS ADDR ON ADDR.SCHREGNO = T1.SCHREGNO ");
            stb.append("       LEFT JOIN SCHREG_NAME_SETUP_DAT T6 ON T6.SCHREGNO = T1.SCHREGNO ");
            stb.append("            AND T6.DIV = '01' ");
            stb.append("       LEFT JOIN SCHREG_REGD_HDAT T4 ON T4.YEAR = T1.YEAR ");
            stb.append("            AND T4.SEMESTER = T1.SEMESTER ");
            stb.append("            AND T4.GRADE = T1.GRADE ");
            stb.append("            AND T4.HR_CLASS = T1.HR_CLASS ");
            stb.append("       LEFT JOIN SCHREG_REGD_GDAT GDAT ");
            stb.append("         ON GDAT.YEAR = T1.YEAR ");
            stb.append("        AND GDAT.GRADE = T1.GRADE ");
            stb.append("       LEFT JOIN COURSE_MST T6 ON T6.COURSECD = T1.COURSECD ");
            stb.append("       LEFT JOIN MAJOR_MST T5 ON T5.COURSECD = T1.COURSECD ");
            stb.append("            AND T5.MAJORCD = T1.MAJORCD ");
            stb.append("       LEFT JOIN SCHREG_ENVIR_DAT L5 ON L5.SCHREGNO = T1.SCHREGNO ");
            stb.append("       LEFT JOIN STATION_NETMST J1 ON J1.STATION_CD = L5.JOSYA_1 ");
            stb.append("       LEFT JOIN STATION_NETMST J2 ON J2.STATION_CD = L5.JOSYA_2 ");
            stb.append("       LEFT JOIN STATION_NETMST J3 ON J3.STATION_CD = L5.JOSYA_3 ");
            stb.append("       LEFT JOIN STATION_NETMST J4 ON J4.STATION_CD = L5.JOSYA_4 ");
            stb.append("       LEFT JOIN STATION_NETMST J5 ON J5.STATION_CD = L5.JOSYA_5 ");
            stb.append("       LEFT JOIN STATION_NETMST J6 ON J6.STATION_CD = L5.JOSYA_6 ");
            stb.append("       LEFT JOIN STATION_NETMST J7 ON J7.STATION_CD = L5.JOSYA_7 ");
            stb.append("       LEFT JOIN STATION_NETMST G1 ON G1.STATION_CD = L5.GESYA_1 ");
            stb.append("       LEFT JOIN STATION_NETMST G2 ON G2.STATION_CD = L5.GESYA_2 ");
            stb.append("       LEFT JOIN STATION_NETMST G3 ON G3.STATION_CD = L5.GESYA_3 ");
            stb.append("       LEFT JOIN STATION_NETMST G4 ON G4.STATION_CD = L5.GESYA_4 ");
            stb.append("       LEFT JOIN STATION_NETMST G5 ON G5.STATION_CD = L5.GESYA_5 ");
            stb.append("       LEFT JOIN STATION_NETMST G6 ON G6.STATION_CD = L5.GESYA_6 ");
            stb.append("       LEFT JOIN STATION_NETMST G7 ON G7.STATION_CD = L5.GESYA_7 ");
            stb.append("WHERE  T1.YEAR = '" + param._year + "' AND ");
            stb.append("       T1.SEMESTER = '" + param._gakki + "' AND ");
            stb.append("       T1.GRADE||T1.HR_CLASS||T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._schregnos) + " ");
            stb.append("ORDER BY ");
            stb.append("       T1.GRADE, ");
            stb.append("       T1.HR_CLASS, ");
            stb.append("       T1.ATTENDNO ");
        } else {
            if ("A143H_6".equals(param._useFormNameA143H)) {
                stb.append("SELECT STAFFCD AS SCHREGNO, ");
            } else {
                stb.append("SELECT STAFFCD, ");
            }
            stb.append("    STAFFNAME AS NAME, ");
            stb.append("    '　' AS COURSENAME, ");
            stb.append("    '　' AS MAJORNAME, ");
            stb.append("    '　' AS HR_NAME, ");
            stb.append("    '' AS SCHOOL_KIND, ");
            stb.append("    '' AS GRADE, ");
            stb.append("    '' AS HR_CLASS, ");
            stb.append("    '' AS ATTENDNO, ");
            stb.append("    STAFFNAME_REAL AS REAL_NAME, ");
            stb.append("    0 AS USE_REAL_NAME, ");
            stb.append("    STAFFBIRTHDAY AS BIRTHDAY, ");
            stb.append("    VALUE(STAFFADDR1, '') AS ADDR1, ");
            stb.append("    VALUE(STAFFADDR2, '') AS ADDR2 ");
            stb.append("FROM ");
            stb.append("    V_STAFF_MST ");
            stb.append("WHERE ");
            stb.append("    YEAR = '" + param._year + "' ");
            stb.append("AND STAFFCD IN " + SQLUtils.whereIn(true, param._schregnos) + " ");
            stb.append("ORDER BY ");
            stb.append("    STAFFCD ");
        }
        return stb.toString();
    }

    private Param getParam(final DB2UDB db2, final HttpServletRequest request) {
        KNJServletUtils.debugParam(request, log);
        log.fatal("$Revision: 74598 $ $Date: 2020-05-29 17:10:06 +0900 (金, 29 5 2020) $"); // CVSキーワードの取り扱いに注意
        Param param = new Param(db2, request);
        return param;
    }

    private static class Param {
        private final String _year;
        private final String _gakki;
        private final String _gradeHrclass;
        private final String[] _schregnos;
        private final String _termSdate;
        private final String _termEdate;
        private final String _taishousha;

        private String _jobname;
        private String _principalName;
        private String _schoolname;
        private String _addr1;
        private String _remark3;

        private final String _documentRoot;
        private String _imagepath;
        private String _extension;
        private final String _useAddrField2;
        private final String _useFormNameA143H;
        final boolean _isOutputDebug;
        private final String _schoolName;
        private final String _outDiv;
        private final boolean _isFukuiken;
        private final boolean _fukuiLabelFlg;
        private final String _printStamp;


        public Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");                        //年度
            _gakki = request.getParameter("GAKKI");                      //学期
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");      //学年＋組
            _termSdate = request.getParameter("TERM_SDATE").replace('/','-');  // 発行日
            _termEdate = null == request.getParameter("TERM_EDATE") ? null : request.getParameter("TERM_EDATE").replace('/','-');  // 有効期限
            _taishousha = request.getParameter("TAISHOUSHA");            //対象者 (1:生徒,2:教職員)

            // 学籍番号の指定
            _schregnos = request.getParameterValues("category_selected"); //学籍番号
            loadCertifSchoolDat(db2, _year);

            _documentRoot = request.getParameter("DOCUMENTROOT");
            //  写真データ
            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;
            try {
                returnval = getinfo.Control(db2);
                _imagepath = returnval.val4;      //格納フォルダ
                _extension = returnval.val5;      //拡張子
            } catch (Exception e) {
                log.error("setHeader set error!", e);
            } finally {
                getinfo = null;
                returnval = null;
            }
            _useAddrField2 = request.getParameter("useAddrField2");
            _useFormNameA143H = request.getParameter("useFormNameA143H");
            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));

            _schoolName = getNameMst(db2, "NAME1", "Z010", "00");
            _outDiv = request.getParameter("OUTDIV"); //出力区分(1:A4用紙,2:ラベル)
            _isFukuiken = "fukuiken".equals(_schoolName);
            if(_isFukuiken && "1".equals(_taishousha) && "2".equals(_outDiv)) {
                //学校名称 = 福井 かつ 対象者 = 生徒 かつ 出力区分 = ラベル の場合
                _fukuiLabelFlg = true;
            }else {
                _fukuiLabelFlg = false;
            }
            _printStamp = request.getParameter("PRINT_STAMP");

        }

        private String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJA143H' AND NAME = '" + propName + "' "));
        }

        public File getImageFile(final String name) {
            final File file = new File(_documentRoot + "/" + _imagepath + "/" + name);
            if (_isOutputDebug) {
            	log.info(" file " + file.getAbsolutePath() + " exists? " + file.exists());
            }
            if (file.exists()) {
                return file;
            }
            return null;
        }

        public void loadCertifSchoolDat(final DB2UDB db2, final String year) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            String certifKindcd = "101";
            try {
                final String sql = "SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + year + "' AND CERTIF_KINDCD = '" + certifKindcd + "'";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _jobname =  rs.getString("JOB_NAME");
                    _principalName = rs.getString("PRINCIPAL_NAME");
                    _schoolname = rs.getString("SCHOOL_NAME");
                    _addr1 = rs.getString("REMARK1");
                    _remark3 = rs.getString("REMARK3");
                }
            } catch (Exception e) {
                log.error("setHeader name_mst error!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        public String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }
    }

}//クラスの括り
