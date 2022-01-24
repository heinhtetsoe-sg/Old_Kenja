<?php

require_once('for_php7.php');
// kanji=漢字
// $Id: knjz420Model.inc 56591 2017-10-22 13:04:39Z maeshiro $
//定数定義
class knjz420Model extends Model {
    var $field      = array();
    var $cmd;
    var $company_cd;
    //メンバ変数の初期化
    function knjz420Model()
    {
    }
    function init()
    {
		$this->field = array("COMPANY_CD"		  =>  VARS::post("COMPANY_CD"),
							 "COMPANY_NAME"		  =>  VARS::post("COMPANY_NAME"),
							 "SHUSHOKU_ADDR"	  =>  VARS::post("SHUSHOKU_ADDR"),
							 "SHIHONKIN"		  =>  VARS::post("SHIHONKIN"),
							 "SONINZU"		      =>  VARS::post("SONINZU"),
							 "TONINZU"			  =>  VARS::post("TONINZU"),
							 "COMPANY_SORT"		  =>  VARS::post("COMPANY_SORT"),
							 "TARGET_SEX"		  =>  VARS::post("TARGET_SEX"),
							 "ZIPCD"			  =>  VARS::post("ZIPCD"),
							 "ADDR1"			  =>  VARS::post("ADDR1"),
							 "ADDR2"			  =>  VARS::post("ADDR2"),
							 "TELNO"			  =>  VARS::post("TELNO"),
							 "REMARK"			  =>  VARS::post("REMARK"),
							 );

        $this->cmd             = VARS::request("cmd");
       if (VARS::get("PROGRAMID")) {
           $this->company_cd = "";
       }
        if (VARS::get("COMPANY_CD")!="") {
            $this->company_cd    = VARS::get("COMPANY_CD");
        }
    }
    //入力項目チェック
    function validateInput($kind)
    {
        if (trim($this->field["COMPANY_CD"]) == ""){
            $this->setWarning("MSG301","(会社コード)");
            return false;
        }

        if ($kind=="update" || $kind=="delete"){
            //リストより選択していない
            if ($this->company_cd==""){
                $this->setWarning("MSG308");
                return false;
            }
            //キー値が変更されていますかをチェック
            if ($this->field["COMPANY_CD"] !=$this->company_cd){
                $this->setWarning("MSG308","キー値は変更できません");
                return false;
            }

            //コードのゼロ埋め
            $this->field["COMPANY_CD"] = sprintf("%08d",$this->field["COMPANY_CD"]);
            //１レコード取得
            $row = knjz420Query::getRow($this->field["COMPANY_CD"]);

            //存在チェック
            if (!is_array($row)){
                $this->setWarning("MSG303");
                return false;
            }
        }
        if ($kind=="update" || $kind=="insert"){

            //コードのゼロ埋め
            $this->field["COMPANY_CD"] = sprintf("%08d",$this->field["COMPANY_CD"]);
            //１レコード取得
            $row = knjz420Query::getRow($this->field["COMPANY_CD"]);

            //重複チェック
            if ($this->cmd == "add" && is_array($row)){
                $this->setWarning("MSG918","(会社コード)");
                return false;
            }
            if (strlen($this->field["COMPANY_NAME"]) > 120){
                $this->setWarning("MSG915","(会社名は120バイトまでです。)");
                return false;
            } elseif (strlen($this->field["SHUSHOKU_ADDR"]) > 120){ 
                $this->setWarning("MSG915","(就業場所は120バイトまでです。)");
                return false;
            } elseif (strlen($this->field["SHIHONKIN"]) > 17){ 
                $this->setWarning("MSG915","(資本金は17バイトまでです。)");
                return false;
            } elseif (strlen($this->field["SONINZU"]) > 8){ 
                $this->setWarning("MSG915","(全体人数は8バイトまでです。)");
                return false;
            } elseif (strlen($this->field["TONINZU"]) > 8){ 
                $this->setWarning("MSG915","(当事務所人数は8バイトまでです。)");
                return false;
            } elseif (strlen($this->field["ZIPCD"]) > 8){ 
                $this->setWarning("MSG915","(郵便番号は8バイトまでです。)");
                return false;
            } elseif (strlen($this->field["ADDR1"]) > 90){ 
                $this->setWarning("MSG915","(住所１は90バイトまでです。)");
                return false;
            } elseif (strlen($this->field["ADDR2"]) > 90){ 
                $this->setWarning("MSG915","(住所２は90バイトまでです。)");
                return false;
            } elseif (strlen($this->field["TELNO"]) > 16){ 
                $this->setWarning("MSG915","(電話番号は16バイトまでです。)");
                return false;
            } elseif (strlen($this->field["REMARK"]) > 120){ 
                $this->setWarning("MSG915","(備考は120バイトまでです。)");
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
            $this->company_cd = $this->field["COMPANY_CD"];
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
        if (!isset($this->company_cd)) {
            $this->setWarning("MSG303");
            return;
        }
        $result = knjz420Query::getDeleteQuery($this->field);
        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
            $this->field = array();
        }
    }
    function &insert()
    {
        $result = knjz420Query::getInsertQuery($this->field);
        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
        }
        return $result;
    }

    //アップデート時の内部メソッド
    function &update()
    {
        $result = knjz420Query::getUpdateQuery($this->field);
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
