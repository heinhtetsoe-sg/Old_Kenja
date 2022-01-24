<?php

require_once('for_php7.php');
class knjd420lModel extends Model {

    var $cmd;
    var $schregno = "";
    var $name = "";
    var $grade = "";
    var $exp_year = "";
    var $exp_semester = "";
    var $testTable;         //テストコンボ使用テーブル
    var $field  = array();
    var $control;           //コントロールマスタ
    var $subclasscd;
    var $unitcd;
    var $sendPrgId;
    var $sendAuth;
    var $auth;

    function knjd420lModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->sendPrgId);
            unset($this->sendAuth);
            unset($this->auth);

            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->subclasscd);
            unset($this->unitcd);
        }

        if (VARS::get("SEND_PRGID")) {
            $this->sendPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        //権限
        $this->auth = $this->sendPrgId ? $this->sendAuth : AUTHORITY;

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->cmd = VARS::request("cmd");

        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME") != "") {
            $this->name     = VARS::get("NAME");
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

        $this->paternInfo = array();
        $this->paternInfo['1'][3] = array("MOJI" => "40", "GYOU" => "10");
        $this->paternInfo['2'][1] = array("MOJI" => "20", "GYOU" => "19");
        $this->paternInfo['2'][3] = array("MOJI" => "20", "GYOU" => "19");
        $this->paternInfo['3'][1] = array("MOJI" => "12", "GYOU" => "25");
        $this->paternInfo['3'][2] = array("MOJI" => "12", "GYOU" => "25");
        $this->paternInfo['3'][3] = array("MOJI" => "15", "GYOU" => "25");

        $this->semester     = VARS::request("SEMESTER");
        $this->subclasscd   = VARS::request("SUBCLASSCD");
        $this->unitcd       = VARS::request("UNITCD");

        if ($this->itemNameArr) {
            foreach($this->itemNameArr as $key => $value) {
                $this->field["REMARK_{$key}"] = VARS::post("REMARK_{$key}");
            }
        }

        $this->deleteList = VARS::post("DEL_LIST");

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

        //文字数チェック
        if ($cmd == "update") {
            foreach($this->itemNameArr as $key => $value) {
                $moji = $this->paternInfo[$this->maxRemarkCnt][$key]["MOJI"];
                $gyou = $this->paternInfo[$this->maxRemarkCnt][$key]["GYOU"];
                if ($this->validate_row_cnt($this->field["REMARK_{$key}"], ($moji * 2)) > $gyou) {
                    $this->setWarning("MSG915","({$value}は全角{$moji}文字X{$gyou}行までです)");
                    return false;
                }
            }
        }

        return true;
    }

    //コピー用のモデルを返します
    function &getCopyModel() {
        knjd420lQuery::getCopyQuery($this);
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
        knjd420lQuery::getInsertQuery($this);
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        knjd420lQuery::getDeleteQuery($this);
        $this->setMessage("MSG201");
    }

    //リスト削除用のモデルを返します
    function &getListDeleteModel() {
        knjd420lQuery::getListDeleteQuery($this);
        $this->setMessage("MSG201");
    }

}
?>
