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
    function getSemesterNameQuery($model){

        $query  = " SELECT ";
        $query .= "    NAMECD2, ";
        $query .= "    NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND NAMECD1 ='Z004' ";
        $query .= "   AND NAMECD2 !='0303' ";       //0303：３学期平均 処理対象外
//        $query .= "   AND NAMECD2 !='8013' ";       //8013：学年平均(総学用) 処理対象外
        $query .= "   AND NAMECD2 IN (SELECT control_code FROM admin_control_dat WHERE year = '".CTRL_YEAR."' AND ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "                     control_flg = '1') ";       //NO001

      return $query;
    }

    //科目読替
    function ReplaceSubclass($model, $semester, $item)
    {

        $query  = " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.{$item}, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.ATTEND_CLASSCD, ";
        $query .= "     T1.ATTEND_SCHOOL_KIND, ";
        $query .= "     T1.ATTEND_CURRICULUM_CD, ";
        $query .= "     T1.ATTEND_SUBCLASSCD, ";
        $query .= "     T1.COMBINED_CLASSCD, ";
        $query .= "     T1.COMBINED_SCHOOL_KIND, ";
        $query .= "     T1.COMBINED_CURRICULUM_CD, ";
        $query .= "     T1.COMBINED_SUBCLASSCD, ";
        $query .= "     T1.WEIGHTING, ";
        $query .= "     CRE.CREDITS, ";
        $query .= "     GCALC.GVAL_CALC ";
        /* 各生徒の読替元 */
        $query .= "  FROM (SELECT ";
        $query .= "             T1.YEAR, ";
        $query .= "             T1.SCHREGNO, ";
        $query .= "             T1.{$item}, ";
        $query .= "             T2.COURSECD, ";
        $query .= "             T2.MAJORCD, ";
        $query .= "             T2.COURSECODE, ";
        $query .= "             T2.GRADE, ";
        $query .= "             T3.ATTEND_CLASSCD, ";
        $query .= "             T3.ATTEND_SCHOOL_KIND, ";
        $query .= "             T3.ATTEND_CURRICULUM_CD, ";
        $query .= "             T3.ATTEND_SUBCLASSCD, ";
        $query .= "             T3.COMBINED_CLASSCD, ";
        $query .= "             T3.COMBINED_SCHOOL_KIND, ";
        $query .= "             T3.COMBINED_CURRICULUM_CD, ";
        $query .= "             T3.COMBINED_SUBCLASSCD, ";
        $query .= "             T3.WEIGHTING ";
        $query .= "          FROM ";
        $query .= "             KIN_RECORD_DAT T1, ";
        $query .= "             SCHREG_REGD_DAT T2, ";
        $query .= "             SUBCLASS_REPLACE_COMBINED_DAT T3 ";
        $query .= "         WHERE T1.YEAR       = T2.YEAR ";
        $query .= "           AND T1.SCHREGNO   = T2.SCHREGNo ";
        $query .= "           AND T1.YEAR       = T3.YEAR ";
        $query .= "           AND T1.CLASSCD    = T3.ATTEND_CLASSCD ";
        $query .= "           AND T1.SCHOOL_KIND = T3.ATTEND_SCHOOL_KIND ";
        $query .= "           AND T1.CURRICULUM_CD = T3.ATTEND_CURRICULUM_CD ";
        $query .= "           AND T1.SUBCLASSCD = T3.ATTEND_SUBCLASSCD ";
        $query .= "           AND T1.YEAR       = '".CTRL_YEAR."' ";
        $query .= "           AND T2.SEMESTER   = '".$semester."' ";
        $query .= "           AND T2.GRADE      = '".$model->field["GRADE"]."' ";
        $query .= "         ) T1 ";
        $query .= "   LEFT JOIN CREDIT_MST CRE ON T1.YEAR = CRE.YEAR ";
        $query .= "        AND T1.COURSECD = CRE.COURSECD ";
        $query .= "        AND T1.MAJORCD = CRE.MAJORCD ";
        $query .= "        AND T1.COURSECODE = CRE.COURSECODE ";
        $query .= "        AND T1.GRADE = CRE.GRADE ";
        $query .= "        AND T1.ATTEND_CLASSCD = CRE.CLASSCD ";
        $query .= "        AND T1.ATTEND_SCHOOL_KIND = CRE.SCHOOL_KIND ";
        $query .= "        AND T1.ATTEND_CURRICULUM_CD = CRE.CURRICULUM_CD ";
        $query .= "        AND T1.ATTEND_SUBCLASSCD = CRE.SUBCLASSCD ";
        $query .= "   LEFT JOIN COMB_GCALC_DAT GCALC ON GCALC.YEAR = T1.YEAR ";
        $query .= "        AND GCALC.COMBINED_CLASSCD = T1.COMBINED_CLASSCD ";
        $query .= "        AND GCALC.COMBINED_SCHOOL_KIND = T1.COMBINED_SCHOOL_KIND ";
        $query .= "        AND GCALC.COMBINED_CURRICULUM_CD = T1.COMBINED_CURRICULUM_CD ";
        $query .= "        AND GCALC.COMBINED_SUBCLASSCD = T1.COMBINED_SUBCLASSCD ";
        $query .= " ) ";

        $query .= " , T_VALUE AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         COMBINED_CLASSCD, ";
        $query .= "         COMBINED_SCHOOL_KIND, ";
        $query .= "         COMBINED_CURRICULUM_CD, ";
        $query .= "         COMBINED_SUBCLASSCD, ";
        $query .= "         CASE VALUE(GVAL_CALC, '0') ";
        //「0:平均」
        $query .= "              WHEN '0' THEN INT(ROUND(AVG(FLOAT({$item})) * 100, -2) / 100) ";
        //「1:単位による重み付け」
        $query .= "              WHEN '1' THEN INT(ROUND(FLOAT(SUM({$item}*CREDITS))/SUM(CREDITS) * 100, -2) / 100) ";
        //「2:最大値」
        $query .= "              WHEN '2' THEN MAX({$item}) ";
        //「3:割合」
        $query .= "              WHEN '3' THEN INT(ROUND(FLOAT(SUM(VALUE({$item}, 0) * VALUE(WEIGHTING, 0))) * 100, -2) / 100) ";
        $query .= "         ELSE NULL END AS VALUE ";
        $query .= "     FROM ";
        $query .= "         MAIN_T ";
        $query .= "     WHERE ";
        $query .= "         0 < CREDITS ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        $query .= "         GVAL_CALC, ";
        $query .= "         COMBINED_CLASSCD, ";
        $query .= "         COMBINED_SCHOOL_KIND, ";
        $query .= "         COMBINED_CURRICULUM_CD, ";
        $query .= "         COMBINED_SUBCLASSCD ";
        $query .= " ) ";
        //③合併先科目の単位を自動計算し、RECORD_DATに登録する。
        $query .= " , T_CREDIT AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         COMBINED_CLASSCD, ";
        $query .= "         COMBINED_SCHOOL_KIND, ";
        $query .= "         COMBINED_CURRICULUM_CD, ";
        $query .= "         COMBINED_SUBCLASSCD, ";
        $query .= "         SUM(CREDITS) AS COMP_CREDIT, ";
        $query .= "         SUM(CREDITS) AS GET_CREDIT ";
        $query .= "     FROM ";
        $query .= "         MAIN_T ";
        $query .= "     WHERE ";
        $query .= "         0 < {$item} "; //評定＝ゼロもしくはNULLのレコードを計算に含まない。
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        $query .= "         COMBINED_CLASSCD, ";
        $query .= "         COMBINED_SCHOOL_KIND, ";
        $query .= "         COMBINED_CURRICULUM_CD, ";
        $query .= "         COMBINED_SUBCLASSCD ";
        $query .= " ), COMB_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.COMBINED_CLASSCD, ";
        $query .= "     T1.COMBINED_SCHOOL_KIND, ";
        $query .= "     T1.COMBINED_CURRICULUM_CD, ";
        $query .= "     T1.COMBINED_SUBCLASSCD, ";
        $query .= "     L1.VALUE AS SCORE, ";
        $query .= "     L2.COMP_CREDIT, ";
        $query .= "     L2.GET_CREDIT ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         COMBINED_CLASSCD, ";
        $query .= "         COMBINED_SCHOOL_KIND, ";
        $query .= "         COMBINED_CURRICULUM_CD, ";
        $query .= "         COMBINED_SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "         MAIN_T ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        $query .= "         COMBINED_CLASSCD, ";
        $query .= "         COMBINED_SCHOOL_KIND, ";
        $query .= "         COMBINED_CURRICULUM_CD, ";
        $query .= "         COMBINED_SUBCLASSCD ";
        $query .= "     ) T1 LEFT JOIN T_VALUE  L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "               AND L1.COMBINED_CLASSCD = T1.COMBINED_CLASSCD ";
        $query .= "               AND L1.COMBINED_SCHOOL_KIND = T1.COMBINED_SCHOOL_KIND ";
        $query .= "               AND L1.COMBINED_CURRICULUM_CD = T1.COMBINED_CURRICULUM_CD ";
        $query .= "               AND L1.COMBINED_SUBCLASSCD = T1.COMBINED_SUBCLASSCD ";
        $query .= "          LEFT JOIN T_CREDIT L2 ON L2.SCHREGNO = T1.SCHREGNO ";
        $query .= "               AND L2.COMBINED_CLASSCD = T1.COMBINED_CLASSCD ";
        $query .= "               AND L2.COMBINED_SCHOOL_KIND = T1.COMBINED_SCHOOL_KIND ";
        $query .= "               AND L2.COMBINED_CURRICULUM_CD = T1.COMBINED_CURRICULUM_CD ";
        $query .= "               AND L2.COMBINED_SUBCLASSCD = T1.COMBINED_SUBCLASSCD ";
        $query .= " ) ";

        $query .= "SELECT DISTINCT ";
        $query .= "      T1.SCHREGNO ";
        $query .= "      ,T1.COMBINED_CLASSCD ";
        $query .= "      ,T1.COMBINED_SCHOOL_KIND ";
        $query .= "      ,T1.COMBINED_CURRICULUM_CD ";
        $query .= "      ,T1.COMBINED_SUBCLASSCD ";
        //読替元のレコードがすべてあってNULLの点数がない場合
        $query .= "      ,CASE WHEN T1.SUBCLASS_CNT = T2.CNT AND T1.REC_CNT = T1.ALL_CNT THEN T1.SCORE ELSE NULL END AS SCORE ";
        $query .= "      ,T3.CHAIRCD ";
        $query .= "      ,T4.SCHREGNO AS EXIST_FLG ";
        $query .= "      ,T5.TYPE_ASSES_CD ";
        $query .= "      ,T5.TYPE_ASSES_LEVEL ";
        /* 各生徒の読替元の平均点とレコード数 */
        $query .= "  FROM (SELECT T1.SCHREGNO, ";
        $query .= "              COUNT(T1.COMBINED_SUBCLASSCD) AS SUBCLASS_CNT, ";                      //読替元科目数
        $query .= "              COUNT(T1.". $item .") AS REC_CNT, ";                          //NULL値を除いた件数
        $query .= "              COUNT(*)           AS ALL_CNT, ";                             //NULL値を含むレコード数
        $query .= "              MAX(COMB_T.SCORE) AS SCORE, ";
        $query .= "              T1.COMBINED_CLASSCD, ";
        $query .= "              T1.COMBINED_SCHOOL_KIND, ";
        $query .= "              T1.COMBINED_CURRICULUM_CD, ";
        $query .= "              T1.COMBINED_SUBCLASSCD ";
        $query .= "          FROM MAIN_T T1 ";
        $query .= "               LEFT JOIN COMB_T ON T1.SCHREGNO = COMB_T.SCHREGNO ";
        $query .= "                    AND T1.COMBINED_CLASSCD = COMB_T.COMBINED_CLASSCD ";
        $query .= "                    AND T1.COMBINED_SCHOOL_KIND = COMB_T.COMBINED_SCHOOL_KIND ";
        $query .= "                    AND T1.COMBINED_CURRICULUM_CD = COMB_T.COMBINED_CURRICULUM_CD ";
        $query .= "                    AND T1.COMBINED_SUBCLASSCD = COMB_T.COMBINED_SUBCLASSCD ";
        $query .= "         GROUP BY T1.SCHREGNO ";
        $query .= "                  ,T1.COMBINED_CLASSCD ";
        $query .= "                  ,T1.COMBINED_SCHOOL_KIND ";
        $query .= "                  ,T1.COMBINED_CURRICULUM_CD ";
        $query .= "                  ,T1.COMBINED_SUBCLASSCD ";
        $query .= "         ) T1 ";
        /* 読替先ごとの読替元の科目数 */
        $query .= "   INNER JOIN ";
        $query .= "       (SELECT ";
        $query .= "            COMBINED_CLASSCD, ";
        $query .= "            COMBINED_SCHOOL_KIND, ";
        $query .= "            COMBINED_CURRICULUM_CD, ";
        $query .= "            COMBINED_SUBCLASSCD, ";
        $query .= "            COUNT(ATTEND_SUBCLASSCD) AS CNT ";
        $query .= "          FROM SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= "         WHERE YEAR      = '".CTRL_YEAR."' ";
        $query .= "         GROUP BY ";
        $query .= "            COMBINED_CLASSCD, ";
        $query .= "            COMBINED_SCHOOL_KIND, ";
        $query .= "            COMBINED_CURRICULUM_CD, ";
        $query .= "            COMBINED_SUBCLASSCD ";
        $query .= "         ) T2 ";
        $query .= "     ON T1.COMBINED_CLASSCD = T2.COMBINED_CLASSCD ";
        $query .= "        AND T1.COMBINED_SCHOOL_KIND = T2.COMBINED_SCHOOL_KIND ";
        $query .= "        AND T1.COMBINED_CURRICULUM_CD = T2.COMBINED_CURRICULUM_CD ";
        $query .= "        AND T1.COMBINED_SUBCLASSCD = T2.COMBINED_SUBCLASSCD ";
        /* 読替先の科目の講座情報 */
        $query .= "   LEFT OUTER JOIN ";
        $query .= "      (SELECT T1.SCHREGNO ";
        $query .= "             ,T1.CHAIRCD ";
        $query .= "             ,T2.CLASSCD ";
        $query .= "             ,T2.SCHOOL_KIND ";
        $query .= "             ,T2.CURRICULUM_CD ";
        $query .= "             ,T2.SUBCLASSCD ";
        $query .= "         FROM CHAIR_STD_DAT T1 ";
        $query .= "             ,CHAIR_DAT T2 ";
        $query .= "        WHERE T1.YEAR     = T2.YEAR  ";
        $query .= "          AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "          AND T1.CHAIRCD  = T2.CHAIRCD ";
        $query .= "          AND T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "          AND T1.SEMESTER = '".$semester."') T3 ";
        $query .= "     ON T1.SCHREGNO = T3.SCHREGNO ";
        $query .= "        AND T1.COMBINED_CLASSCD = T3.CLASSCD ";
        $query .= "        AND T1.COMBINED_SCHOOL_KIND = T3.SCHOOL_KIND ";
        $query .= "        AND T1.COMBINED_CURRICULUM_CD = T3.CURRICULUM_CD ";
        $query .= "        AND T1.COMBINED_SUBCLASSCD = T3.SUBCLASSCD ";
        /* 読替先の科目レコードがあるかチェックする用 */
        $query .= "   LEFT OUTER JOIN ";
        $query .= "     (SELECT SCHREGNO, ";
        $query .= "             SUBCLASSCD, ";
        $query .= "             CLASSCD, ";
        $query .= "             SCHOOL_KIND, ";
        $query .= "             CURRICULUM_CD ";
        $query .= "        FROM KIN_RECORD_DAT ";
        $query .= "       WHERE YEAR = '".CTRL_YEAR."') T4 ";
        $query .= "    ON T1.SCHREGNO           = T4.SCHREGNO ";
        $query .= "   AND T1.COMBINED_CLASSCD = T4.CLASSCD ";
        $query .= "   AND T1.COMBINED_SCHOOL_KIND = T4.SCHOOL_KIND ";
        $query .= "   AND T1.COMBINED_CURRICULUM_CD = T4.CURRICULUM_CD ";
        $query .= "   AND T1.COMBINED_SUBCLASSCD = T4.SUBCLASSCD ";
        $query .= "   LEFT OUTER JOIN ";
        /* 読替先の点数の評定 */
        $query .= "      (SELECT TYPE_ASSES_CD, TYPE_ASSES_LEVEL, TYPE_ASSES_HIGH,TYPE_ASSES_LOW ";
        $query .= "         FROM TYPE_ASSES_MST ";
        $query .= "        WHERE YEAR = '".CTRL_YEAR."') T5 ";
        $query .= "      ON CASE WHEN T1.SUBCLASS_CNT = T2.CNT AND T1.REC_CNT = T1.ALL_CNT THEN T1.SCORE ELSE NULL END ";
        $query .= "         BETWEEN T5.TYPE_ASSES_LOW AND T5.TYPE_ASSES_HIGH ";
