<?php

require_once('for_php7.php');

class knjp744Query extends Query
{
    //年度取得
    public function getYear()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_AUTO_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR != '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //校種取得
    public function getSchkind($model)
    {
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
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //処理年度のデータ存在チェック
    public function checkDataExists($model, $table)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     {$table} ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->schoolKind."' AND ";
        $query .= "     YEAR        = '".CTRL_YEAR."' ";

        return $query;
    }

    //コピー用データ取得
    public function getCopyQuery($model, $flg)
    {
        $query  = " WITH T_COLLECT AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         T1.COLLECT_L_CD, ";
        $query .= "         T2.COLLECT_M_CD ";
        $query .= "     FROM ";
        $query .= "         COLLECT_L_MST T1 ";
        $query .= "         INNER JOIN COLLECT_M_MST T2 ";
        $query .= "              ON T2.SCHOOLCD     = T1.SCHOOLCD ";
        $query .= "             AND T2.SCHOOL_KIND  = T1.SCHOOL_KIND ";
        $query .= "             AND T2.YEAR         = T1.YEAR ";
        $query .= "             AND T2.COLLECT_L_CD = T1.COLLECT_L_CD ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "         T1.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "         T1.LEVY_FLG     = '1' ";
        $query .= " ), T_INCOME AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         LEVY_L_CD AS INCOME_L_CD, ";
        $query .= "         LEVY_M_CD AS INCOME_M_CD ";
        $query .= "     FROM ";
        $query .= "         LEVY_M_MST ";
        $query .= "     WHERE ";
        $query .= "         SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "         YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "         LEVY_IN_OUT_DIV = '1' AND ";
        $query .= "         ZATU_FLG IS NULL AND ";
        $query .= "         YOBI_FLG IS NULL ";
        $query .= " ), T_SCHREG AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T2.SCHOOL_KIND, ";
        $query .= "         T1.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_REGD_GDAT T2 ON T1.YEAR = T2.YEAR AND T1.GRADE = T2.GRADE ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= " ), ";
        if ($model->Properties["not_select_schregno_auto_income"] != "1") {
            $query .= " SUBMAIN AS ( ";
            $query .= "     SELECT DISTINCT ";
            $query .= "         T1.*, ";
            $query .= "         L1.SCHREGNO ";
            $query .= "     FROM ";
            $query .= "         LEVY_REQUEST_INCOME_AUTO_DAT T1 ";
            $query .= "         LEFT JOIN LEVY_REQUEST_INCOME_AUTO_SCHREG_DAT L1 ";
            $query .= "              ON T1.SCHOOLCD     = L1.SCHOOLCD ";
            $query .= "             AND T1.SCHOOL_KIND  = L1.SCHOOL_KIND ";
            $query .= "             AND T1.YEAR         = L1.YEAR ";
            $query .= "             AND T1.AUTO_NO      = L1.AUTO_NO ";
            $query .= "     WHERE ";
            $query .= "         T1.SCHOOLCD = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $query .= "         T1.YEAR     = '".$model->sansyouYear."' ";
            $query .= " ), MAIN AS ( ";
            $query .= "     SELECT ";
            $query .= "         T1.*, ";
            $query .= "         CASE WHEN L1.COLLECT_L_CD IS NULL OR L1.COLLECT_M_CD IS NULL THEN 1 ELSE 0 END AS COLLECT_FLG, ";
            $query .= "         CASE WHEN L2.INCOME_L_CD IS NULL OR L2.INCOME_M_CD IS NULL THEN 1 ELSE 0 END AS INCOME_FLG, ";
            $query .= "         CASE WHEN L3.SCHREGNO IS NULL THEN 1 ELSE 0 END AS SCHREGNO_FLG, ";
            $query .= "         CASE WHEN L4.STAFFCD IS NULL THEN 1 ELSE 0 END AS STAFFCD_FLG ";
            $query .= "     FROM ";
            $query .= "         SUBMAIN T1 ";
            $query .= "         LEFT JOIN T_COLLECT L1 ";
            $query .= "              ON T1.SCHOOL_KIND  = L1.SCHOOL_KIND ";
            $query .= "             AND T1.COLLECT_L_CD = L1.COLLECT_L_CD ";
            $query .= "             AND T1.COLLECT_M_CD = L1.COLLECT_M_CD ";
            $query .= "         LEFT JOIN T_INCOME L2 ";
            $query .= "              ON T1.SCHOOL_KIND  = L2.SCHOOL_KIND ";
            $query .= "             AND T1.INCOME_L_CD  = L2.INCOME_L_CD ";
            $query .= "             AND T1.INCOME_M_CD  = L2.INCOME_M_CD ";
            $query .= "         LEFT JOIN T_SCHREG L3 ";
            $query .= "              ON T1.SCHOOL_KIND  = L3.SCHOOL_KIND ";
            $query .= "             AND T1.SCHREGNO     = L3.SCHREGNO ";
            $query .= "         LEFT JOIN V_STAFF_MST L4 ";
            $query .= "              ON T1.REQUEST_STAFF = L4.STAFFCD ";
            $query .= "             AND L4.YEAR         = '".CTRL_YEAR."' ";
            $query .= " ) ";
        } else {
            $query .= " SUBMAIN AS ( ";
            $query .= "     SELECT DISTINCT ";
            $query .= "         T1.* ";
            $query .= "     FROM ";
            $query .= "         LEVY_REQUEST_INCOME_AUTO_DAT T1 ";
            $query .= "     WHERE ";
            $query .= "         T1.SCHOOLCD = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $query .= "         T1.YEAR     = '".$model->sansyouYear."' ";
            $query .= " ), MAIN AS ( ";
            $query .= "     SELECT ";
            $query .= "         T1.*, ";
            $query .= "         CASE WHEN L1.COLLECT_L_CD IS NULL OR L1.COLLECT_M_CD IS NULL THEN 1 ELSE 0 END AS COLLECT_FLG, ";
            $query .= "         CASE WHEN L2.INCOME_L_CD IS NULL OR L2.INCOME_M_CD IS NULL THEN 1 ELSE 0 END AS INCOME_FLG, ";
            $query .= "         CASE WHEN L4.STAFFCD IS NULL THEN 1 ELSE 0 END AS STAFFCD_FLG ";
            $query .= "     FROM ";
            $query .= "         SUBMAIN T1 ";
            $query .= "         LEFT JOIN T_COLLECT L1 ";
            $query .= "              ON T1.SCHOOL_KIND  = L1.SCHOOL_KIND ";
            $query .= "             AND T1.COLLECT_L_CD = L1.COLLECT_L_CD ";
            $query .= "             AND T1.COLLECT_M_CD = L1.COLLECT_M_CD ";
            $query .= "         LEFT JOIN T_INCOME L2 ";
            $query .= "              ON T1.SCHOOL_KIND  = L2.SCHOOL_KIND ";
            $query .= "             AND T1.INCOME_L_CD  = L2.INCOME_L_CD ";
            $query .= "             AND T1.INCOME_M_CD  = L2.INCOME_M_CD ";
            $query .= "         LEFT JOIN V_STAFF_MST L4 ";
            $query .= "              ON T1.REQUEST_STAFF = L4.STAFFCD ";
            $query .= "             AND L4.YEAR         = '".CTRL_YEAR."' ";
            $query .= " ) ";
        }

