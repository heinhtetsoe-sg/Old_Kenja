<?php

require_once('for_php7.php');

class knjl030kModel extends Model {

    var $cmd;
    var $year;              //現在処理年度
    var $semester;          //現在処理学期
    var $examno;            //受験番号
    var $applicantdiv;      //試験制度
    var $testdiv;           //試験区分
    var $desirediv;         //志望区分
    var $field = array();           //フィールドデータ
    var $cflg;
    var $checkexam;    

    function knjl030kModel()
    {
    }

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->examno);
            unset($this->testdiv);
            unset($this->desirediv);
            unset($this->cflg);
            unset($this->checkexam);
        }

        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR + 1;
        $this->examno = VARS::post("EXAMNO");

        if (strlen($this->examno)) {
            $this->examno = sprintf("%04d",$this->examno);
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

        //フィールド
        $this->field = array("TESTDIV"              => VARS::post("TESTDIV"),               //試験区分
                             "SHDIV"                => VARS::post("SHDIV"),                 //専併区分
                             "DESIREDIV"            => VARS::post("DESIREDIV"),             //志望区分
                             "APPLICANTDIV"         => VARS::post("APPLICANTDIV"),          //出願区分
                             "CLUBCD"               => VARS::post("CLUBCD"),      		    //クラブコード 2006.01.17 alp m-yama
                             "SPECIAL_REASON_DIV"   => VARS::post("SPECIAL_REASON_DIV"),    //クラブコード 2006.01.17 alp m-yama
                             "NAME"                 => VARS::post("NAME"),                  //氏名(志願者)
                             "NAME_KANA"            => VARS::post("NAME_KANA"),             //氏名かな(志願者)
                             "SEX"                  => VARS::post("SEX"),                   //性別
                             "BIRTHDAY"             => VARS::post("BIRTHDAY"),              //生年月日
                             "ADDRESSCD"            => VARS::post("ADDRESSCD"),             //現住所コード
                             "TELNO"                => VARS::post("TELNO"),                 //電話番号(志願者)
                             "LOCATIONCD"           => VARS::post("LOCATIONCD"),            //所在地コード
                             "NATPUBPRIDIV"         => VARS::post("NATPUBPRIDIV"),          //国公私立区分コード
                             "FS_CD"                => VARS::post("FS_CD"),                 //出身学校コード
                             "PS_CD"                => VARS::post("PS_CD"),                 //塾コード
                             "FS_GRDYEAR"           => VARS::post("FS_GRDYEAR"),            //卒業年
                             "GNAME"                => VARS::post("GNAME"),                 //氏名(保護者)
                             "GKANA"                => VARS::post("GKANA"),                 //氏名かな(保護者)
                             "GTELNO"               => VARS::post("GTELNO"),                //電話番号(保護者)
                             "FORMNO"               => VARS::post("FORMNO"),                //フォームNo.
                             "ZIPCD"                => VARS::post("ZIPCD"),                 //郵便番号(志願者)
                             "ADDRESS"              => VARS::post("ADDRESS"),               //住所(志願者)
                             "GZIPCD"               => VARS::post("GZIPCD"),                //郵便番号(保護者)
                             "GADDRESS"             => VARS::post("GADDRESS"),              //住所(保護者),
                             "APPROVAL_FLG"         => VARS::post("CHK_APPROVAL")           //通知承諾可
                            );
        if ($this->cmd == "reload2") {
            $this->cflg = VARS::post("cflg");
        } else {
            $this->cflg = "";
        }
    }

    //入力項目チェック
    function validateInput()
    {
        //空文字チェック
        if((!strlen($this->examno))){
            $this->setWarning("MSG301", "( 受験番号 )");
            return false;
        }
        if((!strlen($this->field["TESTDIV"]))){
            $this->setWarning("MSG301", "( 試験区分 )");
            return false;
        }
        if((!strlen($this->field["SHDIV"]))){
            $this->setWarning("MSG301", "( 専併区分 )");
            return false;
        }
        if((!strlen($this->field["APPLICANTDIV"]))){
            $this->setWarning("MSG301", "( 出願区分 )");
            return false;
        }
        if((!strlen($this->field["DESIREDIV"]))){
            $this->setWarning("MSG301", "( 志望区分 )");
            return false;
        }
        if((!strlen($this->field["NAME"]))){
            $this->setWarning("MSG301", "志願者( 氏名 )");
            return false;
        }
        if((!strlen($this->field["SPECIAL_REASON_DIV"]))){
            $this->setWarning("MSG301", "特別理由区分");
            return false;
        }
        if((!strlen($this->field["NAME_KANA"]))){
            $this->setWarning("MSG301", "志願者( 氏名かな )");
            return false;
        }
        if((!strlen($this->field["SEX"]))){
            $this->setWarning("MSG301", "( 性別 )");
            return false;
        }
        if((!strlen($this->field["ADDRESSCD"]))){
            $this->setWarning("MSG301", "( 現住所コード )");
            return false;
        }
        if((!strlen($this->field["LOCATIONCD"]))){
            $this->setWarning("MSG301", "( 所在地コード )");
            return false;
        }
        if((!strlen($this->field["NATPUBPRIDIV"]))){
            $this->setWarning("MSG301", "( 国公私立区分コード )");
            return false;
        }
// 2006.01.10 alp m-yama
//        if((!strlen($this->field["FS_CD"]))){
//            $this->setWarning("MSG301", "( 学校コード )");
//            return false;
//        }
        //キー値変更チェック
        if ($this->cmd != "add" && $this->examno != $this->checkexam) {
            $this->setWarning("MSG300", "更新、削除は検索を実行してから行ってください。");
            return false;
        }
        //存在チェック
        $row = knjl030kQuery::Existdata($this);
        if(isset($row["EXAMNO"]) && $this->cmd == "add"){
            $this->setWarning("MSG302");
            return false;
        }
        if(!isset($row["EXAMNO"]) && ($this->cmd == "update" || $this->cmd == "back" || $this->cmd == "next")){
            $this->setWarning("MSG303");
            return false;
        }
        //文字数チェック
        if(strlen($this->examno) > 4){
            $this->setWarning("MSG915","受験番号は4バイトまでです。");
            return false;
        }
        if(strlen($this->field["NAME"]) > 60){
            $this->setWarning("MSG915","志願者( 氏名 )は60バイトまでです。");
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
        if(strlen($this->field["ADDRESS"]) > 153){
            $this->setWarning("MSG915","志願者( 住所 )は153バイトまでです。");
            return false;
        }
        if(strlen($this->field["FS_GRDYEAR"]) > 4){
            $this->setWarning("MSG915","卒業年は4バイトまでです。");
            return false;
        }
        if(strlen($this->field["GNAME"]) > 60){
            $this->setWarning("MSG915","保護者( 氏名 )は60バイトまでです。");
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
        if(strlen($this->field["GADDRESS"]) > 153){
            $this->setWarning("MSG915","保護者( 住所 )は153バイトまでです。");
            return false;
        }
        return true;
    }

    //追加処理
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $result = knjl030kQuery::getInsertQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel()
    {
        //入力チェック
        if($this->validateInput()){
            $result = knjl030kQuery::getUpdateQuery($this);
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
        $row = knjl030kQuery::Existdata($this);
        if (!isset($row["EXAMNO"])) {
            $this->setWarning("MSG303");
            return false;
        }
        if ($this->examno != $this->checkexam) {
            $this->setWarning("MSG300", "更新、削除は検索を実行してから行ってください。");
            return false;
        }        
        knjl030kQuery::getDeleteQuery($this);
        $this->examno = "";
        $this->setMessage("MSG201");
    }
 
}
?>