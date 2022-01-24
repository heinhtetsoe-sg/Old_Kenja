<?php

require_once('for_php7.php');

class knjm433wQuery extends Query {

    //年度
    function getYear($model) {
        $query  = " SELECT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";
        return $query;
    }

    //更新処理
    function getUpdateQuery($model) {
        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     RECORD_SCORE_HIST_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'X' ";
        $query .= "         FROM ";
        $query .= "             RECORD_SCORE_HIST_DETAIL_DAT E1 ";
        $query .= "         WHERE ";
        $query .= "             E1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "             AND E1.DSEQ = '001' ";
        $query .= "             AND E1.REMARK1 = '1' ";
        $query .= "             AND E1.YEAR = T1.YEAR ";
        $query .= "             AND E1.SEMESTER = T1.SEMESTER ";
        $query .= "             AND E1.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "             AND E1.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "             AND E1.SCORE_DIV = T1.SCORE_DIV ";
        $query .= "             AND E1.CLASSCD = T1.CLASSCD ";
        $query .= "             AND E1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "             AND E1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "             AND E1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "             AND E1.SCHREGNO = T1.SCHREGNO ";
        $query .= "             AND E1.SEQ = T1.SEQ ";
        $query .= "     ) ";
        $db->query($query);

        $query  = " DELETE FROM ";
        $query .= "     RECORD_SCORE_HIST_DETAIL_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->field["YEAR"]}' ";
        $db->query($query);

        $query  = " DELETE FROM ";
        $query .= "     REP_PRESENT_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'X' ";
        $query .= "         FROM ";
        $query .= "             REP_PRESENT_DETAIL_DAT E1 ";
        $query .= "         WHERE ";
        $query .= "             E1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "             AND E1.SEQ = '001' ";
        $query .= "             AND E1.REMARK1 = '1' ";
        $query .= "             AND E1.YEAR = T1.YEAR ";
        $query .= "             AND E1.CLASSCD = T1.CLASSCD ";
        $query .= "             AND E1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "             AND E1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "             AND E1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "             AND E1.STANDARD_SEQ = T1.STANDARD_SEQ ";
        $query .= "             AND E1.REPRESENT_SEQ = T1.REPRESENT_SEQ ";
        $query .= "             AND E1.SCHREGNO = T1.SCHREGNO ";
        $query .= "             AND E1.RECEIPT_DATE = T1.RECEIPT_DATE ";
        $query .= "     ) ";
        $db->query($query);

        $query  = " DELETE FROM ";
        $query .= "     REP_PRESENT_DETAIL_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->field["YEAR"]}' ";
        $db->query($query);

        $query  = " DELETE FROM ";
        $query .= "     SCH_ATTEND_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'X' ";
        $query .= "         FROM ";
        $query .= "             SCH_ATTEND_DETAIL_DAT E1 ";
        $query .= "         WHERE ";
        $query .= "             E1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "             AND E1.SEQ = '001' ";
        $query .= "             AND E1.REMARK1 = '1' ";
        $query .= "             AND E1.YEAR = T1.YEAR ";
        $query .= "             AND E1.SCHREGNO = T1.SCHREGNO ";
        $query .= "             AND E1.EXECUTEDATE = T1.EXECUTEDATE ";
        $query .= "             AND E1.CHAIRCD = T1.CHAIRCD ";
        $query .= "     ) ";
        $db->query($query);

        $query  = " DELETE FROM ";
        $query .= "     SCH_ATTEND_DETAIL_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->field["YEAR"]}' ";
        $db->query($query);

        $query  = " DELETE FROM ";
        $query .= "     TEST_ATTEND_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'X' ";
        $query .= "         FROM ";
        $query .= "             TEST_ATTEND_DETAIL_DAT E1 ";
        $query .= "         WHERE ";
        $query .= "             E1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "             AND E1.SEQ = '001' ";
        $query .= "             AND E1.REMARK1 = '1' ";
        $query .= "             AND E1.YEAR = T1.YEAR ";
        $query .= "             AND E1.SEMESTER = T1.SEMESTER ";
        $query .= "             AND E1.CLASSCD = T1.CLASSCD ";
        $query .= "             AND E1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "             AND E1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "             AND E1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "             AND E1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     ) ";
        $db->query($query);

        $query  = " DELETE FROM ";
        $query .= "     TEST_ATTEND_DETAIL_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->field["YEAR"]}' ";
        $db->query($query);

