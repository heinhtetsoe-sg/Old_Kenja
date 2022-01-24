<?php

require_once('for_php7.php');
class knjd133dquery extends Query {

    //学期取得
    function getSemesterCmb($target="") {
        $query  = " SELECT ";
        if ($target) {
            $query .= "     SEMESTERNAME ";
        } else {
            $query .= "     SEMESTER AS VALUE, ";
            $query .= "     SEMESTERNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        if ($target) {
            $query .= "     AND SEMESTER = '{$target}' ";
        } else {
            $query .= "     AND SEMESTER <> '9' ";
        }

        return $query;
    }

    //名称マスタD008用
    function getNameMstche($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS COUNT";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '$model->che_school_kind' ";

        return $query;
    }

    //名称マスタA042用
    function getNameA042($model) {
        $query  .= " SELECT ";
        $query  .= "   NAMECD2 ";
        $query  .= " FROM ";
        $query  .= "   V_NAME_MST ";
        $query  .= " WHERE ";
        $query  .= "   YEAR = '".CTRL_YEAR."' ";
        $query  .= "   AND NAMECD1 = 'A042' ";
        $query  .= "   AND NAMESPARE1 = '{$model->field["SUBCLASSCD"]}' ";

        return $query;
    }

    //校種取得
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     NAME_M.NAME1 AS VALUE, ";
        $query .= "     NAME_M.ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST NAME_M ";
        $query .= " WHERE ";
        $query .= "     NAME_M.NAMECD1 = 'A023' ";
        $query .= "     AND NAME_M.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME_M.NAME1 IN (SELECT ";
                $query .= "                              REGD_G.SCHOOL_KIND ";
                $query .= "                          FROM ";
                $query .= "                              SCHREG_REGD_GDAT REGD_G ";
                $query .= "                          WHERE ";
                $query .= "                              REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND NAME_M.NAME1 IN (SELECT ";
            $query .= "                              REGD_G.SCHOOL_KIND ";
            $query .= "                          FROM ";
            $query .= "                              SCHREG_REGD_GDAT REGD_G ";
            $query .= "                          WHERE ";
            $query .= "                              REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAME_M.NAMECD2 ";