# echo $query."<BR>";
        return $query;
    }

    //読替先の科目レコードの新規追加
    function AddKinRecord($model, $row, $item)
    {
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query  = "INSERT INTO kin_record_dat (year, schregno, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, subclasscd, chaircd, ".$item.", a_pattern_assess, b_pattern_assess, c_pattern_assess, registercd, updated) ";
        } else {
            $query  = "INSERT INTO kin_record_dat (year, schregno, subclasscd, chaircd, ".$item.", a_pattern_assess, b_pattern_assess, c_pattern_assess, registercd, updated) ";
        }
        $query .= " VALUES ('".CTRL_YEAR."'";
        $query .= "        ,'".$row["SCHREGNO"]."'";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "        ,'".$row["COMBINED_CLASSCD"]."'";
            $query .= "        ,'".$row["COMBINED_SCHOOL_KIND"]."'";
            $query .= "        ,'".$row["COMBINED_CURRICULUM_CD"]."'";
        }
        $query .= "        ,'".$row["COMBINED_SUBCLASSCD"]."'";
        $query .= "        ,".(strlen($row["CHAIRCD"]) ? "'".$row["CHAIRCD"]."'" : "NULL");
        $query .= "        ,".(strlen($row["SCORE"])   ? $row["SCORE"] : "NULL");
        if (strlen($row["A_PATTERN_ASSESS"])){
            $query .= "        ,'".$row["A_PATTERN_ASSESS"]."'";
        }else{
            $query .= "        ,NULL";
        }
        if (strlen($row["B_PATTERN_ASSESS"])){
            $query .= "        ,'".$row["B_PATTERN_ASSESS"]."'";
        }else{
            $query .= "        ,NULL";
        }
        if (strlen($row["C_PATTERN_ASSESS"])){
            $query .= "        ,'".$row["C_PATTERN_ASSESS"]."'";
        }else{
            $query .= "        ,NULL";
        }
        $query .= "        ,'".STAFFCD."'";
        $query .= "        ,SYSDATE() ";
        $query .= "        ) ";
