<?php

require_once('for_php7.php');

require_once('knjm702dModel.inc');
require_once('knjm702dQuery.inc');

class knjm702dController extends Controller {
    var $ModelClassName = "knjm702dModel";
    var $ProgramID      = "KNJM702D";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "knjm702d";
                    $sessionInstance->knjm702dModel();
                    $this->callView("knjm702dForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm702dCtl = new knjm702dController;
?>
