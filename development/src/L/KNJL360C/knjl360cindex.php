<?php

require_once('for_php7.php');

require_once('knjl360cModel.inc');
require_once('knjl360cQuery.inc');

class knjl360cController extends Controller {
    var $ModelClassName = "knjl360cModel";
    var $ProgramID      = "KNJL360C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl360c":
                    $sessionInstance->knjl360cModel();
                    $this->callView("knjl360cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl360cCtl = new knjl360cController;
//var_dump($_REQUEST);
?>
