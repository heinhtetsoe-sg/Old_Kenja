<?php

require_once('for_php7.php');

class knjl252yQuery extends Query {

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
        $query .= "     NAMECD1 = 'L030' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //グループ
    function getHallName($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.EXAMHALLCD AS VALUE, ";
        $query .= "     T1.EXAMHALLCD || ':' || T1.EXAMHALL_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND T1.EXAM_TYPE    = '{$model->exam_type}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //一覧表示
    function SelectQuery($model) {
        $query  = "";
        //得点データ
        $query .= "WITH T_INTERVIEWE AS ( ";
        $query .= "    SELECT S1.EXAMNO ";
        $query .= "      ,S1.INTERVIEW_VALUE2 ";
        $query .= "      ,S1.INTERVIEW_REMARK2 ";
        $query .= "      FROM ENTEXAM_INTERVIEW_DAT S1 ";
        $query .= "     WHERE S1.ENTEXAMYEAR    = '{$model->ObjYear}' ";
        $query .= "       AND S1.APPLICANTDIV   = '{$model->applicantdiv}' ";
        $query .= "       AND S1.TESTDIV        = '{$model->testdiv}' ";
        $query .= ") ";
        //メイン
        $query .= "SELECT T1.RECEPTNO ";
        $query .= "      ,T1.EXAMNO ";
        $query .= "      ,T2.NAME";
        $query .= "      ,S1.INTERVIEW_VALUE2 ";
        $query .= "      ,S1.INTERVIEW_REMARK2 ";
        $query .= "  FROM ENTEXAM_RECEPT_DAT T1 ";
        $query .= "       INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "               ON T2.ENTEXAMYEAR    = T1.ENTEXAMYEAR ";
        $query .= "              AND T2.EXAMNO         = T1.EXAMNO ";
        $query .= "              AND T2.APPLICANTDIV   = T1.APPLICANTDIV ";
        $query .= "       INNER JOIN ENTEXAM_HALL_LIST_YDAT H1 ";
        $query .= "               ON H1.ENTEXAMYEAR    = T1.ENTEXAMYEAR ";
        $query .= "              AND H1.APPLICANTDIV   = T1.APPLICANTDIV ";
        $query .= "              AND H1.TESTDIV        = T1.TESTDIV ";
        $query .= "              AND H1.EXAM_TYPE      = T1.EXAM_TYPE ";
        $query .= "              AND H1.EXAMHALLCD     = '{$model->examhallcd}' ";
        $query .= "              AND H1.RECEPTNO       = T1.RECEPTNO ";
        $query .= "        LEFT JOIN T_INTERVIEWE S1 ON S1.EXAMNO = T1.RECEPTNO ";
        $query .= " WHERE T1.ENTEXAMYEAR    = '{$model->ObjYear}' ";
        $query .= "   AND T1.APPLICANTDIV   = '{$model->applicantdiv}' ";
        $query .= "   AND T1.TESTDIV        = '{$model->testdiv}' ";
        $query .= "   AND T1.EXAM_TYPE      = '{$model->exam_type}' ";
        $query .= " ORDER BY T1.RECEPTNO";

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
            $data["INTERVIEW_VALUE2"][TEXT]  = $model->fields["INTERVIEW_VALUE2"][$i];
            $data["INTERVIEW_REMARK2"][TEXT] = $model->fields["INTERVIEW_REMARK2"][$i];
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