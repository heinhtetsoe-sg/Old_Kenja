<?php

require_once('for_php7.php');

require_once('knjl220cModel.inc');
require_once('knjl220cQuery.inc');

class knjl220cController extends Controller {
    var $ModelClassName = "knjl220cModel";
    var $ProgramID      = "KNJL220C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                    $this->callView("knjl220cForm1");
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
$knjl220cCtl = new knjl220cController;
?>