        return $query;
    }

    function getTeikeiData($model, $datadiv) {
        $query   = "    SELECT ";
        $query  .= "      GRADE, ";
        $query  .= "      PATTERN_CD, ";
        $query  .= "      REMARK ";
        $query  .= "    FROM ";
        $query  .= "      HTRAINREMARK_TEMP_SEMES_DAT ";
        $query  .= "    WHERE ";
        $query  .= "      YEAR = '".CTRL_YEAR."' ";
        $query  .= "      AND SEMESTER = '9' ";
        $query  .= "      AND DATA_DIV = '{$datadiv}' ";
        $query  .= "    ORDER BY ";
        $query  .= "      GRADE, PATTERN_CD ";

        return $query;
    }

    //科目データの取得
    function selectSubclassQuery($model) {
        $namecd1 = ($model->count > 0) ? $model->che_school_kind : "D008";

        $query  = "    SELECT DISTINCT ";
        $query .= "      VSUB.CLASSCD || '-' || VSUB.SCHOOL_KIND || '-' || VSUB.CURRICULUM_CD || '-' || VSUB.SUBCLASSCD AS VALUE, ";
        $query .= "      VSUB.CLASSCD || '-' || VSUB.SCHOOL_KIND || '-' || VSUB.CURRICULUM_CD || '-' || VSUB.SUBCLASSCD || ':' || VALUE (VSUB.SUBCLASSNAME, '') AS LABEL ";
        $query .= "    FROM ";
        $query .= "      CHAIR_DAT CHAIR ";
        $query .= "    INNER JOIN CHAIR_STF_DAT STF ";
        $query .= "       ON STF.YEAR             = CHAIR.YEAR ";
        $query .= "      AND STF.SEMESTER         = CHAIR.SEMESTER ";
        $query .= "      AND STF.CHAIRCD          = CHAIR.CHAIRCD ";
        $query .= "    LEFT JOIN V_SUBCLASS_MST VSUB ";
        $query .= "       ON VSUB.YEAR            = CHAIR.YEAR ";
        $query .= "      AND VSUB.CLASSCD         = CHAIR.CLASSCD ";
        $query .= "      AND VSUB.SCHOOL_KIND     = CHAIR.SCHOOL_KIND ";
        $query .= "      AND VSUB.CURRICULUM_CD   = CHAIR.CURRICULUM_CD ";
        $query .= "      AND VSUB.SUBCLASSCD      = CHAIR.SUBCLASSCD ";
        $query .= "    WHERE ";
        $query .= "          CHAIR.SEMESTER       = {$model->semester} ";
        // $query .= "      AND SUBSTR(CHAIR.SUBCLASSCD, 1, 2) IN (SELECT N1.NAMECD2 FROM V_NAME_MST N1 WHERE N1.YEAR = '".CTRL_YEAR."' AND N1.NAMECD1 = '{$namecd1}') ";
        $query .= "      AND CHAIR.CLASSCD    = '90' ";
        $query .= "      AND CHAIR.SCHOOL_KIND    = '{$model->field["SCHOOL_KIND"]}' ";
        //権限(制限付)
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "        AND STF.STAFFCD = '".STAFFCD."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //講座データの取得
    function selectChairQuery($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     CHAIR.CHAIRCD AS VALUE, ";
        $query .= "     CHAIR.CHAIRCD || ' ' || VALUE(CHAIR.CHAIRNAME,'') AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT CHAIR ";
        $query .= " INNER JOIN CHAIR_STF_DAT STF ";
        $query .= "      ON STF.YEAR       = CHAIR.YEAR ";
        $query .= "     AND STF.SEMESTER   = CHAIR.SEMESTER ";
        $query .= "     AND STF.CHAIRCD    = CHAIR.CHAIRCD ";
        $query .= " WHERE ";
        $query .= "         CHAIR.SEMESTER = {$model->semester} ";
        $query .= "     AND CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || '-' || CHAIR.CURRICULUM_CD || '-' || CHAIR.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        //権限(制限付)
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "        AND STF.STAFFCD = '".STAFFCD."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期取得
    function getSemester($model) {
        $query  = " SELECT ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";

        return $query;
    }

    //学期コース毎定型文取得
    function getHtrainremarkTempSemesCourseDat($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR, ";
        $query .= "     GRADE, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     COURSECODE, ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_TEMP_SEMES_COURSE_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '9' ";

        return $query;
    }

    //全体のデータの取得
    function selectQuery($model, $execute_date) {
        $query  = "    SELECT ";
        $query .= "      REGD.SCHREGNO, ";
        $query .= "      REGD.GRADE, ";
        $query .= "      HDAT.HR_NAME, ";
        $query .= "      REGD.ATTENDNO, ";
        $query .= "      BASE.NAME AS NAME_SHOW, ";
        $query .= "      SCORE_D.SCORE AS PATTERN_CD, ";

        $query .= "      REGD.COURSECD, ";
        $query .= "      REGD.MAJORCD, ";
        $query .= "      REGD.COURSECODE, ";
        $query .= "      SCORE_D.CLASSCD, ";
        $query .= "      SCORE_D.SCHOOL_KIND, ";
        $query .= "      SCORE_D.CURRICULUM_CD, ";
        $query .= "      SCORE_D.SUBCLASSCD, ";

        $query .= "      TEMP.REMARK ";
        $query .= "    FROM ";
        $query .= "      CHAIR_STD_DAT CHR_STD ";
        $query .= "    INNER JOIN SCHREG_REGD_DAT REGD ";
        $query .= "       ON CHR_STD.YEAR         = REGD.YEAR ";
        $query .= "      AND CHR_STD.SEMESTER     = REGD.SEMESTER ";
        $query .= "      AND CHR_STD.SCHREGNO     = REGD.SCHREGNO ";
        $query .= "    INNER JOIN CHAIR_DAT CHAIR ";
        $query .= "       ON CHAIR.YEAR           = CHR_STD.YEAR ";
        $query .= "      AND CHAIR.SEMESTER       = CHR_STD.SEMESTER ";
        $query .= "      AND CHAIR.CHAIRCD        = CHR_STD.CHAIRCD ";
        $query .= "    LEFT JOIN RECORD_SCORE_DAT SCORE_D ";
        $query .= "       ON SCORE_D.YEAR            = CHAIR.YEAR ";
        $query .= "      AND SCORE_D.SEMESTER        = CHAIR.SEMESTER ";
        $query .= "      AND SCORE_D.TESTKINDCD      = '99' ";
        $query .= "      AND SCORE_D.TESTITEMCD      = '00' ";
        $query .= "      AND SCORE_D.SCORE_DIV       = '09' ";
        $query .= "      AND SCORE_D.CLASSCD         = CHAIR.CLASSCD ";
        $query .= "      AND SCORE_D.SCHOOL_KIND     = CHAIR.SCHOOL_KIND ";
        $query .= "      AND SCORE_D.CURRICULUM_CD   = CHAIR.CURRICULUM_CD ";
        $query .= "      AND SCORE_D.SUBCLASSCD      = CHAIR.SUBCLASSCD ";
        $query .= "      AND SCORE_D.SCHREGNO        = CHR_STD.SCHREGNO ";
        $query .= "    LEFT JOIN HTRAINREMARK_TEMP_SEMES_DAT TEMP ";
        $query .= "       ON TEMP.YEAR            = REGD.YEAR ";
        $query .= "      AND TEMP.SEMESTER        = '9' ";
        $query .= "      AND TEMP.GRADE           = REGD.GRADE ";
        $query .= "      AND TEMP.DATA_DIV        = '{$model->datadiv}' ";
        $query .= "      AND cast(TEMP.PATTERN_CD as varchar) = SCORE_D.SCORE ";
        $query .= "    LEFT JOIN SCHREG_BASE_MST BASE ";
        $query .= "      ON BASE.SCHREGNO         = REGD.SCHREGNO ";
        $query .= "    LEFT JOIN SCHREG_REGD_HDAT HDAT ";
        $query .= "       ON HDAT.YEAR            = REGD.YEAR ";
        $query .= "      AND HDAT.SEMESTER        = REGD.SEMESTER ";
        $query .= "      AND HDAT.GRADE           = REGD.GRADE ";
        $query .= "      AND HDAT.HR_CLASS        = REGD.HR_CLASS ";
        $query .= "    LEFT JOIN V_SUBCLASS_MST VSUB ";
        $query .= "       ON VSUB.YEAR            = CHAIR.YEAR ";
        $query .= "      AND VSUB.CLASSCD         = CHAIR.CLASSCD ";
        $query .= "      AND VSUB.SCHOOL_KIND     = CHAIR.SCHOOL_KIND ";
        $query .= "      AND VSUB.CURRICULUM_CD   = CHAIR.CURRICULUM_CD ";
        $query .= "      AND VSUB.SUBCLASSCD      = CHAIR.SUBCLASSCD ";
        $query .= "    WHERE ";
        $query .= "          CHR_STD.YEAR         = '".CTRL_YEAR."' ";
        $query .= "      AND CHR_STD.SEMESTER     = '{$model->semester}' ";
        $query .= "      AND CHR_STD.CHAIRCD      = '{$model->field["CHAIRCD"]}' ";
        $query .= "      AND '".$execute_date."'  BETWEEN CHR_STD.APPDATE AND CHR_STD.APPENDDATE ";
        $query .= "    ORDER BY ";
        $query .= "      REGD.GRADE, ";
        $query .= "      REGD.HR_CLASS, ";
        $query .= "      REGD.ATTENDNO ";

        return $query;
    }

    /* データ更新処理 */
    function update($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $year = CTRL_YEAR;
        list($classcd, $school_kind, $curriculum_cd, $subclasscd) = preg_split('/-/', $model->field["SUBCLASSCD"]);

        foreach ($model->data["SCHREGNO"] as $schregno) {
            //項目
            $data = array();
            $data["YEAR"][TEXT]             = $year;
            $data["SEMESTER"][TEXT]         = $model->semester;
            $data["CLASSCD"][TEXT]          = $classcd;
            $data["TESTKINDCD"][TEXT]       = '99';
            $data["TESTITEMCD"][TEXT]       = '00';
            $data["SCORE_DIV"][TEXT]        = '09';
            $data["SCHOOL_KIND"][TEXT]      = $school_kind;
            $data["CURRICULUM_CD"][TEXT]    = $curriculum_cd;
            $data["SUBCLASSCD"][TEXT]       = $subclasscd;
            $data["SCHREGNO"][TEXT]         = $schregno;
            $data["SCORE"][TEXT]            = $model->fields["PATTERN_CD_".$schregno];
            $data["REGISTERCD"][TEXT]       = STAFFCD ;
            $data["UPDATED"][FUNC]          = "sysdate()";
            //存在チェック
            $query  = "SELECT COUNT(*) FROM RECORD_SCORE_DAT";
            $query .= " WHERE YEAR          = '{$year}'";
            $query .= "   AND SEMESTER      = '{$model->semester}'";
            $query .= "   AND TESTKINDCD    = '99' ";
            $query .= "   AND TESTITEMCD    = '00' ";
            $query .= "   AND SCORE_DIV     = '09' ";
            $query .= "   AND CLASSCD       = '{$classcd}' ";
            $query .= "   AND SCHOOL_KIND   = '{$school_kind}' ";
            $query .= "   AND CURRICULUM_CD = '{$curriculum_cd}' ";
            $query .= "   AND SUBCLASSCD    = '{$subclasscd}' ";
            $query .= "   AND SCHREGNO      = '{$schregno}'";
            if (0 < $db->getOne($query)) {
                //更新
                $where  = " WHERE YEAR          = '{$year}'";
                $where .= "   AND SEMESTER      = '{$model->semester}'";
                $where .= "   AND TESTKINDCD    = '99' ";
                $where .= "   AND TESTITEMCD    = '00' ";
                $where .= "   AND SCORE_DIV     = '09' ";
                $where .= "   AND CLASSCD       = '{$classcd}' ";
                $where .= "   AND SCHOOL_KIND   = '{$school_kind}' ";
                $where .= "   AND CURRICULUM_CD = '{$curriculum_cd}' ";
                $where .= "   AND SUBCLASSCD    = '{$subclasscd}' ";
                $where .= "   AND SCHREGNO      = '{$schregno}'";
                $query = Query::updateSQL($data, "RECORD_SCORE_DAT", $where);
            } else {
                //追加
                $query = Query::insertSQL($data, "RECORD_SCORE_DAT");
            }
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

}
?>
