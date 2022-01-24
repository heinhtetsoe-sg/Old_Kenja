<?php

require_once('for_php7.php');

class knjz234aQuery extends Query
{

    //対象学期コンボ
    public function getSemester()
    {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME AS LABEL, ";
        $query .= "     SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR      = '".CTRL_YEAR."' ";

        return $query;
    }

    //9学期以外の最終学期取得
    public function getSemesterLast()
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR      = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER DESC ";

        return $query;
    }

    //参照学期コンボ
    public function getSemester2($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     SEM.SEMESTERNAME AS LABEL, ";
        $query .= "     SEM.SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     CHAIR_GROUP_MST CG_M ";
        $query .= "     LEFT JOIN SEMESTER_MST SEM ";
        $query .= "          ON CG_M.YEAR     = SEM.YEAR ";
        $query .= "         AND CG_M.SEMESTER = SEM.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     CG_M.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND CG_M.SEMESTER <> '".$model->target_seme."' ";

        return $query;
    }

    //学籍在籍データの件数取得
    public function getRegdDatCnt($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
                $query .= "          ON G1.YEAR         = T1.YEAR ";
                $query .= "         AND G1.GRADE        = T1.GRADE ";
                $query .= "         AND G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
            $query .= "          ON G1.YEAR         = T1.YEAR ";
            $query .= "         AND G1.GRADE        = T1.GRADE ";
            $query .= "         AND G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '". CTRL_YEAR ."' ";

        return $query;
    }

    //コース名コンボ取得
    public function getCouseName($model, $flg)
    {
        if ($flg) {
            $query  = " SELECT DISTINCT ";
            $query .= "     T2.COURSECD, ";
            $query .= "     T3.MAJORCD, ";
            $query .= "     T1.COURSECODE, ";
            $query .= "     T4.GRADE, ";
            $query .= "     T4.YEAR, ";
            $query .= "     T4.GRADE_NAME1, ";
            $query .= "     T2.COURSENAME, ";
            $query .= "     T3.MAJORNAME, ";
            $query .= "     T1.COURSECODENAME ";
            $query .= " FROM ";
            $query .= "     COURSECODE_MST T1, ";
            $query .= "     COURSE_MST T2, ";
            $query .= "     MAJOR_MST T3, ";
            $query .= "     SCHREG_REGD_GDAT T4 ";
            $query .= " WHERE ";
            $query .= "     T4.YEAR     ='".CTRL_YEAR."' AND ";
            $query .= "     T2.COURSECD = T3.COURSECD ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND T4.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T4.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= " ORDER BY ";
            $query .= "     T4.GRADE, ";
            $query .= "     T2.COURSECD, ";
            $query .= "     T3.MAJORCD, ";
            $query .= "     T1.COURSECODE ";
        } else {
            $query  = " SELECT DISTINCT ";
            $query .= "     T4.COURSECD, ";
            $query .= "     T4.MAJORCD, ";
            $query .= "     T4.COURSECODE, ";
            $query .= "     T4.GRADE, ";
            $query .= "     T4.YEAR, ";
            $query .= "     L4.GRADE_NAME1, ";
            $query .= "     T2.COURSENAME, ";
            $query .= "     T3.MAJORNAME, ";
            $query .= "     T1.COURSECODENAME ";
            $query .= " FROM ";
            $query .= "     COURSECODE_MST T1, ";
            $query .= "     COURSE_MST T2, ";
            $query .= "     MAJOR_MST T3, ";
            $query .= "     SCHREG_REGD_DAT T4 ";
            $query .= "     INNER JOIN SCHREG_REGD_GDAT L4 ON L4.YEAR = T4.YEAR ";
            $query .= "                                  AND L4.GRADE = T4.GRADE ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "                              AND L4.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "                              AND L4.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= " WHERE ";
            $query .= "     T4.YEAR       ='".CTRL_YEAR."' AND ";
            $query .= "     T1.COURSECODE = T4.COURSECODE AND ";
            $query .= "     T2.COURSECD   = T4.COURSECD AND ";
            $query .= "     T3.COURSECD   = T4.COURSECD AND ";
            $query .= "     T4.MAJORCD    = T3.MAJORCD ";
            $query .= " ORDER BY ";
            $query .= "     T4.GRADE, ";
            $query .= "     T4.COURSECD, ";
            $query .= "     T4.MAJORCD, ";
            $query .= "     T4.COURSECODE ";
        }

        return $query;
    }

    //選択科目カウント
    public function getJyuhukuCnt($model, $data, $subclasscd_set, $flg)
    {

        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array       = array();
            $subclass_array       = explode('-', $subclasscd_set);
        }
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     CHAIR_GROUP_MST T1 ";
        if ($model->Properties["use_CHAIR_GROUP_SDIV_DAT"] === '1') {
            $query .= " LEFT JOIN CHAIR_GROUP_SDIV_DAT L1 ON L1.YEAR = T1.YEAR ";
            $query .= "                             AND L1.SEMESTER = T1.SEMESTER ";
            $query .= "                             AND L1.CHAIR_GROUP_CD = T1.CHAIR_GROUP_CD ";
        } else {
            $query .= " LEFT JOIN CHAIR_GROUP_DAT L1 ON L1.YEAR = T1.YEAR ";
            $query .= "                             AND L1.SEMESTER = T1.SEMESTER ";
            $query .= "                             AND L1.CHAIR_GROUP_CD = T1.CHAIR_GROUP_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= " AND T1.SEMESTER = '".$model->target_seme."' ";
        if ($flg == "CHAIR_GROUP_CD") {
            $query .= " AND T1.CHAIR_GROUP_CD = '".$data["CHAIR_GROUP_CD"]."' ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND T1.CLASSCD          = '".$subclass_array[0]."' ";
            $query .= " AND T1.SCHOOL_KIND      = '".$subclass_array[1]."' ";
            $query .= " AND T1.CURRICULUM_CD    = '".$subclass_array[2]."' ";
            $query .= " AND T1.SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
            $query .= " AND T1.SUBCLASSCD = '".$subclasscd_set."' ";
        }
        if ($model->Properties["use_CHAIR_GROUP_SDIV_DAT"] === '1') {
            $query .= " AND (L1.TESTKINDCD || L1.TESTITEMCD || L1.SCORE_DIV = '". $model->field['TEST_CD'] ."' ";
            $query .= " OR  (L1.TESTKINDCD IS NULL ";
            $query .= " AND  L1.TESTITEMCD IS NULL ";
            $query .= " AND  L1.SCORE_DIV IS NULL)) ";
        } else {
            $query .= " AND (L1.TESTKINDCD || L1.TESTITEMCD = '". $model->field['TEST_CD'] ."' ";
            $query .= " OR  (L1.TESTKINDCD IS NULL ";
            $query .= " AND  L1.TESTITEMCD IS NULL)) ";
        }

        if ($flg == "") {
            $query .= " GROUP BY ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.SEMESTER, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD, ";
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
            }
            $query .= "     T1.SUBCLASSCD ";
        }

        return $query;
    }

    //科目一覧取得
    public function getChairMst($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T2.SUBCLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD, ";
            $query .= "     T2.SCHOOL_KIND, ";
            $query .= "     T2.CURRICULUM_CD, ";
        }
        $query .= "     T2.SUBCLASSNAME, ";
        $query .= "     '{$model->target_seme}' AS SEMESTER, ";
        if ($model->Properties["KNJZ234A_ShowOtherEntrySubclass"] == '1') {
            $query .= "     CASE WHEN l2.testkindcd || l2.testitemcd || l2.score_div IS NOT NULL ";
            $query .= "          THEN l1.CHAIR_GROUP_CD ";
            $query .= "          ELSE '9999' ";
            $query .= "     END AS CHAIR_GROUP_CD, ";
            $query .= "     CASE WHEN l2.testkindcd || l2.testitemcd || l2.score_div IS NOT NULL ";
            $query .= "          THEN INT(l1.CHAIR_GROUP_CD) ";
            $query .= "          ELSE 9999 ";
            $query .= "     END AS CHAIR_GROUP_CD_ORDER, ";
            $query .= "     CASE WHEN l2.testkindcd || l2.testitemcd || l2.score_div IS NOT NULL ";
            $query .= "          THEN l1.CHAIR_GROUP_NAME ";
            $query .= "          ELSE '' ";
            $query .= "     END AS CHAIR_GROUP_NAME, ";
            $query .= "     CASE WHEN l2.testkindcd || l2.testitemcd || l2.score_div IS NOT NULL ";
            $query .= "          THEN l1.CHAIR_GROUP_ABBV ";
            $query .= "          ELSE '' ";
            $query .= "     END AS CHAIR_GROUP_ABBV, ";
            if ($model->Properties["use_CHAIR_GROUP_SDIV_DAT"] === '1') {
                $query .= "     L2.SCORE_DIV, ";
                $query .= "     CASE WHEN l1.chair_group_cd IS NOT NULL ";
                $query .= "          THEN L2.TESTKINDCD || L2.TESTITEMCD || L2.SCORE_DIV ";
                $query .= "          ELSE '' ";
                $query .= "     END AS TEST_CD, ";
            } else {
                $query .= "     CASE WHEN l1.chair_group_cd IS NOT NULL ";
                $query .= "          THEN L2.TESTKINDCD || L2.TESTITEMCD ";
                $query .= "          ELSE '' ";
                $query .= "     END AS TEST_CD, ";
            }
        } else {
            $query .= "     L1.CHAIR_GROUP_CD, ";
            $query .= "     L1.CHAIR_GROUP_NAME, ";
            $query .= "     L1.CHAIR_GROUP_ABBV, ";
            if ($model->Properties["use_CHAIR_GROUP_SDIV_DAT"] === '1') {
                $query .= "     L2.SCORE_DIV, ";
                $query .="      L2.TESTKINDCD || L2.TESTITEMCD || L2.SCORE_DIV AS TEST_CD, ";
            } else {
                $query .="      L2.TESTKINDCD || L2.TESTITEMCD AS TEST_CD, ";
            }
        }
        $query .= "     L2.CHAIRCD, ";
        $query .= "     L2.TESTKINDCD, ";
        $query .= "     L2.TESTITEMCD ";
        $query .= " FROM ";
        $query .= "     CREDIT_MST T1, ";
        $query .= "     V_SUBCLASS_MST T2 ";
        $query .= "     LEFT JOIN CHAIR_GROUP_MST L1 ON L1.YEAR       = T2.YEAR ";
        $query .= "                                 AND L1.SEMESTER   = '".$model->target_seme."' ";
        $query .= "                                 AND L1.SUBCLASSCD = T2.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                 AND L1.CLASSCD       = T2.CLASSCD ";
            $query .= "                                 AND L1.SCHOOL_KIND   = T2.SCHOOL_KIND  ";
            $query .= "                                 AND L1.CURRICULUM_CD = T2.CURRICULUM_CD  ";
        }
        if ($model->Properties["use_CHAIR_GROUP_SDIV_DAT"] === '1') {
            $query .= "     LEFT JOIN CHAIR_GROUP_SDIV_DAT L2 ON L2.YEAR           = T2.YEAR ";
            $query .= "                                      AND L2.SEMESTER       = '".$model->target_seme."' ";
            $query .= "                                      AND L2.CHAIR_GROUP_CD = L1.CHAIR_GROUP_CD ";
            $query .= "                                      AND L2.TESTKINDCD || L2.TESTITEMCD || L2.SCORE_DIV = '".$model->field["TEST_CD"]."' ";
        } else {
            $query .= "     LEFT JOIN CHAIR_GROUP_DAT L2 ON L2.YEAR           = T2.YEAR ";
            $query .= "                                 AND L2.SEMESTER       = '".$model->target_seme."' ";
            $query .= "                                 AND L2.CHAIR_GROUP_CD = L1.CHAIR_GROUP_CD ";
            $query .= "                                 AND L2.TESTKINDCD || L2.TESTITEMCD = '".$model->field["TEST_CD"]."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR       = '".CTRL_YEAR."' ";
        $query .= " AND T1.COURSECODE = '".$model->coursecode."' ";
        $query .= " AND T1.COURSECD   = '".$model->coursecd ."' ";
        $query .= " AND T1.MAJORCD    = '".$model->majorcd ."' ";
        $query .= " AND T1.GRADE      = '".$model->grade ."' ";
        $query .= " AND T1.YEAR       = T2.YEAR  ";
        $query .= " AND T1.SUBCLASSCD = T2.SUBCLASSCD  ";

        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND T1.CLASSCD       = T2.CLASSCD  ";
            $query .= " AND T1.SCHOOL_KIND   = T2.SCHOOL_KIND  ";
            $query .= " AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.SCHOOL_KIND, ";
            $query .= "     T2.CURRICULUM_CD, ";
            $query .= "     T2.CLASSCD, ";
        }
        $query .= "     T2.SUBCLASSCD ";
        if ($model->Properties["KNJZ234A_ShowOtherEntrySubclass"] == '1') {
            $query .= "     , CHAIR_GROUP_CD_ORDER ";
        }

        return $query;
    }

    //グループデータ取得
    public function getGroupData($gropucd, $test_cd, $chaircd, $model)
    {
        $query  =" SELECT ";
        $query .="     CHA_GD.CHAIR_GROUP_CD, ";
        $query .="     CHA_GD.CHAIRCD, ";
        $query .="     CHA_D.CHAIRNAME ";
        $query .=" FROM ";
        if ($model->Properties["use_CHAIR_GROUP_SDIV_DAT"] === '1') {
            $query .="     CHAIR_GROUP_SDIV_DAT CHA_GD ";
        } else {
            $query .="     CHAIR_GROUP_DAT CHA_GD ";
        }
        $query .="     LEFT JOIN CHAIR_DAT CHA_D ON CHA_D.YEAR = CHA_GD.YEAR ";
        $query .="          AND CHA_D.SEMESTER = CHA_GD.SEMESTER ";
        $query .="          AND CHA_D.CHAIRCD = CHA_GD.CHAIRCD ";
        $query .=" WHERE ";
        $query .="         CHA_GD.YEAR     = '".CTRL_YEAR."' ";
        $query .="     AND CHA_GD.SEMESTER = '".$model->target_seme."' ";
        $query .="     AND CHA_GD.CHAIR_GROUP_CD = '".$gropucd."' ";
        if ($model->Properties["use_CHAIR_GROUP_SDIV_DAT"] === '1') {
            $query .= " AND CHA_GD.TESTKINDCD || CHA_GD.TESTITEMCD || CHA_GD.SCORE_DIV ='".$test_cd."' ";
        } else {
            $query .= " AND CHA_GD.TESTKINDCD || CHA_GD.TESTITEMCD  ='".$test_cd."' ";
        }
        $query .="     AND CHA_D.CHAIRCD = '".$chaircd."' ";
        $query .=" ORDER BY ";
        $query .="     CHA_GD.CHAIRCD ";

        return $query;
    }

    //学科、コース、学年名、科目名称取得
    public function getTitleName($model)
    {
        $query  =" SELECT ";
        $query .="     T1.SUBCLASSNAME, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .="     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD ||'-' || T1.SUBCLASSCD AS SUBCLASS_SET, ";
        } else {
            $query .="     T1.SUBCLASSCD AS SUBCLASS_SET, ";
        }
        $query .= "     T5.GRADE_NAME1 || ' ' || T3.COURSENAME || T4.MAJORNAME || ' ' || T2.COURSECODENAME AS NAME_SET ";
        $query .=" FROM ";
        $query .="     V_SUBCLASS_MST T1, ";
        $query .= "     COURSECODE_MST T2, ";
        $query .= "     COURSE_MST T3, ";
        $query .= "     MAJOR_MST T4, ";
        $query .= "     SCHREG_REGD_GDAT T5  ";
        $query .=" WHERE ";
        $query .="     T1.YEAR     = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .="     AND T1.CLASSCD        = '".$model->classcd."' ";
            $query .="     AND T1.SCHOOL_KIND    = '".$model->school_kind."' ";
            $query .="     AND T1.CURRICULUM_CD  = '".$model->curriculum_cd."' ";
        }
        $query .="     AND T1.SUBCLASSCD     = '".$model->subclasscd."' ";
        $query .="     AND T1.YEAR           = T5.YEAR ";
        $query .= "    AND T2.COURSECODE     = '".$model->getcoursecode."' ";
        $query .= "    AND T3.COURSECD       = '".$model->getcoursecd ."' ";
        $query .= "    AND T4.MAJORCD        = '".$model->getmajorcd ."' ";
        $query .= "    AND T5.GRADE          = '".$model->getgrade ."' ";

        return $query;
    }

    //グループ情報取得
    public function getRow($chair_group_cd, $model)
    {
        $db    = Query::dbCheckOut();

        $query  = "SELECT ";
        $query .= "    * ";
        $query .= "FROM ";
        $query .= "    CHAIR_GROUP_MST ";
        $query .= "WHERE ";
        $query .="         YEAR     = '".CTRL_YEAR."' ";
        $query .="     AND SEMESTER = '".$model->target_seme."' ";
        $query .="     AND CHAIR_GROUP_CD = '".$chair_group_cd."' ";
        $row   = $db->getRow($query, DB_FETCHMODE_ASSOC);

        Query::dbCheckIn($db);
        return $row;
    }

    //テスト名称取得
    public function getTestItem($model)
    {
        $query  =" SELECT ";
        if ($model->Properties["use_CHAIR_GROUP_SDIV_DAT"] === '1') {
            $query .="     TESTKINDCD || TESTITEMCD || SCORE_DIV AS VALUE, ";
            $query .="     TESTKINDCD || TESTITEMCD || SCORE_DIV || ':' || TESTITEMNAME AS LABEL ";
        } else {
            $query .="     TESTKINDCD || TESTITEMCD AS VALUE, ";
            $query .="     TESTKINDCD || TESTITEMCD || ':' || TESTITEMNAME AS LABEL ";
        }
        $query .=" FROM ";
        if ($model->Properties["use_CHAIR_GROUP_SDIV_DAT"] === '1') {
            $query .="     TESTITEM_MST_COUNTFLG_NEW_SDIV ";
        } elseif ($model->Properties["useTestCountflg"] === 'TESTITEM_MST_COUNTFLG_NEW') {
            $query .="     TESTITEM_MST_COUNTFLG_NEW ";
        } else {
            $query .="     TESTITEM_MST_COUNTFLG ";
        }
        $query .=" WHERE ";
        $query .="     YEAR     = '".CTRL_YEAR."' ";
        if ($model->Properties["use_CHAIR_GROUP_SDIV_DAT"] === '1' || $model->Properties["useTestCountflg"] === 'TESTITEM_MST_COUNTFLG_NEW') {
            $query .=" AND SEMESTER = '".$model->target_seme."' ";
        }

        return $query;
    }

    //テスト名称取得
    public function getTestItemName($model)
    {
        $query  =" SELECT ";
        if ($model->Properties["use_CHAIR_GROUP_SDIV_DAT"] === '1') {
            $query .="     TESTKINDCD || TESTITEMCD || SCORE_DIV || ':' || TESTITEMNAME AS ITEMNAME ";
        } else {
            $query .="     TESTKINDCD || TESTITEMCD || ':' || TESTITEMNAME AS ITEMNAME ";
        }
        $query .=" FROM ";
        if ($model->Properties["use_CHAIR_GROUP_SDIV_DAT"] === '1') {
            $query .="     TESTITEM_MST_COUNTFLG_NEW_SDIV ";
        } elseif ($model->Properties["useTestCountflg"] === 'TESTITEM_MST_COUNTFLG_NEW') {
            $query .="     TESTITEM_MST_COUNTFLG_NEW ";
        } else {
            $query .="     TESTITEM_MST_COUNTFLG ";
        }
        $query .=" WHERE ";
        $query .="     YEAR     = '".CTRL_YEAR."' ";
        if ($model->Properties["use_CHAIR_GROUP_SDIV_DAT"] === '1') {
            $query .="     AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '".$model->test_cd."' ";
        } else {
            $query .="     AND TESTKINDCD || TESTITEMCD = '".$model->test_cd."' ";
        }
        if ($model->Properties["use_CHAIR_GROUP_SDIV_DAT"] === '1' || $model->Properties["useTestCountflg"] === 'TESTITEM_MST_COUNTFLG_NEW') {
            $query .=" AND SEMESTER = '".$model->target_seme."' ";
        }
        return $query;
    }

    //データ一覧取得
    public function selectQuery($subclasscd, $chair_group_cd, $test_cd, $target_seme, $model)
    {
        //教育課程対応
        if (($model->Properties["useCurriculumcd"] == '1') && (strlen($subclasscd) > 6)) {
            $substr_classcd         = substr($subclasscd, 0, 2);
            $substr_school_kind     = substr($subclasscd, 3, 1);
            $substr_curriculum_cd   = substr($subclasscd, 5, 1);
            $substr_subclasscd      = substr($subclasscd, 7, 6);
        }
        $query  =" SELECT ";
        $query .="     CHA_D.CHAIRCD AS VALUE, ";
        $query .="     CHA_GD.CHAIRCD AS JOINCD, ";
        $query .="     CHA_GD.CHAIR_GROUP_CD, ";
        $query .="     CHA_D.CHAIRCD || ' ' || CHA_D.CHAIRNAME AS LABEL ";
        $query .=" FROM ";
        $query .="     CHAIR_DAT CHA_D ";
        if ($model->Properties["use_CHAIR_GROUP_SDIV_DAT"] === '1') {
            $query .="     LEFT JOIN CHAIR_GROUP_SDIV_DAT CHA_GD ";
        } else {
            $query .="     LEFT JOIN CHAIR_GROUP_DAT CHA_GD  ";
        }
        $query .="           ON CHA_GD.YEAR = CHA_D.YEAR ";
        $query .="          AND CHA_GD.SEMESTER = CHA_D.SEMESTER ";
        $query .="          AND CHA_GD.CHAIRCD = CHA_D.CHAIRCD ";
        //登録済講座非表示設定
        if ($model->Properties["KNJZ234A_HideChairDuplicate"] == '1') {
            //登録済講座を全部取得
        } else {
            $query .="          AND CHA_GD.CHAIR_GROUP_CD = '".$chair_group_cd."' ";
        }
        //テスト種別コードの設定
        if ($model->Properties["useKoteiTestCd"] === '1') {
            if ($model->Properties["use_CHAIR_GROUP_SDIV_DAT"] === '1') {
                $query .="          AND CHA_GD.TESTKINDCD || CHA_GD.TESTITEMCD || CHA_GD.SCORE_DIV = '000000' ";
            } else {
                $query .="          AND CHA_GD.TESTKINDCD || CHA_GD.TESTITEMCD = '0000' ";
            }
        } else {
            if ($model->Properties["use_CHAIR_GROUP_SDIV_DAT"] === '1') {
                $query .="          AND CHA_GD.TESTKINDCD || CHA_GD.TESTITEMCD || CHA_GD.SCORE_DIV = '".$test_cd."' ";
            } else {
                $query .="          AND CHA_GD.TESTKINDCD || CHA_GD.TESTITEMCD = '".$test_cd."' ";
            }
        }
        $query .=" WHERE ";
        $query .="     CHA_D.YEAR = '".CTRL_YEAR."' ";
        $query .="     AND CHA_D.SEMESTER = '".$target_seme."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            if (strlen($subclasscd) > 6) {
                $query .="     AND CHA_D.CLASSCD        = '".$substr_classcd."' ";
                $query .="     AND CHA_D.SCHOOL_KIND    = '".$substr_school_kind."' ";
                $query .="     AND CHA_D.CURRICULUM_CD  = '".$substr_curriculum_cd."' ";
                $query .="     AND CHA_D.SUBCLASSCD     = '".$substr_subclasscd."' ";
            } else {
                $query .="     AND CHA_D.CLASSCD        = '".$model->classcd."' ";
                $query .="     AND CHA_D.SCHOOL_KIND    = '".$model->school_kind."' ";
                $query .="     AND CHA_D.CURRICULUM_CD  = '".$model->curriculum_cd."' ";
                $query .="     AND CHA_D.SUBCLASSCD     = '".$subclasscd."' ";
            }
        } else {
            $query .="     AND CHA_D.SUBCLASSCD = '".$subclasscd."' ";
        }
        $query .=" ORDER BY ";
        $query .="     VALUE ";

        return $query;
    }

    //チェック用SQL
    public function checkChairGroupM($model, $year_sem, $div = "")
    {
        $query  = "SELECT ";
        $query .= "    COUNT(*) ";
        $query .= "FROM ";
        $query .= "    CHAIR_GROUP_MST ";
        $query .= "WHERE ";
        if ($div == "LAST_DATA") {
            $query .= "    YEAR || SEMESTER = '".$year_sem."' ";
        } else {
            $query .= "        YEAR     = '".CTRL_YEAR."' ";
            $query .= "    AND SEMESTER = '".$model->target_seme."' ";
        }
        if ($div == "INS" || $div == "UPD") {
            $query .= "    AND CHAIR_GROUP_CD = '".$model->chair_group_cd."' ";
        }

        return $query;
    }

    //チェック用SQL
    public function checkChairGroupD($model, $year_sem, $div)
    {
        $query  = "SELECT ";
        $query .= "    COUNT(*) ";
        $query .= "FROM ";
        if ($model->Properties["use_CHAIR_GROUP_SDIV_DAT"] === '1') {
            $query .= "    CHAIR_GROUP_SDIV_DAT T1 ";
        } else {
            $query .= "    CHAIR_GROUP_DAT T1 ";
        }
        if ($div === 'INS') {
            $query .= "    INNER JOIN CHAIR_GROUP_MST L1 ON L1.YEAR = T1.YEAR ";
            $query .= "                                 AND L1.SEMESTER = T1.SEMESTER ";
            $query .= "                                 AND L1.CHAIR_GROUP_CD = T1.CHAIR_GROUP_CD ";
        }
        $query .= "WHERE ";
        $query .= "    T1.YEAR || T1.SEMESTER = '".$year_sem."' ";
        //テスト種別コードの設定
        if ($model->Properties["useKoteiTestCd"] === '1') {
            $setcd = "";
            $setcd = sprintf("%06d", $model->field["CHAIR_GROUP_CD"]);
            $query .= "    AND T1.CHAIR_GROUP_CD = '".$setcd."' ";
        } else {
            $query .= "    AND T1.CHAIR_GROUP_CD = '".$model->field["CHAIR_GROUP_CD"]."' ";
        }
        if ($div != "DEL") {
            //テスト種別コードの設定
            if ($model->Properties["useKoteiTestCd"] === '1') {
                if ($model->Properties["use_CHAIR_GROUP_SDIV_DAT"] === '1') {
                    $query .= "    AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '000000' ";
                } else {
                    $query .= "    AND T1.TESTKINDCD || T1.TESTITEMCD = '0000' ";
                }
            } else {
                if ($model->Properties["use_CHAIR_GROUP_SDIV_DAT"] === '1') {
                    $query .= "    AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '".$model->field['TEST_CD']."' ";
                } else {
                    $query .= "    AND T1.TESTKINDCD || T1.TESTITEMCD = '".$model->field['TEST_CD']."' ";
                }
            }
        }

        return $query;
    }

    //マスタ新規SQL作成
    public function makeInsChairMQuery($model)
    {
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEMESTER"][TEXT]         = $model->target_seme;
        //テスト種別コードの設定
        if ($model->Properties["useKoteiTestCd"] === '1') {
            $data["CHAIR_GROUP_CD"][TEXT]   = sprintf("%06d", $model->field["CHAIR_GROUP_CD"]);
        } else {
            $data["CHAIR_GROUP_CD"][TEXT]   = $model->field["CHAIR_GROUP_CD"];
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]           = $model->classcd;
            $data["SCHOOL_KIND"][TEXT]       = $model->school_kind;
            $data["CURRICULUM_CD"][TEXT]     = $model->curriculum_cd;
            $data["SUBCLASSCD"][TEXT]        = $model->subclasscd;
        } else {
            $data["SUBCLASSCD"][TEXT]        = $model->subclasscd;
        }
        $data["CHAIR_GROUP_NAME"][TEXT] = $model->field["CHAIR_GROUP_NAME"];
        $data["CHAIR_GROUP_ABBV"][TEXT] = $model->field["CHAIR_GROUP_ABBV"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $query = Query::insertSQL($data, "CHAIR_GROUP_MST");

        return $query;
    }

    //マスタ削除SQL作成
    public function delChairMQuery($model)
    {
        $query  = "DELETE FROM ";
        $query .= "    CHAIR_GROUP_MST ";
        $query .= "WHERE ";
        $query .="         YEAR     = '".CTRL_YEAR."' ";
        $query .="     AND SEMESTER = '".$model->target_seme."' ";
        //テスト種別コードの設定
        if ($model->Properties["useKoteiTestCd"] === '1') {
            $setcd = "";
            $setcd = sprintf("%06d", $model->field["CHAIR_GROUP_CD"]);
            $query .="     AND CHAIR_GROUP_CD = '".$setcd."' ";
        } else {
            $query .="     AND CHAIR_GROUP_CD = '".$model->field["CHAIR_GROUP_CD"]."' ";
        }

        return $query;
    }

    //グループデータ追加
    public function &chairGInstQuery(&$db, $select, $model)
    {
        //グループ新規
        $data = array();
        $selectdata = explode(",", $select);
        for ($i = 0; $i < get_count($selectdata); $i++) {
            if (!$selectdata[$i]) {
                continue;
            }
            $db->query(knjz234aQuery::makeInsChairGQuery($model, $selectdata[$i]));
        }
        return;
    }

    //データ新規SQL作成
    public function makeInsChairGQuery($model, $selectdata)
    {
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEMESTER"][TEXT]         = $model->target_seme;
        //テスト種別コードの設定
        if ($model->Properties["useKoteiTestCd"] === '1') {
            $data["CHAIR_GROUP_CD"][TEXT]   = sprintf("%06d", $model->field["CHAIR_GROUP_CD"]);
            $data["TESTKINDCD"][TEXT]       = '00';
            $data["TESTITEMCD"][TEXT]       = '00';
            if ($model->Properties["use_CHAIR_GROUP_SDIV_DAT"] === '1') {
                $data["SCORE_DIV"][TEXT]    = '00';
            }
        } else {
            $data["CHAIR_GROUP_CD"][TEXT]   = $model->field["CHAIR_GROUP_CD"];
            $data["TESTKINDCD"][TEXT]       = substr($model->test_cd, 0, 2);
            $data["TESTITEMCD"][TEXT]       = substr($model->test_cd, 2, 2);
            if ($model->Properties["use_CHAIR_GROUP_SDIV_DAT"] === '1') {
                $data["SCORE_DIV"][TEXT]    = substr($model->test_cd, 4, 2);
            }
        }
        $data["CHAIRCD"][TEXT]          = $selectdata;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        if ($model->Properties["use_CHAIR_GROUP_SDIV_DAT"] === '1') {
            $query = Query::insertSQL($data, "CHAIR_GROUP_SDIV_DAT");
        } else {
            $query = Query::insertSQL($data, "CHAIR_GROUP_DAT");
        }

        return $query;
    }

    //データ削除SQL作成
    public function delChairGQuery($model)
    {
        $query  = "DELETE FROM ";
        if ($model->Properties["use_CHAIR_GROUP_SDIV_DAT"] === '1') {
            $query .= "    CHAIR_GROUP_SDIV_DAT ";
        } else {
            $query .= "    CHAIR_GROUP_DAT ";
        }
        $query .= "WHERE ";
        $query .="         YEAR     = '".CTRL_YEAR."' ";
        $query .="     AND SEMESTER = '".$model->target_seme."' ";
        //テスト種別コードの設定
        if ($model->Properties["useKoteiTestCd"] === '1') {
            $setcd = "";
            $setcd = sprintf("%06d", $model->field["CHAIR_GROUP_CD"]);
            $query .="     AND CHAIR_GROUP_CD = '".$setcd."' ";
            if ($model->Properties["use_CHAIR_GROUP_SDIV_DAT"] === '1') {
                $query .="     AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '000000' ";
            } else {
                $query .="     AND TESTKINDCD || TESTITEMCD = '0000' ";
            }
        } else {
            $query .="     AND CHAIR_GROUP_CD = '".$model->field["CHAIR_GROUP_CD"]."' ";
            if ($model->Properties["use_CHAIR_GROUP_SDIV_DAT"] === '1') {
                $query .="     AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '".$model->test_cd."' ";
            } else {
                $query .="     AND TESTKINDCD || TESTITEMCD = '".$model->test_cd."' ";
            }
        }

        return $query;
    }

    //データ存在チェック
    public function countChairGroupMst($semester)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CHAIR_GROUP_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$semester."' ";

        $cnt = $db->getOne($query);
        Query::dbCheckIn($db);
        return $cnt;
    }

    //CHAIR_GROUP_MSTのデータを削除
    public function deleteChairGroupMst($model)
    {
        $query  =" DELETE FROM CHAIR_GROUP_MST ";
        $query .=" WHERE YEAR     = '".CTRL_YEAR."' ";
        $query .="   AND SEMESTER = '".$model->target_seme."' ";

        return $query;
    }

    //CHAIR_GROUP_MSTの参照学期のデータを対象学期にインサート
    public function insertChairGroupMst($model)
    {
        $query  =" INSERT INTO CHAIR_GROUP_MST ";
        $query .=" SELECT ";
        $query .="      '".CTRL_YEAR."', ";
        $query .="      '".$model->target_seme."', ";
        $query .="      CHAIR_GROUP_CD, ";
        $query .="      CLASSCD, ";
        $query .="      SCHOOL_KIND, ";
        $query .="      CURRICULUM_CD, ";
        $query .="      SUBCLASSCD, ";
        $query .="      CHAIR_GROUP_NAME, ";
        $query .="      CHAIR_GROUP_ABBV, ";
        $query .="      '".STAFFCD."', ";
        $query .="      SYSDATE()";
        $query .=" FROM CHAIR_GROUP_MST ";
        $query .=" WHERE ";
        $query .="         YEAR     = '".CTRL_YEAR."' ";
        $query .="     AND SEMESTER = '".$model->refer_seme."' ";

        return $query;
    }

    //CHAIR_GROUP_DATのデータを削除
    public function deleteChairGroupDat($model)
    {
        if ($model->Properties["use_CHAIR_GROUP_SDIV_DAT"] === '1') {
            $query  =" DELETE FROM CHAIR_GROUP_SDIV_DAT ";
        } else {
            $query  =" DELETE FROM CHAIR_GROUP_DAT ";
        }
        $query .=" WHERE YEAR     = '".CTRL_YEAR."' ";
        $query .="   AND SEMESTER = '".$model->target_seme."' ";

        return $query;
    }

    //CHAIR_GROUP_DATの参照学期のデータを対象学期にインサート
    public function insertChairGroupDat($model)
    {
        if ($model->Properties["use_CHAIR_GROUP_SDIV_DAT"] === '1') {
            $query  =" INSERT INTO CHAIR_GROUP_SDIV_DAT ";
        } else {
            $query  =" INSERT INTO CHAIR_GROUP_DAT ";
        }
        $query .=" SELECT ";
        $query .="      '".CTRL_YEAR."', ";
        $query .="      '".$model->target_seme."', ";
        $query .="      CHAIR_GROUP_CD, ";
        $query .="      TESTKINDCD, ";
        $query .="      TESTITEMCD, ";
        if ($model->Properties["use_CHAIR_GROUP_SDIV_DAT"] === '1') {
            $query .="      SCORE_DIV, ";
        }
        $query .="      CHAIRCD, ";
        $query .="      '".STAFFCD."', ";
        $query .="      SYSDATE()";
        if ($model->Properties["use_CHAIR_GROUP_SDIV_DAT"] === '1') {
            $query .=" FROM CHAIR_GROUP_SDIV_DAT ";
        } else {
            $query .=" FROM CHAIR_GROUP_DAT ";
        }
        $query .=" WHERE ";
        $query .="         YEAR     = '".CTRL_YEAR."' ";
        $query .="     AND SEMESTER = '".$model->refer_seme."' ";

        return $query;
    }

    //「コピーボタン」押し時の処理
    public function getInsertCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $db->query(knjz234aQuery::deleteChairGroupMst($model));
        $db->query(knjz234aQuery::insertChairGroupMst($model));

        $db->query(knjz234aQuery::deleteChairGroupDat($model));
        $db->query(knjz234aQuery::insertChairGroupDat($model));

        $db->commit();
        Query::dbCheckIn($db);
    }
}
