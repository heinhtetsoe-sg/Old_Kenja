<?php

require_once('for_php7.php');

class knje453Model extends Model {
    var $field = array();
    var $cmd;

    function knje453Model() {}

    function init() {

        $this->data_div = VARS::post("DATA_DIV");

        $this->field["REMARK"] = VARS::post("REMARK");

        //テキストのサイズ設定
        $this->remark_size["moji"] = 50;
        $this->remark_size["gyou"] = 1;

        $this->cmd = VARS::request("cmd");
    }

    //入力項目チェック
    function validateInput() {
        if ($this->cmd == "copy") {
            $db = Query::dbCheckOut();
            $pre_year = CTRL_YEAR - 1;
            $preYear_cnt = $db->getOne(knje453Query::getCopyData($pre_year, "cnt"));
            //前年度データチェック
            if ($preYear_cnt == 0) {
                $this->setWarning("MSG203", "前年度データがありません。");
                return false;  
            }
            Query::dbCheckIn($db);
        } else {
            //文字数チェック
            if ($this->validate_row_cnt($this->field["REMARK"], ($this->remark_size["moji"] * 2)) > $this->remark_size["gyou"]) {
                if ($this->remark_size["gyou"] == 1) {
                    $this->setWarning("MSG915","(定型文は全角{$this->remark_size["moji"]}文字までです)");
                } else {
                    $this->setWarning("MSG915","(定型文は{$this->remark_size["gyou"]}行までです)");
                }
                return false;
            }
        }

        return true;
    }

    //更新用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()){
            knje453Query::getInsertQuery($this);
            $this->setMessage("MSG201");
        }
        return false;
    }

    //コピー用モデルを返します
    function &getCopyModel() {
        if ($this->validateInput()) {
            knje453Query::getCopyQuery();
            $this->setMessage("MSG201");
        }
        return false;
    }
}
?>
