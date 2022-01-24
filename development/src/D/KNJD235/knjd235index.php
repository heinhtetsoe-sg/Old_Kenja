<?php

require_once('for_php7.php');

require_once('knjd235Model.inc');
require_once('knjd235Query.inc');

class knjd235Controller extends Controller {
    var $ModelClassName = "knjd235Model";
    var $ProgramID      = "KNJD235";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd235":
                case "gakki":
                    $sessionInstance->knjd235Model();
                    $this->callView("knjd235Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd235Ctl = new knjd235Controller;
var_dump($_REQUEST);
?>
