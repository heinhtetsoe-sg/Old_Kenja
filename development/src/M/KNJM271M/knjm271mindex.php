<?php

require_once('for_php7.php');

require_once('knjm271mModel.inc');
require_once('knjm271mQuery.inc');

class knjm271mController extends Controller {
    var $ModelClassName = "knjm271mModel";
    var $ProgramID      = "KNJM271M";

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
                    $this->callView("knjm271mSubForm1");
                    break 2;
                case "cmdStart":
                case "read":
                case "addread":
                case "main":
                case "sort":
                    $this->callView("knjm271mForm1");
                    break 2;
                case "dsub":
                    $this->callView("knjm271mForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("cmdStart");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm271mCtl = new knjm271mController;
//var_dump($_REQUEST);
?>
