<?php

require_once('for_php7.php');

class knje061bquery extends Query
{

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
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
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

        //DELETE(本校区分:0)
        $query = " DELETE FROM SCHREG_STUDYREC_DAT T1 ";
        $query .= " WHERE T1.schoolcd = '0'";
        $query .= "   AND T1.YEAR ".$OPERATOR."'" .CTRL_YEAR ."'";
        $query .= "   AND EXISTS (SELECT 'X' FROM ".$model->vSchregRegdDat." W ";
        $query .= "                WHERE W.YEAR     = '" .CTRL_YEAR ."'";
//        $query .= "                  AND W.SEMESTER = '" .$model->maxSemester ."'";
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
        $db->query(knje061bQuery::insertStudyrecQuery($model, "0", "off"));

        // DELETE(本校区分:2)
        $query = " DELETE FROM SCHREG_STUDYREC_DAT T1 ";
        $query .= " WHERE T1.schoolcd = '2'";
        $query .= "   AND T1.YEAR ".$OPERATOR."'" .CTRL_YEAR ."'";
        $query .= "   AND EXISTS (SELECT 'X' FROM ".$model->vSchregRegdDat." W ";
        $query .= "                WHERE W.YEAR     = '" .CTRL_YEAR ."'";
//        $query .= "                  AND W.SEMESTER = '" .$model->maxSemester ."'";
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
        $db->query(knje061bQuery::insertStudyrecQuery($model, "2", "off"));

