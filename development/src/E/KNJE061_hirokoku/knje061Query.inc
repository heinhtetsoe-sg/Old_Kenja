<?php

require_once('for_php7.php');

class knje061query extends Query {

    // 成績テーブルを判断するためのフラグを取得(法政・自修館・その他)
    function getNameMst()
    {
        $query  = "";
        $query .= "SELECT ";
        $query .= "    NAMECD1, ";
        $query .= "    NAMECD2, ";
        $query .= "    NAME1, "; // 学校区分
        $query .= "    NAMESPARE1 "; // 1:RECORD_SCORE_DAT, null:RECORD_DAT
        $query .= "FROM ";
        $query .= "    NAME_MST ";
        $query .= "WHERE ";
        $query .= "    NAMECD1='Z010' AND ";
        $query .= "    NAMECD2='00' ";

        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

//echo "NAME1=" .$rtnRow["NAME1"] .", NAMESPARE1=" .$rtnRow["NAMESPARE1"];
        return $rtnRow;
    }

    //------------------------------------------------------------
    //科目数の取得
    //------------------------------------------------------------
    function getSubclasses($data)
    {
        $query .= " SELECT SUBCLASSES ";
        $query .= "   FROM CLASS_MST ";
        $query .= "  WHERE CLASSCD = '".sprintf("%02d",$data["CLASSCD"]) ."'";

        return $query;
    }

    //------------------------------------------------------------
    //コースを取得
    //------------------------------------------------------------
    function selectCourse($model)
    {
        $query  = " SELECT DISTINCT T1.coursecode, T2.coursecodename ";
        $query .= "   FROM ".$model->RegdTable." T1,";
        $query .= "         coursecode_mst T2 ";
        $query .= "  WHERE T1.coursecode = T2.coursecode";
        $query .= "    AND T1.grade      = '". $model->annual ."'";
        $query .= "    AND T1.hr_class   = '". $model->hr_class ."'";
        $query .= "    AND T1.year       = '".CTRL_YEAR ."'";
        $query .= "    AND T1.semester   = '".CTRL_SEMESTER ."'";
        $query .= "  ORDER BY T1.coursecode ";
        return $query;
    }

    //------------------------------------------------------------
    //生徒取得
    //------------------------------------------------------------
    function selectSchregno($model)
    {
        $query  = " SELECT T1.attendno, T1.schregno, T2.name_show";
        $query .= "   FROM schreg_regd_dat T1, schreg_base_mst T2 ";
        $query .= "  WHERE T1.year       = '".CTRL_YEAR ."'";
        $query .= "    AND T1.semester   = '".CTRL_SEMESTER ."'";
        $query .= "    AND T1.grade     = '". $model->annual ."'";
        $query .= "    AND T1.hr_class   = '". $model->hr_class ."'";
        $query .= "    AND T1.coursecode = '". $model->coursecode ."'";
        $query .= "    AND T1.schregno   = T2.schregno";
        $query .= "  ORDER BY T1.attendno";
        return $query;
    }

    //------------------------------------------------------------
    //学年ごとの組を取得
    //------------------------------------------------------------
    function selectQueryHRClass($grade){
        $query  = "SELECT hr_class FROM schreg_regd_hdat";
        $query .= " WHERE year     = '".CTRL_YEAR."'";
        $query .= "   AND semester = '".CTRL_SEMESTER."'";
        $query .= "   AND grade    = '".sprintf("%02d",$grade)."'";
        return $query;	
    }

    //------------------------------------------------------------
    //学年（年次）コンボボックス用
    //------------------------------------------------------------
    function selectQueryAnnual($model)
    {
        $query  = "SELECT grade AS annual FROM schreg_regd_hdat";
        $query .= " WHERE year     = '".CTRL_YEAR."'";
        $query .= "   AND semester = '".CTRL_SEMESTER."'";
        $query .= " GROUP BY grade"; 

        return $query;
    }
        
