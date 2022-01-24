<?php

require_once('for_php7.php');

require_once('knjm260wModel.inc');
require_once('knjm260wQuery.inc');

class knjm260wController extends Controller {
    var $ModelClassName = "knjm260wModel";
    var $ProgramID      = "KNJM260W";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "add":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("addread");
                    break 1;
                case "chdel":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "":
                case "read":
                case "addread":
                case "main":
                    $this->callView("knjm260wForm1");
                    break 2;
                case "dsub":
                    $this->callView("knjm260wForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm260wCtl = new knjm260wController;
//var_dump($_REQUEST);
?>
