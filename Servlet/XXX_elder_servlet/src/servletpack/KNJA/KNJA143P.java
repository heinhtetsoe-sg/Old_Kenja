// kanji=漢字
/*
 * $Id: fcfb4af24944e6a4646b7c52605281985d5ff094 $
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
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 *
 *	学校教育システム 賢者 [学籍管理]
 *
 *					＜ＫＮＪＡ１４３Ｐ＞  身分証明書（千代田九段）
 *
 **/

public class KNJA143P {

    private static final Log log = LogFactory.getLog(KNJA143P.class);
	private boolean _hasdata;

	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{
        log.fatal("$Revision: 70307 $ $Date: 2019-10-23 11:20:24 +0900 (水, 23 10 2019) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);

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
	        Param param = new Param(db2, request);

	        printMain(db2, svf, KnjDbUtils.query(db2, getStudentSql(param)), param);
		} catch (Exception ex) {
			log.error("DB2 prepareStatement set error!", ex);
		} finally {
	        //  該当データ無し
	        if (!_hasdata) {
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

    }

	/** 生徒情報 **/
	private String getStudentSql(final Param param) {
		final StringBuffer stb = new StringBuffer();

        stb.append(" WITH SCHREG_ADDRESS_MAX AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     MAX(T1.ISSUEDATE) AS ISSUEDATE ");
        stb.append(" FROM ");
        stb.append("     SCHREG_ADDRESS_DAT T1 ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SCHREGNO ");
        stb.append("     ) ");
        stb.append(" , SCHREG_ADDRESS AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.ISSUEDATE, ");
        stb.append("     T1.ADDR1, ");
        stb.append("     T1.ADDR2 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_ADDRESS_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_ADDRESS_MAX T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.ISSUEDATE = T1.ISSUEDATE ");
        stb.append("     ) ");

        stb.append("SELECT ");
        stb.append("       REGD.SCHREGNO, ");
        stb.append("       REGD.ATTENDNO, ");
        stb.append("       REGDH.HR_NAME, ");
        stb.append("       REGDG.SCHOOL_KIND, ");
        stb.append("       T5.COURSENAME, ");
        stb.append("       value(BASE.INOUTCD,'') as INOUTCD, ");
        stb.append("       BASE.NAME, ");
        stb.append("       BASE.REAL_NAME, ");
        stb.append("       (CASE WHEN T6.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS USE_REAL_NAME, ");
        stb.append("       TADDR.ADDR1, ");
        stb.append("       TADDR.ADDR2, ");
        for (int i = 1; i <= 7; i++) {
            stb.append("       ENVIR.FLG_" + i + " AS FLG_" + i + ", ");
            stb.append("       CASE WHEN ENVIR.FLG_" + i + " = '1' THEN NET" + i + "_1.STATION_NAME ELSE ENVIR.JOSYA_" + i + " END AS JOSYA_" + i + "_NAME, ");
            stb.append("       CASE WHEN ENVIR.FLG_" + i + " = '1' THEN NET" + i + "_1.LINE_NAME ELSE ENVIR.ROSEN_" + i + " END AS ROSEN_" + i + "_NAME, ");
            stb.append("       CASE WHEN ENVIR.FLG_" + i + " = '1' THEN NET" + i + "_2.STATION_NAME ELSE ENVIR.GESYA_" + i + " END AS GESYA_" + i + "_NAME, ");
        }
        stb.append("       BASE.BIRTHDAY ");
        stb.append("FROM   SCHREG_REGD_DAT REGD ");
        stb.append("       INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append("       LEFT JOIN COURSE_MST T5 ON T5.COURSECD = REGD.COURSECD ");
        stb.append("       LEFT JOIN SCHREG_NAME_SETUP_DAT T6 ON T6.SCHREGNO = REGD.SCHREGNO ");
        stb.append("            AND T6.DIV = '01' ");
        stb.append("       INNER JOIN SCHREG_ADDRESS TADDR ON TADDR.SCHREGNO = REGD.SCHREGNO ");
        stb.append("       LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
        stb.append("            AND REGDH.SEMESTER = REGD.SEMESTER ");
        stb.append("            AND REGDH.GRADE = REGD.GRADE ");
        stb.append("            AND REGDH.HR_CLASS = REGD.HR_CLASS ");
        stb.append("       LEFT JOIN SCHREG_ENVIR_DAT ENVIR ON ENVIR.SCHREGNO = REGD.SCHREGNO ");
        stb.append("       LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = REGD.YEAR AND REGDG.GRADE = REGD.GRADE ");
        for (int i = 1; i <= 7; i++) {
            stb.append("       LEFT JOIN STATION_NETMST NET" + i + "_1 ON NET" + i + "_1.STATION_CD = ENVIR.JOSYA_" + i + " ");
            stb.append("   AND NET" + i + "_1.LINE_CD = ENVIR.ROSEN_" + i + " ");
            stb.append("       LEFT JOIN STATION_NETMST NET" + i + "_2 ON NET" + i + "_2.STATION_CD = ENVIR.GESYA_" + i + " ");
            stb.append("   AND NET" + i + "_2.LINE_CD = ENVIR.ROSEN_" + i + " ");
        }
        stb.append(" WHERE  REGD.YEAR = '" + param._year + "' AND ");
        stb.append("       REGD.SEMESTER = '" + param._gakki + "' ");
        stb.append("       AND REGD.SCHREGNO IN (" + param._findschreg + ") ");
        stb.append(" ORDER BY ");
        if(param._useFormFlg) {
            if("1".equals(param._sortDiv)) {
                //組番号順
                stb.append("       REGD.HR_CLASS, ");
                stb.append("       REGD.ATTENDNO  ");
            }else {
                //学籍番号順
                stb.append("       REGD.SCHREGNO ");
            }
        }else {
            stb.append("       REGD.GRADE, ");
            stb.append("       REGD.HR_CLASS, ");
            stb.append("       REGD.ATTENDNO ");
        }

        return stb.toString();
	}

	private void printMain(final DB2UDB db2, final Vrw32alp svf, final List dataListAll, final Param param) {
	    final int maxLine = 5;
	    final List pageList = getPageList(dataListAll, maxLine);

	    final String form = param._useFormFlg ? param._useFormName + ".frm" : "KNJA143P.frm";

        svf.VrSetForm(form, 1);

	    for (int pi = 0; pi < pageList.size(); pi++) {

	        final List dataList = (List) pageList.get(pi);
	        for (int j = 0; j < dataList.size(); j++) {
	            final int line = j + 1;

	            final Map student = (Map) dataList.get(j);

                printOmote(db2, param, svf, line, student);
	            printUra(param, svf, line, student);
	        }
	        svf.VrEndPage();
	        _hasdata = true;
	    }
	}

    private void printOmote(final DB2UDB db2, final Param param, final Vrw32alp svf, final int line, final Map student) {
        final String name = "1".equals(KnjDbUtils.getString(student, "USE_REAL_NAME")) || "1".equals(param._certifPrintRealName) ? KnjDbUtils.getString(student, "REAL_NAME") : KnjDbUtils.getString(student, "NAME");
        final String schregno = KnjDbUtils.getString(student, "SCHREGNO");

        svf.VrsOutn("TITLE", line, "生徒証"); // タイトル
        // 顔写真
        final String photoPath = param._documentroot + "/" + param._ctrlMstImageDir + "/" + "P" + schregno + "." + param._ctrlMstExtension;
        if (new File(photoPath).exists()) {
            svf.VrsOutn("PHOTO_BMP", line, photoPath);//顔写真
        }

        if (param._z010.equals("nagisa")) {
            svf.VrsOutn("SCHREGNO", line, String.valueOf(Integer.parseInt(schregno))); // 学籍番号
        } else {
            svf.VrsOutn("SCHREGNO", line, schregno); // 学籍番号
        }
        if (null != param._termSdate) {
            svf.VrsOutn("SDATE", line, KNJ_EditDate.h_format_JP(db2, param._termSdate)); // 発行日
        }
        if (null != param._termEdate) {
            svf.VrsOutn("FDATE", line, KNJ_EditDate.h_format_JP(db2, param._termEdate)); // 有効期限日
        }
        final String attendno = NumberUtils.isDigits(KnjDbUtils.getString(student, "ATTENDNO")) ? String.valueOf(Integer.parseInt(KnjDbUtils.getString(student, "ATTENDNO"))) + "番" : StringUtils.defaultString(KnjDbUtils.getString(student, "ATTENDNO"));
        final String hrNameAttendno = StringUtils.defaultString(KnjDbUtils.getString(student, "HR_NAME")) + " " + attendno;
        svf.VrsOutn("HR_NAME", line, hrNameAttendno); // 学科、クラス、出席番号
        final int namelength = KNJ_EditEdit.getMS932ByteLength(name);
        final String nameidx = namelength > 24 ? "2" : "";
        svf.VrsOutn("NAME" + nameidx, line, name); // 生徒氏名
        final String birthday = KnjDbUtils.getString(student, "BIRTHDAY");
        if (null != birthday) {
            svf.VrsOutn("BIRTHDAY", line, KNJ_EditDate.h_format_JP(db2, birthday) + "生"); // 生年月日・年齢
        }
        if (null != param._stampPath) {
            svf.VrsOutn("STAMP_BMP", line, param._stampPath); //
        }

        final String schoolKind = KnjDbUtils.getString(student, "SCHOOL_KIND");
        final String coursename = "J".equals(schoolKind) ? "前期課程" : "H".equals(schoolKind) ? "後期課程" : "";
        svf.VrsOutn("COURSE" + (KNJ_EditEdit.getMS932ByteLength(coursename) <= 14 ? "1" : "2"), line, coursename); // 課程 義務課程

        param.setCertifSchoolDat(schoolKind);
        final int ketaSchooladdress = KNJ_EditEdit.getMS932ByteLength(param._certifSchoolDatremark1);
        svf.VrsOutn("SCHOOLADDRESS" + (ketaSchooladdress <= 34 ? "" : ketaSchooladdress <= 40 ? "2" : ketaSchooladdress <= 50 ? "3" : "4"), line, param._certifSchoolDatremark1); // 学校所在地

        final String barCode = param._useFormFlg ? schregno.substring(1, 8) : schregno;
        svf.VrsOutn("BARCODE", line, barCode); // バーコード

        svf.VrsOutn("SCHOOLPHONE", line, param._certifSchoolDatremark3); // 学校電話番号
        svf.VrsOutn("SCHOOLNAME", line, param._certifSchoolDatSchoolName); // 学校名
        svf.VrsOutn("STAFFNAME", line, param._certifSchoolDatPrincipalName); // 職員氏名
        svf.VrsOutn("JOBNAME", line, param._certifSchoolDatJobName); // 職名

        final String addr1 = KnjDbUtils.getString(student, "ADDR1");
        final int ketaAddr1 = KNJ_EditEdit.getMS932ByteLength(addr1);
        svf.VrsOutn("ADDRESS1_" + (ketaAddr1 <= 40 ? "1" : ketaAddr1 <= 50 ? "2" : "3"), line, addr1); // 住所

        final String addr2 = KnjDbUtils.getString(student, "ADDR2");
        final int ketaAddr2 = KNJ_EditEdit.getMS932ByteLength(addr2);
        svf.VrsOutn("ADDRESS2_" + (ketaAddr2 <= 40 ? "1" : ketaAddr2 <= 50 ? "2" : "3"), line, addr2); // 住所

        if (null != param._schoolLogoPath) {
            svf.VrsOutn("SCHOOLLOGO", line, param._schoolLogoPath);
        }
    }

    private void printUra(final Param param, final Vrw32alp svf, final int line, final Map student) {
        //電車、バス、その他で分類分けして、個々に乗り始めと最終下車駅でまとめる。
        //※電車だけは乗り継ぎを別途まとめる。
        List raillist = new ArrayList();
        List buslist = new ArrayList();
        List otherlist = new ArrayList();

        //個々のデータをlistに分ける。
        summaryroute(raillist, buslist, otherlist, student);

        //list毎に乗車/下車/乗り継ぎを取得。
        RouteClass rwk1 = new RouteClass();
        getroutestr(raillist, rwk1);

        RouteClass rwk2 = new RouteClass();
        getroutestr(buslist, rwk2);

        RouteClass rwk3 = new RouteClass();
        getroutestr(otherlist, rwk3);

        String section1_1 = rwk1._startstation;
        final int ketaSection1_1 = KNJ_EditEdit.getMS932ByteLength(section1_1);
        svf.VrsOutn("SECTION1_1_" + (ketaSection1_1 <= 10 ? "1" : ketaSection1_1 <= 16 ? "2" : "3"), line, section1_1); // 通学区間1(乗車)

        String section1_2 = rwk1._endstation;
        final int ketaSection1_2 = KNJ_EditEdit.getMS932ByteLength(section1_2);
        svf.VrsOutn("SECTION1_2_" + (ketaSection1_2 <= 10 ? "1" : ketaSection1_2 <= 16 ? "2" : "3"), line, section1_2); // 通学区間1(下車)

        final String section2_1 = rwk2._startstation;
        final int ketaSection2_1 = KNJ_EditEdit.getMS932ByteLength(section2_1);
        svf.VrsOutn("SECTION2_1_" + (ketaSection2_1 <= 10 ? "1" : ketaSection2_1 <= 16 ? "2" : "3"), line, section2_1); // 通学区間2(乗車)

        final String section2_2 = rwk2._endstation;
        final int ketaSection2_2 = KNJ_EditEdit.getMS932ByteLength(section2_2);
        svf.VrsOutn("SECTION2_2_" + (ketaSection2_2 <= 10 ? "1" : ketaSection2_2 <= 16 ? "2" : "3"), line, section2_2); // 通学区間2(下車)

        final String transfer = rwk1._routecorp;
        final int ketaTransfer = KNJ_EditEdit.getMS932ByteLength(transfer);
        svf.VrsOutn("TRANSFER" + (ketaTransfer <= 20 ? "1" : "2"), line, transfer); // 乗り換え

        final String section3_1 = rwk3._startstation;
        final int ketaSection3_1 = KNJ_EditEdit.getMS932ByteLength(section3_1);
        svf.VrsOutn("SECTION3_1_" + (ketaSection3_1 <= 10 ? "1" : ketaSection3_1 <= 16 ? "2" : "3"), line, section3_1); // 通学区間

        final String section3_2 = rwk3._endstation;
        final int ketaSection3_2 = KNJ_EditEdit.getMS932ByteLength(section3_2);
        svf.VrsOutn("SECTION3_2_" + (ketaSection3_2 <= 10 ? "1" : ketaSection3_2 <= 16 ? "2" : "3"), line, section3_2); // 通学区間

    }

    private void summaryroute(final List raillist, final List buslist, final List otherlist, final Map student) {
        for (int cnt = 1;cnt <=7;cnt++) {
            if (KnjDbUtils.getString(student, "JOSYA_" + cnt + "_NAME") != null) {
                RouteClass rwk = new RouteClass(KnjDbUtils.getString(student, "JOSYA_" + cnt + "_NAME"), KnjDbUtils.getString(student, "GESYA_" + cnt + "_NAME"), KnjDbUtils.getString(student, "ROSEN_" + cnt + "_NAME"));
                String chkflg = KnjDbUtils.getString(student, "FLG_" + cnt);
                if ("5".equals(chkflg) || "1".equals(chkflg)) {
                    raillist.add(rwk);
                } else if ("4".equals(chkflg)) {
                    buslist.add(rwk);
                } else if ("2".equals(chkflg)) {
                    otherlist.add(rwk);
                }
            }
        }

    }

    void getroutestr(final List chklist, final RouteClass rcls) {
        RouteClass rwk = null;

        if (chklist.size() > 0) {
            rwk = (RouteClass)chklist.get(0);
            rcls._startstation = rwk._startstation;
            rwk = (RouteClass)chklist.get(chklist.size() - 1);
            rcls._endstation = rwk._endstation;
            if (chklist.size() > 1) {
                for (int cnt = 1;cnt < chklist.size();cnt++) {
                   if (cnt > 1) {
                       rcls._routecorp += "、";
                   }
                   rwk = (RouteClass)chklist.get(cnt);
                   rcls._routecorp += rwk._startstation;
                }
            }
        }
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

    private class RouteClass {
        String _startstation;
        String _endstation;
        String _routecorp;
        RouteClass() {
            _startstation = "";
            _endstation = "";
            _routecorp = "";
        }
        RouteClass(final String startstation, final String endstation, final String routecorp) {
            _startstation = startstation;
            _endstation = endstation;
            _routecorp = routecorp;
        }
    }
	private static class Param {
        final String _year;
	    final String _gakki;
        private final String _disp;
        private final String[] _gradeHrclass;
	    final String[] _categorySelected;
	    final String _termSdate;
        final String _termEdate;
	    final String _documentroot;
	    final String _certifPrintRealName;
	    private String _stampPath;
	    private String _ctrlMstImageDir;
	    private String _ctrlMstExtension;
	    private String _101certifSchoolDatJobName;
	    private String _101certifSchoolDatPrincipalName;
	    private String _101certifSchoolDatSchoolName;
	    private String _101certifSchoolDatremark1;
	    private String _101certifSchoolDatremark3;
	    private String _102certifSchoolDatJobName;
	    private String _102certifSchoolDatPrincipalName;
	    private String _102certifSchoolDatSchoolName;
	    private String _102certifSchoolDatremark1;
	    private String _102certifSchoolDatremark3;
	    private String _certifSchoolDatJobName;
	    private String _certifSchoolDatPrincipalName;
	    private String _certifSchoolDatSchoolName;
	    private String _certifSchoolDatremark1;
	    private String _certifSchoolDatremark3;
	    private String _stampExtension;
        private final String _useAddrField2;
        private String _z010;

        private final String _findgr_hr;
        private final String _findschreg;
        private final String _useFormName;
        private final boolean _useFormFlg;
        private final String _sortDiv;
        private String _schoolLogoPath;

        public Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");                        //年度
            _gakki = request.getParameter("GAKKI");                       //学期
            _disp = request.getParameter("DISP");
            _termSdate = StringUtils.replace(request.getParameter("TERM_SDATE"), "/", "-"); // 発行日
            _termEdate = StringUtils.replace(request.getParameter("TERM_EDATE"), "/", "-"); // 有効期限
            _documentroot = request.getParameter("DOCUMENTROOT");
            _certifPrintRealName = request.getParameter("certifPrintRealName");

            _useFormName = request.getParameter("useFormNameA143P");
            _useFormFlg = !"".equals(_useFormName) ? true : false;
            _sortDiv = request.getParameter("SORT_DIV");

            if ("2".equals(_disp)) {
                _gradeHrclass = new String[1];
                _gradeHrclass[0] = request.getParameter("GRADE_HR_CLASS");    //学年＋組
                _findgr_hr = conv_arystr_to_str(_gradeHrclass, ",");
                _categorySelected = request.getParameterValues("CATEGORY_SELECTED"); //学籍番号
            } else {
                _gradeHrclass = request.getParameterValues("CATEGORY_SELECTED"); //学籍番号
                _findgr_hr = conv_arystr_to_str(_gradeHrclass, ",");
                _categorySelected = getSchregnos(db2);
            }
            _findschreg = conv_arystr_to_str(_categorySelected, ",", "-", 1);

//            // 学籍番号の指定
//            _categorySelected = request.getParameterValues("category_selected"); // 学籍番号
//            if (null != _categorySelected) {
//            	for (int i = 0; i < _categorySelected.length; i++) {
//            		_categorySelected[i] = StringUtils.split(_categorySelected[i], "-")[1];
//            	}
//            }

            loadCertifSchoolDat(db2);
            _useAddrField2 = request.getParameter("useAddrField2");

            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;

            //  写真データ
            try {
                returnval = getinfo.Control(db2);
                _ctrlMstImageDir = returnval.val4;      //格納フォルダ
                _ctrlMstExtension = returnval.val5;      //拡張子
            } catch (Exception e) {
                log.error("setHeader set error!", e);
            }

            //  名称マスタ（学校区分）
            _z010 = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                _stampExtension = ".bmp";
                final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00'";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _z010 = rs.getString("NAME1");
                    if ("jisyukan".equals(_z010) || "tokyoto".equals(_z010)) {
                        _stampExtension = ".jpg";
                    }
                }
            } catch (Exception e) {
                log.error("setHeader name_mst error!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            getinfo = null;
            returnval = null;

            _stampPath = _documentroot + "/" + _ctrlMstImageDir + "/" + "SCHOOLSTAMP" + _stampExtension; // 学校印;
            final boolean stampImageExists = new File(_stampPath).exists(); // 学校長印データ存在チェック用
            log.info(" stamp = " + _stampPath + " exists? " + stampImageExists);
            if (!stampImageExists) {
                _stampPath = null;
            }

            _schoolLogoPath = _documentroot + "/" + _ctrlMstImageDir + "/" + "SCHOOLLOGO.jpg";
            final boolean logoImageExists = new File(_schoolLogoPath).exists();
            if (!logoImageExists) {
                _schoolLogoPath = null;
            }

        }

        private String conv_arystr_to_str(final String[] strary, final String sep) {
            return conv_arystr_to_str(strary, sep, "", 0);
        }
        private String conv_arystr_to_str(final String[] strary, final String sep, final String delim, final int cutno) {
            String convgr_hr = "";
            String sepwk = "";
            if (null != strary) {
            	for (int ii = 0;ii < strary.length;ii++) {
            		String cutwkstr[];
            		int idx = 0;
            		if (!"".equals(delim) && cutno > 0) {
            			cutwkstr = StringUtils.split(strary[ii], delim);
            			idx = cutno - 1;
            		} else {
            			cutwkstr = new String[1];
            			cutwkstr[0] = strary[ii];
            		}
            		convgr_hr += sepwk + "'" + cutwkstr[idx] + "'";
            		sepwk = sep;
            	}
            }
            return convgr_hr;
        }

        private String[] getSchregnos(final DB2UDB db2) {
            String[] retstrlist;
            List retwklist = new ArrayList();

            String schregno_get_sql = "SELECT SCHREGNO FROM SCHREG_REGD_DAT WHERE YEAR = '"+_year+"' AND SEMESTER = '"+_gakki+"' AND GRADE || HR_CLASS IN ("+_findgr_hr+") ";
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(schregno_get_sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retwklist.add(rs.getString("SCHREGNO"));
                }
            } catch (Exception ex) {
                log.error("setSvfout set error!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            retstrlist = (String[])retwklist.toArray(new String[retwklist.size()]);
            return retstrlist;
        }

        public void loadCertifSchoolDat(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = "SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD IN ('101', '102') ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                	if ("101".equals(rs.getString("CERTIF_KINDCD"))) {
                        _101certifSchoolDatJobName =  rs.getString("JOB_NAME");
                        _101certifSchoolDatPrincipalName = rs.getString("PRINCIPAL_NAME");
                        _101certifSchoolDatSchoolName = rs.getString("SCHOOL_NAME");
                        _101certifSchoolDatremark1 = rs.getString("REMARK1"); // 学校住所
                        _101certifSchoolDatremark3 = rs.getString("REMARK3"); // 学校電話番号
                	} else if ("102".equals(rs.getString("CERTIF_KINDCD"))) {
                        _102certifSchoolDatJobName =  rs.getString("JOB_NAME");
                        _102certifSchoolDatPrincipalName = rs.getString("PRINCIPAL_NAME");
                        _102certifSchoolDatSchoolName = rs.getString("SCHOOL_NAME");
                        _102certifSchoolDatremark1 = rs.getString("REMARK1"); // 学校住所
                        _102certifSchoolDatremark3 = rs.getString("REMARK3"); // 学校電話番号
                	}
                }
            } catch (Exception e) {
                log.error("setHeader name_mst error!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        public void setCertifSchoolDat(final String schoolKind) {
        	String certifKindcd = null;
            if ("J".equals(schoolKind)) {
                certifKindcd = "102";
            } else if ("H".equals(schoolKind)) {
                certifKindcd = "101";
            }
        	if ("101".equals(certifKindcd)) {
        		_certifSchoolDatJobName = _101certifSchoolDatJobName;
                _certifSchoolDatPrincipalName = _101certifSchoolDatPrincipalName;
                _certifSchoolDatSchoolName = _101certifSchoolDatSchoolName;
                _certifSchoolDatremark1 = _101certifSchoolDatremark1;
                _certifSchoolDatremark3 = _101certifSchoolDatremark3;
        	} else if ("102".equals(certifKindcd)) {
        		_certifSchoolDatJobName = _102certifSchoolDatJobName;
                _certifSchoolDatPrincipalName = _102certifSchoolDatPrincipalName;
                _certifSchoolDatSchoolName = _102certifSchoolDatSchoolName;
                _certifSchoolDatremark1 = _102certifSchoolDatremark1;
                _certifSchoolDatremark3 = _102certifSchoolDatremark3;
        	}
        }
	}

}//クラスの括り
