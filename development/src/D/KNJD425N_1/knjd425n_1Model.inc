<?php

require_once('for_php7.php');

class knjd425n_1Model extends Model {

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
    var $itemNameArr = array();
    var $target = array();
    var $field = array();
    var $updateSeq = "";
    var $deleteSeq = "";
    var $getSort = "";
    var $sort;

    function knjd425n_1Model() {
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
            "SEMESTER"     =>     VARS::post("SEMESTER"),
        );

        //出力するREMARK数
        $this->remarkCnt = 8;
        //REMARK
        for ($i = 1; $i <= $this->remarkCnt; $i++) {
            $this->field["REMARK-".$i] = VARS::post("REMARK-".$i);
        }

        //DB接続
        $db = Query::dbCheckOut();

        //テーブルヘッダ
        $query = knjd425n_1Query::getHreportGuidanceKindNameDat($this->exp_year, $this->selKindNo);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->remarkTitle[$row["KIND_SEQ"]] = $row["KIND_REMARK"];
        }
        $result->free();

        //自立活動区分 対象
        $this->target = array(
            "01" => "重点目標①",
            "02" => "重点目標②",
            "03" => "重点目標③",
            "04" => "重点目標④",
            "05" => "指導内容①",
            "06" => "指導内容②",
            "07" => "指導内容③",
            "08" => "指導内容④",
        );

        //各textareaの最大文字数
        $this->textLimit = array(
            "1"     =>     array("moji" => 45, "gyou" =>  4),
            "2"     =>     array("moji" => 45, "gyou" =>  4),
            "3"     =>     array("moji" => 10, "gyou" => 20),
            "4"     =>     array("moji" => 17, "gyou" => 20),
            "5"     =>     array("moji" => 17, "gyou" => 20),
            "6"     =>     array("moji" => 45, "gyou" =>  4),
            "7"     =>     array("moji" => 45, "gyou" =>  4),
            "8"     =>     array("moji" => 45, "gyou" =>  4),
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

        //更新対象のSEQ
        if (VARS::post("UPDATE_SEQ")) {
            $this->updateSeq = explode(":", VARS::post("UPDATE_SEQ"));
        }
        //削除対象のSEQ
        if (VARS::post("DELETE_SEQ")) {
            $this->deleteSeq = explode(":", VARS::post("DELETE_SEQ"));
        }

        //取消用の初期値
        $this->initRow = array();
        for ($i = 1; $i <= $this->remarkCnt; $i++) {
            $this->initRow["REMARK-".$i] = VARS::post("INIT_REMARK-".$i);
        }

        //「自立活動」区分登録
        //対象ラジオ
        if (VARS::post("TARGET")) {
            $this->field["TARGET"] = VARS::post("TARGET");
        }
        //登録ボタン押下時にチェックされているチェックボックスの値を取得
        if ($this->cmd == "zirituInsert") {
            $this->zirituCheckValue = array();
            $query = knjd425n_1Query::getHreportGuidanceSelfrelianceMst($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if (VARS::post("CHECK-".$row["SELF_DIV"]."-".$row["SELF_SEQ"])) {
                    $this->zirituCheckValue[] = VARS::post("CHECK-".$row["SELF_DIV"]."-".$row["SELF_SEQ"]);
                }
            }
        }

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
            foreach ($this->updateSeq as $seq) {
                if ($this->validate_row_cnt($this->field["REMARK-{$seq}"], ($this->textLimit[$seq]["moji"] * 2)) > $this->textLimit[$seq]["gyou"]) {
                    $kindSeq = sprintf("%03d", $seq);
                    $this->setWarning("MSG915","({$this->remarkTitle[$kindSeq]}は{$this->textLimit[$seq]["gyou"]}行までです)");
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
        knjd425n_1Query::getInsertQuery($this);
        $this->setMessage("MSG201");
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        if (!$this->validateInput()) {
            return;
        }
        knjd425n_1Query::getDeleteQuery($this);
        $this->setMessage("MSG201");
    }

    //更新用のモデルを返します
    function &getZirituInsertModel() {
        knjd425n_1Query::getZirituInsertQuery($this);
        $this->setMessage("MSG201");
    }

}
?>
