<?php

require_once('for_php7.php');

require_once('knjm436Model.inc');
require_once('knjm436Query.inc');

class knjm436Controller extends Controller
{
    public $ModelClassName = "knjm436Model";
    public $ProgramID      = "knjm436";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":        //CSV取り込み
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":   //CSV出力
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjm436Form1");
                    }
                    break 2;
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjm436Form1");
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
$knjm436Ctl = new knjm436Controller();
