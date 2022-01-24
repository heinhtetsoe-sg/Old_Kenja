<?php

require_once('for_php7.php');
class knjd_behavior_sdQuery extends Query {

    //生徒情報
    function getSchInfo($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //学期取得
    function getSemester($model) {
        if ($model->call_prg == "KNJD137K") {
            $query  = " SELECT ";
            $query .= "   NM.NAMECD2 AS VALUE, ";
            $query .= "   NM.NAME1 AS LABEL ";
            $query .= " FROM ";
            $query .= "   NAME_MST NM ";
            $query .= " WHERE ";
            $query .= "   NM.NAMECD1 = 'DP78' ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        } else {
            if ($model->call_prg == "KNJD137L") {
                $query  = "WITH MAXSEMESTER AS (";
                $query .= " SELECT ";
                $query .= "   WKSM.YEAR, ";
                $query .= "   MAX(SEMESTER) AS MAXSEM ";
                $query .= " FROM ";
                $query .= "     SEMESTER_MST WKSM ";
                $query .= " WHERE ";
                $query .= "     WKSM.YEAR = '".$model->exp_year."' ";
                $query .= "     AND WKSM.SEMESTER <> '9' ";
                $query .= " GROUP BY ";
                $query .= "    WKSM.YEAR ";
                $query .= " ) ";
                $query .= " SELECT ";
            } else {
                $query  = " SELECT ";
            }
            $query .= "     SM.SEMESTER AS VALUE, ";
            $query .= "     SM.SEMESTERNAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SEMESTER_MST SM ";
            if ($model->call_prg == "KNJD137L") {
                $query .= "     LEFT JOIN MAXSEMESTER MSS ";
                $query .= "        ON SM.YEAR = MSS.YEAR ";
                $query .= "       AND SM.SEMESTER = MSS.MAXSEM ";
            }
            $query .= " WHERE ";
            $query .= "     SM.YEAR = '".$model->exp_year."' ";
            if ($model->call_prg == "KNJD137L") {
                $query .= "     AND MSS.MAXSEM IS NULL "; //「"9"以外の最大値」を除外する。"9"は取得。
            } else {
                $query .= "     AND SM.SEMESTER <> '9' ";
            }
            $query .= " ORDER BY ";
            $query .= "     SM.SEMESTER ";
        }

        return $query;
    }

    //項目取得
    function getNameMst($model, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD1 ";

        return $query;
    }

    //項目取得(BEHAVIOR_SEMES_MST)
    function getBehaviorSemesMst($model)
    {
        $query  = "  SELECT ";
        $query .= "    CODE, ";
        $query .= "    VIEWNAME ";
        $query .= "  FROM ";
        $query .= "    BEHAVIOR_SEMES_MST ";
        $query .= "  WHERE ";
        $query .= "    YEAR = '".$model->exp_year."' ";
        $query .= "    AND GRADE = '".$model->grade."' ";
        $query .= "    AND CODE BETWEEN '01' AND '10' ";
        $query .= "  ORDER BY ";
        $query .= "    CODE ";

        return $query;
    }

    //行動記録の取得
    function getBehavior($model)
    {
        $query  = " SELECT ";
        $query .= "     CODE, ";
        $query .= "     RECORD ";
        $query .= " FROM ";
        $query .= "     BEHAVIOR_SEMES_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' ";
        $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     CODE ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($model)
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
