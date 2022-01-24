<?php

require_once('for_php7.php');
class knjf164query extends Query
{

    //年度・学期一覧
    public function getYearSemester($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR || '年度 ' || T2.SEMESTERNAME AS LABEL, ";
        $query .= "     T1.YEAR || T1.SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1, ";
        $query .= "     SEMESTER_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.GRADE IN (SELECT ";
                $query .= "                     G1.GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT G1 ";
                $query .= "                 WHERE ";
                $query .= "                     G1.YEAR         = T1.YEAR AND ";
                $query .= "                     G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                $query .= "                 ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.GRADE IN (SELECT ";
            $query .= "                     G1.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT G1 ";
            $query .= "                 WHERE ";
            $query .= "                     G1.YEAR         = T1.YEAR AND ";
            $query .= "                     G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組一覧
    public function getGradeHrclass($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     HR_NAME AS LABEL, ";
        $query .= "     GRADE || HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR || SEMESTER = '".$model->field["YEAR"]."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.GRADE IN (SELECT ";
                $query .= "                     G1.GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT G1 ";
                $query .= "                 WHERE ";
                $query .= "                     G1.YEAR         = T1.YEAR AND ";
                $query .= "                     G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                $query .= "                 ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.GRADE IN (SELECT ";
            $query .= "                     G1.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT G1 ";
            $query .= "                 WHERE ";
            $query .= "                     G1.YEAR         = T1.YEAR AND ";
            $query .= "                     G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入学前病気けが入院歴SEQ取得
    public function getSickRecSEQ($schregno, $table, $div)
    {
        $query  = " SELECT ";
        if ($div == "max") {
            $query .= "     MAX(INT(SEQ)) ";
        } elseif ($div == "list") {
            $query .= "     SEQ ";
        }
        $query .= " FROM ";
        if ($table == "before") {
            $query .= "     HEALTH_BEF_SICKREC_DAT ";
        } else {
            $query .= "     HEALTH_AFT_SICKREC_DAT ";
        }
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //更新（病気けが（入学前））
    public function insertQueryCsv1($model, &$data_arr)
    {
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $seq = ($data_arr[$i]["SEQ"] == "") ? $db->getOne(knjf164Query::getSickRecSEQ($data_arr[$i]["SCHREGNO"], "before", "max"))+1 : $data_arr[$i]["SEQ"];

            //データセット
            $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
            $data["SEQ"][TEXT]              = $seq;
            $data["DISEASE"][TEXT]          = $data_arr[$i]["DISEASE"];
            $data["S_YEAR"][TEXT]           = $data_arr[$i]["S_YEAR"];
            $data["S_MONTH"][TEXT]          = $data_arr[$i]["S_MONTH"];
            $data["E_YEAR"][TEXT]           = $data_arr[$i]["E_YEAR"];
            $data["E_MONTH"][TEXT]          = $data_arr[$i]["E_MONTH"];
            $data["SITUATION"][TEXT]        = $data_arr[$i]["SITUATION"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            if ($data_arr[$i]["SEQ"] == "") {
                $query = Query::insertSQL($data, "HEALTH_BEF_SICKREC_DAT");
            } else {
                $where  = " WHERE SCHREGNO  = '".$data_arr[$i]["SCHREGNO"]."' AND ";
                $where .= "       SEQ       = '".$data_arr[$i]["SEQ"]."' ";
                $query = Query::updateSQL($data, "HEALTH_BEF_SICKREC_DAT", $where);
            }

            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //削除（病気けが（入学前））
    public function deleteQueryCsv1($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $query  = " DELETE FROM HEALTH_BEF_SICKREC_DAT ";
            $query .= " WHERE SCHREGNO  = '".$data_arr[$i]["SCHREGNO"]."' AND ";
            $query .= "       SEQ       = '".$data_arr[$i]["SEQ"]."' ";

            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //更新（病気けが（入学後））
    public function insertQueryCsv2($model, &$data_arr)
    {
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $seq = ($data_arr[$i]["SEQ"] == "") ? $db->getOne(knjf164Query::getSickRecSEQ($data_arr[$i]["SCHREGNO"], "after", "max"))+1 : $data_arr[$i]["SEQ"];

            //データセット
            $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
            $data["SEQ"][TEXT]              = $seq;
            $data["DISEASE"][TEXT]          = $data_arr[$i]["DISEASE"];
            $data["S_YEAR"][TEXT]           = $data_arr[$i]["S_YEAR"];
            $data["S_MONTH"][TEXT]          = $data_arr[$i]["S_MONTH"];
            $data["E_YEAR"][TEXT]           = $data_arr[$i]["E_YEAR"];
            $data["E_MONTH"][TEXT]          = $data_arr[$i]["E_MONTH"];
            $data["HOSPITAL"][TEXT]         = $data_arr[$i]["HOSPITAL"];
            $data["DOCTOR"][TEXT]           = $data_arr[$i]["DOCTOR"];
            $data["TELNO"][TEXT]            = $data_arr[$i]["TELNO"];
            $data["MEDICINE"][TEXT]         = $data_arr[$i]["MEDICINE"];
            $data["SITUATION"][TEXT]        = $data_arr[$i]["SITUATION"];
            $data["SUSPECT"][TEXT]          = ($data_arr[$i]["SUSPECT"] == "1") ? "1" : "";
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            if ($data_arr[$i]["SEQ"] == "") {
                $query = Query::insertSQL($data, "HEALTH_AFT_SICKREC_DAT");
            } else {
                $where  = " WHERE SCHREGNO  = '".$data_arr[$i]["SCHREGNO"]."' AND ";
                $where .= "       SEQ       = '".$data_arr[$i]["SEQ"]."' ";
                $query = Query::updateSQL($data, "HEALTH_AFT_SICKREC_DAT", $where);
            }

            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //削除（病気けが（入学後））
    public function deleteQueryCsv2($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $query  = " DELETE FROM HEALTH_AFT_SICKREC_DAT ";
            $query .= " WHERE SCHREGNO  = '".$data_arr[$i]["SCHREGNO"]."' AND ";
            $query .= "       SEQ       = '".$data_arr[$i]["SEQ"]."' ";

            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //親族番号取得
    public function getRelaNo($schregno, $table, $div)
    {
        $query  = " SELECT ";
        if ($div == "max") {
            $query .= "     MAX(INT(RELANO)) ";
        } elseif ($div == "list") {
            $query .= "     RELANO ";
        }
        $query .= " FROM ";
        if ($table == "schreg") {
            $query .= "     SCHREG_RELA_DAT ";
        } elseif ($table == "health") {
            $query .= "     HEALTH_RELA_DAT ";
        }
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //更新（家族情報）
    public function insertQueryCsv3($model, &$data_arr)
    {
        $dataS = array();
        $dataH = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $relano = ($data_arr[$i]["RELANO"] == "") ? strval($db->getOne(knjf164Query::getRelaNo($data_arr[$i]["SCHREGNO"], "schreg", "max"))+1) : strval($data_arr[$i]["RELANO"]);

            //学籍親族データ
            $dataS["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
            $dataS["RELANO"][TEXT]           = $relano;
            $dataS["RELANAME"][TEXT]         = $data_arr[$i]["RELANAME"];
            $dataS["RELAKANA"][TEXT]         = $data_arr[$i]["RELAKANA"];
            $dataS["RELABIRTHDAY"][TEXT]     = str_replace("/", "-", $data_arr[$i]["RELABIRTHDAY"]);
            $dataS["RELATIONSHIP"][TEXT]     = $data_arr[$i]["RELATIONSHIP"];
            $dataS["REGISTERCD"][TEXT]       = STAFFCD;
            $dataS["UPDATED"][NUMBER]        = "SYSDATE()";

            if ($data_arr[$i]["RELANO"] == "") {
                $query = Query::insertSQL($dataS, "SCHREG_RELA_DAT");
            } else {
                $where  = " WHERE SCHREGNO  = '".$data_arr[$i]["SCHREGNO"]."' AND ";
                $where .= "       RELANO    = '".$data_arr[$i]["RELANO"]."' ";
                $query = Query::updateSQL($dataS, "SCHREG_RELA_DAT", $where);
            }

            $db->query($query);

            //家族保健情報データ
            $dataH["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
            $dataH["RELANO"][TEXT]           = $relano;
            $dataH["REMARK"][TEXT]           = $data_arr[$i]["REMARK"];
            $dataH["REGISTERCD"][TEXT]       = STAFFCD;
            $dataH["UPDATED"][NUMBER]        = "SYSDATE()";

            if (!in_array($relano, $db->getCol(knjf164Query::getRelaNo($data_arr[$i]["SCHREGNO"], "health", "list")), true)) {
                $query = Query::insertSQL($dataH, "HEALTH_RELA_DAT");
            } else {
                $where  = " WHERE SCHREGNO  = '".$data_arr[$i]["SCHREGNO"]."' AND ";
                $where .= "       RELANO    = '".$relano."' ";
                $query = Query::updateSQL($dataH, "HEALTH_RELA_DAT", $where);
            }

            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //削除（家族情報）
    public function deleteQueryCsv3($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //学籍親族データ
            $query  = " DELETE FROM SCHREG_RELA_DAT ";
            $query .= " WHERE SCHREGNO  = '".$data_arr[$i]["SCHREGNO"]."' AND ";
            $query .= "       RELANO    = '".$data_arr[$i]["RELANO"]."' ";

            $db->query($query);

            //家族保健情報データ
            $query  = " DELETE FROM HEALTH_RELA_DAT ";
            $query .= " WHERE SCHREGNO  = '".$data_arr[$i]["SCHREGNO"]."' AND ";
            $query .= "       RELANO    = '".$data_arr[$i]["RELANO"]."' ";

            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //更新（その他病気））
    public function insertQueryCsv4($model, &$data_arr)
    {
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //データセット
            $data["SCHREGNO"][TEXT]                 = $data_arr[$i]["SCHREGNO"];
            $data["ALLERGY_MEDICINE"][TEXT]         = $data_arr[$i]["ALLERGY_MEDICINE"];
            $data["ALLERGY_FOOD"][TEXT]             = $data_arr[$i]["ALLERGY_FOOD"];
            $data["ALLERGY_OTHER"][TEXT]            = $data_arr[$i]["ALLERGY_OTHER"];
            $data["BLOOD"][TEXT]                    = $data_arr[$i]["BLOOD"];
            $data["RH"][TEXT]                       = (in_array($data_arr[$i]["RH"], array("+","-"))) ? $data_arr[$i]["RH"] : "";
            $data["MEASLES_AGE"][TEXT]              = $data_arr[$i]["MEASLES_AGE"];
            $data["G_MEASLES_AGE"][TEXT]            = $data_arr[$i]["G_MEASLES_AGE"];
            $data["VARICELLA_AGE"][TEXT]            = $data_arr[$i]["VARICELLA_AGE"];
            $data["OTITIS_MEDIA_AGE"][TEXT]         = $data_arr[$i]["OTITIS_MEDIA_AGE"];
            $data["TB_AGE"][TEXT]                   = $data_arr[$i]["TB_AGE"];
            $data["KAWASAKI_AGE"][TEXT]             = $data_arr[$i]["KAWASAKI_AGE"];
            $data["INFECTION_AGE"][TEXT]            = $data_arr[$i]["INFECTION_AGE"];
            $data["MUMPS_AGE"][TEXT]                = $data_arr[$i]["MUMPS_AGE"];
            $data["HEART_DISEASE"][TEXT]            = $data_arr[$i]["HEART_DISEASE"];
            $data["HEART_S_AGE"][TEXT]              = $data_arr[$i]["HEART_S_AGE"];
            $data["HEART_SITUATION"][TEXT]          = $data_arr[$i]["HEART_SITUATION"];
            $data["HEART_E_AGE"][TEXT]              = $data_arr[$i]["HEART_E_AGE"];
            $data["KIDNEY_DISEASE"][TEXT]           = $data_arr[$i]["KIDNEY_DISEASE"];
            $data["KIDNEY_S_AGE"][TEXT]             = $data_arr[$i]["KIDNEY_S_AGE"];
            $data["KIDNEY_SITUATION"][TEXT]         = $data_arr[$i]["KIDNEY_SITUATION"];
            $data["KIDNEY_E_AGE"][TEXT]             = $data_arr[$i]["KIDNEY_E_AGE"];
            $data["ASTHMA_S_AGE"][TEXT]             = $data_arr[$i]["ASTHMA_S_AGE"];
            $data["ASTHMA_SITUATION"][TEXT]         = $data_arr[$i]["ASTHMA_SITUATION"];
            $data["ASTHMA_E_AGE"][TEXT]             = $data_arr[$i]["ASTHMA_E_AGE"];
            $data["CONVULSIONS_S_AGE"][TEXT]        = $data_arr[$i]["CONVULSIONS_S_AGE"];
            $data["CONVULSIONS_SITUATION"][TEXT]    = $data_arr[$i]["CONVULSIONS_SITUATION"];
            $data["CONVULSIONS_E_AGE"][TEXT]        = $data_arr[$i]["CONVULSIONS_E_AGE"];
            $data["OTHER_DISEASE"][TEXT]            = $data_arr[$i]["OTHER_DISEASE"];
            $data["TUBERCULIN"][TEXT]               = $data_arr[$i]["TUBERCULIN"];
            $data["TUBERCULIN_YEAR"][TEXT]          = $data_arr[$i]["TUBERCULIN_YEAR"];
            $data["TUBERCULIN_MONTH"][TEXT]         = $data_arr[$i]["TUBERCULIN_MONTH"];
            $data["TUBERCULIN_JUDGE"][TEXT]         = $data_arr[$i]["TUBERCULIN_JUDGE"];
            $data["BCG"][TEXT]                      = $data_arr[$i]["BCG"];
            $data["BCG_YEAR"][TEXT]                 = $data_arr[$i]["BCG_YEAR"];
            $data["BCG_MONTH"][TEXT]                = $data_arr[$i]["BCG_MONTH"];
            $data["POLIO"][TEXT]                    = $data_arr[$i]["POLIO"];
            $data["POLIO_YEAR"][TEXT]               = $data_arr[$i]["POLIO_YEAR"];
            $data["POLIO_MONTH"][TEXT]              = $data_arr[$i]["POLIO_MONTH"];
            $data["G_MEASLES"][TEXT]                = $data_arr[$i]["G_MEASLES"];
            $data["G_MEASLES_YEAR"][TEXT]           = $data_arr[$i]["G_MEASLES_YEAR"];
            $data["G_MEASLES_MONTH"][TEXT]          = $data_arr[$i]["G_MEASLES_MONTH"];
            $data["VARICELLA"][TEXT]                = $data_arr[$i]["VARICELLA"];
            $data["VARICELLA_YEAR"][TEXT]           = $data_arr[$i]["VARICELLA_YEAR"];
            $data["VARICELLA_MONTH"][TEXT]          = $data_arr[$i]["VARICELLA_MONTH"];
            $data["MUMPS"][TEXT]                    = $data_arr[$i]["MUMPS"];
            $data["MUMPS_YEAR"][TEXT]               = $data_arr[$i]["MUMPS_YEAR"];
            $data["MUMPS_MONTH"][TEXT]              = $data_arr[$i]["MUMPS_MONTH"];
            $data["ENCEPHALITIS"][TEXT]             = $data_arr[$i]["ENCEPHALITIS"];
            $data["ENCEPHALITIS_YEAR1"][TEXT]       = $data_arr[$i]["ENCEPHALITIS_YEAR1"];
            $data["ENCEPHALITIS_MONTH1"][TEXT]      = $data_arr[$i]["ENCEPHALITIS_MONTH1"];
            $data["ENCEPHALITIS_YEAR2"][TEXT]       = $data_arr[$i]["ENCEPHALITIS_YEAR2"];
            $data["ENCEPHALITIS_MONTH2"][TEXT]      = $data_arr[$i]["ENCEPHALITIS_MONTH2"];
            $data["ENCEPHALITIS_YEAR3"][TEXT]       = $data_arr[$i]["ENCEPHALITIS_YEAR3"];
            $data["ENCEPHALITIS_MONTH3"][TEXT]      = $data_arr[$i]["ENCEPHALITIS_MONTH3"];
            $data["ENCEPHALITIS_YEAR4"][TEXT]       = $data_arr[$i]["ENCEPHALITIS_YEAR4"];
            $data["ENCEPHALITIS_MONTH4"][TEXT]      = $data_arr[$i]["ENCEPHALITIS_MONTH4"];
            $data["ENCEPHALITIS_YEAR5"][TEXT]       = $data_arr[$i]["ENCEPHALITIS_YEAR5"];
            $data["ENCEPHALITIS_MONTH5"][TEXT]      = $data_arr[$i]["ENCEPHALITIS_MONTH5"];
            $data["MIXED"][TEXT]                    = $data_arr[$i]["MIXED"];
            $data["MIXED_YEAR1"][TEXT]              = $data_arr[$i]["MIXED_YEAR1"];
            $data["MIXED_MONTH1"][TEXT]             = $data_arr[$i]["MIXED_MONTH1"];
            $data["MIXED_YEAR2"][TEXT]              = $data_arr[$i]["MIXED_YEAR2"];
            $data["MIXED_MONTH2"][TEXT]             = $data_arr[$i]["MIXED_MONTH2"];
            $data["MIXED_YEAR3"][TEXT]              = $data_arr[$i]["MIXED_YEAR3"];
            $data["MIXED_MONTH3"][TEXT]             = $data_arr[$i]["MIXED_MONTH3"];
            $data["MIXED_YEAR4"][TEXT]              = $data_arr[$i]["MIXED_YEAR4"];
            $data["MIXED_MONTH4"][TEXT]             = $data_arr[$i]["MIXED_MONTH4"];
            $data["MIXED_YEAR5"][TEXT]              = $data_arr[$i]["MIXED_YEAR5"];
            $data["MIXED_MONTH5"][TEXT]             = $data_arr[$i]["MIXED_MONTH5"];
            $data["MEASLES"][TEXT]                  = (in_array($data_arr[$i]["MEASLES"], array("1","2"))) ? $data_arr[$i]["MEASLES"] : "";
            $data["MEASLES_TIMES"][TEXT]            = $data_arr[$i]["MEASLES_TIMES"];
            $data["MEASLES_YEAR1"][TEXT]            = $data_arr[$i]["MEASLES_YEAR1"];
            $data["MEASLES_MONTH1"][TEXT]           = $data_arr[$i]["MEASLES_MONTH1"];
            $data["MEASLES_YEAR2"][TEXT]            = $data_arr[$i]["MEASLES_YEAR2"];
            $data["MEASLES_MONTH2"][TEXT]           = $data_arr[$i]["MEASLES_MONTH2"];
            $data["MEASLES_YEAR3"][TEXT]            = $data_arr[$i]["MEASLES_YEAR3"];
            $data["MEASLES_MONTH3"][TEXT]           = $data_arr[$i]["MEASLES_MONTH3"];
            $data["VACCINE"][TEXT]                  = $data_arr[$i]["VACCINE"];
            $data["LOT_NO"][TEXT]                   = $data_arr[$i]["LOT_NO"];
            $data["CONFIRMATION"][TEXT]             = $data_arr[$i]["CONFIRMATION"];
            $data["A_MEASLES"][TEXT]                = (in_array($data_arr[$i]["A_MEASLES"], array("1","2"))) ? $data_arr[$i]["A_MEASLES"] : "";
            $data["A_MEASLES_AGE"][TEXT]            = $data_arr[$i]["A_MEASLES_AGE"];
            $data["A_CONFIRMATION"][TEXT]           = $data_arr[$i]["A_CONFIRMATION"];
            $data["ANTIBODY"][TEXT]                 = (in_array($data_arr[$i]["ANTIBODY"], array("1","2"))) ? $data_arr[$i]["ANTIBODY"] : "";
            $data["ANTIBODY_YEAR"][TEXT]            = $data_arr[$i]["ANTIBODY_YEAR"];
            $data["ANTIBODY_MONTH"][TEXT]           = $data_arr[$i]["ANTIBODY_MONTH"];
            $data["ANTIBODY_POSITIVE"][TEXT]        = (in_array($data_arr[$i]["ANTIBODY_POSITIVE"], array("1","2"))) ? $data_arr[$i]["ANTIBODY_POSITIVE"] : "";
            $data["REGISTERCD"][TEXT]               = STAFFCD;
            $data["UPDATED"][NUMBER]                = "SYSDATE()";

            if (0 == $db->getOne("SELECT COUNT(*) FROM HEALTH_INVEST_OTHER_DAT WHERE SCHREGNO  = '".$data_arr[$i]["SCHREGNO"]."'")) {
                $query = Query::insertSQL($data, "HEALTH_INVEST_OTHER_DAT");
            } else {
                $where = " WHERE SCHREGNO  = '".$data_arr[$i]["SCHREGNO"]."' ";
                $query = Query::updateSQL($data, "HEALTH_INVEST_OTHER_DAT", $where);
            }

            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //削除（その他病気）
    public function deleteQueryCsv4($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $query  = " DELETE FROM HEALTH_INVEST_OTHER_DAT ";
            $query .= " WHERE SCHREGNO  = '".$data_arr[$i]["SCHREGNO"]."' ";

            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //更新（健康調査）
    public function insertQueryCsv5_1($model, &$data_arr)
    {
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $questioncd = $db->getCol(knjf164Query::getHealthQuestion());

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            for ($j = 0; $j < get_count($questioncd); $j++) {
                //データセット
                $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
                $data["YEAR"][TEXT]             = $data_arr[$i]["INVEST_YEAR"];
                $data["E_YEAR"][TEXT]           = $data_arr[$i]["E_YEAR"];
                $data["E_MONTH"][TEXT]          = $data_arr[$i]["E_MONTH"];
                $data["QUESTIONCD"][TEXT]       = $questioncd[$j];
                $data["ANSWER"][TEXT]           = $data_arr[$i]["ANSWER".$questioncd[$j]];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";

                $chk_query  = " SELECT COUNT(*) FROM HEALTH_INVEST_DAT ";
                $chk_query .= " WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' AND YEAR = '".$data_arr[$i]["INVEST_YEAR"]."' AND ";
                $chk_query .= "       QUESTIONCD = '".$questioncd[$j]."' ";

                if ("0" == $db->getOne($chk_query)) {
                    $query = Query::insertSQL($data, "HEALTH_INVEST_DAT");
                } else {
                    $where  = " WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' AND YEAR = '".$data_arr[$i]["INVEST_YEAR"]."' AND ";
                    $where .= "       QUESTIONCD = '".$questioncd[$j]."' ";
                    $query = Query::updateSQL($data, "HEALTH_INVEST_DAT", $where);
                }

                $db->query($query);
            }
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //削除（健康調査）
    public function deleteQueryCsv5_1($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $query  = " DELETE FROM HEALTH_INVEST_DAT ";
            $query .= " WHERE SCHREGNO  = '".$data_arr[$i]["SCHREGNO"]."' AND YEAR = '".$data_arr[$i]["INVEST_YEAR"]."' ";

            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //更新（健康調査（生活上注意すべき点））
    public function insertQueryCsv5_2($model, &$data_arr)
    {
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //データセット
            $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
            $data["ATTENTION"][TEXT]        = $data_arr[$i]["ATTENTION"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            if (0 == $db->getOne("SELECT COUNT(*) FROM HEALTH_INVEST_ATTENTION_DAT WHERE SCHREGNO  = '".$data_arr[$i]["SCHREGNO"]."'")) {
                $query = Query::insertSQL($data, "HEALTH_INVEST_ATTENTION_DAT");
            } else {
                $where = " WHERE SCHREGNO  = '".$data_arr[$i]["SCHREGNO"]."' ";
                $query = Query::updateSQL($data, "HEALTH_INVEST_ATTENTION_DAT", $where);
            }

            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //削除（健康調査（生活上注意すべき点））
    public function deleteQueryCsv5_2($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $query  = " DELETE FROM HEALTH_INVEST_ATTENTION_DAT ";
            $query .= " WHERE SCHREGNO  = '".$data_arr[$i]["SCHREGNO"]."' ";

            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //更新（保健室記入）
    public function insertQueryCsv6($model, &$data_arr)
    {
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //データセット
            $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
            $data["INSURED_NAME"][TEXT]     = $data_arr[$i]["INSURED_NAME"];
            $data["INSURED_MARK"][TEXT]     = $data_arr[$i]["INSURED_MARK"];
            $data["INSURED_NO"][TEXT]       = $data_arr[$i]["INSURED_NO"];
            $data["INSURANCE_NAME"][TEXT]   = $data_arr[$i]["INSURANCE_NAME"];
            $data["INSURANCE_NO"][TEXT]     = $data_arr[$i]["INSURANCE_NO"];
            $data["VALID_DATE"][TEXT]       = str_replace("/", "-", $data_arr[$i]["VALID_DATE"]);
            $data["AUTHORIZE_DATE"][TEXT]   = str_replace("/", "-", $data_arr[$i]["AUTHORIZE_DATE"]);
            $data["RELATIONSHIP"][TEXT]     = $data_arr[$i]["RELATIONSHIP"];
            $data["REMARK"][TEXT]           = $data_arr[$i]["REMARK"];
            $data["ATTENTION"][TEXT]        = $data_arr[$i]["ATTENTION"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            if (0 == $db->getOne("SELECT COUNT(*) FROM HEALTH_NURSE_ENT_DAT WHERE SCHREGNO  = '".$data_arr[$i]["SCHREGNO"]."'")) {
                $query = Query::insertSQL($data, "HEALTH_NURSE_ENT_DAT");
            } else {
                $where = " WHERE SCHREGNO  = '".$data_arr[$i]["SCHREGNO"]."' ";
                $query = Query::updateSQL($data, "HEALTH_NURSE_ENT_DAT", $where);
            }

            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //削除（保健室記入）
    public function deleteQueryCsv6($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $query  = " DELETE FROM HEALTH_NURSE_ENT_DAT ";
            $query .= " WHERE SCHREGNO  = '".$data_arr[$i]["SCHREGNO"]."' ";

            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1 = array();
        $data1["PROGRAMID"][TEXT]       = PROGRAMID;
        $data1["MSGROW"][NUMBER]        = $record_no;
        $data1["MSGREMARK"][TEXT]       = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //CSVデータ出力（病気けが（入学前））
    public function selectMainQuery1($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T3.SEQ, ";
        $query .= "     T3.DISEASE, ";
        $query .= "     T3.S_YEAR, ";
        $query .= "     T3.S_MONTH, ";
        $query .= "     T3.E_YEAR, ";
        $query .= "     T3.E_MONTH, ";
        $query .= "     T3.SITUATION, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO, ";
        $query .= "     HEALTH_BEF_SICKREC_DAT T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' AND ";
        $query .= "     T3.SCHREGNO = T1.SCHREGNO ";
        if ($model->field["GRADE_HR_CLASS"] != "") {
            $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.GRADE IN (SELECT ";
                $query .= "                     G1.GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT G1 ";
                $query .= "                 WHERE ";
                $query .= "                     G1.YEAR         = T1.YEAR AND ";
                $query .= "                     G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                $query .= "                 ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.GRADE IN (SELECT ";
            $query .= "                     G1.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT G1 ";
            $query .= "                 WHERE ";
            $query .= "                     G1.YEAR         = T1.YEAR AND ";
            $query .= "                     G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     INT(T3.SEQ) ";

        return $query;
    }

    //CSVデータ出力（病気けが（入学後））
    public function selectMainQuery2($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T3.SEQ, ";
        $query .= "     T3.DISEASE, ";
        $query .= "     T3.S_YEAR, ";
        $query .= "     T3.S_MONTH, ";
        $query .= "     T3.E_YEAR, ";
        $query .= "     T3.E_MONTH, ";
        $query .= "     T3.SUSPECT, ";
        $query .= "     T3.HOSPITAL, ";
        $query .= "     T3.DOCTOR, ";
        $query .= "     T3.TELNO, ";
        $query .= "     T3.MEDICINE, ";
        $query .= "     T3.SITUATION, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";

        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO, ";
        $query .= "     HEALTH_AFT_SICKREC_DAT T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' AND ";
        $query .= "     T3.SCHREGNO = T1.SCHREGNO ";
        if ($model->field["GRADE_HR_CLASS"] != "") {
            $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.GRADE IN (SELECT ";
                $query .= "                     G1.GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT G1 ";
                $query .= "                 WHERE ";
                $query .= "                     G1.YEAR         = T1.YEAR AND ";
                $query .= "                     G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                $query .= "                 ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.GRADE IN (SELECT ";
            $query .= "                     G1.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT G1 ";
            $query .= "                 WHERE ";
            $query .= "                     G1.YEAR         = T1.YEAR AND ";
            $query .= "                     G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     INT(T3.SEQ) ";

        return $query;
    }

    //CSVデータ出力（家族情報）
    public function selectMainQuery3($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T3.RELANO, ";
        $query .= "     T3.RELANAME, ";
        $query .= "     T3.RELAKANA, ";
        $query .= "     T3.RELATIONSHIP, ";
        $query .= "     T3.RELABIRTHDAY, ";
        $query .= "     T4.REMARK, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO, ";
        $query .= "     SCHREG_RELA_DAT T3 ";
        $query .= "     LEFT JOIN HEALTH_RELA_DAT T4 ON T4.SCHREGNO = T3.SCHREGNO AND T4.RELANO = T3.RELANO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' AND ";
        $query .= "     T3.SCHREGNO = T1.SCHREGNO ";
        if ($model->field["GRADE_HR_CLASS"] != "") {
            $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.GRADE IN (SELECT ";
                $query .= "                     G1.GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT G1 ";
                $query .= "                 WHERE ";
                $query .= "                     G1.YEAR         = T1.YEAR AND ";
                $query .= "                     G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                $query .= "                 ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.GRADE IN (SELECT ";
            $query .= "                     G1.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT G1 ";
            $query .= "                 WHERE ";
            $query .= "                     G1.YEAR         = T1.YEAR AND ";
            $query .= "                     G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     INT(T3.RELANO) ";

        return $query;
    }

    //CSVデータ出力（その他病気）
    public function selectMainQuery4($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T3.ALLERGY_MEDICINE, ";
        $query .= "     T3.ALLERGY_FOOD, ";
        $query .= "     T3.ALLERGY_OTHER, ";
        $query .= "     T3.BLOOD, ";
        $query .= "     T3.RH, ";
        $query .= "     T3.MEASLES_AGE, ";
        $query .= "     T3.G_MEASLES_AGE, ";
        $query .= "     T3.VARICELLA_AGE, ";
        $query .= "     T3.OTITIS_MEDIA_AGE, ";
        $query .= "     T3.TB_AGE, ";
        $query .= "     T3.KAWASAKI_AGE, ";
        $query .= "     T3.INFECTION_AGE, ";
        $query .= "     T3.MUMPS_AGE, ";
        $query .= "     T3.HEART_DISEASE, ";
        $query .= "     T3.HEART_S_AGE, ";
        $query .= "     T3.HEART_SITUATION, ";
        $query .= "     T3.HEART_E_AGE, ";
        $query .= "     T3.KIDNEY_DISEASE, ";
        $query .= "     T3.KIDNEY_S_AGE, ";
        $query .= "     T3.KIDNEY_SITUATION, ";
        $query .= "     T3.KIDNEY_E_AGE, ";
        $query .= "     T3.ASTHMA_S_AGE, ";
        $query .= "     T3.ASTHMA_SITUATION, ";
        $query .= "     T3.ASTHMA_E_AGE, ";
        $query .= "     T3.CONVULSIONS_S_AGE, ";
        $query .= "     T3.CONVULSIONS_SITUATION, ";
        $query .= "     T3.CONVULSIONS_E_AGE, ";
        $query .= "     T3.OTHER_DISEASE, ";
        $query .= "     T3.TUBERCULIN, ";
        $query .= "     T3.TUBERCULIN_YEAR, ";
        $query .= "     T3.TUBERCULIN_MONTH, ";
        $query .= "     T3.TUBERCULIN_JUDGE, ";
        $query .= "     T3.BCG, ";
        $query .= "     T3.BCG_YEAR, ";
        $query .= "     T3.BCG_MONTH, ";
        $query .= "     T3.POLIO, ";
        $query .= "     T3.POLIO_YEAR, ";
        $query .= "     T3.POLIO_MONTH, ";
        $query .= "     T3.G_MEASLES, ";
        $query .= "     T3.G_MEASLES_YEAR, ";
        $query .= "     T3.G_MEASLES_MONTH, ";
        $query .= "     T3.VARICELLA, ";
        $query .= "     T3.VARICELLA_YEAR, ";
        $query .= "     T3.VARICELLA_MONTH, ";
        $query .= "     T3.MUMPS, ";
        $query .= "     T3.MUMPS_YEAR, ";
        $query .= "     T3.MUMPS_MONTH, ";
        $query .= "     T3.ENCEPHALITIS, ";
        $query .= "     T3.ENCEPHALITIS_YEAR1, ";
        $query .= "     T3.ENCEPHALITIS_MONTH1, ";
        $query .= "     T3.ENCEPHALITIS_YEAR2, ";
        $query .= "     T3.ENCEPHALITIS_MONTH2, ";
        $query .= "     T3.ENCEPHALITIS_YEAR3, ";
        $query .= "     T3.ENCEPHALITIS_MONTH3, ";
        $query .= "     T3.ENCEPHALITIS_YEAR4, ";
        $query .= "     T3.ENCEPHALITIS_MONTH4, ";
        $query .= "     T3.ENCEPHALITIS_YEAR5, ";
        $query .= "     T3.ENCEPHALITIS_MONTH5, ";
        $query .= "     T3.MIXED, ";
        $query .= "     T3.MIXED_YEAR1, ";
        $query .= "     T3.MIXED_MONTH1, ";
        $query .= "     T3.MIXED_YEAR2, ";
        $query .= "     T3.MIXED_MONTH2, ";
        $query .= "     T3.MIXED_YEAR3, ";
        $query .= "     T3.MIXED_MONTH3, ";
        $query .= "     T3.MIXED_YEAR4, ";
        $query .= "     T3.MIXED_MONTH4, ";
        $query .= "     T3.MIXED_YEAR5, ";
        $query .= "     T3.MIXED_MONTH5, ";
        $query .= "     T3.MEASLES, ";
        $query .= "     T3.MEASLES_TIMES, ";
        $query .= "     T3.MEASLES_YEAR1, ";
        $query .= "     T3.MEASLES_MONTH1, ";
        $query .= "     T3.MEASLES_YEAR2, ";
        $query .= "     T3.MEASLES_MONTH2, ";
        $query .= "     T3.MEASLES_YEAR3, ";
        $query .= "     T3.MEASLES_MONTH3, ";
        $query .= "     T3.VACCINE, ";
        $query .= "     T3.LOT_NO, ";
        $query .= "     T3.CONFIRMATION, ";
        $query .= "     T3.A_MEASLES, ";
        $query .= "     T3.A_MEASLES_AGE, ";
        $query .= "     T3.A_CONFIRMATION, ";
        $query .= "     T3.ANTIBODY, ";
        $query .= "     T3.ANTIBODY_YEAR, ";
        $query .= "     T3.ANTIBODY_MONTH, ";
        $query .= "     T3.ANTIBODY_POSITIVE, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO, ";
        $query .= "     HEALTH_INVEST_OTHER_DAT T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' AND ";
        $query .= "     T3.SCHREGNO = T1.SCHREGNO ";
        if ($model->field["GRADE_HR_CLASS"] != "") {
            $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.GRADE IN (SELECT ";
                $query .= "                     G1.GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT G1 ";
                $query .= "                 WHERE ";
                $query .= "                     G1.YEAR         = T1.YEAR AND ";
                $query .= "                     G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                $query .= "                 ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.GRADE IN (SELECT ";
            $query .= "                     G1.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT G1 ";
            $query .= "                 WHERE ";
            $query .= "                     G1.YEAR         = T1.YEAR AND ";
            $query .= "                     G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //健康調査質問取得
    public function getHealthQuestion()
    {
        $query  = " WITH QUESTION AS ( ";
        $query .= "     SELECT ";
        $query .= "         QUESTIONCD, ";
        $query .= "         CONTENTS, ";
        $query .= "         CASE WHEN RTRIM(LTRIM(SORT))='' THEN '99' WHEN SORT IS NULL THEN '99' ELSE SORT END SORT ";
        $query .= "     FROM ";
        $query .= "         HEALTH_QUESTION_MST ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     QUESTIONCD, ";
        $query .= "     CONTENTS ";
        $query .= " FROM ";
        $query .= "     QUESTION ";
        $query .= " ORDER BY ";
        $query .= "     INT(SORT), ";
        $query .= "     INT(QUESTIONCD) ";

        return $query;
    }

    //CSVデータ出力（健康調査）
    public function selectMainQuery5_1($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T3.YEAR AS INVEST_YEAR, ";
        $query .= "     T3.E_YEAR, ";
        $query .= "     T3.E_MONTH, ";
        $query .= "     T3.QUESTIONCD, ";
        $query .= "     CASE WHEN RTRIM(LTRIM(T4.SORT))='' THEN '99' WHEN T4.SORT IS NULL THEN '99' ELSE T4.SORT END SORT, ";
        $query .= "     T3.ANSWER ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO, ";
        $query .= "     HEALTH_INVEST_DAT T3 ";
        $query .= "     INNER JOIN HEALTH_QUESTION_MST T4 ON T4.QUESTIONCD = T3.QUESTIONCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' AND ";
        $query .= "     T3.SCHREGNO = T1.SCHREGNO ";
        if ($model->field["GRADE_HR_CLASS"] != "") {
            $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.GRADE IN (SELECT ";
                $query .= "                     G1.GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT G1 ";
                $query .= "                 WHERE ";
                $query .= "                     G1.YEAR         = T1.YEAR AND ";
                $query .= "                     G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                $query .= "                 ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.GRADE IN (SELECT ";
            $query .= "                     G1.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT G1 ";
            $query .= "                 WHERE ";
            $query .= "                     G1.YEAR         = T1.YEAR AND ";
            $query .= "                     G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T3.YEAR, ";
        $query .= "     INT(SORT), ";
        $query .= "     INT(T3.QUESTIONCD) ";

        return $query;
    }

    //CSVデータ出力（健康調査（生活上注意すべき点））
    public function selectMainQuery5_2($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T3.ATTENTION, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO, ";
        $query .= "     HEALTH_INVEST_ATTENTION_DAT T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' AND ";
        $query .= "     T3.SCHREGNO = T1.SCHREGNO ";
        if ($model->field["GRADE_HR_CLASS"] != "") {
            $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.GRADE IN (SELECT ";
                $query .= "                     G1.GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT G1 ";
                $query .= "                 WHERE ";
                $query .= "                     G1.YEAR         = T1.YEAR AND ";
                $query .= "                     G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                $query .= "                 ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.GRADE IN (SELECT ";
            $query .= "                     G1.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT G1 ";
            $query .= "                 WHERE ";
            $query .= "                     G1.YEAR         = T1.YEAR AND ";
            $query .= "                     G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //CSVデータ出力（保健室記入）
    public function selectMainQuery6($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T3.INSURED_NAME, ";
        $query .= "     T3.INSURED_MARK, ";
        $query .= "     T3.INSURED_NO, ";
        $query .= "     T3.INSURANCE_NAME, ";
        $query .= "     T3.INSURANCE_NO, ";
        $query .= "     T3.VALID_DATE, ";
        $query .= "     T3.AUTHORIZE_DATE, ";
        $query .= "     T3.RELATIONSHIP, ";
        $query .= "     T3.REMARK, ";
        $query .= "     T3.ATTENTION, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO, ";
        $query .= "     HEALTH_NURSE_ENT_DAT T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' AND ";
        $query .= "     T3.SCHREGNO = T1.SCHREGNO ";
        if ($model->field["GRADE_HR_CLASS"] != "") {
            $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.GRADE IN (SELECT ";
                $query .= "                     G1.GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT G1 ";
                $query .= "                 WHERE ";
                $query .= "                     G1.YEAR         = T1.YEAR AND ";
                $query .= "                     G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                $query .= "                 ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.GRADE IN (SELECT ";
            $query .= "                     G1.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT G1 ";
            $query .= "                 WHERE ";
            $query .= "                     G1.YEAR         = T1.YEAR AND ";
            $query .= "                     G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }
}
