<?php

require_once('for_php7.php');


class knjz090_2Model extends Model {
    var $field      = array();
    var $cmd;
    var $finschoolcd;        //primary

    /**
     * メンバ変数の初期化
     */
    function knjz090_2Model()
    {
    }
    function init()
    {              
        $this->field = array("FINSCHOOLCD"          =>  VARS::post("FINSCHOOLCD"),
                             "FINSCHOOL_DISTCD"     =>  VARS::post("FINSCHOOL_DISTCD"),
                             "FINSCHOOL_NAME"       =>  VARS::post("FINSCHOOL_NAME"),
                             "FINSCHOOL_KANA"       =>  VARS::post("FINSCHOOL_KANA"),
                             "PRINCNAME"            =>  VARS::post("PRINCNAME"),
                             "PRINCNAME_SHOW"       =>  VARS::post("PRINCNAME_SHOW"),
                             "PRINCKANA"            =>  VARS::post("PRINCKANA"),
                             "DISTRICTCD"           =>  VARS::post("DISTRICTCD"),
                             "FINSCHOOL_ZIPCD"      =>  VARS::post("FINSCHOOL_ZIPCD"),
                             "FINSCHOOL_ADDR1"      =>  VARS::post("FINSCHOOL_ADDR1"),
                             "FINSCHOOL_ADDR2"      =>  VARS::post("FINSCHOOL_ADDR2"),
                             "FINSCHOOL_TELNO"      =>  VARS::post("FINSCHOOL_TELNO"),
                             "FINSCHOOL_FAXNO"      =>  VARS::post("FINSCHOOL_FAXNO"),
                             "EDBOARDCD"            =>  VARS::post("EDBOARDCD"),
                             "UPDATED"              =>  VARS::post("UPDATED")
                             );

        $this->cmd       = VARS::request("cmd");
        if (VARS::get("mode") == "1"){
            unset($this->finschoolcd);
        }
        $temp = VARS::get("FINSCHOOLCD");
        if (isset($temp)){
            $this->finschoolcd  = VARS::get("FINSCHOOLCD");
        }
    }

   //入力項目チェック
    function validateInput()
    {
        //キーのチェック
        if (trim($this->field["FINSCHOOLCD"]) == "" && $this->cmd == "add") {
            $this->setWarning('MSG901',"( 学校コード )");
            return false;
        }
        if ($this->finschoolcd  == "" && $this->cmd == "update") {
            $this->setWarning('MSG308');
            return false;
        }
        if ($this->cmd == "update" && $this->finschoolcd != $this->field["FINSCHOOLCD"]) {
            $this->setWarning('MSG308');
            return;
        }
        //コードのゼロ埋め
        if (strlen($this->field["FINSCHOOLCD"]) < 7){
            $this->field["FINSCHOOLCD"] = sprintf("%07d", $this->field["FINSCHOOLCD"]);
        }

        //１レコード取得
        $row = knjz090_2Query::getRow($this->field["FINSCHOOLCD"]);

        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning('MSG303');
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning('MSG918', "( 学校コード )");
            return false;
        }
        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]){
            $this->setWarning('MSG307');
            return false;
        }
        if (strlen($this->field["FINSCHOOLCD"]) > 7) {
            $this->setWarning('MSG901', "( 学校コード ) 7バイトまで");
            return false;
        } elseif (strlen($this->field["FINSCHOOL_NAME"])>75){ 
            $this->setWarning('MSG901', "( 学校名 ) 75バイトまで");
            return false;
        } elseif (strlen($this->field["FINSCHOOL_KANA"])>75){ 
            $this->setWarning('MSG901', "( 学校名カナ) 75バイトまで");
            return false;
        } elseif (strlen($this->field["PRINCNAME"])>60){ 
            $this->setWarning('MSG901', "( 学校長氏名 ) 60バイトまで");
            return false;
        } elseif (strlen($this->field["PRINCNAME_SHOW"])>30){ 
            $this->setWarning('MSG901', "( 学校長氏名表示用 ) 30バイトまで");
            return false;
        } elseif (strlen($this->field["PRINCKANA"])>120){ 
            $this->setWarning('MSG901', "( 学校長氏名カナ ) 120バイトまで");
            return false;
        } elseif (strlen($this->field["FINSCHOOL_ZIPCD"])>8){ 
            $this->setWarning('MSG901', "( 郵便番号 ) 8バイトまで");
            return false;
        } elseif (strlen($this->field["DISTRICTCD"])>2){ 
            $this->setWarning('MSG901', "( 地区コード ) 2バイトまで");
            return false;
        } elseif (strlen($this->field["FINSCHOOL_ADDR1"])>75){ 
            $this->setWarning('MSG901', "( 住所1 ) 75バイトまで");
            return false;
        } elseif (strlen($this->field["FINSCHOOL_ADDR2"])>75){ 
            $this->setWarning('MSG901', "( 住所2 ) 75バイトまで");
            return false;
        } elseif (strlen($this->field["FINSCHOOL_TELNO"])>14){ 
            $this->setWarning('MSG901', "( TEL番号 ) 14バイトまで");
            return false;
        } elseif (strlen($this->field["FINSCHOOL_FAXNO"])>14){ 
            $this->setWarning('MSG901', "( FAX番号 ) 14バイトまで");
            return false;
        } elseif (strlen($this->field["EDBOARDCD"])>6){ 
            $this->setWarning('MSG901', "( 教育委員会コード ) 6バイトまで");
            return false;
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
            $this->finschoolcd = $this->field["FINSCHOOLCD"];
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
        if ($this->finschoolcd != $this->field["FINSCHOOLCD"]) {
            $this->setWarning('MSG308');
            return;
        }
        $row = knjz090_2Query::getRow($this->finschoolcd);
        if (!is_array($row)){
            $this->setWarning('MSG303');
            return;
        }
        if (knjz090_2Query::IsExisting($this->finschoolcd)){
            $this->setWarning('MSG919',"年度データに登録されています。");
            return;
        }
        knjz090_2Query::getDeleteQuery($this->field);
        $this->setMessage('MSG201');
    }
    
    function &insert()
    {
        knjz090_2Query::getInsertQuery($this->field);
    }

    //アップデート時の内部メソッド
    function &update()
    {
        knjz090_2Query::getUpdateQuery($this->field);

    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
