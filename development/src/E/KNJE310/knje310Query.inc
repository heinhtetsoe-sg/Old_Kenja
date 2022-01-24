<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knje310Query.inc 56587 2017-10-22 12:54:51Z maeshiro $

class knje310Query extends Query {

    // 学籍基礎マスタより学籍番号と名前を取得
    function sqlSchregnoName($schregno, $mode) {
        $query  ="select SCHREGNO, NAME_SHOW as NAME";
        if ($mode == "ungrd")   $query .=" from SCHREG_BASE_MST";// 在籍
        if ($mode == "grd")     $query .=" from GRD_BASE_MST";// 卒業
        $query .=" where SCHREGNO = '$schregno'";

        return $query;
    }

    // 進路情報データよりデータを取得--->１生徒のデータ全て
    function sqlList($schregno) {
        $query  = "select distinct";
        $query .= "  T1.YEAR,";
        $query .= "  T1.SEQ,";
        $query .= "  T1.SCHREGNO,";
        $query .= "  T1.TOROKU_DATE,";
        $query .= "  T1.STAT_CD,";
        $query .= "  case when T1.SENKOU_KIND='0'"; // SENKOU_KIND '0':学校, '1':会社
        $query .= "       then value(T1.STAT_CD,'') || '　' || T1.STAT_NAME || '/' || value(T1.BUNAME,'')";
        $query .= "       when T1.SENKOU_KIND='1'";
        $query .= "       then value(T1.STAT_CD,'') || '　' || T1.STAT_NAME";
        $query .= "  end as STAT_NAME,";
        $query .= "  E001.NM_SCHOOL_SORT,";
        $query .= "  E003.NM_SENKOU_KAI,";
        $query .= "  E004.NM_SENKOU_FIN,";
        $query .= "  E005.NM_DECISION,";
        $query .= "  E006.NM_PLANSTAT";
        $query .= " from (";
        $query .= "  select * from AFT_GRAD_COURSE_DAT";
        $query .= "   where SCHREGNO = '$schregno'";
        $query .= "     and STAT_KIND = '1'";   // STAT_KIND '0':選考以外, '1':選考
        $query .= " ) T1";
        $query .= " left join (";
        $query .= "  select NAMECD2, NAMECD2 || '　' || NAME1 as NM_SCHOOL_SORT";
        $query .= "    from NAME_MST";
        $query .= "   where NAMECD1 = 'E001'";
        $query .= " ) E001 on T1.SCHOOL_SORT = E001.NAMECD2";
        $query .= " left join (";
        $query .= "  select NAMECD2, NAMECD2 || '　' || NAME1 as NM_SENKOU_KAI";
        $query .= "    from NAME_MST";
        $query .= "   where NAMECD1 = 'E003'";
        $query .= " ) E003 on T1.SENKOU_KAI = E003.NAMECD2";
        $query .= " left join (";
        $query .= "  select NAMECD2, NAMECD2 || '　' || NAME1 as NM_SENKOU_FIN";
        $query .= "    from NAME_MST";
        $query .= "   where NAMECD1 = 'E004'";
        $query .= " ) E004 on T1.SENKOU_FIN = E004.NAMECD2";
        $query .= " left join (";
        $query .= "  select NAMECD2, NAMECD2 || '　' || NAME1 as NM_DECISION";
        $query .= "    from NAME_MST";
        $query .= "   where NAMECD1 = 'E005'";
        $query .= " ) E005 on T1.DECISION = E005.NAMECD2";
        $query .= " left join (";
        $query .= "  select NAMECD2, NAMECD2 || '　' || NAME1 as NM_PLANSTAT";
        $query .= "    from NAME_MST";
        $query .= "   where NAMECD1 = 'E006'";
        $query .= " ) E006 on T1.PLANSTAT = E006.NAMECD2";
        $query .= " order by T1.TOROKU_DATE";

        return $query;
    }

