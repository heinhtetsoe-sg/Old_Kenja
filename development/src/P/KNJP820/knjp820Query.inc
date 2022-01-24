<?php

require_once('for_php7.php');

class knjp820query extends Query {
    /******************/
    /**  画面で使用  **/
    /******************/
    //年度取得
    function getYear($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //校種取得
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     ABBV1 AS LABEL, ";
        $query .= "     NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->field["SELYEAR"]."' AND ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    //学年取得
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->field["SELYEAR"]}' ";
        $query .= "     AND SCHOOL_KIND = '".$model->field["SELSCHKIND"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /************************/
    /**  ＣＳＶ出力で使用  **/
    /************************/
    //CSVデータ出力
    function selectQuery($model) {
        $seldatewk = str_replace("/", "-", $model->field["SELDATE"]);
        $query  = " WITH GRD_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         ENT_GRD.* ";
        $query .= "     FROM ";
        $query .= "         SCHREG_ENT_GRD_HIST_DAT ENT_GRD ";
        $query .= "     WHERE ";
        $query .= "         ENT_GRD.SCHOOL_KIND = '".$model->field["SELSCHKIND"]."' ";
        $query .= " ), TRANS AS ( ";
        $query .= "     SELECT ";
        $query .= "         TRANS.TRANSFERCD, ";
        $query .= "         TRANS.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_TRANSFER_DAT TRANS ";
        $query .= "     WHERE ";
        $query .= "         TRANS.TRANSFERCD = '2'";
        $query .= "         AND '".$seldatewk."' BETWEEN TRANS.TRANSFER_SDATE AND VALUE(TRANS.TRANSFER_EDATE, '9999-12-31') ";
        $query .= " ), COMEBACK AS ( ";
        $query .= "     SELECT ";
        $query .= "         DISTINCT ";
        $query .= "         COMEBACK.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_ENT_GRD_HIST_COMEBACK_DAT COMEBACK ";
        $query .= "     WHERE ";
        $query .= "         COMEBACK.SCHOOL_KIND = '".$model->field["SELSCHKIND"]."' ";
        $query .= "         AND COMEBACK.COMEBACK_DATE BETWEEN '".$model->field["SELYEAR"]."-04-01' AND '".$seldatewk."' ";
        $query .= " ), SCHBASE AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.YEAR, ";
        $query .= "         MAX(T1.SEMESTER) AS SEMESTER, ";
        $query .= "         T2.SCHOOL_KIND,";
        $query .= "         T1.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_REGD_GDAT T2 ";
        $query .= "              ON T2.YEAR        = T1.YEAR ";
        $query .= "             AND T2.SCHOOL_KIND = '".$model->field["SELSCHKIND"]."' ";
        $query .= "             AND T2.GRADE       = T1.GRADE ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".$model->field["SELYEAR"]."' ";
        if ($model->field["SELGRADE"] != "ALL") {
            $query .= "     AND T1.GRADE = '".$model->field["SELGRADE"]."' ";
        }
        $query .= "     GROUP BY ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T2.SCHOOL_KIND,";
        $query .= "         T1.SCHREGNO ";
        $query .= " ), ADDR AS ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         SCHREG_ADDRESS_DAT ";
        $query .= "     WHERE ";
        $query .= "         '".$seldatewk."' BETWEEN ISSUEDATE AND VALUE(EXPIREDATE, '9999-12-31') ";
        $query .= " ), NEW_ZIPCD_T AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         NEW_ZIPCD, ";
        $query .= "         PREF, ";
        $query .= "         CITY ";
        $query .= "     FROM ";
        $query .= "         ZIPCD_MST ";
        $query .= " ), REGD_NOW AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_BASE_MST ";
        $query .= "     WHERE ";
        $query .= "         '".$seldatewk."' > ENT_DATE ";
        $query .= "     AND '".$seldatewk."' < VALUE(GRD_DATE, '9999-12-31') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO AS SRC_SCHREGNO, ";
        if ($model->field["SELSCHKIND"] == "J") {
            $query .= "     SIGEL_SCH.SIGEL_NO_J AS SIGEL_NO, ";
        } else if ($model->field["SELSCHKIND"] == "H") {
            $query .= "     SIGEL_SCH.SIGEL_NO_H AS SIGEL_NO, ";
        } else {
            $query .= "     '' AS SIGEL_NO, ";
        }
        $query .= "     BASE.NAME AS GNAME, ";
        $query .= "     BASE.NAME_KANA AS GKANA, ";
        $query .= "     BASE.SEX, ";
        $query .= "     BASE.BIRTHDAY, ";
        $query .= "     ADDR.ZIPCD AS ZIP_NO, ";
        $query .= "     PREF.PREF_CD, ";
        $query .= "     PREF.PREF_NAME, ";
        $query .= "     ZIP.CITY, ";
        $query .= "     ADDR.ADDR1 AS ADDRESS1, ";
        $query .= "     '' AS ADDRESS2, ";
        $query .= "     ADDR.ADDR2 AS ADDRESS3, ";
        $query .= "     ADDR.TELNO AS TELNO, ";
        $query .= "     '0' AS VER_NO, ";
        $query .= "     SIGEL_COURSE.SGL_SCHOOLKIND, ";
        $query .= "     SIGEL_COURSE.SGL_MAJORCD, ";
        $query .= "     SIGEL_COURSE.SGL_COURSECODE, ";
        if ($model->field["SELSCHKIND"] == "J") {
            $query .= "     case ";
            $query .= "         when SIGEL_SCH.SCHREGNO_J is null then LTRIM(T1.SCHREGNO, '0') ";
            $query .= "         else LTRIM(SIGEL_SCH.SCHREGNO_J, '0') ";
            $query .= "     end AS SCHREGNO, ";
        } else if ($model->field["SELSCHKIND"] == "H") {
            $query .= "     case ";
            $query .= "         when SIGEL_SCH.SCHREGNO_H is null then 'H' || LTRIM(T1.SCHREGNO, '0') ";
            $query .= "         else LTRIM(SIGEL_SCH.SCHREGNO_H, '0') ";
            $query .= "     end AS SCHREGNO, ";
        } else {
            $query .= "     '' AS SCHREGNO, ";
        }
        $query .= "       CASE WHEN TRANS.TRANSFERCD = '2' THEN '2' ";
        $query .= "            WHEN REG_NOW.SCHREGNO is not null THEN '0' ";
        $query .= "            ELSE CASE WHEN GRD_T.GRD_DIV = '1' THEN '1' ";
        $query .= "                      WHEN GRD_T.GRD_DIV = '2' THEN '5' ";
        $query .= "                      WHEN GRD_T.GRD_DIV = '6' THEN '6' ";
        $query .= "                      WHEN GRD_T.GRD_DIV = '3' THEN '7' ";
        $query .= "                      ELSE '0' ";
        $query .= "                 END ";
        $query .= "       END AS ENROLL_STAT_TYPE,  ";
        $query .= "       CASE WHEN REG_NOW.SCHREGNO is not null THEN '0' ";
        $query .= "            ELSE CASE WHEN GRD_T.GRD_DIV IN ('1', '2', '3', '6') THEN '1' ELSE '0' END ";
        $query .= "       END AS ENROLL_TYPE,  ";
        $query .= "       '01' AS STUDENT_TYPE, ";
        $query .= "       '0' AS STUDENT_KUBUN, ";
        $query .= "       INT(VALUE(GDAT.GRADE, '0')) AS GRADE, ";
        $query .= "       CASE WHEN COMEBACK.SCHREGNO IS NOT NULL THEN '4' ";
        $query .= "            WHEN GRD_T.ENT_DIV = '4' THEN '1' ";
        $query .= "            WHEN GRD_T.ENT_DIV = '5' THEN '2' ";
        $query .= "            WHEN GRD_T.ENT_DIV = '7' THEN '3' ";
        $query .= "            ELSE '0' END AS ENT_TYPE, ";
        $query .= "       YEAR(GRD_T.ENT_DATE) AS ENT_YEAR, ";
        $query .= "       case when REG_NOW.SCHREGNO is not null then null ";
        $query .= "            else BASE.GRD_DATE ";
        $query .= "       end AS DROPOUT_DATE,  ";
        $query .= "       B_DETAIL003.BASE_REMARK1, ";
        $query .= "       '2' AS TUITION_SEND_TYPE, ";
        $query .= "       REG_BANK.BANKCD AS BANK_CD, ";
        $query .= "       BANK.BANKNAME_KANA AS BANK_KANA, ";
        $query .= "       REG_BANK.BRANCHCD AS BANK_BRANCH_CD, ";
        $query .= "       BANK.BRANCHNAME_KANA AS BANK_BRANCH_NAME, ";
        $query .= "       REG_BANK.DEPOSIT_ITEM AS BANK_DEPOSIT_TYPE, ";
        $query .= "       REG_BANK.ACCOUNTNO AS BANK_ACCOUNT_NO, ";
        $query .= "       REG_BANK.ACCOUNTNAME AS BANK_ACCOUNT_NAME, ";
        $query .= "       '1' AS ENT_SEMESTER ";
        $query .= " FROM ";
        $query .= "     SCHBASE T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT T2 ";
        $query .= "       ON T2.YEAR     = T1.YEAR ";
        $query .= "      AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "      AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ";
        $query .= "       ON BASE.SCHREGNO = T2.SCHREGNO ";
        $query .= "     LEFT JOIN ADDR ";
        $query .= "       ON ADDR.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN NEW_ZIPCD_T ZIP ";
        $query .= "       ON ZIP.NEW_ZIPCD = ADDR.ZIPCD ";
        $query .= "     LEFT JOIN PREF_MST PREF ";
        $query .= "       ON ZIP.PREF = PREF.PREF_NAME ";
        $query .= "     LEFT JOIN COLLECT_SGL_SCHREGNO_MAPPING_DAT SIGEL_SCH ";
        $query .= "       ON SIGEL_SCH.SCHREGNO = T2.SCHREGNO ";
        $query .= "     LEFT JOIN COLLECT_SGL_COURSE_MAPPING_DAT SIGEL_COURSE ";
        $query .= "       ON SIGEL_COURSE.YEAR     = T2.YEAR ";
        $query .= "      AND SIGEL_COURSE.GRADE    = T2.GRADE ";
        $query .= "      AND SIGEL_COURSE.HR_CLASS = T2.HR_CLASS ";
        $query .= "     LEFT JOIN TRANS ";
        $query .= "       ON TRANS.SCHREGNO = T2.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ";
        $query .= "       ON GDAT.YEAR  = T2.YEAR ";
        $query .= "      AND GDAT.GRADE = T2.GRADE ";
        $query .= "     LEFT JOIN COMEBACK ";
        $query .= "       ON COMEBACK.SCHREGNO = T2.SCHREGNO ";
        $query .= "     LEFT JOIN GRD_T ";
        $query .= "       ON GRD_T.SCHREGNO    = T2.SCHREGNO ";
        $query .= "      AND GRD_T.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "     LEFT JOIN SCHREG_BASE_DETAIL_MST B_DETAIL003 ";
        $query .= "       ON B_DETAIL003.SCHREGNO = T2.SCHREGNO ";
        $query .= "      AND B_DETAIL003.BASE_SEQ = '003' ";
        $query .= "     LEFT JOIN REGISTBANK_DAT REG_BANK ";
        $query .= "       ON REG_BANK.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "      AND REG_BANK.SCHREGNO    = T2.SCHREGNO ";
        $query .= "      AND REG_BANK.SEQ         = '1' ";
        $query .= "     LEFT JOIN BANK_MST BANK ";
        $query .= "       ON BANK.BANKCD   = REG_BANK.BANKCD ";
        $query .= "      AND BANK.BRANCHCD = REG_BANK.BRANCHCD ";
        $query .= "     LEFT JOIN REGD_NOW REG_NOW ";
        $query .= "       ON REG_NOW.SCHREGNO = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //COLLECT_SGL_SCHREGNO_MAPPING_DAT取得
    function getMappingCnt($schregNo) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     COLLECT_SGL_SCHREGNO_MAPPING_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregNo}' ";

        return $query;
    }

