<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjm030mModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    /**
     * メンバ変数の初期化
     */
    function knjm030mModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),             //日付
                             "SUBCLASSNAME"         =>  VARS::post("SUBCLASSNAME"),     //科目
                             "SCHOOLINGKINDCD"      =>  VARS::post("SCHOOLINGKINDCD"),  //スクーリング種別
                             "HEADER"               =>  VARS::post("HEADER"),           //ヘッダ出力
                             "DBNAME"               =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }

    /**
     * 
     */
    function getDownloadModel()
    {

        //ヘッダの作成//
        $header = array("YEAR"              => "年度",
                        "SCHREGNO"          => "学籍番号",
                        "EXECUTEDATE"       => "実施日付",
                        "CHAIRCD"           => "講座コード",
                        "CHAIRNAME"         => "講座名",
                        "PERIODCD"          => "校時コード",
                        "SCHOOLING_SEQ"     => "スクーリング回数",
                        "CREDIT_TIME"       => "時間",
                        "RECEIPT_DATE"      => "受付日付",
                        "RECEIPT_TIME"      => "受付時間",
                        "STAFFCD"           => "担当者職員コード",
                        "SCHOOLINGKINDCD"   => "スクーリング種別",
                        "REMARK"            => "備考"
                        );

        $contents = "" ;
        if ($this->field["HEADER"] == "on"){
            $contents .= implode($header, ",") ."\n";
        }
        //DB接続
        $db = Query::dbCheckOut();

        //出力データの作成//
        //SQL文発行
        $query = knjm030mQuery::selectCsvQuery($this);

        $result = $db->query($query);
        $data = false;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",",".",str_replace("\n", "↓", str_replace("\r", "\n", str_replace("\r\n", "\n", $row))));//データ内にカンマがあればピリオドに置換・・・置換する文字は、カンマでなければ何でも良い。
            $contents .= implode($row, ",") ."\n";
            $data = true;
        }

        $result->free();
        Query::dbCheckIn($db);

        if ($data){
            $couse = "スクーリング出席状況";
            $filename = sprintf("%s.csv",
                                         $couse
                                );
            common::downloadContents($contents, $filename);
            return true;
        }else{
            $this->setWarning("MSG303");
            return false;
        }

    }

}
?>
