<?php

require_once('for_php7.php');

require_once('knjl615aModel.inc');
require_once('knjl615aQuery.inc');

class knjl615aController extends Controller
{
    public $ModelClassName = "knjl615aModel";
    public $ProgramID      = "KNJL615A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //CSV取込
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":       //CSV出力
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl615aForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl615aForm1");
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
$knjl615aCtl = new knjl615aController;
?>
