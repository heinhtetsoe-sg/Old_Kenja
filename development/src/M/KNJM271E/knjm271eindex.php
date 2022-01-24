<?php

require_once('for_php7.php');

require_once('knjm271eModel.inc');
require_once('knjm271eQuery.inc');

class knjm271eController extends Controller {
    var $ModelClassName = "knjm271eModel";
    var $ProgramID      = "KNJM271E";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "add":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("addread");
                    break 1;
                case "":
                case "reset":
                case "addread":
                case "main":
                    $this->callView("knjm271eForm1");
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
$knjm271eCtl = new knjm271eController;
//var_dump($_REQUEST);
?>
