<?php

require_once('for_php7.php');

class knjd102aQuery extends Query {

    //テストコンボ取得
    function getTest($model)
    {
        $query  = " SELECT ";
        $query .= "     TESTKINDCD || TESTITEMCD AS VALUE, ";
        $query .= "     TESTKINDCD || TESTITEMCD || '  ' || TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .=       $model->testTable ;
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' AND ";
        if ($model->testTable == "TESTITEM_MST_COUNTFLG_NEW") {
            $query .= "     SEMESTER = '".$model->exp_semester ."' AND ";
        }
        $query .= "     TESTKINDCD <> '99' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //１レコード取得
    function getRecordRemark($model)
    {

        $query  = " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     REMARK1 ";
        $query .= " FROM ";
        $query .= "     HEXAM_RECORD_REMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER = '".$model->exp_semester."' AND ";
        $query .= "     TESTKINDCD || TESTITEMCD = '".$model->field["TESTKINDCD"]."' AND ";
        $query .= "     SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     REMARK_DIV = '1' ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($db, $model)
    {
        $data["YEAR"][TEXT]                 = $model->exp_year;
        $data["SEMESTER"][TEXT]             = $model->exp_semester;
        $data["TESTKINDCD"][TEXT]           = substr($model->field["TESTKINDCD"],0,2);
        $data["TESTITEMCD"][TEXT]           = substr($model->field["TESTKINDCD"],2);
        $data["SCHREGNO"][TEXT]             = $model->schregno;
        $data["REMARK_DIV"][TEXT]           = 1;
        $data["REMARK1"][TEXT]              = $model->field["REMARK1"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $db->query(Query::insertSQL($data, "hexam_record_remark_dat"));

        return ;
    }

   //DELETE
   function &getDeleteQuery($db, $model)
   {
        $query  = " DELETE FROM ";
        $query .= "     HEXAM_RECORD_REMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER = '".$model->exp_semester."' AND ";
        $query .= "     TESTKINDCD || TESTITEMCD  = '".$model->field["TESTKINDCD"]."' AND ";
        $query .= "     SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     REMARK_DIV = '1' ";

        $db->query($query);

        return $result;
   }

}
?>