        //どの資格データへ取込むか(1:STUDYRECREMARK_QUALIFIED_DAT それ以外:STUDYRECREMARK_DAT)
        if ($model->Properties["useStudyrecRemarkQualifiedDat"] != 1) {
            // DELETE(備考データ)
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
            knje061bQuery::insertStudyrecRemark($db, $model, "off");
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

        //INSERT(本校区分:0)
        $db->query(knje061bQuery::insertStudyrecQuery($model, "0", "on"));

        //INSERT(本校区分:2)
        $db->query(knje061bQuery::insertStudyrecQuery($model, "2", "on"));

        //どの資格データへ取込むか(1:STUDYRECREMARK_QUALIFIED_DAT それ以外:STUDYRECREMARK_DAT)
        if ($model->Properties["useStudyrecRemarkQualifiedDat"] != 1) {
            //INSERT(備考データ)
            knje061bQuery::insertStudyrecRemark($db, $model, "on");
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

        //作成するデータを一旦削除する(本校区分:0)
        $db->query(knje061bQuery::deleteStudyrecQuery($model, "0"));

        //INSERT(本校区分:0)
        $db->query(knje061bQuery::insertStudyrecQuery($model, "0", "on"));

        //作成するデータを一旦削除する(本校区分:2)
        $db->query(knje061bQuery::deleteStudyrecQuery($model, "2"));

        //INSERT(本校区分:2)
        $db->query(knje061bQuery::insertStudyrecQuery($model, "2", "on"));

        //どの資格データへ取込むか(1:STUDYRECREMARK_QUALIFIED_DAT それ以外:STUDYRECREMARK_DAT)
        if ($model->Properties["useStudyrecRemarkQualifiedDat"] != 1) {
            //作成するデータを一旦削除する(備考データ)
            $db->query(knje061bQuery::deleteStudyrecRemark($model));
            //INSERT(備考データ)
            knje061bQuery::InsertStudyrec_Remark($db, $model, "on");
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    //学籍学習記録データ削除クエリ文 === 作成するデータを一旦削除する
    public function deleteStudyrecQuery($model, $schoolcd)
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
            $query .= knje061bQuery::insertStudyrecQuerySchoolcd0($model, "off");
        }
        if ($schoolcd == "2") {
            $query .= knje061bQuery::insertStudyrecQuerySchoolcd2($model, "off");
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
    public function insertStudyrecQuery($model, $schoolcd, $not_exists)
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
            $query .= knje061bQuery::insertStudyrecQuerySchoolcd0($model, $not_exists);
        }
        if ($schoolcd == "2") {
            $query .= knje061bQuery::insertStudyrecQuerySchoolcd2($model, $not_exists);
        }
        //echo "---start---schoolcd = " .$schoolcd ." not_exists = " .$not_exists ."<BR>";
        //echo $query;
        //echo "<BR>---end---schoolcd = " .$schoolcd ." not_exists = " .$not_exists ."<BR>";

        return $query;
    }

    //学籍学習記録データ追加クエリ文 === 本校区分:0
    public function insertStudyrecQuerySchoolcd0($model, $not_exists)
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

        //指導計画データから取込
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T5.ANNUAL, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     CAST(T1.REMARK4 AS SMALLINT) AS VALUATION, ";
        $query .= "     T2.CREDITS AS GET_CREDIT, ";
        $query .= "     CAST(NULL AS SMALLINT) AS ADD_CREDIT, ";
        $query .= "     T2.CREDITS AS COMP_CREDIT ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_GUIDANCE_DAT T1 ";
        $query .= "     INNER JOIN ( ";
        $query .= "         SELECT ";
        $query .= "             YEAR, ";
        $query .= "             MAX(SEMESTER) AS SEMESTER, ";
        $query .= "             SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             CLASSCD, ";
            $query .= "             SCHOOL_KIND, ";
            $query .= "             CURRICULUM_CD, ";
        }
        $query .= "             SUBCLASSCD ";
        $query .= "         FROM ";
        $query .= "             HREPORTREMARK_GUIDANCE_DAT ";
        $query .= "         GROUP BY ";
        $query .= "             YEAR, ";
        $query .= "             SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             CLASSCD, ";
            $query .= "             SCHOOL_KIND, ";
            $query .= "             CURRICULUM_CD, ";
        }
        $query .= "             SUBCLASSCD ";
        $query .= "     ) M1 ON M1.YEAR = T1.YEAR ";
        $query .= "         AND M1.SEMESTER = T1.SEMESTER ";
        $query .= "         AND M1.SCHREGNO = T1.SCHREGNO ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND M1.CLASSCD = T1.CLASSCD ";
            $query .= "         AND M1.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "         AND M1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "         AND M1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "     INNER JOIN ".$model->vSchregRegdDat." T5 ON T5.YEAR = T1.YEAR AND T5.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN CREDIT_MST T2 ON T2.YEAR = T1.YEAR ";
        $query .= "         AND T2.COURSECD = T5.COURSECD ";
        $query .= "         AND T2.MAJORCD = T5.MAJORCD ";
        $query .= "         AND T2.GRADE = T5.GRADE ";
        $query .= "         AND T2.COURSECODE = T5.COURSECODE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND T2.CLASSCD = T1.CLASSCD ";
            $query .= "         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "         AND T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR ".$OPERATOR." '".CTRL_YEAR."' ";
        $query .= "     AND T1.REMARK4 IS NOT NULL "; //評価ありのみ取り込む
        //指定生徒
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
        //合併元科目を除く
        $query .= "   AND  NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT S1 ";
        $query .= "                     WHERE S1.YEAR = T1.YEAR  ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                   AND S1.ATTEND_CLASSCD = T1.CLASSCD ";
            $query .= "                   AND S1.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                   AND S1.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "                       AND S1.ATTEND_SUBCLASSCD = T1.SUBCLASSCD) ";

        $query .= "         ) tbl1 ";

        //教科科目名称マスタのマスタ化
        if ($model->Properties["useClassDetailDat"] == '1') {
            $query .= "         LEFT JOIN CLASS_DETAIL_DAT N1 ON N1.YEAR        = tbl1.YEAR ";
            $query .= "                                      AND N1.CLASSCD     = substr(tbl1.SUBCLASSCD,1,2) ";
            $query .= "                                      AND N1.SCHOOL_KIND = tbl1.SCHOOL_KIND ";
            $query .= "                                      AND CLASS_SEQ      = '003' ";
        } else {
            $query .= "         LEFT JOIN NAME_YDAT N1 ON N1.YEAR = tbl1.YEAR ";
            $query .= "                               AND N1.NAMECD1 = 'D008' ";
            $query .= "                               AND N1.NAMECD2 = substr(tbl1.SUBCLASSCD,1,2) ";
        }


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
        if ($not_exists == "on") {
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
                $query .= " WHERE ";
            }
            $query .= "        NOT EXISTS (SELECT 'X' FROM ATTEST_OPINIONS_WK WK ";
            $query .= "                     WHERE WK.YEAR = tbl.YEAR ";
            $query .= "                       AND WK.SCHREGNO = tbl.SCHREGNO ";
            $query .= "                       AND WK.CHAGE_OPI_SEQ IS NOT NULL) ";
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

        $query .= knje061bQuery::getInsertStudyrecRemarkQuery($model, "off");

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
        $result = $db->query(knje061bQuery::getInsertStudyrecRemarkQuery($model, $not_exists) .$query);
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
                $db->query(knje061bQuery::setInsertStudyrecRemarkQuery($opt, $model));
                $tmp_remark = $row["REMARK"];
            //echo $opt["SCHREGNO"] .$opt["SUBCLASSCD"] .$opt["REMARK"] .strlen($opt["REMARK"]) ."<BR>";
