<?php

require_once('for_php7.php');

class knji030Model extends Model
{
    public $field      = array();
    public $cmd;
    public $schregno;

    public function knji030Model()
    {
        $this->Change_flag == "off";
    }
    public function init()
    {
        //生徒情報
        $this->field = array("SCHREGNO"              =>  VARS::post("SCHREGNO"),                //学籍番号
                             "OLD_NAME"              =>  VARS::post("OLD_NAME"),                //旧氏名
                             "OLD_NAME_SHOW"         =>  VARS::post("OLD_NAME_SHOW"),           //旧氏名表示名
                             "OLD_NAME_KANA"         =>  VARS::post("OLD_NAME_KANA"),           //旧氏名かな
                             "OLD_NAME_ENG"          =>  VARS::post("OLD_NAME_ENG"),            //旧氏名英字
                             "NAME"                  =>  VARS::post("NAME"),                    //現氏名
                             "NAME_SHOW"             =>  VARS::post("NAME_SHOW"),               //現氏名表示名
                             "NAME_KANA"             =>  VARS::post("NAME_KANA"),               //現氏名かな
                             "NAME_ENG"              =>  VARS::post("NAME_ENG"),                //現氏名英字
                             "CUR_ZIPCD"             =>  VARS::post("CUR_ZIPCD"),               //郵便コード
                             "CUR_AREACD"            =>  VARS::post("CUR_AREACD"),              //地区コード
                             "CUR_ADDR1"             =>  VARS::post("CUR_ADDR1"),               //住所1
                             "CUR_ADDR2"             =>  VARS::post("CUR_ADDR2"),               //住所2
                             "CUR_ADDR_FLG"          =>  VARS::post("CUR_ADDR_FLG"),            //住所フラグ
                             "UNKNOWN_ADDR_FLG"      =>  VARS::post("UNKNOWN_ADDR_FLG"),        //住所不明フラグ
                             "CUR_ADDR1_ENG"         =>  VARS::post("CUR_ADDR1_ENG"),           //(英字)住所1
                             "CUR_ADDR2_ENG"         =>  VARS::post("CUR_ADDR2_ENG"),           //(英字)住所2
                             "CUR_TELNO"             =>  VARS::post("CUR_TELNO"),               //電話番号
                             "CUR_FAXNO"             =>  VARS::post("CUR_FAXNO"),               //FAX番号
                             "CUR_EMAIL"             =>  VARS::post("CUR_EMAIL"),               //E-mail
                             "CUR_EMERGENCYCALL"     =>  VARS::post("CUR_EMERGENCYCALL"),       //緊急連絡先
                             "CUR_EMERGENCYTELNO"    =>  VARS::post("CUR_EMERGENCYTELNO"),      //緊急連絡番号
                             "ZIPCD"                 =>  VARS::post("ZIPCD"),                   //(実家)郵便コード
                             "AREACD"                =>  VARS::post("AREACD"),                  //(実家)地区コード
                             "ADDR1"                 =>  VARS::post("ADDR1"),                   //(実家)住所1
                             "ADDR2"                 =>  VARS::post("ADDR2"),                   //(実家)住所2
                             "TELNO"                 =>  VARS::post("TELNO"),                   //(実家)電話番号
                             "FAXNO"                 =>  VARS::post("FAXNO"),                   //(実家)FAX番号
                             "REMARK"                =>  VARS::post("REMARK")                   //備考
                        );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::get("SCHREGNO")) {
            $this->schregno = VARS::get("SCHREGNO");
        } else {
            $this->schregno = VARS::post("SCHREGNO");
        }
    }
   
    //入力項目チェック
    public function validateInput()
    {
        if (trim($this->field["SCHREGNO"]) == "") {
            $this->setWarning(MZ0049, "(学籍番号)");
            return false;
        }
        if ($this->Properties["chkNameInputHankaku"] == "1") {
            //現氏名で、半角文字列が混ざっているか、チェック
            if (!$this->chkHankaku($this->field["NAME"])) {
                $this->setWarning(MSG901, "(現氏名) 半角文字が入力されています。");
                return false;
            }
        }
        if ($this->Properties["chkAddrInputHankaku"] == "1") {
            if (!$this->chkHankaku($this->field["CUR_ADDR1"])) {
                $this->setWarning(MSG901, "(住所1) 半角文字が入力されています。");
                return false;
            }
            if (!$this->chkHankaku($this->field["CUR_ADDR2"])) {
                $this->setWarning(MSG901, "(住所2) 半角文字が入力されています。");
                return false;
            }
            if (!$this->chkHankaku($this->field["ADDR1"])) {
                $this->setWarning(MSG901, "((実家)住所1) 半角文字が入力されています。");
                return false;
            }
            if (!$this->chkHankaku($this->field["ADDR2"])) {
                $this->setWarning(MSG901, "((実家)住所2) 半角文字が入力されています。");
                return false;
            }
        }
        $nameSLen = $this->Properties["NAME_INPUT_SIZE"] ? $this->Properties["NAME_INPUT_SIZE"] : 40;
        $addrSLen = $this->Properties["ADDR_INPUT_SIZE"] ? $this->Properties["ADDR_INPUT_SIZE"] : 30;
        if ($this->Properties["NAME_INPUT_SIZE"] != "" && mb_strlen($this->field["NAME"], 'UTF-8') > $nameSLen) {
            $this->setWarning(MSG914, "(現氏名) 全角{$nameSLen}文字を超えています。");
            return false;
        }
        if ($this->Properties["NAME_INPUT_SIZE"] == "" && strlen($this->field["NAME"]) > intval($nameSLen) * 3) {
            $this->setWarning(MSG914, "(現氏名)");
            return false;
        }
        if (strlen($this->field["NAME_SHOW"]) > 30) {
            $this->setWarning("MSG914", "(現氏名表示用)");
            return false;
        }
        if (strlen($this->field["NAME_KANA"]) > 240) {
            $this->setWarning("MSG914", "(現氏名かな)");
            return false;
        }
        if (strlen($this->field["NAME_ENG"]) > 40) {
            $this->setWarning("MSG914", "(現英字氏名)");
            return false;
        }
        if ($this->Properties["ADDR_INPUT_SIZE"] != "" && mb_strlen($this->field["CUR_ADDR1"], 'UTF-8') > $addrSLen) {
            $this->setWarning("MSG914", "(住所1) 全角{$addrSLen}文字を超えています。");
            return false;
        } elseif ($this->Properties["ADDR_INPUT_SIZE"] == "" && strlen($this->field["CUR_ADDR1"]) > intval($addrSLen) * 3) {
            $this->setWarning("MSG914", "(住所1)");
            return false;
        } elseif ($this->Properties["ADDR_INPUT_SIZE"] != "" && mb_strlen($this->field["CUR_ADDR2"], 'UTF-8') > $addrSLen) {
            $this->setWarning("MSG914", "(住所2) 全角{$addrSLen}文字を超えています。");
            return false;
        } elseif ($this->Properties["ADDR_INPUT_SIZE"] == "" && strlen($this->field["CUR_ADDR2"]) > intval($addrSLen) * 3) {
            $this->setWarning("MSG914", "(住所2)");
            return false;
        } elseif (strlen($this->field["CUR_ADDR2_ENG"]) > 70) {
            $this->setWarning("MSG914", "((英字)住所1)");
            return false;
        } elseif (strlen($this->field["CUR_ADDR2_ENG"]) > 70) {
            $this->setWarning("MSG914", "((英字)住所2)");
            return false;
        } elseif (strlen($this->field["CUR_TELNO"]) > 14) {
            $this->setWarning("MSG914", "(電話番号)");
            return false;
        } elseif (strlen($this->field["CUR_FAXNO"]) > 14) {
            $this->setWarning("MSG914", "(FAX番号)");
            return false;
        } elseif (strlen($this->field["CUR_EMAILNO"]) > 20) {
            $this->setWarning("MSG914", "(EMAIL番号)");
            return false;
        } elseif (strlen($this->field["CUR_EMERGENCYCALL"]) > 60) {
            $this->setWarning("MSG914", "(緊急連絡先)");
            return false;
        } elseif (strlen($this->field["CUR_EMERGENCYTELNO"]) > 14) {
            $this->setWarning("MSG914", "(緊急連絡番号)");
            return false;
        } elseif ($this->Properties["ADDR_INPUT_SIZE"] != "" && mb_strlen($this->field["ADDR1"], 'UTF-8') > $addrSLen) {
            $this->setWarning("MSG914", "((実家)住所1) 全角{$addrSLen}文字を超えています。");
            return false;
        } elseif ($this->Properties["ADDR_INPUT_SIZE"] == "" && strlen($this->field["ADDR1"]) > intval($addrSLen) * 3) {
            $this->setWarning("MSG914", "((実家)住所1)");
            return false;
        } elseif ($this->Properties["ADDR_INPUT_SIZE"] != "" && mb_strlen($this->field["ADDR2"], 'UTF-8') > $addrSLen) {
            $this->setWarning("MSG914", "((実家)住所2) 全角{$addrSLen}文字を超えています。");
            return false;
        } elseif ($this->Properties["ADDR_INPUT_SIZE"] == "" && strlen($this->field["ADDR2"]) > intval($addrSLen) * 3) {
            $this->setWarning("MSG914", "((実家)住所2)");
            return false;
        } elseif (strlen($this->field["TELNO"]) > 14) {
            $this->setWarning("MSG914", "((実家)電話番号)");
            return false;
        } elseif (strlen($this->field["FAXNO"]) > 14) {
            $this->setWarning("MSG914", "((実家)FAX番号)");
            return false;
        } elseif (strlen($this->field["REMARK"]) > 120) {
            $this->setWarning("MSG914", "(備考)");
            return false;
        }
        return true;
    }
    //更新
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
        }
        return;
    }

    public function &update()
    {
        $result = knji030Query::getUpdateQuery($this->field);

        if (!isset($model->warning)) {
            $this->setMessage('MSG201');
        }
        return $result;
    }

    //値変更時のロック解除
    public function lockOff()
    {
        $this->Change_flag = "off";
        return;
    }

    public function chkHankaku($chkStr)
    {
        foreach (preg_split('//u', $chkStr, -1, PREG_SPLIT_NO_EMPTY) as $idx => $v) {
            if (strlen($v) == 1) {
                return false;
            }
        }
        return true;
    }
}
