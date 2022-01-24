<?php

require_once('for_php7.php');

require_once('knjd150Model.inc');
require_once('knjd150Query.inc');

class knjd150Controller extends Controller {
    var $ModelClassName = "knjd150Model";
    var $ProgramID      = "KNJD150";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd150":
                    $sessionInstance->knjd150Model();
                    $this->callView("knjd150Form1");
                    exit;
                case "gakki":
                    $sessionInstance->knjd150Model();
                    $this->callView("knjd150Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjd150Ctl = new knjd150Controller;
var_dump($_REQUEST);
?>