    //------------------------------------------------------------
    //成績データ新規作成
    //------------------------------------------------------------
    function addRecord_Schreg_Studyrec_Dat($model)
    {
        //生成範囲の条件を分ける
        if ($model->field["RANGE"] == 1){
            $OPERATOR = "=";
        }else{
            $OPERATOR = "<=";
        }

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //DELETE(本校区分:0)
        $query = " DELETE FROM SCHREG_STUDYREC_DAT T1 ";
        $query .= " WHERE T1.schoolcd = '0'";
        $query .= "   AND T1.YEAR ".$OPERATOR."'" .CTRL_YEAR ."'";
        $query .= "   AND EXISTS (SELECT 'X' FROM ".$model->RegdTable." W ";
        $query .= "                WHERE W.YEAR     = '" .CTRL_YEAR ."'";
        $query .= "                  AND W.SEMESTER = '" .CTRL_SEMESTER ."'";
        $query .= "                  AND W.grade   = '" .$model->annual  ."'";
        if (strlen($model->hr_class))   $query .= "              AND W.hr_class   = '".$model->hr_class."'";
        if (strlen($model->coursecode)) $query .= "              AND W.coursecode = '".$model->coursecode."'";
        if (strlen($model->schregno))   $query .= "              AND W.schregno   = '".$model->schregno."'";
        $query .= "                  AND W.SCHREGNO = T1.SCHREGNO ) ";
        $db->query($query);

        //INSERT(本校区分:0)
        $db->query(knje061Query::InsertStudyrecQuery($model, "0", "off"));

        // DELETE(本校区分:2)
        $query = " DELETE FROM SCHREG_STUDYREC_DAT T1 ";
        $query .= " WHERE T1.schoolcd = '2'";
        $query .= "   AND T1.YEAR ".$OPERATOR."'" .CTRL_YEAR ."'";
        $query .= "   AND EXISTS (SELECT 'X' FROM ".$model->RegdTable." W ";
        $query .= "                WHERE W.YEAR     = '" .CTRL_YEAR ."'";
        $query .= "                  AND W.SEMESTER = '" .CTRL_SEMESTER ."'";
        $query .= "                  AND W.grade   = '" .$model->annual  ."'";
        if (strlen($model->hr_class))   $query .= "              AND W.hr_class   = '".$model->hr_class."'";
        if (strlen($model->coursecode)) $query .= "              AND W.coursecode = '".$model->coursecode."'";
        if (strlen($model->schregno))   $query .= "              AND W.schregno   = '".$model->schregno."'";
        $query .= "                  AND W.SCHREGNO = T1.SCHREGNO ) ";
        $db->query($query);

        //INSERT(本校区分:2)
        $db->query(knje061Query::InsertStudyrecQuery($model, "2", "off"));

        // DELETE(備考データ)
        $query = " DELETE FROM STUDYRECREMARK_DAT T1 ";
        $query .= " WHERE T1.YEAR ".$OPERATOR."'" .CTRL_YEAR ."'";
        $query .= "   AND EXISTS (SELECT 'X' FROM ".$model->RegdTable." W ";
        $query .= "                WHERE W.YEAR     = '" .CTRL_YEAR ."'";
        $query .= "                  AND W.SEMESTER = '" .CTRL_SEMESTER ."'";
        $query .= "                  AND W.grade   = '" .$model->annual  ."'";
        if (strlen($model->hr_class))   $query .= "              AND W.hr_class   = '".$model->hr_class."'";
        if (strlen($model->coursecode)) $query .= "              AND W.coursecode = '".$model->coursecode."'";
        if (strlen($model->schregno))   $query .= "              AND W.schregno   = '".$model->schregno."'";
        $query .= "                  AND W.SCHREGNO = T1.SCHREGNO ) ";
        $db->query($query);

        //INSERT(備考データ)
        knje061Query::InsertStudyrec_Remark($db, $model, "off");

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    //------------------------------------------------------------
    //成績データ追加作成
    //------------------------------------------------------------
    function addRecord_Schreg_Studyrec_Dat2($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //INSERT(本校区分:0)
        $db->query(knje061Query::InsertStudyrecQuery($model, "0", "on"));

        //INSERT(本校区分:2)
        $db->query(knje061Query::InsertStudyrecQuery($model, "2", "on"));

        //INSERT(備考データ)
        knje061Query::InsertStudyrec_Remark($db, $model, "on");

        $db->commit();
        Query::dbCheckIn($db);    
    }

    //------------------------------------------------------------
    //成績データ上書作成
    //------------------------------------------------------------
    function addRecord_Schreg_Studyrec_Dat3($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //作成するデータを一旦削除する(本校区分:0)
        $db->query(knje061Query::DeleteStudyrecQuery($model, "0"));

        //INSERT(本校区分:0)
        $db->query(knje061Query::InsertStudyrecQuery($model, "0", "on"));

        //作成するデータを一旦削除する(本校区分:2)
        $db->query(knje061Query::DeleteStudyrecQuery($model, "2"));

        //INSERT(本校区分:2)
        $db->query(knje061Query::InsertStudyrecQuery($model, "2", "on"));

        //作成するデータを一旦削除する(備考データ)
        $db->query(knje061Query::DeleteStudyrec_Remark($model));

        //INSERT(備考データ)
        knje061Query::InsertStudyrec_Remark($db, $model, "on");

        $db->commit();
        Query::dbCheckIn($db);
    }

    //学籍学習記録データ削除クエリ文 === 作成するデータを一旦削除する
    function DeleteStudyrecQuery($model, $schoolcd) {
        $query  = "";
        $query .= " DELETE FROM SCHREG_STUDYREC_DAT ";
        $query .= "  WHERE (SCHOOLCD,YEAR,SCHREGNO,ANNUAL,CLASSCD,SUBCLASSCD) IN ";
        $query .= " (";
        $query .= " select tbl1.SCHOOLCD, ";
        $query .= "        tbl1.YEAR, ";
        $query .= "        tbl1.SCHREGNO, ";
        $query .= "        tbl1.ANNUAL, ";
        $query .= "        tbl1.CLASSCD, ";
        $query .= "        tbl1.SUBCLASSCD ";
        $query .= "   from ( ";

        if ($schoolcd == "0") $query .= knje061Query::InsertStudyrecQuerySchoolcd0($model, "off");
        if ($schoolcd == "2") $query .= knje061Query::InsertStudyrecQuerySchoolcd2($model, "off");

        $query .= "        ) tbl1 ";
        $query .= " ) ";

        return $query;
    }

    //学籍学習記録データ追加クエリ文
    function InsertStudyrecQuery($model, $schoolcd, $not_exists) {
        $query  = "";
        $query .= " insert into SCHREG_STUDYREC_DAT( ";
        $query .= " SCHOOLCD, ";
        $query .= " YEAR, ";
        $query .= " SCHREGNO, ";
        $query .= " ANNUAL, ";
        $query .= " CLASSCD, ";
        $query .= " SUBCLASSCD, ";
        $query .= " VALUATION, ";
        $query .= " GET_CREDIT, ";
        $query .= " ADD_CREDIT, ";
        $query .= " COMP_CREDIT, ";
        $query .= " REGISTERCD ";
        $query .= " ) ";

        if ($schoolcd == "0") $query .= knje061Query::InsertStudyrecQuerySchoolcd0($model, $not_exists);
        if ($schoolcd == "2") $query .= knje061Query::InsertStudyrecQuerySchoolcd2($model, $not_exists);
//echo "---start---schoolcd = " .$schoolcd ." not_exists = " .$not_exists ."<BR>";
//echo $query;
//echo "<BR>---end---schoolcd = " .$schoolcd ." not_exists = " .$not_exists ."<BR>";

        return $query;
    }

    //学籍学習記録データ追加クエリ文 === 本校区分:0
    function InsertStudyrecQuerySchoolcd0($model, $not_exists) {
        // 生成範囲の条件を分ける
        $OPERATOR = ($model->field["RANGE"] == 1) ? "=" : "<=";
        $query  = "";
        $query .= " select '0' as SCHOOLCD  ";
        $query .= "        ,tbl.YEAR  ";
        $query .= "        ,tbl.SCHREGNO  ";
        $query .= "        ,tbl.ANNUAL  ";
        $query .= "        ,tbl.CLASSCD  ";
        $query .= "        ,tbl.SUBCLASSCD  ";
        $query .= "        ,sum(tbl.VALUATION) as VALUATION ";
        $query .= "        ,sum(tbl.GET_CREDIT) as GET_CREDIT ";
        $query .= "        ,sum(tbl.ADD_CREDIT) as ADD_CREDIT ";
        $query .= "        ,sum(tbl.COMP_CREDIT) as COMP_CREDIT ";
        $query .= "        ,'".STAFFCD."' as REGISTERCD ";
        $query .= "   from ( ";

        // a.成績データ
        $query .= " select tbl1.YEAR  ";
        $query .= "        ,tbl1.SCHREGNO  ";
        $query .= "        ,tbl1.ANNUAL  ";
        $query .= "        ,substr(tbl1.SUBCLASSCD,1,2) as CLASSCD  ";
        $query .= "        ,tbl1.SUBCLASSCD  ";
        $query .= "        ,case when N1.NAMECD2 = substr(tbl1.SUBCLASSCD,1,2) then null else tbl1.VALUATION end AS VALUATION  ";
        $query .= "        ,tbl1.GET_CREDIT  ";
        $query .= "        ,tbl1.ADD_CREDIT  ";
        $query .= "        ,tbl1.COMP_CREDIT  ";
        $query .= "   from ( ";

        // 成績席次データ＋成績得点データ・・・自修館／法政の場合
        if ($model->recordTableDiv == "1") {
        $query .= "SELECT  T1.YEAR, ";
        $query .= "        T1.SUBCLASSCD, ";
        $query .= "        T1.SCHREGNO, ";
        $query .= "        T5.ANNUAL, ";
            if ($model->schoolName == "HOUSEI") {
        $query .= "        T3.ASSESSLEVEL AS VALUATION, ";
            } else {
        $query .= "        T1.SCORE AS VALUATION, ";
            }
        $query .= "        T2.GET_CREDIT, ";
        $query .= "        T2.ADD_CREDIT, ";
        $query .= "        T2.COMP_CREDIT ";
        $query .= "  FROM  RECORD_RANK_DAT T1 ";
        $query .= "        LEFT JOIN RECORD_SCORE_DAT T2 ON T2.YEAR = T1.YEAR ";
        $query .= "                                     AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "                                     AND T2.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "                                     AND T2.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "                                     AND T2.SCORE_DIV = '00' ";
        $query .= "                                     AND T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "                                     AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "        LEFT JOIN ASSESS_MST T3 ON T3.ASSESSCD = '3' ";
        $query .= "                               AND T1.SCORE BETWEEN T3.ASSESSLOW AND T3.ASSESSHIGH ";
        $query .= "        LEFT JOIN (SELECT W2.YEAR, W2.SCHREGNO, MAX(W2.ANNUAL) AS ANNUAL ";
        $query .= "                     FROM SCHREG_REGD_DAT W2 ";
        $query .= "                    WHERE W2.YEAR ".$OPERATOR." '".CTRL_YEAR."' ";
        $query .= "                      AND EXISTS (SELECT 'X' FROM SCHREG_REGD_DAT W1 ";
        $query .= "                                   WHERE W1.YEAR = '".CTRL_YEAR."' ";
        $query .= "                                     AND W1.SEMESTER = '" .CTRL_SEMESTER ."' ";
        $query .= "                                     AND W1.GRADE = '" .$model->annual ."' ";
        if (strlen($model->hr_class))   $query .= "     AND W1.HR_CLASS = '" .$model->hr_class."' ";
        if (strlen($model->coursecode)) $query .= "     AND W1.COURSECODE = '". $model->coursecode ."' ";
        if (strlen($model->schregno))   $query .= "     AND W1.SCHREGNO = '". $model->schregno ."' ";
        $query .= "                                     AND W1.SCHREGNO = W2.SCHREGNO) ";
        $query .= "                   GROUP BY W2.YEAR, W2.SCHREGNO ";
        $query .= "                  ) T5 ON T1.YEAR = T5.YEAR AND T1.SCHREGNO = T5.SCHREGNO ";
        $query .= " WHERE  T1.YEAR ".$OPERATOR." '".CTRL_YEAR."' ";
        $query .= "   AND  T1.SEMESTER = '9' ";
        $query .= "   AND  T1.TESTKINDCD = '99' ";
        $query .= "   AND  T1.TESTITEMCD = '00' ";
        $query .= "   AND  T1.SUBCLASSCD NOT IN ('333333','555555','999999') ";
        $query .= "   AND  EXISTS (SELECT 'X' FROM SCHREG_REGD_DAT W1 ";
        $query .= "                 WHERE W1.YEAR = '".CTRL_YEAR."' ";
        $query .= "                   AND W1.SEMESTER = '" .CTRL_SEMESTER ."' ";
        $query .= "                   AND W1.GRADE = '" .$model->annual ."' ";
        if (strlen($model->hr_class))   $query .= " AND W1.HR_CLASS = '" .$model->hr_class."' ";
        if (strlen($model->coursecode)) $query .= " AND W1.COURSECODE = '". $model->coursecode ."' ";
        if (strlen($model->schregno))   $query .= " AND W1.SCHREGNO = '". $model->schregno ."' ";
        $query .= "                   AND W1.SCHREGNO = T1.SCHREGNO) ";
        $query .= "   AND  NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT S1 ";
        $query .= "                     WHERE S1.YEAR = T1.YEAR  ";
        $query .= "                       AND S1.ATTEND_SUBCLASSCD = T1.SUBCLASSCD) ";

            // 法政の場合のみ
            if ($model->schoolName == "HOUSEI") {
        // 合併先科目(固定)
        $query .= "UNION ALL ";
        $query .= "SELECT  TT1.YEAR, ";
        $query .= "        TT1.COMBINED_SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "        TT1.SCHREGNO, ";
        $query .= "        T5.ANNUAL, ";
//      $query .= "        TT1.SCORE, ";
        $query .= "        T3.ASSESSLEVEL AS VALUATION, ";
        $query .= "        CASE WHEN 19 < TT1.SCORE THEN T6.CREDITS ";
        $query .= "             WHEN 0 < TT1.SCORE THEN 0 ";
        $query .= "             ELSE NULL END AS GET_CREDIT, ";
        $query .= "        CASE WHEN TT1.SCHREGNO IS NULL THEN 0 ";
        $query .= "             ELSE NULL END AS ADD_CREDIT, ";
        $query .= "        CASE WHEN 0 < TT1.SCORE THEN T6.CREDITS ";
        $query .= "             ELSE NULL END AS COMP_CREDIT ";
        $query .= "  FROM  ( ";
        $query .= "    SELECT  T1.YEAR, ";
        $query .= "            S1.COMBINED_SUBCLASSCD, ";
        $query .= "            T1.SCHREGNO, ";
        $query .= "            round(avg(float(T1.SCORE)),0) AS SCORE ";
        $query .= "      FROM  RECORD_RANK_DAT T1 ";
        $query .= "            LEFT JOIN RECORD_SCORE_DAT T2 ON T2.YEAR = T1.YEAR ";
        $query .= "                                         AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "                                         AND T2.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "                                         AND T2.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "                                         AND T2.SCORE_DIV = '00' ";
        $query .= "                                         AND T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "                                         AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "           ,SUBCLASS_REPLACE_COMBINED_DAT S1 ";
        $query .= "     WHERE  T1.YEAR ".$OPERATOR." '".CTRL_YEAR."' ";
        $query .= "       AND  T1.SEMESTER = '9' ";
        $query .= "       AND  T1.TESTKINDCD = '99' ";
        $query .= "       AND  T1.TESTITEMCD = '00' ";
        $query .= "       AND  T1.SUBCLASSCD NOT IN ('333333','555555','999999') ";
        $query .= "       AND  EXISTS (SELECT 'X' FROM SCHREG_REGD_DAT W1 ";
        $query .= "                     WHERE W1.YEAR = '".CTRL_YEAR."' ";
        $query .= "                       AND W1.SEMESTER = '" .CTRL_SEMESTER ."' ";
        $query .= "                       AND W1.GRADE = '" .$model->annual ."' ";
        if (strlen($model->hr_class))   $query .= " AND W1.HR_CLASS = '" .$model->hr_class."' ";
        if (strlen($model->coursecode)) $query .= " AND W1.COURSECODE = '". $model->coursecode ."' ";
        if (strlen($model->schregno))   $query .= " AND W1.SCHREGNO = '". $model->schregno ."' ";
        $query .= "                       AND W1.SCHREGNO = T1.SCHREGNO) ";
        $query .= "       AND  S1.YEAR = T1.YEAR ";
        $query .= "       AND  S1.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "       AND  S1.CALCULATE_CREDIT_FLG = '1' "; // 固定＝１
        $query .= "    GROUP BY T1.YEAR, S1.COMBINED_SUBCLASSCD, T1.SCHREGNO ";
        $query .= "    ) TT1 ";
        $query .= "    LEFT JOIN ASSESS_MST T3 ON T3.ASSESSCD = '3' ";
        $query .= "                           AND TT1.SCORE BETWEEN T3.ASSESSLOW AND T3.ASSESSHIGH ";
        $query .= "    LEFT JOIN (SELECT W2.YEAR, W2.SCHREGNO, MAX(W2.ANNUAL) AS ANNUAL ";
        $query .= "                 FROM SCHREG_REGD_DAT W2 ";
        $query .= "                WHERE W2.YEAR ".$OPERATOR." '".CTRL_YEAR."' ";
        $query .= "                  AND EXISTS (SELECT 'X' FROM SCHREG_REGD_DAT W1 ";
        $query .= "                               WHERE W1.YEAR = '".CTRL_YEAR."' ";
        $query .= "                                 AND W1.SEMESTER = '" .CTRL_SEMESTER ."' ";
        $query .= "                                 AND W1.GRADE = '" .$model->annual ."' ";
        if (strlen($model->hr_class))   $query .= " AND W1.HR_CLASS = '" .$model->hr_class."' ";
        if (strlen($model->coursecode)) $query .= " AND W1.COURSECODE = '". $model->coursecode ."' ";
        if (strlen($model->schregno))   $query .= " AND W1.SCHREGNO = '". $model->schregno ."' ";
        $query .= "                                 AND W1.SCHREGNO = W2.SCHREGNO) ";
        $query .= "               GROUP BY W2.YEAR, W2.SCHREGNO ";
        $query .= "              ) T5 ON TT1.YEAR = T5.YEAR AND TT1.SCHREGNO = T5.SCHREGNO ";
        $query .= "    LEFT JOIN (SELECT DISTINCT W2.YEAR, W2.SCHREGNO, W3.SUBCLASSCD, W3.CREDITS ";
        $query .= "                 FROM SCHREG_REGD_DAT W2, ";
        $query .= "                      CREDIT_MST W3 ";
        $query .= "                WHERE W2.YEAR ".$OPERATOR." '".CTRL_YEAR."' ";
        $query .= "                  AND EXISTS (SELECT 'X' FROM SCHREG_REGD_DAT W1 ";
        $query .= "                               WHERE W1.YEAR = '".CTRL_YEAR."' ";
        $query .= "                                 AND W1.SEMESTER = '" .CTRL_SEMESTER ."' ";
        $query .= "                                 AND W1.GRADE = '" .$model->annual ."' ";
        if (strlen($model->hr_class))   $query .= " AND W1.HR_CLASS = '" .$model->hr_class."' ";
        if (strlen($model->coursecode)) $query .= " AND W1.COURSECODE = '". $model->coursecode ."' ";
        if (strlen($model->schregno))   $query .= " AND W1.SCHREGNO = '". $model->schregno ."' ";
        $query .= "                                 AND W1.SCHREGNO = W2.SCHREGNO) ";
        $query .= "                  AND W2.YEAR = W3.YEAR ";
        $query .= "                  AND W2.COURSECD = W3.COURSECD ";
        $query .= "                  AND W2.MAJORCD = W3.MAJORCD ";
        $query .= "                  AND W2.GRADE = W3.GRADE ";
        $query .= "                  AND W2.COURSECODE = W3.COURSECODE ";
        $query .= "              ) T6 ON TT1.YEAR = T6.YEAR AND TT1.SCHREGNO = T6.SCHREGNO AND TT1.COMBINED_SUBCLASSCD = T6.SUBCLASSCD ";

        // 合併先科目(加算)
        $query .= "UNION ALL ";
        $query .= "SELECT  TT1.YEAR, ";
        $query .= "        TT1.COMBINED_SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "        TT1.SCHREGNO, ";
        $query .= "        T5.ANNUAL, ";
//      $query .= "        TT1.SCORE, ";
        $query .= "        T3.ASSESSLEVEL AS VALUATION, ";
        $query .= "        TT1.GET_CREDIT, ";
        $query .= "        TT1.ADD_CREDIT, ";
        $query .= "        TT1.COMP_CREDIT ";
        $query .= "  FROM  ( ";
        $query .= "    SELECT  T1.YEAR, ";
        $query .= "            S1.COMBINED_SUBCLASSCD, ";
        $query .= "            T1.SCHREGNO, ";
        $query .= "            round(avg(float(T1.SCORE)),0) AS SCORE, ";
        $query .= "            SUM(T2.GET_CREDIT) AS GET_CREDIT, ";
        $query .= "            SUM(T2.ADD_CREDIT) AS ADD_CREDIT, ";
        $query .= "            SUM(T2.COMP_CREDIT) AS COMP_CREDIT ";
        $query .= "      FROM  RECORD_RANK_DAT T1 ";
        $query .= "            LEFT JOIN RECORD_SCORE_DAT T2 ON T2.YEAR = T1.YEAR ";
        $query .= "                                         AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "                                         AND T2.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "                                         AND T2.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "                                         AND T2.SCORE_DIV = '00' ";
        $query .= "                                         AND T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "                                         AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "           ,SUBCLASS_REPLACE_COMBINED_DAT S1 ";
        $query .= "     WHERE  T1.YEAR ".$OPERATOR." '".CTRL_YEAR."' ";
        $query .= "       AND  T1.SEMESTER = '9' ";
        $query .= "       AND  T1.TESTKINDCD = '99' ";
        $query .= "       AND  T1.TESTITEMCD = '00' ";
        $query .= "       AND  T1.SUBCLASSCD NOT IN ('333333','555555','999999') ";
        $query .= "       AND  EXISTS (SELECT 'X' FROM SCHREG_REGD_DAT W1 ";
        $query .= "                     WHERE W1.YEAR = '".CTRL_YEAR."' ";
        $query .= "                       AND W1.SEMESTER = '" .CTRL_SEMESTER ."' ";
        $query .= "                       AND W1.GRADE = '" .$model->annual ."' ";
        if (strlen($model->hr_class))   $query .= " AND W1.HR_CLASS = '" .$model->hr_class."' ";
        if (strlen($model->coursecode)) $query .= " AND W1.COURSECODE = '". $model->coursecode ."' ";
        if (strlen($model->schregno))   $query .= " AND W1.SCHREGNO = '". $model->schregno ."' ";
        $query .= "                       AND W1.SCHREGNO = T1.SCHREGNO) ";
        $query .= "       AND  S1.YEAR = T1.YEAR ";
        $query .= "       AND  S1.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "       AND  S1.CALCULATE_CREDIT_FLG = '2' "; // 加算＝２
        $query .= "    GROUP BY T1.YEAR, S1.COMBINED_SUBCLASSCD, T1.SCHREGNO ";
        $query .= "    ) TT1 ";
        $query .= "    LEFT JOIN ASSESS_MST T3 ON T3.ASSESSCD = '3' ";
        $query .= "                           AND TT1.SCORE BETWEEN T3.ASSESSLOW AND T3.ASSESSHIGH ";
        $query .= "    LEFT JOIN (SELECT W2.YEAR, W2.SCHREGNO, MAX(W2.ANNUAL) AS ANNUAL ";
        $query .= "                 FROM SCHREG_REGD_DAT W2 ";
        $query .= "                WHERE W2.YEAR ".$OPERATOR." '".CTRL_YEAR."' ";
        $query .= "                  AND EXISTS (SELECT 'X' FROM SCHREG_REGD_DAT W1 ";
        $query .= "                               WHERE W1.YEAR = '".CTRL_YEAR."' ";
        $query .= "                                 AND W1.SEMESTER = '" .CTRL_SEMESTER ."' ";
        $query .= "                                 AND W1.GRADE = '" .$model->annual ."' ";
        if (strlen($model->hr_class))   $query .= " AND W1.HR_CLASS = '" .$model->hr_class."' ";
        if (strlen($model->coursecode)) $query .= " AND W1.COURSECODE = '". $model->coursecode ."' ";
        if (strlen($model->schregno))   $query .= " AND W1.SCHREGNO = '". $model->schregno ."' ";
        $query .= "                                 AND W1.SCHREGNO = W2.SCHREGNO) ";
        $query .= "               GROUP BY W2.YEAR, W2.SCHREGNO ";
        $query .= "              ) T5 ON TT1.YEAR = T5.YEAR AND TT1.SCHREGNO = T5.SCHREGNO ";
            }//if

        // RECORD_DAT
        } else {
        $query .= "         select t1.YEAR ";
        $query .= "               ,t1.SUBCLASSCD ";
        $query .= "               ,t1.SCHREGNO ";
        $query .= "               ,t5.ANNUAL                                  ";
        $query .= "               ,round(avg(float(case when '90' <= substr(t1.SUBCLASSCD,1,2) then null else t1.GRAD_VALUE end)),0) AS VALUATION ";
        $query .= "               ,sum(t1.GET_CREDIT) AS GET_CREDIT ";
        $query .= "               ,sum(t1.ADD_CREDIT) AS ADD_CREDIT ";
        $query .= "               ,sum(t1.COMP_CREDIT) AS COMP_CREDIT ";
        $query .= "           from RECORD_DAT t1  ";
        $query .= "                LEFT JOIN (  ";
        $query .= "                    select DISTINCT YEAR,SCHREGNO,ANNUAL ";
        $query .= "                      from SCHREG_REGD_DAT ";
        $query .= "                     where YEAR ".$OPERATOR." '".CTRL_YEAR."' ";
        $query .= "                    ) t5 ON t1.YEAR = t5.YEAR and t1.SCHREGNO = t5.SCHREGNO ";
        $query .= "          where t1.YEAR ".$OPERATOR." '".CTRL_YEAR."' ";
        $query .= "            and t1.TAKESEMES = (select max(TAKESEMES) from RECORD_DAT t2 ";
        $query .= "                                 where t2.YEAR       = t1.YEAR  ";
        $query .= "                                   and t2.SUBCLASSCD = t1.SUBCLASSCD ";
        $query .= "                                   and t2.SCHREGNO   = t1.SCHREGNO) ";
        $query .= "            and not exists (select 'X' from SUBCLASS_REPLACE_COMBINED_DAT t3 ";
        $query .= "                             where t3.YEAR = t1.YEAR  ";
        $query .= "                               and t3.ATTEND_SUBCLASSCD = t1.SUBCLASSCD) ";
        $query .= "          GROUP BY t1.YEAR, t1.SCHREGNO, t5.ANNUAL, t1.SUBCLASSCD ";
        }//if
        $query .= "         ) tbl1 ";
        $query .= "         LEFT JOIN NAME_YDAT N1 ON N1.YEAR = tbl1.YEAR ";
        $query .= "                               AND N1.NAMECD1 = 'D008' ";
        $query .= "                               AND N1.NAMECD2 = substr(tbl1.SUBCLASSCD,1,2) ";
        $query .= "  where exists (select 'X' from ".$model->RegdTable." W1 ";
        $query .= "                 where W1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "                   and W1.SEMESTER = '" .CTRL_SEMESTER ."' ";
        $query .= "                   and W1.GRADE   = '" .$model->annual ."' ";
        if (strlen($model->hr_class))   $query .= " and W1.hr_class   = '" .$model->hr_class."' ";
        if (strlen($model->coursecode)) $query .= " and W1.coursecode = '". $model->coursecode ."' ";
        if (strlen($model->schregno))   $query .= " and W1.schregno   = '". $model->schregno ."' ";
        $query .= "                   and W1.SCHREGNO = tbl1.SCHREGNO ) ";


        // c.資格データ(ADD_CREDITのみ)---設定区分 = 1or2
        $query .= " union all ";
        $query .= " select tbl1.YEAR, ";
        $query .= "        tbl1.SCHREGNO, ";
        $query .= "        tbl1.ANNUAL, ";
        $query .= "        substr(tbl1.SUBCLASSCD,1,2) as CLASSCD, ";
        $query .= "        tbl1.SUBCLASSCD, ";
        $query .= "        tbl1.VALUATION, "; // null
        $query .= "        tbl1.GET_CREDIT, "; // null
        $query .= "        tbl1.ADD_CREDIT, ";
        $query .= "        tbl1.COMP_CREDIT "; // null
        $query .= "   from ( ";
        $query .= "         select t1.YEAR, t1.SCHREGNO, t1.SUBCLASSCD, t2.ANNUAL ";
        $query .= "               ,sum(case when t1.CONDITION_DIV = '3' then t1.CREDITS end) as VALUATION ";
        $query .= "               ,sum(case when t1.CONDITION_DIV = '3' then t1.CREDITS end) as GET_CREDIT ";
        $query .= "               ,sum(case when t1.CONDITION_DIV in ('1','2') then t1.CREDITS end) as COMP_CREDIT ";
        $query .= "               ,sum(case when t1.CONDITION_DIV in ('1','2') then t1.CREDITS end) as ADD_CREDIT ";
        $query .= "           from SCHREG_QUALIFIED_DAT t1, ";
        $query .= "                ( ";
        $query .= "                 select distinct year, schregno, annual ";
        $query .= "                   from schreg_regd_dat ";
        $query .= "                  where YEAR ".$OPERATOR." '".CTRL_YEAR."' ";
        $query .= "                ) t2 ";
        $query .= "          where t1.YEAR ".$OPERATOR." '".CTRL_YEAR."' ";
        $query .= "            and t1.CONDITION_DIV in ('1','2') "; // 設定区分 = 1or2
        $query .= "            and t1.CREDITS is not null ";
        $query .= "            and t2.year = t1.year ";
        $query .= "            and t2.schregno = t1.schregno ";
        $query .= "          group by t1.YEAR, t1.SCHREGNO, t1.SUBCLASSCD, t2.ANNUAL ";
        $query .= "        ) tbl1 ";
        $query .= "  where exists (select 'X' from ".$model->RegdTable." W1 ";
        $query .= "                 where W1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "                   and W1.SEMESTER = '" .CTRL_SEMESTER ."' ";
        $query .= "                   and W1.grade   = '" .$model->annual ."' ";
        if (strlen($model->hr_class))   $query .= " and W1.hr_class   = '" .$model->hr_class."' ";
        if (strlen($model->coursecode)) $query .= " and W1.coursecode = '". $model->coursecode ."' ";
        if (strlen($model->schregno))   $query .= " and W1.schregno   = '". $model->schregno ."' ";
        $query .= "                   and W1.SCHREGNO = tbl1.SCHREGNO ) ";

        $query .= "         ) tbl ";

        //既存レコードは除く
        if ($not_exists == "on") {
            $query .= " where NOT EXISTS (SELECT 'X' FROM schreg_studyrec_dat WK1
                                           WHERE WK1.schoolcd   = '0'
                                             AND WK1.year       = tbl.year
                                             AND WK1.schregno   = tbl.schregno
                                             AND WK1.annual     = tbl.annual
                                             AND WK1.classcd    = tbl.classcd
                                             AND WK1.subclasscd = tbl.subclasscd)";
        }

        $query .= "  group by ";
        $query .= "         tbl.YEAR  ";
        $query .= "        ,tbl.SCHREGNO  ";
        $query .= "        ,tbl.ANNUAL  ";
        $query .= "        ,tbl.CLASSCD  ";
        $query .= "        ,tbl.SUBCLASSCD  ";

        return $query;
    }

