<?php

require_once('for_php7.php');
class knjx091aquery extends Query
{
    //年度一覧
    public function getSelectFieldSQL()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR || '年度 ' || T2.SEMESTERNAME AS LABEL, ";
        $query .= "     T1.YEAR || '-' || T1.SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " INNER JOIN ";
        $query .= "     SEMESTER_MST T2 ON  T1.YEAR     = T2.YEAR ";
        $query .= "                     AND T1.SEMESTER = T2.SEMESTER ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

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
        $query .= "     SUBCLASS_STD_SELECT_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SUBCLASS_MST L1 ON L1.SUBCLASSCD = T1.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                    AND L1.CLASSCD       = T1.CLASSCD ";
            $query .= "                    AND L1.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "                    AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '{$model->field["YEAR"]}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //データセット
            $data = array();
            $data["YEAR"][TEXT]          = $data_arr[$i]["YEAR"];
            $data["SEMESTER"][TEXT]      = $data_arr[$i]["SEMESTER"];
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

            $query = Query::insertSQL($data, "SUBCLASS_STD_SELECT_DAT");
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
            $query .= "     SUBCLASS_STD_SELECT_DAT ";
            $query .= " WHERE ";
            $query .= "         YEAR          = '{$data_arr[$i]["YEAR"]}' ";
            $query .= "     AND SEMESTER      = '{$data_arr[$i]["SEMESTER"]}' ";
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

    //GROUPCDが600以上で履修生徒がいないとき削除
    public function getDelete600Query($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $year = array();
        for ($i = 0; $i < get_count($data_arr); $i++) {
            if (!in_array($data_arr[$i]["YEAR"], $year)) {
                $year[] = $data_arr[$i]["YEAR"];
            }
        }

        //SUBCLASS_COMP_SELECT_DAT削除
        $query = knjx091aQuery::getSelectDatOver600($model, $year);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $query  = " DELETE FROM ";
            $query .= "     SUBCLASS_COMP_SELECT_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR            = '". $row["YEAR"] ."' AND ";
            $query .= "     GRADE           = '". $row["GRADE"] ."' AND ";
            $query .= "     COURSECD        = '". $row["COURSECD"] ."' AND ";
            $query .= "     MAJORCD         = '". $row["MAJORCD"] ."' AND ";
            $query .= "     COURSECODE      = '". $row["COURSECODE"] ."' AND ";
            $query .= "     GROUPCD         = '". $row["GROUPCD"] ."' AND ";
            $query .= "     CLASSCD         = '". $row["CLASSCD"] ."' AND ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     SCHOOL_KIND     = '". $row["SCHOOL_KIND"] ."' AND ";
                $query .= "     CURRICULUM_CD   = '". $row["CURRICULUM_CD"] ."' AND ";
            }
            $query .= "     SUBCLASSCD      = '". $row["SUBCLASSCD"] ."' ";

