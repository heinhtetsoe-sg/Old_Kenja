<?php

require_once('for_php7.php');

require_once('knjd643Model.inc');
require_once('knjd643Query.inc');

class knjd643Controller extends Controller {
    var $ModelClassName = "knjd643Model";
    var $ProgramID      = "KNJD643";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd643":
                case "gakki":
                    $sessionInstance->knjd643Model();
                    $this->callView("knjd643Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd643Ctl = new knjd643Controller;
var_dump($_REQUEST);
?>
