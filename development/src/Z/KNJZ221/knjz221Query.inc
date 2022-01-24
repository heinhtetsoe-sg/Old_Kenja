<?php

require_once('for_php7.php');

class knjz221Query extends Query {
    //年度取得
    function getYear() {
        $query  = " SELECT ";
        $query .= "     '".CTRL_YEAR."' AS LABEL, ";
        $query .= "     '".CTRL_YEAR."' AS VALUE ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " UNION    ";
        $query .= " SELECT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     ATTEND_SCORE_CREDIT_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //データ取得 -- ATTEND_SCORE_CREDIT_MST
    function getAttendScoreCreditMst($model, $flg) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ATTEND_SCORE_CREDIT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        if ($flg == "right") {
            $query .= "     AND SEMESTER    = '".$model->semester."' ";
            $query .= "     AND CREDIT      =  ".$model->credit." ";
            $query .= " ORDER BY ";
            $query .= "     ATTEND_SCORE DESC ";
        }

        return $query;
    }

    //学期取得
    function getSemester($model, $flg="") {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME, ";
        $query .= "     SEMESTER ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' AND ";
        $query .= "     SEMESTER <> '9' ";
        if ($flg) {
            $query .= " AND SEMESTER = '".$model->semester."' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     SEMESTER ";
        }

        return $query;
    }

    //MAX単位数取得
    function getMaxCredit($model) {
        $query .= " SELECT ";
        $query .= "     MAX(CREDITS) ";
        $query .= " FROM ";
        $query .= "     CREDIT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";

        return $query;
    }

    //更新処理
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query  = " DELETE FROM ";
        $query .= "     ATTEND_SCORE_CREDIT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->year."' AND ";
        $query .= "     SEMESTER    = '".$model->semester."' AND ";
        $query .= "     CREDIT      =  ".$model->credit." ";

        $db->query($query);

        //追加
        for ($score=0; $score <= 10; $score++) {
            $data = array();
            $data["YEAR"][TEXT]             = $model->year;
            $data["SEMESTER"][TEXT]         = $model->semester;
            $data["CREDIT"][NUMBER]         = $model->credit;
            $data["ATTEND_SCORE"][NUMBER]   = $score;
            $data["KEKKA_LOW"][NUMBER]      = $model->field["KEKKA_LOW".$score];
            $data["KEKKA_HIGH"][NUMBER]     = $model->field["KEKKA_HIGH".$score];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "ATTEND_SCORE_CREDIT_MST");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //前年度からのコピー
    function &getCopyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $this_year  = CTRL_YEAR;
        $pre_year   = CTRL_YEAR - 1;

        $query  = " INSERT INTO ATTEND_SCORE_CREDIT_MST ";
        $query .= " SELECT ";
        $query .= "     '".$this_year."', ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.CREDIT, ";
        $query .= "     T1.ATTEND_SCORE, ";
        $query .= "     T1.KEKKA_LOW, ";
        $query .= "     T1.KEKKA_HIGH, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     ATTEND_SCORE_CREDIT_MST T1 ";
        $query .= "     INNER JOIN SEMESTER_MST L1 ";
        $query .= "          ON L1.YEAR     = '".$this_year."' ";
        $query .= "         AND T1.SEMESTER = L1.SEMESTER ";
        $query .= "         AND L1.SEMESTER <> '9' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".$pre_year."' AND ";
        $query .= "     T1.CREDIT  <= ".$model->maxCredit." AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     ATTEND_SCORE_CREDIT_MST S1 ";
        $query .= "                 WHERE ";
        $query .= "                     S1.YEAR     = '".$this_year."' AND ";
        $query .= "                     S1.SEMESTER = T1.SEMESTER AND ";
        $query .= "                     S1.CREDIT   = T1.CREDIT ";
        $query .= "                 ) ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
   }
}
?>
