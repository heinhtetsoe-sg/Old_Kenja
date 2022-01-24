<?php

require_once('for_php7.php');
class knjl056eQuery extends Query {
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
        $query .= "     AND EXAM_TYPE       = '2' ";  //固定値
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     L058.NAME1 AS DESIREDIV, ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     FSCHOOL.FINSCHOOL_NAME, ";
        $query .= "     SCORE.SCORE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     INNER JOIN ENTEXAM_HALL_GROUP_DAT HALLGD ";
        $query .= "          ON BASE.ENTEXAMYEAR      = HALLGD.ENTEXAMYEAR ";
        $query .= "         AND BASE.APPLICANTDIV     = HALLGD.APPLICANTDIV ";
        $query .= "         AND BASE.TESTDIV          = HALLGD.TESTDIV ";
        $query .= "         AND HALLGD.EXAMHALL_TYPE  = '2' ";  //固定値
        $query .= "         AND HALLGD.EXAMHALLCD     = '{$model->examhallcd}' ";
        $query .= "         AND BASE.EXAMNO           = HALLGD.EXAMNO ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FSCHOOL ";
        $query .= "          ON BASE.FS_CD = FSCHOOL.FINSCHOOLCD ";
        $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT SCORE ";
        $query .= "          ON BASE.ENTEXAMYEAR      = SCORE.ENTEXAMYEAR ";
        $query .= "         AND BASE.APPLICANTDIV     = SCORE.APPLICANTDIV ";
        $query .= "         AND BASE.TESTDIV          = SCORE.TESTDIV ";
        $query .= "         AND SCORE.EXAM_TYPE       = '{$model->exam_type}' ";
        $query .= "         AND BASE.EXAMNO           = SCORE.RECEPTNO ";
        $query .= "         AND SCORE.TESTSUBCLASSCD  = '{$model->subclasscd}' ";
        $query .= "     LEFT JOIN V_NAME_MST L058 ON L058.YEAR    = BASE.ENTEXAMYEAR ";
        $query .= "                              AND L058.NAMECD1 = 'L058' ";
        $query .= "                              AND L058.NAMECD2 = BASE.DESIREDIV ";
        $query .= " WHERE ";
        $query .= "         BASE.ENTEXAMYEAR            = '{$model->ObjYear}' ";
        $query .= "     AND BASE.APPLICANTDIV           = '{$model->applicantdiv}' ";
        $query .= "     AND BASE.TESTDIV                = '{$model->testdiv}' ";
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
        foreach ($model->score as $receptno => $score) {
            $query  = " SELECT ";
            $query .= "     COUNT(*) AS CNT ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_SCORE_DAT ";
            $query .= " WHERE ENTEXAMYEAR    = '{$model->ObjYear}' ";
            $query .= "   AND APPLICANTDIV   = '{$model->applicantdiv}' ";
            $query .= "   AND TESTDIV        = '{$model->testdiv}' ";
            $query .= "   AND EXAM_TYPE      = '{$model->exam_type}' ";
            $query .= "   AND RECEPTNO       = '{$receptno}' ";
            $query .= "   AND TESTSUBCLASSCD = '{$model->subclasscd}' ";
            $dataCnt = $db->getOne($query);

            $data = array();
            $data["SCORE"][NUMBER]      = $score;
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "SYSDATE()";

            if ($dataCnt > 0) {
                $where  = " WHERE ENTEXAMYEAR    = '{$model->ObjYear}' ";
                $where .= "   AND APPLICANTDIV   = '{$model->applicantdiv}' ";
                $where .= "   AND TESTDIV        = '{$model->testdiv}' ";
                $where .= "   AND EXAM_TYPE      = '{$model->exam_type}' ";
                $where .= "   AND RECEPTNO       = '{$receptno}' ";
                $where .= "   AND TESTSUBCLASSCD = '{$model->subclasscd}' ";

                $query = Query::updateSQL($data, "ENTEXAM_SCORE_DAT", $where);
            } else {
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["TESTDIV"][TEXT]          = $model->testdiv;
                $data["EXAM_TYPE"][TEXT]        = $model->exam_type;
                $data["RECEPTNO"][TEXT]         = $receptno;
                $data["TESTSUBCLASSCD"][TEXT]   = $model->subclasscd;
                $query = Query::insertSQL($data, "ENTEXAM_SCORE_DAT");
            }
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
