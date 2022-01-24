<?php

require_once('for_php7.php');

require_once('knjl630iModel.inc');
require_once('knjl630iQuery.inc');

class knjl630iController extends Controller
{
    public $ModelClassName = "knjl630iModel";
    public $ProgramID      = "KNJL630I";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl630i":
                    $sessionInstance->knjl630iModel();
                    $this->callView("knjl630iForm1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadCsvModel()) {
                        $this->callView("knjl630iForm1");
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
$knjl630iCtl = new knjl630iController();
