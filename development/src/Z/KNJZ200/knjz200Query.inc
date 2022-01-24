<?php

require_once('for_php7.php');
class knjz200Query extends Query
{
    //欠課数オーバーの前警告
    public function getNameMst($namecd1, $namecd2)
    {
        $query  = " SELECT NAMESPARE1 FROM NAME_MST ";
        $query .= "  WHERE NAMECD1 = '{$namecd1}' ";
        $query .= "    AND NAMECD2 = '{$namecd2}' ";
        return $query;
    }

    //授業時数のフラグ  欠課数上限値の入力可、付加の判定に使う
    public function getJugyouJisuFlg()
    {
        $query  = " SELECT ";
        $query .= "     JUGYOU_JISU_FLG ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";

        return $query;
    }

    //校種取得
    public function getSchoolKind($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //コース名コンボ取得
    public function getCouseName($model, $flg, $sch_kind)
    {
        if ($flg) {
            $query  = " SELECT DISTINCT ";
            $query .= "     T2.COURSECD, ";
            $query .= "     T3.MAJORCD, ";
            $query .= "     T1.COURSECODE, ";
            $query .= "     T4.GRADE, ";
            $query .= "     T4.YEAR, ";
            $query .= "     T2.COURSENAME, ";
            $query .= "     T3.MAJORNAME, ";
            $query .= "     T1.COURSECODENAME, ";
            $query .= "     T4.GRADE_NAME1 ";
            $query .= " FROM ";
            $query .= "     COURSECODE_MST T1, ";
            $query .= "     COURSE_MST T2, ";
            $query .= "     MAJOR_MST T3, ";
            $query .= "     SCHREG_REGD_GDAT T4 ";
            $query .= " WHERE ";
            $query .= "     T4.YEAR         = '".CTRL_YEAR."' AND ";
            $query .= "     T2.COURSECD     = T3.COURSECD ";
            if ($sch_kind != "ALL") {
                $query .= " AND T4.SCHOOL_KIND  = '".$sch_kind."' ";
            }
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND T4.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T4.SCHOOL_KIND  = '".SCHOOLKIND."' ";
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
            $query .= "     T2.COURSENAME, ";
            $query .= "     T3.MAJORNAME, ";
            $query .= "     T1.COURSECODENAME, ";
            $query .= "     T5.GRADE_NAME1 ";
            $query .= " FROM ";
            $query .= "     COURSECODE_MST T1, ";
            $query .= "     COURSE_MST T2, ";
            $query .= "     MAJOR_MST T3, ";
            $query .= "     SCHREG_REGD_DAT T4, ";
            $query .= "     SCHREG_REGD_GDAT T5 ";
            $query .= " WHERE ";
            $query .= "     T4.YEAR         ='".CTRL_YEAR."' AND ";
            $query .= "     T1.COURSECODE   = T4.COURSECODE AND ";
            $query .= "     T2.COURSECD     = T4.COURSECD AND ";
            $query .= "     T3.COURSECD     = T4.COURSECD AND ";
            $query .= "     T4.MAJORCD      = T3.MAJORCD AND ";
            $query .= "     T4.YEAR         = T5.YEAR AND ";
            $query .= "     T4.GRADE        = T5.GRADE ";
            if ($sch_kind != "ALL") {
                $query .= " AND T5.SCHOOL_KIND  = '".$sch_kind."' ";
            }
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND T5.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T5.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            }
            $query .= " ORDER BY ";
            $query .= "     T4.GRADE, ";
            $query .= "     T4.COURSECD, ";
            $query .= "     T4.MAJORCD, ";
            $query .= "     T4.COURSECODE ";
        }

        return $query;
    }

