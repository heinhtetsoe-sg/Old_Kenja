<?php

require_once('for_php7.php');

require_once('knjl327pModel.inc');
require_once('knjl327pQuery.inc');

class knjl327pController extends Controller {
    var $ModelClassName = "knjl327pModel";
    var $ProgramID      = "KNJL327P";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl327p":                            //メニュー画面もしくはSUBMITした場合
                case "knjl327ptestdiv":                     //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl327pModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl327pForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl327pCtl = new knjl327pController;
?>
