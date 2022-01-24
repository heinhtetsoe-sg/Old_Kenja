//kanji
/*
 * $Id: fed660f656b08bba2ef1200f5ff898808df89e83 $
 *
 * 作成日: 2009/06/12
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *  学校教育システム 賢者 [成績管理] 成績個人票
 */

public class KNJD153T extends KNJD151 {

    private static final Log log = LogFactory.getLog(KNJD153T.class);

    private static final String FORM_FILE = "KNJD153T_1.frm";
    private static final String FORM_FILE2 = "KNJD153T_2.frm";
    
    protected String setSvfForm(final Map paramap) { return ((String)paramap.get("DEVIATION") != null) ? FORM_FILE2 : FORM_FILE; }

    protected List setTestKindList(final Map paramap) {
        final List list = new ArrayList();
        if ((String) paramap.get("TESTKINDCD") == null) { return list; }

        list.add (setTestKindListDetail (paramap));
        return list;
    }

    protected List setRecordList(final Map paramap) {

        final List list = new ArrayList();
        list.add(setRecordFieldListDetail("SEM1_INTR_CHAIRCD","SEM1_INTR_SCORE","SEM1_INTR_VALUE","SEM1_INTR_VALUE_DI","1-0101"));  // １学期中間テスト
        list.add(setRecordFieldListDetail("SEM1_TERM_CHAIRCD","SEM1_TERM_SCORE","SEM1_TERM_VALUE","SEM1_TERM_VALUE_DI","1-0201"));  // １学期期末テスト
        list.add(setRecordFieldListDetail(null,null,"SEM1_VALUE","SEM1_VALUE_DI","1-0000"));  // １学期評価

        final int semester = Integer.parseInt((String)paramap.get("SEMESTER"));
        final int semesterDiv = Integer.parseInt(_knjSchoolMst._year);
        if (1 < semester) {
            list.add(setRecordFieldListDetail("SEM2_INTR_CHAIRCD","SEM2_INTR_SCORE","SEM2_INTR_VALUE","SEM2_INTR_VALUE_DI","2-0101"));  // ２学期中間テスト
            list.add(setRecordFieldListDetail("SEM2_TERM_CHAIRCD","SEM2_TERM_SCORE","SEM2_TERM_VALUE","SEM2_TERM_VALUE_DI","2-0201"));  // ２学期期末テスト
            list.add(setRecordFieldListDetail(null,null,"SEM2_VALUE","SEM2_VALUE_DI","2-0000"));  // ２学期評価
        }
        if (2 < semester && 2 < semesterDiv) {
            list.add(setRecordFieldListDetail("SEM3_TERM_CHAIRCD","SEM3_TERM_SCORE","SEM3_TERM_VALUE","SEM3_TERM_VALUE_DI","3-0201"));  // ３学期期末テスト
            list.add(setRecordFieldListDetail(null,null,"SEM3_VALUE","SEM3_VALUE_DI","3-0000"));  // ３学期評価
        }
        list.add(setRecordFieldListDetail(null,null,"GRAD_VALUE","GRAD_VALUE_DI","9-0000"));  // 学年評定

        return list;
    }
    
    /** 
     *   ＨＲ成績生徒別明細を出力 => VrEndRecord()
     *     printSvfOutDetal(Vrm32alp, svf-field-name, data1(data1がnullでないならdata1を右寄せで出力), 
     *                                               data2(data1がnullならdata2を中央割付で出力))
     */
    protected void printSvfOutMeisai(
            Vrw32alp svf,
            ResultSet rs,
            Map paramap
    ) {
        try {
            svf.VrsOut("NAME", rs.getString("HR_NAME") + " " + String.valueOf( Integer.parseInt( rs.getString("ATTENDNO") ) ) + "番   " + rs.getString("NAME") );  // 生徒名

            String subclassName = rs.getString("SUBCLASSNAME");
            svf.VrsOut("SUBCLASS"+(subclassName != null && subclassName.length() > 8 ? "2" : ""), subclassName);  // 科目名
            svf.VrsOut("CREDIT", rs.getString("CREDITS"));  // 単位数

            printSvfOutScoreAndValue(svf, rs);

            final String absent = rs.getString("ABSENT");
            final String lateEarly = rs.getString("LATE_EARLY");
            final String absent2 = rs.getString("ABSENT2");
            
            if (isEnabled(absent)) {
                svf.VrsOut("KEKKA1" + getFieldNum(absent), absent);  // 欠時数
            }

            if (isEnabled(lateEarly))
                svf.VrsOut("KEKKA2" + getFieldNum(lateEarly),lateEarly);  // 遅早数

            if(isEnabled(absent2)) {
                final DecimalFormat dmf = (definecode.absent_cov == 3 || definecode.absent_cov == 4) ? dmf2 : dmf1;
                final String absent2Val = String.valueOf(dmf.format(rs.getFloat("ABSENT2")));
                svf.VrsOut("KEKKA3" + getFieldNum(absent2Val), absent2Val);  // 欠時数
            }

            printSvfOutInnerChair(svf,rs,paramap);  // 講座に基づく出力
            
            int ret = svf.VrEndRecord();
            if(ret == 0)nonedata = true;
        } catch( SQLException ex ){
            log.error("[KNJD153T]printSvfOutMeisai error!", ex );
        }
    }
    
    private boolean isEnabled(String exNumber) {
        return exNumber != null && 0 < Float.parseFloat(exNumber);
    }
    
    private String getFieldNum(String data) {
        final int size1 = 3;
        return (data != null && data.getBytes().length > size1) ? "_2" : "_1";
    }
}
