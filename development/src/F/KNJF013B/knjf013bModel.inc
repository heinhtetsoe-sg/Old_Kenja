<?php

require_once('for_php7.php');


class knjf013bModel extends Model
{
    public $fields = array();
    public $cmd;
    public $year;
    public $semester;
    public $hr_class;
    public $input_form;
    public $data_cnt;
    public $control;
    public $z010name1;
    public $tb_filmnoFieldSize;    //結核の画像番号サイズ

    public function knjf013bModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        if (VARS::request("PROGRAMID") != "") {
            $this->year = CTRL_YEAR;
            $this->semester = CTRL_SEMESTER;

            //学校を判断
            $db = Query::dbCheckOut();
            $this->z010name1 = $db->getOne(knjf013bQuery::getNameMstZ010());
            Query::dbCheckIn($db);
        }

        $this->data_cnt = VARS::post("DATA_CNT");

        $this->cmd   = VARS::request("cmd");
        $this->windowWidth  = VARS::post("windowWidth");
        $this->windowHeight = VARS::post("windowHeight");

        //更新中、サブミットする項目使用不可
        //よって、サブミットする項目の値は、hiddenから取得
        if ($this->cmd == 'update') {
            $this->hr_class     = VARS::post("H_HR_CLASS");
            $this->input_form   = VARS::post("H_INPUT_FORM");
        } else {
            $this->hr_class     = VARS::post("HR_CLASS");
            $this->input_form   = VARS::post("INPUT_FORM");
        }

        //初期化
        $this->fields = array();
        $this->inputFlg = array();
        if ($this->cmd == 'update') {
            $paramArray = $this->getParam();
            for ($g = 0; $g < $this->data_cnt; $g++) {
                $this->inputFlg[$g] = "NG";
                foreach ($paramArray as $val) {
                    $this->fields[$val][] = VARS::post($val."-".$g);

                    if (!in_array($val, array("ATTENDNO","NAME_SHOW","REGD_SCHREGNO","SCHOOL_KIND","HIDDENDATE"))) {
                        if (strlen($this->fields[$val][$g])) {
                            $this->inputFlg[$g] = "OK";
                        }
                    }
                }
            }//for
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //結核画像番号サイズ
        $this->tb_filmnoFieldSize = ($this->Properties["usetb_filmnoFieldSize"] > 0) ? $this->Properties["usetb_filmnoFieldSize"] : 6;
    }

    public function getParam()
    {
        $paramArray = array(
            'ATTENDNO',
            'NAME_SHOW',

            'REGD_SCHREGNO',
            'SCHOOL_KIND',
            'HIDDENDATE',
            'DATE',

            'HEIGHT',
            'WEIGHT',
            'SITHEIGHT',
            'NO_PRINT_OBESITY_INDEX',
            'R_BAREVISION_MARK',
            'L_BAREVISION_MARK',
            'R_VISION_MARK',
            'L_VISION_MARK',
            'R_BAREVISION',
            'L_BAREVISION',
            'R_VISION',
            'L_VISION',
            'R_VISION_CANTMEASURE',
            'L_VISION_CANTMEASURE',
            'R_EAR_DB',
            'R_EAR_DB_1000',
            'R_EAR_DB_4000',
            'R_EAR',
            'R_EAR_DB_IN',
            'R_EAR_DB_4000_IN',
            'R_EAR_IN',
            'R_EAR_CANTMEASURE',
            'L_EAR_DB',
            'L_EAR_DB_1000',
            'L_EAR_DB_4000',
            'L_EAR',
            'L_EAR_DB_IN',
            'L_EAR_DB_4000_IN',
            'L_EAR_IN',
            'L_EAR_CANTMEASURE',

            'NUTRITIONCD',
            'NUTRITIONCD_REMARK',
            'SPINERIBCD',
            'SPINERIBCD_REMARK',
            'EYEDISEASECD',
            'EYEDISEASECD2',
            'EYEDISEASECD3',
            'EYEDISEASECD4',
            'EYE_TEST_RESULT',
            'EYE_TEST_RESULT2',
            'EYE_TEST_RESULT3',
            'EYEDISEASECD5',
            'NOSEDISEASECD',
            'NOSEDISEASECD_REMARK',
            'NOSEDISEASECD2',
            'NOSEDISEASECD3',
            'NOSEDISEASECD4',
            'NOSEDISEASECD5',
            'NOSEDISEASECD6',
            'NOSEDISEASECD7',
            'NOSEDISEASECD_REMARK1',
            'NOSEDISEASECD_REMARK2',
            'NOSEDISEASECD_REMARK3',
            'SKINDISEASECD',
            'SKINDISEASECD_REMARK',
            'TB_FILMDATE',
            'TB_FILMNO',
            'TB_REMARKCD',
            'TB_X_RAY',
            'TB_OTHERTESTCD',
            'TB_OTHERTEST_REMARK1',
            'TB_NAMECD',
            'TB_NAME_REMARK1',
            'TB_ADVISECD',
            'TB_ADVISE_REMARK1',
            'HEART_MEDEXAM',
            'HEART_MEDEXAM_REMARK',
            'HEART_GRAPH_NO',
            'HEARTDISEASECD',
            'HEARTDISEASECD_REMARK',
            'MANAGEMENT_DIV',
            'MANAGEMENT_REMARK',

            'ALBUMINURIA1CD',
            'URICSUGAR1CD',
            'URICBLEED1CD',
            'URICPH1',
            'ALBUMINURIA2CD',
            'URICSUGAR2CD',
            'URICBLEED2CD',
            'URICOTHERTESTCD',
            'URICPH2',
            'URICOTHERTEST',
            'URI_ADVISECD',
            'PARASITE',
            'OTHERDISEASECD',
            'OTHER_REMARK2',
            'OTHER_ADVISECD',
            'OTHER_REMARK',
            'OTHER_REMARK3',
            'DOC_CD',
            'ANEMIA_REMARK',
            'HEMOGLOBIN',
            'DOC_REMARK',
            'DOC_DATE',
            'TREATCD',
            'TREATCD2',
            'TREAT_REMARK1',
            'TREAT_REMARK2',
            'TREAT_REMARK3',
            'REMARK',
            'MESSAGE',

            'MEDICAL_HISTORY1',
            'MEDICAL_HISTORY2',
            'MEDICAL_HISTORY3',
            'DIAGNOSIS_NAME',
            'GUIDE_DIV',
            'JOINING_SPORTS_CLUB'
            );
        return $paramArray;
    }

