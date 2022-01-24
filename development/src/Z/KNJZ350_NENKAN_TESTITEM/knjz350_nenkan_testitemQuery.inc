<?php

require_once('for_php7.php');

class knjz350_nenkan_testitemQuery extends Query {

    //左画面の校種
    function getNameMst($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     NAME1 || '：' || ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND NAME1 = '{$model->sendSchoolKind}' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }

    //リスト
    function getList($model, $tablename="") {
        $query  = " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.TESTKINDCD, ";
        $query .= "     T1.TESTITEMCD, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SET_SUBCLASSCD, ";
        $query .= "     T1.COURSECD || ':' || T1.MAJORCD || ':' || T1.COURSECODE AS COURSE_MAJOR_COURSECODE, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ':' || L0.SUBCLASSNAME AS SET_SUBCLASSNAME, ";
        if ($tablename) {
            $query .= "     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || ':'  || L3.TESTITEMNAME AS SET_TESTITEMNAME, ";
        }
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     '(' || T1.COURSECD || T1.MAJORCD || ')' || L1.COURSENAME || L1.MAJORNAME || ' ' || '(' || T1.COURSECODE || ')' || L2.COURSECODENAME AS SET_COURSE_NAME, ";
        $query .= "     L1.COURSENAME || L1.MAJORNAME || L2.COURSECODENAME AS COURSE_NAME, ";
        $query .= "     L4.GRADE_NAME1, ";
        $query .= "     T1.YEAR || T1.GRADE || T1.COURSECD || T1.MAJORCD || T1.COURSECODE || T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS KEY ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_TESTITEM_DAT T1 ";
        $query .= "     LEFT JOIN V_SUBCLASS_MST L0 ON T1.YEAR = L0.YEAR ";
        $query .= "                                AND T1.CLASSCD = L0.CLASSCD ";
        $query .= "                                AND T1.SCHOOL_KIND = L0.SCHOOL_KIND ";
        $query .= "                                AND T1.CURRICULUM_CD = L0.CURRICULUM_CD ";
        $query .= "                                AND T1.SUBCLASSCD = L0.SUBCLASSCD ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.COURSECD || T1.MAJORCD = L1.COURSECD || L1.MAJORCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST L2 ON T1.YEAR = L2.YEAR ";
        $query .= "          AND T1.COURSECODE = L2.COURSECODE ";
        if ($tablename) {
            $query .= "     LEFT JOIN $tablename L3 ON T1.YEAR = L3.YEAR ";
            $query .= "                            AND T1.SEMESTER = L3.SEMESTER ";
            $query .= "                            AND T1.TESTKINDCD = L3.TESTKINDCD ";
            $query .= "                            AND T1.TESTITEMCD = L3.TESTITEMCD ";
            if ($tablename == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
                $query .= "                        AND L3.SCORE_DIV = '01' ";
            }
        }
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT L4 ON T1.YEAR = L4.YEAR ";
        $query .= "                                  AND T1.GRADE = L4.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($model->setSchoolKind) {
           $query .= " AND T1.SCHOOL_KIND = '".$model->setSchoolKind."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     KEY ";
        if ($tablename) {
            $query .= "     ,SET_TESTITEMNAME ";
        }

        return $query;
    }

    function getListCount($model, $row) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_TESTITEM_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND GRADE = '{$row["GRADE"]}' ";
        $query .= " AND COURSECD || ':' || MAJORCD || ':' || COURSECODE = '{$row["COURSE_MAJOR_COURSECODE"]}' ";
        $query .= " AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD =  '{$row["SET_SUBCLASSCD"]}' ";

