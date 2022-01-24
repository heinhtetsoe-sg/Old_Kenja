<?php

require_once('for_php7.php');

require_once('knjd050Model.inc');
require_once('knjd050Query.inc');

class knjd050Controller extends Controller {
    var $ModelClassName = "knjd050Model";
    var $ProgramID      = "KNJD050";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "toukei":
                case "":
                    $this->callView("knjd050Form1");
                   break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjd050Ctl = new knjd050Controller;
//var_dump($_REQUEST);
?>
