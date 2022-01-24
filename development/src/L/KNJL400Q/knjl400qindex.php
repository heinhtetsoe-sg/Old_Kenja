<?php

require_once('for_php7.php');

require_once('knjl400qModel.inc');
require_once('knjl400qQuery.inc');

class knjl400qController extends Controller {
    var $ModelClassName = "knjl400qModel";
    var $ProgramID      = "KNJL400Q";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":
                    $this->callView("knjl400qForm1");
                    break 2;
                case "exec":
                    if(!$sessionInstance->getExecModel()){
                        $this->callView("knjl400qForm1");
                    }
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjl400qCtl = new knjl400qController;
?>
