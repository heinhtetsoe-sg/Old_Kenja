<?php

require_once('for_php7.php');

require_once('knjl385jModel.inc');
require_once('knjl385jQuery.inc');

class knjl385jController extends Controller {
    var $ModelClassName = "knjl385jModel";
    var $ProgramID      = "KNJL385J";     //プログラムID

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                    $this->callView("knjl385jForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
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
$knjl385jCtl = new knjl385jController;
?>
