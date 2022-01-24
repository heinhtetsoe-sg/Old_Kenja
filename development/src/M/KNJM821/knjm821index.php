<?php

require_once('for_php7.php');

require_once('knjm821Model.inc');
require_once('knjm821Query.inc');

class knjm821Controller extends Controller {
    var $ModelClassName = "knjm821Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm821":
                    $sessionInstance->knjm821Model();
                    $this->callView("knjm821Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm821Ctl = new knjm821Controller;
var_dump($_REQUEST);
?>
