<?php

require_once('for_php7.php');

class knjf150bQuery extends Query
{
    //クラス名称取得
    function getHrName($model)
    {
        $query  = " SELECT ";
        $query .= "     HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".CTRL_SEMESTER."' AND ";
        $query .= "     GRADE       = '".$model->grade."' AND ";
        $query .= "     HR_CLASS    = '".$model->hr_class."' ";

        return $query;
    }

    //保健室来室記録データよりデータを取得
    function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.VISIT_DATE, ";
        $query .= "     T1.VISIT_HOUR, ";
        $query .= "     T1.VISIT_MINUTE, ";
        $query .= "     T1.OCCUR_CAUSE, ";
        $query .= "     F206.NAME1 AS OCCUR_PLACE, ";
        $query .= "     T1.TREATMENT ";
        $query .= " FROM ";
        $query .= "     NURSEOFF_VISIT_TEXT_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST F206 ";
        $query .= "       ON F206.NAMECD1 = 'F206' ";
        $query .= "      AND F206.NAMECD2 = T1.OCCUR_PLACE";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.VISIT_DATE DESC, ";
        $query .= "     T1.VISIT_HOUR DESC, ";
        $query .= "     T1.VISIT_MINUTE DESC ";

        return $query;
    }

    // DELETE
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $query  = " DELETE FROM ";
        $query .= "     NURSEOFF_VISIT_TEXT_DAT ";
        $query .= " WHERE ";
        $query .= "     VISIT_DATE = '".$model->visit_date."' AND ";
        $query .= "     VISIT_HOUR = '".$model->visit_hour."' AND ";
        $query .= "     VISIT_MINUTE = '".$model->visit_minute."' AND ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";

        $db->query($query);
        Query::dbCheckIn($db);
    }

    //１レコード取得
    function getRow($model)
    {
        $date = str_replace("/", "-", $model->visit_date);

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     NURSEOFF_VISIT_TEXT_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO     = '".$model->schregno."' AND ";
        if ($date) {
            $query .= "     VISIT_DATE   = '".$date."' AND ";
        } else {
            $query .= "     VISIT_DATE IS NULL AND ";
        }
        $query .= "     VISIT_HOUR   = '".$model->visit_hour."' AND ";
        $query .= "     VISIT_MINUTE = '".$model->visit_minute."' ";

        return $query;
    }

    //名称マスタ取得
    function getNameMst($namecd1, $namecd2="")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if($namecd2){
            $query .= "     AND SUBSTR(NAMECD2,1,2) = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //存在チェック
    function getExistCheck($model)
    {
        $db = Query::dbCheckOut();

        $date = str_replace("/", "-", $model->field["VISIT_DATE"]);

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     NURSEOFF_VISIT_TEXT_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO     = '".$model->schregno."' AND ";
        $query .= "     VISIT_DATE   = '".$date."' AND ";
        $query .= "     VISIT_HOUR   = '".$model->field["VISIT_HOUR"]."' AND ";
        $query .= "     VISIT_MINUTE = '".$model->field["VISIT_MINUTE"]."' ";

        $row = $db->getCol($query);
        Query::dbCheckIn($db);
        return $row;
    }

    // UPDATE
    function &getUpdateQuery($model, $fields, $flg)
    {
        $db = Query::dbCheckOut();

        $visit_date = str_replace("/", "-", $fields["VISIT_DATE"]);

        $data["SCHREGNO"][TEXT]             = $model->schregno;
        $data["VISIT_DATE"][DATE]           = $visit_date;
        $data["VISIT_HOUR"][TEXT]           = $fields["VISIT_HOUR"];
        $data["VISIT_MINUTE"][TEXT]         = $fields["VISIT_MINUTE"];
        $data["OCCUR_CAUSE"][TEXT]          = $fields["OCCUR_CAUSE"];
        $data["OCCUR_PLACE"][TEXT]          = $fields["OCCUR_PLACE"];
        $data["TREATMENT"][TEXT]            = $fields["TREATMENT"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        if($flg == "add"){
            $db->query(Query::insertSQL($data, "NURSEOFF_VISIT_TEXT_DAT"));
        } else {
            $where  = " WHERE ";
            $where .= "     SCHREGNO     = '".$model->schregno."' AND ";
            $where .= "     VISIT_DATE   = '".$visit_date."' AND ";
            $where .= "     VISIT_HOUR   = '".$fields["VISIT_HOUR"]."' AND ";
            $where .= "     VISIT_MINUTE = '".$fields["VISIT_MINUTE"]."' ";
            $db->query(Query::updateSQL($data, "NURSEOFF_VISIT_TEXT_DAT", $where));
        }

        Query::dbCheckIn($db);
    }

}
?>
