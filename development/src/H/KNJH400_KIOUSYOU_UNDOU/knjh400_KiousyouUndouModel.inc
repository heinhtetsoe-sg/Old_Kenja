<?php

require_once('for_php7.php');
class knjh400_KiousyouUndouModel extends Model
{
    public $cmd;
    public $schregno = "";
    public $name = "";
    public $grade;
    public $hr_class;
    public $attendno;
    public $field = array();

    public function knjh400_KiousyouUndouModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->hr_class);
            unset($this->attendno);
        }

        $this->field = array("CARE_FLG01"           =>  VARS::post("CARE_FLG01"),
                             "CARE_FLG02"           =>  VARS::post("CARE_FLG02"),
                             "CARE_FLG03"           =>  VARS::post("CARE_FLG03"),
                             "CARE_FLG04"           =>  VARS::post("CARE_FLG04"),
                             "CARE_FLG05"           =>  VARS::post("CARE_FLG05"),
                             "CARE_FLG06"           =>  VARS::post("CARE_FLG06"),
                             "MEDICAL_HISTORY1"     =>  VARS::post("MEDICAL_HISTORY1"),
                             "MEDICAL_HISTORY2"     =>  VARS::post("MEDICAL_HISTORY2"),
                             "MEDICAL_HISTORY3"     =>  VARS::post("MEDICAL_HISTORY3"),
                             "DIAGNOSIS_NAME"       =>  VARS::post("DIAGNOSIS_NAME"),
                             "GUIDE_DIV"            =>  VARS::post("GUIDE_DIV"),
                             "JOINING_SPORTS_CLUB"  =>  VARS::post("JOINING_SPORTS_CLUB"),
                             "EMERGENCYNAME"        =>  VARS::post("EMERGENCYNAME"),
                             "EMERGENCYTELNO"       =>  VARS::post("EMERGENCYTELNO"),
                             "EMERGENCYNAME2"       =>  VARS::post("EMERGENCYNAME2"),
                             "EMERGENCYTELNO2"      =>  VARS::post("EMERGENCYTELNO2"),
                             "DATE"                 =>  VARS::post("DATE"),
                             "DOCTOR"               =>  VARS::post("DOCTOR"),
                             "HOSPITAL"             =>  VARS::post("HOSPITAL"),
                             "REMARK"               =>  VARS::post("REMARK"),
                             "YEAR"                 =>  VARS::post("YEAR"),
                             "DBNAME"               =>  VARS::post("DBNAME")
                            );

        //種別
        for ($i=1; $i <= 2; $i++) {
            //項目
            for ($j=1; $j <= 6; $j++) {
                //SEQ
                for ($k=0; $k <= 12; $k++) {
                    $cd = sprintf("%02d", $i)."_".sprintf("%02d", $j)."_".sprintf("%02d", $k);

                    $this->field["CHECK".$cd]   =  VARS::post("CHECK".$cd);
                    $this->field["TEXT".$cd]    =  VARS::post("TEXT".$cd);

                    if ($i.'_'.$j == '1_3' || $i.'_'.$j.'_'.$k == '1_1_02') {
                        $this->field["DETAIL".$cd]    =  VARS::post("DETAIL".$cd);

                        for ($l=1; $l <= 4; $l++) {
                            $this->field["REASON".sprintf("%02d", $k)."_".$l]   =  VARS::post("REASON".sprintf("%02d", $k)."_".$l);
                        }
                    }
                }
            }
        }


        $this->cmd = VARS::request("cmd");

        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME") != "") {
            $this->name = VARS::get("NAME");
        }
        if (VARS::get("GRADE") != "") {
            $this->grade = VARS::get("GRADE");
        }
        if (VARS::get("HR_CLASS") != "") {
            $this->hr_class = VARS::get("HR_CLASS");
        }
        if (VARS::get("ATTENDNO") != "") {
            $this->attendno = VARS::get("ATTENDNO");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->hr_class);
            unset($this->attendno);
        }

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    public function validateInput($cmd)
    {
        //必須チェック
        if (!isset($this->schregno)) {
            $this->setWarning("MSG308");
            return false;
        }

        //対象年度チェック（処理年度のみ更新可）
        if ($this->field["YEAR"] != CTRL_YEAR) {
            $this->setWarning("MSG300", "更新は".CTRL_YEAR."年度のみ可能です。");
            return false;
        }

        //文字数チェック項目一覧
        $txt_array = $sonota = array();
        $txt_array["subform1"]["TEXT01_02_04"]      = array("gyo" => 1,  "moji" => 20, "title" => "その他");
        $txt_array["subform1"]["TEXT01_03_04"]      = array("gyo" => 1,  "moji" => 20, "title" => "その他");
        $txt_array["subform1"]["TEXT01_05_00"]      = array("gyo" => 15, "moji" => 20, "title" => "急性発作時の対応");
        $txt_array["subform1"]["TEXT02_02_03"]      = array("gyo" => 1,  "moji" => 40, "title" => "動物名");
        $txt_array["subform1"]["TEXT02_04_00"]      = array("gyo" => 7,  "moji" => 40, "title" => "その他の配慮・管理事項");

        $txt_array["subform2"]["TEXT01_02_04"]      = array("gyo" => 1,  "moji" => 20, "title" => "その他");
        $txt_array["subform2"]["TEXT01_03_02"]      = array("gyo" => 1,  "moji" => 40, "title" => "その他");
        $txt_array["subform2"]["TEXT02_02_03"]      = array("gyo" => 1,  "moji" => 40, "title" => "動物名");
        $txt_array["subform2"]["TEXT02_04_00"]      = array("gyo" => 7,  "moji" => 20, "title" => "その他の配慮・管理事項");

        $txt_array["subform3"]["TEXT01_01_05"]      = array("gyo" => 1,  "moji" => 20, "title" => "その他");
        $txt_array["subform3"]["TEXT01_02_04"]      = array("gyo" => 1,  "moji" => 20, "title" => "その他");
        $txt_array["subform3"]["TEXT02_03_00"]      = array("gyo" => 2,  "moji" => 40, "title" => "その他の配慮・管理事項");

        $txt_array["subform4"]["TEXT01_02_01"]      = array("gyo" => 1,  "moji" => 20, "title" => "原因");
        $txt_array["subform4"]["TEXT01_02_06"]      = array("gyo" => 1,  "moji" => 20, "title" => "その他");
        $txt_array["subform4"]["TEXT01_04_03"]      = array("gyo" => 1,  "moji" => 20, "title" => "その他");
        $txt_array["subform4"]["TEXT01_04_03"]      = array("gyo" => 1,  "moji" => 20, "title" => "内服薬");
        $txt_array["subform4"]["TEXT01_04_03"]      = array("gyo" => 1,  "moji" => 20, "title" => "エピペン");
        $txt_array["subform4"]["TEXT02_01_02"]      = array("gyo" => 2,  "moji" => 40, "title" => "保護者と相談し決定");
        $txt_array["subform4"]["TEXT02_02_02"]      = array("gyo" => 2,  "moji" => 40, "title" => "保護者と相談し決定");
        $txt_array["subform4"]["TEXT02_03_02"]      = array("gyo" => 2,  "moji" => 40, "title" => "保護者と相談し決定");
        $txt_array["subform4"]["TEXT02_04_02"]      = array("gyo" => 2,  "moji" => 40, "title" => "保護者と相談し決定");
        $txt_array["subform4"]["TEXT02_05_00"]      = array("gyo" => 7,  "moji" => 25, "title" => "医師記入欄");
        $txt_array["subform4"]["TEXT02_06_00"]      = array("gyo" => 7,  "moji" => 40, "title" => "診断のきっかけ");
        $txt_array["subform4"]["DETAIL01_03_06"]    = array("gyo" => 1,  "moji" => 20, "title" => "種実類・木の実類");
        $txt_array["subform4"]["DETAIL01_03_07"]    = array("gyo" => 1,  "moji" => 20, "title" => "甲殻類");
        $txt_array["subform4"]["DETAIL01_03_08"]    = array("gyo" => 1,  "moji" => 20, "title" => "果物類");
        $txt_array["subform4"]["DETAIL01_03_09"]    = array("gyo" => 1,  "moji" => 20, "title" => "魚類");
        $txt_array["subform4"]["DETAIL01_03_10"]    = array("gyo" => 1,  "moji" => 20, "title" => "肉類");
        $txt_array["subform4"]["DETAIL01_03_11"]    = array("gyo" => 1,  "moji" => 20, "title" => "その他１");
        $txt_array["subform4"]["DETAIL01_03_12"]    = array("gyo" => 1,  "moji" => 20, "title" => "その他２");

        $txt_array["subform6"]["TEXT01_01_02"]      = array("gyo" => 1,  "moji" => 20, "title" => "主な症状の時期");
        $txt_array["subform6"]["TEXT01_02_03"]      = array("gyo" => 1,  "moji" => 20, "title" => "その他");
        $txt_array["subform6"]["TEXT02_02_00"]      = array("gyo" => 7,  "moji" => 25, "title" => "その他の配慮・管理事項");

        $sonota["EMERGENCYNAME"]        = array("gyo" => 1,  "moji" => 10, "title" => "（緊急時連絡先）保護者氏名");
        $sonota["EMERGENCYTELNO"]       = array("gyo" => 0,  "moji" => 14, "title" => "（緊急時連絡先）保護者電話");
        $sonota["EMERGENCYNAME2"]       = array("gyo" => 1,  "moji" => 40, "title" => "（緊急時連絡先）医療機関名");
        $sonota["EMERGENCYTELNO2"]      = array("gyo" => 0,  "moji" => 14, "title" => "（緊急時連絡先）医療機関電話");
        $sonota["DOCTOR"]               = array("gyo" => 1,  "moji" => 10, "title" => "医師名");
        $sonota["HOSPITAL"]             = array("gyo" => 1,  "moji" => 40, "title" => "医療機関名");
        $sonota["REMARK"]               = array("gyo" => 17, "moji" => 40, "title" => "緊急時対応プラン");

        //文字数チェック
        foreach ($txt_array[$cmd] as $key => $val) {
            if ($this->validate_row_cnt($this->field[$key], ($val["moji"] * 2)) > $val["gyo"]) {
                if ($val["gyo"] == 1) {
                    $this->setWarning("MSG915", "({$val["title"]}は{$val["moji"]}文字までです)");
                } else {
                    $this->setWarning("MSG915", "({$val["title"]}は{$val["gyo"]}行までです)");
                }
                return false;
            }
        }

        //文字数チェック
        foreach ($sonota as $key => $val) {
            if ($val["gyo"] == 0) {
                if (strlen($this->field[$key]) > $val["moji"]) {
                    $this->setWarning("MSG915", "({$val["title"]}は{$val["moji"]}バイトまでです)");
                    return false;
                }
            } else {
                if ($this->validate_row_cnt($this->field[$key], ($val["moji"] * 2)) > $val["gyo"]) {
                    if ($val["gyo"] == 1) {
                        $this->setWarning("MSG915", "({$val["title"]}は{$val["moji"]}文字までです)");
                    } else {
                        $this->setWarning("MSG915", "({$val["title"]}は{$val["gyo"]}行までです)");
                    }
                    return false;
                }
            }
        }

        return true;
    }
}
