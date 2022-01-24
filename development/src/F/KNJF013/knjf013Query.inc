<?php

require_once('for_php7.php');

class knjf013Query extends Query
{

    //未検査項目取得
    public function getMedexamDetNotExaminedDat($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DET_NOT_EXAMINED_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR  = '{$model->year}' ";
        $query .= "     AND GRADE = '".substr($model->hr_class, 0, 2)."' ";

        return $query;
    }

    //学校を判断
    public function getNameMstZ010()
    {
        $query = " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ";
        return $query;
    }

    //栄養状態コンボボックス
    public function getNutrition($model, $flg)
    {
        $query = "SELECT NAMECD2, NAME1, ABBV1, ABBV2, NAMESPARE2 FROM V_NAME_MST WHERE YEAR = '{$model->year}' AND NAMECD1 = 'F030'";
        if ($flg === '1') {
            $query .= " AND NAMESPARE3 = '1' ORDER BY NAMECD2 ";
        }
        return $query;
    }

    //年組
    public function getHrClass($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     G1.GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT G1 ";
                $query .= "                 WHERE ";
                $query .= "                     G1.YEAR         = T1.YEAR AND ";
                $query .= "                     G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."')) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GRADE IN (  SELECT ";
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
    //種類
    public function getInputForm()
    {
        $query  = " WITH T_COMMON (VALUE,LABEL) AS ( ";
        $query .= "     VALUES('1','入力項目1') ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('2','入力項目2') ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('3','入力項目3') ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('4','入力項目4') ";
        $query .= "     ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     T_COMMON ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }


    //一覧
    public function getMedexamList($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO AS REGD_SCHREGNO, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME_SHOW, ";
        $query .= "     T2.BIRTHDAY, ";
        $query .= "     T2.SEX, ";
        $query .= "     T3.SCHOOL_KIND, ";
        $query .= "     L1.DATE, ";
        $query .= "     L2.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = T1.YEAR AND T3.GRADE = T1.GRADE ";
        $query .= "     LEFT JOIN MEDEXAM_HDAT L1 ON L1.YEAR = T1.YEAR AND L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN V_MEDEXAM_DET_DAT L2 ON L2.YEAR = T1.YEAR AND L2.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->semester}' ";
        $query .= "     AND T1.GRADE || T1.HR_CLASS = '{$model->hr_class}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }
    //名称マスタ
    public function getNameMst($model, $namecd1, $flg = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        if ($flg == "3") {
            $query .= "     ABBV1 || '　' || NAME1 AS LABEL ";
        } elseif ($flg == "CD_NASHI") {
            $query .= "     NAME1 AS LABEL ";
        } else {
            $query .= "     NAMECD2 || '　' || NAME1 AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '{$model->year}' AND ";
        $query .= "     NAMECD1 = '{$namecd1}' ";
        if ($flg == "1") {
            $query .= "     AND NAMESPARE3 = '1' ";
        }
        if ($flg == "2") {
            $query .= "     AND NAMECD2 = '98' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    public function getVisionMark($model)
    {
        $query .= " SELECT ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMECD2, ";
        $query .= "     ABBV3, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' AND ";
        $query .= "     NAMECD1 = 'F017' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 DESC ";

        return $query;
    }

    //標準偏差値取得
    public function getSD($model)
    {
        $query  = " WITH T_SCHREG AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR     = '".$model->year."' AND ";
        $query .= "         SEMESTER = '".$model->semester."' AND ";
        $query .= "         GRADE || HR_CLASS = '".$model->hr_class."' ";
        $query .= " ), T_HEIGHT AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.HEIGHT ";
        $query .= "     FROM ";
        $query .= "         MEDEXAM_DET_DAT T1 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".$model->year."' AND ";
        $query .= "         T1.HEIGHT IS NOT NULL AND ";
        $query .= "         EXISTS(SELECT 'X' FROM T_SCHREG S1 WHERE S1.SCHREGNO = T1.SCHREGNO) ";
        $query .= " ), H_AVERAGE AS ( ";
        $query .= "     SELECT ";
        $query .= "         DECIMAL(ROUND(STDDEV(FLOAT(HEIGHT))*10,0)/10,5,1) AS H_STDDEV ";
        $query .= "     FROM ";
        $query .= "         T_HEIGHT ";
        $query .= " ), T_WEIGHT AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.WEIGHT ";
        $query .= "     FROM ";
        $query .= "         MEDEXAM_DET_DAT T1 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".$model->year."' AND ";
        $query .= "         T1.WEIGHT IS NOT NULL AND ";
        $query .= "         EXISTS(SELECT 'X' FROM T_SCHREG S1 WHERE S1.SCHREGNO = T1.SCHREGNO) ";
        $query .= " ), W_AVERAGE AS ( ";
        $query .= "     SELECT ";
        $query .= "         DECIMAL(ROUND(STDDEV(FLOAT(WEIGHT))*10,0)/10,5,1) AS W_STDDEV ";
        $query .= "     FROM ";
        $query .= "         T_WEIGHT ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.H_STDDEV, ";
        $query .= "     T2.W_STDDEV ";
        $query .= " FROM ";
        $query .= "     H_AVERAGE T1, ";
        $query .= "     W_AVERAGE T2 ";

        return $query;
    }

    //測定評価平均値データ取得
    public function getHexamPhysicalAvgDat($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     HEXAM_PHYSICAL_AVG_DAT T1, ";
        $query .= "     (SELECT ";
        $query .= "         SEX, ";
        $query .= "         NENREI_YEAR, ";
        $query .= "         MAX(YEAR) AS YEAR ";
        $query .= "     FROM ";
        $query .= "         HEXAM_PHYSICAL_AVG_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR <= '".$model->year."' AND ";
        $query .= "         NENREI_MONTH = 0 ";
        $query .= "     GROUP BY ";
        $query .= "         SEX, ";
        $query .= "         NENREI_YEAR ";
        $query .= "     ) T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = T2.YEAR AND ";
        $query .= "     T1.SEX          = T2.SEX AND ";
        $query .= "     T1.NENREI_YEAR  = T2.NENREI_YEAR AND ";
        $query .= "     T1.NENREI_MONTH = 0 ";

        return $query;
    }

    //健康診断ヘッダデータ存在チェック
    public function checkMedexamHdat($model, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SCHREGNO = '{$schregno}' ";

        return $query;
    }
    //健康診断一般データ存在チェック
    public function checkMedexamDetDat($model, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DET_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SCHREGNO = '{$schregno}' ";

        return $query;
    }
    //健康診断一般詳細データ存在チェック
    public function checkMedexamDetDetailDat($model, $schregno, $seq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DET_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SCHREGNO = '{$schregno}' ";
        $query .= "     AND DET_SEQ = '{$seq}' ";

        return $query;
    }
    //健康診断ヘッダデータ
    public function getUpdateMedexamHdat($db, $model, $g)
    {
        $data = array();
        $schregno = $model->fields["REGD_SCHREGNO"][$g];

        $data["YEAR"][TEXT]         = $model->year;
        $data["SCHREGNO"][TEXT]     = $schregno;
        $data["DATE"][TEXT]         = str_replace("/", "-", $model->fields["DATE"][$g]);
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = knjf013Query::checkMedexamHdat($model, $schregno);
        if ($db->getOne($query) > 0) {
            $where  = " WHERE ";
            $where .= "     YEAR = '{$model->year}' ";
            $where .= "     AND SCHREGNO = '{$schregno}' ";
            $query = Query::updateSQL($data, "MEDEXAM_HDAT", $where);
            $db->query($query);
        } elseif ($model->inputFlg[$g] == "OK") {
            //データを入力していない（all　null）生徒は、レコードを作成しない。（ゴミデータを作成しないため）
            $query = Query::insertSQL($data, "MEDEXAM_HDAT");
            $db->query($query);
        }
    }
    //健康診断一般データ
    public function getUpdateMedexamDetDat($db, $model, $g)
    {
        $data = array();
        $schregno = $model->fields["REGD_SCHREGNO"][$g];

        $data["YEAR"][TEXT]         = $model->year;
        $data["SCHREGNO"][TEXT]     = $schregno;

        if ($model->input_form == "1") {
            $data["HEIGHT"][NUMBER]              = $model->fields["HEIGHT"][$g];
            $data["WEIGHT"][NUMBER]              = $model->fields["WEIGHT"][$g];
            $data["SITHEIGHT"][NUMBER]           = $model->fields["SITHEIGHT"][$g];
            $data["R_BAREVISION_MARK"][TEXT]     = $model->fields["R_BAREVISION_MARK"][$g];
            $data["L_BAREVISION_MARK"][TEXT]     = $model->fields["L_BAREVISION_MARK"][$g];
            $data["R_VISION_MARK"][TEXT]         = $model->fields["R_VISION_MARK"][$g];
            $data["L_VISION_MARK"][TEXT]         = $model->fields["L_VISION_MARK"][$g];
            if ($model->z010name1 == "fukuiken" || $model->z010name1 == "koma") {
                $data["R_BAREVISION"][TEXT]          = $model->fields["R_BAREVISION"][$g];
                $data["R_VISION"][TEXT]              = $model->fields["R_VISION"][$g];
                $data["L_BAREVISION"][TEXT]          = $model->fields["L_BAREVISION"][$g];
                $data["L_VISION"][TEXT]              = $model->fields["L_VISION"][$g];
            }
            $data["R_EAR_DB"][NUMBER]            = $model->fields["R_EAR_DB"][$g];
            $data["R_EAR"][TEXT]                 = $model->fields["R_EAR"][$g];
            $data["L_EAR_DB"][NUMBER]            = $model->fields["L_EAR_DB"][$g];
            $data["L_EAR"][TEXT]                 = $model->fields["L_EAR"][$g];
        }

        if ($model->input_form == "2") {
            $data["NUTRITIONCD"][TEXT]           = $model->fields["NUTRITIONCD"][$g];
            $data["SPINERIBCD"][TEXT]            = $model->fields["SPINERIBCD"][$g];
            $data["SPINERIBCD_REMARK"][TEXT]     = $model->fields["SPINERIBCD_REMARK"][$g];
            $data["EYEDISEASECD"][TEXT]          = $model->fields["EYEDISEASECD"][$g];
            $data["EYE_TEST_RESULT"][TEXT]       = $model->fields["EYE_TEST_RESULT"][$g];
            $data["NOSEDISEASECD"][TEXT]         = $model->fields["NOSEDISEASECD"][$g];
            $data["NOSEDISEASECD_REMARK"][TEXT]  = $model->fields["NOSEDISEASECD_REMARK"][$g];
            $data["SKINDISEASECD"][TEXT]         = $model->fields["SKINDISEASECD"][$g];
            $data["TB_FILMDATE"][TEXT]           = str_replace("/", "-", $model->fields["TB_FILMDATE"][$g]);
            $data["TB_FILMNO"][TEXT]             = $model->fields["TB_FILMNO"][$g];
            $data["TB_REMARKCD"][TEXT]           = $model->fields["TB_REMARKCD"][$g];
            $data["TB_X_RAY"][TEXT]              = $model->fields["TB_X_RAY"][$g];
            $data["TB_OTHERTESTCD"][TEXT]        = $model->fields["TB_OTHERTESTCD"][$g];
            $data["TB_NAMECD"][TEXT]             = $model->fields["TB_NAMECD"][$g];
            $data["TB_ADVISECD"][TEXT]           = $model->fields["TB_ADVISECD"][$g];
            if ($model->Properties["printKenkouSindanIppan"] == "1"
                && ($model->z010name1 == "KINDAI"
                    || $model->z010name1 == "KINJUNIOR")
                && $model->fields["DATE"][$g] != "") {
                $getHeart_medexam_Shokichi = $db->getRow(knjf013Query::getNameMst($model, "F080", "1"), DB_FETCHMODE_ASSOC);
                $data["HEART_MEDEXAM"][TEXT]         = $model->fields["HEART_MEDEXAM"][$g] ? $model->fields["HEART_MEDEXAM"][$g] : $getHeart_medexam_Shokichi["VALUE"];
            } else {
                $data["HEART_MEDEXAM"][TEXT]         = $model->fields["HEART_MEDEXAM"][$g];
            }
            $data["HEART_MEDEXAM_REMARK"][TEXT]  = $model->fields["HEART_MEDEXAM_REMARK"][$g];
            $data["HEARTDISEASECD"][TEXT]        = $model->fields["HEARTDISEASECD"][$g];
            $data["HEARTDISEASECD_REMARK"][TEXT] = $model->fields["HEARTDISEASECD_REMARK"][$g];
            $data["MANAGEMENT_DIV"][TEXT]        = $model->fields["MANAGEMENT_DIV"][$g];
            $data["MANAGEMENT_REMARK"][TEXT]     = $model->fields["MANAGEMENT_REMARK"][$g];
        }

        if ($model->input_form == "3") {
            $data["ALBUMINURIA1CD"][TEXT]        = $model->fields["ALBUMINURIA1CD"][$g];
            $data["URICSUGAR1CD"][TEXT]          = $model->fields["URICSUGAR1CD"][$g];
            $data["URICBLEED1CD"][TEXT]          = $model->fields["URICBLEED1CD"][$g];
            $data["ALBUMINURIA2CD"][TEXT]        = $model->fields["ALBUMINURIA2CD"][$g];
            $data["URICSUGAR2CD"][TEXT]          = $model->fields["URICSUGAR2CD"][$g];
            $data["URICBLEED2CD"][TEXT]          = $model->fields["URICBLEED2CD"][$g];
            $data["URICOTHERTEST"][TEXT]         = $model->fields["URICOTHERTEST"][$g];
            if ($model->Properties["printKenkouSindanIppan"] == "1"
                && ($model->z010name1 == "KINDAI"
                    || $model->z010name1 == "KINJUNIOR")
                && $model->fields["DATE"][$g] != "") {
                $getHeart_uriAdviseCd_Shokichi = $db->getRow(knjf013Query::getNameMst($model, "F021", "1"), DB_FETCHMODE_ASSOC);
                $data["URI_ADVISECD"][TEXT]          = $model->fields["URI_ADVISECD"][$g] ? $model->fields["URI_ADVISECD"][$g] : $getHeart_uriAdviseCd_Shokichi["VALUE"];
            } else {
                $data["URI_ADVISECD"][TEXT]          = $model->fields["URI_ADVISECD"][$g];
            }
            //寄生虫卵表示
            if (($model->fields["SCHOOL_KIND"][$g] == "P" && $model->Properties["useParasite_P"] == "1") ||
                ($model->fields["SCHOOL_KIND"][$g] == "J" && $model->Properties["useParasite_J"] == "1") ||
                ($model->fields["SCHOOL_KIND"][$g] == "H" && $model->Properties["useParasite_H"] == "1")) {
                $data["PARASITE"][TEXT]          = $model->fields["PARASITE"][$g];
            }
            $data["OTHERDISEASECD"][TEXT]        = $model->fields["OTHERDISEASECD"][$g];
            $data["OTHER_ADVISECD"][TEXT]        = $model->fields["OTHER_ADVISECD"][$g];
            $data["OTHER_REMARK"][TEXT]          = $model->fields["OTHER_REMARK"][$g];
            $data["DOC_CD"][TEXT]                = $model->fields["DOC_CD"][$g];
            $data["DOC_REMARK"][TEXT]            = $model->fields["DOC_REMARK"][$g];
            if ($model->z010name1 == "fukuiken" || $model->z010name1 == "koma") {
                $data["ANEMIA_REMARK"][TEXT]         = $model->fields["ANEMIA_REMARK"][$g];
                $data["HEMOGLOBIN"][NUMBER]          = $model->fields["HEMOGLOBIN"][$g];
            }
            //「学校医・日付」が設定されていないとき、「健康診断実施日付」をセットする
            //近大は「学校医・日付」が設定されていないとき、nullのまま更新する
            $query = knjf013Query::getNameMstZ010();
            $SchoolName = $db->getOne($query);
            if ($SchoolName !== 'KINDAI' && $SchoolName !== 'KINJUNIOR') {
                $doc_date = strlen($model->fields["DOC_DATE"][$g]) ? $model->fields["DOC_DATE"][$g] : $model->fields["DATE"][$g];
            } else {
                $doc_date = $model->fields["DOC_DATE"][$g];
            }
            $data["DOC_DATE"][TEXT]              = str_replace("/", "-", $doc_date);
            $data["TREATCD"][TEXT]               = $model->fields["TREATCD"][$g];
            $data["REMARK"][TEXT]                = $model->fields["REMARK"][$g];
        }

        if ($model->input_form == "4") {
            $data["MEDICAL_HISTORY1"][TEXT]      = $model->fields["MEDICAL_HISTORY1"][$g];
            $data["MEDICAL_HISTORY2"][TEXT]      = $model->fields["MEDICAL_HISTORY2"][$g];
            $data["MEDICAL_HISTORY3"][TEXT]      = $model->fields["MEDICAL_HISTORY3"][$g];
            $data["DIAGNOSIS_NAME"][TEXT]        = $model->fields["DIAGNOSIS_NAME"][$g];
            $data["GUIDE_DIV"][TEXT]             = $model->fields["GUIDE_DIV"][$g];
            $data["JOINING_SPORTS_CLUB"][TEXT]   = $model->fields["JOINING_SPORTS_CLUB"][$g];
        }

        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = knjf013Query::checkMedexamDetDat($model, $schregno);
        if ($db->getOne($query) > 0) {
            $where  = " WHERE ";
            $where .= "     YEAR = '{$model->year}' ";
            $where .= "     AND SCHREGNO = '{$schregno}' ";
            $query = Query::updateSQL($data, "MEDEXAM_DET_DAT", $where);
            $db->query($query);
        } elseif ($model->inputFlg[$g] == "OK") {
            //データを入力していない（all　null）生徒は、レコードを作成しない。（ゴミデータを作成しないため）
            $query = Query::insertSQL($data, "MEDEXAM_DET_DAT");
            $db->query($query);
        }

        //MEDEXAM_DET_DETAIL_DAT----------------------------

        if ($model->input_form == "1") {
            $seq = "009";
            $data2 = array();
            $data2["YEAR"][TEXT]         = $model->year;
            $data2["SCHREGNO"][TEXT]     = $schregno;
            $data2["DET_SEQ"][TEXT]      = $seq;
            $data2["DET_REMARK2"][TEXT]  = $model->fields["R_EAR_DB_4000"][$g];
            $data2["DET_REMARK3"][TEXT]  = $model->fields["L_EAR_DB_4000"][$g];
            if ($model->z010name1 == "koma" || ($model->Properties["printKenkouSindanIppan"] == "1" && $model->Properties["KenkouSindan_Ippan_Pattern"] == "1")) {
                $data2["DET_REMARK5"][TEXT]  = $model->fields["R_EAR_DB_1000"][$g];
                $data2["DET_REMARK6"][TEXT]  = $model->fields["L_EAR_DB_1000"][$g];
            }
            $data2["REGISTERCD"][TEXT]   = STAFFCD;
            $data2["UPDATED"][FUNC]      = "sysdate()";

            $query = knjf013Query::checkMedexamDetDetailDat($model, $schregno, $seq);
            if ($db->getOne($query) > 0) {
                $where  = " WHERE ";
                $where .= "     YEAR = '{$model->year}' ";
                $where .= "     AND SCHREGNO = '{$schregno}' ";
                $where .= "     AND DET_SEQ = '{$seq}' ";
                $query = Query::updateSQL($data2, "MEDEXAM_DET_DETAIL_DAT", $where);
                $db->query($query);
            } elseif ($model->inputFlg[$g] == "OK") {
                //データを入力していない（all　null）生徒は、レコードを作成しない。（ゴミデータを作成しないため）
                $query = Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT");
                $db->query($query);
            }

            $seq = "012";
            $data2 = array();
            $data2["YEAR"][TEXT]         = $model->year;
            $data2["SCHREGNO"][TEXT]     = $schregno;
            $data2["DET_SEQ"][TEXT]      = $seq;
            $data2["DET_REMARK1"][TEXT]  = $model->fields["R_VISION_CANTMEASURE"][$g];
            $data2["DET_REMARK2"][TEXT]  = $model->fields["L_VISION_CANTMEASURE"][$g];
            $data2["DET_REMARK3"][TEXT]  = $model->fields["R_EAR_CANTMEASURE"][$g];
            $data2["DET_REMARK4"][TEXT]  = $model->fields["L_EAR_CANTMEASURE"][$g];
            $data2["DET_REMARK5"][TEXT]  = $model->fields["NO_PRINT_OBESITY_INDEX"][$g];
            $data2["REGISTERCD"][TEXT]   = STAFFCD;
            $data2["UPDATED"][FUNC]      = "sysdate()";

            $query = knjf013Query::checkMedexamDetDetailDat($model, $schregno, $seq);
            if ($db->getOne($query) > 0) {
                $where  = " WHERE ";
                $where .= "         YEAR     = '{$model->year}' ";
                $where .= "     AND SCHREGNO = '{$schregno}' ";
                $where .= "     AND DET_SEQ  = '{$seq}' ";
                $query = Query::updateSQL($data2, "MEDEXAM_DET_DETAIL_DAT", $where);
                $db->query($query);
            } elseif ($model->inputFlg[$g] == "OK") {
                //データを入力していない（all　null）生徒は、レコードを作成しない。（ゴミデータを作成しないため）
                $query = Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT");
                $db->query($query);
            }

            //特別支援学校
            if ($model->Properties["useSpecial_Support_School"] == "1") {
                $seq = "018";
                $data2 = array();
                $data2["YEAR"][TEXT]         = $model->year;
                $data2["SCHREGNO"][TEXT]     = $schregno;
                $data2["DET_SEQ"][TEXT]      = $seq;
                $data2["DET_REMARK1"][TEXT]  = $model->fields["R_EAR_IN"][$g];
                $data2["DET_REMARK2"][TEXT]  = $model->fields["R_EAR_DB_IN"][$g];
                $data2["DET_REMARK3"][TEXT]  = $model->fields["R_EAR_DB_4000_IN"][$g];
                $data2["DET_REMARK4"][TEXT]  = $model->fields["L_EAR_IN"][$g];
                $data2["DET_REMARK5"][TEXT]  = $model->fields["L_EAR_DB_IN"][$g];
                $data2["DET_REMARK6"][TEXT]  = $model->fields["L_EAR_DB_4000_IN"][$g];
                $data2["REGISTERCD"][TEXT]   = STAFFCD;
                $data2["UPDATED"][FUNC]      = "sysdate()";

                $query = knjf013Query::checkMedexamDetDetailDat($model, $schregno, $seq);
                if ($db->getOne($query) > 0) {
                    $where  = " WHERE ";
                    $where .= "         YEAR     = '{$model->year}' ";
                    $where .= "     AND SCHREGNO = '{$schregno}' ";
                    $where .= "     AND DET_SEQ  = '{$seq}' ";
                    $query = Query::updateSQL($data2, "MEDEXAM_DET_DETAIL_DAT", $where);
                    $db->query($query);
                } elseif ($model->inputFlg[$g] == "OK") {
                    //データを入力していない（all　null）生徒は、レコードを作成しない。（ゴミデータを作成しないため）
                    $query = Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT");
                    $db->query($query);
                }
            }
        }

        if ($model->input_form == "2") {
            $seq = "009";
            $data2 = array();
            $data2["YEAR"][TEXT]         = $model->year;
            $data2["SCHREGNO"][TEXT]     = $schregno;
            $data2["DET_SEQ"][TEXT]      = $seq;
            $data2["DET_REMARK1"][TEXT]  = $model->fields["NUTRITIONCD_REMARK"][$g];
            if ($model->z010name1 == "koma") {
                $data2["DET_REMARK7"][TEXT]  = $model->fields["HEART_GRAPH_NO"][$g];
            }
            $data2["REGISTERCD"][TEXT]   = STAFFCD;
            $data2["UPDATED"][FUNC]      = "sysdate()";

            $query = knjf013Query::checkMedexamDetDetailDat($model, $schregno, $seq);
            if ($db->getOne($query) > 0) {
                $where  = " WHERE ";
                $where .= "     YEAR = '{$model->year}' ";
                $where .= "     AND SCHREGNO = '{$schregno}' ";
                $where .= "     AND DET_SEQ = '{$seq}' ";
                $query = Query::updateSQL($data2, "MEDEXAM_DET_DETAIL_DAT", $where);
                $db->query($query);
            } elseif ($model->inputFlg[$g] == "OK") {
                //データを入力していない（all　null）生徒は、レコードを作成しない。（ゴミデータを作成しないため）
                $query = Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT");
                $db->query($query);
            }

            $seq = "002";
            $data2 = array();
            $data2["YEAR"][TEXT]         = $model->year;
            $data2["SCHREGNO"][TEXT]     = $schregno;
            $data2["DET_SEQ"][TEXT]      = $seq;
            $data2["DET_REMARK7"][TEXT]  = $model->fields["EYEDISEASECD5"][$g];
            $data2["REGISTERCD"][TEXT]   = STAFFCD;
            $data2["UPDATED"][FUNC]      = "sysdate()";

            $query = knjf013Query::checkMedexamDetDetailDat($model, $schregno, $seq);
            if ($db->getOne($query) > 0) {
                $where  = " WHERE ";
                $where .= "     YEAR = '{$model->year}' ";
                $where .= "     AND SCHREGNO = '{$schregno}' ";
                $where .= "     AND DET_SEQ = '{$seq}' ";
                $query = Query::updateSQL($data2, "MEDEXAM_DET_DETAIL_DAT", $where);
                $db->query($query);
            } elseif ($model->inputFlg[$g] == "OK") {
                //データを入力していない（all　null）生徒は、レコードを作成しない。（ゴミデータを作成しないため）
                $query = Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT");
                $db->query($query);
            }

            $seq = "003";
            $data2 = array();
            $data2["YEAR"][TEXT]         = $model->year;
            $data2["SCHREGNO"][TEXT]     = $schregno;
            $data2["DET_SEQ"][TEXT]      = $seq;
            $data2["DET_REMARK1"][TEXT]  = $model->fields["NOSEDISEASECD_REMARK1"][$g];
            $data2["DET_REMARK2"][TEXT]  = $model->fields["NOSEDISEASECD_REMARK2"][$g];
            $data2["DET_REMARK3"][TEXT]  = $model->fields["NOSEDISEASECD_REMARK3"][$g];
            $data2["DET_REMARK7"][TEXT]  = $model->fields["NOSEDISEASECD5"][$g];
            $data2["DET_REMARK8"][TEXT]  = $model->fields["NOSEDISEASECD6"][$g];
            $data2["DET_REMARK9"][TEXT]  = $model->fields["NOSEDISEASECD7"][$g];
            $data2["REGISTERCD"][TEXT]   = STAFFCD;
            $data2["UPDATED"][FUNC]      = "sysdate()";

            $query = knjf013Query::checkMedexamDetDetailDat($model, $schregno, $seq);
            if ($db->getOne($query) > 0) {
                $where  = " WHERE ";
                $where .= "     YEAR = '{$model->year}' ";
                $where .= "     AND SCHREGNO = '{$schregno}' ";
                $where .= "     AND DET_SEQ = '{$seq}' ";
                $query = Query::updateSQL($data2, "MEDEXAM_DET_DETAIL_DAT", $where);
                $db->query($query);
            } elseif ($model->inputFlg[$g] == "OK") {
                //データを入力していない（all　null）生徒は、レコードを作成しない。（ゴミデータを作成しないため）
                $query = Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT");
                $db->query($query);
            }

            $seq = "006";
            $data2 = array();
            $data2["YEAR"][TEXT]         = $model->year;
            $data2["SCHREGNO"][TEXT]     = $schregno;
            $data2["DET_SEQ"][TEXT]      = $seq;
            $data2["DET_REMARK1"][TEXT]  = $model->fields["SKINDISEASECD_REMARK"][$g];
            $data2["REGISTERCD"][TEXT]   = STAFFCD;
            $data2["UPDATED"][FUNC]      = "sysdate()";

            $query = knjf013Query::checkMedexamDetDetailDat($model, $schregno, $seq);
            if ($db->getOne($query) > 0) {
                $where  = " WHERE ";
                $where .= "     YEAR = '{$model->year}' ";
                $where .= "     AND SCHREGNO = '{$schregno}' ";
                $where .= "     AND DET_SEQ = '{$seq}' ";
                $query = Query::updateSQL($data2, "MEDEXAM_DET_DETAIL_DAT", $where);
                $db->query($query);
            } elseif ($model->inputFlg[$g] == "OK") {
                //データを入力していない（all　null）生徒は、レコードを作成しない。（ゴミデータを作成しないため）
                $query = Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT");
                $db->query($query);
            }

            $seq = "005";
            $data2 = array();
            $data2["YEAR"][TEXT]         = $model->year;
            $data2["SCHREGNO"][TEXT]     = $schregno;
            $data2["DET_SEQ"][TEXT]      = $seq;
            $data2["DET_REMARK2"][TEXT]  = $model->fields["TB_NAME_REMARK1"][$g];
            $data2["REGISTERCD"][TEXT]   = STAFFCD;
            $data2["UPDATED"][FUNC]      = "sysdate()";

            $query = knjf013Query::checkMedexamDetDetailDat($model, $schregno, $seq);
            if ($db->getOne($query) > 0) {
                $where  = " WHERE ";
                $where .= "     YEAR = '{$model->year}' ";
                $where .= "     AND SCHREGNO = '{$schregno}' ";
                $where .= "     AND DET_SEQ = '{$seq}' ";
                $query = Query::updateSQL($data2, "MEDEXAM_DET_DETAIL_DAT", $where);
                $db->query($query);
            } elseif ($model->inputFlg[$g] == "OK") {
                //データを入力していない（all　null）生徒は、レコードを作成しない。（ゴミデータを作成しないため）
                $query = Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT");
                $db->query($query);
            }
        }

        if ($model->input_form == "3") {
            $seq = "001";
            $data2 = array();
            $data2["YEAR"][TEXT]         = $model->year;
            $data2["SCHREGNO"][TEXT]     = $schregno;
            $data2["DET_SEQ"][TEXT]      = $seq;
            $data2["DET_REMARK1"][TEXT]  = $model->fields["OTHER_REMARK2"][$g];
            $data2["REGISTERCD"][TEXT]   = STAFFCD;
            $data2["UPDATED"][FUNC]      = "sysdate()";

            $query = knjf013Query::checkMedexamDetDetailDat($model, $schregno, $seq);
            if ($db->getOne($query) > 0) {
                $where  = " WHERE ";
                $where .= "     YEAR = '{$model->year}' ";
                $where .= "     AND SCHREGNO = '{$schregno}' ";
                $where .= "     AND DET_SEQ = '{$seq}' ";
                $query = Query::updateSQL($data2, "MEDEXAM_DET_DETAIL_DAT", $where);
                $db->query($query);
            } elseif ($model->inputFlg[$g] == "OK") {
                //データを入力していない（all　null）生徒は、レコードを作成しない。（ゴミデータを作成しないため）
                $query = Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT");
                $db->query($query);
            }

            $seq = "004";
            $data2 = array();
            $data2["YEAR"][TEXT]         = $model->year;
            $data2["SCHREGNO"][TEXT]     = $schregno;
            $data2["DET_SEQ"][TEXT]      = $seq;
            $data2["DET_REMARK1"][TEXT]  = $model->fields["TREAT_REMARK1"][$g];
            $data2["REGISTERCD"][TEXT]   = STAFFCD;
            $data2["UPDATED"][FUNC]      = "sysdate()";

            $query = knjf013Query::checkMedexamDetDetailDat($model, $schregno, $seq);
            if ($db->getOne($query) > 0) {
                $where  = " WHERE ";
                $where .= "     YEAR = '{$model->year}' ";
                $where .= "     AND SCHREGNO = '{$schregno}' ";
                $where .= "     AND DET_SEQ = '{$seq}' ";
                $query = Query::updateSQL($data2, "MEDEXAM_DET_DETAIL_DAT", $where);
                $db->query($query);
            } elseif ($model->inputFlg[$g] == "OK") {
                //データを入力していない（all　null）生徒は、レコードを作成しない。（ゴミデータを作成しないため）
                $query = Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT");
                $db->query($query);
            }

            $seq = "011";
            $data2 = array();
            $data2["YEAR"][TEXT]         = $model->year;
            $data2["SCHREGNO"][TEXT]     = $schregno;
            $data2["DET_SEQ"][TEXT]      = $seq;
            $data2["DET_REMARK1"][TEXT]  = $model->fields["TREATCD2"][$g];
            $data2["REGISTERCD"][TEXT]   = STAFFCD;
            $data2["UPDATED"][FUNC]      = "sysdate()";

            $query = knjf013Query::checkMedexamDetDetailDat($model, $schregno, $seq);
            if ($db->getOne($query) > 0) {
                $where  = " WHERE ";
                $where .= "     YEAR = '{$model->year}' ";
                $where .= "     AND SCHREGNO = '{$schregno}' ";
                $where .= "     AND DET_SEQ = '{$seq}' ";
                $query = Query::updateSQL($data2, "MEDEXAM_DET_DETAIL_DAT", $where);
                $db->query($query);
            } elseif ($model->inputFlg[$g] == "OK") {
                //データを入力していない（all　null）生徒は、レコードを作成しない。（ゴミデータを作成しないため）
                $query = Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT");
                $db->query($query);
            }

            if ($model->Properties["printKenkouSindanIppan"] == "1" && $model->Properties["KenkouSindan_Ippan_Pattern"] == "1") {
                $seq = "017";
                $data2 = array();
                $data2["YEAR"][TEXT]         = $model->year;
                $data2["SCHREGNO"][TEXT]     = $schregno;
                $data2["DET_SEQ"][TEXT]      = $seq;
                $data2["DET_REMARK1"][TEXT]  = $model->fields["URICPH1"][$g];
                $data2["DET_REMARK2"][TEXT]  = $model->fields["URICPH2"][$g];
                $data2["REGISTERCD"][TEXT]   = STAFFCD;
                $data2["UPDATED"][FUNC]      = "sysdate()";

                $query = knjf013Query::checkMedexamDetDetailDat($model, $schregno, $seq);
                if ($db->getOne($query) > 0) {
                    $where  = " WHERE ";
                    $where .= "     YEAR = '{$model->year}' ";
                    $where .= "     AND SCHREGNO = '{$schregno}' ";
                    $where .= "     AND DET_SEQ = '{$seq}' ";
                    $query = Query::updateSQL($data2, "MEDEXAM_DET_DETAIL_DAT", $where);
                    $db->query($query);
                } elseif ($model->inputFlg[$g] == "OK") {
                    //データを入力していない（all　null）生徒は、レコードを作成しない。（ゴミデータを作成しないため）
                    $query = Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT");
                    $db->query($query);
                }
            }
        }
    }

    //MEDEXAM_DET_NO_DATを削除
    public function deleteMedexamDetNoDat($db, $model, $g)
    {
        $schregno = $model->fields["REGD_SCHREGNO"][$g];

        $query  = " DELETE FROM ";
        $query .= "     MEDEXAM_DET_NO_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '{$model->year}' AND ";
        $query .= "     SCHREGNO    = '{$schregno}' AND ";
        $query .= "     NO          = '1' ";

        if ($model->inputFlg[$g] == "OK") {
            //データを入力していない（all　null）生徒は、レコードを作成しない(消さない)。（ゴミデータを作成しないため）
            $db->query($query);
        }
        return;
    }

    //MEDEXAM_DET_NO_DAT更新
    public function insertMedexamDetNoDat($db, $model, $g)
    {
        $data = array();
        $schregno = $model->fields["REGD_SCHREGNO"][$g];

        $data["YEAR"][TEXT]                 = $model->year;
        $data["SCHREGNO"][TEXT]             = $schregno;
        $data["NO"][TEXT]                   = "1";
        $data["DATE"][TEXT]                 = str_replace("/", "-", $model->fields["DATE"][$g]);
        $data["HEIGHT"][NUMBER]             = $model->fields["HEIGHT"][$g];
        $data["WEIGHT"][NUMBER]             = $model->fields["WEIGHT"][$g];
        $data["SITHEIGHT"][NUMBER]          = $model->fields["SITHEIGHT"][$g];
        $data["R_BAREVISION_MARK"][TEXT]    = $model->fields["R_BAREVISION_MARK"][$g];
        $data["R_VISION_MARK"][TEXT]        = $model->fields["R_VISION_MARK"][$g];
        $data["L_BAREVISION_MARK"][TEXT]    = $model->fields["L_BAREVISION_MARK"][$g];
        $data["L_VISION_MARK"][TEXT]        = $model->fields["L_VISION_MARK"][$g];
        if ($model->z010name1 == "fukuiken" || $model->z010name1 == "koma") {
            $data["R_BAREVISION"][TEXT]         = $model->fields["R_BAREVISION"][$g];
            $data["R_VISION"][TEXT]             = $model->fields["R_VISION"][$g];
            $data["L_BAREVISION"][TEXT]         = $model->fields["L_BAREVISION"][$g];
            $data["L_VISION"][TEXT]             = $model->fields["L_VISION"][$g];
        }
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        if ($model->inputFlg[$g] == "OK") {
            //データを入力していない（all　null）生徒は、レコードを作成しない。（ゴミデータを作成しないため）
            $db->query(Query::insertSQL($data, "MEDEXAM_DET_NO_DAT"));
        }
        return;
    }

    //追加・更新
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        for ($g = 0; $g < $model->data_cnt; $g++) {
            //健康診断ヘッダデータ
            knjf013Query::getUpdateMedexamHdat($db, $model, $g);
            //健康診断一般データ
            knjf013Query::getUpdateMedexamDetDat($db, $model, $g);
            //MEDEXAM_DET_NO_DAT
            if ($model->input_form == "1" && $model->Properties["isMedexamDetNoDatWrite"] == "1") {
                knjf013Query::deleteMedexamDetNoDat($db, $model, $g);
                knjf013Query::insertMedexamDetNoDat($db, $model, $g);
            }
        }//for

        $db->commit();
        Query::dbCheckIn($db);
    }
}
