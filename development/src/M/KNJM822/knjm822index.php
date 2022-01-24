<?php

require_once('for_php7.php');

require_once('knjm822Model.inc');
require_once('knjm822Query.inc');

class knjm822Controller extends Controller {
    var $ModelClassName = "knjm822Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm822":
                    $sessionInstance->knjm822Model();
                    $this->callView("knjm822Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm822Ctl = new knjm822Controller;
var_dump($_REQUEST);
?>
