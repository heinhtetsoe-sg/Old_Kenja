<?php

require_once('for_php7.php');

require_once('knjx_l018vModel.inc');
require_once('knjx_l018vQuery.inc');

class knjx_l018vController extends Controller
{
    public $ModelClassName = "knjx_l018vModel";
    public $ProgramID      = "KNJX_L018V";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //CSV取込
                    $sessionInstance->getUploadModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":     //CSV出力
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjx_l018vForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjx_l018vForm1");
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
$knjx_l018vCtl = new knjx_l018vController();
