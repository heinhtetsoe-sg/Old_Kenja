<?php

require_once('for_php7.php');

class knjf023jModel extends Model
{
    public $fields = array();
    public $cmd;
    public $grade_hr_class;
    public $screen;
    public $data_cnt;
    public $control;
    public $totalFields = array();

    public function knjf023jModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->cmd      = VARS::request("cmd");
        $this->data_cnt = VARS::post("DATA_CNT");

        //更新中、サブミットする項目使用不可
        //よって、サブミットする項目の値は、hiddenから取得
        if ($this->cmd == 'update') {
            $this->grade_hr_class   = VARS::post("H_GRADE_HR_CLASS");
            $this->screen           = VARS::post("H_SCREEN");
        } else {
            $this->grade_hr_class   = VARS::request("GRADE_HR_CLASS");
            $this->screen           = VARS::request("SCREEN");
        }

        //歯数リスト
        $this->cntItem = array("BABYTOOTH"         => "乳歯・現在数",
                               "REMAINBABYTOOTH"   => "乳歯・未処置数",
                               "TREATEDBABYTOOTH"  => "乳歯・処置数",
                               "BRACK_BABYTOOTH"   => "乳歯・要注意乳歯数",
                               "ADULTTOOTH"        => "永久歯・現在数",
                               "REMAINADULTTOOTH"  => "永久歯・未処置数",
                               "TREATEDADULTTOOTH" => "永久歯・処置数",
                               "LOSTADULTTOOTH"    => "永久歯・喪失数",
                               "BRACK_ADULTTOOTH"  => "永久歯・要観察歯数");

        //歯式リスト
        $this->shisiki = array("UP_R_BABY5"  => array("乳歯-上-右E",   "BABY"),
                               "UP_R_BABY4"  => array("乳歯-上-右D",   "BABY"),
                               "UP_R_BABY3"  => array("乳歯-上-右C",   "BABY"),
                               "UP_R_BABY2"  => array("乳歯-上-右B",   "BABY"),
                               "UP_R_BABY1"  => array("乳歯-上-右A",   "BABY"),
                               "UP_L_BABY1"  => array("乳歯-上-左A",   "BABY"),
                               "UP_L_BABY2"  => array("乳歯-上-左B",   "BABY"),
                               "UP_L_BABY3"  => array("乳歯-上-左C",   "BABY"),
                               "UP_L_BABY4"  => array("乳歯-上-左D",   "BABY"),
                               "UP_L_BABY5"  => array("乳歯-上-左E",   "BABY"),
                               "LW_R_BABY5"  => array("乳歯-下-右E",   "BABY"),
                               "LW_R_BABY4"  => array("乳歯-下-右D",   "BABY"),
                               "LW_R_BABY3"  => array("乳歯-下-右C",   "BABY"),
                               "LW_R_BABY2"  => array("乳歯-下-右B",   "BABY"),
                               "LW_R_BABY1"  => array("乳歯-下-右A",   "BABY"),
                               "LW_L_BABY1"  => array("乳歯-下-左A",   "BABY"),
                               "LW_L_BABY2"  => array("乳歯-下-左B",   "BABY"),
                               "LW_L_BABY3"  => array("乳歯-下-左C",   "BABY"),
                               "LW_L_BABY4"  => array("乳歯-下-左D",   "BABY"),
                               "LW_L_BABY5"  => array("乳歯-下-左E",   "BABY"),
                               "UP_R_ADULT8" => array("永久歯-上-右8", "ADULT"),
                               "UP_R_ADULT7" => array("永久歯-上-右7", "ADULT"),
                               "UP_R_ADULT6" => array("永久歯-上-右6", "ADULT"),
                               "UP_R_ADULT5" => array("永久歯-上-右5", "ADULT"),
                               "UP_R_ADULT4" => array("永久歯-上-右4", "ADULT"),
                               "UP_R_ADULT3" => array("永久歯-上-右3", "ADULT"),
                               "UP_R_ADULT2" => array("永久歯-上-右2", "ADULT"),
                               "UP_R_ADULT1" => array("永久歯-上-右1", "ADULT"),
                               "UP_L_ADULT1" => array("永久歯-上-左1", "ADULT"),
                               "UP_L_ADULT2" => array("永久歯-上-左2", "ADULT"),
                               "UP_L_ADULT3" => array("永久歯-上-左3", "ADULT"),
                               "UP_L_ADULT4" => array("永久歯-上-左4", "ADULT"),
                               "UP_L_ADULT5" => array("永久歯-上-左5", "ADULT"),
                               "UP_L_ADULT6" => array("永久歯-上-左6", "ADULT"),
                               "UP_L_ADULT7" => array("永久歯-上-左7", "ADULT"),
                               "UP_L_ADULT8" => array("永久歯-上-左8", "ADULT"),
                               "LW_R_ADULT8" => array("永久歯-下-右8", "ADULT"),
                               "LW_R_ADULT7" => array("永久歯-下-右7", "ADULT"),
                               "LW_R_ADULT6" => array("永久歯-下-右6", "ADULT"),
                               "LW_R_ADULT5" => array("永久歯-下-右5", "ADULT"),
                               "LW_R_ADULT4" => array("永久歯-下-右4", "ADULT"),
                               "LW_R_ADULT3" => array("永久歯-下-右3", "ADULT"),
                               "LW_R_ADULT2" => array("永久歯-下-右2", "ADULT"),
                               "LW_R_ADULT1" => array("永久歯-下-右1", "ADULT"),
                               "LW_L_ADULT1" => array("永久歯-下-左1", "ADULT"),
                               "LW_L_ADULT2" => array("永久歯-下-左2", "ADULT"),
                               "LW_L_ADULT3" => array("永久歯-下-左3", "ADULT"),
                               "LW_L_ADULT4" => array("永久歯-下-左4", "ADULT"),
                               "LW_L_ADULT5" => array("永久歯-下-左5", "ADULT"),
                               "LW_L_ADULT6" => array("永久歯-下-左6", "ADULT"),
                               "LW_L_ADULT7" => array("永久歯-下-左7", "ADULT"),
                               "LW_L_ADULT8" => array("永久歯-下-左8", "ADULT"));

