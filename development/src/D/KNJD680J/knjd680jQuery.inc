<?php

require_once('for_php7.php');

class knjd680jQuery extends Query {

    //項目取得
    function getNameMst($model, $namecd1)
    {
        $setYear = $model->exp_year ? $model->exp_year : CTRL_YEAR;
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$setYear."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //行動記録の取得
    function getBehavior($model)
    {
        $setYear = $model->exp_year ? $model->exp_year : CTRL_YEAR;
        $query  = " SELECT ";
        $query .= "     CODE, ";
        $query .= "     RECORD ";
        $query .= " FROM ";
        $query .= "     BEHAVIOR_SEMES_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$setYear}' ";
        $query .= "     AND SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     CODE ";

        return $query;
    }

    function &getInsertBehaviorQuery($model)
    {
        $db = Query::dbCheckOut();

        //BEHAVIOR_SEMES_DAT削除
        $query  = " DELETE FROM ";
        $query .= "     BEHAVIOR_SEMES_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' ";
        $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";

        $db->query($query);

        //BEHAVIOR_SEMES_DAT追加
        foreach ($model->itemArray as $key => $val) {

            if ($model->Properties["knjdBehaviorsd_UseText"] != "1" && $model->record["RECORD"][$key] != "1") {
                continue;
            }

            $data["YEAR"][TEXT]         = $model->exp_year;
            $data["SEMESTER"][TEXT]     = $model->field["SEMESTER"];
            $data["SCHREGNO"][TEXT]     = $model->schregno;
            $data["CODE"][TEXT]         = sprintf("%02d", $key);
            $data["RECORD"][TEXT]       = $model->record["RECORD"][$key];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $db->query(Query::insertSQL($data, "BEHAVIOR_SEMES_DAT"));
        }

        Query::dbCheckIn($db);
        return ;
    }

}
?>
