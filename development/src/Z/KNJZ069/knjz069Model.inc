<?php

require_once('for_php7.php');

//定数定義

class knjz069Model extends Model {
    var $cmd;
    var $field;
    var $selectdata;

    //メンバ変数の初期化
    function knjz069Model()
    {
    }
    function init()
    {
        $this->cmd               = VARS::request("cmd");

        $this->field = array(
            "SEMESTER"        =>     VARS::post("SEMESTER"),
            "GRADE"           =>     VARS::post("GRADE"),
            "SUBCLASSCD"      =>     VARS::post("SUBCLASSCD"),
            "ELEMENT_DIV"     =>     VARS::post("ELEMENT_DIV"),
        );

        $this->selectdata        = VARS::post("selectdata");
        $this->rightMoveData     = VARS::post("rightMoveData");
    }

    //入力項目チェック
    function validateInput()
    {
        $selectdata = explode (",", $this->selectdata);
        if (get_count($selectdata) > 6) {
            $this->setWarning("MSG915", "( 対象一覧 ) 6つまで");
            return false;
        }

        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
        }
    }
    //アップデート時の内部メソッド
    function &update()
    {
        knjz069Query::getUpdateQuery($this);
        if (!isset($model->warning)){
            $this->setMessage("MSG201");
        }
    }
}
?>
