<?php

require_once('for_php7.php');

class knjd426Model extends Model {
    var $cmd;
    var $control;        //コントロールマスタ
    var $chkprblock;
    var $optright;

    function knjd426Model() {
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
                             "PRINT_DATE"             =>  VARS::post("PRINT_DATE"),             //記載日付
                             "GHR_CD"                 =>  VARS::post("GHR_CD")                  //年組
                             );

        $this->getPropertiesAll(); //プロパティファイルの読込み

        $this->optright = array();
        $this->selectdata   = VARS::post("selectdata");

        $this->chkprblock = array();
        if ($this->Properties["HREPORT_GUIDANCE_KIND_NAME_HDAT_schoolOnly"] == "1") {
            //MAX10個なので、10回回す
            for ($ii = 0;$ii < 10;$ii++) {
                $this->chkprblock[] = VARS::post("CHK_PRINTBLOCK".($ii+1));
            }
        }
    }
}
?>
