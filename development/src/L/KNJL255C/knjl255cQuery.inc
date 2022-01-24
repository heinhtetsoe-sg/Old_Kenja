<?php

require_once('for_php7.php');


class knjl255cQuery extends Query {

    //出身学校年度一覧取得
    function selectFinSchoolQuery($year)
    {
        $query  = " select FINSCHOOLCD, FINSCHOOL_NAME ";
        $query .= " from V_FINSCHOOL_MST ";
        $query .= " where YEAR = '".($year+1)."'"; 
        $query .= "  AND FINSCHOOLCD IN (SELECT FS_CD ";
        $query .= "                      FROM ENTEXAM_APPLICANTBASE_PRE_DAT ";
        $query .= "                      where YEAR = '".($year+1)."'"; 
        $query .= "                      ) ";
        $query .= " order by FINSCHOOLCD";
        return $query;
    }

    //塾年度一覧取得
    function selectPriSchoolQuery($year)
    {
        $query  = " select PRISCHOOLCD, PRISCHOOL_NAME ";
        $query .= " from V_PRISCHOOL_MST ";
        $query .= " where YEAR = '".($year+1)."'"; 
        $query .= "  AND PRISCHOOLCD IN (SELECT PS_CD ";
        $query .= "                      FROM ENTEXAM_APPLICANTBASE_PRE_DAT ";
        $query .= "                      where YEAR = '".($year+1)."'"; 
        $query .= "                      ) ";
        $query .= " order by PRISCHOOLCD";
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
