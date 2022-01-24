<?php

require_once('for_php7.php');

require_once('knjh661Model.inc');
require_once('knjh661Query.inc');

class knjh661Controller extends Controller
{
    public $ModelClassName = "knjh661Model";
    public $ProgramID      = "KNJH661";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                //CSV取込
                case "exec":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                //CSV出力
                case "head":
                    if (!$sessionInstance->outputDataFileHead()) {
                        $this->callView("knjh661Form1");
                    }
                    break 2;
                case "error":
                    if (!$sessionInstance->outputDataFileError()) {
                        $this->callView("knjh661Form1");
                    }
                    break 2;
                case "data":
                    if (!$sessionInstance->outputDataFile()) {
                        $this->callView("knjh661Form1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjh661Form1");
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
$knjh661Ctl = new knjh661Controller();
