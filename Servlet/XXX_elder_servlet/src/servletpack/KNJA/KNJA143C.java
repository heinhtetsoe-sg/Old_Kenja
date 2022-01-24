// kanji=漢字
/*
 * $Id: 1cbe2536a03d39eed4e54a3e1acc867397b02aec $
 *
 * 作成日: 2010/03/24 10:24:21 - JST
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
import servletpack.KNJZ.detail.KNJSvfFieldModify;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: 1cbe2536a03d39eed4e54a3e1acc867397b02aec $
 */
public class KNJA143C {

    private static final Log log = LogFactory.getLog("KNJA143C.class");

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

            if (_param._isPrintUra) {
                printUra(svf);
            } else {
                printMain(db2, svf);
            }

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

    private void printUra(final Vrw32alp svf) {
        svf.VrSetForm(_param.getFormId(), 4);
        log.debug("フォーム：" + _param.getFormId());

        final int maxBusuu = null != _param._busuu ? Integer.parseInt(_param._busuu) : 0;
        for (int no = 1; no <= maxBusuu; no++) {
            //ダミー
            svf.VrsOut("MASK", "裏");
            //出力
            svf.VrEndRecord();
            _hasData = true;
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
        svf.VrSetForm(_param.getFormId(), 4);
        log.debug("フォーム：" + _param.getFormId());
        final String stampCheck = _param._imagePass + _param._stampName + "." + _param._extension;
        final String logoCheck  = _param._imagePass + _param._logoName  + "." + _param._extension;
        final boolean isStamp = isFileExists(stampCheck);
        final boolean isLogo  = isFileExists(logoCheck);
        for (final Iterator it = printStudents.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            //写真
            final String photoCheck = _param._imagePass + student._photoName + "." + _param._extension;
            if (isFileExists(photoCheck)) {
                svf.VrsOut("PHOTO", photoCheck );//顔写真
            }
            //学校長印
            if (isStamp) {
                svf.VrsOut("STAMP", stampCheck );//学校印
            }
            //学校ロゴ
            if (isLogo) {
                svf.VrsOut("SCHOOLLOGO", logoCheck );//学校ロゴ
            }
            //学籍番号
            svf.VrsOut("BARCODE_NO", student.getJancode());
            svf.VrsOut("BARCODE", student._barcode);
            svf.VrsOut("SCHREGNO", student._schregno);
            //所属
            String courseName = (student._courseName == null ? "" : student._courseName) + "課程";
            svf.VrsOut("COURSE", courseName);
            //学科・年組
            svf.VrsOut("GRADE", student.getGradeName());
            //生徒氏名
            String nameField = "NAME1";
            if (null != student._name && 15 < student._name.length()) {
                nameField = "NAME2";
            }
            svf.VrsOut(nameField, student._name);
            //生徒住所
            int addrketa;
            String addr1Field;
            String addr2Field;
            if ("1".equals(_param._useAddrField2) &&
                    ((null != student._addr1 && student._addr1.length() > 25) ||
                     (null != student._addr2 && student._addr2.length() > 25)
                    )) {
                addr1Field = "ADDRESS1_2";
                addr2Field = "ADDRESS2_2";
                addrketa = 60;
            } else {
                addr1Field = "ADDRESS1";
                addr2Field = "ADDRESS2";
                addrketa = 50;
            }
            svfFieldModifyAddress(svf, addrketa, addr1Field, addr2Field, student._addr1, student._addr2);
            svf.VrsOut(addr1Field, student._addr1);
            svf.VrsOut(addr2Field, student._addr2);
            //学校データ
            if (null != _param._schoolAddress) {
                final String field;
                if ("1".equals(_param._useAddrField2) && _param._schoolAddress.length() > 25) {
                    field = "SCHOOLADDRESS4";
                } else if ("1".equals(_param._useAddrField2) && _param._schoolAddress.length() > 20) {
                    field = "SCHOOLADDRESS3";
                } else if ("1".equals(_param._useAddrField2) && _param._schoolAddress.length() > 15) {
                    field = "SCHOOLADDRESS2";
                } else {
                    field = "SCHOOLADDRESS";
                }
                svf.VrsOut(field, _param._schoolAddress);
            }
            svf.VrsOut("SCHOOLNAME", _param._schoolName);
            svf.VrsOut("JOBNAME", _param._jobName);
            svf.VrsOut("STAFFNAME", _param._principalName);
            //日付
            svf.VrsOut("BIRTHDAY", _param.printDateFormat(student._birthday));
            svf.VrsOut("SDATE", _param.printDateFormat(_param._sDate));
            svf.VrsOut("EDATE", _param.printDateFormat(_param._eDate));
//          TODO:鳥取と同じ表示形式に変更する場合のため残しておく
//          printDate(svf, "BIRTHDAY", student._birthday);
//          printDate(svf, "SDATE", _param._sDate);
//          printDate(svf, "EDATE", _param._eDate);
            //出力
            svf.VrEndRecord();
            _hasData = true;
        }
    }

    private void svfFieldModifyAddress(
            final Vrw32alp svf,
            final int addrketa,
            final String addr1Field,
            final String addr2Field,
            final String addr1,
            final String addr2
    ) {
        KNJSvfFieldModify svfobj = new KNJSvfFieldModify();
        svfobj.width = 666;     //フィールドの幅(ドット)
        svfobj.height = 48;     //フィールドの高さ(ドット)
        svfobj.ystart = 431;   //開始位置(ドット)
        svfobj.minnum = 30;     //最小設定文字数
        svfobj.maxnum = addrketa;     //最大設定文字数
        int hnum;

        svfobj.setRetvalue( addr1, 0 );
        float size1 = svfobj.size;
        svfobj.setRetvalue( addr2, 0 );
        float size2 = svfobj.size;
        
        svfobj.size = Math.min(size1, size2); // ADDRESS1とADDRESS2の文字サイズを同一にする。
        
        int retFieldY = (int)Math.round( ( (double) svfobj.height - ( svfobj.size / 72 * 400 ) ) / 2 );
        hnum = 0;
        svfobj.jiku = retFieldY + svfobj.ystart + svfobj.height * hnum;
        svf.VrAttribute(addr1Field, "Y=" + svfobj.jiku);  //出力位置 + 開始高さ位置(ドット)
        svf.VrAttribute(addr1Field, "Size=" + svfobj.size);
        hnum = 1;
        svfobj.jiku = retFieldY + svfobj.ystart + svfobj.height * hnum;
        svf.VrAttribute(addr2Field, "Y=" + svfobj.jiku);  //出力位置 + 開始高さ位置(ドット)
        svf.VrAttribute(addr2Field, "Size=" + svfobj.size);
    }
    
    private boolean isFileExists(final String fileName) {
        File fileCheck = new File(fileName);
        return fileCheck.exists();
    }

    private void printDate(final Vrw32alp svf, final String field, final String date) {
        if (null != date) {
            String str = KNJ_EditDate.h_format_JP(date);
            String arr_str[] = KNJ_EditDate.tate_format(str);
            if (_param._isSeireki) {
                arr_str[0] = "";
                arr_str[1] = date.substring(0, 4);
            } else if (arr_str[1] == null) {
                arr_str[1] = (arr_str[0]).substring(2, 3);
            }
            for (int i = 1; i < 5; i++) {
                svf.VrsOut(field + String.valueOf(i) , arr_str[i-1] );
            }
        }
    }

    private List getPrintStudent(final DB2UDB db2) throws SQLException {
        final List rtnList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = getStudentSql();
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String barcode = rs.getString("BARCODE");
                final String name = "1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME") : rs.getString("NAME");
                final String birthday = rs.getString("BIRTHDAY");
                final String courseName = rs.getString("COURSENAME");
                final String majorName = rs.getString("MAJORNAME");
                final String addr1 = rs.getString("ADDR1");
                final String addr2 = rs.getString("ADDR2");
                final String grade = rs.getString("GRADE");
                final String hrName = rs.getString("HR_CLASS_NAME1");
                final Student student = new Student(
                        schregno,
                        barcode,
                        name,
                        birthday,
                        courseName,
                        majorName,
                        addr1,
                        addr2,
                        grade,
                        hrName);
                rtnList.add(student);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnList;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append("WITH SCHNO AS ( ");
        stb.append(" SELECT ");
        stb.append("    * ");
        stb.append(" FROM ");
        stb.append("    SCHREG_REGD_DAT ");
        stb.append(" WHERE ");
        stb.append("    YEAR='" + _param._year + "' ");
        stb.append("    AND SEMESTER='" + _param._semester + "' ");
        if (_param._printKojin) {
            stb.append("    AND SCHREGNO IN " + _param._inState + " ");
        }
        if (_param._printClass) {
            stb.append("    AND GRADE || HR_CLASS IN " + _param._inState + " ");
        }
        stb.append(" ) ");

        stb.append(" , SCHREG_ADDRESS_MAX AS ( ");
        stb.append(" SELECT ");
        stb.append("     SCHREGNO, ");
        stb.append("     MAX(ISSUEDATE) AS ISSUEDATE ");
        stb.append(" FROM ");
        stb.append("     SCHREG_ADDRESS_DAT ");
        stb.append(" WHERE ");
        stb.append("     SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) ");
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

        /**********************************************************************
        **                 学籍番号から図書ＩＤコードへの変換                  **
        ***********************************************************************
        **
        ** 図書ＩＤ用のテーブルが用意できない場合の、機械的に算出する方法
        ** 通番3桁にクラス情報と出席番号を入れ込む案
        **
        ** (定義)
        ** 　学籍番号：20090101(西暦4桁＋組2桁＋番号2桁)
        ** 　図書ＩＤ：26090016(お約束2桁＋西暦2桁＋通番3桁＋チェックデジット1桁)
        **
        ** １クラスあたりの在籍数を最大50とし、50単位でクラスを判断させる
        ** 年度当たり20クラスの管理が可能
        **
        ** (計算式１)・・・チェックデジット以外の7桁を求める
        ** 　2600000
        ** 　+ (INT(MOD(20090101/10000,100))*1000)
        ** 　+ (INT(MOD(20090101/100,100))-1)*50
        ** 　+ (INT(MOD(20090101,100)))
        ** 　= 2609001
        **
        **********************************************************************/
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     2600000 ");
        stb.append("     + (INT(MOD(INT(T1.SCHREGNO)/10000,100))*1000) ");
        stb.append("     + (INT(MOD(INT(T1.SCHREGNO)/100,100))-1)*50 ");
        stb.append("     + (INT(MOD(INT(T1.SCHREGNO),100))) ");
        stb.append("     AS BARCODE, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T3.HR_CLASS_NAME1, ");
        stb.append("     T2.NAME, ");
        stb.append("     T2.REAL_NAME, ");
        stb.append("     (CASE WHEN L4.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS USE_REAL_NAME, ");
        stb.append("     T2.BIRTHDAY, ");
        stb.append("     L3.COURSENAME, ");
        stb.append("     L1.MAJORNAME, ");
        stb.append("     L2.ADDR1, ");
        stb.append("     L2.ADDR2 ");
        stb.append(" FROM ");
        stb.append("     SCHNO T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR=T1.YEAR AND T3.SEMESTER=T1.SEMESTER AND T3.GRADE=T1.GRADE AND T3.HR_CLASS=T1.HR_CLASS ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO=T1.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_ADDRESS L2 ON L2.SCHREGNO=T1.SCHREGNO ");
        stb.append("     LEFT JOIN COURSE_MST L3 ON L3.COURSECD=T1.COURSECD ");
        stb.append("     LEFT JOIN MAJOR_MST L1 ON L1.COURSECD = T1.COURSECD AND L1.MAJORCD = T1.MAJORCD ");
        stb.append("     LEFT JOIN SCHREG_NAME_SETUP_DAT L4 ON L4.SCHREGNO = T1.SCHREGNO AND L4.DIV = '05' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO ");
        return stb.toString();
    }

