<?php

require_once('for_php7.php');

//定数定義

class knjz350kModel extends Model {
    var $cmd;
    var $year;  //年度
    var $selectdata;
    var $selectdata2;

    //メンバ変数の初期化
    function knjz350kModel()
    {
    }
    function init()
    {
        $this->year             = CTRL_YEAR;                        //現在処理年度を初期値にする
        $this->selectdata       = VARS::post("selectdata");
        $this->selectdata2      = VARS::post("selectdata2");
        $this->cmd              = VARS::request("cmd");
    }
    //入力項目チェック
    function validateInput()
    {
        if (!is_numeric($this->year)){
            $this->warning = "対象年度を選択してください。";
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
            $this->setMessage("MSG201");
        }
    }

    //アップデート時の内部メソッド
    function &update()
    {
        $result = knjz350kQuery::getUpdateQuery($this);
        return $result;
    }
}
?>
