<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knje311Query.inc 56587 2017-10-22 12:54:51Z maeshiro $

class knje311Query extends Query {

    function &getYear() {
        $query  = " select distinct YEAR";
        $query .= "  from AFT_GRAD_COURSE_DAT";
        $query .= "  where STAT_KIND = '1'";
        $query .= " order by YEAR desc";

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

    function &getNameMst($cd) {
        $query = "  select NAMECD2, NAMECD2 || '　' || NAME1 as NM_LABEL";
        $query .= "  from NAME_MST";
        $query .= "  where NAMECD1 = '$cd'";
        $query .= " order by NAMECD2";

        $db = Query::dbCheckOut();
        $result = $db->query($query);
        $rtn = array();
        $rtn[0] = array("label" => "", "value" => "");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $rtn[] = array("label" => htmlspecialchars($row["NM_LABEL"]),
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        return $rtn;
    }

    // 進路情報データよりデータを取得 ---> 画面表示およびＣＳＶ出力
    function sqlList($year, $senkou_kind, $school_sort) {
        $query = "";
        // 校内選考情報
        $query .= " WITH AFT_GRAD_COURSE AS (";
        $query .= "     select SEQ, SCHREGNO, STAT_NAME, BUNAME, SCHOOL_SORT, JUKEN_HOWTO, AVG, SEISEKI, SENKOU_KAI, SENKOU_FIN,";
        if ($senkou_kind == 0) $query .= "            STAT_CD   AS SHINRO_NO";
        if ($senkou_kind == 1) $query .= "            SENKOU_NO AS SHINRO_NO";
        $query .= "       from AFT_GRAD_COURSE_DAT";
        $query .= "      where YEAR = '$year'";
        $query .= "        and STAT_KIND = '1'"; // STAT_KIND '0':選考以外, '1':選考
        $query .= "        and SENKOU_KIND = '$senkou_kind'"; // SENKOU_KIND '0':学校, '1':会社
        $query .= "        and SCHOOL_SORT = '$school_sort'";
        $query .= "     )";
        // SCHNOにて使用---生徒のMAX年度・学期 ★在
        $query .= " ,UNGRD_MAX_SCHNO AS (";
        $query .= "     select SCHREGNO,";
        $query .= "            MAX(YEAR || SEMESTER) AS MAX_YEAR_SEMESTER";
        $query .= "       from SCHREG_REGD_DAT W1";
        $query .= "      where EXISTS(select 'X'";
        $query .= "                     from AFT_GRAD_COURSE X1";
        $query .= "                    where X1.SCHREGNO = W1.SCHREGNO)";
        $query .= "        and W1.YEAR = '$year'";
        $query .= "      group by SCHREGNO";
        $query .= "     )";
        // SCHNOにて使用---生徒のMAX年度・学期 ★卒
        $query .= " ,GRD_MAX_SCHNO AS (";
        $query .= "     select SCHREGNO,";
        $query .= "            MAX(YEAR || SEMESTER) AS MAX_YEAR_SEMESTER";
        $query .= "       from GRD_REGD_DAT W1";
        $query .= "      where EXISTS(select 'X'";
        $query .= "                     from AFT_GRAD_COURSE X1";
        $query .= "                    where X1.SCHREGNO = W1.SCHREGNO)";
        $query .= "      group by SCHREGNO";
        $query .= "     )";
        // 学籍情報 ★在・卒
        $query .= " ,SCHNO AS (";
        $query .= "     select W1.SCHREGNO, W2.HR_NAME, W1.GRADE, W1.HR_CLASS, W1.ATTENDNO, W3.NAME_SHOW";
        $query .= "       from SCHREG_REGD_DAT W1";
        $query .= "            left join SCHREG_REGD_HDAT W2 on W2.YEAR = W1.YEAR";
        $query .= "                                         and W2.SEMESTER = W1.SEMESTER";
        $query .= "                                         and W2.GRADE = W1.GRADE";
        $query .= "                                         and W2.HR_CLASS = W1.HR_CLASS,";
        $query .= "            SCHREG_BASE_MST W3";
        $query .= "      where W3.SCHREGNO = W1.SCHREGNO";
        $query .= "        and EXISTS(select 'X'";
        $query .= "                     from UNGRD_MAX_SCHNO X1";
        $query .= "                    where X1.SCHREGNO = W1.SCHREGNO";
        $query .= "                      and X1.MAX_YEAR_SEMESTER = W1.YEAR || W1.SEMESTER)";
        $query .= "        and not EXISTS(select 'X'";
        $query .= "                     from GRD_MAX_SCHNO X1";
        $query .= "                    where X1.SCHREGNO = W1.SCHREGNO)";
        $query .= "     union";
        $query .= "     select W1.SCHREGNO, W2.HR_NAME, W1.GRADE, W1.HR_CLASS, W1.ATTENDNO, W3.NAME_SHOW";
        $query .= "       from GRD_REGD_DAT W1";
        $query .= "            left join GRD_REGD_HDAT W2 on W2.YEAR = W1.YEAR";
        $query .= "                                         and W2.SEMESTER = W1.SEMESTER";
        $query .= "                                         and W2.GRADE = W1.GRADE";
        $query .= "                                         and W2.HR_CLASS = W1.HR_CLASS,";
        $query .= "            GRD_BASE_MST W3";
        $query .= "      where W3.SCHREGNO = W1.SCHREGNO";
        $query .= "        and EXISTS(select 'X'";
        $query .= "                     from GRD_MAX_SCHNO X1";
        $query .= "                    where X1.SCHREGNO = W1.SCHREGNO";
        $query .= "                      and X1.MAX_YEAR_SEMESTER = W1.YEAR || W1.SEMESTER)";
        $query .= "     )";
        // 欠席
        $query .= " ,ATTEND_SUM_SCHNO AS (";
        $query .= "     select W1.SCHREGNO,";
        $query .= "            SUM(CASE WHEN S1.SEM_OFFDAYS = '1' ";
        $query .= "                     THEN VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0) + VALUE(OFFDAYS,0) ";
        $query .= "                     ELSE VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0) ";
        $query .= "                     END) AS ATTEND_SUM ";
        $query .= "       from SCHREG_ATTENDREC_DAT W1";
        $query .= "            LEFT JOIN SCHOOL_MST S1 ON S1.YEAR = W1.YEAR ";
        $query .= "      where EXISTS(select 'X'";
        $query .= "                     from AFT_GRAD_COURSE X1";
        $query .= "                    where X1.SCHREGNO = W1.SCHREGNO)";
        $query .= "      group by W1.SCHREGNO";
        $query .= "     )";

        // メイン
        $query .= " select T1.HR_NAME, T1.ATTENDNO, T1.NAME_SHOW,"; // 5,6,7
        $query .= "        A1.ATTEND_SUM,"; // 8
        $query .= "        T2.SEQ,";
        $query .= "        T2.SCHREGNO,";
        $query .= "        T2.SHINRO_NO,"; // 3
        $query .= "        T2.STAT_NAME,"; // 4
        $query .= "        T2.AVG,"; // 9
        $query .= "        T2.SEISEKI,"; // 10
        $query .= "        T2.SENKOU_FIN,"; // 12
        $query .= "        E001.NAME1 AS NM_SCHOOL_SORT,";
        $query .= "        E002.NAME1 AS NM_JUKEN_HOWTO,"; // 2
        $query .= "        E003.NAME1 AS NM_SENKOU_KAI,"; // 11
        $query .= "        E004.NAME1 AS NM_SENKOU_FIN";
        $query .= "   from SCHNO T1";
        $query .= "        left join ATTEND_SUM_SCHNO A1 on A1.SCHREGNO = T1.SCHREGNO,";
        $query .= "        AFT_GRAD_COURSE T2";
        $query .= "        left join NAME_MST E001 on E001.NAMECD1 = 'E001' and E001.NAMECD2 = T2.SCHOOL_SORT";
        $query .= "        left join NAME_MST E002 on E002.NAMECD1 = 'E002' and E002.NAMECD2 = T2.JUKEN_HOWTO";
        $query .= "        left join NAME_MST E003 on E003.NAMECD1 = 'E003' and E003.NAMECD2 = T2.SENKOU_KAI";
        $query .= "        left join NAME_MST E004 on E004.NAMECD1 = 'E004' and E004.NAMECD2 = T2.SENKOU_FIN";
        $query .= "  where T1.SCHREGNO = T2.SCHREGNO";
        $query .= "  order by T2.SENKOU_KAI, T2.JUKEN_HOWTO, T2.SHINRO_NO, T2.BUNAME, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO";

        return $query;
    }

    // --- UPDATE 
    function &getUpdateQuery($year, $data_seq, $data_senkou_fin) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $opt_seq     = explode(",", $data_seq);
        $opt_senkou_fin = explode(",", $data_senkou_fin);

        for ($i = 0; $i < get_count($opt_seq); $i++) {
            $data["SENKOU_FIN"][TEXT]   = ($opt_senkou_fin[$i] != "") ? $opt_senkou_fin[$i] : NULL;
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";

            $where  = " where YEAR   = '$year'";
            $where .= "   and SEQ    = " .$opt_seq[$i] ;

            $query = Query::updateSQL($data, "AFT_GRAD_COURSE_DAT", $where);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
?>
