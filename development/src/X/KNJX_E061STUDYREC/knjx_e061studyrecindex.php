<?php

require_once('for_php7.php');

require_once('knjx_e061studyrecModel.inc');
require_once('knjx_e061studyrecQuery.inc');

class knjx_e061studyrecController extends Controller
{
    public $ModelClassName = "knjx_e061studyrecModel";
    public $ProgramID      = "KNJX_E061STUDYREC";

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
                        $this->callView("knjx_e061studyrecForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjx_e061studyrecForm1");
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
$knjx_e061studyrecCtl = new knjx_e061studyrecController();
