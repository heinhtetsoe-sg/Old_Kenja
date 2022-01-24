<?php

require_once('for_php7.php');

class knjh340aQuery extends Query {
    //リスト内情報取得
    function getListdata($model) {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.MOCKCD, ";
        $query .= "     T1.COURSE_DIV, ";
        $query .= "     T1.MOCK_SUBCLASS_CD, ";
        $query .= "     T1.DIV, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.PERFECT, ";
        $query .= "     T1.PASS_SCORE, ";
        $query .= "     L1.SUBCLASS_NAME, ";
        $query .= "     L2.COURSENAME, ";
        $query .= "     L3.MAJORNAME, ";
        $query .= "     L4.COURSECODENAME, ";
        $query .= "     L5.NAME1 AS COURSE_DIV_NAME ";
        $query .= " FROM ";
        $query .= "     MOCK_PERFECT_COURSE_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     MOCK_SUBCLASS_MST L1 ON L1.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD ";
        $query .= " LEFT JOIN ";
        $query .= "     COURSE_MST L2 ON L2.COURSECD = T1.COURSECD ";
        $query .= " LEFT JOIN ";
        $query .= "     MAJOR_MST L3 ON  L3.COURSECD = T1.COURSECD ";
        $query .= "                  AND L3.MAJORCD  = T1.MAJORCD ";
        $query .= " LEFT JOIN ";
        $query .= "     COURSECODE_MST L4 ON L4.COURSECODE = T1.COURSECODE ";
        $query .= " LEFT JOIN ";
        $query .= "     V_NAME_MST L5 ON  L5.YEAR = T1.YEAR ";
        $query .= "                   AND L5.NAMECD1 = 'H309' ";
        $query .= "                   AND L5.NAMECD2 = T1.COURSE_DIV ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR   = '".CTRL_YEAR."' AND ";
        $query .= "     T1.MOCKCD = '{$model->field["MOCKCD"]}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.MOCKCD, ";
        $query .= "     T1.COURSE_DIV, ";
        $query .= "     T1.MOCK_SUBCLASS_CD, ";
        $query .= "     T1.DIV, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.PERFECT, ";
        $query .= "     T1.PASS_SCORE ";

        return $query;
    }

    //模試コード年間平均
    function getMockAndAvg() {
        $query  = " WITH MOCK_AND_AVG AS ( ";
        $query .= " SELECT ";
        $query .= "     MOCKCD, ";
        $query .= "     MOCKNAME1 ";
        $query .= " FROM ";
        $query .= "     MOCK_MST ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     SUBSTR(T1.MOCKCD,1,1) || '99999999' AS MOCKCD, ";
        $query .= "     L1.NAME1 || '年間平均' AS MOCKNAME1 ";
        $query .= " FROM ";
        $query .= "     MOCK_MST T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'H308' AND L1.NAMECD2 = SUBSTR(T1.MOCKCD,1,1) ";
        $query .= " GROUP BY ";
        $query .= "     SUBSTR(T1.MOCKCD,1,1), ";
        $query .= "     L1.NAME1 ";
        $query .= " ) ";

        return $query;
    }

    //模試コードコンボ
    function getMockcd() {
        $query  = knjh340aQuery::getMockAndAvg();
        $query .= " SELECT ";
        $query .= "     MOCKCD AS VALUE, ";
        $query .= "     MOCKCD || ' ' || MOCKNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     MOCK_AND_AVG ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //模試名取得
    function getMockName($model) {
        $query  = knjh340aQuery::getMockAndAvg();
        $query .= " SELECT ";
        $query .= "     MOCKNAME1 ";
        $query .= " FROM ";
        $query .= "     MOCK_AND_AVG ";
        $query .= " WHERE ";
        $query .= "     MOCKCD = '{$model->mockcd}' ";

        return $query;
    }

    function getCourseDiv() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
        $query .= "     NAMECD1 = 'H309' ";

        return $query;
    }

    //科目コンボ
    function getMockSubclassCd() {
        $query  = " SELECT ";
        $query .= "     MOCK_SUBCLASS_CD AS VALUE, ";
        $query .= "     MOCK_SUBCLASS_CD || ' ' || SUBCLASS_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     MOCK_SUBCLASS_MST ";
        $query .= " ORDER BY ";
        $query .= "     MOCK_SUBCLASS_CD ";

        return $query;
    }

