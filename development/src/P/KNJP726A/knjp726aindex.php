<?php

require_once('for_php7.php');

require_once('knjp726aModel.inc');
require_once('knjp726aQuery.inc');

class knjp726aController extends Controller
{
    public $ModelClassName = "knjp726aModel";
    public $ProgramID      = "KNJP726A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                //CSV出力
                case "exec":
                    if (!$sessionInstance->outputDataFile()) {
                        $this->callView("knjp726aForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjp726aForm1");
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
$knjp726aCtl = new knjp726aController();
