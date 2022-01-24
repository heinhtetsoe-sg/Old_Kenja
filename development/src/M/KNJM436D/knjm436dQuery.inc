<?php

require_once('for_php7.php');

class knjm436dQuery extends Query {

    //科目データの取得
    function selectSubclassQuery($model) {
        $query  = " SELECT ";
        $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS VALUE, ";
        $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD || ' ' || T3.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_STD_DAT T2, ";
        $query .= "     SUBCLASS_MST T3, ";
        $query .= "     CHAIR_STF_DAT T4 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T2.YEAR     = T1.YEAR ";
        $query .= "     AND T1.YEAR     = T4.YEAR ";
        $query .= "     AND T1.SEMESTER = T4.SEMESTER ";
        $query .= "     AND T1.CHAIRCD  = T4.CHAIRCD ";
        $query .= "     AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "     AND T2.CHAIRCD  = T1.CHAIRCD ";
        $query .= "     AND T3.CLASSCD <= '90' ";
        //更新可能(制限付)
        if(AUTHORITY != DEF_UPDATABLE){
            $query .= "     AND t4.STAFFCD = '".STAFFCD."' ";
        }
        $query .= "     AND T3.CLASSCD       = T1.CLASSCD ";
        $query .= "     AND T3.SCHOOL_KIND   = T1.SCHOOL_KIND ";
        $query .= "     AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "     AND T3.SUBCLASSCD    = T1.SUBCLASSCD ";
        $query .= " GROUP BY ";
        $query .= "     T3.CLASSCD, ";
        $query .= "     T3.SCHOOL_KIND, ";
        $query .= "     T3.CURRICULUM_CD, ";
        $query .= "     T3.SUBCLASSCD, ";
        $query .= "     T3.SUBCLASSNAME ";
        $query .= " ORDER BY ";
        $query .= "     T3.CLASSCD, ";
        $query .= "     T3.SCHOOL_KIND, ";
        $query .= "     T3.CURRICULUM_CD, ";
        $query .= "     T3.SUBCLASSCD ";

        return $query;
    }

