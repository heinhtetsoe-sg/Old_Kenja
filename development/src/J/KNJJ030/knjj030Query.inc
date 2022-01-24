<?php

require_once('for_php7.php');

class knjj030Query extends Query {

    //学籍基礎マスタより学籍番号と名前を取得
    function getSchregno_name($schregno){
        $query  ="SELECT schregno,name_show as name FROM schreg_base_mst";
        $query .=" WHERE schregno = '".$schregno."'";

        return $query;
    }

    //校種取得
    function getSchkind($model){
        $query  =" SELECT ";
        $query .="     SCHOOL_KIND ";
        $query .=" FROM ";
        $query .="     SCHREG_REGD_GDAT ";
        $query .=" WHERE ";
        $query .="         YEAR  = '".CTRL_YEAR."' ";
        $query .="     AND GRADE = '".$model->grade."' ";

        return $query;
    }

    //学籍部クラブ履歴データよりデータを取得 
    function getAward($model, $year,$schregno)
    {
        $query  = " SELECT DISTINCT ";
        $query .= " T3.CLUBCD,T1.CLUBCD || '　' || T1.CLUBNAME AS CLUBNAME";
        $query .= " ,T3.SDATE,T3.EDATE,T3.EXECUTIVECD || '　' || T4.NAME1 AS EXECUTIVECD,T3.REMARK";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " , T3.REMARK1 ";
        }
        $query .= " FROM ";
        $query .= " ( ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "    SELECT T001.*, T002.REMARK1 AS REMARK1 "; 
            $query .= "    FROM ";
            $query .= "      SCHREG_CLUB_HIST_DAT T001 ";
            $query .= "      LEFT JOIN CLUB_DETAIL_DAT T002 ";
            $query .= "        ON T002.CLUBCD = T001.CLUBCD ";
            $query .= "       AND T002.YEAR = '".$year."' ";
            $query .= "       AND T002.SEQ = '001' ";
            $query .= "       AND T002.SCHOOLCD = T001.SCHOOLCD ";
            $query .= "       AND T002.SCHOOL_KIND = T001.SCHOOL_KIND ";
            $query .= "    WHERE T001.SCHREGNO   = '".$schregno."' ";
            $query .= "      AND T001.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "      AND T001.SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else {
            $query .= "    SELECT * "; 
            $query .= "    FROM  SCHREG_CLUB_HIST_DAT ";
            $query .= "    WHERE SCHREGNO   = '".$schregno."' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= "    AND SCHOOLCD    = '".SCHOOLCD."' ";
                $query .= "    AND SCHOOL_KIND = '".$model->schKind."' ";
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "    AND SCHOOLCD    = '".SCHOOLCD."' ";
                $query .= "    AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        $query .= " ) T3 ";
        $query .= " INNER JOIN ( ";
        $query .= "    SELECT CLUBCD "; 
        $query .= "    FROM  CLUB_YDAT ";
        $query .= "    WHERE YEAR    = '".$year."'";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "    AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "    AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND = '".$model->schKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "    AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ) T2 ON T2.CLUBCD  = T3.CLUBCD ";
        $query .= " INNER JOIN CLUB_MST T1 ON T1.CLUBCD = T3.CLUBCD ";
        if (($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") || $model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "    AND T3.SCHOOLCD    = T1.SCHOOLCD ";
            $query .= "    AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ";
        }
        $query .= " LEFT JOIN ( ";
        $query .= "    SELECT NAMECD2,NAME1 "; 
        $query .= "    FROM  NAME_MST ";
        $query .= "    WHERE NAMECD1    = 'J001'";
        $query .= " ) T4 ON T4.NAMECD2  = T3.EXECUTIVECD ";

        return $query; 
    }
    
    //１レコード取得
    function getRow($model, $cd1,$cd2)
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT * FROM SCHREG_CLUB_HIST_DAT ";
        $query .= "    WHERE SCHREGNO   = '".$cd1."' ";
        $query .= "    AND CLUBCD       = '".$cd2."' ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "    AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "    AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND = '".$model->schKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "    AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
    
