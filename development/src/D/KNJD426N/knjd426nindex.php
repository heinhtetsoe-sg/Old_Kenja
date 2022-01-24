<?php

require_once('for_php7.php');

require_once('knjd426nModel.inc');
require_once('knjd426nQuery.inc');

class knjd426nController extends Controller
{
    public $ModelClassName = "knjd426nModel";
    public $ProgramID      = "KNJD426N";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":
                case "changePtrn":
                case "changeHukusiki":
                case "main":
                case "seldate":
                case "clear":
                case "knjd426n":
                    $sessionInstance->knjd426nModel();
                    $this->callView("knjd426nForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd426nCtl = new knjd426nController();
