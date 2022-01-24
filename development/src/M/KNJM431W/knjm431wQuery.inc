<?php

require_once('for_php7.php');
class knjm431wQuery extends Query
{

    //学籍基礎マスタより学籍番号と名前を取得
    public function getSchregno_name($schregno)
    {
        $query  =" SELECT ";
        $query .="      schregno, ";
        $query .="      name_show ";
        $query .=" FROM ";
        $query .="      schreg_base_mst ";
        $query .=" WHERE ";
        $query .="      schregno = '".$schregno."'";
        return $query;
    }

    public function getScoreData($model)
    {
        $query .= " SELECT ";
        $query .= "     SCORE.*, ";
        if (strlen($model->Properties["knjm431wPassScore"])) {
            $query .= "     VALUE(PF.PASS_SCORE, ".(intval($model->Properties["knjm431wPassScore"]) > 0 ? intval($model->Properties["knjm431wPassScore"]) : "NULL").") AS PASS_SCORE, ";
            if ($model->Properties["knjm431wUseGakkiHyouka"] == "1") {
                $query .= "     CASE WHEN SCORE.TESTKINDCD = '99' AND SCORE.TESTITEMCD = '00' AND SCORE.SCORE_DIV = '08' THEN ";
                $query .= "         CASE WHEN SCORE.VALUE IS NULL ";
                $query .= "              THEN '欠' ";
                $query .= "              WHEN SCORE.VALUE >= PF.PASS_SCORE ";
                $query .= "              THEN '合' ";
                $query .= "              WHEN SCORE.VALUE > (".(intval($model->Properties["knjm431wPassScore"]) > 0 ? intval($model->Properties["knjm431wPassScore"]) : "NULL").") ";
                $query .= "              THEN '合' ";
                $query .= "              ELSE '否' ";
                $query .= "         END ";
                $query .= "     ELSE ";
                $query .= "         CASE WHEN SCORE.SCORE IS NULL ";
                $query .= "              THEN '欠' ";
                $query .= "              WHEN SCORE.SCORE >= PF.PASS_SCORE ";
                $query .= "              THEN '合' ";
                $query .= "              WHEN SCORE.SCORE > (".(intval($model->Properties["knjm431wPassScore"]) > 0 ? intval($model->Properties["knjm431wPassScore"]) : "NULL").") ";
                $query .= "              THEN '合' ";
                $query .= "              ELSE '否' ";
                $query .= "         END ";
                $query .= "     END AS GOUHI, ";
            } else {
                $query .= "     CASE WHEN SCORE.SCORE IS NULL ";
                $query .= "          THEN '欠' ";
                $query .= "          WHEN SCORE.SCORE >= PF.PASS_SCORE ";
                $query .= "          THEN '合' ";
                $query .= "          WHEN SCORE.SCORE > (".(intval($model->Properties["knjm431wPassScore"]) > 0 ? intval($model->Properties["knjm431wPassScore"]) : "NULL").") ";
                $query .= "          THEN '合' ";
                $query .= "          ELSE '否' ";
                $query .= "     END AS GOUHI, ";
            }
        } else {
            $query .= "     CASE WHEN SCORE.VALUE IS NOT NULL ";
            $query .= "          THEN '合' ";
            $query .= "          ELSE '否' ";
            $query .= "     END AS GOUHI, ";
        }
        $query .= "     SUBCLASS.CLASSCD || '-' || SUBCLASS.SCHOOL_KIND || '-' || SUBCLASS.CURRICULUM_CD || '-' || SUBCLASS.SUBCLASSCD || ':' || SUBCLASS.SUBCLASSNAME AS SUBCLASSNAME, ";
        if ($model->Properties["knjm431wUseGakkiHyouka"] == "1") {
            $query .= "     CASE WHEN SCORE.TESTKINDCD = '99' AND SCORE.TESTITEMCD = '00' AND SCORE.SCORE_DIV = '08' THEN  ";
            $query .= "         '学期評価'  ";
            $query .= "     ELSE ";
            $query .= "         I1.SEMESTER || '-' || I1.TESTKINDCD || '-' || I1.TESTITEMCD || '-' || I1.SCORE_DIV || ':' || I1.TESTITEMNAME ";
            $query .= "     END AS TESTITEMNAME ";
        } else {
            $query .= "     I1.SEMESTER || '-' || I1.TESTKINDCD || '-' || I1.TESTITEMCD || '-' || I1.SCORE_DIV || ':' || I1.TESTITEMNAME AS TESTITEMNAME ";
        }
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_HIST_DAT SCORE ";
        $query .= "     INNER JOIN SUBCLASS_MST SUBCLASS ON SCORE.CLASSCD = SUBCLASS.CLASSCD ";
        $query .= "           AND SCORE.SCHOOL_KIND = SUBCLASS.SCHOOL_KIND ";
        $query .= "           AND SCORE.CURRICULUM_CD = SUBCLASS.CURRICULUM_CD ";
        $query .= "           AND SCORE.SUBCLASSCD = SUBCLASS.SUBCLASSCD ";
        $query .= "     LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV I1 ON SCORE.YEAR = I1.YEAR ";
        $query .= "           AND I1.SEMESTER || '-' || I1.TESTKINDCD || '-' || I1.TESTITEMCD || '-' || I1.SCORE_DIV = SCORE.SEMESTER || '-' || SCORE.TESTKINDCD || '-' || SCORE.TESTITEMCD || '-' || SCORE.SCORE_DIV ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = SCORE.SCHREGNO ";
        $query .= "           AND REGD.YEAR = SCORE.YEAR ";
        $query .= "           AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     LEFT JOIN PERFECT_RECORD_SDIV_DAT PF ON PF.YEAR = SCORE.YEAR ";
        $query .= "          AND PF.SEMESTER = I1.SEMESTER ";
        $query .= "          AND PF.TESTKINDCD = I1.TESTKINDCD ";
        $query .= "          AND PF.TESTITEMCD = I1.TESTITEMCD ";
        $query .= "          AND PF.SCORE_DIV = I1.SCORE_DIV ";
        $query .= "          AND PF.CLASSCD = SCORE.CLASSCD ";
        $query .= "          AND PF.SCHOOL_KIND = SCORE.SCHOOL_KIND ";
        $query .= "          AND PF.CURRICULUM_CD = SCORE.CURRICULUM_CD ";
        $query .= "          AND PF.SUBCLASSCD = SCORE.SUBCLASSCD ";
        $query .= "          AND PF.GRADE = CASE WHEN PF.DIV = '01' THEN '00' ELSE REGD.GRADE END ";
        $query .= "          AND PF.COURSECD || PF.MAJORCD || PF.COURSECODE = CASE WHEN PF.DIV IN ('01','02') THEN '00000000' ELSE REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE END ";
        $query .= " WHERE ";
        $query .= "     SCORE.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCORE.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND SCORE.SEMESTER = '{$model->sendField["SEMESTER"]}' ";
        $query .= "     AND SCORE.TESTKINDCD || SCORE.TESTITEMCD || SCORE.SCORE_DIV = '".$model->sendField["SEND_TESTTYPE"]."' ";
        $query .= "     AND SCORE.CLASSCD || '-' || SCORE.SCHOOL_KIND || '-' || SCORE.CURRICULUM_CD || '-' || SCORE.SUBCLASSCD = '".$model->sendField["SEND_SUBCLASS"]."' ";
        $query .= " ORDER BY ";
        $query .= "     SCORE.YEAR, ";
        $query .= "     SCORE.SEMESTER, ";
        $query .= "     SCORE.TESTKINDCD, ";
        $query .= "     SCORE.TESTITEMCD, ";
        $query .= "     SCORE.SCORE_DIV, ";
        $query .= "     SCORE.CLASSCD, ";
        $query .= "     SCORE.SCHOOL_KIND, ";
        $query .= "     SCORE.CURRICULUM_CD, ";
        $query .= "     SCORE.SUBCLASSCD, ";
        $query .= "     SCORE.SEQ ";

        return $query;
    }

