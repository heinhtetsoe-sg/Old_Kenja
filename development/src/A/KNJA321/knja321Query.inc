<?php

require_once('for_php7.php');

class knja321Query extends Query {

    //年組取得（権限チェック）
    function getGradeHrclass($model) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ";
                $query .= "      ON T2.YEAR         = T1.YEAR ";
                $query .= "     AND T2.GRADE        = T1.GRADE ";
                $query .= "     AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ";
            $query .= "      ON T2.YEAR         = T1.YEAR ";
            $query .= "     AND T2.GRADE        = T1.GRADE ";
            $query .= "     AND T2.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= " AND (TR_CD1     = '".STAFFCD."' OR ";
            $query .= "      TR_CD2     = '".STAFFCD."' OR ";
            $query .= "      TR_CD3     = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD2  = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD3  = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    //学年取得
    function getGrade($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T2.GRADE AS VALUE, ";
        $query .= "     T2.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE    = T2.GRADE ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //ＣＳＶ出力
    function getOutputQueryCsv($model)
    {
        $query  = "WITH SCHNO AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     ATTENDNO, ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER, ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        if($model->field["GRADE"]){
            $query .= "     AND GRADE = '{$model->field["GRADE"]}' ) ";
        } else {
            $query .= "     AND GRADE||HR_CLASS = '{$model->field["GRADE_HR_CLASS"]}' ) ";
        }

        $query .= ",STAFF AS ( ";
        $query .= " SELECT ";
        $query .= "     TR_CD1, ";
        $query .= "     YEAR, ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     HR_NAMEABBV ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        if($model->field["GRADE"]){
            $query .= "     AND GRADE = '{$model->field["GRADE"]}' ) ";
        } else {
            $query .= "     AND GRADE||HR_CLASS = '{$model->field["GRADE_HR_CLASS"]}' ) ";
        }
        $query .= ",ADDRESS_DAT AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.TELNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_ADDRESS_DAT T1 ";
        $query .= " INNER JOIN ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         MAX(ISSUEDATE) AS ISSUEDATE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_ADDRESS_DAT ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= "     ) L1 ON  L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "          AND L1.ISSUEDATE = T1.ISSUEDATE ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     ROW_NUMBER() OVER(ORDER BY T1.GRADE,T1.HR_CLASS,T1.ATTENDNO) AS RENBAN, ";
        $query .= "     T1.YEAR, ";
        $query .= "     RTRIM(CHAR(INT(T1.GRADE))) || RTRIM(CHAR(INT(T1.HR_CLASS))) || (CASE WHEN INT(T1.ATTENDNO)<10 THEN '0'||RTRIM(CHAR(INT(T1.ATTENDNO))) ELSE RTRIM(CHAR(INT(T1.ATTENDNO))) END) AS CODE, ";
        $query .= "     T2.NAME, ";
        $query .= "     T2.NAME_KANA, ";
        $query .= "     T2.SEX ";
        $query .= " FROM ";
        $query .= "     SCHNO T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT T4 ON T4.YEAR = T1.YEAR ";
        $query .= "          AND T4.SEMESTER = T1.SEMESTER ";
        $query .= "          AND T4.GRADE = T1.GRADE ";
        $query .= "          AND T4.HR_CLASS = T1.HR_CLASS ";
        $query .= "     LEFT JOIN ADDRESS_DAT L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     RENBAN";

        return $query;
    }
    //ＣＳＶ出力
    function getOutputQueryCsv2($model)
    {
        $query  = "WITH SCHNO AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     ATTENDNO, ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER, ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND GRADE||HR_CLASS = '{$model->field["GRADE_HR_CLASS"]}' ) ";

        $query .= ",STAFF AS ( ";
        $query .= " SELECT ";
        $query .= "     TR_CD1, ";
        $query .= "     YEAR, ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     HR_NAMEABBV ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND GRADE||HR_CLASS = '{$model->field["GRADE_HR_CLASS"]}' ) ";

        $query .= ",ADDRESS_DAT AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.TELNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_ADDRESS_DAT T1 ";
        $query .= " INNER JOIN ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         MAX(ISSUEDATE) AS ISSUEDATE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_ADDRESS_DAT ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= "     ) L1 ON  L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "          AND L1.ISSUEDATE = T1.ISSUEDATE ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     ROW_NUMBER() OVER(ORDER BY ATTENDNO) AS RENBAN, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T4.HR_NAMEABBV, ";
        $query .= "     INT(T1.ATTENDNO) AS ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T2.EMERGENCYTELNO AS TELNO, ";
        $query .= "     EMERGENCYTELNO2 AS TELNO2 ";
        $query .= " FROM ";
        $query .= "     SCHNO T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT T4 ON T4.YEAR = T1.YEAR ";
        $query .= "          AND T4.SEMESTER = T1.SEMESTER ";
        $query .= "          AND T4.GRADE = T1.GRADE ";
        $query .= "          AND T4.HR_CLASS = T1.HR_CLASS ";
        $query .= "     LEFT JOIN ADDRESS_DAT L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     RENBAN ";

        return $query;
    }
    //学級名、担任、副担任の取得
    function getHrClass($model){
        $query .= " SELECT ";
        $query .= "     HR_CLASS, ";
        $query .= "     HR_NAME, ";
        $query .= "     S1.STAFFNAME AS TANNNIN, ";
        $query .= "     S2.STAFFNAME AS HUKUTANNNIN ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     STAFF_MST S1 ";
        $query .= "     ON S1.STAFFCD = T1.TR_CD1 ";
        $query .= " LEFT JOIN ";
        $query .= "     STAFF_MST S2 ";
        $query .= "     ON S2.STAFFCD = T1.SUBTR_CD1 ";
        $query .= " WHERE ";
        if ($model->field['GRADE']) {
            $query .= "     GRADE = '".$model->field['GRADE']."' ";
        } else {
            $query .= "     GRADE||HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        $query .= "     AND YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     HR_CLASS ";
        $query .= "  ";
        
        return $query;
    }
    
    //学校電話番号の取得
    function getSchoolTelNo($model){
        $query .= " SELECT ";
        $query .= "     SCHOOLTELNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHOOL_MST S1 ";
        $query .= "     ON S1.YEAR=T1.YEAR ";
        $query .= "     AND S1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.GRADE = '".substr($model->field["GRADE_HR_CLASS"],0,2)."' ";
        $query .= "     AND S1.SCHOOLCD = '".sprintf("%012d", SCHOOLCD)."' ";
        
        return $query;
    }
}
?>
