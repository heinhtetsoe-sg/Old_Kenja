<?php

require_once('for_php7.php');

require_once('knjd186iModel.inc');
require_once('knjd186iQuery.inc');

class knjd186iController extends Controller {
    var $ModelClassName = "knjd186iModel";
    var $ProgramID      = "KNJD186I";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "clear";
                case "knjd186i";
                    $sessionInstance->knjd186iModel();
                    $this->callView("knjd186iForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd186iCtl = new knjd186iController;
?>
