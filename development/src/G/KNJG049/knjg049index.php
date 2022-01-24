<?php

require_once('for_php7.php');

require_once('knjg049Model.inc');
require_once('knjg049Query.inc');

class knjg049Controller extends Controller {
    var $ModelClassName = "knjg049Model";
    var $ProgramID      = "KNJG049";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjg049Model();
                    $this->callView("knjg049Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjg049Ctl = new knjg049Controller;
?>
