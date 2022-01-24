<?php

require_once('for_php7.php');

require_once('knjl216yModel.inc');
require_once('knjl216yQuery.inc');

class knjl216yController extends Controller {
    var $ModelClassName = "knjl216yModel";
    var $ProgramID      = "KNJL216Y";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "select":
                case "knjl216y":
                    $sessionInstance->knjl216yModel();
                    $this->callView("knjl216yForm1");
                    exit;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "insert":
                    $sessionInstance->getInsertModel();
                    $sessionInstance->setCmd("main");
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
$knjl216yCtl = new knjl216yController;
var_dump($_REQUEST);
?>