    //成績データ取得
    function GetRecordDatdata($model) {
        $chrsub_array = array();
        $chrsub_array = explode("-", $model->subclassCd);

        $query  = " WITH CHAIR_STD AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         CHAR.YEAR, ";
        $query .= "         CHAR.CLASSCD, ";
        $query .= "         CHAR.SCHOOL_KIND, ";
        $query .= "         CHAR.CURRICULUM_CD, ";
        $query .= "         CHAR.SUBCLASSCD, ";
        $query .= "         STDD.SCHREGNO, ";
        $query .= "         PRST.REPORT_CNT, ";
        $query .= "         ATTE.SCHOOLING_CNT ";
        $query .= "     FROM ";
        $query .= "         CHAIR_DAT CHAR ";
        $query .= "         INNER JOIN CHAIR_STD_DAT STDD ON STDD.YEAR     = CHAR.YEAR ";
        $query .= "                                      AND STDD.SEMESTER = CHAR.SEMESTER ";
        $query .= "                                      AND STDD.CHAIRCD  = CHAR.CHAIRCD ";
        $query .= "         INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV NEWS ON NEWS.YEAR     = CHAR.YEAR ";
        $query .= "                                                       AND NEWS.SEMESTER = CHAR.SEMESTER ";
        $query .= "         INNER JOIN SUBCLASS_STD_PASS_SDIV_DAT PASS ON PASS.YEAR          = CHAR.YEAR ";
        $query .= "                                                   AND PASS.SEMESTER      = CHAR.SEMESTER ";
        $query .= "                                                   AND PASS.TESTKINDCD    = NEWS.TESTKINDCD ";
        $query .= "                                                   AND PASS.TESTITEMCD    = NEWS.TESTITEMCD ";
        $query .= "                                                   AND PASS.SCORE_DIV     = NEWS.SCORE_DIV ";
        $query .= "                                                   AND PASS.CLASSCD       = CHAR.CLASSCD ";
        $query .= "                                                   AND PASS.SCHOOL_KIND   = CHAR.SCHOOL_KIND ";
        $query .= "                                                   AND PASS.CURRICULUM_CD = CHAR.CURRICULUM_CD ";
        $query .= "                                                   AND PASS.SUBCLASSCD    = CHAR.SUBCLASSCD ";
        $query .= "                                                   AND PASS.SCHREGNO      = STDD.SCHREGNO ";
        $query .= "         LEFT JOIN REP_PRESENT_SEMES_DAT PRST ON PRST.YEAR          = CHAR.YEAR ";
        $query .= "                                             AND PRST.SEMESTER      = CHAR.SEMESTER ";
        $query .= "                                             AND PRST.CLASSCD       = CHAR.CLASSCD ";
        $query .= "                                             AND PRST.SCHOOL_KIND   = CHAR.SCHOOL_KIND ";
        $query .= "                                             AND PRST.CURRICULUM_CD = CHAR.CURRICULUM_CD ";
        $query .= "                                             AND PRST.SUBCLASSCD    = CHAR.SUBCLASSCD ";
        $query .= "                                             AND PRST.SCHREGNO      = STDD.SCHREGNO ";
        $query .= "         LEFT JOIN SCH_ATTEND_SEMES_DAT ATTE ON ATTE.YEAR     = CHAR.YEAR ";
        $query .= "                                            AND ATTE.SEMESTER = CHAR.SEMESTER ";
        $query .= "                                            AND ATTE.SCHREGNO = STDD.SCHREGNO ";
        $query .= "                                            AND ATTE.CHAIRCD  = CHAR.CHAIRCD ";
        $query .= "     WHERE ";
        $query .= "             CHAR.YEAR               = '".CTRL_YEAR."' ";
        $query .= "         AND CHAR.CLASSCD            = '".$chrsub_array[0]."' ";
        $query .= "         AND CHAR.SCHOOL_KIND        = '".$chrsub_array[1]."' ";
        $query .= "         AND CHAR.CURRICULUM_CD      = '".$chrsub_array[2]."' ";
        $query .= "         AND CHAR.SUBCLASSCD         = '".$chrsub_array[3]."' ";
        $query .= "         AND PASS.REP_PASS_FLG       = '1' ";
        $query .= "         AND PASS.SCHOOLING_PASS_FLG = '1' ";
        //在籍MAX
        $query .= " ), REGD_MAX AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR, ";
        $query .= "         SCHREGNO, ";
        $query .= "         MAX(SEMESTER) AS SEMESTER ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "     GROUP BY ";
        $query .= "         YEAR, ";
        $query .= "         SCHREGNO ";
        //成績
        $query .= " ), SCORE_HIST AS ( ";
        $query .= "     SELECT ";
        $query .= "         SEMESTER || '-' || TESTKINDCD || '-' || TESTITEMCD || '-' || SCORE_DIV AS TESTCD, ";
        $query .= "         SCHREGNO, ";
        $query .= "         SCORE, ";
        $query .= "         VALUE, ";
        $query .= "         GET_CREDIT, ";
        $query .= "         COMP_CREDIT ";
        $query .= "     FROM ";
        $query .= "         RECORD_SCORE_HIST_DAT ";
        $query .= "     WHERE ";
        $query .= "             YEAR          = '".CTRL_YEAR."' ";
        $query .= "         AND SEMESTER      = '".$model->semester."' ";
        $query .= "         AND CLASSCD       = '".$chrsub_array[0]."' ";
        $query .= "         AND SCHOOL_KIND   = '".$chrsub_array[1]."' ";
        $query .= "         AND CURRICULUM_CD = '".$chrsub_array[2]."' ";
        $query .= "         AND SUBCLASSCD    = '".$chrsub_array[3]."' ";
        $query .= "         AND SEQ           = 1 ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     MAIN.SCHREGNO, ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     BASE.NAME_SHOW, ";
        $query .= "     HDAT.HR_NAME, ";
        $query .= "     MAIN.REPORT_CNT, ";
        $query .= "     MAIN.SCHOOLING_CNT, ";
        $query .= "     VALU.VALUE, ";
        $query .= "     VALU.GET_CREDIT, ";
        $query .= "     VALU.COMP_CREDIT, ";
        $query .= "     CRDT.CREDITS ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD MAIN ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = MAIN.SCHREGNO ";
        $query .= "     INNER JOIN REGD_MAX ON REGD_MAX.SCHREGNO = MAIN.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = MAIN.SCHREGNO ";
        $query .= "                                    AND REGD.YEAR     = MAIN.YEAR ";
        $query .= "                                    AND REGD.SEMESTER = REGD_MAX.SEMESTER ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR     = REGD.YEAR ";
        $query .= "                                     AND HDAT.SEMESTER = REGD.SEMESTER  ";
        $query .= "                                     AND HDAT.GRADE    = REGD.GRADE ";
        $query .= "                                     AND HDAT.HR_CLASS = REGD.HR_CLASS ";
        $query .= "     LEFT JOIN SCORE_HIST VALU ON VALU.SCHREGNO = MAIN.SCHREGNO ";
        $query .= "                              AND VALU.TESTCD   = '{$model->semester}-{$model->testCd}' ";
        $query .= "     LEFT JOIN CREDIT_MST CRDT ON REGD.YEAR          = CRDT.YEAR ";
        $query .= "                              AND REGD.COURSECD      = CRDT.COURSECD ";
        $query .= "                              AND REGD.MAJORCD       = CRDT.MAJORCD ";
        $query .= "                              AND REGD.GRADE         = CRDT.GRADE ";
        $query .= "                              AND REGD.COURSECODE    = CRDT.COURSECODE ";
        $query .= "                              AND MAIN.CLASSCD       = CRDT.CLASSCD ";
        $query .= "                              AND MAIN.SCHOOL_KIND   = CRDT.SCHOOL_KIND ";
        $query .= "                              AND MAIN.CURRICULUM_CD = CRDT.CURRICULUM_CD ";
        $query .= "                              AND MAIN.SUBCLASSCD    = CRDT.SUBCLASSCD ";
        //クラス番号順の場合はクラスまで指定する
        if ($model->order == 2) {
            $query .= " ORDER BY ";
            $query .= "     REGD.GRADE, ";
            $query .= "     REGD.HR_CLASS, ";
            $query .= "     REGD.ATTENDNO ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     MAIN.SCHREGNO ";
        }

        return $query;
    }

