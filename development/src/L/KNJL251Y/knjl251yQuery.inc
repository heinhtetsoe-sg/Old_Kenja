<?php

require_once('for_php7.php');

class knjl251yQuery extends Query {

    //名称マスタより取得
    function getNameMst($namecd1, $year, $namecd2 = "") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        //推薦入試は表示しない
        if (strlen($namecd2)) {
            $query .= " AND NAMECD2 != '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //評価値を取得
    function getHyouteiData() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'L027' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //一覧表示
    function SelectQuery($model, $flg) {
        $query  = " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        if ($flg == "COUNT") {
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query .= "      T1.RECEPTNO ";
            $query .= "     ,T1.EXAMNO ";
            $query .= "     ,T2.NAME";
            $query .= "     ,T3.GNAME";
            if ($model->field["OUTPUT"] == "1") {
                $query .= "     ,S1.INTERVIEW_VALUE ";
                $query .= "     ,S1.INTERVIEW_REMARK ";
            } else {
                $query .= "     ,S1.INTERVIEW_REMARK3 AS INTERVIEW_REMARK ";
            }
        }
        $query .= "  FROM ENTEXAM_RECEPT_DAT T1 ";
        $query .= "       INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "               ON T2.ENTEXAMYEAR    = T1.ENTEXAMYEAR ";
        $query .= "              AND T2.EXAMNO         = T1.EXAMNO ";
        $query .= "              AND T2.APPLICANTDIV   = T1.APPLICANTDIV ";
        $query .= "       LEFT  JOIN ENTEXAM_APPLICANTADDR_DAT T3 ";
        $query .= "               ON T3.ENTEXAMYEAR    = T1.ENTEXAMYEAR ";
        $query .= "              AND T3.EXAMNO         = T1.EXAMNO ";
        $query .= "       LEFT JOIN ENTEXAM_INTERVIEW_DAT S1 ON S1.EXAMNO = T1.EXAMNO ";
        $query .= "                                         AND S1.ENTEXAMYEAR    = T1.ENTEXAMYEAR ";
        $query .= "                                         AND S1.APPLICANTDIV   = T1.APPLICANTDIV ";
        $query .= "                                         AND S1.TESTDIV        = T1.TESTDIV ";
        $query .= " WHERE T1.ENTEXAMYEAR    = '{$model->ObjYear}' ";
        $query .= "   AND T1.APPLICANTDIV   = '{$model->applicantdiv}' ";
        $query .= "   AND T1.TESTDIV        = '{$model->testdiv}' ";
        $query .= "   AND T1.EXAM_TYPE      = '{$model->exam_type}' ";
        if ($model->cmd == "read" || $model->hoji_cmd == "read") {
            $query .= "   AND T1.RECEPTNO >= '".$model->field["S_EXAMNO"]."' ";
            if ($flg != "COUNT") {
                $query .= " ORDER BY T1.RECEPTNO ";
            }
        } else if ($model->cmd == "back" || $model->hoji_cmd == "back") {
            $query .= "   AND T1.RECEPTNO < '".$model->field["S_EXAMNO"]."' ";
            if ($flg != "COUNT") {
                $query .= " ORDER BY T1.RECEPTNO ";
            }
        } else if ($model->cmd == "next" || $model->hoji_cmd == "next") {
            $query .= "   AND T1.RECEPTNO > '".$model->field["E_EXAMNO"]."' ";
            if ($flg != "COUNT") {
                $query .= " ORDER BY T1.RECEPTNO ";
            }
        }
        $query .= " FETCH FIRST 50 ROWS ONLY ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= "  FROM ";
        $query .= "     MAIN_T ";
        if ($flg != "COUNT") {
            $query .= " ORDER BY RECEPTNO ";
        }

        return $query;
    }

    //更新(一度すべて削除してから追加）
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        for ($i = 0; $i < get_count($model->data["EXAMNO"]); $i++) {
            $examno = $model->data["EXAMNO"][$i];
            
            //データチェック
            $query  = " SELECT ";
            $query .= "     COUNT(*) AS CNT ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_INTERVIEW_DAT ";
            $query .= " WHERE ENTEXAMYEAR    = '{$model->ObjYear}'";
            $query .= "   AND APPLICANTDIV   = '{$model->applicantdiv}'";
            $query .= "   AND TESTDIV        = '{$model->testdiv}'";
            $query .= "   AND EXAMNO         = '{$examno}'";
            $getCount = $db->getOne($query);
            
            //データ更新
            $data = array();
            if ($model->field["OUTPUT"] == "1") {
                $data["INTERVIEW_VALUE"][TEXT]   = $model->fields["INTERVIEW_VALUE"][$i];
                $data["INTERVIEW_REMARK"][TEXT]  = $model->fields["INTERVIEW_REMARK"][$i];
            } else {
                $data["INTERVIEW_REMARK3"][TEXT] = $model->fields["INTERVIEW_REMARK"][$i];
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            if ($getCount == 0) {
                $data["ENTEXAMYEAR"][TEXT]       = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]      = $model->applicantdiv;
                $data["TESTDIV"][TEXT]           = $model->testdiv;
                $data["EXAMNO"][TEXT]            = $examno;
                $query = Query::insertSQL($data, "ENTEXAM_INTERVIEW_DAT");
            } else {
                $where  = " WHERE ENTEXAMYEAR    = '{$model->ObjYear}'";
                $where .= "   AND APPLICANTDIV   = '{$model->applicantdiv}'";
                $where .= "   AND TESTDIV        = '{$model->testdiv}'";
                $where .= "   AND EXAMNO         = '{$examno}'";
                $query = Query::updateSQL($data, "ENTEXAM_INTERVIEW_DAT", $where);
            }
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>