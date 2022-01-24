<?php

require_once('for_php7.php');

class knje441Query extends Query {

    //A023
    function getA023() {
        $query  = " SELECT ";
        $query .= "      * ";
        $query .= " FROM ";
        $query .= "      NAME_MST ";
        $query .= " WHERE ";
        $query .= "      NAMECD1 = 'A023' ";
        $query .= "      AND NAME1 = 'H' ";

        return $query;
    }

    //教育委員会判定
    function z010Abbv1() {
        $query  = " SELECT ";
        $query .= "      ABBV1 ";
        $query .= " FROM ";
        $query .= "      NAME_MST ";
        $query .= " WHERE ";
        $query .= "      NAMECD1 = 'Z010' ";
        $query .= "      AND NAMECD2 = '00' ";

        return $query;
    }

    //教育委員会用の学校コード取得
    function getSchoolMst() {
        $db = Query::dbCheckOut();

        //教育委員会判定
        $query = knje441Query::z010Abbv1();
        $z010Abbv1 = $db->getOne($query);

        $retSchool = "";
        if ($z010Abbv1 == "1" || $z010Abbv1 == "2") {
            //V_SCHOOL_MSTから学校コードを取得
            $query  = " SELECT ";
            $query .= "      KYOUIKU_IINKAI_SCHOOLCD ";
            $query .= " FROM ";
            $query .= "      V_SCHOOL_MST ";
            $query .= " WHERE ";
            $query .= "      YEAR    = '".CTRL_YEAR."' ";

            $retSchool = $db->getOne($query);
        } else {
            $retSchool = "000000000000";
        }

        Query::dbCheckIn($db);

        return $retSchool;
    }

    //県への報告履歴コンボ
    function getReport($model) {
        $query  = " SELECT ";
        $query .= "     EXECUTE_DATE AS LABEL, ";
        $query .= "     EXECUTE_DATE AS VALUE ";
        $query .= " FROM ";
        $query .= "     REPORT_AFT_DISEASE_ADDITION441_COLLEGE_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     EXECUTE_DATE DESC ";

        return $query;
    }

    //確定コンボ
    function getFixed($model) {
        $query  = " SELECT ";
        $query .= "     FIXED_DATE AS LABEL, ";
        $query .= "     FIXED_DATE AS VALUE ";
        $query .= " FROM ";
        $query .= "     AFT_DISEASE_ADDITION441_COLLEGE_FIXED_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND YEAR = '".CTRL_YEAR."' ";
        $query .= " GROUP BY ";
        $query .= "     FIXED_DATE ";
        $query .= " ORDER BY ";
        $query .= "     FIXED_DATE DESC ";

        return $query;
    }

    //文書番号
    function getTuutatu($model) {
        $query  = " SELECT ";
        $query .= "     T1.DOC_NUMBER AS VALUE, ";
        $query .= "     T1.DOC_NUMBER AS LABEL ";
        $query .= " FROM ";
        $query .= "     AFT_SEARCH_REPORT_SCHOOL_DAT T1 ";
        $query .= "     INNER JOIN AFT_SEARCH_REPORT_DAT L1 ";
        $query .= "          ON T1.YEAR         = L1.YEAR ";
        $query .= "         AND T1.DOC_NUMBER   = L1.DOC_NUMBER ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR                 = '".CTRL_YEAR."' AND ";
        $query .= "     T1.EDBOARD_SCHOOLCD     = '".$model->schoolcd."' AND ";
        $query .= "     L1.REQUEST_ANSWER_PRG   = '".PROGRAMID."' AND ";
        $query .= "     L1.SUBMISSION_DATE IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     T1.DOC_NUMBER ";

        return $query;
    }

    //情報取得
    function getHdat($model) {
        $table = ($model->fixedData) ? "AFT_DISEASE_ADDITION441_COLLEGE_FIXED_HDAT" : "AFT_DISEASE_ADDITION441_COLLEGE_HDAT";

        $query  = " SELECT ";
        $query .= "     IDOU_DATE ";
        $query .= " FROM ";
        $query .= "     {$table} ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '{$model->schoolcd}' ";
        if ($table == "AFT_DISEASE_ADDITION441_COLLEGE_FIXED_HDAT") {
            $query .= "     AND YEAR       = '".CTRL_YEAR."' ";
            $query .= "     AND FIXED_DATE = '".str_replace("/", "-", $model->fixedData)."' ";
        } else {
            $query .= "     AND YEAR = '".CTRL_YEAR."' ";
        }

        return $query;
    }

    //UPDATE
    function &getUpdateCollegeQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //AFT_DISEASE_ADDITION441_COLLEGE_DAT
        //DELETE
        $query  = "DELETE FROM AFT_DISEASE_ADDITION441_COLLEGE_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '{$model->schoolcd}'";
        $query .= "   AND YEAR     = '".CTRL_YEAR."'";
        $db->query($query);

        $setTrDate = str_replace("/", "-", $model->field["TR_DATE"]);

