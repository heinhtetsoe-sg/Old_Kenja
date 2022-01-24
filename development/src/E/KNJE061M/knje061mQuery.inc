<?php

require_once('for_php7.php');

class knje061mquery extends Query {

    //在籍データは、「生徒のMAX学期のレコード」を参照する。
    function getViewSchregRegdDat() {
        $query  = "(";
        $query .= " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     (SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         YEAR, ";
        $query .= "         max(SEMESTER) as SEMESTER ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        $query .= "         YEAR ";
        $query .= "     ) M1 ";
        $query .= " WHERE ";
        $query .= "         M1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     AND M1.YEAR     = T1.YEAR ";
        $query .= "     AND M1.SEMESTER = T1.SEMESTER ";
        $query .= ")";
        return $query;
    }

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
    //コースを取得
    //------------------------------------------------------------
    function selectCourse($model)
    {
        $query  = " SELECT DISTINCT T1.coursecode, T2.coursecodename ";
        $query .= "   FROM ".$model->vSchregRegdDat." T1,";
        $query .= "         coursecode_mst T2 ";
        $query .= "  WHERE T1.coursecode = T2.coursecode";
        $query .= "    AND T1.grade      = '". $model->annual ."'";
        $query .= "    AND T1.hr_class   = '". $model->hr_class ."'";
        $query .= "    AND T1.year       = '".CTRL_YEAR ."'";
        $query .= "  ORDER BY T1.coursecode ";
        return $query;
    }

