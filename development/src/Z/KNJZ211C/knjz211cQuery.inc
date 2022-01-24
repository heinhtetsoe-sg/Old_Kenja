<?php

require_once('for_php7.php');

class knjz211cQuery extends Query {
    //校種取得
    function getSchKind($model, $val="") {
        $query  = " SELECT DISTINCT ";
        $query .= "     ABBV1 AS LABEL, ";
        $query .= "     NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND NAME1 = '".SCHOOLKIND."' ";
        }
        if ($val) {
            $query .= " AND NAME1 = '".$val."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧取得
    function getJviewstatLevelPatternYmst($model, $pattern_cd="") {
        $query  = " WITH PATTERN_DAT AS ( ";
        $query .= "     SELECT ";
        $query .= "         PATTERN_CD, ";
        $query .= "         COUNT(ASSESSLEVEL) AS ASSESSLEVEL_CNT ";
        $query .= "     FROM ";
        $query .= "         JVIEWSTAT_LEVEL_PATTERN_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "         SCHOOL_KIND = '".$model->schoolkind."' ";
        $query .= "     GROUP BY ";
        $query .= "         PATTERN_CD ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     L1.ASSESSLEVEL_CNT ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_LEVEL_PATTERN_YMST T1 ";
        $query .= "     LEFT JOIN PATTERN_DAT L1 ON T1.PATTERN_CD = L1.PATTERN_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SCHOOL_KIND  = '".$model->schoolkind."' ";
        if ($pattern_cd) {
            $query .= " AND T1.PATTERN_CD  = '".$pattern_cd."' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     T1.PATTERN_CD ";
        }

        return $query;
    }

    //データ取得
    function getJviewstatLevelPatternDat($model, $pattern_cd) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_LEVEL_PATTERN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->schoolkind."' AND ";
        $query .= "     PATTERN_CD  = '".$pattern_cd."' ";
        $query .= " ORDER BY ";
        $query .= "     ASSESSLEVEL ";

        return $query;
    }

    //存在チェック -- JVIEWSTAT_LEVEL_PATTERN_YMST
    function checkExistsYmst($year, $school_kind, $pattern_cd="") {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_LEVEL_PATTERN_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$year."' AND ";
        $query .= "     SCHOOL_KIND = '".$school_kind."' ";
        if ($pattern_cd) {
            $query .= " AND PATTERN_CD  = '".$pattern_cd."' ";
        }

        return $query;
    }

    /******************/
    /*    更新処理    */
    /******************/

