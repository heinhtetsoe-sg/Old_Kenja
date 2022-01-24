<?php

require_once('for_php7.php');

class knjz051_3aQuery extends Query {

    //年度取得
    function getYear($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     MAJOR_YDAT ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";
        
        return $query;
    }

    //学校マスタから学校区分取得
    function getSchoolDiv($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHOOLDIV || ':' || L1.NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST T1";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'Z001' ";
        $query .= "                      AND L1.NAMECD2 = T1.SCHOOLDIV ";
        $query .= " WHERE ";
        $query .= "      T1.YEAR = '".$model->field["YEAR"]."' ";

        return $query;
    }

    //データ取得
    function getData($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.*, ";
        $query .= "     L1.COURSENAME, ";
        $query .= "     L2.NAME1 ";
        $query .= " FROM ";
        $query .= "     V_MAJOR_MST T1 ";
        $query .= "     LEFT JOIN COURSE_MST L1 ON L1.COURSECD = T1.COURSECD ";
        $query .= "     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'Z001' ";
        $query .= "                          AND L2.NAMECD2 = T1.SCHOOLDIV ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->field["YEAR"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD ";
        
        return $query;
    }

    //１レコード取得
    function getRow($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.*, ";
        $query .= "     L1.COURSENAME ";
        $query .= " FROM ";
        $query .= "     V_MAJOR_MST T1 ";
        $query .= "     LEFT JOIN COURSE_MST L1 ON L1.COURSECD = T1.COURSECD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".$model->year."' AND ";
        $query .= "     T1.COURSECD = '".$model->coursecd."' AND ";
        $query .= "     T1.MAJORCD  = '".$model->majorcd."' ";

        return $query;
    }

    //学校区分取得
    function getMajorSchoolDiv() {
        $query  = " SELECT  ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || '：' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z001' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        
        return $query;
    }

    //UPDATE
    function &getUpdateQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $data["SCHOOLDIV"][TEXT]    = $fields["SCHOOLDIV"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $where  = " WHERE ";
        $where .= "     YEAR     = '".$model->year."' AND ";
        $where .= "     COURSECD = '".$model->coursecd."' AND ";
        $where .= "     MAJORCD  = '".$model->majorcd."' ";

        $query = Query::updateSQL($data, "MAJOR_YDAT", $where);

        $db->query($query);
        Query::dbCheckIn($db);

        return;
    }
}
?>
