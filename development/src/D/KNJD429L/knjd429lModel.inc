<?php

require_once('for_php7.php');

class knjd429lModel extends Model {
    var $cmd;
    var $control;        //コントロールマスタ
    var $optright;

    function knjd429lModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
    }

    function init() {
        $this->cmd      = VARS::request("cmd");

        $this->field = array("SEMESTER"               =>  VARS::post("SEMESTER"),               //学期
                             "SCHOOL_KIND"            =>  VARS::post("SCHOOL_KIND"),            //学校種別
                             "HUKUSIKI_RADIO"         =>  VARS::post("HUKUSIKI_RADIO"),         //コンボ切替
                             "MOVE_DATE"              =>  VARS::post("MOVE_DATE"),              //異動対象日付
                             "FRM_PATERN"             =>  VARS::post("FRM_PATERN"),             //帳票パターン
                             "HYOSI"                  =>  VARS::post("HYOSI"),                  //表紙印刷
                             "URA"                    =>  VARS::post("URA"),                    //裏表紙印刷
                             "OUIN"                   =>  VARS::post("OUIN"),                   //押印欄
                             "GHR_CD"                 =>  VARS::post("GHR_CD")                  //年組
                             );

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->optright = array();
        $this->selectdata   = VARS::post("selectdata");

    }
}
?>
