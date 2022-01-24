<?php

require_once('for_php7.php');

class knjz237aQuery extends Query {

    //学期取得
    function getSemester($getname, $semester="") {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($semester == "") {
            //明治は９学期を表示する
            if ($getname === 'meiji') {
            } else {
                $query .= "     AND SEMESTER < '9' ";
            }
        } else {
            $query .= "     AND SEMESTER = '".$semester."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //適用学校情報取得
    function getNameMst() {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= " AND NAMECD2 = '00' ";

        return $query;
    }

    //テスト項目取得
    function getTestitemMstCountflgNew($semester, $testkindcd="") {
        $query  = " SELECT ";
        $query .= "     TESTKINDCD || '-' || TESTITEMCD AS VALUE, ";
        $query .= "     TESTKINDCD || TESTITEMCD || ' ' || TESTITEMNAME AS LABEL, ";
        $query .= "     TESTITEMNAME ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".$semester."' AND ";
        $query .= "     TESTKINDCD IN ('01','02','99') ";
        if ($testkindcd) {
            $query .= "     AND TESTKINDCD || '-' ||TESTITEMCD = '".$testkindcd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //リスト内情報取得
    function getListdata($model) {
        $query  = " SELECT ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.TESTKINDCD || '-' || T1.TESTITEMCD AS TESTKINDCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     L1.SUBCLASSNAME, ";
        $query .= "     T1.DIV, ";
        $query .= "     T1.GRADE, ";
        $query .= "     L2.GRADE_NAME1 AS GRADENAME, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     L3.COURSENAME, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     L3.MAJORNAME, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     L4.COURSECODENAME, ";
        $query .= "     L5.GROUP_CD, ";
        $query .= "     L5.GROUP_NAME, ";
        $query .= "     T1.PERFECT, ";
        $query .= "     T1.PASS_SCORE ";
        $query .= " FROM ";
        $query .= "     PERFECT_RECORD_DAT T1 ";
        $query .= "     LEFT JOIN V_SUBCLASS_MST L1 ON ";
        $query .= "         L1.YEAR         = T1.YEAR AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         L1.CLASSCD = T1.CLASSCD AND ";
            $query .= "         L1.SCHOOL_KIND = T1.SCHOOL_KIND AND ";
            $query .= "         L1.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
        }
        $query .= "         L1.SUBCLASSCD   = T1.SUBCLASSCD ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT L2 ON ";
        $query .= "         L2.YEAR         = T1.YEAR AND ";
        $query .= "         L2.GRADE        = T1.GRADE ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST L3 ON ";
        $query .= "         L3.YEAR         = T1.YEAR AND ";
        $query .= "         L3.COURSECD     = T1.COURSECD AND ";
        $query .= "         L3.MAJORCD      = T1.MAJORCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST L4 ON ";
        $query .= "         L4.YEAR         = T1.YEAR AND ";
        $query .= "         L4.COURSECODE   = T1.COURSECODE ";
        $query .= "     LEFT JOIN COURSE_GROUP_CD_HDAT L5 ON ";
        $query .= "         L5.YEAR         = T1.YEAR AND ";
        $query .= "         L5.GRADE        = T1.GRADE AND ";
        $query .= "         L5.GROUP_CD     = T1.MAJORCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '{$model->field["SEMESTER"]}' AND ";
        $query .= "     T1.TESTKINDCD || '-' || T1.TESTITEMCD = '{$model->field["TESTKINDCD"]}' ";
        //更新可能(制限付)
        if (AUTHORITY != DEF_UPDATABLE) {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD IN ( ";
            } else {
                $query .= "     AND T1.SUBCLASSCD IN ( ";
            }
            $query .= "         SELECT ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "             T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ";
            } else {
                $query .= "             T1.SUBCLASSCD ";
            }
            $query .= "         FROM ";
            $query .= "             CHAIR_DAT T1, ";
          //$query .= "             CHAIR_STD_DAT T2, ";
            $query .= "             CHAIR_STF_DAT T4 ";
            $query .= "         WHERE ";
            $query .= "                 T1.YEAR       = '".CTRL_YEAR."' ";
            $query .= "             AND T1.SEMESTER   = '".$model->field["SEMESTER"]."' ";
          //$query .= "             AND SUBSTR(T1.SUBCLASSCD, 1 ,2) <= '90' ";
          //$query .= "             AND T2.YEAR       = T1.YEAR ";
          //$query .= "             AND T2.SEMESTER   = T1.SEMESTER ";
          //$query .= "             AND T2.CHAIRCD    = T1.CHAIRCD ";
            $query .= "             AND T4.YEAR       = T1.YEAR ";
            $query .= "             AND T4.SEMESTER   = T1.SEMESTER ";
            $query .= "             AND T4.CHAIRCD    = T1.CHAIRCD ";
            $query .= "             AND T4.STAFFCD    = '".STAFFCD."' "; //ログインした先生
            $query .= "         GROUP BY ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD, ";
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
            }
            $query .= "             T1.SUBCLASSCD ";
            $query .= "     ) ";
        }
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.DIV, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE ";

        return $query;
    }

    //実力コード年間平均
    function getProficiencyAndAvg($model) {
        $div = $model->field["PROFICIENCYDIV"] ? $model->field["PROFICIENCYDIV"] : $model->proficiencydiv;
        $query  = " WITH PROFICIENCY_AND_AVG AS ( ";
        $query .= " SELECT ";
        $query .= "     PROFICIENCYCD, ";
        $query .= "     PROFICIENCYNAME1 ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_MST ";
        $query .= " WHERE ";
        $query .= "     PROFICIENCYDIV = '".$div."' ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     '9999' AS PROFICIENCYCD, ";
        $query .= "     L1.NAME1 || '年間平均' AS PROFICIENCYNAME1 ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_MST T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'H508' AND L1.NAMECD2 = T1.PROFICIENCYDIV ";
        $query .= " GROUP BY ";
        $query .= "     T1.PROFICIENCYDIV, ";
        $query .= "     L1.NAME1 ";
        $query .= " ) ";

        return $query;
    }

    //実力コードコンボ
    function getProficiencycd($model) {
        $query  = knjz237aQuery::getProficiencyAndAvg($model);
        $query .= " SELECT ";
        $query .= "     PROFICIENCYCD AS VALUE, ";
        $query .= "     PROFICIENCYCD || ' ' || PROFICIENCYNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_AND_AVG ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目取得
    function getSubclassMst($semester, $model) {
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || ' ' || SUBCLASSNAME AS LABEL ";
        } else {
            $query .= "     SUBCLASSCD AS VALUE, ";
            $query .= "     SUBCLASSCD || ' ' || SUBCLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        //更新可能(制限付)
        if (AUTHORITY != DEF_UPDATABLE) {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD IN ( ";
            } else {
                $query .= "     AND SUBCLASSCD IN ( ";
            }
            $query .= "         SELECT ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "             T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ";
            } else {
                $query .= "             T1.SUBCLASSCD ";
            }
            $query .= "         FROM ";
            $query .= "             CHAIR_DAT T1, ";
          //$query .= "             CHAIR_STD_DAT T2, ";
            $query .= "             CHAIR_STF_DAT T4 ";
            $query .= "         WHERE ";
            $query .= "                 T1.YEAR       = '".CTRL_YEAR."' ";
            $query .= "             AND T1.SEMESTER   = '".$semester."' ";
          //$query .= "             AND SUBSTR(T1.SUBCLASSCD, 1 ,2) <= '90' ";
          //$query .= "             AND T2.YEAR       = T1.YEAR ";
          //$query .= "             AND T2.SEMESTER   = T1.SEMESTER ";
          //$query .= "             AND T2.CHAIRCD    = T1.CHAIRCD ";
            $query .= "             AND T4.YEAR       = T1.YEAR ";
            $query .= "             AND T4.SEMESTER   = T1.SEMESTER ";
            $query .= "             AND T4.CHAIRCD    = T1.CHAIRCD ";
            $query .= "             AND T4.STAFFCD    = '".STAFFCD."' "; //ログインした先生
            $query .= "         GROUP BY ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD, ";
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
            }
            $query .= "             T1.SUBCLASSCD ";
            $query .= "     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    function getGrade() {
        $query .= " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //課程取得
    function getCourse() {
        $query  = " SELECT ";
        $query .= "     COURSECD || ' ' || COURSENAME AS LABEL, ";
        $query .= "     COURSECD AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //学科取得
    function getMajor($coursecd) {
        $query  = " SELECT ";
        $query .= "     COURSECD || MAJORCD || ' ' || MAJORNAME AS LABEL, ";
        $query .= "     MAJORCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_MAJOR_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     COURSECD    = '{$coursecd}' ";

        return $query;
    }

    //コースグループ取得
    function getCourseGroup($grade) {
        $query  = " SELECT ";
        $query .= "     GROUP_CD || ' ' || GROUP_NAME AS LABEL, ";
        $query .= "     GROUP_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     COURSE_GROUP_CD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     GRADE   = '{$grade}' ";

        return $query;
    }

    //コースコンボ
    function getCoursecode() {
        $query  = " SELECT ";
        $query .= "     COURSECODE || ' ' || COURSECODENAME AS LABEL, ";
        $query .= "     COURSECODE AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_COURSECODE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //前年度からのコピーの件数カウント
    function getCopyCountQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "      COUNT(*) ";
        $query .= "  FROM ";
        $query .= "      PERFECT_RECORD_DAT T0 ";
        $query .= "  WHERE YEAR = '" .(CTRL_YEAR-1) ."' AND ";
        $query .= "     not exists ( ";     //今年度にない年度が対象
        $query .= "                 SELECT 'x' FROM PERFECT_RECORD_DAT T1 ";
        $query .= "                 WHERE ";
        $query .= "                 T1.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "                 T1.SEMESTER     = T0.SEMESTER AND ";
        $query .= "                 T1.TESTKINDCD   = T0.TESTKINDCD AND ";
        $query .= "                 T1.TESTITEMCD   = T0.TESTITEMCD AND ";
        $query .= "                 T1.CLASSCD      = T0.CLASSCD AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                 T1.SCHOOL_KIND = T0.SCHOOL_KIND AND ";
            $query .= "                 T1.CURRICULUM_CD = T0.CURRICULUM_CD AND ";
        }
        $query .= "                 T1.SUBCLASSCD   = T0.SUBCLASSCD AND ";
        $query .= "                 T1.DIV          = T0.DIV AND ";
        $query .= "                 T1.GRADE        = T0.GRADE AND ";
        $query .= "                 T1.COURSECD     = T0.COURSECD AND ";
        $query .= "                 T1.MAJORCD      = T0.MAJORCD AND ";
        $query .= "                 T1.COURSECODE   = T0.COURSECODE ";
        $query .= "                 )  AND ";
        $query .= "    (( ";
/******************************************************************/
/* 今年度にない教科や課程、学科、コース、学年のものはコピーしない */
/******************************************************************/
        $query .= "     EXISTS (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 COURSE_YDAT T2 ";
        $query .= "             WHERE ";
        $query .= "                 T2.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "                 T2.COURSECD = T0.COURSECD ";
        $query .= "             ) AND ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 MAJOR_YDAT T3 ";
        $query .= "             WHERE ";
        $query .= "                 T3.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "                 T3.COURSECD = T0.COURSECD AND ";
        $query .= "                 T3.MAJORCD  = T0.MAJORCD ";
        $query .= "             ) AND ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 COURSECODE_YDAT T4 ";
        $query .= "             WHERE ";
        $query .= "                 T4.YEAR       = '".CTRL_YEAR."' AND ";
        $query .= "                 T4.COURSECODE = T0.COURSECODE ";
        $query .= "             ) ";
        $query .= "     ) OR T0.COURSECD = '0' ) ";
        $query .= "     AND ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 V_SUBCLASS_MST T5 ";
        $query .= "             WHERE ";
        $query .= "                 T5.YEAR       = '".CTRL_YEAR."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                 T5.CLASSCD = T0.CLASSCD AND ";
            $query .= "                 T5.SCHOOL_KIND = T0.SCHOOL_KIND AND ";
            $query .= "                 T5.CURRICULUM_CD = T0.CURRICULUM_CD AND ";
        }
        $query .= "                 T5.SUBCLASSCD = T0.SUBCLASSCD ";
        $query .= "             ) ";
        $query .= "     AND ";
        $query .= "     (EXISTS (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 SCHREG_REGD_HDAT T6 ";
        $query .= "             WHERE ";
        $query .= "                 T6.YEAR  = '".CTRL_YEAR."' AND ";
        $query .= "                 T6.GRADE = T0.GRADE ";
        $query .= "             ) ";
        $query .= "      OR T0.GRADE = '00') ";
        $query .= "     AND ";
/*********************************************************************/
/* 今年度にすでに設定されている範囲のものはコピーしない              */
/* たとえば区分1がすでに存在していたら重複ではなくても区分2も区分3も */
/* 区分1とかぶってしまうためコピー対象ではない。                     */
/*********************************************************************/
        $query .= "     ((T0.DIV = '01' AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     * ";
        $query .= "                 FROM ";
        $query .= "                     PERFECT_RECORD_DAT T7 ";
        $query .= "                 WHERE ";
        $query .= "                     T7.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "                     T7.SEMESTER     = T0.SEMESTER AND ";
        $query .= "                     T7.TESTKINDCD   = T0.TESTKINDCD AND ";
        $query .= "                     T7.TESTITEMCD   = T0.TESTITEMCD AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     T7.CLASSCD = T0.CLASSCD AND ";
            $query .= "                     T7.SCHOOL_KIND = T0.SCHOOL_KIND AND ";
            $query .= "                     T7.CURRICULUM_CD = T0.CURRICULUM_CD AND ";
        }
        $query .= "                     T7.SUBCLASSCD   = T0.SUBCLASSCD ";
        $query .= "                 )) ";
        $query .= "     OR ";
        $query .= "     (T0.DIV = '02' AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     * ";
        $query .= "                 FROM ";
        $query .= "                     PERFECT_RECORD_DAT T8 ";
        $query .= "                 WHERE ";
        $query .= "                     T8.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "                     T8.SEMESTER     = T0.SEMESTER AND ";
        $query .= "                     T8.TESTKINDCD   = T0.TESTKINDCD AND ";
        $query .= "                     T8.TESTITEMCD   = T0.TESTITEMCD AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     T8.CLASSCD = T0.CLASSCD AND ";
            $query .= "                     T8.SCHOOL_KIND = T0.SCHOOL_KIND AND ";
            $query .= "                     T8.CURRICULUM_CD = T0.CURRICULUM_CD AND ";
        }
        $query .= "                     T8.SUBCLASSCD   = T0.SUBCLASSCD AND ";
        $query .= "                     (T8.GRADE       = T0.GRADE OR ";
        $query .= "                     T8.GRADE        = '00') ";
        $query .= "                 )) ";
        $query .= "     OR ";
        $query .= "     (T0.DIV = '03' AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     * ";
        $query .= "                 FROM ";
        $query .= "                     PERFECT_RECORD_DAT T8 ";
        $query .= "                 WHERE ";
        $query .= "                     T8.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "                     T8.SEMESTER     = T0.SEMESTER AND ";
        $query .= "                     T8.TESTKINDCD   = T0.TESTKINDCD AND ";
        $query .= "                     T8.TESTITEMCD   = T0.TESTITEMCD AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     T8.CLASSCD = T0.CLASSCD AND ";
            $query .= "                     T8.SCHOOL_KIND = T0.SCHOOL_KIND AND ";
            $query .= "                     T8.CURRICULUM_CD = T0.CURRICULUM_CD AND ";
        }
        $query .= "                     T8.SUBCLASSCD   = T0.SUBCLASSCD AND ";
        $query .= "                     (T8.GRADE       = T0.GRADE OR ";
        $query .= "                     T8.GRADE        = '00') AND ";
        $query .= "                     ((T8.COURSECD   = T0.COURSECD AND ";
        $query .= "                     T8.MAJORCD      = T0.MAJORCD AND ";
        $query .= "                     T8.COURSECODE   = T0.COURSECODE) OR ";
        $query .= "                     T8.COURSECD     = '0') ";
        $query .= "                 )) ";
        $query .= "     OR ";
        $query .= "     (T0.DIV = '04' AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     * ";
        $query .= "                 FROM ";
        $query .= "                     PERFECT_RECORD_DAT T8 ";
        $query .= "                 WHERE ";
        $query .= "                     T8.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "                     T8.SEMESTER     = T0.SEMESTER AND ";
        $query .= "                     T8.TESTKINDCD   = T0.TESTKINDCD AND ";
        $query .= "                     T8.TESTITEMCD   = T0.TESTITEMCD AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     T8.CLASSCD = T0.CLASSCD AND ";
            $query .= "                     T8.SCHOOL_KIND = T0.SCHOOL_KIND AND ";
            $query .= "                     T8.CURRICULUM_CD = T0.CURRICULUM_CD AND ";
        }
        $query .= "                     T8.SUBCLASSCD   = T0.SUBCLASSCD AND ";
        $query .= "                     (T8.GRADE       = T0.GRADE OR ";
        $query .= "                     T8.GRADE        = '00') AND ";
        $query .= "                     (T8.MAJORCD     = T0.MAJORCD OR ";
        $query .= "                     T8.MAJORCD      = '000') ";
        $query .= "                 ))) ";

        $cnt = $db->getOne($query);
        Query::dbCheckIn($db);
        return $cnt;
    }

    //前年度からのコピー
    function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO PERFECT_RECORD_DAT( ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER, ";
        $query .= "     TESTKINDCD, ";
        $query .= "     TESTITEMCD, ";
        $query .= "     CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "     SUBCLASSCD, ";
        $query .= "     DIV, ";
        $query .= "     GRADE, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     COURSECODE, ";
        $query .= "     PERFECT, ";
        $query .= "     PASS_SCORE, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " (SELECT ";
        $query .= "      '" . CTRL_YEAR ."', ";
        $query .= "      SEMESTER, ";
        $query .= "      TESTKINDCD, ";
        $query .= "      TESTITEMCD, ";
        $query .= "      CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "      SUBCLASSCD, ";
        $query .= "      DIV, ";
        $query .= "      GRADE, ";
        $query .= "      COURSECD, ";
        $query .= "      MAJORCD, ";
        $query .= "      COURSECODE, ";
        $query .= "      PERFECT, ";
        $query .= "      PASS_SCORE, ";
        $query .= "      '".STAFFCD."', ";
        $query .= "      SYSDATE() ";
        $query .= "  FROM ";
        $query .= "      PERFECT_RECORD_DAT T0 ";
        $query .= "  WHERE YEAR = '" .(CTRL_YEAR-1) ."' AND ";
        $query .= "     not exists ( ";     //今年度にない年度が対象
        $query .= "                 SELECT 'x' FROM PERFECT_RECORD_DAT T1 ";
        $query .= "                 WHERE ";
        $query .= "                 T1.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "                 T1.SEMESTER     = T0.SEMESTER AND ";
        $query .= "                 T1.TESTKINDCD   = T0.TESTKINDCD AND ";
        $query .= "                 T1.TESTITEMCD   = T0.TESTITEMCD AND ";
        $query .= "                 T1.CLASSCD      = T0.CLASSCD AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                 T1.SCHOOL_KIND = T0.SCHOOL_KIND AND ";
            $query .= "                 T1.CURRICULUM_CD = T0.CURRICULUM_CD AND ";
        }
        $query .= "                 T1.SUBCLASSCD   = T0.SUBCLASSCD AND ";
        $query .= "                 T1.DIV          = T0.DIV AND ";
        $query .= "                 T1.GRADE        = T0.GRADE AND ";
        $query .= "                 T1.COURSECD     = T0.COURSECD AND ";
        $query .= "                 T1.MAJORCD      = T0.MAJORCD AND ";
        $query .= "                 T1.COURSECODE   = T0.COURSECODE ";
        $query .= "                 )  AND ";
        $query .= "    (( ";
/******************************************************************/
/* 今年度にない教科や課程、学科、コース、学年のものはコピーしない */
/******************************************************************/
        $query .= "     EXISTS (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 COURSE_YDAT T2 ";
        $query .= "             WHERE ";
        $query .= "                 T2.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "                 T2.COURSECD = T0.COURSECD ";
        $query .= "             ) AND ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 MAJOR_YDAT T3 ";
        $query .= "             WHERE ";
        $query .= "                 T3.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "                 T3.COURSECD = T0.COURSECD AND ";
        $query .= "                 T3.MAJORCD  = T0.MAJORCD ";
        $query .= "             ) AND ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 COURSECODE_YDAT T4 ";
        $query .= "             WHERE ";
        $query .= "                 T4.YEAR       = '".CTRL_YEAR."' AND ";
        $query .= "                 T4.COURSECODE = T0.COURSECODE ";
        $query .= "             ) ";
        $query .= "     ) OR T0.COURSECD = '0' ) ";
        $query .= "     AND ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 V_SUBCLASS_MST T5 ";
        $query .= "             WHERE ";
        $query .= "                 T5.YEAR       = '".CTRL_YEAR."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                 T5.CLASSCD = T0.CLASSCD AND ";
            $query .= "                 T5.SCHOOL_KIND = T0.SCHOOL_KIND AND ";
            $query .= "                 T5.CURRICULUM_CD = T0.CURRICULUM_CD AND ";
        }
        $query .= "                 T5.SUBCLASSCD = T0.SUBCLASSCD ";
        $query .= "             ) ";
        $query .= "     AND ";
        $query .= "     (EXISTS (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 SCHREG_REGD_HDAT T6 ";
        $query .= "             WHERE ";
        $query .= "                 T6.YEAR  = '".CTRL_YEAR."' AND ";
        $query .= "                 T6.GRADE = T0.GRADE ";
        $query .= "             ) ";
        $query .= "      OR T0.GRADE = '00') ";
        $query .= "     AND ";
