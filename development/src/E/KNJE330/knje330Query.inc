<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knje330Query.inc 56587 2017-10-22 12:54:51Z maeshiro $

class knje330Query extends Query {

    // 学籍基礎マスタより学籍番号と名前を取得
    function sqlSchregnoName($schregno, $mode) {
        $query  ="select SCHREGNO, NAME_SHOW as NAME";
        if ($mode == "ungrd")   $query .=" from SCHREG_BASE_MST";// 在籍
        if ($mode == "grd")     $query .=" from GRD_BASE_MST";// 卒業
        $query .=" where SCHREGNO = '$schregno'";

        return $query;
    }

    // 進路情報データよりデータを取得
    function sqlList($schregno) {
        $query  = "select distinct";
        $query .= "  T1.YEAR,";
        $query .= "  T1.SEQ,";
        $query .= "  T1.SCHREGNO,";
        $query .= "  T1.STAT_DATE1,";
        $query .= "  T1.STAT_CD,";
        $query .= "  value(T1.STAT_CD,'') || '　' || T1.STAT_NAME as STAT_NAME,";
        $query .= "  T1.BUNAME,";
        $query .= "  E005.NM_DECISION,";
        $query .= "  E002.NM_HOWTOEXAM,";
        $query .= "  E006.NM_PLANSTAT";
        $query .= " from (";
        $query .= "  select * from AFT_GRAD_COURSE_DAT";
        $query .= "  where SCHREGNO = '$schregno'";
        $query .= "    and SENKOU_KIND = '0'"; // SENKOU_KIND '0':学校, '1':会社
        $query .= "    and (STAT_KIND = '0'";  // STAT_KIND '0':選考以外, '1':選考
        $query .= "    or (STAT_KIND = '1' and SENKOU_FIN = '1'))"; // SENKOU_FIN (E004) '1':通過, '2':不可, '9':その他
        $query .= " ) T1";
        $query .= " left join (";
        $query .= "  select NAMECD2, NAMECD2 || '　' || NAME1 as NM_DECISION";
        $query .= "  from NAME_MST";
        $query .= "  where NAMECD1 = 'E005'";
        $query .= " ) E005 on T1.DECISION = E005.NAMECD2";
        $query .= " left join (";
        $query .= "  select NAMECD2, NAMECD2 || '　' || NAME1 as NM_HOWTOEXAM";
        $query .= "  from NAME_MST";
        $query .= "  where NAMECD1 = 'E002'";
        $query .= " ) E002 on T1.HOWTOEXAM = E002.NAMECD2";
        $query .= " left join (";
        $query .= "  select NAMECD2, NAMECD2 || '　' || NAME1 as NM_PLANSTAT";
        $query .= "  from NAME_MST";
        $query .= "  where NAMECD1 = 'E006'";
        $query .= " ) E006 on T1.PLANSTAT = E006.NAMECD2";
        $query .= "  order by T1.YEAR, T1.STAT_DATE1, T1.STAT_CD";

        return $query;
    }

    // １レコード取得(学校マスタ)
    function &getCollegeMst($school_cd) {
        $query  = "select *";
        $query .= " from COLLEGE_MST";
        $query .= " where SCHOOL_CD = '$school_cd'";

        $db = Query::dbCheckOut();
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    function &getNameMst($year, $cd) {
        $query = "  select M.NAMECD2, M.NAMECD2 || '　' || M.NAME1 as NM_LABEL";
        $query .= "  from NAME_YDAT Y, NAME_MST M";
        $query .= "  where Y.YEAR = '$year'";
        $query .= "    and Y.NAMECD1 = '$cd'";
        $query .= "    and Y.NAMECD1 = M.NAMECD1";
        $query .= "    and Y.NAMECD2 = M.NAMECD2";

        $db = Query::dbCheckOut();
        $result = $db->query($query);

        $rtn = array();
        $rtn[0] = array("label" => "", "value" => "");

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($cd == "E001" && "04" < $row["NAMECD2"]) break;
            $rtn[] = array("label" => htmlspecialchars($row["NM_LABEL"]),
                           "value" => $row["NAMECD2"]);
        }

        $result->free();
        Query::dbCheckIn($db);

        return $rtn;
    }

