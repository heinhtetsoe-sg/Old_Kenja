<?php

require_once('for_php7.php');

require_once('knjp855Model.inc');
require_once('knjp855Query.inc');

class knjp855Controller extends Controller
{
    public $ModelClassName = "knjp855Model";
    public $ProgramID      = "KNJP855";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "change_grade":
                case "chk_month":
                    $this->callView("knjp855Form1");
                    exit;
                //CSV出力
                case "data":
                case "err":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjp855Form1");
                    }
                    $sessionInstance->setCmd("main");
                    break 2;
                //CSV取込
                case "exec":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp855Ctl = new knjp855Controller();
