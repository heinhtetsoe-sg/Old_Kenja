<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjj080Model extends Model {
    var $cmd;
    var $year;  //年度
    var $selectdata;
    
    /**
     * メンバ変数の初期化
     */
    function knjj080Model()
    {
    }
    function init()
    {

        if (VARS::get("PROGRAMID")) {
            unset($this->schKind);
        }

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->cmd               = VARS::request("cmd"); /* NO001 move1 */
        $temp = VARS::post("year");    
        if (isset($temp)) {
            $this->year              = VARS::post("year");
        }else{ /* NO001↓ */
            $this->year              = CTRL_YEAR;               //現在処理年度を初期値にする
        }      /* NO001↑ */
        $this->selectdata        = VARS::post("selectdata");

        if (VARS::post("SCHKIND") != "") {
            $this->schKind = VARS::post("SCHKIND");
        }
//NO001 move1        $this->cmd               = VARS::request("cmd");
    }
    //入力項目チェック
    function validateInput()
    {
        // 権限チェック 2004/05/11 nakamoto add
        if(common::SecurityCheck(STAFFCD,PROGRAMID) != DEF_UPDATABLE){
            $this->setWarning("MSG300");
            return false;
        }
        /* NO001↓ */
        if (!is_numeric($this->year)){
            $this->warning = "対象年度を選択してください。";
            return false;
        }
        /* NO001↑ */

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
        $result = knjj080Query::getUpdateQuery($this);
        return $result;
    }
}
?>
