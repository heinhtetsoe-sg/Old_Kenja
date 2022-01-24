<?php
class knjl321kQuery extends Query {
    //特別理由区分
    function getSpecialReasonDiv($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->ObjYear}' AND ";
        $query .= "     NAMECD1 = 'L017' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //試験区分
    function GetTestdiv($model, $div = ""){
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->ObjYear."' AND ";
        $query .= "     NAMECD1 = 'L003' ";
        if ($div != "") {
            $query .= "     AND NAMECD2 = '".$div."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //会場
    function GetExamHallcd($model){
        $query  = " SELECT DISTINCT ";
        $query .= "     EXAMHALLCD, ";
        $query .= "     EXAMHALL_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_DAT ";
        if ($model->testdiv != 99) 
            $query .= " WHERE TESTDIV = '".$model->testdiv."' ";
        $query .= " ORDER BY EXAMHALLCD ";

        return $query;
    }

    //タイトル
    function getTitle() {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'L013' ";
        $query .= "     AND NAMECD2 = '1' ";

        return $query;
    }

    //タイトル
    function getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'L013' ";
        $query .= "     AND NAMECD2 = '1' ";
        $db->query($query);

        $data["NAMECD1"][TEXT]    = "L013";
        $data["NAMECD2"][TEXT]    = "1";
        $data["NAME1"][TEXT]      = $model->title;
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC]    = "sysdate()";
        $query = Query::insertSQL($data, "NAME_MST");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //中高判別
    function GetJorH(){
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOLNAME1 LIKE '%中学%' ";

        return $query;
    }

}
?>
