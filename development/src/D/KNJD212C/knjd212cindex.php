<?php

require_once('for_php7.php');


require_once('knjd212cModel.inc');
require_once('knjd212cQuery.inc');

class knjd212cController extends Controller
{
    public $ModelClassName = "knjd212cModel";
    public $ProgramID      = "KNJD212C";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "execute":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjd212cForm1");
                    break 2;
                case "main":
                    $this->callView("knjd212cForm1");
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
$knjd212cCtl = new knjd212cController();
?>
