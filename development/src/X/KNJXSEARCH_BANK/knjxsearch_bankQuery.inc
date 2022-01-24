<?php

require_once('for_php7.php');

class knjxsearch_bankQuery extends Query {

    //学校区分抽出
    function GetSchoolDiv($year) {
        $query  = "select schooldiv";
        $query .= "  from school_mst";
        $query .= " where year = '".$year."'";
        return $query;
    }

    function GetYear() {
        $query  = "select distinct Fiscalyear(grd_date) as graduate_year";
        $query .= "  from grd_base_mst";
        $query .= " order by graduate_year desc";
        return $query;
    }

   //学年と組
   function GetHr_Class($model)
   {
        $query = "";
        $query .= "SELECT ";
        $query .= "  T1.GRADE, ";
        $query .= "  T1.HR_CLASS, ";
        $query .= "  T1.HR_NAME, ";
        $query .= "  T1.HR_NAMEABBV, ";
        $query .= "  T2.STAFFCD, ";
        $query .= "  T2.STAFFNAME_SHOW ";
        $query .= "FROM ";
        $query .= "  SCHREG_REGD_HDAT T1 ";
        if ($model->usr_auth == DEF_UPDATE_RESTRICT || $model->usr_auth == DEF_REFER_RESTRICT) {
            $query .= "  LEFT JOIN SCHREG_REGD_GDAT L1 ON L1.YEAR  = T1.YEAR ";
            $query .= "                               AND L1.GRADE = T1.GRADE ";
        }
        $query .= "  ,STAFF_MST T2 ";
        $query .= "WHERE ";
        if ($model->usr_auth == DEF_UPDATE_RESTRICT || $model->usr_auth == DEF_REFER_RESTRICT) {
            $query .= " ((T1.TR_CD1 = '". STAFFCD ."' OR";           //2005/06/29
            $query .= "  T1.TR_CD2 = '". STAFFCD ."' OR";           //2005/06/29
            $query .= "  T1.TR_CD3 = '". STAFFCD ."' OR";           //2005/06/29
            $query .= "  T1.SUBTR_CD1 = '". STAFFCD ."' OR";        //2005/06/29
            $query .= "  T1.SUBTR_CD2 = '". STAFFCD ."' OR";        //2005/06/29
            $query .= "  T1.SUBTR_CD3 = '". STAFFCD ."') ";      //2005/06/29
            $query .= " OR (L1.GRADE IN ";
            $query .= "       (SELECT ";
            $query .= "            FIELD2 ";
            $query .= "        FROM ";
            $query .= "            STAFF_DETAIL_MST ST ";
            $query .= "        WHERE ";
            $query .= "            T1.YEAR = ST.YEAR ";
            $query .= "            AND ST.STAFFCD = '".STAFFCD."' ";
            $query .= "            AND ST.STAFF_SEQ IN ('005', '006', '007') ";
            $query .= "            AND ST.FIELD1 = '0200')) ";
            $query .= "   ) AND ";
        }
        $query .= "  T1.TR_CD1 = T2.STAFFCD AND ";
        $query .= "  T1.YEAR || '-' || T1.SEMESTER = '".CTRL_YEAR."-".CTRL_SEMESTER."' ";
        if ($model->grdGrade == "1") {
            $query .= "  AND EXISTS (SELECT ";
            $query .= "                 'X' ";
            $query .= "             FROM ";
            $query .= "                 NAME_MST A023 ";
            $query .= "             WHERE ";
            $query .= "                 A023.NAMECD1 = 'A023' ";
            $query .= "                 AND T1.GRADE BETWEEN A023.NAMESPARE2 AND A023.NAMESPARE3) ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "  AND EXISTS (SELECT ";
                $query .= "                 'X' ";
                $query .= "             FROM ";
                $query .= "                 SCHREG_REGD_GDAT S1 ";
                $query .= "             WHERE ";
                $query .= "                 S1.YEAR     = T1.YEAR AND ";
                $query .= "                 S1.GRADE    = T1.GRADE AND ";
                $query .= "                 S1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
            }
        } else if ($model->school_kind != "") {
            $query .= "  AND EXISTS (SELECT ";
            $query .= "                 'X' ";
            $query .= "             FROM ";
            $query .= "                 SCHREG_REGD_GDAT S1 ";
            $query .= "             WHERE ";
            $query .= "                 S1.YEAR     = T1.YEAR AND ";
            $query .= "                 S1.GRADE    = T1.GRADE AND ";
            $query .= "                 S1.SCHOOL_KIND IN ('".implode(explode(':', $model->school_kind),"','")."')) ";
        }

        $query .= " ORDER BY ";
        $query .= "  T1.GRADE,T1.HR_CLASS ";

        return $query;
    }

