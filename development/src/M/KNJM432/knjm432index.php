<?php

require_once('for_php7.php');

require_once('knjm432Model.inc');
require_once('knjm432Query.inc');

class knjm432Controller extends Controller {
    var $ModelClassName = "knjm432Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm432":
                    $sessionInstance->knjm432Model();
                    $this->callView("knjm432Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm432Ctl = new knjm432Controller;
var_dump($_REQUEST);
?>