        $query  = " INSERT INTO AFT_DISEASE_ADDITION441_COLLEGE_DAT ";
        $query .= " WITH SCH_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_BASE_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE BETWEEN '{$model->a023["NAMESPARE2"]}' AND '{$model->a023["NAMESPARE3"]}' AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     NOT( ";
        $query .= "      (VALUE(GRD_DIV,'') IN ('2','3','6','7') AND GRD_DATE < '{$setTrDate}') ";
        $query .= "      OR ";
        $query .= "      ENT_DATE > '{$setTrDate}' ";
        $query .= "     ) ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     '{$model->schoolcd}', ";
        $query .= "     '".CTRL_YEAR."', ";
        $query .= "     REGD.COURSECD, ";
        $query .= "     REGD.MAJORCD, ";
        $query .= "     BASE.SEX, ";
        $query .= "     '00' AS SCHOOL_SORT, ";
        $query .= "     '00' AS SCHOOL_GROUP, ";
        $query .= "     COUNT(*) AS CNT, ";
        $query .= "     cast(null as integer), ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     SCH_T ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON SCH_T.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "          AND SCH_T.SCHREGNO = REGD.SCHREGNO ";
        $query .= " GROUP BY ";
        $query .= "     REGD.COURSECD, ";
        $query .= "     REGD.MAJORCD, ";
        $query .= "     BASE.SEX ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '{$model->schoolcd}', ";
        $query .= "     '".CTRL_YEAR."', ";
        $query .= "     REGD.COURSECD, ";
        $query .= "     REGD.MAJORCD, ";
        $query .= "     BASE.SEX, ";
        $query .= "     E001.NAMESPARE1 AS SCHOOL_SORT, ";
        $query .= "     CASE ";
        $query .= "     WHEN E001.NAMESPARE1 IN ('01', '03') ";
        $query .= "          THEN COLLEGE.SCHOOL_GROUP ";
        $query .= "     WHEN E001.NAMESPARE1 = '02' AND COLLEGE.SCHOOL_GROUP = '04' ";
        $query .= "          THEN '02' ";
        $query .= "     WHEN E001.NAMESPARE1 = '02' AND COLLEGE.SCHOOL_GROUP = '05' ";
        $query .= "          THEN '03' ";
        $query .= "     ELSE '00' ";
        $query .= "     END AS SCHOOL_GROUP, ";
        $query .= "     COUNT(*) AS CNT, ";
        $query .= "     cast(null as integer), ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     SCH_T, ";
        $query .= "     AFT_GRAD_COURSE_DAT AFT ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON AFT.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT REGD ON AFT.YEAR = REGD.YEAR ";
        $query .= "          AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "          AND AFT.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     INNER JOIN COLLEGE_MST COLLEGE ON AFT.STAT_CD = COLLEGE.SCHOOL_CD ";
        $query .= "     INNER JOIN NAME_MST E001 ON E001.NAMECD1 = 'E001' ";
        $query .= "           AND COLLEGE.SCHOOL_SORT = E001.NAMECD2 ";
        $query .= "           AND E001.NAMESPARE1 IS NOT NULL ";
        $query .= "     LEFT JOIN NAME_MST E012 ON E012.NAMECD1 = 'E012' ";
        $query .= "          AND COLLEGE.SCHOOL_GROUP = E012.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     AFT.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND AFT.SCHREGNO = SCH_T.SCHREGNO ";
        $query .= "     AND AFT.SENKOU_KIND = '0' ";
        $query .= "     AND AFT.DECISION = '1' ";
        $query .= "     AND AFT.PLANSTAT = '1' ";
        $query .= " GROUP BY ";
        $query .= "     REGD.COURSECD, ";
        $query .= "     REGD.MAJORCD, ";
        $query .= "     BASE.SEX, ";
        $query .= "     E001.NAMESPARE1, ";
        $query .= "     CASE ";
        $query .= "     WHEN E001.NAMESPARE1 IN ('01', '03') ";
        $query .= "          THEN COLLEGE.SCHOOL_GROUP ";
        $query .= "     WHEN E001.NAMESPARE1 = '02' AND COLLEGE.SCHOOL_GROUP = '04' ";
        $query .= "          THEN '02' ";
        $query .= "     WHEN E001.NAMESPARE1 = '02' AND COLLEGE.SCHOOL_GROUP = '05' ";
        $query .= "          THEN '03' ";
        $query .= "     ELSE '00' ";
        $query .= "     END ";
        $query .= " ORDER BY ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     SEX, ";
        $query .= "     SCHOOL_SORT, ";
        $query .= "     SCHOOL_GROUP ";
        $db->query($query);

        //AFT_DISEASE_ADDITION441_COLLEGE_HDAT
        //DELETE
        $query  = "DELETE FROM AFT_DISEASE_ADDITION441_COLLEGE_HDAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "            AND YEAR     = '".CTRL_YEAR."'";
        $db->query($query);

        //INSERT
        $data = array(); //初期化
        $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["IDOU_DATE"][DATE]            = $model->idouDate;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            =" sysdate()";

        $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION441_COLLEGE_HDAT");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateCollege2Query($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //AFT_DISEASE_ADDITION441_COLLEGE2_DAT
        //DELETE
        $query  = "DELETE FROM AFT_DISEASE_ADDITION441_COLLEGE2_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '{$model->schoolcd}'";
        $query .= "   AND YEAR     = '".CTRL_YEAR."'";
        $db->query($query);

        $setTrDate = str_replace("/", "-", $model->field["TR_DATE"]);

