// kanji=漢字
/*
 * $Id: 4c5c309e6aff65c1c70827335867bd280022d947 $
 *
 * 作成日: 2017/08/14 10:24:21 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: 4c5c309e6aff65c1c70827335867bd280022d947 $
 */
public class KNJA143O {

    private static final Log log = LogFactory.getLog("KNJA143O.class");

    private boolean _hasData;

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
            log.fatal("$Revision: 71179 $");
            KNJServletUtils.debugParam(request, log);

            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
            final Param param = new Param(db2, request);

            _param = param;

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

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes( "MS932").length;
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
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

    private void vrImageOut(final Vrw32alp svf, final String field, final String filename) {
    	final String path = _param.getImageFilePath(filename);
    	if (null != path) {
    		svf.VrsOut(field, path);
    	}
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final List studentList = Student.getStudentList(db2, _param);

        if ("1".equals(_param._printPage)) {
            final String knja143oImage1filename = "2".equals(_param._imageColor) ? "knja143oImage1red.jpg" : "knja143oImage1blue.jpg";
            final String knja143oImage2filename = "2".equals(_param._imageColor) ? "knja143oImage2red.jpg" : "knja143oImage2blue.jpg";
            final String schoolstampHfilename = "SCHOOLSTAMP_H.bmp";
            final String schoollogoHfilename = "SCHOOLLOGO_H.jpg";

            final String form = "KNJA143O_1.frm";

            for (int j = 0; j < studentList.size(); j++) {
            	final Student student = (Student) studentList.get(j);

                svf.VrSetForm(form, 1);

                vrImageOut(svf, "SCHHOL_NAME_LOGO", knja143oImage1filename);
                vrImageOut(svf, "PHOTO_BMP", "P" + student._schregno + "." + _param._extension); // 顔写真
                vrImageOut(svf, "SCHOOL_LOGO", schoollogoHfilename); // 顔写真
                vrImageOut(svf, "STAMP_BMP", schoolstampHfilename); // 校印
                vrImageOut(svf, "UNDER_LINE", knja143oImage2filename);

                final String schoolAddr = StringUtils.defaultString(append(prepend("〒", _param._schoolZipcd), "　")) + StringUtils.defaultString(_param._addr1);
				svf.VrsOut("SCHOOL_ADDR" + (KNJ_EditEdit.getMS932ByteLength(schoolAddr) <= 44 ? "1" : "2"), schoolAddr); // 学校住所

                final String addr = StringUtils.defaultString(student._addr1) + StringUtils.defaultString(student._addr2);
                final int ketaAddr = KNJ_EditEdit.getMS932ByteLength(addr);
                svf.VrsOut("ADDR" + (ketaAddr <= 46 ? "1" : ketaAddr <= 60 ? "2" : ketaAddr <= 70 ? "3" : "4"), addr); // 住所

                final int ketaName = KNJ_EditEdit.getMS932ByteLength(student._name);
                svf.VrsOut("NAME" + (ketaName <= 20 ? "1" : ketaName <= 30 ? "2" : "3"), student._name); // 氏名

                svf.VrsOut("PRINT_DATE", KNJ_EditDate.h_format_SeirekiJP(_param._issueDate) + "発行　" + KNJ_EditDate.h_format_SeirekiJP(_param._limitDate) + "まで有効"); // 発行日
                svf.VrsOut("SCHREGNO", student._schregno); // 学籍番号
                svf.VrsOut("GRADE_COURSE", "第" + digitsToZenkaku(String.valueOf(Integer.parseInt(student._gradeCd))) + "学年　" + StringUtils.defaultString(student._majorname)); // 学年コース
                svf.VrsOut("BIRTHDAY", append(KNJ_EditDate.h_format_SeirekiJP(student._birthday), "生")); // 生年月日
                svf.VrsOut("SCHOOL_NAME", StringUtils.defaultString(_param._schoolname) + StringUtils.defaultString(_param._jobname)); // 学校名
                svf.VrsOut("BARCODE", student._schregno); // バーコード(学籍番号)

                svf.VrEndPage();
                _hasData = true;
            }

        } else if ("2".equals(_param._printPage)) {
        	if ("1".equals(_param._page2Div)) {
                final String form = "KNJA143O_2.frm";

                final int tokenKetaMax1 = 20;
                final int tokenKetaMax2 = 26;
                final int tokenKetaMax3 = 30;

                final int rowMax = 5; // 最大行
                final int colMax = 2; // 最大列
                int row = _param._porow;
                int col = _param._pocol;
                boolean start = true;
                boolean newpage = true;
                for (int sti = 0; sti < studentList.size(); sti++) {
                	final Student student = (Student) studentList.get(sti);

                	if (newpage) {
                		svf.VrSetForm(form, 1);
                		if (start) {
                			// 印字しない範囲を空白
                			if (null != _param._whitespacePath) {
                				for (int sr = 1; sr <= _param._porow; sr++) {
                					for (int sc = 1; sc <= (sr == _param._porow ? _param._pocol - 1 : colMax); sc++) {
                						svf.VrsOutn("whitespace" + String.valueOf(sc), sr, _param._whitespacePath);
                					}
                				}
                			}
                			start = false;
                		}
                	}
                	newpage = false;

        			svf.VrsOutn("SCHREGNO" + String.valueOf(col), row, student._schregno);
        			svf.VrsOutn("NAME" + String.valueOf(col), row, student._name);

                	String field = "1";
                	int keta = tokenKetaMax1;
                	for (int i = 0; i < student._rosenList.size(); i++) {
                		final String rosen = (String) student._rosenList.get(i);
                		final int dataKeta = KNJ_EditEdit.getMS932ByteLength(rosen);
    					if (dataKeta > keta) {
    						if (dataKeta > tokenKetaMax2) {
    							keta = tokenKetaMax3;
    							field = "3";
    						} else {
    							keta = tokenKetaMax2;
    							field = "2";
    						}
                			break;
                		}
                	}
                	final List tokenList = new ArrayList();
                	for (int i = 0; i < student._rosenList.size(); i++) {
                		final String rosen = (String) student._rosenList.get(i);
                		if (null != rosen) {
                			tokenList.addAll(KNJ_EditKinsoku.getTokenList(rosen, keta));
                		}
                	}

                	if (tokenList.isEmpty()) {
            			svf.VrsOutn("SECTION" + String.valueOf(col) + "_" + field + "_1", row, "DUMMY");
            			svf.VrAttributen("SECTION" + String.valueOf(col) + "_" + field + "_1", row, "X=10000");
                	} else {
                		for (int i = 0; i < tokenList.size(); i++) {
                			svf.VrsOutn("SECTION" + String.valueOf(col) + "_" + field + "_" + String.valueOf(i + 1), row, (String) tokenList.get(i));
                		}
                	}

                	col++;
                	if (col > colMax) {
                		row++;
                		col = 1;
                		if (row > rowMax) {
                			row = 1;
                			svf.VrEndPage();
                			newpage = true;
                		}
                	}
                	_hasData = true;
                }
                if (!newpage) {
        			if (null != _param._whitespacePath) {
            			// 印字しない範囲を空白
        				for (; row <= rowMax; row++) {
        					for (; col <= colMax; col++) {
        						svf.VrsOutn("whitespace" + String.valueOf(col), row, _param._whitespacePath);
        					}
        					col = 1;
        				}
        			}
        			svf.VrEndPage();
                }
        	} else if ("2".equals(_param._page2Div)) {
        		svf.VrSetForm("KNJA143O_2_2.frm", 1);
    			svf.VrsOut("DUMMY", "1");
    			svf.VrEndPage();
    			_hasData = true;
        	}
        }
    }

