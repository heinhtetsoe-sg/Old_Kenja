<?php
require_once('knjx_h111aModel.inc');
require_once('knjx_h111aQuery.inc');

class knjx_h111aController extends Controller
{
    public $ModelClassName = "knjx_h111aModel";
    public $ProgramID      = "KNJX_H111A";

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
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjx_h111aForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $sessionInstance->knjx_h111aModel();
                    $this->callView("knjx_h111aForm1");
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
$knjx_h111aCtl = new knjx_h111aController();
