<?php

require_once('for_php7.php');

class knjx093bquery extends Query
{
    //年度・学年取得
    public function getYearGrade($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR || '年度 ' || T2.GRADE_NAME1 AS LABEL, ";
        $query .= "     T1.YEAR || '-' || T1.GRADE AS VALUE, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRADE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.GRADE = T2.GRADE AND ";
        $query .= "     T2.SCHOOL_KIND IN ('H', 'J') ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR DESC, ";
        $query .= "     T1.GRADE ";

        return $query;
    }

    //年度一覧
    public function getSelectFieldSQL($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR || '年度 ' || T2.SEMESTERNAME AS LABEL, ";
        $query .= "     T1.YEAR || '-' || T1.SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON T1.YEAR = REG_G.YEAR ";
                $query .= "           AND T1.GRADE = REG_G.GRADE ";
                $query .= "           AND REG_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON T1.YEAR = REG_G.YEAR ";
            $query .= "           AND T1.GRADE = REG_G.GRADE ";
            $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " INNER JOIN ";
        $query .= "     SEMESTER_MST T2 ON  T1.YEAR     = T2.YEAR ";
        $query .= "                     AND T1.SEMESTER = T2.SEMESTER ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //履修登録日コード
    public function getRirekiCode($year, $rireki_code = "")
    {
        $query  = " SELECT ";
        if ($rireki_code) {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     RIREKI_CODE AS VALUE, ";
            $query .= "     SELECT_NAME || ' ' || CHAR(REPLACE(CHAR(SELECT_DATE), '-', '/')) AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     STUDY_SELECT_DATE_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        if ($rireki_code) {
            $query .= "     AND RIREKI_CODE = '{$rireki_code}' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //学年
    public function getGrade($model, $grade = "")
    {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->field["YEAR"]}' AND ";
        $query .= "     SCHOOL_KIND IN ('H', 'J') ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if ($grade) {
            $query .= "     AND GRADE = '".$grade."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     INT(GRADE) ";

        return $query;
    }

    //科目
    public function getSubclassStdDat($model)
    {
        $query  = " SELECT DISTINCT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ' ' || L1.SUBCLASSNAME AS LABEL, ";
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE ";
        } else {
            $query .= "     T1.SUBCLASSCD || ' ' || L1.SUBCLASSNAME AS LABEL, ";
            $query .= "     T1.SUBCLASSCD AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     SUBCLASS_STD_SELECT_RIREKI_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SUBCLASS_MST L1 ON L1.SUBCLASSCD = T1.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                    AND L1.CLASSCD       = T1.CLASSCD ";
            $query .= "                    AND L1.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "                    AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "         T1.YEAR         = '{$model->field["YEAR"]}' ";
        $query .= "     AND T1.SEMESTER     = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.RIREKI_CODE  = '{$model->field["RIREKI_CODE"]}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

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

    //そのデータが存在するか(学籍番号)
    public function schregNoCntSql($year, $semester, $schregno, $courseCd, $majorCd, $courseCode)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$year}' ";
        $query .= "     AND SEMESTER = '{$semester}' ";
        $query .= "     AND SCHREGNO = '{$schregno}' ";

        return $query;
    }

    /**************/
    /* ＣＳＶ出力 */
    /**************/
    //ヘッダー用データ取得
    public function getHeaderData($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE || '-' || T1.GROUPCD AS GROUPCD, ";
        $query .= "     T1.NAME AS GROUPNAME, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS SUBCLASSCD, ";
        } else {
            $query .= "     T2.SUBCLASSCD AS SUBCLASSCD, ";
        }
        $query .= "     T3.SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_COMP_SELECT_MST T1, ";
        $query .= "     SUBCLASS_COMP_SELECT_DAT T2, ";
        $query .= "     V_SUBCLASS_MST T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR          = T2.YEAR AND ";
        $query .= "     T1.YEAR          = T3.YEAR AND ";
        $query .= "     T1.GRADE         = T2.GRADE AND ";
        $query .= "     T1.COURSECD      = T2.COURSECD AND ";
        $query .= "     T1.MAJORCD       = T2.MAJORCD AND ";
        $query .= "     T1.COURSECODE    = T2.COURSECODE AND ";
        $query .= "     T1.GROUPCD       = T2.GROUPCD AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD       = T3.CLASSCD AND ";
            $query .= "     T2.SCHOOL_KIND   = T3.SCHOOL_KIND AND ";
            $query .= "     T2.CURRICULUM_CD = T3.CURRICULUM_CD AND ";
        }
        $query .= "     T2.SUBCLASSCD    = T3.SUBCLASSCD AND";
        if ($model->field["OUTPUT"] == "4") {
            if ($model->Properties["useCurriculumcd"] == '1') {
                if ($model->field["SUBCLASS"] != '99-X-9-999999') {
                    $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD = '".$model->field["SUBCLASS"]."' AND ";
                }
            } else {
                if ($model->field["SUBCLASS"] != '999999') {
                    $query .= "     T2.SUBCLASSCD = '".$model->field["SUBCLASS"]."' AND ";
                }
            }
        }
        $query .= "     T1.YEAR || '-' || T1.GRADE = '".$model->field["YEAR_GRADE"]."' AND ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 SCHREG_REGD_DAT E1, ";
        $query .= "                 SCHREG_REGD_GDAT E2 ";
        $query .= "             WHERE ";
        $query .= "                 E1.YEAR         = E2.YEAR AND ";
        $query .= "                 E1.GRADE        = E2.GRADE AND ";
        $query .= "                 E1.YEAR || '-' || E1.GRADE = '".$model->field["YEAR_GRADE"]."' AND ";
        $query .= "                 E1.COURSECD     = T1.COURSECD AND ";
        $query .= "                 E1.MAJORCD      = T1.MAJORCD AND ";
        $query .= "                 E1.COURSECODE   = T1.COURSECODE AND ";
        $query .= "                 E2.SCHOOL_KIND  = T2.SCHOOL_KIND) ";
        $query .= " ORDER BY ";
        $query .= "     GROUPCD, ";
        $query .= "     SUBCLASSCD ";