    //１レコード取得SDATE含む NO001 add
    function getRowSdate($model, $cd1,$cd2,$cd3)
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT * FROM SCHREG_CLUB_HIST_DAT ";
        $query .= "    WHERE SCHREGNO   = '".$cd1."' ";
        $query .= "    AND CLUBCD       = '".$cd2."' ";
        $query .= "    AND SDATE        = '" .str_replace("/","-",$cd3) ."' ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "    AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "    AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND = '".$model->schKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "    AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
    //NAME_MSTから役職名を取得
    function getName_Data($cd1)
    {

        $query  = " SELECT DISTINCT T1.NAMECD2, T1.NAME1";
        $query .= " FROM NAME_MST T1,NAME_YDAT T2";
        $query .= " WHERE   T1.NAMECD1  = 'J001'";
        $query .= "     AND T2.YEAR = '" .$cd1 ."'";
        $query .= "     AND T2.NAMECD1  = T1.NAMECD1";
        $query .= "     AND T2.NAMECD2  = T1.NAMECD2";
        $query .= " ORDER BY T1.NAMECD2 ";

        return $query;
    }

    //CLUB_MSTから部クラブ名を取得
    function getClub_Data($model, $cd1)
    {

        $query  = " SELECT DISTINCT T1.CLUBCD,T1.CLUBCD || '　' || T1.CLUBNAME AS CLUBNAME";
        $query .= " FROM CLUB_MST T1,CLUB_YDAT T2";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " INNER JOIN CLUB_DETAIL_DAT CDDX ";
            $query .= "    ON CDDX.CLUBCD = T2.CLUBCD ";
            $query .= "   AND CDDX.YEAR = T2.YEAR ";
            $query .= "   AND CDDX.SEQ = '001' ";
            $query .= "   AND CDDX.REMARK1 Like '%".$model->schKind."%' ";
        }
        $query .= " WHERE   T2.YEAR = '" .$cd1 ."'";
        $query .= "     AND T2.CLUBCD  = T1.CLUBCD";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "    AND T1.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= "    AND T2.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "    AND T1.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T1.SCHOOL_KIND = '".$model->schKind."' ";
            $query .= "    AND T2.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T2.SCHOOL_KIND = '".$model->schKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "    AND T1.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= "    AND T2.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY T1.CLUBCD ";

