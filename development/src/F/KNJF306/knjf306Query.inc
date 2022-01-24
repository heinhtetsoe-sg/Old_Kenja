<?php

require_once('for_php7.php');

class knjf306Query extends Query {

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
        $query = knjf306Query::z010Abbv1();
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

    //情報取得
    function getRow($model) {
        $table = ($model->fixedData) ? "MEDEXAM_DISEASE_ADDITION306_FIXED_HDAT" : "MEDEXAM_DISEASE_ADDITION306_HDAT";

        $query  = " SELECT ";
        $query .= "     HIMAN, ";
        $query .= "     SOSHIN, ";
        $query .= "     IDOU_DATE ";
        $query .= " FROM ";
        $query .= "     {$table} ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '{$model->schoolcd}' ";
        if ($table == "MEDEXAM_DISEASE_ADDITION306_FIXED_HDAT") {
            $query .= "     AND YEAR       = '".CTRL_YEAR."' ";
            $query .= "     AND FIXED_DATE = '".str_replace("/", "-", $model->fixedData)."' ";
        } else {
            $query .= "     AND YEAR = (SELECT DISTINCT ";
            $query .= "                     MAX(YEAR)  ";
            $query .= "                 FROM  ";
            $query .= "                     MEDEXAM_DISEASE_ADDITION306_HDAT ";
            $query .= "                 WHERE  ";
            $query .= "                     EDBOARD_SCHOOLCD = '{$model->schoolcd}' ";
            $query .= "                 ) ";
        }

        return $query;
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
        $query .= "     AND L1.REQUEST_ANSWER_PRG = 'KNJF306' ";
        $query .= "     AND L1.SUBMISSION_DATE IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     T1.DOC_NUMBER ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //MEDEXAM_DISEASE_ADDITION306_DAT
        //DELETE
        $query  = "DELETE FROM MEDEXAM_DISEASE_ADDITION306_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."' ";
        $query .= "   AND YEAR     = '".CTRL_YEAR."' ";
        $db->query($query);

        //INSERT
        $query = knjf306Query::getMedexamDetDat($model);
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
            $data["INT_VAL"][NUMBER]            = $row["INT_VAL"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";

            $query  = Query::insertSQL($data, "MEDEXAM_DISEASE_ADDITION306_DAT");
            $db->query($query);
        }

        //MEDEXAM_DISEASE_ADDITION306_HDAT
        //DELETE
        $query  = "DELETE FROM MEDEXAM_DISEASE_ADDITION306_HDAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "            AND YEAR     = '".CTRL_YEAR."'";
        $db->query($query);

        //INSERT
        $data = array(); //初期化
        $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["HIMAN"][TEXT]                = $model->field["HIMAN"];
        $data["SOSHIN"][TEXT]               = $model->field["SOSHIN"];
        $data["IDOU_DATE"][DATE]            = $model->idouDate;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            =" sysdate()";

        $query  = Query::insertSQL($data, "MEDEXAM_DISEASE_ADDITION306_HDAT");
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
        $query  = " DELETE FROM MEDEXAM_DISEASE_ADDITION306_FIXED_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".CTRL_YEAR."'";
        $query .= "   AND FIXED_DATE        = '".str_replace("/", "-", $model->fixedDate)."'";
        $db->query($query);

        //INSERT
        $query = knjf306Query::getMedexamDetDat($model);
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
            $data["INT_VAL"][NUMBER]            = $row["INT_VAL"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";
            $query  = Query::insertSQL($data, "MEDEXAM_DISEASE_ADDITION306_FIXED_DAT");
            $db->query($query);

        }
        $result->free();

        //県側、学校側,HDATにする
        $query  = " DELETE FROM MEDEXAM_DISEASE_ADDITION306_FIXED_HDAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".CTRL_YEAR."'";
        $query .= "   AND FIXED_DATE        = '".str_replace("/", "-", $model->fixedDate)."'";
        $db->query($query);

        //INSERT
        //キー
        $data = array(); //初期化
        $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["HIMAN"][TEXT]                = $model->field["HIMAN"];
        $data["SOSHIN"][TEXT]               = $model->field["SOSHIN"];
        $data["IDOU_DATE"][DATE]            = $model->idouDate;
        $data["FIXED_DATE"][DATE]           = $model->fixedDate;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            =" sysdate()";
        $query  = Query::insertSQL($data, "MEDEXAM_DISEASE_ADDITION306_FIXED_HDAT");
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
        $query .= "     REPORT_MEDEXAM_DISEASE_ADDITION306_DAT ";
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
        $query .= "     MEDEXAM_DISEASE_ADDITION306_FIXED_DAT ";
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
        $query .= "                  (VALUE(GRD_DIV,'') IN ('2','3','6','7') AND GRD_DATE < '".str_replace("/", "-", $model->idouDate)."') ";
        $query .= "                  OR ";
        $query .= "                  ENT_DATE > '".str_replace("/", "-", $model->idouDate)."' ";
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
        $query .= "                 SEQ ";
        $query .= "             FROM ";
        $query .= "                 BASE ";
        $query .= "             ORDER BY ";
        $query .= "                 COURSECD, ";
        $query .= "                 MAJORCD, ";
        $query .= "                 NENREI_YEAR, ";
        $query .= "                 SEX ";
        $query .= " ), JUKENALL AS ( ";
        $query .= "             SELECT ";
        $query .= "                 S1.COURSECD, ";
        $query .= "                 S1.MAJORCD, ";
        $query .= "                 H1.NENREI_YEAR, ";
        $query .= "                 B1.SEX, ";
        $query .= "                 '01' AS SEQ1, ";
        $query .= "                 COUNT(*) AS JUKEN ";
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
        $query .= " ), HIMAN AS ( ";
        $query .= "             SELECT ";
        $query .= "                 S1.COURSECD, ";
        $query .= "                 S1.MAJORCD, ";
        $query .= "                 H1.NENREI_YEAR, ";
        $query .= "                 B1.SEX, ";
        $query .= "                 '02' AS SEQ2 , ";
        $query .= "                 COUNT(*) AS GAITOU ";
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
        $query .= "                 AND (100 * (M1.WEIGHT - (H1.STD_WEIGHT_KEISU_A * M1.HEIGHT - H1.STD_WEIGHT_KEISU_B)) / (H1.STD_WEIGHT_KEISU_A * M1.HEIGHT - H1.STD_WEIGHT_KEISU_B)) > {$model->himan} ";
        $query .= "             GROUP BY ";
        $query .= "                 S1.COURSECD, ";
        $query .= "                 S1.MAJORCD, ";
        $query .= "                 H1.NENREI_YEAR, ";
        $query .= "                 B1.SEX ";
        $query .= " ), SOSIN AS ( ";
        $query .= "             SELECT ";
        $query .= "                 S1.COURSECD, ";
        $query .= "                 S1.MAJORCD, ";
        $query .= "                 H1.NENREI_YEAR, ";
        $query .= "                 B1.SEX, ";
        $query .= "                 '02' AS SEQ2 , ";
        $query .= "                 COUNT(*) AS GAITOU ";
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
        $query .= "                 AND (100 * (M1.WEIGHT - (H1.STD_WEIGHT_KEISU_A * M1.HEIGHT - H1.STD_WEIGHT_KEISU_B)) / (H1.STD_WEIGHT_KEISU_A * M1.HEIGHT - H1.STD_WEIGHT_KEISU_B)) < -{$model->soshin} ";
        $query .= "             GROUP BY ";
        $query .= "                 S1.COURSECD, ";
        $query .= "                 S1.MAJORCD, ";
        $query .= "                 H1.NENREI_YEAR, ";
        $query .= "                 B1.SEX ";
        $query .= " ), HIMAN_R AS ( ";
        $query .= "             SELECT ";
        $query .= "                 J1.COURSECD, ";
        $query .= "                 J1.MAJORCD, ";
        $query .= "                 J1.NENREI_YEAR, ";
        $query .= "                 J1.SEX, ";
        $query .= "                 '03' AS SEQ3, ";
        $query .= "                 ROUND((DECIMAL(H1.GAITOU) * 100 / DECIMAL(J1.JUKEN)), 2) AS RITSU ";
        $query .= "             FROM ";
        $query .= "                 JUKENALL J1 ";
        $query .= "             LEFT JOIN HIMAN H1 ON J1.COURSECD    = H1.COURSECD ";
        $query .= "                               AND J1.MAJORCD     = H1.MAJORCD ";
        $query .= "                               AND J1.NENREI_YEAR = H1.NENREI_YEAR ";
        $query .= "                               AND J1.SEX         = H1.SEX ";
        $query .= " ), SOSIN_R AS ( ";
        $query .= "             SELECT ";
        $query .= "                 J1.COURSECD, ";
        $query .= "                 J1.MAJORCD, ";
        $query .= "                 J1.NENREI_YEAR, ";
        $query .= "                 J1.SEX, ";
        $query .= "                 '03' AS SEQ3, ";
        $query .= "                 ROUND((DECIMAL(S1.GAITOU) * 100 / DECIMAL(J1.JUKEN)), 2) AS RITSU ";
        $query .= "             FROM ";
        $query .= "                 JUKENALL J1 ";
        $query .= "             LEFT JOIN SOSIN S1 ON J1.COURSECD    = S1.COURSECD ";
        $query .= "                               AND J1.MAJORCD     = S1.MAJORCD ";
        $query .= "                               AND J1.NENREI_YEAR = S1.NENREI_YEAR ";
        $query .= "                               AND J1.SEX         = S1.SEX ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     '001' AS DATA_DIV, ";
        $query .= "     MAIN.COURSECD, ";
        $query .= "     MAIN.MAJORCD, ";
        $query .= "     MAIN.NENREI_YEAR AS AGE, ";
        $query .= "     MAIN.SEX, ";
        $query .= "     MAIN.SEQ, ";
        $query .= "     CASE ";
        $query .= "         WHEN MAIN.SEQ = '01' THEN ALL.JUKEN ";
        $query .= "         WHEN MAIN.SEQ = '02' THEN HIM.GAITOU ";
        $query .= "         WHEN MAIN.SEQ = '03' THEN HIR.RITSU ";
        $query .= "     END AS INT_VAL ";
        $query .= "  ";
        $query .= " FROM ";
        $query .= "     MAIN_T MAIN  ";
        $query .= " LEFT JOIN JUKENALL ALL ON MAIN.COURSECD    = ALL.COURSECD ";
        $query .= "                       AND MAIN.MAJORCD     = ALL.MAJORCD ";
        $query .= "                       AND MAIN.NENREI_YEAR = ALL.NENREI_YEAR ";
        $query .= "                       AND MAIN.SEX         = ALL.SEX ";
        $query .= "                       AND MAIN.SEQ         = ALL.SEQ1 ";
        $query .= " LEFT JOIN HIMAN HIM ON MAIN.COURSECD    = HIM.COURSECD ";
        $query .= "                    AND MAIN.MAJORCD     = HIM.MAJORCD ";
        $query .= "                    AND MAIN.NENREI_YEAR = HIM.NENREI_YEAR ";
        $query .= "                    AND MAIN.SEX         = HIM.SEX ";
        $query .= "                    AND MAIN.SEQ         = HIM.SEQ2 ";
        $query .= " LEFT JOIN HIMAN_R HIR ON MAIN.COURSECD    = HIR.COURSECD ";
        $query .= "                      AND MAIN.MAJORCD     = HIR.MAJORCD ";
        $query .= "                      AND MAIN.NENREI_YEAR = HIR.NENREI_YEAR ";
        $query .= "                      AND MAIN.SEX         = HIR.SEX ";
        $query .= "                      AND MAIN.SEQ         = HIR.SEQ3 ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     '002' AS DATA_DIV, ";
        $query .= "     MAIN.COURSECD, ";
        $query .= "     MAIN.MAJORCD, ";
        $query .= "     MAIN.NENREI_YEAR AS AGE, ";
        $query .= "     MAIN.SEX, ";
        $query .= "     MAIN.SEQ, ";
        $query .= "     CASE ";
        $query .= "         WHEN MAIN.SEQ = '01' THEN ALL.JUKEN ";
        $query .= "         WHEN MAIN.SEQ = '02' THEN SOS.GAITOU ";
        $query .= "         WHEN MAIN.SEQ = '03' THEN SOR.RITSU ";
        $query .= "     END AS INT_VAL ";
        $query .= " FROM ";
        $query .= "     MAIN_T MAIN  ";
        $query .= " LEFT JOIN JUKENALL ALL ON MAIN.COURSECD    = ALL.COURSECD ";
        $query .= "                       AND MAIN.MAJORCD     = ALL.MAJORCD ";
        $query .= "                       AND MAIN.NENREI_YEAR = ALL.NENREI_YEAR ";
        $query .= "                       AND MAIN.SEX         = ALL.SEX ";
        $query .= "                       AND MAIN.SEQ         = ALL.SEQ1 ";
        $query .= " LEFT JOIN SOSIN SOS ON MAIN.COURSECD    = SOS.COURSECD ";
        $query .= "                    AND MAIN.MAJORCD     = SOS.MAJORCD ";
        $query .= "                    AND MAIN.NENREI_YEAR = SOS.NENREI_YEAR ";
        $query .= "                    AND MAIN.SEX         = SOS.SEX ";
        $query .= "                    AND MAIN.SEQ         = SOS.SEQ2 ";
        $query .= " LEFT JOIN SOSIN_R SOR ON MAIN.COURSECD    = SOR.COURSECD ";
        $query .= "                      AND MAIN.MAJORCD     = SOR.MAJORCD ";
        $query .= "                      AND MAIN.NENREI_YEAR = SOR.NENREI_YEAR ";
        $query .= "                      AND MAIN.SEX         = SOR.SEX ";
        $query .= "                      AND MAIN.SEQ         = SOR.SEQ3 ";
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
    function getM306FixedDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DISEASE_ADDITION306_FIXED_DAT ";
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
        $query  = "DELETE FROM REPORT_MEDEXAM_DISEASE_ADDITION306_DAT ";
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
        $query  = Query::insertSQL($data, "REPORT_MEDEXAM_DISEASE_ADDITION306_DAT");
        $db->query($query);
        $db2->query($query);

        //DELETE
        //県側・肥満度傾向児及び痩身傾向児出現率一覧テーブル
        $query  = "DELETE FROM MEDEXAM_DISEASE_ADDITION306_FIXED_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."' ";
        $query .= "   AND YEAR              = '".CTRL_YEAR."' ";
        $query .= "   AND FIXED_DATE        = '".str_replace("/", "-", $model->fixedData)."' ";
        $db2->query($query);

        //INSERT
        //学校側から県側へコピーするデータ取得（肥満度傾向児及び痩身傾向児出現率一覧）
        $query = knjf306Query::getM306FixedDat($model);
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
            $data["INT_VAL"][NUMBER]            = $row["INT_VAL"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";
            $query  = Query::insertSQL($data, "MEDEXAM_DISEASE_ADDITION306_FIXED_DAT");
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
            $query .= "     MEDEXAM_DISEASE_ADDITION306_FIXED_DAT ";
        } else {
            $query .= "     MEDEXAM_DISEASE_ADDITION306_DAT ";
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

    //CSV
    function selectCsvQuery($model, $dataDiv) {
        $table = ($model->fixedData) ? "MEDEXAM_DISEASE_ADDITION306_FIXED_DAT" : "MEDEXAM_DISEASE_ADDITION306_DAT";

        $query  = " WITH AGEDATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         MD1.EDBOARD_SCHOOLCD, ";
        $query .= "         MD1.YEAR, ";
        $query .= "         MD1.DATA_DIV, ";
        if ($table == "MEDEXAM_DISEASE_ADDITION306_FIXED_DAT") {
            $query .= "         MD1.FIXED_DATE, ";
        }
        $query .= "         MD1.COURSECD, ";
        $query .= "         MD1.MAJORCD, ";
        $query .= "         MD1.AGE, ";
        $query .= "         MD1.SEX, ";
        $query .= "         MD1.INT_VAL AS INT_VAL1, ";
        $query .= "         MD2.INT_VAL AS INT_VAL2, ";
        $query .= "         MD3.INT_VAL AS INT_VAL3 ";
        $query .= "     FROM ";
        $query .= "         {$table} MD1 ";
        $query .= "     INNER JOIN {$table} MD2 ON MD1.EDBOARD_SCHOOLCD = MD2.EDBOARD_SCHOOLCD ";
        $query .= "                                    AND MD1.YEAR     = MD2.YEAR ";
        $query .= "                                    AND MD1.DATA_DIV = MD2.DATA_DIV ";
        if ($table == "MEDEXAM_DISEASE_ADDITION306_FIXED_DAT") {
            $query .= "                                AND MD1.FIXED_DATE = MD2.FIXED_DATE ";
        }
        $query .= "                                    AND MD1.COURSECD = MD2.COURSECD ";
        $query .= "                                    AND MD1.MAJORCD  = MD2.MAJORCD ";
        $query .= "                                    AND MD1.AGE      = MD2.AGE ";
        $query .= "                                    AND MD1.SEX      = MD2.SEX ";
        $query .= "                                    AND MD2.SEQ      = '02' ";
        $query .= "     INNER JOIN {$table} MD3 ON MD1.EDBOARD_SCHOOLCD = MD3.EDBOARD_SCHOOLCD ";
        $query .= "                                    AND MD1.YEAR     = MD3.YEAR ";
        $query .= "                                    AND MD1.DATA_DIV = MD3.DATA_DIV ";
        if ($table == "MEDEXAM_DISEASE_ADDITION306_FIXED_DAT") {
            $query .= "                                AND MD1.FIXED_DATE = MD3.FIXED_DATE ";
        }
        $query .= "                                    AND MD1.COURSECD = MD3.COURSECD ";
        $query .= "                                    AND MD1.MAJORCD  = MD3.MAJORCD ";
        $query .= "                                    AND MD1.AGE      = MD3.AGE ";
        $query .= "                                    AND MD1.SEX      = MD3.SEX ";
        $query .= "                                    AND MD3.SEQ      = '03' ";
        $query .= "     WHERE ";
        $query .= "         MD1.EDBOARD_SCHOOLCD = '{$model->schoolcd}' ";
        $query .= "         AND MD1.YEAR         = '".CTRL_YEAR."' ";
        $query .= "         AND MD1.DATA_DIV     = '{$dataDiv}' ";
        if ($table == "MEDEXAM_DISEASE_ADDITION306_FIXED_DAT") {
            $query .= "     AND MD1.FIXED_DATE   = '".str_replace("/", "-", $model->fixedData)."' ";
        }
        $query .= "         AND MD1.SEQ          = '01' ";
        $query .= "     ORDER BY ";
        $query .= "         MD1.COURSECD, ";
        $query .= "         MD1.MAJORCD, ";
        $query .= "         MD1.SEX ";
        $query .= "  ) ";
        //メイン
        $query .= " SELECT ";
        $query .= "     V_CM.COURSENAME || V_CM.MAJORNAME ";
        $query .= "     ,VN1.NAME1 ";     //性別
        foreach ($model->age as $ageVal) {
            $query .= "     ,CASE WHEN A{$ageVal}.INT_VAL1 IS NULL THEN 0 ELSE A{$ageVal}.INT_VAL1 END ";  //受験者数
            $query .= "     ,CASE WHEN A{$ageVal}.INT_VAL2 IS NULL THEN 0 ELSE A{$ageVal}.INT_VAL2 END ";  //該当者数
            $query .= "     ,CAST(ROUND(CASE WHEN A{$ageVal}.INT_VAL3 IS NULL THEN 0 ELSE A{$ageVal}.INT_VAL3 END, 2) AS DECIMAL(10,2)) ";  //出現率（％）
        }
        $query .= " FROM ";
        $query .= "     (SELECT DISTINCT ";
        $query .= "         EDBOARD_SCHOOLCD, ";
        $query .= "         YEAR, ";
        $query .= "         DATA_DIV, ";
        if ($table == "MEDEXAM_DISEASE_ADDITION306_FIXED_DAT") {
            $query .= "     FIXED_DATE, ";
        }
        $query .= "         COURSECD, ";
        $query .= "         MAJORCD, ";
        $query .= "         SEX ";
        $query .= "      FROM ";
        $query .= "         {$table} ";
        $query .= "      ) MD1 ";
        foreach ($model->age as $ageVal) {
            $query .= " LEFT JOIN AGEDATA A{$ageVal}  ON MD1.EDBOARD_SCHOOLCD = A{$ageVal}.EDBOARD_SCHOOLCD ";
            $query .= "                              AND MD1.YEAR       = A{$ageVal}.YEAR ";
            $query .= "                              AND MD1.DATA_DIV   = A{$ageVal}.DATA_DIV ";
            if ($table == "MEDEXAM_DISEASE_ADDITION306_FIXED_DAT") {
                $query .= "                          AND MD1.FIXED_DATE = A{$ageVal}.FIXED_DATE ";
            }
            $query .= "                              AND MD1.COURSECD   = A{$ageVal}.COURSECD ";
            $query .= "                              AND MD1.MAJORCD    = A{$ageVal}.MAJORCD ";
            $query .= "                              AND A{$ageVal}.AGE = {$ageVal} ";
            $query .= "                              AND MD1.SEX        = A{$ageVal}.SEX ";
        }
        $query .= " JOIN V_COURSE_MAJOR_MST V_CM ON MD1.YEAR     = V_CM.YEAR ";
        $query .= "                             AND MD1.COURSECD = V_CM.COURSECD ";
        $query .= "                             AND MD1.MAJORCD  = V_CM.MAJORCD ";
        $query .= " JOIN V_NAME_MST VN1 ON MD1.YEAR    = VN1.YEAR ";
        $query .= "                    AND VN1.NAMECD1 = 'Z002' ";
        $query .= "                    AND MD1.SEX     = VN1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     MD1.EDBOARD_SCHOOLCD = '{$model->schoolcd}' ";
        $query .= "     AND MD1.YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND MD1.DATA_DIV     = '{$dataDiv}' ";
        if ($table == "MEDEXAM_DISEASE_ADDITION306_FIXED_DAT") {
            $query .= "     AND MD1.FIXED_DATE = '".str_replace("/", "-", $model->fixedData)."' ";
        }
        $query .= "     AND MD1.SEX          IN ('1', '2') ";
        $query .= " ORDER BY ";
        $query .= "     MD1.COURSECD, ";
        $query .= "     MD1.MAJORCD, ";
        $query .= "     MD1.SEX ";

        return $query;
    }

    //CSV合計データ
    function selectCsvQuery2($model, $dataDiv) {
        $table = ($model->fixedData) ? "MEDEXAM_DISEASE_ADDITION306_FIXED_DAT" : "MEDEXAM_DISEASE_ADDITION306_DAT";

        $query  = " WITH AGEDATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         MD1.EDBOARD_SCHOOLCD, ";
        $query .= "         MD1.YEAR, ";
        $query .= "         MD1.DATA_DIV, ";
        if ($table == "MEDEXAM_DISEASE_ADDITION306_FIXED_DAT") {
            $query .= "     MD1.FIXED_DATE, ";
        }
        $query .= "         MD1.AGE, ";
        $query .= "         SUM(MD1.INT_VAL) AS SUM1, ";
        $query .= "         SUM(MD2.INT_VAL) AS SUM2, ";
        $query .= "         SUM(MD3.INT_VAL) AS SUM3 ";
        $query .= "     FROM ";
        $query .= "         {$table} MD1 ";
        $query .= "     INNER JOIN {$table} MD2 ON MD1.EDBOARD_SCHOOLCD = MD2.EDBOARD_SCHOOLCD ";
        $query .= "                                    AND MD1.YEAR     = MD2.YEAR ";
        $query .= "                                    AND MD1.DATA_DIV = MD2.DATA_DIV ";
        if ($table == "MEDEXAM_DISEASE_ADDITION306_FIXED_DAT") {
            $query .= "                                AND MD1.FIXED_DATE = MD2.FIXED_DATE ";
        }
        $query .= "                                    AND MD1.COURSECD = MD2.COURSECD ";
        $query .= "                                    AND MD1.MAJORCD  = MD2.MAJORCD ";
        $query .= "                                    AND MD1.AGE      = MD2.AGE ";
        $query .= "                                    AND MD1.SEX      = MD2.SEX ";
        $query .= "                                    AND MD2.SEQ      = '02' ";
        $query .= "     INNER JOIN {$table} MD3 ON MD1.EDBOARD_SCHOOLCD = MD3.EDBOARD_SCHOOLCD ";
        $query .= "                                    AND MD1.YEAR     = MD3.YEAR ";
        $query .= "                                    AND MD1.DATA_DIV = MD3.DATA_DIV ";
        if ($table == "MEDEXAM_DISEASE_ADDITION306_FIXED_DAT") {
            $query .= "                                AND MD1.FIXED_DATE = MD3.FIXED_DATE ";
        }
        $query .= "                                    AND MD1.COURSECD = MD3.COURSECD ";
        $query .= "                                    AND MD1.MAJORCD  = MD3.MAJORCD ";
        $query .= "                                    AND MD1.AGE      = MD3.AGE ";
        $query .= "                                    AND MD1.SEX      = MD3.SEX ";
        $query .= "                                    AND MD3.SEQ      = '03' ";
        $query .= "     WHERE ";
        $query .= "         MD1.EDBOARD_SCHOOLCD = '{$model->schoolcd}' ";
        $query .= "         AND MD1.YEAR         = '".CTRL_YEAR."' ";
        $query .= "         AND MD1.DATA_DIV     = '{$dataDiv}' ";
        if ($table == "MEDEXAM_DISEASE_ADDITION306_FIXED_DAT") {
            $query .= "     AND MD1.FIXED_DATE   = '".str_replace("/", "-", $model->fixedData)."' ";
        }
        $query .= "         AND MD1.SEQ          = '01' ";
        $query .= "     GROUP BY ";
        $query .= "         MD1.EDBOARD_SCHOOLCD, ";
        $query .= "         MD1.YEAR, ";
        if ($table == "MEDEXAM_DISEASE_ADDITION306_FIXED_DAT") {
            $query .= "     MD1.FIXED_DATE, ";
        }
        $query .= "         MD1.DATA_DIV, ";
        $query .= "         MD1.AGE ";
        $query .= "  ) ";
        //メイン（合計）
        $query .= " SELECT ";
        $query .= "     '全体' ";
        $query .= "     ,'全体' ";
        foreach ($model->age as $ageVal) {
            $query .= "     ,CASE WHEN SUM(A{$ageVal}.SUM1) IS NULL THEN 0 ELSE SUM(A{$ageVal}.SUM1) END ";  //受験者数
            $query .= "     ,CASE WHEN SUM(A{$ageVal}.SUM2) IS NULL THEN 0 ELSE SUM(A{$ageVal}.SUM2) END ";  //該当者数
            $query .= "     ,CAST(ROUND(CASE WHEN SUM(A{$ageVal}.SUM2) * 100 / SUM(A{$ageVal}.SUM1) IS NULL THEN 0 ELSE SUM(A{$ageVal}.SUM2) * 100 / SUM(A{$ageVal}.SUM1) END, 2) AS DECIMAL(10,2)) ";  //出現率（％）
        }
        $query .= " FROM ";
        $query .= "     (SELECT DISTINCT ";
        $query .= "         EDBOARD_SCHOOLCD, ";
        $query .= "         YEAR, ";
        $query .= "         DATA_DIV ";
        if ($table == "MEDEXAM_DISEASE_ADDITION306_FIXED_DAT") {
            $query .= "     ,FIXED_DATE ";
        }
        $query .= "      FROM ";
        $query .= "         {$table} ";
        $query .= "      ) MD1 ";
        foreach ($model->age as $ageVal) {
            $query .= " LEFT JOIN AGEDATA A{$ageVal} ON MD1.EDBOARD_SCHOOLCD = A{$ageVal}.EDBOARD_SCHOOLCD ";
            $query .= "                                   AND MD1.YEAR       = A{$ageVal}.YEAR ";
            $query .= "                                   AND MD1.DATA_DIV   = A{$ageVal}.DATA_DIV ";
            if ($table == "MEDEXAM_DISEASE_ADDITION306_FIXED_DAT") {
                $query .= "                               AND MD1.FIXED_DATE = A{$ageVal}.FIXED_DATE ";
            }
            $query .= "                                   AND A{$ageVal}.AGE = {$ageVal} ";
        }
        $query .= " WHERE ";
        $query .= "     MD1.EDBOARD_SCHOOLCD = '{$model->schoolcd}' ";
        $query .= "     AND MD1.YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND MD1.DATA_DIV     = '{$dataDiv}' ";
        if ($table == "MEDEXAM_DISEASE_ADDITION306_FIXED_DAT") {
            $query .= "     AND MD1.FIXED_DATE = '".str_replace("/", "-", $model->fixedData)."' ";
        }

        return $query;
    }
}
?>
