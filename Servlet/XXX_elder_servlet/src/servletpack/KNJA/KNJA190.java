// kanji=漢字
/*
 * $Id: ae0ee5bc0c5edf4dd5dec5c07463330ef4a9d3fe $
 *
 * 作成日: 2003/11/17
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import static servletpack.KNJZ.detail.KNJ_EditEdit.getMS932ByteLength;

import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Semester;
import servletpack.KNJZ.detail.KnjDbUtils;

/*
 *
 *  学校教育システム 賢者 [学籍管理]
 *
 *                  ＜ＫＮＪＡ１９０＞  生徒住所のタックシール印刷
 *
 * 2003/11/17 nakamoto 印刷対象（保護者・生徒）追加
 * 2005/06/24 m-yama   フォーム変更に伴う修正(NO002)
 */

/**
 * <<住所のタックシール印刷>>。
 */
public class KNJA190 {
    private static final Log log = LogFactory.getLog(KNJA190.class);

    private boolean hasdata;

    private Param _param;

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();

        // svf設定
        final Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        svf.VrInit();                         //クラスの初期化
        svf.VrSetSpoolFileStream(outstrm);    //PDFファイル名の設定

        // ＤＢ接続
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch (Exception ex) {
            log.error("[KNJA190]DB2 open error!", ex);
            return;
        }

        try {
            _param = createParam(db2, request);

            hasdata = false;

            printMain(svf, db2);
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            //該当データ無しフォーム出力
            if (hasdata == false) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }

