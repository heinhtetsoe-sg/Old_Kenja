<?php

require_once('for_php7.php');

require_once('knjm270sModel.inc');
require_once('knjm270sQuery.inc');

class knjm270sController extends Controller {
    var $ModelClassName = "knjm270sModel";
    var $ProgramID      = "KNJM270S";

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
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read2");
                    break 1;
                case "subform1":
                case "read2":
                case "reset":
                    $this->callView("knjm270sSubForm1");
                    break 2;
                case "":
                case "read":
                case "addread":
                case "main":
                    $this->callView("knjm270sForm1");
                    break 2;
                case "dsub":
                    $this->callView("knjm270sForm1");
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
$knjm270sCtl = new knjm270sController;
//var_dump($_REQUEST);
?>
