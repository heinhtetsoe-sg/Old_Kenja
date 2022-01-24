<?php

require_once('for_php7.php');

require_once('knjh718Model.inc');
require_once('knjh718Query.inc');

class knjh718Controller extends Controller
{
    public $ModelClassName = "knjh718Model";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh718":
                    $sessionInstance->knjh718Model();
                    $this->callView("knjh718Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh718Ctl = new knjh718Controller();
