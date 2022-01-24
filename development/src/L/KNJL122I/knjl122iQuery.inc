<?php
class knjl122iQuery extends Query {
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
    function getTestdivMst($model, $testdiv="") {
        $query  = " SELECT ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        if ($testdiv) {
            $query .= "     AND TESTDIV = '{$testdiv}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //面接班取得
    function getHallYdat($model, $examhallcd="") {
        $query  = " SELECT ";
        $query .= "     EXAMHALLCD || ':' || EXAMHALL_NAME AS LABEL, ";
        $query .= "     EXAMHALLCD AS VALUE, ";
        $query .= "     EXAMHALL_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV = '{$model->testdiv}' ";
        $query .= "     AND EXAM_TYPE = '{$model->exam_type}' ";
        $query .= "     AND EXAMHALLCD LIKE '2%' "; // 面接班：先頭1桁が'2'のデータ
        if ($examhallcd) {
            $query .= "     AND EXAMHALLCD = '{$examhallcd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //面接評価取得
    function getInterview($model, $namecd1, $namecd2="")
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     SEQ AS VALUE, ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND SETTING_CD = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND SEQ = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    function selectQuery($model, $cmd="") {
        $query  = " SELECT ";
        $query .= "     RECEPT.RECEPTNO AS EXAMNO, "; // 「RECEPTNO」「EXAMNO」・・・同じ受験番号が入っている
        $query .= "     INTERVIEW.INTERVIEW_A, ";
        $query .= "     INTERVIEW.INTERVIEW_B, ";
        $query .= "     INTERVIEW.INTERVIEW_C, ";
        $query .= "     INTERVIEW.ATTEND_FLG, ";
        $query .= "     INTERVIEW.SCORE1 - INTERVIEW.SCORE2 AS PLUS_MINUS_POINT, ";
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
        if ($cmd) {
            $query .= "     AND VALUE(INTERVIEW.ATTEND_FLG, '0') != '1' "; // 一括更新画面は、面接欠席者を表示しない
        }
        $query .= " ORDER BY ";
        $query .= "     RECEPT.RECEPTNO ";

        return $query;
    }

    //更新
    function getUpdateQuery($model) {
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
            if ($value["ATTEND_FLG"] == "1") { // 「1:面接欠席」
                $data["INTERVIEW_A"][TEXT]      = "";
                $data["INTERVIEW_B"][TEXT]      = "";
                if ($model->testdiv == "2") { //B方式のみ
                    $data["INTERVIEW_C"][TEXT]      = "";
                }
                $data["ATTEND_FLG"][TEXT]       = $value["ATTEND_FLG"];
            } else { // 「0:面接出席」
                $data["INTERVIEW_A"][TEXT]      = $value["INTERVIEW_A"];
                $data["INTERVIEW_B"][TEXT]      = $value["INTERVIEW_B"];
                if ($model->testdiv == "2") { //B方式のみ
                    $data["INTERVIEW_C"][TEXT]      = $value["INTERVIEW_C"];
                }
                $data["ATTEND_FLG"][TEXT]       = "0";
            }
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

            //換算ポイントの更新
            if ($model->testdiv == "2") { //B方式のみ
                knjl122iQuery::updPointQuery($db, $model, $examno);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //一括更新
    function getReplaceUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $examnoArray = explode(",", $model->replace_data["selectdata"]); // 一括更新画面は、面接欠席者を表示しない。よって、面接出席のみ

        foreach ($examnoArray as $key => $examno) {
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
            if ($model->replace_data["check"]["INTERVIEW_A"] == "1") {
                $data["INTERVIEW_A"][TEXT]      = $model->replace_data["field"]["INTERVIEW_A"];
            }
            if ($model->replace_data["check"]["INTERVIEW_B"] == "1") {
                $data["INTERVIEW_B"][TEXT]      = $model->replace_data["field"]["INTERVIEW_B"];
            }
            if ($model->testdiv == "2") { //B方式のみ
                if ($model->replace_data["check"]["INTERVIEW_C"] == "1") {
                    $data["INTERVIEW_C"][TEXT]      = $model->replace_data["field"]["INTERVIEW_C"];
                }
            }
            $data["ATTEND_FLG"][TEXT]       = "0"; // 面接出席のみ
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

            //換算ポイントの更新
            if ($model->testdiv == "2") { //B方式のみ
                knjl122iQuery::updPointQuery($db, $model, $examno);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //換算ポイントの更新
    function updPointQuery($db, $model, $examno) {
        //換算ポイントを取得
        $query  = " SELECT ";
        $query .= "     INTERVIEW.EXAMNO, ";
        $query .= "     POINT.PLUS_POINT, ";
        $query .= "     POINT.MINUS_POINT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_INTERVIEW_DAT INTERVIEW ";
        $query .= "     LEFT JOIN ENTEXAM_SETTING_MST LA ON LA.ENTEXAMYEAR = INTERVIEW.ENTEXAMYEAR AND LA.APPLICANTDIV = INTERVIEW.APPLICANTDIV AND LA.SETTING_CD = 'LH27' AND LA.SEQ = INTERVIEW.INTERVIEW_A ";
        $query .= "     LEFT JOIN ENTEXAM_SETTING_MST LB ON LB.ENTEXAMYEAR = INTERVIEW.ENTEXAMYEAR AND LB.APPLICANTDIV = INTERVIEW.APPLICANTDIV AND LB.SETTING_CD = 'LH27' AND LB.SEQ = INTERVIEW.INTERVIEW_B ";
        $query .= "     LEFT JOIN ENTEXAM_SETTING_MST LC ON LC.ENTEXAMYEAR = INTERVIEW.ENTEXAMYEAR AND LC.APPLICANTDIV = INTERVIEW.APPLICANTDIV AND LC.SETTING_CD = 'LH27' AND LC.SEQ = INTERVIEW.INTERVIEW_C ";
        $query .= "     LEFT JOIN ENTEXAM_POINT_MST POINT ";
        $query .= "          ON POINT.ENTEXAMYEAR = INTERVIEW.ENTEXAMYEAR ";
        $query .= "         AND POINT.APPLICANTDIV = INTERVIEW.APPLICANTDIV ";
        $query .= "         AND POINT.TESTDIV = INTERVIEW.TESTDIV ";
        $query .= "         AND POINT.POINTCD = '5' "; // 評価区分「5:面接評価」
        $query .= "         AND LA.NAMESPARE1 + LB.NAMESPARE1 + LC.NAMESPARE1 BETWEEN POINT.POINTLOW AND POINT.POINTHIGH ";
        $query .= " WHERE ";
        $query .= "     INTERVIEW.ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND INTERVIEW.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND INTERVIEW.TESTDIV = '{$model->testdiv}' ";
        $query .= "     AND INTERVIEW.EXAMNO = '{$examno}' ";
        $pointRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        //データセット
        $data = array();
        $data["SCORE1"][NUMBER]         = $pointRow["PLUS_POINT"];
        $data["SCORE2"][NUMBER]         = $pointRow["MINUS_POINT"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";
        $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
        $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $where .= "   AND TESTDIV       = '{$model->testdiv}' ";
        $where .= "   AND EXAMNO        = '{$examno}' ";
        //更新
        $query = Query::updateSQL($data, "ENTEXAM_INTERVIEW_DAT", $where);
        $db->query($query);
    }
}
?>
