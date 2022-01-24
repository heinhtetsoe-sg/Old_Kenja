<?php

require_once('for_php7.php');

class knjl041qQuery extends Query {

    //名称マスタより取得
    function getNameMst($namecd1, $year, $namecd2 = "") {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd1 == "L003") {
            $query .= "     AND NAMESPARE3 = '".SCHOOLKIND."' ";
        }
        if ($namecd1 == "L004") {
            $query .= "     AND NAMECD2 != '9' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //一覧表示
    function SelectQuery($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.RECEPTNO, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T1.JUDGEDIV, ";
        $query .= "     FIN_M.FINSCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "              ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "             AND T2.EXAMNO       = T1.EXAMNO ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FIN_M ";
        $query .= "              ON T2.FS_CD        = FIN_M.FINSCHOOLCD ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR    = '".$model->year."' ";
        $query .= "     AND T1.APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "     AND T1.TESTDIV        = '".$model->testdiv."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.RECEPTNO ";

        return $query;
    }

    //更新
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //チェックあり：欠席　チェックなし：NULL
        //受付データ
        $data = array();
        $data["JUDGEDIV"][TEXT]         = $model->field["CHK_DATA"][$model->hid_receptno];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        //条件
        $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
        $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
        $where .= "   AND TESTDIV       = '".$model->testdiv."' ";
        $where .= "   AND RECEPTNO      = '".$model->hid_receptno."' ";
        $where .= "   AND VALUE(JUDGEDIV, '4') = '4' "; //欠席またはNULL
        $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
        $db->query($query);

        //基礎データ
        $data = array();
        $data["JUDGEMENT"][TEXT]        = $model->field["CHK_DATA"][$model->hid_receptno];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        //条件
        $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
        $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
        $where .= "   AND EXAMNO        = '".$model->hid_examno2."'";
        $where .= "   AND VALUE(JUDGEMENT, '4') = '4' ";
        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
?>