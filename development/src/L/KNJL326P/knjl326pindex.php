<?php

require_once('for_php7.php');

require_once('knjl326pModel.inc');
require_once('knjl326pQuery.inc');

class knjl326pController extends Controller {
    var $ModelClassName = "knjl326pModel";
    var $ProgramID      = "KNJL326P";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl326p":                            //メニュー画面もしくはSUBMITした場合
                case "knjl326ptestdiv":                     //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl326pModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl326pForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl326pCtl = new knjl326pController;
?>
