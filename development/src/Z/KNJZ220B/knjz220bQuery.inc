<?php

require_once('for_php7.php');


class knjz220bQuery extends Query {

    //教科一覧取得
    function getClass($model) {
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "      CLASSCD || '-' || SCHOOL_KIND || ':' || CLASSNAME AS LABEL, ";
            $query .= "      CLASSCD || '-' || SCHOOL_KIND AS VALUE ";
        } else {
            $query .= "      CLASSCD || ':' || CLASSNAME AS LABEL, ";
            $query .= "      CLASSCD AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     V_CLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "      SCHOOL_KIND, ";
        }
        $query .= "     CLASSCD";

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

        if ($model->Properties["useCurriculumcd"] == '1') {
            list($classcd, $schoolKind) = explode("-", $model->field["CLASSCD"]);
            $query .= "     AND CLASSCD = '".$classcd."' ";
            $query .= "     AND SCHOOL_KIND = '".$schoolKind."' ";
        } else {
            $query .= "     AND CLASSCD = '".$model->field["CLASSCD"]."' ";
        }

        $query .= " ORDER BY ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " SCHOOL_KIND, CLASSCD, CURRICULUM_CD, SUBCLASSCD ";
        } else {
            $query .= " SUBCLASSCD ";
        }

        return $query;
    }

    //科目一覧リスト用
    function getSubclassList($model) {

        if ($model->Properties["useCurriculumcd"] == '1') {
            list($classcd, $schoolKind, $curriculumCd, $subclassCd) = explode("-", $model->field["SUBCLASSCD"]);
        } else {
            $subclassCd = $model->field["SUBCLASSCD"];
        }

        $query  .= "  SELECT ";
        $query  .= "    T1.YEAR, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query  .= "    T1.CLASSCD, ";
            $query  .= "    T1.SCHOOL_KIND, ";
            $query  .= "    T1.CURRICULUM_CD, ";
        }
        $query  .= "    T1.SUBCLASSCD, ";
        $query  .= "    T1.SUBCLASSNAME, ";
        $query  .= "    T2.COMBINED_CLASSCD, ";
        $query  .= "    T2.COMBINED_SCHOOL_KIND, ";
        $query  .= "    T2.COMBINED_CURRICULUM_CD, ";
        $query  .= "    T2.COMBINED_SUBCLASSCD, ";
        $query  .= "    T2.ATTEND_CLASSCD, ";
        $query  .= "    T2.ATTEND_SCHOOL_KIND, ";
        $query  .= "    T2.ATTEND_CURRICULUM_CD, ";
        $query  .= "    T2.ATTEND_SUBCLASSCD ";
        $query  .= "  FROM ";
        $query  .= "    V_SUBCLASS_MST T1 ";
        $query  .= "    LEFT OUTER JOIN SUBCLASS_RANK_REPLACE_DAT T2 ";
        $query  .= "      ON T1.YEAR = T2.YEAR ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query  .= "      AND T1.CLASSCD = T2.ATTEND_CLASSCD ";
            $query  .= "      AND T1.SCHOOL_KIND = T2.ATTEND_SCHOOL_KIND ";
            $query  .= "      AND T1.CURRICULUM_CD = T2.ATTEND_CURRICULUM_CD ";
        }
        $query  .= "      AND T1.SUBCLASSCD = T2.ATTEND_SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query  .= "      AND T2.COMBINED_CLASSCD = '".$classcd."' ";
            $query  .= "      AND T2.COMBINED_SCHOOL_KIND = '".$schoolKind."' ";
            $query  .= "      AND T2.COMBINED_CURRICULUM_CD = '".$curriculumCd."' ";
        }
        $query  .= "      AND T2.COMBINED_SUBCLASSCD = '".$subclassCd."' ";
        $query  .= "  WHERE ";
        $query  .= "    T1.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            list($classcd, $schoolKind, $curriculumCd, $subclassCd) = explode("-", $model->field["SUBCLASSCD"]);
            $query .= "     AND CLASSCD = '".$classcd."' ";
            $query .= "     AND SCHOOL_KIND = '".$schoolKind."' ";
        }
        //合併先科目を除く
        if ($model->field["SUBCLASSCD"]) {
            if ($model->Properties["useCurriculumcd"] == '1') {
                 $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD <> '".$model->field["SUBCLASSCD"]."' ";
            } else {
                 $query .= "     AND SUBCLASSCD <> '".$model->field["SUBCLASSCD"]."' ";
            }
        }
        $query .= " ORDER BY ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " T1.SCHOOL_KIND, T1.CLASSCD, T1.CURRICULUM_CD, T1.SUBCLASSCD ";
        } else {
            $query .= " T1.SUBCLASSCD ";
        }

        return $query;
    }

    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if ($model->Properties["useCurriculumcd"] == '1') {
            list($classcd, $schoolKind, $curriculumCd, $subclassCd) = explode("-", $model->field["SUBCLASSCD"]);
        } else {
            $subclassCd = $model->field["SUBCLASSCD"];
        }

        $query  = " DELETE FROM ";
        $query .= "     SUBCLASS_RANK_REPLACE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND COMBINED_CLASSCD       = '".$classcd."' ";
            $query .= "     AND COMBINED_SCHOOL_KIND   = '". $schoolKind."'  ";
            $query .= "     AND COMBINED_CURRICULUM_CD = '".$curriculumCd."'  ";
        }
        $query .= "     AND COMBINED_SUBCLASSCD = '".$subclassCd."' ";

        $db->query($query);

        $data = array();
        $selectdata = explode(",", $model->selectdata);
        for ($i = 0; $i < get_count($selectdata); $i++) {
            if (!$selectdata[$i]) {
                continue;
            }
            $data["YEAR"][TEXT]                 = CTRL_YEAR;
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["COMBINED_CLASSCD"][TEXT]        = $classcd;
                $data["ATTEND_CLASSCD"][TEXT]          = substr($selectdata[$i],0,2);
                $data["COMBINED_SCHOOL_KIND"][TEXT]    = $schoolKind;
                $data["ATTEND_SCHOOL_KIND"][TEXT]      = substr($selectdata[$i],3,1);
                $data["COMBINED_CURRICULUM_CD"][TEXT]  = $curriculumCd;
                $data["ATTEND_CURRICULUM_CD"][TEXT]    = substr($selectdata[$i],5,1);
                $data["COMBINED_SUBCLASSCD"][TEXT]     = $subclassCd;
                $data["ATTEND_SUBCLASSCD"][TEXT]       = substr($selectdata[$i],7,13);
            } else {
                $data["COMBINED_SUBCLASSCD"][TEXT]  = $subclassCd;
                $data["ATTEND_SUBCLASSCD"][TEXT]    = $selectdata[$i];
            }
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "SYSDATE()";
            $query = Query::insertSQL($data, "SUBCLASS_RANK_REPLACE_DAT");

            $db->query($query);
        }
        $db->commit();

        Query::dbCheckIn($db);
        return;
    }

}
?>