        $query  = " INSERT INTO AFT_DISEASE_ADDITION441_COLLEGE2_DAT ";
        $query .= " WITH SCH_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_BASE_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE BETWEEN '{$model->a023["NAMESPARE2"]}' AND '{$model->a023["NAMESPARE3"]}' AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     NOT( ";
        $query .= "      (VALUE(GRD_DIV,'') IN ('2','3','6','7') AND GRD_DATE < '{$setTrDate}') ";
        $query .= "      OR ";
        $query .= "      ENT_DATE > '{$setTrDate}' ";
        $query .= "     ) ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     '{$model->schoolcd}', ";
        $query .= "     '".CTRL_YEAR."', ";
        $query .= "     REGD.COURSECD, ";
        $query .= "     REGD.MAJORCD, ";
        $query .= "     BASE.SEX, ";
        $query .= "     E001.NAMESPARE1 AS SCHOOL_SORT, ";
        $query .= "     COLLEGE.BUNYA, ";
        $query .= "     COUNT(*) AS CNT, ";
        $query .= "     cast(null as integer), ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     SCH_T, ";
        $query .= "     AFT_GRAD_COURSE_DAT AFT ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON AFT.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT REGD ON AFT.YEAR = REGD.YEAR ";
        $query .= "          AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "          AND AFT.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     INNER JOIN COLLEGE_MST COLLEGE ON AFT.STAT_CD = COLLEGE.SCHOOL_CD ";
        $query .= "           AND COLLEGE.BUNYA IS NOT NULL ";
        $query .= "     INNER JOIN NAME_MST E001 ON E001.NAMECD1 = 'E001' ";
        $query .= "           AND COLLEGE.SCHOOL_SORT = E001.NAMECD2 ";
        $query .= "           AND E001.NAMESPARE1 IS NOT NULL ";
        $query .= "           AND E001.NAMESPARE1 < '10' ";
        $query .= " WHERE ";
        $query .= "     AFT.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND AFT.SCHREGNO = SCH_T.SCHREGNO ";
        $query .= "     AND AFT.SENKOU_KIND = '0' ";
        $query .= "     AND AFT.DECISION = '1' ";
        $query .= "     AND AFT.PLANSTAT = '1' ";
        $query .= " GROUP BY ";
        $query .= "     REGD.COURSECD, ";
        $query .= "     REGD.MAJORCD, ";
        $query .= "     BASE.SEX, ";
        $query .= "     E001.NAMESPARE1, ";
        $query .= "     COLLEGE.BUNYA ";
        $query .= " ORDER BY ";
        $query .= "     REGD.COURSECD, ";
        $query .= "     REGD.MAJORCD, ";
        $query .= "     BASE.SEX, ";
        $query .= "     E001.NAMESPARE1, ";
        $query .= "     COLLEGE.BUNYA ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateSenmonQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //AFT_DISEASE_ADDITION441_SENMON_DAT
        //DELETE
        $query  = "DELETE FROM AFT_DISEASE_ADDITION441_SENMON_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '{$model->schoolcd}'";
        $query .= "   AND YEAR     = '".CTRL_YEAR."'";
        $db->query($query);

        $setTrDate = str_replace("/", "-", $model->field["TR_DATE"]);

        $query  = " INSERT INTO AFT_DISEASE_ADDITION441_SENMON_DAT ";
        $query .= " WITH SCH_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_BASE_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE BETWEEN '{$model->a023["NAMESPARE2"]}' AND '{$model->a023["NAMESPARE3"]}' AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     NOT( ";
        $query .= "      (VALUE(GRD_DIV,'') IN ('2','3','6','7') AND GRD_DATE < '{$setTrDate}') ";
        $query .= "      OR ";
        $query .= "      ENT_DATE > '{$setTrDate}' ";
        $query .= "     ) ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     '{$model->schoolcd}', ";
        $query .= "     '".CTRL_YEAR."', ";
        $query .= "     REGD.COURSECD, ";
        $query .= "     REGD.MAJORCD, ";
        $query .= "     BASE.SEX, ";
        $query .= "     E001.NAMESPARE1 AS SCHOOL_SORT, ";
        $query .= "     COUNT(*) AS CNT, ";
        $query .= "     cast(null as integer), ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     SCH_T, ";
        $query .= "     AFT_GRAD_COURSE_DAT AFT ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON AFT.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT REGD ON AFT.YEAR = REGD.YEAR ";
        $query .= "          AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "          AND AFT.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     INNER JOIN COLLEGE_MST COLLEGE ON AFT.STAT_CD = COLLEGE.SCHOOL_CD ";
        $query .= "           AND COLLEGE.BUNYA IS NOT NULL ";
        $query .= "     INNER JOIN NAME_MST E001 ON E001.NAMECD1 = 'E001' ";
        $query .= "           AND COLLEGE.SCHOOL_SORT = E001.NAMECD2 ";
        $query .= "           AND E001.NAMESPARE1 IS NOT NULL ";
        $query .= "           AND E001.NAMESPARE1 > '10' ";
        $query .= " WHERE ";
        $query .= "     AFT.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND AFT.SCHREGNO = SCH_T.SCHREGNO ";
        $query .= "     AND AFT.SENKOU_KIND = '0' ";
        $query .= "     AND AFT.DECISION = '1' ";
        $query .= "     AND AFT.PLANSTAT = '1' ";
        $query .= " GROUP BY ";
        $query .= "     REGD.COURSECD, ";
        $query .= "     REGD.MAJORCD, ";
        $query .= "     BASE.SEX, ";
        $query .= "     E001.NAMESPARE1 ";
        $query .= " ORDER BY ";
        $query .= "     REGD.COURSECD, ";
        $query .= "     REGD.MAJORCD, ";
        $query .= "     BASE.SEX, ";
        $query .= "     E001.NAMESPARE1 ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateSenmon2Query($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //AFT_DISEASE_ADDITION441_SENMON2_DAT
        //DELETE
        $query  = "DELETE FROM AFT_DISEASE_ADDITION441_SENMON2_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '{$model->schoolcd}'";
        $query .= "   AND YEAR     = '".CTRL_YEAR."'";
        $db->query($query);

