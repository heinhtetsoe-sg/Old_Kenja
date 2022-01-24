<?php

require_once('for_php7.php');

require_once('knjb1231Model.inc');
require_once('knjb1231Query.inc');

class knjb1231Controller extends Controller {
    var $ModelClassName = "knjb1231Model";
    var $ProgramID      = "KNJB1231";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "hukusiki":
                case "change_class":
                case "knjb1231":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjb1231Model();
                    $this->callView("knjb1231Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjb1231Ctl = new knjb1231Controller;
var_dump($_REQUEST);
?>
