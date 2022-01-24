<?php

require_once('for_php7.php');

require_once('knjd620eModel.inc');
require_once('knjd620eQuery.inc');

class knjd620eController extends Controller
{
    public $ModelClassName = "knjd620eModel";
    public $ProgramID      = "KNJD620E";

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
                        $this->callView("knjd620eForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjd620eForm1");
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
$knjd620eCtl = new knjd620eController();
