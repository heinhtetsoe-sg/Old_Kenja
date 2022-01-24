<?php

require_once('for_php7.php');

require_once('knjf342aModel.inc');
require_once('knjf342aQuery.inc');

class knjf342aController extends Controller
{
    public $ModelClassName = "knjf342aModel";
    public $ProgramID      = "KNJF342A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf342a":
                    $sessionInstance->knjf342aModel();
                    $this->callView("knjf342aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjf342aForm1");
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
$knjf342aCtl = new knjf342aController();
