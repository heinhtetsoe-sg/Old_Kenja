<?php

require_once('for_php7.php');

//定数定義
class knjtx005Model extends Model {
    var $field      = array();
    var $cmd;
    var $kojin_no;      //primary
    var $s_date;        //primary
    //メンバ変数の初期化
    function knjtx005Model()
    {
    }
    function init()
    {
        $this->field = array("KOJIN_NO"				  =>  VARS::post("KOJIN_NO"),        		//個人番号
        					 "S_DATE"       		  =>  VARS::post("S_DATE"),          		//開始日
						     "BANKCD"          		  =>  VARS::post("BANKCD");
						     "BRANCHCD"          	  =>  VARS::post("BRANCHCD");
						     "CHOKIN_DIV"             =>  VARS::post("CHOKIN_DIV");
						     "ACCOUNT_NO"             =>  VARS::post("ACCOUNT_NO");
							 "BANK_MEIGI_SEI_KANA"    =>  VARS::post("BANK_MEIGI_SEI_KANA");
						     "BANK_MEIGI_MEI_KANA"    =>  VARS::post("BANK_MEIGI_MEI_KANA");
						     "BANK_MEIGI_SEI_NAME"    =>  VARS::post("BANK_MEIGI_SEI_NAME");
						     "BANK_MEIGI_MEI_NAME"    =>  VARS::post("BANK_MEIGI_MEI_NAME");
						     "ZIPCD"          		  =>  VARS::post("ZIPCD");
						     "ADDR1"          		  =>  VARS::post("ADDR1");
						     "ADDR2"          		  =>  VARS::post("ADDR2");
						     "TELNO1"          		  =>  VARS::post("TELNO1");
				       		 "TELNO2"          		  =>  VARS::post("TELNO2");
                             "UPDATED"      		  =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");
        if (VARS::get("year_code")){
            unset($this->kojin_no);
            unset($this->s_date);
        }
        if (VARS::get("PROGRAMID")) {
           $this->kojin_no = "";
           $this->s_date = "";
        }
        if (VARS::get("KOJIN_NO")!="") {
            $this->kojin_no    = VARS::get("KOJIN_NO");
        }
        if (VARS::get("S_DATE")!="") {
            $this->s_date    = VARS::get("S_DATE");
        }
    }
    //入力項目チェック
    function validateInput($kind)
    {
        if (trim($this->field["KOJIN_NO"]) == ""){
            $this->setWarning("MSG301","(個人番号)");
            return false;
        }
        if (trim($this->field["S_DATE"]) == ""){
            $this->setWarning("MSG301","(開始日)");
            return false;
        }

        if ($kind=="update" || $kind=="delete"){
            //リストより選択していない
            if ($this->kojin_no=="" || $this->s_date==""){
                $this->setWarning("MSG308");
                return false;
            }
            //キー値が変更されていますかをチェック
            if ($this->field["KOJIN_NO"] != $this->kojin_no || $this->field["S_DATE"] != $this->s_date){
                $this->setWarning("MSG308","キー値は変更できません");
                return false;
            }

            //１レコード取得
            $row = knjtx005Query::getRow($this->field["KOJIN_NO"], $this->field["S_DATE"]);

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

            //１レコード取得
            $row = knjtx005Query::getRow($this->field["S_DATE"]);

            //重複チェック
            if ($this->cmd == "add" && is_array($row)){
                $this->setWarning("MSG918","(個人番号、開始日)");
                return false;
            }
            /*if (strlen($this->field["CHIJI_NAME"]) >120 ){
                $this->setWarning("MSG915","(知事名称は120バイトまでです。)");
                return false;
            } elseif (strlen($this->field["CHIJI_NAME"])==0){ 
                $this->setWarning("MSG301","(知事名称)");
                return false;
            }*/
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
            $this->kojin_no = $this->field["KOJIN_NO"];
            $this->s_date = $this->field["S_DATE"];
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
        if (!isset($this->s_date)) {
            $this->setWarning("MSG303");
            return;
        }

        $result = knjtx005Query::getDeleteQuery($this->field);
        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
            $this->field = array();
        }
    }
    function &insert()
    {
        $result = knjtx005Query::getInsertQuery($this->field);
        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
        }
        return $result;
    }

    //アップデート時の内部メソッド
    function &update()
    {
        $result = knjtx005Query::getUpdateQuery($this->field);
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
