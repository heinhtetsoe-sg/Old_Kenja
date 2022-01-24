<?php

require_once('for_php7.php');

require_once('knjz068dModel.inc');
require_once('knjz068dQuery.inc');

class knjz068dController extends Controller {
    var $ModelClassName = "knjz068dModel";
    var $ProgramID      = "KNJZ068D";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                    $this->callView("knjz068dForm1");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                /*case "uploadCsv":
                    $sessionInstance->getUploadCsvModel();
                    $sessionInstance->setCmd("edit");
                    break 1;*/
                case "downloadHead":
                case "downloadCsv":
                case "downloadError":
                    if (!$sessionInstance->getDownloadCsvModel()) {
                        $this->callView("knjz068dForm1");
                    }
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}

$knjz068dCtl = new knjz068dController;
?>
