<?php

require_once('for_php7.php');

require_once('knjx_e372cModel.inc');
require_once('knjx_e372cQuery.inc');

class knjx_e372cController extends Controller
{
    public $ModelClassName = "knjx_e372cModel";
    public $ProgramID      = "KNJX_E372C";

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
                        $this->callView("knjx_e372cForm1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    break 2;
                case "":
                case "main":
                case "change_radio":
                    $sessionInstance->getMainModel();
                    $this->callView("knjx_e372cForm1");
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
$knjx_e372cCtl = new knjx_e372cController();
