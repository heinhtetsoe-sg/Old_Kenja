<?php

require_once('for_php7.php');

require_once('knjl312mModel.inc');
require_once('knjl312mQuery.inc');

class knjl312mController extends Controller {
    var $ModelClassName = "knjl312mModel";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl312m":
                    $sessionInstance->knjl312mModel();
                    $this->callView("knjl312mForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl312mCtl = new knjl312mController;
var_dump($_REQUEST);
?>
