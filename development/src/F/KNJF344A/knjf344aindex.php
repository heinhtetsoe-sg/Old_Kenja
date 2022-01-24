<?php

require_once('for_php7.php');

require_once('knjf344aModel.inc');
require_once('knjf344aQuery.inc');

class knjf344aController extends Controller
{
    public $ModelClassName = "knjf344aModel";
    public $ProgramID      = "KNJF344A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf344a":
                    $sessionInstance->knjf344aModel();
                    $this->callView("knjf344aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjf344aForm1");
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
$knjf344aCtl = new knjf344aController();
