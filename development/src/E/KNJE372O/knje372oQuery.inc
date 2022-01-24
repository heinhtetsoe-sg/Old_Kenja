<?php
class knje372oQuery extends Query {

    //年度
    function getYear($year_name){
        if($year_name == "OYEAR"){
            $query .= "WITH YEAR_T(VALUE, LABEL) AS ( ";
            $query .= "     VALUES('".CTRL_YEAR."', '".CTRL_YEAR."') ";
            $query .= ") ";
            $query .= "SELECT ";
            $query .= "    * ";
            $query .= "FROM ";
            $query .= "    YEAR_T ";
            $query .= "UNION ";
        }
        $query .= "  SELECT DISTINCT ";
        $query .= "      YEAR AS LABEL, ";
        $query .= "      YEAR AS VALUE ";
        $query .= "  FROM ";
        $query .= "      AFT_RECOMMENDATION_REQD_SUBCLASS_DAT ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    //推薦枠番号
    function getAftRecommendationLimitMst($year, $row, $select = "") {
        $query  = "  WITH MAIN AS( ";
        $query .= "    SELECT ";
        $query .= "      RECOMMENDATION_CD, ";
        $query .= "      CASE WHEN DEPARTMENT_S IS NOT NULL ";
        $query .= "           THEN '専願'||LPAD(DEPARTMENT_S,2,'0') ";
        $query .= "           ELSE '' ";
        $query .= "      END AS DEPARTMENT_S, ";
        $query .= "      CASE WHEN DEPARTMENT_H IS NOT NULL ";
        $query .= "           THEN '併願'||LPAD(DEPARTMENT_H,2,'0') ";
        $query .= "           ELSE '' ";
        $query .= "      END AS DEPARTMENT_H ";
        $query .= "    FROM ";
        $query .= "          AFT_RECOMMENDATION_LIMIT_MST ";
        $query .= "    WHERE ";
        $query .= "         YEAR = '".$year."' ";
        if($select == "CHECK"){
            $query .= "     AND RECOMMENDATION_CD = '".$row["RECOMMENDATION_CD"]."' ";
        }
        $query .= " ) ";
        $query .= "  SELECT ";
        $query .= "    RECOMMENDATION_CD AS VALUE, ";
        $query .= "    CASE WHEN DEPARTMENT_S <> '' AND DEPARTMENT_H <> '' ";
        $query .= "         THEN RECOMMENDATION_CD ||':'|| DEPARTMENT_S ||','|| DEPARTMENT_H ";
        $query .= "         ELSE RECOMMENDATION_CD ||':'|| DEPARTMENT_S || DEPARTMENT_H ";
        $query .= "    END AS LABEL ";
        $query .= "    FROM ";
        $query .= "      MAIN ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    //課程学科
    function getCourseMajorMst($year, $row, $select = "") {
        $query  = "  SELECT ";
        $query .= "      COURSECD || '-' || MAJORCD || ':' || VALUE(COURSENAME, '') || VALUE(MAJORNAME, '') AS LABEL, ";
        $query .= "      COURSECD || MAJORCD AS VALUE ";
        $query .= "  FROM ";
        $query .= "      V_COURSE_MAJOR_MST ";
        $query .= "  WHERE ";
        $query .= "             YEAR     = '".$year."' ";
        if($select == "CHECK"){
            $query .= "     AND COURSECD = '".$row["COURSECD"]."' ";
            $query .= "     AND MAJORCD  = '".$row["MAJORCD"]."' ";
        }
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    //コース
    function getCourseCodeMst($year, $row, $select = "") {
        $query  = "  SELECT ";
        $query .= "      COURSECODE || ':' || COURSECODENAME AS LABEL, ";
        $query .= "      COURSECODE AS VALUE ";
        $query .= "  FROM ";
        $query .= "      V_COURSECODE_MST ";
        $query .= "  WHERE ";
        $query .= "         YEAR = '".$year."' ";
        if($select == "CHECK"){
            $query .= " AND COURSECODE = '".$row["COURSECODE"]."' ";
        }
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    //教科コンボ
    function getClassMst($year, $select = "") {
        $query  = "  SELECT ";
        $query .= "      CLASSCD || SCHOOL_KIND AS VALUE, ";
        $query .= "      CLASSCD || '-' || SCHOOL_KIND || '-' || CLASSNAME AS LABEL ";
        $query .= "  FROM ";
        $query .= "      V_CLASS_MST ";
        $query .= "  WHERE ";
        $query .= "      YEAR        = '".$year."' ";
        $query .= "  AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";
        if($select == "subclass"){
            $query .= " FETCH FIRST 1 ROWS ONLY ";
        }

        return $query;
    }

    //科目一覧
    function getSubclassList($model, $row, $select = ""){
        if($select == "CHECK"){
            $classcd     = $row["CLASSCD"];
            $school_kind = $row["SCHOOL_KIND"];
        } else {
            $classcd     = $model->field["CLASSCD"];
            $school_kind = $model->field["SCHOOL_KIND"];
        }

        $query  = "  SELECT DISTINCT ";
        $query .= "     T2.RECOMMENDATION_CD, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ':' || T1.SUBCLASSNAME AS LABEL, ";
        $query .= "     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS VALUE ";
        $query .= "  FROM ";
        $query .= "     V_SUBCLASS_MST T1 ";
        $query .= "  LEFT JOIN ";
        $query .= "    AFT_RECOMMENDATION_REQD_SUBCLASS_DAT T2 ON T1.YEAR                   = T2.YEAR ";
        $query .= "                                           AND T1.CLASSCD                = T2.CLASSCD ";
        $query .= "                                           AND T1.SCHOOL_KIND            = T2.SCHOOL_KIND ";
        $query .= "                                           AND T1.CURRICULUM_CD          = T2.CURRICULUM_CD ";
        $query .= "                                           AND T1.SUBCLASSCD             = T2.SUBCLASSCD";
        $query .= "                                           AND T2.RECOMMENDATION_CD      = '".$model->field["RECOMMENDATION_CD"]."' ";
        $query .= "                                           AND T2.COURSECD || T2.MAJORCD = '".$model->field["COURSEMAJOR"]."' ";
        $query .= "                                           AND T2.COURSECODE             = '".$model->field["COURSECODE"]."' ";
        $query .= "  WHERE ";
        $query .= "             T1.YEAR        = '".$model->oyear."' ";
        $query .= "         AND T1.CLASSCD     = '".$classcd."' ";
        $query .= "         AND T1.SCHOOL_KIND = '".$school_kind."' ";
        if($select == "CHECK"){
            $query .= "     AND T1.CURRICULUM_CD = '".$row["CURRICULUM_CD"]."' ";
            $query .= "     AND T1.SUBCLASSCD    = '".$row["SUBCLASSCD"]."' ";
        }
        $query .= "  ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示(左画面)
    function getSelectQuery($model){
        $query  = "  SELECT DISTINCT ";
        $query .= "    T1.RECOMMENDATION_CD, ";
        $query .= "    T2.DEPARTMENT_S, ";
        $query .= "    T2.DEPARTMENT_H, ";
        $query .= "    T1.COURSECD || T1.MAJORCD AS COURSEMAJOR, ";
        $query .= "    T1.COURSECODE, ";
        $query .= "    T1.COURSECD || T1.MAJORCD || T1.COURSECODE || ':' || T3.COURSECODENAME AS COURSECODENAME, ";
        $query .= "    T1.CLASSCD || T1.SCHOOL_KIND AS CLASSCD ";
        $query .= "  FROM ";
        $query .= "    AFT_RECOMMENDATION_REQD_SUBCLASS_DAT T1 ";
        $query .= "  LEFT JOIN ";
        $query .= "    AFT_RECOMMENDATION_LIMIT_MST T2 ON T1.YEAR              = T2.YEAR ";
        $query .= "                                   AND T1.RECOMMENDATION_CD = T2.RECOMMENDATION_CD ";
        $query .= "  LEFT JOIN ";
        $query .= "    COURSECODE_MST T3 ON T3.COURSECODE = T1.COURSECODE ";
        $query .= "  WHERE ";
        $query .= "    T1.YEAR = '".$model->oyear."' ";
        $query .= "  ORDER BY ";
        $query .= "    T1.RECOMMENDATION_CD, ";
        $query .= "    COURSECODE ";

        return $query;
    }

    //科目取得(左画面)
    function getSubclass($year,$row){
        $query  = "  SELECT ";
        $query .= "    T2.SUBCLASSNAME ";
        $query .= "  FROM ";
        $query .= "    AFT_RECOMMENDATION_REQD_SUBCLASS_DAT T1 ";
        $query .= "  LEFT JOIN ";
        $query .= "    V_SUBCLASS_MST T2 ON T1.YEAR          = T2.YEAR ";
        $query .= "                     AND T1.CLASSCD       = T2.CLASSCD ";
        $query .= "                     AND T1.SCHOOL_KIND   = T2.SCHOOL_KIND ";
        $query .= "                     AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
        $query .= "                     AND T1.SUBCLASSCD    = T2.SUBCLASSCD";
        $query .= "  WHERE ";
        $query .= "        T1.YEAR                      = '".$year."' ";
        $query .= "    AND T1.RECOMMENDATION_CD         = '".$row["RECOMMENDATION_CD"]."' ";
        $query .= "    AND T1.COURSECD || T1.MAJORCD    = '".$row["COURSEMAJOR"]."' ";
        $query .= "    AND T1.COURSECODE                = '".$row["COURSECODE"]."' ";
        $query .= "    AND T1.CLASSCD || T1.SCHOOL_KIND = '".$row["CLASSCD"]."' ";
        $query .= "  ORDER BY ";
        $query .= "    T1.SUBCLASSCD ";

        return $query;
    }

    //存在チェック
    function getSelectData($model)
    {
        $query  = "  SELECT ";
        $query .= "    * ";
        $query .= "  FROM ";
        $query .= "    AFT_RECOMMENDATION_REQD_SUBCLASS_DAT T1 ";
        $query .= "  WHERE ";
        $query .= "     YEAR              = '".$model->oyear."' ";
        $query .= " AND RECOMMENDATION_CD = '".$model->field["RECOMMENDATION_CD"]."' ";
        $query .= " AND COURSECD          = '".substr($model->field["COURSEMAJOR"], 0, 1)."' ";
        $query .= " AND MAJORCD           = '".substr($model->field["COURSEMAJOR"], 1, 3)."' ";
        $query .= " AND COURSECODE        = '".substr($model->field["COURSECODE"], 0, 4)."' ";
        $query .= " AND CLASSCD           = '".$model->field["CLASSCD"]."' ";
        $query .= " AND SCHOOL_KIND       = '".$model->field["SCHOOL_KIND"]."' ";

        return $query;
    }

    //削除
    function &getDeleteQuery($model,&$db)
    {
        $query  = "  DELETE FROM AFT_RECOMMENDATION_REQD_SUBCLASS_DAT ";
        $query .= "  WHERE ";
        $query .= "        YEAR              = '".$model->oyear."' ";
        $query .= "    AND RECOMMENDATION_CD = '".$model->field["RECOMMENDATION_CD"]."' ";
        $query .= "    AND COURSECD          = '".substr($model->field["COURSEMAJOR"], 0, 1)."' ";
        $query .= "    AND MAJORCD           = '".substr($model->field["COURSEMAJOR"], 1, 3)."' ";
        $query .= "    AND COURSECODE        = '".substr($model->field["COURSECODE"], 0, 4)."' ";
        $query .= "    AND CLASSCD           = '".$model->field["CLASSCD"]."' ";
        $query .= "    AND SCHOOL_KIND       = '".$model->field["SCHOOL_KIND"]."' ";
        $db->query($query);

        return;
    }

    //追加・更新
    function &getUpdateQuery($model,&$db){
        if ($model->selectLeft) {
            $selectSchArr = explode(",", $model->selectLeft);
            foreach ($selectSchArr as $selectSchCd) {
                $data = array();
                $data["YEAR"][TEXT]              = $model->oyear;
                $data["RECOMMENDATION_CD"][TEXT] = $model->field["RECOMMENDATION_CD"];
                $data["COURSECD"][TEXT]          = substr($model->field["COURSEMAJOR"], 0, 1);
                $data["MAJORCD"][TEXT]           = substr($model->field["COURSEMAJOR"], 1, 3);
                $data["COURSECODE"][TEXT]        = substr($model->field["COURSECODE"], 0, 4);
                $data["CLASSCD"][TEXT]           = substr($selectSchCd, 0, 2);
                $data["SCHOOL_KIND"][TEXT]       = substr($selectSchCd, 2, 1);
                $data["CURRICULUM_CD"][TEXT]     = substr($selectSchCd, 3, 1);
                $data["SUBCLASSCD"][TEXT]        = substr($selectSchCd, 4, 6);
                $data["REGISTERCD"][TEXT]        = STAFFCD;
                $data["UPDATED"][FUNC]           = "sysdate()";

                $query = Query::insertSQL($data, "AFT_RECOMMENDATION_REQD_SUBCLASS_DAT");
                $db->query($query);
            }
        }
        return;
    }

    //前年度データ取得
    function &getCopyData($year) {
        $query  = "  SELECT ";
        $query .= "     * ";
        $query .= "  FROM ";
        $query .= "     AFT_RECOMMENDATION_REQD_SUBCLASS_DAT ";
        $query .= "  WHERE ";
        $query .= "     YEAR = '".$year."' ";

        return $query;
    }

    //コピー処理
    function &getCopyQuery($model, $row,&$db){
        $data = array();
        $data["YEAR"][TEXT]              = $model->oyear;
        $data["RECOMMENDATION_CD"][TEXT] = $row["RECOMMENDATION_CD"];
        $data["COURSECD"][TEXT]          = $row["COURSECD"];
        $data["MAJORCD"][TEXT]           = $row["MAJORCD"];
        $data["COURSECODE"][TEXT]        = $row["COURSECODE"];
        $data["CLASSCD"][TEXT]           = $row["CLASSCD"];
        $data["SCHOOL_KIND"][TEXT]       = $row["SCHOOL_KIND"];
        $data["CURRICULUM_CD"][TEXT]     = $row["CURRICULUM_CD"];
        $data["SUBCLASSCD"][TEXT]        = $row["SUBCLASSCD"];
        $data["REGISTERCD"][TEXT]        = STAFFCD;
        $data["UPDATED"][FUNC]           = "sysdate()";

        $query = Query::insertSQL($data, "AFT_RECOMMENDATION_REQD_SUBCLASS_DAT");
        $db->query($query);
        return;
    }

}
?>
