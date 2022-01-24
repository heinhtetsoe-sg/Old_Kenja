<?php

require_once('for_php7.php');


class knje301Model extends Model {
    var $cmd;
    var $year;  //年度
    var $selectdata;
    
    /**
     * メンバ変数の初期化
     */
    function knje301Model()
    {
    }
    function init()
    {   
        $this->cmd               = VARS::request("cmd");
        $this->year              = CTRL_YEAR;
        $this->field = array(
                                'kubun' => VARS::post("kubun"),
                                'STAFFCD' => VARS::post("STAFFCD"),
                                'kousyu' => VARS::post("kousyu")
        );
        $this->selectdata        = VARS::post("selectdata");
        $this->selectdata_del    = VARS::post("selectdata_del");
        
        if($this->cmd=='main'){
            $this->selectdata = '';
            $this->selectdata_del = '';
        }
        
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
            $this->selectdata = '';
            $this->selectdata_del = '';
            $this->setMessage('MSG201');
        }
    }
    
    /**
     * アップデート時の内部メソッド
     */
    function &update()
    {
        $result = knje301Query::getUpdateQuery($this);
        return $result;
    }
    
    //前年度からのコピーのモデルを返します
    function &getCopyModel() {
        $db = Query::dbCheckOut();
        $this_year_check = $db->getOne(knje301Query::getSelectQueryDataCount($this, $this->year));
        $last_year_check = $db->getOne(knje301Query::getSelectQueryDataCount($this, $this->year-1));

        if($this_year_check != 0) {
            $this->setWarning("MSG203","既にデータが存在します。");
            return false;
        } else if($last_year_check == 0) {
            $this->setWarning("MSG303");
            return false;
        } else {
            knje301Query::selectQueryDataCopy($db, $this, $this->year);
        }
        $db->commit();

        $this->setMessage("MSG201");
        
        Query::dbCheckIn($db);
    }
}
?>
