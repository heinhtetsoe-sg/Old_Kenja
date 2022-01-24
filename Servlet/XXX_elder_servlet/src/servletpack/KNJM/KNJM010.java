// kanji=漢字
/*
 * $Id: 77940c4ebd18932486f0a15522d512ccaea915ff $
 *
 * 作成日: 2009/02/13 14:42:08 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2014 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 *
 *  学校教育システム 賢者 [通信制]
 *
 *                  ＜ＫＮＪＭ０１０＞  学籍番号バーコードラベル(通信制)
 *
 *  2005/03/10 m-yama 作成日
 **/

public class KNJM010 {

    private static final Log log = LogFactory.getLog(KNJM010.class);
    private boolean _hasData;
    private Param _param;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {
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

            //  ＳＶＦ作成処理
            PreparedStatement ps  = null;

            //SQL作成
            try {
                ps  = db2.prepareStatement(preStat());
            } catch( Exception ex ) {
                log.error("DB2 prepareStatement set error!");
            }
            //SVF出力
            if (setSvfMain(db2, svf, ps)) _hasData = true;  //帳票出力のメソッド
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

    }//doGetの括り

    /**
     *  svf print 印刷処理
     */
    private boolean setSvfMain(
        DB2UDB db2,
        Vrw32alp svf,
        PreparedStatement ps
    ) {
        boolean nonedata = false;
        try {
            ResultSet rs = ps.executeQuery();
            if ("KOKUYO-LBP-F7656".equals(_param._printType)) {
                svf.VrSetForm("KNJM010_2.frm", 1);
                while (rs.next()) {
                    for (int bu = 0; bu < Integer.parseInt(_param._busu); bu++) {
                        final int maxLineCnt = 20;
                        final int maxGyoCnt = 4;
                        for (int lineCnt = 1; lineCnt <= maxLineCnt; lineCnt++) {
                            for (int gyoCnt = 1; gyoCnt <= maxGyoCnt; gyoCnt++) {
                                //ヘッダ
                                svf.VrsOut("CLASS_NAME1_1", rs.getString("HR_NAME"));
                                svf.VrsOut("SCHREG_NO1_1", rs.getString("SCHREGNO"));
                                svf.VrsOut("CLASS_NAME1_2", rs.getString("HR_NAME"));
                                svf.VrsOut("SCHREG_NO1_2", rs.getString("SCHREGNO"));
                                final String name = rs.getString("NAME");
                                final String nameField = getMS932Bytecount(name) > 30 ? "2" : "1";
                                svf.VrsOut("NAME" + nameField, name);

                                svf.VrsOutn("CLASS_NAME2_" + gyoCnt, lineCnt, rs.getString("HR_NAME"));
                                svf.VrsOutn("SCHREG_NO2_" + gyoCnt, lineCnt, rs.getString("SCHREGNO"));
                                svf.VrsOutn("BARCODE" + gyoCnt, lineCnt, rs.getString("SCHREGNO"));
                            }
                        }
                        svf.VrEndPage();
                    }
                    nonedata = true;
                }
            } else if("KNJM010_4".equals(_param._printType)) {
                final int maxCol = 5;
                final int maxLine = 13;
                svf.VrSetForm("KNJM010_4.frm", 1);

                while (rs.next()) {
                	for (int bu = 0;bu < Integer.parseInt(_param._busu);bu++) {
                		svf.VrsOut("NENDO", _param._year + "年度");
                        svf.VrsOut("DATE", _param._printDate);
                        svf.VrsOut("CLASS", rs.getString("HR_NAME"));
                        final String name = rs.getString("NAME");

                        for (int line = 0; line < maxLine; line++) {
                        	final int lineCnt = line + 1;
                        	for (int col = 0; col < maxCol; col++) {
                        		final String colCnt = String.valueOf(col + 1);
                        		 svf.VrsOutn("SCHNAME3_" + colCnt + (KNJ_EditEdit.getMS932ByteLength(name) > 20 ? "_2" : ""), lineCnt, name);
                        		 svf.VrsOutn("BARCODE_" + colCnt, lineCnt,  rs.getString("SCHREGNO"));
                        		 final String bangou = seikei(rs.getString("ATTENDNO"));
                                 final String syozoku = rs.getString("HR_NAME") + bangou + "番";
                                 svf.VrsOutn("HR_NAME" + colCnt, lineCnt, syozoku);
                        	}
                        }
                        svf.VrEndPage();
                    }
                	nonedata = true;
                }
            }else {
                svf.VrSetForm("KNJM010.frm", 4);
                while (rs.next()) {
                    for (int bu = 0;bu < Integer.parseInt(_param._busu);bu++) {
                        final int recCnt = 48;
                        for (int reccnt = 0; reccnt <= recCnt; reccnt++) {
                            //ヘッダ
                            svf.VrsOut("NENDO", _param._year + "年度");
                            svf.VrsOut("DATE", _param._printDate);
                            svf.VrsOut("CLASS", rs.getString("HR_NAME"));
                            svf.VrsOut("GAKU1", rs.getString("SCHREGNO"));
                            final String name = rs.getString("NAME");
                            svf.VrsOut(KNJ_EditEdit.setformatArea("SCHNAME1", name, 17, "", "_2"), name);
                            //明細
                            svf.VrsOut(KNJ_EditEdit.setformatArea("SCHNAME2", name, 10, "", "_2"), name);
                            svf.VrsOut("BARCODE", rs.getString("SCHREGNO"));
                            if (reccnt > 0) svf.VrEndRecord();
                        }
                    }
                    nonedata = true;
                }
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.error("setSvfMain set error!");
        }
        return nonedata;
    }

    private static int getMS932Bytecount(String str) {
        int count = 0;
        if (null != str) {
            try {
                count = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error(e);
            }
        }
        return count;
    }

    private <T> List<List<T>> getSplitList(final List<T> list, final int max) {
        final List<List<T>> splitList = new ArrayList();
        List<T> current = null;
        for (final T o : list) {
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                splitList.add(current);
            }
            current.add(o);
        }
        return splitList;
    }

