<?php

require_once('for_php7.php');

require_once('knjz281_job_reflectionModel.inc');
require_once('knjz281_job_reflectionQuery.inc');

class knjz281_job_reflectionController extends Controller {
    var $ModelClassName = "knjz281_job_reflectionModel";
    var $ProgramID      = "KNJZ281A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $sessionInstance->setAccessLogDetail("U", "KNJZ281_JOB_REFLECTION");
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "sel":
                    $sessionInstance->setAccessLogDetail("S", "KNJZ281_JOB_REFLECTION");
                    $this->callView("knjz281_job_reflectionForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz281_job_reflectionCtl = new knjz281_job_reflectionController;
?>
