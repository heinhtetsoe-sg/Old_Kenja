<?php

require_once('for_php7.php');

require_once('knjd151Model.inc');
require_once('knjd151Query.inc');

class knjd151Controller extends Controller
{
    public $ModelClassName = "knjd151Model";
    public $ProgramID      = "KNJD151";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                case "knjd151":
                    $sessionInstance->knjd151Model();
                    $this->callView("knjd151Form1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjd151Form1");
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
$knjd151Ctl = new knjd151Controller();
