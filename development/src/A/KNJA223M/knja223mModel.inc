<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knja223mModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード
    var $selectdata;
    var $schoolCd;
    var $schoolName;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    /**
     * メンバ変数の初期化
     */
    function knja223mModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //職員コード
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

        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                            "OUTPUT"            =>  VARS::post("OUTPUT"),           //名票
                            "KANA_OUTTYPE"      =>  VARS::post("KANA_OUTTYPE"),     //出力(ふりがな/学籍番号)
                            "KENSUU"            =>  VARS::post("KENSUU"),           //出力件数
                            "GAKKI"             =>  VARS::post("GAKKI"),            //学期コード
                            "DBNAME"            =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->mark = VARS::post("MARK");

        //学校を判断
        $db = Query::dbCheckOut();
        $this->schoolName = $db->getOne(knja223mQuery::getSchoolName());
        Query::dbCheckIn($db);

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
    }

}
?>