        return $query;
    }

    //１レコード取得
    function getSelectData($field) {
        $query  = " SELECT ";
        $query .= "     GRADE, ";
        $query .= "     SEMESTER, ";
        $query .= "     COURSECD || ':' || MAJORCD || ':' || COURSECODE AS COURSE_MAJOR_COURSECODE, ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     TESTKINDCD, ";
        $query .= "     TESTITEMCD ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_TESTITEM_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR       = '".CTRL_YEAR."' ";
        //$query .= "     AND SEMESTER   = '".$field["SEMESTER"]."' ";
        $query .= "     AND GRADE      = '".$field["GRADE"]."' ";
        $query .= "     AND COURSECD || ':' || MAJORCD || ':' || COURSECODE = '".$field["COURSE_MAJOR_COURSECODE"]."' ";
        $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$field["SUBCLASSCD"]."' ";

        return $query;
    }

    //学年
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "       SCHREG_REGD_GDAT" ;
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->setSchoolKind) {
            $query .= " AND SCHOOL_KIND = '".$model->setSchoolKind."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     GRADE ";

        return $query;
    }

    //校種
    function getSchoolKind($model, $grade) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "       SCHREG_REGD_GDAT" ;
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '".$grade."' ";

        return $query;
    }

    //課程
    function getCourse($grade) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.COURSECD || ':' || T1.MAJORCD || ':' || T1.COURSECODE AS VALUE, ";
        $query .= "     '(' || T1.COURSECD || T1.MAJORCD || ')' || L1.COURSENAME || L1.MAJORNAME || ' ' || '(' || T1.COURSECODE || ')' || L2.COURSECODENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.COURSECD = L1.COURSECD ";
        $query .= "          AND T1.MAJORCD = L1.MAJORCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST L2 ON T1.YEAR = L2.YEAR ";
        $query .= "          AND T1.COURSECODE = L2.COURSECODE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND T1.GRADE = '".$grade."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目
    function getSubclass($model) {
        if ($model->selectSchoolKind) {
            if ($model->selectSchoolKind !== 'P') {
                if ($model->selectSchoolKind === 'J') {
                    $setName = '基本設定(中学)';
                } else {
                    $setName = '基本設定(高校)';
                }
                $query  = " SELECT ";
                $query .= "     '00' || '-' || '{$model->selectSchoolKind}' || '-' || '00' || '-' || '000000' AS VALUE, ";
                $query .= "     '00' || '-' || '{$model->selectSchoolKind}' || '-' || '00' || '-' || '000000' || ':' || '{$setName}' AS LABEL ";
                $query .= " FROM ";
                $query .= "     SYSIBM.SYSDUMMY1 ";
                $query .= " UNION ";
            }
        } else {
            $query  = "";
        }
        $query .= " SELECT ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE, ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || ':' || SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND SCHOOL_KIND = '".$model->selectSchoolKind."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //課程一覧(右側)
    function getTestItem($model, $tableName, $flg="") {
        $query  = " SELECT ";
        $query .= "     SEMESTER || TESTKINDCD || TESTITEMCD || ':' || TESTITEMNAME AS LABEL, ";
        $query .= "     SEMESTER || ':' || TESTKINDCD || ':' || TESTITEMCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     $tableName ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        //$query .= " AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " AND TESTKINDCD IN ('01', '02') ";
        if ($tableName == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
            $query .= " AND SCORE_DIV = '01' ";
        }
        if ($flg) {
            $query .= " AND SEMESTER || TESTKINDCD || TESTITEMCD IN ( ";
        } else {
            $query .= " AND SEMESTER || TESTKINDCD || TESTITEMCD NOT IN ( ";
        }
        $query .= "         SELECT ";
        $query .= "             SEMESTER || TESTKINDCD || TESTITEMCD ";
        $query .= "         FROM ";
        $query .= "             ADMIN_CONTROL_TESTITEM_DAT ";
        $query .= "         WHERE ";
        $query .= "             YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND GRADE = '{$model->sendField["GRADE"]}' ";
        $query .= "         AND COURSECD || ':' || MAJORCD || ':' || COURSECODE = '{$model->sendField["COURSE_MAJOR_COURSECODE"]}' ";
        $query .= "         AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->sendField["SUBCLASSCD"]}' ";
        //$query .= "         AND SEMESTER = '{$model->sendField["SEMESTER"]}' ";
        $query .= " ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /********↓前年度からのコピー↓********/
    //前年度からのコピーの件数カウント
    function getCopyCountQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_TESTITEM_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '".(CTRL_YEAR-1)."' ";
        if ($model->setSchoolKind) {
            $query .= "     AND SCHOOL_KIND = '".$model->setSchoolKind."' ";
        }

        $cnt = $db->getOne($query);
        Query::dbCheckIn($db);
        return $cnt;
    }

    //今年度の件数カウント
    function getCopyCount2($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_TESTITEM_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '".CTRL_YEAR."' ";
        if ($model->setSchoolKind) {
            $query .= "     AND SCHOOL_KIND = '".$model->setSchoolKind."' ";
        }

        $cnt2 = $db->getOne($query);
        Query::dbCheckIn($db);
        return $cnt2;
    }

    //今年度の登録済み考査種別の件数カウント
    function getCopyCountTsetitem($model, $tablename) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ".$tablename." ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND TESTKINDCD IN ('01', '02') ";
        if ($tablename == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
            $query .= "     AND SCORE_DIV = '01' ";
        }

        $cntTest = $db->getOne($query);
        Query::dbCheckIn($db);
        return $cntTest;
    }

    //今年度のADMIN_CONTROL_TESTITEM_DATデータを消去
    function &getCopyDelTestQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     ADMIN_CONTROL_TESTITEM_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '".CTRL_YEAR."' ";
        if ($model->setSchoolKind) {
            $query .= "     AND SCHOOL_KIND = '".$model->setSchoolKind."' ";
        }

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //前年度からのコピー(ADMIN_CONTROL_TESTITEM_DAT)
    function &getCopyTestQuery($model, $tablename) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO ADMIN_CONTROL_TESTITEM_DAT ";
        $query .= " SELECT ";
        $query .= "     '".CTRL_YEAR."' AS YEAR, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.TESTKINDCD, ";
        $query .= "     T1.TESTITEMCD, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_TESTITEM_DAT T1 ";
        if ($tablename) {
            $query .= "     INNER JOIN $tablename L3 ON L3.YEAR       = '".CTRL_YEAR."' ";
            $query .= "                             AND T1.SEMESTER   = L3.SEMESTER ";
            $query .= "                             AND T1.TESTKINDCD = L3.TESTKINDCD ";
            $query .= "                             AND T1.TESTITEMCD = L3.TESTITEMCD ";
            $query .= "                             AND L3.TESTKINDCD IN ('01', '02') ";
            if ($tablename == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
                $query .= "                         AND L3.SCORE_DIV = '01' ";
            }
        }
        $query .= " WHERE ";
        $query .= "         T1.YEAR        = '".(CTRL_YEAR-1)."' ";
        $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE IN ( ";
        $query .= "                            SELECT DISTINCT ";
        $query .= "                                COURSECD || MAJORCD || COURSECODE ";
        $query .= "                            FROM ";
        $query .= "                                SCHREG_REGD_DAT ";
        $query .= "                            WHERE ";
        $query .= "                                YEAR = '".CTRL_YEAR."' ";
        $query .= "                           ) ";
        if ($model->setSchoolKind) {
            $query .= "     AND T1.SCHOOL_KIND = '".$model->setSchoolKind."' ";
        }

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return true;
   }

    //コピー件数カウント
    function &getCntKekka($model, $tablename) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_TESTITEM_DAT T1 ";
        if ($tablename) {
            $query .= "     INNER JOIN $tablename L3 ON T1.YEAR       = L3.YEAR ";
            $query .= "                             AND T1.SEMESTER   = L3.SEMESTER ";
            $query .= "                             AND T1.TESTKINDCD = L3.TESTKINDCD ";
            $query .= "                             AND T1.TESTITEMCD = L3.TESTITEMCD ";
            $query .= "                             AND L3.TESTKINDCD IN ('01', '02') ";
            if ($tablename == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
                $query .= "                         AND L3.SCORE_DIV = '01' ";
            }
        }
        $query .= " WHERE ";
        $query .= "         T1.YEAR        = '".CTRL_YEAR."' ";
        if ($model->setSchoolKind) {
            $query .= "     AND T1.SCHOOL_KIND = '".$model->setSchoolKind."' ";
        }

        $cntKekka = $db->getOne($query);
        Query::dbCheckIn($db);
        return $cntKekka;
   }
    /**********↑↑↑↑↑↑↑↑↑**********/

    //--- INSERT
    function &getInsertQuery($fields) {
        list($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode('-', $fields["SUBCLASSCD"]);
        list($coursecd, $majorcd, $coursecode) = explode(':', $fields["COURSE_MAJOR_COURSECODE"]);
        
        $db = Query::dbCheckOut();
        $data = array();
        $data["YEAR"][TEXT]         = CTRL_YEAR;
        $data["GRADE"][TEXT]        = $fields["GRADE"];

        $data["COURSECD"][TEXT]     = $coursecd;
        $data["MAJORCD"][TEXT]      = $majorcd;
        $data["COURSECODE"][TEXT]   = $coursecode;
        
        $data["CLASSCD"][TEXT]      = $classcd;
        $data["SCHOOL_KIND"][TEXT]  = $school_kind;
        $data["CURRICULUM_CD"][TEXT]= $curriculum_cd;
        $data["SUBCLASSCD"][TEXT]   = $subclasscd;

        $data["SEMESTER"][TEXT]     = $fields["SEMESTER"];
        $data["TESTKINDCD"][TEXT]   = $fields["TESTKINDCD"];
        $data["TESTITEMCD"][TEXT]   = $fields["TESTITEMCD"];

        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $query = Query::insertSQL($data, "ADMIN_CONTROL_TESTITEM_DAT");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields, $flg="") {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     ADMIN_CONTROL_TESTITEM_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR           = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE      = '".$fields["GRADE"]."' ";
        $query .= "     AND COURSECD || ':' || MAJORCD || ':' || COURSECODE = '".$fields["COURSE_MAJOR_COURSECODE"]."' ";
        $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$fields["SUBCLASSCD"]}' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }
}
?>
