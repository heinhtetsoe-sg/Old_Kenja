<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjz280Model extends Model {
    var $cmd;
    var $year;  //年度
    var $selectdata;
    
    /**
     * メンバ変数の初期化
     */

    function knjz280Model()
    {
        $this->year = CTRL_YEAR ;
    }

    function init()
    {
        $temp = VARS::post("year");
        $this->year = (isset($temp))? VARS::post("year") : CTRL_YEAR;
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

    /**
     * 編集完了用のモデルを返します
    */
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
            $this->setMessage("MSG201");
        }
    }

    /**
     * アップデート時の内部メソッド
     */
    function &update()
    {
        $result = knjz280Query::getUpdateQuery($this);
        return $result;
    }
}
?>
