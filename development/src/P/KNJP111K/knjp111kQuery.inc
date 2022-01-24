<?php

require_once('for_php7.php');

class knjp111kQuery extends Query {

    function GetMaxUpdate($db, $field)
    {
        $query  = " SELECT ";
        $query .= "     DATE(MAX(".$field.")), ";
        $query .= "     TIME(MAX(".$field.")) ";
        $query .= "   FROM ";
        $query .= "     APPL_RESULT_TMP_DATA ";
        $tmp   = $db->getRow($query);
        
        $date1 = ($tmp[0]=="") ? NULL : strftime("%Y年%m月%d日 %H時%M分",strtotime($tmp[0]." ".$tmp[1]));
        return $date1;
    }

    function GetProcessCount($field)
    {
        $query  = "SELECT COUNT(".$field.") ";
        $query .= "  FROM APPL_RESULT_TMP_DATA ";

        switch ($field) {
            case "PROCESS1_UPDATED":
                $query .= " WHERE PROCESS1_STS = '0' ";
                break;
            case "PROCESS2_UPDATED":
                $query .= " WHERE PROCESS2_STS = '0' ";
                break;
            default:
                break;
        }
        return $query;
    }

    function GetErrCount($val)
    {
//        $query  = "SELECT COUNT(*) FROM APPL_RESULT_ERR_DATA WHERE PROCESSCD = '".$val."' AND ERR_LEVEL != '0' ";
		//NO002
		$query  = "SELECT COUNT(FILE_LINE_NUMBER) ";
		$query .= "FROM ";
		$query .= "    (SELECT DISTINCT ";
		$query .= "        FILE_LINE_NUMBER AS FILE_LINE_NUMBER ";
		$query .= "     FROM ";
		$query .= "        APPL_RESULT_ERR_DATA ";
		$query .= "     WHERE ";
		$query .= "        PROCESSCD = '".$val."' ";
		$query .= "        AND FILE_LINE_NUMBER != 0) T1 ";
        return $query;
    }

    function DeleteQuery($table)
    {
        $query  = "DELETE FROM ".$table;
        return $query;
    }

	//NO001
	function GetJorH(){
		$query  = " SELECT ";
		$query .= "     COUNT(*) ";
		$query .= " FROM ";
		$query .= "     SCHOOL_MST ";
		$query .= " WHERE ";
		$query .= "     YEAR = '".CTRL_YEAR."' AND ";
		$query .= "     SCHOOLNAME1 LIKE '%近畿%中学%' ";

		return $query;
	}

    function InsertQuery($data)
    {

        $query = " INSERT INTO APPL_RESULT_TMP_DATA( ";
        $query .= " FILE_LINE_NUMBER, ";
        $query .= " PROCESS1_UPDATED, ";
        $query .= " PROCESS1_STS, ";
        $query .= " MAKE_DATE, ";
        $query .= " BANKCD, ";
        $query .= " BRANCHCD, ";
        $query .= " DEPOSIT_ITEM, ";
        $query .= " ACCOUNTNO, ";
        $query .= " ACCOUNTNAME, ";
        $query .= " APPLI_PAID_DATE, ";
        $query .= " INOUT_FG, ";
        $query .= " JOB_FG, ";
        $query .= " APPLI_PAID_MONEY, ";
        $query .= " REFERENCE_NUMBER, ";
        $query .= " CLIENT_NAME, ";
        $query .= " CLIENT_NAME_ORG, ";
        $query .= " REGISTERCD, ";
        $query .= " UPDATED ";
        $query .= " ) ";
        $query .= " VALUES ";
        $query .= " ( ";
        $query .= $data["FILE_LINE_NUMBER"] .", ";
        $query .= "SYSDATE(), ";
        $query .= "'0', ";
        $query .= "'".$data["MAKE_DATE"] ."', ";
        $query .= "'".$data["BANKCD"] ."', ";
        $query .= "'".$data["BRANCHCD"] ."', ";
        $query .= "'".$data["DEPOSIT_ITEM"] ."', ";
        $query .= "'".$data["ACCOUNTNO"] ."', ";
        $query .= "'".$data["ACCOUNTNAME"] ."', ";
        $query .= "'".$data["APPLI_PAID_DATE"] ."', ";
        $query .= "'".$data["INOUT_FG"] ."', ";
        $query .= "'".$data["JOB_FG"] ."', ";
        $query .= $data["APPLI_PAID_MONEY"] .", ";
        $query .= "'".$data["REFERENCE_NUMBER"] ."', ";
        $query .= "'".$data["CLIENT_NAME"] ."', ";
        $query .= "'".$data["CLIENT_NAME_ORG"] ."', ";
        $query .= "'".STAFFCD ."', ";
        $query .= "SYSDATE() ";
        $query .= " ) ";

        return $query;
    }