        if ($model->Properties["useHyoutei"] == "1") {
            $testCd = "9-99-00-08";
        } else {
            $testCd = "9-99-00-09";
        }
        $query  = " WITH KEIZOKU_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.COMP_CONTINUE ";
        $query .= " FROM ";
        $query .= "     V_RECORD_SCORE_HIST_DAT T1 ";
        $query .= " WHERE ";
        $query .= "    T1.YEAR = '".($model->field["YEAR"] - 1)."' ";
        $query .= "    AND T1.SEMESTER || '-' || T1.TESTKINDCD || '-' || T1.TESTITEMCD || '-' || T1.SCORE_DIV = '{$testCd}' ";
        $query .= "    AND T1.COMP_CONTINUE = '1' ";
        $query .= " ), STD_SELECT AS ( ";
        $query .= " SELECT ";
        $query .= "     STD_SEL.CLASSCD, ";
        $query .= "     STD_SEL.SCHOOL_KIND, ";
        $query .= "     STD_SEL.CURRICULUM_CD, ";
        $query .= "     STD_SEL.SUBCLASSCD, ";
        $query .= "     STD_SEL.SCHREGNO, ";
        $query .= "     MAX(CHAIR.CHAIRCD) AS CHAIRCD ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_STD_SELECT_DAT STD_SEL ";
        $query .= "     INNER JOIN CHAIR_DAT CHAIR ON CHAIR.YEAR = STD_SEL.YEAR ";
        $query .= "           AND CHAIR.SEMESTER = STD_SEL.SEMESTER ";
        $query .= "           AND CHAIR.CLASSCD = STD_SEL.CLASSCD ";
        $query .= "           AND CHAIR.SCHOOL_KIND = STD_SEL.SCHOOL_KIND ";
        $query .= "           AND CHAIR.CURRICULUM_CD = STD_SEL.CURRICULUM_CD ";
        $query .= "           AND CHAIR.SUBCLASSCD = STD_SEL.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "    STD_SEL.YEAR = '{$model->field["YEAR"]}' ";
        $query .= " GROUP BY ";
        $query .= "     STD_SEL.CLASSCD, ";
        $query .= "     STD_SEL.SCHOOL_KIND, ";
        $query .= "     STD_SEL.CURRICULUM_CD, ";
        $query .= "     STD_SEL.SUBCLASSCD, ";
        $query .= "     STD_SEL.SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     STD_SELECT.CLASSCD, ";
        $query .= "     STD_SELECT.SCHOOL_KIND, ";
        $query .= "     STD_SELECT.CURRICULUM_CD, ";
        $query .= "     STD_SELECT.SUBCLASSCD, ";
        $query .= "     STD_SELECT.SCHREGNO, ";
        $query .= "     STD_SELECT.CHAIRCD, ";
        $query .= "     KEIZOKU_T.COMP_CONTINUE ";
        $query .= " FROM ";
        $query .= "     STD_SELECT, ";
        $query .= "     KEIZOKU_T ";
        $query .= " WHERE ";
        $query .= "    STD_SELECT.CLASSCD = KEIZOKU_T.CLASSCD ";
        $query .= "    AND STD_SELECT.SCHOOL_KIND = KEIZOKU_T.SCHOOL_KIND ";
        $query .= "    AND STD_SELECT.CURRICULUM_CD = KEIZOKU_T.CURRICULUM_CD ";
        $query .= "    AND STD_SELECT.SUBCLASSCD = KEIZOKU_T.SUBCLASSCD ";
        $query .= "    AND STD_SELECT.SCHREGNO = KEIZOKU_T.SCHREGNO ";

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            //RECORD_SCORE_HIST_DAT
            $query  = " INSERT INTO RECORD_SCORE_HIST_DAT ";
            $query .= " SELECT ";
            $query .= "     '{$model->field["YEAR"]}', ";
            $query .= "     SEMESTER, ";
            $query .= "     TESTKINDCD, ";
            $query .= "     TESTITEMCD, ";
            $query .= "     SCORE_DIV, ";
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
            $query .= "     SUBCLASSCD, ";
            $query .= "     SCHREGNO, ";
            $query .= "     SEQ, ";
            $query .= "     TEST_DATE, ";
            $query .= "     CHAIRCD, ";
            $query .= "     SCORE, ";
            $query .= "     VALUE, ";
            $query .= "     VALUE_DI, ";
            $query .= "     GET_CREDIT, ";
            $query .= "     ADD_CREDIT, ";
            $query .= "     COMP_TAKESEMES, ";
            $query .= "     COMP_CREDIT, ";
            $query .= "     NULL, ";
            $query .= "     '".STAFFCD."', ";
            $query .= "     sysdate() ";
            $query .= " FROM ";
            $query .= "     RECORD_SCORE_HIST_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".($model->field["YEAR"] - 1)."' ";
            $query .= "     AND CLASSCD = '{$row["CLASSCD"]}' ";
            $query .= "     AND SCHOOL_KIND = '{$row["SCHOOL_KIND"]}' ";
            $query .= "     AND CURRICULUM_CD = '{$row["CURRICULUM_CD"]}' ";
            $query .= "     AND SUBCLASSCD = '{$row["SUBCLASSCD"]}' ";
            $query .= "     AND SCHREGNO = '{$row["SCHREGNO"]}' ";
            $db->query($query);