# echo $query."<BR>";
        return $query;
    }

    //読替先の科目レコードの更新
    function UpdateKinRecord($model, $row, $item)
    {
        $query  = "UPDATE kin_record_dat ";
        $query .= "   SET ".$item."        = ".(strlen($row["SCORE"]) ? $row["SCORE"] : "NULL");
        if (strlen($row["A_PATTERN_ASSESS"])){
            $query .= "      ,a_pattern_assess = '".$row["A_PATTERN_ASSESS"]."'";
        }else{
            $query .= "      ,a_pattern_assess = null";
        }
        if (strlen($row["B_PATTERN_ASSESS"])){
            $query .= "      ,b_pattern_assess = '".$row["B_PATTERN_ASSESS"]."'";
        }else{
            $query .= "      ,b_pattern_assess = null";
        }
        if (strlen($row["C_PATTERN_ASSESS"])){
            $query .= "      ,c_pattern_assess = '".$row["C_PATTERN_ASSESS"]."'";
        }else{
            $query .= "      ,c_pattern_assess = null";
        }
        $query .= "      ,registercd       = '".STAFFCD."'";
        $query .= "      ,updated          = SYSDATE() ";
        $query .= " WHERE year       = '".CTRL_YEAR."'";
        $query .= "   AND schregno   = '".$row["SCHREGNO"]."'";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "   AND CLASSCD = '".$row["COMBINED_CLASSCD"]."'";
            $query .= "   AND SCHOOL_KIND = '".$row["COMBINED_SCHOOL_KIND"]."'";
            $query .= "   AND CURRICULUM_CD = '".$row["COMBINED_CURRICULUM_CD"]."'";
        }
        $query .= "   AND subclasscd = '".$row["COMBINED_SUBCLASSCD"]."'";
#    echo $query."<BR>";
        return $query;
    }
    
    //学年平均の更新(3学期期末が入っているレコードのみ。しかし3年生の場合は条件にしない）
    //1学期平均、2学期平均、3学期平均の平均値を求める
    function UpdateGradeRec1($model, $row, $flg)
    {

        $query .= "UPDATE kin_record_dat ";
        $query .= "   SET grade_record  ";
        $query .= "      = ";
        $query .= "       (SELECT ROUND(AVG(DECIMAL(rec)), 0) ";
        $query .= "          FROM ";
        $query .= "        (SELECT sem1_rec AS rec ";
        $query .= "          FROM kin_record_dat  ";
        $query .= "         WHERE year       = '".CTRL_YEAR."'  ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "           AND CLASSCD = '".$row["GRADING_CLASSCD"]."'  ";
            $query .= "           AND SCHOOL_KIND = '".$row["GRADING_SCHOOL_KIND"]."'  ";
            $query .= "           AND CURRICULUM_CD = '".$row["GRADING_CURRICULUM_CD"]."'  ";
        }
        $query .= "           AND subclasscd = '".$row["GRADING_SUBCLASSCD"]."'  ";
        $query .= "           AND schregno   = '".$row["SCHREGNO"]."'  ";
        $query .= "        UNION ALL";
        $query .= "        SELECT sem2_rec AS rec ";
        $query .= "          FROM  kin_record_dat  ";
        $query .= "         WHERE year       = '".CTRL_YEAR."'  ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "           AND CLASSCD = '".$row["GRADING_CLASSCD"]."'  ";
            $query .= "           AND SCHOOL_KIND = '".$row["GRADING_SCHOOL_KIND"]."'  ";
            $query .= "           AND CURRICULUM_CD = '".$row["GRADING_CURRICULUM_CD"]."'  ";
        }
        $query .= "           AND subclasscd = '".$row["GRADING_SUBCLASSCD"]."'  ";
        $query .= "           AND schregno   = '".$row["SCHREGNO"]."'  ";
        $query .= "        UNION ALL";