    //学籍学習記録データ追加クエリ文 === 本校区分:2
    function InsertStudyrecQuerySchoolcd2($model, $not_exists) {
        // 生成範囲の条件を分ける
        $OPERATOR = ($model->field["RANGE"] == 1) ? "=" : "<=";
        $query  = "";
        $query .= " select '2' as SCHOOLCD, ";
        $query .= "        tbl.YEAR, ";
        $query .= "        tbl.SCHREGNO, ";
        $query .= "        tbl.ANNUAL, ";
        $query .= "        tbl.CLASSCD, ";
        $query .= "        tbl.SUBCLASSCD, ";
        $query .= "        tbl.VALUATION, "; // null
        $query .= "        tbl.GET_CREDIT, ";
        $query .= "        tbl.ADD_CREDIT, "; // null
        $query .= "        tbl.COMP_CREDIT, "; // null
        $query .= "        '".STAFFCD."' as REGISTERCD ";
        $query .= "   from ( ";
        $query .= "         select t1.YEAR, t1.SCHREGNO, substr(t1.SUBCLASSCD,1,2) as CLASSCD, t1.SUBCLASSCD, t2.ANNUAL, ";
        $query .= "                sum(case when t1.CONDITION_DIV in ('1','2') then t1.CREDITS end) as VALUATION, ";
        $query .= "                sum(case when t1.CONDITION_DIV in ('1','2') then t1.CREDITS end) as ADD_CREDIT, ";
        $query .= "                sum(case when t1.CONDITION_DIV = '3' then t1.CREDITS end) as COMP_CREDIT, ";
        $query .= "                sum(case when t1.CONDITION_DIV = '3' then t1.CREDITS end) as GET_CREDIT ";
        $query .= "           from SCHREG_QUALIFIED_DAT t1, ";
        $query .= "                ( ";
        $query .= "                 select distinct year, schregno, annual ";
        $query .= "                   from schreg_regd_dat ";
        $query .= "                  where YEAR ".$OPERATOR." '".CTRL_YEAR."' ";
        $query .= "                ) t2 ";
        $query .= "          where t1.YEAR ".$OPERATOR." '".CTRL_YEAR."' ";
        $query .= "            and t1.CONDITION_DIV = '3' "; // 設定区分 = 3
        $query .= "            and t1.CREDITS is not null ";
        $query .= "            and t2.year = t1.year ";
        $query .= "            and t2.schregno = t1.schregno ";
        $query .= "          group by t1.YEAR, t1.SCHREGNO, substr(t1.SUBCLASSCD,1,2), t1.SUBCLASSCD, t2.ANNUAL ";
        $query .= "        ) tbl ";
        $query .= "  where exists (select 'X' from ".$model->RegdTable." W1 ";
        $query .= "                 where W1.YEAR     = '" .CTRL_YEAR ."' ";
        $query .= "                   and W1.SEMESTER = '" .CTRL_SEMESTER ."' ";
        $query .= "                   and W1.grade   = '" .$model->annual ."' ";
        if (strlen($model->hr_class))   $query .= " and W1.hr_class   = '" .$model->hr_class."' ";
        if (strlen($model->coursecode)) $query .= " and W1.coursecode = '". $model->coursecode ."' ";
        if (strlen($model->schregno))   $query .= " and W1.schregno   = '". $model->schregno ."' ";
        $query .= "                   and W1.SCHREGNO = tbl.SCHREGNO ) ";

        //既存レコードは除く
        if ($not_exists == "on") {
            $query .= "  AND NOT EXISTS  (SELECT 'X' FROM schreg_studyrec_dat WK1
                                           WHERE WK1.schoolcd   = '2'
                                             AND WK1.year       = tbl.year
                                             AND WK1.schregno   = tbl.schregno
                                             AND WK1.annual     = tbl.annual
                                             AND WK1.classcd    = tbl.classcd
                                             AND WK1.subclasscd = tbl.subclasscd)";
        }