/*********************************************************************/
/* 今年度にすでに設定されている範囲のものはコピーしない              */
/* たとえば区分1がすでに存在していたら重複ではなくても区分2も区分3も */
/* 区分1とかぶってしまうためコピー対象ではない。                     */
/*********************************************************************/
        $query .= "     ((T0.DIV = '01' AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     * ";
        $query .= "                 FROM ";
        $query .= "                     PERFECT_RECORD_DAT T7 ";
        $query .= "                 WHERE ";
        $query .= "                     T7.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "                     T7.SEMESTER     = T0.SEMESTER AND ";
        $query .= "                     T7.TESTKINDCD   = T0.TESTKINDCD AND ";
        $query .= "                     T7.TESTITEMCD   = T0.TESTITEMCD AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     T7.CLASSCD = T0.CLASSCD AND ";
            $query .= "                     T7.SCHOOL_KIND = T0.SCHOOL_KIND AND ";
            $query .= "                     T7.CURRICULUM_CD = T0.CURRICULUM_CD AND ";
        }
        $query .= "                     T7.SUBCLASSCD   = T0.SUBCLASSCD ";
        $query .= "                 )) ";
        $query .= "     OR ";
        $query .= "     (T0.DIV = '02' AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     * ";
        $query .= "                 FROM ";
        $query .= "                     PERFECT_RECORD_DAT T8 ";
        $query .= "                 WHERE ";
        $query .= "                     T8.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "                     T8.SEMESTER     = T0.SEMESTER AND ";
        $query .= "                     T8.TESTKINDCD   = T0.TESTKINDCD AND ";
        $query .= "                     T8.TESTITEMCD   = T0.TESTITEMCD AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     T8.CLASSCD = T0.CLASSCD AND ";
            $query .= "                     T8.SCHOOL_KIND = T0.SCHOOL_KIND AND ";
            $query .= "                     T8.CURRICULUM_CD = T0.CURRICULUM_CD AND ";
        }
        $query .= "                     T8.SUBCLASSCD   = T0.SUBCLASSCD AND ";
        $query .= "                     (T8.GRADE       = T0.GRADE OR ";
        $query .= "                     T8.GRADE        = '00') ";
        $query .= "                 )) ";
        $query .= "     OR ";
        $query .= "     (T0.DIV = '03' AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     * ";
        $query .= "                 FROM ";
        $query .= "                     PERFECT_RECORD_DAT T8 ";
        $query .= "                 WHERE ";
        $query .= "                     T8.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "                     T8.SEMESTER     = T0.SEMESTER AND ";
        $query .= "                     T8.TESTKINDCD   = T0.TESTKINDCD AND ";
        $query .= "                     T8.TESTITEMCD   = T0.TESTITEMCD AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     T8.CLASSCD = T0.CLASSCD AND ";
            $query .= "                     T8.SCHOOL_KIND = T0.SCHOOL_KIND AND ";
            $query .= "                     T8.CURRICULUM_CD = T0.CURRICULUM_CD AND ";
        }
        $query .= "                     T8.SUBCLASSCD   = T0.SUBCLASSCD AND ";
        $query .= "                     (T8.GRADE       = T0.GRADE OR ";
        $query .= "                     T8.GRADE        = '00') AND ";
        $query .= "                     ((T8.COURSECD   = T0.COURSECD AND ";
        $query .= "                     T8.MAJORCD      = T0.MAJORCD AND ";
        $query .= "                     T8.COURSECODE   = T0.COURSECODE) OR ";
        $query .= "                     T8.COURSECD     = '0') ";
        $query .= "                 )) ";
        $query .= "     OR ";
        $query .= "     (T0.DIV = '04' AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     * ";
        $query .= "                 FROM ";
        $query .= "                     PERFECT_RECORD_DAT T8 ";
        $query .= "                 WHERE ";
        $query .= "                     T8.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "                     T8.SEMESTER     = T0.SEMESTER AND ";
        $query .= "                     T8.TESTKINDCD   = T0.TESTKINDCD AND ";
        $query .= "                     T8.TESTITEMCD   = T0.TESTITEMCD AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     T8.CLASSCD = T0.CLASSCD AND ";
            $query .= "                     T8.SCHOOL_KIND = T0.SCHOOL_KIND AND ";
            $query .= "                     T8.CURRICULUM_CD = T0.CURRICULUM_CD AND ";
        }
        $query .= "                     T8.SUBCLASSCD   = T0.SUBCLASSCD AND ";
        $query .= "                     (T8.GRADE       = T0.GRADE OR ";
        $query .= "                     T8.GRADE        = '00') AND ";
        $query .= "                     (T8.MAJORCD     = T0.MAJORCD OR ";
        $query .= "                     T8.MAJORCD      = '000') ";
        $query .= "                 ))) ";
        $query .= " ) ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return true;
   }

    //存在チェック
    function getExists($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = " SELECT ";
        $query .= "     COUNT(*)  ";
        $query .= " FROM ";
        $query .= "     PERFECT_RECORD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR                            = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER                        = '{$model->field["SEMESTER"]}' AND ";
        $query .= "     TESTKINDCD || '-' || TESTITEMCD = '{$model->field["TESTKINDCD"]}' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD                         = '{$subclass_array[0]}' AND ";
            $query .= "     SCHOOL_KIND                     = '{$subclass_array[1]}' AND ";
            $query .= "     CURRICULUM_CD                   = '{$subclass_array[2]}' AND ";
            $query .= "     SUBCLASSCD                      = '{$subclass_array[3]}' AND ";
        } else {
            $query .= "     SUBCLASSCD                      = '{$model->field["SUBCLASSCD"]}' AND ";
        }
        $query .= "     DIV                             = '{$model->field["DIV"]}' AND ";
        $query .= "     GRADE                           = '{$model->field["GRADE"]}' AND ";
        $query .= "     COURSECD                        = '{$model->field["COURSECD"]}' AND ";
        $query .= "     MAJORCD                         = '{$model->field["MAJORCD"]}' AND ";
        $query .= "     COURSECODE                      = '{$model->field["COURSECODE"]}' ";

        return $query;
    }

    // INSERT&UPDATE 一度DELETEしてINSERTする。
    function &getInsertUpdateQuery($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        list($testkindcd, $testitemcd) = preg_split("/-/", $model->field["TESTKINDCD"]);

        if ($model->field["DIV"] == '01') {
            $query  = " DELETE FROM ";
            $query .= "     PERFECT_RECORD_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR            = '".CTRL_YEAR."' AND ";
            $query .= "     SEMESTER        = '{$model->field["SEMESTER"]}' AND ";
            $query .= "     TESTKINDCD      = '{$testkindcd}' AND ";
            $query .= "     TESTITEMCD      = '{$testitemcd}' AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         CLASSCD                         = '{$subclass_array[0]}' AND ";
                $query .= "         SCHOOL_KIND                     = '{$subclass_array[1]}' AND ";
                $query .= "         CURRICULUM_CD                   = '{$subclass_array[2]}' AND ";
                $query .= "         SUBCLASSCD                      = '{$subclass_array[3]}'  ";
            } else {
                $query .= "     SUBCLASSCD      = '{$model->field["SUBCLASSCD"]}' ";
            }

            $db->query($query);
        } else {
            $query  = " DELETE FROM ";
            $query .= "     PERFECT_RECORD_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR            = '".CTRL_YEAR."' AND ";
            $query .= "     SEMESTER        = '{$model->field["SEMESTER"]}' AND ";
            $query .= "     TESTKINDCD      = '{$testkindcd}' AND ";
            $query .= "     TESTITEMCD      = '{$testitemcd}' AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         CLASSCD                         = '{$subclass_array[0]}' AND ";
                $query .= "         SCHOOL_KIND                     = '{$subclass_array[1]}' AND ";
                $query .= "         CURRICULUM_CD                   = '{$subclass_array[2]}' AND ";
                $query .= "         SUBCLASSCD                      = '{$subclass_array[3]}' AND ";
            } else {
                $query .= "     SUBCLASSCD      = '{$model->field["SUBCLASSCD"]}' AND ";
            }
            $query .= "     DIV = '01' ";

            $db->query($query);

            $query  = " DELETE FROM ";
            $query .= "     PERFECT_RECORD_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR            = '".CTRL_YEAR."' AND ";
            $query .= "     SEMESTER        = '{$model->field["SEMESTER"]}' AND ";
            $query .= "     TESTKINDCD      = '{$testkindcd}' AND ";
            $query .= "     TESTITEMCD      = '{$testitemcd}' AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         CLASSCD                         = '{$subclass_array[0]}' AND ";
                $query .= "         SCHOOL_KIND                     = '{$subclass_array[1]}' AND ";
                $query .= "         CURRICULUM_CD                   = '{$subclass_array[2]}' AND ";
                $query .= "         SUBCLASSCD                      = '{$subclass_array[3]}' AND ";
            } else {
                $query .= "     SUBCLASSCD      = '{$model->field["SUBCLASSCD"]}' AND ";
            }
            $query .= "     DIV            != '{$model->field["DIV"]}' AND ";
            $query .= "     GRADE           = '{$model->field["GRADE"]}' ";

            $db->query($query);

            $query  = " DELETE FROM ";
            $query .= "     PERFECT_RECORD_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR            = '".CTRL_YEAR."' AND ";
            $query .= "     SEMESTER        = '{$model->field["SEMESTER"]}' AND ";
            $query .= "     TESTKINDCD      = '{$testkindcd}' AND ";
            $query .= "     TESTITEMCD      = '{$testitemcd}' AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         CLASSCD                         = '{$subclass_array[0]}' AND ";
                $query .= "         SCHOOL_KIND                     = '{$subclass_array[1]}' AND ";
                $query .= "         CURRICULUM_CD                   = '{$subclass_array[2]}' AND ";
                $query .= "         SUBCLASSCD                      = '{$subclass_array[3]}' AND ";
            } else {
                $query .= "     SUBCLASSCD      = '{$model->field["SUBCLASSCD"]}' AND ";
            }
            $query .= "     DIV             = '{$model->field["DIV"]}' AND ";
            $query .= "     GRADE           = '{$model->field["GRADE"]}' AND ";
            $query .= "     COURSECD        = '{$model->field["COURSECD"]}' AND ";
            $query .= "     MAJORCD         = '{$model->field["MAJORCD"]}' AND ";
            $query .= "     COURSECODE      = '{$model->field["COURSECODE"]}' ";

            $db->query($query);
        }

        $query = "";
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
        $data["TESTKINDCD"][TEXT]       = $testkindcd;
        $data["TESTITEMCD"][TEXT]       = $testitemcd;
        $data["CLASSCD"][TEXT]          = substr($model->field["SUBCLASSCD"],0,2);
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["SCHOOL_KIND"][TEXT]     = $subclass_array[1];
            $data["CURRICULUM_CD"][TEXT]   = $subclass_array[2];
            $data["SUBCLASSCD"][TEXT]      = $subclass_array[3];
        } else {
            $data["SUBCLASSCD"][TEXT]       = $model->field["SUBCLASSCD"];
        }
        $data["DIV"][TEXT]              = $model->field["DIV"];
        $data["GRADE"][TEXT]            = $model->field["GRADE"];
        $data["COURSECD"][TEXT]         = $model->field["COURSECD"];
        $data["MAJORCD"][TEXT]          = $model->field["MAJORCD"];
        $data["COURSECODE"][TEXT]       = $model->field["COURSECODE"];

        $data["PERFECT"][NUMBER]        = $model->field["PERFECT"];
        $data["PASS_SCORE"][NUMBER]     = $model->field["PASS_SCORE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "SYSDATE()";

        $query = Query::insertSQL($data, "PERFECT_RECORD_DAT");

        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    // DELETE
    function &getDeleteQuery($model) {
    
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $db = Query::dbCheckOut();
        list($testkindcd, $testitemcd) = preg_split("/-/", $model->field["TESTKINDCD"]);

        $query  = " DELETE FROM ";
        $query .= "     PERFECT_RECORD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER        = '{$model->field["SEMESTER"]}' AND ";
        $query .= "     TESTKINDCD      = '{$testkindcd}' AND ";
        $query .= "     TESTITEMCD      = '{$testitemcd}' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         CLASSCD                         = '{$subclass_array[0]}' AND ";
            $query .= "         SCHOOL_KIND                     = '{$subclass_array[1]}' AND ";
            $query .= "         CURRICULUM_CD                   = '{$subclass_array[2]}' AND ";
            $query .= "         SUBCLASSCD                      = '{$subclass_array[3]}' AND ";
        } else {
            $query .= "     SUBCLASSCD      = '{$model->field["SUBCLASSCD"]}' AND ";
        }
        $query .= "     DIV             = '{$model->field["DIV"]}' AND ";
        $query .= "     GRADE           = '{$model->field["GRADE"]}' AND ";
        $query .= "     COURSECD        = '{$model->field["COURSECD"]}' AND ";
        $query .= "     MAJORCD         = '{$model->field["MAJORCD"]}' AND";
        $query .= "     COURSECODE      = '{$model->field["COURSECODE"]}' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }
}
?>
