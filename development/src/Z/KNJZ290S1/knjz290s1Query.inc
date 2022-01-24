<?php

require_once('for_php7.php');

class knjz290s1Query extends Query {

    //出身学校年度一覧取得
    function selectSchoolCd()
    {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //リスト取得
    function getList()
    {
        $query  = " WITH MAX_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     MAX(T1.FROM_DATE) AS FROM_DATE ";
        $query .= " FROM ";
        $query .= "     STAFF_PRINCIPAL_HIST_DAT T1 ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHOOL_KIND ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.FROM_DATE, ";
        $query .= "     T1.TO_DATE, ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     L1.STAFFNAME, ";
        $query .= "     L3.SCHOOL_KIND AS IS_MAX ";
        $query .= " FROM ";
        $query .= "     STAFF_PRINCIPAL_HIST_DAT T1 ";
        $query .= "     LEFT JOIN STAFF_MST L1 ON T1.STAFFCD = L1.STAFFCD ";
        $query .= "     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'A023' ";
        $query .= "          AND T1.SCHOOL_KIND = L2.NAME1 ";
        $query .= "     LEFT JOIN MAX_DATA L3 ON T1.SCHOOL_KIND = L3.SCHOOL_KIND ";
        $query .= "          AND T1.FROM_DATE = L3.FROM_DATE ";
        $query .= " ORDER BY ";
        $query .= "     L2.NAMECD2, ";
        $query .= "     T1.FROM_DATE ";

        return $query;
    }

    //１レコード取得
    function getDispData($field)
    {
        $fromDate = str_replace("/", "-", $field["FROM_DATE"]);
        $query  = " SELECT ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.FROM_DATE, ";
        $query .= "     T1.TO_DATE, ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     L1.STAFFNAME ";
        $query .= " FROM ";
        $query .= "     STAFF_PRINCIPAL_HIST_DAT T1 ";
        $query .= "     LEFT JOIN STAFF_MST L1 ON T1.STAFFCD = L1.STAFFCD ";
        $query .= "     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'A023' ";
        $query .= "          AND T1.SCHOOL_KIND = L2.NAME1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOL_KIND = '{$field["SCHOOL_KIND"]}' ";
        $query .= "     AND T1.FROM_DATE = '{$fromDate}' ";
        $query .= "     AND T1.STAFFCD = '{$field["STAFFCD"]}' ";

        return $query;
    }

    //校種
    function getSchoolKind()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //職員
    function getStaff($model)
    {
        if ($model->Properties["useSchoolWare"] == "1") {
            $query .= " SELECT ";
            $query .= "     STF.STAFFCD || '：' || STF.STAFFNAME AS LABEL, ";
            $query .= "     STF.STAFFCD AS VALUE ";
            $query .= " FROM ";
            $query .= "     STAFF_WORK_HIST_DAT STF_WORK ";
            $query .= "     INNER JOIN STAFF_MST STF ON STF_WORK.STAFFCD = STF.STAFFCD ";
            $query .= " WHERE ";
            $query .= "     STF_WORK.FROM_SCHOOLCD = '{$model->schoolCd}' ";
            $query .= " GROUP BY ";
            $query .= "     STF.STAFFCD, ";
            $query .= "     STF.STAFFNAME ";
            $query .= " ORDER BY ";
            $query .= "     STF.STAFFCD ";
        } else {
            $query  = " SELECT ";
            $query .= "     STAFFCD || '：' || STAFFNAME AS LABEL, ";
            $query .= "     STAFFCD AS VALUE ";
            $query .= " FROM ";
            $query .= "     V_STAFF_MST ";
            $query .= " GROUP BY ";
            $query .= "     STAFFCD, ";
            $query .= "     STAFFNAME ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }
        return $query;
    }

    //１レコード取得
    function isNewData($field)
    {
        $fromDate = str_replace("/", "-", $field["FROM_DATE"]);
        $query  = " WITH MAX_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     MAX(T1.FROM_DATE) AS FROM_DATE ";
        $query .= " FROM ";
        $query .= "     STAFF_PRINCIPAL_HIST_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOL_KIND = '{$field["SCHOOL_KIND"]}' ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.STAFFCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MAX_DATA T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOL_KIND = '{$field["SCHOOL_KIND"]}' ";
        $query .= "     AND T1.FROM_DATE = '{$fromDate}' ";
        $query .= "     AND T1.STAFFCD = '{$field["STAFFCD"]}' ";

        return $query;
    }

    //ひとつ前のデータ取得
    function getLastDispData($field)
    {
        $fromDate = str_replace("/", "-", $field["FROM_DATE"]);
        $query  = " WITH MAX_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     MAX(T1.FROM_DATE) AS FROM_DATE ";
        $query .= " FROM ";
        $query .= "     STAFF_PRINCIPAL_HIST_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOL_KIND = '{$field["SCHOOL_KIND"]}' ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHOOL_KIND ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     MAX(T1.FROM_DATE) AS FROM_DATE ";
        $query .= " FROM ";
        $query .= "     STAFF_PRINCIPAL_HIST_DAT T1, ";
        $query .= "     MAX_DATA T2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "     AND T1.FROM_DATE < T2.FROM_DATE ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHOOL_KIND ";

        return $query;
    }

    //ひとつ前のデータ取得
    function getMaxDispData($field)
    {
        $fromDate = str_replace("/", "-", $field["FROM_DATE"]);
        $query  = " SELECT ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     MAX(T1.FROM_DATE) AS FROM_DATE ";
        $query .= " FROM ";
        $query .= "     STAFF_PRINCIPAL_HIST_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOL_KIND = '{$field["SCHOOL_KIND"]}' ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHOOL_KIND ";

        return $query;
    }

    function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        list($year, $month, $day) = preg_split("/\//", $fields["FROM_DATE"]);
        $updToDate = date("Y-m-d", mktime( 0, 0, 0, $month, $day - 1, $year ));

        $query  = " UPDATE STAFF_PRINCIPAL_HIST_DAT T1 ";
        $query .= " SET T1.TO_DATE = '{$updToDate}' ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOL_KIND = '{$fields["SCHOOL_KIND"]}' ";
        $query .= "     AND T1.FROM_DATE IN ( ";
        $query .= "         SELECT ";
        $query .= "             MAX(I1.FROM_DATE) AS FROM_DATE ";
        $query .= "         FROM ";
        $query .= "             STAFF_PRINCIPAL_HIST_DAT I1 ";
        $query .= "         WHERE ";
        $query .= "             I1.SCHOOL_KIND = '{$fields["SCHOOL_KIND"]}' ";
        $query .= "     ) ";
        $db->query($query);

        $data["SCHOOL_KIND"][TEXT]  = $fields["SCHOOL_KIND"];
        $data["FROM_DATE"][DATE]    = $fields["FROM_DATE"];
        $data["STAFFCD"][TEXT]      = $fields["STAFFCD"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "STAFF_PRINCIPAL_HIST_DAT");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    function &getUpdateQuery($fields)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //最新削除
        $query  = " DELETE FROM STAFF_PRINCIPAL_HIST_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOL_KIND = '{$fields["SCHOOL_KIND"]}' ";
        $query .= "     AND T1.FROM_DATE IN ( ";
        $query .= "         SELECT ";
        $query .= "             MAX(I1.FROM_DATE) AS FROM_DATE ";
        $query .= "         FROM ";
        $query .= "             STAFF_PRINCIPAL_HIST_DAT I1 ";
        $query .= "         WHERE ";
        $query .= "             I1.SCHOOL_KIND = '{$fields["SCHOOL_KIND"]}' ";
        $query .= "     ) ";
        $db->query($query);

        //最新更新（削除してるので、最新のひとつ前）
        list($year, $month, $day) = preg_split("/\//", $fields["FROM_DATE"]);
        $updToDate = date("Y-m-d", mktime( 0, 0, 0, $month, $day - 1, $year ));

        $query  = " UPDATE STAFF_PRINCIPAL_HIST_DAT T1 ";
        $query .= " SET T1.TO_DATE = '{$updToDate}' ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOL_KIND = '{$fields["SCHOOL_KIND"]}' ";
        $query .= "     AND T1.FROM_DATE IN ( ";
        $query .= "         SELECT ";
        $query .= "             MAX(I1.FROM_DATE) AS FROM_DATE ";
        $query .= "         FROM ";
        $query .= "             STAFF_PRINCIPAL_HIST_DAT I1 ";
        $query .= "         WHERE ";
        $query .= "             I1.SCHOOL_KIND = '{$fields["SCHOOL_KIND"]}' ";
        $query .= "     ) ";
        $db->query($query);

        //最新登録
        $data["SCHOOL_KIND"][TEXT]  = $fields["SCHOOL_KIND"];
        $data["FROM_DATE"][DATE]    = $fields["FROM_DATE"];
        $data["STAFFCD"][TEXT]      = $fields["STAFFCD"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "STAFF_PRINCIPAL_HIST_DAT");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //最新削除
        $fromDate = str_replace("/", "-", $fields["FROM_DATE"]);
        $query  = " DELETE FROM STAFF_PRINCIPAL_HIST_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOL_KIND = '{$fields["SCHOOL_KIND"]}' ";
        $query .= "     AND T1.FROM_DATE  = '{$fromDate}' ";
        $db->query($query);

        //最新更新（削除してるので、最新のひとつ前）
        $query  = " UPDATE STAFF_PRINCIPAL_HIST_DAT T1 ";
        $query .= " SET T1.TO_DATE = NULL ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOL_KIND = '{$fields["SCHOOL_KIND"]}' ";
        $query .= "     AND T1.FROM_DATE IN ( ";
        $query .= "         SELECT ";
        $query .= "             MAX(I1.FROM_DATE) AS FROM_DATE ";
        $query .= "         FROM ";
        $query .= "             STAFF_PRINCIPAL_HIST_DAT I1 ";
        $query .= "         WHERE ";
        $query .= "             I1.SCHOOL_KIND = '{$fields["SCHOOL_KIND"]}' ";
        $query .= "     ) ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
