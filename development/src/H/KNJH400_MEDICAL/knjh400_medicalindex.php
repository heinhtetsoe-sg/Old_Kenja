<?php

require_once('for_php7.php');

require_once('knjh400_medicalModel.inc');
require_once('knjh400_medicalQuery.inc');

class knjh400_medicalController extends Controller {
    var $ModelClassName = "knjh400_medicalModel";
    var $ProgramID      = "KNJH400_MEDICAL";

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
                    $this->callView("knjh400_medicalForm1");
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
$knjh400_medicalCtl = new knjh400_medicalController;
?>
