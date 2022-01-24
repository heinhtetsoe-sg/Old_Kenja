// kanji=漢字
/*
 * $Id: 9228455eae7b3b635e9918aab331c1fd18af39ab $
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
 * @version $Id: 9228455eae7b3b635e9918aab331c1fd18af39ab $
 */
public class KNJA143G {

    private static final Log log = LogFactory.getLog("KNJA143G.class");

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

        final int maxBusuu = 0;
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
        final String stampCheck = _param._imagePass + _param._stampName + "." + "bmp";
        final String logoCheck  = _param._imagePass + _param._logoName  + "." + _param._extension;
        log.debug("stampCheck：" + stampCheck);
        log.debug("logoCheck：" + logoCheck);
        final boolean isStamp = isFileExists(stampCheck);
        final boolean isLogo  = isFileExists(logoCheck);
        for (final Iterator it = printStudents.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();

            //表
            svf.VrSetForm("KNJA143G_1.frm", 4);
            log.debug("フォーム：KNJA143G_1.frm");
            //写真
            final String photoCheck = _param._imagePass + student._photoName + "." + _param._extension;
            log.debug("photoCheck：" + photoCheck);
            if (isFileExists(photoCheck)) {
                svf.VrsOut("PHOTO_BMP", photoCheck );//顔写真
            }
            //学校長印
            if (isStamp) {
                svf.VrsOut("STAMP_BMP", stampCheck );//学校印
            }
            //学校ロゴ
            if (isLogo) {
                svf.VrsOut("SCHOOL_BMP", logoCheck );//学校ロゴ
            }
            //タイトル
            svf.VrsOut("TITLE", "身分証明書");
            //学籍番号
            svf.VrsOut("SCHREGNO", student._schregno);
//            svf.VrsOut("BARCODE_NO", student.getJancode());
//            svf.VrsOut("BARCODE", student._barcode);
//            //所属
//            String courseName = (student._courseName == null ? "" : student._courseName) + "課程";
//            svf.VrsOut("COURSE", courseName);
//            //学科・年組
//            svf.VrsOut("GRADE", student.getGradeName());
            //生徒氏名
            svf.VrsOut("NAME", student._name);
            //生徒住所
//            int addrketa;
            String addr1Field;
            String addr2Field;
            final int len1 = null == student._addr1 ? 0 : student._addr1.length();
            final int len2 = null == student._addr2 ? 0 : student._addr2.length();
            if (len1 > 25 || len2 > 25) {
                addr1Field = "ADDRESS1_5";
                addr2Field = "ADDRESS2_5";
            } else if (len1 > 20 || len2 > 20) {
                addr1Field = "ADDRESS1_4";
                addr2Field = "ADDRESS2_4";
            } else if (len1 > 18 || len2 > 18) {
                addr1Field = "ADDRESS1_3";
                addr2Field = "ADDRESS2_3";
            } else if (len1 > 16 || len2 > 16) {
                addr1Field = "ADDRESS1_2";
                addr2Field = "ADDRESS2_2";
            } else {
                addr1Field = "ADDRESS1";
                addr2Field = "ADDRESS2";
            }
//            svfFieldModifyAddress(svf, addrketa, addr1Field, addr2Field, student._addr1, student._addr2);
            svf.VrsOut(addr1Field, student._addr1);
            svf.VrsOut(addr2Field, student._addr2);
            //生徒・教員
            svf.VrsOut("SENTENCE", "生徒");
            //学校データ
            if (null != _param._schoolAddress) {
                final String field;
                if (_param._schoolAddress.length() > 30) {
                    field = "SCHOOLADDRESS5";
                } else if (_param._schoolAddress.length() > 25) {
                    field = "SCHOOLADDRESS4";
                } else if (_param._schoolAddress.length() > 20) {
                    field = "SCHOOLADDRESS3";
                } else if (_param._schoolAddress.length() > 18) {
                    field = "SCHOOLADDRESS2";
                } else {
                    field = "SCHOOLADDRESS1";
                }
                svf.VrsOut(field, _param._schoolAddress);
            }
            svf.VrsOut("TELNO", _param._schoolTelno);
            svf.VrsOut("SCHOOLNAME1", _param._schoolName);
            svf.VrsOut("JOBNAME", _param._jobName);
            svf.VrsOut("STAFFNAME", _param._principalName);
            //日付
//            svf.VrsOut("BIRTHDAY1", _param.printDateFormat(student._birthday));
//            svf.VrsOut("SDATE", _param.printDateFormat(_param._sDate) + "発行");
//            svf.VrsOut("EDATE", _param.printDateFormat(_param._eDate));
            svf.VrsOut("BIRTHDAY1", _param.printDate(student._birthday));
            svf.VrsOut("SDATE", _param.printDate(_param._sDate) + "発行");
            svf.VrsOut("EDATE", _param.printDate(_param._eDate));
            //出力
            svf.VrEndRecord();

            //裏
            svf.VrSetForm("KNJA143G_2.frm", 4);
            log.debug("フォーム：KNJA143G_2.frm");
            //バーコード
            svf.VrsOut("BARCODE", student.getJancode2());
//            svf.VrsOut("BARCODE", student._barcode);
            //生徒氏名(ローマ字)
            if (null != student._nameEng) {
                final String strz = null == student._nameEng ? "" : student._nameEng;
                final int z = strz.indexOf(" "); // 空白文字の位置
                final String strx = z < 0 ? strz : strz.substring(0, z); // 姓
                final String stry = z < 0 ? ""   : strz.substring(z + 1); // 名
                final int lenx = getMS932ByteLength(strx);
                final int leny = getMS932ByteLength(stry);
                final String field;
                if (lenx <= 14 && leny <= 14) {
                    field = "1";
                } else {
                    field = "2";
                }
                svf.VrsOut("ENAME1_" + field, strx);
                svf.VrsOut("ENAME2_" + field, stry);
            }
            //生徒氏名(漢字)
            if (null != student._name) {
                final String strz = null == student._name ? "" : student._name;
                final int z = strz.indexOf("　"); // 空白文字の位置
                final String strx = z < 0 ? strz : strz.substring(0, z); // 姓
                final String stry = z < 0 ? ""   : strz.substring(z + 1); // 名
                final int lenx = getMS932ByteLength(strx);
                final int leny = getMS932ByteLength(stry);
                final String field;
                if (lenx <= 6 && leny <= 6) {
                    field = "1";
                } else if (lenx <= 8 && leny <= 8) {
                    field = "2";
                } else if (lenx <= 12 && leny <= 12) {
                    field = "3";
                } else {
                    field = "4";
                }
                svf.VrsOut("NAME1_" + field, strx);
                svf.VrsOut("NAME2_" + field, stry);
            }
            //出力
            svf.VrEndRecord();

            _hasData = true;
        }
    }

    private int getMS932ByteLength(final String s) {
        int rtn = 0;
        if (null != s) {
            try {
                rtn = s.getBytes("MS932").length;
            } catch (final Exception e) {
                log.error("exception!", e);
                rtn = s.length();
            }
        }
        return rtn;
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
        log.debug("getStudentSql :" + sql);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String barcode = rs.getString("BARCODE");
                final String nameEng = rs.getString("NAME_ENG");
                final String name = "1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME") : rs.getString("NAME");
                final String birthday = rs.getString("BIRTHDAY");
                final String courseName = rs.getString("COURSENAME");
                final String majorName = rs.getString("MAJORNAME");
                final String addr1 = rs.getString("ADDR1");
                final String addr2 = rs.getString("ADDR2");
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String attendno = rs.getString("ATTENDNO");
                final String hrName = rs.getString("HR_CLASS_NAME1");
                final String entDate = rs.getString("ENT_DATE");
                final String entYear = rs.getString("ENT_YEAR");
                final String entHrClass = rs.getString("ENT_HR_CLASS");
                final String entAttendno = rs.getString("ENT_ATTENDNO");
                final String entSchoolKind = rs.getString("ENT_SCHOOL_KIND");
                final Student student = new Student(
                        schregno,
                        barcode,
                        nameEng,
                        name,
                        birthday,
                        courseName,
                        majorName,
                        addr1,
                        addr2,
                        grade,
                        hrClass,
                        attendno,
                        hrName,
                        entDate,
                        entYear,
                        entHrClass,
                        entAttendno,
                        entSchoolKind);
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

        //バーコード出力データ
        stb.append(" , SCHREG_BASE_ENT AS ( ");
        stb.append(" SELECT ");
        stb.append("     SCHREGNO, ");
        stb.append("     FISCALYEAR(ENT_DATE) AS ENT_YEAR ");
        stb.append(" FROM ");
        stb.append("     SCHREG_BASE_MST ");
        stb.append(" WHERE ");
        stb.append("     SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) ");
        stb.append("     ) ");
        stb.append(" , SCHREG_REGD_MIN AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.YEAR, ");
        stb.append("     MIN(T1.SEMESTER) AS SEMESTER ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_BASE_ENT T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.ENT_YEAR = T1.YEAR ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.YEAR ");
        stb.append("     ) ");
        stb.append(" , SCHREG_REGD_ENT AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.YEAR AS ENT_YEAR, ");
        stb.append("     T1.HR_CLASS AS ENT_HR_CLASS, ");
        stb.append("     T1.ATTENDNO AS ENT_ATTENDNO, ");
        stb.append("     T3.SCHOOL_KIND AS ENT_SCHOOL_KIND ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_MIN T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("     INNER JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = T1.YEAR AND T3.GRADE = T1.GRADE ");
        stb.append("     )   ");

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
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");

        stb.append("     T2.ENT_DATE, ");
        stb.append("     L5.ENT_YEAR, ");
        stb.append("     L5.ENT_HR_CLASS, ");
        stb.append("     L5.ENT_ATTENDNO, ");
        stb.append("     L5.ENT_SCHOOL_KIND, ");

        stb.append("     T3.HR_CLASS_NAME1, ");
        stb.append("     T2.NAME_ENG, ");
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
        stb.append("     LEFT JOIN SCHREG_REGD_ENT L5 ON L5.SCHREGNO = T1.SCHREGNO ");
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO ");
        return stb.toString();
    }

    private class Student {
        final String _schregno;
        final String _barcode;
        final String _nameEng;
        final String _name;
        final String _birthday;
        final String _courseName;
        final String _majorName;
        final String _addr1;
        final String _addr2;
        final String _photoName;
        final String _grade;
        final String _hrClass;
        final String _attendno;
        final String _hrName;

        final String _entDate;
        final String _entYear;
        final String _entHrClass;
        final String _entAttendno;
        final String _entSchoolKind;

        Student(final String schregno,
                final String barcode,
                final String nameEng,
                final String name,
                final String birthday,
                final String courseName,
                final String majorName,
                final String addr1,
                final String addr2,
                final String grade,
                final String hrClass,
                final String attendno,
                final String hrName,
                final String entDate,
                final String entYear,
                final String entHrClass,
                final String entAttendno,
                final String entSchoolKind
        ) {
            _schregno = schregno;
            _barcode = barcode;
            _nameEng = nameEng;
            _name = name;
            _birthday = birthday;
            _courseName = courseName;
            _majorName = majorName;
            _addr1 = addr1;
            _addr2 = addr2;
            _photoName = "P" + schregno;
            _grade = grade;
            _hrClass = hrClass;
            _attendno = attendno;
            _hrName = hrName;

            _entDate = entDate;
            _entYear = entYear;
            _entHrClass = entHrClass;
            _entAttendno = entAttendno;
            _entSchoolKind = entSchoolKind;
        }

        private String getGradeName() {
            String majorName = (null == _majorName) ? "" : _majorName;
            String grade = (null == _grade) ? "" : "第" + _grade.substring(1) + "学年";
            String hrName = (null == _hrName) ? "" : _hrName;
            return majorName + grade + hrName;
        }

        private String getBarcode() {
            String entYear = (null == _entYear) ? "" : _entYear.substring(2); //入学年度:下2桁
            String entAttendno = (null == _entAttendno) ? "" : _entAttendno.substring(1); //入学年度の出席番号:下2桁
            //入学年度の組:下1桁。中学から入学はそのまま。高校から入学は+4する。
            String entHrClass = "";
            if (null != _entHrClass) {
                entHrClass = _entHrClass.substring(2);
                if ("H".equals(_entSchoolKind)) {
                    entHrClass = String.valueOf(Integer.parseInt(entHrClass) + 4);
                }
            }
            return "643001" + entYear + entHrClass + entAttendno;
        }

        private String getJancode2() {
            if (null == getBarcode()) return getBarcode();

            String str = getBarcode();
            int strLen = getBarcode().length();
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

            return getBarcode() + cd;
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
        private String _schoolTelno;
        private String _jobName;
        private String _principalName;
        private String _imagepass;
        private String _extension;

        private final boolean _isPrintOmote;
        private final boolean _isPrintUra;
        private final String _useAddrField2;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("GAKKI");

            String output = "1"; // 1:表 2:裏
            _isPrintOmote = "1".equals(output);
            _isPrintUra = "2".equals(output);

            if (_isPrintUra) {
            } else {
                String disp = "1"; // 1:個人,2:クラス
                _printKojin = "1".equals(disp);
                _printClass = "2".equals(disp);
                final String schregno[] = request.getParameterValues("category_selected");
                String sep = "";
                final StringBuffer stb = new StringBuffer();
                stb.append("(");
                for( int ia=0 ; ia<schregno.length ; ia++ ){
                    stb.append(getInstate(schregno[ia], sep));
                    sep = ",";
                }
                stb.append(")");
                _inState = stb.toString();

                final String sDate = request.getParameter("TERM_SDATE");
                _sDate = sDate.replace('/', '-');
                final String eDate = request.getParameter("TERM_EDATE");
                _eDate = eDate.replace('/', '-');
                String rootPass = request.getParameter("DOCUMENTROOT"); // '/usr/local/development/src'

                setSeirekiFlg(db2);
                setSchoolInfo(db2);
                setPhotoFileExtension(db2);
                _imagePass = rootPass + "/" + _imagepass + "/";
                _stampName = "SCHOOLSTAMP";
                _logoName = "SCHOOLLOGO";
            }
            _useAddrField2 = request.getParameter("useAddrField2");
        }

        private String getInstate(final String classcd, String sep) {
            String rtnSt = sep + "'" + classcd + "'";
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
                    _isSeireki = true; //西暦
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
            return (_param._isPrintUra) ? "KNJA143G_2.frm" : "KNJA143G_1.frm";
        }

        private void setSchoolInfo(final DB2UDB db2) {
            try {
                _schoolName = "";
                _schoolAddress = "";
                _schoolTelno = "";
                _jobName = "";
                _principalName = "";
                String sql = "SELECT SCHOOL_NAME,JOB_NAME,PRINCIPAL_NAME,REMARK1,REMARK3 " +
                             "FROM CERTIF_SCHOOL_DAT " +
                             "WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '101' ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    _schoolName = rs.getString("SCHOOL_NAME");
                    _schoolAddress = rs.getString("REMARK1");
                    _schoolTelno = rs.getString("REMARK3");
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
                _imagepass = "";
                _extension = "";
                String sql = "SELECT IMAGEPATH,EXTENSION FROM CONTROL_MST WHERE CTRL_NO = '01' ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    _imagepass = rs.getString("IMAGEPATH");
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
