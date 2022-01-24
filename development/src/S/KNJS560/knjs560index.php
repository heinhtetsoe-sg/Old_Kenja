<?php

require_once('for_php7.php');

require_once('knjs560Model.inc');
require_once('knjs560Query.inc');

class knjs560Controller extends Controller
{
    public $ModelClassName = "knjs560Model";
    public $ProgramID      = "KNJS560";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":        //CSV取り込み
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "henkan":        //CSV取り込み
                    $sessionInstance->getExecModel();
                    break 2;
                case "csv":   //CSV出力
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjs560Form1");
                    }
                    break 2;
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjs560Form1");
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
$knjs560Ctl = new knjs560Controller();
