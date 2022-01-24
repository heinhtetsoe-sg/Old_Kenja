<?php

require_once('for_php7.php');

require_once('knjh410_medicalModel.inc');
require_once('knjh410_medicalQuery.inc');

class knjh410_medicalController extends Controller {
    var $ModelClassName = "knjh410_medicalModel";
    var $ProgramID      = "KNJH410_MEDICAL";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "general":     //一般
                case "dental":      //歯・口腔
                case "next_year":   //次の年
                case "before_year": //前の年
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh410_medicalForm1");
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
$knjh410_medicalCtl = new knjh410_medicalController;
?>
