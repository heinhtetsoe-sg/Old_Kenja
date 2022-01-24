<?php

require_once('for_php7.php');

require_once('knjf346aModel.inc');
require_once('knjf346aQuery.inc');

class knjf346aController extends Controller
{
    public $ModelClassName = "knjf346aModel";
    public $ProgramID      = "KNJF346A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf346a":
                    $sessionInstance->knjf346aModel();
                    $this->callView("knjf346aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjf346aForm1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjf346aCtl = new knjf346aController();
