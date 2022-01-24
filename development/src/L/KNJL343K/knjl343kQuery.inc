<?php
class knjl343kQuery extends Query {
    //特別理由区分
    function getSpecialReasonDiv($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->ObjYear}' AND ";
        $query .= "     NAMECD1 = 'L017' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //試験区分
    function GetTestdiv($model){
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->ObjYear."' AND ";
        $query .= "     NAMECD1 = 'L003' ";

        return $query;
    }

    //一覧リスト---塾
    function GetPrischool($model){
        $query  = " SELECT ";
        $query .= "     PRISCHOOLCD, ";
        $query .= "     PRISCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     V_PRISCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->ObjYear."' ";
        $query .= " ORDER BY PRISCHOOLCD";

        return $query;
    }

    //中高判別
    function GetJorH(){
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOLNAME1 LIKE '%中学%' ";

        return $query;
    }

    //コース毎の基準点を取得
    function getBoderScore($model){
            //受験コースマスタ
        $query  = "WITH EXAM_COURSE AS ( ";
        $query .= "    SELECT COURSECD||MAJORCD||EXAMCOURSECD AS COURSE, ";
        $query .= "           EXAMCOURSE_NAME ";
        $query .= "    FROM   ENTEXAM_COURSE_MST ";
        $query .= "    WHERE  ENTEXAMYEAR='".$model->ObjYear."' ";
        $query .= "    ) ";
            //合格点マスタ
        $query .= ",EXAM_PASS AS ( ";
        $query .= "    SELECT COURSECD||MAJORCD||EXAMCOURSECD AS COURSE, ";
        $query .= "           BORDER_SCORE ";
        $query .= "    FROM   ENTEXAM_PASSINGMARK_MST ";
        $query .= "    WHERE  ENTEXAMYEAR='".$model->ObjYear."' AND TESTDIV='".$model->testdiv."' ";
        $query .= "    ) ";

            //メイン
        $query .= "SELECT T1.COURSE,VALUE(EXAMCOURSE_NAME,'') AS NAME,BORDER_SCORE AS SCORE ";
        $query .= "FROM   EXAM_COURSE T1 ";
        $query .= "       LEFT JOIN EXAM_PASS T2 ON T2.COURSE=T1.COURSE ";
        $query .= "ORDER BY T1.COURSE DESC ";

        return $query;
    }

