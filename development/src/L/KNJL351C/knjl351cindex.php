<?php

require_once('for_php7.php');

require_once('knjl351cModel.inc');
require_once('knjl351cQuery.inc');

class knjl351cController extends Controller {
    var $ModelClassName = "knjl351cModel";
    var $ProgramID      = "KNJL351C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl351c":
                    $sessionInstance->knjl351cModel();
                    $this->callView("knjl351cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
    }
}
$knjl351cCtl = new knjl351cController;
//var_dump($_REQUEST);
?>
