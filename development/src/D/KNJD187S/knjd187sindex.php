<?php

require_once('for_php7.php');
require_once('knjd187sModel.inc');
require_once('knjd187sQuery.inc');

class knjd187sController extends Controller
{
    public $ModelClassName = "knjd187sModel";
    public $ProgramID      = "KNJD187S";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        $sessionInstance->programID = $this->ProgramID;

        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "change":
                    $this->callView("knjd187sForm1");
                    break 2;
                case "":
                case "main":
                    $this->callView("knjd187sForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd187sCtl = new knjd187sController();
