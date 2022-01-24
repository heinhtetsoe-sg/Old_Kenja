<?php

require_once('for_php7.php');

require_once('knjl084jModel.inc');
require_once('knjl084jQuery.inc');

class knjl084jController extends Controller {
    var $ModelClassName = "knjl084jModel";
    var $ProgramID      = "KNJL084J";     //プログラムID

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                    $this->callView("knjl084jForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "clear":
                    $sessionInstance->getClearModel();
                    $sessionInstance->setCmd("main");
                    break 1;
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
$knjl084jCtl = new knjl084jController;
?>
