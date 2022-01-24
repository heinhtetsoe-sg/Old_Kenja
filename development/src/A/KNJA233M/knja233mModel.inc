<?php

require_once('for_php7.php');

class knja233mModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $schoolName;
    var $schoolCode;

    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    function knja233mModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd      = STAFFCD;                  //職員コード
    }

    function init() {
        //プロパティファイル
        $this->getPropertiesAll();
        
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }

        //権限チェック
        $this->auth = $this->getPrgId ? $this->sendAuth : common::SecurityCheck(STAFFCD, 'KNJA233M');

        $this->getPropertiesAll(); //プロパティファイル

        $this->year = CTRL_YEAR;
        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "CLASSCD"          =>  VARS::post("CLASSCD"),          //教科コード
                             "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),       //科目コード
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //名票
                             "HURIGANA_OUTPUT"  =>  VARS::post("HURIGANA_OUTPUT"),  //ふりがな出力
                             "HEIGHT"           =>  VARS::post("HEIGHT"),           //縦サイズ
                             "WIDTH"            =>  VARS::post("WIDTH"),            //横サイズ
                             "KENSUU"           =>  VARS::post("KENSUU"),           //出力件数
                             "DATE"             =>  VARS::post("DATE"),             //学籍処理日
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        if (!$this->field["DATE"]) {
            $this->field["DATE"] = $this->control["学籍処理日"];
        }

        $this->cmd = VARS::request("cmd");

        //学校名取得
        $query = knja233mQuery::getSchoolName();
        $db = Query::dbCheckOut();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $this->schoolName = $rowZ010["NAME1"];
        $this->schoolCode = $rowZ010["NAME2"];
        Query::dbCheckIn($db);
    }

    function clean() {}
}
?>