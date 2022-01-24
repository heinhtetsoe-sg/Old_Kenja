<?php

require_once('for_php7.php');

require_once('knjl640iModel.inc');
require_once('knjl640iQuery.inc');

class knjl640iController extends Controller
{
    public $ModelClassName = "knjl640iModel";
    public $ProgramID      = "KNJL640I";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl640i":
                    $sessionInstance->knjl640iModel();
                    $this->callView("knjl640iForm1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadCsvModel()) {
                        $this->callView("knjl640iForm1");
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
$knjl640iCtl = new knjl640iController();
