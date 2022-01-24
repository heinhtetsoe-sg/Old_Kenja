<?php

require_once('for_php7.php');

class knjl030yQuery extends Query {

    //名称マスタより取得
    function getNameMst($namecd1, $year)
    {
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

    //試験会場設定データ
    function selectQuery(&$model)
    {
        $query  = "";
        $query .= " WITH T_DETAIL AS ( ";
        $query .= "     SELECT ";
        $query .= "         D1.EXAMHALLCD, ";
        $query .= "         COUNT(D1.DETAIL_NO) AS DETAIL_CNT ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_HALL_DETAIL_YDAT D1 ";
        $query .= "     WHERE ";
        $query .= "         D1.ENTEXAMYEAR = '" .$model->year ."' AND ";
        $query .= "         D1.APPLICANTDIV = '" .$model->applicantdiv ."' AND ";
        $query .= "         D1.TESTDIV = '" .$model->testdiv ."' AND ";
        $query .= "         D1.EXAM_TYPE = '" .$model->exam_type ."' ";
        $query .= "     GROUP BY ";
        $query .= "         D1.EXAMHALLCD ";
        $query .= "     ) ";

        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T2.DETAIL_CNT ";
        $query .= "    ,R1.EXAMNO AS S_EXAMNO ";
        $query .= "    ,R2.EXAMNO AS E_EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT T1 ";
        $query .= "     LEFT JOIN T_DETAIL T2 ON T2.EXAMHALLCD = T1.EXAMHALLCD ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DAT R1 ";
        $query .= "         ON  R1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND R1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND R1.TESTDIV      = T1.TESTDIV ";
        $query .= "         AND R1.EXAM_TYPE    = T1.EXAM_TYPE ";
        $query .= "         AND R1.RECEPTNO     = T1.S_RECEPTNO ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DAT R2 ";
        $query .= "         ON  R2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND R2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND R2.TESTDIV      = T1.TESTDIV ";
        $query .= "         AND R2.EXAM_TYPE    = T1.EXAM_TYPE ";
        $query .= "         AND R2.RECEPTNO     = T1.E_RECEPTNO ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '" .$model->year ."' AND ";
        if ($model->cmd == "edit"){
            $query .= "     T1.EXAMHALLCD = '" .$model->examhallcd ."' AND ";
        }
        $query .= "     T1.APPLICANTDIV = '" .$model->applicantdiv ."' AND ";
        $query .= "     T1.TESTDIV   = '" .$model->testdiv ."' AND ";
        $query .= "     T1.EXAM_TYPE = '" .$model->exam_type ."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.S_RECEPTNO ";
        return $query;
    }

    //試験会場分割データ
    function getDetailList(&$model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     D1.DETAIL_NO, ";
        $query .= "     D1.CAPA_CNT AS DETAIL_CAPA_CNT, ";
        $query .= "     D1.S_RECEPTNO AS DETAIL_S_RECEPTNO, ";
        $query .= "     D1.E_RECEPTNO AS DETAIL_E_RECEPTNO ";
        $query .= "    ,R1.EXAMNO AS DETAIL_S_EXAMNO ";
        $query .= "    ,R2.EXAMNO AS DETAIL_E_EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_DETAIL_YDAT D1 ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DAT R1 ";
        $query .= "         ON  R1.ENTEXAMYEAR  = D1.ENTEXAMYEAR ";
        $query .= "         AND R1.APPLICANTDIV = D1.APPLICANTDIV ";
        $query .= "         AND R1.TESTDIV      = D1.TESTDIV ";
        $query .= "         AND R1.EXAM_TYPE    = D1.EXAM_TYPE ";
        $query .= "         AND R1.RECEPTNO     = D1.S_RECEPTNO ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DAT R2 ";
        $query .= "         ON  R2.ENTEXAMYEAR  = D1.ENTEXAMYEAR ";
        $query .= "         AND R2.APPLICANTDIV = D1.APPLICANTDIV ";
        $query .= "         AND R2.TESTDIV      = D1.TESTDIV ";
        $query .= "         AND R2.EXAM_TYPE    = D1.EXAM_TYPE ";
        $query .= "         AND R2.RECEPTNO     = D1.E_RECEPTNO ";
        $query .= " WHERE ";
        $query .= "     D1.ENTEXAMYEAR = '" .$model->year ."' AND ";
        $query .= "     D1.APPLICANTDIV = '" .$model->applicantdiv ."' AND ";
        $query .= "     D1.TESTDIV = '" .$model->testdiv ."' AND ";
        $query .= "     D1.EXAM_TYPE = '" .$model->exam_type ."' AND ";
        $query .= "     D1.EXAMHALLCD = '" .$model->examhallcd ."' ";
        $query .= " ORDER BY ";
        $query .= "     D1.S_RECEPTNO ";
        return $query;
    }

    //座席番号生成済みリスト取得
    function getReceptCnt($model)
    {
        $query  = "";
        $query .= " SELECT ";
        //帰国生対応(高校のみ)の場合、帰国生のうちわけも表示する
        $query .= "     VALUE(T1.INTERVIEW_ATTEND_FLG, '0') AS KIKOKU, ";
        //高校(1:学特、3:一般)の場合、共愛中学出身者のうちわけも表示する
        if ($model->applicantdiv == "2" && ($model->testdiv == "1" || $model->testdiv == "3")) {
            $query .= " CASE WHEN SUBSTR(T3.EXAMNO,1,1) = '6' THEN '6' ELSE '0' END AS NAIBU, ";
        }
        $query .= "     T3.DESIREDIV || '-' || VALUE(T3.RECOM_KIND,'0') || ':' || L2.EXAMCOURSE_NAME || ' ' || VALUE(N1.NAME1,'') AS LABEL, ";
        $query .= "     COUNT(T2.RECEPTNO) AS CNT_RECEPTNO, ";
        $query .= "     MIN(T2.RECEPTNO) AS S_RECEPTNO, ";
        $query .= "     MAX(T2.RECEPTNO) AS E_RECEPTNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T2 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "         ON  T1.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "         AND T1.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "         AND T1.EXAMNO = T2.EXAMNO ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTDESIRE_DAT T3 ";
        $query .= "         ON  T3.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "         AND T3.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "         AND T3.TESTDIV = T2.TESTDIV ";
        $query .= "         AND T3.EXAMNO = T2.EXAMNO ";
        $query .= "     LEFT JOIN NAME_MST N1 ";
        $query .= "         ON  N1.NAMECD1 = 'L023' ";
        $query .= "         AND N1.NAMECD2 = T3.RECOM_KIND ";
        $query .= "     LEFT JOIN ENTEXAM_WISHDIV_MST L1 ";
        $query .= "         ON  L1.ENTEXAMYEAR = T3.ENTEXAMYEAR ";
        $query .= "         AND L1.APPLICANTDIV = T3.APPLICANTDIV ";
        $query .= "         AND L1.TESTDIV = T3.TESTDIV ";
        $query .= "         AND L1.DESIREDIV = T3.DESIREDIV ";
        $query .= "         AND L1.WISHNO = '1' ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST L2 ";
        $query .= "         ON  L2.ENTEXAMYEAR = L1.ENTEXAMYEAR ";
        $query .= "         AND L2.APPLICANTDIV = L1.APPLICANTDIV ";
        $query .= "         AND L2.TESTDIV = L1.TESTDIV ";
        $query .= "         AND L2.COURSECD = L1.COURSECD ";
        $query .= "         AND L2.MAJORCD = L1.MAJORCD ";
        $query .= "         AND L2.EXAMCOURSECD = L1.EXAMCOURSECD ";
        $query .= " WHERE ";
        $query .= "     T2.ENTEXAMYEAR     = '".$model->year."' AND ";
        $query .= "     T2.APPLICANTDIV    = '".$model->applicantdiv."' AND ";
        $query .= "     T2.TESTDIV         = '".$model->testdiv."' ";
        $query .= " GROUP BY ";
        //帰国生対応(高校のみ)の場合、帰国生のうちわけも表示する
        $query .= "     VALUE(T1.INTERVIEW_ATTEND_FLG, '0'), ";
        $query .= "     T3.DESIREDIV, ";
        $query .= "     VALUE(T3.RECOM_KIND,'0'), ";
        //高校(1:学特、3:一般)の場合、共愛中学出身者のうちわけも表示する
        if ($model->applicantdiv == "2" && ($model->testdiv == "1" || $model->testdiv == "3")) {
            $query .= " CASE WHEN SUBSTR(T3.EXAMNO,1,1) = '6' THEN '6' ELSE '0' END, ";
        }
        $query .= "     L2.EXAMCOURSE_NAME, ";
        $query .= "     VALUE(N1.NAME1,'') ";
        $query .= " ORDER BY ";
        $query .= "     S_RECEPTNO ";

        return $query;
    }

    //受験番号取得
    function getReceptExamno($model, $receptno) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T2.EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T2.ENTEXAMYEAR     = '{$model->year}' AND ";
        $query .= "     T2.APPLICANTDIV    = '{$model->applicantdiv}' AND ";
        $query .= "     T2.TESTDIV         = '{$model->testdiv}' AND ";
        $query .= "     T2.RECEPTNO        = '{$receptno}' ";
        return $query;
    }

