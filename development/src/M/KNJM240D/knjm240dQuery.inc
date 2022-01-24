<?php

require_once('for_php7.php');

class knjm240dQuery extends Query {
    /************/
    /** 左画面 **/
    /************/
    //年度取得
    function getSub_ClasyearQuery() {
        $query  = " SELECT DISTINCT";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM  ";
        $query .= "     CHAIR_DAT ";
        $query .= " ORDER BY  ";
        $query .= "     YEAR DESC ";

        return $query;
    }

    //学期取得
    function getSemester($year) {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR      = '{$year}' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= "         ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //講座一覧
    function ReadQuery($model) {
        $query  = " SELECT ";
        $query .= "     CHAIR.YEAR, ";
        $query .= "     CHAIR.SEMESTER, ";
        $query .= "     CHAIR.CHAIRCD, ";
        $query .= "     SEMES.REPO_MAX_CNT, ";
        $query .= "     SEMES.REPO_LIMIT_CNT, ";
        $query .= "     SEMES.SCHOOLING_MAX_CNT, ";
        $query .= "     SEMES.SCHOOLING_LIMIT_CNT, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || '-' || CHAIR.CURRICULUM_CD || '-' || CHAIR.SUBCLASSCD AS SUBCLASSCD, ";
        } else {
            $query .= "     CHAIR.SUBCLASSCD, ";
        }
        $query .= "     CHAIR.CHAIRNAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT CHAIR ";
        $query .= "     LEFT JOIN CHAIR_CORRES_SEMES_DAT SEMES ON CHAIR.YEAR          = SEMES.YEAR ";
        $query .= "                                           AND CHAIR.SEMESTER      = SEMES.SEMESTER ";
        $query .= "                                           AND CHAIR.CHAIRCD       = SEMES.CHAIRCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                                       AND CHAIR.CLASSCD       = SEMES.CLASSCD ";
            $query .= "                                       AND CHAIR.SCHOOL_KIND   = SEMES.SCHOOL_KIND ";
            $query .= "                                       AND CHAIR.CURRICULUM_CD = SEMES.CURRICULUM_CD ";
        }
        $query .= "                                           AND CHAIR.SUBCLASSCD    = SEMES.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "         CHAIR.YEAR     = {$model->year} ";
        $query .= "     AND CHAIR.SEMESTER = {$model->semester} ";
        $query .= " ORDER BY ";
        $query .= "     CHAIR.CHAIRCD ";

        return $query;
    }

    /************/
    /** 右画面 **/
    /************/
    //学期等、取得
    function getSemesterCnt($model) {
        $query  = " SELECT ";
        $query .= "     CORS.REPO_MAX_CNT, ";
        $query .= "     CORS.REPO_LIMIT_CNT, ";
        $query .= "     CORS.SCHOOLING_MAX_CNT, ";
        $query .= "     CORS.SCHOOLING_LIMIT_CNT, ";
        $query .= "     SEMS.SEMESTER, ";
        $query .= "     SEMS.SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT CHAR ";
        $query .= "     LEFT JOIN SEMESTER_MST SEMS ON CHAR.YEAR     = SEMS.YEAR ";
        $query .= "                                AND CHAR.SEMESTER = SEMS.SEMESTER ";
        $query .= "     LEFT JOIN CHAIR_CORRES_SEMES_DAT CORS ON CHAR.YEAR          = CORS.YEAR ";
        $query .= "                                          AND CHAR.SEMESTER      = CORS.SEMESTER ";
        $query .= "                                          AND CHAR.CHAIRCD       = CORS.CHAIRCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                                      AND CHAR.CLASSCD       = CORS.CLASSCD ";
            $query .= "                                      AND CHAR.SCHOOL_KIND   = CORS.SCHOOL_KIND ";
            $query .= "                                      AND CHAR.CURRICULUM_CD = CORS.CURRICULUM_CD ";
        }
        $query .= "                                          AND CHAR.SUBCLASSCD    = CORS.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "         CHAR.YEAR     = '{$model->year}' ";
        $query .= "     AND CHAR.CHAIRCD  = '{$model->chairCd}' ";
        $query .= " ORDER BY ";
        $query .= "     SEMS.SEMESTER ";

        return $query;
    }

    /**************/
    /** 更新処理 **/
    /**************/
    //１レコード取得
    function chairCorresSemesCnt($model, $semester) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CHAIR_CORRES_SEMES_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$semester}' ";
        $query .= "     AND CHAIRCD  = '{$model->chairCd}' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->subclassCd}' ";
        } else {
            $query .= "     AND SUBCLASSCD = '{$model->subclassCd}' ";
        }

        return $query;
    }

    //UPDATE
    function &getInsertUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        list($classCd, $schoolKind, $curriculumCd, $subclassCd) = explode("-", $model->subclassCd);

        for ($i = 0; $i < get_count($model->data["SEMESTER"]); $i++) {
            $semester = $model->data["SEMESTER"][$i];

            $chairCorresSemesCnt = $db->getOne(knjm240dQuery::chairCorresSemesCnt($model, $semester));

            $data = array();
            $data["YEAR"][TEXT]                     = $model->year;
            $data["SEMESTER"][TEXT]                 = $semester;
            $data["CHAIRCD"][TEXT]                  = $model->chairCd;
            $data["CLASSCD"][TEXT]                  = $classCd;
            $data["SCHOOL_KIND"][TEXT]              = $schoolKind;
            $data["CURRICULUM_CD"][TEXT]            = $curriculumCd;
            $data["SUBCLASSCD"][TEXT]               = $subclassCd;   
            $data["REPO_MAX_CNT"][NUMBER]           = $model->fields["REPO_MAX_CNT"][$semester];
            $data["REPO_LIMIT_CNT"][NUMBER]         = $model->fields["REPO_LIMIT_CNT"][$semester];
            $data["SCHOOLING_MAX_CNT"][NUMBER]      = $model->fields["SCHOOLING_MAX_CNT"][$semester];
            $data["SCHOOLING_LIMIT_CNT"][NUMBER]    = $model->fields["SCHOOLING_LIMIT_CNT"][$semester];
            $data["REGISTERCD"][TEXT]               = STAFFCD;
            $data["UPDATED"][FUNC]                  = "sysdate()";

            if ($chairCorresSemesCnt > 0) {
                $where  = " WHERE ";
                $where .= "         YEAR          = '{$model->year}' ";
                $where .= "     AND SEMESTER      = '{$semester}' ";
                $where .= "     AND CHAIRCD       = '{$model->chairCd}' ";
                $where .= "     AND CLASSCD       = '{$classCd}' ";
                $where .= "     AND SCHOOL_KIND   = '{$schoolKind}' ";
                $where .= "     AND CURRICULUM_CD = '{$curriculumCd}' ";
                $where .= "     AND SUBCLASSCD    = '{$subclassCd}' ";

                $query = Query::updateSQL($data, "CHAIR_CORRES_SEMES_DAT", $where);
                $db->query($query);
            } else {
                $query = Query::insertSQL($data, "CHAIR_CORRES_SEMES_DAT");
                $db->query($query);
            }
        }

        $db->commit();      // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }
}
?>
