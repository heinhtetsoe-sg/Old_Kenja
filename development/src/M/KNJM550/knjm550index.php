<?php

require_once('for_php7.php');

require_once('knjm550Model.inc');
require_once('knjm550Query.inc');

class knjm550Controller extends Controller
{
    public $ModelClassName = "knjm550Model";
    public $ProgramID      = "KNJM500";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm550_1":
                case "knjm550_1Search":
                case "read":
                case "meibo":
                    $sessionInstance->knjm550Model();
                    $this->callView("knjm550_1Form1");
                    exit;
                case "knjm550_3":
                case "knjm550_3Search":
                case "change_class":
                    $sessionInstance->knjm550Model();
                    $this->callView("knjm550_3Form1");
                    exit;
                case "knjm550_2":
                case "knjm550_2Search":
                    $sessionInstance->knjm550Model();
                    $this->callView("knjm550_2Form1");
                    exit;
                case "clschange":
                    $sessionInstance->knjm550Model();
                    $this->callView("knjm550_1Form1");
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
$knjm550Ctl = new knjm550Controller();
