<?php

require_once('for_php7.php');

require_once('knjd616qModel.inc');
require_once('knjd616qQuery.inc');

class knjd616qController extends Controller {
    var $ModelClassName = "knjd616qModel";
    var $ProgramID      = "KNJD616Q";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd616q":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd616qModel();       //コントロールマスタの呼び出し
                    $this->callView("knjd616qForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjd616qCtl = new knjd616qController;
//var_dump($_REQUEST);
?>
