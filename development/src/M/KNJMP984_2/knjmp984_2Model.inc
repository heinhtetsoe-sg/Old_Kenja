<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjmp984_2Model extends Model {
    var $field;
    var $cmd;
    var $selectdata;
    var $groupcd;
    var $groupname;
    var $year;
    /**
     * メンバ変数の初期化
     */
    function knjmp984_2Model()
    {
    }
    function init()
    {       
        $this->field = array("YEAR"          => VARS::post("YEAR"),
                             "LEVY_GROUP_CD" => VARS::post("LEVY_GROUP_CD"));
        
        $this->selectdata   = VARS::post("selectdata");
        $this->cmd          = VARS::request("cmd");
        
        if (VARS::get("SEND_YEAR") !="") {
            $this->year    = VARS::get("SEND_YEAR");
        }
        if (VARS::get("SEND_LEVY_GROUP_CD") !="") {
            $this->groupcd    = VARS::get("SEND_LEVY_GROUP_CD");
        }
        //初期値セット
        if ($this->cmd == "") {
            $this->field["YEAR"] = (!$this->field["YEAR"]) ? $this->year : $this->field["YEAR"];
            $this->field["LEVY_GROUP_CD"] = (!$this->field["LEVY_GROUP_CD"]) ? $this->groupcd : $this->field["LEVY_GROUP_CD"];
        }
    }
    //入力項目チェック
    function validateInput()
    {
        $db = Query::dbCheckOut();
        
        //他のグループに所属しているかをチェック
        $getCount = $db->getOne(knjmp984_2Query::getCountCheckQuery($this));   
        if ($getCount > 0) {
            $this->setWarning("MSG302", "選択した会計科目の中に別グループに設定しているものがあります。");
            return false;
        }

        Query::dbCheckIn($db);
        
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
        $result = knjmp984_2Query::getUpdateQuery($this);
        if ( !isset($model->warning)){
            $this->setWarning('MSG201');
        }
        return $result;
    }
}
?>
