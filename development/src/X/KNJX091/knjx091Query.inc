<?php

require_once('for_php7.php');
class knjx091query extends Query
{
    //年度・学年取得（複数科目／1行）
    public function getYearGrade()
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
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR DESC, ";
        $query .= "     T1.GRADE ";

        return $query;
    }

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
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //データセット
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
        $query = knjx091Query::getSelectDatOver600($model, $year);

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
        $query = knjx091Query::getSelectMstOver600($year);
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
        $query .= "     C1.COURSENAME, ";
        $query .= "     L1.MAJORCD, ";
        $query .= "     M1.MAJORNAME, ";
        $query .= "     L1.COURSECODE, ";
        $query .= "     L1.GRADE, ";
        $query .= "     L1.HR_CLASS, ";
        $query .= "     H1.HR_NAME, ";
        $query .= "     L1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L2.NAME, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     C2.CLASSNAME, ";
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
        $query .= "     COURSE_MST C1 ON C1.COURSECD = L1.COURSECD ";
        $query .= " LEFT JOIN ";
        $query .= "     MAJOR_MST M1 ON M1.COURSECD = L1.COURSECD ";
        $query .= "                 AND M1.MAJORCD  = L1.MAJORCD ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_HDAT H1  ON H1.YEAR         = L1.YEAR ";
        $query .= "                         AND H1.SEMESTER     = L1.SEMESTER ";
        $query .= "                         AND H1.GRADE        = L1.GRADE ";
        $query .= "                         AND H1.HR_CLASS     = L1.HR_CLASS ";
        $query .= " LEFT JOIN ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASS_MST C2 ON C2.CLASSCD = SUBSTR(T1.SUBCLASSCD, 1, 2) ";
            $query .= "                 AND C2.SCHOOL_KIND  = T1.SCHOOL_KIND ";
        } else {
            $query .= "     CLASS_MST C2 ON C2.CLASSCD = SUBSTR(T1.SUBCLASSCD, 1, 2) ";
        }
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

    //講座名簿取得（履修選択登録を除く）
    public function getChairStdDat($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T3.CLASSCD, ";
        $query .= "     T3.SCHOOL_KIND, ";
        $query .= "     T3.CURRICULUM_CD, ";
        $query .= "     T3.SUBCLASSCD, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     L1.NAME, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST L1 ON T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "     INNER JOIN CHAIR_STD_DAT T2  ON T1.YEAR = T2.YEAR ";
        $query .= "                                 AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "                                 AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     INNER JOIN CHAIR_DAT T3      ON T1.YEAR     = T3.YEAR ";
        $query .= "                                 AND T1.SEMESTER = T3.SEMESTER ";
        $query .= "                                 AND T2.CHAIRCD  = T3.CHAIRCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     SUBCLASS_STD_SELECT_DAT S1 ";
        $query .= "                 WHERE ";
        $query .= "                     S1.YEAR         = T1.YEAR AND ";
        $query .= "                     S1.SEMESTER     = T1.SEMESTER AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     S1.CLASSCD      = T3.CLASSCD AND ";
            $query .= "                     S1.SCHOOL_KIND  = T3.SCHOOL_KIND AND ";
            $query .= "                     S1.CURRICULUM_CD = T3.CURRICULUM_CD AND ";
        }
        $query .= "                     S1.SUBCLASSCD   = T3.SUBCLASSCD AND ";
        $query .= "                     S1.SCHREGNO     = T1.SCHREGNO ";
        $query .= "                 ) ";
        $query .= " ORDER BY ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD, ";
            $query .= "     T3.SCHOOL_KIND, ";
            $query .= "     T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.SUBCLASSCD, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //SUBCLASS_COMP_SELECT_DATからGROUPCD取得
    public function getSubclassCompSelectDat($model, $row, $flg)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     GROUPCD ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_COMP_SELECT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$row["YEAR"]."' AND ";
        $query .= "     GRADE       = '".$row["GRADE"]."' AND ";
        $query .= "     COURSECD    = '".$row["COURSECD"]."' AND ";
        $query .= "     MAJORCD     = '".$row["MAJORCD"]."' AND ";
        $query .= "     COURSECODE  = '".$row["COURSECODE"]."' AND ";
        $query .= "     CLASSCD     = '".$row["CLASSCD"]."' AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SCHOOL_KIND = '".$row["SCHOOL_KIND"]."' AND ";
            $query .= "     CURRICULUM_CD = '".$row["CURRICULUM_CD"]."' AND ";
        }
        $query .= "     SUBCLASSCD  = '".$row["SUBCLASSCD"]."' AND ";
        if ($flg == "under") {
            $query .= "     GROUPCD     < '600' ";
        } else {
            $query .= "     GROUPCD    >= '600' ";
        }

        return $query;
    }

    //SUBCLASS_COMP_SELECT_MSTから600以上のGROUPCDのMAX値を取得
    public function getSubclassCompSelectMst($row)
    {
        $query  = " SELECT ";
        $query .= "     MAX(GROUPCD) ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_COMP_SELECT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$row["YEAR"]."' AND ";
        $query .= "     GRADE       = '".$row["GRADE"]."' AND ";
        $query .= "     COURSECD    = '".$row["COURSECD"]."' AND ";
        $query .= "     MAJORCD     = '".$row["MAJORCD"]."' AND ";
        $query .= "     COURSECODE  = '".$row["COURSECODE"]."' AND ";
        $query .= "     GROUPCD    >= '600' ";

        return $query;
    }

    //SUBCLASS_STD_SELECT_DAT追加（講座名簿から）
    public function insertAutoSubclassStd($model, $row, $groupcd)
    {
        $auto_data = array();
        $auto_data["YEAR"][TEXT]            = $row["YEAR"];
        $auto_data["SEMESTER"][TEXT]        = $row["SEMESTER"];
        $auto_data["GROUPCD"][TEXT]         = $groupcd;
        $auto_data["CLASSCD"][TEXT]         = $row["CLASSCD"];
        if ($model->Properties["useCurriculumcd"] == '1') {
            $auto_data["SCHOOL_KIND"][TEXT]     = $row["SCHOOL_KIND"];
        }
        $auto_data["CURRICULUM_CD"][TEXT]   = $row["CURRICULUM_CD"];
        $auto_data["SUBCLASSCD"][TEXT]      = $row["SUBCLASSCD"];
        $auto_data["SCHREGNO"][TEXT]        = $row["SCHREGNO"];
        $auto_data["REGISTERCD"][TEXT]      = STAFFCD;
        $auto_data["UPDATED"][NUMBER]       = "sysdate()";

        $query = Query::insertSQL($auto_data, "SUBCLASS_STD_SELECT_DAT");
        return $query;
    }

    //SUBCLASS_COMP_SELECT_DAT追加（600以上のGROUPCDを新規追加）
    public function insertAutoSubclassCompSelectDat($model, $row, $groupcd)
    {
        $auto_data = array();
        $auto_data["YEAR"][TEXT]            = $row["YEAR"];
        $auto_data["GRADE"][TEXT]           = $row["GRADE"];
        $auto_data["COURSECD"][TEXT]        = $row["COURSECD"];
        $auto_data["MAJORCD"][TEXT]         = $row["MAJORCD"];
        $auto_data["COURSECODE"][TEXT]      = $row["COURSECODE"];
        $auto_data["GROUPCD"][TEXT]         = $groupcd;
        $auto_data["CLASSCD"][TEXT]         = $row["CLASSCD"];
        if ($model->Properties["useCurriculumcd"] == '1') {
            $auto_data["SCHOOL_KIND"][TEXT]     = $row["SCHOOL_KIND"];
            $auto_data["CURRICULUM_CD"][TEXT]   = $row["CURRICULUM_CD"];
        }
        $auto_data["SUBCLASSCD"][TEXT]      = $row["SUBCLASSCD"];
        $auto_data["REGISTERCD"][TEXT]      = STAFFCD;
        $auto_data["UPDATED"][NUMBER]       = "sysdate()";

        $query = Query::insertSQL($auto_data, "SUBCLASS_COMP_SELECT_DAT");
        return $query;
    }

    //SUBCLASS_COMP_SELECT_DAT追加（600以上のGROUPCDを新規追加）
    public function insertAutoSubclassCompSelectMst($row, $groupcd)
    {
        $auto_data = array();
        $auto_data["YEAR"][TEXT]            = $row["YEAR"];
        $auto_data["GRADE"][TEXT]           = $row["GRADE"];
        $auto_data["COURSECD"][TEXT]        = $row["COURSECD"];
        $auto_data["MAJORCD"][TEXT]         = $row["MAJORCD"];
        $auto_data["COURSECODE"][TEXT]      = $row["COURSECODE"];
        $auto_data["GROUPCD"][TEXT]         = $groupcd;
        $auto_data["NAME"][TEXT]            = $groupcd;
        $auto_data["ABBV"][TEXT]            = $groupcd;
        $auto_data["JOUGEN"][NUMBER]          = 1;
        $auto_data["KAGEN"][NUMBER]           = 1;
        $auto_data["REGISTERCD"][TEXT]      = STAFFCD;
        $auto_data["UPDATED"][NUMBER]       = "sysdate()";

        $query = Query::insertSQL($auto_data, "SUBCLASS_COMP_SELECT_MST");
        return $query;
    }

    //1科目のみの600未満の履修グループと同一科目の600以上の履修グループを取得
    public function getSameGroupcdList($model)
    {
        $query  = " WITH OVER600 AS ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_COMP_SELECT_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         GROUPCD >= '600' ";
        $query .= " ), SELECT_DAT AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.* ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_COMP_SELECT_DAT T1, ";
        $query .= "         (SELECT ";
        $query .= "             YEAR, ";
        $query .= "             GRADE, ";
        $query .= "             COURSECD, ";
        $query .= "             MAJORCD, ";
        $query .= "             COURSECODE, ";
        $query .= "             GROUPCD, ";
        $query .= "             COUNT(*) AS CNT ";
        $query .= "         FROM ";
        $query .= "             SUBCLASS_COMP_SELECT_DAT ";
        $query .= "         WHERE ";
        $query .= "             YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "             GROUPCD < '600' ";
        $query .= "         GROUP BY ";
        $query .= "             YEAR, ";
        $query .= "             GRADE, ";
        $query .= "             COURSECD, ";
        $query .= "             MAJORCD, ";
        $query .= "             COURSECODE, ";
        $query .= "             GROUPCD ";
        $query .= "         ) T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR         = T2.YEAR AND ";
        $query .= "         T1.GRADE        = T2.GRADE AND ";
        $query .= "         T1.COURSECD     = T2.COURSECD AND ";
        $query .= "         T1.MAJORCD      = T2.MAJORCD AND ";
        $query .= "         T1.COURSECODE   = T2.COURSECODE AND ";
        $query .= "         T1.GROUPCD      = T2.GROUPCD AND ";
        $query .= "         T2.CNT          = 1 ";
        $query .= " ), UNDER600 AS ( ";
        $query .= "     SELECT ";
        $query .= "     T1.* ";
        $query .= "     FROM ";
        $query .= "         SELECT_DAT T1 ";
        $query .= "     WHERE ";
        $query .= "         NOT EXISTS (SELECT ";
        $query .= "                         'X' ";
        $query .= "                     FROM ";
        $query .= "                         SELECT_DAT T2 ";
        $query .= "                     WHERE ";
        $query .= "                         T1.YEAR         = T2.YEAR AND ";
        $query .= "                         T1.GRADE        = T2.GRADE AND ";
        $query .= "                         T1.COURSECD     = T2.COURSECD AND ";
        $query .= "                         T1.MAJORCD      = T2.MAJORCD AND ";
        $query .= "                         T1.COURSECODE   = T2.COURSECODE AND ";
        $query .= "                         T1.GROUPCD     != T2.GROUPCD AND ";
        $query .= "                         T1.CLASSCD      = T2.CLASSCD AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                         T1.SCHOOL_KIND  = T2.SCHOOL_KIND AND ";
            $query .= "                         T1.CURRICULUM_CD = T2.CURRICULUM_CD AND ";
        }
        $query .= "                         T1.SUBCLASSCD   = T2.SUBCLASSCD ";
        $query .= "                     ) ";
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.GROUPCD AS O_GROUPCD, ";
        $query .= "     T2.GROUPCD AS U_GROUPCD ";
        $query .= " FROM ";
        $query .= "     OVER600 T1, ";
        $query .= "     UNDER600 T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = T2.YEAR AND ";
        $query .= "     T1.GRADE        = T2.GRADE AND ";
        $query .= "     T1.COURSECD     = T2.COURSECD AND ";
        $query .= "     T1.MAJORCD      = T2.MAJORCD AND ";
        $query .= "     T1.COURSECODE   = T2.COURSECODE AND ";
        $query .= "     T1.CLASSCD      = T2.CLASSCD AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.SCHOOL_KIND  = T2.SCHOOL_KIND AND ";
            $query .= "     T1.CURRICULUM_CD = T2.CURRICULUM_CD AND ";
        }
        $query .= "     T1.SUBCLASSCD   = T2.SUBCLASSCD ";

        return $query;
    }

    //対象履修グループを登録している生徒取得
    public function getOver600StdList($model, $row)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_STD_SELECT_DAT T1, ";
        $query .= "     SUBCLASS_COMP_SELECT_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = T2.YEAR AND ";
        $query .= "     T1.YEAR         = '".$row["YEAR"]."' AND ";
        $query .= "     T1.SEMESTER     = '".CTRL_SEMESTER."' AND ";
        $query .= "     T2.GRADE        = '".$row["GRADE"]."' AND ";
        $query .= "     T2.COURSECD     = '".$row["COURSECD"]."' AND ";
        $query .= "     T2.MAJORCD      = '".$row["MAJORCD"]."' AND ";
        $query .= "     T2.COURSECODE   = '".$row["COURSECODE"]."' AND ";
        $query .= "     T1.GROUPCD      = T2.GROUPCD AND ";
        $query .= "     T1.GROUPCD      = '".$row["O_GROUPCD"]."' AND ";
        $query .= "     T1.CLASSCD      = T2.CLASSCD AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.SCHOOL_KIND  = T2.SCHOOL_KIND AND ";
            $query .= "     T1.CURRICULUM_CD = T2.CURRICULUM_CD AND ";
        }
        $query .= "     T1.SUBCLASSCD   = T2.SUBCLASSCD ";

        return $query;
    }

    //SUBCLASS_STD_SELECT_DAT削除
    public function deleteOver600StdDat($model, $row)
    {
        $query  = " DELETE FROM";
        $query .= "     SUBCLASS_STD_SELECT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".$row["YEAR"]."' AND ";
        $query .= "     SEMESTER        = '".$row["SEMESTER"]."' AND ";
        $query .= "     GROUPCD         = '".$row["GROUPCD"]."' AND ";
        $query .= "     CLASSCD         = '".$row["CLASSCD"]."' AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SCHOOL_KIND     = '".$row["SCHOOL_KIND"]."' AND ";
        }
        $query .= "     CURRICULUM_CD   = '".$row["CURRICULUM_CD"]."' AND ";
        $query .= "     SUBCLASSCD      = '".$row["SUBCLASSCD"]."' AND ";
        $query .= "     SCHREGNO        = '".$row["SCHREGNO"]."' ";

        return $query;
    }

    //SUBCLASS_COMP_SELECT_DAT削除（GROUPCDが600以上）
    public function deleteOver600SelectDat($model, $row)
    {
        $query  = " DELETE FROM ";
        $query .= "     SUBCLASS_COMP_SELECT_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".$row["YEAR"]."' AND ";
        $query .= "     GRADE           = '".$row["GRADE"]."' AND ";
        $query .= "     COURSECD        = '".$row["COURSECD"]."' AND ";
        $query .= "     MAJORCD         = '".$row["MAJORCD"]."' AND ";
        $query .= "     COURSECODE      = '".$row["COURSECODE"]."' AND ";
        $query .= "     GROUPCD         = '".$row["O_GROUPCD"]."' AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     SUBCLASS_STD_SELECT_DAT S1, ";
        $query .= "                     SCHREG_REGD_DAT S2 ";
        $query .= "                 WHERE ";
        $query .= "                     T1.YEAR         = S1.YEAR AND ";
        $query .= "                     T1.YEAR         = S2.YEAR AND ";
        $query .= "                     T1.GRADE        = S2.GRADE AND ";
        $query .= "                     T1.COURSECD     = S2.COURSECD AND ";
        $query .= "                     T1.MAJORCD      = S2.MAJORCD AND ";
        $query .= "                     T1.COURSECODE   = S2.COURSECODE AND ";
        $query .= "                     T1.GROUPCD      = S1.GROUPCD AND ";
        $query .= "                     S1.SEMESTER     = S2.SEMESTER AND ";
        $query .= "                     S1.SCHREGNO     = S2.SCHREGNO ";
        $query .= "                 ) ";

        return $query;
    }

    //SUBCLASS_COMP_SELECT_MST削除（GROUPCDが600以上）
    public function deleteOver600SelectMst($model, $row)
    {
        $query  = " DELETE FROM";
        $query .= "     SUBCLASS_COMP_SELECT_MST T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".$row["YEAR"]."' AND ";
        $query .= "     GRADE           = '".$row["GRADE"]."' AND ";
        $query .= "     COURSECD        = '".$row["COURSECD"]."' AND ";
        $query .= "     MAJORCD         = '".$row["MAJORCD"]."' AND ";
        $query .= "     COURSECODE      = '".$row["COURSECODE"]."' AND ";
        $query .= "     GROUPCD         = '".$row["O_GROUPCD"]."' AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     SUBCLASS_COMP_SELECT_DAT S1 ";
        $query .= "                 WHERE ";
        $query .= "                     T1.YEAR         = S1.YEAR AND ";
        $query .= "                     T1.GRADE        = S1.GRADE AND ";
        $query .= "                     T1.COURSECD     = S1.COURSECD AND ";
        $query .= "                     T1.MAJORCD      = S1.MAJORCD AND ";
        $query .= "                     T1.COURSECODE   = S1.COURSECODE AND ";
        $query .= "                     T1.GROUPCD      = S1.GROUPCD  ";
        $query .= "                 ) ";

        return $query;
    }

    /*******************************/
    /* ＣＳＶ出力（複数科目／1行） */
    /*******************************/
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
        $query .= "     T1.COURSECD, ";
        $query .= "     C1.COURSENAME, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     M1.MAJORNAME, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     H1.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     LEFT JOIN COURSE_MST C1 ON T1.COURSECD      = C1.COURSECD ";
        $query .= "     LEFT JOIN MAJOR_MST M1 ON T1.COURSECD       = M1.COURSECD ";
        $query .= "                           AND T1.MAJORCD        = M1.MAJORCD ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT H1 ON T1.YEAR        = H1.YEAR ";
        $query .= "                                  AND T1.SEMESTER    = H1.SEMESTER ";
        $query .= "                                  AND T1.GRADE       = H1.GRADE ";
        $query .= "                                  AND T1.HR_CLASS    = H1.HR_CLASS ";
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

    //SUBCLASS_STD_SELECT_DAT取得
    public function getSubclassStdSelectDat($model, $schregno, $groupcd, $subclasscd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_STD_SELECT_DAT T1, ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".$model->field["YEAR"]."' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     T1.SCHREGNO = '".$schregno."' AND ";
        $query .= "     T2.COURSECD || '-' || T2.MAJORCD || '-' || T2.COURSECODE || '-' || T1.GROUPCD = '".$groupcd."' AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$subclasscd."' ";
        } else {
            $query .= "     T1.SUBCLASSCD = '".$subclasscd."' ";
        }

        return $query;
    }

    /***********************************/
    /* エラーチェック（複数科目／1行） */
    /***********************************/
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

    /*******************************/
    /* ＣＳＶ取込（複数科目／1行） */
    /*******************************/
    //更新処理
    public function insertQueryCsv2($model, $data_arrH, $data_arr)
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
                    $query = Query::insertSQL($data, "SUBCLASS_STD_SELECT_DAT");
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
    public function deleteQueryCsv2($model, $data_arrH, $data_arr)
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
                $query .= "     SUBCLASS_STD_SELECT_DAT ";
                $query .= " WHERE ";
                $query .= "     YEAR            = '{$data_arr[$i]["YEAR"]}' AND ";
                $query .= "     SEMESTER        = '{$data_arr[$i]["SEMESTER"]}' AND ";
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