#        $query .= "        SELECT sem3_rec AS rec ";   //arakaki
        $query .= "        SELECT SEM3_TERM_REC AS rec ";
        $query .= "          FROM  kin_record_dat  ";
        $query .= "         WHERE year       = '".CTRL_YEAR."'  ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "           AND CLASSCD = '".$row["GRADING_CLASSCD"]."'  ";
            $query .= "           AND SCHOOL_KIND = '".$row["GRADING_SCHOOL_KIND"]."'  ";
            $query .= "           AND CURRICULUM_CD = '".$row["GRADING_CURRICULUM_CD"]."'  ";
        }
        $query .= "           AND subclasscd = '".$row["GRADING_SUBCLASSCD"]."'  ";
        $query .= "           AND schregno   = '".$row["SCHREGNO"]."' ) t1) ";
        $query .= "  WHERE year       = '".CTRL_YEAR."'  ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "           AND CLASSCD = '".$row["GRADING_CLASSCD"]."'  ";
            $query .= "           AND SCHOOL_KIND = '".$row["GRADING_SCHOOL_KIND"]."'  ";
            $query .= "           AND CURRICULUM_CD = '".$row["GRADING_CURRICULUM_CD"]."'  ";
        }
        $query .= "    AND subclasscd = '".$row["GRADING_SUBCLASSCD"]."'  ";
        $query .= "    AND schregno   = '".$row["SCHREGNO"]."'  ";
        if ($flg != "03")
#            $query .= "    AND sem3_rec IS NOT NULL "; //2005/03/03 arakaki
            $query .= "    AND SEM3_TERM_REC IS NOT NULL ";
#echo $query."<BR>";
        return $query;

/*
        $query .= "UPDATE kin_record_dat ";
        $query .= "   SET grade_record  ";
        $query .= "      = ";
        $query .= "       (SELECT ROUND(AVG(DECIMAL(rec)), 0) ";
        $query .= "          FROM ";
        $query .= "        (SELECT sem1_rec AS rec ";
        $query .= "          FROM kin_record_dat  ";
        $query .= "         WHERE year       = '".CTRL_YEAR."'  ";
        $query .= "           AND subclasscd = '".$row["GRADING_SUBCLASSCD"]."'  ";
        $query .= "           AND schregno   = '".$row["SCHREGNO"]."'  ";
        $query .= "        UNION ALL";
        $query .= "        SELECT sem2_rec AS rec ";
        $query .= "          FROM  kin_record_dat  ";
        $query .= "         WHERE year       = '".CTRL_YEAR."'  ";
        $query .= "           AND subclasscd = '".$row["GRADING_SUBCLASSCD"]."'  ";
        $query .= "           AND schregno   = '".$row["SCHREGNO"]."'  ";
        $query .= "        UNION ALL";
#        $query .= "        SELECT sem3_rec AS rec ";   //arakaki
        $query .= "        SELECT SEM3_TERM_REC AS rec ";
        $query .= "          FROM  kin_record_dat  ";
        $query .= "         WHERE year       = '".CTRL_YEAR."'  ";
        $query .= "           AND subclasscd = '".$row["GRADING_SUBCLASSCD"]."'  ";
        $query .= "           AND schregno   = '".$row["SCHREGNO"]."' ) t1) ";
        $query .= "  WHERE year       = '".CTRL_YEAR."'  ";
        $query .= "    AND subclasscd = '".$row["GRADING_SUBCLASSCD"]."'  ";
        $query .= "    AND schregno   = '".$row["SCHREGNO"]."'  ";
        if ($flg != "03")
#            $query .= "    AND sem3_rec IS NOT NULL "; //2005/03/03 arakaki
            $query .= "    AND SEM3_TERM_REC IS NOT NULL ";
#echo $query."<BR>";
        return $query;
*/

    }

    //3学期期末が入ってない場合は学年平均をNULLにする
    function UpdateGradeRec2($db,$model,$ITEM,$SEM)
    {
#    function getTGHDCalQuery($db,$model,$ITEM,$SEM)


/*2005/03/09*/
        if($SEM[$model->field["SEMESTER"]] == '3'){
            //選択された月の学期開始日
            $sdate = $db->getOne("SELECT sdate FROM semester_mst WHERE year = '".CTRL_YEAR."' AND semester = '".$SEM[$model->field["SEMESTER"]]."'");
            //選択された月の学期終了日
            $edate = $db->getOne("SELECT edate FROM semester_mst WHERE year = '".CTRL_YEAR."' AND semester = '".$SEM[$model->field["SEMESTER"]]."'");
        }
/*2005/03/09*/

        $query .= "UPDATE kin_record_dat ";
        $query .= "   SET grade_record     = NULL, ";
        $query .= "       A_PATTERN_ASSESS = NULL, ";
        $query .= "       B_PATTERN_ASSESS = NULL, ";
        $query .= "       C_PATTERN_ASSESS = NULL ";
        $query .= "  WHERE year       = '".CTRL_YEAR."'  ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "           AND CLASSCD = '".$row["COMBINED_CLASSCD"]."'  ";
            $query .= "           AND SCHOOL_KIND = '".$row["COMBINED_SCHOOL_KIND"]."'  ";
            $query .= "           AND CURRICULUM_CD = '".$row["COMBINED_CURRICULUM_CD"]."'  ";
        }
        $query .= "    AND subclasscd = '".$row["COMBINED_SUBCLASSCD"]."'  ";
        $query .= "    AND schregno   = '".$row["SCHREGNO"]."'  ";
        $query .= "    AND SEM3_TERM_REC IS NULL ";

/*2005/03/09*/
        $query .= "    AND (SCHREGNO IN ( ";
        $query .= "                          SELECT schregno  ";
        $query .= "                            FROM schreg_base_mst ";
        $query .= "                           WHERE grd_div IN ('2', '3') "; //退学、転学
        $query .= "                             AND grd_date <= DATE('".$model->ctrl_date."') ";
        $query .= "                    )  ";
        $query .= "         or SCHREGNO IN ( ";
        $query .= "                          SELECT schregno ";
        $query .= "                            FROM schreg_transfer_dat ";
        $query .= "                           WHERE transfercd = '2' ";  //休学
        $query .= "                             AND DATE('".$model->ctrl_date."') BETWEEN transfer_sdate AND transfer_edate ";
        $query .= "                        )  ";
        $query .= "        or SCHREGNO IN ( ";
        $query .= "                          SELECT T1.schregno ";
        $query .= "                            FROM (SELECT * FROM schreg_transfer_dat ";
        if($SEM[$model->field["SEMESTER"]] == '3'){
            $query .= "                           WHERE SCHREGNO NOT IN (SELECT schregno FROM schreg_transfer_dat WHERE transfercd='1' AND transfer_sdate >= DATE('".$sdate."') AND transfer_edate <= DATE('".$edate."'))";
        }
        $query .= "                                 ) T1";
        $query .= "                          WHERE T1.transfercd = '1' ";  //留学
        $query .= "                            AND DATE('".$model->ctrl_date."') BETWEEN T1.transfer_sdate AND T1.transfer_edate ";
        $query .= "                        ) ";
        $query .= "        ) ";
/*2005/03/09*/

