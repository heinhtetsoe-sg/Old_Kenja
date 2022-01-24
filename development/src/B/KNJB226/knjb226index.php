<?php

require_once('for_php7.php');

require_once('knjb226Model.inc');
require_once('knjb226Query.inc');

class knjb226Controller extends Controller
{
    public $ModelClassName = "knjb226Model";
    public $ProgramID      = "KNJB226";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb226":
                case "rishuu":
                    $sessionInstance->knjb226Model();
                    $this->callView("knjb226Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjb226Form1");
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
$knjb226Ctl = new knjb226Controller();