    private static String append(final String a, final String b) {
    	if (StringUtils.isBlank(a)) {
    		return "";
    	}
    	return a + StringUtils.defaultString(b);
    }

    private static String prepend(final String a, final String b) {
    	if (StringUtils.isBlank(b)) {
    		return "";
    	}
    	return StringUtils.defaultString(a) + b;
    }

	private String digitsToZenkaku(final String s) {
    	final StringBuffer stb = new StringBuffer();
    	for (int i = 0; i < s.length(); i++) {
    		char ch = s.charAt(i);
    		if ('0' <= ch && ch <= '9') {
    			ch = (char) (ch - '0' + (int) '\uFF10');
    		}
    		stb.append(ch);
    	}
		return stb.toString();
	}

	private static class Student {
		String _schregno;
        String _grade;
        String _hrClass;
        String _attendno;
        String _schoolKind;
        String _gradeCd;
        String _gradeName1;
		String _majorname;
        String _name;
        String _nameKana;
        String _birthday;
        String _zipcd;
        String _addr1;
        String _addr2;
        final List _rosenList = new ArrayList();

        public static List getStudentList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            final String sql = sql(param);
            log.debug(" sql = " + sql);
            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                final Student student = new Student();
                final Map map = (Map) it.next();
                student._schregno = KnjDbUtils.getString(map, "SCHREGNO");
                student._grade = KnjDbUtils.getString(map, "GRADE");
                student._hrClass = KnjDbUtils.getString(map, "HR_CLASS");
                student._attendno = KnjDbUtils.getString(map, "ATTENDNO");
                student._schoolKind = KnjDbUtils.getString(map, "SCHOOL_KIND");
                student._gradeCd = KnjDbUtils.getString(map, "GRADE_CD");
                student._gradeName1 = KnjDbUtils.getString(map, "GRADE_NAME1");
                student._majorname = KnjDbUtils.getString(map, "MAJORNAME");
                student._name = KnjDbUtils.getString(map, "NAME");
                student._nameKana = KnjDbUtils.getString(map, "NAME_KANA");
                student._birthday = KnjDbUtils.getString(map, "BIRTHDAY");
                student._zipcd = KnjDbUtils.getString(map, "ZIPCD");
                student._addr1 = KnjDbUtils.getString(map, "ADDR1");
                student._addr2 = KnjDbUtils.getString(map, "ADDR2");
                for (int i = 1; i <= 7; i++) {
                    final String josya = KnjDbUtils.getString(map, "JOSYA_" + String.valueOf(i));
                    final String rosen = KnjDbUtils.getString(map, "ROSEN_" + String.valueOf(i));
                    final String gesya = KnjDbUtils.getString(map, "GESYA_" + String.valueOf(i));
                    if (StringUtils.isBlank(josya) && StringUtils.isBlank(rosen) && StringUtils.isBlank(gesya)) {
                    	continue;
                    }
                    student._rosenList.add(append(append(rosen, "　"), append(append(josya, "～"), gesya)));
                }
                list.add(student);
            }
            return list;
        }

        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     BASE.SCHREGNO, ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGD.ATTENDNO, ");
            stb.append("     REGDG.SCHOOL_KIND, ");
            stb.append("     REGDG.GRADE_CD, ");
            stb.append("     REGDG.GRADE_NAME1, ");
            stb.append("     MAJ.MAJORNAME, ");
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.NAME_KANA, ");
            stb.append("     BASE.BIRTHDAY, ");
            stb.append("     ADDR.ZIPCD, ");
            stb.append("     ADDR.ADDR1, ");
            stb.append("     ADDR.ADDR2 ");
            stb.append("     ,CASE WHEN ENVR.FLG_1 = '1' THEN J1.STATION_NAME ELSE ENVR.JOSYA_1 END AS JOSYA_1 ");
            stb.append("     ,CASE WHEN ENVR.FLG_2 = '1' THEN J2.STATION_NAME ELSE ENVR.JOSYA_2 END AS JOSYA_2 ");
            stb.append("     ,CASE WHEN ENVR.FLG_3 = '1' THEN J3.STATION_NAME ELSE ENVR.JOSYA_3 END AS JOSYA_3 ");
            stb.append("     ,CASE WHEN ENVR.FLG_4 = '1' THEN J4.STATION_NAME ELSE ENVR.JOSYA_4 END AS JOSYA_4 ");
            stb.append("     ,CASE WHEN ENVR.FLG_5 = '1' THEN J5.STATION_NAME ELSE ENVR.JOSYA_5 END AS JOSYA_5 ");
            stb.append("     ,CASE WHEN ENVR.FLG_6 = '1' THEN J6.STATION_NAME ELSE ENVR.JOSYA_6 END AS JOSYA_6 ");
            stb.append("     ,CASE WHEN ENVR.FLG_7 = '1' THEN J7.STATION_NAME ELSE ENVR.JOSYA_7 END AS JOSYA_7 ");
            stb.append("     ,CASE WHEN ENVR.FLG_1 = '1' THEN J1.LINE_NAME ELSE ENVR.ROSEN_1 END AS ROSEN_1 ");
            stb.append("     ,CASE WHEN ENVR.FLG_2 = '1' THEN J2.LINE_NAME ELSE ENVR.ROSEN_2 END AS ROSEN_2 ");
            stb.append("     ,CASE WHEN ENVR.FLG_3 = '1' THEN J3.LINE_NAME ELSE ENVR.ROSEN_3 END AS ROSEN_3 ");
            stb.append("     ,CASE WHEN ENVR.FLG_4 = '1' THEN J4.LINE_NAME ELSE ENVR.ROSEN_4 END AS ROSEN_4 ");
            stb.append("     ,CASE WHEN ENVR.FLG_5 = '1' THEN J5.LINE_NAME ELSE ENVR.ROSEN_5 END AS ROSEN_5 ");
            stb.append("     ,CASE WHEN ENVR.FLG_6 = '1' THEN J6.LINE_NAME ELSE ENVR.ROSEN_6 END AS ROSEN_6 ");
            stb.append("     ,CASE WHEN ENVR.FLG_7 = '1' THEN J7.LINE_NAME ELSE ENVR.ROSEN_7 END AS ROSEN_7 ");
            stb.append("     ,CASE WHEN ENVR.FLG_1 = '1' THEN G1.STATION_NAME ELSE ENVR.GESYA_1 END AS GESYA_1 ");
            stb.append("     ,CASE WHEN ENVR.FLG_2 = '1' THEN G2.STATION_NAME ELSE ENVR.GESYA_2 END AS GESYA_2 ");
            stb.append("     ,CASE WHEN ENVR.FLG_3 = '1' THEN G3.STATION_NAME ELSE ENVR.GESYA_3 END AS GESYA_3 ");
            stb.append("     ,CASE WHEN ENVR.FLG_4 = '1' THEN G4.STATION_NAME ELSE ENVR.GESYA_4 END AS GESYA_4 ");
            stb.append("     ,CASE WHEN ENVR.FLG_5 = '1' THEN G5.STATION_NAME ELSE ENVR.GESYA_5 END AS GESYA_5 ");
            stb.append("     ,CASE WHEN ENVR.FLG_6 = '1' THEN G6.STATION_NAME ELSE ENVR.GESYA_6 END AS GESYA_6 ");
            stb.append("     ,CASE WHEN ENVR.FLG_7 = '1' THEN G7.STATION_NAME ELSE ENVR.GESYA_7 END AS GESYA_7 ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT REGD ");
            stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = REGD.YEAR ");
            stb.append("         AND REGDG.GRADE = REGD.GRADE ");
            stb.append("     LEFT JOIN MAJOR_MST MAJ ON MAJ.COURSECD = REGD.COURSECD AND MAJ.MAJORCD = REGD.MAJORCD ");
            stb.append("     LEFT JOIN ( ");
            stb.append("         SELECT ");
            stb.append("             SCHREGNO, ");
            stb.append("             MAX(ISSUEDATE) AS ISSUEDATE ");
            stb.append("         FROM ");
            stb.append("             SCHREG_ADDRESS_DAT ");
            stb.append("         GROUP BY ");
            stb.append("             SCHREGNO ");
            stb.append("     ) ADDR_MAX ON ADDR_MAX.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_ADDRESS_DAT ADDR ON ADDR.SCHREGNO = ADDR_MAX.SCHREGNO ");
            stb.append("         AND ADDR.ISSUEDATE = ADDR_MAX.ISSUEDATE ");
            stb.append("     LEFT JOIN SCHREG_ENVIR_DAT ENVR ON ENVR.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN STATION_NETMST J1 ON J1.STATION_CD = ENVR.JOSYA_1 ");
            stb.append("     LEFT JOIN STATION_NETMST J2 ON J2.STATION_CD = ENVR.JOSYA_2 ");
            stb.append("     LEFT JOIN STATION_NETMST J3 ON J3.STATION_CD = ENVR.JOSYA_3 ");
            stb.append("     LEFT JOIN STATION_NETMST J4 ON J4.STATION_CD = ENVR.JOSYA_4 ");
            stb.append("     LEFT JOIN STATION_NETMST J5 ON J5.STATION_CD = ENVR.JOSYA_5 ");
            stb.append("     LEFT JOIN STATION_NETMST J6 ON J6.STATION_CD = ENVR.JOSYA_6 ");
            stb.append("     LEFT JOIN STATION_NETMST J7 ON J7.STATION_CD = ENVR.JOSYA_7 ");
            stb.append("     LEFT JOIN STATION_NETMST G1 ON G1.STATION_CD = ENVR.GESYA_1 ");
            stb.append("     LEFT JOIN STATION_NETMST G2 ON G2.STATION_CD = ENVR.GESYA_2 ");
            stb.append("     LEFT JOIN STATION_NETMST G3 ON G3.STATION_CD = ENVR.GESYA_3 ");
            stb.append("     LEFT JOIN STATION_NETMST G4 ON G4.STATION_CD = ENVR.GESYA_4 ");
            stb.append("     LEFT JOIN STATION_NETMST G5 ON G5.STATION_CD = ENVR.GESYA_5 ");
            stb.append("     LEFT JOIN STATION_NETMST G6 ON G6.STATION_CD = ENVR.GESYA_6 ");
            stb.append("     LEFT JOIN STATION_NETMST G7 ON G7.STATION_CD = ENVR.GESYA_7 ");
            stb.append(" WHERE ");
            stb.append("     REGD.YEAR = '" + param._year + "' ");
            stb.append("     AND REGD.SEMESTER = '" + param._semester + "' ");
            if ("1".equals(param._disp)) {
                stb.append("     AND REGD.GRADE || REGD.HR_CLASS IN " + SQLUtils.whereIn(true, param._category_selected) + " ");
            } else if ("2".equals(param._disp)) {
                stb.append("     AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, param._category_selected) + " ");
            }
            stb.append(" ORDER BY ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGD.ATTENDNO ");
            return stb.toString();
        }

    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _disp; // 1:クラス 2:個人
        final String[] _category_selected;
        final String _printPage; // 1:表面 2:裏面
        final String _imageColor; // 1:青 2:赤
        final String _page2Div; // 1:シール 2:定型文選択
        String _issueDate;
        String _limitDate;
        final int _porow;
        final int _pocol;
        final String _documentroot;
        final String _useAddrField2;

        private String _jobname;
        private String _principalName;
        private String _schoolname;
        private String _addr1;
        private String _telno;
        private String _schoolZipcd;

        private String _extension;
        private String _imagepass;
        private String _whitespacePath;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _disp = request.getParameter("DISP");
            _category_selected = request.getParameterValues("CATEGORY_SELECTED");
            if (null != _category_selected) {
            	for (int i = 0; i < _category_selected.length; i++) {
            		_category_selected[i] = StringUtils.split(_category_selected[i], "-")[0];
            	}
            }
            _printPage = request.getParameter("PRINT_PAGE");
            _imageColor = request.getParameter("IMAGE_COLOR");
            if (null != request.getParameter("ISSUE_DATE")) {
            	_issueDate = StringUtils.defaultString(request.getParameter("ISSUE_DATE")).replace('/', '-');
            }
            if (null != request.getParameter("LIMIT_DATE")) {
            	_limitDate = StringUtils.defaultString(request.getParameter("LIMIT_DATE")).replace('/', '-');
            }
            _page2Div = request.getParameter("PAGE2_DIV");
            _porow = NumberUtils.isDigits(request.getParameter("POROW")) ? Integer.parseInt(request.getParameter("POROW")) : -1;
            _pocol = NumberUtils.isDigits(request.getParameter("POCOL")) ? Integer.parseInt(request.getParameter("POCOL")) : -1;
            _documentroot = request.getParameter("DOCUMENTROOT");
            _useAddrField2 = request.getParameter("useAddrField2");

            KNJSchoolMst knjSchoolMst = new KNJSchoolMst(db2, _year);
            _schoolZipcd = knjSchoolMst._schoolZipcd;

            setControlMst(db2);
            loadCertifSchoolDat(db2);

            _whitespacePath = getImageFilePath("whitespace.png");
        }

        private void setControlMst(DB2UDB db2) {
            _extension = "";
            final Map map = KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT IMAGEPATH, EXTENSION FROM CONTROL_MST "));
            _imagepass = KnjDbUtils.getString(map, "IMAGEPATH");
            _extension = KnjDbUtils.getString(map, "EXTENSION");
        }


        /**
         * 写真データファイルの取得
         */
        private String getImageFilePath(final String filename) {
            if (null == _documentroot || null == _imagepass || null == filename) {
                return null;
            } // DOCUMENTROOT
            final StringBuffer path = new StringBuffer();
            path.append(_documentroot).append("/").append(_imagepass).append("/").append(filename);
            final File file = new File(path.toString());
            if (!file.exists()) {
                log.warn("画像ファイル無し:" + path);
                return null;
            } // 写真データ存在チェック用
            return path.toString();
        }

        public void loadCertifSchoolDat(final DB2UDB db2) {

        	final Map map = KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '101'"));
            _jobname =  KnjDbUtils.getString(map, "JOB_NAME");
            _principalName = KnjDbUtils.getString(map, "PRINCIPAL_NAME");
            _schoolname = KnjDbUtils.getString(map, "SCHOOL_NAME");
            _addr1 = KnjDbUtils.getString(map, "REMARK1"); // 学校住所
            _telno = KnjDbUtils.getString(map, "REMARK3"); // 学校電話番号
        }
    }
}

// eof
