package servletpack.KNJWD;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;


/**
 * 学習進度一覧（新課程）
 * @author nakasone
 * @version $Id: 4bc103711db870386ceeeb802ff4cb4fc39b2f49 $
 */
public class KNJWD740 {
    private static final String FORM_FILE = "KNJWD740.frm";
    private static final String NOTE_VALUE = "*・・・出向スクーリング生";
    private static final int PAGE_MAX_LINE = 50;
    private static final Integer ZERO = new Integer(0);

    private boolean _hasData;
    private Form _form;
    private Vrw32alp _svf;
    private DB2UDB db2;
    Param _param;

    private static final Log log = LogFactory.getLog(KNJWD740.class);
    
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        _param = new Param(db2, request);

        _form = new Form(FORM_FILE, response);

        db2 = null;
        try {
            final String dbName = request.getParameter("DBNAME");
            db2 = new DB2UDB(dbName, "db2inst1", "db2inst1", DB2UDB.TYPE2);
            if (openDb(db2)) {
                return;
            }
            _param.load(db2);
            
            printMain(db2);
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            _form.closeSvf();
            closeDb(db2);
        }
    }

    private void printMain(final DB2UDB db2) throws Exception {
    	boolean hasData = false;
    	
    	// 指示画面にて選択された所属分を繰り返す
        for (int i = 0; i < _param._grade.length; i++) {
            final String serchGrade = _param._grade[i];

            // 所属名を取得
            final String gradeName = getGredeName(db2, serchGrade);

    		final List subclasses = createSubclasses(db2, serchGrade);
            log.debug("科目の数=" + subclasses.size());
            loadOther(db2, subclasses);

            // 帳票出力のメソッド
    		hasData = outPutPrint(gradeName, subclasses);
    		if (hasData) {
    			_hasData = true;
    		}
        }
    }

    private void loadOther(final DB2UDB db2, final List subclasses) throws SQLException {
        loadReport(db2, subclasses);
        loadCommutingDat(db2, subclasses);

    }

    private void loadCommutingDat(final DB2UDB db2, final List subclasses) throws SQLException {
        for (final Iterator it = subclasses.iterator(); it.hasNext();) {
            final Subclass subclass = (Subclass) it.next();
            subclass.createRecCommutingDat(db2);
        }
    }

    private void loadReport(final DB2UDB db2, final List subclasses) throws SQLException {
        for (final Iterator it = subclasses.iterator(); it.hasNext();) {
            final Subclass subclass = (Subclass) it.next();
            db2.query(subclass.reportSql());
            final ResultSet rs = db2.getResultSet();
            while (rs.next()) {
                final Integer count1 = KNJServletUtils.getInteger(rs, "REPORT_COUNT1");
                if (null != count1) {
                    subclass._reportCount1 = count1;
                }

                final Integer count2 = KNJServletUtils.getInteger(rs, "REPORT_COUNT2");
                if (null != count2) {
                    subclass._reportCount2 = count2;
                }
            }
        }
    }

    private boolean outPutPrint(String gradeName, final List subclasses) throws Exception {
    	boolean dataflg = false;		// 対象データ存在フラグ
    	int gyo = 1;					// 現在ページ数の判断用（行）
    	int recCnt = 0;					// 合計レコード数
    	int staffNameCnt = 0;
    	int nameCnt = 0;
    	int classCnt = 0;
    	String keyStaffName = null;
    	String keyName = null;
    	String keyClass = null;
    	
    	String hrClass = "";
        _form._svf.VrAttribute( "SCHOOLNAME", "FF=1");	// 自動改ページ
        _form._svf.VrAttribute( "HR_CLASS", "FF=1");	// 自動改ページ

        for (Iterator it = subclasses.iterator(); it.hasNext();) {
            final Subclass subclass = (Subclass) it.next();
			if (recCnt > 0){
		        _form._svf.VrEndRecord();
			}

			//ページMAX行(50行) 又は 組コードブレイクの場合
			if ((gyo > PAGE_MAX_LINE) || !hrClass.equals(subclass._hrClass)) {
				// 教科名
				_form._svf.VrsOut("CLASS_NAME", subclass._classname);
				gyo = 1;
				_param.pagecnt++;
			}

			printHeader(gradeName, subclass);

			// 担任
			staffNameCnt = printStaff(staffNameCnt, keyStaffName, subclass);

			// 学籍番号
			_form._svf.VrsOut("SCHREGNO", subclass._schregno);

            // 教科名
            if (keyName == null || !keyName.equals(subclass._name)) {
				if (nameCnt == 9) {
					nameCnt = 0;
				}
				++nameCnt;

				classCnt = 1;
				_form._svf.VrsOut("CLASS_NAME", subclass._classname);
			} else if(keyClass == null || !keyClass.equals(subclass._classcd)){ 
				if (classCnt == 9) {
					classCnt = 0;
				}
				++classCnt;
				_form._svf.VrsOut("CLASS_NAME", subclass._classname);
			}

            // 生徒氏名
			_form._svf.VrsOut("FLG2", String.valueOf(nameCnt));
			_form._svf.VrsOut(setFormatArea("NAME", subclass._name, 10, "1_1", "1_2") , subclass._name);

			// 教科名
			_form._svf.VrsOut("FLG3", String.valueOf(nameCnt) + String.valueOf(classCnt));

            // 科目名
			_form._svf.VrsOut(setFormatArea("SUBCLASSNAME", subclass._subclassName, 10, "1_1", "1_2"), subclass._subclassName);

            // 基準単位
            if (null != subclass._credits) {
                _form._svf.VrsOut("CREDITS", subclass._credits.toString());
            }

            // 平常点
            if (null != subclass._usualScore) {
                _form._svf.VrsOut("USUAL_SCORE", subclass._usualScore.toString());
            }

            // スクーリング
            printSchooling(subclass);

			// レポート
            printReport(subclass);

            // テスト
	        printTest(subclass);

			// 備考
			_form._svf.VrsOut("NOTE", NOTE_VALUE);

            log.debug(subclass + ":" + subclass._subclassName + ":schooling=" + subclass._schoolingSeq + "(" + subclass._commutingDats + ")");

			++recCnt;
			++gyo;
			hrClass = subclass._hrClass;
	    	keyStaffName = subclass._staffName;
	    	keyName = subclass._name;
	    	keyClass = subclass._classcd;
			dataflg = true;
        }

        // 最終レコードを出力
        if (dataflg) {
	        _form._svf.VrEndRecord();
		}
		
		return dataflg;
    }

    private void printTest(final Subclass subclass) {
        if (!subclass._testFlg) {
            if (null != subclass._score9) {
                _form._svf.VrsOut("SCORE1", subclass._score9.toString());
            }
            if (null != subclass._score3) {
                _form._svf.VrsOut("SCORE2", subclass._score3.toString());
            }
        } else {
            // 科目詳細マスタのテスト実施フラグがNULLの場合、得点エリアにハイフンを設定
        	if (null == subclass._score3 && null == subclass._score9) {
                ;
        	} else {
            	if (null == subclass._score3) {
        			_form._svf.VrsOut("SCORE1", "-");
            	} else if (null == subclass._score9) {
        			_form._svf.VrsOut("SCORE2", "-");
            	} else {
        			_form._svf.VrsOut("SCORE1", "-");
        			_form._svf.VrsOut("SCORE2", "-");
            	}
        	}
        }
    }

    private void printReport(final Subclass subclass) {
        final String zan;
        if (null != subclass._reportSeq) {
            _form._svf.VrsOut("REPORT_SEQ", subclass._reportSeq.toString()); // 基準
            zan = String.valueOf(subclass.getReportCount());
        } else {
            zan = "0";
        }
        _form._svf.VrsOut("COUNT", zan);    // 残数
    }

    private void printSchooling(final Subclass subclass) {
        if (null != subclass._schoolingSeq) {
            _form._svf.VrsOut("SCHOOLING_SEQ", subclass._schoolingSeq.toString());
        }

        // 通学区分＝1:通学生以外の場合
        final double mensetuTime;
        if (!"1".equals(_param._commutingDivMapString(subclass))) {
            // 実績面接時間を算出(スクーリング回数×(割合÷10))
            mensetuTime = subclass.getSchoolingSeq() * ((double) subclass.getRate() / 10);
        } else if (null != subclass._recordCount) {
            mensetuTime = subclass._recordCount.doubleValue();
        } else {
            mensetuTime = 0;
        }

        // 実績メディア時間を算出(修得時間の合計(分)÷50(分))
        final double mediaTime = (double) subclass._getValue.intValue() / 50;

        // スクーリング(残)を算出(スクーリング回数-(実績面接時間+実績メディア時間+通学実績))
        final double zan = subclass.getSchoolingSeq() - (mensetuTime + mediaTime + subclass._commutingDats.intValue());
        if (zan < 0) {
            _form._svf.VrsOut("TIME_LEFT", "0");
        } else {
            final BigDecimal zanObj = new BigDecimal(String.valueOf(zan));
            final String timeLeft = getRateValue(zanObj.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            _form._svf.VrsOut("TIME_LEFT", timeLeft);
        }
    }

    private int printStaff(int staffNameCnt, final String keyStaffName, final Subclass subclass) {
        if (keyStaffName == null || !keyStaffName.equals(subclass._staffName)) {
        	++staffNameCnt;
        	if (staffNameCnt == 10) {
        		staffNameCnt = 0;
        	}
        }
        _form._svf.VrsOut("FLG1", String.valueOf(staffNameCnt));
        _form._svf.VrsOut(setFormatArea("STAFFNAME", subclass._staffName, 10, "1_1", "1_2") , subclass._staffName);
        return staffNameCnt;
    }

    private void printHeader(final String gradeName, final Subclass subclass) {
        _form._svf.VrsOut("PAGE"	,	String.valueOf(_param.pagecnt));	// 現在項
        _form._svf.VrsOut("NENDO"	,	_param._nendo);		// 対象年度
        _form._svf.VrsOut("DATE"	,	_param._date);		// 作成日
        _form._svf.VrsOut("SCHOOLNAME"	,gradeName);		// 所属名：自動改ページ用項目
        _form._svf.VrsOut("HR_CLASS"	,subclass._hrClass);	// 組(非表示)：自動改ページ用項目
    }

    private List createSubclasses(final DB2UDB db2, String serchGrade) throws SQLException {
        final List rtn = new ArrayList();
        final String sql = getSubclassSql(serchGrade);
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final Subclass subclass = new Subclass(
                		rs.getString("GRADE"),
                		rs.getString("HR_CLASS"),
                		rs.getString("YEAR"),
                		rs.getString("SCHREGNO"),
                        nvlT(rs.getString("COURSE_DIV")),
                        nvlT(rs.getString("STUDENT_DIV")),
                		rs.getString("CLASSCD"),
                		rs.getString("CURRICULUM_CD"),
                		rs.getString("SUBCLASSCD"),
                		nvlT(rs.getString("NAME")),
                		nvlT(rs.getString("STAFFNAME")),
                		nvlT(rs.getString("CLASSNAME")),
                		nvlT(rs.getString("SUBCLASSNAME")),
                        KNJServletUtils.getInteger(rs, "CREDITS"),
                        KNJServletUtils.getInteger(rs, "SCHOOLING_SEQ"),
                        KNJServletUtils.getInteger(rs, "REPORT_SEQ"),
                		nvlT(rs.getString("TEST_FLG")),
                        KNJServletUtils.getInteger(rs, "USUAL_SCORE"),
                        KNJServletUtils.getInteger(rs, "GET_VALUE"),
                        KNJServletUtils.getInteger(rs, "SCORE3"),
                        KNJServletUtils.getInteger(rs, "SCORE9"),
                        KNJServletUtils.getInteger(rs, "RATE"),
                        KNJServletUtils.getInteger(rs, "RECORD_COUNT")
                );
                rtn.add(subclass);
//                log.debug(subclass._name + "/" + subclass._subclassName + "/" + subclass._credits);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }

        return rtn;
    }

    private String getSubclassSql(String serchGrade){
    	StringBuffer stb = new StringBuffer();

		stb.append(" WITH MAIN_T AS (");
		stb.append("     select");
		stb.append("         W1.GRADE,");
		stb.append("         W1.HR_CLASS,");
		stb.append("         W1.YEAR,");
		stb.append("         W1.SCHREGNO,");
        stb.append("         W1.COURSE_DIV,");
        stb.append("         W1.STUDENT_DIV,");
		stb.append("         W2.CLASSCD,");
		stb.append("         W2.CURRICULUM_CD,");
		stb.append("         W2.SUBCLASSCD,");
		stb.append("         W3.NAME,");
		stb.append("         W5.STAFFNAME,");
		stb.append("         W6.CLASSNAME,");
		stb.append("         W7.SUBCLASSNAME,");
		stb.append("         W8.CREDITS,");
		stb.append("         W8.SCHOOLING_SEQ,");
		stb.append("         W8.REPORT_SEQ,");
		stb.append("         W8.TEST_FLG,");
		stb.append("         W9.USUAL_SCORE");
		// 学籍在籍データ
		stb.append("     from SCHREG_REGD_DAT W1");
		// 履修登録データ
		stb.append("     inner join COMP_REGIST_DAT W2 on");
		stb.append("         W1.YEAR     = W2.YEAR and");
		stb.append("         W1.SCHREGNO = W2.SCHREGNO");
		// 学籍基礎マスタ
		stb.append("     inner join SCHREG_BASE_MST W3 on");
		stb.append("         W2.SCHREGNO = W3.SCHREGNO");
		// 学籍在籍ヘッダデータ
		stb.append("     inner join SCHREG_REGD_HDAT W4 on");
		stb.append("         W1.YEAR     = W4.YEAR and");
		stb.append("         W1.SEMESTER = W4.SEMESTER and");
		stb.append("         W1.GRADE    = W4.GRADE and");
		stb.append("         W1.HR_CLASS = W4.HR_CLASS");
		// 職員マスタ
		stb.append("     left join STAFF_MST W5 on");
		stb.append("         W4.TR_CD1   = W5.STAFFCD");
		// 教科マスタ
		stb.append("     inner join CLASS_MST W6 on");
		stb.append("         W2.CLASSCD   = W6.CLASSCD");
		// 科目マスタ
		stb.append("     inner join SUBCLASS_MST W7 on");
		stb.append("         W2.CLASSCD       = W7.CLASSCD and");
		stb.append("         W2.CURRICULUM_CD = W7.CURRICULUM_CD and");
		stb.append("         W2.SUBCLASSCD    = W7.SUBCLASSCD");
		// 科目詳細マスタ
		stb.append("     left join SUBCLASS_DETAILS_MST W8 on");
		stb.append("         W2.YEAR          = W8.YEAR and");
		stb.append("         W2.CLASSCD       = W8.CLASSCD and");
		stb.append("         W2.CURRICULUM_CD = W8.CURRICULUM_CD and");
		stb.append("         W2.SUBCLASSCD    = W8.SUBCLASSCD");
		// 単位認定結果データ
		stb.append("     left join REC_CREDIT_ADMITS W9 on");
		stb.append("         W2.YEAR          = W9.YEAR and");
		stb.append("         W2.CLASSCD       = W9.CLASSCD and");
		stb.append("         W2.CURRICULUM_CD = W9.CURRICULUM_CD and");
		stb.append("         W2.SUBCLASSCD    = W9.SUBCLASSCD and");
		stb.append("         W2.SCHREGNO      = W9.SCHREGNO and");
        stb.append("         '1'             <> W9.COMBINED_FLG and");
        stb.append("         '1'              = W9.ADMITS_FLG");
		stb.append("     where");
		stb.append("         W1.GRADE = '" + serchGrade + "' and");
		stb.append("         W1.YEAR = '" + _param._year + "' and");
		stb.append("         W1.SEMESTER = '" + _param._semester + "'");
		stb.append("     order by W2.CLASSCD, W2.CURRICULUM_CD, W2.SUBCLASSCD, W1.SCHREGNO");
		stb.append(" )");
    	// 通信スクーリング割合データ取得
		stb.append(" ,REC_SCHOOLING_RATE_DAT_T AS (");
		stb.append("     select");
		stb.append("         T1.YEAR,");
		stb.append("         T1.CLASSCD,");
		stb.append("         T1.CURRICULUM_CD,");
		stb.append("         T1.SUBCLASSCD,");
		stb.append("         T1.SCHREGNO,");
		stb.append("         SUM(T1.RATE) AS RATE");
		stb.append("     from (");
		stb.append("         select");
		stb.append("             W1.YEAR,");
		stb.append("             W1.CLASSCD,");
		stb.append("             W1.CURRICULUM_CD,");
		stb.append("             W1.SUBCLASSCD,");
		stb.append("             W1.SCHREGNO,");
		stb.append("             W2.RATE");
		stb.append("         from MAIN_T W1");
		stb.append("         left join REC_SCHOOLING_RATE_DAT W2 on");
		stb.append("             W1.YEAR          = W2.YEAR and");
		stb.append("             W1.CLASSCD       = W2.CLASSCD and");
		stb.append("             W1.CURRICULUM_CD = W2.CURRICULUM_CD and");
		stb.append("             W1.SUBCLASSCD    = W2.SUBCLASSCD and");
		stb.append("             W1.SCHREGNO      = W2.SCHREGNO and");
		stb.append("             W2.COMMITED_S is not null and");
		stb.append("             W2.COMMITED_E is not null");
		stb.append("         inner join SCHOOLING_TYPE_MST W3 on");
		stb.append("             W2.SCHOOLING_TYPE = W3.SCHOOLING_TYPE and");
		stb.append("             W3.SCHOOLING_DIV  = '01'");
		stb.append("     ) T1");
		stb.append("     group by T1.YEAR, T1.CLASSCD, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO ");
		stb.append("     order by T1.YEAR, T1.CLASSCD, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO");
		stb.append(" )");
    	// 通信スクーリング実績データ取得
		stb.append(" ,REC_SCHOOLING_DAT_T AS (");
		stb.append("     select");
		stb.append("         T1.YEAR,");
		stb.append("         T1.CLASSCD,");
		stb.append("         T1.CURRICULUM_CD,");
		stb.append("         T1.SUBCLASSCD,");
		stb.append("         T1.SCHREGNO,");
		stb.append("         SUM(T1.GET_VALUE) AS GET_VALUE");
		stb.append("     from (");
		stb.append("         select");
		stb.append("             W1.YEAR,");
		stb.append("             W1.CLASSCD,");
		stb.append("             W1.CURRICULUM_CD,");
		stb.append("             W1.SUBCLASSCD,");
		stb.append("             W1.SCHREGNO,");
		stb.append("             W2.GET_VALUE");
		stb.append("         from MAIN_T W1");
		stb.append("         left join REC_SCHOOLING_DAT W2 on");
		stb.append("             W1.YEAR          = W2.YEAR and");
		stb.append("             W1.CLASSCD       = W2.CLASSCD and");
		stb.append("             W1.CURRICULUM_CD = W2.CURRICULUM_CD and");
		stb.append("             W1.SUBCLASSCD    = W2.SUBCLASSCD and");
		stb.append("             W1.SCHREGNO      = W2.SCHREGNO");
		stb.append("         inner join SCHOOLING_TYPE_MST W3 on");
		stb.append("             W2.SCHOOLING_TYPE = W3.SCHOOLING_TYPE and");
        stb.append("             W3.SCHOOLING_DIV  = '02'");
		stb.append("     ) T1");
		stb.append("     group by T1.YEAR, T1.CLASSCD, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO ");
		stb.append("     order by T1.YEAR, T1.CLASSCD, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO");
		stb.append(" )");
        // 通信スクーリング実績データ取得(通学生の場合の面接時間を取得)
        stb.append(" ,REC_SCHOOLING_DAT_T2 AS (");
        stb.append("     select");
        stb.append("         W1.YEAR,");
        stb.append("         W1.CLASSCD,");
        stb.append("         W1.CURRICULUM_CD,");
        stb.append("         W1.SUBCLASSCD,");
        stb.append("         W1.SCHREGNO,");
        stb.append("         COUNT(*) AS RECORD_COUNT");
        stb.append("     from MAIN_T W1");
        stb.append("     inner join REC_SCHOOLING_DAT W2 on");
        stb.append("         W1.YEAR          = W2.YEAR and");
        stb.append("         W1.CLASSCD       = W2.CLASSCD and");
        stb.append("         W1.CURRICULUM_CD = W2.CURRICULUM_CD and");
        stb.append("         W1.SUBCLASSCD    = W2.SUBCLASSCD and");
        stb.append("         W1.SCHREGNO      = W2.SCHREGNO");
        stb.append("     inner join SCHOOLING_TYPE_MST W3 on");
        stb.append("         W2.SCHOOLING_TYPE = W3.SCHOOLING_TYPE and");
        stb.append("         W3.SCHOOLING_DIV  = '01'");
        stb.append("     group by W1.YEAR, W1.CLASSCD, W1.CURRICULUM_CD, W1.SUBCLASSCD, W1.SCHREGNO ");
        stb.append("     order by W1.YEAR, W1.CLASSCD, W1.CURRICULUM_CD, W1.SUBCLASSCD, W1.SCHREGNO");
        stb.append(" )");
    	// テスト実績データ取得
		stb.append(" ,REC_TEST_DAT_T AS(");
		stb.append("     select");
		stb.append("         T1.YEAR,");
		stb.append("         T1.CLASSCD,");
		stb.append("         T1.CURRICULUM_CD,");
		stb.append("         T1.SUBCLASSCD,");
		stb.append("         T1.SCHREGNO,");
		stb.append("         SUM(T1.SCORE3) AS SCORE3,");
		stb.append("         SUM(T1.SCORE9) AS SCORE9");
		stb.append("     from (");
		stb.append("         select");
		stb.append("             W1.YEAR,");
		stb.append("             W1.CLASSCD,");
		stb.append("             W1.CURRICULUM_CD,");
		stb.append("             W1.SUBCLASSCD,");
		stb.append("             W1.SCHREGNO,");
		stb.append("             CASE WHEN W2.MONTH = '03' THEN W2.SCORE ELSE NULL END AS SCORE3,");
		stb.append("             CASE WHEN W2.MONTH = '09' THEN W2.SCORE ELSE NULL END AS SCORE9");
		stb.append("         from MAIN_T W1");
		stb.append("         left join REC_TEST_DAT W2 on");
		stb.append("             W1.YEAR          = W2.YEAR and");
		stb.append("             W1.CLASSCD       = W2.CLASSCD and");
		stb.append("             W1.CURRICULUM_CD = W2.CURRICULUM_CD and");
		stb.append("             W1.SUBCLASSCD    = W2.SUBCLASSCD and");
		stb.append("             W1.SCHREGNO      = W2.SCHREGNO and");
		stb.append("             W2.MONTH is not null and");
		stb.append("             (W2.MONTH = '03' OR W2.MONTH = '09')");
		stb.append("         order by W1.YEAR, W1.CLASSCD, W1.CURRICULUM_CD, W1.SUBCLASSCD, W1.SCHREGNO");
		stb.append("     ) T1");
		stb.append("     group by T1.YEAR, T1.CLASSCD, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO ");
		stb.append("     order by T1.YEAR, T1.CLASSCD, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO ");
		stb.append(" )");
		stb.append(" select");
		stb.append("     T1.GRADE,");
		stb.append("     T1.HR_CLASS,");
		stb.append("     T1.YEAR,");
		stb.append("     T1.SCHREGNO,");
        stb.append("     T1.COURSE_DIV,");
        stb.append("     T1.STUDENT_DIV,");
		stb.append("     T1.CLASSCD,");
		stb.append("     T1.CURRICULUM_CD,");
		stb.append("     T1.SUBCLASSCD,");
		stb.append("     T1.NAME,");
		stb.append("     T1.STAFFNAME,");
		stb.append("     T1.CLASSNAME,");
		stb.append("     T1.SUBCLASSNAME,");
		stb.append("     T1.SCHOOLING_SEQ,");
		stb.append("     T1.CREDITS,");
		stb.append("     T1.REPORT_SEQ,");
		stb.append("     T1.TEST_FLG,");
		stb.append("     T1.USUAL_SCORE,");
		stb.append("     T2.GET_VALUE,");
		stb.append("     T4.SCORE3,");
		stb.append("     T4.SCORE9,");
		stb.append("     T5.RATE,");
        stb.append("     T6.RECORD_COUNT");
		stb.append(" from MAIN_T T1");
		stb.append(" left join REC_SCHOOLING_DAT_T T2 on");
		stb.append("      T1.YEAR          = T2.YEAR and");
		stb.append("      T1.CLASSCD       = T2.CLASSCD and");
		stb.append("      T1.CURRICULUM_CD = T2.CURRICULUM_CD and");
		stb.append("      T1.SUBCLASSCD    = T2.SUBCLASSCD and");
		stb.append("      T1.SCHREGNO      = T2.SCHREGNO");
		stb.append(" left join REC_TEST_DAT_T T4 on");
		stb.append("      T1.YEAR          = T4.YEAR and");
		stb.append("      T1.CLASSCD       = T4.CLASSCD and");
		stb.append("      T1.CURRICULUM_CD = T4.CURRICULUM_CD and");
		stb.append("      T1.SUBCLASSCD    = T4.SUBCLASSCD and");
		stb.append("      T1.SCHREGNO      = T4.SCHREGNO");
		stb.append(" left join REC_SCHOOLING_RATE_DAT_T T5 on");
		stb.append("      T1.YEAR          = T5.YEAR and");
		stb.append("      T1.CLASSCD       = T5.CLASSCD and");
		stb.append("      T1.CURRICULUM_CD = T5.CURRICULUM_CD and");
		stb.append("      T1.SUBCLASSCD    = T5.SUBCLASSCD and");
		stb.append("      T1.SCHREGNO      = T5.SCHREGNO");
        stb.append(" left join REC_SCHOOLING_DAT_T2 T6 on");
        stb.append("      T1.YEAR          = T6.YEAR and");
        stb.append("      T1.CLASSCD       = T6.CLASSCD and");
        stb.append("      T1.CURRICULUM_CD = T6.CURRICULUM_CD and");
        stb.append("      T1.SUBCLASSCD    = T6.SUBCLASSCD and");
        stb.append("      T1.SCHREGNO      = T6.SCHREGNO");
		stb.append(" order by T1.GRADE, T1.HR_CLASS, T1.SCHREGNO, T1.CLASSCD, T1.CURRICULUM_CD, T1.SUBCLASSCD");

		return stb.toString();
    }

    private class Subclass {
        final String _grade;
        final String _hrClass;
        final String _year;
        final String _schregno;
        final String _courseDiv;
        final String _studentDiv;
        final String _classcd;
        final String _curriculumCd;
        final String _subclasscd;
        final String _name;
        final String _staffName;
        final String _classname;
        final String _subclassName;
        /** 単位数. */
        final Integer _credits;
        final Integer _schoolingSeq;
        /** 年間レポート回数. */
        final Integer _reportSeq;
        final boolean _testFlg;//TAKAESU:仕様が?
        /** 単位認定結果.平常点. */
        final Integer _usualScore;
        final Integer _getValue;

        /** 初回のみ存在し、30点以上の数. */
        Integer _reportCount1 = ZERO;

        /** 再提出があり、再提出が30点以上の数. */
        Integer _reportCount2 = ZERO;

        final Integer _score3;
        final Integer _score9;

        /** 割. */
        final Integer _rate;
        final Integer _recordCount;

        /** 通学スクーリング実績の件数(時間数). */
        private Integer _commutingDats;

        Subclass(
                final String grade,
                final String hrClass,
                final String year,
                final String schregno,
                final String courseDiv,
                final String studentDiv,
                final String classcd,
                final String curriculumCd,
                final String subclasscd,
                final String name,
                final String staffname,
                final String classname,
                final String subclassname,
                final Integer credits,
                final Integer schoolingSeq,
                final Integer reportSeq,
                final String testFlg,
                final Integer usualScore,
                final Integer getValue,
                final Integer score3,
                final Integer score9,
                final Integer rate,
                final Integer recordCount
        ) {
        	_grade = grade;
        	_hrClass = hrClass;
        	_year = year;
        	_schregno = schregno;
            _courseDiv = courseDiv;
            _studentDiv = studentDiv;
        	_classcd = classcd;
        	_curriculumCd = curriculumCd;
        	_subclasscd = subclasscd;
        	_name = name;
        	_staffName = staffname;
        	_classname = classname;
        	_subclassName = subclassname;
        	_credits = credits;
        	_schoolingSeq = schoolingSeq;
        	_reportSeq = reportSeq;
        	_testFlg = ("".equals(testFlg)) ? true : false;
        	_usualScore = usualScore;
        	_getValue = (null == getValue) ? ZERO : getValue;
        	_score3 = score3;
        	_score9 = score9;
        	_rate = rate;
            _recordCount = recordCount;
        }

        public int getReportCount() {
            final int reportSeq = _reportSeq.intValue();
            final int count1 = _reportCount1.intValue();
            final int count2 = _reportCount2.intValue();
            return reportSeq - (count1 + count2);
        }

        public int getSchoolingSeq() {
            return (null == _schoolingSeq) ? 0 : _schoolingSeq.intValue();
        }

        public int getRate() {
            return (null == _rate) ? 0 : _rate.intValue();
        }

        public String toString() {
            return _schregno + "/" + _classcd + _curriculumCd + _subclasscd;
        }

        private String reportSql() {
            final String sql;
            sql = "SELECT"
                 + "  SUM(CASE WHEN COMMITED_SCORE2 IS NULL AND COMMITED_SCORE1 >= 30 THEN 1 ELSE 0 END) AS REPORT_COUNT1,"// 初回のみ存在し、30点以上の数
                 + "  SUM(CASE WHEN COMMITED_SCORE2 IS NOT NULL AND COMMITED_SCORE2 >= 30 THEN 1 ELSE 0 END) AS REPORT_COUNT2"// 再提出があり、再提出が30点以上の数
                 + " FROM"
                 + "  REC_REPORT_DAT "
                 + " WHERE"
                 + "  (COMMITED_SCORE1 is not null or COMMITED_SCORE2 is not null) AND"
                 + "  YEAR='" + _param._year + "' AND"
                 + "  CLASSCD='" + _classcd + "' AND"
                 + "  CURRICULUM_CD='" + _curriculumCd + "' AND"
                 + "  SUBCLASSCD='" + _subclasscd + "' AND"
                 + "  SCHREGNO='" + _schregno + "'"
                 ;
            return sql;
        }

        private Integer createRecCommutingDat(final DB2UDB db2) throws SQLException {
            Integer total = null;

            PreparedStatement ps = null;
            ResultSet rs = null;

            final String sql;
            sql = "select"
                + "   count(*) as TOTAL"
                + " from"
                + "   REC_COMMUTING_DAT"
                + " where"
                + "   YEAR = '" + _param._year + "'"
                + "   and CLASSCD = '" + _classcd + "'"
                + "   and CURRICULUM_CD = '" + _curriculumCd + "'"
                + "   and SUBCLASSCD = '" + _subclasscd + "'"
                + "   and SCHREGNO = '" + _schregno + "'"
                ;
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                total = KNJServletUtils.getInteger(rs, "TOTAL");
            }
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);

            _commutingDats = total;
            return total;
        }
    }

    /**
     * 所属名取得処理
     * @param db2			ＤＢ接続オブジェクト
     * @param serchGrade	所属コード
     * @return
     * @throws SQLException
     */
    private String getGredeName(final DB2UDB db2, String serchGrade) throws SQLException {
    	String retGredeName = "";
        final String sql = getBelogingMstSql(serchGrade);
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
            	retGredeName = nvlT(rs.getString("SCHOOLNAME1"));
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return retGredeName;
    }
    
    /**
     * 所属名抽出ＳＱＬ生成処理
     * @param serchGrade
     * @return
     */
    private String getBelogingMstSql(String serchGrade){
    	StringBuffer stb = new StringBuffer();
    	
		stb.append(" select");
		stb.append("     SCHOOLNAME1");
		stb.append(" from BELONGING_MST");
		stb.append(" where");
		stb.append("     BELONGING_DIV = '" + serchGrade + "'");

		return stb.toString();
    }

    // ======================================================================
    /**
     * 学生区分マスタ。
     */
    private Map createStudentDivMst(final DB2UDB db2)
        throws SQLException {

        final Map rtn = new HashMap();

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlStudentDivMst());
        rs = ps.executeQuery();

        while (rs.next()) {
            final String code1 = rs.getString("courseDiv");
            final String code2 = rs.getString("studentDiv");
            final String name = rs.getString("commutingDiv");

            rtn.put(code1 + code2, name);
        }

        return rtn;
    }

    private String sqlStudentDivMst() {
        return " select"
                + "    COURSE_DIV as courseDiv,"
                + "    STUDENT_DIV as studentDiv,"
                + "    COMMUTING_DIV as commutingDiv"
                + " from"
                + "    STUDENTDIV_MST"
                ;
    }
    
    private class Param {
    	private final String _programid;
    	private final String _year;
    	private final String _semester;
    	private final String _loginDate;
    	private final String[] _grade;

    	private final String _date;
    	private final String _nendo;
    	private int pagecnt = 0;	// 現在ページ数
        private Map _commutingDivMap;  // 通学区分

        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _programid = request.getParameter("PRGID");
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _grade = request.getParameterValues("CATEGORY_SELECTED");

            _date = KNJ_EditDate.h_format_JP(_loginDate);
            _nendo = KenjaProperties.gengou(Integer.parseInt(_year))+"年度";
        }

        public void load(final DB2UDB db2) throws SQLException {
            _commutingDivMap = createStudentDivMst(db2);
            return;
        }

        public String _commutingDivMapString(final Subclass subclass) {
            final String code1 = subclass._courseDiv;
            final String code2 = subclass._studentDiv;

            return (String) nvlT((String)_commutingDivMap.get(code1 + code2));
        }
    }

    /**
     * 帳票に設定する文字数が制限文字超の場合
     * 帳票設定エリアの変更を行う
     * @param area_name	帳票出力エリア
     * @param sval			値
     * @param area_len		制限文字数
     * @param hokan_Name1	制限文字以下の場合のエリア名
     * @param hokan_Name2	制限文字超の場合のエリア名
     * @return
     */
    private String setFormatArea(String area_name, String sval, int area_len, String hokan_Name1, String hokan_Name2) {

    	String retAreaName = "";
		// 値がnullの場合はnullが返される
    	if (sval == null) {
			return null;
		}
    	// 設定値が制限文字超の場合、帳票設定エリアの変更を行う
    	if(area_len > sval.length()){
   			retAreaName = area_name + hokan_Name1;
    	} else {
   			retAreaName = area_name + hokan_Name2;
    	}
        return retAreaName;
    }

	/**
	 * 小数点以下が０の場合は、整数部のみを返す
	 */
	private String getRateValue(final String val) {
		String retVal = "";
		if (val.equals("")) {
			return val;
		}
		// 小数点の位置を取得
		int ipos = val.indexOf(".");
		// 小数点が存在する場合
		if(ipos >= 0){
			int len = val.length();
			// 小数点以下の文字列を取得
			retVal = val.substring(ipos+1, len);
		}
		// 小数点以下が０の場合整数部のみを取り出す
		if (Integer.parseInt(retVal) == 0) {
			retVal = val.substring(0, ipos);
		} else {
			retVal = val;
		}
		return retVal;
	}

	/**
	 * NULL値を""として返す。
	 */
	private String nvlT(String val) {

		if (val == null) {
			return "";
		} else {
			return val;
		}
	}

    private static BigDecimal getHour(int minutes) {
        double hour = (double) minutes / 60;

        BigDecimal bd = new BigDecimal(String.valueOf(hour));

        return bd.setScale(1, BigDecimal.ROUND_HALF_UP);
    }

    // ======================================================================
    private class Form {
        private Vrw32alp _svf;

        public Form(final String file,final HttpServletResponse response) throws IOException {
            _svf = new Vrw32alp();

            if (_svf.VrInit() < 0) {
                throw new IllegalStateException("svf初期化失敗");
            }
            _svf.VrSetSpoolFileStream(response.getOutputStream());
            response.setContentType("application/pdf");

            _svf.VrSetForm(FORM_FILE, 4);
        }

        private void closeSvf() {
            if (!_hasData) {
                _svf.VrSetForm("MES001.frm", 0);
                _svf.VrsOut("note", "note");
                _svf.VrEndPage();
            }

            final int ret = _svf.VrQuit();
            log.info("===> VrQuit():" + ret);
        }
    }

    private boolean openDb(final DB2UDB db2) {
        try {
            db2.open();
        } catch (final Exception ex) {
            log.error("db open error!", ex);
            return true;
        }
        return false;
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }
}
