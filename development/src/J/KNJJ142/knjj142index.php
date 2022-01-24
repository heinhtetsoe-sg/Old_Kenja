<?php

require_once('for_php7.php');

require_once('knjj142Model.inc');
require_once('knjj142Query.inc');

class knjj142Controller extends Controller {
    var $ModelClassName = "knjj142Model";
    var $ProgramID      = "KNJJ142";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main";
                    $this->callView("knjj142Form1");
                    break 2;
                case "knjj142":                              //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjj142Model();        //コントロールマスタの呼び出し
                    $this->callView("knjj142Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjj142Ctl = new knjj142Controller;
?>
