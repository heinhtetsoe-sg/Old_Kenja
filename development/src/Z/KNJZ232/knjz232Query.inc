<?php

require_once('for_php7.php');

class knjz232Query extends Query {

    var $attendclasscd = array();

    //学年コンボ
    function GetGrade($year)
    {
        return "SELECT DISTINCT grade FROM schreg_regd_hdat WHERE year = '" .$year ."' ORDER BY grade";
    }

    //教科コンボ
    function GetClass($year, $model)
    {
        $query  = "  SELECT DISTINCT ";
        $query .= "      classcd, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "      SCHOOL_KIND, ";
        }
        $query .= "      classname ";
        $query .= "  FROM ";
        $query .= "      v_class_mst ";
        $query .= "  WHERE ";
        $query .= "      year = '".$year."' ";
        $query .= "  ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "      classcd, ";
            $query .= "      SCHOOL_KIND ";
        } else {
            $query .= "      classcd ";
        }

        return $query;
    }

    //科目リスト
    function GetClasses($year, $grade, $classcd, $model)
    {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $class_array = array();
                $class_array = explode("-", $classcd);
            }
            $query  = " SELECT ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD, ";
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
                $query .= "     T2.GRADING_CLASSCD, ";
                $query .= "     T2.GRADING_SCHOOL_KIND, ";
                $query .= "     T2.GRADING_CURRICULUM_CD, ";
            }
            $query .= "     T3.NOLINK, ";
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     T1.SUBCLASSNAME, ";
            $query .= "     T2.GRADING_SUBCLASSCD, ";
            $query .= "     T2.GRADING_SUBCLASSNAME ";
            $query .= " FROM ";
            $query .= "     v_subclass_mst T1 LEFT OUTER JOIN (SELECT ";
            $query .= "                                         w1.REPLACECD, ";
            $query .= "                                         w1.YEAR, ";
            $query .= "                                         w1.ATTEND_SUBCLASSCD, ";
            $query .= "                                         w1.ANNUAL, ";
            $query .= "                                         w1.GRADING_SUBCLASSCD, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                                     w1.ATTEND_CLASSCD, ";
                $query .= "                                     w1.ATTEND_SCHOOL_KIND, ";
                $query .= "                                     w1.ATTEND_CURRICULUM_CD, ";
                $query .= "                                     w1.GRADING_CLASSCD, ";
                $query .= "                                     w1.GRADING_SCHOOL_KIND, ";
                $query .= "                                     w1.GRADING_CURRICULUM_CD, ";
            }
            $query .= "                                         w2.SUBCLASSNAME AS GRADING_SUBCLASSNAME ";
            $query .= "                                        FROM ";
            $query .= "                                         SUBCLASS_REPLACE_DAT w1, ";
            $query .= "                                         SUBCLASS_MST w2 ";
            $query .= "                                        WHERE ";
            $query .= "                                         w1.GRADING_SUBCLASSCD = w2.SUBCLASSCD ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                               AND   w1.GRADING_CLASSCD       = w2.CLASSCD ";
                $query .= "                               AND   w1.GRADING_SCHOOL_KIND   = w2.SCHOOL_KIND ";
                $query .= "                               AND   w1.GRADING_CURRICULUM_CD = w2.CURRICULUM_CD ";
            }
            $query .= "                                        ) T2 ON T1.YEAR = T2.YEAR ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                                            AND T1.CLASSCD       = T2.ATTEND_CLASSCD  ";
                $query .= "                                            AND T1.SCHOOL_KIND   = T2.ATTEND_SCHOOL_KIND  ";
                $query .= "                                            AND T1.CURRICULUM_CD = T2.ATTEND_CURRICULUM_CD  ";
            }
            $query .= "                                            AND T1.SUBCLASSCD = T2.ATTEND_SUBCLASSCD  ";
            $query .= "                                            AND T2.ANNUAL = '".$grade."' ";
            $query .= "                       LEFT OUTER JOIN (SELECT DISTINCT ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                                         CLASSCD, ";
                $query .= "                                         SCHOOL_KIND, ";
                $query .= "                                         CURRICULUM_CD, ";
            }
            $query .= "                                         SUBCLASSCD, ";
            $query .= "                                         '1' AS NOLINK ";
            $query .= "                                        FROM ";
            $query .= "                                         V_SUBCLASS_MST t1 ";
            $query .= "                                        WHERE ";

       if ($classcd != '00'){
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
               $query .= "                                          CLASSCD          = '".$class_array[0]."' AND ";
               $query .= "                                          SCHOOL_KIND      = '".$class_array[1]."' AND ";
            } else {
               $query .= "                                         substr(SUBCLASSCD,1,2) = '".$classcd."' AND ";
            }
       }

            $query .= "                                         year = '".$year."' AND ";
            $query .= "                                         ( ";//NO001
            $query .= "                                         (   EXISTS (SELECT ";
            $query .= "                                                       'x' ";
            $query .= "                                                     FROM ";
            $query .= "                                                         SUBCLASS_REPLACE_DAT w1 ";
            $query .= "                                                     WHERE ";
            $query .= "                                                          w1.REPLACECD = '2' ";
            $query .= "                                                      AND w1.YEAR = t1.YEAR ";//NO001
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                                                  AND w1.GRADING_CLASSCD         = t1.CLASSCD  ";
                $query .= "                                                  AND w1.GRADING_SCHOOL_KIND     = t1.SCHOOL_KIND  ";
                $query .= "                                                  AND w1.GRADING_CURRICULUM_CD   = t1.CURRICULUM_CD  ";
            }
            $query .= "                                                      AND w1.GRADING_SUBCLASSCD = t1.SUBCLASSCD  ";
            $query .= "                                                      AND w1.ANNUAL = '".$grade."' ";
            $query .= "                                                     ) AND ";
            $query .= "                                             NOT EXISTS (SELECT ";
            $query .= "                                                           'x' ";
            $query .= "                                                         FROM ";
            $query .= "                                                           SUBCLASS_REPLACE_DAT w2 ";
            $query .= "                                                         WHERE ";
            $query .= "                                                             w2.REPLACECD = '2' ";
            $query .= "                                                         AND w2.YEAR = t1.year ";//NO001
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                                                     AND w2.ATTEND_CLASSCD         = t1.CLASSCD  ";
                $query .= "                                                     AND w2.ATTEND_SCHOOL_KIND     = t1.SCHOOL_KIND  ";
                $query .= "                                                     AND w2.ATTEND_CURRICULUM_CD   = t1.CURRICULUM_CD  ";
            }
            $query .= "                                                         AND w2.ATTEND_SUBCLASSCD = t1.SUBCLASSCD ";
            $query .= "                                                         AND w2.ANNUAL = '".$grade."' ";
            $query .= "                                                         )  ";
            $query .= "                                         ) OR ( ";
            $query .= "                                             EXISTS (SELECT ";
            $query .= "                                                       'x' ";
            $query .= "                                                     FROM ";
            $query .= "                                                         SUBCLASS_REPLACE_DAT w3 ";
            $query .= "                                                     WHERE ";
            $query .= "                                                         w3.REPLACECD = '1' ";
            $query .= "                                                     AND w3.YEAR = t1.YEAR ";//NO001
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                                                 AND w3.GRADING_CLASSCD         = t1.CLASSCD  ";
                $query .= "                                                 AND w3.GRADING_SCHOOL_KIND     = t1.SCHOOL_KIND  ";
                $query .= "                                                 AND w3.GRADING_CURRICULUM_CD   = t1.CURRICULUM_CD  ";
            }
            $query .= "                                                     AND w3.GRADING_SUBCLASSCD = t1.SUBCLASSCD ";
            $query .= "                                                     AND w3.ANNUAL = '".$grade."' ";
            $query .= "                                                     ) AND ";
            $query .= "                                                     NOT (EXISTS (SELECT ";
            $query .= "                                                                    'x' ";
            $query .= "                                                                  FROM ";
            $query .= "                                                                     SUBCLASS_REPLACE_DAT w4 ";
            $query .= "                                                                  WHERE ";
            $query .= "                                                                     w4.YEAR = t1.YEAR ";//NO001
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                                                             AND w4.GRADING_CLASSCD         = t1.CLASSCD  ";
                $query .= "                                                             AND w4.GRADING_SCHOOL_KIND     = t1.SCHOOL_KIND  ";
                $query .= "                                                             AND w4.GRADING_CURRICULUM_CD   = t1.CURRICULUM_CD  ";
            }
            $query .= "                                                                 AND w4.GRADING_SUBCLASSCD = t1.SUBCLASSCD ";
            $query .= "                                                                 AND w4.ANNUAL = '".$grade."' ";
            $query .= "                                                                  ) AND ";
            $query .= "                                                          EXISTS (SELECT ";
            $query .= "                                                                   'x' ";
            $query .= "                                                                  FROM ";
            $query .= "                                                                     SUBCLASS_REPLACE_DAT w5 ";
            $query .= "                                                                  WHERE ";
            $query .= "                                                                     w5.YEAR = t1.YEAR ";//NO001
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                                                             AND w5.ATTEND_CLASSCD         = t1.CLASSCD  ";
                $query .= "                                                             AND w5.ATTEND_SCHOOL_KIND     = t1.SCHOOL_KIND  ";
                $query .= "                                                             AND w5.ATTEND_CURRICULUM_CD   = t1.CURRICULUM_CD  ";
            }
            $query .= "                                                                 AND w5.ATTEND_SUBCLASSCD = t1.SUBCLASSCD ";
            $query .= "                                                                 AND w5.ANNUAL = '".$grade."' ";
            $query .= "                                                                  ) ";
            $query .= "                                                          ) ";
            $query .= "                                         ) ";
            $query .= "                                         ) ";//NO001
            $query .= "                                        ) T3 ON T1.SUBCLASSCD = T3.SUBCLASSCD ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                                            AND T1.CLASSCD       = T3.CLASSCD ";
                $query .= "                                            AND T1.SCHOOL_KIND   = T3.SCHOOL_KIND ";
                $query .= "                                            AND T1.CURRICULUM_CD = T3.CURRICULUM_CD ";
            }
            $query .= " WHERE ";

       if($classcd != '00'){
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
               $query .= "      T1.CLASSCD          = '".$class_array[0]."' AND ";
               $query .= "      T1.SCHOOL_KIND      = '".$class_array[1]."' AND ";
            } else {
                $query .= "     substr(T1.SUBCLASSCD,1,2) = '".$classcd."' AND ";
            }
       }

            $query .= "     T1.YEAR = '".$year."' ";
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

    //選択されたリスト
    function getChosenData($cd, $model)
    {
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || '　' || SUBCLASSNAME AS LABEL ";
        } else {
            $query .= "     SUBCLASSCD AS VALUE, ";
            $query .= "     SUBCLASSCD || '　' || SUBCLASSNAME AS LABEL";
        }
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST ";
        $query .= " WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " CLASSCD       = '".substr($model->subclasscd, 0, 2)."' AND ";
            $query .= " SCHOOL_KIND   = '".$model->school_kind."' AND ";
            $query .= " CURRICULUM_CD = '".$model->curriculum_cd."' AND ";
        }
        $query .= "     SUBCLASSCD = '".$cd."' ";

        return $query;
    }

    //読替え科目一覧取得
    function selectQuery($subclasscd, $classcd, $grade, $year, $model)
    {
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     W1.GRADING_CLASSCD || '-' || W1.GRADING_SCHOOL_KIND || '-' || W1.GRADING_CURRICULUM_CD || '-' || W1.GRADING_SUBCLASSCD AS VALUE, ";
            $query .= "     W1.GRADING_CLASSCD || '-' || W1.GRADING_SCHOOL_KIND || '-' || W1.GRADING_CURRICULUM_CD || '-' || W1.GRADING_SUBCLASSCD || '　' || W2.SUBCLASSNAME AS LABEL ";
        } else {
            $query .= "     W1.GRADING_SUBCLASSCD AS VALUE, ";
            $query .= "     W1.GRADING_SUBCLASSCD || '　' || W2.SUBCLASSNAME AS LABEL";
            //$query .= "     W1.grading_subclasscd, ";
            //$query .= "     W1.attend_subclasscd, ";
            //$query .= "     W2.subclasscd, ";
            //$query .= "     W2.subclassname ";
        }
        $query .= " FROM ";
        $query .= "     SUBCLASS_REPLACE_DAT W1, ";
        $query .= "     SUBCLASS_MST W2 ";
        $query .= " WHERE ";
        $query .= "     W1.YEAR = '".$year."' ";
        $query .= " AND W1.ANNUAL = '".$grade."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND W1.ATTEND_CLASSCD       = '".substr($model->subclasscd, 0, 2)."' ";
            $query .= " AND W1.ATTEND_SCHOOL_KIND   = '".$model->school_kind."' ";
            $query .= " AND W1.ATTEND_CURRICULUM_CD = '".$model->curriculum_cd."' ";
            $query .= " AND W1.ATTEND_SUBCLASSCD    = '".$model->subclasscd."' ";
            $query .= " AND W2.CLASSCD          = W1.GRADING_CLASSCD ";
            $query .= " AND W2.SCHOOL_KIND      = W1.GRADING_SCHOOL_KIND ";
            $query .= " AND W2.CURRICULUM_CD    = W1.GRADING_CURRICULUM_CD ";
        } else {
            $query .= " AND W1.ATTEND_SUBCLASSCD = '".$subclasscd."' ";
        }
        $query .= " AND W2.SUBCLASSCD = W1.GRADING_SUBCLASSCD ";

        return $query;
    }

    //科目一覧取得
    function selectClassQuery($year, $classcd, $subclasscd, $grade, $model)
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
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || '　' || T1.SUBCLASSNAME AS LABEL ";
        } else {
            $query .= "     T1.SUBCLASSCD AS VALUE, ";
            $query .= "     T1.SUBCLASSCD || '　' || T1.SUBCLASSNAME AS LABEL";
        }
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST T1 ";
        $query .= " WHERE ";
        $query .= "     not exists(SELECT ";
        $query .= "                     T2.GRADING_SUBCLASSCD ";
        $query .= "                 FROM ";
        $query .= "                     SUBCLASS_REPLACE_DAT T2 ";
        $query .= "                 WHERE ";
        $query .= "                     T2.YEAR = '".$year."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                 AND T2.ATTEND_CLASSCD       = '".substr($model->subclasscd, 0, 2)."' ";
            $query .= "                 AND T2.ATTEND_SCHOOL_KIND   = '".$model->school_kind."' ";
            $query .= "                 AND T2.ATTEND_CURRICULUM_CD = '".$model->curriculum_cd."' ";
            $query .= "                 AND T2.ATTEND_SUBCLASSCD    = '".$model->subclasscd."' ";
            $query .= "                 AND T2.GRADING_CLASSCD          = T1.CLASSCD ";
            $query .= "                 AND T2.GRADING_SCHOOL_KIND      = T1.SCHOOL_KIND ";
            $query .= "                 AND T2.GRADING_CURRICULUM_CD    = T1.CURRICULUM_CD ";
        } else {
            $query .= "                 AND T2.ATTEND_SUBCLASSCD    = '".$subclasscd."' ";
        }
        $query .= "                 AND T2.ANNUAL               = '".$grade."' ";
        $query .= "                 AND T2.GRADING_SUBCLASSCD   = T1.SUBCLASSCD ";
        $query .= "                 ) AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD          = '".$class_array[0]."' ";
            $query .= " AND T1.SCHOOL_KIND      = '".$class_array[1]."' ";
        } else {
            $query .= "     substr(T1.SUBCLASSCD,1,2) = '".$classcd."' ";
        }
        $query .= " AND T1.YEAR = '".$year."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //更新時の重複チェック
    function IsExisting($year, $grade, $selectdata, $subclasscd, $org_data, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            //cntselectdataのSUBCLASSCD分割
            $tmp_selectdata = array(); //分割したselectdataを入れるための一時的に使う配列
            $array_selectdata = explode(",", $selectdata); //selectdataを分割するために一度配列にする
            for ($i = 0; $i < get_count($array_selectdata); $i++) {
                $tmp_selectdata[$i] = substr($array_selectdata[$i], 7, 6);
            }
            $substr_selectdata = implode(",",$tmp_selectdata);
            $cntselectdata = explode(",", $substr_selectdata);
            
            //orgdataのSUBCLASSCD分割
            $substr_org_data = array();
            for ($i = 0; $i < get_count($org_data); $i++) {
                $substr_org_data[$i] = substr($org_data[$i], 7, 6);
            }
            $orgdata = implode(",",$substr_org_data);
        } else {
            $cntselectdata = explode(",", $selectdata);
            $orgdata = implode(",",$org_data);
        }
        $db = Query::dbCheckOut();

        if(get_count($cntselectdata) == 1) {
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     SUBCLASS_REPLACE_DAT t1 ";
            $query .= " WHERE ";
            $query .= "     REPLACECD         ='2' ";
            $query .= " AND YEAR              = '".$year."' ";
            $query .= " AND ANNUAL            = '".$grade."' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                 AND ATTEND_SUBCLASSCD       <> '".$model->subclasscd."' ";
                $query .= "                 AND GRADING_CLASSCD          = '".substr($selectdata, 0, 2)."' ";
                $query .= "                 AND GRADING_SCHOOL_KIND      = '".substr($selectdata, 3, 1)."' ";
                $query .= "                 AND GRADING_CURRICULUM_CD    = '".substr($selectdata, 5, 1)."' ";
                $query .= "                 AND GRADING_SUBCLASSCD       = '".substr($selectdata, 7, 6)."' ";
            } else {
                $query .= " AND ATTEND_SUBCLASSCD <> '".$subclasscd."' ";
                $query .= " AND GRADING_SUBCLASSCD = '".$selectdata."' ";
            }
        } else {
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     SUBCLASS_REPLACE_DAT t1 ";
            $query .= " WHERE ";
            $query .= "     YEAR              = '".$year."' ";
            $query .= " AND ANNUAL            = '".$grade."' ";
            $query .= " AND GRADING_SUBCLASSCD NOT IN ('".str_replace(",","','",$orgdata)."') ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= " AND GRADING_SUBCLASSCD IN ('".str_replace(",","','",$substr_selectdata)."') ";
            } else {
                $query .= " AND GRADING_SUBCLASSCD IN ('".str_replace(",","','",$selectdata)."') ";
            }
        }
        $cntgradingcd = $db->getOne($query);

        Query::dbCheckIn($db);
        return $cntgradingcd;
    }

    //読替え元になっていないかチェック
    function ExistGradingcd($year, $grade, $subclasscd)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT DISTINCT ";
        $query .= "     ATTEND_SUBCLASSCD ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_REPLACE_DAT ";
        $query .= " WHERE ";
        $query .= "     ATTEND_SUBCLASSCD <> '".$subclasscd."' ";
        $query .= " AND GRADING_SUBCLASSCD like '%' ";
        $query .= " AND YEAR   = '".$year."' ";
        $query .= " AND ANNUAL = '".$grade."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND ATTEND_CLASSCD        <> GRADING_CLASSCD ";
            $query .= " AND ATTEND_SCHOOL_KIND    <> GRADING_SCHOOL_KIND ";
            $query .= " AND ATTEND_CURRICULUM_CD  <> GRADING_CURRICULUM_CD ";
        }
        $query .= " AND ATTEND_SUBCLASSCD <> GRADING_SUBCLASSCD ";
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     ATTEND_CLASSCD, ";
            $query .= "     ATTEND_SCHOOL_KIND, ";
            $query .= "     ATTEND_CURRICULUM_CD, ";
        }
        $query .= "     ATTEND_SUBCLASSCD ";
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $existgradingcd[] = $row["ATTEND_SUBCLASSCD"];
        }

        Query::dbCheckIn($db);
        return $existgradingcd;
    }

    function &getUpdateQuery($model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            if(strlen($model->classcd) > 2) {
                $model->classcd = substr($model->classcd, 0, 2);
            }
        }
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
            $query  = " DELETE FROM ";
            $query .= "     SUBCLASS_REPLACE_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' ";
            $query .= " AND ANNUAL = '".$model->grade."' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= " AND ATTEND_CLASSCD          = '".substr($model->subclasscd, 0, 2)."' ";
                $query .= " AND ATTEND_SCHOOL_KIND      = '".$model->school_kind."' ";
                $query .= " AND ATTEND_CURRICULUM_CD    = '".$model->curriculum_cd."' ";
                $query .= " AND ATTEND_SUBCLASSCD       = '".$model->subclasscd."' ";
            } else {
                $query .= " AND ATTEND_SUBCLASSCD = '".$model->subclasscd."' ";
            }

            $db->query($query);

        if($model->selectdata != "") {
            $selectdata = explode(",", $model->selectdata);
            if(get_count($selectdata) == 1) {
                $query  = " INSERT INTO SUBCLASS_REPLACE_DAT  ";
                $query .= "             (REPLACECD, ";
                $query .= "              YEAR, ";
                $query .= "              ANNUAL, ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "     ATTEND_CLASSCD, ";
                    $query .= "     ATTEND_SCHOOL_KIND, ";
                    $query .= "     ATTEND_CURRICULUM_CD, ";
                }
                $query .= "              ATTEND_SUBCLASSCD, ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "     GRADING_CLASSCD, ";
                    $query .= "     GRADING_SCHOOL_KIND, ";
                    $query .= "     GRADING_CURRICULUM_CD, ";
                }
                $query .= "              GRADING_SUBCLASSCD, ";
                $query .= "              REGISTERCD, ";
                $query .= "              UPDATED )";
                $query .= " VALUES ";
                $query .= "      ('1', ";
                $query .= "       '".CTRL_YEAR."', ";
                $query .= "       '".$model->grade."', ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "       '".substr($model->subclasscd, 0, 2)."', ";
                    $query .= "       '".$model->school_kind."', ";
                    $query .= "       '".$model->curriculum_cd."', ";
                    $query .= "       '".$model->subclasscd."', ";
                    $query .= "       '".substr($model->selectdata, 0, 2)."', ";
                    $query .= "       '".substr($model->selectdata, 3, 1)."', ";
                    $query .= "       '".substr($model->selectdata, 5, 1)."', ";
                    $query .= "       '".substr($model->selectdata, 7, 6)."', ";
                } else {
                    $query .= "       '".$model->subclasscd."', ";
                    $query .= "       '".$model->selectdata."', ";
                }
                $query .= "       '".STAFFCD."', ";
                $query .= "       sysdate() )";
            } else {
                $query  = " INSERT INTO SUBCLASS_REPLACE_DAT  ";
                $query .= "             (REPLACECD, ";
                $query .= "              YEAR, ";
                $query .= "              ANNUAL, ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "     ATTEND_CLASSCD, ";
                    $query .= "     ATTEND_SCHOOL_KIND, ";
                    $query .= "     ATTEND_CURRICULUM_CD, ";
                }
                $query .= "              ATTEND_SUBCLASSCD, ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "     GRADING_CLASSCD, ";
                    $query .= "     GRADING_SCHOOL_KIND, ";
                    $query .= "     GRADING_CURRICULUM_CD, ";
                }
                $query .= "              GRADING_SUBCLASSCD, ";
                $query .= "              REGISTERCD, ";
                $query .= "              UPDATED )";
                $query .= " VALUES ";

                for ($i=0; $i<get_count($selectdata); $i++) { //---------->読替え科目が複数時
                    $sp = ($i == get_count($selectdata)-1)? "" : "," ;

                    $query .= "      ('2', ";
                    $query .= "       '".CTRL_YEAR."', ";
                    $query .= "       '".$model->grade."', ";
                    //教育課程対応
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $query .= "       '".substr($model->subclasscd, 0, 2)."', ";
                        $query .= "       '".$model->school_kind."', ";
                        $query .= "       '".$model->curriculum_cd."', ";
                        $query .= "       '".$model->subclasscd."', ";
                        $query .= "       '".substr($selectdata[$i], 0, 2)."', ";
                        $query .= "       '".substr($selectdata[$i], 3, 1)."', ";
                        $query .= "       '".substr($selectdata[$i], 5, 1)."', ";
                        $query .= "       '".substr($selectdata[$i], 7, 6)."', ";
                    } else {
                        $query .= "       '".$model->subclasscd."', ";
                        $query .= "       '" .$selectdata[$i]."', ";
                    }
                    $query .= "       '".STAFFCD."', ";
                    $query .= "       sysdate() )" .$sp;
                }
            }
            $db->query($query);
        }

        $db->commit();

        Query::dbCheckIn($db);
        return;
    }
}
?>
