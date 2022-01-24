<?php

require_once('for_php7.php');

require_once('knjm271sModel.inc');
require_once('knjm271sQuery.inc');

class knjm271sController extends Controller {
    var $ModelClassName = "knjm271sModel";
    var $ProgramID      = "KNJM271S";

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
                    $this->callView("knjm271sSubForm1");
                    break 2;
                case "":
                case "read":
                case "addread":
                case "main":
                    $this->callView("knjm271sForm1");
                    break 2;
                case "dsub":
                    $this->callView("knjm271sForm1");
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
$knjm271sCtl = new knjm271sController;
//var_dump($_REQUEST);
?>
