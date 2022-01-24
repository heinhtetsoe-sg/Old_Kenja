<?php

require_once('for_php7.php');

class knjl398qModel extends Model {
    var $field = array();
    var $cmd;
    var $finschoolcd;        //primary
    var $selectFinschoolType;
    var $file;

    /**
     * メンバ変数の初期化
     */
    function knjl398qModel() {}

    function init() {
        $this->field = array("FINSCHOOLCD"         => VARS::post("FINSCHOOLCD"),         //学校コード
                             "FINSCHOOL_TYPE"      => VARS::post("FINSCHOOL_TYPE"),      //職種
                             "FINSCHOOL_DISTCD"    => VARS::post("FINSCHOOL_DISTCD"),    //学校立コード
                             "FINSCHOOL_DISTCD2"   => VARS::post("FINSCHOOL_DISTCD2"),   //学区コード
                             "FINSCHOOL_DIV"       => VARS::post("FINSCHOOL_DIV"),       //学校種別
                             "FINSCHOOL_NAME"      => VARS::post("FINSCHOOL_NAME"),      //学校名
                             "FINSCHOOL_KANA"      => VARS::post("FINSCHOOL_KANA"),      //学校名かな
                             "FINSCHOOL_NAME_ABBV" => VARS::post("FINSCHOOL_NAME_ABBV"), //学校名略称
                             "FINSCHOOL_KANA_ABBV" => VARS::post("FINSCHOOL_KANA_ABBV"), //学校名かな略称
                             "PRINCNAME"           => VARS::post("PRINCNAME"),           //学校長氏名
                             "PRINCNAME_SHOW"      => VARS::post("PRINCNAME_SHOW"),      //学校長氏名表示用
                             "PRINCKANA"           => VARS::post("PRINCKANA"),           //学校長氏名かな
                             "DISTRICTCD"          => VARS::post("DISTRICTCD"),          //地区コード
                             "FINSCHOOL_PREF_CD"   => VARS::post("FINSCHOOL_PREF_CD"),   //都道府県
                             "FINSCHOOL_ZIPCD"     => VARS::post("FINSCHOOL_ZIPCD"),     //郵便番号
                             "FINSCHOOL_ADDR1"     => VARS::post("FINSCHOOL_ADDR1"),     //住所
                             "FINSCHOOL_ADDR2"     => VARS::post("FINSCHOOL_ADDR2"),     //住所
                             "FINSCHOOL_TELNO"     => VARS::post("FINSCHOOL_TELNO"),     //電話番号
                             "FINSCHOOL_FAXNO"     => VARS::post("FINSCHOOL_FAXNO"),     //FAX番号
                             "EDBOARDCD"           => VARS::post("EDBOARDCD"),           //教育委員会コード
                             "TOHAIGO_CHK"         => VARS::post("TOHAIGO_CHK"),         //統廃合チェック
                             "UPDATED"             => VARS::post("UPDATED"),             //
                             );
        if($this->field["FINSCHOOL_TYPE"] != ""){
            if($this->field["FINSCHOOL_TYPE"] == "3"){
                $this->field["FINSCHOOLCD"] = "20".VARS::post("FINSCHOOLCD");
            }else{
                $this->field["FINSCHOOLCD"] = "30".VARS::post("FINSCHOOLCD");
            }
        }

        $this->cmd = VARS::request("cmd");

        if ($this->cmd == "changeType") {
            $this->selectFinschoolType = VARS::post("SELECT_FINSCHOOL_TYPE");
            $this->selectPrefCd        = VARS::post("SELECT_PREFCD");
        }else if($this->cmd == ""){
            //初期化
            unset($this->selectFinschoolType);
            unset($this->selectPrefCd);
            unset($this->finschoolcd);
        }


        if (VARS::get("mode") == "1"){
            unset($this->finschoolcd);
            unset($this->selectFinschoolType);
        }
        $temp = VARS::get("FINSCHOOLCD");
        if (isset($temp)){
            $this->finschoolcd  = VARS::get("FINSCHOOLCD");
        }
    }

