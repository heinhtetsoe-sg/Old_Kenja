<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjl376qModel extends Model {
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
    function knjl376qModel()
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
                             
                            );


    }
    function &getMainModel()
    {
    }

    function getUpdateModel()
    {
        $db = Query::dbCheckOut();
        
        $this->update = array();
        
        $this->update["REGISTERCD"] = STAFFCD;
        $this->update["UPDATED"] = "sysdate()";
        $this->update["YEAR"] = CTRL_YEAR;
        
        foreach($this->satNo as $key => $val){
            $this->update["SCORE_ENGLISH"]  = VARS::post("ENG_FIRST{$val}") != "" ? VARS::post("ENG_FIRST{$val}") : " ";
            $this->update["SCORE_ENGLISH2"] = VARS::post("ENG_SECOND{$val}") != "" ? VARS::post("ENG_SECOND{$val}") : " ";
            
            $this->update["SCORE_MATH"]  = VARS::post("MATH_FIRST{$val}") != "" ? VARS::post("MATH_FIRST{$val}") : " ";
            $this->update["SCORE_MATH2"] = VARS::post("MATH_SECOND{$val}") != "" ? VARS::post("MATH_SECOND{$val}") : " ";
            
            $this->update["SCORE_JAPANESE"]  = VARS::post("JAP_FIRST{$val}") != "" ? VARS::post("JAP_FIRST{$val}") : " ";
            $this->update["SCORE_JAPANESE2"] = VARS::post("JAP_SECOND{$val}") != "" ? VARS::post("JAP_SECOND{$val}") : " ";
            
            $where = " WHERE SAT_NO = '".$val."' AND YEAR = '".CTRL_YEAR."' ";
            
            common::_update("SAT_EXAM_DAT", "", $where, $this->update, $db, "1");
        }
        
        $this->setMessage("更新しました。");
        $this->messageFlg = 1;
        
        Query::dbCheckIn($db);
    }
    


}
//var_dump($_REQUEST);
?>
