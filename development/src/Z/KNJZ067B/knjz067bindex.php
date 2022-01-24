<?php

require_once('for_php7.php');

require_once('knjz067bModel.inc');
require_once('knjz067bQuery.inc');

class knjz067bController extends Controller
{
    public $ModelClassName = "knjz067bModel";
    public $ProgramID      = "KNJZ067B";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjz067bForm1");
                    break 2;
                case "downloadCsv":
                    if (!$sessionInstance->getDownloadCsvModel()) {
                        $this->callView("knjz067bForm1");
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
$knjz067bCtl = new knjz067bController();