    public function getRecordScoreHist($model)
    {
        $query .= " SELECT ";
        $query .= "     SCORE.*, ";
        $query .= "     DETAIL.REMARK1 AS COMMENT ";
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_HIST_DAT SCORE ";
        $query .= "     LEFT JOIN RECORD_SCORE_HIST_DETAIL_DAT DETAIL ";
        $query .= "      ON DETAIL.YEAR          = SCORE.YEAR ";
        $query .= "     AND DETAIL.SEMESTER      = SCORE.SEMESTER ";
        $query .= "     AND DETAIL.TESTKINDCD    = SCORE.TESTKINDCD ";
        $query .= "     AND DETAIL.TESTITEMCD    = SCORE.TESTITEMCD ";
        $query .= "     AND DETAIL.SCORE_DIV     = SCORE.SCORE_DIV ";
        $query .= "     AND DETAIL.CLASSCD       = SCORE.CLASSCD ";
        $query .= "     AND DETAIL.SCHOOL_KIND   = SCORE.SCHOOL_KIND ";
        $query .= "     AND DETAIL.CURRICULUM_CD = SCORE.CURRICULUM_CD ";
        $query .= "     AND DETAIL.SUBCLASSCD    = SCORE.SUBCLASSCD ";
        $query .= "     AND DETAIL.SCHREGNO      = SCORE.SCHREGNO ";
        $query .= "     AND DETAIL.SEQ           = SCORE.SEQ ";
        $query .= "     AND DETAIL.DSEQ          = '002' ";
        $query .= " WHERE ";
        $query .= "     SCORE.YEAR = '{$model->sendField["YEAR"]}' ";
        $query .= "     AND SCORE.SEMESTER = '{$model->sendField["SEMESTER"]}' ";
        $query .= "     AND SCORE.TESTKINDCD = '{$model->sendField["TESTKINDCD"]}' ";
        $query .= "     AND SCORE.TESTITEMCD = '{$model->sendField["TESTITEMCD"]}' ";
        $query .= "     AND SCORE.SCORE_DIV = '{$model->sendField["SCORE_DIV"]}' ";
        $query .= "     AND SCORE.CLASSCD = '{$model->sendField["CLASSCD"]}' ";
        $query .= "     AND SCORE.SCHOOL_KIND = '{$model->sendField["SCHOOL_KIND"]}' ";
        $query .= "     AND SCORE.CURRICULUM_CD = '{$model->sendField["CURRICULUM_CD"]}' ";
        $query .= "     AND SCORE.SUBCLASSCD = '{$model->sendField["SUBCLASSCD"]}' ";
        $query .= "     AND SCORE.SCHREGNO = '{$model->schregno}' ";
        if (strlen($model->sendField["SEQ"]) == 0) {
            $query .= "     AND SCORE.SEQ = -1 ";
        } else {
            $query .= "     AND SCORE.SEQ = {$model->sendField["SEQ"]} ";
        }
        $query .= " ORDER BY ";
        $query .= "     SCORE.YEAR, ";
        $query .= "     SCORE.SEMESTER, ";
        $query .= "     SCORE.TESTKINDCD, ";
        $query .= "     SCORE.TESTITEMCD, ";
        $query .= "     SCORE.SCORE_DIV, ";
        $query .= "     SCORE.CLASSCD, ";
        $query .= "     SCORE.SCHOOL_KIND, ";
        $query .= "     SCORE.CURRICULUM_CD, ";
        $query .= "     SCORE.SUBCLASSCD, ";
        $query .= "     SCORE.SEQ ";
        return $query;
    }

