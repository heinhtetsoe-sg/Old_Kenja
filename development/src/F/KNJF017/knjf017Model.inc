<?php

require_once('for_php7.php');


class knjf017Model extends Model {
    var $field = array();
    var $cmd;
    var $grade;
    var $hr_class;
    var $input_form;
    var $z010name1;

    function knjf017Model() {}

    function init() {
        if (VARS::request("PROGRAMID") != "") {
            //学校を判断
            $db = Query::dbCheckOut();
            $this->z010name1 = $db->getOne(knjf017Query::getNameMstZ010());
            Query::dbCheckIn($db);
        }

        $this->cmd = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //学年
        $this->grade        = VARS::post("GRADE");
        //年組
        $this->hr_class     = VARS::post("HR_CLASS");
        //対象生徒
        $this->selectdata   = VARS::post("selectdata");
        //種類
        $this->input_form   = VARS::post("INPUT_FORM");
        //選択チェックボックス件数
        $this->check_cnt    = VARS::post("CHECK_CNT");

        //広工大フラグ
        $this->isHirokoudai = ($this->z010name1 == "hirokoudai") ? true: false;

        //フィールド
        $this->field = array("DATE"                  =>  VARS::post("DATE"),                 //健康診断実施日付
                             "R_BAREVISION"          =>  VARS::post("R_BAREVISION"),         //視力・右裸眼（数字）
                             "R_BAREVISION_MARK"     =>  VARS::post("R_BAREVISION_MARK"),    //視力・右裸眼（文字）
                             "R_VISION"              =>  VARS::post("R_VISION"),             //視力・右矯正（数字）
                             "R_VISION_MARK"         =>  VARS::post("R_VISION_MARK"),        //視力・右矯正（文字）
                             "L_BAREVISION"          =>  VARS::post("L_BAREVISION"),         //視力・左裸眼（数字）
                             "L_BAREVISION_MARK"     =>  VARS::post("L_BAREVISION_MARK"),    //視力・左裸眼（文字）
                             "L_VISION"              =>  VARS::post("L_VISION"),             //視力・左矯正（数字）
                             "L_VISION_MARK"         =>  VARS::post("L_VISION_MARK"),        //視力・左矯正（文字）
                             "R_EAR_DB"              =>  VARS::post("R_EAR_DB"),             //聴力・右DB
                             "R_EAR_DB_4000"         =>  VARS::post("R_EAR_DB_4000"),        //聴力・右4000Hz
                             "R_EAR"                 =>  VARS::post("R_EAR"),                //聴力・右状態
                             "L_EAR_DB"              =>  VARS::post("L_EAR_DB"),             //聴力・左DB
                             "L_EAR_DB_4000"         =>  VARS::post("L_EAR_DB_4000"),        //聴力・左4000Hz
                             "L_EAR"                 =>  VARS::post("L_EAR"),                //聴力・左状態
                             "ALBUMINURIA1CD"        =>  VARS::post("ALBUMINURIA1CD"),       //尿・１次蛋白
                             "URICSUGAR1CD"          =>  VARS::post("URICSUGAR1CD"),         //尿・１次糖
                             "URICBLEED1CD"          =>  VARS::post("URICBLEED1CD"),         //尿・１次潜血
                             "ALBUMINURIA2CD"        =>  VARS::post("ALBUMINURIA2CD"),       //尿・２次蛋白
                             "URICSUGAR2CD"          =>  VARS::post("URICSUGAR2CD"),         //尿・２次糖
                             "URICBLEED2CD"          =>  VARS::post("URICBLEED2CD"),         //尿・２次潜血
                             "URICOTHERTESTCD"       =>  VARS::post("URICOTHERTESTCD"),      //尿・その他の検査
                             "URICOTHERTEST"         =>  VARS::post("URICOTHERTEST"),        //尿・その他の検査
                             "URI_ADVISECD"          =>  VARS::post("URI_ADVISECD"),         //尿・指導区分(コンボ)
                             "NUTRITIONCD"           =>  VARS::post("NUTRITIONCD"),          //栄養状態
                             "NUTRITIONCD_REMARK"    =>  VARS::post("NUTRITIONCD_REMARK"),   //栄養状態備考
                             "EYEDISEASECD"          =>  VARS::post("EYEDISEASECD"),         //目の疾病及び異常
                             "EYEDISEASECD2"         =>  VARS::post("EYEDISEASECD2"),        //目の疾病及び異常
                             "EYEDISEASECD3"         =>  VARS::post("EYEDISEASECD3"),        //目の疾病及び異常
                             "EYEDISEASECD4"         =>  VARS::post("EYEDISEASECD4"),        //目の疾病及び異常
                             "EYE_TEST_RESULT"       =>  VARS::post("EYE_TEST_RESULT"),      //眼科検診結果
                             "EYE_TEST_RESULT2"      =>  VARS::post("EYE_TEST_RESULT2"),     //眼科検診結果2
                             "EYE_TEST_RESULT3"      =>  VARS::post("EYE_TEST_RESULT3"),     //眼科検診結果3
                             "SPINERIBCD"            =>  VARS::post("SPINERIBCD"),           //脊柱・胸郭・四肢(コンボ)
                             "SPINERIBCD_REMARK"     =>  VARS::post("SPINERIBCD_REMARK"),    //脊柱・胸郭・四肢(テキスト)
                             "NOSEDISEASECD"         =>  VARS::post("NOSEDISEASECD"),        //耳鼻咽頭疾患(コンボ)
                             "NOSEDISEASECD2"        =>  VARS::post("NOSEDISEASECD2"),       //耳鼻咽頭疾患(コンボ)
                             "NOSEDISEASECD3"        =>  VARS::post("NOSEDISEASECD3"),       //耳鼻咽頭疾患(コンボ)
                             "NOSEDISEASECD4"        =>  VARS::post("NOSEDISEASECD4"),       //耳鼻咽頭疾患(コンボ)
                             "NOSEDISEASECD_REMARK"  =>  VARS::post("NOSEDISEASECD_REMARK"), //耳鼻咽頭疾患(テキスト)
                             "NOSEDISEASECD_REMARK2" =>  VARS::post("NOSEDISEASECD_REMARK2"),//耳鼻咽頭疾患2(テキスト)
                             "NOSEDISEASECD_REMARK3" =>  VARS::post("NOSEDISEASECD_REMARK3"),//耳鼻咽頭疾患3(テキスト)
                             "SKINDISEASECD"         =>  VARS::post("SKINDISEASECD"),        //皮膚疾患
                             "SKINDISEASECD_REMARK"  =>  VARS::post("SKINDISEASECD_REMARK"), //皮膚疾患(テキスト)
                             "HEART_MEDEXAM"         =>  VARS::post("HEART_MEDEXAM"),        //心臓・臨床医学的検査(コンボ)
                             "HEART_MEDEXAM_REMARK"  =>  VARS::post("HEART_MEDEXAM_REMARK"), //心臓・臨床医学的検査(テキスト)
                             "HEARTDISEASECD"        =>  VARS::post("HEARTDISEASECD"),       //心臓・疾病及び異常(コンボ)
                             "HEARTDISEASECD_REMARK" =>  VARS::post("HEARTDISEASECD_REMARK"),//心臓・疾病及び異常(テキスト)
                             "MANAGEMENT_DIV"        =>  VARS::post("MANAGEMENT_DIV"),       //心臓・管理区分（コンボ）
                             "MANAGEMENT_REMARK"     =>  VARS::post("MANAGEMENT_REMARK"),    //心臓・管理区分（テキスト）
                             "TB_FILMDATE"           =>  VARS::post("TB_FILMDATE"),          //結核・撮影日付
                             "TB_REMARKCD"           =>  VARS::post("TB_REMARKCD"),          //結核・所見
                             "TB_X_RAY"              =>  VARS::post("TB_X_RAY"),             //結核・検査(X線)
                             "TB_OTHERTESTCD"        =>  VARS::post("TB_OTHERTESTCD"),       //結核・その他検査
                             "TB_OTHERTEST_REMARK1"  =>  VARS::post("TB_OTHERTEST_REMARK1"), //結核・その他検査
                             "TB_NAMECD"             =>  VARS::post("TB_NAMECD"),            //結核・病名
                             "TB_NAME_REMARK1"       =>  VARS::post("TB_NAME_REMARK1"),      //結核・病名
                             "TB_ADVISECD"           =>  VARS::post("TB_ADVISECD"),          //結核・指導区分
                             "TB_ADVISE_REMARK1"     =>  VARS::post("TB_ADVISE_REMARK1"),    //結核・指導区分
                             "OTHERDISEASECD"        =>  VARS::post("OTHERDISEASECD"),       //その他疾病及び異常(コンボ)
                             "OTHER_REMARK"          =>  VARS::post("OTHER_REMARK"),         //その他疾病及び異常(テキスト)
                             "OTHER_REMARK2"         =>  VARS::post("OTHER_REMARK2"),        //その他疾病及び異常2(テキスト)
                             "OTHER_REMARK3"         =>  VARS::post("OTHER_REMARK3"),        //その他疾病及び異常3(テキスト)
                             "OTHER_ADVISECD"        =>  VARS::post("OTHER_ADVISECD"),       //その他疾病及び異常・指導区分(コンボ)
                             "DOC_DATE"              =>  VARS::post("DOC_DATE"),             //学校医・所見日付
                             "DOC_CD"                =>  VARS::post("DOC_CD"),               //学校医・所見(コンボ)
                             "DOC_REMARK"            =>  VARS::post("DOC_REMARK"),           //学校医・所見(テキスト)
                             "REMARK"                =>  VARS::post("REMARK"),               //備考
                             "PARASITE"              =>  VARS::post("PARASITE"),             //寄生虫卵
                             "MEDICAL_HISTORY1"      =>  VARS::post("MEDICAL_HISTORY1"),     //既往症1
                             "MEDICAL_HISTORY2"      =>  VARS::post("MEDICAL_HISTORY2"),     //既往症2
                             "MEDICAL_HISTORY3"      =>  VARS::post("MEDICAL_HISTORY3"),     //既往症3
                             "DIAGNOSIS_NAME"        =>  VARS::post("DIAGNOSIS_NAME"),       //診断名
                             "GUIDE_DIV"             =>  VARS::post("GUIDE_DIV"),            //運動/指導区分
                             "JOINING_SPORTS_CLUB"   =>  VARS::post("JOINING_SPORTS_CLUB")   //運動/部活動
                            );

        for ($i=0; $i <= $this->check_cnt; $i++) {
            $this->field["RCHECK".$i] = VARS::post("RCHECK".$i);
        }

        if (VARS::request("PROGRAMID") != "") {
            $this->field["DATE"] = CTRL_DATE;
        }

        $this->setField = array();
        if ($this->input_form == "1") {
            $this->setField[0]  = array("DATE");
            $this->setField[1]  = array("R_BAREVISION", "R_BAREVISION_MARK");
            $this->setField[2]  = array("R_VISION", "R_VISION_MARK");
            $this->setField[3]  = array("L_BAREVISION", "L_BAREVISION_MARK");
            $this->setField[4]  = array("L_VISION", "L_VISION_MARK");
            $this->setField[5]  = array("R_EAR_DB");
            $this->setField[6]  = array("R_EAR");
            $this->setField[7]  = array("L_EAR_DB");
            $this->setField[8]  = array("L_EAR");
            $this->setField[9]  = array("R_EAR_DB_4000");
            $this->setField[10] = array("L_EAR_DB_4000");
        } else if ($this->input_form == "2") {
            $this->setField[0]  = array("ALBUMINURIA1CD");
            $this->setField[1]  = array("URICSUGAR1CD");
            $this->setField[2]  = array("URICBLEED1CD");
            $this->setField[3]  = array("ALBUMINURIA2CD");
            $this->setField[4]  = array("URICSUGAR2CD");
            $this->setField[5]  = array("URICBLEED2CD");
            $this->setField[6]  = array("URICOTHERTEST");
            $this->setField[7]  = array("URI_ADVISECD");
            $this->setField[8]  = array("NUTRITIONCD", "NUTRITIONCD_REMARK");
            $this->setField[9]  = array("EYEDISEASECD", "EYEDISEASECD2", "EYEDISEASECD3", "EYEDISEASECD4", "EYE_TEST_RESULT", "EYE_TEST_RESULT2", "EYE_TEST_RESULT3");
            $this->setField[10] = array("SPINERIBCD", "SPINERIBCD_REMARK");
            $this->setField[11] = array("NOSEDISEASECD", "NOSEDISEASECD2", "NOSEDISEASECD3", "NOSEDISEASECD4", "NOSEDISEASECD_REMARK", "NOSEDISEASECD_REMARK2", "NOSEDISEASECD_REMARK3");
            $this->setField[12] = array("SKINDISEASECD", "SKINDISEASECD_REMARK");
            $this->setField[13] = array("HEART_MEDEXAM", "HEART_MEDEXAM_REMARK");
            $this->setField[14] = array("HEARTDISEASECD", "HEARTDISEASECD_REMARK");
            $this->setField[15] = array("MANAGEMENT_DIV", "MANAGEMENT_REMARK");
            $this->setField[16] = array("TB_FILMDATE");
            $this->setField[17] = array("TB_REMARKCD");
            $this->setField[18] = array("TB_X_RAY");
            $this->setField[19] = array("TB_OTHERTESTCD", "TB_OTHERTEST_REMARK1");
            $this->setField[20] = array("TB_NAMECD", "TB_NAME_REMARK1");
            $this->setField[21] = array("TB_ADVISECD", "TB_ADVISE_REMARK1");
        } else if ($this->input_form == "3") {
            if ($this->Properties["printKenkouSindanIppan"] == "2") {
                $this->setField[0]  = array("OTHERDISEASECD", "OTHER_REMARK", "OTHER_REMARK2", "OTHER_REMARK3");
            } else {
                $this->setField[0]  = array("OTHERDISEASECD");
            }
            if ($this->Properties["printKenkouSindanIppan"] == "1") {
                $this->setField[1]  = array("OTHER_ADVISECD", "OTHER_REMARK");
            } else {
                $this->setField[1]  = array("OTHER_ADVISECD");
            }
            $this->setField[2]  = array("DOC_DATE");
            $this->setField[3]  = array("DOC_CD", "DOC_REMARK");
            $this->setField[4]  = array("REMARK");
            $this->setField[5]  = array("TB_FILMDATE");
            $this->setField[6]  = array("TB_REMARKCD");
            $this->setField[7]  = array("TB_X_RAY");
            $this->setField[8]  = array("TB_OTHERTESTCD");
            $this->setField[9]  = array("TB_NAMECD");
            $this->setField[10] = array("TB_ADVISECD");
            $this->setField[11] = array("PARASITE");
            $this->setField[12] = array("ALBUMINURIA1CD");
            $this->setField[13] = array("URICSUGAR1CD");
            $this->setField[14] = array("URICBLEED1CD");
            $this->setField[15] = array("ALBUMINURIA2CD");
            $this->setField[16] = array("URICSUGAR2CD");
            $this->setField[17] = array("URICBLEED2CD");
            $this->setField[18] = array("URICOTHERTEST");
            $this->setField[19] = array("URI_ADVISECD");
            $this->setField[20] = array("URICOTHERTESTCD");
        } else if ($this->input_form == "4") {
            $this->setField[0]  = array("MEDICAL_HISTORY1");
            $this->setField[1]  = array("MEDICAL_HISTORY2");
            $this->setField[2]  = array("MEDICAL_HISTORY3");
            $this->setField[3]  = array("DIAGNOSIS_NAME");
            $this->setField[4]  = array("GUIDE_DIV");
            $this->setField[5]  = array("JOINING_SPORTS_CLUB");
        }
    }

