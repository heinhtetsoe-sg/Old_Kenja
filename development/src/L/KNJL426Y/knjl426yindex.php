<?php

require_once('for_php7.php');

require_once('knjl426yModel.inc');
require_once('knjl426yQuery.inc');

class knjl426yController extends Controller {
    var $ModelClassName = "knjl426yModel";
    var $ProgramID      = "KNJL426Y";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl426y":
                    $sessionInstance->knjl426yModel();
                    $this->callView("knjl426yForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl426yCtl = new knjl426yController;
?>
