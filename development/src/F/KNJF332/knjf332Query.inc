<?php

require_once('for_php7.php');

class knjf332Query extends Query {

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
        $query = knjf332Query::z010Abbv1();
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

    //Z010
    function getZ010()
    {
        $query  = "SELECT NAME1, NAMESPARE2 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ";
        return $query;
    }

    //学習記録データの存在確認
    function getRecSchregno()
    {
        $query  = "SELECT COUNT(schregno) FROM schreg_studyrec_dat ";
        $query .= " WHERE year = '".CTRL_YEAR."'";
        return $query;
    }
    //学籍在籍データの存在確認
    function getSchSchregno()
    {
        $query  = "SELECT COUNT(schregno) FROM schreg_regd_dat ";
        $query .= " WHERE year     = '".CTRL_YEAR."'";
        $query .= "   AND semester = '".CTRL_SEMESTER."'";
        return $query;
    }
    //評定マスタの存在確認
    function getAssesscd()
    {
        $query  = "SELECT COUNT(assesscd) FROM assess_mst ";
        $query .= " WHERE assesscd = '4' ";
        return $query;
    }

    //データ表示
    function ReadQuery($model) {
        $query  = "";
        //学年・性別
        $query .= " WITH T_AGE (AGE_DATA, AGE_NAME) AS ( ";
        $query .= "     SELECT ";
        $query .= "         CASE WHEN BIRTHDAY IS NOT NULL THEN YEAR('".CTRL_YEAR."-04-01' - BASE.BIRTHDAY) END AS AGE_DATA, ";
        $query .= "         RTRIM(CAST(CASE WHEN BIRTHDAY IS NOT NULL THEN YEAR('".CTRL_YEAR."-04-01' - BASE.BIRTHDAY) END AS CHAR(4))) AS AGE_NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT REGD ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON REGD.YEAR = REG_G.YEAR ";
                $query .= "           AND REGD.GRADE = REG_G.GRADE ";
                $query .= "           AND REG_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON REGD.YEAR = REG_G.YEAR ";
            $query .= "           AND REGD.GRADE = REG_G.GRADE ";
            $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "         INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "               AND BASE.BIRTHDAY IS NOT NULL ";
        $query .= "     WHERE ";
        $query .= "         REGD.YEAR     = '".CTRL_YEAR."' ";
        $query .= "         AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     GROUP BY ";
        $query .= "         CASE WHEN BIRTHDAY IS NOT NULL THEN YEAR('".CTRL_YEAR."-04-01' - BASE.BIRTHDAY) END ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES(99, '合計') ";
        $query .= "     ) ";
        $query .= " , T_SEX (SEX, SEX_NAME) AS ( ";
        $query .= "     SELECT ";
        $query .= "         NAMECD2, ";
        $query .= "         ABBV1 ";
        $query .= "     FROM ";
        $query .= "         NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         NAMECD1 = 'Z002' ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('9', '合計') ";
        $query .= "     ) ";
        $query .= " , T_AGE_SEX AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.*, ";
        $query .= "         T2.* ";
        $query .= "     FROM ";
        $query .= "         T_AGE T1, ";
        $query .= "         T_SEX T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.AGE_DATA = 99 OR T2.SEX != '9' ";
        $query .= "     ) ";
        if ($model->cmd == "recalc") {
            //再計算
            $query .= " , T_RECALC AS ( ";
            $query .= "     SELECT ";
            $query .= "         CASE WHEN BIRTHDAY IS NOT NULL THEN YEAR('".CTRL_YEAR."-04-01' - T2.BIRTHDAY) END AS AGE_DATA, ";
            $query .= "         VALUE(T2.SEX, '9') AS SEX, ";
            $query .= "         SUM(CASE WHEN L1.CARE_FLG = '1' THEN 1 ELSE 0 END) AS DATA001_01, ";
            $query .= "         SUM(CASE WHEN L2.CARE_FLG = '1' THEN 1 ELSE 0 END) AS DATA001_02, ";
            $query .= "         SUM(CASE WHEN L6.CARE_FLG = '1' THEN 1 ELSE 0 END) AS DATA001_03, ";
            $query .= "         SUM(CASE WHEN L3.CARE_FLG = '1' THEN 1 ELSE 0 END) AS DATA001_04, ";
            $query .= "         0 AS DATA001_05, ";
            $query .= "         SUM(CASE WHEN L4.CARE_FLG = '1' THEN 1 ELSE 0 END) AS DATA001_06, ";
            $query .= "         SUM(CASE WHEN L5.CARE_FLG = '1' THEN 1 ELSE 0 END) AS DATA001_07, ";
            $query .= "         SUM(CASE WHEN (L4.CARE_FLG = '1' OR L5.CARE_FLG = '1') AND L7.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS DATA001_08, ";
            $query .= "         0 AS DATA001_09, ";
            $query .= "         0 AS DATA002_01, ";
            $query .= "         0 AS DATA003_01, ";
            $query .= "         0 AS DATA004_01 ";
            $query .= "     FROM ";
            $query .= "         SCHREG_REGD_DAT T1 ";
            $query .= "         INNER JOIN SCHREG_BASE_MST T2 ";
            $query .= "             ON  T2.SCHREGNO = T1.SCHREGNO ";
            $query .= "         INNER JOIN SCHREG_REGD_GDAT T3 ";
            $query .= "             ON  T3.YEAR     = T1.YEAR ";
            $query .= "             AND T3.GRADE    = T1.GRADE ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "             AND T3.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "             AND T3.SCHOOL_KIND    = '".SCHOOLKIND."' ";
            }
            $query .= "         LEFT JOIN MEDEXAM_CARE_HDAT L1 ";
            $query .= "             ON  L1.YEAR     = T1.YEAR ";
            $query .= "             AND L1.SCHREGNO = T1.SCHREGNO ";
            $query .= "             AND L1.CARE_DIV = '01' ";
            $query .= "         LEFT JOIN MEDEXAM_CARE_HDAT L2 ";
            $query .= "             ON  L2.YEAR     = T1.YEAR ";
            $query .= "             AND L2.SCHREGNO = T1.SCHREGNO ";
            $query .= "             AND L2.CARE_DIV = '02' ";
            $query .= "         LEFT JOIN MEDEXAM_CARE_HDAT L3 ";
            $query .= "             ON  L3.YEAR     = T1.YEAR ";
            $query .= "             AND L3.SCHREGNO = T1.SCHREGNO ";
            $query .= "             AND L3.CARE_DIV = '03' ";
            $query .= "         LEFT JOIN MEDEXAM_CARE_HDAT L4 ";
            $query .= "             ON  L4.YEAR     = T1.YEAR ";
            $query .= "             AND L4.SCHREGNO = T1.SCHREGNO ";
            $query .= "             AND L4.CARE_DIV = '04' ";
            $query .= "         LEFT JOIN MEDEXAM_CARE_HDAT L5 ";
            $query .= "             ON  L5.YEAR     = T1.YEAR ";
            $query .= "             AND L5.SCHREGNO = T1.SCHREGNO ";
            $query .= "             AND L5.CARE_DIV = '05' ";
            $query .= "         LEFT JOIN MEDEXAM_CARE_HDAT L6 ";
            $query .= "             ON  L6.YEAR     = T1.YEAR ";
            $query .= "             AND L6.SCHREGNO = T1.SCHREGNO ";
            $query .= "             AND L6.CARE_DIV = '06' ";
            $query .= "         LEFT JOIN MEDEXAM_CARE_DAT L7 ";
            $query .= "             ON  L7.YEAR     = T1.YEAR ";
            $query .= "             AND L7.SCHREGNO = T1.SCHREGNO ";
            $query .= "             AND L7.CARE_DIV = '04' ";
            $query .= "             AND L7.CARE_KIND = '01' ";
            $query .= "             AND L7.CARE_ITEM = '04' ";
            $query .= "             AND L7.CARE_SEQ = '02' ";
            $query .= "     WHERE ";
            $query .= "             T1.YEAR     = '".CTRL_YEAR."' ";
            $query .= "         AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "     GROUP BY ";
            $query .= "         GROUPING SETS ((CASE WHEN BIRTHDAY IS NOT NULL THEN YEAR('".CTRL_YEAR."-04-01' - T2.BIRTHDAY) END, T2.SEX), (T2.SEX), ()) ";
            $query .= "     ) ";
        } else {
            //登録テーブル
            $query .= " , T_ADDITION1 AS ( ";
            $query .= "     SELECT ";
            $query .= "         * ";
            $query .= "     FROM ";
            if ($model->fixedData) {
                $query .= "         MEDEXAM_DISEASE_SONOTA_FIXED_DAT ";
            } else {
                $query .= "         MEDEXAM_DISEASE_SONOTA_DAT ";
            }
            $query .= "     WHERE ";
            $query .= "         EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
            $query .= "         AND YEAR = '".CTRL_YEAR."' ";
            if ($model->fixedData) {
                $query .= "         AND FIXED_DATE = '".str_replace("/", "-", $model->fixedData)."' ";
            }
            $query .= "     ) ";
        }

