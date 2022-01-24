<?php

require_once('for_php7.php');

class knjl550jQuery extends Query {

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

    //入試方式取得
    function getExamType($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "   EXAM_TYPE || ':' || EXAMTYPE_NAME AS LABEL, ";
        $query .= "   EXAM_TYPE AS VALUE ";
        $query .= " FROM ";
        $query .= "  ENTEXAM_EXAMTYPE_MST ";
        $query .= " WHERE ";
        $query .= "  ENTEXAMYEAR = '".$model->exp_year."' ";
        $query .= "  AND APPLICANTDIV = '".$model->applicantdiv."' ";
        return $query;
    }

    //入試種別
    function getTestdiv($model) {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->exp_year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目リスト取得
    function getSubjectList($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     SUBM.SUBCLASSCD AS VALUE,";
        $query .= "     SUBM.SUBCLASSCD || ':' || L009.NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_EXAMTYPE_MST MAIN ";
        $query .= "     LEFT JOIN ENTEXAM_EXAMTYPE_SUBCLASS_MST SUBM ON MAIN.ENTEXAMYEAR  = SUBM.ENTEXAMYEAR ";
        $query .= "                                                 AND MAIN.APPLICANTDIV = SUBM.APPLICANTDIV ";
        $query .= "                                                 AND MAIN.EXAM_TYPE    = SUBM.EXAM_TYPE ";
        $query .= "     LEFT JOIN V_NAME_MST L009 ON L009.YEAR    = MAIN.ENTEXAMYEAR ";
        $query .= "                              AND L009.NAMECD2 = SUBM.SUBCLASSCD ";
        $query .= "                              AND L009.NAMECD1 = 'L009' ";
        $query .= " WHERE ";
        $query .= "         MAIN.ENTEXAMYEAR    = '".$model->exp_year."' ";
        $query .= "     AND MAIN.APPLICANTDIV   = '".$model->applicantdiv."' ";
        if ($model->examtype != "ALL") {
            $query .= "     AND MAIN.EXAM_TYPE      = '".$model->examtype."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SUBM.SUBCLASSCD ";

        return $query;
    }

    //一覧表示
    function SelectQuery($model, $div) {
        $query  = " WITH PERFECT_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         P1.* ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_PERFECT_MST P1 ";
        $query .= "         INNER JOIN ( ";
        $query .= "             SELECT ";
        $query .= "                 TESTSUBCLASSCD, ";
        $query .= "                 min(COURSECD||MAJORCD||EXAMCOURSECD) as COURSE ";
        $query .= "             FROM ";
        $query .= "                 ENTEXAM_PERFECT_MST ";
        $query .= "             WHERE ";
        $query .= "                 ENTEXAMYEAR     = '{$model->exp_year}' AND ";
        $query .= "                 APPLICANTDIV    = '{$model->applicantdiv}' AND ";
        $query .= "                 TESTDIV         = '00' ";
        $query .= "             GROUP BY ";
        $query .= "                 TESTSUBCLASSCD ";
        $query .= "         ) P2 ON P2.TESTSUBCLASSCD   = P1.TESTSUBCLASSCD ";
        $query .= "             AND P2.COURSE           = P1.COURSECD||P1.MAJORCD||P1.EXAMCOURSECD ";
        $query .= "     WHERE ";
        $query .= "         P1.ENTEXAMYEAR     = '{$model->exp_year}' AND ";
        $query .= "         P1.APPLICANTDIV    = '{$model->applicantdiv}' AND ";
        $query .= "         P1.TESTDIV         = '00' ";
        $query .= "     ) ";
        $query .= " , MAIN_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         R1.RECEPTNO, ";
        $query .= "         R1.ENTEXAMYEAR, ";
        $query .= "         R1.APPLICANTDIV, ";
        $query .= "         R1.TESTDIV, ";
        $query .= "         '{$model->testsubclasscd}' as TESTSUBCLASSCD, ";
        $query .= "         R1.EXAMNO, ";
        $query .= "         RD_009.REMARK1 AS ORDERNO, ";
        $query .= "         B1.NAME, ";
        $query .= "         B1.NAME_KANA, ";
        $query .= "         S1.ATTEND_FLG, ";
        $query .= "         S1.SCORE, ";
        //満点(初期値)　国語100点、算数100点、社会60点、理科60点
        $perfect = ($model->testsubclasscd == "3" || $model->testsubclasscd == "4") ? 60 : 100;
        $query .= "         VALUE(P1.PERFECT, {$perfect}) AS PERFECT, ";
        $query .= "         CASE WHEN ES.SUBCLASSCD IS NOT NULL THEN 1 END AS EXAMTYPE_SUBCLASS, ";
        $query .= "         'DUMMY' as DUMMY ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECEPT_DAT R1 ";
        $query .= "         INNER JOIN ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "              ON B1.ENTEXAMYEAR      = R1.ENTEXAMYEAR ";
        $query .= "             AND B1.APPLICANTDIV     = R1.APPLICANTDIV ";
        $query .= "             AND B1.EXAMNO           = R1.EXAMNO ";
        $query .= "         INNER JOIN PERFECT_T P1 ";
        $query .= "              ON P1.ENTEXAMYEAR      = R1.ENTEXAMYEAR ";
        $query .= "             AND P1.APPLICANTDIV     = R1.APPLICANTDIV ";
        $query .= "             AND P1.TESTSUBCLASSCD   = '{$model->evaltype}' ";
        $query .= "         LEFT JOIN ENTEXAM_SCORE_DAT S1 ";
        $query .= "              ON S1.ENTEXAMYEAR      = R1.ENTEXAMYEAR ";
        $query .= "             AND S1.APPLICANTDIV     = R1.APPLICANTDIV ";
        $query .= "             AND S1.TESTDIV          = R1.TESTDIV ";
        $query .= "             AND S1.EXAM_TYPE        = R1.EXAM_TYPE ";
        $query .= "             AND S1.RECEPTNO         = R1.RECEPTNO ";
        $query .= "             AND S1.TESTSUBCLASSCD   = '{$model->evaltype}' ";
        $query .= "         LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD_009 ";
        $query .= "              ON RD_009.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "             AND RD_009.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "             AND RD_009.TESTDIV      = R1.TESTDIV ";
        $query .= "             AND RD_009.EXAM_TYPE    = R1.EXAM_TYPE ";
        $query .= "             AND RD_009.RECEPTNO     = R1.RECEPTNO ";
        $query .= "             AND RD_009.SEQ          = '009' ";
        $query .= "         LEFT JOIN ENTEXAM_EXAMTYPE_SUBCLASS_MST ES ";
        $query .= "              ON ES.ENTEXAMYEAR      = R1.ENTEXAMYEAR ";
        $query .= "             AND ES.APPLICANTDIV     = R1.APPLICANTDIV ";
        $query .= "             AND ES.EXAM_TYPE        = R1.EXAM_TYPE ";
        $query .= "             AND ES.SUBCLASSCD       = '{$model->evaltype}' ";
        $query .= "     WHERE ";
        $query .= "             R1.ENTEXAMYEAR  = '{$model->exp_year}' ";
        $query .= "         AND R1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "         AND R1.TESTDIV      = '{$model->testdiv}' ";
        if ($model->examtype != "ALL") {
            $query .= "         AND R1.EXAM_TYPE    = '{$model->examtype}' ";
        }
        if ($model->applicantdiv == "1") {
            if ($model->s_findno != "") {
                $query .= "         AND RD_009.REMARK1 >= '{$model->s_findno}' ";
            }
            if ($model->e_findno != "") {
                $query .= "         AND RD_009.REMARK1 <= '{$model->e_findno}' ";
            }
        } else {
            if ($model->s_findno != "") {
                $query .= "         AND R1.RECEPTNO >= '{$model->s_findno}' ";
            }
            if ($model->e_findno != "") {
                $query .= "         AND R1.RECEPTNO <= '{$model->e_findno}' ";
            }
        }
        $query .= "         AND VALUE(R1.JUDGEDIV,'0') != '5' "; //受験不可
        $query .= " ORDER BY ";
        $query .= "     RD_009.REMARK1,";
        $query .= "     RECEPTNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " ORDER BY ";
        $query .= "     ORDERNO, ";
        $query .= "     RECEPTNO ";
        return $query;

    }

    function getScoreDatCount($model, $receptno, $examtype, $seq="") {
        $tblname = "ENTEXAM_SCORE_DAT";

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     {$tblname} ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR         = '{$model->exp_year}' ";
        $query .= "     AND APPLICANTDIV    = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV         = '{$model->testdiv}' ";
        $query .= "     AND EXAM_TYPE       = '{$examtype}' ";
        $query .= "     AND RECEPTNO        = '{$receptno}' ";
        $query .= "     AND TESTSUBCLASSCD  = '{$model->evaltype}' ";
        if ($seq != "") {
            $query .= "     AND SEQ         = '{$seq}' ";
        }

        return $query;
    }

    //入試方式取得
    function getExamTypeData($model) {
        $query  = " SELECT ";
        $query .= "     R1.RECEPTNO, ";
        $query .= "     R1.EXAM_TYPE, ";
        $query .= "     CASE WHEN ES.SUBCLASSCD IS NOT NULL THEN 1 END AS EXAMTYPE_SUBCLASS ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT R1 ";
        $query .= "     LEFT JOIN ENTEXAM_EXAMTYPE_SUBCLASS_MST ES ";
        $query .= "          ON ES.ENTEXAMYEAR      = R1.ENTEXAMYEAR ";
        $query .= "         AND ES.APPLICANTDIV     = R1.APPLICANTDIV ";
        $query .= "         AND ES.EXAM_TYPE        = R1.EXAM_TYPE ";
        $query .= "         AND ES.SUBCLASSCD       = '{$model->evaltype}' ";
        $query .= " WHERE ";
        $query .= "     R1.ENTEXAMYEAR      = '{$model->exp_year}' ";
        $query .= "     AND R1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND R1.TESTDIV      = '{$model->testdiv}' ";

        return $query;
    }

    //更新
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if ($model->examtype == "ALL") {
            $examtypeArray = array();
            $result = $db->query(knjl550jQuery::getExamTypeData($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $examtypeArray[$row["RECEPTNO"]] = array($row["EXAM_TYPE"], $row["EXAMTYPE_SUBCLASS"]);
            }
        }

        //追加or更新
        foreach ($model->score as $receptno => $score) {
            //入試方式
            $examtype = $model->examtype;
            if ($model->examtype == "ALL") {
                list ($examtype, $examtype_subclass) = $examtypeArray[$receptno];
                if (!$examtype_subclass) continue;
            }

            $data = array();
            if ($score === '*') {
                $data["ATTEND_FLG"][TEXT]   = "0";
                $data["SCORE"][NUMBER]      = "";
            } else {
                $data["ATTEND_FLG"][TEXT]   = !strlen($score) ? "" : "1";
                $data["SCORE"][NUMBER]      = !strlen($score) ? "" : $score;
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $reccnt = $db->getOne(knjl550jQuery::getScoreDatCount($model, $receptno, $examtype));
            if ($reccnt > 0) {
                $where  = " WHERE ";
                $where .= "     ENTEXAMYEAR         = '{$model->exp_year}' ";
                $where .= "     AND APPLICANTDIV    = '{$model->applicantdiv}' ";
                $where .= "     AND TESTDIV         = '{$model->testdiv}' ";
                $where .= "     AND EXAM_TYPE       = '{$examtype}' ";
                $where .= "     AND RECEPTNO        = '{$receptno}' ";
                $where .= "     AND TESTSUBCLASSCD  = '{$model->evaltype}' ";
                $query = Query::updateSQL($data, "ENTEXAM_SCORE_DAT", $where);
                $db->query($query);
            } else {
                $data["ENTEXAMYEAR"][TEXT]      = $model->exp_year;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["TESTDIV"][TEXT]          = $model->testdiv;
                $data["EXAM_TYPE"][TEXT]        = $examtype;
                $data["RECEPTNO"][TEXT]         = $receptno;
                $data["TESTSUBCLASSCD"][TEXT]   = $model->evaltype;
                $query = Query::insertSQL($data, "ENTEXAM_SCORE_DAT");
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>