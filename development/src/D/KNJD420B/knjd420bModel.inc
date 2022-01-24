<?php

require_once('for_php7.php');

class knjd420bModel extends Model {

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
    var $semestercopy;
    var $subclasscdcopy;

    function knjd420bModel() {
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

        $this->sort         = VARS::request("SORT");
        // ソート順の初期化(デフォルト:学期)
        if (!$this->sort) {
            $this->sort = "SEMESTER";
        }
        $this->subclasscd   = VARS::request("SUBCLASSCD");
        $this->unitcd       = VARS::request("UNITCD");
        $this->unitcd00     = '00';
        $this->remark_cnt   = VARS::post("GROUP_REMARK_CNT");

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

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
            $pos = strpos(VARS::get("GRADE"), "-");//ハイフンを含むときは、HR_CLASSはセットしない
            if ($pos === false) {
                $this->grade = VARS::get("GRADE")."-".VARS::get("HR_CLASS");
            } else {
                $this->grade = VARS::get("GRADE");
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

        if ($this->itemNameArr) {
            foreach($this->itemNameArr as $nameCd2 => $name1) {
                $this->field["REMARK_{$nameCd2}"] = VARS::post("REMARK_{$nameCd2}");
            }
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

    //コピー用のモデルを返します
    function &getCopyModel() {
        knjd420bQuery::getCopyQuery($this);
        $this->setMessage("MSG201");
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
        knjd420bQuery::getInsertQuery($this);
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        if (!$this->validateInput("delete")) {
            return;
        }
        knjd420bQuery::getDeleteQuery($this);
        $this->setMessage("MSG201");
    }
}
?>