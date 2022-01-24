<?php

require_once('for_php7.php');

require_once('knjx155csvModel.inc');
require_once('knjx155csvQuery.inc');

class knjx155csvController extends Controller
{
    public $ModelClassName = "knjx155csvModel";
    public $ProgramID      = "KNJX155CSV";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //CSV取込
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":       //CSV出力
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjx155csvForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjx155csvForm1");
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
$knjx155csvCtl = new knjx155csvController();