    //コース一覧取得
    public function getList($model)
    {
        list($coursecode, $coursecd, $majorcd, $grade) = explode(" ", $model->coursename);
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T2.SUBCLASSNAME, ";
        $query .= "     T1.CREDITS, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.REQUIRE_FLG, ";
        $query .= "     T3.NAME1, ";
        $query .= "     T1.AUTHORIZE_FLG, ";
        $query .= "     T1.COMP_UNCONDITION_FLG, ";
        $query .= "     T1.ABSENCE_HIGH, ";
        $query .= "     T1.GET_ABSENCE_HIGH, ";
        if ($model->Properties["useTimeUnit"] == '1') {
            $query .= "     T1.TIME_UNIT, ";
        }
        if (in_array("1", $model->control["SEMESTER"])) {
            $query .= "     T1.ABSENCE_WARN ";
        }
        if (in_array("2", $model->control["SEMESTER"])) {
            $query .= "     ,T1.ABSENCE_WARN2 ";
        }
        if (in_array("3", $model->control["SEMESTER"])) {
            $query .= "     ,T1.ABSENCE_WARN3 ";
        }
        if ($model->Properties["useMultiplicationRate"] == '1') {
            $query .= "     ,T1.RATE ";
        }
        $query .= " FROM ";
        $query .= "     CREDIT_MST T1 ";
        $query .= "     LEFT OUTER JOIN NAME_MST T3 ON T3.NAMECD1='Z011' AND T1.REQUIRE_FLG = T3.NAMECD2, ";
        $query .= "     SUBCLASS_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR       = '".CTRL_YEAR."' ";
        $query .= " AND T1.COURSECODE = '".$coursecode."' ";
        $query .= " AND T1.COURSECD   = '".$coursecd ."' ";
        $query .= " AND T1.MAJORCD    = '".$majorcd ."' ";
        $query .= " AND T1.GRADE      = '".$grade ."' ";
        $query .= " AND T1.SUBCLASSCD = T2.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND T1.CLASSCD       = T2.CLASSCD ";
            $query .= " AND T1.SCHOOL_KIND   = T2.SCHOOL_KIND ";
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
        $query .= "     T1.SUBCLASSCD ";

        return $query;
    }

