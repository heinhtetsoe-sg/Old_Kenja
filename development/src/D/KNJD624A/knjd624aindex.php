<?php

require_once('for_php7.php');

require_once('knjd624aModel.inc');
require_once('knjd624aQuery.inc');

class knjd624aController extends Controller {
    var $ModelClassName = "knjd624aModel";
    var $ProgramID      = "KNJD624A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd624a":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd624aModel();       //コントロールマスタの呼び出し
                    $this->callView("knjd624aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjd624aCtl = new knjd624aController;
//var_dump($_REQUEST);
?>