    //CVSファイル
    //サーバ ⇒ クライアント
    function getDownloadCsvModel() 
    {
        $contents = "学校コード,校種,学校名,学校種別,学校名略称,統廃合\n";
        
        $db = Query::dbCheckOut();
        
        $query = knjl398qQuery::selectQuery($this, "1");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            
            $tohaigo_chk = $row["TOHAIGO_CHK"] != "" ? "1" : "";
            
            $contents .= $row["FINSCHOOLCD"].",".$row["FINSCHOOL_TYPE"].",".$row["FINSCHOOL_NAME"].",".$row["FINSCHOOL_DIV"].",".$row["FINSCHOOL_NAME_ABBV"].",".$tohaigo_chk."\n";
            
            $pref = $row["FINSCHOOL_PREF_CD"];
            
            $data = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        if ($data){
            if($this->selectPrefCd != ""){
                $prefname = substr($pref, 3);
                $couse = $prefname." ";
            }else{
                $couse = "";
            }
            if($this->selectFinschoolType == "3"){
                $couse .= "中学校";
            }else{
                $couse .= "高等学校";
            }
            $couse .="一覧";
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename);
            return true;
        }else{
            $this->setWarning("MSG303");
            return false;
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

        //DB接続
        $db = Query::dbCheckOut();

        //１レコード取得
        $row = $db->getRow(knjl398qQuery::getScoolData($this->field["FINSCHOOLCD"]), DB_FETCHMODE_ASSOC);

        //DB切断
        Query::dbCheckIn($db);

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
        //文字型チェック
        foreach(array("FINSCHOOL_TELNO"   => array("[^-|0-9]",        "( 電話番号 )"),
                      "FINSCHOOL_FAXNO"   => array("[^-|0-9]",        "( FAX番号 )")
                ) as $key => $val
        ) {
            if ($this->field[$key] !="" && preg_match('{' . $val[0] . '}i', trim($this->field[$key]))) {
                $this->setWarning("MSG901", $val[1]);
                return false;
            }
        }
        if (strlen($this->field["FINSCHOOLCD"]) > 7) {
            $this->setWarning('MSG901', "( 学校コード ) 7バイトまで");
            return false;
        } elseif (strlen($this->field["FINSCHOOL_NAME"])>75){ 
            $this->setWarning('MSG901', "( 学校名 ) 75バイトまで");
            return false;
        } elseif (strlen($this->field["FINSCHOOL_KANA"])>75){ 
            $this->setWarning('MSG901', "( 学校名かな) 75バイトまで");
            return false;
        } elseif (strlen($this->field["FINSCHOOL_NAME_ABBV"])>30){ 
            $this->setWarning('MSG901', "( 学校名略称 ) 30バイトまで");
            return false;
        } elseif (strlen($this->field["FINSCHOOL_KANA_ABBV"])>75){ 
            $this->setWarning('MSG901', "( 学校名かな略称) 75バイトまで");
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
        } elseif (strlen($this->field["FINSCHOOL_ADDR1"])>90){ 
            $this->setWarning('MSG901', "( 住所 ) 90バイトまで");
            return false;
        } elseif (strlen($this->field["FINSCHOOL_ADDR2"])>90){ 
            $this->setWarning('MSG901', "( 方書き(アパート名等) ) 90バイトまで");
            return false;
        } elseif (strlen($this->field["FINSCHOOL_TELNO"])>14){ 
            $this->setWarning('MSG901', "( 電話番号 ) 14バイトまで");
            return false;
        } elseif (strlen($this->field["FINSCHOOL_FAXNO"])>14){ 
            $this->setWarning('MSG901', "( FAX番号 ) 14バイトまで");
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
    function &getDeleteModel() {
        //DB接続
        $db = Query::dbCheckOut();

        //キーの更新チェック
        if ($this->finschoolcd == "") {
            $this->setWarning('MSG308');
            return false;
        }
        if ($this->finschoolcd != $this->field["FINSCHOOLCD"]) {
            $this->setWarning('MSG308');
            return;
        }

        $row = $db->getRow(knjl398qQuery::getScoolData($this->finschoolcd), DB_FETCHMODE_ASSOC);
        if (!is_array($row)){
            $this->setWarning('MSG303');
            return;
        }

        //年度データの存在チェック
        if (knjl398qQuery::IsExisting($db, $this->finschoolcd)){
            $this->setWarning('MSG919',"年度データに登録されています。");
            return;
        }

        //削除
        knjl398qQuery::getDeleteQuery($db, $this->field);
        $this->setMessage('MSG201');

        //削除
        knjl398qQuery::getDeleteDetailQuery($db, $this->field);
        $this->setMessage('MSG201');

        //DB切断
        Query::dbCheckIn($db);

    }

    //追加
    function &insert() {
        knjl398qQuery::getInsertQuery($this->field);
    }

    //更新
    function &update() {
        knjl398qQuery::getUpdateQuery($this->field);
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
