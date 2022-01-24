<?php

require_once('for_php7.php');

class knjb1257Query extends Query {
    //処理年度
    function getExeYear($model)
    {
        $query  = " WITH EXE_YEAR (LABEL, VALUE) AS ( ";
        if ($model->search_div == "1") {
            $query .="     VALUES (".(CTRL_YEAR + 1).",".(CTRL_YEAR + 1).")";
        } else {
            $query .="     VALUES (".CTRL_YEAR.",".CTRL_YEAR."), (".(CTRL_YEAR + 1).",".(CTRL_YEAR + 1).")";
        }
        $query .="     ) ";
        $query .=" SELECT ";
        $query .="     * ";
        $query .=" FROM ";
        $query .="     EXE_YEAR ";
        $query .=" ORDER BY ";
        $query .="     VALUE DESC ";

        return $query;
    }

    //学期を取得
    function getSemester($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //履修履歴
    function getRirekiCode($model)
    {
        $query  = " SELECT ";
        $query .= "     RIREKI_CODE AS VALUE, ";
        $query .= "     SELECT_NAME || ' ' || CHAR(REPLACE(CHAR(SELECT_DATE), '-', '/')) AS LABEL ";
        $query .= " FROM ";
        $query .= "     STUDY_SELECT_DATE_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /* 学級名称取得 */
    function getScregRegdHdat($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     REG_H.HR_NAME AS LABEL, ";
        $query .= "     REG_H.GRADE || '_' || REG_H.HR_CLASS AS VALUE     ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT REG_H ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON REG_H.YEAR = REG_G.YEAR ";
                $query .= "           AND REG_H.GRADE = REG_G.GRADE ";
                $query .= "           AND REG_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON REG_H.YEAR = REG_G.YEAR ";
            $query .= "           AND REG_H.GRADE = REG_G.GRADE ";
            $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     REG_H.YEAR = '".$model->year."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //グループ名の取得
    function getGroupCnt($model) {
        $query  = " WITH COURSE_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.GRADE || '_' || T1.COURSECD || '_' || T1.MAJORCD || '_' || T1.COURSECODE || '_' || L1.SCHOOL_KIND AS G_COURSE_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.GRADE = L1.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
        $query .= "     AND T1.GRADE || '_' || T1.HR_CLASS = '{$model->field["GRADE_CLASS"]}' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.GROUPCD, ";
        $query .= "     L1.NAME, ";
        $query .= "     COUNT(*) AS COUNT ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_COMP_SELECT_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SUBCLASS_COMP_SELECT_MST L1 ON  L1.YEAR = T1.YEAR ";
        $query .= "                                 AND L1.GRADE = T1.GRADE ";
        $query .= "                                 AND L1.COURSECD = T1.COURSECD ";
        $query .= "                                 AND L1.MAJORCD = T1.MAJORCD ";
        $query .= "                                 AND L1.COURSECODE = T1.COURSECODE ";
        $query .= "                                 AND L1.GROUPCD = T1.GROUPCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' AND ";
        $query .= "     T1.GRADE || '_' || T1.COURSECD || '_' || T1.MAJORCD || '_' || T1.COURSECODE || '_' || T1.SCHOOL_KIND ";
        $query .= "         IN (SELECT G_COURSE_KIND FROM COURSE_T) ";
        if ($model->field["GROUP_SELECT"] == "1") {
            $query .= "     AND T1.GROUPCD < '600' ";
        } else {
            $query .= "     AND T1.GROUPCD >= '600' ";
        }
        $query .= " GROUP BY ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.GROUPCD, ";
        $query .= "     L1.NAME ";
        $query .= " ORDER BY ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.GROUPCD, ";
        $query .= "     L1.NAME ";

        return $query;
    }

    //科目名の取得
    function getSubclassName($model) {
        $query  = " WITH COURSE_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.GRADE || '_' || T1.COURSECD || '_' || T1.MAJORCD || '_' || T1.COURSECODE || '_' || L1.SCHOOL_KIND AS G_COURSE_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.GRADE = L1.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
        $query .= "     AND T1.GRADE || '_' || T1.HR_CLASS = '{$model->field["GRADE_CLASS"]}' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.GROUPCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
        } else {
            $query .= "     T1.SUBCLASSCD, ";
        }
        $query .= "     L1.SUBCLASSABBV ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_COMP_SELECT_DAT T1 ";
        $query .= " LEFT JOIN ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SUBCLASS_MST L1 ON L1.CLASSCD       = T1.CLASSCD ";
            $query .= "                    AND L1.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "                    AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
            $query .= "                    AND L1.SUBCLASSCD    = T1.SUBCLASSCD ";
        } else {
            $query .= "     SUBCLASS_MST L1 ON L1.SUBCLASSCD = T1.SUBCLASSCD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
        $query .= "     AND T1.GRADE || '_' || T1.COURSECD || '_' || T1.MAJORCD || '_' || T1.COURSECODE || '_' || T1.SCHOOL_KIND ";
        $query .= "         IN (SELECT G_COURSE_KIND FROM COURSE_T) ";
        if ($model->field["GROUP_SELECT"] == "1") {
            $query .= "     AND T1.GROUPCD < '600' ";
        } else {
            $query .= "     AND T1.GROUPCD >= '600' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.GROUPCD, ";
        $query .= "     T1.CLASSCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD ";

        return $query;
    }

    //リスト
    function getList($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     L1.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_BASE_MST L1 ON  L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '". $model->year ."' ";
        $query .= "     AND T1.SEMESTER = '". $model->semester ."' ";
        $query .= "     AND T1.GRADE    = '{$model->field["GRADE"]}' ";
        $query .= "     AND T1.HR_CLASS = '{$model->field["HR_CLASS"]}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //SUBCLASS_STD_SELECT_RIREKI_DATにデータがあるのか確認する(チェックボックスにチェックをつけるため)
    function checkSubclassStdSelectDat($groupcd, $subclasscd, $schregno, $model) {
        if ($model->Properties["useCurriculumcd"] == '1') {
            list($classcd, $school_kind, $curriculum_cd, $subclasscd) = preg_split("/-/", $subclasscd);
        } else {
            $classcd = substr($subclasscd, 0, 2);
        }

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_STD_SELECT_RIREKI_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR          = '". $model->year ."' AND ";
        $query .= "     SEMESTER      = '". $model->semester ."' AND ";
        $query .= "     RIREKI_CODE   = '". $model->rirekiCode ."' AND ";
        $query .= "     GROUPCD       = '{$groupcd}' AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD         = '{$classcd}' AND ";
            $query .= "     SCHOOL_KIND     = '{$school_kind}' AND ";
            $query .= "     CURRICULUM_CD   = '{$curriculum_cd}' AND ";
            $query .= "     SUBCLASSCD      = '{$subclasscd}' AND ";
        } else {
            $query .= "     CLASSCD       = '{$classcd}' AND ";
            $query .= "     CURRICULUM_CD = '2' AND ";
            $query .= "     SUBCLASSCD    = '{$subclasscd}' AND ";
        }
        $query .= "     SCHREGNO      = '{$schregno}' ";

        return $query;
    }

    /**************/
    /* インサート */
    /**************/
    function getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $delSub = "(";
        $delSep = "";
        foreach ($model->listArray as $listKey => $listVal) {
            $delSub .= $delSep."'{$listVal["SUBCLASSCD"]}'";
            $delSep = ",";
        }
        $delSub = $delSep == "" ? "('')" : $delSub.")";

        foreach ($model->field["SCHREGNO_ARRAY"] as $schregno) {
           //まずは削除
            $query = '';
            //SUBCLASS_STD_SELECT_RIREKI_DATの削除
            $query  = " DELETE FROM ";
            $query .= "     SUBCLASS_STD_SELECT_RIREKI_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR     = '". $model->year ."' ";
            $query .= "     AND SEMESTER = '". $model->semester ."' ";
            $query .= "     AND RIREKI_CODE = '". $model->rirekiCode ."' ";
            $query .= "     AND SCHREGNO = '{$schregno}' ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD IN {$delSub} ";
            } else {
                $query .= "     AND SCHREGNO IN {$delSub} ";
            }

            $db->query($query);
        }

         foreach ($model->field as $field_key => $field_val) {
            if (function_exists("mbereg")) {
                if (!mbereg('^SELECTDATA', $field_key)) {
                    continue;
                }
            } else {
                if (!mb_ereg('^SELECTDATA', $field_key)) {
                    continue;
                }
            }
            list($dummy, $schregno, $groupcd, $subclasscd) =explode("_",$field_key);
            if ($model->Properties["useCurriculumcd"] == '1') {
                list($classcd, $school_kind, $curriculum_cd, $subclasscd) = preg_split("/-/", $subclasscd);
            } else {
                $classcd = substr($subclasscd, 0, 2);
            }

            $data = array();
            //SUBCLASS_STD_SELECT_RIREKI_DATの更新
            $data["YEAR"][TEXT]          = $model->year;
            $data["SEMESTER"][TEXT]      = $model->semester;
            $data["RIREKI_CODE"][TEXT]   = $model->rirekiCode;
            $data["GROUPCD"][TEXT]       = $groupcd;
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["CLASSCD"][TEXT]       = $classcd;
                $data["SCHOOL_KIND"][TEXT]   = $school_kind;
                $data["CURRICULUM_CD"][TEXT] = $curriculum_cd;
                $data["SUBCLASSCD"][TEXT]    = $subclasscd;
            } else {
                $data["CLASSCD"][TEXT]       = $classcd;
                $data["CURRICULUM_CD"][TEXT] = "2";
                $data["SUBCLASSCD"][TEXT]    = $subclasscd;
            }
            $data["SCHREGNO"][TEXT]      = $schregno;
            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][NUMBER]     = "sysdate()";

            $query = Query::insertSQL($data, "SUBCLASS_STD_SELECT_RIREKI_DAT");
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

}
?>
