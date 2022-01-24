<?php

require_once('for_php7.php');

require_once('knjp851Model.inc');
require_once('knjp851Query.inc');

class knjp851Controller extends Controller
{
    public $ModelClassName = "knjp851Model";
    public $ProgramID      = "KNJP851";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                //CSV出力
                case "csv":
                    if (!$sessionInstance->OutputDataFile()) {
                        $this->callView("knjp851Form1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjp851Form1");
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
$knjp851Ctl = new knjp851Controller();
