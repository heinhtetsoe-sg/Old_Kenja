<?php

require_once('for_php7.php');

//定数定義
class knjz080kModel extends Model {
    var $field = array();
    var $cmd;
    var $appli_no;       //申込コード
    var $year;

    //メンバ変数の初期化
    function knjz080kModel()
    {
    }

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->appli_no);
        }

        $this->year = CTRL_YEAR;
        $this->cmd = VARS::request("cmd");

        if (VARS::get("APPLICATIONCD")) {
            $this->appli_no = sprintf("%04d",VARS::get("APPLICATIONCD"));
        }

        $this->field = array("YEAR"             => $this->year,
                             "APPLICATIONCD"    => VARS::post("APPLICATIONCD"),		//申込コード
                             "APPLICATIONNAME"  => VARS::post("APPLICATIONNAME"),	//申込名称
                             "APPLICATIONMONEY" => VARS::post("APPLICATIONMONEY"),  //金額
							//mk 2005.06.20 以下５行追加
							"BANKCD"			=> VARS::post("BANKCD"),            //銀行コード
							"BANK_BRANCHCD"		=> VARS::post("BANK_BRANCHCD"),     //支店コード
							"DEPOSIT_ITEM"		=> VARS::post("DEPOSIT_ITEM"),		//預金種目コード
							"ACCOUNTNO"			=> VARS::post("ACCOUNTNO"),			//口座番号
							"ACCOUNTNAME"		=> VARS::post("ACCOUNTNAME")		//口座名義
                             );
    }

    //入力項目チェック
    function validateInput($kind)
    {
        //空文字チェック
        if(strlen($this->field["APPLICATIONCD"]) == 0){
            $this->setWarning("MSG301","(申込コード)");
            return false;
        }
        if($kind == "insert" || $kind == "update") {
            if(strlen($this->field["APPLICATIONNAME"]) == 0){
                $this->setWarning("MSG301","(申込名称)");
                return false;
            }
        }            
        //バイト数チェック
        if (strlen($this->field["APPLICATIONNAME"]) > 60){
            $this->setWarning("MSG915","(申込名称は60バイトまでです。)");
            return false;
        }
        if(($kind=="update" || $kind=="delete") && $this->appli_no != $this->field["APPLICATIONCD"]){
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }

		//mk 2005.06.20 以下追加
        if (strlen($this->field["ACCOUNTNAME"]) > 48) {
            $this->setWarning("MSG901","口座名義　48バイトまで");
            return false;
        }
        
        //全角スペースを正規表現で検索できないため一度半角に置き換える
		/*
        $this->field["ACCOUNTNAME"] = str_replace("　"," ",$this->field["ACCOUNTNAME"]);
        if(!mbereg("^[ァ-ヶ\s]*$",$this->field["ACCOUNTNAME"])){
            $this->setWarning("MSG901","口座名義 すべて全角カナで入力してください");
            return false;
        }
		*/
		//mk 2005.06.22 全角文字のみ対応
		$str = mb_convert_encoding($this->field["ACCOUNTNAME"], "EUC-JP", "auto");
		if(preg_match('/[\x00-\x8E]/', $str)){
        	$this->setWarning("MSG901","口座名義 すべて全角文字で入力してください");
			return False;
		}
        //$this->field["ACCOUNTNAME"] = str_replace(" ","　",$this->field["ACCOUNTNAME"]);

        if (strlen($this->field["ACCOUNTNO"]) != 0) {
	        $this->field["ACCOUNTNO"] = sprintf("%07d", $this->field["ACCOUNTNO"]);
        }
		//ここまで

        //入力項目の１レコード取得
        $row = knjz080kQuery::getRow($this,2);
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
            knjz080kQuery::getInsertQuery($this->field);
            $this->appli_no = sprintf("%04d",$this->field["APPLICATIONCD"]);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel()
    {
        //入力チェック
        if($this->validateInput("update")){
            knjz080kQuery::getUpdateQuery($this->field);
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    function &getDeleteModel()
    {
        //入力チェック
        if($this->validateInput("delete")){
            knjz080kQuery::getDeleteQuery($this->field);
            $this->appli_no = "";
            $this->setMessage("MSG201");
        }
        return;
    }

    //前年度のデータをコピー
    function &getCopyModel()
    {
        //今年度のデータが存在するか確認
        $t_year = knjz080kQuery::CheckData($this->year);
        if(isset($t_year)){
           $this->setWarning('MSG203', "今年度のデータが存在します。");
           return false;
        }
        //前年度のデータが存在するか確認
        $pre_year = knjz080kQuery::CheckData($this->year - 1);
        if(!isset($pre_year)){
          $this->setWarning('MSG203', "前年データが存在しません。");
          return false;
        }

        knjz080kQuery::getCopyQuery($this);
        $this->setMessage("MSG201");
        return;
    }

    //変数をクリアします
    function clean()
    {
    }
}
?>
