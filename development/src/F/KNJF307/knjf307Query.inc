<?php

require_once('for_php7.php');

class knjf307Query extends Query {

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
        $query = knjf307Query::z010Abbv1();
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

    //文書番号
    function getTuutatu($model) {

        $query  = " SELECT ";
        $query .= "     T1.DOC_NUMBER AS VALUE, ";
        $query .= "     T1.DOC_NUMBER AS LABEL ";
        $query .= " FROM ";
        $query .= "     AFT_SEARCH_REPORT_SCHOOL_DAT T1 ";
        $query .= "     INNER JOIN AFT_SEARCH_REPORT_DAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "           AND T1.DOC_NUMBER = L1.DOC_NUMBER ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR               = '".CTRL_YEAR."' ";
        $query .= "     AND T1.EDBOARD_SCHOOLCD   = '{$model->schoolcd}' ";
        $query .= "     AND L1.REQUEST_ANSWER_PRG = 'KNJF307' ";
        $query .= "     AND L1.SUBMISSION_DATE IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     T1.DOC_NUMBER ";

        return $query;
    }

    //情報取得
    function getRow($model) {
        $table = ($model->fixedData) ? "MEDEXAM_DISEASE_ADDITION307_FIXED_HDAT" : "MEDEXAM_DISEASE_ADDITION307_HDAT";

        $query  = " SELECT ";
        $query .= "     IDOU_DATE ";
        $query .= " FROM ";
        $query .= "     {$table} ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '{$model->schoolcd}' ";
        if ($table == "MEDEXAM_DISEASE_ADDITION307_FIXED_HDAT") {
            $query .= "     AND YEAR       = '".CTRL_YEAR."' ";
            $query .= "     AND FIXED_DATE = '".str_replace("/", "-", $model->fixedData)."' ";
        } else {
            $query .= "     AND YEAR = (SELECT DISTINCT ";
            $query .= "                     MAX(YEAR)  ";
            $query .= "                 FROM  ";
            $query .= "                     MEDEXAM_DISEASE_ADDITION307_HDAT ";
            $query .= "                 WHERE  ";
            $query .= "                     EDBOARD_SCHOOLCD = '{$model->schoolcd}' ";
            $query .= "                 ) ";
        }

        return $query;
    }

    //UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //MEDEXAM_DISEASE_ADDITION307_DAT
        //DELETE
        $query  = "DELETE FROM MEDEXAM_DISEASE_ADDITION307_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."' ";
        $query .= "   AND YEAR     = '".CTRL_YEAR."' ";
        $db->query($query);

        //INSERT
        $query = knjf307Query::getMedexamDetDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array(); //初期化
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
            $data["DATA_DIV"][TEXT]             = $row["DATA_DIV"];
            $data["YEAR"][TEXT]                 = CTRL_YEAR;
            $data["COURSECD"][TEXT]             = $row["COURSECD"];
            $data["MAJORCD"][TEXT]              = $row["MAJORCD"];
            $data["AGE"][NUMBER]                = $row["AGE"];
            $data["SEX"][TEXT]                  = $row["SEX"];
            $data["SEQ"][TEXT]                  = $row["SEQ"];
            $data["INT_VAL1"][NUMBER]           = $row["INT_VAL1"];
            $data["INT_VAL2"][NUMBER]           = $row["INT_VAL2"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";

            $query  = Query::insertSQL($data, "MEDEXAM_DISEASE_ADDITION307_DAT");
            $db->query($query);
        }

        //MEDEXAM_DISEASE_ADDITION307_HDAT
        //DELETE
        $query  = "DELETE FROM MEDEXAM_DISEASE_ADDITION307_HDAT ";
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

        $query  = Query::insertSQL($data, "MEDEXAM_DISEASE_ADDITION307_HDAT");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //確定UPDATE
    function &getFixedUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //県側、学校側・報告テーブル
        $query  = " DELETE FROM MEDEXAM_DISEASE_ADDITION307_FIXED_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".CTRL_YEAR."'";
        $query .= "   AND FIXED_DATE        = '".str_replace("/", "-", $model->fixedDate)."'";
        $db->query($query);

        //INSERT
        $query = knjf307Query::getMedexamDetDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            //キー
            $data = array(); //初期化
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
            $data["DATA_DIV"][TEXT]             = $row["DATA_DIV"];
            $data["FIXED_DATE"][DATE]           = $model->fixedDate;
            $data["YEAR"][TEXT]                 = CTRL_YEAR;
            $data["COURSECD"][TEXT]             = $row["COURSECD"];
            $data["MAJORCD"][TEXT]              = $row["MAJORCD"];
            $data["AGE"][NUMBER]                = $row["AGE"];
            $data["SEX"][TEXT]                  = $row["SEX"];
            $data["SEQ"][TEXT]                  = $row["SEQ"];
            $data["INT_VAL1"][NUMBER]           = $row["INT_VAL1"];
            $data["INT_VAL2"][NUMBER]           = $row["INT_VAL2"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";
            $query  = Query::insertSQL($data, "MEDEXAM_DISEASE_ADDITION307_FIXED_DAT");
            $db->query($query);

        }
        $result->free();

