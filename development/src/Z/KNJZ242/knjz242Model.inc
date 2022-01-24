<?php

require_once('for_php7.php');

class knjz242Model extends model {
    var $cmd;
    var $field = array();
    var $fieldArr = array();
    var $z010name1;

    function knjz242Model() {}

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->field = array("YEAR"         =>  VARS::post("YEAR"),         //年度
                             "GRADE"        =>  VARS::post("GRADE")         //学年
                            );

        //学校を判断
        $db = Query::dbCheckOut();
        $this->z010name1 = $db->getOne(knjz242Query::getSchoolName());
        Query::dbCheckIn($db);

        foreach ($this->fieldArr as $field => $fieldVal) {
            $this->field[$field] = VARS::post($field);
        }

        //親がチェック有の時は、子供も全て "1" を立てる
        /** 視力 **/
        if ($this->field["VISION"] == "1") {
            $this->field["R_BAREVISION"]            = "1";
            $this->field["R_BAREVISION_MARK"]       = "1";
            $this->field["L_BAREVISION"]            = "1";
            $this->field["L_BAREVISION_MARK"]       = "1";
            $this->field["R_VISION"]                = "1";
            $this->field["R_VISION_MARK"]           = "1";
            $this->field["L_VISION"]                = "1";
            $this->field["L_VISION_MARK"]           = "1";
            $this->field["VISION_CANTMEASURE"]      = "1";
            $this->field["R_VISION_CANTMEASURE"]    = "1";
            $this->field["L_VISION_CANTMEASURE"]    = "1";
        }
        /** 聴力 **/
        if ($this->field["EAR"] == "1") {
            $this->field["R_EAR"]                  = "1";
            $this->field["R_EAR_DB"]               = "1";
            $this->field["R_EAR_DB_4000"]          = "1";
            $this->field["R_EAR_CANTMEASURE"]      = "1";
            $this->field["L_EAR"]                  = "1";
            $this->field["L_EAR_DB"]               = "1";
            $this->field["L_EAR_DB_4000"]          = "1";
            $this->field["L_EAR_CANTMEASURE"]      = "1";
        }
        /** 尿 **/
        if ($this->field["URI"] == "1") {
            $this->field["ALBUMINURIA1CD"]         = "1";
            $this->field["URICSUGAR1CD"]           = "1";
            $this->field["URICBLEED1CD"]           = "1";
            $this->field["ALBUMINURIA2CD"]         = "1";
            $this->field["URICSUGAR2CD"]           = "1";
            $this->field["URICBLEED2CD"]           = "1";
            $this->field["URICOTHERTESTCD"]        = "1";
            $this->field["URICOTHERTEST"]          = "1";
            $this->field["URI_ADVISECD"]           = "1";
        }
        /** 栄養状態 **/
        if ($this->field["NUTRITIONCD"] == "1") {
            $this->field["NUTRITIONCD_REMARK"]     = "1";
        }
        /** 脊柱・胸郭・四肢 **/
        if ($this->field["SPINERIBCD"] == "1") {
            $this->field["SPINERIBCD_REMARK"]      = "1";
        }
        /** 目の疾病及び異常 **/
        if ($this->field["EYE"] == "1") {
            $this->field["EYEDISEASECD"]           = "1";
            $this->field["EYEDISEASECD2"]          = "1";
            $this->field["EYEDISEASECD3"]          = "1";
            $this->field["EYEDISEASECD4"]          = "1";
            $this->field["EYEDISEASECD5"]          = "1";
            $this->field["EYE_TEST_RESULT"]        = "1";
            $this->field["EYE_TEST_RESULT2"]       = "1";
            $this->field["EYE_TEST_RESULT3"]       = "1";
        }
        /** 耳鼻咽頭疾患 **/
        if ($this->field["NOSEDISEASECD"] == "1") {
            $this->field["NOSEDISEASECD2"]         = "1";
            $this->field["NOSEDISEASECD3"]         = "1";
            $this->field["NOSEDISEASECD4"]         = "1";
            $this->field["NOSEDISEASECD5"]         = "1";
            $this->field["NOSEDISEASECD6"]         = "1";
            $this->field["NOSEDISEASECD7"]         = "1";
            $this->field["NOSEDISEASECD_REMARK"]   = "1";
            $this->field["NOSEDISEASECD_REMARK1"]  = "1";
            $this->field["NOSEDISEASECD_REMARK2"]  = "1";
            $this->field["NOSEDISEASECD_REMARK3"]  = "1";
        }
        /** 皮膚疾患 **/
        if ($this->field["SKINDISEASECD"] == "1") {
            $this->field["SKINDISEASECD_REMARK"]   = "1";
        }
        /** 心臓 **/
        if ($this->field["HEART"] == "1") {
            $this->field["HEART_MEDEXAM"]          = "1";
            $this->field["HEART_MEDEXAM_REMARK"]   = "1";
            $this->field["HEARTDISEASECD"]         = "1";
            $this->field["HEARTDISEASECD_REMARK"]  = "1";
            $this->field["MANAGEMENT_DIV"]         = "1";
            $this->field["MANAGEMENT_REMARK"]      = "1";
        }
        /** 結核 **/
        if ($this->field["TB_"] == "1") {
            $this->field["TB_DATE"]                = "1";
            $this->field["TB_REACT"]               = "1";
            $this->field["TB_RESULT"]              = "1";
            $this->field["TB_BCGDATE"]             = "1";
            $this->field["TB_FILMDATE"]            = "1";
            $this->field["TB_FILMNO"]              = "1";
            $this->field["TB_REMARKCD"]            = "1";
            $this->field["TB_OTHERTESTCD"]         = "1";
            $this->field["TB_OTHERTEST_REMARK1"]   = "1";
            $this->field["TB_NAMECD"]              = "1";
            $this->field["TB_NAME_REMARK1"]        = "1";
            $this->field["TB_ADVISECD"]            = "1";
            $this->field["TB_ADVISE_REMARK1"]      = "1";
            $this->field["TB_X_RAY"]               = "1";
        }
        /** 貧血 **/
        if ($this->field["ANEMIA_REMARK"] == "1") {
            $this->field["ANEMIA_REMARK"]          = "1";
            $this->field["HEMOGLOBIN"]             = "1";
        }
        /** 寄生虫卵 **/
        if ($this->field["PARASITE"] == "1") {
            $this->field["PARASITE"]               = "1";
        }
        /** 寄生虫卵 **/
        if ($this->field["OTHERDISEASECD"] == "1") {
            $this->field["OTHER_ADVISECD"]         = "1";
            $this->field["OTHER_REMARK"]           = "1";
            $this->field["OTHER_REMARK2"]          = "1";
            $this->field["OTHER_REMARK3"]          = "1";
        }
        /** 学校医 **/
        if ($this->field["DOC_"] == "1") {
            $this->field["DOC_CD"]                 = "1";
            $this->field["DOC_REMARK"]             = "1";
            $this->field["DOC_DATE"]               = "1";
        }
        /** 事後措置 **/
        if ($this->field["TREATCD"] == "1") {
            $this->field["TREATCD2"]               = "1";
            $this->field["TREAT_REMARK1"]          = "1";
            $this->field["TREAT_REMARK2"]          = "1";
            $this->field["TREAT_REMARK3"]          = "1";
        }
        /** 備考 **/
        if ($this->field["REMARK"] == "1") {
            $this->field["REMARK"]                 = "1";
        }
        /** 運動 **/
        if ($this->field["GUIDE_DIV"] == "1") {
            $this->field["JOINING_SPORTS_CLUB"]    = "1";
        }
        /** 既往症 **/
        if ($this->field["MEDICAL_HISTORY"] == "1") {
            $this->field["MEDICAL_HISTORY1"]       = "1";
            $this->field["MEDICAL_HISTORY2"]       = "1";
            $this->field["MEDICAL_HISTORY3"]       = "1";
        }

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //前年度からのコピーのモデルを返します
    function &getCopyModel() {
        //今年度のデータが存在するか確認
        $t_year = knjz242Query::CheckData($this->field["YEAR"]);
        if (isset($t_year)) {
           $this->setWarning('MSG203', "今年度のデータが存在します。");
           return false;
        }
        //前年度のデータが存在するか確認
        $pre_year = knjz242Query::CheckData($this->field["YEAR"] - 1);
        if (!isset($pre_year)) {
           $this->setWarning('MSG203', "前年データが存在しません。");
           return false;
        }

        knjz242Query::getCopyQuery($this);
        $this->setMessage("MSG201");
        return;
    }

    function &getMainModel() {}

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        $result = knjz242Query::getUpdateQuery($this);
        if ($result) {
            $this->setMessage("MSG201");
        }
    }
}
?>
