<?php

require_once('for_php7.php');

require_once('knjm812bModel.inc');
require_once('knjm812bQuery.inc');

class knjm812bController extends Controller {
    var $ModelClassName = "knjm812bModel";
    var $ProgramID      = "KNJM812B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change_class":
                case "knjm812b":
                    $sessionInstance->knjm812bModel();
                    $this->callView("knjm812bForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm812bCtl = new knjm812bController;
var_dump($_REQUEST);
?>
