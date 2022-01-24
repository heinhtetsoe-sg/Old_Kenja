<?php

require_once('for_php7.php');

require_once('knjd293Model.inc');
require_once('knjd293Query.inc');

class knjd293Controller extends Controller {
    var $ModelClassName = "knjd293Model";
    var $ProgramID      = "KNJD293";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd293":
                case "gakki":
                    $sessionInstance->knjd293Model();
                    $this->callView("knjd293Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd293Ctl = new knjd293Controller;
//var_dump($_REQUEST);
?>
