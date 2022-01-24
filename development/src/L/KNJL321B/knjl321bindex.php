<?php

require_once('for_php7.php');

require_once('knjl321bModel.inc');
require_once('knjl321bQuery.inc');

class knjl321bController extends Controller
{
    public $ModelClassName = "knjl321bModel";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl321b":
                    $sessionInstance->knjl321bModel();
                    $this->callView("knjl321bForm1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl321bForm1");
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
$knjl321bCtl = new knjl321bController;
//var_dump($_REQUEST);
