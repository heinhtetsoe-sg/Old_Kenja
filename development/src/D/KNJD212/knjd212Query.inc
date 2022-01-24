<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjd212Query.inc 56580 2017-10-22 12:35:29Z maeshiro $

class knjd212query extends Query {
    
    //学年の取得
    function getGradeQuery(){

        $query  = " SELECT ";
        $query .= "    GRADE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " GROUP BY GRADE ";

        return $query;
    }

    //学期名の取得
    function getSemesterNameQuery($model){

        $query  = " SELECT ";
        $query .= "    NAMECD2, ";
        $query .= "    NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND NAMECD1 ='Z004' ";
        $query .= "   AND NAMECD2 !='0303' ";       //0303：３学期平均 処理対象外
        $query .= "   AND NAMECD2 !='8013' ";       //8013：学年平均(総学用) 処理対象外
        $query .= "   AND NAMECD2 IN (SELECT control_code FROM admin_control_dat WHERE year = '".CTRL_YEAR."' AND ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "             control_flg = '1') ";

        return $query;
    }

    //科目名の取得
    function getSubclassNameQuery($model){

        $query  = " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || '　' || T2.SUBCLASSNAME AS LABEL ";
        } else {
            $query .= "     T1.SUBCLASSCD AS VALUE, ";
            $query .= "     T1.SUBCLASSCD || '　' || T2.SUBCLASSNAME VS LABEL";
        }
        $query .= " FROM ";
        $query .= "     TYPE_GROUP_MST T1, ";
        $query .= "     V_SUBCLASS_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.GRADE = '".$model->field["GRADE"]."' AND ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD = T2.CLASSCD AND ";
            $query .= "    T1.SCHOOL_KIND = T2.SCHOOL_KIND AND ";
            $query .= "    T1.CURRICULUM_CD = T2.CURRICULUM_CD AND ";
        }
        $query .= "     T1.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= " ORDER BY VALUE ";
        return $query;
    }

    //類型グループ名の取得
    function getTypeGroupNameQuery($model, $flg = ""){
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        if ($flg == "all") {
        $query .= " SELECT ";
        $query .= "     YEAR || TYPE_GROUP_CD AS TYPE_GROUP_CD , ";
        $query .= "     TYPE_GROUP_NAME ";
        $query .= " FROM ";
        $query .= "     TYPE_GROUP_MST ";
        } else {
        $query .= " SELECT ";
        $query .= "     TYPE_GROUP_CD, ";
        $query .= "     TYPE_GROUP_NAME ";
        $query .= " FROM ";
        $query .= "     TYPE_GROUP_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     GRADE = '".$model->field["GRADE"]."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD         = '".$subclass_array[0]."' AND ";
            $query .= "     SCHOOL_KIND     = '".$subclass_array[1]."' AND ";
            $query .= "     CURRICULUM_CD   = '".$subclass_array[2]."' AND ";
            $query .= "     SUBCLASSCD      = '".$subclass_array[3]."' ";
        } else {
            $query .= "     SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= " ORDER BY TYPE_GROUP_CD ";
        }

        return $query;
    }

    //類型グループ平均算出処理済リストを取得
    function getTypeGroupHrDatQuery(){

        $query  = " SELECT YEAR,TYPE_GROUP_CD,GRADE,UPDATED,JUDGE_SEMESTER ";
        $query .= "   FROM( ";
        $query .= "   SELECT YEAR,TYPE_GROUP_CD,GRADE,MAX(SEM1_INTER_REC_DATE) AS UPDATED,'0101' AS JUDGE_SEMESTER FROM TYPE_GROUP_MST WHERE SEM1_INTER_REC_CNT IS NOT NULL GROUP BY YEAR,TYPE_GROUP_CD,GRADE ";
        $query .= "   UNION ";
        $query .= "   SELECT YEAR,TYPE_GROUP_CD,GRADE,MAX(SEM1_TERM_REC_DATE)  AS UPDATED,'0102' AS JUDGE_SEMESTER FROM TYPE_GROUP_MST WHERE SEM1_TERM_REC_CNT  IS NOT NULL GROUP BY YEAR,TYPE_GROUP_CD,GRADE ";
        $query .= "   UNION ";
        $query .= "   SELECT YEAR,TYPE_GROUP_CD,GRADE,MAX(SEM1_REC_DATE)       AS UPDATED,'0103' AS JUDGE_SEMESTER FROM TYPE_GROUP_MST WHERE SEM1_REC_CNT       IS NOT NULL GROUP BY YEAR,TYPE_GROUP_CD,GRADE ";
        $query .= "   UNION ";
        $query .= "   SELECT YEAR,TYPE_GROUP_CD,GRADE,MAX(SEM2_INTER_REC_DATE) AS UPDATED,'0201' AS JUDGE_SEMESTER FROM TYPE_GROUP_MST WHERE SEM2_INTER_REC_CNT IS NOT NULL GROUP BY YEAR,TYPE_GROUP_CD,GRADE ";
        $query .= "   UNION ";
        $query .= "   SELECT YEAR,TYPE_GROUP_CD,GRADE,MAX(SEM2_TERM_REC_DATE)  AS UPDATED,'0202' AS JUDGE_SEMESTER FROM TYPE_GROUP_MST WHERE SEM2_TERM_REC_CNT  IS NOT NULL GROUP BY YEAR,TYPE_GROUP_CD,GRADE ";
        $query .= "   UNION ";
        $query .= "   SELECT YEAR,TYPE_GROUP_CD,GRADE,MAX(SEM2_REC_DATE)       AS UPDATED,'0203' AS JUDGE_SEMESTER FROM TYPE_GROUP_MST WHERE SEM2_REC_CNT       IS NOT NULL GROUP BY YEAR,TYPE_GROUP_CD,GRADE ";
        $query .= "   UNION ";
        $query .= "   SELECT YEAR,TYPE_GROUP_CD,GRADE,MAX(SEM3_TERM_REC_DATE)  AS UPDATED,'0302' AS JUDGE_SEMESTER FROM TYPE_GROUP_MST WHERE SEM3_TERM_REC_CNT  IS NOT NULL GROUP BY YEAR,TYPE_GROUP_CD,GRADE ";
        $query .= "   UNION ";
        $query .= "   SELECT YEAR,TYPE_GROUP_CD,GRADE,MAX(GRADE_RECORD_DATE)   AS UPDATED,'8003' AS JUDGE_SEMESTER FROM TYPE_GROUP_MST WHERE GRADE_RECORD_CNT   IS NOT NULL GROUP BY YEAR,TYPE_GROUP_CD,GRADE ";
        $query .= ")T1 ";
        $query .= "WHERE UPDATED IS NOT NULL ";
        $query .= "ORDER BY UPDATED DESC";

        return $query;
    }

    //類型グループHRクラス毎の合計値のクリア
    function getTGHDClearQuery($model,$ITEM){

        $query  = " UPDATE TYPE_GROUP_HR_DAT AS T1 ";
        $query .= "    SET ".$ITEM[$model->field["SEMESTER"]]."_SUM = Null ,";
        $query .= "        ".$ITEM[$model->field["SEMESTER"]]."_CNT = Null ";
        $query .= "  WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND TYPE_GROUP_CD IN ('" .implode($model->selectdata, "','") ."') ";
        $query .= "    AND GRADE='".$model->field["GRADE"]."' ";

        return $query;
    }


    //類型グループHRクラス毎の合計値の算出
    //■変更　読替先の成績は、読替元の成績の平均値ではなくそのままセット
    function getTGHDCalQuery($db,$model,$ITEM,$SEM)
    {
        if($SEM[$model->field["SEMESTER"]] == '3'){
            //選択された月の学期開始日
            $sdate = $db->getOne("SELECT sdate FROM semester_mst WHERE year = '".CTRL_YEAR."' AND semester = '".$SEM[$model->field["SEMESTER"]]."'");
            //選択された月の学期終了日
            $edate = $db->getOne("SELECT edate FROM semester_mst WHERE year = '".CTRL_YEAR."' AND semester = '".$SEM[$model->field["SEMESTER"]]."'");
        }

        $setRecFlg = $ITEM[$model->field["SEMESTER"]]."_FLG";
        $setRecDi  = $ITEM[$model->field["SEMESTER"]]."_DI";
        //学期成績用(0103→SEM1_TERM_REC_FLG)
        //学期成績用(0203→SEM2_TERM_REC_FLG)
        //学期成績用(8003→固定でSEM3_TERM_REC_FLG)
        $setGakkiYouRecFlg = "SEM".$SEM[$model->field["SEMESTER"]]."_TERM_REC_FLG";
        $setGakkiYouRecDi  = "SEM".$SEM[$model->field["SEMESTER"]]."_TERM_REC_DI";
        $setJouken = " AND ";

        $query  = " SELECT ";
        $query .= "     w1.YEAR,w3.TYPE_GROUP_CD,w2.GRADE,w2.HR_CLASS,coalesce(SUM(w1.KIN_REC),0) as SUM,coalesce(COUNT(w1.KIN_REC_CNT),0) as CNT";
        $query .= " FROM";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    (SELECT YEAR,SCHREGNO,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD, ";
        } else {
            $query .= "    (SELECT YEAR,SCHREGNO,SUBCLASSCD, ";
        }
        if ($model->field["SEMESTER"] != "8003" && "3" != substr($model->field["SEMESTER"], 3)) {
            $query .= "     CASE WHEN {$setRecFlg} = '1' OR {$setRecFlg} = '2' OR ";
            $query .= "               {$setRecDi} = 'KK' OR {$setRecDi} = 'KS' ";
            $query .= "          THEN 0 ";
            $query .= "          ELSE ".$ITEM[$model->field["SEMESTER"]]." ";
            $query .= "     END as KIN_REC, ";
            $query .= "     CASE WHEN {$setRecFlg} = '1' OR {$setRecFlg} = '2' OR ";
            $query .= "               {$setRecDi} = 'KK' OR {$setRecDi} = 'KS' ";
            $query .= "          THEN NULL ";
            $query .= "          ELSE ".$ITEM[$model->field["SEMESTER"]]." ";
            $query .= "     END as KIN_REC_CNT ";
        } else if ($model->field["SEMESTER"] != "8003" && "3" == substr($model->field["SEMESTER"], 3)) {
            $query .= "     CASE WHEN {$setRecFlg} = '2' OR ";
            $query .= "               ({$setGakkiYouRecFlg} = '1' OR {$setGakkiYouRecFlg} = '2' OR ";
            $query .= "                {$setGakkiYouRecDi} = 'KK' OR {$setGakkiYouRecDi} = 'KS') ";
            $query .= "          THEN 0 ";
            $query .= "          ELSE ".$ITEM[$model->field["SEMESTER"]]." ";
            $query .= "     END as KIN_REC, ";
            $query .= "     CASE WHEN {$setRecFlg} = '2' OR ";
            $query .= "               ({$setGakkiYouRecFlg} = '1' OR {$setGakkiYouRecFlg} = '2' OR ";
            $query .= "                {$setGakkiYouRecDi} = 'KK' OR {$setGakkiYouRecDi} = 'KS') ";
            $query .= "          THEN NULL ";
            $query .= "          ELSE ".$ITEM[$model->field["SEMESTER"]]." ";
            $query .= "     END as KIN_REC_CNT ";
        } else {
            $query .= "     CASE WHEN SEM3_TERM_REC_FLG = '1' OR SEM3_TERM_REC_FLG = '2' OR ";
            $query .= "               SEM3_TERM_REC_DI = 'KK' OR SEM3_TERM_REC_DI = 'KS' ";
            $query .= "          THEN 0 ";
            $query .= "          ELSE ".$ITEM[$model->field["SEMESTER"]]." ";
            $query .= "     END as KIN_REC, ";
            $query .= "     CASE WHEN SEM3_TERM_REC_FLG = '1' OR SEM3_TERM_REC_FLG = '2' OR ";
            $query .= "               SEM3_TERM_REC_DI = 'KK' OR SEM3_TERM_REC_DI = 'KS' ";
            $query .= "          THEN NULL ";
            $query .= "          ELSE ".$ITEM[$model->field["SEMESTER"]]." ";
            $query .= "     END as KIN_REC_CNT ";
        }
        $query .= "       FROM KIN_RECORD_DAT";
        $query .= "      WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "    ) w1,";
        $query .= "    (SELECT SCHREGNO,GRADE,HR_CLASS";
        $query .= "       FROM SCHREG_REGD_DAT";
        $query .= "      WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "        AND SEMESTER='".$SEM[$model->field["SEMESTER"]]."'";
        $query .= "        AND GRADE='".$model->field["GRADE"]."' ";
        $query .= "        AND SCHREGNO NOT IN ( ";
        $query .= "                             SELECT schregno  ";
        $query .= "                               FROM schreg_base_mst ";
        $query .= "                              WHERE grd_div IN ('2', '3') "; //退学、転学
        $query .= "                                AND grd_date <= DATE('".$model->ctrl_date."') ";
        $query .= "                              )  ";
        $query .= "        AND SCHREGNO NOT IN ( ";
        $query .= "                             SELECT schregno ";
        $query .= "                               FROM schreg_transfer_dat ";
        $query .= "                              WHERE transfercd = '2' ";  //休学
        $query .= "                                AND DATE('".$model->ctrl_date."') BETWEEN transfer_sdate AND transfer_edate ";
        $query .= "                              )  ";
        $query .= "        AND SCHREGNO NOT IN ( ";
        $query .= "                             SELECT T1.schregno ";
        $query .= "                               FROM (select * from schreg_transfer_dat ";
        if($SEM[$model->field["SEMESTER"]] == '3'){
            $query .= "                               WHERE SCHREGNO NOT IN (SELECT schregno FROM schreg_transfer_dat WHERE transfercd='1' AND transfer_sdate >= DATE('".$sdate."') AND transfer_edate <= DATE('".$edate."'))";
        }
        $query .= "                                    ) T1";
        $query .= "                              WHERE T1.transfercd = '1' ";  //留学
        $query .= "                                AND DATE('".$model->ctrl_date."') BETWEEN T1.transfer_sdate AND T1.transfer_edate ";
        $query .= "                            )  ";
        $query .= "    ) w2,";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    (SELECT TYPE_GROUP_CD,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD";
        } else {
            $query .= "    (SELECT TYPE_GROUP_CD,SUBCLASSCD";
        }
        $query .= "       FROM TYPE_GROUP_MST";
        $query .= "      WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "        AND TYPE_GROUP_CD IN ('" .implode($model->selectdata, "','") ."') ";
        $query .= "    )w3,";
        $query .= "    (SELECT TYPE_GROUP_CD,HR_CLASS";
        $query .= "       FROM TYPE_GROUP_HR_DAT";
        $query .= "      WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "        AND GRADE='".$model->field["GRADE"]."' ";
        $query .= "    )w4";
        $query .= " WHERE w1.SCHREGNO      = w2.SCHREGNO ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   AND w1.CLASSCD        = w3.CLASSCD ";
            $query .= "   AND w1.SCHOOL_KIND    = w3.SCHOOL_KIND ";
            $query .= "   AND w1.CURRICULUM_CD  = w3.CURRICULUM_CD ";
        }
        $query .= "   AND w1.SUBCLASSCD    = w3.SUBCLASSCD ";
        $query .= "   AND w3.TYPE_GROUP_CD = w4.TYPE_GROUP_CD ";
        $query .= "   AND w2.HR_CLASS      = w4.HR_CLASS";
        $query .= " GROUP BY";
        $query .= "    w1.YEAR,";
        $query .= "    w3.TYPE_GROUP_CD,";
        $query .= "    w2.GRADE,";
        $query .= "    w2.HR_CLASS";

        return $query;
    }

    //類型グループHRクラス毎の合計値の更新
    function getTGHDUpQuery($model,$ITEM,$row){

        $query  = " UPDATE TYPE_GROUP_HR_DAT ";
        $query .= " SET(".$ITEM[$model->field["SEMESTER"]]."_SUM,".$ITEM[$model->field["SEMESTER"]]."_CNT) ";
        $query .= " =  (".$row["SUM"].",".$row["CNT"].") ";
        $query .= " WHERE YEAR = '".$row["YEAR"]."'";
        $query .= "   AND TYPE_GROUP_CD = '".$row["TYPE_GROUP_CD"]."'";
        $query .= "   AND GRADE = '".$row["GRADE"]."'";
        $query .= "   AND HR_CLASS = '".$row["HR_CLASS"]."'";

        return $query;
    }

    //類型グループ毎の合計値のクリア
    function getTGMClearQuery($model,$ITEM){

        $query  = " UPDATE TYPE_GROUP_MST ";
        $query .= "    SET ".$ITEM[$model->field["SEMESTER"]]."_SUM = Null ,".$ITEM[$model->field["SEMESTER"]]."_CNT = Null ";
        $query .= "  WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND TYPE_GROUP_CD IN ('" .implode($model->selectdata, "','") ."') ";
        $query .= "    AND GRADE='".$model->field["GRADE"]."' ";

        return $query;
    }

    //類型グループ毎の合計値の算出
    function getTGMCalQuery($model,$ITEM){

        $query .= " SELECT YEAR,TYPE_GROUP_CD,coalesce(SUM(".$ITEM[$model->field["SEMESTER"]]."_SUM),0) as SUM, coalesce(SUM(".$ITEM[$model->field["SEMESTER"]]."_CNT),0) as CNT";
        $query .= "   FROM TYPE_GROUP_HR_DAT";
        $query .= "  WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND TYPE_GROUP_CD IN ('" .implode($model->selectdata, "','") ."') ";
        $query .= "    AND GRADE='".$model->field["GRADE"]."' ";
        $query .= "  GROUP BY YEAR,TYPE_GROUP_CD ";

        return $query;
    }

    //類型グループ毎の合計値の更新
    function getTGMUpQuery($model,$ITEM,$row){

        $query  = " UPDATE TYPE_GROUP_MST ";
        $query .= " SET(".$ITEM[$model->field["SEMESTER"]]."_SUM,".$ITEM[$model->field["SEMESTER"]]."_CNT) ";
        $query .= " =  (".$row["SUM"].",".$row["CNT"].") ";
        $query .= " WHERE YEAR = '".$row["YEAR"]."'";
        $query .= "   AND TYPE_GROUP_CD = '".$row["TYPE_GROUP_CD"]."'";

        return $query;
    }

    ///類型グループ毎の類型評定のクリア
    function getTASSCDClearQuery($model,$ITEM){

        $query  = " UPDATE TYPE_GROUP_MST ";
        $query .= " SET(".$ITEM[$model->field["SEMESTER"]]."_TYPE_ASSES_CD,".$ITEM[$model->field["SEMESTER"]]."_DATE) = (Null,Null) ";
        $query .= "  WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND TYPE_GROUP_CD IN ('" .implode($model->selectdata, "','") ."') ";
        $query .= "    AND GRADE='".$model->field["GRADE"]."' ";

        return $query;
    }

    //類型グループ毎の類型評定の判定
    function getTASSCDCalQuery($model,$ITEM){

        $query .= " SELECT W1.YEAR,W2.TYPE_GROUP_CD,W1.TYPE_ASSES_CD ";
        $query .= "   FROM TYPE_ASSES_HDAT AS W1,TYPE_GROUP_MST AS W2";
        $query .= "  WHERE W2.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND W2.TYPE_GROUP_CD IN ('" .implode($model->selectdata, "','") ."') ";
        $query .= "    AND W2.GRADE = '".$model->field["GRADE"]."' ";
        $query .= "    AND W1.YEAR = W2.YEAR ";
        $query .= "    AND ROUND( DOUBLE(W2.".$ITEM[$model->field["SEMESTER"]]."_SUM)/DOUBLE(W2.".$ITEM[$model->field["SEMESTER"]]."_CNT),0) ";
        $query .= "    BETWEEN W1.TYPE_GROUP_AVE_LOW AND W1.TYPE_GROUP_AVE_HIGH ";
        $query .= "    AND W2.".$ITEM[$model->field["SEMESTER"]]."_CNT > 0 ";

        return $query;
    }

    //類型グループ毎の類型評定の更新
    function getTASSCDUpQuery($model,$ITEM,$row){

        $query  = " UPDATE TYPE_GROUP_MST ";
        $query .= " SET(".$ITEM[$model->field["SEMESTER"]]."_TYPE_ASSES_CD,".$ITEM[$model->field["SEMESTER"]]."_DATE) ";
        $query .= " =  ('".$row["TYPE_ASSES_CD"]."',SYSDATE()) ";
        $query .= " WHERE YEAR = '".$row["YEAR"]."'";
        $query .= "   AND TYPE_GROUP_CD = '".$row["TYPE_GROUP_CD"]."'";

        return $query;
    }

    //固定評定処理
    function getFixValueSetQuery($model,$ITEM){

        $query  = " UPDATE TYPE_GROUP_MST ";
        $query .= " SET(".$ITEM[$model->field["SEMESTER"]]."_TYPE_ASSES_CD,".$ITEM[$model->field["SEMESTER"]]."_DATE) ";
        $query .= " =  (TYPE_ASSES_CD,SYSDATE()) ";
        $query .= " WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND TYPE_GROUP_CD IN ('" .implode($model->selectdata, "','") ."') ";
        $query .= "   AND GRADE = '".$model->field["GRADE"]."' ";
        $query .= "   AND TYPE_ASSES_CD IN('A','B','C') ";

        return $query;
    }

    //生徒毎の類型評定更新用データ取得
    function getSTDCalQuery($model,$ITEM){

        $query  = " SELECT T1.YEAR, ";
        $query .= "        T1.TYPE_GROUP_CD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "        T1.SUBCLASSCD, ";
        $query .= "        T2.GRADE, ";
        $query .= "        T2.HR_CLASS, ";
        $query .= "        T1.".$ITEM[$model->field["SEMESTER"]]."_TYPE_ASSES_CD AS TYPE_ASSES_CD";
        $query .= "   FROM TYPE_GROUP_MST AS T1, TYPE_GROUP_HR_DAT AS T2 ";
        $query .= "  WHERE T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND T1.YEAR = T2.YEAR ";
        $query .= "    AND T1.TYPE_GROUP_CD IN ('" .implode($model->selectdata, "','") ."') ";
        $query .= "    AND T1.TYPE_GROUP_CD = T2.TYPE_GROUP_CD ";
        $query .= "    AND T2.GRADE = '".$model->field["GRADE"]."' ";

        return $query;
    }

    //生徒毎の類型評定の更新
    function getSTDUpQuery($model,$ITEM,$SEM,$row){

        $query  = " UPDATE KIN_RECORD_DAT ";
        if ($row["TYPE_ASSES_CD"] != "") {
            $query .= " SET (JUDGE_PATTERN) ";
            $query .= " =   ('".$row["TYPE_ASSES_CD"]."') ";
        } else {
            $query .= " SET JUDGE_PATTERN = Null ";
        }
        $query .= " WHERE YEAR = '".$row["YEAR"]."'";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   AND CLASSCD       = '".$row["CLASSCD"]."'";
            $query .= "   AND SCHOOL_KIND   = '".$row["SCHOOL_KIND"]."'";
            $query .= "   AND CURRICULUM_CD = '".$row["CURRICULUM_CD"]."'";
        }
        $query .= "   AND SUBCLASSCD = '".$row["SUBCLASSCD"]."'";
        $query .= "   AND SCHREGNO IN( SELECT SCHREGNO ";
        $query .= "                      FROM SCHREG_REGD_DAT ";
        $query .= "                     WHERE YEAR = '".$row["YEAR"]."'";
        $query .= "                       AND SEMESTER='".$SEM[$model->field["SEMESTER"]]."'";
        $query .= "                       AND GRADE = '".$row["GRADE"]."' ";
        $query .= "                       AND HR_CLASS  = '".$row["HR_CLASS"]."' ";
        $query .= "                  )";

        return $query;
    }

}
?>