        //初期化
        $this->fields = array();
        $this->inputFlg = array();

        if ($this->cmd == 'update') {
            for ($g = 0; $g < $this->data_cnt; $g++) {
                $this->fields["SCHREGNO"][]          = VARS::post("SCHREGNO"."-".$g);
                $this->fields["ATTENDNO"][]          = VARS::post("ATTENDNO"."-".$g);
                $this->fields["NAME_SHOW"][]         = VARS::post("NAME_SHOW"."-".$g);
                $this->fields["TOOTH_DATE"][]        = VARS::post("TOOTH_DATE"."-".$g);
                $this->fields["JAWS_JOINTCD"][]      = VARS::post("JAWS_JOINTCD"."-".$g);
                $this->fields["JAWS_JOINTCD2"][]     = VARS::post("JAWS_JOINTCD2"."-".$g);
                $this->fields["PLAQUECD"][]          = VARS::post("PLAQUECD"."-".$g);
                $this->fields["GUMCD"][]             = VARS::post("GUMCD"."-".$g);
                $this->fields["CALCULUS"][]          = VARS::post("CALCULUS"."-".$g);
                $this->fields["ORTHODONTICS"][]      = VARS::post("ORTHODONTICS"."-".$g);
                $this->fields["BABYTOOTH"][]         = VARS::post("BABYTOOTH"."-".$g);
                $this->fields["REMAINBABYTOOTH"][]   = VARS::post("REMAINBABYTOOTH"."-".$g);
                $this->fields["TREATEDBABYTOOTH"][]  = VARS::post("TREATEDBABYTOOTH"."-".$g);
                $this->fields["BRACK_BABYTOOTH"][]   = VARS::post("BRACK_BABYTOOTH"."-".$g);
                $this->fields["ADULTTOOTH"][]        = VARS::post("ADULTTOOTH"."-".$g);
                $this->fields["REMAINADULTTOOTH"][]  = VARS::post("REMAINADULTTOOTH"."-".$g);
                $this->fields["TREATEDADULTTOOTH"][] = VARS::post("TREATEDADULTTOOTH"."-".$g);
                $this->fields["LOSTADULTTOOTH"][]    = VARS::post("LOSTADULTTOOTH"."-".$g);
                $this->fields["BRACK_ADULTTOOTH"][]  = VARS::post("BRACK_ADULTTOOTH"."-".$g);
                $this->fields["OTHERDISEASECD"][]    = VARS::post("OTHERDISEASECD"."-".$g);
                $this->fields["OTHERDISEASE"][]      = VARS::post("OTHERDISEASE"."-".$g);
                $this->fields["OTHERDISEASECD2"][]   = VARS::post("OTHERDISEASECD2"."-".$g);
                $this->fields["OTHERDISEASE2"][]     = VARS::post("OTHERDISEASE2"."-".$g);
                $this->fields["DENTISTREMARKCD"][]   = VARS::post("DENTISTREMARKCD"."-".$g);
                $this->fields["DENTISTREMARKCD2"][]  = VARS::post("DENTISTREMARKCD2"."-".$g);
                $this->fields["DENTISTREMARK2"][]    = VARS::post("DENTISTREMARK2"."-".$g);
                $this->fields["DENTISTREMARKCD3"][]  = VARS::post("DENTISTREMARKCD3"."-".$g);
                $this->fields["DENTISTREMARK3"][]    = VARS::post("DENTISTREMARK3"."-".$g);
                $this->fields["DENTISTREMARK"][]     = VARS::post("DENTISTREMARK"."-".$g);
                $this->fields["DENTISTREMARKDATE"][] = VARS::post("DENTISTREMARKDATE"."-".$g);
                $this->fields["DENTISTTREATCD"][]    = VARS::post("DENTISTTREATCD"."-".$g);
                $this->fields["DENTISTTREAT"][]      = VARS::post("DENTISTTREAT"."-".$g);
                $this->fields["DENTISTTREATCD2"][]   = VARS::post("DENTISTTREATCD2"."-".$g);
                $this->fields["DENTISTTREAT2"][]     = VARS::post("DENTISTTREAT2"."-".$g);

                foreach ($this->shisiki as $key => $val) {
                    $this->fields[$key][] =  VARS::post($key."-".$g);
                }
            }

            for ($g = 0; $g < $this->data_cnt; $g++) {
                $this->inputFlg[$g] = "NG";
                foreach ($this->fields as $key => $val) {
                    if (!in_array($key, array("SCHREGNO","ATTENDNO","NAME_SHOW"))) {
                        if (strlen($val[$g])) {
                            $this->inputFlg[$g] = "OK";
                        }
                    }
                }
            }
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $db = Query::dbCheckOut();
    }