    // １レコード取得(学校・会社マスタ)
    function &getCollegeOrCompanyMst($stat_cd, $school_sort) {
        if ("04" < $school_sort) {
            $query  = "select COMPANY_CD as STAT_CD, COMPANY_NAME as STAT_NAME";
            $query .= " from COMPANY_MST";
            $query .= " where COMPANY_CD = '$stat_cd'";
        } else {
            $query  = "select SCHOOL_CD as STAT_CD, SCHOOL_NAME as STAT_NAME, BUNAME, SCHOOL_SORT";
            $query .= " from COLLEGE_MST";
            $query .= " where SCHOOL_CD = '$stat_cd'";
        }
        $db = Query::dbCheckOut();
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    function &getNameMst($cd, $showflg = "on") {
        $query = "  select NAMECD2, NAMECD2 || '　' || NAME1 as NM_LABEL";
        $query .= "  from NAME_MST";
        $query .= "  where NAMECD1 = '$cd'";
        $query .= " order by NAMECD2";

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

    function &getNameMst2($cd, $showflg = "on") {
        $query = "  select NAMECD2, NAMECD2 || '　' || NAME1 as NM_LABEL";
        $query .= "  from NAME_MST";
        $query .= "  where NAMECD1 = '$cd'";
        $query .= " order by NAMECD2";

        $db = Query::dbCheckOut();
        $result = $db->query($query);

        $rtn = array();

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $rtn[$row["NAMECD2"]] = htmlspecialchars($row["NM_LABEL"]);
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

    function &getAttend($schregno) {
        $query  = "SELECT ";
        $query .= "    SUM(CASE WHEN S1.SEM_OFFDAYS = '1' ";
        $query .= "         THEN VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0) + VALUE(OFFDAYS,0) ";
        $query .= "         ELSE VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0) ";
        $query .= "         END) AS ATTEND_SUM ";
        $query .= "FROM ";
        $query .= "    SCHREG_ATTENDREC_DAT T1 ";
        $query .= "    LEFT JOIN SCHOOL_MST S1 ON S1.YEAR = T1.YEAR ";
        $query .= "WHERE ";
        $query .= "    T1.SCHREGNO='$schregno' ";

        $db = Query::dbCheckOut();
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $attend = 0;
        if (isset($row)) {
            if ($row["ATTEND_SUM"] == 0) {
                $attend = 10;
            } else if ($row["ATTEND_SUM"] <= 3) {
                $attend = 8;
            } else if ($row["ATTEND_SUM"] <= 6) {
                $attend = 6;
            } else if ($row["ATTEND_SUM"] <= 9) {
                $attend = 4;
            } else if ($row["ATTEND_SUM"] <= 12) {
                $attend = 2;
            } else if ($row["ATTEND_SUM"] <= 15) {
                $attend = 1;
            } else if ($row["ATTEND_SUM"] >= 16) {
                $attend = 0;
            }
        }
        Query::dbCheckIn($db);
        return $attend;
    }

    function &getAvg($schregno) {
        $query  = "select AVG(FLOAT(VALUATION)) AS AVG_VAL";
        $query .= " from SCHREG_STUDYREC_DAT";
        $query .= " where SCHREGNO = '$schregno'";

        $db = Query::dbCheckOut();
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $avg = 0;
        if (isset($row)) {
            $avg = ($row["AVG_VAL"] > 0) ? $row["AVG_VAL"] : 0;
        }
        Query::dbCheckIn($db);
        return $avg;
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

    // INSERT ---> ★ 学校のみデータあり 会社はnull
    function &getInsertQuery($field, $year, $seq, $schregno) {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]             = $year;
        $data["SEQ"][NUMBER]            = $seq;
        $data["SCHREGNO"][TEXT]         = $schregno;
        $data["STAT_KIND"][TEXT]        = "1";
        $data["SENKOU_KIND"][TEXT]      = ("04" < $field["SCHOOL_SORT"]) ? "1" : "0";
        $data["STAT_CD"][TEXT]          = $field["STAT_CD"];
        $data["STAT_NAME"][TEXT]        = $field["STAT_NAME"];
        $data["BUNAME"][TEXT]           = $field["BUNAME"];         // ★ (学部名)
        $data["SCHOOL_SORT"][TEXT]      = $field["SCHOOL_SORT"];
        $data["DECISION"][TEXT]         = $field["DECISION"];
        $data["PLANSTAT"][TEXT]         = $field["PLANSTAT"];
        $data["SENKOU_NO"][NUMBER]      = $field["SENKOU_NO"];
        $data["TOROKU_DATE"][DATE]      = $field["TOROKU_DATE"];
        $data["JUKEN_HOWTO"][TEXT]      = $field["JUKEN_HOWTO"];    // ★ (受験方法)
        $data["RECOMMEND"][TEXT]        = $field["RECOMMEND"];      // ★ (推薦基準)
        $data["ATTEND"][NUMBER]         = $field["ATTEND"];
        $data["AVG"][NUMBER]            = $field["AVG"];
        $data["TEST"][NUMBER]           = $field["TEST"];           // ★ (統一テスト)
        $data["SEISEKI"][NUMBER]        = $field["SEISEKI"];
        $data["SENKOU_KAI"][TEXT]       = $field["SENKOU_KAI"];
        $data["SENKOU_FIN"][TEXT]       = $field["SENKOU_FIN"];
        $data["REMARK"][TEXT]           = $field["REMARK"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "AFT_GRAD_COURSE_DAT");
        $db->query($query);

        Query::dbCheckIn($db);

        return;
    }

    // UPDATE ---> ★ 学校のみデータあり 会社はnull
    function &getUpdateQuery($field, $year, $seq, $schregno) {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]             = $year;
        $data["SEQ"][NUMBER]            = $seq;
        $data["SCHREGNO"][TEXT]         = $schregno;
        $data["STAT_KIND"][TEXT]        = "1";
        $data["SENKOU_KIND"][TEXT]      = ("04" < $field["SCHOOL_SORT"]) ? "1" : "0";
        $data["STAT_CD"][TEXT]          = $field["STAT_CD"];
        $data["STAT_NAME"][TEXT]        = $field["STAT_NAME"];
        $data["BUNAME"][TEXT]           = $field["BUNAME"];         // ★ (学部名)
        $data["SCHOOL_SORT"][TEXT]      = $field["SCHOOL_SORT"];
        $data["DECISION"][TEXT]         = $field["DECISION"];
        $data["PLANSTAT"][TEXT]         = $field["PLANSTAT"];
        $data["SENKOU_NO"][NUMBER]      = $field["SENKOU_NO"];
        $data["TOROKU_DATE"][DATE]      = $field["TOROKU_DATE"];
        $data["JUKEN_HOWTO"][TEXT]      = $field["JUKEN_HOWTO"];    // ★ (受験方法)
        $data["RECOMMEND"][TEXT]        = $field["RECOMMEND"];      // ★ (推薦基準)
        $data["ATTEND"][NUMBER]         = $field["ATTEND"];
        $data["AVG"][NUMBER]            = $field["AVG"];
        $data["TEST"][NUMBER]           = $field["TEST"];           // ★ (統一テスト)
        $data["SEISEKI"][NUMBER]        = $field["SEISEKI"];
        $data["SENKOU_KAI"][TEXT]       = $field["SENKOU_KAI"];
        $data["SENKOU_FIN"][TEXT]       = $field["SENKOU_FIN"];
        $data["REMARK"][TEXT]           = $field["REMARK"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

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
