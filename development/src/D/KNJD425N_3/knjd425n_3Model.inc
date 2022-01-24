<?php

require_once('for_php7.php');

class knjd425n_3Model extends Model {

    var $cmd;
    var $schregno = "";
    var $name = "";
    var $grade = "";
    var $exp_year = "";
    var $exp_semester = "";
    var $selKindNo = "";
    var $upddate = "";
    var $control;           //コントロールマスタ
    var $getPrgId;
    var $auth;
    var $sendAuth;
    var $field = array();

    function knjd425n_3Model() {
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
            unset($this->selKindNo);
            unset($this->upddate);
            unset($this->getPrgId);
            unset($this->auth);
            unset($this->sendAuth);
            unset($this->field);
        }

        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        //権限
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

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
        }

        $this->field = array(
            "INPUT_ITEMS"     =>     VARS::request("INPUT_ITEMS"),
            "SUBCLASSCD"      =>     VARS::request("SUBCLASSCD"),
            "SEMESTER"        =>     VARS::request("SEMESTER"),
        );

        //出力するREMARK数
        $this->remarkCnt = 4;
        //REMARK
        for ($i = 1; $i <= $this->remarkCnt; $i++) {
            $this->field["REMARK-".$i] = VARS::post("REMARK-".$i);
        }

        //DB接続
        $db = Query::dbCheckOut();

        //テーブルヘッダ
        $query = knjd425n_3Query::getHreportGuidanceKindNameDat($this->exp_year, $this->selKindNo);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->remarkTitle[$row["KIND_SEQ"]] = $row["KIND_REMARK"];
        }
        $result->free();

        //各textareaの最大文字数
        $this->textLimit = array(
            "1"     =>     array("moji" => 12, "gyou" => 25),
            "2"     =>     array("moji" => 12, "gyou" => 25),
            "3"     =>     array("moji" => 15, "gyou" => 25),
            "4"     =>     array("moji" => 40, "gyou" => 20),
        );

        //ソート
        if (VARS::get("PROGRAMID") != "") {
            unset($this->getSort);
            //ソート処理の初期化
            $this->sort["SEMESTER"] = 1;
        }
        if (VARS::get("sort")) {
            $this->getSort = VARS::get("sort");
            $this->sort[$this->getSort] = $this->sort[$this->getSort] == 1 ? 2 : 1;
        }

        //「合わせた指導」教科登録
        $this->targetClassField = array(
            "SUBCLASSCD"     =>     VARS::post("SUBCLASSCD"),
        );

        //登録ボタン押下時にチェックされているチェックボックスの値を取得
        if ($this->cmd == "targetClassInsert") {
            $this->targetClassCheckValue = array();
            $query = knjd425n_3Query::getVClass($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if (VARS::post("CHECK-".$row["CLASSCD"]."-".$row["SCHOOL_KIND"])) {
                    $this->targetClassCheckValue[] = VARS::post("CHECK-".$row["CLASSCD"]."-".$row["SCHOOL_KIND"]);
                }
            }
        }

        $this->deleteList = VARS::post("DEL_LIST");

        //DB切断
        Query::dbCheckIn($db);
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }

        //文字数チェック
        if ($this->cmd == "update") {
            if ($this->field["INPUT_ITEMS"] == 1) {
                for ($i = 1; $i <= 3; $i++) {
                    if ($this->validate_row_cnt($this->field["REMARK-{$i}"], ($this->textLimit[$i]["moji"] * 2)) > $this->textLimit[$i]["gyou"]) {
                        $kindSeq = sprintf("%03d", $i);
                        $this->setWarning("MSG915","({$this->remarkTitle[$kindSeq]}は{$this->textLimit[$i]["gyou"]}行までです)");
                        return false;
                    }
                }
            } else {
                if ($this->validate_row_cnt($this->field["REMARK-4"], ($this->textLimit[4]["moji"] * 2)) > $this->textLimit[4]["gyou"]) {
                    $kindSeq = sprintf("%03d", 4);
                    $this->setWarning("MSG915","({$this->remarkTitle[$kindSeq]}は{$this->textLimit[4]["gyou"]}行までです)");
                    return false;
                }
            }
        }

        return true;
    }

    //更新用のモデルを返します
    function &getUpdateModel() {
        if (!$this->validateInput()) {
            return;
        }
        knjd425n_3Query::getInsertQuery($this);
        $this->setMessage("MSG201");
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        if (!$this->validateInput()) {
            return;
        }
        knjd425n_3Query::getDeleteQuery($this);
        $this->setMessage("MSG201");
    }

    //リスト削除用のモデルを返します
    function &getListDeleteModel() {
        knjd425n_3Query::getListDeleteQuery($this);
        $this->setMessage("MSG201");
    }

    //「合わせた指導」教科登録
    function &getTargetClassInsertModel() {
        knjd425n_3Query::getTargetClassInsertQuery($this);
        $this->setMessage("MSG201");
    }

}
?>
