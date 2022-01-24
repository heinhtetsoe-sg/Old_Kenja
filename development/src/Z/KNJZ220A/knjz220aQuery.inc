<?php

require_once('for_php7.php');


class knjz220aquery extends Query {

    //学期一覧取得
    function getSemesterList()
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }

    //教科
    function getClass($model)
    {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND || '-' || CLASSCD AS SORTVAL, ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND AS VALUE, ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || ':' || CLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_CLASS_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["useCurriculumcd"] == '1' && $model->field["SCHOOL_KIND"] != "ALL") {
            $query .= "     AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SORTVAL ";

        return $query;
    }

    //科目コンボ
    function getSubclass($model) {
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
        //選択校種条件
        if ($model->field["SCHOOL_KIND"] != "ALL") {
            $query .= " AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        }
        //選択教科条件
        if ($model->field["CLASSCD"] != "ALL") {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= " AND CLASSCD || '-' || SCHOOL_KIND = '".$model->field["CLASSCD"]."' ";
            } else {
                $classcdArr = explode("-", $model->field["CLASSCD"]);
                $query .= " AND SUBSTR(SUBCLASSCD, 1, 2) = '".$classcdArr[0]."' ";
            }
        }

        $query .= " ORDER BY ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " SCHOOL_KIND, CLASSCD, CURRICULUM_CD, SUBCLASSCD ";
        } else {
            $query .= " SUBCLASSCD ";
        }

        return $query;
    }

    //全体のデータの取得
    function selectQuery($model, $semesValList)
    {
        $query   = "  SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query  .= "    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
        } else {
            $query  .= "    T1.SUBCLASSCD AS SUBCLASSCD, ";
        }
        $query  .= "    T1.SUBCLASSNAME, ";
        $query  .= "    T1.SUBCLASSABBV, ";
        $query  .= "    TMP.SEMESTER, ";
        $query  .= "    T2.RATE ";
        $query  .= "  FROM ";
        $query  .= "    V_SUBCLASS_MST T1 ";

        //固定数値列を作成
        $query  .= "  CROSS JOIN ( ";
        $sep = "";
        foreach ($semesValList as $val) {
            $query  .= $sep."    SELECT ".$val." AS SEMESTER FROM SYSIBM.SYSDUMMY1 ";
            $sep = "    UNION ";
        }
        $query  .= "  ) AS TMP ";

        $query  .= "    LEFT JOIN SUBCLASS_RATE_SEMES_DAT T2 ";
        $query  .= "      ON ";
        $query  .= "  	      T1.YEAR = T2.YEAR ";
        $query  .= "      AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD ";
        $query  .= "      AND T2.SEMESTER = TMP.SEMESTER ";
        $query  .= "  WHERE ";
        $query  .= "    T1.YEAR = '".CTRL_YEAR."' ";
        //選択条件
        if ($model->field["SUBCLASSCD"] != "ALL") {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query  .= "      AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
            } else {
                $query  .= "      AND T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
            }
        } else if ($model->field["CLASSCD"] != "ALL") {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= " AND T1.CLASSCD || '-' || T1.SCHOOL_KIND = '".$model->field["CLASSCD"]."' ";
            } else {
                $classcdArr = explode("-", $model->field["CLASSCD"]);
                $query .= " AND SUBSTR(T1.SUBCLASSCD, 1, 2) = '".$classcdArr[0]."' ";
            }
        } else if ($model->field["SCHOOL_KIND"] != "ALL") {
            $query .= " AND T1.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        }

        $query  .= "  ORDER BY ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " T1.SCHOOL_KIND, T1.CLASSCD, T1.CURRICULUM_CD, T1.SUBCLASSCD, ";
        } else {
            $query  .= "    T1.SUBCLASSCD, ";
        }
        $query  .= " TMP.SEMESTER ";

        return $query;
    }

    /* データ更新処理 */
    function update($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        //DELETE
        foreach ($model->nameList as $name) {
            list($dummy, $subclasscd, $semester) = explode("_", $name);

            $query   = "  DELETE FROM SUBCLASS_RATE_SEMES_DAT ";
            $query  .= "  WHERE ";
            $query  .= "  YEAR          = '".CTRL_YEAR."'    AND ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                list($classcd, $school_kind, $curriculumcd, $subclasscd) = explode("-", $subclasscd);
                $query  .= "  CLASSCD       = '".$classcd."'       AND ";
                $query  .= "  SCHOOL_KIND   = '".$school_kind."'   AND ";
                $query  .= "  CURRICULUM_CD = '".$curriculumcd."'  AND ";
            }
            $query  .= "  SUBCLASSCD    = '".$subclasscd."'  AND ";
            $query  .= "  SEMESTER      = '".$semester."' ";

            $db->query($query);
        }

        //INSERT
        foreach ($model->rateList as $key => $rate) {
            list($dummy, $subclasscd, $semester) = explode("_", $key);

            //項目
            $data = array();
            $data["YEAR"][TEXT]                 = CTRL_YEAR;
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                list($classcd, $school_kind, $curriculumcd, $subclasscd) = explode("-", $subclasscd);

                $data["CLASSCD"][TEXT]          = $classcd;
                $data["SCHOOL_KIND"][TEXT]      = $school_kind;
                $data["CURRICULUM_CD"][TEXT]    = $curriculumcd;
            }
            $data["SUBCLASSCD"][TEXT]           = $subclasscd;
            $data["SEMESTER"][TEXT]             = $semester;
            $data["RATE"][TEXT]                 = $rate;

            $data["REGISTERCD"][TEXT]           = STAFFCD ;
            $data["UPDATED"][FUNC]              = "sysdate()";

            //追加
            $query = Query::insertSQL($data, "SUBCLASS_RATE_SEMES_DAT");

            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
