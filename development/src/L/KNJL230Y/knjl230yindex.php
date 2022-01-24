<?php

require_once('for_php7.php');

require_once('knjl230yModel.inc');
require_once('knjl230yQuery.inc');

class knjl230yController extends Controller {
    var $ModelClassName = "knjl230yModel";
    var $ProgramID      = "KNJL230Y";     //プログラムID

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl230yForm1");
                    break 2;
                case "edit":
                    $this->callView("knjl230yForm2");
                    break 2;
                case "insert":
                    $sessionInstance->getInsertModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delete":
                    $sessionInstance->getDeleteModel();
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
$knjl230yCtl = new knjl230yController;
?>
