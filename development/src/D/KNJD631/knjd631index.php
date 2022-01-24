<?php

require_once('for_php7.php');

require_once('knjd631Model.inc');
require_once('knjd631Query.inc');

class knjd631Controller extends Controller {
    var $ModelClassName = "knjd631Model";
    var $ProgramID      = "KNJD631";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                case "knjd631":
                    $sessionInstance->knjd631Model();
                    $this->callView("knjd631Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjd631Ctl = new knjd631Controller;
//var_dump($_REQUEST);
?>
