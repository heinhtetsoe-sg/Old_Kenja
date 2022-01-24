<?php

require_once('for_php7.php');


class knjz100aQuery extends Query
{

    //出身学校年度一覧取得
    public function selectFinSchoolQuery($year, $selectFinschoolType, $personnel = "")
    {
        $query  = " SELECT ";
        $query .= "     V_FIN.FINSCHOOLCD, ";
        $query .= "     V_FIN.FINSCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     V_FINSCHOOL_MST V_FIN ";
        $query .= "     INNER JOIN FINSCHOOL_MST FIN ON ";
        $query .= "           FIN.FINSCHOOLCD = V_FIN.FINSCHOOLCD ";
        $query .= " WHERE ";
        $query .= "     V_FIN.YEAR         = '".$year."' ";
        if ("99" != $selectFinschoolType) {
            $query .= " AND FIN.FINSCHOOL_TYPE = '".$selectFinschoolType."' ";
        }
        if ($personnel != "") {
            $query .= " AND FIN.FINSCHOOL_STAFFCD IS NOT NULL ";
        }
        $query .= " ORDER BY ";
        $query .= "     V_FIN.FINSCHOOLCD ";
        return $query;
    }

    //塾年度一覧取得
    public function selectPriSchoolQuery($year)
    {
        $query  = " select PRISCHOOLCD, PRISCHOOL_NAME ";
        $query .= " from V_PRISCHOOL_MST ";
        $query .= " where YEAR = '".$year."'";
        $query .= " order by PRISCHOOLCD";
        return $query;
    }

    //１レコード取得
    public function getRow($cd)
    {
        $db = Query::dbCheckOut();

        $query = "SELECT * FROM subclass_mst ".
                 " WHERE SUBCLASSCD = '" .$cd ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    public function getJyoto()
    {
        $query  = " SELECT ";
        $query .= "     CASE NAME1 WHEN 'jyoto' THEN 1 ELSE 0 END AS IS_JYOTO ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";
        return $query;
    }

    //校種取得
    public function getFinschoolTypeQuery($model)
    {
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

    //学校種別がnullの件数
    public function getFinschoolTypeNullCount($year)
    {
        $query  = " select COUNT(*) as count ";
        $query .= " from V_FINSCHOOL_MST T1 ";
        $query .= " INNER JOIN FINSCHOOL_MST T2 ON T2.FINSCHOOLCD = T1.FINSCHOOLCD ";
        $query .= " where T1.YEAR = '".$year."'";
        $query .= "   and T2.FINSCHOOL_TYPE is null ";
        return $query;
    }
}
