<?php

require_once('for_php7.php');
require_once('knjd627gModel.inc');
require_once('knjd627gQuery.inc');

class knjd627gController extends Controller
{
    public $ModelClassName = "knjd627gModel";
    public $ProgramID      = "KNJD627G";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        $sessionInstance->programID = $this->ProgramID;

        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjd627gForm1");
                    break 2;
                case "csv":
                    if (!$sessionInstance->getDownloadCsvModel()) {
                        $this->callView("knjd627gForm1");
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
$knjd627gCtl = new knjd627gController();
