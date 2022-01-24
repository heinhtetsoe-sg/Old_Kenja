<?php

require_once('for_php7.php');

require_once('knjp400Model.inc');
require_once('knjp400Query.inc');

class knjp400Controller extends Controller {
    var $ModelClassName = "knjp400Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjp400":
                    $sessionInstance->knjp400Model();
                    $this->callView("knjp400Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjp400Ctl = new knjp400Controller;
var_dump($_REQUEST);
?>
