<?php

require_once('for_php7.php');

class knjz355Model extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
    var $control;  //コントロールマスタ
    var $staffcd;  //登録者コード
    var $properties = array();

    function knjz355Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;   //登録者コード
    }

    function init() {
        $this->field = array(
                             "STAFFCD"    => VARS::post("STAFFCD"),    //職員コード
                             "HEADER"     => VARS::post("HEADER"),     //ヘッダ有
                             "YEAR"       => VARS::post("YEAR"),       //年度
                             "S_DATE"     => VARS::post("S_DATE"),      //出力範囲開始日
                             "E_DATE"     => VARS::post("E_DATE")     //出力範囲終了日
                             );
        $this->cmd = VARS::request("cmd");

        $this->getPropertiesAll();
    }

    function &getMainModel() {}

    //ＣＳＶ出力
    function getDownloadModel()
    {
        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");
        $header = array(
                        "UPDATED"     => "アクセス時間",
                        "USERID"      => "利用者ID",
                        "STAFFCD"     => "職員番号",
                        "STAFFNAME"   => "氏名",
                        "PROGRAMID"   => "プログラムID",
                        "IPADDRESS"   => "IPアドレス",
                        "ACCESS_CD"   => "アクセスコード※"
                        );

        $header_rei = array(
                            "UPDATED"       => "2017/06/12 11:24:28.883",
                            "USERID"        => "alpoki",
                            "STAFFCD"       => "9999999999",
                            "STAFFNAME"     => "アルプ　太郎",
                            "PROGRAMID"     => "KNJXMENU",
                            "IPADDRESS"     => "192.168.50.7",
                            "ACCESS_CD"     => "F",
                            );

        $contents  = "";    //クリア

        if( $this->field["HEADER"]=="on" ) $contents .= implode($header, ",")."\n";
 
            $header_data = false;
            //DB接続
            $db = Query::dbCheckOut();
            $table = "";
            $where = "";
            $query = knjz355Query::getselectDataSQL($this);
            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                $contents .= implode($row, ",") ."\n";
                $header_data = true;
            }
            $result->free();
            Query::dbCheckIn($db);


        //ＣＳＶ出力
        if ($header_data) {
            $filename = sprintf("%s.csv", "アクセスログ一覧");
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
    
}
?>
