<?php

require_once('for_php7.php');

require_once('knjd184kModel.inc');
require_once('knjd184kQuery.inc');

class knjd184kController extends Controller {
    var $ModelClassName = "knjd184kModel";
    var $ProgramID      = "KNJD184K";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "chgGrade":
                case "chgHrClass":
                case "knjd184k":
                    $sessionInstance->knjd184kModel();
                    $this->callView("knjd184kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd184kCtl = new knjd184kController;
?>
