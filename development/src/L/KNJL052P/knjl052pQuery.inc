<?php

require_once('for_php7.php');

class knjl052pQuery extends Query {
    //名称マスタより取得
    function getNameMst($namecd1, $year) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd1 == "L024") {
            $query .= "     AND NAMECD2 <> '1' "; //中学・県外前期は、面接ないので表示しない
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    function SelectQuery($model, $flg) {
        //県内高校推薦か？
        $suisenFlg = ($model->applicantdiv == "2" && $model->testdiv == "1") ? "1" : "";
        $perfect = ($suisenFlg == "1") ? 30 : 50;

        $query  = " WITH MAIN_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.RECEPTNO, ";
        $query .= "         T1.EXAMNO, ";
        $query .= "         T1.JUDGEDIV, ";
        $query .= "         S1.NAME, ";
        $query .= "         S1.NAME_KANA, ";
        $query .= "         L1.ATTEND_FLG, ";
        $query .= "         L1.SCORE, ";
        $query .= "         L1.SCORE1, ";//高校推薦用
        $query .= "         L1.SCORE2, ";//高校推薦用
        $query .= "         {$perfect} AS PERFECT ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECEPT_DAT T1 ";
        $query .= "         INNER JOIN ENTEXAM_APPLICANTBASE_DAT S1 ";
        $query .= "              ON S1.ENTEXAMYEAR      = T1.ENTEXAMYEAR ";
        $query .= "             AND S1.APPLICANTDIV     = T1.APPLICANTDIV ";
        $query .= "             AND S1.EXAMNO           = T1.EXAMNO ";
        $query .= "         LEFT JOIN ENTEXAM_INTERVIEW_DAT L1 ";
        $query .= "              ON L1.ENTEXAMYEAR      = T1.ENTEXAMYEAR ";
        $query .= "             AND L1.APPLICANTDIV     = T1.APPLICANTDIV ";
        $query .= "             AND L1.TESTDIV          = T1.TESTDIV ";
        $query .= "             AND L1.EXAMNO           = T1.EXAMNO ";
        $query .= "     WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->ObjYear}' AND ";
        $query .= "         T1.APPLICANTDIV = '{$model->applicantdiv}' AND ";
        $query .= "         T1.TESTDIV      = '{$model->testdiv}' ";
//        $query .= "         AND VALUE(T1.JUDGEDIV,'') <> '4' ";
        if ($flg == "") {
            if ($model->cmd == "back") {
                $query .= "   AND T1.RECEPTNO < '".$model->s_receptno."' ";
                $query .= " ORDER BY T1.RECEPTNO DESC ";
            } else if ($model->cmd == "next") {
                $query .= "   AND T1.RECEPTNO > '".$model->e_receptno."' ";
                $query .= " ORDER BY T1.RECEPTNO ";
            }
        } else if ($flg == "BAC_COUNT" || $flg == "NEXT_COUNT") {
            if ($flg == "BAC_COUNT") {
                $query .= "   AND T1.RECEPTNO < '".$model->s_receptno."' ";
                $query .= " ORDER BY T1.RECEPTNO DESC ";
            } else if ($flg == "NEXT_COUNT") {
                $query .= "   AND T1.RECEPTNO > '".$model->e_receptno."' ";
                $query .= " ORDER BY T1.RECEPTNO ";
            }
        } else if ($flg == "CHECK") {
                $query .= "   AND T1.RECEPTNO >= '".$model->s_receptno."' ";
        }
        $query .= "     FETCH FIRST 500 ROWS ONLY ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        if ($flg != "BAC_COUNT" && $flg != "NEXT_COUNT" ) {
            $query .= " ORDER BY RECEPTNO";
        }

        return $query;
    }

    //更新(一度すべて削除してから追加）
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //県内高校推薦か？
        $suisenFlg = ($model->applicantdiv == "2" && $model->testdiv == "1") ? "1" : "";

        //削除
        $query  = " DELETE FROM ENTEXAM_INTERVIEW_DAT ";
        $query .= " WHERE ENTEXAMYEAR    = '".$model->ObjYear."' ";
        $query .= "   AND APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "   AND TESTDIV        = '".$model->testdiv."' ";
        $query .= "   AND EXAMNO IN ('".implode("','",array_keys($model->receptno))."')";
        $db->query($query);

        //追加
        foreach ($model->receptno as $receptno => $receptno) {
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["TESTDIV"][TEXT]          = $model->testdiv;
            $data["EXAMNO"][TEXT]           = $receptno;
            if ($suisenFlg == "1") {
                $score1 = $model->score1[$receptno];
                $score2 = $model->score2[$receptno];
                if ($score1 === '*' || $score2 === '*') {
                    $data["ATTEND_FLG"][TEXT]   = "0";
                    $data["SCORE1"][NUMBER]     = "";
                    $data["SCORE2"][NUMBER]     = "";
                    $data["SCORE"][NUMBER]      = "";
                } else {
                    $data["ATTEND_FLG"][TEXT]   = !strlen($score1) && !strlen($score2) ? "" : "1";
                    $data["SCORE1"][NUMBER]     = !strlen($score1) ? "" : $score1;
                    $data["SCORE2"][NUMBER]     = !strlen($score2) ? "" : $score2;
                    if (strlen($score1) && strlen($score2)) {
                        $data["SCORE"][NUMBER]      = $score1 + $score2;
                    } else if (strlen($score1)) {
                        $data["SCORE"][NUMBER]      = $score1;
                    } else if (strlen($score2)) {
                        $data["SCORE"][NUMBER]      = $score2;
                    } else {
                        $data["SCORE"][NUMBER]      = "";
                    }
                }
            } else {
                $score  = $model->score[$receptno];
                if ($score === '*') {
                    $data["ATTEND_FLG"][TEXT]   = "0";
                    $data["SCORE"][NUMBER]      = "";
                } else {
                    $data["ATTEND_FLG"][TEXT]   = !strlen($score) ? "" : "1";
                    $data["SCORE"][NUMBER]      = !strlen($score) ? "" : $score;
                }
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $query = Query::insertSQL($data, "ENTEXAM_INTERVIEW_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>