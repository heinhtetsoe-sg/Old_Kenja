<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjz130aModel extends Model {
    var $cmd;
    var $year;  //年度
    var $selectdata;
    
    /**
     * メンバ変数の初期化
     */
    function knjz130aModel()
    {
        $this->year = "";
    }
    function init()
    {
        //プロパティファイル
        $this->getPropertiesAll();
        
        $this->selectdata   = VARS::post("selectdata");
        $this->cmd          = VARS::request("cmd");
        if (VARS::post("year") !="") {
            $this->year     = VARS::post("year");
        }    
    }
    //入力項目チェック
    function validateInput()
    {
        if (!is_numeric($this->year)){
            $this->warning = "対象年度を選択して下さい";
            return false;
        }
        return true;
    }
    /**
     * 編集完了用のモデルを返します
    */
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
        }
    }      
    /**
     * アップデート時の内部メソッド
     */
    function &update()
    {   
        $result = knjz130aQuery::getUpdateQuery($this);
        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
        }
        if ($result==true) {
            $this->year = "";
        }
        return $result;
    }
}
?>
