<?php

require_once('for_php7.php');


class knjz050kModel extends Model {
    var $field      = array();
    var $cmd;
    var $cd;        //primary
    var $bankcmb;   //NO001

    /**
     * メンバ変数の初期化
     */
    function knjz050kModel()
    {
    }

    function init()
    {              
        if (VARS::get("PROGRAMID")) {
            $this->cd = "";
            $this->bankcmb = "";//NO001
        }
        $this->field = array("BANKCD"          =>  VARS::post("BANKCD"),
                             "BRANCHCD"        =>  VARS::post("BRANCHCD"),
                             "BANKNAME"        =>  VARS::post("BANKNAME"),
                             "BANKNAME_KANA"   =>  VARS::post("BANKNAME_KANA"),
                             "BRANCHNAME"      =>  VARS::post("BRANCHNAME"),
                             "BRANCHNAME_KANA" =>  VARS::post("BRANCHNAME_KANA"),
                             "BANKZIPCD"       =>  VARS::post("BANKZIPCD"),
                             "BANKADDR1"       =>  VARS::post("BANKADDR1"),
                             "BANKADDR2"       =>  VARS::post("BANKADDR2"),
                             "BANKTELNO"       =>  VARS::post("BANKTELNO"),
                             "BANKFAXNO"       =>  VARS::post("BANKFAXNO"),
                             "EDBOARDCD"       =>  VARS::post("EDBOARDCD"),
                             "UPDATED"         =>  VARS::post("UPDATED"));

        $this->cmd       = VARS::request("cmd");
        if (VARS::get("CD")!=""){
            $this->cd  = VARS::get("CD");
        }
        //NO001
        if (VARS::post("BANKCMB")!=""){
            $this->bankcmb  = VARS::post("BANKCMB");
        }
        //銀行が変わった時に初期化---NO001
        if ($this->cmd == "change") {
            $this->cd = "";
        }
    }

   //入力項目チェック
    function validateInput()
    {
        //キーのチェック
        if (trim($this->field["BANKCD"]) == "" || trim($this->field["BRANCHCD"]) == "") {
            $this->setWarning('MSG301',"( 銀行コードまたは支店コード )");
            return false;
        }
        if ($this->cd  == "" && $this->cmd == "update") {
            $this->setWarning('MSG308');
            return false;
        }
        if ($this->cmd == "update" && $this->cd != $this->field["BANKCD"]."-".$this->field["BRANCHCD"]) {
            $this->setWarning('MSG308');
            return;
        }
        //コードのゼロ埋め
        $this->field["BANKCD"]   = sprintf("%04d", $this->field["BANKCD"]);
        $this->field["BRANCHCD"] = sprintf("%03d", $this->field["BRANCHCD"]);

        //１レコード取得
        $row = knjz050kQuery::getRow($this->cd);

        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning('MSG303');
            return false;
        }

