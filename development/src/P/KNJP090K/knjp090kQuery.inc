<?php

require_once('for_php7.php');

class knjp090kQuery extends Query {

    function GetMaxUpdate($db, $field)
    {
        $query  = " SELECT ";
        $query .= "     DATE(MAX(".$field.")), ";
        $query .= "     TIME(MAX(".$field.")) ";
        $query .= "   FROM ";
        $query .= "     BANK_RESULT_TMP_DATA3 ";    //NO008
        $tmp   = $db->getRow($query);
        
        $date1 = ($tmp[0]=="") ? NULL : strftime("%Y年%m月%d日 %H時%M分",strtotime($tmp[0]." ".$tmp[1]));
        return $date1;
    }

    //中高判定フラグ NO007
    function getschool()
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOOLNAME1 like '%近畿%中学%' ";

        return $query;
    }

    //新入生データチェック NO009
    function checkFreshman($schregno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS FRESHCNT ";
        $query .= " FROM ";
        $query .= "     FRESHMAN_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTERYEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //重複チェック NO010
    function checkTmp1($schregno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS TMPCNT ";
        $query .= " FROM ";
        $query .= "     BANK_RESULT_TMP_DATA ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;
    }

    function GetProcessCount($field)
    {
        //NO006
        switch ($field) {
            case "PROCESS1_UPDATED":
                $query  = "SELECT COUNT(".$field.") ";
                $query .= "  FROM BANK_RESULT_TMP_DATA3 ";    //NO008
                $query .= " WHERE VALUE(PROCESS1_STS,'0') = '0' ";
                break;
            case "PROCESS2_UPDATED":
                $query  = "SELECT COUNT(".$field.") ";
                $query .= "  FROM BANK_RESULT_TMP_DATA3 ";    //NO008
                $query .= " WHERE VALUE(PROCESS2_STS,'0') = '0' ";
                break;
            case "PROCESS3_UPDATED":
                $query  = "SELECT COUNT(".$field.") ";
                $query .= "  FROM BANK_RESULT_TMP_DATA3 ";    //NO008
                $query .= " WHERE VALUE(PROCESS3_STS,'0') = '0' ";
                break;
            default:
                break;
        }
        return $query;
    }

    function GetErrCount($val)
    {
        if ($val == "1") {
            $query  = "SELECT COUNT(FILE_LINE_NUMBER) ";
            $query .= "FROM ";
            $query .= "    (SELECT DISTINCT ";
            $query .= "        FILE_LINE_NUMBER AS FILE_LINE_NUMBER ";
            $query .= "     FROM ";
            $query .= "        bank_result_err_data ";
            $query .= "     WHERE ";
            $query .= "        processcd = '".$val."') T1 ";
        } else {
            $query  = "SELECT COUNT(FILE_LINE_NUMBER) ";
            $query .= "FROM ";
            $query .= "    BANK_RESULT_TMP_DATA3 ";    //NO008
            $query .= "WHERE ";
            if ($val == "2") {
                $query .= "    PROCESS2_STS = '1' ";
            } else {
                $query .= "    PROCESS3_STS = '1' ";
            }
        }
        return $query;
    }

    //入金日読替取得
    function getReplaceData()
    {
        $query  = " SELECT ";
        $query .= "     PAID_MONEY_DATE, ";
        $query .= "     REPLACE_MONEY_DATE ";
        $query .= " FROM ";
        $query .= "     BANK_RESULT_TMP_DATA_REPLACE ";

        return $query;
    }

    //入金月の取得 NO008
    function getPaidMonth()
    {
        $query = " SELECT 
                       MONTH(T1.PAID_MONEY_DATE) AS PAID_MONTH,
                       CASE WHEN MONTH(T1.PAID_MONEY_DATE) > 3 THEN MONTH(T1.PAID_MONEY_DATE) ELSE MONTH(T1.PAID_MONEY_DATE) + 12 END AS SORT
                   FROM
                       (SELECT TERM01_PAID_MONEY_DATE AS PAID_MONEY_DATE FROM BANK_RESULT_TMP_DATA3
                        UNION
                        SELECT TERM02_PAID_MONEY_DATE AS PAID_MONEY_DATE FROM BANK_RESULT_TMP_DATA3
                        UNION
                        SELECT TERM03_PAID_MONEY_DATE AS PAID_MONEY_DATE FROM BANK_RESULT_TMP_DATA3
                        UNION
                        SELECT TERM04_PAID_MONEY_DATE AS PAID_MONEY_DATE FROM BANK_RESULT_TMP_DATA3
                        UNION
                        SELECT TERM05_PAID_MONEY_DATE AS PAID_MONEY_DATE FROM BANK_RESULT_TMP_DATA3
                        UNION
                        SELECT TERM06_PAID_MONEY_DATE AS PAID_MONEY_DATE FROM BANK_RESULT_TMP_DATA3
                        UNION
                        SELECT TERM07_PAID_MONEY_DATE AS PAID_MONEY_DATE FROM BANK_RESULT_TMP_DATA3
                        UNION
                        SELECT TERM08_PAID_MONEY_DATE AS PAID_MONEY_DATE FROM BANK_RESULT_TMP_DATA3
                        UNION
                        SELECT TERM09_PAID_MONEY_DATE AS PAID_MONEY_DATE FROM BANK_RESULT_TMP_DATA3
                        UNION
                        SELECT TERM10_PAID_MONEY_DATE AS PAID_MONEY_DATE FROM BANK_RESULT_TMP_DATA3
                        UNION
                        SELECT TERM11_PAID_MONEY_DATE AS PAID_MONEY_DATE FROM BANK_RESULT_TMP_DATA3
                        UNION
                        SELECT TERM12_PAID_MONEY_DATE AS PAID_MONEY_DATE FROM BANK_RESULT_TMP_DATA3
                       ) AS T1
                   WHERE
                       T1.PAID_MONEY_DATE IS NOT NULL
                   GROUP BY
                       MONTH(T1.PAID_MONEY_DATE)
                   ORDER BY
                       SORT
                 ";

        return $query;
    }

    //入金日の取得
    function getPaidDay($paid_month)
    {
        $query = " SELECT 
                       DAY(T1.PAID_MONEY_DATE) AS PAID_DAY
                   FROM
                       (SELECT TERM01_PAID_MONEY_DATE AS PAID_MONEY_DATE FROM BANK_RESULT_TMP_DATA
                        UNION
                        SELECT TERM02_PAID_MONEY_DATE AS PAID_MONEY_DATE FROM BANK_RESULT_TMP_DATA
                        UNION
                        SELECT TERM03_PAID_MONEY_DATE AS PAID_MONEY_DATE FROM BANK_RESULT_TMP_DATA
                        UNION
                        SELECT TERM04_PAID_MONEY_DATE AS PAID_MONEY_DATE FROM BANK_RESULT_TMP_DATA
                        UNION
                        SELECT TERM05_PAID_MONEY_DATE AS PAID_MONEY_DATE FROM BANK_RESULT_TMP_DATA
                        UNION
                        SELECT TERM06_PAID_MONEY_DATE AS PAID_MONEY_DATE FROM BANK_RESULT_TMP_DATA
                        UNION
                        SELECT TERM07_PAID_MONEY_DATE AS PAID_MONEY_DATE FROM BANK_RESULT_TMP_DATA
                        UNION
                        SELECT TERM08_PAID_MONEY_DATE AS PAID_MONEY_DATE FROM BANK_RESULT_TMP_DATA
                        UNION
                        SELECT TERM09_PAID_MONEY_DATE AS PAID_MONEY_DATE FROM BANK_RESULT_TMP_DATA
                        UNION
                        SELECT TERM10_PAID_MONEY_DATE AS PAID_MONEY_DATE FROM BANK_RESULT_TMP_DATA
                        UNION
                        SELECT TERM11_PAID_MONEY_DATE AS PAID_MONEY_DATE FROM BANK_RESULT_TMP_DATA
                        UNION
                        SELECT TERM12_PAID_MONEY_DATE AS PAID_MONEY_DATE FROM BANK_RESULT_TMP_DATA
                       ) AS T1
                   WHERE
                       T1.PAID_MONEY_DATE IS NOT NULL AND
                       MONTH(T1.PAID_MONEY_DATE) = ".$paid_month."
                   GROUP BY
                       DAY(T1.PAID_MONEY_DATE)
                 ";

        return $query;
    }

    //銀行コードの取得
    function getBankcd()
    {
        $query  = " SELECT ";
        $query .= "     bankcd, ";
        $query .= "     bankname_kana ";
        $query .= " FROM ";
        $query .= "     bank_result_tmp_data3 ";
        $query .= " WHERE bankcd IS NOT NULL ";
        $query .= " GROUP BY ";
        $query .= "     bankcd, bankname_kana ";
        $query .= " ORDER BY ";
        $query .= "     bankcd, bankname_kana ";
        
        return $query;
    }

    //銀行コードの取得
    function getBranchcd($bankcd)
    {
        $query  = " SELECT ";
        $query .= "     branchcd, ";
        $query .= "     branchname_kana ";
        $query .= " FROM ";
        $query .= "     bank_result_tmp_data3 ";
        $query .= " WHERE ";
        $query .= "     bankcd = '".$bankcd."'";
        $query .= " GROUP BY ";
        $query .= "     branchcd, branchname_kana ";
        $query .= " ORDER BY ";
        $query .= "     branchcd, branchname_kana ";

        return $query;
    }

    function DeleteQuery($table)
    {
        $query  = "DELETE FROM ".$table." WHERE FILE_LINE_NUMBER < 1000 ";
        return $query;
    }

    function DeleteQuery2($table)
    {
        $query  = "DELETE FROM ".$table." WHERE FILE_LINE_NUMBER >= 1000 ";
        return $query;
    }

    //NO009
    function InsertQuery($data_arr,$freshFlg)
    {

        $data = array();

        $data["FILE_LINE_NUMBER"][NUMBER]               = $data_arr["FILE_LINE_NUMBER"];

        if ( strlen($data_arr["DATA_DATE"]) > 0) {
            $data["DATA_DATE"][FUNC] = "date('".substr($data_arr["DATA_DATE"],0,4)."-".substr($data_arr["DATA_DATE"],4,2)."-".substr($data_arr["DATA_DATE"],6,2)."')";
        } else {
            $data["DATA_DATE"][FUNC] = "CAST(NULL as date)";
        }

        $data["PROCESS1_UPDATED"][FUNC] = "SYSDATE()";
        $data["PROCESS1_STS"][TEXT]     = "0";
        $data["SCHOOL_CODE"][TEXT]                      = $data_arr["SCHOOL_CODE"];
        $data["COURSE_CODE"][TEXT]                      = $data_arr["COURSE_CODE"];
        $data["SCHREGNO"][TEXT]                         = $data_arr["SCHREGNO"];

        if ( strlen($data_arr["BIRTHDAY"]) > 0) {
            $data["BIRTHDAY"][FUNC] = "date('".substr($data_arr["BIRTHDAY"],0,4)."-".substr($data_arr["BIRTHDAY"],4,2)."-".substr($data_arr["BIRTHDAY"],6,2)."')";
        } else {
            $data["BIRTHDAY"][FUNC] = "CAST(NULL as date)";
        }

        $data["NAME_KANA"][TEXT]                        = mb_convert_kana($data_arr["NAME_KANA"], "KV");
        $data["NAME"][TEXT]                             = mb_convert_kana($data_arr["NAME"], "KV");
        $data["PAYER_NAME_KANA"][TEXT]                  = mb_convert_kana($data_arr["PAYER_NAME_KANA"], "KV");
        $data["PAYER_NAME"][TEXT]                       = mb_convert_kana($data_arr["PAYER_NAME"], "KV");
        $data["PAYER_TELNO"][TEXT]                      = $data_arr["PAYER_TELNO"];
        $data["BANKCD"][TEXT]                           = $data_arr["BANKCD"];
        $data["BRANCHCD"][TEXT]                         = $data_arr["BRANCHCD"];
        $data["BANKNAME_KANA"][TEXT]                    = mb_convert_kana($data_arr["BANKNAME_KANA"], "kV");
        $data["BRANCHNAME_KANA"][TEXT]                  = mb_convert_kana($data_arr["BRANCHNAME_KANA"], "kV");
        $data["DEPOSIT_ITEM"][TEXT]                     = $data_arr["DEPOSIT_ITEM"];
        $data["ACCOUNTNO"][TEXT]                        = $data_arr["ACCOUNTNO"];
        $data["ACCOUNTNAME"][TEXT]                      = mb_convert_kana($data_arr["ACCOUNTNAME"], "kV");
        $data["EXPENSE01_PAID_MONEY"][NUMBER]           = $data_arr["EXPENSE01_PAID_MONEY"];
        $data["EXPENSE01_NECE_MONEY"][NUMBER]           = $data_arr["EXPENSE01_NECE_MONEY"];
        $data["EXPENSE02_PAID_MONEY"][NUMBER]           = $data_arr["EXPENSE02_PAID_MONEY"];
        $data["EXPENSE02_NECE_MONEY"][NUMBER]           = $data_arr["EXPENSE02_NECE_MONEY"];
        $data["EXPENSE03_PAID_MONEY"][NUMBER]           = $data_arr["EXPENSE03_PAID_MONEY"];
        $data["EXPENSE03_NECE_MONEY"][NUMBER]           = $data_arr["EXPENSE03_NECE_MONEY"];
        $data["EXPENSE04_PAID_MONEY"][NUMBER]           = $data_arr["EXPENSE04_PAID_MONEY"];
        $data["EXPENSE04_NECE_MONEY"][NUMBER]           = $data_arr["EXPENSE04_NECE_MONEY"];
        $data["EXPENSE05_PAID_MONEY"][NUMBER]           = $data_arr["EXPENSE05_PAID_MONEY"];
        $data["EXPENSE05_NECE_MONEY"][NUMBER]           = $data_arr["EXPENSE05_NECE_MONEY"];
        $data["EXPENSE06_PAID_MONEY"][NUMBER]           = $data_arr["EXPENSE06_PAID_MONEY"];
        $data["EXPENSE06_NECE_MONEY"][NUMBER]           = $data_arr["EXPENSE06_NECE_MONEY"];
        if ($freshFlg) {
            $data["TERM01_EXPENSE01_MONEY_DUE"][NUMBER]     = "CAST(NULL AS integer)";
            $data["TERM01_EXPENSE02_MONEY_DUE"][NUMBER]     = "CAST(NULL AS integer)";
            $data["TERM01_EXPENSE03_MONEY_DUE"][NUMBER]     = "CAST(NULL AS integer)";
        }else {
            $data["TERM01_EXPENSE01_MONEY_DUE"][NUMBER]     = $data_arr["TERM01_EXPENSE01_MONEY_DUE"];
            $data["TERM01_EXPENSE02_MONEY_DUE"][NUMBER]     = $data_arr["TERM01_EXPENSE02_MONEY_DUE"];
            $data["TERM01_EXPENSE03_MONEY_DUE"][NUMBER]     = $data_arr["TERM01_EXPENSE03_MONEY_DUE"];
        }
        $data["TERM01_EXPENSE04_MONEY_DUE"][NUMBER]     = $data_arr["TERM01_EXPENSE04_MONEY_DUE"];
        $data["TERM01_EXPENSE05_MONEY_DUE"][NUMBER]     = $data_arr["TERM01_EXPENSE05_MONEY_DUE"];
        $data["TERM01_EXPENSE06_MONEY_DUE"][NUMBER]     = $data_arr["TERM01_EXPENSE06_MONEY_DUE"];
        $data["TERM01_SUM_MONEY_DUE"][NUMBER]           = $data_arr["TERM01_SUM_MONEY_DUE"];
        $data["TERM01_FLAG01"][TEXT]                    = $data_arr["TERM01_FLAG01"];
        $data["TERM01_FLAG02"][TEXT]                    = $data_arr["TERM01_FLAG02"];
        $data["TERM01_FLAG03"][TEXT]                    = $data_arr["TERM01_FLAG03"];
        $data["TERM01_FLAG04"][TEXT]                    = $data_arr["TERM01_FLAG04"];
        $data["TERM01_FLAG05"][TEXT]                    = $data_arr["TERM01_FLAG05"];

        if ( strlen($data_arr["TERM01_PAID_MONEY_DATE"]) > 0) {
            $data["TERM01_PAID_MONEY_DATE"][FUNC] = "date('".substr($data_arr["TERM01_PAID_MONEY_DATE"],0,4)."-".substr($data_arr["TERM01_PAID_MONEY_DATE"],4,2)."-".substr($data_arr["TERM01_PAID_MONEY_DATE"],6,2)."')";
        } else {
            $data["TERM01_PAID_MONEY_DATE"][FUNC] = "CAST(NULL as date)";
        }
        if ( strlen($data_arr["TERM01_FURI_MONEY_DATE"]) > 0) {
            $data["TERM01_FURI_MONEY_DATE"][FUNC] = "date('".substr($data_arr["TERM01_FURI_MONEY_DATE"],0,4)."-".substr($data_arr["TERM01_FURI_MONEY_DATE"],4,2)."-".substr($data_arr["TERM01_FURI_MONEY_DATE"],6,2)."')";
        } else {
            $data["TERM01_FURI_MONEY_DATE"][FUNC] = "CAST(NULL as date)";
        }

        $data["TERM02_EXPENSE01_MONEY_DUE"][NUMBER]     = $data_arr["TERM02_EXPENSE01_MONEY_DUE"];
        if ($freshFlg) {
            $data["TERM02_EXPENSE02_MONEY_DUE"][NUMBER]     = "CAST(NULL AS integer)";
            $data["TERM02_EXPENSE03_MONEY_DUE"][NUMBER]     = "CAST(NULL AS integer)";
        }else {
            $data["TERM02_EXPENSE02_MONEY_DUE"][NUMBER]     = $data_arr["TERM02_EXPENSE02_MONEY_DUE"];
            $data["TERM02_EXPENSE03_MONEY_DUE"][NUMBER]     = $data_arr["TERM02_EXPENSE03_MONEY_DUE"];
        }
        $data["TERM02_EXPENSE04_MONEY_DUE"][NUMBER]     = $data_arr["TERM02_EXPENSE04_MONEY_DUE"];
        $data["TERM02_EXPENSE05_MONEY_DUE"][NUMBER]     = $data_arr["TERM02_EXPENSE05_MONEY_DUE"];
        $data["TERM02_EXPENSE06_MONEY_DUE"][NUMBER]     = $data_arr["TERM02_EXPENSE06_MONEY_DUE"];
        $data["TERM02_SUM_MONEY_DUE"][NUMBER]           = $data_arr["TERM02_SUM_MONEY_DUE"];
        $data["TERM02_FLAG01"][TEXT]                    = $data_arr["TERM02_FLAG01"];
        $data["TERM02_FLAG02"][TEXT]                    = $data_arr["TERM02_FLAG02"];
        $data["TERM02_FLAG03"][TEXT]                    = $data_arr["TERM02_FLAG03"];
        $data["TERM02_FLAG04"][TEXT]                    = $data_arr["TERM02_FLAG04"];
        $data["TERM02_FLAG05"][TEXT]                    = $data_arr["TERM02_FLAG05"];

        if ( strlen($data_arr["TERM02_PAID_MONEY_DATE"]) > 0) {
            $data["TERM02_PAID_MONEY_DATE"][FUNC] = "date('".substr($data_arr["TERM02_PAID_MONEY_DATE"],0,4)."-".substr($data_arr["TERM02_PAID_MONEY_DATE"],4,2)."-".substr($data_arr["TERM02_PAID_MONEY_DATE"],6,2)."')";
        } else {
            $data["TERM02_PAID_MONEY_DATE"][FUNC] = "CAST(NULL as date)";
        }
        if ( strlen($data_arr["TERM02_FURI_MONEY_DATE"]) > 0) {
            $data["TERM02_FURI_MONEY_DATE"][FUNC] = "date('".substr($data_arr["TERM02_FURI_MONEY_DATE"],0,4)."-".substr($data_arr["TERM02_FURI_MONEY_DATE"],4,2)."-".substr($data_arr["TERM02_FURI_MONEY_DATE"],6,2)."')";
        } else {
            $data["TERM02_FURI_MONEY_DATE"][FUNC] = "CAST(NULL as date)";
        }

        $data["TERM03_EXPENSE01_MONEY_DUE"][NUMBER]     = $data_arr["TERM03_EXPENSE01_MONEY_DUE"];
        if ($freshFlg) {
            $data["TERM03_EXPENSE02_MONEY_DUE"][NUMBER]     = "CAST(NULL AS integer)";
            $data["TERM03_EXPENSE03_MONEY_DUE"][NUMBER]     = "CAST(NULL AS integer)";
        }else {
            $data["TERM03_EXPENSE02_MONEY_DUE"][NUMBER]     = $data_arr["TERM03_EXPENSE02_MONEY_DUE"];
            $data["TERM03_EXPENSE03_MONEY_DUE"][NUMBER]     = $data_arr["TERM03_EXPENSE03_MONEY_DUE"];
        }
        $data["TERM03_EXPENSE04_MONEY_DUE"][NUMBER]     = $data_arr["TERM03_EXPENSE04_MONEY_DUE"];
        $data["TERM03_EXPENSE05_MONEY_DUE"][NUMBER]     = $data_arr["TERM03_EXPENSE05_MONEY_DUE"];
        $data["TERM03_EXPENSE06_MONEY_DUE"][NUMBER]     = $data_arr["TERM03_EXPENSE06_MONEY_DUE"];
        $data["TERM03_SUM_MONEY_DUE"][NUMBER]           = $data_arr["TERM03_SUM_MONEY_DUE"];
        $data["TERM03_FLAG01"][TEXT]                    = $data_arr["TERM03_FLAG01"];
        $data["TERM03_FLAG02"][TEXT]                    = $data_arr["TERM03_FLAG02"];
        $data["TERM03_FLAG03"][TEXT]                    = $data_arr["TERM03_FLAG03"];
        $data["TERM03_FLAG04"][TEXT]                    = $data_arr["TERM03_FLAG04"];
        $data["TERM03_FLAG05"][TEXT]                    = $data_arr["TERM03_FLAG05"];

        if ( strlen($data_arr["TERM03_PAID_MONEY_DATE"]) > 0) {
            $data["TERM03_PAID_MONEY_DATE"][FUNC] = "date('".substr($data_arr["TERM03_PAID_MONEY_DATE"],0,4)."-".substr($data_arr["TERM03_PAID_MONEY_DATE"],4,2)."-".substr($data_arr["TERM03_PAID_MONEY_DATE"],6,2)."')";
        } else {
            $data["TERM03_PAID_MONEY_DATE"][FUNC] = "CAST(NULL as date)";
        }
        if ( strlen($data_arr["TERM03_FURI_MONEY_DATE"]) > 0) {
            $data["TERM03_FURI_MONEY_DATE"][FUNC] = "date('".substr($data_arr["TERM03_FURI_MONEY_DATE"],0,4)."-".substr($data_arr["TERM03_FURI_MONEY_DATE"],4,2)."-".substr($data_arr["TERM03_FURI_MONEY_DATE"],6,2)."')";
        } else {
            $data["TERM03_FURI_MONEY_DATE"][FUNC] = "CAST(NULL as date)";
        }

        $data["TERM04_EXPENSE01_MONEY_DUE"][NUMBER]     = $data_arr["TERM04_EXPENSE01_MONEY_DUE"];
        if ($freshFlg) {
            $data["TERM04_EXPENSE02_MONEY_DUE"][NUMBER]     = "CAST(NULL AS integer)";
            $data["TERM04_EXPENSE03_MONEY_DUE"][NUMBER]     = "CAST(NULL AS integer)";
        }else {
            $data["TERM04_EXPENSE02_MONEY_DUE"][NUMBER]     = $data_arr["TERM04_EXPENSE02_MONEY_DUE"];
            $data["TERM04_EXPENSE03_MONEY_DUE"][NUMBER]     = $data_arr["TERM04_EXPENSE03_MONEY_DUE"];
        }
        $data["TERM04_EXPENSE04_MONEY_DUE"][NUMBER]     = $data_arr["TERM04_EXPENSE04_MONEY_DUE"];
        $data["TERM04_EXPENSE05_MONEY_DUE"][NUMBER]     = $data_arr["TERM04_EXPENSE05_MONEY_DUE"];
        $data["TERM04_EXPENSE06_MONEY_DUE"][NUMBER]     = $data_arr["TERM04_EXPENSE06_MONEY_DUE"];
        $data["TERM04_SUM_MONEY_DUE"][NUMBER]           = $data_arr["TERM04_SUM_MONEY_DUE"];
        $data["TERM04_FLAG01"][TEXT]                    = $data_arr["TERM04_FLAG01"];
        $data["TERM04_FLAG02"][TEXT]                    = $data_arr["TERM04_FLAG02"];
        $data["TERM04_FLAG03"][TEXT]                    = $data_arr["TERM04_FLAG03"];
        $data["TERM04_FLAG04"][TEXT]                    = $data_arr["TERM04_FLAG04"];
        $data["TERM04_FLAG05"][TEXT]                    = $data_arr["TERM04_FLAG05"];

        if ( strlen($data_arr["TERM04_PAID_MONEY_DATE"]) > 0) {
            $data["TERM04_PAID_MONEY_DATE"][FUNC] = "date('".substr($data_arr["TERM04_PAID_MONEY_DATE"],0,4)."-".substr($data_arr["TERM04_PAID_MONEY_DATE"],4,2)."-".substr($data_arr["TERM04_PAID_MONEY_DATE"],6,2)."')";
        } else {
            $data["TERM04_PAID_MONEY_DATE"][FUNC] = "CAST(NULL as date)";
        }
        if ( strlen($data_arr["TERM04_FURI_MONEY_DATE"]) > 0) {
            $data["TERM04_FURI_MONEY_DATE"][FUNC] = "date('".substr($data_arr["TERM04_FURI_MONEY_DATE"],0,4)."-".substr($data_arr["TERM04_FURI_MONEY_DATE"],4,2)."-".substr($data_arr["TERM04_FURI_MONEY_DATE"],6,2)."')";
        } else {
            $data["TERM04_FURI_MONEY_DATE"][FUNC] = "CAST(NULL as date)";
        }

        $data["TERM05_EXPENSE01_MONEY_DUE"][NUMBER]     = $data_arr["TERM05_EXPENSE01_MONEY_DUE"];
        if ($freshFlg) {
            $data["TERM05_EXPENSE02_MONEY_DUE"][NUMBER]     = "CAST(NULL AS integer)";
            $data["TERM05_EXPENSE03_MONEY_DUE"][NUMBER]     = "CAST(NULL AS integer)";
        }else {
            $data["TERM05_EXPENSE02_MONEY_DUE"][NUMBER]     = $data_arr["TERM05_EXPENSE02_MONEY_DUE"];
            $data["TERM05_EXPENSE03_MONEY_DUE"][NUMBER]     = $data_arr["TERM05_EXPENSE03_MONEY_DUE"];
        }
        $data["TERM05_EXPENSE04_MONEY_DUE"][NUMBER]     = $data_arr["TERM05_EXPENSE04_MONEY_DUE"];
        $data["TERM05_EXPENSE05_MONEY_DUE"][NUMBER]     = $data_arr["TERM05_EXPENSE05_MONEY_DUE"];
        $data["TERM05_EXPENSE06_MONEY_DUE"][NUMBER]     = $data_arr["TERM05_EXPENSE06_MONEY_DUE"];
        $data["TERM05_SUM_MONEY_DUE"][NUMBER]           = $data_arr["TERM05_SUM_MONEY_DUE"];
        $data["TERM05_FLAG01"][TEXT]                    = $data_arr["TERM05_FLAG01"];
        $data["TERM05_FLAG02"][TEXT]                    = $data_arr["TERM05_FLAG02"];
        $data["TERM05_FLAG03"][TEXT]                    = $data_arr["TERM05_FLAG03"];
        $data["TERM05_FLAG04"][TEXT]                    = $data_arr["TERM05_FLAG04"];
        $data["TERM05_FLAG05"][TEXT]                    = $data_arr["TERM05_FLAG05"];

        if ( strlen($data_arr["TERM05_PAID_MONEY_DATE"]) > 0) {
            $data["TERM05_PAID_MONEY_DATE"][FUNC] = "date('".substr($data_arr["TERM05_PAID_MONEY_DATE"],0,4)."-".substr($data_arr["TERM05_PAID_MONEY_DATE"],4,2)."-".substr($data_arr["TERM05_PAID_MONEY_DATE"],6,2)."')";
        } else {
            $data["TERM05_PAID_MONEY_DATE"][FUNC] = "CAST(NULL as date)";
        }
        if ( strlen($data_arr["TERM05_FURI_MONEY_DATE"]) > 0) {
            $data["TERM05_FURI_MONEY_DATE"][FUNC] = "date('".substr($data_arr["TERM05_FURI_MONEY_DATE"],0,4)."-".substr($data_arr["TERM05_FURI_MONEY_DATE"],4,2)."-".substr($data_arr["TERM05_FURI_MONEY_DATE"],6,2)."')";
        } else {
            $data["TERM05_FURI_MONEY_DATE"][FUNC] = "CAST(NULL as date)";
        }

        $data["TERM06_EXPENSE01_MONEY_DUE"][NUMBER]     = $data_arr["TERM06_EXPENSE01_MONEY_DUE"];
        if ($freshFlg) {
            $data["TERM06_EXPENSE02_MONEY_DUE"][NUMBER]     = "CAST(NULL AS integer)";
            $data["TERM06_EXPENSE03_MONEY_DUE"][NUMBER]     = "CAST(NULL AS integer)";
        }else {
            $data["TERM06_EXPENSE02_MONEY_DUE"][NUMBER]     = $data_arr["TERM06_EXPENSE02_MONEY_DUE"];
            $data["TERM06_EXPENSE03_MONEY_DUE"][NUMBER]     = $data_arr["TERM06_EXPENSE03_MONEY_DUE"];
        }
        $data["TERM06_EXPENSE04_MONEY_DUE"][NUMBER]     = $data_arr["TERM06_EXPENSE04_MONEY_DUE"];
        $data["TERM06_EXPENSE05_MONEY_DUE"][NUMBER]     = $data_arr["TERM06_EXPENSE05_MONEY_DUE"];
        $data["TERM06_EXPENSE06_MONEY_DUE"][NUMBER]     = $data_arr["TERM06_EXPENSE06_MONEY_DUE"];
        $data["TERM06_SUM_MONEY_DUE"][NUMBER]           = $data_arr["TERM06_SUM_MONEY_DUE"];
        $data["TERM06_FLAG01"][TEXT]                    = $data_arr["TERM06_FLAG01"];
        $data["TERM06_FLAG02"][TEXT]                    = $data_arr["TERM06_FLAG02"];
        $data["TERM06_FLAG03"][TEXT]                    = $data_arr["TERM06_FLAG03"];
        $data["TERM06_FLAG04"][TEXT]                    = $data_arr["TERM06_FLAG04"];
        $data["TERM06_FLAG05"][TEXT]                    = $data_arr["TERM06_FLAG05"];

        if ( strlen($data_arr["TERM06_PAID_MONEY_DATE"]) > 0) {
            $data["TERM06_PAID_MONEY_DATE"][FUNC] = "date('".substr($data_arr["TERM06_PAID_MONEY_DATE"],0,4)."-".substr($data_arr["TERM06_PAID_MONEY_DATE"],4,2)."-".substr($data_arr["TERM06_PAID_MONEY_DATE"],6,2)."')";
        } else {
            $data["TERM06_PAID_MONEY_DATE"][FUNC] = "CAST(NULL as date)";
        }
        if ( strlen($data_arr["TERM06_FURI_MONEY_DATE"]) > 0) {
            $data["TERM06_FURI_MONEY_DATE"][FUNC] = "date('".substr($data_arr["TERM06_FURI_MONEY_DATE"],0,4)."-".substr($data_arr["TERM06_FURI_MONEY_DATE"],4,2)."-".substr($data_arr["TERM06_FURI_MONEY_DATE"],6,2)."')";
        } else {
            $data["TERM06_FURI_MONEY_DATE"][FUNC] = "CAST(NULL as date)";
        }

        $data["TERM07_EXPENSE01_MONEY_DUE"][NUMBER]     = $data_arr["TERM07_EXPENSE01_MONEY_DUE"];
        if ($freshFlg) {
            $data["TERM07_EXPENSE02_MONEY_DUE"][NUMBER]     = "CAST(NULL AS integer)";
            $data["TERM07_EXPENSE03_MONEY_DUE"][NUMBER]     = "CAST(NULL AS integer)";
        }else {
            $data["TERM07_EXPENSE02_MONEY_DUE"][NUMBER]     = $data_arr["TERM07_EXPENSE02_MONEY_DUE"];
            $data["TERM07_EXPENSE03_MONEY_DUE"][NUMBER]     = $data_arr["TERM07_EXPENSE03_MONEY_DUE"];
        }
        $data["TERM07_EXPENSE04_MONEY_DUE"][NUMBER]     = $data_arr["TERM07_EXPENSE04_MONEY_DUE"];
        $data["TERM07_EXPENSE05_MONEY_DUE"][NUMBER]     = $data_arr["TERM07_EXPENSE05_MONEY_DUE"];
        $data["TERM07_EXPENSE06_MONEY_DUE"][NUMBER]     = $data_arr["TERM07_EXPENSE06_MONEY_DUE"];
        $data["TERM07_SUM_MONEY_DUE"][NUMBER]           = $data_arr["TERM07_SUM_MONEY_DUE"];
        $data["TERM07_FLAG01"][TEXT]                    = $data_arr["TERM07_FLAG01"];
        $data["TERM07_FLAG02"][TEXT]                    = $data_arr["TERM07_FLAG02"];
        $data["TERM07_FLAG03"][TEXT]                    = $data_arr["TERM07_FLAG03"];
        $data["TERM07_FLAG04"][TEXT]                    = $data_arr["TERM07_FLAG04"];
        $data["TERM07_FLAG05"][TEXT]                    = $data_arr["TERM07_FLAG05"];

        if ( strlen($data_arr["TERM07_PAID_MONEY_DATE"]) > 0) {
            $data["TERM07_PAID_MONEY_DATE"][FUNC] = "date('".substr($data_arr["TERM07_PAID_MONEY_DATE"],0,4)."-".substr($data_arr["TERM07_PAID_MONEY_DATE"],4,2)."-".substr($data_arr["TERM07_PAID_MONEY_DATE"],6,2)."')";
        } else {
            $data["TERM07_PAID_MONEY_DATE"][FUNC] = "CAST(NULL as date)";
        }
        if ( strlen($data_arr["TERM07_FURI_MONEY_DATE"]) > 0) {
            $data["TERM07_FURI_MONEY_DATE"][FUNC] = "date('".substr($data_arr["TERM07_FURI_MONEY_DATE"],0,4)."-".substr($data_arr["TERM07_FURI_MONEY_DATE"],4,2)."-".substr($data_arr["TERM07_FURI_MONEY_DATE"],6,2)."')";
        } else {
            $data["TERM07_FURI_MONEY_DATE"][FUNC] = "CAST(NULL as date)";
        }

        $data["TERM08_EXPENSE01_MONEY_DUE"][NUMBER]     = $data_arr["TERM08_EXPENSE01_MONEY_DUE"];
        if ($freshFlg) {
            $data["TERM08_EXPENSE02_MONEY_DUE"][NUMBER]     = "CAST(NULL AS integer)";
            $data["TERM08_EXPENSE03_MONEY_DUE"][NUMBER]     = "CAST(NULL AS integer)";
        }else {
            $data["TERM08_EXPENSE02_MONEY_DUE"][NUMBER]     = $data_arr["TERM08_EXPENSE02_MONEY_DUE"];
            $data["TERM08_EXPENSE03_MONEY_DUE"][NUMBER]     = $data_arr["TERM08_EXPENSE03_MONEY_DUE"];
        }
        $data["TERM08_EXPENSE04_MONEY_DUE"][NUMBER]     = $data_arr["TERM08_EXPENSE04_MONEY_DUE"];
        $data["TERM08_EXPENSE05_MONEY_DUE"][NUMBER]     = $data_arr["TERM08_EXPENSE05_MONEY_DUE"];
        $data["TERM08_EXPENSE06_MONEY_DUE"][NUMBER]     = $data_arr["TERM08_EXPENSE06_MONEY_DUE"];
        $data["TERM08_SUM_MONEY_DUE"][NUMBER]           = $data_arr["TERM08_SUM_MONEY_DUE"];
        $data["TERM08_FLAG01"][TEXT]                    = $data_arr["TERM08_FLAG01"];
        $data["TERM08_FLAG02"][TEXT]                    = $data_arr["TERM08_FLAG02"];
        $data["TERM08_FLAG03"][TEXT]                    = $data_arr["TERM08_FLAG03"];
        $data["TERM08_FLAG04"][TEXT]                    = $data_arr["TERM08_FLAG04"];
        $data["TERM08_FLAG05"][TEXT]                    = $data_arr["TERM08_FLAG05"];

        if ( strlen($data_arr["TERM08_PAID_MONEY_DATE"]) > 0) {
            $data["TERM08_PAID_MONEY_DATE"][FUNC] = "date('".substr($data_arr["TERM08_PAID_MONEY_DATE"],0,4)."-".substr($data_arr["TERM08_PAID_MONEY_DATE"],4,2)."-".substr($data_arr["TERM08_PAID_MONEY_DATE"],6,2)."')";
        } else {
            $data["TERM08_PAID_MONEY_DATE"][FUNC] = "CAST(NULL as date)";
        }
        if ( strlen($data_arr["TERM08_FURI_MONEY_DATE"]) > 0) {
            $data["TERM08_FURI_MONEY_DATE"][FUNC] = "date('".substr($data_arr["TERM08_FURI_MONEY_DATE"],0,4)."-".substr($data_arr["TERM08_FURI_MONEY_DATE"],4,2)."-".substr($data_arr["TERM08_FURI_MONEY_DATE"],6,2)."')";
        } else {
            $data["TERM08_FURI_MONEY_DATE"][FUNC] = "CAST(NULL as date)";
        }

        $data["TERM09_EXPENSE01_MONEY_DUE"][NUMBER]     = $data_arr["TERM09_EXPENSE01_MONEY_DUE"];
        if ($freshFlg) {
            $data["TERM09_EXPENSE02_MONEY_DUE"][NUMBER]     = "CAST(NULL AS integer)";
            $data["TERM09_EXPENSE03_MONEY_DUE"][NUMBER]     = "CAST(NULL AS integer)";
        }else {
            $data["TERM09_EXPENSE02_MONEY_DUE"][NUMBER]     = $data_arr["TERM09_EXPENSE02_MONEY_DUE"];
            $data["TERM09_EXPENSE03_MONEY_DUE"][NUMBER]     = $data_arr["TERM09_EXPENSE03_MONEY_DUE"];
        }
        $data["TERM09_EXPENSE04_MONEY_DUE"][NUMBER]     = $data_arr["TERM09_EXPENSE04_MONEY_DUE"];
        $data["TERM09_EXPENSE05_MONEY_DUE"][NUMBER]     = $data_arr["TERM09_EXPENSE05_MONEY_DUE"];
        $data["TERM09_EXPENSE06_MONEY_DUE"][NUMBER]     = $data_arr["TERM09_EXPENSE06_MONEY_DUE"];
        $data["TERM09_SUM_MONEY_DUE"][NUMBER]           = $data_arr["TERM09_SUM_MONEY_DUE"];
        $data["TERM09_FLAG01"][TEXT]                    = $data_arr["TERM09_FLAG01"];
        $data["TERM09_FLAG02"][TEXT]                    = $data_arr["TERM09_FLAG02"];
        $data["TERM09_FLAG03"][TEXT]                    = $data_arr["TERM09_FLAG03"];
        $data["TERM09_FLAG04"][TEXT]                    = $data_arr["TERM09_FLAG04"];
        $data["TERM09_FLAG05"][TEXT]                    = $data_arr["TERM09_FLAG05"];

        if ( strlen($data_arr["TERM09_PAID_MONEY_DATE"]) > 0) {
            $data["TERM09_PAID_MONEY_DATE"][FUNC] = "date('".substr($data_arr["TERM09_PAID_MONEY_DATE"],0,4)."-".substr($data_arr["TERM09_PAID_MONEY_DATE"],4,2)."-".substr($data_arr["TERM09_PAID_MONEY_DATE"],6,2)."')";
        } else {
            $data["TERM09_PAID_MONEY_DATE"][FUNC] = "CAST(NULL as date)";
        }
        if ( strlen($data_arr["TERM09_FURI_MONEY_DATE"]) > 0) {
            $data["TERM09_FURI_MONEY_DATE"][FUNC] = "date('".substr($data_arr["TERM09_FURI_MONEY_DATE"],0,4)."-".substr($data_arr["TERM09_FURI_MONEY_DATE"],4,2)."-".substr($data_arr["TERM09_FURI_MONEY_DATE"],6,2)."')";
        } else {
            $data["TERM09_FURI_MONEY_DATE"][FUNC] = "CAST(NULL as date)";
        }

        $data["TERM10_EXPENSE01_MONEY_DUE"][NUMBER]     = $data_arr["TERM10_EXPENSE01_MONEY_DUE"];
        if ($freshFlg) {
            $data["TERM10_EXPENSE02_MONEY_DUE"][NUMBER]     = "CAST(NULL AS integer)";
            $data["TERM10_EXPENSE03_MONEY_DUE"][NUMBER]     = "CAST(NULL AS integer)";
        }else {
            $data["TERM10_EXPENSE02_MONEY_DUE"][NUMBER]     = $data_arr["TERM10_EXPENSE02_MONEY_DUE"];
            $data["TERM10_EXPENSE03_MONEY_DUE"][NUMBER]     = $data_arr["TERM10_EXPENSE03_MONEY_DUE"];
        }
        $data["TERM10_EXPENSE04_MONEY_DUE"][NUMBER]     = $data_arr["TERM10_EXPENSE04_MONEY_DUE"];
        $data["TERM10_EXPENSE05_MONEY_DUE"][NUMBER]     = $data_arr["TERM10_EXPENSE05_MONEY_DUE"];
        $data["TERM10_EXPENSE06_MONEY_DUE"][NUMBER]     = $data_arr["TERM10_EXPENSE06_MONEY_DUE"];
        $data["TERM10_SUM_MONEY_DUE"][NUMBER]           = $data_arr["TERM10_SUM_MONEY_DUE"];
        $data["TERM10_FLAG01"][TEXT]                    = $data_arr["TERM10_FLAG01"];
        $data["TERM10_FLAG02"][TEXT]                    = $data_arr["TERM10_FLAG02"];
        $data["TERM10_FLAG03"][TEXT]                    = $data_arr["TERM10_FLAG03"];
        $data["TERM10_FLAG04"][TEXT]                    = $data_arr["TERM10_FLAG04"];
        $data["TERM10_FLAG05"][TEXT]                    = $data_arr["TERM10_FLAG05"];

        if ( strlen($data_arr["TERM10_PAID_MONEY_DATE"]) > 0) {
            $data["TERM10_PAID_MONEY_DATE"][FUNC] = "date('".substr($data_arr["TERM10_PAID_MONEY_DATE"],0,4)."-".substr($data_arr["TERM10_PAID_MONEY_DATE"],4,2)."-".substr($data_arr["TERM10_PAID_MONEY_DATE"],6,2)."')";
        } else {
            $data["TERM10_PAID_MONEY_DATE"][FUNC] = "CAST(NULL as date)";
        }
        if ( strlen($data_arr["TERM10_FURI_MONEY_DATE"]) > 0) {
            $data["TERM10_FURI_MONEY_DATE"][FUNC] = "date('".substr($data_arr["TERM10_FURI_MONEY_DATE"],0,4)."-".substr($data_arr["TERM10_FURI_MONEY_DATE"],4,2)."-".substr($data_arr["TERM10_FURI_MONEY_DATE"],6,2)."')";
        } else {
            $data["TERM10_FURI_MONEY_DATE"][FUNC] = "CAST(NULL as date)";
        }

        $data["TERM11_EXPENSE01_MONEY_DUE"][NUMBER]     = $data_arr["TERM11_EXPENSE01_MONEY_DUE"];
        if ($freshFlg) {
            $data["TERM11_EXPENSE02_MONEY_DUE"][NUMBER]     = "CAST(NULL AS integer)";
            $data["TERM11_EXPENSE03_MONEY_DUE"][NUMBER]     = "CAST(NULL AS integer)";
        }else {
            $data["TERM11_EXPENSE02_MONEY_DUE"][NUMBER]     = $data_arr["TERM11_EXPENSE02_MONEY_DUE"];
            $data["TERM11_EXPENSE03_MONEY_DUE"][NUMBER]     = $data_arr["TERM11_EXPENSE03_MONEY_DUE"];
        }
        $data["TERM11_EXPENSE04_MONEY_DUE"][NUMBER]     = $data_arr["TERM11_EXPENSE04_MONEY_DUE"];
        $data["TERM11_EXPENSE05_MONEY_DUE"][NUMBER]     = $data_arr["TERM11_EXPENSE05_MONEY_DUE"];
        $data["TERM11_EXPENSE06_MONEY_DUE"][NUMBER]     = $data_arr["TERM11_EXPENSE06_MONEY_DUE"];
        $data["TERM11_SUM_MONEY_DUE"][NUMBER]           = $data_arr["TERM11_SUM_MONEY_DUE"];
        $data["TERM11_FLAG01"][TEXT]                    = $data_arr["TERM11_FLAG01"];
        $data["TERM11_FLAG02"][TEXT]                    = $data_arr["TERM11_FLAG02"];
        $data["TERM11_FLAG03"][TEXT]                    = $data_arr["TERM11_FLAG03"];
        $data["TERM11_FLAG04"][TEXT]                    = $data_arr["TERM11_FLAG04"];
        $data["TERM11_FLAG05"][TEXT]                    = $data_arr["TERM11_FLAG05"];

        if ( strlen($data_arr["TERM11_PAID_MONEY_DATE"]) > 0) {
            $data["TERM11_PAID_MONEY_DATE"][FUNC] = "date('".substr($data_arr["TERM11_PAID_MONEY_DATE"],0,4)."-".substr($data_arr["TERM11_PAID_MONEY_DATE"],4,2)."-".substr($data_arr["TERM11_PAID_MONEY_DATE"],6,2)."')";
        } else {
            $data["TERM11_PAID_MONEY_DATE"][FUNC] = "CAST(NULL as date)";
        }
        if ( strlen($data_arr["TERM11_FURI_MONEY_DATE"]) > 0) {
            $data["TERM11_FURI_MONEY_DATE"][FUNC] = "date('".substr($data_arr["TERM11_FURI_MONEY_DATE"],0,4)."-".substr($data_arr["TERM11_FURI_MONEY_DATE"],4,2)."-".substr($data_arr["TERM11_FURI_MONEY_DATE"],6,2)."')";
        } else {
            $data["TERM11_FURI_MONEY_DATE"][FUNC] = "CAST(NULL as date)";
        }

        $data["TERM12_EXPENSE01_MONEY_DUE"][NUMBER]     = $data_arr["TERM12_EXPENSE01_MONEY_DUE"];
        if ($freshFlg) {
            $data["TERM12_EXPENSE02_MONEY_DUE"][NUMBER]     = "CAST(NULL AS integer)";
            $data["TERM12_EXPENSE03_MONEY_DUE"][NUMBER]     = "CAST(NULL AS integer)";
        }else {
            $data["TERM12_EXPENSE02_MONEY_DUE"][NUMBER]     = $data_arr["TERM12_EXPENSE02_MONEY_DUE"];
            $data["TERM12_EXPENSE03_MONEY_DUE"][NUMBER]     = $data_arr["TERM12_EXPENSE03_MONEY_DUE"];
        }
        $data["TERM12_EXPENSE04_MONEY_DUE"][NUMBER]     = $data_arr["TERM12_EXPENSE04_MONEY_DUE"];
        $data["TERM12_EXPENSE05_MONEY_DUE"][NUMBER]     = $data_arr["TERM12_EXPENSE05_MONEY_DUE"];
        $data["TERM12_EXPENSE06_MONEY_DUE"][NUMBER]     = $data_arr["TERM12_EXPENSE06_MONEY_DUE"];
        $data["TERM12_SUM_MONEY_DUE"][NUMBER]           = $data_arr["TERM12_SUM_MONEY_DUE"];
        $data["TERM12_FLAG01"][TEXT]                    = $data_arr["TERM12_FLAG01"];
        $data["TERM12_FLAG02"][TEXT]                    = $data_arr["TERM12_FLAG02"];
        $data["TERM12_FLAG03"][TEXT]                    = $data_arr["TERM12_FLAG03"];
        $data["TERM12_FLAG04"][TEXT]                    = $data_arr["TERM12_FLAG04"];
        $data["TERM12_FLAG05"][TEXT]                    = $data_arr["TERM12_FLAG05"];

        if ( strlen($data_arr["TERM12_PAID_MONEY_DATE"]) > 0) {
            $data["TERM12_PAID_MONEY_DATE"][FUNC] = "date('".substr($data_arr["TERM12_PAID_MONEY_DATE"],0,4)."-".substr($data_arr["TERM12_PAID_MONEY_DATE"],4,2)."-".substr($data_arr["TERM12_PAID_MONEY_DATE"],6,2)."')";
        } else {
            $data["TERM12_PAID_MONEY_DATE"][FUNC] = "CAST(NULL as date)";
        }
        if ( strlen($data_arr["TERM12_FURI_MONEY_DATE"]) > 0) {
            $data["TERM12_FURI_MONEY_DATE"][FUNC] = "date('".substr($data_arr["TERM12_FURI_MONEY_DATE"],0,4)."-".substr($data_arr["TERM12_FURI_MONEY_DATE"],4,2)."-".substr($data_arr["TERM12_FURI_MONEY_DATE"],6,2)."')";
        } else {
            $data["TERM12_FURI_MONEY_DATE"][FUNC] = "CAST(NULL as date)";
        }

        if ( strlen($data_arr["EXTR_EXPENSE01_TRANS_SDATE"]) > 0) {

            $data["EXTR_EXPENSE01_TRANS_SDATE"][FUNC] = "date('".substr($data_arr["EXTR_EXPENSE01_TRANS_SDATE"],0,4)."-".substr($data_arr["EXTR_EXPENSE01_TRANS_SDATE"],4,2)."-".substr($data_arr["EXTR_EXPENSE01_TRANS_SDATE"],6,2)."')";
        } else {
            $data["EXTR_EXPENSE01_TRANS_SDATE"][FUNC] = "CAST(NULL as date)";
        }

        $data["EXTR_EXPENSE01_MONEY"][NUMBER]           = $data_arr["EXTR_EXPENSE01_MONEY"];
        $data["EXTR_EXPENSE01_FLAG01"][TEXT]            = $data_arr["EXTR_EXPENSE01_FLAG01"];
        $data["EXTR_EXPENSE01_FLAG02"][TEXT]            = $data_arr["EXTR_EXPENSE01_FLAG02"];
        $data["EXTR_EXPENSE01_FLAG03"][TEXT]            = $data_arr["EXTR_EXPENSE01_FLAG03"];
        $data["EXTR_EXPENSE01_FLAG04"][TEXT]            = $data_arr["EXTR_EXPENSE01_FLAG04"];
        $data["EXTR_EXPENSE01_FLAG05"][TEXT]            = $data_arr["EXTR_EXPENSE01_FLAG05"];

        if ( strlen($data_arr["EXTR_EXPENSE01_PAID_MONEY_DATE"]) > 0) {
            $data["EXTR_EXPENSE01_PAID_MONEY_DATE"][FUNC] = "date('".substr($data_arr["EXTR_EXPENSE01_PAID_MONEY_DATE"],0,4)."-".substr($data_arr["EXTR_EXPENSE01_PAID_MONEY_DATE"],4,2)."-".substr($data_arr["EXTR_EXPENSE01_PAID_MONEY_DATE"],6,2)."')";

        } else {
            $data["EXTR_EXPENSE01_PAID_MONEY_DATE"][FUNC] = "CAST(NULL as date)";
        }

        if ( strlen($data_arr["EXTR_EXPENSE02_TRANS_SDATE"]) > 0) {
            $data["EXTR_EXPENSE02_TRANS_SDATE"][FUNC] = "date('".substr($data_arr["EXTR_EXPENSE02_TRANS_SDATE"],0,4)."-".substr($data_arr["EXTR_EXPENSE02_TRANS_SDATE"],4,2)."-".substr($data_arr["EXTR_EXPENSE02_TRANS_SDATE"],6,2)."')";
        } else {
            $data["EXTR_EXPENSE02_TRANS_SDATE"][FUNC] = "CAST(NULL as date)";
        }

        $data["EXTR_EXPENSE02_MONEY"][NUMBER]           = $data_arr["EXTR_EXPENSE02_MONEY"];
        $data["EXTR_EXPENSE02_FLAG01"][TEXT]            = $data_arr["EXTR_EXPENSE02_FLAG01"];
        $data["EXTR_EXPENSE02_FLAG02"][TEXT]            = $data_arr["EXTR_EXPENSE02_FLAG02"];
        $data["EXTR_EXPENSE02_FLAG03"][TEXT]            = $data_arr["EXTR_EXPENSE02_FLAG03"];
        $data["EXTR_EXPENSE02_FLAG04"][TEXT]            = $data_arr["EXTR_EXPENSE02_FLAG04"];
        $data["EXTR_EXPENSE02_FLAG05"][TEXT]            = $data_arr["EXTR_EXPENSE02_FLAG05"];

        if ( strlen($data_arr["EXTR_EXPENSE02_PAID_MONEY_DATE"]) > 0) {
            $data["EXTR_EXPENSE02_PAID_MONEY_DATE"][FUNC] = "date('".substr($data_arr["EXTR_EXPENSE02_PAID_MONEY_DATE"],0,4)."-".substr($data_arr["EXTR_EXPENSE02_PAID_MONEY_DATE"],4,2)."-".substr($data_arr["EXTR_EXPENSE02_PAID_MONEY_DATE"],6,2)."')";
        } else {
            $data["EXTR_EXPENSE02_PAID_MONEY_DATE"][FUNC] = "CAST(NULL as date)";
        }

        if ( strlen($data_arr["EXTR_EXPENSE03_TRANS_SDATE"]) > 0) {
            $data["EXTR_EXPENSE03_TRANS_SDATE"][FUNC] = "date('".substr($data_arr["EXTR_EXPENSE03_TRANS_SDATE"],0,4)."-".substr($data_arr["EXTR_EXPENSE03_TRANS_SDATE"],4,2)."-".substr($data_arr["EXTR_EXPENSE03_TRANS_SDATE"],6,2)."')";
        } else {
            $data["EXTR_EXPENSE03_TRANS_SDATE"][FUNC] = "CAST(NULL as date)";
        }

        $data["EXTR_EXPENSE03_MONEY"][NUMBER]           = $data_arr["EXTR_EXPENSE03_MONEY"];
        $data["EXTR_EXPENSE03_FLAG01"][TEXT]            = $data_arr["EXTR_EXPENSE03_FLAG01"];
        $data["EXTR_EXPENSE03_FLAG02"][TEXT]            = $data_arr["EXTR_EXPENSE03_FLAG02"];
        $data["EXTR_EXPENSE03_FLAG03"][TEXT]            = $data_arr["EXTR_EXPENSE03_FLAG03"];
        $data["EXTR_EXPENSE03_FLAG04"][TEXT]            = $data_arr["EXTR_EXPENSE03_FLAG04"];
        $data["EXTR_EXPENSE03_FLAG05"][TEXT]            = $data_arr["EXTR_EXPENSE03_FLAG05"];

        if ( strlen($data_arr["EXTR_EXPENSE03_PAID_MONEY_DATE"]) > 0) {
            $data["EXTR_EXPENSE03_PAID_MONEY_DATE"][FUNC] = "date('".substr($data_arr["EXTR_EXPENSE03_PAID_MONEY_DATE"],0,4)."-".substr($data_arr["EXTR_EXPENSE03_PAID_MONEY_DATE"],4,2)."-".substr($data_arr["EXTR_EXPENSE03_PAID_MONEY_DATE"],6,2)."')";
        } else {
            $data["EXTR_EXPENSE03_PAID_MONEY_DATE"][FUNC] = "CAST(NULL as date)";
        }

        if ( strlen($data_arr["EXTR_EXPENSE04_TRANS_SDATE"]) > 0) {
            $data["EXTR_EXPENSE04_TRANS_SDATE"][FUNC] = "date('".substr($data_arr["EXTR_EXPENSE04_TRANS_SDATE"],0,4)."-".substr($data_arr["EXTR_EXPENSE04_TRANS_SDATE"],4,2)."-".substr($data_arr["EXTR_EXPENSE04_TRANS_SDATE"],6,2)."')";
        } else {
            $data["EXTR_EXPENSE04_TRANS_SDATE"][FUNC] = "CAST(NULL as date)";
        }

        $data["EXTR_EXPENSE04_MONEY"][NUMBER]           = $data_arr["EXTR_EXPENSE04_MONEY"];
        $data["EXTR_EXPENSE04_FLAG01"][TEXT]            = $data_arr["EXTR_EXPENSE04_FLAG01"];
        $data["EXTR_EXPENSE04_FLAG02"][TEXT]            = $data_arr["EXTR_EXPENSE04_FLAG02"];
        $data["EXTR_EXPENSE04_FLAG03"][TEXT]            = $data_arr["EXTR_EXPENSE04_FLAG03"];
        $data["EXTR_EXPENSE04_FLAG04"][TEXT]            = $data_arr["EXTR_EXPENSE04_FLAG04"];
        $data["EXTR_EXPENSE04_FLAG05"][TEXT]            = $data_arr["EXTR_EXPENSE04_FLAG05"];

        if ( strlen($data_arr["EXTR_EXPENSE04_PAID_MONEY_DATE"]) > 0) {
            $data["EXTR_EXPENSE04_PAID_MONEY_DATE"][FUNC] = "date('".substr($data_arr["EXTR_EXPENSE04_PAID_MONEY_DATE"],0,4)."-".substr($data_arr["EXTR_EXPENSE04_PAID_MONEY_DATE"],4,2)."-".substr($data_arr["EXTR_EXPENSE04_PAID_MONEY_DATE"],6,2)."')";
        } else {
            $data["EXTR_EXPENSE04_PAID_MONEY_DATE"][FUNC] = "CAST(NULL as date)";
        }

        if ( strlen($data_arr["EXTR_EXPENSE04_PAID_MONEY_DATE"]) > 0) {
            $data["EXTR_EXPENSE05_TRANS_SDATE"][FUNC] = "date('".substr($data_arr["EXTR_EXPENSE05_TRANS_SDATE"],0,4)."-".substr($data_arr["EXTR_EXPENSE05_TRANS_SDATE"],4,2)."-".substr($data_arr["EXTR_EXPENSE05_TRANS_SDATE"],6,2)."')";
        } else {
            $data["EXTR_EXPENSE05_TRANS_SDATE"][FUNC] = "CAST(NULL as date)";
        }

        $data["EXTR_EXPENSE05_MONEY"][NUMBER]           = $data_arr["EXTR_EXPENSE05_MONEY"];
        $data["EXTR_EXPENSE05_FLAG01"][TEXT]            = $data_arr["EXTR_EXPENSE05_FLAG01"];
        $data["EXTR_EXPENSE05_FLAG02"][TEXT]            = $data_arr["EXTR_EXPENSE05_FLAG02"];
        $data["EXTR_EXPENSE05_FLAG03"][TEXT]            = $data_arr["EXTR_EXPENSE05_FLAG03"];
        $data["EXTR_EXPENSE05_FLAG04"][TEXT]            = $data_arr["EXTR_EXPENSE05_FLAG04"];
        $data["EXTR_EXPENSE05_FLAG05"][TEXT]            = $data_arr["EXTR_EXPENSE05_FLAG05"];

        if ( strlen($data_arr["EXTR_EXPENSE05_PAID_MONEY_DATE"]) > 0) {
            $data["EXTR_EXPENSE05_PAID_MONEY_DATE"][FUNC] = "date('".substr($data_arr["EXTR_EXPENSE05_PAID_MONEY_DATE"],0,4)."-".substr($data_arr["EXTR_EXPENSE05_PAID_MONEY_DATE"],4,2)."-".substr($data_arr["EXTR_EXPENSE05_PAID_MONEY_DATE"],6,2)."')";
        } else {
            $data["EXTR_EXPENSE05_PAID_MONEY_DATE"][FUNC] = "CAST(NULL as date)";
        }

        if ( strlen($data_arr["EXTR_EXPENSE06_TRANS_SDATE"]) > 0) {
            $data["EXTR_EXPENSE06_TRANS_SDATE"][FUNC] = "date('".substr($data_arr["EXTR_EXPENSE06_TRANS_SDATE"],0,4)."-".substr($data_arr["EXTR_EXPENSE06_TRANS_SDATE"],4,2)."-".substr($data_arr["EXTR_EXPENSE06_TRANS_SDATE"],6,2)."')";
        } else {
            $data["EXTR_EXPENSE06_TRANS_SDATE"][FUNC] = "CAST(NULL as date)";
        }

        $data["EXTR_EXPENSE06_MONEY"][NUMBER]           = $data_arr["EXTR_EXPENSE06_MONEY"];
        $data["EXTR_EXPENSE06_FLAG01"][TEXT]            = $data_arr["EXTR_EXPENSE06_FLAG01"];
        $data["EXTR_EXPENSE06_FLAG02"][TEXT]            = $data_arr["EXTR_EXPENSE06_FLAG02"];
        $data["EXTR_EXPENSE06_FLAG03"][TEXT]            = $data_arr["EXTR_EXPENSE06_FLAG03"];
        $data["EXTR_EXPENSE06_FLAG04"][TEXT]            = $data_arr["EXTR_EXPENSE06_FLAG04"];
        $data["EXTR_EXPENSE06_FLAG05"][TEXT]            = $data_arr["EXTR_EXPENSE06_FLAG05"];

        if ( strlen($data_arr["EXTR_EXPENSE06_PAID_MONEY_DATE"]) > 0) {
            $data["EXTR_EXPENSE06_PAID_MONEY_DATE"][FUNC] = "date('".substr($data_arr["EXTR_EXPENSE06_PAID_MONEY_DATE"],0,4)."-".substr($data_arr["EXTR_EXPENSE06_PAID_MONEY_DATE"],4,2)."-".substr($data_arr["EXTR_EXPENSE06_PAID_MONEY_DATE"],6,2)."')";
        } else {
            $data["EXTR_EXPENSE06_PAID_MONEY_DATE"][FUNC] = "CAST(NULL as date)";
        }
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][FUNC]        = "SYSDATE()";

        $query = Query::insertSQL($data, "bank_result_tmp_data");
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
        
        $query = Query::insertSQL($data, "bank_result_err_data");
        return $query;
    }

    function SelectData()
    {
        $query = " SELECT * FROM bank_result_tmp_data3 ";
        return $query;
    }

    //エラーテーブルの処理区分が消込処理のものを削除
    function ErrDataDelete($processcd)
    {
        $query  = "DELETE FROM bank_result_err_data WHERE PROCESSCD = '".$processcd."'";
        return $query;
    }

    //事前エラーチェック
    function InsertBeforeErrors(&$db, &$model)
    {
        $query = " INSERT INTO BANK_RESULT_ERR_DATA( ";
        $query .= " PROCESSCD, ";
        $query .= " FILE_LINE_NUMBER, ";
        $query .= " COLNAME, ";
        $query .= " ERR_LEVEL, ";
        $query .= " ERR_MSG, ";
        $query .= " REGISTERCD, ";
        $query .= " UPDATED ";
        $query .= " ) ";
        $query .= "     (SELECT ";
        $query .= "         '2', ";
        $query .= "         t1.FILE_LINE_NUMBER, ";
        $query .= "         'SCHOOL_CODE' AS COLNAME, ";
        $query .= "         '8', ";
        $query .= "         'MSG901 入力された値は不正です。', ";
        $query .= "         '".STAFFCD ."', ";
        $query .= "         SYSDATE() ";
        $query .= "     FROM ";
        $query .= "         BANK_RESULT_TMP_DATA3 t1 ";    //NO008
        $query .= "     WHERE ";
        $query .= "         t1.SCHOOL_CODE NOT IN (SELECT ";
        $query .= "                         W.VAR1 ";
        $query .= "                     FROM ";
        $query .= "                         SCHOOL_EXPENSES_SYS_INI w ";
        $query .= "                     WHERE ";
        $query .= "                         PROGRAMID = 'KNJP000K' AND ";
        $query .= "                         DIV       = '0001' ";
        $query .= "                     ) ";
        $query .= " UNION             ";
        $query .= " SELECT ";
        $query .= "     '2', ";
        $query .= "     t1.FILE_LINE_NUMBER, ";
        $query .= "     'COURSE_CODE' AS COLNAME, ";
        $query .= "     '8', ";
        $query .= "     'MSG901 入力された値は不正です。', ";
        $query .= "     '".STAFFCD ."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     BANK_RESULT_TMP_DATA3 t1 ";    //NO008
        $query .= " WHERE ";
        $query .= "     NOT EXISTS(SELECT ";
        $query .= "                     w1.year, ";
        $query .= "                     w1.grade, ";
        $query .= "                     w1.hr_class, ";
        $query .= "                     w1.bank_majorcd, ";
        $query .= "                     w1.bank_hr_class, ";
        $query .= "                     w2.schregno ";
        $query .= "                 FROM ";
        $query .= "                     bank_class_mst w1, ";
        $query .= "                     schreg_regd_dat w2 ";
        $query .= "                 WHERE ";
        $query .= "                     w1.year = w2.year AND ";
        $query .= "                     w1.grade = w2.grade AND ";
        $query .= "                     w1.hr_class = w2.hr_class AND ";
        $query .= "                     w1.year = '".CTRL_YEAR ."' AND ";
        $query .= "                     w2.semester = '".CTRL_SEMESTER ."' AND ";
        $query .= "                     w1.bank_majorcd = substr(t1.course_code, 1, 2) AND ";
        $query .= "                     w1.bank_hr_class = substr(t1.course_code, 5, 2) AND ";
        $query .= "                     w2.schregno = t1.schregno ";
        $query .= "                 ) ";
        $query .= " UNION             ";
        $query .= " SELECT ";
        $query .= "     '2', ";
        $query .= "     t1.FILE_LINE_NUMBER, ";
        $query .= "     'SCHREGNO' AS COLNAME, ";
        $query .= "     '8', ";
        $query .= "     'MSG901 入力された値は不正です。', ";
        $query .= "     '".STAFFCD ."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     BANK_RESULT_TMP_DATA3 t1 ";    //NO008
        $query .= " WHERE ";
        $query .= "     t1.SCHREGNO NOT IN (SELECT ";
        $query .= "                             SCHREGNO ";
        $query .= "                         FROM ";
        $query .= "                             SCHREG_BASE_MST w ";
        $query .= "                         ) ";
        $query .= " UNION    ";
        $query .= " SELECT ";
        $query .= "     '2', ";
        $query .= "     t1.FILE_LINE_NUMBER, ";
        $query .= "     'SCHREGNO' AS COLNAME, ";
        $query .= "     '8', ";
        $query .= "     'MSG302 データが重複しています。', ";
        $query .= "     '".STAFFCD ."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     BANK_RESULT_TMP_DATA3 t1, ";    //NO008
        $query .= "     BANK_RESULT_TMP_DATA3 t2, ";    //NO008
        $query .= "     SCHREG_BASE_MST t3 ";
        $query .= " WHERE ";
        $query .= "     t1.SCHREGNO = t2.SCHREGNO AND ";
        $query .= "     t1.SCHREGNO = t3.SCHREGNO AND ";
        $query .= "     t1.FILE_LINE_NUMBER <> t2.FILE_LINE_NUMBER ";
        $query .= "     ) ";

        $db->query($query);
    }

    //事前エラーチェック（銀行データ取込）
    function InsertBeforeBankErrors(&$db, &$model)
    {
        $query = " INSERT INTO BANK_RESULT_ERR_DATA( ";
        $query .= " PROCESSCD, ";
        $query .= " FILE_LINE_NUMBER, ";
        $query .= " COLNAME, ";
        $query .= " ERR_LEVEL, ";
        $query .= " ERR_MSG, ";
        $query .= " REGISTERCD, ";
        $query .= " UPDATED ";
        $query .= " ) ";
        $query .= " (SELECT ";
        $query .= "     '3', ";
        $query .= "     t1.FILE_LINE_NUMBER, ";
        $query .= "     'SCHREGNO' AS COLNAME, ";
        $query .= "     '8', ";
        $query .= "     'MSG901 入力された値は不正です。', ";
        $query .= "     '".STAFFCD ."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     BANK_RESULT_TMP_DATA3 t1 ";
        $query .= " WHERE ";
        $query .= "     t1.SCHREGNO NOT IN (SELECT ";
        $query .= "                             SCHREGNO ";
        $query .= "                         FROM ";
        $query .= "                             SCHREG_BASE_MST w ";
        $query .= "                         ) ";
        $query .= " UNION           ";
        $query .= " SELECT ";
        $query .= "     '3', ";
        $query .= "     t1.FILE_LINE_NUMBER, ";
        $query .= "     'SCHREGNO' AS COLNAME, ";
        $query .= "     '8', ";
        $query .= "     'MSG302 データが重複しています。', ";
        $query .= "     '".STAFFCD ."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     BANK_RESULT_TMP_DATA3 t1, ";
        $query .= "     BANK_RESULT_TMP_DATA3 t2, ";
        $query .= "     SCHREG_BASE_MST t3 ";
        $query .= " WHERE ";
        $query .= "     t1.BANKCD = t2.BANKCD AND ";
        $query .= "     t1.BRANCHCD = t2.BANKCD AND ";
        $query .= "     t1.SCHREGNO = t2.SCHREGNO AND ";
        $query .= "     t1.SCHREGNO = t3.SCHREGNO AND ";
        $query .= "     t1.FILE_LINE_NUMBER <> t2.FILE_LINE_NUMBER                ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '3', ";
        $query .= "     t1.FILE_LINE_NUMBER, ";
        $query .= "     'BRANCHCD' AS COLNAME, ";
        $query .= "     '8', ";
        $query .= "     'MSG901 入力された値は不正です。', ";
        $query .= "     '".STAFFCD ."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     BANK_RESULT_TMP_DATA3 t1 ";
        $query .= " WHERE ";
        $query .= "     t1.BRANCHCD IS NULL OR t1.BRANCHCD = '' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '3', ";
        $query .= "     t1.FILE_LINE_NUMBER, ";
        $query .= "     'DEPOSIT_ITEM' AS COLNAME, ";
        $query .= "     '8', ";
        $query .= "     'MSG901 入力された値は不正です。', ";
        $query .= "     '".STAFFCD ."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     BANK_RESULT_TMP_DATA3 t1 ";
        $query .= " WHERE ";
        $query .= "     t1.DEPOSIT_ITEM IS NULL OR t1.DEPOSIT_ITEM = '' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '3', ";
        $query .= "     t1.FILE_LINE_NUMBER, ";
        $query .= "     'ACCOUNTNO' AS COLNAME, ";
        $query .= "     '8', ";
        $query .= "     'MSG901 入力された値は不正です。', ";
        $query .= "     '".STAFFCD ."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     BANK_RESULT_TMP_DATA3 t1 ";
        $query .= " WHERE ";
        $query .= "     t1.ACCOUNTNO IS NULL OR t1.ACCOUNTNO = '' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '3', ";
        $query .= "     t1.FILE_LINE_NUMBER, ";
        $query .= "     'ACCOUNTNAME' AS COLNAME, ";
        $query .= "     '8', ";
        $query .= "     'MSG901 入力された値は不正です。', ";
        $query .= "     '".STAFFCD ."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     BANK_RESULT_TMP_DATA3 t1 ";
        $query .= " WHERE ";
        $query .= "     t1.ACCOUNTNAME IS NULL OR t1.ACCOUNTNAME = '' ";
        $query .= "     ) ";
    
        $db->query($query);
    }

    //二段目追加分データのカウント用 NO006
    function UpdateAfterProcess2(&$db, &$model)
    {
        $query = " UPDATE ";
        $query .= "     BANK_RESULT_TMP_DATA3 T1 ";    //NO008
        $query .= " SET ";
        $query .= "     PROCESS2_STS = '1',PROCESS2_UPDATED = SYSDATE() ";
        $query .= " WHERE ";
        $query .= "     EXISTS ( ";
        $query .= "         SELECT 'x' ";
        $query .= "         FROM ";
        $query .= "             BANK_RESULT_ERR_DATA ST1 ";
        $query .= "         WHERE ";
        $query .= "             ST1.PROCESSCD = '2' ";
        $query .= "             AND ST1.FILE_LINE_NUMBER = T1.FILE_LINE_NUMBER ";
        $query .= "     )   ";

        $db->query($query);

        $query = " UPDATE ";
        $query .= "     BANK_RESULT_TMP_DATA3 T1 ";    //NO008
        $query .= " SET ";
        $query .= "     PROCESS2_STS = '0',PROCESS2_UPDATED = SYSDATE() ";
        $query .= " WHERE ";
        $query .= "     NOT EXISTS ( ";
        $query .= "         SELECT 'x' ";
        $query .= "         FROM ";
        $query .= "             BANK_RESULT_ERR_DATA ST1 ";
        $query .= "         WHERE ";
        $query .= "             ST1.PROCESSCD = '2' ";
        $query .= "             AND ST1.FILE_LINE_NUMBER = T1.FILE_LINE_NUMBER ";
        $query .= "     )   ";

        $db->query($query);
    }

    //入金中分類データ登録 NO008
    function InsertAll_MoneyPaidMDat(&$db, &$model, $month, $year)
    {
        $query = " INSERT INTO MONEY_PAID_M_DAT( ";
        $query .= " YEAR, ";
        $query .= " SCHREGNO, ";
        $query .= " EXPENSE_L_CD, ";
        $query .= " EXPENSE_M_CD, ";
        $query .= " PAID_INPUT_FLG, ";
        $query .= " PAID_MONEY_DATE, ";
        $query .= " PAID_MONEY, ";
        $query .= " PAID_MONEY_DIV, ";
        $query .= " REGISTERCD, ";
        $query .= " UPDATED ";
        $query .= " ) ";
        $query .= "    (SELECT ";
        $query .= "         '".CTRL_YEAR."' AS YEAR, ";
        $query .= "         ST1.SCHREGNO, ";
        $query .= "         ST1.EXPENSE, ";
        $query .= "         COALESCE(ST2.EXPENSE_M_CD, RIGHT(digits(INTEGER(ST1.EXPENSE)+80),2)) AS EXPENSE_M_CD, ";
        $query .= "         '1' AS PAID_INPUT_FLG, ";
        $query .= "         ST1.TERM_PAID_MONEY_DATE, ";
        $query .= "         ST1.TERM_EXPENSE_MONEY_DUE, ";
        $query .= "         '01' AS PAID_MONEY_DIV, ";
        $query .= "         '".STAFFCD."',SYSDATE() ";
        $query .= "     FROM ";
        $query .= "         V_DEBUG01_BANK_RESULT_TMP_DATA ST1 ";
        $query .= "         LEFT OUTER JOIN(SELECT ";
        $query .= "                             DENSERANK() OVER(ORDER BY BANK_TRANS_SDATE) AS TERM, ";
        $query .= "                             ST1.EXPENSE_L_CD, ";
        $query .= "                             ST1.EXPENSE_M_CD ";
        $query .= "                         FROM ";
        $query .= "                             EXPENSE_M_MST ST1 ";
        $query .= "                         WHERE ";
        $query .= "                             ST1.YEAR = '".CTRL_YEAR."' ";
        $query .= "                         ) ST2 ON INTEGER(ST1.TERM) = ST2.TERM ";
        $query .= "              AND ST1.EXPENSE = ST2.EXPENSE_L_CD ";
        $query .= "     WHERE ";
        $query .= "         YEAR(ST1.TERM_PAID_MONEY_DATE) = ".$year." AND ";
        $query .= "         MONTH(ST1.TERM_PAID_MONEY_DATE) = ".$month." AND ";
        $query .= "         ST1.TERM_EXPENSE_MONEY_DUE > 0 AND ";
        $query .= "         NOT EXISTS(SELECT ";
        $query .= "                         * ";
        $query .= "                     FROM ";
        $query .= "                         MONEY_PAID_M_DAT W ";
        $query .= "                     WHERE ";
        $query .= "                         W.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "                         W.SCHREGNO = ST1.SCHREGNO AND ";
        $query .= "                         W.EXPENSE_L_CD = ST1.EXPENSE AND ";
        $query .= "                         W.EXPENSE_M_CD = COALESCE(ST2.EXPENSE_M_CD, RIGHT(digits(INTEGER(ST1.EXPENSE)+80),2)) AND ";
        $query .= "                         W.PAID_INPUT_FLG = '1' ";
        $query .= "                     ) AND ";
        $query .= "         NOT EXISTS(SELECT ";
        $query .= "                         * ";
        $query .= "                     FROM ";
        $query .= "                         BANK_RESULT_TMP_DATA3 E1 ";
        $query .= "                     WHERE ";
        $query .= "                         E1.FILE_LINE_NUMBER = ST1.FILE_LINE_NUMBER AND ";
        $query .= "                         (E1.PROCESS1_STS = '1' OR E1.PROCESS2_STS = '1') ";
        $query .= "                     ) ";
        $query .= "     ) ";

        $db->query($query);
    }
    //入金中分類データ更新
    function Update_MoneyPaidMDat(&$db, &$model, $month, $year)
    {
        $query = " UPDATE ";
        $query .= "     MONEY_PAID_M_DAT T1 ";
        $query .= " SET ";
        $query .= "     (PAID_MONEY_DATE,PAID_MONEY,UPDATED) = (SELECT  ";
        $query .= "         T2.TERM_PAID_MONEY_DATE, ";
        $query .= "         T2.TERM_EXPENSE_MONEY_DUE, ";
        $query .= "         SYSDATE() ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             ST1.*, ";
        $query .= "             COALESCE(ST2.EXPENSE_M_CD, RIGHT(digits(INTEGER(ST1.EXPENSE)+80),2)) AS EXPENSE_M_CD ";
        $query .= "         FROM ";
        $query .= "             V_DEBUG01_BANK_RESULT_TMP_DATA ST1 LEFT OUTER JOIN(SELECT ";
        $query .= "                 DENSERANK() OVER(ORDER BY BANK_TRANS_SDATE) AS TERM, ";
        $query .= "                 ST1.EXPENSE_L_CD, ";
        $query .= "                 ST1.EXPENSE_M_CD ";
        $query .= "             FROM ";
        $query .= "                 EXPENSE_M_MST ST1 ";
        $query .= "             WHERE ";
        $query .= "                 ST1.YEAR = '".CTRL_YEAR."' ";
        $query .= "         ) ST2 ON INTEGER(ST1.TERM) = ST2.TERM AND ST1.EXPENSE = ST2.EXPENSE_L_CD ";
        $query .= "         WHERE ";
        $query .= "             NOT EXISTS(SELECT ";
        $query .= "                            * ";
        $query .= "                        FROM ";
        $query .= "                            BANK_RESULT_TMP_DATA3 E1 ";
        $query .= "                        WHERE ";
        $query .= "                            E1.FILE_LINE_NUMBER = ST1.FILE_LINE_NUMBER AND ";
        $query .= "                            (E1.PROCESS1_STS = '1' OR E1.PROCESS2_STS = '1') ";
        $query .= "                       ) AND ";
        $query .= "             YEAR(ST1.TERM_PAID_MONEY_DATE) = ".$year." AND ";
        $query .= "             MONTH(ST1.TERM_PAID_MONEY_DATE) = ".$month." AND ";
        $query .= "             ST1.TERM_EXPENSE_MONEY_DUE > 0 ";
        $query .= "         ) T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "         T1.EXPENSE_L_CD = T2.EXPENSE AND ";
        $query .= "         T1.EXPENSE_M_CD = T2.EXPENSE_M_CD ";
        $query .= "     ) ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     (SCHREGNO,EXPENSE_L_CD,EXPENSE_M_CD) IN (SELECT ";
        $query .= "         ST1.SCHREGNO, ";
        $query .= "         ST1.EXPENSE, ";
        $query .= "         COALESCE(ST2.EXPENSE_M_CD, RIGHT(digits(INTEGER(ST1.EXPENSE)+80),2)) AS EXPENSE_M_CD ";
        $query .= "     FROM ";
        $query .= "         V_DEBUG01_BANK_RESULT_TMP_DATA ST1 LEFT OUTER JOIN(SELECT ";
        $query .= "             DENSERANK() OVER(ORDER BY BANK_TRANS_SDATE) AS TERM, ";
        $query .= "             ST1.EXPENSE_L_CD, ";
        $query .= "             ST1.EXPENSE_M_CD ";
        $query .= "         FROM ";
        $query .= "             EXPENSE_M_MST ST1 ";
        $query .= "         WHERE ";
        $query .= "             ST1.YEAR = '".CTRL_YEAR."' ";
        $query .= "         ) ST2 ON INTEGER(ST1.TERM) = ST2.TERM AND ST1.EXPENSE = ST2.EXPENSE_L_CD ";
        $query .= "     WHERE ";
        $query .= "         NOT EXISTS(SELECT ";
        $query .= "                        * ";
        $query .= "                    FROM ";
        $query .= "                        BANK_RESULT_TMP_DATA3 E1 ";
        $query .= "                    WHERE ";
        $query .= "                        E1.FILE_LINE_NUMBER = ST1.FILE_LINE_NUMBER AND ";
        $query .= "                        (E1.PROCESS1_STS = '1' OR E1.PROCESS2_STS = '1') ";
        $query .= "                   ) AND ";
        $query .= "         YEAR(ST1.TERM_PAID_MONEY_DATE) = ".$year." AND ";
        $query .= "         MONTH(ST1.TERM_PAID_MONEY_DATE) = ".$month." AND ";
        $query .= "         ST1.TERM_EXPENSE_MONEY_DUE > 0 ";
        $query .= "     ) ";

        $db->query($query);
    }
    //入金小分類データ登録
    function InsertAll_MoneyPaidSDat(&$db, &$model, $month, $year)
    {
        $query = " INSERT INTO MONEY_PAID_S_DAT( ";
        $query .= " YEAR, ";
        $query .= " SCHREGNO, ";
        $query .= " EXPENSE_M_CD, ";
        $query .= " EXPENSE_S_CD, ";
        $query .= " PAID_INPUT_FLG, ";
        $query .= " PAID_MONEY_DATE, ";
        $query .= " PAID_MONEY, ";
        $query .= " PAID_MONEY_DIV, ";
        $query .= " REGISTERCD, ";
        $query .= " UPDATED ";
        $query .= " ) ";
        $query .= " (SELECT ";
        $query .= "     '".CTRL_YEAR."', ";
        $query .= "     ST1.SCHREGNO, ";
        $query .= "     ST1.EXPENSE_M_CD, ";
        $query .= "     ST1.EXPENSE_S_CD, ";
        $query .= "     '1', ";
        $query .= "     CASE WHEN ST1.TERM_EXPENSE_MONEY_DUE >= ST1.MONEY_DUE2 THEN ST1.TERM_PAID_MONEY_DATE ELSE CAST(NULL AS DATE) END AS PAID_MONEY_DATE, ";
        $query .= "     CASE WHEN ST1.TERM_EXPENSE_MONEY_DUE >= ST1.MONEY_DUE2 THEN ST1.MONEY_DUE ELSE CAST(NULL AS INTEGER) END AS PAID_MONEY, ";
        $query .= "     CASE WHEN ST1.TERM_EXPENSE_MONEY_DUE >= ST1.MONEY_DUE2 THEN '01' ELSE CAST(NULL AS VARCHAR(2)) END AS PAID_MONEY_DIV, ";
        $query .= "     '".STAFFCD."',SYSDATE() ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         ST1.SCHREGNO, ";
        $query .= "         ST1.TERM_EXPENSE_MONEY_DUE , ";
        $query .= "         ST2.EXPENSE_M_CD, ";
        $query .= "         ST3.EXPENSE_S_CD, ";
        $query .= "         ST1.TERM_PAID_MONEY_DATE, ";
        $query .= "         ST3.MONEY_DUE, ";
        $query .= "         ST4.MONEY_DUE AS MONEY_DUE2 ";
        $query .= "     FROM ";
        $query .= "         V_DEBUG01_BANK_RESULT_TMP_DATA ST1, ";
        $query .= "         (SELECT ";
        $query .= "             DENSERANK() OVER(ORDER BY BANK_TRANS_SDATE) AS TERM, ";
        $query .= "             ST1.EXPENSE_L_CD, ";
        $query .= "             ST1.EXPENSE_M_CD ";
        $query .= "         FROM ";
        $query .= "             EXPENSE_M_MST ST1 ";
        $query .= "         WHERE ";
        $query .= "             ST1.YEAR = '".CTRL_YEAR."' ";
        $query .= "         ) ST2 , ";
        $query .= "         (SELECT ";
        $query .= "             W.SCHREGNO, ";
        $query .= "             W2.EXPENSE_M_CD, ";
        $query .= "             W2.EXPENSE_S_CD, ";
        $query .= "             COALESCE(SUM(W.MONEY_DUE),0) AS MONEY_DUE ";
        $query .= "         FROM ";
        $query .= "             MONEY_DUE_S_DAT W, ";
        $query .= "             V_EXPENSE_S_MST W2 ";
        $query .= "         WHERE ";
        $query .= "             W.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "             W.YEAR = W2.YEAR AND ";
        $query .= "             W.EXPENSE_M_CD = W2.EXPENSE_M_CD AND ";
        $query .= "             W.EXPENSE_S_CD = W2.EXPENSE_S_CD ";
        $query .= "         GROUP BY ";
        $query .= "             W.SCHREGNO, ";
        $query .= "             W2.EXPENSE_M_CD, ";
        $query .= "             W2.EXPENSE_S_CD ";
        $query .= "         ) ST3, ";
        $query .= "         (SELECT ";
        $query .= "             W.SCHREGNO, ";
        $query .= "             W2.EXPENSE_M_CD, ";
        $query .= "             COALESCE(SUM(W.MONEY_DUE),0) AS MONEY_DUE ";
        $query .= "         FROM ";
        $query .= "             MONEY_DUE_S_DAT W, ";
        $query .= "             V_EXPENSE_S_MST W2 ";
        $query .= "         WHERE ";
        $query .= "             W.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "             W.YEAR = W2.YEAR AND ";
        $query .= "             W.EXPENSE_M_CD = W2.EXPENSE_M_CD AND ";
        $query .= "             W.EXPENSE_S_CD = W2.EXPENSE_S_CD ";
        $query .= "         GROUP BY ";
        $query .= "             W.SCHREGNO, ";
        $query .= "             W2.EXPENSE_M_CD ";
        $query .= "         ) ST4 ";
        $query .= "     WHERE ";
        $query .= "         NOT EXISTS(SELECT ";
        $query .= "                         * ";
        $query .= "                     FROM ";
        $query .= "                         BANK_RESULT_TMP_DATA3 E1 ";
        $query .= "                     WHERE ";
        $query .= "                         E1.FILE_LINE_NUMBER = ST1.FILE_LINE_NUMBER AND ";
        $query .= "                         (E1.PROCESS1_STS = '1' OR E1.PROCESS2_STS = '1') ";
        $query .= "                     ) AND ";
        $query .= "         YEAR(ST1.TERM_PAID_MONEY_DATE) = ".$year." AND ";
        $query .= "         MONTH(ST1.TERM_PAID_MONEY_DATE) = ".$month." AND ";
        $query .= "         INTEGER(ST1.TERM) = ST2.TERM AND ";
        $query .= "         ST1.EXPENSE = ST2.EXPENSE_L_CD AND ";
        $query .= "         ST2.EXPENSE_M_CD = ST3.EXPENSE_M_CD AND ";
        $query .= "         ST2.EXPENSE_M_CD = ST4.EXPENSE_M_CD AND ";
        $query .= "         ST1.SCHREGNO = ST3.SCHREGNO AND ";
        $query .= "         ST1.SCHREGNO = ST4.SCHREGNO ";
        $query .= "     ) ST1 ";
        $query .= " WHERE ";
        $query .= "     NOT EXISTS(SELECT ";
        $query .= "                     * ";
        $query .= "                 FROM ";
        $query .= "                     MONEY_PAID_S_DAT W ";
        $query .= "                 WHERE ";
        $query .= "                     W.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "                     W.SCHREGNO = ST1.SCHREGNO AND ";
        $query .= "                     W.EXPENSE_M_CD = ST1.EXPENSE_M_CD AND ";
        $query .= "                     W.EXPENSE_S_CD = ST1.EXPENSE_S_CD AND ";
        $query .= "                     W.PAID_INPUT_FLG = '1' ";
        $query .= "                 ) ";
        if ($model->schooldiv == 0) {
            $query .= "     AND NOT EXISTS( SELECT ";
            $query .= "                         * ";
            $query .= "                     FROM ";
            $query .= "                         FRESHMAN_DAT W2 ";
            $query .= "                     WHERE ";
            $query .= "                         W2.ENTERYEAR = '".CTRL_YEAR."' ";
            $query .= "                         AND W2.SCHREGNO = ST1.SCHREGNO ";
            $query .= "                   ) ";
        }
        $query .= " )   ";

        $db->query($query);
    }

    //入金小分類データ登録(入金額オーバーのレコードを追加）
    function InsertOver_MoneyPaidSDat(&$db, &$model, $month, $year)
    {
        $query = " INSERT INTO MONEY_PAID_S_DAT( ";
        $query .= " YEAR, ";
        $query .= " SCHREGNO, ";
        $query .= " EXPENSE_M_CD, ";
        $query .= " EXPENSE_S_CD, ";
        $query .= " PAID_INPUT_FLG, ";
        $query .= " PAID_MONEY_DATE, ";
        $query .= " PAID_MONEY, ";
        $query .= " PAID_MONEY_DIV, ";
        $query .= " REMARK, ";
        $query .= " REGISTERCD, ";
        $query .= " UPDATED ";
        $query .= " ) ";
        $query .= "      (SELECT ";
        $query .= "             '".CTRL_YEAR."', ";
        $query .= "             ST1.SCHREGNO, ";
        $query .= "             ST1.EXPENSE_M_CD, ";
        $query .= "             '99', ";
        $query .= "             '1', ";
        $query .= "             ST1.TERM_PAID_MONEY_DATE, ";
        $query .= "             ST1.TERM_EXPENSE_MONEY_DUE - ST1.MONEY_DUE , ";
        $query .= "             '01', ";
        $query .= "             '".STAFFCD."', ";
        $query .= "             '".STAFFCD."', ";
        $query .= "             SYSDATE() ";
        $query .= "         FROM ";
        $query .= "             (SELECT ";
        $query .= "                 ST1.SCHREGNO, ";
        $query .= "                 ST1.TERM_EXPENSE_MONEY_DUE, ";
        $query .= "                 ST2.EXPENSE_M_CD, ";
        $query .= "                 '99', ";
        $query .= "                 ST1.TERM_PAID_MONEY_DATE, ";
        $query .= "                 ST3.MONEY_DUE ";
        $query .= "             FROM ";
        $query .= "                 V_DEBUG01_BANK_RESULT_TMP_DATA ST1 , ";
        $query .= "                 (SELECT ";
        $query .= "                     DENSERANK() OVER(ORDER BY BANK_TRANS_SDATE) AS TERM, ";
        $query .= "                     ST1.EXPENSE_L_CD, ";
        $query .= "                     ST1.EXPENSE_M_CD ";
        $query .= "                 FROM ";
        $query .= "                     EXPENSE_M_MST ST1 ";
        $query .= "                 WHERE ";
        $query .= "                     ST1.YEAR = '".CTRL_YEAR."' ";
        $query .= "                 ) ST2 , ";
        $query .= "                 (SELECT ";
        $query .= "                     W.SCHREGNO, ";
        $query .= "                     W2.EXPENSE_M_CD, ";
        $query .= "                     COALESCE(SUM(W.MONEY_DUE),0) AS MONEY_DUE ";
        $query .= "                 FROM ";
        $query .= "                     MONEY_DUE_S_DAT W, ";
        $query .= "                     V_EXPENSE_S_MST W2 ";
        $query .= "                 WHERE ";
        $query .= "                     W.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "                     W.YEAR = W2.YEAR AND ";
        $query .= "                     W.EXPENSE_M_CD = W2.EXPENSE_M_CD AND ";
        $query .= "                     W.EXPENSE_S_CD = W2.EXPENSE_S_CD ";
        $query .= "                 GROUP BY ";
        $query .= "                     W.SCHREGNO, ";
        $query .= "                     W2.EXPENSE_M_CD ";
        $query .= "                 ) ST3 ";
        $query .= "             WHERE ";
        $query .= "                 NOT EXISTS(SELECT ";
        $query .= "                                 * ";
        $query .= "                            FROM ";
        $query .= "                                 BANK_RESULT_TMP_DATA3 E1 ";
        $query .= "                            WHERE ";
        $query .= "                                 E1.FILE_LINE_NUMBER = ST1.FILE_LINE_NUMBER AND ";
        $query .= "                                 (E1.PROCESS1_STS = '1' OR E1.PROCESS2_STS = '1') ";
        $query .= "                           ) AND ";
        $query .= "                 YEAR(ST1.TERM_PAID_MONEY_DATE) = ".$year." AND ";
        $query .= "                 MONTH(ST1.TERM_PAID_MONEY_DATE) = ".$month." AND ";
        $query .= "                 INTEGER(ST1.TERM) = ST2.TERM AND ";
        $query .= "                 ST1.EXPENSE = ST2.EXPENSE_L_CD AND ";
        $query .= "                 ST2.EXPENSE_M_CD = ST3.EXPENSE_M_CD AND ";
        $query .= "                 ST1.SCHREGNO = ST3.SCHREGNO ";
        $query .= "             ) ST1 ";
        $query .= "         WHERE ";
        $query .= "             ST1.TERM_EXPENSE_MONEY_DUE > ST1.MONEY_DUE AND ";
        $query .= "             NOT EXISTS(SELECT ";
        $query .= "                             * ";
        $query .= "                         FROM ";
        $query .= "                             MONEY_PAID_S_DAT W ";
        $query .= "                         WHERE ";
        $query .= "                             W.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "                             W.SCHREGNO = ST1.SCHREGNO AND ";
        $query .= "                             W.EXPENSE_M_CD = ST1.EXPENSE_M_CD AND ";
        $query .= "                             W.EXPENSE_S_CD = '99' AND ";
        $query .= "                             W.PAID_INPUT_FLG = '1' ";
        $query .= "                         ) ";
        if ($model->schooldiv == 0) {
            $query .= "             AND NOT EXISTS( SELECT ";
            $query .= "                                 * ";
            $query .= "                             FROM ";
            $query .= "                                 FRESHMAN_DAT W2 ";
            $query .= "                             WHERE ";
            $query .= "                                 W2.ENTERYEAR = '".CTRL_YEAR."' ";
            $query .= "                                 AND W2.SCHREGNO = ST1.SCHREGNO ";
            $query .= "                           ) ";
        }
        $query .= "         ) ";

        $db->query($query);
    }
    //入金小分類データ更新
    function Update_MoneyPaidSDat(&$db, &$model, $month, $year)
    {
        $query = " UPDATE ";
        $query .= "     MONEY_PAID_S_DAT T1 ";
        $query .= " SET ";
        $query .= "     ( PAID_MONEY_DATE, PAID_MONEY, PAID_MONEY_DIV, REGISTERCD,UPDATED )=(SELECT ";
        $query .= "     CASE WHEN ST1.TERM_EXPENSE_MONEY_DUE >= ST1.MONEY_DUE2 THEN ST1.TERM_PAID_MONEY_DATE ELSE CAST(NULL AS DATE) END AS PAID_MONEY_DATE, ";
        $query .= "     CASE WHEN ST1.TERM_EXPENSE_MONEY_DUE >= ST1.MONEY_DUE2 THEN ST1.MONEY_DUE ELSE CAST(NULL AS INTEGER) END AS PAID_MONEY, ";
        $query .= "     CASE WHEN ST1.TERM_EXPENSE_MONEY_DUE >= ST1.MONEY_DUE2 THEN '01' ELSE CAST(NULL AS VARCHAR(2)) END AS PAID_MONEY_DIV, ";
        $query .= "     '".STAFFCD."',SYSDATE() ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         ST1.SCHREGNO, ";
        $query .= "         ST1.TERM_EXPENSE_MONEY_DUE , ";
        $query .= "         ST2.EXPENSE_M_CD, ";
        $query .= "         ST3.EXPENSE_S_CD, ";
        $query .= "         ST1.TERM_PAID_MONEY_DATE, ";
        $query .= "         ST3.MONEY_DUE, ";
        $query .= "         ST4.MONEY_DUE AS MONEY_DUE2 ";
        $query .= "     FROM ";
        $query .= "         V_DEBUG01_BANK_RESULT_TMP_DATA ST1 , ";
        $query .= "         (SELECT ";
        $query .= "             DENSERANK() OVER(ORDER BY BANK_TRANS_SDATE) AS TERM, ";
        $query .= "             ST1.EXPENSE_L_CD, ";
        $query .= "             ST1.EXPENSE_M_CD ";
        $query .= "         FROM ";
        $query .= "             EXPENSE_M_MST ST1 ";
        $query .= "         WHERE ";
        $query .= "             ST1.YEAR = '".CTRL_YEAR."' ";
        $query .= "         ) ST2 , ";
        $query .= "         (SELECT ";
        $query .= "             W.SCHREGNO, ";
        $query .= "             W2.EXPENSE_M_CD, ";
        $query .= "             W2.EXPENSE_S_CD, ";
        $query .= "             COALESCE(SUM(W.MONEY_DUE),0) AS MONEY_DUE ";
        $query .= "         FROM ";
        $query .= "             MONEY_DUE_S_DAT W, ";
        $query .= "             V_EXPENSE_S_MST W2 ";
        $query .= "         WHERE ";
        $query .= "             W.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "             W.YEAR = W2.YEAR AND ";
        $query .= "             W.EXPENSE_M_CD = W2.EXPENSE_M_CD AND ";
        $query .= "             W.EXPENSE_S_CD = W2.EXPENSE_S_CD ";
        $query .= "         GROUP BY ";
        $query .= "             W.SCHREGNO, ";
        $query .= "             W2.EXPENSE_M_CD, ";
        $query .= "             W2.EXPENSE_S_CD ";
        $query .= "         ) ST3, ";
        $query .= "         (SELECT ";
        $query .= "             W.SCHREGNO, ";
        $query .= "             W2.EXPENSE_M_CD, ";
        $query .= "             COALESCE(SUM(W.MONEY_DUE),0) AS MONEY_DUE ";
        $query .= "         FROM ";
        $query .= "             MONEY_DUE_S_DAT W, ";
        $query .= "             V_EXPENSE_S_MST W2 ";
        $query .= "         WHERE ";
        $query .= "             W.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "             W.YEAR = W2.YEAR AND ";
        $query .= "             W.EXPENSE_M_CD = W2.EXPENSE_M_CD AND ";
        $query .= "             W.EXPENSE_S_CD = W2.EXPENSE_S_CD ";
        $query .= "         GROUP BY ";
        $query .= "             W.SCHREGNO, ";
        $query .= "             W2.EXPENSE_M_CD ";
        $query .= "         ) ST4 ";
        $query .= "     WHERE ";
        $query .= "         NOT EXISTS(SELECT ";
        $query .= "                         * ";
        $query .= "                    FROM ";
        $query .= "                         BANK_RESULT_TMP_DATA3 E1 ";
        $query .= "                    WHERE ";
        $query .= "                         E1.FILE_LINE_NUMBER = ST1.FILE_LINE_NUMBER AND ";
        $query .= "                         (E1.PROCESS1_STS = '1' OR E1.PROCESS2_STS = '1') ";
        $query .= "                   ) AND ";
        $query .= "         YEAR(ST1.TERM_PAID_MONEY_DATE) = ".$year." AND ";
        $query .= "         MONTH(ST1.TERM_PAID_MONEY_DATE) = ".$month." AND ";
        $query .= "         INTEGER(ST1.TERM) = ST2.TERM AND ";
        $query .= "         ST1.EXPENSE = ST2.EXPENSE_L_CD AND ";
        $query .= "         ST2.EXPENSE_M_CD = ST3.EXPENSE_M_CD AND ";
        $query .= "         ST2.EXPENSE_M_CD = ST4.EXPENSE_M_CD AND ";
        $query .= "         ST1.SCHREGNO = ST3.SCHREGNO AND ";
        $query .= "         ST1.SCHREGNO = ST4.SCHREGNO ";
        $query .= "     ) ST1 ";
        $query .= " WHERE ";
        $query .= "     ST1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     AND ST1.EXPENSE_M_CD = T1.EXPENSE_M_CD ";
        $query .= "     AND ST1.EXPENSE_S_CD = T1.EXPENSE_S_CD ";
        if ($model->schooldiv == 0) {
            $query .= "     AND NOT EXISTS( SELECT ";
            $query .= "                         * ";
            $query .= "                     FROM ";
            $query .= "                         FRESHMAN_DAT EX ";
            $query .= "                     WHERE ";
            $query .= "                         EX.ENTERYEAR = '".CTRL_YEAR."' ";
            $query .= "                         AND EX.SCHREGNO = ST1.SCHREGNO ";
            $query .= "                   ) ";
        }
        $query .= " ) ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.PAID_INPUT_FLG = '1' AND ";
        $query .= "     (T1.SCHREGNO,T1.EXPENSE_M_CD) IN (SELECT ";
        $query .= "         ST1.SCHREGNO, ";
        $query .= "         ST2.EXPENSE_M_CD ";
        $query .= "     FROM ";
        $query .= "         V_DEBUG01_BANK_RESULT_TMP_DATA ST1 , ";
        $query .= "         (SELECT ";
        $query .= "             DENSERANK() OVER(ORDER BY BANK_TRANS_SDATE) AS TERM, ";
        $query .= "             ST1.EXPENSE_L_CD, ";
        $query .= "             ST1.EXPENSE_M_CD ";
        $query .= "         FROM ";
        $query .= "             EXPENSE_M_MST ST1 ";
        $query .= "         WHERE ";
        $query .= "             ST1.YEAR = '".CTRL_YEAR."' ";
        $query .= "         ) ST2 ";
        $query .= "     WHERE ";
        $query .= "         NOT EXISTS(SELECT ";
        $query .= "                         * ";
        $query .= "                    FROM ";
        $query .= "                         BANK_RESULT_TMP_DATA3 E1 ";
        $query .= "                    WHERE ";
        $query .= "                         E1.FILE_LINE_NUMBER = ST1.FILE_LINE_NUMBER AND ";
        $query .= "                         (E1.PROCESS1_STS = '1' OR E1.PROCESS2_STS = '1') ";
        $query .= "                   ) AND ";
        $query .= "         YEAR(ST1.TERM_PAID_MONEY_DATE) = ".$year." AND ";
        $query .= "         MONTH(ST1.TERM_PAID_MONEY_DATE) = ".$month." AND ";
        $query .= "         INTEGER(ST1.TERM) = ST2.TERM AND ";
        $query .= "         ST1.EXPENSE = ST2.EXPENSE_L_CD ";
        $query .= "     ) ";
        if ($model->schooldiv == 0) {
            $query .= "     AND NOT EXISTS( SELECT ";
            $query .= "                         * ";
            $query .= "                     FROM ";
            $query .= "                         FRESHMAN_DAT EX2 ";
            $query .= "                     WHERE ";
            $query .= "                         EX2.ENTERYEAR = '".CTRL_YEAR."' ";
            $query .= "                         AND EX2.SCHREGNO = T1.SCHREGNO ";
            $query .= "                   ) ";
        }

        $db->query($query);
    }
    //入金小分類データ更新(入金額オーバーのレコードを更新）
    function Update_OverMoneyPaidSDat(&$db, &$model, $month, $year)
    {
        $query = " UPDATE ";
        $query .= "     MONEY_PAID_S_DAT T1 ";
        $query .= " SET ";
        $query .= "     ( PAID_MONEY_DATE, PAID_MONEY, PAID_MONEY_DIV, REGISTERCD, UPDATED )=(SELECT ";
        $query .= "                 ST1.TERM_PAID_MONEY_DATE, ";
        $query .= "                 ST1.TERM_EXPENSE_MONEY_DUE-ST1.MONEY_DUE, ";
        $query .= "                 '01', ";
        $query .= "                 '09999',SYSDATE() ";
        $query .= "             FROM ";
        $query .= "                 (SELECT ";
        $query .= "                     ST1.SCHREGNO, ";
        $query .= "                     ST1.TERM_EXPENSE_MONEY_DUE, ";
        $query .= "                     ST2.EXPENSE_M_CD, ";
        $query .= "                     '99' AS EXPENSE_S_CD, ";
        $query .= "                     ST1.TERM_PAID_MONEY_DATE, ";
        $query .= "                     ST3.MONEY_DUE ";
        $query .= "                 FROM ";
        $query .= "                     V_DEBUG01_BANK_RESULT_TMP_DATA ST1 , ";
        $query .= "                     (SELECT ";
        $query .= "                         DENSERANK() OVER(ORDER BY BANK_TRANS_SDATE) AS TERM, ";
        $query .= "                         ST1.EXPENSE_L_CD, ";
        $query .= "                         ST1.EXPENSE_M_CD ";
        $query .= "                     FROM ";
        $query .= "                         EXPENSE_M_MST ST1 ";
        $query .= "                     WHERE ";
        $query .= "                         ST1.YEAR = '".CTRL_YEAR."' ";
        $query .= "                     ) ST2 , ";
        $query .= "                     (SELECT ";
        $query .= "                         W.SCHREGNO, ";
        $query .= "                         W2.EXPENSE_M_CD, ";
        $query .= "                         COALESCE(SUM(W.MONEY_DUE),0) AS MONEY_DUE ";
        $query .= "                     FROM ";
        $query .= "                         MONEY_DUE_S_DAT W, ";
        $query .= "                         V_EXPENSE_S_MST W2 ";
        $query .= "                     WHERE ";
        $query .= "                         W.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "                         W.YEAR = W2.YEAR AND ";
        $query .= "                         W.EXPENSE_M_CD = W2.EXPENSE_M_CD AND ";
        $query .= "                         W.EXPENSE_S_CD = W2.EXPENSE_S_CD ";
        $query .= "                     GROUP BY ";
        $query .= "                         W.SCHREGNO, ";
        $query .= "                         W2.EXPENSE_M_CD ";
        $query .= "                     ) ST3 ";
        $query .= "                 WHERE ";
        $query .= "                     NOT EXISTS(SELECT ";
        $query .= "                                     * ";
        $query .= "                                FROM ";
        $query .= "                                     BANK_RESULT_TMP_DATA3 E1 ";
        $query .= "                                WHERE ";
        $query .= "                                     E1.FILE_LINE_NUMBER = ST1.FILE_LINE_NUMBER AND ";
        $query .= "                                     (E1.PROCESS1_STS = '1' OR E1.PROCESS2_STS = '1') ";
        $query .= "                               ) AND ";
        $query .= "                     YEAR(ST1.TERM_PAID_MONEY_DATE) = ".$year." AND ";
        $query .= "                     MONTH(ST1.TERM_PAID_MONEY_DATE) = ".$month." AND ";
        $query .= "                     INTEGER(ST1.TERM) = ST2.TERM AND ";
        $query .= "                     ST1.EXPENSE = ST2.EXPENSE_L_CD AND ";
        $query .= "                     ST2.EXPENSE_M_CD = ST3.EXPENSE_M_CD AND ";
        $query .= "                     ST1.SCHREGNO = ST3.SCHREGNO ";
        $query .= "                 ) ST1 ";
        $query .= "             WHERE ";
        $query .= "                 ST1.TERM_EXPENSE_MONEY_DUE > ST1.MONEY_DUE AND ";
        $query .= "                 ST1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                 ST1.EXPENSE_M_CD = T1.EXPENSE_M_CD AND ";
        $query .= "                 ST1.EXPENSE_S_CD = T1.EXPENSE_S_CD ";
        if ($model->schooldiv == 0) {
            $query .= "                 AND NOT EXISTS( SELECT ";
            $query .= "                                     * ";
            $query .= "                                 FROM ";
            $query .= "                                     FRESHMAN_DAT EX ";
            $query .= "                                 WHERE ";
            $query .= "                                     EX.ENTERYEAR = '".CTRL_YEAR."' ";
            $query .= "                                     AND EX.SCHREGNO = ST1.SCHREGNO ";
            $query .= "                               ) ";
        }
        $query .= "             ) ";
        $query .= " WHERE ";
        $query .= " T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= " T1.PAID_INPUT_FLG = '1' AND ";
        $query .= " (T1.SCHREGNO,T1.EXPENSE_M_CD,T1.EXPENSE_S_CD) IN (SELECT ";
        $query .= "         ST1.SCHREGNO, ";
        $query .= "         ST2.EXPENSE_M_CD, ";
        $query .= "         '99' ";
        $query .= "     FROM ";
        $query .= "         V_DEBUG01_BANK_RESULT_TMP_DATA ST1 , ";
        $query .= "         (SELECT ";
        $query .= "             DENSERANK() OVER(ORDER BY BANK_TRANS_SDATE) AS TERM, ";
        $query .= "             ST1.EXPENSE_L_CD, ";
        $query .= "             ST1.EXPENSE_M_CD ";
        $query .= "         FROM ";
        $query .= "             EXPENSE_M_MST ST1 ";
        $query .= "         WHERE ";
        $query .= "             ST1.YEAR = '".CTRL_YEAR."' ";
        $query .= "         ) ST2 ";
        $query .= "     WHERE ";
        $query .= "         NOT EXISTS(SELECT ";
        $query .= "                         * ";
        $query .= "                    FROM ";
        $query .= "                         BANK_RESULT_TMP_DATA3 E1 ";
        $query .= "                    WHERE ";
        $query .= "                         E1.FILE_LINE_NUMBER = ST1.FILE_LINE_NUMBER AND ";
        $query .= "                         (E1.PROCESS1_STS = '1' OR E1.PROCESS2_STS = '1') ";
        $query .= "                   ) AND ";
        $query .= "         YEAR(ST1.TERM_PAID_MONEY_DATE) = ".$year." AND ";
        $query .= "         MONTH(ST1.TERM_PAID_MONEY_DATE) = ".$month." AND ";
        $query .= "         INTEGER(ST1.TERM) = ST2.TERM AND ";
        $query .= "         ST1.EXPENSE = ST2.EXPENSE_L_CD ";
        $query .= "     ) ";
        if ($model->schooldiv == 0) {
            $query .= " AND NOT EXISTS( SELECT ";
            $query .= "                     * ";
            $query .= "                 FROM ";
            $query .= "                     FRESHMAN_DAT EX2 ";
            $query .= "                 WHERE ";
            $query .= "                     EX2.ENTERYEAR = '".CTRL_YEAR."' ";
            $query .= "                     AND EX2.SCHREGNO = T1.SCHREGNO ";
            $query .= "               ) ";
        }

        $db->query($query);
    }

    //入金中分類データ取り消し
    function Cancel_MoneyPaidMDat(&$db, &$model)
    {
        $query = " UPDATE ";
        $query .= "     MONEY_PAID_M_DAT T1 ";
        $query .= " SET ";
        $query .= "     (PAID_MONEY_DATE,PAID_MONEY,PAID_MONEY_DIV,UPDATED) = (NULL,NULL,NULL,SYSDATE()) ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     (SCHREGNO,EXPENSE_L_CD) NOT IN (SELECT ";
        $query .= "         ST1.SCHREGNO, ";
        $query .= "         ST1.EXPENSE ";
        $query .= "     FROM ";
        $query .= "         V_DEBUG01_BANK_RESULT_TMP_DATA ST1 ";
        $query .= "     WHERE ";
        $query .= "         NOT EXISTS(SELECT ";
        $query .= "                         * ";
        $query .= "                    FROM ";
        $query .= "                         BANK_RESULT_TMP_DATA3 E1 ";
        $query .= "                    WHERE ";
        $query .= "                         E1.FILE_LINE_NUMBER = ST1.FILE_LINE_NUMBER AND ";
        $query .= "                         (E1.PROCESS1_STS = '1' OR E1.PROCESS2_STS = '1' OR E1.PROCESS3_STS = '1') ";
        $query .= "                   ) AND ";
        $query .= "         YEAR(ST1.TERM_PAID_MONEY_DATE) = ".CTRL_YEAR." AND ";
        $query .= "         MONTH(ST1.TERM_PAID_MONEY_DATE) = ".$model->paid_month." ";
        $query .= "     ) ";

        $db->query($query);
    }
    //入金小分類データ取り消し
    function Cancel_MoneyPaidSDat(&$db, &$model, $year)
    {
        $query = " UPDATE ";
        $query .= "     MONEY_PAID_S_DAT T1 ";
        $query .= " SET ";
        $query .= "     ( PAID_MONEY_DATE, PAID_MONEY, PAID_MONEY_DIV, UPDATED)=(NULL,NULL,NULL,SYSDATE()) ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.PAID_INPUT_FLG = '1' AND ";
        $query .= "     (T1.SCHREGNO,T1.EXPENSE_M_CD) NOT IN (SELECT ";
        $query .= "         ST1.SCHREGNO, ";
        $query .= "         ST2.EXPENSE_M_CD ";
        $query .= "     FROM ";
        $query .= "         V_DEBUG01_BANK_RESULT_TMP_DATA ST1 , ";
        $query .= "         (SELECT ";
        $query .= "             DENSERANK() OVER(ORDER BY BANK_TRANS_SDATE) AS TERM, ";
        $query .= "             ST1.EXPENSE_L_CD, ";
        $query .= "             ST1.EXPENSE_M_CD ";
        $query .= "         FROM ";
        $query .= "             EXPENSE_M_MST ST1 ";
        $query .= "         WHERE ";
        $query .= "             ST1.YEAR = '".CTRL_YEAR."' ";
        $query .= "         ) ST2 ";
        $query .= "     WHERE ";
        $query .= "         NOT EXISTS(SELECT ";
        $query .= "                         * ";
        $query .= "                    FROM ";
        $query .= "                         BANK_RESULT_TMP_DATA3 E1 ";
        $query .= "                    WHERE ";
        $query .= "                         E1.FILE_LINE_NUMBER = ST1.FILE_LINE_NUMBER AND ";
        $query .= "                         (E1.PROCESS1_STS = '1' OR E1.PROCESS2_STS = '1' OR E1.PROCESS3_STS = '1') ";
        $query .= "                   ) AND ";
        $query .= "         YEAR(ST1.TERM_PAID_MONEY_DATE) = ".CTRL_YEAR." AND ";
        $query .= "         MONTH(ST1.TERM_PAID_MONEY_DATE) = ".$model->paid_month." AND ";
        $query .= "         INTEGER(ST1.TERM) = ST2.TERM AND ";
        $query .= "         ST1.EXPENSE = ST2.EXPENSE_L_CD ";
        $query .= "     ) ";
        if ($model->schooldiv == 0) {
            $query .= "     AND NOT EXISTS( SELECT ";
            $query .= "                         * ";
            $query .= "                     FROM ";
            $query .= "                         FRESHMAN_DAT EX2 ";
            $query .= "                     WHERE ";
            $query .= "                         EX2.ENTERYEAR = '".CTRL_YEAR."' ";
            $query .= "                         AND EX2.SCHREGNO = T1.SCHREGNO ";
            $query .= "                   ) ";
        }

        $db->query($query);
    }
    //三段目追加分データのカウント用 NO006
    function UpdateBankMstCnt(&$db, &$model)
    {
        //二段目エラーも取り込まない為
        $query = " UPDATE ";
        $query .= "     BANK_RESULT_TMP_DATA3 T1 ";
        $query .= " SET ";
        $query .= "     PROCESS3_STS = '1',PROCESS3_UPDATED = SYSDATE() ";
        $query .= " WHERE ";
        $query .= "     T1.BANKCD    IS NOT NULL AND ";
        $query .= "     T1.BRANCHCD  IS NOT NULL AND ";
        $query .= "     EXISTS ( ";
        $query .= "         SELECT 'x' ";
        $query .= "         FROM ";
        $query .= "             BANK_RESULT_ERR_DATA ST1 ";
        $query .= "         WHERE ";
        $query .= "             ST1.PROCESSCD = '2' ";
        $query .= "             AND ST1.FILE_LINE_NUMBER = T1.FILE_LINE_NUMBER ";
        $query .= "     )   ";

        $db->query($query);

        //三段目エラー
        $query = " UPDATE ";
        $query .= "     BANK_RESULT_TMP_DATA3 T1 ";
        $query .= " SET ";
        $query .= "     PROCESS3_STS = '1',PROCESS3_UPDATED = SYSDATE() ";
        $query .= " WHERE ";
        $query .= "     T1.BANKCD    IS NOT NULL AND ";
        $query .= "     T1.BRANCHCD  IS NOT NULL AND ";
        $query .= "     EXISTS ( ";
        $query .= "         SELECT 'x' ";
        $query .= "         FROM ";
        $query .= "             BANK_RESULT_ERR_DATA ST1 ";
        $query .= "         WHERE ";
        $query .= "             ST1.PROCESSCD = '3' ";
        $query .= "             AND ST1.FILE_LINE_NUMBER = T1.FILE_LINE_NUMBER ";
        $query .= "     )   ";

        $db->query($query);

        $query = " UPDATE ";
        $query .= "     BANK_RESULT_TMP_DATA3 T1 ";
        $query .= " SET ";
        $query .= "     PROCESS3_STS = '0',PROCESS3_UPDATED = SYSDATE() ";
        $query .= " WHERE ";
        $query .= "     T1.BANKCD    IS NOT NULL AND ";
        $query .= "     T1.BRANCHCD  IS NOT NULL AND ";
        $query .= "     VALUE(T1.PROCESS3_STS,'0') != '1' ";

        $db->query($query);

    }
    //銀行マスタ登録
    function InsertBankMst(&$db, &$model)
    {
        $query = " INSERT INTO BANK_MST( ";
        $query .= " BANKCD, ";
        $query .= " BRANCHCD, ";
        $query .= " BANKNAME_KANA, ";
        $query .= " BRANCHNAME_KANA, ";
        $query .= " REGISTERCD, ";
        $query .= " UPDATED ";
        $query .= " ) ";
        $query .= "   (SELECT DISTINCT ";
        $query .= "         BANKCD, ";
        $query .= "         BRANCHCD, ";
        $query .= "         BANKNAME_KANA, ";
        $query .= "         BRANCHNAME_KANA, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         SYSDATE() ";
        $query .= "     FROM ";
        $query .= "         BANK_RESULT_TMP_DATA3 T1 ";
        $query .= "     WHERE ";
        $query .= "         BANKCD    IS NOT NULL AND ";
        $query .= "         BRANCHCD  IS NOT NULL AND ";
        $query .= "         NOT EXISTS(SELECT ";
        $query .= "                         W.BANKCD ";
        $query .= "                     FROM ";
        $query .= "                         BANK_MST W ";
        $query .= "                     WHERE ";
        $query .= "                         W.BANKCD = T1.BANKCD AND ";
        $query .= "                         W.BRANCHCD = T1.BRANCHCD ";
        $query .= "                     ) ";
        $query .= "     )   ";

        $db->query($query);
    }
    //銀行マスタ更新
    function UpdateBankMst(&$db, &$model)
    {
        $query = " UPDATE ";
        $query .= "     BANK_MST T1 ";
        $query .= " SET ";
        $query .= "     ( BANKCD, BRANCHCD, BANKNAME_KANA, BRANCHNAME_KANA, REGISTERCD, UPDATED) =  ";
        $query .= "   (SELECT ";
        $query .= "         ST1.BANKCD, ";
        $query .= "         ST1.BRANCHCD, ";
        $query .= "         MAX(ST1.BANKNAME_KANA), ";
        $query .= "         MAX(ST1.BRANCHNAME_KANA), ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         SYSDATE() ";
        $query .= "     FROM ";
        $query .= "         BANK_RESULT_TMP_DATA3 ST1 ";
        $query .= "     WHERE ";
        $query .= "         ST1.BANKCD    IS NOT NULL AND ";
        $query .= "         ST1.BRANCHCD  IS NOT NULL AND ";
        $query .= "         T1.BANKCD = ST1.BANKCD AND ";
        $query .= "         T1.BRANCHCD = ST1.BRANCHCD ";
        $query .= "     GROUP BY ";
        $query .= "         ST1.BANKCD, ";
        $query .= "         ST1.BRANCHCD ";
        $query .= "     )   ";
        $query .= " WHERE EXISTS ( ";
        $query .= "       SELECT 'x' ";
        $query .= "       FROM ";
        $query .= "             BANK_RESULT_TMP_DATA3 ST2 ";
        $query .= "       WHERE ST2.BANKCD   IS NOT NULL ";
        $query .= "             AND ST2.BRANCHCD IS NOT NULL ";
        $query .= "             AND ST2.BANKCD = T1.BANKCD ";
        $query .= "             AND ST2.BRANCHCD = T1.BRANCHCD ) ";

        $db->query($query);
    }
    //登録銀行データ登録
    function InsertRegistBankDat(&$db, &$model)
    {
        $query = " INSERT INTO REGISTBANK_DAT( ";
        $query .= " SCHREGNO, ";
        $query .= " BANKCD, ";
        $query .= " BRANCHCD, ";
        $query .= " DEPOSIT_ITEM, ";
        $query .= " ACCOUNTNO, ";
        $query .= " ACCOUNTNAME, ";
        $query .= " REGISTERCD, ";
        $query .= " UPDATED ";
        $query .= " ) ";
        $query .= "  (SELECT DISTINCT ";
        $query .= "         SCHREGNO, ";
        $query .= "         BANKCD, ";
        $query .= "         BRANCHCD, ";
        $query .= "         DEPOSIT_ITEM, ";
        $query .= "         ACCOUNTNO, ";
        $query .= "         ACCOUNTNAME, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         SYSDATE() ";
        $query .= "     FROM ";
        $query .= "         BANK_RESULT_TMP_DATA3 T1 ";
        $query .= "     WHERE ";
        $query .= "         BANKCD   IS NOT NULL AND ";
        $query .= "         BRANCHCD IS NOT NULL AND ";
        $query .= "         NOT EXISTS(SELECT ";
        $query .= "                         * ";
        $query .= "                    FROM ";
        $query .= "                         BANK_RESULT_TMP_DATA3 E1 ";
        $query .= "                    WHERE ";
        $query .= "                         E1.FILE_LINE_NUMBER = T1.FILE_LINE_NUMBER AND ";
        $query .= "                         (E1.PROCESS1_STS = '1' OR E1.PROCESS2_STS = '1' OR E1.PROCESS3_STS = '1') ";
        $query .= "                   ) AND ";
        $query .= "         NOT EXISTS(SELECT ";
        $query .= "                         W.SCHREGNO ";
        $query .= "                     FROM ";
        $query .= "                         REGISTBANK_DAT W ";
        $query .= "                     WHERE ";
        $query .= "                         W.SCHREGNO = T1.SCHREGNO ";
        $query .= "                     ) ";
        $query .= "     )   ";

        $db->query($query);
    }
    //登録銀行データ更新
    function UpdateRegistBankDat(&$db, &$model)
    {
        $query = " UPDATE ";
        $query .= "     REGISTBANK_DAT T1 ";
        $query .= " SET ";
        $query .= "     ( BANKCD, BRANCHCD, DEPOSIT_ITEM, ACCOUNTNO, ACCOUNTNAME, REGISTERCD, UPDATED) =  ";
        $query .= "     (SELECT DISTINCT ";
        $query .= "             BANKCD, ";
        $query .= "             BRANCHCD, ";
        $query .= "             DEPOSIT_ITEM, ";
        $query .= "             ACCOUNTNO, ";
        $query .= "             ACCOUNTNAME, ";
        $query .= "             '".STAFFCD."', ";
        $query .= "             SYSDATE() ";
        $query .= "         FROM ";
        $query .= "             BANK_RESULT_TMP_DATA3 ST1 ";
        $query .= "        WHERE ";
        $query .= "            ST1.SCHREGNO = T1.SCHREGNO ";
        $query .= "            AND BANKCD   IS NOT NULL ";
        $query .= "            AND BRANCHCD IS NOT NULL ";
        $query .= "            AND NOT EXISTS(SELECT ";
        $query .= "                               * ";
        $query .= "                           FROM ";
        $query .= "                               BANK_RESULT_TMP_DATA3 E1 ";
        $query .= "                           WHERE ";
        $query .= "                               E1.FILE_LINE_NUMBER = ST1.FILE_LINE_NUMBER AND ";
        $query .= "                               (E1.PROCESS1_STS = '1' OR E1.PROCESS2_STS = '1' OR E1.PROCESS3_STS = '1') ";
        $query .= "                          ) ";
        $query .= "         ) ";
        $query .= " WHERE T1.SCHREGNO IN( ";
        $query .= "       SELECT DISTINCT SCHREGNO ";
        $query .= "         FROM ";
        $query .= "             BANK_RESULT_TMP_DATA3 ST1 ";
        $query .= "        WHERE ";
        $query .= "            BANKCD IS NOT NULL ";
        $query .= "            AND BRANCHCD IS NOT NULL ";
        $query .= "            AND NOT EXISTS(SELECT ";
        $query .= "                               * ";
        $query .= "                           FROM ";
        $query .= "                               BANK_RESULT_TMP_DATA3 E1 ";
        $query .= "                           WHERE ";
        $query .= "                               E1.FILE_LINE_NUMBER = ST1.FILE_LINE_NUMBER AND ";
        $query .= "                               (E1.PROCESS1_STS = '1' OR E1.PROCESS2_STS = '1' OR E1.PROCESS3_STS = '1') ";
        $query .= "                          ) ";
        $query .= "         ) ";

        $db->query($query);
    }


    //学校コード取得
    function getSchoolcode()
    {
        $query  = " SELECT var1 ";
        $query .= "   FROM school_expenses_sys_ini ";
        $query .= "  WHERE programid = 'KNJP000K' ";
        $query .= "    AND div       = '0001' ";
        return $query;
    }

    //チェック処理前に処理結果情報更新
    function UpdateTempDataProcess($process,$arg1,$arg2){
    
        $data = array();
        $data["PROCESS".$process ."_UPDATED"][FUNC] = $arg1;
        $data["PROCESS".$process ."_STS"][FUNC]     = $arg2;

        $query = Query::updateSQL($data, "BANK_RESULT_TMP_DATA", $where);
        return $query;
    }

    //TMP2にデータコピー NO008
    function InsertDat2(&$db, &$model)
    {
        $query = " INSERT INTO BANK_RESULT_TMP_DATA2 ";
        $query .= "       SELECT * FROM BANK_RESULT_TMP_DATA ";
        $query .= "       WHERE FILE_LINE_NUMBER IN ( SELECT FILE_LINE_NUMBER ";
        $query .= "                                   FROM (SELECT SCHREGNO,MAX(FILE_LINE_NUMBER) AS FILE_LINE_NUMBER ";
        $query .= "                                         FROM BANK_RESULT_TMP_DATA ";
        $query .= "                                         GROUP BY SCHREGNO ) T1 ";
        $query .= "                                 ) ";

        $db->query($query);
    }

    //TMP2削除 NO008
    function DeleteDat2(&$db, &$model)
    {
        $query = " DELETE FROM BANK_RESULT_TMP_DATA2 ";

        $db->query($query);
    }

    //TMP3にデータコピー NO008
    function InsertDat3(&$db, &$model)
    {
        $query = " INSERT INTO BANK_RESULT_TMP_DATA3 ";
        $query .= "       SELECT ";
        $query .= "           ST1.FILE_LINE_NUMBER, ";
        $query .= "           ST1.PROCESS1_UPDATED, ";
        $query .= "           ST1.PROCESS1_STS, ";
        $query .= "           ST1.PROCESS2_UPDATED, ";
        $query .= "           ST1.PROCESS2_STS, ";
        $query .= "           ST1.PROCESS3_UPDATED, ";
        $query .= "           ST1.PROCESS3_STS, ";
        $query .= "           ST1.DATA_DATE, ";
        $query .= "           ST1.SCHOOL_CODE, ";
        $query .= "           ST1.COURSE_CODE, ";
        $query .= "           ST1.SCHREGNO, ";
        $query .= "           ST1.BIRTHDAY, ";
        $query .= "           ST1.NAME_KANA, ";
        $query .= "           ST1.NAME, ";
        $query .= "           ST1.PAYER_NAME_KANA, ";
        $query .= "           ST1.PAYER_NAME, ";
        $query .= "           ST1.PAYER_TELNO, ";
        $query .= "           ST1.BANKCD, ";
        $query .= "           ST1.BRANCHCD, ";
        $query .= "           ST1.BANKNAME_KANA, ";
        $query .= "           ST1.BRANCHNAME_KANA, ";
        $query .= "           ST1.DEPOSIT_ITEM, ";
        $query .= "           ST1.ACCOUNTNO, ";
        $query .= "           ST1.ACCOUNTNAME, ";
        $query .= "           ST1.EXPENSE01_PAID_MONEY, ";
        $query .= "           ST1.EXPENSE01_NECE_MONEY, ";
        $query .= "           ST1.EXPENSE02_PAID_MONEY, ";
        $query .= "           ST1.EXPENSE02_NECE_MONEY, ";
        $query .= "           ST1.EXPENSE03_PAID_MONEY, ";
        $query .= "           ST1.EXPENSE03_NECE_MONEY, ";
        $query .= "           ST1.EXPENSE04_PAID_MONEY, ";
        $query .= "           ST1.EXPENSE04_NECE_MONEY, ";
        $query .= "           ST1.EXPENSE05_PAID_MONEY, ";
        $query .= "           ST1.EXPENSE05_NECE_MONEY, ";
        $query .= "           ST1.EXPENSE06_PAID_MONEY, ";
        $query .= "           ST1.EXPENSE06_NECE_MONEY, ";
        $query .= "           CASE WHEN ST1.TERM01_EXPENSE01_MONEY_DUE = ST2.TERM01_EXPENSE01_MONEY_DUE ";
        $query .= "                     AND ST1.TERM01_PAID_MONEY_DATE = ST2.TERM01_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM01_EXPENSE01_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM01_EXPENSE02_MONEY_DUE = ST2.TERM01_EXPENSE02_MONEY_DUE ";
        $query .= "                     AND ST1.TERM01_PAID_MONEY_DATE = ST2.TERM01_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM01_EXPENSE02_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM01_EXPENSE03_MONEY_DUE = ST2.TERM01_EXPENSE03_MONEY_DUE ";
        $query .= "                     AND ST1.TERM01_PAID_MONEY_DATE = ST2.TERM01_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM01_EXPENSE03_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM01_EXPENSE04_MONEY_DUE = ST2.TERM01_EXPENSE04_MONEY_DUE ";
        $query .= "                     AND ST1.TERM01_PAID_MONEY_DATE = ST2.TERM01_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM01_EXPENSE04_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM01_EXPENSE05_MONEY_DUE = ST2.TERM01_EXPENSE05_MONEY_DUE ";
        $query .= "                     AND ST1.TERM01_PAID_MONEY_DATE = ST2.TERM01_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM01_EXPENSE05_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM01_EXPENSE06_MONEY_DUE = ST2.TERM01_EXPENSE06_MONEY_DUE ";
        $query .= "                     AND ST1.TERM01_PAID_MONEY_DATE = ST2.TERM01_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM01_EXPENSE06_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM01_SUM_MONEY_DUE = ST2.TERM01_SUM_MONEY_DUE ";
        $query .= "                     AND ST1.TERM01_PAID_MONEY_DATE = ST2.TERM01_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM01_SUM_MONEY_DUE END, ";
        $query .= "           ST1.TERM01_FLAG01, ";
        $query .= "           ST1.TERM01_FLAG02, ";
        $query .= "           ST1.TERM01_FLAG03, ";
        $query .= "           ST1.TERM01_FLAG04, ";
        $query .= "           ST1.TERM01_FLAG05, ";
        $query .= "           ST1.TERM01_PAID_MONEY_DATE, ";
        $query .= "           ST1.TERM01_FURI_MONEY_DATE, ";
        $query .= "           CASE WHEN ST1.TERM02_EXPENSE01_MONEY_DUE = ST2.TERM02_EXPENSE01_MONEY_DUE ";
        $query .= "                     AND ST1.TERM02_PAID_MONEY_DATE = ST2.TERM02_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM02_EXPENSE01_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM02_EXPENSE02_MONEY_DUE = ST2.TERM02_EXPENSE02_MONEY_DUE ";
        $query .= "                     AND ST1.TERM02_PAID_MONEY_DATE = ST2.TERM02_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM02_EXPENSE02_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM02_EXPENSE03_MONEY_DUE = ST2.TERM02_EXPENSE03_MONEY_DUE ";
        $query .= "                     AND ST1.TERM02_PAID_MONEY_DATE = ST2.TERM02_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM02_EXPENSE03_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM02_EXPENSE04_MONEY_DUE = ST2.TERM02_EXPENSE04_MONEY_DUE ";
        $query .= "                     AND ST1.TERM02_PAID_MONEY_DATE = ST2.TERM02_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM02_EXPENSE04_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM02_EXPENSE05_MONEY_DUE = ST2.TERM02_EXPENSE05_MONEY_DUE ";
        $query .= "                     AND ST1.TERM02_PAID_MONEY_DATE = ST2.TERM02_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM02_EXPENSE05_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM02_EXPENSE06_MONEY_DUE = ST2.TERM02_EXPENSE06_MONEY_DUE ";
        $query .= "                     AND ST1.TERM02_PAID_MONEY_DATE = ST2.TERM02_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM02_EXPENSE06_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM02_SUM_MONEY_DUE = ST2.TERM02_SUM_MONEY_DUE ";
        $query .= "                     AND ST1.TERM02_PAID_MONEY_DATE = ST2.TERM02_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM02_SUM_MONEY_DUE END, ";
        $query .= "           ST1.TERM02_FLAG01, ";
        $query .= "           ST1.TERM02_FLAG02, ";
        $query .= "           ST1.TERM02_FLAG03, ";
        $query .= "           ST1.TERM02_FLAG04, ";
        $query .= "           ST1.TERM02_FLAG05, ";
        $query .= "           ST1.TERM02_PAID_MONEY_DATE, ";
        $query .= "           ST1.TERM02_FURI_MONEY_DATE, ";
        $query .= "           CASE WHEN ST1.TERM03_EXPENSE01_MONEY_DUE = ST2.TERM03_EXPENSE01_MONEY_DUE ";
        $query .= "                     AND ST1.TERM03_PAID_MONEY_DATE = ST2.TERM03_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM03_EXPENSE01_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM03_EXPENSE02_MONEY_DUE = ST2.TERM03_EXPENSE02_MONEY_DUE ";
        $query .= "                     AND ST1.TERM03_PAID_MONEY_DATE = ST2.TERM03_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM03_EXPENSE02_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM03_EXPENSE03_MONEY_DUE = ST2.TERM03_EXPENSE03_MONEY_DUE ";
        $query .= "                     AND ST1.TERM03_PAID_MONEY_DATE = ST2.TERM03_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM03_EXPENSE03_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM03_EXPENSE04_MONEY_DUE = ST2.TERM03_EXPENSE04_MONEY_DUE ";
        $query .= "                     AND ST1.TERM03_PAID_MONEY_DATE = ST2.TERM03_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM03_EXPENSE04_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM03_EXPENSE05_MONEY_DUE = ST2.TERM03_EXPENSE05_MONEY_DUE ";
        $query .= "                     AND ST1.TERM03_PAID_MONEY_DATE = ST2.TERM03_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM03_EXPENSE05_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM03_EXPENSE06_MONEY_DUE = ST2.TERM03_EXPENSE06_MONEY_DUE ";
        $query .= "                     AND ST1.TERM03_PAID_MONEY_DATE = ST2.TERM03_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM03_EXPENSE06_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM03_SUM_MONEY_DUE = ST2.TERM03_SUM_MONEY_DUE ";
        $query .= "                     AND ST1.TERM03_PAID_MONEY_DATE = ST2.TERM03_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM03_SUM_MONEY_DUE END, ";
        $query .= "           ST1.TERM03_FLAG01, ";
        $query .= "           ST1.TERM03_FLAG02, ";
        $query .= "           ST1.TERM03_FLAG03, ";
        $query .= "           ST1.TERM03_FLAG04, ";
        $query .= "           ST1.TERM03_FLAG05, ";
        $query .= "           ST1.TERM03_PAID_MONEY_DATE, ";
        $query .= "           ST1.TERM03_FURI_MONEY_DATE, ";
        $query .= "           CASE WHEN ST1.TERM04_EXPENSE01_MONEY_DUE = ST2.TERM04_EXPENSE01_MONEY_DUE ";
        $query .= "                     AND ST1.TERM04_PAID_MONEY_DATE = ST2.TERM04_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM04_EXPENSE01_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM04_EXPENSE02_MONEY_DUE = ST2.TERM04_EXPENSE02_MONEY_DUE ";
        $query .= "                     AND ST1.TERM04_PAID_MONEY_DATE = ST2.TERM04_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM04_EXPENSE02_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM04_EXPENSE03_MONEY_DUE = ST2.TERM04_EXPENSE03_MONEY_DUE ";
        $query .= "                     AND ST1.TERM04_PAID_MONEY_DATE = ST2.TERM04_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM04_EXPENSE03_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM04_EXPENSE04_MONEY_DUE = ST2.TERM04_EXPENSE04_MONEY_DUE ";
        $query .= "                     AND ST1.TERM04_PAID_MONEY_DATE = ST2.TERM04_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM04_EXPENSE04_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM04_EXPENSE05_MONEY_DUE = ST2.TERM04_EXPENSE05_MONEY_DUE ";
        $query .= "                     AND ST1.TERM04_PAID_MONEY_DATE = ST2.TERM04_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM04_EXPENSE05_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM04_EXPENSE06_MONEY_DUE = ST2.TERM04_EXPENSE06_MONEY_DUE ";
        $query .= "                     AND ST1.TERM04_PAID_MONEY_DATE = ST2.TERM04_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM04_EXPENSE06_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM04_SUM_MONEY_DUE = ST2.TERM04_SUM_MONEY_DUE ";
        $query .= "                     AND ST1.TERM04_PAID_MONEY_DATE = ST2.TERM04_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM04_SUM_MONEY_DUE END, ";
        $query .= "           ST1.TERM04_FLAG01, ";
        $query .= "           ST1.TERM04_FLAG02, ";
        $query .= "           ST1.TERM04_FLAG03, ";
        $query .= "           ST1.TERM04_FLAG04, ";
        $query .= "           ST1.TERM04_FLAG05, ";
        $query .= "           ST1.TERM04_PAID_MONEY_DATE, ";
        $query .= "           ST1.TERM04_FURI_MONEY_DATE, ";
        $query .= "           CASE WHEN ST1.TERM05_EXPENSE01_MONEY_DUE = ST2.TERM05_EXPENSE01_MONEY_DUE ";
        $query .= "                     AND ST1.TERM05_PAID_MONEY_DATE = ST2.TERM05_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM05_EXPENSE01_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM05_EXPENSE02_MONEY_DUE = ST2.TERM05_EXPENSE02_MONEY_DUE ";
        $query .= "                     AND ST1.TERM05_PAID_MONEY_DATE = ST2.TERM05_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM05_EXPENSE02_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM05_EXPENSE03_MONEY_DUE = ST2.TERM05_EXPENSE03_MONEY_DUE ";
        $query .= "                     AND ST1.TERM05_PAID_MONEY_DATE = ST2.TERM05_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM05_EXPENSE03_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM05_EXPENSE04_MONEY_DUE = ST2.TERM05_EXPENSE04_MONEY_DUE ";
        $query .= "                     AND ST1.TERM05_PAID_MONEY_DATE = ST2.TERM05_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM05_EXPENSE04_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM05_EXPENSE05_MONEY_DUE = ST2.TERM05_EXPENSE05_MONEY_DUE ";
        $query .= "                     AND ST1.TERM05_PAID_MONEY_DATE = ST2.TERM05_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM05_EXPENSE05_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM05_EXPENSE06_MONEY_DUE = ST2.TERM05_EXPENSE06_MONEY_DUE ";
        $query .= "                     AND ST1.TERM05_PAID_MONEY_DATE = ST2.TERM05_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM05_EXPENSE06_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM05_SUM_MONEY_DUE = ST2.TERM05_SUM_MONEY_DUE ";
        $query .= "                     AND ST1.TERM05_PAID_MONEY_DATE = ST2.TERM05_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM05_SUM_MONEY_DUE END, ";
        $query .= "           ST1.TERM05_FLAG01, ";
        $query .= "           ST1.TERM05_FLAG02, ";
        $query .= "           ST1.TERM05_FLAG03, ";
        $query .= "           ST1.TERM05_FLAG04, ";
        $query .= "           ST1.TERM05_FLAG05, ";
        $query .= "           ST1.TERM05_PAID_MONEY_DATE, ";
        $query .= "           ST1.TERM05_FURI_MONEY_DATE, ";
        $query .= "           CASE WHEN ST1.TERM06_EXPENSE01_MONEY_DUE = ST2.TERM06_EXPENSE01_MONEY_DUE ";
        $query .= "                     AND ST1.TERM06_PAID_MONEY_DATE = ST2.TERM06_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM06_EXPENSE01_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM06_EXPENSE02_MONEY_DUE = ST2.TERM06_EXPENSE02_MONEY_DUE ";
        $query .= "                     AND ST1.TERM06_PAID_MONEY_DATE = ST2.TERM06_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM06_EXPENSE02_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM06_EXPENSE03_MONEY_DUE = ST2.TERM06_EXPENSE03_MONEY_DUE ";
        $query .= "                     AND ST1.TERM06_PAID_MONEY_DATE = ST2.TERM06_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM06_EXPENSE03_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM06_EXPENSE04_MONEY_DUE = ST2.TERM06_EXPENSE04_MONEY_DUE ";
        $query .= "                     AND ST1.TERM06_PAID_MONEY_DATE = ST2.TERM06_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM06_EXPENSE04_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM06_EXPENSE05_MONEY_DUE = ST2.TERM06_EXPENSE05_MONEY_DUE ";
        $query .= "                     AND ST1.TERM06_PAID_MONEY_DATE = ST2.TERM06_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM06_EXPENSE05_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM06_EXPENSE06_MONEY_DUE = ST2.TERM06_EXPENSE06_MONEY_DUE ";
        $query .= "                     AND ST1.TERM06_PAID_MONEY_DATE = ST2.TERM06_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM06_EXPENSE06_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM06_SUM_MONEY_DUE = ST2.TERM06_SUM_MONEY_DUE ";
        $query .= "                     AND ST1.TERM06_PAID_MONEY_DATE = ST2.TERM06_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM06_SUM_MONEY_DUE END, ";
        $query .= "           ST1.TERM06_FLAG01, ";
        $query .= "           ST1.TERM06_FLAG02, ";
        $query .= "           ST1.TERM06_FLAG03, ";
        $query .= "           ST1.TERM06_FLAG04, ";
        $query .= "           ST1.TERM06_FLAG05, ";
        $query .= "           ST1.TERM06_PAID_MONEY_DATE, ";
        $query .= "           ST1.TERM06_FURI_MONEY_DATE, ";
        $query .= "           CASE WHEN ST1.TERM07_EXPENSE01_MONEY_DUE = ST2.TERM07_EXPENSE01_MONEY_DUE ";
        $query .= "                     AND ST1.TERM07_PAID_MONEY_DATE = ST2.TERM07_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM07_EXPENSE01_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM07_EXPENSE02_MONEY_DUE = ST2.TERM07_EXPENSE02_MONEY_DUE ";
        $query .= "                     AND ST1.TERM07_PAID_MONEY_DATE = ST2.TERM07_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM07_EXPENSE02_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM07_EXPENSE03_MONEY_DUE = ST2.TERM07_EXPENSE03_MONEY_DUE ";
        $query .= "                     AND ST1.TERM07_PAID_MONEY_DATE = ST2.TERM07_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM07_EXPENSE03_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM07_EXPENSE04_MONEY_DUE = ST2.TERM07_EXPENSE04_MONEY_DUE ";
        $query .= "                     AND ST1.TERM07_PAID_MONEY_DATE = ST2.TERM07_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM07_EXPENSE04_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM07_EXPENSE05_MONEY_DUE = ST2.TERM07_EXPENSE05_MONEY_DUE ";
        $query .= "                     AND ST1.TERM07_PAID_MONEY_DATE = ST2.TERM07_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM07_EXPENSE05_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM07_EXPENSE06_MONEY_DUE = ST2.TERM07_EXPENSE06_MONEY_DUE ";
        $query .= "                     AND ST1.TERM07_PAID_MONEY_DATE = ST2.TERM07_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM07_EXPENSE06_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM07_SUM_MONEY_DUE = ST2.TERM07_SUM_MONEY_DUE ";
        $query .= "                     AND ST1.TERM07_PAID_MONEY_DATE = ST2.TERM07_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM07_SUM_MONEY_DUE END, ";
        $query .= "           ST1.TERM07_FLAG01, ";
        $query .= "           ST1.TERM07_FLAG02, ";
        $query .= "           ST1.TERM07_FLAG03, ";
        $query .= "           ST1.TERM07_FLAG04, ";
        $query .= "           ST1.TERM07_FLAG05, ";
        $query .= "           ST1.TERM07_PAID_MONEY_DATE, ";
        $query .= "           ST1.TERM07_FURI_MONEY_DATE, ";
        $query .= "           CASE WHEN ST1.TERM08_EXPENSE01_MONEY_DUE = ST2.TERM08_EXPENSE01_MONEY_DUE ";
        $query .= "                     AND ST1.TERM08_PAID_MONEY_DATE = ST2.TERM08_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM08_EXPENSE01_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM08_EXPENSE02_MONEY_DUE = ST2.TERM08_EXPENSE02_MONEY_DUE ";
        $query .= "                     AND ST1.TERM08_PAID_MONEY_DATE = ST2.TERM08_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM08_EXPENSE02_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM08_EXPENSE03_MONEY_DUE = ST2.TERM08_EXPENSE03_MONEY_DUE ";
        $query .= "                     AND ST1.TERM08_PAID_MONEY_DATE = ST2.TERM08_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM08_EXPENSE03_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM08_EXPENSE04_MONEY_DUE = ST2.TERM08_EXPENSE04_MONEY_DUE ";
        $query .= "                     AND ST1.TERM08_PAID_MONEY_DATE = ST2.TERM08_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM08_EXPENSE04_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM08_EXPENSE05_MONEY_DUE = ST2.TERM08_EXPENSE05_MONEY_DUE ";
        $query .= "                     AND ST1.TERM08_PAID_MONEY_DATE = ST2.TERM08_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM08_EXPENSE05_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM08_EXPENSE06_MONEY_DUE = ST2.TERM08_EXPENSE06_MONEY_DUE ";
        $query .= "                     AND ST1.TERM08_PAID_MONEY_DATE = ST2.TERM08_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM08_EXPENSE06_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM08_SUM_MONEY_DUE = ST2.TERM08_SUM_MONEY_DUE ";
        $query .= "                     AND ST1.TERM08_PAID_MONEY_DATE = ST2.TERM08_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM08_SUM_MONEY_DUE END, ";
        $query .= "           ST1.TERM08_FLAG01, ";
        $query .= "           ST1.TERM08_FLAG02, ";
        $query .= "           ST1.TERM08_FLAG03, ";
        $query .= "           ST1.TERM08_FLAG04, ";
        $query .= "           ST1.TERM08_FLAG05, ";
        $query .= "           ST1.TERM08_PAID_MONEY_DATE, ";
        $query .= "           ST1.TERM08_FURI_MONEY_DATE, ";
        $query .= "           CASE WHEN ST1.TERM09_EXPENSE01_MONEY_DUE = ST2.TERM09_EXPENSE01_MONEY_DUE ";
        $query .= "                     AND ST1.TERM09_PAID_MONEY_DATE = ST2.TERM09_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM09_EXPENSE01_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM09_EXPENSE02_MONEY_DUE = ST2.TERM09_EXPENSE02_MONEY_DUE ";
        $query .= "                     AND ST1.TERM09_PAID_MONEY_DATE = ST2.TERM09_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM09_EXPENSE02_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM09_EXPENSE03_MONEY_DUE = ST2.TERM09_EXPENSE03_MONEY_DUE ";
        $query .= "                     AND ST1.TERM09_PAID_MONEY_DATE = ST2.TERM09_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM09_EXPENSE03_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM09_EXPENSE04_MONEY_DUE = ST2.TERM09_EXPENSE04_MONEY_DUE ";
        $query .= "                     AND ST1.TERM09_PAID_MONEY_DATE = ST2.TERM09_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM09_EXPENSE04_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM09_EXPENSE05_MONEY_DUE = ST2.TERM09_EXPENSE05_MONEY_DUE ";
        $query .= "                     AND ST1.TERM09_PAID_MONEY_DATE = ST2.TERM09_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM09_EXPENSE05_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM09_EXPENSE06_MONEY_DUE = ST2.TERM09_EXPENSE06_MONEY_DUE ";
        $query .= "                     AND ST1.TERM09_PAID_MONEY_DATE = ST2.TERM09_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM09_EXPENSE06_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM09_SUM_MONEY_DUE = ST2.TERM09_SUM_MONEY_DUE ";
        $query .= "                     AND ST1.TERM09_PAID_MONEY_DATE = ST2.TERM09_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM09_SUM_MONEY_DUE END, ";
        $query .= "           ST1.TERM09_FLAG01, ";
        $query .= "           ST1.TERM09_FLAG02, ";
        $query .= "           ST1.TERM09_FLAG03, ";
        $query .= "           ST1.TERM09_FLAG04, ";
        $query .= "           ST1.TERM09_FLAG05, ";
        $query .= "           ST1.TERM09_PAID_MONEY_DATE, ";
        $query .= "           ST1.TERM09_FURI_MONEY_DATE, ";
        $query .= "           CASE WHEN ST1.TERM10_EXPENSE01_MONEY_DUE = ST2.TERM10_EXPENSE01_MONEY_DUE ";
        $query .= "                     AND ST1.TERM10_PAID_MONEY_DATE = ST2.TERM10_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM10_EXPENSE01_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM10_EXPENSE02_MONEY_DUE = ST2.TERM10_EXPENSE02_MONEY_DUE ";
        $query .= "                     AND ST1.TERM10_PAID_MONEY_DATE = ST2.TERM10_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM10_EXPENSE02_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM10_EXPENSE03_MONEY_DUE = ST2.TERM10_EXPENSE03_MONEY_DUE ";
        $query .= "                     AND ST1.TERM10_PAID_MONEY_DATE = ST2.TERM10_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM10_EXPENSE03_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM10_EXPENSE04_MONEY_DUE = ST2.TERM10_EXPENSE04_MONEY_DUE ";
        $query .= "                     AND ST1.TERM10_PAID_MONEY_DATE = ST2.TERM10_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM10_EXPENSE04_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM10_EXPENSE05_MONEY_DUE = ST2.TERM10_EXPENSE05_MONEY_DUE ";
        $query .= "                     AND ST1.TERM10_PAID_MONEY_DATE = ST2.TERM10_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM10_EXPENSE05_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM10_EXPENSE06_MONEY_DUE = ST2.TERM10_EXPENSE06_MONEY_DUE ";
        $query .= "                     AND ST1.TERM10_PAID_MONEY_DATE = ST2.TERM10_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM10_EXPENSE06_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM10_SUM_MONEY_DUE = ST2.TERM10_SUM_MONEY_DUE ";
        $query .= "                     AND ST1.TERM10_PAID_MONEY_DATE = ST2.TERM10_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM10_SUM_MONEY_DUE END, ";
        $query .= "           ST1.TERM10_FLAG01, ";
        $query .= "           ST1.TERM10_FLAG02, ";
        $query .= "           ST1.TERM10_FLAG03, ";
        $query .= "           ST1.TERM10_FLAG04, ";
        $query .= "           ST1.TERM10_FLAG05, ";
        $query .= "           ST1.TERM10_PAID_MONEY_DATE, ";
        $query .= "           ST1.TERM10_FURI_MONEY_DATE, ";
        $query .= "           CASE WHEN ST1.TERM11_EXPENSE01_MONEY_DUE = ST2.TERM11_EXPENSE01_MONEY_DUE ";
        $query .= "                     AND ST1.TERM11_PAID_MONEY_DATE = ST2.TERM11_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM11_EXPENSE01_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM11_EXPENSE02_MONEY_DUE = ST2.TERM11_EXPENSE02_MONEY_DUE ";
        $query .= "                     AND ST1.TERM11_PAID_MONEY_DATE = ST2.TERM11_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM11_EXPENSE02_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM11_EXPENSE03_MONEY_DUE = ST2.TERM11_EXPENSE03_MONEY_DUE ";
        $query .= "                     AND ST1.TERM11_PAID_MONEY_DATE = ST2.TERM11_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM11_EXPENSE03_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM11_EXPENSE04_MONEY_DUE = ST2.TERM11_EXPENSE04_MONEY_DUE ";
        $query .= "                     AND ST1.TERM11_PAID_MONEY_DATE = ST2.TERM11_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM11_EXPENSE04_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM11_EXPENSE05_MONEY_DUE = ST2.TERM11_EXPENSE05_MONEY_DUE ";
        $query .= "                     AND ST1.TERM11_PAID_MONEY_DATE = ST2.TERM11_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM11_EXPENSE05_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM11_EXPENSE06_MONEY_DUE = ST2.TERM11_EXPENSE06_MONEY_DUE ";
        $query .= "                     AND ST1.TERM11_PAID_MONEY_DATE = ST2.TERM11_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM11_EXPENSE06_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM11_SUM_MONEY_DUE = ST2.TERM11_SUM_MONEY_DUE ";
        $query .= "                     AND ST1.TERM11_PAID_MONEY_DATE = ST2.TERM11_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM11_SUM_MONEY_DUE END, ";
        $query .= "           ST1.TERM11_FLAG01, ";
        $query .= "           ST1.TERM11_FLAG02, ";
        $query .= "           ST1.TERM11_FLAG03, ";
        $query .= "           ST1.TERM11_FLAG04, ";
        $query .= "           ST1.TERM11_FLAG05, ";
        $query .= "           ST1.TERM11_PAID_MONEY_DATE, ";
        $query .= "           ST1.TERM11_FURI_MONEY_DATE, ";
        $query .= "           CASE WHEN ST1.TERM12_EXPENSE01_MONEY_DUE = ST2.TERM12_EXPENSE01_MONEY_DUE ";
        $query .= "                     AND ST1.TERM12_PAID_MONEY_DATE = ST2.TERM12_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM12_EXPENSE01_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM12_EXPENSE02_MONEY_DUE = ST2.TERM12_EXPENSE02_MONEY_DUE ";
        $query .= "                     AND ST1.TERM12_PAID_MONEY_DATE = ST2.TERM12_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM12_EXPENSE02_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM12_EXPENSE03_MONEY_DUE = ST2.TERM12_EXPENSE03_MONEY_DUE ";
        $query .= "                     AND ST1.TERM12_PAID_MONEY_DATE = ST2.TERM12_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM12_EXPENSE03_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM12_EXPENSE04_MONEY_DUE = ST2.TERM12_EXPENSE04_MONEY_DUE ";
        $query .= "                     AND ST1.TERM12_PAID_MONEY_DATE = ST2.TERM12_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM12_EXPENSE04_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM12_EXPENSE05_MONEY_DUE = ST2.TERM12_EXPENSE05_MONEY_DUE ";
        $query .= "                     AND ST1.TERM12_PAID_MONEY_DATE = ST2.TERM12_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM12_EXPENSE05_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM12_EXPENSE06_MONEY_DUE = ST2.TERM12_EXPENSE06_MONEY_DUE ";
        $query .= "                     AND ST1.TERM12_PAID_MONEY_DATE = ST2.TERM12_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM12_EXPENSE06_MONEY_DUE END, ";
        $query .= "           CASE WHEN ST1.TERM12_SUM_MONEY_DUE = ST2.TERM12_SUM_MONEY_DUE ";
        $query .= "                     AND ST1.TERM12_PAID_MONEY_DATE = ST2.TERM12_PAID_MONEY_DATE ";    //NO010
        $query .= "                THEN CAST(NULL AS INTEGER) ";
        $query .= "                ELSE ST1.TERM12_SUM_MONEY_DUE END, ";
        $query .= "           ST1.TERM12_FLAG01, ";
        $query .= "           ST1.TERM12_FLAG02, ";
        $query .= "           ST1.TERM12_FLAG03, ";
        $query .= "           ST1.TERM12_FLAG04, ";
        $query .= "           ST1.TERM12_FLAG05, ";
        $query .= "           ST1.TERM12_PAID_MONEY_DATE, ";
        $query .= "           ST1.TERM12_FURI_MONEY_DATE, ";
        $query .= "           ST1.EXTR_EXPENSE01_TRANS_SDATE, ";
        $query .= "           ST1.EXTR_EXPENSE01_MONEY, ";
        $query .= "           ST1.EXTR_EXPENSE01_FLAG01, ";
        $query .= "           ST1.EXTR_EXPENSE01_FLAG02, ";
        $query .= "           ST1.EXTR_EXPENSE01_FLAG03, ";
        $query .= "           ST1.EXTR_EXPENSE01_FLAG04, ";
        $query .= "           ST1.EXTR_EXPENSE01_FLAG05, ";
        $query .= "           ST1.EXTR_EXPENSE01_PAID_MONEY_DATE, ";
        $query .= "           ST1.EXTR_EXPENSE02_TRANS_SDATE, ";
        $query .= "           ST1.EXTR_EXPENSE02_MONEY, ";
        $query .= "           ST1.EXTR_EXPENSE02_FLAG01, ";
        $query .= "           ST1.EXTR_EXPENSE02_FLAG02, ";
        $query .= "           ST1.EXTR_EXPENSE02_FLAG03, ";
        $query .= "           ST1.EXTR_EXPENSE02_FLAG04, ";
        $query .= "           ST1.EXTR_EXPENSE02_FLAG05, ";
        $query .= "           ST1.EXTR_EXPENSE02_PAID_MONEY_DATE, ";
        $query .= "           ST1.EXTR_EXPENSE03_TRANS_SDATE, ";
        $query .= "           ST1.EXTR_EXPENSE03_MONEY, ";
        $query .= "           ST1.EXTR_EXPENSE03_FLAG01, ";
        $query .= "           ST1.EXTR_EXPENSE03_FLAG02, ";
        $query .= "           ST1.EXTR_EXPENSE03_FLAG03, ";
        $query .= "           ST1.EXTR_EXPENSE03_FLAG04, ";
        $query .= "           ST1.EXTR_EXPENSE03_FLAG05, ";
        $query .= "           ST1.EXTR_EXPENSE03_PAID_MONEY_DATE, ";
        $query .= "           ST1.EXTR_EXPENSE04_TRANS_SDATE, ";
        $query .= "           ST1.EXTR_EXPENSE04_MONEY, ";
        $query .= "           ST1.EXTR_EXPENSE04_FLAG01, ";
        $query .= "           ST1.EXTR_EXPENSE04_FLAG02, ";
        $query .= "           ST1.EXTR_EXPENSE04_FLAG03, ";
        $query .= "           ST1.EXTR_EXPENSE04_FLAG04, ";
        $query .= "           ST1.EXTR_EXPENSE04_FLAG05, ";
        $query .= "           ST1.EXTR_EXPENSE04_PAID_MONEY_DATE, ";
        $query .= "           ST1.EXTR_EXPENSE05_TRANS_SDATE, ";
        $query .= "           ST1.EXTR_EXPENSE05_MONEY, ";
        $query .= "           ST1.EXTR_EXPENSE05_FLAG01, ";
        $query .= "           ST1.EXTR_EXPENSE05_FLAG02, ";
        $query .= "           ST1.EXTR_EXPENSE05_FLAG03, ";
        $query .= "           ST1.EXTR_EXPENSE05_FLAG04, ";
        $query .= "           ST1.EXTR_EXPENSE05_FLAG05, ";
        $query .= "           ST1.EXTR_EXPENSE05_PAID_MONEY_DATE, ";
        $query .= "           ST1.EXTR_EXPENSE06_TRANS_SDATE, ";
        $query .= "           ST1.EXTR_EXPENSE06_MONEY, ";
        $query .= "           ST1.EXTR_EXPENSE06_FLAG01, ";
        $query .= "           ST1.EXTR_EXPENSE06_FLAG02, ";
        $query .= "           ST1.EXTR_EXPENSE06_FLAG03, ";
        $query .= "           ST1.EXTR_EXPENSE06_FLAG04, ";
        $query .= "           ST1.EXTR_EXPENSE06_FLAG05, ";
        $query .= "           ST1.EXTR_EXPENSE06_PAID_MONEY_DATE, ";
        $query .= "           ST1.REGISTERCD, ";
        $query .= "           ST1.UPDATED ";
        $query .= "       FROM BANK_RESULT_TMP_DATA ST1 ";
        $query .= "            LEFT JOIN BANK_RESULT_TMP_DATA2 ST2 ON ST1.SCHREGNO = ST2.SCHREGNO ";
        $query .= "       WHERE ";
        $query .= "           NOT EXISTS(SELECT ";
        $query .= "                           * ";
        $query .= "                       FROM ";
        $query .= "                           BANK_RESULT_TMP_DATA2 W ";
        $query .= "                       WHERE ";
        $query .= "                           VALUE(ST1.SCHOOL_CODE,'*****')                               = VALUE(W.SCHOOL_CODE,'*****') ";
        $query .= "                           AND VALUE(ST1.COURSE_CODE,'********')                        = VALUE(W.COURSE_CODE,'********') ";
        $query .= "                           AND VALUE(ST1.SCHREGNO,'********')                           = VALUE(W.SCHREGNO,'********') ";
        $query .= "                           AND VALUE(ST1.BIRTHDAY,'0001-01-01')                         = VALUE(W.BIRTHDAY,'0001-01-01') ";
        $query .= "                           AND VALUE(ST1.NAME_KANA,'*')                                 = VALUE(W.NAME_KANA,'*') ";
        $query .= "                           AND VALUE(ST1.NAME,'*')                                      = VALUE(W.NAME,'*') ";
        $query .= "                           AND VALUE(ST1.PAYER_NAME_KANA,'*')                           = VALUE(W.PAYER_NAME_KANA,'*') ";
        $query .= "                           AND VALUE(ST1.PAYER_NAME,'*')                                = VALUE(W.PAYER_NAME,'*') ";
        $query .= "                           AND VALUE(ST1.PAYER_TELNO,'*')                               = VALUE(W.PAYER_TELNO,'*') ";
        $query .= "                           AND VALUE(ST1.BANKCD,'*')                                    = VALUE(W.BANKCD,'*') ";
        $query .= "                           AND VALUE(ST1.BRANCHCD,'*')                                  = VALUE(W.BRANCHCD,'*') ";
        $query .= "                           AND VALUE(ST1.BANKNAME_KANA,'*')                             = VALUE(W.BANKNAME_KANA,'*') ";
        $query .= "                           AND VALUE(ST1.BRANCHNAME_KANA,'*')                           = VALUE(W.BRANCHNAME_KANA,'*') ";
        $query .= "                           AND VALUE(ST1.DEPOSIT_ITEM,'*')                              = VALUE(W.DEPOSIT_ITEM,'*') ";
        $query .= "                           AND VALUE(ST1.ACCOUNTNO,'*******')                           = VALUE(W.ACCOUNTNO,'*******') ";
        $query .= "                           AND VALUE(ST1.ACCOUNTNAME,'*')                               = VALUE(W.ACCOUNTNAME,'*') ";
        $query .= "                           AND VALUE(ST1.EXPENSE01_PAID_MONEY,0)                        = VALUE(W.EXPENSE01_PAID_MONEY,0) ";
        $query .= "                           AND VALUE(ST1.EXPENSE01_NECE_MONEY,0)                        = VALUE(W.EXPENSE01_NECE_MONEY,0) ";
        $query .= "                           AND VALUE(ST1.EXPENSE02_PAID_MONEY,0)                        = VALUE(W.EXPENSE02_PAID_MONEY,0) ";
        $query .= "                           AND VALUE(ST1.EXPENSE02_NECE_MONEY,0)                        = VALUE(W.EXPENSE02_NECE_MONEY,0) ";
        $query .= "                           AND VALUE(ST1.EXPENSE03_PAID_MONEY,0)                        = VALUE(W.EXPENSE03_PAID_MONEY,0) ";
        $query .= "                           AND VALUE(ST1.EXPENSE03_NECE_MONEY,0)                        = VALUE(W.EXPENSE03_NECE_MONEY,0) ";
        $query .= "                           AND VALUE(ST1.EXPENSE04_PAID_MONEY,0)                        = VALUE(W.EXPENSE04_PAID_MONEY,0) ";
        $query .= "                           AND VALUE(ST1.EXPENSE04_NECE_MONEY,0)                        = VALUE(W.EXPENSE04_NECE_MONEY,0) ";
        $query .= "                           AND VALUE(ST1.EXPENSE05_PAID_MONEY,0)                        = VALUE(W.EXPENSE05_PAID_MONEY,0) ";
        $query .= "                           AND VALUE(ST1.EXPENSE05_NECE_MONEY,0)                        = VALUE(W.EXPENSE05_NECE_MONEY,0) ";
        $query .= "                           AND VALUE(ST1.EXPENSE06_PAID_MONEY,0)                        = VALUE(W.EXPENSE06_PAID_MONEY,0) ";
        $query .= "                           AND VALUE(ST1.EXPENSE06_NECE_MONEY,0)                        = VALUE(W.EXPENSE06_NECE_MONEY,0) ";
        $query .= "                           AND VALUE(ST1.TERM01_EXPENSE01_MONEY_DUE,0)                  = VALUE(W.TERM01_EXPENSE01_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM01_EXPENSE02_MONEY_DUE,0)                  = VALUE(W.TERM01_EXPENSE02_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM01_EXPENSE03_MONEY_DUE,0)                  = VALUE(W.TERM01_EXPENSE03_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM01_EXPENSE04_MONEY_DUE,0)                  = VALUE(W.TERM01_EXPENSE04_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM01_EXPENSE05_MONEY_DUE,0)                  = VALUE(W.TERM01_EXPENSE05_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM01_EXPENSE06_MONEY_DUE,0)                  = VALUE(W.TERM01_EXPENSE06_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM01_SUM_MONEY_DUE,0)                        = VALUE(W.TERM01_SUM_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM01_PAID_MONEY_DATE,'0001-01-01')           = VALUE(W.TERM01_PAID_MONEY_DATE,'0001-01-01') ";
        $query .= "                           AND VALUE(ST1.TERM01_FURI_MONEY_DATE,'0001-01-01')           = VALUE(W.TERM01_FURI_MONEY_DATE,'0001-01-01') ";
        $query .= "                           AND VALUE(ST1.TERM02_EXPENSE01_MONEY_DUE,0)                  = VALUE(W.TERM02_EXPENSE01_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM02_EXPENSE02_MONEY_DUE,0)                  = VALUE(W.TERM02_EXPENSE02_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM02_EXPENSE03_MONEY_DUE,0)                  = VALUE(W.TERM02_EXPENSE03_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM02_EXPENSE04_MONEY_DUE,0)                  = VALUE(W.TERM02_EXPENSE04_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM02_EXPENSE05_MONEY_DUE,0)                  = VALUE(W.TERM02_EXPENSE05_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM02_EXPENSE06_MONEY_DUE,0)                  = VALUE(W.TERM02_EXPENSE06_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM02_SUM_MONEY_DUE,0)                        = VALUE(W.TERM02_SUM_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM02_PAID_MONEY_DATE,'0001-01-01')           = VALUE(W.TERM02_PAID_MONEY_DATE,'0001-01-01') ";
        $query .= "                           AND VALUE(ST1.TERM02_FURI_MONEY_DATE,'0001-01-01')           = VALUE(W.TERM02_FURI_MONEY_DATE,'0001-01-01') ";
        $query .= "                           AND VALUE(ST1.TERM03_EXPENSE01_MONEY_DUE,0)                  = VALUE(W.TERM03_EXPENSE01_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM03_EXPENSE02_MONEY_DUE,0)                  = VALUE(W.TERM03_EXPENSE02_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM03_EXPENSE03_MONEY_DUE,0)                  = VALUE(W.TERM03_EXPENSE03_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM03_EXPENSE04_MONEY_DUE,0)                  = VALUE(W.TERM03_EXPENSE04_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM03_EXPENSE05_MONEY_DUE,0)                  = VALUE(W.TERM03_EXPENSE05_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM03_EXPENSE06_MONEY_DUE,0)                  = VALUE(W.TERM03_EXPENSE06_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM03_SUM_MONEY_DUE,0)                        = VALUE(W.TERM03_SUM_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM03_PAID_MONEY_DATE,'0001-01-01')           = VALUE(W.TERM03_PAID_MONEY_DATE,'0001-01-01') ";
        $query .= "                           AND VALUE(ST1.TERM03_FURI_MONEY_DATE,'0001-01-01')           = VALUE(W.TERM03_FURI_MONEY_DATE,'0001-01-01') ";
        $query .= "                           AND VALUE(ST1.TERM04_EXPENSE01_MONEY_DUE,0)                  = VALUE(W.TERM04_EXPENSE01_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM04_EXPENSE02_MONEY_DUE,0)                  = VALUE(W.TERM04_EXPENSE02_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM04_EXPENSE03_MONEY_DUE,0)                  = VALUE(W.TERM04_EXPENSE03_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM04_EXPENSE04_MONEY_DUE,0)                  = VALUE(W.TERM04_EXPENSE04_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM04_EXPENSE05_MONEY_DUE,0)                  = VALUE(W.TERM04_EXPENSE05_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM04_EXPENSE06_MONEY_DUE,0)                  = VALUE(W.TERM04_EXPENSE06_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM04_SUM_MONEY_DUE,0)                        = VALUE(W.TERM04_SUM_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM04_PAID_MONEY_DATE,'0001-01-01')           = VALUE(W.TERM04_PAID_MONEY_DATE,'0001-01-01') ";
        $query .= "                           AND VALUE(ST1.TERM04_FURI_MONEY_DATE,'0001-01-01')           = VALUE(W.TERM04_FURI_MONEY_DATE,'0001-01-01') ";
        $query .= "                           AND VALUE(ST1.TERM05_EXPENSE01_MONEY_DUE,0)                  = VALUE(W.TERM05_EXPENSE01_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM05_EXPENSE02_MONEY_DUE,0)                  = VALUE(W.TERM05_EXPENSE02_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM05_EXPENSE03_MONEY_DUE,0)                  = VALUE(W.TERM05_EXPENSE03_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM05_EXPENSE04_MONEY_DUE,0)                  = VALUE(W.TERM05_EXPENSE04_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM05_EXPENSE05_MONEY_DUE,0)                  = VALUE(W.TERM05_EXPENSE05_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM05_EXPENSE06_MONEY_DUE,0)                  = VALUE(W.TERM05_EXPENSE06_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM05_SUM_MONEY_DUE,0)                        = VALUE(W.TERM05_SUM_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM05_PAID_MONEY_DATE,'0001-01-01')           = VALUE(W.TERM05_PAID_MONEY_DATE,'0001-01-01') ";
        $query .= "                           AND VALUE(ST1.TERM05_FURI_MONEY_DATE,'0001-01-01')           = VALUE(W.TERM05_FURI_MONEY_DATE,'0001-01-01') ";
        $query .= "                           AND VALUE(ST1.TERM06_EXPENSE01_MONEY_DUE,0)                  = VALUE(W.TERM06_EXPENSE01_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM06_EXPENSE02_MONEY_DUE,0)                  = VALUE(W.TERM06_EXPENSE02_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM06_EXPENSE03_MONEY_DUE,0)                  = VALUE(W.TERM06_EXPENSE03_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM06_EXPENSE04_MONEY_DUE,0)                  = VALUE(W.TERM06_EXPENSE04_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM06_EXPENSE05_MONEY_DUE,0)                  = VALUE(W.TERM06_EXPENSE05_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM06_EXPENSE06_MONEY_DUE,0)                  = VALUE(W.TERM06_EXPENSE06_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM06_SUM_MONEY_DUE,0)                        = VALUE(W.TERM06_SUM_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM06_PAID_MONEY_DATE,'0001-01-01')           = VALUE(W.TERM06_PAID_MONEY_DATE,'0001-01-01') ";
        $query .= "                           AND VALUE(ST1.TERM06_FURI_MONEY_DATE,'0001-01-01')           = VALUE(W.TERM06_FURI_MONEY_DATE,'0001-01-01') ";
        $query .= "                           AND VALUE(ST1.TERM07_EXPENSE01_MONEY_DUE,0)                  = VALUE(W.TERM07_EXPENSE01_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM07_EXPENSE02_MONEY_DUE,0)                  = VALUE(W.TERM07_EXPENSE02_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM07_EXPENSE03_MONEY_DUE,0)                  = VALUE(W.TERM07_EXPENSE03_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM07_EXPENSE04_MONEY_DUE,0)                  = VALUE(W.TERM07_EXPENSE04_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM07_EXPENSE05_MONEY_DUE,0)                  = VALUE(W.TERM07_EXPENSE05_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM07_EXPENSE06_MONEY_DUE,0)                  = VALUE(W.TERM07_EXPENSE06_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM07_SUM_MONEY_DUE,0)                        = VALUE(W.TERM07_SUM_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM07_PAID_MONEY_DATE,'0001-01-01')           = VALUE(W.TERM07_PAID_MONEY_DATE,'0001-01-01') ";
        $query .= "                           AND VALUE(ST1.TERM07_FURI_MONEY_DATE,'0001-01-01')           = VALUE(W.TERM07_FURI_MONEY_DATE,'0001-01-01') ";
        $query .= "                           AND VALUE(ST1.TERM08_EXPENSE01_MONEY_DUE,0)                  = VALUE(W.TERM08_EXPENSE01_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM08_EXPENSE02_MONEY_DUE,0)                  = VALUE(W.TERM08_EXPENSE02_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM08_EXPENSE03_MONEY_DUE,0)                  = VALUE(W.TERM08_EXPENSE03_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM08_EXPENSE04_MONEY_DUE,0)                  = VALUE(W.TERM08_EXPENSE04_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM08_EXPENSE05_MONEY_DUE,0)                  = VALUE(W.TERM08_EXPENSE05_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM08_EXPENSE06_MONEY_DUE,0)                  = VALUE(W.TERM08_EXPENSE06_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM08_SUM_MONEY_DUE,0)                        = VALUE(W.TERM08_SUM_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM08_PAID_MONEY_DATE,'0001-01-01')           = VALUE(W.TERM08_PAID_MONEY_DATE,'0001-01-01') ";
        $query .= "                           AND VALUE(ST1.TERM08_FURI_MONEY_DATE,'0001-01-01')           = VALUE(W.TERM08_FURI_MONEY_DATE,'0001-01-01') ";
        $query .= "                           AND VALUE(ST1.TERM09_EXPENSE01_MONEY_DUE,0)                  = VALUE(W.TERM09_EXPENSE01_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM09_EXPENSE02_MONEY_DUE,0)                  = VALUE(W.TERM09_EXPENSE02_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM09_EXPENSE03_MONEY_DUE,0)                  = VALUE(W.TERM09_EXPENSE03_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM09_EXPENSE04_MONEY_DUE,0)                  = VALUE(W.TERM09_EXPENSE04_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM09_EXPENSE05_MONEY_DUE,0)                  = VALUE(W.TERM09_EXPENSE05_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM09_EXPENSE06_MONEY_DUE,0)                  = VALUE(W.TERM09_EXPENSE06_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM09_SUM_MONEY_DUE,0)                        = VALUE(W.TERM09_SUM_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM09_PAID_MONEY_DATE,'0001-01-01')           = VALUE(W.TERM09_PAID_MONEY_DATE,'0001-01-01') ";
        $query .= "                           AND VALUE(ST1.TERM09_FURI_MONEY_DATE,'0001-01-01')           = VALUE(W.TERM09_FURI_MONEY_DATE,'0001-01-01') ";
        $query .= "                           AND VALUE(ST1.TERM10_EXPENSE01_MONEY_DUE,0)                  = VALUE(W.TERM10_EXPENSE01_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM10_EXPENSE02_MONEY_DUE,0)                  = VALUE(W.TERM10_EXPENSE02_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM10_EXPENSE03_MONEY_DUE,0)                  = VALUE(W.TERM10_EXPENSE03_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM10_EXPENSE04_MONEY_DUE,0)                  = VALUE(W.TERM10_EXPENSE04_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM10_EXPENSE05_MONEY_DUE,0)                  = VALUE(W.TERM10_EXPENSE05_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM10_EXPENSE06_MONEY_DUE,0)                  = VALUE(W.TERM10_EXPENSE06_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM10_SUM_MONEY_DUE,0)                        = VALUE(W.TERM10_SUM_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM10_PAID_MONEY_DATE,'0001-01-01')           = VALUE(W.TERM10_PAID_MONEY_DATE,'0001-01-01') ";
        $query .= "                           AND VALUE(ST1.TERM10_FURI_MONEY_DATE,'0001-01-01')           = VALUE(W.TERM10_FURI_MONEY_DATE,'0001-01-01') ";
        $query .= "                           AND VALUE(ST1.TERM11_EXPENSE01_MONEY_DUE,0)                  = VALUE(W.TERM11_EXPENSE01_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM11_EXPENSE02_MONEY_DUE,0)                  = VALUE(W.TERM11_EXPENSE02_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM11_EXPENSE03_MONEY_DUE,0)                  = VALUE(W.TERM11_EXPENSE03_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM11_EXPENSE04_MONEY_DUE,0)                  = VALUE(W.TERM11_EXPENSE04_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM11_EXPENSE05_MONEY_DUE,0)                  = VALUE(W.TERM11_EXPENSE05_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM11_EXPENSE06_MONEY_DUE,0)                  = VALUE(W.TERM11_EXPENSE06_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM11_SUM_MONEY_DUE,0)                        = VALUE(W.TERM11_SUM_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM11_PAID_MONEY_DATE,'0001-01-01')           = VALUE(W.TERM11_PAID_MONEY_DATE,'0001-01-01') ";
        $query .= "                           AND VALUE(ST1.TERM11_FURI_MONEY_DATE,'0001-01-01')           = VALUE(W.TERM11_FURI_MONEY_DATE,'0001-01-01') ";
        $query .= "                           AND VALUE(ST1.TERM12_EXPENSE01_MONEY_DUE,0)                  = VALUE(W.TERM12_EXPENSE01_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM12_EXPENSE02_MONEY_DUE,0)                  = VALUE(W.TERM12_EXPENSE02_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM12_EXPENSE03_MONEY_DUE,0)                  = VALUE(W.TERM12_EXPENSE03_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM12_EXPENSE04_MONEY_DUE,0)                  = VALUE(W.TERM12_EXPENSE04_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM12_EXPENSE05_MONEY_DUE,0)                  = VALUE(W.TERM12_EXPENSE05_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM12_EXPENSE06_MONEY_DUE,0)                  = VALUE(W.TERM12_EXPENSE06_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM12_SUM_MONEY_DUE,0)                        = VALUE(W.TERM12_SUM_MONEY_DUE,0) ";
        $query .= "                           AND VALUE(ST1.TERM12_PAID_MONEY_DATE,'0001-01-01')           = VALUE(W.TERM12_PAID_MONEY_DATE,'0001-01-01') ";
        $query .= "                           AND VALUE(ST1.TERM12_FURI_MONEY_DATE,'0001-01-01')           = VALUE(W.TERM12_FURI_MONEY_DATE,'0001-01-01') ";
        $query .= "                           AND VALUE(ST1.EXTR_EXPENSE01_TRANS_SDATE,'0001-01-01')       = VALUE(W.EXTR_EXPENSE01_TRANS_SDATE,'0001-01-01') ";
        $query .= "                           AND VALUE(ST1.EXTR_EXPENSE01_MONEY,0)                        = VALUE(W.EXTR_EXPENSE01_MONEY,0) ";
        $query .= "                           AND VALUE(ST1.EXTR_EXPENSE01_PAID_MONEY_DATE,'0001-01-01')   = VALUE(W.EXTR_EXPENSE01_PAID_MONEY_DATE,'0001-01-01') ";
        $query .= "                           AND VALUE(ST1.EXTR_EXPENSE02_TRANS_SDATE,'0001-01-01')       = VALUE(W.EXTR_EXPENSE02_TRANS_SDATE,'0001-01-01') ";
        $query .= "                           AND VALUE(ST1.EXTR_EXPENSE02_MONEY,0)                        = VALUE(W.EXTR_EXPENSE02_MONEY,0) ";
        $query .= "                           AND VALUE(ST1.EXTR_EXPENSE02_PAID_MONEY_DATE,'0001-01-01')   = VALUE(W.EXTR_EXPENSE02_PAID_MONEY_DATE,'0001-01-01') ";
        $query .= "                           AND VALUE(ST1.EXTR_EXPENSE03_TRANS_SDATE,'0001-01-01')       = VALUE(W.EXTR_EXPENSE03_TRANS_SDATE,'0001-01-01') ";
        $query .= "                           AND VALUE(ST1.EXTR_EXPENSE03_MONEY,0)                        = VALUE(W.EXTR_EXPENSE03_MONEY,0) ";
        $query .= "                           AND VALUE(ST1.EXTR_EXPENSE03_PAID_MONEY_DATE,'0001-01-01')   = VALUE(W.EXTR_EXPENSE03_PAID_MONEY_DATE,'0001-01-01') ";
        $query .= "                           AND VALUE(ST1.EXTR_EXPENSE04_TRANS_SDATE,'0001-01-01')       = VALUE(W.EXTR_EXPENSE04_TRANS_SDATE,'0001-01-01') ";
        $query .= "                           AND VALUE(ST1.EXTR_EXPENSE04_MONEY,0)                        = VALUE(W.EXTR_EXPENSE04_MONEY,0) ";
        $query .= "                           AND VALUE(ST1.EXTR_EXPENSE04_PAID_MONEY_DATE,'0001-01-01')   = VALUE(W.EXTR_EXPENSE04_PAID_MONEY_DATE,'0001-01-01') ";
        $query .= "                           AND VALUE(ST1.EXTR_EXPENSE05_TRANS_SDATE,'0001-01-01')       = VALUE(W.EXTR_EXPENSE05_TRANS_SDATE,'0001-01-01') ";
        $query .= "                           AND VALUE(ST1.EXTR_EXPENSE05_MONEY,0)                        = VALUE(W.EXTR_EXPENSE05_MONEY,0) ";
        $query .= "                           AND VALUE(ST1.EXTR_EXPENSE05_PAID_MONEY_DATE,'0001-01-01')   = VALUE(W.EXTR_EXPENSE05_PAID_MONEY_DATE,'0001-01-01') ";
        $query .= "                           AND VALUE(ST1.EXTR_EXPENSE06_TRANS_SDATE,'0001-01-01')       = VALUE(W.EXTR_EXPENSE06_TRANS_SDATE,'0001-01-01') ";
        $query .= "                           AND VALUE(ST1.EXTR_EXPENSE06_MONEY,0)                        = VALUE(W.EXTR_EXPENSE06_MONEY,0) ";
        $query .= "                           AND VALUE(ST1.EXTR_EXPENSE06_PAID_MONEY_DATE,'0001-01-01')   = VALUE(W.EXTR_EXPENSE06_PAID_MONEY_DATE,'0001-01-01') ";
        $query .= "                     ) ";

        return $query;
    }

}
?>
