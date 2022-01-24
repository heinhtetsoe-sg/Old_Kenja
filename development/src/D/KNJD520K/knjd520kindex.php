<?php

require_once('for_php7.php');

require_once('knjd520kModel.inc');
require_once('knjd520kQuery.inc');

class knjd520kController extends Controller
{
    var $ModelClassName = "knjd520kModel";
    var $ProgramID      = "KNJD520K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true )
        {
            switch (trim($sessionInstance->cmd))
            {
                case "main":
                    $this->callView("knjd520kForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "reflect":
                    $sessionInstance->getReflectModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "cancel":
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
$knjd520kCtl = new knjd520kController;
//var_dump($_REQUEST);
?>
