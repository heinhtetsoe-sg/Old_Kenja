<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knje386Model extends Model {
    var $field      = array();
    var $cmd;
    var $parentPrgId;

    /**
     * メンバ変数の初期化
     */
    function knje386Model() {
    }

    function init() {

        if (VARS::get("PROGRAMID") != "") {
            unset($this->parentPrgId);
        }
        if (VARS::get("SEND_PRGID") != "") {
            $this->parentPrgId = VARS::get("SEND_PRGID");
        }

        $this->field = array("GRADE"     =>  VARS::post("GRADE"),     //学年
                             "TEST_DATE" =>  VARS::post("TEST_DATE"), //TOEFL基準日
                             );

        $this->cmd = VARS::request("cmd");

        //選択した得点
        $this->selected = explode(",", VARS::post("CATEGORY_SELECTED_DATA"));

        //プロパティの読み込み
        $this->getPropertiesAll();
    }

    function &getMainModel() {
    }

    //入力項目チェック
    function validateInput() {
        if ($this->selected[0] == "") {
            $this->setWarning("MSG304");
            return false;
        }
        if (in_array("TOEFL_SCORE", $this->selected) && $this->field["TEST_DATE"] == "") {
            $this->setWarning("MSG301", "(TOEFLを選択している場合はTOEFL基準日を入力してください)");
            return false;
        }

        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            knje386Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