        if ($flg == 'check') {
            $query .= " SELECT ";
            $query .= "     * ";
            $query .= " FROM ";
            $query .= "     MAIN ";
        } else {
            $query .= " SELECT DISTINCT ";
            $query .= "     SCHOOLCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     '".CTRL_YEAR."' AS YEAR, ";
            $query .= "     AUTO_NO, ";
            if ($flg == 'dat') {
                $query .= "     AUTO_NAME, ";
                $query .= "     INCOME_L_CD, ";
                $query .= "     INCOME_M_CD, ";
                $query .= "     REQUEST_REASON, ";
                $query .= "     REQUEST_STAFF, ";
                $query .= "     REMARK, ";
                $query .= "     COMMODITY_PRICE, ";
                $query .= "     COLLECT_L_CD, ";
                $query .= "     COLLECT_M_CD, ";
                $query .= "     COLLECT_S_CD, ";
            } else {
                $query .= "     SCHREGNO, ";
            }
            $query .= "     '".STAFFCD."' AS REGISTERCD, ";
            $query .= "     SYSDATE() AS UPDATED ";
            if ($flg == 'dat') {
                if ($model->Properties["disp_approval_chk"] == "1") {
                    $query .= "     ,AUTO_INCOME_APPROVAL ";
                } else {
                    $query .= "     ,NULL AS AUTO_INCOME_APPROVAL ";
                }
            }
            $query .= " FROM ";
            $query .= "     MAIN ";
            $query .= " WHERE ";
            if ($model->Properties["not_select_schregno_auto_income"] != "1") {
                $query .= "     COLLECT_FLG + INCOME_FLG + SCHREGNO_FLG + STAFFCD_FLG = 0 ";
            } else {
                $query .= "     COLLECT_FLG + INCOME_FLG + STAFFCD_FLG = 0 ";
            }
        }

