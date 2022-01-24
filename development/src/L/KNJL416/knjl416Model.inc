<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjl416Model extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
    var $control;           //コントロールマスタ
    var $staffcd;           //登録者コード
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    /**
     * メンバ変数の初期化
     */
    function knjl416Model()
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

        $this->field = array("YEAR_SEMESTER"    =>  VARS::post("YEAR_SEMESTER")
                            );

        $this->getPropertiesAll(); //プロパティファイル
    }
    function &getMainModel()
    {
    }

    // 今年度生成
    function makeYearData()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knjl416Query::insertRecruitDat();
        $db->query($query);

        $this->setMessage("MSG201");

        $db->commit();
        Query::dbCheckIn($db);
    }

}
//var_dump($_REQUEST);
?>
