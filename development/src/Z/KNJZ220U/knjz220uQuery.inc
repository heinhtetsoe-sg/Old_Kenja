<?php

require_once('for_php7.php');

class knjz220uQuery extends Query {

    //段階数
    function getAssessLevelCnt($model) {

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     RELATIVEASSESS_RATE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //評定マスタ情報抽出
    function selectQuery($model) {

        $query  = " SELECT ";
        $query .= "     ASSESSLEVEL, ";
        $query .= "     ASSESSRATE ";
        $query .= " FROM ";
        $query .= "     RELATIVEASSESS_RATE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     ASSESSLEVEL DESC ";

        return $query;
    }

    //Insert作成
    function getInsertQuery($model) {

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM RELATIVEASSESS_RATE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        $db->query($query);

        $data = array();
        for ($i = 0; $i < $model->level; $i++) {
            $assesslebel = $model->level - $i;

            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["ASSESSLEVEL"][NUMBER]    = $assesslebel;
            if ($assesslebel == "1") {
                $data["ASSESSRATE"][NUMBER] = 1;
            } else {
                $data["ASSESSRATE"][NUMBER] = $model->field["ASSESSRATE".$assesslebel];
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $query = Query::insertSQL($data, "RELATIVEASSESS_RATE_MST");

            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
?>
