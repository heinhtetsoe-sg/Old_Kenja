<?php

require_once('for_php7.php');

class knjh171Query extends Query {

    //災害帰宅グループを取得
    function getGoHomeGroup() {

        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GO_HOME_GROUP_NO, ";
        $query .= "     T2.GO_HOME_GROUP_NAME, ";
        $query .= "     '［' || T2.MEETING_PLACE || '］' AS MEETING_PLACE ";
        $query .= " FROM ";
        $query .= "     SCHREG_ENVIR_DAT T1 ";
        $query .= "     LEFT JOIN GO_HOME_GROUP_MST T2 ON T1.GO_HOME_GROUP_NO = T2.GO_HOME_GROUP_NO ";
        $query .= " WHERE ";
        $query .= "     T1.GO_HOME_GROUP_NO IS NOT NULL AND ";
        $query .= "     T1.GO_HOME_GROUP_NO <> '00' AND ";
        $query .= "     T1.SCHREGNO IN (SELECT ";
        $query .= "                         SCHREGNO ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_REGD_DAT ";
        $query .= "                     WHERE ";
        $query .= "                         YEAR = '".CTRL_YEAR."' AND ";
        $query .= "                         SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "                     ) ";
        $query .= " ORDER BY ";
        $query .= "     T1.GO_HOME_GROUP_NO ";

        return $query;
    }

    //CVS作成用のQUERY
    function selectCsvQuery($groupno) {

        $query  = " WITH SCH_INFO AS( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T3.HR_NAMEABBV, ";
        $query .= "         substr(T1.ATTENDNO, 2) AS ATTENDNO, ";
        $query .= "         T2.NAME, ";
        $query .= "         CASE WHEN T2.GRD_DIV IS NOT NULL AND T2.GRD_DIV <> '4' AND VALUE(GRD_DATE, '9999-12-31') < '".CTRL_DATE."' THEN '1' END AS IS_GRD ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         LEFT JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "         LEFT JOIN SCHREG_REGD_HDAT T3 ON T1.YEAR = T3.YEAR AND T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "                                          T1.GRADE = T3.GRADE AND T1.HR_CLASS = T3.HR_CLASS ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.RESPONSIBILITY, ";
        $query .= "     T2.HR_NAMEABBV || T2.ATTENDNO AS ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T2.IS_GRD ";
        $query .= " FROM ";
        $query .= "     SCHREG_ENVIR_DAT T1,  ";
        $query .= "     SCH_INFO T2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     T1.GO_HOME_GROUP_NO = '".$groupno."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GO_HOME_GROUP_NO, ";
        $query .= "     T2.GRADE DESC, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO ";

        return $query;
    }
}
?>