    //受付データと会場データの人数チェック
    function receptnoCheck(&$model)
    {
        $db = Query::dbCheckOut();

        $query  = "";
        $query .= " SELECT ";
        $query .= "     SUM(T1.CAPA_CNT) AS CAPA_CNT_HALL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '" .$model->year ."' AND ";
        if ($model->cmd == "update"){
            $query .= "     T1.EXAMHALLCD NOT IN ('" .$model->examhallcd ."') AND ";
        }
        $query .= "     T1.APPLICANTDIV = '" .$model->applicantdiv ."' AND ";
        $query .= "     T1.TESTDIV   = '" .$model->testdiv ."' AND ";
        $query .= "     T1.EXAM_TYPE = '" .$model->exam_type ."' ";

        $cnt = $db->getOne($query);
        Query::dbCheckIn($db);
        return $cnt;
    }

    //更新
    function update(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_HALL_YDAT
        $query  = " UPDATE ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " SET ";
        $query .= "     EXAMHALL_NAME = '" .Query::addquote($model->field["EXAMHALL_NAME"]) ."', ";
        $query .= "     CAPA_CNT = " .$model->field["CAPA_CNT"] .", ";
        $query .= "     REGISTERCD = '".STAFFCD ."', ";
        $query .= "     UPDATED  = SYSDATE() ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '" .$model->year ."' AND ";
        $query .= "     APPLICANTDIV = '" .$model->applicantdiv ."' AND ";
        $query .= "     TESTDIV = '" .$model->testdiv ."' AND ";
        $query .= "     EXAM_TYPE = '" .$model->exam_type ."' AND ";
        $query .= "     EXAMHALLCD = '" .$model->examhallcd ."' ";
        $db->query($query);

        //ENTEXAM_HALL_DETAIL_YDAT
        //削除
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_DETAIL_YDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '" .$model->year ."' AND ";
        $query .= "     APPLICANTDIV = '" .$model->applicantdiv ."' AND ";
        $query .= "     TESTDIV = '" .$model->testdiv ."' AND ";
        $query .= "     EXAM_TYPE = '" .$model->exam_type ."' AND ";
        $query .= "     EXAMHALLCD = '" .$model->examhallcd ."' ";
        $db->query($query);
        //追加
        for ($detail_no = 1; $detail_no <= (int) $model->field["DETAIL_CNT"]; $detail_no++) {
            $query  = " INSERT INTO ENTEXAM_HALL_DETAIL_YDAT( ";
            $query .= " ENTEXAMYEAR, ";
            $query .= " APPLICANTDIV, ";
            $query .= " TESTDIV, ";
            $query .= " EXAM_TYPE, ";
            $query .= " EXAMHALLCD, ";
            $query .= " DETAIL_NO, ";
            $query .= " CAPA_CNT, ";
            $query .= " REGISTERCD ";
            $query .= " ) ";
            $query .= " VALUES ";
            $query .= " ( ";
            $query .= " '" .$model->year ."', ";
            $query .= " '" .$model->applicantdiv ."', ";
            $query .= " '" .$model->testdiv ."', ";
            $query .= " '" .$model->exam_type ."', ";
            $query .= " '".$model->examhallcd. "', ";
            $query .= " '".$detail_no. "', ";
            $query .= " " .$model->field["DETAIL_CAPA_CNT" .$detail_no] .", ";
            $query .= " '".STAFFCD ."' ";
            $query .= " ) ";
            $db->query($query);
        }

        //割り振り実行
        knjl030yQuery::exec($db, $model);

        $db->commit(); 
        Query::dbCheckIn($db);
    }

