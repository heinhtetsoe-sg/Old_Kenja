<?php

require_once('for_php7.php');

require_once('knjl590jModel.inc');
require_once('knjl590jQuery.inc');

class knjl590jController extends Controller {
    var $ModelClassName = "knjl590jModel";
    var $ProgramID      = "KNJL590J";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "change":
                case "changeTest":
                case "change_testdiv2":
                case "back1":
                case "next1":
                    $this->callView("knjl590jForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl590jForm1");
                    break 2;
                case "back2":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl590jForm1");
                    break 2;
                case "next2":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl590jForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "reference":
                    $this->callView("knjl590jForm1");
                    break 2;
                case "reset":
                case "":
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
$knjl590jCtl = new knjl590jController;
?>
