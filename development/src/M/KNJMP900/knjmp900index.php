<?php

require_once('for_php7.php');

require_once('knjmp900Model.inc');
require_once('knjmp900Query.inc');

class knjmp900Controller extends Controller {
    var $ModelClassName = "knjmp900Model";
    var $ProgramID      = "KNJMP900";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "knjmp900":
                case "clear":
                case "search":
                    $sessionInstance->knjmp900Model();       //コントロールマスタの呼び出し
                    $this->callView("knjmp900Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjmp900Ctl = new knjmp900Controller;
?>
