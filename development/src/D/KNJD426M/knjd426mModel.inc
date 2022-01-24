<?php

require_once('for_php7.php');

class knjd426mModel extends Model {
    var $cmd;
    var $control;        //コントロールマスタ
    var $chkColumnBlock; //出力する項目（左列）
    var $chkPrintBlock;  //出力する項目（右列）
    var $optright;

    function knjd426mModel() {
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
                             "GHR_CD"                 =>  VARS::post("GHR_CD"),                 //年組
                             "OUTPUT_CLASS"           =>  VARS::post("OUTPUT_CLASS")            //出力クラス
                             );

        $this->getPropertiesAll(); //プロパティファイルの読込み

        $this->optright = array();
        $this->selectdata   = VARS::post("selectdata");

        $this->chkColumnBlock = array();
        $this->chkPrintBlock = array();
        //MAX5個なので、5回回す
        for ($ii = 0;$ii < 5;$ii++) {
            $idx = $ii+1;
            $this->chkColumnBlock[] = VARS::post("CHK_COLUMNBLOCK".$idx);
            $this->chkPrintBlock[] = VARS::post("CHK_PRINTBLOCK".$idx);
        }
    }
}
?>
