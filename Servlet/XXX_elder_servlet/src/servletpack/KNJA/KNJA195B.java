// kanji=漢字
/*
 * $Id: 1999f5ceb764f3312f26eba57a1e23472a9cbeda $
 *
 * 作成日: 2019/03/04
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/*
 *
 *  学校教育システム 賢者 [学籍管理]
 *
 *                  ＜ＫＮＪＡ１９５Ｂ＞  封筒の印刷
 */
public class KNJA195B extends HttpServlet {
    private static final Log log = LogFactory.getLog(KNJA195B.class);

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
            ps = db2.prepareStatement(sql);

            for (int i = 0; i < _param._classSelected.length; i++) {
                final String schregno = _param._classSelected[i];

                ps.setString(1, schregno);
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (_param._prtInfo.chkOutputEngLang()) {
                        //英語出力
                        svf.VrSetForm("KNJA195B_2.frm", 1);
                        final String name = "NAME: " + StringUtils.defaultString(rs.getString("NAME_ENG"));
						svf.VrsOut("NAME", name);
						svfUnderLine(svf, "NAME", name);
                        svf.VrsOut("CERT_NAME", _param._prtInfo.getCertName());

                        final String schoolName = "SCHOOL NAME: " + StringUtils.defaultString(_param._prtInfo._schoolNameEng);
						svf.VrsOut("SCHOOL_NAME", schoolName);
						svfUnderLine(svf, "SCHOOL_NAME", schoolName);
                        final String address =    "ADDRESS:     " + StringUtils.defaultString(_param._prtInfo._schoolAddr1Eng) + StringUtils.defaultString(_param._prtInfo._schoolAddr2Eng);
						svf.VrsOut("SCHOOL_ADDR", address);
						svfUnderLine(svf, "SCHOOL_ADDR", address);
                    } else {
                        //日本語出力
                        svf.VrSetForm("KNJA195B_1.frm", 1);
                        final int nlen = KNJ_EditEdit.getMS932ByteLength(rs.getString("NAME"));
                        final String nfield = nlen > 30 ? "3" : nlen > 20 ? "2" : "1";
                        svf.VrsOut("NAME" + nfield, rs.getString("NAME"));

                        final int mr_line = _param._prtInfo.getMrLine();
                        svf.VrsOutn("CIRCLE", mr_line, "〇");
                    }

                    svf.VrEndPage();

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

	private void svfUnderLine(final Vrw32alp svf, final String field, final String val) {
		svf.VrAttribute(field, "UnderLine=(0,1,1), Keta=" + String.valueOf(KNJ_EditEdit.getMS932ByteLength(val)));
	}

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        if (_param._prtInfo.chkOutputEngLang()) {
        	stb.append("SELECT NAME_ENG FROM SCHREG_BASE_MST WHERE SCHREGNO = ? ");
        } else {
        	stb.append("SELECT NAME FROM SCHREG_BASE_MST WHERE SCHREGNO = ? ");
        }
        return stb.toString();
    }

    private static class PrtDetailInfo {
    	final String _code;
    	final String _name1;
    	final String _name2;
    	final String _nameSpare1;
    	final String _nameSpare2;

    	PrtDetailInfo(
    	    	final String code,
    	    	final String name1,
    	    	final String name2,
    	    	final String nameSpare1,
    	    	final String nameSpare2
    			) {
        	_code = code;
        	_name1 = name1;
        	_name2 = name2;
        	_nameSpare1 = nameSpare1;
        	_nameSpare2 = nameSpare2;
    	}
    }

    private static class PrtInfo {
    	final String _output;
    	Map _prtMapInfo;
    	String _schoolZipCd;
        String _schoolNameEng;
        String _schoolAddr1Eng;
        String _schoolAddr2Eng;

    	PrtInfo (final String output) {
    		_output = output;
    		_prtMapInfo = new HashMap();
    	}

    	private void setPrtMap(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT * FROM NAME_MST WHERE NAMECD1 = 'A048' ";
                //log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                	final String code = rs.getString("NAMECD2");
                	final String name1 = rs.getString("NAME1");
                	final String name2 = rs.getString("NAME2");
                	final String nameSpare1 = rs.getString("NAMESPARE1");
                	final String nameSrare2 = rs.getString("NAMESPARE2");
                	PrtDetailInfo addwk = new PrtDetailInfo(code, name1, name2, nameSpare1, nameSrare2);
                	_prtMapInfo.put(code, addwk);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

        }

        private void setSchoolInfoEng(final DB2UDB db2, final String year, final String schoolCd, final String grade) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT * FROM SCHOOL_MST "
                		            + "WHERE YEAR = '" + year + "' AND SCHOOLCD = '" + schoolCd + "' AND "
                		            + " SCHOOL_KIND = (SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT "
                		            + "                WHERE YEAR = '" + year + "' AND GRADE = '" + grade + "' ) ";
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                	_schoolNameEng = rs.getString("SCHOOLNAME_ENG");
                	_schoolZipCd = rs.getString("SCHOOLZIPCD");
                	_schoolAddr1Eng = rs.getString("SCHOOLADDR1_ENG");
                	_schoolAddr2Eng = rs.getString("SCHOOLADDR2_ENG");
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

    	}

    	//英語出力チェック true:英語 false:日本語
    	private boolean chkOutputEngLang() {
    		PrtDetailInfo chkObj = (PrtDetailInfo)_prtMapInfo.get(_output);
    		boolean retbl = false;
    		if ("1".equals(chkObj._nameSpare1)) {
    			retbl = true;
    		}
    		return retbl;
    	}

    	private int getMrLine() {
    		PrtDetailInfo chkObj = (PrtDetailInfo)_prtMapInfo.get(_output);
    		return Integer.parseInt(chkObj._nameSpare2);
    	}

    	private String getCertName() {
    		PrtDetailInfo chkObj = (PrtDetailInfo)_prtMapInfo.get(_output);
    		return chkObj._name2;
    	}
    }
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 67622 $");
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
        final PrtInfo _prtInfo;
        final String _schoolCd;
        final String _grade;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _prgid = request.getParameter("PRGID");
            _kubun = request.getParameter("KUBUN");
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            for (int i = 0; i < _classSelected.length; i++) {
            _classSelected[i] = StringUtils.split(_classSelected[i], "-")[0];
            }
            _grade = request.getParameter("GRADE_HR_CLASS").substring(0,2);
            _schoolCd = request.getParameter("SCHOOLCD");
            _output = request.getParameter("OUTPUT");
            _prtInfo = new PrtInfo(_output);
            _prtInfo.setPrtMap(db2);
            _prtInfo.setSchoolInfoEng(db2, _ctrlYear, _schoolCd, _grade);
        }
    }
}
