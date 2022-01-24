<?php

require_once('for_php7.php');

require_once('knjd233aModel.inc');
require_once('knjd233aQuery.inc');

class knjd233aController extends Controller {
    var $ModelClassName = "knjd233aModel";
    var $ProgramID      = "KNJD233A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd233a":
                case "gakki":
                    $sessionInstance->knjd233aModel();
                    $this->callView("knjd233aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd233aCtl = new knjd233aController;
var_dump($_REQUEST);
?>
