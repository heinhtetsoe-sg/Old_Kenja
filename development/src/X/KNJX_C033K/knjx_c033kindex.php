<?php

require_once('for_php7.php');

require_once('knjx_c033kModel.inc');
require_once('knjx_c033kQuery.inc');

class knjx_c033kController extends Controller
{
    public $ModelClassName = "knjx_c033kModel";
    public $ProgramID      = "KNJX_C033K";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //CSV取込
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":     //CSV出力
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjx_c033kForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjx_c033kForm1");
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
$knjx_c033kCtl = new knjx_c033kController();
