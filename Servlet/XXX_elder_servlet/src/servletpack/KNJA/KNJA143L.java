// kanji=漢字
/*
 * $Id: d34207a93ee84eb71251a09e115703efb5114596 $
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
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 *
 *	学校教育システム 賢者 [学籍管理]
 *
 *					＜ＫＮＪＡ１４３Ｌ＞  身分証明書
 *
 **/

public class KNJA143L {

    private static final Log log = LogFactory.getLog(KNJA143L.class);

    boolean nonedata = false;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        log.fatal("$Revision: 63531 $ $Date: 2018-11-22 17:06:37 +0900 (木, 22 11 2018) $"); // CVSキーワードの取り扱いに注意

        Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;						//Databaseクラスを継承したクラス

        //	print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

        //	svf設定
        svf.VrInit();						   		//クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream());  		//PDFファイル名の設定

        //	ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch (Exception ex) {
            log.error("DB2 open error!", ex);
            return;
        }

        try {
            //  パラメータの取得
            KNJServletUtils.debugParam(request, log);
            final Param param = new Param(db2, request);

            //  ＳＶＦ作成処理
            setSvfout(db2, svf, param);

        } catch (Exception ex) {
            log.error("parameter error!", ex);
        } finally {
            log.debug("nonedata = " + nonedata);
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

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
    	return KNJ_EditEdit.getMS932ByteLength(str);
    }

    private static List getGroupList(final List list, final int max) {
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

    private void setSvfout(
            final DB2UDB db2,
            final Vrw32alp svf,
            final Param param
            ) {
        final int maxLine = 5;
        final int maxCol = 2;
        final List studentListAll = Student.getStudentList(db2, param);
        final List pageList = getGroupList(studentListAll, maxLine * maxCol);

        //画像 学校印
        String schoolStampPath = param._documentroot + "/" + param._imagePass + "/" + ("SCHOOLSTAMP" + (null == param._schoolKind ? "" : ("_" + param._schoolKind)) + param._extensionStamp);
        if (!new File(schoolStampPath).exists()) {
            schoolStampPath = null;
        }

        final boolean isH = "H".equals(param._schoolKind);
        final boolean isJorH = "J".equals(param._schoolKind) || isH;
        final boolean isP = "P".equals(param._schoolKind);
        final String form1 = isP ? "KNJA143L_5.frm" : isJorH ? "KNJA143L_3.frm" : "KNJA143L_1.frm";
        final String form2 = isP ? "KNJA143L_6.frm" : isJorH ? "KNJA143L_2.frm" : "KNJA143L_4.frm";

        for (int pi = 0; pi < pageList.size(); pi++) {
            final List studentList = (List) pageList.get(pi);

            final List lineList = getGroupList(studentList, maxCol);

            svf.VrSetForm(form1, 1);//用紙(表)
            for (int li = 0; li < maxLine; li++) {
                final int line = li + 1;
                for (int ci = 0; ci < maxCol; ci++) {
                    final String f = ci == 1 ? "_2" : "";
                    svf.VrsOutn("SENTENCE" + f, line, "生徒");
                }
            }

            for (int li = 0; li < lineList.size(); li++) {
                final int line = li + 1;
                final List cols = (List) lineList.get(li);
                for (int ci = 0; ci < cols.size(); ci++) {
                    final Student student = (Student) cols.get(ci);
                    final String f = ci == 1 ? "_2" : "";
                    //画像 顔写真
                    final String photo_check = param._documentroot + "/" + param._imagePass + "/" + ("P" + student._schregno + "." + param._extensionPhoto);
                    if (new File(photo_check).exists()) {
                        svf.VrsOutn("PHOTO_BMP" + f, line, photo_check);//顔写真
                    }
                    //学校印
                    if (null != schoolStampPath) {
                        svf.VrsOutn("STAMP_BMP" + f, line, schoolStampPath);//学校印
                    }

                    // 生徒情報
                    if (isH) {
                        svf.VrsOutn("GRADE_TITLE" + f, line, "学年");
                        svf.VrsOutn("GRADE" + f, line, "第" + StringUtils.defaultString(student._gradeName2, " ") + "学年");
                    }
                    svf.VrsOutn("NAME" + f, line, "1".equals(student._useRealName) ? student._realName : student._name);                //氏名(漢字)
                    svf.VrsOutn("TITLE" + f, line, "身分証明書");
                    svf.VrsOutn("SCHREGNO" + f, line, student._schregno);
                    svf.VrsOutn("SENTENCE" + f, line, "生徒");

                    printDivitedDate(db2, svf, param, "SDATE", f, line, param._termSdate); // 発行日
                    if (null != student._birthday) {
                        printDivitedDate(db2, svf, param, "BIRTHDAY", f, line, student._birthday); // 生年月日
                    }
                    if (!isP && isJorH) {
                        printDivitedDate(db2, svf, param, "LDATE", f, line, param._yuukouKigen); // 期限
                        final int addr1len = getMS932ByteLength(StringUtils.defaultString(student._addr1));
                        final int addr2len = getMS932ByteLength(StringUtils.defaultString(student._addr2));
                        if (addr1len > 40 || addr2len > 40) {
                            svf.VrsOutn("ADDRESS1" + f + "__3", line, student._addr1);
                            svf.VrsOutn("ADDRESS2" + f + "__3", line, student._addr2);
                        } else if (addr1len > 30 || addr2len > 30) {
                            svf.VrsOutn("ADDRESS1" + f + "__2", line, student._addr1);
                            svf.VrsOutn("ADDRESS2" + f + "__2", line, student._addr2);
                        } else {
                            svf.VrsOutn("ADDRESS1" + f, line, student._addr1);
                            svf.VrsOutn("ADDRESS2" + f, line, student._addr2);
                        }
                    } else {
                        final String addr = StringUtils.defaultString(student._addr1) + StringUtils.defaultString(student._addr2);
                        final int addrKeta = getMS932ByteLength(addr);
                        svf.VrsOutn("ADDRESS" + (addrKeta > 50 ? "5" : addrKeta > 30 ? "3" : "1") + f, line, addr);
                    }

                    // 発行者情報
                    final int remark1Keta = getMS932ByteLength(param._remark1);
                    svf.VrsOutn("SCHOOLADDRESS" + (remark1Keta > 50 ? "5" : remark1Keta > 30 ? "3" : "1") + f, line, param._remark1);  // 学校住所
                    svf.VrsOutn("SCHOOLNAME1" + f, line, param._schoolName);
                    svf.VrsOutn("JOBNAME" + f, line, param._jobName); // 職名
                    svf.VrsOutn("STAFFNAME" + f, line, param._principalName); // 職員名
                }
            }
            svf.VrEndPage();

            svf.VrSetForm(form2, 1);//用紙(裏)
            if (!isP && !isJorH) {
                svf.VrsOut("DUMMY", "\nDUMMY");
            } else {
                for (int li = 0; li < lineList.size(); li++) {
                    final int line = li + 1;
                    final List cols = (List) lineList.get(li);
                    for (int ci = 0; ci < cols.size(); ci++) {
                        final String f = ci == 1 ? "_2" : "";
                        svf.VrsOutn("SCHOOL_NAME" + f, line, param._schoolName);

                        if (null != param._yuukouKigen) {
                        	final String yuukouKigenFormat;
                        	if (param._isSeireki) {
                        		yuukouKigenFormat = KNJ_EditDate.h_format_SeirekiJP(param._yuukouKigen);
                        	} else {
                        		yuukouKigenFormat = KNJ_EditDate.h_format_JP(db2, param._yuukouKigen);
                        	}
                        	svf.VrsOutn("LIMIT" + f, line, yuukouKigenFormat);
                        }
                    }
                }
            }
            svf.VrEndPage();

            nonedata = true;
        }
    }

    private void printDivitedDate(final DB2UDB db2, final Vrw32alp svf, final Param param, final String field, final String f, final int line, final String date) {
    	if (param._isSeireki) {
    		svf.VrAttributen(field + "1" + f, line, "Edit=");
			svf.VrsOutn(field + "1" + f, line, KNJ_EditDate.h_format_SeirekiJP(date));
    	} else {
    		String[] arrTermSdate = null;
    		if (date != null) {
    			arrTermSdate = KNJ_EditDate.tate_format(KNJ_EditDate.h_format_JP(db2, date));
    		}
    		if (arrTermSdate != null) {
    			for (int i = 1; i <= 4; i++) {
    				svf.VrsOutn(field + String.valueOf(i) + f, line, arrTermSdate[i - 1]);
    			}
    		}
    	}
    }

    private static class Student {
        final String _schregno;
        final String _gradeName2;
        final String _hrClassName1;
        final String _attendno;
        final String _inoutcd;
        final String _entSchool;
        final String _name;
        final String _realName;
        final String _useRealName;
        final String _addr1;
        final String _addr2;
        final String _birthday;

        Student(
            final String schregno,
            final String gradeName2,
            final String hrClassName1,
            final String attendno,
            final String inoutcd,
            final String entSchool,
            final String name,
            final String realName,
            final String useRealName,
            final String addr1,
            final String addr2,
            final String birthday
        ) {
            _schregno = schregno;
            _gradeName2 = gradeName2;
            _hrClassName1 = hrClassName1;
            _attendno = attendno;
            _inoutcd = inoutcd;
            _entSchool = entSchool;
            _name = name;
            _realName = realName;
            _useRealName = useRealName;
            _addr1 = addr1;
            _addr2 = addr2;
            _birthday = birthday;
        }

        public static List getStudentList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    final String gradeName2 = rs.getString("GRADE_NAME2");
                    final String hrClassName1 = rs.getString("HR_CLASS_NAME1");
                    final String attendno = rs.getString("ATTENDNO");
                    final String inoutcd = rs.getString("INOUTCD");
                    final String entSchool = rs.getString("ENT_SCHOOL");
                    final String name = rs.getString("NAME");
                    final String realName = rs.getString("REAL_NAME");
                    final String useRealName = rs.getString("USE_REAL_NAME");
                    final String addr1 = rs.getString("ADDR1");
                    final String addr2 = rs.getString("ADDR2");
                    final String birthday = rs.getString("BIRTHDAY");
                    final Student student = new Student(schregno, gradeName2, hrClassName1, attendno, inoutcd, entSchool, name, realName, useRealName, addr1, addr2, birthday);
                    list.add(student);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCHREG_ADDRESS_MAX AS (  ");
            stb.append("      SELECT  ");
            stb.append("          SCHREGNO,  ");
            stb.append("          MAX(ISSUEDATE) AS ISSUEDATE  ");
            stb.append("      FROM  ");
            stb.append("          SCHREG_ADDRESS_DAT  ");
            stb.append("      GROUP BY  ");
            stb.append("          SCHREGNO  ");
            stb.append("      )  ");
            stb.append("  , SCHREG_ADDRESS AS (  ");
            stb.append("      SELECT DISTINCT ");
            stb.append("          T1.SCHREGNO, ");
            stb.append("          T1.ZIPCD, ");
            stb.append("          P1.PREF_CD, ");
            stb.append("          P1.PREF_NAME, ");
            stb.append("          T1.AREACD, ");
            stb.append("          N1.NAME1 AS AREA_NAME,  ");
            stb.append("          T1.ADDR1, ");
            stb.append("          T1.ADDR2, ");
            stb.append("          T1.TELNO ");
            stb.append("      FROM ");
            stb.append("          SCHREG_ADDRESS_DAT T1  ");
            stb.append("          INNER JOIN SCHREG_ADDRESS_MAX T2  ");
            stb.append("              ON  T2.SCHREGNO = T1.SCHREGNO  ");
            stb.append("              AND T2.ISSUEDATE = T1.ISSUEDATE  ");
            stb.append("          LEFT JOIN ZIPCD_MST Z1 ON Z1.NEW_ZIPCD = T1.ZIPCD  ");
            stb.append("          LEFT JOIN PREF_MST P1 ON P1.PREF_CD = SUBSTR(Z1.CITYCD,1,2)  ");
            stb.append("          LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'A020' AND N1.NAMECD2 = T1.AREACD ");
            stb.append("      )  ");
            stb.append(" SELECT T1.SCHREGNO,  ");
            stb.append("        L1.GRADE_NAME2,  ");
            stb.append("        L2.HR_CLASS_NAME1,  ");
            stb.append("        SMALLINT(T1.ATTENDNO) AS ATTENDNO,  ");
            stb.append("        VALUE(T2.INOUTCD,'') AS INOUTCD,  ");
            stb.append("        T2.ENT_SCHOOL,  ");
            stb.append("        T2.NAME,  ");
            stb.append("        T2.REAL_NAME,  ");
            stb.append("        (CASE WHEN T6.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS USE_REAL_NAME, ");
            stb.append("        ADDR.ADDR1,  ");
            stb.append("        ADDR.ADDR2,  ");
            stb.append("        T2.BIRTHDAY  ");
            stb.append(" FROM   SCHREG_REGD_DAT T1  ");
            stb.append("        INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO  ");
            stb.append("        LEFT JOIN SCHREG_NAME_SETUP_DAT T6 ON T6.SCHREGNO = T1.SCHREGNO  ");
            stb.append("             AND T6.DIV='05'  ");
            stb.append("        LEFT JOIN SCHREG_REGD_GDAT L1 ON L1.YEAR = T1.YEAR ");
            stb.append("             AND L1.GRADE = T1.GRADE  ");
            stb.append("        LEFT JOIN SCHREG_REGD_HDAT L2 ON L2.YEAR = T1.YEAR ");
            stb.append("             AND L2.SEMESTER = T1.SEMESTER  ");
            stb.append("             AND L2.GRADE = T1.GRADE ");
            stb.append("             AND L2.HR_CLASS = T1.HR_CLASS  ");
            stb.append("        LEFT JOIN SCHREG_ADDRESS ADDR ON ADDR.SCHREGNO = T1.SCHREGNO  ");
            stb.append(" WHERE ");
            stb.append("       T1.YEAR = '" + param._year + "' AND ");
            stb.append("       T1.SEMESTER = '" + param._semester + "' AND ");
            stb.append("       T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            stb.append(" ORDER BY  ");
            stb.append("        T1.GRADE, ");
            stb.append("        T1.HR_CLASS, ");
            stb.append("        T1.ATTENDNO ");
            return stb.toString();
        }
    }

    private static class Param {
        final String _year;
        final String _semester;
        final String _hrclass;
        final String _termSdate;
        final String _useAddrField2;
        final String _documentroot;
        final String[] _categorySelected; // 学籍番号
        private String _jobName;
        private String _imagePass;
        private String _extensionPhoto;
        private String _principalName;
        private String _schoolName;
        private String _remark1;
        private String _extensionStamp;
        private String _schoolKind;
        private boolean _hasCertifSchoolDatRecord = false;
        private String _z010Name1;
        private String _yuukouKigen;
        private String _knja143lExpireFlg;
        private boolean _isSeireki;

        public Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("GAKKI");
            _hrclass = request.getParameter("GRADE_HR_CLASS");
            _termSdate = request.getParameter("TERM_SDATE").replace('/','-'); //発行日
            _documentroot = request.getParameter("DOCUMENTROOT");
            _categorySelected = request.getParameterValues("category_selected");
            _useAddrField2 = request.getParameter("useAddrField2");
            _knja143lExpireFlg = request.getParameter("knja143lExpireFlg");
            setYuukouKigen(db2);

            setSchoolInfo(db2);

            setHeader(db2);
        }

        private void setYuukouKigen(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = "SELECT GRADE_CD, SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _hrclass.substring(0, 2) + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _schoolKind = rs.getString("SCHOOL_KIND");
    	        	if ("1".equals(_knja143lExpireFlg)) {
    	        		if (null != _termSdate) {
    	        			final Calendar cal = Calendar.getInstance();
    	        			cal.setTime(Date.valueOf(_termSdate));
    	        			final int endYear = cal.get(Calendar.YEAR) + (cal.get(Calendar.MONTH) <= Calendar.MARCH ? 0 : 1);
    	        			_yuukouKigen = String.valueOf(endYear) + "-03-31"; // 発行日年度の3/31
    	        		} else {
    	        			_yuukouKigen = String.valueOf(Integer.parseInt(_year) + 1) + "-03-31";
    	        		}
    	        	} else {
                        final String gradeCd = rs.getString("GRADE_CD");
                        if ("01".equals(gradeCd)) {
                            _yuukouKigen = String.valueOf(Integer.parseInt(_year) + 3) + "-03-31";
                        } else if ("02".equals(gradeCd)) {
                            _yuukouKigen = String.valueOf(Integer.parseInt(_year) + 2) + "-03-31";
                        } else {
                            _yuukouKigen = String.valueOf(Integer.parseInt(_year) + 1) + "-03-31";
                        }
                    }
                }
            } catch (Exception e) {
                log.error("setHeader name_mst error!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        public void setSchoolInfo(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            final String certifKindcd = "101";
            try {
                final String sql = "SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '" + certifKindcd + "'";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _jobName =  rs.getString("JOB_NAME");
                    _principalName = rs.getString("PRINCIPAL_NAME");
                    _schoolName = rs.getString("SCHOOL_NAME");
                    _remark1 = rs.getString("REMARK1"); // 学校住所
                    _hasCertifSchoolDatRecord = true;
                }
            } catch (Exception e) {
                log.error("setHeader name_mst error!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            if (!_hasCertifSchoolDatRecord) {
                //SVF出力
                //職名・職員名取得メソッド
                PreparedStatement ps2 = null;
                try {
                    StringBuffer stb = new StringBuffer();
                    stb.append("SELECT STAFFNAME, ");
                    stb.append("       (SELECT JOBNAME FROM JOB_MST T2 WHERE T2.JOBCD=T1.JOBCD) AS JOBNAME ");
                    stb.append("FROM   V_STAFF_MST T1 ");
                    stb.append("WHERE  YEAR='" + _year + "' AND JOBCD='0001' ");//学校長

                    ps2 = db2.prepareStatement(stb.toString());        //職名・職員名

                    rs = ps2.executeQuery();
                    while (rs.next()) {
                        _jobName  = rs.getString("JOBNAME");    //職名
                        _principalName = rs.getString("STAFFNAME");  //職員名
                    }
                } catch (Exception ex) {
                    log.error("setStaffJobName set error!", ex);
                } finally {
                    DbUtils.closeQuietly(null, ps2, rs);
                    db2.commit();
                }

                //学校名取得メソッド
                PreparedStatement ps3 = null;
                try {
                    final StringBuffer stb = new StringBuffer();
                    stb.append("SELECT SCHOOLNAME1,SCHOOLADDR1 ");
                    stb.append("FROM   SCHOOL_MST ");
                    stb.append("WHERE  YEAR='" + _year + "' ");

                    ps3 = db2.prepareStatement(stb.toString());        //学校名・学校住所

                    rs = ps3.executeQuery();
                    while (rs.next()) {
                        _schoolName = rs.getString("SCHOOLNAME1");    //学校名
                        _remark1 = rs.getString("SCHOOLADDR1");    //学校住所
                    }
                } catch (Exception ex) {
                    log.error("setSchoolName set error!", ex);
                } finally {
                    DbUtils.closeQuietly(null, ps3, rs);
                    db2.commit();
                }
            }
        }

        /** 事前処理 **/
        private void setHeader(final DB2UDB db2) {
            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;

            //  写真データ
            try {
                returnval = getinfo.Control(db2);
                _imagePass = returnval.val4;      //格納フォルダ
                _extensionPhoto = returnval.val5;      //拡張子
            } catch (Exception e) {
                log.error("setHeader set error!", e);
            }

            getinfo = null;
            returnval = null;

            //  名称マスタ（学校区分）
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                _extensionStamp = ".jpg";
                final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z010' AND NAMECD2='00'";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _z010Name1 = rs.getString("NAME1");
                }
            } catch (Exception e) {
                log.error("setHeader name_mst error!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            try {
                final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z012' AND NAMECD2 = '00'";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _isSeireki = "2".equals(rs.getString("NAME1"));
                }
            } catch (Exception e) {
                log.error("Z012 name_mst error!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }

}//クラスの括り
