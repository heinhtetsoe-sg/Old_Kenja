<?php

require_once('for_php7.php');

//定数定義

class knjz072aModel extends Model {
    var $cmd;
    var $year;  //年度
    var $selectdata;
    var $temp_year;

    //メンバ変数の初期化
    function knjz072aModel()
    {
    }
    function init()
    {
        //プロパティファイル
        $this->getPropertiesAll();
        
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
        }
    }
    //アップデート時の内部メソッド
    function &update()
    {
        $result = knjz072aQuery::getUpdateQuery($this);
        if (!isset($model->warning)){
            $this->setMessage("MSG201");
        }
        if ($result==true) {
            $this->year = "";
        }
        return $result;
    }
}
?>