    private class Student {
        final String _schregno;
        final String _barcode;
        final String _name;
        final String _birthday;
        final String _courseName;
        final String _majorName;
        final String _addr1;
        final String _addr2;
        final String _photoName;
        final String _grade;
        final String _hrName;

        Student(final String schregno,
                final String barcode,
                final String name,
                final String birthday,
                final String courseName,
                final String majorName,
                final String addr1,
                final String addr2,
                final String grade,
                final String hrName
        ) {
            _schregno = schregno;
            _barcode = barcode;
            _name = name;
            _birthday = birthday;
            _courseName = courseName;
            _majorName = majorName;
            _addr1 = addr1;
            _addr2 = addr2;
            _photoName = "P" + schregno;
            _grade = grade;
            _hrName = hrName;
        }

        private String getGradeName() {
            String majorName = (null == _majorName) ? "" : _majorName;
            String grade = (null == _grade) ? "" : "第" + _grade.substring(1) + "学年";
            String hrName = (null == _hrName) ? "" : _hrName;
            return majorName + grade + hrName;
        }

        /***********************************************************************
         ** 　※チェックデジット1桁を求める。以下、参考
         ***********************************************************************
         **
         ** (計算式２)・・・チェックデジット1桁を求める（例：2609001□）
         **
         ** 　求めるチェックデジットを1桁目として右端から左方向に「桁番号」を付ける。
         **
         ** 　1)すべての偶数位置の数字を加算する。
         ** 　2)1の結果を3倍する。
         ** 　3)すべての奇数位置の数字を加算する。
         ** 　4)2の答えと3の答えを加算する。
         ** 　5)最後に'24'の下1桁の数字を'10'から引く。
         ** 　　この場合は'10'から'4'を引き算した答えの'6'がチェックデジットである。
         ** 　6)下1桁が'0'となった場合は、チェックデジットはそのまま'0'となる。
         **
         **   ---------------------------
         ** 　|桁 番 号| 8 7 6 5 4 3 2 1 |
         **   ---------------------------
         ** 　|　 例 　| 2 6 0 9 0 0 1 □|
         **   ---------------------------
         ** 　|偶数位置| 2 + 0 + 0 + 1   |
         **   ---------------------------
         ** 　|奇数位置|   6 + 9 + 0     |
         **   ---------------------------
         **
         ** 　1)2+0+0+1 = 3
         ** 　2)3 * 3   = 9
         ** 　3) 6+9+0  = 15
         ** 　4)9 + 15  = 24
         ** 　5)10 - 4  = 6　・・・チェックデジット
         **
         **********************************************************************/
        private String getJancode() {
            if (null == _barcode) return _barcode;

            String str = _barcode;
            int strLen = _barcode.length();
            int gusuu = 0;
            int kisuu = 0;
            for (int i = 0; i < strLen; i++) {
                String code = str.substring((strLen - (i + 1)), (strLen - i));
                if (0 == i % 2) {
                    // 1)偶数位置の数字を加算
                    gusuu += Integer.parseInt(code);
                } else {
                    // 3)奇数位置の数字を加算
                    kisuu += Integer.parseInt(code);
                }
            }
            // 4)1の結果を3倍と3の結果を加算
            String strKasan = String.valueOf((gusuu * 3) + kisuu);
            // 4の結果の下1桁の数字を'10'から引く。下1桁が'0'の場合は'0'
            int kasan1 = Integer.parseInt(strKasan.substring(strKasan.length() - 1));
            String cd = (0 < kasan1) ? String.valueOf(10 - kasan1) : "0";

            return _barcode + cd;
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
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private boolean _printKojin;
        private boolean _printClass;
        private String _inState;
        private String _sDate;
        private String _eDate;
        private String _imagePass;
        private String _stampName;
        private String _logoName;
        private boolean _isSeireki;
        private String _schoolName;
        private String _schoolAddress;
        private String _jobName;
        private String _principalName;
        private String _extension;

        private final boolean _isPrintOmote;
        private final boolean _isPrintUra;
        private String _busuu;
        private final String _useAddrField2;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");

            String output = request.getParameter("OUTPUT"); // 1:表 2:裏
            _isPrintOmote = "1".equals(output);
            _isPrintUra = "2".equals(output);

            if (_isPrintUra) {
                _busuu = request.getParameter("BUSUU");
            } else {
                String disp = request.getParameter("DISP"); // 1:個人,2:クラス
                _printKojin = "1".equals(disp);
                _printClass = "2".equals(disp);
                final String classcd[] = request.getParameterValues("category_selected");
                String sep = "";
                final StringBuffer stb = new StringBuffer();
                stb.append("(");
                for( int ia=0 ; ia<classcd.length ; ia++ ){
                    stb.append(getInstate(classcd[ia], sep));
                    sep = ",";
                }
                stb.append(")");
                _inState = stb.toString();
                
                final String sDate = request.getParameter("TERM_SDATE");
                _sDate = sDate.replace('/', '-');
                final String eDate = request.getParameter("TERM_EDATE");
                _eDate = eDate.replace('/', '-');
                String rootPass = request.getParameter("DOCUMENTROOT"); // '/usr/local/development/src'
                _imagePass = rootPass + "/image/";
                
                setSeirekiFlg(db2);
                setSchoolInfo(db2);
                setPhotoFileExtension(db2);
                _stampName = "SCHOOLSTAMP";
                _logoName = "SCHOOLLOGO";
            }
            _useAddrField2 = request.getParameter("useAddrField2");
        }

