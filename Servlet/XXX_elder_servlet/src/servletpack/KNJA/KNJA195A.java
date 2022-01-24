// kanji=漢字
/*
 * $Id: b879c154b2a57a84946e615291d2f70c9d22104e $
 *
 * 作成日: 2016/10/04
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/*
 *
 *  学校教育システム 賢者 [学籍管理]
 *
 *                  ＜ＫＮＪＡ１９５Ａ＞  封筒の印刷
 */
public class KNJA195A extends HttpServlet {
    private static final Log log = LogFactory.getLog(KNJA195A.class);
    
    private static final String PRGID_KNJG081A = "KNJG081A";

    private boolean nonedata;
    private Param _param;

    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws Exception
    {
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
            log.error("DB2 open error!", ex);
            return;
        }

        try {
            // パラメータの取得
            _param = createParam(db2, request);

            nonedata = false;       // 該当データなしフラグ(MES001.frm出力用)

            printMain(db2, svf);
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            //該当データ無しフォーム出力
            if (nonedata == false) {
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = sql();
            //log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);

            for (int i = 0; i < _param._classSelected.length; i++) {
                final String schregno = _param._classSelected[i];
                
                ps.setString(1, schregno);
                rs = ps.executeQuery();
                while (rs.next()) {
                	if (_param._isOsakatoin) {
                        svf.VrSetForm("KNJA195A_TOIN.frm", 1);
                    	if (!PRGID_KNJG081A.equals(_param._prgid) && "1".equals(_param._kubun)) {
                			final String schoolName = StringUtils.defaultString(rs.getString("SCHOOL_NAME"), "　　　　　") + "　学長殿";
                			if (schoolName.length() > 44) {
                				svf.VrsOut("COLLEGE_NAME3_1", schoolName);
                			} else if (schoolName.length() > 36) {
                				svf.VrsOut("COLLEGE_NAME2", schoolName);
                			} else {
                				svf.VrsOut("COLLEGE_NAME1", schoolName);
                			}
                    		
                    		svf.VrsOut("COLLEGE_F", "");
                    		
                    		if (!"2".equals(_param._output)) {
                    			final String facultyname = StringUtils.defaultString(rs.getString("FACULTYNAME"), "　　　　　") + "　御中";
                    			if (facultyname.length() > 34) {
                    				svf.VrsOut("FACULTY_NAME3_1", facultyname);
                    			} else if (facultyname.length() > 28) {
                    				svf.VrsOut("FACULTY_NAME2", facultyname);
                    			} else {
                    				svf.VrsOut("FACULTY_NAME1", facultyname);
                    			}
                    		}
                    	}
                    	
                    	final String name = "生徒氏名　" + StringUtils.defaultString(rs.getString("NAME"));
                    	final int nameKeta = KNJ_EditEdit.getMS932ByteLength(name);
                    	svf.VrsOut(nameKeta > 30 ? "NAME3" : nameKeta > 20 ? "NAME2" : "NAME1", name);
                    	svf.VrEndPage();
                    
                	} else {
                        svf.VrSetForm("KNJA195A.frm", 1);
                    	if ("1".equals(_param._kubun)) {
                    		if (null != rs.getString("SCHOOL_NAME")) {
                    			final String schoolName = rs.getString("SCHOOL_NAME");
                    			if (schoolName.length() > 9) {
                    				svf.VrsOut("COLLEGE_NAME3_1", schoolName);
                    			} else if (schoolName.length() > 6) {
                    				svf.VrsOut("COLLEGE_NAME2", schoolName);
                    			} else {
                    				svf.VrsOut("COLLEGE_NAME1", schoolName);
                    			}
                    		}
                    		
                    		svf.VrsOut("COLLEGE_F", "学長殿");
                    		
                    		if (!"2".equals(_param._output)) {
                    			if (null != rs.getString("FACULTYNAME")) {
                    				final String facultyname = rs.getString("FACULTYNAME");
                    				if (facultyname.length() > 10) {
                    					svf.VrsOut("FACULTY_NAME3_1", facultyname);
                    				} else if (facultyname.length() > 6) {
                    					svf.VrsOut("FACULTY_NAME2", facultyname);
                    				} else {
                    					svf.VrsOut("FACULTY_NAME1", facultyname);
                    				}
                    			}
                    			svf.VrsOut("FACULTY_F", "御中");
                    		}
                    		
                    		svf.VrsOut("CERTIF_NO", rs.getString("CERTIF_NO"));
                    		
                    		final String[] torokuDateSplit = StringUtils.split(rs.getString("TOROKU_DATE"), "-");
                    		if (null != torokuDateSplit && torokuDateSplit.length == 3) {
                    			final int year = Integer.parseInt(torokuDateSplit[0]);
                    			final int month = Integer.parseInt(torokuDateSplit[1]);
                    			final int dayOfMonth = Integer.parseInt(torokuDateSplit[2]);
                    			
                    			svf.VrsOut("FIELD1", String.valueOf(year));
                    			svf.VrsOut("FIELD2", String.valueOf(month));
                    			svf.VrsOut("FIELD3", String.valueOf(dayOfMonth));
                    		}
                    	}
                    	
                    	final String name = rs.getString("NAME");
                    	final int nameKeta = KNJ_EditEdit.getMS932ByteLength(name);
                    	svf.VrsOut(nameKeta > 30 ? "NAME3" : nameKeta > 20 ? "NAME2" : "NAME1", name);
                    	svf.VrEndPage();
                	}
                    
                    nonedata = true;
                }
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        if (PRGID_KNJG081A.equals(_param._prgid)) {
            stb.append(" SELECT ");
            stb.append("     BASE.SCHREGNO ");
            stb.append("     , BASE.NAME ");
            stb.append(" FROM SCHREG_BASE_MST BASE ");
            stb.append(" WHERE ");
            stb.append("     BASE.SCHREGNO = ? ");
        	
        } else if ("2".equals(_param._kubun)) {
            stb.append(" SELECT ");
            stb.append("     BASE.SCHREGNO ");
            stb.append("     , BASE.NAME ");
            stb.append(" FROM GRD_BASE_MST BASE ");
            stb.append(" WHERE ");
            stb.append("     BASE.SCHREGNO = ? ");
        } else {
            stb.append(" SELECT ");
            stb.append("     BASE.SCHREGNO ");
            stb.append("     , BASE.NAME ");
            stb.append("     , T1.STAT_CD ");
            stb.append("     , T2.SCHOOL_NAME ");
            stb.append("     , T1.FACULTYCD ");
            stb.append("     , T3.FACULTYNAME ");
            stb.append("     , T1.TOROKU_DATE ");
            stb.append("     , T4.REMARK1 AS CERTIF_NO ");
            stb.append(" FROM SCHREG_BASE_MST BASE ");
            stb.append(" INNER JOIN AFT_GRAD_COURSE_DAT T1 ON BASE.SCHREGNO = T1.SCHREGNO ");
            stb.append(" INNER JOIN COLLEGE_MST T2 ON T2.SCHOOL_CD = T1.STAT_CD ");
            stb.append(" LEFT JOIN COLLEGE_FACULTY_MST T3 ON T3.SCHOOL_CD = T1.STAT_CD ");
            stb.append("     AND T3.FACULTYCD = T1.FACULTYCD ");
            stb.append(" LEFT JOIN CERTIF_DETAIL_EACHTYPE_DAT T4 ON T4.YEAR = T1.YEAR ");
            stb.append("     AND T4.SCHREGNO = T1.SCHREGNO ");
            stb.append("     AND INT(T4.REMARK13) = T1.SEQ ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND T1.DECISION = '5' ");
            stb.append("     AND T1.SENKOU_KIND = '0' ");
            stb.append("     AND T1.SCHREGNO = ? ");
            stb.append(" ORDER BY ");
            stb.append("     T1.SEQ ");
        }
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 63103 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    private static class Param {
        final String _kubun;
        final String _ctrlYear;
        final String[] _classSelected;   // 学籍番号
        final String _output; // 学科名印字
        final String _prgid;
        final String _z010name1;
        final boolean _isOsakatoin;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _prgid = request.getParameter("PRGID");
            if (PRGID_KNJG081A.equals(_prgid)) {
            	_kubun = null;
            	_classSelected = getKNJG081Aschregno(db2, request);
            	_output = null;
            } else {
            	_kubun = request.getParameter("KUBUN");
            	_classSelected = request.getParameterValues("CLASS_SELECTED");
            	for (int i = 0; i < _classSelected.length; i++) {
            		_classSelected[i] = StringUtils.split(_classSelected[i], "-")[0];
            	}
            	_output = request.getParameter("OUTPUT");
            }
            _z010name1 = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2= '00' "));
            log.info(" z010 = " + _z010name1);
            _isOsakatoin = "osakatoin".equals(_z010name1);
        }
        
        private String[] getKNJG081Aschregno(final DB2UDB db2, final HttpServletRequest request) {
        	final List schregnoList = new ArrayList();
        	for (int i = 0; i < 1000; i++) {
        		final String schregno = request.getParameter("SCHREGNO-" + String.valueOf(i));
        		if (StringUtils.isBlank(schregno)) {
        			break;
        		}
        		final String count = request.getParameter("PRINT_CNT-" + String.valueOf(i));
        		if (!NumberUtils.isDigits(count)) {
        			continue;
        		}
        		for (int j = 1; j <= Integer.parseInt(count); j++) {
        			schregnoList.add(schregno);
        		}
        	}
        	final String[] schregnos = new String[schregnoList.size()];
        	schregnoList.toArray(schregnos);
        	log.info(" schregnos = " + ArrayUtils.toString(schregnos));
    		return schregnos;
    	}
    }
}