    public function getSubclassCd($model)
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ':' || I1.SUBCLASSNAME AS LABEL, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     REP_PRESENT_DAT T1 ";
        $query .= "     INNER JOIN SUBCLASS_MST I1 ON T1.CLASSCD = I1.CLASSCD ";
        $query .= "           AND T1.SCHOOL_KIND = I1.SCHOOL_KIND ";
        $query .= "           AND T1.CURRICULUM_CD = I1.CURRICULUM_CD ";
        $query .= "           AND T1.SUBCLASSCD = I1.SUBCLASSCD ";
        $query .= "     INNER JOIN SUBCLASS_STD_PASS_SDIV_DAT PASS_T ON T1.YEAR = PASS_T.YEAR ";
        $query .= "           AND T1.CLASSCD = PASS_T.CLASSCD ";
        $query .= "           AND T1.SCHOOL_KIND = PASS_T.SCHOOL_KIND ";
        $query .= "           AND T1.CURRICULUM_CD = PASS_T.CURRICULUM_CD ";
        $query .= "           AND T1.SUBCLASSCD = PASS_T.SUBCLASSCD ";
        $query .= "           AND T1.SCHREGNO  = PASS_T.SCHREGNO ";
        $query .= "           AND VALUE(PASS_T.SEM_PASS_FLG, '0') = '1' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    public function getTest($model, $subclassCd)
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     N1.NAMESPARE1 || ':' || I1.TESTITEMNAME AS LABEL, ";
        $query .= "     N1.NAMESPARE1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     REP_STANDARDDATE_DAT T1 ";
        $query .= "     INNER JOIN NAME_MST N1 ON N1.NAMECD1 = 'M002' ";
        $query .= "           AND N1.NAMECD2 = T1.REPORTDIV ";
        $query .= "           AND N1.NAMESPARE1 IS NOT NULL ";
        $query .= "     INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV I1 ON T1.YEAR = I1.YEAR ";
        $query .= "           AND I1.SEMESTER || '-' || I1.TESTKINDCD || '-' || I1.TESTITEMCD || '-' || I1.SCORE_DIV = N1.NAMESPARE1 ";
        $query .= "     INNER JOIN SUBCLASS_STD_PASS_SDIV_DAT PASS_T ON T1.YEAR = PASS_T.YEAR ";
        $query .= "           AND T1.CLASSCD = PASS_T.CLASSCD ";
        $query .= "           AND T1.SCHOOL_KIND = PASS_T.SCHOOL_KIND ";
        $query .= "           AND T1.CURRICULUM_CD = PASS_T.CURRICULUM_CD ";
        $query .= "           AND T1.SUBCLASSCD = PASS_T.SUBCLASSCD ";
        $query .= "           AND PASS_T.SEMESTER || '-' || PASS_T.TESTKINDCD || '-' || PASS_T.TESTITEMCD || '-' || PASS_T.SCORE_DIV = N1.NAMESPARE1 ";
        $query .= "           AND PASS_T.SCHREGNO = '{$model->schregno}' ";
        $query .= "           AND VALUE(PASS_T.SEM_PASS_FLG, '0') = '1' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '{$subclassCd}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    public function getRepPass($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SEM_PASS_FLG ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_STD_PASS_SDIV_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR              = '".CTRL_YEAR."' ";
        $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$model->sendField["CLASSCD"]."-".$model->sendField["SCHOOL_KIND"]."-".$model->sendField["CURRICULUM_CD"]."-".$model->sendField["SUBCLASSCD"]."' ";
        $query .= "     AND T1.SEMESTER || '-' || T1.TESTKINDCD || '-' || T1.TESTITEMCD || '-' || T1.SCORE_DIV  = '".$model->sendField["SEMESTER"]."-".$model->sendField["TESTKINDCD"]."-".$model->sendField["TESTITEMCD"]."-".$model->sendField["SCORE_DIV"]."' ";
        $query .= "     AND T1.SCHREGNO  = '{$model->schregno}' ";

