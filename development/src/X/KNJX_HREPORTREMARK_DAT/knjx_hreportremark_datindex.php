<?php

require_once('for_php7.php');

require_once('knjx_hreportremark_datModel.inc');
require_once('knjx_hreportremark_datQuery.inc');

class knjx_hreportremark_datController extends Controller
{
    public $ModelClassName = "knjx_hreportremark_datModel";
    public $ProgramID      = "knjx_hreportremark_dat";

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
                        $this->callView("knjx_hreportremark_datForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjx_hreportremark_datForm1");
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
$knjx_hreportremark_datCtl = new knjx_hreportremark_datController();