    //学年コンボ
    function getGrade() {
        $query  = " SELECT ";
        $query .= "     REGD_G.GRADE_NAME1 AS LABEL, ";
        $query .= "     REGD_H.GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT REGD_H ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT REGD_G ON  REGD_G.YEAR  = REGD_H.YEAR ";
        $query .= "                                        AND REGD_G.GRADE = REGD_H.GRADE ";
        $query .= " WHERE ";
        $query .= "     REGD_H.YEAR = '".CTRL_YEAR."' ";
        $query .= " GROUP BY ";
        $query .= "     REGD_H.GRADE, ";
        $query .= "     REGD_G.GRADE_NAME1 ";

        return $query;
    }

    //課程コンボ
    function getCourse() {
        $query  = " SELECT ";
        $query .= "     T1.COURSECD || ' ' || L1.COURSENAME AS LABEL, ";
        $query .= "     T1.COURSECD AS VALUE ";
        $query .= " FROM ";
        $query .= "     COURSE_YDAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     COURSE_MST L1 ON L1.COURSECD = T1.COURSECD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //学科コンボ
    function getMajor($coursece) {
        $query  = " SELECT ";
        $query .= "     T1.COURSECD || T1.MAJORCD || ' ' || L1.MAJORNAME AS LABEL, ";
        $query .= "     T1.MAJORCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     MAJOR_YDAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     MAJOR_MST L1 ON L1.COURSECD = T1.COURSECD ";
        $query .= "                 AND L1.MAJORCD  = T1.MAJORCD ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.COURSECD = '{$coursece}' ";

        return $query;
    }

    //コースコンボ
    function getCoursecode() {
        $query  = " SELECT ";
        $query .= "     T1.COURSECODE || ' ' || L1.COURSECODENAME AS LABEL, ";
        $query .= "     T1.COURSECODE AS VALUE ";
        $query .= " FROM ";
        $query .= "     COURSECODE_YDAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     COURSECODE_MST L1 ON L1.COURSECODE = T1.COURSECODE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //前年度からのコピーの件数カウント
    function getCopyCountQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "      COUNT(*) ";
        $query .= "  FROM ";
        $query .= "      MOCK_PERFECT_COURSE_DAT T0 ";
        $query .= "  WHERE YEAR = '" .(CTRL_YEAR-1) ."' AND ";
        $query .= "     not exists ( "; //今年度にない年度が対象
        $query .= "                 SELECT 'x' FROM MOCK_PERFECT_COURSE_DAT T1 ";
        $query .= "                 WHERE ";
        $query .= "                 T1.YEAR             = '".CTRL_YEAR."' AND ";
        $query .= "                 T1.MOCKCD           = T0.MOCKCD AND ";
        $query .= "                 T1.COURSE_DIV       = T0.COURSE_DIV AND ";
        $query .= "                 T1.MOCK_SUBCLASS_CD = T0.MOCK_SUBCLASS_CD AND ";
        $query .= "                 T1.DIV              = T0.DIV AND ";
        $query .= "                 T1.GRADE            = T0.GRADE AND ";
        $query .= "                 T1.COURSECD         = T0.COURSECD AND ";
        $query .= "                 T1.MAJORCD          = T0.MAJORCD AND ";
        $query .= "                 T1.COURSECODE       = T0.COURSECODE ";
        $query .= "                 )  AND ";
        $query .= "    (( ";
/******************************************************************/
/* 今年度にない教科や課程、学科、コース、学年のものはコピーしない */
/******************************************************************/
        $query .= "     EXISTS (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 COURSE_YDAT T2 ";
        $query .= "             WHERE ";
        $query .= "                 T2.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "                 T2.COURSECD = T0.COURSECD ";
        $query .= "             ) AND ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 MAJOR_YDAT T3 ";
        $query .= "             WHERE ";
        $query .= "                 T3.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "                 T3.COURSECD = T0.COURSECD AND ";
        $query .= "                 T3.MAJORCD  = T0.MAJORCD ";
        $query .= "             ) AND ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 COURSECODE_YDAT T4 ";
        $query .= "             WHERE ";
        $query .= "                 T4.YEAR       = '".CTRL_YEAR."' AND ";
        $query .= "                 T4.COURSECODE = T0.COURSECODE ";
        $query .= "             ) ";
        $query .= "     ) OR T0.COURSECD = '0' ) ";
        $query .= "     AND ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 MOCK_SUBCLASS_MST T5 ";
        $query .= "             WHERE ";
        $query .= "                 T5.MOCK_SUBCLASS_CD = T0.MOCK_SUBCLASS_CD ";
        $query .= "             ) ";
        $query .= "     AND ";
        $query .= "     (EXISTS (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 SCHREG_REGD_HDAT T6 ";
        $query .= "             WHERE ";
        $query .= "                 T6.YEAR  = '".CTRL_YEAR."' AND ";
        $query .= "                 T6.GRADE = T0.GRADE ";
        $query .= "             ) ";
        $query .= "      OR T0.GRADE = '00') ";
        $query .= "     AND ";
/*********************************************************************/
/* 今年度にすでに設定されている範囲のものはコピーしない              */
/* たとえば区分1がすでに存在していたら重複ではなくても区分2も区分3も */
/* 区分1とかぶってしまうためコピー対象ではない。                     */
/*********************************************************************/
        $query .= "     ((T0.DIV = '01' AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     * ";
        $query .= "                 FROM ";
        $query .= "                     MOCK_PERFECT_COURSE_DAT T7 ";
        $query .= "                 WHERE ";
        $query .= "                     T7.YEAR             = '".CTRL_YEAR."' AND ";
        $query .= "                     T7.MOCKCD           = T0.MOCKCD AND ";
        $query .= "                     T7.COURSE_DIV       = T0.COURSE_DIV AND ";
        $query .= "                     T7.MOCK_SUBCLASS_CD = T0.MOCK_SUBCLASS_CD ";
        $query .= "                 )) ";
        $query .= "     OR ";
        $query .= "     (T0.DIV = '02' AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     * ";
        $query .= "                 FROM ";
        $query .= "                     MOCK_PERFECT_COURSE_DAT T8 ";
        $query .= "                 WHERE ";
        $query .= "                     T8.YEAR             = '".CTRL_YEAR."' AND ";
        $query .= "                     T8.MOCKCD           = T0.MOCKCD AND ";
        $query .= "                     T8.COURSE_DIV       = T0.COURSE_DIV AND ";
        $query .= "                     T8.MOCK_SUBCLASS_CD = T0.MOCK_SUBCLASS_CD AND ";
        $query .= "                     (T8.GRADE           = T0.GRADE OR ";
        $query .= "                     T8.GRADE            = '00') ";
        $query .= "                 )) ";
        $query .= "     OR ";
        $query .= "     (T0.DIV = '03' AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     * ";
        $query .= "                 FROM ";
        $query .= "                     MOCK_PERFECT_COURSE_DAT T8 ";
        $query .= "                 WHERE ";
        $query .= "                     T8.YEAR             = '".CTRL_YEAR."' AND ";
        $query .= "                     T8.MOCKCD           = T0.MOCKCD AND ";
        $query .= "                     T8.COURSE_DIV       = T0.COURSE_DIV AND ";
        $query .= "                     T8.MOCK_SUBCLASS_CD = T0.MOCK_SUBCLASS_CD AND ";
        $query .= "                     (T8.GRADE           = T0.GRADE OR ";
        $query .= "                     T8.GRADE            = '00') AND ";
        $query .= "                     ((T8.COURSECD       = T0.COURSECD AND ";
        $query .= "                     T8.MAJORCD          = T0.MAJORCD AND ";
        $query .= "                     T8.COURSECODE       = T0.COURSECODE) OR ";
        $query .= "                     T8.COURSECD         = '0') ";
        $query .= "                 ))) ";

        $cnt = $db->getOne($query);
        Query::dbCheckIn($db);
        return $cnt;
    }

    //前年度からのコピー
    function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO MOCK_PERFECT_COURSE_DAT( ";
        $query .= "     YEAR, ";
        $query .= "     MOCKCD, ";
        $query .= "     COURSE_DIV, ";
        $query .= "     MOCK_SUBCLASS_CD, ";
        $query .= "     DIV, ";
        $query .= "     GRADE, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     COURSECODE, ";
        $query .= "     PERFECT, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " (SELECT ";
        $query .= "      '" . CTRL_YEAR ."', ";
        $query .= "      MOCKCD, ";
        $query .= "      COURSE_DIV, ";
        $query .= "      MOCK_SUBCLASS_CD, ";
        $query .= "      DIV, ";
        $query .= "      GRADE, ";
        $query .= "      COURSECD, ";
        $query .= "      MAJORCD, ";
        $query .= "      COURSECODE, ";
        $query .= "      PERFECT, ";
        $query .= "      '".STAFFCD."', ";
        $query .= "      SYSDATE() ";
        $query .= "  FROM ";
        $query .= "      MOCK_PERFECT_COURSE_DAT T0 ";
        $query .= "  WHERE YEAR = '" .(CTRL_YEAR-1) ."' AND ";
        $query .= "     not exists ( "; //今年度にない年度が対象
        $query .= "                 SELECT 'x' FROM MOCK_PERFECT_COURSE_DAT T1 ";
        $query .= "                 WHERE ";
        $query .= "                 T1.YEAR             = '".CTRL_YEAR."' AND ";
        $query .= "                 T1.MOCKCD           = T0.MOCKCD AND ";
        $query .= "                 T1.COURSE_DIV       = T0.COURSE_DIV AND ";
        $query .= "                 T1.MOCK_SUBCLASS_CD = T0.MOCK_SUBCLASS_CD AND ";
        $query .= "                 T1.DIV              = T0.DIV AND ";
        $query .= "                 T1.GRADE            = T0.GRADE AND ";
        $query .= "                 T1.COURSECD         = T0.COURSECD AND ";
        $query .= "                 T1.MAJORCD          = T0.MAJORCD AND ";
        $query .= "                 T1.COURSECODE       = T0.COURSECODE ";
        $query .= "                 )  AND ";
        $query .= "    (( ";
/******************************************************************/
/* 今年度にない教科や課程、学科、コース、学年のものはコピーしない */
/******************************************************************/
        $query .= "     EXISTS (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 COURSE_YDAT T2 ";
        $query .= "             WHERE ";
        $query .= "                 T2.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "                 T2.COURSECD = T0.COURSECD ";
        $query .= "             ) AND ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 MAJOR_YDAT T3 ";
        $query .= "             WHERE ";
        $query .= "                 T3.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "                 T3.COURSECD = T0.COURSECD AND ";
        $query .= "                 T3.MAJORCD  = T0.MAJORCD ";
        $query .= "             ) AND ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 COURSECODE_YDAT T4 ";
        $query .= "             WHERE ";
        $query .= "                 T4.YEAR       = '".CTRL_YEAR."' AND ";
        $query .= "                 T4.COURSECODE = T0.COURSECODE ";
        $query .= "             ) ";
        $query .= "     ) OR T0.COURSECD = '0' ) ";
        $query .= "     AND ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 MOCK_SUBCLASS_MST T5 ";
        $query .= "             WHERE ";
        $query .= "                 T5.MOCK_SUBCLASS_CD = T0.MOCK_SUBCLASS_CD ";
        $query .= "             ) ";
        $query .= "     AND ";
        $query .= "     (EXISTS (SELECT ";
        $query .= "                 * ";
        $query .= "             FROM ";
        $query .= "                 SCHREG_REGD_HDAT T6 ";
        $query .= "             WHERE ";
        $query .= "                 T6.YEAR  = '".CTRL_YEAR."' AND ";
        $query .= "                 T6.GRADE = T0.GRADE ";
        $query .= "             ) ";
        $query .= "      OR T0.GRADE = '00') ";
        $query .= "     AND ";
/*********************************************************************/
/* 今年度にすでに設定されている範囲のものはコピーしない              */
/* たとえば区分1がすでに存在していたら重複ではなくても区分2も区分3も */
/* 区分1とかぶってしまうためコピー対象ではない。                     */
/*********************************************************************/
        $query .= "     ((T0.DIV = '01' AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     * ";
        $query .= "                 FROM ";
        $query .= "                     MOCK_PERFECT_COURSE_DAT T7 ";
        $query .= "                 WHERE ";
        $query .= "                     T7.YEAR             = '".CTRL_YEAR."' AND ";
        $query .= "                     T7.MOCKCD           = T0.MOCKCD AND ";
        $query .= "                     T7.COURSE_DIV       = T0.COURSE_DIV AND ";
        $query .= "                     T7.MOCK_SUBCLASS_CD = T0.MOCK_SUBCLASS_CD ";
        $query .= "                 )) ";
        $query .= "     OR ";
        $query .= "     (T0.DIV = '02' AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     * ";
        $query .= "                 FROM ";
        $query .= "                     MOCK_PERFECT_COURSE_DAT T8 ";
        $query .= "                 WHERE ";
        $query .= "                     T8.YEAR             = '".CTRL_YEAR."' AND ";
        $query .= "                     T8.MOCKCD           = T0.MOCKCD AND ";
        $query .= "                     T8.COURSE_DIV       = T0.COURSE_DIV AND ";
        $query .= "                     T8.MOCK_SUBCLASS_CD = T0.MOCK_SUBCLASS_CD AND ";
        $query .= "                     (T8.GRADE           = T0.GRADE OR ";
        $query .= "                     T8.GRADE            = '00') ";
        $query .= "                 )) ";
        $query .= "     OR ";
        $query .= "     (T0.DIV = '03' AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     * ";
        $query .= "                 FROM ";
        $query .= "                     MOCK_PERFECT_COURSE_DAT T8 ";
        $query .= "                 WHERE ";
        $query .= "                     T8.YEAR             = '".CTRL_YEAR."' AND ";
        $query .= "                     T8.MOCKCD           = T0.MOCKCD AND ";
        $query .= "                     T8.COURSE_DIV       = T0.COURSE_DIV AND ";
        $query .= "                     T8.MOCK_SUBCLASS_CD = T0.MOCK_SUBCLASS_CD AND ";
        $query .= "                     (T8.GRADE           = T0.GRADE OR ";
        $query .= "                     T8.GRADE            = '00') AND ";
        $query .= "                     ((T8.COURSECD       = T0.COURSECD AND ";
        $query .= "                     T8.MAJORCD          = T0.MAJORCD AND ";
        $query .= "                     T8.COURSECODE       = T0.COURSECODE) OR ";
        $query .= "                     T8.COURSECD         = '0') ";
        $query .= "                 ))) ";
        $query .= " ) ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return true;
   }

    //存在チェック(区分1)
    function getExistsDiv1($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*)  ";
        $query .= " FROM ";
        $query .= "     MOCK_PERFECT_COURSE_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR             = '".CTRL_YEAR."' ";
        $query .= "     AND MOCKCD           = '{$model->field["MOCKCD"]}' ";
        $query .= "     AND COURSE_DIV       = '{$model->field["COURSE_DIV"]}' ";
        $query .= "     AND MOCK_SUBCLASS_CD = '{$model->field["MOCK_SUBCLASS_CD"]}' ";

        return $query;
    }

    //存在チェック(区分2)
    function getExistsDiv2($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*)  ";
        $query .= " FROM ";
        $query .= "     MOCK_PERFECT_COURSE_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR             = '".CTRL_YEAR."' ";
        $query .= "     AND MOCKCD           = '{$model->field["MOCKCD"]}' ";
        $query .= "     AND COURSE_DIV       = '{$model->field["COURSE_DIV"]}' ";
        $query .= "     AND MOCK_SUBCLASS_CD = '{$model->field["MOCK_SUBCLASS_CD"]}' ";
        $query .= "     AND (GRADE           = '{$model->field["GRADE"]}' ";
        $query .= "     OR   GRADE           = '00') ";

        return $query;
    }

