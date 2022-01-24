<?php

require_once('for_php7.php');

require_once('knjm550wModel.inc');
require_once('knjm550wQuery.inc');

class knjm550wController extends Controller
{
    public $ModelClassName = "knjm550wModel";
    public $ProgramID      = "KNJM500";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm550w_1":
                case "knjm550w_1Search":
                case "read":
                case "meibo":
                    $sessionInstance->knjm550wModel();
                    $this->callView("knjm550w_1Form1");
                    exit;
                case "knjm550w_3":
                case "knjm550w_3Search":
                case "change_class":
                    $sessionInstance->knjm550wModel();
                    $this->callView("knjm550w_3Form1");
                    exit;
                case "knjm550w_2":
                case "knjm550w_2Search":
                    $sessionInstance->knjm550wModel();
                    $this->callView("knjm550w_2Form1");
                    exit;
                case "clschange":
                    $sessionInstance->knjm550wModel();
                    $this->callView("knjm550w_1Form1");
                    exit;
                case "csv":     //CSV出力
                    $sessionInstance->getDownloadModel();
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm550wCtl = new knjm550wController();
