<?php

require_once('for_php7.php');

class knjl510aModel extends Model {

    var $cmd;
    var $year;              //現在処理年度
    var $semester;          //現在処理学期
    var $examno;            //受験番号
    var $applicantdiv;      //試験制度
    var $testdiv;           //試験区分
    var $desirediv;         //志望区分
    var $field = array();           //フィールドデータ
    var $checkexam;

    function knjl510aModel()
    {
    }

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->examno);
            unset($this->testdiv);
            unset($this->desirediv);
            unset($this->checkexam);
        }

        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR + 1;
        $this->examno = VARS::post("EXAMNO");

        if (strlen($this->examno)) {
            if ($this->cmd == 'reset') {
                $this->examno = VARS::post("preExamno");
            } else {
                $this->examno = sprintf("%05d",$this->examno);
            }
        }
        if ($this->cmd == "reload1") {
            $this->examno = "";
        }
        if (strlen(VARS::post("TESTDIV"))) {
            $this->testdiv = VARS::post("TESTDIV");
        }
        if (strlen(VARS::post("DESIREDIV"))) {
            $this->desirediv = VARS::post("DESIREDIV");
        }

        //入試区分変更前の値
        if ($this->cmd == "update" && strlen(VARS::post("preTestdiv"))) {
            $this->preTestdiv = VARS::post("preTestdiv");
        }

        //フィールド
        $this->field = array("TESTDIV"              => VARS::post("TESTDIV"),               //試験区分
                             "SHDIV"                => VARS::post("SHDIV"),                 //専併区分
                             "HOPE_COURSECODE"      => VARS::post("HOPE_COURSECODE"),       //志望区分
                             "APPLICANTDIV"         => "1",                                 //入試制度(1:高校で固定)
                             "NAME"                 => VARS::post("NAME"),                  //氏名(志願者)
                             "NAME_KANA"            => VARS::post("NAME_KANA"),             //氏名かな(志願者)
                             "SEX"                  => "2",                                 //性別
                             "BIRTHDAY"             => VARS::post("BIRTHDAY"),              //生年月日
                             "ADDRESSCD"            => VARS::post("ADDRESSCD"),             //現住所コード
                             "TELNO"                => VARS::post("TELNO"),                 //電話番号(志願者)
                             "FS_CD"                => VARS::post("FS_CD"),                 //出身学校コード
                             "FS_DAY"               => VARS::post("FS_DAY"),                //卒業年月日
                             "GNAME"                => VARS::post("GNAME"),                 //氏名(保護者)
                             "GKANA"                => VARS::post("GKANA"),                 //氏名かな(保護者)
                             "GTELNO"               => VARS::post("GTELNO"),                //電話番号(保護者)
                             "ZIPCD"                => VARS::post("ZIPCD"),                 //郵便番号(志願者)
                             "ADDRESS1"             => VARS::post("ADDRESS1"),              //住所(志願者)
                             "ADDRESS2"             => VARS::post("ADDRESS2"),              //方書(志願者)
                             "GZIPCD"               => VARS::post("GZIPCD"),                //郵便番号(保護者)
                             "GADDRESS1"            => VARS::post("GADDRESS1"),             //住所(保護者),
                             "GADDRESS2"            => VARS::post("GADDRESS2"),             //方書(保護者),
                             "RELATIONSHIP"         => VARS::post("RELATIONSHIP")           //方書(保護者),
                            );

        if (strlen(trim(str_replace("　", " ", $this->field["FS_CD"])))) {
            $this->field["FS_CD"] = sprintf("%04d", $this->field["FS_CD"]);
        }

        if ($this->cmd == 'sendFsCd') {
            $this->fsCdSearch = VARS::post("FS_CD_SEARCH");
        }

    }

    //入力項目チェック
    function validateInput()
    {

        //空文字チェック
        if((!strlen($this->field["TESTDIV"]))){
            $this->setWarning("MSG301", "( 入試区分 )");
            return false;
        }
        if((!strlen($this->field["HOPE_COURSECODE"]))){
            $this->setWarning("MSG301", "( 志望区分 )");
            return false;
        }
        if((!strlen($this->examno))){
            $this->setWarning("MSG301", "( 受験番号 )");
            return false;
        }
        if((!strlen($this->field["NAME"]))){
            $this->setWarning("MSG301", "志願者( 氏名 )");
            return false;
        }
        if((!strlen($this->field["NAME_KANA"]))){
            $this->setWarning("MSG301", "志願者( 氏名かな )");
            return false;
        }
        /*
        //キー値変更チェック
        if ($this->cmd != "add" && $this->examno != $this->checkexam) {
            $this->setWarning("MSG300", "更新、削除は検索を実行してから行ってください。");
            return false;
        }
        */
        //存在チェック
        $row = knjl510aQuery::Existdata($this);
        if(isset($row["EXAMNO"]) && $this->cmd == "add"){
            $this->setWarning("MSG302");
            return false;
        }
        
        if(!isset($row["EXAMNO"]) && ($this->cmd == "update" || $this->cmd == "back" || $this->cmd == "next")){
            $this->setWarning("MSG303");
            return false;
        }
        
        //文字数チェック
        if(strlen($this->examno) > 5){
            $this->setWarning("MSG915","受験番号は5バイトまでです。");
            return false;
        }
        if(strlen($this->field["NAME"]) > 120){
            $this->setWarning("MSG915","志願者( 氏名 )は120バイトまでです。");
            return false;
        }
        if(strlen($this->field["NAME_KANA"]) > 120){
            $this->setWarning("MSG915","志願者( 氏名かな )は120バイトまでです。");
            return false;
        }
        if(strlen($this->field["TELNO"]) > 14){
            $this->setWarning("MSG915","志願者( 電話番号 )は14バイトまでです。");
            return false;
        }
        if(strlen($this->field["ADDRESS1"]) > 150){
            $this->setWarning("MSG915","志願者( 住所 )は150バイトまでです。");
            return false;
        }
        if(strlen($this->field["ADDRESS2"]) > 150){
            $this->setWarning("MSG915","志願者( 方書 )は150バイトまでです。");
            return false;
        }
        if(strlen($this->field["FS_CD"]) > 4){
            $this->setWarning("MSG915","出身学校コードは4バイトまでです。");
            return false;
        }
        if(strlen($this->field["GNAME"]) > 120){
            $this->setWarning("MSG915","保護者( 氏名 )は120バイトまでです。");
            return false;
        }
        if(strlen($this->field["GKANA"]) > 120){
            $this->setWarning("MSG915","保護者( 氏名かな )は120バイトまでです。");
            return false;
        }
        if(strlen($this->field["GTELNO"]) > 14){
            $this->setWarning("MSG915","保護者( 電話番号 )は14バイトまでです。");
            return false;
        }
        if(strlen($this->field["GADDRESS1"]) > 150){
            $this->setWarning("MSG915","保護者( 住所 )は150バイトまでです。");
            return false;
        }
        if(strlen($this->field["GADDRESS2"]) > 150){
            $this->setWarning("MSG915","保護者( 方書 )は150バイトまでです。");
            return false;
        }

        return true;
    }

    //追加処理
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $result = knjl510aQuery::getInsertQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel()
    {
        //入力チェック
        if($this->validateInput()){
            $result = knjl510aQuery::getUpdateQuery($this);
            if($this->cmd == "update"){
                $this->setMessage("MSG201");
            }
        }
        return;
    }

    //削除処理
    function &getDeleteModel()
    {
        if($this->examno == ""){
            $this->setWarning("MSG304", "( 受験番号 )");
            return false;
        }
        $row = knjl510aQuery::Existdata($this);
        if (!isset($row["EXAMNO"])) {
            $this->setWarning("MSG303");
            return false;
        }
        if ($this->examno != $this->checkexam) {
            $this->setWarning("MSG300", "更新、削除は検索を実行してから行ってください。");
            return false;
        }        
        knjl510aQuery::getDeleteQuery($this);
        $this->examno = "";
        $this->testdiv = "";
        $this->setMessage("MSG201");
    }

    //データ受信処理
     function &getSendModel()
    {
        $db = Query::dbCheckOut();
        if ($this->cmd == "sendFsCd") {
            $query = knjl510aQuery::getFsCdSearch($this, $this->fsCdSearch);
            $data = $db->getOne($query);
            //URIエンコード
            //$data = rawurlencode($data);
            //出力charsetをutf-8に
            mb_http_output ( 'UTF-8' );
            //ヘッダ
            header ("Content-Type: text/html; charset=utf-8"); 
            echo($data);
        }
        exit;
    }

}
?>