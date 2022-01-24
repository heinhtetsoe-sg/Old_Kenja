<?php

require_once('for_php7.php');

require_once('knjm701Model.inc');
require_once('knjm701Query.inc');

class knjm701Controller extends Controller {
    var $ModelClassName = "knjm701Model";
    var $ProgramID      = "KNJM701";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "knjm701";
                    $sessionInstance->knjm701Model();
                    $this->callView("knjm701Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm701Ctl = new knjm701Controller;
?>
