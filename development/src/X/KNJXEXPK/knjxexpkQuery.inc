<?php

require_once('for_php7.php');

class knjxexpkQuery extends Query {
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
        $query .= "  SCHREG_REGD_HDAT T1,";
        $query .= "  STAFF_MST T2 ";
        $query .= "WHERE ";
#        if ($model->usr_auth == DEF_UPDATE_RESTRICT) {             //2005/05/31
        if ($model->usr_auth == DEF_UPDATE_RESTRICT || $model->usr_auth == DEF_REFER_RESTRICT) {
            $query .= " T1.TR_CD1 = '". STAFFCD ."' AND";
        }
        $query .= "  T1.TR_CD1 = T2.STAFFCD AND ";
        $query .= "  T1.YEAR = '" .CTRL_YEAR ."' AND ";
        $query .= "  T1.SEMESTER = '" .CTRL_SEMESTER ."' ";
        $query .= " ORDER BY ";
        $query .= "  T1.GRADE,T1.HR_CLASS ";

        return $query;
    }
    //コース取得
    function GetCourseCode()
    {
        $query = " SELECT ";
        $query .= "   COURSECODE, ";
        $query .= "   COURSECODENAME ";
        $query .= " FROM ";
        $query .= "   COURSECODE_MST ";

        return $query;
    }
    //卒業年度
    function GetGrdYear()
    {
        $query = " SELECT DISTINCT ";
        $query .= "   FISCALYEAR(GRD_DATE) AS GRD_YEAR ";
        $query .= " FROM ";
        $query .= "   GRD_BASE_MST ";

        return $query;
    }
    function GetStudents(&$model)
    {
        if ($model->cmd == "list" && $model->grade){
            $grade = $model->grade;
        }else if ($model->cmd == "list" && !$model->grade){
            $grade = "";
        }else if ($model->cmd == "search" && $model->search["GRADE"]){
            $grade = $model->search["GRADE"];
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
        $query .= "   SCHREG_REGD_DAT T1, ";
        $query .= "   SCHREG_BASE_MST T2,";
        $query .= "   SCHREG_REGD_HDAT T3 ";
        $query .= " WHERE ";
        if (isset($grade)){
            $query .= "   T1.GRADE || '-' || T1.HR_CLASS = '" .$grade."' AND ";
        }
        if ($model->search["HR_CLASS"]){
            $query .= "   T2.GRD_HR_CLASS = '" .sprintf("%02d", $model->search["HR_CLASS"]). "' AND ";
        }
        $query .= "   T1.YEAR = '" .CTRL_YEAR. "' AND ";
        $query .= "   T1.SEMESTER = '" .CTRL_SEMESTER. "' AND ";
        if ($model->search["COURSECODE"]){
            $query .= "   T1.COURSECODE = '" .$model->search["COURSECODE"]. "' AND ";
        }
        if ($model->search["NAME"]){
            $query .= "   T2.NAME LIKE '%" .str_replace("'","''",$model->search["NAME"]). "%' AND ";
        }
        if ($model->search["NAME_SHOW"]){
            $query .= "   T2.NAME_SHOW LIKE '%" .str_replace("'","''",$model->search["NAME_SHOW"]). "%' AND ";
        }
        if ($model->search["NAME_KANA"]){
            $query .= "   T2.NAME_KANA LIKE '%" .str_replace("'","''",$model->search["NAME_KANA"]). "%' AND ";
        }
        if ($model->search["NAME_ENG"]){
            $query .= "   T2.NAME_ENG LIKE '%" .str_replace("'","''",$model->search["NAME_ENG"]). "%' AND ";
        }
        if ($model->search["SEX"]){
            $query .= "   T2.SEX = '" .$model->search["SEX"]. "' AND ";
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

#        echo $query;
        return $query;
    }
    //異動情報検索
    function GetStudents2(&$model)
    {
        $query = " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T3.HR_NAME, ";
        $query .= "     T3.HR_NAMEABBV, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.SEX, ";
        $query .= "     T2.NAME_SHOW, ";
        $query .= "     T2.NAME_KANA, ";
        $query .= "     FISCALYEAR(T2.GRD_DATE) AS GRD_YEAR ";
        $query .= " FROM ";
        if ($model->search2["DIV2"] || $model->search2["BASEDATE2"] ){
            $query .= "     SCHREG_TRANSFER_DAT T4, ";
        }
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_BASE_MST T2, ";
        $query .= "     SCHREG_REGD_HDAT T3 ";
        $query .= " WHERE ";
        if ($model->search2["DIV1"]){
            $query .= "     T2.ENT_DIV = '".$model->search2["DIV1"]."' AND ";
        }
        if ($model->search2["BASEDATE1"]){
            $query .= "     T2.ENT_DATE = DATE('".str_replace("/","-",$model->search2["BASEDATE1"])."') AND ";
        }
        if ($model->search2["DIV3"]){
            $query .= "     T2.GRD_DIV = '".$model->search2["DIV3"]."' AND ";
        }
        if ($model->search2["BASEDATE3"]){
            $query .= "     T2.GRD_DATE = DATE('".str_replace("/","-",$model->search2["BASEDATE3"])."') AND ";
        }
        if ($model->search2["DIV2"] || $model->search2["BASEDATE2"] ){
            $query .= "     T1.SCHREGNO = T4.SCHREGNO AND ";
        }
        if ($model->search2["DIV2"]){
            $query .= "     T4.TRANSFERCD = '".$model->search2["DIV2"]."' AND ";
        }
        if ($model->search2["BASEDATE2"]){
            $query .= "     DATE('".str_replace("/","-",$model->search2["BASEDATE2"])."') BETWEEN TRANSFER_SDATE AND ";
            $query .= "     COALESCE(TRANSFER_EDATE,DATE('9999-12-31')) AND ";
        }
        $query .= "     T1.YEAR = '" .CTRL_YEAR. "' AND ";
        $query .= "     T1.SEMESTER = '" .CTRL_SEMESTER. "' AND ";
        $query .= "     T1.YEAR = T3.YEAR AND ";
        $query .= "     T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "     T1.GRADE = T3.GRADE AND ";
        $query .= "     T1.HR_CLASS = T3.HR_CLASS AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";
        return $query;
    }
    //交付情報検索
    function GetStudents3(&$model)
    {
        $query = " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T3.HR_NAME, ";
        $query .= "     T3.HR_NAMEABBV, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.SEX, ";
        $query .= "     T2.NAME_SHOW, ";
        $query .= "     T2.NAME_KANA, ";
        $query .= "     FISCALYEAR(T2.GRD_DATE) AS GRD_YEAR ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_BASE_MST T2, ";
        $query .= "     SCHREG_REGD_HDAT T3, ";
        $query .= "     SCHREG_GRANT_DAT T4 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = T4.SCHREGNO AND ";
        if ($model->search3["BASEDATE"]){
            $query .= "     DATE('".str_replace("/","-",$model->search3["BASEDATE"])."') BETWEEN GRANTSDATE AND ";
            $query .= "     COALESCE(GRANTEDATE,DATE('9999-12-31')) AND ";
        }
        if ($model->search3["YEAR"]){
            $query .= "     T4.YEAR = '".$model->search3["YEAR"]."' AND ";
        }
        if ($model->search3["GRANTCD"]){
            $query .= "     T4.GRANTCD = '".$model->search3["GRANTCD"]."' AND ";
        }
        $query .= "     T1.YEAR = '" .CTRL_YEAR. "' AND ";
        $query .= "     T1.SEMESTER = '" .CTRL_SEMESTER. "' AND ";
        $query .= "     T1.YEAR = T3.YEAR AND ";
        $query .= "     T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "     T1.GRADE = T3.GRADE AND ";
        $query .= "     T1.HR_CLASS = T3.HR_CLASS AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";
#        echo $query;
        return $query;
    }
    //銀行情報検索
    function GetStudents4(&$model)
    {
        $query = " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T3.HR_NAME, ";
        $query .= "     T3.HR_NAMEABBV, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.SEX, ";
        $query .= "     T2.NAME_SHOW, ";
        $query .= "     T2.NAME_KANA, ";
        $query .= "     FISCALYEAR(T2.GRD_DATE) AS GRD_YEAR ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_BASE_MST T2, ";
        $query .= "     SCHREG_REGD_HDAT T3, ";
        $query .= "     REGISTBANK_DAT T4 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = T4.SCHREGNO AND ";
        if ($model->search4["BANKCD"]){
            $query .= "     T4.BANKCD = '".$model->search4["BANKCD"]."' AND ";
        }
        if ($model->search4["BRANCHCD"]){
#            $query .= "     T4.BRANCHCD = '".$model->search4["BRANCHCD"]."' AND ";
            $query .= "     T4.BRANCHCD = '".substr($model->search4["BRANCHCD"],5,3)."' AND ";
        }
        if ($model->search4["DEPOSIT_ITEM"]){
            $query .= "     T4.DEPOSIT_ITEM = '".$model->search4["DEPOSIT_ITEM"]."' AND ";
        }
        $query .= "     T1.YEAR = '" .CTRL_YEAR. "' AND ";
        $query .= "     T1.SEMESTER = '" .CTRL_SEMESTER. "' AND ";
        $query .= "     T1.YEAR = T3.YEAR AND ";
        $query .= "     T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "     T1.GRADE = T3.GRADE AND ";
        $query .= "     T1.HR_CLASS = T3.HR_CLASS AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";
#        echo $query;
        return $query;
    }    
    //入金情報検索（中分類）
    function GetStudents5(&$model)
    {
        $query = " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T3.HR_NAME, ";
        $query .= "     T3.HR_NAMEABBV, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.SEX, ";
        $query .= "     T2.NAME_SHOW, ";
        $query .= "     T2.NAME_KANA, ";
        $query .= "     FISCALYEAR(T2.GRD_DATE) AS GRD_YEAR ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_BASE_MST T2, ";
        $query .= "     SCHREG_REGD_HDAT T3 ";
        $query .= " WHERE ";
        if (is_numeric($model->search5["MONEY_DUE"]) || 
            $model->search5["REDUCTION_REASON"] || 
            $model->search5["BANK_TRANS_STOP_REASON"] || 
            $model->search5["INST_CD"] ){
            $query .= "     EXISTS(SELECT ";
            $query .= "                 * ";
            $query .= "             FROM ";
            $query .= "                 MONEY_DUE_M_DAT W ";
            $query .= "             WHERE ";
            $query .= "                 W.YEAR = '".CTRL_YEAR."' AND ";
            if ($model->search5["EXPENSE_M_CD"]){
                $query .= "                 W.EXPENSE_M_CD = '".substr($model->search5["EXPENSE_M_CD"],2,2)."' AND ";
            }
            if (is_numeric($model->search5["MONEY_DUE"])){
                $query .= "                 W.MONEY_DUE = ".$model->search5["MONEY_DUE"] ." AND ";
            }
            if ($model->search5["REDUCTION_REASON"]){
                $query .= "                 W.REDUCTION_REASON = '".$model->search5["REDUCTION_REASON"] ."' AND ";
            }
            if ($model->search5["BANK_TRANS_STOP_REASON"]){
                $query .= "                 W.BANK_TRANS_STOP_RESON = '".$model->search5["BANK_TRANS_STOP_REASON"] ."' AND ";
            }
            if ($model->search5["INST_CD"] == 1){
                $query .= "             W.INST_CD IS NULL AND ";
            }else if ($model->search5["INST_CD"] == 2){
                $query .= "             W.INST_CD IS NOT NULL AND ";
            }
            $query .= "                 W.SCHREGNO = T1.SCHREGNO  ";
            $query .= "             ) AND ";
        }
        if ($model->search5["PAID_INPUT_FLG"] || 
            $model->search5["PAID_MONEY_DATE"] || 
            is_numeric($model->search5["PAID_MONEY"]) || 
            $model->search5["PAID_MONEY_DIV"] ||
            $model->search5["REPAY_DATE"] || 
            is_numeric($model->search5["REPAY_MONEY"]) || 
            $model->search5["REPAY_DEV"]
            ){
            $query .= "     EXISTS(SELECT ";
            $query .= "                 * ";
            $query .= "             FROM ";
            $query .= "                 MONEY_PAID_M_DAT W2 ";
            $query .= "             WHERE ";
            if ($model->search5["PAID_INPUT_FLG"]){
                $query .= "             W2.PAID_INPUT_FLG = '". $model->search5["PAID_INPUT_FLG"]."' AND ";
            }
            if ($model->search5["EXPENSE_M_CD"]){
                $query .= "             W2.YEAR = '".CTRL_YEAR."' AND ";
                $query .= "             W2.EXPENSE_L_CD = '". $model->search5["EXPENSE_L_CD"]."' AND ";
                $query .= "             W2.EXPENSE_M_CD = '". substr($model->search5["EXPENSE_M_CD"],2,2)."' AND ";
            }
            if ($model->search5["PAID_MONEY_DATE"]){
                $query .= "             W2.PAID_MONEY_DATE = '". str_replace("/","-",$model->search5["PAID_MONEY_DATE"])."' AND ";
            }
            if (is_numeric($model->search5["PAID_MONEY"])){
                $query .= "             W2.PAID_MONEY = ". $model->search5["PAID_MONEY"]." AND ";
            }
            if ($model->search5["PAID_MONEY_DIV"]){
                $query .= "             W2.PAID_MONEY_DIV = '". $model->search5["PAID_MONEY_DIV"]."' AND ";
            }
            if ($model->search5["REPAY_DATE"]){
                $query .= "             W2.REPAY_DATE = '". str_replace("/","-",$model->search5["REPAY_DATE"])."' AND ";
            }
            if (is_numeric($model->search5["REPAY_MONEY"])){
                $query .= "             W2.REPAY_MONEY = ". $model->search5["REPAY_MONEY"]." AND ";
            }
            if ($model->search5["REPAY_DEV"]){
                $query .= "             W2.REPAY_DEV = '". $model->search5["REPAY_DEV"]."' AND ";
            }
            $query .= "                 W2.SCHREGNO = T1.SCHREGNO  ";
            $query .= "             ) AND ";
        }
        $query .= "     T1.YEAR = '" .CTRL_YEAR. "' AND ";
        $query .= "     T1.SEMESTER = '" .CTRL_SEMESTER. "' AND ";
        $query .= "     T1.YEAR = T3.YEAR AND ";
        $query .= "     T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "     T1.GRADE = T3.GRADE AND ";
        $query .= "     T1.HR_CLASS = T3.HR_CLASS AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";
        return $query;
    }    
    //入金情報検索（小分類）
    function GetStudents6(&$model)
    {
        $query = " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T3.HR_NAME, ";
        $query .= "     T3.HR_NAMEABBV, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.SEX, ";
        $query .= "     T2.NAME_SHOW, ";
        $query .= "     T2.NAME_KANA, ";
        $query .= "     FISCALYEAR(T2.GRD_DATE) AS GRD_YEAR ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_BASE_MST T2, ";
        $query .= "     SCHREG_REGD_HDAT T3 ";
        $query .= " WHERE ";
        if (is_numeric($model->search6["MONEY_DUE"])){
            $query .= "     EXISTS(SELECT ";
            $query .= "                 * ";
            $query .= "             FROM ";
            $query .= "                 MONEY_DUE_S_DAT W ";
            $query .= "             WHERE ";
            $query .= "                 W.YEAR = '".CTRL_YEAR."' AND ";
            if ($model->search6["EXPENSE_S_CD"]){
                $query .= "                 W.EXPENSE_S_CD = '".substr($model->search6["EXPENSE_S_CD"],4,2)."' AND ";
            }
            if (is_numeric($model->search6["MONEY_DUE"])){
                $query .= "                 W.MONEY_DUE = ".$model->search6["MONEY_DUE"] ." AND ";
            }
            $query .= "                 W.SCHREGNO = T1.SCHREGNO  ";
            $query .= "             ) AND ";
        }
        if ($model->search6["PAID_INPUT_FLG"] || 
            $model->search6["PAID_MONEY_DATE"] || 
            is_numeric($model->search6["PAID_MONEY"]) || 
            $model->search6["PAID_MONEY_DIV"] ||
            $model->search6["REPAY_DATE"] || 
            is_numeric($model->search6["REPAY_MONEY"]) || 
            $model->search6["REPAY_DEV"]
            ){
            $query .= "     EXISTS(SELECT ";
            $query .= "                 * ";
            $query .= "             FROM ";
            $query .= "                 MONEY_PAID_S_DAT W2 ";
            $query .= "             WHERE ";
            if ($model->search6["PAID_INPUT_FLG"]){
                $query .= "             W2.PAID_INPUT_FLG = '". $model->search6["PAID_INPUT_FLG"]."' AND ";
            }
            if ($model->search6["EXPENSE_M_CD"]){
                $query .= "             W2.YEAR = '".CTRL_YEAR."' AND ";
                $query .= "             W2.EXPENSE_M_CD = '". substr($model->search6["EXPENSE_M_CD"],2,2)."' AND ";
                $query .= "             W2.EXPENSE_S_CD = '". substr($model->search6["EXPENSE_S_CD"],4,2)."' AND ";
            }
            if ($model->search6["PAID_MONEY_DATE"]){
                $query .= "             W2.PAID_MONEY_DATE = '". str_replace("/","-",$model->search6["PAID_MONEY_DATE"])."' AND ";
            }
            if (is_numeric($model->search6["PAID_MONEY"])){
                $query .= "             W2.PAID_MONEY = ". $model->search6["PAID_MONEY"]." AND ";
            }
            if ($model->search6["PAID_MONEY_DIV"]){
                $query .= "             W2.PAID_MONEY_DIV = '". $model->search6["PAID_MONEY_DIV"]."' AND ";
            }
            if ($model->search6["REPAY_DATE"]){
                $query .= "             W2.REPAY_MONEY_DATE = '". str_replace("/","-",$model->search6["REPAY_DATE"])."' AND ";
            }
            if (is_numeric($model->search6["REPAY_MONEY"])){
                $query .= "             W2.REPAY_MONEY = ". $model->search6["REPAY_MONEY"]." AND ";
            }
            if ($model->search6["REPAY_DEV"]){
                $query .= "             W2.REPAY_MONEY_DIV = '". $model->search6["REPAY_DEV"]."' AND ";
            }
            $query .= "                 W2.SCHREGNO = T1.SCHREGNO  ";
            $query .= "             ) AND ";
        }
        $query .= "     T1.YEAR = '" .CTRL_YEAR. "' AND ";
        $query .= "     T1.SEMESTER = '" .CTRL_SEMESTER. "' AND ";
        $query .= "     T1.YEAR = T3.YEAR AND ";
        $query .= "     T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "     T1.GRADE = T3.GRADE AND ";
        $query .= "     T1.HR_CLASS = T3.HR_CLASS AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";
#        echo $query;
        return $query;
    }    
    //入学区分、異動区分、卒業区分の内容
    function selectSearchDiv(&$model)
    {
        $query = " SELECT DISTINCT ";
        $query .= "     T2.NAMECD1, ";
        $query .= "     T1.ENT_DIV AS DIV, ";
        $query .= "     T2.NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1, ";
        $query .= "     V_NAME_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.ENT_DIV = T2.NAMECD2 AND ";
        $query .= "     T2.NAMECD1 = 'A002' ";
        $query .= " UNION  ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T2.NAMECD1, ";
        $query .= "     T1.GRD_DIV AS DIV , ";
        $query .= "     T2.NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1, ";
        $query .= "     V_NAME_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.GRD_DIV = T2.NAMECD2 AND ";
        $query .= "     T2.NAMECD1 = 'A003' ";
        $query .= " UNION  ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T2.NAMECD1, ";
        $query .= "     T1.TRANSFERCD AS DIV, ";
        $query .= "     T2.NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_TRANSFER_DAT T1, ";
        $query .= "     V_NAME_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.TRANSFERCD = T2.NAMECD2 AND ";
        $query .= "     T2.NAMECD1 = 'A004' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD1, ";
        $query .= "     DIV ";

        return $query;
    }

    //交付コード
    function selectGrantcd(&$model)
    {
        $query = " SELECT DISTINCT ";
        $query .= "     T1.GRANTCD, ";
        $query .= "     T2.NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_GRANT_DAT T1, ";
        $query .= "     V_NAME_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.GRANTCD = T2.NAMECD2 AND ";
        $query .= "     T2.NAMECD1 = 'Z006' ";
        $query .= " ORDER BY ";
        $query .= "     GRANTCD ";

        return $query;
    }
    //銀行コード
    function selectBankcd(&$model)
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
        
#        echo $query;
        return $query;
    }
    //費目大分類
    function selectExpense_L_Cd(&$model)
    {
#        $query = " SELECT DISTINCT ";
#        $query .= "     T1.EXPENSE_L_CD, ";
#        $query .= "     MEISYOU_GET(T1.EXPENSE_L_CD,'G201',1) AS EXPENSE_L_CD2, ";
#        $query .= "     T1.EXPENSE_M_CD, ";
#        $query .= "     T3.EXPENSE_M_NAME ";
#        $query .= " FROM ";
#        $query .= "     MONEY_PAID_M_DAT T1 , ";
#        $query .= "     MONEY_DUE_M_DAT T2, ";
#        $query .= "     EXPENSE_M_MST T3 ";
#        $query .= " WHERE ";
#        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
#        $query .= "     T1.YEAR = T2.YEAR AND ";
#        $query .= "     T1.YEAR = T3.YEAR AND ";
#        $query .= "     T1.SCHREGNO     = T2.SCHREGNO AND ";
#        $query .= "     T1.EXPENSE_M_CD = T3.EXPENSE_M_CD AND ";
#        $query .= "     T1.EXPENSE_M_CD = T2.EXPENSE_M_CD ";
#        $query .= " ORDER BY ";
#        $query .= "     T1.EXPENSE_M_CD ";

        $query = " SELECT DISTINCT ";
        $query .= "     T2.EXPENSE_L_CD, ";
        $query .= "     MEISYOU_GET(T2.EXPENSE_L_CD,'G201',1) AS EXPENSE_L_CD2, ";
        $query .= "     T2.EXPENSE_M_CD, ";
        $query .= "     T2.EXPENSE_M_NAME ";
        $query .= " FROM ";
        $query .= "     MONEY_DUE_M_DAT T1, ";
        $query .= "     EXPENSE_M_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.EXPENSE_M_CD = T2.EXPENSE_M_CD ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.EXPENSE_L_CD, ";
        $query .= "     MEISYOU_GET(T1.EXPENSE_L_CD,'G201',1) AS EXPENSE_L_CD2, ";
        $query .= "     T1.EXPENSE_M_CD, ";
        $query .= "     T2.EXPENSE_M_NAME ";
        $query .= " FROM ";
        $query .= "     MONEY_PAID_M_DAT T1 , ";
        $query .= "     EXPENSE_M_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.EXPENSE_M_CD = T2.EXPENSE_M_CD ";
        $query .= " ORDER BY ";
        $query .= "     EXPENSE_L_CD,EXPENSE_M_CD ";


#        echo $query;
        return $query;
    }
    //費目小分類
    function selectExpense_S_Cd(&$model)
    {
#        $query = " SELECT DISTINCT ";
#        $query .= "     T1.EXPENSE_S_CD, ";
#        $query .= "     T3.EXPENSE_S_NAME, ";
#        $query .= "     T4.EXPENSE_M_CD, ";
#        $query .= "     T4.EXPENSE_M_NAME, ";
#        $query .= "     T5.EXPENSE_L_CD, ";
#        $query .= "     MEISYOU_GET(T5.EXPENSE_L_CD,'G201',1) AS EXPENSE_L_CD2 ";
#        $query .= " FROM ";
#        $query .= "     MONEY_PAID_S_DAT T1, ";
#        $query .= "     MONEY_DUE_S_DAT T2, ";
#        $query .= "     EXPENSE_S_MST T3, ";
#        $query .= "     EXPENSE_M_MST T4, ";
#        $query .= "     MONEY_PAID_M_DAT T5 ";
#        $query .= " WHERE ";
#        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
#        $query .= "     T1.YEAR = T2.YEAR AND ";
#        $query .= "     T1.YEAR = T4.YEAR AND ";
#        $query .= "     T1.YEAR = T5.YEAR AND ";
#        $query .= "     T1.EXPENSE_S_CD = T2.EXPENSE_S_CD AND ";
#        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
#        $query .= "     T1.SCHREGNO = T5.SCHREGNO AND ";
#        $query .= "     T1.EXPENSE_M_CD = T4.EXPENSE_M_CD AND ";
#        $query .= "     T1.EXPENSE_M_CD = T5.EXPENSE_M_CD AND ";
#        $query .= "     T1.SCHREGNO = T5.SCHREGNO ";
#        $query .= " ORDER BY ";
#        $query .= "     T1.EXPENSE_S_CD, ";
#        $query .= "     T4.EXPENSE_M_CD, ";
#        $query .= "     T5.EXPENSE_L_CD ";

        $query = " SELECT DISTINCT ";
        $query .= "     T2.EXPENSE_S_CD, ";
        $query .= "     T2.EXPENSE_S_NAME, ";
        $query .= "     T3.EXPENSE_M_CD, ";
        $query .= "     T3.EXPENSE_M_NAME, ";
        $query .= "     T3.EXPENSE_L_CD, ";
        $query .= "     MEISYOU_GET(T3.EXPENSE_L_CD,'G201',1) AS EXPENSE_L_CD2 ";
        $query .= " FROM ";
        $query .= "     MONEY_DUE_S_DAT T1, ";
        $query .= "     EXPENSE_S_MST T2, ";
        $query .= "     EXPENSE_M_MST T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = T3.YEAR AND ";
        $query .= "     T1.EXPENSE_S_CD = T2.EXPENSE_S_CD AND ";
        $query .= "     T2.EXPENSE_M_CD = T3.EXPENSE_M_CD ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T2.EXPENSE_S_CD, ";
        $query .= "     T2.EXPENSE_S_NAME, ";
        $query .= "     T3.EXPENSE_M_CD, ";
        $query .= "     T3.EXPENSE_M_NAME, ";
        $query .= "     T3.EXPENSE_L_CD, ";
        $query .= "     MEISYOU_GET(T3.EXPENSE_L_CD,'G201',1) AS EXPENSE_L_CD2 ";
        $query .= " FROM ";
        $query .= "     MONEY_PAID_S_DAT T1, ";
        $query .= "     EXPENSE_S_MST T2, ";
        $query .= "     EXPENSE_M_MST T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = T3.YEAR AND ";
        $query .= "     T1.EXPENSE_S_CD = T2.EXPENSE_S_CD AND ";
        $query .= "     T2.EXPENSE_M_CD = T3.EXPENSE_M_CD ";
        $query .= " ORDER BY ";
        $query .= "     EXPENSE_L_CD, ";
        $query .= "     EXPENSE_M_CD, ";
        $query .= "     EXPENSE_S_CD ";
#        echo $query;
        return $query;
    }

    //入金予定情報
    function selectMoney_Due_M_Dat(&$model)
    {
        $query = " SELECT DISTINCT ";
        $query .= "     T1.REDUCTION_REASON, ";
        $query .= "     MEISYOU_GET(T1.REDUCTION_REASON,'G204',1) AS REDUCTION_REASON2, ";
        $query .= "     T1.INST_CD, ";
        $query .= "     T1.BANK_TRANS_STOP_RESON, ";
        $query .= "     MEISYOU_GET(T1.BANK_TRANS_STOP_RESON,'G208',1) AS BANK_TRANS_STOP_RESON2 ";
        $query .= " FROM ";
        $query .= "     MONEY_DUE_M_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.EXPENSE_M_CD = '". substr($model->Expense["M"],0,2)."'";
        $query .= " ORDER BY ";
        $query .= "     T1.REDUCTION_REASON, ";
        $query .= "     T1.BANK_TRANS_STOP_RESON ";
        return $query;
    }
    //入金済情報
    function selectMoney_Paid_M_Dat(&$model)
    {
        $query = " SELECT DISTINCT ";
        $query .= "     T1.PAID_INPUT_FLG, ";
        $query .= "     T1.PAID_MONEY_DIV, ";
        $query .= "     MEISYOU_GET(T1.PAID_MONEY_DIV,'G205',1) AS PAID_MONEY_DIV2, ";
        $query .= "     T1.REPAY_DEV, ";
        $query .= "     MEISYOU_GET(T1.REPAY_DEV,'Z006',1) AS REPAY_DEV2 ";
        $query .= " FROM ";
        $query .= "     MONEY_PAID_M_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.EXPENSE_L_CD = '". $model->Expense["L"]."' AND ";
        $query .= "     T1.EXPENSE_M_CD = '". substr($model->Expense["M"],2,2)."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.PAID_INPUT_FLG, ";
        $query .= "     T1.PAID_MONEY_DIV, ";
        $query .= "     T1.REPAY_DEV ";
        $query .= "  ";
        return $query;
    }
    //入金済情報
    function selectMoney_Paid_S_Dat(&$model)
    {
        $query = " SELECT DISTINCT ";
        $query .= "     T1.PAID_INPUT_FLG, ";
        $query .= "     T1.PAID_MONEY_DIV, ";
        $query .= "     MEISYOU_GET(T1.PAID_MONEY_DIV,'G205',1) AS PAID_MONEY_DIV2, ";
        $query .= "     T1.REPAY_MONEY_DIV AS REPAY_DEV, ";
        $query .= "     MEISYOU_GET(T1.REPAY_MONEY_DIV,'G209',1) AS REPAY_DEV2 ";
        $query .= " FROM ";
        $query .= "     MONEY_PAID_S_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.EXPENSE_M_CD = '". substr($model->Expense["S"],2,2)."' AND ";
        $query .= "     T1.EXPENSE_S_CD = '". substr($model->Expense["S"],4,2)."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.PAID_INPUT_FLG, ";
        $query .= "     T1.PAID_MONEY_DIV, ";
        $query .= "     T1.REPAY_MONEY_DIV ";
        $query .= "  ";

        return $query;
    }
}
?>
