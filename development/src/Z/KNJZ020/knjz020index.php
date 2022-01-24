<?php

require_once('for_php7.php');
require_once('knjz020Model.inc');
require_once('knjz020Query.inc');

class knjz020Controller extends Controller
{
    public $ModelClassName = "knjz020Model";
    public $ProgramID      = "KNJZ020";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "changeKind":
                case "changeJisuDiv":
                case "main":
                case "copy":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz020Form1");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "uploadCsv":
                    $sessionInstance->getUploadCsvModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "downloadHead":
                case "downloadCsv":
                case "downloadError":
                    if (!$sessionInstance->getDownloadCsvModel()) {
                        $this->callView("knjz020Form1");
                    }
                    break 2;
                case "error":
                    $this->callView("error");
                    break 1;
                case "":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
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
$knjz020Ctl = new knjz020Controller();
