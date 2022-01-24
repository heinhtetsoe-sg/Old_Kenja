<?php

require_once('for_php7.php');

require_once('knjb224Model.inc');
require_once('knjb224Query.inc');

class knjb224Controller extends Controller
{
    public $ModelClassName = "knjb224Model";
    public $ProgramID      = "KNJB224";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb224":
                    $sessionInstance->knjb224Model();
                    $this->callView("knjb224Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjb224Form1");
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
$knjb224Ctl = new knjb224Controller();