        //県側、学校側,HDATにする
        $query  = " DELETE FROM MEDEXAM_DISEASE_ADDITION307_FIXED_HDAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".CTRL_YEAR."'";
        $query .= "   AND FIXED_DATE        = '".str_replace("/", "-", $model->fixedDate)."'";
        $db->query($query);

        //INSERT
        //キー
        $data = array(); //初期化
        $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["IDOU_DATE"][DATE]            = $model->idouDate;
        $data["FIXED_DATE"][DATE]           = $model->fixedDate;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            =" sysdate()";
        $query  = Query::insertSQL($data, "MEDEXAM_DISEASE_ADDITION307_FIXED_HDAT");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //県への報告履歴コンボ
    function getReport($model) {
        $query  = " SELECT ";
        $query .= "     EXECUTE_DATE AS LABEL, ";
        $query .= "     EXECUTE_DATE AS VALUE ";
        $query .= " FROM ";
        $query .= "     REPORT_MEDEXAM_DISEASE_ADDITION307_DAT ";
        $query .= " WHERE ";
        $query .= "         EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND YEAR             = '".CTRL_YEAR."' ";
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
        $query .= "     MEDEXAM_DISEASE_ADDITION307_FIXED_DAT ";
        $query .= " WHERE ";
        $query .= "         EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND YEAR             = '".CTRL_YEAR."' ";
        $query .= " GROUP BY ";
        $query .= "     FIXED_DATE ";
        $query .= " ORDER BY ";
        $query .= "     FIXED_DATE DESC ";

        return $query;
    }

