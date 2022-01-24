<?php

require_once('for_php7.php');

class knjz050_3Query extends Query {

    //MAJOR_YDAT 年度取得
    function getYear($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "      YEAR AS LABEL, ";
        $query .= "      YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "      MAJOR_YDAT ";
        $query .= " ORDER BY ";
        $query .= "      VALUE DESC";
        
        return $query;
    }
    
    //学校区分取得
    function getSchoolDiv($model, $flg)
    {
        $query  = " SELECT ";
        if (!$flg) {
            $query .= "      T1.SCHOOLDIV || '：' || L1.NAME1 AS VALUE ";
        } else {
            $query .= "      T1.SCHOOLDIV ";
        }
        $query .= " FROM ";
        $query .= "      SCHOOL_MST T1";
        $query .= " LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'Z001' ";
        $query .= "                      AND L1.NAMECD2 = T1.SCHOOLDIV ";
        $query .= " WHERE ";
        $query .= "      T1.YEAR = '".$model->field["YEAR"]."' ";

        return $query;
    }

    //MAJOR_MST データ取得
    function getData($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "      T1.*, ";
        $query .= "      T1.COURSECD || T1.MAJORCD AS COURSE_MAJORCD, ";
        $query .= "      L1.COURSENAME || '　' || L0.MAJORNAME AS COURSE_MAJORNAME, ";
        $query .= "      T1.SCHOOLDIV || '：' || L2.NAME1 AS SCHOOLDIV_NAME ";
        $query .= " FROM ";
        $query .= "      MAJOR_YDAT T1 ";
        $query .= " LEFT JOIN MAJOR_MST L0  ON L0.COURSECD = T1.COURSECD ";
        $query .= "                        AND L0.MAJORCD  = T1.MAJORCD ";
        $query .= " LEFT JOIN COURSE_MST L1 ON L1.COURSECD = T1.COURSECD ";
        $query .= " LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'Z001' ";
        $query .= "                      AND L2.NAMECD2 = T1.SCHOOLDIV ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->field["YEAR"]."' ";
        $query .= " ORDER BY ";
        $query .= "     COURSE_MAJORCD ";
        
        return $query;
    }

    //１レコード取得
    function getRow($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT DISTINCT ";
        $query .= "      T1.*, ";
        $query .= "      T1.COURSECD || T1.MAJORCD AS COURSE_MAJORCD, ";
        $query .= "      L1.COURSENAME || '　' || L0.MAJORNAME AS COURSE_MAJORNAME ";
        $query .= " FROM ";
        $query .= "      MAJOR_YDAT T1 ";
        $query .= " LEFT JOIN MAJOR_MST L0  ON L0.COURSECD = T1.COURSECD ";
        $query .= "                        AND L0.MAJORCD  = T1.MAJORCD ";
        $query .= " LEFT JOIN COURSE_MST L1 ON L1.COURSECD = T1.COURSECD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".$model->leftyear."' ";
        $query .= " AND T1.COURSECD = '".$model->coursecd."' ";
        $query .= " AND T1.MAJORCD  = '".$model->majorcd."' ";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //MAJOR_YDAT課程コード
    function getMajorSchoolDiv()
    {
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

    //--- UPDATE 
    function &getUpdateQuery($model, $fields)
    {
        $db = Query::dbCheckOut();
        if ($fields["SCHOOLDIV"] !==  'XXX') {
            $data["SCHOOLDIV"][TEXT]        = $fields["SCHOOLDIV"];
        } else {
            $data["SCHOOLDIV"][TEXT]        = "";
        }
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";
        $where  = " WHERE ";
        $where .= "     YEAR     = '".$model->leftyear."' ";
        $where .= " AND COURSECD = '".$model->coursecd."' ";
        $where .= " AND MAJORCD  = '".$model->majorcd."' ";

        $query = Query::updateSQL($data, "MAJOR_YDAT", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
}
?>