            $query  = " INSERT INTO RECORD_SCORE_HIST_DETAIL_DAT ";
            $query .= " SELECT ";
            $query .= "     '{$model->field["YEAR"]}', ";
            $query .= "     SEMESTER, ";
            $query .= "     TESTKINDCD, ";
            $query .= "     TESTITEMCD, ";
            $query .= "     SCORE_DIV, ";
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
            $query .= "     SUBCLASSCD, ";
            $query .= "     SCHREGNO, ";
            $query .= "     SEQ, ";
            $query .= "     '001', ";
            $query .= "     '1', ";
            $query .= "     NULL, ";
            $query .= "     NULL, ";
            $query .= "     NULL, ";
            $query .= "     NULL, ";
            $query .= "     '".STAFFCD."', ";
            $query .= "     sysdate() ";
            $query .= " FROM ";
            $query .= "     RECORD_SCORE_HIST_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".($model->field["YEAR"] - 1)."' ";
            $query .= "     AND CLASSCD = '{$row["CLASSCD"]}' ";
            $query .= "     AND SCHOOL_KIND = '{$row["SCHOOL_KIND"]}' ";
            $query .= "     AND CURRICULUM_CD = '{$row["CURRICULUM_CD"]}' ";
            $query .= "     AND SUBCLASSCD = '{$row["SUBCLASSCD"]}' ";
            $query .= "     AND SCHREGNO = '{$row["SCHREGNO"]}' ";
            $db->query($query);

            //REP_PRESENT_DAT
            $query  = " INSERT INTO REP_PRESENT_DAT ";
            $query .= " SELECT ";
            $query .= "     '{$model->field["YEAR"]}', ";
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
            $query .= "     SUBCLASSCD, ";
            $query .= "     STANDARD_SEQ, ";
            $query .= "     REPRESENT_SEQ, ";
            $query .= "     SCHREGNO, ";
            $query .= "     RECEIPT_DATE, ";
            $query .= "     RECEIPT_INPUT_DATE, ";
            $query .= "     RECEIPT_TIME, ";
            $query .= "     CHAIRCD, ";
            $query .= "     STAFFCD, ";
            $query .= "     TERMINAL_CD, ";
            $query .= "     GRAD_VALUE, ";
            $query .= "     GRAD_DATE, ";
            $query .= "     GRAD_INPUT_DATE, ";
            $query .= "     GRAD_TIME, ";
            $query .= "     REPRESENT_PRINT, ";
            $query .= "     '".STAFFCD."', ";
            $query .= "     sysdate() ";
            $query .= " FROM ";
            $query .= "     REP_PRESENT_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".($model->field["YEAR"] - 1)."' ";
            $query .= "     AND CLASSCD = '{$row["CLASSCD"]}' ";
            $query .= "     AND SCHOOL_KIND = '{$row["SCHOOL_KIND"]}' ";
            $query .= "     AND CURRICULUM_CD = '{$row["CURRICULUM_CD"]}' ";
            $query .= "     AND SUBCLASSCD = '{$row["SUBCLASSCD"]}' ";
            $query .= "     AND SCHREGNO = '{$row["SCHREGNO"]}' ";
            $db->query($query);

            $query  = " INSERT INTO REP_PRESENT_DETAIL_DAT ";
            $query .= " SELECT ";
            $query .= "     '{$model->field["YEAR"]}', ";
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
            $query .= "     SUBCLASSCD, ";
            $query .= "     STANDARD_SEQ, ";
            $query .= "     REPRESENT_SEQ, ";
            $query .= "     SCHREGNO, ";
            $query .= "     RECEIPT_DATE, ";
            $query .= "     '001', ";
            $query .= "     '1', ";
            $query .= "     NULL, ";
            $query .= "     NULL, ";
            $query .= "     NULL, ";
            $query .= "     NULL, ";
            $query .= "     '".STAFFCD."', ";
            $query .= "     sysdate() ";
            $query .= " FROM ";
            $query .= "     REP_PRESENT_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".($model->field["YEAR"] - 1)."' ";
            $query .= "     AND CLASSCD = '{$row["CLASSCD"]}' ";
            $query .= "     AND SCHOOL_KIND = '{$row["SCHOOL_KIND"]}' ";
            $query .= "     AND CURRICULUM_CD = '{$row["CURRICULUM_CD"]}' ";
            $query .= "     AND SUBCLASSCD = '{$row["SUBCLASSCD"]}' ";
            $query .= "     AND SCHREGNO = '{$row["SCHREGNO"]}' ";
            $db->query($query);