    //学校側から県側へコピーするデータ取得
    function getMedexamDetDat($model) {
        $query  = " WITH REGDEXIST AS ( ";
        $query .= "             SELECT DISTINCT ";
        $query .= "                 T1.SCHREGNO ";
        $query .= "             FROM ";
        $query .= "                 SCHREG_REGD_DAT T1, ";
        $query .= "                 SCHREG_BASE_MST T2 ";
        $query .= "             WHERE ";
        $query .= "                 T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "                 AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "             AND T1.GRADE IN (SELECT ";
            $query .= "                                  GRADE ";
            $query .= "                              FROM ";
            $query .= "                                  SCHREG_REGD_GDAT ";
            $query .= "                              WHERE ";
            $query .= "                                      YEAR        = '".CTRL_YEAR."' ";
            $query .= "                                  AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= "                              ) ";
        }
        $query .= "                 AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "                 AND NOT ( ";
        $query .= "                  (VALUE(GRD_DIV,'') IN ('2','3','6','7') AND GRD_DATE < '".CTRL_YEAR."-07-25') ";
        $query .= "                  OR ";
        $query .= "                  ENT_DATE > '".CTRL_YEAR."-07-25' ";
        $query .= "                 ) ";
        $query .= " ), BASE AS ( ";
        $query .= "             SELECT ";
        $query .= "                 S1.COURSECD, ";
        $query .= "                 S1.MAJORCD, ";
        $query .= "                 H1.NENREI_YEAR, ";
        $query .= "                 B1.SEX, ";
        $query .= "                 '01' AS SEQ ";
        $query .= "             FROM ";
        $query .= "                 MEDEXAM_DET_DAT M1 ";
        $query .= "             INNER JOIN SCHREG_BASE_MST B1 ON M1.SCHREGNO = B1.SCHREGNO ";
        $query .= "             INNER JOIN REGDEXIST R1 ON M1.SCHREGNO = R1.SCHREGNO ";
        $query .= "             JOIN SCHREG_REGD_DAT S1 ON M1.SCHREGNO = S1.SCHREGNO ";
        $query .= "                                    AND M1.YEAR     = S1.YEAR ";
        $query .= "                                    AND S1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "             JOIN HEXAM_PHYSICAL_AVG_DAT H1 ON M1.YEAR = H1.YEAR ";
        $query .= "                                           AND B1.SEX  = H1.SEX ";
        $query .= "                                           AND YEAR('".CTRL_YEAR."-04-01' - B1.BIRTHDAY) = H1.NENREI_YEAR ";
        $query .= "                                           AND H1.NENREI_MONTH = 0 ";
        $query .= "             WHERE ";
        $query .= "                     M1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "                 AND B1.BIRTHDAY IS NOT NULL ";
        $query .= "             GROUP BY ";
        $query .= "                 S1.COURSECD, ";
        $query .= "                 S1.MAJORCD, ";
        $query .= "                 H1.NENREI_YEAR, ";
        $query .= "                 B1.SEX ";
        $query .= "             UNION ALL ";
        $query .= "             SELECT ";
        $query .= "                 S1.COURSECD, ";
        $query .= "                 S1.MAJORCD, ";
        $query .= "                 H1.NENREI_YEAR, ";
        $query .= "                 B1.SEX, ";
        $query .= "                 '02' AS SEQ ";
        $query .= "             FROM ";
        $query .= "                 MEDEXAM_DET_DAT M1 ";
        $query .= "             INNER JOIN SCHREG_BASE_MST B1 ON M1.SCHREGNO = B1.SCHREGNO ";
        $query .= "             INNER JOIN REGDEXIST R1 ON M1.SCHREGNO = R1.SCHREGNO ";
        $query .= "             JOIN SCHREG_REGD_DAT S1 ON M1.SCHREGNO = S1.SCHREGNO ";
        $query .= "                                    AND M1.YEAR     = S1.YEAR ";
        $query .= "                                    AND S1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "             JOIN HEXAM_PHYSICAL_AVG_DAT H1 ON M1.YEAR = H1.YEAR ";
        $query .= "                                           AND B1.SEX  = H1.SEX ";
        $query .= "                                           AND YEAR('".CTRL_YEAR."-04-01' - B1.BIRTHDAY) = H1.NENREI_YEAR ";
        $query .= "                                           AND H1.NENREI_MONTH = 0 ";
        $query .= "             WHERE ";
        $query .= "                     M1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "                 AND B1.BIRTHDAY IS NOT NULL ";
        $query .= "             GROUP BY ";
        $query .= "                 S1.COURSECD, ";
        $query .= "                 S1.MAJORCD, ";
        $query .= "                 H1.NENREI_YEAR, ";
        $query .= "                 B1.SEX ";
        $query .= "             UNION ALL ";
        $query .= "             SELECT ";
        $query .= "                 S1.COURSECD, ";
        $query .= "                 S1.MAJORCD, ";
        $query .= "                 H1.NENREI_YEAR, ";
        $query .= "                 B1.SEX, ";
        $query .= "                 '03' AS SEQ ";
        $query .= "             FROM ";
        $query .= "                 MEDEXAM_DET_DAT M1 ";
        $query .= "             INNER JOIN SCHREG_BASE_MST B1 ON M1.SCHREGNO = B1.SCHREGNO ";
        $query .= "             INNER JOIN REGDEXIST R1 ON M1.SCHREGNO = R1.SCHREGNO ";
        $query .= "             JOIN SCHREG_REGD_DAT S1 ON M1.SCHREGNO = S1.SCHREGNO ";
        $query .= "                                    AND M1.YEAR     = S1.YEAR ";
        $query .= "                                    AND S1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "             JOIN HEXAM_PHYSICAL_AVG_DAT H1 ON M1.YEAR = H1.YEAR ";
        $query .= "                                           AND B1.SEX  = H1.SEX ";
        $query .= "                                           AND YEAR('".CTRL_YEAR."-04-01' - B1.BIRTHDAY) = H1.NENREI_YEAR ";
        $query .= "                                           AND H1.NENREI_MONTH = 0 ";
        $query .= "             WHERE ";
        $query .= "                     M1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "                 AND B1.BIRTHDAY IS NOT NULL ";
        $query .= "             GROUP BY ";
        $query .= "                 S1.COURSECD, ";
        $query .= "                 S1.MAJORCD, ";
        $query .= "                 H1.NENREI_YEAR, ";
        $query .= "                 B1.SEX ";
        $query .= " ), MAIN_T AS ( ";
        $query .= "             SELECT ";
        $query .= "                 COURSECD, ";
        $query .= "                 MAJORCD, ";
        $query .= "                 NENREI_YEAR, ";
        $query .= "                 SEX, ";
        $query .= "                 SEQ, ";
        $query .= "                 '' AS INT_VAL1, ";
        $query .= "                 '' AS INT_VAL2 ";
        $query .= "             FROM ";
        $query .= "                 BASE ";
        $query .= "             ORDER BY ";
        $query .= "                 COURSECD, ";
        $query .= "                 MAJORCD, ";
        $query .= "                 NENREI_YEAR, ";
        $query .= "                 SEX ";
        $query .= " ), HEI_D AS ( ";
        $query .= "             SELECT ";
        $query .= "                 S1.COURSECD, ";
        $query .= "                 S1.MAJORCD, ";
        $query .= "                 H1.NENREI_YEAR, ";
        $query .= "                 B1.SEX, ";
        $query .= "                 '01' AS SEQ1, ";
        $query .= "                 COUNT(M1.HEIGHT) AS CNTHEI, ";
        $query .= "                 SUM(M1.HEIGHT) AS SUMHEI ";
        $query .= "             FROM ";
        $query .= "                 MEDEXAM_DET_DAT M1 ";
        $query .= "             INNER JOIN SCHREG_BASE_MST B1 ON M1.SCHREGNO = B1.SCHREGNO ";
        $query .= "             JOIN SCHREG_REGD_DAT S1 ON M1.SCHREGNO = S1.SCHREGNO ";
        $query .= "                                    AND M1.YEAR     = S1.YEAR ";
        $query .= "                                    AND S1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "             JOIN HEXAM_PHYSICAL_AVG_DAT H1 ON M1.YEAR = H1.YEAR ";
        $query .= "                                           AND B1.SEX  = H1.SEX ";
        $query .= "                                           AND YEAR('".CTRL_YEAR."-04-01' - B1.BIRTHDAY) = H1.NENREI_YEAR ";
        $query .= "                                           AND H1.NENREI_MONTH = 0 ";
        $query .= "             WHERE ";
        $query .= "                 M1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "                 AND B1.BIRTHDAY IS NOT NULL ";
        $query .= "             GROUP BY ";
        $query .= "                 S1.COURSECD, ";
        $query .= "                 S1.MAJORCD, ";
        $query .= "                 H1.NENREI_YEAR, ";
        $query .= "                 B1.SEX ";
        $query .= " ), WEI_D AS ( ";
        $query .= "             SELECT ";
        $query .= "                 S1.COURSECD, ";
        $query .= "                 S1.MAJORCD, ";
        $query .= "                 H1.NENREI_YEAR, ";
        $query .= "                 B1.SEX, ";
        $query .= "                 '02' AS SEQ2 , ";
        $query .= "                 COUNT(M1.WEIGHT) AS CNTWEI, ";
        $query .= "                 SUM(M1.WEIGHT) AS SUMWEI ";
        $query .= "             FROM ";
        $query .= "                 MEDEXAM_DET_DAT M1 ";
        $query .= "             INNER JOIN SCHREG_BASE_MST B1 ON M1.SCHREGNO = B1.SCHREGNO ";
        $query .= "             JOIN SCHREG_REGD_DAT S1 ON M1.SCHREGNO = S1.SCHREGNO ";
        $query .= "                                    AND M1.YEAR     = S1.YEAR ";
        $query .= "                                    AND S1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "             JOIN HEXAM_PHYSICAL_AVG_DAT H1 ON M1.YEAR = H1.YEAR ";
        $query .= "                                           AND B1.SEX  = H1.SEX ";
        $query .= "                                           AND YEAR('".CTRL_YEAR."-04-01' - B1.BIRTHDAY) = H1.NENREI_YEAR ";
        $query .= "                                           AND H1.NENREI_MONTH = 0 ";
        $query .= "             WHERE ";
        $query .= "                     M1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "                 AND B1.BIRTHDAY IS NOT NULL ";
        $query .= "             GROUP BY ";
        $query .= "                 S1.COURSECD, ";
        $query .= "                 S1.MAJORCD, ";
        $query .= "                 H1.NENREI_YEAR, ";
        $query .= "                 B1.SEX ";
        $query .= " ), SIT_D AS ( ";
        $query .= "             SELECT ";
        $query .= "                 S1.COURSECD, ";
        $query .= "                 S1.MAJORCD, ";
        $query .= "                 H1.NENREI_YEAR, ";
        $query .= "                 B1.SEX, ";
        $query .= "                 '03' AS SEQ3, ";
        $query .= "                 COUNT(M1.SITHEIGHT) AS CNTSIT, ";
        $query .= "                 SUM(M1.SITHEIGHT) AS SUMSIT ";
        $query .= "             FROM ";
        $query .= "                 MEDEXAM_DET_DAT M1 ";
        $query .= "             INNER JOIN SCHREG_BASE_MST B1 ON M1.SCHREGNO = B1.SCHREGNO ";
        $query .= "             JOIN SCHREG_REGD_DAT S1 ON M1.SCHREGNO = S1.SCHREGNO ";
        $query .= "                                    AND M1.YEAR     = S1.YEAR ";
        $query .= "                                    AND S1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "             JOIN HEXAM_PHYSICAL_AVG_DAT H1 ON M1.YEAR = H1.YEAR ";
        $query .= "                                           AND B1.SEX  = H1.SEX ";
        $query .= "                                           AND YEAR('".CTRL_YEAR."-04-01' - B1.BIRTHDAY) = H1.NENREI_YEAR ";
        $query .= "                                           AND H1.NENREI_MONTH = 0 ";
        $query .= "             WHERE ";
        $query .= "                     M1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "                 AND B1.BIRTHDAY IS NOT NULL ";
        $query .= "             GROUP BY ";
        $query .= "                 S1.COURSECD, ";
        $query .= "                 S1.MAJORCD, ";
        $query .= "                 H1.NENREI_YEAR, ";
        $query .= "                 B1.SEX ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     '003' AS DATA_DIV, ";
        $query .= "     MAIN.COURSECD, ";
        $query .= "     MAIN.MAJORCD, ";
        $query .= "     MAIN.NENREI_YEAR AS AGE, ";
        $query .= "     MAIN.SEX, ";
        $query .= "     MAIN.SEQ, ";
        $query .= "     CASE WHEN MAIN.SEQ = '01' THEN HE_D.CNTHEI ";
        $query .= "          WHEN MAIN.SEQ = '02' THEN WE_D.CNTWEI ";
        $query .= "          WHEN MAIN.SEQ = '03' THEN SI_D.CNTSIT ";
        $query .= "     END AS INT_VAL1, ";
        $query .= "     CASE WHEN MAIN.SEQ = '01' THEN HE_D.SUMHEI ";
        $query .= "          WHEN MAIN.SEQ = '02' THEN WE_D.SUMWEI ";
        $query .= "          WHEN MAIN.SEQ = '03' THEN SI_D.SUMSIT ";
        $query .= "     END AS INT_VAL2 ";
        $query .= " FROM ";
        $query .= "     MAIN_T MAIN  ";
        $query .= " LEFT JOIN HEI_D HE_D ON MAIN.COURSECD    = HE_D.COURSECD ";
        $query .= "                     AND MAIN.MAJORCD     = HE_D.MAJORCD ";
        $query .= "                     AND MAIN.NENREI_YEAR = HE_D.NENREI_YEAR ";
        $query .= "                     AND MAIN.SEX         = HE_D.SEX ";
        $query .= "                     AND MAIN.SEQ         = HE_D.SEQ1 ";
        $query .= " LEFT JOIN WEI_D WE_D ON MAIN.COURSECD    = WE_D.COURSECD ";
        $query .= "                     AND MAIN.MAJORCD     = WE_D.MAJORCD ";
        $query .= "                     AND MAIN.NENREI_YEAR = WE_D.NENREI_YEAR ";
        $query .= "                     AND MAIN.SEX         = WE_D.SEX ";
        $query .= "                     AND MAIN.SEQ         = WE_D.SEQ2 ";
        $query .= " LEFT JOIN SIT_D SI_D ON MAIN.COURSECD    = SI_D.COURSECD ";
        $query .= "                     AND MAIN.MAJORCD     = SI_D.MAJORCD ";
        $query .= "                     AND MAIN.NENREI_YEAR = SI_D.NENREI_YEAR ";
        $query .= "                     AND MAIN.SEX         = SI_D.SEX ";
        $query .= "                     AND MAIN.SEQ         = SI_D.SEQ3 ";
        $query .= " ORDER BY ";
        $query .= "     DATA_DIV, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     AGE, ";
        $query .= "     SEX, ";
        $query .= "     SEQ ";

        return $query;
    }

    //学校側から県側へコピーするデータ取得
    function getM307FixedDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DISEASE_ADDITION307_FIXED_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND FIXED_DATE   = '".str_replace("/", "-", $model->fixedData)."' ";

        return $query;
    }

    //UPDATE
    function &getUpdateReport($model) {
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();
        $db->autoCommit(false);
        $db2->autoCommit(false);

        //県側、学校側・報告テーブル
        $query  = "DELETE FROM REPORT_MEDEXAM_DISEASE_ADDITION307_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".CTRL_YEAR."'";
        $query .= "   AND EXECUTE_DATE = '".str_replace("/", "-", $model->execute_date)."' ";
        $db->query($query);
        $db2->query($query);

        //県側、学校側・報告テーブル
        $data = array(); //初期化
        $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["EXECUTE_DATE"][DATE]         = $model->execute_date;
        $data["FIXED_DATE"][DATE]           = $model->fixedData;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            =" sysdate()";
        $query  = Query::insertSQL($data, "REPORT_MEDEXAM_DISEASE_ADDITION307_DAT");
        $db->query($query);
        $db2->query($query);

        //DELETE
        //県側・肥満度傾向児及び痩身傾向児出現率一覧テーブル
        $query  = "DELETE FROM MEDEXAM_DISEASE_ADDITION307_FIXED_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."' ";
        $query .= "   AND YEAR              = '".CTRL_YEAR."' ";
        $query .= "   AND FIXED_DATE        = '".str_replace("/", "-", $model->fixedData)."' ";
        $db2->query($query);

        //INSERT
        //学校側から県側へコピーするデータ取得（肥満度傾向児及び痩身傾向児出現率一覧）
        $query = knjf307Query::getM307FixedDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //県側・肥満度傾向児及び痩身傾向児出現率一覧テーブル
            $data = array(); //初期化
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $row["EDBOARD_SCHOOLCD"];
            $data["YEAR"][TEXT]                 = $row["YEAR"];
            $data["DATA_DIV"][TEXT]             = $row["DATA_DIV"];
            $data["FIXED_DATE"][DATE]           = $row["FIXED_DATE"];
            $data["COURSECD"][TEXT]             = $row["COURSECD"];
            $data["MAJORCD"][TEXT]              = $row["MAJORCD"];
            $data["AGE"][NUMBER]                = $row["AGE"];
            $data["SEX"][TEXT]                  = $row["SEX"];
            $data["SEQ"][TEXT]                  = $row["SEQ"];
            //人数
            $data["INT_VAL1"][NUMBER]           = $row["INT_VAL1"];
            $data["INT_VAL2"][NUMBER]           = $row["INT_VAL2"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";
            $query  = Query::insertSQL($data, "MEDEXAM_DISEASE_ADDITION307_FIXED_DAT");
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
        $where .= "         YEAR             = '".CTRL_YEAR."' ";
        $where .= "     AND DOC_NUMBER       = {$model->field["DOC_NUMBER"]} ";
        $where .= "     AND EDBOARD_SCHOOLCD = '{$model->schoolcd}' ";

        $query = Query::updateSQL($data, "AFT_SEARCH_REPORT_SCHOOL_DAT", $where);
        $db2->query($query);

        $db->commit();
        $db2->commit();
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);
        return;
    }

    //年齢取得
    function getAge($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     AGE ";
        $query .= " FROM ";
        if ($model->fixedData) {
            $query .= "     MEDEXAM_DISEASE_ADDITION307_FIXED_DAT ";
        } else {
            $query .= "     MEDEXAM_DISEASE_ADDITION307_DAT ";
        }
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        if ($model->fixedData) {
            $query .= "     AND FIXED_DATE = '".str_replace("/", "-", $model->fixedData)."' ";
        } else {
            $query .= "     AND YEAR = '".CTRL_YEAR."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     AGE ";

        return $query;
    }

    //学校名取得
    function getSchoolName($model) {
        $query  = " SELECT ";
        $query .= "     SCHOOLNAME1 ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '".CTRL_YEAR."' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND SCHOOLCD    = '000000000000' ";
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //コースコード取得
    function getCoursecd() {
        $query  = " SELECT DISTINCT ";
        $query .= "     COURSECD ";
        $query .= " FROM ";
        $query .= "     V_COURSE_MAJOR_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //CSV
    function selectCsvQuery($model, $dataDiv, $coursecd) {
        $table   = ($model->fixedData) ? "MEDEXAM_DISEASE_ADDITION307_FIXED_DAT" : "MEDEXAM_DISEASE_ADDITION307_DAT";

        $query  = " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     MD1.SEQ , ";
        $query .= "     MD1.COURSECD , ";
        $query .= "     MD1.MAJORCD , ";
        $query .= "     V_CM.COURSENAME || V_CM.MAJORNAME AS C_M_NAME , ";
        $query .= "     V_CM.COURSENAME  AS C_NAME , ";
        $query .= "     MD1.SEX , ";
        $query .= "     VN1.NAME1 AS SEXNAME ";
        foreach ($model->age as $ageVal) {
            $query .= "     ,CAST(CASE WHEN A{$ageVal}.INT_VAL1 IS NULL THEN 0 ELSE A{$ageVal}.INT_VAL1 END AS INTEGER) AS INT_VAL1_{$ageVal} ";
            $query .= "     ,CASE WHEN A{$ageVal}.INT_VAL2 IS NULL THEN 0 ELSE A{$ageVal}.INT_VAL2 END AS INT_VAL2_{$ageVal} ";
            $query .= "     ,ROUND(CAST(CASE WHEN A{$ageVal}.INT_VAL2 / A{$ageVal}.INT_VAL1 IS NULL ";
            $query .= "                THEN 0 ";
            $query .= "                ELSE A{$ageVal}.INT_VAL2 / A{$ageVal}.INT_VAL1 ";
            $query .= "           END AS DECIMAL(5,2) ";
            $query .= "      ) * 100, 2) / 100 AS AVG_{$ageVal} ";
        }
        $query .= " FROM ";
        $query .= "     (SELECT DISTINCT ";
        $query .= "         EDBOARD_SCHOOLCD, ";
        $query .= "         YEAR, ";
        $query .= "         DATA_DIV, ";
        if ($table == "MEDEXAM_DISEASE_ADDITION307_FIXED_DAT") {
            $query .= "     FIXED_DATE, ";
        }
        $query .= "         COURSECD, ";
        $query .= "         MAJORCD, ";
        $query .= "         SEX, ";
        $query .= "         '01' AS SEQ ";
        $query .= "     FROM ";
        $query .= "         {$table} ";
        $query .= "     ) MD1  ";
        foreach ($model->age as $ageVal) {
            $query .= " LEFT JOIN {$table} A{$ageVal}  ";
            $query .= "                     ON MD1.EDBOARD_SCHOOLCD = A{$ageVal}.EDBOARD_SCHOOLCD  ";
            $query .= "                     AND MD1.YEAR       = A{$ageVal}.YEAR  ";
            $query .= "                     AND MD1.DATA_DIV   = A{$ageVal}.DATA_DIV  ";
            if ($table == "MEDEXAM_DISEASE_ADDITION307_FIXED_DAT") {
                $query .= "                     AND MD1.FIXED_DATE = A{$ageVal}.FIXED_DATE ";
            }
            $query .= "                     AND MD1.COURSECD   = A{$ageVal}.COURSECD  ";
            $query .= "                     AND MD1.MAJORCD    = A{$ageVal}.MAJORCD  ";
            $query .= "                     AND A{$ageVal}.AGE = {$ageVal}  ";
            $query .= "                     AND MD1.SEX        = A{$ageVal}.SEX  ";
            $query .= "                     AND A{$ageVal}.SEQ = '01'  ";
        }
        $query .= " LEFT JOIN V_COURSE_MAJOR_MST V_CM ON MD1.YEAR = V_CM.YEAR  ";
        $query .= "                     AND MD1.COURSECD = V_CM.COURSECD  ";
        $query .= "                     AND MD1.MAJORCD = V_CM.MAJORCD  ";
        $query .= " JOIN V_NAME_MST VN1 ON MD1.YEAR = VN1.YEAR  ";
        $query .= "                     AND VN1.NAMECD1 = 'Z002'  ";
        $query .= "                     AND MD1.SEX = VN1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     MD1.EDBOARD_SCHOOLCD = '{$model->schoolcd}' ";
        $query .= "     AND MD1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND MD1.DATA_DIV = '{$dataDiv}' ";
        if ($table == "MEDEXAM_DISEASE_ADDITION307_FIXED_DAT") {
            $query .= "     AND MD1.FIXED_DATE = '".str_replace("/", "-", $model->fixedData)."' ";
        }
        $query .= "     AND MD1.COURSECD IN ('".str_replace(",", "','", implode(",", $coursecd))."') ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     MD1.SEQ , ";
        $query .= "     MD1.COURSECD , ";
        $query .= "     MD1.MAJORCD , ";
        $query .= "     V_CM.COURSENAME || V_CM.MAJORNAME AS C_M_NAME , ";
        $query .= "     V_CM.COURSENAME  AS C_NAME , ";
        $query .= "     MD1.SEX , ";
        $query .= "     VN1.NAME1 AS SEXNAME ";
        foreach ($model->age as $ageVal) {
            $query .= "     ,CAST(CASE WHEN A{$ageVal}.INT_VAL1 IS NULL THEN 0 ELSE A{$ageVal}.INT_VAL1 END AS INTEGER) AS INT_VAL1_{$ageVal} ";
            $query .= "     ,CASE WHEN A{$ageVal}.INT_VAL2 IS NULL THEN 0 ELSE A{$ageVal}.INT_VAL2 END AS INT_VAL2_{$ageVal} ";
            $query .= "     ,CAST(CASE WHEN A{$ageVal}.INT_VAL2 / A{$ageVal}.INT_VAL1 IS NULL THEN 0 ELSE A{$ageVal}.INT_VAL2 / A{$ageVal}.INT_VAL1 END AS DECIMAL(5,2)) AS AVG_{$ageVal}  ";
        }
        $query .= " FROM ";
        $query .= "     (SELECT DISTINCT ";
        $query .= "         EDBOARD_SCHOOLCD, ";
        $query .= "         YEAR, ";
        $query .= "         DATA_DIV, ";
        if ($table == "MEDEXAM_DISEASE_ADDITION307_FIXED_DAT") {
            $query .= "     FIXED_DATE, ";
        }
        $query .= "         COURSECD, ";
        $query .= "         MAJORCD, ";
        $query .= "         SEX, ";
        $query .= "         '02' AS SEQ ";
        $query .= "     FROM ";
        $query .= "         {$table} ";
        $query .= "     ) MD1  ";
        foreach ($model->age as $ageVal) {
            $query .= " LEFT JOIN {$table} A{$ageVal}  ";
            $query .= "                     ON MD1.EDBOARD_SCHOOLCD = A{$ageVal}.EDBOARD_SCHOOLCD  ";
            $query .= "                     AND MD1.YEAR = A{$ageVal}.YEAR  ";
            $query .= "                     AND MD1.DATA_DIV = A{$ageVal}.DATA_DIV  ";
            if ($table == "MEDEXAM_DISEASE_ADDITION307_FIXED_DAT") {
                $query .= "                     AND MD1.FIXED_DATE = A{$ageVal}.FIXED_DATE ";
            }
            $query .= "                     AND MD1.COURSECD = A{$ageVal}.COURSECD  ";
            $query .= "                     AND MD1.MAJORCD = A{$ageVal}.MAJORCD  ";
            $query .= "                     AND A{$ageVal}.AGE = {$ageVal}  ";
            $query .= "                     AND MD1.SEX = A{$ageVal}.SEX  ";
            $query .= "                     AND A{$ageVal}.SEQ = '02'  ";
        }
        $query .= " LEFT JOIN V_COURSE_MAJOR_MST V_CM ON MD1.YEAR = V_CM.YEAR  ";
        $query .= "                     AND MD1.COURSECD = V_CM.COURSECD  ";
        $query .= "                     AND MD1.MAJORCD = V_CM.MAJORCD  ";
        $query .= " JOIN V_NAME_MST VN1 ON MD1.YEAR = VN1.YEAR  ";
        $query .= "                     AND VN1.NAMECD1 = 'Z002'  ";
        $query .= "                     AND MD1.SEX = VN1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     MD1.EDBOARD_SCHOOLCD = '{$model->schoolcd}' ";
        $query .= "     AND MD1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND MD1.DATA_DIV = '{$dataDiv}' ";
        if ($table == "MEDEXAM_DISEASE_ADDITION307_FIXED_DAT") {
            $query .= "     AND MD1.FIXED_DATE = '".str_replace("/", "-", $model->fixedData)."' ";
        }
        $query .= "     AND MD1.COURSECD IN ('".str_replace(",", "','", implode(",", $coursecd))."') ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     MD1.SEQ , ";
        $query .= "     MD1.COURSECD , ";
        $query .= "     MD1.MAJORCD , ";
        $query .= "     V_CM.COURSENAME || V_CM.MAJORNAME AS C_M_NAME , ";
        $query .= "     V_CM.COURSENAME  AS C_NAME , ";
        $query .= "     MD1.SEX, ";
        $query .= "     VN1.NAME1 AS SEXNAME ";
        foreach ($model->age as $ageVal) {
            $query .= "     ,CAST(CASE WHEN A{$ageVal}.INT_VAL1 IS NULL THEN 0 ELSE A{$ageVal}.INT_VAL1 END AS INTEGER) AS INT_VAL1_{$ageVal} ";
            $query .= "     ,CASE WHEN A{$ageVal}.INT_VAL2 IS NULL THEN 0 ELSE A{$ageVal}.INT_VAL2 END AS INT_VAL2_{$ageVal} ";
            $query .= "     ,CAST(CASE WHEN A{$ageVal}.INT_VAL2 / A{$ageVal}.INT_VAL1 IS NULL THEN 0 ELSE A{$ageVal}.INT_VAL2 / A{$ageVal}.INT_VAL1 END AS DECIMAL(5,2)) AS AVG_{$ageVal}  ";
        }
        $query .= " FROM ";
        $query .= "     (SELECT DISTINCT ";
        $query .= "         EDBOARD_SCHOOLCD, ";
        $query .= "         YEAR, ";
        $query .= "         DATA_DIV, ";
        if ($table == "MEDEXAM_DISEASE_ADDITION307_FIXED_DAT") {
            $query .= "     FIXED_DATE, ";
        }
        $query .= "         COURSECD, ";
        $query .= "         MAJORCD, ";
        $query .= "         SEX, ";
        $query .= "         '03' AS SEQ ";
        $query .= "     FROM ";
        $query .= "         {$table} ";
        $query .= "     ) MD1  ";
        foreach ($model->age as $ageVal) {
            $query .= " LEFT JOIN {$table} A{$ageVal}  ";
            $query .= "                     ON MD1.EDBOARD_SCHOOLCD = A{$ageVal}.EDBOARD_SCHOOLCD  ";
            $query .= "                     AND MD1.YEAR = A{$ageVal}.YEAR  ";
            $query .= "                     AND MD1.DATA_DIV = A{$ageVal}.DATA_DIV  ";
            if ($table == "MEDEXAM_DISEASE_ADDITION307_FIXED_DAT") {
                $query .= "                     AND MD1.FIXED_DATE = A{$ageVal}.FIXED_DATE ";
            }
            $query .= "                     AND MD1.COURSECD = A{$ageVal}.COURSECD  ";
            $query .= "                     AND MD1.MAJORCD = A{$ageVal}.MAJORCD  ";
            $query .= "                     AND A{$ageVal}.AGE = {$ageVal}  ";
            $query .= "                     AND MD1.SEX = A{$ageVal}.SEX  ";
            $query .= "                     AND A{$ageVal}.SEQ = '03'  ";
        }
        $query .= " LEFT JOIN V_COURSE_MAJOR_MST V_CM ON MD1.YEAR = V_CM.YEAR  ";
        $query .= "                     AND MD1.COURSECD = V_CM.COURSECD  ";
        $query .= "                     AND MD1.MAJORCD = V_CM.MAJORCD  ";
        $query .= " JOIN V_NAME_MST VN1 ON MD1.YEAR = VN1.YEAR  ";
        $query .= "                     AND VN1.NAMECD1 = 'Z002'  ";
        $query .= "                     AND MD1.SEX = VN1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     MD1.EDBOARD_SCHOOLCD = '{$model->schoolcd}' ";
        $query .= "     AND MD1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND MD1.DATA_DIV = '{$dataDiv}' ";
        if ($table == "MEDEXAM_DISEASE_ADDITION307_FIXED_DAT") {
            $query .= "     AND MD1.FIXED_DATE = '".str_replace("/", "-", $model->fixedData)."' ";
        }
        $query .= "     AND MD1.COURSECD IN ('".str_replace(",", "','", implode(",", $coursecd))."') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     SEQ , ";
        $query .= "     COURSECD , ";
        $query .= "     MAJORCD , ";
        $query .= "     C_M_NAME AS COURSENAME, ";
        $query .= "     SEX , ";
        $query .= "     SEXNAME ";
        foreach ($model->age as $ageVal) {
            $query .= "     ,INT_VAL1_{$ageVal} ";
            $query .= "     ,INT_VAL2_{$ageVal} ";
            $query .= "     ,AVG_{$ageVal} ";
        }
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     SEQ , ";
        $query .= "     COURSECD , ";
        $query .= "     '999' AS MAJORCD , ";
        $query .= "     C_NAME AS COURSENAME, ";
        $query .= "     SEX , ";
        $query .= "     SEXNAME ";
        foreach ($model->age as $ageVal) {
            $query .= "     ,SUM(INT_VAL1_{$ageVal}) AS INT_VAL1_{$ageVal} ";
            $query .= "     ,SUM(INT_VAL2_{$ageVal}) AS INT_VAL2_{$ageVal} ";
            $query .= "     ,CASE WHEN SUM(INT_VAL1_{$ageVal}) = 0 ";
            $query .= "          THEN FLOAT(0) ";
            $query .= "          ELSE SUM(INT_VAL2_{$ageVal}) / FLOAT(SUM(INT_VAL1_{$ageVal})) ";
            $query .= "      END AS AVG_{$ageVal} ";
        }
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " GROUP BY ";
        $query .= "     SEQ , ";
        $query .= "     COURSECD , ";
        $query .= "     C_NAME, ";
        $query .= "     SEX , ";
        $query .= "     SEXNAME ";
        $query .= " ORDER BY ";
        $query .= "     SEQ , ";
        $query .= "     COURSECD , ";
        $query .= "     MAJORCD , ";
        $query .= "     SEX ";

        return $query;
    }
}
?>
