<?php

require_once('for_php7.php');

class knjl051eQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1, $namecd2="") {
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
        $query .= "     AND EXAM_TYPE       = '1' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     HALLGD.EXAMHALLGROUPCD as EXAMHALLGROUP_NAME, ";
        $query .= "     HALLGD.EXAMHALLGROUP_ORDER, ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     FSCHOOL.FINSCHOOL_NAME, ";
        $query .= "     INTERVIEW.INTERVIEW_A, ";
        $query .= "     INTERVIEW.INTERVIEW_B ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     INNER JOIN ENTEXAM_HALL_GROUP_DAT HALLGD ";
        $query .= "          ON BASE.ENTEXAMYEAR      = HALLGD.ENTEXAMYEAR ";
        $query .= "         AND BASE.APPLICANTDIV     = HALLGD.APPLICANTDIV ";
        $query .= "         AND BASE.TESTDIV          = HALLGD.TESTDIV ";
        $query .= "         AND HALLGD.EXAMHALL_TYPE  = '1' ";
        $query .= "         AND HALLGD.EXAMHALLCD     = '{$model->examhallcd}' ";
        $query .= "         AND BASE.EXAMNO           = HALLGD.EXAMNO ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FSCHOOL ";
        $query .= "          ON BASE.FS_CD = FSCHOOL.FINSCHOOLCD ";
        $query .= "     LEFT JOIN ENTEXAM_INTERVIEW_DAT INTERVIEW ";
        $query .= "          ON BASE.ENTEXAMYEAR      = INTERVIEW.ENTEXAMYEAR ";
        $query .= "         AND BASE.APPLICANTDIV     = INTERVIEW.APPLICANTDIV ";
        $query .= "         AND BASE.TESTDIV          = INTERVIEW.TESTDIV ";
        $query .= "         AND BASE.EXAMNO           = INTERVIEW.EXAMNO ";
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
            $data["INTERVIEW_A"][TEXT]      = !strlen($value["A"]) ? "" : $value["A"];
            $data["INTERVIEW_B"][TEXT]      = !strlen($value["B"]) ? "" : $value["B"];
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