            $db->query($query);
        }

        //SUBCLASS_COMP_SELECT_MST削除
        $query = knjx091aQuery::getSelectMstOver600($year);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $query  = " DELETE FROM ";
            $query .= "     SUBCLASS_COMP_SELECT_MST ";
            $query .= " WHERE ";
            $query .= "     YEAR            = '". $row["YEAR"] ."' AND ";
            $query .= "     GRADE           = '". $row["GRADE"] ."' AND ";
            $query .= "     COURSECD        = '". $row["COURSECD"] ."' AND ";
            $query .= "     MAJORCD         = '". $row["MAJORCD"] ."' AND ";
            $query .= "     COURSECODE      = '". $row["COURSECODE"] ."' AND ";
            $query .= "     GROUPCD         = '". $row["GROUPCD"] ."' ";

            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    //SUBCLASS_COMP_SELECT_DAT取得（GROUPCDが600以上）
    public function getSelectDatOver600($model, $year)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_COMP_SELECT_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR IN ('".implode("','", $year)."') AND ";
        $query .= "     GROUPCD >= '600' AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     SUBCLASS_STD_SELECT_DAT T2 ";
        $query .= "                 WHERE ";
        $query .= "                     T1.YEAR          = T2.YEAR AND ";
        $query .= "                     T1.GROUPCD       = T2.GROUPCD AND ";
        $query .= "                     T1.CLASSCD       = T2.CLASSCD AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     T1.SCHOOL_KIND   = T2.SCHOOL_KIND AND ";
        }
        $query .= "                     T1.CURRICULUM_CD = T2.CURRICULUM_CD AND ";
        $query .= "                     T1.SUBCLASSCD    = T2.SUBCLASSCD ";
        $query .= "                 ) ";

        return $query;
    }

    //SUBCLASS_COMP_SELECT_MST取得（GROUPCDが600以上）
    public function getSelectMstOver600($year)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_COMP_SELECT_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR IN ('".implode("','", $year)."') AND ";
        $query .= "     T1.GROUPCD >= '600' AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     SUBCLASS_COMP_SELECT_DAT T2 ";
        $query .= "                 WHERE ";
        $query .= "                     T1.YEAR         = T2.YEAR AND ";
        $query .= "                     T1.GRADE        = T2.GRADE AND ";
        $query .= "                     T1.COURSECD     = T2.COURSECD AND ";
        $query .= "                     T1.MAJORCD      = T2.MAJORCD AND ";
        $query .= "                     T1.COURSECODE   = T2.COURSECODE AND ";
        $query .= "                     T1.GROUPCD      = T2.GROUPCD ";
        $query .= "                 ) ";

        return $query;
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
        $query .= "     L1.COURSECD, ";
        $query .= "     L1.MAJORCD, ";
        $query .= "     L1.COURSECODE, ";
        $query .= "     L1.GRADE, ";
        $query .= "     L1.HR_CLASS, ";
        $query .= "     L1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L2.NAME, ";
        $query .= "     T1.CLASSCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.SCHOOL_KIND, ";
        }
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     L3.SUBCLASSNAME, ";
        $query .= "     T1.GROUPCD, ";
        $query .= "     '".$model->lastColumn."' ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_STD_SELECT_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_DAT L1 ON  L1.YEAR = T1.YEAR ";
        $query .= "                        AND L1.SEMESTER = T1.SEMESTER ";
        $query .= "                        AND L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_BASE_MST L2 ON L2.SCHREGNO = T1.SCHREGNO ";
        $query .= " LEFT JOIN ";
        $query .= "     SUBCLASS_MST L3 ON L3.SUBCLASSCD = T1.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND L3.CLASSCD = T1.CLASSCD ";
            $query .= "     AND L3.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "     AND L3.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "         T1.YEAR       = '{$model->field["YEAR"]}' ";
        $query .= "     AND T1.SEMESTER   = '{$model->field["SEMESTER"]}' ";
        if ($model->field["GRADE"] != '99') {
            $query .= " AND L1.GRADE      = '{$model->field["GRADE"]}' ";
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
        $query .= "     L1.COURSECD, ";
        $query .= "     L1.MAJORCD, ";
        $query .= "     L1.COURSECODE, ";
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

    //そのデータが存在するか(選択グループコード)
    public function groupcdCntSql($year, $grade, $courseCd, $majorCd, $courseCode, $groupcd, $classcd, $subclasscd, $school_kind = "")
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_COMP_SELECT_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '{$year}' ";
        $query .= "     AND GRADE       = '{$grade}' ";
        $query .= "     AND COURSECD    = '{$courseCd}' ";
        $query .= "     AND MAJORCD     = '{$majorCd}' ";
        $query .= "     AND COURSECODE  = '{$courseCode}' ";
        $query .= "     AND GROUPCD     = '{$groupcd}' ";
        $query .= "     AND CLASSCD     = '{$classcd}' ";
        $query .= "     AND SUBCLASSCD  = '{$subclasscd}' ";
        if ($school_kind) {
            $query .= "     AND SCHOOL_KIND  = '{$school_kind}' ";
        }

        return $query;
    }
}
