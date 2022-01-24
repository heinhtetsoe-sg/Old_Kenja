<?php
class knjl740hQuery extends Query
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
        $query .= "     INNER JOIN ENTEXAM_HALL_LIST_YDAT T3 ";
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
        $where .= "   AND T1.EXAM_TYPE    = '1' ";
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
        $query .= "     INNER JOIN ENTEXAM_HALL_LIST_YDAT T3 ";
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
        $where .= "   AND T1.EXAM_TYPE    = '1' ";
        $query .= "   AND T1.JUDGEDIV     = '5' ";  //既に欠席の受験者
        $query .= " ORDER BY ";
        $query .= "     T1.RECEPTNO ";
        return $query;
    }

    //更新
    public function getUpdateQuery(&$model, $flg)
    {
        //リストが空の場合
        if ($flg == "left" && !$model->selectLeft) {
            return false;
        } elseif ($flg == "right" && !$model->selectRight) {
            return false;
        }

        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $selectSchArr = array();
        
        if ($flg == "left") {
            //左側リスト
            $judge = "5"; //欠席
            $selectSchArr = explode(",", $model->selectLeft);
        } elseif ($flg == "right") {
            //右側リスト
            $judge = ""; //空白
            $selectSchArr = explode(",", $model->selectRight);
        }

        foreach ($selectSchArr as $receptNo) {
            //ENTEXAM_RECEPT_DAT
            $data = array();
            $data["JUDGEDIV"][TEXT]     = $judge;
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $where  = " WHERE ENTEXAMYEAR   = '".$model->ObjYear."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->field["APPLICANTDIV"]."' ";
            $where .= "   AND TESTDIV       = '".$model->field["TESTDIV"]."' ";
            $where .= "   AND EXAM_TYPE     = '1' ";
            $where .= "   AND RECEPTNO      = '".$receptNo."' ";
            if ($flg == "right") {
                $where .= "   AND (JUDGEDIV = '5' OR JUDGEDIV IS NULL) "; //JUDGEDIVが '5':欠席 または NULL の場合、更新対象とする
            }

            //JUDGEDIVの更新前に、ENTEXAM_RECEPT_DAT.EXAMNOを取得
            $examno = $db->getOne(" SELECT EXAMNO FROM ENTEXAM_RECEPT_DAT ".$where);

            //更新（ENTEXAM_RECEPT_DAT）
            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DAT
            $data = array();
            $data["JUDGEMENT"][TEXT]    = $judge;
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $where  = " WHERE ENTEXAMYEAR   = '".$model->ObjYear."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->field["APPLICANTDIV"]."' ";
            $where .= "   AND EXAMNO        = '".$examno."' ";

            //更新（ENTEXAM_APPLICANTBASE_DAT）
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
