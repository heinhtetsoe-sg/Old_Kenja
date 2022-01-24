<?php

require_once('for_php7.php');

class knjl017dQuery extends Query
{

    //名称マスタより取得
    public function getNameMst($namecd1, $year, $flg = true, $paramcd = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        if ($flg) {
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
            $query .= "     NAMESPARE2 ";
        } else {
            $query .= "     NAME1 AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($paramcd !== "") {
            $query .= "     AND NAMECD2 = '".$paramcd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //クラブリスト取得
    public function getCmbClubList($model)
    {
        $query .= " SELECT ";
        $query .= "     CLUB_M.CLUBCD AS VALUE, ";
        $query .= "     CLUB_M.CLUBCD || ':' || CLUB_M.CLUBNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CLUB_MST CLUB_M ";
        $query .= " WHERE ";
        $query .= "     CLUB_M.SCHOOLCD = '{$model->schoolcd}' ";
        $query .= " AND CLUB_M.SCHOOL_KIND = '{$model->schoolkind}' ";
        $query .= " GROUP BY ";
        $query .= "     CLUB_M.CLUBCD, CLUB_M.CLUBNAME ";
        $query .= " ORDER BY ";
        $query .= "     CLUB_M.CLUBCD ";

        return $query;
    }

    //一覧表示
    public function SelectQuery($model, $flg)
    {
        $query .= " SELECT ";
        if ($flg == "COUNT") {
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query .= "     T1.EXAMNO AS EXAMNO, ";
            $query .= "     T1.NAME AS NAME, ";
            $query .= "     NAME_SHDIV.NAME1 AS SHDIV, ";
            $query .= "     T1.DESIREDIV AS DESIREDIV, ";
            $query .= "     CASE WHEN T3_1.REMARK10 IS NOT NULL OR T3_2.REMARK10 IS NOT NULL OR T3.TOTAL5 IS NOT NULL THEN ";
            $query .= "         VALUE(INT(T3_1.REMARK10), 0) + VALUE(INT(T3_2.REMARK10), 0) + VALUE(T3.TOTAL5,0) END AS SUBTOTAL5, ";
            $query .= "     CASE WHEN T3_1.REMARK11 IS NOT NULL OR T3_2.REMARK11 IS NOT NULL OR T3.TOTAL_ALL IS NOT NULL THEN ";
            $query .= "         VALUE(INT(T3_1.REMARK11), 0) + VALUE(INT(T3_2.REMARK11), 0) + VALUE(T3.TOTAL_ALL,0) END AS SUBTOTAL_ALL, ";
            $query .= "     T3.ABSENCE_DAYS AS ABSENCE_DAYS, ";
            $query .= "     T3.ABSENCE_DAYS2 AS ABSENCE_DAYS2, ";
            $query .= "     T3.ABSENCE_DAYS3 AS ABSENCE_DAYS3, ";
            $query .= "     D1_J_LANG.SCORE AS SCORE1, ";
            $query .= "     D1_MATH.SCORE AS SCORE2, ";
            $query .= "     D1_E_LANG.SCORE AS SCORE3, ";
            $query .= "     (D1_J_LANG.SCORE + D1_MATH.SCORE + D1_E_LANG.SCORE) AS SCORE_TOTAL, ";
            $query .= "     T1.JUDGE_KIND AS JUDGE_KIND, ";
            $query .= "     T2.REMARK3 CLUBCD, ";
            $query .= "     CLUB_M.CLUBNAME AS CLUBNAME, ";
            $query .= "     T1.SUB_ORDER AS SUB_ORDER ";
        }
        $query .= "  FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T2 ";
        $query .= "           ON T1.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "           AND T1.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "           AND T1.EXAMNO = T2.EXAMNO ";
        $query .= "           AND T2.SEQ = '019' ";
        $query .= "     LEFT JOIN CLUB_MST CLUB_M ";
        $query .= "           ON T2.REMARK1 = CLUB_M.SCHOOLCD ";
        $query .= "           AND T2.REMARK2 = CLUB_M.SCHOOL_KIND ";
        $query .= "           AND T2.REMARK3 = CLUB_M.CLUBCD ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT T3 ";
        $query .= "           ON T1.ENTEXAMYEAR = T3.ENTEXAMYEAR ";
        $query .= "           AND T1.APPLICANTDIV = T3.APPLICANTDIV ";
        $query .= "           AND T1.EXAMNO = T3.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT T3_1 ";
        $query .= "           ON T1.ENTEXAMYEAR = T3_1.ENTEXAMYEAR ";
        $query .= "           AND T1.APPLICANTDIV = T3_1.APPLICANTDIV ";
        $query .= "           AND T1.EXAMNO = T3_1.EXAMNO ";
        $query .= "           AND T3_1.SEQ = '001' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT T3_2 ";
        $query .= "           ON T1.ENTEXAMYEAR = T3_2.ENTEXAMYEAR ";
        $query .= "           AND T1.APPLICANTDIV = T3_2.APPLICANTDIV ";
        $query .= "           AND T1.EXAMNO = T3_2.EXAMNO ";
        $query .= "           AND T3_2.SEQ = '002' ";
        $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT D1_J_LANG ";
        $query .= "           ON T1.ENTEXAMYEAR = D1_J_LANG.ENTEXAMYEAR ";
        $query .= "           AND T1.APPLICANTDIV = D1_J_LANG.APPLICANTDIV ";
        $query .= "           AND T1.TESTDIV = D1_J_LANG.TESTDIV ";
        $query .= "           AND T1.EXAMNO = D1_J_LANG.RECEPTNO ";
        $query .= "           AND D1_J_LANG.TESTSUBCLASSCD = '1' ";
        $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT D1_MATH ";
        $query .= "           ON T1.ENTEXAMYEAR = D1_MATH.ENTEXAMYEAR ";
        $query .= "           AND T1.APPLICANTDIV = D1_MATH.APPLICANTDIV ";
        $query .= "           AND T1.TESTDIV = D1_MATH.TESTDIV ";
        $query .= "           AND T1.EXAMNO = D1_MATH.RECEPTNO ";
        $query .= "           AND D1_MATH.TESTSUBCLASSCD = '2' ";
        $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT D1_E_LANG ";
        $query .= "           ON T1.ENTEXAMYEAR = D1_E_LANG.ENTEXAMYEAR ";
        $query .= "           AND T1.APPLICANTDIV = D1_E_LANG.APPLICANTDIV ";
        $query .= "           AND T1.TESTDIV = D1_E_LANG.TESTDIV ";
        $query .= "           AND T1.EXAMNO = D1_E_LANG.RECEPTNO ";
        $query .= "           AND D1_E_LANG.TESTSUBCLASSCD = '3' ";
        $query .= "     LEFT JOIN NAME_MST NAME_SHDIV ";
        $query .= "           ON NAME_SHDIV.NAMECD1 = 'L006' ";
        $query .= "           AND T1.SHDIV = NAME_SHDIV.NAMECD2 ";
        $query .= " WHERE T1.ENTEXAMYEAR    = '".$model->ObjYear."'";
        $query .= "   AND T1.APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "   AND T1.TESTDIV        = '".$model->testdiv."' ";
        if ($flg != "COUNT") {
            switch ($flg) {
                case "J_LANG_ASC":
                    $query .= " ORDER BY CASE WHEN SCORE1 IS NULL THEN 0 ELSE 1 END DESC, SCORE1 ASC";
                    break;
                case "J_LANG_DESC":
                    $query .= " ORDER BY CASE WHEN SCORE1 IS NULL THEN 0 ELSE 1 END DESC, SCORE1 DESC";
                    break;
                case "MATH_ASC":
                    $query .= " ORDER BY CASE WHEN SCORE2 IS NULL THEN 0 ELSE 1 END DESC, SCORE2 ASC";
                    break;
                case "MATH_DESC":
                    $query .= " ORDER BY CASE WHEN SCORE2 IS NULL THEN 0 ELSE 1 END DESC, SCORE2 DESC";
                    break;
                case "E_LANG_ASC":
                    $query .= " ORDER BY CASE WHEN SCORE3 IS NULL THEN 0 ELSE 1 END DESC, SCORE3 ASC";
                    break;
                case "E_LANG_DESC":
                    $query .= " ORDER BY CASE WHEN SCORE3 IS NULL THEN 0 ELSE 1 END DESC, SCORE3 DESC";
                    break;
                case "TOTAL_ASC":
                    $query .= " ORDER BY CASE WHEN SCORE_TOTAL IS NULL THEN 0 ELSE 1 END DESC, SCORE_TOTAL ASC";
                    break;
                case "TOTAL_DESC":
                    $query .= " ORDER BY CASE WHEN SCORE_TOTAL IS NULL THEN 0 ELSE 1 END DESC, SCORE_TOTAL DESC";
                    break;
                default:
                    $query .= " ORDER BY SUBSTR(CHAR(DECIMAL(EXAMNO, 10, 0)),1,10) ";
                    break;
            }
        }
        return $query;
    }

    public function getJudgeAllCntQuery($model, $jdgtype)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= "  FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " WHERE T1.ENTEXAMYEAR    = '".$model->ObjYear."'";
        $query .= "   AND T1.APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "   AND T1.TESTDIV        = '".$model->testdiv."' ";
        if ($jdgtype !== "") {
            $query .= "   AND T1.SUB_ORDER      = '".$jdgtype."' ";
        }
        return $query;
    }

    public function getNameMstCdQuery($namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    public function getDecisionMstCdQuery()
    {
        $query  = " SELECT ";
        $query .= "     DECISION_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_INTERNAL_DECISION_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //登録チェック(BASE_DETAIL)
    public function chkDataQuery_BaseDet($model, $receptno_array)
    {
        $db = Query::dbCheckOut();

        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= "  FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT T2 ";
        $query .= " WHERE T2.ENTEXAMYEAR    = '".$model->ObjYear."'";
        $query .= "   AND T2.APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "   AND T2.EXAMNO         = '".$receptno_array[0]."' ";
        $query .= "   AND T2.SEQ            = '019' ";

        $retval =$db->getOne($query);
        Query::dbCheckIn($db);
        return $retval;
    }

    //新規挿入(BASE_DETAIL)
    public function insertQuery_BaseDet($model, $receptno_array)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]  = $model->ObjYear;
        $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
        $data["EXAMNO"][TEXT]       = $receptno_array[0];
        $data["SEQ"][TEXT]          = "019";
        $data["REMARK1"][TEXT]      = $model->schoolcd;
        $data["REMARK2"][TEXT]      = $model->schoolkind;
        $data["REMARK3"][TEXT]      = $model->clubcd[$receptno_array[1]];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    //更新(BASE_DETAIL)
    public function UpdateQuery_BaseDet(&$model, $receptno_array)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data = array();
        $data["REMARK1"][TEXT]    = $model->schoolcd;
        $data["REMARK2"][TEXT]    = $model->schoolkind;
        $data["REMARK3"][TEXT]    = $model->clubcd[$receptno_array[1]];
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC]    = "sysdate()";

        $where  = " WHERE ";
        $where .= "     ENTEXAMYEAR      = '".$model->ObjYear."' ";
        $where .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $where .= "     AND EXAMNO       = '".$receptno_array[0]."' ";
        $where .= "     AND SEQ          = '019' ";

        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    //更新(BASE)
    public function UpdateQuery_Base(&$model, $receptno_array)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data = array();
        $data["SUB_ORDER"][TEXT]    = $model->suborder[$receptno_array[1]];
        $data["JUDGE_KIND"][TEXT]    = $model->judgekind[$receptno_array[1]];

        $where  = " WHERE ";
        $where .= "     ENTEXAMYEAR      = '".$model->ObjYear."' ";
        $where .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $where .= "     AND TESTDIV = '".$model->testdiv."' ";
        $where .= "     AND EXAMNO       = '".$receptno_array[0]."' ";

        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
