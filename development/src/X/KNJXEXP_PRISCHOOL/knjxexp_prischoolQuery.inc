<?php

require_once('for_php7.php');

class knjxexp_prischoolQuery extends Query {

    //名称マスタ
    function getNameMst($namecd1)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".(CTRL_YEAR + 1)."' AND ";
        $query .= "     NAMECD1 = '{$namecd1}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //左リスト
    function GetPriSchool($model)
    {

        $tableName = "RECRUIT_DAT";

        $where = knjxexp_prischoolQuery::GetPriSchoolearch($model);
        $query  = " WITH STATION AS ( ";
        $query .= " SELECT ";
        $query .= "     LINE_CD, ";
        $query .= "     MAX(LINE_NAME) AS LINE_NAME ";
        $query .= " FROM ";
        $query .= "     STATION_NETMST ";
        $query .= " GROUP BY ";
        $query .= "     LINE_CD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   PRI_CLASS.PRISCHOOLCD, ";
        $query .= "   PRI_CLASS.PRISCHOOL_CLASS_CD, ";
        $query .= "   PRI_M.PRISCHOOL_NAME, ";
        $query .= "   PRI_CLASS.PRISCHOOL_NAME AS PRISCHOOL_CLASS_NAME ";
        $query .= " FROM ";
        $query .= "   PRISCHOOL_CLASS_MST PRI_CLASS ";
        $query .= "   INNER JOIN PRISCHOOL_MST PRI_M ON PRI_CLASS.PRISCHOOLCD = PRI_M.PRISCHOOLCD ";
        $query .= "   LEFT JOIN STATION STATION1 ON PRI_CLASS.ROSEN_1 = STATION1.LINE_CD ";
        $query .= "   LEFT JOIN STATION STATION2 ON PRI_CLASS.ROSEN_2 = STATION2.LINE_CD ";
        $query .= "   LEFT JOIN STATION STATION3 ON PRI_CLASS.ROSEN_3 = STATION3.LINE_CD ";
        $query .= "   LEFT JOIN STATION STATION4 ON PRI_CLASS.ROSEN_4 = STATION4.LINE_CD ";
        $query .= "   LEFT JOIN STATION STATION5 ON PRI_CLASS.ROSEN_5 = STATION5.LINE_CD ";
        $query .= " WHERE ".$where;
        $query .= " ORDER BY ";
        $query .= "   PRI_CLASS.PRISCHOOLCD, ";
        $query .= "   PRI_CLASS.PRISCHOOL_CLASS_CD ";

        return $query;
   }

    function GetPriSchoolearch($model)
    {
        //塾コード
        if ($model->search["PRISCHOOLCD"]) {
            $where .= "   ".$and."PRI_CLASS.PRISCHOOLCD = '".$model->search["PRISCHOOLCD"]."' ";
            $and = "AND ";
        }

        //塾名
        if ($model->search["PRISCHOOL_NAME"]) {
            $where .= "   ".$and."PRI_M.PRISCHOOL_NAME LIKE '%" .str_replace("'","''",$model->search["PRISCHOOL_NAME"]). "%' ";
            $and = "AND ";
        }

        //塾名かな
        if ($model->search["PRISCHOOL_KANA"]) {
            $where .= "   ".$and."PRI_M.PRISCHOOL_KANA LIKE '%" .str_replace("'","''",$model->search["PRISCHOOL_KANA"]). "%' ";
            $and = "AND ";
        }

        //教室名
        if ($model->search["PRISCHOOL_CLASS_NAME"]) {
            $where .= "   ".$and."PRI_CLASS.PRISCHOOL_NAME LIKE '%" .str_replace("'","''",$model->search["PRISCHOOL_CLASS_NAME"]). "%' ";
            $and = "AND ";
        }

        //教室名かな
        if ($model->search["PRISCHOOL_CLASS_KANA"]) {
            $where .= "   ".$and."PRI_CLASS.PRISCHOOL_KANA LIKE '%" .str_replace("'","''",$model->search["PRISCHOOL_CLASS_KANA"]). "%' ";
            $and = "AND ";
        }

        //最寄路線名
        if ($model->search["ROSEN_NAME"]) {
            $where .= "   ".$and."(";
            $where .= "      STATION1.LINE_NAME LIKE '%" .str_replace("'","''",$model->search["ROSEN_NAME"]). "%' ";
            $where .= "   OR STATION2.LINE_NAME LIKE '%" .str_replace("'","''",$model->search["ROSEN_NAME"]). "%' ";
            $where .= "   OR STATION3.LINE_NAME LIKE '%" .str_replace("'","''",$model->search["ROSEN_NAME"]). "%' ";
            $where .= "   OR STATION4.LINE_NAME LIKE '%" .str_replace("'","''",$model->search["ROSEN_NAME"]). "%' ";
            $where .= "   OR STATION5.LINE_NAME LIKE '%" .str_replace("'","''",$model->search["ROSEN_NAME"]). "%' ";
            $where .= "   )";
            $and = "AND ";
        }

        //最寄駅名
        if ($model->search["NEAREST_STATION_NAME"]) {
            $where .= "   ".$and."(";
            $where .= "      PRI_CLASS.NEAREST_STATION_NAME1 LIKE '%" .str_replace("'","''",$model->search["NEAREST_STATION_NAME"]). "%' ";
            $where .= "   OR PRI_CLASS.NEAREST_STATION_NAME2 LIKE '%" .str_replace("'","''",$model->search["NEAREST_STATION_NAME"]). "%' ";
            $where .= "   )";
            $and = "AND ";
        }

        //地区
        if ($model->search["DISTRICTCD"]) {
            $where .= "   ".$and."PRI_CLASS.DISTRICTCD = '".$model->search["DISTRICTCD"]."' ";
            $and = "AND ";
        }

        return $where;
    }

}
?>
