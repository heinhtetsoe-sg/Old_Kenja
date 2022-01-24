<?php

require_once('for_php7.php');

class knjz407Model extends Model {
    var $field = array();
    var $cmd;
    var $code;
    var $isupdsuccess;

    function knjz407Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->code);
            unset($this->isupdsuccess);
        }

        if ($this->cmd != "reset") {
            $this->field = array("CODE"   =>  VARS::post("CODE"),
                                 "NAME1"  =>  VARS::post("NAME1"),
                                 "RECCNT" =>  VARS::post("RECCNT"),
                                 "FIX"    =>  VARS::post("FIX")
                                 );
        } else {
            $this->field = array();
            $this->isupdsuccess = false;

        }

        $this->cmd = VARS::request("cmd");

        if (VARS::get("CODE")) {
            $this->code = VARS::get("CODE");
            $this->field["CODE"] = VARS::get("CODE");
            $this->field["RECCNT"] = "";
        }
        //左選択、及び確定ボタン以外の操作と、DB登録チェックエラー時は、値を保持する。
        if (($this->cmd == "edit" && !$this->isupdsuccess) || $this->cmd != "edit" && $this->cmd != "fix" && VARS::post("NAME2_1")) {
            $this->detailRemark = array();
            $ii = 1;
            while (VARS::post("NAME2_".$ii)) {
                $addwk = array();
                $addwk["SEQ"]   = VARS::post("SEQ_".$ii);
                $addwk["NAME2"] = VARS::post("NAME2_".$ii);
                $addwk["QUESTION_CONTENTS"] = VARS::post("QUESTION_CONTENTS_".$ii);
                $addwk["ANSWER_PATTERN"] = VARS::post("ANSWER_PATTERN_".$ii);
                $addwk["ANSWER_SELECT_COUNT"] = VARS::post("ANSWER_SELECT_COUNT_".$ii);
                $this->detailRemark[] = $addwk;
                $ii++;
            }
        } else {
            $this->detailRemark = array();
        }
    }

    //入力項目チェック
    function validateInput() {

        //必須チェック
        if (trim($this->field["CODE"]) == "") {
            $this->setWarning("MSG301", "　　　　　　　(　コード　)");
            return false;
        }
        if ($this->cmd == "add" || $this->cmd == "update") {
            if (trim($this->field["NAME1"]) == "") {
                $this->setWarning("MSG301", "　　　　　　　(　名称　)");
                return false;
            }
        }

        //文字数チェック
        if ($this->cmd == "add" || $this->cmd == "update") {
            if (strlen($this->field["CODE"]) > 2 ) {
                $this->setWarning("MSG915", "(コード) 2バイトまでです。");
                return false;
            }

            if ($this->validate_row_cnt($this->field["NAME1"], (10 * 2)) > 1) {
                $this->setWarning("MSG915", "(名称) 全角10文字までです。");
                return false;
            }
            foreach ($this->detailRemark as $val) {
                if (is_array($val)) {
                    if ($this->validate_row_cnt($val["NAME2"], (15 * 2)) > 1) {
                        $this->setWarning("MSG915", "(名称) 全角15文字までです。");
                        return false;
                    }
                    $chkStr = $val["QUESTION_CONTENTS"];
                    if ($this->validate_row_cnt($chkStr, (30 * 2)) > 10) {
                        $this->setWarning("MSG915", "(設問) 全角300文字までです。");
                        return false;
                    }
                    if (!in_array($val["ANSWER_PATTERN"], array("1", "2", "3"))) {
                        $this->setWarning("MSG915", "(回答入力形式) 想定外の指定です。");
                        return false;
                    }
                    if (strlen($val["ANSWER_SELECT_COUNT"]) != mb_strlen($val["ANSWER_SELECT_COUNT"]) or (strlen($val["ANSWER_SELECT_COUNT"]) > 2)) {
                        $this->setWarning("MSG915", "(名称) 半角2文字までです。");
                        return false;
                    }
                }
            }
        }

        //頭ゼロ埋め
        if (strlen($this->field["CODE"]) <= 2) {
            $this->field["CODE"] = sprintf("%02d", $this->field["CODE"]);
        }

        //キーの更新チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && ($this->code != $this->field["CODE"])) {
            $this->setWarning("MSG308");
            return false;
        }
        if (($this->cmd == "update" || $this->cmd == "delete") && !strlen($this->code)) {
            $this->setWarning("MSG308");
            return false;
        }

        //存在チェック
        $db = Query::dbCheckOut();
        $query = knjz407Query::getBaseRemarkMst($this->field["CODE"]);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        //更新対象がなければエラー
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //追加時 既にレコードがあればエラー
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918");
            return false;
        }

        return true;
    }

    /**********/
    /* UPDATE */
    /**********/
    function &getUpdateModel() {
        $this->isupdsuccess = $this->validateInput();
        if ($this->isupdsuccess){
            knjz407Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }

    /**********/
    /* INSERT */
    /**********/
    function &getInsertModel() {
        if ($this->validateInput()){
            knjz407Query::getInsertQuery($this);
            $this->code = $this->field["CODE"];
            $this->setMessage("MSG201");
        }
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteModel() {
        if ($this->validateInput()){
            knjz407Query::getDeleteQuery($this);
            unset($this->code);
            unset($this->field);
            $this->setMessage("MSG201");
        }
    }
}
?>
