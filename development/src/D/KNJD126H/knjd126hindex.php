<?php

require_once('for_php7.php');

require_once('knjd126hModel.inc');
require_once('knjd126hQuery.inc');

class knjd126hController extends Controller {
    var $ModelClassName = "knjd126hModel";
    var $ProgramID      = "KNJD126H";

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
                case "calc":
                   $this->callView("knjd126hForm1");
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

$knjd126hCtl = new knjd126hController;
?>