            //SCH_ATTEND_DAT
            $query  = " INSERT INTO SCH_ATTEND_DAT ";
            $query .= " SELECT ";
            $query .= "     '{$model->field["YEAR"]}', ";
            $query .= "     SCHREGNO, ";
            $query .= "     EXECUTEDATE, ";
            $query .= "     CHAIRCD, ";
            $query .= "     PERIODCD, ";
            $query .= "     SCHOOLING_SEQ, ";
            $query .= "     RECEIPT_DATE, ";
            $query .= "     RECEIPT_TIME, ";
            $query .= "     STAFFCD, ";
            $query .= "     TERMINAL_CD, ";
            $query .= "     SCHOOLINGKINDCD, ";
            $query .= "     REMARK, ";
            $query .= "     CREDIT_TIME, ";
            $query .= "     '".STAFFCD."', ";
            $query .= "     sysdate() ";
            $query .= " FROM ";
            $query .= "     SCH_ATTEND_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".($model->field["YEAR"] - 1)."' ";
            $query .= "     AND SCHREGNO = '{$row["SCHREGNO"]}' ";
            $query .= "     AND CHAIRCD = '{$row["CHAIRCD"]}' ";
            $db->query($query);

            $query  = " INSERT INTO SCH_ATTEND_DETAIL_DAT ";
            $query .= " SELECT ";
            $query .= "     '{$model->field["YEAR"]}', ";
            $query .= "     SCHREGNO, ";
            $query .= "     EXECUTEDATE, ";
            $query .= "     CHAIRCD, ";
            $query .= "     '001', ";
            $query .= "     '1', ";
            $query .= "     NULL, ";
            $query .= "     NULL, ";
            $query .= "     NULL, ";
            $query .= "     NULL, ";
            $query .= "     '".STAFFCD."', ";
            $query .= "     sysdate() ";
            $query .= " FROM ";
            $query .= "     SCH_ATTEND_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".($model->field["YEAR"] - 1)."' ";
            $query .= "     AND SCHREGNO = '{$row["SCHREGNO"]}' ";
            $query .= "     AND CHAIRCD = '{$row["CHAIRCD"]}' ";
            $db->query($query);

            //TEST_ATTEND_DAT
            $query  = " INSERT INTO TEST_ATTEND_DAT ";
            $query .= " SELECT ";
            $query .= "     '{$model->field["YEAR"]}', ";
            $query .= "     SEMESTER, ";
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
            $query .= "     SUBCLASSCD, ";
            $query .= "     SCHREGNO, ";
            $query .= "     TEST_KIND, ";
            $query .= "     INPUT_DATE, ";
            $query .= "     '".STAFFCD."', ";
            $query .= "     sysdate() ";
            $query .= " FROM ";
            $query .= "     TEST_ATTEND_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".($model->field["YEAR"] - 1)."' ";
            $query .= "     AND CLASSCD = '{$row["CLASSCD"]}' ";
            $query .= "     AND SCHOOL_KIND = '{$row["SCHOOL_KIND"]}' ";
            $query .= "     AND CURRICULUM_CD = '{$row["CURRICULUM_CD"]}' ";
            $query .= "     AND SUBCLASSCD = '{$row["SUBCLASSCD"]}' ";
            $query .= "     AND SCHREGNO = '{$row["SCHREGNO"]}' ";
            $db->query($query);

            $query  = " INSERT INTO TEST_ATTEND_DETAIL_DAT ";
            $query .= " SELECT ";
            $query .= "     '{$model->field["YEAR"]}', ";
            $query .= "     SEMESTER, ";
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
            $query .= "     SUBCLASSCD, ";
            $query .= "     SCHREGNO, ";
            $query .= "     '001', ";
            $query .= "     '1', ";
            $query .= "     NULL, ";
            $query .= "     NULL, ";
            $query .= "     NULL, ";
            $query .= "     NULL, ";
            $query .= "     '".STAFFCD."', ";
            $query .= "     sysdate() ";
            $query .= " FROM ";
            $query .= "     TEST_ATTEND_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".($model->field["YEAR"] - 1)."' ";
            $query .= "     AND CLASSCD = '{$row["CLASSCD"]}' ";
            $query .= "     AND SCHOOL_KIND = '{$row["SCHOOL_KIND"]}' ";
            $query .= "     AND CURRICULUM_CD = '{$row["CURRICULUM_CD"]}' ";
            $query .= "     AND SUBCLASSCD = '{$row["SUBCLASSCD"]}' ";
            $query .= "     AND SCHREGNO = '{$row["SCHREGNO"]}' ";
            $db->query($query);
        }

        $db->commit();
        //DB切断
        Query::dbCheckIn($db);
    }

}
?>