    //------------------------------------------------------------
    //生徒取得
    //------------------------------------------------------------
    function selectSchregno($model)
    {
        $query  = " SELECT T1.attendno, T1.schregno, T2.name_show";
        $query .= "   FROM ".$model->vSchregRegdDat." T1, schreg_base_mst T2 ";
        $query .= "  WHERE T1.year       = '".CTRL_YEAR ."'";
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
    function selectQueryHRClass($model){
        $query  = "SELECT DISTINCT hr_class, HR_NAME FROM schreg_regd_hdat";
        $query .= " WHERE year     = '".CTRL_YEAR."'";
        $query .= "   AND grade    = '".sprintf("%02d",$model->annual)."'";
        $query .= "  ORDER BY hr_class";
        return $query;	
    }

    //------------------------------------------------------------
    //学年（年次）コンボボックス用
    //------------------------------------------------------------
    function selectQueryAnnual($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     smallint(GRADE_CD) as GRADE_CD, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->field["KIND"] == 3) $query .= "     AND SCHOOL_KIND = 'J' ";
        if ($model->field["KIND"] == 2) $query .= "     AND SCHOOL_KIND = 'H' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }


    /**********************************/
    /* ↓↓↓ 中学・高校(出欠) ↓↓↓ */
    /**********************************/

    //------------------------------------------------------------
    //学籍出欠記録データ作成処理
    //------------------------------------------------------------
    function addRecord_Schreg_Attendrec_Dat($model)
    {
        //署名チェックに該当したデータを対象から除く。
        $queryShomei = "";
        if ($model->Properties["useSeitoSidoYorokuShomeiKinou"] == 1) {
            $queryShomei .= "    AND NOT EXISTS (SELECT 'X' FROM ATTEST_OPINIONS_WK WK ";
            $queryShomei .= "                     WHERE WK.YEAR = T1.YEAR ";
            $queryShomei .= "                       AND WK.SCHREGNO = T1.SCHREGNO ";
            $queryShomei .= "                       AND WK.CHAGE_OPI_SEQ IS NOT NULL) ";
        }

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
        $query .= "    AND EXISTS (SELECT 'X' FROM ".$model->vSchregRegdDat." W ";
        $query .= "                 WHERE W.year     = '" .CTRL_YEAR ."' ";
        $query .= "                   AND W.grade   = '" .$model->annual  ."'";
        if (strlen($model->hr_class))   $query .= " AND W.hr_class   = '". $model->hr_class."'";
        if (strlen($model->coursecode)) $query .= " AND W.coursecode = '". $model->coursecode."'";
        if (strlen($model->schregno))   $query .= " AND W.schregno   = '". $model->schregno ."'";
        $query .= "                AND W.schregno = T1.schregno) ";
        $query .= $queryShomei;
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
        $query .= "     SUM(COALESCE(T2.suspend, 0) + COALESCE(T2.virus, 0))  AS suspend, ";
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
        $query .= "   FROM ".$model->vSchregRegdDat." T1 ";
        $query .= "  WHERE T1.year ".$OPERATOR."'" .CTRL_YEAR ."'";
        $query .= "    AND EXISTS (SELECT 'X' FROM ".$model->vSchregRegdDat." W1 ";
        $query .= "                 WHERE W1.year     = '" .CTRL_YEAR ."'";
        $query .= "                   AND W1.grade   = '" .$model->annual ."'";
        if (strlen($model->hr_class))   $query .= " AND W1.hr_class   = '". $model->hr_class."'";
        if (strlen($model->coursecode)) $query .= " AND W1.coursecode = '". $model->coursecode."'";
        if (strlen($model->schregno))   $query .= " AND W1.schregno   = '". $model->schregno ."'";
        $query .= "                   AND W1.schregno = T1.schregno) ";
        $query .= $queryShomei;
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
        $query .= "             ".$model->vSchregRegdDat." W1 ";
        $query .= "           WHERE ";
        $query .= "             W1.YEAR     = '" .CTRL_YEAR ."' AND ";
        $query .= "             W1.grade = '" .$model->annual ."' AND ";
        if (strlen($model->hr_class))   $query .= " W1.HR_CLASS   = '". $model->hr_class."' AND ";
        if (strlen($model->coursecode)) $query .= " W1.COURSECODE = '". $model->coursecode."' AND ";
        if (strlen($model->schregno))   $query .= " W1.SCHREGNO   = '". $model->schregno ."' AND ";
        $query .= "             W1.SCHREGNO = T1.SCHREGNO ";
        $query .= "           ) ";
        $query .= "  AND T1.YEAR ".$OPERATOR."'".CTRL_YEAR."'";
        $query .= $queryShomei;
        $db->query($query);
        
        $db->commit();
        Query::dbCheckIn($db);
    }


    /****************************/
    /* ↓↓↓ 高校(成績) ↓↓↓ */
    /****************************/

    function getStudentSql($model)
    {
        $query  = " WITH REGD_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     YEAR, ";
        $query .= "     MAX(SEMESTER) AS SEMESTER ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '".$model->annual."' ";
        if (strlen($model->hr_class))   $query .= "     AND HR_CLASS    = '".$model->hr_class."' ";
        if (strlen($model->coursecode)) $query .= "     AND COURSECODE  = '".$model->coursecode."' ";
        if (strlen($model->schregno))   $query .= "     AND SCHREGNO    = '".$model->schregno."' ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO, ";
        $query .= "     YEAR ";
        $query .= " ORDER BY ";
        $query .= "     SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.ANNUAL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     REGD_T T2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     AND T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.SEMESTER = T2.SEMESTER ";

        //署名チェックに該当したデータを対象から除く。
        if ($model->Properties["useSeitoSidoYorokuShomeiKinou"] == 1) {
            $query .= "    AND NOT EXISTS (SELECT 'X' FROM ATTEST_OPINIONS_WK WK ";
            $query .= "                     WHERE WK.YEAR = T1.YEAR ";
            $query .= "                       AND WK.SCHREGNO = T1.SCHREGNO ";
            $query .= "                       AND WK.CHAGE_OPI_SEQ IS NOT NULL) ";
        }

        $query .= " ORDER BY ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    function getStudentRegdSql($schregNo, $grade)
    {
        $query  = " WITH SCH_GRADE AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$schregNo."' ";
        $query .= "     AND T1.GRADE < '".$grade."' ";
        $query .= "     AND EXISTS( ";
        $query .= "             SELECT ";
        $query .= "                 'x' ";
        $query .= "             FROM ";
        $query .= "                 SCHREG_REGD_GDAT T2 ";
        $query .= "             WHERE ";
        $query .= "                 T2.YEAR = T1.YEAR ";
        $query .= "                 AND T2.SCHOOL_KIND = 'H' ";
        $query .= "                 AND T2.GRADE = T1.GRADE ";
        $query .= "     ) ";
        $query .= " ), REGD_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.YEAR, ";
        $query .= "     MAX(T1.SEMESTER) AS SEMESTER ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             SCH_GRADE T2 ";
        $query .= "         WHERE ";
        $query .= "             T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "             AND T1.GRADE = T2.GRADE ";
        $query .= "     ) ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.YEAR ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.ANNUAL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     REGD_T T2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     AND T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.SEMESTER = T2.SEMESTER ";

        return $query;
    }

    function getRecordScoreSql($year, $semester, $schregNo, $classCd, $subclassCd, $scoreIsNotNull, $orderDiv, $model)
    {
        $query  = " SELECT ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     SCHREGNO, ";
        $query .= "     SCORE, "; //10段階：使用する！
        $query .= "     VALUE, "; //5段階：使用しない！・・・今のところ
        $query .= "     GET_CREDIT, ";
        $query .= "     ADD_CREDIT, ";
        $query .= "     COMP_CREDIT ";
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SEMESTER = '{$semester}' ";
        $query .= "     AND TESTKINDCD = '99' ";
        $query .= "     AND TESTITEMCD = '00' ";
        $query .= "     AND SCORE_DIV = '00' ";
        $query .= "     AND SUBSTR(SUBCLASSCD,1,2) = '{$classCd}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND SCHOOL_KIND   = 'H' ";
            //高1(2013年度から新課程)
            //高2(2014年度から新課程)
            if (($model->regdGdat[$model->annual]["GRADE_CD"] == "1" && "2013" <= CTRL_YEAR) || 
                ($model->regdGdat[$model->annual]["GRADE_CD"] == "2" && "2014" <= CTRL_YEAR)) {
                $query .= "   AND CURRICULUM_CD = '3' ";
            } else {
                $query .= "   AND CURRICULUM_CD = '2' ";
            }
        }
        $query .= "     AND SCHREGNO = '{$schregNo}' ";
        if (strlen($subclassCd)) {
            $query .= "     AND SUBCLASSCD = '{$subclassCd}' ";
        }
        if ($scoreIsNotNull == "on") {
            $query .= "     AND SCORE IS NOT NULL "; //点があるものだけ取得したい場合
        }
        if ($orderDiv == "score") {
            $query .= " ORDER BY SCORE DESC ";
        }

        return $query;
    }

    function getAssessMstSql($score)
    {
        if (!strlen($score)) $score = -1; //DBエラー防止のため

        $query  = " SELECT ";
        $query .= "     ASSESSLEVEL "; //5段階
        $query .= " FROM ";
        $query .= "     ASSESS_MST ";
        $query .= " WHERE ";
        $query .= "     ASSESSCD = '3' ";
        $query .= "     AND ".$score." BETWEEN ASSESSLOW AND ASSESSHIGH ";

        return $query;
    }

    function getCredits($year, $gradeData, $classCd, $subclassCd, $model)
    {
        $query  = " SELECT ";
        $query .= "     CREDITS ";
        $query .= " FROM ";
        $query .= "     CREDIT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND COURSECD = '".$gradeData["COURSECD"]."' ";
        $query .= "     AND MAJORCD = '".$gradeData["MAJORCD"]."' ";
        $query .= "     AND GRADE = '".$gradeData["GRADE"]."' ";
        $query .= "     AND COURSECODE = '".$gradeData["COURSECODE"]."' ";
        $query .= "     AND CLASSCD = '".$classCd."' ";
        $query .= "     AND SUBCLASSCD = '".$subclassCd."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND SCHOOL_KIND   = 'H' ";
            //高1(2013年度から新課程)
            //高2(2014年度から新課程)
            if (($model->regdGdat[$model->annual]["GRADE_CD"] == "1" && "2013" <= CTRL_YEAR) || 
                ($model->regdGdat[$model->annual]["GRADE_CD"] == "2" && "2014" <= CTRL_YEAR)) {
                $query .= "   AND CURRICULUM_CD = '3' ";
            } else {
                $query .= "   AND CURRICULUM_CD = '2' ";
            }
        }

        return $query;
    }

