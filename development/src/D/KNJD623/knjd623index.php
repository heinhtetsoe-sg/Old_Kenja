<?php

require_once('for_php7.php');

require_once('knjd623Model.inc');
require_once('knjd623Query.inc');

class knjd623Controller extends Controller {
    var $ModelClassName = "knjd623Model";
    var $ProgramID      = "KNJD623";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd623":
                    $sessionInstance->knjd623Model();
                    $this->callView("knjd623Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjd623Ctl = new knjd623Controller;
var_dump($_REQUEST);
?>