        return $query;
    }

    //生徒一覧取得
    public function getSchList($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     '".$model->field["RIREKI_CODE"]."' AS RIREKI_CODE, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".$model->field["YEAR"]."' AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.GRADE    = '".$model->field["GRADE"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //SUBCLASS_STD_SELECT_RIREKI_DAT取得
    public function getSubclassStdSelectRirekiDat($model, $schregno, $groupcd, $subclasscd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_STD_SELECT_RIREKI_DAT T1, ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = T2.YEAR AND ";
        $query .= "     T1.YEAR         = '".$model->field["YEAR"]."' AND ";
        $query .= "     T1.SEMESTER     = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER     = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.RIREKI_CODE  = '".$model->field["RIREKI_CODE"]."' AND ";
        $query .= "     T1.SCHREGNO     = T2.SCHREGNO AND ";
        $query .= "     T1.SCHREGNO     = '".$schregno."' AND ";
        $query .= "     T2.COURSECD || '-' || T2.MAJORCD || '-' || T2.COURSECODE || '-' || T1.GROUPCD = '".$groupcd."' AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$subclasscd."' ";
        } else {
            $query .= "     T1.SUBCLASSCD   = '".$subclasscd."' ";
        }

        return $query;
    }

    /******************/
    /* エラーチェック */
    /******************/
    //存在チェック(選択グループコード)
    public function getCourseGroupcdCnt($year, $grade, $groupcd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_COMP_SELECT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     GRADE   = '".$grade."' AND ";
        $query .= "     COURSECD || '-' || MAJORCD || '-' || COURSECODE || '-' || GROUPCD = '".$groupcd."' ";

        return $query;
    }

    //存在チェック(科目)
    public function getSubclasscdCnt($model, $year, $grade, $groupcd, $subclasscd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_COMP_SELECT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     GRADE   = '".$grade."' AND ";
        $query .= "     COURSECD || '-' || MAJORCD || '-' || COURSECODE || '-' || GROUPCD = '".$groupcd."' AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$subclasscd."' ";
        } else {
            $query .= "     SUBCLASSCD = '".$subclasscd."' ";
        }

        return $query;
    }

    /**************/
    /* ＣＳＶ取込 */
    /**************/
    //更新処理
    public function insertQueryCsv($model, $data_arrH, $data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            for ($j = 0; $j < get_count($data_arrH["GROUPCD"]); $j++) {
                if ($model->Properties["useCurriculumcd"] == '1') {
                    list($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode('-', $data_arrH["SUBCLASSCD"][$j]);
                } else {
                    $classcd = substr($data_arrH["SUBCLASSCD"][$j], 0, 2);
                    $curriculum_cd = '2';
                    $subclasscd = $data_arrH["SUBCLASSCD"][$j];
                }
                list($coursecd, $majorcd, $coursecode, $groupcd) = explode('-', $data_arrH["GROUPCD"][$j]);

                //データセット
                $data = array();
                $data["YEAR"][TEXT]          = $data_arr[$i]["YEAR"];
                $data["SEMESTER"][TEXT]      = $data_arr[$i]["SEMESTER"];
                $data["RIREKI_CODE"][TEXT]   = $data_arr[$i]["RIREKI_CODE"];
                $data["GROUPCD"][TEXT]       = $groupcd;
                $data["CLASSCD"][TEXT]       = $classcd;
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $data["SCHOOL_KIND"][TEXT]   = $school_kind;
                }
                $data["CURRICULUM_CD"][TEXT] = $curriculum_cd;
                $data["SUBCLASSCD"][TEXT]    = $subclasscd;
                $data["SCHREGNO"][TEXT]      = $data_arr[$i]["SCHREGNO"];
                $data["REGISTERCD"][TEXT]    = STAFFCD;
                $data["UPDATED"][NUMBER]     = "SYSDATE()";

                if ($data_arr[$i]["FLG"][$j] == "1") {
                    $query = Query::insertSQL($data, "SUBCLASS_STD_SELECT_RIREKI_DAT");
                    $db->query($query);
                }
            }
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //削除処理
    public function deleteQueryCsv($model, $data_arrH, $data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            for ($j = 0; $j < get_count($data_arrH["GROUPCD"]); $j++) {
                if ($model->Properties["useCurriculumcd"] == '1') {
                    list($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode('-', $data_arrH["SUBCLASSCD"][$j]);
                } else {
                    $subclasscd = $data_arrH["SUBCLASSCD"][$j];
                }
                list($coursecd, $majorcd, $coursecode, $groupcd) = explode('-', $data_arrH["GROUPCD"][$j]);

                //削除
                $query  = " DELETE FROM ";
                $query .= "     SUBCLASS_STD_SELECT_RIREKI_DAT ";
                $query .= " WHERE ";
                $query .= "     YEAR            = '{$data_arr[$i]["YEAR"]}' AND ";
                $query .= "     SEMESTER        = '{$data_arr[$i]["SEMESTER"]}' AND ";
                $query .= "     RIREKI_CODE     = '{$data_arr[$i]["RIREKI_CODE"]}' AND ";
                $query .= "     GROUPCD         = '{$groupcd}' AND ";
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "     CLASSCD         = '{$classcd}' AND ";
                    $query .= "     SCHOOL_KIND     = '{$school_kind}' AND ";
                    $query .= "     CURRICULUM_CD   = '{$curriculum_cd}' AND ";
                }
                $query .= "     SUBCLASSCD      = '{$subclasscd}' AND ";
                $query .= "     SCHREGNO        = '{$data_arr[$i]["SCHREGNO"]}' ";

                $db->query($query);
            }
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }
}
