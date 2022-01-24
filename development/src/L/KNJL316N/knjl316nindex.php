<?php

require_once('for_php7.php');

require_once('knjl316nModel.inc');
require_once('knjl316nQuery.inc');

class knjl316nController extends Controller
{
    public $ModelClassName = "knjl316nModel";
    public $ProgramID      = "KNJL316N";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                //CSV出力
                case "csv":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl316nForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl316nForm1");
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
$knjl316nCtl = new knjl316nController();