        $setTrDate = str_replace("/", "-", $model->field["TR_DATE"]);

        $query  = " INSERT INTO AFT_DISEASE_ADDITION441_SENMON2_DAT ";
        $query .= " WITH SCH_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_BASE_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE BETWEEN '{$model->a023["NAMESPARE2"]}' AND '{$model->a023["NAMESPARE3"]}' AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     NOT( ";
        $query .= "      (VALUE(GRD_DIV,'') IN ('2','3','6','7') AND GRD_DATE < '{$setTrDate}') ";
        $query .= "      OR ";
        $query .= "      ENT_DATE > '{$setTrDate}' ";
        $query .= "     ) ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     '{$model->schoolcd}', ";
        $query .= "     '".CTRL_YEAR."', ";
        $query .= "     REGD.COURSECD, ";
        $query .= "     REGD.MAJORCD, ";
        $query .= "     BASE.SEX, ";
        $query .= "     COLLEGE.BUNYA, ";
        $query .= "     COUNT(*) AS CNT, ";
        $query .= "     cast(null as integer), ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     SCH_T, ";
        $query .= "     AFT_GRAD_COURSE_DAT AFT ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON AFT.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT REGD ON AFT.YEAR = REGD.YEAR ";
        $query .= "          AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "          AND AFT.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     INNER JOIN COLLEGE_MST COLLEGE ON AFT.STAT_CD = COLLEGE.SCHOOL_CD ";
        $query .= "           AND COLLEGE.BUNYA IS NOT NULL ";
        $query .= "     INNER JOIN NAME_MST E001 ON E001.NAMECD1 = 'E001' ";
        $query .= "           AND COLLEGE.SCHOOL_SORT = E001.NAMECD2 ";
        $query .= "           AND E001.NAMESPARE1 IS NOT NULL ";
        $query .= "           AND E001.NAMESPARE1 > '10' ";
        $query .= " WHERE ";
        $query .= "     AFT.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND AFT.SCHREGNO = SCH_T.SCHREGNO ";
        $query .= "     AND AFT.SENKOU_KIND = '0' ";
        $query .= "     AND AFT.DECISION = '1' ";
        $query .= "     AND AFT.PLANSTAT = '1' ";
        $query .= " GROUP BY ";
        $query .= "     REGD.COURSECD, ";
        $query .= "     REGD.MAJORCD, ";
        $query .= "     BASE.SEX, ";
        $query .= "     COLLEGE.BUNYA ";
        $query .= " ORDER BY ";
        $query .= "     REGD.COURSECD, ";
        $query .= "     REGD.MAJORCD, ";
        $query .= "     BASE.SEX, ";
        $query .= "     COLLEGE.BUNYA ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //確定UPDATE
    function &getFixedUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //県側・報告テーブル
        $query  = " DELETE FROM AFT_DISEASE_ADDITION441_COLLEGE_FIXED_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".CTRL_YEAR."'";
        $query .= "   AND FIXED_DATE        = '".str_replace("/", "-", $model->fixedDate)."'";
        $db->query($query);

        //INSERT
        $query = knje441Query::getCollegeDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            //キー
            $data = array(); //初期化
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $row["EDBOARD_SCHOOLCD"];
            $data["YEAR"][TEXT]                 = $row["YEAR"];
            $data["FIXED_DATE"][DATE]           = $model->fixedDate;
            $data["COURSECD"][TEXT]             = $row["COURSECD"];
            $data["MAJORCD"][TEXT]              = $row["MAJORCD"];
            $data["SEX"][TEXT]                  = $row["SEX"];
            $data["SCHOOL_SORT"][TEXT]          = $row["SCHOOL_SORT"];
            $data["SCHOOL_GROUP"][TEXT]         = $row["SCHOOL_GROUP"];
            $data["INT_VAL1"][NUMBER]           = $row["INT_VAL1"];
            $data["INT_VAL2"][NUMBER]           = $row["INT_VAL2"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION441_COLLEGE_FIXED_DAT");
            $db->query($query);

        }
        $result->free();

        //県側・報告テーブル
        $query  = " DELETE FROM AFT_DISEASE_ADDITION441_COLLEGE2_FIXED_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".CTRL_YEAR."'";
        $query .= "   AND FIXED_DATE        = '".str_replace("/", "-", $model->fixedDate)."'";
        $db->query($query);

