<?php

require_once('for_php7.php');
class knjl052eQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1, $namecd2="", $nameSpare3="") {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        if ($nameSpare3) {
            $query .= " AND NAMESPARE3 = '".$nameSpare3."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //会場取得
    function getHallData($model) {
        $query  = " SELECT ";
        $query .= "     EXAMHALL_NAME AS LABEL, ";
        $query .= "     EXAMHALLCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR         = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV    = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV         = '{$model->testdiv}' ";
        $query .= "     AND EXAM_TYPE       = '2' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     HALLGY.EXAMHALLGROUP_NAME, ";
        $query .= "     HALLGD.EXAMHALLGROUP_ORDER, ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     INTERVIEW.INTERVIEW_A, ";
        $query .= "     INTERVIEW.INTERVIEW_B, ";
        $query .= "     SEQ033.REMARK1 AS SEQ033_R1, ";
        $query .= "     SEQ033.REMARK2 AS SEQ033_R2, ";
        $query .= "     INTERVIEW.INTERVIEW_A, ";
        $query .= "     INTERVIEW.INTERVIEW_B, ";
        $query .= "     INTERVIEW.OTHER_REMARK1, ";
        $query .= "     INTERVIEW.OTHER_REMARK2, ";
        $query .= "     INTERVIEW.OTHER_REMARK4, ";
        $query .= "     INTERVIEW.OTHER_REMARK5 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     INNER JOIN ENTEXAM_HALL_GROUP_DAT HALLGD ";
        $query .= "          ON BASE.ENTEXAMYEAR      = HALLGD.ENTEXAMYEAR ";
        $query .= "         AND BASE.APPLICANTDIV     = HALLGD.APPLICANTDIV ";
        $query .= "         AND BASE.TESTDIV          = HALLGD.TESTDIV ";
        $query .= "         AND HALLGD.EXAMHALL_TYPE  = '2' ";
        $query .= "         AND HALLGD.EXAMHALLCD     = '{$model->examhallcd}' ";
        $query .= "         AND BASE.EXAMNO           = HALLGD.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_HALL_GROUP_YMST HALLGY ";
        $query .= "          ON HALLGD.ENTEXAMYEAR      = HALLGY.ENTEXAMYEAR ";
        $query .= "         AND HALLGD.APPLICANTDIV     = HALLGY.APPLICANTDIV ";
        $query .= "         AND HALLGD.TESTDIV          = HALLGY.TESTDIV ";
        $query .= "         AND HALLGD.EXAMHALL_TYPE    = HALLGY.EXAMHALL_TYPE ";
        $query .= "         AND HALLGD.EXAMHALLCD       = HALLGY.EXAMHALLCD ";
        $query .= "         AND HALLGD.EXAMHALLGROUPCD  = HALLGY.EXAMHALLGROUPCD ";
        $query .= "     LEFT JOIN ENTEXAM_INTERVIEW_DAT INTERVIEW ";
        $query .= "          ON BASE.ENTEXAMYEAR      = INTERVIEW.ENTEXAMYEAR ";
        $query .= "         AND BASE.APPLICANTDIV     = INTERVIEW.APPLICANTDIV ";
        $query .= "         AND BASE.TESTDIV          = INTERVIEW.TESTDIV ";
        $query .= "         AND BASE.EXAMNO           = INTERVIEW.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ033 ";
        $query .= "          ON BASE.ENTEXAMYEAR      = SEQ033.ENTEXAMYEAR ";
        $query .= "         AND BASE.APPLICANTDIV     = SEQ033.APPLICANTDIV ";
        $query .= "         AND BASE.EXAMNO           = SEQ033.EXAMNO ";
        $query .= "         AND SEQ033.SEQ            = '033' ";
        $query .= " WHERE ";
        $query .= "         BASE.ENTEXAMYEAR            = '{$model->ObjYear}' ";
        $query .= "     AND BASE.APPLICANTDIV           = '{$model->applicantdiv}' ";
        $query .= "     AND BASE.TESTDIV                = '{$model->testdiv}' ";
        $query .= "     AND VALUE(BASE.JUDGEMENT, '0') != '3' ";
        $query .= " ORDER BY ";
        $query .= "     HALLGD.EXAMHALLGROUPCD, ";
        $query .= "     int(HALLGD.EXAMHALLGROUP_ORDER), ";
        $query .= "     BASE.EXAMNO ";

        return $query;
    }

    //更新(一度すべて削除してから追加）
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knjl052eQuery::getNameMst($model->ObjYear, "L004", $model->testdiv);
        $l004Row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //追加
        foreach ($model->interview as $examno => $value) {
            $query  = " SELECT ";
            $query .= "     COUNT(*) AS CNT ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_INTERVIEW_DAT ";
            $query .= " WHERE ENTEXAMYEAR    = '{$model->ObjYear}' ";
            $query .= "   AND APPLICANTDIV   = '{$model->applicantdiv}' ";
            $query .= "   AND TESTDIV        = '{$model->testdiv}' ";
            $query .= "   AND EXAMNO         = '{$examno}' ";
            $dataCnt = $db->getOne($query);

            $data = array();
            $data["OTHER_REMARK1"][TEXT]    = !strlen($value["1"]) ? "" : $value["1"];
            $data["OTHER_REMARK2"][TEXT]    = !strlen($value["2"]) ? "" : $value["2"];
            $data["OTHER_REMARK4"][TEXT]    = !strlen($value["4"]) ? "" : $value["4"];
            $data["OTHER_REMARK5"][TEXT]    = !strlen($value["5"]) ? "" : $value["5"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            if ($dataCnt > 0) {
                $where  = " WHERE ENTEXAMYEAR    = '{$model->ObjYear}' ";
                $where .= "   AND APPLICANTDIV   = '{$model->applicantdiv}' ";
                $where .= "   AND TESTDIV        = '{$model->testdiv}' ";
                $where .= "   AND EXAMNO         = '{$examno}' ";

                $query = Query::updateSQL($data, "ENTEXAM_INTERVIEW_DAT", $where);
            } else {
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["TESTDIV"][TEXT]          = $model->testdiv;
                $data["EXAMNO"][TEXT]           = $examno;
                $query = Query::insertSQL($data, "ENTEXAM_INTERVIEW_DAT");
            }
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
