<?php

require_once('for_php7.php');

require_once('knjl013hModel.inc');
require_once('knjl013hQuery.inc');

class knjl013hController extends Controller {
    var $ModelClassName = "knjl013hModel";
    var $ProgramID      = "KNJL013H";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl013hForm1");
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
$knjl013hCtl = new knjl013hController;
?>
