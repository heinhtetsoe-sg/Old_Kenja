<?php

require_once('for_php7.php');

require_once('knjd624gModel.inc');
require_once('knjd624gQuery.inc');

class knjd624gController extends Controller {
    var $ModelClassName = "knjd624gModel";
    var $ProgramID      = "KNJD624G";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd624g":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd624gModel();       //コントロールマスタの呼び出し
                    $this->callView("knjd624gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjd624gCtl = new knjd624gController;
//var_dump($_REQUEST);
?>