    //存在チェック(区分3)
    function getExistsDiv3($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*)  ";
        $query .= " FROM ";
        $query .= "     MOCK_PERFECT_COURSE_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR             = '".CTRL_YEAR."' ";
        $query .= "     AND MOCKCD           = '{$model->field["MOCKCD"]}' ";
        $query .= "     AND COURSE_DIV       = '{$model->field["COURSE_DIV"]}' ";
        $query .= "     AND MOCK_SUBCLASS_CD = '{$model->field["MOCK_SUBCLASS_CD"]}' ";
        $query .= "     AND (GRADE           = '{$model->field["GRADE"]}' ";
        $query .= "     OR  GRADE            = '00') ";
        $query .= "     AND ((COURSECD       = '{$model->field["COURSECD"]}' ";
        $query .= "     AND MAJORCD          = '{$model->field["MAJORCD"]}' ";
        $query .= "     AND COURSECODE       = '{$model->field["COURSECODE"]}') ";
        $query .= "     OR  COURSECD         = '0') ";

        return $query;
    }

    //存在チェック
    function getExists($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*)  ";
        $query .= " FROM ";
        $query .= "     MOCK_PERFECT_COURSE_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR             = '".CTRL_YEAR."' ";
        $query .= "     AND MOCKCD           = '{$model->field["MOCKCD"]}' ";
        $query .= "     AND COURSE_DIV       = '{$model->field["COURSE_DIV"]}' ";
        $query .= "     AND MOCK_SUBCLASS_CD = '{$model->field["MOCK_SUBCLASS_CD"]}' ";
        $query .= "     AND DIV              = '{$model->field["DIV"]}' ";
        $query .= "     AND GRADE            = '{$model->field["GRADE"]}' ";
        $query .= "     AND COURSECD         = '{$model->field["COURSECD"]}' ";
        $query .= "     AND MAJORCD          = '{$model->field["MAJORCD"]}' ";
        $query .= "     AND COURSECODE       = '{$model->field["COURSECODE"]}' ";

        return $query;
    }

    // INSERT&UPDATE 一度DELETEしてINSERTする。
    function &getInsertUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     MOCK_PERFECT_COURSE_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR             = '".CTRL_YEAR."' ";
        $query .= "     AND MOCKCD           = '{$model->field["MOCKCD"]}' ";
        $query .= "     AND COURSE_DIV       = '{$model->field["COURSE_DIV"]}' ";
        $query .= "     AND MOCK_SUBCLASS_CD = '{$model->field["MOCK_SUBCLASS_CD"]}' ";
        $query .= "     AND DIV              = '{$model->field["DIV"]}' ";
        $query .= "     AND GRADE            = '{$model->field["GRADE"]}' ";
        $query .= "     AND COURSECD         = '{$model->field["COURSECD"]}' ";
        $query .= "     AND MAJORCD          = '{$model->field["MAJORCD"]}' ";
        $query .= "     AND COURSECODE       = '{$model->field["COURSECODE"]}' ";

        $db->query($query);

        $query = "";
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["MOCKCD"][TEXT]           = $model->field["MOCKCD"];
        $data["COURSE_DIV"][TEXT]       = $model->field["COURSE_DIV"];
        $data["MOCK_SUBCLASS_CD"][TEXT] = $model->field["MOCK_SUBCLASS_CD"];
        $data["DIV"][TEXT]              = $model->field["DIV"];
        $data["GRADE"][TEXT]            = $model->field["GRADE"];
        $data["COURSECD"][TEXT]         = $model->field["COURSECD"];
        $data["MAJORCD"][TEXT]          = $model->field["MAJORCD"];
        $data["COURSECODE"][TEXT]       = $model->field["COURSECODE"];

        $data["PERFECT"][NUMBER]        = $model->field["PERFECT"];
        $data["PASS_SCORE"][NUMBER]     = $model->field["PASS_SCORE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "SYSDATE()";

        $query = Query::insertSQL($data, "MOCK_PERFECT_COURSE_DAT");

        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    // DELETE
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     MOCK_PERFECT_COURSE_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR             = '".CTRL_YEAR."' ";
        $query .= "     AND MOCKCD           = '{$model->field["MOCKCD"]}' ";
        $query .= "     AND COURSE_DIV       = '{$model->field["COURSE_DIV"]}' ";
        $query .= "     AND MOCK_SUBCLASS_CD = '{$model->field["MOCK_SUBCLASS_CD"]}' ";
        $query .= "     AND DIV              = '{$model->field["DIV"]}' ";
        $query .= "     AND GRADE            = '{$model->field["GRADE"]}' ";
        $query .= "     AND COURSECD         = '{$model->field["COURSECD"]}' ";
        $query .= "     AND MAJORCD          = '{$model->field["MAJORCD"]}' ";
        $query .= "     AND COURSECODE       = '{$model->field["COURSECODE"]}' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }

    //コピー元をカウント
    function &getFromCopyCountQuery($model, $flg) {
        $db = Query::dbCheckOut();
    
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MOCK_PERFECT_COURSE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($flg == "to") {
            $query .= "     AND T1.MOCKCD = '{$model->field["MOCKCD"]}' ";
        } else {
            $query .= "     AND T1.MOCKCD = '{$model->field["MOCKCD_FROMCOPY"]}' ";
        }
        $checkcnt = $db->getOne($query);
        Query::dbCheckIn($db);
        return $checkcnt;
    }

    //実力テストコピー
    function fromCopyInsertCopy($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
    
        $query  = " INSERT INTO MOCK_PERFECT_COURSE_DAT ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     '{$model->field["MOCKCD"]}' AS MOCKCD, ";
        $query .= "     T1.COURSE_DIV, ";
        $query .= "     T1.MOCK_SUBCLASS_CD, ";
        $query .= "     T1.DIV, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.PERFECT, ";
        $query .= "     T1.PASS_SCORE, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     MOCK_PERFECT_COURSE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.MOCKCD = '{$model->field["MOCKCD_FROMCOPY"]}' ";
        $db->query($query);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }
}
?>
