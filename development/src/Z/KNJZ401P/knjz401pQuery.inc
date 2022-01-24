<?php

require_once('for_php7.php');

class knjz401pQuery extends Query {

    //年度一覧取得
    function getYearList()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM  ";
        $query .= "     JVIEWNAME_GRADE_YDAT ";
        $query .= " ORDER BY ";
        $query .= "     YEAR DESC ";

        return $query;
    }

    //年度観点一覧取得
    function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     CASE WHEN L1.REMARK1 IS NOT NULL THEN L1.REMARK1 ";
        $query .= "          WHEN L2.REMARK1 IS NOT NULL THEN L2.REMARK1 ";
        $query .= "          WHEN L3.REMARK1 IS NOT NULL THEN L3.REMARK1 ";
        $query .= "     END AS VIEWNAME, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.GRADE || '-' || T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || '-' || T1.VIEWCD || ' ' || VIEWNAME AS LABEL, ";
            $query .= "     T1.GRADE || '-' || T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || '-' || T1.VIEWCD AS VALUE, ";
        } else {
            $query .= "     T1.GRADE || '-' || T1.SUBCLASSCD || '-' || T1.VIEWCD || ' ' || VIEWNAME AS LABEL, ";
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
        $query .= "     T1.VIEWCD ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_MST T1 ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_DETAIL_MST L1 ON L1.GRADE = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                        AND L1.CLASSCD       = T1.CLASSCD ";
            $query .= "                                        AND L1.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "                                        AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "                                            AND L1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "                                            AND L1.VIEWCD = T1.VIEWCD ";
        $query .= "                                            AND L1.VIEW_SEQ = '001' ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_DETAIL_MST L2 ON L2.GRADE = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                        AND L2.CLASSCD       = T1.CLASSCD ";
            $query .= "                                        AND L2.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "                                        AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "                                            AND L2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "                                            AND L2.VIEWCD = T1.VIEWCD ";
        $query .= "                                            AND L2.VIEW_SEQ = '002' ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_DETAIL_MST L3 ON L3.GRADE = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                        AND L3.CLASSCD       = T1.CLASSCD ";
            $query .= "                                        AND L3.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "                                        AND L3.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "                                            AND L3.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "                                            AND L3.VIEWCD = T1.VIEWCD ";
        $query .= "                                            AND L3.VIEW_SEQ = '003' ";
        $query .= "     ,JVIEWNAME_GRADE_YDAT T2 ";
        $query .= " WHERE "; 
        $query .= "     T2.YEAR = '".$model->year."' "; 
        $query .= " AND T1.GRADE = T2.GRADE "; 
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND T1.CLASSCD       = T2.CLASSCD ";
            $query .= " AND T1.SCHOOL_KIND   = T2.SCHOOL_KIND ";
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
        $query .= "     CASE WHEN L1.REMARK1 IS NOT NULL THEN L1.REMARK1 ";
        $query .= "          WHEN L2.REMARK1 IS NOT NULL THEN L2.REMARK1 ";
        $query .= "          WHEN L3.REMARK1 IS NOT NULL THEN L3.REMARK1 ";
        $query .= "     END AS VIEWNAME, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.GRADE || '-' || T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || '-' || T1.VIEWCD || ' ' || VIEWNAME AS LABEL, ";
            $query .= "     T1.GRADE || '-' || T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || '-' || T1.VIEWCD AS VALUE, ";
        } else {
            $query .= "     T1.GRADE || '-' || T1.SUBCLASSCD || '-' || T1.VIEWCD || ' ' || VIEWNAME AS LABEL, ";
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
        $query .= "     T1.VIEWCD ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_MST T1 ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_DETAIL_MST L1 ON L1.GRADE = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                        AND L1.CLASSCD       = T1.CLASSCD ";
            $query .= "                                        AND L1.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "                                        AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "                                            AND L1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "                                            AND L1.VIEWCD = T1.VIEWCD ";
        $query .= "                                            AND L1.VIEW_SEQ = '001' ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_DETAIL_MST L2 ON L2.GRADE = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                        AND L2.CLASSCD       = T1.CLASSCD ";
            $query .= "                                        AND L2.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "                                        AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "                                            AND L2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "                                            AND L2.VIEWCD = T1.VIEWCD ";
        $query .= "                                            AND L2.VIEW_SEQ = '002' ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_DETAIL_MST L3 ON L3.GRADE = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                        AND L3.CLASSCD       = T1.CLASSCD ";
            $query .= "                                        AND L3.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "                                        AND L3.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "                                            AND L3.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "                                            AND L3.VIEWCD = T1.VIEWCD ";
        $query .= "                                            AND L3.VIEW_SEQ = '003' ";
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