#       echo $query ."<BR>";
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
        $query .= "   UNION ";
        $query .= "   SELECT ";
        $query .= "       T1.YEAR, ";
        $query .= "       T2.GRADE, ";
        $query .= "       MAX(T1.UPDATED) AS UPDATED, ";
        $query .= "       '8013' AS JUDGE_SEMESTER ";
        $query .= "   FROM ";
        $query .= "       RECORD_PROV_FLG_DAT T1 ";
        $query .= "       INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                    AND T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "                                    AND T2.SEMESTER='".CTRL_SEMESTER."' ";
        $query .= "   WHERE ";
        $query .= "       T1.YEAR = '".CTRL_YEAR."'";
        $query .= "       AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD IN (SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '".CTRL_YEAR."' AND NAMECD1 = 'D065') ";
        $query .= "   GROUP BY ";
        $query .= "       T1.YEAR, ";
        $query .= "       T2.GRADE ";
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
    //2005/03/02■変更　読替先の成績は、読替元の成績の平均値ではなくそのままセット
    function getTGHDCalQuery($db,$model,$ITEM,$SEM)
    {
/*2005/03/09*/
        if($SEM[$model->field["SEMESTER"]] == '3'){
            //選択された月の学期開始日
            $sdate = $db->getOne("SELECT sdate FROM semester_mst WHERE year = '".CTRL_YEAR."' AND semester = '".$SEM[$model->field["SEMESTER"]]."'");
            //選択された月の学期終了日
            $edate = $db->getOne("SELECT edate FROM semester_mst WHERE year = '".CTRL_YEAR."' AND semester = '".$SEM[$model->field["SEMESTER"]]."'");
        }
/*2005/03/09*/
#echo $sdate."<br>".$edate;



        $query  = " SELECT w1.YEAR,w3.TYPE_GROUP_CD,w2.GRADE,w2.HR_CLASS,coalesce(SUM(w1.KIN_REC),0) as SUM,coalesce(COUNT(w1.KIN_REC),0) as CNT";
        $query .= " FROM";
#↓2005/03/09 近大-作業依頼書20050309-01
#↓2005/03/02

        $query .= "    (SELECT YEAR,SCHREGNO,SUBCLASSCD,".$ITEM[$model->field["SEMESTER"]]." as KIN_REC ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "           ,CLASSCD  ";
            $query .= "           ,SCHOOL_KIND  ";
            $query .= "           ,CURRICULUM_CD  ";
        }
        $query .= "       FROM KIN_RECORD_DAT";
        $query .= "      WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "    ) w1,";
                 /* 読替元の成績を読替先の成績として取得 */
#        $query .= "(SELECT T1.year, T1.schregno, T2.grading_subclasscd AS subclasscd, T1.".$ITEM[$model->field["SEMESTER"]]." as KIN_REC ";
#        $query .= "  FROM kin_record_dat T1 ";
#        $query .= "      ,subclass_replace_dat T2 ";
#        $query .= " WHERE T1.year       = T2.year ";
#        $query .= "   AND T1.subclasscd = T2.attend_subclasscd ";
#        $query .= "   AND T2.replacecd  = '1' ";
#        $query .= "   AND T2.annual     = '".$model->field["GRADE"]."' ";
#        $query .= "   AND T1.year       = '".CTRL_YEAR."' ";
#        $query .= " UNION  ALL ";
#                  /* 読替科目以外の一般の成績 */
#        $query .= "SELECT T1.year, T1.schregno, T1.subclasscd, T1.".$ITEM[$model->field["SEMESTER"]]." as KIN_REC ";
#        $query .= "  FROM kin_record_dat T1 ";
#        $query .= " WHERE T1.year       = '".CTRL_YEAR."' ";
#        $query .= "   AND T1.subclasscd NOT IN (SELECT DISTINCT grading_subclasscd  ";
#        $query .= "                               FROM subclass_replace_dat T2 ";
#        $query .= "                              WHERE T2.year      = '".CTRL_YEAR."' ";
#        $query .= "                                AND T2.replacecd = '1' ";
#        $query .= "                                AND annual       = '".$model->field["GRADE"]."')) w1, ";
#↑2005/03/02
#↑2005/03/09 近大-作業依頼書20050309-01
        $query .= "    (SELECT SCHREGNO,GRADE,HR_CLASS";
        $query .= "       FROM SCHREG_REGD_DAT";
        $query .= "      WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "        AND SEMESTER='".$SEM[$model->field["SEMESTER"]]."'";
        $query .= "        AND GRADE='".$model->field["GRADE"]."' ";
        $query .= "        AND SCHREGNO NOT IN ( ";                                                                                 // ↓ 2005/02/16    
        $query .= "                             SELECT schregno  ";
        $query .= "                               FROM schreg_base_mst ";
        $query .= "                              WHERE grd_div IN ('2', '3') "; //退学、転学
        $query .= "                                AND grd_date <= DATE('".$model->ctrl_date."') ";
        $query .= "                              )  ";
/*
        $query .= "        AND SCHREGNO NOT IN ( ";
        $query .= "                             SELECT schregno ";
        $query .= "                               FROM schreg_transfer_dat ";
        $query .= "                              WHERE transfercd IN ('1', '2') ";  //留学、休学
        $query .= "                                AND DATE('".$model->ctrl_date."') BETWEEN transfer_sdate AND transfer_edate ";
        $query .= "                              )  ";                                                                              // ↑ 2005/02/16
*/
/*2005/03/09*/
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
/*2005/03/09*/
        $query .= "                            )  ";                                                                              // ↑ 2005/02/16
        $query .= "    ) w2,";
        $query .= "    (SELECT TYPE_GROUP_CD,SUBCLASSCD";#echo $sdate."<br>".$edate;
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "           ,CLASSCD  ";
            $query .= "           ,SCHOOL_KIND  ";
            $query .= "           ,CURRICULUM_CD  ";
        }
        $query .= "       FROM TYPE_GROUP_MST";
        $query .= "      WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "    )w3,";
        $query .= "    (SELECT TYPE_GROUP_CD,HR_CLASS";
        $query .= "       FROM TYPE_GROUP_HR_DAT";
        $query .= "      WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "        AND GRADE='".$model->field["GRADE"]."' ";
        $query .= "    )w4";
        $query .= " WHERE w1.SCHREGNO      = w2.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "   AND w1.CLASSCD    = w3.CLASSCD ";
            $query .= "   AND w1.SCHOOL_KIND    = w3.SCHOOL_KIND ";
            $query .= "   AND w1.CURRICULUM_CD    = w3.CURRICULUM_CD ";
        }
        $query .= "   AND w1.SUBCLASSCD    = w3.SUBCLASSCD ";
        $query .= "   AND w3.TYPE_GROUP_CD = w4.TYPE_GROUP_CD ";
        $query .= "   AND w2.HR_CLASS      = w4.HR_CLASS";
        $query .= " GROUP BY";
        $query .= "    w1.YEAR,";
        $query .= "    w3.TYPE_GROUP_CD,";
        $query .= "    w2.GRADE,";
        $query .= "    w2.HR_CLASS";
#     echo $query;
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
        //echo $query."<BR>";
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

//        $query .= " SELECT YEAR,TYPE_GROUP_CD,SUM(".$ITEM[$model->field["SEMESTER"]]."_SUM) as SUM, SUM(".$ITEM[$model->field["SEMESTER"]]."_CNT) as CNT";    2004/09/14 arakaki 近大-作業依頼書20040908-01.doc
        $query .= " SELECT YEAR,TYPE_GROUP_CD,coalesce(SUM(".$ITEM[$model->field["SEMESTER"]]."_SUM),0) as SUM, coalesce(SUM(".$ITEM[$model->field["SEMESTER"]]."_CNT),0) as CNT";
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
        //echo $query . "<BR>";
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

        $query .= " SELECT W1.YEAR,W2.TYPE_GROUP_CD,W1.TYPE_ASSES_CD ";
        $query .= "   FROM TYPE_ASSES_HDAT AS W1,TYPE_GROUP_MST AS W2";
        $query .= "  WHERE W1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND W1.YEAR = W2.YEAR ";
        $query .= "    AND W2.GRADE = '".$model->field["GRADE"]."' ";
        $query .= "    AND W1.YEAR = W2.YEAR ";
