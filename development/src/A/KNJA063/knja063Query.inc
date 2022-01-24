<?php

require_once('for_php7.php');

class knja063Query extends Query {

    //年度・学期コンボ
    function getTerm($model) {
        $year = CTRL_YEAR;
        $nextyear = CTRL_YEAR + 1;

        $query  = " SELECT ";
        $query .= "     YEAR || '年度　' || SEMESTERNAME AS LABEL, ";
        $query .= "     YEAR || '-' || SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SEMESTER < '9' ";
        //次年度の1学期を追加表示
        if (CTRL_SEMESTER == $model->control["学期数"]) { //最終学期のとき
            $query .= " UNION ALL ";
            $query .= " SELECT ";
            $query .= "     YEAR || '年度　' || SEMESTERNAME AS LABEL, ";
            $query .= "     YEAR || '-' || SEMESTER AS VALUE ";
            $query .= " FROM ";
            $query .= "     SEMESTER_MST ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$nextyear}' ";
            $query .= "     AND SEMESTER = '1' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //左クラスコンボ
    function getGhrCd($model) {
        $query  = " SELECT ";
        $query .= "     GHR_NAME AS LABEL, ";
        $query .= "     GHR_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GHR_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
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
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //左生徒リスト
    function getGhrStudents($model, $flg="") {
        $query  = " SELECT ";
        if (!$flg) {
            $query .= "     T4.SCHREGNO, ";
            $query .= "     T1.GRADE, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.ATTENDNO, ";
            $query .= "     T2.HR_NAME, ";
            $query .= "     T4.GHR_CD, ";
            $query .= "     T4.GHR_ATTENDNO, ";
            $query .= "     T5.GHR_NAME, ";
            $query .= "     T3.NAME ";
        } else {
            $query .= "     COUNT(*) AS CNT ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GHR_DAT T4 ";
        $query .= "     INNER JOIN SCHREG_REGD_GHR_HDAT T5 ON T5.YEAR = T4.YEAR AND T5.SEMESTER = T4.SEMESTER AND T5.GHR_CD = T4.GHR_CD ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T1 ON T1.SCHREGNO = T4.SCHREGNO AND T1.YEAR = T4.YEAR AND T1.SEMESTER = T4.SEMESTER ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = T4.YEAR AND T2.SEMESTER = T4.SEMESTER AND T2.GRADE = T1.GRADE AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T4.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T4.YEAR = '{$model->year}' ";
        $query .= "     AND T4.SEMESTER = '{$model->semester}' ";
        $query .= "     AND T4.GHR_CD = '{$model->ghr_cd}' ";
        if (!$flg) {
            $query .= " ORDER BY ";
            $query .= "     T4.GHR_CD, ";
            $query .= "     T4.GHR_ATTENDNO, ";
            $query .= "     T1.GRADE, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.ATTENDNO ";
        }
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
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->semester}' ";
        $query .= "     AND T1.GRADE || T1.HR_CLASS = '{$model->hr_class}' ";
        //但し、左側リストにいる人(右側から左側へ移動しただけの生徒も含む)と
        //SCHREG_REGD_GHR_DATに登録されている人(GHR割振り済みの生徒)は除く
        $query .= "     AND T1.SCHREGNO NOT IN ('" .str_replace("," ,"','" ,$model->selectdata). "') ";
        $query .= "     AND T1.SCHREGNO NOT IN ( ";
        $query .= "         SELECT ";
        $query .= "             T4.SCHREGNO ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_GHR_DAT T4 ";
        $query .= "         WHERE ";
        $query .= "             T4.YEAR = '{$model->year}' ";
        $query .= "             AND T4.SEMESTER = '{$model->semester}' ";
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

        //②既に登録済み生徒のうち、左のリストにいない生徒を削除する
        //（つまり、左から右へ移動した生徒を削除する）
        $query  = " DELETE FROM ";
        $query .= "     SCHREG_REGD_GHR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $query .= "     AND GHR_CD = '{$model->ghr_cd}' ";
        $query .= "     AND SCHREGNO NOT IN ('" .str_replace("," ,"','" ,$model->selectdata). "') ";
        $db->query($query);

        //③左のリストの生徒のうち、未登録生徒を追加する
        //（つまり、右から左へ移動した生徒を追加する）
        $query  = " INSERT INTO SCHREG_REGD_GHR_DAT ( ";
        $query .= " SCHREGNO, ";
        $query .= " YEAR, ";
        $query .= " SEMESTER, ";
        $query .= " GHR_CD, ";
        $query .= " REGISTERCD, ";
        $query .= " UPDATED ";
        $query .= " ) ";
        $query .= "   (SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER, ";
        $query .= "     '{$model->ghr_cd}', ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     SYSDATE() ";
        $query .= "   FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= "   WHERE ";
        $query .= "     SCHREGNO IN ('" .str_replace("," ,"','" ,$model->selectdata). "') ";
        $query .= "     AND YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        //登録済み生徒は除く
        $query .= "     AND SCHREGNO NOT IN ( ";
        $query .= "         SELECT ";
        $query .= "             SCHREGNO ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_GHR_DAT ";
        $query .= "         WHERE ";
        $query .= "             YEAR = '{$model->year}' ";
        $query .= "             AND SEMESTER = '{$model->semester}' ";
        $query .= "     ) ";
        $query .= "   )  ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
