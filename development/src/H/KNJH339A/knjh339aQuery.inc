<?php

require_once('for_php7.php');

class knjh339aquery extends Query {

    //模試種別
    function getMockDiv()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'H308' ";
        $query .= "     AND NAMESPARE1 = '1' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //模試種別
    function getMockMst($model)
    {
        $query  = " SELECT ";
        $query .= "     MOCKCD AS VALUE, ";
        $query .= "     MOCKCD || ':' || MOCKNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     MOCK_MST ";
        $query .= " WHERE ";
        $query .= "     substr(MOCKCD, 1, 1) = '".$model->data_div."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //模試データ更新
    function UpdateMockDat($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " WITH T_1 AS  ( ";
        $query .= " SELECT ";
        $query .= "     LT1.MOCKCD, ";
        $query .= "     LT1.SCHREGNO, ";
        $query .= "     LT1.MOCK_SUBCLASS_CD, ";
        $query .= "     LL1.GRADE, ";
        $query .= "     CASE WHEN LT1.SCORE_DI = '*' ";
        $query .= "          THEN 1 ";
        $query .= "          ELSE 0 ";
        $query .= "     END AS SCORE_DIS, ";
        $query .= "     LT1.SCORE ";
        $query .= " FROM ";
        $query .= "     MOCK_DAT LT1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT LL1 ON LL1.YEAR = LT1.YEAR ";
        $query .= "         AND LL1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "         AND LL1.SCHREGNO = LT1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     LT1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND LT1.MOCKCD = '".$model->mockcd."' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     U1.MOCKCD, ";
        $query .= "     U1.SCHREGNO, ";
        $query .= "     U1.MOCK_SUBCLASS_CD, ";
        $query .= "     U1.GRADE, ";
        $query .= "     U1.SCORE, ";
        $query .= "     RANK() OVER(PARTITION BY U1.GRADE, U1.MOCKCD, U1.MOCK_SUBCLASS_CD ORDER BY CASE WHEN U1.SCORE_DIS > 0 THEN -1 ELSE VALUE(U1.SCORE, -1) END DESC) AS RANK, ";
        $query .= "     CASE WHEN 0 < UL1.STDDEV then DECIMAL(ROUND((10*(U1.SCORE - UL1.AVG) / UL1.STDDEV + 50) * 10, 0) / 10, 5, 1) END AS DEVIATION ";
        $query .= " FROM ";
        $query .= "     T_1 U1 ";
        $query .= "     LEFT JOIN (SELECT ";
        $query .= "                    LT1.MOCKCD, ";
        $query .= "                    LT1.MOCK_SUBCLASS_CD, ";
        $query .= "                    LT1.GRADE, ";
        $query .= "                    DECIMAL(ROUND(AVG(FLOAT(LT1.SCORE))*10,0)/10,5,1) AS AVG, ";
        $query .= "                    DECIMAL(ROUND(STDDEV(FLOAT(LT1.SCORE))*10,0)/10,5,1) AS STDDEV ";
        $query .= "                FROM ";
        $query .= "                    T_1 LT1 ";
        $query .= "                GROUP BY ";
        $query .= "                    LT1.MOCKCD, ";
        $query .= "                    LT1.MOCK_SUBCLASS_CD, ";
        $query .= "                    LT1.GRADE ";
        $query .= "     ) UL1 ON UL1.MOCKCD = U1.MOCKCD ";
        $query .= "          AND UL1.MOCK_SUBCLASS_CD = U1.MOCK_SUBCLASS_CD ";
        $query .= "          AND UL1.GRADE = U1.GRADE ";
        $query .= " ORDER BY ";
        $query .= "     U1.GRADE, ";
        $query .= "     U1.MOCKCD, ";
        $query .= "     U1.MOCK_SUBCLASS_CD,U1.SCORE DESC ";

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["RANK"][NUMBER]      = (!strlen($row["SCORE"])) ? "" : $row["RANK"];
            $data["DEVIATION"][NUMBER] = (!strlen($row["SCORE"])) ? "" : $row["DEVIATION"];
            $data["REGISTERCD"][TEXT]  = STAFFCD;
            $data["UPDATED"][FUNC]     = "SYSDATE()";

            $where  = " WHERE ";
            $where .= "     YEAR = '".CTRL_YEAR."' ";
            $where .= "     AND MOCKCD = '".$row["MOCKCD"]."' ";
            $where .= "     AND SCHREGNO = '".$row["SCHREGNO"]."' ";
            $where .= "     AND MOCK_SUBCLASS_CD = '".$row["MOCK_SUBCLASS_CD"]."' ";

            $query = Query::updateSQL($data, "MOCK_DAT", $where);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    //模試席次更新
    function UpdateMockRankDat($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $delQuery = "DELETE FROM MOCK_RANK_DAT WHERE YEAR = '".CTRL_YEAR."' AND MOCKCD = '".$model->mockcd."'";
        $db->query($delQuery);

        $query = knjh339aQuery::getMockRankDat($model, "", "", "TUUJOU_KAMOKU");

        knjh339aQuery::insertMockRankDat($db, $query, $model, "TUUJOU_KAMOKU");
        $db->commit();

        if ($model->Properties["deviationAvgRank"] == "1") {
            $query = knjh339aQuery::getMockRankDatDeviation($model, "", "999999", "TUUJOU_KAMOKU");

            knjh339aQuery::insertMockRankDatDeviation($db, $query, $model, "TUUJOU_KAMOKU");
            $db->commit();
        }

        $groupMstQuery  = "SELECT DISTINCT ";
        $groupMstQuery .= "  GROUP_DIV ";
        $groupMstQuery .= "FROM ";
        $groupMstQuery .= "  MOCK_SUBCLASS_GROUP_MST ";
        $groupMstQuery .= "WHERE ";
        $groupMstQuery .= "  YEAR = '".CTRL_YEAR."' ";
        $groupMstQuery .= "  AND MOCKCD = '".$model->mockcd."' ";

        $result = $db->query($groupMstQuery);

        while ($groupMst = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            $groupSubclass = "";
            for ($i = 0; $i < 6; $i++) {
                $groupSubclass .= $groupMst["GROUP_DIV"];
            }

            $query = knjh339aQuery::getMockRankDat($model, $groupMst, $groupSubclass, "GASSAN_KAMOKU");

            knjh339aQuery::insertMockRankDat($db, $query, $model, "GASSAN_KAMOKU");

            if ($model->Properties["deviationAvgRank"] == "1") {
                $query = knjh339aQuery::getMockRankDatDeviation($model, $groupMst, $groupSubclass, "GASSAN_KAMOKU");

                knjh339aQuery::insertMockRankDatDeviation($db, $query, $model, "GASSAN_KAMOKU");
                $db->commit();
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    function getMockRankDat($model, $groupMst, $groupSubclass, $div)
    {
        $query  = " WITH MOCT_T AS (";
        if ($div == "TUUJOU_KAMOKU") {
            $query .= " SELECT ";
            $query .= "     LT1.MOCKCD, ";
            $query .= "     '999999' AS MOCK_SUBCLASS_CD, ";
            $query .= "     LT1.SCHREGNO, ";
            $query .= "     LL1.GRADE, ";
            $query .= "     LL1.HR_CLASS, ";
            $query .= "     LL1.COURSECD || LL1.MAJORCD || LL1.COURSECODE AS COURSE, ";
            $query .= "     SUM (CASE WHEN LT1.SCORE_DI = '*' ";
            $query .= "               THEN 1 ";
            $query .= "               ELSE 0 ";
            $query .= "          END) ";
            $query .= "     AS SCORE_DIS, ";
            $query .= "     SUM(LT1.SCORE) AS SCORE, ";
            $query .= "     DECIMAL(ROUND(AVG(FLOAT(LT1.SCORE))*100000,0)/100000,9,5) AS AVG, ";
            $query .= "     DECIMAL(ROUND(((FLOAT(SUM(LT1.SCORE))/FLOAT(SUM(VALUE(LL2.PERFECT, 100))))*100)*100000,0)/100000,9,5) AS AVG2 ";
            $query .= " FROM ";
            $query .= "     MOCK_DAT LT1 ";
            $query .= "     INNER JOIN SCHREG_REGD_DAT LL1 ON LL1.YEAR = LT1.YEAR ";
            $query .= "         AND LL1.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "         AND LL1.SCHREGNO = LT1.SCHREGNO ";
            $query .= "     LEFT JOIN MOCK_PERFECT_DAT LL2 ON LL2.YEAR = LT1.YEAR ";
            $query .= "         AND LL2.COURSE_DIV = SUBSTR(LL1.COURSECODE, 4, 1) ";
            $query .= "         AND LL2.GRADE = LL1.GRADE ";
            $query .= "         AND LL2.MOCK_SUBCLASS_CD = LT1.MOCK_SUBCLASS_CD ";
            $query .= " WHERE ";
            $query .= "     LT1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND LT1.MOCKCD = '".$model->mockcd."' ";
            $query .= "     AND (LT1.SCORE IS NOT NULL OR (LT1.SCORE IS NULL AND LT1.SCORE_DI = '*')) ";
            $query .= " GROUP BY ";
            $query .= "     LT1.MOCKCD, ";
            $query .= "     LT1.SCHREGNO, ";
            $query .= "     LL1.GRADE, ";
            $query .= "     LL1.HR_CLASS, ";
            $query .= "     LL1.COURSECD, ";
            $query .= "     LL1.MAJORCD, ";
            $query .= "     LL1.COURSECODE ";
            $query .= " UNION ALL ";
            $query .= " SELECT ";
            $query .= "     LT1.MOCKCD, ";
            $query .= "     LT1.MOCK_SUBCLASS_CD, ";
            $query .= "     LT1.SCHREGNO, ";
            $query .= "     LL1.GRADE, ";
            $query .= "     LL1.HR_CLASS, ";
            $query .= "     LL1.COURSECD || LL1.MAJORCD || LL1.COURSECODE AS COURSE, ";
            $query .= "     SUM (CASE WHEN LT1.SCORE_DI = '*' ";
            $query .= "               THEN 1 ";
            $query .= "               ELSE 0 ";
            $query .= "          END) ";
            $query .= "     AS SCORE_DIS, ";
            $query .= "     SUM(LT1.SCORE) AS SCORE, ";
            $query .= "     DECIMAL(ROUND(AVG(FLOAT(LT1.SCORE))*100000,0)/100000,9,5) AS AVG, ";
            $query .= "     DECIMAL(ROUND(((FLOAT(SUM(LT1.SCORE))/FLOAT(SUM(VALUE(LL2.PERFECT, 100))))*100)*100000,0)/100000,9,5) AS AVG2 ";
            $query .= " FROM ";
            $query .= "     MOCK_DAT LT1 ";
            $query .= "     INNER JOIN SCHREG_REGD_DAT LL1 ON LL1.YEAR = LT1.YEAR ";
            $query .= "         AND LL1.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "         AND LL1.SCHREGNO = LT1.SCHREGNO ";
            $query .= "     LEFT JOIN MOCK_PERFECT_DAT LL2 ON LL2.YEAR = LT1.YEAR ";
            $query .= "         AND LL2.COURSE_DIV = SUBSTR(LL1.COURSECODE, 4, 1) ";
            $query .= "         AND LL2.GRADE = LL1.GRADE ";
            $query .= "         AND LL2.MOCK_SUBCLASS_CD = LT1.MOCK_SUBCLASS_CD ";
            $query .= " WHERE ";
            $query .= "     LT1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND LT1.MOCKCD = '".$model->mockcd."' ";
            $query .= "     AND LT1.SCORE IS NOT NULL ";
            $query .= " GROUP BY ";
            $query .= "     LT1.MOCKCD, ";
            $query .= "     LT1.MOCK_SUBCLASS_CD, ";
            $query .= "     LT1.SCHREGNO, ";
            $query .= "     LL1.GRADE, ";
            $query .= "     LL1.HR_CLASS, ";
            $query .= "     LL1.COURSECD, ";
            $query .= "     LL1.MAJORCD, ";
            $query .= "     LL1.COURSECODE ";
        } else {
            $query .= " SELECT ";
            $query .= "     LT1.MOCKCD, ";
            $query .= "     '".$groupSubclass."' AS MOCK_SUBCLASS_CD, ";
            $query .= "     LT1.SCHREGNO, ";
            $query .= "     LL1.GRADE, ";
            $query .= "     LL1.HR_CLASS, ";
            $query .= "     LL1.COURSECD || LL1.MAJORCD || LL1.COURSECODE AS COURSE, ";
            $query .= "     SUM(LT1.SCORE) AS SCORE, ";
            $query .= "     DECIMAL(ROUND(AVG(FLOAT(LT1.SCORE))*100000,0)/100000,9,5) AS AVG, ";
            $query .= "     DECIMAL(ROUND(((FLOAT(SUM(LT1.SCORE))/FLOAT(SUM(VALUE(LL2.PERFECT, 100))))*100)*100000,0)/100000,9,5) AS AVG2 ";
            $query .= " FROM ";
            $query .= "     MOCK_DAT LT1 ";
            $query .= "     INNER JOIN SCHREG_REGD_DAT LL1 ON LL1.YEAR = LT1.YEAR ";
            $query .= "         AND LL1.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "         AND LL1.SCHREGNO = LT1.SCHREGNO ";
            $query .= "     LEFT JOIN MOCK_PERFECT_DAT LL2 ON LL2.YEAR = LT1.YEAR ";
            $query .= "         AND LL2.COURSE_DIV = SUBSTR(LL1.COURSECODE, 4, 1) ";
            $query .= "         AND LL2.GRADE = LL1.GRADE ";
            $query .= "         AND LL2.MOCK_SUBCLASS_CD = LT1.MOCK_SUBCLASS_CD, ";
            $query .= "     MOCK_SUBCLASS_GROUP_DAT L2 ";
            $query .= " WHERE ";
            $query .= "     LT1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND LT1.MOCKCD = '".$model->mockcd."' ";
            $query .= "     AND LT1.SCORE IS NOT NULL ";
            $query .= "     AND L2.YEAR = LT1.YEAR ";
            $query .= "     AND L2.MOCKCD = LT1.MOCKCD ";
            $query .= "     AND L2.GROUP_DIV = '".$groupMst["GROUP_DIV"]."' ";
            $query .= "     AND L2.GRADE = LL1.GRADE ";
            $query .= "     AND L2.COURSECD = LL1.COURSECD ";
            $query .= "     AND L2.MAJORCD = LL1.MAJORCD ";
            $query .= "     AND L2.COURSECODE = LL1.COURSECODE ";
            $query .= "     AND L2.MOCK_SUBCLASS_CD = LT1.MOCK_SUBCLASS_CD ";
            $query .= "     AND EXISTS( ";
            $query .= "         SELECT ";
            $query .= "             'x' ";
            $query .= "         FROM ";
            $query .= "            (SELECT ";
            $query .= "                 E1.SCHREGNO ";
            $query .= "             FROM ";
            $query .= "                (SELECT ";
            $query .= "                     ET1.MOCKCD, ";
            $query .= "                     EL3.CLASSCD, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                     EL3.SCHOOL_KIND, ";
                $query .= "                     EL3.CURRICULUM_CD, ";
            }
            $query .= "                     SUM (CASE WHEN ET1.SCORE_DI = '*' ";
            $query .= "                               THEN 1 ";
            $query .= "                               ELSE 0 ";
            $query .= "                          END) ";
            $query .= "                     AS SCORE_DIS, ";
            $query .= "                     ET1.SCHREGNO, ";
            $query .= "                     EL1.GRADE, ";
            $query .= "                     EL1.HR_CLASS, ";
            $query .= "                     EL1.COURSECD || EL1.MAJORCD || EL1.COURSECODE AS COURSE ";
            $query .= "                 FROM ";
            $query .= "                     MOCK_DAT ET1 ";
            $query .= "                     INNER JOIN SCHREG_REGD_DAT EL1 ON EL1.YEAR = ET1.YEAR ";
            $query .= "                         AND EL1.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "                         AND EL1.SCHREGNO = ET1.SCHREGNO ";
            $query .= "                     LEFT JOIN MOCK_PERFECT_DAT EL2 ON EL2.YEAR = ET1.YEAR ";
            $query .= "                         AND EL2.COURSE_DIV = SUBSTR(EL1.COURSECODE, 4, 1) ";
            $query .= "                         AND EL2.GRADE = EL1.GRADE ";
            $query .= "                         AND EL2.MOCK_SUBCLASS_CD = ET1.MOCK_SUBCLASS_CD ";
            $query .= "                     LEFT JOIN MOCK_SUBCLASS_MST EL3 ON EL3.MOCK_SUBCLASS_CD = ET1.MOCK_SUBCLASS_CD, ";
            $query .= "                     MOCK_SUBCLASS_GROUP_DAT L2 ";
            $query .= "                 WHERE ";
            $query .= "                     ET1.YEAR = '".CTRL_YEAR."' ";
            $query .= "                     AND ET1.MOCKCD = '".$model->mockcd."' ";
            $query .= "                     AND (ET1.SCORE IS NOT NULL OR (ET1.SCORE IS NULL AND ET1.SCORE_DI = '*')) ";
            $query .= "                     AND L2.YEAR = ET1.YEAR ";
            $query .= "                     AND L2.MOCKCD = ET1.MOCKCD ";
            $query .= "                     AND L2.GROUP_DIV = '".$groupMst["GROUP_DIV"]."' ";
            $query .= "                     AND L2.GRADE = EL1.GRADE ";
            $query .= "                     AND L2.COURSECD = EL1.COURSECD ";
            $query .= "                     AND L2.MAJORCD = EL1.MAJORCD ";
            $query .= "                     AND L2.COURSECODE = EL1.COURSECODE ";
            $query .= "                     AND L2.MOCK_SUBCLASS_CD = ET1.MOCK_SUBCLASS_CD ";
            $query .= "                 GROUP BY ";
            $query .= "                     ET1.MOCKCD, ";
            $query .= "                     EL3.CLASSCD, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                     EL3.SCHOOL_KIND, ";
                $query .= "                     EL3.CURRICULUM_CD, ";
            }
            $query .= "                     ET1.SCHREGNO, ";
            $query .= "                     EL1.GRADE, ";
            $query .= "                     EL1.HR_CLASS, ";
            $query .= "                     EL1.COURSECD, ";
            $query .= "                     EL1.MAJORCD, ";
            $query .= "                     EL1.COURSECODE ";
            $query .= "                 ) E1 ";
            $query .= "             WHERE ";
            $query .= "                 E1.SCORE_DIS = 0 ";
            $query .= "             GROUP BY ";
            $query .= "                 E1.SCHREGNO ";
            if ($groupMst["GROUP_DIV"] != 9 && $model->Properties["notGroupCnt"] != "1") {
                $query .= "             HAVING ";
                $query .= "                 COUNT(*) >= ".$groupMst["GROUP_DIV"]." ";
            }
            $query .= "             ) EE1 ";
            $query .= "         WHERE ";
            $query .= "             EE1.SCHREGNO = LT1.SCHREGNO ";
            $query .= "     ) ";
            $query .= " GROUP BY ";
            $query .= "     LT1.MOCKCD, ";
            $query .= "     LT1.SCHREGNO, ";
            $query .= "     LL1.GRADE, ";
            $query .= "     LL1.HR_CLASS, ";
            $query .= "     LL1.COURSECD, ";
            $query .= "     LL1.MAJORCD, ";
            $query .= "     LL1.COURSECODE ";
        }
        if ($div == "GASSAN_KAMOKU") {
            $query .= " ), SCORE_DI_T AS  ( ";
            $query .= " SELECT ";
            $query .= "     E1.SCHREGNO, ";
            $query .= "     SUM(E1.SCORE_DIS) AS SCORE_DIS ";
            $query .= " FROM ";
            $query .= "    (SELECT ";
            $query .= "         ET1.MOCKCD, ";
            $query .= "         EL3.CLASSCD, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         EL3.SCHOOL_KIND, ";
                $query .= "         EL3.CURRICULUM_CD, ";
            }
            $query .= "         SUM (CASE WHEN ET1.SCORE_DI = '*' ";
            $query .= "                   THEN 1 ";
            $query .= "                   ELSE 0 ";
            $query .= "              END) ";
            $query .= "         AS SCORE_DIS, ";
            $query .= "         ET1.SCHREGNO, ";
            $query .= "         EL1.GRADE, ";
            $query .= "         EL1.HR_CLASS, ";
            $query .= "         EL1.COURSECD || EL1.MAJORCD || EL1.COURSECODE AS COURSE ";
            $query .= "     FROM ";
            $query .= "         MOCK_DAT ET1 ";
            $query .= "         INNER JOIN SCHREG_REGD_DAT EL1 ON EL1.YEAR = ET1.YEAR ";
            $query .= "             AND EL1.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "             AND EL1.SCHREGNO = ET1.SCHREGNO ";
            $query .= "         LEFT JOIN MOCK_PERFECT_DAT EL2 ON EL2.YEAR = ET1.YEAR ";
            $query .= "             AND EL2.COURSE_DIV = SUBSTR(EL1.COURSECODE, 4, 1) ";
            $query .= "             AND EL2.GRADE = EL1.GRADE ";
            $query .= "             AND EL2.MOCK_SUBCLASS_CD = ET1.MOCK_SUBCLASS_CD ";
            $query .= "         LEFT JOIN MOCK_SUBCLASS_MST EL3 ON EL3.MOCK_SUBCLASS_CD = ET1.MOCK_SUBCLASS_CD, ";
            $query .= "         MOCK_SUBCLASS_GROUP_DAT L2 ";
            $query .= "     WHERE ";
            $query .= "         ET1.YEAR = '".CTRL_YEAR."' ";
            $query .= "         AND ET1.MOCKCD = '".$model->mockcd."' ";
            $query .= "         AND (ET1.SCORE IS NOT NULL OR (ET1.SCORE IS NULL AND ET1.SCORE_DI = '*')) ";
            $query .= "         AND L2.YEAR = ET1.YEAR ";
            $query .= "         AND L2.MOCKCD = ET1.MOCKCD ";
            $query .= "         AND L2.GROUP_DIV = '".$groupMst["GROUP_DIV"]."' ";
            $query .= "         AND L2.GRADE = EL1.GRADE ";
            $query .= "         AND L2.COURSECD = EL1.COURSECD ";
            $query .= "         AND L2.MAJORCD = EL1.MAJORCD ";
            $query .= "         AND L2.COURSECODE = EL1.COURSECODE ";
            $query .= "         AND L2.MOCK_SUBCLASS_CD = ET1.MOCK_SUBCLASS_CD ";
            $query .= "     GROUP BY ";
            $query .= "         ET1.MOCKCD, ";
            $query .= "         EL3.CLASSCD, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         EL3.SCHOOL_KIND, ";
                $query .= "         EL3.CURRICULUM_CD, ";
            }
            $query .= "         ET1.SCHREGNO, ";
            $query .= "         EL1.GRADE, ";
            $query .= "         EL1.HR_CLASS, ";
            $query .= "         EL1.COURSECD, ";
            $query .= "         EL1.MAJORCD, ";
            $query .= "         EL1.COURSECODE ";
            $query .= "     ) E1 ";
            $query .= " GROUP BY ";
            $query .= "     E1.SCHREGNO ";
            if ($groupMst["GROUP_DIV"] != 9 && $model->Properties["notGroupCnt"] != "1") {
                $query .= " HAVING ";
                $query .= "     COUNT(*) >= ".$groupMst["GROUP_DIV"]." ";
            }
        }

        $query .= " ), MOCT_T_KANSAN AS  ( ";
        if ($div == "TUUJOU_KAMOKU") {
            $query .= " SELECT ";
            $query .= "     LT1.MOCKCD, ";
            $query .= "     '999999' AS MOCK_SUBCLASS_CD, ";
            $query .= "     LT1.SCHREGNO, ";
            $query .= "     LL1.GRADE, ";
            $query .= "     LL1.HR_CLASS, ";
            $query .= "     LL1.COURSECD || LL1.MAJORCD || LL1.COURSECODE AS COURSE, ";
            $query .= "     SUM (CASE WHEN LT1.SCORE_DI = '*' ";
            $query .= "               THEN 1 ";
            $query .= "               ELSE 0 ";
            $query .= "          END) ";
            $query .= "     AS SCORE_DIS, ";
            $query .= "     SUM(CEIL(FLOAT(LT1.SCORE) * 100 / VALUE(LL2.PERFECT, 100))) AS SCORE, ";
            $query .= "     DECIMAL(ROUND(AVG(FLOAT(CEIL(FLOAT(LT1.SCORE) * 100 / VALUE(LL2.PERFECT, 100))))*100000,0)/100000,9,5) AS AVG ";
            $query .= " FROM ";
            $query .= "     MOCK_DAT LT1 ";
            $query .= "     INNER JOIN SCHREG_REGD_DAT LL1 ON LL1.YEAR = LT1.YEAR ";
            $query .= "         AND LL1.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "         AND LL1.SCHREGNO = LT1.SCHREGNO ";
            $query .= "     LEFT JOIN MOCK_PERFECT_DAT LL2 ON LL2.YEAR = LT1.YEAR ";
            $query .= "         AND LL2.COURSE_DIV = SUBSTR(LL1.COURSECODE, 4, 1) ";
            $query .= "         AND LL2.GRADE = LL1.GRADE ";
            $query .= "         AND LL2.MOCK_SUBCLASS_CD = LT1.MOCK_SUBCLASS_CD ";
            $query .= " WHERE ";
            $query .= "     LT1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND LT1.MOCKCD = '".$model->mockcd."' ";
            $query .= "     AND (LT1.SCORE IS NOT NULL OR (LT1.SCORE IS NULL AND LT1.SCORE_DI = '*')) ";
            $query .= " GROUP BY ";
            $query .= "     LT1.MOCKCD, ";
            $query .= "     LT1.SCHREGNO, ";
            $query .= "     LL1.GRADE, ";
            $query .= "     LL1.HR_CLASS, ";
            $query .= "     LL1.COURSECD, ";
            $query .= "     LL1.MAJORCD, ";
            $query .= "     LL1.COURSECODE ";
            $query .= " UNION ALL ";
            $query .= " SELECT ";
            $query .= "     LT1.MOCKCD, ";
            $query .= "     LT1.MOCK_SUBCLASS_CD, ";
            $query .= "     LT1.SCHREGNO, ";
            $query .= "     LL1.GRADE, ";
            $query .= "     LL1.HR_CLASS, ";
            $query .= "     LL1.COURSECD || LL1.MAJORCD || LL1.COURSECODE AS COURSE, ";
            $query .= "     SUM (CASE WHEN LT1.SCORE_DI = '*' ";
            $query .= "               THEN 1 ";
            $query .= "               ELSE 0 ";
            $query .= "          END) ";
            $query .= "     AS SCORE_DIS, ";
            $query .= "     SUM(CEIL(FLOAT(LT1.SCORE) * 100 / VALUE(LL2.PERFECT, 100))) AS SCORE, ";
            $query .= "     DECIMAL(ROUND(AVG(FLOAT(CEIL(FLOAT(LT1.SCORE) * 100 / VALUE(LL2.PERFECT, 100))))*100000,0)/100000,9,5) AS AVG ";
            $query .= " FROM ";
            $query .= "     MOCK_DAT LT1 ";
            $query .= "     INNER JOIN SCHREG_REGD_DAT LL1 ON LL1.YEAR = LT1.YEAR ";
            $query .= "         AND LL1.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "         AND LL1.SCHREGNO = LT1.SCHREGNO ";
            $query .= "     LEFT JOIN MOCK_PERFECT_DAT LL2 ON LL2.YEAR = LT1.YEAR ";
            $query .= "         AND LL2.COURSE_DIV = SUBSTR(LL1.COURSECODE, 4, 1) ";
            $query .= "         AND LL2.GRADE = LL1.GRADE ";
            $query .= "         AND LL2.MOCK_SUBCLASS_CD = LT1.MOCK_SUBCLASS_CD ";
            $query .= " WHERE ";
            $query .= "     LT1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND LT1.MOCKCD = '".$model->mockcd."' ";
            $query .= "     AND LT1.SCORE IS NOT NULL ";
            $query .= " GROUP BY ";
            $query .= "     LT1.MOCKCD, ";
            $query .= "     LT1.MOCK_SUBCLASS_CD, ";
            $query .= "     LT1.SCHREGNO, ";
            $query .= "     LL1.GRADE, ";
            $query .= "     LL1.HR_CLASS, ";
            $query .= "     LL1.COURSECD, ";
            $query .= "     LL1.MAJORCD, ";
            $query .= "     LL1.COURSECODE ";
        } else {
            $query .= " SELECT ";
            $query .= "     LT1.MOCKCD, ";
            $query .= "     '".$groupSubclass."' AS MOCK_SUBCLASS_CD, ";
            $query .= "     LT1.SCHREGNO, ";
            $query .= "     LL1.GRADE, ";
            $query .= "     LL1.HR_CLASS, ";
            $query .= "     LL1.COURSECD || LL1.MAJORCD || LL1.COURSECODE AS COURSE, ";
            $query .= "     SUM(CEIL(FLOAT(LT1.SCORE) * 100 / VALUE(LL2.PERFECT, 100))) AS SCORE, ";
            $query .= "     DECIMAL(ROUND(AVG(FLOAT(CEIL(FLOAT(LT1.SCORE) * 100 / VALUE(LL2.PERFECT, 100))))*100000,0)/100000,9,5) AS AVG ";
            $query .= " FROM ";
            $query .= "     MOCK_DAT LT1 ";
            $query .= "     INNER JOIN SCHREG_REGD_DAT LL1 ON LL1.YEAR = LT1.YEAR ";
            $query .= "         AND LL1.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "         AND LL1.SCHREGNO = LT1.SCHREGNO ";
            $query .= "     LEFT JOIN MOCK_PERFECT_DAT LL2 ON LL2.YEAR = LT1.YEAR ";
            $query .= "         AND LL2.COURSE_DIV = SUBSTR(LL1.COURSECODE, 4, 1) ";
            $query .= "         AND LL2.GRADE = LL1.GRADE ";
            $query .= "         AND LL2.MOCK_SUBCLASS_CD = LT1.MOCK_SUBCLASS_CD, ";
            $query .= "     MOCK_SUBCLASS_GROUP_DAT L2 ";
            $query .= " WHERE ";
            $query .= "     LT1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND LT1.MOCKCD = '".$model->mockcd."' ";
            $query .= "     AND LT1.SCORE IS NOT NULL ";
            $query .= "     AND L2.YEAR = LT1.YEAR ";
            $query .= "     AND L2.MOCKCD = LT1.MOCKCD ";
            $query .= "     AND L2.GROUP_DIV = '".$groupMst["GROUP_DIV"]."' ";
            $query .= "     AND L2.GRADE = LL1.GRADE ";
            $query .= "     AND L2.COURSECD = LL1.COURSECD ";
            $query .= "     AND L2.MAJORCD = LL1.MAJORCD ";
            $query .= "     AND L2.COURSECODE = LL1.COURSECODE ";
            $query .= "     AND L2.MOCK_SUBCLASS_CD = LT1.MOCK_SUBCLASS_CD ";
            $query .= "     AND EXISTS( ";
            $query .= "         SELECT ";
            $query .= "             'x' ";
            $query .= "         FROM ";
            $query .= "             SCORE_DI_T EE1 ";
            $query .= "         WHERE ";
            $query .= "             EE1.SCHREGNO = LT1.SCHREGNO ";
            $query .= "     ) ";
            $query .= " GROUP BY ";
            $query .= "     LT1.MOCKCD, ";
            $query .= "     LT1.SCHREGNO, ";
            $query .= "     LL1.GRADE, ";
            $query .= "     LL1.HR_CLASS, ";
            $query .= "     LL1.COURSECD, ";
            $query .= "     LL1.MAJORCD, ";
            $query .= "     LL1.COURSECODE ";
        }

        $query .= " ), T_AVERAGE AS  ( ";

        $query .= knjh339aQuery::getTavg("1", $div);

        $query .= " UNION ";
        $query .= knjh339aQuery::getTavg("2", $div);

        $query .= " UNION ";
        $query .= knjh339aQuery::getTavg("3", $div);


        $query .= " ), T_AVERAGE_KANSAN AS  ( ";

        $query .= knjh339aQuery::getTavgKansan("1");

        $query .= " UNION ";
        $query .= knjh339aQuery::getTavgKansan("2");

        $query .= " UNION ";
        $query .= knjh339aQuery::getTavgKansan("3");

        $query .= " ) ";

        if ($div == "TUUJOU_KAMOKU") {
            $query .= " SELECT ";
            $query .= "     W1.MOCKCD, ";
            $query .= "     W1.SCHREGNO, ";
            $query .= "     W1.MOCK_SUBCLASS_CD, ";
            $query .= "     W1.GRADE, ";
            $query .= "     W1.SCORE_DIS, ";
            $query .= "     W1.SCORE, ";
            $query .= "     W1.AVG, ";
            $query .= "     W1.AVG2, ";
            $query .= "     L1.SCORE AS SCORE_KANSAN, ";
            $query .= "     L1.AVG AS AVG_KANSAN, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE ORDER BY CASE WHEN W1.SCORE_DIS > 0 THEN -1 ELSE VALUE(W1.SCORE, -1) END DESC) AS GRADE_SCORE_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE, W1.HR_CLASS ORDER BY CASE WHEN W1.SCORE_DIS > 0 THEN -1 ELSE VALUE(W1.SCORE, -1) END DESC) AS CLASS_SCORE_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE, W1.COURSE ORDER BY CASE WHEN W1.SCORE_DIS > 0 THEN -1 ELSE VALUE(W1.SCORE, -1) END DESC) AS COURSE_SCORE_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE ORDER BY CASE WHEN W1.SCORE_DIS > 0 THEN -1 ELSE VALUE(W1.AVG, -1) END DESC) AS GRADE_AVG_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE, W1.HR_CLASS ORDER BY CASE WHEN W1.SCORE_DIS > 0 THEN -1 ELSE VALUE(W1.AVG, -1) END DESC) AS CLASS_AVG_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE, W1.COURSE ORDER BY CASE WHEN W1.SCORE_DIS > 0 THEN -1 ELSE VALUE(W1.AVG, -1) END DESC) AS COURSE_AVG_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE ORDER BY case when 0 < W2.STDDEV then DECIMAL(ROUND((10*(W1.SCORE - ROUND(FLOAT(W2.AVG) * 10, 0) / 10)/W2.STDDEV+50)*10,0)/10,5,1) end DESC) AS GRADE_DEVIATION_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE, W1.HR_CLASS ORDER BY case when 0 < W3.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-ROUND(FLOAT(W3.AVG) * 10, 0) / 10)/W3.STDDEV+50)*10,0)/10,5,1) end DESC) AS CLASS_DEVIATION_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE, W1.COURSE ORDER BY case when 0 < W4.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-ROUND(FLOAT(W4.AVG) * 10, 0) / 10)/W4.STDDEV+50)*10,0)/10,5,1) end DESC) AS COURSE_DEVIATION_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE ORDER BY CASE WHEN W1.SCORE_DIS > 0 THEN -1 ELSE VALUE(W1.AVG2, -1) END DESC) AS GRADE_AVG_RANK2, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE, W1.HR_CLASS ORDER BY CASE WHEN W1.SCORE_DIS > 0 THEN -1 ELSE VALUE(W1.AVG2, -1) END DESC) AS CLASS_AVG_RANK2, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE, W1.COURSE ORDER BY CASE WHEN W1.SCORE_DIS > 0 THEN -1 ELSE VALUE(W1.AVG2, -1) END DESC) AS COURSE_AVG_RANK2, ";
            $query .= "     case when 0 < W2.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-ROUND(FLOAT(W2.AVG) * 10, 0) / 10)/W2.STDDEV+50)*10,0)/10,5,1) end AS GRADE_DEVIATION, ";
            $query .= "     case when 0 < W3.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-ROUND(FLOAT(W3.AVG) * 10, 0) / 10)/W3.STDDEV+50)*10,0)/10,5,1) end AS CLASS_DEVIATION, ";
            $query .= "     case when 0 < W4.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-ROUND(FLOAT(W4.AVG) * 10, 0) / 10)/W4.STDDEV+50)*10,0)/10,5,1) end AS COURSE_DEVIATION, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE ORDER BY CASE WHEN W1.SCORE_DIS > 0 THEN -1 ELSE VALUE(L1.SCORE, -1) END DESC) AS GRADE_SCORE_RANK_KANSAN, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE, W1.HR_CLASS ORDER BY CASE WHEN W1.SCORE_DIS > 0 THEN -1 ELSE VALUE(L1.SCORE, -1) END DESC) AS CLASS_SCORE_RANK_KANSAN, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE, W1.COURSE ORDER BY CASE WHEN W1.SCORE_DIS > 0 THEN -1 ELSE VALUE(L1.SCORE, -1) END DESC) AS COURSE_SCORE_RANK_KANSAN, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE ORDER BY CASE WHEN W1.SCORE_DIS > 0 THEN -1 ELSE VALUE(L1.AVG, -1) END DESC) AS GRADE_AVG_RANK_KANSAN, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE, W1.HR_CLASS ORDER BY CASE WHEN W1.SCORE_DIS > 0 THEN -1 ELSE VALUE(L1.AVG, -1) END DESC) AS CLASS_AVG_RANK_KANSAN, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE, W1.COURSE ORDER BY CASE WHEN W1.SCORE_DIS > 0 THEN -1 ELSE VALUE(L1.AVG, -1) END DESC) AS COURSE_AVG_RANK_KANSAN, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE ORDER BY case when 0 < K2.STDDEV then DECIMAL(ROUND((10*(L1.SCORE-ROUND(FLOAT(K2.AVG) * 10, 0) / 10)/K2.STDDEV+50)*10,0)/10,5,1) end DESC) AS GRADE_DEVIATION_RANK_KANSAN, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE, W1.HR_CLASS ORDER BY case when 0 < K3.STDDEV then DECIMAL(ROUND((10*(L1.SCORE-ROUND(FLOAT(K3.AVG) * 10, 0) / 10)/K3.STDDEV+50)*10,0)/10,5,1) end DESC) AS CLASS_DEVIATION_RANK_KANSAN, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE, W1.COURSE ORDER BY case when 0 < K4.STDDEV then DECIMAL(ROUND((10*(L1.SCORE-ROUND(FLOAT(K4.AVG) * 10, 0) / 10)/K4.STDDEV+50)*10,0)/10,5,1) end DESC) AS COURSE_DEVIATION_RANK_KANSAN, ";
            $query .= "     case when 0 < K2.STDDEV then DECIMAL(ROUND((10*(L1.SCORE-ROUND(FLOAT(K2.AVG) * 10, 0) / 10)/K2.STDDEV+50)*10,0)/10,5,1) end AS GRADE_DEVIATION_KANSAN, ";
            $query .= "     case when 0 < K3.STDDEV then DECIMAL(ROUND((10*(L1.SCORE-ROUND(FLOAT(K3.AVG) * 10, 0) / 10)/K3.STDDEV+50)*10,0)/10,5,1) end AS CLASS_DEVIATION_KANSAN, ";
            $query .= "     case when 0 < K4.STDDEV then DECIMAL(ROUND((10*(L1.SCORE-ROUND(FLOAT(K4.AVG) * 10, 0) / 10)/K4.STDDEV+50)*10,0)/10,5,1) end AS COURSE_DEVIATION_KANSAN ";
            $query .= " FROM ";
            $query .= "     MOCT_T W1 ";
            $query .= "     LEFT JOIN MOCT_T_KANSAN L1 ON W1.MOCKCD = L1.MOCKCD ";
            $query .= "          AND W1.MOCK_SUBCLASS_CD = L1.MOCK_SUBCLASS_CD ";
            $query .= "          AND W1.SCHREGNO = L1.SCHREGNO ";
            $query .= "          AND W1.GRADE = L1.GRADE ";
            $query .= "          AND W1.HR_CLASS = L1.HR_CLASS ";
            $query .= "          AND W1.COURSE = L1.COURSE ";
            $query .= "     LEFT JOIN T_AVERAGE W2 ON W2.AVG_DIV = '1' AND W2.MOCKCD = W1.MOCKCD AND W2.MOCK_SUBCLASS_CD = W1.MOCK_SUBCLASS_CD AND W2.GRADE = W1.GRADE ";
            $query .= "     LEFT JOIN T_AVERAGE W3 ON W3.AVG_DIV = '2' AND W3.MOCKCD = W1.MOCKCD AND W3.MOCK_SUBCLASS_CD = W1.MOCK_SUBCLASS_CD AND W3.GRADE = W1.GRADE AND W3.HR_CLASS = W1.HR_CLASS ";
            $query .= "     LEFT JOIN T_AVERAGE W4 ON W4.AVG_DIV = '3' AND W4.MOCKCD = W1.MOCKCD AND W4.MOCK_SUBCLASS_CD = W1.MOCK_SUBCLASS_CD AND W4.GRADE = W1.GRADE AND W4.COURSE = W1.COURSE ";
            $query .= "     LEFT JOIN T_AVERAGE_KANSAN K2 ON K2.AVG_DIV = '1' AND K2.MOCKCD = W1.MOCKCD AND K2.MOCK_SUBCLASS_CD = W1.MOCK_SUBCLASS_CD AND K2.GRADE = W1.GRADE ";
            $query .= "     LEFT JOIN T_AVERAGE_KANSAN K3 ON K3.AVG_DIV = '2' AND K3.MOCKCD = W1.MOCKCD AND K3.MOCK_SUBCLASS_CD = W1.MOCK_SUBCLASS_CD AND K3.GRADE = W1.GRADE AND K3.HR_CLASS = W1.HR_CLASS ";
            $query .= "     LEFT JOIN T_AVERAGE_KANSAN K4 ON K4.AVG_DIV = '3' AND K4.MOCKCD = W1.MOCKCD AND K4.MOCK_SUBCLASS_CD = W1.MOCK_SUBCLASS_CD AND K4.GRADE = W1.GRADE AND K4.COURSE = W1.COURSE ";
        } else {
            $query .= " , RANK_T AS  ( ";
            $query .= " SELECT ";
            $query .= "     W1.SCHREGNO, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE ORDER BY VALUE(W1.SCORE, -1) DESC) AS GRADE_SCORE_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE, W1.HR_CLASS ORDER BY VALUE(W1.SCORE, -1) DESC) AS CLASS_SCORE_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE, W1.COURSE ORDER BY VALUE(W1.SCORE, -1) DESC) AS COURSE_SCORE_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE ORDER BY VALUE(W1.AVG, -1) DESC) AS GRADE_AVG_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE, W1.HR_CLASS ORDER BY VALUE(W1.AVG, -1) DESC) AS CLASS_AVG_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE, W1.COURSE ORDER BY VALUE(W1.AVG, -1) DESC) AS COURSE_AVG_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE ORDER BY case when 0 < W2.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-ROUND(FLOAT(W2.AVG) * 10, 0) / 10)/W2.STDDEV+50)*10,0)/10,5,1) end DESC) AS GRADE_DEVIATION_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE, W1.HR_CLASS ORDER BY case when 0 < W3.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-ROUND(FLOAT(W3.AVG) * 10, 0) / 10)/W3.STDDEV+50)*10,0)/10,5,1) end DESC) AS CLASS_DEVIATION_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE, W1.COURSE ORDER BY case when 0 < W4.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-ROUND(FLOAT(W4.AVG) * 10, 0) / 10)/W4.STDDEV+50)*10,0)/10,5,1) end DESC) AS COURSE_DEVIATION_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE ORDER BY VALUE(W1.AVG2, -1) DESC) AS GRADE_AVG_RANK2, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE, W1.HR_CLASS ORDER BY VALUE(W1.AVG2, -1) DESC) AS CLASS_AVG_RANK2, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE, W1.COURSE ORDER BY VALUE(W1.AVG2, -1) DESC) AS COURSE_AVG_RANK2, ";
            $query .= "     case when 0 < W2.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-ROUND(FLOAT(W2.AVG) * 10, 0) / 10)/W2.STDDEV+50)*10,0)/10,5,1) end AS GRADE_DEVIATION, ";
            $query .= "     case when 0 < W3.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-ROUND(FLOAT(W3.AVG) * 10, 0) / 10)/W3.STDDEV+50)*10,0)/10,5,1) end AS CLASS_DEVIATION, ";
            $query .= "     case when 0 < W4.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-ROUND(FLOAT(W4.AVG) * 10, 0) / 10)/W4.STDDEV+50)*10,0)/10,5,1) end AS COURSE_DEVIATION, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE ORDER BY VALUE(L1.SCORE, -1) DESC) AS GRADE_SCORE_RANK_KANSAN, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE, W1.HR_CLASS ORDER BY VALUE(L1.SCORE, -1) DESC) AS CLASS_SCORE_RANK_KANSAN, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE, W1.COURSE ORDER BY VALUE(L1.SCORE, -1) DESC) AS COURSE_SCORE_RANK_KANSAN, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE ORDER BY VALUE(L1.AVG, -1) DESC) AS GRADE_AVG_RANK_KANSAN, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE, W1.HR_CLASS ORDER BY VALUE(L1.AVG, -1) DESC) AS CLASS_AVG_RANK_KANSAN, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE, W1.COURSE ORDER BY VALUE(L1.AVG, -1) DESC) AS COURSE_AVG_RANK_KANSAN, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE ORDER BY case when 0 < K2.STDDEV then DECIMAL(ROUND((10*(L1.SCORE-ROUND(FLOAT(K2.AVG) * 10, 0) / 10)/K2.STDDEV+50)*10,0)/10,5,1) end DESC) AS GRADE_DEVIATION_RANK_KANSAN, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE, W1.HR_CLASS ORDER BY case when 0 < K3.STDDEV then DECIMAL(ROUND((10*(L1.SCORE-ROUND(FLOAT(K3.AVG) * 10, 0) / 10)/K3.STDDEV+50)*10,0)/10,5,1) end DESC) AS CLASS_DEVIATION_RANK_KANSAN, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE, W1.COURSE ORDER BY case when 0 < K4.STDDEV then DECIMAL(ROUND((10*(L1.SCORE-ROUND(FLOAT(K4.AVG) * 10, 0) / 10)/K4.STDDEV+50)*10,0)/10,5,1) end DESC) AS COURSE_DEVIATION_RANK_KANSAN, ";
            $query .= "     case when 0 < K2.STDDEV then DECIMAL(ROUND((10*(L1.SCORE-ROUND(FLOAT(K2.AVG) * 10, 0) / 10)/K2.STDDEV+50)*10,0)/10,5,1) end AS GRADE_DEVIATION_KANSAN, ";
            $query .= "     case when 0 < K3.STDDEV then DECIMAL(ROUND((10*(L1.SCORE-ROUND(FLOAT(K3.AVG) * 10, 0) / 10)/K3.STDDEV+50)*10,0)/10,5,1) end AS CLASS_DEVIATION_KANSAN, ";
            $query .= "     case when 0 < K4.STDDEV then DECIMAL(ROUND((10*(L1.SCORE-ROUND(FLOAT(K4.AVG) * 10, 0) / 10)/K4.STDDEV+50)*10,0)/10,5,1) end AS COURSE_DEVIATION_KANSAN ";
            $query .= " FROM ";
            $query .= "     MOCT_T W1 ";
            $query .= "     LEFT JOIN MOCT_T_KANSAN L1 ON W1.MOCKCD = L1.MOCKCD ";
            $query .= "          AND W1.MOCK_SUBCLASS_CD = L1.MOCK_SUBCLASS_CD ";
            $query .= "          AND W1.SCHREGNO = L1.SCHREGNO ";
            $query .= "          AND W1.GRADE = L1.GRADE ";
            $query .= "          AND W1.HR_CLASS = L1.HR_CLASS ";
            $query .= "          AND W1.COURSE = L1.COURSE ";
            $query .= "     LEFT JOIN T_AVERAGE W2 ON W2.AVG_DIV = '1' AND W2.MOCKCD = W1.MOCKCD AND W2.MOCK_SUBCLASS_CD = W1.MOCK_SUBCLASS_CD AND W2.GRADE = W1.GRADE ";
            $query .= "     LEFT JOIN T_AVERAGE W3 ON W3.AVG_DIV = '2' AND W3.MOCKCD = W1.MOCKCD AND W3.MOCK_SUBCLASS_CD = W1.MOCK_SUBCLASS_CD AND W3.GRADE = W1.GRADE AND W3.HR_CLASS = W1.HR_CLASS ";
            $query .= "     LEFT JOIN T_AVERAGE W4 ON W4.AVG_DIV = '3' AND W4.MOCKCD = W1.MOCKCD AND W4.MOCK_SUBCLASS_CD = W1.MOCK_SUBCLASS_CD AND W4.GRADE = W1.GRADE AND W4.COURSE = W1.COURSE ";
            $query .= "     LEFT JOIN T_AVERAGE_KANSAN K2 ON K2.AVG_DIV = '1' AND K2.MOCKCD = W1.MOCKCD AND K2.MOCK_SUBCLASS_CD = W1.MOCK_SUBCLASS_CD AND K2.GRADE = W1.GRADE ";
            $query .= "     LEFT JOIN T_AVERAGE_KANSAN K3 ON K3.AVG_DIV = '2' AND K3.MOCKCD = W1.MOCKCD AND K3.MOCK_SUBCLASS_CD = W1.MOCK_SUBCLASS_CD AND K3.GRADE = W1.GRADE AND K3.HR_CLASS = W1.HR_CLASS ";
            $query .= "     LEFT JOIN T_AVERAGE_KANSAN K4 ON K4.AVG_DIV = '3' AND K4.MOCKCD = W1.MOCKCD AND K4.MOCK_SUBCLASS_CD = W1.MOCK_SUBCLASS_CD AND K4.GRADE = W1.GRADE AND K4.COURSE = W1.COURSE ";
            $query .= " WHERE ";
            $query .= "     EXISTS ( ";
            $query .= "         SELECT ";
            $query .= "             'x' ";
            $query .= "         FROM ";
            $query .= "             SCORE_DI_T EE1 ";
            $query .= "         WHERE ";
            $query .= "             EE1.SCHREGNO = W1.SCHREGNO ";
            $query .= "             AND EE1.SCORE_DIS = 0 ";
            $query .= "     ) ";

            $query .= " ) ";
            $query .= " SELECT ";
            $query .= "     W1.MOCKCD, ";
            $query .= "     W1.SCHREGNO, ";
            $query .= "     W1.MOCK_SUBCLASS_CD, ";
            $query .= "     W1.GRADE, ";
            $query .= "     W1.SCORE, ";
            $query .= "     W1.AVG, ";
            $query .= "     W1.AVG2, ";
            $query .= "     L1.SCORE AS SCORE_KANSAN, ";
            $query .= "     L1.AVG AS AVG_KANSAN, ";
            $query .= "     RANK_T.GRADE_SCORE_RANK, ";
            $query .= "     RANK_T.CLASS_SCORE_RANK, ";
            $query .= "     RANK_T.COURSE_SCORE_RANK, ";
            $query .= "     RANK_T.GRADE_AVG_RANK, ";
            $query .= "     RANK_T.CLASS_AVG_RANK, ";
            $query .= "     RANK_T.COURSE_AVG_RANK, ";
            $query .= "     RANK_T.GRADE_DEVIATION_RANK, ";
            $query .= "     RANK_T.CLASS_DEVIATION_RANK, ";
            $query .= "     RANK_T.COURSE_DEVIATION_RANK, ";
            $query .= "     RANK_T.GRADE_AVG_RANK2, ";
            $query .= "     RANK_T.CLASS_AVG_RANK2, ";
            $query .= "     RANK_T.COURSE_AVG_RANK2, ";
            $query .= "     RANK_T.GRADE_DEVIATION, ";
            $query .= "     RANK_T.CLASS_DEVIATION, ";
            $query .= "     RANK_T.COURSE_DEVIATION, ";
            $query .= "     RANK_T.GRADE_SCORE_RANK_KANSAN, ";
            $query .= "     RANK_T.CLASS_SCORE_RANK_KANSAN, ";
            $query .= "     RANK_T.COURSE_SCORE_RANK_KANSAN, ";
            $query .= "     RANK_T.GRADE_AVG_RANK_KANSAN, ";
            $query .= "     RANK_T.CLASS_AVG_RANK_KANSAN, ";
            $query .= "     RANK_T.COURSE_AVG_RANK_KANSAN, ";
            $query .= "     RANK_T.GRADE_DEVIATION_RANK_KANSAN, ";
            $query .= "     RANK_T.CLASS_DEVIATION_RANK_KANSAN, ";
            $query .= "     RANK_T.COURSE_DEVIATION_RANK_KANSAN, ";
            $query .= "     RANK_T.GRADE_DEVIATION_KANSAN, ";
            $query .= "     RANK_T.CLASS_DEVIATION_KANSAN, ";
            $query .= "     RANK_T.COURSE_DEVIATION_KANSAN ";
            $query .= " FROM ";
            $query .= "     MOCT_T W1 ";
            $query .= "     LEFT JOIN RANK_T ON W1.SCHREGNO = RANK_T.SCHREGNO ";
            $query .= "     LEFT JOIN MOCT_T_KANSAN L1 ON W1.MOCKCD = L1.MOCKCD ";
            $query .= "          AND W1.MOCK_SUBCLASS_CD = L1.MOCK_SUBCLASS_CD ";
            $query .= "          AND W1.SCHREGNO = L1.SCHREGNO ";
            $query .= "          AND W1.GRADE = L1.GRADE ";
            $query .= "          AND W1.HR_CLASS = L1.HR_CLASS ";
            $query .= "          AND W1.COURSE = L1.COURSE ";

        }
        $query .= " ORDER BY ";
        $query .= "     W1.GRADE, ";
        $query .= "     W1.MOCKCD, ";
        $query .= "     W1.MOCK_SUBCLASS_CD, ";
        $query .= "     W1.SCORE DESC ";

        return $query;
    }

    function getMockRankDatDeviation($model, $groupMst, $groupSubclass, $div) {

        $query  = " WITH MOCT_T AS ( ";
        if ($div == "TUUJOU_KAMOKU") {
            $query .= " SELECT ";
            $query .= "     LT1.MOCKCD, ";
            $query .= "     '999999' AS MOCK_SUBCLASS_CD, ";
            $query .= "     LT1.SCHREGNO, ";
            $query .= "     LL1.GRADE, ";
            $query .= "     LL1.HR_CLASS, ";
            $query .= "     LL1.COURSECD || LL1.MAJORCD || LL1.COURSECODE AS COURSE, ";
            $query .= "     SUM(LL3.SCORE_DIS) AS SCORE_DIS, ";
            $query .= "     SUM(LT1.SCORE) AS SCORE, ";
            $query .= "     DECIMAL(ROUND(AVG(FLOAT(LT1.AVG))*100000,0)/100000,9,5) AS AVG, ";
            $query .= "     DECIMAL(ROUND(AVG(FLOAT(LT1.GRADE_DEVIATION))*10,0)/10,5,1) AS GRADE_AVG, ";
            $query .= "     DECIMAL(ROUND(AVG(FLOAT(LT1.CLASS_DEVIATION))*10,0)/10,5,1) AS CLASS_AVG, ";
            $query .= "     DECIMAL(ROUND(AVG(FLOAT(LT1.COURSE_DEVIATION))*10,0)/10,5,1) AS COURSE_AVG ";
            $query .= " FROM ";
            $query .= "     MOCK_RANK_DAT LT1 ";
            $query .= "     INNER JOIN SCHREG_REGD_DAT LL1 ON LL1.YEAR = LT1.YEAR ";
            $query .= "         AND LL1.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "         AND LL1.SCHREGNO = LT1.SCHREGNO ";
            $query .= "     INNER JOIN MOCK_DAT LL2 ON LL2.YEAR = LT1.YEAR ";
            $query .= "         AND LL2.MOCKCD = LT1.MOCKCD ";
            $query .= "         AND LL2.SCHREGNO = LT1.SCHREGNO ";
            $query .= "         AND LL2.MOCK_SUBCLASS_CD = LT1.MOCK_SUBCLASS_CD ";
            $query .= "     INNER JOIN ( ";
            $query .= "         SELECT ";
            $query .= "             W1.YEAR, ";
            $query .= "             W1.MOCKCD, ";
            $query .= "             W1.SCHREGNO, ";
            $query .= "             SUM (CASE WHEN W1.SCORE_DI = '*' ";
            $query .= "                       THEN 1 ";
            $query .= "                       ELSE 0 ";
            $query .= "                  END) ";
            $query .= "             AS SCORE_DIS ";
            $query .= "         FROM ";
            $query .= "             MOCK_DAT W1 ";
            $query .= "         WHERE ";
            $query .= "             W1.YEAR = '".CTRL_YEAR."' ";
            $query .= "             AND W1.MOCKCD = '".$model->mockcd."' ";
            $query .= "         GROUP BY ";
            $query .= "             W1.YEAR, ";
            $query .= "             W1.MOCKCD, ";
            $query .= "             W1.SCHREGNO ";
            $query .= "     ) LL3 ON LL3.YEAR = LT1.YEAR ";
            $query .= "         AND LL3.MOCKCD = LT1.MOCKCD ";
            $query .= "         AND LL3.SCHREGNO = LT1.SCHREGNO ";
            $query .= " WHERE ";
            $query .= "     LT1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND LT1.MOCKCD = '".$model->mockcd."' ";
            $query .= "     AND LT1.MOCKDIV = '3' ";
            $query .= "     AND (LL2.SCORE IS NOT NULL OR (LL2.SCORE IS NULL AND LL2.SCORE_DI = '*')) ";
            $query .= " GROUP BY ";
            $query .= "     LT1.MOCKCD, ";
            $query .= "     LT1.SCHREGNO, ";
            $query .= "     LL1.GRADE, ";
            $query .= "     LL1.HR_CLASS, ";
            $query .= "     LL1.COURSECD, ";
            $query .= "     LL1.MAJORCD, ";
            $query .= "     LL1.COURSECODE ";
        } else {
            $query .= " SELECT ";
            $query .= "     LT1.MOCKCD, ";
            $query .= "     '".$groupSubclass."' AS MOCK_SUBCLASS_CD, ";
            $query .= "     LT1.SCHREGNO, ";
            $query .= "     LL1.GRADE, ";
            $query .= "     LL1.HR_CLASS, ";
            $query .= "     LL1.COURSECD || LL1.MAJORCD || LL1.COURSECODE AS COURSE, ";
            $query .= "     SUM(LT1.SCORE) AS SCORE, ";
            $query .= "     DECIMAL(ROUND(AVG(FLOAT(LT1.AVG))*100000,0)/100000,9,5) AS AVG, ";
            $query .= "     DECIMAL(ROUND(AVG(FLOAT(LT1.GRADE_DEVIATION))*10,0)/10,5,1) AS GRADE_AVG, ";
            $query .= "     DECIMAL(ROUND(AVG(FLOAT(LT1.CLASS_DEVIATION))*10,0)/10,5,1) AS CLASS_AVG, ";
            $query .= "     DECIMAL(ROUND(AVG(FLOAT(LT1.COURSE_DEVIATION))*10,0)/10,5,1) AS COURSE_AVG ";
            $query .= " FROM ";
            $query .= "     MOCK_RANK_DAT LT1 ";
            $query .= "     INNER JOIN SCHREG_REGD_DAT LL1 ON LL1.YEAR = LT1.YEAR ";
            $query .= "         AND LL1.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "         AND LL1.SCHREGNO = LT1.SCHREGNO ";
            $query .= "     INNER JOIN MOCK_DAT LL2 ON LL2.YEAR = LT1.YEAR ";
            $query .= "         AND LL2.MOCKCD = LT1.MOCKCD ";
            $query .= "         AND LL2.SCHREGNO = LT1.SCHREGNO ";
            $query .= "         AND LL2.MOCK_SUBCLASS_CD = LT1.MOCK_SUBCLASS_CD, ";
            $query .= "     MOCK_SUBCLASS_GROUP_DAT L2 ";
            $query .= " WHERE ";
            $query .= "     LT1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND LT1.MOCKCD = '".$model->mockcd."' ";
            $query .= "     AND LT1.MOCKDIV = '3' ";
            $query .= "     AND L2.YEAR = LT1.YEAR ";
            $query .= "     AND L2.MOCKCD = LT1.MOCKCD ";
            $query .= "     AND L2.GROUP_DIV = '".$groupMst["GROUP_DIV"]."' ";
            $query .= "     AND L2.GRADE = LL1.GRADE ";
            $query .= "     AND L2.COURSECD = LL1.COURSECD ";
            $query .= "     AND L2.MAJORCD = LL1.MAJORCD ";
            $query .= "     AND L2.COURSECODE = LL1.COURSECODE ";
            $query .= "     AND L2.MOCK_SUBCLASS_CD = LT1.MOCK_SUBCLASS_CD ";
            $query .= "     AND EXISTS( ";
            $query .= "         SELECT ";
            $query .= "             'x' ";
            $query .= "         FROM ";
            $query .= "            (SELECT ";
            $query .= "                 E1.SCHREGNO ";
            $query .= "             FROM ";
            $query .= "                (SELECT ";
            $query .= "                     ET1.MOCKCD, ";
            $query .= "                     EL3.CLASSCD, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                     EL3.SCHOOL_KIND, ";
                $query .= "                     EL3.CURRICULUM_CD, ";
            }
            $query .= "                     SUM (CASE WHEN ET1.SCORE_DI = '*' ";
            $query .= "                               THEN 1 ";
            $query .= "                               ELSE 0 ";
            $query .= "                          END) ";
            $query .= "                     AS SCORE_DIS, ";
            $query .= "                     ET1.SCHREGNO, ";
            $query .= "                     EL1.GRADE, ";
            $query .= "                     EL1.HR_CLASS, ";
            $query .= "                     EL1.COURSECD || EL1.MAJORCD || EL1.COURSECODE AS COURSE ";
            $query .= "                 FROM ";
            $query .= "                     MOCK_DAT ET1 ";
            $query .= "                     INNER JOIN SCHREG_REGD_DAT EL1 ON EL1.YEAR = ET1.YEAR ";
            $query .= "                         AND EL1.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "                         AND EL1.SCHREGNO = ET1.SCHREGNO ";
            $query .= "                     LEFT JOIN MOCK_PERFECT_DAT EL2 ON EL2.YEAR = ET1.YEAR ";
            $query .= "                         AND EL2.COURSE_DIV = SUBSTR(EL1.COURSECODE, 4, 1) ";
            $query .= "                         AND EL2.GRADE = EL1.GRADE ";
            $query .= "                         AND EL2.MOCK_SUBCLASS_CD = ET1.MOCK_SUBCLASS_CD ";
            $query .= "                     LEFT JOIN MOCK_SUBCLASS_MST EL3 ON EL3.MOCK_SUBCLASS_CD = ET1.MOCK_SUBCLASS_CD, ";
            $query .= "                     MOCK_SUBCLASS_GROUP_DAT L2 ";
            $query .= "                 WHERE ";
            $query .= "                     ET1.YEAR = '".CTRL_YEAR."' ";
            $query .= "                     AND ET1.MOCKCD = '".$model->mockcd."' ";
            $query .= "                     AND (ET1.SCORE IS NOT NULL OR (ET1.SCORE IS NULL AND ET1.SCORE_DI = '*')) ";
            $query .= "                     AND L2.YEAR = ET1.YEAR ";
            $query .= "                     AND L2.MOCKCD = ET1.MOCKCD ";
            $query .= "                     AND L2.GROUP_DIV = '".$groupMst["GROUP_DIV"]."' ";
            $query .= "                     AND L2.GRADE = EL1.GRADE ";
            $query .= "                     AND L2.COURSECD = EL1.COURSECD ";
            $query .= "                     AND L2.MAJORCD = EL1.MAJORCD ";
            $query .= "                     AND L2.COURSECODE = EL1.COURSECODE ";
            $query .= "                     AND L2.MOCK_SUBCLASS_CD = ET1.MOCK_SUBCLASS_CD ";
            $query .= "                 GROUP BY ";
            $query .= "                     ET1.MOCKCD, ";
            $query .= "                     EL3.CLASSCD, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                     EL3.SCHOOL_KIND, ";
                $query .= "                     EL3.CURRICULUM_CD, ";
            }
            $query .= "                     ET1.SCHREGNO, ";
            $query .= "                     EL1.GRADE, ";
            $query .= "                     EL1.HR_CLASS, ";
            $query .= "                     EL1.COURSECD, ";
            $query .= "                     EL1.MAJORCD, ";
            $query .= "                     EL1.COURSECODE ";
            $query .= "                 ) E1 ";
            $query .= "             WHERE ";
            $query .= "                 E1.SCORE_DIS = 0 ";
            $query .= "             GROUP BY ";
            $query .= "                 E1.SCHREGNO ";
            if ($groupMst["GROUP_DIV"] != 9 && $model->Properties["notGroupCnt"] != "1") {
                $query .= "             HAVING ";
                $query .= "                 COUNT(*) >= ".$groupMst["GROUP_DIV"]." ";
            }
            $query .= "             ) EE1 ";
            $query .= "         WHERE ";
            $query .= "             EE1.SCHREGNO = LT1.SCHREGNO ";
            $query .= "     ) ";
            $query .= " GROUP BY ";
            $query .= "     LT1.MOCKCD, ";
            $query .= "     LT1.SCHREGNO, ";
            $query .= "     LL1.GRADE, ";
            $query .= "     LL1.HR_CLASS, ";
            $query .= "     LL1.COURSECD, ";
            $query .= "     LL1.MAJORCD, ";
            $query .= "     LL1.COURSECODE ";
        }
        if ($div == "GASSAN_KAMOKU") {
            $query .= " ), SCORE_DI_T AS  ( ";
            $query .= " SELECT ";
            $query .= "     E1.SCHREGNO, ";
            $query .= "     SUM(E1.SCORE_DIS) AS SCORE_DIS ";
            $query .= " FROM ";
            $query .= "    (SELECT ";
            $query .= "         ET1.MOCKCD, ";
            $query .= "         EL3.CLASSCD, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         EL3.SCHOOL_KIND, ";
                $query .= "         EL3.CURRICULUM_CD, ";
            }
            $query .= "         SUM (CASE WHEN ET1.SCORE_DI = '*' ";
            $query .= "                   THEN 1 ";
            $query .= "                   ELSE 0 ";
            $query .= "              END) ";
            $query .= "         AS SCORE_DIS, ";
            $query .= "         ET1.SCHREGNO, ";
            $query .= "         EL1.GRADE, ";
            $query .= "         EL1.HR_CLASS, ";
            $query .= "         EL1.COURSECD || EL1.MAJORCD || EL1.COURSECODE AS COURSE ";
            $query .= "     FROM ";
            $query .= "         MOCK_DAT ET1 ";
            $query .= "         INNER JOIN SCHREG_REGD_DAT EL1 ON EL1.YEAR = ET1.YEAR ";
            $query .= "             AND EL1.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "             AND EL1.SCHREGNO = ET1.SCHREGNO ";
            $query .= "         LEFT JOIN MOCK_PERFECT_DAT EL2 ON EL2.YEAR = ET1.YEAR ";
            $query .= "             AND EL2.COURSE_DIV = SUBSTR(EL1.COURSECODE, 4, 1) ";
            $query .= "             AND EL2.GRADE = EL1.GRADE ";
            $query .= "             AND EL2.MOCK_SUBCLASS_CD = ET1.MOCK_SUBCLASS_CD ";
            $query .= "         LEFT JOIN MOCK_SUBCLASS_MST EL3 ON EL3.MOCK_SUBCLASS_CD = ET1.MOCK_SUBCLASS_CD, ";
            $query .= "         MOCK_SUBCLASS_GROUP_DAT L2 ";
            $query .= "     WHERE ";
            $query .= "         ET1.YEAR = '".CTRL_YEAR."' ";
            $query .= "         AND ET1.MOCKCD = '".$model->mockcd."' ";
            $query .= "         AND (ET1.SCORE IS NOT NULL OR (ET1.SCORE IS NULL AND ET1.SCORE_DI = '*')) ";
            $query .= "         AND L2.YEAR = ET1.YEAR ";
            $query .= "         AND L2.MOCKCD = ET1.MOCKCD ";
            $query .= "         AND L2.GROUP_DIV = '".$groupMst["GROUP_DIV"]."' ";
            $query .= "         AND L2.GRADE = EL1.GRADE ";
            $query .= "         AND L2.COURSECD = EL1.COURSECD ";
            $query .= "         AND L2.MAJORCD = EL1.MAJORCD ";
            $query .= "         AND L2.COURSECODE = EL1.COURSECODE ";
            $query .= "         AND L2.MOCK_SUBCLASS_CD = ET1.MOCK_SUBCLASS_CD ";
            $query .= "     GROUP BY ";
            $query .= "         ET1.MOCKCD, ";
            $query .= "         EL3.CLASSCD, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         EL3.SCHOOL_KIND, ";
                $query .= "         EL3.CURRICULUM_CD, ";
            }
            $query .= "         ET1.SCHREGNO, ";
            $query .= "         EL1.GRADE, ";
            $query .= "         EL1.HR_CLASS, ";
            $query .= "         EL1.COURSECD, ";
            $query .= "         EL1.MAJORCD, ";
            $query .= "         EL1.COURSECODE ";
            $query .= "     ) E1 ";
            $query .= " GROUP BY ";
            $query .= "     E1.SCHREGNO ";
            if ($groupMst["GROUP_DIV"] != 9 && $model->Properties["notGroupCnt"] != "1") {
                $query .= " HAVING ";
                $query .= "     COUNT(*) >= ".$groupMst["GROUP_DIV"]." ";
            }
        }
        $query .= " ), RANK_T AS  ( ";
        $query .= " SELECT ";
        $query .= "     W1.SCHREGNO, ";
        if ($div == "TUUJOU_KAMOKU") {
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE ORDER BY CASE WHEN W1.SCORE_DIS > 0 THEN -1 ELSE VALUE(W1.GRADE_AVG, -1) END DESC) AS GRADE_DEVIATION_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE, W1.HR_CLASS ORDER BY CASE WHEN W1.SCORE_DIS > 0 THEN -1 ELSE VALUE(W1.CLASS_AVG, -1) END DESC) AS CLASS_DEVIATION_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE, W1.COURSE ORDER BY CASE WHEN W1.SCORE_DIS > 0 THEN -1 ELSE VALUE(W1.COURSE_AVG, -1) END DESC) AS COURSE_DEVIATION_RANK ";
            $query .= " FROM ";
            $query .= "     MOCT_T W1 ";
        } else {
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE ORDER BY VALUE(W1.GRADE_AVG, -1) DESC) AS GRADE_DEVIATION_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE, W1.HR_CLASS ORDER BY VALUE(W1.CLASS_AVG, -1) DESC) AS CLASS_DEVIATION_RANK, ";
            $query .= "     RANK() OVER(PARTITION BY W1.MOCKCD, W1.MOCK_SUBCLASS_CD, W1.GRADE, W1.COURSE ORDER BY VALUE(W1.COURSE_AVG, -1) DESC) AS COURSE_DEVIATION_RANK ";
            $query .= " FROM ";
            $query .= "     MOCT_T W1 ";
            $query .= " WHERE ";
            $query .= "     EXISTS ( ";
            $query .= "         SELECT ";
            $query .= "             'x' ";
            $query .= "         FROM ";
            $query .= "             SCORE_DI_T EE1 ";
            $query .= "         WHERE ";
            $query .= "             EE1.SCHREGNO = W1.SCHREGNO ";
            $query .= "             AND EE1.SCORE_DIS = 0 ";
            $query .= "     ) ";
        }
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     W1.MOCKCD, ";
        $query .= "     W1.SCHREGNO, ";
        $query .= "     W1.MOCK_SUBCLASS_CD, ";
        $query .= "     W1.GRADE, ";
        $query .= "     W1.SCORE, ";
        $query .= "     W1.AVG, ";
        $query .= "     W1.GRADE_AVG, ";
        $query .= "     W1.CLASS_AVG, ";
        $query .= "     W1.COURSE_AVG, ";
        if ($div == "TUUJOU_KAMOKU") {
            $query .= "     W1.SCORE_DIS, ";
        }
        $query .= "     RANK_T.GRADE_DEVIATION_RANK, ";
        $query .= "     RANK_T.CLASS_DEVIATION_RANK, ";
        $query .= "     RANK_T.COURSE_DEVIATION_RANK ";
        $query .= " FROM ";
        $query .= "     MOCT_T W1 ";
        $query .= "     LEFT JOIN RANK_T ON W1.SCHREGNO = RANK_T.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     W1.GRADE, ";
        $query .= "     W1.MOCKCD, ";
        $query .= "     W1.MOCK_SUBCLASS_CD, ";
        $query .= "     W1.SCORE DESC ";

        return $query;
    }

    function getTavg($avgDiv, $div)
    {
        $groupBy = "";
        $selectHr = "'000' AS ";
        $selectCs = "'00000000' AS ";
        if ($avgDiv == "1") {
            $groupBy = " MOCKCD, MOCK_SUBCLASS_CD, GRADE ";
        } else if ($avgDiv == "2") {
            $groupBy = " MOCKCD, MOCK_SUBCLASS_CD, GRADE, HR_CLASS ";
            $selectHr = "";
        } else {
            $groupBy = " MOCKCD, MOCK_SUBCLASS_CD, GRADE, COURSE ";
            $selectCs = "";
        }

        $query  = " SELECT ";
        $query .= "    '".$avgDiv."' as AVG_DIV, ";
        $query .= "     MOCKCD, ";
        $query .= "     MOCK_SUBCLASS_CD, ";
        $query .= "     GRADE, ";
        $query .= "     ".$selectHr."HR_CLASS, ";
        $query .= "     ".$selectCs."COURSE, ";
        $query .= "     SUM(SCORE) AS SCORE, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(SCORE))*100000,0)/100000,9,5) AS AVG, ";
        $query .= "     DECIMAL(ROUND(STDDEV(FLOAT(SCORE))*10,0)/10,5,1) AS STDDEV ";
        $query .= " FROM ";
        $query .= "     MOCT_T ";
        if ($div == "GASSAN_KAMOKU") {
            $query .= " WHERE ";
            $query .= "     EXISTS ( ";
            $query .= "         SELECT ";
            $query .= "             'x' ";
            $query .= "         FROM ";
            $query .= "             SCORE_DI_T EE1 ";
            $query .= "         WHERE ";
            $query .= "             EE1.SCHREGNO = MOCT_T.SCHREGNO ";
            $query .= "             AND EE1.SCORE_DIS = 0 ";
            $query .= "     ) ";
        }
        $query .= " GROUP BY ".$groupBy;

        return $query;
    }

    function getTavgKansan($avgDiv)
    {
        $groupBy = "";
        $selectHr = "'000' AS ";
        $selectCs = "'00000000' AS ";
        if ($avgDiv == "1") {
            $groupBy = " MOCKCD, MOCK_SUBCLASS_CD, GRADE ";
        } else if ($avgDiv == "2") {
            $groupBy = " MOCKCD, MOCK_SUBCLASS_CD, GRADE, HR_CLASS ";
            $selectHr = "";
        } else {
            $groupBy = " MOCKCD, MOCK_SUBCLASS_CD, GRADE, COURSE ";
            $selectCs = "";
        }

        $query  = " SELECT ";
        $query .= "    '".$avgDiv."' as AVG_DIV, ";
        $query .= "     MOCKCD, ";
        $query .= "     MOCK_SUBCLASS_CD, ";
        $query .= "     GRADE, ";
        $query .= "     ".$selectHr."HR_CLASS, ";
        $query .= "     ".$selectCs."COURSE, ";
        $query .= "     SUM(SCORE) AS SCORE, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(SCORE))*100000,0)/100000,9,5) AS AVG, ";
        $query .= "     DECIMAL(ROUND(STDDEV(FLOAT(SCORE))*10,0)/10,5,1) AS STDDEV ";
        $query .= " FROM ";
        $query .= "     MOCT_T_KANSAN ";
        $query .= " GROUP BY ".$groupBy;

        return $query;
    }

    function insertMockRankDat($db, $query, $model, $div)
    {
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($div == "GASSAN_KAMOKU") {
                $delQuery = "DELETE FROM MOCK_RANK_DAT WHERE YEAR = '".CTRL_YEAR."' AND MOCKCD = '".$model->mockcd."' AND MOCK_SUBCLASS_CD = '".$row["MOCK_SUBCLASS_CD"]."' AND SCHREGNO = '".$row["SCHREGNO"]."'";
                $db->query($delQuery);
            }

            //総得点での順位
            $data = array();
            $data["YEAR"][TEXT]                 = CTRL_YEAR;
            $data["MOCKCD"][TEXT]               = $row["MOCKCD"];
            $data["SCHREGNO"][TEXT]             = $row["SCHREGNO"];
            $data["MOCK_SUBCLASS_CD"][TEXT]     = $row["MOCK_SUBCLASS_CD"];
            $data["MOCKDIV"][TEXT]              = "1";
            $data["SCORE"][NUMBER]              = $row["SCORE"];
            $data["AVG"][NUMBER]                = $row["AVG2"];
            $data["GRADE_RANK"][NUMBER]         = $row["SCORE_DIS"] > 0 ? NULL : $row["GRADE_SCORE_RANK"];
            $data["GRADE_DEVIATION"][NUMBER]    = $row["GRADE_DEVIATION"];
            $data["CLASS_RANK"][NUMBER]         = $row["SCORE_DIS"] > 0 ? NULL : $row["CLASS_SCORE_RANK"];
            $data["CLASS_DEVIATION"][NUMBER]    = $row["CLASS_DEVIATION"];
            $data["COURSE_RANK"][NUMBER]        = $row["SCORE_DIS"] > 0 ? NULL : $row["COURSE_SCORE_RANK"];
            $data["COURSE_DEVIATION"][NUMBER]   = $row["COURSE_DEVIATION"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "SYSDATE()";

            $query = Query::insertSQL($data, "MOCK_RANK_DAT");
            $db->query($query);

            //平均点での順位
            $data = array();
            $data["YEAR"][TEXT]                 = CTRL_YEAR;
            $data["MOCKCD"][TEXT]               = $row["MOCKCD"];
            $data["SCHREGNO"][TEXT]             = $row["SCHREGNO"];
            $data["MOCK_SUBCLASS_CD"][TEXT]     = $row["MOCK_SUBCLASS_CD"];
            $data["MOCKDIV"][TEXT]              = "2";
            $data["SCORE"][NUMBER]              = $row["SCORE"];
            $data["AVG"][NUMBER]                = $row["AVG2"];
            $data["GRADE_RANK"][NUMBER]         = $row["SCORE_DIS"] > 0 ? NULL : $row["GRADE_AVG_RANK2"];
            $data["GRADE_DEVIATION"][NUMBER]    = $row["GRADE_DEVIATION"];
            $data["CLASS_RANK"][NUMBER]         = $row["SCORE_DIS"] > 0 ? NULL : $row["CLASS_AVG_RANK2"];
            $data["CLASS_DEVIATION"][NUMBER]    = $row["CLASS_DEVIATION"];
            $data["COURSE_RANK"][NUMBER]        = $row["SCORE_DIS"] > 0 ? NULL : $row["COURSE_AVG_RANK2"];
            $data["COURSE_DEVIATION"][NUMBER]   = $row["COURSE_DEVIATION"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "SYSDATE()";

            $query = Query::insertSQL($data, "MOCK_RANK_DAT");
            $db->query($query);

            //偏差値での順位
            $data = array();
            $data["YEAR"][TEXT]                 = CTRL_YEAR;
            $data["MOCKCD"][TEXT]               = $row["MOCKCD"];
            $data["SCHREGNO"][TEXT]             = $row["SCHREGNO"];
            $data["MOCK_SUBCLASS_CD"][TEXT]     = $row["MOCK_SUBCLASS_CD"];
            $data["MOCKDIV"][TEXT]              = "3";
            $data["SCORE"][NUMBER]              = $row["SCORE_KANSAN"];
            $data["AVG"][NUMBER]                = $row["AVG_KANSAN"];
            $data["GRADE_RANK"][NUMBER]         = $row["SCORE_DIS"] > 0 ? NULL : $row["GRADE_DEVIATION_RANK_KANSAN"];
            $data["GRADE_DEVIATION"][NUMBER]    = $row["GRADE_DEVIATION_KANSAN"];
            $data["CLASS_RANK"][NUMBER]         = $row["SCORE_DIS"] > 0 ? NULL : $row["CLASS_DEVIATION_RANK_KANSAN"];
            $data["CLASS_DEVIATION"][NUMBER]    = $row["CLASS_DEVIATION_KANSAN"];
            $data["COURSE_RANK"][NUMBER]        = $row["SCORE_DIS"] > 0 ? NULL : $row["COURSE_DEVIATION_RANK_KANSAN"];
            $data["COURSE_DEVIATION"][NUMBER]   = $row["COURSE_DEVIATION_KANSAN"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "SYSDATE()";

            $query = Query::insertSQL($data, "MOCK_RANK_DAT");
            $db->query($query);

            //平均点2での順位
            $data = array();
            $data["YEAR"][TEXT]                 = CTRL_YEAR;
            $data["MOCKCD"][TEXT]               = $row["MOCKCD"];
            $data["SCHREGNO"][TEXT]             = $row["SCHREGNO"];
            $data["MOCK_SUBCLASS_CD"][TEXT]     = $row["MOCK_SUBCLASS_CD"];
            $data["MOCKDIV"][TEXT]              = "4";
            $data["SCORE"][NUMBER]              = $row["SCORE"];
            $data["AVG"][NUMBER]                = $row["AVG2"];
            $data["GRADE_RANK"][NUMBER]         = $row["SCORE_DIS"] > 0 ? NULL : $row["GRADE_AVG_RANK2"];
            $data["GRADE_DEVIATION"][NUMBER]    = $row["GRADE_DEVIATION"];
            $data["CLASS_RANK"][NUMBER]         = $row["SCORE_DIS"] > 0 ? NULL : $row["CLASS_AVG_RANK2"];
            $data["CLASS_DEVIATION"][NUMBER]    = $row["CLASS_DEVIATION"];
            $data["COURSE_RANK"][NUMBER]        = $row["SCORE_DIS"] > 0 ? NULL : $row["COURSE_AVG_RANK2"];
            $data["COURSE_DEVIATION"][NUMBER]   = $row["COURSE_DEVIATION"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "SYSDATE()";

            $query = Query::insertSQL($data, "MOCK_RANK_DAT");
            $db->query($query);
        }
        $result->free();
    }

    function insertMockRankDatDeviation($db, $query, $model, $div)
    {
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $delQuery = "DELETE FROM MOCK_RANK_DAT WHERE YEAR = '".CTRL_YEAR."' AND MOCKCD = '".$model->mockcd."' AND MOCK_SUBCLASS_CD = '".$row["MOCK_SUBCLASS_CD"]."' AND SCHREGNO = '".$row["SCHREGNO"]."' AND MOCKDIV = '3'";
            $db->query($delQuery);

            //偏差値での順位
            $data = array();
            $data["YEAR"][TEXT]                 = CTRL_YEAR;
            $data["MOCKCD"][TEXT]               = $row["MOCKCD"];
            $data["SCHREGNO"][TEXT]             = $row["SCHREGNO"];
            $data["MOCK_SUBCLASS_CD"][TEXT]     = $row["MOCK_SUBCLASS_CD"];
            $data["MOCKDIV"][TEXT]              = "3";
            $data["SCORE"][NUMBER]              = $row["SCORE"];
            $data["AVG"][NUMBER]                = $row["AVG"];
            $data["GRADE_RANK"][NUMBER]         = $row["SCORE_DIS"] > 0 ? NULL : $row["GRADE_DEVIATION_RANK"];
            $data["GRADE_DEVIATION"][NUMBER]    = $row["GRADE_AVG"];
            $data["CLASS_RANK"][NUMBER]         = $row["SCORE_DIS"] > 0 ? NULL : $row["CLASS_DEVIATION_RANK"];
            $data["CLASS_DEVIATION"][NUMBER]    = $row["CLASS_AVG"];
            $data["COURSE_RANK"][NUMBER]        = $row["SCORE_DIS"] > 0 ? NULL : $row["COURSE_DEVIATION_RANK"];
            $data["COURSE_DEVIATION"][NUMBER]   = $row["COURSE_AVG"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "SYSDATE()";
            $query = Query::insertSQL($data, "MOCK_RANK_DAT");
            $db->query($query);

        }
        $result->free();
    }

    //模試席次更新
    function UpdateMockAverageDat($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $delQuery = "DELETE FROM MOCK_AVERAGE_DAT WHERE YEAR = '".CTRL_YEAR."' AND MOCKCD = '".$model->mockcd."'";
        $db->query($delQuery);

        $query = knjh339aQuery::getAverageDatSql($model, "", "", "TUUJOU_KAMOKU");
        knjh339aQuery::InsertMockAverageDat($db, $query);

        $groupMstQuery  = "SELECT DISTINCT ";
        $groupMstQuery .= "  GROUP_DIV ";
        $groupMstQuery .= "FROM ";
        $groupMstQuery .= "  MOCK_SUBCLASS_GROUP_MST ";
        $groupMstQuery .= "WHERE ";
        $groupMstQuery .= "  YEAR = '".CTRL_YEAR."' ";
        $groupMstQuery .= "  AND MOCKCD = '".$model->mockcd."' ";

        $result = $db->query($groupMstQuery);
        while ($groupMst = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            $groupSubclass = "";
            for ($i = 0; $i < 6; $i++) {
                $groupSubclass .= $groupMst["GROUP_DIV"];
            }

            $query = knjh339aQuery::getAverageDatSql($model, $groupMst, $groupSubclass, "GASSAN_KAMOKU");
            knjh339aQuery::InsertMockAverageDat($db, $query);

        }
        $result = $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
    }

    function getAverageDatSql($model, $groupMst, $groupSubclass, $div)
    {
        $query  = " WITH MOCT_T AS (";
        $query .= " SELECT ";
        $query .= "     T1.MOCKCD, ";
        if ($div == "GASSAN_KAMOKU") {
            $query .= "     '".$groupSubclass."' AS MOCK_SUBCLASS_CD, ";
        } else {
            $query .= "     T1.MOCK_SUBCLASS_CD, ";
        }
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L1.GRADE, ";
        $query .= "     L1.HR_CLASS, ";
        $query .= "     L1.COURSECD || L1.MAJORCD || L1.COURSECODE AS COURSE, ";
        $query .= "     SUM(T1.SCORE) AS SCORE, ";
        $query .= "     SUM(VALUE(LL1.PERFECT, 100)) AS PERFECT, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(T1.SCORE)) * 100000, 0) / 100000, 9, 5) AS AVG ";
        $query .= " FROM ";
        $query .= "     MOCK_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "           AND L1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "           AND L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN MOCK_PERFECT_DAT LL1 ON LL1.YEAR = T1.YEAR ";
        $query .= "         AND LL1.COURSE_DIV = SUBSTR(L1.COURSECODE, 4, 1) ";
        $query .= "         AND LL1.GRADE = L1.GRADE ";
        $query .= "         AND LL1.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD ";
        if ($div == "GASSAN_KAMOKU") {
            $query .= "     ,MOCK_SUBCLASS_GROUP_DAT L2 ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.MOCKCD = '".$model->mockcd."' ";
        $query .= "     AND T1.SCORE IS NOT NULL ";
        if ($div == "GASSAN_KAMOKU") {
            $query .= "     AND L2.YEAR = T1.YEAR ";
            $query .= "     AND L2.MOCKCD = T1.MOCKCD ";
            $query .= "     AND L2.GROUP_DIV = '".$groupMst["GROUP_DIV"]."' ";
            $query .= "     AND L2.GRADE = L1.GRADE ";
            $query .= "     AND L2.COURSECD = L1.COURSECD ";
            $query .= "     AND L2.MAJORCD = L1.MAJORCD ";
            $query .= "     AND L2.COURSECODE = L1.COURSECODE ";
            $query .= "     AND L2.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD ";
            $query .= "     AND EXISTS( ";
            $query .= "         SELECT ";
            $query .= "             'x' ";
            $query .= "         FROM ";
            $query .= "            (SELECT ";
            $query .= "                 E1.SCHREGNO ";
            $query .= "             FROM ";
            $query .= "                (SELECT ";
            $query .= "                     ET1.MOCKCD, ";
            $query .= "                     EL3.CLASSCD, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                     EL3.SCHOOL_KIND, ";
                $query .= "                     EL3.CURRICULUM_CD, ";
            }
            $query .= "                     SUM (CASE WHEN ET1.SCORE_DI = '*' ";
            $query .= "                               THEN 1 ";
            $query .= "                               ELSE 0 ";
            $query .= "                          END) ";
            $query .= "                     AS SCORE_DIS, ";
            $query .= "                     ET1.SCHREGNO, ";
            $query .= "                     EL1.GRADE, ";
            $query .= "                     EL1.HR_CLASS, ";
            $query .= "                     EL1.COURSECD || EL1.MAJORCD || EL1.COURSECODE AS COURSE ";
            $query .= "                 FROM ";
            $query .= "                     MOCK_DAT ET1 ";
            $query .= "                     INNER JOIN SCHREG_REGD_DAT EL1 ON EL1.YEAR = ET1.YEAR ";
            $query .= "                         AND EL1.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "                         AND EL1.SCHREGNO = ET1.SCHREGNO ";
            $query .= "                     LEFT JOIN MOCK_PERFECT_DAT EL2 ON EL2.YEAR = ET1.YEAR ";
            $query .= "                         AND EL2.COURSE_DIV = SUBSTR(EL1.COURSECODE, 4, 1) ";
            $query .= "                         AND EL2.GRADE = EL1.GRADE ";
            $query .= "                         AND EL2.MOCK_SUBCLASS_CD = ET1.MOCK_SUBCLASS_CD ";
            $query .= "                     LEFT JOIN MOCK_SUBCLASS_MST EL3 ON EL3.MOCK_SUBCLASS_CD = ET1.MOCK_SUBCLASS_CD, ";
            $query .= "                     MOCK_SUBCLASS_GROUP_DAT L2 ";
            $query .= "                 WHERE ";
            $query .= "                     ET1.YEAR = '".CTRL_YEAR."' ";
            $query .= "                     AND ET1.MOCKCD = '".$model->mockcd."' ";
            $query .= "                     AND (ET1.SCORE IS NOT NULL OR (ET1.SCORE IS NULL AND ET1.SCORE_DI = '*')) ";
            $query .= "                     AND L2.YEAR = ET1.YEAR ";
            $query .= "                     AND L2.MOCKCD = ET1.MOCKCD ";
            $query .= "                     AND L2.GROUP_DIV = '".$groupMst["GROUP_DIV"]."' ";
            $query .= "                     AND L2.GRADE = EL1.GRADE ";
            $query .= "                     AND L2.COURSECD = EL1.COURSECD ";
            $query .= "                     AND L2.MAJORCD = EL1.MAJORCD ";
            $query .= "                     AND L2.COURSECODE = EL1.COURSECODE ";
            $query .= "                     AND L2.MOCK_SUBCLASS_CD = ET1.MOCK_SUBCLASS_CD ";
            $query .= "                 GROUP BY ";
            $query .= "                     ET1.MOCKCD, ";
            $query .= "                     EL3.CLASSCD, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                     EL3.SCHOOL_KIND, ";
                $query .= "                     EL3.CURRICULUM_CD, ";
            }
            $query .= "                     ET1.SCHREGNO, ";
            $query .= "                     EL1.GRADE, ";
            $query .= "                     EL1.HR_CLASS, ";
            $query .= "                     EL1.COURSECD, ";
            $query .= "                     EL1.MAJORCD, ";
            $query .= "                     EL1.COURSECODE ";
            $query .= "                 ) E1 ";
            $query .= "             WHERE ";
            $query .= "                 E1.SCORE_DIS = 0 ";
            $query .= "             GROUP BY ";
            $query .= "                 E1.SCHREGNO ";
            if ($groupMst["GROUP_DIV"] != 9 && $model->Properties["notGroupCnt"] != "1") {
                $query .= "             HAVING ";
                $query .= "                 COUNT(*) >= ".$groupMst["GROUP_DIV"]." ";
            }
            $query .= "             ) EE1 ";
            $query .= "         WHERE ";
            $query .= "             EE1.SCHREGNO = T1.SCHREGNO ";
            $query .= "     ) ";
        }
        $query .= " GROUP BY ";
        $query .= "     T1.MOCKCD, ";
        if ($div == "TUUJOU_KAMOKU") {
            $query .= "     T1.MOCK_SUBCLASS_CD, ";
        }
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L1.GRADE, ";
        $query .= "     L1.HR_CLASS, ";
        $query .= "     L1.COURSECD, ";
        $query .= "     L1.MAJORCD, ";
        $query .= "     L1.COURSECODE ";

        if ($div == "GASSAN_KAMOKU") {
            $query .= " ), SCORE_DI_T AS  ( ";
            $query .= " SELECT ";
            $query .= "     E1.SCHREGNO, ";
            $query .= "     SUM(E1.SCORE_DIS) AS SCORE_DIS ";
            $query .= " FROM ";
            $query .= "    (SELECT ";
            $query .= "         ET1.MOCKCD, ";
            $query .= "         EL3.CLASSCD, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         EL3.SCHOOL_KIND, ";
                $query .= "         EL3.CURRICULUM_CD, ";
            }
            $query .= "         SUM (CASE WHEN ET1.SCORE_DI = '*' ";
            $query .= "                   THEN 1 ";
            $query .= "                   ELSE 0 ";
            $query .= "              END) ";
            $query .= "         AS SCORE_DIS, ";
            $query .= "         ET1.SCHREGNO, ";
            $query .= "         EL1.GRADE, ";
            $query .= "         EL1.HR_CLASS, ";
            $query .= "         EL1.COURSECD || EL1.MAJORCD || EL1.COURSECODE AS COURSE ";
            $query .= "     FROM ";
            $query .= "         MOCK_DAT ET1 ";
            $query .= "         INNER JOIN SCHREG_REGD_DAT EL1 ON EL1.YEAR = ET1.YEAR ";
            $query .= "             AND EL1.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "             AND EL1.SCHREGNO = ET1.SCHREGNO ";
            $query .= "         LEFT JOIN MOCK_PERFECT_DAT EL2 ON EL2.YEAR = ET1.YEAR ";
            $query .= "             AND EL2.COURSE_DIV = SUBSTR(EL1.COURSECODE, 4, 1) ";
            $query .= "             AND EL2.GRADE = EL1.GRADE ";
            $query .= "             AND EL2.MOCK_SUBCLASS_CD = ET1.MOCK_SUBCLASS_CD ";
            $query .= "         LEFT JOIN MOCK_SUBCLASS_MST EL3 ON EL3.MOCK_SUBCLASS_CD = ET1.MOCK_SUBCLASS_CD, ";
            $query .= "         MOCK_SUBCLASS_GROUP_DAT L2 ";
            $query .= "     WHERE ";
            $query .= "         ET1.YEAR = '".CTRL_YEAR."' ";
            $query .= "         AND ET1.MOCKCD = '".$model->mockcd."' ";
            $query .= "         AND (ET1.SCORE IS NOT NULL OR (ET1.SCORE IS NULL AND ET1.SCORE_DI = '*')) ";
            $query .= "         AND L2.YEAR = ET1.YEAR ";
            $query .= "         AND L2.MOCKCD = ET1.MOCKCD ";
            $query .= "         AND L2.GROUP_DIV = '".$groupMst["GROUP_DIV"]."' ";
            $query .= "         AND L2.GRADE = EL1.GRADE ";
            $query .= "         AND L2.COURSECD = EL1.COURSECD ";
            $query .= "         AND L2.MAJORCD = EL1.MAJORCD ";
            $query .= "         AND L2.COURSECODE = EL1.COURSECODE ";
            $query .= "         AND L2.MOCK_SUBCLASS_CD = ET1.MOCK_SUBCLASS_CD ";
            $query .= "     GROUP BY ";
            $query .= "         ET1.MOCKCD, ";
            $query .= "         EL3.CLASSCD, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         EL3.SCHOOL_KIND, ";
                $query .= "         EL3.CURRICULUM_CD, ";
            }
            $query .= "         ET1.SCHREGNO, ";
            $query .= "         EL1.GRADE, ";
            $query .= "         EL1.HR_CLASS, ";
            $query .= "         EL1.COURSECD, ";
            $query .= "         EL1.MAJORCD, ";
            $query .= "         EL1.COURSECODE ";
            $query .= "     ) E1 ";
            $query .= " GROUP BY ";
            $query .= "     E1.SCHREGNO ";
            if ($groupMst["GROUP_DIV"] != 9 && $model->Properties["notGroupCnt"] != "1") {
                $query .= " HAVING ";
                $query .= "     COUNT(*) >= ".$groupMst["GROUP_DIV"]." ";
            }
        }

        $query .= " ), T_AVERAGE AS  ( ";

        $query .= knjh339aQuery::getTavg2("1", $div);

        $query .= " UNION ";
        $query .= knjh339aQuery::getTavg2("2", $div);

        $query .= " UNION ";
        $query .= knjh339aQuery::getTavg2("3", $div);

        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     MOCKCD, ";
        $query .= "     MOCK_SUBCLASS_CD, ";
        $query .= "     AVG_DIV, ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     COURSE, ";
        $query .= "     SCORE, ";
        $query .= "     HIGHSCORE, ";
        $query .= "     HIGHSCORE_KANSAN, ";
        $query .= "     LOWSCORE, ";
        $query .= "     LOWSCORE_KANSAN, ";
        $query .= "     CNT, ";
        $query .= "     AVG, ";
        $query .= "     AVG2, ";
        $query .= "     STDDEV ";
        $query .= " FROM ";
        $query .= "     T_AVERAGE ";
        $query .= " ORDER BY ";
        $query .= "     GRADE, ";
        $query .= "     MOCKCD, ";
        $query .= "     AVG_DIV, ";
        $query .= "     MOCK_SUBCLASS_CD ";

        return $query;
    }

    function getTavg2($avgDiv, $div)
    {
        $groupBy = "";
        $selectHr = "'000' AS ";
        $selectCs = "'00000000' AS ";
        if ($avgDiv == "1") {
            $groupBy = " MOCKCD, MOCK_SUBCLASS_CD, GRADE ";
        } else if ($avgDiv == "2") {
            $groupBy = " MOCKCD, MOCK_SUBCLASS_CD, GRADE, HR_CLASS ";
            $selectHr = "";
        } else {
            $groupBy = " MOCKCD, MOCK_SUBCLASS_CD, GRADE, COURSE ";
            $selectCs = "";
        }

        $query .= " SELECT ";
        $query .= "    '".$avgDiv."' as AVG_DIV, ";
        $query .= "     MOCKCD, ";
        $query .= "     MOCK_SUBCLASS_CD, ";
        $query .= "     GRADE, ";
        $query .= "     ".$selectHr."HR_CLASS, ";
        $query .= "     ".$selectCs."COURSE, ";
        $query .= "     SUM(SCORE) AS SCORE, ";
        $query .= "     MAX(SCORE) AS HIGHSCORE, ";
        $query .= "     MAX(CEIL( FLOAT(SCORE) * 100 / PERFECT)) AS HIGHSCORE_KANSAN, ";
        $query .= "     MIN(SCORE) AS LOWSCORE, ";
        $query .= "     MIN(CEIL( FLOAT(SCORE) * 100 / PERFECT)) AS LOWSCORE_KANSAN, ";
        $query .= "     COUNT(MOCKCD) AS CNT, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(SCORE))*100000,0) / 100000, 9, 5) AS AVG, ";
        $query .= "     DECIMAL(ROUND(((FLOAT(SUM(SCORE))/FLOAT(SUM(PERFECT)))*100)*100000,0) / 100000, 9, 5) AS AVG2, ";
        $query .= "     DECIMAL(ROUND(STDDEV(FLOAT(SCORE))*10,0) / 10, 5, 1) AS STDDEV ";
        $query .= " FROM ";
        $query .= "     MOCT_T ";
        if ($div == "GASSAN_KAMOKU") {
            $query .= " WHERE ";
            $query .= "     EXISTS ( ";
            $query .= "         SELECT ";
            $query .= "             'x' ";
            $query .= "         FROM ";
            $query .= "             SCORE_DI_T EE1 ";
            $query .= "         WHERE ";
            $query .= "             EE1.SCHREGNO = MOCT_T.SCHREGNO ";
            $query .= "             AND EE1.SCORE_DIS = 0 ";
            $query .= "     ) ";
        }
        $query .= " GROUP BY ".$groupBy;

        return $query;
    }

    //模試平均データ登録
    function InsertMockAverageDat($db, $query) {

        $result = $db->query($query);

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["YEAR"][TEXT]                 = CTRL_YEAR;
            $data["MOCKCD"][TEXT]               = $row["MOCKCD"];
            $data["MOCK_SUBCLASS_CD"][TEXT]     = $row["MOCK_SUBCLASS_CD"];
            $data["AVG_DIV"][TEXT]              = $row["AVG_DIV"];
            $data["GRADE"][TEXT]                = $row["GRADE"];
            $data["HR_CLASS"][TEXT]             = $row["HR_CLASS"];
            $data["COURSECD"][TEXT]             = substr($row["COURSE"], 0, 1);
            $data["MAJORCD"][TEXT]              = substr($row["COURSE"], 1, 3);
            $data["COURSECODE"][TEXT]           = substr($row["COURSE"], 4);
            $data["SCORE"][NUMBER]              = $row["SCORE"];
            $data["SCORE_KANSAN"][NUMBER]       = $row["SCORE"];
            $data["HIGHSCORE"][NUMBER]          = $row["HIGHSCORE"];
            $data["HIGHSCORE_KANSAN"][NUMBER]   = $row["HIGHSCORE_KANSAN"];
            $data["LOWSCORE"][NUMBER]           = $row["LOWSCORE"];
            $data["LOWSCORE_KANSAN"][NUMBER]    = $row["LOWSCORE_KANSAN"];
            $data["COUNT"][NUMBER]              = $row["CNT"];
            $data["AVG"][NUMBER]                = $row["AVG"];
            $data["AVG_KANSAN"][NUMBER]         = $row["AVG2"];
            $data["STDDEV"][NUMBER]             = $row["STDDEV"];
            $data["STDDEV_KANSAN"][NUMBER]      = $row["STDDEV"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "SYSDATE()";

            $query = Query::insertSQL($data, "MOCK_AVERAGE_DAT");
            $db->query($query);
        }
        $result->free();
    }
}
?>