        //INSERT
        $query = knje441Query::getCollege2Dat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            //キー
            $data = array(); //初期化
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $row["EDBOARD_SCHOOLCD"];
            $data["YEAR"][TEXT]                 = $row["YEAR"];
            $data["FIXED_DATE"][DATE]           = $model->fixedDate;
            $data["COURSECD"][TEXT]             = $row["COURSECD"];
            $data["MAJORCD"][TEXT]              = $row["MAJORCD"];
            $data["SEX"][TEXT]                  = $row["SEX"];
            $data["SCHOOL_SORT"][TEXT]          = $row["SCHOOL_SORT"];
            $data["BUNYA"][TEXT]                = $row["BUNYA"];
            $data["INT_VAL1"][NUMBER]           = $row["INT_VAL1"];
            $data["INT_VAL2"][NUMBER]           = $row["INT_VAL2"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION441_COLLEGE2_FIXED_DAT");
            $db->query($query);

        }
        $result->free();

        //県側・報告テーブル
        $query  = " DELETE FROM AFT_DISEASE_ADDITION441_SENMON_FIXED_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".CTRL_YEAR."'";
        $query .= "   AND FIXED_DATE        = '".str_replace("/", "-", $model->fixedDate)."'";
        $db->query($query);

        //INSERT
        $query = knje441Query::getSenmonDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            //キー
            $data = array(); //初期化
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $row["EDBOARD_SCHOOLCD"];
            $data["YEAR"][TEXT]                 = $row["YEAR"];
            $data["FIXED_DATE"][DATE]           = $model->fixedDate;
            $data["COURSECD"][TEXT]             = $row["COURSECD"];
            $data["MAJORCD"][TEXT]              = $row["MAJORCD"];
            $data["SEX"][TEXT]                  = $row["SEX"];
            $data["SCHOOL_SORT"][TEXT]          = $row["SCHOOL_SORT"];
            $data["INT_VAL1"][NUMBER]           = $row["INT_VAL1"];
            $data["INT_VAL2"][NUMBER]           = $row["INT_VAL2"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION441_SENMON_FIXED_DAT");
            $db->query($query);

        }
        $result->free();

        //県側・報告テーブル
        $query  = " DELETE FROM AFT_DISEASE_ADDITION441_SENMON2_FIXED_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".CTRL_YEAR."'";
        $query .= "   AND FIXED_DATE        = '".str_replace("/", "-", $model->fixedDate)."'";
        $db->query($query);

        //INSERT
        $query = knje441Query::getSenmon2Dat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            //キー
            $data = array(); //初期化
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $row["EDBOARD_SCHOOLCD"];
            $data["YEAR"][TEXT]                 = $row["YEAR"];
            $data["FIXED_DATE"][DATE]           = $model->fixedDate;
            $data["COURSECD"][TEXT]             = $row["COURSECD"];
            $data["MAJORCD"][TEXT]              = $row["MAJORCD"];
            $data["SEX"][TEXT]                  = $row["SEX"];
            $data["BUNYA"][TEXT]                = $row["BUNYA"];
            $data["INT_VAL1"][NUMBER]           = $row["INT_VAL1"];
            $data["INT_VAL2"][NUMBER]           = $row["INT_VAL2"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION441_SENMON2_FIXED_DAT");
            $db->query($query);

        }
        $result->free();

        //県側,HDATにする
        $query  = " DELETE FROM AFT_DISEASE_ADDITION441_COLLEGE_FIXED_HDAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".CTRL_YEAR."'";
        $query .= "   AND FIXED_DATE        = '".str_replace("/", "-", $model->fixedDate)."'";
        $db->query($query);

        //INSERT
        //キー
        $data = array(); //初期化
        $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["FIXED_DATE"][DATE]           = $model->fixedDate;
        $data["IDOU_DATE"][DATE]            = $model->idouDate;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            =" sysdate()";
        $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION441_COLLEGE_FIXED_HDAT");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //COLLEGE確定
    function getCollegeDat($model) {
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= "  ";
        $query .= " FROM ";
        $query .= "     AFT_DISEASE_ADDITION441_COLLEGE_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND YEAR         = '".CTRL_YEAR."' ";

        return $query;
    }

    //COLLEGE2確定
    function getCollege2Dat($model) {
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= "  ";
        $query .= " FROM ";
        $query .= "     AFT_DISEASE_ADDITION441_COLLEGE2_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND YEAR         = '".CTRL_YEAR."' ";

        return $query;
    }

    //SENMON確定
    function getSenmonDat($model) {
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= "  ";
        $query .= " FROM ";
        $query .= "     AFT_DISEASE_ADDITION441_SENMON_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND YEAR         = '".CTRL_YEAR."' ";

        return $query;
    }

    //SENMON2確定
    function getSenmon2Dat($model) {
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= "  ";
        $query .= " FROM ";
        $query .= "     AFT_DISEASE_ADDITION441_SENMON2_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND YEAR         = '".CTRL_YEAR."' ";

        return $query;
    }

