<?php

require_once('for_php7.php');

require_once('knjp940Model.inc');
require_once('knjp940Query.inc');

class knjp940Controller extends Controller {
    var $ModelClassName = "knjp940Model";
    var $ProgramID      = "KNJP940";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "knjp940":
                case "clear":
                case "search":
                    $sessionInstance->knjp940Model();       //コントロールマスタの呼び出し
                    $this->callView("knjp940Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp940Ctl = new knjp940Controller;
?>
