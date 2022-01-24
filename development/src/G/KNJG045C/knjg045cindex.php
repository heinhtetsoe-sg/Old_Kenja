<?php

require_once('for_php7.php');

require_once('knjg045cModel.inc');
require_once('knjg045cQuery.inc');

class knjg045cController extends Controller {
    var $ModelClassName = "knjg045cModel";
    var $ProgramID      = "KNJG045C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjg045cForm1");
                    break 2;
                case "update":
                case "delete":
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
$knjg045cCtl = new knjg045cController;
//var_dump($_REQUEST);
?>
