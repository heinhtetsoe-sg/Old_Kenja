<?php

require_once('for_php7.php');

require_once('knjd181hModel.inc');
require_once('knjd181hQuery.inc');

class knjd181hController extends Controller {
    var $ModelClassName = "knjd181hModel";
    var $ProgramID      = "KNJD181H";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "clear";
                case "knjd181h";
                    $sessionInstance->knjd181hModel();
                    $this->callView("knjd181hForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd181hCtl = new knjd181hController;
?>