    private String seikei(final String str) {
    	Pattern p = Pattern.compile("^0+([0-9]+.*)");
        Matcher m = p.matcher(str);

        String printstr= null;
        if (m.matches()) {
        	printstr = m.group(1);
        }

    	return printstr;

    }

    /**データ　取得**/
    private String preStat()
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("with atable(SCHREGNO,HR_CLASS,ATTENDNO,HR_NAME) as (SELECT ");
            stb.append("    w1.SCHREGNO, ");
            stb.append("    w1.HR_CLASS, ");
            stb.append("    w1.ATTENDNO, ");
            stb.append("    w2.HR_NAME ");
            stb.append("FROM ");
            stb.append("    SCHREG_REGD_DAT w1 LEFT JOIN SCHREG_REGD_HDAT w2 on w1.GRADE||w1.HR_CLASS = w2.GRADE||w2.HR_CLASS AND w2.year = '" + _param._year + "' AND w2.semester = '" + _param._semester + "' ");
            stb.append("WHERE ");
            stb.append("    w1.YEAR = '" + _param._year + "' AND ");
            stb.append("    w1.SEMESTER = '" + _param._semester + "' AND ");
            if("1".equals(_param._choice)) {
            	stb.append("    w1.GRADE||w1.HR_CLASS in " + _param._hrClassInState );
            }else {
            	stb.append("    w1.GRADE || w1.HR_CLASS = '" + _param._hrClass + "' ");
            }
            stb.append(") ");
            stb.append("select w1.SCHREGNO,w1.NAME,w2.HR_CLASS,w2.HR_NAME,w2.ATTENDNO ");
            stb.append("from schreg_base_mst w1,atable w2 ");
            stb.append("where w1.SCHREGNO = w2.SCHREGNO ");
            if("2".equals(_param._choice)) {
            	stb.append("AND w1.SCHREGNO in " + _param._hrClassInState );
            }
            stb.append("order by w2.HR_CLASS,w2.ATTENDNO ");
log.debug(stb);
        } catch( Exception ex ){
            log.error("preStat error!");
        }
        return stb.toString();

    }//preStat()の括り

    /**PrepareStatement close**/
    private void preStatClose(
        PreparedStatement ps
    ) {
        try {
            ps.close();
        } catch( Exception ex ){
            log.error("preStatClose error!");
        }
    }//preStatClose()の括り

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) {
        log.fatal("$Revision: 74936 $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return new Param(db2, request);
    }

    private static class Param {

        final String _year;
        final String _semester;
        final String _hrClass;
        final String _choice; //1:クラス指定　2:個人指定
        final String _hrClassInState;
        final String _busu;
        final String _printDate;
        final String _printType;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("GAKKI");
            _choice = request.getParameter("CHOICE");
            _hrClass = "2".equals(_choice) ? request.getParameter("GRADE_HR_CLASS") : "";
            String hrclass[] = request.getParameterValues("CATEGORY_SELECTED");     //学年・組
            String setHrClassInState = "( ";
            for (int ia = 0; ia < hrclass.length; ia++) {
                if (ia != 0) setHrClassInState = setHrClassInState + ", ";
                setHrClassInState = setHrClassInState + "'";
                setHrClassInState = setHrClassInState + hrclass[ia];
                setHrClassInState = setHrClassInState + "'";
            }
            setHrClassInState = setHrClassInState + " )";
            _hrClassInState = setHrClassInState;
            _busu = request.getParameter("BUSU");    //部数
            _printType = request.getParameter("formTypeM010");
            _printDate = getPrintDate(db2);
        }
        private String getPrintDate(final DB2UDB db2) {
            String retStr = "";
            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;

            //作成日(現在処理日)の取得
            returnval = getinfo.Control(db2);
            retStr = KNJ_EditDate.h_format_thi(returnval.val3,0);
            return retStr;
        }
    }


}//クラスの括り