    function insertStudyRecSql($data, $model)
    {
        $datas = array();
        $datas["SCHOOLCD"][TEXT]        = $data["SCHOOLCD"];
        $datas["YEAR"][TEXT]            = $data["YEAR"];
        $datas["SCHREGNO"][TEXT]        = $data["SCHREGNO"];
        $datas["ANNUAL"][TEXT]          = $data["ANNUAL"];
        $datas["CLASSCD"][TEXT]         = $data["CLASSCD"];
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $datas["SCHOOL_KIND"][TEXT]     = 'H';
            //高1(2013年度から新課程)
            //高2(2014年度から新課程)
            if (($model->regdGdat[$model->annual]["GRADE_CD"] == "1" && "2013" <= CTRL_YEAR) || 
                ($model->regdGdat[$model->annual]["GRADE_CD"] == "2" && "2014" <= CTRL_YEAR)) {
                $datas["CURRICULUM_CD"][TEXT]   = '3';
            } else {
                $datas["CURRICULUM_CD"][TEXT]   = '2';
            }
        }
        $datas["SUBCLASSCD"][TEXT]      = $data["SUBCLASSCD"];
        $datas["VALUATION"][NUMBER]     = $data["VALUATION"];
        $datas["GET_CREDIT"][NUMBER]    = $data["GET_CREDIT"];
        $datas["ADD_CREDIT"][NUMBER]    = $data["ADD_CREDIT"];
        $datas["COMP_CREDIT"][NUMBER]   = $data["COMP_CREDIT"];
        $datas["REGISTERCD"][TEXT]      = STAFFCD;
        $datas["UPDATED"][FUNC]         = "SYSDATE()";

