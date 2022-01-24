<?php

require_once('for_php7.php');

require_once('knjb1307Model.inc');
require_once('knjb1307Query.inc');

class knjb1307Controller extends Controller {
    var $ModelClassName = "knjb1307Model";
    var $ProgramID      = "KNJB1307";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb1307":
                    $sessionInstance->knjb1307Model();
                    $this->callView("knjb1307Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb1307Ctl = new knjb1307Controller;
//var_dump($_REQUEST);
?>
