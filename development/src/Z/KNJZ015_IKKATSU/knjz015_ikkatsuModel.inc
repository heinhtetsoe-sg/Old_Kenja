<?php

require_once('for_php7.php');

class knjz015_ikkatsuModel extends Model {
    var $cmd;
    var $year;  //年度
    var $selectdata;
    var $selectdata2;

    //メンバ変数の初期化
    function knjz015_ikkatsuModel() {
    }
    function init() {
        $this->field = array("SCHOOL_KIND"             =>  VARS::post("SCHOOL_KIND")       //校種
                             );

        $this->selectdata  = VARS::post("selectdata");
        $this->selectdata2 = VARS::post("selectdata2");
        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    //入力項目チェック
    function validateInput() {
        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->setMessage("MSG201");
        }
    }

    //アップデート時の内部メソッド
    function &update() {
        $result = knjz015_ikkatsuQuery::getUpdateQuery($this);
        return $result;
    }
}
?>