        return $query;
    }

    //年組,先生名
    function getGrd_ClasQuery($model)
    {
        $query  = "SELECT DISTINCT ";
        $query .= "       T1.GRADE || ',' || T1.HR_CLASS AS GC,";
        $query .= "       T1.HR_NAME AS GC_J, ";
        $query .= "       T2.STAFFNAME AS NAME_SHOW ";
        $query .= "  FROM SCHREG_REGD_HDAT T1 LEFT OUTER JOIN STAFF_MST T2 ";
        $query .= "       ON T1.tr_cd1 = T2.staffcd";
        $query .= " WHERE T1.YEAR     = '". $model->control_data["年度"]. "' ";
        $query .= "   AND T1.SEMESTER = '". $model->control_data["学期"]. "' ";

        if ($model->sec_competence == DEF_UPDATE_RESTRICT || $model->sec_competence == DEF_REFER_RESTRICT)
            $query .= "AND T1.TR_CD1 = '". STAFFCD ."'";
        $query .= " ORDER BY GC ";
        return $query;
    }

    //生徒一覧
    function ReadQuery($model)
    {
        $query = " SELECT DISTINCT "; 
        $query .= " T1.YEAR, T1.GRADE, T1.ATTENDNO,T2.SCHREGNO, T2.NAME_SHOW AS NAME_SHOW, ";
        $query .= " T1.HR_CLASS,T2.NAME_KANA AS KANA_SHOW, T2.SEX, T1.UPDATED ";
        $query .= " ,( SELECT COUNT(*) FROM SCHREG_REGD_DAT T1"; 
        $query .= "    INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "    WHERE T1.YEAR   = '".$model->control_data["年度"]."' ";
        $query .= "    AND T1.GRADE || T1.HR_CLASS  = '". $model->GradeClass ."' ";
        $query .= "    AND T1.SEMESTER = '".$model->control_data["学期"]."' )";
        $query .= "    AS CLASS_SUM ";                     
        $query .= " FROM  SCHREG_REGD_DAT T1 ";
        $query .= " LEFT OUTER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE T1.YEAR     = '". $model->control_data["年度"] . "' "; 
        $query .= "   AND T1.GRADE || T1.HR_CLASS = '". $model->GradeClass . "' ";
        $query .= "   AND T1.SEMESTER = '". $model->control_data["学期"]. "' ";
        $query .= " ORDER BY T1.GRADE,T1.HR_CLASS,T1.ATTENDNO ";

        return $query;
    }

    //チェック用レコード取得 NO001
    function getClubHistory_Dat_chk($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT * ";
        $query .= " FROM SCHREG_CLUB_HIST_DAT ";
        $query .= "    WHERE SCHREGNO   = '".$model->field["SCHREGNO"]."' ";
        $query .= "    AND CLUBCD       = '".$model->field["CLUBCD"]."' ";
        $query .= "    AND SDATE    = '".str_replace("/","-",$model->field["SDATE"])."' ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "    AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "    AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND = '".$model->schKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "    AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        $getrec = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $getrec;
    }

    //記録詳細存在チェック
    function chkDetailExist($model, $kind)
    {
        $db = Query::dbCheckOut();

        $query  = " WITH HIST AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         CLUBCD, ";
        if($kind == "update"){
            $query .= "         CASE WHEN EDATE IS NULL THEN '9999-12-31' ELSE EDATE END AS EDATE1, ";
            $query .= "         CASE WHEN '".str_replace("/","-",$model->field["EDATE"])."' = '' THEN '9999-12-31' ELSE '".str_replace("/","-",$model->field["EDATE"])."' END AS EDATE2 ";
        } else {
            $query .= "         SDATE, ";
            $query .= "         CASE WHEN EDATE IS NULL THEN '9999-12-31' ELSE EDATE END AS EDATE ";
        }
        $query .= "     FROM ";
        $query .= "         SCHREG_CLUB_HIST_DAT ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO = '".$model->schregno."' AND ";
        $query .= "         CLUBCD = '".$model->field["CLUBCD"]."' AND ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "    SCHOOLCD    = '".SCHOOLCD."' AND ";
            $query .= "    SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "    SCHOOLCD    = '".SCHOOLCD."' AND ";
            $query .= "    SCHOOL_KIND = '".$model->schKind."' AND ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "    SCHOOLCD    = '".SCHOOLCD."' AND ";
            $query .= "    SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "         SDATE = '".str_replace("/","-",$model->field["SDATE"])."' ";

        $query .= " ), DETAIL AS( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.CLUBCD, ";
        $query .= "         T2.DETAIL_DATE ";
        $query .= "     FROM ";
        $query .= "         HIST T1, ";
        $query .= "         SCHREG_CLUB_HDETAIL_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "         T1.CLUBCD = T2.CLUBCD AND ";
        if($kind == "update"){
            $query .= "         T1.EDATE2 < T1.EDATE1 AND ";
            $query .= "         T2.DETAIL_DATE <= T1.EDATE1 AND ";
            $query .= "         T2.DETAIL_DATE > T1.EDATE2 ";
        } else {
            $query .= "         T2.DETAIL_DATE BETWEEN T1.SDATE AND T1.EDATE ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "    AND T2.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T2.SCHOOL_KIND = '".$model->schKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "    AND T2.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "     T1.DETAIL_DATE ";
        $query .= " FROM ";
        $query .= "     DETAIL T1 ";
        $query .= " WHERE ";
        $query .= "     T1.DETAIL_DATE NOT IN ( ";
        $query .= "         SELECT ";
        $query .= "             S1.DETAIL_DATE ";
        $query .= "         FROM ";
        $query .= "             DETAIL S1, ";
        $query .= "             SCHREG_CLUB_HIST_DAT S2 ";
        $query .= "         WHERE ";
        $query .= "             S1.SCHREGNO = S2.SCHREGNO AND ";
        $query .= "             S1.CLUBCD = S2.CLUBCD AND ";
        $query .= "             S2.SDATE <> '".str_replace("/","-",$model->field["SDATE"])."' AND ";
        $query .= "             S1.DETAIL_DATE BETWEEN S2.SDATE AND ";
        $query .= "             CASE WHEN S2.EDATE IS NULL THEN '9999-12-31' ELSE S2.EDATE END) ";

        $cnt = get_count($db->getCol($query));

        Query::dbCheckIn($db);
        return $cnt;
    }

    //INSERT
    function &getInsertQuery($model, $fields,$schregno)
    {         
        $db = Query::dbCheckOut();
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $data["SCHOOLCD"][TEXT]     = SCHOOLCD;
            $data["SCHOOL_KIND"][TEXT]  = SCHOOLKIND;
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $data["SCHOOLCD"][TEXT]     = SCHOOLCD;
            $data["SCHOOL_KIND"][TEXT]  = $model->schKind;
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $data["SCHOOLCD"][TEXT]     = SCHOOLCD;
            $data["SCHOOL_KIND"][TEXT]  = SCHOOLKIND;
        }
        
        $data["SCHREGNO"][TEXT]     = $schregno;
        $data["CLUBCD"][TEXT]       = $fields["CLUBCD"];
        $data["SDATE"][TEXT]        = str_replace("/","-",$fields["SDATE"]);
        $data["EDATE"][TEXT]        = str_replace("/","-",$fields["EDATE"]);
        $data["EXECUTIVECD"][TEXT]  = $fields["EXECUTIVECD"];
        $data["REMARK"][TEXT]       = $fields["REMARK"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $query = Query::insertSQL($data, "schreg_club_hist_dat");
        
        $db->query($query);
        Query::dbCheckIn($db);
        
        return;
    }

    //UPDATE
    function &getUpdateQuery($model, $fields,$schregno)
    {        
        $db = Query::dbCheckOut();

        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $data["SCHOOLCD"][TEXT]     = SCHOOLCD;
            $data["SCHOOL_KIND"][TEXT]  = SCHOOLKIND;
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $data["SCHOOLCD"][TEXT]     = SCHOOLCD;
            $data["SCHOOL_KIND"][TEXT]  = $model->schKind;
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $data["SCHOOLCD"][TEXT]     = SCHOOLCD;
            $data["SCHOOL_KIND"][TEXT]  = SCHOOLKIND;
        }
        $data["SCHREGNO"][TEXT]     = $schregno;
        $data["CLUBCD"][TEXT]       = $fields["CLUBCD"];
        $data["SDATE"][TEXT]        = str_replace("/","-",$fields["SDATE"]);
        $data["EDATE"][TEXT]        = str_replace("/","-",$fields["EDATE"]);
        $data["EXECUTIVECD"][TEXT]  = $fields["EXECUTIVECD"];
        $data["REMARK"][TEXT]       = $fields["REMARK"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $where  = " WHERE SCHREGNO  = '" .$schregno ."' ";
        $where .= "   AND CLUBCD    = '" .$fields["CLUBCD"] ."' ";
        $where .= "   AND SDATE     = '" .str_replace("/","-",$fields["SDATE"]) ."' ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $where.= "     AND SCHOOLCD    = '".SCHOOLCD."' ";
            $where.= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $where.= "     AND SCHOOLCD    = '".SCHOOLCD."' ";
            $where.= "     AND SCHOOL_KIND = '".$model->schKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $where.= "     AND SCHOOLCD    = '".SCHOOLCD."' ";
            $where.= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        $query = Query::updateSQL($data, "schreg_club_hist_dat", $where);
        
        $db->query($query);
        Query::dbCheckIn($db);
        
        return ;
    }

    //DELETE
    function &getDeleteQuery($model, $fields,$schregno)
    {
        $db = Query::dbCheckOut();

        $query  = "DELETE FROM schreg_club_hist_dat ";
        $query .= " WHERE SCHREGNO  = '" .$schregno ."' ";
        $query .= "   AND CLUBCD    = '" .$fields["CLUBCD"] ."' ";
        $query .= "   AND SDATE     = '" .str_replace("/","-",$fields["SDATE"]) ."' ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "    AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "    AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND = '".$model->schKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "    AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }
}
?>
