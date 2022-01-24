<?php

require_once('for_php7.php');

class knje061query extends Query
{

    //集計月コンボ
    public function selectSemesAll()
    {
        $query  = " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER, ";
        $query .= "     SEMESTERNAME, ";
        $query .= "     CASE WHEN MONTH(SDATE) < 4 ";
        $query .= "          THEN MONTH(SDATE) + 12 ";
        $query .= "          ELSE MONTH(SDATE) END AS S_MONTH, ";
        $query .= "     CASE WHEN MONTH(EDATE) < 4 ";
        $query .= "          THEN MONTH(EDATE) + 12 ";
        $query .= "          ELSE MONTH(EDATE) END AS E_MONTH ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //ログイン年度が名称マスタ「E060」の登録されている年度の時は、【実行】ボタン使用不可とする
    public function getCntE060()
    {
        $query = " SELECT COUNT(*) FROM NAME_MST WHERE NAMECD1 = 'E060' AND NAME1 = '".CTRL_YEAR."' ";
        return $query;
    }

    //在籍データは、「生徒のMAX学期のレコード」を参照する。
    public function getViewSchregRegdDat()
    {
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
    /***
        // 今年度の最大学期を取得
        function getMaxSemester()
        {
            $query  = "";
            $query .= " SELECT  MAX(SEMESTER) AS SEMESTER ";
            $query .= " FROM    SCHREG_REGD_DAT ";
            $query .= " WHERE   YEAR = '".CTRL_YEAR ."' ";

            $db = Query::dbCheckOut();
            $rtnVal = $db->getOne($query);
            Query::dbCheckIn($db);

            return $rtnVal;
        }
    ***/
    // 成績テーブルを判断するためのフラグを取得(法政・自修館・その他)
    public function getNameMst()
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

    // 観点の計算方法
    public function getNameMstD027()
    {
        $query  = "";
        $query .= "SELECT ";
        $query .= "    NAMECD1, ";
        $query .= "    NAMECD2, ";
        $query .= "    NAMESPARE1 "; // 1:平均、2:最高
        $query .= "FROM ";
        $query .= "    NAME_MST ";
        $query .= "WHERE ";
        $query .= "    NAMECD1='D027' AND ";
        $query .= "    NAMECD2='01' ";

        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $rtnRow;
    }

    //------------------------------------------------------------
    //科目数の取得
    //------------------------------------------------------------
    public function getSubclasses($data, $model)
    {
        $query .= " SELECT SUBCLASSES ";
        $query .= "   FROM CLASS_MST ";
        $query .= "  WHERE CLASSCD = '".sprintf("%02d", $data["CLASSCD"]) ."'";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    AND SCHOOL_KIND      = '" .$data["SCHOOL_KIND"] ."'";
        }

        return $query;
    }

    //------------------------------------------------------------
    //コースを取得
    //------------------------------------------------------------
    public function selectCourse($model)
    {
        $query  = " SELECT DISTINCT T1.coursecode, T2.coursecodename ";
        $query .= "   FROM ".$model->vSchregRegdDat." T1,";
        $query .= "         coursecode_mst T2 ";
        $query .= "  WHERE T1.coursecode = T2.coursecode";
        $query .= "    AND T1.grade      = '". $model->annual ."'";
        $query .= "    AND T1.hr_class   = '". $model->hr_class ."'";
        $query .= "    AND T1.year       = '".CTRL_YEAR ."'";
//        $query .= "    AND T1.semester   = '".$model->maxSemester ."'";
        $query .= "  ORDER BY T1.coursecode ";
        return $query;
    }

    //------------------------------------------------------------
    //生徒取得
    //------------------------------------------------------------
    public function selectSchregno($model)
    {
        $query  = " SELECT T1.attendno, T1.schregno, T2.name_show";
        $query .= "   FROM ".$model->vSchregRegdDat." T1, schreg_base_mst T2 ";
        $query .= "  WHERE T1.year       = '".CTRL_YEAR ."'";
        //$query .= "    AND T1.semester   = '".$model->maxSemester ."'";
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
    public function selectQueryHRClass($model)
    {
        $query  = "SELECT DISTINCT hr_class, HR_NAME FROM schreg_regd_hdat";
        $query .= " WHERE year     = '".CTRL_YEAR."'";
//        $query .= "   AND semester = '".$model->maxSemester."'";
        $query .= "   AND grade    = '".sprintf("%02d", $model->annual)."'";
        $query .= "  ORDER BY hr_class";
        return $query;
    }

    //------------------------------------------------------------
    //学年（年次）コンボボックス用
    //------------------------------------------------------------
    public function selectQueryAnnual($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //------------------------------------------------------------
    //学年（年次）の学校種別を取得する
    //------------------------------------------------------------
    public function setAnnualSchoolKind($db, $model)
    {
        $annualSchoolKind = '';
        $result = $db->query(knje061Query::selectQueryAnnual($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["VALUE"] == $model->annual) {
                $annualSchoolKind = $row["SCHOOL_KIND"];
            }
        }
        return $annualSchoolKind;
    }
        
    //------------------------------------------------------------
    //成績データ新規作成
    //------------------------------------------------------------
    public function addRecordSchregStudyrecDat($model)
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
        if ($model->field["RANGE"] == 1) {
            $OPERATOR = "=";
        } else {
            $OPERATOR = "<=";
        }

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //学年の学校種別
        $model->annualSchoolKind = knje061Query::setAnnualSchoolKind($db, $model);

        //DELETE(本校区分:0)
        $query = " DELETE FROM SCHREG_STUDYREC_DAT T1 ";
        $query .= " WHERE T1.schoolcd = '0'";
        $query .= "   AND T1.YEAR ".$OPERATOR."'" .CTRL_YEAR ."'";
        $query .= "   AND EXISTS (SELECT 'X' FROM ".$model->vSchregRegdDat." W ";
        $query .= "                WHERE W.YEAR     = '" .CTRL_YEAR ."'";
        $query .= "                  AND W.grade   = '" .$model->annual  ."'";
        if (strlen($model->hr_class)) {
            $query .= "              AND W.hr_class   = '".$model->hr_class."'";
        }
        if (strlen($model->coursecode)) {
            $query .= "              AND W.coursecode = '".$model->coursecode."'";
        }
        if (strlen($model->schregno)) {
            $query .= "              AND W.schregno   = '".$model->schregno."'";
        }
        $query .= "                  AND W.SCHREGNO = T1.SCHREGNO ) ";
        $query .= $queryShomei;
        $db->query($query);

        //INSERT(本校区分:0)
        $db->query(knje061Query::insertStudyrecQuery($model, "0", "off", $db));

        // DELETE(本校区分:2)
        $query = " DELETE FROM SCHREG_STUDYREC_DAT T1 ";
        $query .= " WHERE T1.schoolcd = '2'";
        $query .= "   AND T1.YEAR ".$OPERATOR."'" .CTRL_YEAR ."'";
        $query .= "   AND EXISTS (SELECT 'X' FROM ".$model->vSchregRegdDat." W ";
        $query .= "                WHERE W.YEAR     = '" .CTRL_YEAR ."'";
        $query .= "                  AND W.grade   = '" .$model->annual  ."'";
        if (strlen($model->hr_class)) {
            $query .= "              AND W.hr_class   = '".$model->hr_class."'";
        }
        if (strlen($model->coursecode)) {
            $query .= "              AND W.coursecode = '".$model->coursecode."'";
        }
        if (strlen($model->schregno)) {
            $query .= "              AND W.schregno   = '".$model->schregno."'";
        }
        $query .= "                  AND W.SCHREGNO = T1.SCHREGNO ) ";
        $query .= $queryShomei;
        $db->query($query);

        //INSERT(本校区分:2)
        $db->query(knje061Query::insertStudyrecQuery($model, "2", "off", $db));

        //どの資格データへ取込むか(1:STUDYRECREMARK_QUALIFIED_DAT それ以外:STUDYRECREMARK_DAT)
        if ($model->Properties["useStudyrecRemarkQualifiedDat"] != 1) {
            //DELETE(備考データ)
            $query = " DELETE FROM STUDYRECREMARK_DAT T1 ";
            $query .= " WHERE T1.YEAR ".$OPERATOR."'" .CTRL_YEAR ."'";
            $query .= "   AND EXISTS (SELECT 'X' FROM ".$model->vSchregRegdDat." W ";
            $query .= "                WHERE W.YEAR     = '" .CTRL_YEAR ."'";
            $query .= "                  AND W.grade   = '" .$model->annual  ."'";
            if (strlen($model->hr_class)) {
                $query .= "              AND W.hr_class   = '".$model->hr_class."'";
            }
            if (strlen($model->coursecode)) {
                $query .= "              AND W.coursecode = '".$model->coursecode."'";
            }
            if (strlen($model->schregno)) {
                $query .= "              AND W.schregno   = '".$model->schregno."'";
            }
            $query .= "                  AND W.SCHREGNO = T1.SCHREGNO ) ";
            $query .= $queryShomei;
            $db->query($query);

            //INSERT(備考データ)
            knje061Query::insertStudyrecRemark($db, $model, "off");
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    //------------------------------------------------------------
    //成績データ追加作成
    //------------------------------------------------------------
    public function addRecordSchregStudyrecDat2($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //学年の学校種別
        $model->annualSchoolKind = knje061Query::setAnnualSchoolKind($db, $model);

        //INSERT(本校区分:0)
        $db->query(knje061Query::insertStudyrecQuery($model, "0", "on", $db));

        //INSERT(本校区分:2)
        $db->query(knje061Query::insertStudyrecQuery($model, "2", "on", $db));

        //どの資格データへ取込むか(1:STUDYRECREMARK_QUALIFIED_DAT それ以外:STUDYRECREMARK_DAT)
        if ($model->Properties["useStudyrecRemarkQualifiedDat"] != 1) {
            //INSERT(備考データ)
            knje061Query::insertStudyrecRemark($db, $model, "on");
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    //------------------------------------------------------------
    //成績データ上書作成
    //------------------------------------------------------------
    public function addRecordSchregStudyrecDat3($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //学年の学校種別
        $model->annualSchoolKind = knje061Query::setAnnualSchoolKind($db, $model);

        //作成するデータを一旦削除する(本校区分:0)
        $db->query(knje061Query::deleteStudyrecQuery($model, "0", $db));

        //INSERT(本校区分:0)
        $db->query(knje061Query::insertStudyrecQuery($model, "0", "on", $db));

        //作成するデータを一旦削除する(本校区分:2)
        $db->query(knje061Query::deleteStudyrecQuery($model, "2", $db));

        //INSERT(本校区分:2)
        $db->query(knje061Query::insertStudyrecQuery($model, "2", "on", $db));

        //どの資格データへ取込むか(1:STUDYRECREMARK_QUALIFIED_DAT それ以外:STUDYRECREMARK_DAT)
        if ($model->Properties["useStudyrecRemarkQualifiedDat"] != 1) {
            //INSERT(備考データ)
            knje061Query::insertStudyrecRemark($db, $model, "off");
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    //学籍学習記録データ削除クエリ文 === 作成するデータを一旦削除する
    public function deleteStudyrecQuery($model, $schoolcd, $db)
    {
        $query  = "";
        $query .= " DELETE FROM SCHREG_STUDYREC_DAT T1 ";
        $query .= "  WHERE (SCHOOLCD,YEAR,SCHREGNO,ANNUAL,CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SCHOOL_KIND,CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD) IN ";
        $query .= " (";
        $query .= " select tbl1.SCHOOLCD, ";
        $query .= "        tbl1.YEAR, ";
        $query .= "        tbl1.SCHREGNO, ";
        $query .= "        tbl1.ANNUAL, ";
        $query .= "        tbl1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    tbl1.SCHOOL_KIND, ";
            $query .= "    tbl1.CURRICULUM_CD, ";
        }
        $query .= "        tbl1.SUBCLASSCD ";
        $query .= "   from ( ";

        if ($schoolcd == "0") {
            $query .= knje061Query::insertStudyrecQuerySchoolcd0($model, "off", $db);
        }
        if ($schoolcd == "2") {
            $query .= knje061Query::insertStudyrecQuerySchoolcd2($model, "off");
        }

        $query .= "        ) tbl1 ";
        $query .= " ) ";

        //署名チェックに該当したデータを対象から除く。
        if ($model->Properties["useSeitoSidoYorokuShomeiKinou"] == 1) {
            $query .= "    AND NOT EXISTS (SELECT 'X' FROM ATTEST_OPINIONS_WK WK ";
            $query .= "                     WHERE WK.YEAR = T1.YEAR ";
            $query .= "                       AND WK.SCHREGNO = T1.SCHREGNO ";
            $query .= "                       AND WK.CHAGE_OPI_SEQ IS NOT NULL) ";
        }

        return $query;
    }

    //学籍学習記録データ追加クエリ文
    public function insertStudyrecQuery($model, $schoolcd, $not_exists, $db)
    {
        $query  = "";
        $query .= " insert into SCHREG_STUDYREC_DAT( ";
        $query .= " SCHOOLCD, ";
        $query .= " YEAR, ";
        $query .= " SCHREGNO, ";
        $query .= " ANNUAL, ";
        $query .= " CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " SCHOOL_KIND, ";
            $query .= " CURRICULUM_CD, ";
        }
        $query .= " SUBCLASSCD, ";
        $query .= " VALUATION, ";
        $query .= " GET_CREDIT, ";
        $query .= " ADD_CREDIT, ";
        $query .= " COMP_CREDIT, ";
        $query .= " REGISTERCD ";
        $query .= " ) ";

        if ($schoolcd == "0") {
            $query .= knje061Query::insertStudyrecQuerySchoolcd0($model, $not_exists, $db);
        }
        if ($schoolcd == "2") {
            $query .= knje061Query::insertStudyrecQuerySchoolcd2($model, $not_exists);
        }

        return $query;
    }

    //学籍学習記録データ追加クエリ文 === 本校区分:0
    public function insertStudyrecQuerySchoolcd0($model, $not_exists, $db)
    {
        // 生成範囲の条件を分ける
        $OPERATOR = ($model->field["RANGE"] == 1) ? "=" : "<=";
        $query  = "";
        $query .= " select '0' as SCHOOLCD  ";
        $query .= "        ,tbl.YEAR  ";
        $query .= "        ,tbl.SCHREGNO  ";
        $query .= "        ,tbl.ANNUAL  ";
        $query .= "        ,tbl.CLASSCD  ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    ,tbl.SCHOOL_KIND  ";
            $query .= "    ,tbl.CURRICULUM_CD  ";
        }
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
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    ,tbl1.SCHOOL_KIND  ";
            $query .= "    ,tbl1.CURRICULUM_CD  ";
        }
        $query .= "        ,tbl1.SUBCLASSCD  ";
        //教科科目名称マスタのマスタ化
        if ($model->Properties["useClassDetailDat"] == '1') {
            $query .= "        ,case when N1.CLASSCD = substr(tbl1.SUBCLASSCD,1,2) then null else tbl1.VALUATION end AS VALUATION  ";
        } else {
            $query .= "        ,case when N1.NAMECD2 = substr(tbl1.SUBCLASSCD,1,2) then null else tbl1.VALUATION end AS VALUATION  ";
        }
        $query .= "        ,tbl1.GET_CREDIT  ";
        $query .= "        ,tbl1.ADD_CREDIT  ";
        $query .= "        ,tbl1.COMP_CREDIT  ";
        $query .= "   from ( ";

        // 成績席次データ＋成績得点データ
        if ($model->recordTableDiv == "1" || $model->Properties["useRecordDat"] == 'RECORD_SCORE_HIST_DAT') {
            //自修館／法政の場合
            if ($model->schoolName == "HOUSEI" || $model->schoolName == "jisyukan") {
                $query .= "SELECT  T1.YEAR, ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "    T1.CLASSCD, ";
                    $query .= "    T1.SCHOOL_KIND, ";
                    $query .= "    T1.CURRICULUM_CD, ";
                }
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
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "                                 AND T2.CLASSCD = T1.CLASSCD ";
                    $query .= "                                 AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                    $query .= "                                 AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
                }
                $query .= "                                     AND T2.SUBCLASSCD = T1.SUBCLASSCD ";
                $query .= "                                     AND T2.SCHREGNO = T1.SCHREGNO ";
                $query .= "        LEFT JOIN ASSESS_MST T3 ON T3.ASSESSCD = '3' ";
                $query .= "                               AND T1.SCORE BETWEEN T3.ASSESSLOW AND T3.ASSESSHIGH ";
                $query .= "        LEFT JOIN (SELECT W2.YEAR, W2.SCHREGNO, MAX(W2.ANNUAL) AS ANNUAL ";
                $query .= "                     FROM ".$model->vSchregRegdDat." W2 ";
                $query .= "                    WHERE W2.YEAR ".$OPERATOR." '".CTRL_YEAR."' ";
                $query .= "                      AND EXISTS (SELECT 'X' FROM ".$model->vSchregRegdDat." W1 ";
                $query .= "                                   WHERE W1.YEAR = '".CTRL_YEAR."' ";
                $query .= "                                     AND W1.GRADE = '" .$model->annual ."' ";
                if (strlen($model->hr_class)) {
                    $query .= "     AND W1.HR_CLASS = '" .$model->hr_class."' ";
                }
                if (strlen($model->coursecode)) {
                    $query .= "     AND W1.COURSECODE = '". $model->coursecode ."' ";
                }
                if (strlen($model->schregno)) {
                    $query .= "     AND W1.SCHREGNO = '". $model->schregno ."' ";
                }
                $query .= "                                     AND W1.SCHREGNO = W2.SCHREGNO) ";
                $query .= "                   GROUP BY W2.YEAR, W2.SCHREGNO ";
                $query .= "                  ) T5 ON T1.YEAR = T5.YEAR AND T1.SCHREGNO = T5.SCHREGNO ";
                $query .= " WHERE  T1.YEAR ".$OPERATOR." '".CTRL_YEAR."' ";
                $query .= "   AND  T1.SEMESTER = '9' ";
                $query .= "   AND  T1.TESTKINDCD = '99' ";
                $query .= "   AND  T1.TESTITEMCD = '00' ";
                $query .= "   AND  T1.SUBCLASSCD NOT IN ('333333','555555','999999') ";
                $query .= "   AND  EXISTS (SELECT 'X' FROM ".$model->vSchregRegdDat." W1 ";
                $query .= "                 WHERE W1.YEAR = '".CTRL_YEAR."' ";
                $query .= "                   AND W1.GRADE = '" .$model->annual ."' ";
                if (strlen($model->hr_class)) {
                    $query .= " AND W1.HR_CLASS = '" .$model->hr_class."' ";
                }
                if (strlen($model->coursecode)) {
                    $query .= " AND W1.COURSECODE = '". $model->coursecode ."' ";
                }
                if (strlen($model->schregno)) {
                    $query .= " AND W1.SCHREGNO = '". $model->schregno ."' ";
                }
                $query .= "                   AND W1.SCHREGNO = T1.SCHREGNO) ";
                $query .= "   AND  NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT S1 ";
                $query .= "                     WHERE S1.YEAR = T1.YEAR  ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "                   AND S1.ATTEND_CLASSCD = T1.CLASSCD ";
                    $query .= "                   AND S1.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ";
                    $query .= "                   AND S1.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ";
                }
                $query .= "                       AND S1.ATTEND_SUBCLASSCD = T1.SUBCLASSCD) ";

                // 法政の場合のみ
                if ($model->schoolName == "HOUSEI") {
                    // 合併先科目(固定)
                    $query .= "UNION ALL ";
                    $query .= "SELECT  TT1.YEAR, ";
                    //教育課程対応
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $query .= "    TT1.COMBINED_CLASSCD AS CLASSCD, ";
                        $query .= "    TT1.COMBINED_SCHOOL_KIND AS SCHOOL_KIND, ";
                        $query .= "    TT1.COMBINED_CURRICULUM_CD AS CURRICULUM_CD, ";
                    }
                    $query .= "        TT1.COMBINED_SUBCLASSCD AS SUBCLASSCD, ";
                    $query .= "        TT1.SCHREGNO, ";
                    $query .= "        T5.ANNUAL, ";
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
                    //教育課程対応
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $query .= "        S1.COMBINED_CLASSCD, ";
                        $query .= "        S1.COMBINED_SCHOOL_KIND, ";
                        $query .= "        S1.COMBINED_CURRICULUM_CD, ";
                    }
                    $query .= "            S1.COMBINED_SUBCLASSCD, ";
                    $query .= "            T1.SCHREGNO, ";
                    $query .= "            round(avg(float(T1.SCORE)),0) AS SCORE ";
                    $query .= "      FROM  RECORD_RANK_DAT T1 ";
                    $query .= "            LEFT JOIN RECORD_SCORE_DAT T2 ON T2.YEAR = T1.YEAR ";
                    $query .= "                                         AND T2.SEMESTER = T1.SEMESTER ";
                    $query .= "                                         AND T2.TESTKINDCD = T1.TESTKINDCD ";
                    $query .= "                                         AND T2.TESTITEMCD = T1.TESTITEMCD ";
                    $query .= "                                         AND T2.SCORE_DIV = '00' ";
                    //教育課程対応
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $query .= "                                     AND T2.CLASSCD = T1.CLASSCD ";
                        $query .= "                                     AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                        $query .= "                                     AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
                    }
                    $query .= "                                         AND T2.SUBCLASSCD = T1.SUBCLASSCD ";
                    $query .= "                                         AND T2.SCHREGNO = T1.SCHREGNO ";
                    $query .= "           ,SUBCLASS_REPLACE_COMBINED_DAT S1 ";
                    $query .= "     WHERE  T1.YEAR ".$OPERATOR." '".CTRL_YEAR."' ";
                    $query .= "       AND  T1.SEMESTER = '9' ";
                    $query .= "       AND  T1.TESTKINDCD = '99' ";
                    $query .= "       AND  T1.TESTITEMCD = '00' ";
                    $query .= "       AND  T1.SUBCLASSCD NOT IN ('333333','555555','999999') ";
                    $query .= "       AND  EXISTS (SELECT 'X' FROM ".$model->vSchregRegdDat." W1 ";
                    $query .= "                     WHERE W1.YEAR = '".CTRL_YEAR."' ";
                    $query .= "                       AND W1.GRADE = '" .$model->annual ."' ";
                    if (strlen($model->hr_class)) {
                        $query .= " AND W1.HR_CLASS = '" .$model->hr_class."' ";
                    }
                    if (strlen($model->coursecode)) {
                        $query .= " AND W1.COURSECODE = '". $model->coursecode ."' ";
                    }
                    if (strlen($model->schregno)) {
                        $query .= " AND W1.SCHREGNO = '". $model->schregno ."' ";
                    }
                    $query .= "                       AND W1.SCHREGNO = T1.SCHREGNO) ";
                    $query .= "       AND  S1.YEAR = T1.YEAR ";
                    //教育課程対応
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $query .= "   AND  S1.ATTEND_CLASSCD = T1.CLASSCD ";
                        $query .= "   AND  S1.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ";
                        $query .= "   AND  S1.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ";
                    }
                    $query .= "       AND  S1.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ";
                    $query .= "       AND  S1.CALCULATE_CREDIT_FLG = '1' "; // 固定＝１
                    $query .= "    GROUP BY T1.YEAR, ";
                    //教育課程対応
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $query .= "        S1.COMBINED_CLASSCD, ";
                        $query .= "        S1.COMBINED_SCHOOL_KIND, ";
                        $query .= "        S1.COMBINED_CURRICULUM_CD, ";
                    }
                    $query .= "            S1.COMBINED_SUBCLASSCD, ";
                    $query .= "            T1.SCHREGNO ";
                    $query .= "    ) TT1 ";
                    $query .= "    LEFT JOIN ASSESS_MST T3 ON T3.ASSESSCD = '3' ";
                    $query .= "                           AND TT1.SCORE BETWEEN T3.ASSESSLOW AND T3.ASSESSHIGH ";
                    $query .= "    LEFT JOIN (SELECT W2.YEAR, W2.SCHREGNO, MAX(W2.ANNUAL) AS ANNUAL ";
                    $query .= "                 FROM ".$model->vSchregRegdDat." W2 ";
                    $query .= "                WHERE W2.YEAR ".$OPERATOR." '".CTRL_YEAR."' ";
                    $query .= "                  AND EXISTS (SELECT 'X' FROM ".$model->vSchregRegdDat." W1 ";
                    $query .= "                               WHERE W1.YEAR = '".CTRL_YEAR."' ";
                    $query .= "                                 AND W1.GRADE = '" .$model->annual ."' ";
                    if (strlen($model->hr_class)) {
                        $query .= " AND W1.HR_CLASS = '" .$model->hr_class."' ";
                    }
                    if (strlen($model->coursecode)) {
                        $query .= " AND W1.COURSECODE = '". $model->coursecode ."' ";
                    }
                    if (strlen($model->schregno)) {
                        $query .= " AND W1.SCHREGNO = '". $model->schregno ."' ";
                    }
                    $query .= "                                 AND W1.SCHREGNO = W2.SCHREGNO) ";
                    $query .= "               GROUP BY W2.YEAR, W2.SCHREGNO ";
                    $query .= "              ) T5 ON TT1.YEAR = T5.YEAR AND TT1.SCHREGNO = T5.SCHREGNO ";
                    $query .= "    LEFT JOIN (SELECT DISTINCT W2.YEAR, W2.SCHREGNO, ";
                    //教育課程対応
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $query .= "                  W3.CLASSCD, ";
                        $query .= "                  W3.SCHOOL_KIND, ";
                        $query .= "                  W3.CURRICULUM_CD, ";
                    }
                    $query .= "                      W3.SUBCLASSCD, ";
                    $query .= "                      W3.CREDITS ";
                    $query .= "                 FROM ".$model->vSchregRegdDat." W2, ";
                    $query .= "                      CREDIT_MST W3 ";
                    $query .= "                WHERE W2.YEAR ".$OPERATOR." '".CTRL_YEAR."' ";
                    $query .= "                  AND EXISTS (SELECT 'X' FROM ".$model->vSchregRegdDat." W1 ";
                    $query .= "                               WHERE W1.YEAR = '".CTRL_YEAR."' ";
                    $query .= "                                 AND W1.GRADE = '" .$model->annual ."' ";
                    if (strlen($model->hr_class)) {
                        $query .= " AND W1.HR_CLASS = '" .$model->hr_class."' ";
                    }
                    if (strlen($model->coursecode)) {
                        $query .= " AND W1.COURSECODE = '". $model->coursecode ."' ";
                    }
                    if (strlen($model->schregno)) {
                        $query .= " AND W1.SCHREGNO = '". $model->schregno ."' ";
                    }
                    $query .= "                                 AND W1.SCHREGNO = W2.SCHREGNO) ";
                    $query .= "                  AND W2.YEAR = W3.YEAR ";
                    $query .= "                  AND W2.COURSECD = W3.COURSECD ";
                    $query .= "                  AND W2.MAJORCD = W3.MAJORCD ";
                    $query .= "                  AND W2.GRADE = W3.GRADE ";
                    $query .= "                  AND W2.COURSECODE = W3.COURSECODE ";
                    $query .= "              ) T6 ON TT1.YEAR = T6.YEAR AND TT1.SCHREGNO = T6.SCHREGNO AND TT1.COMBINED_SUBCLASSCD = T6.SUBCLASSCD ";
                    //教育課程対応
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $query .= "              AND TT1.COMBINED_CLASSCD = T6.CLASSCD ";
                        $query .= "              AND TT1.COMBINED_SCHOOL_KIND = T6.SCHOOL_KIND ";
                        $query .= "              AND TT1.COMBINED_CURRICULUM_CD = T6.CURRICULUM_CD ";
                    }

                    // 合併先科目(加算)
                    $query .= "UNION ALL ";
                    $query .= "SELECT  TT1.YEAR, ";
                    //教育課程対応
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $query .= "    TT1.COMBINED_CLASSCD AS CLASSCD, ";
                        $query .= "    TT1.COMBINED_SCHOOL_KIND AS SCHOOL_KIND, ";
                        $query .= "    TT1.COMBINED_CURRICULUM_CD AS CURRICULUM_CD, ";
                    }
                    $query .= "        TT1.COMBINED_SUBCLASSCD AS SUBCLASSCD, ";
                    $query .= "        TT1.SCHREGNO, ";
                    $query .= "        T5.ANNUAL, ";
                    $query .= "        T3.ASSESSLEVEL AS VALUATION, ";
                    $query .= "        TT1.GET_CREDIT, ";
                    $query .= "        TT1.ADD_CREDIT, ";
                    $query .= "        TT1.COMP_CREDIT ";
                    $query .= "  FROM  ( ";
                    $query .= "    SELECT  T1.YEAR, ";
                    //教育課程対応
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $query .= "        S1.COMBINED_CLASSCD, ";
                        $query .= "        S1.COMBINED_SCHOOL_KIND, ";
                        $query .= "        S1.COMBINED_CURRICULUM_CD, ";
                    }
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
                    //教育課程対応
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $query .= "                                     AND T2.CLASSCD = T1.CLASSCD ";
                        $query .= "                                     AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                        $query .= "                                     AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
                    }
                    $query .= "                                         AND T2.SUBCLASSCD = T1.SUBCLASSCD ";
                    $query .= "                                         AND T2.SCHREGNO = T1.SCHREGNO ";
                    $query .= "           ,SUBCLASS_REPLACE_COMBINED_DAT S1 ";
                    $query .= "     WHERE  T1.YEAR ".$OPERATOR." '".CTRL_YEAR."' ";
                    $query .= "       AND  T1.SEMESTER = '9' ";
                    $query .= "       AND  T1.TESTKINDCD = '99' ";
                    $query .= "       AND  T1.TESTITEMCD = '00' ";
                    $query .= "       AND  T1.SUBCLASSCD NOT IN ('333333','555555','999999') ";
                    $query .= "       AND  EXISTS (SELECT 'X' FROM ".$model->vSchregRegdDat." W1 ";
                    $query .= "                     WHERE W1.YEAR = '".CTRL_YEAR."' ";
                    $query .= "                       AND W1.GRADE = '" .$model->annual ."' ";
                    if (strlen($model->hr_class)) {
                        $query .= " AND W1.HR_CLASS = '" .$model->hr_class."' ";
                    }
                    if (strlen($model->coursecode)) {
                        $query .= " AND W1.COURSECODE = '". $model->coursecode ."' ";
                    }
                    if (strlen($model->schregno)) {
                        $query .= " AND W1.SCHREGNO = '". $model->schregno ."' ";
                    }
                    $query .= "                       AND W1.SCHREGNO = T1.SCHREGNO) ";
                    $query .= "       AND  S1.YEAR = T1.YEAR ";
                    //教育課程対応
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $query .= "   AND  S1.ATTEND_CLASSCD = T1.CLASSCD ";
                        $query .= "   AND  S1.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ";
                        $query .= "   AND  S1.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ";
                    }
                    $query .= "       AND  S1.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ";
                    $query .= "       AND  S1.CALCULATE_CREDIT_FLG = '2' "; // 加算＝２
                    $query .= "    GROUP BY T1.YEAR, ";
                    //教育課程対応
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $query .= "        S1.COMBINED_CLASSCD, ";
                        $query .= "        S1.COMBINED_SCHOOL_KIND, ";
                        $query .= "        S1.COMBINED_CURRICULUM_CD, ";
                    }
                    $query .= "            S1.COMBINED_SUBCLASSCD, ";
                    $query .= "            T1.SCHREGNO ";
                    $query .= "    ) TT1 ";
                    $query .= "    LEFT JOIN ASSESS_MST T3 ON T3.ASSESSCD = '3' ";
                    $query .= "                           AND TT1.SCORE BETWEEN T3.ASSESSLOW AND T3.ASSESSHIGH ";
                    $query .= "    LEFT JOIN (SELECT W2.YEAR, W2.SCHREGNO, MAX(W2.ANNUAL) AS ANNUAL ";
                    $query .= "                 FROM ".$model->vSchregRegdDat." W2 ";
                    $query .= "                WHERE W2.YEAR ".$OPERATOR." '".CTRL_YEAR."' ";
                    $query .= "                  AND EXISTS (SELECT 'X' FROM ".$model->vSchregRegdDat." W1 ";
                    $query .= "                               WHERE W1.YEAR = '".CTRL_YEAR."' ";
                    $query .= "                                 AND W1.GRADE = '" .$model->annual ."' ";
                    if (strlen($model->hr_class)) {
                        $query .= " AND W1.HR_CLASS = '" .$model->hr_class."' ";
                    }
                    if (strlen($model->coursecode)) {
                        $query .= " AND W1.COURSECODE = '". $model->coursecode ."' ";
                    }
                    if (strlen($model->schregno)) {
                        $query .= " AND W1.SCHREGNO = '". $model->schregno ."' ";
                    }
                    $query .= "                                 AND W1.SCHREGNO = W2.SCHREGNO) ";
                    $query .= "               GROUP BY W2.YEAR, W2.SCHREGNO ";
                    $query .= "              ) T5 ON TT1.YEAR = T5.YEAR AND TT1.SCHREGNO = T5.SCHREGNO ";
                }//if 法政の場合
            } elseif ($model->schoolName == "osakashinnai" && SCHOOLKIND == 'P') {
                // 成績得点データ・・・大阪信愛
                $query .= "SELECT  T1.YEAR, ";
                $query .= "        T1.CLASSCD, ";
                $query .= "        T1.SCHOOL_KIND, ";
                $query .= "        T1.CURRICULUM_CD, ";
                $query .= "        T1.SUBCLASSCD, ";
                $query .= "        T1.SCHREGNO, ";
                $query .= "        T1.ANNUAL, ";
                $query .= "        CASE ";
                $query .= "            WHEN T1.SUM_SCORE BETWEEN 21 AND 30 THEN 3 ";
                $query .= "            WHEN T1.SUM_SCORE BETWEEN 10 AND 20 THEN 2 ";
                $query .= "            WHEN T1.SUM_SCORE BETWEEN  0 AND  9 THEN 1 ";
                $query .= "        END AS VALUATION, ";
                $query .= "        T1.GET_CREDIT, ";
                $query .= "        T1.ADD_CREDIT, ";
                $query .= "        T1.COMP_CREDIT ";
                $query .= "  FROM ( ";
                $query .= "    SELECT  T1.YEAR, ";
                $query .= "            T1.CLASSCD, ";
                $query .= "            T1.SCHOOL_KIND, ";
                $query .= "            T1.CURRICULUM_CD, ";
                $query .= "            T1.SUBCLASSCD, ";
                $query .= "            T1.SCHREGNO, ";
                $query .= "            T5.ANNUAL, ";
                $query .= "            CASE T1.SCORE  ";
                $query .= "                WHEN 3 THEN 10 ";
                $query .= "                WHEN 2 THEN 5 ";
                $query .= "                WHEN 1 THEN 0 ";
                $query .= "            END AS SUM_SCORE, ";
                $query .= "            T1.GET_CREDIT, ";
                $query .= "            T1.ADD_CREDIT, ";
                $query .= "            T1.COMP_CREDIT ";
                $query .= "    FROM ";
                $query .= "    RECORD_SCORE_DAT T1 ";
                $query .= "        LEFT JOIN (SELECT W2.YEAR, W2.SCHREGNO, MAX(W2.ANNUAL) AS ANNUAL ";
                $query .= "                     FROM ".$model->vSchregRegdDat." W2 ";
                $query .= "                    WHERE W2.YEAR ".$OPERATOR." '".CTRL_YEAR."' ";
                $query .= "                      AND EXISTS (SELECT 'X' FROM ".$model->vSchregRegdDat." W1 ";
                $query .= "                                   WHERE W1.YEAR = '".CTRL_YEAR."' ";
                $query .= "                                     AND W1.GRADE = '" .$model->annual ."' ";
                if (strlen($model->hr_class)) {
                    $query .= "     AND W1.HR_CLASS = '" .$model->hr_class."' ";
                }
                if (strlen($model->coursecode)) {
                    $query .= "     AND W1.COURSECODE = '". $model->coursecode ."' ";
                }
                if (strlen($model->schregno)) {
                    $query .= "     AND W1.SCHREGNO = '". $model->schregno ."' ";
                }
                $query .= "                                     AND W1.SCHREGNO = W2.SCHREGNO) ";
                $query .= "                   GROUP BY W2.YEAR, W2.SCHREGNO ";
                $query .= "                  ) T5 ON T1.YEAR = T5.YEAR AND T1.SCHREGNO = T5.SCHREGNO ";
                $query .= " WHERE  T1.YEAR ".$OPERATOR." '".CTRL_YEAR."' ";
                $query .= "   AND  T1.SEMESTER < '9' ";
                $query .= "   AND  T1.TESTKINDCD = '99' ";
                $query .= "   AND  T1.TESTITEMCD = '00' ";
                $query .= "   AND  T1.SCORE_DIV = '08' ";
                $query .= "   AND  EXISTS (SELECT 'X' FROM ".$model->vSchregRegdDat." W1 ";
                $query .= "                 WHERE W1.YEAR = '".CTRL_YEAR."' ";
                $query .= "                   AND W1.GRADE = '" .$model->annual ."' ";
                if (strlen($model->hr_class)) {
                    $query .= " AND W1.HR_CLASS = '" .$model->hr_class."' ";
                }
                if (strlen($model->coursecode)) {
                    $query .= " AND W1.COURSECODE = '". $model->coursecode ."' ";
                }
                if (strlen($model->schregno)) {
                    $query .= " AND W1.SCHREGNO = '". $model->schregno ."' ";
                }
                $query .= "                   AND W1.SCHREGNO = T1.SCHREGNO) ";
                $query .= "   AND  NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT S1 ";
                $query .= "                     WHERE S1.YEAR = T1.YEAR  ";
                $query .= "                       AND S1.ATTEND_CLASSCD = T1.CLASSCD ";
                $query .= "                       AND S1.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ";
                $query .= "                       AND S1.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ";
                $query .= "                       AND S1.ATTEND_SUBCLASSCD = T1.SUBCLASSCD) ";
                $query .= "  ) T1 ";
            } else {
                // 成績得点データ・・・自修館／法政以外の場合
                $query .= "SELECT  T1.YEAR, ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "    T1.CLASSCD, ";
                    $query .= "    T1.SCHOOL_KIND, ";
                    $query .= "    T1.CURRICULUM_CD, ";
                }
                $query .= "        T1.SUBCLASSCD, ";
                $query .= "        T1.SCHREGNO, ";
                $query .= "        T5.ANNUAL, ";
                //パーツタイプ対応
                if ($model->Properties["useRecordDat"] == 'RECORD_SCORE_HIST_DAT') {
                    if ($model->Properties["KNJE061_RECORD_SCORE_HIST_DAT"] == 'SCORE') {
                        $query .= "    T1.SCORE AS VALUATION, ";
                    } else {
                        $query .= "    T1.VALUE AS VALUATION, ";
                    }
                } elseif ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
                    $query .= "    T1.SCORE AS VALUATION, ";
                } else {
                    $query .= "    T1.VALUE AS VALUATION, ";
                }
                $query .= "        T1.GET_CREDIT, ";
                $query .= "        T1.ADD_CREDIT, ";
                $query .= "        T1.COMP_CREDIT ";
                $query .= "  FROM ";
                if ($model->Properties["useRecordDat"] == 'RECORD_SCORE_HIST_DAT') {
                    $query .= "    V_RECORD_SCORE_HIST_DAT T1 ";
                } else {
                    $query .= "    RECORD_SCORE_DAT T1 ";
                }
                $query .= "        LEFT JOIN (SELECT W2.YEAR, W2.SCHREGNO, MAX(W2.ANNUAL) AS ANNUAL ";
                $query .= "                     FROM ".$model->vSchregRegdDat." W2 ";
                $query .= "                    WHERE W2.YEAR ".$OPERATOR." '".CTRL_YEAR."' ";
                $query .= "                      AND EXISTS (SELECT 'X' FROM ".$model->vSchregRegdDat." W1 ";
                $query .= "                                   WHERE W1.YEAR = '".CTRL_YEAR."' ";
                $query .= "                                     AND W1.GRADE = '" .$model->annual ."' ";
                if (strlen($model->hr_class)) {
                    $query .= "     AND W1.HR_CLASS = '" .$model->hr_class."' ";
                }
                if (strlen($model->coursecode)) {
                    $query .= "     AND W1.COURSECODE = '". $model->coursecode ."' ";
                }
                if (strlen($model->schregno)) {
                    $query .= "     AND W1.SCHREGNO = '". $model->schregno ."' ";
                }
                $query .= "                                     AND W1.SCHREGNO = W2.SCHREGNO) ";
                $query .= "                   GROUP BY W2.YEAR, W2.SCHREGNO ";
                $query .= "                  ) T5 ON T1.YEAR = T5.YEAR AND T1.SCHREGNO = T5.SCHREGNO ";
                $query .= " WHERE  T1.YEAR ".$OPERATOR." '".CTRL_YEAR."' ";
                $query .= "   AND  T1.SEMESTER = '9' ";
                $query .= "   AND  T1.TESTKINDCD = '99' ";
                $query .= "   AND  T1.TESTITEMCD = '00' ";
                //パーツタイプ対応
                if ($model->Properties["useRecordDat"] == 'RECORD_SCORE_HIST_DAT') {
                    $query .= "   AND  T1.SCORE_DIV = '09' ";
                } elseif ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
                    $query .= "   AND  T1.SCORE_DIV = '09' ";
                } else {
                    $query .= "   AND  T1.SCORE_DIV = '00' ";
                }
                $query .= "   AND  EXISTS (SELECT 'X' FROM ".$model->vSchregRegdDat." W1 ";
                $query .= "                 WHERE W1.YEAR = '".CTRL_YEAR."' ";
                $query .= "                   AND W1.GRADE = '" .$model->annual ."' ";
                if (strlen($model->hr_class)) {
                    $query .= " AND W1.HR_CLASS = '" .$model->hr_class."' ";
                }
                if (strlen($model->coursecode)) {
                    $query .= " AND W1.COURSECODE = '". $model->coursecode ."' ";
                }
                if (strlen($model->schregno)) {
                    $query .= " AND W1.SCHREGNO = '". $model->schregno ."' ";
                }
                $query .= "                   AND W1.SCHREGNO = T1.SCHREGNO) ";
                if ($model->schoolName == "kaijyo") {
                    $query .= "   AND  NOT EXISTS (SELECT 'X' FROM SUBCLASS_WEIGHTING_COURSE_DAT S1 ";
                    $query .= "                     INNER JOIN ".$model->vSchregRegdDat." R1 ON T1.SCHREGNO = R1.SCHREGNO ";
                    $query .= "                       AND S1.YEAR = R1.YEAR  ";
                    $query .= "                       AND S1.GRADE = R1.GRADE  ";
                    $query .= "                       AND S1.COURSECD = R1.COURSECD  ";
                    $query .= "                       AND S1.MAJORCD = R1.MAJORCD  ";
                    $query .= "                       AND S1.COURSECODE = R1.COURSECODE  ";
                    $query .= "                     WHERE S1.YEAR = T1.YEAR  ";
                    $query .= "                       AND S1.FLG = '2' ";
                    //教育課程対応
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $query .= "                   AND S1.ATTEND_CLASSCD = T1.CLASSCD ";
                        $query .= "                   AND S1.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ";
                        $query .= "                   AND S1.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ";
                    }
                    $query .= "                       AND S1.ATTEND_SUBCLASSCD = T1.SUBCLASSCD) ";
                } else {
                    $query .= "   AND  NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT S1 ";
                    $query .= "                     WHERE S1.YEAR = T1.YEAR  ";
                    //教育課程対応
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $query .= "                   AND S1.ATTEND_CLASSCD = T1.CLASSCD ";
                        $query .= "                   AND S1.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ";
                        $query .= "                   AND S1.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ";
                    }
                    $query .= "                       AND S1.ATTEND_SUBCLASSCD = T1.SUBCLASSCD) ";
                }
            }//if

        // RECORD_DAT
        } else {
            $query .= "         select t1.YEAR ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "           ,t1.CLASSCD ";
                $query .= "           ,t1.SCHOOL_KIND ";
                $query .= "           ,t1.CURRICULUM_CD ";
            }
            $query .= "               ,t1.SUBCLASSCD ";
            $query .= "               ,t1.SCHREGNO ";
            $query .= "               ,t5.ANNUAL                                  ";
            $query .= "               ,round(avg(float(case when '90' <= substr(t1.SUBCLASSCD,1,2) then null else t1.GRAD_VALUE end)),0) AS VALUATION ";
            $query .= "               ,sum(t1.GET_CREDIT) AS GET_CREDIT ";
            $query .= "               ,sum(t1.ADD_CREDIT) AS ADD_CREDIT ";
            $query .= "               ,sum(t1.COMP_CREDIT) AS COMP_CREDIT ";
            $query .= "           from RECORD_DAT t1  ";
            $query .= "                LEFT JOIN (  ";
            $query .= "                    select DISTINCT W1.YEAR,W1.SCHREGNO,W1.ANNUAL ";
            $query .= "                      from ".$model->vSchregRegdDat." W1 ";
            $query .= "                     where W1.YEAR ".$OPERATOR." '".CTRL_YEAR."' ";
            $query .= "                    ) t5 ON t1.YEAR = t5.YEAR and t1.SCHREGNO = t5.SCHREGNO ";
            $query .= "          where t1.YEAR ".$OPERATOR." '".CTRL_YEAR."' ";
            $query .= "            and t1.TAKESEMES = (select max(TAKESEMES) from RECORD_DAT t2 ";
            $query .= "                                 where t2.YEAR       = t1.YEAR  ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                               and t2.CLASSCD = t1.CLASSCD ";
                $query .= "                               and t2.SCHOOL_KIND = t1.SCHOOL_KIND ";
                $query .= "                               and t2.CURRICULUM_CD = t1.CURRICULUM_CD ";
            }
            $query .= "                                   and t2.SUBCLASSCD = t1.SUBCLASSCD ";
            $query .= "                                   and t2.SCHREGNO   = t1.SCHREGNO) ";
            $query .= "            and not exists (select 'X' from SUBCLASS_REPLACE_COMBINED_DAT t3 ";
            $query .= "                             where t3.YEAR = t1.YEAR  ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                           and t3.ATTEND_CLASSCD = t1.CLASSCD ";
                $query .= "                           and t3.ATTEND_SCHOOL_KIND = t1.SCHOOL_KIND ";
                $query .= "                           and t3.ATTEND_CURRICULUM_CD = t1.CURRICULUM_CD ";
            }
            $query .= "                               and t3.ATTEND_SUBCLASSCD = t1.SUBCLASSCD) ";
            $query .= "          GROUP BY t1.YEAR, t1.SCHREGNO, t5.ANNUAL, t1.SUBCLASSCD ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "           ,t1.CLASSCD ";
                $query .= "           ,t1.SCHOOL_KIND ";
                $query .= "           ,t1.CURRICULUM_CD ";
            }
        }//if
        $query .= "         ) tbl1 ";
        //教科科目名称マスタのマスタ化
        if ($model->Properties["useClassDetailDat"] == '1') {
            $query .= "         LEFT JOIN CLASS_DETAIL_DAT N1 ON N1.YEAR        = tbl1.YEAR ";
            $query .= "                                      AND N1.CLASSCD     = substr(tbl1.SUBCLASSCD,1,2) ";
            $query .= "                                      AND N1.SCHOOL_KIND = tbl1.SCHOOL_KIND ";
            $query .= "                                      AND CLASS_SEQ      = '003' ";
        } elseif (0 < knje061query::getDSchoolKind08(CTRL_YEAR, $model->annualSchoolKind, $db)) {
            $query .= "         LEFT JOIN NAME_YDAT N1 ON N1.YEAR = tbl1.YEAR ";
            $query .= "                               AND N1.NAMECD1 = 'D{$model->annualSchoolKind}08' ";
            $query .= "                               AND N1.NAMECD2 = substr(tbl1.SUBCLASSCD,1,2) ";
        } else {
            $query .= "         LEFT JOIN NAME_YDAT N1 ON N1.YEAR = tbl1.YEAR ";
            $query .= "                               AND N1.NAMECD1 = 'D008' ";
            $query .= "                               AND N1.NAMECD2 = substr(tbl1.SUBCLASSCD,1,2) ";
        }
        if ($model->recordTableDiv == "1" || $model->Properties["useRecordDat"] == 'RECORD_SCORE_HIST_DAT') {
        } else {
            $query .= "  where exists (select 'X' from ".$model->vSchregRegdDat." W1 ";
            $query .= "                 where W1.YEAR     = '".CTRL_YEAR."' ";
//            $query .= "                   and W1.SEMESTER = '" .$model->maxSemester ."' ";
            $query .= "                   and W1.GRADE   = '" .$model->annual ."' ";
            if (strlen($model->hr_class)) {
                $query .= " and W1.hr_class   = '" .$model->hr_class."' ";
            }
            if (strlen($model->coursecode)) {
                $query .= " and W1.coursecode = '". $model->coursecode ."' ";
            }
            if (strlen($model->schregno)) {
                $query .= " and W1.schregno   = '". $model->schregno ."' ";
            }
            $query .= "                   and W1.SCHREGNO = tbl1.SCHREGNO ) ";
        }//if


        // c.資格データ(ADD_CREDITのみ)---設定区分 = 1or2
        $query .= " union all ";
        $query .= " select tbl1.YEAR, ";
        $query .= "        tbl1.SCHREGNO, ";
        $query .= "        tbl1.ANNUAL, ";
        $query .= "        substr(tbl1.SUBCLASSCD,1,2) as CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    tbl1.SCHOOL_KIND, ";
            $query .= "    tbl1.CURRICULUM_CD, ";
        }
        $query .= "        tbl1.SUBCLASSCD, ";
        $query .= "        tbl1.VALUATION, "; // null
        $query .= "        tbl1.GET_CREDIT, "; // null
        $query .= "        tbl1.ADD_CREDIT, ";
        $query .= "        tbl1.COMP_CREDIT "; // null
        $query .= "   from ( ";
        $query .= "         select t1.YEAR, t1.SCHREGNO, t1.SUBCLASSCD, t2.ANNUAL ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "           ,t1.CLASSCD ";
            $query .= "           ,t1.SCHOOL_KIND ";
            $query .= "           ,t1.CURRICULUM_CD ";
        }
        $query .= "               ,sum(case when t1.CONDITION_DIV = '3' then t1.CREDITS end) as VALUATION ";
        $query .= "               ,sum(case when t1.CONDITION_DIV = '3' then t1.CREDITS end) as GET_CREDIT ";
        $query .= "               ,sum(case when t1.CONDITION_DIV in ('1','2') then t1.CREDITS end) as COMP_CREDIT ";
        $query .= "               ,sum(case when t1.CONDITION_DIV in ('1','2') then t1.CREDITS end) as ADD_CREDIT ";
        $query .= "           from SCHREG_QUALIFIED_DAT t1, ";
        $query .= "                ( ";
        $query .= "                 select distinct W1.year, W1.schregno, W1.annual ";
        $query .= "                   from ".$model->vSchregRegdDat." W1 ";
        $query .= "                  where W1.YEAR ".$OPERATOR." '".CTRL_YEAR."' ";
        $query .= "                ) t2 ";
        $query .= "          where t1.YEAR ".$OPERATOR." '".CTRL_YEAR."' ";
        $query .= "            and t1.CONDITION_DIV in ('1','2') "; // 設定区分 = 1or2
        $query .= "            and t1.CREDITS is not null ";
        $query .= "            and t2.year = t1.year ";
        $query .= "            and t2.schregno = t1.schregno ";
        $query .= "          group by t1.YEAR, t1.SCHREGNO, t1.SUBCLASSCD, t2.ANNUAL ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "           ,t1.CLASSCD ";
            $query .= "           ,t1.SCHOOL_KIND ";
            $query .= "           ,t1.CURRICULUM_CD ";
        }
        $query .= "        ) tbl1 ";
        $query .= "  where exists (select 'X' from ".$model->vSchregRegdDat." W1 ";
        $query .= "                 where W1.YEAR     = '".CTRL_YEAR."' ";
