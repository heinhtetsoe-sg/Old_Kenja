<?php

require_once('for_php7.php');

class knjd425l_4Model extends Model
{

    public $cmd;
    public $schregno;
    public $name;
    public $grade;
    public $exp_year;
    public $exp_semester;
    public $selKindNo;
    public $upddate;
    public $control;           //コントロールマスタ
    public $sendPrgId;
    public $sendAuth;
    public $auth;
    public $field = array();

    public function knjd425l_4Model()
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
            unset($this->selKindNo);
            unset($this->upddate);
            unset($this->sendPrgId);
            unset($this->sendAuth);
            unset($this->auth);
            unset($this->field);
        }

        if (VARS::get("SEND_PRGID")) {
            $this->sendPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        //権限
        $this->auth = $this->sendPrgId ? $this->sendAuth : AUTHORITY;

        $this->cmd = VARS::request("cmd");

        if (VARS::get("EXP_YEAR") != "") {
            $this->exp_year = VARS::get("EXP_YEAR");
        }
        if (VARS::get("EXP_SEMESTER") != "") {
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }
        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME") != "") {
            $this->name     = VARS::get("NAME");
        }
        if (VARS::get("GRADE") != "") {
            $this->grade    = VARS::get("GRADE");
        }
        if (VARS::get("KINDNO")) {
            $this->selKindNo = VARS::get("KINDNO");
        }
        if (VARS::get("UPDDATE")) {
            $this->upddate = VARS::get("UPDDATE");
            $this->recordDate = VARS::get("UPDDATE");
        }
        if (VARS::post("RECORD_DATE")) {
            $this->recordDate = VARS::post("RECORD_DATE");
        }
        if ($this->recordDate) {
            $this->recordDate = str_replace("/", "-", $this->recordDate);
        }

        $this->field = array(
            "INPUT_ITEMS"   =>     VARS::post("INPUT_ITEMS")
            , "SUBCLASSCD"  =>     VARS::post("SUBCLASSCD")
            , "SEMESTER"    =>     VARS::post("SEMESTER")
            , "UNITCD"      =>     VARS::post("UNITCD")
            , "REMARK"      =>     VARS::post("REMARK")
        );

        //DB接続
        $db = Query::dbCheckOut();
        //生徒の履修科目グループ取得
        $query = knjd425l_4Query::getGradeKindSchregGroupDat($this);
        $this->schregInfo = $db->getRow($query, DB_FETCHMODE_ASSOC);
        //テーブルヘッダ項目名取得(各教科等)
        $query = knjd425l_4Query::getGuidanceItemName($this);
// var_dump($query);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if ($row) {
            $this->remarkTitle["1"]["ITEM_REMARK"] = $row["ITEM_REMARK1"];
            $this->remarkTitle["2"]["ITEM_REMARK"] = $row["ITEM_REMARK2"];
            $this->remarkTitle["3"]["ITEM_REMARK"] = $row["ITEM_REMARK3"];
        }
        //各textareaの最大文字数
        $this->textLimit = array(
            "1"     =>     array("moji" => 15, "gyou" => 25),
            "2"     =>     array("moji" => 15, "gyou" => 25),
            "3"     =>     array("moji" => 20, "gyou" => 25),
        );

        //テキストエリア
        foreach ($this->textLimit as $seq => $textLimit) {
            $this->field["REMARK_".$seq] = VARS::post("REMARK_".$seq);
        }

        //テーブルヘッダ項目名取得(総合所見)
        $query = knjd425l_4Query::getHreportGuidanceKindNamedat($this);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if ($row) {
            $this->itemRemarkTitle = $row["KIND_REMARK"];
        }

        //「合わせた指導」教科登録
        $this->targetClassField = array(
            "SUBCLASSCD"     =>     VARS::post("SUBCLASSCD"),
        );
        if ($this->cmd == "targetClass") {
            if (VARS::get("SUBCLASSCD")) {
                $this->targetClassField["SUBCLASSCD"] = VARS::get("SUBCLASSCD");
            }
        }
        //登録ボタン押下時にチェックされているチェックボックスの値を取得
        if ($this->cmd == "targetClassInsert") {
            $this->targetClassCheck = array();
            if (VARS::post("CHECK")) {
                $this->targetClassCheck = VARS::post("CHECK");
            }
        }

        $this->deleteList = VARS::post("DEL_LIST");

        //DB切断
        Query::dbCheckIn($db);
    }

    //入力項目チェック
    public function validateInput()
    {
        //必須チェック
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }

        //文字数チェック
        if ($this->cmd == "update") {
            if ($this->field["INPUT_ITEMS"] == 1) {
                foreach ($this->textLimit as $seq => $textLimit) {
                    if ($this->validate_row_cnt($this->field["REMARK_{$seq}"], ($textLimit["moji"] * 2)) > $textLimit["gyou"]) {
                        $title = $this->remarkTitle[$seq]["ITEM_REMARK"];
                        $this->setWarning("MSG915", "({$title}は全角{$textLimit["moji"]}文字X{$textLimit["gyou"]}行までです)");
                        return false;
                    }
                }
            } else {
                $moji = 50;
                $gyou = 25;
                if ($this->validate_row_cnt($this->field["REMARK"], ($moji * 2)) > $gyou) {
                    $title = $this->itemRemarkTitle;
                    $this->setWarning("MSG915", "({$title}は全角{$moji}文字X{$gyou}行までです)");
                    return false;
                }
            }
        }

        return true;
    }

    //更新用のモデルを返します
    public function &getUpdateModel()
    {
        if (!$this->validateInput()) {
            $this->setCmd("check");
            return;
        }
        knjd425l_4Query::getInsertQuery($this);
        $this->setCmd("edit");
        $this->setMessage("MSG201");
    }

    //削除用のモデルを返します
    public function &getDeleteModel()
    {
        if (!$this->validateInput()) {
            $this->setCmd("check");
            return;
        }
        knjd425l_4Query::getDeleteQuery($this);
        $this->setCmd("edit");
        $this->setMessage("MSG201");
    }

    //リスト削除用のモデルを返します
    public function &getListDeleteModel()
    {
        knjd425l_4Query::getListDeleteQuery($this);
        $this->setCmd("edit");
        $this->setMessage("MSG201");
    }

    //「合わせた指導」教科登録
    public function &getTargetClassInsertModel()
    {
        knjd425l_4Query::getTargetClassInsertQuery($this);
        $this->setMessage("MSG201");
    }
}
