/*
 * $Id$
 *
 * 作成日: 2015/03/13
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJP;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJP961 {

    private static final Log log = LogFactory.getLog(KNJP961.class);

    private final String SYUUNYUU = "01";
    private final String ZATUSYUUNYUU = "02";
    private final String SISYUTU = "03";
    private final String YOBI = "04";

    private final String INCOME = "1";
    private final String OUTGO = "2";

    private final int MAXLETU = 8;
    private final int MAXGYO  = 50;

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
        final Map lmstMap = getLmstMap(db2);

        svf.VrSetForm("KNJP961.frm", 1);

        for (Iterator iterator = lmstMap.keySet().iterator(); iterator.hasNext();) {
            final String levyLCd = (String)iterator.next();
            Lmst lmst = (Lmst) lmstMap.get(levyLCd);

            //タイトル文字列作成
            final String gengou = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year));
            final String title = gengou + "年度　" + lmst._lName + "　学年別収入伺明細表";

            int hrGrpIndex = 0;
            while(hrGrpIndex < lmst._hrClassGrpList.size()) {

                //ヘッダ作成
                printHeader(db2, svf, title);

                final HrClassGrp hrClassGrp = (HrClassGrp)lmst._hrClassGrpList.get(hrGrpIndex);
                int retuCnt = 1;
                int billCnt = 0;
                for (Iterator itHr = hrClassGrp._hrKeyList.iterator(); itHr.hasNext();) {
                  final String hrKey = (String)itHr.next();
                  HrClass hrClass = (HrClass) lmst._hrClassMap.get(hrKey);

                  svf.VrsOut("HR_NAME" + retuCnt, hrClass._hrName);

                  int lineCnt = 1;
                  int hrMoney = 0;
                  int schCnt  = 1;
                  for (Iterator itSch = hrClass.getSchListIterator(); hrClass.hasNextSch();) {
                      //ページ当たりの最大行数を超えた生徒の表示はは次ページに持ち越す
                      if (schCnt > MAXGYO) {
                          break;
                      }

                      SchregDat schregDat = (SchregDat) itSch.next();
                      svf.VrsOutn("NO" + retuCnt, lineCnt, schregDat._attendno);
                      final String nameSoeji = getMS932Length(schregDat._name) > 12 ? "2" : "1";
                      svf.VrsOutn("NAME" + retuCnt + "_" + nameSoeji, lineCnt, schregDat._name);
                      svf.VrsOutn("BILL" + retuCnt, lineCnt, schregDat._incomeMoney);

                      lineCnt++;
                      billCnt += Integer.parseInt(schregDat._incomeMoney) > 0 ? 1 : 0;
                      hrMoney += Integer.parseInt(schregDat._incomeMoney);

                      _hasData = true;
                      schCnt++;
                  }

                  //クラス合計額
                  svf.VrsOutn("BILL" + retuCnt, 51, String.valueOf(hrMoney));
                  retuCnt++;
                }

                //下段合計
                svf.VrsOut("TOTAL_NUM", billCnt + "名/" + lmst._totalCnt + "名");
                svf.VrsOut("TOTAL_MONEY", String.valueOf(lmst._totalMoney));

                //ページ内に収まりきらなかった生徒が残っていない場合、次のクラスグループに進む
                if (!hrClassGrp.hasHrClassRemainingSch()) {
                    hrGrpIndex++;
                }

                svf.VrEndPage();
            }
        }

    }

    private void printHeader(final DB2UDB db2, final Vrw32alp svf, final String title) {

        svf.VrsOut("TITLE", title);

        if ("1".equals(_param._printDiv)) {
            svf.VrsOut("SUBTITLE2", "伝票番号：" + _param._requestNo);
        } else {
            if ("1".equals(_param._incomeDiv)) {
                final String reqYmF = KNJ_EditDate.h_format_Seireki_M(_param._reqYearMonthF + "-01");
                final String reqYmT = KNJ_EditDate.h_format_Seireki_M(_param._reqYearMonthT + "-01");
                svf.VrsOut("SUBTITLE2", "収入伺日：" + reqYmF + "\uFF5E" + reqYmT);
            } else {
                final String inYmF = KNJ_EditDate.h_format_Seireki_M(_param._inYearMonthF + "-01");
                final String inYmT = KNJ_EditDate.h_format_Seireki_M(_param._inYearMonthT + "-01");
                svf.VrsOut("SUBTITLE2", "収入決定日：" + inYmF + "\uFF5E" + inYmT);
            }
        }
        svf.VrsOut("PRINT_DATE", KNJ_EditDate.h_format_JP(db2, _param._ctrlDate));

    }

    private static int getMS932Length(final String s) {
        if (null != s) {
            try {
                return s.getBytes("MS932").length;
            } catch (Exception e) {
                log.fatal("exception!", e);
            }
        }
        return 0;
    }

    private Map getLmstMap(final DB2UDB db2) {
        final Map lmstMap = new TreeMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSchregSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String levyLCd        = rs.getString("LEVY_L_CD");
                final String levyLName      = rs.getString("LEVY_L_NAME");
                final String schregno       = rs.getString("SCHREGNO");
                final String name           = rs.getString("NAME");
                final String grade          = rs.getString("GRADE");
                final String hrClass        = rs.getString("HR_CLASS");
                final String hrName         = rs.getString("HR_NAME");
                final String attendno       = rs.getString("ATTENDNO");
                final String incomeMoney    = rs.getString("INCOME_MONEY");

                //Lmstをセット
                if (!lmstMap.containsKey(levyLCd)) {
                    lmstMap.put(levyLCd, new Lmst(levyLCd, levyLName));
                }
                final Lmst lmst = (Lmst)lmstMap.get(levyLCd);

                //HrClassをセット
                if (!lmst._hrClassMap.containsKey(hrClass)) {
                    lmst._hrClassMap.put(hrClass, new HrClass(grade, hrClass, hrName));
                }
                final HrClass setHrClass = (HrClass)lmst._hrClassMap.get(hrClass);

                //HrClassGrpをセット
                lmst.addHrClassToGroup(hrClass);

                //SchregDatをセット
                final SchregDat schregDat = new SchregDat(levyLCd, levyLName, schregno, name, grade, hrClass, hrName, attendno, incomeMoney);
                setHrClass._schList.add(schregDat);

                //合計
                lmst._totalCnt++;
                lmst._totalMoney += Integer.parseInt(incomeMoney);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return lmstMap;
    }

    private String getSchregSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCH_MAX AS ( ");
        stb.append("    SELECT ");
        stb.append("        T1.YEAR, ");
        stb.append("        MAX(T1.SEMESTER) AS SEMESTER, ");
        stb.append("        T1.SCHREGNO ");
        stb.append("    FROM ");
        stb.append("        SCHREG_REGD_DAT T1 ");
        stb.append("    WHERE ");
        stb.append("            T1.YEAR  = '" + _param._year + "' ");
        stb.append("        AND T1.GRADE = '" + _param._grade + "' ");
        stb.append("    GROUP BY ");
        stb.append("        T1.YEAR, ");
        stb.append("        T1.SCHREGNO ");
        stb.append(" ), SCH_T AS ( ");
        stb.append("    SELECT ");
        stb.append("        T1.YEAR, ");
        stb.append("        T1.SCHREGNO, ");
        stb.append("        L3.NAME, ");
        stb.append("        T1.GRADE, ");
        stb.append("        L1.SCHOOL_KIND, ");
        stb.append("        T1.HR_CLASS, ");
        stb.append("        L2.HR_NAME, ");
        stb.append("        T1.ATTENDNO ");
        stb.append("    FROM ");
        stb.append("        SCHREG_REGD_DAT T1 ");
        stb.append("        INNER JOIN SCH_MAX T2 ");
        stb.append("             ON T1.SCHREGNO  = T2.SCHREGNO ");
        stb.append("            AND T1.YEAR      = T2.YEAR ");
        stb.append("            AND T1.SEMESTER  = T2.SEMESTER ");
        stb.append("        LEFT JOIN SCHREG_REGD_GDAT L1 ");
        stb.append("             ON T1.YEAR      = L1.YEAR ");
        stb.append("            AND T1.GRADE     = L1.GRADE ");
        stb.append("        LEFT JOIN SCHREG_REGD_HDAT L2 ");
        stb.append("             ON T1.YEAR      = L2.YEAR ");
        stb.append("            AND T1.SEMESTER  = L2.SEMESTER ");
        stb.append("            AND T1.GRADE     = L2.GRADE ");
        stb.append("            AND T1.HR_CLASS  = L2.HR_CLASS ");
        stb.append("        LEFT JOIN SCHREG_BASE_MST L3");
        stb.append("             ON T1.SCHREGNO  = L3.SCHREGNO ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     LMST.LEVY_L_CD, ");
        stb.append("     LMST.LEVY_L_NAME, ");
        stb.append("     SCH_T.SCHREGNO, ");
        stb.append("     SCH_T.NAME, ");
        stb.append("     SCH_T.GRADE, ");
        stb.append("     SCH_T.HR_CLASS, ");
        stb.append("     SCH_T.HR_NAME, ");
        stb.append("     SCH_T.ATTENDNO, ");
        stb.append("     SUM(VALUE(INSCH.INCOME_MONEY, 0)) AS INCOME_MONEY ");
        stb.append(" FROM ");
        stb.append("     LEVY_REQUEST_INCOME_SCHREG_DAT INSCH ");
        stb.append("     INNER JOIN SCH_T ");
        stb.append("          ON SCH_T.YEAR         = INSCH.YEAR ");
        stb.append("         AND SCH_T.SCHREGNO     = INSCH.SCHREGNO ");
        stb.append("         AND SCH_T.SCHOOL_KIND  = INSCH.SCHOOL_KIND ");
        stb.append("     INNER JOIN LEVY_REQUEST_INCOME_DAT INCOME ");
        stb.append("          ON INCOME.YEAR        = INSCH.YEAR ");
        stb.append("         AND INCOME.INCOME_L_CD = INSCH.INCOME_L_CD ");
        stb.append("         AND INCOME.INCOME_M_CD = INSCH.INCOME_M_CD ");
        stb.append("         AND INCOME.REQUEST_NO  = INSCH.REQUEST_NO ");
        stb.append("     LEFT JOIN LEVY_L_MST LMST");
        stb.append("          ON LMST.SCHOOLCD      = INSCH.SCHOOLCD ");
        stb.append("         AND LMST.SCHOOL_KIND   = INSCH.SCHOOL_KIND ");
        stb.append("         AND LMST.YEAR          = INSCH.YEAR ");
        stb.append("         AND LMST.LEVY_L_CD     = INSCH.INCOME_L_CD ");
        stb.append("     WHERE ");
        stb.append("         VALUE(INCOME.INCOME_CANCEL, '0') = '0' ");
        if ("1".equals(_param._printDiv)) {
            stb.append("     AND INCOME.REQUEST_NO = '" + _param._requestNo + "' ");
        } else {
            if ("1".equals(_param._incomeDiv)) {
                stb.append(" AND INCOME.REQUEST_DATE BETWEEN DATE('" + _param._reqYearMonthF + "-01" + "') AND last_day(DATE('" + _param._reqYearMonthT + "-01" + "')) ");
            } else {
                stb.append(" AND INCOME.REQUEST_DATE BETWEEN DATE('" + _param._inYearMonthF + "-01" + "') AND last_day(DATE('" + _param._inYearMonthT + "-01" + "')) ");
            }
        }
        stb.append(" GROUP BY ");
        stb.append("     LMST.LEVY_L_CD, ");
        stb.append("     LMST.LEVY_L_NAME, ");
        stb.append("     SCH_T.SCHREGNO, ");
        stb.append("     SCH_T.NAME, ");
        stb.append("     SCH_T.GRADE, ");
        stb.append("     SCH_T.HR_CLASS, ");
        stb.append("     SCH_T.HR_NAME, ");
        stb.append("     SCH_T.ATTENDNO ");
        stb.append(" ORDER BY ");
        stb.append("     LMST.LEVY_L_CD, ");
        stb.append("     SCH_T.GRADE, ");
        stb.append("     SCH_T.HR_CLASS, ");
        stb.append("     SCH_T.ATTENDNO ");
        stb.append("  ");

        return stb.toString();
    }

    private class Lmst {
        private final String _lCd;
        private final String _lName;
        private int _totalCnt;
        private int _totalMoney;
        private final Map _hrClassMap;
        private final List _hrClassGrpList; //ページあたりのクラスの論理グループ

        public Lmst(
                final String lCd,
                final String lName
        ) {
            _lCd = lCd;
            _lName = lName;
            _hrClassMap     = new TreeMap();
            _hrClassGrpList = new ArrayList();
            _totalCnt = 0;
            _totalMoney = 0;
        }

        public void addHrClassToGroup(final String hrClass) {
            if (_hrClassGrpList.size() == 0) {
                new HrClassGrp(this);
            }
            //リスト末尾のHrClassGrpを取得
            HrClassGrp hrClassGrp = (HrClassGrp)_hrClassGrpList.get(_hrClassGrpList.size() - 1);

            //既にリストにあるクラスなら追加しない
            if (hrClassGrp._hrKeyList.contains(hrClass)) {
                return;
            }

            if (hrClassGrp._hrKeyList.size() >= MAXLETU) {
                //リスト末尾のHrClassGrpが保持できる最大数を超えていた場合、次のHrClassGrpを作成し、そちらにクラスを追加する
                HrClassGrp newHrClassGrp = new HrClassGrp(this);
                newHrClassGrp._hrKeyList.add(hrClass);
            } else {
                hrClassGrp._hrKeyList.add(hrClass);
            }
        }
    }

    private class HrClassGrp {
        private final Lmst _lmst;
        private final List _hrKeyList;

        public HrClassGrp(final Lmst lmst) {
            _lmst = lmst;
            _lmst._hrClassGrpList.add(this);
            _hrKeyList = new ArrayList();
        }

        public boolean hasHrClassRemainingSch() {
            boolean flg = false;
            for (Iterator iter = _hrKeyList.iterator(); iter.hasNext();) {
                final String hrKey = (String)iter.next();
                final HrClass hrClass = (HrClass)_lmst._hrClassMap.get(hrKey);
                if (hrClass.hasNextSch()) {
                    flg = true;
                }
            }
            return flg;
        }
    }

    private class HrClass {
        private final String _grade;
        private final String _hrClass;
        private final String _hrName;
        private final List _schList;
        private Iterator _hrIterator; //イテレータを保持しておくことで次ページで行数超過分の生徒から表示を開始できる

        public HrClass(
                final String grade,
                final String hrClass,
                final String hrName
        ) {
            _grade   = grade;
            _hrClass = hrClass;
            _hrName  = hrName;
            _schList = new ArrayList();
        }

        public Iterator getSchListIterator() {
            if (_hrIterator == null) {
                _hrIterator = _schList.iterator();
            }
            return _hrIterator;
        }

        public boolean hasNextSch() {
            return getSchListIterator().hasNext();
        }
    }

    private class SchregDat {
        private final String _levyLCd;
        private final String _levyLName;
        private final String _schregno;
        private final String _name;
        private final String _grade;
        private final String _hrClass;
        private final String _hrName;
        private final String _attendno;
        private final String _incomeMoney;

        public SchregDat(
                final String levyLCd,
                final String levyLName,
                final String schregno,
                final String name,
                final String grade,
                final String hrClass,
                final String hrName,
                final String attendno,
                final String incomeMoney
        ) {
            _levyLCd        = levyLCd;
            _levyLName      = levyLName;
            _schregno       = schregno;
            _name           = name;
            _grade          = grade;
            _hrClass        = hrClass;
            _hrName         = hrName;
            _attendno       = attendno;
            _incomeMoney    = incomeMoney;
        }

    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 62411 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _grade;
        private final String _printDiv;
        private final String _requestNo;
        private final String _incomeDiv;
        private final String _reqYearMonthF;
        private final String _reqYearMonthT;
        private final String _inYearMonthF;
        private final String _inYearMonthT;
        private final String _prgid;
        private final String _year;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _schoolName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _grade = request.getParameter("GRADE");
            _printDiv = request.getParameter("PRINT_DIV");
            _requestNo = request.getParameter("REQUEST_NO");
            _incomeDiv = request.getParameter("INCOME_DIV");
            _reqYearMonthF = request.getParameter("REQUEST_MONTH_F");
            _reqYearMonthT = request.getParameter("REQUEST_MONTH_T");
            _inYearMonthF = request.getParameter("INCOME_MONTH_F");
            _inYearMonthT = request.getParameter("INCOME_MONTH_T");
            _prgid = request.getParameter("PRGID");
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _schoolName = getSchoolName(db2, _year);
        }

        private String getSchoolName(final DB2UDB db2, final String year) {
            String retSchoolName = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + year + "' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    retSchoolName = rs.getString("SCHOOLNAME1");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retSchoolName;
        }

    }
}

// eof

