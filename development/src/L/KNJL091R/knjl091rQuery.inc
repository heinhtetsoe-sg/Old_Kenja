<?php

require_once('for_php7.php');

class knjl091rQuery extends Query {
    //名称マスタよりデータを取得
    function getName($ObjYear, $namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD1, ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     NAME2, ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$ObjYear ."' ";
        $query .= "     AND NAMECD1 IN ('".implode($namecd1, "','") ."') ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD1, ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //名称マスタよりデータを取得
    function get_name_cd($year, $nameCd1, $nameCd2='') {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME2, "; //中学用名称
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$nameCd1."' ";
        if ($nameCd2 != '') {
            $query .= "     AND NAMECD2 = '".$nameCd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験コースマスタより志望学科を取得
    function get_coursemajor($year, $applicantdiv, $testdiv, $coursemajor = "") {
        $query  = " SELECT ";
        $query .= "     t1.coursecd, t1.majorcd, t1.examcoursecd, ";
        $query .= "     RTRIM(t1.examcourse_name) AS coursename ";
        $query .= " FROM ";
        $query .= "     entexam_course_mst t1 ";
        $query .= " WHERE ";
        $query .= "     t1.entexamyear  = '{$year}' and ";
        $query .= "     t1.applicantdiv = '{$applicantdiv}' and ";
        $query .= "     t1.testdiv      = '{$testdiv}' ";
        if (strlen($coursemajor)) {
            $query .= "     and t1.coursecd || t1.majorcd || t1.examcoursecd = '{$coursemajor}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     t1.coursecd, t1.majorcd, t1.examcoursecd ";

        return $query;
    }

    //合格コース
    function getEntexamCourseMst($year, $applicantdiv) {
        $query  = " SELECT DISTINCT ";
        $query .= "     E_COURSE.COURSECD || E_COURSE.MAJORCD || E_COURSE.EXAMCOURSECD AS VALUE, ";
        $query .= "     E_COURSE.COURSECD || E_COURSE.MAJORCD || E_COURSE.EXAMCOURSECD || ':' || EXAMCOURSE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST E_COURSE ";
        $query .= " WHERE ";
        $query .= "         E_COURSE.ENTEXAMYEAR  = '{$year}' ";
        $query .= "     AND E_COURSE.APPLICANTDIV = '{$applicantdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入学課程コース取得
    function getCourseMajorCoursecode($year, $applicantdiv) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.ENTER_COURSECD || T1.ENTER_MAJORCD || T1.ENTER_COURSECODE AS VALUE, ";
        $query .= "     T1.ENTER_COURSECD || T1.ENTER_MAJORCD || T1.ENTER_COURSECODE || ':' || L1.COURSENAME || L1.MAJORNAME || '　' || L2.COURSECODENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST T1 ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST L1 ON L1.YEAR     = T1.ENTEXAMYEAR ";
        $query .= "                                    AND L1.COURSECD = T1.ENTER_COURSECD ";
        $query .= "                                    AND L1.MAJORCD  = ENTER_MAJORCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST L2 ON L2.YEAR       = T1.ENTEXAMYEAR ";
        $query .= "                                  AND L2.COURSECODE = T1.ENTER_COURSECODE ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$applicantdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //メインデータ取得
    function get_edit_data($model) {
        $query  = " SELECT";
        $query .= "     T1.ENTEXAMYEAR,";
        $query .= "     T1.EXAMNO,";
        $query .= "     T1.NAME,";
        $query .= "     T1.NAME_KANA,";
        $query .= "     T1.BIRTH_Y,";
        $query .= "     T1.BIRTH_M,";
        $query .= "     T1.BIRTH_D,";
        $query .= "     T1.BIRTHDAY,";
        $query .= "     T1.SEX,";
        $query .= "     T1.DESIREDIV, ";
        $query .= "     T1.APPLICANTDIV,";
        $query .= "     N0.NAME1 AS APPLICANTDIVNAME,";
        $query .= "     T1.TESTDIV,";
        $query .= "     N1.NAME1 AS TESTDIVNAME,";
        $query .= "     T1.SUC_COURSECD || T1.SUC_MAJORCD || T1.SUC_COURSECODE AS COURSEMAJOR,";
        $query .= "     E1.REMARK1 || E1.REMARK2 || E1.REMARK3 AS ENTER_COURSEMAJOR,";
        $query .= "     T1.JUDGEMENT,";
        $query .= "     D_030.REMARK1 as PASS_ENTDIV,";
        $query .= "     D_030.REMARK2 as PASS_EXAMNO,";
        $query .= "     D_030.REMARK3 as PASS_JUDGEDIV,";
        $query .= "     T6.NAME1 AS JUDGEMENT_NAME,";
        $query .= "     T1.PROCEDUREDIV,";
        $query .= "     T1.PROCEDUREDATE,";
        $query .= "     T1.ENTDIV,";
        $query .= "     J1.REMARK1 AS JIZEN_PAGE, ";
        $query .= "     J1.REMARK2 AS JIZEN_SEQ, ";
        $query .= "     J1.REMARK1 || '-' || J1.REMARK2 AS JIZEN, ";
        $query .= "     T2.NAME1 AS SEXNAME,";
        $query .= "     T5.NAME1 AS ERA_NAME";
        $query .= " FROM";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT OUTER JOIN V_NAME_MST T2 ON T2.YEAR    = T1.ENTEXAMYEAR  ";
        $query .= "                                  AND T2.NAMECD1 = 'Z002' ";
        $query .= "                                  AND T2.NAMECD2 = T1.SEX ";
        $query .= "     LEFT OUTER JOIN V_NAME_MST T5 ON T5.YEAR    = T1.ENTEXAMYEAR ";
        $query .= "                                  AND T5.NAMECD1 = 'L007' ";
        $query .= "                                  AND T5.NAMECD2 = T1.ERACD ";
        $query .= "     LEFT OUTER JOIN V_NAME_MST T6 ON T6.YEAR    = T1.ENTEXAMYEAR ";
        $query .= "                                  AND T6.NAMECD1 = 'L013' ";
        $query .= "                                  AND T6.NAMECD2 = T1.JUDGEMENT ";
        $query .= "     LEFT OUTER JOIN V_NAME_MST N0 ON N0.YEAR    = T1.ENTEXAMYEAR ";
        $query .= "                                  AND N0.NAMECD1 = 'L003' ";
        $query .= "                                  AND N0.NAMECD2 = T1.APPLICANTDIV  ";
        $query .= "     LEFT OUTER JOIN V_NAME_MST N1 ON N1.YEAR    = T1.ENTEXAMYEAR ";
        $query .= "                                  AND N1.NAMECD1 = 'L004' ";
        $query .= "                                  AND N1.NAMECD2 = T1.TESTDIV  ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT J1 ON J1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "                                                  AND J1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                                                  AND J1.EXAMNO       = T1.EXAMNO ";
        $query .= "                                                  AND J1.SEQ          = '002' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT E1 ON E1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "                                                  AND E1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                                                  AND E1.EXAMNO       = T1.EXAMNO ";
        $query .= "                                                  AND E1.SEQ          = '007' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT D_030 ON D_030.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "                                                     AND D_030.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                                                     AND D_030.EXAMNO       = T1.EXAMNO ";
        $query .= "                                                     AND D_030.SEQ          = '030' ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '".$model->year."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->applicantdiv."' AND ";

        if ($model->cmd == "back1" || $model->cmd == "back2") {
            $query .= "      T1.EXAMNO < '".$model->examno."'";
            $query .= " ORDER BY T1.EXAMNO DESC ";
            //$query .= " FETCH FIRST 1 ROWS ONLY "; 
        } elseif ($model->cmd == "next1" || $model->cmd == "next2") {
            $query .= "      T1.EXAMNO > '".$model->examno."'";
            $query .= " ORDER BY T1.EXAMNO ASC ";
            //$query .= " FETCH FIRST 1 ROWS ONLY "; 
        } else {
            $query .= "      T1.EXAMNO = '".$model->examno."'";
        }

        return $query;
    }

    //受付データのテーブル
    function getRecept($model, $applicantdiv) {
        $query  = " SELECT";
        $query .= "     T1.RECEPTNO, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.TESTDIV AS TESTDIV2, ";
        $query .= "     T1.EXAM_TYPE, ";
        $query .= "     L013.NAME1 as JUDGEDIV, ";
        $query .= "     T1.PROCEDUREDATE1, ";
        foreach ($model->testDivArr as $key => $val) {
            $query .= "     BUN_012.REMARK{$val} as RECEPTNO_{$val}, ";
            $query .= "     BUN_013.REMARK{$val} as SHDIV_{$val}, ";
            $query .= "     BUN_014.REMARK{$val} as INNER_PROMISE_{$val}, ";
        }
        $query .= "     RD1_008.REMARK1 as ADD_SCORE1, ";
        $query .= "     RD1_008.REMARK2 as ADD_SCORE2, ";
        $query .= "     T1.TOTAL4, ";
        $query .= "     T1.AVARAGE4, ";
        $query .= "     T1.TOTAL_RANK4, ";
        $query .= "     T3.EXAMHALL_NAME ";
        $query .= " FROM";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD1_008 ON RD1_008.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "                                                AND RD1_008.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                                                AND RD1_008.TESTDIV      = T1.TESTDIV ";
        $query .= "                                                AND RD1_008.EXAM_TYPE    = T1.EXAM_TYPE ";
        $query .= "                                                AND RD1_008.RECEPTNO     = T1.RECEPTNO ";
        $query .= "                                                AND RD1_008.SEQ          = '008' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BUN_012 ON BUN_012.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "                                                           AND BUN_012.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                                                           AND BUN_012.EXAMNO       = T1.EXAMNO ";
        $query .= "                                                           AND BUN_012.SEQ          = '012' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BUN_013 ON BUN_013.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "                                                           AND BUN_013.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                                                           AND BUN_013.EXAMNO       = T1.EXAMNO ";
        $query .= "                                                           AND BUN_013.SEQ          = '013' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BUN_014 ON BUN_014.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "                                                           AND BUN_014.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                                                           AND BUN_014.EXAMNO       = T1.EXAMNO ";
        $query .= "                                                           AND BUN_014.SEQ          = '014' ";
        $query .= "     LEFT JOIN ENTEXAM_HALL_YDAT T3 ";
        $query .= "                   ON T1.ENTEXAMYEAR  = T3.ENTEXAMYEAR ";
        $query .= "                  AND T1.APPLICANTDIV = T3.APPLICANTDIV ";
        $query .= "                  AND T1.TESTDIV      = T3.TESTDIV ";
        $query .= "                  AND T1.EXAM_TYPE    = T3.EXAM_TYPE ";
        $query .= "                  AND T1.RECEPTNO BETWEEN T3.S_RECEPTNO AND T3.E_RECEPTNO ";
        $query .= "     LEFT JOIN V_NAME_MST L013 ON L013.YEAR    = T1.ENTEXAMYEAR ";
        $query .= "                              AND L013.NAMECD1 = 'L013' ";
        $query .= "                              AND L013.NAMECD2 = T1.JUDGEDIV ";
        $query .= " WHERE";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '{$model->testdiv2}' ";
        $query .= "     AND T1.EXAM_TYPE    = '{$model->exam_type}' ";
        $query .= "     AND T1.EXAMNO       = '{$model->examno}' ";

        return $query;
    }

    //得点データ取得
    function getScore($model, $applicantdiv) {
        $query  = " SELECT ";
        $query .= "     T2.TESTSUBCLASSCD, ";
        $query .= "     T2.ATTEND_FLG, ";
        $query .= "     T2.SCORE, ";
        $query .= "     L009.NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_SCORE_DAT T2 ";
        $query .= "         ON  T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.TESTDIV      = T1.TESTDIV ";
        $query .= "         AND T2.EXAM_TYPE    = T1.EXAM_TYPE ";
        $query .= "         AND T2.RECEPTNO     = T1.RECEPTNO ";
        $query .= "     LEFT JOIN V_NAME_MST L009 ON L009.YEAR    = T1.ENTEXAMYEAR ";
        $query .= "                              AND L009.NAMECD1 = 'L009' ";
        $query .= "                              AND L009.NAMECD2 = T2.TESTSUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '{$model->year}' AND ";
        $query .= "     T1.APPLICANTDIV = '{$applicantdiv}' AND ";
        $query .= "     T1.TESTDIV      = '{$model->testdiv2}' AND ";
        $query .= "     T1.EXAMNO       = '{$model->examno}' ";
        $query .= " ORDER BY ";
        $query .= "     T2.TESTSUBCLASSCD ";

        return $query;
    }

    //存在チェック
    function Existdata($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND EXAMNO       = '".$model->examno."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //--- UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        // 2.志願者基礎データの更新
        $data = array();
        $data["SUC_COURSECD"][TEXT]         = $model->field["SUC_COURSECD"];
        $data["SUC_MAJORCD"][TEXT]          = $model->field["SUC_MAJORCD"];
        $data["SUC_COURSECODE"][TEXT]       = $model->field["SUC_COURSECODE"];
        $data["JUDGEMENT"][TEXT]            = $model->judgement;
        $data["PROCEDUREDIV"][TEXT]         = $model->field["PROCEDUREDIV"];
        $data["PROCEDUREDATE"][TEXT]        = str_replace("/", "-", $model->field["PROCEDUREDATE"]);
        $data["ENTDIV"][TEXT]               = $model->field["ENTDIV"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
        $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
        $where .= "   AND EXAMNO        = '".$model->examno."'";
        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
        $db->query($query);

        // 3.入学基礎詳細データの更新（入学コース）
        $seq007Cnt = $db->getOne(knjl091rQuery::getCntData($model->year, $model->applicantdiv, $model->examno, '007'));
        if ($model->field["ENTER_COURSEMAJOR"]) {
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]  = $model->year;
            $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
            $data["EXAMNO"][TEXT]       = $model->examno;
            $data["SEQ"][TEXT]          = "007";
            $data["REMARK1"][TEXT]      = $model->field["ENTER_COURSECD"];
            $data["REMARK2"][TEXT]      = $model->field["ENTER_MAJORCD"];
            $data["REMARK3"][TEXT]      = $model->field["ENTER_COURSECODE"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            if ($seq007Cnt == 0) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
                $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."'";
                $where .= "   AND EXAMNO        = '".$model->examno."'";
                $where .= "   AND SEQ           = '007'";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //カウント取得
    function getCntData($year, $applicantDiv, $examNo, $seq) {
        $query  = " SELECT DISTINCT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR   = '{$year}' ";
        $query .= "     AND APPLICANTDIV  = '{$applicantDiv}' ";
        $query .= "     AND EXAMNO        = '{$examNo}' ";
        $query .= "     AND SEQ           = '{$seq}' ";

        return $query;
    }
}
?>