<?php

require_once('for_php7.php');

require_once('knjl328pModel.inc');
require_once('knjl328pQuery.inc');

class knjl328pController extends Controller {
    var $ModelClassName = "knjl328pModel";
    var $ProgramID      = "KNJL328P";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl328p":                            //メニュー画面もしくはSUBMITした場合
                case "knjl328ptestdiv":                     //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl328pModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl328pForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl328pCtl = new knjl328pController;
?>
