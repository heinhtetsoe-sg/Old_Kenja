<?php

require_once('for_php7.php');


class knjj060Query extends Query {
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
        $query .= "     NAME1 ";

        return $query;
    }

    function getNameMst()
    {
        $query  = "";
        $query .= "SELECT ";
        $query .= "    NAME1 "; // 学校区分
        $query .= "FROM ";
        $query .= "    NAME_MST ";
        $query .= "WHERE ";
        $query .= "    NAMECD1='Z010' AND ";
        $query .= "    NAMECD2='00' ";

        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $rtnRow;
    }

    //クラブ取得
    function getClubNameList($model)
    {
        $query  = "SELECT T1.CLUBCD AS VALUE,T2.CLUBNAME AS LABEL ";
        $query .= "FROM   CLUB_YDAT T1,CLUB_MST T2 ";
        //参照・更新可（制限付き）---2005.06.02
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= "   INNER JOIN ( SELECT CLUBCD FROM CLUB_ADVISER_DAT ";
            $query .= "                WHERE  YEAR='" .$model->control["年度"] ."' AND ADVISER='" .STAFFCD ."' ";
            if ($model->Properties["useClubMultiSchoolKind"] == "1"){
                $query .= " AND SCHOOLCD = '".SCHOOLCD."' ";
                $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
                $query .= " AND SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND SCHOOLCD = '".SCHOOLCD."' ";
                $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= "              ) T3 ON T3.CLUBCD = T2.CLUBCD ";
        }
        $query .= "WHERE  T1.YEAR = '" .$model->control["年度"] ."' AND ";
        $query .= "       T1.CLUBCD = T2.CLUBCD ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1"){
            $query .= " AND T1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= " AND T2.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T1.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
            $query .= " AND T2.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND T2.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= " AND T2.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "ORDER BY T1.CLUBCD ";

        return $query;
    }

    //ＣＳＶ-A作成
    function selectCsvQueryA($model) {

        $sort = explode(',', $model->selectsort);
        $setFromDate = str_replace("/", "-", $model->field["FROM_DATE"]);
        $setToDate = str_replace("/", "-", $model->field["TO_DATE"]);

        $query .= " WITH SCHINFO AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T3.HR_NAMEABBV, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T2.NAME_KANA, ";
        $query .= "     T2.BIRTHDAY, ";
        $query .= "     T2.SEX ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1"){
            //全生徒が対象
        } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO, ";
        $query .= "     SCHREG_REGD_HDAT T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T3.YEAR AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE = T3.GRADE AND ";
        $query .= "     T1.HR_CLASS = T3.HR_CLASS ";
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.HR_NAMEABBV, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T2.NAME_KANA, ";
        $query .= "     T2.BIRTHDAY, ";
        $query .= "     T1.CLUBCD, ";
        $query .= "     C1.CLUBNAME, ";
        $query .= "     T1.SDATE, ";
        $query .= "     T1.EDATE, ";
        $query .= "     T1.EXECUTIVECD, ";
        $query .= "     N1.NAME1 AS EXECUTIVENAME, ";
        $query .= "     T1.REMARK, ";
        $query .= "     G1.GUARD_NAME, ";
        $query .= "     VALUE(G1.GUARD_ADDR1,'') || VALUE(G1.GUARD_ADDR2,'') AS GUARD_ADDR, ";
        $query .= "     G1.GUARD_TELNO, ";
        $query .= "     H1.DETAIL_DATE, ";
        $query .= "     H1.DETAIL_REMARK, ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.SEX, ";
        $query .= "     case when T1.EXECUTIVECD is null then -1 else smallint(T1.EXECUTIVECD) end DESC ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HIST_DAT T1 ";
        $query .= "     LEFT JOIN CLUB_MST C1 ON T1.CLUBCD = C1.CLUBCD ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND C1.SCHOOLCD    = T1.SCHOOLCD ";
            $query .= " AND C1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND C1.SCHOOLCD    = T1.SCHOOLCD ";
            $query .= " AND C1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND C1.SCHOOLCD    = T1.SCHOOLCD ";
            $query .= " AND C1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'J001' AND T1.EXECUTIVECD = N1.NAMECD2 ";
        $query .= "     LEFT JOIN GUARDIAN_DAT G1 ON T1.SCHREGNO = G1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_CLUB_HDETAIL_DAT H1 ON T1.SCHREGNO = H1.SCHREGNO AND T1.CLUBCD = H1.CLUBCD AND ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " H1.SCHOOLCD    = T1.SCHOOLCD AND ";
            $query .= " H1.SCHOOL_KIND = T1.SCHOOL_KIND AND ";
        } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " H1.SCHOOLCD    = T1.SCHOOLCD AND ";
            $query .= " H1.SCHOOL_KIND = T1.SCHOOL_KIND AND ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " H1.SCHOOLCD    = T1.SCHOOLCD AND ";
            $query .= " H1.SCHOOL_KIND = T1.SCHOOL_KIND AND ";
        }
        $query .= "         H1.DETAIL_DATE BETWEEN T1.SDATE AND CASE WHEN T1.EDATE IS NULL THEN '9999-12-31' ELSE T1.EDATE END, ";
        $query .= "     SCHINFO T2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " T1.SCHOOLCD = '".SCHOOLCD."' AND ";
            $query .= " T1.SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " T1.SCHOOLCD    = '".SCHOOLCD."' AND ";
            $query .= " T1.SCHOOL_KIND = '".$model->field["SCHKIND"]."' AND ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " T1.SCHOOLCD = '".SCHOOLCD."' AND ";
            $query .= " T1.SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        if($model->field["taibusya_nozoku"]) {
            $query .= "     (T1.EDATE IS NULL OR ";
            if ($model->field["PATTERN"] == '2') {
                $query .= "      T1.EDATE > '".CTRL_DATE."') AND ";
            } else {
                $query .= "      T1.EDATE > '{$setToDate}') AND ";
            }
        }
        $query .= "     T1.CLUBCD IN ('".implode("','", explode(',', $model->selectdata))."') ";
        $query .= "     AND ((T1.SDATE BETWEEN '{$setFromDate}' AND '{$setToDate}') ";
        $query .= "           OR ";
        $query .= "          (VALUE(T1.EDATE, '9999-12-31') BETWEEN '{$setFromDate}' AND '{$setToDate}') ";
        $query .= "           OR ";
        $query .= "          (T1.SDATE <= '{$setFromDate}' AND VALUE(T1.EDATE, '9999-12-31') >= '{$setToDate}') ";
        $query .= "         ) ";
        $query .= " ORDER BY ";
        $query .= "     T1.CLUBCD ";
        if($sort[0] == ""){
            $query .= "     ,T2.GRADE ";
            $query .= "     ,T2.HR_CLASS ";
            $query .= "     ,T2.ATTENDNO ";
            $query .= "     ,T1.SDATE ";
        } else {
            //役職は、中京：昇順。その他：降順。
            //-- 役職コードがNULLの場合、１番下。
            $executivecd = "";
            if ($model->schoolName == "chukyo") {
                $executivecd = " ,T1.EXECUTIVECD ";
            } else {
                $executivecd = " ,case when T1.EXECUTIVECD is null then -1 else smallint(T1.EXECUTIVECD) end DESC ";
            }
            $executivecd2 = " ,T1.EXECUTIVECD ";
            for($i = 0; $i < get_count($sort); $i++){
                if($sort[$i] == 'SEX')          $query .= " ,T2.SEX ";
                if($sort[$i] == 'EXECUTIVECD')  $query .= $executivecd;
                if($sort[$i] == 'EXECUTIVECD2') $query .= $executivecd2;
                if($sort[$i] == 'NEN_KUMI_BAN') $query .= " ,T2.GRADE, T2.HR_CLASS, T2.ATTENDNO ";
            }
            if(!in_array('NEN_KUMI_BAN', $sort)) {
                $query .= " ,T2.GRADE, T2.HR_CLASS, T2.ATTENDNO ";
            }
        }
        
        return $query;
    }

    //ＣＳＶ-B作成
    function selectCsvQueryB($model) {

        $sort = explode(',', $model->selectsort);

        $query .= " WITH SCHINFO AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T3.HR_NAMEABBV, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T2.NAME_KANA, ";
        $query .= "     T2.BIRTHDAY, ";
        $query .= "     ADDR.ADDR1, ";
        $query .= "     ADDR.ADDR2, ";
        $query .= "     ADDR.TELNO, ";
        $query .= "     FIN.FINSCHOOL_NAME, ";
        $query .= "     T2.SEX ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1"){
            //全生徒が対象
        } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FIN ON T2.FINSCHOOLCD = FIN.FINSCHOOLCD ";
        $query .= "     LEFT JOIN (SELECT ";
        $query .= "                    T1.SCHREGNO, ";
        $query .= "                    T1.ADDR1, ";
        $query .= "                    T1.ADDR2, ";
        $query .= "                    T1.TELNO ";
        $query .= "                FROM ";
        $query .= "                    SCHREG_ADDRESS_DAT T1, ";
        $query .= "                    (SELECT ";
        $query .= "                          SCHREGNO, ";
        $query .= "                          MAX(ISSUEDATE) AS ISSUEDATE ";
        $query .= "                        FROM ";
        $query .= "                          SCHREG_ADDRESS_DAT ";
        $query .= "                        GROUP BY ";
        $query .= "                          SCHREGNO ";
        $query .= "                    ) T2 ";
        $query .= "                WHERE  ";
        $query .= "                    T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "                    AND T1.ISSUEDATE = T2.ISSUEDATE ";
        $query .= "         ) ADDR ON T1.SCHREGNO = ADDR.SCHREGNO, ";
        $query .= "     SCHREG_REGD_HDAT T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T3.YEAR AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE = T3.GRADE AND ";
        $query .= "     T1.HR_CLASS = T3.HR_CLASS ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.CLUBCD, ";
        $query .= "     C1.CLUBNAME, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.HR_NAMEABBV, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T2.NAME_KANA, ";
        $query .= "     T2.BIRTHDAY, ";
        $query .= "     N1.NAME1 AS SEX, ";
        $query .= "     VALUE(T2.ADDR1,'') || VALUE(T2.ADDR2,'') AS ADDR, ";
        $query .= "     T2.TELNO, ";
        $query .= "     G1.GUARD_NAME, ";
        $query .= "     G1.GUARD_KANA, ";
        $query .= "     T2.FINSCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HIST_DAT T1 ";
        $query .= "     LEFT JOIN CLUB_MST C1 ON T1.CLUBCD = C1.CLUBCD ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND C1.SCHOOLCD    = T1.SCHOOLCD ";
            $query .= " AND C1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND C1.SCHOOLCD    = T1.SCHOOLCD ";
            $query .= " AND C1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND C1.SCHOOLCD    = T1.SCHOOLCD ";
            $query .= " AND C1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN GUARDIAN_DAT G1 ON T1.SCHREGNO = G1.SCHREGNO, ";
        $query .= "     SCHINFO T2 ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' AND T2.SEX = N1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " T1.SCHOOLCD = '".SCHOOLCD."' AND ";
            $query .= " T1.SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " T1.SCHOOLCD = '".SCHOOLCD."' AND ";
            $query .= " T1.SCHOOL_KIND = '".$model->field["SCHKIND"]."' AND ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " T1.SCHOOLCD = '".SCHOOLCD."' AND ";
            $query .= " T1.SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        if($model->field["taibusya_nozoku"]) {
            $query .= "     (T1.EDATE IS NULL OR ";
            if ($model->field["PATTERN"] == '2') {
                $query .= "      T1.EDATE > '".CTRL_DATE."') AND ";
            } else {
                $query .= "      T1.EDATE > '{$setToDate}') AND ";
            }
        }
        $query .= "     T1.CLUBCD IN ('".implode("','", explode(',', $model->selectdata))."') ";
        $query .= " ORDER BY ";
        $query .= "     T1.CLUBCD ";
        if($sort[0] == ""){
            $query .= "     ,T2.GRADE ";
            $query .= "     ,T2.HR_CLASS ";
            $query .= "     ,T2.ATTENDNO ";
            $query .= "     ,T1.SDATE ";
        } else {
            //役職は、中京：昇順。その他：降順。
            //-- 役職コードがNULLの場合、１番下。
            $executivecd = "";
            if ($model->schoolName == "chukyo") {
                $executivecd = " ,T1.EXECUTIVECD ";
            } else {
                $executivecd = " ,case when T1.EXECUTIVECD is null then -1 else smallint(T1.EXECUTIVECD) end DESC ";
            }
            $executivecd2 = " ,T1.EXECUTIVECD ";
            for($i = 0; $i < get_count($sort); $i++){
                if($sort[$i] == 'SEX')          $query .= " ,T2.SEX ";
                if($sort[$i] == 'EXECUTIVECD')  $query .= $executivecd;
                if($sort[$i] == 'EXECUTIVECD2') $query .= $executivecd2;
                if($sort[$i] == 'NEN_KUMI_BAN') $query .= " ,T2.GRADE, T2.HR_CLASS, T2.ATTENDNO ";
            }
            if(!in_array('NEN_KUMI_BAN', $sort)) {
                $query .= " ,T2.GRADE, T2.HR_CLASS, T2.ATTENDNO ";
            }
        }
        
        return $query;
    }
}
?>
