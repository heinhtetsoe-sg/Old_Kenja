<?php

require_once('for_php7.php');

require_once('knjd611Model.inc');
require_once('knjd611Query.inc');

class knjd611Controller extends Controller {
    var $ModelClassName = "knjd611Model";
    var $ProgramID      = "KNJD611";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd611":
                case "gakki":
                    $sessionInstance->knjd611Model();
                    $this->callView("knjd611Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd611Ctl = new knjd611Controller;
var_dump($_REQUEST);
?>
