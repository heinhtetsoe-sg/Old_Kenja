<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjl392qModel extends Model {
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
    function knjl392qModel()
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
        
        $this->field = array(
                             "STUDENT"    =>  VARS::post("STUDENT") != "" ? VARS::post("STUDENT") : "1",
                             
                             "CHOICE"    =>  VARS::post("CHOICE") != "" ? VARS::post("CHOICE") : "1",
                             
                             "PLACE_COMB"   =>  VARS::post("PLACE_COMB"),
                             
                             "EXAM_FROM"     =>  VARS::post("EXAM_FROM"),
                             
                             "EXAM_TO"     =>  VARS::post("EXAM_TO"),
                             
                             
                             );

    }
    function &getMainModel()
    {
    }


    


}
//var_dump($_REQUEST);
?>