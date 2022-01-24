<?php

require_once('for_php7.php');

require_once('knjp151kModel.inc');
require_once('knjp151kQuery.inc');

class knjp151kController extends Controller {
    var $ModelClassName = "knjp151kModel";
    var $ProgramID      = "KNJP151K";
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "edit_clear":
                    $this->callView("knjp151kForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "all_update":
                    $sessionInstance->getAllUpdateModel();
                    $this->callView("knjp151kForm1");
                    break 2;
                case "":
                    $this->callView("knjp151kForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp151kCtl = new knjp151kController;
//var_dump($_REQUEST);
?>
