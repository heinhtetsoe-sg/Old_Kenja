<?php

require_once('for_php7.php');

class knjl051wQuery extends Query {

    //権限チェック（入試管理者）
    function getAdminFlg() {
        $query  = " SELECT ";
        $query .= "     FIELD1 ";
        $query .= " FROM ";
        $query .= "     STAFF_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND STAFFCD = '".STAFFCD."' ";
        $query .= "     AND STAFF_SEQ = '009' ";

        $db = Query::dbCheckOut();
        $rtnFlg = $db->getOne($query);
        Query::dbCheckIn($db);

        return $rtnFlg;
    }
    
    //名称マスタより取得
    function getNameMst($model, $namecd1, $year, $namecd2 = "") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAME1 AS CSV_NAME ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }
        if ($namecd1 == "L057") {
            $query .= "     AND NAMESPARE1 = '{$model->dataDiv}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学科・コース取得
    function getCourse($model, $course = "") {
        $query  = " SELECT ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.EXAMCOURSECD || ':' || M1.MAJORNAME || T1.EXAMCOURSE_NAME AS LABEL, ";
        $query .= "     T1.COURSECD || '_' || T1.MAJORCD || '_' || T1.EXAMCOURSECD AS VALUE, ";
        $query .= "     M1.MAJORNAME || T1.EXAMCOURSE_NAME AS CSV_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST T1 ";
        $query .= "     LEFT JOIN MAJOR_MST M1 ";
        $query .= "          ON M1.COURSECD = T1.COURSECD ";
        $query .= "         AND M1.MAJORCD = T1.MAJORCD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR     = '".$model->ObjYear."' ";
        $query .= "     AND T1.APPLICANTDIV    = '".$model->applicantdiv."' ";
        $query .= "     AND T1.TESTDIV         = '".$model->testdiv."' ";
        if ($course) {
            $query .= "     AND T1.COURSECD || '_' || T1.MAJORCD || '_' || T1.EXAMCOURSECD = '{$course}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト科目取得
    function getTestSubclass($model) {
        $query .= " SELECT ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'L057' ";
        $query .= "     AND NAMECD2 = '{$model->kaijiSubclasscd}' ";
        return $query;
    }

    //一覧表示
    function SelectQuery($model, $flg, $testSubclass = "") {
        $query  = " WITH MAIN_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.EXAMNO, ";
        $query .= "         T1.RECOM_EXAMNO, ";
        $query .= "         T1.JUDGEMENT, ";
        $query .= "         T1.NAME, ";
        $query .= "         T1.NAME_KANA, ";
        $query .= "         T1.LAST_DAI1_COURSECD || T1.LAST_DAI1_MAJORCD || T1.LAST_DAI1_COURSECODE || ':' || M1.MAJORNAME || C1.EXAMCOURSE_NAME AS DAI1_COURSENAME, ";
        $query .= "         T1.LAST_DAI2_COURSECD || T1.LAST_DAI2_MAJORCD || T1.LAST_DAI2_COURSECODE || ':' || M2.MAJORNAME || C2.EXAMCOURSE_NAME AS DAI2_COURSENAME, ";
        $query .= "         L1.SCORE, ";
        $query .= "         999 AS PERFECT ";
        $query .= "     FROM ";
        $query .= "         V_ENTEXAM_APPLICANTBASE_DAT T1 ";
        if ($model->cmd == "copy") {
            $query .= "         LEFT JOIN ENTEXAM_SCORE_DAT L1 ";
            $query .= "              ON L1.ENTEXAMYEAR      = T1.ENTEXAMYEAR ";
            $query .= "             AND L1.APPLICANTDIV     = T1.APPLICANTDIV ";
            $query .= "             AND L1.TESTDIV          = T1.TESTDIV ";
            $query .= "             AND L1.RECEPTNO         = T1.EXAMNO ";
            $query .= "             AND L1.TESTSUBCLASSCD   = '{$testSubclass}' ";
        } else {
            $query .= "         LEFT JOIN ENTEXAM_KAIJI_DAT L1 ";
            $query .= "              ON L1.ENTEXAMYEAR      = T1.ENTEXAMYEAR ";
            $query .= "             AND L1.APPLICANTDIV     = T1.APPLICANTDIV ";
            $query .= "             AND L1.TESTDIV          = T1.TESTDIV ";
            $query .= "             AND L1.RECEPTNO         = T1.EXAMNO ";
            $query .= "             AND L1.KAIJI_SUBCLASSCD = '{$model->kaijiSubclasscd}' ";
        }
        $query .= "         LEFT JOIN ENTEXAM_COURSE_MST C1 ";
        $query .= "              ON C1.ENTEXAMYEAR      = T1.ENTEXAMYEAR ";
        $query .= "             AND C1.APPLICANTDIV     = T1.APPLICANTDIV ";
        $query .= "             AND C1.TESTDIV          = T1.TESTDIV ";
        $query .= "             AND C1.COURSECD         = T1.LAST_DAI1_COURSECD ";
        $query .= "             AND C1.MAJORCD          = T1.LAST_DAI1_MAJORCD ";
        $query .= "             AND C1.EXAMCOURSECD     = T1.LAST_DAI1_COURSECODE ";
        $query .= "         LEFT JOIN ENTEXAM_COURSE_MST C2 ";
        $query .= "              ON C2.ENTEXAMYEAR      = T1.ENTEXAMYEAR ";
        $query .= "             AND C2.APPLICANTDIV     = T1.APPLICANTDIV ";
        $query .= "             AND C2.TESTDIV          = T1.TESTDIV ";
        $query .= "             AND C2.COURSECD         = T1.LAST_DAI2_COURSECD ";
        $query .= "             AND C2.MAJORCD          = T1.LAST_DAI2_MAJORCD ";
        $query .= "             AND C2.EXAMCOURSECD     = T1.LAST_DAI2_COURSECODE ";
        $query .= "         LEFT JOIN MAJOR_MST M1 ";
        $query .= "              ON M1.COURSECD         = T1.LAST_DAI1_COURSECD ";
        $query .= "             AND M1.MAJORCD          = T1.LAST_DAI1_MAJORCD ";
        $query .= "         LEFT JOIN MAJOR_MST M2 ";
        $query .= "              ON M2.COURSECD         = T1.LAST_DAI2_COURSECD ";
        $query .= "             AND M2.MAJORCD          = T1.LAST_DAI2_MAJORCD ";
        $query .= "     WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->ObjYear}' AND ";
        $query .= "         T1.APPLICANTDIV = '{$model->applicantdiv}' AND ";
        $query .= "         T1.TESTDIV      = '{$model->testdiv}' AND ";
        $query .= "         T1.LAST_DAI1_COURSECD || '_' || T1.LAST_DAI1_MAJORCD || '_' || T1.LAST_DAI1_COURSECODE = '".$model->examcoursecd."' ";
        $query .= "         AND VALUE(T1.JUDGEMENT, '0') <> '5' ";//願変の人は除く
        $query .= "         AND T1.RECOM_EXAMNO IS NOT NULL ";
        if ($model->testdiv2 == "1") {
            $query .= "     AND T1.TESTDIV2 = '{$model->testdiv2}' ";
        }
        if ($flg == "") {
            if ($model->cmd == "back") {
                $query .= "   AND T1.EXAMNO < '".$model->s_receptno."' ";
                $query .= " ORDER BY T1.EXAMNO DESC ";
            } else if ($model->cmd == "next") {
                $query .= "   AND T1.EXAMNO > '".$model->e_receptno."' ";
                $query .= " ORDER BY T1.EXAMNO ";
            }
        } else if ($flg == "BAC_COUNT" || $flg == "NEXT_COUNT") {
            if ($flg == "BAC_COUNT") {
                $query .= "   AND T1.EXAMNO < '".$model->s_receptno."' ";
                $query .= " ORDER BY T1.EXAMNO DESC ";
            } else if ($flg == "NEXT_COUNT") {
                $query .= "   AND T1.EXAMNO > '".$model->e_receptno."' ";
                $query .= " ORDER BY T1.EXAMNO ";
            }
        } else if ($flg == "CHECK") {
                $query .= "   AND T1.EXAMNO >= '".$model->s_receptno."' ";
        }
        $query .= "     FETCH FIRST 500 ROWS ONLY ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        if ($flg != "BAC_COUNT" && $flg != "NEXT_COUNT" ) {
            $query .= " ORDER BY EXAMNO";
        }

        return $query;
    }

    //更新(一度すべて削除してから追加）
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();

        //保持
        $keep = array();

        $result = $db->query(knjl051wQuery::SelectQuery($model, "CHECK"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $keep[$row["EXAMNO"]] = $row;

            $checkScore = $model->score[$row["EXAMNO"]];
        }

        $db->autoCommit(false);

        //削除
        $query  = " DELETE FROM ENTEXAM_KAIJI_DAT ";
        $query .= " WHERE entexamyear    = '".$model->ObjYear."' ";
        $query .= "   AND applicantdiv   = '".$model->applicantdiv."' ";
        $query .= "   AND testdiv        = '".$model->testdiv."' ";
        $query .= "   AND exam_type      = '1' ";
        $query .= "   AND KAIJI_SUBCLASSCD = '".$model->kaijiSubclasscd."' ";
        $query .= "   AND receptno IN ('".implode("','",array_keys($model->score))."')";
        $db->query($query);

        //追加
        foreach ($model->score as $receptno => $score) {
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["TESTDIV"][TEXT]          = $model->testdiv;
            $data["EXAM_TYPE"][TEXT]        = 1;
            $data["RECEPTNO"][TEXT]         = $receptno;
            $data["KAIJI_SUBCLASSCD"][TEXT] = $model->kaijiSubclasscd;
            $data["SCORE"][TEXT]            = !strlen($score) ? "" : $score;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $query = Query::insertSQL($data, "ENTEXAM_KAIJI_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>