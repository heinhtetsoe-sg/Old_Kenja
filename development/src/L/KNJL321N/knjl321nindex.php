<?php

require_once('for_php7.php');

require_once('knjl321nModel.inc');
require_once('knjl321nQuery.inc');

class knjl321nController extends Controller
{
    public $ModelClassName = "knjl321nModel";
    public $ProgramID      = "KNJL321N";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl321n":
                    $sessionInstance->knjl321nModel();
                    $this->callView("knjl321nForm1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl321nForm1");
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
$knjl321nCtl = new knjl321nController();
//var_dump($_REQUEST);
