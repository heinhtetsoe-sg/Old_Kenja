<?php

require_once('for_php7.php');

class knje452Query extends Query {

    //設問取得
    function getQuestion($model)
    {
        $query  =  " SELECT ";
        $query .=  "    * ";
        $query .=  " FROM ";
        $query .=  "    ASSESSMENT_Q_MST ";
        $query .=  " WHERE ";
        $query .=  "    YEAR = '".CTRL_YEAR."' ";
        $query .=  "    AND ASSESS_DIV = '{$model->assessDiv}' ";
        $query .=  " ORDER BY ";
        $query .=  "    ASSESS_CD ";

        return $query;
    }

    //Insert作成
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  =  " DELETE FROM ";
        $query .=  "    ASSESSMENT_Q_MST ";
        $query .=  " WHERE ";
        $query .=  "    YEAR = '".CTRL_YEAR."' ";
        $query .=  "    AND ASSESS_DIV = '{$model->assessDiv}' ";

        $db->query($query);

        for ($i = 1; $i <= $model->assessCnt[$model->assessDiv]; $i++) {
            if (!$model->field["QUESTION".$i]) {
                continue;
            }
            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["ASSESS_DIV"][TEXT]   = $model->assessDiv;
            $data["ASSESS_CD"][TEXT]    = $i;
            $data["QUESTION"][TEXT]     = $model->field["QUESTION".$i];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";

            $query = Query::insertSQL($data, "ASSESSMENT_Q_MST");

            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    //前年度データ取得
    function getCopyData($year, $flg) {
        $query  =  " SELECT ";
        if ($flg == "cnt") {
            $query .=  "    COUNT(*) ";
        } else {
            $query .=  "    * ";
        }
        $query .=  " FROM ";
        $query .=  "    ASSESSMENT_Q_MST ";
        $query .=  " WHERE ";
        $query .=  "    YEAR = '".$year."' ";

        return $query;
    }

    //前年度コピー処理
    function &getCopyQuery() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query  =  " DELETE FROM ";
        $query .=  "    ASSESSMENT_Q_MST ";
        $query .=  " WHERE ";
        $query .=  "    YEAR = '".CTRL_YEAR."' ";
        $db->query($query);

        //前年度データ取得
        $pre_year = CTRL_YEAR - 1;
        $query = knje452Query::getCopyData($pre_year, "list");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //追加
            $data = array();
            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["ASSESS_DIV"][TEXT]   = $row["ASSESS_DIV"];
            $data["ASSESS_CD"][TEXT]    = $row["ASSESS_CD"];
            $data["QUESTION"][TEXT]     = $row["QUESTION"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";

            $query = Query::insertSQL($data, "ASSESSMENT_Q_MST");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
