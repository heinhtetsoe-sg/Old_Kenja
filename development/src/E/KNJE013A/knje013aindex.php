<?php
require_once('knje013aModel.inc');
require_once('knje013aQuery.inc');

class knje013aController extends Controller
{
    public $ModelClassName = "knje013aModel";
    public $ProgramID      = "KNJE013A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje013a":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knje013aModel();
                    $this->callView("knje013aForm1");
                    exit;
                case "exec":     //実行
                    if (!$sessionInstance->getUpdateModel()) {
                        $this->callView("knje013aForm1");
                    }
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje013aCtl = new knje013aController;
