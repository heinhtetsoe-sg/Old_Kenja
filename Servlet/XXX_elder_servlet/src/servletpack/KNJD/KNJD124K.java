/**
 *
 *  学校教育システム 賢者 [成績管理]
 *
 *                  ＜ＫＮＪＤ１２４Ｋ＞  観点別成績・評価チェックリスト（近大・大宮版）
 *
 *  2005/12/27 nakamoto 作成日
 *  2006/04/07 nakamoto 千代田区版(KNJD124J.class)をコピー。評定欄をカット
 */

package servletpack.KNJD;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
//import jp.co.fit.vfreport.Vrw32;
import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
//import nao_package.db.Database;
import nao_package.svf.Vrw32alp;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJD124K {

            private static final Log log = LogFactory.getLog(KNJD124K.class);

            private String _useCurriculumcd;
            private String _kanten2keta;

            public void svf_out(HttpServletRequest httpservletrequest, HttpServletResponse httpservletresponse) throws ServletException, IOException {

                log.fatal("$Revision: 66527 $ $Date: 2019-03-26 10:09:38 +0900 (火, 26 3 2019) $"); // CVSキーワードの取り扱いに注意
                KNJServletUtils.debugParam(httpservletrequest, log);

/*  45*/        Vrw32alp vrw32alp = new Vrw32alp();
/*  46*/        DB2UDB db2udb = null;
/*  47*/        String as[] = new String[12];
/*  48*/        HashMap hashmap = new HashMap();
/*  52*/        try {
/*  52*/            as[0] = httpservletrequest.getParameter("CTRL_Y");//今年度
/*  53*/            as[1] = httpservletrequest.getParameter("CTRL_S");//学期 観点データ以外用 1:前期,2:後期
/*  54*/            as[2] = httpservletrequest.getParameter("CTRL_D");//生徒を抽出する日付 学籍処理日または学期終了日
/*  56*/            as[3] = httpservletrequest.getParameter("SEMESTER");//学期 観点データ用 1:前期,9:学年
/*  57*/            as[4] = httpservletrequest.getParameter("CLASSCD");//教科
/*  58*/            as[5] = httpservletrequest.getParameter("CHAIRCD");//講座
                    _useCurriculumcd = httpservletrequest.getParameter("useCurriculumcd"); // プロパティ(教育課程コード)(1:教育課程対応)
                    _kanten2keta     = "1".equals(_useCurriculumcd) ? StringUtils.split(as[4], "-")[0] : as[4]; // 観点の頭2桁
                }
/*  58*/        catch(Exception exception) {
/*  60*/            log.warn("parameter error!", exception);
                }
/*  64*/        PrintWriter printwriter = new PrintWriter(httpservletresponse.getOutputStream());
/*  65*/        httpservletresponse.setContentType("application/pdf");
/*  68*/        vrw32alp.VrInit();
/*  69*/        vrw32alp.VrSetSpoolFileStream(httpservletresponse.getOutputStream());
/*  73*/        try {
/*  73*/            db2udb = new DB2UDB(httpservletrequest.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
/*  74*/            db2udb.open();
                }
/*  74*/        catch(Exception exception1) {
/*  76*/            log.error("DB2 open error!", exception1);
/*  77*/            return;
                }
/*  82*/        boolean flag = false;
/*  84*/        getHeaderData(db2udb, vrw32alp, as);
/*  86*/        for(int j = 0; j < as.length; j++)
/*  86*/            log.debug("param[" + j + "]=" + as[j]);

/*  90*/        if(printMain(db2udb, vrw32alp, as, hashmap))
/*  90*/            flag = true;
/*  92*/        log.debug("nonedata=" + flag);
/*  95*/        if(!flag) {
/*  96*/            vrw32alp.VrSetForm("MES001.frm", 0);
/*  97*/            vrw32alp.VrsOut("note", "");
/*  98*/            vrw32alp.VrEndPage();
                }
/* 102*/        vrw32alp.VrQuit();
/* 103*/        db2udb.commit();
/* 104*/        db2udb.close();
/* 105*/        printwriter.close();
            }

            private void getHeaderData(DB2UDB db2udb, Vrw32alp vrw32alp, String as[]) {
/* 115*/        vrw32alp.VrSetForm("KNJD124K.frm", 1);
/* 119*/        try {
/* 119*/            as[7] = KenjaProperties.gengou(Integer.parseInt(as[0])) + "\u5E74\u5EA6";//平成XX年度
                }
/* 119*/        catch(Exception exception) {
/* 121*/            log.warn("nendo get error!", exception);
                }
/* 126*/        try {
/* 126*/            String s = "VALUES RTRIM(CHAR(DATE(SYSDATE()))),RTRIM(CHAR(HOUR(SYSDATE()))),RTRIM(CHAR(MINUTE(SYSDATE())))";
/* 127*/            db2udb.query(s);
/* 128*/            ResultSet resultset3 = db2udb.getResultSet();
/* 129*/            String as1[] = new String[3];
/* 130*/            for(int j = 0; resultset3.next(); j++)
/* 132*/                as1[j] = resultset3.getString(1);

/* 135*/            db2udb.commit();
/* 136*/            as[8] = KNJ_EditDate.h_format_JP(db2udb, as1[0]) + as1[1] + "\u6642" + as1[2] + "\u5206" + "\u3000\u73FE\u5728";//平成XX年XX月XX日XX時XX分　現在
                }
/* 136*/        catch(Exception exception1) {
/* 138*/            log.warn("ctrl_date get error!", exception1);
                }
/* 143*/        try {
/* 143*/            db2udb.query("SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z009' AND NAMECD2 = '" + as[3] + "'");
/* 144*/            for(ResultSet resultset = db2udb.getResultSet(); resultset.next();)
/* 146*/                as[9] = resultset.getString("NAME1");//管理者コントロール 観点名称 1:前期,9:学年

/* 148*/            db2udb.commit();
                }
/* 148*/        catch(Exception exception2) {
/* 150*/            log.warn("name1 get error!", exception2);
                }
/* 155*/        try {
                    int count = 0;
                    final String sqlClassMst;
                    if ("1".equals(_useCurriculumcd)) {
                        sqlClassMst = "SELECT COUNT(*) AS COUNT FROM CLASS_MST WHERE CLASSCD || '-' || SCHOOL_KIND = '" + as[4] + "'";
                    } else {
                        sqlClassMst = "SELECT COUNT(*) AS COUNT FROM CLASS_MST WHERE CLASSCD = '" + as[4] + "'";
                    }
                    db2udb.query(sqlClassMst);
                    for(ResultSet resultset1 = db2udb.getResultSet(); resultset1.next();) {
                        count = resultset1.getInt("COUNT");
                    }
                    String sql = "";
                    if (count > 1) {
                        sql =  " SELECT CLASSNAME FROM CLASS_MST WHERE CLASSCD || '-' || SCHOOL_KIND = ";
                        sql += "   (SELECT MIN(CLASSCD || '-' || SCHOOL_KIND) FROM CLASS_MST ";
                        sql += "     WHERE LENGTH(CLASSNAME) = (SELECT MIN(LENGTH(CLASSNAME))  FROM CLASS_MST WHERE ";
                        if ("1".equals(_useCurriculumcd)) {
                            sql += "     CLASSCD || '-' || SCHOOL_KIND = '" + as[4] + "')) ";
                        } else {
                            sql += "     CLASSCD = '" + as[4] + "')) ";
                        }
                    } else {
                        sql = "SELECT CLASSNAME AS CLASSNAME FROM CLASS_MST WHERE ";
                        if ("1".equals(_useCurriculumcd)) {
                            sql += " CLASSCD || '-' || SCHOOL_KIND = '" + as[4] + "' ";
                        } else {
                            sql += " CLASSCD = '" + as[4] + "' ";
                        }
                    }
                    log.debug(" sql = " + sql);
                    db2udb.query(sql);
                    for(ResultSet resultset1 = db2udb.getResultSet(); resultset1.next();)
                        as[10] = resultset1.getString("CLASSNAME");//教科名称


/* 160*/            db2udb.commit();
                }
/* 160*/        catch(Exception exception3) {
/* 162*/            log.warn("classname get error!", exception3);
                }
/* 167*/        try {
/* 167*/            db2udb.query("SELECT CHAIRNAME FROM CHAIR_DAT WHERE YEAR = '" + as[0] + "' AND SEMESTER = '" + as[1] + "' AND CHAIRCD = '" + as[5] + "'");
/* 168*/            for(ResultSet resultset2 = db2udb.getResultSet(); resultset2.next();)
/* 170*/                as[11] = resultset2.getString("CHAIRNAME");//講座名称

/* 172*/            db2udb.commit();
                }
/* 172*/        catch(Exception exception4) {
/* 174*/            log.warn("chairname get error!", exception4);
                }
            }

            private boolean printMain(DB2UDB db2udb, Vrw32alp vrw32alp, String as[], Map map) {
/* 183*/        boolean flag = false;
/* 188*/        try {
/* 188*/            getJviewname(db2udb, vrw32alp, as, map);
/* 191*/            if(printMeisai(db2udb, vrw32alp, as, map))
/* 191*/                flag = true;
                }
/* 191*/        catch(Exception exception) {
/* 196*/            log.warn("printMain read error!", exception);
                }
/* 199*/        return flag;
            }

            private void getJviewname(DB2UDB db2udb, Vrw32alp vrw32alp, String as[], Map map) {
/* 209*/        try {
/* 209*/            log.debug("Jviewname start!");
/* 210*/            db2udb.query(statementJviewname(as));
/* 211*/            ResultSet resultset = db2udb.getResultSet();
/* 212*/            log.debug("Jviewname end!");
/* 214*/            int i = 0;
/* 215*/            String s = "";
/* 218*/            for(; resultset.next(); map.put(resultset.getString("VIEWCD"), String.valueOf(++i))) {
/* 218*/                if(i == 5)
/* 218*/                    break;
/* 219*/                if(i == 0)
/* 219*/                    as[6] = "";
/* 220*/                as[6] = as[6] + s + resultset.getString("VIEWCD");//観点コード 観点状況
/* 221*/                s = "','";
                    }

/* 225*/            if(0 < i) {
/* 226*/                as[6] = "('" + as[6] + s + _kanten2keta + "99" + "')";//観点コード 評定
/* 227*/                map.put(_kanten2keta + "99", "9");
                    }
/* 229*/            log.debug("param[6]=" + as[6]);
/* 230*/            resultset.close();
/* 231*/            db2udb.commit();
                }
/* 231*/        catch(Exception exception) {
/* 233*/            log.warn("getJviewname read error!", exception);
                }
            }

            private boolean printMeisai(DB2UDB db2udb, Vrw32alp vrw32alp, String as[], Map map) {
/* 242*/        boolean flag = false;
/* 245*/        try {
/* 245*/            log.debug("Meisai start!");
/* 246*/            PreparedStatement preparedstatement = db2udb.prepareStatement(statementMeisai(as));
/* 247*/            ResultSet resultset = preparedstatement.executeQuery();
/* 248*/            log.debug("Meisai end!");
/* 250*/            int k = 0;
/* 251*/            String s = "0";
/* 254*/            while(resultset.next())  {
/* 254*/                if(!s.equals("0") && !s.equals(resultset.getString("SCHREGNO")))
/* 254*/                    k++;
/* 256*/                if(49 < k) {
/* 258*/                    printHeader(vrw32alp, as);
/* 259*/                    if(as[6] != null)
/* 259*/                        getJviewname2(db2udb, vrw32alp, as);
/* 260*/                    vrw32alp.VrEndPage();
/* 261*/                    k = 0;
                        }
/* 264*/                printExam(vrw32alp, as, resultset, k, map);
/* 266*/                s = resultset.getString("SCHREGNO");
/* 267*/                flag = true;
                    }
/* 271*/            if(flag) {
/* 273*/                printHeader(vrw32alp, as);
/* 274*/                if(as[6] != null)
/* 274*/                    getJviewname2(db2udb, vrw32alp, as);
/* 275*/                vrw32alp.VrEndPage();
                    }
/* 277*/            resultset.close();
/* 278*/            preparedstatement.close();
/* 279*/            db2udb.commit();
                }
/* 279*/        catch(Exception exception) {
/* 281*/            log.warn("printMeisai read error!", exception);
                }
/* 283*/        return flag;
            }

            private void printHeader(Vrw32alp vrw32alp, String as[]) {
/* 293*/        try {
/* 293*/            vrw32alp.VrsOut("NENDO", as[7]);//年度
/* 294*/            vrw32alp.VrsOut("DATE", as[8]);//作成日時
/* 295*/            vrw32alp.VrsOut("SEMESTER", as[9]);//学期
/* 296*/            vrw32alp.VrsOut("CLASS", as[10]);//教科
/* 297*/            vrw32alp.VrsOut("CHAIRNAME", as[11]);//講座
                }
/* 297*/        catch(Exception exception) {
/* 299*/            log.warn("printHeader read error!", exception);
                }
            }

            private void printExam(Vrw32alp vrw32alp, String as[], ResultSet resultset, int i, Map map) {
/* 311*/        try {
/* 311*/            vrw32alp.VrsOutn("HR_NAME", i + 1, resultset.getString("HR_NAME") + "-" + resultset.getString("ATTENDNO"));//クラス
/* 312*/            vrw32alp.VrsOutn("NAME", i + 1, resultset.getString("SCHREGNO") + "\u3000" + resultset.getString("NAME_SHOW"));//氏名
/* 314*/            if(resultset.getString("VIEWCD") != null && as[6] != null)
/* 315*/                vrw32alp.VrsOutn("STATUS" + (String)map.get(resultset.getString("VIEWCD")), i + 1, resultset.getString("STATUS"));//観点状況および評定
                }
/* 315*/        catch(Exception exception) {
/* 318*/            log.warn("printExam read error!", exception);
                }
            }

            private void getJviewname2(DB2UDB db2udb, Vrw32alp vrw32alp, String as[]) {
/* 329*/        try {
/* 329*/            log.debug("Jviewname2 start!");
/* 330*/            PreparedStatement preparedstatement = db2udb.prepareStatement(statementJviewname(as));
/* 331*/            ResultSet resultset = preparedstatement.executeQuery();
/* 332*/            log.debug("Jviewname2 end!");
/* 334*/            int i1 = 0;
/* 335*/            int j1 = 1;
/* 336*/            String as1[] = {
/* 336*/                "1", "2", "3", "4", "5"
                    };
/* 339*/            while(resultset.next())  {
/* 339*/                if(i1 == 5)
/* 339*/                    break;
/* 342*/                vrw32alp.VrsOut("VIEWCD1_" + String.valueOf(i1 + 1), as1[i1]);
/* 345*/                if(20 < resultset.getString("VIEWNAME").length()) {
/* 347*/                    vrw32alp.VrsOutn("VIEWCD2", i1 + j1, as1[i1]);
/* 348*/                    vrw32alp.VrsOutn("VIEWNAME", i1 + j1, resultset.getString("VIEWNAME").substring(0, 10));
/* 349*/                    j1++;
/* 350*/                    vrw32alp.VrsOutn("VIEWCD2", i1 + j1, "");
/* 351*/                    vrw32alp.VrsOutn("VIEWNAME", i1 + j1, resultset.getString("VIEWNAME").substring(10, 20));
/* 352*/                    j1++;
/* 353*/                    vrw32alp.VrsOutn("VIEWCD2", i1 + j1, "");
/* 354*/                    vrw32alp.VrsOutn("VIEWNAME", i1 + j1, resultset.getString("VIEWNAME").substring(20));
                        } else
/* 356*/                if(10 < resultset.getString("VIEWNAME").length()) {
/* 358*/                    vrw32alp.VrsOutn("VIEWCD2", i1 + j1, as1[i1]);
/* 359*/                    vrw32alp.VrsOutn("VIEWNAME", i1 + j1, resultset.getString("VIEWNAME").substring(0, 10));
/* 360*/                    j1++;
/* 361*/                    vrw32alp.VrsOutn("VIEWCD2", i1 + j1, "");
/* 362*/                    vrw32alp.VrsOutn("VIEWNAME", i1 + j1, resultset.getString("VIEWNAME").substring(10));
                        } else {
/* 366*/                    vrw32alp.VrsOutn("VIEWCD2", i1 + j1, as1[i1]);//観点コード
/* 367*/                    vrw32alp.VrsOutn("VIEWNAME", i1 + j1, resultset.getString("VIEWNAME"));//観点名称
                        }
/* 371*/                i1++;
                    }
/* 374*/            resultset.close();
/* 375*/            preparedstatement.close();
                }
/* 375*/        catch(Exception exception) {
/* 378*/            log.warn("getJviewname2 read error!", exception);
                }
            }

            private String statementMeisai(String as[]) {
/* 390*/        StringBuffer stringbuffer = new StringBuffer();
/* 392*/        try {
/* 392*/            stringbuffer.append("WITH SCHNO AS ( ");
/* 393*/            stringbuffer.append("SELECT ");
/* 394*/            stringbuffer.append("    t1.hr_name, t2.grade, t2.hr_class, t2.attendno, t4.schregno, t3.name_show ");
/* 395*/            stringbuffer.append("FROM ");
/* 396*/            stringbuffer.append("    schreg_regd_hdat t1, ");
/* 397*/            stringbuffer.append("    schreg_regd_dat t2, ");
/* 398*/            stringbuffer.append("    schreg_base_mst t3, ");
/* 399*/            stringbuffer.append("    chair_std_dat t4 ");
/* 400*/            stringbuffer.append("WHERE ");
/* 401*/            stringbuffer.append("    t1.year      = t2.year AND ");
/* 402*/            stringbuffer.append("    t1.semester  = t2.semester AND ");
/* 403*/            stringbuffer.append("    t1.grade     = t2.grade AND ");
/* 404*/            stringbuffer.append("    t1.hr_class  = t2.hr_class AND ");
/* 405*/            stringbuffer.append("    t2.year      = '" + as[0] + "' AND ");
/* 406*/            stringbuffer.append("    t2.semester  = '" + as[1] + "' AND ");
/* 407*/            stringbuffer.append("    t2.schregno  = t3.schregno AND ");
/* 408*/            stringbuffer.append("    t4.year      = t2.year AND ");
/* 409*/            stringbuffer.append("    t4.chaircd   = '" + as[5] + "' AND ");
/* 410*/            stringbuffer.append("    t4.schregno  = t2.schregno AND ");
/* 411*/            stringbuffer.append("    '" + as[2] + "' BETWEEN t4.appdate AND t4.appenddate ");
/* 412*/            stringbuffer.append(" ) ");
/* 414*/            stringbuffer.append(",JVIEWSTAT AS ( ");
/* 415*/            stringbuffer.append("SELECT ");
/* 416*/            stringbuffer.append("    schregno, viewcd, status ");
/* 417*/            stringbuffer.append("FROM ");
/* 418*/            stringbuffer.append("    jviewstat_dat ");
/* 419*/            stringbuffer.append("WHERE ");
/* 420*/            stringbuffer.append("    year = '" + as[0] + "' AND ");
/* 421*/            stringbuffer.append("    semester = '" + as[3] + "' AND ");
/* 422*/            if(as[6] != null)
/* 423*/                stringbuffer.append("    viewcd in " + as[6] + " AND ");
/* 425*/            else
/* 425*/                stringbuffer.append("    SUBSTR(VIEWCD,1,2) = '" + _kanten2keta + "' AND ");
/* 426*/            stringbuffer.append("    status is not null ");
/* 427*/            stringbuffer.append(" ) ");
/* 429*/            stringbuffer.append("SELECT  TBL1.hr_name,TBL1.grade,TBL1.hr_class,TBL1.attendno,TBL1.schregno,TBL1.name_show, ");
/* 430*/            stringbuffer.append("        TBL2.viewcd, TBL2.status ");
/* 431*/            stringbuffer.append("FROM    SCHNO TBL1 ");
/* 432*/            stringbuffer.append("        LEFT JOIN JVIEWSTAT TBL2 ON TBL2.SCHREGNO = TBL1.SCHREGNO ");
/* 433*/            stringbuffer.append("ORDER BY TBL1.grade, TBL1.hr_class, TBL1.attendno, TBL2.viewcd ");
                }
/* 433*/        catch(Exception exception) {
/* 435*/            log.warn("statementMeisai error!", exception);
                }
/* 437*/        return stringbuffer.toString();
            }

            private String statementJviewname(String as[]) {
/* 448*/        StringBuffer stringbuffer = new StringBuffer();
/* 450*/        try {
/* 450*/            stringbuffer.append("SELECT T1.VIEWCD, T2.VIEWNAME ");
/* 451*/            stringbuffer.append("FROM   JVIEWNAME_YDAT T1, JVIEWNAME_MST T2 ");
/* 452*/            stringbuffer.append("WHERE  T1.YEAR = '" + as[0] + "' AND ");
/* 453*/            stringbuffer.append("       T1.VIEWCD = T2.VIEWCD AND ");
/* 454*/            stringbuffer.append("       SUBSTR(T1.VIEWCD,1,2) = '" + _kanten2keta + "' ");
/* 455*/            stringbuffer.append("ORDER BY T1.VIEWCD ");
                }
/* 455*/        catch(Exception exception) {
/* 457*/            log.warn("statementJviewname error!", exception);
                }
/* 459*/        return stringbuffer.toString();
            }
}