#        $query .= "    AND ROUND( W2.".$ITEM[$model->field["SEMESTER"]]."_SUM/W2.".$ITEM[$model->field["SEMESTER"]]."_CNT,0) ";    //2004/11/04 arakaki 近大-作業依頼書20041102-02
        $query .= "    AND ROUND( DOUBLE(W2.".$ITEM[$model->field["SEMESTER"]]."_SUM)/DOUBLE(W2.".$ITEM[$model->field["SEMESTER"]]."_CNT),0) ";
        $query .= "    BETWEEN W1.TYPE_GROUP_AVE_LOW AND W1.TYPE_GROUP_AVE_HIGH ";
        $query .= "    AND W2.".$ITEM[$model->field["SEMESTER"]]."_CNT > 0 ";      //2004/09/14 arakaki 近大-作業依頼書20040908-01.doc
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

    //固定評定処理
    function getFixValueSetQuery($model,$ITEM){

        $query  = " UPDATE TYPE_GROUP_MST ";
        $query .= " SET(".$ITEM[$model->field["SEMESTER"]]."_TYPE_ASSES_CD,".$ITEM[$model->field["SEMESTER"]]."_DATE) ";
        $query .= " =  (TYPE_ASSES_CD,SYSDATE()) ";
        $query .= " WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND GRADE = '".$model->field["GRADE"]."' ";   //2004/12/01 arakaki
//        $query .= "   AND TYPE_ASSES_CD is not null ";             2004/12/01 arakaki
        $query .= "   AND TYPE_ASSES_CD IN('A','B','C') ";

        //echo $query;

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
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "        T1.CLASSCD, ";
            $query .= "        T1.SCHOOL_KIND, ";
            $query .= "        T1.CURRICULUM_CD, ";
        }
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
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "   AND CLASSCD = '".$row["CLASSCD"]."'";
            $query .= "   AND SCHOOL_KIND = '".$row["SCHOOL_KIND"]."'";
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
        //echo $query."<BR>";
      return $query;
    }

    //追試験対象者データクリア
    function getSTDsplClearQuery($grade){
//        $query  = " DELETE FROM supp_exa_dat ";   //←これだと学年変えて更新したときに、別の学年のデータも削除される
        $query  = " DELETE FROM supp_exa_dat WHERE schregno IN (SELECT schregno FROM schreg_regd_dat ";
        $query .= "                                              WHERE year     = '".CTRL_YEAR."' ";
        $query .= "                                                AND semester = '".CTRL_SEMESTER."' ";
        $query .= "                                                AND grade    = '".$grade."' ";
        $query .= "                                            )";
        $query .= "                            AND year = '".CTRL_YEAR."' ";
      return $query;
    }

    //追試験対象者データ追加
    function getSTDsplInsertQuery($model,$grade){

        $query  = " INSERT INTO supp_exa_dat ";
        $query .= "             (year, subclasscd, schregno, grade_record, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "   CLASSCD, ";
            $query .= "   SCHOOL_KIND, ";
            $query .= "   CURRICULUM_CD, ";
        }
        $query .= "              a_pattern_assess, b_pattern_assess, c_pattern_assess, judge_pattern, ";
        $query .= "              registercd, updated) ";
        $query .= " SELECT ";
        $query .= "     year, ";
        $query .= "     W.subclasscd, ";
        $query .= "     W.schregno, ";
        $query .= "     W.grade_record, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "   W.CLASSCD, ";
            $query .= "   W.SCHOOL_KIND, ";
            $query .= "   W.CURRICULUM_CD, ";
        }
        $query .= "     W.a_pattern_assess, ";
        $query .= "     W.b_pattern_assess , ";
        $query .= "     W.c_pattern_assess, ";
        $query .= "     W.judge_pattern, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         T.schregno, ";
        $query .= "         COUNT(integer(T.elect)) as elect, ";        //-------->必修科目の欠点科目数
        $query .= "         COUNT(integer(T.elect_sel)) as elect_sel "; //-------->選択科目の欠点科目数
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             wt1.year, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "             wt1.CLASSCD, ";
            $query .= "             wt1.SCHOOL_KIND, ";
            $query .= "             wt1.CURRICULUM_CD, ";
        }
        $query .= "             wt1.subclasscd, ";
        $query .= "             wt1.schregno, ";
        $query .= "             CASE WHEN wt1.judge_pattern='A' then wt1.a_pattern_assess ";
        $query .= "                  WHEN wt1.judge_pattern='B' then wt1.b_pattern_assess ";
        $query .= "                  WHEN wt1.judge_pattern='C' then wt1.c_pattern_assess ";
        $query .= "             END AS assess, ";
        $query .= "             CASE WHEN wt2.electdiv IS NULL then '0' ";
        $query .= "                  WHEN wt2.electdiv='0' then wt2.electdiv ";
        $query .= "             END AS elect, ";
        $query .= "             CASE WHEN wt2.electdiv='1' then wt2.electdiv ";
        $query .= "             END AS elect_sel ";
        $query .= "         FROM ";
        $query .= "             kin_record_dat wt1 ";
        $query .= "         LEFT OUTER JOIN ";
        $query .= "             v_subclass_mst wt2 ";
        $query .= "         ON ";
        $query .= "             wt1.year = wt2.year AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "             wt1.CLASSCD = wt2.CLASSCD AND ";
            $query .= "             wt1.SCHOOL_KIND = wt2.SCHOOL_KIND AND ";
            $query .= "             wt1.CURRICULUM_CD = wt2.CURRICULUM_CD AND ";
        }
        $query .= "             wt1.subclasscd = wt2.subclasscd ";
        $query .= "         WHERE ";
        $query .= "             wt1.year = '".CTRL_YEAR."' AND ";
        $query .= "             wt1.judge_pattern <> ''  AND";
        $query .= "             wt1.schregno IN (SELECT schregno ";
        $query .= "                                FROM schreg_regd_dat ";
        $query .= "                               WHERE year     = '".CTRL_YEAR."' ";
        $query .= "                                 AND semester = '".CTRL_SEMESTER."' ";
        $query .= "                                 AND grade    = '".$grade."' ";
        $query .= "                         )" ;
#↓2005/03/07 異動生徒は、対象にしない。
        $query .= "        AND wt1.SCHREGNO NOT IN ( ";
        $query .= "                             SELECT schregno  ";
        $query .= "                               FROM schreg_base_mst ";
        $query .= "                              WHERE grd_div IN ('2', '3') ";
        $query .= "                                AND grd_date <= DATE('".$model->ctrl_date."') ";
        $query .= "                              )  ";
        $query .= "        AND wt1.SCHREGNO NOT IN ( ";
        $query .= "                             SELECT schregno ";
        $query .= "                               FROM schreg_transfer_dat ";
        $query .= "                              WHERE transfercd IN ('1', '2') ";
        $query .= "                                AND DATE('".$model->ctrl_date."') BETWEEN transfer_sdate AND transfer_edate ";
        $query .= "                              )  ";
