<?php

require_once('for_php7.php');
class knjz421bQuery extends Query
{

    //対象年度
    public function getJobOfferYear()
    {
        $query  = " WITH THIS_YEAR(LABEL, VALUE) AS (VALUES('".CTRL_YEAR."', '".CTRL_YEAR."')) ";
        $query .= " SELECT ";
        $query .= "     LABEL, ";
        $query .= "     VALUE ";
        $query .= " FROM ";
        $query .= "     THIS_YEAR ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM  ";
        $query .= "     JOB_OFFER_SS_DAT ";
        $query .= " ORDER BY  ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //管轄
    public function getKankatsu()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR='".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1='E049' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    // １レコード取得(会社マスタ)
    public function &getCollegeOrCompanyMst($company_cd)
    {
        $query  = " SELECT ";
        $query .= "     T1.COMPANY_CD, ";
        $query .= "     T1.COMPANY_NAME, ";
        $query .= "     T1.SHIHONKIN, ";
        $query .= "     T1.SONINZU, ";
        $query .= "     T1.ZIPCD AS COMPANY_ZIPCD, ";
        $query .= "     T1.ADDR1 AS COMPANY_ADDR1, ";
        $query .= "     T1.ADDR2 AS COMPANY_ADDR2, ";
        $query .= "     T1.TELNO AS COMPANY_TELNO1 ";
        $query .= " FROM ";
        $query .= "     COMPANY_MST T1 ";
        $query .= " WHERE ";
        $query .= "     COMPANY_CD = '$company_cd'";

        return $query;
    }

    //１レコード取得
    public function getRow($year, $cd)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     *  ";
        $query .= " FROM  ";
        $query .= "     JOB_OFFER_SS_DAT ";
        $query .= " WHERE  ";
        $query .= "     YEAR       = '".$year."' ";
        $query .= " AND SENKOU_NO  = " .$cd ." ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //一覧取得
    public function getList($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     VALUE(T1.TSUKIN_NINZU, 0) + VALUE(T1.SUMIKOMI_NINZU, 0) + VALUE(T1.FUMON_NINZU, 0) AS TOTAL_NINZU, ";
        $query .= "     T1.COMPANY_SORT || ':' || T2.NAME1 AS COMPANY_SORT, ";
        $query .= "     T1.TARGET_SEX || ':' || T3.NAME1 AS TARGET_SEX, ";
        $query .= "     T1.JUDGING_MEANS1 || ':' || T4.NAME1 AS JUDGING_MEANS1, ";
        $query .= "     T1.JUDGING_MEANS2 || ':' || T5.NAME1 AS JUDGING_MEANS2, ";
        $query .= "     T1.JUDGING_MEANS3 || ':' || T6.NAME1 AS JUDGING_MEANS3, ";
        $query .= "     T1.JUDGING_MEANS4 || ':' || T7.NAME1 AS JUDGING_MEANS4, ";
        $query .= "     T8.INDUSTRY_SNAME, ";
        $query .= "     JTYPE.JOBTYPE_SNAME ";
        $query .= " FROM JOB_OFFER_SS_DAT T1 ";
        $query .= " LEFT JOIN V_NAME_MST T2 ON T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "           AND T2.NAMECD1 = 'E008' AND T2.NAMECD2 = COMPANY_SORT ";
        $query .= " LEFT JOIN V_NAME_MST T3 ON T3.YEAR = '".CTRL_YEAR."' ";
        $query .= "           AND T3.NAMECD1 = 'E007' AND T3.NAMECD2 = TARGET_SEX ";
        $query .= " LEFT JOIN V_NAME_MST T4 ON T4.YEAR = '".CTRL_YEAR."' ";
        $query .= "           AND T4.NAMECD1 = 'E029' AND T4.NAMECD2 = JUDGING_MEANS1 ";
        $query .= " LEFT JOIN V_NAME_MST T5 ON T5.YEAR = '".CTRL_YEAR."' ";
        $query .= "           AND T5.NAMECD1 = 'E029' AND T5.NAMECD2 = JUDGING_MEANS2 ";
        $query .= " LEFT JOIN V_NAME_MST T6 ON T6.YEAR = '".CTRL_YEAR."' ";
        $query .= "           AND T6.NAMECD1 = 'E029' AND T6.NAMECD2 = JUDGING_MEANS3 ";
        $query .= " LEFT JOIN V_NAME_MST T7 ON T7.YEAR = '".CTRL_YEAR."' ";
        $query .= "           AND T7.NAMECD1 = 'E029' AND T7.NAMECD2 = JUDGING_MEANS4 ";
        $query .= " LEFT JOIN INDUSTRY_S_MST T8 ON T8.INDUSTRY_SCD = T1.INDUSTRY_SCD ";
        $query .= " LEFT JOIN JOBTYPE_SS_MST JTYPE ON JTYPE.JOBTYPE_LCD  = T1.JOBTYPE_LCD ";
        $query .= "                               AND JTYPE.JOBTYPE_MCD  = T1.JOBTYPE_MCD ";
        $query .= "                               AND JTYPE.JOBTYPE_SCD  = T1.JOBTYPE_SCD ";
        $query .= "                               AND JTYPE.JOBTYPE_SSCD = T1.JOBTYPE_SSCD ";
        $query .= " WHERE  ";
        $query .= "     T1.YEAR = '".$model->field["YEAR"]."' ";
        $query .= " ORDER BY SENKOU_NO ";

        return $query;
    }

    //雇用形態の取得
    public function getEmploymentStatus()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'E053' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //職種の取得
    public function getCompanycd()
    {
        $query  = " SELECT NAMECD2 AS VALUE,NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE YEAR='".CTRL_YEAR."' ";
        $query .= "       AND NAMECD1='E008' ";
        $query .= " ORDER BY NAMECD2";

        return $query;
    }

    //選考方法の取得
    public function getJudgeing()
    {
        $query  = " SELECT NAMECD2 AS VALUE,NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE YEAR='".CTRL_YEAR."' ";
        $query .= "       AND NAMECD1='E029' ";
        $query .= " ORDER BY NAMECD2";

        return $query;
    }

    //産業種別（小）名称取得
    public function getIndustrySName($cd)
    {
        $query  = " SELECT ";
        $query .= "     INDUSTRY_SNAME ";
        $query .= " FROM ";
        $query .= "     INDUSTRY_S_MST ";
        $query .= " WHERE ";
        $query .= "     INDUSTRY_SCD = '{$cd}' ";

        return $query;
    }

    //職業種別（小）名称取得
    public function getjobTypeSName($model, $lCd, $mCd, $sCd, $ssCd)
    {
        $setSCd = $model->getZeroPaddingScd($sCd, "S");
        $setSSCd = $model->getZeroPaddingScd($ssCd, "SS");

        $query  = " SELECT ";
        $query .= "     JOBTYPE_SNAME ";
        $query .= " FROM ";
        $query .= "     JOBTYPE_SS_MST ";
        $query .= " WHERE JOBTYPE_SCD  = '{$setSCd}' ";
        $query .= "   AND JOBTYPE_SSCD = '{$setSSCd}' ";

        return $query;
    }

    //INSERT
    public function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["SENKOU_NO"][NUMBER]          = $fields["SENKOU_NO"];
        $data["ACCEPTANCE_DATE"][DATE]      = $fields["ACCEPTANCE_DATE"];
        $data["COMPANY_CD"][TEXT]           = $fields["COMPANY_CD"];
        $data["COMPANY_NAME"][TEXT]         = $fields["COMPANY_NAME"];
        $data["COMPANY_NAMEKANA"][TEXT]     = $fields["COMPANY_NAMEKANA"];
        $data["COMPANY_CONTENTS"][TEXT]     = $fields["COMPANY_CONTENTS"];
        $data["SHIHONKIN"][TEXT]            = $fields["SHIHONKIN"];
        $data["MAKECOMPANY_YEAR"][TEXT]     = $fields["MAKECOMPANY_YEAR"];
        $data["COMPANY_ZIPCD"][TEXT]        = $fields["COMPANY_ZIPCD"];
        $data["COMPANY_ADDR1"][TEXT]        = $fields["COMPANY_ADDR1"];
        $data["COMPANY_ADDR2"][TEXT]        = $fields["COMPANY_ADDR2"];
        $data["COMPANY_TELNO1"][TEXT]       = $fields["COMPANY_TELNO1"];
        $data["COMPANY_TELNO2"][TEXT]       = $fields["COMPANY_TELNO2"];
        $data["COMPANY_FAXNO"][TEXT]        = $fields["COMPANY_FAXNO"];
        $data["INDUSTRY_SCD"][TEXT]         = $fields["INDUSTRY_SCD"];
        $data["SONINZU"][NUMBER]            = $fields["SONINZU"];
        $data["SHUSHOKU_NAME"][TEXT]        = $fields["SHUSHOKU_NAME"];
        $data["SHUSHOKU_ZIPCD"][TEXT]       = $fields["SHUSHOKU_ZIPCD"];
        $data["SHUSHOKU_ADDR1"][TEXT]       = $fields["SHUSHOKU_ADDR1"];
        $data["SHUSHOKU_ADDR2"][TEXT]       = $fields["SHUSHOKU_ADDR2"];
        $data["SHUSHOKU_TELNO1"][TEXT]      = $fields["SHUSHOKU_TELNO1"];
        $data["SHUSHOKU_TELNO2"][TEXT]      = $fields["SHUSHOKU_TELNO2"];
        $data["SHUSHOKU_FAXNO"][TEXT]       = $fields["SHUSHOKU_FAXNO"];
        $data["TONINZU"][NUMBER]            = $fields["TONINZU"];
        if ($model->Properties["useCompany_Sort"] == "1") {
            $data["COMPANY_SORT_REMARK"][TEXT]  = $fields["COMPANY_SORT_REMARK"];
        } else {
            $data["COMPANY_SORT"][TEXT]         = $fields["COMPANY_SORT"];
        }
        $data["TARGET_NINZU"][NUMBER]       = $fields["TARGET_NINZU"];
        $data["TARGET_SEX"][TEXT]           = $fields["TARGET_SEX"];
        $data["JUDGING_MEANS1"][TEXT]       = $fields["JUDGING_MEANS1"];
        $data["JUDGING_MEANS2"][TEXT]       = $fields["JUDGING_MEANS2"];
        $data["JUDGING_MEANS3"][TEXT]       = $fields["JUDGING_MEANS3"];
        $data["JUDGING_MEANS4"][TEXT]       = $fields["JUDGING_MEANS4"];
        $data["HOLIDAY1"][TEXT]             = $fields["HOLIDAY1"];
        $data["HOLIDAY2"][TEXT]             = $fields["HOLIDAY2"];
        $data["HOLIDAY3"][TEXT]             = $fields["HOLIDAY3"];
        $data["HOLIDAY4"][TEXT]             = $fields["HOLIDAY4"];
        $data["HOLIDAY_REMARK"][TEXT]       = $fields["HOLIDAY_REMARK"];
        $data["SHIFT"][TEXT]                = $fields["SHIFT"];
        $data["COMPANY_LODGING"][TEXT]      = $fields["COMPANY_LODGING"];
        $data["BASIC_SALARY"][NUMBER]       = $fields["BASIC_SALARY"];
        $data["TAKE_SALARY"][NUMBER]        = $fields["TAKE_SALARY"];
        $data["KANKATSU"][TEXT]             = $fields["KANKATSU"];
        $data["JOBTYPE_LCD"][TEXT]          = $fields["JOBTYPE_LCD"];
        $data["JOBTYPE_MCD"][TEXT]          = $fields["JOBTYPE_MCD"];
        $data["JOBTYPE_SCD"][TEXT]          = $fields["JOBTYPE_SCD"];
        $data["JOBTYPE_SSCD"][TEXT]         = $fields["JOBTYPE_SSCD"];
        $data["TSUKIN_NINZU"][NUMBER]       = $fields["TSUKIN_NINZU"];
        $data["SUMIKOMI_NINZU"][NUMBER]     = $fields["SUMIKOMI_NINZU"];
        $data["FUMON_NINZU"][NUMBER]        = $fields["FUMON_NINZU"];
        $data["SUISEN_NINZU"][NUMBER]       = $fields["SUISEN_NINZU"];
        $data["NIJI_BOSYU"][TEXT]           = $fields["NIJI_BOSYU"];
        $data["TSUKIN_SALARY"][NUMBER]      = $fields["TSUKIN_SALARY"];
        $data["SUMIKOMI_SALARY"][NUMBER]    = $fields["SUMIKOMI_SALARY"];
        $data["SELECT_RECEPT_DATE"][DATE]   = $fields["SELECT_RECEPT_DATE"];
        $data["SELECT_DATE"][DATE]          = $fields["SELECT_DATE"];
        $data["KENGAKU_KAI"][TEXT]          = $fields["KENGAKU_KAI"];
        $data["OTHER_HOLIDAY"][TEXT]        = $fields["OTHER_HOLIDAY"];
        $data["REMARK"][TEXT]               = $fields["REMARK"];
        $data["EMPLOYMENT_STATUS"][TEXT]    = $fields["EMPLOYMENT_STATUS"];
        $data["APPLICATION_TARGET"][TEXT]   = $fields["APPLICATION_TARGET"];
        $data["PERSONNEL_MANAGER"][TEXT]    = $fields["PERSONNEL_MANAGER"];
        $data["DEPARTMENT_POSITION"][TEXT]  = $fields["DEPARTMENT_POSITION"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";
        $query = Query::insertSQL($data, "JOB_OFFER_SS_DAT");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    public function &getUpdateQuery($fields, $model)
    {
        $db = Query::dbCheckOut();

        $data["ACCEPTANCE_DATE"][DATE]      = $fields["ACCEPTANCE_DATE"];
        $data["COMPANY_CD"][TEXT]           = $fields["COMPANY_CD"];
        $data["COMPANY_NAME"][TEXT]         = $fields["COMPANY_NAME"];
        $data["COMPANY_NAMEKANA"][TEXT]     = $fields["COMPANY_NAMEKANA"];
        $data["COMPANY_CONTENTS"][TEXT]     = $fields["COMPANY_CONTENTS"];
        $data["SHIHONKIN"][TEXT]            = $fields["SHIHONKIN"];
        $data["MAKECOMPANY_YEAR"][TEXT]     = $fields["MAKECOMPANY_YEAR"];
        $data["COMPANY_ZIPCD"][TEXT]        = $fields["COMPANY_ZIPCD"];
        $data["COMPANY_ADDR1"][TEXT]        = $fields["COMPANY_ADDR1"];
        $data["COMPANY_ADDR2"][TEXT]        = $fields["COMPANY_ADDR2"];
        $data["COMPANY_TELNO1"][TEXT]       = $fields["COMPANY_TELNO1"];
        $data["COMPANY_TELNO2"][TEXT]       = $fields["COMPANY_TELNO2"];
        $data["COMPANY_FAXNO"][TEXT]        = $fields["COMPANY_FAXNO"];
        $data["INDUSTRY_SCD"][TEXT]         = $fields["INDUSTRY_SCD"];
        $data["SONINZU"][NUMBER]            = $fields["SONINZU"];
        $data["SHUSHOKU_NAME"][TEXT]        = $fields["SHUSHOKU_NAME"];
        $data["SHUSHOKU_ZIPCD"][TEXT]       = $fields["SHUSHOKU_ZIPCD"];
        $data["SHUSHOKU_ADDR1"][TEXT]       = $fields["SHUSHOKU_ADDR1"];
        $data["SHUSHOKU_ADDR2"][TEXT]       = $fields["SHUSHOKU_ADDR2"];
        $data["SHUSHOKU_TELNO1"][TEXT]      = $fields["SHUSHOKU_TELNO1"];
        $data["SHUSHOKU_TELNO2"][TEXT]      = $fields["SHUSHOKU_TELNO2"];
        $data["SHUSHOKU_FAXNO"][TEXT]       = $fields["SHUSHOKU_FAXNO"];
        $data["TONINZU"][NUMBER]            = $fields["TONINZU"];
        if ($model->Properties["useCompany_Sort"] == "1") {
            $data["COMPANY_SORT_REMARK"][TEXT]  = $fields["COMPANY_SORT_REMARK"];
        } else {
            $data["COMPANY_SORT"][TEXT]         = $fields["COMPANY_SORT"];
        }
        $data["TARGET_NINZU"][NUMBER]       = $fields["TARGET_NINZU"];
        $data["TARGET_SEX"][TEXT]           = $fields["TARGET_SEX"];
        $data["JUDGING_MEANS1"][TEXT]       = $fields["JUDGING_MEANS1"];
        $data["JUDGING_MEANS2"][TEXT]       = $fields["JUDGING_MEANS2"];
        $data["JUDGING_MEANS3"][TEXT]       = $fields["JUDGING_MEANS3"];
        $data["JUDGING_MEANS4"][TEXT]       = $fields["JUDGING_MEANS4"];
        $data["HOLIDAY1"][TEXT]             = $fields["HOLIDAY1"];
        $data["HOLIDAY2"][TEXT]             = $fields["HOLIDAY2"];
        $data["HOLIDAY3"][TEXT]             = $fields["HOLIDAY3"];
        $data["HOLIDAY4"][TEXT]             = $fields["HOLIDAY4"];
        $data["HOLIDAY_REMARK"][TEXT]       = $fields["HOLIDAY_REMARK"];
        $data["SHIFT"][TEXT]                = $fields["SHIFT"];
        $data["COMPANY_LODGING"][TEXT]      = $fields["COMPANY_LODGING"];
        $data["BASIC_SALARY"][NUMBER]       = $fields["BASIC_SALARY"];
        $data["TAKE_SALARY"][NUMBER]        = $fields["TAKE_SALARY"];
        $data["KANKATSU"][TEXT]             = $fields["KANKATSU"];
        $data["JOBTYPE_LCD"][TEXT]          = $fields["JOBTYPE_LCD"];
        $data["JOBTYPE_MCD"][TEXT]          = $fields["JOBTYPE_MCD"];
        $data["JOBTYPE_SCD"][TEXT]          = $fields["JOBTYPE_SCD"];
        $data["JOBTYPE_SSCD"][TEXT]         = $fields["JOBTYPE_SSCD"];
        $data["TSUKIN_NINZU"][NUMBER]       = $fields["TSUKIN_NINZU"];
        $data["SUMIKOMI_NINZU"][NUMBER]     = $fields["SUMIKOMI_NINZU"];
        $data["FUMON_NINZU"][NUMBER]        = $fields["FUMON_NINZU"];
        $data["SUISEN_NINZU"][NUMBER]       = $fields["SUISEN_NINZU"];
        $data["NIJI_BOSYU"][TEXT]           = $fields["NIJI_BOSYU"];
        $data["TSUKIN_SALARY"][NUMBER]      = $fields["TSUKIN_SALARY"];
        $data["SUMIKOMI_SALARY"][NUMBER]    = $fields["SUMIKOMI_SALARY"];
        $data["SELECT_RECEPT_DATE"][DATE]   = $fields["SELECT_RECEPT_DATE"];
        $data["SELECT_DATE"][DATE]          = $fields["SELECT_DATE"];
        $data["KENGAKU_KAI"][TEXT]          = $fields["KENGAKU_KAI"];
        $data["OTHER_HOLIDAY"][TEXT]        = $fields["OTHER_HOLIDAY"];
        $data["REMARK"][TEXT]               = $fields["REMARK"];
        $data["EMPLOYMENT_STATUS"][TEXT]    = $fields["EMPLOYMENT_STATUS"];
        $data["APPLICATION_TARGET"][TEXT]   = $fields["APPLICATION_TARGET"];
        $data["PERSONNEL_MANAGER"][TEXT]    = $fields["PERSONNEL_MANAGER"];
        $data["DEPARTMENT_POSITION"][TEXT]  = $fields["DEPARTMENT_POSITION"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";
        $where  = " WHERE ";
        $where .= "     YEAR       = '" .$model->getyear ."' ";
        $where .= " AND SENKOU_NO  = " .$model->senkou_no ." ";

        $query = Query::updateSQL($data, "JOB_OFFER_SS_DAT", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    public function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $query  = " DELETE FROM  ";
        $query .= "     JOB_OFFER_SS_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR       = '" .$model->getyear ."' ";
        $query .= " AND SENKOU_NO  = " .$model->senkou_no ." ";
        $db->query($query);
        Query::dbCheckIn($db);
        return $result;
    }

    //エラーデータの削除
    public function deleteQueryErr()
    {
        $query  = " DELETE FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ";
        return $query;
    }

    //存在チェック -- 名称マスタ
    public function checkNameMst($namecd1, $namecd2)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' AND ";
        $query .= "     NAMECD2 = '".$namecd2."' ";

        return $query;
    }

    //存在チェック -- 職業種別コード
    public function checkJobtypeCd($jobtypeName, $jobtypeCd)
    {
        if ($jobtypeName == "JOBTYPE_LCD") {
            $setTableName = "JOBTYPE_L_MST";
        } elseif ($jobtypeName == "JOBTYPE_MCD") {
            $setTableName = "JOBTYPE_M_MST";
        } else {
            $setTableName = "JOBTYPE_SS_MST";
        }
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     {$setTableName} ";
        $query .= " WHERE ";
        $query .= "     {$jobtypeName} = '{$jobtypeCd}' ";

        return $query;
    }

    //存在チェック -- JOB_OFFER_DAT
    public function checkJobOfferDat($year, $senkou_no)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM  ";
        $query .= "     JOB_OFFER_SS_DAT ";
        $query .= " WHERE  ";
        $query .= "     YEAR       = '".$year."' AND ";
        $query .= "     SENKOU_NO  = " .$senkou_no." ";

        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1 = array();
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //CSV取込（追加）
    public function insertQueryCsv($db, $model, $data_arr)
    {
        $data = array();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //データセット
            $data = array();
            $data["YEAR"][TEXT]                 = $data_arr[$i]["YEAR"];
            $data["SENKOU_NO"][NUMBER]          = $data_arr[$i]["SENKOU_NO"];
            $data["ACCEPTANCE_DATE"][DATE]      = $data_arr[$i]["ACCEPTANCE_DATE"];
            $data["COMPANY_CD"][TEXT]           = $data_arr[$i]["COMPANY_CD"];
            $data["COMPANY_NAME"][TEXT]         = $data_arr[$i]["COMPANY_NAME"];
            $data["COMPANY_NAMEKANA"][TEXT]     = $data_arr[$i]["COMPANY_NAMEKANA"];
            $data["COMPANY_CONTENTS"][TEXT]     = $data_arr[$i]["COMPANY_CONTENTS"];
            $data["SHIHONKIN"][TEXT]            = $data_arr[$i]["SHIHONKIN"];
            $data["MAKECOMPANY_YEAR"][TEXT]     = $data_arr[$i]["MAKECOMPANY_YEAR"];
            $data["COMPANY_ZIPCD"][TEXT]        = $data_arr[$i]["COMPANY_ZIPCD"];
            $data["COMPANY_ADDR1"][TEXT]        = $data_arr[$i]["COMPANY_ADDR1"];
            $data["COMPANY_ADDR2"][TEXT]        = $data_arr[$i]["COMPANY_ADDR2"];
            $data["COMPANY_TELNO1"][TEXT]       = $data_arr[$i]["COMPANY_TELNO1"];
            $data["COMPANY_TELNO2"][TEXT]       = $data_arr[$i]["COMPANY_TELNO2"];
            $data["COMPANY_FAXNO"][TEXT]        = $data_arr[$i]["COMPANY_FAXNO"];
            $data["INDUSTRY_SCD"][TEXT]         = $data_arr[$i]["INDUSTRY_SCD"];
            $data["SONINZU"][NUMBER]            = $data_arr[$i]["SONINZU"];
            $data["SHUSHOKU_NAME"][TEXT]        = $data_arr[$i]["SHUSHOKU_NAME"];
            $data["SHUSHOKU_ZIPCD"][TEXT]       = $data_arr[$i]["SHUSHOKU_ZIPCD"];
            $data["SHUSHOKU_ADDR1"][TEXT]       = $data_arr[$i]["SHUSHOKU_ADDR1"];
            $data["SHUSHOKU_ADDR2"][TEXT]       = $data_arr[$i]["SHUSHOKU_ADDR2"];
            $data["SHUSHOKU_TELNO1"][TEXT]      = $data_arr[$i]["SHUSHOKU_TELNO1"];
            $data["SHUSHOKU_TELNO2"][TEXT]      = $data_arr[$i]["SHUSHOKU_TELNO2"];
            $data["SHUSHOKU_FAXNO"][TEXT]       = $data_arr[$i]["SHUSHOKU_FAXNO"];
            $data["TONINZU"][NUMBER]            = $data_arr[$i]["TONINZU"];
            if ($model->Properties["useCompany_Sort"] == "1") {
                $data["COMPANY_SORT_REMARK"][TEXT]  = $data_arr[$i]["COMPANY_SORT_REMARK"];
            } else {
                $data["COMPANY_SORT"][TEXT]         = $data_arr[$i]["COMPANY_SORT"];
            }
            $data["TARGET_NINZU"][NUMBER]       = $data_arr[$i]["TARGET_NINZU"];
            $data["TARGET_SEX"][TEXT]           = $data_arr[$i]["TARGET_SEX"];
            $data["JUDGING_MEANS1"][TEXT]       = $data_arr[$i]["JUDGING_MEANS1"];
            $data["JUDGING_MEANS2"][TEXT]       = $data_arr[$i]["JUDGING_MEANS2"];
            $data["JUDGING_MEANS3"][TEXT]       = $data_arr[$i]["JUDGING_MEANS3"];
            $data["JUDGING_MEANS4"][TEXT]       = $data_arr[$i]["JUDGING_MEANS4"];
            $data["HOLIDAY1"][TEXT]             = $data_arr[$i]["HOLIDAY1"];
            $data["HOLIDAY2"][TEXT]             = $data_arr[$i]["HOLIDAY2"];
            $data["HOLIDAY3"][TEXT]             = $data_arr[$i]["HOLIDAY3"];
            $data["HOLIDAY4"][TEXT]             = $data_arr[$i]["HOLIDAY4"];
            $data["HOLIDAY_REMARK"][TEXT]       = $data_arr[$i]["HOLIDAY_REMARK"];
            $data["SHIFT"][TEXT]                = $data_arr[$i]["SHIFT"];
            $data["COMPANY_LODGING"][TEXT]      = $data_arr[$i]["COMPANY_LODGING"];
            $data["BASIC_SALARY"][NUMBER]       = $data_arr[$i]["BASIC_SALARY"];
            $data["TAKE_SALARY"][NUMBER]        = $data_arr[$i]["TAKE_SALARY"];
            $data["KANKATSU"][TEXT]             = $data_arr[$i]["KANKATSU"];
            $data["JOBTYPE_LCD"][TEXT]          = $data_arr[$i]["JOBTYPE_LCD"];
            $data["JOBTYPE_MCD"][TEXT]          = $data_arr[$i]["JOBTYPE_MCD"];
            $data["JOBTYPE_SCD"][TEXT]          = $data_arr[$i]["JOBTYPE_SCD"];
            $data["JOBTYPE_SSCD"][TEXT]         = $data_arr[$i]["JOBTYPE_SSCD"];
            $data["TSUKIN_NINZU"][NUMBER]       = $data_arr[$i]["TSUKIN_NINZU"];
            $data["SUMIKOMI_NINZU"][NUMBER]     = $data_arr[$i]["SUMIKOMI_NINZU"];
            $data["FUMON_NINZU"][NUMBER]        = $data_arr[$i]["FUMON_NINZU"];
            $data["SUISEN_NINZU"][NUMBER]       = $data_arr[$i]["SUISEN_NINZU"];
            $data["NIJI_BOSYU"][TEXT]           = $data_arr[$i]["NIJI_BOSYU"];
            $data["TSUKIN_SALARY"][NUMBER]      = $data_arr[$i]["TSUKIN_SALARY"];
            $data["SUMIKOMI_SALARY"][NUMBER]    = $data_arr[$i]["SUMIKOMI_SALARY"];
            $data["SELECT_RECEPT_DATE"][DATE]   = $data_arr[$i]["SELECT_RECEPT_DATE"];
            $data["SELECT_DATE"][DATE]          = $data_arr[$i]["SELECT_DATE"];
            $data["KENGAKU_KAI"][TEXT]          = $data_arr[$i]["KENGAKU_KAI"];
            $data["OTHER_HOLIDAY"][TEXT]        = $data_arr[$i]["OTHER_HOLIDAY"];
            $data["REMARK"][TEXT]               = $data_arr[$i]["REMARK"];
            $data["EMPLOYMENT_STATUS"][TEXT]    = $data_arr[$i]["EMPLOYMENT_STATUS"];
            $data["APPLICATION_TARGET"][TEXT]    = $data_arr[$i]["APPLICATION_TARGET"];
            $data["PERSONNEL_MANAGER"][TEXT]    = $data_arr[$i]["PERSONNEL_MANAGER"];
            $data["DEPARTMENT_POSITION"][TEXT]  = $data_arr[$i]["DEPARTMENT_POSITION"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "SYSDATE()";

            $query = Query::insertSQL($data, "JOB_OFFER_SS_DAT");
            $db->query($query);
            $cnt++;
        }
        $db->commit();

        return $cnt;
    }

    //エラー出力
    public function getErrorCsvData()
    {
        $query  = " SELECT ";
        $query .= "     MSGROW, ";
        $query .= "     MSGREMARK ";
        $query .= " FROM ";
        $query .= "     W_CSVMSG_PRG_DAT ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = '".PROGRAMID."' ";

        return $query;
    }

    //CSV書出データ取得
    public function getCsvData($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     'LASTCOLUMN' AS LASTCOLUMN ";
        $query .= " FROM ";
        $query .= "     JOB_OFFER_SS_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= " ORDER BY ";
        $query .= "     YEAR, ";
        $query .= "     SENKOU_NO ";

        return $query;
    }
}
