<?php

require_once('for_php7.php');

require_once('knjl325pModel.inc');
require_once('knjl325pQuery.inc');

class knjl325pController extends Controller {
    var $ModelClassName = "knjl325pModel";
    var $ProgramID      = "KNJL325P";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl325p":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl325pModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl325pForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl325pCtl = new knjl325pController;
?>
