<?php

require_once('for_php7.php');

require_once('knjp900Model.inc');
require_once('knjp900Query.inc');

class knjp900Controller extends Controller {
    var $ModelClassName = "knjp900Model";
    var $ProgramID      = "KNJP900";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "knjp900":
                case "clear":
                case "search":
                    $sessionInstance->knjp900Model();       //コントロールマスタの呼び出し
                    $this->callView("knjp900Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp900Ctl = new knjp900Controller();
?>
