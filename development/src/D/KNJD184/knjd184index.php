<?php

require_once('for_php7.php');

require_once('knjd184Model.inc');
require_once('knjd184Query.inc');

class knjd184Controller extends Controller {
    var $ModelClassName = "knjd184Model";
    var $ProgramID      = "KNJD184";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd184":
                    $sessionInstance->knjd184Model();
                    $this->callView("knjd184Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjd184Ctl = new knjd184Controller;
?>
