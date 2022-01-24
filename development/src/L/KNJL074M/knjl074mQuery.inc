<?php

require_once('for_php7.php');

class knjl074mQuery extends Query {

    //年度コンボ
    function getTestYear() {
        $query  = " SELECT ";
        $query .= "     smallint(YEAR) + 1 AS LABEL, ";
        $query .= "     smallint(YEAR) + 1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " ORDER BY ";
        $query .= "     YEAR DESC ";

        return $query;
    }

    //一覧
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     T1.NAMECD2 AS JUDGEMENT, ";
        $query .= "     T1.NAME1 AS JUDGEMENT_NAME, ";
        $query .= "     L1.JUDGE_CNT ";
        $query .= " FROM ";
        $query .= "     NAME_MST T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_JUDGEMENT_HISTORY_DAT L1 ";
        $query .= "                           ON  L1.ENTEXAMYEAR  = '{$model->test_year}' ";
        $query .= "                           AND L1.JUDGEMENT = T1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.NAMECD1 = 'L013' AND ";
        $query .= "     T1.NAMECD2 IN ('1', '2', '4') ";
        $query .= " ORDER BY ";
        $query .= "     T1.NAMECD2 ";

        return $query;
    }

    function updateEntexamApplicantbaseDat($model) {
        $db = Query::dbCheckOut();
        foreach ($model->field as $key => $val) {
            if (!preg_match('/^JUDGE_CNT_/', $key)) continue;
            list($dummy, $dummy, $judgement) = preg_split("/_/", $key);
            //削除
            $query  = "DELETE FROM ENTEXAM_JUDGEMENT_HISTORY_DAT";
            $query .= " WHERE ENTEXAMYEAR = '{$model->test_year}'";
            $query .= "   AND JUDGEMENT   = '{$judgement}'";
            $db->query($query);
            //追加
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]  = $model->test_year;
            $data["JUDGEMENT"][TEXT]    = $judgement;
            $data["JUDGE_CNT"][NUMBER]  = $val;
            $data["REGISTERCD"][TEXT]   = STAFFCD ;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $query = Query::insertSQL($data, "ENTEXAM_JUDGEMENT_HISTORY_DAT");
            $db->query($query);
        }
        Query::dbCheckIn($db);
    }
}
?>