        //メイン
        $query .= " SELECT ";
        $query .= "     T1.AGE_DATA, ";
        $query .= "     T1.SEX, ";
        $query .= "     T1.AGE_NAME, ";
        $query .= "     T1.SEX_NAME ";
        if ($model->cmd == "recalc") {
            foreach ($model->dataField as $key => $divSeq) {
                $query .= "     ,L1.DATA{$divSeq} ";
            }
        } else {
            foreach ($model->dataField as $key => $divSeq) {
                $query .= "     ,L{$divSeq}.INT_VAL AS DATA{$divSeq} ";
            }
        }
        $query .= " FROM ";
        $query .= "     T_AGE_SEX T1 ";
        if ($model->cmd == "recalc") {
            $query .= "     LEFT JOIN T_RECALC L1 ON L1.AGE_DATA = T1.AGE_DATA AND L1.SEX = T1.SEX ";
        } else {
            foreach ($model->dataField as $key => $divSeq) {
                $query .= "     LEFT JOIN T_ADDITION1 L{$divSeq} ON L{$divSeq}.AGE = T1.AGE_DATA ";
                $query .= "          AND L{$divSeq}.SEX = T1.SEX ";
                $query .= "          AND L{$divSeq}.DATA_DIV || '_' || L{$divSeq}.SEQ = '{$divSeq}' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     T1.AGE_DATA, ";
        $query .= "     T1.SEX ";
        return $query;
    }

    //UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //DELETE
        foreach ($model->dataField as $key => $divSeq) {
            list($dataDiv, $seq) = preg_split("/_/", $divSeq);
            $query  = "DELETE FROM MEDEXAM_DISEASE_SONOTA_DAT ";
            $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
            $query .= "   AND YEAR     = '".CTRL_YEAR."'";
            $query .= "   AND DATA_DIV = '{$dataDiv}'";
            $query .= "   AND SEQ      = '{$seq}'";
            $db->query($query);
        }

        //INSERT
        foreach ($model->updField as $key => $val) {
            list($dataDiv, $seq, $age, $sex) = preg_split("/_/", $key);
            $data = array(); //初期化
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
            $data["YEAR"][TEXT]                 = CTRL_YEAR;
            $data["AGE"][NUMBER]                = $age;
            $data["SEX"][TEXT]                  = $sex;
            $data["DATA_DIV"][TEXT]             = $dataDiv;
            $data["SEQ"][TEXT]                  = $seq;

            //理由は解らないが、サーバーにより
            //$valが配列になる場合とならない場合がある。
            //tokioでは、配列となっているが73サーバーではならない
            $data["INT_VAL"][NUMBER]            = is_array($val) ? $val[0] : $val;

            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";

            $query  = Query::insertSQL($data, "MEDEXAM_DISEASE_SONOTA_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //確定UPDATE
    function &getFixedUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //県側、学校側・報告テーブル
        $query  = "DELETE FROM MEDEXAM_DISEASE_SONOTA_FIXED_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".CTRL_YEAR."'";
        $query .= "   AND FIXED_DATE        = '".str_replace("/", "-", $model->fixedDate)."'";
        $db->query($query);

        //INSERT
        $query = knjf332Query::getKensinDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //キー
            $data = array(); //初期化
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $row["EDBOARD_SCHOOLCD"];
            $data["YEAR"][TEXT]                 = $row["YEAR"];
            $data["FIXED_DATE"][DATE]           = $model->fixedDate;
            $data["AGE"][NUMBER]                = $row["AGE"];
            $data["SEX"][TEXT]                  = $row["SEX"];
            $data["DATA_DIV"][TEXT]             = $row["DATA_DIV"];
            $data["SEQ"][TEXT]                  = $row["SEQ"];
            //人数
            $data["INT_VAL"][NUMBER]            = $row["INT_VAL"];
            $data["CHAR_VAL"][TEXT]             = $row["CHAR_VAL"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";
            $query  = Query::insertSQL($data, "MEDEXAM_DISEASE_SONOTA_FIXED_DAT");
            $db->query($query);

        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //県への報告履歴コンボ
    function getReport($model) {
        $query  = " SELECT ";
        $query .= "     T1.EXECUTE_DATE AS LABEL, ";
        $query .= "     T1.EXECUTE_DATE AS VALUE ";
        $query .= " FROM ";
        $query .= "     REPORT_DISEASE_SONOTA_DAT T1";
        $query .= " WHERE ";
        $query .= "         T1.EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND T1.YEAR             = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXECUTE_DATE DESC ";

        return $query;
    }

    //確定コンボ
    function getFixed($model) {
        $query  = " SELECT ";
        $query .= "     T1.FIXED_DATE AS LABEL, ";
        $query .= "     T1.FIXED_DATE AS VALUE ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DISEASE_SONOTA_FIXED_DAT T1";
        $query .= " WHERE ";
        $query .= "         T1.EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND T1.YEAR             = '".CTRL_YEAR."' ";
        $query .= " GROUP BY ";
        $query .= "     T1.FIXED_DATE ";
        $query .= " ORDER BY ";
        $query .= "     T1.FIXED_DATE DESC ";

        return $query;
    }

    //学校側から県側へコピーするデータ取得（疾病等結果一覧）
    function getKensinDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DISEASE_SONOTA_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //学校側から県側へコピーするデータ取得（疾病等結果一覧）
    function getKensinFixedDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DISEASE_SONOTA_FIXED_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND FIXED_DATE = '".str_replace("/", "-", $model->fixedData)."' ";

        return $query;
    }

    //UPDATE
    function &getUpdateReport($model) {
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();
        $db->autoCommit(false);
        $db2->autoCommit(false);

        //県側、学校側・報告テーブル
        $query  = "DELETE FROM REPORT_DISEASE_SONOTA_DAT ";
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
        $query  = Query::insertSQL($data, "REPORT_DISEASE_SONOTA_DAT");
        $db->query($query);
        $db2->query($query);

        //DELETE
        //県側・疾病等結果一覧テーブル
        $query  = "DELETE FROM MEDEXAM_DISEASE_SONOTA_FIXED_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".CTRL_YEAR."'";
        $query .= "   AND FIXED_DATE = '".str_replace("/", "-", $model->fixedData)."' ";
        $db2->query($query);

        //INSERT
        //学校側から県側へコピーするデータ取得（疾病等結果一覧）
        $query = knjf332Query::getKensinFixedDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //県側・疾病等結果一覧テーブル
            //キー
            $data = array(); //初期化
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $row["EDBOARD_SCHOOLCD"];
            $data["YEAR"][TEXT]                 = $row["YEAR"];
            $data["FIXED_DATE"][DATE]           = $row["FIXED_DATE"];
            $data["AGE"][NUMBER]                = $row["AGE"];
            $data["SEX"][TEXT]                  = $row["SEX"];
            $data["DATA_DIV"][TEXT]             = $row["DATA_DIV"];
            $data["SEQ"][TEXT]                  = $row["SEQ"];
            //人数
            $data["INT_VAL"][NUMBER]            = $row["INT_VAL"];
            $data["CHAR_VAL"][TEXT]             = $row["CHAR_VAL"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";
            $query  = Query::insertSQL($data, "MEDEXAM_DISEASE_SONOTA_FIXED_DAT");
            $db2->query($query);
        }
        $result->free();

        $db->commit();
        $db2->commit();
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);
        return;
    }

    //CSV
    function selectCsvQuery($model) {
        $query  = "";
        //学年・性別
        $query .= " WITH T_AGE (AGE_DATA, AGE_NAME) AS ( ";
        $query .= "     SELECT ";
        $query .= "         CASE WHEN BIRTHDAY IS NOT NULL THEN YEAR('".CTRL_YEAR."-04-01' - BASE.BIRTHDAY) END AS AGE_DATA, ";
        $query .= "         RTRIM(CAST(CASE WHEN BIRTHDAY IS NOT NULL THEN YEAR('".CTRL_YEAR."-04-01' - BASE.BIRTHDAY) END AS CHAR(4))) AS AGE_NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT REGD ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON REGD.YEAR = REG_G.YEAR ";
                $query .= "           AND REGD.GRADE = REG_G.GRADE ";
                $query .= "           AND REG_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON REGD.YEAR = REG_G.YEAR ";
            $query .= "           AND REGD.GRADE = REG_G.GRADE ";
            $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "         INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "               AND BASE.BIRTHDAY IS NOT NULL ";
        $query .= "     WHERE ";
        $query .= "         REGD.YEAR     = '".CTRL_YEAR."' ";
        $query .= "         AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     GROUP BY ";
        $query .= "         CASE WHEN BIRTHDAY IS NOT NULL THEN YEAR('".CTRL_YEAR."-04-01' - BASE.BIRTHDAY) END ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES(99, '合計') ";
        $query .= "     ) ";
        $query .= " , T_SEX (SEX, SEX_NAME) AS ( ";
        $query .= "     SELECT ";
        $query .= "         NAMECD2, ";
        $query .= "         ABBV1 ";
        $query .= "     FROM ";
        $query .= "         NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         NAMECD1 = 'Z002' ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('9', '合計') ";
        $query .= "     ) ";
        $query .= " , T_AGE_SEX AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.*, ";
        $query .= "         T2.* ";
        $query .= "     FROM ";
        $query .= "         T_AGE T1, ";
        $query .= "         T_SEX T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.AGE_DATA = 99 OR T2.SEX != '9' ";
        $query .= "     ) ";
        //登録テーブル
        $query .= " , T_ADDITION1 AS ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        if ($model->fixedData) {
            $query .= "         MEDEXAM_DISEASE_SONOTA_FIXED_DAT ";
        } else {
            $query .= "         MEDEXAM_DISEASE_SONOTA_DAT ";
        }
        $query .= "     WHERE ";
        $query .= "         EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "         AND YEAR = '".CTRL_YEAR."' ";
        if ($model->fixedData) {
            $query .= "         AND FIXED_DATE = '".str_replace("/", "-", $model->fixedData)."' ";
        }
        $query .= "     ) ";

        //メイン
        $query .= " SELECT ";
        $query .= "     T1.AGE_NAME, ";
        $query .= "     T1.SEX_NAME ";
        foreach ($model->dataField as $key => $divSeq) {
            $query .= "     ,L{$divSeq}.INT_VAL AS DATA{$divSeq} ";
        }
        $query .= " FROM ";
        $query .= "     T_AGE_SEX T1 ";
        if ($model->cmd == "recalc") {
            $query .= "     LEFT JOIN T_RECALC L1 ON L1.AGE_DATA = T1.AGE_DATA AND L1.SEX = T1.SEX ";
        } else {
            foreach ($model->dataField as $key => $divSeq) {
                $query .= "     LEFT JOIN T_ADDITION1 L{$divSeq} ON L{$divSeq}.AGE = T1.AGE_DATA ";
                $query .= "          AND L{$divSeq}.SEX = T1.SEX ";
                $query .= "          AND L{$divSeq}.DATA_DIV || '_' || L{$divSeq}.SEQ = '{$divSeq}' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     T1.AGE_DATA, ";
        $query .= "     T1.SEX ";

        return $query;
    }
}
?>
