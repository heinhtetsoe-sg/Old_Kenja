<?php

require_once('for_php7.php');

class knje464bQuery extends Query {
    //学部取得
    function getSchoolKind() {
        $query  = " SELECT ";
        $query .= "     NAME1 || ':' || ABBV1 AS LABEL, ";
        $query .= "     NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //年組取得（権限チェック）
    function getHrClass($year, $model)
    {
        //参照・更新可
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT T2 ";
        $query .= "       ON T2.YEAR = T1.YEAR ";
        $query .= "      AND T2.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$year."' ";
        if ($model->schoolKind == "") {
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == '1' && SCHOOLKIND != '') {
                $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        } else {
            $query .= " AND T2.SCHOOL_KIND = '".$model->schoolKind."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (   T1.TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR T1.TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR T1.TR_CD3 = '".STAFFCD."' ";
            $query .= "          OR T1.SUBTR_CD1 = '".STAFFCD."' ";
            $query .= "          OR T1.SUBTR_CD2 = '".STAFFCD."' ";
            $query .= "          OR T1.SUBTR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //リスト取得（左フレーム）
    function getList($model, $keyCnt="") {
        $query  = " SELECT ";
        if ($keyCnt != "") {
            $query .= " COUNT(*) ";
        } else {
            $query .= "   T1.SPRT_FACILITY_GRP, ";
            $query .= "   T1.SPRT_FACIL_GRP_NAME, ";
            $query .= "   T3.SPRT_FACILITY_NAME ";
        }
        $query .= " FROM ";
        $query .= "   CHALLENGED_SUPPORT_FACILITY_GRP_MST T1 ";
        $query .= "   LEFT JOIN CHALLENGED_SUPPORT_FACILITY_GRP_DAT T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "    AND T2.SPRT_FACILITY_GRP = T1.SPRT_FACILITY_GRP ";
        $query .= "   LEFT JOIN CHALLENGED_SUPPORT_FACILITY_YMST T3 ";
        $query .= "     ON T3.YEAR = T2.YEAR ";
        $query .= "    AND T3.SPRT_FACILITY_CD = T2.SPRT_FACILITY_CD ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".CTRL_YEAR."' ";
        if ($keyCnt != "") {
            $query .= "   AND T1.SPRT_FACILITY_GRP = '".$keyCnt."' ";
        } else {
            $query .= " ORDER BY T1.SPRT_FACILITY_GRP, T3.SPRT_FACILITY_CD ";
        }
        return $query;
    }
    
    function getGroupNameYmst($model, $cd="") {
        $db = Query::dbCheckOut();
        $query  = " SELECT ";
        $query .= "   T1.SPRT_FACILITY_GRP AS IPT_FACILITY_GRP, ";
        $query .= "   T1.SPRT_FACIL_GRP_NAME AS IPT_GROUPNAME ";
        $query .= " FROM ";
        $query .= "   CHALLENGED_SUPPORT_FACILITY_GRP_MST T1 ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".CTRL_YEAR."' ";
        if ($cd != "") {
            $query .= "   AND T1.SPRT_FACILITY_GRP = '".$cd."' ";
        }
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    function getSupportFacilities($grpcd, $getData=false) {
        $query .= " SELECT ";
        $query .= "   T2.SPRT_FACILITY_CD || ' ' || T2.SPRT_FACILITY_NAME AS LABEL, ";
        $query .= "   T2.SPRT_FACILITY_CD AS VALUE ";
        $query .= " FROM ";
        //$getDataがtrueなら、登録済みのデータを取る。それ以外はマスタのデータで取る。
        if ($getData==true) {
            $query .= "  CHALLENGED_SUPPORT_FACILITY_GRP_DAT T1 ";
            $query .= "   LEFT JOIN CHALLENGED_SUPPORT_FACILITY_YMST T2 ";
            $query .= "     ON T2.YEAR = T1.YEAR ";
            $query .= "    AND T2.SPRT_FACILITY_CD = T1.SPRT_FACILITY_CD ";
            $query .= " WHERE ";
            $query .= "   T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "   AND T1.SPRT_FACILITY_GRP = '".$grpcd."' ";
        } else {
            $query .= "   CHALLENGED_SUPPORT_FACILITY_YMST T2 ";
            $query .= " WHERE ";
            $query .= "   T2.YEAR = '".CTRL_YEAR."' ";
        }
        $query .= " ORDER BY ";
        $query .= "   VALUE ";
        return $query;
    }

    function getSelSchregNo($model, $getData="") {
        $query  = " WITH MXSCHREG AS ( ";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     MAX(SEMESTER) AS SEMESTER, ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " GROUP BY ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        if ($getData == "DataGet") {
            $query .= "   T1.SCHREGNO || ' ' || T3.NAME AS LABEL, ";
            $query .= "   T1.SCHREGNO AS VALUE ";
            $query .= " FROM ";
            $query .= "   SCHREG_CHALLENGED_SUPPORT_FACILITY_GRP_DAT T1 ";
            $query .= "   INNER JOIN MXSCHREG T2WK ";
            $query .= "     ON T2WK.YEAR = T1.YEAR ";
            $query .= "    AND T2WK.SCHREGNO = T1.SCHREGNO ";
        } else if ($getData == "GetNot") {
            $query .= " DISTINCT ";
            $query .= "   T1.SPRT_FACILITY_GRP AS LABEL, ";
            $query .= "   T1.SCHREGNO AS VALUE ";
            $query .= " FROM ";
            $query .= "   SCHREG_CHALLENGED_SUPPORT_FACILITY_GRP_DAT T1 ";
            $query .= "   INNER JOIN MXSCHREG T2WK ";
            $query .= "     ON T2WK.YEAR = T1.YEAR ";
            $query .= "    AND T2WK.SCHREGNO = T1.SCHREGNO ";
        } else if ($getData == "GetSchK") {
            $query .= " DISTINCT ";
            $query .= "   T5.SCHOOL_KIND ";
            $query .= " FROM ";
            $query .= "   SCHREG_CHALLENGED_SUPPORT_FACILITY_GRP_DAT T1 ";
            $query .= "   INNER JOIN MXSCHREG T2WK ";
            $query .= "     ON T2WK.YEAR = T1.YEAR ";
            $query .= "    AND T2WK.SCHREGNO = T1.SCHREGNO ";
        } else {
            $query .= "   T2WK.SCHREGNO || ' ' || T3.NAME AS LABEL, ";
            $query .= "   T2WK.SCHREGNO AS VALUE ";
            $query .= " FROM ";
            $query .= " MXSCHREG T2WK ";
        }
        $query .= "   LEFT JOIN SCHREG_REGD_DAT T2 ";
        $query .= "     ON T2.YEAR = T2WK.YEAR ";
        $query .= "    AND T2.SEMESTER = T2WK.SEMESTER ";
        $query .= "    AND T2.SCHREGNO = T2WK.SCHREGNO ";
        $query .= "   LEFT JOIN SCHREG_BASE_MST T3 ";
        $query .= "     ON T3.SCHREGNO = T2.SCHREGNO ";
        $query .= "   LEFT JOIN SCHREG_REGD_HDAT T4 ";
        $query .= "     ON T4.YEAR = T2.YEAR ";
        $query .= "    AND T4.SEMESTER = T2.SEMESTER ";
        $query .= "    AND T4.GRADE = T2.GRADE ";
        $query .= "    AND T4.HR_CLASS = T2.HR_CLASS ";
        $query .= "   LEFT JOIN SCHREG_REGD_GDAT T5 ";
        $query .= "     ON T5.YEAR = T2.YEAR ";
        $query .= "    AND T5.GRADE = T2.GRADE ";
        if ($getData == "DataGet" || $getData == "GetSchK") {
            $query .= " WHERE ";
            $query .= "   T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "   AND T1.SPRT_FACILITY_GRP = '".$model->field["IPT_FACILITY_GRP"]."' ";
        } else if ($getData == "GetNot") {
            $query .= " WHERE ";
            $query .= "   T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "   AND T1.SPRT_FACILITY_GRP <> '".$model->field["IPT_FACILITY_GRP"]."' ";
        } else if ($getData == "") {
            $query .= " WHERE ";
            $query .= "   T2WK.YEAR = '".CTRL_YEAR."' ";
            $query .= "   AND T2.GRADE || T2.HR_CLASS = '".$model->hr_class."' ";
        }
        
        return $query;
    }

    //更新
    function &UpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        /*****************************************/
        /*  CHALLENGED_SUPPORT_FACILITY_GRP_MST  */
        /*****************************************/
        //削除
        $query  = " DELETE FROM  ";
        $query .= "     CHALLENGED_SUPPORT_FACILITY_GRP_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR                = '".$model->year."' ";
        $query .= " AND SPRT_FACILITY_GRP   = '".$model->field["IPT_FACILITY_GRP"]."' ";

        $db->query($query);

        //追加
        if ($model->cmd != "delete") {
            $data = array();
            $data["YEAR"][TEXT]                = $model->year;
            $data["SPRT_FACILITY_GRP"][TEXT]   = $model->field["IPT_FACILITY_GRP"];
            $data["SPRT_FACIL_GRP_NAME"][TEXT] = $model->field["IPT_GROUPNAME"];
            $data["REGISTERCD"][TEXT]          = STAFFCD;
            $data["UPDATED"][FUNC]             = "sysdate()";
            $query = Query::insertSQL($data, "CHALLENGED_SUPPORT_FACILITY_GRP_MST");

            $db->query($query);
        }

        /*******************************/
        /*  CHALLENGED_SUPPORT_FACILITY_GRP_DAT  */
        /*******************************/
        //削除
        $query  = " DELETE FROM  ";
        $query .= "     CHALLENGED_SUPPORT_FACILITY_GRP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR                = '".$model->year."' ";
        $query .= " AND SPRT_FACILITY_GRP   = '".$model->field["IPT_FACILITY_GRP"]."' ";

        $db->query($query);

        //追加
        $selectdata = explode(',', $model->selectdata);
        if ($model->selectdata) {
            for ($i=0; $i < get_count($selectdata); $i++) {
                $data = array();
                $data["YEAR"][TEXT]                 = $model->year;
                $data["SPRT_FACILITY_GRP"][TEXT]    = $model->field["IPT_FACILITY_GRP"];
                $data["SPRT_FACILITY_CD"][TEXT]     = $selectdata[$i];
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][FUNC]              = "sysdate()";
                $query = Query::insertSQL($data, "CHALLENGED_SUPPORT_FACILITY_GRP_DAT");

                $db->query($query);
            }
        }

        $db->commit();      // トランザクションをコミットする。

        Query::dbCheckIn($db);
        return;
    }

    //更新狙い用
    function &UpdateQueryAim($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        /*******************************/
        /*  SCHREG_CHALLENGED_SUPPORT_FACILITY_GRP_DAT  */
        /*******************************/
        //削除
        $query  = " DELETE FROM  ";
        $query .= "     SCHREG_CHALLENGED_SUPPORT_FACILITY_GRP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR                = '".$model->year."' ";
        $query .= " AND SPRT_FACILITY_GRP   = '".$model->field["IPT_FACILITY_GRP"]."' ";

        $db->query($query);

        if ($model->cmd != "delete") {
            //追加
            $selectdata2 = explode(',', $model->selectdata2);
            if ($model->selectdata2) {
                for ($i=0; $i < get_count($selectdata2); $i++) {
                    $data = array();
                    $data["YEAR"][TEXT]              = $model->year;
                    $data["SPRT_FACILITY_GRP"][TEXT] = $model->field["IPT_FACILITY_GRP"];
                    $data["SCHREGNO"][TEXT]          = $selectdata2[$i];
                    $data["REGISTERCD"][TEXT]        = STAFFCD;
                    $data["UPDATED"][FUNC]           = "sysdate()";
                    $query = Query::insertSQL($data, "SCHREG_CHALLENGED_SUPPORT_FACILITY_GRP_DAT");

                    $db->query($query);
                }
            }
        }

        $db->commit();      // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    function chkChallengedSupportFacilityGrpMst($model, $year) {
        $query  = " SELECT ";
        $query .= "   COUNT(*) ";
        $query .= " FROM ";
        $query .= "   CHALLENGED_SUPPORT_FACILITY_GRP_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".$year."' ";
        return $query;
    }

    function chkChallengedSupportFacilityGrpDat($model, $year) {
        $query  = " SELECT ";
        $query .= "   COUNT(*) ";
        $query .= " FROM ";
        $query .= "   CHALLENGED_SUPPORT_FACILITY_GRP_DAT ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".$year."' ";
        return $query;
    }

    function chkSchregChallengedSupportFacilityGrpDat($model, $year) {
        $query  = " SELECT ";
        $query .= "   COUNT(*) ";
        $query .= " FROM ";
        $query .= "   SCHREG_CHALLENGED_SUPPORT_FACILITY_GRP_DAT ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".$year."' ";
        $query .= "   AND SPRT_FACILITY_GRP = '".$model->field["IPT_FACILITY_GRP"]."' ";
        return $query;
    }

    function copyChallengedSupportFacilityGrpMst($model) {
        $query  = " INSERT INTO CHALLENGED_SUPPORT_FACILITY_GRP_MST ";
        $query .= " SELECT ";
        $query .= "   '".CTRL_YEAR."' AS YEAR, ";
        $query .= "   SPRT_FACILITY_GRP, ";
        $query .= "   SPRT_FACIL_GRP_NAME, ";
        $query .= "   '".STAFFCD."' AS REGISTERCD, ";
        $query .= "   SYSDATE() AS UPDATED ";
        $query .= " FROM ";
        $query .= "   CHALLENGED_SUPPORT_FACILITY_GRP_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".(CTRL_YEAR-1)."' ";
        return $query;
    }

    function copyChallengedSupportFacilityGrpDat($model) {
        $query  = " INSERT INTO CHALLENGED_SUPPORT_FACILITY_GRP_DAT ";
        $query .= " SELECT ";
        $query .= "   '".CTRL_YEAR."' AS YEAR, ";
        $query .= "   SPRT_FACILITY_GRP, ";
        $query .= "   SPRT_FACILITY_CD, ";
        $query .= "   '".STAFFCD."' AS REGISTERCD, ";
        $query .= "   SYSDATE() AS UPDATED ";
        $query .= " FROM ";
        $query .= "   CHALLENGED_SUPPORT_FACILITY_GRP_DAT ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".(CTRL_YEAR-1)."' ";
        return $query;
    }

    function copySchregChallengedSupportFacilityGrpDat($model) {
        $query  = " INSERT INTO SCHREG_CHALLENGED_SUPPORT_FACILITY_GRP_DAT ";
        $query .= " SELECT ";
        $query .= "   '".CTRL_YEAR."' AS YEAR, ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T1.SPRT_FACILITY_GRP, ";
        $query .= "   '".STAFFCD."' AS REGISTERCD, ";
        $query .= "   SYSDATE() AS UPDATED ";
        $query .= " FROM ";
        $query .= "   SCHREG_CHALLENGED_SUPPORT_FACILITY_GRP_DAT T1 ";
        $query .= "   LEFT JOIN SCHREG_BASE_MST T2 ";
        $query .= "     ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".(CTRL_YEAR-1)."' ";
        $query .= "   AND T1.SPRT_FACILITY_GRP = '".$model->field["IPT_FACILITY_GRP"]."' ";
        $query .= "   AND (T2.GRD_DATE IS NULL OR T2.GRD_DATE >= '".CTRL_YEAR."-04-01' ) ";
        return $query;
    }
}
?>
