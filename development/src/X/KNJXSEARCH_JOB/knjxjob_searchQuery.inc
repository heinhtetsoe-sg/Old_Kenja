<?php

require_once('for_php7.php');

class knjxjob_searchQuery extends Query {
    //検索結果
    function getSchoolList($model){
        $query .= " SELECT ";
        $query .= "     T1.JOBTYPE_SNAME, ";
        $query .= "     L1.JOBTYPE_LNAME, ";
        $query .= "     T1.JOBTYPE_LCD || '|' || T1.JOBTYPE_MCD || '|' || T1.JOBTYPE_SCD || '|' || T1.JOBTYPE_SNAME AS VALUE ";
        $query .= " FROM ";
        $query .= "     JOBTYPE_S_MST T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     JOBTYPE_L_MST L1 ON L1.JOBTYPE_LCD = T1.JOBTYPE_LCD ";
        $query .= " WHERE ";
        if ($model->field["SORT_TYPE"] == '1') { //1：部分一致 2：先頭一致
            $query .= "     T1.JOBTYPE_SNAME      LIKE '%{$model->field["JOBTYPE_SNAME"]}%' OR ";
            $query .= "     T1.JOBTYPE_SNAME_KANA LIKE '%{$model->field["JOBTYPE_SNAME"]}%' ";
        } else {
            $query .= "     T1.JOBTYPE_SNAME      LIKE '{$model->field["JOBTYPE_SNAME"]}%' OR ";
            $query .= "     T1.JOBTYPE_SNAME_KANA LIKE '{$model->field["JOBTYPE_SNAME"]}%' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.JOBTYPE_SNAME_KANA, ";
        $query .= "     T1.JOBTYPE_LCD ";

        return $query;
    }
}
?>
