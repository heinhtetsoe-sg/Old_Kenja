<?php

require_once('for_php7.php');

require_once('knjl318rModel.inc');
require_once('knjl318rQuery.inc');

class knjl318rController extends Controller {
    var $ModelClassName = "knjl318rModel";
    var $ProgramID      = "KNJL318R";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl318r":
                    $sessionInstance->knjl318rModel();
                    $this->callView("knjl318rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl318rCtl = new knjl318rController;
//var_dump($_REQUEST);
?>
