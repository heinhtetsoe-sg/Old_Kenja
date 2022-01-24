<?php

require_once('for_php7.php');

require_once('knjd126nModel.inc');
require_once('knjd126nQuery.inc');

class knjd126nController extends Controller {
    var $ModelClassName = "knjd126nModel";
    var $ProgramID      = "KNJD126N";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "changeGradeHrClass":
                case "changeSubclasscd":
                case "changeSemester":
                case "reset":
                   $this->callView("knjd126nForm1");
                   break 2;
                case "calc":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjd126nForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}

$knjd126nCtl = new knjd126nController;
?>
