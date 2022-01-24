<?php

require_once('for_php7.php');

class knjz183Query extends Query {
    //学年取得
    function getGrade() {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学校校種を取得
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND GRADE = '".$model->grade."' ";
        
        return $query;
    }

    //リスト情報取得
    function getList($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.*, ";
        if ($model->schoolkind === 'H') {
            $query .= "     T2.GROUP_NAME, ";
        } else {
            $query .= "     T2.COURSENAME, ";
            $query .= "     T4.COURSECODENAME, ";
        }
        $query .= "     T3.SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_RATE_GRADE_DAT T1, ";
        if ($model->schoolkind === 'H') {
            $query .= "     COURSE_GROUP_CD_HDAT T2, ";
        } else {
            $query .= "     V_COURSE_MAJOR_MST T2, ";
            $query .= "     V_COURSECODE_MST T4, ";
        }
        $query .= "     V_SUBCLASS_MST T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = T3.YEAR AND ";
        $query .= "     T1.YEAR         = '".CTRL_YEAR."' AND ";
        if ($model->schoolkind === 'H') {
            $query .= "     T1.DIV        = '5' AND ";//コースグループの設定値
            $query .= "     T1.YEAR       = T2.YEAR AND ";
            $query .= "     T1.GRADE      = T2.GRADE AND ";
            $query .= "     T1.COURSECD   = '0' AND ";
            $query .= "     T1.MAJORCD    = T2.GROUP_CD AND ";
            $query .= "     T1.COURSECODE = '0000' AND ";
        } else {
            $query .= "     T1.DIV        = '3' AND ";//コースの設定値
            $query .= "     T1.YEAR       = T2.YEAR AND ";
            $query .= "     T1.COURSECD   = T2.COURSECD AND ";
            $query .= "     T1.MAJORCD    = T2.MAJORCD AND ";
            $query .= "     T1.YEAR       = T4.YEAR AND ";
            $query .= "     T1.COURSECODE = T4.COURSECODE AND ";
        }
        $query .= "     T1.GRADE        = '".$model->grade."' AND ";
        $query .= "     T1.HR_CLASS     = '000' AND ";
        $query .= "     T1.CLASSCD      = T3.CLASSCD AND ";
        $query .= "     T1.SCHOOL_KIND  = T3.SCHOOL_KIND AND ";
        $query .= "     T1.SCHOOL_KIND  = '".$model->grd_sch_kind[$model->grade]."' AND ";
        $query .= "     T1.CURRICULUM_CD = T3.CURRICULUM_CD AND ";
        $query .= "     T1.SUBCLASSCD   = T3.SUBCLASSCD ";
        $query .= " ORDER BY ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD ";

        return $query;
    }

