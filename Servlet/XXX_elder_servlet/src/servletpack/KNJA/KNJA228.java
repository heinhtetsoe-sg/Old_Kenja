/*
 * $Id: f45eb9d2ceb501c0c1c5bd524d8f535463013cda $
 *
 * 作成日: 2017/05/01
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;


import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJA228 {

    private static final Log log = LogFactory.getLog(KNJA228.class);
    private static final int PRINTMAXLINE = 52;

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

    private int roundup(final int val, final int div_base) {
    	BigDecimal bd1 = new BigDecimal(val);
    	BigDecimal bd2 = new BigDecimal(div_base);
    	return bd1.divide(bd2, 0, BigDecimal.ROUND_UP).intValue();
    }

    private int pageCount(final PrintInfo printwk) {
        int pagecnt = 1;
        int subpcnt = 0;
        int zaikorecperpage = PRINTMAXLINE;
        int gradrecperpage = PRINTMAXLINE;
        int otherrecperpage = PRINTMAXLINE;
        //ヘッダ部5行+余白+受験情報(タイトル、ヘッダ+データ数)を出力した残り
        int leastreccnt = PRINTMAXLINE - (5 + 1 + 2 + roundup((printwk._ei.size() > 0 ? printwk._ei.size() : 1), 2));
        //余白を加味して、残りを確認。
        leastreccnt -= 1;
        //出力する余白があるかないかで処理分け(余白があることが前提として処理を作成)
        //ヘッダ分を加味した上で計算。(タイトル+ヘッダ+データ数)。ただし、改ページごとにタイトルを付加する事、1レコード3行消費に注意。
        final int sreccnt = printwk._s.size() > 0 ? printwk._s.size() : 1;
        if (sreccnt * 3 - (leastreccnt - 2) > 0) {
        	//余白出力を除いてカウントする(余白分はカウント済みとなる)
            int onepagereccnt = ((zaikorecperpage - 2) / 3);
            onepagereccnt = onepagereccnt * 3;
            subpcnt = roundup((sreccnt * 3 - (leastreccnt - 2)), onepagereccnt);
            //ページに入る件数を割り出すため、1レコード利用件数で割って端数切捨てして1レコード利用件数かける。

            leastreccnt = (zaikorecperpage - 2) - ((sreccnt * 3 - (leastreccnt - 2)) % onepagereccnt);
        } else {
        	subpcnt = 0;
      	    leastreccnt = leastreccnt - 2 - (sreccnt * 3);
      	}
        if (leastreccnt > 1) {//余白分を加味する
        	leastreccnt--;
        }
        pagecnt += subpcnt;

        //余白+ヘッダ+データ1行分の出力で残りが無いなら次ページ
        final int gsreccnt = printwk._gs.size() > 0 ? printwk._gs.size() : 1;
        if (leastreccnt > 3) {
        	//ヘッダ分を出力した上で計算。
        	if (gsreccnt - (leastreccnt - 2) > 0) {
        	    subpcnt = roundup((gsreccnt - (leastreccnt - 2)  ), (gradrecperpage - 2));
            	leastreccnt = (gradrecperpage - 2) - ((gsreccnt - (leastreccnt - 2)) % (gradrecperpage - 2));
        	} else {
        		subpcnt = 0;
        	    leastreccnt = (leastreccnt - 2) - gsreccnt;
        	}
        } else {
        	subpcnt = roundup(gsreccnt, (gradrecperpage - 2));
        	leastreccnt = (gradrecperpage - 2) - (gsreccnt % (gradrecperpage - 2));
        }
        if (leastreccnt > 1) {//余白分を加味する
        	leastreccnt--;
        }
        pagecnt += subpcnt;

        //余白+ヘッダ+データ1行分の出力で残りが無いなら次ページ
        Map cntchkobj = null;
        if ("1".equals(_param._disppattern)) {
        	cntchkobj = printwk._os;
            final int osreccnt = printwk._os.size() > 0 ? printwk._os.size() : 1;
            if (leastreccnt > 3) {
            	//ヘッダ分を出力した上で計算。
            	if (osreccnt - (leastreccnt - 2) > 0) {
            	    subpcnt = roundup((osreccnt - (leastreccnt - 2)), (otherrecperpage - 2));
                	leastreccnt = (otherrecperpage - 2) - ((osreccnt - (leastreccnt - 2)) % (otherrecperpage - 2));
            	} else {
            		subpcnt = 0;
            	    leastreccnt = leastreccnt - 2 - osreccnt;
            	}
            } else {
            	subpcnt = roundup(osreccnt, (otherrecperpage - 2));
            	leastreccnt = (otherrecperpage - 2) - (osreccnt % (otherrecperpage - 2));
            }
        } else {
        	cntchkobj = printwk._ji;
        	leastreccnt--; //データが無いので、1行だけ空行を出力する。
        }
        //最後は余白のチェックは不要。
        pagecnt += subpcnt;
        return pagecnt;
    }

    private class PrintCntCtl {
    	int _prtinpagecnt;
    	int _pagenum;
    	int _pagecnt;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        _param._formname = "KNJA228.frm";

        getInfo(db2);

        for (int ii = 0;ii < _param._prtinflist.size();ii++) {
        	_hasData = true;
        	PrintInfo printwk = (PrintInfo)_param._prtinflist.get(ii);
        	svf.VrSetForm(_param._formname, 4);

        	PrintCntCtl pObj = new PrintCntCtl();
            pObj._pagenum = pageCount(printwk);
            pObj._pagecnt = 1;
            pObj._prtinpagecnt = 0;

            //各選択情報毎に処理(※タイトル出力は、改ページを各表毎に行うため、個々の表毎に判定して出力)
            ///表への出力
            ////基本情報
            String ttlstr = "1".equals(_param._disppattern) ? "中学校カルテ" : "塾カルテ";
            svf.VrsOut("TITLE", ttlstr);
            setPageCntHead(svf, pObj);

            String fsnhstr = "1".equals(_param._disppattern) ? "中学校" : "塾名";
            svf.VrsOut("FINSCHOOL_NAME_HEADER", fsnhstr);

            svf.VrsOut("FINSCHOOL_CD", printwk._si._schcode);
            svf.VrsOut("FINSCHOOL_NAME", StringUtils.defaultString(printwk._si._name, "") + StringUtils.defaultString(printwk._si._schclsname, ""));
            svf.VrsOut("STAFF", StringUtils.defaultString(printwk._si._staffname, ""));

            String fsjhstr = "1".equals(_param._disppattern) ? "校長" : "代表者";
            svf.VrsOut("FINSCHOOL_JOB_HEADER", fsjhstr);
            svf.VrsOut("FINSCHOOL_PRINCIPAL_NAME", StringUtils.defaultString(printwk._si._pricname));

            String fschstr = "1".equals(_param._disppattern) ? "統廃校" : "";
            svf.VrsOut("FINSCHOOL_CONBINE_HEADER", fschstr);
            svf.VrsOut("COMBINE", StringUtils.defaultString(printwk._si._rmk, ""));

            svf.VrsOut("FINSCHOOL_PREF", StringUtils.defaultString(printwk._si._zipcd, ""));
            svf.VrsOut("FINSCHOOL_ADDR", StringUtils.defaultString(printwk._si._addr1, "") + StringUtils.defaultString(printwk._si._addr2, ""));

            svf.VrsOut("FINSCHOOL_TELNO", StringUtils.defaultString(printwk._si._telno, ""));
            svf.VrsOut("FINSCHOOL_FAXNO", StringUtils.defaultString(printwk._si._faxno, ""));

        	/////改ページ制御,タイトル出力 <-固定表なので、改ページは不要。
            pObj._prtinpagecnt += 5+1;

            svf.VrsOut("BLANK", "AAA");

            ////入試情報
    		setExamTitle(svf);
            pObj._prtinpagecnt += 2;

    		int jj = 0;
        	for (jj = 0;jj < printwk._ei.size();jj++) {
        		int jidx = jj % 2 == 0 ? jj / 2 : ((jj - 1 + printwk._ei.size()) / 2);
        		//reccnt = (jj / 2) + 1;
        		examCntInfo eiprintwk = (examCntInfo)printwk._ei.get(jidx);
        		final String field1 = jj % 2 == 0 ? "EXAM_INFO_YEAR1" : "EXAM_INFO_YEAR2";
            	svf.VrsOut(field1, eiprintwk._year);
        		final String field2 = jj % 2 == 0 ? "EXAM_INFO_SELECT1" : "EXAM_INFO_SELECT2";
            	svf.VrsOut(field2, eiprintwk._petitionname);
        		final String field3 = jj % 2 == 0 ? "EXAM_INFO_COURSE1" : "EXAM_INFO_COURSE2";
                int courselen = KNJ_EditEdit.getMS932ByteLength(eiprintwk._coursename);
                String coursefield = courselen > 10 ? "_2" : "";
            	svf.VrsOut(field3 + coursefield, eiprintwk._coursename);
        		final String field4 = jj % 2 == 0 ? "EXAM_INFO_EXAM_NUM1" : "EXAM_INFO_EXAM_NUM2";
            	svf.VrsOut(field4, eiprintwk._cnt1);
        		final String field5 = jj % 2 == 0 ? "EXAM_INFO_ENT_NUM1" : "EXAM_INFO_ENT_NUM2";
            	svf.VrsOut(field5, eiprintwk._cnt2);
            	/////現状3コース。MAX5コース5年=50件->2列なので25行出力なら大丈夫。
            	if (jj % 2 == 1) {
            		svf.VrEndRecord();
            	}
        	}
        	//片方だけ出力して終了する場合、これで終了とする。
        	if (jj == printwk._ei.size() && jj % 2 == 1) {
        		svf.VrEndRecord();
        	} else if (jj == 0) {
        		svf.VrsOut("EXAM_INFO_BLANK", "AAA");
        		svf.VrEndRecord();
        	}
        	pObj._prtinpagecnt += (jj == 0 ? 1 : roundup(printwk._ei.size(), 2)) + 1;

            svf.VrsOut("BLANK", "AAA");

            ////在校生情報
        	setEnroll(svf, printwk, pObj);

            ////卒業生情報
        	setGrad(svf, printwk, pObj);

            ////その他情報
        	if ("1".equals(_param._disppattern)) {
        	    setOther(svf, printwk, pObj);
        	} else {
        	    setOther2(svf, printwk, pObj);
        	}
        }

    }

    private void setExamTitle(final Vrw32alp svf) {
    	svf.VrsOut("EXAM_INFO_TITLE1", "受験情報");
    	svf.VrsOut("EXAM_INFO_TITLE2", "受験情報");
    	svf.VrsOut("EXAM_INFO_HEADER1", "AAA");
    }

    private void setEnroll(final Vrw32alp svf, PrintInfo printwk, PrintCntCtl pObj) {

        ////在校生情報
        setEnrollTitle(svf);
        pObj._prtinpagecnt += 2; //タイトル+項目名称を出力したのでカウント
		int reccnt = 1;
		Set skey = printwk._s.keySet();
        for (final Iterator sit = skey.iterator(); sit.hasNext();) {
        	final String skstr = (String)sit.next();
            final Student student = (Student) printwk._s.get(skstr);
            if (pObj._prtinpagecnt + 3 > PRINTMAXLINE) {
            	//改ページ
            	//svf.VrEndPage();
            	svf.VrSetForm(_param._formname, 4);
            	pObj._pagecnt++;
                setPageCntHead(svf, pObj);
                setEnrollTitle(svf);
                pObj._prtinpagecnt = 2;//ヘッダ分を加味
                reccnt = 1;
            }
            //1列目
            svf.VrsOut("ENROLL_INFO_YEAR1",  student._entdate);
            int courselen = KNJ_EditEdit.getMS932ByteLength(student._course);
            String coursefield = courselen > 8 ? "_2" : "";
            svf.VrsOut("ENROLL_INFO_COURSE1" + coursefield,  student._course);
            svf.VrsOut("ENROLL_INFO_HR_NAME1",  student._gradename+student._clsname+ " " + student._attendno);
            svf.VrsOut("ENROLL_INFO_SEX1",  student._sex);
            int namelen = KNJ_EditEdit.getMS932ByteLength(student._name);
            String namefield = namelen > 10 ? "ENROLL_INFO_NAME2": "ENROLL_INFO_NAME1";
            svf.VrsOut(namefield, student._name);

            printStudentDetail(svf, "ENROLL_INFO_SEMESTER1", reccnt, (String)_param._semestername.get("1"), "");
            printStudentDetailScore(svf, "ENROLL_INFO_SCORE1_1", reccnt, (AvgScoreInfo)student._RecordInfo.get("011"), "");
            printStudentDetailScore(svf, "ENROLL_INFO_SCORE1_2", reccnt, (AvgScoreInfo)student._RecordInfo.get("021"), "");
            printStudentDetailScore(svf, "ENROLL_INFO_SCORE1_3", reccnt, (AvgScoreInfo)student._RecordInfo.get("031"), "");
            printStudentDetailNotice(svf, "ENROLL_INFO_NOTICE1_1", reccnt, (AttendInfo)student._AttendInfo.get("011"), "");
            printStudentDetailNotice(svf, "ENROLL_INFO_NOTICE1_2", reccnt, (AttendInfo)student._AttendInfo.get("021"), "");
            printStudentDetailNotice(svf, "ENROLL_INFO_NOTICE1_3", reccnt, (AttendInfo)student._AttendInfo.get("031"), "");
            if ("1".equals(_param._disppattern)) {
                printStudentDetail(svf, "ENROLL_INFO_PRISCHOOL_NAME1", reccnt, (String)student._prischname[0], "");
            } else {
                printStudentDetail(svf, "ENROLL_INFO_PRISCHOOL_NAME1", reccnt, (String)student._finschnameArray[0], "");
            }
            svf.VrsOut("ENROLL_INFO_SKILL1", student._tokutaikbn);
            final String[] token = KNJ_EditEdit.get_token(student._clubname, 10, 3);
            if (token != null && !"".equals(token[0])) {
                svf.VrsOut("ENROLL_INFO_CLUB1", token[0]);
            }
            svf.VrEndRecord();

            //2行目
            printStudentDetail(svf, "ENROLL_INFO_SEMESTER2", reccnt, (String)_param._semestername.get("2"), "");
            printStudentDetailScore(svf, "ENROLL_INFO_SCORE2_1", reccnt, (AvgScoreInfo)student._RecordInfo.get("012"), "");
            printStudentDetailScore(svf, "ENROLL_INFO_SCORE2_2", reccnt, (AvgScoreInfo)student._RecordInfo.get("022"), "");
            printStudentDetailScore(svf, "ENROLL_INFO_SCORE2_3", reccnt, (AvgScoreInfo)student._RecordInfo.get("032"), "");
            printStudentDetailNotice(svf, "ENROLL_INFO_NOTICE2_1", reccnt, (AttendInfo)student._AttendInfo.get("012"), "");
            printStudentDetailNotice(svf, "ENROLL_INFO_NOTICE2_2", reccnt, (AttendInfo)student._AttendInfo.get("022"), "");
            printStudentDetailNotice(svf, "ENROLL_INFO_NOTICE2_3", reccnt, (AttendInfo)student._AttendInfo.get("032"), "");
            if ("1".equals(_param._disppattern)) {
                printStudentDetail(svf, "ENROLL_INFO_PRISCHOOL_NAME2", reccnt, (String)student._prischname[1], "");
            } else {
                printStudentDetail(svf, "ENROLL_INFO_PRISCHOOL_NAME2", reccnt, (String)student._finschnameArray[1], "");
            }
            //ENROLL_INFO_SKILL2
            if (token != null && !"".equals(token[1])) {
                svf.VrsOut("ENROLL_INFO_CLUB2", token[1]);
            }
            svf.VrEndRecord();

            //3行目
            printStudentDetail(svf, "ENROLL_INFO_SEMESTER3", reccnt, (String)_param._semestername.get("3"), "");
            printStudentDetailScore(svf, "ENROLL_INFO_SCORE3_1", reccnt, (AvgScoreInfo)student._RecordInfo.get("013"), "");
            printStudentDetailScore(svf, "ENROLL_INFO_SCORE3_2", reccnt, (AvgScoreInfo)student._RecordInfo.get("023"), "");
            printStudentDetailScore(svf, "ENROLL_INFO_SCORE3_3", reccnt, (AvgScoreInfo)student._RecordInfo.get("033"), "");
            printStudentDetailNotice(svf, "ENROLL_INFO_NOTICE3_1", reccnt, (AttendInfo)student._AttendInfo.get("013"), "");
            printStudentDetailNotice(svf, "ENROLL_INFO_NOTICE3_2", reccnt, (AttendInfo)student._AttendInfo.get("023"), "");
            printStudentDetailNotice(svf, "ENROLL_INFO_NOTICE3_3", reccnt, (AttendInfo)student._AttendInfo.get("033"), "");
            if ("1".equals(_param._disppattern)) {
                printStudentDetail(svf, "ENROLL_INFO_PRISCHOOL_NAME3", reccnt, (String)student._prischname[2], "");
            } else {
                printStudentDetail(svf, "ENROLL_INFO_PRISCHOOL_NAME3", reccnt, (String)student._finschnameArray[2], "");
            }
            //ENROLL_INFO_SKILL3
            if (token != null && !"".equals(token[2])) {
                svf.VrsOut("ENROLL_INFO_CLUB3", token[2]);
            }
            svf.VrEndRecord();
            pObj._prtinpagecnt += 3;
            reccnt++;
    	}
        if (printwk._s.size() == 0) {
            svf.VrsOut("EXAM_INFO_BLANK2", "AAA");
            svf.VrEndRecord();
            svf.VrsOut("EXAM_INFO_BLANK3", "AAA");
            svf.VrEndRecord();
            svf.VrsOut("EXAM_INFO_BLANK4", "AAA");
            svf.VrEndRecord();
            pObj._prtinpagecnt += 3;//空行分
        }
        pObj._prtinpagecnt += 1;//余白を加味
        if (pObj._prtinpagecnt + 3 >= PRINTMAXLINE) { //まだ出力余白(次のヘッダ2行+データ1行)があるか、確認
        	//改ページ
        	svf.VrSetForm(_param._formname, 4);
        	pObj._pagecnt++;
            setPageCntHead(svf, pObj);
            pObj._prtinpagecnt = 0;//カウントクリア<-実際のカウントは次処理で出力時にカウント
        } else {
        	//余白を入れる
            svf.VrsOut("BLANK", "AAA");
        }
    }

    private void setEnrollTitle(final Vrw32alp svf) {
    	svf.VrsOut("ENROLL_INFO_TITLE1", "在学生情報");
        String eiphstr = "1".equals(_param._disppattern) ? "出身塾名" : "出身中学";
    	svf.VrsOut("ENROLL_INFO_PRISCHOOL_HEADER", eiphstr);
    }

    private void setGrad(final Vrw32alp svf, PrintInfo printwk, PrintCntCtl pObj) {
        ////卒業生情報
        setGradTitle(svf);
        pObj._prtinpagecnt += 2;//タイトル+項目名称を出力したのでカウント。

		Set gkey = printwk._gs.keySet();
		for (final Iterator git = gkey.iterator(); git.hasNext();) {
        	final String gkstr = (String)git.next();
            final Student student = (Student) printwk._gs.get(gkstr);

            if (pObj._prtinpagecnt + 1 > PRINTMAXLINE) {
            	//改ページ
            	svf.VrSetForm(_param._formname, 4);
            	pObj._pagecnt++;
                setPageCntHead(svf, pObj);
                setGradTitle(svf);
                pObj._prtinpagecnt = 2;//ヘッダ分を加味
            }

            svf.VrsOut("GRAD_INFO_YEAR1", student._entdate);
            int courselen = KNJ_EditEdit.getMS932ByteLength(student._course);
            String coursefield = courselen > 8 ? "_2" : "";
            svf.VrsOut("GRAD_INFO_COURSE1" + coursefield, student._course);
            svf.VrsOut("GRAD_SCHREG_NO", student._gradename+student._clsname+" " + student._attendno);
            final int namelen = KNJ_EditEdit.getMS932ByteLength(student._name);
            final String namefield = namelen > 10 ? "GRAD_INFO_NAME2": "GRAD_INFO_NAME1";
            svf.VrsOut(namefield, student._name);
            svf.VrsOut("GRAD_INFO_SEX1", student._sex);
            svf.VrsOut("GRAD_INFO_SCORE1_1", student._sex);

            if ("1".equals(_param._disppattern)) {//出身中学
            	//塾を出力
                final int prinamelen = KNJ_EditEdit.getMS932ByteLength(student._prischname[0]);
                final String prinamefield = prinamelen > 20 ? "GRAD_INFO_PRISCHOOL_NAME3" : prinamelen > 10 ? "GRAD_INFO_PRISCHOOL_NAME2" : "GRAD_INFO_PRISCHOOL_NAME1";
                svf.VrsOut(prinamefield, student._prischname[0]);
            } else {
            	//出身中学を出力
                final int prinamelen = KNJ_EditEdit.getMS932ByteLength(student._finschname);
                final String prinamefield = prinamelen > 20 ? "GRAD_INFO_PRISCHOOL_NAME3" : prinamelen > 10 ? "GRAD_INFO_PRISCHOOL_NAME2" : "GRAD_INFO_PRISCHOOL_NAME1";
                svf.VrsOut(prinamefield, student._finschname);
            }
            final int clubnamelen = KNJ_EditEdit.getMS932ByteLength(student._clubname);
            final String clubnamefield = clubnamelen > 8 ? "GRAD_INFO_CLUB3_1" : clubnamelen > 6 ? "GRAD_INFO_CLUB2" : "GRAD_INFO_CLUB1";
            svf.VrsOut(clubnamefield, student._clubname);

            final int aftnamelen = KNJ_EditEdit.getMS932ByteLength(student._remark1);
            final String aftnamefield = aftnamelen > 10 ? "GRAD_INFO_AFT2": "GRAD_INFO_AFT1";
            svf.VrsOut(aftnamefield, student._remark1);

            final int passnamelen = KNJ_EditEdit.getMS932ByteLength(student._remark2);
            final String passnamefield = passnamelen > 10 ? "GRAD_INFO_PASS2": "GRAD_INFO_PASS1";
            svf.VrsOut(passnamefield, student._remark2);

            final int skillnamelen = KNJ_EditEdit.getMS932ByteLength(student._tokutaikbn);
            final String skillnamefield = skillnamelen > 10 ? "GRAD_INFO_SKILL2": "GRAD_INFO_SKILL1";
            svf.VrsOut(skillnamefield, student._tokutaikbn);

            pObj._prtinpagecnt += 1;
            svf.VrEndRecord();
    	}
        if (printwk._gs.size() == 0) {
            svf.VrsOut("GRAD_INFO_BLANK", "AAA");
            svf.VrEndRecord();
            pObj._prtinpagecnt += 1;//空行分
        }
        pObj._prtinpagecnt += 1;//余白を加味
        if (pObj._prtinpagecnt + 3 >= PRINTMAXLINE ) { //まだ出力余白(次のヘッダ2行+データ1行)があるか、確認
        	//改ページ
        	svf.VrSetForm(_param._formname, 4);
        	pObj._pagecnt++;
            setPageCntHead(svf, pObj);
            pObj._prtinpagecnt = 0;//カウントクリア<-実際のカウントは次処理で出力時にカウント
        } else {
            svf.VrsOut("BLANK", "AAA");
        }
    }

    private void setGradTitle(final Vrw32alp svf) {
    	svf.VrsOut("GRAD_INFO_TITLE1", "卒業生情報");
        String eiphstr = "1".equals(_param._disppattern) ? "塾名" : "出身中学";
    	svf.VrsOut("GRAD_INFO_PRISCHOOL_HEADER", eiphstr);
    }

    private void setOther(final Vrw32alp svf, PrintInfo printwk, PrintCntCtl pObj) {
        ////その他の情報
        setOtherTitle(svf);
        pObj._prtinpagecnt += 1;

		Set okey = printwk._os.keySet();
		for (final Iterator oit = okey.iterator(); oit.hasNext();) {
        	final String okstr = (String)oit.next();
            final Student student = (Student) printwk._os.get(okstr);

            if (pObj._prtinpagecnt + 1 > PRINTMAXLINE) {
            	//改ページ
            	svf.VrSetForm(_param._formname, 4);
            	pObj._pagecnt++;
                setPageCntHead(svf, pObj);
                setOtherTitle(svf);
                pObj._prtinpagecnt = 2;//ヘッダ分を加味
            }

            svf.VrsOut("MOVE_INFO_YEAR1", student.getEntYear());
            svf.VrsOut("MOVE_DATE", student._movedate);

            final int clubnamelen = KNJ_EditEdit.getMS932ByteLength(student._clubname);
            final String clubnamefield = clubnamelen > 8 ? "MOVE_INFO_CLUB3_1" : clubnamelen > 6 ? "MOVE_INFO_CLUB2" : "MOVE_INFO_CLUB1";
            svf.VrsOut(clubnamefield, student._clubname);
            final int namelen = KNJ_EditEdit.getMS932ByteLength(student._name);
            final String namefield = namelen > 10 ? "MOVE_INFO_NAME2": "MOVE_INFO_NAME1";
            svf.VrsOut(namefield, student._name);
            svf.VrsOut("MOVE_KIND", student._movediv);
            final int skilllen = KNJ_EditEdit.getMS932ByteLength(student._tokutaikbn);
            final String skillfield = skilllen > 6 ? "MOVE_INFO_SKILL2": "MOVE_INFO_SKILL1";
            svf.VrsOut(skillfield, student._tokutaikbn);

            svf.VrsOut("MOVE_INFO_SEX1", student._sex);
            final int transtolen = KNJ_EditEdit.getMS932ByteLength(student._transto);
            final String transtofield = transtolen > 22 ? "MOVE_INFO_AFT2" : "MOVE_INFO_AFT1";
            svf.VrsOut(transtofield, student._transto);

            svf.VrEndRecord();
            pObj._prtinpagecnt += 1;
    	}
        if (printwk._gs.size() == 0) {
            svf.VrsOut("MOVE_INFO_BLANK", "AAA");
            svf.VrEndRecord();
            pObj._prtinpagecnt += 1;//空行分
        }
        pObj._prtinpagecnt += 1;//余白を加味
        //これ以降、出力は無いので改ページ処理はしない。
    }

    private void setOtherTitle(final Vrw32alp svf) {
    	if ("1".equals(_param._disppattern)) {
        	svf.VrsOut("MOVE_INFO_TITLE1", "異動情報");
    	    svf.VrsOut("MOVE_INFO_HEADER2", "AAA");
    	} else {
    	    svf.VrsOut("PRI_INFO_TITLE1", "塾訪問記録");
    	    svf.VrsOut("PRI_INFO_HEADER2", "AAA");
    	}
    }

    private void setOther2(final Vrw32alp svf, PrintInfo printwk, PrintCntCtl pObj) {
        ////その他の情報
        setOtherTitle(svf);
        pObj._prtinpagecnt += 1;

        boolean dataFlg = false;
        Set jkey = printwk._ji.keySet();
        for (final Iterator jit = jkey.iterator(); jit.hasNext();) {
            final String jkstr = (String)jit.next();
            final PriSchVisitInfo visitinfo = (PriSchVisitInfo) printwk._ji.get(jkstr);

            if (pObj._prtinpagecnt + 1 >= PRINTMAXLINE) {
                //改ページ
                svf.VrSetForm(_param._formname, 4);
                pObj._pagecnt++;
                setPageCntHead(svf, pObj);
                setOtherTitle(svf);
                pObj._prtinpagecnt = 2;//ヘッダ分を加味
            }

            svf.VrsOut("PRI_INFO_YEAR1", visitinfo._visityear);
            svf.VrsOut("PRI_INFO_DATE", visitinfo._visitdate);

            final int pristafflen = KNJ_EditEdit.getMS932ByteLength(visitinfo._visitstaff);
            final String pristafffield = pristafflen > 10 ? "PRI_INFO_STAFF2": "PRI_INFO_STAFF1";
            svf.VrsOut(pristafffield, visitinfo._visitstaff);

            final int visitorlen = KNJ_EditEdit.getMS932ByteLength(visitinfo._visitor);
            final String visitorfield = visitorlen > 10 ? "PRI_INFO_INTERVIEW2": "PRI_INFO_INTERVIEW1";
            svf.VrsOut(visitorfield, visitinfo._visitor);

            svf.VrsOut("PRI_INFO_COMMENT", visitinfo._comment);

            final int examinfolen = KNJ_EditEdit.getMS932ByteLength(visitinfo._examinfo);
            final String examinfofield = examinfolen > 22 ? "PRI_INFO_STUDENT2": "PRI_INFO_STUDENT1";
            svf.VrsOut(examinfofield, visitinfo._examinfo);

            final int remarklen = KNJ_EditEdit.getMS932ByteLength(visitinfo._remark);
            final String remarkfield = remarklen > 22 ? "PRI_INFO_REMARK2": "PRI_INFO_REMARK1";
            svf.VrsOut(remarkfield, visitinfo._remark);

            svf.VrEndRecord();
            pObj._prtinpagecnt += 1;

            if("".equals(visitinfo._visitdate)) {
                //データがない場合、空行1行を出力する。
                svf.VrsOut("PRI_INFO_BLANK", "AAA");
                svf.VrEndRecord();
            }
            dataFlg = true;
        }

        if(!dataFlg) {
            //データがない場合、空行1行を出力する。
            svf.VrsOut("PRI_INFO_BLANK", "AAA");
            svf.VrEndRecord();
        }

        pObj._prtinpagecnt += 1;//余白を加味
        //これ以降、出力は無いので改ページ処理はしない。
    }


    private void printStudentDetail(final Vrw32alp svf, final String printfield, final int reccnt, final String outputstr, final String defstr) {
        String outval = outputstr == null ? defstr : outputstr;
        svf.VrsOut(printfield, outval);
    }
    private void printStudentDetailScore(final Vrw32alp svf, final String printfield, final int reccnt, final AvgScoreInfo outputstr, final String defstr) {
        String outval = "";
        if (outputstr == null) {
            outval = defstr;
        } else {
            BigDecimal roundwk = new BigDecimal(outputstr._avg);
            outval = roundwk.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
        }
        svf.VrsOut(printfield, outval);
    }
    private void printStudentDetailNotice(final Vrw32alp svf, final String printfield, final int reccnt, AttendInfo attinf, final String defstr) {
        String outval = attinf == null ? defstr : attinf._attend;
        svf.VrsOut(printfield, outval);
    }

    private void setPageCntHead(final Vrw32alp svf, PrintCntCtl pObj) {
        svf.VrsOut("PAGE1", String.valueOf(pObj._pagecnt));
        svf.VrsOut("PAGE2", String .valueOf(pObj._pagenum));
    }
    private void getInfo(final DB2UDB db2) {
    	String sql = "";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
        	if ("1".equals(_param._disppattern)) { //出身中学
        	    sql = sqlFinSchInfo(_param._selectedInState);
        	} else {
        		sql = sqlPriSchInfo(_param._selectedInState, _param._selectedInState2);
        	}
            log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
            	final String schcd = rs.getString("SCHCODE");
            	final String name = rs.getString("NAME");
            	final String pricname = rs.getString("PRICNAME");
            	final String staffname = rs.getString("STAFFNAME");
            	final String zipcd = rs.getString("ZIPCD");
            	final String addr1 = rs.getString("ADDR1");
            	final String addr2 = rs.getString("ADDR2");
            	final String telno = rs.getString("TELNO");
            	final String faxno = rs.getString("FAXNO");
            	final String rmk;
            	final String schclscd;
            	final String schclsname;
            	if ("1".equals(_param._disppattern)) { //出身中学
            		rmk = rs.getString("REMARK1");
            		schclscd = "";
            		schclsname = "";
            	} else {
            		rmk = "";
            		schclscd = rs.getString("SCHCLSCODE");
            		schclsname =  rs.getString("SCHCLSNAME");
            	}
            	schInfo addinf = new schInfo(schcd, name, pricname, zipcd, addr1, addr2, telno, faxno, rmk, schclscd, schclsname, staffname);
            	PrintInfo addwk = new PrintInfo(addinf);
            	getExamInf(db2, addwk);
            	getStudentInf(db2, addwk);
            	if ("2".equals(_param._disppattern)) {
            		getPriSchInviteInf(db2, addwk,rs.getString("PRISCHOOLCD"),StringUtils.defaultString(rs.getString("PRISCHOOL_CLASS_CD")));
            	}
                _param._prtinflist.add(addwk);
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return;
    }

    private void getExamInf(final DB2UDB db2, PrintInfo setwk) {
    	String sql = "";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
        	if ("1".equals(_param._disppattern)) { //出身中学
        	    sql = sqlgetFinSchExamCnt(setwk._si._schcode);
        	} else {
        		sql = sqlgetPriSchExamCnt(setwk._si._schcode, setwk._si._schclscd);
        	}
            log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
            	final String year = rs.getString("YEAR");
            	final String petition = rs.getString("PETITIONNAME");
            	final String coursename = rs.getString("COURSENAME");
            	final String cnt1 = rs.getString("CNT1");
            	final String cnt2 =  rs.getString("CNT2");
            	examCntInfo addcntinf = new examCntInfo(year, petition, coursename, cnt1, cnt2);
            	setwk._ei.add(addcntinf);
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return;
    }

    private void getStudentInf(final DB2UDB db2, final PrintInfo setwk) {
    	String sql = "";
        PreparedStatement ps = null;
        ResultSet rs = null;
        String nowStudentInState = "";
        String grdStudentInState = "";
        String sep = "";
        String sep2 = "";
        try {
        	if ("1".equals(_param._disppattern)) { //出身中学
        	    sql = sqlgetFinSchStudentCnt(setwk._si._schcode);
        	} else {
        		sql = sqlgetPriSchStudentCnt(setwk._si._schcode, setwk._si._schclscd);
        	}
            log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
            	final String entdate = rs.getString("ENT_DATE");
            	final String year = rs.getString("YEAR");
            	final String schregno = rs.getString("SCHREGNO");
            	final String grade = rs.getString("GRADE");
            	final String gradename = rs.getString("GRADE_NAME1");
            	final String hrclass = rs.getString("HR_CLASS");
            	final String hrname = rs.getString("HR_NAMEABBV");
            	final String attendno = NumberUtils.isDigits(rs.getString("ATTENDNO")) ? String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))) : StringUtils.defaultString(rs.getString("ATTENDNO"));
            	final String sex = rs.getString("SEX");
            	final String name = rs.getString("NAME");
            	final String leave = rs.getString("LEAVE");
            	final String grddivname = rs.getString("GRD_DIV_NAME");
            	final String grddate = rs.getString("GRD_DATE");
            	final String scholarshipname = rs.getString("SCHOLARSHIPNAME");
            	final String clubname = rs.getString("CLUBNAME");
            	final String cm1name = rs.getString("CM1_NAME");
            	final String cm2name = rs.getString("CM2_NAME");
            	final String coursename =  rs.getString("COURSECODEABBV1");
            	final String finschname = rs.getString("FINSCHOOL_NAME");
            	final String[] finschnameToken = KNJ_EditEdit.get_token(finschname, 16, 3);
				final String[] finschnameArray = null == finschnameToken ? new String[] {"", "", ""} : finschnameToken;
            	final String transto = rs.getString("TRANSTO");
            	final String[] prischname = new String[3];
            	final String prischnamewk1 =  StringUtils.defaultString(rs.getString("PRISCHCLSNAME1"), "");
                prischname[0] = StringUtils.defaultString(rs.getString("PRISCHNAME1"), "") + (!"".equals(prischnamewk1) ? "," + prischnamewk1 : "");
            	final String prischnamewk2 =  StringUtils.defaultString(rs.getString("PRISCHCLSNAME2"), "");
                prischname[1] = StringUtils.defaultString(rs.getString("PRISCHNAME2"), "") + (!"".equals(prischnamewk2) ? "," + prischnamewk2 : "");
            	final String prischnamewk3 =  StringUtils.defaultString(rs.getString("PRISCHCLSNAME3"), "");
          	    prischname[2] = StringUtils.defaultString(rs.getString("PRISCHNAME3"), "") + (!"".equals(prischnamewk3) ? "," + prischnamewk3 : "");
            	Student addstinf = new Student(year, schregno, entdate, leave, coursename, grade, gradename, hrclass, hrname, attendno,
            			                        sex, name,scholarshipname, clubname, finschname, finschnameArray, prischname,
            			                        grddate, grddivname,cm1name, cm2name, transto);
            	if ("0".equals(leave)) {//在校生
            	    setwk._s.put(schregno, addstinf);
            	    nowStudentInState += sep + "'" + schregno + "'";
            	    sep = ",";
            	} else if ("2".equals(leave)) {//卒業生
            	    setwk._gs.put(schregno, addstinf);
            	    grdStudentInState += sep2 + "'" + schregno + "'";
            	    sep2 = ",";
                } else if ("1".equals(_param._disppattern) && "1".equals(leave)) {//その他
            	    setwk._os.put(schregno, addstinf);
                }
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        //在校生データがある場合は、成績、欠席情報を取得する
        if (!"".equals(nowStudentInState)) {
        	nowStudentInState = "(" + nowStudentInState + ")";
        	getAttendInfo(db2, setwk, nowStudentInState);
        	getScoreInfo(db2, setwk, nowStudentInState);
        }
        //卒業生データがある場合は、成績を取得する
        if (!"".equals(grdStudentInState)) {
        	grdStudentInState = "(" + grdStudentInState + ")";
        	getFinalScoreInfo(db2, setwk, grdStudentInState);
        }
        return;
    }

    private void getAttendInfo(final DB2UDB db2, final PrintInfo setwk, final String nowStudentInState) {
    	String sql = "";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
      	    sql = sqlAttendInfo(nowStudentInState);
            log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            String bkschregno = "";
            Student wkobj = null;
            while (rs.next()) {
            	final String semester = rs.getString("SEMESTER");
            	final String grade = rs.getString("GRADE");
            	final String gradecd = rs.getString("GRADE_CD");
            	final String schregno = rs.getString("SCHREGNO");
            	final String attend = rs.getString("ATTEND");
            	AttendInfo addwk = new AttendInfo(grade, semester, attend);
            	if ("".equals(bkschregno) || !bkschregno.equals(schregno)) {
            	    wkobj = (Student)setwk._s.get(schregno);
            	}
            	if (wkobj != null) {
            		wkobj._AttendInfo.put(gradecd+semester, addwk);
            	}
            	bkschregno = schregno;
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return;
    }

    private void getScoreInfo(final DB2UDB db2, final PrintInfo setwk, final String nowStudentInState) {
    	String sql = "";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
      	    sql = sqlScoreInfo(nowStudentInState);
            log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            String bkschregno = "";
            Student wkobj = null;
            while (rs.next()) {
            	final String semester = rs.getString("SEMESTER");
            	final String grade = rs.getString("GRADE");
            	final String gradecd = rs.getString("GRADE_CD");
            	final String schregno = rs.getString("SCHREGNO");
            	final String avg = rs.getString("AVG");
            	AvgScoreInfo addwk = new AvgScoreInfo(grade, semester, avg);
            	if ("".equals(bkschregno) || !bkschregno.equals(schregno)) {
            	    wkobj = (Student)setwk._s.get(schregno);
            	}
            	if (wkobj != null) {
            		wkobj._RecordInfo.put(gradecd+semester, addwk);
            	}
            	bkschregno = schregno;
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return;
    }

    private void getFinalScoreInfo(final DB2UDB db2, final PrintInfo setwk, final String nowStudentInState) {
    	String sql = "";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
      	    sql = sqlFinalScoreInfo(nowStudentInState);
            log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            String bkschregno = "";
            Student wkobj = null;
            while (rs.next()) {
            	final String schregno = rs.getString("SCHREGNO");
            	final String avg = rs.getString("AVG");
            	if ("".equals(bkschregno) || !bkschregno.equals(schregno)) {
            	    wkobj = (Student)setwk._s.get(schregno);
            	}
            	if (wkobj != null) {
            		wkobj._avg = avg;
            	}
            	bkschregno = schregno;
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return;
    }

    private void getPriSchInviteInf(final DB2UDB db2, final PrintInfo setwk, final String priSchoolCd, final String priSchoolClassCd) {
    	String sql = "";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            sql = sqlPriSchInviteInfo(priSchoolCd, priSchoolClassCd);
            log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String visityear = rs.getString("VISITYEAR");
                final String visitdate = StringUtils.defaultString(rs.getString("VISITDATE"));
                final String visitstaff = rs.getString("VISITSTAFF");
                final String staffcd = rs.getString("STAFFCD");
                final String visitor = rs.getString("VISITOR");
                final String comment = rs.getString("COMMENT");
                final String examinfo = rs.getString("EXAMINFO");
                final String remark = rs.getString("REMARK");
                PriSchVisitInfo addwk = new PriSchVisitInfo(visityear, visitdate, visitstaff, visitor, comment, examinfo, remark);
                //訪問日+教員名+面会者でkeyとする。
                setwk._ji.put(visitdate + staffcd + visitor, addwk);
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return;
    }

    private String sqlFinSchInfo(final String finschcds) {
        final StringBuffer stb = new StringBuffer();
        //出身中学の情報を取得する。
        //1学校に複数の先生が登録されている事を想定して、1学校に対して1人を抽出する(番号若い人)。
        stb.append(" WITH STAFFDAT AS ( ");
        stb.append("   SELECT ");
        stb.append("     YEAR, ");
        stb.append("     FINSCHOOLCD, ");
        stb.append("     MIN(STAFFCD) AS STAFFCD ");
        stb.append("   FROM ");
        stb.append("     STAFF_RECRUIT_DAT ");
        stb.append("   WHERE ");
        stb.append("     YEAR = '" + _param._year + "' ");
        stb.append("     AND FINSCHOOLCD IN " + finschcds + " ");
        stb.append("     AND RECRUIT_DIV = '1' ");
        stb.append("   GROUP BY ");
        stb.append("     YEAR, ");
        stb.append("     FINSCHOOLCD ");
        stb.append(" ) ");
        // 基本情報と、上記担任1人を紐づける。
        stb.append(" SELECT ");
        stb.append("   FY.FINSCHOOLCD AS SCHCODE, ");
        stb.append("   FM.FINSCHOOL_NAME AS NAME, ");
        stb.append("   FM.PRINCNAME AS PRICNAME, ");
        stb.append("   SM.STAFFNAME AS STAFFNAME,");
        stb.append("   FM.FINSCHOOL_ZIPCD AS ZIPCD, ");
        stb.append("   FM.FINSCHOOL_ADDR1 AS ADDR1, ");
        stb.append("   FM.FINSCHOOL_ADDR2 AS ADDR2, ");
        stb.append("   FM.FINSCHOOL_TELNO AS TELNO, ");
        stb.append("   FM.FINSCHOOL_FAXNO AS FAXNO, ");
        stb.append("   FDM.REMARK1 ");
        stb.append(" FROM ");
        stb.append("   FINSCHOOL_YDAT FY ");
        stb.append("   LEFT JOIN FINSCHOOL_MST FM ");
        stb.append("     ON FM.FINSCHOOLCD = FY.FINSCHOOLCD ");
        stb.append("   LEFT JOIN FINSCHOOL_DETAIL_MST FDM ");
        stb.append("     ON FDM.FINSCHOOLCD = FM.FINSCHOOLCD ");
        stb.append("     AND FINSCHOOL_SEQ = '001' ");
        stb.append("   LEFT JOIN STAFFDAT SRD");
        stb.append("     ON  SRD.YEAR = FY.YEAR ");
        stb.append("     AND SRD.FINSCHOOLCD = FY.FINSCHOOLCD ");
        stb.append("   LEFT JOIN STAFF_MST SM ");
        stb.append("     ON SM.STAFFCD = SRD.STAFFCD");
        stb.append(" WHERE ");
        stb.append("   FY.YEAR IN '"+_param._year+"' ");
        stb.append("   AND FY.FINSCHOOLCD IN " + finschcds + " ");
        stb.append(" ORDER BY");
        stb.append("   FY.FINSCHOOLCD ");

        return stb.toString();
    }

    private String sqlPriSchInfo(final String prischcds1, final String prischcds2) {
        final StringBuffer stb = new StringBuffer();

        //塾の情報を取得する。
        //1塾1教室に複数の先生が登録されている事を想定して、1塾1教室に対して1人を抽出する(番号若い人)。
        stb.append(" WITH STAFFDAT AS ( ");
        stb.append("   SELECT ");
        stb.append("     YEAR, ");
        stb.append("     PRISCHOOLCD, ");
        stb.append("     PRISCHOOL_CLASS_CD, ");
        stb.append("     MIN(STAFFCD) AS STAFFCD ");
        stb.append("   FROM ");
        stb.append("     STAFF_RECRUIT_DAT ");
        stb.append("   WHERE ");
        stb.append("     YEAR = '" + _param._year + "' ");
        if (prischcds1 != null && !"".equals(prischcds1) && prischcds2 != null && !"".equals(prischcds2)) {
            stb.append("     AND ((PRISCHOOLCD || '-' || PRISCHOOL_CLASS_CD IN " + prischcds1 + ") ");
            stb.append("           OR (PRISCHOOLCD IN " + prischcds2 + ")) ");
        } else if (prischcds1 != null && !"".equals(prischcds1)) {
                stb.append("     AND PRISCHOOLCD || '-' || PRISCHOOL_CLASS_CD IN " + prischcds1 + " ");
        } else {
            stb.append("     AND PRISCHOOLCD IN " + prischcds2 + " ");
        }
        stb.append("     AND RECRUIT_DIV = '2' ");
        stb.append("   GROUP BY ");
        stb.append("     YEAR, ");
        stb.append("     PRISCHOOLCD, ");
        stb.append("     PRISCHOOL_CLASS_CD ");
        stb.append(" ) ");
        // 基本情報と、上記担任1人を紐づける。
        stb.append(" SELECT ");
        stb.append("   PY.PRISCHOOLCD         AS PRISCHOOLCD, ");
        stb.append("   PCM.PRISCHOOL_CLASS_CD AS PRISCHOOL_CLASS_CD, ");
        stb.append("   PY.PRISCHOOLCD AS SCHCODE, ");
        stb.append("   PM.PRISCHOOL_NAME AS NAME, ");
        stb.append("   PCM.PRISCHOOL_CLASS_CD AS SCHCLSCODE, ");
        stb.append("   PCM.PRISCHOOL_NAME AS SCHCLSNAME, ");
        stb.append("   PM.PRINCNAME AS PRICNAME, ");
        stb.append("   SM.STAFFNAME AS STAFFNAME,");
        stb.append("   PM.PRISCHOOL_ZIPCD AS ZIPCD, ");
        stb.append("   PM.PRISCHOOL_ADDR1 AS ADDR1, ");
        stb.append("   PM.PRISCHOOL_ADDR2 AS ADDR2, ");
        stb.append("   PM.PRISCHOOL_TELNO AS TELNO, ");
        stb.append("   PM.PRISCHOOL_FAXNO AS FAXNO ");
        stb.append(" FROM ");
        stb.append("   PRISCHOOL_YDAT PY ");
        stb.append("   LEFT JOIN PRISCHOOL_MST PM ");
        stb.append("     ON PM.PRISCHOOLCD = PY.PRISCHOOLCD ");
        stb.append("   LEFT JOIN PRISCHOOL_CLASS_MST PCM ");
        stb.append("     ON PCM.PRISCHOOLCD = PY.PRISCHOOLCD ");
        stb.append("   LEFT JOIN STAFFDAT SRD");
        stb.append("     ON  SRD.YEAR = PY.YEAR ");
        stb.append("     AND SRD.PRISCHOOLCD = PY.PRISCHOOLCD ");
        stb.append("     AND SRD.PRISCHOOL_CLASS_CD = PCM.PRISCHOOL_CLASS_CD ");
        stb.append("   LEFT JOIN STAFF_MST SM ");
        stb.append("     ON SM.STAFFCD = SRD.STAFFCD");
        stb.append(" WHERE ");
        stb.append("   PY.YEAR = '"+_param._year+"' ");
        if (prischcds1 != null && !"".equals(prischcds1) && prischcds2 != null && !"".equals(prischcds2)) {
            stb.append("     AND ((PY.PRISCHOOLCD || '-' || PCM.PRISCHOOL_CLASS_CD IN " + prischcds1 + ") ");
            stb.append("           OR (PY.PRISCHOOLCD IN " + prischcds2 + ")) ");
        } else if (prischcds1 != null && !"".equals(prischcds1)) {
            stb.append("     AND PY.PRISCHOOLCD || '-' || PCM.PRISCHOOL_CLASS_CD IN " + prischcds1 + " ");
        } else {
            stb.append("     AND PY.PRISCHOOLCD IN " + prischcds2 + " ");
        }
        stb.append(" ORDER BY ");
        stb.append("   PY.PRISCHOOLCD, PCM.PRISCHOOL_CLASS_CD");

        return stb.toString();
    }

    private String sqlgetPriSchExamCnt(final String prischcd, final String prischclscd) {
        final StringBuffer stb = new StringBuffer();
        final int getpastyear = 4; //login年度を含めての5年なので、2018年なら、2014(=2018-4)～2018年が対象。

        //塾の受験数を集計する。
        //年度、専願/併願、コース毎に受験者数をカウント。
        stb.append(" WITH EXAMCNT AS ( ");
        stb.append(" SELECT ");
        stb.append("   EAD.ENTEXAMYEAR AS YEAR, ");
        stb.append("   ERDD.REMARK1 AS PETITIONDIV, ");
        stb.append("   ERDD.REMARK2 AS COURSE_CD, ");
        stb.append("   COUNT(EAD.EXAMNO) AS CNT1 ");
        stb.append(" FROM ");
        stb.append("   ENTEXAM_APPLICANTBASE_DETAIL_DAT EADD ");
        stb.append("   LEFT JOIN ENTEXAM_APPLICANTBASE_DAT EAD ");
        stb.append("     ON EAD.ENTEXAMYEAR = EADD.ENTEXAMYEAR ");
        stb.append("     AND EAD.APPLICANTDIV = EADD.APPLICANTDIV ");
        stb.append("     AND EAD.EXAMNO = EADD.EXAMNO ");
        stb.append("   LEFT JOIN ENTEXAM_RECEPT_DAT ERD ");
        stb.append("     ON ERD.ENTEXAMYEAR = EAD.ENTEXAMYEAR ");
        stb.append("     AND ERD.APPLICANTDIV = EAD.APPLICANTDIV ");
        stb.append("     AND ERD.EXAMNO = EAD.EXAMNO ");
        stb.append("   LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT ERDD ");
        stb.append("     ON ERDD.ENTEXAMYEAR = ERD.ENTEXAMYEAR ");
        stb.append("     AND ERDD.APPLICANTDIV = ERD.APPLICANTDIV ");
        stb.append("     AND ERDD.TESTDIV = ERD.TESTDIV ");
        stb.append("     AND ERDD.EXAM_TYPE = ERD.EXAM_TYPE ");
        stb.append("     AND ERDD.RECEPTNO = ERD.RECEPTNO ");
        stb.append("     AND ERDD.SEQ = '006' ");
        stb.append(" WHERE ");
        stb.append("   EADD.ENTEXAMYEAR >= '" + String.valueOf((Integer.parseInt(_param._year) - getpastyear)) + "' ");
        stb.append("   AND EAD.ENTEXAMYEAR <= '" + _param._year + "' ");
        stb.append("   AND ( ");
        stb.append("       (EADD.REMARK1 = '" + prischcd + "' AND EADD.REMARK3 = '" + prischclscd + "') ");
        stb.append("       OR ");
        stb.append("       (EADD.REMARK4 = '" + prischcd + "' AND EADD.REMARK5 = '" + prischclscd + "') ");
        stb.append("       OR ");
        stb.append("       (EADD.REMARK6 = '" + prischcd + "' AND EADD.REMARK7 = '" + prischclscd + "') ");
        stb.append("   ) ");
        stb.append(" GROUP BY ");
        stb.append("   EAD.ENTEXAMYEAR, ");
        stb.append("   ERDD.REMARK1, ");
        stb.append("   ERDD.REMARK2 ");
        //年度、専願/併願、コース毎に合格者数をカウント。
        stb.append(" ), ENTRYCNT AS ( ");
        stb.append(" SELECT ");
        stb.append("   EAD.ENTEXAMYEAR AS YEAR, ");
        stb.append("   ERDD.REMARK1 AS PETITIONDIV, ");
        stb.append("   ERDD.REMARK2 AS COURSE_CD, ");
        stb.append("   COUNT(EAD.EXAMNO) AS CNT2 ");
        stb.append(" FROM ");
        stb.append("   ENTEXAM_APPLICANTBASE_DETAIL_DAT EADD ");
        stb.append("   LEFT JOIN ENTEXAM_APPLICANTBASE_DAT EAD ");
        stb.append("     ON EAD.ENTEXAMYEAR = EADD.ENTEXAMYEAR ");
        stb.append("     AND EAD.APPLICANTDIV = EADD.APPLICANTDIV ");
        stb.append("     AND EAD.EXAMNO = EADD.EXAMNO ");
        stb.append("   LEFT JOIN ENTEXAM_RECEPT_DAT ERD ");
        stb.append("     ON ERD.ENTEXAMYEAR = EAD.ENTEXAMYEAR ");
        stb.append("     AND ERD.APPLICANTDIV = EAD.APPLICANTDIV ");
        stb.append("     AND ERD.EXAMNO = EAD.EXAMNO ");
        stb.append("   LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT ERDD ");
        stb.append("     ON ERDD.ENTEXAMYEAR = ERD.ENTEXAMYEAR ");
        stb.append("     AND ERDD.APPLICANTDIV = ERD.APPLICANTDIV ");
        stb.append("     AND ERDD.TESTDIV = ERD.TESTDIV ");
        stb.append("     AND ERDD.EXAM_TYPE = ERD.EXAM_TYPE ");
        stb.append("     AND ERDD.RECEPTNO = ERD.RECEPTNO ");
        stb.append("     AND ERDD.SEQ = '006' ");
        stb.append(" WHERE ");
        stb.append("   EADD.ENTEXAMYEAR > '" + String.valueOf((Integer.parseInt(_param._year) - getpastyear)) + "' ");
        stb.append("   AND EAD.ENTEXAMYEAR <= '" + _param._year + "' ");
        stb.append("   AND EAD.PROCEDUREDIV = '1' ");
        stb.append("   AND EADD.REMARK2 <> '1' ");
        stb.append("   AND ( ");
        stb.append("       (EADD.REMARK1 = '" + prischcd + "' AND EADD.REMARK3 = '" + prischclscd + "') ");
        stb.append("       OR ");
        stb.append("       (EADD.REMARK4 = '" + prischcd + "' AND EADD.REMARK5 = '" + prischclscd + "') ");
        stb.append("       OR ");
        stb.append("       (EADD.REMARK6 = '" + prischcd + "' AND EADD.REMARK7 = '" + prischclscd + "') ");
        stb.append("   ) ");
        stb.append(" GROUP BY ");
        stb.append("   EAD.ENTEXAMYEAR, ");
        stb.append("   ERDD.REMARK1, ");
        stb.append("   ERDD.REMARK2 ");
        //上記をまとめる。
        stb.append(" ), MERGETBL AS ( ");
        stb.append(" SELECT ");
        stb.append("   CASE WHEN T1.YEAR IS NOT NULL THEN T1.YEAR ELSE T2.YEAR END AS YEAR, ");
        stb.append("   CASE WHEN T1.PETITIONDIV IS NOT NULL THEN T1.PETITIONDIV ELSE T2.PETITIONDIV END AS PETITIONDIV, ");
        stb.append("   CASE WHEN T1.COURSE_CD IS NOT NULL THEN  T1.COURSE_CD ELSE T2.COURSE_CD END COURSE_CD, ");
        stb.append("   T1.CNT1, ");
        stb.append("   T2.CNT2 ");
        stb.append(" FROM ");
        stb.append("   EXAMCNT T1 ");
        stb.append("   FULL OUTER JOIN ENTRYCNT T2 ");
        stb.append("     ON T1.YEAR = T2.YEAR ");
        stb.append("     AND T1.PETITIONDIV = T2.PETITIONDIV ");
        stb.append("     AND T1.COURSE_CD = T2.COURSE_CD ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   T1.YEAR, ");
        stb.append("   L006.NAME1 AS PETITIONNAME, ");
        stb.append("   LH58.NAME1 AS COURSENAME, ");
        stb.append("   T1.CNT1, ");
        stb.append("   T1.CNT2 ");
        stb.append(" FROM ");
        stb.append("   MERGETBL T1 ");
        stb.append("   LEFT JOIN NAME_MST L006 ");
        stb.append("     ON L006.NAMECD1 = 'L006' ");
        stb.append("     AND L006.NAMECD2 = T1.PETITIONDIV ");
        stb.append("   LEFT JOIN NAME_MST LH58 ");
        stb.append("     ON LH58.NAMECD1 = 'LH58' ");
        stb.append("     AND LH58.NAMECD2 = T1.COURSE_CD ");

        return stb.toString();
    }

    private String sqlgetFinSchExamCnt(final String fs_cd) {
        final StringBuffer stb = new StringBuffer();
        final int getpastyear = 4; //login年度を含めての5年なので、2018年なら、2014(=2018-4)～2018年が対象。
    	//受験者数をカウント
        stb.append(" WITH EXAMCNT AS ( ");
        stb.append(" SELECT ");
        stb.append("   EAD.ENTEXAMYEAR AS YEAR, ");
        stb.append("   ERDD.REMARK1 AS PETITIONDIV, ");
        stb.append("   ERDD.REMARK2 AS COURSE_CD, ");
        stb.append("   COUNT(EAD.EXAMNO) AS CNT1 ");
        stb.append(" FROM ");
        stb.append("   ENTEXAM_APPLICANTBASE_DAT EAD ");
        stb.append("   LEFT JOIN ENTEXAM_RECEPT_DAT ERD ");
        stb.append("     ON ERD.ENTEXAMYEAR = EAD.ENTEXAMYEAR ");
        stb.append("     AND ERD.APPLICANTDIV = EAD.APPLICANTDIV ");
        stb.append("     AND ERD.EXAMNO = EAD.EXAMNO ");
        stb.append("   LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT ERDD ");
        stb.append("     ON ERDD.ENTEXAMYEAR = ERD.ENTEXAMYEAR ");
        stb.append("     AND ERDD.APPLICANTDIV = ERD.APPLICANTDIV ");
        stb.append("     AND ERDD.TESTDIV = ERD.TESTDIV ");
        stb.append("     AND ERDD.EXAM_TYPE = ERD.EXAM_TYPE ");
        stb.append("     AND ERDD.RECEPTNO = ERD.RECEPTNO ");
        stb.append("     AND ERDD.SEQ = '006' ");
        stb.append(" WHERE ");
        stb.append("   EAD.ENTEXAMYEAR >= '" + String.valueOf((Integer.parseInt(_param._year) - getpastyear)) + "' ");
        stb.append("   AND EAD.ENTEXAMYEAR <= '" + _param._year + "' ");
        stb.append("   AND EAD.FS_CD = '"+fs_cd+"' ");
        stb.append(" GROUP BY ");
        stb.append("   EAD.ENTEXAMYEAR, ");
        stb.append("   ERDD.REMARK1, ");
        stb.append("   ERDD.REMARK2 ");
        //入学者数をカウント
        stb.append(" ), ENTRYCNT AS ( ");
        stb.append(" SELECT ");
        stb.append("   EAD.ENTEXAMYEAR AS YEAR, ");
        stb.append("   ERDD.REMARK1 AS PETITIONDIV, ");
        stb.append("   ERDD.REMARK2 AS COURSE_CD, ");
        stb.append("   COUNT(EAD.EXAMNO) AS CNT2 ");
        stb.append(" FROM ");
        stb.append("   ENTEXAM_APPLICANTBASE_DAT EAD ");
        stb.append("   LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT EADD ");
        stb.append("     ON EADD.ENTEXAMYEAR = EAD.ENTEXAMYEAR ");
        stb.append("     AND EADD.APPLICANTDIV = EAD.APPLICANTDIV ");
        stb.append("     AND EADD.EXAMNO = EAD.EXAMNO ");
        stb.append("     AND EADD.SEQ = '022' ");
        stb.append("   LEFT JOIN ENTEXAM_RECEPT_DAT ERD ");
        stb.append("     ON ERD.ENTEXAMYEAR = EAD.ENTEXAMYEAR ");
        stb.append("     AND ERD.APPLICANTDIV = EAD.APPLICANTDIV ");
        stb.append("     AND ERD.EXAMNO = EAD.EXAMNO ");
        stb.append("   LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT ERDD ");
        stb.append("     ON ERDD.ENTEXAMYEAR = ERD.ENTEXAMYEAR ");
        stb.append("     AND ERDD.APPLICANTDIV = ERD.APPLICANTDIV ");
        stb.append("     AND ERDD.TESTDIV = ERD.TESTDIV ");
        stb.append("     AND ERDD.EXAM_TYPE = ERD.EXAM_TYPE ");
        stb.append("     AND ERDD.RECEPTNO = ERD.RECEPTNO ");
        stb.append("     AND ERDD.SEQ = '006' ");
        stb.append(" WHERE ");
        stb.append("   EAD.ENTEXAMYEAR >= '" + String.valueOf((Integer.parseInt(_param._year) - getpastyear)) + "' ");
        stb.append("   AND EAD.ENTEXAMYEAR <= '" + _param._year + "' ");
        stb.append("   AND EAD.FS_CD = '" + fs_cd + "' ");
        stb.append("   AND EAD.PROCEDUREDIV = '1' "); //払い込みフラグがたっている事
        stb.append("   AND EADD.REMARK2 <> '1' "); //辞退フラグがたっていない事
        stb.append(" GROUP BY ");
        stb.append("   EAD.ENTEXAMYEAR, ");
        stb.append("   ERDD.REMARK1, ");
        stb.append("   ERDD.REMARK2 ");
        //受験者数と入学者数を1行にマージ
        stb.append(" ), MERGETBL AS ( ");
        stb.append(" SELECT ");
        stb.append("   CASE WHEN T1.YEAR IS NOT NULL THEN T1.YEAR ELSE T2.YEAR END AS YEAR, ");
        stb.append("   CASE WHEN T1.PETITIONDIV IS NOT NULL THEN T1.PETITIONDIV ELSE T2.PETITIONDIV END AS PETITIONDIV, ");
        stb.append("   CASE WHEN T1.COURSE_CD IS NOT NULL THEN  T1.COURSE_CD ELSE T2.COURSE_CD END COURSE_CD, ");
        stb.append("   T1.CNT1, ");
        stb.append("   T2.CNT2 ");
        stb.append(" FROM ");
        stb.append("   EXAMCNT T1 ");
        stb.append("   FULL OUTER JOIN ENTRYCNT T2 ");
        stb.append("     ON T1.YEAR = T2.YEAR ");
        stb.append("     AND T1.PETITIONDIV = T2.PETITIONDIV ");
        stb.append("     AND T1.COURSE_CD = T2.COURSE_CD ");
        stb.append(" ) ");
        //名称との紐づけ
        stb.append(" SELECT ");
        stb.append("   T1.YEAR, ");
        stb.append("   L006.NAME1 AS PETITIONNAME, ");
        stb.append("   LH58.NAME1 AS COURSENAME, ");
        stb.append("   T1.CNT1, ");
        stb.append("   T1.CNT2 ");
        stb.append(" FROM ");
        stb.append("   MERGETBL T1 ");
        stb.append("   LEFT JOIN NAME_MST L006 ");
        stb.append("     ON L006.NAMECD1 = 'L006' ");
        stb.append("     AND L006.NAMECD2 = T1.PETITIONDIV ");
        stb.append("   LEFT JOIN NAME_MST LH58 ");
        stb.append("     ON LH58.NAMECD1 = 'LH58' ");
        stb.append("     AND LH58.NAMECD2 = T1.COURSE_CD ");

        return stb.toString();
    }

    private String sqlgetPriSchStudentCnt(final String prischcd, final String prischclscode) {
        final StringBuffer stb = new StringBuffer();
        final int getpastyear = 1; //卒業生の2ヵ年は、loginが2018年だと当年度分を無視するので2016(=2018-1-1)。2年分なので、2016(=2018-1-1)～2017。
    	final String datecutwk = _param._loginDate.replace('/', '-');

        //在学生、卒業生、そのたの生徒を取得(LEAVEで判定。0：在校生、2:卒業生、1:その他
        //まずは、SCHREG_REGD_DATのデータで、対象塾に通っている生徒の最大学年、最大学期の当年度データを取得する。
        stb.append(" WITH MAXSCHREG AS ( ");
    	stb.append(" SELECT ");
    	stb.append("  T1.SCHREGNO, ");
    	stb.append("  max(T1.YEAR || T1.SEMESTER) MAX_YS ");
    	stb.append(" FROM ");
    	stb.append("  SCHREG_REGD_DAT T1 ");
    	stb.append("  LEFT JOIN SCHREG_BASE_MST SBM ");
    	stb.append("    ON SBM.SCHREGNO = T1.SCHREGNO ");
    	stb.append("  LEFT JOIN SCHREG_REGD_GDAT SRG ");
    	stb.append("    ON SRG.YEAR = T1.YEAR ");
    	stb.append("    AND SRG.GRADE = T1.GRADE ");
    	stb.append("  LEFT JOIN SCHREG_BASE_DETAIL_MST SBDM010");
    	stb.append("    ON SBDM010.SCHREGNO = T1.SCHREGNO ");
    	stb.append("    AND SBDM010.BASE_SEQ = '010' ");
    	stb.append(" WHERE ");
//    	stb.append("  T1.YEAR >= '" + String.valueOf((Integer.parseInt(_param._year) - getpastyear)) + "' ");
//    	stb.append("  AND T1.YEAR <= '" + _param._year + "' ");
    	stb.append("  T1.YEAR = '" + _param._year + "' ");
    	if (_param._isOsakatoin) {
        	stb.append("  AND SBM.PRISCHOOLCD = '" + prischcd + "'  ");
        	stb.append("  AND SBDM010.BASE_REMARK1 = '" + prischclscode + "'  ");
    	} else {
        	stb.append("  AND ( SBDM010.BASE_REMARK1 = '" + prischcd + "'  ");
        	stb.append("        OR SBDM010.BASE_REMARK3 = '" + prischcd + "'  ");
        	stb.append("        OR SBDM010.BASE_REMARK5 = '" + prischcd + "'  ");
        	stb.append("      )");
    	}
    	stb.append("  AND SRG.SCHOOL_KIND = 'H' ");
    	stb.append("  AND T1.GRADE = (SELECT MAX(SGO.GRADE) FROM SCHREG_REGD_DAT SGO WHERE SGO.SCHREGNO = T1.SCHREGNO) ");
    	stb.append(" GROUP BY ");
    	stb.append("  T1.SCHREGNO ");
    	//卒業生データGRD_REGD_DATで、出身中学対象生徒の最大学年、最大学期のデータを取得する。
    	stb.append("), MAXGRDREGD AS ( ");
    	stb.append(" SELECT ");
    	stb.append("  TG1.SCHREGNO, ");
    	stb.append("  max(TG1.YEAR || TG1.SEMESTER) MAX_YS ");
    	stb.append(" FROM ");
    	stb.append("  GRD_REGD_DAT TG1 ");
    	stb.append("  LEFT JOIN GRD_BASE_MST GBM ");
    	stb.append("    ON GBM.SCHREGNO = TG1.SCHREGNO ");
    	stb.append("  LEFT JOIN SCHREG_REGD_GDAT SRG ");
    	stb.append("    ON SRG.YEAR = TG1.YEAR ");
    	stb.append("    AND SRG.GRADE = TG1.GRADE ");
    	stb.append("  LEFT JOIN SCHREG_BASE_DETAIL_MST SBDM010");
    	stb.append("    ON SBDM010.SCHREGNO = TG1.SCHREGNO ");
    	stb.append("    AND SBDM010.BASE_SEQ = '010' ");
    	stb.append(" WHERE ");
    	stb.append("  TG1.YEAR >= '" + String.valueOf((Integer.parseInt(_param._year) - 1 - getpastyear)) + "' ");
    	stb.append("  AND TG1.YEAR <= '" + String.valueOf((Integer.parseInt(_param._year) - 1)) + "' ");
    	if (_param._isOsakatoin) {
        	stb.append("  AND GBM.PRISCHOOLCD = '" + prischcd + "'  ");
        	stb.append("  AND SBDM010.BASE_REMARK1 = '" + prischclscode + "'  ");
    	} else {
    		stb.append("  AND ( SBDM010.BASE_REMARK1 = '" + prischcd + "'  ");
    		stb.append("        OR SBDM010.BASE_REMARK3 = '" + prischcd + "'  ");
    		stb.append("        OR SBDM010.BASE_REMARK5 = '" + prischcd + "'  ");
    		stb.append("      )");
    	}
    	stb.append("  AND SRG.SCHOOL_KIND = 'H' ");
    	stb.append("  AND TG1.GRADE = (SELECT MAX(TGO.GRADE) FROM GRD_REGD_DAT TGO WHERE TGO.SCHREGNO = TG1.SCHREGNO) ");
    	stb.append(" GROUP BY ");
    	stb.append("  TG1.SCHREGNO ");
    	//副問い合わせがJOIN内ではできないので、事前に各生徒のAFT_GRAD_COURSE_DATの(最大SEQの)情報を取得する。
    	stb.append(" ), SUB_CALL_A AS ( ");
    	stb.append("   SELECT ");
    	stb.append("     TC1.SCHREGNO AS WWWW_, ");
    	stb.append("     AGCD1.* ");
    	stb.append("   FROM ");
    	stb.append("     MAXGRDREGD TC1 ");
    	stb.append("     LEFT JOIN AFT_GRAD_COURSE_DAT AGCD1 ");
    	stb.append("       ON AGCD1.SCHREGNO = TC1.SCHREGNO ");
    	stb.append("       AND AGCD1.YEAR = substr(TC1.MAX_YS, 1, 4) ");
    	stb.append("   WHERE ");
    	stb.append("     AGCD1.SEQ = (SELECT MAX(WD1.SEQ) AS SEQ FROM AFT_GRAD_COURSE_DAT WD1 LEFT JOIN SCHREG_REGD_DAT WT1 ON WD1.YEAR = WT1.YEAR AND WD1.SCHREGNO = WT1.SCHREGNO WHERE WD1.PLANSTAT = '1' AND WD1.SCHREGNO = TC1.SCHREGNO) ");
    	//副問い合わせがJOIN内ではできないので、事前に各生徒のAFT_GRAD_COURSE_DATの(最大SEQの)情報を取得する。
    	stb.append(" ), SUB_CALL_B AS ( ");
    	stb.append("   SELECT ");
    	stb.append("     TC2.SCHREGNO AS WWWW_, ");
    	stb.append("     AGCD2.* ");
    	stb.append("   FROM ");
    	stb.append("     MAXGRDREGD TC2 ");
    	stb.append("     LEFT JOIN AFT_GRAD_COURSE_DAT AGCD2 ");
    	stb.append("       ON AGCD2.SCHREGNO = TC2.SCHREGNO ");
    	stb.append("       AND AGCD2.YEAR = substr(TC2.MAX_YS, 1, 4) ");
    	stb.append("   WHERE ");
    	stb.append("     AGCD2.SEQ = (SELECT MAX(WD2.SEQ) AS SEQ FROM AFT_GRAD_COURSE_DAT WD2 LEFT JOIN SCHREG_REGD_DAT WT2 ON WD2.YEAR = WT2.YEAR AND WD2.SCHREGNO = WT2.SCHREGNO WHERE WD2.DECISION = '1' AND WD2.SCHREGNO = TC2.SCHREGNO) ");
    	stb.append("), MARGEREGD AS ( ");
    	stb.append(" SELECT B1.*,'0' AS LEAVE FROM MAXSCHREG B1 ");
    	stb.append(" UNION ");
    	stb.append(" SELECT B2.*,'2' AS LEAVE FROM MAXGRDREGD B2 ");
    	//卒業生、在校生データをまとめる(REGD_DAT)
    	stb.append(" ), MARGE_REGD_DAT AS (");
    	stb.append(" SELECT B3.*,'0' AS LEAVE FROM SCHREG_REGD_DAT B3 ");
    	stb.append(" UNION ");
    	stb.append(" SELECT B4.*,'2' AS LEAVE FROM GRD_REGD_DAT B4 ");
    	//卒業生、在校生データをまとめる(BASE_MST)
    	stb.append(" ), MARGE_BASE_MST AS (");
    	stb.append(" SELECT ");
    	stb.append("   B5.SCHREGNO,B5.NAME,B5.NAME_SHOW,B5.NAME_KANA,B5.NAME_ENG,B5.REAL_NAME,B5.REAL_NAME_KANA,B5.BIRTHDAY,");
    	stb.append("   B5.SEX,B5.BLOODTYPE,B5.BLOOD_RH,B5.HANDICAP,B5.NATIONALITY,B5.FINSCHOOLCD,B5.FINISH_DATE,B5.PRISCHOOLCD,");
    	stb.append("   B5.ENT_DATE,B5.ENT_DIV,B5.ENT_REASON,B5.ENT_SCHOOL,B5.ENT_ADDR,B5.ENT_ADDR2,B5.GRD_DATE,B5.GRD_DIV,");
    	stb.append("   B5.GRD_REASON,B5.GRD_SCHOOL,B5.GRD_ADDR,B5.GRD_ADDR2,B5.GRD_NO,B5.GRD_TERM,B5.REMARK1,B5.REMARK2,B5.REMARK3,");
    	stb.append("   '0' AS LEAVE");
    	stb.append(" FROM ");
    	stb.append("   SCHREG_BASE_MST B5 ");
    	stb.append(" UNION ");
    	stb.append(" SELECT ");
    	stb.append("   B6.SCHREGNO,B6.NAME,B6.NAME_SHOW,B6.NAME_KANA,B6.NAME_ENG,B6.REAL_NAME,B6.REAL_NAME_KANA,B6.BIRTHDAY,");
    	stb.append("   B6.SEX,B6.BLOODTYPE,B6.BLOOD_RH,B6.HANDICAP,B6.NATIONALITY,B6.FINSCHOOLCD,B6.FINISH_DATE,B6.PRISCHOOLCD,");
    	stb.append("   B6.ENT_DATE,B6.ENT_DIV,B6.ENT_REASON,B6.ENT_SCHOOL,B6.ENT_ADDR,B6.ENT_ADDR2,B6.GRD_DATE,B6.GRD_DIV,");
    	stb.append("   B6.GRD_REASON,B6.GRD_SCHOOL,B6.GRD_ADDR,B6.GRD_ADDR2,B6.GRD_NO,B6.GRD_TERM,B6.REMARK1,B6.REMARK2,B6.REMARK3,");
    	stb.append("   '2' AS LEAVE");
    	stb.append(" FROM ");
    	stb.append("   GRD_BASE_MST B6 ");
    	//上記と他テーブルを元に、在校/卒業/その他の共通的な情報を取得する。
    	//副問い合わせがJOIN内ではできないので、事前に各生徒のAFT_GRAD_COURSE_DATの(最大SEQの)情報を取得する。
    	stb.append(" ), BASEINFO AS ( ");
    	stb.append(" SELECT ");
    	stb.append("  SBM.ENT_DATE, ");
    	stb.append("  T1.YEAR, ");
    	stb.append("  T1.SEMESTER, ");
    	stb.append("  T2.SCHREGNO, ");
    	stb.append("  T1.GRADE, ");
    	stb.append("  SRG.SCHOOL_KIND, ");
    	stb.append("  SRG.GRADE_NAME1, ");
    	stb.append("  T1.HR_CLASS, ");
    	stb.append("  SRH.HR_NAME, ");
    	stb.append("  SRH.HR_NAMEABBV, ");
    	stb.append("  T1.ATTENDNO, ");
    	stb.append("  SBM.SEX AS SEXCD, ");
    	stb.append("  SBM.NAME, ");
    	stb.append("  SBDM010.BASE_REMARK1 AS PRISCHCD1, ");
    	stb.append("  SBDM010.BASE_REMARK2 AS PRISCHCLSCD1, ");
    	stb.append("  SBDM010.BASE_REMARK3 AS PRISCHCD2, ");
    	stb.append("  SBDM010.BASE_REMARK4 AS PRISCHCLSCD2, ");
    	stb.append("  SBDM010.BASE_REMARK5 AS PRISCHCD3, ");
    	stb.append("  SBDM010.BASE_REMARK6 AS PRISCHCLSCD3, ");
    	stb.append("  SBM.FINSCHOOLCD, ");
    	stb.append("  CASE WHEN T2.LEAVE = '2' THEN '2' ");
    	stb.append("       WHEN W3.SCHREGNO IS NOT NULL THEN '1' ");
    	stb.append("       WHEN W4.SCHREGNO IS NOT NULL THEN '1' ");
    	stb.append("       WHEN W5.SCHREGNO IS NOT NULL THEN '1' ");
    	stb.append("       ELSE '0' END AS LEAVE, ");
    	stb.append("  CASE WHEN W5.SCHREGNO IS NOT NULL THEN W5.TRANSFERPLACE ELSE '' END AS TRANSTO, ");
    	stb.append("  SBM.GRD_DIV, ");
    	stb.append("  SBM.GRD_DATE, ");
    	stb.append("  T1.COURSECODE, ");
    	stb.append("  AGCD1.STAT_CD AS STAT_CD1, ");
    	stb.append("  AGCD2.STAT_CD AS STAT_CD2 ");
    	stb.append(" FROM ");
    	stb.append("  MARGEREGD T2 ");
    	stb.append("  LEFT JOIN MARGE_REGD_DAT T1 ");
    	stb.append("    ON T1.SCHREGNO = T2.SCHREGNO ");
    	stb.append("    AND T1.YEAR || T1.SEMESTER = T2.MAX_YS ");
    	stb.append("  INNER JOIN V_SEMESTER_GRADE_MST W2 ON W2.YEAR = T1.YEAR AND W2.SEMESTER = T1.SEMESTER AND W2.GRADE = T1.GRADE ");
    	stb.append("  LEFT JOIN MARGE_BASE_MST SBM ");
    	stb.append("    ON SBM.SCHREGNO = T1.SCHREGNO ");
    	stb.append("  LEFT JOIN SCHREG_BASE_DETAIL_MST SBDM010");
    	stb.append("    ON SBDM010.SCHREGNO = SBM.SCHREGNO ");
    	stb.append("    AND SBDM010.BASE_SEQ = '010' ");
    	stb.append("  LEFT JOIN SCHREG_REGD_GDAT SRG ");
    	stb.append("    ON SRG.YEAR = T1.YEAR ");
    	stb.append("    AND SRG.GRADE = T1.GRADE ");
    	stb.append("  LEFT JOIN SCHREG_REGD_HDAT SRH ");
    	stb.append("    ON SRH.YEAR = T1.YEAR ");
    	stb.append("    AND SRH.SEMESTER = T1.SEMESTER ");
    	stb.append("    AND SRH.GRADE = T1.GRADE ");
    	stb.append("    AND SRH.HR_CLASS = T1.HR_CLASS ");
    	//               転学・退学者で、異動日が[学期終了日または出欠集計日の小さい日付]より小さい日付の場合
    	stb.append("  LEFT JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = T1.SCHREGNO ");
    	stb.append("                   AND W3.GRD_DIV IN('2','3') ");
    	stb.append("                   AND W3.GRD_DATE < CASE WHEN W2.EDATE < '" + _param._loginDate + "' THEN W2.EDATE ELSE '" + _param._loginDate + "' END ");
    	//               転入・編入者で、異動日が[学期終了日または出欠集計日の小さい日付]より大きい日付の場合
    	stb.append("  LEFT JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = T1.SCHREGNO ");
    	stb.append("                   AND W4.ENT_DIV IN('4','5') ");
    	stb.append("                   AND W4.ENT_DATE > CASE WHEN W2.EDATE < '" + _param._loginDate + "' THEN W2.EDATE ELSE '" + _param._loginDate + "' END ");
    	//               留学・休学者で、[学期終了日または出欠集計日の小さい日付]が留学・休学期間内にある場合
    	stb.append("  LEFT JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = T1.SCHREGNO ");
    	stb.append("                   AND W5.TRANSFERCD IN ('1','2') ");
    	stb.append("                   AND CASE WHEN W2.EDATE < '" + _param._loginDate + "' THEN W2.EDATE ELSE '" + _param._loginDate + "' END BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE ");
    	stb.append("  LEFT JOIN SUB_CALL_A AGCD1 ");
    	stb.append("    ON AGCD1.YEAR = T1.YEAR ");
    	stb.append("    AND AGCD1.SCHREGNO = T1.SCHREGNO ");
    	stb.append("  LEFT JOIN SUB_CALL_B AGCD2 ");
    	stb.append("    ON AGCD2.YEAR = T1.YEAR ");
    	stb.append("    AND AGCD2.SCHREGNO = T1.SCHREGNO ");
    	stb.append(" ORDER BY ");
    	stb.append("  T1.YEAR DESC, ");
    	stb.append("  T2.SCHREGNO ");
    	//クラブ情報を取得する
    	stb.append(" ) , CLUBINFO AS ( ");
    	stb.append(" SELECT ");
    	stb.append("   WKSCHD.SCHOOLCD, ");
    	stb.append("   WKSCHD.SCHOOL_KIND, ");
    	stb.append("   WKSCHD.SCHREGNO, ");
    	stb.append("   WKSCHD.CLUBCD, ");
    	stb.append("   row_number() over (partition by WKSCHD.SCHREGNO) as seq ");
    	stb.append(" FROM ");
    	stb.append("   BASEINFO WKBINF ");
    	stb.append("   LEFT JOIN SCHREG_CLUB_HIST_DAT WKSCHD ");
    	stb.append("    ON WKSCHD.SCHOOLCD = '000000000000' ");
    	stb.append("    AND WKSCHD.SCHOOL_KIND = WKBINF.SCHOOL_KIND ");
    	stb.append("    AND WKSCHD.SCHREGNO = WKBINF.SCHREGNO ");
    	stb.append("    AND WKSCHD.SDATE <= (CASE WHEN WKBINF.LEAVE = '0' THEN '"+_param._loginDate+"' ELSE WKBINF.GRD_DATE END) ");
    	stb.append("    AND (WKSCHD.EDATE >= (CASE WHEN WKBINF.LEAVE = '0' THEN '"+_param._loginDate+"' ELSE WKBINF.GRD_DATE END) OR WKSCHD.EDATE IS NULL) ");
    	//複数のクラブがあれば、1行に集約する
    	stb.append(" ), SCHCONCATCLUB AS ( ");
    	stb.append(" select ");
        stb.append(" C1A.SCHOOLCD, ");
        stb.append(" C1A.SCHOOL_KIND, ");
        stb.append(" C1A.SCHREGNO, ");
        stb.append(" C1A.CLUBCD, ");
        stb.append(" (CM1.CLUBNAME ");
        stb.append(" || (CASE WHEN CM2.CLUBNAME IS NULL THEN '' ELSE ', ' || CM2.CLUBNAME END) ");
        stb.append(" || (CASE WHEN CM3.CLUBNAME IS NULL THEN '' ELSE ', ' || CM3.CLUBNAME END) ");
        stb.append(" || (CASE WHEN CM4.CLUBNAME IS NULL THEN '' ELSE ', ' || CM4.CLUBNAME END)) AS CLUBNAME ");
        stb.append(" from ");
        stb.append(" CLUBINFO C1A ");
        stb.append("    LEFT JOIN CLUBINFO C1B ON C1B.SCHREGNO = C1A.SCHREGNO AND C1B.SCHOOLCD = C1A.SCHOOLCD AND C1B.SCHOOL_KIND = C1A.SCHOOL_KIND AND C1B.SEQ = 2 ");
        stb.append("    LEFT JOIN CLUBINFO C1C ON C1C.SCHREGNO = C1A.SCHREGNO AND C1C.SCHOOLCD = C1A.SCHOOLCD AND C1C.SCHOOL_KIND = C1A.SCHOOL_KIND AND C1C.SEQ = 3 ");
        stb.append("    LEFT JOIN CLUBINFO C1D ON C1D.SCHREGNO = C1A.SCHREGNO AND C1D.SCHOOLCD = C1A.SCHOOLCD AND C1D.SCHOOL_KIND = C1A.SCHOOL_KIND AND C1D.SEQ = 4 ");
        stb.append("    LEFT JOIN CLUB_MST CM1 ON CM1.SCHOOLCD = '000000000000' AND CM1.CLUBCD = C1A.CLUBCD ");
        stb.append("    LEFT JOIN CLUB_MST CM2 ON CM2.SCHOOLCD = '000000000000' AND CM2.CLUBCD = C1B.CLUBCD ");
        stb.append("    LEFT JOIN CLUB_MST CM3 ON CM3.SCHOOLCD = '000000000000' AND CM3.CLUBCD = C1C.CLUBCD ");
        stb.append("    LEFT JOIN CLUB_MST CM4 ON CM4.SCHOOLCD = '000000000000' AND CM4.CLUBCD = C1D.CLUBCD ");
        stb.append(" WHERE ");
        stb.append("   C1A.seq = 1 ");
    	stb.append(" ) ");
    	//個別の情報や名称などの追加情報を付加する
    	stb.append(" SELECT ");
    	stb.append("  TA.*, ");
    	stb.append("  Z002.ABBV1 AS SEX, ");
    	stb.append("  SSHD.SCHOLARSHIP AS SCHOLARSHIP, ");
    	stb.append("  A044.NAME1 AS SCHOLARSHIPNAME, ");
    	stb.append("  SCC.CLUBCD, ");
    	stb.append("  SCC.CLUBNAME, ");
    	stb.append("  CSM1.COURSECODENAME, ");
    	stb.append("  CSM1.COURSECODEABBV1, ");
    	stb.append("  CASE WHEN TA.LEAVE = '2' THEN CM1.SCHOOL_NAME ELSE '' END AS CM1_NAME, ");
    	stb.append("  CASE WHEN TA.LEAVE = '2' THEN CM2.SCHOOL_NAME ELSE '' END AS CM2_NAME, ");
    	stb.append("  PM1.PRISCHOOL_NAME AS PRISCHNAME1, ");
    	stb.append("  PCM1.PRISCHOOL_NAME AS PRISCHCLSNAME1, ");
    	stb.append("  PM2.PRISCHOOL_NAME AS PRISCHNAME2, ");
    	stb.append("  PCM2.PRISCHOOL_NAME AS PRISCHCLSNAME2, ");
    	stb.append("  PM3.PRISCHOOL_NAME AS PRISCHNAME3, ");
    	stb.append("  PCM3.PRISCHOOL_NAME AS PRISCHCLSNAME3, ");
    	stb.append("  FM.FINSCHOOL_NAME, ");
    	stb.append("  A003.NAME1 AS GRD_DIV_NAME");
    	stb.append(" FROM ");
    	stb.append("  BASEINFO TA ");
    	stb.append("  LEFT JOIN SCHREG_SCHOLARSHIP_HIST_DAT SSHD ");
    	stb.append("    ON SSHD.SCHOOLCD = '000000000000' ");
    	stb.append("    AND SSHD.SCHOOL_KIND = TA.SCHOOL_KIND ");
    	stb.append("    AND SSHD.SCHREGNO = TA.SCHREGNO ");
    	stb.append("    AND SSHD.FROM_DATE <= '" + datecutwk + "' ");
    	stb.append("    AND SSHD.TO_DATE >= '" + datecutwk + "' ");
    	stb.append("  LEFT JOIN SCHCONCATCLUB SCC ");
    	stb.append("    ON SCC.SCHOOLCD = '000000000000' ");
    	stb.append("    AND SCC.SCHOOL_KIND = TA.SCHOOL_KIND ");
    	stb.append("    AND SCC.SCHREGNO = TA.SCHREGNO ");
/*
    	stb.append("    AND SCHD.SDATE <= (CASE WHEN TA.LEAVE = '0' THEN '"+_param._loginDate+"' ELSE TA.GRD_DATE END) ");
    	stb.append("    AND (SCHD.EDATE >= (CASE WHEN TA.LEAVE = '0' THEN '"+_param._loginDate+"' ELSE TA.GRD_DATE END) OR SCHD.EDATE IS NULL) ");
*/
/*
    	stb.append("  LEFT JOIN CLUB_MST CM ");
    	stb.append("    ON CM.SCHOOLCD = '000000000000' ");
    	stb.append("    AND CM.SCHOOL_KIND = SCHD.SCHOOL_KIND ");
    	stb.append("    AND CM.CLUBCD = SCHD.CLUBCD ");
*/
    	stb.append("  LEFT JOIN NAME_MST A044 ");
    	stb.append("    ON A044.NAMECD1 = 'A044' ");
    	stb.append("    AND A044.NAMECD2 = SSHD.SCHOLARSHIP ");
    	stb.append("  LEFT JOIN COLLEGE_MST CM1 ");
    	stb.append("    ON CM1.SCHOOL_CD = TA.STAT_CD1 ");
    	stb.append("  LEFT JOIN COLLEGE_MST CM2 ");
    	stb.append("    ON CM2.SCHOOL_CD = TA.STAT_CD2 ");
    	stb.append("  LEFT JOIN NAME_MST Z002 ");
    	stb.append("    ON Z002.NAMECD1 = 'Z002' ");
    	stb.append("    AND Z002.NAMECD2 = TA.SEXCD ");
    	stb.append("  LEFT JOIN COURSECODE_MST CSM1 ");
    	stb.append("    ON CSM1.COURSECODE = TA.COURSECODE ");
    	stb.append("  LEFT JOIN PRISCHOOL_MST PM1 ");
    	stb.append("    ON PM1.PRISCHOOLCD = TA.PRISCHCD1 ");
    	stb.append("  LEFT JOIN PRISCHOOL_CLASS_MST PCM1 ");
    	stb.append("    ON PCM1.PRISCHOOLCD = TA.PRISCHCD1 ");
    	stb.append("    AND PCM1.PRISCHOOL_CLASS_CD = TA.PRISCHCLSCD1 ");
    	stb.append("  LEFT JOIN PRISCHOOL_MST PM2 ");
    	stb.append("    ON PM2.PRISCHOOLCD = TA.PRISCHCD2 ");
    	stb.append("  LEFT JOIN PRISCHOOL_CLASS_MST PCM2 ");
    	stb.append("    ON PCM2.PRISCHOOLCD = TA.PRISCHCD2 ");
    	stb.append("    AND PCM2.PRISCHOOL_CLASS_CD = TA.PRISCHCLSCD2 ");
    	stb.append("  LEFT JOIN PRISCHOOL_MST PM3 ");
    	stb.append("    ON PM3.PRISCHOOLCD = TA.PRISCHCD3 ");
    	stb.append("  LEFT JOIN PRISCHOOL_CLASS_MST PCM3 ");
    	stb.append("    ON PCM3.PRISCHOOLCD = TA.PRISCHCD3 ");
    	stb.append("    AND PCM3.PRISCHOOL_CLASS_CD = TA.PRISCHCLSCD3 ");
    	stb.append("  LEFT JOIN FINSCHOOL_MST FM ");
    	stb.append("    ON FM.FINSCHOOLCD = TA.FINSCHOOLCD");
    	stb.append("  LEFT JOIN NAME_MST A003 ");
    	stb.append("    ON A003.NAMECD1 = 'A003' ");
    	stb.append("    AND A003.NAMECD2 = TA.GRD_DIV");
    	stb.append(" ORDER BY ");
    	stb.append("  TA.GRADE, TA.HR_CLASS, TA.ATTENDNO ");

    	return stb.toString();
    }

    private String sqlgetFinSchStudentCnt(final String fs_cd) {
        final StringBuffer stb = new StringBuffer();
        final int getpastyear = 1; //卒業生の2ヵ年は、loginが2018年だと2016(=2018-1-1)。2年分なので、2016(=2018-1-1)～2017。
    	final String datecutwk = _param._loginDate.replace('/', '-');

        //在学生、卒業生、その他の生徒を取得(LEAVEで判定。0：在校生、2:卒業生、1:その他
        //まずは、在校生データSCHREG_REGD_DATで、出身中学対象生徒の最大学年、最大学期のデータを取得する。
        stb.append(" WITH MAXSCHREG AS ( ");
    	stb.append(" SELECT ");
    	stb.append("  T1.SCHREGNO, ");
    	stb.append("  max(T1.YEAR || T1.SEMESTER) MAX_YS ");
    	stb.append(" FROM ");
    	stb.append("  SCHREG_REGD_DAT T1 ");
    	stb.append("  LEFT JOIN SCHREG_BASE_MST SBM ");
    	stb.append("    ON SBM.SCHREGNO = T1.SCHREGNO ");
    	stb.append("  LEFT JOIN SCHREG_REGD_GDAT SRG ");
    	stb.append("    ON SRG.YEAR = T1.YEAR ");
    	stb.append("    AND SRG.GRADE = T1.GRADE ");
    	stb.append(" WHERE ");
//    	stb.append("  T1.YEAR >= '" + String.valueOf((Integer.parseInt(_param._year) - getpastyear)) + "' ");
//    	stb.append("  AND T1.YEAR <= '" + _param._year + "' ");
    	stb.append("  T1.YEAR = '" + _param._year + "' ");
    	stb.append("  AND SBM.FINSCHOOLCD = '" + fs_cd + "' ");
    	stb.append("  AND SRG.SCHOOL_KIND = 'H' ");
    	stb.append("  AND T1.GRADE = (SELECT MAX(SGO.GRADE) FROM SCHREG_REGD_DAT SGO WHERE SGO.SCHREGNO = T1.SCHREGNO) ");
    	stb.append(" GROUP BY ");
    	stb.append("  T1.SCHREGNO ");
    	//卒業生データGRD_REGD_DATで、出身中学対象生徒の最大学年、最大学期のデータを取得する。
    	stb.append("), MAXGRDREGD AS ( ");
    	stb.append(" SELECT ");
    	stb.append("  TG1.SCHREGNO, ");
    	stb.append("  max(TG1.YEAR || TG1.SEMESTER) MAX_YS ");
    	stb.append(" FROM ");
    	stb.append("  GRD_REGD_DAT TG1 ");
    	stb.append("  LEFT JOIN GRD_BASE_MST GBM ");
    	stb.append("    ON GBM.SCHREGNO = TG1.SCHREGNO ");
    	stb.append("  LEFT JOIN SCHREG_REGD_GDAT SRG ");
    	stb.append("    ON SRG.YEAR = TG1.YEAR ");
    	stb.append("    AND SRG.GRADE = TG1.GRADE ");
    	stb.append(" WHERE ");
    	stb.append("  TG1.YEAR >= '" + String.valueOf((Integer.parseInt(_param._year) - 1 - getpastyear)) + "' ");
    	stb.append("  AND TG1.YEAR <= '" + String.valueOf((Integer.parseInt(_param._year) - 1)) + "' ");
    	stb.append("  AND GBM.FINSCHOOLCD = '" + fs_cd + "' ");
    	stb.append("  AND SRG.SCHOOL_KIND = 'H' ");
    	stb.append("  AND TG1.GRADE = (SELECT MAX(TGO.GRADE) FROM GRD_REGD_DAT TGO WHERE TGO.SCHREGNO = TG1.SCHREGNO) ");
    	stb.append(" GROUP BY ");
    	stb.append("  TG1.SCHREGNO ");
    	//副問い合わせがJOIN内ではできないので、事前に各生徒のAFT_GRAD_COURSE_DATの(最大SEQの)情報を取得する。
    	stb.append(" ), SUB_CALL_A AS ( ");
    	stb.append("   SELECT ");
    	stb.append("     TC1.SCHREGNO AS WWWW_, ");
    	stb.append("     AGCD1.* ");
    	stb.append("   FROM ");
    	stb.append("     MAXGRDREGD TC1 ");
    	stb.append("     LEFT JOIN AFT_GRAD_COURSE_DAT AGCD1 ");
    	stb.append("       ON AGCD1.SCHREGNO = TC1.SCHREGNO ");
    	stb.append("       AND AGCD1.YEAR = substr(TC1.MAX_YS, 1, 4) ");
    	stb.append("   WHERE ");
    	stb.append("     AGCD1.SEQ = (SELECT MAX(WD1.SEQ) AS SEQ FROM AFT_GRAD_COURSE_DAT WD1 LEFT JOIN SCHREG_REGD_DAT WT1 ON WD1.YEAR = WT1.YEAR AND WD1.SCHREGNO = WT1.SCHREGNO WHERE WD1.PLANSTAT = '1' AND WD1.SCHREGNO = TC1.SCHREGNO) ");
    	//副問い合わせがJOIN内ではできないので、事前に各生徒のAFT_GRAD_COURSE_DATの(最大SEQの)情報を取得する。
    	stb.append(" ), SUB_CALL_B AS ( ");
    	stb.append("   SELECT ");
    	stb.append("     TC2.SCHREGNO AS WWWW_, ");
    	stb.append("     AGCD2.* ");
    	stb.append("   FROM ");
    	stb.append("     MAXGRDREGD TC2 ");
    	stb.append("     LEFT JOIN AFT_GRAD_COURSE_DAT AGCD2 ");
    	stb.append("       ON AGCD2.SCHREGNO = TC2.SCHREGNO ");
    	stb.append("       AND AGCD2.YEAR = substr(TC2.MAX_YS, 1, 4) ");
    	stb.append("   WHERE ");
    	stb.append("     AGCD2.SEQ = (SELECT MAX(WD2.SEQ) AS SEQ FROM AFT_GRAD_COURSE_DAT WD2 LEFT JOIN SCHREG_REGD_DAT WT2 ON WD2.YEAR = WT2.YEAR AND WD2.SCHREGNO = WT2.SCHREGNO WHERE WD2.DECISION = '1' AND WD2.SCHREGNO = TC2.SCHREGNO) ");
    	stb.append("), MARGEREGD AS ( ");
    	stb.append(" SELECT B1.*,'0' AS LEAVE FROM MAXSCHREG B1 ");
    	stb.append(" UNION ");
    	stb.append(" SELECT B2.*,'2' AS LEAVE FROM MAXGRDREGD B2 ");
    	//卒業生、在校生データをまとめる(REGD_DAT)
    	stb.append(" ), MARGE_REGD_DAT AS (");
    	stb.append(" SELECT B3.*,'0' AS LEAVE FROM SCHREG_REGD_DAT B3 ");
    	stb.append(" UNION ");
    	stb.append(" SELECT B4.*,'2' AS LEAVE FROM GRD_REGD_DAT B4 ");
    	//卒業生、在校生データをまとめる(BASE_MST)
    	stb.append(" ), MARGE_BASE_MST AS (");
    	stb.append(" SELECT ");
    	stb.append("   B5.SCHREGNO,B5.NAME,B5.NAME_SHOW,B5.NAME_KANA,B5.NAME_ENG,B5.REAL_NAME,B5.REAL_NAME_KANA,B5.BIRTHDAY,");
    	stb.append("   B5.SEX,B5.BLOODTYPE,B5.BLOOD_RH,B5.HANDICAP,B5.NATIONALITY,B5.FINSCHOOLCD,B5.FINISH_DATE,B5.PRISCHOOLCD,");
    	stb.append("   B5.ENT_DATE,B5.ENT_DIV,B5.ENT_REASON,B5.ENT_SCHOOL,B5.ENT_ADDR,B5.ENT_ADDR2,B5.GRD_DATE,B5.GRD_DIV,");
    	stb.append("   B5.GRD_REASON,B5.GRD_SCHOOL,B5.GRD_ADDR,B5.GRD_ADDR2,B5.GRD_NO,B5.GRD_TERM,B5.REMARK1,B5.REMARK2,B5.REMARK3,");
    	stb.append("   '0' AS LEAVE");
    	stb.append(" FROM ");
    	stb.append("   SCHREG_BASE_MST B5 ");
    	stb.append(" UNION ");
    	stb.append(" SELECT ");
    	stb.append("   B6.SCHREGNO,B6.NAME,B6.NAME_SHOW,B6.NAME_KANA,B6.NAME_ENG,B6.REAL_NAME,B6.REAL_NAME_KANA,B6.BIRTHDAY,");
    	stb.append("   B6.SEX,B6.BLOODTYPE,B6.BLOOD_RH,B6.HANDICAP,B6.NATIONALITY,B6.FINSCHOOLCD,B6.FINISH_DATE,B6.PRISCHOOLCD,");
    	stb.append("   B6.ENT_DATE,B6.ENT_DIV,B6.ENT_REASON,B6.ENT_SCHOOL,B6.ENT_ADDR,B6.ENT_ADDR2,B6.GRD_DATE,B6.GRD_DIV,");
    	stb.append("   B6.GRD_REASON,B6.GRD_SCHOOL,B6.GRD_ADDR,B6.GRD_ADDR2,B6.GRD_NO,B6.GRD_TERM,B6.REMARK1,B6.REMARK2,B6.REMARK3,");
    	stb.append("   '2' AS LEAVE");
    	stb.append(" FROM ");
    	stb.append("   GRD_BASE_MST B6 ");
    	//上記と他テーブルを元に、在校/卒業/その他の共通的な情報を取得する。
    	//副問い合わせがJOIN内ではできないので、事前に各生徒のAFT_GRAD_COURSE_DATの(最大SEQの)情報を取得する。
    	stb.append(" ), BASEINFO AS ( ");
    	stb.append(" SELECT ");
    	stb.append("  SBM.ENT_DATE, ");
    	stb.append("  T1.YEAR, ");
    	stb.append("  T1.SEMESTER, ");
    	stb.append("  T2.SCHREGNO, ");
    	stb.append("  T1.GRADE, ");
    	stb.append("  SRG.SCHOOL_KIND, ");
    	stb.append("  SRG.GRADE_NAME1, ");
    	stb.append("  T1.HR_CLASS, ");
    	stb.append("  SRH.HR_NAME, ");
    	stb.append("  SRH.HR_NAMEABBV, ");
    	stb.append("  T1.ATTENDNO, ");
    	stb.append("  SBM.SEX AS SEXCD, ");
    	stb.append("  SBM.NAME, ");
    	stb.append("  SBDM010.BASE_REMARK1 AS PRISCHCD1, ");
    	stb.append("  SBDM010.BASE_REMARK2 AS PRISCHCLSCD1, ");
    	stb.append("  SBDM010.BASE_REMARK3 AS PRISCHCD2, ");
    	stb.append("  SBDM010.BASE_REMARK4 AS PRISCHCLSCD2, ");
    	stb.append("  SBDM010.BASE_REMARK5 AS PRISCHCD3, ");
    	stb.append("  SBDM010.BASE_REMARK6 AS PRISCHCLSCD3, ");
    	stb.append("  SBM.FINSCHOOLCD, ");
    	stb.append("  CASE WHEN T2.LEAVE = '2' THEN '2' ");
    	stb.append("       WHEN W3.SCHREGNO IS NOT NULL THEN '1' ");
    	stb.append("       WHEN W4.SCHREGNO IS NOT NULL THEN '1' ");
    	stb.append("       WHEN W5.SCHREGNO IS NOT NULL THEN '1' ");
    	stb.append("       ELSE '0' END AS LEAVE, ");
    	stb.append("  CASE WHEN W5.SCHREGNO IS NOT NULL THEN W5.TRANSFERPLACE ELSE '' END AS TRANSTO, ");
    	stb.append("  SBM.GRD_DIV, ");
    	stb.append("  SBM.GRD_DATE, ");
    	stb.append("  T1.COURSECODE, ");
    	stb.append("  AGCD1.STAT_CD AS STAT_CD1, ");
    	stb.append("  AGCD2.STAT_CD AS STAT_CD2 ");
    	stb.append(" FROM ");
    	stb.append("  MARGEREGD T2 ");
    	stb.append("  LEFT JOIN MARGE_REGD_DAT T1 ");
    	stb.append("    ON T1.SCHREGNO = T2.SCHREGNO ");
    	stb.append("    AND T1.YEAR || T1.SEMESTER = T2.MAX_YS ");
    	stb.append("  INNER JOIN V_SEMESTER_GRADE_MST W2 ON W2.YEAR = T1.YEAR AND W2.SEMESTER = T1.SEMESTER AND W2.GRADE = T1.GRADE ");
    	stb.append("  LEFT JOIN MARGE_BASE_MST SBM ");
    	stb.append("    ON SBM.SCHREGNO = T1.SCHREGNO ");
    	stb.append("  LEFT JOIN SCHREG_BASE_DETAIL_MST SBDM010");
    	stb.append("    ON SBDM010.SCHREGNO = SBM.SCHREGNO ");
    	stb.append("    AND SBDM010.BASE_SEQ = '010' ");
    	stb.append("  LEFT JOIN SCHREG_REGD_GDAT SRG ");
    	stb.append("    ON SRG.YEAR = T1.YEAR ");
    	stb.append("    AND SRG.GRADE = T1.GRADE ");
    	stb.append("  LEFT JOIN SCHREG_REGD_HDAT SRH ");
    	stb.append("    ON SRH.YEAR = T1.YEAR ");
    	stb.append("    AND SRH.SEMESTER = T1.SEMESTER ");
    	stb.append("    AND SRH.GRADE = T1.GRADE ");
    	stb.append("    AND SRH.HR_CLASS = T1.HR_CLASS ");
    	//               転学・退学者で、異動日が[学期終了日または出欠集計日の小さい日付]より小さい日付の場合
    	stb.append("  LEFT JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = T1.SCHREGNO ");
    	stb.append("                   AND W3.GRD_DIV IN('2','3') ");
    	stb.append("                   AND W3.GRD_DATE < CASE WHEN W2.EDATE < '" + _param._loginDate + "' THEN W2.EDATE ELSE '" + _param._loginDate + "' END ");
    	//               転入・編入者で、異動日が[学期終了日または出欠集計日の小さい日付]より大きい日付の場合
    	stb.append("  LEFT JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = T1.SCHREGNO ");
    	stb.append("                   AND W4.ENT_DIV IN('4','5') ");
    	stb.append("                   AND W4.ENT_DATE > CASE WHEN W2.EDATE < '" + _param._loginDate + "' THEN W2.EDATE ELSE '" + _param._loginDate + "' END ");
    	//               留学・休学者で、[学期終了日または出欠集計日の小さい日付]が留学・休学期間内にある場合
    	stb.append("  LEFT JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = T1.SCHREGNO ");
    	stb.append("                   AND W5.TRANSFERCD IN ('1','2') ");
    	stb.append("                   AND CASE WHEN W2.EDATE < '" + _param._loginDate + "' THEN W2.EDATE ELSE '" + _param._loginDate + "' END BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE ");
    	stb.append("  LEFT JOIN SUB_CALL_A AGCD1 ");
    	stb.append("    ON AGCD1.YEAR = T1.YEAR ");
    	stb.append("    AND AGCD1.SCHREGNO = T1.SCHREGNO ");
    	stb.append("  LEFT JOIN SUB_CALL_B AGCD2 ");
    	stb.append("    ON AGCD2.YEAR = T1.YEAR ");
    	stb.append("    AND AGCD2.SCHREGNO = T1.SCHREGNO ");
    	stb.append(" ORDER BY ");
    	stb.append("  T1.YEAR DESC, ");
    	stb.append("  T2.SCHREGNO ");
    	//クラブ情報を取得する
    	stb.append(" ) , CLUBINFO AS ( ");
    	stb.append(" SELECT ");
    	stb.append("   WKSCHD.SCHOOLCD, ");
    	stb.append("   WKSCHD.SCHOOL_KIND, ");
    	stb.append("   WKSCHD.SCHREGNO, ");
    	stb.append("   WKSCHD.CLUBCD, ");
    	stb.append("   row_number() over (partition by WKSCHD.SCHREGNO) as seq ");
    	stb.append(" FROM ");
    	stb.append("   BASEINFO WKBINF ");
    	stb.append("   LEFT JOIN SCHREG_CLUB_HIST_DAT WKSCHD ");
    	stb.append("    ON WKSCHD.SCHOOLCD = '000000000000' ");
    	stb.append("    AND WKSCHD.SCHOOL_KIND = WKBINF.SCHOOL_KIND ");
    	stb.append("    AND WKSCHD.SCHREGNO = WKBINF.SCHREGNO ");
    	stb.append("    AND WKSCHD.SDATE <= (CASE WHEN WKBINF.LEAVE = '0' THEN '"+_param._loginDate+"' ELSE WKBINF.GRD_DATE END) ");
    	stb.append("    AND (WKSCHD.EDATE >= (CASE WHEN WKBINF.LEAVE = '0' THEN '"+_param._loginDate+"' ELSE WKBINF.GRD_DATE END) OR WKSCHD.EDATE IS NULL) ");
    	//複数のクラブがあれば、1行に集約する
    	stb.append(" ), SCHCONCATCLUB AS ( ");
    	stb.append(" select ");
        stb.append(" C1A.SCHOOLCD, ");
        stb.append(" C1A.SCHOOL_KIND, ");
        stb.append(" C1A.SCHREGNO, ");
        stb.append(" C1A.CLUBCD, ");
        stb.append(" (CM1.CLUBNAME ");
        stb.append(" || (CASE WHEN CM2.CLUBNAME IS NULL THEN '' ELSE ', ' || CM2.CLUBNAME END) ");
        stb.append(" || (CASE WHEN CM3.CLUBNAME IS NULL THEN '' ELSE ', ' || CM3.CLUBNAME END) ");
        stb.append(" || (CASE WHEN CM4.CLUBNAME IS NULL THEN '' ELSE ', ' || CM4.CLUBNAME END)) AS CLUBNAME ");
        stb.append(" from ");
        stb.append(" CLUBINFO C1A ");
        stb.append("    LEFT JOIN CLUBINFO C1B ON C1B.SCHREGNO = C1A.SCHREGNO AND C1B.SCHOOLCD = C1A.SCHOOLCD AND C1B.SCHOOL_KIND = C1A.SCHOOL_KIND AND C1B.SEQ = 2 ");
        stb.append("    LEFT JOIN CLUBINFO C1C ON C1C.SCHREGNO = C1A.SCHREGNO AND C1C.SCHOOLCD = C1A.SCHOOLCD AND C1C.SCHOOL_KIND = C1A.SCHOOL_KIND  AND C1C.SEQ = 3 ");
        stb.append("    LEFT JOIN CLUBINFO C1D ON C1D.SCHREGNO = C1A.SCHREGNO AND C1D.SCHOOLCD = C1A.SCHOOLCD AND C1D.SCHOOL_KIND = C1A.SCHOOL_KIND  AND C1D.SEQ = 4 ");
        stb.append("    LEFT JOIN CLUB_MST CM1 ON CM1.SCHOOLCD = '000000000000' AND CM1.CLUBCD = C1A.CLUBCD ");
        stb.append("    LEFT JOIN CLUB_MST CM2 ON CM2.SCHOOLCD = '000000000000' AND CM2.CLUBCD = C1B.CLUBCD ");
        stb.append("    LEFT JOIN CLUB_MST CM3 ON CM3.SCHOOLCD = '000000000000' AND CM3.CLUBCD = C1C.CLUBCD ");
        stb.append("    LEFT JOIN CLUB_MST CM4 ON CM4.SCHOOLCD = '000000000000' AND CM4.CLUBCD = C1D.CLUBCD ");
        stb.append(" WHERE ");
        stb.append("   C1A.seq = 1 ");
        stb.append(" ) ");
        //個別の情報や名称などの追加情報を付加する
    	stb.append(" SELECT ");
    	stb.append("  TA.*, ");
    	stb.append("  Z002.ABBV1 AS SEX, ");
    	stb.append("  SSHD.SCHOLARSHIP AS SCHOLARSHIP, ");
    	stb.append("  A044.NAME1 AS SCHOLARSHIPNAME, ");
    	stb.append("  SCC.CLUBCD, ");
    	stb.append("  SCC.CLUBNAME, ");
    	stb.append("  CSM1.COURSECODENAME, ");
    	stb.append("  CSM1.COURSECODEABBV1, ");
    	stb.append("  CASE WHEN TA.LEAVE = '2' THEN CM1.SCHOOL_NAME ELSE '' END AS CM1_NAME, ");
    	stb.append("  CASE WHEN TA.LEAVE = '2' THEN CM2.SCHOOL_NAME ELSE '' END AS CM2_NAME, ");
    	stb.append("  PM1.PRISCHOOL_NAME AS PRISCHNAME1, ");
    	stb.append("  PCM1.PRISCHOOL_NAME AS PRISCHCLSNAME1, ");
    	stb.append("  PM2.PRISCHOOL_NAME AS PRISCHNAME2, ");
    	stb.append("  PCM2.PRISCHOOL_NAME AS PRISCHCLSNAME2, ");
    	stb.append("  PM3.PRISCHOOL_NAME AS PRISCHNAME3, ");
    	stb.append("  PCM3.PRISCHOOL_NAME AS PRISCHCLSNAME3, ");
    	stb.append("  FM.FINSCHOOL_NAME, ");
    	stb.append("  A003.NAME1 AS GRD_DIV_NAME");
    	stb.append(" FROM ");
    	stb.append("  BASEINFO TA ");
    	stb.append("  LEFT JOIN SCHREG_SCHOLARSHIP_HIST_DAT SSHD ");
    	stb.append("    ON SSHD.SCHOOLCD = '000000000000' ");
    	stb.append("    AND SSHD.SCHOOL_KIND = TA.SCHOOL_KIND ");
    	stb.append("    AND SSHD.SCHREGNO = TA.SCHREGNO ");
    	stb.append("    AND SSHD.FROM_DATE <= '" + datecutwk + "' ");
    	stb.append("    AND SSHD.TO_DATE >= '" + datecutwk + "' ");
    	stb.append("  LEFT JOIN SCHCONCATCLUB SCC ");
    	stb.append("    ON SCC.SCHOOLCD = '000000000000' ");
    	stb.append("    AND SCC.SCHOOL_KIND = TA.SCHOOL_KIND ");
    	stb.append("    AND SCC.SCHREGNO = TA.SCHREGNO ");
/*
    	stb.append("    AND SCHD.SDATE <= (CASE WHEN TA.LEAVE = '0' THEN '"+_param._loginDate+"' ELSE TA.GRD_DATE END) ");
    	stb.append("    AND (SCHD.EDATE >= (CASE WHEN TA.LEAVE = '0' THEN '"+_param._loginDate+"' ELSE TA.GRD_DATE END) OR SCHD.EDATE IS NULL) ");
*/
/*
    	stb.append("  LEFT JOIN CLUB_MST CM ");
    	stb.append("    ON CM.SCHOOLCD = '000000000000' ");
    	stb.append("    AND CM.SCHOOL_KIND = SCHD.SCHOOL_KIND ");
    	stb.append("    AND CM.CLUBCD = SCHD.CLUBCD ");
*/
    	stb.append("  LEFT JOIN NAME_MST A044 ");
    	stb.append("    ON A044.NAMECD1 = 'A044' ");
    	stb.append("    AND A044.NAMECD2 = SSHD.SCHOLARSHIP ");
    	stb.append("  LEFT JOIN COLLEGE_MST CM1 ");
    	stb.append("    ON CM1.SCHOOL_CD = TA.STAT_CD1 ");
    	stb.append("  LEFT JOIN COLLEGE_MST CM2 ");
    	stb.append("    ON CM2.SCHOOL_CD = TA.STAT_CD2 ");
    	stb.append("  LEFT JOIN NAME_MST Z002 ");
    	stb.append("    ON Z002.NAMECD1 = 'Z002' ");
    	stb.append("    AND Z002.NAMECD2 = TA.SEXCD ");
    	stb.append("  LEFT JOIN COURSECODE_MST CSM1 ");
    	stb.append("    ON CSM1.COURSECODE = TA.COURSECODE ");
    	stb.append("  LEFT JOIN PRISCHOOL_MST PM1 ");
    	stb.append("    ON PM1.PRISCHOOLCD = TA.PRISCHCD1 ");
    	stb.append("  LEFT JOIN PRISCHOOL_CLASS_MST PCM1 ");
    	stb.append("    ON PCM1.PRISCHOOLCD = TA.PRISCHCD1 ");
    	stb.append("    AND PCM1.PRISCHOOL_CLASS_CD = TA.PRISCHCLSCD1 ");
    	stb.append("  LEFT JOIN PRISCHOOL_MST PM2 ");
    	stb.append("    ON PM2.PRISCHOOLCD = TA.PRISCHCD2 ");
    	stb.append("  LEFT JOIN PRISCHOOL_CLASS_MST PCM2 ");
    	stb.append("    ON PCM2.PRISCHOOLCD = TA.PRISCHCD2 ");
    	stb.append("    AND PCM2.PRISCHOOL_CLASS_CD = TA.PRISCHCLSCD2 ");
    	stb.append("  LEFT JOIN PRISCHOOL_MST PM3 ");
    	stb.append("    ON PM3.PRISCHOOLCD = TA.PRISCHCD3 ");
    	stb.append("  LEFT JOIN PRISCHOOL_CLASS_MST PCM3 ");
    	stb.append("    ON PCM3.PRISCHOOLCD = TA.PRISCHCD3 ");
    	stb.append("    AND PCM3.PRISCHOOL_CLASS_CD = TA.PRISCHCLSCD3 ");
    	stb.append("  LEFT JOIN FINSCHOOL_MST FM ");
    	stb.append("    ON FM.FINSCHOOLCD = TA.FINSCHOOLCD");
    	stb.append("  LEFT JOIN NAME_MST A003 ");
    	stb.append("    ON A003.NAMECD1 = 'A003' ");
    	stb.append("    AND A003.NAMECD2 = TA.GRD_DIV");
    	stb.append(" ORDER BY ");
    	stb.append("  TA.GRADE, TA.HR_CLASS, TA.ATTENDNO ");

    	return stb.toString();
    }

    private String sqlAttendInfo(final String schregnos) {
        final StringBuffer stb = new StringBuffer();
        //年度、学期ごとに集計する
        stb.append(" WITH SUMDAT AS (");
        stb.append(" SELECT ");
        stb.append("   ASD.YEAR, ");
        stb.append("   ASD.SEMESTER, ");
        stb.append("   ASD.SCHREGNO, ");
        stb.append("   SUM(ASD.SICK) AS SICK, ");
        stb.append("   SUM(ASD.OFFDAYS) AS OFFDAYS, ");
        stb.append("   SUM(ASD.NOTICE) AS NOTICE, ");
        stb.append("   SUM(ASD.NONOTICE) AS NONOTICE ");
        stb.append(" FROM ");
        stb.append("   ATTEND_SEMES_DAT ASD ");
        stb.append(" WHERE ");
        stb.append("   SCHREGNO IN " + schregnos + " ");
        stb.append(" GROUP BY ");
        stb.append("   ASD.YEAR, ");
        stb.append("   ASD.SEMESTER, ");
        stb.append("   ASD.SCHREGNO ");
        //留年した等の事を踏まえて、その生徒の各学年において、最新の年度を特定する。
        stb.append(" ),SCHREGINF AS ( ");
        stb.append(" SELECT ");
        stb.append("   SRD.SCHREGNO, ");
        stb.append("   SRD.GRADE, ");
        stb.append("   SRG.GRADE_CD, ");
        stb.append("   MAX(SRD.YEAR) AS YEAR ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_DAT SRD");
        stb.append("   LEFT JOIN SCHREG_REGD_GDAT SRG ");
        stb.append("     ON SRG.YEAR = SRD.YEAR ");
        stb.append("     AND SRG.GRADE = SRD.GRADE");
        stb.append(" WHERE ");
        stb.append("   SRD.SCHREGNO IN " + schregnos + " ");
        stb.append(" GROUP BY ");
        stb.append("   SRD.SCHREGNO, ");
        stb.append("   SRD.GRADE, ");
        stb.append("   SRG.GRADE_CD ");
        stb.append(" ) ");
        //上記を元に、欠席を算出する。
        stb.append(" SELECT ");
        stb.append("   SD.YEAR, ");
        stb.append("   SD.SEMESTER, ");
        stb.append("   SGI.GRADE, ");
        stb.append("   SGI.GRADE_CD, ");
        stb.append("   SD.SCHREGNO, ");
        stb.append("   CASE WHEN SM.SEM_OFFDAYS = '1' ");
        stb.append("        THEN VALUE(SD.SICK,0) + VALUE(SD.NOTICE,0) + VALUE(SD.NONOTICE,0) + VALUE(SD.OFFDAYS,0) ");
        stb.append("              ELSE VALUE(SD.SICK,0) + VALUE(SD.NOTICE,0) + VALUE(SD.NONOTICE,0) ");
        stb.append("              END AS ATTEND"); // 病欠＋事故欠（届・無）:
        stb.append(" FROM ");
        stb.append("   SUMDAT SD ");
        stb.append("   LEFT JOIN SCHOOL_MST SM ON SM.YEAR = SD.YEAR ");
        stb.append("   LEFT JOIN SCHREGINF SGI ON SGI.SCHREGNO = SD.SCHREGNO AND SGI.YEAR = SD.YEAR ");
        stb.append(" WHERE ");
        stb.append("   SD.SCHREGNO IN " + schregnos + " ");
        stb.append("   AND SGI.GRADE IS NOT NULL ");
        stb.append(" ORDER BY ");
        stb.append("   SD.SCHREGNO,SD.YEAR,SGI.GRADE,SD.SEMESTER");

        return stb.toString();
    }

    private String sqlScoreInfo(final String schregnos) {
        final StringBuffer stb = new StringBuffer();
        //対象生徒の有効データ(留年データを除くために、生徒に対する各学年の最新年度データ)を抽出する
        stb.append(" WITH MAXSCHREG AS ( ");
    	stb.append(" SELECT ");
    	stb.append("  T1.SCHREGNO, ");
    	stb.append("  T1.GRADE, ");
    	stb.append("  SRG.GRADE_CD, ");
    	stb.append("  T1.SEMESTER, ");
    	stb.append("  max(T1.YEAR) AS YEAR ");
    	stb.append(" FROM ");
    	stb.append("  SCHREG_REGD_DAT T1 ");
    	stb.append("  LEFT JOIN SCHREG_BASE_MST SBM ");
    	stb.append("    ON SBM.SCHREGNO = T1.SCHREGNO ");
    	stb.append("  LEFT JOIN SCHREG_REGD_GDAT SRG ");
    	stb.append("    ON SRG.YEAR = T1.YEAR ");
    	stb.append("    AND SRG.GRADE = T1.GRADE ");
    	stb.append(" WHERE ");
    	stb.append("  T1.SCHREGNO IN " + schregnos + " ");
    	stb.append("  AND SRG.SCHOOL_KIND = 'H' ");
    	stb.append(" GROUP BY ");
    	stb.append("  T1.SCHREGNO, ");
    	stb.append("  T1.GRADE, ");
    	stb.append("  SRG.GRADE_CD, ");
    	stb.append("  T1.SEMESTER ");
    	stb.append(" ) ");
    	//成績を紐づける。
        stb.append(" SELECT ");
        stb.append("   D1.YEAR, ");
        stb.append("   D1.SEMESTER, ");
        stb.append("   D1.GRADE, ");
    	stb.append("   D1.GRADE_CD, ");
        stb.append("   D1.SCHREGNO, ");
        stb.append("   VALUE(RRSD.AVG, 0) AS AVG ");
        stb.append(" FROM ");
        stb.append("   MAXSCHREG D1 ");
        stb.append("   LEFT JOIN RECORD_RANK_SDIV_DAT RRSD ");
        stb.append("     ON RRSD.YEAR = D1.YEAR ");
        stb.append("     AND RRSD.SEMESTER = D1.SEMESTER ");
        stb.append("     AND RRSD.SCHREGNO = D1.SCHREGNO ");
        stb.append("     AND SUBCLASSCD = '999999' ");
        stb.append("     AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '990008' ");
        stb.append(" ORDER BY ");
        stb.append("   D1.SCHREGNO, D1.GRADE, D1.SEMESTER ");

        return stb.toString();
    }

    private String sqlFinalScoreInfo(final String schregnos) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" WITH GBASEMST AS ( ");
        stb.append(" SELECT ");
        stb.append("   GBM.SCHREGNO, ");
        stb.append("   CASE WHEN MONTH(GBM.GRD_DATE) < 4 THEN YEAR(GBM.GRD_DATE)-1 ELSE YEAR(GBM.GRD_DATE) END AS GRD_YEAR ");
        stb.append(" FROM ");
        stb.append("   GRD_BASE_MST GBM ");
        stb.append(" WHERE ");
        stb.append("    GBM.SCHREGNO IN " + schregnos + " ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   GBM.SCHREGNO, ");
        stb.append("   GBM.GRD_YEAR, ");
        stb.append("   SSCD.AVG ");
        stb.append(" FROM ");
        stb.append("   GBASEMST GBM ");
        stb.append("   LEFT JOIN SCHREG_STUDYREC_RANK_CLASS_DAT SSCD ");
        stb.append("     ON SSCD.SCHREGNO = GBM.SCHREGNO ");
        stb.append("     AND SSCD.YEAR = CHAR(GBM.GRD_YEAR) ");
        stb.append("     AND SSCD.CLASS_DIV = '1' ");
        stb.append("     AND SSCD.CLASSCD = '00' ");
        stb.append("     AND SSCD.SCHOOL_KIND = 'H' ");
        stb.append("     AND SSCD.RANK_DIV = '0' ");

        return stb.toString();
    }

    private String sqlPriSchInviteInfo(final String priSchoolCd, final String priSchoolClassCd) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("   CASE WHEN MONTH(PVR.VISIT_DATE) < 4 ");
        stb.append("        THEN INT(YEAR(PVR.VISIT_DATE)) - 1 ");
        stb.append("        ELSE INT(YEAR(PVR.VISIT_DATE)) ");
        stb.append("   END                  AS VISITYEAR,  ");
        stb.append("   PVR.VISIT_DATE       AS VISITDATE,  ");
        stb.append("   PVR.STAFFCD          AS STAFFCD,    ");
        stb.append("   SM.STAFFNAME         AS VISITSTAFF, ");
        stb.append("   PVR.PRISCHOOL_STAFF  AS VISITOR,    ");
        stb.append("   PVR.COMMENT          AS COMMENT,    ");
        stb.append("   PVR.EXAM_STD_INFO    AS EXAMINFO,   ");
        stb.append("   PVR.REMARK           AS REMARK      ");
        stb.append(" FROM ");
        stb.append("   PRISCHOOL_YDAT PY ");
        stb.append("   LEFT JOIN PRISCHOOL_VISIT_RECORD_DAT PVR ");
        stb.append("     ON PVR.PRISCHOOLCD = PY.PRISCHOOLCD ");
        if(!"".equals(priSchoolClassCd)) {
            stb.append("     AND PVR.PRISCHOOL_CLASS_CD = '" + priSchoolClassCd + "' ");
        }
        stb.append("   LEFT JOIN STAFF_MST SM ");
        stb.append("     ON SM.STAFFCD = PVR.STAFFCD");
        stb.append(" WHERE ");
        stb.append("   PY.YEAR = '"+_param._year+"' ");
        stb.append("   AND PY.PRISCHOOLCD = '" + priSchoolCd + "' ");
        stb.append(" ORDER BY ");
        stb.append("   PY.PRISCHOOLCD, PVR.PRISCHOOL_CLASS_CD, PVR.VISIT_DATE ");
        stb.append(" FETCH FIRST 4 ROWS ONLY ");
        return stb.toString();
    }

    private class PrintInfo {
    	final schInfo _si; //塾・出身中学学校情報
    	final List _ei;    //試験情報リスト(examCntInfo)
    	final Map _s;      //生徒リスト(Student)
    	final Map _gs;     //卒業生リスト(Student)
    	final Map _os;     //退学転学休学者リスト(Student)
    	final Map _ji;     //塾訪問記録情報
    	PrintInfo (final schInfo si) {
    		_si = si;
    		_ei = new ArrayList();
    		_s = new LinkedMap();
    		_gs = new LinkedMap();
    		_os = new LinkedMap();
    		_ji = new LinkedMap();
    	}
    }

    private class schInfo {
    	final String _schcode;
    	final String _name;
    	final String _pricname;
    	final String _zipcd;
    	final String _addr1;
    	final String _addr2;
    	final String _telno;
    	final String _faxno;
    	final String _rmk;
    	final String _schclscd;
    	final String _schclsname;
    	final String _staffname;
    	schInfo(final String schcode,
    			final String name,
    			final String pricname,
    			final String zipcd,
    			final String addr1,
    			final String addr2,
    			final String telno,
    			final String faxno,
    			final String rmk,
    	    	final String schclscd,
    	    	final String schclsname,
    	    	final String staffname
                ) {
    		_schcode = schcode;
    		_name = name;
    		_pricname = pricname;
    		_zipcd = zipcd;
    		_addr1 = addr1;
    		_addr2 = addr2;
    		_telno = telno;
    		_faxno = faxno;
    		_rmk = rmk;
        	_schclscd = schclscd;
        	_schclsname = schclsname;
        	_staffname = staffname;
    	}

    }

    private class examCntInfo {
    	final String _year;
    	final String _petitionname;
    	final String _coursename;
    	final String _cnt1;
    	final String _cnt2;
    	examCntInfo(final String year,
                    final String petitionname,
                    final String coursename,
                    final String cnt1,
                    final String cnt2
                    ) {
    		_year = year;
    		_petitionname = petitionname;
    		_coursename = coursename;
    		_cnt1 = cnt1;
    		_cnt2 = cnt2;
    	}
    }

    private class Student {
    	final String _newestyear;
    	final String _schregno;
    	final String _entdate;
    	final String _infodiv; //0:在籍生、1:卒業生、2:その他
    	final String _course;
    	final String _grade;
    	final String _gradename;
    	final String _hrclass;
    	final String _clsname;
    	final String _attendno;
    	final String _sex;
    	final String _name;
    	final Map _RecordInfo;      //先頭から1年、2年、3年で各3学期分格納。卒業生はまとめて1つになる。
    	final Map _AttendInfo;      //先頭から1年、2年、3年で各3学期分格納。卒業生はまとめて1つになる。
    	final List _relativeschname; //先頭から最大3つ格納。
    	final String _tokutaikbn;
    	final String _clubname;
    	final String _finschname;
    	final String[] _finschnameArray;
    	final String[] _prischname;
    	//下記は、卒業生、その他のみ利用。
    	String _avg;     //成績(別口で設定する)
    	final String _movedate;//異動日
    	final String _movediv; //区分
    	final String _remark1; //進学先または
    	final String _remark2; //合格先または
    	final String _transto; //異動先など

    	Student(final String newestyear,
    		    final String schregno,
    		    final String entdate,
    		    final String infodiv,
    		    final String course,
    		    final String grade,
    		    final String gradename,
    		    final String hrclass,
    		    final String clsname,
    		    final String attendno,
    		    final String sex,
    		    final String name,
    		    final String tokutaikbn,
    		    final String clubname,
    		    final String finschname,
    		    final String[] finschnameArray,
    		    final String[] prischname,
    		    final String movedate,
    		    final String movediv,
    		    final String remark1,
    		    final String remark2,
    		    final String transto
               ) {
    		_newestyear = newestyear;
    		_schregno = schregno;
    		_entdate = entdate;
    		_infodiv = infodiv;
    		_course = course;
    		_grade = grade;
    		_gradename = gradename;
    		_hrclass = hrclass;
    		_clsname = clsname;
    		_attendno = attendno;
    		_sex = sex;
    		_name = name;
    		_tokutaikbn = tokutaikbn;
    		_clubname = clubname;
    		_movedate = movedate;
    		_movediv = movediv;
    		_remark1 = remark1;
    		_remark2 = remark2;
    		_finschname = finschname;
    		_finschnameArray = finschnameArray;
    		_prischname = prischname;
    		_RecordInfo = new HashMap();
    		_AttendInfo = new HashMap();
    		_relativeschname = new ArrayList();
    		_transto = transto;
    		_avg = "";
    	}
    	private String getEntYear() {
    		char delimstr = _entdate.indexOf('-') >= 0 ? '-' : '/';
    		String retstr;
    		String[] cutstr = StringUtils.split(_entdate, delimstr);
    		if (cutstr.length >= 2) {
    			if (Integer.parseInt(cutstr[1]) < 4) {
    			    retstr = String.valueOf((Integer.parseInt(cutstr[0]) - 1));
    			} else {
    			    retstr = cutstr[0];
    			}
    		} else {
    			retstr = _entdate;
    		}
    		return retstr;

    	}
    }

    private class AttendInfo {
    	private final String _grade;
    	private final String _semester;
    	private final String _attend;

    	AttendInfo(final String grade,
    			   final String semester,
    			   final String attend
    			) {
    		_grade = grade;
    		_semester = semester;
    		_attend = attend;
    	}
    }

    private class AvgScoreInfo {
    	private final String _grade;
    	private final String _semester;
    	private final String _avg;

    	AvgScoreInfo(final String grade,
    			   final String semester,
    			   final String avg
    			) {
    		_grade = grade;
    		_semester = semester;
    		_avg = avg;
    	}
    }

    private class PriSchVisitInfo {
    	private final String _visityear;
    	private final String _visitdate;
    	private final String _visitstaff;
    	private final String _visitor;
    	private final String _comment;
    	private final String _examinfo;
    	private final String _remark;
    	PriSchVisitInfo (final String visityear,
    			final String visitdate,
    			final String visitstaff,
    			final String visitor,
    			final String comment,
    			final String examinfo,
    			final String remark) {
    		_visityear = visityear;
    		_visitdate = visitdate;
    		_visitstaff = visitstaff;
    		_visitor = visitor;
    		_comment = comment;
    		_examinfo = examinfo;
    		_remark = remark;
    	}
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 69915 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
    	private String _formname;
    	private List _prtinflist;
        private final String _year;
        private final String _disppattern;
        private final String _loginDate;
        private final String[] _categoryselected;
        private final String _selectedInState;
        private final String _selectedInState2;
        private final Map _semestername;
        private final String _z010name1;
        private final boolean _isOsakatoin;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
        	_formname = "";
        	_prtinflist = new ArrayList();
            _year = request.getParameter("YEAR");
            _disppattern = request.getParameter("DISP_PATTERN");
            _loginDate = request.getParameter("CTRL_DATE");
            _categoryselected = request.getParameterValues("CATEGORY_SELECTED");

            String selectedInStatewk = "";
            String selectedInStatewk2 = "";
            String sep1 = "";
            String sep2 = "";
            for (int cnt = 0;cnt < _categoryselected.length;cnt++) {
            	if ("2".equals(_disppattern)) {//塾の選択肢は"-"付きと"-"無しがあるので切り分けが必要
            		//"-"無しは別変数に入れる
            		if (_categoryselected[cnt].indexOf('-') < 0) {
                        selectedInStatewk2 += sep2 + " '" + _categoryselected[cnt] + "'";
                    	sep2 = ",";
                    	continue;
            		}
            	}
            	selectedInStatewk += sep1 + " '" + _categoryselected[cnt] + "'";
            	sep1 = ",";
            }
            if (!"".equals(selectedInStatewk)) selectedInStatewk = "(" + selectedInStatewk + ")";
            if (!"".equals(selectedInStatewk2)) selectedInStatewk2 = "(" + selectedInStatewk2 + ")";
            _selectedInState = selectedInStatewk;
            _selectedInState2 = selectedInStatewk2;
            _semestername = loadSemesterMst(db2);
            _z010name1 = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' "));
            _isOsakatoin = "osakatoin".equals(_z010name1);
        }

        private Map loadSemesterMst(final DB2UDB db2) {
        	return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, "SELECT SEMESTER, SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' "), "SEMESTER", "SEMESTERNAME");
        }
    }
}

// eof

