<?php
class knjl730hQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($model, $namecd1)
    {
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "       YEAR     = '" . $model->ObjYear . "' ";
        $query .= "   AND NAMECD1  = '" . $namecd1 . "' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //入試区分の取得
    public function getTestDiv($model)
    {
        $query .= " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "       ENTEXAMYEAR  = '" . $model->ObjYear . "' ";
        $query .= "   AND APPLICANTDIV = '" . $model->field["APPLICANTDIV"] . "' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //試験会場設定データ
    public function getExamHall($model)
    {
        $query .= " SELECT ";
        $query .= "     *, ";
        $query .= "     EXAMHALLCD AS VALUE, ";
        $query .= "     EXAMHALLCD || ':' || EXAMHALL_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "       ENTEXAMYEAR  = '" . $model->ObjYear . "' ";
        $query .= "   AND APPLICANTDIV = '" . $model->field["APPLICANTDIV"] . "' ";
        $query .= "   AND TESTDIV      = '" . $model->field["TESTDIV"] . "' ";
        $query .= "   AND EXAM_TYPE    = '" . $model->field["EXAM_TYPE"] . "' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //一覧取得
    public function getSelectQuery($model)
    {
        $query .= " SELECT ";
        $query .= "     T1.RECEPTNO AS VALUE, ";
        $query .= "     T1.RECEPTNO || '：' ||  T2.NAME AS LABEL ";
        $query .= " FROM  ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "             ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "            AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "            AND T2.EXAMNO       = T1.EXAMNO ";
        $query .= " WHERE ";
        $query .= "       T1.ENTEXAMYEAR  = '" . $model->ObjYear . "' ";
        $query .= "   AND T1.APPLICANTDIV = '" . $model->field["APPLICANTDIV"] . "' ";
        $query .= "   AND T1.TESTDIV      = '" . $model->field["TESTDIV"] . "' ";
        $query .= " ORDER BY ";
        $query .= "     T1.RECEPTNO ";
        return $query;
    }

    //対象外の志願者取得
    public function getSelectQueryLeft($model)
    {
        $query .= " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM  ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "             ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "            AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "            AND T2.EXAMNO       = T1.EXAMNO ";
        $query .= "     INNER JOIN ENTEXAM_HALL_LIST_YDAT T3 "; //会場割振りされた志願者は対象外
        $query .= "             ON T3.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "            AND T3.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "            AND T3.TESTDIV      = T1.TESTDIV ";
        $query .= "            AND T3.EXAM_TYPE    = '" . $model->field["EXAM_TYPE"] . "' ";
        $query .= "            AND T3.EXAMHALLCD   = '" . $model->field["EXAMHALLCD"] . "' ";
        $query .= "            AND T3.RECEPTNO     = T1.RECEPTNO ";
        $query .= " WHERE ";
        $query .= "       T1.ENTEXAMYEAR  = '" . $model->ObjYear . "' ";
        $query .= "   AND T1.APPLICANTDIV = '" . $model->field["APPLICANTDIV"] . "' ";
        $query .= "   AND T1.TESTDIV      = '" . $model->field["TESTDIV"] . "' ";
        $query .= " ORDER BY ";
        $query .= "     T1.RECEPTNO ";
        return $query;
    }
    
    //対象外の志願者取得(※別会場含む)
    public function getSelectQueryLeft2($model)
    {
        $query .= " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM  ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "             ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "            AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "            AND T2.EXAMNO       = T1.EXAMNO ";
        $query .= "     INNER JOIN ENTEXAM_HALL_LIST_YDAT T3 "; //会場割振りされた志願者は対象外
        $query .= "             ON T3.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "            AND T3.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "            AND T3.TESTDIV      = T1.TESTDIV ";
        $query .= "            AND T3.EXAM_TYPE    = '" . $model->field["EXAM_TYPE"] . "' ";
        $query .= "            AND T3.RECEPTNO     = T1.RECEPTNO ";
        $query .= " WHERE ";
        $query .= "       T1.ENTEXAMYEAR  = '" . $model->ObjYear . "' ";
        $query .= "   AND T1.APPLICANTDIV = '" . $model->field["APPLICANTDIV"] . "' ";
        $query .= "   AND T1.TESTDIV      = '" . $model->field["TESTDIV"] . "' ";
        $query .= " ORDER BY ";
        $query .= "     T1.RECEPTNO ";
        return $query;
    }

    //削除
    public function &getDeleteQuery($model, &$db)
    {
        $query  = "  DELETE FROM ENTEXAM_HALL_LIST_YDAT ";
        $query .= "  WHERE ";
        $query .= "        ENTEXAMYEAR  = '" . $model->ObjYear . "' ";
        $query .= "    AND APPLICANTDIV = '" . $model->field["APPLICANTDIV"] . "' ";
        $query .= "    AND TESTDIV      = '" . $model->field["TESTDIV"] . "' ";
        $query .= "    AND EXAM_TYPE    = '" . $model->field["EXAM_TYPE"] . "' ";
        $query .= "    AND EXAMHALLCD   = '" . $model->field["EXAMHALLCD"] . "' ";

        $db->query($query);

        return;
    }

    //追加
    public function &getUpdateQuery($model, &$db)
    {
        $selectSchArr = explode(",", $model->selectLeft);
        foreach ($selectSchArr as $receptNo) {
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
            $data["TESTDIV"][TEXT]          = $model->field["TESTDIV"];
            $data["EXAM_TYPE"][TEXT]        = $model->field["EXAM_TYPE"];
            $data["EXAMHALLCD"][TEXT]       = $model->field["EXAMHALLCD"];
            $data["RECEPTNO"][TEXT]         = $receptNo;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $query = Query::insertSQL($data, "ENTEXAM_HALL_LIST_YDAT");
            $db->query($query);
        }
        return;
    }

    //削除(ENTEXAM_HALL_YDAT)
    public function &getDeleteEntexamHallYdat($model, &$db)
    {
        $query  = "  DELETE FROM ENTEXAM_HALL_YDAT ";
        $query .= "  WHERE ";
        $query .= "        ENTEXAMYEAR  = '" . $model->ObjYear . "' ";
        $query .= "    AND APPLICANTDIV = '" . $model->field["APPLICANTDIV"] . "' ";
        $query .= "    AND TESTDIV      = '" . $model->field["TESTDIV"] . "' ";
        $query .= "    AND EXAM_TYPE    = '" . $model->field["EXAM_TYPE"] . "' ";
        $query .= "    AND EXAMHALLCD   = '" . $model->field["EXAMHALLCD"] . "' ";
        $db->query($query);

        return;
    }

    //追加(ENTEXAM_HALL_YDAT)
    public function &getUpdateEntexamHallYdat($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //会場コードの取得
        $query  = " SELECT ";
        $query .= "   MAX(EXAMHALLCD) AS EXAMHALLCD ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_HALL_YDAT ";
        $query .= "  WHERE ";
        $query .= "        ENTEXAMYEAR  = '" . $model->ObjYear . "' ";
        $query .= "    AND APPLICANTDIV = '" . $model->applicantdiv . "' ";
        $query .= "    AND TESTDIV      = '" . $model->testdiv . "' ";
        $maxHallCd = $db->getOne($query);
        $maxHallCd = $maxHallCd + 1;
        $maxHallCd = sprintf("%04d", $maxHallCd);

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
        $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
        $data["TESTDIV"][TEXT]          = $model->testdiv;
        $data["EXAM_TYPE"][TEXT]        = $model->field["EXAM_TYPE"];
        $data["EXAMHALLCD"][TEXT]       = $maxHallCd;
        $data["EXAMHALL_NAME"][TEXT]    = $model->field["EXAMHALL_NAME"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";
        $query = Query::insertSQL($data, "ENTEXAM_HALL_YDAT");
        $db->query($query);

        $db->commit();
        //DB切断
        Query::dbCheckIn($db);
    }
}
