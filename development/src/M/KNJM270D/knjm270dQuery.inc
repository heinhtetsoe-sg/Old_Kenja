<?php

require_once('for_php7.php');

class knjm270dQuery extends Query {

    //学期コンボ
    function getSemecmb() {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME AS LABEL, ";
        $query .= "     SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR      = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目データの取得
    function getSubClassCd($model) {
        $query  = " SELECT ";
        $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS VALUE, ";
        $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD || ' ' || T3.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_STD_DAT T2, ";
        $query .= "     SUBCLASS_MST T3, ";
        $query .= "     CHAIR_STF_DAT T4 ";
        if ($model->Properties["printSubclassLastChairStd"] == "1") {
            $query .= "    ,SEMESTER_MST T5 ";
        }
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$model->semester."' ";
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
        if ($model->Properties["printSubclassLastChairStd"] == "1") {
            $query .= "         AND T5.YEAR  = T1.YEAR ";
            $query .= "         AND T5.SEMESTER = T1.SEMESTER ";
            $query .= "         AND T5.EDATE    = T2.APPENDDATE ";
        }
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

    //講座データの取得
    function getChrSubCd($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CHAIRCD || ' ' || T1.CHAIRNAME AS LABEL, ";
        $query .= "     T1.CHAIRCD || '-' || T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_STD_DAT T2, ";
        $query .= "     CHAIR_STF_DAT T3 ";
        if ($model->Properties["printSubclassLastChairStd"] == "1") {
            $query .= "    ,SEMESTER_MST T5 ";
        }
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$model->semester."' ";
        $query .= "     AND T2.YEAR     = T1.YEAR ";
        $query .= "     AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "     AND T2.CHAIRCD  = T1.CHAIRCD ";
        $query .= "     AND T1.YEAR     = T3.YEAR ";
        $query .= "     AND T1.SEMESTER = T3.SEMESTER ";
        $query .= "     AND T1.CHAIRCD  = T3.CHAIRCD ";
        $query .= "     AND T1.CLASSCD  <= '90' ";
        //更新可能(制限付)
        if(AUTHORITY != DEF_UPDATABLE){
          $query .= "       AND T3.STAFFCD = '".STAFFCD."' ";
        }
        $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD= '".$model->field["SUBCLASSCD"]."' ";
        if ($model->Properties["printSubclassLastChairStd"] == "1") {
            $query .= "         AND T5.YEAR  = T1.YEAR ";
            $query .= "         AND T5.SEMESTER = T1.SEMESTER ";
            $query .= "         AND T5.EDATE    = T2.APPENDDATE ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //レポート回数・最低提出回数、取得
    function getChairCorresSemesDat($model) {
        $chrsub_array = array();
        list($chaircd, $classcd, $school_kind, $curriculum_cd, $subclasscd) = explode("-", $model->chairCd);

        $query  = " SELECT ";
        $query .= "     REPO_MAX_CNT, ";
        $query .= "     REPO_LIMIT_CNT ";
        $query .= " FROM ";
        $query .= "     CHAIR_CORRES_SEMES_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR          = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER      = '".$model->semester."' ";
        $query .= "     AND CHAIRCD       = '".$chaircd."' ";
        $query .= "     AND CLASSCD       = '".$classcd."' ";
        $query .= "     AND SCHOOL_KIND   = '".$school_kind."' ";
        $query .= "     AND CURRICULUM_CD = '".$curriculum_cd."' ";
        $query .= "     AND SUBCLASSCD    = '".$subclasscd."' ";

        return $query;
    }

    //提出回数データ取得
    function getSchAttendSemesDat($model) {
        $chrsub_array = array();
        $chrsub_array = explode("-", $model->chairCd);
        list($chaircd, $classcd, $school_kind, $curriculum_cd, $subclasscd) = explode("-", $model->chairCd);

        $query  = " WITH CHAIR_STD AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         T1.CHAIRCD, ";
        $query .= "         T1.CLASSCD, ";
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         T1.CURRICULUM_CD, ";
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T2.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         CHAIR_DAT T1 ";
        $query .= "         INNER JOIN CHAIR_STD_DAT T2 ON T2.YEAR     = T1.YEAR ";
        $query .= "                                    AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "                                    AND T2.CHAIRCD  = T1.CHAIRCD ";
        if ($model->Properties["printSubclassLastChairStd"] == "1") {
            $query .= "         INNER JOIN SEMESTER_MST T3 ON T3.YEAR  = T1.YEAR ";
            $query .= "                                AND T3.SEMESTER = T1.SEMESTER ";
            $query .= "                                AND T3.EDATE    = T2.APPENDDATE ";
        }
        $query .= "     WHERE ";
        $query .= "             T1.YEAR          = '".CTRL_YEAR."' ";
        $query .= "         AND T1.SEMESTER      = '".$model->semester."' ";
        $query .= "         AND T1.CHAIRCD       = '".$chaircd."' ";
        $query .= "         AND T1.CLASSCD       = '".$classcd."' ";
        $query .= "         AND T1.SCHOOL_KIND   = '".$school_kind."' ";
        $query .= "         AND T1.CURRICULUM_CD = '".$curriculum_cd."' ";
        $query .= "         AND T1.SUBCLASSCD    = '".$subclasscd."' ";
        $query .= " ), SCORE_HIST_DATA AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         SCHREGNO, ";
        $query .= "         '入力済み' AS INTO_CHECK ";
        $query .= "     FROM ";
        $query .= "         V_RECORD_SCORE_HIST_DAT ";
        $query .= "     WHERE ";
        $query .= "             YEAR          = '".CTRL_YEAR."' ";
        $query .= "         AND SEMESTER      = '".$model->semester."' ";
        $query .= "         AND CLASSCD       = '".$classcd."' ";
        $query .= "         AND SCHOOL_KIND   = '".$school_kind."' ";
        $query .= "         AND CURRICULUM_CD = '".$curriculum_cd."' ";
        $query .= "         AND SUBCLASSCD    = '".$subclasscd."' ";
        $query .= "         AND (SCORE IS NOT NULL OR VALUE IS NOT NULL) ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     MAIN.SCHREGNO, ";
        $query .= "     HDAT.HR_NAME, ";
        $query .= "     BASE.NAME_SHOW, ";
        $query .= "     SEME.REPORT_CNT, ";
        $query .= "     HIST.INTO_CHECK ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD MAIN ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = MAIN.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = MAIN.SCHREGNO ";
        $query .= "                                    AND REGD.YEAR     = MAIN.YEAR ";
        $query .= "                                    AND REGD.SEMESTER = MAIN.SEMESTER ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR     = REGD.YEAR ";
        $query .= "                                     AND HDAT.SEMESTER = REGD.SEMESTER ";
        $query .= "                                     AND HDAT.GRADE    = REGD.GRADE ";
        $query .= "                                     AND HDAT.HR_CLASS = REGD.HR_CLASS ";
        $query .= "     LEFT JOIN REP_PRESENT_SEMES_DAT SEME ON SEME.YEAR    = MAIN.YEAR ";
        $query .= "                                        AND SEME.SEMESTER = MAIN.SEMESTER ";
        $query .= "                                        AND SEME.SCHREGNO = MAIN.SCHREGNO ";
        $query .= "                                        AND SEME.CLASSCD  = MAIN.CLASSCD ";
        $query .= "                                        AND SEME.SCHOOL_KIND  = MAIN.SCHOOL_KIND ";
        $query .= "                                        AND SEME.CURRICULUM_CD  = MAIN.CURRICULUM_CD ";
        $query .= "                                        AND SEME.SUBCLASSCD  = MAIN.SUBCLASSCD ";
        $query .= "     LEFT JOIN SCORE_HIST_DATA HIST ON HIST.SCHREGNO = MAIN.SCHREGNO ";
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
    /* データ更新処理 */
    function updateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $chrsub_array = array();
        list($chaircd, $classcd, $school_kind, $curriculum_cd, $subclasscd) = explode("-", $model->chairCd);

        $query  = " DELETE FROM REP_PRESENT_SEMES_DAT ";
        $query .= "       WHERE YEAR     = '".CTRL_YEAR."' ";
        $query .= "         AND SEMESTER = '".$model->semester."' ";
        $query .= "         AND CLASSCD  = '".$classcd."' ";
        $query .= "         AND SCHOOL_KIND  = '".$school_kind."' ";
        $query .= "         AND CURRICULUM_CD  = '".$curriculum_cd."' ";
        $query .= "         AND SUBCLASSCD  = '".$subclasscd."' ";

        $db->query($query);

        for ($i = 0; $i < get_count($model->data["SCHREGNO"]); $i++) {
            $schregNo = $model->data["SCHREGNO"][$i];

            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SEMESTER"][TEXT]         = $model->semester;
            $data["SCHREGNO"][TEXT]         = $schregNo;
            $data["CLASSCD"][TEXT]          = $classcd;
            $data["SCHOOL_KIND"][TEXT]      = $school_kind;
            $data["CURRICULUM_CD"][TEXT]    = $curriculum_cd;
            $data["SUBCLASSCD"][TEXT]       = $subclasscd;
            $data["CHAIRCD"][TEXT]          = $chaircd;
            $data["REPORT_CNT"][NUMBER]     = $model->fields["REPORT_CNT"][$schregNo];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "REP_PRESENT_SEMES_DAT");
            $db->query($query);
        }

        $db->commit();      // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }
}
?>
