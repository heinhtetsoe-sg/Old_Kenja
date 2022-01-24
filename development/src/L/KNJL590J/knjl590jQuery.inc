<?php

require_once('for_php7.php');

class knjl590jQuery extends Query 
{

    //名称マスタよりデータを取得
    function getName($ObjYear, $namecd1) {
        $query  = " SELECT ";
        $query .= "     namecd1, ";
        $query .= "     namecd2, ";
        $query .= "     name1, ";
        $query .= "     name2, ";
        $query .= "     namespare1 ";
        $query .= " FROM ";
        $query .= "     v_name_mst ";
        $query .= " WHERE ";
        $query .= "     year    = '".$ObjYear ."' AND ";
        $query .= "     namecd1 IN ('".implode($namecd1, "','") ."') ";
        $query .= " ORDER BY ";
        $query .= "     namecd1, ";
        $query .= "     namecd2 ";
        return $query;
    }

    //名称マスタより取得
    function getNameMst($year, $namecd1, $nameCd2="") {
        $query  = " SELECT ";
        if ($nameCd2) {
            $query .= "     NAME1 ";
        } else {
            $query .= "     NAMECD2 AS VALUE, ";
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
            $query .= "     NAMECD2, ";
            $query .= "     NAME1, ";
            $query .= "     NAMESPARE2 ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($nameCd2) {
            $query .= "     AND NAMECD2 = '".$nameCd2."' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //入試種別取得
    function getTestDiv($model) {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //メインデータ取得
    function get_edit_data($model) {
        $query  = " SELECT";
        $query .= "     BASE.ENTEXAMYEAR,";
        $query .= "     BASE.EXAMNO,";
        $query .= "     BASE.NAME,";
        $query .= "     BASE.NAME_KANA,";
        $query .= "     BASE.APPLICANTDIV,";
        $query .= "     BASE.TESTDIV,";
        $query .= "     RCPT.JUDGEDIV AS JUDGEMENT,";
        $query .= "     RCPT.PROCEDUREDATE1 AS PROCEDUREDATE, ";
        $query .= "     RCPT.PROCEDUREDIV1  AS PROCEDUREDIV, ";
        $query .= "     RCPT.ADJOURNMENTDIV AS ENTDIV, ";
        $query .= "     BD026.REMARK1 AS SCHREGNO ";
        $query .= " FROM";
        $query .= "     ENTEXAM_RECEPT_DAT RCPT ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "          ON RCPT.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND RCPT.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND RCPT.EXAMNO       = BASE.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD026 ";
        $query .= "          ON RCPT.ENTEXAMYEAR  = BD026.ENTEXAMYEAR ";
        $query .= "         AND RCPT.APPLICANTDIV = BD026.APPLICANTDIV ";
        $query .= "         AND RCPT.EXAMNO       = BD026.EXAMNO ";
        $query .= "         AND BD026.SEQ         = '026' ";
        $query .= " WHERE ";
        $query .= "         RCPT.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND RCPT.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND RCPT.TESTDIV      = '{$model->testdiv}' ";

        if ($model->cmd == "back2" || $model->cmd == "back1") {
            $query.= "     AND RCPT.EXAMNO < '".$model->examno."'";
            $query.= " ORDER BY ";
            $query.= "     RCPT.EXAMNO DESC ";
        } elseif ($model->cmd == "next2" || $model->cmd == "next1") {
            $query.= "     AND RCPT.EXAMNO > '".$model->examno."'";
            $query.= " ORDER BY ";
            $query.= "     RCPT.EXAMNO ASC ";
        } else {
            $query.= "     AND RCPT.EXAMNO = '".$model->examno."'";
        }

        return $query;
    }

    //受験科目
    function getNameMstL009($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '{$model->year}' ";
        $query .= "     AND NAMECD1 = 'L009' ";
        $query .= "     AND NAMECD2 IN ( ";
        $query .= "                 SELECT ";
        $query .= "                     SUBCLASSCD ";
        $query .= "                 FROM ";
        $query .= "                     ENTEXAM_EXAMTYPE_SUBCLASS_MST ";
        $query .= "                 WHERE ";
        $query .= "                         ENTEXAMYEAR     = '{$model->year}' ";
        $query .= "                     AND APPLICANTDIV    = '{$model->applicantdiv}' ";
        $query .= "                 ) ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //成績・内申備考取得
    function getScoreRemark($model) {
        $query  = " SELECT ";
        $query .= "     R1.TESTDIV, ";
        $query .= "     T1.TESTDIV_NAME, ";
        $query .= "     R1.RECEPTNO, ";
        $query .= "     R1.EXAMNO, ";
        $query .= "     R1.TOTAL4, ";
        $query .= "     R1.TOTAL_RANK4, ";
        foreach ($model->subClassArr as $subCd => $subName) {
            $query .= "     S{$subCd}.SCORE AS SCORE{$subCd}, ";
        }
        $query .= "     C1.REMARK1 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT R1 ";
        $query .= "     LEFT JOIN ENTEXAM_TESTDIV_MST T1 ";
        $query .= "          ON T1.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "         AND T1.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND T1.TESTDIV      = R1.TESTDIV ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT C1 ";
        $query .= "          ON C1.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "         AND C1.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND C1.EXAMNO       = R1.EXAMNO ";
        foreach ($model->subClassArr as $subCd => $subName) {
            $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT S{$subCd} ";
            $query .= "          ON S{$subCd}.ENTEXAMYEAR    = R1.ENTEXAMYEAR ";
            $query .= "         AND S{$subCd}.APPLICANTDIV   = R1.APPLICANTDIV ";
            $query .= "         AND S{$subCd}.TESTDIV        = R1.TESTDIV ";
            $query .= "         AND S{$subCd}.EXAM_TYPE      = R1.EXAM_TYPE ";
            $query .= "         AND S{$subCd}.RECEPTNO       = R1.RECEPTNO ";
            $query .= "         AND S{$subCd}.TESTSUBCLASSCD = '{$subCd}' ";
        }
        $query .= " WHERE ";
        $query .= "         R1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND R1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND R1.EXAMNO IN ( ";
        $query .= "         SELECT ";
        $query .= "             EXAMNO ";
        $query .= "         FROM ";
        $query .= "             ENTEXAM_APPLICANTBASE_DAT ";
        $query .= "         WHERE ";
        $query .= "             ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "         AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "         AND RECOM_EXAMNO IN ( ";
        $query .= "             SELECT ";
        $query .= "                 RECOM_EXAMNO ";
        $query .= "             FROM ";
        $query .= "                 ENTEXAM_APPLICANTBASE_DAT ";
        $query .= "             WHERE ";
        $query .= "                 ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "             AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "             AND EXAMNO       = '{$model->examno}' ";
        $query .= "         ) ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     R1.TESTDIV ";

        return $query;
    }

    //存在チェック
    function Existdata($model) {
        $db = Query::dbCheckOut();

        $query = "  SELECT
                        EXAMNO
                    FROM
                        ENTEXAM_APPLICANTBASE_DAT
                    WHERE
                        ENTEXAMYEAR = '".$model->year."' AND
                        APPLICANTDIV = '".$model->applicantdiv."' AND
                        EXAMNO      = '".$model->examno."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //合格・入学コース
    function getSucEntCourse($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     COURSECD        AS SUC_COURSECD, ";
        $query .= "     MAJORCD         AS SUC_MAJORCD, ";
        $query .= "     EXAMCOURSECD    AS SUC_COURSECODE, ";
        $query .= "     ENTER_COURSECD, ";
        $query .= "     ENTER_MAJORCD, ";
        $query .= "     ENTER_COURSECODE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV      = '{$model->testdiv}' ";
        return $query;
    }

    //名称マスタよりデータを取得
    function getJudge($model) {
        $query  = " SELECT ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '{$model->year}' AND ";
        $query .= "     NAMECD1 = 'L013' AND ";
        $query .= "     NAMECD2 = '{$model->judgement}' ";
        return $query;
    }

    //ENTEXAM_APPLICANTBASE_DAT更新用
    function getBaseInfo($model) {
        $query  = " SELECT ";
        $query .= "     SUM(CASE WHEN JUDGEDIV = '4' THEN 1 ELSE 0 END) AS ABSENCE_CNT, ";
        $query .= "     SUM(CASE WHEN JUDGEDIV = '2' THEN 1 ELSE 0 END) AS UNPASS_CNT, ";
        $query .= "     SUM(CASE WHEN JUDGEDIV = '1' THEN 1 ELSE 0 END) AS PASS_CNT, ";
        $query .= "     SUM(CASE WHEN JUDGEDIV = '1' AND PROCEDUREDIV1 = '1' AND ADJOURNMENTDIV = '1' THEN 1 ELSE 0 END) AS ENT_CNT, ";
        $query .= "     SUM(CASE WHEN JUDGEDIV = '1' AND PROCEDUREDIV1 = '1' AND ADJOURNMENTDIV = '2' THEN 1 ELSE 0 END) AS JITAI_CNT, ";
        $query .= "     MAX(PROCEDUREDATE1) AS PROCEDUREDATE, ";
        $query .= "     MAX(BASE026.REMARK1) AS SCHREGNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RCPT ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON RCPT.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "                                              AND RCPT.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "                                              AND RCPT.EXAMNO       = BASE.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE026 ON RCPT.ENTEXAMYEAR  = BASE026.ENTEXAMYEAR ";
        $query .= "                                                       AND RCPT.APPLICANTDIV = BASE026.APPLICANTDIV ";
        $query .= "                                                       AND RCPT.EXAMNO       = BASE026.EXAMNO ";
        $query .= "                                                       AND BASE026.SEQ       = '026' ";
        $query .= " WHERE ";
        $query .= "         RCPT.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND RCPT.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND RCPT.EXAMNO       = '{$model->examno}' ";
        return $query;
    }

    //--- UPDATE
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //合格フラグ取得したい
        $judge = $db->getOne(knjl590jQuery::getJudge($model));

        //合格・入学コース取得したい
        $courseRow = $db->getRow(knjl590jQuery::getSucEntCourse($model), DB_FETCHMODE_ASSOC);

        // 1.受付データの更新
        $data = array();
        $data["JUDGEDIV"][TEXT]         = $model->judgement;
        $data["PROCEDUREDIV1"][TEXT]    = $model->field["PROCEDUREDIV"];
        $data["PROCEDUREDATE1"][TEXT]   = str_replace("/", "-", $model->field["PROCEDUREDATE"]);
        $entdiv = ($model->field["PROCEDUREDIV"] == "1" && $model->field["ENTDIV"] == "") ? "1" : $model->field["ENTDIV"];
        $data["ADJOURNMENTDIV"][TEXT]   = $entdiv;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' AND";
        $where .= "       APPLICANTDIV  = '".$model->applicantdiv."' AND ";
        $where .= "       TESTDIV       = '".$model->testdiv."' AND ";
        $where .= "       EXAMNO        = '".$model->examno."'";
        $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DAT更新用
        $baseInfo = $db->getRow(knjl590jQuery::getBaseInfo($model), DB_FETCHMODE_ASSOC);
        $setField = array();
        //合否
        if ($baseInfo["PASS_CNT"] > 0) {
            $setField["JUDGEMENT"] = "1";
        } else if ($baseInfo["UNPASS_CNT"] > 0) {
            $setField["JUDGEMENT"] = "2";
        } else if ($baseInfo["ABSENCE_CNT"] > 0) {
            $setField["JUDGEMENT"] = "4";
        } else {
            $setField["JUDGEMENT"] = "";
        }
        //手続情報
        if ($baseInfo["ENT_CNT"] > 0) {
            $setField["PROCEDUREDIV"] = "1";
            $setField["PROCEDUREDATE"] = $baseInfo["PROCEDUREDATE"];
            $setField["ENTDIV"] = "1";
        } else if ($baseInfo["JITAI_CNT"] > 0) {
            $setField["PROCEDUREDIV"] = "1";
            $setField["PROCEDUREDATE"] = $baseInfo["PROCEDUREDATE"];
            $setField["ENTDIV"] = "2";
        } else if ($baseInfo["PASS_CNT"] > 0) {
            $setField["PROCEDUREDIV"] = "2";
            $setField["PROCEDUREDATE"] = "";
            $setField["ENTDIV"] = "";
        } else {
            $setField["PROCEDUREDIV"] = "";
            $setField["PROCEDUREDATE"] = "";
            $setField["ENTDIV"] = "";
        }
        //学籍番号
        if ($entdiv == "1") { //①画面の学籍番号が入力可の状態
            $setField["SCHREGNO"] = $model->field["SCHREGNO"];
        } else if ($setField["ENTDIV"] == "1") { //①の状態 または ②画面の学籍番号が入力不可の状態だが、学籍番号が表示（入力）されている状態
            $setField["SCHREGNO"] = $baseInfo["SCHREGNO"];
        } else {
            $setField["SCHREGNO"] = "";
        }

        // 2.志願者基礎データの更新
        $data = array();
        $data["SUC_COURSECD"][TEXT]     = ($setField["JUDGEMENT"] != "1") ? "" : $courseRow["SUC_COURSECD"];
        $data["SUC_MAJORCD"][TEXT]      = ($setField["JUDGEMENT"] != "1") ? "" : $courseRow["SUC_MAJORCD"];
        $data["SUC_COURSECODE"][TEXT]   = ($setField["JUDGEMENT"] != "1") ? "" : $courseRow["SUC_COURSECODE"];
        $data["JUDGEMENT"][TEXT]        = $setField["JUDGEMENT"];
        //手続情報
        $data["PROCEDUREDIV"][TEXT]     = $setField["PROCEDUREDIV"];
        $data["PROCEDUREDATE"][TEXT]    = $setField["PROCEDUREDATE"];
        $data["ENTDIV"][TEXT]           = $setField["ENTDIV"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' AND";
        $where .= "       APPLICANTDIV  = '".$model->applicantdiv."' AND ";
        $where .= "       EXAMNO        = '".$model->examno."'";
        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
        $db->query($query);

        // 3.入学基礎詳細データの更新（入学コース）
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ENTEXAMYEAR   = '".$model->year."' AND ";
        $query .= "       APPLICANTDIV  = '".$model->applicantdiv."' AND ";
        $query .= "       EXAMNO        = '".$model->examno."' AND ";
        $query .= "       SEQ           = '007' ";
        $db->query($query);

        if ($setField["ENTDIV"] == "1") {
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]  = $model->year;
            $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
            $data["EXAMNO"][TEXT]       = $model->examno;
            $data["SEQ"][TEXT]          = "007";
            $data["REMARK1"][TEXT]      = $courseRow["ENTER_COURSECD"];
            $data["REMARK2"][TEXT]      = $courseRow["ENTER_MAJORCD"];
            $data["REMARK3"][TEXT]      = $courseRow["ENTER_COURSECODE"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            $db->query($query);
        }

        // 3.入学基礎詳細データの更新（学籍番号）
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ENTEXAMYEAR   = '".$model->year."' AND ";
        $query .= "       APPLICANTDIV  = '".$model->applicantdiv."' AND ";
        $query .= "       EXAMNO        = '".$model->examno."' AND ";
        $query .= "       SEQ           = '026' ";
        $db->query($query);

        if ($setField["ENTDIV"] == "1") {
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]  = $model->year;
            $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
            $data["EXAMNO"][TEXT]       = $model->examno;
            $data["SEQ"][TEXT]          = "026";
            $data["REMARK1"][TEXT]      = $setField["SCHREGNO"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>