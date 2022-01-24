<?php

require_once('for_php7.php');

//定数定義
class knjp705Model extends Model {
    var $field = array();
    var $cmd;
    var $collect_bank_cd;       //学校払込コード
    var $collect_bank_div;      //銀行種別

    //メンバ変数の初期化
    function knjp705Model()
    {
    }

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->collect_bank_cd);
        }

        $this->cmd = VARS::request("cmd");

        if (VARS::get("COLLECT_BANK_CD")) {
            $this->collect_bank_cd = sprintf("%04d",VARS::get("COLLECT_BANK_CD"));
        }
        if (VARS::get("COLLECT_BANK_DIV")) {
            $this->collect_bank_div = VARS::get("COLLECT_BANK_DIV");
        }

        $this->field = array("COLLECT_BANK_CD"      => VARS::post("COLLECT_BANK_CD"),		//学校払込コード
                             "COLLECT_BANK_NAME"    => VARS::post("COLLECT_BANK_NAME"),	    //学校払込名称
                             "COLLECT_BANK_DIV"     => VARS::post("COLLECT_BANK_DIV"),      //銀行種別
                             
							 "BANK_CD"			    => VARS::post("BANK_CD"),               //銀行コード
							 "BRAN_CHCD"	        => VARS::post("BRAN_CHCD"),             //支店コード
							 "BANK_DEPOSIT_ITEM"	=> VARS::post("BANK_DEPOSIT_ITEM"),		//預金種別
							 "BANK_ACCOUNTNO"		=> VARS::post("BANK_ACCOUNTNO"),		//口座番号
							 "BANK_ACCOUNTNAME"		=> VARS::post("BANK_ACCOUNTNAME"),		//口座名
                             
							 "YUUCYO_CD"	        => VARS::post("YUUCYO_CD"),             //口座記号
							 "YUUCYO_DEPOSIT_ITEM"	=> VARS::post("YUUCYO_DEPOSIT_ITEM"),	//預金種別
							 "YUUCYO_ACCOUNTNO"		=> VARS::post("YUUCYO_ACCOUNTNO"),		//口座番号
							 "YUUCYO_ACCOUNTNAME"	=> VARS::post("YUUCYO_ACCOUNTNAME"),	//口座名
                             
                             "ZIPCD"       			=> VARS::post("ZIPCD"),
                             "ADDR1"       			=> VARS::post("ADDR1"),
                             "ADDR2"       			=> VARS::post("ADDR2"),
                             "TELNO1"       		=> VARS::post("TELNO1"),
                             "TELNO2"       		=> VARS::post("TELNO2"),
                             "UPDATED"         		=> VARS::post("UPDATED"));
    }

    //入力項目チェック
    function validateInput($kind)
    {
        //空文字チェック
        if(strlen($this->field["COLLECT_BANK_CD"]) == 0){
            $this->setWarning("MSG301","(学校払込コード)");
            return false;
        }
        if(strlen($this->field["COLLECT_BANK_DIV"]) == 0){
            $this->setWarning("MSG301","(銀行種別)");
            return false;
        }
        
        if ($kind == "insert" || $kind == "update") {
            if(strlen($this->field["COLLECT_BANK_NAME"]) == 0){
                $this->setWarning("MSG301","(学校払込名称)");
                return false;
            }
            //銀行用
            if ($this->field["COLLECT_BANK_DIV"] === '1') {
                if(strlen($this->field["BANK_CD"]) == 0){
                    $this->setWarning("MSG301","(銀行コード)");
                    return false;
                }
                if(strlen($this->field["BRAN_CHCD"]) == 0){
                    $this->setWarning("MSG301","(支店コード)");
                    return false;
                }
                if(strlen($this->field["BANK_DEPOSIT_ITEM"]) == 0){
                    $this->setWarning("MSG301","(預金種別)");
                    return false;
                }
                if(strlen($this->field["BANK_ACCOUNTNO"]) == 0){
                    $this->setWarning("MSG301","(口座番号)");
                    return false;
                }
                if(strlen($this->field["BANK_ACCOUNTNAME"]) == 0){
                    $this->setWarning("MSG301","(口座名)");
                    return false;
                }
            //郵便局用
            } else {
                if(strlen($this->field["YUUCYO_CD"]) == 0){
                    $this->setWarning("MSG301","(口座記号)");
                    return false;
                }
                if(strlen($this->field["YUUCYO_DEPOSIT_ITEM"]) == 0){
                    $this->setWarning("MSG301","(預金種別)");
                    return false;
                }
                if(strlen($this->field["YUUCYO_ACCOUNTNO"]) == 0){
                    $this->setWarning("MSG301","(口座番号)");
                    return false;
                }
                if(strlen($this->field["YUUCYO_ACCOUNTNAME"]) == 0){
                    $this->setWarning("MSG301","(口座名)");
                    return false;
                }
            }
        }            
        //バイト数チェック
        if (strlen($this->field["COLLECT_BANK_NAME"]) > 60){
            $this->setWarning("MSG915","(学校払込名称は60バイトまでです。)");
            return false;
        }
        if (strlen($this->field["ZIPCD"]) > 8){
            $this->setWarning("MSG915","(郵便番号は8バイトまでです。)");
            return false;
        }
        if (strlen($this->field["ADDR1"]) > 150){
            $this->setWarning("MSG915","(住所1は150バイトまでです。)");
            return false;
        }
        if (strlen($this->field["ADDR2"]) > 90){
            $this->setWarning("MSG915","(住所2は90バイトまでです。)");
            return false;
        }
        if (strlen($this->field["TELNO1"]) > 14){
            $this->setWarning("MSG915","(電話番号1は14バイトまでです。)");
            return false;
        }
        if (strlen($this->field["TELNO2"]) > 14){
            $this->setWarning("MSG915","(電話番号2は14バイトまでです。)");
            return false;
        }
        
        if (($kind=="update" || $kind=="delete") && $this->collect_bank_cd != $this->field["COLLECT_BANK_CD"] && $this->collect_bank_div != $this->field["COLLECT_BANK_DIV"]){
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }

        //銀行用
        if ($this->field["COLLECT_BANK_DIV"] === '1') {
            if (strlen($this->field["BANK_ACCOUNTNO"]) > 7) {
                $this->setWarning("MSG901","口座番号　7バイトまで");
                return false;
            }
            if (strlen($this->field["BANK_ACCOUNTNAME"]) > 60) {
                $this->setWarning("MSG901","口座名　60バイトまで");
                return false;
            }
        } else {
            if (strlen($this->field["YUUCYO_CD"]) > 5) {
                $this->setWarning("MSG901","口座記号　5バイトまで");
                return false;
            }
            if (strlen($this->field["YUUCYO_ACCOUNTNO"]) > 8) {
                $this->setWarning("MSG901","口座番号　8バイトまで");
                return false;
            }
            if (strlen($this->field["YUUCYO_ACCOUNTNAME"]) > 60) {
                $this->setWarning("MSG901","口座名　60バイトまで");
                return false;
            }
        }
        
        //全角スペースを正規表現で検索できないため一度半角に置き換える
		/*
        $this->field["BANK_ACCOUNTNAME"] = str_replace("　"," ",$this->field["BANK_ACCOUNTNAME"]);
        if(!mbereg("^[ァ-ヶ\s]*$",$this->field["BANK_ACCOUNTNAME"])){
            $this->setWarning("MSG901","口座名義 すべて全角カナで入力してください");
            return false;
        }
		*/
		//全角文字のみ対応
		$str = mb_convert_encoding($this->field["BANK_ACCOUNTNAME"], "EUC-JP", "auto");
		if(preg_match('/[\x00-\x8E]/', $str)){
        	$this->setWarning("MSG901","口座名 すべて全角文字で入力してください");
			return False;
		}
		$str = mb_convert_encoding($this->field["YUUCYO_ACCOUNTNAME"], "EUC-JP", "auto");
		if(preg_match('/[\x00-\x8E]/', $str)){
        	$this->setWarning("MSG901","口座名 すべて全角文字で入力してください");
			return False;
		}

        if (strlen($this->field["BANK_ACCOUNTNO"]) != 0) {
	        $this->field["BANK_ACCOUNTNO"] = sprintf("%07d", $this->field["BANK_ACCOUNTNO"]);
        }
        if (strlen($this->field["YUUCYO_CD"]) != 0) {
	        $this->field["YUUCYO_CD"] = sprintf("%05d", $this->field["YUUCYO_CD"]);
        }
        if (strlen($this->field["YUUCYO_ACCOUNTNO"]) != 0) {
	        $this->field["YUUCYO_ACCOUNTNO"] = sprintf("%08d", $this->field["YUUCYO_ACCOUNTNO"]);
        }

        //入力項目の１レコード取得
        $row = knjp705Query::getRow($this,2);
        //重複チェック
        if ($kind == "insert" && is_array($row)){
            $this->setWarning("MSG918");
            return false;
        }
        //存在チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        return true;
    }

    //追加処理
    function &getInsertModel()
    {
        //入力チェック
        if($this->validateInput("insert")){
            knjp705Query::getInsertQuery($this->field);
            $this->collect_bank_cd = sprintf("%04d",$this->field["COLLECT_BANK_CD"]);
            $this->collect_bank_div = $this->field["COLLECT_BANK_DIV"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel()
    {
        //入力チェック
        if($this->validateInput("update")){
            knjp705Query::getUpdateQuery($this->field);
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    function &getDeleteModel()
    {
        //入力チェック
        if($this->validateInput("delete")){
            knjp705Query::getDeleteQuery($this->field);
            $this->collect_bank_cd = "";
            $this->collect_bank_div = "";
            $this->field["BANK_CD"] = "";
            $this->setMessage("MSG201");
        }
        return;
    }

    //変数をクリアします
    function clean()
    {
    }
}
?>
