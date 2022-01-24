<?php

require_once('for_php7.php');

class knjl322bQuery extends Query {

    //名称マスタより取得
    function getNameMst($year, $namecd1)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //CSVデータ
    function selectMainQuery($model)
    {
        $query  = " WITH CSV_MAIN AS ( ";    
        $query .= " SELECT DISTINCT ";
        $query .= "     APP_B.TESTDIV, ";
        //入試区分
        $query .= "     L004.NAME1 AS TESTDIV_NAME, ";
        //合否区分略称
        $query .= "     CASE WHEN ENT_JM2.JUDGMENT_COURSE_ABBV IS NOT NULL THEN ENT_JM2.JUDGMENT_COURSE_ABBV ";
        $query .= "          WHEN ENT_JM2.JUDGMENT_COURSE_ABBV IS NULL AND ENT_JM1.JUDGMENT_COURSE_ABBV IS NOT NULL THEN ENT_JM1.JUDGMENT_COURSE_ABBV ";
        $query .= "          WHEN ENT_JM2.JUDGMENT_COURSE_ABBV IS NULL AND ENT_JM1.JUDGMENT_COURSE_ABBV IS NULL THEN VALUE(L013.ABBV1, L013.NAME1) ";//正規略称
        $query .= "          ELSE NULL END AS JUDGMENT_COURSE_ABBV, ";
        //以下、入学コース、入試区分は入学手続者のみ表示
        //入学コース
        $query .= "     CASE WHEN APP_B.ENTDIV = '1' THEN COS_M.COURSENAME || MAJ_M.MAJORNAME || '　' || COSCODE_M.COURSECODENAME ";
        $query .= "          ELSE NULL END AS ENTER_COURSEMAJORCODE_NAME, ";
        //入学区分
        $query .= "     CASE WHEN APP_B.ENTDIV = '1' AND SUC_COURSE_M.EXAMCOURSE_NAME IS NOT NULL THEN SUC_COURSE_M.EXAMCOURSE_NAME || '（' || SUC_COURSE_M.EXAMCOURSE_ABBV || '）' ";
        $query .= "          ELSE NULL END AS EXAMCOURSE_NAME, ";
        //希望コース
        $query .= "     L033.NAME1 AS SELECT_SUBCLASS_DIV_NAME, ";
        //受験番号、他
        $query .= "     APP_B.EXAMNO, ";
        $query .= "     APP_B.NAME, ";
        $query .= "     APP_B.NAME_KANA, ";
        $query .= "     Z002.NAME2, ";
        $query .= "     APP_B.FS_CD, ";
        $query .= "     SCHOOL.FINSCHOOL_NAME_ABBV, ";
        $query .= "     APP_B.RECEPTDATE, ";
        $query .= "     BD006.REMARK1, ";
        $query .= "     VALUE(BD006.REMARK6, '') || '-' || VALUE(BD006.REMARK7, '') || '-' || VALUE(BD006.REMARK8, '') AS REMARK678, ";
        $query .= "     L012.NAME1 AS ENTDIV_NAME, ";
        $query .= "     APP_B.PROCEDUREDATE, ";
        $query .= "     APP_B.SUB_ORDER, ";
        //入学金
        $query .= "     CASE WHEN APP_B.PROCEDUREDIV = '1' AND (RECE_D.PROCEDUREDIV1 <> '1' OR RECE_D.PROCEDUREDIV1 IS NULL) AND (COURSE_JM1.SPECIAL_DIV = '1' OR COURSE_JM2_2.SPECIAL_DIV = '1' OR COURSE_JM3.SPECIAL_DIV = '1') THEN PAY1.ENT_MONEY ";
        $query .= "          WHEN APP_B.PROCEDUREDIV = '1' AND RECE_D.PROCEDUREDIV1  = '1' AND COURSE_JM2.SPECIAL_DIV = '1' THEN PAY1.ENT_MONEY ";
        $query .= "          WHEN APP_B.PROCEDUREDIV = '1' AND (RECE_D.PROCEDUREDIV1 <> '1' OR RECE_D.PROCEDUREDIV1 IS NULL) AND (COURSE_JM1.SPECIAL_DIV = '2' OR COURSE_JM2_2.SPECIAL_DIV = '2' OR COURSE_JM3.SPECIAL_DIV = '2') THEN PAY2.ENT_MONEY ";
        $query .= "          WHEN APP_B.PROCEDUREDIV = '1' AND RECE_D.PROCEDUREDIV1  = '1' AND COURSE_JM2.SPECIAL_DIV = '2' THEN PAY2.ENT_MONEY ";
        $query .= "          WHEN APP_B.PROCEDUREDIV = '1' AND (RECE_D.PROCEDUREDIV1 <> '1' OR RECE_D.PROCEDUREDIV1 IS NULL) AND ((COURSE_JM1.SPECIAL_DIV NOT IN ('1', '2') OR COURSE_JM1.SPECIAL_DIV IS NULL) OR (COURSE_JM2_2.SPECIAL_DIV NOT IN ('1', '2') OR COURSE_JM2_2.SPECIAL_DIV IS NULL) OR (COURSE_JM3.SPECIAL_DIV NOT IN ('1', '2') OR COURSE_JM3.SPECIAL_DIV IS NULL)) THEN PAY0.ENT_MONEY ";
        $query .= "          WHEN APP_B.PROCEDUREDIV = '1' AND RECE_D.PROCEDUREDIV1  = '1' AND (COURSE_JM2.SPECIAL_DIV NOT IN ('1', '2') OR COURSE_JM2.SPECIAL_DIV IS NULL) THEN PAY0.ENT_MONEY ";
        $query .= "          ELSE NULL ";
        $query .= "     END AS ENT_MONEY, ";
        //施設拡充費
        $query .= "     CASE WHEN APP_B.PROCEDUREDIV = '1' AND (RECE_D.PROCEDUREDIV1 <> '1' OR RECE_D.PROCEDUREDIV1 IS NULL) AND (COURSE_JM1.SPECIAL_DIV = '1' OR COURSE_JM2_2.SPECIAL_DIV = '1' OR COURSE_JM3.SPECIAL_DIV = '1') THEN PAY1.FAC_MONEY ";
        $query .= "          WHEN APP_B.PROCEDUREDIV = '1' AND RECE_D.PROCEDUREDIV1  = '1' AND COURSE_JM2.SPECIAL_DIV = '1' THEN PAY1.FAC_MONEY ";
        $query .= "          WHEN APP_B.PROCEDUREDIV = '1' AND (RECE_D.PROCEDUREDIV1 <> '1' OR RECE_D.PROCEDUREDIV1 IS NULL) AND (COURSE_JM1.SPECIAL_DIV = '2' OR COURSE_JM2_2.SPECIAL_DIV = '2' OR COURSE_JM3.SPECIAL_DIV = '2') THEN PAY2.FAC_MONEY ";
        $query .= "          WHEN APP_B.PROCEDUREDIV = '1' AND RECE_D.PROCEDUREDIV1  = '1' AND COURSE_JM2.SPECIAL_DIV = '2' THEN PAY2.FAC_MONEY ";
        $query .= "          WHEN APP_B.PROCEDUREDIV = '1' AND (RECE_D.PROCEDUREDIV1 <> '1' OR RECE_D.PROCEDUREDIV1 IS NULL) AND ((COURSE_JM1.SPECIAL_DIV NOT IN ('1', '2') OR COURSE_JM1.SPECIAL_DIV IS NULL) OR (COURSE_JM2_2.SPECIAL_DIV NOT IN ('1', '2') OR COURSE_JM2_2.SPECIAL_DIV IS NULL) OR (COURSE_JM3.SPECIAL_DIV NOT IN ('1', '2') OR COURSE_JM3.SPECIAL_DIV IS NULL)) THEN PAY0.FAC_MONEY ";
        $query .= "          WHEN APP_B.PROCEDUREDIV = '1' AND RECE_D.PROCEDUREDIV1  = '1' AND (COURSE_JM2.SPECIAL_DIV NOT IN ('1', '2') OR COURSE_JM2.SPECIAL_DIV IS NULL) THEN PAY0.FAC_MONEY ";
        $query .= "          ELSE NULL ";
        $query .= "     END AS FAC_MONEY, ";
        //計
        $query .= "     CAST(NULL AS INT) AS GOUKEI ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT APP_B ";
        $query .= "     LEFT JOIN NAME_MST L004 ON L004.NAMECD1 = 'L004' ";
        $query .= "          AND APP_B.TESTDIV = L004.NAMECD2 ";
        $query .= "     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ";
        $query .= "          AND APP_B.SEX = Z002.NAMECD2 ";
        $query .= "     LEFT JOIN NAME_MST L012 ON L012.NAMECD1 = 'L012' ";
        $query .= "          AND APP_B.ENTDIV = L012.NAMECD2 ";
        $query .= "     LEFT JOIN NAME_MST L033 ON L033.NAMECD1 = 'L033' ";
        $query .= "          AND APP_B.SELECT_SUBCLASS_DIV = L033.NAMECD2 ";
        //入学区分
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST SUC_COURSE_M ON APP_B.ENTEXAMYEAR = SUC_COURSE_M.ENTEXAMYEAR ";
        $query .= "          AND APP_B.APPLICANTDIV = SUC_COURSE_M.APPLICANTDIV ";
        $query .= "          AND APP_B.TESTDIV = SUC_COURSE_M.TESTDIV ";
        $query .= "          AND APP_B.SUC_COURSECD || APP_B.SUC_MAJORCD || APP_B.SUC_COURSECODE = SUC_COURSE_M.COURSECD || SUC_COURSE_M.MAJORCD || SUC_COURSE_M.EXAMCOURSECD ";
        //入学コース
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD007 ON APP_B.ENTEXAMYEAR = BD007.ENTEXAMYEAR ";
        $query .= "          AND APP_B.EXAMNO = BD007.EXAMNO ";
        $query .= "          AND BD007.SEQ = '007' ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST COURSE_M ON APP_B.ENTEXAMYEAR = COURSE_M.ENTEXAMYEAR ";
        $query .= "          AND APP_B.APPLICANTDIV = COURSE_M.APPLICANTDIV ";
        $query .= "          AND APP_B.TESTDIV = COURSE_M.TESTDIV ";
        $query .= "          AND VALUE(BD007.REMARK1, '0') || VALUE(BD007.REMARK2, '0') || VALUE(BD007.REMARK3, '0') = COURSE_M.ENTER_COURSECD || COURSE_M.ENTER_MAJORCD || COURSE_M.ENTER_COURSECODE ";
        $query .= "     LEFT JOIN COURSE_MST COS_M ON COURSE_M.ENTER_COURSECD = COS_M.COURSECD ";
        $query .= "     LEFT JOIN MAJOR_MST MAJ_M  ON COURSE_M.ENTER_COURSECD = MAJ_M.COURSECD ";
        $query .= "                               AND COURSE_M.ENTER_MAJORCD = MAJ_M.MAJORCD ";
        $query .= "     LEFT JOIN COURSECODE_MST COSCODE_M ON COURSE_M.ENTER_COURSECODE = COSCODE_M.COURSECODE ";
        $query .= "     LEFT JOIN FINSCHOOL_MST SCHOOL ON APP_B.FS_CD = SCHOOL.FINSCHOOLCD ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD006 ON APP_B.ENTEXAMYEAR = BD006.ENTEXAMYEAR ";
        $query .= "          AND APP_B.EXAMNO = BD006.EXAMNO ";
        $query .= "          AND BD006.SEQ = '006' ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DAT RECE_D ON APP_B.ENTEXAMYEAR = RECE_D.ENTEXAMYEAR ";
        $query .= "          AND APP_B.APPLICANTDIV = RECE_D.APPLICANTDIV ";
        $query .= "          AND APP_B.TESTDIV = RECE_D.TESTDIV ";
        $query .= "          AND RECE_D.EXAM_TYPE = '1' ";
        $query .= "          AND APP_B.EXAMNO = RECE_D.EXAMNO ";
        $query .= "     LEFT JOIN NAME_MST L013 ON L013.NAMECD1 = 'L013' ";
        $query .= "          AND RECE_D.JUDGEDIV = L013.NAMECD2 ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RECE_DD001 ON RECE_D.ENTEXAMYEAR = RECE_DD001.ENTEXAMYEAR ";
        $query .= "          AND RECE_D.APPLICANTDIV = RECE_DD001.APPLICANTDIV ";
        $query .= "          AND RECE_D.TESTDIV = RECE_DD001.TESTDIV ";
        $query .= "          AND RECE_D.EXAM_TYPE= RECE_DD001.EXAM_TYPE ";
        $query .= "          AND RECE_D.RECEPTNO = RECE_DD001.RECEPTNO ";
        $query .= "          AND RECE_DD001.SEQ = '001' ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RECE_DD002 ON RECE_D.ENTEXAMYEAR = RECE_DD002.ENTEXAMYEAR ";
        $query .= "          AND RECE_D.APPLICANTDIV = RECE_DD002.APPLICANTDIV ";
        $query .= "          AND RECE_D.TESTDIV = RECE_DD002.TESTDIV ";
        $query .= "          AND RECE_D.EXAM_TYPE= RECE_DD002.EXAM_TYPE ";
        $query .= "          AND RECE_D.RECEPTNO = RECE_DD002.RECEPTNO ";
        $query .= "          AND RECE_DD002.SEQ = '002' ";
        //推薦入試
        $query .= "     LEFT JOIN ENTEXAM_COURSE_JUDGMENT_MST COURSE_JM1 ON RECE_DD001.ENTEXAMYEAR = COURSE_JM1.ENTEXAMYEAR ";
        $query .= "          AND COURSE_JM1.TAKE_RECOMMEND_TEST_FLG = '1' ";
        $query .= "          AND APP_B.SUC_COURSECD || APP_B.SUC_MAJORCD || APP_B.SUC_COURSECODE = COURSE_JM1.NORMAL_PASSCOURSECD || COURSE_JM1.NORMAL_PASSMAJORCD || COURSE_JM1.NORMAL_PASSEXAMCOURSECD ";
        $query .= "          AND APP_B.TESTDIV = '1' ";
        //一般入試 (単願切換)
        $query .= "     LEFT JOIN ENTEXAM_COURSE_JUDGMENT_MST COURSE_JM2 ON APP_B.ENTEXAMYEAR = COURSE_JM2.ENTEXAMYEAR ";
        $query .= "          AND COURSE_JM2.CHANGE_SINGLE_TEST_FLG = '1' ";
        $query .= "          AND RECE_DD002.REMARK4 = '1' ";
        $query .= "          AND APP_B.SUC_COURSECD || APP_B.SUC_MAJORCD || APP_B.SUC_COURSECODE = VALUE(RECE_DD002.REMARK1, '0') || VALUE(RECE_DD002.REMARK2, '000') || VALUE(RECE_DD002.REMARK3, '0000') ";
        $query .= "          AND APP_B.SUC_COURSECD || APP_B.SUC_MAJORCD || APP_B.SUC_COURSECODE = COURSE_JM2.EARLY_PASSCOURSECD || COURSE_JM2.EARLY_PASSMAJORCD || COURSE_JM2.EARLY_PASSEXAMCOURSECD ";
        $query .= "          AND APP_B.TESTDIV = '2' ";
        //一般入試 (単願切換対象者だが、正規手続)
        $query .= "     LEFT JOIN ENTEXAM_COURSE_JUDGMENT_MST COURSE_JM2_2 ON APP_B.ENTEXAMYEAR = COURSE_JM2_2.ENTEXAMYEAR ";
        //$query .= "          AND COURSE_JM2_2.CHANGE_SINGLE_TEST_FLG = '1' ";
        $query .= "          AND COURSE_JM2_2.TAKE_GENERAL_TEST_FLG = '1' ";
        $query .= "          AND RECE_DD002.REMARK4 = '1' ";
        $query .= "          AND APP_B.SUC_COURSECD || APP_B.SUC_MAJORCD || APP_B.SUC_COURSECODE <> VALUE(RECE_DD002.REMARK1, '0') || VALUE(RECE_DD002.REMARK2, '000') || VALUE(RECE_DD002.REMARK3, '0000') ";
        $query .= "          AND APP_B.SUC_COURSECD || APP_B.SUC_MAJORCD || APP_B.SUC_COURSECODE = COURSE_JM2_2.NORMAL_PASSCOURSECD || COURSE_JM2_2.NORMAL_PASSMAJORCD || COURSE_JM2_2.NORMAL_PASSEXAMCOURSECD ";
        $query .= "          AND APP_B.TESTDIV = '2' ";
        //一般入試 (正規)
        $query .= "     LEFT JOIN ENTEXAM_COURSE_JUDGMENT_MST COURSE_JM3 ON APP_B.ENTEXAMYEAR = COURSE_JM3.ENTEXAMYEAR ";
        $query .= "          AND COURSE_JM3.TAKE_GENERAL_TEST_FLG = '1' ";
        $query .= "          AND VALUE(RECE_DD002.REMARK4, '') <> '1' ";
        $query .= "          AND APP_B.SUC_COURSECD || APP_B.SUC_MAJORCD || APP_B.SUC_COURSECODE = COURSE_JM3.NORMAL_PASSCOURSECD || COURSE_JM3.NORMAL_PASSMAJORCD || COURSE_JM3.NORMAL_PASSEXAMCOURSECD ";
        $query .= "          AND APP_B.TESTDIV = '2' ";
        //入学区分1 (正規)
        $query .= "     LEFT JOIN ENTEXAM_COURSE_JUDGMENT_MST ENT_JM1 ON RECE_DD001.ENTEXAMYEAR = ENT_JM1.ENTEXAMYEAR ";
        $query .= "          AND RECE_DD001.REMARK4 = '1' ";
        $query .= "          AND (ENT_JM1.TAKE_RECOMMEND_TEST_FLG = '1' OR ENT_JM1.TAKE_GENERAL_TEST_FLG = '1') ";
        $query .= "          AND VALUE(RECE_DD001.REMARK1, '0') || VALUE(RECE_DD001.REMARK2, '000') || VALUE(RECE_DD001.REMARK3, '0000') = ENT_JM1.NORMAL_PASSCOURSECD || ENT_JM1.NORMAL_PASSMAJORCD || ENT_JM1.NORMAL_PASSEXAMCOURSECD ";
        //入学区分2 (単願切換)
        $query .= "     LEFT JOIN ENTEXAM_COURSE_JUDGMENT_MST ENT_JM2 ON RECE_DD002.ENTEXAMYEAR = ENT_JM2.ENTEXAMYEAR ";
        $query .= "          AND RECE_DD002.REMARK4 = '1' ";
        $query .= "          AND ENT_JM2.CHANGE_SINGLE_TEST_FLG = '1' ";
        $query .= "          AND VALUE(RECE_DD002.REMARK1, '0') || VALUE(RECE_DD002.REMARK2, '000') || VALUE(RECE_DD002.REMARK3, '0000') = ENT_JM2.EARLY_PASSCOURSECD || ENT_JM2.EARLY_PASSMAJORCD || ENT_JM2.EARLY_PASSEXAMCOURSECD ";
        //入学金、施設拡充費取得
        $query .= "     LEFT JOIN ENTEXAM_PAYMENT_MONEY_YMST PAY0 ON APP_B.ENTEXAMYEAR = PAY0.ENTEXAMYEAR ";
        $query .= "          AND APP_B.APPLICANTDIV = PAY0.APPLICANTDIV ";
        $query .= "          AND PAY0.JUDGE_KIND = '0' ";
        $query .= "     LEFT JOIN ENTEXAM_PAYMENT_MONEY_YMST PAY1 ON APP_B.ENTEXAMYEAR = PAY1.ENTEXAMYEAR ";
        $query .= "          AND APP_B.APPLICANTDIV = PAY1.APPLICANTDIV ";
        $query .= "          AND PAY1.JUDGE_KIND = '1' ";
        $query .= "     LEFT JOIN ENTEXAM_PAYMENT_MONEY_YMST PAY2 ON APP_B.ENTEXAMYEAR = PAY2.ENTEXAMYEAR ";
        $query .= "          AND APP_B.APPLICANTDIV = PAY2.APPLICANTDIV ";
        $query .= "          AND PAY2.JUDGE_KIND = '2' ";
        $query .= " WHERE ";
        $query .= "     APP_B.ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND APP_B.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        if ($model->field["OUTPUT"] == "2") {
            $query .= "     AND APP_B.JUDGEMENT = '1' ";
        }
        if ($model->field["OUTPUT"] == "3") {
            $query .= "     AND APP_B.JUDGEMENT = '2' ";
        }
        $query .= " ) ";        
        $query .= " SELECT ";
        $query .= "     ROW_NUMBER() OVER(ORDER BY TESTDIV, EXAMNO) AS ROWNO, ";
        $query .= "     TESTDIV_NAME, ";
        $query .= "     JUDGMENT_COURSE_ABBV, ";
        $query .= "     ENTER_COURSEMAJORCODE_NAME, ";
        $query .= "     EXAMCOURSE_NAME, ";
        $query .= "     SELECT_SUBCLASS_DIV_NAME, ";
        $query .= "     EXAMNO, ";
        $query .= "     NAME, ";
        $query .= "     NAME_KANA, ";
        $query .= "     NAME2, ";
        $query .= "     FS_CD, ";
        $query .= "     FINSCHOOL_NAME_ABBV, ";
        $query .= "     RECEPTDATE, ";
        $query .= "     REMARK1, ";
        $query .= "     REMARK678, ";
        $query .= "     ENTDIV_NAME, ";
        $query .= "     PROCEDUREDATE, ";
        $query .= "     SUB_ORDER, ";
        $query .= "     ENT_MONEY, ";
        $query .= "     FAC_MONEY, ";
        $query .= "     GOUKEI ";
        $query .= " FROM ";
        $query .= "     CSV_MAIN ";
        $query .= " ORDER BY ";
        $query .= "     TESTDIV, ";
        $query .= "     EXAMNO ";

        return $query;
    }
}
?>