//        $query .= "                   and W1.SEMESTER = '" .$model->maxSemester ."' ";
        $query .= "                   and W1.grade   = '" .$model->annual ."' ";
        if (strlen($model->hr_class)) {
            $query .= " and W1.hr_class   = '" .$model->hr_class."' ";
        }
        if (strlen($model->coursecode)) {
            $query .= " and W1.coursecode = '". $model->coursecode ."' ";
        }
        if (strlen($model->schregno)) {
            $query .= " and W1.schregno   = '". $model->schregno ."' ";
        }
        $query .= "                   and W1.SCHREGNO = tbl1.SCHREGNO ) ";

        $query .= "         ) tbl ";

        //既存レコードは除く
        $where = "";
        if ($not_exists == "on") {
            $where = "1";

            $query .= " where NOT EXISTS (SELECT 'X' FROM schreg_studyrec_dat WK1 ";
            $query .= "                    WHERE WK1.schoolcd   = '0' ";
            $query .= "                      AND WK1.year       = tbl.year ";
            $query .= "                      AND WK1.schregno   = tbl.schregno ";
            $query .= "                      AND WK1.annual     = tbl.annual ";
            $query .= "                      AND WK1.classcd    = tbl.classcd ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                  AND WK1.SCHOOL_KIND    = tbl.SCHOOL_KIND ";
                $query .= "                  AND WK1.CURRICULUM_CD    = tbl.CURRICULUM_CD ";
            }
            $query .= "                      AND WK1.subclasscd = tbl.subclasscd) ";
        }

        //署名チェックに該当したデータを対象から除く。
        if ($model->Properties["useSeitoSidoYorokuShomeiKinou"] == 1) {
            if ($not_exists == "on") {
                $query .= " AND ";
            } else {
                $where = "1";
                $query .= " WHERE ";
            }
            $query .= "        NOT EXISTS (SELECT 'X' FROM ATTEST_OPINIONS_WK WK ";
            $query .= "                     WHERE WK.YEAR = tbl.YEAR ";
            $query .= "                       AND WK.SCHREGNO = tbl.SCHREGNO ";
            $query .= "                       AND WK.CHAGE_OPI_SEQ IS NOT NULL) ";
        }

        //中学は名称マスタE047で設定した科目のみ取り込む
        if ($model->Properties["knje061CreateSchregStudyecDatE047J"] == 1 && $model->annualSchoolKind == 'J') {
            if ($where == "1") {
                $query .= " AND ";
            } else {
                $query .= " WHERE ";
                $where = "1";
            }
            $query .= "        EXISTS (SELECT 'X' FROM V_NAME_MST NME047 ";
            $query .= "                     WHERE ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                       NME047.NAME1 = tbl.CLASSCD || '-' || tbl.SCHOOL_KIND || '-' || tbl.CURRICULUM_CD || '-' || tbl.SUBCLASSCD ";
            } else {
                $query .= "                       NME047.NAME1 = tbl.SUBCLASSCD ";
            }
            $query .= "                       AND NME047.YEAR = '".CTRL_YEAR."') ";
        }

        $query .= "  group by ";
        $query .= "         tbl.YEAR  ";
        $query .= "        ,tbl.SCHREGNO  ";
        $query .= "        ,tbl.ANNUAL  ";
        $query .= "        ,tbl.CLASSCD  ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    ,tbl.SCHOOL_KIND ";
            $query .= "    ,tbl.CURRICULUM_CD ";
        }
        $query .= "        ,tbl.SUBCLASSCD  ";

        return $query;
    }

    //学籍学習記録データ追加クエリ文 === 本校区分:2
    public function insertStudyrecQuerySchoolcd2($model, $not_exists)
    {
        // 生成範囲の条件を分ける
        $OPERATOR = ($model->field["RANGE"] == 1) ? "=" : "<=";
        $query  = "";
        $query .= " select '2' as SCHOOLCD, ";
        $query .= "        tbl.YEAR, ";
        $query .= "        tbl.SCHREGNO, ";
        $query .= "        tbl.ANNUAL, ";
        $query .= "        tbl.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    tbl.SCHOOL_KIND, ";
            $query .= "    tbl.CURRICULUM_CD, ";
        }
        $query .= "        tbl.SUBCLASSCD, ";
        $query .= "        tbl.VALUATION, "; // null
        $query .= "        tbl.GET_CREDIT, ";
        $query .= "        tbl.ADD_CREDIT, "; // null
        $query .= "        tbl.COMP_CREDIT, "; // null
        $query .= "        '".STAFFCD."' as REGISTERCD ";
        $query .= "   from ( ";
        $query .= "         select t1.YEAR, t1.SCHREGNO, substr(t1.SUBCLASSCD,1,2) as CLASSCD, t1.SUBCLASSCD, t2.ANNUAL, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "            t1.SCHOOL_KIND, ";
            $query .= "            t1.CURRICULUM_CD, ";
        }
        $query .= "                sum(case when t1.CONDITION_DIV in ('1','2') then t1.CREDITS end) as VALUATION, ";
        $query .= "                sum(case when t1.CONDITION_DIV in ('1','2') then t1.CREDITS end) as ADD_CREDIT, ";
        $query .= "                sum(case when t1.CONDITION_DIV = '3' then t1.CREDITS end) as COMP_CREDIT, ";
        $query .= "                sum(case when t1.CONDITION_DIV = '3' then t1.CREDITS end) as GET_CREDIT ";
        $query .= "           from SCHREG_QUALIFIED_DAT t1, ";
        $query .= "                ( ";
        $query .= "                 select distinct W1.year, W1.schregno, W1.annual ";
        $query .= "                   from ".$model->vSchregRegdDat." W1 ";
        $query .= "                  where W1.YEAR ".$OPERATOR." '".CTRL_YEAR."' ";
        $query .= "                ) t2 ";
        $query .= "          where t1.YEAR ".$OPERATOR." '".CTRL_YEAR."' ";
        $query .= "            and t1.CONDITION_DIV = '3' "; // 設定区分 = 3
        $query .= "            and t1.CREDITS is not null ";
        $query .= "            and t2.year = t1.year ";
        $query .= "            and t2.schregno = t1.schregno ";
        $query .= "          group by t1.YEAR, t1.SCHREGNO, substr(t1.SUBCLASSCD,1,2), t1.SUBCLASSCD, t2.ANNUAL ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "           ,t1.SCHOOL_KIND ";
            $query .= "           ,t1.CURRICULUM_CD ";
        }
        $query .= "        ) tbl ";
        $query .= "  where exists (select 'X' from ".$model->vSchregRegdDat." W1 ";
        $query .= "                 where W1.YEAR     = '" .CTRL_YEAR ."' ";
