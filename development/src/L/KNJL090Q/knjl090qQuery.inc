<?php

require_once('for_php7.php');

class knjl090qQuery extends Query {
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
        $query .= "     YEAR    = '".$ObjYear ."' AND ";
        $query .= "     NAMECD1 IN ('".implode($namecd1, "','") ."') ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD1, ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //入試制度を取得
    function get_name_cd($year, $namecd1, $sitei = "") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     ABBV1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($sitei) {
            $query .= "     AND NAMECD2 = '".$sitei."' ";
        }
        if ($namecd1 == "L003") {
            $query .= " AND NAMESPARE3 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験科目取得
    function getTestSubclasscd($ObjYear) {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$ObjYear ."' AND ";
        $query .= "     NAMECD1 = 'L009' AND ";
        $query .= "     NAME2 IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }
    
    //受験科目取得(小学)
    function getTestSubclasscdP($ObjYear) {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME3 AS NAME2";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$ObjYear ."' AND ";
        $query .= "     NAMECD1 = 'L009' AND ";
        $query .= "     NAME3 IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //受験番号帯の受験区分取得
    function getKubun($model) {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     N1.NAME1 AS KUBUN_TESTDIV, ";
        $query .= "     L1.EXAMCOURSE_NAME AS KUBUN_EXAMCOURSE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_NO_DAT T1 ";
        if(SCHOOLKIND == "P") {
            $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'LP24' AND N1.NAMECD2 = T1.TESTDIV ";
        } else {
            $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'L024' AND N1.NAMECD2 = T1.TESTDIV ";
        }
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST L1 ";
        $query .= "          ON L1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND L1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND L1.TESTDIV      = T1.TESTDIV ";
        $query .= "         AND L1.COURSECD     = T1.COURSECD ";
        $query .= "         AND L1.MAJORCD      = T1.MAJORCD ";
        $query .= "         AND L1.EXAMCOURSECD = T1.EXAMCOURSECD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '".$model->year."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->applicantdiv."' AND ";
        $query .= "     '".$model->examno."' BETWEEN T1.EXAMNO_FROM AND T1.EXAMNO_TO ";

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
        $query .= "     T1.APPLICANTDIV,";
        $query .= "     T1.TESTDIV,";
        $query .= "     T1.JUDGEMENT,";
        $query .= "     T1.PROCEDUREDATE,";
        $query .= "     T1.PROCEDUREDIV,";
        $query .= "     T1.ENTDIV,";
        $query .= "     T2.NAME1 AS SEXNAME ";
        $query .= " FROM";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN V_NAME_MST T2 ";
        $query .= "          ON T2.YEAR     = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.NAMECD1  = 'Z002' ";
        $query .= "         AND T2.NAMECD2  = T1.SEX ";
        $query .= " WHERE";
        $query .= "     T1.ENTEXAMYEAR  = '".$model->year."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->applicantdiv."' AND ";

        if ($model->cmd == "back1" || $model->cmd == "back2") {
            $query .= "     T1.EXAMNO < '".$model->examno."'";
            $query .= " ORDER BY T1.EXAMNO DESC ";
        } elseif ($model->cmd == "next1" || $model->cmd == "next2") {
            $query .= "     T1.EXAMNO > '".$model->examno."'";
            $query .= " ORDER BY T1.EXAMNO ASC ";
        } else {
            $query .= "     T1.EXAMNO = '".$model->examno."'";
        }

        return $query;
    }

    //最大testdiv取得
    function getMaxtestdiv($model) {
        $query  = " SELECT ";
        $query .= "     MAX(TESTDIV) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->year."' AND ";
        $query .= "     APPLICANTDIV = '".$model->applicantdiv."' AND ";
        $query .= "     EXAMNO       = '".$model->examno."' ";

        return $query;
    }

    //受付データのテーブル
    function getRecept($model) {
        $query  = " SELECT";
        $query .= "     RECEPTNO,";
        $query .= "     EXAMNO,";
        $query .= "     TESTDIV,";
        $query .= "     TOTAL4,";
        $query .= "     AVARAGE4,";
        $query .= "     TOTAL_RANK4";
        $query .= " FROM";
        $query .= "     ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE";
        $query .= "     ENTEXAMYEAR     = '{$model->year}' AND ";
        $query .= "     APPLICANTDIV    = '{$model->applicantdiv}' AND ";
        $query .= "     TESTDIV         = '{$model->testdiv}' AND ";
        $query .= "     EXAMNO          = '{$model->examno}' ";

        return $query;
    }

    //得点データ取得
    function getScore($model) {
        $query  = " SELECT ";
        $query .= "     T2.TESTSUBCLASSCD, ";
        $query .= "     T2.ATTEND_FLG, ";
        $query .= "     T2.SCORE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_SCORE_DAT T2 ";
        $query .= "          ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.TESTDIV      = T1.TESTDIV ";
        $query .= "         AND T2.EXAM_TYPE    = T1.EXAM_TYPE ";
        $query .= "         AND T2.RECEPTNO     = T1.RECEPTNO ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '{$model->year}' AND ";
        $query .= "     T1.APPLICANTDIV = '{$model->applicantdiv}' AND ";
        $query .= "     T1.TESTDIV      = '{$model->testdiv}' AND ";
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
        $query .= "     ENTEXAMYEAR     = '".$model->year."' AND ";
        $query .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
        $query .= "     EXAMNO          = '".$model->examno."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //合格・入学コース
    function getSucEntCourse($model) {
        $query  = " SELECT ";
        $query .= "     B1.DAI1_COURSECD AS SUC_COURSECD, ";
        $query .= "     B1.DAI1_MAJORCD AS SUC_MAJORCD, ";
        $query .= "     B1.DAI1_COURSECODE AS SUC_COURSECODE, ";
        $query .= "     C1.ENTER_COURSECD, ";
        $query .= "     C1.ENTER_MAJORCD, ";
        $query .= "     C1.ENTER_COURSECODE ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST C1 ";
        $query .= "          ON C1.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "         AND C1.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "         AND C1.TESTDIV      = B1.TESTDIV ";
        $query .= "         AND C1.COURSECD     = B1.DAI1_COURSECD ";
        $query .= "         AND C1.MAJORCD      = B1.DAI1_MAJORCD ";
        $query .= "         AND C1.EXAMCOURSECD = B1.DAI1_COURSECODE ";
        $query .= " WHERE ";
        $query .= "     B1.ENTEXAMYEAR  = '{$model->year}' AND ";
        $query .= "     B1.APPLICANTDIV = '{$model->applicantdiv}' AND ";
        $query .= "     B1.EXAMNO       = '{$model->examno}' ";

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

    //--- UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //合格・入学コース取得
        $courseRow = $db->getRow(knjl090qQuery::getSucEntCourse($model), DB_FETCHMODE_ASSOC);

        //合否フラグ取得
        $judge = $db->getOne(knjl090qQuery::getJudge($model));

        //更新 -- ENTEXAM_APPLICANTBASE_DAT
        $data = array();
        //合否情報
        $data["SUC_COURSECD"][TEXT]         = ($judge != "1") ? "" : $courseRow["SUC_COURSECD"];
        $data["SUC_MAJORCD"][TEXT]          = ($judge != "1") ? "" : $courseRow["SUC_MAJORCD"];
        $data["SUC_COURSECODE"][TEXT]       = ($judge != "1") ? "" : $courseRow["SUC_COURSECODE"];
        $data["JUDGEMENT"][TEXT]            = $model->judgement;
        //手続情報
        $data["PROCEDUREDIV"][TEXT]         = $model->field["PROCEDUREDIV"];
        $data["PROCEDUREDATE"][TEXT]        = str_replace("/", "-", $model->field["PROCEDUREDATE"]);
        if ($model->field["PROCEDUREDIV"] == "1" && $model->field["ENTDIV"] == "") $model->field["ENTDIV"] = "1";
        $data["ENTDIV"][TEXT]               = $model->field["ENTDIV"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' AND";
        $where .= "       APPLICANTDIV  = '".$model->applicantdiv."' AND ";
        $where .= "       EXAMNO        = '".$model->examno."'";
        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
        $db->query($query);

        //更新 -- ENTEXAM_APPLICANTBASE_DETAIL_DAT(SEQ=007)
        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$model->year."' AND ";
        $query .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
        $query .= "     EXAMNO          = '".$model->examno."' AND ";
        $query .= "     SEQ             = '007' ";
        $db->query($query);

        if ($model->field["ENTDIV"] == "1") {
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

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>