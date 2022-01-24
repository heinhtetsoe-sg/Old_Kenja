<?php

require_once('for_php7.php');
class knja353query extends Query
{
    //CSVデータ出力
    public function selectQuery($model)
    {
        $querys = array();
        $targetList = array('FED_KTSNDB_','FED_KKOTOUDB_','FED_KIKANDB_','FED_KSOGODB_','FED_KNHNBDB_','FED_KMIRAIDB_','FED_KNOZOMDB_');
        for ($i = 0; $i < get_count($targetList); $i++) {
            $query  = " SELECT ";
            $query .= "     NMST.NAME1, ";
            $query .= "     SMST.SCHOOLNAME1, ";
            $query .= "     T2.SCHREGNO, ";
            $query .= "     T3.GRADE_NAME1, ";
            $query .= "     T4.HR_NAME, ";
            $query .= "     T5.COURSECODENAME, ";
            $query .= "     T1.ATTENDNO, ";
            $query .= "     T2.NAME, ";
            $query .= "     T2.NAME_KANA, ";
            $query .= "     T2.SEX, ";
            $query .= "     T2.BIRTHDAY, ";
            $query .= "     T6.ZIPCD, ";
            $query .= "     T6.ADDR1, ";
            $query .= "     T6.ADDR2, ";
            $query .= "     T6.TELNO, ";
            $query .= "     T6.ISSUEDATE, ";
            $query .= "     T7.ISSUEDATE AS ISSUEDATE2, ";
            $query .= "     T8.GUARD_NAME, ";
            $query .= "     T8.GUARD_KANA, ";
            $query .= "     T9.ISSUEDATE AS ISSUEDATE3, ";
            $query .= "     T10.ENT_DATE, ";
            $query .= "     T10.ENT_DIV, ";
            $query .= "     CASE WHEN T10.GRD_DIV IN ('1', '4', '5') THEN T10.GRD_DATE ELSE NULL END AS GRD_DATE, ";
            $query .= "     CASE WHEN VALUE(T10.GRD_DIV, '1') NOT IN ('1', '4', '5') THEN T10.GRD_DIV ELSE '' END AS JYOSEKI_DIV, ";
            $query .= "     CASE WHEN VALUE(T10.GRD_DIV, '1') NOT IN ('1', '4', '5') THEN T10.GRD_DATE ELSE NULL END AS JYOSEKI_DATE, ";
            $query .= "     T11.TRANSFERCD, ";
            $query .= "     T11.TRANSFER_SDATE, ";
            $query .= "     T11.TRANSFER_EDATE, ";
            $query .= "     T11.TRANSFERPLACE, ";
            $query .= "     CASE WHEN T12.SCHREGNO IS NOT NULL THEN '1' ELSE '' END AS GENRYUU, ";
            $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
            $query .= " FROM ";
            $query .= "     {$targetList[$i]}SCHREG_REGD_DAT T1 ";
            $query .= " LEFT JOIN {$targetList[$i]}NAME_MST NMST ";
            $query .= "      ON NMST.NAMECD1 = 'A054' ";
            $query .= "     AND NMST.NAMECD2 = '00' ";
            $query .= " LEFT JOIN {$targetList[$i]}SCHREG_BASE_MST T2 ";
            $query .= "     ON T1.SCHREGNO = T2.SCHREGNO ";
            $query .= " LEFT JOIN {$targetList[$i]}SCHREG_REGD_GDAT T3 ";
            $query .= "     ON T1.YEAR = T3.YEAR ";
            $query .= "     AND T1.GRADE = T3.GRADE ";
            $query .= " LEFT JOIN {$targetList[$i]}SCHOOL_MST SMST ";
            $query .= "     ON T1.YEAR = SMST.YEAR ";
            $query .= "     AND SMST.SCHOOLCD = '000000000000' ";
            $query .= "     AND T3.SCHOOL_KIND = SMST.SCHOOL_KIND ";
            $query .= " LEFT JOIN {$targetList[$i]}SCHREG_REGD_HDAT T4 ";
            $query .= "     ON T1.YEAR = T4.YEAR ";
            $query .= "     AND T1.SEMESTER = T4.SEMESTER ";
            $query .= "     AND T1.GRADE = T4.GRADE ";
            $query .= "     AND T1.HR_CLASS = T4.HR_CLASS ";
            $query .= " LEFT JOIN {$targetList[$i]}COURSECODE_MST T5 ";
            $query .= "     ON T1.COURSECODE = T5.COURSECODE ";
            $query .= " LEFT JOIN {$targetList[$i]}SCHREG_ADDRESS_DAT T6 ";
            $query .= "     ON T1.SCHREGNO = T6.SCHREGNO ";
            $query .= "     AND T6.ISSUEDATE = (SELECT MAX(ISSUEDATE) FROM {$targetList[$i]}SCHREG_ADDRESS_DAT WHERE SCHREGNO = T1.SCHREGNO) ";
            $query .= " LEFT JOIN {$targetList[$i]}SCHREG_BASE_HIST_DAT T7 ";
            $query .= "     ON T1.SCHREGNO = T7.SCHREGNO ";
            $query .= "     AND T7.ISSUEDATE = (SELECT MAX(ISSUEDATE) FROM {$targetList[$i]}SCHREG_BASE_HIST_DAT WHERE SCHREGNO = T1.SCHREGNO) ";
            $query .= " LEFT JOIN {$targetList[$i]}GUARDIAN_DAT T8 ";
            $query .= "     ON T1.SCHREGNO = T8.SCHREGNO ";
            $query .= " LEFT JOIN {$targetList[$i]}GUARDIAN_HIST_DAT T9 ";
            $query .= "     ON T1.SCHREGNO = T9.SCHREGNO ";
            $query .= "     AND T9.ISSUEDATE = (SELECT MAX(ISSUEDATE) FROM {$targetList[$i]}GUARDIAN_HIST_DAT WHERE SCHREGNO = T1.SCHREGNO) ";
            $query .= " LEFT JOIN {$targetList[$i]}SCHREG_ENT_GRD_HIST_DAT T10 ";
            $query .= "     ON T1.SCHREGNO = T10.SCHREGNO ";
            $query .= "     AND T3.SCHOOL_KIND = T10.SCHOOL_KIND ";
            $query .= " LEFT JOIN {$targetList[$i]}SCHREG_TRANSFER_DAT T11 ";
            $query .= "     ON T1.SCHREGNO = T11.SCHREGNO ";
            $query .= "     AND T11.TRANSFERCD = '1' ";
            $query .= "     AND T11.TRANSFER_SDATE = (SELECT MAX(TRANSFER_SDATE) FROM {$targetList[$i]}SCHREG_TRANSFER_DAT WHERE SCHREGNO = T1.SCHREGNO AND T11.TRANSFERCD = '1') ";
            $query .= " LEFT JOIN  ";
            $query .= "     (SELECT DISTINCT ";
            $query .= "         SCHREGNO ";
            $query .= "     FROM ";
            $query .= "         (SELECT ";
            $query .= "             YEAR, ";
            $query .= "             SCHREGNO, ";
            $query .= "             COUNT(GRADE) AS CNT ";
            $query .= "         FROM ";
            $query .= "             (SELECT DISTINCT ";
            $query .= "                 YEAR, ";
            $query .= "                 GRADE, ";
            $query .= "                 SCHREGNO ";
            $query .= "             FROM ";
            $query .= "                 {$targetList[$i]}SCHREG_REGD_DAT ";
            $query .= "             GROUP BY ";
            $query .= "                 SCHREGNO, ";
            $query .= "                 YEAR, ";
            $query .= "                 GRADE ";
            $query .= "             ) ";
            $query .= "         GROUP BY ";
            $query .= "             YEAR, ";
            $query .= "             SCHREGNO ";
            $query .= "         HAVING ";
            $query .= "             COUNT(GRADE)>1 ";
            $query .= "         ) ";
            $query .= "     ) T12 ON T1.SCHREGNO = T12.SCHREGNO ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '{$model->year}' ";
            $query .= "     AND T1.SEMESTER = (SELECT MAX(SEMESTER) FROM {$targetList[$i]}SCHREG_REGD_DAT WHERE YEAR = '{$model->year}') ";
            $query .= " ORDER BY ";
            $query .= "     T2.SCHREGNO ";

            $querys[] = $query;
        }
        return '(' . implode(') UNION ALL (', $querys) . ')';
    }
}