    //入力項目チェック
    public function validateInput()
    {
        //初期化
        $this->totalFields = array();

        for ($g = 0; $g < $this->data_cnt; $g++) {
            //データを入力されている時は、健康診断実施日のnullチェックをする。
            if ($this->inputFlg[$g] == "OK" && !strlen($this->fields["TOOTH_DATE"][$g])) {
                $atno_name = $this->fields["ATTENDNO"][$g].'番 '.$this->fields["NAME_SHOW"][$g];
                $this->setWarning("MSG301", $atno_name."：(健康診断実施日)");
                return false;
            }

            $tooth = "";
            if ($this->screen == "1") {
                //文字数チェック
                foreach ($this->cntItem as $key => $val) {
                    if (strlen($this->fields[$key][$g]) > 2) {
                        $atno_name = $this->fields["ATTENDNO"][$g].'番 '.$this->fields["NAME_SHOW"][$g];
                        $this->setWarning("MSG915", $atno_name."：({$val}) 半角2文字までです。");
                        return false;
                    }
                    $tooth .= $this->fields[$key][$g];
                }
                if ($this->validate_row_cnt($this->fields["OTHERDISEASE"][$g], (20 * 2)) > 1) {
                    $atno_name = $this->fields["ATTENDNO"][$g].'番 '.$this->fields["NAME_SHOW"][$g];
                    $this->setWarning("MSG915", $atno_name."：(その他疾病及び異常) 全角20文字までです。");
                    return false;
                }
                if ($this->validate_row_cnt($this->fields["OTHERDISEASE2"][$g], (20 * 2)) > 1) {
                    $atno_name = $this->fields["ATTENDNO"][$g].'番 '.$this->fields["NAME_SHOW"][$g];
                    $this->setWarning("MSG915", $atno_name."：(口腔の疾病及び異常) 全角20文字までです。");
                    return false;
                }
                if ($this->validate_row_cnt($this->fields["DENTISTREMARK"][$g], (20 * 2)) > 1) {
                    $atno_name = $this->fields["ATTENDNO"][$g].'番 '.$this->fields["NAME_SHOW"][$g];
                    $this->setWarning("MSG915", $atno_name."：(学校歯科医所見1) 全角20文字までです。");
                    return false;
                }
                if ($this->validate_row_cnt($this->fields["DENTISTREMARK2"][$g], (20 * 2)) > 1) {
                    $atno_name = $this->fields["ATTENDNO"][$g].'番 '.$this->fields["NAME_SHOW"][$g];
                    $this->setWarning("MSG915", $atno_name."：(学校歯科医所見2) 全角20文字までです。");
                    return false;
                }
                if ($this->validate_row_cnt($this->fields["DENTISTREMARK3"][$g], (20 * 2)) > 1) {
                    $atno_name = $this->fields["ATTENDNO"][$g].'番 '.$this->fields["NAME_SHOW"][$g];
                    $this->setWarning("MSG915", $atno_name."：(学校歯科医所見3) 全角20文字までです。");
                    return false;
                }
                if ($this->validate_row_cnt($this->fields["DENTISTTREAT"][$g], (20 * 2)) > 1) {
                    $atno_name = $this->fields["ATTENDNO"][$g].'番 '.$this->fields["NAME_SHOW"][$g];
                    $this->setWarning("MSG915", $atno_name."：(学校歯科医事後措置1) 全角20文字までです。");
                    return false;
                }
                if ($this->validate_row_cnt($this->fields["DENTISTTREAT2"][$g], (20 * 2)) > 1) {
                    $atno_name = $this->fields["ATTENDNO"][$g].'番 '.$this->fields["NAME_SHOW"][$g];
                    $this->setWarning("MSG915", $atno_name."：(学校歯科医事後措置2) 全角20文字までです。");
                    return false;
                }

                //歯数が1つでも入力されていたら、NULL項目に"0"をセット
                if ($tooth != "") {
                    foreach ($this->cntItem as $key => $val) {
                        if ($this->fields[$key][$g] == "") {
                            $this->fields[$key][$g] = 0;
                        }
                    }
                }
            } else {
                //歯数カウント
                foreach ($this->shisiki as $key => $val) {
                    //現在数
                    if ($this->fields[$key][$g] && $this->fields[$key][$g] != "04") {
                        $this->totalFields[$val[1]."TOOTH"][$g] += 1;
                    }
                    //未処置数
                    if ($this->fields[$key][$g] == "02") {
                        $this->totalFields["REMAIN".$val[1]."TOOTH"][$g] += 1;
                    }
                    //処置数
                    if ($this->fields[$key][$g] == "03") {
                        $this->totalFields["TREATED".$val[1]."TOOTH"][$g] += 1;
                    }
                    //喪失数
                    if ($this->fields[$key][$g] == "04" && $val[1] == "ADULT") {
                        $this->totalFields["LOST".$val[1]."TOOTH"][$g] += 1;
                    }
                    //要注意乳歯数
                    if ($this->fields[$key][$g] == "05" && $val[1] == "BABY") {
                        $this->totalFields["BRACK_".$val[1]."TOOTH"][$g] += 1;
                    }
                    //要観察歯数
                    if ($this->fields[$key][$g] == "06" && $val[1] == "ADULT") {
                        $this->totalFields["BRACK_".$val[1]."TOOTH"][$g] += 1;
                    }

                    $tooth .= $this->fields[$key][$g];
                }

                //歯数が1つでも入力されていたら、NULL項目に"0"をセット
                if ($tooth != "") {
                    foreach ($this->cntItem as $key => $val) {
                        if ($this->totalFields[$key][$g] == "") {
                            $this->totalFields[$key][$g] = 0;
                        }
                    }
                }
            }
        }

        return true;
    }

    //更新処理
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            knjf023jQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