//var_dump($opt);
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
            $db->query(knje061bQuery::setInsertStudyrecRemarkQuery($opt, $model));
            //echo $opt["SCHREGNO"] .$opt["SUBCLASSCD"] .$opt["REMARK"] .strlen($opt["REMARK"]) ."<BR>";
//var_dump($opt);
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
    public function deleteStudyRecRemark2($data, $model)
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
    public function insertStudyRecRemark2($data, $model)
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

    /****************************/
    /* ↓↓↓ 観点データ ↓↓↓ */
    /****************************/

    //生徒取得
    public function getStudent($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     ".$model->vSchregRegdDat." T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
//        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.GRADE = '".$model->annual."' ";
        if (strlen($model->hr_class)) {
            $query .= "     AND T1.HR_CLASS    = '".$model->hr_class."' ";
        }
        if (strlen($model->coursecode)) {
            $query .= "     AND T1.COURSECODE  = '".$model->coursecode."' ";
        }
        if (strlen($model->schregno)) {
            $query .= "     AND T1.SCHREGNO    = '".$model->schregno."' ";
        }

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
    public function delDataJview($db, $schregNo, $model)
    {
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_SUB_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '9' ";
        $query .= "     AND T1.SCHREGNO = '".$schregNo."' ";

        $db->query($query);
    }

    //追加
    public function insDataJview($db, $schregNo, $model)
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
        $query .= "             T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND T1.GRADE = '".$model->annual."' ";
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
        $query .= "             T1.YEAR = '".CTRL_YEAR."' ";
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
        $query .= "     '".CTRL_YEAR."' AS YEAR, ";
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
    public function insDataJviewMeiji($db, $schregNo, $model)
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
        $query .= "             T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND T1.GRADE = '".$model->annual."' ";
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
        $query .= "             AND I1.GRADE = '".$model->annual."' ";
        $query .= "             AND I1.SEMESTER = T1.SEMESTER ";
        $query .= "             AND I1.VIEWFLG = '1' ";
        $query .= "         LEFT JOIN NAME_MST L1 ";
        $query .= "              ON L1.NAMECD1 = 'D029' ";
        $query .= "             AND L1.ABBV1 = T1.STATUS ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR = '".CTRL_YEAR."' ";
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
        $query .= "             T1.YEAR = '".CTRL_YEAR."' ";
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
        $query .= "     '".CTRL_YEAR."' AS YEAR, ";
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
        $query .= "          ON T2.YEAR = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T2.CLASSCD = T1.STUDYREC_CLASSCD ";
            $query .= "     AND T2.SCHOOL_KIND = T1.STUDYREC_SCHOOL_KIND ";
            $query .= "     AND T2.CURRICULUM_CD = T1.STUDYREC_CURRICULUM_CD ";
        }
        $query .= "         AND T2.SUBCLASSCD = T1.STUDYREC_SUBCLASSCD ";
        $query .= "         AND T2.VIEWCD = T1.STUDYREC_VIEWCD ";
        $query .= "         AND T2.DIV = '3' ";
        $query .= "         AND T2.GRADE = '".$model->annual."' ";
        $query .= "         AND T1.TOTAL BETWEEN T2.ASSESSLOW AND T2.ASSESSHIGH ";
        $query .= "     LEFT JOIN NAME_MST L1 ";
        $query .= "          ON L1.NAMECD1 = 'D028' ";
        $query .= "         AND T2.ASSESSLEVEL = CAST(VALUE(L1.NAMESPARE2, '0') AS SMALLINT) ";
        //メイン1
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '".CTRL_YEAR."' AS YEAR, ";
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
        $query  = knje061bQuery::getStudyRecSql($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //新規作成
            if ($model->field["CREATEDIV"] == 1) {
                $query = knje061bQuery::deleteStudyRecSql($row, $model);
                $db->query($query);
                $query = knje061bQuery::insertStudyRecSql($row, $model);
                $db->query($query);
            //追加作成
            } elseif ($model->field["CREATEDIV"] == 2) {
                $query = knje061bQuery::existsStudyRecSql($row, $model);
                if ($db->getOne($query) == 0) {
                    $query = knje061bQuery::insertStudyRecSql($row, $model);
                    $db->query($query);
                }
                //上書作成
            } else {
                $query = knje061bQuery::existsStudyRecSql($row, $model);
                if ($db->getOne($query) == 0) {
                    $query = knje061bQuery::insertStudyRecSql($row, $model);
                    $db->query($query);
                } else {
                    $query = knje061bQuery::updateStudyRecSql($row, $model);
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
            $query = knje061bQuery::deleteStudyrecRemarkQualifiedSql($model);
            $db->query($query);
        }

        $query  = "";
        //クエリー
        $query  = knje061bQuery::getStudyrecRemarkQualifiedSql($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //新規作成
            if ($model->field["CREATEDIV"] == 1) {
                $query = knje061bQuery::insertStudyrecRemarkQualifiedSql($row, $model);
                $db->query($query);
            //追加作成
            } elseif ($model->field["CREATEDIV"] == 2) {
                $query = knje061bQuery::existsStudyrecRemarkQualifiedSql($row);
                if ($db->getOne($query) == 0) {
                    $query = knje061bQuery::insertStudyrecRemarkQualifiedSql($row, $model);
                    $db->query($query);
                }
                //上書作成
            } else {
                $query = knje061bQuery::existsStudyrecRemarkQualifiedSql($row);
                if ($db->getOne($query) == 0) {
                    $query = knje061bQuery::insertStudyrecRemarkQualifiedSql($row, $model);
                    $db->query($query);
                } else {
                    $query = knje061bQuery::updateStudyrecRemarkQualifiedSql($row, $model);
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
            $query = knje061bQuery::deleteStudyRecProvFlgSql($model);
            $db->query($query);
        }

        $query  = "";
        //クエリー
        $query  = knje061bQuery::getStudyRecProvFlgSql($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //新規作成
            if ($model->field["CREATEDIV"] == 1) {
                $query = knje061bQuery::insertStudyRecProvFlgSql($row, $model);
                $db->query($query);
            //追加作成
            } elseif ($model->field["CREATEDIV"] == 2) {
                $query = knje061bQuery::existsStudyRecProvFlgSql($row, $model);
                if ($db->getOne($query) == 0) {
                    $query = knje061bQuery::insertStudyRecProvFlgSql($row, $model);
                    $db->query($query);
                }
                //上書作成
            } else {
                $query = knje061bQuery::existsStudyRecProvFlgSql($row, $model);
                if ($db->getOne($query) == 0) {
                    $query = knje061bQuery::insertStudyRecProvFlgSql($row, $model);
                    $db->query($query);
                } else {
                    $query = knje061bQuery::updateStudyRecProvFlgSql($row, $model);
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
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $query = Query::insertSQL($data, "STUDYREC_PROV_FLG_DAT");
        return $query;
    }

    public function updateStudyRecProvFlgSql($row, $model)
    {
        $data = array();
        $data["PROV_FLG"][TEXT]         = $row["PROV_FLG"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $where  = " WHERE SCHOOLCD      = '".$row["SCHOOLCD"]."' ";
        $where .= "   AND YEAR          = '".$row["YEAR"]."' ";
        $where .= "   AND SCHREGNO      = '".$row["SCHREGNO"]."' ";
        $where .= "   AND CLASSCD       = '".$row["CLASSCD"]."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   AND SCHOOL_KIND    = '".$row["SCHOOL_KIND"]."' ";
            $query .= "   AND CURRICULUM_CD  = '".$row["CURRICULUM_CD"]."' ";
        }
        $where .= "   AND SUBCLASSCD    = '".$row["SUBCLASSCD"]."' ";

        $query = Query::updateSQL($data, "STUDYREC_PROV_FLG_DAT", $where);
        return $query;
    }

    public function deleteStudyRecProvFlgSql($model)
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
        $query .= "     T1.PROV_FLG ";
        $query .= " FROM ";
        $query .= "     RECORD_PROV_FLG_DAT T1 ";
        $query .= " WHERE T1.YEAR       ".$operator." '".CTRL_YEAR."' ";
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
}
