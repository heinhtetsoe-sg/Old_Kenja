<?php

require_once('for_php7.php');

require_once('knjp714Model.inc');
require_once('knjp714Query.inc');

class knjp714Controller extends Controller {
    var $ModelClassName = "knjp714Model";
    var $ProgramID      = "KNJP714";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "reset":
                    $this->callView("knjp714Form1");
                    break 2;
                case "grpform":
                    $this->callView("knjp714grpForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }            
        }
    }

}
$knjp714Ctl = new knjp714Controller;
//var_dump($_REQUEST);
?>
