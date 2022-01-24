<?php

require_once('for_php7.php');


class knjz100Query extends Query {

    //出身学校年度一覧取得
    function selectFinSchoolQuery($year, $selectFinschoolType)
    {
        $query  = " select T1.FINSCHOOLCD, T1.FINSCHOOL_NAME ";
        $query .= " from V_FINSCHOOL_MST T1 ";
        $query .= " INNER JOIN FINSCHOOL_MST T2 ON T2.FINSCHOOLCD = T1.FINSCHOOLCD ";
        $query .= " where T1.YEAR = '".$year."'"; 
        if ("99" != $selectFinschoolType) {
            $query .= "   and T2.FINSCHOOL_TYPE = '".$selectFinschoolType."'"; 
        }
        $query .= " order by T1.FINSCHOOLCD";
        return $query;
    }

    //学校種別がnullの件数
    function getFinschoolTypeNullCount($year)
    {
        $query  = " select COUNT(*) as count ";
        $query .= " from V_FINSCHOOL_MST T1 ";
        $query .= " INNER JOIN FINSCHOOL_MST T2 ON T2.FINSCHOOLCD = T1.FINSCHOOLCD ";
        $query .= " where T1.YEAR = '".$year."'"; 
        $query .= "   and T2.FINSCHOOL_TYPE is null "; 
        return $query;
    }

    //出身学校LABEL取得
    function getFinSchoolLabel($finschoolcd)
    {
        $query  = " select T1.FINSCHOOLCD || ' ' || T1.FINSCHOOL_NAME AS LABEL ";
        $query .= " from FINSCHOOL_MST T1 ";
        $query .= " where T1.FINSCHOOLCD = '".$finschoolcd."'"; 
        return $query;
    }

    //塾年度一覧取得
    function selectPriSchoolQuery($year)
    {
        $query  = " select PRISCHOOLCD, PRISCHOOL_NAME ";
        $query .= " from V_PRISCHOOL_MST ";
        $query .= " where YEAR = '".$year."'"; 
        $query .= " order by PRISCHOOLCD";
        return $query;
    }

    //校種取得
    function getFinschoolTypeQuery($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " , CASE WHEN NAMECD2 = '1' AND 'P' = '".SCHOOLKIND."' THEN 1 ";
            $query .= "        WHEN NAMECD2 = '2' AND 'J' = '".SCHOOLKIND."' THEN 1 ";
            $query .= "        WHEN NAMECD2 = '3' AND 'H' = '".SCHOOLKIND."' THEN 1 ";
            $query .= "        ELSE 0 ";
            $query .= "   END AS IS_DEFAULT";
        } else {
            $query .= " , 0 AS IS_DEFAULT";
        }
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'L019' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称取得
    function getNameMst($namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //１レコード取得
    function getRow($cd)
    {
        $db = Query::dbCheckOut();

        $query = "SELECT * FROM subclass_mst ".
                 " WHERE SUBCLASSCD = '" .$cd ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

}
?>
