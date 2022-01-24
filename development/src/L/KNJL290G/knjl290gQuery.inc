<?php

require_once('for_php7.php');

class knjl290gQuery extends Query {
    //名称マスタより取得
    function get_name_cd($year, $cd, $sitei = "") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     ABBV1, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$cd."' ";
        if ($sitei) {
            $query .= "     AND NAMECD2 = '".$sitei."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタより取得
    function get_name_cdJ($year, $cd, $sitei) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     ABBV1, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$cd."' ";
        $query .= "     AND NAMECD2 = '".$sitei."' ";

        return $query;
    }

    //合格コース
    function getSucCourse($year, $applicantdiv, $testdiv) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.EXAMCOURSECD AS VALUE, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.EXAMCOURSECD || ':' || T1.EXAMCOURSE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '{$testdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //入学課程コース取得
    function getCourseMajorCoursecode($year, $applicantdiv, $testdiv) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.ENTER_COURSECD || T1.ENTER_MAJORCD || T1.ENTER_COURSECODE AS VALUE, ";
        $query .= "     T1.ENTER_COURSECD || T1.ENTER_MAJORCD || T1.ENTER_COURSECODE || ':' || L2.COURSECODENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST T1 ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST L1 ON L1.YEAR = T1.ENTEXAMYEAR ";
        $query .= "                                    AND L1.COURSECD = T1.ENTER_COURSECD ";
        $query .= "                                    AND L1.MAJORCD = T1.ENTER_MAJORCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST L2   ON L2.YEAR = T1.ENTEXAMYEAR ";
        $query .= "                                    AND L2.COURSECODE = T1.ENTER_COURSECODE ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '{$testdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        
        return $query;
    }

    //メインデータ取得
    function get_edit_data($model) {
        $query  = "";
        $query .= " SELECT";
        $query .= "     BASE.APPLICANTDIV,";
        $query .= "     BASE.TESTDIV,";
        $query .= "     BASE.EXAMNO,";
        $query .= "     BASE.NAME,";
        $query .= "     BASE.NAME_KANA,";
        $query .= "     BASE.BIRTHDAY,";
        $query .= "     BASE.SEX,";
        $query .= "     BASE.SHDIV,";
        $query .= "     L006.NAME1 AS SHDIVNAME,";
        $query .= "     BASE.DAI1_COURSECD || BASE.DAI1_MAJORCD || BASE.DAI1_COURSECODE AS EXAMCOURSE, ";
        $query .= "     BASE.DAI2_COURSECD || BASE.DAI2_MAJORCD || BASE.DAI2_COURSECODE AS EXAMCOURSE2, ";
        $query .= "     C1.EXAMCOURSE_NAME AS EXAMCOURSE_NAME, ";
        $query .= "     C2.EXAMCOURSE_NAME AS EXAMCOURSE_NAME2, ";
        $query .= "     BASE.SUC_COURSECD || BASE.SUC_MAJORCD || BASE.SUC_COURSECODE AS COURSEMAJOR,";
        $query .= "     E1.REMARK1 || E1.REMARK2 || E1.REMARK3 AS ENTER_COURSEMAJOR,";
        $query .= "     BASE.JUDGEMENT,";
        $query .= "     BASE.JUDGE_KIND,";
        $query .= "     L025.NAME1 AS JUDGE_KIND_NAME,";
        $query .= "     BASE.PROCEDUREDATE,";
        $query .= "     BASE.PROCEDUREDIV,";
        $query .= "     BASE.ENTDIV,";
        $query .= "     E2.REMARK2 ENTRYPAY_LOAN,";
        $query .= "     Z002.NAME1 AS SEXNAME ";
        $query .= " FROM";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     LEFT JOIN V_NAME_MST Z002 ";
        $query .= "                  ON Z002.YEAR = BASE.ENTEXAMYEAR AND Z002.NAMECD1 = 'Z002' AND Z002.NAMECD2 = BASE.SEX ";
        $query .= "     LEFT JOIN V_NAME_MST L025 ";
        $query .= "                  ON L025.YEAR = BASE.ENTEXAMYEAR AND L025.NAMECD1 = 'L025' AND L025.NAMECD2 = BASE.JUDGE_KIND ";
        $query .= "     LEFT JOIN V_NAME_MST L006 ";
        $query .= "                  ON L006.YEAR = BASE.ENTEXAMYEAR AND L006.NAMECD1 = 'L006' AND L006.NAMECD2 = BASE.SHDIV ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST C1 ";
        $query .= "                  ON  C1.ENTEXAMYEAR    = BASE.ENTEXAMYEAR ";
        $query .= "                  AND C1.APPLICANTDIV   = BASE.APPLICANTDIV ";
        $query .= "                  AND C1.TESTDIV        = BASE.TESTDIV ";
        $query .= "                  AND C1.COURSECD       = BASE.DAI1_COURSECD ";
        $query .= "                  AND C1.MAJORCD        = BASE.DAI1_MAJORCD ";
        $query .= "                  AND C1.EXAMCOURSECD   = BASE.DAI1_COURSECODE ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST C2 ";
        $query .= "                  ON  C2.ENTEXAMYEAR    = BASE.ENTEXAMYEAR ";
        $query .= "                  AND C2.APPLICANTDIV   = BASE.APPLICANTDIV ";
        $query .= "                  AND C2.TESTDIV        = BASE.TESTDIV ";
        $query .= "                  AND C2.COURSECD       = BASE.DAI2_COURSECD ";
        $query .= "                  AND C2.MAJORCD        = BASE.DAI2_MAJORCD ";
        $query .= "                  AND C2.EXAMCOURSECD   = BASE.DAI2_COURSECODE ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT E1 ";
        $query .= "                  ON  E1.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "                  AND E1.EXAMNO = BASE.EXAMNO ";
        $query .= "                  AND E1.SEQ = '007' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT E2 ";
        $query .= "                  ON  E2.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "                  AND E2.EXAMNO = BASE.EXAMNO ";
        $query .= "                  AND E2.SEQ = '021' ";
        $query .= " WHERE";
        $query .= "         BASE.ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND BASE.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND BASE.TESTDIV      = '".$model->field["TESTDIV"]."' ";

        if ($model->cmd == "back1" || $model->cmd == "back2") {
            $query .= "      AND BASE.EXAMNO < '".$model->examno."'";
            $query .= " ORDER BY BASE.EXAMNO DESC ";
        } elseif ($model->cmd == "next1" || $model->cmd == "next2") {
            $query .= "      AND BASE.EXAMNO > '".$model->examno."'";
            $query .= " ORDER BY BASE.EXAMNO ASC ";
        } else {
            $query .= "      AND BASE.EXAMNO = '".$model->examno."'";
        }

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
        $query .= "     AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND EXAMNO       = '".$model->examno."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    /************/
    /* 更新処理 */
    /************/
    //更新
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        // 志願者基礎データの更新
        $data = array();
        $data["SUC_COURSECD"][TEXT]         = $model->field["SUC_COURSECD"];
        $data["SUC_MAJORCD"][TEXT]          = $model->field["SUC_MAJORCD"];
        $data["SUC_COURSECODE"][TEXT]       = $model->field["SUC_COURSECODE"];
        $data["JUDGEMENT"][TEXT]            = $model->field["JUDGEMENT"];
        $data["PROCEDUREDIV"][TEXT]         = $model->field["PROCEDUREDIV"];
        $data["PROCEDUREDATE"][TEXT]        = str_replace("/", "-", $model->field["PROCEDUREDATE"]);
        $data["ENTDIV"][TEXT]               = $model->field["ENTDIV"];
        $data["registercd"][TEXT]           = STAFFCD;
        $data["updated"][FUNC]              = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $where .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $where .= "   AND EXAMNO       = '".$model->examno."' ";
        $query = Query::updateSQL($data, "entexam_applicantbase_dat", $where);
        $db->query($query);

        // 入学基礎詳細データの更新（入学コース）
        //cnt
        $getCount007 = $db->getOne(knjl290gQuery::getCountDetail($model, "007"));
        //where
        $getWhere007 = knjl290gQuery::getWhereDetail($model, "007");

        $data = array();
        $data["REMARK1"][TEXT]      = $model->field["ENTER_COURSECD"];
        $data["REMARK2"][TEXT]      = $model->field["ENTER_MAJORCD"];
        $data["REMARK3"][TEXT]      = $model->field["ENTER_COURSECODE"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        if ($getCount007 == 0) {
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $model->examno;
            $data["SEQ"][TEXT]              = "007";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $getWhere007);
        }
        $db->query($query);

        // 入学基礎詳細データの更新（入学支度金貸付）
        //cnt
        $getCount021 = $db->getOne(knjl290gQuery::getCountDetail($model, "021"));
        //where
        $getWhere021 = knjl290gQuery::getWhereDetail($model, "021");

        $data = array();
        $data["REMARK2"][TEXT]      = $model->field["ENTRYPAY_LOAN"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        if ($getCount021 == 0) {
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $model->examno;
            $data["SEQ"][TEXT]              = "021";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $getWhere021);
        }
        $db->query($query);


        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //カウントDetail
    function getCountDetail($model, $seq) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND EXAMNO          = '{$model->examno}' ";
        $query .= "     AND SEQ             = '{$seq}' ";

        return $query;
    }

    //WHERE 条件セット Detail
    function getWhereDetail($model, $seq) {
        $query  = " WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND EXAMNO          = '{$model->examno}' ";
        $query .= "     AND SEQ             = '{$seq}' ";

        return $query;
    }

}
?>