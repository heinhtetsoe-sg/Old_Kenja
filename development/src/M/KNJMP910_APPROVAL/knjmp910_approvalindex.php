<?php

require_once('for_php7.php');

require_once('knjmp910_approvalModel.inc');
require_once('knjmp910_approvalQuery.inc');

class knjmp910_approvalController extends Controller {
    var $ModelClassName = "knjmp910_approvalModel";
    var $ProgramID      = "KNJMP910_APPROVAL";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "check":
                case "main":
                    $this->callView("knjmp910_approvalForm1");
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
$knjmp910_approvalCtl = new knjmp910_approvalController;
//var_dump($_REQUEST);
?>
