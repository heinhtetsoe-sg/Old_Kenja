<?php

require_once('for_php7.php');

require_once('knjl678iModel.inc');
require_once('knjl678iQuery.inc');

class knjl678iController extends Controller
{
    public $ModelClassName = "knjl678iModel";
    public $ProgramID      = "KNJL678I";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl678i":
                    $sessionInstance->knjl678iModel();
                    $this->callView("knjl678iForm1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadCsvModel()) {
                        $this->callView("knjl678iForm1");
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
$knjl678iCtl = new knjl678iController();
