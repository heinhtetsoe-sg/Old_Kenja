<?php

require_once('for_php7.php');

require_once('knjz291_staff_reflectionModel.inc');
require_once('knjz291_staff_reflectionQuery.inc');

class knjz291_staff_reflectionController extends Controller {
    var $ModelClassName = "knjz291_staff_reflectionModel";
    var $ProgramID      = "KNJZ291_STAFF_REFLECTION";
    
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "search";
                case "main";
                    $this->callView("knjz291_staff_reflectionForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("search");
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
$knjz291_staff_reflectionCtl = new knjz291_staff_reflectionController;
//var_dump($_REQUEST);
?>
