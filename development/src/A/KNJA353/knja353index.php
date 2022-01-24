<?php

require_once('for_php7.php');

require_once('knja353Model.inc');
require_once('knja353Query.inc');

class knja353Controller extends Controller
{
    public $ModelClassName = "knja353Model";
    public $ProgramID      = "KNJA353";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knja353Form1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knja353Form1");
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
$knja353Ctl = new knja353Controller();