      //銀行コード
    function selectBankcd($model)
    {
        $query = " SELECT DISTINCT ";
        $query .= "     T1.BANKCD, ";
        $query .= "     T1.BRANCHCD, ";
        $query .= "     T2.BANKNAME, ";
        $query .= "     T2.BANKNAME_KANA, ";
        $query .= "     T2.BRANCHNAME, ";
        $query .= "     T2.BRANCHNAME_KANA, ";
        $query .= "     T1.DEPOSIT_ITEM, ";
        $query .= "     MEISYOU_GET(T1.DEPOSIT_ITEM,'G203',1) AS DEPOSIT_ITEM2 ";
        $query .= " FROM ";
        $query .= "     REGISTBANK_DAT T1 LEFT OUTER JOIN BANK_MST T2  ";
        $query .= "         ON T1.BANKCD = T2.BANKCD  ";
        $query .= "         AND T1.BRANCHCD = T2.BRANCHCD ";
        $query .= " ORDER BY ";
        $query .= "     T1.BANKCD, ";
        $query .= "     T1.BRANCHCD, ";
        $query .= "     T1.DEPOSIT_ITEM ";

        return $query;
    }

    function nameGet($cd)
    {
        $query  = "SELECT namecd2, name1 FROM v_name_mst WHERE namecd1 = '".$cd."' AND year = '".CTRL_YEAR."'";
        return $query;
    }

    function SearchStudent($model) {
        $grade = "";
        if ($model->cmd == "search") {
            $grade = $model->search_fields["HR_CLASS"];
        }
        
        if ($model->cmd == "chg_grade") {
            $grade = $model->grade;
        }

        $query = " SELECT DISTINCT ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T1.GRADE, ";
        $query .= "   T1.HR_CLASS, ";
        $query .= "   T3.HR_NAME, ";
        $query .= "   T3.HR_NAMEABBV, ";
        $query .= "   T1.ATTENDNO, ";
        $query .= "   T2.SEX, ";
        $query .= "   T2.NAME_SHOW, ";
        $query .= "   T2.NAME_KANA, ";
        $query .= "   FISCALYEAR(T2.GRD_DATE) AS GRD_YEAR ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT T1 ";
        $query .= "   LEFT JOIN REGISTBANK_DAT REGISTBANK ON T1.SCHREGNO = REGISTBANK.SCHREGNO, ";
        $query .= "   SCHREG_BASE_MST T2,";
        $query .= "   SCHREG_REGD_HDAT T3 ";
        $query .= " WHERE ";
        if ($grade){
            $query .= "   T1.GRADE || '-' || T1.HR_CLASS = '".$grade."' AND ";
        }
        $query .= "   T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "   T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        if ($model->search_fields["COURSECODE"]){
            $query .= "   T1.COURSECODE = '".$model->search_fields["COURSECODE"]."' AND ";
        }
        if ($model->search_fields["NAME"]){
            $query .= "   T2.NAME LIKE '%".str_replace("'","''",$model->search_fields["NAME"])."%' AND ";
        }
        if ($model->search_fields["SRCH_SCHREGNO"]){
            $query .= "   T1.SCHREGNO = '".$model->search_fields["SRCH_SCHREGNO"]."' AND ";
        }
        if ($model->search_fields["NAME"]){
            $query .= "   T2.NAME LIKE '%".str_replace("'","''",$model->search_fields["NAME"])."%' AND ";
        }
        if ($model->search_fields["NAME_SHOW"]){
            $query .= "   T2.NAME_SHOW LIKE '%".str_replace("'","''",$model->search_fields["NAME_SHOW"])."%' AND ";
        }
        if ($model->search_fields["NAME_KANA"]){
            $query .= "   T2.NAME_KANA LIKE '%".str_replace("'","''",$model->search_fields["NAME_KANA"])."%' AND ";
        }
        if ($model->search_fields["NAME_ENG"]){
            $query .= "   T2.NAME_ENG LIKE '%".str_replace("'","''",$model->search_fields["NAME_ENG"])."%' AND ";
        }
        if ($model->search_fields["BANKCD"]){
            $query .= "   REGISTBANK. BANKCD = '".$model->search_fields["BANKCD"]."' AND ";
        }
        if ($model->search_fields["BRANCHCD"]){
            $query .= "   REGISTBANK. BRANCHCD = '".$model->search_fields["BRANCHCD"]."' AND ";
        }
        if ($model->search_fields["DEPOSIT_ITEM"]){
            $query .= "   REGISTBANK. DEPOSIT_ITEM = '".$model->search_fields["DEPOSIT_ITEM"]."' AND ";
        }
        if ($model->search_fields["ACCOUNTNO"]){
            $query .= "   REGISTBANK. ACCOUNTNO = '".$model->search_fields["ACCOUNTNO"]."' AND ";
        }
        $query .= "   T1.YEAR     = T3.YEAR AND ";
        $query .= "   T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "   T1.GRADE    = T3.GRADE AND ";
        $query .= "   T1.HR_CLASS = T3.HR_CLASS AND ";
        $query .= "   T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "   T1.GRADE,";
        $query .= "   T1.HR_CLASS,";
        $query .= "   T1.ATTENDNO ";

        return $query;
    }
}
?>
