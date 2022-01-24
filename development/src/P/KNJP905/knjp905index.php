<?php

require_once('for_php7.php');

require_once('knjp905Model.inc');
require_once('knjp905Query.inc');

class knjp905Controller extends Controller
{
    public $ModelClassName = "knjp905Model";
    public $ProgramID      = "KNJP905";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "knjp905":
                case "clear":
                case "search":
                    $sessionInstance->knjp905Model();       //コントロールマスタの呼び出し
                    $this->callView("knjp905Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp905Ctl = new knjp905Controller();
