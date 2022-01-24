<?php

require_once('for_php7.php');

class knjd420mModel extends Model
{

    public $cmd;
    public $schregno = "";
    public $name = "";
    public $grade = "";
    public $exp_year = "";
    public $exp_semester = "";
    public $testTable;         //テストコンボ使用テーブル
    public $field  = array();
    public $field2 = array();
    public $field2seq = array();
    public $field3 = array();
    public $field4 = array();
    public $control;           //コントロールマスタ
    public $subclasscd;
    public $unitcd;
    public $retprg;
    public $getPrgId;
    public $auth;
    public $sendAuth;

    public function knjd420mModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
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

        $this->sort         = VARS::request("SORT");
        // ソート順の初期化(デフォルト:学期)
        if (!$this->sort) {
            $this->sort = "SEMESTER";
        }
        $this->semester     = VARS::request("SEMESTER");
        $this->staffname    = VARS::request("STAFFNAME");
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

        if ($this->itemNameArr) {
            foreach ($this->itemNameArr as $itemNo => $item_remark) {
                $this->field["REMARK_{$itemNo}"] = VARS::post("REMARK_{$itemNo}");
            }
        }

        //状態区分セット
        $isNaraken = knjd420mQuery::getIsNaraken();
        $this->handicapFlg = ($isNaraken) ? ":2:3" : "";
    }

    //入力項目チェック
    public function validateInput($cmd)
    {
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
        if (strlen($this->staffname) > 75 ) {
            $this->setWarning("MSG914", "(作成者名は75バイトまでです)");
            return false;
        }
        if ($cmd == "update") {
            foreach ($this->itemNameArr as $itemNo => $item_remark) {
                if ($this->validate_row_cnt($this->field["REMARK_{$itemNo}"], 10 * 2) > 25) {
                    $this->setWarning("MSG915", "({$item_remark}は25行までです)");
                    return false;
                }
            }
        }

        return true;
    }

    //更新用のモデルを返します
    public function &getUpdateModel()
    {
        if (!$this->validateInput("update")) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }
    //アップデート時の内部メソッド
    public function &update()
    {
        knjd420mQuery::getInsertQuery($this);
        return;
    }

    //削除用のモデルを返します
    public function &getDeleteModel()
    {
        if (!$this->validateInput("delete")) {
            return;
        }
        knjd420mQuery::getDeleteQuery($this);
        $this->setMessage("MSG201");
    }
}