<?php

require_once('for_php7.php');

require_once('knjmp953Model.inc');
require_once('knjmp953Query.inc');

class knjmp953Controller extends Controller {
    var $ModelClassName = "knjmp953Model";
    var $ProgramID      = "KNJMP953";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjmp953":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjmp953Model();       //コントロールマスタの呼び出し
                    $this->callView("knjmp953Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjmp953Ctl = new knjmp953Controller;
?>
