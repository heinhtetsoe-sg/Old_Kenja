<?php

require_once('for_php7.php');

class knjd425_6Model extends Model {

    var $cmd;
    var $schregno = "";
    var $name = "";
    var $grade = "";
    var $exp_year = "";
    var $exp_semester = "";
    var $testTable;         //テストコンボ使用テーブル
    var $field  = array();
    var $field2 = array();
    var $field2seq = array();
    var $field3 = array();
    var $field4 = array();
    var $control;           //コントロールマスタ
    var $subclasscd;
    var $unitcd;
    var $retprg;
    var $getPrgId;
    var $auth;
    var $sendAuth;
    var $itemNameArr = array();

    function knjd425_6Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->subclasscd);
            unset($this->unitcd);
            unset($this->retprg);
            unset($this->getPrgId);
            unset($this->auth);
            unset($this->sendAuth);
        }

        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        //権限
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->maxRemarkCnt = '8';

        $this->paternInfo = array();
        $this->paternInfo['A'][1] = array("MOJI"        => "35",
                                          "GYOU"        => "25");
        $this->paternInfo['B'][1] = array("MOJI"        => "10",
                                          "GYOU"        => "25");
        $this->paternInfo['B'][2] = array("MOJI"        => "12",
                                          "GYOU"        => "25");
        $this->paternInfo['B'][3] = array("MOJI"        => "18",
                                          "GYOU"        => "25");
        $this->paternInfo['C'][1] = array("MOJI"        => "9",
                                          "GYOU"        => "25");
        $this->paternInfo['C'][2] = array("MOJI"        => "4",
                                          "GYOU"        => "25");
        $this->paternInfo['C'][3] = array("MOJI"        => "11",
                                          "GYOU"        => "25");
        $this->paternInfo['C'][4] = array("MOJI"        => "16",
                                          "GYOU"        => "25");
        $this->paternInfo['D'][1] = array("MOJI"        => "8",
                                          "GYOU"        => "25");
        $this->paternInfo['D'][2] = array("MOJI"        => "8",
                                          "GYOU"        => "25");
        $this->paternInfo['D'][3] = array("MOJI"        => "3",
                                          "GYOU"        => "25");
        $this->paternInfo['D'][4] = array("MOJI"        => "10",
                                          "GYOU"        => "25");
        $this->paternInfo['D'][5] = array("MOJI"        => "16",
                                          "GYOU"        => "25");

        $this->subclasscd   = VARS::request("SUBCLASSCD");
        $this->unitcd       = VARS::request("UNITCD");
        $this->unitcd00     = '00';
        $this->remark_cnt   = VARS::post("GROUP_REMARK_CNT");

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();

        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME") != "") {
            $this->name     = VARS::get("NAME");
        }
        if (VARS::get("RETPRGID") != "") {
            $this->retprg = VARS::get("RETPRGID");
        }
        if (VARS::get("GRADE") != "") {
            if ($this->retprg == "KNJD425") {
                $this->grade        = substr(VARS::get("GRADE"), 0, 2);
                $this->gradeHrClass = VARS::get("GRADE");
            } else {
                $this->grade    = VARS::get("GRADE");
            }
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
        }
        if (VARS::get("EXP_YEAR") != "") {
            $this->exp_year = VARS::get("EXP_YEAR");
        }
        if (VARS::get("EXP_SEMESTER") != "") {
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }
        $this->semester = VARS::request("SEMESTER");

        foreach($this->itemNameArr as $nameCd2 => $name1) {
            $this->field["REMARK_{$nameCd2}"] = VARS::post("REMARK_{$nameCd2}");
        }

        if (VARS::get("PROGRAMID")) {
            unset($this->getSort);
            //ソート処理の初期化
            $this->sort["SRT_SEMESTER"] = 1;
        }

        if (VARS::get("sort")) {
            $this->getSort = VARS::get("sort");
            $this->sort[$this->getSort] = $this->sort[$this->getSort] == 1 ? 2 : 1;
        }
    }

    //入力項目チェック
    function validateInput($cmd) {
        //必須チェック
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }
        if ($this->subclasscd == "") {
            $this->setWarning("MSG301", "科目コード");
            return false;
        }
        if ($this->unit_aim_div == "1") {
            if (strlen($this->unitcd) == 0) {
                $this->setWarning("MSG301", "単元");
                return false;
            }
        }

/*
        // 名称マスタ設定
        if ($this->printPattern == '') {
            $this->setWarning("MSG203", "名称マスタの設定がされていません。");
            return false;
        }
*/

        //文字数チェック
        if ($cmd == "update") {
            if ($this->printPattern != '') {
                foreach($this->itemNameArr as $nameCd2 => $name1) {
                    if ($this->validate_row_cnt($this->field["REMARK_{$nameCd2}"], ($this->paternInfo[$this->printPattern][$nameCd2]["MOJI"] * 2)) > $this->paternInfo[$this->printPattern][$nameCd2]["GYOU"]) {
                        $this->setWarning("MSG915","({$name1}は{$this->paternInfo[$this->printPattern][$nameCd2]["GYOU"]}行までです)");
                        return false;
                    }
                }
            }
        }

        return true;
    }

    //更新用のモデルを返します
    function &getUpdateModel() {
        if (!$this->validateInput("update")) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }
    //アップデート時の内部メソッド
    function &update() {
        knjd425_6Query::getInsertQuery($this);
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        if (!$this->validateInput("delete")) {
            return;
        }
        knjd425_6Query::getDeleteQuery($this);
        $this->setMessage("MSG201");
    }
}
?>