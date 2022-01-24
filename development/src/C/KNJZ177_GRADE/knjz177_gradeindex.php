<?php

require_once('for_php7.php');

require_once('knjz177_gradeModel.inc');
require_once('knjz177_gradeQuery.inc');

class knjz177_gradeController extends Controller {
    var $ModelClassName = "knjz177_gradeModel";
    var $ProgramID      = "KNJZ177_GRADE";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "hanei":
                case "reset":
                    $this->callView("knjz177_gradeForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $this->callView("knjz177_gradeForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz177_gradeCtl = new knjz177_gradeController;
?>
