<?php

require_once('for_php7.php');

require_once('knjl017dModel.inc');
require_once('knjl017dQuery.inc');

class knjl017dController extends Controller
{
    public $ModelClassName = "knjl017dModel";
    public $ProgramID      = "KNJL017D";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "read":
                case "reset":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl017dForm1");
                    break 2;
                case "csv":
                    $sessionInstance->downloadCsvFile();
                    $sessionInstance->setCmd("");
                    break 2;
                case "sort_j_lang":
                case "sort_e_lang":
                case "sort_math":
                case "sort_total":
                    $sessionInstance->sortSetting(trim($sessionInstance->cmd));
                    $this->callView("knjl017dForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl017dCtl = new knjl017dController();
