<?php

require_once('for_php7.php');

class knjl030hQuery extends Query {

    //名称
    function GetName($namecd1,$ObjYear,$order = "")
    {
        $query = "SELECT namecd2, name1, name2, namespare2 FROM v_name_mst
                   WHERE year    = '".$ObjYear."'
                     AND namecd1 = '".$namecd1."'
                   ORDER BY namecd2 ".$order;
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
        knjl030hQuery::exec($db, $model);
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
        $query .= "         SUBSTR(DIGITS(COALESCE(SUM(ST1.CAPA_CNT),0)+".$model->s_receptno."), 7, 4), ";
        $query .= "         SUBSTR(DIGITS(COALESCE(SUM(ST1.CAPA_CNT),0)+".$model->s_receptno."+T1.CAPA_CNT-1), 7, 4) ";
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
            $s_receptno = $model->s_receptno;
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
        knjl030hQuery::exec($db, $model);

        $db->commit(); 
        Query::dbCheckIn($db);
    }

}
?>