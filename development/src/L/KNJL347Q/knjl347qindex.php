<?php

require_once('for_php7.php');

require_once('knjl347qModel.inc');
require_once('knjl347qQuery.inc');

class knjl347qController extends Controller {
    var $ModelClassName = "knjl347qModel";
    var $ProgramID      = "KNJL347Q";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl347q":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl347qModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl347qForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl347qCtl = new knjl347qController;
?>