    //UPDATE
    function &getUpdateReport($model) {

        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();
        $db->autoCommit(false);
        $db2->autoCommit(false);

        //県側、学校側・報告テーブル
        $query  = "DELETE FROM REPORT_AFT_DISEASE_ADDITION441_COLLEGE_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".CTRL_YEAR."'";
        $query .= "   AND EXECUTE_DATE = '".str_replace("/", "-", $model->execute_date)."' ";

        $db->query($query);
        $db2->query($query);

        //県側、学校側・報告テーブル
        //キー
        $data = array(); //初期化
        $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["EXECUTE_DATE"][DATE]         = $model->execute_date;
        $data["FIXED_DATE"][DATE]           = $model->fixedData;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            =" sysdate()";
        $query  = Query::insertSQL($data, "REPORT_AFT_DISEASE_ADDITION441_COLLEGE_DAT");
        $db->query($query);
        $db2->query($query);

        //県側、学校側・報告テーブル
        $query  = "DELETE FROM REPORT_AFT_DISEASE_ADDITION441_COLLEGE2_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".CTRL_YEAR."'";
        $query .= "   AND EXECUTE_DATE = '".str_replace("/", "-", $model->execute_date)."' ";
        $db->query($query);
        $db2->query($query);

        //県側、学校側・報告テーブル
        //キー
        $data = array(); //初期化
        $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["EXECUTE_DATE"][DATE]         = $model->execute_date;
        $data["FIXED_DATE"][DATE]           = $model->fixedData;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            =" sysdate()";
        $query  = Query::insertSQL($data, "REPORT_AFT_DISEASE_ADDITION441_COLLEGE2_DAT");
        $db->query($query);
        $db2->query($query);

        //県側、学校側・報告テーブル
        $query  = "DELETE FROM REPORT_AFT_DISEASE_ADDITION441_SENMON_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".CTRL_YEAR."'";
        $query .= "   AND EXECUTE_DATE = '".str_replace("/", "-", $model->execute_date)."' ";
        $db->query($query);
        $db2->query($query);

        //県側、学校側・報告テーブル
        //キー
        $data = array(); //初期化
        $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["EXECUTE_DATE"][DATE]         = $model->execute_date;
        $data["FIXED_DATE"][DATE]           = $model->fixedData;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            =" sysdate()";
        $query  = Query::insertSQL($data, "REPORT_AFT_DISEASE_ADDITION441_SENMON_DAT");
        $db->query($query);
        $db2->query($query);

        //県側、学校側・報告テーブル
        $query  = "DELETE FROM REPORT_AFT_DISEASE_ADDITION441_SENMON2_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".CTRL_YEAR."'";
        $query .= "   AND EXECUTE_DATE = '".str_replace("/", "-", $model->execute_date)."' ";
        $db->query($query);
        $db2->query($query);

        //県側、学校側・報告テーブル
        //キー
        $data = array(); //初期化
        $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["EXECUTE_DATE"][DATE]         = $model->execute_date;
        $data["FIXED_DATE"][DATE]           = $model->fixedData;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            =" sysdate()";
        $query  = Query::insertSQL($data, "REPORT_AFT_DISEASE_ADDITION441_SENMON2_DAT");
        $db->query($query);
        $db2->query($query);

        //DELETE
        //県側
        $query  = "DELETE FROM AFT_DISEASE_ADDITION441_COLLEGE_FIXED_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".CTRL_YEAR."'";
        $query .= "   AND FIXED_DATE        = '".str_replace("/", "-", $model->fixedData)."' ";
        $db2->query($query);

        //INSERT
        //学校側から県側へコピーするデータ取得
        $query = knje441Query::get441CollegeFixedDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //県側・疾病等結果一覧テーブル
            //キー
            $data = array(); //初期化
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $row["EDBOARD_SCHOOLCD"];
            $data["YEAR"][TEXT]                 = $row["YEAR"];
            $data["FIXED_DATE"][DATE]           = $row["FIXED_DATE"];
            $data["COURSECD"][TEXT]             = $row["COURSECD"];
            $data["MAJORCD"][TEXT]              = $row["MAJORCD"];
            $data["SEX"][TEXT]                  = $row["SEX"];
            $data["SCHOOL_SORT"][TEXT]          = $row["SCHOOL_SORT"];
            $data["SCHOOL_GROUP"][TEXT]         = $row["SCHOOL_GROUP"];
            $data["INT_VAL1"][NUMBER]           = $row["INT_VAL1"];
            $data["INT_VAL2"][NUMBER]           = $row["INT_VAL2"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION441_COLLEGE_FIXED_DAT");
            $db2->query($query);
        }
        $result->free();

        //DELETE
        //県側
        $query  = "DELETE FROM AFT_DISEASE_ADDITION441_COLLEGE2_FIXED_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".CTRL_YEAR."'";
        $query .= "   AND FIXED_DATE        = '".str_replace("/", "-", $model->fixedData)."' ";
        $db2->query($query);

        //INSERT
        //学校側から県側へコピーするデータ取得
        $query = knje441Query::get441College2FixedDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //県側・疾病等結果一覧テーブル
            //キー
            $data = array(); //初期化
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $row["EDBOARD_SCHOOLCD"];
            $data["YEAR"][TEXT]                 = $row["YEAR"];
            $data["FIXED_DATE"][DATE]           = $row["FIXED_DATE"];
            $data["COURSECD"][TEXT]             = $row["COURSECD"];
            $data["MAJORCD"][TEXT]              = $row["MAJORCD"];
            $data["SEX"][TEXT]                  = $row["SEX"];
            $data["SCHOOL_SORT"][TEXT]          = $row["SCHOOL_SORT"];
            $data["BUNYA"][TEXT]                = $row["BUNYA"];
            $data["INT_VAL1"][NUMBER]           = $row["INT_VAL1"];
            $data["INT_VAL2"][NUMBER]           = $row["INT_VAL2"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION441_COLLEGE2_FIXED_DAT");
            $db2->query($query);
        }
        $result->free();

        //DELETE
        //県側
        $query  = "DELETE FROM AFT_DISEASE_ADDITION441_SENMON_FIXED_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".CTRL_YEAR."'";
        $query .= "   AND FIXED_DATE        = '".str_replace("/", "-", $model->fixedData)."' ";
        $db2->query($query);

        //INSERT
        //学校側から県側へコピーするデータ取得
        $query = knje441Query::get441SenmonFixedDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //県側・疾病等結果一覧テーブル
            //キー
            $data = array(); //初期化
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $row["EDBOARD_SCHOOLCD"];
            $data["YEAR"][TEXT]                 = $row["YEAR"];
            $data["FIXED_DATE"][DATE]           = $row["FIXED_DATE"];
            $data["COURSECD"][TEXT]             = $row["COURSECD"];
            $data["MAJORCD"][TEXT]              = $row["MAJORCD"];
            $data["SEX"][TEXT]                  = $row["SEX"];
            $data["SCHOOL_SORT"][TEXT]          = $row["SCHOOL_SORT"];
            $data["INT_VAL1"][NUMBER]           = $row["INT_VAL1"];
            $data["INT_VAL2"][NUMBER]           = $row["INT_VAL2"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION441_SENMON_FIXED_DAT");
            $db2->query($query);
        }
        $result->free();

        //DELETE
        //県側
        $query  = "DELETE FROM AFT_DISEASE_ADDITION441_SENMON2_FIXED_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".CTRL_YEAR."'";
        $query .= "   AND FIXED_DATE        = '".str_replace("/", "-", $model->fixedData)."' ";
        $db2->query($query);

        //INSERT
        //学校側から県側へコピーするデータ取得
        $query = knje441Query::get441Senmon2FixedDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //県側・疾病等結果一覧テーブル
            //キー
            $data = array(); //初期化
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $row["EDBOARD_SCHOOLCD"];
            $data["YEAR"][TEXT]                 = $row["YEAR"];
            $data["FIXED_DATE"][DATE]           = $row["FIXED_DATE"];
            $data["COURSECD"][TEXT]             = $row["COURSECD"];
            $data["MAJORCD"][TEXT]              = $row["MAJORCD"];
            $data["SEX"][TEXT]                  = $row["SEX"];
            $data["BUNYA"][TEXT]                = $row["BUNYA"];
            $data["INT_VAL1"][NUMBER]           = $row["INT_VAL1"];
            $data["INT_VAL2"][NUMBER]           = $row["INT_VAL2"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION441_SENMON2_FIXED_DAT");
            $db2->query($query);
        }
        $result->free();

        //報告処理
        $data = array();
        $data["ANSWER_FLG"][TEXT]      = "1";
        $data["ANSWER_DATE"][FUNC]     = "SYSDATE()";
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "SYSDATE()";

        $where  = " WHERE ";
        $where .= "     YEAR             = '".CTRL_YEAR."' AND ";
        $where .= "     DOC_NUMBER       = {$model->docNumber} AND ";
        $where .= "     EDBOARD_SCHOOLCD = '{$model->schoolcd}' ";

        $query = Query::updateSQL($data, "AFT_SEARCH_REPORT_SCHOOL_DAT", $where);
        $db2->query($query);

        $db->commit();
        $db2->commit();
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        return;
    }

