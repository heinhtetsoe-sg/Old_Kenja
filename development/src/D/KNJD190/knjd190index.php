<?php

require_once('for_php7.php');

require_once('knjd190Model.inc');
require_once('knjd190Query.inc');

class knjd190Controller extends Controller {
    var $ModelClassName = "knjd190Model";
    var $ProgramID      = "KNJD190";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "toukei":
                case "":
                    $this->callView("knjd190Form1");
                   break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjd190Ctl = new knjd190Controller;
//var_dump($_REQUEST);
?>
