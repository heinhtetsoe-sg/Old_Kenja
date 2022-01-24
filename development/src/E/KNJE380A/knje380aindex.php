<?php
require_once('for_php7.php');

require_once('knje380aModel.inc');
require_once('knje380aQuery.inc');

class knje380aController extends Controller
{
    public $ModelClassName = "knje380aModel";
    public $ProgramID      = "KNJE380A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":        //CSV出力
                    $sessionInstance->setAccessLogDetail("E", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knje380aForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->getMainModel();
                    $this->callView("knje380aForm1");
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
$knje380aCtl = new knje380aController();
