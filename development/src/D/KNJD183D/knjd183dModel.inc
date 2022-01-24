<?php

require_once('for_php7.php');

class knjd183dModel extends Model
{
    public $cmd;
    public $exp_year;
    public $exp_semester;
    public $schregno;
    public $name;
    public $field = array();
    public $control;
    public $detail;
    public $hid_recordnamelist;
    public $hid__recactvidlist;
    public $record = array();
    public $idlist = array();

    public function knjd183dModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->exp_grade);
        }

        $this->field = array("SEMESTER"            => VARS::post("SEMESTER"),
                             "SEIKIN_CHECK"        => VARS::post("SEIKIN_CHECK"),
                             "SHUTOKUSIKAKU"       => VARS::post("SHUTOKUSIKAKU"),
                             "COMMUNICATION"       => VARS::post("COMMUNICATION"),
                             "ZIRITUKATUDO"        => VARS::post("ZIRITUKATUDO"),
                             "UPDATED"             => VARS::post("UPDATED"),
                             "SHUTOKUSIKAKU_GYOU"  => VARS::post("SHUTOKUSIKAKU_GYOU"),
                             "SHUTOKUSIKAKU_MOJI"  => VARS::post("SHUTOKUSIKAKU_MOJI"),
        );

        $this->cmd = VARS::request("cmd");

        if (VARS::get("EXP_YEAR") != "") {
            $this->exp_year     = VARS::get("EXP_YEAR");
            $this->exp_semester = VARS::get("EXP_SEMESTER");
            $this->exp_grade = VARS::get("GRADE");
        }
        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME") != "") {
            $this->name = VARS::get("NAME");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->exp_grade);
        }

        $this->hid__recactvidlist = VARS::post("HID_RECACTVIDLIST");
        $this->hid_recordnamelist = VARS::post("HID_RECACTVNAMELIST");
        if ($this->hid_recordnamelist) {
            $this->idlist = explode(",", $this->hid_recordnamelist);
            //生徒の記録
            $this->record["RECORD"] = array();
            for ($i = 0; $i < get_count($this->idlist); $i++) {
                $tmpval = $i + 1;
                $ival = sprintf("%02d", $tmpval);
                $this->record["RECORD"][$ival] = VARS::post($this->idlist[$i]);
            }
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        //取得資格・検定の文字数
        if ($this->field["SHUTOKUSIKAKU_GYOU"] != "" && $this->field["SHUTOKUSIKAKU_MOJI"] != "") {
            $this->getPro["SHUTOKUSIKAKU"]["moji"] = $this->field["SHUTOKUSIKAKU_MOJI"];
            $this->getPro["SHUTOKUSIKAKU"]["gyou"] = $this->field["SHUTOKUSIKAKU_GYOU"];
        } else {
            $this->getPro["SHUTOKUSIKAKU"]["moji"] = 35;
            $this->getPro["SHUTOKUSIKAKU"]["gyou"] = 4;
        }
        //固定設定
        $this->getPro["ZIRITUKATUDO"]["moji"] = 20;
        $this->getPro["ZIRITUKATUDO"]["gyou"] = 4;

        $this->getPro["COMMUNICATION"]["moji"] = 25;
        $this->getPro["COMMUNICATION"]["gyou"] = 6;
    }

    //入力項目チェック
    public function validateInput()
    {
        //必須チェック
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }

        if ($this->validate_row_cnt($this->field["SHUTOKUSIKAKU"], ($this->getPro["SHUTOKUSIKAKU"]["moji"] * 2)) > $this->getPro["SHUTOKUSIKAKU"]["gyou"]) {
            $this->setWarning("MSG915", "(取得資格・検定は{$this->getPro["SHUTOKUSIKAKU"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["COMMUNICATION"], ($this->getPro["COMMUNICATION"]["moji"] * 2)) > $this->getPro["COMMUNICATION"]["gyou"]) {
            $this->setWarning("MSG915", "(所見は{$this->getPro["COMMUNICATION"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["ZIRITUKATUDO"], ($this->getPro["ZIRITUKATUDO"]["moji"] * 2)) > $this->getPro["ZIRITUKATUDO"]["gyou"]) {
            $this->setWarning("MSG915", "(自立活動は{$this->getPro["ZIRITUKATUDO"]["gyou"]}行までです)");
            return false;
        }

        return true;
    }

    //更新用モデル
    public function &getUpdateModel()
    {
        if (!$this->validateInput()) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }

    //更新
    public function &update()
    {
        knjd183dQuery::getUpdateQuery($this);
        if ($this->hid_recordnamelist) {
            knjd183dQuery::getInsertQuery2($this);
        }
        return;
    }
}
