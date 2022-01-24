<?php

require_once('for_php7.php');

require_once('knjp392qModel.inc');
require_once('knjp392qQuery.inc');

class knjp392qController extends Controller {
    var $ModelClassName = "knjp392qModel";
    var $ProgramID      = "KNJP392Q";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjp392q":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjp392qModel();      //コントロールマスタの呼び出し
                    $this->callView("knjp392qForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjp392qCtl = new knjp392qController;
var_dump($_REQUEST);
?>