    function &getOneRow($year, $seq, $schregno) {
        $query  = "select *";
        $query .= " from AFT_GRAD_COURSE_DAT";
        $query .= " where YEAR = '$year'";
        $query .= "   and SEQ = $seq";
        $query .= "   and SCHREGNO = '$schregno'";

        $db = Query::dbCheckOut();
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    function &getMaxSeq($year) {

        $query  = "select coalesce(max(SEQ),0) as MAX_SEQ";
        $query .= " from AFT_GRAD_COURSE_DAT";
        $query .= " where YEAR = '$year'";

        $db = Query::dbCheckOut();
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row["MAX_SEQ"];
    }

    // INSERT
    function &getInsertQuery($field, $year, $seq, $schregno) {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]             = $year;
        $data["SEQ"][NUMBER]            = $seq;
        $data["SCHREGNO"][TEXT]         = $schregno;
        $data["STAT_KIND"][TEXT]        = "0";
        $data["SENKOU_KIND"][TEXT]      = "0";
        $data["STAT_CD"][TEXT]          = $field["STAT_CD"];
        $data["STAT_NAME"][TEXT]        = $field["SCHOOL_NAME"];
        $data["BUNAME"][TEXT]           = $field["BUNAME"];
        $data["SCHOOL_SORT"][TEXT]      = $field["SCHOOL_SORT"];
        $data["TELNO"][TEXT]            = $field["TELNO"];
        $data["HOWTOEXAM"][TEXT]        = $field["HOWTOEXAM"];
        $data["HOWTOEXAM_REMARK"][TEXT] = $field["HOWTOEXAM_REMARK"];
        $data["HAND_DATE"][DATE]        = $field["HAND_DATE"];
        $data["DECISION"][TEXT]         = $field["DECISION"];
        $data["PLANSTAT"][TEXT]         = $field["PLANSTAT"];
        $data["STAT_DATE1"][DATE]       = $field["STAT_DATE1"];
        if ($field["HOUR_S"] == "" || $field["MINUTE_S"] == "") {
            $data["STAT_STIME"][TEXT]   = NULL;
        } else {
            $data["STAT_STIME"][TEXT]   = sprintf("%02d:%02d:00", $field["HOUR_S"], $field["MINUTE_S"]);
        }
        if ($field["HOUR_E"] == "" || $field["MINUTE_E"] == "") {
            $data["STAT_ETIME"][TEXT]   = NULL;
        } else {
            $data["STAT_ETIME"][TEXT]   = sprintf("%02d:%02d:00", $field["HOUR_E"], $field["MINUTE_E"]);
        }
        $data["AREA_NAME"][TEXT]        = $field["AREA_NAME"];
        $data["STAT_DATE2"][DATE]       = $field["STAT_DATE2"];
        $data["CONTENTEXAM"][TEXT]      = $field["CONTENTEXAM"];
        $data["REASONEXAM"][TEXT]       = $field["REASONEXAM"];
        $data["THINKEXAM"][TEXT]        = $field["THINKEXAM"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "AFT_GRAD_COURSE_DAT");
        $db->query($query);

        Query::dbCheckIn($db);

        return;
    }

    // UPDATE
    function &getUpdateQuery($field, $year, $seq, $schregno) {
        $db = Query::dbCheckOut();

        if ($field["STAT_KIND"] == '0') {
            $data["YEAR"][TEXT]             = $year;
            $data["SEQ"][NUMBER]            = $seq;
            $data["SCHREGNO"][TEXT]         = $schregno;
            $data["STAT_CD"][TEXT]          = $field["STAT_CD"];
            $data["STAT_NAME"][TEXT]        = $field["SCHOOL_NAME"];
            $data["BUNAME"][TEXT]           = $field["BUNAME"];
            $data["SCHOOL_SORT"][TEXT]      = $field["SCHOOL_SORT"];
            $data["TELNO"][TEXT]            = $field["TELNO"];
            $data["HOWTOEXAM"][TEXT]        = $field["HOWTOEXAM"];
            $data["HOWTOEXAM_REMARK"][TEXT] = $field["HOWTOEXAM_REMARK"];
            $data["HAND_DATE"][DATE]        = $field["HAND_DATE"];
            $data["DECISION"][TEXT]         = $field["DECISION"];
            $data["PLANSTAT"][TEXT]         = $field["PLANSTAT"];
            $data["STAT_DATE1"][DATE]       = $field["STAT_DATE1"];
            if ($field["HOUR_S"] == "" || $field["MINUTE_S"] == "") {
                $data["STAT_STIME"][TEXT]   = NULL;
            } else {
                $data["STAT_STIME"][TEXT]   = sprintf("%02d:%02d:00", $field["HOUR_S"], $field["MINUTE_S"]);
            }
            if ($field["HOUR_E"] == "" || $field["MINUTE_E"] == "") {
                $data["STAT_ETIME"][TEXT]   = NULL;
            } else {
                $data["STAT_ETIME"][TEXT]   = sprintf("%02d:%02d:00", $field["HOUR_E"], $field["MINUTE_E"]);
            }
            $data["AREA_NAME"][TEXT]        = $field["AREA_NAME"];
            $data["STAT_DATE2"][DATE]       = $field["STAT_DATE2"];
            $data["CONTENTEXAM"][TEXT]      = $field["CONTENTEXAM"];
            $data["REASONEXAM"][TEXT]       = $field["REASONEXAM"];
            $data["THINKEXAM"][TEXT]        = $field["THINKEXAM"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";
        } else {
            $data["DECISION"][TEXT]         = $field["DECISION"];
            $data["PLANSTAT"][TEXT]         = $field["PLANSTAT"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";
        }
        $where  = " where YEAR = '$year'";
        $where .= "   and SEQ = $seq";
        $where .= "   and SCHREGNO = '$schregno'";

        $query = Query::updateSQL($data, "AFT_GRAD_COURSE_DAT", $where);
        $db->query($query);

        Query::dbCheckIn($db);

        return ;
    }

    // DELETE
    function &getDeleteQuery($year, $seq, $schregno) {
        $db = Query::dbCheckOut();

        $query  = "DELETE ";
        $query .= " from AFT_GRAD_COURSE_DAT";
        $query .= " where YEAR = '$year'";
        $query .= "   and SEQ = $seq";
        $query .= "   and SCHREGNO = '$schregno'";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }
}
?>
