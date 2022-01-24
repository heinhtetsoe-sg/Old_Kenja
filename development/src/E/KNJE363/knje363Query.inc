<?php

require_once('for_php7.php');
class knje363query extends Query
{
    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID   = 'KNJE363' AND ";
        $query .= "     INVALID_FLG = '0' ";

        return $query;
    }

    //年度一覧
    public function getYearSeme($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR || '年度 ' || T2.SEMESTERNAME AS LABEL, ";
        $query .= "     T1.YEAR || T1.SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN SEMESTER_MST T2 ON T1.YEAR       = T2.YEAR ";
        $query .= "                               AND T1.SEMESTER   = T2.SEMESTER ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
                $query .= "          ON G1.YEAR         = T1.YEAR ";
                $query .= "         AND G1.GRADE        = T1.GRADE ";
                $query .= "         AND G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
            $query .= "          ON G1.YEAR         = T1.YEAR ";
            $query .= "         AND G1.GRADE        = T1.GRADE ";
            $query .= "         AND G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //会社一覧
    public function getCompanyMst()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     COMPANY_CD AS VALUE, ";
        $query .= "     COMPANY_CD || ':' || COMPANY_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COMPANY_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //更新データの有無
    public function updDataExists($year, $seq, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     AFT_GRAD_COURSE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$year."' AND ";
        $query .= "     SEQ         =  ".$seq." AND ";
        $query .= "     SCHREGNO    = '".$schregno."' ";

        return $query;
    }

    //存在チェック -- COMPANY_MST
    public function chkCompanyMst($company_cd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     COMPANY_MST ";
        $query .= " WHERE ";
        $query .= "     COMPANY_CD = '".$company_cd."' ";

        return $query;
    }

    //存在チェック -- JOBTYPE_L_MST
    public function chkJobtypeLMst($jobtype_lcd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     JOBTYPE_L_MST ";
        $query .= " WHERE ";
        $query .= "     JOBTYPE_LCD = '".$jobtype_lcd."' ";

        return $query;
    }

    //存在チェック -- JOBTYPE_M_MST
    public function chkJobtypeMMst($jobtype_lcd, $jobtype_mcd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     JOBTYPE_M_MST ";
        $query .= " WHERE ";
        $query .= "     JOBTYPE_LCD = '".$jobtype_lcd."' AND ";
        $query .= "     JOBTYPE_MCD = '".$jobtype_mcd."' ";

        return $query;
    }

    //存在チェック -- JOBTYPE_S_MST
    public function chkJobtypeSMst($jobtype_lcd, $jobtype_mcd, $jobtype_scd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     JOBTYPE_S_MST ";
        $query .= " WHERE ";
        $query .= "     JOBTYPE_LCD = '".$jobtype_lcd."' AND ";
        $query .= "     JOBTYPE_MCD = '".$jobtype_mcd."' AND ";
        $query .= "     JOBTYPE_SCD = '".$jobtype_scd."' ";

        return $query;
    }

    //存在チェック -- PREF_MST
    public function chkPrefMst($pref_cd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     PREF_MST ";
        $query .= " WHERE ";
        $query .= "     PREF_CD = '".$pref_cd."' ";

        return $query;
    }

    //存在チェック -- V_NAME_MST
    public function chkNameMst($year, $namecd1, $namecd2, $namespare1 = "")
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' AND ";
        $query .= "     NAMECD2 = '".$namecd2."' ";
        if ($namespare1) {
            $query .= " AND NAMESPARE1 = '".$namespare1."' ";
        }

        return $query;
    }

    //新規
    public function insertQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $seq = $db->getOne(knje363Query::getMaxSeq($data_arr[$i]["YEAR"])) + 1;

            $data = array();
            //AFT_GRAD_COURSE_DAT
            $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
            $data["SEQ"][NUMBER]            = $seq;
            $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
            $data["SENKOU_KIND"][TEXT]      = "1";
            $data["TOROKU_DATE"][TEXT]      = $data_arr[$i]["TOROKU_DATE"];
            $data["STAT_CD"][TEXT]          = $data_arr[$i]["STAT_CD"];
            $data["JOBTYPE_LCD"][TEXT]      = $data_arr[$i]["JOBTYPE_LCD"];
            $data["JOBTYPE_MCD"][TEXT]      = $data_arr[$i]["JOBTYPE_MCD"];
            $data["JOBTYPE_SCD"][TEXT]      = $data_arr[$i]["JOBTYPE_SCD"];
            $data["PREF_CD"][TEXT]          = $data_arr[$i]["PREF_CD"];
            $data["INTRODUCTION_DIV"][TEXT] = $data_arr[$i]["INTRODUCTION_DIV"];
            $data["HOWTOEXAM"][TEXT]        = $data_arr[$i]["HOWTOEXAM"];
            $setDecision = $data_arr[$i]["DECISION1"];
            $setDecision = $data_arr[$i]["DECISION2"] ? $data_arr[$i]["DECISION2"] : $setDecision;
            $setDecision = $data_arr[$i]["DECISION3"] ? $data_arr[$i]["DECISION3"] : $setDecision;
            $data["DECISION"][TEXT]         = $setDecision;
            $data["PLANSTAT"][TEXT]         = $data_arr[$i]["PLANSTAT"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $query = Query::insertSQL($data, "AFT_GRAD_COURSE_DAT");
            $db->query($query);

            //AFT_GRAD_COURSE_DETAIL_DAT
            $query  = " DELETE FROM ";
            $query .= "     AFT_GRAD_COURSE_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR        = '".$data_arr[$i]["YEAR"]."' AND ";
            $query .= "     SEQ         = ".$seq." AND ";
            $query .= "     DETAIL_SEQ  = 2 ";
            $db->query($query);

            $data = array();
            $data["YEAR"][TEXT]         = $data_arr[$i]["YEAR"];
            $data["SEQ"][NUMBER]        = $seq;
            $data["DETAIL_SEQ"][NUMBER] = 2;
            $data["REMARK1"][TEXT]      = $data_arr[$i]["REMARK1"];
            $data["REMARK2"][TEXT]      = $data_arr[$i]["REMARK2"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "SYSDATE()";

            $query = Query::insertSQL($data, "AFT_GRAD_COURSE_DETAIL_DAT");
            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //更新
    public function updateQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $seq = $data_arr[$i]["SEQ"];

            $data = array();
            //AFT_GRAD_COURSE_DAT
            $data["SENKOU_KIND"][TEXT]      = "1";
            $data["TOROKU_DATE"][TEXT]      = $data_arr[$i]["TOROKU_DATE"];
            $data["STAT_CD"][TEXT]          = $data_arr[$i]["STAT_CD"];
            $data["JOBTYPE_LCD"][TEXT]      = $data_arr[$i]["JOBTYPE_LCD"];
            $data["JOBTYPE_MCD"][TEXT]      = $data_arr[$i]["JOBTYPE_MCD"];
            $data["JOBTYPE_SCD"][TEXT]      = $data_arr[$i]["JOBTYPE_SCD"];
            $data["PREF_CD"][TEXT]          = $data_arr[$i]["PREF_CD"];
            $data["INTRODUCTION_DIV"][TEXT] = $data_arr[$i]["INTRODUCTION_DIV"];
            $data["HOWTOEXAM"][TEXT]        = $data_arr[$i]["HOWTOEXAM"];
            $setDecision = $data_arr[$i]["DECISION1"];
            $setDecision = $data_arr[$i]["DECISION2"] ? $data_arr[$i]["DECISION2"] : $setDecision;
            $setDecision = $data_arr[$i]["DECISION3"] ? $data_arr[$i]["DECISION3"] : $setDecision;
            $data["DECISION"][TEXT]         = $setDecision;
            $data["PLANSTAT"][TEXT]         = $data_arr[$i]["PLANSTAT"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ";
            $where .= "     YEAR        = '".$data_arr[$i]["YEAR"]."' AND ";
            $where .= "     SEQ         = ".$seq." AND ";
            $where .= "     SCHREGNO    = '".$data_arr[$i]["SCHREGNO"]."' ";

            $query = Query::updateSQL($data, "AFT_GRAD_COURSE_DAT", $where);
            $db->query($query);

            $data = array();
            //AFT_GRAD_COURSE_DETAIL_DAT
            $query  = " DELETE FROM ";
            $query .= "     AFT_GRAD_COURSE_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR        = '".$data_arr[$i]["YEAR"]."' AND ";
            $query .= "     SEQ         = ".$seq." AND ";
            $query .= "     DETAIL_SEQ  = 2 ";
            $db->query($query);

            $data["YEAR"][TEXT]         = $data_arr[$i]["YEAR"];
            $data["SEQ"][NUMBER]        = $seq;
            $data["DETAIL_SEQ"][NUMBER] = 2;
            $data["REMARK1"][TEXT]      = $data_arr[$i]["REMARK1"];
            $data["REMARK2"][TEXT]      = $data_arr[$i]["REMARK2"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "SYSDATE()";

            $query = Query::insertSQL($data, "AFT_GRAD_COURSE_DETAIL_DAT");
            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //SEQのMAX値取得
    public function getMaxSeq($year)
    {
        $query  = " SELECT ";
        $query .= "     MAX(SEQ) AS SEQ ";
        $query .= " FROM ";
        $query .= "     AFT_GRAD_COURSE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= " GROUP BY ";
        $query .= "     YEAR ";

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

    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     AFT.SEQ, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     AFT.TOROKU_DATE, ";
        $query .= "     AFT.STAT_CD, ";
        $query .= "     COMPANY.COMPANY_NAME, ";
        $query .= "     AFT.JOBTYPE_LCD, ";
        $query .= "     JOBTYPE_L.JOBTYPE_LNAME, ";
        $query .= "     AFT.JOBTYPE_MCD, ";
        $query .= "     JOBTYPE_M.JOBTYPE_MNAME, ";
        $query .= "     AFT.JOBTYPE_SCD, ";
        $query .= "     JOBTYPE_S.JOBTYPE_SNAME, ";
        $query .= "     AFT.PREF_CD, ";
        $query .= "     PREF.PREF_NAME, ";
        $query .= "     AFT.INTRODUCTION_DIV, ";
        $query .= "     AFT_D.REMARK1, ";
        $query .= "     AFT_D.REMARK2, ";
        $query .= "     AFT.HOWTOEXAM, ";
        $query .= "     CASE WHEN N1.NAMESPARE1 = '1' THEN AFT.DECISION ELSE '' END AS DECISION1, ";
        $query .= "     CASE WHEN N1.NAMESPARE1 = '2' THEN AFT.DECISION ELSE '' END AS DECISION2, ";
        $query .= "     CASE WHEN N1.NAMESPARE1 = '3' THEN AFT.DECISION ELSE '' END AS DECISION3, ";
        $query .= "     AFT.PLANSTAT, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ";
        $query .= "                  ON T2.SCHREGNO = T1.SCHREGNO ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
                $query .= "          ON G1.YEAR         = T1.YEAR ";
                $query .= "         AND G1.GRADE        = T1.GRADE ";
                $query .= "         AND G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
            $query .= "          ON G1.YEAR         = T1.YEAR ";
            $query .= "         AND G1.GRADE        = T1.GRADE ";
            $query .= "         AND G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= "     INNER JOIN AFT_GRAD_COURSE_DAT AFT ";
        $query .= "                  ON AFT.YEAR        = T1.YEAR ";
        $query .= "                 AND AFT.SCHREGNO    = T1.SCHREGNO ";
        $query .= "                 AND AFT.SENKOU_KIND = '1' ";
        $query .= "     LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT AFT_D ";
        $query .= "                  ON AFT_D.YEAR      = AFT.YEAR ";
        $query .= "                 AND AFT_D.SEQ       = AFT.SEQ ";
        $query .= "                 AND AFT_D.DETAIL_SEQ = 2 ";
        $query .= "     LEFT JOIN COMPANY_MST COMPANY ";
        $query .= "                  ON COMPANY.COMPANY_CD = AFT.STAT_CD ";
        $query .= "     LEFT JOIN JOBTYPE_L_MST JOBTYPE_L ";
        $query .= "                  ON JOBTYPE_L.JOBTYPE_LCD = AFT.JOBTYPE_LCD ";
        $query .= "     LEFT JOIN JOBTYPE_M_MST JOBTYPE_M ";
        $query .= "                  ON JOBTYPE_M.JOBTYPE_LCD = AFT.JOBTYPE_LCD ";
        $query .= "                 AND JOBTYPE_M.JOBTYPE_MCD = AFT.JOBTYPE_MCD ";
        $query .= "     LEFT JOIN JOBTYPE_S_MST JOBTYPE_S ";
        $query .= "                  ON JOBTYPE_S.JOBTYPE_LCD = AFT.JOBTYPE_LCD ";
        $query .= "                 AND JOBTYPE_S.JOBTYPE_MCD = AFT.JOBTYPE_MCD ";
        $query .= "                 AND JOBTYPE_S.JOBTYPE_SCD = AFT.JOBTYPE_SCD ";
        $query .= "     LEFT JOIN PREF_MST PREF ";
        $query .= "                  ON PREF.PREF_CD = AFT.PREF_CD ";
        $query .= "     LEFT JOIN NAME_MST N1 ";
        $query .= "                  ON N1.NAMECD1 = 'E005' ";
        $query .= "                 AND N1.NAMECD2 = AFT.DECISION ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        if ($model->field["COMPANY_CD"]) {
            $query .= "     AND AFT.STAT_CD = '".$model->field["COMPANY_CD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }
}
