<?php

require_once('for_php7.php');

class knjm251wQuery extends Query {

    //教科一覧取得
    function GetClass($model)
    {
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
        $query .= " ORDER BY ";
        $query .= "     CLASSCD";

        return $query;
    }

    //教科名称取得
    function GetClassName($model)
    {
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
            $query .= "     L1.ATTEND_SUBCLASSCD, ";
            $query .= "     L1.ATTEND_NAME ";
        }
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST T1 ";
        $query .= "     LEFT JOIN ( SELECT ";
        $query .= "                     W1.COMBINED_SUBCLASSCD, ";
        $query .= "                     W1.ATTEND_SUBCLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     W1.COMBINED_CLASSCD, ";
            $query .= "                     W1.COMBINED_SCHOOL_KIND, ";
            $query .= "                     W1.COMBINED_CURRICULUM_CD, ";
            $query .= "                     W1.ATTEND_CLASSCD, ";
            $query .= "                     W1.ATTEND_SCHOOL_KIND, ";
            $query .= "                     W1.ATTEND_CURRICULUM_CD, ";
        }
        $query .= "                     W2.SUBCLASSNAME AS ATTEND_NAME ";
        $query .= "                 FROM ";
        $query .= "                     REP_SUBCLASS_COMBINED_DAT W1 ";
        $query .= "                     LEFT JOIN V_SUBCLASS_MST W2 ON W2.YEAR = W1.YEAR ";
        $query .= "                          AND W2.SUBCLASSCD = W1.ATTEND_SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                          AND W2.CLASSCD         = W1.ATTEND_CLASSCD ";
            $query .= "                          AND W2.SCHOOL_KIND     = W1.ATTEND_SCHOOL_KIND ";
            $query .= "                          AND W2.CURRICULUM_CD   = W1.ATTEND_CURRICULUM_CD ";
        }
        $query .= "                 WHERE ";
        $query .= "                         W1.YEAR = '".CTRL_YEAR."' ";
        $query .= "               ) L1 ON L1.COMBINED_SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     AND L1.COMBINED_CLASSCD         = T1.CLASSCD ";
            $query .= "                     AND L1.COMBINED_SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "                     AND L1.COMBINED_CURRICULUM_CD   = T1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($div == "MAIN") {
            if($classcd != '00'){
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                   $query .= "     AND CLASSCD          = '".$class_array[0]."' ";
                   $query .= "     AND SCHOOL_KIND      = '".$class_array[1]."' ";
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
    function getChosenData($model, $cd)
    {
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
    function selectQuery($model, $subclasscd, $rightclasscd, $school_kind, $curriculum_Cd)
    {
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.SUBCLASSNAME, ";
        $query .= "     L1.ATTEND_SUBCLASSCD ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST T1 ";
        $query .= "     LEFT JOIN ( SELECT ";
        $query .= "                     W1.ATTEND_SUBCLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     W1.ATTEND_CLASSCD, ";
            $query .= "                     W1.ATTEND_SCHOOL_KIND, ";
            $query .= "                     W1.ATTEND_CURRICULUM_CD ";
        }
        $query .= "                 FROM ";
        $query .= "                     REP_SUBCLASS_COMBINED_DAT W1 ";
        $query .= "                 WHERE ";
        $query .= "                         W1.YEAR = '".CTRL_YEAR."' ";
        $query .= "                     AND W1.COMBINED_SUBCLASSCD = '".$subclasscd."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                          AND W1.COMBINED_CLASSCD       = '".$rightclasscd."' ";
            $query .= "                          AND W1.COMBINED_SCHOOL_KIND   = '".$school_kind."' ";
            $query .= "                          AND W1.COMBINED_CURRICULUM_CD = '".$curriculum_Cd."' ";
        }
        $query .= "               ) L1 ON L1.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     AND L1.ATTEND_CLASSCD         = T1.CLASSCD ";
            $query .= "                     AND L1.ATTEND_SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "                     AND L1.ATTEND_CURRICULUM_CD   = T1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD != '".substr($subclasscd, 0, 2).'-'.$school_kind.'-'.$curriculum_Cd.'-'.$subclasscd."' ";
        } else {
            $query .= "     AND T1.SUBCLASSCD != '".$subclasscd."' ";
        }
        $query .= "     AND substr(SUBCLASSCD,1,2) = '".$rightclasscd."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND SCHOOL_KIND   = '".$school_kind."' ";
        }

        return $query;
    }

    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     REP_SUBCLASS_COMBINED_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND COMBINED_SUBCLASSCD = '".$model->subclasscd."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND COMBINED_CLASSCD       = '".$model->rightclasscd."' ";
            $query .= "     AND COMBINED_SCHOOL_KIND   = '".$model->school_kind."'  ";
            $query .= "     AND COMBINED_CURRICULUM_CD = '".$model->curriculum_Cd."'  ";
        }
        
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
                $data["COMBINED_CLASSCD"][TEXT]        = $model->rightclasscd;
                $data["ATTEND_CLASSCD"][TEXT]          = substr($selectdata[$i],0,2);
                $data["COMBINED_SCHOOL_KIND"][TEXT]    = $model->school_kind;
                $data["ATTEND_SCHOOL_KIND"][TEXT]      = substr($selectdata[$i],3,1);
                $data["COMBINED_CURRICULUM_CD"][TEXT]  = $model->curriculum_Cd;
                $data["ATTEND_CURRICULUM_CD"][TEXT]    = substr($selectdata[$i],5,1);
                $data["COMBINED_SUBCLASSCD"][TEXT]     = $model->subclasscd;
                $data["ATTEND_SUBCLASSCD"][TEXT]       = substr($selectdata[$i],7,13);
            } else {
                $data["COMBINED_SUBCLASSCD"][TEXT]  = $model->subclasscd;
                $data["ATTEND_SUBCLASSCD"][TEXT]    = $selectdata[$i];
            }
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "SYSDATE()";
            $query = Query::insertSQL($data, "REP_SUBCLASS_COMBINED_DAT");

            $db->query($query);
        }
        $db->commit();

        Query::dbCheckIn($db);
        return;
    }
}
?>