#↑2005/03/07 異動生徒は、対象にしない。
#↓2005/03/02 読替元の科目は、欠点科目としない
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         AND wt1.CLASSCD || wt1.SCHOOL_KIND || wt1.CURRICULUM_CD || wt1.SUBCLASSCD NOT IN (SELECT DISTINCT ATTEND_CLASSCD || ATTEND_SCHOOL_KIND || ATTEND_CURRICULUM_CD || ATTEND_SUBCLASSCD  ";
        } else {
            $query .= "         AND wt1.subclasscd NOT IN (SELECT DISTINCT ATTEND_SUBCLASSCD  ";
        }
        $query .= "                                      FROM SUBCLASS_REPLACE_COMBINED_DAT  T2 ";
        $query .= "                                     WHERE T2.year      = '".CTRL_YEAR."' ";
        $query .= "                                       AND T2.replacecd = '1') ";
#↑2005/03/02
        $query .= "         ) T ";
        $query .= "     WHERE ";
        $query .= "         T.assess <= '1' ";
        $query .= "     GROUP BY T.schregno ";
        $query .= "     ) T1 ";     //---------->schregno毎の評定１の必修科目の数と選択科目の数
        $query .= " LEFT OUTER JOIN ";
        $query .= "     (SELECT ";
        $query .= "         year, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         subclasscd, ";
        $query .= "         schregno, ";
        $query .= "         grade_record, ";
        $query .= "         judge_pattern, ";
        $query .= "         a_pattern_assess, ";
        $query .= "         b_pattern_assess, ";
        $query .= "         c_pattern_assess, ";
        $query .= "         CASE WHEN judge_pattern='A' then a_pattern_assess ";
        $query .= "              WHEN judge_pattern='B' then b_pattern_assess ";
        $query .= "              WHEN judge_pattern='C' then c_pattern_assess ";
        $query .= "         END AS assess ";
        $query .= "     FROM ";
        $query .= "         kin_record_dat ";
        $query .= "     WHERE ";
        $query .= "         year = '".CTRL_YEAR."' AND ";
        $query .= "         judge_pattern <> ''  AND";
        $query .= "         schregno IN (SELECT schregno ";
        $query .= "                        FROM schreg_regd_dat ";
        $query .= "                       WHERE year     = '".CTRL_YEAR."' ";
        $query .= "                         AND semester = '".CTRL_SEMESTER."' ";
        $query .= "                         AND grade    = '".$grade."' ";
#↓2005/03/02 読替元の科目は、欠点科目としない
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         AND CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD NOT IN (SELECT DISTINCT ATTEND_CLASSCD || ATTEND_SCHOOL_KIND || ATTEND_CURRICULUM_CD || ATTEND_SUBCLASSCD  ";
        } else {
            $query .= "         AND subclasscd NOT IN (SELECT DISTINCT ATTEND_SUBCLASSCD  ";
        }
        $query .= "                                  FROM SUBCLASS_REPLACE_COMBINED_DAT  T2 ";
        $query .= "                                 WHERE T2.year      = '".CTRL_YEAR."' ";
        $query .= "                                   AND T2.replacecd = '1') ";
