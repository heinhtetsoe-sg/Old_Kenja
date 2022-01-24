<?php

require_once('for_php7.php');

require_once('knjd141cModel.inc');
require_once('knjd141cQuery.inc');

class knjd141cController extends Controller
{
    public $ModelClassName = "knjd141cModel";
    public $ProgramID      = "KNJD141C";

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
                    $this->callView("knjd141cForm1");
                    break 2;
                case "main":
                    $this->callView("knjd141cForm1");
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
$knjd141cCtl = new knjd141cController();
