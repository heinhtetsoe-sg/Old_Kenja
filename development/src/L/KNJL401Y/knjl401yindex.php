<?php

require_once('for_php7.php');

require_once('knjl401yModel.inc');
require_once('knjl401yQuery.inc');

class knjl401yController extends Controller {
    var $ModelClassName = "knjl401yModel";
    var $ProgramID      = "KNJL401Y";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl401y":
                    $sessionInstance->knjl401yModel();
                    $this->callView("knjl401yForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl401yCtl = new knjl401yController;
?>
