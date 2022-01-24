<?php

require_once('for_php7.php');

require_once('knjl060fModel.inc');
require_once('knjl060fQuery.inc');

class knjl060fController extends Controller
{
    public $ModelClassName = "knjl060fModel";
    public $ProgramID      = "KNJL060F";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl060f":
                    $sessionInstance->knjl060fModel();
                    $this->callView("knjl060fForm1");
                    exit;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knjl060f");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl060fCtl = new knjl060fController();
//var_dump($_REQUEST);
