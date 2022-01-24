<?php

require_once('for_php7.php');

require_once('knjl692iModel.inc');
require_once('knjl692iQuery.inc');

class knjl692iController extends Controller
{
    public $ModelClassName = "knjl692iModel";
    public $ProgramID      = "KNJL692I";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl692i":
                    $sessionInstance->knjl692iModel();
                    $this->callView("knjl692iForm1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadCsvModel()) {
                        $this->callView("knjl692iForm1");
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
$knjl692iCtl = new knjl692iController();