    //追加
    function insert(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_HALL_YDAT
        $query = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '" .$model->year ."' AND ";
        $query .= "     APPLICANTDIV = '" .$model->applicantdiv ."' AND ";
        $query .= "     TESTDIV = '" .$model->testdiv ."' AND ";
        $query .= "     EXAM_TYPE = '" .$model->exam_type ."' ";
        $query .= " ORDER BY ";
        $query .= "     EXAMHALLCD DESC ";

        //会場コード、開始座席番号を設定。
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (is_array($row)){
            $examhallcd = (int) $row["EXAMHALLCD"] + 1;
            $s_receptno = (int) $row["E_RECEPTNO"] + 1;
        } else {
            $examhallcd = 1;
            $s_receptno = 1;
        }
        $e_receptno = $s_receptno + (int) $model->field["CAPA_CNT"] - 1;

        $query  = " INSERT INTO ENTEXAM_HALL_YDAT( ";
        $query .= " ENTEXAMYEAR, ";
        $query .= " APPLICANTDIV, ";
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
        $query .= " '" .$model->year ."', ";
        $query .= " '" .$model->applicantdiv ."', ";
        $query .= " '" .$model->testdiv ."', ";
        $query .= " '" .$model->exam_type ."', ";
        $query .= " '".sprintf("%04d", $examhallcd). "', ";
        $query .= " '" .Query::addquote($model->field["EXAMHALL_NAME"]) ."', ";
        $query .= " " .$model->field["CAPA_CNT"] .", ";
        $query .= " '".sprintf("%05d", $s_receptno). "', ";
        $query .= " '".sprintf("%05d", $e_receptno). "', ";
        $query .= " '".STAFFCD ."' ";
        $query .= " ) ";
        $db->query($query);

        //ENTEXAM_HALL_DETAIL_YDAT
        //削除
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_DETAIL_YDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '" .$model->year ."' AND ";
        $query .= "     APPLICANTDIV = '" .$model->applicantdiv ."' AND ";
        $query .= "     TESTDIV = '" .$model->testdiv ."' AND ";
        $query .= "     EXAM_TYPE = '" .$model->exam_type ."' AND ";
        $query .= "     EXAMHALLCD = '" .sprintf("%04d", $examhallcd) ."' ";
        $db->query($query);
        //追加
        for ($detail_no = 1; $detail_no <= (int) $model->field["DETAIL_CNT"]; $detail_no++) {
            if (1 < $detail_no){
                $detail_s_receptno = (int) $detail_e_receptno + 1;
            } else {
                $detail_s_receptno = $s_receptno;
            }
            $detail_e_receptno = $detail_s_receptno + (int) $model->field["DETAIL_CAPA_CNT" .$detail_no] - 1;

            $query  = " INSERT INTO ENTEXAM_HALL_DETAIL_YDAT( ";
            $query .= " ENTEXAMYEAR, ";
            $query .= " APPLICANTDIV, ";
            $query .= " TESTDIV, ";
            $query .= " EXAM_TYPE, ";
            $query .= " EXAMHALLCD, ";
            $query .= " DETAIL_NO, ";
            $query .= " CAPA_CNT, ";
            $query .= " S_RECEPTNO, ";
            $query .= " E_RECEPTNO, ";
            $query .= " REGISTERCD ";
            $query .= " ) ";
            $query .= " VALUES ";
            $query .= " ( ";
            $query .= " '" .$model->year ."', ";
            $query .= " '" .$model->applicantdiv ."', ";
            $query .= " '" .$model->testdiv ."', ";
            $query .= " '" .$model->exam_type ."', ";
            $query .= " '".sprintf("%04d", $examhallcd). "', ";
            $query .= " '".$detail_no. "', ";
            $query .= " " .$model->field["DETAIL_CAPA_CNT" .$detail_no] .", ";
            $query .= " '".sprintf("%05d", $detail_s_receptno). "', ";
            $query .= " '".sprintf("%05d", $detail_e_receptno). "', ";
            $query .= " '".STAFFCD ."' ";
            $query .= " ) ";
            $db->query($query);
        }

        $db->commit(); 
        Query::dbCheckIn($db);
    }