//        $query .= "                   and W1.SEMESTER = '" .$model->maxSemester ."' ";
        $query .= "                   and W1.grade   = '" .$model->annual ."' ";
        if (strlen($model->hr_class)) {
            $query .= " and W1.hr_class   = '" .$model->hr_class."' ";
        }
        if (strlen($model->coursecode)) {
            $query .= " and W1.coursecode = '". $model->coursecode ."' ";
        }
        if (strlen($model->schregno)) {
            $query .= " and W1.schregno   = '". $model->schregno ."' ";
        }
        $query .= "                   and W1.SCHREGNO = tbl.SCHREGNO ) ";

        //既存レコードは除く
        if ($not_exists == "on") {
            $query .= "  AND NOT EXISTS  (SELECT 'X' FROM schreg_studyrec_dat WK1 ";
            $query .= "                    WHERE WK1.schoolcd   = '2' ";
            $query .= "                      AND WK1.year       = tbl.year ";
            $query .= "                      AND WK1.schregno   = tbl.schregno ";
            $query .= "                      AND WK1.annual     = tbl.annual ";
            $query .= "                      AND WK1.classcd    = tbl.classcd ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                  AND WK1.SCHOOL_KIND    = tbl.SCHOOL_KIND ";
                $query .= "                  AND WK1.CURRICULUM_CD    = tbl.CURRICULUM_CD ";
            }
            $query .= "                      AND WK1.subclasscd = tbl.subclasscd) ";
        }

        //署名チェックに該当したデータを対象から除く。
        if ($model->Properties["useSeitoSidoYorokuShomeiKinou"] == 1) {
            $query .= "    AND NOT EXISTS (SELECT 'X' FROM ATTEST_OPINIONS_WK WK ";
            $query .= "                     WHERE WK.YEAR = tbl.YEAR ";
            $query .= "                       AND WK.SCHREGNO = tbl.SCHREGNO ";
            $query .= "                       AND WK.CHAGE_OPI_SEQ IS NOT NULL) ";
        }

        //中学は名称マスタE047で設定した科目のみ取り込む
        if ($model->Properties["knje061CreateSchregStudyecDatE047J"] == 1 && $model->annualSchoolKind == 'J') {
            $query .= " AND EXISTS (SELECT 'X' FROM V_NAME_MST NME047 ";
            $query .= "                     WHERE ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                       NME047.NAME1 = tbl.CLASSCD || '-' || tbl.SCHOOL_KIND || '-' || tbl.CURRICULUM_CD || '-' || tbl.SUBCLASSCD ";
            } else {
                $query .= "                       NME047.NAME1 = tbl.SUBCLASSCD ";
            }
            $query .= "                       AND NME047.YEAR = '".CTRL_YEAR."') ";
        }

        return $query;
    }

    //学籍学習記録「備考」データの削除 === 作成するデータを一旦削除する
    public function deleteStudyrecRemark($model)
    {
        $query  = "";
        $query .= " DELETE FROM STUDYRECREMARK_DAT T1 ";
        $query .= "  WHERE (YEAR,SCHREGNO,CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD) IN ";
        $query .= " (";
        $query .= " select tbl1.YEAR, ";
        $query .= "        tbl1.SCHREGNO, ";
        $query .= "        tbl1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    tbl1.SCHOOL_KIND, ";
            $query .= "    tbl1.CURRICULUM_CD, ";
        }
        $query .= "        tbl1.SUBCLASSCD ";
        $query .= "   from ( ";

        $query .= knje061Query::getInsertStudyrecRemarkQuery($model, "off");

        $query .= "        ) tbl1 ";
        $query .= " ) ";

        //署名チェックに該当したデータを対象から除く。
        if ($model->Properties["useSeitoSidoYorokuShomeiKinou"] == 1) {
            $query .= "    AND NOT EXISTS (SELECT 'X' FROM ATTEST_OPINIONS_WK WK ";
            $query .= "                     WHERE WK.YEAR = T1.YEAR ";
            $query .= "                       AND WK.SCHREGNO = T1.SCHREGNO ";
            $query .= "                       AND WK.CHAGE_OPI_SEQ IS NOT NULL) ";
        }

        return $query;
    }

    //学籍学習記録「備考」データの作成
    public function insertStudyrecRemark($db, $model, $not_exists)
    {
        $opt = array();
        $cmp_key = $tmp_remark = "";
        $query  = "order by tbl.YEAR, tbl.SCHREGNO, tbl.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     tbl.SCHOOL_KIND, ";
            $query .= "     tbl.CURRICULUM_CD, ";
        }
        $query .= "         tbl.SUBCLASSCD ";
        $result = $db->query(knje061Query::getInsertStudyrecRemarkQuery($model, $not_exists) .$query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $keyRow  = $row["YEAR"];
            $keyRow .= $row["SCHREGNO"];
            $keyRow .= $row["CLASSCD"];
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $keyRow .= $row["SCHOOL_KIND"];
                $keyRow .= $row["CURRICULUM_CD"];
            }
            $keyRow .= $row["SUBCLASSCD"];

            if ($cmp_key != "" && $cmp_key !== $keyRow) {
                knje061Query::deleteStudyrecRemarkIfExists($db, $cmp_key, $model); // $cmp_keyのレコードを削除
                $db->query(knje061Query::setInsertStudyrecRemarkQuery($opt, $model));
                $tmp_remark = $row["REMARK"];
            } else {
                // 備考が複数レコード存在する場合、足していく === 90バイトまで
                if (strlen($tmp_remark .$row["REMARK"]) < 91) {
                    $tmp_remark .= $row["REMARK"];
                }
            }
            $row["REMARK"] = $tmp_remark;
            $cmp_key = $keyRow;
            $opt = $row;
        }

        // 最後のレコードを処理
        if ($cmp_key != "") {
            knje061Query::deleteStudyrecRemarkIfExists($db, $cmp_key, $model); // $cmp_keyのレコードを削除
            $db->query(knje061Query::setInsertStudyrecRemarkQuery($opt, $model));
        }
    }
    
    public function deleteStudyrecRemarkIfExists($db, $cmp_key, $model)
    {
        $query = "";
        $query .= " SELECT COUNT(*) FROM STUDYRECREMARK_DAT ";
        $query .= "   WHERE YEAR || SCHREGNO || CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    || SCHOOL_KIND || CURRICULUM_CD ";
        }
        $query .= "    || SUBCLASSCD = '".$cmp_key."' ";
        if ($db->getOne($query) > 0) {
            $query = "";
            $query .= " DELETE FROM STUDYRECREMARK_DAT ";
            $query .= "   WHERE YEAR || SCHREGNO || CLASSCD ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    || SCHOOL_KIND || CURRICULUM_CD ";
            }
            $query .= "    || SUBCLASSCD = '".$cmp_key."' ";
            $db->query($query);
        }
    }

    //学籍学習記録「備考」データの作成
    public function setInsertStudyrecRemarkQuery($data, $model)
    {
        $datas = array();
        $datas["YEAR"][TEXT]             = $data["YEAR"];
        $datas["SCHREGNO"][TEXT]         = $data["SCHREGNO"];
        $datas["CLASSCD"][TEXT]          = $data["CLASSCD"];
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $datas["SCHOOL_KIND"][TEXT]     = $data["SCHOOL_KIND"];
            $datas["CURRICULUM_CD"][TEXT]   = $data["CURRICULUM_CD"];
        }
        $datas["SUBCLASSCD"][TEXT]       = $data["SUBCLASSCD"];
        $datas["REMARK"][TEXT]           = $data["REMARK"];
        $datas["REGISTERCD"][TEXT]       = STAFFCD;
//        $datas["UPDATED"][FUNC]          = "SYSDATE()";
        $query = Query::insertSQL($datas, "STUDYRECREMARK_DAT");
        return $query;
    }

    //学籍学習記録備考データ追加クエリ文
    public function getInsertStudyrecRemarkQuery($model, $not_exists)
    {
        // 生成範囲の条件を分ける
        $OPERATOR = ($model->field["RANGE"] == 1) ? "=" : "<=";
        $query  = "";
        $query .= " select tbl.YEAR, ";
        $query .= "        tbl.SCHREGNO, ";
        $query .= "        tbl.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    tbl.SCHOOL_KIND, ";
            $query .= "    tbl.CURRICULUM_CD, ";
        }
        $query .= "        tbl.SUBCLASSCD, ";
        $query .= "        tbl.REMARK, ";
        $query .= "        '".STAFFCD."' as REGISTERCD ";
        $query .= "   from ( ";
        $query .= "         select t1.YEAR, ";
        $query .= "                t1.SCHREGNO, ";
        $query .= "                substr(t1.SUBCLASSCD,1,2) as CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "            t1.SCHOOL_KIND, ";
            $query .= "            t1.CURRICULUM_CD, ";
        }
        $query .= "                t1.SUBCLASSCD, ";
        $query .= "                t1.REMARK ";
        $query .= "           from SCHREG_QUALIFIED_DAT t1 ";
        $query .= "          where t1.YEAR ".$OPERATOR." '".CTRL_YEAR."' ";
        $query .= "        ) tbl ";
        $query .= "  where exists (select 'X' from ".$model->vSchregRegdDat." W1 ";
        $query .= "                 where W1.YEAR     = '" .CTRL_YEAR ."' ";
//        $query .= "                   and W1.SEMESTER = '" .$model->maxSemester ."' ";
        $query .= "                   and W1.grade   = '" .$model->annual ."' ";
        if (strlen($model->hr_class)) {
            $query .= " and W1.hr_class   = '" .$model->hr_class."' ";
        }
        if (strlen($model->coursecode)) {
            $query .= " and W1.coursecode = '". $model->coursecode ."' ";
        }
        if (strlen($model->schregno)) {
            $query .= " and W1.schregno   = '". $model->schregno ."' ";
        }
        $query .= "                   and W1.SCHREGNO = tbl.SCHREGNO ) ";

        //既存レコードは除く
        if ($not_exists == "on") {
            $query .= "  AND NOT EXISTS  (SELECT 'X' FROM studyrecremark_dat WK1 ";
            $query .= "                    WHERE WK1.year       = tbl.year ";
            $query .= "                      AND WK1.schregno   = tbl.schregno ";
            $query .= "                      AND WK1.classcd    = tbl.classcd ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                  AND WK1.SCHOOL_KIND    = tbl.SCHOOL_KIND ";
                $query .= "                  AND WK1.CURRICULUM_CD  = tbl.CURRICULUM_CD ";
            }
            $query .= "                      AND WK1.subclasscd = tbl.subclasscd) ";
        }

        //署名チェックに該当したデータを対象から除く。
        if ($model->Properties["useSeitoSidoYorokuShomeiKinou"] == 1) {
            $query .= "    AND NOT EXISTS (SELECT 'X' FROM ATTEST_OPINIONS_WK WK ";
            $query .= "                     WHERE WK.YEAR = tbl.YEAR ";
            $query .= "                       AND WK.SCHREGNO = tbl.SCHREGNO ";
            $query .= "                       AND WK.CHAGE_OPI_SEQ IS NOT NULL) ";
        }

        return $query;
    }


    //集計月の取得
    public function getMonthSemeArray($db, $model)
    {
        $opt_month = array();

        $value_flg = false; //出欠データは選択した集計月までの範囲内のデータを生成する
        $query = knje061query::selectSemesAll();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            for ($i = $row["S_MONTH"]; $i <= $row["E_MONTH"]; $i++) {
                if ($i > 12) {
                    $month = $i - 12;
                } else {
                    $month = $i;
                }

                $value = sprintf('%02d', $month) . '-' . $row["SEMESTER"];

                if (!$value_flg) {
                    $opt_month[] = $value;
                }

                if ($model->target_month == $value) {
                    $value_flg = true;
                }
            }
        }
        $result->free();

        return $opt_month;
    }

    //------------------------------------------------------------
    //学籍出欠記録データ作成処理
    //------------------------------------------------------------
    public function addRecordSchregAttendrecDat($model)
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
        if ($model->field["RANGE"] == 1) {
            $OPERATOR = "=";
        } else {
            $OPERATOR = "<=";
        }

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //集計月の取得
        $monthSemeArray = knje061query::getMonthSemeArray($db, $model);

        //学籍出欠記録データ削除
        $query  = " DELETE FROM schreg_attendrec_dat T1 ";
        $query .= "  WHERE T1.schoolcd = '0'";
        $query .= "    AND T1.year ".$OPERATOR." '" .CTRL_YEAR ."' ";
        $query .= "    AND EXISTS (SELECT 'X' FROM ".$model->vSchregRegdDat." W ";
        $query .= "                 WHERE W.year     = '" .CTRL_YEAR ."' ";
