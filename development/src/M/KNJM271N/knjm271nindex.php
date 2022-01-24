<?php

require_once('for_php7.php');

require_once('knjm271nModel.inc');
require_once('knjm271nQuery.inc');

class knjm271nController extends Controller {
    var $ModelClassName = "knjm271nModel";
    var $ProgramID      = "KNJM271N";

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
                case "addread":
                case "main":
                case "reset":
                case "sort":
                    $this->callView("knjm271nForm1");
                    break 2;
                case "dsub":
                    $this->callView("knjm271nForm1");
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
$knjm271nCtl = new knjm271nController;
//var_dump($_REQUEST);
?>
