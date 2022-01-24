<?php

require_once('for_php7.php');

require_once('knjm812aModel.inc');
require_once('knjm812aQuery.inc');

class knjm812aController extends Controller {
    var $ModelClassName = "knjm812aModel";
    var $ProgramID      = "KNJM812A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change_class":
                case "knjm812a":
                    $sessionInstance->knjm812aModel();
                    $this->callView("knjm812aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm812aCtl = new knjm812aController;
var_dump($_REQUEST);
?>
