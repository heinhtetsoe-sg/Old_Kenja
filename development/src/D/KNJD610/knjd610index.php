<?php

require_once('for_php7.php');

require_once('knjd610Model.inc');
require_once('knjd610Query.inc');

class knjd610Controller extends Controller {
    var $ModelClassName = "knjd610Model";
    var $ProgramID      = "KNJD610";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd610":
                case "gakki":
                    $sessionInstance->knjd610Model();
                    $this->callView("knjd610Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd610Ctl = new knjd610Controller;
var_dump($_REQUEST);
?>
