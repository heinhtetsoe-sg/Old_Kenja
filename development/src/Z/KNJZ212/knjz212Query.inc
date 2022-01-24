<?php

require_once('for_php7.php');

class knjz212Query extends Query {

    //学期取得
    function getSemester()
    {
        $query  = "SELECT ";
        $query .= "    SEMESTER AS VALUE, ";
        $query .= "    SEMESTERNAME AS LABEL ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER < '9' ";
        $query .= "ORDER BY ";
        $query .= "    SEMESTER ";

        return $query;
    }

    //学年取得
    function getGrade($model)
    {
        $query  = "SELECT DISTINCT ";
        $query .= "    GRADE AS VALUE, ";
        $query .= "    CAST(CAST(GRADE AS SMALLINT) AS CHAR(1)) || '学年' AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_HDAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER = '".$model->semester."' ";
        $query .= "ORDER BY ";
        $query .= "    GRADE ";

        return $query;
    }

    //学校校種取得
    function getSchoolKind($model) {
        $query  = "SELECT ";
        $query .= "        SCHOOL_KIND ";
        $query .= "FROM ";
        $query .= "        SCHREG_REGD_GDAT ";
        $query .= "WHERE ";
        $query .= "        GRADE = '".$model->grade."' ";
        $query .= "AND      YEAR = '".CTRL_YEAR."' ";
        
        return $query;
    }


    //データ取得
    function Getdata($model)
    {
        $query  = " SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.SUBCLASSNAME, ";
        $query .= "     L1.VALUE ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST T1 ";
        $query .= "     LEFT JOIN SUBCLASS_ADJUST_DAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "          AND L1.SEMESTER = '".$model->semester."' ";
        $query .= "          AND L1.GRADE = '".$model->grade."' ";
        $query .= "          AND L1.SUBCLASSCD = T1.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "          AND L1.CLASSCD         = T1.CLASSCD ";
            $query .= "          AND L1.SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "          AND L1.CURRICULUM_CD   = T1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND EXISTS(SELECT ";
        $query .= "                    * ";
        $query .= "                FROM ";
        $query .= "                    CREDIT_MST E1 ";
        $query .= "                WHERE ";
        $query .= "                    E1.YEAR = T1.YEAR ";
        $query .= "                    AND E1.GRADE = '".$model->grade."' ";
        $query .= "                    AND E1.SUBCLASSCD = T1.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "          AND E1.CLASSCD         = T1.CLASSCD ";
            $query .= "          AND E1.SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "          AND E1.CURRICULUM_CD   = T1.CURRICULUM_CD ";
        }
        $query .= "               ) ";
        $query .= " ORDER BY ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " T1.CLASSCD, ";
            $query .= " T1.SCHOOL_KIND, ";
            $query .= " T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD ";

        return $query;
    }

    //コピーチェック
    function getDataCnt()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_ADJUST_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //コピー処理
    function getCopyQuery($model)
    {
        $query  = " INSERT INTO ";
        $query .= "     SUBCLASS_ADJUST_DAT ";
        $query .= " (SELECT ";
        $query .= "      '".CTRL_YEAR."', ";
        $query .= "      SEMESTER, ";
        $query .= "      GRADE, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "      SUBCLASSCD, ";
        $query .= "      VALUE, ";
        $query .= "      '".STAFFCD."', ";
        $query .= "      sysdate() ";
        $query .= "  FROM ";
        $query .= "      SUBCLASS_ADJUST_DAT ";
        $query .= "  WHERE ";
        $query .= "      YEAR = '".(CTRL_YEAR - 1)."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "      AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD IN (SELECT ";
            $query .= "                             CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD ";
            $query .= "                         FROM ";
            $query .= "                             V_SUBCLASS_MST ";
            $query .= "                         WHERE ";
            $query .= "                             YEAR = '".CTRL_YEAR."' ) ";
        }
        $query .= "      AND SUBCLASSCD IN (SELECT ";
        $query .= "                             SUBCLASSCD ";
        $query .= "                         FROM ";
        $query .= "                             V_SUBCLASS_MST ";
        $query .= "                         WHERE ";
        $query .= "                             YEAR = '".CTRL_YEAR."' ) ";
        $query .= "      ) ";

        return $query;
    }

    //DELETE(更新前処理)
    function getDeleteQuery($model)
    {
        $query  = " DELETE FROM ";
        $query .= "     SUBCLASS_ADJUST_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$model->semester."' ";
        $query .= "     AND GRADE = '".$model->grade."' ";
        return $query;
    }

    //UPDATE(更新処理)
    function getUpdateQuery($model, $subclasscd, $value)
    {
        $data = array();
        $data["YEAR"][TEXT]       = CTRL_YEAR;
        $data["SEMESTER"][TEXT]   = $model->semester;
        $data["GRADE"][TEXT]      = $model->grade;
        $data["SUBCLASSCD"][TEXT] = $subclasscd;
        $data["VALUE"][NUMBER]    = $value;
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC]    = "sysdate()";
        $query = Query::insertSQL($data, "SUBCLASS_ADJUST_DAT");
        return $query;
    }
    
    //UPDATE(更新処理)※教育課程対応
    function getUpdateUseCurriculumQuery($model, $classcd ,$schoolkind, $curriculumcd, $subclasscd, $value)
    {
        $data = array();
        $data["YEAR"][TEXT]          = CTRL_YEAR;
        $data["SEMESTER"][TEXT]      = $model->semester;
        $data["GRADE"][TEXT]         = $model->grade;
        $data["CLASSCD"][TEXT]       = $classcd;
        $data["SCHOOL_KIND"][TEXT]   = $schoolkind;
        $data["CURRICULUM_CD"][TEXT] = $curriculumcd;
        $data["SUBCLASSCD"][TEXT]    = $subclasscd;
        $data["VALUE"][NUMBER]       = $value;
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";
        $query = Query::insertSQL($data, "SUBCLASS_ADJUST_DAT");
        return $query;
    }
}
?>
