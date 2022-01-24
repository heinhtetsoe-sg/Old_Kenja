<?php

require_once('for_php7.php');

class knjl050aQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1, $namecd2="") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタよりNAME2を取得
    function getName2ForMst($year, $namecd1, $namecd2="") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAME2 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験種別コンボ
    function getTestdivMst($model) {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
        $query .= "     DEFAULT_FLG AS NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //会場取得
    function getHallData($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     EXAMHALLCD || ':' || EXAMHALL_NAME AS LABEL, ";
        $query .= "     EXAMHALLCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
        $query .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
        $query .= "     TESTDIV         = '".$model->testdiv."' AND ";
        $query .= "     EXAM_TYPE       = '".$model->exam_type."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験科目取得
    function getTestSubclasscd($model, $testsubclasscd="") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->ObjYear."' AND ";
        $query .= "     NAMECD1     = 'L009' AND ";
        $query .= "     NAME1 IS NOT NULL ";
        if ($testsubclasscd) {
            $query .= " AND NAMECD2     = '".$testsubclasscd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    function SelectQuery($model, $div) {
        $query  = " WITH MAIN_T AS ( ";
        $query .= "     SELECT ";
        if ($div == "csv") {
            $query .= "         B1.ENTEXAMYEAR, ";
            $query .= "         B1.TESTDIV, ";
            if ($model->testsubclasscd != "6") { //面接以外
                $query .= "         S1.TESTSUBCLASSCD, ";
            } else {
                $query .= "         '".$model->testsubclasscd."' AS TESTSUBCLASSCD, ";
            }
            $query .= "         'DUMMY' AS DUMMY, ";
        }
        $query .= "         RCPT.RECEPTNO AS RECEPTNO, ";
        $query .= "         B1.NAME AS NAME, ";
        $query .= "         B1.NAME_KANA AS KANA, ";
        $query .= "         L1.NAME1 AS SEX,";
        $query .= "         S1.SCORE ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECEPT_DAT RCPT ";
        $query .= "         LEFT JOIN ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "              ON B1.ENTEXAMYEAR   = RCPT.ENTEXAMYEAR ";
        $query .= "             AND B1.APPLICANTDIV  = RCPT.APPLICANTDIV ";
        $query .= "             AND B1.EXAMNO        = RCPT.EXAMNO ";
        $query .= "         LEFT JOIN NAME_MST L1 ";
        $query .= "              ON L1.NAMECD1 = 'Z002' ";
        $query .= "             AND L1.NAMECD2 = B1.SEX ";
        if ($model->testsubclasscd != "6") { //面接以外
            $query .= "         LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT R006 ";
            $query .= "              ON R006.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ";
            $query .= "             AND R006.APPLICANTDIV   = RCPT.APPLICANTDIV ";
            $query .= "             AND R006.TESTDIV        = RCPT.TESTDIV ";
            $query .= "             AND R006.EXAM_TYPE      = RCPT.EXAM_TYPE ";
            $query .= "             AND R006.RECEPTNO       = RCPT.RECEPTNO ";
            $query .= "             AND R006.SEQ            = '006' ";
            $query .= "         LEFT JOIN ENTEXAM_EXAMTYPE_SUBCLASS_MST EMSB ";
            $query .= "              ON EMSB.ENTEXAMYEAR    = R006.ENTEXAMYEAR ";
            $query .= "             AND EMSB.APPLICANTDIV   = R006.APPLICANTDIV ";
            $query .= "             AND EMSB.EXAM_TYPE      = R006.REMARK3 ";
            $query .= "         LEFT JOIN ENTEXAM_SCORE_DAT S1 ";
            $query .= "              ON S1.ENTEXAMYEAR      = RCPT.ENTEXAMYEAR ";
            $query .= "             AND S1.APPLICANTDIV     = RCPT.APPLICANTDIV ";
            $query .= "             AND S1.TESTDIV          = RCPT.TESTDIV ";
            $query .= "             AND S1.RECEPTNO         = RCPT.RECEPTNO ";
            $query .= "             AND S1.EXAM_TYPE        = '{$model->exam_type}' ";
            $query .= "             AND S1.TESTSUBCLASSCD   = '{$model->testsubclasscd}' ";
        } else {
            $query .= "         LEFT JOIN ENTEXAM_INTERVIEW_DAT S1 ";
            $query .= "              ON S1.ENTEXAMYEAR      = RCPT.ENTEXAMYEAR ";
            $query .= "             AND S1.APPLICANTDIV     = RCPT.APPLICANTDIV ";
            $query .= "             AND S1.TESTDIV          = RCPT.TESTDIV ";
            $query .= "             AND S1.EXAMNO           = RCPT.EXAMNO ";
        }
        $query .= "     WHERE ";
        $query .= "             RCPT.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "         AND RCPT.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "         AND RCPT.TESTDIV      = '{$model->testdiv}' ";
        if ($model->testsubclasscd != "6") { //面接以外
            $query .= "                         AND EMSB.TESTSUBCLASSCD = '{$model->testsubclasscd}' ";
        }
        if ($div == "list") {
            if ($model->cmd == "next") {
                $query .= "         AND RCPT.RECEPTNO > '{$model->e_receptno}' ";
            } else if ($model->cmd == "back") {
                $query .= "         AND RCPT.RECEPTNO < '{$model->s_receptno}' ";
            } else {
                if (strlen($model->s_receptno)) {
                    $query .= "         AND RCPT.RECEPTNO >= '{$model->s_receptno}' ";
                }
            }
            if (!in_array($model->cmd, array('back', 'next'))) {
                if (strlen($model->e_receptno)> 0) {
                    $query .= "         AND RCPT.RECEPTNO <= '{$model->e_receptno}' ";
                }
            }

            $sort = ($model->cmd == "back") ? "DESC" : "";
            $query .= "     ORDER BY ";
            $query .= "         RCPT.RECEPTNO ".$sort;
            $query .= "     FETCH FIRST 100 ROWS ONLY ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " ORDER BY ";
        $query .= "     RECEPTNO ";

        return $query;
    }

    //更新(一度すべて削除してから追加）
    function getUpdateScoreQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->score as $receptno => $score) {
            if ($model->updFlg[$receptno] !== "1") continue;

            //削除
            $query  = " DELETE FROM ";
            $query .= "     ENTEXAM_SCORE_DAT ";
            $query .= " WHERE ENTEXAMYEAR    = '".$model->ObjYear."' ";
            $query .= "   AND APPLICANTDIV   = '".$model->applicantdiv."' ";
            $query .= "   AND TESTDIV        = '".$model->testdiv."' ";
            $query .= "   AND EXAM_TYPE      = '".$model->exam_type."' ";
            $query .= "   AND RECEPTNO       = '".$receptno."' ";
            $query .= "   AND TESTSUBCLASSCD = '".$model->testsubclasscd."' ";
            $db->query($query);

            //追加
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["TESTDIV"][TEXT]          = $model->testdiv;
            $data["EXAM_TYPE"][TEXT]        = $model->exam_type;
            $data["RECEPTNO"][TEXT]         = $receptno;
            $data["TESTSUBCLASSCD"][TEXT]   = $model->testsubclasscd;
            $data["ATTEND_FLG"][TEXT]       = !strlen($score) ? "" : "1";
            $data["SCORE"][NUMBER]          = !strlen($score) ? "" : $score;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $query = Query::insertSQL($data, "ENTEXAM_SCORE_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //面接データ登録チェック
    function getInterviewDatQuery($model) {
        $query .= "     SELECT ";
        $query .= "         RCPT.RECEPTNO AS RECEPTNO, ";
        $query .= "         RCPT.EXAMNO AS RCPT_EXAMNO, ";
        $query .= "         ITRV.EXAMNO AS ITRV_EXAMNO ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECEPT_DAT RCPT ";
        $query .= "         LEFT JOIN ENTEXAM_INTERVIEW_DAT ITRV ";
        $query .= "            ON ITRV.ENTEXAMYEAR = RCPT.ENTEXAMYEAR ";
        $query .= "           AND ITRV.APPLICANTDIV = RCPT.APPLICANTDIV ";
        $query .= "           AND ITRV.TESTDIV = RCPT.TESTDIV ";
        $query .= "           AND ITRV.EXAMNO = RCPT.EXAMNO ";
        $query .= "     WHERE ";
        $query .= "             RCPT.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "         AND RCPT.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "         AND RCPT.TESTDIV      = '{$model->testdiv}' ";
        $query .= "         AND RCPT.EXAM_TYPE    = '{$model->exam_type}'";
        $query .= "         AND RCPT.RECEPTNO     IN ('".implode("','",array_keys($model->score))."')";
        return $query;
    }

    //更新(一度すべて削除してから追加）
    function getUpdateInterviewQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_INTERVIEW_DAT ";
        $query .= " WHERE ENTEXAMYEAR    = '".$model->ObjYear."' ";
        $query .= "   AND APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "   AND TESTDIV        = '".$model->testdiv."' ";
        $query .= "   AND EXAMNO IN ('".implode("','",array_keys($model->examscore))."')";
        $db->query($query);

        //追加
        foreach ($model->examscore as $examno => $score) {
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["TESTDIV"][TEXT]          = $model->testdiv;
            $data["EXAMNO"][TEXT]           = $examno;
            $data["SCORE"][NUMBER]          = !strlen($score) ? "" : $score;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $query = Query::insertSQL($data, "ENTEXAM_INTERVIEW_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>