    //入力項目チェック
    function validateInput() {
        //チェック項目一覧
        $checkdata = array();
        $checkdata["DATE"]                  = array(10,   "",       "",     "健康診断実施日");
        $checkdata["R_BAREVISION"]          = array(5,    "FLOAT",  "",     "視力・右裸眼（数字）");
        $checkdata["R_VISION"]              = array(5,    "FLOAT",  "",     "視力・右矯正（数字）");
        $checkdata["L_BAREVISION"]          = array(5,    "FLOAT",  "",     "視力・左裸眼（数字）");
        $checkdata["L_VISION"]              = array(5,    "FLOAT",  "",     "視力・左裸眼（数字）");
        $checkdata["R_BAREVISION_MARK"]     = array(1,    "",       "",     "視力・右裸眼（文字）");
        $checkdata["R_VISION_MARK"]         = array(1,    "",       "",     "視力・右矯正（文字）");
        $checkdata["L_BAREVISION_MARK"]     = array(1,    "",       "",     "視力・左裸眼（文字）");
        $checkdata["L_VISION_MARK"]         = array(1,    "",       "",     "視力・左矯正（文字）");
        $checkdata["R_EAR_DB"]              = array(3,    "INT",    "",     "聴力・右DB");
        $checkdata["R_EAR_DB_4000"]         = array(3,    "INT",    "",     "聴力・右4000Hz");
        $checkdata["R_EAR"]                 = array("",   "",       "F010", "聴力・右状態");
        $checkdata["L_EAR_DB"]              = array(3,    "INT",    "",     "聴力・左DB");
        $checkdata["L_EAR_DB_4000"]         = array(3,    "INT",    "",     "聴力・左4000Hz");
        $checkdata["L_EAR"]                 = array("",   "",       "F010", "聴力・左状態");
        $checkdata["ALBUMINURIA1CD"]        = array("",   "",       "F020", "尿・１次蛋白");
        $checkdata["URICSUGAR1CD"]          = array("",   "",       "F019", "尿・１次糖");
        $checkdata["URICBLEED1CD"]          = array("",   "",       "F018", "尿・１次潜血");
        $checkdata["ALBUMINURIA2CD"]        = array("",   "",       "F020", "尿・２次蛋白");
        $checkdata["URICSUGAR2CD"]          = array("",   "",       "F019", "尿・２次糖");
        $checkdata["URICBLEED2CD"]          = array("",   "",       "F018", "尿・２次潜血");
        $checkdata["URICOTHERTEST"]         = array(60,   "",       "",     "尿・その他の検査");
        $checkdata["NUTRITIONCD"]           = array("",   "",       "F030", "栄養状態");
        $checkdata["NUTRITIONCD_REMARK"]    = array(60,   "",       "",     "栄養状態備考");
        $checkdata["EYEDISEASECD"]          = array("",   "",       "F050", "目の疾病及び異常");
        $checkdata["EYE_TEST_RESULT"]       = array(60,   "",       "",     "眼科検診結果");
        $checkdata["EYE_TEST_RESULT2"]      = array(60,   "",       "",     "眼科検診結果2");
        $checkdata["EYE_TEST_RESULT3"]      = array(60,   "",       "",     "眼科検診結果3");
        $checkdata["SPINERIBCD"]            = array("",   "",       "F040", "脊柱・胸郭・四肢");
        $checkdata["SPINERIBCD_REMARK"]     = array(60,   "",       "",     "脊柱・胸郭・四肢");
        $checkdata["NOSEDISEASECD"]         = array("",   "",       "F060", "耳鼻咽頭疾患");
        $checkdata["NOSEDISEASECD_REMARK"]  = array(60,   "",       "",     "耳鼻咽頭疾患");
        $checkdata["NOSEDISEASECD_REMARK2"] = array(60,   "",       "",     "耳鼻咽頭疾患2");
        $checkdata["NOSEDISEASECD_REMARK3"] = array(60,   "",       "",     "耳鼻咽頭疾患3");
        $checkdata["SKINDISEASECD"]         = array("",   "",       "F070", "皮膚疾患");
        $checkdata["SKINDISEASECD_REMARK"]  = array(60,   "",       "",     "皮膚疾患");
        $checkdata["HEART_MEDEXAM"]         = array("",   "",       "F080", "心臓・臨床医学的検査");
        $checkdata["HEART_MEDEXAM_REMARK"]  = array(60,   "",       "",     "心臓・臨床医学的検査");
        $checkdata["HEARTDISEASECD"]        = array("",   "",       "F090", "心臓・疾病及び異常");
        $checkdata["HEARTDISEASECD_REMARK"] = array(60,   "",       "",     "心臓・疾病及び異常");
        $checkdata["MANAGEMENT_REMARK"]     = array(60,   "",       "",     "心臓・管理区分");
        $checkdata["TB_FILMDATE"]           = array(10,   "",       "",     "結核・撮影日付");
        $checkdata["TB_REMARKCD"]           = array("",   "",       "F100", "結核・所見");
        $checkdata["TB_X_RAY"]              = array(60,   "",       "",     "結核・検査(X線)");
        $checkdata["TB_OTHERTESTCD"]        = array("",   "",       "F110", "結核・その他検査");
        $checkdata["TB_OTHERTEST_REMARK1"]  = array(60,   "",       "",     "結核・その他検査");
        $tb_namecd_label = ($this->Properties["printKenkouSindanIppan"] == "1" && $this->z010name1 != "miyagiken") ? "疾病及び異常" : "病名";
        $checkdata["TB_NAMECD"]             = array("",   "",       "F120", "結核・".$tb_namecd_label);
        $checkdata["TB_NAME_REMARK1"]       = array(60,   "",       "",     "結核・".$tb_namecd_label);
        $checkdata["TB_ADVISE_REMARK1"]     = array(60,   "",       "",     "結核・指導区分");
        $checkdata["OTHERDISEASECD"]        = array("",   "",       "F140", "その他疾病及び異常");
        $checkdata["OTHER_REMARK"]          = array(60,   "",       "",     "その他疾病及び異常");
        $checkdata["OTHER_REMARK2"]         = array(60,   "",       "",     "その他疾病及び異常2");
        $checkdata["OTHER_REMARK3"]         = array(60,   "",       "",     "その他疾病及び異常3");
        $checkdata["DOC_REMARK"]            = array(60,   "",       "",     "学校医・所見");
        $checkdata["REMARK"]                = array(60,   "",       "",     "備考");
        $checkdata["DIAGNOSIS_NAME"]        = array(150,  "",       "",     "診断名");

        //初期値取得
        if ($this->Properties["printKenkouSindanIppan"] == "2") {
            $db = Query::dbCheckOut();
            $default = array();
            foreach ($this->setField as $key => $array) {
                if ($this->field["RCHECK".$key] == "1") {
                    foreach ($array as $field) {
                        if (strlen($checkdata[$field][2]) > 0) {
                            $flg = (in_array($field, array("ALBUMINURIA2CD", "URICSUGAR2CD", "URICBLEED2CD"))) ? "2" : "1";
                            $default[$field] = $db->getOne(knjf017Query::getNameMst($this, $checkdata[$field][2], $flg));
                        }
                    }
                }
            }
            Query::dbCheckIn($db);
        }

        //広工大は数値が名称マスタ設定の範囲内に存在するかチェック
        if ($this->isHirokoudai) {
            $visionChkHiro = true;
            $checkHiroArr = array();
            $visions = array('R_BAREVISION', 'R_VISION', 'L_BAREVISION', 'L_VISION');
            $db = Query::dbCheckOut();
            $query = knjf017Query::getVisionMark($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $checkHiroArr[$row["NAMECD2"]] = $row;
            }
            Query::dbCheckIn($db);

            foreach ($visions as $val) {
                $valMark = $val."_MARK";
                foreach ($checkHiroArr as $nameCd2 => $arrVal) {
                    if ($this->field[$val] != '') {
                        $visionChkHiro = false;
                        if ('A' == mb_convert_kana($arrVal["NAMECD2"], 'r')) {
                            if ($arrVal["NAMESPARE1"] != '' && $this->field[$val] >= $arrVal["NAMESPARE1"]) {
                                $this->field[$valMark] = mb_convert_kana($arrVal["NAMECD2"], 'r');
                                $visionChkHiro = true;
                                break 1;
                            }
                        } else {
                            if (($arrVal["NAMESPARE1"] <= $this->field[$val]) && ($this->field[$val] <= $arrVal["ABBV3"])) {
                                $this->field[$valMark] = mb_convert_kana($arrVal["NAMECD2"], 'r');
                                $visionChkHiro = true;
                                break 1;
                            }
                        }
                    }
                }
                if (!$visionChkHiro) {
                    break;
                }
            }
        }
        if (!$visionChkHiro) {
            $this->setWarning("MSG203","視力・名称マスタの設定を確認して下さい。");
            return false;
        }

        foreach ($this->setField as $key => $array) {
            if ($this->field["RCHECK".$key] == "1") {
                foreach ($array as $field) {
                    $value = $this->field[$field];
                    $label = "(".$checkdata[$field][3].")";

                    //値チェック（数値）
                    if ($checkdata[$field][1] == "INT" && strlen($value) > 0 && !is_numeric($value)) {
                        $this->setWarning("MSG907", $label);
                        return false;
                    }

                    //値チェック（小数点）
                    if ($checkdata[$field][1] == "FLOAT" && strlen($value) > 0) {
                        $fval = explode(".", $value);
                        if (strlen($fval[0]) > 1) {
                            $this->setWarning("MSG901", $label);
                            return false;
                        } else if (strlen($fval[1]) > 3) {
                            $this->setWarning("MSG901", $label."小数点第３位まで" );
                            return false;
                        }
                    }

                    //サイズチェック
                    if ($checkdata[$field][0] && strlen($value) > $checkdata[$field][0]) {
                        $this->setWarning("MSG915", $label.$checkdata[$field][0]."バイトまで" );
                        return false;
                    }

                    //データチェック（値、初期値）
                    if ($this->Properties["printKenkouSindanIppan"] == "2") {
                        if (strlen($checkdata[$field][2]) > 0 && in_array($field, array("EYEDISEASECD", "NOSEDISEASECD"))) {

                            if (strlen($value) == 0 && strlen($default[$field]) == 0) {
                                $this->setWarning("MSG310", $label);
                                return false;
                            }
                        }
                    }
                }
            }
        }

        //初期値セット
        if ($this->Properties["printKenkouSindanIppan"] == "2") {
            foreach ($this->setField as $key => $array) {
                if ($this->field["RCHECK".$key] == "1") {
                    foreach ($array as $field) {
                        if (strlen($checkdata[$field][2]) > 0) {
                            $this->field[$field] = (strlen($this->field[$field]) > 0) ? $this->field[$field] : $default[$field];
                        }
                    }
                }
            }
        }

        return true;
    }

