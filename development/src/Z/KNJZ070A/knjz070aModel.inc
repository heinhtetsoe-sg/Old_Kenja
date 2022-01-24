<?php

require_once('for_php7.php');

class knjz070aModel extends Model {
    var $cmd;
    var $control;
    var $year;
    var $selectdata;

    function knjz070aModel(){
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->year);
        }

        //プロパティファイル
        $this->getPropertiesAll();

        //年度
        $this->year = VARS::post("year");

        $this->selectdata = VARS::post("selectdata");
        $this->cmd = VARS::request("cmd");
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if (!is_numeric($this->year)) {
            $this->warning = "対象年度を選択して下さい。";
            return false;
        }
        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
            $this->setMessage("MSG201");
        }
    }

    //アップデート時の内部メソッド
    function &update() {
        knjz070aQuery::getUpdateQuery($this);
        return;
    }
}
?>