#↑2005/03/02

        $query .= "                     )" ;
        $query .= "     ) W ";      //---------->judge_pattern で pattern_assessを判断し、データを取得する
        $query .= "   ON W.schregno = T1.schregno ";
        $query .= " WHERE ";
        $query .= "     (W.assess <= '1' AND ";
        $query .= "     T1.elect BETWEEN 1 AND 3) ";   //---------->必修科目の欠点科目が１科目～３科目
        $query .= " OR ";                              //---------->または
        $query .= "     (W.assess <= '1' AND ";
        $query .= "     T1.elect = 0 AND T1.elect_sel >= 1) ";  //---------->必修科目の欠点科目が０科目で選択科目の欠点科目が１科目以上を追試験対象とする
        $query .= " ORDER BY ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         W.CLASSCD, ";
            $query .= "         W.SCHOOL_KIND, ";
            $query .= "         W.CURRICULUM_CD, ";
        }
        $query .= "     W.subclasscd,W.schregno ";
      //echo $query;
      return $query;
    }

    //RECORD_PROV_FLG_DATのレコードを削除。
    function delRecordProvFlgDat($model, $ITEM, $SEM, $row) {
        $query  = "";
        $query .= " DELETE FROM RECORD_PROV_FLG_DAT PROV ";
        $query .= " WHERE ";
        $query .= " EXISTS ( ";
        $query .= "     SELECT ";
        $query .= "         'X' ";
        $query .= "     FROM ";
        $query .= "         KIN_RECORD_DAT KINREC ";
        $query .= "     WHERE ";
        $query .= "         KINREC.YEAR = '".$row["YEAR"]."'";
        $query .= "         AND KINREC.CLASSCD = '".$row["CLASSCD"]."'";
        $query .= "         AND KINREC.SCHOOL_KIND = '".$row["SCHOOL_KIND"]."'";
        $query .= "         AND KINREC.CURRICULUM_CD = '".$row["CURRICULUM_CD"]."'";
        $query .= "         AND KINREC.SUBCLASSCD = '".$row["SUBCLASSCD"]."'";
        $query .= "         AND KINREC.SCHREGNO IN( SELECT SCHREGNO ";
        $query .= "                                 FROM SCHREG_REGD_DAT ";
        $query .= "                                 WHERE YEAR = '".$row["YEAR"]."'";
        $query .= "                                   AND SEMESTER='".$SEM[$model->field["SEMESTER"]]."'";
        $query .= "                                   AND GRADE = '".$row["GRADE"]."' ";
        $query .= "                                   AND HR_CLASS  = '".$row["HR_CLASS"]."' ";
        $query .= "                        )";
        $query .= "         AND PROV.YEAR = KINREC.YEAR ";
        $query .= "         AND PROV.CLASSCD = KINREC.CLASSCD ";
        $query .= "         AND PROV.SCHOOL_KIND = KINREC.SCHOOL_KIND ";
        $query .= "         AND PROV.CURRICULUM_CD = KINREC.CURRICULUM_CD ";
        $query .= "         AND PROV.SUBCLASSCD = KINREC.SUBCLASSCD ";
        $query .= "         AND PROV.SCHREGNO = KINREC.SCHREGNO ";
        $query .= " ) ";

        return $query;
    }

    //RECORD_PROV_FLG_DATのレコードを追加
    function insRecordProvFlgDat($model, $ITEM, $SEM, $row) {
        $query  = "";
        $query .= " INSERT INTO RECORD_PROV_FLG_DAT ";
        $query .= " ( ";
        $query .= "     YEAR, ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     SCHREGNO, ";
        $query .= "     PROV_FLG, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     SCHREGNO, ";
        $query .= "     '1', ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     KIN_RECORD_DAT KINREC ";
        $query .= " WHERE ";
        $query .= "     KINREC.YEAR = '".$row["YEAR"]."'";
        $query .= "     AND KINREC.CLASSCD = '".$row["CLASSCD"]."'";
        $query .= "     AND KINREC.SCHOOL_KIND = '".$row["SCHOOL_KIND"]."'";
        $query .= "     AND KINREC.CURRICULUM_CD = '".$row["CURRICULUM_CD"]."'";
        $query .= "     AND KINREC.SUBCLASSCD = '".$row["SUBCLASSCD"]."'";
        $query .= "     AND KINREC.SCHREGNO IN( SELECT SCHREGNO ";
        $query .= "                             FROM SCHREG_REGD_DAT ";
        $query .= "                             WHERE YEAR = '".$row["YEAR"]."'";
        $query .= "                               AND SEMESTER='".$SEM[$model->field["SEMESTER"]]."'";
        $query .= "                               AND GRADE = '".$row["GRADE"]."' ";
        $query .= "                               AND HR_CLASS  = '".$row["HR_CLASS"]."' ";
        $query .= "                    )";
        $query .= "     AND KINREC.".$ITEM[$model->field["SEMESTER"]]." IS NOT NULL ";

        return $query;
    }

    //RECORD_PROV_FLG_DATのレコードを削除。
    function delRecordProvFlgDatD065($model, $SEM) {
        $query  = "";
        $query .= " DELETE FROM RECORD_PROV_FLG_DAT PROV ";
        $query .= " WHERE ";
        $query .= " EXISTS ( ";
        $query .= "     SELECT ";
        $query .= "         'X' ";
        $query .= "     FROM ";
        $query .= "         KIN_RECORD_DAT KINREC ";
        $query .= "     WHERE ";
        $query .= "         KINREC.YEAR = '".CTRL_YEAR."'";
        $query .= "         AND KINREC.CLASSCD || '-' || KINREC.SCHOOL_KIND || '-' || KINREC.CURRICULUM_CD || '-' || KINREC.SUBCLASSCD IN (SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '".CTRL_YEAR."' AND NAMECD1 = 'D065') ";
        $query .= "         AND KINREC.SCHREGNO IN( SELECT SCHREGNO ";
        $query .= "                                 FROM SCHREG_REGD_DAT ";
        $query .= "                                 WHERE YEAR = '".CTRL_YEAR."'";
        $query .= "                                   AND SEMESTER='".$SEM[$model->field["SEMESTER"]]."'";
        $query .= "                                   AND GRADE = '".$model->field["GRADE"]."' ";
        $query .= "                        )";
        $query .= "         AND PROV.YEAR = KINREC.YEAR ";
        $query .= "         AND PROV.CLASSCD = KINREC.CLASSCD ";
        $query .= "         AND PROV.SCHOOL_KIND = KINREC.SCHOOL_KIND ";
        $query .= "         AND PROV.CURRICULUM_CD = KINREC.CURRICULUM_CD ";
        $query .= "         AND PROV.SUBCLASSCD = KINREC.SUBCLASSCD ";
        $query .= "         AND PROV.SCHREGNO = KINREC.SCHREGNO ";
        $query .= " ) ";

        return $query;
    }

    //RECORD_PROV_FLG_DATのレコードを追加
    function insRecordProvFlgDatD065($model, $ITEM, $SEM) {
        $query  = "";
        $query .= " INSERT INTO RECORD_PROV_FLG_DAT ";
        $query .= " ( ";
        $query .= "     YEAR, ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     SCHREGNO, ";
        $query .= "     PROV_FLG, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     SCHREGNO, ";
        $query .= "     '1', ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     KIN_RECORD_DAT KINREC ";
        $query .= " WHERE ";
        $query .= "     KINREC.YEAR = '".CTRL_YEAR."'";
        $query .= "     AND KINREC.CLASSCD || '-' || KINREC.SCHOOL_KIND || '-' || KINREC.CURRICULUM_CD || '-' || KINREC.SUBCLASSCD IN (SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '".CTRL_YEAR."' AND NAMECD1 = 'D065') ";
        $query .= "     AND KINREC.SCHREGNO IN( SELECT SCHREGNO ";
        $query .= "                             FROM SCHREG_REGD_DAT ";
        $query .= "                             WHERE YEAR = '".CTRL_YEAR."'";
        $query .= "                               AND SEMESTER='".$SEM[$model->field["SEMESTER"]]."'";
        $query .= "                               AND GRADE = '".$model->field["GRADE"]."' ";
        $query .= "                    )";
        $query .= "     AND KINREC.".$ITEM[$model->field["SEMESTER"]]." IS NOT NULL ";

        return $query;
    }

    //RECORD_PROV_FLG_DATのレコードを削除。
    function delRecordProvFlgDatKariNomi($model, $ITEM, $ITEM_KARINOMI, $SEM, $row) {
        $query  = "";
        $query .= " DELETE FROM RECORD_PROV_FLG_DAT PROV ";
        $query .= " WHERE ";
        $query .= " EXISTS ( ";
        $query .= "     SELECT ";
        $query .= "         'X' ";
        $query .= "     FROM ";
        $query .= "         KIN_RECORD_DAT KINREC ";
        $query .= "     WHERE ";
        $query .= "         KINREC.YEAR = '".$row["YEAR"]."'";
        $query .= "         AND KINREC.CLASSCD = '".$row["CLASSCD"]."'";
        $query .= "         AND KINREC.SCHOOL_KIND = '".$row["SCHOOL_KIND"]."'";
        $query .= "         AND KINREC.CURRICULUM_CD = '".$row["CURRICULUM_CD"]."'";
        $query .= "         AND KINREC.SUBCLASSCD = '".$row["SUBCLASSCD"]."'";
        $query .= "         AND KINREC.{$ITEM_KARINOMI[$model->field["SEMESTER"]]} IN ('1', '2') ";
        $query .= "         AND KINREC.SCHREGNO IN( SELECT SCHREGNO ";
        $query .= "                                 FROM SCHREG_REGD_DAT ";
        $query .= "                                 WHERE YEAR = '".$row["YEAR"]."'";
        $query .= "                                   AND SEMESTER='".$SEM[$model->field["SEMESTER"]]."'";
        $query .= "                                   AND GRADE = '".$row["GRADE"]."' ";
        $query .= "                                   AND HR_CLASS  = '".$row["HR_CLASS"]."' ";
        $query .= "                        )";
        $query .= "         AND PROV.YEAR = KINREC.YEAR ";
        $query .= "         AND PROV.CLASSCD = KINREC.CLASSCD ";
        $query .= "         AND PROV.SCHOOL_KIND = KINREC.SCHOOL_KIND ";
        $query .= "         AND PROV.CURRICULUM_CD = KINREC.CURRICULUM_CD ";
        $query .= "         AND PROV.SUBCLASSCD = KINREC.SUBCLASSCD ";
        $query .= "         AND PROV.SCHREGNO = KINREC.SCHREGNO ";
        $query .= " ) ";

        return $query;
    }

    //RECORD_PROV_FLG_DATのレコードを追加
    function insRecordProvFlgDatKariNomi($model, $ITEM, $ITEM_KARINOMI, $SEM, $row) {
        $query  = "";
        $query .= " INSERT INTO RECORD_PROV_FLG_DAT ";
        $query .= " ( ";
        $query .= "     YEAR, ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     SCHREGNO, ";
        $query .= "     PROV_FLG, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     SCHREGNO, ";
        $query .= "     '1', ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     KIN_RECORD_DAT KINREC ";
        $query .= " WHERE ";
        $query .= "     KINREC.YEAR = '".$row["YEAR"]."'";
        $query .= "     AND KINREC.CLASSCD = '".$row["CLASSCD"]."'";
        $query .= "     AND KINREC.SCHOOL_KIND = '".$row["SCHOOL_KIND"]."'";
        $query .= "     AND KINREC.CURRICULUM_CD = '".$row["CURRICULUM_CD"]."'";
        $query .= "     AND KINREC.SUBCLASSCD = '".$row["SUBCLASSCD"]."'";
        $query .= "     AND KINREC.{$ITEM_KARINOMI[$model->field["SEMESTER"]]} IN ('1', '2') ";
        $query .= "     AND KINREC.SCHREGNO IN( SELECT SCHREGNO ";
        $query .= "                             FROM SCHREG_REGD_DAT ";
        $query .= "                             WHERE YEAR = '".$row["YEAR"]."'";
        $query .= "                               AND SEMESTER='".$SEM[$model->field["SEMESTER"]]."'";
        $query .= "                               AND GRADE = '".$row["GRADE"]."' ";
        $query .= "                               AND HR_CLASS  = '".$row["HR_CLASS"]."' ";
        $query .= "                    )";
        $query .= "     AND KINREC.".$ITEM[$model->field["SEMESTER"]]." IS NOT NULL ";

        return $query;
    }

}
?>
