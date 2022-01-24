<?php

require_once('for_php7.php');

require_once('knjd624qModel.inc');
require_once('knjd624qQuery.inc');

class knjd624qController extends Controller {
    var $ModelClassName = "knjd624qModel";
    var $ProgramID      = "KNJD624Q";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd624q":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd624qModel();       //コントロールマスタの呼び出し
                    $this->callView("knjd624qForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjd624qCtl = new knjd624qController;
//var_dump($_REQUEST);
?>
