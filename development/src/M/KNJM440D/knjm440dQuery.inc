<?php

require_once('for_php7.php');

class knjm440dQuery extends Query {

    //学期取得
    function getSemesterMst() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
 
    //存在チェック
    function checkSubclassStdPassDat($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_STD_PASS_SDIV_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";

        return $query;
    }

    //更新対象
    function selectQuery($model) {
        $query  = " WITH CHAIR AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T2.CLASSCD, ";
        $query .= "         T2.SCHOOL_KIND, ";
        $query .= "         T2.CURRICULUM_CD, ";
        $query .= "         T2.SUBCLASSCD, ";
        $query .= "         T1.CHAIRCD ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT T1, ";
        $query .= "         CHAIR_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = T2.YEAR ";
        $query .= "         AND T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "         AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "         AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "         AND T1.CHAIRCD  = T2.CHAIRCD ";
        $query .= " ) ";

        //メイン
        $query .= " SELECT DISTINCT ";
        $query .= "     CHAIR.SCHREGNO, ";
        $query .= "     CHAIR.CLASSCD, ";
        $query .= "     CHAIR.SCHOOL_KIND, ";
        $query .= "     CHAIR.CURRICULUM_CD, ";
        $query .= "     CHAIR.SUBCLASSCD, ";
        $query .= "     CASE WHEN CORRES_SEM.SCHOOLING_LIMIT_CNT <= ATTEND_SEM.SCHOOLING_CNT THEN '1' ELSE NULL END AS SCHOOLING_PASS_FLG, ";
        $query .= "     CASE WHEN CORRES_SEM.REPO_LIMIT_CNT <= PRESENT_SEM.REPORT_CNT THEN '1' ELSE NULL END AS REP_PASS_FLG ";
        $query .= " FROM ";
        $query .= "     CHAIR ";
        $query .= "     INNER JOIN CHAIR_CORRES_SEMES_DAT CORRES_SEM ON CHAIR.YEAR = CORRES_SEM.YEAR ";
        $query .= "           AND CHAIR.SEMESTER      = CORRES_SEM.SEMESTER ";
        $query .= "           AND CHAIR.CHAIRCD       = CORRES_SEM.CHAIRCD ";
        $query .= "           AND CHAIR.CLASSCD       = CORRES_SEM.CLASSCD ";
        $query .= "           AND CHAIR.SCHOOL_KIND   = CORRES_SEM.SCHOOL_KIND ";
        $query .= "           AND CHAIR.CURRICULUM_CD = CORRES_SEM.CURRICULUM_CD ";
        $query .= "           AND CHAIR.SUBCLASSCD    = CORRES_SEM.SUBCLASSCD ";
        $query .= "     LEFT JOIN SCH_ATTEND_SEMES_DAT ATTEND_SEM ON CHAIR.YEAR = ATTEND_SEM.YEAR ";
        $query .= "          AND CHAIR.SEMESTER      = ATTEND_SEM.SEMESTER ";
        $query .= "          AND CHAIR.SCHREGNO      = ATTEND_SEM.SCHREGNO ";
        $query .= "          AND CHAIR.CHAIRCD       = ATTEND_SEM.CHAIRCD ";
        $query .= "     LEFT JOIN REP_PRESENT_SEMES_DAT PRESENT_SEM ON CHAIR.YEAR = PRESENT_SEM.YEAR ";
        $query .= "          AND CHAIR.SEMESTER      = PRESENT_SEM.SEMESTER ";
        $query .= "          AND CHAIR.CLASSCD       = PRESENT_SEM.CLASSCD ";
        $query .= "          AND CHAIR.SCHOOL_KIND   = PRESENT_SEM.SCHOOL_KIND ";
        $query .= "          AND CHAIR.CURRICULUM_CD = PRESENT_SEM.CURRICULUM_CD ";
        $query .= "          AND CHAIR.SUBCLASSCD    = PRESENT_SEM.SUBCLASSCD ";
        $query .= "          AND CHAIR.SCHREGNO      = PRESENT_SEM.SCHREGNO ";

        return $query;
    }

    //テスト種別
    function getTestcd($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.TESTKINDCD, ";
        $query .= "     T1.TESTITEMCD, ";
        $query .= "     T1.SCORE_DIV, ";
        $query .= "     T1.TESTITEMNAME ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND T1.TESTKINDCD != '99' ";
        return $query;
    }

    //INSERT
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knjm440dQuery::getTestcd($model);
        $result = $db->query($query);
        $testArray = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $testArray[] = $row;
        }

        //削除
        $query  = " DELETE FROM ";
        $query .= "     SUBCLASS_STD_PASS_SDIV_DAT ";
        $query .= " WHERE  ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' ";

        $db->query($query);

        //追加
        $query = knjm440dQuery::selectQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            foreach ($testArray as $key => $val) {
                $data = array();
                $subclass = "";
                $data["YEAR"][TEXT]             = CTRL_YEAR;
                $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
                $data["TESTKINDCD"][TEXT]       = $val["TESTKINDCD"];
                $data["TESTITEMCD"][TEXT]       = $val["TESTITEMCD"];
                $data["SCORE_DIV"][TEXT]        = $val["SCORE_DIV"];
                $data["CLASSCD"][TEXT]          = $row["CLASSCD"];
                $data["SCHOOL_KIND"][TEXT]      = $row["SCHOOL_KIND"];
                $data["CURRICULUM_CD"][TEXT]    = $row["CURRICULUM_CD"];
                $data["SUBCLASSCD"][TEXT]       = $row["SUBCLASSCD"];
                $data["SCHREGNO"][TEXT]         = $row["SCHREGNO"];
                $data["REP_PASS_FLG"][TEXT]     = $row["REP_PASS_FLG"];
                $data["SCHOOLING_PASS_FLG"][TEXT] = $row["SCHOOLING_PASS_FLG"];

                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "sysdate()";

                $query = Query::insertSQL($data, "SUBCLASS_STD_PASS_SDIV_DAT");
                $db->query($query);
            }
        }
        $result->free();

        $db->commit();  // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }

}
?>