            // 終了処理
            db2.close();        // DBを閉じる
            svf.VrQuit();
            outstrm.close();    // ストリームを閉じる
        }

    }   //doGetの括り

    private List<Map<String, String>> getList(final DB2UDB db2) {
        boolean useGhr = false;
        boolean useFi = false;
        if ("2".equals(_param._hukusikiKirikae) && "1".equals(_param._useSpecial_Support_Hrclass)) {
        	useGhr = true;
        } else if ("2".equals(_param._hukusikiKirikae) && "1".equals(_param._useFi_Hrclass)) {
        	useFi = true;
        }

        //1:保護者 3:負担者 4:その他
        final String table = "4".equals(_param._output) ? "SCHREG_SEND_ADDRESS_DAT" : "3".equals(_param._output) ? "GUARDIAN_DAT" : "GUARDIAN_DAT";
        final String fldZipcd = "4".equals(_param._output) ? "SEND_ZIPCD" : "3".equals(_param._output) ? "GUARANTOR_ZIPCD" : "GUARD_ZIPCD";
        final String fldAddr1 = "4".equals(_param._output) ? "SEND_ADDR1" : "3".equals(_param._output) ? "GUARANTOR_ADDR1" : "GUARD_ADDR1";
        final String fldAddr2 = "4".equals(_param._output) ? "SEND_ADDR2" : "3".equals(_param._output) ? "GUARANTOR_ADDR2" : "GUARD_ADDR2";
        final String fldName = "4".equals(_param._output) ? "SEND_NAME" : "3".equals(_param._output) ? "GUARANTOR_NAME" : "GUARD_NAME";
        final String where = "4".equals(_param._output) ? " AND t1.DIV = '1' " : "";

        final StringBuffer sql = new StringBuffer();
        sql.append("WITH REGD AS ( ");
        sql.append("SELECT ");
        sql.append("    REGD.SCHREGNO ");
        if (useGhr) {
            sql.append("  , REGD.GHR_CD AS GRADE_HR_CLASS ");
            sql.append("  , REGD.GHR_ATTENDNO AS ATTENDNO ");
            sql.append("  , REGDH.GHR_NAME AS HR_NAME ");
        } else {
            sql.append("  , REGD.GRADE || REGD.HR_CLASS AS GRADE_HR_CLASS ");
            sql.append("  , REGD.ATTENDNO AS ATTENDNO ");
            sql.append("  , REGDH.HR_NAME AS HR_NAME ");
        }
        sql.append("    , BASE.NAME ");
        sql.append("    , BASE.NAME_KANA ");
        if (!useGhr) {
            sql.append("   , REGDG.SCHOOL_KIND ");
        } else {
            sql.append("   , '' AS SCHOOL_KIND ");
        }
        sql.append(" FROM ");
        if (useGhr) {
            sql.append("  SCHREG_REGD_GHR_DAT REGD ");
            sql.append("  LEFT JOIN SCHREG_REGD_GHR_HDAT REGDH ");
            sql.append("    ON REGDH.YEAR = REGD.YEAR AND REGDH.SEMESTER = REGD.SEMESTER AND REGD.GHR_CD = REGDH.GHR_CD ");
        } else if (useFi) {
        	sql.append("  SCHREG_REGD_FI_DAT REGD ");
        	sql.append("  LEFT JOIN SCHREG_REGD_FI_HDAT REGDH ");
        	sql.append("    ON REGDH.YEAR = REGD.YEAR AND REGDH.SEMESTER = REGD.SEMESTER AND REGD.GRADE = REGDH.GRADE AND REGD.HR_CLASS = REGDH.HR_CLASS ");
        } else {
            sql.append("  SCHREG_REGD_DAT REGD ");
            sql.append("  LEFT JOIN SCHREG_REGD_HDAT REGDH ");
            sql.append("    ON REGDH.YEAR = REGD.YEAR AND REGDH.SEMESTER = REGD.SEMESTER AND REGD.GRADE = REGDH.GRADE AND REGD.HR_CLASS = REGDH.HR_CLASS ");
        }
        sql.append("  INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
        if (!useGhr) {
            sql.append("  LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = REGD.YEAR AND REGDG.GRADE = REGD.GRADE ");
        }
        sql.append(" WHERE ");
        sql.append("     REGD.YEAR = '" + _param._year + "' ");
        sql.append(" AND REGD.SEMESTER = '" + _param._semester + "' ");
        if ("1".equals(_param._choice) || "3".equals(_param._choice)) {
            sql.append(" AND REGD.SCHREGNO IN " + _param._schregInState + " ");
        } else {
            if (useGhr) {
                sql.append(" AND REGD.GHR_CD IN " + _param._schregInState + " ");
            } else {
                sql.append(" AND REGD.GRADE || REGD.HR_CLASS IN " + _param._schregInState + " ");
            }
        }
        if ("1".equals(_param._grdDiv)) {
            sql.append(" AND NOT ((BASE.GRD_DIV IS NOT NULL AND BASE.GRD_DIV <> '4') AND GRD_DATE < '" + _param._ctrlDate + "' ) ");
        }
        sql.append(" ) ");

        sql.append("SELECT ");
        sql.append("     REGD.SCHREGNO ");
        if ("2".equals(_param._output)) {
            sql.append("   , VALUE(T1.ZIPCD,'') AS ZIPCD ");
            sql.append("   , T1.ADDR1 ");
            sql.append("   , T1.ADDR2 ");
            sql.append("   , REGD.NAME ");
        } else {
            sql.append("   , CASE WHEN T1." + fldAddr1 + " IS NOT NULL THEN VALUE(T1." + fldZipcd + ", '') ELSE T1_2.GUARD_ZIPCD END AS ZIPCD");
            sql.append("   , CASE WHEN T1." + fldAddr1 + " IS NOT NULL THEN T1." + fldAddr1 + "            ELSE T1_2.GUARD_ADDR1 END AS ADDR1 ");
            sql.append("   , CASE WHEN T1." + fldAddr1 + " IS NOT NULL THEN T1." + fldAddr2 + "            ELSE T1_2.GUARD_ADDR2 END AS ADDR2 ");
            sql.append("   , CASE WHEN T1." + fldAddr1 + " IS NOT NULL THEN T1." + fldName + "             ELSE T1_2.GUARD_NAME END AS NAME ");
        }
        sql.append("   , REGD.GRADE_HR_CLASS ");
        sql.append("   , REGD.ATTENDNO ");
        sql.append("   , REGD.HR_NAME ");
        sql.append("   , REGD.NAME AS NAME2 ");
        sql.append("   , REGD.NAME_KANA ");
        sql.append("   , REGD.SCHOOL_KIND ");
        sql.append("FROM ");
        sql.append("   REGD ");
        if ("2".equals(_param._output)) {
            sql.append("INNER JOIN (");
            sql.append("  SELECT ");
            sql.append("      SCHREGNO, ZIPCD, ADDR1, ADDR2 ");
            sql.append("  FROM ");
            sql.append("      SCHREG_ADDRESS_DAT W1 ");
            sql.append("  WHERE ");
            sql.append("       (W1.SCHREGNO, W1.ISSUEDATE) IN ( ");
            sql.append("                 SELECT SCHREGNO, MAX(ISSUEDATE) ");
            sql.append("                 FROM   SCHREG_ADDRESS_DAT W2 ");
            sql.append("                 WHERE  W2.ISSUEDATE <= '" + _param._semesterEdate + "' ");
            sql.append("                    AND (W2.EXPIREDATE IS NULL OR W2.EXPIREDATE >= '" + _param._semesterSdate + "') ");
            sql.append("                    AND W2.SCHREGNO IN (SELECT SCHREGNO FROM REGD) ");
            sql.append("                 GROUP BY SCHREGNO");
            sql.append("       ) ");
            sql.append(" ) T1 ON T1.SCHREGNO = REGD.SCHREGNO ");

    	} else {
            sql.append("    LEFT JOIN " + table + " T1 ON T1.SCHREGNO = REGD.SCHREGNO " + where);
            sql.append("    LEFT JOIN GUARDIAN_DAT T1_2 ON T1_2.SCHREGNO = REGD.SCHREGNO ");
            sql.append(" WHERE ");
            sql.append("     (T1." + fldAddr1 + " IS NOT NULL OR T1_2.GUARD_ADDR1 IS NOT NULL) ");
    	}
        sql.append(" ORDER BY ");
        sql.append("     REGD.GRADE_HR_CLASS ");
        if (_param._output2.equals("1")) {
        	sql.append(" , REGD.SCHREGNO");
        } else {
        	sql.append(" , REGD.ATTENDNO");
        }
    	log.fatal("set_detail sql = " + sql);
    	return KnjDbUtils.query(db2, sql.toString());
    }

    public static String getString(final Map<String, String> row, String field) {
        if (null == field || null == row || row.isEmpty()) {
            return null;
        }
        if (!row.containsKey(field)) {
        	field = field.toUpperCase();
            if (!row.containsKey(field)) {
                throw new IllegalStateException("no such field : " + field + " / " + row);
            }
        } else {
            if (!row.containsKey(field)) {
                throw new IllegalStateException("no such field : " + field + " / " + row);
            }
        }
        return row.get(field);
    }

    private void printMain(final Vrw32alp svf, final DB2UDB db2) {
    	final List<Map<String, String>> list = getList(db2);
    	if (list.size() == 0) {
    		return;
    	}

    	final String frmFile;
    	int rowMax = 6;
    	int colMax = 3;
    	if (_param._check3 != null) {
    		frmFile = "KNJA190_7.frm";
    	} else if (_param._isMusashinohigashi) {
    		frmFile = "KNJA190_3.frm";
    	} else if (_param._isHigashiosaka) {
    		frmFile = "KNJA190_4.frm";
    		colMax = 2;
    	} else if (_param._isRisshisha) {
    		frmFile = "KNJA190_5.frm";
    		colMax = 2;
    	} else if ("KNJA190A".equals(_param._prgId)) {
    		frmFile = "KNJA190A.frm";
    		rowMax = 8;
    	} else if (!"".equals(StringUtils.defaultString(_param._useFormName, ""))) {
    		frmFile = _param._useFormName;
    		if ("KNJA190_6.frm".equals(frmFile)) {
    			rowMax = 8;
    			colMax = 2;
    		}
    	} else {
    		frmFile = "KNJA190_2.frm";
    	}

        svf.VrSetForm(frmFile, 1);

        int row = Integer.parseInt(_param._poRow);    //行
        int col = Integer.parseInt(_param._poCol);    //列
        boolean hasData = false;
        String beforeGhrClass = list.get(0).get("GRADE_HR_CLASS");
        for (final Map<String, String> m : list) {
            final String rsZipcd = getString(m, "ZIPCD");
            final String rsAddr1 = getString(m, "ADDR1");
            final String rsAddr2 = getString(m, "ADDR2");
            final String rsName = getString(m, "NAME");
            final String rsNameKana = getString(m, "NAME_KANA");
            final String rsName2 = getString(m, "NAME2");
            final String rsHrname = getString(m, "HR_NAME");
            final String rsAttendno = getString(m, "ATTENDNO");
            final String rsSchregno = getString(m, "SCHREGNO");
            final String rsSchoolKind = getString(m, "SCHOOL_KIND");
            final String rsGradeHrClass = getString(m, "GRADE_HR_CLASS"); //クラス毎改ページ用に使用
            
            if ("2".equals(_param._output)) {
                if (!StringUtils.isBlank(rsZipcd) || null != rsAddr1 || null != rsAddr2 || null != rsName || null != rsHrname || null != rsAttendno) {
                    hasData = true;
                }
                if (_param._check != null && null != rsSchregno) {
                    hasData = true;
                }
            } else {
                if (!StringUtils.isBlank(rsZipcd) || null != rsAddr1 || null != rsAddr2 || null != rsName) {
                    hasData = true;
                }
                if (_param._check != null && (null != rsHrname || null != rsName2 || _param._isMusashinohigashi && "K".equals(rsSchoolKind) && null != rsNameKana)) {
                    hasData = true;
                }
            }
            
            //クラス毎に改ページ
             if (!beforeGhrClass.contentEquals(rsGradeHrClass)) {
                log.fatal("before:"+beforeGhrClass+" rsGradeHrClass:"+rsGradeHrClass);
            	if (hasData) {
                    svf.VrEndPage();
                    hasdata = true;
            	}
            	beforeGhrClass = rsGradeHrClass;
            	row = 1;
            	col = 1;
            }
            
            if(col > colMax){
                col = 1;
                row++;
                if (row > rowMax) {
                    if (hasData) {
                        svf.VrEndPage();
                        hasdata = true;
                    }
                    row = 1;
                }
            }
            if (!StringUtils.isBlank(rsZipcd)) {
                svf.VrsOutn("ZIPCODE"    + col, row, "〒" + rsZipcd); //郵便番号
            }
            final int check_len = getMS932ByteLength(rsAddr1);
            final int check_len2 = getMS932ByteLength(rsAddr2);
            if ("1".equals(_param._useAddrField2) && (check_len > 50 || check_len2 > 50)) {
                svf.VrsOutn("ADDRESS" + col + "_1_3" , row, rsAddr1);     //住所
                svf.VrsOutn("ADDRESS" + col + "_2_3" , row, rsAddr2);     //住所
            } else if (check_len > 40 || check_len2 > 40) {
                svf.VrsOutn("ADDRESS" + col + "_1_2" , row, rsAddr1);     //住所
                svf.VrsOutn("ADDRESS" + col + "_2_2" , row, rsAddr2);     //住所
            } else if (check_len > 0 || check_len2 > 0) {
                svf.VrsOutn("ADDRESS" + col + "_1_1" , row, rsAddr1);     //住所
                svf.VrsOutn("ADDRESS" + col + "_2_1" , row, rsAddr2);     //住所
            }

            if (!StringUtils.isBlank(rsName)) {
                if (_param._isMusashinohigashi) {
                    svf.VrsOutn("NAME" + col + "_1_2", row, rsName + "　" + _param._sama);  //名称
                } else {
                    svf.VrsOutn("NAME" + col + "_1", row, rsName + "　" + _param._sama);  //名称
                }
            }
            if ("2".equals(_param._output)) {
                if (_param._isMusashinohigashi) {
                } else {
                    final String hrname = StringUtils.defaultString(rsHrname);
                    final String attendno = StringUtils.defaultString(rsAttendno);
                    final String hrClassAttend = StringUtils.isBlank(hrname + attendno) ? "" : "(" + hrname + attendno + "番)";
                    final String name2;
                    if (_param._check != null){
                        name2 = StringUtils.defaultString(rsSchregno) + (StringUtils.isBlank(hrClassAttend) ? "" : (" " + hrClassAttend));
                    } else {
                        name2 = hrClassAttend;
                    }
                	if ("KNJA190_6.frm".equals(frmFile)) {
                        svf.VrsOutn("HR_NAME" + col , row, name2);
                	} else {
                        svf.VrsOutn("NAME" + col + "_2", row, name2); //名称
                	}
                }
            } else {
                if (_param._isMusashinohigashi) {
                    if (_param._check != null){
                        final String name2 = StringUtils.defaultString(_param._isMusashinohigashi && "K".equals(rsSchoolKind) ? rsNameKana : rsName2);
                        if (!StringUtils.isBlank(name2)) {
                            svf.VrsOutn("NAME" + col + "_2_2", row, name2 + "　" + _param._san); //名称
                        }
                    }
                } else {
                    if (_param._check != null){
                        //(××年××組××番 氏名)
                        final String hrname = StringUtils.defaultString(rsHrname);
                        final String name2 = StringUtils.defaultString(rsName2);
                        if (!StringUtils.isBlank(hrname + name2)) {
                        	if ("KNJA190_6.frm".equals(frmFile)) {
                                svf.VrsOutn("HR_NAME" + col, row, "(" + hrname + ")");
                                svf.VrsOutn("NAME" + col + "_2", row , name2 + "　" + _param._sama);
                        	} else {
                                svf.VrsOutn("NAME" + col + "_2", row ,"(" + hrname + " " + name2 + ")");
                        	}
                        }
                    }
                }
            }

            if(_param._check3 != null) {
                svf.VrsOutn("GUARD_NAME" + col + "_1", row ,"保護者・保証人　様");
            }
            col++;
        }
        if (hasData) {
            svf.VrEndPage();
            hasdata = true;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 73822 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    private static class Param {
        final String _year;
        final String _semester;
        final String _poRow;
        final String _poCol;
        final String _output; // 1:保護者 2:生徒 3:負担者 4:その他
        final String _output2;
        final String _check;
        final String _check3;
        final String _ctrlDate;
        final String _grdDiv;
        final String _choice; // 1:個人指定 2:クラス指定
        final String[] _classSelected;
        final String _prgId;
        final String _sama;
        final String _san;
        final String _schregInState;
        final String _semesterSdate;
        final String _semesterEdate;
        final String _useAddrField2;
        final boolean _isMusashinohigashi;
        final boolean _isHigashiosaka;
        final boolean _isRisshisha;
        final String _hukusikiKirikae;
        final String _useSpecial_Support_Hrclass;
        final String _knja190PreferSendAddress;
        final String _useFi_Hrclass;
        final String _useFormName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _knja190PreferSendAddress = request.getParameter("knja190PreferSendAddress");
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("GAKKI");
            _poRow = request.getParameter("POROW");
            _poCol = request.getParameter("POCOL");
            _prgId = request.getParameter("PRGID");
            final String output = request.getParameter("OUTPUT");
            if ("1".equals(_knja190PreferSendAddress) && "1".equals(output)) {
            	// 保護者を指定した場合、その他住所を優先する (その他住所がない場合、保護者住所を出力する)
            	_output = "4";
            } else {
            	_output = output;
            }
            _output2 = request.getParameter("OUTPUT2");
            if ("2".equals(_output)){
                _check = request.getParameter("CHECK2");              // 学籍番号印刷
            }else {
                _check = request.getParameter("CHECK1");              // 生徒名印刷
            }
            _check3 = request.getParameter("CHECK3");              // 保護者・保証人様出力
            _ctrlDate = request.getParameter("CTRL_DATE");   // 日付
            _grdDiv = request.getParameter("GRDDIV");   // 出力条件
            _choice = request.getParameter("CHOICE");
            //対象学籍番号の編集
            if ("3".equals(_choice)) {
                String[] _domiSelected = request.getParameterValues("category_name");   // 学籍番号
                List schListWk = getSchregnoListFromDomitory(db2, _domiSelected);
                _classSelected = (String[]) schListWk.toArray(new String[schListWk.size()]);   // 学籍番号
            } else {
                _classSelected = request.getParameterValues("category_name");   // 学籍番号
            }
            StringBuffer sbx = new StringBuffer();
            sbx.append("(");
            for (int ia = 0; ia < _classSelected.length; ia++){
                if (_classSelected[ia] == null) {
                    break;
                }
                if (ia > 0) {
                    sbx.append(",");
                }
                sbx.append("'");
                int i = _classSelected[ia].indexOf("-");
                if (-1 < i) {
                    sbx.append(_classSelected[ia].substring(0,i));
                } else {
                    sbx.append(_classSelected[ia]);
                }
                sbx.append("'");
            }
            sbx.append(")");
            _schregInState = sbx.toString();

            KNJ_Semester semester = new KNJ_Semester();                     //クラスのインスタンス作成
            KNJ_Semester.ReturnVal returnval = semester.Semester(db2, _year, _semester);
            _semesterSdate = returnval.val2;                                          //学期開始日
            _semesterEdate = returnval.val3;                                          //学期終了日
            _useAddrField2 = request.getParameter("useAddrField2");
            final String z010Name1 = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00'"));
            _isMusashinohigashi = "musashinohigashi".equals(z010Name1);
            _isHigashiosaka = "higashiosaka".equals(z010Name1);
            _isRisshisha    = "risshisha".equals(z010Name1);
            _hukusikiKirikae = request.getParameter("HUKUSIKI_KIRIKAE");
            _useSpecial_Support_Hrclass = request.getParameter("useSpecial_Support_Hrclass");
            _useFi_Hrclass = request.getParameter("useFi_Hrclass");
            _sama = "様";
            _san = "さん";
            _useFormName = request.getParameter("knja190Form");
        }

        private List getSchregnoListFromDomitory(final DB2UDB db2, final String[] selDomiCd) {
            List retList = new ArrayList();
            if (selDomiCd.length == 0) {
                return retList;
            }

            String dateStr = _ctrlDate.replace('/', '-');
            String SrchDomiCds = "";
            String delimStr = "";
            for (int ii = 0; ii < selDomiCd.length; ii++) {
                SrchDomiCds += delimStr + " '" + selDomiCd[ii] + "'";
                delimStr = ",";
            }

            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("  T1.SCHREGNO ");
            stb.append(" FROM ");
            stb.append("   SCHREG_REGD_DAT T1 ");
            stb.append("   INNER JOIN SCHREG_DOMITORY_HIST_DAT T2 ");
            stb.append("     ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("    AND ((DOMI_ENTDAY <= DATE('" + dateStr + "') AND DOMI_OUTDAY IS NULL) ");
            stb.append("          OR DATE('" + dateStr + "') BETWEEN DOMI_ENTDAY AND DOMI_OUTDAY) ");
            stb.append("   INNER JOIN SCHREG_BASE_MST T3 ");
            stb.append("     ON T3.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("   T1.YEAR = '" + _year + "' ");
            stb.append("   AND T1.SEMESTER = '" + _semester + "' ");
            stb.append("   AND T2.DOMI_CD IN (" + SrchDomiCds + ") ");
            stb.append(" ORDER BY ");
            if ("2".equals(_output2)) {
                stb.append(" T1.GRADE, ");
                stb.append(" T1.HR_CLASS, ");
                stb.append(" T1.ATTENDNO ");
            } else {
                stb.append(" T1.SCHREGNO ");
            }
            log.debug(" getSchregnoListFromDomitory sql = " + stb.toString());
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (!"".equals(StringUtils.defaultString(rs.getString("SCHREGNO"), ""))) {
                        retList.add(rs.getString("SCHREGNO"));
                    }
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            return retList;
        }
    }
}
