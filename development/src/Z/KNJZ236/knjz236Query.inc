<?php

require_once('for_php7.php');

class knjz236Query extends Query {
    //教科一覧取得
    function GetClass($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "      SCHOOL_KIND, ";
        }
        $query .= "     CLASSNAME ";
        $query .= " FROM ";
        $query .= "     V_CLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     CLASSCD";

        return $query;
    }

    //教科名称取得
    function GetClassName($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     CLASSNAME ";
        $query .= " FROM ";
        $query .= "     V_CLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND CLASSCD = '".$model->rightclasscd."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND SCHOOL_KIND   = '".$model->school_kind."' ";
        }
        
        return $query;
    }

    //科目リスト
    function GetSubClass($model, $classcd, $div = "")
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $classcd);
        }
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ':' || T1.SUBCLASSNAME AS LABEL, ";
        } else {
            $query .= "     T1.SUBCLASSCD AS VALUE, ";
            $query .= "     T1.SUBCLASSNAME AS LABEL, ";
        }
        if ($div == "MAIN") {
            $query .= "     COUNT(T1.SUBCLASSCD) AS CNT ";
        } else {
            $query .= "     L1.MAJOR_ATTEND_SUBCLASSCD, ";
            $query .= "     L1.ATTEND_SUBCLASSCD, ";
            $query .= "     L1.ATTEND_NAME, ";
            $query .= "     L1.CALCULATE ";
        }
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST T1 ";
        $query .= "     LEFT JOIN ( SELECT DISTINCT ";
        $query .= "                     W1.SUBSTITUTION_SUBCLASSCD, ";
        $query .= "                     W1.ATTEND_SUBCLASSCD, ";
        $query .= "                     W2.SUBCLASSNAME AS ATTEND_NAME, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     W1.SUBSTITUTION_CLASSCD, ";
            $query .= "                     W1.SUBSTITUTION_SCHOOL_KIND, ";
            $query .= "                     W1.SUBSTITUTION_CURRICULUM_CD, ";
        }
        $query .= "                     CASE WHEN W1.SUBSTITUTION_TYPE_FLG = '1' ";
        $query .= "                          THEN '全部代替' ";
        $query .= "                          ELSE '一部代替' END AS CALCULATE, ";
        $query .= "                     W3.ATTEND_SUBCLASSCD AS MAJOR_ATTEND_SUBCLASSCD ";
        $query .= "                 FROM ";
        $query .= "                     SUBCLASS_REPLACE_SUBSTITUTION_DAT W1 ";
        $query .= "                     LEFT JOIN V_SUBCLASS_MST W2 ON W2.YEAR = W1.YEAR ";
        $query .= "                          AND W2.SUBCLASSCD = W1.ATTEND_SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                          AND W2.CLASSCD         = W1.ATTEND_CLASSCD ";
            $query .= "                          AND W2.SCHOOL_KIND     = W1.ATTEND_SCHOOL_KIND ";
            $query .= "                          AND W2.CURRICULUM_CD   = W1.ATTEND_CURRICULUM_CD ";
        }

        $query .= "                     LEFT JOIN SUBCLASS_REPLACE_SUBSTITUTION_MAJOR_DAT W3 ON W3.YEAR = W1.YEAR ";
        $query .= "                          AND W3.SUBSTITUTION_SUBCLASSCD = W1.SUBSTITUTION_SUBCLASSCD ";
        $query .= "                          AND W3.ATTEND_SUBCLASSCD = W1.ATTEND_SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                          AND W3.SUBSTITUTION_CLASSCD         = W1.SUBSTITUTION_CLASSCD ";
            $query .= "                          AND W3.SUBSTITUTION_SCHOOL_KIND     = W1.SUBSTITUTION_SCHOOL_KIND ";
            $query .= "                          AND W3.SUBSTITUTION_CURRICULUM_CD   = W1.SUBSTITUTION_CURRICULUM_CD ";
            $query .= "                          AND W3.ATTEND_CLASSCD         = W1.ATTEND_CLASSCD ";
            $query .= "                          AND W3.ATTEND_SCHOOL_KIND     = W1.ATTEND_SCHOOL_KIND ";
            $query .= "                          AND W3.ATTEND_CURRICULUM_CD   = W1.ATTEND_CURRICULUM_CD ";
        }

        $query .= "                 WHERE ";
        $query .= "                     W1.REPLACECD = '1' ";
        $query .= "                     AND W1.YEAR = '".CTRL_YEAR."' ";
        $query .= "               ) L1 ON L1.SUBSTITUTION_SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     AND L1.SUBSTITUTION_CLASSCD         = T1.CLASSCD ";
            $query .= "                     AND L1.SUBSTITUTION_SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "                     AND L1.SUBSTITUTION_CURRICULUM_CD   = T1.CURRICULUM_CD ";
        }
        if ($div == "MAIN") {
            $query .= "     INNER JOIN CLASS_MST L2 ON  L2.CLASSCD = SUBSTR(T1.SUBCLASSCD, 1, 2) ";
            $query .= "                             AND VALUE(L2.SPECIALDIV, '0') <> '1' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                             AND L2.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            }
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        if ($div == "MAIN") {
            if($classcd != '00'){
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "     AND T1.CLASSCD          = '".$class_array[0]."' ";
                    $query .= "     AND T1.SCHOOL_KIND      = '".$class_array[1]."' ";
                } else {
                    $query .= "     AND substr(SUBCLASSCD,1,2) = '".$classcd."' ";
                }
            }
            $query .= " GROUP BY ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD, ";
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
            }
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     T1.SUBCLASSNAME ";
            $query .= " ORDER BY ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD, ";
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
            }
            $query .= "     T1.SUBCLASSCD ";
        } else {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     AND CLASSCD         = substr('".$classcd."', 1, 2) ";
                $query .= "     AND SCHOOL_KIND     = substr('".$classcd."', 4, 1) ";
                $query .= "     AND CURRICULUM_CD   = substr('".$classcd."', 6, 1) ";
                $query .= "     AND SUBCLASSCD      = substr('".$classcd."', 8, 6) ";
            } else {
                $query .= "     AND SUBCLASSCD = '".$classcd."' ";
            }
            $query .= " ORDER BY ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD, ";
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
            }
            $query .= "     T1.SUBCLASSCD, L1.ATTEND_SUBCLASSCD ";
        }
        return $query;
    }

    //選択されたリスト
    function getChosenData($model, $cd) {
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        } 
        $query .= "     subclasscd, ";
        $query .= "     subclassname ";
        $query .= " FROM ";
        $query .= "     subclass_mst ";
        $query .= " WHERE ";
        $query .= "     subclasscd = '".$cd."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND   CLASSCD       = '".$model->rightclasscd."' ";
            $query .= " AND   SCHOOL_KIND   = '".$model->school_kind."' ";
            $query .= " AND   CURRICULUM_CD = '".$model->curriculum_Cd."' ";
        }        
        return $query;
    }

    //読替え科目一覧取得
    function selectQuery($model, $subclasscd, $rightclasscd, $school_kind, $curriculum_Cd) {
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.SUBCLASSNAME, ";
        $query .= "     L1.ATTEND_SUBCLASSCD, ";
        $query .= "     L1.SUBSTITUTION_TYPE_FLG ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST T1 ";
        $query .= "     LEFT JOIN ( SELECT ";
        $query .= "                     W1.ATTEND_SUBCLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     W1.ATTEND_CLASSCD, ";
            $query .= "                     W1.ATTEND_SCHOOL_KIND, ";
            $query .= "                     W1.ATTEND_CURRICULUM_CD, ";
        }
        $query .= "                     W1.SUBSTITUTION_TYPE_FLG ";
        $query .= "                 FROM ";
        $query .= "                     SUBCLASS_REPLACE_SUBSTITUTION_DAT W1 ";
        $query .= "                 WHERE ";
        $query .= "                         W1.REPLACECD               = '1' ";
        $query .= "                     AND W1.YEAR                    = '".CTRL_YEAR."' ";
        $query .= "                     AND W1.SUBSTITUTION_SUBCLASSCD = '".$subclasscd."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                          AND W1.SUBSTITUTION_CLASSCD       = '".$rightclasscd."' ";
            $query .= "                          AND W1.SUBSTITUTION_SCHOOL_KIND   = '".$school_kind."' ";
            $query .= "                          AND W1.SUBSTITUTION_CURRICULUM_CD = '".$curriculum_Cd."' ";
        }
        $query .= "               ) L1 ON L1.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     AND L1.ATTEND_CLASSCD         = T1.CLASSCD ";
            $query .= "                     AND L1.ATTEND_SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "                     AND L1.ATTEND_CURRICULUM_CD   = T1.CURRICULUM_CD ";
        }
        $query .= "     INNER JOIN CLASS_MST L2 ON  L2.CLASSCD = SUBSTR(T1.SUBCLASSCD, 1, 2) ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     AND L2.SCHOOL_KIND    = T1.SCHOOL_KIND ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR        = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD != '".substr($subclasscd, 0, 2).'-'.$school_kind.'-'.$curriculum_Cd.'-'.$subclasscd."' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        } else {
            $query .= " AND T1.SUBCLASSCD != '".$subclasscd."' ";
        }
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD ";
        return $query;
    }

    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     SUBCLASS_REPLACE_SUBSTITUTION_DAT ";
        $query .= " WHERE ";
        $query .= "     REPLACECD = '1' ";
        $query .= " AND YEAR = '".CTRL_YEAR."' ";
        $query .= " AND SUBSTITUTION_SUBCLASSCD = '".$model->subclasscd."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND SUBSTITUTION_CLASSCD       = '".$model->rightclasscd."' ";
            $query .= "     AND SUBSTITUTION_SCHOOL_KIND   = '".$model->school_kind."'  ";
            $query .= "     AND SUBSTITUTION_CURRICULUM_CD = '".$model->curriculum_Cd."'  ";
        }

        $db->query($query);

        $selectdata = str_replace(",","','",$model->selectdata);
        $query  = " DELETE FROM ";
        $query .= "     SUBCLASS_REPLACE_SUBSTITUTION_MAJOR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND SUBSTITUTION_SUBCLASSCD = '".$model->subclasscd."' ";
            $query .= "     AND SUBSTITUTION_CLASSCD       = '".$model->rightclasscd."' ";
            $query .= "     AND SUBSTITUTION_SCHOOL_KIND   = '".$model->school_kind."'  ";
            $query .= "     AND SUBSTITUTION_CURRICULUM_CD = '".$model->curriculum_Cd."'  ";
            $query .= "     AND ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD NOT IN ('".$selectdata."') ";
        } else {
            $query .= "     AND SUBSTITUTION_SUBCLASSCD = '".$model->subclasscd."' ";
            $query .= "     AND ATTEND_SUBCLASSCD NOT IN ('".$selectdata."') ";
        }

        $db->query($query);

        $data = array();
        $selectdata = explode(",", $model->selectdata);
        for ($i = 0; $i < get_count($selectdata); $i++) {
            if (!$selectdata[$i]) {
                continue;
            }
            $data["REPLACECD"][TEXT]                = "1";
            $data["YEAR"][TEXT]                     = CTRL_YEAR;
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["SUBSTITUTION_CLASSCD"][TEXT]         = $model->rightclasscd;
                $data["ATTEND_CLASSCD"][TEXT]               = substr($selectdata[$i],0,2);
                $data["SUBSTITUTION_SCHOOL_KIND"][TEXT]     = $model->school_kind;
                $data["ATTEND_SCHOOL_KIND"][TEXT]           = substr($selectdata[$i],3,1);
                $data["SUBSTITUTION_CURRICULUM_CD"][TEXT]   = $model->curriculum_Cd;
                $data["ATTEND_CURRICULUM_CD"][TEXT]         = substr($selectdata[$i],5,1);
                $data["SUBSTITUTION_SUBCLASSCD"][TEXT]      = $model->subclasscd;
                $data["ATTEND_SUBCLASSCD"][TEXT]            = substr($selectdata[$i],7,13);
            } else {
                $data["SUBSTITUTION_SUBCLASSCD"][TEXT]  = $model->subclasscd;
                $data["ATTEND_SUBCLASSCD"][TEXT]        = $selectdata[$i];
            }
            $data["SUBSTITUTION_TYPE_FLG"][TEXT]     = $model->substitution_type_flg;
            $data["STUDYREC_CREATE_FLG"][FUNC]      = "NULL";
            $data["PRINT_FLG1"][TEXT]               = "0";
            $data["PRINT_FLG2"][TEXT]               = "0";
            $data["PRINT_FLG3"][TEXT]               = "0";
            $data["REGISTERCD"][TEXT]               = STAFFCD;
            $data["UPDATED"][FUNC]                  = "SYSDATE()";
            $query = Query::insertSQL($data, "SUBCLASS_REPLACE_SUBSTITUTION_DAT");

            $db->query($query);
        }
        $db->commit();

        Query::dbCheckIn($db);
        return;
    }
}
?>
