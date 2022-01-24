<?php

require_once('for_php7.php');

class knjl071pQuery extends Query {

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
        if ($namecd1 == "L013") {
            $query .= " AND NAMECD2 <> '4' ";
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
        if ($model->field["MENSETU"] == "1") {
            $query .= "     T1.TOTAL4 AS TOTAL, ";
            $query .= "     T1.JUDGE_DEVIATION AS JUDGE_DEVIATION, ";
            $query .= "     T1.TOTAL_RANK4 AS TOTAL_RANK, ";
        } else {
            $query .= "     T1.TOTAL2 AS TOTAL, ";
            $query .= "     T1.LINK_JUDGE_DEVIATION AS JUDGE_DEVIATION, ";
            $query .= "     T1.TOTAL_RANK2 AS TOTAL_RANK, ";
        }
        $query .= "     T2.PROCEDUREDIV, ";
        $query .= "     T2.ENTDIV, ";
        $query .= "     T1.JUDGEDIV, ";
        $query .= "     N1.NAME1 AS JUDGEDIV_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "              ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "             AND T2.EXAMNO       = T1.EXAMNO ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ";
        $query .= "              ON N1.YEAR     = T1.ENTEXAMYEAR ";
        $query .= "             AND N1.NAMECD1  = 'L013' ";
        $query .= "             AND N1.NAMECD2  = T1.JUDGEDIV ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR    = '".$model->year."' ";
        $query .= "     AND T1.APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "     AND T1.TESTDIV        = '".$model->testdiv."' ";
        $query .= " ORDER BY ";
        if ($model->field["ORDERDIV"] == "1") {
            $query .= "     T1.TOTAL_RANK4, ";
        }
        $query .= "     T1.RECEPTNO ";

        return $query;
    }

    //合格コース
    function getCourse($model) {
        $query  = " SELECT ";
        $query .= "     COURSECD || MAJORCD || EXAMCOURSECD AS COURSE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV= '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV     = '{$model->testdiv}' ";
        return $query;
    }

    //更新
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //合格コース
        $course = $db->getOne(knjl071pQuery::getCourse($model));

        $keyList = array();
        $keyList = explode(",", $model->hid_examno);
        //チェックあり：更新する   チェックなし：更新しない
        foreach ($keyList as $key => $val) {
            $tmp = explode("-", $val);
            $receptno = $tmp[0];
            $examno = $tmp[1];

            //チェックありの人だけ更新する。
            if ($model->field2["CHK_DATA"][$receptno] != "on") continue;

            //受付データ
            $data = array();
            $data["JUDGEDIV"][TEXT]         = $model->judgediv;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            //条件
            $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
            $where .= "   AND TESTDIV       = '".$model->testdiv."' ";
            $where .= "   AND RECEPTNO      = '".$receptno."' ";
            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
            $db->query($query);

            //基礎データ
            $data = array();
            $data["JUDGEMENT"][TEXT]        = $model->judgediv;
            $data["SUC_COURSECD"][TEXT]     = ($model->judgediv == "1") ? substr($course, 0, 1) : "";
            $data["SUC_MAJORCD"][TEXT]      = ($model->judgediv == "1") ? substr($course, 1, 3) : "";
            $data["SUC_COURSECODE"][TEXT]   = ($model->judgediv == "1") ? substr($course, 4, 4) : "";
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            //条件
            $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
            $where .= "   AND EXAMNO        = '".$examno."'";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
?>