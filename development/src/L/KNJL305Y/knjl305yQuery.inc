<?php

require_once('for_php7.php');

class knjl305yQuery extends Query {

    //名称マスタより取得
    function getNameMst($namecd, $year)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //会場取得
    function getExamHall($model, $flg="")
    {
        $selectdata = implode("','",explode(",",$model->selectdata));

        $query  = " SELECT ";
        $query .= "     EXAMHALLCD AS VALUE, ";
        $query .= "     EXAMHALL_NAME || ' (' || S_RECEPTNO || '～' || E_RECEPTNO || ')' AS LABEL, ";
        $query .= "     S_RECEPTNO, ";
        $query .= "     E_RECEPTNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
        $query .= "     APPLICANTDIV    = '".$model->field["APPLICANTDIV"]."' AND ";
        $query .= "     TESTDIV         = '".$model->field["TESTDIV"]."' AND ";
        $query .= "     EXAM_TYPE       = '1' ";
        if($flg == "1") {
            $query .= " AND EXAMHALLCD NOT IN ('".$selectdata."') ";
        } else if($flg == "2") {
            $query .= " AND EXAMHALLCD IN ('".$selectdata."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験番号取得
    function getExamNo($model, $receptno)
    {
        $query  = " SELECT ";
        $query .= "     EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
        $query .= "     APPLICANTDIV    = '".$model->field["APPLICANTDIV"]."' AND ";
        $query .= "     TESTDIV         = '".$model->field["TESTDIV"]."' AND ";
        $query .= "     EXAM_TYPE       = '1' AND ";	
        $query .= "     RECEPTNO        = '".$receptno."' ";	

        return $query;
    }

    //入試制度名称取得
    function getApplicantName($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->ObjYear."' AND ";
        $query .= "     NAMECD1 = 'L003' AND ";
        $query .= "     NAMECD2 = '".$model->field["APPLICANTDIV"]."' ";

        return $query;
    }

    //コメント取得
    function getDocumentDat($model)
    {
        $query  = " SELECT ";
        $query .= "     DOCUMENT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_DOCUMENT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->ObjYear."' AND ";
        $query .= "     APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";

        return $query;
    }

    //UPDATE
    function &getInsertQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
        $data["APPLICANTDIV"][TEXT]     = $fields["APPLICANTDIV"];
        $data["DOCUMENT"][TEXT]         = $fields["COMMENT"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $db->query(Query::insertSQL($data, "ENTEXAM_DOCUMENT_DAT"));
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ENTEXAM_DOCUMENT_DAT ";
        $query .= " WHERE ENTEXAMYEAR   = '".$model->ObjYear."'";
        $query .= "   AND APPLICANTDIV  = '".$fields["APPLICANTDIV"]."'";

        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }
}
?>
