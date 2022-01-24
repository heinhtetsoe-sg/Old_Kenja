<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjz310Model extends Model {
    var $field      = array();
    var $cmd;
    var $subclasscd;        //primary
    var $year_code;
    var $isload;
    var $tmp;               //NO001
    var $year;              //NO001
    
    /**
     * メンバ変数の初期化
     */
    function knjz310Model()
    {
    }
    function init()
    {              
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("GROUPCD"    =>  VARS::post("GROUPCD"),            //グループコード
                             "GROUPNAME"  =>  VARS::post("GROUPNAME"),          //グループ名
                             "temp_cd"    =>  VARS::post("temp_cd"),            //最初のCD
                             "UPDATED"    =>  VARS::post("UPDATED")             //更新日付
                             );
 
        $this->no  = VARS::get("NO");

        $this->cmd         = VARS::request("cmd");
        if (VARS::get("PROGRAMID")) {
            unset($this->groupcd);
        }
        $temp = VARS::get("GROUPCD");
        if (isset($temp)){
            $this->groupcd     = VARS::get("GROUPCD");
        }
        //NO001-->
        $tmp=(VARS::request("year"));
        if($tmp){
            $this->year = VARS::request("year");
        }else{
            $this->year = CTRL_YEAR;
        }
        //NO001<--
    }
    //入力項目チェック
    function validateInput($kind)
    {
        //１レコード取得
        $this->isload = "0";
        if ($kind=="update" || $kind=="delete") {
            $row = knjz310Query::getRow($this->groupcd, $this);
            //リストより選択していない
            if ($this->field["temp_cd"]==""){
                $this->isload = "1";
                $this->setWarning('MSG308');
                return false;
            }
            //キー値が変更されていますかをチェック
            if ($this->field["GROUPCD"] !=$this->field["temp_cd"]){
                $this->isload = "1";
                $this->setWarning('MSG308');
                return false;
            }
            //更新日付チェック1
            if ($this->cmd == "update" && $row["UPDATED"] == ""){
                $this->setWarning('MSG303');
                return false;
            }
            //更新日付チェック2
            if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]){
                $this->setWarning('MSG307');
                return false;
            }
        }
        if ($kind=="update" || $kind=="insert") {
            $Row = knjz310Query::getRow($this->field["GROUPCD"], $this);
            if (trim($this->field["GROUPCD"]) == "") {
                $this->setWarning('MSG901',"データを入力してください。(グループコード)");
                return false;
            }
            //コードのゼロ埋め
            if (strlen($this->field["GROUPCD"]) < 4) {
                $this->groupcd = $this->field["GROUPCD"] = sprintf("%04d",$this->field["GROUPCD"]);
            
            }
            //重複チェック
            if ($this->cmd == "add" && is_array($Row)){
                $this->setWarning('MSG918',"(グループコード)");
                return false;
            }
            if (strlen($this->field["GROUPNAME"]) >60 ) {
                $this->setWarning('MSG901',"グループ名は60バイトまでです。");
                return false;
            } elseif (strlen($this->field["GROUPNAME"])==0){ 
                $this->setWarning('MSG901',"グループ名を入力してください。");
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
        }    
    }
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")){
            $this->groupcd = $this->field["GROUPCD"];
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
    function &delete()
    {
        $result = knjz310Query::getDeleteQuery($this->field, $this);
        if ($result==true) {
            if ( !isset($model->warning)){
                $this->setMessage('MSG201');  
            }
            $this->field = array();
        } else {
            $this->setWarning('MSG919',"所属している職員が存在します。");
        }    
        return $result;
    }
    function &insert()
    {
        $result = knjz310Query::getInsertQuery($this->field, $this);
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
        $result = knjz310Query::getUpdateQuery($this->field, $this);
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
