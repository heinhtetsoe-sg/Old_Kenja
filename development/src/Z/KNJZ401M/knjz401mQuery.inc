<?php

require_once('for_php7.php');

class knjz401mQuery extends Query
{
    //学年学期取得
    public function getGradeSemes($model, $grade_semes = "")
    {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || '-' || T2.SEMESTER AS VALUE, ";
        $query .= "     T1.GRADE_NAME1 || ' ' || T2.SEMESTERNAME AS LABEL, ";
        $query .= "     T1.SCHOOL_KIND ";
        $query .= " FROM  ";
        $query .= "     SCHREG_REGD_GDAT T1, ";
        $query .= "     SEMESTER_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T2.SEMESTER <> '9' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if ($grd_seme) {
            $query .= " AND T1.GRADE || '-' || T2.SEMESTER <> '".$grade_semes."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.SCHOOL_KIND DESC, ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧取得
    public function getLeftList($model, $grade_semes, $flg)
    {
        list($grade, $semester) = explode('-', $grade_semes);

        $query  = " SELECT DISTINCT ";
        if ($flg == "cnt") {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
                $query .= "     COUNT(*) AS CNT ";
            } else {
                $query .= "     T1.SUBCLASSCD, ";
                $query .= "     COUNT(T1.SUBCLASSCD) AS CNT ";
            }
        } else {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
            } else {
                $query .= "     T1.SUBCLASSCD, ";
            }
            $query .= "     L1.SUBCLASSNAME, ";
            $query .= "     T1.VIEWCD, ";
            $query .= "     T1.VIEWCD || ':' || VALUE(T1.VIEWABBV,'') AS VIEWABBV, ";
            $query .= "     D1.REMARK1 ";
        }
        $query .= " FROM  ";
        $query .= "     JVIEWNAME_GRADE_MST T1 ";
        $query .= "     LEFT JOIN V_SUBCLASS_MST L1 ";
        $query .= "              ON L1.YEAR             = '".CTRL_YEAR."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND L1.CLASSCD          = T1.CLASSCD ";
            $query .= "         AND L1.SCHOOL_KIND      = T1.SCHOOL_KIND ";
            $query .= "         AND L1.CURRICULUM_CD    = T1.CURRICULUM_CD ";
        }
        $query .= "             AND L1.SUBCLASSCD       = T1.SUBCLASSCD ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_SEMES_DETAIL_MST D1 ";
        $query .= "              ON D1.YEAR             = '".CTRL_YEAR."' ";
        $query .= "             AND D1.SEMESTER         = '".$semester."' ";
        $query .= "             AND D1.GRADE            = T1.GRADE ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND D1.CLASSCD          = T1.CLASSCD ";
            $query .= "         AND D1.SCHOOL_KIND      = T1.SCHOOL_KIND ";
            $query .= "         AND D1.CURRICULUM_CD    = T1.CURRICULUM_CD ";
        }
        $query .= "             AND D1.SUBCLASSCD       = T1.SUBCLASSCD ";
        $query .= "             AND D1.VIEWCD           = T1.VIEWCD ";
        $query .= " WHERE ";
        $query .= "     T1.GRADE = '".$grade."' ";
        if ($flg == "cnt") {
            $query .= " GROUP BY ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD, ";
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
            }
            $query .= "     T1.SUBCLASSCD ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     SUBCLASSCD, ";
            $query .= "     T1.VIEWCD ";
        }

        return $query;
    }

    //科目取得
    public function getSubClassName($model, $subclasscd)
    {
        $query  = " SELECT ";
        $query .= "     SUBCLASSNAME ";
        $query .= " FROM  ";
        $query .= "     SUBCLASS_MST ";
        $query .= " WHERE ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$subclasscd."' ";
        } else {
            $query .= "     SUBCLASSCD = '".$subclasscd."' ";
        }

        return $query;
    }

    //データ取得
    public function getData($model, $grade_semes, $subclasscd)
    {
        list($grade, $semester) = explode('-', $grade_semes);

        $query  = " SELECT ";
        $query .= "     T1.VIEWCD, ";
        $query .= "     T1.VIEWABBV, ";
        $query .= "     D1.REMARK1 ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_MST T1 ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_SEMES_DETAIL_MST D1 ";
        $query .= "              ON D1.YEAR             = '".CTRL_YEAR."' ";
        $query .= "             AND D1.SEMESTER         = '".$semester."' ";
        $query .= "             AND D1.GRADE            = T1.GRADE ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND D1.CLASSCD          = T1.CLASSCD ";
            $query .= "         AND D1.SCHOOL_KIND      = T1.SCHOOL_KIND ";
            $query .= "         AND D1.CURRICULUM_CD    = T1.CURRICULUM_CD ";
        }
        $query .= "             AND D1.SUBCLASSCD       = T1.SUBCLASSCD ";
        $query .= "             AND D1.VIEWCD           = T1.VIEWCD ";
        $query .= " WHERE ";
        $query .= "     T1.GRADE = '".$grade."' AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$subclasscd."' ";
        } else {
            $query .= "     T1.SUBCLASSCD = '".$subclasscd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.VIEWCD ";

        return $query;
    }

    //UPDATE
    public function &getUpdateQuery($model, $fields)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        list($grade, $semester) = explode('-', $model->grade_semes);
        list($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode('-', $model->subclasscd);

        //削除
        $query  = " DELETE FROM ";
        $query .= "     JVIEWNAME_GRADE_SEMES_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".$semester."' AND ";
        $query .= "     GRADE       = '".$grade."' AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->subclasscd."' ";
        } else {
            $query .= "     SUBCLASSCD = '".$model->subclasscd."' ";
        }
        $db->query($query);

        //追加
        $result = $db->query(knjz401mQuery::getData($model, $model->grade_semes, $model->subclasscd));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SEMESTER"][TEXT]         = $semester;
            $data["GRADE"][TEXT]            = $grade;

            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["CLASSCD"][TEXT]          = $classcd;
                $data["SCHOOL_KIND"][TEXT]      = $school_kind;
                $data["CURRICULUM_CD"][TEXT]    = $curriculum_cd;
                $data["SUBCLASSCD"][TEXT]       = $subclasscd;
            } else {
                $data["SUBCLASSCD"][TEXT]       = $model->subclasscd;
            }
            $data["VIEWCD"][TEXT]           = $row["VIEWCD"];
            $data["REMARK1"][TEXT]          = $fields["REMARK1_".$row["VIEWCD"]];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $query = Query::insertSQL($data, "JVIEWNAME_GRADE_SEMES_DETAIL_MST");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    //データチェック
    public function checkData($model, $grade_semes, $grade_semes2, $flg, $year)
    {
        list($grade, $semester)    = explode('-', $grade_semes);
        list($grade2, $semester2)  = explode('-', $grade_semes2);

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_SEMES_DETAIL_MST T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$year."' AND ";
        $query .= "     SEMESTER    = '".$semester."' ";
        if ($flg != "pre_copy") {
            $query .= " AND GRADE   = '".$grade."' ";
        }
        if ($flg == "moto") {
            $query .= " AND EXISTS (SELECT ";
            $query .= "                 'X' ";
            $query .= "             FROM ";
            $query .= "                 JVIEWNAME_GRADE_MST E1 ";
            $query .= "             WHERE ";
            $query .= "                 E1.GRADE        = '".$grade2."' AND ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                 E1.CLASSCD      = T1.CLASSCD AND ";
                $query .= "                 E1.SCHOOL_KIND  = T1.SCHOOL_KIND AND ";
                $query .= "                 E1.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
            }
            $query .= "                 E1.SUBCLASSCD   = T1.SUBCLASSCD AND ";
            $query .= "                 E1.VIEWCD       = T1.VIEWCD ";
            $query .= "             ) ";
        }

        return $query;
    }

    //コピー
    public function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        list($grade, $semester)    = explode('-', $model->grade_semes);
        list($grade2, $semester2)  = explode('-', $model->r_grade_semes);

        $query  = " INSERT INTO JVIEWNAME_GRADE_SEMES_DETAIL_MST ";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     '".$semester."' AS SEMESTER, ";
        $query .= "     '".$grade."' AS SEMESTER, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "     SUBCLASSCD, ";
        $query .= "     VIEWCD, ";
        $query .= "     REMARK1, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_SEMES_DETAIL_MST T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".$semester2."' AND ";
        $query .= "     GRADE       = '".$grade2."' AND ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 JVIEWNAME_GRADE_MST E1 ";
        $query .= "             WHERE ";
        $query .= "                 E1.GRADE        = '".$grade."' AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                 E1.CLASSCD      = T1.CLASSCD AND ";
            $query .= "                 E1.SCHOOL_KIND  = T1.SCHOOL_KIND AND ";
            $query .= "                 E1.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
        }
        $query .= "                 E1.SUBCLASSCD   = T1.SUBCLASSCD AND ";
        $query .= "                 E1.VIEWCD       = T1.VIEWCD ";
        $query .= "             ) ";

        $db->query($query);
        Query::dbCheckIn($db);

        return;
    }

    //前年度コピー
    public function &getPreCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        list($grade, $semester)    = explode('-', $model->grade_semes);
        $pre_year = CTRL_YEAR - 1;

        $query  = " INSERT INTO JVIEWNAME_GRADE_SEMES_DETAIL_MST ";
        $query .= " SELECT ";
        $query .= "     '".CTRL_YEAR."', ";
        $query .= "     SEMESTER, ";
        $query .= "     GRADE, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "     SUBCLASSCD, ";
        $query .= "     VIEWCD, ";
        $query .= "     REMARK1, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_SEMES_DETAIL_MST T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$pre_year."' AND ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 JVIEWNAME_GRADE_MST E1 ";
        $query .= "             WHERE ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                 E1.CLASSCD      = T1.CLASSCD AND ";
            $query .= "                 E1.SCHOOL_KIND  = T1.SCHOOL_KIND AND ";
            $query .= "                 E1.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
        }
        $query .= "                 E1.SUBCLASSCD   = T1.SUBCLASSCD AND ";
        $query .= "                 E1.VIEWCD       = T1.VIEWCD ";
        $query .= "             ) ";

        $db->query($query);
        Query::dbCheckIn($db);

        return;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data = array();
        $data["PROGRAMID"][TEXT]    = PROGRAMID;
        $data["MSGROW"][NUMBER]     = $record_no;
        $data["MSGREMARK"][TEXT]    = $check_error;

        $query = Query::insertSQL($data, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //科目データ取得
    public function getSubclassData($model)
    {
        $query  = " SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-'|| SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE, ";
        } else {
            $query .= "     SUBCLASSCD AS VALUE, ";
        }
        $query .= "     SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //ＣＳＶ出力データ取得
    public function getCsvData($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.GRADE, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
        } else {
            $query .= "     T1.SUBCLASSCD, ";
        }
        $query .= "     T1.VIEWCD, ";
        $query .= "     T1.VIEWABBV, ";
        $query .= "     L1.SUBCLASSNAME, ";
        $query .= "     D1.SEMESTER, ";
        $query .= "     D1.REMARK1 ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_MST T1 ";
        $query .= "     LEFT JOIN SUBCLASS_MST L1 ";
        $query .= "              ON L1.SUBCLASSCD       = T1.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND L1.CLASSCD          = T1.CLASSCD ";
            $query .= "         AND L1.SCHOOL_KIND      = T1.SCHOOL_KIND ";
            $query .= "         AND L1.CURRICULUM_CD    = T1.CURRICULUM_CD ";
        }
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_SEMES_DETAIL_MST D1 ";
        $query .= "              ON D1.YEAR             = '".CTRL_YEAR."' ";
        $query .= "             AND D1.GRADE            = T1.GRADE ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND D1.CLASSCD          = T1.CLASSCD ";
            $query .= "         AND D1.SCHOOL_KIND      = T1.SCHOOL_KIND ";
            $query .= "         AND D1.CURRICULUM_CD    = T1.CURRICULUM_CD ";
        }
        $query .= "             AND D1.SUBCLASSCD       = T1.SUBCLASSCD ";
        $query .= "             AND D1.VIEWCD           = T1.VIEWCD ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " WHERE ";
                $query .= "     T1.GRADE IN (SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
                $query .= "                     SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."')) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " WHERE ";
            $query .= "     T1.GRADE IN (SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT ";
            $query .= "                 WHERE ";
            $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     D1.SEMESTER, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.VIEWCD ";

        return $query;
    }

    //学年チェック
    public function checkGrade($model, $data)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     GRADE = '".$data["GRADE"]."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //学期チェック
    public function checkSemester($model, $data)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' AND ";
        $query .= "     SEMESTER = '".$data["SEMESTER"]."' ";

        return $query;
    }

    //科目チェック
    public function checkSubclasscd($model, $data)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST ";
        $query .= " WHERE ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$data["SUBCLASSCD"]."' ";
        } else {
            $query .= "     SUBCLASSCD = '".$data["SUBCLASSCD"]."' ";
        }

        return $query;
    }

    //観点チェック
    public function checkViewcd($model, $data, $viewcd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_MST ";
        $query .= " WHERE ";
        $query .= "     GRADE = '".$data["GRADE"]."' AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$data["SUBCLASSCD"]."' AND ";
        } else {
            $query .= "     SUBCLASSCD = '".$data["SUBCLASSCD"]."' AND ";
        }
        $query .= "     VIEWCD = '".$viewcd."' ";

        return $query;
    }

    //CSVファイルよりDBへインサート
    public function insertQueryCsv($db, $model, $data_arr)
    {
        $cnt = 0;    //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            for ($j = 1; $j <= $data_arr[$i]["COL_NO"]; $j++) {
                if ($data_arr[$i]["VIEWCD".$j] == "") {
                    continue;
                }

                //データ有無チェック
                $check = $db->getOne(knjz401mQuery::checkDataExist($model, $data_arr[$i], $data_arr[$i]["VIEWCD".$j]));

                //データセット
                $data = array();
                if (1 > $check) {
                    $data["YEAR"][TEXT]             = CTRL_YEAR;
                    $data["SEMESTER"][TEXT]         = $data_arr[$i]["SEMESTER"];
                    $data["GRADE"][TEXT]            = $data_arr[$i]["GRADE"];
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        list($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode('-', $data_arr[$i]["SUBCLASSCD"]);
                        $data["CLASSCD"][TEXT]          = $classcd;
                        $data["SCHOOL_KIND"][TEXT]      = $school_kind;
                        $data["CURRICULUM_CD"][TEXT]    = $curriculum_cd;
                        $data["SUBCLASSCD"][TEXT]       = $subclasscd;
                    } else {
                        $data["SUBCLASSCD"][TEXT]       = $data_arr[$i]["SUBCLASSCD"];
                    }
                    $data["VIEWCD"][TEXT]           = $data_arr[$i]["VIEWCD".$j];
                }
                $data["REMARK1"][TEXT]          = $data_arr[$i]["REMARK1".$j];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";

                //データが一件もなければ、insertする
                if (1 > $check) {
                    $query = Query::insertSQL($data, "JVIEWNAME_GRADE_SEMES_DETAIL_MST");
                } else {
                    $where  = " WHERE ";
                    $where .= "     YEAR            = '".CTRL_YEAR."' AND ";
                    $where .= "     SEMESTER        = '".$data_arr[$i]["SEMESTER"]."' AND ";
                    $where .= "     GRADE           = '".$data_arr[$i]["GRADE"]."' AND ";
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $where .= "     CLASSCD         = '".$classcd."' AND ";
                        $where .= "     SCHOOL_KIND     = '".$school_kind."' AND ";
                        $where .= "     CURRICULUM_CD   = '".$curriculum_cd."' AND ";
                        $where .= "     SUBCLASSCD      = '".$subclasscd."' AND ";
                    } else {
                        $where .= "     SUBCLASSCD      = '".$data_arr[$i]["SUBCLASSCD"]."' AND ";
                    }
                    $where .= "     VIEWCD          = '".$data_arr[$i]["VIEWCD".$j]."' ";

                    $query = Query::updateSQL($data, "JVIEWNAME_GRADE_SEMES_DETAIL_MST", $where);
                }
                $db->query($query);
                $cnt++;
            }
        }
        return $cnt;
    }

    //データ有無チェック
    public function checkDataExist($model, $data_arr, $viewcd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_SEMES_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".$data_arr["SEMESTER"]."' AND ";
        $query .= "     GRADE       = '".$data_arr["GRADE"]."' AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$data_arr["SUBCLASSCD"]."' AND ";
        } else {
            $query .= "     SUBCLASSCD  = '".$data_arr["SUBCLASSCD"]."' AND ";
        }
        $query .= "     VIEWCD      = '".$viewcd."' ";

        return $query;
    }
}