        $row = knjz050kQuery::getRow($this->field["BANKCD"]."-".$this->field["BRANCHCD"]);
        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning('MSG918');
            return false;
        }
        if (strlen($this->field["BANKCD"]) > 4) {
            $this->setWarning('MSG901', "( 銀行コード ) 7バイトまで");
            return false;
        } elseif (strlen($this->field["BRANCHCD"])>3){ 
            $this->setWarning('MSG901', "( 支店コード ) 3バイトまで");
            return false;
        } elseif (strlen($this->field["BANKNAME"])>45){ 
            $this->setWarning('MSG901', "( 銀行名 ) 45バイトまで");
            return false;
        } elseif (strlen($this->field["BANKNAME_KANA"])>45){ 
            $this->setWarning('MSG901', "( 銀行名カナ ) 45バイトまで");
            return false;
        } elseif (strlen($this->field["BRANCHNAME"])>45){ 
            $this->setWarning('MSG901', "( 支店名 ) 45バイトまで");
            return false;
        } elseif (strlen($this->field["BRANCHNAME_KANA"])>45){ 
            $this->setWarning('MSG901', "( 支店名カナ ) 45バイトまで");
            return false;
        } elseif (strlen($this->field["BANKZIPCD"])>8){ 
            $this->setWarning('MSG901', "( 郵便番号 ) 8バイトまで");
            return false;
        } elseif (strlen($this->field["BANKADDR1"])>75){ 
            $this->setWarning('MSG901', "( 住所1 ) 75バイトまで");
            return false;
        } elseif (strlen($this->field["BANKADDR2"])>75){ 
            $this->setWarning('MSG901', "( 住所2 ) 75バイトまで");
            return false;
        } elseif (strlen($this->field["BANKTELNO"])>14){ 
            $this->setWarning('MSG901', "( TEL番号 ) 14バイトまで");
            return false;
        } elseif (strlen($this->field["BANKFAXNO"])>14){ 
            $this->setWarning('MSG901', "( FAX番号 ) 14バイトまで");
            return false;
        }
        if (preg_match("/[^-0-9]/",$this->field["BANKTELNO"])) {
            $this->setWarning('MSG901', "( TEL番号 ) 半角文字数字");
            return false;
        }
        if (preg_match("/[^-0-9]/",$this->field["BANKFAXNO"])) {
            $this->setWarning('MSG901', "( FAX番号 ) 半角文字数字");
            return false;
        }
        //全角スペースを正規表現で検索できないため一度半角に置き換える
        $this->field["BANKNAME_KANA"] = str_replace("　"," ",$this->field["BANKNAME_KANA"]);
//2005.11.29 alp m-yama
//        if(!mbereg("^[ァ-ヶー\s]*$",$this->field["BANKNAME_KANA"])){
        if(!mbereg("^[ｦ-ﾟ\s]*$",$this->field["BANKNAME_KANA"])){
            $this->setWarning("MSG901","銀行名カナ すべて半角カナで入力してください");
            return false;
        }
        $this->field["BANKNAME_KANA"] = str_replace(" ","　",$this->field["BANKNAME_KANA"]);

        //全角スペースを正規表現で検索できないため一度半角に置き換える
        $this->field["BRANCHNAME_KANA"] = str_replace("　"," ",$this->field["BRANCHNAME_KANA"]);
//2005.11.29 alp m-yama
//        if(!mbereg("^[ァ-ヶー\s]*$",$this->field["BANKNAME_KANA"])){
        if(!mbereg("^[ｦ-ﾟ\s]*$",$this->field["BRANCHNAME_KANA"])){
            $this->setWarning("MSG901","支店名カナ すべて半角カナで入力してください");
            return false;
        }
        $this->field["BRANCHNAME_KANA"] = str_replace(" ","　",$this->field["BRANCHNAME_KANA"]);

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
            $this->setMessage('MSG201');
        }    
    }

    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->insert();
            $this->cd = $this->field["BANKCD"]."-".$this->field["BRANCHCD"];
            $this->bankcmb = $this->field["BANKCD"];//NO001
            $this->setMessage('MSG201');
        }
    }

    // 削除用のモデルを返します
    function &getDeleteModel()
    {
        //キーの更新チェック
        if ($this->cd == "") {
            $this->setWarning('MSG308');
            return false;
        }
        if ($this->cd != $this->field["BANKCD"]."-".$this->field["BRANCHCD"]) {
            $this->setWarning('MSG308');
            return;
        }
        $row = knjz050kQuery::getRow($this->cd);
        if (!is_array($row)){
            $this->setWarning('MSG303');
            return;
        }
        knjz050kQuery::getDeleteQuery($this->field);
        $this->setMessage('MSG201');
    }
    
    function &insert()
    {
        knjz050kQuery::getInsertQuery($this->field);
    }

    //アップデート時の内部メソッド
    function &update()
    {
        knjz050kQuery::getUpdateQuery($this->field);

    }
}
?>
