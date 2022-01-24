<?php

require_once('for_php7.php');

class knjz401jQuery extends Query {

    //年度一覧取得
    function getYearList($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM  ";
        $query .= "     JVIEWNAME_GRADE_YDAT ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " WHERE ";
                $query .= "     SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1") {
            $query .= " WHERE ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     YEAR DESC ";

        return $query;
    }

    //年度観点一覧取得
    function selectQuery($model)
    {
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.GRADE || '-' || T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || '-' || T1.VIEWCD || ' ' || T1.VIEWNAME AS LABEL, ";
            $query .= "     T1.GRADE || '-' || T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || '-' || T1.VIEWCD AS VALUE, ";
        } else {
            $query .= "     T1.GRADE || '-' || T1.SUBCLASSCD || '-' || T1.VIEWCD || ' ' || T1.VIEWNAME AS LABEL, ";
            $query .= "     T1.GRADE || '-' || T1.SUBCLASSCD || '-' || T1.VIEWCD  AS VALUE, ";
        }
        $query .= "     T1.GRADE, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, "; 
        $query .= "     T1.VIEWCD, ";
        $query .= "     T1.VIEWNAME ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_MST T1, ";
        $query .= "     JVIEWNAME_GRADE_YDAT T2 ";
        $query .= " WHERE "; 
        $query .= "     T2.YEAR = '".$model->year."' "; 
        $query .= " AND T1.GRADE = T2.GRADE "; 
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND T1.CLASSCD       = T2.CLASSCD ";
            $query .= " AND T1.SCHOOL_KIND   = T2.SCHOOL_KIND ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1") {
                $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= " AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
        }
        $query .= " AND T1.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= " AND T1.VIEWCD = T2.VIEWCD "; 
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //観点一覧取得
    function selectViewNameQuery($viewcd_array, $model)
    {
        $query  = " SELECT "; 
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.GRADE || '-' || T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || '-' || T1.VIEWCD || ' ' || T1.VIEWNAME AS LABEL, ";
            $query .= "     T1.GRADE || '-' || T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || '-' || T1.VIEWCD AS VALUE, ";
        } else {
            $query .= "     T1.GRADE || '-' || T1.SUBCLASSCD || '-' || T1.VIEWCD || ' ' || T1.VIEWNAME AS LABEL, ";
            $query .= "     T1.GRADE || '-' || T1.SUBCLASSCD || '-' || T1.VIEWCD  AS VALUE, ";
        }
        $query .= "     T1.GRADE, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, "; 
        $query .= "     T1.VIEWCD, "; 
        $query .= "     T1.VIEWNAME ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_MST T1 ";
        $query .= " WHERE ";
        $query .= "     NOT EXISTS (SELECT 'X' ";
        $query .= "                 FROM JVIEWNAME_GRADE_YDAT T2 ";
        $query .= "                 WHERE T2.YEAR = '".$model->year."' ";
        $query .= "                   AND T2.GRADE = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                  AND T2.CLASSCD       = T1.CLASSCD ";
            $query .= "                  AND T2.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "                  AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "                   AND T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "                   AND T2.VIEWCD = T1.VIEWCD ) ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE "; 

        return $query;
    }

    //UPDATE
    function &getUpdateQuery($model)
    {
        $all_light = false;
        //観点コード配列に格納
        $array = explode (",", $model->selectdata);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time()); //更新日付

        $query = " DELETE FROM JVIEWNAME_GRADE_YDAT WHERE year = '" .$model->year ."'";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $db->query($query);

        //観点年度データの登録
        if ($array[0] != "") {
            $query  = "INSERT ";
            $query .= "     INTO JVIEWNAME_GRADE_YDAT ";
            $query .= "    ( YEAR, ";
            $query .= "      GRADE,";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "      CLASSCD,";
                $query .= "      SCHOOL_KIND,";
                $query .= "      CURRICULUM_CD,";
            }
            $query .= "      SUBCLASSCD,";
            $query .= "      VIEWCD,";
            $query .= "      REGISTERCD, ";
            $query .= "      UPDATED  ) ";
            $query .= " VALUES ";
            for ($i = 0; $i < get_count($array); $i++){
                $sp = ",";
                if ($i == get_count($array)-1){
                    $sp = "";
                }
                $data = preg_split("/-/", $array[$i]);

                $query.= "('" .$model->year ."'";                               //年度
                $query.= ",'" .$data[0] ."'";                                   //学年
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query.= ",'" .$data[1] ."'";                                   //教科コード
                    $query.= ",'" .$data[2] ."'";                                   //学校校種
                    $query.= ",'" .$data[3] ."'";                                   //教育課程コード
                    $query.= ",'" .$data[4] ."'";                                   //科目コード
                    $query.= ",'" .$data[5] ."'";                                   //観点コード
                } else {
                    $query.= ",'" .$data[1] ."'";                                   //科目コード
                    $query.= ",'" .$data[2] ."'";                                   //観点コード
                }
                $query.= ",'" .STAFFCD ."'";                                    //STAFFCD
                $query.= ",'" .$updated ."')" .$sp;                             //更新日付

            }
            $db->query($query);
        } else {
            $all_light = true;
        }
        $db->commit(); //トランザクションをコミットする。

        Query::dbCheckIn($db);
        return $all_light;
    }
}
?>
