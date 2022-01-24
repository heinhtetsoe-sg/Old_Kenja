<?php

require_once('for_php7.php');

class knjd210kquery extends Query {
    
    //学年の取得
    function getGradeQuery(){

        $query  = " SELECT ";
        $query .= "    GRADE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     GROUP BY GRADE ";

      return $query;
    }

    //学期名の取得
    function getSemesterNameQuery(){

        $query  = " SELECT ";
        $query .= "    NAMECD2, ";
        $query .= "    NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND NAMECD1 ='Z004' ";
        $query .= "   AND NAMECD2 !='0303' ";       //0303：３学期平均 処理対象外

      return $query;
    }

    //類型グループ平均算出処理済リストを取得
    function getTypeGroupHrDatQuery(){

        $query  = " SELECT YEAR,GRADE,UPDATED,JUDGE_SEMESTER ";
        $query .= "   FROM( ";
        $query .= "   SELECT YEAR,GRADE,MAX(SEM1_INTER_REC_DATE) AS UPDATED,'0101' AS JUDGE_SEMESTER FROM TYPE_GROUP_MST WHERE SEM1_INTER_REC_CNT IS NOT NULL GROUP BY YEAR,GRADE ";
        $query .= "   UNION ";
        $query .= "   SELECT YEAR,GRADE,MAX(SEM1_TERM_REC_DATE)  AS UPDATED,'0102' AS JUDGE_SEMESTER FROM TYPE_GROUP_MST WHERE SEM1_TERM_REC_CNT  IS NOT NULL GROUP BY YEAR,GRADE ";
        $query .= "   UNION ";
        $query .= "   SELECT YEAR,GRADE,MAX(SEM1_REC_DATE)       AS UPDATED,'0103' AS JUDGE_SEMESTER FROM TYPE_GROUP_MST WHERE SEM1_REC_CNT       IS NOT NULL GROUP BY YEAR,GRADE ";
        $query .= "   UNION ";
        $query .= "   SELECT YEAR,GRADE,MAX(SEM2_INTER_REC_DATE) AS UPDATED,'0201' AS JUDGE_SEMESTER FROM TYPE_GROUP_MST WHERE SEM2_INTER_REC_CNT IS NOT NULL GROUP BY YEAR,GRADE ";
        $query .= "   UNION ";
        $query .= "   SELECT YEAR,GRADE,MAX(SEM2_TERM_REC_DATE)  AS UPDATED,'0202' AS JUDGE_SEMESTER FROM TYPE_GROUP_MST WHERE SEM2_TERM_REC_CNT  IS NOT NULL GROUP BY YEAR,GRADE ";
        $query .= "   UNION ";
        $query .= "   SELECT YEAR,GRADE,MAX(SEM2_REC_DATE)       AS UPDATED,'0203' AS JUDGE_SEMESTER FROM TYPE_GROUP_MST WHERE SEM2_REC_CNT       IS NOT NULL GROUP BY YEAR,GRADE ";
        $query .= "   UNION ";
        $query .= "   SELECT YEAR,GRADE,MAX(SEM3_TERM_REC_DATE)  AS UPDATED,'0302' AS JUDGE_SEMESTER FROM TYPE_GROUP_MST WHERE SEM3_TERM_REC_CNT  IS NOT NULL GROUP BY YEAR,GRADE ";
        $query .= "   UNION ";
        $query .= "   SELECT YEAR,GRADE,MAX(GRADE_RECORD_DATE)   AS UPDATED,'8003' AS JUDGE_SEMESTER FROM TYPE_GROUP_MST WHERE GRADE_RECORD_CNT   IS NOT NULL GROUP BY YEAR,GRADE ";
        $query .= ")T1 ";
        $query .= "ORDER BY UPDATED DESC";

      return $query;
    }

