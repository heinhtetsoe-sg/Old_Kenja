<?php

require_once('for_php7.php');

require_once('knjx_toolModel.inc');
require_once('knjx_toolQuery.inc');

class knjx_toolController extends Controller {
    var $ModelClassName = "knjx_toolModel";
    var $ProgramID      = "KNJX_TOOL";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "regd": 
                    $sessionInstance->REGDupdateModel();
                    $this->callView("knjx_toolForm1");
                    break 2;
                case "score1": 
                    $sessionInstance->SCORE1updateModel();
                    $this->callView("knjx_toolForm1");
                    break 2;
                case "score2": 
                    $sessionInstance->SCORE2updateModel();
                    $this->callView("knjx_toolForm1");
                    break 2;
                case "score4": 
                    $sessionInstance->SCORE4updateModel();
                    $this->callView("knjx_toolForm1");
                    break 2;
                case "score5": 
                    $sessionInstance->SCORE5updateModel();
                    $this->callView("knjx_toolForm1");
                    break 2;
                case "attend": 
                    $sessionInstance->ATTENDupdateModel();
                    $this->callView("knjx_toolForm1");
                    break 2;
                case "record": 
                    $sessionInstance->RECORDupdateModel();
                    $this->callView("knjx_toolForm1");
                    break 2;
                case "grad": 
                    $sessionInstance->GRADupdateModel();
                    $this->callView("knjx_toolForm1");
                    break 2;
                case "user": 
                    $sessionInstance->USERupdateModel();
                    $this->callView("knjx_toolForm1");
                    break 2;
                case "":
                case "main":
                    $this->callView("knjx_toolForm1");
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
$knjx_toolCtl = new knjx_toolController;
?>