    //学校側から県側へコピーするデータ取得
    function get441CollegeFixedDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     AFT_DISEASE_ADDITION441_COLLEGE_FIXED_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND FIXED_DATE   = '".str_replace("/", "-", $model->fixedData)."' ";

        return $query;
    }

    //学校側から県側へコピーするデータ取得
    function get441College2FixedDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     AFT_DISEASE_ADDITION441_COLLEGE2_FIXED_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND FIXED_DATE   = '".str_replace("/", "-", $model->fixedData)."' ";

        return $query;
    }

    //学校側から県側へコピーするデータ取得
    function get441SenmonFixedDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     AFT_DISEASE_ADDITION441_SENMON_FIXED_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND FIXED_DATE   = '".str_replace("/", "-", $model->fixedData)."' ";

        return $query;
    }

    //学校側から県側へコピーするデータ取得
    function get441Senmon2FixedDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     AFT_DISEASE_ADDITION441_SENMON2_FIXED_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND FIXED_DATE   = '".str_replace("/", "-", $model->fixedData)."' ";

        return $query;
    }

    //課程学科性別取得
    function getCsvMajor($model, $table) {

        $query .= " SELECT DISTINCT ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     MAJOR.MAJORNAME, ";
        $query .= "     Z002.NAMECD2 AS SEX, ";
        $query .= "     Z002.ABBV1 ";
        $query .= " FROM ";
        $query .= "     {$table} T1 ";
        $query .= "     LEFT JOIN MAJOR_MST MAJOR ON T1.COURSECD = MAJOR.COURSECD ";
        $query .= "          AND T1.MAJORCD = MAJOR.MAJORCD ";
        $query .= "     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ";
        $query .= " WHERE ";
        $query .= "     T1.EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND T1.YEAR = '".CTRL_YEAR."' ";
        if ($table == "AFT_DISEASE_ADDITION441_COLLEGE_FIXED_HDAT") {
            $query .= "     AND T1.FIXED_DATE   = '".str_replace("/", "-", $model->fixedData)."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     Z002.NAMECD2 ";

        return $query;
    }

    //学校側から県側へコピーするデータ取得
    function getNameMst($namecd1) {
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '{$namecd1}' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //CSV
    function getCollegeCnt($model, $msVal, $dataVal) {
        $table = ($model->fixedData) ? "AFT_DISEASE_ADDITION441_COLLEGE_FIXED_DAT" : "AFT_DISEASE_ADDITION441_COLLEGE_DAT";

        $query  = " SELECT ";
        $query .= "     SUM(VALUE(T1.INT_VAL1, 0)) AS INT_VAL1 ";
        $query .= " FROM ";
        $query .= "     {$table} T1 ";
        $query .= " WHERE ";
        $query .= "     T1.EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND T1.YEAR = '".CTRL_YEAR."' ";
        if ($table == "AFT_DISEASE_ADDITION441_COLLEGE_FIXED_HDAT") {
            $query .= "     AND T1.FIXED_DATE   = '".str_replace("/", "-", $model->fixedData)."' ";
        }
        $query .= "     AND T1.COURSECD = '{$msVal["COURSECD"]}' ";
        $query .= "     AND T1.MAJORCD = '{$msVal["MAJORCD"]}' ";
        $query .= "     AND T1.SEX = '{$msVal["SEX"]}' ";
        $query .= "     AND T1.SCHOOL_SORT = '{$dataVal["SCHOOL_SORT"]}' ";
        $query .= "     AND T1.SCHOOL_GROUP = '{$dataVal["SCHOOL_GROUP"]}' ";

        return $query;
    }

    //CSV
    function getCollege2Cnt($model, $msVal, $dataVal, $e050Val) {
        $table = ($model->fixedData) ? "AFT_DISEASE_ADDITION441_COLLEGE2_FIXED_DAT" : "AFT_DISEASE_ADDITION441_COLLEGE2_DAT";

        $query  = " SELECT ";
        $query .= "     SUM(VALUE(T1.INT_VAL1, 0)) AS INT_VAL1 ";
        $query .= " FROM ";
        $query .= "     {$table} T1 ";
        $query .= " WHERE ";
        $query .= "     T1.EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND T1.YEAR = '".CTRL_YEAR."' ";
        if ($table == "AFT_DISEASE_ADDITION441_COLLEGE_FIXED_HDAT") {
            $query .= "     AND T1.FIXED_DATE   = '".str_replace("/", "-", $model->fixedData)."' ";
        }
        $query .= "     AND T1.COURSECD = '{$msVal["COURSECD"]}' ";
        $query .= "     AND T1.MAJORCD = '{$msVal["MAJORCD"]}' ";
        $query .= "     AND T1.SEX = '{$msVal["SEX"]}' ";
        $query .= "     AND T1.SCHOOL_SORT = '{$dataVal["SCHOOL_SORT"]}' ";
        $query .= "     AND T1.BUNYA = '{$e050Val["NAMECD2"]}' ";

        return $query;
    }

    //CSV
    function getSenmonCnt($model, $msVal, $dataVal) {
        $table = ($model->fixedData) ? "AFT_DISEASE_ADDITION441_SENMON_FIXED_DAT" : "AFT_DISEASE_ADDITION441_SENMON_DAT";

        $query  = " SELECT ";
        $query .= "     SUM(VALUE(T1.INT_VAL1, 0)) AS INT_VAL1 ";
        $query .= " FROM ";
        $query .= "     {$table} T1 ";
        $query .= " WHERE ";
        $query .= "     T1.EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND T1.YEAR = '".CTRL_YEAR."' ";
        if ($table == "AFT_DISEASE_ADDITION441_COLLEGE_FIXED_HDAT") {
            $query .= "     AND T1.FIXED_DATE   = '".str_replace("/", "-", $model->fixedData)."' ";
        }
        $query .= "     AND T1.COURSECD = '{$msVal["COURSECD"]}' ";
        $query .= "     AND T1.MAJORCD = '{$msVal["MAJORCD"]}' ";
        $query .= "     AND T1.SEX = '{$msVal["SEX"]}' ";
        $query .= "     AND T1.SCHOOL_SORT = '{$dataVal["SCHOOL_SORT"]}' ";

        return $query;
    }

    //CSV
    function getSenmon2Cnt($model, $msVal, $e051Val) {
        $table = ($model->fixedData) ? "AFT_DISEASE_ADDITION441_SENMON2_FIXED_DAT" : "AFT_DISEASE_ADDITION441_SENMON2_DAT";

        $query  = " SELECT ";
        $query .= "     SUM(VALUE(T1.INT_VAL1, 0)) AS INT_VAL1 ";
        $query .= " FROM ";
        $query .= "     {$table} T1 ";
        $query .= " WHERE ";
        $query .= "     T1.EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND T1.YEAR = '".CTRL_YEAR."' ";
        if ($table == "AFT_DISEASE_ADDITION441_COLLEGE_FIXED_HDAT") {
            $query .= "     AND T1.FIXED_DATE   = '".str_replace("/", "-", $model->fixedData)."' ";
        }
        $query .= "     AND T1.COURSECD = '{$msVal["COURSECD"]}' ";
        $query .= "     AND T1.MAJORCD = '{$msVal["MAJORCD"]}' ";
        $query .= "     AND T1.SEX = '{$msVal["SEX"]}' ";
        $query .= "     AND T1.BUNYA = '{$e051Val["NAMECD2"]}' ";

        return $query;
    }
}
?>
