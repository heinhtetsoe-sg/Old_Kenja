<?php

require_once('for_php7.php');


class knje065iQuery extends Query {

    //校種取得
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /* 実行 */

    //評定平均値・席次データ更新
    function updSchregStudyrecRankClassDat($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query = knje065iQuery::delSchregStudyrecRankClassDat($model);
        $db->query($query);

        //教科別評定平均値（過年度含む）
        $classDiv = "1";
        $query = knje065iQuery::getSchregStudyrecRankClassDat($model, $classDiv);
        knje065iQuery::insSchregStudyrecRankClassDat($db, $query, $model, $classDiv);

        //全科目の評定平均値（過年度含む）
        $classDiv = "9";
        $query = knje065iQuery::getSchregStudyrecRankClassDat($model, $classDiv);
        knje065iQuery::insSchregStudyrecRankClassDat($db, $query, $model, $classDiv);

        $db->commit();
        Query::dbCheckIn($db);
    }

    function delSchregStudyrecRankClassDat($model) {
        $query  = " DELETE FROM ";
        $query .= "      SCHREG_STUDYREC_RANK_CLASS_DAT T1 ";
        $query .= " WHERE ";
        $query .= "      T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "      AND T1.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "      AND EXISTS( ";
        $query .= "              SELECT ";
        $query .= "                  'x' ";
        $query .= "              FROM ";
        $query .= "                  SCHREG_REGD_DAT E1 ";
        $query .= "              WHERE ";
        $query .= "                  E1.SCHREGNO = T1.SCHREGNO ";
        $query .= "                  AND E1.YEAR = '".CTRL_YEAR."' ";
        $query .= "                  AND E1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "                  AND E1.GRADE = '".$model->field["GRADE"]."' ";
        $query .= "          ) ";

        return $query;
    }

    function getSchregStudyrecRankClassDat($model, $classDiv) {
        $query  = "";
        $query .= " WITH T_SCHNO AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         COURSECD, ";
        $query .= "         MAJORCD, ";
        $query .= "         COURSECODE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "         AND GRADE = '".$model->field["GRADE"]."' ";
        $query .= " ) ";

        $query .= " , DROP_YEAR AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         YEAR ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO IN (SELECT SCHREGNO FROM T_SCHNO) ";
        $query .= "         AND YEAR <= '".CTRL_YEAR."' ";
        $query .= "         AND (SCHREGNO, YEAR) NOT IN ( ";
        $query .= "             SELECT ";
        $query .= "                 SCHREGNO, ";
        $query .= "                 MAX(YEAR) ";
        $query .= "             FROM ";
        $query .= "                 SCHREG_REGD_DAT ";
        $query .= "             WHERE ";
        $query .= "                 SCHREGNO IN (SELECT SCHREGNO FROM T_SCHNO) ";
        $query .= "                 AND YEAR <= '".CTRL_YEAR."' ";
        $query .= "             GROUP BY ";
        $query .= "                 SCHREGNO, ";
        $query .= "                 GRADE ";
        $query .= "         ) ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        $query .= "         YEAR ";
        $query .= " ) ";

        $query .= " , T_STUDYREC AS ( ";
        $query .= "     SELECT ";
        if ($classDiv == "1") {
            $query .= "         T1.CLASSCD, ";
        } else {
            $query .= "         '00' AS CLASSCD, ";
        }
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T2.GRADE, ";
        $query .= "         T2.HR_CLASS, ";
        $query .= "         T2.COURSECD, ";
        $query .= "         T2.MAJORCD, ";
        $query .= "         T2.COURSECODE, ";
        $query .= "         SUM(T1.VALUATION) AS SCORE, ";
        $query .= "         DECIMAL(ROUND(AVG(FLOAT(T1.VALUATION))*100000,0)/100000,9,5) AS AVG, ";
        $query .= "         COUNT(T1.VALUATION) AS COUNT ";
        $query .= "     FROM ";
        $query .= "         SCHREG_STUDYREC_DAT T1 ";
        $query .= "         LEFT JOIN T_SCHNO T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR <= '".CTRL_YEAR."' ";
        $query .= "         AND T1.SCHREGNO IN (SELECT SCHREGNO FROM T_SCHNO) ";
        $query .= "         AND T1.CLASSCD < '90' ";
        $query .= "         AND T1.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "         AND (T1.SCHREGNO, T1.YEAR) NOT IN (SELECT SCHREGNO, YEAR FROM DROP_YEAR) ";
        $query .= "         AND T1.VALUATION >= 1 ";
        $query .= "         AND T1.VALUATION <= 5 ";
        $query .= "     GROUP BY ";
        if ($classDiv == "1") {
            $query .= "         T1.CLASSCD, ";
        }
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T2.GRADE, ";
        $query .= "         T2.HR_CLASS, ";
        $query .= "         T2.COURSECD, ";
        $query .= "         T2.MAJORCD, ";
        $query .= "         T2.COURSECODE ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SCORE, ";
        $query .= "     T1.AVG, ";
        $query .= "     T1.COUNT, ";

        $query .= "     RANK() OVER(PARTITION BY T1.CLASSCD, T1.SCHOOL_KIND, T1.GRADE ORDER BY T1.SCORE DESC) AS GRADE_SCORE_RANK, ";
        $query .= "     RANK() OVER(PARTITION BY T1.CLASSCD, T1.SCHOOL_KIND, T1.GRADE, T1.HR_CLASS ORDER BY T1.SCORE DESC) AS CLASS_SCORE_RANK, ";
        $query .= "     RANK() OVER(PARTITION BY T1.CLASSCD, T1.SCHOOL_KIND, T1.GRADE, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ORDER BY T1.SCORE DESC) AS COURSE_SCORE_RANK, ";
        $query .= "     RANK() OVER(PARTITION BY T1.CLASSCD, T1.SCHOOL_KIND, T1.GRADE, T1.COURSECD, T1.MAJORCD ORDER BY T1.SCORE DESC) AS MAJOR_SCORE_RANK, ";

        $query .= "     RANK() OVER(PARTITION BY T1.CLASSCD, T1.SCHOOL_KIND, T1.GRADE ORDER BY T1.AVG DESC) AS GRADE_AVG_RANK, ";
        $query .= "     RANK() OVER(PARTITION BY T1.CLASSCD, T1.SCHOOL_KIND, T1.GRADE, T1.HR_CLASS ORDER BY T1.AVG DESC) AS CLASS_AVG_RANK, ";
        $query .= "     RANK() OVER(PARTITION BY T1.CLASSCD, T1.SCHOOL_KIND, T1.GRADE, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ORDER BY T1.AVG DESC) AS COURSE_AVG_RANK, ";
        $query .= "     RANK() OVER(PARTITION BY T1.CLASSCD, T1.SCHOOL_KIND, T1.GRADE, T1.COURSECD, T1.MAJORCD ORDER BY T1.AVG DESC) AS MAJOR_AVG_RANK ";
        $query .= " FROM ";
        $query .= "     T_STUDYREC T1 ";
        $query .= " ORDER BY ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.AVG DESC ";

        return $query;
    }

