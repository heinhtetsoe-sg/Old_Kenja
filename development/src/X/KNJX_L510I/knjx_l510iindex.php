<?php

require_once('for_php7.php');

require_once('knjx_l510iModel.inc');
require_once('knjx_l510iQuery.inc');

class knjx_l510iController extends Controller
{
    public $ModelClassName = "knjx_l510iModel";
    public $ProgramID      = "KNJX_L510I";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":     //CSV出力
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjx_l510iForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjx_l510iForm1");
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
$knjx_l510iCtl = new knjx_l510iController();
