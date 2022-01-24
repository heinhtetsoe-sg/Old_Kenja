<?php

require_once('for_php7.php');

class knjb0011Query extends Query {
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

    //コースを取得
    function getCourse($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T4.GRADE || '-' || T4.COURSECD || '-' || T4.MAJORCD || '-' || T4.COURSECODE AS VALUE, ";
        $query .= "     T5.GRADE_NAME1 || '　(' || T4.COURSECD || T4.MAJORCD || ')　' || T2.COURSENAME || T3.MAJORNAME || '　(' || T4.COURSECODE || ')　' || T1.COURSECODENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COURSECODE_MST T1, ";
        $query .= "     COURSE_MST T2, ";
        $query .= "     MAJOR_MST T3, ";
        $query .= "     SCHREG_REGD_DAT T4, ";
        $query .= "     SCHREG_REGD_GDAT T5 ";
        $query .= " WHERE ";
        $query .= "     T4.YEAR         ='{$model->year}' AND ";
        $query .= "     T4.SEMESTER     ='{$model->semester}' AND ";
        $query .= "     T1.COURSECODE   = T4.COURSECODE AND ";
        $query .= "     T2.COURSECD     = T4.COURSECD AND ";
        $query .= "     T3.COURSECD     = T4.COURSECD AND ";
        $query .= "     T4.MAJORCD      = T3.MAJORCD AND ";
        $query .= "     T4.YEAR         = T5.YEAR AND ";
        $query .= "     T4.GRADE        = T5.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T5.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T5.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期終了日を取得
    function getSemesterEndDate($model, $grade) {
        $query  = " SELECT ";
        $query .= "     EDATE, ";
        $query .= "     SDATE ";
        $query .= " FROM ";
        $query .= "     V_SEMESTER_GRADE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $query .= "     AND GRADE = '{$grade}' ";

        return $query;
    }

    //教科一覧
    function getClassMst($model) {
        $query  = " SELECT ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND AS VALUE, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_CLASS_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        if (AUTHORITY == DEF_UPDATE_RESTRICT || AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= " AND T1.CLASSCD || T1.SCHOOL_KIND IN ( ";
            $query .= "     SELECT ";
            $query .= "         I1.CLASSCD || I1.SCHOOL_KIND ";
            $query .= "     FROM ";
            $query .= "         CHAIR_STF_DAT CHAIR_STF ";
            $query .= "         INNER JOIN CHAIR_DAT I1 ON CHAIR_STF.YEAR = I1.YEAR ";
            $query .= "               AND CHAIR_STF.SEMESTER = I1.SEMESTER ";
            $query .= "               AND CHAIR_STF.CHAIRCD = I1.CHAIRCD ";
            $query .= "     WHERE ";
            $query .= "         CHAIR_STF.YEAR = '{$model->year}' ";
            $query .= "         AND CHAIR_STF.SEMESTER = '{$model->semester}' ";
            $query .= "         AND CHAIR_STF.STAFFCD = '".STAFFCD."' ";
            $query .= " ) ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //グループ名の取得
    function getGroupCnt($model) {
        $query  = "SELECT ";
        if ($model->selectGroup == "1") {
            $query .= "    t1.classcd, ";
            $query .= "    T1.SCHOOL_KIND, ";
            $query .= "    t1.classcd || '-' || T1.SCHOOL_KIND || ':' || w3.classname as groupname, ";
        } else {
            $query .= "    t1.groupcd, ";
            $query .= "    t1.groupcd || ':' || w1.groupname as groupname, ";
        }
        $query .= "     COUNT(*) AS CNT ";
        $query .= "FROM ";
        $query .= "    chair_dat t1 left join v_electclass_mst w1 on w1.year=t1.year and w1.groupcd=t1.groupcd ";
        $query .= "    left join v_subclass_mst w2 on w2.year=t1.year and w2.classcd=t1.classcd and w2.school_kind=t1.school_kind and ";
        $query .= "                                   w2.curriculum_cd=t1.curriculum_cd  and w2.subclasscd=t1.subclasscd ";
        $query .= "    left join v_class_mst w3 on w3.year=t1.year and w3.classcd=t1.classcd and ";
        $query .= "                                w3.school_kind=t1.school_kind ";
        $query .= "WHERE ";
        $query .= "    t1.year='{$model->year}' AND ";
        $query .= "    t1.semester='{$model->semester}' AND ";
        if ($model->selectClass != "ALL") {
            $query .= "    T1.CLASSCD || '-' || T1.SCHOOL_KIND ='{$model->selectClass}' AND ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "    T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') AND ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "    T1.SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        if (AUTHORITY == DEF_UPDATE_RESTRICT || AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= "     T1.CLASSCD || T1.SCHOOL_KIND IN ( ";
            $query .= "         SELECT ";
            $query .= "             I1.CLASSCD || I1.SCHOOL_KIND ";
            $query .= "         FROM ";
            $query .= "             CHAIR_STF_DAT CHAIR_STF ";
            $query .= "             INNER JOIN CHAIR_DAT I1 ON CHAIR_STF.YEAR = I1.YEAR ";
            $query .= "                   AND CHAIR_STF.SEMESTER = I1.SEMESTER ";
            $query .= "                   AND CHAIR_STF.CHAIRCD = I1.CHAIRCD ";
            $query .= "         WHERE ";
            $query .= "             CHAIR_STF.YEAR = '{$model->year}' ";
            $query .= "             AND CHAIR_STF.SEMESTER = '{$model->semester}' ";
            $query .= "             AND CHAIR_STF.STAFFCD = '".STAFFCD."' ";
            $query .= "     ) AND ";
        }
        //絞込み（教科の場合、群コード：０）（群の場合、群コード：０以外）
        if ($model->selectGroup == "1") {
            $query .= "    t1.groupcd = '0000' ";
            $query .= " GROUP BY ";
            $query .= "     t1.classcd, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     w3.classname ";
            $query .= " ORDER BY ";
            $query .= "     t1.classcd, ";
            $query .= "     T1.SCHOOL_KIND ";
        } else {

            $query .= "    T1.GROUPCD <> '0000' ";
            $query .= " GROUP BY ";
            $query .= "     t1.GROUPCD, ";
            $query .= "     w1.GROUPNAME ";
            $query .= " ORDER BY ";
            $query .= "     t1.GROUPCD ";
        }

        return $query;
    }

    //講座名の取得
    function getChairName($model) {
        $query  = "SELECT ";
        $query .= "    t1.chaircd, ";
        $query .= "    t1.groupcd, ";
        if ($model->selectGroup == "1") {
            $query .= "    w3.classname as groupname, ";
        } else {
            $query .= "    w1.groupname, ";
        }
        $query .= "    t1.classcd, ";
        $query .= "    t1.school_kind, ";
        $query .= "    t1.curriculum_cd, ";
        $query .= "    t1.subclasscd, ";
        $query .= "    w2.subclassname, ";
        $query .= "    t1.chairname ";
        $query .= "FROM ";
        $query .= "    chair_dat t1 left join v_electclass_mst w1 on w1.year=t1.year and w1.groupcd=t1.groupcd ";
        $query .= "    left join v_subclass_mst w2 on w2.year=t1.year and w2.classcd=t1.classcd and w2.school_kind=t1.school_kind and ";
        $query .= "                                   w2.curriculum_cd=t1.curriculum_cd  and w2.subclasscd=t1.subclasscd ";
        $query .= "    left join v_class_mst w3 on w3.year=t1.year and w3.classcd=t1.classcd and ";
        $query .= "                                w3.school_kind=t1.school_kind ";
        $query .= "WHERE ";
        $query .= "    t1.year='{$model->year}' AND ";
        $query .= "    t1.semester='{$model->semester}' AND ";
        if ($model->selectClass != "ALL") {
            $query .= "    T1.CLASSCD || '-' || T1.SCHOOL_KIND ='{$model->selectClass}' AND ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "    T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') AND ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "    T1.SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        if (AUTHORITY == DEF_UPDATE_RESTRICT || AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= "     T1.CLASSCD || T1.SCHOOL_KIND IN ( ";
            $query .= "         SELECT ";
            $query .= "             I1.CLASSCD || I1.SCHOOL_KIND ";
            $query .= "         FROM ";
            $query .= "             CHAIR_STF_DAT T1 ";
            $query .= "             INNER JOIN CHAIR_DAT I1 ON T1.YEAR = I1.YEAR ";
            $query .= "                   AND T1.SEMESTER = I1.SEMESTER ";
            $query .= "                   AND T1.CHAIRCD = I1.CHAIRCD ";
            $query .= "         WHERE ";
            $query .= "             T1.YEAR = '{$model->year}' ";
            $query .= "             AND T1.SEMESTER = '{$model->semester}' ";
            $query .= "             AND T1.STAFFCD = '".STAFFCD."' ";
            $query .= "     ) AND ";
        }
        //絞込み（教科の場合、群コード：０）（群の場合、群コード：０以外）
        if ($model->selectGroup == "1") {
            $query .= "    t1.groupcd = '0000' ";
            $query .= " ORDER BY ";
            $query .= "     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD, ";
            $query .= "     T1.CHAIRCD ";
        } else {
            $query .= "    T1.GROUPCD <> '0000' ";
            $query .= " ORDER BY ";
            $query .= "     T1.GROUPCD, ";
            $query .= "     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD, ";
            $query .= "     T1.CHAIRCD ";
        }

        return $query;
    }

    /* 名簿 */
    function getChairStd($model, $schregno) {
        $query .= " SELECT ";
        $query .= "     CSTD.CHAIRCD, ";
        $query .= "     CSTD.APPDATE ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT CSTD ";
        $query .= " WHERE ";
        $query .= "     CSTD.YEAR = '{$model->year}' ";
        $query .= "     AND CSTD.SEMESTER = '{$model->semester}' ";
        $query .= "     AND CSTD.SCHREGNO = '{$schregno}' ";
        $query .= "     AND CSTD.APPENDDATE = '{$model->setEndDate}' ";
        $query .= " ORDER BY ";
        $query .= "     CSTD.CHAIRCD ";

        return $query;
    }

    //リスト
    function getList($model, $schregNo = "") {
        $query .= " SELECT DISTINCT ";
        $query .= "     REGH.GRADE, ";
        $query .= "     REGH.HR_CLASS, ";
        $query .= "     REGH.HR_NAME, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     BASE.SCHREGNO, ";
        $query .= "     BASE.NAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_CLS_DAT CHAIR_C ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ON CHAIR_C.YEAR = REGD.YEAR ";
        $query .= "           AND CHAIR_C.SEMESTER = REGD.SEMESTER ";
        if ($schregNo) {
            $query .= "           AND REGD.SCHREGNO = '{$schregNo}' ";
        }
        $query .= "           AND CHAIR_C.TRGTGRADE = REGD.GRADE ";
        $query .= "           AND CHAIR_C.TRGTCLASS = REGD.HR_CLASS ";
        if ($model->course != "ALL") {
            $query .= "           AND REGD.GRADE || '-' || REGD.COURSECD || '-' || REGD.MAJORCD || '-' || REGD.COURSECODE = '{$model->course}' ";
        }
        $query .= "     INNER JOIN SCHREG_REGD_HDAT REGH ON REGD.YEAR = REGH.YEAR ";
        $query .= "           AND REGD.SEMESTER = REGH.SEMESTER ";
        $query .= "           AND REGD.GRADE = REGH.GRADE ";
        $query .= "           AND REGD.HR_CLASS = REGH.HR_CLASS ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ON REGD.YEAR = GDAT.YEAR ";
                $query .= "           AND REGD.GRADE = GDAT.GRADE ";
                $query .= "           AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ON REGD.YEAR = GDAT.YEAR ";
            $query .= "           AND REGD.GRADE = GDAT.GRADE ";
            $query .= "           AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     CHAIR_C.YEAR = '{$model->year}' ";
        $query .= "     AND CHAIR_C.SEMESTER = '{$model->semester}' ";
        if ($model->groupInstate) {
            if ($model->selectGroup == "1") {
                $query .= "     AND CHAIR_C.CHAIRCD IN (SELECT ";
                $query .= "                                 CHAIRCD ";
                $query .= "                             FROM ";
                $query .= "                                 CHAIR_DAT CHAIR_D ";
                $query .= "                             WHERE ";
                $query .= "                                 CHAIR_C.YEAR        = CHAIR_D.YEAR AND ";
                $query .= "                                 CHAIR_C.SEMESTER    = CHAIR_D.SEMESTER AND ";
                $query .= "                                 CHAIR_C.CHAIRCD     = CHAIR_D.CHAIRCD AND ";
                $query .= "                                 CHAIR_C.GROUPCD     = '0000' AND ";
                $query .= "                                 CHAIR_D.CLASSCD || '-' || CHAIR_D.SCHOOL_KIND IN ({$model->groupInstate})) ";
            } else {
                $query .= "     AND CHAIR_C.GROUPCD IN ({$model->groupInstate}) ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     REGH.GRADE, ";
        $query .= "     REGH.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO ";

        return $query;
    }

    //SUBCLASS_STD_SELECT_RIREKI_DATにデータがあるのか確認する(チェックボックスにチェックをつけるため)
    function checkSubclassStdSelectDat($groupcd, $subclasscd, $schregno, $model) {
        list($classcd, $school_kind, $curriculum_cd, $subclasscd) = preg_split("/-/", $subclasscd);

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_STD_SELECT_RIREKI_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR          = '". $model->year ."' AND ";
        $query .= "     SEMESTER      = '". $model->semester ."' AND ";
        $query .= "     RIREKI_CODE   = '". $model->rirekiCode ."' AND ";
        $query .= "     GROUPCD       = '{$groupcd}' AND ";
        $query .= "     CLASSCD         = '{$classcd}' AND ";
        $query .= "     SCHOOL_KIND     = '{$school_kind}' AND ";
        $query .= "     CURRICULUM_CD   = '{$curriculum_cd}' AND ";
        $query .= "     SUBCLASSCD      = '{$subclasscd}' AND ";
        $query .= "     SCHREGNO      = '{$schregno}' ";

        return $query;
    }

    /* 生徒情報 */
    function getSchInfo($model) {
        $query .= " SELECT DISTINCT ";
        $query .= "     REGH.GRADE, ";
        $query .= "     REGH.HR_CLASS, ";
        $query .= "     REGH.HR_NAME, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     BASE.SCHREGNO, ";
        $query .= "     BASE.NAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_CLS_DAT CHAIR_C ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ON CHAIR_C.YEAR = REGD.YEAR ";
        $query .= "           AND CHAIR_C.SEMESTER = REGD.SEMESTER ";
        $query .= "           AND CHAIR_C.TRGTGRADE = REGD.GRADE ";
        $query .= "           AND CHAIR_C.TRGTCLASS = REGD.HR_CLASS ";
        $query .= "           AND REGD.SCHREGNO = '{$model->schregNo}' ";
        if ($model->course != "ALL") {
            $query .= "           AND REGD.GRADE || '-' || REGD.COURSECD || '-' || REGD.MAJORCD || '-' || REGD.COURSECODE = '{$model->course}' ";
        }
        $query .= "     INNER JOIN SCHREG_REGD_HDAT REGH ON REGD.YEAR = REGH.YEAR ";
        $query .= "           AND REGD.SEMESTER = REGH.SEMESTER ";
        $query .= "           AND REGD.GRADE = REGH.GRADE ";
        $query .= "           AND REGD.HR_CLASS = REGH.HR_CLASS ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ON REGD.YEAR = GDAT.YEAR ";
                $query .= "           AND REGD.GRADE = GDAT.GRADE ";
                $query .= "           AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ON REGD.YEAR = GDAT.YEAR ";
            $query .= "           AND REGD.GRADE = GDAT.GRADE ";
            $query .= "           AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     CHAIR_C.YEAR = '{$model->year}' ";
        $query .= "     AND CHAIR_C.SEMESTER = '{$model->semester}' ";
        if ($model->groupInstate) {
            if ($model->selectGroup == "1") {
                $query .= "     AND CHAIR_C.CHAIRCD IN (SELECT ";
                $query .= "                                 CHAIRCD ";
                $query .= "                             FROM ";
                $query .= "                                 CHAIR_DAT CHAIR_D ";
                $query .= "                             WHERE ";
                $query .= "                                 CHAIR_C.YEAR        = CHAIR_D.YEAR AND ";
                $query .= "                                 CHAIR_C.SEMESTER    = CHAIR_D.SEMESTER AND ";
                $query .= "                                 CHAIR_C.CHAIRCD     = CHAIR_D.CHAIRCD AND ";
                $query .= "                                 CHAIR_C.GROUPCD     = '0000' AND ";
                $query .= "                                 CHAIR_D.CLASSCD || '-' || CHAIR_D.SCHOOL_KIND IN ({$model->groupInstate})) ";
            } else {
                $query .= "     AND CHAIR_C.GROUPCD IN ({$model->groupInstate}) ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     REGH.GRADE, ";
        $query .= "     REGH.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO ";

        return $query;
    }

    //名簿の終了日が学期最終日の講座を取得
    function getChaircd($year, $semester, $schregno) {
        $query  = " SELECT ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T1.APPDATE ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1, ";
        $query .= "     SEMESTER_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".$year."' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER = '".$semester."' AND ";
        $query .= "     T1.SCHREGNO = '".$schregno."' AND ";
        $query .= "     T1.APPENDDATE = T2.EDATE ";

        return $query;
    }

    //日付変更可能範囲取得
    function getDate($year, $semester, $schregno) {
        $query  = " WITH SEM AS ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         SEMESTER_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR     = '".$year."' AND ";
        $query .= "         SEMESTER = '".$semester."' ";
        $query .= " ), CHAIR_STD AS ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT T1 ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '".$year."' AND ";
        $query .= "         SEMESTER    = '".$semester."' AND ";
        $query .= "         SCHREGNO    = '".$schregno."' AND ";
        $query .= "         APPDATE     = ( SELECT ";
        $query .= "                             MAX(APPDATE) AS APPDATE ";
        $query .= "                         FROM ";
        $query .= "                             CHAIR_STD_DAT S1 ";
        $query .= "                         WHERE ";
        $query .= "                             T1.YEAR     = S1.YEAR AND ";
        $query .= "                             T1.SEMESTER = S1.SEMESTER AND ";
        $query .= "                             T1.CHAIRCD  = S1.CHAIRCD AND ";
        $query .= "                             T1.SCHREGNO = S1.SCHREGNO ";
        $query .= "                         ) ";
        $query .= " ), SCH_CHR AS ( ";
        $query .= "     SELECT ";
        $query .= "         CHAIRCD, ";
        $query .= "         EXECUTEDATE ";
        $query .= "     FROM ";
        $query .= "         SCH_CHR_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '".$year."' AND ";
        $query .= "         SEMESTER    = '".$semester."' AND ";
        $query .= "         EXECUTED    = '1' ";
        $query .= "     UNION  ";
        $query .= "     SELECT ";
        $query .= "         T1.CHAIRCD, ";
        $query .= "         T2.EXECUTEDATE ";
        $query .= "     FROM ";
        $query .= "         SCH_CHR_DAT T1, ";
        $query .= "         SCH_CHR_HRATE_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR         = '".$year."' AND ";
        $query .= "         T1.SEMESTER     = '".$semester."' AND ";
        $query .= "         T1.CHAIRCD      = T2.CHAIRCD AND ";
        $query .= "         T1.EXECUTEDATE  = T2.EXECUTEDATE AND ";
        $query .= "         T1.PERIODCD     = T2.PERIODCD AND ";
        $query .= "         T2.EXECUTED     = '1' AND ";
        $query .= "         EXISTS (SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_DAT S1 ";
        $query .= "                 WHERE ";
        $query .= "                     T1.YEAR     = S1.YEAR AND ";
        $query .= "                     T1.SEMESTER = S1.SEMESTER AND ";
        $query .= "                     T2.GRADE    = S1.GRADE AND ";
        $query .= "                     T2.HR_CLASS = S1.HR_CLASS AND ";
        $query .= "                     S1.SCHREGNO = '".$schregno."' ";
        $query .= "                 ) ";
        $query .= " ), ATTEND AS ( ";
        $query .= "     SELECT ";
        $query .= "         CHAIRCD, ";
        $query .= "         MAX(EXECUTEDATE) AS EXECUTEDATE ";
        $query .= "     FROM ";
        $query .= "         SCH_CHR ";
        $query .= "     GROUP BY ";
        $query .= "         CHAIRCD ";
        $query .= " ), SDATELIST AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.CHAIRCD, ";
        $query .= "         T2.SDATE ";
        $query .= "     FROM ";
        $query .= "         CHAIR_DAT T1, ";
        $query .= "         SEM T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = T2.YEAR AND ";
        $query .= "         T1.SEMESTER = T2.SEMESTER ";
        $query .= "     UNION    ";
        $query .= "     SELECT ";
        $query .= "         T1.CHAIRCD, ";
        $query .= "         CASE WHEN T2.EDATE != T1.APPENDDATE THEN (T1.APPENDDATE +1 DAY) ";
        $query .= "              ELSE T1.APPDATE END AS SDATE ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD T1, ";
        $query .= "         SEM T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = T2.YEAR AND ";
        $query .= "         T1.SEMESTER = T2.SEMESTER ";
        $query .= "     UNION       ";
        $query .= "     SELECT CHAIRCD, (EXECUTEDATE +1 DAY) AS SDATE FROM ATTEND ";
        $query .= " ), MAX_SDATE AS ( ";
        $query .= "     SELECT ";
        $query .= "         CHAIRCD, ";
        $query .= "         MAX(SDATE) AS SDATE ";
        $query .= "     FROM ";
        $query .= "         SDATELIST ";
        $query .= "     GROUP BY ";
        $query .= "         CHAIRCD ";
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "     T2.CHAIRCD, ";
        $query .= "     T2.SDATE, ";
        $query .= "     T1.EDATE ";
        $query .= " FROM ";
        $query .= "     SEM T1, ";
        $query .= "     MAX_SDATE T2 ";

        return $query;
    }

    /**************/
    /* インサート */
    /**************/
    function getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $appDate = str_replace("/", "-", $model->appDate);
        $cnt = 0;   //件数
        foreach ($model->chrList as $chrCd => $chrName) {
            //日付の変更可能範囲取得
            list ($sdate, $edate) = $model->chgOkDate[$chrCd];
            if ($sdate <= $appDate && $appDate <= $edate) {

                if ($model->flg_chk[$chrCd] == "1" && $model->flg_rgd[$chrCd] != "1") {
                    //追加
                    $data = array();
                    $data["YEAR"][TEXT]         = $model->year;
                    $data["SEMESTER"][TEXT]     = $model->semester;
                    $data["CHAIRCD"][TEXT]      = $chrCd;
                    $data["SCHREGNO"][TEXT]     = $model->schregNo;
                    $data["APPDATE"][TEXT]      = $appDate;
                    $data["APPENDDATE"][TEXT]   = $model->setEndDate;
                    $data["REGISTERCD"][TEXT]   = STAFFCD;
                    $data["UPDATED"][NUMBER]    = "sysdate()";

                    $query = Query::insertSQL($data, "CHAIR_STD_DAT");
                    $db->query($query);
                    $cnt++;
                } else if ($model->flg_chk[$chrCd] != "1" && $model->flg_rgd[$chrCd] == "1") {
                    //指定開始日が学期開始日または講座開始日と同じとき削除する
                    if (($appDate == $model->setStartDate) || ($appDate == $this->regd_chrAppdate[$chrCd])) {
                        $query  = " DELETE FROM ";
                        $query .= "     CHAIR_STD_DAT ";
                        $query .= " WHERE ";
                        $query .= "     YEAR        = '".$model->year."' AND ";
                        $query .= "     SEMESTER    = '".$model->semester."' AND ";
                        $query .= "     CHAIRCD     = '".$chrCd."' AND ";
                        $query .= "     SCHREGNO    = '".$model->schregNo."' AND ";
                        $query .= "     APPDATE     = '".$appDate."' ";
                        $db->query($query);
                        $cnt++;
                    } else {
                        //更新
                        $data = array();
                        $data["APPENDDATE"][TEXT]   = date("Y-m-d",strtotime("{$appDate} -1 day"));
                        $data["REGISTERCD"][TEXT]   = STAFFCD;
                        $data["UPDATED"][NUMBER]    = "SYSDATE()";

                        $where  = " WHERE ";
                        $where .= "     YEAR        = '".$model->year."' AND ";
                        $where .= "     SEMESTER    = '".$model->semester."' AND ";
                        $where .= "     CHAIRCD     = '".$chrCd."' AND ";
                        $where .= "     SCHREGNO    = '".$model->schregNo."' AND ";
                        $where .= "     APPENDDATE  = '".$model->setEndDate."' ";

                        $query = Query::updateSQL($data, "CHAIR_STD_DAT", $where);
                        $db->query($query);
                        $cnt++;
                    }
                }
            }
        }
        $db->commit();
        Query::dbCheckIn($db);
        return $cnt;
    }
}
?>
