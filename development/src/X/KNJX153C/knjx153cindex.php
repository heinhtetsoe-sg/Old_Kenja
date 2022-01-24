<?php

require_once('for_php7.php');

require_once('knjx153cModel.inc');
require_once('knjx153cQuery.inc');

class knjx153cController extends Controller
{
    public $ModelClassName = "knjx153cModel";
    public $ProgramID      = "KNJX153C";

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
                        $this->callView("knjx153cForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjx153cForm1");
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
$knjx153cCtl = new knjx153cController();
