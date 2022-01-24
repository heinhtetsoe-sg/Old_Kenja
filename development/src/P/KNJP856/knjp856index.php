<?php

require_once('for_php7.php');

require_once('knjp856Model.inc');
require_once('knjp856Query.inc');

class knjp856Controller extends Controller
{
    public $ModelClassName = "knjp856Model";
    public $ProgramID      = "KNJP856";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "header"://CSV出力
                    if (!$sessionInstance->getDownloadHeaderModel()) {
                        $this->callView("knjp856Form1");
                    }
                    break 2;
                case "exec"://CSV取込
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "err"://CSV出力
                    if (!$sessionInstance->getDownloadErrorModel()) {
                        $this->callView("knjp856Form1");
                    }
                    break 2;
                case "copy"://CSV取込
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjp856Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp856Ctl = new knjp856Controller();