    /**************/
    /** 更新処理 **/
    /**************/
    /* UPDATE */
    function updateQuery($model) {
        $db = Query::dbCheckOut();

        $chrsub_array = array();
        $chrsub_array = explode("-", $model->subclassCd);

        for ($i = 0; $i < get_count($model->data["SCHREGNO"]); $i++) {
            $schregNo = $model->data["SCHREGNO"][$i];

            //評定
            $testCd     = "{$model->semester}-{$model->testCd}";
            $setValue   = $model->fields["VALUE"][$schregNo];
            $setGetCre  = $model->fields["GET_CREDIT"][$schregNo];
            $setCompCre = $model->fields["COMP_CREDIT"][$schregNo];
            $query = knjm436dQuery::getUpdateQuery($db, $model, $chrsub_array, $schregNo, $testCd, $setValue, $setGetCre, $setCompCre);
            $db->query($query);
        }

        Query::dbCheckIn($db);
    }

    /* データ更新処理 */
    function getUpdateQuery($db, $model, $chrsub_array, $schregNo, $testCd, $setValue, $setGetCre, $setCompCre) {
        $test_array = explode("-", $testCd);

        $data = array();
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEMESTER"][TEXT]         = $test_array[0];
        $data["TESTKINDCD"][TEXT]       = $test_array[1];
        $data["TESTITEMCD"][TEXT]       = $test_array[2];
        $data["SCORE_DIV"][TEXT]        = $test_array[3];
        $data["CLASSCD"][TEXT]          = $chrsub_array[0];
        $data["SCHOOL_KIND"][TEXT]      = $chrsub_array[1];
        $data["CURRICULUM_CD"][TEXT]    = $chrsub_array[2];
        $data["SUBCLASSCD"][TEXT]       = $chrsub_array[3];
        $data["SCHREGNO"][TEXT]         = $schregNo;
        $data["SEQ"][NUMBER]            = 1; //1回目のみ
        $data["TEST_DATE"][TEXT]        = CTRL_DATE;
        $data["VALUE"][NUMBER]          = $setValue;
        $data["GET_CREDIT"][NUMBER]     = $setGetCre;
        $data["COMP_CREDIT"][NUMBER]    = $setCompCre;
        $data["REGISTERCD"][TEXT]       = STAFFCD ;
        $data["UPDATED"][FUNC]          = "sysdate()";

        //条件
        $where  = " WHERE YEAR          = '".CTRL_YEAR."' ";
        $where .= "   AND SEMESTER || '-' || TESTKINDCD || '-' || TESTITEMCD || '-' || SCORE_DIV = '{$testCd}' ";
        $where .= "   AND CLASSCD       = '".$chrsub_array[0]."' ";
        $where .= "   AND SCHOOL_KIND   = '".$chrsub_array[1]."' ";
        $where .= "   AND CURRICULUM_CD = '".$chrsub_array[2]."' ";
        $where .= "   AND SUBCLASSCD    = '".$chrsub_array[3]."' ";
        $where .= "   AND SCHREGNO      = '{$schregNo}'";
        $where .= "   AND SEQ           = 1 ";

        $query = "SELECT COUNT(*) AS CNT FROM RECORD_SCORE_HIST_DAT ".$where;
        $dataCnt = $db->getOne($query);

        //更新
        if (0 < $dataCnt) {
            $query = Query::updateSQL($data, "RECORD_SCORE_HIST_DAT", $where);
        //追加
        } else {
            $query = Query::insertSQL($data, "RECORD_SCORE_HIST_DAT");
        }
        return $query;
    }
}
?>
