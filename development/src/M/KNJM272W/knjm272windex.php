<?php

require_once('for_php7.php');

require_once('knjm272wModel.inc');
require_once('knjm272wQuery.inc');

class knjm272wController extends Controller {
    var $ModelClassName = "knjm272wModel";
    var $ProgramID      = "KNJM272W";

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
                case "subform1":
                case "read2":
                case "reset":
                    $this->callView("knjm272wSubForm1");
                    break 2;
                case "":
                case "read":
                case "addread":
                case "main":
                    $this->callView("knjm272wForm1");
                    break 2;
                case "dsub":
                    $this->callView("knjm272wForm1");
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
$knjm272wCtl = new knjm272wController;
//var_dump($_REQUEST);
?>
