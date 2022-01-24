<?php

require_once('for_php7.php');

class knja293Query extends Query {

    //名称マスタより取得
    function getNameMst($model, $namecd1)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->ObjYear."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd1 == "L009") {
            $query .= "     AND NAME1 IS NOT NULL ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学納金CSVヘッダー
    function getHeaderQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PAYMENT_MONEY_YMST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= " AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= " AND JUDGE_KIND = '0' ";

        return $query;
    }
        
    //CSVデータ
    function selectMainQuery($model, $testsubclassList)
    {
        $query  = " SELECT ";
//        $query .= "     ROW_NUMBER() OVER(ORDER BY T1.TESTDIV, T1.EXAMNO) AS ROWNO,  ";
        $query .= "     GHA.GRADE, ";
        $query .= "     GHA.HR_CLASS, ";
        $query .= "     GHA.ATTENDNO, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     L004.NAME1 AS TESTDIV_NAME, ";
        $query .= "     BD001.REMARK10 AS EXAMCOURSECD, ";
        $query .= "     CM001.EXAMCOURSE_NAME AS EXAMCOURSE_NAME,  ";
        $query .= "     BD007.REMARK3 AS ENTER_COURSECODE, ";
        $query .= "     CM007.COURSECODENAME AS ENTER_COURSECODENAME, ";
        $query .= "     T1.SUC_COURSECODE AS SUC_COURSECODE, ";
        $query .= "     CMSUC.EXAMCOURSE_NAME AS SUC_COURSECODENAME, ";
        $query .= "     T1.JUDGEMENT, ";
        $query .= "     L013.NAME1 AS JUDGEMENT_NAME, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.SEX, ";
        $query .= "     Z002.NAME2 AS SEX_NAME, ";
        $query .= "     T1.FS_CD, ";
        $query .= "     SCHOOL.FINSCHOOL_NAME_ABBV,  ";
        $query .= "     T1.RECEPTDATE, ";
        $query .= "     CASE WHEN BD006.REMARK6 = '1' THEN '○' ELSE NULL END AS ONGAKU1_KIBOU, ";
        $query .= "     CASE WHEN BD006.REMARK7 = '1' THEN '○' ELSE NULL END AS BIJYUTU1_KIBOU, ";
        $query .= "     CASE WHEN BD006.REMARK8 = '1' THEN '○' ELSE NULL END AS SHODOU1_KIBOU, ";
        $query .= "     T1.ENTDIV, ";
        $query .= "     L012.NAME1 AS ENTDIV_NAME, ";
        $query .= "     T1.PROCEDUREDATE, ";
        $query .= "     PM.ENT_MONEY, ";
        $query .= "     PM.FAC_MONEY, ";
        $query .= "     PM.REMARK1_MONEY, ";
        $query .= "     PM.REMARK2_MONEY, ";
        $query .= "     PM.REMARK3_MONEY, ";
        $query .= "     VALUE(PM.ENT_MONEY , 0) + VALUE(PM.FAC_MONEY , 0) + VALUE(PM.REMARK1_MONEY , 0) + VALUE(PM.REMARK2_MONEY , 0) + VALUE(PM.REMARK3_MONEY , 0) AS TOTAL_MONEY, ";
        //
        $query .= "     T1.SHDIV, ";
        $query .= "     N2.NAME1 AS SHDIV_NAME, ";
        $query .= "     BD009.REMARK9, ";
        $query .= "     N5.NAME1 AS REMARK9_NAME, ";
        $query .= "     T1.BIRTHDAY, ";
        $query .= "     T4.ZIPCD, ";
        $query .= "     T4.ADDRESS1, ";
        $query .= "     T4.ADDRESS2, ";
        $query .= "     T4.TELNO, ";
        $query .= "     T1.FS_DAY, ";
        $query .= "     T1.FS_GRDDIV, ";
        $query .= "     N1.NAME1 AS FS_GRDDIV_NAME, ";
        $query .= "     T4.GNAME, ";
        $query .= "     T4.GKANA, ";
        $query .= "     T4.GZIPCD, ";
        $query .= "     T4.GADDRESS1, ";
        $query .= "     T4.GADDRESS2, ";
        $query .= "     T4.GTELNO, ";
        $query .= "     BD009.REMARK7, ";
        $query .= "     N3.NAME1 AS REMARK7_NAME, ";
        $query .= "     BD009.REMARK1, ";
        $query .= "     BD009.REMARK2, ";
        $query .= "     BD009.REMARK3, ";
        $query .= "     BD009.REMARK4, ";
        $query .= "     BD009.REMARK8, ";
        $query .= "     N4.NAME1 AS REMARK8_NAME, ";
        $query .= "     BD009.REMARK5, ";
        $query .= "     BD009.REMARK6, ";
        $query .= "     T3.CONFIDENTIAL_RPT01, ";
        $query .= "     T3.CONFIDENTIAL_RPT02, ";
        $query .= "     T3.CONFIDENTIAL_RPT03, ";
        $query .= "     T3.CONFIDENTIAL_RPT04, ";
        $query .= "     T3.CONFIDENTIAL_RPT05, ";
        $query .= "     T3.CONFIDENTIAL_RPT06, ";
        $query .= "     T3.CONFIDENTIAL_RPT07, ";
        $query .= "     T3.CONFIDENTIAL_RPT08, ";
        $query .= "     T3.CONFIDENTIAL_RPT09, ";
        $query .= "     T3.TOTAL_ALL, ";
        $query .= "     T3.TOTAL5, ";
        $query .= "     T3.KASANTEN_ALL, ";
        $query .= "     T3.ABSENCE_DAYS, ";
        $query .= "     T3.ABSENCE_DAYS2, ";
        $query .= "     T3.ABSENCE_DAYS3, ";
        $query .= "     T3.ABSENCE_REMARK, ";
        $query .= "     T3.ABSENCE_REMARK2, ";
        $query .= "     T3.ABSENCE_REMARK3, ";
        $query .= "     T5.REMARK1 AS REMARK1_004, ";
        $query .= "     T3.REMARK1 AS TOKU_REMARK1, ";
        $query .= "     R1.TOTAL4 AS E_TOTAL ";
        foreach ($testsubclassList as $val => $label) {
            $query .= "     ,S{$val}.SCORE AS EXAM_SCORE{$val} ";
        }

        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ( ";
        $query .= "         SELECT ";
        $query .= "             fiscalyear(B1.ENT_DATE) AS ENTEXAMYEAR, ";
        $query .= "             B2.BASE_REMARK1 AS EXAMNO, ";
        $query .= "             R1.GRADE, ";
        $query .= "             R1.HR_CLASS, ";
        $query .= "             R1.ATTENDNO ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT R1 ";
        $query .= "             LEFT JOIN SCHREG_BASE_MST B1 ON B1.SCHREGNO = R1.SCHREGNO ";
        $query .= "             LEFT JOIN SCHREG_BASE_DETAIL_MST B2 ON B2.SCHREGNO = B1.SCHREGNO AND B2.BASE_SEQ = '003' ";
        $query .= "         WHERE ";
        $query .= "             R1.YEAR = '".$model->ObjYear."' ";
        $query .= "             AND R1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "             AND R1.YEAR = fiscalyear(B1.ENT_DATE) ";
        $query .= "             AND B1.ENT_DATE IS NOT NULL ";
        $query .= "             AND B2.BASE_REMARK1 IS NOT NULL ";
        $query .= "     ) GHA ON GHA.ENTEXAMYEAR = T1.ENTEXAMYEAR AND GHA.EXAMNO = T1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD001 ON BD001.ENTEXAMYEAR = T1.ENTEXAMYEAR  ";
        $query .= "                                                     AND BD001.EXAMNO = T1.EXAMNO  ";
        $query .= "                                                     AND BD001.SEQ = '001'  ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD006 ON BD006.ENTEXAMYEAR = T1.ENTEXAMYEAR  ";
        $query .= "                                                     AND BD006.EXAMNO = T1.EXAMNO  ";
        $query .= "                                                     AND BD006.SEQ = '006'  ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD007 ON BD007.ENTEXAMYEAR = T1.ENTEXAMYEAR  ";
        $query .= "                                                     AND BD007.EXAMNO = T1.EXAMNO  ";
        $query .= "                                                     AND BD007.SEQ = '007'  ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD009 ON BD009.ENTEXAMYEAR = T1.ENTEXAMYEAR  ";
        $query .= "                                                     AND BD009.EXAMNO = T1.EXAMNO  ";
        $query .= "                                                     AND BD009.SEQ = '009'  ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST CM001 ON CM001.ENTEXAMYEAR = T1.ENTEXAMYEAR  ";
        $query .= "                                       AND CM001.APPLICANTDIV = T1.APPLICANTDIV  ";
        $query .= "                                       AND CM001.TESTDIV = T1.TESTDIV  ";
        $query .= "                                       AND CM001.COURSECD = BD001.REMARK8 ";
        $query .= "                                       AND CM001.MAJORCD = BD001.REMARK9 ";
        $query .= "                                       AND CM001.EXAMCOURSECD = BD001.REMARK10 ";
        $query .= "     LEFT JOIN COURSECODE_MST CM007 ON CM007.COURSECODE = BD007.REMARK3 ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST CMSUC ON CMSUC.ENTEXAMYEAR = T1.ENTEXAMYEAR  ";
        $query .= "                                       AND CMSUC.APPLICANTDIV = T1.APPLICANTDIV  ";
        $query .= "                                       AND CMSUC.TESTDIV = T1.TESTDIV  ";
        $query .= "                                       AND CMSUC.COURSECD = T1.SUC_COURSECD ";
        $query .= "                                       AND CMSUC.MAJORCD = T1.SUC_MAJORCD ";
        $query .= "                                       AND CMSUC.EXAMCOURSECD = T1.SUC_COURSECODE ";
        $query .= "     LEFT JOIN ENTEXAM_PAYMENT_MONEY_YMST PM ON PM.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                                            AND PM.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                                            AND PM.JUDGE_KIND = VALUE(BD009.REMARK8, '0') ";
        $query .= "     LEFT JOIN FINSCHOOL_MST SCHOOL ON T1.FS_CD = SCHOOL.FINSCHOOLCD ";
        $query .= "     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' AND Z002.NAMECD2 = T1.SEX ";
        $query .= "     LEFT JOIN NAME_MST L004 ON L004.NAMECD1 = 'L004' AND L004.NAMECD2 = T1.TESTDIV ";
        $query .= "     LEFT JOIN NAME_MST L012 ON L012.NAMECD1 = 'L012' AND L012.NAMECD2 = T1.ENTDIV ";
        $query .= "     LEFT JOIN NAME_MST L013 ON L013.NAMECD1 = 'L013' AND L013.NAMECD2 = T1.JUDGEMENT ";
        //
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT T3 ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR AND T3.EXAMNO = T1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT T4 ON T4.ENTEXAMYEAR = T1.ENTEXAMYEAR AND T4.EXAMNO = T1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T5 ON T5.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                                                  AND T5.EXAMNO      = T1.EXAMNO ";
        $query .= "                                                  AND T5.SEQ         = '004' ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'L016' AND N1.NAMECD2 = T1.FS_GRDDIV ";
        $query .= "     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'L006' AND N2.NAMECD2 = T1.SHDIV ";
        $query .= "     LEFT JOIN NAME_MST N3 ON N3.NAMECD1 = 'L025' AND N3.NAMECD2 = BD009.REMARK7 ";
        $query .= "     LEFT JOIN NAME_MST N4 ON N4.NAMECD1 = 'L025' AND N4.NAMECD2 = BD009.REMARK8 ";
        $query .= "     LEFT JOIN NAME_MST N5 ON N5.NAMECD1 = 'L026' AND N5.NAMECD2 = BD009.REMARK9 ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DAT R1  ON R1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "                                     AND R1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                                     AND R1.TESTDIV      = T1.TESTDIV ";
        $query .= "                                     AND R1.EXAM_TYPE    = '1' ";
        $query .= "                                     AND R1.RECEPTNO     = T1.EXAMNO ";
        foreach ($testsubclassList as $val => $label) {
            $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT S{$val}  ON S{$val}.ENTEXAMYEAR     = R1.ENTEXAMYEAR ";
            $query .= "                                         AND S{$val}.APPLICANTDIV    = R1.APPLICANTDIV ";
            $query .= "                                         AND S{$val}.TESTDIV         = R1.TESTDIV ";
            $query .= "                                         AND S{$val}.EXAM_TYPE       = R1.EXAM_TYPE ";
            $query .= "                                         AND S{$val}.RECEPTNO        = R1.RECEPTNO ";
            $query .= "                                         AND S{$val}.TESTSUBCLASSCD  = '{$val}' ";
        }

        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= " AND T1.ENTDIV = '1' ";
        $query .= " AND T1.JUDGEMENT IN ('1', '3') ";
        $query .= " ORDER BY ";
        $query .= "     GHA.GRADE, ";
        $query .= "     GHA.HR_CLASS, ";
        $query .= "     GHA.ATTENDNO, ";
        $query .= "     T1.EXAMNO ";

        return $query;
    }
}
?>