    //追加および更新
    function &getUpdateModel() {
        $detail = array();
        $detail["OTHER_REMARK2"]            = array("001", "DET_REMARK2");
        $detail["OTHER_REMARK3"]            = array("001", "DET_REMARK3");
        $detail["EYE_TEST_RESULT2"]         = array("002", "DET_REMARK2");
        $detail["EYE_TEST_RESULT3"]         = array("002", "DET_REMARK3");
        $detail["EYEDISEASECD2"]            = array("002", "DET_REMARK4");
        $detail["EYEDISEASECD3"]            = array("002", "DET_REMARK5");
        $detail["EYEDISEASECD4"]            = array("002", "DET_REMARK6");
        $detail["NOSEDISEASECD_REMARK2"]    = array("003", "DET_REMARK2");
        $detail["NOSEDISEASECD_REMARK3"]    = array("003", "DET_REMARK3");
        $detail["NOSEDISEASECD2"]           = array("003", "DET_REMARK4");
        $detail["NOSEDISEASECD3"]           = array("003", "DET_REMARK5");
        $detail["NOSEDISEASECD4"]           = array("003", "DET_REMARK6");
        $detail["TB_OTHERTEST_REMARK1"]     = array("005", "DET_REMARK1");
        $detail["TB_NAME_REMARK1"]          = array("005", "DET_REMARK2");
        $detail["TB_ADVISE_REMARK1"]        = array("005", "DET_REMARK3");
        $detail["SKINDISEASECD_REMARK"]     = array("006", "DET_REMARK1");
        $detail["URICOTHERTESTCD"]          = array("007", "DET_REMARK1");
        $detail["NUTRITIONCD_REMARK"]       = array("009", "DET_REMARK1");
        $detail["R_EAR_DB_4000"]            = array("009", "DET_REMARK2");
        $detail["L_EAR_DB_4000"]            = array("009", "DET_REMARK3");

        //入力チェック
        if ($this->validateInput()) {
            knjf017Query::getUpdateQuery($this, $detail);
            if ($this->input_form == "1" && $this->field["DATE"] == "") {
                $this->field["DATE"] = CTRL_DATE;
            }
            $this->setMessage("MSG201");
        }
    }

}
?>
