<?php

require_once('for_php7.php');

require_once('knjx093dModel.inc');
require_once('knjx093dQuery.inc');

class knjx093dController extends Controller
{
    public $ModelClassName = "knjx093dModel";
    public $ProgramID      = "KNJX093D";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                //CSV取込
                case "exec":
                    $sessionInstance->setAccessLogDetail("E", $ProgramID);
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                //CSV出力
                case "csv":
                case "head":
                case "error":
                case "data":
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjx093dForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjx093dForm1");
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
$knjx093dCtl = new knjx093dController();