        return $query;
    }

    //自動設定リスト取得
    public function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.AUTO_NO, ";
        $query .= "     T1.AUTO_NAME, ";
        $query .= "     T1.INCOME_L_CD, ";
        $query .= "     T1.INCOME_M_CD, ";
        $query .= "     T1.INCOME_L_CD || T1.INCOME_M_CD || ':' || VALUE(L1.LEVY_M_NAME,'') AS INCOME_M_NAME, ";
        $query .= "     T1.COLLECT_L_CD, ";
        $query .= "     T1.COLLECT_M_CD, ";
        $query .= "     T1.COLLECT_L_CD || T1.COLLECT_M_CD || ':' || VALUE(L2.COLLECT_M_NAME,'') AS COLLECT_M_NAME ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_AUTO_DAT T1 ";
        $query .= "     INNER JOIN LEVY_M_MST L1 ";
        $query .= "          ON T1.SCHOOLCD     = L1.SCHOOLCD ";
        $query .= "         AND T1.SCHOOL_KIND  = L1.SCHOOL_KIND ";
        $query .= "         AND T1.YEAR         = L1.YEAR ";
        $query .= "         AND T1.INCOME_L_CD  = L1.LEVY_L_CD ";
        $query .= "         AND T1.INCOME_M_CD  = L1.LEVY_M_CD ";
        $query .= "     INNER JOIN COLLECT_M_MST L2 ";
        $query .= "          ON T1.SCHOOLCD     = L2.SCHOOLCD ";
        $query .= "         AND T1.SCHOOL_KIND  = L2.SCHOOL_KIND ";
        $query .= "         AND T1.YEAR         = L2.YEAR ";
        $query .= "         AND T1.COLLECT_L_CD = L2.COLLECT_L_CD ";
        $query .= "         AND T1.COLLECT_M_CD = L2.COLLECT_M_CD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD     = '".(sprintf("%012d", SCHOOLCD))."' AND ";
        $query .= "     T1.SCHOOL_KIND  = '".$model->schoolKind."' AND ";
        $query .= "     T1.YEAR         = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.AUTO_NO ";

        return $query;
    }

    //１レコード取得
    public function getAutoData($model, $auto_no)
    {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     COLLECT_L_CD || '-' || COLLECT_M_CD AS COLLECT_LM_CD, ";
        $query .= "     INCOME_L_CD || '-' ||INCOME_M_CD AS INCOME_LM_CD ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_AUTO_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->schoolKind."' AND ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     AUTO_NO     = '".$auto_no."' ";

        return $query;
    }

    //入金項目重複チェック
    public function getExistsLM($model, $lmCd)
    {
        $query .= "     SELECT ";
        $query .= "         COUNT(*) AS CNT ";
        $query .= "     FROM ";
        $query .= "         LEVY_REQUEST_INCOME_AUTO_DAT ";
        $query .= "     WHERE ";
        $query .= "         SCHOOLCD        = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "         AND SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "         AND YEAR        = '".CTRL_YEAR."' ";
        $query .= "         AND COLLECT_L_CD || '-' || COLLECT_M_CD = '{$lmCd}' ";

        return $query;
    }

    //生徒取得
    public function getAutoSchregData($model, $auto_no)
    {
        $query  = " SELECT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_AUTO_SCHREG_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->schoolKind."' AND ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     AUTO_NO     = '".$auto_no."' ";

        return $query;
    }

    //入金項目取得
    public function getCollectLMst($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.COLLECT_L_CD || '-' || T2.COLLECT_M_CD AS VALUE, ";
        $query .= "     T1.COLLECT_L_CD || T2.COLLECT_M_CD || ':' || VALUE(T2.COLLECT_M_NAME,'') AS LABEL ";
        $query .= " FROM ";
        $query .= "     COLLECT_L_MST T1 ";
        $query .= "     INNER JOIN COLLECT_M_MST T2 ";
        $query .= "          ON T2.SCHOOLCD     = T1.SCHOOLCD ";
        $query .= "         AND T2.SCHOOL_KIND  = T1.SCHOOL_KIND ";
        $query .= "         AND T2.YEAR         = T1.YEAR ";
        $query .= "         AND T2.COLLECT_L_CD = T1.COLLECT_L_CD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "     T1.SCHOOL_KIND  = '".$model->schoolKind."' AND ";
        $query .= "     T1.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "     T1.LEVY_FLG     = '1' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //預り金項目取得
    public function getLevyMMst($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     LEVY_L_CD || '-' || LEVY_M_CD AS VALUE, ";
        $query .= "     LEVY_L_CD || LEVY_M_CD || ':' || VALUE(LEVY_M_NAME,'') AS LABEL ";
        $query .= " FROM ";
        $query .= "     LEVY_M_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->schoolKind."' AND ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     LEVY_IN_OUT_DIV = '1' AND ";
        $query .= "     ZATU_FLG IS NULL AND ";
        $query .= "     YOBI_FLG IS NULL ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //起案者取得
    public function getRequestStaff($model, $staff)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     STAFFCD || ':' || STAFFNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '{$staff}' ";

        return $query;
    }

    //学年取得
    public function getGrade($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒取得
    public function getSchList($model, $flg="")
    {
        $query  = " SELECT  ";
        $query .= "     T1.GRADE || T1.HR_CLASS || T1.ATTENDNO || '-' || T1.SCHREGNO AS VALUE,  ";
        $query .= "     L1.HR_NAME || T1.ATTENDNO || '番' || '　' || T2.NAME AS LABEL  ";
        $query .= " FROM  ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ";
        $query .= "          ON T1.SCHREGNO     = T2.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T3 ";
        $query .= "          ON T1.YEAR         = T3.YEAR ";
        $query .= "         AND T1.GRADE        = T3.GRADE ";
        $query .= "         AND T3.SCHOOL_KIND  = '".$model->schoolKind."' ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT L1 ";
        $query .= "          ON T1.YEAR         = L1.YEAR ";
        $query .= "         AND T1.SEMESTER     = L1.SEMESTER ";
        $query .= "         AND T1.GRADE        = L1.GRADE ";
        $query .= "         AND T1.HR_CLASS     = L1.HR_CLASS ";
        $query .= " WHERE  ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."'  ";
        $query .= " AND T1.SEMESTER = '".CTRL_SEMESTER."'  ";
        if ($flg) {
            $query .= " AND T1.SCHREGNO IN ('".implode(explode(',', $model->selectdata), "','")."')  ";
        } elseif ($model->grade) {
            $query .= " AND T1.GRADE    = '".$model->grade."' ";
        }
        $query .= " ORDER BY  ";
        $query .= "   VALUE ";

        return $query;
    }

    //削除処理
    public function &getDeleteQuery($model, &$db)
    {
        //削除 -- LEVY_REQUEST_INCOME_AUTO_DAT
        $query  = " DELETE FROM ";
        $query .= "     LEVY_REQUEST_INCOME_AUTO_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->schoolKind."' AND ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     AUTO_NO     = '".sprintf("%03d", $model->field["AUTO_NO"])."' ";
        $db->query($query);

        if ($model->Properties["not_select_schregno_auto_income"] != "1") {
            //削除 -- LEVY_REQUEST_INCOME_AUTO_SCHREG_DAT
            $query  = " DELETE FROM ";
            $query .= "     LEVY_REQUEST_INCOME_AUTO_SCHREG_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $query .= "     SCHOOL_KIND = '".$model->schoolKind."' AND ";
            $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "     AUTO_NO     = '".sprintf("%03d", $model->field["AUTO_NO"])."' ";
            $db->query($query);
        }

        return;
    }

    //更新処理
    public function &getUpdateQuery($model, &$db)
    {
        //更新 -- LEVY_REQUEST_INCOME_AUTO_DAT
        $data = array();
        $data["SCHOOLCD"][TEXT]             = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]          = $model->schoolKind;
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["AUTO_NO"][TEXT]              = sprintf("%03d", $model->field["AUTO_NO"]);
        $data["AUTO_NAME"][TEXT]            = $model->field["AUTO_NAME"];
        $data["INCOME_L_CD"][TEXT]          = $model->field["INCOME_L_CD"];
        $data["INCOME_M_CD"][TEXT]          = $model->field["INCOME_M_CD"];
        $data["REQUEST_REASON"][TEXT]       = $model->field["REQUEST_REASON"];
        if (!$model->field["REQUEST_STAFF"]) {
            $data["REQUEST_STAFF"][TEXT]        = STAFFCD;
        } else {
            $data["REQUEST_STAFF"][TEXT]        = $model->field["REQUEST_STAFF"];
        }
        $data["REMARK"][TEXT]               = $model->field["REMARK"];
        if ($model->Properties["not_select_schregno_auto_income"] != "1") {
            $data["COMMODITY_PRICE"][NUMBER]    = $model->field["COMMODITY_PRICE"];
        } else {
            $data["COMMODITY_PRICE"][NUMBER]    = "0"; //単価は指定しないが、テーブルがNOT NULLなのでダミー値を入れておく
        }
        $data["COLLECT_L_CD"][TEXT]         = $model->field["COLLECT_L_CD"];
        $data["COLLECT_M_CD"][TEXT]         = $model->field["COLLECT_M_CD"];
        $data["COLLECT_S_CD"][TEXT]         = '000';
        if ($model->Properties["disp_approval_chk"] == "1") {
            $data["AUTO_INCOME_APPROVAL"][TEXT]    = $model->field["AUTO_INCOME_APPROVAL"];
        }
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "LEVY_REQUEST_INCOME_AUTO_DAT");
        $db->query($query);

        if ($model->Properties["not_select_schregno_auto_income"] != "1" && $model->selectdata) {
            $schArray = explode(',', $model->selectdata);
            foreach ($schArray as $schno) {
                //更新 -- LEVY_REQUEST_INCOME_AUTO_SCHREG_DAT
                $data = array();
                $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
                $data["SCHOOL_KIND"][TEXT]      = $model->schoolKind;
                $data["YEAR"][TEXT]             = CTRL_YEAR;
                $data["AUTO_NO"][TEXT]          = sprintf("%03d", $model->field["AUTO_NO"]);
                $data["SCHREGNO"][TEXT]         = $schno;
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $query = Query::insertSQL($data, "LEVY_REQUEST_INCOME_AUTO_SCHREG_DAT");
                $db->query($query);
            }
        }

        return;
    }

    //データコピー
    public function &getCopyYearQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除 -- LEVY_REQUEST_INCOME_AUTO_DAT
        $query  = " DELETE FROM ";
        $query .= "     LEVY_REQUEST_INCOME_AUTO_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "     YEAR        = '".CTRL_YEAR."' ";
        $db->query($query);

        if ($model->Properties["not_select_schregno_auto_income"] != "1") {
            //削除 -- LEVY_REQUEST_INCOME_AUTO_SCHREG_DAT
            $query  = " DELETE FROM ";
            $query .= "     LEVY_REQUEST_INCOME_AUTO_SCHREG_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $query .= "     YEAR        = '".CTRL_YEAR."' ";
            $db->query($query);
        }

        //更新 -- LEVY_REQUEST_INCOME_AUTO_DAT
        $query  = " INSERT INTO LEVY_REQUEST_INCOME_AUTO_DAT ";
        $query .= knjp744Query::getCopyQuery($model, 'dat');
        $db->query($query);

        if ($model->Properties["not_select_schregno_auto_income"] != "1") {
            //更新 -- LEVY_REQUEST_INCOME_AUTO_SCHREG_DAT
            $query  = " INSERT INTO LEVY_REQUEST_INCOME_AUTO_SCHREG_DAT ";
            $query .= knjp744Query::getCopyQuery($model, 'schreg');
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }
}