    //１レコード取得
    public function getRow($model, $db)
    {
        list($coursecode, $coursecd, $majorcd, $grade) = explode(" ", $model->coursename);
        $query  = " SELECT * FROM CREDIT_MST ";
        $query .= "  WHERE COURSECODE = '".$coursecode."' ";
        $query .= "   AND  COURSECD   = '".$coursecd ."' ";
        $query .= "   AND  MAJORCD    = '".$majorcd ."' ";
        $query .= "   AND  GRADE      = '".$grade ."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   AND  CLASSCD         = '".$model->classcd."' ";
            $query .= "   AND  SCHOOL_KIND     = '".$model->school_kind."' ";
            $query .= "   AND  CURRICULUM_CD   = '".$model->curriculum_cd."' ";
            $query .= "   AND  SUBCLASSCD      = '".$model->subclasscd."' ";
        } else {
            $query .= "   AND  CLASSCD    = '".substr($model->subclasscd, 0, 2)."' ";
            $query .= "   AND  SUBCLASSCD      = '".$model->subclasscd."' ";
        }
        $query .= "   AND  YEAR       = '".CTRL_YEAR."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        return $row;
    }

    //重複チェック
    public function chkCODE($model, $db)
    {
        list($coursecode, $coursecd, $majorcd, $grade) = explode(" ", $model->coursename);
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $classcd        = substr($model->field["SUBCLASSCD"], 0, 2);
            $school_kind    = substr($model->field["SUBCLASSCD"], 3, 1);
            $curriculum_cd  = substr($model->field["SUBCLASSCD"], 5, 1);
            $suclasscd      = substr($model->field["SUBCLASSCD"], 7, 6);
        }
    
        $query  = " SELECT * FROM CREDIT_MST ";
        $query .= "  WHERE COURSECODE = '".$coursecode."' ";
        $query .= "   AND  COURSECD   = '".$coursecd ."' ";
        $query .= "   AND  MAJORCD    = '".$majorcd ."' ";
        $query .= "   AND  GRADE      = '".$grade ."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND  CLASSCD        = '".$classcd."' ";
            $query .= " AND SCHOOL_KIND     = '".$school_kind."' ";
            $query .= " AND CURRICULUM_CD   = '".$curriculum_cd."' ";
            $query .= " AND SUBCLASSCD      = '".$suclasscd."' ";
        } else {
            $query .= "   AND  CLASSCD    = '".substr($model->field["SUBCLASSCD"], 0, 2)."' ";
            $query .= "   AND  SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= "   AND  YEAR       = '".CTRL_YEAR."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        return $row;
    }

    //学年の校種
    public function getSchregRegdGdatSchoolKind($model, $db)
    {
        list($coursecode, $coursecd, $majorcd, $grade) = explode(" ", $model->coursename);
        $query  = " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT ";
        $query .= "  WHERE GRADE      = '".$grade ."' ";
        $query .= "   AND  YEAR       = '".CTRL_YEAR."'";

        $schoolKind = $db->getOne($query);
        return $schoolKind;
    }

    //科目コンボ
    public function getSubclass($model)
    {
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || ':' || SUBCLASSNAME AS LABEL, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE ";
        } else {
            $query .= "     SUBCLASSCD || ':' || SUBCLASSNAME AS LABEL, ";
            $query .= "     SUBCLASSCD AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR ='".CTRL_YEAR."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学籍在籍データの件数取得
    public function getRegdDatCnt($grade = "")
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";
        if ($grade) {
            $query .= "     AND GRADE = '". $grade ."' ";
        }

        return $query;
    }

    //前年度からのデータを取得
    public function &getOldYear($model, $flg, $coursecode)
    {
        $query  = " SELECT * ";
        $query .= " FROM ";
        $query .= "     credit_mst m ";
        $query .= " WHERE ";
        $query .= "     m.year = '".(CTRL_YEAR-1)."' AND ";
        $query .= "     m.coursecode = '".$coursecode."' AND ";
        $query .= "     not exists (SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     CREDIT_MST m3 ";
        $query .= "                 WHERE ";
        $query .= "                     m3.year = '".CTRL_YEAR."' ";
        $query .= "                 AND m.coursecode = m3.coursecode ";
        $query .= "                 AND m.coursecd = m3.coursecd ";
        $query .= "                 AND m.majorcd = m3.majorcd ";
        $query .= "                 AND m.grade = m3.grade ";
        $query .= "                 AND m.classcd = m3.classcd ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                 AND m.SCHOOL_KIND = m3.SCHOOL_KIND ";
            $query .= "                 AND m.CURRICULUM_CD = m3.CURRICULUM_CD ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "             AND m.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "                 AND m.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        $query .= "                 AND m.subclasscd = m3.subclasscd ";
        $query .= "                 ) ";
        $query .= " AND  exists (SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 coursecode_mst m2 ";
        $query .= "             WHERE ";
        $query .= "                 m.coursecode = m2.coursecode ";
        $query .= "             ) ";
        $query .= " AND exists (SELECT ";
        $query .= "                'X' ";
        $query .= "             FROM ";
        $query .= "                 v_subclass_mst m4 ";
        $query .= "             WHERE ";
        $query .= "                 m4.year = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                 AND m.CLASSCD = m4.CLASSCD ";
            $query .= "                 AND m.SCHOOL_KIND = m4.SCHOOL_KIND ";
            $query .= "                 AND m.CURRICULUM_CD = m4.CURRICULUM_CD ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "             AND m.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "                 AND m.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        $query .= "             AND m.subclasscd = m4.subclasscd ";
        $query .= "             ) ";
        if (!$flg) {
            $query .= " AND  exists (SELECT ";
            $query .= "                 'X' ";
            $query .= "             FROM ";
            $query .= "                 SCHREG_REGD_DAT m5 ";
            $query .= "             WHERE ";
            $query .= "                 m5.YEAR = '".CTRL_YEAR."' AND ";
            $query .= "                 m5.COURSECODE = m.COURSECODE AND ";
            $query .= "                 m5.GRADE = m.GRADE AND ";
            $query .= "                 m5.MAJORCD = m.MAJORCD AND ";
            $query .= "                 m5.COURSECODE = m.COURSECODE ";
            $query .= "             ) ";
        }
        $query .= " AND  exists (SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 SCHREG_REGD_GDAT m6 ";
        $query .= "             WHERE ";
        $query .= "                 m6.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "                 m6.GRADE        = m.GRADE ";
        if ($model->sch_kind != "ALL") {
            $query .= "             AND m6.SCHOOL_KIND  = '".$model->sch_kind."' ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "         AND m6.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "             AND m6.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= "             ) ";

        return $query;
    }

    //前年度からのデータを今年度にINSERT
    public function &copyInsert($Row, $model)
    {
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["GRADE"][TEXT]                = $Row["GRADE"];
        $data["CLASSCD"][TEXT]              = $Row["CLASSCD"];
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["SCHOOL_KIND"][TEXT]    = $Row["SCHOOL_KIND"];
            $data["CURRICULUM_CD"][TEXT]  = $Row["CURRICULUM_CD"];
            $data["SUBCLASSCD"][TEXT]     = $Row["SUBCLASSCD"];
        }
        $data["COURSECODE"][TEXT]           = $Row["COURSECODE"];
        $data["COURSECD"][TEXT]             = $Row["COURSECD"];
        $data["MAJORCD"][TEXT]              = $Row["MAJORCD"];
        $data["SUBCLASSCD"][TEXT]           = $Row["SUBCLASSCD"];
        $data["CREDITS"][NUMBER]            = $Row["CREDITS"];
        $data["ABSENCE_HIGH"][NUMBER]       = $Row["ABSENCE_HIGH"];
        $data["GET_ABSENCE_HIGH"][NUMBER]   = $Row["GET_ABSENCE_HIGH"];
        $data["ABSENCE_WARN"][NUMBER]       = $Row["ABSENCE_WARN"];
        $data["ABSENCE_WARN2"][NUMBER]      = $Row["ABSENCE_WARN2"];
        $data["ABSENCE_WARN3"][NUMBER]      = $Row["ABSENCE_WARN3"];
        $data["REQUIRE_FLG"][TEXT]          = $Row["REQUIRE_FLG"];
        $data["AUTHORIZE_FLG"][TEXT]        = $Row["AUTHORIZE_FLG"];
        $data["COMP_UNCONDITION_FLG"][TEXT] = $Row["COMP_UNCONDITION_FLG"];
        $data["TIME_UNIT"][NUMBER]          = $Row["TIME_UNIT"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        $data["RATE"][NUMBER]               = $Row["RATE"];
        $query = Query::insertSQL($data, "CREDIT_MST");
        return $query;
    }

    //INSERT
    public function &getInsertQuery($model)
    {
        list($coursecode, $coursecd, $majorcd, $grade) = explode(" ", $model->coursename);
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $classcd        = substr($model->field["SUBCLASSCD"], 0, 2);
            $school_kind    = substr($model->field["SUBCLASSCD"], 3, 1);
            $curriculum_cd  = substr($model->field["SUBCLASSCD"], 5, 1);
            $suclasscd      = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        if ($model->cmd == "update") {
            knjz200Query::getDeleteQuery($model); //update時
        }

        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["COURSECODE"][TEXT]           = $coursecode;
        $data["COURSECD"][TEXT]             = $coursecd;
        $data["MAJORCD"][TEXT]              = $majorcd;
        $data["GRADE"][TEXT]                = $grade;
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]        = $classcd;
            $data["SCHOOL_KIND"][TEXT]    = $school_kind;
            $data["CURRICULUM_CD"][TEXT]  = $curriculum_cd;
            $data["SUBCLASSCD"][TEXT]     = $suclasscd;
        } else {
            $data["CLASSCD"][TEXT]        = substr($model->field["SUBCLASSCD"], 0, 2);
            $data["SUBCLASSCD"][TEXT]     = $model->field["SUBCLASSCD"];
        }
        $data["CREDITS"][NUMBER]            = $model->field["CREDITS"];
        if ($model->jugyou_jisu_flg != '2' || $model->cmd != 'add') {
            $data["ABSENCE_HIGH"][NUMBER]       = $model->field["ABSENCE_HIGH"];
            $data["GET_ABSENCE_HIGH"][NUMBER]   = $model->field["GET_ABSENCE_HIGH"];
        }
        $data["ABSENCE_WARN"][NUMBER]       = $model->field["ABSENCE_WARN"];
        $data["ABSENCE_WARN2"][NUMBER]      = $model->field["ABSENCE_WARN2"];
        $data["ABSENCE_WARN3"][NUMBER]      = $model->field["ABSENCE_WARN3"];
        $data["REQUIRE_FLG"][TEXT]          = $model->field["REQUIRE_FLG"];
        $data["AUTHORIZE_FLG"][TEXT]        = $model->field["AUTHORIZE_FLG"];
        $data["COMP_UNCONDITION_FLG"][TEXT] = $model->field["COMP_UNCONDITION_FLG"];
        if ($model->Properties["useTimeUnit"] == '1') {
            $data["TIME_UNIT"][NUMBER]          = $model->field["TIME_UNIT"];
        }
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($model->Properties["useMultiplicationRate"] == '1') {
            $data["RATE"][NUMBER]          = $model->field["RATE"];
        }
        $query = Query::insertSQL($data, "CREDIT_MST");

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    public function &getDeleteQuery($model)
    {
        list($coursecode, $coursecd, $majorcd, $grade) = explode(" ", $model->coursename);
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $classcd        = substr($model->field["SUBCLASSCD"], 0, 2);
            $school_kind    = substr($model->field["SUBCLASSCD"], 3, 1);
            $curriculum_cd  = substr($model->field["SUBCLASSCD"], 5, 1);
            $suclasscd      = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $db = Query::dbCheckOut();

        $query  = "DELETE FROM credit_mst ";
        $query .= "     WHERE YEAR       = '".CTRL_YEAR."' ";
        $query .= "       AND COURSECD   = '" .$coursecd ."' ";
        $query .= "       AND MAJORCD    = '" .$majorcd ."' ";
        $query .= "       AND GRADE      = '" .$grade ."' ";
        $query .= "       AND COURSECODE = '" .$coursecode ."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND CLASSCD         = '".$classcd."' ";
            $query .= " AND SCHOOL_KIND     = '".$school_kind."' ";
            $query .= " AND CURRICULUM_CD   = '".$curriculum_cd."' ";
            $query .= " AND SUBCLASSCD      = '".$suclasscd."' ";
        } else {
            $query .= "     AND CLASSCD    = '".substr($model->subclasscd, 0, 2)."' ";
            $query .= "     AND SUBCLASSCD  = '{$model->field["SUBCLASSCD"]}' ";
        }

        $db->query($query);
        Query::dbCheckIn($db);
    }

    //必履修区分取得
    public function getRequireName($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || '：' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    ='".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 ='Z011' ";

        return $query;
    }

    //単位マスタ取得
    public function getSubQuery1($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     CREDIT_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".CTRL_YEAR."' ";
        $query .= " AND T1.COURSECD     = '".$model->coursecd."' ";
        $query .= " AND T1.MAJORCD      = '".$model->majorcd."' ";
        $query .= " AND T1.GRADE        = '".$model->grade."' ";
        $query .= " AND T1.COURSECODE   = '".$model->coursecode."' ";
        $query .= " AND T1.SUBCLASSCD   = '".$model->subclasscd."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND T1.CLASSCD          = '".$model->classcd."' ";
            $query .= " AND T1.SCHOOL_KIND      = '".$model->school_kind."' ";
            $query .= " AND T1.CURRICULUM_CD    = '".$model->curriculum_cd."' ";
        }

        return $query;
    }

    //課程学科一覧取得
    public function getCourseList($model, $flg)
    {
        if ($flg) {
            $query  = " SELECT DISTINCT ";
            $query .= "     T2.COURSECD || T3.MAJORCD || T1.COURSECODE AS VALUE, ";
            $query .= "     T2.COURSECD || T3.MAJORCD || T1.COURSECODE || ' ' || T2.COURSENAME || T3.MAJORNAME || ' ' || T1.COURSECODENAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     COURSECODE_MST T1, ";
            $query .= "     COURSE_MST T2, ";
            $query .= "     MAJOR_MST T3, ";
            $query .= "     SCHREG_REGD_GDAT T4 ";
            $query .= " WHERE ";
            $query .= "     T4.YEAR         = '".CTRL_YEAR."' AND ";
            $query .= "     T4.GRADE        = '".$model->grade."' AND ";
            $query .= "     T2.COURSECD     = T3.COURSECD ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        } else {
            $query  = " SELECT DISTINCT ";
            $query .= "     T4.COURSECD || T4.MAJORCD || T4.COURSECODE AS VALUE, ";
            $query .= "     T4.COURSECD || T4.MAJORCD || T4.COURSECODE || ' ' || T2.COURSENAME || T3.MAJORNAME || ' ' || T1.COURSECODENAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     COURSECODE_MST T1, ";
            $query .= "     COURSE_MST T2, ";
            $query .= "     MAJOR_MST T3, ";
            $query .= "     SCHREG_REGD_DAT T4 ";
            $query .= " WHERE ";
            $query .= "     T4.YEAR         = '".CTRL_YEAR."' AND ";
            $query .= "     T4.GRADE        = '".$model->grade."' AND ";
            $query .= "     T1.COURSECODE   = T4.COURSECODE AND ";
            $query .= "     T2.COURSECD     = T4.COURSECD AND ";
            $query .= "     T3.COURSECD     = T4.COURSECD AND ";
            $query .= "     T3.MAJORCD      = T4.MAJORCD ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //科目取得
    public function getSubclassList($model)
    {
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || ':' || SUBCLASSNAME AS LABEL ";
        } else {
            $query .= "     SUBCLASSCD AS VALUE, ";
            $query .= "     SUBCLASSCD || ' ' || SUBCLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR ='".CTRL_YEAR."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //単位マスタ取得
    public function checkCreditMst($model, $course, $subclass)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CREDIT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     COURSECD || MAJORCD || COURSECODE = '".$course."' AND ";
        $query .= "     GRADE       = '".$model->grade."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD  = '".$subclass."' ";
        } else {
            $query .= "     CLASSCD     = '".substr($subclass, 0, 2)."' AND ";
            $query .= "     SUBCLASSCD  = '".$subclass."' ";
        }

        return $query;
    }

    // UPDATE（一括更新）
    public function &getRepUpdateQuery($db, $model, $fields, $course, $subclass)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            list($classcd, $school_kind, $curriculum_cd, $suclasscd) = explode('-', $subclass);
        }

        //単位マスタ存在チェック
        $check_creditM = $db->getOne(knjz200Query::checkCreditMst($model, $course, $subclass));

        //更新対象項目
        $check_item = array(
            "CREDITS"               =>  NUMBER,
            "ABSENCE_HIGH"          =>  NUMBER,
            "GET_ABSENCE_HIGH"      =>  NUMBER,
            "ABSENCE_WARN"          =>  NUMBER,
            "ABSENCE_WARN2"         =>  NUMBER,
            "ABSENCE_WARN3"         =>  NUMBER,
            "REQUIRE_FLG"           =>  TEXT,
            "AUTHORIZE_FLG"         =>  TEXT,
            "COMP_UNCONDITION_FLG"  =>  TEXT,
            "TIME_UNIT"             =>  NUMBER,
            "RATE"                  =>  NUMBER,
        );

        foreach ($fields as $key => $val) {
            $data[$key][$check_item[$key]]   = $fields[$key];
        }
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        if ($check_creditM == "" && $fields["CREDITS"] != "") {
            //追加
            $data["YEAR"][TEXT]                 = CTRL_YEAR;
            $data["COURSECODE"][TEXT]           = substr($course, 4);
            $data["COURSECD"][TEXT]             = substr($course, 0, 1);
            $data["MAJORCD"][TEXT]              = substr($course, 1, 3);
            $data["GRADE"][TEXT]                = $model->grade;
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["CLASSCD"][TEXT]          = $classcd;
                $data["SCHOOL_KIND"][TEXT]      = $school_kind;
                $data["CURRICULUM_CD"][TEXT]    = $curriculum_cd;
                $data["SUBCLASSCD"][TEXT]       = $suclasscd;
            } else {
                $data["CLASSCD"][TEXT]          = substr($subclass, 0, 2);
                $data["SUBCLASSCD"][TEXT]       = $subclass;
            }
    
            $query = Query::insertSQL($data, "CREDIT_MST");
        } else {
            //更新
            $where  = " WHERE ";
            $where .= "     YEAR        = '". CTRL_YEAR ."' AND ";
            $where .= "     COURSECD || MAJORCD || COURSECODE = '".$course."' AND ";
            $where .= "     GRADE       = '".$model->grade."' AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $where .= "     CLASSCD         = '".$classcd."' AND ";
                $where .= "     SCHOOL_KIND     = '".$school_kind."' AND ";
                $where .= "     CURRICULUM_CD   = '".$curriculum_cd."' AND ";
                $where .= "     SUBCLASSCD      = '".$suclasscd."' ";
            } else {
                $where .= "     CLASSCD     = '".substr($subclass, 0, 2)."' AND ";
                $where .= "     SUBCLASSCD  = '".$subclass."' ";
            }

            $query = Query::updateSQL($data, "CREDIT_MST", $where);
        }

        return $query;
    }
    
    //CSVデータ*****/
    //CSV書出し
    public function getCsvData($model)
    {
        $query  = " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     GRADE, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     COURSECODE, ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     CREDITS, ";
        $query .= "     ABSENCE_HIGH, ";
        $query .= "     GET_ABSENCE_HIGH, ";
        //欠課数オーバ
        if (in_array("1", $model->control["SEMESTER"])) {
            $query .= "     ABSENCE_WARN, ";
        }
        if (in_array("2", $model->control["SEMESTER"])) {
            $query .= "     ABSENCE_WARN2, ";
        }
        if (in_array("3", $model->control["SEMESTER"])) {
            $query .= "     ABSENCE_WARN3, ";
        }
        $query .= "     REQUIRE_FLG, ";
        $query .= "     AUTHORIZE_FLG, ";
        $query .= "     COMP_UNCONDITION_FLG, ";
        //時間単位
        if ($model->Properties["useTimeUnit"] == '1') {
            $query .= "     TIME_UNIT, ";
        }
        //掛け率
        if ($model->Properties["useMultiplicationRate"] == '1') {
            $query .= "     RATE, ";
        }
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     CREDIT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
                $query .= "                     SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                $query .= "              ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT ";
            $query .= "                 WHERE ";
            $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= "              ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     GRADE, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     COURSECODE ";

        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT] = PROGRAMID;
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //学年チェック
    public function checkGrade($model, $data)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$data["YEAR"]."' AND ";
        $query .= "     GRADE       = '".$data["GRADE"]."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //科目取得
    public function checkSubclass($model, $data)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".$data["YEAR"]."' AND ";
        $query .= "     CLASSCD         = '".$data["CLASSCD"]."' AND ";
        $query .= "     SCHOOL_KIND     = '".$data["SCHOOL_KIND"]."' AND ";
        $query .= "     CURRICULUM_CD   = '".$data["CURRICULUM_CD"]."' AND ";
        $query .= "     SUBCLASSCD      = '".$data["SUBCLASSCD"]."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOL_KIND     = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //単位マスタ登録データチェック
    public function getCreditCount($setData)
    {
        $query  = "SELECT ";
        $query .= "    COUNT(*) AS CNT ";
        $query .= "FROM ";
        $query .= "    CREDIT_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '{$setData["YEAR"]}' ";
        $query .= "AND GRADE = '{$setData["GRADE"]}' ";
        $query .= "AND COURSECD = '{$setData["COURSECD"]}' ";
        $query .= "AND MAJORCD = '{$setData["MAJORCD"]}' ";
        $query .= "AND COURSECODE = '{$setData["COURSECODE"]}' ";
        $query .= "AND CLASSCD = '{$setData["CLASSCD"]}' ";
        $query .= "AND SCHOOL_KIND = '{$setData["SCHOOL_KIND"]}' ";
        $query .= "AND CURRICULUM_CD = '{$setData["CURRICULUM_CD"]}' ";
        $query .= "AND SUBCLASSCD = '{$setData["SUBCLASSCD"]}' ";

        return $query;
    }

    //CSVファイルよりDBへインサート
    public function insertQueryCsv($db, $model, $data_arr)
    {
        $data = array();
        $db->autoCommit(false);

        $cnt = 0;    //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //データセット
            $data["YEAR"][TEXT] = $data_arr[$i]["YEAR"];
            $data["GRADE"][TEXT] = $data_arr[$i]["GRADE"];
            $data["COURSECD"][TEXT] = $data_arr[$i]["COURSECD"];
            $data["MAJORCD"][TEXT] = $data_arr[$i]["MAJORCD"];
            $data["COURSECODE"][TEXT] = $data_arr[$i]["COURSECODE"];
            $data["CLASSCD"][TEXT] = $data_arr[$i]["CLASSCD"];
            $data["SCHOOL_KIND"][TEXT] = $data_arr[$i]["SCHOOL_KIND"];
            $data["CURRICULUM_CD"][TEXT] = $data_arr[$i]["CURRICULUM_CD"];
            $data["SUBCLASSCD"][TEXT] = $data_arr[$i]["SUBCLASSCD"];
            $data["CREDITS"][NUMBER] = $data_arr[$i]["CREDITS"];
            $data["ABSENCE_HIGH"][NUMBER] = $data_arr[$i]["ABSENCE_HIGH"];
            $data["GET_ABSENCE_HIGH"][NUMBER] = $data_arr[$i]["GET_ABSENCE_HIGH"];
            //欠課数オーバ
            if (in_array("1", $model->control["SEMESTER"])) {
                $data["ABSENCE_WARN"][NUMBER] = $data_arr[$i]["ABSENCE_WARN"];
            }
            if (in_array("2", $model->control["SEMESTER"])) {
                $data["ABSENCE_WARN2"][NUMBER] = $data_arr[$i]["ABSENCE_WARN2"];
            }
            if (in_array("3", $model->control["SEMESTER"])) {
                $data["ABSENCE_WARN3"][NUMBER] = $data_arr[$i]["ABSENCE_WARN3"];
            }
            $data["REQUIRE_FLG"][TEXT] = $data_arr[$i]["REQUIRE_FLG"];
            $data["AUTHORIZE_FLG"][TEXT] = $data_arr[$i]["AUTHORIZE_FLG"];
            $data["COMP_UNCONDITION_FLG"][TEXT] = $data_arr[$i]["COMP_UNCONDITION_FLG"];
            //時間単位
            if ($model->Properties["useTimeUnit"] == '1') {
                $data["TIME_UNIT"][NUMBER] = $data_arr[$i]["TIME_UNIT"];
            }
            $data["REGISTERCD"][TEXT]          = STAFFCD;
            $data["UPDATED"][NUMBER]           = "SYSDATE()";
            //掛け率
            if ($model->Properties["useMultiplicationRate"] == '1') {
                $data["RATE"][NUMBER] = $data_arr[$i]["RATE"];
            }
            //登録データチェック
            $query  = knjz200Query::getCreditCount($data_arr[$i]);
            if (1 > $db->getOne($query)) {
                $query = Query::insertSQL($data, "CREDIT_MST");
            } else {
                $where  = "WHERE ";
                $where .= "    YEAR = '{$data_arr[$i]["YEAR"]}' ";
                $where .= "AND GRADE = '{$data_arr[$i]["GRADE"]}' ";
                $where .= "AND COURSECD = '{$data_arr[$i]["COURSECD"]}' ";
                $where .= "AND MAJORCD = '{$data_arr[$i]["MAJORCD"]}' ";
                $where .= "AND COURSECODE = '{$data_arr[$i]["COURSECODE"]}' ";
                $where .= "AND CLASSCD = '{$data_arr[$i]["CLASSCD"]}' ";
                $where .= "AND SCHOOL_KIND = '{$data_arr[$i]["SCHOOL_KIND"]}' ";
                $where .= "AND CURRICULUM_CD = '{$data_arr[$i]["CURRICULUM_CD"]}' ";
                $where .= "AND SUBCLASSCD = '{$data_arr[$i]["SUBCLASSCD"]}' ";

                $query = Query::updateSQL($data, "CREDIT_MST", $where);
            }
            $db->query($query);
            $cnt++;
        }
        $db->commit();

        return $cnt;
    }
}
