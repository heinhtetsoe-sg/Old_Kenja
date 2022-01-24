<?php

require_once('for_php7.php');

class knjl090uQuery extends Query 
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

    //入試制度・入試区分を取得
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
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //メインデータ取得
    function get_edit_data($model) {
        $query  = "";
        $query .= " SELECT";
        $query .= "     T1.entexamyear,";
        $query .= "     T1.examno,";
        $query .= "     T1.name,";
        $query .= "     T1.name_kana,";
        $query .= "     T1.applicantdiv,";
        $query .= "     T1.TESTDIV,";
        $query .= "     T1.judgement,";
        $query .= "     T1.PROCEDUREDATE,";
        $query .= "     T1.procedurediv,";
        $query .= "     T1.entdiv,";
        $query .= "     T1.SLIDE_FLG,";
        $query .= "     B2.REMARK1 AS SCHREGNO ";
        $query .= " FROM";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN V_NAME_MST L1 ON L1.YEAR = T1.ENTEXAMYEAR AND L1.NAMECD1 = 'L004' AND L1.NAMECD2 = T1.TESTDIV ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT B2 ";
        $query .= "          ON B2.ENTEXAMYEAR      = T1.ENTEXAMYEAR ";
        $query .= "         AND B2.APPLICANTDIV     = T1.APPLICANTDIV ";
        $query .= "         AND B2.EXAMNO           = T1.EXAMNO ";
        $query .= "         AND B2.SEQ              = '026' ";
        $query .= " WHERE";
        $query .= "     T1.entexamyear = '".$model->year."' AND ";
        $query .= "     T1.APPLICANTDIV= '".$model->applicantdiv."' AND ";
        $query .= "     T1.TESTDIV = '".$model->testdiv."' AND ";
        $query .= "     VALUE(T1.JUDGEMENT,'0') != '5' AND "; //受験不可

        if ($model->cmd == "back1" || $model->cmd == "back2") {
            $query .= "      T1.examno < '".$model->examno."'";
            $query .= " ORDER BY T1.examno DESC ";
        } elseif ($model->cmd == "next1" || $model->cmd == "next2") {
            $query .= "      T1.examno > '".$model->examno."'";
            $query .= " ORDER BY T1.examno ASC ";
        } else {
            $query .= "      T1.examno = '".$model->examno."'";
        }

        return $query;
    }

    //受付データのテーブル
    function getRecept($model) {
        $query  = "";
        $query .= " SELECT";
        $query .= "     T1.* ";
        $query .= " FROM";
        $query .= "     entexam_recept_dat T1 ";
        $query .= " WHERE";
        $query .= "     T1.entexamyear  = '{$model->year}' AND ";
        $query .= "     T1.applicantdiv = '{$model->applicantdiv}' AND ";
        $query .= "     T1.testdiv      = '{$model->testdiv}' AND ";
        $query .= "     T1.examno       = '{$model->examno}' ";

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
        $query .= "         ON  T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
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

    //受験科目
    function getTestSubclasscd($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '{$model->year}' AND ";
        $query .= "     NAMECD1     = 'L009' AND ";
        $query .= "     NAME1 IS NOT NULL AND ";
        //満点マスタの設定が前提
        $query .= "     NAMECD2 IN (SELECT ";
        $query .= "                     TESTSUBCLASSCD ";
        $query .= "                 FROM ";
        $query .= "                     ENTEXAM_PERFECT_MST ";
        $query .= "                 WHERE ";
        $query .= "                     ENTEXAMYEAR     = '{$model->year}' AND ";
        $query .= "                     APPLICANTDIV    = '{$model->applicantdiv}' AND ";
        $query .= "                     TESTDIV         = '{$model->testdiv}' ";
        $query .= "                 ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

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

    //--- UPDATE
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //合格フラグ取得したい
        $judge = $db->getOne(knjl090uQuery::getJudge($model));

        //合格・入学コース取得したい
        $courseRow = $db->getRow(knjl090uQuery::getSucEntCourse($model), DB_FETCHMODE_ASSOC);

        // 1.受付データの更新
        $data = array();
        $data["JUDGEDIV"][TEXT]         = $model->judgement;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' AND";
        $where .= "       APPLICANTDIV  = '".$model->applicantdiv."' AND ";
        $where .= "       TESTDIV       = '".$model->testdiv."' AND ";
        $where .= "       EXAMNO        = '".$model->examno."'";
        $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
        $db->query($query);

        // 2.志願者基礎データの更新
        $data = array();
        $data["SUC_COURSECD"][TEXT]     = ($judge != "1") ? "" : $courseRow["SUC_COURSECD"];
        $data["SUC_MAJORCD"][TEXT]      = ($judge != "1") ? "" : $courseRow["SUC_MAJORCD"];
        $data["SUC_COURSECODE"][TEXT]   = ($judge != "1") ? "" : $courseRow["SUC_COURSECODE"];
        $data["JUDGEMENT"][TEXT]        = $model->judgement;
        //手続情報
        $data["PROCEDUREDIV"][TEXT]     = $model->field["PROCEDUREDIV"];
        $data["PROCEDUREDATE"][TEXT]    = str_replace("/", "-", $model->field["PROCEDUREDATE"]);
        $entdiv = ($model->field["PROCEDUREDIV"] == "1" && $model->field["ENTDIV"] == "") ? "1" : $model->field["ENTDIV"];
        $data["ENTDIV"][TEXT]           = $entdiv;
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

        if ($entdiv == "1") {
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
        if ($entdiv == "1") {
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]  = $model->year;
            $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
            $data["EXAMNO"][TEXT]       = $model->examno;
            $data["SEQ"][TEXT]          = "026";
            $data["REMARK1"][TEXT]      = $model->field["SCHREGNO"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $query  = " SELECT COUNT(*) FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
            $query .= " WHERE ENTEXAMYEAR   = '".$model->year."' AND ";
            $query .= "       APPLICANTDIV  = '".$model->applicantdiv."' AND ";
            $query .= "       EXAMNO        = '".$model->examno."' AND ";
            $query .= "       SEQ           = '026' ";
            $getCount = $db->getOne($query);
            if ($getCount == 0) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' AND ";
                $where .= "       APPLICANTDIV  = '".$model->applicantdiv."' AND ";
                $where .= "       EXAMNO        = '".$model->examno."' AND ";
                $where .= "       SEQ           = '026' ";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>