        return $query;
    }

    public function getScoreInfo($model)
    {
        list($semester, $testKind, $testItem, $scoreDiv) = str_split("-", $model->sendField["SEMESTER"]."-".$model->sendField["TESTKINDCD"]."-".$model->sendField["TESTITEMCD"]."-".$model->sendField["SCORE_DIV"]);
        $query .= " SELECT ";
        $query .= "     CHAIR.CHAIRCD, ";
        $query .= "     PERFECT.PASS_SCORE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN PERFECT_RECORD_SDIV_DAT PERFECT ON PERFECT.YEAR = REGD.YEAR ";
        $query .= "          AND PERFECT.SEMESTER || '-' || PERFECT.TESTKINDCD || '-' || PERFECT.TESTITEMCD || '-' || PERFECT.SCORE_DIV = '".$model->sendField["SEMESTER"]."-".$model->sendField["TESTKINDCD"]."-".$model->sendField["TESTITEMCD"]."-".$model->sendField["SCORE_DIV"]."' ";
        $query .= "          AND PERFECT.CLASSCD || '-' || PERFECT.SCHOOL_KIND || '-' || PERFECT.CURRICULUM_CD || '-' || PERFECT.SUBCLASSCD = '".$model->sendField["CLASSCD"]."-".$model->sendField["SCHOOL_KIND"]."-".$model->sendField["CURRICULUM_CD"]."-".$model->sendField["SUBCLASSCD"]."' ";
        $query .= "          AND PERFECT.GRADE = CASE WHEN PERFECT.DIV = '01' THEN '00' ELSE REGD.GRADE END ";
        $query .= "          AND PERFECT.COURSECD || PERFECT.MAJORCD || PERFECT.COURSECODE = CASE WHEN PERFECT.DIV IN ('01','02') THEN '00000000' ELSE REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE END ";
        $query .= "     LEFT JOIN CHAIR_DAT CHAIR ON CHAIR.YEAR = REGD.YEAR ";
        $query .= "          AND CHAIR.SEMESTER = '{$semester}' ";
        $query .= "          AND CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || '-' || CHAIR.CURRICULUM_CD || '-' || CHAIR.SUBCLASSCD = '".$model->sendField["CLASSCD"]."-".$model->sendField["SCHOOL_KIND"]."-".$model->sendField["CURRICULUM_CD"]."-".$model->sendField["SUBCLASSCD"]."' ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND REGD.SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    public function getMaxSeq($model)
    {
        $query  = " SELECT ";
        $query .= "     VALUE(MAX(SEQ), 0) + 1 AS SEQ ";
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_HIST_DAT SCORE ";
        $query .= " WHERE ";
        $query .= "     SCORE.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCORE.SEMESTER || '-' || SCORE.TESTKINDCD || '-' || SCORE.TESTITEMCD || '-' || SCORE.SCORE_DIV = '".$model->sendField["SEMESTER"]."-".$model->sendField["TESTKINDCD"]."-".$model->sendField["TESTITEMCD"]."-".$model->sendField["SCORE_DIV"]."' ";
        $query .= "     AND SCORE.CLASSCD || '-' || SCORE.SCHOOL_KIND || '-' || SCORE.CURRICULUM_CD || '-' || SCORE.SUBCLASSCD = '".$model->sendField["CLASSCD"]."-".$model->sendField["SCHOOL_KIND"]."-".$model->sendField["CURRICULUM_CD"]."-".$model->sendField["SUBCLASSCD"]."' ";
        $query .= "     AND SCORE.SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //INSERT
    public function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $query = knjm431wQuery::getScoreInfo($model);
        $scoreInfo = $db->getRow($query, DB_FETCHMODE_ASSOC);

        list($semester, $testKind, $testItem, $scoreDiv) = str_split("-", $model->sendField["SEMESTER"]."-".$model->sendField["TESTKINDCD"]."-".$model->sendField["TESTITEMCD"]."-".$model->sendField["SCORE_DIV"]);
        list($classCd, $schoolKind, $curriculum, $subclassCd) = str_split("-", $model->sendField["CLASSCD"]."-".$model->sendField["SCHOOL_KIND"]."-".$model->sendField["CURRICULUM_CD"]."-".$model->sendField["SUBCLASSCD"]);
        $query = knjm431wQuery::getMaxSeq($model);
        $setSeq = $db->getOne($query);
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEMESTER"][TEXT]         = $semester;
        $data["TESTKINDCD"][TEXT]       = $testKind;
        $data["TESTITEMCD"][TEXT]       = $testItem;
        $data["SCORE_DIV"][TEXT]        = $scoreDiv;
        $data["CLASSCD"][TEXT]          = $classCd;
        $data["SCHOOL_KIND"][TEXT]      = $schoolKind;
        $data["CURRICULUM_CD"][TEXT]    = $curriculum;
        $data["SUBCLASSCD"][TEXT]       = $subclassCd;
        $data["SCHREGNO"][TEXT]         = $model->schregno;
        $data["SEQ"][NUMBER]            = $setSeq;
        $data["TEST_DATE"][DATE]        = $model->field["TEST_DATE"];
        $data["CHAIRCD"][TEXT]          = $scoreInfo["CHAIRCD"];
        if ($model->Properties["knjm431wUseGakkiHyouka"] == "1" && $testKind == '99' && $testItem == '00' && $scoreDiv == '08') {
            $data["VALUE"][NUMBER]          = $model->field["SCORE"];
        } else {
            $data["SCORE"][NUMBER]          = $model->field["SCORE"];
            if ($setSeq == 1) {
                $setValue = $scoreInfo["PASS_SCORE"] > $model->field["SCORE"] ? "" : $model->field["SCORE"];
            } else {
                $setValue = $scoreInfo["PASS_SCORE"] > $model->field["SCORE"] ? "" : $scoreInfo["PASS_SCORE"];
            }
            $data["VALUE"][TEXT]            = $setValue;
        }
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "RECORD_SCORE_HIST_DAT");

        $db->query($query);
        Query::dbCheckIn($db);
        knjm431wQuery::getInsertQuery2($model, $setSeq);
        return;
    }

    public function &getInsertQuery2($model, $seq)
    {
        $db = Query::dbCheckOut();
        $query = knjm431wQuery::getScoreInfo($model);
        $scoreInfo = $db->getRow($query, DB_FETCHMODE_ASSOC);

        list($semester, $testKind, $testItem, $scoreDiv) = str_split("-", $model->sendField["SEMESTER"]."-".$model->sendField["TESTKINDCD"]."-".$model->sendField["TESTITEMCD"]."-".$model->sendField["SCORE_DIV"]);
        list($classCd, $schoolKind, $curriculum, $subclassCd) = str_split("-", $model->sendField["CLASSCD"]."-".$model->sendField["SCHOOL_KIND"]."-".$model->sendField["CURRICULUM_CD"]."-".$model->sendField["SUBCLASSCD"]);

        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEMESTER"][TEXT]         = $semester;
        $data["TESTKINDCD"][TEXT]       = $testKind;
        $data["TESTITEMCD"][TEXT]       = $testItem;
        $data["SCORE_DIV"][TEXT]        = $scoreDiv;
        $data["CLASSCD"][TEXT]          = $classCd;
        $data["SCHOOL_KIND"][TEXT]      = $schoolKind;
        $data["CURRICULUM_CD"][TEXT]    = $curriculum;
        $data["SUBCLASSCD"][TEXT]       = $subclassCd;
        $data["SCHREGNO"][TEXT]         = $model->schregno;

        $data["SEQ"][NUMBER]            = $seq;
        $data["DSEQ"][TEXT]            = "002";

        $data["REMARK1"][TEXT]            = $model->field["COMMENT"];

        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "RECORD_SCORE_HIST_DETAIL_DAT");

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }


    //UPDATE
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $query = knjm431wQuery::getScoreInfo($model);
        $scoreInfo = $db->getRow($query, DB_FETCHMODE_ASSOC);

        list($semester, $testKind, $testItem, $scoreDiv) = str_split("-", $model->sendField["SEMESTER"]."-".$model->sendField["TESTKINDCD"]."-".$model->sendField["TESTITEMCD"]."-".$model->sendField["SCORE_DIV"]);
        list($classCd, $schoolKind, $curriculum, $subclassCd) = str_split("-", $model->sendField["CLASSCD"]."-".$model->sendField["SCHOOL_KIND"]."-".$model->sendField["CURRICULUM_CD"]."-".$model->sendField["SUBCLASSCD"]);

        $data["TEST_DATE"][DATE]        = $model->field["TEST_DATE"];
        $data["CHAIRCD"][TEXT]          = $scoreInfo["CHAIRCD"];
        if ($model->Properties["knjm431wUseGakkiHyouka"] == "1" && $testKind == '99' && $testItem == '00' && $scoreDiv == '08') {
            $data["VALUE"][NUMBER]          = $model->field["SCORE"];
        } else {
            $setValue = $scoreInfo["PASS_SCORE"] > $model->field["SCORE"] ? "" : $scoreInfo["PASS_SCORE"];
            $data["SCORE"][NUMBER]          = $model->field["SCORE"];
            $data["VALUE"][TEXT]            = $setValue;
        }

        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where  = " WHERE YEAR        = '" .CTRL_YEAR. "'";
        $where .= "     AND SEMESTER || '-' || TESTKINDCD || '-' || TESTITEMCD || '-' || SCORE_DIV = '".$model->sendField["SEMESTER"]."-".$model->sendField["TESTKINDCD"]."-".$model->sendField["TESTITEMCD"]."-".$model->sendField["SCORE_DIV"]."' ";
        $where .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->sendField["CLASSCD"]."-".$model->sendField["SCHOOL_KIND"]."-".$model->sendField["CURRICULUM_CD"]."-".$model->sendField["SUBCLASSCD"]."' ";
        $where .= "     AND SCHREGNO = '{$model->schregno}' ";
        $where .= "     AND SEQ = {$model->sendField["SEQ"]} ";

        $query = Query::updateSQL($data, "RECORD_SCORE_HIST_DAT", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        knjm431wQuery::getUpdateQuery2($model);
        return;
    }

    public function &getUpdateQuery2($model)
    {
        $db = Query::dbCheckOut();
        $query = knjm431wQuery::getScoreInfo($model);
        $scoreInfo = $db->getRow($query, DB_FETCHMODE_ASSOC);

        list($semester, $testKind, $testItem, $scoreDiv) = str_split("-", $model->sendField["SEMESTER"]."-".$model->sendField["TESTKINDCD"]."-".$model->sendField["TESTITEMCD"]."-".$model->sendField["SCORE_DIV"]);
        list($classCd, $schoolKind, $curriculum, $subclassCd) = str_split("-", $model->sendField["CLASSCD"]."-".$model->sendField["SCHOOL_KIND"]."-".$model->sendField["CURRICULUM_CD"]."-".$model->sendField["SUBCLASSCD"]);

        $setValue = $scoreInfo["PASS_SCORE"] > $model->field["SCORE"] ? "" : $scoreInfo["PASS_SCORE"];

        $data["REMARK1"][TEXT]          = $model->field["COMMENT"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where  = " WHERE YEAR        = '" .CTRL_YEAR. "'";
        $where .= "     AND SEMESTER || '-' || TESTKINDCD || '-' || TESTITEMCD || '-' || SCORE_DIV = '".$model->sendField["SEMESTER"]."-".$model->sendField["TESTKINDCD"]."-".$model->sendField["TESTITEMCD"]."-".$model->sendField["SCORE_DIV"]."' ";
        $where .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->sendField["CLASSCD"]."-".$model->sendField["SCHOOL_KIND"]."-".$model->sendField["CURRICULUM_CD"]."-".$model->sendField["SUBCLASSCD"]."' ";
        $where .= "     AND SCHREGNO = '{$model->schregno}' ";
        $where .= "     AND SEQ = {$model->sendField["SEQ"]} ";
        $where .= "     AND DSEQ = '002' ";

        $checkSql = " SELECT COUNT(*) FROM RECORD_SCORE_HIST_DETAIL_DAT ".$where;
        if ($db->getOne($checkSql) > 0) {
            $query = Query::updateSQL($data, "RECORD_SCORE_HIST_DETAIL_DAT", $where);
        } else {
            $data["YEAR"][TEXT] = CTRL_YEAR;
            $data["SEMESTER"][TEXT]      = $model->sendField["SEMESTER"];
            $data["TESTKINDCD"][TEXT]    = $model->sendField["TESTKINDCD"];
            $data["TESTITEMCD"][TEXT]    = $model->sendField["TESTITEMCD"];
            $data["SCORE_DIV"][TEXT]     = $model->sendField["SCORE_DIV"];
            $data["CLASSCD"][TEXT]       = $model->sendField["CLASSCD"];
            $data["SCHOOL_KIND"][TEXT]   = $model->sendField["SCHOOL_KIND"];
            $data["CURRICULUM_CD"][TEXT] = $model->sendField["CURRICULUM_CD"];
            $data["SUBCLASSCD"][TEXT]    = $model->sendField["SUBCLASSCD"];
            $data["SCHREGNO"][TEXT]      = $model->schregno;
            $data["SEQ"][NUMBER]         = $model->sendField["SEQ"];
            $data["DSEQ"][TEXT]          = "002";
            $query = Query::insertSQL($data, "RECORD_SCORE_HIST_DETAIL_DAT");
        }



        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    public function &getDeleteQuery($model)
    {
        knjm431wQuery::getDeleteQuery2($model);

        $db = Query::dbCheckOut();

        $query  = " DELETE FROM RECORD_SCORE_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .CTRL_YEAR. "'";
        $query .= "     AND SEMESTER || '-' || TESTKINDCD || '-' || TESTITEMCD || '-' || SCORE_DIV = '".$model->sendField["SEMESTER"]."-".$model->sendField["TESTKINDCD"]."-".$model->sendField["TESTITEMCD"]."-".$model->sendField["SCORE_DIV"]."' ";
        $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->sendField["CLASSCD"]."-".$model->sendField["SCHOOL_KIND"]."-".$model->sendField["CURRICULUM_CD"]."-".$model->sendField["SUBCLASSCD"]."' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND SEQ = {$model->sendField["SEQ"]} ";

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    public function &getDeleteQuery2($model)
    {

        $db = Query::dbCheckOut();

        $query  = " DELETE FROM RECORD_SCORE_HIST_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .CTRL_YEAR. "'";
        $query .= "     AND SEMESTER || '-' || TESTKINDCD || '-' || TESTITEMCD || '-' || SCORE_DIV = '".$model->sendField["SEMESTER"]."-".$model->sendField["TESTKINDCD"]."-".$model->sendField["TESTITEMCD"]."-".$model->sendField["SCORE_DIV"]."' ";
        $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->sendField["CLASSCD"]."-".$model->sendField["SCHOOL_KIND"]."-".$model->sendField["CURRICULUM_CD"]."-".$model->sendField["SUBCLASSCD"]."' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND SEQ = {$model->sendField["SEQ"]} ";
        $query .= "     AND DSEQ = '002' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
}
