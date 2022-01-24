<?php

require_once('for_php7.php');

require_once('knjp950Model.inc');
require_once('knjp950Query.inc');

class knjp950Controller extends Controller {
    var $ModelClassName = "knjp950Model";
    var $ProgramID      = "KNJP950";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "data_set":
                case "clear":
                case "edit":
                    $this->callView("knjp950Form1");
                    break 2;
                case "insert":
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp950Ctl = new knjp950Controller;
?>
