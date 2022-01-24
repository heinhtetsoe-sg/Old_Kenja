<?php

require_once('for_php7.php');


class knjz091a_3Model extends Model {
    var $field      = array();
    var $cmd;
    var $prischoolClassCd;
    var $prischoolCd;

    /**
     * メンバ変数の初期化
     */
    function knjz091a_3Model()
    {
    }
    function init()
    {              
        $this->field = array("PRISCHOOL_CLASS_CD"           =>  VARS::post("PRISCHOOL_CLASS_CD"),
                             "PRISCHOOL_NAME"               =>  VARS::post("PRISCHOOL_NAME"),
                             "PRISCHOOL_KANA"               =>  VARS::post("PRISCHOOL_KANA"),
                             "PRINCNAME"                    =>  VARS::post("PRINCNAME"),
                             "PRINCNAME_SHOW"               =>  VARS::post("PRINCNAME_SHOW"),
                             "PRINCKANA"                    =>  VARS::post("PRINCKANA"),
                             "DISTRICTCD"                   =>  VARS::post("DISTRICTCD"),
                             "PRISCHOOL_ZIPCD"              =>  VARS::post("PRISCHOOL_ZIPCD"),
                             "PRISCHOOL_ADDR1"              =>  VARS::post("PRISCHOOL_ADDR1"),
                             "PRISCHOOL_ADDR2"              =>  VARS::post("PRISCHOOL_ADDR2"),
                             "PRISCHOOL_TELNO"              =>  VARS::post("PRISCHOOL_TELNO"),
                             "PRISCHOOL_FAXNO"              =>  VARS::post("PRISCHOOL_FAXNO"),
                             "HIDDEN_ROSEN_1"               =>  VARS::post("HIDDEN_ROSEN_1"),
                             "HIDDEN_ROSEN_2"               =>  VARS::post("HIDDEN_ROSEN_2"),
                             "HIDDEN_ROSEN_3"               =>  VARS::post("HIDDEN_ROSEN_3"),
                             "HIDDEN_ROSEN_4"               =>  VARS::post("HIDDEN_ROSEN_4"),
                             "HIDDEN_ROSEN_5"               =>  VARS::post("HIDDEN_ROSEN_5"),
                             "NEAREST_STATION_NAME1"        =>  VARS::post("NEAREST_STATION_NAME1"),
                             "NEAREST_STATION_KANA1"        =>  VARS::post("NEAREST_STATION_KANA1"),
                             "NEAREST_STATION_NAME2"        =>  VARS::post("NEAREST_STATION_NAME2"),
                             "NEAREST_STATION_KANA2"        =>  VARS::post("NEAREST_STATION_KANA2"),
                             "DIRECT_MAIL_FLG"              =>  VARS::post("DIRECT_MAIL_FLG"),
                             "UPDATED"                      =>  VARS::post("UPDATED"),

                             //Rosen.phpからのデータ
                             "AREA_SENTAKU"                 =>  VARS::request("AREA_SENTAKU"),    //エリア選択コンボ
                             "ROSEN_SELECT"                 =>  VARS::request("ROSEN_SELECT")     //路線選択コンボ
                             );

        $this->cmd = VARS::request("cmd");
        if (VARS::get("mode") == "1" || VARS::get("PRISCHOOLCD")){
            unset($this->prischoolCd);
            unset($this->prischoolClassCd);
        }

        $temp1 = VARS::get("PRISCHOOLCD");
        if (isset($temp1)){
            $this->prischoolCd  = VARS::get("PRISCHOOLCD");
        }

        $temp2 = VARS::get("PRISCHOOL_CLASS_CD");
        if (isset($temp2)){
            $this->prischoolClassCd  = VARS::get("PRISCHOOL_CLASS_CD");
        }
    }

