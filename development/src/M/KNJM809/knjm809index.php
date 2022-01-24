<?php

require_once('for_php7.php');

require_once('knjm809Model.inc');
require_once('knjm809Query.inc');

class knjm809Controller extends Controller {
    var $ModelClassName = "knjm809Model";
    var $ProgramID      = "KNJM809";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm809":
                    $sessionInstance->knjm809Model();
                    $this->callView("knjm809Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm809Ctl = new knjm809Controller;
var_dump($_REQUEST);
?>