    //コースグループ取得
    function getGroupCd($model) {
        $query  = " SELECT ";
        $query .= "     GROUP_CD AS VALUE, ";
        $query .= "     GROUP_CD || ':' || VALUE(GROUP_NAME,'') AS LABEL ";
        $query .= " FROM ";
        $query .= "     COURSE_GROUP_CD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     GRADE   = '".$model->grade."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //コースを表示(中学校用)
    function getCourseMajor($model) {
        $query  = " WITH SCHREG_DATA AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     COURSECODE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " AND GRADE = '".$model->grade."' ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE AS VALUE, ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE || ':' || L1.COURSENAME || L1.MAJORNAME || '　' || L2.COURSECODENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_DATA T1 ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                                    AND L1.COURSECD = T1.COURSECD ";
        $query .= "                                    AND L1.MAJORCD = T1.MAJORCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST L2 ON L2.YEAR = T1.YEAR ";
        $query .= "                                  AND L2.COURSECODE = T1.COURSECODE ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //科目コンボ
    function getSubclass($model) {
        $query  = " SELECT ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE, ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || ':' || SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' ";
        $query .= " AND SCHOOL_KIND = '".$model->grd_sch_kind[$model->grade]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //存在チェック
    function getExistsCnt($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*)  ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_RATE_GRADE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     GRADE       = '".$model->grade."' AND ";
        $query .= "     HR_CLASS = '000' AND ";//固定
        //コースグループコンボ
        if ($model->schoolkind === 'H') {
            $query .= "     DIV = '5' AND ";//コースグループの設定値
            $query .= "     COURSECD    = '0' AND ";
            $query .= "     MAJORCD     = '".$model->field["GROUP_CD"]."' AND ";
            $query .= "     COURSECODE  = '0000' AND ";
        } else {
            list($coursecd, $majorcd, $coursecode) = explode("-", $model->field["COURSE_MAJOR"]);
            $query .= "     DIV = '3' AND ";//コースの設定値
            $query .= "     COURSECD    = '".$coursecd."' AND ";
            $query .= "     MAJORCD     = '".$majorcd."' AND ";
            $query .= "     COURSECODE  = '".$coursecode."' AND ";
        }
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASS"]."' ";

        return $query;
    }

    //INSERT OR UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        list ($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode('-', $model->field["SUBCLASS"]);

        $db->autoCommit(false);

        $data = array();
        if ($model->cmd == "insert") {
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["CLASSCD"][TEXT]          = $classcd;
            $data["SCHOOL_KIND"][TEXT]      = $school_kind;
            $data["CURRICULUM_CD"][TEXT]    = $curriculum_cd;
            $data["SUBCLASSCD"][TEXT]       = $subclasscd;
            $data["GRADE"][TEXT]            = $model->grade;
            $data["HR_CLASS"][TEXT]         = '000';
            if ($model->schoolkind === 'H') {
                $data["DIV"][TEXT]              = 5;
                $data["COURSECD"][TEXT]         = 0;
                $data["MAJORCD"][TEXT]          = $model->field["GROUP_CD"];
                $data["COURSECODE"][TEXT]       = '0000';
            } else {
                list ($coursecd, $majorcd, $coursecode) = explode("-", $model->field["COURSE_MAJOR"]);
                $data["DIV"][TEXT]              = 3;
                $data["COURSECD"][TEXT]         = $coursecd;
                $data["MAJORCD"][TEXT]          = $majorcd;
                $data["COURSECODE"][TEXT]       = $coursecode;
            }
        }
        $data["RATE"][NUMBER]           = $model->field["RATE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "SYSDATE()";

        if ($model->cmd == "insert") {
            $query = Query::insertSQL($data, "SUBCLASS_RATE_GRADE_DAT");
        } else {
            $where  = " WHERE ";
            $where .= "     YEAR            = '".CTRL_YEAR."' AND ";
            $where .= "     CLASSCD         = '".$classcd."' AND ";
            $where .= "     SCHOOL_KIND     = '".$school_kind."' AND ";
            $where .= "     CURRICULUM_CD   = '".$curriculum_cd."' AND ";
            $where .= "     SUBCLASSCD      = '".$subclasscd."' AND ";
            $where .= "     GRADE           = '".$model->grade."' AND ";
            $where .= "     HR_CLASS        = '000' AND ";
            if ($model->schoolkind === 'H') {
                $where .= "     DIV         = '5' AND ";
                $where .= "     COURSECD    = '0' AND ";
                $where .= "     MAJORCD     = '".$model->field["GROUP_CD"]."' AND ";
                $where .= "     COURSECODE  = '0000' ";
            } else {
                list ($coursecd, $majorcd, $coursecode) = explode("-", $model->field["COURSE_MAJOR"]);
                $where .= "     DIV         = '3' AND ";
                $where .= "     COURSECD    = '".$coursecd."' AND ";
                $where .= "     MAJORCD     = '".$majorcd."' AND ";
                $where .= "     COURSECODE  = '".$coursecode."' ";
            }
            $query = Query::updateSQL($data, "SUBCLASS_RATE_GRADE_DAT", $where);
        }

        $db->query($query);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();
        list ($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode('-', $model->field["SUBCLASS"]);

        $query  = " DELETE FROM ";
        $query .= "     SUBCLASS_RATE_GRADE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".CTRL_YEAR."' AND ";
        $query .= "     CLASSCD         = '".$classcd."' AND ";
        $query .= "     SCHOOL_KIND     = '".$school_kind."' AND ";
        $query .= "     CURRICULUM_CD   = '".$curriculum_cd."' AND ";
        $query .= "     SUBCLASSCD      = '".$subclasscd."' AND ";
        $query .= "     GRADE           = '".$model->grade."' AND ";
        $query .= "     HR_CLASS        = '000' AND ";
        if ($model->schoolkind === 'H') {
            $query .= "     DIV         = '5' AND ";
            $query .= "     COURSECD    = '0' AND ";
            $query .= "     MAJORCD     = '".$model->field["GROUP_CD"]."' AND ";
            $query .= "     COURSECODE  = '0000' ";
        } else {
            list ($coursecd, $majorcd, $coursecode) = explode("-", $model->field["COURSE_MAJOR"]);
            $query .= "     DIV         = '3' AND ";
            $query .= "     COURSECD    = '".$coursecd."' AND ";
            $query .= "     MAJORCD     = '".$majorcd."' AND ";
            $query .= "     COURSECODE  = '".$coursecode."' ";
        }

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
    
    //SUBCLASS_RATE_GRADE_DATのデータチェック
    function checkdata($year, $div = "") {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_RATE_GRADE_DAT ";
        $query .= " WHERE ";
        if ($div == "LAST_DATA") {
            $query .= "     YEAR = '".$year."' ";
        } else {
            $query .= "     YEAR = '".CTRL_YEAR."' ";
        }

        return $query;
    }

    //コピー処理
    function getCopyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $lastyear = CTRL_YEAR - 1;

        $query  = " INSERT INTO SUBCLASS_RATE_GRADE_DAT ";
        $query .= " ( ";
        $query .= " SELECT ";
        $query .= "     '".CTRL_YEAR."', ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     DIV, ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD , ";
        $query .= "     COURSECODE, ";
        $query .= "     RATE, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_RATE_GRADE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$lastyear."' ";
        $query .= " ) ";

        $db->query($query);
        $db->commit();

        Query::dbCheckIn($db);

        return;
    }
}
?>