    //入力項目チェック
    public function validateInput()
    {
        for ($g = 0; $g < $this->data_cnt; $g++) {
            //エラーメッセージに出席番号・氏名を表示（誰がエラーかわかるように）
            $atno_name = $this->fields["ATTENDNO"][$g].'番 '.$this->fields["NAME_SHOW"][$g];
            //キーの更新チェック
            if (!strlen($this->fields["REGD_SCHREGNO"][$g])) {
                $this->setWarning("MSG301", $atno_name."：(学籍番号)");
                return false;
            }
            //データを入力されている時は、健康診断実施日のnullチェックをする。
            if ($this->inputFlg[$g] == "OK" && !strlen($this->fields["DATE"][$g])) {
                $this->setWarning("MSG301", $atno_name."：(健康診断実施日)");
                return false;
            }
            if ($this->Properties["printKenkouSindanIppan"] == "1" && $this->Properties["KenkouSindan_Ippan_Pattern"] == "1") {
                //2.1桁チェックと数値チェック
                $ph1 = $ph2 = array();
                $ph1 = explode(".", $this->fields["URICPH1"][$g]);
                if (is_array($ph1)) {
                    if (get_count($ph1) > 2 || $this->fields["URICPH1"][$g] == ".") {
                        $this->setWarning("MSG901", $atno_name."：(尿１次ＰＨ)");
                        return false;
                    } else {
                        if (strlen($ph1[0]) > 2) {
                            $this->setWarning("MSG901", $atno_name."：(尿１次ＰＨ) 整数は2桁まで");
                        } elseif (get_count($ph1) == 2 && strlen($ph1[1]) > 1) {
                            $this->setWarning("MSG901", $atno_name."：(尿１次ＰＨ) 小数は第1位まで");
                        }
                    }
                }
                $ph2 = explode(".", $this->fields["URICPH2"][$g]);
                if (is_array($ph2)) {
                    if (get_count($ph2) > 2 || $this->fields["URICPH2"][$g] == ".") {
                        $this->setWarning("MSG901", $atno_name."：(尿２次ＰＨ)");
                        return false;
                    } else {
                        if (strlen($ph2[0]) > 2) {
                            $this->setWarning("MSG901", $atno_name."：(尿２次ＰＨ) 整数は2桁まで");
                        } elseif (get_count($ph2) == 2 && strlen($ph2[1]) > 1) {
                            $this->setWarning("MSG901", $atno_name."：(尿２次ＰＨ) 小数は第1位まで");
                        }
                    }
                }
            }
            //値チェック
            $height = explode(".", $this->fields["HEIGHT"][$g]);
            $weight = explode(".", $this->fields["WEIGHT"][$g]);
            $sitheight = explode(".", $this->fields["SITHEIGHT"][$g]);
            if (strlen($height[0]) > 3) {
                $this->setWarning("MSG901", $atno_name."：(身長)");
                return false;
            } elseif (strlen($height[1]) > 1) {
                $this->setWarning("MSG901", $atno_name."：(身長) 小数点第一位まで");
                return false;
            }
            if (strlen($weight[0]) > 3) {
                $this->setWarning("MSG901", $atno_name."：(体重)");
                return false;
            } elseif (strlen($weight[1]) > 1) {
                $this->setWarning("MSG901", $atno_name."：(体重) 小数点第一位まで");
                return false;
            }
            if (strlen($sitheight[0]) > 3) {
                $this->setWarning("MSG901", $atno_name."：(座高)");
                return false;
            } elseif (strlen($sitheight[1]) > 1) {
                $this->setWarning("MSG901", $atno_name."：(座高) 小数点第一位まで");
                return false;
            }
            $r_barevision = explode(".", $this->fields["R_BAREVISION"][$g]);
            $r_vision     = explode(".", $this->fields["R_VISION"][$g]);
            $l_barevision = explode(".", $this->fields["L_BAREVISION"][$g]);
            $l_vision     = explode(".", $this->fields["L_VISION"][$g]);
            if (strlen($r_barevision[0]) > 1) {
                $this->setWarning("MSG901", $atno_name."：(視力・右裸眼)");
                return false;
            } elseif (strlen($r_barevision[1]) > 3) {
                $this->setWarning("MSG901", $atno_name."：(視力・右裸眼) 小数点第３位まで");
                return false;
            }
            if (strlen($r_vision[0]) > 1) {
                $this->setWarning("MSG901", $atno_name."：(視力・右矯正)");
                return false;
            } elseif (strlen($r_vision[1]) > 3) {
                $this->setWarning("MSG901", $atno_name."：(視力・右矯正) 小数点第３位まで");
                return false;
            }
            if (strlen($l_barevision[0]) > 1) {
                $this->setWarning("MSG901", $atno_name."：(視力・左裸眼)");
                return false;
            } elseif (strlen($l_barevision[1]) > 3) {
                $this->setWarning("MSG901", $atno_name."：(視力・左裸眼) 小数点第３位まで");
                return false;
            }
            if (strlen($l_vision[0]) > 1) {
                $this->setWarning("MSG901", $atno_name."：(視力・左矯正)");
                return false;
            } elseif (strlen($l_vision[1]) > 3) {
                $this->setWarning("MSG901", $atno_name."：(視力・左矯正) 小数点第３位まで");
                return false;
            }
            //視力の整合性チェック
            $visionChk = true;
            $checkHiroArr = array();
            $visions = array('R_BAREVISION', 'R_VISION', 'L_BAREVISION', 'L_VISION');
            $db = Query::dbCheckOut();
            $query = knjf013bQuery::getVisionMark($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                foreach ($visions as $val) {
                    $valMark = $val."_MARK";
                    // 文字、数字ともに入力時にチェック
                    if ($this->fields[$val][$g] != '' && $this->fields[$valMark][$g] != '' && ($this->fields[$valMark][$g] == $row["NAMECD2"])) {
                        if ('A' == mb_convert_kana($row["NAMECD2"], 'r')) {
                            if ($this->fields[$val][$g] >= $row["NAMESPARE1"]) {
                            } else {
                                $visionChk = false;
                            }
                        } else {
                            if (($row["NAMESPARE1"] <= $this->fields[$val][$g]) && ($this->fields[$val][$g] <= $row["ABBV3"])) {
                            } else {
                                $visionChk = false;
                            }
                        }
                    }
                }
                $checkHiroArr[$row["NAMECD2"]] = $row;
            }
            Query::dbCheckIn($db);
            if (!$visionChk) {
                $this->setWarning("MSG901", $atno_name."：視力・入力値の整合性エラーがあります。");
                return false;
            }
            //広工大は数値が名称マスタ設定の範囲内に存在するかチェック
            $visionChkHiro = true;
            if ($this->z010name1 == "hirokoudai") {
                foreach ($visions as $val) {
                    $valMark = $val."_MARK";
                    foreach ($checkHiroArr as $nameCd2 => $arrVal) {
                        if ($this->fields[$val][$g] != '') {
                            $visionChkHiro = false;
                            if ('A' == mb_convert_kana($arrVal["NAMECD2"], 'r')) {
                                if ($arrVal["NAMESPARE1"] != '' && $this->fields[$val][$g] >= $arrVal["NAMESPARE1"]) {
                                    $this->fields[$valMark][$g] = mb_convert_kana($arrVal["NAMECD2"], 'r');
                                    $visionChkHiro = true;
                                    break 1;
                                }
                            } else {
                                if (($arrVal["NAMESPARE1"] <= $this->fields[$val][$g]) && ($this->fields[$val][$g] <= $arrVal["ABBV3"])) {
                                    $this->fields[$valMark][$g] = mb_convert_kana($arrVal["NAMECD2"], 'r');
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
                $this->setWarning("MSG203", "視力・名称マスタの設定を確認して下さい。");
                return false;
            }

            if ($this->fields["R_EAR_DB"][$g] > 999) {
                $this->setWarning("MSG915", $atno_name."：聴力・右DBは半角数字で999までです。");
                return false;
            }
            if ($this->z010name1 == "koma") {
                if ($this->fields["R_EAR_DB_1000"][$g] > 999) {
                    $this->setWarning("MSG915", $atno_name."：聴力・右1000Hzは半角数字で999までです。");
                    return false;
                }
            }
            if ($this->fields["R_EAR_DB_4000"][$g] > 999) {
                $this->setWarning("MSG915", $atno_name."：聴力・右4000Hzは半角数字で999までです。");
                return false;
            }
            if ($this->fields["L_EAR_DB"][$g] > 999) {
                $this->setWarning("MSG915", $atno_name."：聴力・左DBは半角数字で999までです。");
                return false;
            }
            if ($this->z010name1 == "koma") {
                if ($this->fields["L_EAR_DB_1000"][$g] > 999) {
                    $this->setWarning("MSG915", $atno_name."：聴力・左1000Hzは半角数字で999までです。");
                    return false;
                }
            }
            if ($this->fields["L_EAR_DB_4000"][$g] > 999) {
                $this->setWarning("MSG915", $atno_name."：聴力・左4000Hzは半角数字で999までです。");
                return false;
            }
            $hemog = explode(".", $this->fields["HEMOGLOBIN"][$g]);
            //文字数チェック
            if (strlen($this->fields["NUTRITIONCD_REMARK"][$g]) > 60) {
                $this->setWarning("MSG915", $atno_name."：(栄養状態所見) 60バイトまでです。");
                return false;
            } elseif (strlen($this->fields["SPINERIBCD_REMARK"][$g]) > 60) {
                $this->setWarning("MSG915", $atno_name."：(脊柱・胸郭・四肢所見) 60バイトまでです。");
                return false;
            } elseif (strlen($this->fields["EYE_TEST_RESULT"][$g]) > 60) {
                $this->setWarning("MSG915", $atno_name."：(目の疾病及び異常所見) 60バイトまでです。");
                return false;
            } elseif (strlen($this->fields["NOSEDISEASECD_REMARK"][$g]) > 60) {
                $this->setWarning("MSG915", $atno_name."：(耳鼻咽頭疾患所見) 60バイトまでです。");
                return false;
            } elseif (strlen($this->fields["NOSEDISEASECD_REMARK1"][$g]) > 60) {
                $this->setWarning("MSG915", $atno_name."：(耳疾患所見) 60バイトまでです。");
                return false;
            } elseif (strlen($this->fields["NOSEDISEASECD_REMARK2"][$g]) > 60) {
                $this->setWarning("MSG915", $atno_name."：(鼻・副鼻腔疾患所見) 60バイトまでです。");
                return false;
            } elseif (strlen($this->fields["NOSEDISEASECD_REMARK3"][$g]) > 60) {
                $this->setWarning("MSG915", $atno_name."：(口腔咽頭疾患・異常所見) 60バイトまでです。");
                return false;
            } elseif (strlen($this->fields["SKINDISEASECD_REMARK"][$g]) > 60) {
                $this->setWarning("MSG915", $atno_name."：(皮膚疾患所見) 60バイトまでです。");
                return false;
            } elseif (strlen($this->fields["TB_FILMNO"][$g]) > $this->tb_filmnoFieldSize) {
                $this->setWarning("MSG915", $atno_name."：(結核・画像番号) ".$this->tb_filmnoFieldSize."バイトまでです。");
                return false;
            } elseif (strlen($this->fields["TB_X_RAY"][$g]) > 60) {
                $this->setWarning("MSG915", $atno_name."：(結核・所見) 60バイトまでです。");
                return false;
            } elseif (strlen($this->fields["TB_NAME_REMARK1"][$g]) > 60) {
                $this->setWarning("MSG915", $atno_name."：(結核・病名) 60バイトまでです。");
                return false;
            } elseif (strlen($this->fields["HEART_MEDEXAM_REMARK"][$g]) > 120) {
                $this->setWarning("MSG915", $atno_name."：(心臓・臨床医学的検査所見) 120バイトまでです。");
                return false;
            } elseif ($this->z010name1 == "koma" && strlen($this->fields["HEART_GRAPH_NO"][$g]) > 12) {
                $this->setWarning("MSG915", $atno_name."：(心臓・心電図NO) 12バイトまでです。");
                return false;
            } elseif (strlen($this->fields["HEARTDISEASECD_REMARK"][$g]) > 60) {
                $this->setWarning("MSG915", $atno_name."：(心臓・疾病及び異常所見) 60バイトまでです。");
                return false;
            } elseif (strlen($this->fields["MANAGEMENT_REMARK"][$g]) > 60) {
                $this->setWarning("MSG915", $atno_name."：(心臓・管理区分所見) 60バイトまでです。");
                return false;
            } elseif (strlen($this->fields["URICOTHERTEST"][$g]) > 60) {
                $this->setWarning("MSG915", $atno_name."：(尿・その他の検査) 60バイトまでです。");
                return false;
            } elseif (strlen($this->fields["OTHER_REMARK2"][$g]) > 60) {
                $this->setWarning("MSG915", $atno_name."：(その他疾病及び異常所見) 60バイトまでです。");
                return false;
            } elseif (strlen($this->fields["OTHER_REMARK"][$g]) > 60) {
                $this->setWarning("MSG915", $atno_name."：(その他疾病及び異常・指導区分) 60バイトまでです。");
                return false;
            } elseif (strlen($this->field["ANEMIA_REMARK"][$g]) > 30) {
                $this->setWarning("MSG915", $atno_name."：貧血・所見は30バイトまでです。");
                return false;
            } elseif (strlen($hemog[0]) > 2) {
                $this->setWarning("MSG901", $atno_name."：(ヘモグロビン)");
                return false;
            } elseif (strlen($hemog[1]) > 1) {
                $this->setWarning("MSG901", $atno_name."：(ヘモグロビン) 小数点第一位まで");
                return false;
            } elseif (strlen($this->fields["DOC_REMARK"][$g]) > 60) {
                $this->setWarning("MSG915", $atno_name."：(学校医所見) 60バイトまでです。");
                return false;
            } elseif (strlen($this->fields["TREAT_REMARK1"][$g]) > 60) {
                $this->setWarning("MSG915", $atno_name."：(事後措置所見) 60バイトまでです。");
                return false;
            } elseif (strlen($this->fields["REMARK"][$g]) > 300) {
                $this->setWarning("MSG915", $atno_name."：(備考) 300バイトまでです。");
                return false;
            } elseif (strlen($this->fields["DIAGNOSIS_NAME"][$g]) > 150) {
                $this->setWarning("MSG915", $atno_name."：(診断名) 150バイトまでです。");
                return false;
            }

            if (strlen($r_barevision[0]) > 0) {
                $this->fields["R_BAREVISION"][$g] = $r_barevision[0] . "." . sprintf("%-03s", $r_barevision[1]);
            }
            if (strlen($r_vision[0]) > 0) {
                $this->fields["R_VISION"][$g]     = $r_vision[0]     . "." . sprintf("%-03s", $r_vision[1]);
            }
            if (strlen($l_barevision[0]) > 0) {
                $this->fields["L_BAREVISION"][$g] = $l_barevision[0] . "." . sprintf("%-03s", $l_barevision[1]);
            }
            if (strlen($l_vision[0]) > 0) {
                $this->fields["L_VISION"][$g]     = $l_vision[0]     . "." . sprintf("%-03s", $l_vision[1]);
            }
            if (strlen($hemog[0]) > 0) {
                $this->fields["HEMOGLOBIN"][$g]   = $hemog[0]        . "." . sprintf("%-01s", $hemog[1]);
            }
        }//for
        return true;
    }

    //追加および更新
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            knjf013bQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