    //類型グループHRクラス毎の合計値のクリア
    function getTGHDClearQuery($model,$ITEM){

        $query  = " UPDATE TYPE_GROUP_HR_DAT AS T1 ";
        $query .= "    SET ".$ITEM[$model->field["SEMESTER"]]."_SUM = Null ,";
        $query .= "        ".$ITEM[$model->field["SEMESTER"]]."_CNT = Null ";
        $query .= "  WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND GRADE='".$model->field["GRADE"]."' ";
        //echo $query;
      return $query;
    }

    //類型グループHRクラス毎の合計値の算出
    function getTGHDCalQuery($model,$ITEM,$SEM){

        $query  = " SELECT w1.YEAR,w3.TYPE_GROUP_CD,w2.GRADE,w2.HR_CLASS,SUM(w1.KIN_REC) as SUM,COUNT(w1.KIN_REC) as CNT";
        $query .= " FROM";
        $query .= "    (SELECT YEAR,SCHREGNO,SUBCLASSCD,".$ITEM[$model->field["SEMESTER"]]." as KIN_REC ";
        $query .= "       FROM KIN_RECORD_DAT";
        $query .= "      WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "    ) w1,";
        $query .= "    (SELECT SCHREGNO,GRADE,HR_CLASS";
        $query .= "       FROM SCHREG_REGD_DAT";
        $query .= "      WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "        AND SEMESTER='".$SEM[$model->field["SEMESTER"]]."'";
        $query .= "        AND GRADE='".$model->field["GRADE"]."' ";
        $query .= "    ) w2,";
        $query .= "    (SELECT TYPE_GROUP_CD,SUBCLASSCD";
        $query .= "       FROM TYPE_GROUP_MST";
        $query .= "      WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "    )w3,";
        $query .= "    (SELECT TYPE_GROUP_CD,HR_CLASS";
        $query .= "       FROM TYPE_GROUP_HR_DAT";
        $query .= "      WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "        AND GRADE='".$model->field["GRADE"]."' ";
        $query .= "    )w4";
        $query .= " WHERE w1.SCHREGNO = w2.SCHREGNO ";
        $query .= "   AND w1.SUBCLASSCD = w3.SUBCLASSCD ";
        $query .= "   AND w3.TYPE_GROUP_CD = w4.TYPE_GROUP_CD ";
        $query .= "   AND w2.HR_CLASS =w4.HR_CLASS";
        $query .= " GROUP BY";
        $query .= "    w1.YEAR,";
        $query .= "    w3.TYPE_GROUP_CD,";
        $query .= "    w2.GRADE,";
        $query .= "    w2.HR_CLASS";
      //echo $query;
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
        //echo $query;

      return $query;
    }

    //類型グループ毎の合計値のクリア
    function getTGMClearQuery($model,$ITEM){

        $query  = " UPDATE TYPE_GROUP_MST ";
        $query .= "    SET ".$ITEM[$model->field["SEMESTER"]]."_SUM = Null ,".$ITEM[$model->field["SEMESTER"]]."_CNT = Null ";
        $query .= "  WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND GRADE='".$model->field["GRADE"]."' ";
        //echo $query;

      return $query;
    }

    //類型グループ毎の合計値の算出
    function getTGMCalQuery($model,$ITEM){

        $query .= " SELECT YEAR,TYPE_GROUP_CD,SUM(".$ITEM[$model->field["SEMESTER"]]."_SUM) as SUM, SUM(".$ITEM[$model->field["SEMESTER"]]."_CNT) as CNT";
        $query .= "   FROM TYPE_GROUP_HR_DAT";
        $query .= "  WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND GRADE='".$model->field["GRADE"]."' ";
        $query .= "  GROUP BY YEAR,TYPE_GROUP_CD ";
        //echo $query;

      return $query;
    }

    //類型グループ毎の合計値の更新
    function getTGMUpQuery($model,$ITEM,$row){

        $query  = " UPDATE TYPE_GROUP_MST ";
        $query .= " SET(".$ITEM[$model->field["SEMESTER"]]."_SUM,".$ITEM[$model->field["SEMESTER"]]."_CNT) ";
        $query .= " =  (".$row["SUM"].",".$row["CNT"].") ";
        $query .= " WHERE YEAR = '".$row["YEAR"]."'";
        $query .= "   AND TYPE_GROUP_CD = '".$row["TYPE_GROUP_CD"]."'";
        //echo $query;

      return $query;
    }

    ///類型グループ毎の類型評定のクリア
    function getTASSCDClearQuery($model,$ITEM){

        $query  = " UPDATE TYPE_GROUP_MST ";
        $query .= " SET(".$ITEM[$model->field["SEMESTER"]]."_TYPE_ASSES_CD,".$ITEM[$model->field["SEMESTER"]]."_DATE) = (Null,Null) ";
        $query .= "  WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND GRADE='".$model->field["GRADE"]."' ";
        //echo $query;

      return $query;
    }

    //類型グループ毎の類型評定の判定
    function getTASSCDCalQuery($model,$ITEM){

        $query .= " SELECT W1.YEAR,w2.TYPE_GROUP_CD,W1.TYPE_ASSES_CD ";
        $query .= "   FROM TYPE_ASSES_HDAT AS W1,TYPE_GROUP_MST AS W2";
        $query .= "  WHERE W1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND W1.YEAR = W2.YEAR ";
        $query .= "    AND W2.GRADE = '".$model->field["GRADE"]."' ";
        $query .= "    AND W1.YEAR = W2.YEAR ";
        $query .= "    AND ROUND( W2.".$ITEM[$model->field["SEMESTER"]]."_SUM/W2.".$ITEM[$model->field["SEMESTER"]]."_CNT,0) ";
        $query .= "    BETWEEN W1.TYPE_GROUP_AVE_LOW AND W1.TYPE_GROUP_AVE_HIGH ";
        //echo $query;

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

    //生徒毎の類型評定データクリア
    function getSTDClearQuery($model,$ITEM,$SEM){

        $query  = " UPDATE KIN_RECORD_DAT ";
        $query .= " SET JUDGE_PATTERN = Null ";
        $query .= " WHERE YEAR = '".CTRL_YEAR."'";
        $query .= "   AND SCHREGNO IN( SELECT SCHREGNO ";
        $query .= "                      FROM SCHREG_REGD_DAT ";
        $query .= "                     WHERE YEAR = '".CTRL_YEAR."'";
        $query .= "                       AND SEMESTER='".$SEM[$model->field["SEMESTER"]]."'";
        $query .= "                       AND GRADE = '".$model->field["GRADE"]."' ";
        $query .= "                  )";

      return $query;
    }

    //生徒毎の類型評定更新用データ取得
    function getSTDCalQuery($model,$ITEM){

        $query  = " SELECT T1.YEAR, ";
        $query .= "        T1.TYPE_GROUP_CD, ";
        $query .= "        T1.SUBCLASSCD, ";
        $query .= "        T2.GRADE, ";
        $query .= "        T2.HR_CLASS, ";
        $query .= "        T1.".$ITEM[$model->field["SEMESTER"]]."_TYPE_ASSES_CD AS TYPE_ASSES_CD";
        $query .= "   FROM TYPE_GROUP_MST AS T1, TYPE_GROUP_HR_DAT AS T2 ";
        $query .= "  WHERE T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND T1.YEAR = T2.YEAR ";
        $query .= "    AND T1.TYPE_GROUP_CD = T2.TYPE_GROUP_CD ";
        $query .= "    AND T2.GRADE = '".$model->field["GRADE"]."' ";
        $query .= "    AND T1.".$ITEM[$model->field["SEMESTER"]]."_TYPE_ASSES_CD IS NOT NULL ";

      //echo $query;
      return $query;
    }

    //生徒毎の類型評定の更新
    function getSTDUpQuery($model,$ITEM,$SEM,$row){

        $query  = " UPDATE KIN_RECORD_DAT ";
        $query .= " SET (JUDGE_PATTERN) ";
        $query .= " =   ('".$row["TYPE_ASSES_CD"]."') ";
        $query .= " WHERE YEAR = '".$row["YEAR"]."'";
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
