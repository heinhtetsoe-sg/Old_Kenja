<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knje384Model extends Model {
    var $field      = array();
    var $cmd;

    /**
     * メンバ変数の初期化
     */
    function knje384Model() {
    }

    function init() {

        $this->field = array("BASE_SCORE"    =>  VARS::post("BASE_SCORE"),     //基準点
                             "RANGE_F"       =>  VARS::post("RANGE_F"),        //範囲(FROM)
                             "RANGE_T"       =>  VARS::post("RANGE_T"),        //範囲(TO)
                             );

        $this->cmd = VARS::request("cmd");
    }

    function &getMainModel() {
    }

    //入力項目チェック
    function validateInput() {

        if ($this->cmd == "update") {
            //必須チェック
            if (strlen($this->field["BASE_SCORE"]) == 0) {
                $this->setWarning("MSG301","(基準点)");
                return false;
            }
            if (strlen($this->field["RANGE_F"]) == 0) {
                $this->setWarning("MSG301","(範囲(FROM))");
                return false;
            }
            if (strlen($this->field["RANGE_T"]) == 0) {
                $this->setWarning("MSG301","(範囲(TO))");
                return false;
            }
            //数値チェック
            if ($this->field["BASE_SCORE"] != "" && !is_numeric($this->field["BASE_SCORE"])) {
                $this->setWarning("MSG905","(基準点)");
                return false;
            }
            if ($this->field["RANGE_F"] != "" && !is_numeric($this->field["RANGE_F"])) {
                $this->setWarning("MSG905","(範囲(FROM))");
                return false;
            }
            if ($this->field["RANGE_T"] != "" && !is_numeric($this->field["RANGE_T"])) {
                $this->setWarning("MSG905","(範囲(TO))");
                return false;
            }
            //文字数チェック
            if (strlen($this->field["BASE_SCORE"]) > 4) {
                $this->setWarning("MSG915","(基準点は4バイトまでです)");
                return false;
            }
            if (strlen($this->field["RANGE_F"]) > 4) {
                $this->setWarning("MSG915","(範囲(FROM)は4バイトまでです)");
                return false;
            }
            if (strlen($this->field["RANGE_T"]) > 4) {
                $this->setWarning("MSG915","(範囲(TO)は4バイトまでです)");
                return false;
            }
        } else if ($this->cmd == "copy") {

            $db = Query::dbCheckOut();

            $query = knje384Query::getToeflMst(CTRL_YEAR, true);
            if (0 < $db->getOne($query)) {
                $this->setWarning("MSG917","(今年度データが存在しています)");
                return false;
            }
            $query = knje384Query::getToeflMst(CTRL_YEAR - 1, true);
            if (0 == $db->getOne($query)) {
                $this->setWarning("MSG917","(前年度データが存在していません)");
                return false;
            }

            Query::dbCheckIn($db);
        }
        return true;
    }

    //前年度からのコピー
    function &getCopyModel() {
        if ($this->validateInput()) {
            $db = Query::dbCheckOut();

            $db->query(knje384Query::getCopyQuery());

            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
        return;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            knje384Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
