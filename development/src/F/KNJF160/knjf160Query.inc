<?php

require_once('for_php7.php');

class knjf160Query extends Query
{
    //データを取得（メイン画面）
    function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     S2.HR_NAME, ";
        $query .= "     INT(T1.ATTENDNO) AS ATTENDNO, ";
        $query .= "     S1.NAME, ";
        $query .= "     S1.NAME_KANA, ";
        $query .= "     S1.BIRTHDAY, ";
        $query .= "     S3.ZIPCD, ";
        $query .= "     S3.ADDR1, ";
        $query .= "     S3.ADDR2, ";
        $query .= "     S3.TELNO, ";
        $query .= "     S4.GUARD_NAME, ";
        $query .= "     S4.GUARD_KANA, ";
        $query .= "     S4.RELATIONSHIP, ";
        $query .= "     S4.GUARD_ZIPCD, ";
        $query .= "     S4.GUARD_ADDR1, ";
        $query .= "     S4.GUARD_ADDR2, ";
        $query .= "     S4.GUARD_WORK_NAME, ";
        $query .= "     S4.GUARD_TELNO, ";
        $query .= "     S1.EMERGENCYNAME, ";
        $query .= "     S1.EMERGENCYRELA_NAME, ";
        $query .= "     S1.EMERGENCYCALL, ";
        $query .= "     S1.EMERGENCYTELNO, ";
        $query .= "     S1.EMERGENCYNAME2, ";
        $query .= "     S1.EMERGENCYRELA_NAME2, ";
        $query .= "     S1.EMERGENCYCALL2, ";
        $query .= "     S1.EMERGENCYTELNO2 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST S1 ON ";
        $query .= "             T1.SCHREGNO = S1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT S2 ON ";
        $query .= "             T1.YEAR = S2.YEAR AND ";
        $query .= "             T1.SEMESTER = S2.SEMESTER AND ";
        $query .= "             T1.GRADE = S2.GRADE AND ";
        $query .= "             T1.HR_CLASS = S2.HR_CLASS ";
        $query .= "     LEFT JOIN SCHREG_ADDRESS_DAT S3 ON ";
        $query .= "             T1.SCHREGNO = S3.SCHREGNO AND ";
        $query .= "             S3.ISSUEDATE <= '".CTRL_DATE."' AND ";
        $query .= "             S3.EXPIREDATE >= '".CTRL_DATE."' ";
        $query .= "     LEFT JOIN GUARDIAN_DAT S4 ON ";
        $query .= "             T1.SCHREGNO = S4.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //部クラブマスタ取得
    function getClubName($model)
    {
        $query  = " SELECT ";
        $query .= "     T3.CLUBNAME, ";
        $query .= "     T2.CLUBCD, ";
        $query .= "     T2.SDATE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_CLUB_HIST_DAT T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     LEFT JOIN CLUB_MST T3 ON T2.CLUBCD = T3.CLUBCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     T2.CLUBCD IN(SELECT ";
        $query .= "                     CLUBCD ";
        $query .= "                 FROM ";
        $query .= "                     CLUB_YDAT ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR = '".CTRL_YEAR."' ";
        $query .= "                 ) ";
        $query .= " ORDER BY ";
        $query .= "     T2.SDATE DESC ";

        return $query;
    }

    //スポーツ振興センター
    function getCntBaseRemark1($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND BASE_SEQ = '008' ";
        $query .= "     AND BASE_REMARK1 = '1' ";

        return $query;
    }
    //スポーツ振興センター
    function getCntAnotherSport($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ANOTHER_SCHOOL_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND ANOTHER_SPORT = '1' ";

        return $query;
    }

    //名称マスタ取得
    function getNameMst($namecd1, $namecd2="")
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if($namecd2){
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }

        return $query;
    }

    //クラス名称取得
    function getHrName($model)
    {
        $query  = " SELECT ";
        $query .= "     HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     GRADE = '".$model->grade."' AND ";
        $query .= "     HR_CLASS = '".$model->hr_class."' ";

        return $query;
    }

    //入学前後の病気・けがなどの履歴データ取得
    function getSubQuery1($model, $table, $seq="")
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .=       $table ;
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";
        if($seq){
            $query .= "     AND SEQ = '".$seq."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     INT(SEQ) ";

        return $query;
    }

    // DELETE（入院前後の病気・けが）
    function &getDeleteQuery($db, $model, $table)
    {
        $array = explode (",", implode(',',$model->checked));

        for ($i = 0; $i < get_count($array); $i++){
            $query  = " DELETE FROM ";
            $query .=       $table ;
            $query .= " WHERE ";
            $query .= "     SCHREGNO = '".$model->schregno."' AND ";
            $query .= "     SEQ = '".$array[$i]."' ";

            $db->query($query);
        }
        return ;
    }

    // UPDATE（入学前後の病気・けが）
    function &getUpdateQuery($db, $model, $fields, $insert, $table)
    {
        $seq = ($insert == "insert") ? (int)$db->getOne(knjf160Query::getMaxSeq($db, $model, $table))+1 : $model->seq;

        $data["SCHREGNO"][TEXT]     = $model->schregno;
        $data["SEQ"][DATE]          = $seq;
        $data["DISEASE"][TEXT]      = $fields["DISEASE"];
        $data["S_YEAR"][TEXT]       = $fields["S_YEAR"];
        $data["S_MONTH"][TEXT]      = $fields["S_MONTH"];
        $data["E_YEAR"][TEXT]       = $fields["E_YEAR"];
        $data["E_MONTH"][TEXT]      = $fields["E_MONTH"];
        $data["SITUATION"][TEXT]    = $fields["SITUATION"];

        if($table == "HEALTH_AFT_SICKREC_DAT"){
            $data["HOSPITAL"][TEXT]     = $fields["HOSPITAL"];
            $data["DOCTOR"][TEXT]       = $fields["DOCTOR"];
            $data["TELNO"][TEXT]        = $fields["TELNO"];
            $data["MEDICINE"][TEXT]     = $fields["MEDICINE"];
            $data["SUSPECT"][TEXT]      = $fields["SUSPECT"];
        }

        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        if($insert == "insert"){
            $db->query(Query::insertSQL($data, $table));
        } else {
            $where  = " WHERE ";
            $where .= "     SCHREGNO = '".$model->schregno."' AND ";
            $where .= "     SEQ = '".$seq."' ";

            $db->query(Query::updateSQL($data, $table, $where));
        }
        return;
    }

    //入学前後の病気・けがなどの履歴データのMAX値取得
    function getMaxSeq($db, $model, $table)
    {
        $query  = " SELECT ";
        $query .= "     MAX(SEQ) AS SEQ ";
        $query .= " FROM ";
        $query .=       $table;
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO ";

        return $query;
    }

    //家族情報取得
    function getRelaInfo($model, $relano="")
    {
        $query  = " SELECT ";
        $query .= "     T1.RELANO, ";
        $query .= "     T1.RELANAME, ";
        $query .= "     T1.RELAKANA, ";
        $query .= "     T1.RELATIONSHIP, ";
        $query .= "     T1.RELABIRTHDAY, ";
        $query .= "     T2.REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_RELA_DAT T1 ";
        $query .= "     LEFT JOIN HEALTH_RELA_DAT T2 ON T1.SCHREGNO = T2.SCHREGNO AND T1.RELANO = T2.RELANO ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        if($relano){
            $query .= "     AND T1.RELANO = '".$relano."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     INT(RELANO) ";

        return $query;
    }

    //名称マスタ取得（コンボ用）
    function getNameMst2($namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || '：' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";

        return $query;
    }

    // DELETE（家族情報）
    function &getDeleteQuery3($db, $model)
    {
        $db->autoCommit(false);

        //学籍親族データ
        $query  = " DELETE FROM ";
        $query .= "     SCHREG_RELA_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     RELANO = '".$model->relano."' ";

        $db->query($query);

        //家族保健情報データ
        $query  = " DELETE FROM ";
        $query .= "     HEALTH_RELA_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     RELANO = '".$model->relano."' ";

        $db->query($query);
        $db->commit();
        return ;
    }

    // UPDATE（家族情報）
    function &getUpdateQuery3($db, $model, $fields, $insert)
    {
        $relano = ($insert == "insert") ? strval((int)$db->getOne(knjf160Query::getRelano($model, "schreg", "max"))+1) : strval($model->relano);
        $db->autoCommit(false);

        //学籍親族データ
        $dataS["SCHREGNO"][TEXT]     = $model->schregno;
        $dataS["RELANO"][TEXT]       = $relano;
        $dataS["RELANAME"][TEXT]     = $fields["RELANAME"];
        $dataS["RELAKANA"][TEXT]     = $fields["RELAKANA"];
        $dataS["RELABIRTHDAY"][TEXT] = str_replace("/", "-",$fields["RELABIRTHDAY"]);
        $dataS["RELATIONSHIP"][TEXT] = $fields["RELATIONSHIP"];
        $dataS["REGISTERCD"][TEXT]   = STAFFCD;
        $dataS["UPDATED"][NUMBER]    = "sysdate()";

        if($insert == "insert"){
            $query = Query::insertSQL($dataS, "SCHREG_RELA_DAT");
        } else {
            $where  = " WHERE ";
            $where .= "     SCHREGNO    = '".$model->schregno."' AND ";
            $where .= "     RELANO      = '".$relano."' ";

            $query = Query::updateSQL($dataS, "SCHREG_RELA_DAT", $where);
        }

        $db->query($query);

        //家族保健情報データ
        $dataH["SCHREGNO"][TEXT]     = $model->schregno;
        $dataH["RELANO"][TEXT]       = $relano;
        $dataH["REMARK"][TEXT]       = $fields["REMARK"];
        $dataH["REGISTERCD"][TEXT]   = STAFFCD;
        $dataH["UPDATED"][NUMBER]    = "sysdate()";

        if (!in_array($relano, $db->getCol(knjf160Query::getRelaNo($model, "health", "list")),true)) {
            $query = Query::insertSQL($dataH, "HEALTH_RELA_DAT");
        } else {
            $where  = " WHERE ";
            $where .= "     SCHREGNO    = '".$model->schregno."' AND ";
            $where .= "     RELANO      = '".$relano."' ";

            $query = Query::updateSQL($dataH, "HEALTH_RELA_DAT", $where);
        }

        $db->query($query);
        $db->commit();
        return;
    }

    //親族番号取得
    function getRelano($model, $table, $div)
    {
        $query  = " SELECT ";
        if ($div == "max"){
            $query .= "     MAX(INT(RELANO)) AS RELANO ";
        } else if ($div == "list") {
            $query .= "     RELANO ";
        }
        $query .= " FROM ";
        if ($table == "schreg"){
            $query .= "     SCHREG_RELA_DAT ";
        } else if ($table == "health") {
            $query .= "     HEALTH_RELA_DAT ";
        }
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //その他調査取得
    function getInvestOther($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HEALTH_INVEST_OTHER_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    // UPDATE（その他調査）
    function &getUpdateQuery4($db, $model, $fields)
    {
        $db->autoCommit(false);

        $query  = " DELETE FROM HEALTH_INVEST_OTHER_DAT ";
        $query .= " WHERE SCHREGNO = '".$model->schregno."'";
        $db->query($query);

        $data["SCHREGNO"][TEXT]                 = $model->schregno;
        $data["ALLERGY_MEDICINE"][TEXT]         = $fields["ALLERGY_MEDICINE"];
        $data["ALLERGY_FOOD"][TEXT]             = $fields["ALLERGY_FOOD"];
        $data["ALLERGY_OTHER"][TEXT]            = $fields["ALLERGY_OTHER"];
        $data["BLOOD"][TEXT]                    = $fields["BLOOD"];
        $data["RH"][TEXT]                       = $fields["RH"];
        $data["MEASLES_AGE"][TEXT]              = $fields["MEASLES_AGE"];
        $data["G_MEASLES_AGE"][TEXT]            = $fields["G_MEASLES_AGE"];
        $data["VARICELLA_AGE"][TEXT]            = $fields["VARICELLA_AGE"];
        $data["OTITIS_MEDIA_AGE"][TEXT]         = $fields["OTITIS_MEDIA_AGE"];
        $data["TB_AGE"][TEXT]                   = $fields["TB_AGE"];
        $data["KAWASAKI_AGE"][TEXT]             = $fields["KAWASAKI_AGE"];
        $data["INFECTION_AGE"][TEXT]            = $fields["INFECTION_AGE"];
        $data["MUMPS_AGE"][TEXT]                = $fields["MUMPS_AGE"];
        $data["HEART_DISEASE"][TEXT]            = $fields["HEART_DISEASE"];
        $data["HEART_S_AGE"][TEXT]              = $fields["HEART_S_AGE"];
        $data["HEART_SITUATION"][TEXT]          = $fields["HEART_SITUATION"];
        $data["HEART_E_AGE"][TEXT]              = $fields["HEART_E_AGE"];
        $data["KIDNEY_DISEASE"][TEXT]           = $fields["KIDNEY_DISEASE"];
        $data["KIDNEY_S_AGE"][TEXT]             = $fields["KIDNEY_S_AGE"];
        $data["KIDNEY_SITUATION"][TEXT]         = $fields["KIDNEY_SITUATION"];
        $data["KIDNEY_E_AGE"][TEXT]             = $fields["KIDNEY_E_AGE"];
        $data["ASTHMA_S_AGE"][TEXT]             = $fields["ASTHMA_S_AGE"];
        $data["ASTHMA_SITUATION"][TEXT]         = $fields["ASTHMA_SITUATION"];
        $data["ASTHMA_E_AGE"][TEXT]             = $fields["ASTHMA_E_AGE"];
        $data["CONVULSIONS_S_AGE"][TEXT]        = $fields["CONVULSIONS_S_AGE"];
        $data["CONVULSIONS_SITUATION"][TEXT]    = $fields["CONVULSIONS_SITUATION"];
        $data["CONVULSIONS_E_AGE"][TEXT]        = $fields["CONVULSIONS_E_AGE"];
        $data["OTHER_DISEASE"][TEXT]            = $fields["OTHER_DISEASE"];
        $data["TUBERCULIN"][TEXT]               = $fields["TUBERCULIN"];
        $data["TUBERCULIN_YEAR"][TEXT]          = $fields["TUBERCULIN_YEAR"];
        $data["TUBERCULIN_MONTH"][TEXT]         = $fields["TUBERCULIN_MONTH"];
        $data["TUBERCULIN_JUDGE"][TEXT]         = $fields["TUBERCULIN_JUDGE"];
        $data["BCG"][TEXT]                      = $fields["BCG"];
        $data["BCG_YEAR"][TEXT]                 = $fields["BCG_YEAR"];
        $data["BCG_MONTH"][TEXT]                = $fields["BCG_MONTH"];
        $data["POLIO"][TEXT]                    = $fields["POLIO"];
        $data["POLIO_YEAR"][TEXT]               = $fields["POLIO_YEAR"];
        $data["POLIO_MONTH"][TEXT]              = $fields["POLIO_MONTH"];
        $data["G_MEASLES"][TEXT]                = $fields["G_MEASLES"];
        $data["G_MEASLES_YEAR"][TEXT]           = $fields["G_MEASLES_YEAR"];
        $data["G_MEASLES_MONTH"][TEXT]          = $fields["G_MEASLES_MONTH"];
        $data["VARICELLA"][TEXT]                = $fields["VARICELLA"];
        $data["VARICELLA_YEAR"][TEXT]           = $fields["VARICELLA_YEAR"];
        $data["VARICELLA_MONTH"][TEXT]          = $fields["VARICELLA_MONTH"];
        $data["MUMPS"][TEXT]                    = $fields["MUMPS"];
        $data["MUMPS_YEAR"][TEXT]               = $fields["MUMPS_YEAR"];
        $data["MUMPS_MONTH"][TEXT]              = $fields["MUMPS_MONTH"];
        $data["ENCEPHALITIS"][TEXT]             = $fields["ENCEPHALITIS"];
        $data["ENCEPHALITIS_YEAR1"][TEXT]       = $fields["ENCEPHALITIS_YEAR1"];
        $data["ENCEPHALITIS_MONTH1"][TEXT]      = $fields["ENCEPHALITIS_MONTH1"];
        $data["ENCEPHALITIS_YEAR2"][TEXT]       = $fields["ENCEPHALITIS_YEAR2"];
        $data["ENCEPHALITIS_MONTH2"][TEXT]      = $fields["ENCEPHALITIS_MONTH2"];
        $data["ENCEPHALITIS_YEAR3"][TEXT]       = $fields["ENCEPHALITIS_YEAR3"];
        $data["ENCEPHALITIS_MONTH3"][TEXT]      = $fields["ENCEPHALITIS_MONTH3"];
        $data["ENCEPHALITIS_YEAR4"][TEXT]       = $fields["ENCEPHALITIS_YEAR4"];
        $data["ENCEPHALITIS_MONTH4"][TEXT]      = $fields["ENCEPHALITIS_MONTH4"];
        $data["ENCEPHALITIS_YEAR5"][TEXT]       = $fields["ENCEPHALITIS_YEAR5"];
        $data["ENCEPHALITIS_MONTH5"][TEXT]      = $fields["ENCEPHALITIS_MONTH5"];
        $data["MIXED"][TEXT]                    = $fields["MIXED"];
        $data["MIXED_YEAR1"][TEXT]              = $fields["MIXED_YEAR1"];
        $data["MIXED_MONTH1"][TEXT]             = $fields["MIXED_MONTH1"];
        $data["MIXED_YEAR2"][TEXT]              = $fields["MIXED_YEAR2"];
        $data["MIXED_MONTH2"][TEXT]             = $fields["MIXED_MONTH2"];
        $data["MIXED_YEAR3"][TEXT]              = $fields["MIXED_YEAR3"];
        $data["MIXED_MONTH3"][TEXT]             = $fields["MIXED_MONTH3"];
        $data["MIXED_YEAR4"][TEXT]              = $fields["MIXED_YEAR4"];
        $data["MIXED_MONTH4"][TEXT]             = $fields["MIXED_MONTH4"];
        $data["MIXED_YEAR5"][TEXT]              = $fields["MIXED_YEAR5"];
        $data["MIXED_MONTH5"][TEXT]             = $fields["MIXED_MONTH5"];
        $data["MEASLES"][TEXT]                  = $fields["MEASLES"];
        $data["MEASLES_TIMES"][TEXT]            = $fields["MEASLES_TIMES"];
        $data["MEASLES_YEAR1"][TEXT]            = $fields["MEASLES_YEAR1"];
        $data["MEASLES_MONTH1"][TEXT]           = $fields["MEASLES_MONTH1"];
        $data["MEASLES_YEAR2"][TEXT]            = $fields["MEASLES_YEAR2"];
        $data["MEASLES_MONTH2"][TEXT]           = $fields["MEASLES_MONTH2"];
        $data["MEASLES_YEAR3"][TEXT]            = $fields["MEASLES_YEAR3"];
        $data["MEASLES_MONTH3"][TEXT]           = $fields["MEASLES_MONTH3"];
        $data["VACCINE"][TEXT]                  = $fields["VACCINE"];
        $data["LOT_NO"][TEXT]                   = $fields["LOT_NO"];
        $data["CONFIRMATION"][TEXT]             = $fields["CONFIRMATION"];
        $data["A_MEASLES"][TEXT]                = $fields["A_MEASLES"];
        $data["A_MEASLES_AGE"][TEXT]            = $fields["A_MEASLES_AGE"];
        $data["A_CONFIRMATION"][TEXT]           = $fields["A_CONFIRMATION"];
        $data["ANTIBODY"][TEXT]                 = $fields["ANTIBODY"];
        $data["ANTIBODY_YEAR"][TEXT]            = $fields["ANTIBODY_YEAR"];
        $data["ANTIBODY_MONTH"][TEXT]           = $fields["ANTIBODY_MONTH"];
        $data["ANTIBODY_POSITIVE"][TEXT]        = $fields["ANTIBODY_POSITIVE"];
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][NUMBER]                = "sysdate()";
        $db->query(Query::insertSQL($data, "HEALTH_INVEST_OTHER_DAT"));

        $db->commit();
        return;
    }

    //健康調査データ（ヘッダー部分）
    function selectInvestDat($model)
    {
        $query  = " WITH SCHINFO AS( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         SCHREGNO, ";
        $query .= "         YEAR ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO = '".$model->schregno."' ";
        $query .= " ) ";

        $query .= " SELECT  ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.YEAR, ";
        $query .= "     MAX(T2.E_YEAR || '-' || T2.E_MONTH) AS E_DATE ";
        $query .= " FROM ";
        $query .= "     SCHINFO T1, ";
        $query .= "     HEALTH_INVEST_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.YEAR ";
        $query .= " ORDER BY ";
        $query .= "     T2.YEAR ";

        return $query;
    }

    //校種取得
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "    SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "    SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "        YEAR  = '".CTRL_YEAR."' ";
        $query .= "    AND GRADE = '".$model->grade."' ";

        return $query;
    }

    //在籍した学年取得
    function getRegdGrade($model, $year="")
    {
        $query  = " SELECT ";
        $query .= "     T1.YEAR AS VALUE, ";
        $query .= "     T2.GRADE_NAME1 AS LABEL, ";
        $query .= "     MAX(T1.SEMESTER) AS SEMESTER ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ";
        $query .= "              ON T1.YEAR     = T2.YEAR ";
        $query .= "             AND T1.GRADE    = T2.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "             AND T2.SCHOOL_KIND = '".$model->schKind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "             AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     T1.YEAR NOT IN (SELECT ";
        $query .= "                         S1.YEAR ";
        $query .= "                     FROM ";
        $query .= "                         HEALTH_INVEST_DAT S1 ";
        $query .= "                     WHERE ";
        $query .= "                         S1.SCHREGNO = T1.SCHREGNO) ";
        $query .= " GROUP BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T2.GRADE_NAME1 ";
        if ($year) {
            $query .= " UNION  ";
            $query .= " SELECT ";
            $query .= "     T1.YEAR AS VALUE, ";
            $query .= "     T2.GRADE_NAME1 AS LABEL, ";
            $query .= "     MAX(T1.SEMESTER) AS SEMESTER ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT T1 ";
            $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ";
            $query .= "              ON T1.YEAR     = T2.YEAR ";
            $query .= "             AND T1.GRADE    = T2.GRADE ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= "             AND T2.SCHOOL_KIND = '".$model->schKind."' ";
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "             AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= " WHERE ";
            $query .= "     T1.SCHREGNO = '".$model->schregno."' AND ";
            $query .= "     T1.YEAR     = '".$year."' ";
            $query .= " GROUP BY ";
            $query .= "     T1.YEAR, ";
            $query .= "     T2.GRADE_NAME1 ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //健康調査データの取得
    function selectQuery5($model, $year_key)
    {
        $query  = " WITH INVEST AS( ";
        $query .= "     SELECT ";
        $query .= "         QUESTIONCD ";
        foreach ($year_key as $key => $value)
        {
            $query .= "    ,MAX(CASE WHEN YEAR = '".$value."' THEN ANSWER ELSE NULL END) AS ANSWER".$key;
        }
        $query .= "     FROM ";
        $query .= "         HEALTH_INVEST_DAT ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO = '".$model->schregno."' ";
        if (get_count($year_key) > 0) {
            $query .= "         AND YEAR IN('".implode("','", $year_key)."') ";
        }
        $query .= "     GROUP BY ";
        $query .= "         QUESTIONCD ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.QUESTIONCD, ";
        $query .= "     T1.CONTENTS, ";
        $query .= "     T1.SORT ";
        foreach ($year_key as $key => $value)
        {
            $query .= "     , T2.ANSWER".$key;
        }
        $query .= " FROM ";
        $query .= "     HEALTH_QUESTION_MST T1 ";
        $query .= "     LEFT JOIN INVEST T2 ON T2.QUESTIONCD = T1.QUESTIONCD ";
        $query .= " ORDER BY ";
        $query .= "     INT(T1.SORT), ";
        $query .= "     INT(T1.QUESTIONCD) ";

        return $query;

    }

    //健康調査（注意すべき点）取得
    function getInvestAttention($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HEALTH_INVEST_ATTENTION_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    // UPDATE（健康調査）
    function &getUpdateQuery5($db, $model)
    {
        $db->autoCommit(false);

        $query  = " DELETE FROM HEALTH_INVEST_DAT ";
        $query .= " WHERE SCHREGNO = '".$model->schregno."'";
        $db->query($query);

        for($i=0; $i<get_count($model->data["QUESTIONCD"]); $i++){

            if(get_count($model->data["YEAR"]) > 0){

                foreach ($model->data["YEAR"] as $code => $col) {

                    if($model->fields["GRADE".$code][$i]){

                        $data["SCHREGNO"][TEXT]         = $model->schregno;
                        $data["YEAR"][TEXT]             = $model->fields["GRADE".$code][$i];
                        $data["E_YEAR"][TEXT]           = $model->fields["E_YEAR".$code][$i];
                        $data["E_MONTH"][TEXT]          = ($model->fields["E_MONTH".$code][$i]) ? intval($model->fields["E_MONTH".$code][$i]) : "";
                        $data["QUESTIONCD"][TEXT]       = $model->data["QUESTIONCD"][$i];
                        $data["ANSWER"][TEXT]           = $model->fields["ANSWER".$code][$i];
                        $data["REGISTERCD"][TEXT]       = STAFFCD;
                        $data["UPDATED"][NUMBER]        = "sysdate()";

                        $db->query(Query::insertSQL($data, "HEALTH_INVEST_DAT"));
                    }
                }
            }

            if($model->field["GRADE"]){

                $code2 = (int)$code+1;

                $data_N["SCHREGNO"][TEXT]         = $model->schregno;
                $data_N["YEAR"][TEXT]             = $model->field["GRADE"];
                $data_N["E_YEAR"][TEXT]           = $model->field["E_YEAR"];
                $data_N["E_MONTH"][TEXT]          = $model->field["E_MONTH"];
                $data_N["QUESTIONCD"][TEXT]       = $model->data["QUESTIONCD"][$i];
                $data_N["ANSWER"][TEXT]           = $model->fields["ANSWER".$code2][$i];
                $data_N["REGISTERCD"][TEXT]       = STAFFCD;
                $data_N["UPDATED"][NUMBER]        = "sysdate()";

                $db->query(Query::insertSQL($data_N, "HEALTH_INVEST_DAT"));
            }
        }

        $query  = " DELETE FROM HEALTH_INVEST_ATTENTION_DAT ";
        $query .= " WHERE SCHREGNO = '".$model->schregno."'";
        $db->query($query);

        if($model->field["ATTENTION"]){
            $data_A["SCHREGNO"][TEXT]         = $model->schregno;
            $data_A["ATTENTION"][TEXT]        = $model->field["ATTENTION"];
            $data_A["REGISTERCD"][TEXT]       = STAFFCD;
            $data_A["UPDATED"][NUMBER]        = "sysdate()";

            $db->query(Query::insertSQL($data_A, "HEALTH_INVEST_ATTENTION_DAT"));
        }

        $db->commit();
        return;
    }

    //健康調査取得
    function getInvestData($model)
    {
        $query  = " WITH REGD_INFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         YEAR, ";
        $query .= "         MIN(SEMESTER) AS SEMESTER, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO = '".$model->schregno."' ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        $query .= "         YEAR, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS ";
        $query .= " ), SCH_INFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.*, ";
        $query .= "         T2.GRADE_NAME ";
        $query .= "     FROM ";
        $query .= "         REGD_INFO T1, ";
        $query .= "         SCHREG_REGD_HDAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = T2.YEAR AND ";
        $query .= "         T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "         T1.GRADE = T2.GRADE AND ";
        $query .= "         T1.HR_CLASS = T2.HR_CLASS ";
        $query .= " ), QUESTION AS( ";
        $query .= "     SELECT ";
        $query .= "         T1.QUESTIONCD, ";
        $query .= "         T1.CONTENTS, ";
        $query .= "         T1.SORT, ";
        $query .= "         T2.SCHREGNO, ";
        $query .= "         T2.YEAR, ";
        $query .= "         T2.E_YEAR, ";
        $query .= "         T2.E_MONTH, ";
        $query .= "         T2.ANSWER ";
        $query .= "     FROM ";
        $query .= "         HEALTH_QUESTION_MST T1 ";
        $query .= "         LEFT JOIN HEALTH_INVEST_DAT T2 ON ";
        $query .= "             T1.QUESTIONCD = T2.QUESTIONCD AND ";
        $query .= "             T2.SCHREGNO = '".$model->schregno."' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T2.GRADE_NAME ";
        $query .= " FROM ";
        $query .= "     QUESTION T1  ";
        $query .= "     LEFT JOIN SCH_INFO T2 ON ";
        $query .= "         T1.YEAR = T2.YEAR AND ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.SORT, ";
        $query .= "     T1.QUESTIONCD, ";
        $query .= "     T1.YEAR ";

        return $query;
    }


    //保健室記入取得
    function getNurseEnt($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HEALTH_NURSE_ENT_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    // UPDATE（保健室記入）
    function &getUpdateQuery6($db, $model, $fields)
    {
        $db->autoCommit(false);

        $query  = " DELETE FROM HEALTH_NURSE_ENT_DAT ";
        $query .= " WHERE SCHREGNO = '".$model->schregno."'";
        $db->query($query);

        $data["SCHREGNO"][TEXT]         = $model->schregno;
        $data["INSURED_NAME"][TEXT]     = $fields["INSURED_NAME"];
        $data["INSURED_MARK"][TEXT]     = $fields["INSURED_MARK"];
        $data["INSURED_NO"][TEXT]       = $fields["INSURED_NO"];
        $data["INSURANCE_NAME"][TEXT]   = $fields["INSURANCE_NAME"];
        $data["INSURANCE_NO"][TEXT]     = $fields["INSURANCE_NO"];
        $data["VALID_DATE"][TEXT]       = str_replace("/", "-",$fields["VALID_DATE"]);
        $data["AUTHORIZE_DATE"][TEXT]   = str_replace("/", "-",$fields["AUTHORIZE_DATE"]);
        $data["RELATIONSHIP"][TEXT]     = $fields["RELATIONSHIP"];
        $data["REMARK"][TEXT]           = $fields["INFIARMARY_REMARK"];
        $data["ATTENTION"][TEXT]        = $fields["ATTENTION"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $db->query(Query::insertSQL($data, "HEALTH_NURSE_ENT_DAT"));

        $db->commit();
        return;
    }

    //テーブル存在チェック
    function checkTableExist() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSTABLES ";
        $query .= " WHERE ";
        $query .= "     NAME = 'SETTING_DAT' ";

        return $query;
    }

    //生徒項目名取得
    function getSchName($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     REMARK1 ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "    , SCHOOLCD ";
        }
        $query .= " FROM ";
        $query .= "     SETTING_DAT ";
        $query .= " WHERE ";
        $query .= "     SEQ = '001' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOL_KIND IN (SELECT ";
            $query .= "                         S2.SCHOOL_KIND ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_DAT S1, ";
            $query .= "                         SCHREG_REGD_GDAT S2 ";
            $query .= "                     WHERE ";
            $query .= "                         S1.YEAR     = S2.YEAR AND ";
            $query .= "                         S1.YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "                         S1.SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "                         S1.GRADE    = S2.GRADE AND ";
            $query .= "                         S1.SCHREGNO = '".$model->schregno."' ";
            $query .= "                     ) ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else {
            $query .= " AND SCHOOL_KIND IN (SELECT ";
            $query .= "                         S2.SCHOOL_KIND ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_DAT S1, ";
            $query .= "                         SCHREG_REGD_GDAT S2 ";
            $query .= "                     WHERE ";
            $query .= "                         S1.YEAR     = S2.YEAR AND ";
            $query .= "                         S1.YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "                         S1.SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "                         S1.GRADE    = S2.GRADE AND ";
            $query .= "                         S1.SCHREGNO = '".$model->schregno."' ";
            $query .= "                     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     SCHOOLCD ";
    }
        return $query;
    }
}
?>