        private String getInstate(final String classcd, String sep) {
            String rtnSt = "";
            if (_printClass) {
                rtnSt = sep + "'" + classcd + "'";
            } else if (_printKojin) {
                rtnSt = sep + "'" + (classcd).substring(0,(classcd).indexOf("-")) + "'";
            }
            return rtnSt;
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

        private String printDate(final String date) {
            if (null == date) {
                return "";
            }
            if (_isSeireki) {
                return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
            } else {
                return KNJ_EditDate.h_format_JP(date);
            }
        }

        private String printDateFormat(final String date) {
            if (_isSeireki) {
                final String wdate = (null == date) ? date : date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
                return KNJ_EditDate.setDateFormat2(wdate);
            } else {
                final String wdate = (null == date) ? date : KNJ_EditDate.h_format_JP(date);
                return KNJ_EditDate.setDateFormat(wdate, _year);
            }
        }

        private String getFormId() {
            return (_param._isPrintUra) ? "KNJA143C_2.frm" : "KNJA143C.frm";
        }

        private void setSchoolInfo(final DB2UDB db2) {
            try {
                _schoolName = "";
                _schoolAddress = "";
                _jobName = "";
                _principalName = "";
                String sql = "SELECT SCHOOL_NAME,JOB_NAME,PRINCIPAL_NAME,REMARK1 " +
                             "FROM CERTIF_SCHOOL_DAT " +
                             "WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '101' ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    _schoolName = rs.getString("SCHOOL_NAME");
                    _schoolAddress = rs.getString("REMARK1");
                    _jobName = rs.getString("JOB_NAME");
                    _principalName = rs.getString("PRINCIPAL_NAME");
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
    }
}

// eof