    function insSchregStudyrecRankClassDat($db, $query, $model, $classDiv) {
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            foreach ($model->rankDivArray as $key => $val) {
                $data = array();
                $data["YEAR"][TEXT]             = CTRL_YEAR;
                $data["CLASS_DIV"][TEXT]        = $classDiv;
                $data["CLASSCD"][TEXT]          = $row["CLASSCD"];
                $data["SCHOOL_KIND"][TEXT]      = $row["SCHOOL_KIND"];
                $data["SCHREGNO"][TEXT]         = $row["SCHREGNO"];
                $data["RANK_DIV"][TEXT]         = $key;
                $data["SCORE"][NUMBER]          = $row["SCORE"];
                $data["AVG"][NUMBER]            = $row["AVG"];
                $data["COUNT"][NUMBER]          = $row["COUNT"];
                $data["SCORE_RANK"][NUMBER]     = $row[$val."_SCORE_RANK"];
                $data["AVG_RANK"][NUMBER]       = $row[$val."_AVG_RANK"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "SYSDATE()";

                $query = Query::insertSQL($data, "SCHREG_STUDYREC_RANK_CLASS_DAT");
                $db->query($query);
            }
        }
        $result->free();
    }

    //評定平均値・平均データ更新
    //評定平均値・席次データから算出
    function updSchregStudyrecAvgClassDat($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query = knje065iQuery::delSchregStudyrecAvgClassDat($model);
        $db->query($query);

        //教科別評定平均値（過年度含む）（平均値から算出）
        $classDiv = "1";
        $query = knje065iQuery::getSchregStudyrecAvgClassDat($model, $classDiv);
        knje065iQuery::insSchregStudyrecAvgClassDat($db, $query, $model, $classDiv);

        //全科目の評定平均値（過年度含む）（平均値から算出）
        $classDiv = "9";
        $query = knje065iQuery::getSchregStudyrecAvgClassDat($model, $classDiv);
        knje065iQuery::insSchregStudyrecAvgClassDat($db, $query, $model, $classDiv);

        //全科目の評定平均値（過年度含む）（合計値から算出）
        $classDiv = "8";
        $query = knje065iQuery::getSchregStudyrecAvgClassDat($model, $classDiv);
        knje065iQuery::insSchregStudyrecAvgClassDat($db, $query, $model, $classDiv);

        $db->commit();
        Query::dbCheckIn($db);
    }

    function delSchregStudyrecAvgClassDat($model) {
        $query  = " DELETE FROM ";
        $query .= "      SCHREG_STUDYREC_AVG_CLASS_DAT T1 ";
        $query .= " WHERE ";
        $query .= "      T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "      AND T1.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."'";
        $query .= "      AND T1.GRADE = '".$model->field["GRADE"]."'";

        return $query;
    }

