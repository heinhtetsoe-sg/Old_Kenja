<?php

require_once('for_php7.php');

require_once('knjd292wModel.inc');
require_once('knjd292wQuery.inc');

class knjd292wController extends Controller
{
    public $ModelClassName = "knjd292wModel";
    public $ProgramID      = "KNJD292W";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd292w":
                    $sessionInstance->setAccessLogDetail("S", $this->ProgramID);
                    $sessionInstance->knjd292wModel();
                    $this->callView("knjd292wForm1");
                    exit;
                case "gakki":
                    $sessionInstance->knjd292wModel();
                    $this->callView("knjd292wForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjd292wForm1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $this->ProgramID);
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd292wCtl = new knjd292wController();
//var_dump($_REQUEST);
