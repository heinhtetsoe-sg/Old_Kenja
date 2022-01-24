<?php

require_once('for_php7.php');

require_once('knjp910_approvalModel.inc');
require_once('knjp910_approvalQuery.inc');

class knjp910_approvalController extends Controller {
    var $ModelClassName = "knjp910_approvalModel";
    var $ProgramID      = "KNJP910_APPROVAL";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "check":
                case "main":
                    $this->callView("knjp910_approvalForm1");
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
$knjp910_approvalCtl = new knjp910_approvalController;
//var_dump($_REQUEST);
?>
