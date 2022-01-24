<?php

require_once('for_php7.php');

require_once('knjl321aModel.inc');
require_once('knjl321aQuery.inc');

class knjl321aController extends Controller
{
    public $ModelClassName = "knjl321aModel";
    public $ProgramID      = "KNJL321A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl321a":
                    $sessionInstance->knjl321aModel();
                    $this->callView("knjl321aForm1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadCsvModel()) {
                        $this->callView("knjl321aForm1");
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
$knjl321aCtl = new knjl321aController();
