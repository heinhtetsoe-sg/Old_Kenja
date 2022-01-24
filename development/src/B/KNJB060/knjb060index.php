<?php

require_once('for_php7.php');

require_once('knjb060Model.inc');
require_once('knjb060Query.inc');

class knjb060Controller extends Controller
{
    public $ModelClassName = "knjb060Model";
    public $ProgramID      = "KNJB060";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb060":
                    $this->callView("knjb060Form1");
                    break 2;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjb060Form1");
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
$knjb060Ctl = new knjb060Controller();
//var_dump($_REQUEST);
