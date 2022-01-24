<?php

require_once('for_php7.php');

require_once('knjd214Model.inc');
require_once('knjd214Query.inc');

class knjd214Controller extends Controller {
    var $ModelClassName = "knjd214Model";
    var $ProgramID      = "KNJD214";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "copy":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("knjd214");
                    break 1;
                case "clear":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getClearModel();
                    $sessionInstance->setCmd("knjd214");
                    break 1;
                case "":
                case "knjd214":
                case "semechg":
                    $sessionInstance->knjd214Model();
                    $this->callView("knjd214Form1");
                    exit;
                case "gakki":
                    $sessionInstance->knjd214Model();
                    $this->callView("knjd214Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd214Ctl = new knjd214Controller;
var_dump($_REQUEST);
?>
