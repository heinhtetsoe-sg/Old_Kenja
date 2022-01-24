<?php

require_once('for_php7.php');

require_once('knjl610iModel.inc');
require_once('knjl610iQuery.inc');

class knjl610iController extends Controller
{
    public $ModelClassName = "knjl610iModel";
    public $ProgramID      = "KNJL610I";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl610i":
                    $sessionInstance->knjl610iModel();
                    $this->callView("knjl610iForm1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadCsvModel()) {
                        $this->callView("knjl610iForm1");
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
$knjl610iCtl = new knjl610iController();
