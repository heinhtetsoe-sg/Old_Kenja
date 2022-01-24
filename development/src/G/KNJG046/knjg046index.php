<?php

require_once('for_php7.php');

require_once('knjg046Model.inc');
require_once('knjg046Query.inc');

class knjg046Controller extends Controller
{
    var $ModelClassName = "knjg046Model";
    var $ProgramID      = "KNJG046";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjg046Model();
                    $this->callView("knjg046Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjg046Ctl = new knjg046Controller();
