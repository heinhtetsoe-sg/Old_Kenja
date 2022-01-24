<?php

require_once('for_php7.php');

require_once('knjmp714Model.inc');
require_once('knjmp714Query.inc');

class knjmp714Controller extends Controller {
    var $ModelClassName = "knjmp714Model";
    var $ProgramID      = "KNJMP714";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "reset":
                    $this->callView("knjmp714Form1");
                    break 2;
                case "grpform":
                    $this->callView("knjmp714grpForm1");
                    break 2;
                case "update":  //更新
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
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
$knjmp714Ctl = new knjmp714Controller;
//var_dump($_REQUEST);
?>
