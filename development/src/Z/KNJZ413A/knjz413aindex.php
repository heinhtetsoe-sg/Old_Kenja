<?php

require_once('for_php7.php');

require_once('knjz413aModel.inc');
require_once('knjz413aQuery.inc');

class knjz413aController extends Controller {
    var $ModelClassName = "knjz413aModel";
    var $ProgramID      = "KNJZ413A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $sessionInstance->knjz413aModel();
                    $this->callView("knjz413aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjz413aCtl = new knjz413aController;
var_dump($_REQUEST);
?>