//        $query .= "                   AND W.semester = '" .$model->maxSemester ."' ";
        $query .= "                   AND W.grade   = '" .$model->annual  ."'";
        if (strlen($model->hr_class)) {
            $query .= " AND W.hr_class   = '". $model->hr_class."'";
        }
        if (strlen($model->coursecode)) {
            $query .= " AND W.coursecode = '". $model->coursecode."'";
        }
        if (strlen($model->schregno)) {
            $query .= " AND W.schregno   = '". $model->schregno ."'";
        }
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

        $query .= "     SUM(COALESCE(T2.suspend, 0)) ";
        if ($model->Properties["useVirus"] == 'true') {
            $query .= "     + SUM(COALESCE(T2.VIRUS, 0)) ";
        }
        if ($model->Properties["useKoudome"] == 'true') {
            $query .= "     + SUM(COALESCE(T2.KOUDOME, 0)) ";
        }
        $query .= "     AS suspend, ";

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
//        $query .= "                   AND W1.semester = '" .$model->maxSemester ."'";
        $query .= "                   AND W1.grade   = '" .$model->annual ."'";
        if (strlen($model->hr_class)) {
            $query .= " AND W1.hr_class   = '". $model->hr_class."'";
        }
        if (strlen($model->coursecode)) {
            $query .= " AND W1.coursecode = '". $model->coursecode."'";
        }
        if (strlen($model->schregno)) {
            $query .= " AND W1.schregno   = '". $model->schregno ."'";
        }
        $query .= "                   AND W1.schregno = T1.schregno) ";
        $query .= $queryShomei;
        $query .= "     ) T1 ";
        $query .= "     LEFT JOIN ( ";
        $query .= "         SELECT ";
        $query .= "             * ";
        $query .= "         FROM ";
        $query .= "             ATTEND_SEMES_DAT ";
        $query .= "         WHERE ";
        $query .= "             YEAR = '".CTRL_YEAR."' ";
        $query .= "             AND MONTH || '-' || SEMESTER IN ('".implode("','", $monthSemeArray)."') ";
        $query .= "     ) T2 ON T1.schregno = T2.schregno AND T1.year = T2.year ";
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
//        $query .= "             W1.SEMESTER = '" .$model->maxSemester ."' AND ";
        $query .= "             W1.grade = '" .$model->annual ."' AND ";
        if (strlen($model->hr_class)) {
            $query .= " W1.HR_CLASS   = '". $model->hr_class."' AND ";
        }
        if (strlen($model->coursecode)) {
            $query .= " W1.COURSECODE = '". $model->coursecode."' AND ";
        }
        if (strlen($model->schregno)) {
            $query .= " W1.SCHREGNO   = '". $model->schregno ."' AND ";
        }
        $query .= "             W1.SCHREGNO = T1.SCHREGNO ";
        $query .= "           ) ";
        $query .= "  AND T1.YEAR ".$OPERATOR."'".CTRL_YEAR."'";
        $query .= $queryShomei;
        $db->query($query);
        
        $db->commit();
        Query::dbCheckIn($db);
    }

    //署名チェック
    public function getOpinionsWk($data)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ATTEST_OPINIONS_WK ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '" .$data["YEAR"] ."' AND ";
        $query .= "     SCHREGNO = '" .sprintf("%08d", $data["SCHREGNO"]) ."' ";
        return $query;
    }

    //------------------------------------------------------------
    //学籍出欠記録データの削除(ＣＳＶデータ書込み前)
    //------------------------------------------------------------
    public function deleteQuerySchregAttendRecDat($data)
    {
        $query  = " DELETE FROM SCHREG_ATTENDREC_DAT ";
        $query .= "  WHERE SCHOOLCD   = '" .$data["SCHOOLCD"] ."'";
        $query .= "    AND YEAR       = '" .$data["YEAR"] ."'";
        $query .= "    AND SCHREGNO   = '" .sprintf("%08d", $data["SCHREGNO"]) ."'";
        $query .= "    AND ANNUAL     = '" .sprintf("%02d", $data["ANNUAL"]) ."'";
        return $query;
    }

    //------------------------------------------------------------
    //学籍出欠記録データの作成（ＣＳＶデータより読込）
    //------------------------------------------------------------
    public function addQuerySchregAttendRecDat($data)
    {
        $datas = array();
        $datas["SCHOOLCD"][TEXT]           = $data["SCHOOLCD"];
        $datas["YEAR"][TEXT]               = $data["YEAR"];
        $datas["SCHREGNO"][TEXT]           = sprintf("%08d", $data["SCHREGNO"]);
        $datas["ANNUAL"][TEXT]             = sprintf("%02d", $data["ANNUAL"]);
        $datas["SUMDATE"][TEXT]            = str_replace('/', '-', $data["SUMDATE"]);
        $datas["CLASSDAYS"][NUMBER]        = ((is_numeric($data["CLASSDAYS"]))        ? $data["CLASSDAYS"]        : "NULL");
        $datas["OFFDAYS"][NUMBER]          = ((is_numeric($data["OFFDAYS"]))          ? $data["OFFDAYS"]          : "NULL");
        $datas["ABSENT"][NUMBER]           = ((is_numeric($data["ABSENT"]))           ? $data["ABSENT"]           : "NULL");
        $datas["SUSPEND"][NUMBER]          = ((is_numeric($data["SUSPEND"]))          ? $data["SUSPEND"]          : "NULL");
        $datas["MOURNING"][NUMBER]         = ((is_numeric($data["MOURNING"]))         ? $data["MOURNING"]         : "NULL");
        $datas["ABROAD"][NUMBER]           = ((is_numeric($data["ABROAD"]))           ? $data["ABROAD"]           : "NULL");
        $datas["REQUIREPRESENT"][NUMBER]   = ((is_numeric($data["REQUIREPRESENT"]))   ? $data["REQUIREPRESENT"]   : "NULL");
        $datas["SICK"][NUMBER]             = ((is_numeric($data["SICK"]))             ? $data["SICK"]             : "NULL");
        $datas["ACCIDENTNOTICE"][NUMBER]   = ((is_numeric($data["ACCIDENTNOTICE"]))   ? $data["ACCIDENTNOTICE"]   : "NULL");
        $datas["NOACCIDENTNOTICE"][NUMBER] = ((is_numeric($data["NOACCIDENTNOTICE"])) ? $data["NOACCIDENTNOTICE"] : "NULL");
        $datas["PRESENT"][NUMBER]          = ((is_numeric($data["PRESENT"]))          ? $data["PRESENT"]          : "NULL");
        $datas["REGISTERCD"][TEXT]         = STAFFCD;
        $datas["UPDATED"][FUNC]            = "SYSDATE()";

        $query = Query::insertSQL($datas, "SCHREG_ATTENDREC_DAT");
        return $query;
    }


    //------------------------------------------------------------
    //STUDYREC_PROV_FLG_DATの削除(ＣＳＶデータ書込み前)
    //------------------------------------------------------------
    public function deleteStudyRecProvFlgCsv($data, $model)
    {
        $query  = " DELETE FROM STUDYREC_PROV_FLG_DAT ";
        $query .= "  WHERE SCHOOLCD   = '" .$data["SCHOOLCD"] ."'";
        $query .= "    AND YEAR       = '" .$data["YEAR"] ."'";
        $query .= "    AND SCHREGNO   = '" .sprintf("%08d", $data["SCHREGNO"]) ."'";
        $query .= "    AND CLASSCD    = '" .sprintf("%02d", $data["CLASSCD"]) ."'";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    AND SCHOOL_KIND      = '" .$data["SCHOOL_KIND"] ."'";
            $query .= "    AND CURRICULUM_CD    = '" .$data["CURRICULUM_CD"] ."'";
        }
        $query .= "    AND SUBCLASSCD = '" .sprintf("%06d", $data["SUBCLASSCD"]) ."'";
        return $query;
    }

    //------------------------------------------------------------
    //STUDYREC_PROV_FLG_DATの作成(ＣＳＶデータより読込)
    //------------------------------------------------------------
    public function insertStudyRecProvFlgCsv($data, $model)
    {
        $datas = array();
        $datas["SCHOOLCD"][TEXT]         = $data["SCHOOLCD"];
        $datas["YEAR"][TEXT]             = $data["YEAR"];
        $datas["SCHREGNO"][TEXT]         = sprintf("%08d", $data["SCHREGNO"]);
        $datas["CLASSCD"][TEXT]          = sprintf("%02d", $data["CLASSCD"]);
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $datas["SCHOOL_KIND"][TEXT]     = $data["SCHOOL_KIND"];
            $datas["CURRICULUM_CD"][TEXT]   = $data["CURRICULUM_CD"];
        }
        $datas["SUBCLASSCD"][TEXT]       = sprintf("%06d", $data["SUBCLASSCD"]);
        $datas["PROV_FLG"][TEXT]          = "1";
        $datas["REGISTERCD"][TEXT]       = STAFFCD;
        $datas["UPDATED"][FUNC]          = "SYSDATE()";

        $query = Query::insertSQL($datas, "STUDYREC_PROV_FLG_DAT");
        return $query;
    }

    //------------------------------------------------------------
    //学籍学習記録データの削除(ＣＳＶデータ書込み前)
    //------------------------------------------------------------
    public function deleteQuerySchregStudyRecDat($data, $model)
    {
        $query  = " DELETE FROM SCHREG_STUDYREC_DAT ";
        $query .= "  WHERE SCHOOLCD   = '" .$data["SCHOOLCD"] ."'";
        $query .= "    AND YEAR       = '" .$data["YEAR"] ."'";
        $query .= "    AND SCHREGNO   = '" .sprintf("%08d", $data["SCHREGNO"]) ."'";
        $query .= "    AND ANNUAL     = '" .sprintf("%02d", $data["ANNUAL"]) ."'";
        $query .= "    AND CLASSCD    = '" .sprintf("%02d", $data["CLASSCD"]) ."'";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    AND SCHOOL_KIND      = '" .$data["SCHOOL_KIND"] ."'";
            $query .= "    AND CURRICULUM_CD    = '" .$data["CURRICULUM_CD"] ."'";
        }
        $query .= "    AND SUBCLASSCD = '" .sprintf("%06d", $data["SUBCLASSCD"]) ."'";
        return $query;
    }

    //------------------------------------------------------------
    //学籍学習記録データの作成（ＣＳＶデータより読込）
    //------------------------------------------------------------
    public function addQuerySchregStudyRecDat($data, $model)
    {
        $datas = array();
        $datas["SCHOOLCD"][TEXT]         = $data["SCHOOLCD"];
        $datas["YEAR"][TEXT]             = $data["YEAR"];
        $datas["SCHREGNO"][TEXT]         = sprintf("%08d", $data["SCHREGNO"]);
        $datas["ANNUAL"][TEXT]           = sprintf("%02d", $data["ANNUAL"]);
        $datas["CLASSCD"][TEXT]          = sprintf("%02d", $data["CLASSCD"]);
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $datas["SCHOOL_KIND"][TEXT]     = $data["SCHOOL_KIND"];
            $datas["CURRICULUM_CD"][TEXT]   = $data["CURRICULUM_CD"];
        }
        $datas["SUBCLASSCD"][TEXT]       = sprintf("%06d", $data["SUBCLASSCD"]);
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
    public function deleteStudyRecRemarkCsv($data, $model)
    {
        $query  = " DELETE FROM STUDYRECREMARK_DAT ";
        $query .= " WHERE YEAR       = '".$data["YEAR"]."'";
        $query .= "   AND SCHREGNO   = '".sprintf("%08d", $data["SCHREGNO"])."'";
        $query .= "   AND CLASSCD    = '".sprintf("%02d", $data["CLASSCD"])."'";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    AND SCHOOL_KIND      = '" .$data["SCHOOL_KIND"] ."'";
            $query .= "    AND CURRICULUM_CD    = '" .$data["CURRICULUM_CD"] ."'";
        }
        $query .= "   AND SUBCLASSCD = '".sprintf("%06d", $data["SUBCLASSCD"])."'";
        return $query;
    }

    //------------------------------------------------------------
    //学籍学習記録「備考」データの作成
    //------------------------------------------------------------
    public function insertStudyRecRemarkCsv($data, $model)
    {
        $datas = array();
        $datas["YEAR"][TEXT]             = $data["YEAR"];
        $datas["SCHREGNO"][TEXT]         = sprintf("%08d", $data["SCHREGNO"]);
        $datas["CLASSCD"][TEXT]          = sprintf("%02d", $data["CLASSCD"]);
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $datas["SCHOOL_KIND"][TEXT]     = $data["SCHOOL_KIND"];
            $datas["CURRICULUM_CD"][TEXT]   = $data["CURRICULUM_CD"];
        }
        $datas["SUBCLASSCD"][TEXT]       = sprintf("%06d", $data["SUBCLASSCD"]);
        $datas["REMARK"][TEXT]           = $data["REMARK"];
        $datas["REGISTERCD"][TEXT]       = STAFFCD;
        $datas["UPDATED"][FUNC]          = "SYSDATE()";
        $query = Query::insertSQL($datas, "STUDYRECREMARK_DAT");
        return $query;
    }

    /******************************************/
    /* ↓↓↓ 観点データ(玉川聖)(成城) ↓↓↓ */
    /******************************************/

    //元になるテストCD取得・・・成績から観点を生成する処理に使用
    public function getNameMstE064()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 "; //(玉川聖)9990008 (成城)9990009
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'E064' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //成績データ(9-990008)から観点データを生成（観点変換テーブル参照）
    public function getRecordScoreDatTamagawa($db, $model, $schregNo, $year)
    {
        $query = knje061Query::getRecordScoreDatTamagawaSql($model, $schregNo, $year);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //評定があれば生成
            if (strlen($row["SCORE"])) {
                //観点マスタがあれば生成
                $viewcdArray = knje061Query::getJviewnameSubMst($db, $model, $row, $year);
                if (get_count($viewcdArray) > 0) {
                    //観点配列
                    $kantenArray = array();
                    for ($i = 0; $i < get_count($viewcdArray); $i++) {
                        $fieldNo = $i + 1;
                        $kanten = $db->getOne(knje061Query::getJviewSubMstSql($model, $fieldNo, $row, $year));
                        $kantenArray[] = $kanten;
                    }
                    //観点はランダムに割振る(成城)
                    if ($model->Properties["useKantenGakunenSeiseki"] == '3') {
                        //乱数生成器を初期化
                        //PHP4.2以前は、必要らしいのでsrandを使用する。
                        srand((double)microtime()*1000000);
                        shuffle($kantenArray);
                    }
                    //観点データ追加
                    for ($i = 0; $i < get_count($viewcdArray); $i++) {
                        knje061Query::insData($db, $model, $schregNo, $viewcdArray[$i], $kantenArray[$i], $row, $year);
                    }
                }
            }
        }
        $result->free();
    }

    //成績データ取得
    public function getRecordScoreDatTamagawaSql($model, $schregNo, $year)
    {
        $query  = " SELECT ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.SCORE ";
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' ";
        $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '{$model->e064name1}' ";
        $query .= "     AND T1.SCHREGNO = '{$schregNo}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD ";

        return $query;
    }

    //観点変換テーブルから観点を取得
    public function getJviewSubMstSql($model, $fieldNo, $row, $year)
    {
        $query  = " SELECT ";
        $query .= "     JVIEW{$fieldNo} AS JVIEW ";
        $query .= " FROM ";
        $query .= "     JVIEW_SUB_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR            = '{$year}' ";
        $query .= "     AND GRADE           = '{$model->annual}' ";
        $query .= "     AND CLASSCD         = '{$row["CLASSCD"]}' ";
        $query .= "     AND SCHOOL_KIND     = '{$row["SCHOOL_KIND"]}' ";
        $query .= "     AND CURRICULUM_CD   = '{$row["CURRICULUM_CD"]}' ";
        $query .= "     AND SUBCLASSCD      = '{$row["SUBCLASSCD"]}' ";
        $query .= "     AND SCORE           = {$row["SCORE"]} ";

        return $query;
    }

    /**********************************/
    /* ↓↓↓ 観点データ(海城) ↓↓↓ */
    /**********************************/

    //成績データの学年成績から観点データを生成
    public function getRecordScoreDat($db, $model, $schregNo, $year)
    {
        $query = knje061Query::getRecordScoreDatSql($model, $schregNo, $year);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学年成績があれば生成
            if (strlen($row["VALUE"])) {
                //観点マスタがあれば生成
                $viewcdArray = knje061Query::getJviewnameSubMst($db, $model, $row, $year);
                if (get_count($viewcdArray) > 0) {
                    //観点配列
                    $kantenArray = array();
                    for ($i = 0; $i < get_count($viewcdArray); $i++) {
                        $fieldNo = $i + 1;
                        $kanten = $db->getOne(knje061Query::getJviewCntMstSql($row["VALUE"], $fieldNo, $year));
                        $kantenArray[] = $kanten;
                    }
                    //観点データ追加
                    for ($i = 0; $i < get_count($viewcdArray); $i++) {
                        knje061Query::insData($db, $model, $schregNo, $viewcdArray[$i], $kantenArray[$i], $row, $year);
                    }
                }
            }
        }
        $result->free();
    }

    //成績データ取得
    public function getRecordScoreDatSql($model, $schregNo, $year)
    {
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.VALUE ";
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$year."' ";
        $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '9990100' ";
        $query .= "     AND T1.SCHREGNO = '".$schregNo."' ";
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

    //学年成績から観点を取得
    public function getJviewCntMstSql($score, $fieldNo, $year)
    {
        $query  = " SELECT ";
        $query .= "     T1.JVIEW{$fieldNo} AS JVIEW ";
        $query .= " FROM ";
        $query .= "     JVIEW_CNT_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$year."' ";
        $query .= "     AND T1.SCORE = ".$score." ";

        return $query;
    }

    /**********************************/
    /* ↓↓↓ 観点データ(大宮) ↓↓↓ */
    /**********************************/

    //学籍学習記録データの評定から観点データをランダム生成
    public function getSchregStudyrecDat($db, $model, $schregNo, $year)
    {
        $query = knje061Query::getSchregStudyrecDatSql($model, $schregNo, $year);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //評定が1～5であれば生成
            if (1 <= $row["VALUATION"] && $row["VALUATION"] <= 5) {
                //観点マスタがあれば生成
                $viewcdArray = knje061Query::getJviewnameSubMst($db, $model, $row, $year);
                if (get_count($viewcdArray) > 0) {
                    if ($model->schoolName == "kmirai") {
                        //開智未来の場合
                        // 評定5　⇒　観点すべてA
                        // 評定4　⇒　観点2つB/その他A
                        // 評定3　⇒　観点すべてB
                        // 評定2　⇒　観点3つB/その他C
                        // 評定1　⇒　観点すべてC
                        //Bの数
                        if ($row["VALUATION"] == 4) {
                            $cntB = 2;
                        } elseif ($row["VALUATION"] == 2) {
                            $cntB = 3;
                        }
                        //観点配列
                        $kantenArray = array();
                        for ($i = 0; $i < get_count($viewcdArray); $i++) {
                            if ($row["VALUATION"] == 5) {
                                $kantenArray[] = "A";
                            } elseif ($row["VALUATION"] == 4) {
                                $kantenArray[] = ($cntB > 0) ? "B" : "A";
                                $cntB--;
                            } elseif ($row["VALUATION"] == 3) {
                                $kantenArray[] = "B";
                            } elseif ($row["VALUATION"] == 2) {
                                $kantenArray[] = ($cntB > 0) ? "B" : "C";
                                $cntB--;
                            } elseif ($row["VALUATION"] == 1) {
                                $kantenArray[] = "C";
                            }
                        }
                    } else {
                        //Bの数
                        $cntB = knje061Query::getCntB($row["VALUATION"]);
                        //観点配列
                        $kantenArray = array();
                        for ($i = 0; $i < get_count($viewcdArray); $i++) {
                            $kantenArray[] = ($cntB > 0) ? "B" : "A";
                            $cntB--;
                        }
                    }
                    //乱数生成器を初期化
                    //PHP4.2以前は、必要らしいのでsrandを使用する。
                    srand((double)microtime()*1000000);
                    shuffle($kantenArray);
                    //観点データ追加
                    for ($i = 0; $i < get_count($viewcdArray); $i++) {
                        knje061Query::insData($db, $model, $schregNo, $viewcdArray[$i], $kantenArray[$i], $row, $year);
                    }
                }
            }
        }
        $result->free();
    }

    //学籍学習記録データ取得
    public function getSchregStudyrecDatSql($model, $schregNo, $year)
    {
        $query  = " SELECT ";
        $query .= "     T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.VALUATION ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$year."' ";
        $query .= "     AND T1.SCHREGNO = '".$schregNo."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD ";

        return $query;
    }

    //観点マスタ
    public function getJviewnameSubMst($db, $model, $row, $year)
    {
        $rtnArray = array();

        $query = knje061Query::getJviewnameSubMstSql($model, $row, $year);
        $result = $db->query($query);
        while ($rowJview = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $rtnArray[] = $rowJview["VIEWCD"];
        }
        $result->free();

        return $rtnArray;
    }

    //観点マスタ取得
    public function getJviewnameSubMstSql($model, $row, $year)
    {
        $query  = " SELECT ";
        $query .= "     T1.VIEWCD ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_SUB_YDAT T1, ";
        $query .= "     JVIEWNAME_SUB_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$year."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T1.CLASSCD = '".$row["CLASSCD"]."' ";
            $query .= "     AND T1.SCHOOL_KIND = '".$row["SCHOOL_KIND"]."' ";
            $query .= "     AND T1.CURRICULUM_CD = '".$row["CURRICULUM_CD"]."' ";
        }
        $query .= "     AND T1.SUBCLASSCD = '".$row["SUBCLASSCD"]."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T1.CLASSCD = T2.CLASSCD ";
            $query .= "     AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
            $query .= "     AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
        }
        $query .= "     AND T1.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= "     AND T1.VIEWCD = T2.VIEWCD ";
        $query .= " ORDER BY ";
        $query .= "     T1.VIEWCD ";

        return $query;
    }

    /***
    観点成績の扱いについてです。
    基本的に
    ５　⇒　すべてＡ
    ４　⇒　１つＢ
    ３　⇒　２つＢ
    ２　⇒　３つＢ
    １　⇒　４つＢ
    という形でお願いします。（Ｃはつけません）
    ***/
    public function getCntB($valuation)
    {
        $cntB = -1;

        switch ($valuation) {
            case 5:
                $cntB = 0;
                break 1;
            case 4:
                $cntB = 1;
                break 1;
            case 3:
                $cntB = 2;
                break 1;
            case 2:
                $cntB = 3;
                break 1;
            case 1:
                $cntB = 4;
                break 1;
            default:
                $cntB = -1;
                break 1;
        }

        return $cntB;
    }

    //観点データ追加
    public function insData($db, $model, $schregNo, $viewcd, $status, $row, $year)
    {
        $data = array();
        $data["YEAR"][TEXT]                 = $year;
        $data["SEMESTER"][TEXT]             = "9";
        $data["SCHREGNO"][TEXT]             = $schregNo;
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]          = $row["CLASSCD"];
            $data["SCHOOL_KIND"][TEXT]      = $row["SCHOOL_KIND"];
            $data["CURRICULUM_CD"][TEXT]    = $row["CURRICULUM_CD"];
        }
        $data["SUBCLASSCD"][TEXT]           = $row["SUBCLASSCD"];
        $data["VIEWCD"][TEXT]               = $viewcd;
        $data["STATUS"][TEXT]               = $status;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";

        $query = Query::insertSQL($data, "JVIEWSTAT_SUB_DAT");
        $db->query($query);
    }

    /************************************************************/
    /* ↓↓↓ 指導要録観点・学習記録データ(立命館小学) ↓↓↓ */
    /************************************************************/

    //追加
    public function insDataJviewRitsumeikanP($db, $schregNo, $model, $year, $grade)
    {
        $query  = " INSERT INTO JVIEWSTAT_SUB_DAT ";
        //学年別観点マスタ
        $query .= " WITH SUBCLASS_VIEW AS ( ";
        $query .= "     SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD, ";
            $query .= "     T2.SCHOOL_KIND, ";
            $query .= "     T2.CURRICULUM_CD, ";
        }
        $query .= "         T2.SUBCLASSCD, ";
        $query .= "         T2.VIEWCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.STUDYREC_CLASSCD, ";
            $query .= "     T2.STUDYREC_SCHOOL_KIND, ";
            $query .= "     T2.STUDYREC_CURRICULUM_CD, ";
        }
        $query .= "         T2.STUDYREC_SUBCLASSCD, ";
        $query .= "         T2.STUDYREC_VIEWCD ";
        $query .= "     FROM ";
        $query .= "         JVIEWNAME_GRADE_YDAT T1, ";
        $query .= "         JVIEWNAME_GRADE_MST T2 ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR = '".$year."' ";
        $query .= "         AND T1.GRADE = '".$grade."' ";
        $query .= "         AND T2.GRADE = T1.GRADE ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T2.CLASSCD = T1.CLASSCD ";
            $query .= "     AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "     AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "         AND T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "         AND T2.VIEWCD = T1.VIEWCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T2.STUDYREC_CLASSCD IS NOT NULL ";
            $query .= "     AND T2.STUDYREC_SCHOOL_KIND IS NOT NULL ";
            $query .= "     AND T2.STUDYREC_CURRICULUM_CD IS NOT NULL ";
        }
        $query .= "         AND T2.STUDYREC_SUBCLASSCD IS NOT NULL ";
        $query .= "         AND T2.STUDYREC_VIEWCD IS NOT NULL ";
        $query .= "     ) ";

        //通知票観点
        $query .= " , JVIEWSTAT AS ( ";
        $query .= "     SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.STUDYREC_CLASSCD, ";
            $query .= "     T2.STUDYREC_SCHOOL_KIND, ";
            $query .= "     T2.STUDYREC_CURRICULUM_CD, ";
        }
        $query .= "         T2.STUDYREC_SUBCLASSCD, ";
        $query .= "         T2.STUDYREC_VIEWCD, ";
        $query .= "         SUM(CAST(VALUE(L1.NAMESPARE2, '0') AS SMALLINT)) AS TOTAL ";
        $query .= "     FROM ";
        $query .= "         JVIEWSTAT_RECORD_DAT T1 ";
        $query .= "         INNER JOIN SUBCLASS_VIEW T2 ";
        $query .= "              ON T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "             AND T2.VIEWCD = T1.VIEWCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND T2.CLASSCD = T1.CLASSCD ";
            $query .= "         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "         LEFT JOIN NAME_MST L1 ";
        $query .= "              ON L1.NAMECD1 = 'D029' ";
        $query .= "             AND L1.ABBV1 = T1.STATUS ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR = '".$year."' ";
        $query .= "         AND T1.SEMESTER < '9' ";
        $query .= "         AND T1.SCHREGNO = '".$schregNo."' ";
        $query .= "         AND T1.STATUS IS NOT NULL ";
        $query .= "     GROUP BY ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.STUDYREC_CLASSCD, ";
            $query .= "     T2.STUDYREC_SCHOOL_KIND, ";
            $query .= "     T2.STUDYREC_CURRICULUM_CD, ";
        }
        $query .= "         T2.STUDYREC_SUBCLASSCD, ";
        $query .= "         T2.STUDYREC_VIEWCD ";
        $query .= "     ) ";

        //メイン
        $query .= " SELECT ";
        $query .= "     '".$year."' AS YEAR, ";
        $query .= "     '9' AS SEMESTER, ";
        $query .= "     '".$schregNo."' AS SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.STUDYREC_CLASSCD AS CLASSCD, ";
            $query .= "     T1.STUDYREC_SCHOOL_KIND AS SCHOOL_KIND, ";
            $query .= "     T1.STUDYREC_CURRICULUM_CD AS CURRICULUM_CD, ";
        }
        $query .= "     T1.STUDYREC_SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     T1.STUDYREC_VIEWCD AS VIEWCD, ";
        $query .= "     L1.NAME1 AS STATUS, ";
        $query .= "     '".STAFFCD."' AS REGISTERCD, ";
        $query .= "     SYSDATE() AS UPDATED ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ";
        $query .= "          ON L1.NAMECD1 = 'D113' ";
        $query .= "         AND L1.NAMECD2 = T1.TOTAL ";

        $db->query($query);
    }

    //削除
    public function delDataStudyRecRitsumeikanP($db, $schregNo, $model, $year)
    {
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '".$year."' ";
        $query .= "     AND T1.SCHREGNO = '".$schregNo."' ";

        $db->query($query);
    }

    //追加
    //指導要録観点データから学習記録データの評定を生成
    public function insDataStudyRecRitsumeikanP($db, $schregNo, $model, $year, $grade)
    {
        $query  = " INSERT INTO SCHREG_STUDYREC_DAT( ";
        $query .= "     SCHOOLCD, ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     ANNUAL, ";
        $query .= "     CLASSCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "     SUBCLASSCD, ";
        $query .= "     VALUATION, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";

        //通知票観点
        $query .= " WITH JVIEWSTAT AS ( ";
        $query .= "     SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         SUM(CAST(VALUE(L1.NAMESPARE2, '0') AS SMALLINT)) AS TOTAL ";
        $query .= "     FROM ";
        $query .= "         JVIEWSTAT_SUB_DAT T1 ";
        $query .= "         LEFT JOIN NAME_MST L1 ";
        $query .= "              ON L1.NAMECD1 = 'D028' ";
        $query .= "             AND L1.ABBV1 = T1.STATUS ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR = '".$year."' ";
        $query .= "         AND T1.SEMESTER = '9' ";
        $query .= "         AND T1.SCHREGNO = '".$schregNo."' ";
        $query .= "         AND T1.STATUS IS NOT NULL ";
        $query .= "     GROUP BY ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD ";
        $query .= "     ) ";

        //メイン
        $query .= " SELECT ";
        $query .= "     '0' AS SCHOOLCD, ";
        $query .= "     '".$year."' AS YEAR, ";
        $query .= "     '".$schregNo."' AS SCHREGNO, ";
        $query .= "     '".$grade."' AS ANNUAL, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        } else {
            $query .= "     substr(T1.SUBCLASSCD,1,2) AS CLASSCD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     smallint(L1.NAME1) AS VALUATION, ";
        $query .= "     '".STAFFCD."' AS REGISTERCD, ";
        $query .= "     SYSDATE() AS UPDATED ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ";
        $query .= "          ON L1.NAMECD1 = 'D114' ";
        $query .= "         AND L1.NAMECD2 = T1.TOTAL ";

        $db->query($query);
    }

    /************************************************************/
    /* ↓↓↓ 指導要録観点・学習記録データ(駿台甲府小学) ↓↓↓ */
    /************************************************************/

    //追加
    public function insDataJviewSundaiP($db, $schregNo, $model, $year, $grade)
    {
        $query  = " INSERT INTO JVIEWSTAT_SUB_DAT ";
        //学年別観点マスタ
        $query .= " WITH SUBCLASS_VIEW AS ( ";
        $query .= "     SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD, ";
            $query .= "     T2.SCHOOL_KIND, ";
            $query .= "     T2.CURRICULUM_CD, ";
        }
        $query .= "         T2.SUBCLASSCD, ";
        $query .= "         T2.VIEWCD, ";
        //教育課程対応・・・TODO:スクリプト修正要
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.STUDYREC_CLASSCD, ";
            $query .= "     T2.STUDYREC_SCHOOL_KIND, ";
            $query .= "     T2.STUDYREC_CURRICULUM_CD, ";
        }
        $query .= "         T2.STUDYREC_SUBCLASSCD, ";
        $query .= "         T2.STUDYREC_VIEWCD ";
        $query .= "     FROM ";
        $query .= "         JVIEWNAME_GRADE_YDAT T1, ";
        $query .= "         JVIEWNAME_GRADE_MST T2 ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR = '".$year."' ";
        $query .= "         AND T1.GRADE = '".$grade."' ";
        $query .= "         AND T2.GRADE = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T2.CLASSCD = T1.CLASSCD ";
            $query .= "     AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "     AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "         AND T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "         AND T2.VIEWCD = T1.VIEWCD ";
        //教育課程対応・・・TODO:スクリプト修正要
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T2.STUDYREC_CLASSCD IS NOT NULL ";
            $query .= "     AND T2.STUDYREC_SCHOOL_KIND IS NOT NULL ";
            $query .= "     AND T2.STUDYREC_CURRICULUM_CD IS NOT NULL ";
        }
        $query .= "         AND T2.STUDYREC_SUBCLASSCD IS NOT NULL ";
        $query .= "         AND T2.STUDYREC_VIEWCD IS NOT NULL ";
        $query .= "     ) ";

        //JVIEWSTAT_INPUTSEQ_DATがある科目
        $query .= " , SUBCLASS_INPUTSEQ AS ( ";
        $query .= "     SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD, ";
            $query .= "     T2.SCHOOL_KIND, ";
            $query .= "     T2.CURRICULUM_CD, ";
        }
        $query .= "         T2.SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "         JVIEWSTAT_INPUTSEQ_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "             T2.YEAR = '".$year."' ";
        $query .= "         AND T2.GRADE = '".$grade."' ";
        $query .= "         AND T2.VIEWFLG = '1' ";
        $query .= "     GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD, ";
            $query .= "     T2.SCHOOL_KIND, ";
            $query .= "     T2.CURRICULUM_CD, ";
        }
        $query .= "         T2.SUBCLASSCD ";
        $query .= "     ) ";
        //学年別観点マスタ（JVIEWSTAT_INPUTSEQ_DATがある科目）
        $query .= " , SUBCLASS_VIEW_YES AS ( ";
        $query .= "     SELECT ";
        $query .= "         T2.* ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_VIEW T2 ";
        $query .= "     WHERE ";
        $query .= "         EXISTS ( ";
        $query .= "             SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 SUBCLASS_INPUTSEQ W1 ";
        $query .= "             WHERE ";
        $query .= "                     W1.SUBCLASSCD = T2.SUBCLASSCD ";
        //教育課程対応・・・TODO:スクリプト修正要
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             AND W1.CLASSCD = T2.CLASSCD ";
            $query .= "             AND W1.SCHOOL_KIND = T2.SCHOOL_KIND ";
            $query .= "             AND W1.CURRICULUM_CD = T2.CURRICULUM_CD ";
        }
        $query .= "             ) ";
        $query .= "     ) ";
        //学年別観点マスタ（JVIEWSTAT_INPUTSEQ_DATがない科目）
        $query .= " , SUBCLASS_VIEW_NO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T2.* ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_VIEW T2 ";
        $query .= "     WHERE ";
        $query .= "         NOT EXISTS ( ";
        $query .= "             SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 SUBCLASS_INPUTSEQ W1 ";
        $query .= "             WHERE ";
        $query .= "                     W1.SUBCLASSCD = T2.SUBCLASSCD ";
        //教育課程対応・・・TODO:スクリプト修正要
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             AND W1.CLASSCD = T2.CLASSCD ";
            $query .= "             AND W1.SCHOOL_KIND = T2.SCHOOL_KIND ";
            $query .= "             AND W1.CURRICULUM_CD = T2.CURRICULUM_CD ";
        }
        $query .= "             ) ";
        $query .= "     ) ";

        //成績観点2(JVIEWSTAT_INPUTSEQ_DATがある科目)
        $query .= " , JVIEWSTAT_YES AS ( ";
        $query .= "     SELECT ";
        //教育課程対応・・・TODO:スクリプト修正要
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.STUDYREC_CLASSCD, ";
            $query .= "     T2.STUDYREC_SCHOOL_KIND, ";
            $query .= "     T2.STUDYREC_CURRICULUM_CD, ";
        }
        $query .= "         T2.STUDYREC_SUBCLASSCD, ";
        $query .= "         T2.STUDYREC_VIEWCD, ";
        // 観点の計算方法 3:設定(合計で段階値を取得)
        $query .= "         SUM(CAST(VALUE(L1.NAMESPARE2, '0') AS SMALLINT)) AS TOTAL ";
        $query .= "     FROM ";
        $query .= "         JVIEWSTAT_RECORD_DAT T1 ";
        $query .= "         INNER JOIN SUBCLASS_VIEW_YES T2 ";
        $query .= "              ON T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "             AND T2.VIEWCD = T1.VIEWCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND T2.CLASSCD = T1.CLASSCD ";
            $query .= "         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "         INNER JOIN JVIEWSTAT_INPUTSEQ_DAT I1 ";
        $query .= "              ON I1.YEAR = T1.YEAR ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND I1.CLASSCD = T1.CLASSCD ";
            $query .= "         AND I1.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "         AND I1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "             AND I1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "             AND I1.VIEWCD = T1.VIEWCD ";
        $query .= "             AND I1.GRADE = '".$grade."' ";
        $query .= "             AND I1.SEMESTER = T1.SEMESTER ";
        $query .= "             AND I1.VIEWFLG = '1' ";
        $query .= "         LEFT JOIN NAME_MST L1 ";
        $query .= "              ON L1.NAMECD1 = 'D029' ";
        $query .= "             AND L1.ABBV1 = T1.STATUS ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR = '".$year."' ";
        $query .= "         AND T1.SEMESTER < '9' ";
        $query .= "         AND T1.SCHREGNO = '".$schregNo."' ";
        $query .= "     GROUP BY ";
        //教育課程対応・・・TODO:スクリプト修正要
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.STUDYREC_CLASSCD, ";
            $query .= "     T2.STUDYREC_SCHOOL_KIND, ";
            $query .= "     T2.STUDYREC_CURRICULUM_CD, ";
        }
        $query .= "         T2.STUDYREC_SUBCLASSCD, ";
        $query .= "         T2.STUDYREC_VIEWCD ";
        $query .= "     ) ";

        //成績観点1(JVIEWSTAT_INPUTSEQ_DATがない科目)
        $query .= " , JVIEWSTAT_NO AS ( ";
        $query .= "     SELECT ";
        //教育課程対応・・・TODO:スクリプト修正要
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.STUDYREC_CLASSCD, ";
            $query .= "     T2.STUDYREC_SCHOOL_KIND, ";
            $query .= "     T2.STUDYREC_CURRICULUM_CD, ";
        }
        $query .= "         T2.STUDYREC_SUBCLASSCD, ";
        $query .= "         T2.STUDYREC_VIEWCD, ";
        // 観点の計算方法 1:平均、2:最高
        if ($model->keisanDiv == "2") {
            $query .= "     rtrim(char(max(smallint(L1.NAMESPARE2)))) AS STATUS_SUUJI ";
        } else {
            $query .= "     rtrim(char(smallint(round(avg(float(smallint(L1.NAMESPARE2))),0)))) AS STATUS_SUUJI ";
        }
        $query .= "     FROM ";
        $query .= "         JVIEWSTAT_RECORD_DAT T1 ";
        $query .= "         INNER JOIN SUBCLASS_VIEW_NO T2 ";
        $query .= "              ON T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "             AND T2.VIEWCD = T1.VIEWCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND T2.CLASSCD = T1.CLASSCD ";
            $query .= "         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "         LEFT JOIN NAME_MST L1 ";
        $query .= "              ON L1.NAMECD1 = 'D029' ";
        $query .= "             AND L1.ABBV1 = T1.STATUS ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR = '".$year."' ";
        $query .= "         AND T1.SEMESTER < '9' ";
        $query .= "         AND T1.SCHREGNO = '".$schregNo."' ";
        $query .= "     GROUP BY ";
        //教育課程対応・・・TODO:スクリプト修正要
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.STUDYREC_CLASSCD, ";
            $query .= "     T2.STUDYREC_SCHOOL_KIND, ";
            $query .= "     T2.STUDYREC_CURRICULUM_CD, ";
        }
        $query .= "         T2.STUDYREC_SUBCLASSCD, ";
        $query .= "         T2.STUDYREC_VIEWCD ";
        $query .= "     ) ";

        //メイン2
        $query .= " SELECT ";
        $query .= "     '".$year."' AS YEAR, ";
        $query .= "     '9' AS SEMESTER, ";
        $query .= "     '".$schregNo."' AS SCHREGNO, ";
        //教育課程対応・・・TODO:スクリプト修正要
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.STUDYREC_CLASSCD AS CLASSCD, ";
            $query .= "     T1.STUDYREC_SCHOOL_KIND AS SCHOOL_KIND, ";
            $query .= "     T1.STUDYREC_CURRICULUM_CD AS CURRICULUM_CD, ";
        }
        $query .= "     T1.STUDYREC_SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     T1.STUDYREC_VIEWCD AS VIEWCD, ";
        $query .= "     L1.ABBV1 AS STATUS, ";
        $query .= "     '".STAFFCD."' AS REGISTERCD, ";
        $query .= "     SYSDATE() AS UPDATED ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_YES T1 ";
        $query .= "     INNER JOIN JVIEWSTAT_LEVEL_MST T2 ";
        $query .= "          ON T2.YEAR = '".$year."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T2.CLASSCD = T1.STUDYREC_CLASSCD ";
            $query .= "     AND T2.SCHOOL_KIND = T1.STUDYREC_SCHOOL_KIND ";
            $query .= "     AND T2.CURRICULUM_CD = T1.STUDYREC_CURRICULUM_CD ";
        }
        $query .= "         AND T2.SUBCLASSCD = T1.STUDYREC_SUBCLASSCD ";
        $query .= "         AND T2.VIEWCD = T1.STUDYREC_VIEWCD ";
        $query .= "         AND T2.DIV = '3' ";
        $query .= "         AND T2.GRADE = '".$grade."' ";
        $query .= "         AND T1.TOTAL BETWEEN T2.ASSESSLOW AND T2.ASSESSHIGH ";
        $query .= "     LEFT JOIN NAME_MST L1 ";
        $query .= "          ON L1.NAMECD1 = 'D028' ";
        $query .= "         AND T2.ASSESSLEVEL = CAST(VALUE(L1.NAMESPARE2, '0') AS SMALLINT) ";
        //メイン1
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '".$year."' AS YEAR, ";
        $query .= "     '9' AS SEMESTER, ";
        $query .= "     '".$schregNo."' AS SCHREGNO, ";
        //教育課程対応・・・TODO:スクリプト修正要
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.STUDYREC_CLASSCD AS CLASSCD, ";
            $query .= "     T1.STUDYREC_SCHOOL_KIND AS SCHOOL_KIND, ";
            $query .= "     T1.STUDYREC_CURRICULUM_CD AS CURRICULUM_CD, ";
        }
        $query .= "     T1.STUDYREC_SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     T1.STUDYREC_VIEWCD AS VIEWCD, ";
        $query .= "     L1.ABBV1 AS STATUS, ";
        $query .= "     '".STAFFCD."' AS REGISTERCD, ";
        $query .= "     SYSDATE() AS UPDATED ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_NO T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ";
        $query .= "          ON L1.NAMECD1 = 'D028' ";
        $query .= "         AND L1.NAMESPARE2 = T1.STATUS_SUUJI ";

        //echo $query ."<BR>";
        $db->query($query);
    }

    //削除
    public function delDataStudyRecSundaiP($db, $schregNo, $model, $year)
    {
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '".$year."' ";
        $query .= "     AND T1.SCHREGNO = '".$schregNo."' ";

        $db->query($query);
    }

    //追加
    //指導要録観点データから学習記録データの評定を生成
    public function insDataStudyRecSundaiP($db, $schregNo, $model, $year, $grade)
    {
        $query  = " INSERT INTO SCHREG_STUDYREC_DAT( ";
        $query .= "     SCHOOLCD, ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     ANNUAL, ";
        $query .= "     CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "     SUBCLASSCD, ";
        $query .= "     VALUATION, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     '0' AS SCHOOLCD, ";
        $query .= "     '".$year."' AS YEAR, ";
        $query .= "     '".$schregNo."' AS SCHREGNO, ";
        $query .= "     '".$grade."' AS ANNUAL, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        } else {
            $query .= "     substr(T1.SUBCLASSCD,1,2) AS CLASSCD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     smallint(round(sum(float(smallint(value(L1.NAMESPARE2,'0')))*value(T2.WEIGHT,1))/sum(value(T2.WEIGHT,1)),0)) AS VALUATION, ";
        $query .= "     '".STAFFCD."' AS REGISTERCD, ";
        $query .= "     SYSDATE() AS UPDATED ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_SUB_DAT T1 ";
        $query .= "     LEFT JOIN JVIEWNAME_SUB_MST T2 ";
        $query .= "          ON T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "         AND T2.VIEWCD = T1.VIEWCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND T2.CLASSCD = T1.CLASSCD ";
            $query .= "         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "     LEFT JOIN NAME_MST L1 ";
        $query .= "          ON L1.NAMECD1 = 'D028' ";
        $query .= "         AND L1.ABBV1 = T1.STATUS ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '".$year."' ";
        $query .= "     AND T1.SEMESTER = '9' ";
        $query .= "     AND T1.SCHREGNO = '".$schregNo."' ";
        $query .= "     AND T1.STATUS IS NOT NULL ";
        $query .= " GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        } else {
            $query .= "     substr(T1.SUBCLASSCD,1,2), ";
        }
        $query .= "     T1.SUBCLASSCD ";

        $db->query($query);
    }

    /******************************************/
    /* ↓↓↓ 学習記録データ(武蔵野東) ↓↓↓ */
    /******************************************/

    //追加
    public function insDataJviewMusasinoHigasi($db, $schregNo, $model, $year, $grade)
    {
        $query  = " INSERT INTO JVIEWSTAT_SUB_DAT ";
        //学年別観点マスタ
        $query .= " WITH SUBCLASS_VIEW AS ( ";
        $query .= "     SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD, ";
            $query .= "     T2.SCHOOL_KIND, ";
            $query .= "     T2.CURRICULUM_CD, ";
        }
        $query .= "         T2.SUBCLASSCD, ";
        $query .= "         T2.VIEWCD, ";
        $query .= "         CASE WHEN T2.VIEW_SEQ = '001' THEN '1' ";
        $query .= "              WHEN T2.VIEW_SEQ = '002' THEN '2' ";
        $query .= "              WHEN T2.VIEW_SEQ = '003' THEN '3' ";
        $query .= "              END AS SEMESTER, ";
        //教育課程対応・・・TODO:スクリプト修正要
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.REMARK4 AS STUDYREC_CLASSCD, ";
            $query .= "     T2.REMARK5 AS STUDYREC_SCHOOL_KIND, ";
            $query .= "     T2.REMARK6 AS STUDYREC_CURRICULUM_CD, ";
        }
        $query .= "         T2.REMARK7 AS STUDYREC_SUBCLASSCD, ";
        $query .= "         T2.REMARK8 AS STUDYREC_VIEWCD ";
        $query .= "     FROM ";
        $query .= "         JVIEWNAME_GRADE_YDAT T1, ";
        $query .= "         JVIEWNAME_GRADE_DETAIL_MST T2 ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR = '".$year."' ";
        $query .= "         AND T1.GRADE = '".$grade."' ";
        $query .= "         AND T2.GRADE = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T2.CLASSCD = T1.CLASSCD ";
            $query .= "     AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "     AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "         AND T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "         AND T2.VIEWCD = T1.VIEWCD ";
        $query .= "         AND T2.VIEW_SEQ IN ('001','002','003') ";//1,2,3学期
        //教育課程対応・・・TODO:スクリプト修正要
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T2.REMARK4 IS NOT NULL ";
            $query .= "     AND T2.REMARK5 IS NOT NULL ";
            $query .= "     AND T2.REMARK6 IS NOT NULL ";
        }
        $query .= "         AND T2.REMARK7 IS NOT NULL ";
        $query .= "         AND T2.REMARK8 IS NOT NULL ";
        $query .= "     ) ";
        //成績観点
        $query .= " , JVIEWSTAT AS ( ";
        $query .= "     SELECT ";
        //教育課程対応・・・TODO:スクリプト修正要
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.STUDYREC_CLASSCD, ";
            $query .= "     T2.STUDYREC_SCHOOL_KIND, ";
            $query .= "     T2.STUDYREC_CURRICULUM_CD, ";
        }
        $query .= "         T2.STUDYREC_SUBCLASSCD, ";
        $query .= "         T2.STUDYREC_VIEWCD, ";
        // 観点の計算方法 1:平均、2:最高
        if ($model->keisanDiv == "2") {
            $query .= "     rtrim(char(max(smallint(L1.NAMESPARE2)))) AS STATUS_SUUJI ";
        } else {
            $query .= "     rtrim(char(smallint(round(avg(float(smallint(L1.NAMESPARE2))),0)))) AS STATUS_SUUJI ";
        }
        $query .= "     FROM ";
        $query .= "         JVIEWSTAT_RECORD_DAT T1 ";
        $query .= "         INNER JOIN SUBCLASS_VIEW T2 ";
        $query .= "              ON T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "             AND T2.VIEWCD = T1.VIEWCD ";
        $query .= "             AND T2.SEMESTER = T1.SEMESTER ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND T2.CLASSCD = T1.CLASSCD ";
            $query .= "         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "         LEFT JOIN NAME_MST L1 ";
        $query .= "              ON L1.NAMECD1 = 'D028' ";
        $query .= "             AND L1.ABBV1 = T1.STATUS ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR = '".$year."' ";
        $query .= "         AND T1.SEMESTER < '9' ";
        $query .= "         AND T1.SCHREGNO = '".$schregNo."' ";
        $query .= "     GROUP BY ";
        //教育課程対応・・・TODO:スクリプト修正要
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.STUDYREC_CLASSCD, ";
            $query .= "     T2.STUDYREC_SCHOOL_KIND, ";
            $query .= "     T2.STUDYREC_CURRICULUM_CD, ";
        }
        $query .= "         T2.STUDYREC_SUBCLASSCD, ";
        $query .= "         T2.STUDYREC_VIEWCD ";
        $query .= "     ) ";

        //メイン
        $query .= " SELECT ";
        $query .= "     '".$year."' AS YEAR, ";
        $query .= "     '9' AS SEMESTER, ";
        $query .= "     '".$schregNo."' AS SCHREGNO, ";
        //教育課程対応・・・TODO:スクリプト修正要
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.STUDYREC_CLASSCD AS CLASSCD, ";
            $query .= "     T1.STUDYREC_SCHOOL_KIND AS SCHOOL_KIND, ";
            $query .= "     T1.STUDYREC_CURRICULUM_CD AS CURRICULUM_CD, ";
        }
        $query .= "     T1.STUDYREC_SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     T1.STUDYREC_VIEWCD AS VIEWCD, ";
        $query .= "     L1.ABBV1 AS STATUS, ";
        $query .= "     '".STAFFCD."' AS REGISTERCD, ";
        $query .= "     SYSDATE() AS UPDATED ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ";
        $query .= "          ON L1.NAMECD1 = 'D028' ";
        $query .= "         AND L1.NAMESPARE2 = T1.STATUS_SUUJI ";

        $db->query($query);
    }

    //削除
    public function delDataStudyRecMusasinoHigasi($db, $schregNo, $model, $year)
    {
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '".$year."' ";
        $query .= "     AND T1.SCHREGNO = '".$schregNo."' ";

        $db->query($query);
    }

    //追加
    //指導要録観点データから学習記録データの評定を生成
    public function insDataStudyRecMusasinoHigasi($db, $schregNo, $model, $year, $grade)
    {
        $query  = " INSERT INTO SCHREG_STUDYREC_DAT( ";
        $query .= "     SCHOOLCD, ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     ANNUAL, ";
        $query .= "     CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "     SUBCLASSCD, ";
        $query .= "     VALUATION, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     '0' AS SCHOOLCD, ";
        $query .= "     '".$year."' AS YEAR, ";
        $query .= "     '".$schregNo."' AS SCHREGNO, ";
        $query .= "     '".$grade."' AS ANNUAL, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        } else {
            $query .= "     substr(T1.SUBCLASSCD,1,2) AS CLASSCD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     smallint(round(sum(float(smallint(value(L1.NAMESPARE2,'0')))*value(T2.WEIGHT,1))/sum(value(T2.WEIGHT,1)),0)) AS VALUATION, ";
        $query .= "     '".STAFFCD."' AS REGISTERCD, ";
        $query .= "     SYSDATE() AS UPDATED ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_SUB_DAT T1 ";
        $query .= "     LEFT JOIN JVIEWNAME_SUB_MST T2 ";
        $query .= "          ON T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "         AND T2.VIEWCD = T1.VIEWCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND T2.CLASSCD = T1.CLASSCD ";
            $query .= "         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "     LEFT JOIN NAME_MST L1 ";
        $query .= "          ON L1.NAMECD1 = 'D028' ";
        $query .= "         AND L1.ABBV1 = T1.STATUS ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '".$year."' ";
        $query .= "     AND T1.SEMESTER = '9' ";
        $query .= "     AND T1.SCHREGNO = '".$schregNo."' ";
        $query .= "     AND T1.STATUS IS NOT NULL ";
        $query .= " GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        } else {
            $query .= "     substr(T1.SUBCLASSCD,1,2), ";
        }
        $query .= "     T1.SUBCLASSCD ";

        $db->query($query);
    }

    /******************************************/
    /* ↓↓↓ 学習記録データ(大阪信愛) ↓↓↓ */
    /******************************************/
    //追加
    public function insDataJviewOsakashinnai($db, $schregNo, $model, $year, $grade)
    {
        $query  = " INSERT INTO JVIEWSTAT_SUB_DAT ";
        //学年別観点マスタ
        $query .= " WITH SUBCLASS_VIEW AS ( ";
        $query .= "     SELECT ";
        $query .= "         T2.CLASSCD, ";
        $query .= "         T2.SCHOOL_KIND, ";
        $query .= "         T2.CURRICULUM_CD, ";
        $query .= "         T2.SUBCLASSCD, ";
        $query .= "         T2.VIEWCD, ";
        $query .= "         T2.STUDYREC_CLASSCD, ";
        $query .= "         T2.STUDYREC_SCHOOL_KIND, ";
        $query .= "         T2.STUDYREC_CURRICULUM_CD, ";
        $query .= "         T2.STUDYREC_SUBCLASSCD, ";
        $query .= "         T2.STUDYREC_VIEWCD ";
        $query .= "     FROM ";
        $query .= "         JVIEWNAME_GRADE_YDAT T1, ";
        $query .= "         JVIEWNAME_GRADE_MST T2 ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR = '".$year."' ";
        $query .= "         AND T1.GRADE = '".$grade."' ";
        $query .= "         AND T2.GRADE = T1.GRADE ";
        $query .= "         AND T2.CLASSCD = T1.CLASSCD ";
        $query .= "         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "         AND T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "         AND T2.VIEWCD = T1.VIEWCD ";
        $query .= "         AND T2.STUDYREC_CLASSCD IS NOT NULL ";
        $query .= "         AND T2.STUDYREC_SCHOOL_KIND IS NOT NULL ";
        $query .= "         AND T2.STUDYREC_CURRICULUM_CD IS NOT NULL ";
        $query .= "         AND T2.STUDYREC_SUBCLASSCD IS NOT NULL ";
        $query .= "         AND T2.STUDYREC_VIEWCD IS NOT NULL ";
        $query .= "     ) ";
        //成績観点
        $query .= " , JVIEWSTAT AS ( ";
        $query .= "     SELECT ";
        $query .= "         T2.STUDYREC_CLASSCD, ";
        $query .= "         T2.STUDYREC_SCHOOL_KIND, ";
        $query .= "         T2.STUDYREC_CURRICULUM_CD, ";
        $query .= "         T2.STUDYREC_SUBCLASSCD, ";
        $query .= "         T2.STUDYREC_VIEWCD, ";
        $query .= "         COUNT(CASE WHEN T1.STATUS IN ('A', 'B', 'C') THEN 1 END) AS COUNT_ALL, ";
        $query .= "         COUNT(CASE WHEN T1.STATUS = 'A' THEN 1 END) AS COUNT_A, ";
        $query .= "         COUNT(CASE WHEN T1.STATUS = 'B' THEN 1 END) AS COUNT_B, ";
        $query .= "         COUNT(CASE WHEN T1.STATUS = 'C' THEN 1 END) AS COUNT_C  ";
        $query .= "     FROM ";
        $query .= "         JVIEWSTAT_RECORD_DAT T1 ";
        $query .= "         INNER JOIN SUBCLASS_VIEW T2 ";
        $query .= "              ON T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "             AND T2.VIEWCD = T1.VIEWCD ";
        $query .= "             AND T2.CLASSCD = T1.CLASSCD ";
        $query .= "             AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "             AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR = '".$year."' ";
        $query .= "         AND T1.SEMESTER < '9' ";
        $query .= "         AND T1.SCHREGNO = '".$schregNo."' ";
        $query .= "         AND T1.STATUS IN ('A', 'B', 'C') ";
        $query .= "     GROUP BY ";
        $query .= "         T2.STUDYREC_CLASSCD, ";
        $query .= "         T2.STUDYREC_SCHOOL_KIND, ";
        $query .= "         T2.STUDYREC_CURRICULUM_CD, ";
        $query .= "         T2.STUDYREC_SUBCLASSCD, ";
        $query .= "         T2.STUDYREC_VIEWCD ";
        $query .= "     ) ";

        //メイン
        $query .= " SELECT ";
        $query .= "     '".$year."' AS YEAR, ";
        $query .= "     '9' AS SEMESTER, ";
        $query .= "     '".$schregNo."' AS SCHREGNO, ";
        $query .= "     T1.STUDYREC_CLASSCD AS CLASSCD, ";
        $query .= "     T1.STUDYREC_SCHOOL_KIND AS SCHOOL_KIND, ";
        $query .= "     T1.STUDYREC_CURRICULUM_CD AS CURRICULUM_CD, ";
        $query .= "     T1.STUDYREC_SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     T1.STUDYREC_VIEWCD AS VIEWCD, ";
        $query .= "     CASE  ";
        $query .= "        WHEN T1.COUNT_A >= T1.COUNT_ALL * 2 / 3.0 THEN 'A' ";
        $query .= "        WHEN T1.COUNT_C >= T1.COUNT_ALL * 2 / 3.0 THEN 'C' ";
        $query .= "        ELSE  'B' ";
        $query .= "     END AS STATUS, ";
        $query .= "     '".STAFFCD."' AS REGISTERCD, ";
        $query .= "     SYSDATE() AS UPDATED ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT T1 ";

        $db->query($query);
    }

    /****************************/
    /* ↓↓↓ 観点データ ↓↓↓ */
    /****************************/

    //生徒取得
    public function getStudent($model)
    {
        // 生成範囲の条件を分ける
        $operator = ($model->field["RANGE"] == 1) ? "=" : "<=";

        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     L1.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     ".$model->vSchregRegdDat." T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT L1 ON L1.YEAR = T1.YEAR AND L1.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR ".$operator." '".CTRL_YEAR."' ";

        //対象生徒
        $query .= "   AND EXISTS (SELECT 'X' FROM ".$model->vSchregRegdDat." W1 ";
        $query .= "                WHERE W1.YEAR                        = '".CTRL_YEAR."' ";
        $query .= "                  AND W1.GRADE                       = '" .$model->annual ."' ";
        if (strlen($model->hr_class)) {
            $query .= " AND W1.HR_CLASS     = '" .$model->hr_class ."' ";
        }
        if (strlen($model->coursecode)) {
            $query .= " AND W1.COURSECODE   = '" .$model->coursecode ."' ";
        }
        if (strlen($model->schregno)) {
            $query .= " AND W1.SCHREGNO     = '" .$model->schregno ."' ";
        }
        $query .= "                  AND W1.SCHREGNO                    = T1.SCHREGNO) ";

        //署名チェックに該当したデータを対象から除く。
        if ($model->Properties["useSeitoSidoYorokuShomeiKinou"] == 1) {
            $query .= "    AND NOT EXISTS (SELECT 'X' FROM ATTEST_OPINIONS_WK WK ";
            $query .= "                     WHERE WK.YEAR = T1.YEAR ";
            $query .= "                       AND WK.SCHREGNO = T1.SCHREGNO ";
            $query .= "                       AND WK.CHAGE_OPI_SEQ IS NOT NULL) ";
        }

        return $query;
    }

    //削除
    public function delDataJview($db, $schregNo, $model, $year)
    {
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_SUB_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '".$year."' ";
        $query .= "     AND T1.SEMESTER = '9' ";
        $query .= "     AND T1.SCHREGNO = '".$schregNo."' ";

        $db->query($query);
    }

    //追加
    public function insDataJview($db, $schregNo, $model, $year, $grade)
    {
        $query  = " INSERT INTO JVIEWSTAT_SUB_DAT ";
        //学年別観点マスタ
        $query .= " WITH SUBCLASS_VIEW AS ( ";
        $query .= "     SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD, ";
            $query .= "     T2.SCHOOL_KIND, ";
            $query .= "     T2.CURRICULUM_CD, ";
        }
        $query .= "         T2.SUBCLASSCD, ";
        $query .= "         T2.VIEWCD, ";
        //教育課程対応・・・TODO:スクリプト修正要
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.STUDYREC_CLASSCD, ";
            $query .= "     T2.STUDYREC_SCHOOL_KIND, ";
            $query .= "     T2.STUDYREC_CURRICULUM_CD, ";
        }
        $query .= "         T2.STUDYREC_SUBCLASSCD, ";
        $query .= "         T2.STUDYREC_VIEWCD ";
        $query .= "     FROM ";
        $query .= "         JVIEWNAME_GRADE_YDAT T1, ";
        $query .= "         JVIEWNAME_GRADE_MST T2 ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR = '".$year."' ";
        $query .= "         AND T1.GRADE = '".$grade."' ";
        $query .= "         AND T2.GRADE = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T2.CLASSCD = T1.CLASSCD ";
            $query .= "     AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "     AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "         AND T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "         AND T2.VIEWCD = T1.VIEWCD ";
        //教育課程対応・・・TODO:スクリプト修正要
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T2.STUDYREC_CLASSCD IS NOT NULL ";
            $query .= "     AND T2.STUDYREC_SCHOOL_KIND IS NOT NULL ";
            $query .= "     AND T2.STUDYREC_CURRICULUM_CD IS NOT NULL ";
        }
        $query .= "         AND T2.STUDYREC_SUBCLASSCD IS NOT NULL ";
        $query .= "         AND T2.STUDYREC_VIEWCD IS NOT NULL ";
        $query .= "     ) ";
        //成績観点
        $query .= " , JVIEWSTAT AS ( ";
        $query .= "     SELECT ";
        //教育課程対応・・・TODO:スクリプト修正要
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.STUDYREC_CLASSCD, ";
            $query .= "     T2.STUDYREC_SCHOOL_KIND, ";
            $query .= "     T2.STUDYREC_CURRICULUM_CD, ";
        }
        $query .= "         T2.STUDYREC_SUBCLASSCD, ";
        $query .= "         T2.STUDYREC_VIEWCD, ";
        // 観点の計算方法 1:平均、2:最高
        if ($model->keisanDiv == "2") {
            $query .= "     rtrim(char(max(smallint(L1.NAMESPARE2)))) AS STATUS_SUUJI ";
        } else {
            $query .= "     rtrim(char(smallint(round(avg(float(smallint(L1.NAMESPARE2))),0)))) AS STATUS_SUUJI ";
        }
        $query .= "     FROM ";
        $query .= "         JVIEWSTAT_RECORD_DAT T1 ";
        $query .= "         INNER JOIN SUBCLASS_VIEW T2 ";
        $query .= "              ON T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "             AND T2.VIEWCD = T1.VIEWCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND T2.CLASSCD = T1.CLASSCD ";
            $query .= "         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "         LEFT JOIN NAME_MST L1 ";
        $query .= "              ON L1.NAMECD1 = 'D028' ";
        $query .= "             AND L1.ABBV1 = T1.STATUS ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR = '".$year."' ";
        $query .= "         AND T1.SEMESTER = '9' ";
        $query .= "         AND T1.SCHREGNO = '".$schregNo."' ";
        $query .= "     GROUP BY ";
        //教育課程対応・・・TODO:スクリプト修正要
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.STUDYREC_CLASSCD, ";
            $query .= "     T2.STUDYREC_SCHOOL_KIND, ";
            $query .= "     T2.STUDYREC_CURRICULUM_CD, ";
        }
        $query .= "         T2.STUDYREC_SUBCLASSCD, ";
        $query .= "         T2.STUDYREC_VIEWCD ";
        $query .= "     ) ";

        //メイン
        $query .= " SELECT ";
        $query .= "     '".$year."' AS YEAR, ";
        $query .= "     '9' AS SEMESTER, ";
        $query .= "     '".$schregNo."' AS SCHREGNO, ";
        //教育課程対応・・・TODO:スクリプト修正要
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.STUDYREC_CLASSCD AS CLASSCD, ";
            $query .= "     T1.STUDYREC_SCHOOL_KIND AS SCHOOL_KIND, ";
            $query .= "     T1.STUDYREC_CURRICULUM_CD AS CURRICULUM_CD, ";
        }
        $query .= "     T1.STUDYREC_SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     T1.STUDYREC_VIEWCD AS VIEWCD, ";
        $query .= "     L1.ABBV1 AS STATUS, ";
        $query .= "     '".STAFFCD."' AS REGISTERCD, ";
        $query .= "     SYSDATE() AS UPDATED ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ";
        $query .= "          ON L1.NAMECD1 = 'D028' ";
        $query .= "         AND L1.NAMESPARE2 = T1.STATUS_SUUJI ";

        $db->query($query);
    }

    //追加(明治用)
    public function insDataJviewMeiji($db, $schregNo, $model, $year, $grade)
    {
        $query  = " INSERT INTO JVIEWSTAT_SUB_DAT ";
        //学年別観点マスタ
        $query .= " WITH SUBCLASS_VIEW AS ( ";
        $query .= "     SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD, ";
            $query .= "     T2.SCHOOL_KIND, ";
            $query .= "     T2.CURRICULUM_CD, ";
        }
        $query .= "         T2.SUBCLASSCD, ";
        $query .= "         T2.VIEWCD, ";
        //教育課程対応・・・TODO:スクリプト修正要
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.STUDYREC_CLASSCD, ";
            $query .= "     T2.STUDYREC_SCHOOL_KIND, ";
            $query .= "     T2.STUDYREC_CURRICULUM_CD, ";
        }
        $query .= "         T2.STUDYREC_SUBCLASSCD, ";
        $query .= "         T2.STUDYREC_VIEWCD ";
        $query .= "     FROM ";
        $query .= "         JVIEWNAME_GRADE_YDAT T1, ";
        $query .= "         JVIEWNAME_GRADE_MST T2 ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR = '".$year."' ";
        $query .= "         AND T1.GRADE = '".$grade."' ";
        $query .= "         AND T2.GRADE = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T2.CLASSCD = T1.CLASSCD ";
            $query .= "     AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "     AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "         AND T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "         AND T2.VIEWCD = T1.VIEWCD ";
        //教育課程対応・・・TODO:スクリプト修正要
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T2.STUDYREC_CLASSCD IS NOT NULL ";
            $query .= "     AND T2.STUDYREC_SCHOOL_KIND IS NOT NULL ";
            $query .= "     AND T2.STUDYREC_CURRICULUM_CD IS NOT NULL ";
        }
        $query .= "         AND T2.STUDYREC_SUBCLASSCD IS NOT NULL ";
        $query .= "         AND T2.STUDYREC_VIEWCD IS NOT NULL ";
        $query .= "     ) ";

        //学年別観点マスタ(観点が２件以上)
        $query .= " , SUBCLASS_VIEW_CNT AS ( ";
        $query .= "     SELECT ";
        //教育課程対応・・・TODO:スクリプト修正要
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.STUDYREC_CLASSCD, ";
            $query .= "     T2.STUDYREC_SCHOOL_KIND, ";
            $query .= "     T2.STUDYREC_CURRICULUM_CD, ";
        }
        $query .= "         T2.STUDYREC_SUBCLASSCD, ";
        $query .= "         T2.STUDYREC_VIEWCD, ";
        $query .= "         COUNT(*) AS CNT ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_VIEW T2 ";
        $query .= "     GROUP BY ";
        //教育課程対応・・・TODO:スクリプト修正要
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.STUDYREC_CLASSCD, ";
            $query .= "     T2.STUDYREC_SCHOOL_KIND, ";
            $query .= "     T2.STUDYREC_CURRICULUM_CD, ";
        }
        $query .= "         T2.STUDYREC_SUBCLASSCD, ";
        $query .= "         T2.STUDYREC_VIEWCD ";
        $query .= "     HAVING ";
        $query .= "         2 <= COUNT(*) ";
        $query .= "     ) ";
        $query .= " , SUBCLASS_VIEW_CNT2 AS ( ";
        $query .= "     SELECT ";
        $query .= "         T2.* ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_VIEW T2 ";
        $query .= "     WHERE ";
        $query .= "         EXISTS ( ";
        $query .= "             SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 SUBCLASS_VIEW_CNT W1 ";
        $query .= "             WHERE ";
        $query .= "                     W1.STUDYREC_SUBCLASSCD = T2.STUDYREC_SUBCLASSCD ";
        $query .= "                 AND W1.STUDYREC_VIEWCD = T2.STUDYREC_VIEWCD ";
        //教育課程対応・・・TODO:スクリプト修正要
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             AND W1.STUDYREC_CLASSCD = T2.STUDYREC_CLASSCD ";
            $query .= "             AND W1.STUDYREC_SCHOOL_KIND = T2.STUDYREC_SCHOOL_KIND ";
            $query .= "             AND W1.STUDYREC_CURRICULUM_CD = T2.STUDYREC_CURRICULUM_CD ";
        }
        $query .= "             ) ";
        $query .= "     ) ";
        //学年別観点マスタ(観点が１件)
        $query .= " , SUBCLASS_VIEW_CNT1 AS ( ";
        $query .= "     SELECT ";
        $query .= "         T2.* ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_VIEW T2 ";
        $query .= "     WHERE ";
        $query .= "         NOT EXISTS ( ";
        $query .= "             SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 SUBCLASS_VIEW_CNT W1 ";
        $query .= "             WHERE ";
        $query .= "                     W1.STUDYREC_SUBCLASSCD = T2.STUDYREC_SUBCLASSCD ";
        $query .= "                 AND W1.STUDYREC_VIEWCD = T2.STUDYREC_VIEWCD ";
        //教育課程対応・・・TODO:スクリプト修正要
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             AND W1.STUDYREC_CLASSCD = T2.STUDYREC_CLASSCD ";
            $query .= "             AND W1.STUDYREC_SCHOOL_KIND = T2.STUDYREC_SCHOOL_KIND ";
            $query .= "             AND W1.STUDYREC_CURRICULUM_CD = T2.STUDYREC_CURRICULUM_CD ";
        }
        $query .= "             ) ";
        $query .= "     ) ";

        //成績観点2(各学期の評定の合計)
        $query .= " , JVIEWSTAT2 AS ( ";
        $query .= "     SELECT ";
        //教育課程対応・・・TODO:スクリプト修正要
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.STUDYREC_CLASSCD, ";
            $query .= "     T2.STUDYREC_SCHOOL_KIND, ";
            $query .= "     T2.STUDYREC_CURRICULUM_CD, ";
        }
        $query .= "         T2.STUDYREC_SUBCLASSCD, ";
        $query .= "         T2.STUDYREC_VIEWCD, ";
        // 観点の計算方法 3:設定(合計で段階値を取得)
        $query .= "         SUM(CAST(VALUE(L1.NAMESPARE2, '0') AS SMALLINT)) AS TOTAL ";
        $query .= "     FROM ";
        $query .= "         JVIEWSTAT_RECORD_DAT T1 ";
        $query .= "         INNER JOIN SUBCLASS_VIEW_CNT2 T2 ";
        $query .= "              ON T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "             AND T2.VIEWCD = T1.VIEWCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND T2.CLASSCD = T1.CLASSCD ";
            $query .= "         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "         INNER JOIN JVIEWSTAT_INPUTSEQ_DAT I1 ";
        $query .= "              ON I1.YEAR = T1.YEAR ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND I1.CLASSCD = T1.CLASSCD ";
            $query .= "         AND I1.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "         AND I1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "             AND I1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "             AND I1.VIEWCD = T1.VIEWCD ";
        $query .= "             AND I1.GRADE = '".$grade."' ";
        $query .= "             AND I1.SEMESTER = T1.SEMESTER ";
        $query .= "             AND I1.VIEWFLG = '1' ";
        $query .= "         LEFT JOIN NAME_MST L1 ";
        $query .= "              ON L1.NAMECD1 = 'D029' ";
        $query .= "             AND L1.ABBV1 = T1.STATUS ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR = '".$year."' ";
        $query .= "         AND T1.SEMESTER < '9' ";
        $query .= "         AND T1.SCHREGNO = '".$schregNo."' ";
        $query .= "     GROUP BY ";
        //教育課程対応・・・TODO:スクリプト修正要
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.STUDYREC_CLASSCD, ";
            $query .= "     T2.STUDYREC_SCHOOL_KIND, ";
            $query .= "     T2.STUDYREC_CURRICULUM_CD, ";
        }
        $query .= "         T2.STUDYREC_SUBCLASSCD, ";
        $query .= "         T2.STUDYREC_VIEWCD ";
        $query .= "     ) ";

        //成績観点1(9学期の評定)
        $query .= " , JVIEWSTAT1 AS ( ";
        $query .= "     SELECT ";
        //教育課程対応・・・TODO:スクリプト修正要
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.STUDYREC_CLASSCD, ";
            $query .= "     T2.STUDYREC_SCHOOL_KIND, ";
            $query .= "     T2.STUDYREC_CURRICULUM_CD, ";
        }
        $query .= "         T2.STUDYREC_SUBCLASSCD, ";
        $query .= "         T2.STUDYREC_VIEWCD, ";
        // 観点の計算方法 1:平均、2:最高
        if ($model->keisanDiv == "2") {
            $query .= "     rtrim(char(max(smallint(L1.NAMESPARE2)))) AS STATUS_SUUJI ";
        } else {
            $query .= "     rtrim(char(smallint(round(avg(float(smallint(L1.NAMESPARE2))),0)))) AS STATUS_SUUJI ";
        }
        $query .= "     FROM ";
        $query .= "         JVIEWSTAT_RECORD_DAT T1 ";
        $query .= "         INNER JOIN SUBCLASS_VIEW_CNT1 T2 ";
        $query .= "              ON T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "             AND T2.VIEWCD = T1.VIEWCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND T2.CLASSCD = T1.CLASSCD ";
            $query .= "         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "         LEFT JOIN NAME_MST L1 ";
        $query .= "              ON L1.NAMECD1 = 'D028' ";
        $query .= "             AND L1.ABBV1 = T1.STATUS ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR = '".$year."' ";
        $query .= "         AND T1.SEMESTER = '9' ";
        $query .= "         AND T1.SCHREGNO = '".$schregNo."' ";
        $query .= "     GROUP BY ";
        //教育課程対応・・・TODO:スクリプト修正要
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.STUDYREC_CLASSCD, ";
            $query .= "     T2.STUDYREC_SCHOOL_KIND, ";
            $query .= "     T2.STUDYREC_CURRICULUM_CD, ";
        }
        $query .= "         T2.STUDYREC_SUBCLASSCD, ";
        $query .= "         T2.STUDYREC_VIEWCD ";
        $query .= "     ) ";

        //メイン2
        $query .= " SELECT ";
        $query .= "     '".$year."' AS YEAR, ";
        $query .= "     '9' AS SEMESTER, ";
        $query .= "     '".$schregNo."' AS SCHREGNO, ";
        //教育課程対応・・・TODO:スクリプト修正要
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.STUDYREC_CLASSCD AS CLASSCD, ";
            $query .= "     T1.STUDYREC_SCHOOL_KIND AS SCHOOL_KIND, ";
            $query .= "     T1.STUDYREC_CURRICULUM_CD AS CURRICULUM_CD, ";
        }
        $query .= "     T1.STUDYREC_SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     T1.STUDYREC_VIEWCD AS VIEWCD, ";
        $query .= "     L1.ABBV1 AS STATUS, ";
        $query .= "     '".STAFFCD."' AS REGISTERCD, ";
        $query .= "     SYSDATE() AS UPDATED ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT2 T1 ";
        $query .= "     INNER JOIN JVIEWSTAT_LEVEL_MST T2 ";
        $query .= "          ON T2.YEAR = '".$year."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T2.CLASSCD = T1.STUDYREC_CLASSCD ";
            $query .= "     AND T2.SCHOOL_KIND = T1.STUDYREC_SCHOOL_KIND ";
            $query .= "     AND T2.CURRICULUM_CD = T1.STUDYREC_CURRICULUM_CD ";
        }
        $query .= "         AND T2.SUBCLASSCD = T1.STUDYREC_SUBCLASSCD ";
        $query .= "         AND T2.VIEWCD = T1.STUDYREC_VIEWCD ";
        $query .= "         AND T2.DIV = '3' ";
        $query .= "         AND T2.GRADE = '".$grade."' ";
        $query .= "         AND T1.TOTAL BETWEEN T2.ASSESSLOW AND T2.ASSESSHIGH ";
        $query .= "     LEFT JOIN NAME_MST L1 ";
        $query .= "          ON L1.NAMECD1 = 'D028' ";
        $query .= "         AND T2.ASSESSLEVEL = CAST(VALUE(L1.NAMESPARE2, '0') AS SMALLINT) ";
        //メイン1
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '".$year."' AS YEAR, ";
        $query .= "     '9' AS SEMESTER, ";
        $query .= "     '".$schregNo."' AS SCHREGNO, ";
        //教育課程対応・・・TODO:スクリプト修正要
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.STUDYREC_CLASSCD AS CLASSCD, ";
            $query .= "     T1.STUDYREC_SCHOOL_KIND AS SCHOOL_KIND, ";
            $query .= "     T1.STUDYREC_CURRICULUM_CD AS CURRICULUM_CD, ";
        }
        $query .= "     T1.STUDYREC_SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     T1.STUDYREC_VIEWCD AS VIEWCD, ";
        $query .= "     L1.ABBV1 AS STATUS, ";
        $query .= "     '".STAFFCD."' AS REGISTERCD, ";
        $query .= "     SYSDATE() AS UPDATED ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT1 T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ";
        $query .= "          ON L1.NAMECD1 = 'D028' ";
        $query .= "         AND L1.NAMESPARE2 = T1.STATUS_SUUJI ";

        $db->query($query);
    }

    //観点データの削除(ＣＳＶデータ書込み前)
    public function delCsvDataJview($data, $model)
    {
        $query  = " DELETE FROM JVIEWSTAT_SUB_DAT ";
        $query .= "  WHERE YEAR         = '" .$data["YEAR"] ."'";
        $query .= "    AND SEMESTER     = '9' ";
        $query .= "    AND SCHREGNO     = '" .sprintf("%08d", $data["SCHREGNO"]) ."'";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    AND CLASSCD       = '" .sprintf("%02d", $data["CLASSCD"]) ."'";
            $query .= "    AND SCHOOL_KIND   = '" .$data["SCHOOL_KIND"] ."'";
            $query .= "    AND CURRICULUM_CD = '" .$data["CURRICULUM_CD"] ."'";
        }
        $query .= "    AND SUBCLASSCD   = '" .sprintf("%06d", $data["SUBCLASSCD"]) ."'";
        $query .= "    AND VIEWCD       = '" .sprintf("%04d", $data["VIEWCD"]) ."'";
        return $query;
    }

    //観点データの作成（ＣＳＶデータより読込）
    public function insCsvDataJview($data, $model)
    {
        $datas = array();
        $datas["YEAR"][TEXT]            = $data["YEAR"];
        $datas["SEMESTER"][TEXT]        = '9';
        $datas["SCHREGNO"][TEXT]        = sprintf("%08d", $data["SCHREGNO"]);
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $datas["CLASSCD"][TEXT]          = sprintf("%02d", $data["CLASSCD"]);
            $datas["SCHOOL_KIND"][TEXT]      = $data["SCHOOL_KIND"];
            $datas["CURRICULUM_CD"][TEXT]    = $data["CURRICULUM_CD"];
        }
        $datas["SUBCLASSCD"][TEXT]      = sprintf("%06d", $data["SUBCLASSCD"]);
        $datas["VIEWCD"][TEXT]          = sprintf("%04d", $data["VIEWCD"]);
        $datas["STATUS"][TEXT]          = $data["STATUS"];
        $datas["REGISTERCD"][TEXT]      = STAFFCD;
        $datas["UPDATED"][FUNC]         = "SYSDATE()";

        $query = Query::insertSQL($datas, "JVIEWSTAT_SUB_DAT");
        return $query;
    }

    //学校種別取得
    public function getSchoolKind()
    {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     ABBV1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        $query .= " ORDER BY ";
        $query .= "     NAME1 ASC ";

        return $query;
    }

    /*******************************************/
    /* ↓↓↓ SCHREG_STUDYREC_DAT(大宮) ↓↓↓ */
    /*******************************************/

    //元科目の成績データの学年評定から先科目の学籍学習記録データを生成
    public function oomiyaStudyRecSaki($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = "";
        //クエリー
        $query  = knje061Query::getStudyRecSakiSql($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $query = knje061Query::existsStudyRecSql($row, $model);
            if ($db->getOne($query) == 0) {
                $query = knje061Query::insertStudyRecSql($row, $model);
                $db->query($query);
            } else {
                $query = knje061Query::updateStudyRecSql($row, $model);
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    //元科目の成績データの学年評定から先科目の学籍学習記録データを生成
    public function getStudyRecSakiSql($model)
    {
        // 生成範囲の条件を分ける
        $operator = ($model->field["RANGE"] == 1) ? "=" : "<=";

        $query  = "";
        $query .= " WITH T_RECORD AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     R1.COMBINED_CLASSCD AS CLASSCD, ";
            $query .= "     R1.COMBINED_SCHOOL_KIND AS SCHOOL_KIND, ";
            $query .= "     R1.COMBINED_CURRICULUM_CD AS CURRICULUM_CD, ";
        } else {
            $query .= "     SUBSTR(R1.COMBINED_SUBCLASSCD,1,2) AS CLASSCD, ";
        }
        $query .= "     R1.COMBINED_SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     MAX(R1.CALCULATE_CREDIT_FLG) AS FLG, ";
        $gradValue = ($model->recordTableDiv == "1") ? "VALUE" : "GRAD_VALUE";
        $query .= "     ROUND(AVG(FLOAT(T1.{$gradValue})),0) AS VALUATION, ";
        $query .= "     SUM(T1.GET_CREDIT) AS GET_CREDIT, ";
        $query .= "     SUM(T1.ADD_CREDIT) AS ADD_CREDIT, ";
        $query .= "     SUM(T1.COMP_CREDIT) AS COMP_CREDIT ";
        $query .= " FROM ";
        if ($model->recordTableDiv == "1") {
            $query .= "     RECORD_SCORE_DAT T1 ";
        } else {
            $query .= "     RECORD_DAT T1 ";
        }
        $query .= "     INNER JOIN SUBCLASS_REPLACE_COMBINED_DAT R1 ";
        $query .= "             ON  R1.YEAR = T1.YEAR ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             AND R1.ATTEND_CLASSCD = T1.CLASSCD ";
            $query .= "             AND R1.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "             AND R1.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "             AND R1.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR ".$operator." '".CTRL_YEAR."' ";
        if ($model->recordTableDiv == "1") {
            $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '9990000' ";
        }
        //$query .= "     AND T1.SCHREGNO = '".$schregNo."' ";
        $query .= "     AND (T1.{$gradValue} IS NOT NULL OR T1.COMP_CREDIT IS NOT NULL OR T1.GET_CREDIT IS NOT NULL) ";
        $query .= " GROUP BY ";
        $query .= "     T1.YEAR, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     R1.COMBINED_CLASSCD, ";
            $query .= "     R1.COMBINED_SCHOOL_KIND, ";
            $query .= "     R1.COMBINED_CURRICULUM_CD, ";
        }
        $query .= "     R1.COMBINED_SUBCLASSCD, ";
        $query .= "     T1.SCHREGNO ";
        $query .= "     ) ";

        //メイン
        $query .= " SELECT ";
        $query .= "     '0' AS SCHOOLCD, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L1.ANNUAL, ";
        $query .= "     T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " T1.SCHOOL_KIND, ";
            $query .= " T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.VALUATION, ";
        $query .= "     CASE WHEN 1 < T1.VALUATION AND T1.FLG = '1' THEN L2.CREDITS ";
        $query .= "          WHEN 1 < T1.VALUATION AND T1.FLG = '2' THEN T1.GET_CREDIT ";
        $query .= "          WHEN 1 = T1.VALUATION THEN 0 ";
        $query .= "     END AS GET_CREDIT, ";
        $query .= "     CAST(NULL AS SMALLINT) AS ADD_CREDIT, ";
        $query .= "     CASE WHEN 0 < T1.VALUATION AND T1.FLG = '1' THEN L2.CREDITS ";
        $query .= "          WHEN 0 < T1.VALUATION AND T1.FLG = '2' THEN T1.COMP_CREDIT ";
        $query .= "     END AS COMP_CREDIT ";
        $query .= " FROM ";
        $query .= "     T_RECORD T1 ";
        $query .= "     INNER JOIN ".$model->vSchregRegdDat." L1 ";
        $query .= "         ON  L1.SCHREGNO     = T1.SCHREGNO ";
        $query .= "         AND L1.YEAR         = T1.YEAR ";
        $query .= "         AND L1.ANNUAL IS NOT NULL ";
        $query .= "     LEFT JOIN CREDIT_MST L2 ";
        $query .= "         ON  L2.YEAR         = L1.YEAR ";
        $query .= "         AND L2.COURSECD     = L1.COURSECD ";
        $query .= "         AND L2.MAJORCD      = L1.MAJORCD ";
        $query .= "         AND L2.GRADE        = L1.GRADE ";
        $query .= "         AND L2.COURSECODE   = L1.COURSECODE ";
        $query .= "         AND L2.CLASSCD      = T1.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND L2.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "     AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "         AND L2.SUBCLASSCD   = T1.SUBCLASSCD ";
        $query .= "         AND L2.CREDITS IS NOT NULL ";
        $query .= " WHERE EXISTS (SELECT 'X' FROM ".$model->vSchregRegdDat." W1 ";
        $query .= "                WHERE W1.YEAR = '".CTRL_YEAR."' ";
        $query .= "                  AND W1.GRADE = '" .$model->annual ."' ";
        if (strlen($model->hr_class)) {
            $query .= "                  AND W1.HR_CLASS = '" .$model->hr_class ."' ";
        }
        if (strlen($model->coursecode)) {
            $query .= "                  AND W1.COURSECODE = '" .$model->coursecode ."' ";
        }
        if (strlen($model->schregno)) {
            $query .= "                  AND W1.SCHREGNO = '" .$model->schregno ."' ";
        }
        $query .= "                  AND W1.SCHREGNO = T1.SCHREGNO) ";

        //署名チェックに該当したデータを対象から除く。
        if ($model->Properties["useSeitoSidoYorokuShomeiKinou"] == 1) {
            $query .= "    AND NOT EXISTS (SELECT 'X' FROM ATTEST_OPINIONS_WK WK ";
            $query .= "                     WHERE WK.YEAR = T1.YEAR ";
            $query .= "                       AND WK.SCHREGNO = T1.SCHREGNO ";
            $query .= "                       AND WK.CHAGE_OPI_SEQ IS NOT NULL) ";
        }

        return $query;
    }

    /***********************************************/
    /* ↓↓↓ SCHREG_STUDYREC_DAT(広島国際) ↓↓↓ */
    /***********************************************/

    //メイン
    //総合学習(900100)とＬＨＲ(940000)
    public function hirogakuStudyRec($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = "";
        //クエリー
        $query  = knje061Query::getStudyRecSql($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //新規作成
            if ($model->field["CREATEDIV"] == 1) {
                $query = knje061Query::deleteStudyRecSql($row, $model);
                $db->query($query);
                $query = knje061Query::insertStudyRecSql($row, $model);
                $db->query($query);
            //追加作成
            } elseif ($model->field["CREATEDIV"] == 2) {
                $query = knje061Query::existsStudyRecSql($row, $model);
                if ($db->getOne($query) == 0) {
                    $query = knje061Query::insertStudyRecSql($row, $model);
                    $db->query($query);
                }
                //上書作成
            } else {
                $query = knje061Query::existsStudyRecSql($row, $model);
                if ($db->getOne($query) == 0) {
                    $query = knje061Query::insertStudyRecSql($row, $model);
                    $db->query($query);
                } else {
                    $query = knje061Query::updateStudyRecSql($row, $model);
                    $db->query($query);
                }
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    public function insertStudyRecSql($row, $model)
    {
        $data = array();
        $data["SCHOOLCD"][TEXT]        = $row["SCHOOLCD"];
        $data["YEAR"][TEXT]            = $row["YEAR"];
        $data["SCHREGNO"][TEXT]        = $row["SCHREGNO"];
        $data["ANNUAL"][TEXT]          = $row["ANNUAL"];
        $data["CLASSCD"][TEXT]         = $row["CLASSCD"];
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["SCHOOL_KIND"][TEXT]      = $row["SCHOOL_KIND"];
            $data["CURRICULUM_CD"][TEXT]    = $row["CURRICULUM_CD"];
        }
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

    public function updateStudyRecSql($row, $model)
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
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   AND SCHOOL_KIND    = '".$row["SCHOOL_KIND"]."' ";
            $query .= "   AND CURRICULUM_CD  = '".$row["CURRICULUM_CD"]."' ";
        }
        $where .= "   AND SUBCLASSCD    = '".$row["SUBCLASSCD"]."' ";

        $query = Query::updateSQL($data, "SCHREG_STUDYREC_DAT", $where);
        return $query;
    }

    public function deleteStudyRecSql($row, $model)
    {
        $query  = " DELETE FROM SCHREG_STUDYREC_DAT ";
        $query .= " WHERE SCHOOLCD      = '".$row["SCHOOLCD"]."' ";
        $query .= "   AND YEAR          = '".$row["YEAR"]."' ";
        $query .= "   AND SCHREGNO      = '".$row["SCHREGNO"]."' ";
        $query .= "   AND ANNUAL        = '".$row["ANNUAL"]."' ";
        $query .= "   AND CLASSCD       = '".$row["CLASSCD"]."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   AND SCHOOL_KIND    = '".$row["SCHOOL_KIND"]."' ";
            $query .= "   AND CURRICULUM_CD  = '".$row["CURRICULUM_CD"]."' ";
        }
        $query .= "   AND SUBCLASSCD    = '".$row["SUBCLASSCD"]."' ";
        return $query;
    }

    public function existsStudyRecSql($row, $model)
    {
        $query  = " SELECT COUNT(*) FROM SCHREG_STUDYREC_DAT ";
        $query .= " WHERE SCHOOLCD      = '".$row["SCHOOLCD"]."' ";
        $query .= "   AND YEAR          = '".$row["YEAR"]."' ";
        $query .= "   AND SCHREGNO      = '".$row["SCHREGNO"]."' ";
        $query .= "   AND ANNUAL        = '".$row["ANNUAL"]."' ";
        $query .= "   AND CLASSCD       = '".$row["CLASSCD"]."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   AND SCHOOL_KIND    = '".$row["SCHOOL_KIND"]."' ";
            $query .= "   AND CURRICULUM_CD  = '".$row["CURRICULUM_CD"]."' ";
        }
        $query .= "   AND SUBCLASSCD    = '".$row["SUBCLASSCD"]."' ";
        return $query;
    }

    //総合学習(900100)とＬＨＲ(940000)
    //-- 講座名簿に登録されている生徒は、データ生成する。
    //-- 履修・修得単位は、単位マスタを参照して、無条件に与える。
    public function getStudyRecSql($model)
    {
        // 生成範囲の条件を分ける
        $operator = ($model->field["RANGE"] == 1) ? "=" : "<=";

        $query  = "";
        $query .= " WITH T_CHAIR AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.YEAR, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
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
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T2.SCHREGNO ";
        $query .= "     ) ";

        $query .= " SELECT ";
        $query .= "     '0' AS SCHOOLCD, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     MAX(L1.ANNUAL) AS ANNUAL, ";
        $query .= "     SUBSTR(T1.SUBCLASSCD,1,2) AS CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " T1.SCHOOL_KIND, ";
            $query .= " T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     CAST(NULL AS SMALLINT) AS VALUATION, ";
        $query .= "     MAX(L2.CREDITS) AS GET_CREDIT, ";
        $query .= "     CAST(NULL AS SMALLINT) AS ADD_CREDIT, ";
        $query .= "     MAX(L2.CREDITS) AS COMP_CREDIT ";
        $query .= " FROM ";
        $query .= "     T_CHAIR T1 ";
        $query .= "     INNER JOIN ".$model->vSchregRegdDat." L1 ";
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
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND L2.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "     AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "         AND L2.SUBCLASSCD   = T1.SUBCLASSCD ";
        $query .= "         AND L2.CREDITS IS NOT NULL ";
        $query .= " WHERE EXISTS (SELECT 'X' FROM ".$model->vSchregRegdDat." W1 ";
        $query .= "                WHERE W1.YEAR = '".CTRL_YEAR."' ";
        $query .= "                  AND W1.GRADE = '" .$model->annual ."' ";
        if (strlen($model->hr_class)) {
            $query .= "                  AND W1.HR_CLASS = '" .$model->hr_class ."' ";
        }
        if (strlen($model->coursecode)) {
            $query .= "                  AND W1.COURSECODE = '" .$model->coursecode ."' ";
        }
        if (strlen($model->schregno)) {
            $query .= "                  AND W1.SCHREGNO = '" .$model->schregno ."' ";
        }
        $query .= "                  AND W1.SCHREGNO = T1.SCHREGNO) ";

        //署名チェックに該当したデータを対象から除く。
        if ($model->Properties["useSeitoSidoYorokuShomeiKinou"] == 1) {
            $query .= "    AND NOT EXISTS (SELECT 'X' FROM ATTEST_OPINIONS_WK WK ";
            $query .= "                     WHERE WK.YEAR = T1.YEAR ";
            $query .= "                       AND WK.SCHREGNO = T1.SCHREGNO ";
            $query .= "                       AND WK.CHAGE_OPI_SEQ IS NOT NULL) ";
        }

        $query .= " GROUP BY ";
        $query .= "     T1.YEAR, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " T1.CLASSCD, ";
            $query .= " T1.SCHOOL_KIND, ";
            $query .= " T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    /****************************************************/
    /* ↓↓↓ STUDYRECREMARK_QUALIFIED_DAT(熊本) ↓↓↓ */
    /****************************************************/

    //メイン
    public function kumamotoStudyrecRemarkQualified($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //新規作成
        if ($model->field["CREATEDIV"] == 1) {
            //対象生徒のデータを全て削除
            $query = knje061Query::deleteStudyrecRemarkQualifiedSql($model);
            $db->query($query);
        }

        $query  = "";
        //クエリー
        $query  = knje061Query::getStudyrecRemarkQualifiedSql($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //新規作成
            if ($model->field["CREATEDIV"] == 1) {
                $query = knje061Query::insertStudyrecRemarkQualifiedSql($row, $model);
                $db->query($query);
            //追加作成
            } elseif ($model->field["CREATEDIV"] == 2) {
                $query = knje061Query::existsStudyrecRemarkQualifiedSql($row);
                if ($db->getOne($query) == 0) {
                    $query = knje061Query::insertStudyrecRemarkQualifiedSql($row, $model);
                    $db->query($query);
                }
                //上書作成
            } else {
                $query = knje061Query::existsStudyrecRemarkQualifiedSql($row);
                if ($db->getOne($query) == 0) {
                    $query = knje061Query::insertStudyrecRemarkQualifiedSql($row, $model);
                    $db->query($query);
                } else {
                    $query = knje061Query::updateStudyrecRemarkQualifiedSql($row, $model);
                    $db->query($query);
                }
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    public function insertStudyrecRemarkQualifiedSql($row, $model)
    {
        $data = array();
        $data["YEAR"][TEXT]             = $row["YEAR"];
        $data["SCHREGNO"][TEXT]         = $row["SCHREGNO"];
        $data["SEQ"][NUMBER]            = $row["SEQ"];
        $data["REGDDATE"][TEXT]         = $row["REGDDATE"];
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]       = $row["CLASSCD"];
            $data["SCHOOL_KIND"][TEXT]   = $row["SCHOOL_KIND"];
            $data["CURRICULUM_CD"][TEXT] = $row["CURRICULUM_CD"];
        }
        $data["SUBCLASSCD"][TEXT]       = $row["SUBCLASSCD"];
        $data["CONDITION_DIV"][TEXT]    = $row["CONDITION_DIV"];
        $data["CONTENTS"][TEXT]         = $row["CONTENTS"];
        $data["REMARK"][TEXT]           = $row["REMARK"];
        $data["CREDITS"][NUMBER]        = $row["CREDITS"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $query = Query::insertSQL($data, "STUDYRECREMARK_QUALIFIED_DAT");
        return $query;
    }

    public function updateStudyrecRemarkQualifiedSql($row, $model)
    {
        $data = array();
        $data["REGDDATE"][TEXT]         = $row["REGDDATE"];
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]       = $row["CLASSCD"];
            $data["SCHOOL_KIND"][TEXT]   = $row["SCHOOL_KIND"];
            $data["CURRICULUM_CD"][TEXT] = $row["CURRICULUM_CD"];
        }
        $data["SUBCLASSCD"][TEXT]       = $row["SUBCLASSCD"];
        $data["CONDITION_DIV"][TEXT]    = $row["CONDITION_DIV"];
        $data["CONTENTS"][TEXT]         = $row["CONTENTS"];
        $data["REMARK"][TEXT]           = $row["REMARK"];
        $data["CREDITS"][NUMBER]        = $row["CREDITS"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $where  = " WHERE YEAR          = '".$row["YEAR"]."' ";
        $where .= "   AND SCHREGNO      = '".$row["SCHREGNO"]."' ";
        $where .= "   AND SEQ           =  ".$row["SEQ"]." ";

        $query = Query::updateSQL($data, "STUDYRECREMARK_QUALIFIED_DAT", $where);
        return $query;
    }

    public function deleteStudyrecRemarkQualifiedSql($model)
    {
        // 生成範囲の条件を分ける
        $operator = ($model->field["RANGE"] == 1) ? "=" : "<=";

        $query  = " DELETE FROM STUDYRECREMARK_QUALIFIED_DAT T1 ";
        $query .= " WHERE T1.YEAR ".$operator." '".CTRL_YEAR."' ";
        //対象生徒
        $query .= "   AND EXISTS (SELECT 'X' FROM ".$model->vSchregRegdDat." W1 ";
        $query .= "                WHERE W1.YEAR = '".CTRL_YEAR."' ";
        $query .= "                  AND W1.GRADE = '" .$model->annual ."' ";
        if (strlen($model->hr_class)) {
            $query .= "                  AND W1.HR_CLASS = '" .$model->hr_class ."' ";
        }
        if (strlen($model->coursecode)) {
            $query .= "                  AND W1.COURSECODE = '" .$model->coursecode ."' ";
        }
        if (strlen($model->schregno)) {
            $query .= "                  AND W1.SCHREGNO = '" .$model->schregno ."' ";
        }
        $query .= "                  AND W1.SCHREGNO = T1.SCHREGNO) ";
        //署名チェックに該当したデータを対象から除く。
        if ($model->Properties["useSeitoSidoYorokuShomeiKinou"] == 1) {
            $query .= "    AND NOT EXISTS (SELECT 'X' FROM ATTEST_OPINIONS_WK WK ";
            $query .= "                     WHERE WK.YEAR = T1.YEAR ";
            $query .= "                       AND WK.SCHREGNO = T1.SCHREGNO ";
            $query .= "                       AND WK.CHAGE_OPI_SEQ IS NOT NULL) ";
        }

        return $query;
    }

    public function existsStudyrecRemarkQualifiedSql($row)
    {
        $query  = " SELECT COUNT(*) FROM STUDYRECREMARK_QUALIFIED_DAT ";
        $query .= " WHERE YEAR          = '".$row["YEAR"]."' ";
        $query .= "   AND SCHREGNO      = '".$row["SCHREGNO"]."' ";
        $query .= "   AND SEQ           =  ".$row["SEQ"]." ";
        return $query;
    }

    public function getStudyrecRemarkQualifiedSql($model)
    {
        // 生成範囲の条件を分ける
        $operator = ($model->field["RANGE"] == 1) ? "=" : "<=";

        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SEQ, ";
        $query .= "     T1.REGDDATE, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " T1.CLASSCD, ";
            $query .= " T1.SCHOOL_KIND, ";
            $query .= " T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.CONDITION_DIV, ";
        $query .= "     T1.CONTENTS, ";
        $query .= "     T1.REMARK, ";
        $query .= "     T1.CREDITS ";
        $query .= " FROM ";
        $query .= "     SCHREG_QUALIFIED_DAT T1 ";
        $query .= " WHERE T1.YEAR ".$operator." '".CTRL_YEAR."' ";

        $query .= "   AND EXISTS (SELECT 'X' FROM ".$model->vSchregRegdDat." W1 ";
        $query .= "                WHERE W1.YEAR = '".CTRL_YEAR."' ";
        $query .= "                  AND W1.GRADE = '" .$model->annual ."' ";
        if (strlen($model->hr_class)) {
            $query .= "                  AND W1.HR_CLASS = '" .$model->hr_class ."' ";
        }
        if (strlen($model->coursecode)) {
            $query .= "                  AND W1.COURSECODE = '" .$model->coursecode ."' ";
        }
        if (strlen($model->schregno)) {
            $query .= "                  AND W1.SCHREGNO = '" .$model->schregno ."' ";
        }
        $query .= "                  AND W1.SCHREGNO = T1.SCHREGNO) ";

        //署名チェックに該当したデータを対象から除く。
        if ($model->Properties["useSeitoSidoYorokuShomeiKinou"] == 1) {
            $query .= "    AND NOT EXISTS (SELECT 'X' FROM ATTEST_OPINIONS_WK WK ";
            $query .= "                     WHERE WK.YEAR = T1.YEAR ";
            $query .= "                       AND WK.SCHREGNO = T1.SCHREGNO ";
            $query .= "                       AND WK.CHAGE_OPI_SEQ IS NOT NULL) ";
        }

        $query .= " ORDER BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SEQ ";

        return $query;
    }

    /*****************************************************/
    /* ↓↓↓ STUDYREC_PROV_FLG_DAT(宮城県・常磐) ↓↓↓ */
    /*****************************************************/

    //メイン
    //仮評定フラグのコピー
    public function tokiwaStudyRecProvFlg($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //新規作成
        if ($model->field["CREATEDIV"] == 1) {
            //対象生徒のデータを全て削除
            $query = knje061Query::deleteStudyRecProvFlgSqlALL($model);
            $db->query($query);
        }

        $query  = "";
        //クエリー
        $query  = knje061Query::getStudyRecProvFlgSql($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //新規作成
            if ($model->field["CREATEDIV"] == 1) {
                $query = knje061Query::deleteStudyRecProvFlgSql($row, $model);
                $db->query($query);
                if ($row["PROV_FLG"] == "1") {
                    $query = knje061Query::insertStudyRecProvFlgSql($row, $model);
                    $db->query($query);
                }
                //追加作成
            } elseif ($model->field["CREATEDIV"] == 2) {
                $query = knje061Query::existsStudyRecProvFlgSql($row, $model);
                if ($db->getOne($query) == 0) {
                    $query = knje061Query::deleteStudyRecProvFlgSql($row, $model);
                    $db->query($query);
                    if ($row["PROV_FLG"] == "1") {
                        $query = knje061Query::insertStudyRecProvFlgSql($row, $model);
                        $db->query($query);
                    }
                }
                //追加上書
            } else {
                $query = knje061Query::deleteStudyRecProvFlgSql($row, $model);
                $db->query($query);
                if ($row["PROV_FLG"] == "1") {
                    $query = knje061Query::insertStudyRecProvFlgSql($row, $model);
                    $db->query($query);
                }
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    public function insertStudyRecProvFlgSql($row, $model)
    {
        $data = array();
        $data["SCHOOLCD"][TEXT]         = $row["SCHOOLCD"];
        $data["YEAR"][TEXT]             = $row["YEAR"];
        $data["SCHREGNO"][TEXT]         = $row["SCHREGNO"];
        $data["CLASSCD"][TEXT]          = $row["CLASSCD"];
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["SCHOOL_KIND"][TEXT]      = $row["SCHOOL_KIND"];
            $data["CURRICULUM_CD"][TEXT]    = $row["CURRICULUM_CD"];
        }
        $data["SUBCLASSCD"][TEXT]       = $row["SUBCLASSCD"];
        $data["PROV_FLG"][TEXT]         = $row["PROV_FLG"];
        $data["PROV_SEMESTER"][TEXT]    = $row["PROV_SEMESTER"];
        $data["PROV_TESTKINDCD"][TEXT]  = $row["PROV_TESTKINDCD"];
        $data["PROV_TESTITEMCD"][TEXT]  = $row["PROV_TESTITEMCD"];
        $data["PROV_SCORE_DIV"][TEXT]   = $row["PROV_SCORE_DIV"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $query = Query::insertSQL($data, "STUDYREC_PROV_FLG_DAT");
        return $query;
    }

    public function deleteStudyRecProvFlgSqlALL($model)
    {
        // 生成範囲の条件を分ける
        $operator = ($model->field["RANGE"] == 1) ? "=" : "<=";

        $query  = " DELETE FROM STUDYREC_PROV_FLG_DAT T1 ";
        $query .= " WHERE T1.SCHOOLCD   = '0' ";
        $query .= "   AND T1.YEAR       ".$operator." '".CTRL_YEAR."' ";
        //対象生徒
        $query .= "   AND EXISTS (SELECT 'X' FROM ".$model->vSchregRegdDat." W1 ";
        $query .= "                WHERE W1.YEAR                        = '".CTRL_YEAR."' ";
        $query .= "                  AND W1.GRADE                       = '" .$model->annual ."' ";
        if (strlen($model->hr_class)) {
            $query .= " AND W1.HR_CLASS     = '" .$model->hr_class ."' ";
        }
        if (strlen($model->coursecode)) {
            $query .= " AND W1.COURSECODE   = '" .$model->coursecode ."' ";
        }
        if (strlen($model->schregno)) {
            $query .= " AND W1.SCHREGNO     = '" .$model->schregno ."' ";
        }
        $query .= "                  AND W1.SCHREGNO                    = T1.SCHREGNO) ";
        //署名チェックに該当したデータを対象から除く。
        if ($model->Properties["useSeitoSidoYorokuShomeiKinou"] == 1) {
            $query .= "    AND NOT EXISTS (SELECT 'X' FROM ATTEST_OPINIONS_WK WK ";
            $query .= "                     WHERE WK.YEAR = T1.YEAR ";
            $query .= "                       AND WK.SCHREGNO = T1.SCHREGNO ";
            $query .= "                       AND WK.CHAGE_OPI_SEQ IS NOT NULL) ";
        }

        return $query;
    }

    public function deleteStudyRecProvFlgSql($row, $model)
    {
        $query  = " DELETE FROM STUDYREC_PROV_FLG_DAT ";
        $query .= " WHERE SCHOOLCD      = '".$row["SCHOOLCD"]."' ";
        $query .= "   AND YEAR          = '".$row["YEAR"]."' ";
        $query .= "   AND SCHREGNO      = '".$row["SCHREGNO"]."' ";
        $query .= "   AND CLASSCD       = '".$row["CLASSCD"]."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   AND SCHOOL_KIND    = '".$row["SCHOOL_KIND"]."' ";
            $query .= "   AND CURRICULUM_CD  = '".$row["CURRICULUM_CD"]."' ";
        }
        $query .= "   AND SUBCLASSCD    = '".$row["SUBCLASSCD"]."' ";
        return $query;
    }

    public function existsStudyRecProvFlgSql($row, $model)
    {
        $query  = " SELECT COUNT(*) FROM STUDYREC_PROV_FLG_DAT ";
        $query .= " WHERE SCHOOLCD      = '".$row["SCHOOLCD"]."' ";
        $query .= "   AND YEAR          = '".$row["YEAR"]."' ";
        $query .= "   AND SCHREGNO      = '".$row["SCHREGNO"]."' ";
        $query .= "   AND CLASSCD       = '".$row["CLASSCD"]."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   AND SCHOOL_KIND    = '".$row["SCHOOL_KIND"]."' ";
            $query .= "   AND CURRICULUM_CD  = '".$row["CURRICULUM_CD"]."' ";
        }
        $query .= "   AND SUBCLASSCD    = '".$row["SUBCLASSCD"]."' ";
        return $query;
    }

    //仮評定フラグのコピー
    //-- RECORD_PROV_FLG_DATからSTUDYREC_PROV_FLG_DATへコピーする。
    public function getStudyRecProvFlgSql($model)
    {
        // 生成範囲の条件を分ける
        $operator = ($model->field["RANGE"] == 1) ? "=" : "<=";

        $query  = "";
        $query .= " SELECT ";
        $query .= "     '0' AS SCHOOLCD, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     SUBSTR(T1.SUBCLASSCD,1,2) AS CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " T1.SCHOOL_KIND, ";
            $query .= " T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     P1.PROV_FLG, ";
        $query .= "     P1.PROV_SEMESTER, ";
        $query .= "     P1.PROV_TESTKINDCD, ";
        $query .= "     P1.PROV_TESTITEMCD, ";
        $query .= "     P1.PROV_SCORE_DIV ";
        $query .= " FROM ";
        if ($model->Properties["useRecordDat"] == 'RECORD_SCORE_HIST_DAT') {
            $query .= "     V_RECORD_SCORE_HIST_DAT T1 ";
        } else {
            $query .= "     RECORD_SCORE_DAT T1 ";
        }
        $query .= "     LEFT JOIN RECORD_PROV_FLG_DAT P1 ";
        $query .= "             ON  P1.YEAR = T1.YEAR  ";
        $query .= "             AND P1.SCHREGNO = T1.SCHREGNO ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND P1.CLASSCD = T1.CLASSCD ";
            $query .= "         AND P1.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "         AND P1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "             AND P1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= " WHERE T1.YEAR       ".$operator." '".CTRL_YEAR."' ";
        $query .= "   AND  T1.SEMESTER = '9' ";
        $query .= "   AND  T1.TESTKINDCD = '99' ";
        $query .= "   AND  T1.TESTITEMCD = '00' ";
        //パーツタイプ対応
        if ($model->Properties["useRecordDat"] == 'RECORD_SCORE_HIST_DAT') {
            $query .= "   AND  T1.SCORE_DIV = '09' ";
            $query .= "   AND (T1.VALUE is not null OR T1.COMP_CREDIT is not null OR T1.GET_CREDIT is not null) ";
        } elseif ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
            $query .= "   AND  T1.SCORE_DIV = '09' ";
            $query .= "   AND (T1.SCORE is not null OR T1.COMP_CREDIT is not null OR T1.GET_CREDIT is not null) ";
        } else {
            $query .= "   AND  T1.SCORE_DIV = '00' ";
            $query .= "   AND (T1.VALUE is not null OR T1.COMP_CREDIT is not null OR T1.GET_CREDIT is not null) ";
        }
        if ($model->schoolName == "kaijyo") {
            $query .= "   AND  NOT EXISTS (SELECT 'X' FROM SUBCLASS_WEIGHTING_COURSE_DAT S1 ";
            $query .= "                     INNER JOIN ".$model->vSchregRegdDat." R1 ON T1.SCHREGNO = R1.SCHREGNO ";
            $query .= "                       AND S1.YEAR = R1.YEAR  ";
            $query .= "                       AND S1.GRADE = R1.GRADE  ";
            $query .= "                       AND S1.COURSECD = R1.COURSECD  ";
            $query .= "                       AND S1.MAJORCD = R1.MAJORCD  ";
            $query .= "                       AND S1.COURSECODE = R1.COURSECODE  ";
            $query .= "                     WHERE S1.YEAR = T1.YEAR  ";
            $query .= "                       AND S1.FLG = '2' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                   AND S1.ATTEND_CLASSCD = T1.CLASSCD ";
                $query .= "                   AND S1.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ";
                $query .= "                   AND S1.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ";
            }
            $query .= "                       AND S1.ATTEND_SUBCLASSCD = T1.SUBCLASSCD) ";
        } else {
            $query .= "   AND  NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT S1 ";
            $query .= "                     WHERE S1.YEAR = T1.YEAR  ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                   AND S1.ATTEND_CLASSCD = T1.CLASSCD ";
                $query .= "                   AND S1.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ";
                $query .= "                   AND S1.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ";
            }
            $query .= "                       AND S1.ATTEND_SUBCLASSCD = T1.SUBCLASSCD) ";
        }
        //対象生徒
        $query .= "   AND EXISTS (SELECT 'X' FROM ".$model->vSchregRegdDat." W1 ";
        $query .= "                WHERE W1.YEAR                        = '".CTRL_YEAR."' ";
        $query .= "                  AND W1.GRADE                       = '" .$model->annual ."' ";
        if (strlen($model->hr_class)) {
            $query .= " AND W1.HR_CLASS     = '" .$model->hr_class ."' ";
        }
        if (strlen($model->coursecode)) {
            $query .= " AND W1.COURSECODE   = '" .$model->coursecode ."' ";
        }
        if (strlen($model->schregno)) {
            $query .= " AND W1.SCHREGNO     = '" .$model->schregno ."' ";
        }
        $query .= "                  AND W1.SCHREGNO                    = T1.SCHREGNO) ";
        //署名チェックに該当したデータを対象から除く。
        if ($model->Properties["useSeitoSidoYorokuShomeiKinou"] == 1) {
            $query .= "    AND NOT EXISTS (SELECT 'X' FROM ATTEST_OPINIONS_WK WK ";
            $query .= "                     WHERE WK.YEAR = T1.YEAR ";
            $query .= "                       AND WK.SCHREGNO = T1.SCHREGNO ";
            $query .= "                       AND WK.CHAGE_OPI_SEQ IS NOT NULL) ";
        }

        $query .= " ORDER BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " T1.CLASSCD, ";
            $query .= " T1.SCHOOL_KIND, ";
            $query .= " T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD ";

        return $query;
    }


    //履歴一覧
    public function getListRireki($model)
    {
        $year = CTRL_YEAR;
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     L1.STAFFNAME, ";
        $query .= "     L2.GRADE_NAME1, ";
        $query .= "     L3.HR_NAME, ";
        $query .= "     L4.COURSECODENAME, ";
        $query .= "     L5.NAME_SHOW, ";
        $query .= "     P1.SEMESTERNAME || '-' || P2.TESTITEMNAME AS TESTITEMNAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_EXEC_DAT T1 ";
        $query .= "     LEFT JOIN STAFF_MST L1 ON L1.STAFFCD = T1.REGISTERCD ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT L2 ON L2.YEAR = T1.YEAR AND L2.GRADE = T1.GRADE ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT L3 ON L3.YEAR = T1.YEAR AND L3.GRADE = T1.GRADE AND L3.HR_CLASS = T1.HR_CLASS AND L3.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     LEFT JOIN COURSECODE_MST L4 ON L4.COURSECODE = T1.COURSECODE ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST L5 ON L5.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SEMESTER_MST P1 ";
        $query .= "              ON T1.YEAR = P1.YEAR ";
        $query .= "             AND T1.PROV_SEMESTER    = P1.SEMESTER ";
        $query .= "     LEFT JOIN ".$model->Properties["useTestCountflg"]." P2 ";
        $query .= "              ON T1.YEAR = P2.YEAR ";
        if ($model->Properties["useTestCountflg"] != "TESTITEM_MST_COUNTFLG") {
            $query .= "         AND T1.PROV_SEMESTER    = P2.SEMESTER ";
        }
        $query .= "             AND T1.PROV_TESTKINDCD  = P2.TESTKINDCD ";
        $query .= "             AND T1.PROV_TESTITEMCD  = P2.TESTITEMCD ";
        if ($model->Properties["useTestCountflg"] == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
            $query .= "         AND T1.PROV_SCORE_DIV   = P2.SCORE_DIV ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND T1.SCHOOLCD = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.CALC_DATE DESC, ";
        $query .= "     T1.CALC_TIME DESC ";
        return $query;
    }
    /* 実行履歴 */
    public function executeRireki($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //実行日付・時間を取得
        $calcRow = $db->getRow(knje061Query::getCalcDateTime(), DB_FETCHMODE_ASSOC);
        $calcDate = $calcRow["CALC_DATE"];//実行日付
        $calcTime = $calcRow["CALC_TIME"];//実行時間
        //実行履歴データ・追加
        knje061Query::getInsertRireki($db, $calcDate, $calcTime, $model);

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
    //実行日付・時間を取得
    public function getCalcDateTime()
    {
        $query  = " with t_date_time (CALC_DATE,CALC_TIME) as ( ";
        $query .= " values( ";
        $query .= "     date(sysdate()), ";
        $query .= "     time(sysdate()) ";
        $query .= " )) ";
        $query .= "  ";
        $query .= " select * from t_date_time ";
        return $query;
    }
    //卒業可能学年チェック
    public function checkGrdGrade($grade)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' AND ";
        $query .= "     NAME1   = 'H' AND ";
        $query .= "     '".$grade."' BETWEEN NAMESPARE2 AND NAMESPARE3 ";
        return $query;
    }
    //テーブルのフィールド確認
    public function getTableColumn($tabname, $colname)
    {
        $query  = " SELECT T1.COLUMN_NAME ";
        $query .= " FROM ";
        $query .= "     SYSIBM.COLUMNS T1 ";
        $query .= " WHERE ";
        $query .= "     T1.TABLE_NAME = '".$tabname."' AND T1.COLUMN_NAME = '".$colname."' ";
        return $query;
    }
    //仮評定データ取得
    public function getProvData($model)
    {
        // 生成範囲の条件を分ける
        $operator = ($model->field["RANGE"] == 1) ? "=" : "<=";

        $query  = " SELECT DISTINCT ";
        $query .= "     CASE WHEN T2.PROV_SEMESTER IS NULL THEN '99' ELSE T2.PROV_SEMESTER END AS PROV_SEMESTER, ";
        $query .= "     CASE WHEN T2.PROV_TESTKINDCD IS NULL THEN '999' ELSE T2.PROV_TESTKINDCD END AS PROV_TESTKINDCD, ";
        $query .= "     CASE WHEN T2.PROV_TESTITEMCD IS NULL THEN '999' ELSE T2.PROV_TESTITEMCD END AS PROV_TESTITEMCD, ";
        $query .= "     CASE WHEN T2.PROV_SCORE_DIV IS NULL THEN '999' ELSE T2.PROV_SCORE_DIV END AS PROV_SCORE_DIV, ";
        $query .= "     VALUE(L1.SEMESTERNAME,'　') AS SEMESTERNAME, ";
        $query .= "     VALUE(L2.TESTITEMNAME,'　') AS TESTITEMNAME ";
        $query .= " FROM ";
        $query .= "     STUDYREC_PROV_FLG_DAT T2 ";
        $query .= "     LEFT JOIN SEMESTER_MST L1 ";
        $query .= "              ON T2.YEAR = L1.YEAR ";
        $query .= "             AND T2.PROV_SEMESTER    = L1.SEMESTER ";
        $query .= "     LEFT JOIN ".$model->Properties["useTestCountflg"]." L2 ";
        $query .= "              ON T2.YEAR = L2.YEAR ";
        if ($model->Properties["useTestCountflg"] != "TESTITEM_MST_COUNTFLG") {
            $query .= "             AND T2.PROV_SEMESTER    = L2.SEMESTER ";
        }
        $query .= "             AND T2.PROV_TESTKINDCD  = L2.TESTKINDCD ";
        $query .= "             AND T2.PROV_TESTITEMCD  = L2.TESTITEMCD ";
        if ($model->Properties["useTestCountflg"] == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
            $query .= "             AND T2.PROV_SCORE_DIV  = L2.SCORE_DIV ";
        }
        //対象生徒
        $query .= " WHERE T2.SCHOOLCD   = '0' ";
        $query .= "   AND T2.YEAR       ".$operator." '".CTRL_YEAR."' ";
        $query .= "   AND EXISTS (SELECT 'X' FROM ".$model->vSchregRegdDat." W1 ";
        $query .= "                WHERE W1.YEAR                        = '".CTRL_YEAR."' ";
        $query .= "                  AND W1.GRADE                       = '" .$model->annual ."' ";
        if (strlen($model->hr_class)) {
            $query .= " AND W1.HR_CLASS     = '" .$model->hr_class ."' ";
        }
        if (strlen($model->coursecode)) {
            $query .= " AND W1.COURSECODE   = '" .$model->coursecode ."' ";
        }
        if (strlen($model->schregno)) {
            $query .= " AND W1.SCHREGNO     = '" .$model->schregno ."' ";
        }
        $query .= "                  AND W1.SCHREGNO                    = T2.SCHREGNO) ";
        $query .= " ORDER BY ";
        $query .= "     PROV_SEMESTER, ";
        $query .= "     PROV_TESTKINDCD, ";
        $query .= "     PROV_TESTITEMCD, ";
        $query .= "     PROV_SCORE_DIV ";
        return $query;
    }
    //評定有の件数チェック
    public function getValuationCnt($model)
    {
        // 生成範囲の条件を分ける
        $operator = ($model->field["RANGE"] == 1) ? "=" : "<=";

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT T2 ";
        //対象生徒
        $query .= " WHERE T2.SCHOOLCD   = '0' ";
        $query .= "   AND T2.YEAR       ".$operator." '".CTRL_YEAR."' ";
        $query .= "   AND EXISTS (SELECT 'X' FROM ".$model->vSchregRegdDat." W1 ";
        $query .= "                WHERE W1.YEAR                        = '".CTRL_YEAR."' ";
        $query .= "                  AND W1.GRADE                       = '" .$model->annual ."' ";
        if (strlen($model->hr_class)) {
            $query .= " AND W1.HR_CLASS     = '" .$model->hr_class ."' ";
        }
        if (strlen($model->coursecode)) {
            $query .= " AND W1.COURSECODE   = '" .$model->coursecode ."' ";
        }
        if (strlen($model->schregno)) {
            $query .= " AND W1.SCHREGNO     = '" .$model->schregno ."' ";
        }
        $query .= "                  AND W1.SCHREGNO                    = T2.SCHREGNO) ";
        $query .= "   AND T2.VALUATION IS NOT NULL ";
        return $query;
    }
    //実行履歴データ・追加
    public function getInsertRireki($db, $calcDate, $calcTime, $model)
    {
        $data = array();
        if ($model->Properties["useSchool_KindField"] == "1") {
            $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
            if ($model->field["METHOD"] == 2) { //csv取込み
                $data["SCHOOL_KIND"][TEXT]  = strlen(SCHOOLKIND) ? SCHOOLKIND : "H";
            } else {
                $data["SCHOOL_KIND"][TEXT]  = $model->regdGdat[$model->annual]["SCHOOL_KIND"];
            }
        }
        $data["CALC_DATE"][TEXT]            = $calcDate;
        $data["CALC_TIME"][TEXT]            = $calcTime;
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["SEMESTER"][TEXT]             = CTRL_SEMESTER;
        $data["KIND"][TEXT]                 = $model->field["KIND"];
        $data["CHECK_KANTEN"][TEXT]         = $model->check_kanten;
        $data["METHOD"][TEXT]               = $model->field["METHOD"];
        $data["CREATEDIV"][TEXT]            = $model->field["CREATEDIV"];
        $data["RANGE"][TEXT]                = $model->field["RANGE"];
        $data["GRADE"][TEXT]                = $model->annual;
        $data["HR_CLASS"][TEXT]             = $model->hr_class;
        $data["COURSECODE"][TEXT]           = $model->coursecode;
        $data["SCHREGNO"][TEXT]             = $model->schregno;

        $seq = "#";
        $pro  = "";
        $pro .= "KNJE061_JVIEWSTAT_RECORD_DAT=" . $model->Properties["KNJE061_JVIEWSTAT_RECORD_DAT"] . $seq;
        $pro .= "useFromStudyrecToJviewstat=" . $model->Properties["useFromStudyrecToJviewstat"] . $seq;
        $pro .= "useKantenGakunenSeiseki=" . $model->Properties["useKantenGakunenSeiseki"] . $seq;
        $pro .= "useKantenToStudyRec=" . $model->Properties["useKantenToStudyRec"];
        $data["PROPERTIES"][TEXT]           = $pro;

        //成績データ生成の時、仮評定を処理
        if ($model->field["KIND"] == "2") {
            //卒業可能学年チェック
            $query = knje061Query::checkGrdGrade($model->annual);
            $grdGrade = $db->getOne($query);
            if ($model->Properties["useProvFlg"] == 1 && $grdGrade > 0 && $model->field["KIND"] == "2") {
                if ('' != $db->getOne(knje061Query::getTableColumn("STUDYREC_PROV_FLG_DAT", "PROV_TESTKINDCD"))) {
                    //仮評定データ取得
                    $prov = $db->getRow(knje061Query::getProvData($model), DB_FETCHMODE_ASSOC);
                    //SCHREG_STUDYREC_DATの評定有チェック
                    $valCnt = $db->getOne(knje061Query::getValuationCnt($model));
                    //セット
                    if (strlen($prov["PROV_SEMESTER"]) > 0 || $valCnt > 0) {
                        $data["PROV_FLG"][TEXT]         = strlen($prov["PROV_SEMESTER"]) ? "1" : "2";
                        $data["PROV_SEMESTER"][TEXT]    = $prov["PROV_SEMESTER"] == "99" ? "" : $prov["PROV_SEMESTER"];
                        $data["PROV_TESTKINDCD"][TEXT]  = $prov["PROV_TESTKINDCD"] == "999" ? "" : $prov["PROV_TESTKINDCD"];
                        $data["PROV_TESTITEMCD"][TEXT]  = $prov["PROV_TESTITEMCD"] == "999" ? "" : $prov["PROV_TESTITEMCD"];
                        $data["PROV_SCORE_DIV"][TEXT]   = $prov["PROV_SCORE_DIV"] == "999" ? "" : $prov["PROV_SCORE_DIV"];
                    }
                }
            }
        }

        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "SCHREG_STUDYREC_EXEC_DAT");
        $db->query($query);
    }

    /******************************/
    /* ↓↓↓ 行動の記録他 ↓↓↓ */
    /******************************/

    //行動の記録
    public function getBehaviorDat($db, $model, $schregNo, $year, $annual, $grade)
    {
        //行動の記録削除
        $query = knje061Query::delBehaviorDatSql($model, $schregNo, $year);
        $db->query($query);
        //行動の記録取得
        $query = knje061Query::getBehaviorSemesDatSql($model, $schregNo, $year, $grade);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //追加
            knje061Query::insBehaviorDatSql($db, $model, $schregNo, $year, $annual, $row);
        }
        $result->free();
    }
    //行動の記録取得
    public function getBehaviorSemesDatSql($model, $schregNo, $year, $grade)
    {
        $query  = "";
        $query .= " WITH BEHAVIOR AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         L1.SCHOOL_KIND, ";
        $query .= "         T2.STUDYREC_CODE, ";
        $query .= "         rtrim(char(smallint(ceil(avg(float(smallint(N1.NAMESPARE2))))))) AS RECORD_SUUJI "; //切り上げ
        $query .= "     FROM ";
        $query .= "         BEHAVIOR_SEMES_DAT T1 ";
        $query .= "         INNER JOIN BEHAVIOR_SEMES_MST T2 ON T2.YEAR = T1.YEAR ";
        $query .= "                                         AND T2.GRADE = '{$grade}' ";
        $query .= "                                         AND T2.CODE = T1.CODE ";
        $query .= "         LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'D036' ";
        $query .= "                              AND N1.NAME1 = T1.RECORD ";
        $query .= "         LEFT JOIN SCHREG_REGD_GDAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                                      AND L1.GRADE = '{$grade}' ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '{$year}' ";
        $query .= "         AND T1.SEMESTER IN (SELECT MAX(SEMESTER) FROM SEMESTER_MST WHERE YEAR = '{$year}' AND SEMESTER <> '9') "; //最終学期
        $query .= "         AND T1.SCHREGNO = '{$schregNo}' ";
        $query .= "         AND T2.STUDYREC_CODE IS NOT NULL ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         L1.SCHOOL_KIND, ";
        $query .= "         T2.STUDYREC_CODE ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     CASE WHEN T1.SCHOOL_KIND = 'J' THEN '1' ";
        $query .= "          WHEN T1.SCHOOL_KIND = 'P' THEN '3' ";
        $query .= "          ELSE '0' ";
        $query .= "     END AS DIV, ";
        $query .= "     T1.STUDYREC_CODE AS CODE, ";
        $query .= "     N1.NAME1 AS RECORD ";
        $query .= " FROM ";
        $query .= "     BEHAVIOR T1 ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'D036' ";
        $query .= "                          AND N1.NAMESPARE2 = T1.RECORD_SUUJI ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOL_KIND IN ('J','P') "; //中学・小学
        $query .= " ORDER BY ";
        $query .= "     CODE ";

        return $query;
    }
    public function insBehaviorDatSql($db, $model, $schregNo, $year, $annual, $row)
    {
        $data = array();
        $data["YEAR"][TEXT]             = $year;
        $data["SCHREGNO"][TEXT]         = $schregNo;
        $data["DIV"][TEXT]              = $row["DIV"];
        $data["CODE"][TEXT]             = $row["CODE"];
        $data["ANNUAL"][TEXT]           = $annual;
        $data["RECORD"][TEXT]           = $row["RECORD"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $query = Query::insertSQL($data, "BEHAVIOR_DAT");
        $db->query($query);
    }
    public function delBehaviorDatSql($model, $schregNo, $year)
    {
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     BEHAVIOR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SCHREGNO = '{$schregNo}' ";
        $query .= "     AND DIV IN ('1','3') ";

        return $query;
    }

    //総合的な学習の時間
    //出欠の備考
    public function getHtrainremarkDat($db, $model, $schregNo, $year, $annual, $schoolKind)
    {
        //総合的な学習の時間取得
        $setArray1 = array();
        $setArray2 = array();
        $query = knje061Query::getRecordTotalstudytimeDatSql($model, $schregNo, $year, $schoolKind, $db);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $totalstudytime = '';
            if ($row["NAMESPARE1"] == '1') {
                $totalstudytime = $row["REMARK"];
            } else {
                $totalstudytime = $row["TOTALSTUDYTIME"];
            }
            if (strlen($totalstudytime)) {
                $setArray1[] = $totalstudytime;
            }
            if (strlen($row["TOTALSTUDYACT"])) {
                $setArray2[] = $row["TOTALSTUDYACT"];
            }
        }
        $result->free();
        //出欠の備考取得
        $setArray = array();
        $query = knje061Query::getSchregAttendrecDatSql($model, $schregNo, $year);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (0 < $row["SUSPEND"]) {
                $setArray[] = "出停".$row["SUSPEND"];
            }
            if (0 < $row["MOURNING"]) {
                $setArray[] = "忌引".$row["MOURNING"];
            }
            if (0 < $row["SICK"]) {
                $setArray[] = "欠席".$row["SICK"];
            }
        }
        $result->free();
        //(総学)そのまま足す
        $setRemark1 = "";
        $setRemark2 = "";
        if (0 < get_count($setArray1)) {
            foreach ($setArray1 as $key => $val) {
                $setRemark1 .= $val;
            }
        }
        if (0 < get_count($setArray2)) {
            foreach ($setArray2 as $key => $val) {
                $setRemark2 .= $val;
            }
        }
        //(出欠)カンマ区切りで足す
        $setRemark = "";
        $seq = "";
        if (0 < get_count($setArray)) {
            foreach ($setArray as $key => $val) {
                $setRemark .= $seq.$val;
                $seq = ",";
            }
        }
        //追加・更新
        $setData = array();
        $setData["TOTALSTUDYTIME"]  = $setRemark1;
        $setData["TOTALSTUDYACT"]   = $setRemark2;
        $setData["SICK"]            = $setRemark;
        knje061Query::insupdHtrainremarkDatSql($db, $model, $schregNo, $year, $annual, $setData);
    }
    //総合的な学習の時間取得
    public function getRecordTotalstudytimeDatSql($model, $schregNo, $year, $schoolKind, $db)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.TOTALSTUDYTIME, ";
        $query .= "     T1.TOTALSTUDYACT, ";
        $query .= "     T2.NAMESPARE1, ";
        $query .= "     T3.REMARK ";
        $query .= " FROM ";
        $query .= "     RECORD_TOTALSTUDYTIME_DAT T1 ";
        if (0 < knje061query::getDSchoolKind08($year, $schoolKind, $db)) {
            $query .= "     LEFT JOIN V_NAME_MST T2 ON T2.YEAR = T1.YEAR ";
            $query .= "         AND T2.NAMECD1 = 'D{$schoolKind}08' ";
            $query .= "         AND T2.NAMECD2 = T1.CLASSCD ";
        } else {
            $query .= "     LEFT JOIN V_NAME_MST T2 ON T2.YEAR = T1.YEAR ";
            $query .= "         AND T2.NAMECD1 = 'D008' ";
            $query .= "         AND T2.NAMECD2 = T1.CLASSCD ";
        }
        $query .= "     LEFT JOIN (SELECT SCHREGNO, MAX(GRADE) AS GRADE ";
        $query .= "                FROM SCHREG_REGD_DAT  ";
        $query .= "                WHERE YEAR = '{$year}' ";
        $query .= "                GROUP BY SCHREGNO) T_MAX_GRADE ON T_MAX_GRADE.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN RECORD_SCORE_DAT T_SCORE ON T_SCORE.YEAR = T1.YEAR ";
        $query .= "         AND T_SCORE.SEMESTER = '9' ";
        $query .= "         AND T_SCORE.TESTKINDCD = '99' ";
        if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV' || $model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
            $query .= "     AND T_SCORE.SCORE_DIV = '09' ";
        } else {
            $query .= "     AND T_SCORE.SCORE_DIV = '00' ";
        }
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T_SCORE.CLASSCD = T1.CLASSCD ";
            $query .= "     AND T_SCORE.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "     AND T_SCORE.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "         AND T_SCORE.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "         AND T_SCORE.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN HTRAINREMARK_TEMP_DAT T3 ON T3.YEAR = T1.YEAR ";
        $query .= "         AND T_MAX_GRADE.GRADE = T3.GRADE ";
        $query .= "         AND T3.DATA_DIV = '02' ";
        if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV' || $model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
            $query .= "         AND T3.PATTERN_CD = CASE T_SCORE.SCORE WHEN 5 THEN 'A'  ";
        } else {
            $query .= "         AND T3.PATTERN_CD = CASE T_SCORE.VALUE WHEN 5 THEN 'A'  ";
        }
        $query .= "                                                WHEN 4 THEN 'B'  ";
        $query .= "                                                WHEN 3 THEN 'C'  ";
        $query .= "                                                WHEN 2 THEN 'D'  ";
        $query .= "                                                WHEN 1 THEN 'E'  ";
        $query .= "                             END ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' ";
        $query .= "     AND T1.SEMESTER = VALUE(T2.ABBV3, '9') ";
        $query .= "     AND T1.SCHREGNO = '{$schregNo}' ";
        $query .= "     AND T2.NAMESPARE1 = '1' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD ";

        return $query;
    }
    //出欠の備考取得
    public function getSchregAttendrecDatSql($model, $schregNo, $year)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     VALUE(T1.SUSPEND,0)     AS SUSPEND, ";
        $query .= "     VALUE(T1.MOURNING,0)    AS MOURNING, ";
        $query .= "     VALUE(T1.SICK,0) + VALUE(T1.ACCIDENTNOTICE,0) + VALUE(T1.NOACCIDENTNOTICE,0) AS SICK ";
        $query .= " FROM ";
        $query .= "     SCHREG_ATTENDREC_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD = '0' ";
        $query .= "     AND T1.YEAR = '{$year}' ";
        $query .= "     AND T1.SCHREGNO = '{$schregNo}' ";

        return $query;
    }
    public function insupdHtrainremarkDatSql($db, $model, $schregNo, $year, $annual, $setData)
    {
        $data = array();
        $data["YEAR"][TEXT]             = $year;
        $data["SCHREGNO"][TEXT]         = $schregNo;
        $data["ANNUAL"][TEXT]           = $annual;
        $data["TOTALSTUDYACT"][TEXT]    = $setData["TOTALSTUDYACT"];
        $data["TOTALSTUDYVAL"][TEXT]    = $setData["TOTALSTUDYTIME"];
        $data["ATTENDREC_REMARK"][TEXT] = $setData["SICK"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $query = knje061Query::existsHtrainremarkDatSql($model, $schregNo, $year);
        if ($db->getOne($query) == 0) {
            $query = Query::insertSQL($data, "HTRAINREMARK_DAT");
        } else {
            $where  = " WHERE ";
            $where .= "     YEAR = '{$year}' ";
            $where .= "     AND SCHREGNO = '{$schregNo}' ";
            $query = Query::updateSQL($data, "HTRAINREMARK_DAT", $where);
        }
        $db->query($query);
    }
    public function existsHtrainremarkDatSql($model, $schregNo, $year)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SCHREGNO = '{$schregNo}' ";

        return $query;
    }

    public function getZ010()
    {
        $query = " SELECT * FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ";
        return $query;
    }

    public function getDSchoolKind08($year, $schoolKind, $db)
    {
        $cnt = $db->getOne(" SELECT COUNT(*) FROM V_NAME_MST WHERE YEAR = '{$year}' AND NAMECD1 = 'D{$schoolKind}08' ");
        return $cnt;
    }
}
