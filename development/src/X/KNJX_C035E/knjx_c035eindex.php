<?php

require_once('for_php7.php');

require_once('knjx_c035eModel.inc');
require_once('knjx_c035eQuery.inc');

class knjx_c035eController extends Controller
{
    public $ModelClassName = "knjx_c035eModel";
    public $ProgramID      = "KNJX_C035E";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //CSV取込
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    $sessionInstance->setAccessLogDetail("EI", $ProgramID);
                    break 1;
                case "csv":     //CSV出力
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjx_c035eForm1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    break 2;
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjx_c035eForm1");
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
$knjx_c035eCtl = new knjx_c035eController();
