/*
 * $Id: e0f22f71a0411ebd724698bb76339c7a509bbc61 $
 *
 * 作成日: 2018/12/18
 * 作成者: matsushima
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJE016 {

    private static final Log log = LogFactory.getLog(KNJE016.class);

    private boolean _hasData;

    private Param _param;

	private String bithdayField;


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
            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final List printList = getList(db2);

        final String gengou = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._ctrlYear));
        final int remarkLen = 30; //1行あたりのリマーク文字数(バイト)
        final int remarkLine = 66; //リマーク最大行数

        String gradeHrClass = ""; //年組
        int grpcnt = 0;
        int remarkCnt = 0;
        int nextCnt = remarkCnt + 1;
        String [] remark1;
        String [] remark2;
        String [] remark3;
        String [] remark4;
        String [] remark5;
        String [] remark6;
        boolean contFlg = true; //繰り返しフラグ

        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintData printData = (PrintData) iterator.next();

            if(!gradeHrClass.equals(printData._grade + printData._hr_Class)) {
                svf.VrSetForm("KNJE016.frm", 4);
                setTitle(svf, printData, gengou);//ヘッダ
                gradeHrClass = printData._grade + printData._hr_Class;
            }

            remark1 = KNJ_EditEdit.get_token(printData._remark1,remarkLen,remarkLine);
            remark2 = KNJ_EditEdit.get_token(printData._remark2,remarkLen,remarkLine);
            remark3 = KNJ_EditEdit.get_token(printData._remark3,remarkLen,remarkLine);
            remark4 = KNJ_EditEdit.get_token(printData._remark4,remarkLen,remarkLine);
            remark5 = KNJ_EditEdit.get_token(printData._remark5,remarkLen,remarkLine);
            remark6 = KNJ_EditEdit.get_token(printData._remark6,remarkLen,remarkLine);

            //No.
            svf.VrsOut("NO" , printData._attendNo);
            //氏名
            if (20 >= KNJ_EditEdit.getMS932ByteLength(printData._name)) {
                svf.VrsOut("NAME1" , printData._name);
            }else if (30 >= KNJ_EditEdit.getMS932ByteLength(printData._name)) {
                svf.VrsOut("NAME2" , printData._name);
            }else {
                svf.VrsOut("NAME3" , printData._name);
            }

            while(contFlg) {

                //出力グループ
                svf.VrsOut("GRPCD1" , String.valueOf(grpcnt)); //NO
                svf.VrsOut("GRPCD2" , String.valueOf(grpcnt)); //氏名
                svf.VrsOut("GRPCD3" , String.valueOf(grpcnt)); //学習における特徴等
                svf.VrsOut("GRPCD4" , String.valueOf(grpcnt)); //行動の特徴、特技
                svf.VrsOut("GRPCD5" , String.valueOf(grpcnt)); //部活動、ボランティア活動、留学・海外経験等
                svf.VrsOut("GRPCD6" , String.valueOf(grpcnt)); //取得資格、検定
                svf.VrsOut("GRPCD7" , String.valueOf(grpcnt)); //表彰、顕彰等の記録
                svf.VrsOut("GRPCD80" , String.valueOf(grpcnt)); //その他

                //学習における特徴等
                svf.VrsOut("CONTENT1" , rtnPrintDataStr(remark1, remarkCnt));

                //行動の特徴、特技
                svf.VrsOut("CONTENT2" , rtnPrintDataStr(remark2, remarkCnt));

                //部活動、ボランティア活動、留学・海外経験等
                svf.VrsOut("CONTENT3" , rtnPrintDataStr(remark3, remarkCnt));

                //取得資格、検定
                svf.VrsOut("CONTENT4" , rtnPrintDataStr(remark4, remarkCnt));

                //表彰・顕彰等の記録
                svf.VrsOut("CONTENT5" , rtnPrintDataStr(remark5, remarkCnt));

                //その他
                svf.VrsOut("CONTENT6" , rtnPrintDataStr(remark6, remarkCnt));

                if(nextCnt == remarkLine) {
                    contFlg = false;
                }else {
                    if(rtnPrintDataStr(remark1, nextCnt) == "" && rtnPrintDataStr(remark2, nextCnt) == ""
                            && rtnPrintDataStr(remark3, nextCnt) == "" && rtnPrintDataStr(remark4, nextCnt) == ""
                            && rtnPrintDataStr(remark5, nextCnt) == "" && rtnPrintDataStr(remark6, nextCnt) == "") {
                        contFlg = false;
                    }
                }

                if(_param._oneline) contFlg = false;

                remarkCnt++;
                nextCnt = remarkCnt + 1;
                svf.VrEndRecord();
            }
            //次のグループへ切り替え
            grpcnt++;

            //繰り返しで使用する変数の初期化
            remarkCnt = 0;
            nextCnt = remarkCnt + 1;
            contFlg = true;

            _hasData = true;
        }
        if (_hasData) {
        	svf.VrEndRecord();
        }
    }

    private void setTitle(final Vrw32alp svf, PrintData printData, String gengou) {

        svf.VrsOut("TITLE", gengou +"年度 指導上参考となる諸事項（6分割） 一覧表");

        for (int i = 0; i < Math.min(_param._nameList.size(), 5); i++) {
            final String name = _param._nameList.get(i);
            svf.VrsOut("JOB_NAME" + String.valueOf(i + 1), name);
        }

        //担任
        svf.VrsOut("JOB_NAME6", "担任");
        String teachername = printData._staffname != null ? printData._staffname : "";
        svf.VrsOut("TEACHER_NAME", teachername);

        //年組
        svf.VrsOut("HR_NAME", printData._hr_Name);

    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregNo = rs.getString("SCHREGNO");
                final String grade = rs.getString("GRADE");
                final String hr_Class = rs.getString("HR_CLASS");
                final String hr_Name = rs.getString("HR_NAME");
                final String attendNo = rs.getString("ATTENDNO");
                final String name = rs.getString("NAME");
                final String remark1 = rs.getString("REMARK1");
                final String remark2 = rs.getString("REMARK2");
                final String remark3 = rs.getString("REMARK3");
                final String remark4 = rs.getString("REMARK4");
                final String remark5 = rs.getString("REMARK5");
                final String remark6 = rs.getString("REMARK6");
                final String staffname = rs.getString("STAFFNAME");

                final PrintData printData = new PrintData(schregNo, grade, hr_Class, hr_Name, attendNo, name, remark1, remark2, remark3, remark4, remark5, remark6, staffname);
                retList.add(printData);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO    AS SCHREGNO , ");
        stb.append("     T3.GRADE       AS GRADE    , ");
        stb.append("     T3.HR_CLASS    AS HR_CLASS , ");
        stb.append("     T3.HR_NAME     AS HR_NAME  , ");
        stb.append("     T1.ATTENDNO    AS ATTENDNO , ");
        stb.append("     T2.NAME_SHOW   AS NAME     , ");
        stb.append("     T4_1.REMARK    AS REMARK1  , ");
        stb.append("     T4_2.REMARK    AS REMARK2  , ");
        stb.append("     T4_3.REMARK    AS REMARK3  , ");
        stb.append("     T4_4.REMARK    AS REMARK4  , ");
        stb.append("     T4_5.REMARK    AS REMARK5  , ");
        stb.append("     T4_6.REMARK    AS REMARK6  , ");
        stb.append("     T5.STAFFNAME   AS STAFFNAME  ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T2 ");
        stb.append("     ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT T3 ");
        stb.append("     ON T3.YEAR     = T1.YEAR ");
        stb.append("     AND T3.SEMESTER = T1.SEMESTER ");
        stb.append("     AND T3.GRADE    = T1.GRADE ");
        stb.append("     AND T3.HR_CLASS = T1.HR_CLASS ");
        stb.append("     LEFT JOIN HEXAM_ENTREMARK_TRAINREF_DAT T4_1 ");
        stb.append("     ON T4_1.YEAR     = T1.YEAR ");
        stb.append("     AND T4_1.SCHREGNO = T1.SCHREGNO ");
        stb.append("     AND T4_1.TRAIN_SEQ = '001' ");
        stb.append("     LEFT JOIN HEXAM_ENTREMARK_TRAINREF_DAT T4_2 ");
        stb.append("     ON T4_2.YEAR     = T1.YEAR ");
        stb.append("     AND T4_2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     AND T4_2.TRAIN_SEQ = '002' ");
        stb.append("     LEFT JOIN HEXAM_ENTREMARK_TRAINREF_DAT T4_3 ");
        stb.append("     ON T4_3.YEAR     = T1.YEAR ");
        stb.append("     AND T4_3.SCHREGNO = T1.SCHREGNO ");
        stb.append("     AND T4_3.TRAIN_SEQ = '003' ");
        stb.append("     LEFT JOIN HEXAM_ENTREMARK_TRAINREF_DAT T4_4 ");
        stb.append("     ON T4_4.YEAR     = T1.YEAR ");
        stb.append("     AND T4_4.SCHREGNO = T1.SCHREGNO ");
        stb.append("     AND T4_4.TRAIN_SEQ = '004' ");
        stb.append("     LEFT JOIN HEXAM_ENTREMARK_TRAINREF_DAT T4_5 ");
        stb.append("     ON T4_5.YEAR     = T1.YEAR ");
        stb.append("     AND T4_5.SCHREGNO = T1.SCHREGNO ");
        stb.append("     AND T4_5.TRAIN_SEQ = '005' ");
        stb.append("     LEFT JOIN HEXAM_ENTREMARK_TRAINREF_DAT T4_6 ");
        stb.append("     ON T4_6.YEAR     = T1.YEAR ");
        stb.append("     AND T4_6.SCHREGNO = T1.SCHREGNO ");
        stb.append("     AND T4_6.TRAIN_SEQ = '006' ");
        stb.append("     LEFT JOIN STAFF_MST T5 ");
        stb.append("     ON T5.STAFFCD = T3.TR_CD1 ");
        stb.append(" WHERE                                                ");
        stb.append("     T1.YEAR         = '" + _param._ctrlYear + "'     ");
        stb.append("     AND T1.SEMESTER = '" + _param._ctrlSemester + "' ");
        if ("1".equals(_param._disp)) {
            stb.append("     AND T1.GRADE || T1.HR_CLASS IN (" + _param._sqlInstate + ") ");
        } else {
            stb.append("     AND T1.SCHREGNO IN (" + _param._sqlInstate + ")             ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO ");

        return stb.toString();
    }

    private class PrintData {
        final String _schregNo;
        final String _grade;
        final String _hr_Class;
        final String _hr_Name;
        final String _attendNo;
        final String _name;
        final String _remark1;
        final String _remark2;
        final String _remark3;
        final String _remark4;
        final String _remark5;
        final String _remark6;
        final String _staffname;

        public PrintData(
                final String schregNo,
                final String grade,
                final String hr_Class,
                final String hr_Name,
                final String attendNo,
                final String name,
                final String remark1,
                final String remark2,
                final String remark3,
                final String remark4,
                final String remark5,
                final String remark6,
                final String staffname
        ) {
        	_schregNo = schregNo;
            _grade = grade;
            _hr_Class = hr_Class;
            _hr_Name = hr_Name;
            _attendNo = attendNo;
            _name = name;
            _remark1 = remark1;
            _remark2 = remark2;
            _remark3 = remark3;
            _remark4 = remark4;
            _remark5 = remark5;
            _remark6 = remark6;
            _staffname = staffname;
        }
    }

    private List<String> getNameList(final DB2UDB db2, final Param param) {
        final List<String> retList = new ArrayList<String>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getNameSql(param._ctrlYear, "D055");
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String name = rs.getString("NAME1");

                retList.add(name);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getNameSql(final String year, final String namecd1) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + year + "' AND NAMECD1 = '" + namecd1 + "' ORDER BY NAMECD2 ");

        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 71297 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _disp;
        private final String _gradeHrClass;
        private final String[] _categorySelected;
        private final String _sqlInstate;
        private final boolean _oneline;
        final String _useSchool_KindField;
        final String SCHOOLCD;
        final String SCHOOLKIND;
        final List<String> _nameList;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear     = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _disp          = request.getParameter("DISP");
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _oneline = "on".equals(request.getParameter("ONELINE")) ? true : false;
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            SCHOOLCD      = request.getParameter("SCHOOLCD");
            SCHOOLKIND    = request.getParameter("SCHOOLKIND");


            String setInstate = "";
            String sep = "";
            for (int i = 0; i < _categorySelected.length; i++) {
                final String selectVal = _categorySelected[i];
                final String[] setVal = StringUtils.split(selectVal, "-");
                setInstate += sep + "'" + setVal[0] + "'";
                sep = ",";
            }
            _sqlInstate = setInstate;

            _nameList = getPrgStampDat(db2);
            if (_nameList.isEmpty()) {
            	_nameList.addAll(getNameList(db2, this));
            }
        }
        
        private List getPrgStampDat(final DB2UDB db2) throws SQLException {

            final List list = new ArrayList();

            if (KnjDbUtils.setTableColumnCheck(db2, "PRG_STAMP_DAT", null)) {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT  ");
                stb.append("    T1.SEQ ");
                stb.append("  , T1.TITLE ");
                stb.append(" FROM PRG_STAMP_DAT T1 ");
                stb.append(" WHERE ");
                stb.append("   T1.YEAR = '" + _ctrlYear + "' ");
                stb.append("   AND T1.SEMESTER = '9' ");
                if ("1".equals(_useSchool_KindField)) {
                    stb.append("   AND T1.SCHOOLCD = '" + SCHOOLCD + "' ");
                    stb.append("   AND T1.SCHOOL_KIND = '" + SCHOOLKIND + "' ");
                }
                stb.append("   AND T1.PROGRAMID = '" + "KNJE016" + "' ");
                
                for (final Map<String, String> row : KnjDbUtils.query(db2, stb.toString())) {
                	if (NumberUtils.isDigits(KnjDbUtils.getString(row, "SEQ"))) {
                		final int idx = Integer.parseInt(KnjDbUtils.getString(row, "SEQ")) - 1;
                		for (int i = list.size(); i <= idx; i++) {
                			list.add(null);
                		}
                		list.set(idx, KnjDbUtils.getString(row, "TITLE"));
                	}
                }
            }
            return list;
        }

    }

    private String rtnPrintDataStr(final String[] printData,final int cnt) {
        if (null == printData) {
            return "";
        }
        return StringUtils.defaultString(printData[cnt]);
    }
}

// eof