        $query = Query::insertSQL($datas, "SCHREG_STUDYREC_DAT");
        return $query;
    }

    function updateStudyRecSql($data, $model)
    {
        $datas = array();
        $datas["VALUATION"][NUMBER]     = $data["VALUATION"];
        $datas["GET_CREDIT"][NUMBER]    = $data["GET_CREDIT"];
        $datas["ADD_CREDIT"][NUMBER]    = $data["ADD_CREDIT"];
        $datas["COMP_CREDIT"][NUMBER]   = $data["COMP_CREDIT"];
        $datas["REGISTERCD"][TEXT]  = STAFFCD;
        $datas["UPDATED"][FUNC]     = "SYSDATE()";

        $where  = " WHERE SCHOOLCD      = '".$data["SCHOOLCD"]."' ";
        $where .= "   AND YEAR          = '".$data["YEAR"]."' ";
        $where .= "   AND SCHREGNO      = '".$data["SCHREGNO"]."' ";
        $where .= "   AND ANNUAL        = '".$data["ANNUAL"]."' ";
        $where .= "   AND CLASSCD       = '".$data["CLASSCD"]."' ";
        $where .= "   AND SUBCLASSCD    = '".$data["SUBCLASSCD"]."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $where .= "   AND SCHOOL_KIND   = 'H' ";
            //高1(2013年度から新課程)
            //高2(2014年度から新課程)
            if (($model->regdGdat[$model->annual]["GRADE_CD"] == "1" && "2013" <= CTRL_YEAR) || 
                ($model->regdGdat[$model->annual]["GRADE_CD"] == "2" && "2014" <= CTRL_YEAR)) {
                $where .= "   AND CURRICULUM_CD = '3' ";
            } else {
                $where .= "   AND CURRICULUM_CD = '2' ";
            }
        }

        $query = Query::updateSQL($datas, "SCHREG_STUDYREC_DAT", $where);
        return $query;
    }

    function deleteStudyRecSql($data, $model)
    {
        $query  = " DELETE FROM SCHREG_STUDYREC_DAT ";
        $query .= " WHERE SCHOOLCD      = '".$data["SCHOOLCD"]."' ";
        $query .= "   AND YEAR          = '".$data["YEAR"]."' ";
        $query .= "   AND SCHREGNO      = '".$data["SCHREGNO"]."' ";
        $query .= "   AND ANNUAL        = '".$data["ANNUAL"]."' ";
        $query .= "   AND CLASSCD       = '".$data["CLASSCD"]."' ";
        $query .= "   AND SUBCLASSCD    = '".$data["SUBCLASSCD"]."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   AND SCHOOL_KIND   = 'H' ";
            //高1(2013年度から新課程)
            //高2(2014年度から新課程)
            if (($model->regdGdat[$model->annual]["GRADE_CD"] == "1" && "2013" <= CTRL_YEAR) || 
                ($model->regdGdat[$model->annual]["GRADE_CD"] == "2" && "2014" <= CTRL_YEAR)) {
                $query .= "   AND CURRICULUM_CD = '3' ";
            } else {
                $query .= "   AND CURRICULUM_CD = '2' ";
            }
        }
        return $query;
    }

    function existsStudyRecSql($data, $model)
    {
        $query  = " SELECT COUNT(*) AS CNT FROM SCHREG_STUDYREC_DAT ";
        $query .= " WHERE SCHOOLCD      = '".$data["SCHOOLCD"]."' ";
        $query .= "   AND YEAR          = '".$data["YEAR"]."' ";
        $query .= "   AND SCHREGNO      = '".$data["SCHREGNO"]."' ";
        $query .= "   AND ANNUAL        = '".$data["ANNUAL"]."' ";
        $query .= "   AND CLASSCD       = '".$data["CLASSCD"]."' ";
        $query .= "   AND SUBCLASSCD    = '".$data["SUBCLASSCD"]."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   AND SCHOOL_KIND   = 'H' ";
            //高1(2013年度から新課程)
            //高2(2014年度から新課程)
            if (($model->regdGdat[$model->annual]["GRADE_CD"] == "1" && "2013" <= CTRL_YEAR) || 
                ($model->regdGdat[$model->annual]["GRADE_CD"] == "2" && "2014" <= CTRL_YEAR)) {
                $query .= "   AND CURRICULUM_CD = '3' ";
            } else {
                $query .= "   AND CURRICULUM_CD = '2' ";
            }
        }
        return $query;
    }

    function deleteClasscdStudyRecSql($schoolCd, $year, $schregNo, $annual, $classCd, $model)
    {
        $query  = " DELETE FROM SCHREG_STUDYREC_DAT ";
        $query .= " WHERE SCHOOLCD      = '".$schoolCd."' ";
        $query .= "   AND YEAR          = '".$year."' ";
        $query .= "   AND SCHREGNO      = '".$schregNo."' ";
        $query .= "   AND ANNUAL        = '".$annual."' ";
        $query .= "   AND CLASSCD       = '".$classCd."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   AND SCHOOL_KIND   = 'H' ";
        }
        return $query;
    }

    function deleteAllStudyRecSql($schoolCd, $year, $schregNo, $annual, $model)
    {
        $query  = " DELETE FROM SCHREG_STUDYREC_DAT ";
        $query .= " WHERE SCHOOLCD      = '".$schoolCd."' ";
        $query .= "   AND YEAR          = '".$year."' ";
        $query .= "   AND SCHREGNO      = '".$schregNo."' ";
        $query .= "   AND ANNUAL        = '".$annual."' ";
        return $query;
    }


    /****************************/
    /* ↓↓↓ 中学(成績) ↓↓↓ */
    /****************************/
    /*   KNJE064Mの処理を移行   */
    /****************************/

    //生徒取得
    function getStudent($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.GRADE = '".$model->annual."' ";
        if (strlen($model->hr_class))   $query .= "     AND T1.HR_CLASS    = '".$model->hr_class."' ";
        if (strlen($model->coursecode)) $query .= "     AND T1.COURSECODE  = '".$model->coursecode."' ";
        if (strlen($model->schregno))   $query .= "     AND T1.SCHREGNO    = '".$model->schregno."' ";

        //署名チェックに該当したデータを対象から除く。
        if ($model->Properties["useSeitoSidoYorokuShomeiKinou"] == 1) {
            $query .= "    AND NOT EXISTS (SELECT 'X' FROM ATTEST_OPINIONS_WK WK ";
            $query .= "                     WHERE WK.YEAR = T1.YEAR ";
            $query .= "                       AND WK.SCHREGNO = T1.SCHREGNO ";
            $query .= "                       AND WK.CHAGE_OPI_SEQ IS NOT NULL) ";
        }

        return $query;
    }

    //得点取得
    function getScore($semester, $schregNo, $subclassCd, $model)
    {
        $query  = " SELECT ";
        $query .= "     SCORE ";
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '{$semester}' ";
        $query .= "     AND TESTKINDCD = '99' ";
        $query .= "     AND TESTITEMCD = '00' ";
        $query .= "     AND SCORE_DIV = '00' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND SCHOOL_KIND = 'J' ";
            $query .= " AND CURRICULUM_CD = '2' ";
        }
        $query .= "     AND SUBCLASSCD = '".$subclassCd."' ";
        $query .= "     AND SCHREGNO = '".$schregNo."' ";

        return $query;
    }

    //平均得点取得
    function getScoreAvg($semester, $schregNo, $subclassInState, $model)
    {
        $query  = " SELECT ";
        $query .= "     SUM(SCORE) AS SCORE, ";
        $query .= "     SUM(CASE WHEN SCORE IS NULL THEN 0 ELSE 1 END) AS CNT ";
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '{$semester}' ";
        $query .= "     AND TESTKINDCD = '99' ";
        $query .= "     AND TESTITEMCD = '00' ";
        $query .= "     AND SCORE_DIV = '00' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND SCHOOL_KIND = 'J' ";
            $query .= " AND CURRICULUM_CD = '2' ";
        }
        $query .= "     AND SUBCLASSCD IN ".$subclassInState." ";
        $query .= "     AND SCHREGNO = '".$schregNo."' ";

        return $query;
    }

    //最高得点取得
    function getScoreMax($semester, $schregNo, $subclassInState, $model)
    {
        $query  = " SELECT ";
        $query .= "     VALUE(MAX(SCORE), 0) AS SCORE ";
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '{$semester}' ";
        $query .= "     AND TESTKINDCD = '99' ";
        $query .= "     AND TESTITEMCD = '00' ";
        $query .= "     AND SCORE_DIV = '00' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND SCHOOL_KIND = 'J' ";
            $query .= " AND CURRICULUM_CD = '2' ";
        }
        $query .= "     AND SUBCLASSCD IN ".$subclassInState." ";
        $query .= "     AND SCHREGNO = '".$schregNo."' ";

        return $query;
    }

    //更新
    function delInsData($db, $schregNo, $classCd, $kantenArray, $hyoutei, $model)
    {
        //削除
        knje061mQuery::deleteData($db, $schregNo, $classCd, $model);
        //追加(評価)
        for ($i = 0; $i < get_count($kantenArray); $i++) {
            knje061mQuery::insData($db, $schregNo, $classCd."0".($i + 1), $kantenArray[$i], $model);
        }
        //追加(評定)
        //knje061mQuery::insData($db, $schregNo, $classCd."99", $hyoutei);
    }

    //更新2
    function delInsData2($db, $schregNo, $classCd, $startView, $kantenArray, $model)
    {
        //追加(評価)
        for ($i = 0; $i < get_count($kantenArray); $i++) {
            knje061mQuery::insData($db, $schregNo, $classCd."0".$startView, $kantenArray[$i], $model);
            $startView++;
        }
    }

    //新規
    function insData($db, $schregNo, $viewCd, $value, $model)
    {
        $table = (CTRL_YEAR < "2012") ? "JVIEWSTAT_DAT" : "JVIEWSTAT_SUB_DAT";

        $data = array();
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["SEMESTER"][TEXT]             = "9";
        $data["SCHREGNO"][TEXT]             = $schregNo;
        if ($table == "JVIEWSTAT_SUB_DAT") {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["CLASSCD"][TEXT]          = substr($viewCd, 0, 2);
                $data["SCHOOL_KIND"][TEXT]      = 'J';
                $data["CURRICULUM_CD"][TEXT]    = '2';
            }
            $data["SUBCLASSCD"][TEXT]           = substr($viewCd, 0, 2) ."0001";
        }
        $data["VIEWCD"][TEXT]               = $viewCd;
        $data["STATUS"][TEXT]               = $value;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";

        $query = Query::insertSQL($data, $table);
        $db->query($query);
    }

    //削除
    function deleteData($db, $schregNo, $classCd, $model)
    {
        $table = (CTRL_YEAR < "2012") ? "JVIEWSTAT_DAT" : "JVIEWSTAT_SUB_DAT";

        $query  = " DELETE FROM {$table} ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '9' ";
        $query .= "     AND SCHREGNO = '".$schregNo."'";
        if ($table == "JVIEWSTAT_SUB_DAT") {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= " AND CLASSCD = '{$classCd}' ";
                $query .= " AND SCHOOL_KIND = 'J' ";
                $query .= " AND CURRICULUM_CD = '2' ";
            }
            $query .= "     AND SUBCLASSCD = '".$classCd."0001' ";
        }
        $query .= "     AND substr(VIEWCD, 1, 2) = '".$classCd."'";

        $db->query($query);
    }

    //更新(SCHREG_STUDYREC_DAT)
    function delInsDataStudyrec($db, $schregNo, $annual, $classCd, $hyoutei, $model)
    {
        knje061mQuery::delDataStudyrec($db, $schregNo, $annual, $classCd, $model);
        knje061mQuery::insDataStudyrec($db, $schregNo, $annual, $classCd, $hyoutei, $model);
    }

    //新規(SCHREG_STUDYREC_DAT)
    function insDataStudyrec($db, $schregNo, $annual, $classCd, $hyoutei, $model)
    {
        $data = array();
        $data["SCHOOLCD"][TEXT]             = "0";
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["SCHREGNO"][TEXT]             = $schregNo;
        $data["ANNUAL"][TEXT]               = $annual;
        $data["CLASSCD"][TEXT]              = $classCd;
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["SCHOOL_KIND"][TEXT]      = 'J';
            $data["CURRICULUM_CD"][TEXT]    = '2';
        }
        $data["SUBCLASSCD"][TEXT]           = $classCd ."0001";
        $data["VALUATION"][NUMBER]          = $hyoutei;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";

        $query = Query::insertSQL($data, "SCHREG_STUDYREC_DAT");
        $db->query($query);
    }

    //削除(SCHREG_STUDYREC_DAT)
    function delDataStudyrec($db, $schregNo, $annual, $classCd, $model)
    {
        $query  = " DELETE FROM SCHREG_STUDYREC_DAT ";
        $query .= " WHERE SCHOOLCD      = '0' ";
        $query .= "   AND YEAR          = '".CTRL_YEAR."' ";
        $query .= "   AND SCHREGNO      = '".$schregNo."' ";
        $query .= "   AND ANNUAL        = '".$annual."' ";
        $query .= "   AND CLASSCD       = '".$classCd."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   AND SCHOOL_KIND   = 'J' ";
            $query .= "   AND CURRICULUM_CD = '2' ";
        }
        $query .= "   AND SUBCLASSCD    = '".$classCd."0001' ";

        $db->query($query);
    }
}
?>