        return $query;
    }

    //学籍学習記録「備考」データの削除 === 作成するデータを一旦削除する
    function DeleteStudyrec_Remark($model) {
        $query  = "";
        $query .= " DELETE FROM STUDYRECREMARK_DAT ";
        $query .= "  WHERE (YEAR,SCHREGNO,CLASSCD,SUBCLASSCD) IN ";
        $query .= " (";
        $query .= " select tbl1.YEAR, ";
        $query .= "        tbl1.SCHREGNO, ";
        $query .= "        tbl1.CLASSCD, ";
        $query .= "        tbl1.SUBCLASSCD ";
        $query .= "   from ( ";

        $query .= knje061Query::getInsertStudyrec_RemarkQuery($model, "off");

        $query .= "        ) tbl1 ";
        $query .= " ) ";

        return $query;
    }

    //学籍学習記録「備考」データの作成
    function InsertStudyrec_Remark($db, $model, $not_exists)
    {
        $opt = array();
        $cmp_key = $tmp_remark = "";
        $query  = "order by tbl.YEAR, tbl.SCHREGNO, tbl.CLASSCD, tbl.SUBCLASSCD ";
        $result = $db->query(knje061Query::getInsertStudyrec_RemarkQuery($model, $not_exists) .$query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($cmp_key != "" && $cmp_key != $row["YEAR"] .$row["SCHREGNO"] .$row["CLASSCD"] .$row["SUBCLASSCD"]) {
                $db->query(knje061Query::setInsertStudyrec_RemarkQuery($opt));
                $tmp_remark = $row["REMARK"];
//echo $opt["SCHREGNO"] .$opt["SUBCLASSCD"] .$opt["REMARK"] .strlen($opt["REMARK"]) ."<BR>";
//var_dump($opt);
            } else {
                // 備考が複数レコード存在する場合、足していく === 90バイトまで
                if (strlen($tmp_remark .$row["REMARK"]) < 91) $tmp_remark .= $row["REMARK"];
            }
            $row["REMARK"] = $tmp_remark;
            $cmp_key = $row["YEAR"] .$row["SCHREGNO"] .$row["CLASSCD"] .$row["SUBCLASSCD"];
            $opt = $row;
        }

        // 最後のレコードを処理
        if ($cmp_key != "") {
            $db->query(knje061Query::setInsertStudyrec_RemarkQuery($opt));
//echo $opt["SCHREGNO"] .$opt["SUBCLASSCD"] .$opt["REMARK"] .strlen($opt["REMARK"]) ."<BR>";
//var_dump($opt);
        }
    }

    //学籍学習記録「備考」データの作成
    function setInsertStudyrec_RemarkQuery($data)
    {
        $datas = array();
        $datas["YEAR"][TEXT]             = $data["YEAR"];
        $datas["SCHREGNO"][TEXT]         = $data["SCHREGNO"];
        $datas["CLASSCD"][TEXT]          = $data["CLASSCD"];
        $datas["SUBCLASSCD"][TEXT]       = $data["SUBCLASSCD"];
        $datas["REMARK"][TEXT]           = $data["REMARK"];
        $datas["REGISTERCD"][TEXT]       = STAFFCD;
//        $datas["UPDATED"][FUNC]          = "SYSDATE()";
        $query = Query::insertSQL($datas, "STUDYRECREMARK_DAT");
        return $query;
    }

    //学籍学習記録備考データ追加クエリ文
    function getInsertStudyrec_RemarkQuery($model, $not_exists) {
        // 生成範囲の条件を分ける
        $OPERATOR = ($model->field["RANGE"] == 1) ? "=" : "<=";
        $query  = "";
        $query .= " select tbl.YEAR, ";
        $query .= "        tbl.SCHREGNO, ";
        $query .= "        tbl.CLASSCD, ";
        $query .= "        tbl.SUBCLASSCD, ";
        $query .= "        tbl.REMARK, ";
        $query .= "        '".STAFFCD."' as REGISTERCD ";
        $query .= "   from ( ";
        $query .= "         select t1.YEAR, ";
        $query .= "                t1.SCHREGNO, ";
        $query .= "                substr(t1.SUBCLASSCD,1,2) as CLASSCD, ";
        $query .= "                t1.SUBCLASSCD, ";
        $query .= "                t1.REMARK ";
        $query .= "           from SCHREG_QUALIFIED_DAT t1 ";
        $query .= "          where t1.YEAR ".$OPERATOR." '".CTRL_YEAR."' ";
        $query .= "        ) tbl ";
        $query .= "  where exists (select 'X' from ".$model->RegdTable." W1 ";
        $query .= "                 where W1.YEAR     = '" .CTRL_YEAR ."' ";
        $query .= "                   and W1.SEMESTER = '" .CTRL_SEMESTER ."' ";
        $query .= "                   and W1.grade   = '" .$model->annual ."' ";
        if (strlen($model->hr_class))   $query .= " and W1.hr_class   = '" .$model->hr_class."' ";
        if (strlen($model->coursecode)) $query .= " and W1.coursecode = '". $model->coursecode ."' ";
        if (strlen($model->schregno))   $query .= " and W1.schregno   = '". $model->schregno ."' ";
        $query .= "                   and W1.SCHREGNO = tbl.SCHREGNO ) ";

        //既存レコードは除く
        if ($not_exists == "on") {
            $query .= "  AND NOT EXISTS  (SELECT 'X' FROM studyrecremark_dat WK1
                                           WHERE WK1.year       = tbl.year
                                             AND WK1.schregno   = tbl.schregno
                                             AND WK1.classcd    = tbl.classcd
                                             AND WK1.subclasscd = tbl.subclasscd)";
        }

        return $query;
    }


    //------------------------------------------------------------
    //学籍出欠記録データ作成処理
    //------------------------------------------------------------
    function addRecord_Schreg_Attendrec_Dat($model)
    {
        //生成範囲の条件を分ける
        if ($model->field["RANGE"] == 1){
            $OPERATOR = "=";
        }else{
            $OPERATOR = "<=";
        }

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //学籍出欠記録データ削除
        $query  = " DELETE FROM schreg_attendrec_dat T1 ";
        $query .= "  WHERE T1.schoolcd = '0'";
        $query .= "    AND T1.year ".$OPERATOR." '" .CTRL_YEAR ."' ";
        $query .= "    AND EXISTS (SELECT 'X' FROM ".$model->RegdTable." W ";
        $query .= "                 WHERE W.year     = '" .CTRL_YEAR ."' ";
        $query .= "                   AND W.semester = '" .CTRL_SEMESTER ."' ";
        $query .= "                   AND W.grade   = '" .$model->annual  ."'";
        if (strlen($model->hr_class))   $query .= " AND W.hr_class   = '". $model->hr_class."'";
        if (strlen($model->coursecode)) $query .= " AND W.coursecode = '". $model->coursecode."'";
        if (strlen($model->schregno))   $query .= " AND W.schregno   = '". $model->schregno ."'";
        $query .= "                AND W.schregno = T1.schregno) ";
        $db->query($query);

        //学籍出欠記録データ作成(各勤怠別集計)
        $query = " INSERT INTO schreg_attendrec_dat( ";
        $query .= " schoolcd, ";
        $query .= " year, ";
        $query .= " schregno, ";
        $query .= " annual, ";
        $query .= " sumdate, ";
        $query .= " classdays, ";
        $query .= " offdays, ";
        $query .= " absent, ";
        $query .= " suspend, ";
        $query .= " mourning, ";
        $query .= " abroad, ";
        $query .= " sick, ";
        $query .= " requirepresent, ";
        $query .= " accidentnotice, ";
        $query .= " noaccidentnotice, ";
        $query .= " registercd ";
        $query .= " ) ";
        $query .= "  (SELECT ";
        $query .= "     '0', ";
        $query .= "     T1.year, ";
        $query .= "     T1.schregno, ";
        $query .= "     T1.annual, ";
        $query .= "     CAST(NULL as date), ";                         //集計日付
        $query .= "     SUM(COALESCE(T2.lesson, 0)) AS lesson, ";
        $query .= "     SUM(COALESCE(T2.offdays, 0)) AS offdays, ";
        $query .= "     SUM(COALESCE(T2.absent, 0))   AS absent, ";
        $query .= "     SUM(COALESCE(T2.suspend, 0))  AS suspend, ";
        $query .= "     SUM(COALESCE(T2.mourning, 0)) AS mourning, ";
        $query .= "     SUM(COALESCE(T2.abroad, 0)) AS abroad, ";
        $query .= "     SUM(COALESCE(T2.sick, 0)) AS sick, ";
        $query .= "     0, ";
        $query .= "     SUM(COALESCE(T2.notice, 0)) AS accidentnotice, ";
        $query .= "     SUM(COALESCE(T2.nonotice, 0)) AS noaccidentnotice, ";
        $query .= "     '" .STAFFCD ."' ";
        $query .= "   FROM ";
        $query .= "     ( ";
        $query .= " SELECT DISTINCT T1.year ";
        $query .= "       ,T1.schregno ";
        $query .= "       ,T1.annual ";
        $query .= "   FROM ".$model->RegdTable." T1 ";
        $query .= "  WHERE T1.year ".$OPERATOR."'" .CTRL_YEAR ."'";
        $query .= "    AND EXISTS (SELECT 'X' FROM ".$model->RegdTable." W1 ";
        $query .= "                 WHERE W1.year     = '" .CTRL_YEAR ."'";
        $query .= "                   AND W1.semester = '" .CTRL_SEMESTER ."'";
        $query .= "                   AND W1.grade   = '" .$model->annual ."'";
        if (strlen($model->hr_class))   $query .= " AND W1.hr_class   = '". $model->hr_class."'";
        if (strlen($model->coursecode)) $query .= " AND W1.coursecode = '". $model->coursecode."'";
        if (strlen($model->schregno))   $query .= " AND W1.schregno   = '". $model->schregno ."'";
        $query .= "                   AND W1.schregno = T1.schregno) ";
        $query .= "     ) T1 LEFT OUTER JOIN attend_semes_dat T2 ON T1.schregno = T2.schregno AND T1.year = T2.year ";
        $query .= "   GROUP BY T1.year, T1.schregno, T1.annual) ";
        $db->query($query);


        //要出席日数、出席日数の算出
        $query  = " UPDATE ";
        $query .= "   SCHREG_ATTENDREC_DAT T1 ";
        $query .= " SET ";
        $query .= "   REQUIREPRESENT = COALESCE(CLASSDAYS - OFFDAYS - (SUSPEND + MOURNING + ABROAD),0), ";
        $query .= "   PRESENT = COALESCE(CLASSDAYS - OFFDAYS - (SUSPEND + MOURNING+ABROAD) - (SICK + ACCIDENTNOTICE + NOACCIDENTNOTICE),0), ";
        $query .= "   UPDATED = SYSDATE() ";
        $query .= " WHERE ";
        $query .= "   T1.SCHOOLCD = '0' AND ";
        $query .= "   EXISTS (SELECT ";
        $query .= "             'X' ";
        $query .= "           FROM ";
        $query .= "             ".$model->RegdTable." W1 ";
        $query .= "           WHERE ";
        $query .= "             W1.YEAR     = '" .CTRL_YEAR ."' AND ";
        $query .= "             W1.SEMESTER = '" .CTRL_SEMESTER ."' AND ";
        $query .= "             W1.grade = '" .$model->annual ."' AND ";
        if (strlen($model->hr_class))   $query .= " W1.HR_CLASS   = '". $model->hr_class."' AND ";
        if (strlen($model->coursecode)) $query .= " W1.COURSECODE = '". $model->coursecode."' AND ";
        if (strlen($model->schregno))   $query .= " W1.SCHREGNO   = '". $model->schregno ."' AND ";
        $query .= "             W1.SCHREGNO = T1.SCHREGNO ";
        $query .= "           ) ";
        $query .= "  AND T1.YEAR ".$OPERATOR."'".CTRL_YEAR."'";
        $db->query($query);
        
        $db->commit();
        Query::dbCheckIn($db);
    }

    //------------------------------------------------------------
    //学籍学習記録データの削除(ＣＳＶデータ書込み前)
    //------------------------------------------------------------
    function deleteQuerySchreg_StudyRec_Dat($data)
    {
        $query  = " DELETE FROM SCHREG_STUDYREC_DAT ";
        $query .= "  WHERE SCHOOLCD   = '" .$data["SCHOOLCD"] ."'";
        $query .= "    AND YEAR       = '" .$data["YEAR"] ."'";
        $query .= "    AND SCHREGNO   = '" .sprintf("%08d",$data["SCHREGNO"]) ."'";
        $query .= "    AND ANNUAL     = '" .sprintf("%02d",$data["ANNUAL"]) ."'";
        $query .= "    AND CLASSCD    = '" .sprintf("%02d",$data["CLASSCD"]) ."'";
        $query .= "    AND SUBCLASSCD = '" .sprintf("%06d",$data["SUBCLASSCD"]) ."'";
        return $query;
    }

    //------------------------------------------------------------
    //学籍学習記録データの作成（ＣＳＶデータより読込）
    //------------------------------------------------------------
    function addQuerySchreg_StudyRec_Dat($data)
    {
        $datas = array();
        $datas["SCHOOLCD"][TEXT]         = $data["SCHOOLCD"];
        $datas["YEAR"][TEXT]             = $data["YEAR"];
        $datas["SCHREGNO"][TEXT]         = sprintf("%08d",$data["SCHREGNO"]);
        $datas["ANNUAL"][TEXT]           = sprintf("%02d",$data["ANNUAL"]);
        $datas["CLASSCD"][TEXT]          = sprintf("%02d",$data["CLASSCD"]);
        $datas["SUBCLASSCD"][TEXT]       = sprintf("%06d",$data["SUBCLASSCD"]);
        $datas["CLASSNAME"][TEXT]        = $data["CLASSNAME"];
        $datas["CLASSABBV"][TEXT]        = $data["CLASSABBV"];
        $datas["CLASSNAME_ENG"][TEXT]    = $data["CLASSNAME_ENG"];
        $datas["CLASSABBV_ENG"][TEXT]    = $data["CLASSABBV_ENG"];
        $datas["SUBCLASSES"][NUMBER]     = ((is_numeric($data["SUBCLASSES"]))? $data["SUBCLASSES"] : "NULL");
        $datas["SUBCLASSNAME"][TEXT]     = $data["SUBCLASSNAME"];
        $datas["SUBCLASSABBV"][TEXT]     = $data["SUBCLASSABBV"];
        $datas["SUBCLASSNAME_ENG"][TEXT] = $data["SUBCLASSNAME_ENG"];
        $datas["SUBCLASSABBV_ENG"][TEXT] = $data["SUBCLASSABBV_ENG"];
        $datas["VALUATION"][NUMBER]      = ((is_numeric($data["VALUATION"]))? $data["VALUATION"] : "NULL");
        $datas["GET_CREDIT"][NUMBER]     = ((is_numeric($data["GET_CREDIT"]))? $data["GET_CREDIT"] : "NULL");
        $datas["ADD_CREDIT"][NUMBER]     = ((is_numeric($data["ADD_CREDIT"]))? $data["ADD_CREDIT"] : "NULL");
        $datas["COMP_CREDIT"][NUMBER]    = ((is_numeric($data["COMP_CREDIT"]))? $data["COMP_CREDIT"] : "NULL");
        $datas["REGISTERCD"][TEXT]       = STAFFCD;
        $datas["UPDATED"][FUNC]          = "SYSDATE()";

        $query = Query::insertSQL($datas, "SCHREG_STUDYREC_DAT");
        return $query;
    }

    //------------------------------------------------------------
    //学籍学習記録「備考」データの削除
    //------------------------------------------------------------
    function deleteStudyRecRemark($data)
    {
        $query  = " DELETE FROM STUDYRECREMARK_DAT ";
        $query .= " WHERE YEAR       = '".$data["YEAR"]."'";
        $query .= "   AND SCHREGNO   = '".sprintf("%08d",$data["SCHREGNO"])."'";
        $query .= "   AND CLASSCD    = '".sprintf("%02d",$data["CLASSCD"])."'";
        $query .= "   AND SUBCLASSCD = '".sprintf("%06d",$data["SUBCLASSCD"])."'";
        return $query;
    }

    //------------------------------------------------------------
    //学籍学習記録「備考」データの作成
    //------------------------------------------------------------
    function insertStudyRecRemark($data)
    {
        $datas = array();
        $datas["YEAR"][TEXT]             = $data["YEAR"];
        $datas["SCHREGNO"][TEXT]         = sprintf("%08d",$data["SCHREGNO"]);
        $datas["CLASSCD"][TEXT]          = sprintf("%02d",$data["CLASSCD"]);
        $datas["SUBCLASSCD"][TEXT]       = sprintf("%06d",$data["SUBCLASSCD"]);
        $datas["REMARK"][TEXT]           = $data["REMARK"];
        $datas["REGISTERCD"][TEXT]       = STAFFCD;
        $datas["UPDATED"][FUNC]          = "SYSDATE()";
        $query = Query::insertSQL($datas, "STUDYRECREMARK_DAT");
        return $query;
    }

    /***********************************************/
    /* ↓↓↓ SCHREG_STUDYREC_DAT(広島国際) ↓↓↓ */
    /***********************************************/

    //メイン
    //総合学習(900100)とＬＨＲ(940000)
    function hirogakuStudyRec($model) 
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = "";
        //クエリー
        $query  = knje061Query::getStudyRecSql($model);
        $result = $db->query($query); 
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //新規作成
            if ($model->field["CREATEDIV"] == 1) {
                $query = knje061Query::deleteStudyRecSql($row);
                $db->query($query);
                $query = knje061Query::insertStudyRecSql($row);
                $db->query($query);
            //追加作成
            } elseif ($model->field["CREATEDIV"] == 2) {
                $query = knje061Query::existsStudyRecSql($row);
                if ($db->getOne($query) == 0) {
                    $query = knje061Query::insertStudyRecSql($row);
                    $db->query($query);
                }
            //上書作成
            } else {
                $query = knje061Query::existsStudyRecSql($row);
                if ($db->getOne($query) == 0) {
                    $query = knje061Query::insertStudyRecSql($row);
                    $db->query($query);
                } else {
                    $query = knje061Query::updateStudyRecSql($row);
                    $db->query($query);
                }
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    function insertStudyRecSql($row)
    {
        $data = array();
        $data["SCHOOLCD"][TEXT]        = $row["SCHOOLCD"];
        $data["YEAR"][TEXT]            = $row["YEAR"];
        $data["SCHREGNO"][TEXT]        = $row["SCHREGNO"];
        $data["ANNUAL"][TEXT]          = $row["ANNUAL"];
        $data["CLASSCD"][TEXT]         = $row["CLASSCD"];
        $data["SUBCLASSCD"][TEXT]      = $row["SUBCLASSCD"];
        $data["VALUATION"][NUMBER]     = $row["VALUATION"];
        $data["GET_CREDIT"][NUMBER]    = $row["GET_CREDIT"];
        $data["ADD_CREDIT"][NUMBER]    = $row["ADD_CREDIT"];
        $data["COMP_CREDIT"][NUMBER]   = $row["COMP_CREDIT"];
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "SYSDATE()";

        $query = Query::insertSQL($data, "SCHREG_STUDYREC_DAT");
        return $query;
    }

    function updateStudyRecSql($row)
    {
        $data = array();
        $data["VALUATION"][NUMBER]     = $row["VALUATION"];
        $data["GET_CREDIT"][NUMBER]    = $row["GET_CREDIT"];
        $data["ADD_CREDIT"][NUMBER]    = $row["ADD_CREDIT"];
        $data["COMP_CREDIT"][NUMBER]   = $row["COMP_CREDIT"];
        $data["REGISTERCD"][TEXT]  = STAFFCD;
        $data["UPDATED"][FUNC]     = "SYSDATE()";

        $where  = " WHERE SCHOOLCD      = '".$row["SCHOOLCD"]."' ";
        $where .= "   AND YEAR          = '".$row["YEAR"]."' ";
        $where .= "   AND SCHREGNO      = '".$row["SCHREGNO"]."' ";
        $where .= "   AND ANNUAL        = '".$row["ANNUAL"]."' ";
        $where .= "   AND CLASSCD       = '".$row["CLASSCD"]."' ";
        $where .= "   AND SUBCLASSCD    = '".$row["SUBCLASSCD"]."' ";

        $query = Query::updateSQL($data, "SCHREG_STUDYREC_DAT", $where);
        return $query;
    }

    function deleteStudyRecSql($row)
    {
        $query  = " DELETE FROM SCHREG_STUDYREC_DAT ";
        $query .= " WHERE SCHOOLCD      = '".$row["SCHOOLCD"]."' ";
        $query .= "   AND YEAR          = '".$row["YEAR"]."' ";
        $query .= "   AND SCHREGNO      = '".$row["SCHREGNO"]."' ";
        $query .= "   AND ANNUAL        = '".$row["ANNUAL"]."' ";
        $query .= "   AND CLASSCD       = '".$row["CLASSCD"]."' ";
        $query .= "   AND SUBCLASSCD    = '".$row["SUBCLASSCD"]."' ";
        return $query;
    }

    function existsStudyRecSql($row)
    {
        $query  = " SELECT COUNT(*) FROM SCHREG_STUDYREC_DAT ";
        $query .= " WHERE SCHOOLCD      = '".$row["SCHOOLCD"]."' ";
        $query .= "   AND YEAR          = '".$row["YEAR"]."' ";
        $query .= "   AND SCHREGNO      = '".$row["SCHREGNO"]."' ";
        $query .= "   AND ANNUAL        = '".$row["ANNUAL"]."' ";
        $query .= "   AND CLASSCD       = '".$row["CLASSCD"]."' ";
        $query .= "   AND SUBCLASSCD    = '".$row["SUBCLASSCD"]."' ";
        return $query;
    }

    //総合学習(900100)とＬＨＲ(940000)
    //-- 講座名簿に登録されている生徒は、データ生成する。
    //-- 履修・修得単位は、単位マスタを参照して、無条件に与える。
    function getStudyRecSql($model) {
        // 生成範囲の条件を分ける
        $operator = ($model->field["RANGE"] == 1) ? "=" : "<=";

        $query  = "";
        $query .= " WITH T_CHAIR AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T2.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         CHAIR_DAT T1, ";
        $query .= "         CHAIR_STD_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR         ".$operator." '".CTRL_YEAR."' ";
        $query .= "         AND T1.SUBCLASSCD  IN ('900100','940000') "; //総合学習、ＬＨＲ
        $query .= "         AND T2.YEAR         = T1.YEAR ";
        $query .= "         AND T2.SEMESTER     = T1.SEMESTER ";
        $query .= "         AND T2.CHAIRCD      = T1.CHAIRCD ";
        $query .= "     GROUP BY ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T2.SCHREGNO ";
        $query .= "     ) ";

        $query .= " SELECT ";
        $query .= "     '0' AS SCHOOLCD, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     MAX(L1.ANNUAL) AS ANNUAL, ";
        $query .= "     SUBSTR(T1.SUBCLASSCD,1,2) AS CLASSCD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     CAST(NULL AS SMALLINT) AS VALUATION, ";
        $query .= "     MAX(L2.CREDITS) AS GET_CREDIT, ";
        $query .= "     CAST(NULL AS SMALLINT) AS ADD_CREDIT, ";
        $query .= "     MAX(L2.CREDITS) AS COMP_CREDIT ";
        $query .= " FROM ";
        $query .= "     T_CHAIR T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT L1 ";
        $query .= "         ON  L1.SCHREGNO     = T1.SCHREGNO ";
        $query .= "         AND L1.YEAR         = T1.YEAR ";
        $query .= "         AND L1.ANNUAL IS NOT NULL ";
        $query .= "     LEFT JOIN CREDIT_MST L2 ";
        $query .= "         ON  L2.YEAR         = L1.YEAR ";
        $query .= "         AND L2.COURSECD     = L1.COURSECD ";
        $query .= "         AND L2.MAJORCD      = L1.MAJORCD ";
        $query .= "         AND L2.GRADE        = L1.GRADE ";
        $query .= "         AND L2.COURSECODE   = L1.COURSECODE ";
        $query .= "         AND L2.CLASSCD      = SUBSTR(T1.SUBCLASSCD,1,2) ";
        $query .= "         AND L2.SUBCLASSCD   = T1.SUBCLASSCD ";
        $query .= "         AND L2.CREDITS IS NOT NULL ";
        $query .= " WHERE EXISTS (SELECT 'X' FROM ".$model->RegdTable." W1 ";
        $query .= "                WHERE W1.YEAR = '".CTRL_YEAR."' ";
        $query .= "                  AND W1.GRADE = '" .$model->annual ."' ";
        if (strlen($model->hr_class))   $query .= "                  AND W1.HR_CLASS = '" .$model->hr_class ."' ";
        if (strlen($model->coursecode)) $query .= "                  AND W1.COURSECODE = '" .$model->coursecode ."' ";
        if (strlen($model->schregno))   $query .= "                  AND W1.SCHREGNO = '" .$model->schregno ."' ";
        $query .= "                  AND W1.SCHREGNO = T1.SCHREGNO) ";
        $query .= " GROUP BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

}
?>
