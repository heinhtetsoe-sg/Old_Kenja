<?php

require_once('for_php7.php');

require_once('knjm250Model.inc');
require_once('knjm250Query.inc');

class knjm250Controller extends Controller {
    var $ModelClassName = "knjm250Model";
    var $ProgramID      = "KNJM250";     //プログラムID

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "reset":
                case "read":
                    $sessionInstance->getMainModel();
                    $this->callView("knjm250Form1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm250Ctl = new knjm250Controller;
?>