    //更新処理
    function &getUpdateQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        // JVIEWSTAT_LEVEL_PATTERN_YMST
        $data = array();
        if ($model->cmd == "add") {
            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["SCHOOL_KIND"][TEXT]  = $fields["SCHOOL_KIND"];
            $data["PATTERN_CD"][TEXT]   = $fields["PATTERN_CD"];
        }
        $data["PATTERN_NAME"][TEXT]     = $fields["PATTERN_NAME"];
        $data["PERFECT"][NUMBER]        = $fields["PERFECT"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        if ($model->cmd == "add") {
            $query = Query::insertSQL($data, "JVIEWSTAT_LEVEL_PATTERN_YMST");
        } else {
            $where  = " WHERE ";
            $where .= "     YEAR            = '".CTRL_YEAR."' AND ";
            $where .= "     SCHOOL_KIND     = '".$fields["SCHOOL_KIND"]."' AND ";
            $where .= "     PATTERN_CD      = '".$fields["PATTERN_CD"]."' ";

            $query = Query::updateSQL($data, "JVIEWSTAT_LEVEL_PATTERN_YMST", $where);
        }
        $db->query($query);

        // JVIEWSTAT_LEVEL_PATTERN_DAT
        $query  = " DELETE FROM ";
        $query .= "     JVIEWSTAT_LEVEL_PATTERN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND     = '".$fields["SCHOOL_KIND"]."' AND ";
        $query .= "     PATTERN_CD      = '".$fields["PATTERN_CD"]."' ";
        $db->query($query);

        for ($i = 1; $i <= $fields["ASSESSLEVEL_CNT"]; $i++) {
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SCHOOL_KIND"][TEXT]      = $fields["SCHOOL_KIND"];
            $data["PATTERN_CD"][TEXT]       = $fields["PATTERN_CD"];
            $data["ASSESSLEVEL"][NUMBER]    = $i;
            $data["ASSESSMARK"][TEXT]       = $fields["ASSESSMARK-".$i];
            $data["ASSESSLOW"][NUMBER]      = $fields["ASSESSLOW-".$i];
            $data["ASSESSHIGH"][NUMBER]     = $fields["ASSESSHIGH-".$i];
            $data["ASSESS_SHOW1"][TEXT]     = $fields["ASSESS_SHOW1-".$i];
            $data["ASSESS_SHOW2"][TEXT]     = $fields["ASSESS_SHOW2-".$i];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $query = Query::insertSQL($data, "JVIEWSTAT_LEVEL_PATTERN_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //削除処理
    function &getDeleteQuery($model, $school_kind, $pattern_cd="") {
        $db = Query::dbCheckOut();

        // JVIEWSTAT_LEVEL_PATTERN_YMST
        $query  = " DELETE FROM ";
        $query .= "     JVIEWSTAT_LEVEL_PATTERN_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND = '".$school_kind."' ";
        if ($pattern_cd) {
            $query .= " AND PATTERN_CD  = '".$pattern_cd."' ";
        }
        $db->query($query);

        // JVIEWSTAT_LEVEL_PATTERN_DAT
        $query  = " DELETE FROM ";
        $query .= "     JVIEWSTAT_LEVEL_PATTERN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND = '".$school_kind."' ";
        if ($pattern_cd) {
            $query .= " AND PATTERN_CD  = '".$pattern_cd."' ";
        }
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    /********************/
    /*    コピー処理    */
    /********************/

    //コピー用データ取得
    function getCopyData($model, $year) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.PATTERN_CD, ";
        $query .= "     T1.PATTERN_NAME, ";
        $query .= "     T1.PERFECT, ";
        $query .= "     T2.ASSESSLEVEL, ";
        $query .= "     T2.ASSESSMARK, ";
        $query .= "     T2.ASSESSLOW, ";
        $query .= "     T2.ASSESSHIGH, ";
        $query .= "     T2.ASSESS_SHOW1, ";
        $query .= "     T2.ASSESS_SHOW2 ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_LEVEL_PATTERN_YMST T1 ";
        $query .= "     INNER JOIN JVIEWSTAT_LEVEL_PATTERN_DAT T2 ";
        $query .= "          ON T1.YEAR         = T2.YEAR ";
        $query .= "         AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
        $query .= "         AND T1.PATTERN_CD   = T2.PATTERN_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".$year."' AND ";
        $query .= "     T1.SCHOOL_KIND  = '".$model->schoolkind."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.PATTERN_CD, ";
        $query .= "     T2.ASSESSLEVEL ";

        return $query;
    }

    //コピー処理
    function &getCopyQuery($model) {
        $db = Query::dbCheckOut();

        //前年度からコピー
        $pattern_cd = "";
        $query = knjz211cQuery::getCopyData($model, (CTRL_YEAR - 1));
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($pattern_cd != $row["PATTERN_CD"]) {
                // JVIEWSTAT_LEVEL_PATTERN_YMST
                $data = array();
                $data["YEAR"][TEXT]             = CTRL_YEAR;
                $data["SCHOOL_KIND"][TEXT]      = $model->schoolkind;
                $data["PATTERN_CD"][TEXT]       = $row["PATTERN_CD"];
                $data["PATTERN_NAME"][TEXT]     = $row["PATTERN_NAME"];
                $data["PERFECT"][NUMBER]        = $row["PERFECT"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "sysdate()";

                $query = Query::insertSQL($data, "JVIEWSTAT_LEVEL_PATTERN_YMST");
                $db->query($query);
            }

            // JVIEWSTAT_LEVEL_PATTERN_DAT
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SCHOOL_KIND"][TEXT]      = $model->schoolkind;
            $data["PATTERN_CD"][TEXT]       = $row["PATTERN_CD"];
            $data["ASSESSLEVEL"][NUMBER]    = $row["ASSESSLEVEL"];
            $data["ASSESSMARK"][TEXT]       = $row["ASSESSMARK"];
            $data["ASSESSLOW"][NUMBER]      = $row["ASSESSLOW"];
            $data["ASSESSHIGH"][NUMBER]     = $row["ASSESSHIGH"];
            $data["ASSESS_SHOW1"][TEXT]     = $row["ASSESS_SHOW1"];
            $data["ASSESS_SHOW2"][TEXT]     = $row["ASSESS_SHOW2"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $query = Query::insertSQL($data, "JVIEWSTAT_LEVEL_PATTERN_DAT");
            $db->query($query);

            $pattern_cd = $row["PATTERN_CD"];
        }
        $result->free();

        Query::dbCheckIn($db);
        return;
    }
}
?>
