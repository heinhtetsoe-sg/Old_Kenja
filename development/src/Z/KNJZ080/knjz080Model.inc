<?php

require_once('for_php7.php');

//定数定義
class knjz080Model extends Model {
    var $field      = array();
    var $cmd;
    var $edboardcd;        //primary
    //メンバ変数の初期化
    function knjz080Model()
    {
    }
    function init()
    {
        $this->field = array("EDBOARDCD"    =>  VARS::post("EDBOARDCD"),        //教育委員会コード
                             "EDBOARDNAME"  =>  VARS::post("EDBOARDNAME"),      //教育委員会名称
                             "EDBOARDABBV"  =>  VARS::post("EDBOARDABBV"),      //教育委員会略称
                             "UPDATED"      =>  VARS::post("UPDATED")
                             );

        $this->cmd             = VARS::request("cmd");
        if (VARS::get("year_code")){
            unset($this->edboardcd);
        }
       if (VARS::get("PROGRAMID")) {
           $this->edboardcd = "";
       }
        if (VARS::get("EDBOARDCD")!="") {
            $this->edboardcd    = VARS::get("EDBOARDCD");
        }
    }
    //入力項目チェック
    function validateInput($kind)
    {
        if (trim($this->field["EDBOARDCD"]) == ""){
            $this->setWarning("MSG301","(教育委員会コード)");
            return false;
        }

        if ($kind=="update" || $kind=="delete"){
            //リストより選択していない
            if ($this->edboardcd==""){
                $this->setWarning("MSG308");
                return false;
            }
            //キー値が変更されていますかをチェック
            if ($this->field["EDBOARDCD"] !=$this->edboardcd){
                $this->setWarning("MSG308","キー値は変更できません");
                return false;
            }

            //コードのゼロ埋め
            $this->field["EDBOARDCD"] = sprintf("%06d",$this->field["EDBOARDCD"]);
            //１レコード取得
            $row = knjz080Query::getRow($this->field["EDBOARDCD"]);

            //更新日付チェック1
            if ($this->cmd == "update" && $row["UPDATED"] == ""){
                $this->setWarning("MSG303");
                return false;
            }
            //更新日付チェック2
            if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]){
                $this->setWarning("MSG307");
                return false;
            }
        }
        if ($kind=="update" || $kind=="insert"){

            //コードのゼロ埋め
            $this->field["EDBOARDCD"] = sprintf("%06d",$this->field["EDBOARDCD"]);
            //１レコード取得
            $row = knjz080Query::getRow($this->field["EDBOARDCD"]);

            //重複チェック
            if ($this->cmd == "add" && is_array($row)){
                $this->setWarning("MSG918","(教育委員会コード)");
                return false;
            }
            if (strlen($this->field["EDBOARDNAME"]) >60 ){
                $this->setWarning("MSG915","(教育委員会名称は60バイトまでです。)");
                return false;
            } elseif (strlen($this->field["EDBOARDNAME"])==0){ 
                $this->setWarning("MSG301","(教育委員会名称)");
                return false;
            } elseif (strlen($this->field["EDBOARDABBV"]) >15){ 
                $this->setWarning("MSG915","(教育委員会略称は15バイトまでです。)");
                return false;
            }
        }
        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")){
            $this->update();
        }
    }
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")){
            $this->insert();
            $this->edboardcd = $this->field["EDBOARDCD"];
        }
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")){
            $this->delete();
        }
        return;
    }
    function &delete()
    {
        if (!isset($this->edboardcd)) {
            $this->setWarning("MSG303");
            return;
        }
       if (knjz080Query::IsExisting($this->edboardcd)){
            $this->setWarning("MSG919", "出身中学校マスタに登録されています。");
            return;
        }
        $result = knjz080Query::getDeleteQuery($this->field);
        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
            $this->field = array();
        }
    }
    function &insert()
    {
        $result = knjz080Query::getInsertQuery($this->field);
        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
        }
        return $result;
    }

    //アップデート時の内部メソッド
    function &update()
    {
        $result = knjz080Query::getUpdateQuery($this->field);
        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
        }
        return $result;
    }
    //変数をクリアします
    function clean()
    {
    }
}
?>
