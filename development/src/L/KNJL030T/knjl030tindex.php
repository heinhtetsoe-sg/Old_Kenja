<?php

require_once('for_php7.php');

require_once('knjl030tModel.inc');
require_once('knjl030tQuery.inc');

class knjl030tController extends Controller {
    var $ModelClassName = "knjl030tModel";
    var $ProgramID      = "KNJL030T";     //プログラムID

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl030tForm1");
                    break 2;
                case "edit":
                    $this->callView("knjl030tForm2");
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
$knjl030tCtl = new knjl030tController;
?>
