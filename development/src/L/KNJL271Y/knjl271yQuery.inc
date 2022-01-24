<?php

require_once('for_php7.php');

class knjl271yQuery extends Query {

    //名称マスタより取得
    function getNameMst($namecd1, $year) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //一覧表示
    function SelectQuery($model) {
        //試験出力情報 1:傾斜配点 2:通常得点
        //合計
        //3:筆記試験得点（重みなし）＋行動観察評価
        //4:筆記試験得点（重みあり）＋行動観察評価
        $totalNo = ($model->rate_div == "2") ? "3" : "4";

        $query  = "";
        $query .= "SELECT T1.RECEPTNO ";
        $query .= "      ,T1.EXAMNO ";
        $query .= "      ,T2.NAME ";
        $query .= "      ,T1.TOTAL{$totalNo}       AS TOTAL ";
        $query .= "      ,T1.TOTAL_RANK{$totalNo}  AS TOTAL_RANK ";
//        $query .= "      ,T1.ATTEND_ALL_FLG ";
        $query .= "      ,T1.JUDGEDIV ";
        $query .= "      ,N1.NAME1 AS JUDGEDIV_NAME ";
        $query .= "      ,N1.ABBV1 AS JUDGEDIV_ABBV ";
        $query .= "      ,T2.PROCEDUREDIV ";
        $query .= "      ,T2.ENTDIV ";
        $query .= "  FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "              ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "             AND T2.EXAMNO       = T1.EXAMNO ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ";
        $query .= "              ON N1.YEAR         = T1.ENTEXAMYEAR ";
        $query .= "             AND N1.NAMECD1      = 'L013' ";
        $query .= "             AND N1.NAMECD2      = T1.JUDGEDIV ";
        $query .= " WHERE T1.ENTEXAMYEAR    = '{$model->ObjYear}'";
        $query .= "   AND T1.APPLICANTDIV   = '{$model->applicantdiv}' ";
        $query .= "   AND T1.TESTDIV        = '{$model->testdiv}' ";
        $query .= "   AND T1.EXAM_TYPE      = '{$model->exam_type}' ";
        $query .= " ORDER BY ";
        //表示順序ラジオボタン 1:成績順 2:受験番号順
        if ($model->sort == "1") {
            $query .= "     value(T1.TOTAL_RANK{$totalNo}, 999), ";
//            $query .= "     value(T1.TOTAL{$totalNo}, -1) DESC, ";
        }
        $query .= "     T1.EXAMNO ";
        return $query;
    }

    //更新(一度すべて削除してから追加）
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //更新前のデータを取得
        $preData = array();
        $result    = $db->query(knjl271yQuery::SelectQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $preData["JUDGEDIV"][$row["EXAMNO"]]        = $row["JUDGEDIV"];
            $preData["PROCEDUREDIV"][$row["EXAMNO"]]    = $row["PROCEDUREDIV"];
            $preData["ENTDIV"][$row["EXAMNO"]]          = $row["ENTDIV"];
        }

        foreach ($model->score as $examno => $val) {
            //更新前の値が変更されてない場合は何もしない。つまり、変更データのみ更新する。
            if ($preData["JUDGEDIV"][$examno] == $val["JUDGEDIV"]) {
                continue;
            }
            //更新前の値が欠席または手続済みの人は何もしない。
            if ($preData["JUDGEDIV"][$examno] == "4" || $preData["PROCEDUREDIV"][$examno] == "1" || $preData["ENTDIV"][$examno] == "1") {
                continue;
            }

            //受付データ
            $data = array();
            $data["JUDGEDIV"][TEXT]         = $val["JUDGEDIV"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            //条件
            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND";
            $where .= "       APPLICANTDIV  = '{$model->applicantdiv}' AND";
            $where .= "       TESTDIV       = '{$model->testdiv}' AND";
            $where .= "       EXAMNO        = '{$examno}'";
            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
            $db->query($query);

            //基礎データ
            $data = array();
            $data["JUDGEMENT"][TEXT]        = $val["JUDGEDIV"];
            $data["PROCEDUREDATE"][TEXT]    = "";
            $data["PROCEDUREDIV"][TEXT]     = "";
            $data["ENTDIV"][TEXT]           = "";
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            //条件
            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND";
            $where .= "       APPLICANTDIV  = '{$model->applicantdiv}' AND";
            $where .= "       EXAMNO        = '{$examno}'";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>