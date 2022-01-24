<?php

require_once('for_php7.php');

require_once('knjp728Model.inc');
require_once('knjp728Query.inc');

class knjp728Controller extends Controller {
    var $ModelClassName = "knjp728Model";
    var $ProgramID      = "KNJP728";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjp728":
                    $sessionInstance->knjp728Model();
                    $this->callView("knjp728Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjp728Ctl = new knjp728Controller;
var_dump($_REQUEST);
?>
