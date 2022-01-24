<?php

require_once('for_php7.php');

require_once('knjd633Model.inc');
require_once('knjd633Query.inc');

class knjd633Controller extends Controller {
    var $ModelClassName = "knjd633Model";
    var $ProgramID      = "KNJD633";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd633":
                case "gakki":
                    $sessionInstance->knjd633Model();
                    $this->callView("knjd633Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd633Ctl = new knjd633Controller;
var_dump($_REQUEST);
?>
