<?php

require_once('for_php7.php');

require_once('knjm261wModel.inc');
require_once('knjm261wQuery.inc');

class knjm261wController extends Controller {
    var $ModelClassName = "knjm261wModel";
    var $ProgramID      = "KNJM261W";

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
                    $this->callView("knjm261wForm1");
                    break 2;
                case "dsub":
                    $this->callView("knjm261wForm1");
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
$knjm261wCtl = new knjm261wController;
//var_dump($_REQUEST);
?>
