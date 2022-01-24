<?php

require_once('for_php7.php');

//定数定義

class knjz150Model extends Model {
    var $cmd;
    var $year;  //年度
    var $selectdata;

    //メンバ変数の初期化
    function knjz150Model()
    {
    }
    function init()
    {
        $temp = VARS::post("year");
        if (isset($temp)) {
            $this->year          = VARS::post("year");
        }else{
            $this->year          = CTRL_YEAR;           //現在処理年度を初期値にする
        }
        $this->selectdata        = VARS::post("selectdata");
        $this->cmd               = VARS::request("cmd");
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
        $result = knjz150Query::getUpdateQuery($this);
        return $result;
    }
}
?>
