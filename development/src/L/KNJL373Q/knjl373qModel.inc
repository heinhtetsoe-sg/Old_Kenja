<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjl373qModel extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
    var $file;
    var $control;           //コントロールマスタ
    var $staffcd;           //登録者コード
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    /**
     * メンバ変数の初期化
     */
    function knjl373qModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //登録者コード
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


        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        
        
        $this->field = array("RADIO"    =>  VARS::post("Radio") != "" ? VARS::post("Radio") : "1",
                             
                             "PLACECD"  =>  VARS::post("PLACECD"),
                             
                            );


    }
    function &getMainModel()
    {
    }

    function getCsvFile()
    {
        //DB接続
        $db = Query::dbCheckOut();
        
        $header_data = "";
        $contents = "";
        
        //ヘッダーなし
        //$contents = "学年,国内外,受験番号,名前\n";
        
        //件数カウントなし
        
        $query = knjl373qQuery::getCsvData();
        $result = $db->query($query);
        
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $contents .= sprintf("%02d", $row["GRADE"]).",";
            $contents .= sprintf("%02d", $row["INOUT_KUBUN"]).",";
            $contents .= $row["SAT_NO"].",";
            $contents .= $row["NAME1"]."\n";
            
            $header_data = true;
        }
        
        $result->free();
        Query::dbCheckIn($db);
        //ＣＳＶ出力
        if ($header_data) {
            $filename = sprintf("%s.csv","マークリーダ用名簿");
            common::downloadContents($contents, $filename);
            
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }


}
//var_dump($_REQUEST);
?>
