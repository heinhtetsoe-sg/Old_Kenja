<?php

require_once('for_php7.php');

require_once('knjm262wModel.inc');
require_once('knjm262wQuery.inc');

class knjm262wController extends Controller
{
    public $ModelClassName = "knjm262wModel";
    public $ProgramID      = "KNJM262W";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm262w":
                    $sessionInstance->knjm262wModel();
                    $this->callView("knjm262wForm1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadCsvModel()) {
                        $this->callView("knjm262wForm1");
                    }
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm262wCtl = new knjm262wController();