   //入力項目チェック
    function validateInput()
    {
        //キーのチェック
        if (trim($this->prischoolCd) == "" && $this->cmd == "add") {
            $this->setWarning('MSG901',"( 塾コード )");
            return false;
        }
        if (trim($this->field["PRISCHOOL_CLASS_CD"]) == "" && $this->cmd == "add") {
            $this->setWarning('MSG901',"( 教室コード )");
            return false;
        }
        if ($this->prischoolClassCd  == "" && $this->cmd == "update") {
            $this->setWarning('MSG308');
            return false;
        }
        if ($this->cmd == "update" && $this->prischoolClassCd != $this->field["PRISCHOOL_CLASS_CD"]) {
            $this->setWarning('MSG308');
            return;
        }
        //コードのゼロ埋め
        if (strlen($this->field["PRISCHOOL_CLASS_CD"]) < 7){
            $this->field["PRISCHOOL_CLASS_CD"] = sprintf("%07d", $this->field["PRISCHOOL_CLASS_CD"]);
        }

        //１レコード取得
        $row = knjz091a_3Query::getRow($this, $this->field["PRISCHOOL_CLASS_CD"]);

        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning('MSG303');
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning('MSG918', "( 教室コード )");
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
        if (strlen($this->field["PRISCHOOL_CLASS_CD"]) >7 ) {
            $this->setWarning('MSG901', "( 教室コード ) 7バイトまで");
            return false;
        } elseif (strlen($this->field["PRISCHOOL_NAME"])>75){ 
            $this->setWarning('MSG901', "( 教室名 ) 75バイトまで");
            return false;
        } elseif (strlen($this->field["PRISCHOOL_KANA"])>75){ 
            $this->setWarning('MSG901', "( 教室名かな ) 75バイトまで");
            return false;
        } elseif (strlen($this->field["PRINCNAME"])>60){ 
            $this->setWarning('MSG901', "( 校舎長氏名 ) 60バイトまで");
            return false;
        } elseif (strlen($this->field["PRINCNAME_SHOW"])>30){ 
            $this->setWarning('MSG901', "( 校舎長氏名表示用 ) 30バイトまで");
            return false;
        } elseif (strlen($this->field["PRINCKANA"])>120){ 
            $this->setWarning('MSG901', "( 校舎長氏名カナ ) 120バイトまで");
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
        } elseif (strlen($this->field["NEAREST_STATION_NAME1"])>75){ 
            $this->setWarning('MSG901', "( 最寄駅 駅名１ ) 75バイトまで");
            return false;
        } elseif (strlen($this->field["NEAREST_STATION_KANA1"])>75){ 
            $this->setWarning('MSG901', "( 最寄駅 かな１ ) 75バイトまで");
            return false;
        } elseif (strlen($this->field["NEAREST_STATION_NAME2"])>75){ 
            $this->setWarning('MSG901', "( 最寄駅 駅名２ ) 75バイトまで");
            return false;
        } elseif (strlen($this->field["NEAREST_STATION_KANA2"])>75){ 
            $this->setWarning('MSG901', "( 最寄駅 かな２ ) 75バイトまで");
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
            $this->prischoolClassCd = $this->field["PRISCHOOL_CLASS_CD"];
            $this->setMessage('MSG201');
        }
    }

    // 削除用のモデルを返します
    function &getDeleteModel()
    {
        //キーの更新チェック
        if ($this->prischoolClassCd == "") {
            $this->setWarning('MSG308');
            return false;
        }
        if ($this->prischoolClassCd != $this->field["PRISCHOOL_CLASS_CD"]) {
            $this->setWarning('MSG308');
            return;
        }
        $row = knjz091a_3Query::getRow($this, $this->field["PRISCHOOL_CLASS_CD"]);
        if (!is_array($row)){
            $this->setWarning('MSG303');
            return;
        }
        knjz091a_3Query::getDeleteQuery($this, $this->field);
        $this->setMessage('MSG201');
    }
    
    function &insert()
    {
        knjz091a_3Query::getInsertQuery($this, $this->field);
    }

    //アップデート時の内部メソッド
    function &update()
    {
        knjz091a_3Query::getUpdateQuery($this, $this->field);

    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
