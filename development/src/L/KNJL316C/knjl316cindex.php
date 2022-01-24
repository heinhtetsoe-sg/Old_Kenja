<?php

require_once('for_php7.php');

require_once('knjl316cModel.inc');
require_once('knjl316cQuery.inc');

class knjl316cController extends Controller {
    var $ModelClassName = "knjl316cModel";
    var $ProgramID      = "KNJL316C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl316c":
                    $sessionInstance->knjl316cModel();
                    $this->callView("knjl316cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl316cCtl = new knjl316cController;
//var_dump($_REQUEST);
?>
