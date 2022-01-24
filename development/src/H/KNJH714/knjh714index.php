<?php

require_once('for_php7.php');

require_once('knjh714Model.inc');
require_once('knjh714Query.inc');

class knjh714Controller extends Controller
{
    public $ModelClassName = "knjh714Model";
    public $ProgramID      = "KNJH714";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":
                case "change_header":
                    $sessionInstance->knjh714Model();
                    $this->callView("knjh714Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh714Ctl = new knjh714Controller();
