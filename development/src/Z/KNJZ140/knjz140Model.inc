<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjz140Model extends Model {
    var $field      = array();
    var $cmd;
    var $messagecd;        //primary
    var $MSG_KIND;
    var $isload;
//    var $clr;
    
    /**
     * メンバ変数の初期化
     */
    function knjz140Model()
    {
    }
    function init()
    {
        $this->field = array("MSG_CD"      =>  VARS::post("MSG_CD"),           //メッセージコード
                             "MSG_LEVEL"   =>  VARS::post("MSG_LEVEL"),        //メッセージレベル
                             "MSG_CONTENT" =>  VARS::post("MSG_CONTENT"),      //メッセージ内容
                             "MSG_DETAIL"  =>  VARS::post("MSG_DETAIL"),       //メッセージ詳細
                             "HOWTO"       =>  VARS::post("HOWTO"),            //対処
                             "temp_cd"     =>  VARS::post("temp_cd"),          //最初のCD
                             "UPDATED"     =>  VARS::post("UPDATED")
                             );

        $this->cmd        = VARS::request("cmd");
        if (VARS::get("PROGRAMID")) {
            unset($this->messagecd);
        }
        $temp = VARS::get("MSG_CD");
        if (isset($temp)){
            $this->messagecd  = VARS::get("MSG_CD");
        }
        
    }
    //入力項目チェック
    function validateInput($kind)
    {
        //１レコード取得
        $row = knjz140Query::getRow($this->messagecd);
        $Row = knjz140Query::getRow("MSG".$this->field["MSG_CD"]);
        $this->isload = "0";
        if ($kind=="update" || $kind=="delete") {
            //リストより選択していない
            if ($this->field["temp_cd"]==""){
                $this->isload = "1";
                $this->setWarning("MSG308");
                return false;
            }
            //キー値が変更されていますかをチェック
            if ($this->field["MSG_CD"] !=$this->field["temp_cd"]){
                $this->isload = "1";
                $this->setWarning("MSG308");
                return false;
            }
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
        if ($kind=="update" || $kind=="insert") {
            if (trim($this->field["MSG_CD"]) == "") {
                $this->setWarning("MSG301","(メッセージコード)");
                return false;
            }

            //重複チェック
            if ($this->cmd == "add" && is_array($Row)){
                $this->setWarning("MSG918","(メッセージコード)");
                return false;
            }
            if (strlen($this->field["MSG_CD"]) >3 ) {
                $this->setWarning("MSG901","メッセージコードは3バイト以内にしてください");
                return false;
            } elseif (!is_numeric($this->field["MSG_CD"])){
                $this->setWarning("MSG907","(メッセージコード)");
                return false;
            } elseif (strlen($this->field["MSG_CONTENT"]) >150 ) {
                $this->setWarning("MSG901","メッセージ内容は150バイト以内にしてください");
                return false;
            } elseif (strlen($this->field["MSG_DETAIL"]) >150){ 
                $this->setWarning("MSG901","メッセージ詳細は150バイト以内にしてください");
                return false;
            } elseif (strlen($this->field["HOWTO"]) >150){ 
                $this->setWarning("MSG901","対処は150バイト以内にしてください");
                return false;    
            }
        }
        return true;
    }
    /**
     * 編集完了用のモデルを返します
    */
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")){
            $this->update();
            $this->setMessage("MSG201");
        }    
    }
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")){
            $this->insert();
            $this->messagecd = "MSG".$this->field["MSG_CD"];
            $this->setMessage("MSG201");
        }
        return;
    }

    /**
     * 削除用のモデルを返します
     */
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
        if (!isset($this->messagecd)) {
            $this->setWarning("MSG303");
            return;
        }
        
        $result = knjz140Query::getDeleteQuery($this->field);
        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
            $this->field = array();
        }
        return $result; 
    }
    function &insert()
    {
        $result = knjz140Query::getInsertQuery($this->field);
        return $result;
    }

    /**
     * アップデート時の内部メソッド
     */
    function &update()
    {
        $result = knjz140Query::getUpdateQuery($this->field);
        return $result;
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
