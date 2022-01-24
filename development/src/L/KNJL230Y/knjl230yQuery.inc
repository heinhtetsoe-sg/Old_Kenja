<?php

require_once('for_php7.php');

class knjl230yQuery extends Query {

    //名称マスタより取得
    function getNameMst($namecd1, $year) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //グループデータ
    function selectQuery(&$model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND T1.EXAM_TYPE    = '{$model->exam_type}' ";
        if ($model->cmd == "edit"){
            $query .= "     AND T1.EXAMHALLCD   = '{$model->examhallcd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.EXAMHALLCD ";
        return $query;
    }

    //受付データ
    function getReceptCntALL($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     COUNT(T2.RECEPTNO) AS RECEPT_CNT_ALL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T2 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "         ON  T1.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "         AND T1.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "         AND T1.EXAMNO = T2.EXAMNO ";
        $query .= " WHERE ";
        $query .= "         T2.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND T2.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T2.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND T2.EXAM_TYPE    = '{$model->exam_type}' ";
        return $query;
    }

    //追加・更新画面の右リスト
    //受付データからグループデータを除いたリスト
    function getListHallorRecept($model, $listFlg) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.BIRTHDAY, ";
        $query .= "     T2.RECEPTNO, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T2 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "         ON  T1.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "         AND T1.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "         AND T1.EXAMNO = T2.EXAMNO ";
        $query .= " WHERE ";
        $query .= "         T2.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND T2.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T2.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND T2.EXAM_TYPE    = '{$model->exam_type}' ";
        //追加・更新画面の右リスト
        //受付データからグループデータを除いたリスト
        if ($listFlg == "right") {
            $query .= "     AND T2.RECEPTNO NOT IN ( ";
            $query .= "         SELECT ";
            $query .= "             T3.RECEPTNO ";
            $query .= "         FROM ";
            $query .= "             ENTEXAM_HALL_LIST_YDAT T3 ";
            $query .= "         WHERE ";
            $query .= "                 T3.ENTEXAMYEAR  = '{$model->year}' ";
            $query .= "             AND T3.APPLICANTDIV = '{$model->applicantdiv}' ";
            $query .= "             AND T3.TESTDIV      = '{$model->testdiv}' ";
            $query .= "             AND T3.EXAM_TYPE    = '{$model->exam_type}' ";
            $query .= "         ) ";
        }
        //追加・更新画面の左リスト
        //指定グループデータのリスト
        if ($listFlg == "left") {
            $query .= "     AND T2.RECEPTNO IN ( ";
            $query .= "         SELECT ";
            $query .= "             T3.RECEPTNO ";
            $query .= "         FROM ";
            $query .= "             ENTEXAM_HALL_LIST_YDAT T3 ";
            $query .= "         WHERE ";
            $query .= "                 T3.ENTEXAMYEAR  = '{$model->year}' ";
            $query .= "             AND T3.APPLICANTDIV = '{$model->applicantdiv}' ";
            $query .= "             AND T3.TESTDIV      = '{$model->testdiv}' ";
            $query .= "             AND T3.EXAM_TYPE    = '{$model->exam_type}' ";
            if ($model->cmd == "edit"){
                $query .= "             AND T3.EXAMHALLCD   = '{$model->examhallcd}' ";
            }
            $query .= "         ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.BIRTHDAY, ";
        $query .= "     T2.RECEPTNO ";
        return $query;
    }

    //重複・存在チェック
    function examhallcdCheck($model) {
        $db = Query::dbCheckOut();

        $query  = "";
        $query .= " SELECT ";
        $query .= "     COUNT(T1.EXAMHALLCD) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND T1.EXAM_TYPE    = '{$model->exam_type}' ";
        if ($model->cmd == "insert") {
            $query .= "     AND T1.EXAMHALLCD   = '{$model->field["EXAMHALLCD"]}' ";
        }
        if ($model->cmd == "update") {
            $query .= "     AND T1.EXAMHALLCD   = '{$model->examhallcd}' ";
        }

        $cnt = $db->getOne($query);
        Query::dbCheckIn($db);
        return $cnt;
    }

    //追加
    function insert(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //対象者(左のリスト）
        $leftList = strlen($model->selectdata) ? explode(",", $model->selectdata) : array();

        //ENTEXAM_HALL_YDAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]  = $model->year;
        $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
        $data["TESTDIV"][TEXT]      = $model->testdiv;
        $data["EXAM_TYPE"][TEXT]    = $model->exam_type;
        $data["EXAMHALLCD"][TEXT]   = $model->field["EXAMHALLCD"];
        $data["EXAMHALL_NAME"][TEXT]= $model->field["EXAMHALL_NAME"];
        $data["CAPA_CNT"][NUMBER]   = get_count($leftList);
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        $query = Query::insertSQL($data, "ENTEXAM_HALL_YDAT");
        $db->query($query);

        //ENTEXAM_HALL_LIST_YDAT
        foreach ($leftList as $receptData) {
            list($birthday, $receptno) = explode(":", $receptData);
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]  = $model->year;
            $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
            $data["TESTDIV"][TEXT]      = $model->testdiv;
            $data["EXAM_TYPE"][TEXT]    = $model->exam_type;
            $data["EXAMHALLCD"][TEXT]   = $model->field["EXAMHALLCD"];
            $data["RECEPTNO"][TEXT]     = $receptno;
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $query = Query::insertSQL($data, "ENTEXAM_HALL_LIST_YDAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    //更新
    function update(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //対象者(左のリスト）
        $leftList = strlen($model->selectdata) ? explode(",", $model->selectdata) : array();

        //ENTEXAM_HALL_YDAT
        $data = array();
        $data["EXAMHALL_NAME"][TEXT]= $model->field["EXAMHALL_NAME"];
        $data["CAPA_CNT"][NUMBER]   = get_count($leftList);
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        $where  = " WHERE ";
        $where .= "         ENTEXAMYEAR  = '{$model->year}' ";
        $where .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $where .= "     AND TESTDIV      = '{$model->testdiv}' ";
        $where .= "     AND EXAM_TYPE    = '{$model->exam_type}' ";
        $where .= "     AND EXAMHALLCD   = '{$model->field["EXAMHALLCD"]}' ";
        $query = Query::updateSQL($data, "ENTEXAM_HALL_YDAT", $where);
        $db->query($query);

        //ENTEXAM_HALL_LIST_YDAT
        $query  = " DELETE FROM ENTEXAM_HALL_LIST_YDAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND EXAM_TYPE    = '{$model->exam_type}' ";
        $query .= "     AND EXAMHALLCD   = '{$model->field["EXAMHALLCD"]}' ";
        $db->query($query);
        foreach ($leftList as $receptData) {
            list($birthday, $receptno) = explode(":", $receptData);
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]  = $model->year;
            $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
            $data["TESTDIV"][TEXT]      = $model->testdiv;
            $data["EXAM_TYPE"][TEXT]    = $model->exam_type;
            $data["EXAMHALLCD"][TEXT]   = $model->field["EXAMHALLCD"];
            $data["RECEPTNO"][TEXT]     = $receptno;
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $query = Query::insertSQL($data, "ENTEXAM_HALL_LIST_YDAT");
            $db->query($query);
        }

        $db->commit(); 
        Query::dbCheckIn($db);
    }

    //削除
    function delete(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_HALL_YDAT
        $query  = " DELETE FROM ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND EXAM_TYPE    = '{$model->exam_type}' ";
        $query .= "     AND EXAMHALLCD IN ('" .implode($model->field["CHECKED"],"','") ."') ";
        $db->query($query);

        //ENTEXAM_HALL_LIST_YDAT
        $query  = " DELETE FROM ENTEXAM_HALL_LIST_YDAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND EXAM_TYPE    = '{$model->exam_type}' ";
        $query .= "     AND EXAMHALLCD IN ('" .implode($model->field["CHECKED"],"','") ."') ";
        $db->query($query);

        $db->commit(); 
        Query::dbCheckIn($db);
    }
}
?>