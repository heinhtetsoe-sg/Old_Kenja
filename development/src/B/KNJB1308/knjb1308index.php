<?php

require_once('for_php7.php');

require_once('knjb1308Model.inc');
require_once('knjb1308Query.inc');

class knjb1308Controller extends Controller {
    var $ModelClassName = "knjb1308Model";
    var $ProgramID      = "KNJB1308";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb1308":
                    $sessionInstance->knjb1308Model();
                    $this->callView("knjb1308Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb1308Ctl = new knjb1308Controller;
//var_dump($_REQUEST);
?>
