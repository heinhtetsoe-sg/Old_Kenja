<?php

require_once('for_php7.php');


class knjz091_2Model extends Model {
    var $field      = array();
    var $cmd;
    var $finschoolcd;        //primary

    /**
     * メンバ変数の初期化
     */
    function knjz091_2Model()
    {
    }
    function init()
    {              
        $this->field = array("PRISCHOOLCD"          =>  VARS::post("PRISCHOOLCD"),
                             "PRISCHOOL_NAME"       =>  VARS::post("PRISCHOOL_NAME"),
                             "PRISCHOOL_KANA"       =>  VARS::post("PRISCHOOL_KANA"),
                             "PRINCNAME"            =>  VARS::post("PRINCNAME"),
                             "PRINCNAME_SHOW"       =>  VARS::post("PRINCNAME_SHOW"),
                             "PRINCKANA"            =>  VARS::post("PRINCKANA"),
                             "DISTRICTCD"           =>  VARS::post("DISTRICTCD"),
                             "PRISCHOOL_ZIPCD"      =>  VARS::post("PRISCHOOL_ZIPCD"),
                             "PRISCHOOL_ADDR1"      =>  VARS::post("PRISCHOOL_ADDR1"),
                             "PRISCHOOL_ADDR2"      =>  VARS::post("PRISCHOOL_ADDR2"),
                             "PRISCHOOL_TELNO"      =>  VARS::post("PRISCHOOL_TELNO"),
                             "PRISCHOOL_FAXNO"      =>  VARS::post("PRISCHOOL_FAXNO"),
                             "GRP_PRISCHOOLCD"      =>  VARS::post("GRP_PRISCHOOLCD"),
#                             "EDBOARDCD"            =>  VARS::post("EDBOARDCD"),
                             "UPDATED"              =>  VARS::post("UPDATED")
                             );

        $this->cmd       = VARS::request("cmd");
        if (VARS::get("mode") == "1"){
            unset($this->finschoolcd);
        }
        $temp = VARS::get("PRISCHOOLCD");
        if (isset($temp)){
            $this->finschoolcd  = VARS::get("PRISCHOOLCD");
        }
    }

   //入力項目チェック
    function validateInput()
    {
        //キーのチェック
        if (trim($this->field["PRISCHOOLCD"]) == "" && $this->cmd == "add") {
            $this->setWarning('MSG901',"( 塾コード )");
            return false;
        }
        if ($this->finschoolcd  == "" && $this->cmd == "update") {
            $this->setWarning('MSG308');
            return false;
        }
        if ($this->cmd == "update" && $this->finschoolcd != $this->field["PRISCHOOLCD"]) {
            $this->setWarning('MSG308');
            return;
        }
        //コードのゼロ埋め
        if (strlen($this->field["PRISCHOOLCD"]) < 7){
            $this->field["PRISCHOOLCD"] = sprintf("%07d", $this->field["PRISCHOOLCD"]);
        }

        //１レコード取得
        $row = knjz091_2Query::getRow($this->field["PRISCHOOLCD"]);

        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning('MSG303');
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning('MSG918', "( 塾コード )");
            return false;
        }
        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]){
            $this->setWarning('MSG307');
            return false;
        }
        //文字型チェック
        foreach(array("PRISCHOOL_TELNO"   => array("[^-|0-9]",        "( 電話番号 )"),
                      "PRISCHOOL_FAXNO"   => array("[^-|0-9]",        "( FAX番号 )")
                ) as $key => $val
        ) {
            if ($this->field[$key] !="" && preg_match('{' . $val[0] . '}i', trim($this->field[$key]))) {
                $this->setWarning("MSG901", $val[1]);
                return false;
            }
        }
        if (strlen($this->field["PRISCHOOLCD"]) >7 ) {
            $this->setWarning('MSG901', "( 塾コード ) 7バイトまで");
            return false;
        } elseif (strlen($this->field["PRISCHOOL_NAME"])>75){ 
            $this->setWarning('MSG901', "( 塾名 ) 75バイトまで");
            return false;
        } elseif (strlen($this->field["PRISCHOOL_KANA"])>75){ 
            $this->setWarning('MSG901', "( 塾名かな ) 75バイトまで");
            return false;
        } elseif (strlen($this->field["PRINCNAME"])>60){ 
            $this->setWarning('MSG901', "( 塾長氏名 ) 60バイトまで");
            return false;
        } elseif (strlen($this->field["PRINCNAME_SHOW"])>30){ 
            $this->setWarning('MSG901', "( 塾長氏名表示用 ) 30バイトまで");
            return false;
        } elseif (strlen($this->field["PRINCKANA"])>120){ 
            $this->setWarning('MSG901', "( 塾長氏名カナ ) 120バイトまで");
            return false;
        } elseif (strlen($this->field["PRISCHOOL_ZIPCD"])>8){ 
            $this->setWarning('MSG901', "( 郵便番号 ) 8バイトまで");
            return false;
        } elseif (strlen($this->field["DISTRICTCD"])>2){ 
            $this->setWarning('MSG901', "( 地区コード ) 2バイトまで");
            return false;
        } elseif (strlen($this->field["PRISCHOOL_ADDR1"])>90){ 
            $this->setWarning('MSG901', "( 住所 ) 90バイトまで");
            return false;
        } elseif (strlen($this->field["PRISCHOOL_ADDR2"])>90){ 
            $this->setWarning('MSG901', "( 方書き(アパート名等) ) 90バイトまで");
            return false;
        } elseif (strlen($this->field["PRISCHOOL_TELNO"])>14){ 
            $this->setWarning('MSG901', "( 電話番号 ) 14バイトまで");
            return false;
        } elseif (strlen($this->field["PRISCHOOL_FAXNO"])>14){ 
            $this->setWarning('MSG901', "( FAX番号 ) 14バイトまで");
            return false;
#        } elseif (strlen($this->field["EDBOARDCD"])>6){ 
#            $this->setWarning('MSG901', "(教育委員会コード) 6バイトまで");
#            return false;
        }
        //塾グループ【塾コード（親）】が塾マスタに存在するか
        if (strlen($this->field["GRP_PRISCHOOLCD"])) {
            $row = knjz091_2Query::getRow($this->field["GRP_PRISCHOOLCD"]);
            if (($this->cmd == "add" || $this->cmd == "update") && !is_array($row)) {
                $this->setWarning('MSG303', "( 塾グループ )");
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
            $this->finschoolcd = $this->field["PRISCHOOLCD"];
            $this->setMessage('MSG201');
        }
    }

    // 削除用のモデルを返します
    function &getDeleteModel()
    {
        //キーの更新チェック
        if ($this->finschoolcd == "") {
            $this->setWarning('MSG308');
            return false;
        }
        if ($this->finschoolcd != $this->field["PRISCHOOLCD"]) {
            $this->setWarning('MSG308');
            return;
        }
        $row = knjz091_2Query::getRow($this->finschoolcd);
        if (!is_array($row)){
            $this->setWarning('MSG303');
            return;
        }
        if (knjz091_2Query::IsExisting($this->finschoolcd)){
            $this->setWarning('MSG919',"年度データに登録されています。");
            return;
        }
        knjz091_2Query::getDeleteQuery($this->field);
        $this->setMessage('MSG201');
    }
    
    function &insert()
    {
        knjz091_2Query::getInsertQuery($this->field);
    }

    //アップデート時の内部メソッド
    function &update()
    {
        knjz091_2Query::getUpdateQuery($this->field);

    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
