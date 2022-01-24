<?php
class knjm439wQuery extends Query {

    //学籍基礎マスタより学籍番号と名前を取得
    function getSchregno_name($schregno) {
        $query  =" SELECT ";
        $query .="      schregno, ";
        $query .="      name_show ";
        $query .=" FROM ";
        $query .="      schreg_base_mst ";
        $query .=" WHERE ";
        $query .="      schregno = '".$schregno."'";
        return $query;
    }

    function getAttendData($model, $seq = "") {
        $query .= " SELECT ";
        $query .= "     ATT.* ";
        $query .= "   , I1.SUBCLASSNAME ";
        $query .= "   , MST.TESTITEMNAME ";
        $query .= " FROM ";
        $query .= "     TEST_ATTEND_SDIV_HIST_DAT ATT ";
        $query .= "     INNER JOIN SUBCLASS_MST I1 ON ATT.CLASSCD = I1.CLASSCD ";
        $query .= "           AND ATT.SCHOOL_KIND = I1.SCHOOL_KIND ";
        $query .= "           AND ATT.CURRICULUM_CD = I1.CURRICULUM_CD ";
        $query .= "           AND ATT.SUBCLASSCD = I1.SUBCLASSCD ";
        $query .= "     INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV MST ON ATT.YEAR = MST.YEAR ";
        $query .= "           AND MST.SEMESTER = ATT.SEMESTER ";
        $query .= "           AND MST.TESTKINDCD = ATT.TESTKINDCD ";
        $query .= "           AND MST.TESTITEMCD = ATT.TESTITEMCD ";
        $query .= "           AND MST.SCORE_DIV = ATT.SCORE_DIV ";
        $query .= " WHERE ";
        $query .= "     ATT.YEAR = '{$model->sendField["YEAR"]}' ";
        $query .= "     AND ATT.SEMESTER = '{$model->sendField["SEMESTER"]}' ";
        $query .= "     AND ATT.TESTKINDCD = '{$model->sendField["TESTKINDCD"]}' ";
        $query .= "     AND ATT.TESTITEMCD = '{$model->sendField["TESTITEMCD"]}' ";
        $query .= "     AND ATT.SCORE_DIV = '{$model->sendField["SCORE_DIV"]}' ";
        $query .= "     AND ATT.CLASSCD = '{$model->sendField["CLASSCD"]}' ";
        $query .= "     AND ATT.SCHOOL_KIND = '{$model->sendField["SCHOOL_KIND"]}' ";
        $query .= "     AND ATT.CURRICULUM_CD = '{$model->sendField["CURRICULUM_CD"]}' ";
        $query .= "     AND ATT.SUBCLASSCD = '{$model->sendField["SUBCLASSCD"]}' ";
        $query .= "     AND ATT.SCHREGNO = '{$model->schregno}' ";
        if (strlen($seq)) {
            $query .= "     AND ATT.SEQ = ".$seq." ";
        }
        $query .= " ORDER BY ";
        $query .= "     ATT.SEQ ";
        return $query;
    }

    function getSubclassCd($model) {
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

    function getTest($model, $subclassCd) {
        $query .= " SELECT DISTINCT ";
        $query .= "     N1.NAMESPARE1 || ':' || I1.TESTITEMNAME AS LABEL, ";
        $query .= "     N1.NAMESPARE1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     REP_STANDARDDATE_DAT T1 ";
        $query .= "     INNER JOIN NAME_MST N1 ON N1.NAMECD1 = 'M002' ";
        $query .= "           AND N1.NAMECD2 = T1.REPORTDIV ";
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

    function getRepPass($model) {
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

    function getMaxSeq($model) {
        $query  = " SELECT ";
        $query .= "     VALUE(MAX(SEQ), 0) + 1 AS SEQ ";
        $query .= " FROM ";
        $query .= "     TEST_ATTEND_SDIV_HIST_DAT ATT ";
        $query .= " WHERE ";
        $query .= "     ATT.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND ATT.SEMESTER || '-' || ATT.TESTKINDCD || '-' || ATT.TESTITEMCD || '-' || ATT.SCORE_DIV = '".$model->sendField["SEMESTER"]."-".$model->sendField["TESTKINDCD"]."-".$model->sendField["TESTITEMCD"]."-".$model->sendField["SCORE_DIV"]."' ";
        $query .= "     AND ATT.CLASSCD || '-' || ATT.SCHOOL_KIND || '-' || ATT.CURRICULUM_CD || '-' || ATT.SUBCLASSCD = '".$model->sendField["CLASSCD"]."-".$model->sendField["SCHOOL_KIND"]."-".$model->sendField["CURRICULUM_CD"]."-".$model->sendField["SUBCLASSCD"]."' ";
        $query .= "     AND ATT.SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($model, $setSeq = "")
    {
        $db = Query::dbCheckOut();

        list($semester, $testKind, $testItem, $scoreDiv) = split("-", $model->sendField["SEMESTER"]."-".$model->sendField["TESTKINDCD"]."-".$model->sendField["TESTITEMCD"]."-".$model->sendField["SCORE_DIV"]);
        list($classCd, $schoolKind, $curriculum, $subclassCd) = split("-", $model->sendField["CLASSCD"]."-".$model->sendField["SCHOOL_KIND"]."-".$model->sendField["CURRICULUM_CD"]."-".$model->sendField["SUBCLASSCD"]);
        if ($setSeq == "") {
            $query = knjm439wQuery::getMaxSeq($model);
            $setSeq = $db->getOne($query);
        }
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
        $data["ATTEND"][TEXT]           = ($model->field["ATTEND"] == "1") ? "1" : "";
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "TEST_ATTEND_SDIV_HIST_DAT");

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();

        list($semester, $testKind, $testItem, $scoreDiv) = split("-", $model->sendField["SEMESTER"]."-".$model->sendField["TESTKINDCD"]."-".$model->sendField["TESTITEMCD"]."-".$model->sendField["SCORE_DIV"]);
        list($classCd, $schoolKind, $curriculum, $subclassCd) = split("-", $model->sendField["CLASSCD"]."-".$model->sendField["SCHOOL_KIND"]."-".$model->sendField["CURRICULUM_CD"]."-".$model->sendField["SUBCLASSCD"]);

        $data["TEST_DATE"][DATE]        = $model->field["TEST_DATE"];
        $data["ATTEND"][TEXT]           = ($model->field["ATTEND"] == "1") ? "1" : "";
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where  = " WHERE YEAR        = '" .CTRL_YEAR. "'";
        $where .= "     AND SEMESTER || '-' || TESTKINDCD || '-' || TESTITEMCD || '-' || SCORE_DIV = '".$model->sendField["SEMESTER"]."-".$model->sendField["TESTKINDCD"]."-".$model->sendField["TESTITEMCD"]."-".$model->sendField["SCORE_DIV"]."' ";
        $where .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->sendField["CLASSCD"]."-".$model->sendField["SCHOOL_KIND"]."-".$model->sendField["CURRICULUM_CD"]."-".$model->sendField["SUBCLASSCD"]."' ";
        $where .= "     AND SCHREGNO = '{$model->schregno}' ";
        $where .= "     AND SEQ = {$model->sendField["SEQ"]} ";

        $query = Query::updateSQL($data, "TEST_ATTEND_SDIV_HIST_DAT", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model) {

        $db = Query::dbCheckOut();

        $query  = " DELETE FROM TEST_ATTEND_SDIV_HIST_DAT ";
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

}
?>
