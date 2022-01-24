<?php

require_once('for_php7.php');

class knja233eModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ

    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    function knja233eModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
    }

    function init() {
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

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
        $this->auth = $this->getPrgId ? $this->sendAuth : common::SecurityCheck(STAFFCD, 'KNJA233E');

        $this->getPropertiesAll(); //プロパティファイル

        $this->year = CTRL_YEAR;
        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "CLASSCD"          =>  VARS::post("CLASSCD"),          //教科コード
                             "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),       //科目コード
                             "GRD_DIV"          =>  VARS::post("GRD_DIV"),          //転学・退学した生徒は出力しない
                             "DATE"             =>  VARS::post("DATE"),             //学籍処理日
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        if (!$this->field["DATE"]) {
            $this->field["DATE"] = $this->control["学籍処理日"];
        }

        $this->cmd = VARS::request("cmd");

    }

    function clean() {}
}
?>
