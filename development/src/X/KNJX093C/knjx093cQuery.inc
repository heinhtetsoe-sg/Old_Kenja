<?php

require_once('for_php7.php');
class knjx093cquery extends Query
{
    //年度一覧
    public function getSelectFieldSQL($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR || '年度 ' || T2.SEMESTERNAME AS LABEL, ";
        $query .= "     T1.YEAR || T1.SEMESTER AS VALUE ";
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
    public function getGrade($model)
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

    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //データセット
            $data["YEAR"][TEXT]          = $data_arr[$i]["YEAR"];
            $data["SEMESTER"][TEXT]      = $data_arr[$i]["SEMESTER"];
            $data["RIREKI_CODE"][TEXT]   = $data_arr[$i]["RIREKI_CODE"];
            $data["GROUPCD"][TEXT]       = $data_arr[$i]["GROUPCD"];
            $data["CLASSCD"][TEXT]       = $data_arr[$i]["CLASSCD"];
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["SCHOOL_KIND"][TEXT]   = $data_arr[$i]["SCHOOL_KIND"];
            }
            $data["CURRICULUM_CD"][TEXT] = $data_arr[$i]["CURRICULUM_CD"];
            $data["SUBCLASSCD"][TEXT]    = $data_arr[$i]["SUBCLASSCD"];
            $data["SCHREGNO"][TEXT]      = $data_arr[$i]["SCHREGNO"];
            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][NUMBER]     = "SYSDATE()";

            $query = Query::insertSQL($data, "SUBCLASS_STD_SELECT_RIREKI_DAT");
            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //削除（ＣＳＶデータより読込）
    public function deleteQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $query  = " DELETE FROM ";
            $query .= "     SUBCLASS_STD_SELECT_RIREKI_DAT ";
            $query .= " WHERE ";
            $query .= "         YEAR          = '{$data_arr[$i]["YEAR"]}' ";
            $query .= "     AND SEMESTER      = '{$data_arr[$i]["SEMESTER"]}' ";
            $query .= "     AND RIREKI_CODE   = '{$data_arr[$i]["RIREKI_CODE"]}' ";
            $query .= "     AND GROUPCD       = '{$data_arr[$i]["GROUPCD"]}' ";
            $query .= "     AND CLASSCD       = '{$data_arr[$i]["CLASSCD"]}' ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     AND SCHOOL_KIND   = '{$data_arr[$i]["SCHOOL_KIND"]}' ";
            }
            $query .= "     AND CURRICULUM_CD = '{$data_arr[$i]["CURRICULUM_CD"]}' ";
            $query .= "     AND SUBCLASSCD    = '{$data_arr[$i]["SUBCLASSCD"]}' ";
            $query .= "     AND SCHREGNO      = '{$data_arr[$i]["SCHREGNO"]}' ";

            $db->query($query);
            $cnt++;
        }

        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT]       = PROGRAMID;
        $data1["MSGROW"][NUMBER]        = $record_no;
        $data1["MSGREMARK"][TEXT]       = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");

        $result = $db->query($query);
    }

    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.RIREKI_CODE, ";
        $query .= "     T1.GROUPCD, ";
        $query .= "     T1.CLASSCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.SCHOOL_KIND, ";
        }
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     L3.SUBCLASSNAME, ";
        $query .= "     L1.GRADE, ";
        $query .= "     L1.HR_CLASS, ";
        $query .= "     L1.ATTENDNO, ";
        $query .= "     L2.NAME, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     '". $model->lastColumn."' ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_STD_SELECT_RIREKI_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_DAT L1 ON  L1.YEAR = T1.YEAR ";
        $query .= "                        AND L1.SEMESTER = T1.SEMESTER ";
        $query .= "                        AND L1.SCHREGNO = T1.SCHREGNO ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON L1.YEAR = REG_G.YEAR ";
                $query .= "           AND L1.GRADE = REG_G.GRADE ";
                $query .= "           AND REG_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON L1.YEAR = REG_G.YEAR ";
            $query .= "           AND L1.GRADE = REG_G.GRADE ";
            $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_BASE_MST L2 ON L2.SCHREGNO = T1.SCHREGNO ";
        $query .= " LEFT JOIN ";
        $query .= "     SUBCLASS_MST L3 ON L3.SUBCLASSCD        = T1.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                        AND L3.CLASSCD       = T1.CLASSCD ";
            $query .= "                        AND L3.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "                        AND L3.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "         T1.YEAR         = '{$model->field["YEAR"]}' ";
        $query .= "     AND T1.SEMESTER     = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.RIREKI_CODE  = '{$model->field["RIREKI_CODE"]}' ";
        if ($model->field["GRADE"] != '99') {
            $query .= " AND L1.GRADE        = '{$model->field["GRADE"]}' ";
        }
        if ($model->Properties["useCurriculumcd"] == '1') {
            if ($model->field["SUBCLASS"] != '99-X-9-999999') {
                $query .= " AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '{$model->field["SUBCLASS"]}' ";
            }
        } else {
            if ($model->field["SUBCLASS"] != '999999') {
                $query .= " AND T1.SUBCLASSCD = '{$model->field["SUBCLASS"]}' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.GROUPCD, ";
        $query .= "     T1.CLASSCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.SCHOOL_KIND, ";
        }
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     L1.GRADE, ";
        $query .= "     L1.HR_CLASS, ";
        $query .= "     L1.ATTENDNO ";

        return $query;
    }

    //学校種別取得
    public function getSchoolKind()
    {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     ABBV1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        $query .= " ORDER BY ";
        $query .= "     NAME1 ASC ";

        return $query;
    }

    //教育課程取得
    public function getCurriculumCd()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z018' ";

        return $query;
    }

    //そのデータが存在するか(教科コード)
    public function classcdCntSql($model, $data, $classcd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CLASS_MST ";
        $query .= " WHERE ";
        $query .= "     CLASSCD = '{$classcd}' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND SCHOOL_KIND     = '{$data["SCHOOL_KIND"]}' ";
        }

        return $query;
    }

    //そのデータが存在するか(科目コード)
    public function subclassCntSql($model, $data, $subclassCd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     SUBCLASSCD = '{$subclassCd}' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND CLASSCD         = '{$data["CLASSCD"]}' ";
            $query .= "     AND SCHOOL_KIND     = '{$data["SCHOOL_KIND"]}' ";
            $query .= "     AND CURRICULUM_CD   = '{$data["CURRICULUM_CD"]}' ";
        }

        return $query;
    }

    //そのデータが存在するか(学籍番号)
    public function schregNoCntSql($year, $semester, $schregno)
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

    //そのデータが存在するか(選択グループコード)
    public function groupcdCntSql($year, $grade, $groupcd, $classcd, $subclasscd, $school_kind = "")
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_COMP_SELECT_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '{$year}' ";
        $query .= "     AND GRADE       = '{$grade}' ";
        $query .= "     AND GROUPCD     = '{$groupcd}' ";
        $query .= "     AND CLASSCD     = '{$classcd}' ";
        $query .= "     AND SUBCLASSCD  = '{$subclasscd}' ";
        if ($school_kind) {
            $query .= "     AND SCHOOL_KIND  = '{$school_kind}' ";
        }

        return $query;
    }
}
