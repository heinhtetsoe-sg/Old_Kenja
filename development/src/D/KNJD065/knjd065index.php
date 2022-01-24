<?php

require_once('for_php7.php');

require_once('knjd065Model.inc');
require_once('knjd065Query.inc');

class knjd065Controller extends Controller {
    var $ModelClassName = "knjd065Model";
    var $ProgramID      = "KNJD065";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd065":
                case "semechg":
                    $sessionInstance->knjd065Model();
                    $this->callView("knjd065Form1");
                    exit;
                case "gakki":
                    $sessionInstance->knjd065Model();
                    $this->callView("knjd065Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd065Ctl = new knjd065Controller;
var_dump($_REQUEST);
?>
