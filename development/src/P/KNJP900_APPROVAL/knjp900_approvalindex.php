<?php

require_once('for_php7.php');

require_once('knjp900_approvalModel.inc');
require_once('knjp900_approvalQuery.inc');

class knjp900_approvalController extends Controller {
    var $ModelClassName = "knjp900_approvalModel";
    var $ProgramID      = "KNJP900_APPROVAL";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "check":
                case "main":
                    $this->callView("knjp900_approvalForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
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
$knjp900_approvalCtl = new knjp900_approvalController;
//var_dump($_REQUEST);
?>
