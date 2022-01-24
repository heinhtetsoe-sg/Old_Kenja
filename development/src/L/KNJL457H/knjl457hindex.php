<?php

require_once('for_php7.php');

require_once('knjl457hModel.inc');
require_once('knjl457hQuery.inc');

class knjl457hController extends Controller
{
    public $ModelClassName = "knjl457hModel";
    public $ProgramID      = "KNJL457H";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                //CSV出力
                case "csv":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl457hForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl457hForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl457hCtl = new knjl457hController();
