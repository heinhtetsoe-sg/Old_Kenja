<?php

require_once('for_php7.php');


class knje353Query extends Query {

    // 年度
    function &getYear() {
        $query  = "select DISTINCT YEAR ";
        $query .= "  from AFT_GRAD_COURSE_DAT ";
        $query .= " where STAT_KIND = '1'";
        $query .= " order by YEAR desc ";

        $db = Query::dbCheckOut();
        $result = $db->query($query);

        $rtn = array();

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $rtn[] = array("label" => htmlspecialchars($row["YEAR"]),
                           "value" => $row["YEAR"]);
        }

        $result->free();
        Query::dbCheckIn($db);

        return $rtn;
    }

    function &getNameMst($year, $cd, $showflg) {
        $query = "  select M.NAMECD2, M.NAMECD2 || '　' || M.NAME1 as NM_LABEL";
        $query .= "  from NAME_YDAT Y, NAME_MST M";
        $query .= "  where Y.YEAR = '$year'";
        $query .= "    and Y.NAMECD1 = '$cd'";
        $query .= "    and Y.NAMECD1 = M.NAMECD1";
        $query .= "    and Y.NAMECD2 = M.NAMECD2";
        $query .= "  order by M.NAMECD2 ";

        $db = Query::dbCheckOut();
        $result = $db->query($query);

        $rtn = array();
        if ($showflg == "on") $rtn[0] = array("label" => "", "value" => "");

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $rtn[] = array("label" => htmlspecialchars($row["NM_LABEL"]),
                           "value" => $row["NAMECD2"]);
        }

        $result->free();
        Query::dbCheckIn($db);

        return $rtn;
    }
}
?>