    //データの追加処理
    function InsertQuery($db, $model, $row) {
        $db->autoCommit(false);

        //存在チェック
        $query  = knjp820Query::getMappingCnt($row["SRC_SCHREGNO"]);
        $mapDat = $db->getRow($query, DB_FETCHMODE_ASSOC);

        $data = array();
        $data["SCHREGNO"][TEXT] = $row["SRC_SCHREGNO"];
        if ($model->field["SELSCHKIND"] == "J") {
            $data["SCHREGNO_J"][TEXT] = $row["SCHREGNO"];
        } else {
            $data["SCHREGNO_H"][TEXT] = $row["SCHREGNO"];
        }

        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        // マッピングテーブルが空の時は、Insert
        if ($mapDat["SCHREGNO"] == '') {
            $query = Query::insertSQL($data, "COLLECT_SGL_SCHREGNO_MAPPING_DAT");
            $db->query($query);
        } else {
            // マッピングテーブルが存在し、高校で、高校学籍番号が無い時、Insert
            if ($model->field["SELSCHKIND"] == "H" && $mapDat["SCHREGNO_H"] == '') {
                $where  = " WHERE ";
                $where .= "     SCHREGNO = '".$mapDat["SCHREGNO"]."' ";

                $query = Query::updateSQL($data, "COLLECT_SGL_SCHREGNO_MAPPING_DAT", $where);
                $db->query($query);
            }
        }

        $db->commit();

        return;
    }

}
?>