    function getSchregStudyrecAvgClassDat($model, $classDiv) {
        $query  = "";
        $query .= " WITH T_SCHNO AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         COURSECD, ";
        $query .= "         MAJORCD, ";
        $query .= "         COURSECODE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "         AND GRADE = '".$model->field["GRADE"]."' ";
        $query .= " ) ";

        $query .= " , T_STUDYREC AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.CLASSCD, ";
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T2.GRADE, ";
        $query .= "         T2.HR_CLASS, ";
        $query .= "         T2.COURSECD, ";
        $query .= "         T2.MAJORCD, ";
        $query .= "         T2.COURSECODE, ";
        if ($classDiv == "8") {
            $query .= "         T1.SCORE AS VALUE, ";
        } else {
            $query .= "         T1.AVG AS VALUE, ";
        }
        $query .= "         T1.SCORE, ";
        $query .= "         T1.AVG, ";
        $query .= "         T1.COUNT ";
        $query .= "     FROM ";
        $query .= "         SCHREG_STUDYREC_RANK_CLASS_DAT T1 ";
        $query .= "         LEFT JOIN T_SCHNO T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        if ($classDiv == "8") {
            $query .= "         AND T1.CLASS_DIV = '9' ";
        } else {
            $query .= "         AND T1.CLASS_DIV = '{$classDiv}' ";
        }
        $query .= "         AND T1.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "         AND T1.SCHREGNO IN (SELECT SCHREGNO FROM T_SCHNO) ";
        $query .= "         AND T1.RANK_DIV = '1' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     '1' AS AVG_DIV, ";
        $query .= "     T1.GRADE, ";
        $query .= "     '000' AS HR_CLASS, ";
        $query .= "     '0' AS COURSECD, ";
        $query .= "     '000' AS MAJORCD, ";
        $query .= "     '0000' AS COURSECODE, ";
        $query .= "     SUM(T1.VALUE) AS SCORE, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(T1.VALUE))*100000,0)/100000,9,5) AS AVG, ";
        $query .= "     COUNT(T1.VALUE) AS COUNT ";
        $query .= " FROM ";
        $query .= "     T_STUDYREC T1 ";
        $query .= " GROUP BY ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.GRADE ";

        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     '2' AS AVG_DIV, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     '0' AS COURSECD, ";
        $query .= "     '000' AS MAJORCD, ";
        $query .= "     '0000' AS COURSECODE, ";
        $query .= "     SUM(T1.VALUE) AS SCORE, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(T1.VALUE))*100000,0)/100000,9,5) AS AVG, ";
        $query .= "     COUNT(T1.VALUE) AS COUNT ";
        $query .= " FROM ";
        $query .= "     T_STUDYREC T1 ";
        $query .= " GROUP BY ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS ";

        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     '3' AS AVG_DIV, ";
        $query .= "     T1.GRADE, ";
        $query .= "     '000' AS HR_CLASS, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     SUM(T1.VALUE) AS SCORE, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(T1.VALUE))*100000,0)/100000,9,5) AS AVG, ";
        $query .= "     COUNT(T1.VALUE) AS COUNT ";
        $query .= " FROM ";
        $query .= "     T_STUDYREC T1 ";
        $query .= " GROUP BY ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE ";

        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     '4' AS AVG_DIV, ";
        $query .= "     T1.GRADE, ";
        $query .= "     '000' AS HR_CLASS, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     '0000' AS COURSECODE, ";
        $query .= "     SUM(T1.VALUE) AS SCORE, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(T1.VALUE))*100000,0)/100000,9,5) AS AVG, ";
        $query .= "     COUNT(T1.VALUE) AS COUNT ";
        $query .= " FROM ";
        $query .= "     T_STUDYREC T1 ";
        $query .= " GROUP BY ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD ";

        $query .= " ORDER BY ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     AVG_DIV ";

        return $query;
    }

    function insSchregStudyrecAvgClassDat($db, $query, $model, $classDiv) {
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["CLASS_DIV"][TEXT]        = $classDiv;
            $data["CLASSCD"][TEXT]          = $row["CLASSCD"];
            $data["SCHOOL_KIND"][TEXT]      = $row["SCHOOL_KIND"];
            $data["AVG_DIV"][TEXT]          = $row["AVG_DIV"];
            $data["GRADE"][TEXT]            = $row["GRADE"];
            $data["HR_CLASS"][TEXT]         = $row["HR_CLASS"];
            $data["COURSECD"][TEXT]         = $row["COURSECD"];
            $data["MAJORCD"][TEXT]          = $row["MAJORCD"];
            $data["COURSECODE"][TEXT]       = $row["COURSECODE"];
            $data["SCORE"][NUMBER]          = $row["SCORE"];
            $data["AVG"][NUMBER]            = $row["AVG"];
            $data["COUNT"][NUMBER]          = $row["COUNT"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $query = Query::insertSQL($data, "SCHREG_STUDYREC_AVG_CLASS_DAT");
            $db->query($query);
        }
        $result->free();
    }

}
?>
