<?php

require_once('for_php7.php');

require_once('knjx101Model.inc');
require_once('knjx101Query.inc');

class knjx101Controller extends Controller
{
    public $ModelClassName = "knjx101Model";
    public $ProgramID      = "KNJX101";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":        //CSV取り込み
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":         //CSV出力
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjx101Form1");
                    }
                    break 2;
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjx101Form1");
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
$knjx101Ctl = new knjx101Controller();
