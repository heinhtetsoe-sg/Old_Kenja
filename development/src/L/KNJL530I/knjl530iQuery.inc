<?php
class knjl530iQuery extends Query
{
    //入試区分マスタより取得
    public function getTestdivMst(&$model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
        $query .= "     TESTDIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //試験会場設定データ
    public function selectQuery(&$model, $setSort = "")
    {
        $query = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR       = '" .$model->year ."' ";
        $query .= "     AND APPLICANTDIV  = '" .$model->applicantdiv ."' ";
        $query .= "     AND TESTDIV       = '" .$model->testdiv ."' ";
        if ($model->cmd == "edit") {
            $query .= "     AND EXAMHALLCD    = '" .$model->examhallcd ."' ";
        }
        $query .= "     AND EXAM_TYPE     = '" .$model->exam_type ."' ";
        $query .= " ORDER BY ";
        if ($setSort) {
            $query .= "     ".$setSort." ";
        } else {
            $query .= "     EXAMHALLCD ";
        }

        return $query;
    }

    public function update(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " UPDATE ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " SET ";
        $query .= "     EXAMHALL_NAME = '" .Query::addQuote($model->field["EXAMHALL_NAME"]) ."', ";
        $query .= "     CAPA_CNT = " .$model->field["CAPA_CNT"] .", ";
        $query .= "     REGISTERCD = '".STAFFCD ."', ";
        $query .= "     UPDATED  = SYSDATE() ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR       = '" .$model->year ."' ";
        $query .= "     AND APPLICANTDIV  = '" .$model->applicantdiv ."' ";
        $query .= "     AND TESTDIV       = '" .$model->testdiv ."' ";
        $query .= "     AND EXAMHALLCD    = '" .$model->examhallcd ."' ";
        $query .= "     AND EXAM_TYPE     = '" .$model->exam_type ."' ";

        $db->query($query);
        //割り振り実行
        knjl530iQuery::exec($db, $model);
        $db->commit();
        Query::dbCheckIn($db);
    }

    //割り振り実行
    public function exec(&$db, &$model)
    {
        $query = knjl530iQuery::selectQuery($model);
        $result = $db->query($query);
        $hallArray = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $hallArray[] = $row;
        }
        $result->free();

        $sExamNo = "0";
        foreach ($hallArray as $key => $val) {
            $query  = " WITH BASE_T AS ( ";
            $query .= " SELECT ";
            $query .= "     EXAMNO ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR        = '".$model->year."' ";
            $query .= "     AND APPLICANTDIV   = '".$model->applicantdiv."' ";
            $query .= "     AND TESTDIV        = '".$model->testdiv."' ";   //入試区分
            $query .= "     AND TESTDIV0       = '".$model->exam_type."' "; //学科
            $query .= "     AND EXAMNO > '".$sExamNo."' ";
            $query .= " ORDER BY ";
            $query .= "     EXAMNO ";
            $query .= " FETCH FIRST {$val["CAPA_CNT"]} ROWS ONLY ";
            $query .= " ) ";
            $query .= " SELECT ";
            $query .= "     MIN(EXAMNO) AS MIN_R, ";
            $query .= "     MAX(EXAMNO) AS MAX_R ";
            $query .= " FROM ";
            $query .= "     BASE_T ";
            $setRecept = $db->getRow($query, DB_FETCHMODE_ASSOC);

            $query  = " UPDATE ";
            $query .= "     ENTEXAM_HALL_YDAT T1 ";
            $query .= " SET ";
            $query .= "     (S_RECEPTNO, E_RECEPTNO) = ('{$setRecept["MIN_R"]}', '{$setRecept["MAX_R"]}') ";
            $query .= " WHERE ";
            $query .= "         T1.ENTEXAMYEAR      = '".$model->year."' ";
            $query .= "         AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
            $query .= "         AND T1.TESTDIV      = '".$model->testdiv."' ";
            $query .= "         AND T1.EXAM_TYPE    = '".$model->exam_type."' ";
            $query .= "         AND T1.EXAMHALLCD   = '".$val["EXAMHALLCD"]."' ";

            $sExamNo = $setRecept["MAX_R"] ? $setRecept["MAX_R"] : "ZZZZZ";

            $db->query($query);
        }
    }

    public function insert(&$model)
    {
        $db = Query::dbCheckOut();

        $setSort = "EXAMHALLCD DESC";
        $query = knjl530iQuery::selectQuery($model, $setSort);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (is_array($row)) {
            $examhallcd = $row["EXAMHALLCD"] + 1;
        } else {
            $examhallcd = 1;
        }

        $query  = " INSERT INTO ENTEXAM_HALL_YDAT( ";
        $query .= " ENTEXAMYEAR, ";
        $query .= " APPLICANTDIV, ";
        $query .= " TESTDIV, ";
        $query .= " EXAM_TYPE, ";
        $query .= " EXAMHALLCD, ";
        $query .= " EXAMHALL_NAME, ";
        $query .= " CAPA_CNT, ";
        $query .= " REGISTERCD ";
        $query .= " ) ";
        $query .= " VALUES ";
        $query .= " ( ";
        $query .= " '" .$model->year ."', ";
        $query .= " '" .$model->applicantdiv ."', ";
        $query .= " '" .$model->testdiv ."', ";
        $query .= " '" .$model->exam_type ."', ";
        $query .= " '".sprintf("%04d", $examhallcd). "', ";
        $query .= " '" .Query::addQuote($model->field["EXAMHALL_NAME"]) ."', ";
        $query .= " " .$model->field["CAPA_CNT"] .", ";
        $query .= " '".STAFFCD ."' ";
        $query .= " ) ";

        $db->query($query);

        Query::dbCheckIn($db);
    }

    public function delete(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR       = '" .$model->year ."' ";
        $query .= "     AND APPLICANTDIV  = '" .$model->applicantdiv ."' ";
        $query .= "     AND TESTDIV       = '" .$model->testdiv ."' ";
        $query .= "     AND EXAM_TYPE     = '" .$model->exam_type ."' ";
        $query .= "     AND EXAMHALLCD IN ('" .implode("','", $model->field["CHECKED"]) ."') ";

        $db->query($query);

        //割り振り実行
        knjl530iQuery::exec($db, $model);

        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
