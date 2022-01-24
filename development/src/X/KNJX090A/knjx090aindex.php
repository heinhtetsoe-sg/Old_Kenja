<?php

require_once('for_php7.php');

require_once('knjx090aModel.inc');
require_once('knjx090aQuery.inc');

class knjx090aController extends Controller
{
    public $ModelClassName = "knjx090aModel";
    public $ProgramID      = "KNJX090A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "csv2": // 自動生成
                    $sessionInstance->setAccessLogDetail("IEO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel2()) {
                        $this->callView("knjx090aForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->getMainModel();
                    $this->callView("knjx090aForm1");
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
$knjx090aCtl = new knjx090aController();
