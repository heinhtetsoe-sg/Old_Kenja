<?php
class knjl085iQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1, $namecd2="", $nameSpare3="") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
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

    //入試区分取得
    function getTestdivMst($model) {
        $query  = " SELECT ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
        $query .= "     TESTDIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //面接班取得
    function getHallYdat($model) {
        $query  = " SELECT ";
        $query .= "     EXAMHALLCD || ':' || EXAMHALL_NAME AS LABEL, ";
        $query .= "     EXAMHALLCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV = '{$model->testdiv}' ";
        $query .= "     AND EXAM_TYPE = '{$model->exam_type}' ";
        $query .= "     AND EXAMHALLCD LIKE '2%' "; // 面接班：先頭1桁が'2'のデータ
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //面接評価取得
    function getInterview($year, $applicantdiv, $settingCd, $seq = "") {
        $query  = " SELECT ";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     SEQ AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR      = '{$year}' ";
        $query .= "     AND APPLICANTDIV = '{$applicantdiv}' ";
        $query .= "     AND SETTING_CD   = '{$settingCd}' ";
        if ($seq) {
            $query .= " AND SEQ = '{$seq}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     RECEPT.RECEPTNO AS EXAMNO, "; // 「RECEPTNO」「EXAMNO」・・・同じ受験番号が入っている
        $query .= "     INTERVIEW.INTERVIEW_A, ";
        $query .= "     INTERVIEW.ATTEND_FLG, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RECEPT "; // テーブル・・・「4:欠席」「5:未受験」が除かれている
        $query .= "     INNER JOIN ENTEXAM_HALL_YDAT HALL ";
        $query .= "          ON HALL.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ";
        $query .= "         AND HALL.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "         AND HALL.TESTDIV = RECEPT.TESTDIV ";
        $query .= "         AND HALL.EXAM_TYPE = RECEPT.EXAM_TYPE ";
        $query .= "         AND HALL.EXAMHALLCD LIKE '2%' "; // 面接班：先頭1桁が'2'のデータ
        $query .= "         AND RECEPT.RECEPTNO BETWEEN HALL.S_RECEPTNO AND HALL.E_RECEPTNO ";
        if ($model->examhallcd != "ALL") {
            $query .= "         AND HALL.EXAMHALLCD = '{$model->examhallcd}' ";
        }
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "          ON BASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ";
        $query .= "         AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "         AND BASE.TESTDIV = RECEPT.TESTDIV ";
        $query .= "         AND BASE.EXAMNO = RECEPT.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_INTERVIEW_DAT INTERVIEW ";
        $query .= "          ON INTERVIEW.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ";
        $query .= "         AND INTERVIEW.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "         AND INTERVIEW.TESTDIV = RECEPT.TESTDIV ";
        $query .= "         AND INTERVIEW.EXAMNO = RECEPT.EXAMNO ";
        $query .= " WHERE ";
        $query .= "         RECEPT.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND RECEPT.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND RECEPT.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND RECEPT.EXAM_TYPE    = '{$model->exam_type}' ";
        $query .= " ORDER BY ";
        $query .= "     RECEPT.RECEPTNO ";

        return $query;
    }

    //更新(一度すべて削除してから追加）
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->interview as $examno => $value) {
            //存在チェック
            $query  = " SELECT ";
            $query .= "     COUNT(*) AS CNT ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_INTERVIEW_DAT ";
            $query .= " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $query .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $query .= "   AND TESTDIV       = '{$model->testdiv}' ";
            $query .= "   AND EXAMNO        = '{$examno}' ";
            $dataCnt = $db->getOne($query);
            //データセット
            $data = array();
            $data["INTERVIEW_A"][TEXT]      = !strlen($value["INTERVIEW_A"]) ? "" : $value["INTERVIEW_A"];
            $data["ATTEND_FLG"][TEXT]       = !strlen($value["ATTEND_FLG"]) ? "0" : $value["ATTEND_FLG"]; // 「0:面接出席」「1:面接欠席」
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            if ($dataCnt > 0) {
                $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
                $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
                $where .= "   AND TESTDIV       = '{$model->testdiv}' ";
                $where .= "   AND EXAMNO        = '{$examno}' ";
                //更新
                $query = Query::updateSQL($data, "ENTEXAM_INTERVIEW_DAT", $where);
            } else {
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["TESTDIV"][TEXT]          = $model->testdiv;
                $data["EXAMNO"][TEXT]           = $examno;
                //追加
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