    //ＣＳＶ出力
    function getOutputQueryCsv($model){
            //志願者基礎データ
        $query  = "WITH EXAM_BASE AS ( ";
        $query .= "    SELECT TESTDIV,EXAMNO,NAME,NAME_KANA,SEX,DESIREDIV,NATPUBPRIDIV,FS_CD,PS_CD, ";
        $query .= "           CASE WHEN APPROVAL_FLG = '1' THEN APPROVAL_FLG ELSE '2' END AS APPROVAL_FLG, ";       //2005.12.20 m-yama add FS_CD
        $query .= "           PROCEDUREDIV,ENTDIV,JUDGEMENT,SUC_COURSECD||SUC_MAJORCD||SUC_COURSECODE AS SUC_COURSE ";  //2005.12.20 m-yama add PROCEDUREDIV,ENTDIV
        $query .= "    FROM   ENTEXAM_APPLICANTBASE_DAT ";
        $query .= "    WHERE  ENTEXAMYEAR = '".$model->ObjYear."' AND ";
        if ($model->special_reason_div != '9') {
            $query .= "       SPECIAL_REASON_DIV = '{$model->special_reason_div}' AND ";
        }
        $query .= "           TESTDIV = '".$model->testdiv."' AND ";
        $query .= "           PS_CD IN ('".implode($model->select_data["selectdata"],"','")."') ";
        if ($model->output == "on")
            $query .= "           AND APPROVAL_FLG = '1'";
        $query .= "    ) ";
            //志願者得点データ
        $query .= ",EXAM_SCORE AS ( ";
        $query .= "    SELECT EXAMNO,TESTSUBCLASSCD,A_SCORE ";
        $query .= "    FROM   ENTEXAM_SCORE_DAT ";
        $query .= "    WHERE  ENTEXAMYEAR = '".$model->ObjYear."' AND ";
        $query .= "           TESTDIV = '".$model->testdiv."' AND ";
        $query .= "           A_SCORE IS NOT NULL ";
        $query .= "    ) ";
            //志望区分マスタ：志望連番MAX値
        $query .= ",WISHDIV AS ( ";
        $query .= "    SELECT DESIREDIV,WISHNO,COURSECD||MAJORCD||EXAMCOURSECD AS COURSE ";
        $query .= "    FROM   ENTEXAM_WISHDIV_MST ";
        $query .= "    WHERE  ENTEXAMYEAR = '".$model->ObjYear."' AND ";
        $query .= "           TESTDIV = '".$model->testdiv."' AND ";
        $query .= "           (DESIREDIV,WISHNO) IN ( ";
        $query .= "                SELECT DESIREDIV,MAX(WISHNO) AS MAX_WISHNO ";
        $query .= "                FROM   ENTEXAM_WISHDIV_MST ";
        $query .= "                WHERE  ENTEXAMYEAR = '".$model->ObjYear."' AND ";
        $query .= "                       TESTDIV = '".$model->testdiv."' ";
        $query .= "                GROUP BY DESIREDIV ";
        $query .= "                ) ";
        $query .= "    ) ";
            //最終判定コース
        $query .= ",LAST_COURSE AS ( ";
        $query .= "    SELECT T1.EXAMNO, ";
        $query .= "           CASE WHEN T1.JUDGEMENT = '7' THEN T4.COURSE ";//否
        $query .= "                WHEN T1.SUC_COURSE IS NOT NULL THEN T1.SUC_COURSE ";//合
        $query .= "                ELSE NULL END AS COURSE ";
        $query .= "    FROM   EXAM_BASE T1 ";
        $query .= "           LEFT JOIN WISHDIV T4 ON T4.DESIREDIV=T1.DESIREDIV ";
        $query .= "    ) ";
            //満点マスタ：採用区分
        $query .= ",PERFECT AS ( ";
        $query .= "    SELECT COURSECD||MAJORCD||EXAMCOURSECD AS COURSE, ";
        $query .= "           TESTSUBCLASSCD,ADOPTIONDIV ";
        $query .= "    FROM   ENTEXAM_PERFECT_MST ";
        $query .= "    WHERE  ENTEXAMYEAR='".$model->ObjYear."' AND ";
        $query .= "           TESTDIV='".$model->testdiv."' AND ";
        $query .= "           VALUE(A_TOTAL_FLG,'1') = '1' ";//A配点集計フラグ（0:集計に含めない／1:集計に含める）
        $query .= "    ) ";
            //成績：後期（合計・平均・順位の計算用）
        $query .= ",BASE_SCORE AS ( ";
        $query .= "    SELECT T1.EXAMNO,T1.COURSE,T2.TESTSUBCLASSCD,T2.A_SCORE ";
        $query .= "    FROM   LAST_COURSE T1 ";
        $query .= "           LEFT JOIN EXAM_SCORE T2 ON T2.EXAMNO = T1.EXAMNO ";
        $query .= "    ) ";
        $query .= ",BASE_SCORE2 AS ( ";
        $query .= "    SELECT T1.EXAMNO,T1.COURSE,T1.TESTSUBCLASSCD,T1.A_SCORE,T2.ADOPTIONDIV ";
        $query .= "    FROM   BASE_SCORE T1 ";
        $query .= "           LEFT JOIN PERFECT T2 ON T2.COURSE=T1.COURSE AND T2.TESTSUBCLASSCD=T1.TESTSUBCLASSCD ";
        $query .= "    ) ";
        $query .= ",BASE_SCORE3 AS ( ";
        $query .= "    SELECT EXAMNO,COURSE,TESTSUBCLASSCD,A_SCORE ";
        $query .= "    FROM   BASE_SCORE2 ";
        $query .= "    WHERE  ADOPTIONDIV='0' ";
        $query .= "    UNION ";
        $query .= "    SELECT EXAMNO,COURSE,'0' AS TESTSUBCLASSCD,MAX(A_SCORE) AS A_SCORE ";
        $query .= "    FROM   BASE_SCORE2 ";
        $query .= "    WHERE  ADOPTIONDIV='1' ";//アラカルト
        $query .= "    GROUP BY EXAMNO,COURSE ";
        $query .= "    ) ";
            //成績：後期（合計・平均・順位）
        $query .= ",SCORE_S AS ( ";
        $query .= "    SELECT EXAMNO,COURSE, ";
        $query .= "           SUM(A_SCORE) AS SCORE_SUM, ";
        $query .= "           DECIMAL(ROUND(AVG(FLOAT(A_SCORE))*10,0)/10,5,1) AS SCORE_AVG, ";
        $query .= "           RANK() OVER (PARTITION BY COURSE ORDER BY SUM(A_SCORE) DESC) AS SCORE_RNK ";
        $query .= "    FROM   BASE_SCORE3 ";
        $query .= "    GROUP BY EXAMNO,COURSE ";
        $query .= "    ) ";
            //成績：前期（素点）
        $query .= ",SCORE AS ( ";
        $query .= "    SELECT T1.EXAMNO, ";
        $query .= "           SUM(CASE WHEN T2.TESTSUBCLASSCD = '1' THEN T2.A_SCORE ELSE NULL END) AS SCORE1, ";
        $query .= "           SUM(CASE WHEN T2.TESTSUBCLASSCD = '2' THEN T2.A_SCORE ELSE NULL END) AS SCORE2, ";
        $query .= "           SUM(CASE WHEN T2.TESTSUBCLASSCD = '3' THEN T2.A_SCORE ELSE NULL END) AS SCORE3, ";
        $query .= "           SUM(CASE WHEN T2.TESTSUBCLASSCD = '4' THEN T2.A_SCORE ELSE NULL END) AS SCORE4 ";
        $query .= "    FROM   EXAM_BASE T1, EXAM_SCORE T2 ";
        $query .= "    WHERE  T2.EXAMNO=T1.EXAMNO ";
        $query .= "    GROUP BY T1.EXAMNO ";
        $query .= "    ) ";

            //メイン
        $query .= "SELECT T1.PS_CD, ";
        $query .= "       T6.PRISCHOOL_NAME, ";
        $query .= "       T1.EXAMNO, ";
        $query .= "       CASE WHEN T1.SEX = '2' THEN '*' ELSE NULL END AS SEX_NAME, ";
        $query .= "       T1.NAME, ";
        $query .= "       CASE WHEN T4.EXAMCOURSE_ABBV IS NOT NULL THEN '合' ";
        $query .= "            WHEN T1.JUDGEMENT = '7' THEN '否' ";
        $query .= "            WHEN T2.SCORE1 IS NULL AND T2.SCORE2 IS NULL AND ";
        $query .= "                 T2.SCORE3 IS NULL AND T2.SCORE4 IS NULL THEN '欠' ";
        $query .= "            ELSE NULL END AS JUDGE2, ";
        $query .= "       T4.EXAMCOURSE_ABBV AS JUDGE1, ";
        $query .= "       CASE WHEN T1.JUDGEMENT = '7' THEN T2.SCORE1 ELSE NULL END AS SCORE1, ";
        $query .= "       CASE WHEN T1.JUDGEMENT = '7' THEN T2.SCORE2 ELSE NULL END AS SCORE2, ";
        $query .= "       CASE WHEN T1.JUDGEMENT = '7' THEN T2.SCORE3 ELSE NULL END AS SCORE3, ";
        $query .= "       CASE WHEN T1.JUDGEMENT = '7' THEN T2.SCORE4 ELSE NULL END AS SCORE4, ";
        $query .= "       CASE WHEN T1.JUDGEMENT = '7' THEN T5.SCORE_SUM ELSE NULL END AS SCORE_SUM, ";
        $query .= "       T1.APPROVAL_FLG, ";   //2005.12.20 m-yama
        $query .= "       T1.FS_CD, ";          //2005.12.20 m-yama
        $query .= "       T7.FINSCHOOL_NAME, "; //2005.12.20 m-yama
        $query .= "       CASE WHEN t1.PROCEDUREDIV = '2' AND t1.ENTDIV = '1' THEN '3' ";       //2005.12.20 m-yama
        $query .= "            WHEN t1.PROCEDUREDIV = '2' AND t1.ENTDIV = '2' THEN '2' ";       //2005.12.20 m-yama
        $query .= "            WHEN (t1.PROCEDUREDIV < '2') OR ";                               //2005.12.20 m-yama
        $query .= "                 (t1.PROCEDUREDIV = '2' AND t1.ENTDIV is null) THEN '1' ";   //2005.12.20 m-yama
        $query .= "            ELSE '' END AS ENTDIV ";                                         //2005.12.20 m-yama
        $query .= "FROM   EXAM_BASE T1 ";
        $query .= "       LEFT JOIN SCORE T2 ON T2.EXAMNO=T1.EXAMNO ";
        $query .= "       LEFT JOIN ENTEXAM_COURSE_MST T4 ON T4.ENTEXAMYEAR='".$model->ObjYear."' AND  ";
        $query .= "                                    T4.COURSECD||T4.MAJORCD||T4.EXAMCOURSECD = T1.SUC_COURSE ";
        $query .= "       LEFT JOIN NAME_MST N1 ON N1.NAMECD1='L003' AND N1.NAMECD2=T1.TESTDIV ";
        $query .= "       LEFT JOIN SCORE_S T5 ON T5.EXAMNO=T1.EXAMNO ";
        $query .= "       LEFT JOIN PRISCHOOL_MST T6 ON T6.PRISCHOOLCD=T1.PS_CD ";
        $query .= "       LEFT JOIN FINSCHOOL_MST T7 ON T7.FINSCHOOLCD=T1.FS_CD ";  //2005.12.20 m-yama
        $query .= "ORDER BY T1.PS_CD,T1.EXAMNO ";

        return $query;
    }

}
?>
