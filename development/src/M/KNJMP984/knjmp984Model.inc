<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjmp984Model extends Model {
    var $field      = array();
    var $cmd;
    var $tmp;
    
    /**
     * メンバ変数の初期化
     */
    function knjmp984Model()
    {
    }
    function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->levygroupcd);
        }
        $this->field = array("LEVY_GROUP_CD"    =>  VARS::post("LEVY_GROUP_CD"),      //グループコード
                             "LEVY_GROUP_NAME"  =>  VARS::post("LEVY_GROUP_NAME"),    //グループ名
                             "temp_cd"          =>  VARS::post("temp_cd")             //最初のCD
                             );
        $this->cmd = VARS::request("cmd");
 
        $temp = VARS::get("LEVY_GROUP_CD");
        if (isset($temp)){
            $this->levygroupcd     = VARS::get("LEVY_GROUP_CD");
        }
    }
    //入力項目チェック
    function validateInput($kind)
    {
        //１レコード取得
        $this->isload = "0";
        if ($kind=="update" || $kind=="delete") {
            $row = knjmp984Query::getRow($this->levygroupcd);
            //リストより選択していない
            if ($this->field["temp_cd"]==""){
                $this->isload = "1";
                $this->setWarning('MSG308');
                return false;
            }
            //キー値が変更されていますかをチェック
            if ($this->field["LEVY_GROUP_CD"] !=$this->field["temp_cd"]){
                $this->isload = "1";
                $this->setWarning('MSG308');
                return false;
            }
        }
        if ($kind=="update" || $kind=="insert") {
            $Row = knjmp984Query::getRow($this->field["LEVY_GROUP_CD"]);
            if (trim($this->field["LEVY_GROUP_CD"]) == "") {
                $this->setWarning('MSG901',"データを入力してください。(グループコード)");
                return false;
            }
            //コードのゼロ埋め
            if (strlen($this->field["LEVY_GROUP_CD"]) < 4) {
                $this->levygroupcd = $this->field["LEVY_GROUP_CD"] = sprintf("%04d",$this->field["LEVY_GROUP_CD"]);
            
            }
            //重複チェック
            if ($this->cmd == "add" && is_array($Row)){
                $this->setWarning('MSG918',"(グループコード)");
                return false;
            }
            if (strlen($this->field["LEVY_GROUP_NAME"]) >60 ) {
                $this->setWarning('MSG901',"グループ名は60バイトまでです。");
                return false;
            } elseif (strlen($this->field["LEVY_GROUP_NAME"])==0){ 
                $this->setWarning('MSG901',"グループ名を入力してください。");
                return false;
            }
        }    
        return true;
    }
    /**
     * 編集完了用のモデルを返します
    */
    //Update処理
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
            $this->levygroupcd = $this->field["LEVY_GROUP_CD"];
            $this->insert();
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

    //Delete処理
    function &delete()
    {
        $result = knjmp984Query::getDeleteQuery($this->field);
        if ($result==true) {
            if ( !isset($model->warning)){
                $this->setMessage('MSG201');  
            }
            $this->field = array();
        } else {
            $this->setWarning('MSG919',"設定している会計科目が存在します。");
        }    
        return $result;
    }
    
    //Insert処理
    function &insert()
    {
        $result = knjmp984Query::getInsertQuery($this->field);
        if ( !isset($model->warning)){
            $this->setMessage('MSG201');
        }
        return $result;
    }

    /**
     * アップデート時の内部メソッド
     */
    function &update()
    {
        $result = knjmp984Query::getUpdateQuery($this->field);
        if ( !isset($model->warning)){
            $this->setMessage('MSG201');
        }
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
