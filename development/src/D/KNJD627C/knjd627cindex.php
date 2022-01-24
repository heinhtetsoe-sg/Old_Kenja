<?php

require_once('for_php7.php');
require_once('knjd627cModel.inc');
require_once('knjd627cQuery.inc');

class knjd627cController extends Controller
{
    public $ModelClassName = "knjd627cModel";
    public $ProgramID      = "KNJD627C";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        $sessionInstance->programID = $this->ProgramID;

        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "change":
                    $this->callView("knjd627cForm1");
                    break 2;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjd627cForm1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    break 2;
                case "":
                case "main":
                    $this->callView("knjd627cForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd627cCtl = new knjd627cController();
