<?php

require_once('for_php7.php');

class knjz252Query extends Query {

    //校種取得
    function getSchkind($model) {
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
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //リスト内情報取得CNT
    function ReadQueryCnt($model) {
        $query  = " SELECT ";
        $query .= "     PROGRAMID, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     PRG_STAMP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '9' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND SCHOOLCD = '000000000000' ";
            $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";
        }
        $query .= " GROUP BY ";
        $query .= "     PROGRAMID ";

        return $query;
    }

    //リスト内情報取得
    function ReadQuery($model, $prgId = "") {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     L1.STAFFNAME ";
        $query .= " FROM ";
        $query .= "     PRG_STAMP_DAT T1 ";
        $query .= "     LEFT JOIN STAFF_MST L1 ON T1.FILE_NAME = L1.STAFFCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '9' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T1.SCHOOLCD = '000000000000' ";
            $query .= "     AND T1.SCHOOL_KIND = '{$model->schoolKind}' ";
        }
        if ($prgId) {
            $query .= "     AND T1.PROGRAMID = '{$prgId}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.PROGRAMID, ";
        $query .= "     CAST(T1.SEQ AS INTEGER) ";

        return $query;
    }

    //印影職員
    function getIneiStaff($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.STAFFCD AS VALUE, ";
        $query .= "     T1.STAFFCD || ':' || L1.STAFFNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ATTEST_INKAN_DAT T1 ";
        $query .= "     INNER JOIN V_STAFF_MST L1 ON L1.YEAR    = '".CTRL_YEAR."' ";
        $query .= "                              AND T1.STAFFCD = L1.STAFFCD ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /*INSERT*/
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();

        foreach ($model->arrTitle as $seqCnt => $val ) {
            if ($model->arrName[$seqCnt] != "" || $val != "") {
                $data["YEAR"][TEXT]            = CTRL_YEAR;
                $data["SEMESTER"][TEXT]        = 9;
                if ($model->Properties["useSchool_KindField"] == "1") {
                    $data["SCHOOLCD"][TEXT]        = "000000000000";
                    $data["SCHOOL_KIND"][TEXT]     = $model->schoolKind;
                }
                $data["PROGRAMID"][TEXT]       = $model->field["R_PROGRAMID"];
                $data["SEQ"][TEXT]             = $seqCnt;
                $data["TITLE"][TEXT]           = $model->field["R_TITLE".$seqCnt];
                $data["FILE_NAME"][TEXT]       = $model->field["R_FILE_NAME".$seqCnt];

                $data["REGISTERCD"][TEXT]      = STAFFCD;
                $data["UPDATED"][FUNC]         = "sysdate()";

                $query = Query::insertSQL($data, "PRG_STAMP_DAT");
                $db->query($query);
            }
        }
        Query::dbCheckIn($db);

        return true;
    }

    /*DELETE*/
    function &getDeleteQuery($model, $seq) {
     $db = Query::dbCheckOut();

        $query  = " DELETE FROM PRG_STAMP_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER    = '9' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND SCHOOLCD = '000000000000' ";
            $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";
        }
        $query .= "     AND PROGRAMID   = '".$model->field["R_PROGRAMID"]."' ";
        if ($seq != "") {
            $query .= "     AND SEQ         = '".$seq."' ";
        }

        $db->query($query);
        if ($seq == "") {
            Query::dbCheckIn($db);
        }

        return true;
    }

    //存在カウント
    function countPrgStampDat($model) {
        $query  = " SELECT ";
        $query .= "     PROGRAMID, ";
        $query .= "     SEQ ";
        $query .= " FROM ";
        $query .= "     PRG_STAMP_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER    = '9' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND SCHOOLCD = '000000000000' ";
            $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";
        }
        $query .= "     AND PROGRAMID   = '".$model->field["R_PROGRAMID"]."' ";
        $query .= "     AND SEQ         IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     SEQ ";

        return $query;
    }

    /*UPDATE*/
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $query      = knjz252Query::countPrgStampDat($model);
        $result     = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $checkSeq[$row["SEQ"]] = $row["SEQ"];
        }

        for ($seqCnt = 1; $seqCnt <= $model->maxSeq; $seqCnt++) {
            knjz252Query::getDeleteQuery($model, $seqCnt);
            if (trim($model->field["R_TITLE".$seqCnt]) == "" && trim($model->field["R_FILE_NAME".$seqCnt]) == "") {
                //空データは登録しない
            } else {
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["SEMESTER"][TEXT]     = 9;
                if ($model->Properties["useSchool_KindField"] == "1") {
                    $data["SCHOOLCD"][TEXT]        = "000000000000";
                    $data["SCHOOL_KIND"][TEXT]     = $model->schoolKind;
                }
                $data["PROGRAMID"][TEXT]    = $model->field["R_PROGRAMID"];
                $data["SEQ"][TEXT]          = $seqCnt;
                $data["TITLE"][TEXT]        = $model->field["R_TITLE".$seqCnt];
                $data["FILE_NAME"][TEXT]    = $model->field["R_FILE_NAME".$seqCnt];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $query = Query::insertSQL($data, "PRG_STAMP_DAT");
                $db->query($query);
            }
        }
        Query::dbCheckIn($db);

        return true;
    }

    //存在チェック
    function chkPrgStampDat($model, $year) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     PRG_STAMP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$year."' AND ";
        $query .= "     SEMESTER    = '9' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOLCD    = '000000000000' ";
            $query .= " AND SCHOOL_KIND = '{$model->schoolKind}' ";
        }

        return $query;
    }

    //前年度からのコピー
    function &getCopyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $this_year  = CTRL_YEAR;
        $pre_year   = CTRL_YEAR - 1;

        $query  = " INSERT INTO PRG_STAMP_DAT ";
        $query .= " SELECT ";
        $query .= "     '".$this_year."', ";
        $query .= "     T1.SEMESTER, ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     T1.SCHOOLCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
        }
        $query .= "     T1.PROGRAMID, ";
        $query .= "     T1.SEQ, ";
        $query .= "     T1.TITLE, ";
        $query .= "     CASE WHEN T1.FILE_NAME IS NOT NULL AND T1.FILE_NAME = L1.STAFFCD THEN T1.FILE_NAME END AS FILE_NAME, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     PRG_STAMP_DAT T1 ";
        $query .= "     LEFT JOIN (SELECT DISTINCT ";
        $query .= "                     S1.STAFFCD ";
        $query .= "                 FROM ";
        $query .= "                     ATTEST_INKAN_DAT S1, ";
        $query .= "                     V_STAFF_MST S2 ";
        $query .= "                 WHERE ";
        $query .= "                     S2.YEAR     = '".$this_year."' AND ";
        $query .= "                     S1.STAFFCD  = S2.STAFFCD ";
        $query .= "                 ) L1 ON T1.FILE_NAME = L1.STAFFCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".$pre_year."' AND ";
        $query .= "     T1.SEMESTER     = '9' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T1.SCHOOLCD     = '000000000000' ";
            $query .= " AND T1.SCHOOL_KIND  = '{$model->schoolKind}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.PROGRAMID, ";
        $query .= "     T1.SEQ ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
   }
}
?>
