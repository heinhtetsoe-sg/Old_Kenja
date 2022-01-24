<?php

require_once('for_php7.php');

require_once('knjp806Model.inc');
require_once('knjp806Query.inc');

class knjp806Controller extends Controller {
    var $ModelClassName = "knjp806Model";
    var $ProgramID      = "KNJP806";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjp806":                              //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjp806Model();        //コントロールマスタの呼び出し
                    $this->callView("knjp806Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp806Ctl = new knjp806Controller;
//var_dump($_REQUEST);
?>
