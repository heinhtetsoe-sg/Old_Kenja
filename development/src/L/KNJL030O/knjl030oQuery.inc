<?php

require_once('for_php7.php');

class knjl030oQuery extends Query {

    //名称
    function GetName($namecd1, $ObjYear, $namecd2="")
    {
        $query = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$ObjYear."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //入試区分マスタ
    function getTestdivMst($ObjYear)
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS NAMECD2, ";
        $query .= "     NAME AS NAME1 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$ObjYear."' ";
        $query .= " ORDER BY ";
        $query .= "     SHOWORDER, ";
        $query .= "     TESTDIV ";
        return $query;
    }
    
    //試験会場設定データ
    function selectQuery(&$model)
    {
        $query = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_DAT ";
        $query .= " WHERE ";
        if ($model->cmd == "edit"){
            $query .= "     EXAMHALLCD = '" .$model->examhallcd ."' AND ";
        }
        $query .= "     TESTDIV   = '" .$model->testdiv ."' AND ";
        $query .= "     EXAM_TYPE = '" .$model->exam_type ."' ";
        $query .= " ORDER BY ";
        $query .= "     EXAMHALLCD ";
        return $query;
    }
    function update(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query .= " UPDATE ";
        $query .= "     ENTEXAM_HALL_DAT ";
        $query .= " SET ";
        $query .= "     EXAMHALL_NAME = '" .Query::addQuote($model->field["EXAMHALL_NAME"]) ."', ";
        $query .= "     CAPA_CNT = " .$model->field["CAPA_CNT"] .", ";
        $query .= "     REGISTERCD = '".STAFFCD ."', ";
        $query .= "     UPDATED  = SYSDATE() ";
        $query .= " WHERE ";
        $query .= "     TESTDIV = '" .$model->testdiv ."' AND ";
        $query .= "     EXAMHALLCD = '" .$model->examhallcd ."' AND ";
        $query .= "     EXAM_TYPE = '" .$model->exam_type ."' ";

        $db->query($query);
        //割り振り実行
        knjl030oQuery::exec($db, $model);
        $db->commit(); 
        Query::dbCheckIn($db);
    }
    //割り振り実行
    function exec(&$db, &$model)
    {
        $query = " UPDATE ";
        $query .= "     ENTEXAM_HALL_DAT T1 ";
        $query .= " SET ";
        $query .= "     (S_RECEPTNO,E_RECEPTNO)= (SELECT ";
        $query .= "         SUBSTR(DIGITS(COALESCE(SUM(ST1.CAPA_CNT),0)+1), 7, 4), ";
        $query .= "         SUBSTR(DIGITS(COALESCE(SUM(ST1.CAPA_CNT),0)+T1.CAPA_CNT), 7, 4) ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_HALL_DAT ST1 ";
        $query .= "     WHERE ";
        $query .= "         ST1.TESTDIV = T1.TESTDIV AND ";
        $query .= "         ST1.EXAM_TYPE = T1.EXAM_TYPE AND ";
        $query .= "         INTEGER(ST1.EXAMHALLCD) < INTEGER(T1.EXAMHALLCD) ";
        $query .= "     ) ";
        $query .= " WHERE ";
        $query .= "     T1.TESTDIV = '" .$model->testdiv ."' AND ";
        $query .= "     T1.EXAM_TYPE = '" .$model->exam_type ."' ";

        $db->query($query);

    }
    function insert(&$model)
    {
        $db = Query::dbCheckOut();

        $query = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_DAT ";
        $query .= " WHERE ";
        $query .= "     TESTDIV = '" .$model->testdiv ."' AND ";
        $query .= "     EXAM_TYPE = '" .$model->exam_type ."' ";
        $query .= " ORDER BY ";
        $query .= "     EXAMHALLCD DESC ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (is_array($row)){
            $examhallcd = $row["EXAMHALLCD"] + 1;
            $s_receptno = $row["E_RECEPTNO"] + 1;
        }else{
            $examhallcd = 1;
            $s_receptno = 1;
        }
        
        $query = " INSERT INTO ENTEXAM_HALL_DAT( ";
        $query .= " TESTDIV, ";
        $query .= " EXAM_TYPE, ";
        $query .= " EXAMHALLCD, ";
        $query .= " EXAMHALL_NAME, ";
        $query .= " CAPA_CNT, ";
        $query .= " S_RECEPTNO, ";
        $query .= " E_RECEPTNO, ";
        $query .= " REGISTERCD ";
        $query .= " ) ";
        $query .= " VALUES ";
        $query .= " ( ";
        $query .= " '" .$model->testdiv ."', ";
        $query .= " '" .$model->exam_type ."', ";
        $query .= " '".sprintf("%04d", $examhallcd). "', ";
        $query .= " '" .Query::addQuote($model->field["EXAMHALL_NAME"]) ."', ";
        $query .= " " .$model->field["CAPA_CNT"] .", ";
        $query .= " '".sprintf("%04d", $s_receptno). "', ";
        $query .= " '".sprintf("%04d", $s_receptno+$model->field["CAPA_CNT"]-1). "', ";
        $query .= " '".STAFFCD ."' ";
        $query .= " ) ";

        $db->query($query);

        Query::dbCheckIn($db);
    }
    function delete(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = " DELETE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_DAT ";
        $query .= " WHERE ";
        $query .= "     TESTDIV = '" .$model->testdiv ."' AND ";
        $query .= "     EXAM_TYPE = '" .$model->exam_type ."' AND ";
        $query .= "     EXAMHALLCD IN ('" .implode($model->field["CHECKED"],"','") ."') ";

        $result = $db->query($query);

        $db->query($query);

        //割り振り実行
        knjl030oQuery::exec($db, $model);

        $db->commit(); 
        Query::dbCheckIn($db);
    }

}
?>