<?php

require_once('for_php7.php');

require_once('knjd634Model.inc');
require_once('knjd634Query.inc');

class knjd634Controller extends Controller {
    var $ModelClassName = "knjd634Model";
    var $ProgramID      = "KNJD634";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                case "knjd634":
                    $sessionInstance->knjd634Model();
                    $this->callView("knjd634Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjd634Ctl = new knjd634Controller;
//var_dump($_REQUEST);
?>
