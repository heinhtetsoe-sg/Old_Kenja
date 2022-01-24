<?php

require_once('for_php7.php');

class knjd452Query extends Query {

    //左クラスコンボ
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //右クラスコンボ
    function getHrClass($model) {
        $query  = " SELECT ";
        $query .= "     HR_NAME AS LABEL, ";
        $query .= "     GRADE || HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND GRADE = '{$model->grade}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //左生徒リスト
    function getGhrStudents($model, $flg="") {
        $query  = " SELECT ";
        $query .= "     T4.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.HR_NAME, ";
        $query .= "     T3.NAME ";
        $query .= " FROM ";
        $query .= "     EDUCATION_GUIDANCE_SCHREG_DAT T4 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T1 ON T1.SCHREGNO = T4.SCHREGNO AND T1.YEAR = T4.YEAR AND T1.SEMESTER = T4.SEMESTER ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = T4.YEAR AND T2.SEMESTER = T4.SEMESTER AND T2.GRADE = T1.GRADE AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T4.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T4.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T4.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T2.GRADE = '{$model->grade}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";
        return $query;
    }

    //右生徒リスト
    function getHrStudents($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.HR_NAME, ";
        $query .= "     T3.NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER AND T2.GRADE = T1.GRADE AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.GRADE || T1.HR_CLASS = '{$model->hr_class}' ";
        //但し、左側リストにいる人(右側から左側へ移動しただけの生徒も含む)と
        //EDUCATION_GUIDANCE_SCHREG_DATに登録されている人(GHR割振り済みの生徒)は除く
        $query .= "     AND T1.SCHREGNO NOT IN ('" .str_replace("," ,"','" ,$model->selectdata). "') ";
        $query .= "     AND T1.SCHREGNO NOT IN ( ";
        $query .= "         SELECT ";
        $query .= "             T4.SCHREGNO ";
        $query .= "         FROM ";
        $query .= "             EDUCATION_GUIDANCE_SCHREG_DAT T4 ";
        $query .= "         WHERE ";
        $query .= "             T4.YEAR = '".CTRL_YEAR."' ";
        $query .= "             AND T4.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";
        return $query;
    }

    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //①左のリストの生徒のうち、既に登録済み生徒は何もしない
        //（つまり、最初から左にいる生徒は何もしない）

        //②左のリストの生徒のうち、未登録生徒を追加する
        $query  = " INSERT INTO EDUCATION_GUIDANCE_SCHREG_DAT ( ";
        $query .= " SCHREGNO, ";
        $query .= " YEAR, ";
        $query .= " SEMESTER, ";
        $query .= " REGISTERCD, ";
        $query .= " UPDATED ";
        $query .= " ) ";
        $query .= "   (SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     SYSDATE() ";
        $query .= "   FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= "   WHERE ";
        $query .= "     SCHREGNO IN ('" .str_replace("," ,"','" ,$model->selectdata). "') ";
        $query .= "     AND YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        //登録済み生徒は除く
        $query .= "     AND SCHREGNO NOT IN ( ";
        $query .= "         SELECT ";
        $query .= "             SCHREGNO ";
        $query .= "         FROM ";
        $query .= "             EDUCATION_GUIDANCE_SCHREG_DAT ";
        $query .= "         WHERE ";
        $query .= "             YEAR = '".CTRL_YEAR."' ";
        $query .= "             AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     ) ";
        $query .= "   )  ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
