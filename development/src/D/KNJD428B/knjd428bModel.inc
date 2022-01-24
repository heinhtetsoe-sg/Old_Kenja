<?php

require_once('for_php7.php');

class knjd428bModel extends Model {

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

    function knjd428bModel() {
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

        $this->field = array("INPUT_DIV"            => VARS::request("INPUT_DIV"),
                             "INPUT_DETAIL_DIV1"     => VARS::post("INPUT_DETAIL_DIV1"),
                             "INPUT_DETAIL_DIV2"     => VARS::post("INPUT_DETAIL_DIV2")
                            );
        if (VARS::request("SELECT_SEMES") != "") {
            $this->selectSemes = VARS::request("SELECT_SEMES");
        }

        $this->sort         = VARS::request("SORT");
        // ソート順の初期化(デフォルト:学期)
        if (!$this->sort) {
            $this->sort = "SEMESTER";
        }

        //科目CD
        $this->subclasscd   = VARS::request("SUBCLASSCD");

        //奈良特支は単元CD = 00で固定の仕様
        $this->unitcd00     = '00';

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

        if (VARS::post("KEY_DATA") != "") {
            $tmp = VARS::post("KEY_DATA");
            $this->keyData = explode(",", $tmp);
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
            foreach($this->itemNameArr as $kindSeq => $itemName) {
                $this->field["REMARK_{$kindSeq}"] = VARS::post("REMARK_{$kindSeq}");
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
        if ($this->field["INPUT_DIV"] == 3 && $this->subclasscd == "") {
            $this->setWarning("MSG301", "科目コード");
            return false;
        }

        //文字数チェック
        if ($cmd == "update") {
            foreach($this->itemNameArr as $kindSeq => $itemName) {
                $mojiGyou = explode("-", $this->mojiGyouData[$kindSeq]);
                if ($this->validate_row_cnt($this->field["REMARK_{$kindSeq}"], ($mojiGyou[0] * 2)) > $mojiGyou[1]) {
                    $this->setWarning("MSG915","({$itemName}は{$mojiGyou[1]}行までです)");
                    return false;
                }
            }
        }

        return true;
    }

    //コピー用のモデルを返します
    function &getCopyModel() {
        if ($this->field["INPUT_DIV"] != 3) {
            knjd428bQuery::getCopyQuery($this);
        } else {
            knjd428bQuery::getCopySubclassQuery($this);
        }
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
        if ($this->field["INPUT_DIV"] != 3) {
            knjd428bQuery::getInsertQuery($this);
        } else {
            knjd428bQuery::getInsertSubclassQuery($this);
        }
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        if (!$this->validateInput("delete")) {
            return;
        }

        if ($this->field["INPUT_DIV"] != 3) {
            knjd428bQuery::getDeleteQuery($this);
        } else {
            knjd428bQuery::getDeleteSubclassQuery($this);
        }
        $this->setMessage("MSG201");
    }
}
?>
