<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjl393qModel extends Model {
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
    function knjl393qModel()
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
        
        $this->field = array("CHECK"    =>  VARS::post("CHECK"),
                             
                             "GROUPNAME"    =>  VARS::post("GROUPNAME") != "" ? VARS::post("GROUPNAME") : "1",
                             
                             "FINSCHOOL_COMB"   =>  VARS::post("FINSCHOOL_COMB"),
                             
                             "GROUP_COMB"   =>  VARS::post("GROUP_COMB"),
                             
                             "PLACE_COMB"   =>  VARS::post("PLACE_COMB"),
                             
                             "CHOICE"    =>  VARS::post("CHOICE") != "" ? VARS::post("CHOICE") : "1",
                             
                             "FROM"     =>  str_replace("/", "-", VARS::post("FROM")),
                             
                             "TO"     =>  str_replace("/", "-", VARS::post("TO")),
                             
                             
                             );

    }
    function &getMainModel()
    {
    }


    


}
//var_dump($_REQUEST);
?>