    //削除
    function delete(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_HALL_YDAT
        $query = " DELETE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '" .$model->year ."' AND ";
        $query .= "     APPLICANTDIV = '" .$model->applicantdiv ."' AND ";
        $query .= "     TESTDIV = '" .$model->testdiv ."' AND ";
        $query .= "     EXAM_TYPE = '" .$model->exam_type ."' AND ";
        $query .= "     EXAMHALLCD IN ('" .implode($model->field["CHECKED"],"','") ."') ";
        $db->query($query);
        //ENTEXAM_HALL_DETAIL_YDAT
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_DETAIL_YDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '" .$model->year ."' AND ";
        $query .= "     APPLICANTDIV = '" .$model->applicantdiv ."' AND ";
        $query .= "     TESTDIV = '" .$model->testdiv ."' AND ";
        $query .= "     EXAM_TYPE = '" .$model->exam_type ."' AND ";
        $query .= "     EXAMHALLCD IN ('" .implode($model->field["CHECKED"],"','") ."') ";
        $db->query($query);

        //割り振り実行
        knjl030yQuery::exec($db, $model);

        $db->commit(); 
        Query::dbCheckIn($db);
    }

    //割り振り実行
    function exec(&$db, &$model) {
        $query  = " UPDATE ";
        $query .= "     ENTEXAM_HALL_YDAT T1 ";
        $query .= " SET ";
        $query .= "     (S_RECEPTNO,E_RECEPTNO)= (SELECT ";
        $query .= "         SUBSTR(DIGITS(COALESCE(SUM(ST1.CAPA_CNT),0)+1), 6, 5), ";
        $query .= "         SUBSTR(DIGITS(COALESCE(SUM(ST1.CAPA_CNT),0)+T1.CAPA_CNT), 6, 5) ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_HALL_YDAT ST1 ";
        $query .= "     WHERE ";
        $query .= "         ST1.ENTEXAMYEAR = T1.ENTEXAMYEAR AND ";
        $query .= "         ST1.APPLICANTDIV = T1.APPLICANTDIV AND ";
        $query .= "         ST1.TESTDIV = T1.TESTDIV AND ";
        $query .= "         ST1.EXAM_TYPE = T1.EXAM_TYPE AND ";
        $query .= "         INTEGER(ST1.EXAMHALLCD) < INTEGER(T1.EXAMHALLCD) ";
        $query .= "     ) ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '" .$model->year ."' AND ";
        $query .= "     T1.APPLICANTDIV = '" .$model->applicantdiv ."' AND ";
        $query .= "     T1.TESTDIV = '" .$model->testdiv ."' AND ";
        $query .= "     T1.EXAM_TYPE = '" .$model->exam_type ."' ";
        $db->query($query);

        //会場コード・開始番号を取得
        $query  = " SELECT ";
        $query .= "     T1.EXAMHALLCD, ";
        $query .= "     T1.S_RECEPTNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '" .$model->year ."' AND ";
        $query .= "     T1.APPLICANTDIV = '" .$model->applicantdiv ."' AND ";
        $query .= "     T1.TESTDIV = '" .$model->testdiv ."' AND ";
        $query .= "     T1.EXAM_TYPE = '" .$model->exam_type ."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.S_RECEPTNO ";
        //配列にセット
        $detailArray = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $detailArray[$row["EXAMHALLCD"]] = (int) $row["S_RECEPTNO"];
        }
        $result->free();
        //割り振り実行(分割)
        foreach ($detailArray as $examhallcd => $s_receptno) {
            knjl030yQuery::execDetail($db, $model, $examhallcd, $s_receptno);
        }
    }

    //割り振り実行(分割)
    function execDetail(&$db, &$model, $examhallcd, $s_receptno) {
        $query  = " UPDATE ";
        $query .= "     ENTEXAM_HALL_DETAIL_YDAT T1 ";
        $query .= " SET ";
        $query .= "     (S_RECEPTNO,E_RECEPTNO)= (SELECT ";
        $query .= "         SUBSTR(DIGITS(COALESCE(SUM(ST1.CAPA_CNT),0)+".$s_receptno."), 6, 5), ";
        $query .= "         SUBSTR(DIGITS(COALESCE(SUM(ST1.CAPA_CNT),0)+".$s_receptno."+T1.CAPA_CNT-1), 6, 5) ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_HALL_DETAIL_YDAT ST1 ";
        $query .= "     WHERE ";
        $query .= "         ST1.ENTEXAMYEAR = T1.ENTEXAMYEAR AND ";
        $query .= "         ST1.APPLICANTDIV = T1.APPLICANTDIV AND ";
        $query .= "         ST1.TESTDIV = T1.TESTDIV AND ";
        $query .= "         ST1.EXAM_TYPE = T1.EXAM_TYPE AND ";
        $query .= "         ST1.EXAMHALLCD = T1.EXAMHALLCD AND ";
        $query .= "         INTEGER(ST1.DETAIL_NO) < INTEGER(T1.DETAIL_NO) ";
        $query .= "     ) ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '" .$model->year ."' AND ";
        $query .= "     T1.APPLICANTDIV = '" .$model->applicantdiv ."' AND ";
        $query .= "     T1.TESTDIV = '" .$model->testdiv ."' AND ";
        $query .= "     T1.EXAM_TYPE = '" .$model->exam_type ."' AND ";
        $query .= "     T1.EXAMHALLCD = '" .$examhallcd ."' ";

        $db->query($query);
    }
}
?>