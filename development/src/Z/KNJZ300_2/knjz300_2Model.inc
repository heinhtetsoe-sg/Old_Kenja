<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjz300_2Model extends Model {
    var $cmd;
    var $selectdata;
    var $GROUPCD;
    var $GROUPNAME;
    var $post_year;
    var $userscd;
    /**
     * メンバ変数の初期化
     */
    function knjz300_2Model()
    {
    }
    function init()
    {
        $this->userscd      = VARS::request("USERSCD");
        $this->post_year    = VARS::request("YEAR");
        $this->GROUPCD      = VARS::post("GROUPCD");
        $this->GROUPNAME    = VARS::post("GROUPNAME");
        $this->selectdata   = VARS::post("selectdata");
        $this->cmd          = VARS::request("cmd");

        if (VARS::request("GROUPCD") !="") {
            $this->GROUPCD          = VARS::request("GROUPCD");
            $this->GROUPNAME          = VARS::request("GROUPNAME");
        }

        //プロパティファイル
        $this->getPropertiesAll();
    }
    //入力項目チェック
    function validateInput()
    {
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

//        $this->setWarning("MZ0004", "期末または学年末の成績処理が既に実施されています！\\n『成績処理（期末）』\\n『成績処理（学年末）』\\nで仮評価再処理を行って下さい。");
    /**
     * アップデート時の内部メソッド
     */
    function &update()
    {
        $result = knjz300_2Query::getUpdateQuery($this);
        $this->setMessage("MSG201");
        return $result;
    }
    
}
?>
