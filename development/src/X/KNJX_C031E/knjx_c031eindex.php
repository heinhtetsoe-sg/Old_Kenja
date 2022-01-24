<?php

require_once('for_php7.php');

require_once('knjx_c031eModel.inc');
require_once('knjx_c031eQuery.inc');

class knjx_c031eController extends Controller
{
    public $ModelClassName = "knjx_c031eModel";
    public $ProgramID      = "KNJX_C031E";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //CSV取込
                    $sessionInstance->setAccessLogDetail("EI", $ProgramID);
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":     //CSV出力
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjx_c031eForm1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    break 2;
                case "":
                case "main":
                case "change_radio":
                    $sessionInstance->getMainModel();
                    $this->callView("knjx_c031eForm1");
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
$knjx_c031eCtl = new knjx_c031eController();
