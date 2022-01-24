// kanji=漢字
/*
 * $Id: b5a044885e604e69a67baad94c27b71f2ae783fe $
 *
 * 作成日: 2009/11/11 10:24:21 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: b5a044885e604e69a67baad94c27b71f2ae783fe $
 */
public class KNJA143A {

    private static final Log log = LogFactory.getLog("KNJA143A.class");

    private boolean _hasData;

    Param _param;

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
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf);

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            closeDb(db2);
            svf.VrQuit();
        }

    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final List printStudents = getPrintStudent(db2);
        //useFormNameA143A 1:あり 4:なし
        final int formParam = _param._useFormNameA143A != null && !"".equals(_param._useFormNameA143A) ? 1 : 4;
        //final int formParam = 1;
        svf.VrSetForm(_param.getFormId(), formParam);
        log.debug("フォーム：" + _param.getFormId());
        final int maxLinecnt = 4;
        int linecnt = 1;
        for (final Iterator it = printStudents.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();

            if (_param._useFormNameA143A != null && !"".equals(_param._useFormNameA143A)) {
                if (_param._printA4) {
                    //A4用紙
                    if(maxLinecnt  < linecnt) {
                        //改ページ
                        svf.VrEndPage();
                        linecnt = 1;
                    }
                }else {
                    //カード
                	if (linecnt > 1) {
                		svf.VrEndPage();
                	}
                	svf.VrSetForm(_param.getFormId(), formParam);
                }
                //プロパティ設定を活かす処理へ遷移
                printPattern2(db2, svf, student, linecnt);
            } else {
                //古い処理(鳥取)へ遷移
                printPattern1(db2, svf, student);
                svf.VrEndRecord();
            }
            linecnt++;
            _hasData = true;
        }
        //出力
        svf.VrEndPage();
    }

    private void printPattern1(final DB2UDB db2, final Vrw32alp svf, final Student student) throws SQLException {
        //写真
        final String photoCheck = _param._imagePass + student._photoName;
        if (isFileExists(photoCheck)) {
            svf.VrsOut("PHOTO_BMP", photoCheck );//顔写真
        }

//        final String stampCheck = _param._imagePass + _param._stampName; // TODO:必要ないが後で出力できるように残しておく。
//        if (isFileExists(stampCheck)) {
//            svf.VrsOut("STAMP_BMP", stampCheck );//学校印
//        }

        //固定文言
        svf.VrsOut("EDUCATION", "高等課程");
        svf.VrsOut("TITLE", "生徒証");
        svf.VrsOut("SENTENCE", "生徒");
        //生徒
        svf.VrsOut("BARCODE", student._schregno);
        svf.VrsOut("SCHREGNO", student._schregno);
        svf.VrsOut("NAME", student._name);
        String majorName = (student._courseName == null ? "" : student._courseName) + "課程　" + (student._majorName == null ? "" : student._majorName);
        if (majorName.length() <= 15) {
            svf.VrsOut("MAJORNAME1", "課程・学科");
            svf.VrsOut("MAJOR1", majorName);
        } else {
            svf.VrsOut("MAJORNAME2", "課程・学科");
            svf.VrsOut("MAJOR2", (student._courseName != null ? student._courseName : "" ) + "課程");
            svf.VrsOut("MAJOR3", student._majorName);
        }
        if ("1".equals(_param._useAddrField2)
                && ((null != student._addr1 && student._addr1.length() > 25) ||
                    (null != student._addr2 && student._addr2.length() > 25))) {
            svf.VrsOut("ADDRESS1_2", student._addr1);
            svf.VrsOut("ADDRESS2_2", student._addr2);
        } else {
            svf.VrsOut("ADDRESS1", student._addr1);
            svf.VrsOut("ADDRESS2", student._addr2);
        }
        //発行者
        if (_param._printCard) {
            if (null != _param._schoolAddress) {
                String field;
                if ("1".equals(_param._useAddrField2) && _param._schoolAddress.length() > 25) {
                    field = "SCHOOLADDRESS5";
                } else if ("1".equals(_param._useAddrField2) && _param._schoolAddress.length() > 20) {
                    field = "SCHOOLADDRESS4";
                } else if ("1".equals(_param._useAddrField2) && _param._schoolAddress.length() > 18) {
                    field = "SCHOOLADDRESS3";
                } else if (_param._schoolAddress.length() > 16) {
                    field = "SCHOOLADDRESS2";
                } else {
                    field = "SCHOOLADDRESS1";
                }
                svf.VrsOut(field, _param._schoolAddress);
            }
        } else {
            String field;
            if ("1".equals(_param._useAddrField2) && _param._schoolAddress.length() > 50) {
                field = "SCHOOLADDRESS1_4";
            } else if ("1".equals(_param._useAddrField2) && _param._schoolAddress.length() > 40) {
                field = "SCHOOLADDRESS1_3";
            } else if ("1".equals(_param._useAddrField2) && _param._schoolAddress.length() > 32) {
                field = "SCHOOLADDRESS1_2";
            } else {
                field = "SCHOOLADDRESS1";
            }
            svf.VrsOut(field, _param._schoolAddress);
        }
        svf.VrsOut("SCHOOLNAME1", _param._schoolName);
        svf.VrsOut("JOBNAME", _param._jobName);
        svf.VrsOut("STAFFNAME", _param._principalName);
        //日付
        printDate(db2, svf, "BIRTHDAY", student._birthday, 0);
        printDate(db2, svf, "SDATE", _param._sDate, 0);
        printDate(db2, svf, "EDATE", _param._eDate, 0);
    }

    private void printPattern2(final DB2UDB db2, final Vrw32alp svf, final Student student, final int linecnt) throws SQLException {

        //写真
        final String photoCheck = _param._imagePass + student._photoName;
        if (isFileExists(photoCheck)) {
            vprnt(svf, "PHOTO_BMP", linecnt, photoCheck);
        }

        //校種の色
        final String linephoto;
        if (_param._isThroughH) {
            linephoto = _param._imagePass + "knja143aImage_jhblue.jpg";
        } else {
            if ("J".equals(_param._schoolKind)) {
                linephoto = _param._imagePass + "knja143aImage_jblue.jpg";
            } else {
                linephoto = _param._imagePass + "knja143aImage_hred.jpg";
            }
        }
        if (isFileExists(linephoto)) {
            vprnt(svf, "LINE1", linecnt, linephoto); //校種線1
            vprnt(svf, "LINE2", linecnt, linephoto); //校種線2
        }

        //固定文言
        svf.VrsOutn("TITLE", linecnt, "身分証明書");  //タイトル
        svf.VrsOutn("SENTENCE", linecnt, "生徒"); //証明文言

        //生徒
        vprnt(svf, "SCHREGNO", linecnt, student._schregno); //学籍番号
        vprnt(svf, "BARCODE", linecnt, student._schregno); //バーコード
        vprnt(svf, "MAJOR1", linecnt, " 第" + Integer.parseInt(student._grade) + "学年（" + student._entDate.substring(2, 4) + "）年度生"); //学年

        String nameField = "";
        if (_param._printA4) {
            nameField = getMS932ByteLength(student._name) > 16 ? "2" : "1" ;
        }else {
            nameField = getMS932ByteLength(student._name) > 30 ? "4" : getMS932ByteLength(student._name) > 20 ? "3" : getMS932ByteLength(student._name) > 18 ? "2" : "1";
        }
        vprnt(svf, "NAME" + nameField, linecnt, student._name); //生徒氏名
        vprnt(svf, "OLD", linecnt, student._old); //年齢

        final String addr1Field = getMS932ByteLength(student._addr1) > 50 ? "_3" : getMS932ByteLength(student._addr1) > 38 ? "_2" : "";
        final String addr2Field = getMS932ByteLength(student._addr2) > 50 ? "_3" : getMS932ByteLength(student._addr2) > 38 ? "_2" : "";
        vprnt(svf, "ADDRESS1" + addr1Field, linecnt, student._addr1); //所在地1
        vprnt(svf, "ADDRESS2" + addr2Field, linecnt, student._addr2); //所在地2

        String sAddr = "";
        String schoolAddrField = "";
        String sName = "";
        if (_param._printA4) {
            sAddr = "SCHOOLADDRESS1";
            schoolAddrField = getMS932ByteLength(_param._schoolAddress) > 50 ? "_4" : getMS932ByteLength(_param._schoolAddress) > 40 ? "_3" : getMS932ByteLength(_param._schoolAddress) > 32 ? "_2" : "";
            sName = "SCHOOLNAME1";
        }else {
            sAddr = "SCHOOL_ADDR";
            schoolAddrField = getMS932ByteLength(_param._schoolAddress) > 40 ? "2" : "1";
            sName = "SCHOOL_NAME";
        }
        vprnt(svf, sAddr + schoolAddrField, linecnt, _param._schoolAddress); //学校所在地
        vprnt(svf, sName, linecnt, _param._schoolName); //学校名
        if ("KNJA143A_3".equals(_param._useFormNameA143A)) {
            vprnt(svf, "SCHOOL_NAME2", linecnt, _param._schoolName); //玉川聖のみ学校名称を左上部分に出力
        }
        vprnt(svf, "JOBNAME", linecnt, _param._jobName); //役職・氏名
        vprnt(svf, "STAFFNAME", linecnt, _param._principalName); //役職・氏名
        vprnt(svf, "TELNO", linecnt, _param._schoolTelNo); //電話番号

        vprnt(svf, "DUMMY", linecnt, "DUMMY"); //ダミー

        //日付
        if (_param._printA4) {
            printDate(db2, svf, "BIRTHDAY", student._birthday, linecnt); //生年月日
            printDate(db2, svf, "SDATE", _param._sDate, linecnt); //発行日
            svf.VrsOutn("LIMIT", linecnt , KNJ_EditDate.h_format_JP(db2,_param._eDate)); //有効期限
        }else {
            svf.VrsOut("BIRTHDAY", KNJ_EditDate.h_format_SeirekiJP(student._birthday)); //生年月日
            svf.VrsOut("PRINT_DATE", KNJ_EditDate.h_format_SeirekiJP(_param._sDate) + "発行"); //発行日
            svf.VrsOut("EDATE", KNJ_EditDate.h_format_SeirekiJP(_param._eDate)); //有効期限

            String skStr = student._schoolKind;
            if (_param._isThroughH) {
                skStr = "JH";
            }
            String schoolstampExt = ".bmp";
            if ("KNJA143A_3".equals(_param._useFormNameA143A)) {
            	schoolstampExt = ".jpg";
            }
            final String stampCheck = _param._imagePass + "/SCHOOLSTAMP_" + skStr + schoolstampExt;
            if (isFileExists(stampCheck)) {
                svf.VrsOut("STAMP_BMP", stampCheck );//学校印
            }

            final String logoCheck = _param._imagePass + "/SCHOOLLOGO_" + skStr + ".jpg";
            if (isFileExists(logoCheck)) {
                svf.VrsOut("SCHOOL_LOGO", logoCheck );//学校ロゴ
            }
        }
    }

    private void vprnt(final Vrw32alp svf, final String field,  final int gyo, final String val) {
    	if (_param._printA4) {
    		svf.VrsOutn(field, gyo, val);
    	} else {
    		svf.VrsOut(field, val);
    	}
    }

    private boolean isFileExists(final String fileName) {
        File fileCheck = new File(fileName);
        return fileCheck.exists();
    }

    private void printDate(final DB2UDB db2, final Vrw32alp svf, final String field, final String date, final int linecnt) {
        if (null != date) {
            String str = KNJ_EditDate.h_format_JP(db2, date);
            String arr_str[] = KNJ_EditDate.tate_format(str);
            if (_param._isSeireki) {
                arr_str[0] = "";
                arr_str[1] = date.substring(0, 4);
            } else if (arr_str[1] == null) {
                arr_str[1] = (arr_str[0]).substring(2, 3);
            }
            if (_param._useFormNameA143A != null && !"".equals(_param._useFormNameA143A)) {
                //プロパティを活かす処理
                for (int i = 1; i < 5; i++) {
                    vprnt(svf, field + String.valueOf(i) , linecnt , arr_str[i-1]);
                 }
            }else {
                for (int i = 1; i < 5; i++) {
                    svf.VrsOut(field + String.valueOf(i) , arr_str[i-1] );
                 }
            }

        }
    }

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
        return KNJ_EditEdit.getMS932ByteLength(str);
    }

    private List getPrintStudent(final DB2UDB db2) throws SQLException {
        final List rtnList = new ArrayList();
        for (int i = 0; i < _param._schregno.length; i++) {
            final String schno = _param._schregno[i];
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = getStudentSql(schno);
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    final String grade = rs.getString("GRADE");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String entdate = rs.getString("ENT_DATE");
                    final String name;
                    if (_param._useFormNameA143A != null && !"".equals(_param._useFormNameA143A)) {
                        name = rs.getString("NAME");
                    } else {
                        name = "1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME") : rs.getString("NAME");
                    }
                    final String old = rs.getString("OLD");
                    final String birthday = rs.getString("BIRTHDAY");
                    final String courseName = rs.getString("COURSENAME");
                    final String majorName = rs.getString("MAJORNAME");
                    final String addr1 = rs.getString("ADDR1");
                    final String addr2 = rs.getString("ADDR2");
                    final Student student = new Student(
                            schregno,
                            grade,
                            schoolKind,
                            entdate,
                            name,
                            old,
                            birthday,
                            courseName,
                            majorName,
                            addr1,
                            addr2,
                            _param._extension);
                    rtnList.add(student);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        return rtnList;
    }

    private String getStudentSql(final String schno) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHREG_ADDRESS_MAX AS ( ");
        stb.append(" SELECT ");
        stb.append("     SCHREGNO, ");
        stb.append("     MAX(ISSUEDATE) AS ISSUEDATE ");
        stb.append(" FROM ");
        stb.append("     SCHREG_ADDRESS_DAT ");
        stb.append(" WHERE ");
        stb.append("     SCHREGNO='" + schno + "' ");
        stb.append(" GROUP BY ");
        stb.append("     SCHREGNO ");
        stb.append("     ) ");
        stb.append(" , SCHREG_ADDRESS AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.ISSUEDATE, ");
        stb.append("     T1.ADDR1, ");
        stb.append("     T1.ADDR2 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_ADDRESS_DAT T1, ");
        stb.append("     SCHREG_ADDRESS_MAX T2 ");
        stb.append(" WHERE ");
        stb.append("     T2.SCHREGNO = T1.SCHREGNO AND ");
        stb.append("     T2.ISSUEDATE = T1.ISSUEDATE ");
        stb.append("     ) ");
//☆は新規で利用するもの。★はベースからあった。
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");   //★☆
        stb.append("     T1.GRADE, ");      //☆
        stb.append("     GDAT.SCHOOL_KIND, ");  //☆
        stb.append("     YEAR(T2.ENT_DATE) AS ENT_DATE, "); //☆
        stb.append("     T2.NAME, ");       //★☆
        stb.append("     CASE WHEN T2.BIRTHDAY IS NOT NULL THEN YEAR('" + _param._sDate + "' - T2.BIRTHDAY) END AS OLD, "); //☆
        stb.append("     T2.REAL_NAME, ");  //★
        stb.append("     (CASE WHEN L4.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS USE_REAL_NAME, "); //★
        stb.append("     T2.BIRTHDAY, ");   //★☆
        stb.append("     L3.COURSENAME, "); //★
        stb.append("     L1.MAJORNAME, ");  //★
        stb.append("     L2.ADDR1, ");      //★☆
        stb.append("     L2.ADDR2 ");       //★☆
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO=T1.SCHREGNO ");
        stb.append("     LEFT JOIN MAJOR_MST L1 ON L1.COURSECD = T1.COURSECD AND L1.MAJORCD = T1.MAJORCD ");
        stb.append("     LEFT JOIN SCHREG_ADDRESS L2 ON L2.SCHREGNO=T1.SCHREGNO ");
        stb.append("     LEFT JOIN COURSE_MST L3 ON L3.COURSECD=T1.COURSECD ");
        stb.append("     LEFT JOIN SCHREG_NAME_SETUP_DAT L4 ON L4.SCHREGNO=T1.SCHREGNO AND L4.DIV='05' ");
        stb.append("     INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' AND ");
        stb.append("     T1.SEMESTER = '" + _param._semester + "' AND ");
        stb.append("     T1.SCHREGNO = '" + schno + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO ");
        return stb.toString();
    }

    private class Student {
        final String _schregno;
        final String _grade;
        final String _schoolKind;
        final String _entDate;
        final String _name;
        final String _old;
        final String _birthday;
        final String _courseName;
        final String _majorName;
        final String _addr1;
        final String _addr2;
        final String _photoName;

        Student(final String schregno,
                final String grade,
                final String schoolKind,
                final String entdate,
                final String name,
                final String old,
                final String birthday,
                final String courseName,
                final String majorName,
                final String addr1,
                final String addr2,
                final String extension
        ) {
            _schregno = schregno;
            _grade = grade;
            _schoolKind = schoolKind;
            _entDate = entdate;
            _name = name;
            _old = old;
            _birthday = birthday;
            _courseName = courseName;
            _majorName = majorName;
            _addr1 = addr1;
            _addr2 = addr2;
            _photoName = "P" + schregno + "." + extension;
        }

    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 66787 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String[] _schregno;
        private final boolean _printA4;
        private final boolean _printCard;
        private final String _sDate;
        private final String _eDate;
        private final String _imagePass;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private boolean _isSeireki;
        private String _schoolName;
        private String _schoolAddress;
        private String _jobName;
        private String _principalName;
        private String _extension;
        private final String _useAddrField2;
    	private final String _useFormNameA143A;
    	private final String _schoolCd;
    	private final String _schoolKind;
        private final String _grade;
        private final String _grHrCls;
    	private String _schoolTelNo;
    	private String _out2PrintSide;
    	private final boolean _isThroughH;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("GAKKI");
            _schregno = request.getParameterValues("category_selected");
            String output = request.getParameter("OUTPUT"); // 1:Ａ４用紙, 2:カード
            _printA4   = "1".equals(output);
            _printCard = "2".equals(output);
            final String sDate = request.getParameter("TERM_SDATE");
            _sDate = sDate.replace('/', '-');
            final String eDate = request.getParameter("TERM_EDATE");
            _eDate = eDate.replace('/', '-');

            _grade = request.getParameter("GRADE");
            _grHrCls = request.getParameter("GRADE_HR_CLASS");
            _schoolCd = request.getParameter("SCHOOLCD");
            // _schoolKind = request.getParameter("SCHOOLKIND");
            _schoolKind = setSchoolKindForGrade(db2);

            String rootPass = request.getParameter("DOCUMENTROOT"); // '/usr/local/development/src'
            _imagePass = rootPass + "/image/";
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            setSeirekiFlg(db2);
            setPhotoFileExtension(db2);
            _useAddrField2 = request.getParameter("useAddrField2");
            _useFormNameA143A = request.getParameter("useFormNameA143A");
            _out2PrintSide = request.getParameter("OUT2PRINT_SIDE");
            _isThroughH = isThroughH(db2);
            setSchoolInfo(db2);
        }

        private void setSeirekiFlg(final DB2UDB db2) {
            try {
                _isSeireki = false;
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    final String str = rs.getString("NAME1");
                    if ("2".equals(str)) _isSeireki = true; //西暦
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
            }
        }

//        private String printDate(final String date) {
//            if (null == date) {
//                return "";
//            }
//            if (_isSeireki) {
//                return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
//            } else {
//                return KNJ_EditDate.h_format_JP(date);
//            }
//        }

        private String getFormId() {
            final String retStr;
            if (_useFormNameA143A != null && !"".equals(_useFormNameA143A)) {
            	if (_printA4) {
                    retStr = _useFormNameA143A + "_1.frm";
            	} else {
            		if ("2".equals(_out2PrintSide)) {
                        retStr = _useFormNameA143A + "_3.frm";
            		} else {
                        retStr = _useFormNameA143A + "_2.frm";
            		}
            	}
            } else {
                retStr = (_printA4) ? "KNJA143A_2.frm" : "KNJA143A_1.frm";
            }
            return retStr;
        }

        private String setSchoolKindForGrade(final DB2UDB db2) {
        	String retStr = "";
            try {
                String sql = "SELECT SCHOOL_KIND " +
                             "FROM SCHREG_REGD_GDAT " +
                             "WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
            	    retStr = rs.getString("SCHOOL_KIND");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
            }
        	return retStr;
        }
        private void setSchoolInfo(final DB2UDB db2) {
            try {
                _schoolName = "";
                _schoolAddress = "";
                _jobName = "";
                _principalName = "";
                _schoolTelNo = "";

                //佐野日大(中高一貫)の場合は、高校用の学校データを参照。
                //それ以外は校種によって参照箇所を
                String cifCd = _isThroughH ? "101" : "P".equals(_schoolKind) ? "140" : "J".equals(_schoolKind) ? "102" : "101";
                String sql = "SELECT SCHOOL_NAME,JOB_NAME,PRINCIPAL_NAME,REMARK1,REMARK3" +
                            " FROM CERTIF_SCHOOL_DAT " +
                            " WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '" + cifCd + "' ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    _schoolName = rs.getString("SCHOOL_NAME");
                    _schoolAddress = rs.getString("REMARK1");
                    _jobName = rs.getString("JOB_NAME");
                    _principalName = rs.getString("PRINCIPAL_NAME");
                    _schoolTelNo = rs.getString("REMARK3");
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
            }
        }

        private void setPhotoFileExtension(DB2UDB db2) {
            try {
                _extension = "";
                String sql = "SELECT EXTENSION FROM CONTROL_MST ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    _extension = rs.getString("EXTENSION");
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
            }
        }

        /*
         * 名称マスタ　NAMECD1=Z010の予備2を取得し、中高一貫校か、チェックする
         */
        private boolean isThroughH(final DB2UDB db2) {
            boolean retbl = false;
            //佐野日大のみのチェック。frmファイルの文字列一致で判定。
            if (!"KNJA143A_3".equals(_useFormNameA143A)) {
                return retbl;
            }
            String chkflg = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT NAMESPARE2 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ";
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    chkflg = rs.getString("NAMESPARE2");
                }
            } catch (final Exception ex) {
                log.error("名称マスタロードエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("Z010_00_NAMESPARE2 =" + chkflg);
            if ("2".equals(chkflg)) {
                retbl = true;
            }
            return retbl;
        }
    }
}

// eof