    function ErrDataInsert($err_data)
    {
        $data = array();
        $data["PROCESSCD"][TEXT]            = $err_data["PROCESSCD"];
        $data["FILE_LINE_NUMBER"][NUMBER]   = $err_data["FILE_LINE_NUMBER"];
        $data["COLNAME"][TEXT]              = $err_data["COLNAME"];
        $data["ERR_LEVEL"][TEXT]            = $err_data["ERR_LEVEL"];
        $data["ERR_MSG"][TEXT]              = $err_data["ERR_MSG"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";
        
        $query = Query::insertSQL($data, "APPL_RESULT_ERR_DATA");

        return $query;
    }
    //申込コード
    function selectQueryAppli(&$model)
    {
        $query = " SELECT DISTINCT ";
        $query .= "     t1.APPLICATIONNAME, ";
        $query .= "     t1.APPLICATIONCD ";
        $query .= " FROM ";
        $query .= "     APPLICATION_MST t1 ";
        $query .= " WHERE ";
        $query .= "     t1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     t1.APPLICATIONCD ";

        return $query;
    }
    //振込み銀行情報チェック
    function InsertBankcdErrors(&$db, &$model)
    {
        $query = " INSERT INTO APPL_RESULT_ERR_DATA( ";
        $query .= " PROCESSCD, ";
        $query .= " FILE_LINE_NUMBER, ";
        $query .= " COLNAME, ";
        $query .= " ERR_LEVEL, ";
        $query .= " ERR_MSG, ";
        $query .= " REGISTERCD, ";
        $query .= " UPDATED ";
        $query .= " ) ";
        $query .= "   ( ";
        $sep = "";
        foreach(array("BANKCD","BRANCHCD","DEPOSIT_ITEM","ACCOUNTNO","ACCOUNTNAME") as $v){
			$query .= $sep;
			$query .= "     SELECT ";
			$query .= "         '2', ";
			$query .= "         T1.FILE_LINE_NUMBER, ";
			$query .= "         '".$v ."', ";
			$query .= "         '8', ";
			$query .= "         'MSG901 入力された値は不正です。', ";
			$query .= "         '".STAFFCD ."', ";
			$query .= "         SYSDATE() ";
			$query .= "     FROM ";
			$query .= "         APPL_RESULT_TMP_DATA T1 ";
			$query .= "     WHERE ";
			$query .= "         NOT EXISTS (SELECT ";
			$query .= "                         * ";
			$query .= "                     FROM ";
			$query .= "                         APPLICATION_MST W ";
			$query .= "                     WHERE ";
			$query .= "                         W.APPLICATIONCD = '".$model->applicationcd ."' AND ";
			$query .= "                         W.YEAR = '".CTRL_YEAR ."' AND ";
			$query .= "                         T1.BANKCD = W.BANKCD AND ";
			$query .= "                         T1.BRANCHCD = W.BRANCHCD AND ";
			$query .= "                         T1.DEPOSIT_ITEM = W.DEPOSIT_ITEM AND ";
			$query .= "                         digits(INTEGER(T1.ACCOUNTNO)) = digits(INTEGER(W.ACCOUNTNO)) AND ";
			$query .= "                         T1.ACCOUNTNAME = W.ACCOUNTNAME ";
			$query .= "                     ) ";
            
			$sep = "UNION";
        }
        $query .= "     ) ";

        $db->query($query);
    }
    //整理番号エラーチェック
    function GetFileLineNum(&$model, $field)
    {
        $query = " SELECT DISTINCT ";
        $query .= "     T1.FILE_LINE_NUMBER ";
        $query .= " FROM ";
        $query .= "     APPL_RESULT_TMP_DATA T1, ";
        $query .= "     (SELECT ";
        $query .= "         W2.SCHREGNO, ";
        $query .= "         W4.APPLI_PAID_MONEY, ";
        $query .= "         W4.APPLI_MONEY_DUE, ";
//        $query .= "         replace(replace(W3.NAME_KANA,' ',''),'　','')  AS NAME_KANA, ";
        $query .= "         W3.NAME_KANA AS NAME_KANA, ";
        $query .= "         digits(INTEGER(W1.BANK_MAJORCD || W1.GRADE || W1.BANK_HR_CLASS || SUBSTR(W2.ATTENDNO,2,2))) AS REFERENCE_NUMBER ";
        $query .= "     FROM ";
        $query .= "         BANK_CLASS_MST W1, ";
        $query .= "         SCHREG_REGD_DAT W2, ";
        $query .= "         SCHREG_BASE_MST W3, ";
        $query .= "         APPLICATION_DAT W4 ";
        $query .= "     WHERE ";
        $query .= "         W1.YEAR = '".CTRL_YEAR ."' AND ";
        $query .= "         W1.YEAR = W2.YEAR AND ";
        $query .= "         W1.YEAR = W4.YEAR AND ";
        $query .= "         W2.SEMESTER = '".CTRL_SEMESTER ."' AND ";
        $query .= "         W1.GRADE = W2.GRADE AND ";
        $query .= "         W1.HR_CLASS = W2.HR_CLASS AND ";
        $query .= "         W2.SCHREGNO = W3.SCHREGNO AND ";
        $query .= "         W2.SCHREGNO = W4.SCHREGNO AND ";
        $query .= "         W4.APPLICATIONCD    = '".$model->applicationcd ."' ";
        $query .= "     ) T2 ";
        $query .= " WHERE ";
        switch($field){
        case "APPLI_PAID_MONEY":        //入金額チェック
        case "APPLI_PAID_MONEY2":       //入金済みデータ存在チェック
            if ($field == "APPLI_PAID_MONEY"){
    			$query .= "     T2.APPLI_MONEY_DUE <> T1.APPLI_PAID_MONEY AND ";
            }else{
    			$query .= "     T2.APPLI_PAID_MONEY IS NOT NULL AND ";
            }
	//NO004-->
			$query .= "     translate(T2.NAME_KANA,'やゆよあいうえおつ','ゃゅょぁぃぅぇぉっ')  = T1.CLIENT_NAME AND ";
			$query .= "     T2.REFERENCE_NUMBER = T1.REFERENCE_NUMBER ";
			break 1;
	//NO004<--
        case "CLIENT_NAME":             //振込依頼人名チェック
//			$query .= "     T2.NAME_KANA  = T1.CLIENT_NAME AND ";
	//NO004-->
			$query .= "     translate(T2.NAME_KANA,'やゆよあいうえおつ','ゃゅょぁぃぅぇぉっ') <> T1.CLIENT_NAME ";	//2006.01.21 alp m-yama NO003
			$query .= "     AND T2.REFERENCE_NUMBER = T1.REFERENCE_NUMBER ";
			$query .= "     AND T1.FILE_LINE_NUMBER NOT IN (SELECT FILE_LINE_NUMBER FROM APPL_RESULT_ERR_DATA WHERE PROCESSCD = '2' AND COLNAME = 'OK') ";
//			$query .= "     AND T1.FILE_LINE_NUMBER NOT IN (SELECT FILE_LINE_NUMBER ";
//			$query .= "                                     FROM APPL_RESULT_ERR_DATA ";
//			$query .= "                                     WHERE PROCESSCD = '2' ";
//			$query .= "                                           AND COLNAME = 'DOUBLE_ERR') ";
			break 1;
	//NO004<--
        case "REFERENCE_NUMBER":        //整理番号チェック
	//NO004-->
			$query .= "     translate(T2.NAME_KANA,'やゆよあいうえおつ','ゃゅょぁぃぅぇぉっ') = T1.CLIENT_NAME ";	//2006.01.21 alp m-yama NO003
			$query .= "     AND T2.REFERENCE_NUMBER <> T1.REFERENCE_NUMBER ";
			$query .= "     AND T1.FILE_LINE_NUMBER NOT IN (SELECT FILE_LINE_NUMBER FROM APPL_RESULT_ERR_DATA WHERE PROCESSCD = '2' AND COLNAME = 'OK') ";
//			$query .= "     AND T1.FILE_LINE_NUMBER NOT IN (SELECT FILE_LINE_NUMBER ";
//			$query .= "                                     FROM APPL_RESULT_ERR_DATA ";
//			$query .= "                                     WHERE PROCESSCD = '2' ";
//			$query .= "                                           AND COLNAME = 'DOUBLE_ERR') ";
			break 1;
	//NO004<--
	//NO004-->
        case "OK":        //振込依頼人名+整理番号チェック
			$query .= "     translate(T2.NAME_KANA,'やゆよあいうえおつ','ゃゅょぁぃぅぇぉっ') = T1.CLIENT_NAME ";	//2006.01.21 alp m-yama NO003
			$query .= "     AND T2.REFERENCE_NUMBER = T1.REFERENCE_NUMBER ";
			break 1;
	//NO004<--
	//NO004-->
        case "DOUBLE_ERR":        //振込依頼人名+整理番号チェック
			$query .= "     T1.FILE_LINE_NUMBER NOT IN (SELECT FILE_LINE_NUMBER FROM APPL_RESULT_ERR_DATA WHERE PROCESSCD = '2') ";
			$query .= "     AND T1.CLIENT_NAME || T1.REFERENCE_NUMBER NOT IN ";
	        $query .= "     (SELECT ";
			$query .= "         W3.NAME_KANA || digits(INTEGER(W1.BANK_MAJORCD || W1.GRADE || W1.BANK_HR_CLASS || SUBSTR(W2.ATTENDNO,2,2))) ";
			$query .= "     FROM ";
			$query .= "         BANK_CLASS_MST  W1, ";
			$query .= "         SCHREG_REGD_DAT W2, ";
			$query .= "         SCHREG_BASE_MST W3, ";
			$query .= "         APPLICATION_DAT W4 ";
			$query .= "     WHERE ";
			$query .= "         W1.YEAR = '".CTRL_YEAR ."' AND ";
			$query .= "         W1.YEAR = W2.YEAR AND ";
			$query .= "         W1.YEAR = W4.YEAR AND ";
			$query .= "         W2.SEMESTER = '".CTRL_SEMESTER ."' AND ";
			$query .= "         W1.GRADE = W2.GRADE AND ";
			$query .= "         W1.HR_CLASS = W2.HR_CLASS AND ";
			$query .= "         W2.SCHREGNO = W3.SCHREGNO AND ";
			$query .= "         W2.SCHREGNO = W4.SCHREGNO AND ";
			$query .= "         W4.APPLICATIONCD    = '".$model->applicationcd ."' ";
			$query .= "     ) ";
			break 1;
	//NO004<--
        }

        return $query;
    }
    //整理番号エラーチェック
    function InsertReference_NumberErrors(&$db, &$model)
    {
        $query = " INSERT INTO APPL_RESULT_ERR_DATA( ";
        $query .= " PROCESSCD, ";
        $query .= " FILE_LINE_NUMBER, ";
        $query .= " COLNAME, ";
        $query .= " ERR_LEVEL, ";
        $query .= " ERR_MSG, ";
        $query .= " REGISTERCD, ";
        $query .= " UPDATED ";
        $query .= " ) ";
        $query .= "   ( ";
        $query .= " SELECT ";
        $query .= "     '2', ";
        $query .= "     T1.FILE_LINE_NUMBER, ";
        $query .= "     'REFERENCE_NUMBER', ";
        $query .= "     '8', ";
        $query .= "     'MSG901 入力された値は不正です。', ";
        $query .= "     '".STAFFCD ."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     APPL_RESULT_TMP_DATA T1 ";
        $query .= " WHERE ";
//        $query .= "     T1.FILE_LINE_NUMBER NOT IN (" . knjp111kQuery::GetFileLineNum($model, "REFERENCE_NUMBER"). ")";
        $query .= "     T1.FILE_LINE_NUMBER IN (" . knjp111kQuery::GetFileLineNum($model, "REFERENCE_NUMBER"). ")";
        $query .= "     ) ";

        $db->query($query);
    }
    //振込依頼人名エラーチェック
    function InsertClientNameErrors(&$db, &$model)
    {
        $query = " INSERT INTO APPL_RESULT_ERR_DATA( ";
        $query .= " PROCESSCD, ";
        $query .= " FILE_LINE_NUMBER, ";
        $query .= " COLNAME, ";
        $query .= " ERR_LEVEL, ";
        $query .= " ERR_MSG, ";
        $query .= " REGISTERCD, ";
        $query .= " UPDATED ";
        $query .= " ) ";
        $query .= "   ( ";
        $query .= " SELECT ";
        $query .= "     '2', ";
        $query .= "     T1.FILE_LINE_NUMBER, ";
        $query .= "     'CLIENT_NAME', ";
        $query .= "     '8', ";
        $query .= "     'MSG901 入力された値は不正です。', ";
        $query .= "     '".STAFFCD ."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     APPL_RESULT_TMP_DATA T1 ";
        $query .= " WHERE ";
//        $query .= "     T1.FILE_LINE_NUMBER NOT IN (" . knjp111kQuery::GetFileLineNum($model, "CLIENT_NAME"). ")";
        $query .= "     T1.FILE_LINE_NUMBER IN (" . knjp111kQuery::GetFileLineNum($model, "CLIENT_NAME"). ")";
        $query .= "     ) ";
        
        $db->query($query);
    }

    //OKチェック NO004
    function InsertOk(&$db, &$model)
    {
        $query = " INSERT INTO APPL_RESULT_ERR_DATA( ";
        $query .= " PROCESSCD, ";
        $query .= " FILE_LINE_NUMBER, ";
        $query .= " COLNAME, ";
        $query .= " ERR_LEVEL, ";
        $query .= " ERR_MSG, ";
        $query .= " REGISTERCD, ";
        $query .= " UPDATED ";
        $query .= " ) ";
        $query .= "   ( ";
        $query .= " SELECT ";
        $query .= "     '2', ";
        $query .= "     T1.FILE_LINE_NUMBER, ";
        $query .= "     'OK', ";
        $query .= "     '8', ";
        $query .= "     'MSG901 入力された値は不正です。', ";
        $query .= "     '".STAFFCD ."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     APPL_RESULT_TMP_DATA T1 ";
        $query .= " WHERE ";
        $query .= "     T1.FILE_LINE_NUMBER IN (" . knjp111kQuery::GetFileLineNum($model, "OK"). ")";
        $query .= "     ) ";
        
        $db->query($query);
    }

    //整理番号+振込依頼人名エラーチェック NO004
    function InsertClientReferenceErrors(&$db, &$model)
    {
        $query = " INSERT INTO APPL_RESULT_ERR_DATA( ";
        $query .= " PROCESSCD, ";
        $query .= " FILE_LINE_NUMBER, ";
        $query .= " COLNAME, ";
        $query .= " ERR_LEVEL, ";
        $query .= " ERR_MSG, ";
        $query .= " REGISTERCD, ";
        $query .= " UPDATED ";
        $query .= " ) ";
        $query .= "   ( ";
        $query .= " SELECT ";
        $query .= "     '2', ";
        $query .= "     T1.FILE_LINE_NUMBER, ";
        $query .= "     'DOUBLE_ERR', ";
        $query .= "     '8', ";
        $query .= "     'MSG901 入力された値は不正です。', ";
        $query .= "     '".STAFFCD ."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     APPL_RESULT_TMP_DATA T1 ";
        $query .= " WHERE ";
        $query .= "     T1.FILE_LINE_NUMBER IN (" . knjp111kQuery::GetFileLineNum($model, "DOUBLE_ERR"). ")";
        $query .= "     ) ";
        
        $db->query($query);
    }

    //入金済み額エラーチェック
    function InsertAppli_Paid_MoneyErrors(&$db, &$model)
    {
        $query = " INSERT INTO APPL_RESULT_ERR_DATA( ";
        $query .= " PROCESSCD, ";
        $query .= " FILE_LINE_NUMBER, ";
        $query .= " COLNAME, ";
        $query .= " ERR_LEVEL, ";
        $query .= " ERR_MSG, ";
        $query .= " REGISTERCD, ";
        $query .= " UPDATED ";
        $query .= " ) ";
        $query .= "   ( ";
        $query .= " SELECT ";
        $query .= "     '2', ";
        $query .= "     T1.FILE_LINE_NUMBER, ";
        $query .= "     'APPLI_PAID_MONEY', ";
        $query .= "     '8', ";
        $query .= "     'MSG901 入力された値は不正です。入金データが存在します。', ";
        $query .= "     '".STAFFCD ."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     APPL_RESULT_TMP_DATA T1 ";
        $query .= " WHERE ";
        $query .= "     T1.FILE_LINE_NUMBER IN (" . knjp111kQuery::GetFileLineNum($model, "APPLI_PAID_MONEY2"). ")";
        $query .= "     ) ";

        $db->query($query);
    }
    //入金額エラーチェック
    function InsertAppli_Paid_MoneyErrors2(&$db, &$model)
    {
        $query = " INSERT INTO APPL_RESULT_ERR_DATA( ";
        $query .= " PROCESSCD, ";
        $query .= " FILE_LINE_NUMBER, ";
        $query .= " COLNAME, ";
        $query .= " ERR_LEVEL, ";
        $query .= " ERR_MSG, ";
        $query .= " REGISTERCD, ";
        $query .= " UPDATED ";
        $query .= " ) ";
        $query .= "   ( ";
        $query .= " SELECT ";
        $query .= "     '2', ";
        $query .= "     T1.FILE_LINE_NUMBER, ";
        $query .= "     'APPLI_PAID_MONEY', ";
        $query .= "     '8', ";
        $query .= "     'MSG901 入力された値は不正です。', ";
        $query .= "     '".STAFFCD ."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     APPL_RESULT_TMP_DATA T1 ";
        $query .= " WHERE ";
        $query .= "     T1.FILE_LINE_NUMBER IN (" . knjp111kQuery::GetFileLineNum($model, "APPLI_PAID_MONEY"). ") AND";
        $query .= "     NOT EXISTS(SELECT ";
        $query .= "                     * ";
        $query .= "                 FROM ";
        $query .= "                     APPL_RESULT_ERR_DATA W ";
        $query .= "                 WHERE ";
        $query .= "                     W.PROCESSCD = '2' AND ";
        $query .= "                     W.FILE_LINE_NUMBER = T1.FILE_LINE_NUMBER AND ";
        $query .= "                     W.COLNAME = 'APPLI_PAID_MONEY' ";
        $query .= "                 ) ";
        $query .= "     ) ";
        
        $db->query($query);
    }
    //消込処理
    function UpdateAppricationDat(&$db, &$model)
    {
        $query = " UPDATE ";
        $query .= "     APPLICATION_DAT T1 ";
        $query .= " SET ";
        $query .= "     (APPLI_PAID_MONEY,APPLI_PAID_FLG,APPLI_PAID_DIV,APPLI_PAID_DATE,REGISTERCD,UPDATED)=(SELECT ";
        $query .= "         ST1.APPLI_PAID_MONEY, ";
        $query .= "         '1', ";
        $query .= "         '02', ";
        $query .= "         ST1.APPLI_PAID_DATE, ";
        $query .= "         REGISTERCD, ";
        $query .= "         UPDATED ";
        $query .= "     FROM ";
        $query .= "         APPL_RESULT_TMP_DATA ST1, ";
        $query .= "         (SELECT ";
        $query .= "             W2.SCHREGNO, ";
//        $query .= "             replace(replace(W3.NAME_KANA,' ',''),'　','') AS NAME_KANA, ";
        $query .= "             W3.NAME_KANA AS NAME_KANA, ";
        $query .= "             digits(INTEGER(W1.BANK_MAJORCD || W1.GRADE || W1.BANK_HR_CLASS || SUBSTR(W2.ATTENDNO,2,2))) AS REFERENCE_NUMBER, ";
        $query .= "             '".STAFFCD."',SYSDATE() ";
        $query .= "         FROM ";
        $query .= "             BANK_CLASS_MST W1, ";
        $query .= "             SCHREG_REGD_DAT W2, ";
        $query .= "             SCHREG_BASE_MST W3 ";
        $query .= "         WHERE ";
        $query .= "             W1.YEAR = '".CTRL_YEAR ."' AND ";
        $query .= "             W1.YEAR = W2.YEAR AND ";
        $query .= "             W2.SEMESTER = '".CTRL_SEMESTER ."' AND ";
        $query .= "             W1.GRADE = W2.GRADE AND ";
        $query .= "             W1.HR_CLASS = W2.HR_CLASS AND ";
        $query .= "             W2.SCHREGNO = W3.SCHREGNO ";
        $query .= "         ) ST2 ";
        $query .= "     WHERE ";
        $query .= "         ST1.REFERENCE_NUMBER = ST2.REFERENCE_NUMBER AND ";
//        $query .= "         ST2.NAME_KANA   = ST1.CLIENT_NAME AND ";
        $query .= "         translate(ST2.NAME_KANA,'やゆよあいうえおつ','ゃゅょぁぃぅぇぉっ')   = ST1.CLIENT_NAME AND ";	//2006.01.21 alp m-yama NO003
        $query .= "         ST2.SCHREGNO    = T1.SCHREGNO AND ";
        $query .= "         NOT EXISTS(SELECT ";
        $query .= "                         * ";
        $query .= "                     FROM ";
        $query .= "                         APPL_RESULT_ERR_DATA W ";
        $query .= "                     WHERE ";
        $query .= "                         W.FILE_LINE_NUMBER = ST1.FILE_LINE_NUMBER AND ";
        $query .= "                         W.ERR_LEVEL = '8' AND ";
        $query .= "                         W.COLNAME <> 'OK' ";	//NO005
        $query .= "                     ) ";
        $query .= "     ) ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR ."' AND ";
        $query .= "     T1.APPLICATIONCD = '".$model->applicationcd ."' AND ";
        $query .= "     T1.APPLI_PAID_FLG IS NULL ";

        $db->query($query);
    }
    //エラーテーブルの処理区分が消込処理のものを削除
    function ErrDataDelete($processcd)
    {
        $query  = "DELETE FROM APPL_RESULT_ERR_DATA WHERE PROCESSCD = '".$processcd."'";
        return $query;
    }
    //エラーテーブルの処理区分が消込処理のものを削除
    function ErrDataDelete2($processcd,$colname)
    {
        $query  = "DELETE FROM APPL_RESULT_ERR_DATA WHERE PROCESSCD = '".$processcd."' AND COLNAME = '".$colname."' ";

        return $query;
    }
    //一時保管データの取引情報を更新
    function UpdateStatus(&$db, &$model)
    {
        $query = " UPDATE ";
        $query .= "     APPL_RESULT_TMP_DATA T1 ";
        $query .= " SET ";
        $query .= "     (PROCESS2_UPDATED,PROCESS2_STS) = (SELECT DISTINCT ";
        $query .= "         SYSDATE(), ";
        $query .= "         CASE WHEN ST2.PROCESSCD IS NULL THEN '0' ELSE '1' END ";
        $query .= "     FROM ";
        $query .= "         APPL_RESULT_TMP_DATA ST1 LEFT OUTER JOIN APPL_RESULT_ERR_DATA ST2 ON ST1.FILE_LINE_NUMBER = ST2.FILE_LINE_NUMBER ";
        $query .= "     WHERE ";
        $query .= "         ST1.FILE_LINE_NUMBER = T1.FILE_LINE_NUMBER ";
        $query .= "     ) ";

        $db->query($query);
    }
    //年号の最初の年を取得
    function selectQueryYear()
    {
        $query = " SELECT ";
        $query .= "     MAX(NAMESPARE1) AS YEAR ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'G210' ";

        return $query;
    }

}
?>
